#!/usr/bin/env python3
"""Offline site 1.7 acceptance: files/DOM/copy/provenance/release consistency, never an HTTP client.

Python standard library + local Node for syntax/config evaluation. No Firebase/GitHub origin is
requested. Browser rendering, network isolation, live providers and Hosting remain separate checks.
"""
import argparse
import hashlib
from html.parser import HTMLParser
import json
from pathlib import Path
import re
import shutil
import struct
import subprocess
import sys
import tempfile
from urllib.parse import unquote, urlsplit
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
PUBLIC=ROOT/"public"
VERSION="1.7.0-rc1"
APK_NAME="luma-drift-"+VERSION+".apk"
APK_SHA="7eeaa816853533f441acfc73f4ad3f6af0fb2cbdd6fd0eae1fda3f64c388a2bf"  # Independently supplied signed production artifact at release freeze.
APK_URL="https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/"+APK_NAME
CONTACT="nabz0r+gaming@gmail.com"
VOID={"area","base","br","col","embed","hr","img","input","link","meta","param","source","track","wbr"}

class Node:
    def __init__(self,tag="document",attrs=(),parent=None,line=0):
        self.tag=tag;self.attrs=dict(attrs);self.parent=parent;self.children=[];self.line=line
    def descendants(self):
        for child in self.children:
            if isinstance(child,Node):yield child;yield from child.descendants()
    def visible(self,lang):
        node=self
        while node is not None:
            if node.attrs.get("data-lang") not in (None,lang):return False
            if "hidden" in node.attrs and node.attrs.get("data-lang") is None:return False
            node=node.parent
        return True
    def visible_text(self,lang):
        if not self.visible(lang):return ""
        if self.tag in ("script","style"):return ""
        return " ".join(child.visible_text(lang) if isinstance(child,Node) else child for child in self.children)
    def text(self):return " ".join(child.text() if isinstance(child,Node) else child for child in self.children)
    def has_class(self,name):return name in self.attrs.get("class","").split()

class Document(HTMLParser):
    def __init__(self,path):
        super().__init__(convert_charrefs=True);self.path=path;self.root=Node();self.stack=[self.root];self.doctype=False;self.duplicates=[]
        self.feed(path.read_text());self.nodes=list(self.root.descendants());self.ids={}
        for node in self.nodes:
            ident=node.attrs.get("id")
            if ident:
                if ident in self.ids:self.duplicates.append(ident)
                self.ids[ident]=node
    def handle_decl(self,decl):self.doctype|=decl.lower()=="doctype html"
    def handle_starttag(self,tag,attrs):
        node=Node(tag,attrs,self.stack[-1],self.getpos()[0]);self.stack[-1].children.append(node)
        if tag not in VOID:self.stack.append(node)
    def handle_startendtag(self,tag,attrs):
        self.handle_starttag(tag,attrs)
        if tag not in VOID:self.handle_endtag(tag)
    def handle_endtag(self,tag):
        for index in range(len(self.stack)-1,0,-1):
            if self.stack[index].tag==tag:del self.stack[index:];break
    def handle_data(self,data):self.stack[-1].children.append(data)
    def tags(self,tag):return [node for node in self.nodes if node.tag==tag]
    def meta(self,key):
        return next((n.attrs.get("content","") for n in self.tags("meta") if n.attrs.get("name",n.attrs.get("property",n.attrs.get("http-equiv",""))).lower()==key.lower()),"")

def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def payloads():
    return sorted(p for p in PUBLIC.rglob("*") if p.is_file() and "tests" not in p.relative_to(PUBLIC).parts and not any(part.startswith(".") for part in p.relative_to(PUBLIC).parts))
