#!/usr/bin/env python3
"""Merge reviewed extraction geometry only, preserving every rig/art byte."""
from __future__ import annotations

import argparse
import copy
import hashlib
import importlib.util
import json
from pathlib import Path

import numpy as np
from PIL import Image

SPEC = importlib.util.spec_from_file_location("clips", Path(__file__).with_name("prototype-source-clips.py"))
CLIPS = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(CLIPS)
FRAME_GEOMETRY = {"sourceRect", "sourceClip", "bodyBounds", "bodyArea", "bodyAlphaThreshold", "sourceRectProvenance", "sourceClipProvenance"}
SHEET_GEOMETRY = {"frameBoundsMethod", "frameBoundsDiagnostics", "sourceClipsDerived", "sourceClipSelection"}


def sha(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def invariant_digest(manifest: dict) -> str:
    invariant = copy.deepcopy(manifest)
    for key in SHEET_GEOMETRY:
        invariant.pop(key, None)
    for frame in invariant["frames"]:
        for key in FRAME_GEOMETRY:
            frame.pop(key, None)
    return hashlib.sha256(json.dumps(invariant, sort_keys=True, ensure_ascii=False).encode()).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--project", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--candidates", type=Path, default=Path("docs/validation/v1.18/source-clip-selected"))
    parser.add_argument("--apply", action="store_true")
    args = parser.parse_args()
    root = args.project.resolve()
    candidates = root / args.candidates
    paths = sorted((root / "app/src/main/assets/art/v118").glob("*.json"))
    if len(paths) != 148:
        raise ValueError("Refusing incomplete atlas set")
    snapshots = root / "docs/art/v118/metadata-before-source-clips"
    if args.apply:
        snapshots.mkdir(parents=True, exist_ok=True)
    receipts, geometry_tree = [], []
    for path in paths:
        proposal = json.loads((candidates / path.name).read_text())
        if proposal.get("selectionStatus") != "candidate-requires-visual-native-review" or not proposal["geometryInputsCompatible"]:
            raise ValueError(f"Unreviewable/stale proposal {path.name}")
        original = root / proposal["image"].lstrip("/")
        image_hash = sha(original)
        with Image.open(original) as image:
            alpha = np.asarray(image.convert("RGBA"))[:, :, 3]
        measured = []
        for frame in proposal["frames"]:
            l, t, r, b = frame["sourceRect"]
            polygon = [[x - l, y - t] for x, y in frame["sourceClip"]]
            domain = CLIPS.raster_polygon(polygon, (b - t, r - l))
            ys, xs = np.nonzero(domain & (alpha[t:b, l:r] >= 128))
            if not len(xs):
                raise ValueError(f"Empty actual polygon body {path.name}/{frame['poseIndex']}")
            measured.append(([l + int(xs.min()), t + int(ys.min()), l + int(xs.max()) + 1, t + int(ys.max()) + 1], int(len(xs))))
        # Re-read immediately before the write so a separately staged rig merge
        # is preserved. Optimistic check refuses an intervening concurrent edit.
        before_bytes = path.read_bytes()
        manifest = json.loads(before_bytes)
        if manifest["originalSha256"] != image_hash or proposal["originalSha256"] != image_hash:
            raise ValueError(f"Selected image changed {path.name}")
        webp = path.with_suffix(".webp")
        webp_hash = sha(webp)
        if webp_hash != manifest["webpSha256"]:
            raise ValueError(f"Runtime image checksum mismatch {path.name}")
        invariant_before = invariant_digest(manifest)
        result = copy.deepcopy(manifest)
        for old, fresh, candidate, (body, area) in zip(manifest["frames"], result["frames"], proposal["frames"], measured):
            if old["sourceRect"] != candidate["oldSourceRect"]:
                raise ValueError(f"Recorded crop changed before merge {path.name}/{old['poseIndex']}")
            for key in ("originX", "originY", "pixelsPerUnit"):
                if old[key] != candidate[key]:
                    raise ValueError(f"Fixed body transform changed {path.name}/{old['poseIndex']}/{key}")
            fresh.update(sourceRect=candidate["sourceRect"], sourceClip=candidate["sourceClip"], bodyBounds=body,
                         bodyArea=area, bodyAlphaThreshold=128,
                         sourceRectProvenance="Bounding box of the curved gutter extraction polygon; source pixels unchanged; fixed origin and pixelsPerUnit preserved",
                         sourceClipProvenance=proposal["selectionPolicy"] + "; browser counterexamples reviewed; native verification required")
        result.update(frameBoundsMethod="curved-alpha-gutters-selected-v1", sourceClipsDerived=True,
                      sourceClipSelection={"policy": proposal["selectionPolicy"], "proposal": str((candidates / path.name).relative_to(root)),
                                           "maximumPolygonVertices": max(len(f["sourceClip"]) for f in result["frames"]),
                                           "qualifiedApparel": False, "nativeVerificationPending": True},
                      frameBoundsDiagnostics={"referenceRectangleDiagnostics": manifest.get("frameBoundsDiagnostics"),
                                              "matchedInitialConnectedComponents": sum(c["matched"] for c in proposal["coreComponents"]),
                                              "initialConnectedComponentPixelsOutsideCandidateDomains": proposal["ownCorePixelsLostAfter"],
                                              "note": "Initial components may include neighbouring fringe; this diagnostic is not semantic/anatomical proof"})
        invariant_after = invariant_digest(result)
        if invariant_before != invariant_after:
            raise ValueError(f"Forbidden nongeometry mutation {path.name}")
        if args.apply:
            snapshot = snapshots / path.name
            if snapshot.exists():
                raise ValueError(f"Snapshot already exists; refusing an unreviewed repeated merge {path.name}")
            snapshot.write_bytes(before_bytes)
            if path.read_bytes() != before_bytes:
                raise ValueError(f"Concurrent metadata edit; nothing overwritten {path.name}")
            temporary = path.with_suffix(".json.tmp")
            temporary.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n")
            temporary.replace(path)
        if sha(original) != image_hash or sha(webp) != webp_hash:
            raise ValueError(f"Image bytes changed during geometry merge {path.name}")
        geometry_tree.append({"sheet": path.name, "frames": [{k: f[k] for k in ["poseIndex", "sourceRect", "sourceClip", "bodyBounds", "bodyArea"]} for f in result["frames"]]})
        receipts.append({"sheet": path.name, "policy": proposal["selectionPolicy"], "originalSha256": image_hash,
                         "webpSha256": webp_hash, "beforeMetadataSha256": hashlib.sha256(before_bytes).hexdigest(),
                         "afterMetadataSha256": sha(path) if args.apply else None, "nongeometryInvariantSha256": invariant_after,
                         "nongeometryPreserved": invariant_before == invariant_after})
    report = {"schema": 1, "applied": args.apply, "sheetCount": len(receipts), "frameCount": 2960,
              "pixelsModified": False, "allNongeometryFieldsPreserved": all(r["nongeometryPreserved"] for r in receipts),
              "geometryTreeSha256": hashlib.sha256(json.dumps(geometry_tree, sort_keys=True, separators=(",", ":")).encode()).hexdigest(),
              "qualifiedApparel": False, "nativeVerificationPending": True, "receipts": receipts}
    report_path = candidates / "application-receipt.json"
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n")
    print(json.dumps({k: v for k, v in report.items() if k != "receipts"}))


if __name__ == "__main__":
    main()
