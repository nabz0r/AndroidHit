#!/usr/bin/env python3
"""Real emulator UI checks for the isolated Luma Drift Lab application.

Never installs, clears data, changes component export rules, launches a payment
sheet or selects a recipient. Consent testing records an actual UMP attempt;
it never treats an unavailable configured message as a successful ad display.
Fresh-preference precondition: age unspecified, optional advertising off, journal
off, no pending simulated purchase. Test gold stays in the sandbox application.
"""
import argparse
import importlib.util
from pathlib import Path
import re
import sys
import time
import traceback

SPEC = importlib.util.spec_from_file_location("luma_v11_harness", Path(__file__).with_name("smoke-v11.py"))
BASE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(BASE)
PACKAGE = "com.androidhit.lumadrift.sandbox"
MAIN = "com.androidhit.lumadrift.MainActivity"
COMMERCE = "com.androidhit.lumadrift.commerce.CommerceActivity"
BASE.PACKAGE = PACKAGE


class CommerceSmoke(BASE.V11Smoke):
    def __init__(self, serial, adb=None, report=None):
        super().__init__(serial, adb, report or str(Path(__file__).resolve().parents[1] / "docs/validation/commerce-ui-smoke.txt"))
        self.shots = self.root / "docs/screenshots/lab"
        self.width, self.height = 1080, 1920

    def screenshot(self, name):
        self.shots.mkdir(parents=True, exist_ok=True)
        path = self.shots / (name + ".png")
        path.write_bytes(self.command("exec-out", "screencap", "-p"))
        self.log("Screenshot: " + str(path.relative_to(self.root)))

    def launch(self):
        self.shell("am", "start", "-n", PACKAGE + "/" + MAIN)
        time.sleep(0.3)

    def swipe(self, down=False):
        start, end = (0.35, 0.76) if down else (0.76, 0.35)
        self.shell("input", "swipe", str(self.width // 2), str(int(self.height * start)),
                   str(self.width // 2), str(int(self.height * end)), "650")
        time.sleep(0.15)

    def top(self):
        for _ in range(6):
            self.swipe(down=True)

    def visible(self, predicate, nodes):
        for node in nodes:
            if not predicate(node):
                continue
            try:
                left, top, right, bottom = self.bounds(node)
            except AssertionError:
                continue
            if right - left >= 12 and bottom - top >= 24 and (top + bottom) // 2 >= 40 and (top + bottom) // 2 <= self.height - 50:
                return node
        return None

    def seek(self, name=None, predicate=None):
        predicate = predicate or (lambda n: BASE.text(n).casefold() == name.casefold())
        node = self.visible(predicate, self.nodes())
        if node is not None:
            return node
        self.top()
        for _ in range(10):
            node = self.visible(predicate, self.nodes())
            if node is not None:
                return node
            self.swipe()
        raise AssertionError("Scrolled control not found: " + repr(name))

    def press(self, name):
        node = self.seek(name)
        if node.get("enabled") == "false":
            raise AssertionError("Disabled control: " + name)
        self.tap(node)

    def toggle(self, name, checked):
        node = self.seek(name, lambda n: BASE.text(n) == name and n.get("checkable") == "true")
        if (node.get("checked") == "true") != checked:
            self.tap(node)
        node = self.seek(name, lambda n: BASE.text(n) == name and n.get("checkable") == "true")
        if (node.get("checked") == "true") != checked:
            raise AssertionError("Toggle did not become " + str(checked) + ": " + name)

    def balance(self, nodes=None):
        node = self.seek(predicate=lambda n: re.fullmatch(r"\d+ gold", BASE.text(n)) is not None)
        return int(BASE.text(node).split()[0])

    def open_lab(self):
        # Commerce is deliberately non-exported. Enter through the real game UI.
        foreground = self.shell("dumpsys", "activity", "activities")
        resumed = "\n".join(line for line in foreground.splitlines() if "ResumedActivity" in line)
        if COMMERCE in resumed:
            return
        self.launch()
        self.click("Shop", self.home())
        self.click("Gold & rewards · Test")
        time.sleep(0.3)
        self.seek("LUMA DRIFT LAB")

    def select_age(self, label):
        spinner = self.seek(predicate=lambda n: n.get("class") == "android.widget.Spinner")
        self.tap(spinner)
        nodes = self.nodes()
        matches = [n for n in nodes if BASE.text(n) == label]
        if not matches:
            raise AssertionError("Age option missing: " + label)
        self.tap(matches[-1])

    def ad_status(self):
        node = self.seek(predicate=lambda n: n.get("content-desc", "").startswith("Ad status:"))
        return node.get("content-desc", "").split(":", 1)[1].strip()

    def no_payment_activity(self):
        activity = self.shell("dumpsys", "activity", "activities")
        resumed = "\n".join(line for line in activity.splitlines() if "ResumedActivity" in line)
        if "ProxyBillingActivity" in resumed or "com.android.vending" in resumed:
            raise AssertionError("A real payment surface was launched")

    def defaults_and_minors(self):
        self.open_lab()
        self.seek("Not specified")
        optional = self.seek("Allow optional advertising")
        if optional.get("checked") != "false":
            raise AssertionError("Optional ads must initially be off")
        self.screenshot("commerce")
        self.press("Load test ad")
        if self.ad_status() != "Test ads require age 18 or over":
            raise AssertionError("Unknown age did not block ad loading")
        watch = self.seek("Watch an ad · +35 gold")
        if watch.get("enabled") != "false":
            raise AssertionError("Watch enabled for unknown age")
        self.select_age("Under 18")
        self.press("Allow optional advertising")
        optional = self.seek("Allow optional advertising")
        if optional.get("checked") != "false":
            raise AssertionError("A minor enabled advertising")
        self.press("Load test ad")
        if self.ad_status() != "Test ads require age 18 or over":
            raise AssertionError("Minor ad request was not blocked")
        self.screenshot("consent")
        self.select_age("Not specified")
        self.log("PASS default/age UI: unknown age and ads off; unknown/minor loads blocked; minor opt-in remains off; watch disabled")
        self.log("SCOPE: UI blocking checked. No packet capture is claimed; network exclusion also follows the controller's pre-UMP age/opt-in gate.")

    def simulated_purchases(self):
        before = self.balance()
        self.press("Simulate · 500 gold")
        self.click("Cancel", self.nodes())
        if self.balance() != before:
            raise AssertionError("Cancelling a simulated purchase changed gold")
        self.no_payment_activity()
        self.press("Simulate · 500 gold")
        self.click("Leave pending", self.nodes())
        if self.balance() != before:
            raise AssertionError("Pending simulation credited gold")
        self.seek("Payment pending. No gold has been added.")
        self.screenshot("pending")
        self.shell("am", "force-stop", PACKAGE)
        self.launch()
        self.open_lab()
        self.seek("Complete pending test")
        if self.balance() != before:
            raise AssertionError("Process recreation credited a pending transaction")
        self.press("Complete pending test")
        if self.balance() != before + 500:
            raise AssertionError("Pending completion must credit exactly 500")
        self.press("Repeat last test confirmation")
        if self.balance() != before + 500:
            raise AssertionError("Repeated confirmation credited twice")
        self.log("PASS simulation: Cancel +0; Pending +0 across force-stop; Complete +500; repeat confirmation +0")
        total = before + 500
        for amount in (1400, 3600):
            self.press("Simulate · " + str(amount) + " gold")
            self.click("Complete test purchase", self.nodes())
            total += amount
            if self.balance() != total:
                raise AssertionError("Wrong simulated pack amount: " + str(amount))
            self.press("Repeat last test confirmation")
            if self.balance() != total:
                raise AssertionError("Duplicate confirmation farmed " + str(amount))
            self.log("PASS simulation pack " + str(amount) + ": exact one credit; repeat +0")
        self.top()
        self.screenshot("gold")
        self.no_payment_activity()
        self.log("PASS sandbox wallet: " + str(before) + " -> " + str(total) + "; all values are local simulated gold, no money charged")

    def metrics(self):
        node = self.seek("Play journal", lambda n: BASE.text(n) == "Play journal" and n.get("checkable") == "true")
        if node.get("checked") != "false":
            raise AssertionError("Local journal must start off for this test")
        self.toggle("Play journal", True)
        self.screenshot("local-metrics")
        self.toggle("Play journal", False)
        self.shell("am", "force-stop", PACKAGE)
        self.launch()
        self.open_lab()
        restored = self.seek("Play journal", lambda n: BASE.text(n) == "Play journal" and n.get("checkable") == "true")
        if restored.get("checked") != "false":
            raise AssertionError("Journal opt-out did not survive process recreation")
        self.log("PASS local observations: explicit journal opt-in/out; off survives process recreation")

    def advertising(self):
        before = self.balance()
        self.select_age("18 or over")
        self.toggle("Allow optional advertising", True)
        observed = []
        deadline = time.monotonic() + 25
        form_seen = False
        while time.monotonic() < deadline:
            nodes = self.nodes()
            refusal = [n for n in nodes if BASE.text(n) == "Do not consent" and n.get("clickable") == "true"]
            if refusal:
                form_seen = True
                self.log("OBSERVED: real Google Publisher Test Ads UMP form; testing refusal, never accepting personalization")
                self.screenshot("ump-form")
                self.tap(refusal[-1])
                time.sleep(0.3)
                continue
            if any(n.get("class") == "android.webkit.WebView" for n in nodes):
                time.sleep(0.4)
                continue  # Form may still be loading. Never scroll/click behind it.
            statuses = [n.get("content-desc", "").split(":", 1)[1].strip()
                        for n in nodes if n.get("content-desc", "").startswith("Ad status:")]
            if statuses:
                status = statuses[-1]
                observed.append(status)
                if status in ("Advertising privacy choices applied", "Advertising privacy check unavailable. Ads stay blocked."):
                    break
            self.swipe()
        if not observed and not form_seen:
            raise AssertionError("No observable UMP request state after explicit opt-in")
        self.log("OBSERVED UMP attempt after explicit adult opt-in: " + repr(observed))
        self.screenshot("ump-status")
        self.toggle("Allow optional advertising", False)
        self.press("Load test ad")
        if self.ad_status() != "Enable optional advertising to load test ads":
            raise AssertionError("Revocation did not block the next load")
        if self.seek("Watch an ad · +35 gold").get("enabled") != "false":
            raise AssertionError("Revoked advertising left Watch enabled")
        if self.balance() != before:
            raise AssertionError("Consent/request attempt credited gold without an ad reward")
        self.select_age("Under 18")
        self.press("Allow optional advertising")
        if self.seek("Allow optional advertising").get("checked") != "false":
            raise AssertionError("Minor re-enabled advertising after adult session")
        self.select_age("Not specified")
        self.log("PASS revocation: ad switch off; next load blocked; watch disabled; no gold credited; minor cannot re-enable")
        self.log("NOT VALIDATED: actual ad display, completion/reward or paid impression callback. A successful UMP form is not evidence of any of those events.")

    def official_test_ad(self, metrics_expected=False):
        """Optional real SDK check. Only Google sample ads; close, never Install."""
        before = self.balance()
        self.select_age("18 or over")
        self.toggle("Allow optional advertising", True)
        try:
            # Consent refresh is asynchronous even when the previously refused
            # choices remain valid. Never act on controls behind a UMP WebView.
            deadline = time.monotonic() + 25
            status = None
            while time.monotonic() < deadline:
                nodes = self.nodes()
                refusal = [n for n in nodes if BASE.text(n) == "Do not consent" and n.get("clickable") == "true"]
                if refusal:
                    self.tap(refusal[-1])
                    self.log("OBSERVED UMP refusal applied; personalization was not accepted")
                    continue
                if any(n.get("class") == "android.webkit.WebView" for n in nodes):
                    time.sleep(0.4)
                    continue
                statuses = [n.get("content-desc", "").split(":", 1)[1].strip()
                            for n in nodes if n.get("content-desc", "").startswith("Ad status:")]
                if statuses:
                    status = statuses[-1]
                    if status in ("Advertising privacy choices applied", "Advertising privacy check unavailable. Ads stay blocked."):
                        break
                self.swipe()
            if status != "Advertising privacy choices applied":
                raise AssertionError("Consent gate is not ready; no ad request attempted; status=" + repr(status))
            self.press("Load test ad")
            deadline = time.monotonic() + 40
            previous = None
            while time.monotonic() < deadline:
                status = self.ad_status()
                if status != previous:
                    self.log("Google test-ad load: " + status)
                    previous = status
                if status in ("Test ad ready", "Test ad unavailable. Please try again."):
                    break
                time.sleep(0.4)
            self.screenshot("test-ad-load")
            if status != "Test ad ready":
                self.log("NOT VALIDATED display/reward: actual Google sample ad did not become ready")
                return
            self.press("Watch an ad · +35 gold")
            time.sleep(2)
            self.screenshot("test-ad")
            deadline = time.monotonic() + 45
            closed = False
            while time.monotonic() < deadline:
                nodes = self.nodes(all_packages=True)
                reward_visible = any(BASE.text(n) == "Reward granted" for n in nodes)
                close = [n for n in nodes if n.get("clickable") == "true"
                         and BASE.text(n).casefold() in ("close ad", "close", "close video", "close the ad")]
                if reward_visible and close:
                    self.tap(close[-1])
                    closed = True
                    break
                time.sleep(2)
            if not closed:
                self.shell("input", "keyevent", "4")
                time.sleep(0.5)
            after = self.balance()
            self.log("Official test-ad wallet result: " + str(before) + " -> " + str(after))
            if after != before + 35:
                raise AssertionError("Displayed sample ad did not credit exactly 35 after reward")
            self.log("PASS actual Google sample rewarded ad: display, Reward granted, Close, exact +35 local test gold; no external link selected")
            if not metrics_expected:
                self.log("NOT VALIDATED paid/impression journal callback: local metrics were off during this ad; test income is never real revenue")
        finally:
            self.toggle("Allow optional advertising", False)
            self.select_age("Not specified")
            self.log("Cleanup: optional advertising off; age unspecified")

    def run_ad_metrics(self):
        """Targeted existing-state SDK/journal check. No simulated pack rerun."""
        import datetime as dt
        import json
        self.log("Targeted ad-metrics UI smoke; package=" + PACKAGE + "; serial=" + self.serial)
        if self.command("get-state").decode().strip() != "device":
            raise AssertionError("Emulator unavailable")
        package = self.shell("dumpsys", "package", PACKAGE)
        if "versionName=1.2.0-sandbox" not in package:
            raise AssertionError("Sandbox APK required; no install/reset is performed")
        for line in package.splitlines():
            if line.strip().startswith(("versionCode=", "versionName=")):
                self.log("Installed sandbox: " + line.strip())
        dimensions = re.findall(r"(\d+)x(\d+)", self.shell("wm", "size"))
        if dimensions:
            self.width, self.height = map(int, dimensions[-1])
        self.open_lab()
        # Confirm access before generating evidence. This is an explicitly chosen
        # emulator's own sandbox test data; no account or other app data is read.
        root_prefix = []
        identity = self.shell("id")
        if "uid=0(" not in identity:
            try:
                identity = self.shell("su", "0", "id")
            except Exception as error:
                raise AssertionError("Emulator root unavailable for its own staged test export") from error
            if "uid=0(" not in identity:
                raise AssertionError("Emulator root access was not confirmed")
            root_prefix = ["su", "0"]
        self.log("Confirmed emulator root for reading this sandbox's staged export only")
        self.seek("Not specified")
        if self.seek("Allow optional advertising").get("checked") != "false":
            raise AssertionError("Targeted precondition: advertising must be off")
        metrics = self.seek("Play journal", lambda n: BASE.text(n) == "Play journal" and n.get("checkable") == "true")
        if metrics.get("checked") != "false":
            raise AssertionError("Targeted precondition: local journal must be off")
        cutoff = int(self.shell("date", "+%s").strip())
        before = self.balance()
        self.toggle("Play journal", True)
        self.official_test_ad(metrics_expected=True)
        after = self.balance()
        if after != before + 35:
            raise AssertionError("Single SDK ad reward must credit exactly 35")
        self.press("Open local journal")
        self.click("Export journal", self.nodes())
        self.chooser("ad-metrics-export-chooser")  # Open/cancel only; no recipient.
        directory = "/data/user/0/" + PACKAGE + "/cache/shared"
        names = self.shell(*root_prefix, "ls", "-t", directory).splitlines()
        exports = [name.strip() for name in names if re.fullmatch(r"luma_journal_[0-9]+\.json", name.strip())]
        if not exports:
            raise AssertionError("No staged journal JSON after export chooser")
        raw = self.shell(*root_prefix, "cat", directory + "/" + exports[0])
        exported = json.loads(raw)
        if exported.get("schema") != "luma-drift-local-journal-v1":
            raise AssertionError("Unexpected staged journal schema")
        def recent(event):
            stamp = dt.datetime.fromisoformat(event["at"].replace("Z", "+00:00")).timestamp()
            return stamp >= cutoff
        events = [event for event in exported.get("events", []) if recent(event)]
        impressions = [event for event in events if event.get("event") == "ad_impression"]
        paid = [event for event in events if event.get("event") == "ad_paid"]
        rewards = [event for event in events if event.get("event") == "ad_reward_earned"]
        if not impressions:
            raise AssertionError("Real SDK impression missing from the opt-in local export")
        if len(rewards) != 1 or rewards[0].get("fields", {}).get("gold") != "35":
            raise AssertionError("Expected exactly one 35-gold reward observation")
        for event in impressions + paid + rewards:
            if event.get("fields", {}).get("test") != "true":
                raise AssertionError("Expected official test-ad observations only")
        proof = self.root / "docs/validation/lab-ad-journal-export.json"
        proof.write_text(raw, encoding="utf-8")
        self.log("PASS actual SDK ad_impression observed locally: " + str(len(impressions)))
        self.log("PASS actual SDK ad_reward_earned once; wallet " + str(before) + " -> " + str(after) + " (+35)")
        if paid:
            values = [{key: event.get("fields", {}).get(key) for key in ("value_micros", "currency", "precision", "test")} for event in paid]
            self.log("OBSERVED actual SDK ad_paid: " + json.dumps(values, sort_keys=True) + "; test values are not real income")
        else:
            self.log("NOT OBSERVED: ad_paid was not emitted in this test; no paid callback or real income is claimed")
        self.screenshot("ad-metrics-journal")
        self.click("Clear journal", self.nodes())
        self.click("Yes", self.nodes())
        self.click("Play journal: On", self.nodes())
        self.control("Play journal: Off")
        remaining = self.shell(*root_prefix, "ls", directory).splitlines()
        if any(re.fullmatch(r"luma_journal_[0-9]+\.json", name.strip()) for name in remaining):
            raise AssertionError("Temporary journal exports were not cleared")
        self.click("Export journal")
        if not any(BASE.text(node) == "Export journal" for node in self.nodes()):
            raise AssertionError("Cleared journal unexpectedly opened a share chooser")
        crash = self.shell("logcat", "-d", "-b", "crash")
        if "FATAL EXCEPTION" in crash or "Fatal signal" in crash:
            raise AssertionError("Crash buffer is not clean")
        self.log("PASS cleanup: journal and staged JSON exports cleared, journal off, ads off, age unspecified; no recipient/external link/payment selected; crash buffer clean")

    def run_commerce(self):
        self.log("Commerce UI smoke; package=" + PACKAGE + "; serial=" + self.serial)
        if self.command("get-state").decode().strip() != "device":
            raise AssertionError("Emulator is unavailable")
        package = self.shell("dumpsys", "package", PACKAGE)
        if "versionName=1.2.0-sandbox" not in package:
            raise AssertionError("Install the sandbox APK first; this test never installs")
        stable = self.shell("dumpsys", "package", "com.androidhit.lumadrift")
        lab_uid = re.search(r"userId=(\d+)", package)
        stable_uid = re.search(r"userId=(\d+)", stable)
        if stable_uid and lab_uid:
            if stable_uid.group(1) == lab_uid.group(1):
                raise AssertionError("Stable and lab share an application UID")
            self.log("PASS application isolation: distinct stable/lab Android UIDs; stable APK/version not modified by test")
        dimensions = self.shell("wm", "size")
        match = re.findall(r"(\d+)x(\d+)", dimensions)
        if match:
            self.width, self.height = map(int, match[-1])
        self.launch()
        self.click("Shop", self.home())
        self.screenshot("shop")
        self.click("Gold & rewards · Test")
        self.defaults_and_minors()
        self.simulated_purchases()
        self.metrics()
        self.advertising()
        self.no_payment_activity()
        crash = self.shell("logcat", "-d", "-b", "crash")
        if "FATAL EXCEPTION" in crash or "Fatal signal" in crash:
            raise AssertionError("Crash buffer is not clean:\n" + crash)
        self.log("PASS: selected lab flows completed; no payment sheet/recipient selected; no reset; crash buffer clean")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", required=True)
    parser.add_argument("--adb")
    parser.add_argument("--report")
    parser.add_argument("--case", choices=("all", "ad-metrics"), default="all", help="Target existing state with ad-metrics; avoids replaying simulated packs")
    parser.add_argument("--test-ad", action="store_true", help="Also try one actual Google sample rewarded ad and verify +35; never click Install")
    args = parser.parse_args()
    smoke = CommerceSmoke(args.serial, args.adb, args.report)
    try:
        if args.case == "ad-metrics":
            smoke.run_ad_metrics()
        else:
            smoke.run_commerce()
            if args.test_ad:
                smoke.official_test_ad()
        failure = smoke.shots / "failure.png"
        failure.unlink(missing_ok=True)
        return 0
    except Exception as error:
        smoke.log("FAIL: " + str(error))
        try:
            smoke.screenshot("failure")
        except Exception as capture_error:
            smoke.log("Failure screenshot unavailable: " + str(capture_error))
        smoke.log(traceback.format_exc())
        return 1
    finally:
        smoke.write_report()


if __name__ == "__main__":
    sys.exit(main())
