#!/usr/bin/env python3
"""Partition twenty-pose atlases at measured alpha-density gutters.

Only JSON frame bounds are changed. Pixels, original files, origins, scale and
attachments are preserved. The dry-run report includes seam/core diagnostics;
full union coverage alone is not treated as proof of correct pose separation.
"""
from __future__ import annotations

import argparse
import copy
import hashlib
import json
import shutil
import statistics
from pathlib import Path

import numpy as np
from PIL import Image

REVIEW = [("jelli", 3), ("jelli", 6), ("lune", 6), ("lol_jinx", 6),
          ("orbit", 10), ("vela", 10), ("emberling", 6), ("orbit", 6),
          ("lol_vi", 6), ("pip", 10), ("lol_annie", 6), ("vela", 3)]


def valley(density: np.ndarray, centres: tuple[float, float], preferred: float,
           body_sizes: tuple[float, float]) -> tuple[int, dict]:
    """Find a separating line between core centres, favouring actual empty air."""
    first, second = centres
    minimum = max(1, int(np.ceil(first + body_sizes[0] * .17)))
    maximum = min(len(density) - 1, int(np.floor(second - body_sizes[1] * .17)))
    if minimum > maximum:
        minimum, maximum = int(np.ceil(first)), int(np.floor(second))
    if minimum > maximum:
        raise ValueError("No corridor between distinct body centres")
    candidates = np.arange(minimum, maximum + 1)
    # A cut is between pixels cut-1/cut. Three-pixel support avoids choosing
    # a one-pixel hole through a detailed ornament instead of a real gutter.
    padded = np.pad(density, (2, 2), mode="edge")
    support = np.convolve(padded, np.array([.25, .5, .5, .5, .25]), mode="valid")
    scores = (support[candidates - 1] + support[candidates]) * .5
    best_mass = float(scores.min())
    tolerance = max(.5, best_mass * .025)
    near_best = candidates[scores <= best_mass + tolerance]
    cut = int(min(near_best, key=lambda value: (abs(float(value) - preferred), int(value))))
    return cut, {"cut": cut, "search": [minimum, maximum], "preferred": round(preferred, 4),
                 "weightedMass": round(float(density[cut - 1] + density[cut]) / 2, 4),
                 "smoothedMinimumMass": round(best_mass, 4)}


def immutable_contract(manifest: dict) -> str:
    """Digest the parts that bounds repair is forbidden to change."""
    contract = {"image": manifest["image"], "originalSha256": manifest["originalSha256"],
                "webpSha256": manifest["webpSha256"], "pixelsPerUnit": manifest["pixelsPerUnit"],
                "standingHeightPixels": manifest["standingHeightPixels"],
                "anchorsMeasured": manifest["anchorsMeasured"], "apparelQualified": manifest["apparelQualified"],
                "frames": [{k: frame.get(k) for k in ["poseIndex", "pose", "originX", "originY", "pixelsPerUnit",
                                                    "attachments", "anchorsMeasured", "apparelQualified", "anchorProvenance"]}
                           for frame in manifest["frames"]]}
    return hashlib.sha256(json.dumps(contract, sort_keys=True, ensure_ascii=False).encode()).hexdigest()


