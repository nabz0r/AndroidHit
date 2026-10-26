#!/usr/bin/env python3
"""Complete local events through the visible UI of one explicitly chosen emulator.

Preconditions: the app is already open on its English Home screen; current daily
and weekly completion bonuses are unclaimed. Existing saves are never reset.
This script never installs an APK, clears app data, or selects a physical device.
"""

import argparse
import datetime as dt
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


PACKAGE = "com.androidhit.lumadrift"
BUTTON = "android.widget.Button"
PIECE = re.compile(r"^Piece, row (\d+), column (\d+),")
WALLET = re.compile(r"^Coins: (\d+)$")
VICTORY = re.compile(r"Lovely drift!,\s*(\d+) Stars,\s*Reward:\s*(\d+) Coins")


def stable_seed(text):
    """Exact Java GameStore.stableSeed for these ASCII period identifiers."""
    value = 0xCBF29CE484222325
    for char in text:
        value ^= ord(char)
        value = (value * 0x100000001B3) & 0xFFFFFFFFFFFFFFFF
    return value & 0x7FFFFFFFFFFFFFFF


def node_text(node):
    return node.get("text") or node.get("content-desc") or ""


class EventSmoke:
    def __init__(self, serial):
        if not re.fullmatch(r"emulator-\d+", serial):
            raise ValueError("An explicit emulator-NNNN serial is required; physical devices are excluded.")
        self.serial = serial
        self.root = Path(__file__).resolve().parents[1]
        sdk = Path(os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
                   or Path.home() / "Library/Android/sdk")
        self.adb = str(sdk / "platform-tools/adb")
        self.shots = self.root / "docs/screenshots"
        self.shots.mkdir(parents=True, exist_ok=True)
        self.report = []
        self.date = None
        self.java = None
        self.classes = None

    def command(self, *args):
        return subprocess.check_output(
            [self.adb, "-s", self.serial, *args], stderr=subprocess.STDOUT, timeout=35)

    def shell(self, *args):
        return self.command("shell", *args).decode("utf-8", errors="replace")

    def log(self, message):
        self.report.append(message)
        print(message, flush=True)

    def nodes(self):
        remote = "/sdcard/luma-events-smoke.xml"
        self.shell("uiautomator", "dump", remote)
        raw = self.shell("cat", remote)
        return [node for node in ET.fromstring(raw).iter("node")
                if node.get("package") == PACKAGE]

    def click_node(self, node, delay=0.22):
        bounds = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.get("bounds", ""))
        if bounds is None:
            raise AssertionError("Missing bounds for " + node_text(node))
        left, top, right, bottom = map(int, bounds.groups())
        if right <= left or bottom <= top:
            raise AssertionError("Empty control bounds: " + node_text(node))
        self.shell("input", "tap", str((left + right) // 2), str((top + bottom) // 2))
        time.sleep(delay)

    def control(self, text, nodes=None, prefix=False):
        nodes = self.nodes() if nodes is None else nodes
        matches = [node for node in nodes
                   if node.get("class") == BUTTON and node.get("clickable") == "true"
                   and (node_text(node).startswith(text) if prefix else node_text(node) == text)]
        if len(matches) != 1:
            labels = [node_text(node) for node in nodes]
            raise AssertionError("Expected one control " + repr(text) + "; found "
                                 + str(len(matches)) + " in " + repr(labels))
        return matches[0]

    def click(self, text, nodes=None, prefix=False):
        self.click_node(self.control(text, nodes, prefix))

    def balance(self, nodes=None):
        nodes = self.nodes() if nodes is None else nodes
        matches = [WALLET.fullmatch(node_text(node)) for node in nodes]
        values = [int(match.group(1)) for match in matches if match]
        if len(values) != 1:
            raise AssertionError("Expected one accessible 'Coins: N' wallet: "
                                 + repr([node_text(node) for node in nodes]))
        return values[0]

    def screenshot(self, name):
        path = self.shots / (name + ".png")
        path.write_bytes(self.command("exec-out", "screencap", "-p"))
        self.log("Screenshot: " + str(path.relative_to(self.root)))

    def check_period(self):
        actual = self.shell("date", "+%F").strip()
        if actual != self.date:
            raise AssertionError("Device date changed during the test: " + self.date + " -> " + actual)

    def fixture(self, seed):
        raw = subprocess.check_output(
            [str(self.java / "java"), "-cp", self.classes, "EngineFixture", "event", str(seed)],
            stderr=subprocess.STDOUT, timeout=15)
        return json.loads(raw)

    def play_stage(self, kind, period, index, initial_nodes):
        self.check_period()
        seed = stable_seed(kind + ":" + period + ":" + str(index))
        fixture = self.fixture(seed)
        solution = fixture["solution"]
        nodes = initial_nodes
        # The tutorial may be unseen on an otherwise untouched emulator save.
        tutorial = [node for node in nodes if node_text(node) == "Let’s drift"]
        if tutorial:
            self.click_node(tutorial[0])
            nodes = self.nodes()
        before = self.balance(nodes)
        positions = {}
        for node in nodes:
            if node.get("class") != BUTTON:
                continue
            match = PIECE.match(node_text(node))
            if match:
                key = (int(match.group(1)) - 1, int(match.group(2)) - 1)
                if key in positions:
                    raise AssertionError("Duplicate accessible cell " + repr(key))
                positions[key] = node
        expected = {tuple(cell) for cell in solution}
        if set(positions) != expected:
            raise AssertionError(kind + " stage " + str(index + 1) + " seed/layout mismatch; "
                                 + "expected=" + repr(sorted(expected))
                                 + ", actual=" + repr(sorted(positions)))
        # Cell bounds stay fixed as pieces leave. Cache once, then tap the real UI.
        for cell in solution:
            self.click_node(positions[tuple(cell)], delay=0.14)
        time.sleep(0.65)
        victory_nodes = self.nodes()
        summaries = [VICTORY.search(node_text(node)) for node in victory_nodes]
        summaries = [match for match in summaries if match]
        if len(summaries) != 1:
            raise AssertionError("Missing victory summary for " + kind + " stage " + str(index + 1)
                                 + ": " + repr([node_text(node) for node in victory_nodes]))
        stars, reward = map(int, summaries[0].groups())
        if stars != 3:
            raise AssertionError("Certified unassisted completion should earn 3 stars; got " + str(stars))
        first_completion = 90 if kind == "daily" else 135
        # A partial rerun can replay earlier stages; the displayed reward must agree
        # with the actual wallet change and never exceed the first-completion award.
        if not 0 <= reward <= first_completion:
            raise AssertionError("Unexpected stage reward: " + str(reward))
        self.click("Next", victory_nodes, prefix=True)
        after_nodes = self.nodes()
        after = self.balance(after_nodes)
        if after != before + reward:
            raise AssertionError("Victory wallet mismatch: " + repr((before, reward, after)))
        self.log(kind.title() + " stage " + str(index + 1) + ": PASS; seed=" + str(seed)
                 + ", pieces=" + str(len(solution)) + ", stars=" + str(stars)
                 + ", reward=" + str(reward) + ", wallet=" + str(before) + "->" + str(after))
        return after_nodes, reward

    def event(self, kind, period, count, bonus):
        nodes = self.nodes()
        self.click("Daily Drift" if kind == "daily" else "Weekly Voyage", nodes)
        nodes = self.nodes()
        if any(node_text(node) == "Collected" for node in nodes):
            raise AssertionError(kind.title() + " bonus is already collected; use an unclaimed period. "
                                 + "This script never clears existing saves.")
        starting = self.balance(nodes)
        self.click("Stage 1", nodes)
        stage_nodes = self.nodes()
        rewards = 0
        for index in range(count):
            stage_nodes, reward = self.play_stage(kind, period, index, stage_nodes)
            rewards += reward
        menu_nodes = stage_nodes
        # Last Next must return to Events, where all stages remain accessible.
        for index in range(count):
            self.control("Stage " + str(index + 1), menu_nodes)
        before = self.balance(menu_nodes)
        if before != starting + rewards:
            raise AssertionError("Event stage wallet total does not match displayed rewards")
        collect = self.control("Collect  +", menu_nodes, prefix=True)
        if "+" + str(bonus) not in node_text(collect):
            raise AssertionError("Wrong completion bonus control: " + node_text(collect))
        self.click_node(collect)
        collected_nodes = self.nodes()
        after = self.balance(collected_nodes)
        if after != before + bonus:
            raise AssertionError("Completion bonus must credit exactly " + str(bonus)
                                 + ": " + repr((before, after)))
        self.click("Collected", collected_nodes)
        final_nodes = self.nodes()
        final = self.balance(final_nodes)
        if final != after:
            raise AssertionError("Repeated completion claim changed the wallet: " + repr((after, final)))
        self.log(kind.title() + " completion bonus: PASS; exact +" + str(bonus)
                 + ", repeat claim +0, wallet=" + str(before) + "->" + str(after))
        self.screenshot("events-" + kind + "-complete")

    def run(self):
        self.log("Event UI smoke test; serial=" + self.serial)
        state = self.command("get-state").decode().strip()
        if state != "device":
            raise AssertionError("Emulator is not ready: " + state)
        self.date = self.shell("date", "+%F").strip()
        day = dt.date.fromisoformat(self.date)
        monday = (day - dt.timedelta(days=day.weekday())).isoformat()
        self.log("Device local daily period=" + self.date + "; weekly period=" + monday)
        home_nodes = self.nodes()
        self.control("Settings", home_nodes)
        self.control("Events", home_nodes)
        self.log("Precondition: English Home screen confirmed; no install or save reset performed")
        java_home = Path(os.environ.get("JAVA_HOME")
                         or "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home")
        self.java = java_home / "bin"
        sources = sorted((self.root / "app/src/main/java/com/androidhit/lumadrift/engine").glob("*.java"))
        sources.append(self.root / "scripts/EngineFixture.java")
        with tempfile.TemporaryDirectory(prefix="luma-event-fixture-") as out:
            self.classes = out
            subprocess.run([str(self.java / "javac"), "-d", out, *map(str, sources)],
                           check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=30)
            self.click("Events", home_nodes)
            self.event("daily", self.date, 3, 100)
            self.event("weekly", monday, 7, 250)
        self.check_period()
        crashes = self.shell("logcat", "-d", "-b", "crash")
        if "FATAL EXCEPTION" in crashes or "Fatal signal" in crashes:
            raise AssertionError("Crash buffer is not clean:\n" + crashes)
        self.log("Crash buffer: clean (read without clearing existing logs)")
        self.log("PASS: 10 event stages completed via UI; daily +100 and weekly +250 bonuses "
                 "each credited once, repeated claims credited zero")

    def write_report(self):
        path = self.root / "docs/EVENT_SMOKE_RESULTS.txt"
        path.write_text("\n".join(self.report) + "\n", encoding="utf-8")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", required=True, help="Explicit emulator serial")
    args = parser.parse_args()
    smoke = EventSmoke(args.serial)
    status = 0
    try:
        smoke.run()
    except Exception as error:
        status = 1
        smoke.log("FAIL: " + str(error))
        try:
            smoke.screenshot("events-failure")
        except Exception as capture_error:
            smoke.log("Failure screenshot unavailable: " + str(capture_error))
        smoke.log(traceback.format_exc())
    finally:
        smoke.write_report()
    return status


if __name__ == "__main__":
    sys.exit(main())