def snapshot():return {str(p.relative_to(ROOT)):{"bytes":p.stat().st_size,"sha256":digest(p)} for p in payloads()+[ROOT/"firebase.json"]}
def normalized(value):return re.sub(r"\s+"," ",value).strip()
def accessible_name(node,doc,lang):
    if node.attrs.get("aria-label"):return node.attrs["aria-label"]
    if node.attrs.get("aria-labelledby"):return " ".join(doc.ids[x].visible_text(lang) for x in node.attrs["aria-labelledby"].split() if x in doc.ids)
    text=node.visible_text(lang)
    for child in node.descendants():
        if child.tag=="img":text+=" "+child.attrs.get("data-alt-"+lang,child.attrs.get("alt",""))
    return normalized(text)

class Audit:
    def __init__(self,args):
        self.args=args;self.out=Path(args.report_dir);self.out.mkdir(parents=True,exist_ok=True)
        self.checks=[];self.assets=[];self.references=[];self.documents={};self.start=snapshot()
        self.node=args.node or shutil.which("node")
    def check(self,condition,message):
        self.checks.append({"pass":bool(condition),"message":message})
    def read_config(self):
        if not self.node:self.check(False,"Local Node executable available for syntax/config checks");return {}
        program="""const fs=require('node:fs'),vm=require('node:vm');const scope={window:{}};vm.runInNewContext(fs.readFileSync(process.argv[1],'utf8'),scope,{timeout:1000,contextCodeGeneration:{strings:false,wasm:false}});process.stdout.write(JSON.stringify(scope.window.LUMA_SITE_CONFIG));"""
        try:return json.loads(subprocess.check_output([self.node,"-e",program,str(PUBLIC/"site-config.js")],text=True,stderr=subprocess.STDOUT,timeout=10))
        except Exception as error:self.check(False,"Public configuration evaluates locally: "+str(error));return {}
    def local_reference(self,reference,source):
        parsed=urlsplit(reference)
        if parsed.scheme in ("mailto","tel","data"):return
        if parsed.scheme and parsed.scheme not in ("https","http"):
            self.check(False,"No unsafe reference scheme: "+reference);return
        if parsed.scheme=="http":self.check(False,"External web links use HTTPS: "+reference)
        if parsed.netloc:
            # Absolute Hosting links are checked against local files, never fetched.
            if parsed.netloc!="luma-drift-production.web.app":return
        elif parsed.scheme:return
        path=unquote(parsed.path)
        target=(PUBLIC/path.lstrip("/")) if path.startswith("/") or parsed.netloc else source.parent/path if path else source
        if target.is_dir():target/="index.html"
        resolved=target.resolve()
        try:resolved.relative_to(PUBLIC.resolve())
        except ValueError:self.check(False,"Reference stays inside public/: "+reference);return
        self.references.append({"from":str(source.relative_to(PUBLIC)),"reference":reference,"target":str(resolved.relative_to(PUBLIC))})
        self.check(resolved.is_file(),"Local reference exists: "+str(source.relative_to(PUBLIC))+" → "+reference)
        if parsed.fragment and resolved.suffix==".html" and resolved.is_file():
            doc=self.documents.get(resolved) or Document(resolved)
            self.check(unquote(parsed.fragment) in doc.ids,"Local fragment exists: "+reference)
    def html(self,path):
        doc=Document(path);self.documents[path.resolve()]=doc;name=str(path.relative_to(PUBLIC));source=path.read_text()
        self.check(doc.doctype,name+": HTML5 doctype")
        self.check(len(doc.tags("html"))==1 and doc.tags("html")[0].attrs.get("lang")=="fr",name+": initial document language is French")
        self.check(not doc.duplicates,name+": unique DOM IDs "+repr(doc.duplicates))
        self.check(len(doc.tags("main"))==1 and "main" in doc.ids,name+": one main landmark and skip target")
        for lang in ("fr","en"):
            self.check(sum(bool(n.visible_text(lang).strip()) for n in doc.tags("h1"))==1,name+": one visible H1 in "+lang)
        self.check(bool(doc.tags("title")) and bool(doc.tags("title")[0].text().strip()),name+": meaningful page title")
        self.check("width=device-width" in doc.meta("viewport"),name+": responsive viewport without blocked zoom")
        self.check(not re.search(r'user-scalable\s*=\s*(?:no|0)|maximum-scale\s*=\s*1(?:[,;\s]|$)',doc.meta("viewport")),name+": zoom is not disabled")
        self.check(doc.meta("referrer")=="no-referrer",name+": no-referrer meta")
        csp=doc.meta("Content-Security-Policy")
        for rule in ("default-src 'self'","object-src 'none'","base-uri 'self'"):
            self.check(rule in csp,name+": CSP "+rule)
        self.check("'unsafe-inline'" not in csp and "'unsafe-eval'" not in csp,name+": no unsafe inline/eval CSP exception")
        buttons={n.attrs.get("data-set-lang"):n for n in doc.tags("button") if "data-set-lang" in n.attrs}
        self.check(set(buttons)=={"fr","en"},name+": FR/EN switch buttons")
        self.check(buttons.get("fr",Node()).attrs.get("aria-pressed")=="true" and buttons.get("en",Node()).attrs.get("aria-pressed")=="false",name+": initial language button state")
        groups={}
        for node in doc.nodes:
            if "data-lang" in node.attrs:
                lang=node.attrs["data-lang"];self.check(lang in ("fr","en") and node.attrs.get("lang")==lang,name+": translated span has explicit matching language")
                groups.setdefault(id(node.parent),[]).append(lang)
                self.check(("hidden" in node.attrs)==(lang=="en"),name+": inactive initial language hidden")
            for attr in node.attrs:
                self.check(not re.match(r'^on[a-z]+$',attr),name+": no inline handler "+attr)
            if node.tag in ("a","button"):
                for lang in ("fr","en"):
                    if not node.visible(lang):continue
                    self.check(bool(accessible_name(node,doc,lang)),name+": accessible "+node.tag+" name in "+lang)
            if node.tag=="button":self.check(node.attrs.get("type")=="button",name+": explicit non-submit button type")
            if "data-content-fr" in node.attrs:
                self.check(bool(node.attrs.get("data-content-en")),name+": paired localized metadata content")
                self.check(node.attrs.get("content")==node.attrs["data-content-fr"],name+": metadata starts in the initial French locale")
            for aria in ("aria-labelledby","aria-describedby","aria-controls"):
                if node.attrs.get(aria):self.check(all(x in doc.ids for x in node.attrs[aria].split()),name+": "+aria+" resolves")
            if node.tag=="img":
                self.check("alt" in node.attrs,name+": image alt provided")
                if any(key in node.attrs for key in ("data-alt-fr","data-alt-en")):
                    self.check(bool(node.attrs.get("data-alt-fr")) and bool(node.attrs.get("data-alt-en")),name+": paired FR/EN image alt")
                if "data-screenshot-fr" in node.attrs:
                    self.check(bool(node.attrs.get("data-screenshot-en")),name+": paired localized screenshot paths")
                    self.check(node.attrs.get("src")==node.attrs["data-screenshot-fr"],name+": initial screenshot matches initial French locale")
                self.check(bool(node.attrs.get("width")) and bool(node.attrs.get("height")),name+": image dimensions reserved")
                image_source=node.attrs.get("src","")
                if image_source and not urlsplit(image_source).netloc and image_source.split("?",1)[0].endswith(".png"):
                    image=path.parent/image_source.split("?",1)[0]
                    if image.is_file():
                        raw=image.read_bytes();valid=raw.startswith(b"\x89PNG\r\n\x1a\n") and len(raw)>=24
                        self.check(valid,name+": valid PNG header "+image_source)
                        if valid and "screenshots/" in image_source and node.attrs.get("width","").isdigit() and node.attrs.get("height","").isdigit():
                            width,height=struct.unpack(">II",raw[16:24]);declared_width,declared_height=int(node.attrs["width"]),int(node.attrs["height"])
                            self.check(height>0 and declared_height>0 and abs(width/height-declared_width/declared_height)<.001,name+": reserved aspect ratio matches actual PNG "+image_source)
            if node.tag=="script":
                self.check(not node.text().strip(),name+": no inline JavaScript")
                if node.attrs.get("src"):self.check(not urlsplit(node.attrs["src"]).netloc,name+": scripts load from local origin before explicit account action")
            for attr in ("href","src","poster","data-screenshot-fr","data-screenshot-en"):
                if node.attrs.get(attr):self.local_reference(node.attrs[attr],path)
            href=node.attrs.get("href","")
            if href.startswith("mailto:"):
                self.check(unquote(href[7:].split("?",1)[0])==CONTACT,name+": exact plus-tag mail recipient")
                self.check("%2b" in href.lower(),name+": literal mail plus tag is encoded")
        for values in groups.values():self.check(values.count("fr")==values.count("en") and bool(values),name+": sibling language parity")
        for lang in ("fr","en"):
            self.check(any(n.tag=="a" and n.attrs.get("href")=="#main" and accessible_name(n,doc,lang) for n in doc.nodes),name+": skip link in "+lang)
            visible=normalized(doc.root.visible_text(lang))
            self.check("nabilux" in visible and CONTACT in visible,name+": static contact fallback in "+lang)
            notices=[n for n in doc.nodes if "data-preparation-notice" in n.attrs]
            self.check(bool(notices) and any(n.visible_text(lang).strip() for n in notices),name+": qualification notice in "+lang)
            updated=" ".join(n.visible_text(lang) for n in doc.nodes if n.has_class("updated"))
            if updated:self.check(not re.search(r'\b1\.5(?:\.0)?\b',updated),name+": current legal-page edition does not retain release1.5 in "+lang)
        if name=="delete-account/index.html":
            for ident in ("google-sign-in","delete-account","delete-confirmation","confirm-delete","deletion-status","cancel-delete"):
                self.check(ident in doc.ids,name+": explicit deletion control "+ident)
            self.check("disabled" in doc.ids.get("google-sign-in",Node()).attrs and "disabled" in doc.ids.get("delete-account",Node()).attrs,name+": destructive/auth controls fail closed before JS configuration")
            self.check("hidden" in doc.ids.get("delete-confirmation",Node()).attrs,name+": destructive confirmation initially hidden")
            status=doc.ids.get("deletion-status",Node());self.check(status.attrs.get("role")=="status" and status.attrs.get("aria-live")=="polite",name+": deletion status announced politely")
        return doc
    def release(self,home):
        manifest=json.loads((ROOT/"releases"/("manifest-"+VERSION+".json")).read_text());artifact=manifest["artifacts"][APK_NAME];apk=ROOT/"releases"/APK_NAME
        self.check(manifest["version"]==VERSION and manifest["version_code"]==8 and manifest["application_id"]=="com.androidhit.lumadrift","Release is current production1.7/code8")
        self.check(bool(re.fullmatch(r"[a-f0-9]{64}",self.args.apk_sha256 or APK_SHA or "")),"Independent final APK hash supplied at freeze")
        self.check(digest(apk)==artifact["sha256"]==(self.args.apk_sha256 or APK_SHA) and apk.stat().st_size==artifact["bytes"],"Download target local APK bytes/hash match exact release manifest")
        self.check(manifest["configuration"].get("paid_cosmetic_draws_enabled") is False,"Final APK manifest keeps the premium cosmetic client gate closed")
        self.check(manifest["configuration"].get("force_test_ads") is True and manifest["configuration"].get("live_services_validated") is False,"Release still declares test ads / live services unqualified")
        links=[n.attrs["href"] for n in home.tags("a") if ".apk" in n.attrs.get("href","")]
        self.check(len(links)>=2 and all(x==APK_URL for x in links),"All download actions point at the exact current signed APK URL")
        for key in ("description","og:description"):
            self.check(not re.search(r'\b1\.5(?:\.0)?\b',home.meta(key)),"Homepage "+key+" does not advertise release1.5")
        for lang in ("fr","en"):
            notes=[n for n in home.nodes if n.has_class("download-note")]
            copy=normalized(" ".join(n.visible_text(lang) for n in notes))
            self.check(VERSION in copy,"Visible download version exact in "+lang)
            sizes=re.findall(r'(\d+(?:[.,]\d+)?)\s*(Mo|MB|Mio|MiB)\b',copy)
            valid=[]
            for value,unit in sizes:
                amount=float(value.replace(",","."));actual=artifact["bytes"]/(1024**2 if unit in ("Mio","MiB") else 1e6)
                tolerance=.5 if re.fullmatch(r'\d+',value) else .15;valid.append(abs(amount-actual)<=tolerance)
            self.check(not sizes or all(valid),"Any visible download size matches current APK rounding in "+lang)
            visible=normalized(home.root.visible_text(lang))
            for number in ("240","24","18","10","7"):
                self.check(bool(re.search(r'\b'+number+r'\b',visible)),"Current catalog count "+number+" visible in "+lang)
            self.check(bool(re.search(r'\b1[\s,.\u00a0\u202f]?000\b',visible)),"1000 Journey boards visible in "+lang)
    def edition_contract(self):
        home=self.documents[(PUBLIC/"index.html").resolve()]
        privacy=self.documents[(PUBLIC/"privacy/index.html").resolve()]
        deletion=self.documents[(PUBLIC/"delete-account/index.html").resolve()]
        for lang in ("fr","en"):
            copy=normalized(home.root.visible_text(lang))
            for token in ("LumaCoins","LumaGold","150","60","30","10","25 / 60 / 100"):
                self.check(token in copy,"Homepage has exact earned capsule/currency disclosure in "+lang+": "+token)
            self.check(("7,5" if lang=="fr" else "7.5") in copy and ("3,75" if lang=="fr" else "3.75") in copy and ("1,25" if lang=="fr" else "1.25") in copy,"Individual cosmetic odds visible in "+lang)
            self.check(("deux comptes" if lang=="fr" else "two-account") in copy and ("qualification" in copy),"Real two-account social qualification is not fabricated in "+lang)
            self.check(("achats réels" if lang=="fr" else "real checkout") in copy,"Real checkout qualification explicitly stated in "+lang)
            legal=normalized(privacy.root.visible_text(lang))
            self.check("1.7" in legal and "27" in legal and "2026" in legal,"Privacy edition/date is current in "+lang)
            for token in ("7","30","UTC","LumaCoins","LumaGold"):
                self.check(token in legal,"Privacy includes social expiry/currency disclosure in "+lang+": "+token)
            self.check(("désactivé par défaut" if lang=="fr" else "off by default") in legal,"Social progress sharing defaults off in "+lang)
            self.check(("carnet d’adresses" if lang=="fr" else "address book") in legal,"No address-book import claim is explicit in "+lang)
            self.check(("avant de supprimer" if lang=="fr" else "before deleting") in legal,"Server cleanup precedes Firebase identity deletion in "+lang)
            self.check(("français et en anglais" if lang=="fr" else "French and English") in legal,"Two website languages distinguished from six Android languages in "+lang)
            dcopy=normalized(deletion.root.visible_text(lang))
            self.check(("d’abord les données serveur" if lang=="fr" else "first remove server data") in dcopy,"Deletion instructions preserve server-before-Auth order in "+lang)
        localized=[n for n in home.tags("img") if "data-screenshot-fr" in n.attrs]
        self.check(len(localized)>=5,"At least five native image elements provide FR/EN sources")
        for node in localized:
            for key in ("data-screenshot-fr","data-screenshot-en"):
                self.check(node.attrs[key].startswith("assets/screenshots/v1.7/"),"Current product screenshot uses versioned 1.7 evidence")
        self.check(not any("pilot" in n.attrs.get("src","") for n in home.tags("img")),"No pilot image is presented as final product capture")
        site=(PUBLIC/"assets/site.js").read_text()
        self.check("Luma Drift 1.7" in site and "Luma Drift 1.6" not in site,"Language controller title matches edition 1.7")

    def asset_sources(self):
        sources=[];runtime_sources={}
        # New imagery must belong to a passing, restored native run on one exact final APK.
        final_apks={digest(ROOT/"releases"/name) for name in ("luma-drift-1.7.0-rc1.apk","luma-drift-1.7.0-sandbox.apk") if (ROOT/"releases"/name).is_file()}
        for directory in (ROOT/"docs/screenshots/v1.7").glob("ui-sdk-final-*"):
            report_path=ROOT/"docs/validation/v1.7"/directory.name/"host-report.json"
            if not report_path.is_file():continue
            report=json.loads(report_path.read_text())
            if report.get("passed") is not True or report.get("restored") is not True or report.get("apk_sha256") not in final_apks:continue
            for source in directory.glob("*.png"):
                if report.get("screenshots",{}).get(source.name)!=digest(source):continue
                sources.append(source);runtime_sources[str(source.relative_to(ROOT))]={"report":str(report_path.relative_to(ROOT)),"apk_sha256":report["apk_sha256"],"package":report.get("package"),"profile_scope":report.get("scope")}
        for directory in (ROOT/"docs/screenshots/v1.6",ROOT/"docs/screenshots/v1.5",ROOT/"docs/store",ROOT/"app/src/main/res/drawable-nodpi"):
            if directory.exists():sources.extend(p for p in directory.rglob("*") if p.is_file() and p.suffix.lower() in (".png",".svg",".webp",".jpg"))
        indexed={}
        for source in sources:indexed.setdefault(digest(source),[]).append(str(source.relative_to(ROOT)))
        for path in payloads():
            if path.suffix.lower() not in (".png",".svg",".webp",".jpg"):continue
            sha=digest(path);matches=indexed.get(sha,[]);relative=str(path.relative_to(ROOT))
            if "/screenshots/" in relative:
                self.check(bool(matches),"Actual screenshot has an exact local evidence source: "+relative)
            if path.suffix.lower()==".svg":
                try:
                    svg=ET.fromstring(path.read_bytes());self.check("viewBox" in svg.attrib,"Vector has viewBox: "+relative)
                    self.check(not any(n.tag.rsplit("}",1)[-1] in ("script","foreignObject") for n in svg.iter()),"Vector has no embedded active content: "+relative)
                except ET.ParseError:self.check(False,"Vector parses: "+relative)
            if "/screenshots/v1.7/" in relative:
                self.check(any("screenshots/v1.7/ui-sdk-final-" in x for x in matches),"New website screenshot is an exact FINAL 1.7 capture, never a pilot: "+relative)
            family="final-sdk-1.7" if any("screenshots/v1.7/ui-sdk-final-" in x for x in matches) else "final-production-1.6" if any("ui/final-production/" in x for x in matches) else "final-lab-1.6" if any("ui/final-lab/" in x for x in matches) else "historical-1.5" if any("screenshots/v1.5/" in x for x in matches) else "other-local-source" if matches else "public-only-asset"
            self.assets.append({"public":relative,"bytes":path.stat().st_size,"sha256":sha,"exact_sources":matches,"source_classification":family,"passing_final_runtime_sources":[runtime_sources[name] for name in matches if name in runtime_sources]})
    def configuration(self):
        config=self.read_config();self.check(config.get("publisherName")=="nabilux","Exact public publisher name")
        self.check(config.get("supportEmail")==CONTACT,"Exact configured support plus-tag address")
        self.check(config.get("preparation") is True,"Qualification notice remains enabled")
        wallet=urlsplit(config.get("walletApiBaseUrl",""));self.check(wallet.scheme=="https" and bool(wallet.netloc) and not wallet.query and not wallet.fragment,"Configured wallet is HTTPS; availability not requested")
        firebase=config.get("firebase",{});self.check(firebase.get("projectId")=="luma-drift-production" and ":web:" in firebase.get("appId",""),"External deletion uses the matching Firebase WEB app configuration")
        self.check((PUBLIC/"app-ads.txt").read_text().strip()=="google.com, pub-6914185503996225, DIRECT, f08c47fec0942fa0","Exact app-ads publisher record")
        hosting=json.loads((ROOT/"firebase.json").read_text())["hosting"]
        self.check(hosting.get("site")=="luma-drift-production" and hosting.get("public")=="public","Hosting target points at public/ and the expected site")
        ignores=hosting.get("ignore",[]);self.check("tests/**" in ignores and "**/tests/**" in ignores,"Browser/tests are excluded from deployment")
        headers={item["source"]:{h["key"].lower():h["value"] for h in item["headers"]} for item in hosting.get("headers",[])}
        for key,value in (("x-content-type-options","nosniff"),("referrer-policy","no-referrer"),("x-frame-options","DENY")):
            self.check(headers.get("**",{}).get(key)==value,"Hosting header "+key)
        self.check(headers.get("/site-config.js",{}).get("cache-control")=="no-store","Public configuration is not cached")
        self.check(headers.get("/",{}).get("cache-control")=="no-cache" and headers.get("**/*.html",{}).get("cache-control")=="no-cache","Entry page and HTML require cache revalidation")
        css=(PUBLIC/"assets/style.css").read_text()
        self.check(bool(re.search(r'\[hidden\]\s*\{[^}]*display\s*:\s*none\s*!important',css)),"CSS keeps inactive language/confirmation nodes hidden")
        self.check("prefers-reduced-motion" in css and "scroll-behavior:auto" in css,"Reduced-motion style is present")
        self.check(":focus-visible" in css or ":focus" in css,"Visible keyboard focus rule is present")
        site=(PUBLIC/"assets/site.js").read_text()
        self.check("localStorage" not in re.sub(r'//[^\n]*',"",site) and "document.cookie" not in site,"Language routing does not write localStorage/cookies")
        for token in ("data-screenshot-fr","data-alt-fr","aria-pressed","luma-language","history.replaceState"):
            self.check(token in site,"Language controller includes "+token)
    def syntax_and_refs(self):
        modules={((doc.path.parent/node.attrs["src"].split("?",1)[0]).resolve()) for doc in self.documents.values() for node in doc.tags("script") if node.attrs.get("type")=="module" and node.attrs.get("src") and not urlsplit(node.attrs["src"]).netloc}
        for path in payloads():
            if path.suffix==".css":
                for value in re.findall(r'url\(\s*[\"\']?([^\)\"\']+)',path.read_text()):self.local_reference(value.strip(),path)
            if path.suffix in (".js",".mjs"):
                source=path.read_text()
                for value in re.findall(r'(?:\bfrom\s*|\bimport\s*\(\s*|\bimport\s*)[\"\']([^\"\']+)',source):
                    if value.startswith(("./","../","/")):self.local_reference(value,path)
                if self.node:
                    mode="module" if path.suffix==".mjs" or path.resolve() in modules else "commonjs"
                    result=subprocess.run([self.node,"--input-type="+mode,"--check"],input=source,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=10)
                    self.check(result.returncode==0,"Node parses "+str(path.relative_to(PUBLIC))+": "+result.stdout.strip())
    def run(self):
        try:
            for name in ("index.html","privacy/index.html","delete-account/index.html"):self.html(PUBLIC/name)
            self.edition_contract();self.release(self.documents[(PUBLIC/"index.html").resolve()]);self.configuration();self.asset_sources();self.syntax_and_refs()
        except Exception as error:self.check(False,"Audit completed without an exception: "+repr(error))
        end=snapshot();self.check(self.start==end,"Public payloads and Firebase config unchanged during check")
        failures=[x["message"] for x in self.checks if not x["pass"]]
        node_version=subprocess.check_output([self.node,"--version"],text=True,timeout=10).strip() if self.node else None
        report={"status":"FAIL" if failures else "PASS","checks":len(self.checks),"failures":failures,"local_references":len(self.references),"html_pages":len(self.documents),"node":self.node,"node_version":node_version,"python_version":sys.version.split()[0],"network_requests_performed":0,"autonomous_browser_runner_executed":False,"browser_scope":"Browser QA is performed separately through CUA on localhost. This offline checker does not launch a browser. The historical autonomous runner is retained for 1.6 and is not used to certify this revision.","scope":"Offline source/DOM/file/hash checks only. No browser rendering, Hosting origin request, deployment, real auth/deletion, accessibility certification or remote APK availability check.","results":self.checks}
        (self.out/"static-check.json").write_text(json.dumps(report,indent=2)+"\n")
        summary=report["status"]+": "+str(report["html_pages"])+" HTML pages; "+str(report["checks"])+" offline source assertions; "+str(len(failures))+" failures; "+str(len(self.references))+" local references.\n"+"\n".join("FAIL "+x for x in failures)+"\n"+report["scope"]+"\nHistorical autonomous browser-check.mjs NOT EXECUTED. Current browser QA uses CUA localhost. Browser QA: separate CUA localhost run.\nRuntime: Python "+report["python_version"]+"; Node "+str(node_version)+".\n"
        (self.out/"static-check.txt").write_text(summary)
        (self.out/"source-manifest.json").write_text(json.dumps({"status":report["status"],"network_verified":False,"files":{"/"+str(Path(name).relative_to("public")):info for name,info in end.items() if name.startswith("public/")},"firebase_config_sha256":end["firebase.json"]["sha256"],"runner_sha256":digest(Path(__file__))},indent=2)+"\n")
        (self.out/"source-assets.json").write_text(json.dumps({"screenshots_edited_by_check":False,"assets":self.assets},indent=2)+"\n")
        print(summary,end="");return not failures

