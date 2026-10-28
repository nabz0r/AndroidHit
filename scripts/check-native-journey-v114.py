#!/usr/bin/env python3
"""Validate completed RC2 marathon evidence; never run Android or replay its engine.

Usage: python3 scripts/check-native-journey-v114.py HOST_REPORT SOURCE_FREEZE FIXTURES
       [--screenshots DIRECTORY] [--out NEW_DIRECTORY] [--check-only]
An incomplete, failed or inconsistent run exits nonzero and writes no attestation.
"""
import argparse
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import re
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]
APK = "cbc74c3214f3c64c888f34b9452be162d3e3e168f1c218d1a4537410ef005ff0"
SDK = "16fc8a4275c1998fd64ab4cd01b39088f59b1f84d924f87b7feef8031ff56784"
OLD_SDK = "6a2f8c634f5acd1a5a7d4a107d8c10058a65e741611299559ac26b3fcd72f83d"
LAYOUT = "0a6de06054a377d37b5b8ab1e2374cdbe694c03983adeb1b5714f86aec54dafc"
SNAPSHOT = "68995928cdf31eaa357cf615e715e20e759f8932857ae8f7ca1e8eda965dde51"
REVALIDATION = "1d24d2de4bee0cd9db922008037f64d21447bd44239ee0b5569d1aa9e8c759ec"
PACKAGE = "com.androidhit.lumadrift.sandbox"
LAYOUT_PATH = "app/src/main/java/com/androidhit/lumadrift/engine/LearningLayouts.java"
RUNNER_PATH = "java/com/androidhit/lumadrift/qa/v114/CandidateUiInstrumentation.java"


def require(condition, reason):
    if not condition:
        raise ValueError(reason)


def joined(base, relative):
    require(isinstance(relative, str) and relative and not Path(relative).is_absolute()
            and ".." not in Path(relative).parts, "Unsafe evidence path: " + str(relative))
    result = (base / relative).resolve()
    require(result.is_relative_to(base.resolve()), "Evidence path escapes its archive")
    return result


class Reader:
    def __init__(self):
        self.files = {}

    def bytes(self, path, expected=None):
        path = path.resolve()
        require(path.is_file(), "Missing final evidence file: " + str(path))
        data = path.read_bytes()
        digest = hashlib.sha256(data).hexdigest()
        require(expected is None or digest == expected, "SHA256 mismatch: " + str(path))
        try:
            label = str(path.relative_to(ROOT))
        except ValueError:
            label = str(path)
        require(label not in self.files or self.files[label] == digest,
                "Evidence changed while being read: " + label)
        self.files[label] = digest
        return data

    def json(self, path, expected=None):
        value = json.loads(self.bytes(path, expected))
        require(isinstance(value, dict), "Expected JSON object: " + str(path))
        return value

    def stable(self):
        for label, digest in self.files.items():
            path = Path(label) if Path(label).is_absolute() else ROOT / label
            require(hashlib.sha256(path.read_bytes()).hexdigest() == digest,
                    "Evidence changed during validation: " + label)


