#!/usr/bin/env python3
"""Check two-session RC2 puzzle coverage, never a successful single marathon.

The failed C report is preserved: only its eight complete PASS batches are used.
The tail must be a separately restored 801..1000 run with declared synthetic
unlocked=801 setup. No Android access, save injection or engine replay is done here.
"""
import argparse
import copy
from datetime import datetime, timezone
import difflib
import hashlib
import importlib.util
import json
from pathlib import Path
import re
import sys
import tempfile

SPEC = importlib.util.spec_from_file_location(
    "marathon_evidence_reader", Path(__file__).with_name("check-native-journey-v114.py"))
core = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(core)
ROOT, require, joined = core.ROOT, core.require, core.joined
C_REPORT = "d35c7a1b2ffd676f466408ee5e93a2a2a82efb00599f76ec83201bfcdbc69cd1"
MARATHON_CHECKER = "9d807475061ac0ace8ede65d538a4ccfa19f358c9b618aedf686072a9e8459ed"

TAIL_SDK = "474477eac3f613834b78c66a90001e280e1ba6bf88e3bfafdfcd2a0d2f6f95ea"
TAIL_SNAPSHOT = "2f8f305a645409b08f9299c4915240f26f49221e6668559c4571faf57fcc8874"
TAIL_DIFF = "6f288d8052b6531f80bdb351fbdade4f5a000f62b8412d9560e961fe1f6abac8"
TAIL_PREREQUISITE = "148b4b9e895930eeeb6cd7d36cba0510ea1f820d2701e62a1ae8f266d1b29dd4"


def check_host(reader, path, first, last, sdk, expected_pass):
    host = reader.json(path, C_REPORT if first == 1 else None)
    require(host.get("passed") is expected_pass and host.get("restored") is True,
            "Both original hosts must retain their true result and restored=true")
    require(host.get("cases") == "journey" and host.get("catalog_family") == "N"
            and host.get("journey_from") == first and host.get("journey_through") == last
            and host.get("batch_size") == 100 and host.get("package") == core.PACKAGE
            and host.get("serial") == "emulator-5580",
            "Wrong independently labelled host scope")
    for key in ("apk_sha256", "expected_apk_sha256", "installed_apk_sha256_after_run"):
        require(host.get(key) == core.APK, "Host is not the exact unchanged RC2 Lab APK: " + key)
    for key in ("test_apk_sha256", "installed_test_apk_sha256"):
        require(host.get(key) == sdk, "Wrong session SDK: " + key)
    for key in ("isolated_test_package_removed", "installed_apk_matches_expected_after_run",
                "rotation_settings_restored"):
        require(host.get(key) is True, "Incomplete host cleanup: " + key)
    if "display_size_restored" in host:
        require(host["display_size_restored"] is True, "Display restoration failed")
    require(not any(host.get(key) for key in ("new_native_crash_entries", "restore_error",
                "test_package_restore_error", "final_apk_verification_error",
                "rotation_restore_error", "display_restore_error")), "Host cleanup/crash failure")
    if expected_pass:
        require(not host.get("error"), "Successful tail still records an error")
    return host


