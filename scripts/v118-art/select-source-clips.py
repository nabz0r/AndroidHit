#!/usr/bin/env python3
"""Select separate atlas-geometry candidates; never writes runtime assets.

Use the whole-component curve only when it crosses no opaque interior pixels.
Use the bounded recorded-gutter curve on the flagged exceptions, so it cannot
descend deeply into a detached crescent/chapeau. Visual/native review remains
required. Complete-component counts are diagnostics, not an anatomy oracle.
"""
from __future__ import annotations

import argparse
import hashlib
import importlib.util
import json
from pathlib import Path

SPEC = importlib.util.spec_from_file_location("clip_prototype", Path(__file__).with_name("prototype-source-clips.py"))
CLIPS = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(CLIPS)


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def select(root: Path, full: Path, bounded: Path, output: Path):
    output.mkdir(parents=True, exist_ok=True)
    reports, stale = [], []
    paths = sorted((root / "app/src/main/assets/art/v118").glob("*.json"))
    for path in paths:
        manifest = json.loads(path.read_text())
        complete = json.loads((full / path.name).read_text())
        constrained = json.loads((bounded / path.name).read_text())
        opaque = sum(s["alpha248InteriorCrossings"] for s in complete["seams"])
        selected = constrained if opaque else complete
        policy = "bounded-recorded-gutter" if opaque else "whole-component-no-opaque-crossing"
        reason = "Avoid long excursions into detached ornaments on an opaque seam" if opaque else "Keep every measured component while following a nonopaque gutter"
        incompatible = []
        if selected["originalSha256"] != manifest["originalSha256"]:
            incompatible.append("selected original changed since proposal")
        for proposal, frame in zip(selected["frames"], manifest["frames"]):
            if proposal["oldSourceRect"] != frame["sourceRect"]:
                incompatible.append(f"pose {frame['poseIndex']} recorded crop changed")
            for key in ("originX", "originY", "pixelsPerUnit"):
                if proposal[key] != frame[key]:
                    incompatible.append(f"pose {frame['poseIndex']} {key} changed")
            if proposal["sourceClip"] is None or not 3 <= len(proposal["sourceClip"]) <= 256:
                incompatible.append(f"pose {frame['poseIndex']} polygon invalid")
        actual_original = root / manifest["original"]
        if digest(actual_original) != manifest["originalSha256"] or digest(path.with_suffix(".webp")) != manifest["webpSha256"]:
            incompatible.append("selected image checksum mismatch")
        if incompatible:
            stale.append({"sheet": path.stem, "issues": incompatible})
        record = dict(selected)
        record.update(selectionPolicy=policy, selectionReason=reason,
                      selectedProposal=str((bounded if opaque else full).relative_to(root) / path.name),
                      metadataSha256AtSelection=digest(path), geometryInputsCompatible=not incompatible,
                      selectionStatus="candidate-requires-visual-native-review", applied=False,
                      componentMetricNote="The initial connected component can contain a fringe bridge to a neighbouring appendage; counts are diagnostics, not semantic ownership proof.")
        (output / path.name).write_text(json.dumps(record, ensure_ascii=False, separators=(",", ":")) + "\n")
        reports.append(record)
    summary = {"status": "candidate-requires-visual-native-review", "applied": False,
               "runtimeFilesModified": False, "sheetCount": len(reports), "frameCount": sum(len(r["frames"]) for r in reports),
               "wholeComponentSheets": sum(r["selectionPolicy"].startswith("whole") for r in reports),
               "boundedSheets": [{"id": r["id"], "level": r["levelMilestone"]} for r in reports if r["selectionPolicy"].startswith("bounded")],
               "initialComponentPixelsOutsideOldRect": sum(r["ownCorePixelsLostBefore"] for r in reports),
               "initialComponentPixelsOutsideCandidateDomain": sum(r["ownCorePixelsLostAfter"] for r in reports),
               "maximumPolygonVertices": max(f["polygonVertices"] for r in reports for f in r["frames"]),
               "simplificationAlpha64PixelChanges": sum((f.get("simplification") or {}).get("alpha64PixelAssignmentChanges", 0) for r in reports for f in r["frames"]),
               "geometryInputsAllCompatible": not stale, "staleProposals": stale,
               "qualification": "Candidate geometry only. Does not certify all anatomy, alpha fringes or any clothing. Native review and counterexamples must pass before integration."}
    (output / "summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n")
    (output / "report.json").write_text(json.dumps({"schema": 1, "applied": False, "sheetCount": len(reports), "reports": reports}, ensure_ascii=False, separators=(",", ":")) + "\n")
    (output / "review.html").write_text(CLIPS.HTML.replace("RECORDS", json.dumps(reports, ensure_ascii=False, separators=(",", ":"))))
    print(json.dumps(summary, ensure_ascii=False))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--project", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--full", type=Path, default=Path("docs/validation/v1.18/source-clip-prototype-all"))
    parser.add_argument("--bounded", type=Path, default=Path("docs/validation/v1.18/source-clip-bounded-all"))
    parser.add_argument("--output", type=Path, default=Path("docs/validation/v1.18/source-clip-selected"))
    args = parser.parse_args()
    root = args.project.resolve()
    select(root, root / args.full, root / args.bounded, root / args.output)


if __name__ == "__main__":
    main()
