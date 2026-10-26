#!/usr/bin/env python3
"""Real Lab 1.3 UI checks, with an explicitly acknowledged synthetic profile.

Only emulator-NNNN and the isolated sandbox package are supported. --seed-profile
temporarily gives Journey 75 access, 10000 LOCAL TEST gold and four of each power.
Five synthetic 3-star clears make the free-loot test eligible. This is not
evidence that these levels were played. Original game preferences are
backed up outside the repository and restored even after a failure. No install,
physical phone, real purchase, advertisement Install link or share recipient.
The ads case watches two official Google sample rewarded ads, never fabricated
earned callbacks: one continuation and one +1 Spark. It can fail if ads/UMP are
unavailable; a consent screen alone is never counted as successful advertising.
"""
import argparse
import hashlib
import importlib.util
import json
from pathlib import Path
import re
import shlex
import subprocess
import sys
import tempfile
import time
import traceback
import xml.etree.ElementTree as ET

SPEC = importlib.util.spec_from_file_location("luma_commerce_harness", Path(__file__).with_name("smoke-commerce.py"))
COMMERCE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(COMMERCE)
BASE = COMMERCE.BASE
PACKAGE = COMMERCE.PACKAGE
POWERS = {"hint": "Spark", "vision": "Vision", "flow": "Flow", "shield": "Shield", "focus": "Focus", "trail": "Trail"}
PREF_NAMES = ("luma_drift_save_v1", "luma_session", "luma_drift_meta_v1", "luma_drift_journal_v1", "luma_commerce_privacy_v1", "luma_ad_continuations_v1")
FIXTURE = r'''
import com.androidhit.lumadrift.engine.*;
public final class MovePowerFixture {
 public static void main(String[] a){
  int n=Integer.parseInt(a[1]);Level l=a[0].equals("Q")?BalancedCatalog.remix(n):BalancedCatalog.get(n);Board b=new Board(l);
  MoveBudget budget=MoveBudget.start(a[0].equals("Q")?3:0,l);
  System.out.print("{\"size\":"+l.size+",\"base\":"+budget.base()+",\"gates\":"+l.initialGateMask+",\"tiles\":[");
  for(int i=0;i<l.tiles.size();i++){Tile t=l.tile(i);if(i>0)System.out.print(",");System.out.print("{\"id\":"+t.id+",\"row\":"+t.row+",\"col\":"+t.col+",\"direction\":"+t.direction.ordinal()+",\"switch\":"+t.switchMask+",\"legal\":"+b.canMove(t.id)+"}");}
  System.out.print("],\"steps\":[");for(int i=0;i<l.solution.length;i++){Tile t=l.tile(l.solution[i]);if(i>0)System.out.print(",");System.out.print("{\"id\":"+t.id+",\"row\":"+t.row+",\"col\":"+t.col+"}");}
  System.out.println("]}");
 }
}
'''


