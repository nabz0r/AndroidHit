#!/usr/bin/env python3
"""Exercise v1.1 through the real UI on one explicitly selected emulator.

Never installs, resets app data, modifies private preferences or selects a phone.
Precondition: v1.1 installed, English UI. Cases are independently callable; all
starts with the journal off, completes campaign 1..12, and uses earned test coins
for two Flow charges if needed. Sharing/export only opens and cancels the system
chooser: no recipient or external application is selected.
"""
import argparse
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import time
import traceback
import xml.etree.ElementTree as ET
import zlib

PACKAGE = "com.androidhit.lumadrift"
PIECE = re.compile(r"^Piece, row (\d+), column (\d+), arrow (up|right|down|left),")
VICTORY = re.compile(r"Lovely drift!,\s*(\d+) Stars,\s*Reward:\s*(\d+) Coins,\s*\+(\d+) XP")
RANK = re.compile(r"Lightkeeper (\d+), (\d+) XP, (\d+) / 120 XP")
ALL_CASES = ("journal", "campaign", "remix", "missions", "friends", "share", "flow")


def text(node):
    return node.get("text") or node.get("content-desc") or ""


def challenge_code(family, value):
    alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    digits = ""
    n = value
    while n:
        n, digit = divmod(n, 36)
        digits = alphabet[digit] + digits
    body = "LD1-" + family + "-" + (digits or "0")
    return body + "-" + format(zlib.crc32(body.encode("ascii")), "08X")


FIXTURE_SOURCE = r'''
import com.androidhit.lumadrift.engine.*;
public class SmokeV11Fixture {
 public static void main(String[] args) {
  String kind=args[0];long value=Long.parseLong(args[1]);
  Level level=kind.equals("C")?LevelCatalog.get((int)value):kind.equals("R")?LevelCatalog.remix((int)value):LevelCatalog.event(value);
  Board board=new Board(level);
  System.out.print("{\"size\":"+level.size+",\"gates\":"+level.initialGateMask+",\"tiles\":[");
  for(int i=0;i<level.tiles.size();i++){Tile t=level.tile(i);if(i>0)System.out.print(",");System.out.print("{\"id\":"+t.id+",\"row\":"+t.row+",\"col\":"+t.col+",\"direction\":"+t.direction.ordinal()+",\"switch\":"+t.switchMask+"}");}
  System.out.print("],\"steps\":[");
  for(int i=0;i<level.solution.length;i++){int id=level.solution[i];Tile t=level.tile(id);Board.MoveResult move=board.move(id);if(!move.success)throw new AssertionError("Fixture solution");if(i>0)System.out.print(",");System.out.print("{\"id\":"+id+",\"row\":"+t.row+",\"col\":"+t.col+",\"gates\":"+board.gateMask()+"}");}
  System.out.println("]}");
 }
}
'''


