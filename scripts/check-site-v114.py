#!/usr/bin/env python3
"""Offline website 1.14 checks: bilingual source, release bytes and native capture provenance.

No HTTP request, browser, deployment or provider action is performed. Rendered layout and
live Hosting qualification remain separate. Historical checkers and reports are immutable.
"""
import argparse
from collections import Counter
import hashlib
import importlib.util
import json
from pathlib import Path
import re
import struct
import subprocess
import sys
from urllib.parse import urlsplit
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
PUBLIC=ROOT/"public"
SPEC=importlib.util.spec_from_file_location("site_contract_helpers",ROOT/"scripts/check-site-v17.py")
BASE=importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(BASE)
VERSION="1.14.0-rc2"
LAB_VERSION="1.14.0-sandbox-rc2"
CURRENT_POLICY_SHA="3a4e666de4e8105c80dda2934ae12fec3c98ec4d2a8bb3e9c393254acc0093a3"
APK_NAME="luma-drift-"+VERSION+".apk"
APK_PREFIX="https://raw.githubusercontent.com/nabz0r/AndroidHit/"
UNCHANGED={
    "public/site-config.js":"6053074f0b45e54e14267e7d2316be8f68d981110c2376641653e039fc0f4f67",
    "public/assets/delete-account.js":"68933dc4981d16a7473776c2cef6635799558923f3a11673951f1f9c768b0ce3",
    "public/assets/deletion-core.mjs":"67815fa6c57a157aa6d712e56b29bb356bdf3bf21da63f9b7275d4dcf2e512ad",
    "public/delete-account/index.html":"",
    "public/assets/style.css":"",
    "public/app-ads.txt":"1ec22f11296d1bf892fdd26f7019ca10049cc97977d31ce9ea806b888b6dbcba",
    "firebase.json":"1a02cfbadae7edeff76275b7c2c223664b475d0406ba310cf45b07bd20842867",
}

def history_snapshot():
    paths=[p for folder in (ROOT/"docs/validation").glob("site-v*") if folder.name!="site-v1.14" for p in folder.rglob("*") if p.is_file()]
    paths += [p for p in (ROOT/"scripts").glob("*-site-v*.py") if "v114" not in p.name]
    paths += [p for p in (PUBLIC/"tests").glob("site-language-v*.test.mjs") if "v114" not in p.name]
    return {str(p.relative_to(ROOT)):BASE.digest(p) for p in paths}