def check_archive(reader, path, host, sdk, snapshot_sha, sources):
    reference = reader.json(path.parent / "sdk-source-reference.json")
    snapshot_key = "snapshot_sha256" if sdk == core.SDK else "source_snapshot_sha256"
    require(reference.get("test_apk_sha256") == sdk and reference.get(snapshot_key) == snapshot_sha,
            "Host SDK reference is not the reviewed revision")
    archive = joined(ROOT, reference["archive"])
    if "source_snapshot" in reference:
        require(joined(ROOT, reference["source_snapshot"]) == archive / "source-snapshot.json",
                "SDK reference source path differs from archive")
    snapshot = reader.json(archive / "source-snapshot.json", snapshot_sha)
    require(snapshot.get("test_apk_sha256") == sdk, "Source archive belongs to another SDK")
    inventory = snapshot["source_sha256"]
    for name, digest in inventory.items():
        reader.bytes(joined(archive / "runner-source", name), digest)
    require(host.get("runner_source_sha256") == inventory.get(core.RUNNER_PATH)
            and host.get("wrapper_source_sha256") == inventory.get("run.py"),
            "Host sources do not match their archived SDK inventory")
    link = reader.json(archive / "fixture-link.json")
    require(link.get("test_apk_sha256") == sdk, "Fixture bridge belongs to another SDK")
    if sdk == core.SDK:
        require(link.get("fixture_count") == 1000 and link.get("all_match") is True,
                "Incomplete original SDK fixture bridge")
        revalidation_path = joined(ROOT, link["independent_revalidation"])
    else:
        require(link.get("passed") is True and link.get("n_fixture_count") == 1000
                and link.get("previous_inventory") == "../native-sdk-marathon-rc2/fixture-link.json",
                "Incomplete separate-tail fixture bridge")
        previous_link = reader.json(ROOT / "docs/validation/v1.14/native-sdk-marathon-rc2/fixture-link.json")
        revalidation_path = joined(ROOT, previous_link["independent_revalidation"])
    revalidation = reader.json(revalidation_path, core.REVALIDATION)
    require(revalidation.get("passed") is True and revalidation.get("regeneratedFixtureCount") == 1000
            and revalidation.get("productiveActionCount") == 19920
            and revalidation.get("allFixtureBytesIdenticalAcrossFinalSourceRegenerationWorkingAssetsAndFrozenSdk") is True
            and revalidation.get("frozenSdk", {}).get("sha256") == core.OLD_SDK
            and revalidation.get("finalSourceFreeze", {}).get("inputsSha256") == sources,
            "SDK fixtures are not bound to the same final116 app inputs")
    return archive, inventory, revalidation


