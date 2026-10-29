#!/usr/bin/env python3
"""Independently audit the complete v1.18 companion art release without modifying art.

Requires Pillow and NumPy. An exit code of zero means that all structural and
pixel-integrity checks passed; it does not certify pose semantics or apparel fit.
The explicit --packaged-only CI mode checks the shipped WebP pixels and a frozen
rig receipt, without requiring or claiming a new comparison to original PNGs.
"""
from __future__ import annotations

import argparse
import concurrent.futures
import datetime as dt
import hashlib
import json
import math
import re
import sys
import time
from collections import Counter
from pathlib import Path

import numpy as np
from PIL import Image

IDENTITIES = (
    "nova miko orbit pip astra zig lune fenn hoot buzz tula jelli vela nimbi soli "
    "mochi nixi prickly quill waddles halo emberling bambu inky "
    "lol_ashe lol_ezreal lol_soraka lol_ahri lol_jinx lol_lux lol_teemo "
    "lol_yasuo lol_annie lol_lulu lol_sona lol_miss_fortune lol_vi"
).split()
LEVELS = [1, 3, 6, 10]
POSES = ["front", "back", "left", "right", "walk_front", "walk_back", "walk_left", "walk_right",
         "jump_up", "land", "hug", "crouch", "happy", "sad", "surprise", "think",
         "calm", "ready", "guard", "proud"]
ATTACHMENTS = {"HAT", "DRESS", "LEFT_GLOVE", "RIGHT_GLOVE", "LEFT_SHOE", "RIGHT_SHOE",
               "HEAD_GEAR", "CHARM", "AURA"}
LAYERS = {"BACK_EFFECT", "FAR_LIMB", "BODY", "DRESS", "SHOE", "NEAR_HAND", "HAT", "FRONT_GEAR"}


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def finite(value) -> bool:
    return isinstance(value, (int, float)) and not isinstance(value, bool) and math.isfinite(value)


def exact_int(value) -> bool:
    return finite(value) and value == int(value)


def validate_source_clip(clip, source_rect: list) -> list:
    """Validate the closed polygon in atlas coordinates, including edge contacts."""
    if not isinstance(clip, list) or not 3 <= len(clip) <= 256:
        raise ValueError("sourceClip requires 3..256 vertices")
    left, top, right, bottom = source_rect
    points = []
    for point in clip:
        if not isinstance(point, list) or len(point) != 2 or not all(finite(v) for v in point):
            raise ValueError("sourceClip vertices require two finite numeric coordinates")
        x, y = point
        if not left <= x <= right or not top <= y <= bottom:
            raise ValueError("sourceClip vertex outside sourceRect")
        points.append((float(x), float(y)))
    if len(set(points)) != len(points):
        raise ValueError("sourceClip contains duplicate vertices or a repeated closing vertex")
    area_twice = sum(a[0] * b[1] - b[0] * a[1] for a, b in zip(points, points[1:] + points[:1]))
    if abs(area_twice) <= 1e-6:
        raise ValueError("sourceClip must enclose nonzero area")

    def cross(a, b, c):
        return (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0])

    def on_segment(a, b, p):
        return (abs(cross(a, b, p)) <= 1e-9
                and min(a[0], b[0]) <= p[0] <= max(a[0], b[0])
                and min(a[1], b[1]) <= p[1] <= max(a[1], b[1]))

    def intersects(a, b, c, d):
        if (max(a[0], b[0]) < min(c[0], d[0]) or max(c[0], d[0]) < min(a[0], b[0])
                or max(a[1], b[1]) < min(c[1], d[1]) or max(c[1], d[1]) < min(a[1], b[1])):
            return False
        ab_c, ab_d, cd_a, cd_b = cross(a, b, c), cross(a, b, d), cross(c, d, a), cross(c, d, b)
        if ((ab_c > 1e-9 and ab_d < -1e-9) or (ab_c < -1e-9 and ab_d > 1e-9)) and (
                (cd_a > 1e-9 and cd_b < -1e-9) or (cd_a < -1e-9 and cd_b > 1e-9)):
            return True
        return (on_segment(a, b, c) or on_segment(a, b, d)
                or on_segment(c, d, a) or on_segment(c, d, b))

    for index, current in enumerate(points):
        previous, following = points[index - 1], points[(index + 1) % len(points)]
        # Collinear forward edges are valid; reversing along an edge is not simple.
        if abs(cross(previous, current, following)) <= 1e-9:
            if ((previous[0] - current[0]) * (following[0] - current[0])
                    + (previous[1] - current[1]) * (following[1] - current[1])) > 0:
                raise ValueError("sourceClip adjacent edges overlap")
    for first in range(len(points)):
        for second in range(first + 1, len(points)):
            if second == first + 1 or (first == 0 and second == len(points) - 1):
                continue
            if intersects(points[first], points[(first + 1) % len(points)],
                          points[second], points[(second + 1) % len(points)]):
                raise ValueError("sourceClip is not simple: nonadjacent edges intersect or touch")
    return points


