#!/usr/bin/env python3
"""Open P1/P6, generate real Android share PNGs, cancel the system chooser.

Only the explicit QA emulators5556/5580 and installed production1.5 are allowed.
No installation, profile seeding, login, payment, advertisement or recipient tap.
The current board is replaced by these requested challenges. Earned economy/meta
are read only and must remain unchanged. Generated PNGs are copied from this
app's cache for inspection; exact pixel probes require real walls/key badges.
--check-fixtures compiles the source oracle only and makes no emulator call.
"""
import argparse
import hashlib
import importlib.util
import json
from pathlib import Path
import re
import struct
import subprocess
import sys
import tempfile
import time
import traceback
import zlib

ROOT = Path(__file__).resolve().parents[1]
SPEC = importlib.util.spec_from_file_location("share_sealed_v15", ROOT / "scripts/smoke-sealed-v15.py")
SEALED = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(SEALED)
PACKAGE = SEALED.PACKAGE


def read_png(raw):
    """Decode noninterlaced Android RGB/RGBA PNGs using Python's standard library."""
    if raw[:8] != b"\x89PNG\r\n\x1a\n":
        raise AssertionError("Export is not a PNG")
    compressed = bytearray()
    position = 8
    width = height = channels = None
    while position < len(raw):
        length = struct.unpack_from(">I", raw, position)[0]
        kind = raw[position + 4:position + 8]
        data = raw[position + 8:position + 8 + length]
        if kind == b"IHDR":
            width, height, depth, color, compression, filtering, interlace = struct.unpack(">IIBBBBB", data)
            if depth != 8 or color not in (2, 6) or compression or filtering or interlace:
                raise AssertionError("Unsupported Android PNG encoding")
            channels = 3 if color == 2 else 4
        elif kind == b"IDAT":
            compressed.extend(data)
        elif kind == b"IEND":
            break
        position += length + 12
    if width is None:
        raise AssertionError("PNG has no dimensions")
    decoded = zlib.decompress(compressed)
    stride = width * channels
    if len(decoded) != height * (stride + 1):
        raise AssertionError("PNG row size mismatch")
    rows = []
    previous = bytearray(stride)
    for row_number in range(height):
        offset = row_number * (stride + 1)
        filter_type = decoded[offset]
        row = bytearray(decoded[offset + 1:offset + stride + 1])
        if filter_type not in range(5):
            raise AssertionError("Invalid PNG filter")
        for x in range(stride):
            left = row[x - channels] if x >= channels else 0
            above = previous[x]
            upper_left = previous[x - channels] if x >= channels else 0
            if filter_type == 1:
                row[x] = (row[x] + left) & 255
            elif filter_type == 2:
                row[x] = (row[x] + above) & 255
            elif filter_type == 3:
                row[x] = (row[x] + (left + above) // 2) & 255
            elif filter_type == 4:
                estimate = left + above - upper_left
                distances = (abs(estimate - left), abs(estimate - above), abs(estimate - upper_left))
                predictor = left if distances[0] <= distances[1] and distances[0] <= distances[2] else above if distances[1] <= distances[2] else upper_left
                row[x] = (row[x] + predictor) & 255
        rows.append(row)
        previous = row
    return width, height, lambda x, y: tuple(rows[int(y)][int(x) * channels:int(x) * channels + 3])


class SealedShareSmoke(SEALED.SealedSmoke):
    def __init__(self, serial, adb=None):
        if serial not in ("emulator-5556", "emulator-5580"):
            raise ValueError("Only the designated QA emulators5556/5580 are allowed")
        super().__init__(serial, adb)
        self.report_path = ROOT / "docs/validation/v1.5/sealed-share-ui.txt"
        self.shots = ROOT / "docs/screenshots/v1.5/sharing"
        self.exports = []
        self.key_undo_only = False

    def check_key_undo(self):
        baseline = self.economy()
        fixture = self.fixture("P", 1)
        positions = self.open_fixture(fixture)
        blocked, key = fixture["tiles"][1], fixture["tiles"][0]
        self.tap(positions[(blocked["row"], blocked["col"])])
        self.verify(fixture)
        self.tap(positions[(key["row"], key["col"])])
        self.verify(fixture, {key["id"]})
        self.screenshot("sealed-p1-key-open-final")
        self.click_pattern(r"(?:↶\s*)?Undo")
        self.verify(fixture)
        self.screenshot("sealed-p1-key-undo-final")
        self.assert_economy(baseline)
        self.log("PASS final APK P1 gameplay: fixed wall blocks crossing piece; key exit opens wall; Undo restores key and wall; economy/meta unchanged.")

    def cache_files(self):
        directory = "/data/user/0/" + PACKAGE + "/cache/shared"
        try:
            names = self.shell("su", "0", "ls", "-1", directory).splitlines()
        except subprocess.CalledProcessError as error:
            if "No such file or directory" in error.output.decode(errors="replace"):
                return set()
            raise
        return {name for name in names if re.fullmatch(r"luma_challenge_[0-9]+\.png", name)}

    def exported_card(self, fixture, original_files):
        new_files = self.cache_files() - original_files
        if len(new_files) != 1:
            raise AssertionError("Expected exactly one newly generated challenge PNG")
        name = new_files.pop()
        raw = self.command("exec-out", "su", "0", "cat", "/data/user/0/" + PACKAGE + "/cache/shared/" + name)
        width, height, pixel = read_png(raw)
        if (width, height) != (1080, 1350):
            raise AssertionError("Unexpected share-card dimensions")
        size = 720 / fixture["size"]
        for wall in fixture["barriers"]:
            x = 180 + (wall["col"] + .5) * size
            y = 292 + (wall["row"] + .5) * size
            if pixel(x - .30 * size, y) != (74, 64, 86):
                raise AssertionError("Real exported PNG omits sealed cell K" + str(wall["keyNumber"]))
        keys = [tile for tile in fixture["tiles"] if tile["keyNumber"]]
        for key in keys:
            x = 180 + (key["col"] + .5) * size
            y = 292 + (key["row"] + .5) * size
            if pixel(x - .14 * size, y - .39 * size) != (255, 217, 146):
                raise AssertionError("Real exported PNG omits gold key badge K" + str(key["keyNumber"]))
        self.shots.mkdir(parents=True, exist_ok=True)
        output = self.shots / ("sealed-p" + str(fixture["number"]) + "-card.png")
        output.write_bytes(raw)
        self.exports.append({"code": fixture["code"], "path": str(output.relative_to(ROOT)),
                             "sha256": hashlib.sha256(raw).hexdigest(), "width": width, "height": height,
                             "walls": len(fixture["barriers"]), "keys": len(keys)})
        self.log("Exported card: " + str(output.relative_to(ROOT)) + "; " + str(len(fixture["barriers"]))
                 + " real wall pixel probes and " + str(len(keys)) + " real key-badge probes passed.")

    def run(self):
        if self.command("get-state").decode().strip() != "device":
            raise AssertionError("Selected QA emulator unavailable")
        installed = self.shell("dumpsys", "package", PACKAGE)
        if "versionName=1.5." not in installed:
            raise AssertionError("Installed production1.5 required; no installation performed")
        expected = hashlib.sha256((ROOT / "app/build/outputs/apk/production/release/app-production-release.apk").read_bytes()).hexdigest()
        paths = [line[8:] for line in self.shell("pm", "path", PACKAGE).splitlines() if line.startswith("package:") and line.endswith("/base.apk")]
        if len(paths) != 1 or self.shell("sha256sum", paths[0]).split()[0] != expected:
            raise AssertionError("Installed APK does not match current production output")
        self.log("Installed production APK SHA256: " + expected)
        self.launch()
        if self.key_undo_only:
            self.check_key_undo()
            return
        baseline = self.economy()
        for number in (1, 6):
            fixture = self.fixture("P", number)
            self.open_fixture(fixture)
            before = self.cache_files()
            self.click("Challenge a friend")
            time.sleep(.35)
            foreground = self.shell("dumpsys", "activity", "activities")
            resumed = "\n".join(line for line in foreground.splitlines() if "ResumedActivity" in line)
            if not re.search(r"(?:Chooser|Resolver)Activity", resumed):
                raise AssertionError("Actual Android share chooser is not foreground")
            self.screenshot("sealed-p" + str(number) + "-chooser")
            self.shell("input", "keyevent", "4")  # Cancel only: no destination or app is selected.
            self.verify(fixture)
            self.exported_card(fixture, before)
            self.assert_economy(baseline)
            self.log("PASS P" + str(number) + ": real system chooser opened and cancelled; no recipient selected; board/economy/meta unchanged.")
        report = ROOT / "docs/validation/v1.5/sealed-share-exports.json"
        report.write_text(json.dumps({"apk_sha256": expected, "exports": self.exports}, indent=2) + "\n")
        self.log("PASS both real PNG exports include all initial walls and matching gold key badges. K1/K2 typography still requires visual inspection of the extracted cards.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", choices=("emulator-5556", "emulator-5580"))
    parser.add_argument("--adb")
    parser.add_argument("--check-fixtures", action="store_true")
    parser.add_argument("--key-undo-only", action="store_true", help="Short final-APK P1 key/open/Undo gameplay probe; no share chooser")
    args = parser.parse_args()
    if not args.serial and not args.check_fixtures:
        parser.error("Explicit QA emulator --serial is required")
    smoke = SealedShareSmoke(args.serial or "emulator-5580", args.adb)
    if args.key_undo_only:
        smoke.key_undo_only = True
        smoke.report_path = ROOT / "docs/validation/v1.5/sealed-final-key-undo.txt"
    result = 0
    try:
        with tempfile.TemporaryDirectory(prefix="luma-sealed-share-oracle-") as directory:
            sources = sorted((ROOT / "app/src/main/java/com/androidhit/lumadrift/engine").glob("*.java"))
            subprocess.run([str(smoke.java / "javac"), "-d", directory, *map(str, sources), str(ROOT / "scripts/V15Fixture.java")], check=True, timeout=60)
            smoke.classes = directory
            if args.check_fixtures:
                for number in (1, 6):
                    fixture = smoke.fixture("P", number)
                    assert fixture["barriers"] and any(tile["keyNumber"] for tile in fixture["tiles"])
                print("PASS P1/P6 share fixture exports; no emulator call.")
            else:
                smoke.run()
    except Exception as error:
        if args.check_fixtures:
            raise
        result = 1
        smoke.log("FAIL: " + str(error))
        smoke.log(traceback.format_exc())
    finally:
        if not args.check_fixtures:
            smoke.report_path.parent.mkdir(parents=True, exist_ok=True)
            smoke.write_report()
    return result


if __name__ == "__main__":
    sys.exit(main())
