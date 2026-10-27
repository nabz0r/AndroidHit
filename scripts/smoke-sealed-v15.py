#!/usr/bin/env python3
"""Real production 1.5 sealed-challenge UI regression on an explicit emulator.

Never installs, resets, seeds, edits preferences, buys items, opens an ad or
contacts another person. P challenges are entered with Android VIEW intents.
Private economy/meta preferences are read only, through run-as or emulator su,
to assert that all six powers, coins, XP and earned progress remain unchanged.
The interrupted board is deliberately replaced by these prototype challenges.
Precondition: production candidate installed and English selected.

--check-fixtures compiles/query-checks the Java source oracle only: no adb calls.
"""
import argparse
from collections import Counter
import hashlib
import importlib.util
import json
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import time
import traceback
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SPEC = importlib.util.spec_from_file_location("sealed_v15_base", ROOT / "scripts/smoke-v11.py")
BASE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(BASE)
PACKAGE = "com.androidhit.lumadrift"
BASE.PACKAGE = PACKAGE
PIECE = re.compile(r"Piece, row (\d+), column (\d+), arrow (up|right|down|left),")
WALL = re.compile(r"^Wall · release key (\d+)$")
VICTORY = re.compile(r"Lovely drift!,\s*(\d+) Stars,\s*Reward:\s*(\d+) Coins,\s*\+(\d+) XP")
POWERS = ("hint", "vision", "flow", "shield", "focus", "trail")


