#!/usr/bin/env python3
"""Exercise the new collection and all ten event boards in the isolated Lab app.

Requires an explicitly selected emulator and the installed English Lab build.
Never installs, clears saves, changes private preferences, selects a physical
phone, enters advertising flows, or opens real payments. It may spend earned
in-game coins on one affordable companion and then tests equip/re-equip.
Existing completed events are replayed honestly; their bonuses must remain +0.
"""
import argparse
import datetime as dt
import importlib.util
import os
from pathlib import Path
import re
import struct
import subprocess
import tempfile
import time
import traceback

SPEC = importlib.util.spec_from_file_location("luma_event_harness", Path(__file__).with_name("smoke-events.py"))
BASE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(BASE)
PACKAGE = "com.androidhit.lumadrift.sandbox"
MAIN = "com.androidhit.lumadrift.MainActivity"
BASE.PACKAGE = PACKAGE
COMPANIONS = [
    ("Nova", 0), ("Miko", 160), ("Orbit", 220), ("Pip", 300), ("Astra", 420), ("Zig", 600),
    ("Lune", 180), ("Fenn", 240), ("Hoot", 280), ("Buzz", 320), ("Tula", 360), ("Jelli", 400),
    ("Vela", 440), ("Nimbi", 480), ("Soli", 520), ("Mochi", 560), ("Nixi", 620),
    ("Prickly", 680), ("Quill", 740), ("Waddles", 800), ("Halo", 860),
    ("Emberling", 920), ("Bambu", 1000), ("Inky", 1100),
]
LOCALES = [
    ("en", "English", "Settings", "Collection", "Events", "Companions", "Daily Drift", "Weekly Voyage", "Close"),
    ("fr", "Français", "Réglages", "Collection", "Événements", "Compagnons", "Virée du jour", "Voyage de la semaine", "Fermer"),
    ("de", "Deutsch", "Einstellungen", "Sammlung", "Events", "Begleiter", "Täglicher Drift", "Wochenreise", "Schließen"),
    ("es", "Español", "Ajustes", "Colección", "Eventos", "Compañeros", "Ruta diaria", "Viaje semanal", "Cerrar"),
    ("pt", "Português", "Definições", "Coleção", "Eventos", "Companheiros", "Jornada diária", "Viagem semanal", "Fechar"),
    ("it", "Italiano", "Impostazioni", "Collezione", "Eventi", "Compagni", "Viaggio del giorno", "Viaggio settimanale", "Chiudi"),
]