def check_phase(reader, path, host, phase, index, first, fixtures, selected):
    last = first + 99
    name = f"batch-{first:04d}-{last:04d}"
    require(phase.get("phase") == name and phase.get("journey_from") == first
            and phase.get("journey_through") == last, "Missing, duplicate or reordered phase: " + name)
    raw = reader.json(path.parent / name / "report.json")
    require(raw == phase.get("native") and raw.get("package") == core.PACKAGE,
            "Embedded and raw native report differ: " + name)
    checks = raw.get("checks", [])
    require(raw.get("checkCount") == len(checks) and checks, "Native check count mismatch")
    if selected:
        require(raw.get("passed") is True and all(check.get("pass") is True for check in checks),
                "Only entirely passed native phases may contribute coverage")
    else:
        require(raw.get("passed") is False and any(check.get("pass") is False for check in checks),
                "Original failed C phase must remain visibly failed")
    previous_pid = host["phases"][index - 1]["native"].get("processId") if index else None
    pid = raw.get("processId")
    if previous_pid is not None and pid is not None:
        require(pid != previous_pid, "Adjacent recorded cold phases share a PID")
    boundaries = host.get("batch_process_boundaries")
    if boundaries is not None:
        require(len(boundaries) == len(host["phases"]), "Incomplete recorded cold boundaries")
        boundary = boundaries[index]
        require(boundary.get("batch") == name and boundary.get("force_stop") is True
                and boundary.get("package_absent_before_restart") is True, "Incomplete cold boundary")
        if "previous_process_id" in boundary:
            require(boundary["previous_process_id"] == previous_pid, "Cold previous PID mismatch")
        if "restarted_process_id" in boundary:
            require(boundary["restarted_process_id"] == pid, "Cold restarted PID mismatch")
    captures = raw.get("captures", [])
    require(len(set(captures)) == len(captures), "Duplicated capture name in phase")
    shots = phase.get("screenshots", {})
    require(set(shots) == {name + "/" + capture + ".png" for capture in captures},
            "Native capture names and phase PNG index differ")
    directory = ROOT / "docs/screenshots/v1.14/ui-bugs" / path.parent.name
    for shot, digest in shots.items():
        require(host.get("screenshots", {}).get(shot) == digest
                and re.fullmatch(r"[a-f0-9]{64}", digest), "Host PNG index/hash mismatch")
        require(reader.bytes(joined(directory, shot), digest).startswith(b"\x89PNG\r\n\x1a\n"),
                "Indexed image is not a PNG")
    observation = reader.json(path.parent / name / "journey-observations.json")
    levels = observation.get("levels", [])
    is_tail = bool(host.get("synthetic_unlock_prerequisite"))
    if is_tail:
        require(raw.get("syntheticUnlockPrerequisite") is True
                and observation.get("syntheticUnlockPrerequisite") is True,
                "Tail native report/observations omit the synthetic prerequisite declaration")
        for scope in (raw.get("scope", ""), observation.get("scope", "")):
            require("synthetic" in scope.lower() and "801" in scope
                    and "no profile injection" not in scope.lower(), "Contradictory tail native scope")
    if selected:
        require([level.get("level") for level in levels] == list(range(first, last + 1)),
                "Selected phase does not contain all100 levels exactly once")
        for level in levels:
            fixture = fixtures[level["level"]]
            matches = {"fixtureGridSize": fixture["size"], "observedInitialPieces": fixture["pieces"],
                       "certifiedProductiveActions": len(fixture["steps"]), "fixtureMinimum": fixture["minimum"],
                       "fixtureAllowance": fixture["base"], "fixtureMargin": fixture["margin"],
                       "savedBudgetUsedAtWin": fixture["minimum"], "starsAfter": 3,
                       "manualGateActions": sum(step["type"] == "OPEN_GATE" for step in fixture["steps"]),
                       "switchPieceExits": sum(step["type"] == "EXIT" and step.get("switch", 0) != 0
                                               for step in fixture["steps"])}
            require(all(type(level.get(key)) is int and level[key] == value for key, value in matches.items()),
                    "Observed board/action budget differs from final fixture: N" + str(level["level"]))
            if is_tail:
                require(all(type(level.get(key)) is int and level[key] == value for key, value
                            in (("mode", 0), ("budgetPolicy", 4), ("contentVersion", 6)))
                        and isinstance(level.get("runId"), str)
                        and level["runId"].strip(), "Tail is not finite-budget Learning Journey with a real attempt identity")
            labels = level.get("winLabels", [])
            require(any("3 Stars" in label for label in labels)
                    and any(label.startswith("Lovely drift!") or label == "Lovely! You did it." for label in labels),
                    "Native victory and three-star labels missing")
    return {"phase": name, "selected": selected, "checks": len(checks), "indexedPngs": len(shots),
            "levels": levels, "shots": shots, "rawChecks": checks}


