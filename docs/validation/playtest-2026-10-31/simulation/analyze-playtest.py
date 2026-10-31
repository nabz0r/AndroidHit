#!/usr/bin/env python3
"""Extra descriptive accounting for the isolated31October run. Reads existing CSVs."""
import collections
import csv
import gzip
import hashlib
import json
from pathlib import Path

BASE = Path(__file__).resolve().parent
DATA = BASE / "run-2026103101"
ROOT = BASE.parents[3]

def load(name):
    return json.loads((BASE / name).read_text())

def rows(name):
    with gzip.open(DATA / name, "rt", newline="") as stream:
        yield from csv.DictReader(stream)

def fraction(n, d):
    return {"numerator": n, "denominator": d, "ratio": n / d if d else None}

def quantiles(values):
    values = sorted(values)
    return {str(q): values[round((len(values) - 1) * q)] for q in [0, .1, .25, .5, .75, .9, 1]}

analysis = load("analysis.json")
summary = load("run-2026103101/summary.json")
assert summary["config"]["seed"] == 2026103101
ids = summary["config"]["scenarios"]
trajectories = collections.defaultdict(list)
for row in rows("trajectories.csv.gz"):
    trajectories[row["scenario"]].append(row)
assert sum(map(len, trajectories.values())) == 8000

days = collections.defaultdict(lambda: {"activeDays": 0, "energyStopWithAction": 0, "energyStopWithoutAction": 0, "energyTrajectories": set(), "stopsByDay": collections.defaultdict(collections.Counter)})
for row in rows("day-results.csv.gz"):
    group = days[row["scenario"]]
    active = row["activeSession"] == "true"
    assert active == (int(row["decisions"]) > 0)
    group["activeDays"] += int(active)
    group["stopsByDay"][row["day"]][row["stopReason"]] += 1
    if row["stopReason"] == "energy_empty":
        group["energyStopWithAction" if active else "energyStopWithoutAction"] += 1
        group["energyTrajectories"].add((row["profileId"], row["repetition"]))

native_first = collections.defaultdict(collections.Counter)
seen = set()
target_completed = set()
for row in rows("level-results.csv.gz"):
    if row["outcome"] == "completed" and int(row["level"]) in [43, 48]:
        key = row["scenario"], row["profileId"], row["repetition"], int(row["level"])
        assert key not in target_completed, "A level was completed twice in one trajectory"
        target_completed.add(key)
    if row["scenario"] != ids[0]:
        continue
    key = row["profileId"], row["repetition"], int(row["level"])
    group = native_first[int(row["level"])]
    group["starts"] += 1
    if key in seen:
        group["retryStarts"] += 1
    else:
        seen.add(key)
        group["firstStarts"] += 1
        group["first_" + row["outcome"]] += 1