def validate(args):
    reader = Reader()
    host_path = args.host_report.resolve()
    host = reader.json(host_path)
    require(host.get("passed") is True and host.get("restored") is True,
            "Global passed AND restored are required; progress/subsection PASS is insufficient")
    require(host.get("cases") == "journey" and host.get("catalog_family") == "N"
            and host.get("journey_from") == 1 and host.get("journey_through") == 1000
            and host.get("batch_size") == 100, "Expected N1..1000 in ten Journey-only batches")
    require(host.get("package") == PACKAGE, "Wrong target package")
    for key in ("apk_sha256", "expected_apk_sha256", "installed_apk_sha256_after_run"):
        require(host.get(key) == APK, "Wrong final RC2 APK identity: " + key)
    for key in ("test_apk_sha256", "installed_test_apk_sha256"):
        require(host.get(key) == SDK, "Expected marathon SDK16fc, distinct from gallery/core SDK6a2: " + key)
    for key in ("isolated_test_package_removed", "installed_apk_matches_expected_after_run",
                "rotation_settings_restored"):
        require(host.get(key) is True, "Incomplete final cleanup: " + key)
    if "display_size_restored" in host:
        require(host["display_size_restored"] is True, "Display restoration failed")
    require(not any(host.get(key) for key in ("error", "new_native_crash_entries",
                "restore_error", "test_package_restore_error", "final_apk_verification_error",
                "rotation_restore_error", "display_restore_error")), "Failure fields remain in final host report")

    freeze = reader.json(args.source_freeze)
    sources = freeze.get("source_inputs_sha256", {})
    require(freeze.get("version_code") == 16 and freeze.get("version") == "1.14.0-rc2"
            and len(sources) == 116 and sources.get(LAYOUT_PATH) == LAYOUT,
            "Expected final116-input RC2 freeze and N layout0a6")
    require(freeze.get("artifacts", {}).get("lab_apk", {}).get("sha256") == APK,
            "Source freeze does not bind the marathon APK")
    reference = reader.json(host_path.parent / "sdk-source-reference.json")
    require(reference.get("test_apk_sha256") == SDK and reference.get("snapshot_sha256") == SNAPSHOT,
            "Wrong archived marathon SDK reference")
    archive = joined(ROOT, reference["archive"])
    snapshot = reader.json(archive / "source-snapshot.json", SNAPSHOT)
    require(snapshot.get("test_apk_sha256") == SDK, "Archived source snapshot belongs to another SDK")
    sdk_sources = snapshot["source_sha256"]
    for name, digest in sdk_sources.items():
        reader.bytes(joined(archive / "runner-source", name), digest)
    require(host.get("runner_source_sha256") == sdk_sources.get(RUNNER_PATH)
            and host.get("wrapper_source_sha256") == sdk_sources.get("run.py"),
            "Host wrapper/runner source does not match archived marathon revision")
    link = reader.json(archive / "fixture-link.json")
    require(link.get("test_apk_sha256") == SDK and link.get("previous_sdk_sha256") == OLD_SDK
            and link.get("fixture_count") == 1000 and link.get("all_match") is True,
            "Missing exact fixture bridge from gallery SDK to marathon SDK")
    revalidation = reader.json(joined(ROOT, link["independent_revalidation"]), REVALIDATION)
    require(revalidation.get("passed") is True and revalidation.get("regeneratedFixtureCount") == 1000
            and revalidation.get("productiveActionCount") == 19920
            and revalidation.get("allFixtureBytesIdenticalAcrossFinalSourceRegenerationWorkingAssetsAndFrozenSdk") is True
            and revalidation.get("frozenSdk", {}).get("sha256") == OLD_SDK
            and revalidation.get("finalSourceFreeze", {}).get("inputsSha256") == sources,
            "Independent fixture regeneration is not bound to the same final116 sources")

    fixtures = {}
    for number in range(1, 1001):
        name = f"n{number}.json"
        digest = sdk_sources["assets/fixtures/" + name]
        require(revalidation["fixtureSha256"].get(name) == digest, "Fixture bridge mismatch: " + name)
        fixture = reader.json(args.fixtures / name, digest)
        require(fixture.get("family") == "N" and fixture.get("number") == number
                and fixture.get("version") == 5 and fixture.get("contentVersion") == 6
                and len(fixture["steps"]) == fixture.get("minimum")
                and fixture.get("base") == fixture["minimum"] + fixture["margin"],
                "Malformed final N fixture: " + name)
        fixtures[number] = fixture

    phases = host.get("phases", [])
    require(len(phases) == 10, "Exactly ten completed phases are required")
    screenshots = args.screenshots or ROOT / "docs/screenshots/v1.14/ui-bugs" / host_path.parent.name
    global_shots = host.get("screenshots", {})
    require(isinstance(global_shots, dict) and global_shots, "No indexed original PNGs")
    union_shots, observations, raw_checks, previous_pid = {}, [], [], None
    boundaries = host.get("batch_process_boundaries")
    if boundaries is not None:
        require(len(boundaries) == 10, "Incomplete recorded cold batch boundaries")
    for index, phase in enumerate(phases):
        first, last = index * 100 + 1, (index + 1) * 100
        name = f"batch-{first:04d}-{last:04d}"
        require(phase.get("phase") == name and phase.get("journey_from") == first
                and phase.get("journey_through") == last, "Missing, duplicated or reordered batch: " + name)
        raw = reader.json(host_path.parent / name / "report.json")
        require(phase.get("native") == raw and raw.get("passed") is True
                and raw.get("package") == PACKAGE, "Host phase differs from its passed raw native report: " + name)
        checks = raw.get("checks", [])
        require(raw.get("checkCount") == len(checks) and checks
                and all(check.get("pass") is True for check in checks), "Native check failure/count mismatch: " + name)
        raw_checks.extend(checks)
        pid = raw.get("processId")
        if previous_pid is not None and pid is not None:
            require(pid != previous_pid, "Adjacent cold batches share a process: " + name)
        if boundaries is not None:
            boundary = boundaries[index]
            require(boundary.get("batch") == name and boundary.get("force_stop") is True
                    and boundary.get("package_absent_before_restart") is True,
                    "Recorded cold boundary is incomplete: " + name)
            if "restarted_process_id" in boundary:
                require(boundary["restarted_process_id"] == pid, "Cold restarted PID differs from native report")
            if "previous_process_id" in boundary:
                require(boundary["previous_process_id"] == previous_pid, "Cold previous PID differs from preceding phase")
        previous_pid = pid
        captures = raw.get("captures", [])
        require(len(captures) == len(set(captures)), "Duplicate capture names within a batch")
        expected_names = {name + "/" + capture + ".png" for capture in captures}
        phase_shots = phase.get("screenshots", {})
        require(set(phase_shots) == expected_names, "Native/host capture index mismatch: " + name)
        for shot, digest in phase_shots.items():
            require(shot not in union_shots and global_shots.get(shot) == digest, "PNG evidence duplicated or mixed: " + shot)
            require(re.fullmatch(r"[a-f0-9]{64}", digest) is not None, "Malformed PNG SHA256")
            require(reader.bytes(joined(screenshots, shot), digest).startswith(b"\x89PNG\r\n\x1a\n"), "Indexed file is not a PNG")
            union_shots[shot] = digest
        levels = reader.json(host_path.parent / name / "journey-observations.json").get("levels", [])
        require([level.get("level") for level in levels] == list(range(first, last + 1)),
                "Missing, duplicated or reordered observed levels: " + name)
        for level in levels:
            fixture = fixtures[level["level"]]
            matches = {"fixtureGridSize": fixture["size"], "observedInitialPieces": fixture["pieces"],
                       "certifiedProductiveActions": len(fixture["steps"]), "fixtureMinimum": fixture["minimum"],
                       "fixtureAllowance": fixture["base"], "fixtureMargin": fixture["margin"],
                       "savedBudgetUsedAtWin": fixture["minimum"], "starsAfter": 3,
                       "manualGateActions": sum(step["type"] == "OPEN_GATE" for step in fixture["steps"]),
                       "switchPieceExits": sum(step["type"] == "EXIT" and step.get("switch", 0) != 0 for step in fixture["steps"])}
            require(all(type(level.get(key)) is int and level[key] == value for key, value in matches.items()),
                    "Observed board differs from exact final fixture: N" + str(level["level"]))
            labels = level.get("winLabels", [])
            require(any("3 Stars" in label for label in labels)
                    and any(label.startswith("Lovely drift!") or label == "Lovely! You did it." for label in labels),
                    "Visible native victory/three-star labels missing: N" + str(level["level"]))
        observations.extend(levels)
    require(union_shots == global_shots, "Global PNG index is not the exact union of these ten batches")
    require([level["level"] for level in observations] == list(range(1, 1001)), "Global levels are not exactly N1..N1000")
    require(sum(level["savedBudgetUsedAtWin"] for level in observations) == 19920
            and sum(level["certifiedProductiveActions"] for level in observations) == 19920,
            "Wrong total productive action count")
    aggregate = host.get("native", {})
    require(aggregate.get("passed") is True and aggregate.get("checks") == raw_checks
            and aggregate.get("checkCount") == len(raw_checks), "Global native summary differs from its ten raw reports")
    reader.bytes(Path(__file__))
    reader.stable()
    return {"passed": True, "verifiedAtUtc": datetime.now(timezone.utc).isoformat(),
            "scope": "Independent final evidence consistency/hash check; no Android access, new playthrough or engine simulation",
            "hostReport": str(host_path), "apkSha256": APK, "marathonSdkSha256": SDK,
            "galleryCoreSdkSha256KeptDistinct": OLD_SDK, "learningLayoutsSha256": LAYOUT,
            "frozenAppInputCount": 116, "completedBatches": 10, "uniqueObservedLevels": 1000,
            "productiveActions": 19920, "threeStarVictories": 1000, "nativeChecks": len(raw_checks),
            "indexedPngsVerified": len(union_shots), "globalPassedAndRestored": True,
            "cleanupVerifiedFromFinalHostReport": True, "evidenceSha256": reader.files,
            "limits": ["Verifies recorded native assertions, observations and archived bytes; it does not re-query a device or cryptographically authenticate its origin.",
                       "fixtureGridSize is fixture-derived metadata; the native observation independently counts pieces and records victory labels.",
                       "Known-solution UI replay is not human difficulty, comprehension, retention or a physical-device result."]}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    for argument in ("host_report", "source_freeze", "fixtures"):
        parser.add_argument(argument, type=Path)
    parser.add_argument("--screenshots", type=Path)
    parser.add_argument("--out", type=Path, help="New attestation directory; defaults next to the final host report")
    parser.add_argument("--check-only", action="store_true", help="Validate without writing an attestation")
    args = parser.parse_args()
    try:
        result = validate(args)
        if not args.check_only:
            target = (args.out or args.host_report.parent / "independent-marathon-check").resolve()
            require(not target.exists(), "Attestation destination already exists; preserve it and choose a new directory")
            target.parent.mkdir(parents=True, exist_ok=True)
            with tempfile.TemporaryDirectory(dir=target.parent, prefix=".native-journey-check-") as temporary:
                directory = Path(temporary) / "attestation"
                directory.mkdir()
                (directory / "attestation.json").write_text(json.dumps(result, indent=2) + "\n")
                (directory / "README.md").write_text(
                    "# Completed RC2 native Journey evidence\n\n"
                    "Independent checks accept all ten passed batches and exactly N1–N1000: **19,920 productive actions, 1,000 three-star native victories**. "
                    "The global host report records successful guest restoration, test-package removal, rotation cleanup and unchanged final APK.\n\n"
                    f"APK `{APK}`; marathon SDK `{SDK}`; N layout `{LAYOUT}`. "
                    "The 116-input source freeze, archived marathon source and exact 1,000-fixture bridge are bound in [the attestation](attestation.json), "
                    "together with every checked raw JSON/source/PNG SHA256. The gallery/core SDK remains separately identified.\n\n"
                    "Only adjacent recorded cold boundaries are checked; Android may reuse PIDs later. No global PID or image-hash uniqueness is assumed. "
                    "The capture selection is the native report's actual index; no screenshot for every board is invented.\n\n"
                    "This is consistency and byte-integrity verification of recorded evidence, not device-origin authentication, a new playthrough, "
                    "human comprehension, retention or physical-device testing.\n")
                directory.rename(target)
            print("ACCEPTED: complete RC2 marathon; attestation: " + str(target))
        else:
            print("ACCEPTED: complete RC2 marathon; check-only, no attestation written")
        return 0
    except (ValueError, KeyError, TypeError, OSError) as error:
        print("REFUSED: " + str(error), file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