def source_domain_mask(source_rect: list, clip=None) -> np.ndarray:
    """Rasterize a validated clip at source-pixel centres, with half-open edges.

    None is the rectangle fallback for an absent sourceClip. Callers must reject
    an explicitly null sourceClip before using this helper, as the runtime does.
    """
    left, top, right, bottom = source_rect
    domain = np.zeros((bottom - top, right - left), dtype=bool)
    if clip is None:
        domain[:] = True
        return domain
    points = np.asarray(validate_source_clip(clip, source_rect), dtype=np.float64)
    points -= (left, top)
    starts, ends = points, np.roll(points, -1, axis=0)
    for y in range(max(0, math.floor(points[:, 1].min())), min(domain.shape[0], math.ceil(points[:, 1].max()))):
        cy = y + .5
        active = ((starts[:, 1] <= cy) & (ends[:, 1] > cy)) | ((ends[:, 1] <= cy) & (starts[:, 1] > cy))
        a, b = starts[active], ends[active]
        crossings = sorted((a[:, 0] + (cy - a[:, 1]) * (b[:, 0] - a[:, 0]) / (b[:, 1] - a[:, 1])).tolist())
        if len(crossings) % 2:
            raise ValueError("sourceClip has odd scanline intersections")
        for x1, x2 in zip(crossings[::2], crossings[1::2]):
            lo, hi = max(0, math.ceil(x1 - .5)), min(domain.shape[1], math.ceil(x2 - .5))
            domain[y, lo:hi] = True
    return domain