def validate(args):
    reader = core.Reader()
    reader.bytes(Path(core.__file__), MARATHON_CHECKER)
    prefix_path, tail_path = args.prefix_report.resolve(), args.tail_report.resolve()
    prefix = check_host(reader, prefix_path, 1, 1000, core.SDK, False)
    tail = check_host(reader, tail_path, 801, 1000, TAIL_SDK, True)
    freeze = reader.json(args.source_freeze)
    sources = freeze.get("source_inputs_sha256", {})
    require(freeze.get("version") == "1.14.0-rc2" and freeze.get("version_code") == 16
            and len(sources) == 116 and sources.get(core.LAYOUT_PATH) == core.LAYOUT
            and freeze.get("artifacts", {}).get("lab_apk", {}).get("sha256") == core.APK,
            "Expected unchanged116-input RC2 freeze, Layout0a6 and Lab APKcbc74")
    old_archive, old_inventory, revalidation = check_archive(
        reader, prefix_path, prefix, core.SDK, core.SNAPSHOT, sources)
    new_archive, new_inventory, _ = check_archive(
        reader, tail_path, tail, TAIL_SDK, TAIL_SNAPSHOT, sources)
    require(set(old_inventory) == set(new_inventory), "SDK file inventory changed beyond reviewed source delta")
    changed = sorted(name for name in old_inventory if old_inventory[name] != new_inventory[name])
    require(changed == [core.RUNNER_PATH, "run.py"], "SDK changes extend beyond reviewed setup/wait source files")
    delta = ""
    for name in changed:
        before = reader.bytes(old_archive / "runner-source" / name).decode()
        after = reader.bytes(new_archive / "runner-source" / name).decode()
        delta += "".join(difflib.unified_diff(before.splitlines(True), after.splitlines(True),
                                          fromfile="revision-b/" + name, tofile="board-ready/" + name))
    reader.bytes(new_archive / "revision-b-to-board-ready.diff", TAIL_DIFF)
    require(hashlib.sha256(delta.encode()).hexdigest() == TAIL_DIFF,
            "Native SDK delta is not the exact independently reviewed wait/setup patch")
    fixtures = {}
    for number in range(1, 1001):
        name = f"n{number}.json"
        digest = old_inventory["assets/fixtures/" + name]
        require(new_inventory["assets/fixtures/" + name] == digest
                and revalidation["fixtureSha256"].get(name) == digest, "Fixture bridge mismatch")
        fixture = reader.json(args.fixtures / name, digest)
        require(fixture.get("family") == "N" and fixture.get("number") == number
                and fixture.get("version") == 5 and fixture.get("contentVersion") == 6
                and fixture.get("minimum") == len(fixture["steps"])
                and fixture.get("base") == fixture["minimum"] + fixture["margin"], "Malformed N fixture")
        fixtures[number] = fixture

    prerequisite = check_prerequisite(reader, tail_path, tail)
    require(len(prefix.get("phases", [])) == 9 and len(tail.get("phases", [])) == 2,
            "Require eight complete C batches, its preserved failed ninth, and two tail batches")
    selected, excluded, levels, session_receipts = [], [], [], []
    for path, host, start in ((prefix_path, prefix, 1), (tail_path, tail, 801)):
        union, all_checks, session_batches = {}, [], []
        for index, phase in enumerate(host["phases"]):
            first = start + index * 100
            is_selected = path == tail_path or index < 8
            measured = check_phase(reader, path, host, phase, index, first, fixtures, is_selected)
            for name, digest in measured["shots"].items():
                require(name not in union, "Duplicate phase PNG path within host")
                union[name] = digest
            all_checks.extend(measured["rawChecks"])
            summary = {"host": str(path), "phase": measured["phase"], "from": first,
                       "through": first + 99, "nativeChecks": measured["checks"],
                       "indexedPngs": measured["indexedPngs"], "selectedForCoverage": is_selected}
            session_batches.append(summary)
            if is_selected:
                selected.append(summary)
                levels.extend(measured["levels"])
            else:
                summary["reason"] = "Original C phase failed at N874; all its partial observations are excluded"
                summary["excludedPartialVictories"] = len(measured["levels"])
                excluded.append(summary)
        require(union == host.get("screenshots"), "Host PNG index is not the exact union of its recorded phases")
        if path == tail_path:
            aggregate = host.get("native", {})
            require(aggregate.get("passed") is True and aggregate.get("checks") == all_checks
                    and aggregate.get("checkCount") == len(all_checks), "Tail global/raw native summaries differ")
        session_receipts.append({"hostReport": str(path), "globalPassed": host["passed"],
                                 "restored": True, "sdkSha256": host["test_apk_sha256"],
                                 "indexedPngsVerifiedIncludingExcluded": len(union), "batches": session_batches})
    require([item["from"] for item in selected] == list(range(1, 1001, 100))
            and len(excluded) == 1, "Coverage is not exactly ten unique ordered full phases")
    require([level["level"] for level in levels] == list(range(1, 1001)),
            "Selected native observations must be exactly N1..N1000, each once")
    require(sum(level["savedBudgetUsedAtWin"] for level in levels) == 19920
            and sum(level["certifiedProductiveActions"] for level in levels) == 19920,
            "Wrong exact productive-action coverage total")
    reader.bytes(Path(__file__))
    reader.bytes(Path(core.__file__))
    reader.stable()
    return {"coverageComplete": True, "singleMarathonPassed": False,
            "verifiedAtUtc": datetime.now(timezone.utc).isoformat(),
            "scope": "Independent byte/consistency check of complete puzzle coverage across two distinct sessions",
            "apkSha256": core.APK, "learningLayoutsSha256": core.LAYOUT,
            "frozenAppInputCount": 116, "sessions": session_receipts,
            "prefixRunCRemainsFailed": True, "selectedCompleteBatches": selected,
            "excludedFailedBatches": excluded, "syntheticPrerequisite": prerequisite,
            "zeroProfileSetupClaimed": False, "uninterruptedEarnedProgressionProved": False,
            "uniqueObservedLevels": 1000, "productiveSolutionActions": 19920,
            "recordedNativeThreeStarVictories": 1000,
            "selectedNativeChecks": sum(phase["nativeChecks"] for phase in selected),
            "selectedIndexedPngs": sum(phase["indexedPngs"] for phase in selected),
            "allIndexedPngsVerifiedIncludingExcluded": sum(session["indexedPngsVerifiedIncludingExcluded"]
                                                         for session in session_receipts),
            "sdkDelta": {"changedPaths": changed, "unifiedDiffSha256": TAIL_DIFF,
                         "reviewedScope": "Observed-UI readiness waits plus explicitly authorized unlocked=801 setup"},
            "evidenceSha256": reader.files,
            "limits": ["Checks recorded native assertions and archived bytes, not live device origin authentication.",
                       "C is a failed marathon; N1..800 are natural Journey wins, while N801..1000 use declared synthetic access.",
                       "No partial failed C801..900 result contributes to coverage; no uninterrupted campaign progression is claimed.",
                       "Known-solution UI replay does not measure human difficulty, comprehension, retention or physical-device behavior.",
                       "fixtureGridSize is fixture metadata; native observations separately count pieces and visible victory labels."]}


