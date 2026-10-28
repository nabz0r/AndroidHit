#!/usr/bin/env python3
"""Offline site 1.12 contracts: bilingual DOM, immutable release, source provenance.

No browser, public Hosting origin, provider endpoint or external network is requested.
Browser rendering and authenticated deployment proof are separate acceptance steps.
The previous edition checkers and their reports remain historical evidence.
"""
import argparse
import importlib.util
import json
from pathlib import Path
import re
import struct
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
PUBLIC = ROOT / "public"
SPEC = importlib.util.spec_from_file_location("site_contract_helpers", ROOT / "scripts/check-site-v17.py")
BASE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(BASE)
VERSION = "1.12.0-rc2"
APK_NAME = "luma-drift-" + VERSION + ".apk"
APK_SHA = "de581253b64232ae45bcbd647862dd470d522068f26516f6417bf515e882403d"
LAB_SHA = "0f55c38c29d5872588200a5675cb154809eb62897d1133d96b48a82ab12b91e4"
APK_URL = "https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/" + APK_NAME
UNCHANGED = {
    "public/site-config.js": "6053074f0b45e54e14267e7d2316be8f68d981110c2376641653e039fc0f4f67",
    "public/assets/delete-account.js": "68933dc4981d16a7473776c2cef6635799558923f3a11673951f1f9c768b0ce3",
    "public/assets/deletion-core.mjs": "67815fa6c57a157aa6d712e56b29bb356bdf3bf21da63f9b7275d4dcf2e512ad",
    "public/app-ads.txt": "1ec22f11296d1bf892fdd26f7019ca10049cc97977d31ce9ea806b888b6dbcba",
    "firebase.json": "1a02cfbadae7edeff76275b7c2c223664b475d0406ba310cf45b07bd20842867",
}
FINAL_RUNS = (
    ("docs/validation/v1.12/ui-bugs/ui-sdk-final-rc2-lab40/host-report.json",
     "docs/screenshots/v1.12/ui-bugs/ui-sdk-final-rc2-lab40", LAB_SHA),
    ("docs/validation/v1.12/ui-bugs/ui-sdk-final-rc2-production-shop/host-report.json",
     "docs/screenshots/v1.12/ui-bugs/ui-sdk-final-rc2-production-shop", APK_SHA),
    ("docs/validation/v1.12/commerce-native/rc2-b/host-report.json",
     "docs/screenshots/v1.12/commerce-native/rc2-b", APK_SHA),
)


def historical_snapshot():
    return {str(p.relative_to(ROOT)): BASE.digest(p)
            for folder in (ROOT / "docs/validation").glob("site-v*")
            if folder.name != "site-v1.12"
            for p in folder.rglob("*") if p.is_file()}


