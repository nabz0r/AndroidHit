#!/usr/bin/env python3
"""Real Lab 1.6 powers UI checks on an explicitly selected emulator.

--seed-profile is mandatory: this temporarily seeds Journey31 access, 10000
synthetic local coins, six charges of every power, and the Moon Seed charm.
The boundary case also grants Journey1000 access; effects grants Journey75 access.
These are not earned progression. Every file in this Lab application's shared_prefs
directory is backed up outside the repository and restored byte for byte in
finally, including files created during the test. The application remains stopped
after restoration. No installation, production package, account login, payment,
advertising, fabricated SDK reward or external recipient is exercised.
Native dialog window animation scale is temporarily0 on the selected QA emulator
and its original value is restored. Canvas animations and effect timing are real.
The optional --cases rotation checks paused/help Flow across real configuration
recreation and restores the original window rotation mode even after failure.
"""
import argparse
import hashlib
import importlib.util
import json
from pathlib import Path
import re
import shlex
import struct
import subprocess
import sys
import tempfile
import time
import traceback
import xml.etree.ElementTree as ET

SPEC = importlib.util.spec_from_file_location("luma_v13_power_harness", Path(__file__).with_name("smoke-move-power-ui.py"))
HARNESS = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(HARNESS)
PACKAGE = HARNESS.PACKAGE
BASE = HARNESS.BASE


