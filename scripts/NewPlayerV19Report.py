#!/usr/bin/env python3
"""Summarize paired engine runs; never manufacture retention or demographic estimates."""
import csv
import hashlib
import json
import pathlib
import sys
from collections import Counter

ROOT = pathlib.Path(__file__).resolve().parents[1]
BASE = ROOT / "docs/validation/v1.9/journey"
baseline = BASE / (sys.argv[1] if len(sys.argv) > 1 else "baseline-39908e2")
candidate = BASE / (sys.argv[2] if len(sys.argv) > 2 else "candidate")


def rows(folder, name):
    with (folder / name).open() as stream:
        return list(csv.DictReader(stream))


old_levels = rows(baseline, "levels-1-100.csv")
new_levels = rows(candidate, "levels-1-100.csv")
assert old_levels == new_levels, "Published first-100 geometry, budgets and structural metrics changed"
old_attempts = rows(baseline, "synthetic-attempts.csv")
new_attempts = rows(candidate, "synthetic-attempts.csv")
assert len(old_attempts) == len(new_attempts) == 16_000
preserved_columns = set(old_attempts[0]) - {"stars", "three_star_limit"}
for old, new in zip(old_attempts, new_attempts):
    assert all(old[k] == new[k] for k in preserved_columns), "Behaviour or random-number pairing changed"

old_profiles = rows(baseline, "profile-journey-totals.csv")
new_profiles = rows(candidate, "profile-journey-totals.csv")
assert len(old_profiles) == len(new_profiles) == 72
profile_table = []
for profile in dict.fromkeys(r["profile"] for r in new_profiles):
    old = [r for r in old_profiles if r["profile"] == profile]
    new = [r for r in new_profiles if r["profile"] == profile]
    identity = ("profile", "sensitivity", "seed")
    for a, b in zip(old, new):
        assert all(a[k] == b[k] for k in identity)
    profile_table.append({
        "profile": profile,
        "journeys": len(new),
        "baseline_cleared_range": [min(int(r["cleared"]) for r in old), max(int(r["cleared"]) for r in old)],
        "candidate_cleared_range": [min(int(r["cleared"]) for r in new), max(int(r["cleared"]) for r in new)],
        "baseline_mean_coins": round(sum(int(r["coins"]) for r in old) / len(old), 2),
        "candidate_mean_coins": round(sum(int(r["coins"]) for r in new) / len(new), 2),
        "candidate_restarts": sum(int(r["restarts"]) for r in new),
        "candidate_help_units": sum(int(r["help_units"]) for r in new),
        "candidate_refunded_undos": sum(int(r["undo"]) for r in new),
        "candidate_restored_interruptions": sum(int(r["resumes"]) for r in new),
    })

bands = []
for lo, hi in ((1, 5), (6, 10), (11, 20), (21, 30), (31, 50), (51, 75), (76, 100)):
    selected = [r for r in new_levels if lo <= int(r["level"]) <= hi]
    bands.append({"from": lo, "through": hi, "levels": len(selected),
                  **{f"mean_{key}": round(sum(int(r[key]) for r in selected) / len(selected), 2)
                     for key in ("pieces", "minimum", "base", "allowance", "decision_points", "structural_score")}})

spikes = sorted(({
    "level": int(b["level"]), "before_score": int(a["structural_score"]),
    "score": int(b["structural_score"]), "increase": int(b["structural_score"]) - int(a["structural_score"])
} for a, b in zip(new_levels, new_levels[1:])), key=lambda v: v["increase"], reverse=True)[:15]

old_rewards = rows(baseline, "actual-reward-ledger.csv")
new_rewards = rows(candidate, "actual-reward-ledger.csv")
trials = [{"after_clears": int(r["clears"]), "entering_level": int(r["unlocked"]), "ids": r["trial_claims"].split(";")}
          for r in new_rewards if r["stars"] == "3" and r["trial_claims"]]
comparison = {
    "passed": True,
    "baseline": baseline.name,
    "candidate": candidate.name,
    "first_100_geometry_budget_and_metrics_identical": True,
    "paired_independent_attempt_outcomes_identical": True,
    "independent_attempts_per_version": 16000,
    "sequential_journeys_per_version": 72,
    "profile_results": profile_table,
    "structural_bands": bands,
    "largest_adjacent_structural_increases": spikes,
    "candidate_claimed_power_trials": trials,
    "baseline_independent_star_distribution": dict(Counter(r["stars"] for r in old_attempts)),
    "candidate_independent_star_distribution": dict(Counter(r["stars"] for r in new_attempts)),
    "limitations": [
        "No real people, Google accounts or social network were simulated.",
        "Aggregate 2026 evidence motivates coverage axes; numerical errors, help and interruption inputs are engineering assumptions.",
        "Age labels are crossed with every profile using identical parameters; no demographic skill, spending or failure inference.",
        "UI tutorial gesture corrections and the new boss mode are not part of these unchanged Board-policy completion comparisons.",
        "No enjoyment, desire-to-continue, D1/D7, purchase conversion or clinical effect is estimated."
    ],
}
(BASE / "comparison.json").write_text(json.dumps(comparison, indent=2) + "\n")
manifest = {}
for folder in (baseline, candidate):
    for path in sorted(folder.glob("*")):
        if path.is_file():
            manifest[str(path.relative_to(BASE))] = hashlib.sha256(path.read_bytes()).hexdigest()
(BASE / "report-inputs.json").write_text(json.dumps({"sha256": manifest}, indent=2) + "\n")
print(json.dumps({"passed": True, "paired_attempts_each": len(new_attempts), "paired_journeys_each": len(new_profiles), "output": str(BASE / "comparison.json")}))
