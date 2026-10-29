#!/usr/bin/env python3
"""Verify real wardrobe choices, allowing exactly one observed preference key to change."""
from pathlib import Path
import argparse
import hashlib
import json


MODE_PREF = "luma_drift_save_v1"
MODE_KEY = "companion_original_costume_v118"
PREFS = {MODE_PREF, "luma_drift_meta_v1", "luma_session", "luma_expedition_session_v118"}
ABSENT = object()
PHASES = (
    ("wardrobe-costume-choice", (
        ("before", ABSENT),
        ("wardrobe-costume-entry", ABSENT),
        ("wardrobe-outfit-selected", False),
        ("wardrobe-outfit-recreated", False),
        ("wardrobe-costume-returned", True),
        ("wardrobe-outfit-ready-for-cold", False),
        ("after", False),
    ), 3),
    ("wardrobe-costume-cold", (
        ("before", False),
        ("wardrobe-outfit-cold-restored", False),
        ("wardrobe-original-selected", True),
        ("wardrobe-original-recreated", True),
        ("after", True),
    ), 1),
    ("wardrobe-costume-original-cold", (
        ("before", True),
        ("wardrobe-original-cold-restored", True),
        ("after", True),
    ), 0),
)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def encoded(value):
    # JSON serialization distinguishes a bool from numeric 0/1, unlike Python equality.
    return json.dumps(value, sort_keys=True, separators=(",", ":"), ensure_ascii=False)


def preference_differences(before, after):
    """No prefix, file, session, inventory or implicit-default exclusions are allowed."""
    differences = []
    for pref in sorted(set(before) | set(after)):
        if pref not in before or pref not in after:
            differences.append({"pref": pref, "reason": "preference-file-set-changed"})
            continue
        for key in sorted(set(before[pref]) | set(after[pref])):
            if pref == MODE_PREF and key == MODE_KEY:
                continue
            old_present = key in before[pref]
            new_present = key in after[pref]
            if old_present != new_present or encoded(before[pref].get(key)) != encoded(after[pref].get(key)):
                differences.append({"pref": pref, "key": key,
                                    "before_present": old_present, "after_present": new_present,
                                    "before": before[pref].get(key), "after": after[pref].get(key)})
    return differences