def component_runs(mask: np.ndarray) -> dict:
    """Four-connected run-length components, preserving actual pixel ownership.

    Bounding boxes can overlap even when their wings/hair never touch. Run
    ownership distinguishes a real clipped body from another sprite merely
    lying in a corner of that body's rectangular bounding box.
    """
    parents, runs = [], []

    def find(index):
        while parents[index] != index:
            parents[index] = parents[parents[index]]
            index = parents[index]
        return index

    previous = []
    for y in range(mask.shape[0]):
        edges = np.flatnonzero(np.diff(np.pad(mask[y].astype(np.int8), (1, 1))))
        current, previous_index = [], 0
        for left, right in edges.reshape(-1, 2):
            left, right = int(left), int(right)
            index = len(parents)
            parents.append(index)
            runs.append((y, left, right))
            while previous_index < len(previous) and previous[previous_index][1] <= left:
                previous_index += 1
            candidate = previous_index
            while candidate < len(previous) and previous[candidate][0] < right:
                a, b = find(index), find(previous[candidate][2])
                if a != b:
                    parents[a] = b
                candidate += 1
            current.append((left, right, index))
        previous = current
    components = {}
    for index, (y, left, right) in enumerate(runs):
        key = find(index)
        component = components.setdefault(key, {"area": 0, "bounds": [mask.shape[1], mask.shape[0], 0, 0], "runs": []})
        component["area"] += right - left
        bounds = component["bounds"]
        bounds[0], bounds[1], bounds[2], bounds[3] = min(bounds[0], left), min(bounds[1], y), max(bounds[2], right), max(bounds[3], y + 1)
        component["runs"].append((y, left, right))
    return {tuple(component["bounds"]): component for component in components.values() if component["area"] >= 600}


