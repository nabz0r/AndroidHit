#!/usr/bin/env python3
"""Read-only atlas extraction experiment using curved transparent gutters.

Writes proposed geometry and review HTML in a separate directory. Never writes
the selected original PNG, runtime WebP, or runtime JSON. The proposed sourceClip
is an atlas-pixel polygon: extraction geometry, not a pixel cleanup operation.
"""
from __future__ import annotations

import argparse
import hashlib
import html
import importlib.util
import json
from pathlib import Path

import numpy as np
from PIL import Image

SPEC = importlib.util.spec_from_file_location("rect_bounds", Path(__file__).with_name("repair-frame-bounds.py"))
BOUNDS = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(BOUNDS)

DEFAULT = ["miko-evo03", "miko-evo10", "fenn-evo10", "lune-evo03", "buzz-evo10", "jelli-evo10",
           "lol_yasuo-evo03", "lol_yasuo-evo10", "lol_lulu-evo06", "lol_lulu-evo10", "lol_sona-evo03",
           "lol_sona-evo06", "lol_miss_fortune-evo01", "lol_miss_fortune-evo03", "lol_miss_fortune-evo06",
           "lol_miss_fortune-evo10", "lol_vi-evo03", "lol_vi-evo06", "lol_vi-evo10", "lol_annie-evo01",
           "lol_annie-evo03", "lol_annie-evo10", "lol_ashe-evo06"]