scenarios = {}
profile_differences = {}
for scenario in ids:
    a = analysis["scenarios"][scenario]
    values = trajectories[scenario]
    assert len(values) == 2000
    d = days[scenario]
    costs = [int(r["coinsSpent"]) for r in values]
    end_coins = [int(r["coinsEnd"]) for r in values]
    for r in values:
        assert int(r["coinsStart"]) == 180
        assert int(r["coinsEnd"]) == 180 + int(r["coinsEarned"]) - int(r["coinsSpent"])
    levels = a["perLevel"]
    peak_levels = sorted(levels, key=lambda n: (-levels[n]["outcomes"].get("budget_exhausted", 0), int(n)))[:10]
    scenarios[scenario] = {
        "trajectories": 2000,
        "attempts": a["starts"],
        "completedPuzzles": a["outcomes"]["completed"],
        "outcomes": a["outcomes"],
        "actions": a["totals"]["actions"],
        "manualExits": a["totals"]["manualExits"],
        "automatedExits": a["totals"]["automatedExits"],
        "blocked": a["totals"]["blocked"],
        "repeatedErrors": a["totals"]["repeatedErrors"],
        "blockedReasons": {k: a["totals"][k] for k in ["closedGateBlocked", "pieceBlocked", "barrierBlocked"]},
        "undosAllIncludingCompanionRewinds": a["totals"]["undos"],
        "resources": a["resources"],
        "paidUndoTrajectories": fraction(sum(v > 0 for v in costs), 2000),
        "coinSpendRelativeToEarned": fraction(a["resources"]["coinsSpent"], a["resources"]["coinsEarned"]),
        "coinsSpentQuantiles": quantiles(costs),
        "endCoinQuantiles": quantiles(end_coins),
        "endCoinZeroTrajectories": sum(v == 0 for v in end_coins),
        "endCoinsCaution": "Final balances do not prove absence of any intermediate affordability constraint. No cosmetic/gacha/equipment purchase occurs in this protocol.",
        "activeDays": d["activeDays"],
        "configuredDays": a["configuredDays"],
        "dailyStops": a["dailyStops"],
        "energyStopPerConfiguredDay": fraction(a["dailyStops"].get("energy_empty", 0), a["configuredDays"]),
        "energyStopWithAction": d["energyStopWithAction"],
        "energyStopWithoutAction": d["energyStopWithoutAction"],
        "energyStoppedTrajectories": fraction(len(d["energyTrajectories"]), 2000),
        "dayCapPerConfiguredDay": fraction(a["dailyStops"].get("day_action_cap", 0), a["configuredDays"]),
        "stopsByModeledDay": {n: dict(v) for n, v in sorted(d["stopsByDay"].items())},
        "manualAssistance": a["manualAssistance"],
        "directCueManualShare": fraction(a["manualAssistance"]["directCueFollowedManualExits"], a["totals"]["manualExits"]),
        "automatedExitShare": fraction(a["totals"]["automatedExits"], a["totals"]["exits"]),
        "skillRequests": a["totals"]["skillRequests"],
        "skillUnavailable": fraction(a["totals"]["skillUnavailable"], a["totals"]["skillRequests"]),
        "learningCurveArrivals": [{"level": n, **{k: levels[str(n)][k] for k in ["starts", "arrivingTrajectories", "outcomes"]}} for n in [1,10,11,13,17,27,29,31,33,36,43,48,61,80,94] if str(n) in levels],
        "topExhaustionLevels": [{"level": int(n), "starts": levels[n]["starts"], "arrivingTrajectories": levels[n]["arrivingTrajectories"], "exhausted": levels[n]["outcomes"].get("budget_exhausted", 0), "undos": levels[n]["totals"]["undos"], "paidUndoCoins": levels[n]["totals"]["paidUndoCoins"], "blocked": levels[n]["totals"]["blocked"], "blockedReasons": {k: levels[n]["totals"][k] for k in ["closedGateBlocked", "pieceBlocked", "barrierBlocked"]}} for n in peak_levels],
    }
    if scenario != ids[0]:
        before = {(r["profileId"], r["repetition"]): r for r in trajectories[ids[0]]}
        paired = collections.defaultdict(list)
        for row in values:
            previous = before[row["profileId"], row["repetition"]]
            paired[row["profileId"]].append(int(row["levelsCleared"]) - int(previous["levelsCleared"]))
        means = [sum(v) / len(v) for v in paired.values()]
        profile_differences[scenario] = {
            "profileClusters": len(means),
            "repetitionsPerProfile": 20,
            "profilesMeanLevelsHigher": sum(v > 0 for v in means),
            "profilesMeanLevelsLower": sum(v < 0 for v in means),
            "profilesMeanLevelsEqual": sum(v == 0 for v in means),
            "meanPairedLevels": sum(means) / len(means),
            "profileMeanDifferenceQuantiles": quantiles(means),
            "scope": "Within generated profiles, not population/human inference; acquisition and different campaign exposure remain excluded.",
        }

total_outcomes = collections.Counter()
for a in scenarios.values():
    total_outcomes.update(a["outcomes"])