def check_prerequisite(reader, tail_path, tail):
    reference = tail.get("synthetic_unlock_prerequisite", {})
    require(reference.get("authorized") is True and reference.get("key") == "unlocked"
            and reference.get("value") == 801 and reference.get("before_snapshot_matches_original_host_backup") is True
            and reference.get("proof_file") == "batch-0801-0900/synthetic-unlock-prerequisite.json",
            "Missing explicit original-backed-up unlocked=801 setup reference")
    proof_path = joined(tail_path.parent, reference["proof_file"])
    digest = reference.get("proof_sha256")
    require(digest == TAIL_PREREQUISITE, "Setup proof is not the exact independently reviewed live prerequisite")
    proof = reader.json(proof_path, digest)
    require(proof.get("passed") is True and proof.get("preference_file") == "luma_drift_save_v1.xml"
            and proof.get("key") == "unlocked" and proof.get("after_value") == 801
            and type(proof.get("before_value")) is int and proof["before_value"] < 801
            and proof["before_value"] == reference.get("before_value")
            and type(proof.get("delta_count")) is int and proof["delta_count"] == 1,
            "Wrong synthetic prerequisite receipt")
    before, after = proof["before"], proof["after"]
    before_values, after_values = before["values"], after["values"]
    before_hashes, after_hashes = before["raw_xml_sha256"], after["raw_xml_sha256"]
    require(before_hashes == tail.get("backup_manifest") and before_hashes
            and set(before_hashes) == set(after_hashes) == set(before_values) == set(after_values),
            "Synthetic before-snapshot is not the exact host original backup/file set")
    pref = "luma_drift_save_v1.xml"
    require(pref in before_values, "Missing original Journey preference file")
    previous = before_values[pref].get("unlocked", {"type": "Integer", "value": 1})
    require(previous == {"type": "Integer", "value": proof["before_value"]},
            "Original typed unlocked value differs from receipt")
    expected = copy.deepcopy(before_values)
    expected[pref]["unlocked"] = {"type": "Integer", "value": 801}
    require(after_values == expected and after_values != before_values,
            "Setup changed a typed value beyond the single authorized unlocked preference")
    for name, old_hash in before_hashes.items():
        new_hash = after_hashes[name]
        require(isinstance(old_hash, str) and re.fullmatch(r"[a-f0-9]{64}", old_hash)
                and isinstance(new_hash, str) and re.fullmatch(r"[a-f0-9]{64}", new_hash),
                "Malformed original/synthetic XML hash")
        require((old_hash != new_hash) if name == pref else (old_hash == new_hash),
                "Setup raw XML changes extend beyond the single authorized preference file")
    require(not (tail_path.parent / "batch-0901-1000/synthetic-unlock-prerequisite.json").exists(),
            "Synthetic prerequisite was repeated in the second native batch")
    require("synthetic" in tail.get("scope", "").lower()
            and "no profile injection" not in tail.get("scope", "").lower(), "Contradictory tail host scope")
    return {"declared": True, "proof": str(proof_path), "proofSha256": digest,
            "preferenceFile": pref, "key": "unlocked", "before": proof["before_value"], "after": 801,
            "independentlyCheckedTypedPreferenceDeltaCount": 1,
            "originalHostBackupMatchesBeforeHashes": True, "allOtherPreferenceXmlBytesUnchanged": True,
            "noInjectedStarsCoinsSessionHistoryOrSolutionActionsAtSetup": True,
            "originalGuestRestoredByFinalHost": tail["restored"]}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    for argument in ("prefix_report", "tail_report", "source_freeze", "fixtures"):
        parser.add_argument(argument, type=Path)
    parser.add_argument("--out", type=Path, help="New directory for a coverage-only receipt")
    parser.add_argument("--check-only", action="store_true")
    args = parser.parse_args()
    try:
        result = validate(args)
        if not args.check_only:
            target = (args.out or args.tail_report.parent / "independent-coverage-check").resolve()
            require(not target.exists(), "Coverage receipt already exists; preserve it")
            target.parent.mkdir(parents=True, exist_ok=True)
            with tempfile.TemporaryDirectory(dir=target.parent, prefix=".native-coverage-check-") as temporary:
                directory = Path(temporary) / "receipt"
                directory.mkdir()
                (directory / "coverage.json").write_text(json.dumps(result, indent=2) + "\n")
                (directory / "README.md").write_text(
                    "# RC2 puzzle coverage across two sessions\n\n"
                    "This is full puzzle coverage, **not a successful single marathon**. C remains failed and restored. "
                    "Its eight complete passed batches cover N1–800. N801–1000 were tested separately after an explicitly synthetic "
                    "`unlocked=801` prerequisite on a backed-up profile, then original guest data were restored. "
                    "No stars, coins, session or solution actions were injected by that prerequisite.\n\n"
                    "The failed C801–900 phase contributes no coverage. The selected ten complete batches cover each N level once, "
                    "19,920 productive solution actions and 1,000 recorded native three-star victories. "
                    "The exact app, source freeze, corpus, each SDK revision and every indexed PNG are bound in [coverage.json](coverage.json).\n\n"
                    "This verifies recorded evidence consistency and bytes. It does not prove uninterrupted earned progression, "
                    "zero profile setup, human difficulty or physical-device behavior.\n")
                directory.rename(target)
            print("COVERAGE ACCEPTED across two sessions; C remains FAIL; receipt: " + str(target))
        else:
            print("COVERAGE ACCEPTED across two sessions; C remains FAIL; check-only")
        return 0
    except (ValueError, KeyError, TypeError, OSError) as error:
        print("COVERAGE REFUSED: " + str(error), file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