def parser_self_test():
    # Regression fixture: inactive ancestors, rather than just an element's own attributes,
    # control the H1/link visibility of the real bilingual privacy document.
    source='''<!doctype html><html lang="fr"><main><div data-lang="fr" lang="fr"><h1>Bonjour</h1><a href="#main">Français</a></div><div data-lang="en" lang="en" hidden><h1>Hello</h1><a href="#main">English</a></div><section hidden><h1>Not visible</h1></section></main></html>'''
    with tempfile.TemporaryDirectory(prefix="luma-site-parser-") as directory:
        path=Path(directory)/"test.html";path.write_text(source);doc=Document(path)
        for language,expected in (("fr","Bonjour"),("en","Hello")):
            visible=[n for n in doc.tags("h1") if n.visible_text(language).strip()]
            if len(visible)!=1 or visible[0].visible_text(language)!=expected:raise AssertionError("Inherited language/hidden visibility regression")
            if len([n for n in doc.tags("a") if n.visible(language)])!=1:raise AssertionError("Inactive-language links cannot count as visible DOM controls")
        if unquote("nabz0r%2Bgaming%40gmail.com")!=CONTACT:raise AssertionError("Plus-tag decoding regression")

if __name__=="__main__":
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument("--node");parser.add_argument("--apk-sha256");parser.add_argument("--report-dir",default=str(ROOT/"docs/validation/site-v1.7"));parser.add_argument("--self-test",action="store_true");arguments=parser.parse_args()
    parser_self_test()
    if arguments.self_test:print("PASS parser regressions: inherited FR/EN visibility, hidden content, active headings/links and exact plus-tag decoding");sys.exit(0)
    sys.exit(0 if Audit(arguments).run() else 1)
