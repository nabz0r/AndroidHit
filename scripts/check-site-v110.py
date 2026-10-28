#!/usr/bin/env python3
"""Offline 1.10 website contracts. No browser, hosting-origin probe or external network request.

--phase source allows the published1.9 download and labelled historical captures
while native1.10 qualification is in progress. It is not release acceptance.
Default release mode requires the frozen1.10 artifact and exact passing screenshots.
"""
import argparse
import hashlib
import importlib.util
import json
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location("site_v17_contract_helpers",ROOT/"scripts/check-site-v17.py")
BASE=importlib.util.module_from_spec(spec);spec.loader.exec_module(BASE)
PUBLIC=ROOT/"public"
VERSION="1.10.0-rc3"
LAB_VERSION="1.10.0-sandbox-rc3"
APK_NAME="luma-drift-"+VERSION+".apk"
APK_URL="https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/"+APK_NAME

def contrast(a,b):
    def luminance(value):
        values=[int(value[i:i+2],16)/255 for i in (1,3,5)]
        values=[v/12.92 if v<=.04045 else ((v+.055)/1.055)**2.4 for v in values]
        return .2126*values[0]+.7152*values[1]+.0722*values[2]
    l1,l2=sorted((luminance(a),luminance(b)))
    return (l2+.05)/(l1+.05)