class Audit(BASE.Audit):
    def __init__(self,args):
        output=Path(args.report_dir).resolve()
        validation=(ROOT/"docs/validation").resolve()
        if validation in output.parents and not any(p.name=="site-v1.14" for p in (output,)+tuple(output.parents)):
            raise ValueError("The 1.14 checker cannot overwrite another edition’s evidence")
        self.history_before=history_snapshot()
        self.known_apks={}
        self.release_record={}
        self.native_input_files=set()
        super().__init__(args)

    def local_reference(self,reference,source):
        super().local_reference(reference,source)
        prefix="https://github.com/nabz0r/AndroidHit/blob/main/"
        if reference.startswith(prefix):
            target=(ROOT/reference[len(prefix):].split("#",1)[0]).resolve()
            self.check(ROOT.resolve() in target.parents and target.is_file(),"Repository documentation link has actual local source: "+reference)

    def edition_contract(self):
        home=self.documents[(PUBLIC/"index.html").resolve()]
        privacy=self.documents[(PUBLIC/"privacy/index.html").resolve()]
        deletion=self.documents[(PUBLIC/"delete-account/index.html").resolve()]
        self.check(home.meta("color-scheme")=="light","Homepage declares a light colour scheme")
        self.check(home.tags("title")[0].text().strip()=="Luma Drift 1.14 — Chaque action compte.","Initial title agrees with current French identity")
        self.check(all(ident in home.ids for ident in ("home","furniture","gameplay","download")),"House, collection, main puzzle and download sections all exist")
        source=home.path.read_text()
        self.check(not re.search(r"__APK_[A-Z0-9_]+__|TODO|TBD|TOKEN[A-Z]+",source),"Homepage has no unresolved release placeholders")
        self.check(not re.search(r"screenshots/v1\.(?:13|12|11|10)/|1\.13\.0-rc2",source),"Homepage does not advertise old captures or downloads")
        for lang in ("fr","en"):
            copy=BASE.normalized(home.root.visible_text(lang))
            for token in ("LumaCoins","LumaGold","12","24","240"):
                self.check(token in copy,"Current collection/currency fact appears in "+lang+": "+token)
            self.check(bool(re.search(r"\b1[\s,.\u00a0\u202f]?000\b",copy)),"1000 Journey boards visible in "+lang)
            for fr,en in (("30 premiers puzzles","30 opening puzzles"),("un retour par tentative","one undo per attempt"),("un essai bloqué ne coûte rien","a blocked try costs nothing"),("recommencer est gratuit","restarting is free"),("sans achat ni pouvoir","without purchases or powers")):
                self.check((fr if lang=="fr" else en).lower() in copy.lower(),"Specific 1.14 puzzle rule visible in "+lang+": "+en)
            for phrase in (("cinq objets","five things"),("sept autres","other seven"),("câlin","hug"),("trois pièces","three-piece"),("qualification","qualification"),("achats réels","real checkout")):
                self.check(phrase[0 if lang=="fr" else 1].lower() in copy.lower(),"Specific 1.14 scope/gate appears in "+lang+": "+phrase[1])
            legal=BASE.normalized(privacy.root.visible_text(lang))
            for token in ("7","30","UTC","LumaCoins","LumaGold"):
                self.check(token in legal,"Retained privacy obligation visible in "+lang+": "+token)
            for fr,en in (("désactivé par défaut","off by default"),("carnet d’adresses","address book"),("avant de supprimer","before deleting")):
                self.check((fr if lang=="fr" else en) in legal,"Retained privacy explanation: "+en+" / "+lang)
            dcopy=BASE.normalized(deletion.root.visible_text(lang))
            self.check(("d’abord les données serveur" if lang=="fr" else "first remove server data") in dcopy,"Deletion keeps server-before-identity order in "+lang)
        images=[n for n in home.tags("img") if "data-screenshot-fr" in n.attrs]
        self.check(len(images)==5,"Four product views plus the hero each have FR and EN sources")
        for image in images:
            for key in ("data-screenshot-fr","data-screenshot-en"):
                self.check(image.attrs[key].startswith("assets/screenshots/v1.14/"),"Localized product image uses current 1.14 evidence")
        self.check(any(n.attrs.get("src")=="assets/screenshots/v1.14/house-hug-en.png" for n in home.tags("img")),"Hug capture is displayed and its English locale is disclosed")
        script=(PUBLIC/"assets/site.js").read_text()
        for title in ("Luma Drift 1.14 — Chaque action compte.","Luma Drift 1.14 — Make every move count."):
            self.check(title in script,"Runtime title matches exact current edition: "+title)
        self.check("Luma Drift 1.12" not in script,"Language controller does not retain an obsolete product title")

    def release(self,home):
        path=ROOT/"releases"/("manifest-"+VERSION+".json")
        self.check(path.is_file(),"Frozen production 1.14 manifest exists")
        if not path.is_file():return
        manifest=json.loads(path.read_text());self.release_record=manifest
        artifact=manifest.get("artifacts",{}).get(APK_NAME,{})
        apk=ROOT/"releases"/APK_NAME
        self.check(manifest.get("version")==VERSION and manifest.get("version_code")==16 and manifest.get("application_id")=="com.androidhit.lumadrift","Frozen production artifact is version1.14.0-rc2/code16")
        expected=self.args.apk_sha256 or ""
        self.check(bool(re.fullmatch(r"[a-f0-9]{40}",self.args.source_commit or "")),"Independent immutable artifact commit supplied with --source-commit")
        self.check(bool(re.fullmatch(r"[a-f0-9]{64}",expected)),"Independent final production hash supplied with --apk-sha256")
        self.check(apk.is_file(),"Exact download APK exists locally")
        if apk.is_file():
            self.check(BASE.digest(apk)==artifact.get("sha256")==expected and apk.stat().st_size==artifact.get("bytes"),"Exact APK bytes and independent hash agree with frozen production manifest")
        if re.fullmatch(r"[a-f0-9]{40}",self.args.source_commit or ""):
            committed=subprocess.run(["git","show",self.args.source_commit+":releases/"+APK_NAME],cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.PIPE,timeout=30)
            self.check(committed.returncode==0 and hashlib.sha256(committed.stdout).hexdigest()==expected and len(committed.stdout)==artifact.get("bytes"),"Immutable artifact commit contains the exact verified APK bytes")
        cfg=manifest.get("configuration",{})
        self.check(cfg.get("paid_cosmetic_draws_enabled") is False and cfg.get("force_test_ads") is True and cfg.get("live_services_validated") is False and cfg.get("wallet_api_configured") is False,"Frozen APK keeps premium/checkout gates closed, test ads and unqualified live services")
        links=[node.attrs["href"] for node in home.tags("a") if ".apk" in node.attrs.get("href","")]
        self.check(len(links)>=2 and all(link==APK_PREFIX+(self.args.source_commit or "")+"/releases/"+APK_NAME for link in links),"Every APK action links to exact current production download")
        for lang in ("fr","en"):
            notes=BASE.normalized(" ".join(node.visible_text(lang) for node in home.nodes if node.has_class("download-note")))
            self.check(VERSION in notes and "16" in notes,"Exact release version and version code visible in "+lang)
            sizes=re.findall(r"(\d+(?:[.,]\d+)?)\s*(Mo|MB|Mio|MiB)\b",notes)
            self.check(bool(sizes),"Actual APK size visible in "+lang)
            if isinstance(artifact.get("bytes"),int):
                for value,unit in sizes:
                    actual=artifact["bytes"]/(1024**2 if unit in ("Mio","MiB") else 1e6)
                    self.check(abs(float(value.replace(",","."))-actual)<=.05,"Rounded APK size agrees with frozen bytes in "+lang)
                self.check(str(artifact["bytes"]) in notes.replace(" ",""),"Exact APK byte count visible in "+lang)
        integrity=" ".join(node.text() for node in home.nodes if node.has_class("integrity"))
        self.check(expected and expected in integrity,"Visible SHA256 equals independently frozen APK hash")
        self.check("RELEASE_1_14.md" in home.path.read_text(),"Release notes point at current 1.14 source")
        for release_path in (ROOT/"releases").glob("manifest-1.14*.json"):
            record=json.loads(release_path.read_text())
            if record.get("version_code")!=16 or record.get("version") not in (VERSION,LAB_VERSION):continue
            for name,entry in record.get("artifacts",{}).items():
                candidate=ROOT/"releases"/name
                if name not in (APK_NAME,"luma-drift-"+LAB_VERSION+".apk") or not candidate.is_file():continue
                if candidate.stat().st_size==entry.get("bytes") and BASE.digest(candidate)==entry.get("sha256"):
                    self.known_apks[entry["sha256"]]={"artifact":name,"manifest":str(release_path.relative_to(ROOT)),"package":"com.androidhit.lumadrift.sandbox" if "-sandbox" in name else "com.androidhit.lumadrift"}

    def configuration(self):
        super().configuration()
        previous=json.loads((ROOT/"docs/validation/site-v1.12/release/source-manifest.json").read_text())
        for name,expected in UNCHANGED.items():
            if not expected:expected=previous["files"]["/"+str(Path(name).relative_to("public"))]["sha256"]
            self.check(BASE.digest(ROOT/name)==expected,"Refresh preserves identity, legal, deletion, ads and existing Hosting bytes: "+name)
        policy_record=json.loads((ROOT/"docs/validation/site-v1.14/privacy-retained-policy.json").read_text())
        policy=self.documents[(PUBLIC/"privacy/index.html").resolve()]
        text_hash=lambda text:hashlib.sha256(BASE.normalized(text).encode()).hexdigest()
        self.check(policy_record.get("historical_policy_sha256")==previous["files"]["/privacy/index.html"]["sha256"],"Privacy retention baseline matches independently frozen 1.12 policy")
        self.check(BASE.digest(policy.path)==CURRENT_POLICY_SHA==policy_record.get("current_policy_sha256"),"Retained 1.13 privacy policy matches its reviewed source hash for this 1.14 refresh")
        for tag,field in (("p","retained_paragraph_sha256"),("heading","retained_heading_sha256")):
            actual=Counter(text_hash(node.text()) for node in policy.nodes if node.tag in (("h1","h2") if tag=="heading" else ("p",)))
            retained=Counter(policy_record.get(field,[]))
            self.check(bool(retained) and not (retained-actual),"Every frozen prior policy "+tag+" remains with its existing wording")
        for lang in ("fr","en"):
            copy=BASE.normalized(policy.root.visible_text(lang))
            for fr,en in (("meubles acquis","acquired furnishings"),("position du compagnon","companion’s position"),("trois pièces","three-piece"),("interactions dans la maison","home movement and interactions")):
                self.check((fr if lang=="fr" else en) in copy,"Policy discloses new local house/opening/journal state in "+lang+": "+en)
            self.check("1.13" in copy and ("28 octobre 2026" if lang=="fr" else "28 October 2026") in copy,"Retained 1.13 policy edition/date is explicit in "+lang)
        css=(PUBLIC/"assets/site.css").read_text()
        for token in ("[hidden]",":focus-visible","prefers-reduced-motion","scroll-behavior:auto","max-width:600px","max-width:360px"):
            self.check(token in css,"Current homepage includes accessibility/responsive rule: "+token)
        self.check(not re.search(r"@import|https?://|url\(",css),"Homepage CSS introduces no external resources or font requests")
        self.check("assets/site.css" in (PUBLIC/"index.html").read_text(),"Homepage actually loads its current stylesheet")
        for script in ("assets/site.js","site-config.js"):
            self.check(sum(node.attrs.get("src","").split("?",1)[0]==script for node in self.documents[(PUBLIC/"index.html").resolve()].tags("script"))==1,"Homepage loads each existing script once: "+script)

    def asset_sources(self):
        path=ROOT/"docs/validation/site-v1.14/screenshot-provenance.json"
        self.check(path.is_file(),"Current screenshot provenance manifest exists")
        if not path.is_file():return
        provenance=json.loads(path.read_text())
        self.check(provenance.get("schemaVersion")==1 and provenance.get("release")==VERSION and provenance.get("screenshotsEdited") is False,"Provenance identifies exact current release and unchanged pixels")
        declared={}
        for entry in provenance.get("images",[]):
            public=entry.get("public","");source=entry.get("source","");evidence=entry.get("evidence","");sha=entry.get("sha256","")
            self.check(public not in declared,"Unique provenance per public screenshot: "+public);declared[public]=entry
            target=(ROOT/public).resolve();original=(ROOT/source).resolve();report_path=(ROOT/evidence).resolve()
            safe=public.startswith("public/assets/screenshots/v1.14/") and source.startswith("docs/screenshots/v1.14/") and evidence.startswith("docs/validation/v1.14/") and all(ROOT.resolve() in p.parents for p in (target,original,report_path))
            self.check(safe,"Current image paths stay in their release-specific source/evidence directories: "+public)
            self.check(not any(token in source.lower() for token in ("analysis-crop","analysis_crop","/analysis/")),"Analysis crops cannot be promoted as native full-frame product captures: "+public)

            self.check(entry.get("locale") in ("fr","en") and bool(entry.get("description")),"Capture locale and scope explicitly declared: "+public)
            locale=re.search(r"-(fr|en)\.png$",public)
            self.check(locale and entry.get("locale")==locale.group(1),"Public filename and declared capture locale agree: "+public)
            source_locale=re.search(r"-(en|fr|de|es|pt|it)\.png$",source)
            if source_locale:self.check(entry.get("locale")==source_locale.group(1),"Original native capture filename and public locale agree: "+public)

            if not safe:continue
            exist=all(p.is_file() for p in (target,original,report_path));self.check(exist,"Public image, original capture and native evidence all exist: "+public)
            if not exist:continue
            self.check(BASE.digest(target)==BASE.digest(original)==sha,"Public image is byte-identical to its exact original native capture: "+public)
            self.native_input_files.update((source,evidence))

            raw=target.read_bytes();png=raw.startswith(b"\x89PNG\r\n\x1a\n") and len(raw)>=24
            self.check(png,"Capture is an actual PNG: "+public)
            if png:
                width,height=struct.unpack(">II",raw[16:24]);self.check((width,height) in ((1080,2340),(2340,1080),(1080,2400),(2400,1080)) and (entry.get("width"),entry.get("height"))==(width,height),"Original full-frame dimensions are retained: "+public)
            report=json.loads(report_path.read_text())
            self.check(report.get("passed") is True and report.get("restored") is True,"Native source run passed and original profile was restored: "+public)
            report_sha=report.get("apk_sha256") or report.get("game_apk_sha256")
            self.check(report_sha in self.known_apks,"Native report identifies a byte-verified current frozen APK: "+public)
            if report_sha in self.known_apks:
                self.native_input_files.update((self.known_apks[report_sha]["manifest"],"releases/"+self.known_apks[report_sha]["artifact"]))
            if report_sha in self.known_apks and report.get("package"):
                self.check(report["package"]==self.known_apks[report_sha]["package"],"Native report package agrees with its frozen APK manifest: "+public)
            records=report.get("screenshots",{})
            matches=[key for key,value in records.items() if value==sha and (source==key or source.endswith("/"+key))]
            self.check(bool(matches),"Passing host report indexes the exact original filename and image hash: "+public)
        referenced=set()
        for doc in self.documents.values():
            for image in doc.tags("img"):
                for attr in ("src","data-screenshot-fr","data-screenshot-en"):
                    value=image.attrs.get(attr,"")
                    if "/screenshots/" in value:
                        self.check("screenshots/v1.14/" in value,"Every rendered game capture belongs to current edition: "+value)
                        referenced.add(str((doc.path.parent/value.split("?",1)[0]).resolve().relative_to(ROOT)))
                        image_path=(doc.path.parent/value.split("?",1)[0]).resolve()
                        if image_path.is_file() and image.attrs.get("width","").isdigit() and image.attrs.get("height","").isdigit():
                            raw=image_path.read_bytes()
                            if raw.startswith(b"\x89PNG\r\n\x1a\n") and len(raw)>=24:
                                width,height=struct.unpack(">II",raw[16:24]);declared_width,declared_height=int(image.attrs["width"]),int(image.attrs["height"])
                                self.check(height>0 and declared_height>0 and abs(width/height-declared_width/declared_height)<.001,"Every FR/EN switched image retains its declared native aspect ratio: "+value)

        self.check(len(referenced)==9,"Nine exact native image files are referenced: four bilingual views plus English hug")
        self.check(referenced==set(declared),"Every displayed capture and every provenance entry have an exact counterpart")
        for file in BASE.payloads():
            if file.suffix.lower() not in (".png",".jpg",".webp",".svg"):continue
            name=str(file.relative_to(ROOT));sha=BASE.digest(file)
            if "/screenshots/v1.14/" in name:self.check(name in declared and declared[name].get("sha256")==sha,"Every new public screenshot is explicitly covered by provenance: "+name)
            if file.suffix.lower()==".svg":
                try:
                    svg=ET.fromstring(file.read_bytes());self.check("viewBox" in svg.attrib and not any(node.tag.rsplit("}",1)[-1] in ("script","foreignObject") for node in svg.iter()),"Vector is scalable and contains no embedded active content: "+name)
                except ET.ParseError:self.check(False,"Vector parses: "+name)
            self.assets.append({"public":name,"bytes":file.stat().st_size,"sha256":sha,"source_classification":"current-unedited-native-capture" if name in declared else "retained-historical-capture" if "/screenshots/" in name else "brand-art-or-decoration","exact_source":declared.get(name,{}).get("source"),"native_evidence":declared.get(name,{}).get("evidence")})

    def syntax_and_refs(self):
        super().syntax_and_refs()
        self.check(self.history_before==history_snapshot(),"All historical website checkers, tests and evidence remain byte-identical during check")
        if self.node:
            result=subprocess.run([self.node,"--test",str(PUBLIC/"tests/site-language-v114.test.mjs")],text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=20)
            self.check(result.returncode==0,"Current eight bilingual controller contracts pass")
            (self.out/"language-tests.txt").write_text(result.stdout)

    def run(self):
        passed=super().run();history_changed=self.history_before!=history_snapshot()
        path=self.out/"static-check.json";report=json.loads(path.read_text())
        report.update(edition="1.14",release=VERSION,release_ready=passed and not history_changed,historical_site_evidence_modified=history_changed,independent_apk_sha256=self.args.apk_sha256,artifact_source_commit=self.args.source_commit)
        if history_changed:report["status"]="FAIL";report["failures"].append("Historical website evidence changed during check")
        path.write_text(json.dumps(report,indent=2)+"\n")
        manifest_path=self.out/"source-manifest.json";manifest=json.loads(manifest_path.read_text())
        manifest.update(status=report["status"],edition="1.14",release=VERSION,production_apk_sha256=self.args.apk_sha256,artifact_source_commit=self.args.source_commit,runner_sha256=BASE.digest(Path(__file__)),base_runner_sha256=BASE.digest(ROOT/"scripts/check-site-v17.py"),release_manifest_sha256=BASE.digest(ROOT/"releases"/("manifest-"+VERSION+".json")) if self.release_record else None,screenshot_provenance_sha256=BASE.digest(ROOT/"docs/validation/site-v1.14/screenshot-provenance.json") if (ROOT/"docs/validation/site-v1.14/screenshot-provenance.json").is_file() else None,privacy_retention_sha256=BASE.digest(ROOT/"docs/validation/site-v1.14/privacy-retained-policy.json"),language_test_sha256=BASE.digest(PUBLIC/"tests/site-language-v114.test.mjs"),native_capture_inputs={name:BASE.digest(ROOT/name) for name in sorted(self.native_input_files)})
        manifest_path.write_text(json.dumps(manifest,indent=2)+"\n")
        return passed and not history_changed

if __name__=="__main__":
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-commit");parser.add_argument("--node");parser.add_argument("--apk-sha256");parser.add_argument("--report-dir",default=str(ROOT/"docs/validation/site-v1.14/release"));parser.add_argument("--self-test",action="store_true")
    arguments=parser.parse_args();BASE.parser_self_test()
    if arguments.self_test:print("PASS inherited language visibility, hidden-control and exact plus-tag parser regressions");sys.exit(0)
    sys.exit(0 if Audit(arguments).run() else 1)