class PowersV16Smoke(HARNESS.MovePowerSmoke):
    def __init__(self, serial, adb=None, report=None):
        if serial not in ("emulator-5556", "emulator-5580"):
            raise ValueError("Only the explicitly designated QA emulators5556/5580 are permitted")
        super().__init__(serial, adb, report or str(Path(__file__).resolve().parents[1] / "docs/validation/v1.6/powers-ui-smoke.txt"))
        if PACKAGE != "com.androidhit.lumadrift.sandbox":
            raise AssertionError("Only the isolated Lab package is permitted")
        self.shots = self.root / "docs/screenshots/v1.6/powers"
        self.profile_path = self.report_path.with_name(self.report_path.stem.replace("-ui-smoke", "") + "-profile.json")
        self.profile = {"scope": "SYNTHETIC EMULATOR TEST PROFILE; NOT EARNED PROGRESSION",
                        "package": PACKAGE, "serial": serial, "restored": False,
                        "seed": {"unlocked": 31, "coins": 10000, "power_stock_each": 6,
                                 "owned_and_equipped_charm": "moon_seed"}}
        self.original = {}
        self.backup_ready = False
        self.window_animation_before = None
        self.rotation_before = None

    def screenshot(self, name):
        HARNESS.COMMERCE.CommerceSmoke.screenshot(self, name)

    def click(self, name, nodes=None, prefix=False, topmost=False, delay=.16):
        # Saga/inventory pagination belongs to the scrollable content, above the
        # pinned navigation. Seek the actual accessible arrow before tapping it.
        if name in ("‹", "›"):
            nodes = self.seek_canvas_control(name, nodes)
        super().click(name, nodes, prefix, topmost, delay)

    def seek_canvas_control(self, name, nodes=None):
        nodes = self.nodes() if nodes is None else nodes
        for attempt in range(6):
            if any(n.get("clickable") == "true" and BASE.text(n) == name for n in nodes):
                self.control(name, nodes)
                return nodes
            if attempt == 5:
                raise AssertionError("Canvas control unavailable after scrolling: " + name)
            surface = next((n for n in nodes if BASE.text(n).startswith("Luma Drift ·")), None)
            if surface is None:
                raise AssertionError("Cannot establish scrollable game surface")
            left, top, right, bottom = self.bounds(surface)
            x = (left + right) // 2
            self.shell("input", "swipe", str(x), str(top + (bottom-top)*3//4),
                       str(x), str(top + (bottom-top)//3), "450")
            time.sleep(.15)
            nodes = self.nodes()
        raise AssertionError("Unreachable control search")

    @property
    def preferences_directory(self):
        return "/data/user/0/" + PACKAGE + "/shared_prefs"

    def prefs_path(self, name):
        if not re.fullmatch(r"[A-Za-z0-9_.-]+", name):
            raise AssertionError("Unsafe preference name")
        return self.preferences_directory + "/" + name + ".xml"

    def preference_files(self):
        try:
            names = self.shell("su", "0", "ls", "-1", self.preferences_directory).splitlines()
        except subprocess.CalledProcessError as error:
            if "No such file or directory" in (error.output or b"").decode(errors="replace"):
                return []
            raise
        if any(not re.fullmatch(r"[A-Za-z0-9_.-]+", name) for name in names):
            raise AssertionError("Unexpected preference filename; refusing mutation")
        return names

    def write_exact_file(self, name, raw):
        if not re.fullmatch(r"[A-Za-z0-9_.-]+", name):
            raise AssertionError("Unsafe preference filename")
        path = self.preferences_directory + "/" + name
        command = "su 0 sh -c " + shlex.quote("cat > " + shlex.quote(path))
        subprocess.run([self.adb, "-s", self.serial, "shell", command], input=raw,
                       check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=40)
        self.shell("su", "0", "chown", self.owner, path)
        self.shell("su", "0", "chmod", "660", path)
        self.shell("su", "0", "restorecon", path)

    def fresh_pref(self, name, values):
        root = ET.Element("map")
        for key, value in values.items():
            if isinstance(value, bool):
                ET.SubElement(root, "boolean", name=key, value=str(value).lower())
            elif isinstance(value, int):
                ET.SubElement(root, "int", name=key, value=str(value))
            else:
                ET.SubElement(root, "string", name=key).text = value
        self.write_pref(name, ET.tostring(root, encoding="utf-8", xml_declaration=True))

    def prepare_profile(self):
        if self.command("get-state").decode().strip() != "device":
            raise AssertionError("Selected emulator unavailable")
        details = self.shell("dumpsys", "package", PACKAGE)
        if "versionName=1.6.0-sandbox" not in details:
            raise AssertionError("Requires installed Lab1.6; this test never installs or touches production")
        for line in details.splitlines():
            if line.strip().startswith(("versionCode=", "versionName=")):
                self.log("Installed Lab: " + line.strip())
        if "uid=0(" not in self.shell("su", "0", "id"):
            raise AssertionError("Rooted emulator required for explicitly acknowledged test profile")
        self.shell("am", "force-stop", PACKAGE)
        self.owner = self.shell("su", "0", "stat", "-c", "%u:%g", "/data/user/0/" + PACKAGE).strip()
        if not re.fullmatch(r"\d+:\d+", self.owner):
            raise AssertionError("Cannot establish Lab preference owner")
        self.backup = Path(tempfile.mkdtemp(prefix="luma-v16-powers-backup-"))
        for name in self.preference_files():
            raw = self.command("exec-out", "su", "0", "cat", self.preferences_directory + "/" + name)
            self.original[name] = raw
            (self.backup / name).write_bytes(raw)
        self.backup_ready = True
        self.profile["backup_location"] = str(self.backup)
        checkpoint = self.profile_path
        checkpoint.parent.mkdir(parents=True, exist_ok=True)
        checkpoint.write_text(json.dumps(self.profile, indent=2) + "\n", encoding="utf-8")
        self.log("Recovery backup: " + str(self.backup))
        self.window_animation_before = self.shell("settings", "get", "global", "window_animation_scale").strip()
        self.profile["temporary_window_animation_scale"] = {"before": self.window_animation_before, "during": "0", "restored": False}
        checkpoint.write_text(json.dumps(self.profile, indent=2) + "\n", encoding="utf-8")
        self.shell("settings", "put", "global", "window_animation_scale", "0")
        if self.shell("settings", "get", "global", "window_animation_scale").strip() != "0":
            raise AssertionError("Could not disable native dialog window animations on selected QA emulator")
        self.log("QA emulator only: native window_animation_scale temporarily0; original=" + self.window_animation_before + "; Canvas/game effect durations unchanged.")
        self.shell("su", "0", "mkdir", "-p", self.preferences_directory)
        self.shell("su", "0", "chown", self.owner, self.preferences_directory)
        self.shell("su", "0", "restorecon", self.preferences_directory)
        store = {"initialized": True, "unlocked": 31, "coins": 10000, "language": "en",
                 "sound": False, "music": False, "haptics": False,
                 "avatar_owned_v1_moon_seed": "EARNED_COINS", "avatar_slot_v1_CHARM": "moon_seed"}
        store.update({"booster_" + power: 6 for power in HARNESS.POWERS})
        self.fresh_pref("luma_drift_save_v1", store)
        self.fresh_pref("luma_session", {"active": False})
        self.fresh_pref("luma_drift_meta_v1", {})
        self.fresh_pref("luma_drift_journal_v1", {"enabled": False})
        self.fresh_pref("luma_commerce_privacy_v1", {"age_status": "UNKNOWN", "explicit_ads_opt_in": False})
        self.initial_crashes = self.shell("logcat", "-d", "-b", "crash")
        apk_paths = self.shell("pm", "path", PACKAGE).splitlines()
        if len(apk_paths) == 1 and apk_paths[0].startswith("package:"):
            self.profile["installed_apk_sha256"] = self.shell("su", "0", "sha256sum", apk_paths[0][8:]).split()[0]
        self.log("SYNTHETIC Lab profile: Journey31 access,10000 local coins,6 charges per power,Moon Seed equipped; no progression or purchase claim. All preference files backed up outside repository.")
        self.launch()

    def restore_profile(self):
        if self.backup is None or not self.backup_ready:
            return
        self.shell("am", "force-stop", PACKAGE)
        for name in self.preference_files():
            self.shell("su", "0", "rm", "-f", self.preferences_directory + "/" + name)
        for name, raw in self.original.items():
            self.write_exact_file(name, raw)
        actual = {name: self.command("exec-out", "su", "0", "cat", self.preferences_directory + "/" + name)
                  for name in self.preference_files()}
        self.profile["restored"] = actual == self.original
        self.profile["restoration_sha256"] = {
            name: {"before": hashlib.sha256(raw).hexdigest(), "after": hashlib.sha256(actual.get(name, b"")).hexdigest()}
            for name, raw in self.original.items()}
        if not self.profile["restored"]:
            raise AssertionError("Preference restoration mismatch; retained backup: " + str(self.backup))
        self.log("PASS restoration: all " + str(len(self.original)) + " original Lab preference files restored byte for byte; newly created files removed; Lab remains stopped.")

    def restore_window_animation(self):
        if self.window_animation_before is None:
            return
        if self.window_animation_before == "null":
            self.shell("settings", "delete", "global", "window_animation_scale")
        else:
            self.shell("settings", "put", "global", "window_animation_scale", self.window_animation_before)
        actual = self.shell("settings", "get", "global", "window_animation_scale").strip()
        self.profile["temporary_window_animation_scale"]["restored"] = actual == self.window_animation_before
        if actual != self.window_animation_before:
            raise AssertionError("QA emulator native window animation scale was not restored")
        self.log("PASS restoration: native window_animation_scale restored to " + actual + " on selected QA emulator.")

    def restore_rotation(self):
        if self.rotation_before is None:
            return
        self.shell("wm", "user-rotation", *self.rotation_before.split())
        actual = self.shell("wm", "user-rotation").strip()
        self.profile["temporary_rotation"]["restored"] = actual == self.rotation_before
        if actual != self.rotation_before:
            raise AssertionError("QA emulator rotation mode was not restored")
        self.log("PASS restoration: window rotation restored to " + actual + " on selected QA emulator.")

    def moves(self, nodes=None):
        matches = [match for node in (self.nodes() if nodes is None else nodes)
                   if (match := re.fullmatch(r"(\d+) / (\d+) moves(?:\s+·\s+.+)?", BASE.text(node)))]
        if len(matches) != 1:
            raise AssertionError("Missing/ambiguous accessible move counter")
        return tuple(map(int, matches[0].groups()))

    def stock(self, power):
        return self.pref("luma_drift_save_v1").get("booster_" + power, 0)

    def pref(self, name):
        # SharedPreferences may rename/write the file during an actual onPause.
        # Retry only absent/incomplete XML for at most one second; valid values
        # still face exactly the same state, duration and debit assertions.
        deadline = time.monotonic() + 1
        while True:
            raw = self.raw_pref(name)
            try:
                if raw is None:
                    raise ET.ParseError("transient missing preference file")
                return self.decode_pref(raw)
            except ET.ParseError:
                if time.monotonic() >= deadline:
                    raise AssertionError("Lab preference XML unavailable after bounded retry: " + name)
                time.sleep(.05)

    def stable_pref(self, name):
        deadline = time.monotonic() + 1
        previous = None
        while True:
            value = self.pref(name)
            if value == previous:
                return value
            previous = value
            if time.monotonic() >= deadline:
                raise AssertionError("Lab preference did not stabilize after lifecycle pause: " + name)
            time.sleep(.05)

    def saved_history(self):
        raw = self.pref("luma_session").get("history", "")
        return [] if not raw else [int(value) for value in raw.split(",")]

    def home(self):
        for _ in range(12):
            nodes=self.nodes();labels=[BASE.text(n) for n in nodes]
            if "Settings" in labels:return nodes
            if "Let’s drift" in labels:self.click("Let’s drift",nodes)
            elif "Leave level" in labels:self.click("Leave level",nodes)
            elif any(BASE.VICTORY.search(label) for label in labels):self.click("Journey",nodes,topmost=True)
            elif "Home" in labels:self.click("Home",nodes)
            elif "Pause" in labels:self.click("Pause",nodes)
            else:self.shell("input","keyevent","4")
        raise AssertionError("Cannot reach Home")

    def journey(self,remix=False):
        self.click("Map",self.home())
        self.click("Remix" if remix else "Journey",topmost=True)
        return self.nodes()

    def start_campaign(self,number):
        nodes=self.journey(False)
        for _ in range(100):
            levels=[int(m.group(1)) for n in nodes if (m:=re.match(r"Level (\d+),",BASE.text(n)))]
            if not levels:raise AssertionError("No saga nodes")
            if min(levels)<=number<=max(levels):
                self.click("Level "+str(number)+",",nodes,prefix=True)
                self.control("Play  →")
                self.click("Play  →")
                return self.nodes()
            self.click("‹" if number<min(levels) else "›",nodes)
            nodes=self.nodes()
        raise AssertionError("Cannot reach requested chapter")

    def positions(self,nodes=None):
        found={}
        for node in self.nodes() if nodes is None else nodes:
            match=re.search(r"Piece, row (\d+), column (\d+), arrow",BASE.text(node))
            if match:found[(int(match.group(1))-1,int(match.group(2))-1)]=node
        return found

    def start_fresh(self):
        nodes = self.start_campaign(6)
        fixture = self.fixture("B", 6)
        self.verify_layout(fixture, nodes=nodes)
        if len(fixture["tiles"]) <= 3 or self.saved_history():
            raise AssertionError("Fixture must be a fresh real board with more than3 pieces")
        return nodes, fixture

    def confirm(self, power, interrupt=None):
        self.tap(self.power_control(power), delay=.08)
        nodes = self.nodes()
        if not any("One activation uses exactly 1 charge" in BASE.text(node) for node in nodes):
            raise AssertionError("Power confirmation did not disclose exact one-charge cost")
        button = self.control("Use one charge", nodes)
        before = self.stock(power)
        started = time.monotonic()
        self.tap(button, delay=0)
        if interrupt is not None:
            # Confirm actual completion before the next real input. Native dialog
            # dismissal can outlive the click even when window animation is0.
            deadline = time.monotonic() + 5
            while self.stock(power) != before - 1:
                if time.monotonic() > deadline:
                    raise AssertionError("Confirmed power did not persist its single charge")
                time.sleep(.02)
            time.sleep(.35)
            self.tap(interrupt, delay=0)
            self.log("Confirmation→interruption input duration: %.3fs" % (time.monotonic() - started))

    def wait_flow(self, count=3):
        deadline = time.monotonic() + 8
        while time.monotonic() < deadline:
            state = self.pref("luma_session")
            if state.get("flow_remaining") == 0 and len(self.saved_history()) == count:
                return state
            time.sleep(.15)
        raise AssertionError("Flow did not settle to exact persisted history count " + str(count))

    def continue_saved(self):
        self.shell("am", "force-stop", PACKAGE)
        self.launch()
        self.click("Resume", self.home(), prefix=True)
        return self.nodes()

    def case_hint(self):
        nodes, _ = self.start_fresh()
        hint = self.power_control("hint", nodes)
        before = self.stock("hint")
        self.tap(hint)
        self.control("Use one charge")
        if self.stock("hint") != before:
            raise AssertionError("Opening confirmation spent a charge")
        self.click("Cancel")
        if self.stock("hint") != before:
            raise AssertionError("Cancelling confirmation spent a charge")
        self.confirm("hint")
        self.tap(hint, delay=.05)  # Cached same control, while its8-second effect is active.
        if self.stock("hint") != before - 1:
            raise AssertionError("Active Spark was charged again")
        if any(BASE.text(node) == "Use one charge" for node in self.nodes()):
            raise AssertionError("Active Spark offered a second confirmation")
        self.screenshot("hint-one-charge-active")
        self.continue_saved()
        if self.stock("hint") != before - 1:
            raise AssertionError("Spark charge replayed after process recreation")
        self.log("PASS Spark: opening/cancelling costs0; confirmed effect costs1; active reactivation costs0; recreation costs0.")

    def case_flow(self):
        for transition in ("pause", "help", "recreate"):
            nodes, fixture = self.start_fresh()
            before = self.stock("flow")
            initial = self.moves(nodes)
            interrupt = self.control("Pause" if transition != "help" else "How to play", nodes)
            self.confirm("flow", interrupt)
            # Freeze first, then inspect: hierarchy dumps must not race the430ms step.
            self.control("Resume" if transition != "help" else "Let’s drift")
            frozen = self.pref("luma_session")
            frozen_history = self.saved_history()
            time.sleep(1.1)
            state = self.pref("luma_session")
            exits = len(frozen_history)
            if exits not in (1, 2) or self.saved_history() != frozen_history or state.get("flow_remaining") != 3 - exits or state.get("budget_used") != frozen.get("budget_used"):
                raise AssertionError("Flow did not preserve its observed intermediate state while frozen: " + transition)
            if state.get("budget_used") != exits or self.stock("flow") != before - 1:
                raise AssertionError("Frozen Flow must count each actual exit and only1 charged item")
            self.screenshot("flow-" + transition + "-frozen")
            if transition == "recreate":
                self.continue_saved()
            else:
                self.click("Resume" if transition == "pause" else "Let’s drift")
            state = self.wait_flow()
            if state.get("budget_used") != 3 or self.stock("flow") != before - 1:
                raise AssertionError("Flow resume duplicated/lost stock or action accounting: " + transition)
            if self.moves()[0] != initial[0] - 3 or len(self.positions()) != len(fixture["tiles"]) - 3:
                raise AssertionError("Flow actual UI differs from exact3 exits/actions: " + transition)
            time.sleep(.7)
            if len(self.saved_history()) != 3 or self.stock("flow") != before - 1:
                raise AssertionError("Stale callback added an exit or debit: " + transition)
            self.screenshot("flow-" + transition + "-resumed")
            self.log("PASS Flow " + transition + ": observed " + str(exits) + " initial exits; no exit while frozen; resume exactly" + str(3-exits) + "; total3 actions and1 item; no stale callback.")

    def case_rotation(self):
        self.rotation_before = self.shell("wm", "user-rotation").strip()
        if not re.fullmatch(r"free|lock [0-3]", self.rotation_before):
            raise AssertionError("Cannot establish original emulator rotation mode")
        self.profile["temporary_rotation"] = {"before": self.rotation_before, "during": "lock 1", "restored": False}
        self.profile_path.write_text(json.dumps(self.profile, indent=2) + "\n", encoding="utf-8")
        for transition in ("pause", "help"):
            nodes, fixture = self.start_fresh()
            before = self.stock("flow")
            initial = self.moves(nodes)
            resume_label = "Resume" if transition == "pause" else "Let’s drift"
            interrupt = self.control("Pause" if transition == "pause" else "How to play", nodes)
            self.confirm("flow", interrupt)
            self.control(resume_label)
            frozen = self.pref("luma_session")
            frozen_history = self.saved_history()
            exits = len(frozen_history)
            if exits not in (1, 2) or frozen.get("flow_remaining") != 3-exits or frozen.get("budget_used") != exits:
                raise AssertionError("Rotation test did not freeze a real intermediate Flow: " + transition)
            self.profile["temporary_rotation"]["restored"] = False
            try:
                self.shell("wm", "user-rotation", "lock", "1")
                time.sleep(.6)
                # Compact landscape scrolls; verify the actual preserved overlay
                # via its dismiss action, without dismissing it during inspection.
                self.seek_canvas_control(resume_label)
                image = self.command("exec-out", "screencap", "-p")
                width, height = struct.unpack(">II", image[16:24])
                if width <= height:
                    raise AssertionError("Actual emulator did not rotate to landscape")
                self.screenshot("flow-rotation-" + transition + "-landscape-frozen")
                time.sleep(1.1)
                state = self.pref("luma_session")
                if (self.saved_history() != frozen_history or state.get("flow_remaining") != 3-exits
                        or state.get("budget_used") != exits or self.stock("flow") != before-1):
                    raise AssertionError("Rotation advanced frozen Flow or changed its single debit: " + transition)
            finally:
                self.restore_rotation()
            time.sleep(.5)
            self.seek_canvas_control(resume_label)
            self.click(resume_label)
            state = self.wait_flow()
            if (state.get("budget_used") != 3 or self.stock("flow") != before-1
                    or self.moves()[0] != initial[0]-3 or len(self.positions()) != len(fixture["tiles"])-3):
                raise AssertionError("Rotation resume lost/duplicated actual exits, actions or debit: " + transition)
            time.sleep(.7)
            if len(self.saved_history()) != 3 or self.stock("flow") != before-1:
                raise AssertionError("Stale callback advanced Flow after rotation completion: " + transition)
            self.screenshot("flow-rotation-" + transition + "-resumed")
            self.log("PASS actual rotation with Flow " + transition + ": overlay preserved in landscape and after return; "
                     + str(exits) + " initial exits frozen; explicit resume exactly" + str(3-exits)
                     + "; total3 exits/actions,1 item, no stale callback.")

    def case_shield(self):
        nodes, fixture = self.start_fresh()
        initial = self.moves(nodes)
        before = self.stock("shield")
        self.confirm("shield")
        self.tap(self.power_control("shield"), delay=.05)
        if self.stock("shield") != before - 1:
            raise AssertionError("Active Shield charged again")
        for expected in (2, 1):
            self.tap(self.blocked_control(fixture))
            state = self.pref("luma_session")
            if self.moves() != initial or state.get("shield_charges") != expected or state.get("gear_grace") != 1:
                raise AssertionError("Shield did not precede and preserve the equipped charm")
        self.continue_saved()
        state = self.pref("luma_session")
        if state.get("shield_charges") != 1 or state.get("gear_grace") != 1:
            raise AssertionError("Shield/charm state lost after process recreation")
        self.tap(self.blocked_control(fixture))
        state = self.pref("luma_session")
        if self.moves() != initial or state.get("shield_charges") != 0 or state.get("gear_grace") != 1:
            raise AssertionError("Third Shield block accounting incorrect")
        self.tap(self.blocked_control(fixture))
        if self.moves() != initial or self.pref("luma_session").get("gear_grace") != 0:
            raise AssertionError("Charm did not protect exactly the fourth blocked attempt")
        self.tap(self.blocked_control(fixture))
        if self.moves()[0] != initial[0] - 1 or self.stock("shield") != before - 1:
            raise AssertionError("Fifth block must cost1 action, without another Shield debit")
        self.screenshot("shield-three-plus-charm-one")
        self.log("PASS Shield3 + charm1: four protected blocks; fifth costs1 action; recreation preserves Shield1/charm1; reactivation spends0; Shield stock−1 total.")

    def case_finish(self):
        _, fixture = self.start_fresh()
        for step in fixture["steps"][:-1]:
            self.tap(self.positions()[(step["row"], step["col"])])
        if len(self.positions()) != 1:
            raise AssertionError("Expected exactly1 actual piece before final Flow")
        before = self.stock("flow")
        self.confirm("flow")
        time.sleep(.7)
        nodes = self.nodes()
        if not any(BASE.VICTORY.search(BASE.text(node)) for node in nodes):
            raise AssertionError("Final-piece Flow did not show actual victory")
        if self.stock("flow") != before - 1 or self.pref("luma_session").get("active") is not False:
            raise AssertionError("Final Flow receipt/session not persisted exactly once")
        balance = self.pref("luma_drift_save_v1")["coins"]
        self.shell("am", "force-stop", PACKAGE)
        self.launch()
        self.home()
        if self.stock("flow") != before - 1 or self.pref("luma_drift_save_v1")["coins"] != balance:
            raise AssertionError("Final Flow reward or charge replayed after recreation")
        self.screenshot("flow-final-piece-victory-recreated")
        self.log("PASS final-piece Flow: real victory once, one charge, inactive saved run, no reward/debit replay after recreation.")

    def case_journey(self):
        self.profile["seed"]["final_boundary_unlocked"] = 1000
        self.log("EXPLICIT additional synthetic fixture: unlocked1000 for UI boundary validation; not earned progression.")
        self.change_access(1000)
        nodes = self.journey(False)
        levels = {int(match.group(1)) for node in nodes
                  if (match := re.fullmatch(r"Level (\d+),.*", BASE.text(node)))}
        if levels != set(range(991, 1001)):
            raise AssertionError("Final Journey page must expose exactly991..1000: " + repr(sorted(levels)))
        self.click("›", nodes)
        nodes = self.nodes()
        after = {int(match.group(1)) for node in nodes
                 if (match := re.fullmatch(r"Level (\d+),.*", BASE.text(node)))}
        if after != levels:
            raise AssertionError("Final-page arrow exposed nonexistent level1001")
        self.screenshot("journey-final-zone-991-1000")
        self.click("Level 1000,", nodes,prefix=True)
        self.click("Play  →")
        fixture = self.fixture("B", 1000)
        self.verify_layout(fixture)
        for step in fixture["steps"]:
            self.tap(self.positions()[(step["row"], step["col"])])
        nodes = self.nodes()
        if not any(BASE.VICTORY.search(BASE.text(node)) for node in nodes):
            raise AssertionError("Certified oracle did not reach actual Journey1000 victory")
        state = self.pref("luma_drift_save_v1")
        if state.get("stars_1000") != 3 or state.get("unlocked") != 1000:
            raise AssertionError("Journey1000 victory did not persist/cap its real UI result")
        self.screenshot("journey-1000-certified-victory")
        self.click("Next", nodes, prefix=True)
        levels = {int(match.group(1)) for node in self.nodes()
                  if (match := re.fullmatch(r"Level (\d+),.*", BASE.text(node)))}
        if levels != set(range(991, 1001)) or self.pref("luma_drift_save_v1").get("unlocked") != 1000:
            raise AssertionError("Final victory Next should return to final Journey page, without1001")
        self.log("PASS Journey1000: zone34 shows only991..1000; final arrow capped; certified actual UI solution wins3 stars; victory Next cannot create1001.")

    def gear_card(self):
        self.click("Inventory", self.home())
        self.click("Equipment")
        # The18 fixed items are paginated3/card page; Starfold is catalog index4.
        self.click("›")
        return self.equipment_card("Starfold Visor")

    def equipment_card(self, title):
        # The equipped-slot shortcut exposes title/state too. Full catalog
        # cards also include the third source/effect segment; require that card.
        cards = [n for n in self.nodes() if n.get("clickable") == "true"
                 and BASE.text(n).startswith(title + ", ") and BASE.text(n).count(",") >= 2]
        if len(cards) != 1:
            raise AssertionError("Missing/ambiguous full equipment card: " + title)
        return cards[0]

    def case_keys(self):
        self.profile["seed"]["key_lesson_unlocked"] = 12
        self.change_access(12)
        nodes=self.start_campaign(12)
        fixture=json.loads(subprocess.check_output([str(self.java/"java"),"-cp",self.classes,"V15Fixture","J","12"],text=True))
        self.verify_layout(fixture,nodes=nodes)
        walls=lambda ns: [n for n in ns if BASE.text(n).startswith("Wall · release key ")]
        if not fixture["barriers"] or not walls(nodes):raise AssertionError("Key lesson has no real visible seals")
        key_ids={t["id"] for t in fixture["tiles"] if t["keyNumber"]>0}
        saw_key=False
        for step in fixture["steps"]:
            before=len(walls(self.nodes()))
            self.tap(self.positions()[(step["row"],step["col"])])
            if step["id"] in key_ids and not saw_key:
                after=len(walls(self.nodes()))
                if after>=before:raise AssertionError("Key exit did not open its real seals")
                self.screenshot("keys-12-open")
                self.click("↶  Undo")
                if len(walls(self.nodes()))!=before:raise AssertionError("Undo did not restore key seals")
                self.screenshot("keys-12-undo-restored")
                self.tap(self.positions()[(step["row"],step["col"])])
                if len(walls(self.nodes()))!=after:raise AssertionError("Replayed key exit did not reopen seals")
                saw_key=True
        if not saw_key or not any(BASE.VICTORY.search(BASE.text(n)) for n in self.nodes()):raise AssertionError("Key lesson certified UI completion missing")
        self.screenshot("keys-12-certified-victory")
        self.log("PASS actual Journey12: real wall/key UI; key opens seals, Undo restores, replay reopens; certified completion within fixed budget, no power or continuation.")

    def case_gear(self):
        card = self.gear_card()
        before = self.pref("luma_drift_save_v1")["coins"]
        self.tap(card)
        self.control("600 earned coins")
        self.screenshot("gear-starfold-preview-soft-price")
        self.click("Close")
        state = self.pref("luma_drift_save_v1")
        if state["coins"] != before or state.get("avatar_owned_v1_starfold_visor"):
            raise AssertionError("Preview or Close bought the soft-currency equipment")
        self.tap(self.equipment_card("Starfold Visor"))
        self.click("600 earned coins")
        state = self.pref("luma_drift_save_v1")
        if state["coins"] != before - 600 or state.get("avatar_owned_v1_starfold_visor") != "EARNED_COINS" or state.get("avatar_slot_v1_HEAD") != "starfold_visor":
            raise AssertionError("Confirmed Starfold purchase did not debit exactly600 local coins and equip once")
        self.tap(self.equipment_card("Starfold Visor"))
        if any(BASE.text(node) == "600 earned coins" and node.get("clickable") == "true" for node in self.nodes()):
            raise AssertionError("Owned equipped Starfold still offers another purchase")
        self.click("Close")
        self.shell("am", "force-stop", PACKAGE)
        self.launch()
        card = self.gear_card()
        if "Equipped" not in BASE.text(card):
            raise AssertionError("Purchased Starfold no longer appears equipped after recreation")
        state = self.pref("luma_drift_save_v1")
        if state["coins"] != before - 600 or state.get("avatar_owned_v1_starfold_visor") != "EARNED_COINS" or state.get("avatar_slot_v1_HEAD") != "starfold_visor":
            raise AssertionError("Soft equipment debit/ownership/equipment changed after recreation")
        self.screenshot("gear-starfold-owned-recreated")
        self.log("PASS equipment SOFT purchase: Starfold preview/Close costs0; confirm exactly600 synthetic earnedcoins; one ownership/equippedHEAD; no duplicate offer/debit; recreated intact. No IAP involved.")

    def case_effects(self):
        self.profile["seed"]["effects_unlocked"] = 75
        self.log("EXPLICIT additional synthetic fixture: Journey 75 access for Vision, Focus and Trail; not earned progression.")
        self.change_access(75)
        for power, milliseconds, ids_key in (("vision", 8000, None), ("focus", 10000, "focus_ids"), ("trail", 12000, "trail_ids")):
            _, fixture = self.start_fresh()
            before = self.stock(power)
            wallet = self.pref("luma_drift_save_v1")["coins"]
            self.confirm(power)
            nodes = self.nodes()
            control = self.power_control(power, nodes)
            if BASE.text(control) != HARNESS.POWERS[power] + ": Active":
                raise AssertionError("Confirmed effect has no actual Active label: " + power)
            snapshot = self.pref("luma_session")
            saved_ms = snapshot.get(power + "_ms", 0)
            if not 0 < saved_ms <= milliseconds:
                raise AssertionError("Saved duration outside the documented limit: " + power)
            if ids_key is not None:
                ids = [int(value) for value in snapshot.get(ids_key, "").split(",") if value]
                if not ids or len(set(ids)) != len(ids) or not set(ids).issubset({tile["id"] for tile in fixture["tiles"]}):
                    raise AssertionError("Saved effect targets are empty or invalid: " + power)
                if power == "focus" and not any("Safe move" in BASE.text(node) for node in nodes):
                    raise AssertionError("Focus active targets are not exposed in the actual board UI")
                if power == "trail":
                    steps = {int(match.group(1)) for node in nodes if (match := re.search(r"Step (\d+)", BASE.text(node)))}
                    if steps != set(range(1, min(5, len(fixture["tiles"])) + 1)):
                        raise AssertionError("Trail actual UI does not expose its exact next steps")
            self.tap(control, delay=.02)
            if self.stock(power) != before - 1 or self.pref("luma_drift_save_v1")["coins"] != wallet:
                raise AssertionError("Active effect was charged twice or spent local coins: " + power)
            self.tap(self.control("Pause", nodes), delay=.02)
            self.control("Resume")
            self.screenshot(power + "-active-paused")
            self.shell("input", "keyevent", "3")  # Actual lifecycle pause persists the frozen time.
            persisted = self.stable_pref("luma_session")
            if not 0 < persisted.get(power + "_ms", 0) <= saved_ms:
                raise AssertionError("Paused effect did not preserve a bounded positive duration: " + power)
            restored = self.continue_saved()
            if self.stock(power) != before - 1 or self.pref("luma_drift_save_v1")["coins"] != wallet:
                raise AssertionError("Effect recreation added a charge or spent local coins: " + power)
            restored_control = self.power_control(power, restored)
            if BASE.text(restored_control) != HARNESS.POWERS[power] + ": Active":
                raise AssertionError("Frozen effect did not restore as Active: " + power)
            if ids_key is not None and self.pref("luma_session").get(ids_key) != persisted.get(ids_key):
                raise AssertionError("Effect targets changed during process recreation: " + power)
            self.screenshot(power + "-active-recreated")
            self.log("PASS " + HARNESS.POWERS[power] + ": actual activation, Active UI, bounded saved duration" + (" and nonempty visible targets" if ids_key else "") + "; active reactivation spends 0; pause/HOME/process recreation preserves effect; total stock debit 1 and coin debit 0.")

    def run(self, cases):
        self.log("Lab1.6 real powers UI; emulator=" + self.serial + "; cases=" + ",".join(cases))
        self.prepare_profile()
        with tempfile.TemporaryDirectory(prefix="luma-v16-powers-engine-") as temporary:
            self.classes = temporary
            source = Path(temporary) / "MovePowerFixture.java"
            source.write_text(HARNESS.FIXTURE, encoding="utf-8")
            sources = sorted((self.root / "app/src/main/java/com/androidhit/lumadrift/engine").glob("*.java"))
            self.profile["engine_sources_sha256"] = hashlib.sha256(b"".join(path.read_bytes() for path in sources)).hexdigest()
            subprocess.run([str(self.java / "javac"), "-d", temporary, *map(str, sources), str(source), str(self.root/"scripts/V15Fixture.java")],
                           check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=60)
            for case in cases:
                getattr(self, "case_" + case)()
        final_crashes = self.shell("logcat", "-d", "-b", "crash")
        new_crashes = final_crashes[len(self.initial_crashes):] if final_crashes.startswith(self.initial_crashes) else final_crashes
        if ("FATAL EXCEPTION" in new_crashes or "Fatal signal" in new_crashes) and PACKAGE in new_crashes:
            raise AssertionError("New Lab crash appeared during UI testing")
        self.no_payment_activity()
        self.log("PASS selected actual UI scenarios; no account/payment/advertising flow; no new Lab crash. Restoration follows.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", required=True, choices=("emulator-5556", "emulator-5580"))
    parser.add_argument("--adb")
    parser.add_argument("--report")
    parser.add_argument("--seed-profile", action="store_true")
    parser.add_argument("--cases", default="hint,flow,shield,finish,journey,gear,effects,keys")
    args = parser.parse_args()
    if not args.seed_profile:
        parser.error("--seed-profile is mandatory: all tests temporarily seed a disclosed synthetic Lab profile")
    cases = args.cases.split(",")
    if not cases or any(case not in ("hint", "flow", "shield", "finish", "journey", "gear", "effects", "keys", "rotation") for case in cases):
        parser.error("cases must be hint,flow,shield,finish,journey,gear,effects,keys,rotation")
    smoke = PowersV16Smoke(args.serial, args.adb, args.report)
    result = 0
    try:
        smoke.run(cases)
    except Exception as error:
        result = 1
        smoke.log("FAIL: " + str(error))
        try:
            smoke.screenshot("failure")
        except Exception:
            pass
        smoke.log(traceback.format_exc())
    finally:
        try:
            smoke.restore_profile()
        except Exception as error:
            result = 1
            smoke.log("RESTORATION FAILED: " + repr(error) + "; backup=" + str(smoke.backup))
        try:
            smoke.restore_window_animation()
        except Exception as error:
            result = 1
            smoke.log("WINDOW ANIMATION RESTORATION FAILED: " + repr(error))
        try:
            smoke.restore_rotation()
        except Exception as error:
            result = 1
            smoke.log("WINDOW ROTATION RESTORATION FAILED: " + repr(error))
        evidence = smoke.profile_path
        evidence.parent.mkdir(parents=True, exist_ok=True)
        evidence.write_text(json.dumps(smoke.profile, indent=2) + "\n", encoding="utf-8")
        smoke.write_report()
        if result == 0:
            (smoke.shots / "failure.png").unlink(missing_ok=True)
    return result


if __name__ == "__main__":
    sys.exit(main())