class Audit(BASE.Audit):
    def __init__(self,args):
        for edition in ("1.7", "1.8", "1.9"):
            history=(ROOT/("docs/validation/site-v"+edition)).resolve()
            try:
                Path(args.report_dir).resolve().relative_to(history)
            except ValueError:
                pass
            else:
                raise ValueError("The 1.10 checker cannot write into historical site evidence")
        self.historical_before=self.historical_snapshot()
        super().__init__(args)
    @staticmethod
    def historical_snapshot():
        indexed={}
        for edition in ("1.7", "1.8", "1.9"):
            folder=ROOT/("docs/validation/site-v"+edition)
            for path in folder.rglob("*"):
                if path.is_file():indexed[edition+"/"+str(path.relative_to(folder))]=BASE.digest(path)
        return indexed
    def release(self,home):
        links=[n.attrs["href"] for n in home.tags("a") if ".apk" in n.attrs.get("href","")]
        previous="https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.9.0-rc1.apk"
        expected=APK_URL if self.args.phase=="release" or APK_URL in links else previous
        self.check(len(links)>=2 and all(link==expected for link in links),"All APK links consistently name the actual current or preparation download")
        manifest_path=ROOT/"releases"/("manifest-"+VERSION+".json")
        artifact=None
        if self.args.phase=="release":
            self.check(manifest_path.is_file(),"Frozen1.10 release manifest exists")
            self.check(bool(re.fullmatch(r"[a-f0-9]{64}",self.args.apk_sha256 or "")),"Independent final production APK SHA256 supplied")
            if manifest_path.is_file():
                manifest=json.loads(manifest_path.read_text());artifact=manifest["artifacts"][APK_NAME];apk=ROOT/"releases"/APK_NAME
                self.check(manifest.get("version")==VERSION and manifest.get("version_code")==11 and manifest.get("application_id")=="com.androidhit.lumadrift","Release matches production1.10rc3/code11")
                self.check(apk.is_file() and BASE.digest(apk)==artifact["sha256"]==self.args.apk_sha256 and apk.stat().st_size==artifact["bytes"],"Download file bytes/hash equal independent freeze and manifest")
                config=manifest.get("configuration",{})
                self.check(config.get("paid_cosmetic_draws_enabled") is False and config.get("force_test_ads") is True and config.get("live_services_validated") is False,"Final build keeps real commerce/ads unqualified and paid draws closed")
        for lang in ("fr","en"):
            copy=BASE.normalized(home.root.visible_text(lang))
            notes=BASE.normalized(" ".join(n.visible_text(lang) for n in home.nodes if n.has_class("download-note")))
            visible_version=VERSION if expected==APK_URL else "1.9.0-rc1"
            self.check(visible_version in notes,"Visible exact download version in "+lang)
            self.check(self.args.phase=="source" or "TOKEN" not in notes,"No APK-size placeholder in release copy: "+lang)
            if expected==previous:self.check(("1.10 en préparation" if lang=="fr" else "1.10 in preparation") in copy,"Source page discloses preparation rather than a1.10 release in "+lang)
            if artifact:
                sizes=re.findall(r"(\d+(?:[.,]\d+)?)\s*(Mo|MB|Mio|MiB)\b",notes)
                self.check(bool(sizes),"Release copy discloses actual APK size in "+lang)
                for value,unit in sizes:
                    actual=artifact["bytes"]/(1024**2 if unit in ("Mio","MiB") else 1e6)
                    self.check(abs(float(value.replace(",","."))-actual)<=.05,"Visible APK size matches frozen bytes in "+lang)
        source=(PUBLIC/"index.html").read_text()
        if self.args.phase=="release":
            self.check(not re.search(r"TOKEN[A-Z]+",source),"No unfinished release tokens in HTML")
            self.check("1.10 en préparation" not in source and "1.10 in preparation" not in source,"Preparation copy removed only after current acceptance")

    def edition_contract(self):
        home=self.documents[(PUBLIC/"index.html").resolve()]
        privacy=self.documents[(PUBLIC/"privacy/index.html").resolve()]
        deletion=self.documents[(PUBLIC/"delete-account/index.html").resolve()]
        self.check(home.meta("color-scheme")=="light","Homepage advertises the real light theme")
        self.check("tutorial" in home.ids and home.ids["tutorial"].tag=="section","Tutorial is a distinct navigable section")
        lessons=[node for node in home.nodes if node.has_class("lesson-list")]
        self.check(len(lessons)==1 and len([n for n in lessons[0].children if isinstance(n,BASE.Node) and n.tag=="li"])==7,"Seven actual tutorial mechanisms described")
        for lang in ("fr","en"):
            copy=BASE.normalized(home.root.visible_text(lang))
            for token in ("LumaCoins","LumaGold","150","240","24","18","10","7","25 / 60 / 100"):
                self.check(token in copy,"Current economy/catalog number visible in "+lang+": "+token)
            self.check(bool(re.search(r"\b1[\s,.\u00a0\u202f]?000\b",copy)),"1000 Journey boards disclosed in "+lang)
            self.check("L/M" in copy,"New route family distinguished from preserved historical boards in "+lang)
            self.check(("pour 1 coup" if lang=="fr" else "costs 1 move") in copy,"Manual gate opening has explicit action cost in "+lang)
            self.check(("Annuler est gratuit et rend le coup" if lang=="fr" else "Undo is free and refunds") in copy,"Undo refund rule explicit in "+lang)
            self.check(("avant confirmation" if lang=="fr" else "Before confirmation") in copy,"Immediate switch preview before commitment in "+lang)
            self.check(("100 LumaCoins + 2 Étincelles" if lang=="fr" else "100 LumaCoins + 2 Sparks") in copy,"Exact one-time tutorial gift in "+lang)
            self.check(("cadeau unique" if lang=="fr" else "one-time gift") in copy,"Practice reward cannot be farmed by replay in "+lang)
            self.check(("aucun niveau du parcours modifié" if lang=="fr" else "no campaign level changed") in copy.lower(),"Practice is separate from campaign in "+lang)
            self.check(("deux comptes" if lang=="fr" else "two-account") in copy and "qualification" in copy,"Real friend qualification remains unclaimed in "+lang)
            self.check(("achats réels" if lang=="fr" else "real checkout") in copy,"Real checkout qualification remains explicit in "+lang)
            for stale in ("Des coups en plus ne rouvrent pas un portail","Extra moves do not reopen a gate"):
                self.check(stale not in copy,"Obsolete permanent-gate-dead-end copy absent in "+lang)
            self.check(("7,5" if lang=="fr" else "7.5") in copy and ("3,75" if lang=="fr" else "3.75") in copy and ("1,25" if lang=="fr" else "1.25") in copy,"Per-item earned capsule odds retained in "+lang)
            legal=BASE.normalized(privacy.root.visible_text(lang))
            for token in ("7","30","UTC","LumaCoins","LumaGold"):self.check(token in legal,"Existing privacy retention/currency disclosure retained in "+lang+": "+token)
            self.check(("désactivé par défaut" if lang=="fr" else "off by default") in legal,"Social position opt-in remains off by default in "+lang)
            self.check(("carnet d’adresses" if lang=="fr" else "address book") in legal,"Address-book exclusion retained in "+lang)
            self.check(("avant de supprimer" if lang=="fr" else "before deleting") in legal,"Server-before-Firebase cleanup retained in "+lang)
            dcopy=BASE.normalized(deletion.root.visible_text(lang))
            self.check(("d’abord les données serveur" if lang=="fr" else "first remove server data") in dcopy,"Deletion instructions preserve server-first order in "+lang)
        for lang in ("fr", "en"):
            copy=BASE.normalized(home.root.visible_text(lang))
            for token in ("288", "12", "100", "50"):
                self.check(token in copy,"Preserved catalogue/milestone number visible in "+lang+": "+token)
            self.check("guardians" in home.ids,"Optional encounters have a navigable section")
            self.check(("facultatives" if lang=="fr" else "optional") in copy,"Encounters remain optional in "+lang)
            self.check(("tous les 10" if lang=="fr" else "per 10") in copy,"Encounter unlock interval disclosed in "+lang)
            self.check(("2 couches" if lang=="fr" else "2 layers") in copy,"Actual two-layer rule disclosed in "+lang)
            self.check(("case par case" if lang=="fr" else "one cell per action") in copy,"Cellwise encounters disclosed in "+lang)
            self.check(("gardien 5" if lang=="fr" else "guardian 5") in copy,"Cellwise start at guardian five in "+lang)
            self.check(("règles restent inchangées" if lang=="fr" else "rules stay unchanged") in copy,"Published Journey and Remix rules preserved in "+lang)
            self.check(("traitements graphiques sont gratuits" if lang=="fr" else "visual treatments are free") in copy,"All visual treatments free in "+lang)
            self.check(("variantes combinatoires" if lang=="fr" else "combinatorial variants") in copy,"288 combinations are not hand-painted-asset claim in "+lang)
            self.check(("ne débloque pas un compagnon" if lang=="fr" else "does not unlock a companion") in copy,"Treatment choice preserves character ownership in "+lang)
            self.check(("cinq poses" if lang=="fr" else "five poses") in copy,"Five companion poses disclosed in "+lang)
            self.check(("progression réelle" if lang=="fr" else "real progress") in copy,"Backdrop uses actual progression in "+lang)
            self.check(("mouvement réduit" if lang=="fr" else "reduced motion") in copy,"Reduced motion disclosed in "+lang)
            self.check(("essai offert de 1 charge" if lang=="fr" else "free 1-charge trial") in copy,"Power trial amount explicit in "+lang)
            self.check(("sans pénalité pour les indices ni Annuler" if lang=="fr" else "no penalty for hints or Undo") in copy,"Scoring ignores hint and Undo counters in "+lang)
            self.check(("stock plein" if lang=="fr" else "full inventory") in copy,"Unclaimed gift retained at full stock in "+lang)
            if self.args.phase=="source" and any("assets/screenshots/v1.9/" in n.attrs.get("src","") for n in home.tags("img")):
                self.check(("captures historiques 1.9" if lang=="fr" else "historical 1.9 captures") in copy,"Source phase explicitly awaits current native captures in "+lang)
        self.check(("RELEASE_1_10.md" if self.args.phase=="release" else "UX_1_10.md") in (PUBLIC/"index.html").read_text(),"Current notes link uses1.10 documentation")
        self.check("objectives" in home.ids,"Companion and objectives have a distinct section")
        for lang in ("fr","en"):
            copy=BASE.normalized(home.root.visible_text(lang))
            for token in ("42","LumaCoins","LumaGold"):
                self.check(token in copy,"1.10 actual catalog/currency figure visible in "+lang+": "+token)
            for fr,en in (("Quatre emplacements cosmétiques et trois équipements","Four cosmetic slots and three equipment slots"),("Épingle un objet","Pin an item"),("Voir la source n’effectue aucun achat","Viewing a source makes no purchase"),("Dix chapitres","Ten chapters"),("ne crée ni récompense","creates no reward")):
                self.check((fr if lang=="fr" else en) in copy,"1.10 truthful UX contract in "+lang+": "+(fr if lang=="fr" else en))
        localized=[n for n in home.tags("img") if "data-screenshot-fr" in n.attrs]
        self.check(len(localized)>=5,"Native image elements retain FR/EN alternatives")
        for node in localized:
            for attr in ("data-screenshot-fr","data-screenshot-en"):
                path=node.attrs[attr]
                self.check("pilot" not in path,"No pilot image presented as product capture")
                self.check(path.startswith("assets/screenshots/v1.10/") if self.args.phase=="release" else path.startswith(("assets/screenshots/v1.10/","assets/screenshots/v1.9/")),"Screenshot edition matches check phase")
        if any("v1.9/" in n.attrs.get("src","") for n in localized):
            for lang in ("fr","en"):
                copy=BASE.normalized(home.root.visible_text(lang));self.check(("historiques" if lang=="fr" else "historical") in copy,"Preparation explicitly labels historical screenshots in "+lang)
        site=(PUBLIC/"assets/site.js").read_text()
        self.check("Luma Drift 1.10" in site and "Luma Drift 1.9" not in site,"Runtime language title matches1.10")

    def asset_sources(self):
        indexed={};runtime={}
        for version in ("1.7","1.8","1.9","1.10"):
            if version=="1.10":
                # Intermediate rc1/rc2 reports cannot qualify the corrected rc3, even when passing.
                apks={BASE.digest(p) for p in (ROOT/"releases"/("luma-drift-"+VERSION+".apk"),ROOT/"releases"/("luma-drift-"+LAB_VERSION+".apk")) if p.is_file()}
            else:
                apks={BASE.digest(p) for p in (ROOT/"releases").glob("luma-drift-"+version+".0*.apk")}
            folder=ROOT/"docs/screenshots"/("v"+version)
            if version=="1.10":directories=[p for p in folder.glob("ui-sdk-*") if p.is_dir()]
            elif version=="1.9":directories=list(folder.glob("ui-sdk-final5-*"))+[folder/"ui-sdk-fresh-guest-d"]
            else:directories=list(folder.glob("ui-sdk-final-*"))
            for directory in directories:
                report_path=ROOT/"docs/validation"/("v"+version)/directory.name/"host-report.json"
                if not report_path.is_file():continue
                report=json.loads(report_path.read_text())
                is_fresh=report.get("fresh_lab_package_absent_before_install") is True
                preserved=(report.get("new_guest_test_profile_retained") is True and report.get("production_state_unchanged") is True and report.get("installed_apk_sha256")==report.get("apk_sha256")) if is_fresh else report.get("restored") is True
                if report.get("passed") is not True or not preserved or report.get("apk_sha256") not in apks:continue
                native_path=report_path.with_name("report.json")
                native=json.loads(native_path.read_text()) if native_path.is_file() else {}
                if version in ("1.9","1.10") and native.get("passed") is not True:continue
                for source in directory.glob("*.png"):
                    sha=BASE.digest(source)
                    if report.get("screenshots",{}).get(source.name)!=sha:continue
                    snapshot_path=report_path.with_name(source.stem+".json")
                    snapshot=json.loads(snapshot_path.read_text()) if snapshot_path.is_file() else None
                    capture_sequence=native.get("captures",[])
                    if version in ("1.9","1.10") and (source.stem not in capture_sequence or not isinstance(snapshot,list)):continue
                    progress_labels=[item.get("name","") for item in snapshot or [] if isinstance(item,dict) and re.search(r"\b\d+\s*/\s*1[\s,.\u00a0\u202f]?000\b",item.get("name",""))]
                    key=str(source.relative_to(ROOT));indexed.setdefault(sha,[]).append(key)
                    runtime[key]={"report":str(report_path.relative_to(ROOT)),"native_report":str(native_path.relative_to(ROOT)) if native_path.is_file() else None,"apk_sha256":report["apk_sha256"],"package":report.get("package"),"profile_scope":report.get("scope"),"profile_preservation":"New guest retained after a proven first Lab installation; there was no original profile to restore." if is_fresh else "Original preferences restored byte-identically after the run.","run_cases":report.get("cases"),"journey_through_requested":report.get("journey_through"),"capture_sequence_index":capture_sequence.index(source.stem) if source.stem in capture_sequence else None,"accessibility_snapshot":str(snapshot_path.relative_to(ROOT)) if snapshot_path.is_file() else None,"accessibility_snapshot_sha256":BASE.digest(snapshot_path) if snapshot_path.is_file() else None,"observed_progress_labels":progress_labels,"progress_scope":"Observed labels and capture order only; no earned progress inferred from a filename or requested run-through level."}
        for directory in (ROOT/"docs/screenshots/v1.6",ROOT/"docs/screenshots/v1.5",ROOT/"docs/store",ROOT/"app/src/main/res/drawable-nodpi"):
            if directory.exists():
                for source in directory.rglob("*"):
                    if source.is_file() and source.suffix.lower() in (".png",".svg",".webp",".jpg"):indexed.setdefault(BASE.digest(source),[]).append(str(source.relative_to(ROOT)))
        current_referenced=set()
        for home in self.documents.values():
            for node in home.tags("img"):
                for attr in ("src","data-screenshot-fr","data-screenshot-en"):
                    value=node.attrs.get(attr,"")
                    if value.startswith("assets/screenshots/v1.10/"):current_referenced.add("public/"+value)
        verified_current=set()
        for path in BASE.payloads():
            if path.suffix.lower() not in (".png",".svg",".webp",".jpg"):continue
            sha=BASE.digest(path);matches=indexed.get(sha,[]);relative=str(path.relative_to(ROOT))
            if "/screenshots/" in relative:self.check(bool(matches),"Unedited native screenshot has exact passing recorded source: "+relative)
            current=[x for x in matches if x.startswith("docs/screenshots/v1.10/") and x in runtime]
            if "/screenshots/v1.10/" in relative:
                self.check(bool(current),"Current1.10 screenshot matches exact frozen APK and passing preserved native record: "+relative)
                if current:verified_current.add(relative)
            if path.suffix.lower()==".svg":
                try:
                    svg=ET.fromstring(path.read_bytes());self.check("viewBox" in svg.attrib,"Vector viewBox: "+relative)
                    self.check(not any(n.tag.rsplit("}",1)[-1] in ("script","foreignObject") for n in svg.iter()),"Vector has no active embedded content: "+relative)
                except ET.ParseError:self.check(False,"Vector parses: "+relative)
            family="current-sdk-1.10" if current else "historical-native-source" if "/screenshots/" in relative else "brand-illustration"
            self.assets.append({"public":relative,"bytes":path.stat().st_size,"sha256":sha,"exact_sources":matches,"source_classification":family,"passing_runtime_sources":[runtime[x] for x in matches if x in runtime]})
        if self.args.phase=="release":
            self.check(len(current_referenced)>=5,"Release actually references at least five current native images")
            self.check(current_referenced<=verified_current,"Every referenced current screenshot has complete frozen-runtime provenance")

    def configuration(self):
        super().configuration()
        css=(PUBLIC/"assets/style.css").read_text()
        for token in ("color-scheme:light","--bg:#fff8f0","--text:#463252","brand/dream-glade-v18.png",".tutorial-section",".lesson-list",".document .delete-panel"):
            self.check(token in css,"Light native-matched site design includes "+token)
        pairs=[("#463252","#fff8f0"),("#75637c","#fff8f0"),("#75637c","#f3ecfc"),("#337568","#fff8f0"),("#765090","#fff8f0"),("#256a59","#e3f8ec"),("#8d314d","#ffe4eb"),("#70541d","#fff3d6")]
        for text,bg in pairs:self.check(contrast(text,bg)>=4.5,"Declared solid text/surface contrast >=4.5: "+text+" on "+bg+" = "+str(round(contrast(text,bg),2)))
    def syntax_and_refs(self):
        super().syntax_and_refs()
        self.check(self.historical_before==self.historical_snapshot(),"Historical1.7/1.8/1.9 website evidence remains byte-identical during this check")
    def run(self):
        passed=super().run();path=self.out/"static-check.json";report=json.loads(path.read_text())
        report.update({"edition":"1.10","phase":self.args.phase,"release_ready":passed and self.args.phase=="release","historical_site_reports_modified":self.historical_before!=self.historical_snapshot()})
        path.write_text(json.dumps(report,indent=2)+"\n")
        if self.args.phase=="source":print("SOURCE PREPARATION ONLY: current1.10 artifact and native captures still require release mode; no publication or runtime acceptance is inferred.")
        return passed

if __name__=="__main__":
    p=argparse.ArgumentParser(description=__doc__);p.add_argument("--node");p.add_argument("--apk-sha256");p.add_argument("--phase",choices=("source","release"),default="release");p.add_argument("--report-dir");p.add_argument("--self-test",action="store_true");args=p.parse_args()
    BASE.parser_self_test()
    if args.self_test:
        assert contrast("#463252","#fff8f0")>10
        print("PASS inherited bilingual DOM visibility/plus-tag parser and light contrast regressions");sys.exit(0)
    if not args.report_dir:args.report_dir=str(ROOT/"docs/validation/site-v1.10"/("source-prepare" if args.phase=="source" else "release"))
    sys.exit(0 if Audit(args).run() else 1)