def propose_bounds(manifest: dict, alpha: np.ndarray) -> tuple[dict, dict]:
    height, width = alpha.shape
    if manifest["width"] != width or manifest["height"] != height or len(manifest["frames"]) != 20:
        raise ValueError("Actual source dimensions or twenty-frame contract mismatch")
    original = copy.deepcopy(manifest)
    frames = original["frames"]
    # Repeated runs always use the initial connected-component measurements,
    # never progressively inflated partitions from a preceding repair.
    bodies = [frame.get("initialBodyBounds", frame["bodyBounds"]) for frame in frames]
    centre_x = [(b[0] + b[2]) / 2 for b in bodies]
    centre_y = [(b[1] + b[3]) / 2 for b in bodies]
    strong = alpha >= 128
    weight = strong.astype(np.float64) + (alpha >= 248).astype(np.float64) * 3
    row_cuts, row_diagnostics = [0], []
    for row in range(4):
        upper, lower = bodies[row * 4:row * 4 + 4], bodies[(row + 1) * 4:(row + 1) * 4 + 4]
        upper_centre = max(centre_y[row * 4:row * 4 + 4])
        lower_centre = min(centre_y[(row + 1) * 4:(row + 1) * 4 + 4])
        preferred = (statistics.median(b[3] for b in upper) + statistics.median(b[1] for b in lower)) / 2
        sizes = (statistics.median(b[3] - b[1] for b in upper), statistics.median(b[3] - b[1] for b in lower))
        cut, diagnostic = valley(weight.sum(axis=1), (upper_centre, lower_centre), preferred, sizes)
        diagnostic["alpha128PixelsOnSeam"] = int(strong[cut - 1:cut + 1].sum())
        row_cuts.append(cut)
        row_diagnostics.append(diagnostic)
    row_cuts.append(height)
    if any(a >= b for a, b in zip(row_cuts, row_cuts[1:])):
        raise ValueError("Non-monotonic measured row gutters")
    column_cuts, column_diagnostics = [], []
    for row in range(5):
        top, bottom = row_cuts[row:row + 2]
        density = weight[top:bottom].sum(axis=0)
        cuts, diagnostics = [0], []
        for column in range(3):
            left, right = bodies[row * 4 + column], bodies[row * 4 + column + 1]
            preferred = (left[2] + right[0]) / 2
            sizes = (left[2] - left[0], right[2] - right[0])
            cut, diagnostic = valley(density, (centre_x[row * 4 + column], centre_x[row * 4 + column + 1]), preferred, sizes)
            diagnostic["alpha128PixelsOnSeam"] = int(strong[top:bottom, cut - 1:cut + 1].sum())
            cuts.append(cut)
            diagnostics.append(diagnostic)
        cuts.append(width)
        if any(a >= b for a, b in zip(cuts, cuts[1:])):
            raise ValueError("Non-monotonic measured column gutters")
        column_cuts.append(cuts)
        column_diagnostics.append(diagnostics)
    row_first_rects = [[column_cuts[row][column], row_cuts[row], column_cuts[row][column + 1], row_cuts[row + 1]]
                       for row in range(5) for column in range(4)]
    row_first_score = sum(d["weightedMass"] for d in row_diagnostics) + sum(d["weightedMass"] for row in column_diagnostics for d in row)
    # A shared horizontal cut can include the next row's taller crown under a
    # short crouch. Evaluate the transposed partition too: global column
    # gutters, then independent row gutters inside each column. Neither is an
    # equal grid. Pick the topology crossing the least actual alpha mass.
    global_columns, global_column_diagnostics = [0], []
    for column in range(3):
        lefts = [bodies[row * 4 + column] for row in range(5)]
        rights = [bodies[row * 4 + column + 1] for row in range(5)]
        centres = (max((b[0] + b[2]) / 2 for b in lefts), min((b[0] + b[2]) / 2 for b in rights))
        preferred = (statistics.median(b[2] for b in lefts) + statistics.median(b[0] for b in rights)) / 2
        sizes = (statistics.median(b[2] - b[0] for b in lefts), statistics.median(b[2] - b[0] for b in rights))
        cut, diagnostic = valley(weight.sum(axis=0), centres, preferred, sizes)
        diagnostic["alpha128PixelsOnSeam"] = int(strong[:, cut - 1:cut + 1].sum())
        global_columns.append(cut)
        global_column_diagnostics.append(diagnostic)
    global_columns.append(width)
    rows_by_column, rows_by_column_diagnostics = [], []
    for column in range(4):
        left, right = global_columns[column:column + 2]
        cuts, diagnostics = [0], []
        density = weight[:, left:right].sum(axis=1)
        for row in range(4):
            upper, lower = bodies[row * 4 + column], bodies[(row + 1) * 4 + column]
            centres = ((upper[1] + upper[3]) / 2, (lower[1] + lower[3]) / 2)
            preferred = (upper[3] + lower[1]) / 2
            sizes = (upper[3] - upper[1], lower[3] - lower[1])
            cut, diagnostic = valley(density, centres, preferred, sizes)
            diagnostic["alpha128PixelsOnSeam"] = int(strong[cut - 1:cut + 1, left:right].sum())
            cuts.append(cut)
            diagnostics.append(diagnostic)
        cuts.append(height)
        rows_by_column.append(cuts)
        rows_by_column_diagnostics.append(diagnostics)
    column_first_rects = [[global_columns[column], rows_by_column[column][row], global_columns[column + 1], rows_by_column[column][row + 1]]
                          for row in range(5) for column in range(4)]
    column_first_score = sum(d["weightedMass"] for d in global_column_diagnostics) + sum(d["weightedMass"] for col in rows_by_column_diagnostics for d in col)
    if column_first_score < row_first_score:
        rects = column_first_rects
        partition = {"orientation": "columns-first", "columns": global_columns, "rowsByColumn": rows_by_column}
    else:
        rects = row_first_rects
        partition = {"orientation": "rows-first", "rows": row_cuts, "columnsByRow": column_cuts}
    partition.update(analysisAlphaThreshold=128, weightAlpha248=3,
                     candidateWeightedSeamMass={"rowsFirst": round(row_first_score, 4), "columnsFirst": round(column_first_score, 4)})
    repaired = copy.deepcopy(original)
    diagnostics = []
    initial_threshold = original.get("componentAlphaThreshold", 128)
    owned_components = component_runs(alpha >= initial_threshold)
    for index, frame in enumerate(repaired["frames"]):
        rect = rects[index]
        left, top, right, bottom = rect
        ys, xs = np.nonzero(strong[top:bottom, left:right])
        if not len(xs):
            raise ValueError(f"Pose {index} assigned corridor has no alpha>=128 pixels")
        body = [left + int(xs.min()), top + int(ys.min()), left + int(xs.max()) + 1, top + int(ys.max()) + 1]
        initial = bodies[index]
        il, it, ir, ib = initial
        threshold = initial_threshold
        initial_strong = alpha[it:ib, il:ir] >= threshold
        preserved = np.zeros(initial_strong.shape, dtype=bool)
        pl, pt, pr, pb = max(il, left), max(it, top), min(ir, right), min(ib, bottom)
        if pl < pr and pt < pb:
            preserved[pt-it:pb-it, pl-il:pr-il] = True
        outside_pixels = int(np.count_nonzero(initial_strong & ~preserved))
        owned = owned_components.get(tuple(initial))
        actual_lost = None
        if owned is not None:
            actual_lost = 0
            for y, run_left, run_right in owned["runs"]:
                kept = max(0, min(run_right, right) - max(run_left, left)) if top <= y < bottom else 0
                actual_lost += run_right - run_left - kept
        # Bounding-box intersections may contain neighbouring art; this is a
        # conservative warning for visual review, not a statement of anatomy.
        diagnostic = {"poseIndex": index, "oldSourceRect": frame["sourceRect"], "sourceRect": rect,
                      "initialBodyBounds": initial, "initialThresholdPixelsOutsideAssignedCorridor": outside_pixels,
                      "initialBodyArea": frame.get("initialBodyArea", frame.get("bodyArea")),
                      "matchedInitialConnectedComponent": owned is not None,
                      "initialConnectedComponentArea": owned["area"] if owned is not None else None,
                      "initialConnectedComponentPixelsOutsideAssignedCorridor": actual_lost,
                      "alpha128Area": int(len(xs))}
        frame.setdefault("initialSourceRect", frame["sourceRect"])
        frame.setdefault("initialBodyBounds", frame["bodyBounds"])
        frame.setdefault("initialBodyArea", frame["bodyArea"])
        frame["sourceRect"] = rect
        frame["bodyBounds"] = body
        frame["bodyArea"] = int(len(xs))
        frame["bodyAlphaThreshold"] = 128
        frame["sourceRectProvenance"] = "Nonoverlapping adaptive alpha-density gutter partition, rows-first or columns-first; original pixels unchanged"
        diagnostics.append(diagnostic)
    repaired["frameBoundsMethod"] = "adaptive-alpha-density-gutters-v2"
    repaired["frameBoundsPartition"] = partition
    repaired["frameBoundsDiagnostics"] = {
        "matchedInitialConnectedComponents": sum(d["matchedInitialConnectedComponent"] for d in diagnostics),
        "initialConnectedComponentPixelsOutsideAssignedCorridors": sum(d["initialConnectedComponentPixelsOutsideAssignedCorridor"] or 0 for d in diagnostics),
        "note": "Connected components can include neighbouring crowns through generated fringe. Visual review remains required."}
    if immutable_contract(original) != immutable_contract(repaired):
        raise ValueError("Forbidden change to origins, scale, anchors or source identity")
    return repaired, {"id": manifest["id"], "levelMilestone": manifest["levelMilestone"],
                      "selectedPartition": partition,
                      "rows": row_diagnostics, "columnsByRow": column_diagnostics,
                      "globalColumns": global_column_diagnostics, "rowsByColumn": rows_by_column_diagnostics,
                      "frames": diagnostics, "immutableContractSha256": immutable_contract(original),
                      "initialThresholdPixelsOutsideAssignedCorridors": sum(d["initialThresholdPixelsOutsideAssignedCorridor"] for d in diagnostics),
                      "matchedInitialConnectedComponents": sum(d["matchedInitialConnectedComponent"] for d in diagnostics),
                      "initialConnectedComponentPixelsOutsideAssignedCorridors": sum(d["initialConnectedComponentPixelsOutsideAssignedCorridor"] or 0 for d in diagnostics)}