class V11Smoke:
    def __init__(self, serial, adb=None, report=None):
        if not re.fullmatch(r"emulator-\d+", serial):
            raise ValueError("Explicit emulator-NNNN serial required; physical devices are excluded")
        self.serial = serial
        self.root = Path(__file__).resolve().parents[1]
        sdk = Path(os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
                   or Path.home() / "Library/Android/sdk")
        self.adb = adb or str(sdk / "platform-tools/adb")
        self.report_path = Path(report) if report else self.root / "docs/validation/v11-ui-smoke.txt"
        self.shots = self.root / "docs/screenshots/v1.1"
        self.messages = []
        self.classes = None
        self.java = Path(os.environ.get("JAVA_HOME")
                         or "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home") / "bin"

    def command(self, *args):
        return subprocess.check_output([self.adb, "-s", self.serial, *args],
                                       stderr=subprocess.STDOUT, timeout=40)

    def shell(self, *args):
        return self.command("shell", *args).decode("utf-8", errors="replace")

    def log(self, message):
        print(message, flush=True)
        self.messages.append(message)

    def nodes(self, all_packages=False):
        remote = "/sdcard/luma-v11-smoke.xml"
        self.shell("uiautomator", "dump", remote)
        nodes = list(ET.fromstring(self.shell("cat", remote)).iter("node"))
        return nodes if all_packages else [n for n in nodes if n.get("package") == PACKAGE]

    def control(self, name, nodes=None, prefix=False, topmost=False):
        nodes = self.nodes() if nodes is None else nodes
        matches = [n for n in nodes if n.get("clickable") == "true"
                   and (text(n).casefold().startswith(name.casefold()) if prefix else text(n).casefold() == name.casefold())]
        if not matches:
            raise AssertionError("Missing control " + repr(name) + " in " + repr([text(n) for n in nodes]))
        if topmost:
            return min(matches, key=lambda n: self.bounds(n)[1])
        if len(matches) != 1:
            raise AssertionError("Ambiguous control " + repr(name))
        return matches[0]

    @staticmethod
    def bounds(node):
        match = re.fullmatch(r"\[(-?\d+),(-?\d+)\]\[(-?\d+),(-?\d+)\]", node.get("bounds", ""))
        if not match:
            raise AssertionError("Bad control bounds: " + text(node))
        left, top, right, bottom = map(int, match.groups())
        if right <= left or bottom <= top:
            raise AssertionError("Empty control bounds: " + text(node))
        return left, top, right, bottom

    def tap(self, node, delay=0.16):
        left, top, right, bottom = self.bounds(node)
        self.shell("input", "tap", str((left + right) // 2), str((top + bottom) // 2))
        time.sleep(delay)

    def click(self, name, nodes=None, prefix=False, topmost=False, delay=0.16):
        self.tap(self.control(name, nodes, prefix, topmost), delay)

    def screenshot(self, name):
        self.shots.mkdir(parents=True, exist_ok=True)
        path = self.shots / ("v11-" + name + ".png")
        path.write_bytes(self.command("exec-out", "screencap", "-p"))
        self.log("Screenshot: " + str(path.relative_to(self.root)))

    def launch(self):
        self.shell("am", "start", "-n", PACKAGE + "/.MainActivity")
        time.sleep(0.35)

    def home(self):
        for _ in range(8):
            nodes = self.nodes()
            labels = [text(n) for n in nodes]
            if "Settings" in labels and "Enter a challenge" in labels:
                return nodes
            if "Let’s drift" in labels:
                self.click("Let’s drift", nodes)
            elif "Leave level" in labels:
                self.click("Leave level", nodes)
            elif any(VICTORY.search(label) for label in labels):
                self.click("Journey", nodes, topmost=True)
            elif "Home" in labels:
                self.click("Home", nodes)
            else:
                self.shell("input", "keyevent", "4")
                time.sleep(0.15)
        raise AssertionError("Could not return to English Home")

    def journey(self, remix=False):
        self.click("Journey", self.home())
        self.click("Remix" if remix else "Journey", topmost=True)
        return self.nodes()

    def start_campaign(self, number):
        nodes = self.journey(False)
        self.click("Level " + str(number), nodes)
        return self.nodes()

    def balance(self, nodes=None):
        values = [int(m.group(1)) for n in (self.nodes() if nodes is None else nodes)
                  if (m := re.fullmatch(r"Coins: (\d+)", text(n)))]
        if len(values) != 1:
            raise AssertionError("Expected one accessible wallet; got " + repr(values))
        return values[0]

    def fixture(self, family, value):
        result = subprocess.check_output([str(self.java / "java"), "-cp", self.classes,
                                          "SmokeV11Fixture", family, str(value)], timeout=20)
        return json.loads(result)

    def positions(self, nodes=None):
        found = {}
        for node in self.nodes() if nodes is None else nodes:
            match = PIECE.match(text(node))
            if match:
                cell = (int(match.group(1)) - 1, int(match.group(2)) - 1)
                if cell in found:
                    raise AssertionError("Duplicate visible piece " + repr(cell))
                found[cell] = node
        return found

    def verify_layout(self, fixture, removed=(), nodes=None):
        actual = self.positions(nodes)
        expected = {(tile["row"], tile["col"]): tile for tile in fixture["tiles"] if tile["id"] not in removed}
        if set(actual) != set(expected):
            raise AssertionError("Fixture/UI layout mismatch: " + repr((sorted(expected), sorted(actual))))
        names = ("up", "right", "down", "left")
        for cell, tile in expected.items():
            label = text(actual[cell])
            if "arrow " + names[tile["direction"]] + "," not in label:
                raise AssertionError("Arrow mismatch: " + label)
            for bit, name in enumerate(names):
                if tile["switch"] & (1 << bit) and name not in label.split("Switch:")[-1]:
                    raise AssertionError("Switch direction missing from accessibility: " + label)
        return actual

    def verify_gates(self, fixture, mask, nodes=None):
        labels = [text(n) for n in (self.nodes() if nodes is None else nodes)]
        used = 0
        for tile in fixture["tiles"]:
            used |= tile["switch"]
        for bit, name in enumerate(("up", "right", "down", "left")):
            if used & (1 << bit):
                expected = ("Gate open" if mask & (1 << bit) else "Gate closed") + " · " + name
                if expected not in labels:
                    raise AssertionError("Expected gate state " + expected + " in " + repr(labels))

    def finish(self, fixture, start=0, cached=None):
        cached = self.positions() if cached is None else cached
        for step in fixture["steps"][start:]:
            cell = (step["row"], step["col"])
            if cell in cached:
                self.tap(cached[cell], delay=0.10)
        time.sleep(0.58)
        nodes = self.nodes()
        matches = [match for n in nodes if (match := VICTORY.search(text(n)))]
        if len(matches) != 1:
            raise AssertionError("Missing complete victory summary: " + repr([text(n) for n in nodes]))
        stars, coins, xp = map(int, matches[0].groups())
        if stars != 3:
            raise AssertionError("Unassisted certified board must earn 3 stars")
        return nodes, coins, xp

    def snapshot(self, nodes=None):
        if nodes is None:
            self.click("Today’s sparks", self.home())
            nodes = self.nodes()
        ranks = [m for n in nodes if (m := RANK.fullmatch(text(n)))]
        if len(ranks) != 1:
            raise AssertionError("Expected accessible total XP and rank")
        rank, xp, progress = map(int, ranks[0].groups())
        if rank != 1 + xp // 120 or progress != xp % 120:
            raise AssertionError("Rank/XP inconsistency")
        missions = tuple(sorted(text(n) for n in nodes if re.search(r", \d+ / [136]$", text(n))))
        if len(missions) != 3:
            raise AssertionError("Expected three accessible mission progress labels")
        return {"coins": self.balance(nodes), "xp": xp, "missions": missions}

    def open_code(self, family, value, dialog=False):
        code = challenge_code(family, value)
        if dialog:
            self.click("Enter a challenge", self.home())
            nodes = self.nodes()
            edits = [n for n in nodes if n.get("class") == "android.widget.EditText"]
            if len(edits) != 1:
                raise AssertionError("Missing challenge input")
            self.tap(edits[0])
            self.shell("input", "text", code)
            self.click("Play", self.nodes())
        else:
            self.shell("am", "start", "-a", "android.intent.action.VIEW", "-d",
                       "lumadrift://challenge/" + code, "-p", PACKAGE)
            time.sleep(0.3)
        return code, self.nodes()

    def chooser(self, screenshot):
        time.sleep(0.35)
        activity = self.shell("dumpsys", "activity", "activities")
        if not re.search(r"(?:Chooser|Resolver)Activity", activity):
            raise AssertionError("No Android share chooser")
        self.screenshot(screenshot)
        self.shell("input", "keyevent", "4")
        time.sleep(0.2)
        if not self.nodes():
            raise AssertionError("Game did not resume after cancelling chooser")

    def case_journal(self):
        self.click("Settings", self.home())
        self.click("Play journal")
        nodes = self.nodes()
        self.control("Play journal: Off", nodes)
        before = self.balance(nodes)
        self.click("Play journal: Off", nodes)
        self.control("Play journal: On")
        self.screenshot("journal-enabled")
        self.click("Export journal")
        self.chooser("journal-export-chooser")
        self.click("Clear journal")
        self.click("Yes", self.nodes())
        self.control("Play journal: On")
        if self.balance() != before:
            raise AssertionError("Clearing journal changed the wallet")
        self.click("Export journal")
        if not any(text(n) == "Export journal" for n in self.nodes()):
            raise AssertionError("Empty journal unexpectedly opened a share chooser")
        self.click("Play journal: On")
        self.shell("am", "force-stop", PACKAGE)
        self.launch()
        self.click("Settings", self.home())
        self.click("Play journal")
        self.control("Play journal: Off")
        if self.balance() != before:
            raise AssertionError("Journal opt-out/recreation changed the wallet")
        self.log("PASS journal: default off; opt-in; export chooser only (cancelled); clear; empty export stays local; opt-out persists; coins unchanged")

    def case_campaign(self):
        nodes = self.start_campaign(1)
        for number in range(1, 13):
            fixture = self.fixture("C", number)
            cached = self.verify_layout(fixture, nodes=nodes)
            nodes, coins, xp = self.finish(fixture, cached=cached)
            self.log("PASS campaign " + str(number) + ": real UI completion, coins=" + str(coins) + ", XP=" + str(xp))
            if number < 12:
                self.click("Next", nodes, prefix=True)
                nodes = self.nodes()
        self.click("Journey", nodes, topmost=True)
        self.click("Remix", topmost=True)
        self.control("Remix Level 1")
        self.screenshot("remix-unlocked")
        self.log("PASS Remix unlock: Journey 12 completed through UI; separate Remix Level 1 available")

    def case_remix(self):
        nodes = self.journey(True)
        self.click("Remix Level 1", nodes)
        fixture = self.fixture("R", 1)
        cached = self.verify_layout(fixture)
        self.verify_gates(fixture, fixture["gates"])
        pair_index = next(i for i, s in enumerate(fixture["steps"])
                          if bin(fixture["tiles"][s["id"]]["switch"]).count("1") == 2)
        for step in fixture["steps"][:pair_index + 1]:
            self.tap(cached[(step["row"], step["col"])], delay=0.10)
        removed = [s["id"] for s in fixture["steps"][:pair_index + 1]]
        gate_mask = fixture["steps"][pair_index]["gates"]
        self.verify_layout(fixture, removed)
        self.verify_gates(fixture, gate_mask)
        self.screenshot("paired-switch-play")
        self.shell("am", "force-stop", PACKAGE)
        self.launch()
        self.click("Continue", self.home(), prefix=True)
        self.verify_layout(fixture, removed)
        self.verify_gates(fixture, gate_mask)
        self.click("↶  Undo")
        self.verify_layout(fixture, removed[:-1])
        self.verify_gates(fixture, fixture["steps"][pair_index - 1]["gates"])
        pair_step = fixture["steps"][pair_index]
        self.tap(self.positions()[(pair_step["row"], pair_step["col"])])
        self.verify_gates(fixture, gate_mask)
        nodes, coins, xp = self.finish(fixture, pair_index + 1)
        self.screenshot("remix-victory")
        self.click("Next", nodes, prefix=True)
        self.verify_layout(self.fixture("R", 2))
        self.log("PASS Remix: paired gates change atomically, mode 3 survives process death, undo restores both gates, certified finish and Remix 2; coins=" + str(coins) + ", XP=" + str(xp))

    def case_missions(self):
        self.click("Today’s sparks", self.home())
        nodes = self.nodes()
        before = self.snapshot(nodes)
        claims = [n for n in nodes if re.fullmatch(r"Collect \+\d+ ◈", text(n))]
        claimed = 0
        for original in claims:
            amount = int(re.search(r"\+(\d+)", text(original)).group(1))
            old = self.balance()
            self.tap(original)
            after_nodes = self.nodes()
            if self.balance(after_nodes) != old + amount:
                raise AssertionError("Mission did not credit exactly " + str(amount))
            bounds = original.get("bounds")
            same = [n for n in after_nodes if text(n) == "Collected" and n.get("bounds") == bounds]
            if len(same) != 1:
                raise AssertionError("Claimed mission not marked collected")
            self.tap(same[0])
            if self.balance() != old + amount:
                raise AssertionError("Repeated mission claim farmed coins")
            claimed += 1
        if not claims:
            collected = [n for n in nodes if text(n) == "Collected"]
            if not collected:
                raise AssertionError("No completed mission to validate; complete 3 new boards first")
            self.tap(collected[0])
            if self.balance() != before["coins"]:
                raise AssertionError("Already claimed mission farmed coins")
            self.log("NOTE missions were already claimed; repeat-claim protection verified, fresh credit not rerun")
        after = self.snapshot(self.nodes())
        if after["xp"] != before["xp"] or after["missions"] != before["missions"]:
            raise AssertionError("Claiming changed XP or completion counts")
        self.screenshot("missions")
        self.log("PASS missions: " + str(claimed) + " exact first credits; repeated claims +0; progress/XP unchanged by collection")

    def case_friends(self):
        before = self.snapshot()
        for family, value in (("C", 1), ("R", 7), ("E", 3522558831355441801)):
            fixture = self.fixture(family, value)
            code, nodes = self.open_code(family, value, dialog=family == "C")
            self.verify_layout(fixture, nodes=nodes)
            self.verify_gates(fixture, fixture["gates"], nodes)
            if any(text(n) in ("Spark", "Vision", "Flow") for n in nodes):
                raise AssertionError("Consumable powers exposed in friend mode")
            self.control("Copy code", nodes)
            if self.balance(nodes) != before["coins"]:
                raise AssertionError("Opening a friend changed coins")
            if family == "R":
                cached = self.positions(nodes)
                removed = []
                for step in fixture["steps"][:2]:
                    self.tap(cached[(step["row"], step["col"])])
                    removed.append(step["id"])
                state_before = sorted(self.positions())
                broken = code[:-1] + ("1" if code[-1] == "0" else "0")
                self.shell("am", "start", "-a", "android.intent.action.VIEW", "-d",
                           "lumadrift://challenge/" + broken, "-p", PACKAGE)
                if sorted(self.positions()) != state_before:
                    raise AssertionError("Invalid deep link replaced the active game")
                self.shell("am", "force-stop", PACKAGE)
                self.launch()
                self.click("Continue", self.home(), prefix=True)
                self.verify_layout(fixture, removed)
                self.verify_gates(fixture, fixture["steps"][1]["gates"])
                self.log("PASS friend: invalid code leaves active run intact; mode 4/code/history survive process death")
            nodes, coins, xp = self.finish(fixture)
            if coins != 0 or xp != 0:
                raise AssertionError("Friend completion granted coins/XP: " + repr((coins, xp)))
            if family == "R":
                self.screenshot("friend-victory")
            self.click("Today’s sparks", nodes)
            after = self.snapshot(self.nodes())
            if after != before:
                raise AssertionError("Friend altered wallet/XP/missions: " + repr((before, after)))
            self.log("PASS friend " + family + ": exact same board, no powers, +0 coins/XP/mission credit; code=" + code)
        nodes = self.journey(True)
        self.control("Remix Level 7 Locked", nodes)
        self.log("PASS friend locked Remix 7 does not unlock Remix progression")

    def case_share(self):
        _, nodes = self.open_code("R", 7)
        before = self.balance(nodes)
        expected = sorted(self.positions(nodes))
        self.click("Challenge a friend", nodes)
        self.chooser("share-chooser")
        if sorted(self.positions()) != expected or self.balance() != before:
            raise AssertionError("Cancelled share changed the active board/wallet")
        self.log("PASS share: real image chooser opened and cancelled; no recipient/app selected; game intact")

    def case_flow(self):
        self.home()
        self.click("Shop")
        self.click("60 ◈")  # Two earned-currency Flow charges; never a real purchase.
        nodes = self.start_campaign(13)
        fixture = self.fixture("C", 13)
        self.verify_layout(fixture, nodes=nodes)
        before = len(self.positions(nodes))
        flow = self.control("Flow", nodes)
        self.tap(flow, delay=0.02)
        self.shell("input", "keyevent", "4")
        time.sleep(1.05)
        self.click("Resume")
        after = len(self.positions())
        if before - after > 1:
            raise AssertionError("Flow continued after pausing")
        nodes = self.nodes()
        before = len(self.positions(nodes))
        self.tap(self.control("Flow", nodes), delay=0.01)
        self.tap(self.control("How to play", nodes), delay=0.01)
        self.shell("input", "keyevent", "4")
        time.sleep(1.05)
        after = len(self.positions())
        if before - after > 1:
            raise AssertionError("Flow resumed after help open/close; stale action epoch")
        self.log("PASS Flow: pause and help transitions permanently cancel later scheduled actions (at most the immediate move)")

    def run(self, cases):
        self.log("v1.1 real UI smoke; emulator=" + self.serial + "; cases=" + ",".join(cases))
        if self.command("get-state").decode().strip() != "device":
            raise AssertionError("Emulator is not ready")
        package = self.shell("dumpsys", "package", PACKAGE)
        if "versionName=1.1.0" not in package:
            raise AssertionError("Install v1.1.0 before running; this script never installs")
        self.launch()
        self.control("Settings", self.home())
        sources = sorted((self.root / "app/src/main/java/com/androidhit/lumadrift/engine").glob("*.java"))
        with tempfile.TemporaryDirectory(prefix="luma-v11-fixture-") as temporary:
            self.classes = temporary
            probe = Path(temporary) / "SmokeV11Fixture.java"
            probe.write_text(FIXTURE_SOURCE, encoding="utf-8")
            subprocess.run([str(self.java / "javac"), "-d", temporary, *map(str, sources), str(probe)],
                           check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=30)
            for case in cases:
                getattr(self, "case_" + case)()
        crash = self.shell("logcat", "-d", "-b", "crash")
        if "FATAL EXCEPTION" in crash or "Fatal signal" in crash:
            raise AssertionError("Crash buffer is not clean:\n" + crash)
        self.home()
        self.log("PASS: selected v1.1 UI cases completed; crash buffer clean; emulator save retained")

    def write_report(self):
        self.report_path.parent.mkdir(parents=True, exist_ok=True)
        self.report_path.write_text("\n".join(self.messages) + "\n", encoding="utf-8")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", required=True, help="Explicit emulator-NNNN serial")
    parser.add_argument("--adb", help="ADB binary; defaults to ANDROID_HOME/ANDROID_SDK_ROOT or standard macOS SDK")
    parser.add_argument("--cases", default="all", help="all, or comma-separated " + ",".join(ALL_CASES))
    parser.add_argument("--report", help="Optional report path")
    args = parser.parse_args()
    cases = ALL_CASES if args.cases == "all" else tuple(args.cases.split(","))
    if not cases or any(case not in ALL_CASES for case in cases):
        parser.error("Unknown case; supported: " + ",".join(ALL_CASES))
    smoke = V11Smoke(args.serial, args.adb, args.report)
    try:
        smoke.run(cases)
        return 0
    except Exception as error:
        smoke.log("FAIL: " + str(error))
        try:
            smoke.screenshot("smoke-failure")
        except Exception as capture_error:
            smoke.log("Failure screenshot unavailable: " + str(capture_error))
        smoke.log(traceback.format_exc())
        return 1
    finally:
        smoke.write_report()


if __name__ == "__main__":
    sys.exit(main())