class SealedSmoke(BASE.V11Smoke):
    def __init__(self, serial, adb=None):
        super().__init__(serial, adb, ROOT / "docs/validation/v1.5/sealed-ui.txt")
        self.shots = ROOT / "docs/screenshots/v1.5"
        self.read_method = None

    def fixture(self, family, number):
        raw = subprocess.check_output([str(self.java / "java"), "-cp", self.classes,
                                       "V15Fixture", family, str(number)], timeout=20)
        return json.loads(raw)

    def nodes(self, all_packages=False):
        remote = "/sdcard/luma-sealed-v15.xml"
        self.shell("uiautomator", "dump", remote)
        found = list(ET.fromstring(self.shell("cat", remote)).iter("node"))
        return found if all_packages else [node for node in found if node.get("package") == PACKAGE]

    def positions(self, nodes=None):
        result = {}
        for node in self.nodes() if nodes is None else nodes:
            match = PIECE.search(BASE.text(node))  # Keys prefix their ordinary Piece description.
            if match:
                cell = (int(match.group(1)) - 1, int(match.group(2)) - 1)
                if cell in result:
                    raise AssertionError("Duplicate virtual piece " + repr(cell))
                result[cell] = node
        return result

    def verify(self, fixture, removed=(), nodes=None):
        nodes = self.nodes() if nodes is None else nodes
        actual = self.positions(nodes)
        expected = {(tile["row"], tile["col"]): tile for tile in fixture["tiles"]
                    if tile["id"] not in removed}
        if set(actual) != set(expected):
            raise AssertionError("Source/UI piece mismatch: " + repr((sorted(expected), sorted(actual))))
        directions = ("up", "right", "down", "left")
        for cell, tile in expected.items():
            label = BASE.text(actual[cell])
            if "arrow " + directions[tile["direction"]] + "," not in label:
                raise AssertionError("Wrong direction: " + label)
            if tile["keyNumber"] and not label.startswith("Key " + str(tile["keyNumber"]) + ","):
                raise AssertionError("Missing matching key badge/label: " + label)
        walls = Counter(int(match.group(1)) for node in nodes
                        if (match := WALL.fullmatch(BASE.text(node))))
        expected_walls = Counter(wall["keyNumber"] for wall in fixture["barriers"]
                                 if wall["key"] not in removed)
        if walls != expected_walls:
            raise AssertionError("Wrong active fixed-cell labels: " + repr((expected_walls, walls)))
        if any(re.match(r"^(Spark|Vision|Flow|Shield|Focus|Trail)(?:[:,]|$)", BASE.text(node))
               for node in nodes if node.get("clickable") == "true"):
            raise AssertionError("Prototype must expose no consumable powers")
        return actual

    def click_pattern(self, pattern, nodes=None):
        nodes = self.nodes() if nodes is None else nodes
        found = [node for node in nodes if node.get("clickable") == "true"
                 and re.fullmatch(pattern, BASE.text(node))]
        if len(found) != 1:
            raise AssertionError("Expected one " + pattern + " control; labels=" + repr([BASE.text(n) for n in nodes]))
        self.tap(found[0])

    def open_fixture(self, fixture):
        self.shell("am", "start", "-a", "android.intent.action.VIEW", "-d",
                   "lumadrift://challenge/" + fixture["code"], "-p", PACKAGE)
        time.sleep(0.35)
        return self.verify(fixture)

    def screenshot(self, name):
        self.shots.mkdir(parents=True, exist_ok=True)
        target = self.shots / (name + ".png")
        target.write_bytes(self.command("exec-out", "screencap", "-p"))
        self.log("Screenshot: " + str(target.relative_to(ROOT)))

    def read_preferences(self, name):
        if name not in ("luma_drift_save_v1", "luma_drift_meta_v1"):
            raise AssertionError("Unrelated preference read refused")
        relative = "shared_prefs/" + name + ".xml"
        methods = (self.read_method,) if self.read_method else ("run-as", "su")
        for method in methods:
            command = ("exec-out", "run-as", PACKAGE, "cat", relative) if method == "run-as" else (
                "exec-out", "su", "0", "cat", "/data/user/0/" + PACKAGE + "/" + relative)
            try:
                raw = self.command(*command)
                root = ET.fromstring(raw)
                values = {}
                for node in root:
                    value = node.text or "" if node.tag == "string" else node.get("value")
                    if node.tag in ("int", "long"):
                        value = int(value)
                    elif node.tag == "boolean":
                        value = value == "true"
                    values[node.get("name")] = value
                self.read_method = method
                return values
            except (subprocess.CalledProcessError, ET.ParseError):
                continue
        raise AssertionError("Read-only private-state verification unavailable; no preference editing attempted")

    def economy(self):
        game = self.read_preferences("luma_drift_save_v1")
        meta = self.read_preferences("luma_drift_meta_v1")
        return {"coins": game.get("coins", 0), "xp": meta.get("xp", 0),
                "powers": {power: game.get("booster_" + power, 0) for power in POWERS},
                "game": game, "meta": meta}

    def assert_economy(self, baseline):
        current = self.economy()
        for key in ("coins", "xp", "powers", "game", "meta"):
            if current[key] != baseline[key]:
                # Do not dump complete private preferences into a public failure report.
                raise AssertionError("Prototype changed protected state: " + key)

    def first_board_checks(self, fixture, positions):
        blocked = fixture["tiles"][1]
        self.tap(positions[(blocked["row"], blocked["col"])])
        self.verify(fixture)
        self.log("P1 fixed obstacle rejects the crossing piece without removing it: PASS")
        self.screenshot("sealed-wall-before")
        key = fixture["tiles"][0]
        cell = (key["row"], key["col"])
        self.tap(positions[cell])
        self.verify(fixture, {0})
        self.screenshot("sealed-wall-open")
        self.click_pattern(r"(?:↶\s*)?Undo")
        self.verify(fixture)
        self.log("P1 key exit opens fixed cell; Undo restores key and fixed-cell blocker: PASS")
        self.tap(self.positions()[cell])
        self.verify(fixture, {0})
        self.shell("am", "force-stop", PACKAGE)
        self.launch()
        nodes = self.nodes()
        if not self.positions(nodes):
            self.click_pattern(r"(?:Resume|Continue)\s*(?:→)?", nodes)
        self.verify(fixture, {0})
        self.click_pattern(r"(?:↶\s*)?Undo")
        self.verify(fixture)
        self.tap(self.positions()[cell])
        self.verify(fixture, {0})
        self.log("P1 force-stop/resume retains opened seal; Undo after restoration closes it correctly: PASS")
        return {0}

    def complete(self, fixture, removed):
        cached = self.verify(fixture, removed)
        for step in fixture["steps"]:
            if step["id"] not in removed:
                self.tap(cached[(step["row"], step["col"])], delay=0.12)
        time.sleep(0.65)
        nodes = self.nodes()
        results = [match for node in nodes if (match := VICTORY.search(BASE.text(node)))]
        if len(results) != 1:
            raise AssertionError("Missing P" + str(fixture["number"]) + " victory: " + repr([BASE.text(n) for n in nodes]))
        stars, coins, xp = map(int, results[0].groups())
        if stars != 3 or coins != 0 or xp != 0:
            raise AssertionError("Prototype must finish without consumables, for 3 stars and no progression payout")
        self.log("P" + str(fixture["number"]) + " completed through real taps: " + str(len(fixture["tiles"]))
                 + " pieces, " + str(len(fixture["barriers"])) + " fixed cells, 0 coins, 0 XP, no powers: PASS")

    def run(self):
        if self.command("get-state").decode().strip() != "device":
            raise AssertionError("Selected emulator unavailable")
        installed = self.shell("dumpsys", "package", PACKAGE)
        if "versionName=1.5." not in installed:
            raise AssertionError("Installed production 1.5 candidate required; no installation performed")
        for line in installed.splitlines():
            if line.strip().startswith(("versionCode=", "versionName=")):
                self.log("Installed production: " + line.strip())
        local_apk = ROOT / "app/build/outputs/apk/production/release/app-production-release.apk"
        expected_sha256 = hashlib.sha256(local_apk.read_bytes()).hexdigest()
        package_paths = [line.removeprefix("package:").strip() for line in self.shell("pm", "path", PACKAGE).splitlines()
                         if line.startswith("package:")]
        base_paths = [path for path in package_paths if path.endswith("/base.apk")]
        if len(base_paths) != 1:
            raise AssertionError("Expected one installed base APK path")
        actual_sha256 = self.shell("sha256sum", base_paths[0]).split()[0]
        if actual_sha256 != expected_sha256:
            raise AssertionError("Installed APK differs from current local production candidate")
        self.log("Installed APK SHA256 matches current production output: " + actual_sha256)
        self.log("Runtime: API " + self.shell("getprop", "ro.build.version.sdk").strip()
                 + ", ABI " + self.shell("getprop", "ro.product.cpu.abi").strip()
                 + ", page size " + self.shell("getconf", "PAGESIZE").strip() + " bytes")
        self.launch()
        time.sleep(0.25)
        baseline = self.economy()
        self.log("No profile seed/reset. Baseline earned coins=" + str(baseline["coins"])
                 + ", XP=" + str(baseline["xp"]) + ", read-only power stocks=" + json.dumps(baseline["powers"], sort_keys=True))
        for number in range(1, 7):
            fixture = self.fixture("P", number)
            positions = self.open_fixture(fixture)
            removed = self.first_board_checks(fixture, positions) if number == 1 else set()
            self.complete(fixture, removed)
            self.assert_economy(baseline)
        self.log("All six completions leave earned coins, XP, all power stocks, progression and meta records unchanged: PASS")
        crashes = self.shell("logcat", "-d", "-b", "crash")
        if PACKAGE in crashes and "FATAL EXCEPTION" in crashes:
            raise AssertionError("Production package appears in crash buffer; inspect rather than clear previous evidence")
        self.log("No production crash found in existing crash buffer. No ad, purchase, profile seeding or publication performed.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial")
    parser.add_argument("--adb")
    parser.add_argument("--check-fixtures", action="store_true")
    args = parser.parse_args()
    if not args.check_fixtures and not args.serial:
        parser.error("An explicit --serial emulator-NNNN is required")
    smoke = SealedSmoke(args.serial or "emulator-0", args.adb)
    try:
        with tempfile.TemporaryDirectory(prefix="luma-v15-source-oracle-") as directory:
            sources = sorted((ROOT / "app/src/main/java/com/androidhit/lumadrift/engine").glob("*.java"))
            subprocess.run([str(smoke.java / "javac"), "-d", directory,
                            *map(str, sources), str(ROOT / "scripts/V15Fixture.java")], check=True, timeout=60)
            smoke.classes = directory
            if args.check_fixtures:
                for family, number in [("P", n) for n in range(1, 7)] + [("G", 1), ("G", 1000), ("H", 240)]:
                    fixture = smoke.fixture(family, number)
                    assert len(fixture["tiles"]) == len(fixture["steps"])
                    assert fixture["code"].startswith("LD1-" + family + "-")
                print("PASS 9 Java source-oracle fixture exports; all 6 sealed, Journey 1/1000 and Remix 240. No adb call.")
                return 0
            smoke.run()
            return 0
    except Exception as error:
        if args.check_fixtures:
            raise
        smoke.log("FAIL: " + str(error))
        try:
            smoke.screenshot("sealed-ui-failure")
        except Exception as capture_error:
            smoke.log("Failure screenshot unavailable: " + str(capture_error))
        smoke.log(traceback.format_exc())
        return 1
    finally:
        if not args.check_fixtures:
            smoke.report_path.parent.mkdir(parents=True, exist_ok=True)
            smoke.report_path.write_text("\n".join(smoke.messages) + "\n", encoding="utf-8")


if __name__ == "__main__":
    sys.exit(main())