def review_html(root: Path, reports: list[dict], output: Path):
    selected = [report for report in reports if (report["id"], report["levelMilestone"]) in REVIEW]
    # Only display the same unmodified original through two Canvas sourceRect
    # windows. This is a browser review, not the creation of edited raster art.
    records = []
    for item in selected:
        stem = f"{item['id']}-evo{item['levelMilestone']:02}-poses"
        current = json.loads((root / "app/src/main/assets/art/v118" / f"{stem}.json").read_text())
        snapshot_path = root / "docs/art/v118/metadata-before-bounds" / f"{stem}.json"
        snapshot = json.loads(snapshot_path.read_text()) if snapshot_path.exists() else current
        regenerated = snapshot["originalSha256"] != current["originalSha256"]
        if regenerated:
            rejected_candidates = sorted((root / "docs/art/v118/rejected").rglob(f"{stem}*.png"))
            rejected = next((path for path in rejected_candidates if hashlib.sha256(path.read_bytes()).hexdigest() == snapshot["originalSha256"]), None)
            if rejected is None:
                raise ValueError(f"No preserved pre-repair original found for {stem}")
            before_image = "/" + str(rejected.relative_to(root))
        else:
            before_image = f"/docs/art/v118/originals/{stem}.png"
        frame_records = copy.deepcopy(item["frames"])
        for index, frame in enumerate(frame_records):
            frame["oldSourceRect"] = snapshot["frames"][index]["sourceRect"]
            frame["sourceRect"] = current["frames"][index]["sourceRect"]
            if regenerated and current.get("frameBoundsDiagnostics", {}).get("initialConnectedComponentPixelsOutsideAssignedCorridors") == 0:
                frame["initialThresholdPixelsOutsideAssignedCorridor"] = 0
                frame["initialConnectedComponentPixelsOutsideAssignedCorridor"] = 0
        records.append({"id": item["id"], "level": item["levelMilestone"], "layoutRegenerated": regenerated,
                        "beforeImage": before_image, "image": f"/docs/art/v118/originals/{stem}.png", "frames": frame_records})
    template = """<!doctype html><html lang="fr"><meta charset="utf-8"><title>Luma — contrôle des cadrages</title>
<style>body{background:#eaf1ed;color:#203338;font:15px system-ui;margin:20px}h1{font-size:24px}h2{margin-top:35px}
.grid{display:grid;grid-template-columns:repeat(4,minmax(210px,1fr));gap:12px}.card{background:#fff9eb;border-radius:12px;padding:8px}
.pair{display:flex;gap:4px}canvas{background:#f4f5f0;max-width:49%;height:auto;border:1px solid #d5ddda}small{display:block;color:#53676b}
.compact{margin:10px}.compact h1{font-size:18px;margin:0}.compact p{font-size:11px;margin:4px 0}.compact h2{font-size:15px;margin:10px 0}
.compact .grid{grid-template-columns:repeat(5,minmax(0,1fr));gap:6px}.compact .card{padding:4px}.compact b{font-size:11px}
.compact small{font-size:7px;line-height:8px;min-height:16px}.compact .pair{justify-content:center}.compact canvas{width:80px;height:100px}
</style><h1>Contrôle des vingt poses : avant / après</h1><p>À gauche : ancien cadrage. À droite : gouttières mesurées. Les reprises de mise en page sont signalées ; aucun effacement manuel de pixels. Les coutures restent à contrôler visuellement.</p><main></main>
<script>
const records=RECORDS;
const main=document.querySelector('main'),query=new URLSearchParams(location.search);
if(query.get('compact')==='1')document.body.className='compact';
for(const record of records){
 if(query.get('sheet')&&query.get('sheet')!==`${record.id}-${record.level}`)continue;
 const section=document.createElement('section');section.id=`sheet-${record.id}-${record.level}`;
 section.innerHTML=`<h2>${record.id} · niveau ${record.level}${record.layoutRegenerated?' · mise en page régénérée':''}</h2><div class="grid"></div>`;
 main.append(section);const grid=section.querySelector('.grid');
 const images=[new Image(),new Image()];let ready=0;
 for(let variant=0;variant<2;variant++){
  images[variant].onload=()=>{if(++ready!==2)return;
   for(const frame of record.frames){
    const card=document.createElement('div');card.className='card';
    card.innerHTML=`<b>Pose ${frame.poseIndex+1} /20</b><small>Ancien / après · boîte initiale hors corridor : ${frame.initialThresholdPixelsOutsideAssignedCorridor} px · composante connexe : ${frame.initialConnectedComponentPixelsOutsideAssignedCorridor} px</small><div class="pair"></div>`;
    grid.append(card);
    for(let side=0;side<2;side++){
     const rect=side===0?frame.oldSourceRect:frame.sourceRect,canvas=document.createElement('canvas');
     canvas.width=240;canvas.height=300;const c=canvas.getContext('2d'),[l,t,r,b]=rect;
     const scale=Math.min(224/(r-l),284/(b-t));
     c.drawImage(images[side],l,t,r-l,b-t,(240-(r-l)*scale)/2,(300-(b-t)*scale)/2,(r-l)*scale,(b-t)*scale);
     card.querySelector('.pair').append(canvas);
    }
   }
  };
  images[variant].src=variant===0?record.beforeImage:record.image;
 }
}
</script></html>"""
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(template.replace("RECORDS", json.dumps(records, ensure_ascii=False)))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--project", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--apply", action="store_true")
    parser.add_argument("--report", type=Path, default=Path("docs/validation/v1.18/frame-bounds-repair.json"))
    parser.add_argument("--review", type=Path, default=Path("docs/validation/v1.18/frame-bounds-review.html"))
    args = parser.parse_args()
    root = args.project.resolve()
    paths = sorted((root / "app/src/main/assets/art/v118").glob("*.json"))
    if args.apply and len(paths) != 148:
        raise ValueError(f"Refusing whole-set mutation before all 148 sheets exist; found {len(paths)}")
    proposals, reports = [], []
    for path in paths:
        manifest = json.loads(path.read_text())
        with Image.open(root / manifest["original"]) as image:
            alpha = np.asarray(image.convert("RGBA"))[:, :, 3]
        repaired, report = propose_bounds(manifest, alpha)
        for index in (1, 5):
            for anchor in repaired["frames"][index]["attachments"]:
                if anchor["name"] == "DRESS":
                    anchor["layer"] = "FAR_LIMB"
        # Rear-layer repair is intentional and separately enumerated. No
        # source coordinates, sizes, rotations or other attachments change.
        report["rearDressLayerRepaired"] = any(a["name"] == "DRESS" and a["layer"] != "FAR_LIMB"
                                                   for index in (1, 5) for a in manifest["frames"][index]["attachments"])
        proposals.append((path, repaired))
        reports.append(report)
    if args.apply:
        snapshots = root / "docs/art/v118/metadata-before-bounds"
        snapshots.mkdir(parents=True, exist_ok=True)
        for path, _ in proposals:
            destination = snapshots / path.name
            if not destination.exists():
                shutil.copy2(path, destination)
        for path, repaired in proposals:
            temporary = path.with_suffix(".json.tmp")
            temporary.write_text(json.dumps(repaired, ensure_ascii=False, indent=2) + "\n")
            temporary.replace(path)
    report_path = args.report if args.report.is_absolute() else root / args.report
    review_path = args.review if args.review.is_absolute() else root / args.review
    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(json.dumps({"schema": 1, "applied": args.apply, "sheetCount": len(reports),
                                      "method": "adaptive-alpha-density-gutters-v2", "pixelsModified": False,
                                      "qualifiedApparel": False, "reports": reports}, ensure_ascii=False, indent=2) + "\n")
    review_html(root, reports, review_path)
    worst = sorted(reports, key=lambda r: r["initialThresholdPixelsOutsideAssignedCorridors"], reverse=True)[:15]
    actual_worst = sorted(reports, key=lambda r: r["initialConnectedComponentPixelsOutsideAssignedCorridors"], reverse=True)[:15]
    print(json.dumps({"applied": args.apply, "sheets": len(reports), "report": str(report_path),
                      "review": str(review_path), "largestConservativeCoreWarnings": [{"id": r["id"], "level": r["levelMilestone"],
                         "pixels": r["initialThresholdPixelsOutsideAssignedCorridors"]} for r in worst],
                      "actualConnectedComponentCuts": [{"id": r["id"], "level": r["levelMilestone"],
                         "pixels": r["initialConnectedComponentPixelsOutsideAssignedCorridors"],
                         "matched": r["matchedInitialConnectedComponents"]} for r in actual_worst]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