def sha(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def core_labels(manifest: dict, alpha: np.ndarray) -> tuple[np.ndarray, list]:
    components = BOUNDS.component_runs(alpha >= manifest.get("componentAlphaThreshold", 128))
    labels = np.full(alpha.shape, -1, dtype=np.int16)
    records = []
    for frame in manifest["frames"]:
        body = frame.get("initialBodyBounds", frame["bodyBounds"])
        component = components.get(tuple(body))
        records.append({"poseIndex": frame["poseIndex"], "matched": component is not None,
                        "area": component["area"] if component else None})
        if component:
            for y, left, right in component["runs"]:
                labels[y, left:right] = frame["poseIndex"]
    return labels, records


def trusted_centres(manifest: dict, cores: np.ndarray) -> np.ndarray:
    """Only stable inner body pixels may impose a hard semantic constraint.

    A connected component can include a neighbour's golden crescent through an
    opaque generated fringe. Protecting its complete boundary locks that false
    ownership into the seam. The central half-width/half-height body region
    anchors identity; alpha energy routes around hands, ears and ornaments.
    """
    trusted = np.full(cores.shape, -1, dtype=np.int16)
    yy, xx = np.indices(cores.shape)
    for frame in manifest["frames"]:
        l, t, r, b = frame.get("initialBodyBounds", frame["bodyBounds"])
        cx, cy = (l + r) / 2, (t + b) / 2
        inner = ((xx - cx) / ((r - l) * .25)) ** 2 + ((yy - cy) / ((b - t) * .25)) ** 2 <= 1
        selected = inner & (cores == frame["poseIndex"])
        trusted[selected] = frame["poseIndex"]
    return trusted


def minimum_seam(alpha: np.ndarray, cores: np.ndarray, group: np.ndarray, split: int,
                 preferred: float, centres: tuple[float, float], max_step: int = 8,
                 baseline: np.ndarray | None = None) -> tuple[np.ndarray, dict]:
    """Find a horizontal cut y(x); inputs may be transposed for a vertical cut.

    High-confidence connected components impose ownership constraints at every
    x. If these constraints have no feasible monotone seam, return an explicitly
    unqualified soft-cost candidate and report the contradiction.
    """
    height, width = alpha.shape
    low = max(1, int(min(centres) - (centres[1] - centres[0]) * .15))
    high = min(height - 1, int(max(centres) + (centres[1] - centres[0]) * .15))
    if low >= high:
        raise ValueError("No geometric corridor between pose rows/columns")
    positions = np.arange(low, high + 1)
    # Cost is the weaker adjacent alpha: an opaque edge next to empty gutter is
    # safe, whereas a cut through the interior of a body is expensive.
    opacity = alpha.astype(np.float64) / 255.0
    adjacent = np.minimum(opacity[positions - 1], opacity[positions])
    # Squared alpha also protects translucent antennas/hair that a very high
    # threshold omits from the dominant component. A high exponent falsely
    # treated these authored edges as cheap fringe in the first experiment.
    # Prefer any genuinely translucent/empty gutter over a shortcut through a
    # bright solid crescent. Summed alpha alone can prefer cutting two opaque
    # ornament edges rather than following a longer antialiased boot contour.
    cost = (adjacent >= (128 / 255.0)) * 1_000_000 + adjacent ** 2 * 1000 + .01
    preference = preferred if baseline is None else baseline[None, :]
    cost += np.abs(positions[:, None] - preference) * .05
    if baseline is not None:
        cost[np.abs(positions[:, None] - baseline[None, :]) > 12] = np.inf
    lower = np.full(width, low, dtype=np.int32)
    upper = np.full(width, high, dtype=np.int32)
    ys, xs = np.nonzero(cores >= 0)
    membership = group[cores[ys, xs]]
    above = membership <= split
    np.maximum.at(lower, xs[above], ys[above] + 1)
    np.minimum.at(upper, xs[~above], ys[~above])
    constrained = (positions[:, None] >= lower) & (positions[:, None] <= upper)
    impossible_columns = int(np.count_nonzero(lower > upper))

    def solve(hard: bool):
        score = cost[:, 0].copy()
        if hard:
            score[~constrained[:, 0]] = np.inf
        back = np.zeros((width, len(positions)), dtype=np.int8)
        shifts = list(range(-max_step, max_step + 1))
        for x in range(1, width):
            candidates = np.full((len(shifts), len(positions)), np.inf, dtype=np.float64)
            for k, shift in enumerate(shifts):
                if shift < 0:
                    candidates[k, :shift] = score[-shift:] + abs(shift) * .025
                elif shift > 0:
                    candidates[k, shift:] = score[:-shift] + shift * .025
                else:
                    candidates[k] = score
            best = np.argmin(candidates, axis=0)
            score = candidates[best, np.arange(len(positions))] + cost[:, x]
            if hard:
                score[~constrained[:, x]] = np.inf
            back[x] = np.asarray(shifts, dtype=np.int8)[best]
        if not np.isfinite(score).any():
            return None
        selected = int(np.argmin(score))
        result = np.empty(width, dtype=np.int32)
        result[-1] = positions[selected]
        for x in range(width - 1, 0, -1):
            selected -= int(back[x, selected])
            result[x - 1] = positions[selected]
        return result

    seam = solve(True) if impossible_columns == 0 else None
    hard_feasible = seam is not None
    if seam is None:
        seam = solve(False)
    crossings = 0
    for x, y in enumerate(seam):
        crossings += int(alpha[y - 1, x] >= 248 and alpha[y, x] >= 248)
    return seam, {"hardCoreConstraintsFeasible": hard_feasible, "contradictoryColumns": impossible_columns,
                  "alpha248InteriorCrossings": crossings, "min": int(seam.min()), "max": int(seam.max()),
                  "maxStep": int(np.abs(np.diff(seam)).max(initial=0)),
                  "maximumDeviationFromRecordedCrop": float(np.max(np.abs(seam - baseline))) if baseline is not None else None}


def recorded_gutter(manifest: dict, axis: str, split: int, length: int) -> np.ndarray:
    partition = manifest["frameBoundsPartition"]
    result = np.zeros(length, dtype=np.float64)
    if axis == "horizontal":
        if partition["orientation"] == "rows-first":
            result[:] = partition["rows"][split + 1]
        else:
            for c in range(4):
                result[partition["columns"][c]:partition["columns"][c + 1]] = partition["rowsByColumn"][c][split + 1]
    else:
        if partition["orientation"] == "columns-first":
            result[:] = partition["columns"][split + 1]
        else:
            for r in range(5):
                result[partition["rows"][r]:partition["rows"][r + 1]] = partition["columnsByRow"][r][split + 1]
    # Smooth only the grid junctions in the blank crossing gutters. The body
    # fields keep their recorded E cut as a bounded local extraction reference.
    return np.convolve(np.pad(result, (12, 12), mode="edge"), np.ones(25) / 25, mode="valid")


def polygons(mask: np.ndarray) -> list[list[list[int]]]:
    """Trace exact pixel-domain boundaries; keep vertices at actual pixel edges."""
    height, width = mask.shape
    edges = {}
    top = mask & ~np.vstack((np.zeros((1, width), dtype=bool), mask[:-1]))
    right = mask & ~np.hstack((mask[:, 1:], np.zeros((height, 1), dtype=bool)))
    bottom = mask & ~np.vstack((mask[1:], np.zeros((1, width), dtype=bool)))
    left = mask & ~np.hstack((np.zeros((height, 1), dtype=bool), mask[:, :-1]))
    for side, boundary in enumerate((top, right, bottom, left)):
        for y, x in zip(*np.nonzero(boundary)):
            if side == 0: a, b = (int(x), int(y)), (int(x + 1), int(y))
            elif side == 1: a, b = (int(x + 1), int(y)), (int(x + 1), int(y + 1))
            elif side == 2: a, b = (int(x + 1), int(y + 1)), (int(x), int(y + 1))
            else: a, b = (int(x), int(y + 1)), (int(x), int(y))
            edges.setdefault(a, []).append(b)
    result = []
    while edges:
        start = min(edges, key=lambda p: (p[1], p[0]))
        point, contour = start, [start]
        for _ in range(4 * (width + 1) * (height + 1)):
            options = edges[point]
            next_point = options.pop()
            if not options:
                del edges[point]
            point = next_point
            if point == start:
                break
            contour.append(point)
        else:
            raise ValueError("Nonclosing domain boundary")
        simplified = []
        for i, point in enumerate(contour):
            previous, following = contour[i - 1], contour[(i + 1) % len(contour)]
            if (point[0] - previous[0]) * (following[1] - point[1]) != (point[1] - previous[1]) * (following[0] - point[0]):
                simplified.append(list(point))
        if len(simplified) >= 3:
            result.append(simplified)
    return result


def rdp_open(points: np.ndarray, tolerance: float) -> list:
    keep = {0, len(points) - 1}
    stack = [(0, len(points) - 1)]
    while stack:
        start, end = stack.pop()
        if end - start <= 1:
            continue
        a, b = points[start], points[end]
        direction = b - a
        offsets = points[start + 1:end] - a
        if np.dot(direction, direction) == 0:
            distances = np.linalg.norm(offsets, axis=1)
        else:
            projections = np.clip((offsets @ direction) / np.dot(direction, direction), 0, 1)
            distances = np.linalg.norm(offsets - projections[:, None] * direction, axis=1)
        relative = int(np.argmax(distances))
        if distances[relative] > tolerance:
            index = start + 1 + relative
            keep.add(index)
            stack.extend(((start, index), (index, end)))
    return points[sorted(keep)].astype(int).tolist()


def simplify_closed(polygon: list, tolerance: float) -> list:
    points = np.asarray(polygon, dtype=np.float64)
    opposite = int(np.argmax(np.sum((points - points[0]) ** 2, axis=1)))
    first = rdp_open(points[:opposite + 1], tolerance)
    second = rdp_open(np.vstack((points[opposite:], points[:1])), tolerance)
    return first[:-1] + second[:-1]


def raster_polygon(polygon: list, shape: tuple) -> np.ndarray:
    result = np.zeros(shape, dtype=bool)
    points = np.asarray(polygon, dtype=np.float64)
    starts, ends = points, np.roll(points, -1, axis=0)
    top = max(0, int(points[:, 1].min()))
    bottom = min(shape[0], int(points[:, 1].max()))
    for y in range(top, bottom):
        cy = y + .5
        active = ((starts[:, 1] <= cy) & (ends[:, 1] > cy)) | ((ends[:, 1] <= cy) & (starts[:, 1] > cy))
        a, b = starts[active], ends[active]
        crossings = sorted((a[:, 0] + (cy - a[:, 1]) * (b[:, 0] - a[:, 0]) / (b[:, 1] - a[:, 1])).tolist())
        if len(crossings) % 2:
            raise ValueError("Odd scanline intersections in proposed clip")
        for left, right in zip(crossings[::2], crossings[1::2]):
            l, r = max(0, int(np.ceil(left - .5))), min(shape[1], int(np.ceil(right - .5)))
            result[y, l:r] = True
    return result


def simplified_safe_polygon(polygon: list, domain: np.ndarray, alpha: np.ndarray) -> tuple[list, dict]:
    if not np.array_equal(raster_polygon(polygon, domain.shape), domain):
        raise ValueError("Exact traced polygon does not reconstruct its pixel domain")
    chosen, tolerance = polygon, 0
    sensitive = alpha >= 64
    for candidate_tolerance in (.49, .70, 1.0, 1.5, 2.0):
        candidate = simplify_closed(polygon, candidate_tolerance)
        if len(candidate) < 3:
            continue
        changed = raster_polygon(candidate, domain.shape) ^ domain
        if np.any(changed & sensitive):
            continue
        chosen, tolerance = candidate, candidate_tolerance
        if len(chosen) <= 256:
            break
    return chosen, {"originalVertices": len(polygon), "vertices": len(chosen), "tolerance": tolerance,
                    "alpha64PixelAssignmentChanges": int(np.count_nonzero((raster_polygon(chosen, domain.shape) ^ domain) & sensitive)),
                    "runtimeVertexLimitPassed": len(chosen) <= 256}


def propose(manifest: dict, alpha: np.ndarray, policy: str = "bounded") -> dict:
    height, width = alpha.shape
    cores, core_records = core_labels(manifest, alpha)
    trusted = cores if policy == "full-component" else trusted_centres(manifest, cores)
    bodies = [frame.get("initialBodyBounds", frame["bodyBounds"]) for frame in manifest["frames"]]
    centres_x = np.array([(b[0] + b[2]) / 2 for b in bodies]).reshape(5, 4)
    centres_y = np.array([(b[1] + b[3]) / 2 for b in bodies]).reshape(5, 4)
    row_group, column_group = np.arange(20) // 4, np.arange(20) % 4
    horizontal, vertical, diagnostics = [], [], []
    for row in range(4):
        a, b = float(np.median(centres_y[row])), float(np.median(centres_y[row + 1]))
        preferred = float(np.median([(bodies[row * 4 + c][3] + bodies[(row + 1) * 4 + c][1]) / 2 for c in range(4)]))
        baseline = None if policy == "full-component" else recorded_gutter(manifest, "horizontal", row, width)
        seam, diagnostic = minimum_seam(alpha, trusted, row_group, row, preferred, (a, b), baseline=baseline)
        horizontal.append(seam)
        diagnostics.append({"axis": "horizontal", "afterGroup": row, **diagnostic})
    for column in range(3):
        a, b = float(np.median(centres_x[:, column])), float(np.median(centres_x[:, column + 1]))
        preferred = float(np.median([(bodies[r * 4 + column][2] + bodies[r * 4 + column + 1][0]) / 2 for r in range(5)]))
        baseline = None if policy == "full-component" else recorded_gutter(manifest, "vertical", column, height)
        seam, diagnostic = minimum_seam(alpha.T, trusted.T, column_group, column, preferred, (a, b), baseline=baseline)
        vertical.append(seam)
        diagnostics.append({"axis": "vertical", "afterGroup": column, **diagnostic})
    yy, xx = np.indices(alpha.shape)
    rows = sum(yy >= seam[None, :] for seam in horizontal)
    columns = sum(xx >= seam[:, None] for seam in vertical)
    owners = rows * 4 + columns
    monotonic = all(np.all(a < b) for a, b in zip(horizontal, horizontal[1:])) and all(np.all(a < b) for a, b in zip(vertical, vertical[1:]))
    frames = []
    for frame in manifest["frames"]:
        index = frame["poseIndex"]
        domain = owners == index
        ys, xs = np.nonzero(domain)
        contours = polygons(domain)
        simplification = None
        if len(contours) == 1:
            contours[0], simplification = simplified_safe_polygon(contours[0], domain, alpha)
        old = np.zeros(alpha.shape, dtype=bool)
        l, t, r, b = frame["sourceRect"]
        old[t:b, l:r] = True
        own = cores == index
        foreign = (cores >= 0) & ~own
        frames.append({"poseIndex": index, "pose": frame["pose"], "oldSourceRect": frame["sourceRect"],
                       "sourceRect": [int(xs.min()), int(ys.min()), int(xs.max()) + 1, int(ys.max()) + 1],
                       "sourceClip": contours[0] if len(contours) == 1 else None,
                       "contourCount": len(contours), "polygonVertices": sum(map(len, contours)),
                       "simplification": simplification,
                       "ownCorePixelsLostBefore": int(np.count_nonzero(own & ~old)),
                       "ownCorePixelsLostAfter": int(np.count_nonzero(own & ~domain)),
                       "trustedCentrePixelsLostAfter": int(np.count_nonzero((trusted == index) & ~domain)),
                       "foreignCorePixelsBefore": int(np.count_nonzero(foreign & old)),
                       "foreignCorePixelsAfter": int(np.count_nonzero(foreign & domain)),
                       "alpha128PixelsRemovedFromOldCrop": int(np.count_nonzero((alpha >= 128) & old & ~domain)),
                       "alpha128PixelsRecoveredFromOutsideOldCrop": int(np.count_nonzero((alpha >= 128) & ~old & domain)),
                       "originX": frame["originX"], "originY": frame["originY"], "pixelsPerUnit": frame["pixelsPerUnit"]})
    return {"id": manifest["id"], "levelMilestone": manifest["levelMilestone"],
            "image": "/" + manifest["original"], "width": width, "height": height,
            "method": "curved-alpha-gutters-full-component-lexicographic-prototype-v5" if policy == "full-component" else "curved-alpha-gutters-bounded-recorded-gutters-prototype-v5", "applied": False,
            "hardConstraintPolicy": "Complete initial component; reject when crossing opaque pixels or a visual counterexample" if policy == "full-component" else "Inner ellipse with half the initial body width/height; complete connected components are diagnostics only because fringe can bridge a neighbouring ornament",
            "originalSha256": manifest["originalSha256"], "coreComponents": core_records,
            "all20CoreComponentsMatched": all(c["matched"] for c in core_records),
            "seamsOrderedEverywhere": bool(monotonic), "seams": diagnostics,
            "singlePolygonEveryPose": all(f["contourCount"] == 1 for f in frames),
            "ownCorePixelsLostBefore": sum(f["ownCorePixelsLostBefore"] for f in frames),
            "ownCorePixelsLostAfter": sum(f["ownCorePixelsLostAfter"] for f in frames),
            "trustedCentrePixelsLostAfter": sum(f["trustedCentrePixelsLostAfter"] for f in frames),
            "foreignCorePixelsBefore": sum(f["foreignCorePixelsBefore"] for f in frames),
            "foreignCorePixelsAfter": sum(f["foreignCorePixelsAfter"] for f in frames),
            "frames": frames}


HTML = r'''<!doctype html><html lang="fr"><meta charset="utf-8"><title>Luma — coutures de pose</title>
<style>body{margin:12px;max-width:1680px;background:#edf2ef;color:#18383d;font:14px system-ui}h1{font-size:20px}h2{font-size:17px;margin:20px 0 8px}.grid{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:6px}.card{background:#fff9ef;border:1px solid #ccd8d2;border-radius:8px;padding:5px}.pair{display:flex;gap:2px;justify-content:center}canvas{width:49%;height:auto;background:#fff7ee}.card small{display:block;font-size:9px;line-height:12px}.bad{border-color:#d64027}.label{font-weight:600;font-size:11px}p{max-width:1100px;font-size:12px}</style>
<h1>Cadrage rectangulaire / domaine courbe</h1><p>Chaque paire utilise les mêmes pixels de l’original. À gauche : cadrage E. À droite : extraction par polygone proposé. Le repère et l’échelle restent identiques. Les chiffres mesurent la conservation des composantes centrales, pas une qualification anatomique ou vestimentaire. Aucun asset sélectionné n’est modifié.</p><main></main>
<script>const records=RECORDS,query=new URLSearchParams(location.search),main=document.querySelector('main');
if(query.get('pose')){const s=document.createElement('style');s.textContent='.grid{grid-template-columns:900px}.pair canvas{width:49%}.label{font-size:18px}.card small{font-size:15px;line-height:20px}';document.head.append(s);}
for(const record of records){if(query.get('sheet')&&query.get('sheet')!==record.id+'-'+record.levelMilestone)continue;
const section=document.createElement('section');section.innerHTML=`<h2>${record.id} · niveau ${record.levelMilestone} — composante perdue ${record.ownCorePixelsLostBefore}→${record.ownCorePixelsLostAfter}, étrangère ${record.foreignCorePixelsBefore}→${record.foreignCorePixelsAfter}</h2><div class="grid"></div>`;main.append(section);
if(query.get('context')){const ns='http://www.w3.org/2000/svg',svg=document.createElementNS(ns,'svg');svg.setAttribute('viewBox','0 430 340 420');svg.setAttribute('width','850');svg.setAttribute('height','1050');svg.style.background='#fff7ee';const im=document.createElementNS(ns,'image');im.setAttribute('href',record.image);im.setAttribute('width',record.width);im.setAttribute('height',record.height);svg.append(im);for(const i of [4,8]){const p=document.createElementNS(ns,'polygon');p.setAttribute('points',record.frames[i].sourceClip.map(p=>p.join(',')).join(' '));p.setAttribute('fill','none');p.setAttribute('stroke',i===4?'#1774ff':'#c925f5');p.setAttribute('stroke-width','.5');svg.append(p);}section.append(svg);continue;}
const img=new Image();img.onload=()=>{for(const frame of record.frames){if(query.get('pose')&&+query.get('pose')!==frame.poseIndex)continue;
const card=document.createElement('div');card.className='card'+(frame.ownCorePixelsLostAfter||frame.foreignCorePixelsAfter?' bad':'');card.innerHTML=`<span class="label">${frame.poseIndex} · ${frame.pose}</span><small>perdu ${frame.ownCorePixelsLostBefore}→${frame.ownCorePixelsLostAfter} · voisin ${frame.foreignCorePixelsBefore}→${frame.foreignCorePixelsAfter}</small><div class="pair"></div>`;section.querySelector('.grid').append(card);
for(let side=0;side<2;side++){const canvas=document.createElement('canvas');canvas.width=180;canvas.height=240;const c=canvas.getContext('2d');const scale=150/(frame.pixelsPerUnit*3.15),ox=90-frame.originX*scale,oy=213-frame.originY*scale;c.save();
if(side===0){const[l,t,r,b]=frame.oldSourceRect;c.beginPath();c.rect(ox+l*scale,oy+t*scale,(r-l)*scale,(b-t)*scale);c.clip();}
else if(frame.sourceClip){c.beginPath();frame.sourceClip.forEach(([x,y],i)=>{if(i)c.lineTo(ox+x*scale,oy+y*scale);else c.moveTo(ox+x*scale,oy+y*scale)});c.closePath();c.clip();}
c.drawImage(img,ox,oy,img.width*scale,img.height*scale);c.restore();card.querySelector('.pair').append(canvas);}
}};img.src=record.image;}
</script></html>'''


def opaque_review(root: Path, output: Path):
    report = json.loads((output / "report.json").read_text())
    records = []
    for sheet in report["reports"]:
        if not any(s["alpha248InteriorCrossings"] for s in sheet["seams"]):
            continue
        original = root / sheet["image"].lstrip("/")
        with Image.open(original) as image:
            alpha = np.asarray(image.convert("RGBA"))[:, :, 3]
        owners = np.full(alpha.shape, -1, dtype=np.int16)
        for frame in sheet["frames"]:
            owners[raster_polygon(frame["sourceClip"], alpha.shape)] = frame["poseIndex"]
        ay, ax = np.nonzero((owners[1:] != owners[:-1]) & (alpha[1:] >= 248) & (alpha[:-1] >= 248))
        by, bx = np.nonzero((owners[:, 1:] != owners[:, :-1]) & (alpha[:, 1:] >= 248) & (alpha[:, :-1] >= 248))
        points = [(int(x), int(y + 1)) for x, y in zip(ax, ay)] + [(int(x + 1), int(y)) for x, y in zip(bx, by)]
        groups = []
        for x, y in sorted(points, key=lambda p: (p[1], p[0])):
            nearby = [group for group in groups if min(abs(x - px) + abs(y - py) for px, py in group) < 32]
            if nearby:
                nearby[0].append((x, y))
            else:
                groups.append([(x, y)])
        for group in groups:
            xs, ys = zip(*group)
            left, top = max(0, min(xs) - 65), max(0, min(ys) - 80)
            right, bottom = min(sheet["width"], max(xs) + 65), min(sheet["height"], max(ys) + 80)
            records.append({"id": sheet["id"], "level": sheet["levelMilestone"], "image": sheet["image"],
                            "width": sheet["width"], "height": sheet["height"], "box": [left, top, right, bottom],
                            "points": group, "frames": sheet["frames"]})
    page = '''<!doctype html><html lang="fr"><meta charset="utf-8"><title>Coutures opaques — revue</title><style>body{font:14px system-ui;background:#eaf1ed;color:#17333a;margin:12px}h1{font-size:20px}.grid{display:grid;grid-template-columns:repeat(4,1fr);gap:10px}.card{background:#fff7ed;padding:8px;border:1px solid #bccdc6;border-radius:8px}svg{width:100%;height:460px;background:#fff7ed}h2{font-size:14px}</style><h1>Pixels opaques sur une couture — contexte original intact</h1><p>Le trait bleu est la frontière proposée. Vérifier membre/ornement réel contre frange générée ; aucun pixel source modifié.</p><div class="grid">'''
    for index, record in enumerate(records):
        l, t, r, b = record["box"]
        page += f'<div class="card"><h2>{index} · {html.escape(record["id"])} L{record["level"]} · {len(record["points"])} arêtes</h2><svg viewBox="{l} {t} {r-l} {b-t}">'
        page += f'<image href="{record["image"]}" width="{record["width"]}" height="{record["height"]}"/>'
        for frame in record["frames"]:
            page += '<polygon points="' + ' '.join(f'{x},{y}' for x, y in frame["sourceClip"]) + '" fill="none" stroke="#146efa" stroke-width=".4"/>'
        page += '</svg></div>'
    page += '</div></html>'
    (output / "opaque-review.html").write_text(page)
    (output / "opaque-regions.json").write_text(json.dumps([{k: v for k, v in record.items() if k != "frames"} for record in records], ensure_ascii=False, indent=2) + "\n")
    print(json.dumps({"regions": len(records), "review": str(output / "opaque-review.html")}))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--project", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--sheet", action="append", help="ID-evoXX, repeatable")
    parser.add_argument("--all", action="store_true")
    parser.add_argument("--policy", choices=("bounded", "full-component"), default="bounded")
    parser.add_argument("--refine-existing", action="store_true", help="Reuse proposed exact domains, verify and simplify polygons only")
    parser.add_argument("--opaque-review-only", action="store_true")
    parser.add_argument("--merge-existing", action="store_true", help="Retain the other already proposed sheets in aggregate review/report")
    parser.add_argument("--output", type=Path, default=Path("docs/validation/v1.18/source-clip-prototype"))
    args = parser.parse_args()
    root = args.project.resolve()
    folder = root / "app/src/main/assets/art/v118"
    paths = sorted(folder.glob("*.json")) if args.all else [folder / f"{key}-poses.json" for key in (args.sheet or DEFAULT)]
    output = args.output if args.output.is_absolute() else root / args.output
    output.mkdir(parents=True, exist_ok=True)
    if args.opaque_review_only:
        opaque_review(root, output)
        return
    reports = []
    for path in paths:
        if not path.exists():
            print(json.dumps({"missing": str(path)}), flush=True)
            continue
        manifest = json.loads(path.read_text())
        original = root / manifest["original"]
        runtime = path.with_suffix(".webp")
        before = [sha(path), sha(original), sha(runtime)]
        with Image.open(original) as image:
            alpha = np.asarray(image.convert("RGBA"))[:, :, 3]
        existing = output / path.name
        if args.refine_existing:
            report = json.loads(existing.read_text())
            if report["inputManifestSha256"] != before[0] or report["originalSha256"] != before[1]:
                report = propose(manifest, alpha, args.policy)
                report["recomputedAfterSelectedInputChanged"] = True
            else:
                for frame in report["frames"]:
                    if frame["sourceClip"]:
                        domain = raster_polygon(frame["sourceClip"], alpha.shape)
                        frame["sourceClip"], frame["simplification"] = simplified_safe_polygon(frame["sourceClip"], domain, alpha)
                        frame["polygonVertices"] = len(frame["sourceClip"])
        else:
            report = propose(manifest, alpha, args.policy)
        report["inputManifestSha256"] = before[0]
        report["selectedInputsUnchanged"] = before == [sha(path), sha(original), sha(runtime)]
        (output / path.name).write_text(json.dumps(report, ensure_ascii=False, separators=(",", ":")) + "\n")
        reports.append(report)
        print(json.dumps({k: report[k] for k in ["id", "levelMilestone", "all20CoreComponentsMatched", "seamsOrderedEverywhere", "singlePolygonEveryPose", "ownCorePixelsLostBefore", "ownCorePixelsLostAfter", "foreignCorePixelsBefore", "foreignCorePixelsAfter", "selectedInputsUnchanged"]}), flush=True)
    if args.merge_existing:
        reports = [json.loads(path.read_text()) for path in sorted(output.glob("*-poses.json"))]
    (output / "review.html").write_text(HTML.replace("RECORDS", json.dumps(reports, ensure_ascii=False, separators=(",", ":"))))
    (output / "report.json").write_text(json.dumps({"schema": 1, "applied": False, "selectedInputsUnchanged": all(r["selectedInputsUnchanged"] for r in reports),
              "note": "Prototype extraction geometry only. Zero component loss alone does not prove semantic/anatomic correctness; inspect all material cases before integration.",
              "sheetCount": len(reports), "reports": reports}, ensure_ascii=False, separators=(",", ":")) + "\n")
    frames = [frame for report in reports for frame in report["frames"]]
    summary = {"sheetCount": len(reports), "frameCount": len(frames), "applied": False,
               "selectedInputsUnchanged": all(r["selectedInputsUnchanged"] for r in reports),
               "all20CoreComponentsMatched": all(r["all20CoreComponentsMatched"] for r in reports),
               "allSeamsOrdered": all(r["seamsOrderedEverywhere"] for r in reports),
               "allSinglePolygons": all(r["singlePolygonEveryPose"] for r in reports),
               "maximumVertices": max((f["polygonVertices"] for f in frames), default=0),
               "framesExceeding256Vertices": sum(f["polygonVertices"] > 256 for f in frames),
               "ownCorePixelsLostBefore": sum(r["ownCorePixelsLostBefore"] for r in reports),
               "ownCorePixelsLostAfter": sum(r["ownCorePixelsLostAfter"] for r in reports),
               "trustedCentrePixelsLostAfter": sum(r.get("trustedCentrePixelsLostAfter", 0) for r in reports),
               "foreignCorePixelsBefore": sum(r["foreignCorePixelsBefore"] for r in reports),
               "foreignCorePixelsAfter": sum(r["foreignCorePixelsAfter"] for r in reports),
               "simplificationAlpha64PixelAssignmentChanges": sum((f.get("simplification") or {}).get("alpha64PixelAssignmentChanges", 0) for f in frames),
               "opaqueInteriorCrossings": [{"id": r["id"], "level": r["levelMilestone"], "seams": [s for s in r["seams"] if s["alpha248InteriorCrossings"]]} for r in reports if any(s["alpha248InteriorCrossings"] for s in r["seams"])],
               "limits": "Dominant core preservation is not full anatomy proof. Translucent pieces, tiny detached details and real overlaps require visual review; clothing is not qualified."}
    (output / "summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n")


if __name__ == "__main__":
    main()