class Audit(BASE.Audit):
    def __init__(self, args):
        output = Path(args.report_dir).resolve()
        if output != (ROOT / "docs/validation/site-v1.12").resolve() and (ROOT / "docs/validation").resolve() in output.parents:
            if not any(p.name == "site-v1.12" for p in output.parents):
                raise ValueError("New checker cannot overwrite another edition's evidence")
        self.history_before = historical_snapshot()
        super().__init__(args)

    def release(self, home):
        manifest_path = ROOT / "releases" / ("manifest-" + VERSION + ".json")
        self.check(manifest_path.is_file(), "Frozen production RC2 manifest exists")
        manifest = json.loads(manifest_path.read_text())
        artifact = manifest["artifacts"][APK_NAME]
        apk = ROOT / "releases" / APK_NAME
        self.check(manifest.get("version") == VERSION and manifest.get("version_code") == 13
                   and manifest.get("application_id") == "com.androidhit.lumadrift", "Production release is 1.12.0-rc2/code13")
        self.check(apk.stat().st_size == artifact["bytes"] == 7773794
                   and BASE.digest(apk) == artifact["sha256"] == APK_SHA,
                   "Download matches independently frozen production APK bytes and SHA256")
        if self.args.apk_sha256:
            self.check(self.args.apk_sha256 == APK_SHA, "Caller-supplied final APK checksum matches frozen RC2")
        cfg = manifest.get("configuration", {})
        self.check(cfg.get("paid_cosmetic_draws_enabled") is False and cfg.get("force_test_ads") is True
                   and cfg.get("live_services_validated") is False and cfg.get("wallet_api_configured") is False,
                   "Frozen build keeps test ads, closed premium draws and unqualified checkout")
        links = [n.attrs["href"] for n in home.tags("a") if ".apk" in n.attrs.get("href", "")]
        self.check(len(links) >= 2 and all(link == APK_URL for link in links), "Every download action targets exact current production RC2")
        for lang in ("fr", "en"):
            notes = BASE.normalized(" ".join(n.visible_text(lang) for n in home.nodes if n.has_class("download-note")))
            self.check(VERSION in notes, "Visible exact APK version in " + lang)
            sizes = re.findall(r"(\d+(?:[.,]\d+)?)\s*(Mo|MB|Mio|MiB)\b", notes)
            self.check(bool(sizes), "Visible actual APK size in " + lang)
            for value, unit in sizes:
                actual = artifact["bytes"] / (1024 ** 2 if unit in ("Mio", "MiB") else 1e6)
                self.check(abs(float(value.replace(",", ".")) - actual) <= .05, "Rounded APK size agrees with bytes in " + lang)
        source = home.path.read_text()
        self.check(not re.search(r"TOKEN[A-Z]+|1\.12 (?:en préparation|in preparation)", source), "No release placeholder or obsolete preparation-version claim")
        self.check("RELEASE_1_12.md" in source, "Current release notes point at 1.12 documentation")

    def edition_contract(self):
        home = self.documents[(PUBLIC / "index.html").resolve()]
        privacy = self.documents[(PUBLIC / "privacy/index.html").resolve()]
        deletion = self.documents[(PUBLIC / "delete-account/index.html").resolve()]
        self.check(home.meta("color-scheme") == "light", "Homepage declares its light native-matched theme")
        self.check(home.tags("title")[0].text().strip() == "Luma Drift 1.12 — Un petit monde à toi.",
                   "Initial French HTML title agrees with current runtime title")
        for lang in ("fr", "en"):
            copy = BASE.normalized(home.root.visible_text(lang))
            for token in ("LumaCoins", "LumaGold", "24", "288"):
                self.check(token in copy, "Current currency/catalogue fact is visible in " + lang + ": " + token)
            self.check(bool(re.search(r"\b1[\s,.\u00a0\u202f]?000\b", copy)), "1000 Journey boards disclosed in " + lang)
            self.check("qualification" in copy.lower(), "Qualification state is explicit in " + lang)
            self.check(("achats réels" if lang == "fr" else "real checkout") in copy.lower(), "Real checkout is not presented as already qualified in " + lang)
            legal = BASE.normalized(privacy.root.visible_text(lang))
            for token in ("7", "30", "UTC", "LumaCoins", "LumaGold"):
                self.check(token in legal, "Privacy retention and currencies retained in " + lang + ": " + token)
            for fr, en in (("désactivé par défaut", "off by default"), ("carnet d’adresses", "address book"), ("avant de supprimer", "before deleting")):
                self.check((fr if lang == "fr" else en) in legal, "Existing privacy obligation retained in " + lang + ": " + en)
            dcopy = BASE.normalized(deletion.root.visible_text(lang))
            self.check(("d’abord les données serveur" if lang == "fr" else "first remove server data") in dcopy,
                       "Deletion explanation keeps server-first order in " + lang)
        site = (PUBLIC / "assets/site.js").read_text()
        for title in ("Luma Drift 1.12 — Un petit monde à toi.", "Luma Drift 1.12 — A little world of your own."):
            self.check(title in site, "Runtime bilingual title uses current edition: " + title)
        self.check("Luma Drift 1.10" not in site, "Runtime title no longer advertises older edition")

    def configuration(self):
        super().configuration()
        for path, sha in UNCHANGED.items():
            self.check(BASE.digest(ROOT / path) == sha, "Site refresh preserves configured identity/deletion/ads/Hosting bytes: " + path)

    def current_sources(self):
        indexed = {}
        for report_name, directory_name, apk_sha in FINAL_RUNS:
            report_path, directory = ROOT / report_name, ROOT / directory_name
            report = json.loads(report_path.read_text())
            self.check(report.get("passed") is True and report.get("restored") is True,
                       "Native source run passed and restored original profile: " + report_name)
            self.check((report.get("apk_sha256") or report.get("game_apk_sha256")) == apk_sha,
                       "Source run names exact frozen RC2 APK: " + report_name)
            if "phases" in report:
                phases = {p["phase"]: p for p in report["phases"]}
            else:
                native = json.loads(report_path.with_name("report.json").read_text())
                self.check(native.get("passed") is True, "Commerce RC2-b native instrumentation passed")
                phases = {"": {"native": native}}
            for key, sha in report.get("screenshots", {}).items():
                if "analysis-crop" in key:
                    continue
                phase = phases.get(key.split("/", 1)[0] if "/" in key else "", {})
                native = phase.get("native", {})
                path = directory / key
                if not (native.get("passed") is True and path.stem in native.get("captures", []) and path.is_file()):
                    continue
                if BASE.digest(path) != sha:
                    continue
                raw = path.read_bytes()
                if not raw.startswith(b"\x89PNG\r\n\x1a\n"):
                    continue
                width, height = struct.unpack(">II", raw[16:24])
                if (width, height) not in ((1080, 2340), (2340, 1080)):
                    continue
                indexed.setdefault(sha, []).append({"source": str(path.relative_to(ROOT)), "evidence": report_name,
                    "apk_sha256": apk_sha, "width": width, "height": height, "package": report.get("package") or native.get("package"),
                    "profile_scope": report.get("scope"), "capture_classification": "unedited-native-full-frame"})
        return indexed

    def asset_sources(self):
        current = self.current_sources()
        provenance_path = ROOT / "docs/validation/site-v1.12/screenshot-provenance.json"
        self.check(provenance_path.is_file(), "Current screenshot provenance manifest exists")
        provenance = json.loads(provenance_path.read_text())
        self.check(provenance.get("schemaVersion") == 1 and provenance.get("release") == VERSION,
                   "Screenshot provenance schema and release are current")
        declared = {}
        for item in provenance.get("images", []):
            public = item.get("public", "")
            self.check(public not in declared, "One provenance record per public image: " + public)
            declared[public] = item
            matches = current.get(item.get("sha256"), [])
            matches = [source for source in matches if source["source"] == item.get("source") and source["evidence"] == item.get("evidence")]
            self.check(bool(matches), "Provenance names a passing exact native source and report: " + public)
            self.check(item.get("locale") in ("fr", "en") and bool(item.get("description")), "Provenance explicitly labels capture locale and scope: " + public)
            source_name = Path(item.get("source", "")).name
            source_locale = re.search(r"-(fr|en)\.png$", source_name)
            if source_locale:
                self.check(item.get("locale") == source_locale.group(1), "Provenance locale matches named native capture: " + public)
            elif source_name == "visual-final-equipped-original.png":
                self.check(item.get("locale") == "en", "Equipped original full frame is explicitly labelled English")
            if matches:
                self.check(item.get("width") == matches[0]["width"] and item.get("height") == matches[0]["height"], "Provenance retains full original dimensions: " + public)
        referenced = set()
        for doc in self.documents.values():
            for node in doc.tags("img"):
                for attr in ("src", "data-screenshot-fr", "data-screenshot-en"):
                    value = node.attrs.get(attr, "")
                    if "/screenshots/" in value:
                        self.check("screenshots/v1.12/" in value, "Referenced game screenshot belongs to current edition: " + value)
                        referenced.add(str((doc.path.parent / value.split("?", 1)[0]).resolve().relative_to(ROOT)))
        self.check(len(referenced) >= 8, "Product page actually references multiple current bilingual native images")
        self.check(referenced <= set(declared), "Every rendered current screenshot is covered by explicit provenance")
        for path in BASE.payloads():
            if path.suffix.lower() not in (".png", ".svg", ".webp", ".jpg"):
                continue
            sha = BASE.digest(path)
            relative = str(path.relative_to(ROOT))
            matches = current.get(sha, [])
            if "/screenshots/v1.12/" in relative:
                self.check(bool(matches), "Current public PNG is an unedited final RC2 native full frame: " + relative)
                item = declared.get(relative, {})
                self.check(item.get("sha256") == sha and item.get("public") == relative, "Public bytes match their provenance manifest: " + relative)
            if path.suffix.lower() == ".svg":
                try:
                    svg = ET.fromstring(path.read_bytes())
                    self.check("viewBox" in svg.attrib, "Vector has viewBox: " + relative)
                    self.check(not any(n.tag.rsplit("}", 1)[-1] in ("script", "foreignObject") for n in svg.iter()), "Vector contains no active content: " + relative)
                except ET.ParseError:
                    self.check(False, "Vector parses: " + relative)
            self.assets.append({"public": relative, "bytes": path.stat().st_size, "sha256": sha,
                "source_classification": "current-sdk-1.12-final-rc2" if matches else "retained-historical-asset" if "/screenshots/" in relative else "brand-illustration",
                "exact_sources": [x["source"] for x in matches], "passing_runtime_sources": matches})

    def syntax_and_refs(self):
        super().syntax_and_refs()
        self.check(self.history_before == historical_snapshot(), "All historical website evidence remains byte-identical during check")

    def run(self):
        passed = super().run()
        path = self.out / "static-check.json"
        report = json.loads(path.read_text())
        history_changed = self.history_before != historical_snapshot()
        report.update({"edition": "1.12", "release": VERSION, "release_ready": passed and not history_changed,
                       "historical_site_reports_modified": history_changed})
        if history_changed:
            report["status"] = "FAIL"
            report["failures"].append("Historical website evidence changed during check")
        path.write_text(json.dumps(report, indent=2) + "\n")
        manifest_path = self.out / "source-manifest.json"
        manifest = json.loads(manifest_path.read_text())
        manifest["runner_sha256"] = BASE.digest(Path(__file__))
        manifest["base_runner_sha256"] = BASE.digest(ROOT / "scripts/check-site-v17.py")
        manifest_path.write_text(json.dumps(manifest, indent=2) + "\n")
        return passed and not history_changed


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--node")
    p.add_argument("--apk-sha256")
    p.add_argument("--report-dir", default=str(ROOT / "docs/validation/site-v1.12/release"))
    p.add_argument("--self-test", action="store_true")
    args = p.parse_args()
    BASE.parser_self_test()
    if args.self_test:
        print("PASS inherited bilingual visibility, hidden controls and exact plus-tag decoding parser regressions")
        sys.exit(0)
    sys.exit(0 if Audit(args).run() else 1)
