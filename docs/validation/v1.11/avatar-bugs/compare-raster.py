#!/usr/bin/env python3
"""Summarize recorded final-pixel CSVs without claiming human recognition."""
import csv
import json
import statistics
from pathlib import Path

base = Path(__file__).resolve().parent
report = {"scope": "Independent host-raster differences only; not a human recognition or Android acceptance rate.", "results": []}
for label in ("baseline", "candidate"):
    with (base / f"{label}-identity-pixels.csv").open() as stream:
        rows = list(csv.DictReader(stream))
    identity = {}
    for radius in ("18", "32"):
        selected = [row for row in rows if row["radius"] == radius]
        ratios = [int(row["changed_pixels"]) / int(row["bare_changed_pixels"]) for row in selected]
        identity[radius] = {"min_visible_ratio": min(ratios), "median_visible_ratio": statistics.median(ratios), "min_changed_pixels": min(int(row["changed_pixels"]) for row in selected)}
    with (base / f"{label}-equipment-pixels.csv").open() as stream:
        rows = list(csv.DictReader(stream))
    gear = {}
    for radius in ("18", "32"):
        for slot in ("HEAD", "AURA", "CHARM"):
            values = [int(row["changed_pixels"]) for row in rows if row["radius"] == radius and row["slot"] == slot]
            gear[f"{radius}:{slot}"] = {"minimum": min(values), "median": statistics.median(values)}
    report["results"].append({"label": label, "identity": identity, "gear": gear})
(base / "comparison.json").write_text(json.dumps(report, indent=2) + "\n")