out = {
    "schemaVersion": 1,
    "seed": 2026103101,
    "provenanceSha256": hashlib.sha256((DATA / "provenance.json").read_bytes()).hexdigest(),
    "counts": {**analysis["counts"], "completedPuzzles": sum(v["completedPuzzles"] for v in scenarios.values()), "actions": sum(v["actions"] for v in scenarios.values()), "blockedEvents": sum(v["blocked"] for v in scenarios.values()), "outcomes": dict(total_outcomes), "humanParticipants": 0},
    "scenarios": scenarios,
    "nativeFirstAttemptAccounting": {str(n): dict(v) for n,v in sorted(native_first.items())},
    "profilePairedDifferences": profile_differences,
    "trajectoryQuantilesByCohort": {n: analysis["scenarios"][n]["trajectoryQuantiles"] for n in ids},
    "probes": analysis["commonEnvironmentTransfer"],
    "probeInterpretation": "Cold/trained policies are adaptive during each probe; only initial learned-rule states are transplanted, fatigue/frustration/memory reset. Different prior campaign exposure remains. No human transfer/comprehension claim.",
    "notModeled": ["decoration rendering or room interactions", "outfit rendering, equipment or squad ownership/acquisition", "LumaGold, real-money purchases or provider balances", "ads, rewarded ads, consent or ad lifecycle", "gacha, home purchases or stock powers", "Android persistence or navigation", "inspection/HUD/visual comprehension", "human enjoyment/emotion/retention"],
    "denominatorLimits": ["Each condition has2000 trajectories and16000 configured days over8 modeled days; active-day counts differ.", "Starts retain retries; unique arriving trajectories are separate and later levels are arrival-selected.", "Horizon censoring is a positive-budget open attempt at the finite8-day study endpoint, not a human dropout or loss.", "Manual exits may follow coach/Focus/GUIDE; absence of a direct cue on one action does not prove no prior help.", "Exact solver continuation stays inside explicitly requested native assistance or the separate104-board control; bounded policy has no solver access."],
}
native = scenarios[ids[0]]
loaned = scenarios["loaned_mixed_123"]
native_summary = next(s for s in summary["scenarios"] if s["id"] == ids[0])
loaned_summary = next(s for s in summary["scenarios"] if s["id"] == "loaned_mixed_123")
families = loaned_summary["mechanics"]["familyUses"]
out["trioInterpretation"] = {
    "scope": "Paired total effect of this synthetic policy and environment, not an intrinsic talent nerf or measured human outcome. Contribution of each mechanism is not identified without another policy experiment.",
    "requestsPerAllDecisions": fraction(loaned["skillRequests"], loaned["actions"]),
    "unavailablePerRequest": loaned["skillUnavailable"],
    "successfulFamilyUses": families,
    "nativeCostsCharge": {"GUIDE": 2, "FLOW": 3, "REWIND": 2, "combo_PULSE": 0},
    "derivedChargeSpentFromSuccessfulFamilyCounts": families["GUIDE"] * 2 + families["FLOW"] * 3 + families["REWIND"] * 2,
    "chargeDerivation": "Derived from each successful family use and the native per-board trio costs, not a separately recorded per-turn charge ledger. Refused drafts spend no native charge/Coins.",
    "nativeChargeState": "Initial3 per board, cap6, +1 per distinct exited tile; one use per slot and one combo after two distinct families. Different from the separate tactical chapter.",
    "policyChoice": "Pressure is remembered block, visible dead-end warning or >8 present pieces; requests also occur every6th decision. Ready slots are shuffled and sampled; REWIND additionally requires remembered block/dead-end. This is not an optimized scheduler.",
    "modeledRequestCost": "Each request, including refusal, consumes one policy decision and the same uncalibrated duration/fatigue rule; native GUIDE/refusal do not spend a productive-move budget unit.",
    "manualLearningExposure": {"nativeManualExits": native["manualExits"], "loanedManualExits": loaned["manualExits"], "loanedAutomatedExits": loaned["automatedExits"], "note": "CognitiveAgent.feedback learns on exit, not skill_flow/skill_pulse; automation changes manual learning exposure. It still applies fatigue for every modeled action."},
    "observedDecisionDifference": loaned["actions"] - native["actions"],
    "observedRefusalDifference": loaned["blocked"] - native["blocked"],
    "commonRuleMean": {"native": native_summary["meanCommonRuleKnowledge"], "loaned": loaned_summary["meanCommonRuleKnowledge"]},
    "randomness": "Same profile/input seeds are paired. Changed actionIndex, state and exposure change subsequent random draws; this is not the same exact action stream with extra ability effects.",
}
out["u43U48ConditionalProgression"] = {}
for scenario in ids:
    out["u43U48ConditionalProgression"][scenario] = {}
    for n in [43, 48]:
        a = analysis["scenarios"][scenario]["perLevel"][str(n)]
        assert a["outcomes"].get("completed", 0) == sum(sc == scenario and level == n for sc,pid,rep,level in target_completed)
        out["u43U48ConditionalProgression"][scenario][str(n)] = {
            "arrivingTrajectories": a["arrivingTrajectories"],
            "startsIncludingRetries": a["starts"],
            "retryStarts": a["starts"] - a["arrivingTrajectories"],
            "completedAttemptAndUniqueProgressedTrajectoryCount": a["outcomes"].get("completed", 0),
            "progressedPerArrivingTrajectory": fraction(a["outcomes"].get("completed", 0), a["arrivingTrajectories"]),
            "completedPerAttemptStart": fraction(a["outcomes"].get("completed", 0), a["starts"]),
            "budgetExhaustedPerAttemptStart": fraction(a["outcomes"].get("budget_exhausted", 0), a["starts"]),
            "horizonOpenAttempts": a["outcomes"].get("horizon_censored", 0),
        }
assert out["counts"]["attemptRows"] == sum(total_outcomes.values()) == 377413
assert out["counts"]["completedPuzzles"] == 372673
assert out["counts"]["blockedEvents"] == 173154
(BASE / "analysis-detail.json").write_text(json.dumps(out, indent=2, sort_keys=True) + "\n")
print(json.dumps({"status": "PASS", "counts": out["counts"], "profilePairedDifferences": profile_differences}, indent=2))