def audit_sheet(root: Path, identity: str, level: int, rig_revision: dict | None = None,
                packaged_only: bool = False) -> dict:
    stem = f"{identity}-evo{level:02}-poses"
    packaged = root / "app/src/main/assets/art/v118"
    metadata_path = packaged / f"{stem}.json"
    image_path = packaged / f"{stem}.webp"
    original_path = root / "docs/art/v118/originals" / f"{stem}.png"
    result = {"id": identity, "levelMilestone": level, "form": LEVELS.index(level),
              "metadata": str(metadata_path.relative_to(root)), "errors": [], "warnings": []}
    errors, warnings = result["errors"], result["warnings"]

    def check(condition: bool, message: str):
        if not condition:
            errors.append(message)

    required = [("metadata", metadata_path), ("WebP", image_path)]
    if not packaged_only:
        required.append(("original", original_path))
    for label, path in required:
        check(path.is_file(), f"Missing {label}: {path.relative_to(root)}")
    if errors:
        result["passed"] = False
        return result
    try:
        data = json.loads(metadata_path.read_text())
        check(metadata_path.stat().st_size <= 512 * 1024, "Metadata exceeds runtime 512 KiB read bound")
        with Image.open(image_path) as im:
            check(im.format == "WEBP", "Packaged image is not WebP")
            decoded = np.asarray(im.convert("RGBA"))
        if packaged_only:
            # Geometry and alpha below are audited from the actual shipped pixels.
            # Recorded PNG values are bound to the revision baseline, not remeasured.
            original = decoded
            original_sha = data.get("originalSha256")
            png_bytes = data.get("pngBytes")
            check(isinstance(original_sha, str) and re.fullmatch(r"[a-f0-9]{64}", original_sha) is not None,
                  "Invalid recorded original PNG SHA-256")
            check(exact_int(png_bytes) and png_bytes > 0, "Invalid recorded original PNG byte size")
        else:
            with Image.open(original_path) as im:
                check(im.format == "PNG", "Original is not PNG")
                original = np.asarray(im.convert("RGBA"))
            original_sha, png_bytes = sha256(original_path), original_path.stat().st_size
        height, width = original.shape[:2]
        result.update(width=width, height=height, pngBytes=png_bytes,
                      webpBytes=image_path.stat().st_size,
                      originalSha256=original_sha, webpSha256=sha256(image_path),
                      metadataSha256=sha256(metadata_path), originalPngVerified=not packaged_only)
        snapshot_path = root / "docs/art/v118/metadata-before-bounds" / metadata_path.name
        if not packaged_only and snapshot_path.is_file():
            snapshot = json.loads(snapshot_path.read_text())
            regenerated = snapshot["originalSha256"] != data.get("originalSha256")
            result["layoutRegenerated"] = regenerated
            if regenerated:
                rejected_candidates = sorted((root / "docs/art/v118/rejected").rglob(f"{stem}*.png"))
                rejected = next((path for path in rejected_candidates if sha256(path) == snapshot["originalSha256"]), None)
                check(rejected is not None,
                      "Superseded PNG does not match saved pre-repair metadata hash")
                if rejected is not None:
                    result["supersededOriginal"] = str(rejected.relative_to(root))
            elif rig_revision is None:
                root_keys = ["pixelsPerUnit", "standingHeightPixels", "anchorsMeasured", "apparelQualified"]
                check(all(snapshot.get(key) == data.get(key) for key in root_keys),
                      "Bounds repair changed fixed scale or qualification flags")
                contract_unchanged = True
                for old, new in zip(snapshot["frames"], data.get("frames", [])):
                    for key in ["poseIndex", "pose", "originX", "originY", "pixelsPerUnit", "anchorsMeasured", "apparelQualified", "anchorProvenance"]:
                        contract_unchanged &= old.get(key) == new.get(key)
                    old_anchors, new_anchors = old.get("attachments", []), new.get("attachments", [])
                    for previous, current in zip(old_anchors, new_anchors):
                        previous = dict(previous)
                        if old["poseIndex"] in (1, 5) and previous["name"] == "DRESS":
                            previous["layer"] = "FAR_LIMB"
                        contract_unchanged &= previous == current
                result["fixedRigContractPreserved"] = bool(contract_unchanged)
                check(contract_unchanged, "Bounds repair changed a protected frame origin, anchor, rotation, flag or provenance")
        if rig_revision is not None:
            record = rig_revision["records"].get(metadata_path.name)
            check(record is not None, "Rig revision receipt omits this sheet")
            if record is not None:
                before_path = rig_revision["path"].parent / "before" / metadata_path.name
                check(before_path.is_file(), "Rig revision has no archived before metadata")
                if before_path.is_file():
                    check(sha256(before_path) == record.get("before"), "Rig revision before hash differs")
                    check(result["metadataSha256"] == record.get("after"), "Metadata differs from frozen rig revision receipt")
                    check(record.get("originalSha256") == result["originalSha256"]
                          and record.get("webpSha256") == result["webpSha256"], "Rig revision raster hashes differ")
                    check(data.get("rigBaselineSha256") == record.get("before"), "Rig baseline hash does not identify archived before metadata")
                    previous = json.loads(before_path.read_text())
                    allowed_root = {"rigBaselineSha256", "rigProposal", "frames"}
                    allowed_frame = {"attachments", "garmentNeckY", "anchorProvenance"}
                    protected_before = {k: v for k, v in previous.items() if k not in allowed_root}
                    protected_after = {k: v for k, v in data.items() if k not in allowed_root}
                    protected_before["frames"] = [{k: v for k, v in f.items() if k not in allowed_frame} for f in previous["frames"]]
                    protected_after["frames"] = [{k: v for k, v in f.items() if k not in allowed_frame} for f in data["frames"]]
                    preserved = protected_before == protected_after
                    result["rigRevisionProtectedFieldsPreserved"] = preserved
                    check(preserved, "Rig revision changed geometry, fixed scale, source hashes, flags or other protected fields")
                    check(data.get("anchorsMeasured") is False and data.get("apparelQualified") is False
                          and all(f.get("anchorsMeasured") is False and f.get("apparelQualified") is False for f in data["frames"]),
                          "Provisional rig revision must keep every qualification flag false")
                    result["rigRevisionReceiptSha256"] = rig_revision["sha256"]
        equivalent = None if packaged_only else bool(np.array_equal(original, decoded))
        result["actualPixelEquivalent"] = equivalent
        if not packaged_only:
            check(equivalent, "Decoded WebP RGBA differs from original RGBA")
        expected = {"schema": 1, "id": identity, "levelMilestone": level,
                    "form": LEVELS.index(level), "image": f"art/v118/{stem}.webp",
                    "original": f"docs/art/v118/originals/{stem}.png",
                    "width": width, "height": height, "columns": 4, "rows": 5,
                    "sourceRectsMeasured": True, "pixelEquivalent": True,
                    "originalSha256": result["originalSha256"], "webpSha256": result["webpSha256"],
                    "pngBytes": result["pngBytes"], "webpBytes": result["webpBytes"]}
        for key, expected_value in expected.items():
            check(data.get(key) == expected_value, f"Metadata mismatch: {key}")
        check(isinstance(data.get("anchorsMeasured"), bool), "Missing boolean anchorsMeasured")
        check(data.get("apparelQualified") is False, "Unexpected apparel qualification; requires separate render proof")
        result["anchorsMeasured"] = data.get("anchorsMeasured", False)
        result["apparelQualified"] = data.get("apparelQualified", False)
        ppu, standing = data.get("pixelsPerUnit"), data.get("standingHeightPixels")
        check(finite(ppu) and ppu > 0, "Invalid fixed pixelsPerUnit")
        check(finite(standing) and standing > 0, "Invalid standingHeightPixels")
        if not finite(ppu) or ppu <= 0:
            ppu = 1
        if finite(standing):
            check(abs(standing / 3.15 - ppu) <= .001, "Standing height and pixelsPerUnit disagree")
        threshold = data.get("componentAlphaThreshold", 128)
        check(exact_int(threshold) and 128 <= threshold <= 252, "Invalid metadata-only component threshold")
        result["componentAlphaThreshold"] = threshold
        alpha = original[:, :, 3]
        histogram = np.bincount(alpha.ravel(), minlength=256)
        actual_alpha = {"zero": int(histogram[0]), "low1to15": int(histogram[1:16].sum()),
                        "main128to255": int(histogram[128:].sum())}
        check(data.get("alpha") == actual_alpha, "Alpha histogram does not match original")
        check(actual_alpha["zero"] > 0, "Original has no genuinely transparent pixels")
        check(actual_alpha["main128to255"] > 0, "Original has no visible body pixels")
        result["alpha"] = actual_alpha
        extras = data.get("unassignedLargeComponents", [])
        check(isinstance(extras, list), "Invalid unassignedLargeComponents")
        if extras:
            warnings.append(f"{len(extras)} unassigned large components outside the selected 20 bodies")
        frames = data.get("frames", [])
        check(isinstance(frames, list) and len(frames) == 20, "Exactly 20 frames are required")
        result["frameCount"] = len(frames) if isinstance(frames, list) else 0
        if not isinstance(frames, list):
            frames = []
        crop_hashes, body_centres, qualified_count, measured_count, attachment_count = [], [], 0, 0, 0
        clip_count = 0
        coverage = np.zeros((height, width), dtype=np.uint8)
        for index, frame in enumerate(frames):
            prefix = f"Frame {index}: "
            frame_error_start = len(errors)
            check(isinstance(frame, dict), prefix + "object required")
            if not isinstance(frame, dict):
                continue
            check(frame.get("poseIndex") == index, prefix + "poseIndex is not its fixed contract index")
            check(index < 20 and frame.get("pose") == POSES[index], prefix + "pose name contradicts fixed pose contract")
            check(finite(frame.get("pixelsPerUnit")) and abs(frame["pixelsPerUnit"] - ppu) <= .001,
                  prefix + "pose scale differs from fixed standing scale")
            check(isinstance(frame.get("anchorsMeasured"), bool), prefix + "missing boolean anchorsMeasured")
            check(frame.get("apparelQualified") is False, prefix + "unproven apparel qualification")
            measured_count += frame.get("anchorsMeasured") is True
            qualified_count += frame.get("apparelQualified") is True
            rectangles = {}
            for key in ("sourceRect", "bodyBounds"):
                rect = frame.get(key)
                valid = isinstance(rect, list) and len(rect) == 4 and all(exact_int(n) for n in rect)
                check(valid, prefix + key + " must contain four exact integers")
                if valid:
                    l, t, r, b = rect
                    valid = 0 <= l < r <= width and 0 <= t < b <= height
                    check(valid, prefix + key + " is empty or outside the actual image")
                    if valid:
                        rectangles[key] = rect
            if len(rectangles) == 2:
                l, t, r, b = rectangles["sourceRect"]
                bl, bt, br, bb = rectangles["bodyBounds"]
                check(l <= bl < br <= r and t <= bt < bb <= b, prefix + "sourceRect clips its measured bodyBounds")
                check(exact_int(frame.get("bodyArea")) and frame["bodyArea"] > 0, prefix + "invalid measured body area")
                body_threshold = frame.get("bodyAlphaThreshold", threshold)
                check(exact_int(body_threshold) and 128 <= body_threshold <= 252, prefix + "invalid body analysis threshold")
                if exact_int(body_threshold) and exact_int(frame.get("bodyArea")):
                    visible_count = int(np.count_nonzero(alpha[bt:bb, bl:br] >= body_threshold))
                    check(visible_count >= frame["bodyArea"], prefix + "measured component area exceeds real source pixels")
                body_centres.append(((bl + br) / 2, (bt + bb) / 2))
                try:
                    if "sourceClip" in frame and frame["sourceClip"] is None:
                        raise ValueError("sourceClip must be a polygon when present, not null")
                    domain = source_domain_mask(rectangles["sourceRect"], frame.get("sourceClip"))
                    clip_count += "sourceClip" in frame
                    if data.get("frameBoundsMethod") == "curved-alpha-gutters-selected-v1":
                        check(frame.get("bodyAlphaThreshold") == 128,
                              prefix + "curved-domain body measurement must use alpha128")
                        actual_body = domain & (alpha[t:b, l:r] >= 128)
                        body_y, body_x = np.nonzero(actual_body)
                        check(len(body_x) == frame.get("bodyArea"), prefix + "bodyArea differs from actual source-domain pixels")
                        check(bool(len(body_x)), prefix + "source domain contains no alpha128 body")
                        if len(body_x):
                            actual_bounds = [int(body_x.min()) + l, int(body_y.min()) + t,
                                             int(body_x.max()) + l + 1, int(body_y.max()) + t + 1]
                            check(rectangles["bodyBounds"] == actual_bounds,
                                  prefix + "bodyBounds differ from actual source-domain pixels")
                    crop = original[t:b, l:r]
                    crop_hashes.append(hashlib.sha256(f"{r-l}x{b-t}:".encode()
                                       + np.packbits(domain).tobytes() + crop[domain].tobytes()).hexdigest())
                    coverage[t:b, l:r] += domain
                except ValueError as invalid_clip:
                    check(False, prefix + str(invalid_clip))
            ox, oy = frame.get("originX"), frame.get("originY")
            check(finite(ox) and finite(oy), prefix + "invalid ground origin")
            if finite(ox) and finite(oy):
                check(-width <= ox <= width * 2 and 0 <= oy <= height + 32, prefix + "ground origin outside runtime limits")
            anchors = frame.get("attachments", [])
            check(isinstance(anchors, list) and len(anchors) == 9, prefix + "exactly nine attachments required")
            if not isinstance(anchors, list):
                anchors = []
            names = [anchor.get("name") for anchor in anchors if isinstance(anchor, dict)]
            check(len(names) == 9 and set(names) == ATTACHMENTS, prefix + "attachment names missing, duplicate or unknown")
            for anchor in anchors:
                if not isinstance(anchor, dict):
                    check(False, prefix + "attachment object required")
                    continue
                attachment_count += 1
                label = prefix + str(anchor.get("name")) + ": "
                values = [anchor.get(k) for k in ("x", "y", "width", "height", "rotation")]
                check(all(finite(v) for v in values), label + "finite geometry required")
                check(anchor.get("layer") in LAYERS, label + "invalid draw layer")
                check(isinstance(anchor.get("visible"), bool), label + "visible must be boolean")
                if all(finite(v) for v in values) and finite(ox) and finite(oy):
                    x, y, aw, ah, rotation = values
                    check(aw > 0 and ah > 0 and abs(rotation) <= 360, label + "invalid size or rotation")
                    check(abs((x - ox) / ppu) <= 4 and abs((y - oy) / ppu + 1.65) <= 4
                          and aw / ppu <= 6 and ah / ppu <= 6, label + "converted geometry exceeds runtime limits")
                if index in (1, 5) and anchor.get("name") == "DRESS":
                    check(anchor.get("layer") == "FAR_LIMB", label + "rear pose would draw frontal legacy bodice over body")
            if len(errors) == frame_error_start and frame.get("anchorsMeasured") is True:
                check(bool(frame.get("anchorProvenance")), prefix + "measured anchors need provenance")
        result.update(attachmentCount=attachment_count, anchorsMeasuredFrameCount=measured_count,
                      apparelQualifiedFrameCount=qualified_count, distinctFramePixelCrops=len(set(crop_hashes)),
                      sourceClipFrameCount=clip_count,
                      coverageDomain="sourceClip when present, otherwise sourceRect; pixel-centre half-open rasterization")
        check(len(set(crop_hashes)) == len(crop_hashes), "Two frame crops have identical pixel content")
        visible_pixels = int(np.count_nonzero(alpha >= 128))
        omitted_pixels = int(np.count_nonzero((alpha >= 128) & (coverage == 0)))
        shared_pixels = int(np.count_nonzero((alpha >= 128) & (coverage > 1)))
        omitted_fraction = omitted_pixels / visible_pixels if visible_pixels else 1
        result.update(visiblePixels128=visible_pixels, visiblePixelsOutsideAllSourceRects=omitted_pixels,
                      visiblePixelOmissionFraction=round(omitted_fraction, 8),
                      visiblePixelsSharedBySourceRects=shared_pixels,
                      visiblePixelsOutsideAllSourceDomains=omitted_pixels,
                      visiblePixelsSharedBySourceDomains=shared_pixels)
        # Alpha 128 is a detection threshold, not an edit. A small budget avoids
        # treating a few isolated generated specks as clipped anatomy. Larger
        # omissions have proved to include crowns, braids and transparent veils.
        check(omitted_fraction <= .001,
              f"Source domains omit {omitted_pixels} alpha>=128 pixels ({omitted_fraction * 100:.4f}%); maximum 0.1%")
        if omitted_pixels:
            warnings.append(f"{omitted_pixels} alpha>=128 source pixels are outside every frame crop")
        if shared_pixels:
            warnings.append(f"{shared_pixels} alpha>=128 source pixels occur in overlapping frame crops; review neighbouring-pose contamination")
        if clip_count:
            check(shared_pixels == 0, "Clipped source domains assign visible pixels to multiple poses")
        if data.get("frameBoundsMethod") in ("adaptive-alpha-density-gutters-v2", "curved-alpha-gutters-selected-v1"):
            check(omitted_pixels == 0, "Adaptive partition omits visible pixels")
            check(shared_pixels == 0, "Adaptive partition includes the same visible pixels in multiple poses")
            if not clip_count:
                rects = [frame.get("sourceRect") for frame in frames]
                for first in range(len(rects)):
                    for second in range(first + 1, len(rects)):
                        a, b = rects[first], rects[second]
                        if isinstance(a, list) and len(a) == 4 and isinstance(b, list) and len(b) == 4:
                            check(max(a[0], b[0]) >= min(a[2], b[2]) or max(a[1], b[1]) >= min(a[3], b[3]),
                                  f"Adaptive source rectangles {first}/{second} overlap geometrically")
                check(sum((r[2]-r[0])*(r[3]-r[1]) for r in rects) == width*height,
                      "Adaptive rectangles do not form a complete disjoint source partition")
        if len(body_centres) == 20:
            for row in range(5):
                check(all(body_centres[row * 4 + n][0] < body_centres[row * 4 + n + 1][0] for n in range(3)),
                      f"Row {row}: measured bodies contradict left-to-right column order")
            row_y = [sum(body_centres[row * 4 + n][1] for n in range(4)) / 4 for row in range(5)]
            check(all(row_y[n] < row_y[n + 1] for n in range(4)), "Measured bodies contradict top-to-bottom row order")
    except Exception as exc:
        errors.append(f"Audit exception {type(exc).__name__}: {exc}")
    result["passed"] = not errors
    return result


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--project", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--output", type=Path, default=Path("docs/validation/v1.18/art-integrity.json"))
    parser.add_argument("--workers", type=int, default=4)
    parser.add_argument("--asset-manifest", type=Path, default=Path("docs/art/v118/assets-manifest.json"))
    parser.add_argument("--rig-revision-receipt", type=Path,
                        help="Explicit applied rig revision receipt with archived before/ metadata; permits only recorded attachment estimates")
    parser.add_argument("--packaged-only", action="store_true",
                        help="CI: audit actual packaged WebP pixels and frozen rig receipt without original PNGs; no new PNG pixel-equivalence claim")
    args = parser.parse_args()
    if args.packaged_only and args.rig_revision_receipt is None:
        parser.error("--packaged-only requires an explicit --rig-revision-receipt and its archived before/ metadata")
    root, started = args.project.resolve(), time.monotonic()
    output = args.output if args.output.is_absolute() else root / args.output
    catalog = root / "app/src/main/java/com/androidhit/lumadrift/data/Catalog.java"
    catalog_ids = re.findall(r'new CharacterItem\("([^\"]+)"', catalog.read_text())
    errors = []
    if len(catalog_ids) != len(set(catalog_ids)):
        errors.append("Catalog contains duplicate character identities")
    if set(catalog_ids) != set(IDENTITIES):
        errors.append(f"Catalog identity mismatch: missing={sorted(set(IDENTITIES)-set(catalog_ids))}; unexpected={sorted(set(catalog_ids)-set(IDENTITIES))}")
    expected_files = {f"{identity}-evo{level:02}-poses.{suffix}" for identity in IDENTITIES
                      for level in LEVELS for suffix in ("json", "webp")}
    packaged = root / "app/src/main/assets/art/v118"
    actual_files = {path.name for path in packaged.iterdir() if path.is_file()}
    missing_files, unexpected_files = sorted(expected_files - actual_files), sorted(actual_files - expected_files)
    if missing_files:
        errors.append(f"{len(missing_files)} expected packaged files are missing")
    if unexpected_files:
        errors.append(f"Unexpected packaged files: {unexpected_files}")
    rig_revision = None
    if args.rig_revision_receipt is not None:
        path = args.rig_revision_receipt if args.rig_revision_receipt.is_absolute() else root / args.rig_revision_receipt
        try:
            document = json.loads(path.read_text())
            records = document["sheets"]
            indexed = {r["file"]: r for r in records}
            expected_metadata = {name for name in expected_files if name.endswith(".json")}
            if len(records) != len(indexed) or set(indexed) != expected_metadata:
                raise ValueError("Receipt must name all 148 metadata files exactly once")
            rig_revision = {"path": path, "sha256": sha256(path), "records": indexed}
        except Exception as invalid_revision:
            errors.append(f"Invalid explicit rig revision receipt: {invalid_revision}")
    pairs = [(identity, level) for identity in IDENTITIES for level in LEVELS]
    with concurrent.futures.ThreadPoolExecutor(max_workers=max(1, min(args.workers, 8))) as pool:
        sheets = list(pool.map(lambda pair: audit_sheet(root, *pair, rig_revision=rig_revision,
                                                       packaged_only=args.packaged_only), pairs))
    for identity in IDENTITIES:
        hashes = [s.get("originalSha256") for s in sheets if s["id"] == identity and "originalSha256" in s]
        if len(set(hashes)) != len(hashes):
            errors.append(f"{identity}: evolution forms reuse identical original files")
        webp_hashes = [s.get("webpSha256") for s in sheets if s["id"] == identity and "webpSha256" in s]
        if len(set(webp_hashes)) != len(webp_hashes):
            errors.append(f"{identity}: evolution forms reuse identical packaged files")
    failed = [sheet for sheet in sheets if not sheet["passed"]]
    summary = {"expectedCharacters": len(IDENTITIES), "catalogCharacters": len(catalog_ids),
               "expectedSheets": len(pairs), "presentSheets": sum("originalSha256" in s for s in sheets),
               "passedSheets": len(sheets) - len(failed), "failedSheets": len(failed),
               "expectedFrames": len(pairs) * 20, "auditedFrames": sum(s.get("frameCount", 0) for s in sheets),
               "expectedAttachments": len(pairs) * 20 * 9,
               "auditedAttachments": sum(s.get("attachmentCount", 0) for s in sheets),
               "actualPixelEquivalentSheets": sum(s.get("actualPixelEquivalent") is True for s in sheets),
               "verifiedOriginalPngSheets": sum(s.get("originalPngVerified") is True for s in sheets),
               "layoutRegeneratedSheets": sum(s.get("layoutRegenerated") is True for s in sheets),
               "preservedFixedRigSheets": sum(s.get("fixedRigContractPreserved") is True for s in sheets),
               "rigRevisionProtectedSheets": sum(s.get("rigRevisionProtectedFieldsPreserved") is True for s in sheets),
               "anchorsMeasuredFrames": sum(s.get("anchorsMeasuredFrameCount", 0) for s in sheets),
               "apparelQualifiedFrames": sum(s.get("apparelQualifiedFrameCount", 0) for s in sheets),
               "sourceClipFrames": sum(s.get("sourceClipFrameCount", 0) for s in sheets),
               "originalPngBytes": sum(s.get("pngBytes", 0) for s in sheets),
               "packagedWebpBytes": sum(s.get("webpBytes", 0) for s in sheets),
               "visiblePixelsOutsideAllSourceRects": sum(s.get("visiblePixelsOutsideAllSourceRects", 0) for s in sheets),
               "visiblePixelsSharedBySourceRects": sum(s.get("visiblePixelsSharedBySourceRects", 0) for s in sheets),
               "visiblePixelsOutsideAllSourceDomains": sum(s.get("visiblePixelsOutsideAllSourceDomains", 0) for s in sheets),
               "visiblePixelsSharedBySourceDomains": sum(s.get("visiblePixelsSharedBySourceDomains", 0) for s in sheets),
               "componentThresholdCounts": dict(sorted(Counter(str(s.get("componentAlphaThreshold")) for s in sheets
                                                                 if "componentAlphaThreshold" in s).items())),
               "warningSheets": sum(bool(s["warnings"]) for s in sheets)}
    passed = not errors and not failed
    packaged_records = [{"path": str((packaged / name).relative_to(root)), "sha256": sha256(packaged / name),
                         "bytes": (packaged / name).stat().st_size} for name in sorted(expected_files)
                        if (packaged / name).is_file()]
    tree_hash = hashlib.sha256("".join(f"{item['sha256']}  {item['path']}\n" for item in packaged_records).encode()).hexdigest()
    manifest_path = args.asset_manifest if args.asset_manifest.is_absolute() else root / args.asset_manifest
    manifest = {"schema": 1, "release": "v1.18", "characters": IDENTITIES, "levels": LEVELS,
                "sheetCount": len(pairs), "poseCount": len(pairs) * 20,
                "packagedFileCount": len(packaged_records), "assetTreeSha256": tree_hash, "files": packaged_records}
    manifest_sha = None
    if passed:
        manifest_path.parent.mkdir(parents=True, exist_ok=True)
        manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n")
        manifest_sha = sha256(manifest_path)
    report = {"schema": 1, "audit": "v1.18-companion-art-integrity", "passed": passed,
              "mode": "packaged-only" if args.packaged_only else "originals-and-packaged",
              "generatedAtUtc": dt.datetime.now(dt.timezone.utc).isoformat(),
              "durationSeconds": round(time.monotonic() - started, 3),
              "scriptSha256": sha256(Path(__file__)), "expectedIdentities": IDENTITIES,
              "explicitRigRevisionReceipt": str(rig_revision["path"]) if rig_revision else None,
              "explicitRigRevisionReceiptSha256": rig_revision["sha256"] if rig_revision else None,
              "assetTreeSha256": tree_hash, "assetManifestPath": str(manifest_path), "assetManifestSha256": manifest_sha,
              "levelMilestones": LEVELS, "fixedPoseOrder": POSES,
              "scope": ["Strict catalog and packaged filenames",
                        "All packaged WebP files decoded; original PNGs deliberately not required" if args.packaged_only else "All original PNG and packaged WebP files decoded",
                        "Actual packaged byte hashes, sizes, dimensions and alpha; recorded PNG hashes/sizes bound to revision baseline" if args.packaged_only else "Byte hashes, byte sizes, dimensions and actual RGBA pixel equality",
                        "All source rectangles, body bounds, pose indices, ground origins and fixed scale",
                        "Optional sourceClip polygons: 3..256 finite vertices inside sourceRect, simple and nonzero area",
                        "At least 99.9% of alpha>=128 pixels contained by the union of source domains at pixel centres",
                        "No alpha>=128 pixel assigned to multiple poses when sourceClip is present; enclosing rectangles may overlap",
                        "All nine attachment names, geometry, runtime bounds and rear bodice occlusion",
                        "Explicit rig revisions, if supplied: exact applied/before hashes, unchanged raster/extraction/scale/flags, only attachment estimates changed",
                        "Distinct pixel crops and distinct evolution original files"],
              "limits": (["Packaged-only mode does not open original PNGs or repeat the historical PNG/WebP pixel-equivalence proof."] if args.packaged_only else []) + ["Pixel differences do not prove correct anatomy, facing, emotion or meaningful evolution.",
                         "Union pixel coverage detects omitted parts but does not prove that every pixel belongs to the correct frame.",
                         "No automatic check qualifies clothing fit or eliminates low-alpha generated fringes.",
                         "The only manually measured anchors are recorded in the individual manifests; all other anchors remain estimates.",
                         "Visual and native renderer review are separate evidence, not implied by this audit."],
              "summary": summary, "errors": errors, "missingPackagedFiles": missing_files,
              "unexpectedPackagedFiles": unexpected_files, "sheets": sheets}
    output.parent.mkdir(parents=True, exist_ok=True)
    temporary = output.with_suffix(output.suffix + ".tmp")
    temporary.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n")
    temporary.replace(output)
    print(json.dumps({"passed": passed, "report": str(output), "summary": summary,
                      "assetTreeSha256": tree_hash, "assetManifestSha256": manifest_sha,
                      "errors": errors, "failedSheetErrors": [{"id": s["id"], "level": s["levelMilestone"],
                                                                 "errors": s["errors"]} for s in failed]}, ensure_ascii=False))
    return 0 if passed else 1


if __name__ == "__main__":
    sys.exit(main())