class CollectionEventsSmoke(BASE.EventSmoke):
    def __init__(self, serial, report=None):
        super().__init__(serial)
        self.shots = self.root / "docs/screenshots/lab"
        self.report_path = Path(report) if report else self.root / "docs/validation/lab/collection-events-ui-smoke.txt"
        self.width, self.height = 0, 0

    def control(self, text, nodes=None, prefix=False):
        nodes = self.nodes() if nodes is None else nodes
        matches = [n for n in nodes if n.get("clickable") == "true"
                   and (BASE.node_text(n).casefold().startswith(text.casefold()) if prefix
                        else BASE.node_text(n).casefold() == text.casefold())]
        if len(matches) != 1:
            raise AssertionError("Expected one control " + repr(text) + "; found " + str(len(matches))
                                 + " in " + repr([BASE.node_text(n) for n in nodes]))
        return matches[0]

    def screenshot(self, name):
        self.shots.mkdir(parents=True, exist_ok=True)
        raw = self.command("exec-out", "screencap", "-p")
        self.width, self.height = struct.unpack(">II", raw[16:24])
        path = self.shots / (name + ".png")
        path.write_bytes(raw)
        self.log("Screenshot: " + str(path.relative_to(self.root)) + " (" + str(self.width) + "x" + str(self.height) + ")")

    def frame_bounds(self, nodes=None):
        nodes = self.nodes() if nodes is None else nodes
        if not self.width:
            raw = self.command("exec-out", "screencap", "-p")
            self.width, self.height = struct.unpack(">II", raw[16:24])
        for node in nodes:
            if node.get("clickable") != "true":
                continue
            bounds = list(map(int, re.findall(r"-?\d+", node.get("bounds", ""))))
            if len(bounds) != 4:
                raise AssertionError("Missing control bounds")
            l, t, r, b = bounds
            if not (0 <= l < r <= self.width and 0 <= t < b <= self.height):
                raise AssertionError("Control out of frame: " + repr((BASE.node_text(node), bounds)))

    def home(self):
        for _ in range(8):
            nodes = self.nodes()
            labels = [BASE.node_text(n) for n in nodes]
            if "Settings" in labels and "Events" in labels and "Enter a challenge" in labels:
                return nodes
            if "Home" in labels:
                self.click("Home", nodes)
            elif "Leave level" in labels:
                self.click("Leave level", nodes)
            else:
                self.shell("input", "keyevent", "4")
                time.sleep(.18)
        raise AssertionError("English Home screen required; no preference was modified")

    def collection(self):
        self.click("Collection", self.home())
        self.click("Companions")
        seen = set()
        candidate = None
        for page in range(3):
            nodes = self.nodes()
            labels = [BASE.node_text(n) for n in nodes]
            visible = [(name, price) for name, price in COMPANIONS
                       if any(label.startswith(name + ", ") for label in labels)]
            expected = COMPANIONS[page * 8:(page + 1) * 8]
            if visible != expected:
                raise AssertionError("Wrong page contents: " + repr((page, visible, expected)))
            if not any("Page " + str(page + 1) + " / 3" in label for label in labels):
                raise AssertionError("Missing accessible page/counter summary")
            seen.update(name for name, price in visible)
            balance = self.balance(nodes)
            if candidate is None:
                candidate = next(((page, name, price) for name, price in visible if price <= balance
                                  and any(label.startswith(name + ", ") and "◈" in label for label in labels)), None)
            self.frame_bounds(nodes)
            self.screenshot("collection-page-" + str(page + 1))
            # Every page provides a real enlarged preview; cancel must not spend coins.
            name, price = visible[-1]
            self.click(name + ", ", nodes, prefix=True)
            dialog = self.nodes()
            if not any(BASE.node_text(n) == name for n in dialog):
                raise AssertionError("Missing preview title for " + name)
            self.control("Close", dialog)
            if page == 2:
                self.screenshot("companion-preview")
            self.click("Close", dialog)
            if self.balance() != balance:
                raise AssertionError("Preview cancellation debited coins")
            if page < 2:
                self.click("Next")
        if len(seen) != 24:
            raise AssertionError("Not all 24 companions were browsable")
        self.log("Collection: all 24 companion cards across three pages; preview cancellation and frame bounds PASS")
        if candidate is not None:
            page, name, price = candidate
            for _ in range(2 - page):
                self.click("Back")
            before = self.balance()
            self.click(name + ", ", prefix=True)
            self.click("Get for " + str(price) + " coins")
            after = self.balance()
            if after != before - price:
                raise AssertionError("Cosmetic confirmation did not debit the exact listed price")
            self.control(name + ", Equipped")
            self.click(name + ", ", prefix=True)
            self.click("Equipped")
            if self.balance() != after:
                raise AssertionError("Owned companion confirmation debited coins again")
            # Test equipping an already owned companion, not another purchase.
            for _ in range(page):
                self.click("Back")
            self.click("Nova, ", prefix=True)
            self.click("Equip")
            self.control("Nova, Equipped")
            if self.balance() != after:
                raise AssertionError("Equip spent coins")
            self.log("Companion buy/equip: " + name + " exact -" + str(price)
                     + "; duplicate confirmation and Nova re-equip cost 0 PASS")
        else:
            self.log("Companion purchase case: SKIPPED (no unowned companion affordable on this existing save)")
        self.click("Themes")
        nodes = self.nodes()
        for name in ("Lumina", "Lagoon", "Blossom", "Ember", "Midnight", "Aurora"):
            self.control(name + ", ", nodes, prefix=True)
        self.frame_bounds(nodes)
        self.log("Themes: original six cards remain reachable PASS")

    def event(self, kind, period, count, bonus):
        self.click("Daily Drift" if kind == "daily" else "Weekly Voyage")
        nodes = self.nodes()
        labels = [BASE.node_text(n) for n in nodes]
        objective = "Clear all " + str(count) + " stages, then collect your bonus."
        if not any(objective in label and "Resets in " in label and "stages cleared" in label
                   and "First clear:" in label for label in labels):
            raise AssertionError("Missing accessible objective/progress/countdown/reward summary")
        if "Device time · Offline · No ranking" not in labels:
            raise AssertionError("Device-calendar/offline nature is not explicit")
        if "Completion bonus: " + str(bonus) + " Coins" not in labels:
            raise AssertionError("Completion bonus amount is missing")
        for stage in range(count):
            self.control("Stage " + str(stage + 1) + ",", nodes, prefix=True)
        self.frame_bounds(nodes)
        self.screenshot("events-" + kind + "-overview")
        already_claimed = any(label.casefold() == "collected" for label in labels)
        starting = self.balance(nodes)
        self.click("Stage 1,", nodes, prefix=True)
        stage_nodes = self.nodes()
        rewards = 0
        for index in range(count):
            stage_nodes, reward = self.play_stage(kind, period, index, stage_nodes)
            rewards += reward
        before = self.balance(stage_nodes)
        if before != starting + rewards:
            raise AssertionError("Event board rewards do not match wallet delta")
        self.control("Replay stage 1", stage_nodes)
        for index in range(count):
            self.control("Stage " + str(index + 1) + ",", stage_nodes, prefix=True)
        self.frame_bounds(stage_nodes)
        self.click("Collected" if already_claimed else "Collect", stage_nodes)
        final_nodes = self.nodes()
        after = self.balance(final_nodes)
        expected = 0 if already_claimed else bonus
        if after != before + expected:
            raise AssertionError("Wrong completion bonus delta: " + repr((before, after, expected)))
        self.click("Collected", final_nodes)
        if self.balance() != after:
            raise AssertionError("Repeated completion bonus claim debited/credited coins")
        self.screenshot("events-" + kind + "-complete")
        self.log(kind.title() + ": " + str(count) + " actual boards solved; stage rewards +" + str(rewards)
                 + ", completion bonus +" + str(expected) + ", repeated claim +0 PASS")

    def localized_screens(self):
        self.home()
        current_settings = "Settings"
        for code, language, settings, collection, events, companions, daily, weekly, close in LOCALES:
            self.click(current_settings)
            self.click(language)
            self.shell("input", "keyevent", "4")
            time.sleep(.15)
            self.click(collection)
            self.click(companions)
            nodes = self.nodes()
            for name, price in COMPANIONS[:8]:
                self.control(name + ", ", nodes, prefix=True)
            self.frame_bounds(nodes)
            self.screenshot("collection-" + code)
            if code == "fr":
                self.click("Nova, ", nodes, prefix=True)
                self.control(close)
                self.screenshot("companion-preview-fr")
                self.click(close)
            self.click(events)
            for event_name, family in ((daily, "daily"), (weekly, "weekly")):
                self.click(event_name)
                nodes = self.nodes()
                # Goal/progress/timer are exposed through a non-clickable summary node in all languages.
                summaries = [BASE.node_text(n) for n in nodes if n.get("class") == "android.widget.TextView"]
                if not any((" / 3" in label if family == "daily" else " / 7" in label) for label in summaries):
                    raise AssertionError("Missing localized event summary: " + code + "/" + family)
                self.frame_bounds(nodes)
                self.screenshot("events-" + family + "-" + code)
            self.log("Localized collection/daily/weekly and frame bounds: " + code + " PASS")
            # System Back exits the event screen without relying on a translated Home label.
            self.shell("input", "keyevent", "4")
            time.sleep(.15)
            current_settings = settings
        self.click(current_settings)
        self.click("English")
        self.shell("input", "keyevent", "4")
        time.sleep(.15)
        self.home()
        self.log("Six-language UI loop complete; English restored; no purchases or ad settings changed by localization loop")

    def run(self, case, skip_locales=False):
        self.log("Lab collection/event real-UI smoke; emulator=" + self.serial + "; case=" + case)
        if self.command("get-state").decode().strip() != "device":
            raise AssertionError("Emulator not ready")
        self.shell("am", "start", "-n", PACKAGE + "/" + MAIN)
        time.sleep(.4)
        self.home()
        if case in ("all", "collection"):
            self.collection()
        if case in ("all", "events"):
            self.home()
            self.date = self.shell("date", "+%F").strip()
            day = dt.date.fromisoformat(self.date)
            monday = (day - dt.timedelta(days=day.weekday())).isoformat()
            self.log("Actual device date=" + self.date + "; weekly period=" + monday)
            self.java = Path(os.environ.get("JAVA_HOME") or "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home") / "bin"
            sources = sorted((self.root / "app/src/main/java/com/androidhit/lumadrift/engine").glob("*.java"))
            sources.append(self.root / "scripts/EngineFixture.java")
            with tempfile.TemporaryDirectory(prefix="luma-lab-event-fixture-") as out:
                self.classes = out
                subprocess.run([str(self.java / "javac"), "-d", out, *map(str, sources)], check=True,
                               stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=30)
                self.click("Events")
                self.event("daily", self.date, 3, 100)
                self.event("weekly", monday, 7, 250)
            self.check_period()
        if not skip_locales:
            self.localized_screens()
        crashes = self.shell("logcat", "-d", "-b", "crash")
        if "FATAL EXCEPTION" in crashes or "Fatal signal" in crashes:
            raise AssertionError("Crash buffer contains a crash:\n" + crashes)
        self.log("PASS: selected UI scenarios completed; crash buffer clean; no ad/payment flow entered")

    def write_report(self):
        self.report_path.parent.mkdir(parents=True, exist_ok=True)
        self.report_path.write_text("\n".join(self.report) + "\n")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", required=True)
    parser.add_argument("--case", choices=("all", "collection", "events"), default="all")
    parser.add_argument("--report")
    parser.add_argument("--skip-locales", action="store_true", help="Skip the six-language visual loop")
    args = parser.parse_args()
    smoke = CollectionEventsSmoke(args.serial, args.report)
    try:
        smoke.run(args.case, args.skip_locales)
    except Exception:
        smoke.log("FAIL: " + traceback.format_exc())
        smoke.write_report()
        raise
    smoke.write_report()


if __name__ == "__main__":
    main()