def verify(directories):
    failures, phases, observations, inputs = [], [], [], {}
    baseline = None
    for directory, (phase, snapshots, expected_toggles) in zip(directories, PHASES):
        report_path = directory / "report.json"
        report = json.loads(report_path.read_text())
        native_path = directory / "native" / "report.json"
        native = json.loads(native_path.read_text())
        for path in (report_path, native_path):
            inputs[str(path.resolve())] = digest(path)
        if not (report.get("passed") is True and native.get("passed") is True
                and report.get("phase") == native.get("phase") == phase
                and report.get("run_token") == native.get("runToken")
                and report.get("native") == native):
            failures.append({"phase": phase, "reason": "fresh-passing-native-phase-required"})
        toggle_actions = [action for action in native.get("actions", [])
                          if action.get("resourceId", "").endswith(":id/wardrobe_costume_toggle")]
        if len(toggle_actions) != expected_toggles:
            failures.append({"phase": phase, "reason": "actual-wardrobe-toggle-count",
                             "expected": expected_toggles, "actual": len(toggle_actions)})
        lifecycle = [action for action in native.get("actions", [])
                     if action.get("lifecycle") == "Android Activity.recreate"]
        expected_recreations = 0 if phase.endswith("original-cold") else 1
        if len(lifecycle) != expected_recreations or any(
                action.get("beforeInstance") == action.get("afterInstance") for action in lifecycle):
            failures.append({"phase": phase, "reason": "actual-distinct-activity-recreation-required"})
        phase_values = {}
        for name, expected in snapshots:
            path = directory / "native" / (name + ".json")
            state = json.loads(path.read_text())
            inputs[str(path.resolve())] = digest(path)
            if set(state) != PREFS or any(not isinstance(values, dict) for values in state.values()):
                raise ValueError("Complete four-file observed snapshot required: " + str(path))
            phase_values[name] = state
            if baseline is None:
                baseline = state
                prior_choices = sorted(key for key in baseline[MODE_PREF] if key.startswith("cosmetic_slot_v17_"))
                if prior_choices:
                    failures.append({"phase": phase, "reason": "implicit-default-profile-has-explicit-cosmetic-selection",
                                     "keys": prior_choices})
            value = state[MODE_PREF].get(MODE_KEY, ABSENT)
            mode_matches = value is ABSENT if expected is ABSENT else type(value) is bool and value == expected
            differences = preference_differences(baseline, state)
            if not mode_matches:
                failures.append({"phase": phase, "snapshot": name, "reason": "mode-does-not-match-ui-sequence",
                                 "expected": "ABSENT" if expected is ABSENT else expected,
                                 "actual": "ABSENT" if value is ABSENT else value})
            if differences:
                failures.append({"phase": phase, "snapshot": name,
                                 "reason": "unexpected-preference-change", "differences": differences})
            observations.append({"phase": phase, "snapshot": name, "sha256": digest(path),
                                 "mode": "ABSENT" if value is ABSENT else value,
                                 "unrelated_fields_equal": not differences})
        phases.append({"phase": phase, "run_token": report.get("run_token"),
                       "process_id": native.get("processId"), "apk_sha256": report.get("apk_sha256"),
                       "observer_sha256": report.get("observer_sha256"), "package": report.get("package"),
                       "values": phase_values})
    for field in ("apk_sha256", "observer_sha256", "package"):
        if len({phase[field] for phase in phases}) != 1 or not phases[0][field]:
            failures.append({"reason": "same-frozen-candidate-observer-package-required", "field": field})
    for field in ("run_token", "process_id"):
        if len({phase[field] for phase in phases}) != 3 or any(not phase[field] for phase in phases):
            failures.append({"reason": "three-distinct-real-process-executions-required", "field": field})
    cold_links = []
    for previous, current in zip(phases, phases[1:]):
        exact = encoded(previous["values"]["after"]) == encoded(current["values"]["before"])
        cold_links.append({"from": previous["phase"], "to": current["phase"], "complete_state_exact": exact})
        if not exact:
            failures.append({"phase": current["phase"], "reason": "complete-state-must-survive-cold-process"})
    for phase in phases:
        phase.pop("values")
    return {"passed": not failures,
            "allowed_preference_delta": {"pref": MODE_PREF, "key": MODE_KEY},
            "implicit_original_default_required": True,
            "phases": phases, "cold_links": cold_links, "observations": observations,
            "failures": failures, "input_sha256": inputs,
            "scope": "Read-only comparison of three actual native UI phases. Only the exact presentation-mode key may change; every other observed game, meta, campaign and expedition field must be identical, including coins, ownership, selected items, copies and equipped companion. The SDK snapshots omit seed-named keys. No purchase, grant, solver, preference write or target invocation is used. Captures and mode persistence do not qualify apparel fit or physical-phone rendering."}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--choice", required=True, type=Path, help="Host evidence directory of wardrobe-costume-choice")
    parser.add_argument("--worn-cold", required=True, type=Path, help="Host evidence directory of wardrobe-costume-cold")
    parser.add_argument("--original-cold", required=True, type=Path, help="Host evidence directory of wardrobe-costume-original-cold")
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    if args.output.exists():
        raise FileExistsError("Comparison evidence must be new: " + str(args.output))
    result = verify((args.choice, args.worn_cold, args.original_cold))
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2, ensure_ascii=False) + "\n")
    print(json.dumps({"passed": result["passed"], "observations": len(result["observations"]),
                      "failures": result["failures"], "output": str(args.output)}, indent=2))
    if not result["passed"]:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