class MovePowerSmoke(COMMERCE.CommerceSmoke):
    def __init__(self, serial, adb=None, report=None):
        super().__init__(serial, adb, report or "docs/validation/v1.3/move-power-ui-smoke.txt")
        self.backup = None
        self.original = {}
        self.profile = {"scope": "SYNTHETIC EMULATOR TEST PROFILE; NOT EARNED PROGRESSION", "package": PACKAGE,
                        "serial": serial, "seed": {"unlocked": 75, "coins": 10000, "power_stock_each": 4, "synthetic_3star_journey_levels": [1, 2, 3, 4, 5]}, "restored": False}

    def screenshot(self, name):
        super().screenshot("v13-" + name)

    def game_balance(self, nodes=None):
        return BASE.V11Smoke.balance(self, nodes)

    def prefs_path(self, name):
        if name not in PREF_NAMES:
            raise AssertionError("Refusing an unrelated preference file")
        return "/data/user/0/" + PACKAGE + "/shared_prefs/" + name + ".xml"

    @staticmethod
    def decode_pref(raw):
        result = {}
        if raw is None:
            return result
        for node in ET.fromstring(raw):
            value = node.get("value") if node.tag != "string" else node.text or ""
            if node.tag in ("int", "long"):
                value = int(value)
            elif node.tag == "boolean":
                value = value == "true"
            result[node.get("name")] = value
        return result

    def pref(self, name):
        return self.decode_pref(self.raw_pref(name))

    def write_pref(self, name, raw):
        path = self.prefs_path(name)
        # XML travels through stdin, never through shell interpolation.
        remote = "su 0 sh -c " + shlex.quote("cat > " + shlex.quote(path))
        subprocess.run([self.adb, "-s", self.serial, "shell", remote], input=raw,
                       check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=40)
        self.shell("su", "0", "chown", self.owner, path)
        self.shell("su", "0", "chmod", "660", path)
        self.shell("su", "0", "restorecon", path)
        self.shell("su", "0", "rm", "-f", path + ".bak")

    def edit_pref(self, name, changes):
        raw = self.raw_pref(name)
        root = ET.fromstring(raw) if raw is not None else ET.Element("map")
        for key, value in changes.items():
            for node in list(root):
                if node.get("name") == key:
                    root.remove(node)
            if isinstance(value, bool):
                ET.SubElement(root, "boolean", name=key, value=str(value).lower())
            elif isinstance(value, int):
                ET.SubElement(root, "int", name=key, value=str(value))
            else:
                ET.SubElement(root, "string", name=key).text = value
        self.write_pref(name, ET.tostring(root, encoding="utf-8", xml_declaration=True))

    def change_access(self, level):
        self.shell("am", "force-stop", PACKAGE)
        self.edit_pref("luma_drift_save_v1", {"unlocked": level})
        self.launch()

    def prepare_profile(self):
        if self.command("get-state").decode().strip() != "device":
            raise AssertionError("Explicit emulator unavailable")
        details = self.shell("dumpsys", "package", PACKAGE)
        if "versionName=1.3.0-sandbox" not in details:
            raise AssertionError("Requires the installed Lab 1.3 build; no installation performed")
        for line in details.splitlines():
            if line.strip().startswith(("versionCode=", "versionName=")):
                self.log("Installed sandbox: " + line.strip())
        if "uid=0(" not in self.shell("su", "0", "id"):
            raise AssertionError("Emulator root must be available for acknowledged test seeding")
        self.shell("am", "force-stop", PACKAGE)
        self.owner = self.shell("su", "0", "stat", "-c", "%u:%g", "/data/user/0/" + PACKAGE).strip()
        if not re.fullmatch(r"\d+:\d+", self.owner):
            raise AssertionError("Could not establish sandbox owner")
        self.backup = Path(tempfile.mkdtemp(prefix="luma-v13-emulator-backup-"))
        for name in PREF_NAMES:
            # No test -f call: absent preferences are normal and restored absent.
            try:
                raw = self.command("exec-out", "su", "0", "cat", self.prefs_path(name))
            except subprocess.CalledProcessError:
                raw = None
            self.original[name] = raw
            if raw is not None:
                (self.backup / (name + ".xml")).write_bytes(raw)
        store = {"initialized": True, "coins": 10000, "language": "en", "unlocked": 75}
        store.update({"booster_" + power: 4 for power in POWERS})
        store.update({"stars_" + str(level): 3 for level in range(1, 6)})
        original_store=self.decode_pref(self.original["luma_drift_save_v1"])
        self.profile["original_state_summary"]={"coins":original_store.get("coins"),"unlocked":original_store.get("unlocked"),
            "journey_clears":sum(1 for level in range(1,241) if original_store.get("stars_"+str(level),0)>0)}
        self.edit_pref("luma_drift_save_v1", store)
        self.edit_pref("luma_session", {"active": False})
        self.edit_pref("luma_drift_journal_v1", {"enabled": False, "events": "", "sessions": 0, "active_dates": "", "first_observed": ""})
        self.edit_pref("luma_commerce_privacy_v1", {"age_status": "UNKNOWN", "explicit_ads_opt_in": False})
        self.profile["backup_location"] = str(self.backup)
        self.log("SYNTHETIC PROFILE: Journey75 access,10000 local test gold,4 of each power,5 synthetic 3-star clears for loot eligibility; no claim of earned progression. Original preferences will be restored.")
        self.launch()

    def restore_profile(self):
        if self.backup is None:
            return
        self.shell("am", "force-stop", PACKAGE)
        for name, raw in self.original.items():
            if raw is None:
                self.shell("su", "0", "rm", "-f", self.prefs_path(name), self.prefs_path(name) + ".bak")
            else:
                self.write_pref(name, raw)
        self.profile["restored"] = all(self.raw_pref(name) == raw for name, raw in self.original.items())
        self.profile["restoration_sha256"]={name:{"before":hashlib.sha256(raw).hexdigest() if raw is not None else None,
            "after":hashlib.sha256(self.raw_pref(name)).hexdigest() if self.raw_pref(name) is not None else None} for name,raw in self.original.items()}
        if not self.profile["restored"]:
            raise AssertionError("Original preference restoration mismatch; backup retained at " + str(self.backup))
        self.launch()
        self.home()
        self.log("PASS restoration: original six game-owned sandbox preference files restored byte for byte, English Home; stable application untouched")

    def raw_pref(self, name):
        try:
            return self.command("exec-out", "su", "0", "cat", self.prefs_path(name))
        except subprocess.CalledProcessError:
            return None

    def fixture(self, family="B", number=6):
        raw = subprocess.check_output([str(self.java / "java"), "-cp", self.classes,
                                       "MovePowerFixture", family, str(number)], timeout=30)
        return json.loads(raw)

    def start_campaign(self, number):
        self.click("Journey", self.home())
        self.click("Journey", topmost=True)
        for _ in range(8):
            nodes = self.nodes()
            levels = [int(match.group(1)) for node in nodes
                      if (match := re.fullmatch(r"Level (\d+)(?: Locked)?", BASE.text(node)))]
            if not levels:
                raise AssertionError("No Journey level controls")
            if min(levels) <= number <= max(levels):
                self.click("Level " + str(number), nodes)
                return self.nodes()
            self.click("Back" if number < min(levels) else "Next", nodes)
        raise AssertionError("Cannot navigate to Journey " + str(number))

    def moves(self, nodes=None):
        matches = [match for node in (self.nodes() if nodes is None else nodes)
                   if (match := re.fullmatch(r"(\d+) / (\d+) moves(?:\s+·\s+Shield\s*×\d+)?", BASE.text(node)))]
        if len(matches) != 1:
            raise AssertionError("Missing/ambiguous accessible move budget")
        return tuple(map(int, matches[0].groups()))

    def continue_saved(self):
        self.shell("am", "force-stop", PACKAGE)
        self.launch()
        self.click("Continue", self.home(), prefix=True)
        return self.nodes()

    def power_control(self, power, nodes=None):
        return self.control(POWERS[power] + ": ", nodes, prefix=True)

    def blocked_control(self, fixture, nodes=None):
        positions = self.positions(nodes)
        blocked = next(tile for tile in fixture["tiles"] if not tile["legal"])
        return positions[(blocked["row"], blocked["col"])]

    def exhaust(self, fixture):
        left, _ = self.moves()
        blocked = self.blocked_control(fixture)
        for _ in range(left):
            self.tap(blocked, delay=.10)
        self.control("Restart · Free")
        if self.moves()[0] != 0:
            raise AssertionError("Repeated real blocked attempts did not exhaust the budget")

    def case_moves(self):
        fixture = self.fixture()
        nodes = self.start_campaign(6)
        initial = self.moves(nodes)
        if initial != (fixture["base"], fixture["base"]):
            raise AssertionError("Initial move budget does not match real engine policy")
        first = fixture["steps"][0]
        blocked = self.blocked_control(fixture, nodes)
        self.tap(blocked)
        if self.moves()[0] != initial[0] - 1 or len(self.positions()) != len(fixture["tiles"]):
            raise AssertionError("Blocked attempt must cost one move without removing a piece")
        self.tap(self.positions()[(first["row"], first["col"])])
        if self.moves()[0] != initial[0] - 2:
            raise AssertionError("Successful exit must cost one action")
        self.click("↶  Undo")
        if self.moves()[0] != initial[0] - 3 or len(self.positions()) != len(fixture["tiles"]):
            raise AssertionError("Undo must cost one action and restore the piece")
        self.click("↶  Undo")
        if self.moves()[0] != initial[0] - 3:
            raise AssertionError("Empty Undo spent an action")
        self.log("PASS moves: certified initial budget; blocked -1; exit -1; successful Undo -1; empty Undo +0")
        self.exhaust(fixture)
        state = self.pref("luma_session")
        wallet = self.game_balance()
        self.screenshot("out-of-moves")
        self.continue_saved()
        if self.moves()[0] != 0 or self.game_balance() != wallet:
            raise AssertionError("Exhaustion/wallet changed during process recreation")
        if self.pref("luma_session")["run_id"] != state["run_id"]:
            raise AssertionError("Process recreation replaced the exhausted run")
        extra = max(5, len(fixture["tiles"]) + 3)
        for quota in (1, 2):
            self.click("+" + str(extra) + " moves · 100 gold")
            wallet -= 100
            if self.game_balance() != wallet or self.moves()[0] != extra:
                raise AssertionError("Gold continuation must debit100 and grant the exact disclosed extension")
            session = self.pref("luma_session")
            if session["budget_extensions"] != quota:
                raise AssertionError("Continuation quota not persisted")
            self.continue_saved()
            if self.game_balance() != wallet or self.moves()[0] != extra:
                raise AssertionError("Gold confirmation replayed after recreation")
            self.exhaust(fixture)
        labels = [BASE.text(node) for node in self.nodes()]
        if any(re.fullmatch(r"\+\d+ moves · 100 gold|Watch test ad · \+\d+ moves", label) for label in labels):
            raise AssertionError("A third continuation is available")
        self.screenshot("continuation-cap")
        self.click("Restart · Free")
        if self.moves() != initial or self.game_balance() != wallet:
            raise AssertionError("Free restart did not reset budget without a debit")
        if self.pref("luma_session")["budget_extensions"] != 0:
            raise AssertionError("Free restart retained continuation quota")
        self.log("PASS exhausted save/recreation; two exact -100 gold extensions; duplicates +0 after recreation; quota2 cap; free restart +0")
        self.open_code("B", 6)
        nodes = self.nodes()
        if "Friend challenge · Unlimited" not in [BASE.text(node) for node in nodes]:
            raise AssertionError("Friend challenge is not unlimited")
        if any(BASE.text(node).startswith(tuple(POWERS.values())) for node in nodes):
            raise AssertionError("Friend mode exposes power controls")
        blocked = self.blocked_control(fixture, nodes)
        for _ in range(initial[0] + 5):
            self.tap(blocked, delay=.08)
        if len(self.positions()) != len(fixture["tiles"]) or self.game_balance() != wallet:
            raise AssertionError("Friend blocked attempts changed board/wallet")
        self.continue_saved()
        if "Friend challenge · Unlimited" not in [BASE.text(node) for node in self.nodes()]:
            raise AssertionError("Friend unlimited mode was lost on recreation")
        self.log("PASS friend B6: unlimited attempts beyond campaign allowance, no powers/continuation purchase and no gold; recreation preserved")

    def case_powers(self):
        self.change_access(1)
        self.click("Shop", self.home())
        self.click("Vision, Level 5")
        labels = [BASE.text(node) for node in self.nodes()]
        if any(label.startswith("Get ×") or label == "Watch test ad · +1" for label in labels):
            raise AssertionError("Locked Vision offered acquisition")
        self.click("Close")
        self.log("PASS locked profile: Vision requires Journey5; no gold/ad acquisition offered")
        self.change_access(75)
        fixture = self.fixture()
        self.start_campaign(6)
        initial = self.moves()
        stock_before = self.pref("luma_drift_save_v1")
        for power in ("hint", "vision", "focus", "trail", "shield"):
            self.tap(self.power_control(power))
            actual = self.pref("luma_drift_save_v1")["booster_" + power]
            if actual != stock_before["booster_" + power] - 1 or self.moves() != initial:
                raise AssertionError("Power activation must debit one stock and no action: " + power)
            if power == "focus" and not any("Safe move" in BASE.text(node) for node in self.nodes()):
                raise AssertionError("Focus did not expose safe-move highlights")
            if power == "trail":
                steps = {int(match.group(1)) for node in self.nodes() if (match := re.search(r"Step (\d+)", BASE.text(node)))}
                if steps != set(range(1, min(5, len(fixture["tiles"])) + 1)):
                    raise AssertionError("Trail did not expose the exact next five preview steps")
            if power in ("focus", "trail"):
                self.screenshot("power-" + power)
        if self.pref("luma_session")["shield_charges"] != 3:
            raise AssertionError("Shield did not arm3 protected blocked attempts")
        self.continue_saved()
        if self.pref("luma_session")["shield_charges"] != 3:
            raise AssertionError("Shield charges were lost after recreation")
        blocked = self.blocked_control(fixture)
        for expected in (2, 1, 0):
            self.tap(blocked)
            if self.moves() != initial or self.pref("luma_session")["shield_charges"] != expected:
                raise AssertionError("Shield did not protect exactly the next three blocked attempts")
        self.tap(blocked)
        if self.moves()[0] != initial[0] - 1:
            raise AssertionError("Fourth block should cost one move")
        self.tap(self.power_control("flow"), delay=1.5)
        if self.moves()[0] != initial[0] - 4 or len(self.positions()) != len(fixture["tiles"]) - 3:
            raise AssertionError("Flow must execute3 exits, consuming3 actions")
        if self.pref("luma_drift_save_v1")["booster_flow"] != stock_before["booster_flow"] - 1:
            raise AssertionError("Flow stock not debited once")
        self.screenshot("six-powers")
        self.log("PASS six powers: one-stock debit; Spark/Vision/Focus/Trail/Shield cost no action; Shield3 blocks survives recreation then fourth -1; Flow3 actual exits cost3")
        self.click("Shop", self.home())
        self.click("Next")
        before = self.pref("luma_drift_save_v1")
        self.click("Shield: ", prefix=True)
        purchase = self.control("Get ×", prefix=True)
        match = re.fullmatch(r"Get ×(\d+) · (\d+) gold", BASE.text(purchase),re.IGNORECASE)
        if not match:
            raise AssertionError("Power shop does not disclose quantity/price")
        count, price = map(int, match.groups())
        self.tap(purchase)
        after = self.pref("luma_drift_save_v1")
        if after["coins"] != before["coins"] - price or after["booster_shield"] != before["booster_shield"] + count:
            raise AssertionError("Shop power pack amount or exact gold debit incorrect")
        self.log("PASS shop: Shield pack +" + str(count) + " at exact local test price -" + str(price))
        before = after
        self.click("Free loot · milestone ", prefix=True)
        after = self.pref("luma_drift_save_v1")
        changed = [power for power in POWERS if after.get("booster_" + power, 0) != before.get("booster_" + power, 0)]
        if len(changed) != 1 or after["booster_" + changed[0]] != before["booster_" + changed[0]] + 1 or after["coins"] != before["coins"]:
            raise AssertionError("Free milestone loot must add exactly one unlocked power without gold")
        self.log("PASS free milestone loot: one unlocked " + changed[0] + ", no gold; eligibility comes from EXPLICITLY SEEDED 5 synthetic first clears, not real played progression")
        time.sleep(3)  # Capture the settled shop after its transient loot notice.
        self.screenshot("power-shop")

    def wait_consent(self):
        self.select_age("18 or over")
        self.toggle("Allow optional advertising", True)
        deadline = time.monotonic() + 35
        while time.monotonic() < deadline:
            nodes = self.nodes()
            refusal = [node for node in nodes if BASE.text(node) == "Do not consent" and node.get("clickable") == "true"]
            if refusal:
                self.tap(refusal[-1])
                self.log("Real UMP choices: refused personalization")
                continue
            if any(node.get("class") == "android.webkit.WebView" for node in nodes):
                time.sleep(.4)
                continue
            statuses = [node.get("content-desc", "").split(":", 1)[1].strip() for node in nodes if node.get("content-desc", "").startswith("Ad status:")]
            if statuses and statuses[-1] == "Advertising privacy choices applied":
                return
            self.swipe()
        raise AssertionError("Real UMP did not permit an ad request; no display/earned success claimed")

    def watch_test_ad(self, watch_label, name):
        self.wait_consent()
        self.press("Load test ad")
        deadline = time.monotonic() + 45
        status = None
        while time.monotonic() < deadline:
            status = self.ad_status()
            if status == "Test ad ready":
                break
            if status == "Test ad unavailable. Please try again.":
                raise AssertionError("Official test ad failed to load; no earned reward claimed")
            time.sleep(.4)
        if status != "Test ad ready":
            raise AssertionError("Official test ad never became ready")
        self.screenshot(name + "-ready")
        self.press(watch_label)
        time.sleep(2)
        self.screenshot(name + "-official-test-ad")
        deadline = time.monotonic() + 50
        while time.monotonic() < deadline:
            nodes = self.nodes(all_packages=True)
            granted = any(BASE.text(node) == "Reward granted" for node in nodes)
            close = [node for node in nodes if node.get("clickable") == "true" and BASE.text(node).casefold() in ("close", "close ad", "close video", "close the ad")]
            if granted and close:
                self.tap(close[-1], delay=.5)
                return
            time.sleep(1)
        raise AssertionError("No visible Reward granted + Close pair; no external creative link selected")

    def case_ads(self):
        self.click("Settings", self.home())
        self.click("Play journal")
        self.control("Play journal: Off")
        self.click("Play journal: Off")
        fixture = self.fixture()
        self.start_campaign(6)
        self.exhaust(fixture)
        wallet = self.game_balance()
        extra = max(5, len(fixture["tiles"]) + 3)
        self.click("Watch test ad · +" + str(extra) + " moves")
        # Cancelling the offer without a real earned callback must grant nothing.
        self.shell("input", "keyevent", "4")
        if self.moves()[0] != 0 or self.game_balance() != wallet:
            raise AssertionError("Cancelling continuation consent granted a reward")
        self.log("PASS negative continuation: entered consent UI and Back without watching =>0 moves,0 gold")
        self.click("Watch test ad · +" + str(extra) + " moves")
        self.watch_test_ad("Watch test ad · +" + str(extra) + " moves", "continuation")
        if self.moves()[0] != extra or self.game_balance() != wallet:
            raise AssertionError("Real rewarded continuation must add disclosed moves and zero gold")
        session = self.pref("luma_session")
        if session["budget_extensions"] != 1 or not session["last_continuation"].startswith("a:"):
            raise AssertionError("Continuation receipt not applied exactly once")
        self.continue_saved()
        if self.moves()[0] != extra or self.game_balance() != wallet or self.pref("luma_session")["budget_extensions"] != 1:
            raise AssertionError("Earned continuation replayed/lost after process recreation")
        self.screenshot("continuation-earned")
        self.log("PASS actual SDK continuation: +" + str(extra) + " moves, quota1, +0 gold; process recreation applies no second extension")
        self.click("Shop", self.home())
        before = self.pref("luma_drift_save_v1")
        self.click("Spark: ", prefix=True)
        self.click("Watch test ad · +1")
        self.watch_test_ad("Watch test ad · Spark ×1", "power")
        after = self.pref("luma_drift_save_v1")
        if after["booster_hint"] != before["booster_hint"] + 1 or after["coins"] != before["coins"]:
            raise AssertionError("Real power ad must grant exactly1 Spark and zero gold")
        self.shell("am", "force-stop", PACKAGE)
        self.launch()
        self.click("Shop", self.home())
        self.control("Spark: " + str(after["booster_hint"]))
        if self.game_balance() != after["coins"]:
            raise AssertionError("Power ad credited gold after recreation")
        self.screenshot("power-earned")
        self.log("PASS actual SDK power: Spark +1 persisted, +0 gold, no continuation bonus; no Install/creative link selected")
        self.click("Settings", self.home())
        self.click("Play journal")
        self.click("Export journal")
        self.chooser("reward-journal-chooser")
        directory = "/data/user/0/" + PACKAGE + "/cache/shared"
        names = self.shell("su", "0", "ls", "-t", directory).splitlines()
        name = next(name for name in names if re.fullmatch(r"luma_journal_[0-9]+\.json", name))
        raw = self.shell("su", "0", "cat", directory + "/" + name)
        exported = json.loads(raw)
        earned = [event for event in exported["events"] if event["event"] == "ad_reward_earned"]
        fields = [event["fields"] for event in earned]
        if len(fields) != 2 or {f.get("reward") for f in fields} != {"continuation", "power"} or any("gold" in f or f.get("test") != "true" for f in fields):
            raise AssertionError("Journal should record exactly the two real mutually exclusive rewards without gold")
        power = next(field for field in fields if field["reward"] == "power")
        if power.get("item") != "hint" or power.get("amount") != "1":
            raise AssertionError("Power journal reward differs from granted item")
        (self.root / "docs/validation/v1.3/ad-journal-export.json").write_text(raw, encoding="utf-8")
        self.log("PASS exported local journal: exactly2 actual SDK earned events, continuation and hint×1, official test=true, no gold fields; chooser cancelled")
        self.click("Clear journal")
        self.click("Yes", self.nodes())
        self.click("Play journal: On")
        self.control("Play journal: Off")

    def run(self, cases):
        self.log("Lab1.3 moves/powers emulator smoke; explicit synthetic fixture; package=" + PACKAGE + "; serial=" + self.serial)
        self.prepare_profile()
        with tempfile.TemporaryDirectory(prefix="luma-v13-engine-fixture-") as temporary:
            self.classes = temporary
            probe = Path(temporary) / "MovePowerFixture.java"
            probe.write_text(FIXTURE, encoding="utf-8")
            sources = list((self.root / "app/src/main/java/com/androidhit/lumadrift/engine").glob("*.java"))
            subprocess.run([str(self.java / "javac"), "-d", temporary, *map(str, sources), str(probe)],
                           check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=45)
            for case in cases:
                getattr(self, "case_" + case)()
        crash = self.shell("logcat", "-d", "-b", "crash")
        if "FATAL EXCEPTION" in crash or "Fatal signal" in crash:
            raise AssertionError("Crash buffer is not clean")
        self.log("PASS selected actual UI flows; no real payment or external recipient/install link; crash buffer clean")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", required=True)
    parser.add_argument("--adb")
    parser.add_argument("--report")
    parser.add_argument("--seed-profile", action="store_true", help="Explicitly acknowledge temporary synthetic Journey75/gold/power profile, with original preferences restored")
    parser.add_argument("--cases", default="moves,powers", help="Comma-separated moves,powers,ads; ads uses two real official Google sample rewarded ads")
    args = parser.parse_args()
    if not args.seed_profile:
        parser.error("--seed-profile is required: these tests intentionally use a disclosed synthetic emulator profile")
    cases = args.cases.split(",")
    if not cases or any(case not in ("moves", "powers", "ads") for case in cases):
        parser.error("cases must be moves,powers,ads")
    smoke = MovePowerSmoke(args.serial, args.adb, args.report)
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
        except Exception as restore_error:
            result = 1
            smoke.log("RESTORATION FAILED: " + repr(restore_error) + "; backup remains " + str(smoke.backup))
        evidence = smoke.root / "docs/validation/v1.3/move-power-profile.json"
        evidence.parent.mkdir(parents=True, exist_ok=True)
        evidence.write_text(json.dumps(smoke.profile, indent=2) + "\n", encoding="utf-8")
        smoke.write_report()
        if result == 0:
            (smoke.shots / "v13-failure.png").unlink(missing_ok=True)
    return result


if __name__ == "__main__":
    sys.exit(main())
