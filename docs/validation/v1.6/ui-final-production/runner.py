#!/usr/bin/env python3
"""Installed menu QA on emulator-5580 only. Never signs in, purchases, loads ads or plays a move.

All private preferences are backed up and restored byte-for-byte; no synthetic profile is seeded.
Window overrides and rotation are also restored in finally. UI navigation/previews and language changes
are real. Screenshots/reports identify the exact installed candidate, not a later rebuilt APK.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shlex
import subprocess
import tempfile
import time
import traceback
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
PACKAGE="com.androidhit.lumadrift.sandbox"
COMPONENT=PACKAGE+"/com.androidhit.lumadrift.MainActivity"
LANGS=("en","fr","de","es","pt","it")
LANGUAGE_NAMES=("English","Français","Deutsch","Español","Português","Italiano")

def copy_catalog():
    result={}
    for filename in ("Strings.java","ExperienceStrings.java","GearStrings.java"):
        source=(ROOT/"app/src/main/java/com/androidhit/lumadrift/data"/filename).read_text()
        for line in source.splitlines():
            if not re.search(r'\badd\("',line):continue
            values=re.findall(r'"((?:\\.|[^"\\])*)"',line)
            if len(values)==7:
                result[values[0]]=[json.loads('"'+value+'"') for value in values[1:]]
    return result

COPY=copy_catalog()
def text(node):return node.get("text") or node.get("content-desc") or ""
def bounds(node):
    match=re.fullmatch(r'\[(-?\d+),(-?\d+)\]\[(-?\d+),(-?\d+)\]',node.get("bounds",""))
    if not match:raise AssertionError("Missing bounds")
    return tuple(map(int,match.groups()))

class Smoke:
    def __init__(self,args):
        self.args=args;self.serial=args.serial;self.lang="en";self.messages=[];self.checks=0;self.backup_ready=False;self.settings_ready=False
        sdk=Path(os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT") or Path.home()/"Library/Android/sdk")
        self.adb=args.adb or str(sdk/"platform-tools/adb")
        self.out=ROOT/"docs/validation/v1.6"/("ui-"+args.label);self.out.mkdir(parents=True,exist_ok=True)
        self.shots=ROOT/"docs/screenshots/v1.6/ui"/args.label;self.shots.mkdir(parents=True,exist_ok=True)
        runner=Path(__file__).read_bytes();(self.out/"runner.py").write_bytes(runner)
        self.meta={"scope":"Installed "+args.variant+" menu QA; no seeded profile, account login, ad or purchase", "serial":self.serial,"package":PACKAGE,"label":args.label,"cases":args.cases,"restored":False,"runner_sha256":hashlib.sha256(runner).hexdigest()}
    def cmd(self,*args):return subprocess.check_output([self.adb,"-s",self.serial,*args],stderr=subprocess.STDOUT,timeout=45)
    def shell(self,*args):return self.cmd("shell",*args).decode(errors="replace")
    def log(self,msg):self.messages.append(msg);print(msg,flush=True)
    def check(self,condition,message):
        self.checks+=1
        if not condition:raise AssertionError(message)
        self.log("PASS "+message)
    def tr(self,key):return COPY[key][LANGS.index(self.lang)]
    def nodes(self,name=None):
        remote="/sdcard/luma-v16-smoke.xml";self.shell("uiautomator","dump",remote)
        raw=self.shell("cat",remote)
        if name:(self.out/(name+".xml")).write_text(raw)
        nodes=list(ET.fromstring(raw).iter("node"))
        return [n for n in nodes if n.get("package")==PACKAGE]
    def labels(self):return [text(n) for n in self.nodes()]
    def tap(self,node):
        label=text(node)
        if label.startswith(("Coins:","Gold &")) or any(x in label for x in ("Sign in with Google","Simulate","Allow optional advertising")):
            raise AssertionError("Forbidden commerce/auth route: "+label)
        l,t,r,b=bounds(node)
        if r-l<10 or b-t<10:raise AssertionError("Clipped control: "+label)
        self.shell("input","tap",str((l+r)//2),str((t+b)//2));time.sleep(.25)
    def click(self,label=None,predicate=None,seek=False):
        predicate=predicate or (lambda n:text(n)==label)
        for attempt in range(7 if seek else 1):
            matches=[n for n in self.nodes() if n.get("clickable")=="true" and predicate(n) and bounds(n)[3]-bounds(n)[1]>20]
            if matches:self.tap(matches[-1]);return
            if seek:self.swipe()
        raise AssertionError("Missing control "+repr(label)+" in "+repr(self.labels()))
    def screen(self,key):
        return any(n.get("class")=="android.view.View" and text(n)=="Luma Drift · "+self.tr(key) for n in self.nodes())
    def require_screen(self,key,message):self.check(self.screen(key),message)
    def capture(self,name):
        self.nodes(name);(self.shots/(name+".png")).write_bytes(self.cmd("exec-out","screencap","-p"));self.log("CAPTURE "+name)
    def back(self):self.shell("input","keyevent","4");time.sleep(.2)
    def launch_home(self):
        self.shell("am","force-stop",PACKAGE);self.shell("am","start","-n",COMPONENT);time.sleep(.3)
    def dimensions(self):
        value=self.shell("wm","size");match=re.findall(r'(?:Physical|Override) size: (\d+)x(\d+)',value)[-1];return tuple(map(int,match))
    def swipe(self,down=False):
        width,height=self.dimensions();a,b=(.29,.72) if down else (.72,.29)
        self.shell("input","swipe",str(width//2),str(int(height*a)),str(width//2),str(int(height*b)),"420");time.sleep(.15)
    def top(self):
        for _ in range(3):self.swipe(True)
    @property
    def prefdir(self):return "/data/user/0/"+PACKAGE+"/shared_prefs"
    def files(self):
        names=self.shell("su","0","ls","-1",self.prefdir).splitlines()
        if any(not re.fullmatch(r'[A-Za-z0-9_.-]+',x) for x in names):raise AssertionError("Unexpected preference filename; refusing incomplete backup")
        return names
    def prepare(self):
        self.check(self.shell("getprop","ro.kernel.qemu").strip()=="1","Explicit QA emulator; no physical device")
        details=self.shell("dumpsys","package",PACKAGE)
        version="1.6.0-sandbox" if self.args.variant=="lab" else "1.6.0-rc1"
        self.check("versionName="+version in details and "versionCode=7 " in details,"Installed "+self.args.variant+"1.6/code7")
        self.shell("am","force-stop",PACKAGE)
        self.backup=Path(tempfile.mkdtemp(prefix="luma-v16-menus-backup-"));self.original={}
        self.owner=self.shell("su","0","stat","-c","%u:%g","/data/user/0/"+PACKAGE).strip()
        if not re.fullmatch(r'\d+:\d+',self.owner):raise AssertionError("Invalid preference owner")
        for name in self.files():
            raw=self.cmd("exec-out","su","0","cat",self.prefdir+"/"+name);self.original[name]=raw;(self.backup/name).write_bytes(raw)
        self.backup_ready=True
        for raw in self.original.values():
            if any(n.get("name")=="previously_signed_in" and n.get("value")=="true" for n in ET.fromstring(raw)):
                raise AssertionError("Guest-only UI qualification; refuses a stored signed-in identity")
        self.meta["backup_location"]=str(self.backup)
        path=self.shell("pm","path",PACKAGE).strip().removeprefix("package:")
        self.meta["apk_sha256"]=self.shell("su","0","sha256sum",path).split()[0]
        self.meta["build"]=re.findall(r'^\s*(versionName=.+|versionCode=.+)',details,re.M)
        self.meta["device"]={"api":self.shell("getprop","ro.build.version.sdk").strip(),"abi":self.shell("getprop","ro.product.cpu.abi").strip(),"page_size":self.shell("getconf","PAGESIZE").strip(),"density":self.shell("wm","density").strip(),"scope":"Actual install/launch and UI on a 16KB kernel; separate from APK alignment inspection"}
        self.settings={"size":self.shell("wm","size"),"density":self.shell("wm","density"),"rotation":self.shell("settings","get","system","user_rotation").strip(),"accelerometer":self.shell("settings","get","system","accelerometer_rotation").strip(),"font_scale":self.shell("settings","get","system","font_scale").strip()}
        self.settings_ready=True;self.crashes_before=self.shell("logcat","-d","-b","crash")
        self.meta["settings_before"]=self.settings;self.launch_home();self.lang="en"
        self.require_screen("home","Cold launch opens Home without abandoning the saved run")
        self.log("APK SHA-256 "+self.meta["apk_sha256"])
    def gameplay_fingerprint(self):
        raw=self.cmd("exec-out","su","0","cat",self.prefdir+"/luma_drift_save_v1.xml")
        values={n.get("name"):(n.get("value") if n.tag!="string" else n.text or "") for n in ET.fromstring(raw)}
        return {k:v for k,v in values.items() if k=="coins" or k=="unlocked" or k.startswith(("stars_","booster_","owned_","avatar_"))}
    def menus(self):
        before=self.gameplay_fingerprint();self.capture("home-en")
        self.click(self.tr("nav_map"));self.require_screen("journey","Home → Map")
        labels=self.labels();nodes=[x for x in labels if x.startswith("Level ") and ", Normal," in x or x.startswith("Level ") and ", Hard," in x or x.startswith("Level ") and ", Expert," in x]
        self.check(len(nodes)==10,"Saga exposes exactly ten level nodes on first page")
        self.capture("map-en")
        self.click(predicate=lambda n:text(n).startswith("Level 1,") and n.get("clickable")=="true")
        self.require_screen("objective","Level node opens the prelevel goal screen")
        self.capture("briefing-en")
        self.click(self.tr("tab_powers"),seek=True);self.require_screen("collection","Prelevel Powers shortcut opens inventory")
        self.capture("powers-en")
        self.back();self.require_screen("objective","Back from inventory restores prelevel without abandoning")
        self.click(self.tr("back"));self.require_screen("journey","Visible Back from prelevel returns to Map")
        self.back();self.require_screen("home","Back after visible prelevel cancellation has no history loop")
        self.click(self.tr("nav_map"));self.require_screen("journey","Map remains reachable after cancellation")
        self.click(self.tr("nav_inventory"));self.require_screen("collection","Map → Inventory")
        self.click(self.tr("tab_companions"));self.click(self.tr("filter_all"));self.capture("companions-en")
        self.click(predicate=lambda n:text(n).startswith("Nova, "));self.check("Nova" in self.labels(),"Owned companion opens a native detail preview")
        self.back();self.require_screen("collection","Cancelling owned preview returns to inventory")
        self.click(self.tr("filter_locked"));self.capture("companions-locked-en")
        self.click(predicate=lambda n:text(n).startswith("Miko, "));self.check("Miko" in self.labels(),"Unowned companion opens its preview")
        self.back();self.check(self.gameplay_fingerprint()==before,"Preview cancellations spend no coins, stock, stars or ownership")
        self.click(self.tr("filter_owned"));self.check(any(text(n).startswith("Nova, Selected,") for n in self.nodes()),"Owned filter includes selected starter companion")
        self.click(self.tr("tab_themes"));self.capture("themes-owned-en")
        self.click(self.tr("tab_equipment"));self.capture("equipment-owned-en")
        self.check(sum(", Equipped," in text(n) for n in self.nodes())==3,"Owned equipment shows all three equipped starters")
        self.click(self.tr("filter_locked"));self.capture("equipment-locked-en")
        self.click(self.tr("tab_powers"));self.capture("powers-locked-en")
        self.check(any(", Locked," in text(n) for n in self.nodes()),"Locked powers retain explicit unlock state")
        self.click(self.tr("nav_events"));self.capture("events-en");self.require_screen("events","Inventory → Events")
        self.check(self.gameplay_fingerprint()==before,"Menu/filter traversal leaves economic and gameplay values unchanged")
    def languages(self):
        for lang,name in zip(LANGS,LANGUAGE_NAMES):
            if lang not in self.args.locales.split(","):continue
            self.launch_home();self.click(self.tr("settings"));self.click(name,seek=True);self.lang=lang;self.back()
            self.require_screen("home","Language "+lang+" returns to localized Home");self.capture("home-"+lang)
            if lang=="fr":
                self.click(self.tr("nav_map"));self.capture("map-fr")
                prefix=self.tr("level_number").replace("%d","1")+",";self.click(predicate=lambda n:text(n).startswith(prefix));self.capture("briefing-fr");self.back()
            self.click(self.tr("nav_inventory"));self.require_screen("collection","Inventory localized "+lang)
            labels=set(self.labels())
            for key in ("tab_companions","tab_themes","tab_equipment","tab_powers","filter_all","filter_owned","filter_locked"):
                self.check(self.tr(key) in labels,"Visible localized control "+lang+"/"+key)
            self.capture("inventory-"+lang)
        self.launch_home();self.click(self.tr("settings"));self.click("English",seek=True);self.lang="en";self.back()
    def compact(self):
        self.shell("wm","size","960x1920");self.shell("wm","density","480");time.sleep(.7);self.launch_home()
        self.require_screen("home","320dp portrait launches Home");self.capture("home-320dp-en")
        self.click(self.tr("nav_inventory"));self.capture("inventory-320dp-en")
        self.click(self.tr("tab_equipment"));self.click(self.tr("filter_all"));self.capture("equipment-320dp-top-en")
        self.swipe();self.capture("equipment-320dp-bottom-en")
        self.check("›" in self.labels(),"320dp scroll makes inventory pager reachable")
        self.shell("wm","size","1920x960");time.sleep(.7);self.launch_home()
        self.require_screen("home","Compact landscape launches Home");self.capture("home-landscape-top-en")
        self.swipe();self.capture("home-landscape-bottom-en")
        self.check(self.tr("nav_inventory") in self.labels(),"Landscape pinned navigation survives content scroll")
        self.click(self.tr("nav_inventory"));self.capture("inventory-landscape-top-en")
        for _ in range(12):
            if any(text(n)=="›" and bounds(n)[3]-bounds(n)[1]>20 for n in self.nodes()):break
            self.swipe()
        self.capture("inventory-landscape-bottom-en")
        self.check(any(text(n)=="›" and bounds(n)[3]-bounds(n)[1]>20 for n in self.nodes()),"Landscape scroll reaches inventory pager")
        host=next(n for n in self.nodes() if n.get("class")=="android.view.View");l,t,r,b=bounds(host)
        self.check(r-l>b-t,"Actual app viewport is landscape, not a portrait-only screenshot")
    def restore_window(self):
        for command,key in (("size","size"),("density","density")):
            pattern=r'Override '+command+r': (\S+)';match=re.search(pattern,self.settings[key])
            self.shell("wm",command,match.group(1) if match else "reset")
    def font(self):
        self.restore_window();self.shell("settings","put","system","font_scale","1.3");time.sleep(.5);self.launch_home()
        self.require_screen("home","Font scale1.3 opens Home");self.capture("home-font130-en")
        self.click(self.tr("nav_inventory"));self.require_screen("collection","Font scale1.3 opens Inventory");self.capture("inventory-font130-en")
        self.click(self.tr("nav_map"));self.capture("map-font130-en")
        prefix=self.tr("level_number").replace("%d","1")+",";self.click(predicate=lambda n:text(n).startswith(prefix));self.capture("briefing-font130-en")
        self.click(self.tr("tab_powers"),seek=True);self.back();self.require_screen("objective","Font scale1.3 preserves briefing/inventory/back flow")
        self.check(self.shell("settings","get","system","font_scale").strip()=="1.3","Actual emulator font preference is1.3")
    def audio(self):
        if self.args.variant!="production":raise AssertionError("Actual runtime audio check requires the production package")
        self.restore_window();self.launch_home();self.click(self.tr("settings"))
        off=self.tr("music")+": "+self.tr("off");on=self.tr("music")+": "+self.tr("on")
        was_off=off in self.labels()
        if was_off:self.click(off)
        try:
            self.check(on in self.labels(),"Music actually enabled through the settings UI before AudioFlinger observation")
            result=subprocess.check_output(["python3",str(ROOT/"scripts/check-audio-runtime.py"),"--serial",self.serial,"--adb",self.adb,"--apk-sha256",self.meta["apk_sha256"]],stderr=subprocess.STDOUT,text=True,timeout=40)
            self.log(result.strip());self.meta["audio_runtime"]="../audio-runtime.json"
        finally:
            if was_off:
                self.launch_home();self.click(self.tr("settings"));self.click(on);self.check(off in self.labels(),"Original music-off preference restored through UI")
    def restore(self):
        if self.settings_ready:
            self.restore_window()
            for name,key in (("user_rotation","rotation"),("accelerometer_rotation","accelerometer"),("font_scale","font_scale")):
                if self.settings[key]=="null":self.shell("settings","delete","system",name)
                else:self.shell("settings","put","system",name,self.settings[key])
            actual={"size":self.shell("wm","size"),"density":self.shell("wm","density"),"rotation":self.shell("settings","get","system","user_rotation").strip(),"accelerometer":self.shell("settings","get","system","accelerometer_rotation").strip(),"font_scale":self.shell("settings","get","system","font_scale").strip()}
            self.meta["settings_restored"]=actual==self.settings
            self.check(self.meta["settings_restored"],"Window overrides, rotation and font preference restored")
        if self.backup_ready:
            self.shell("am","force-stop",PACKAGE)
            for name in self.files():self.shell("su","0","rm","-f",self.prefdir+"/"+name)
            for name,raw in self.original.items():
                path=self.prefdir+"/"+name
                command="su 0 sh -c "+shlex.quote("cat > "+shlex.quote(path))
                subprocess.run([self.adb,"-s",self.serial,"shell",command],input=raw,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=45,check=True)
                self.shell("su","0","chown",self.owner,path);self.shell("su","0","chmod","660",path);self.shell("su","0","restorecon",path)
            restored={name:self.cmd("exec-out","su","0","cat",self.prefdir+"/"+name) for name in self.files()}
            self.meta["restored"]=restored==self.original
            self.meta["preference_sha256"]={name:{"before":hashlib.sha256(raw).hexdigest(),"after":hashlib.sha256(restored.get(name,b"")).hexdigest()} for name,raw in self.original.items()}
            self.check(self.meta["restored"],"All preference files restored byte-for-byte; app left stopped")
    def run(self):
        failure=None
        try:
            self.prepare()
            for case in self.args.cases.split(","):getattr(self,case)()
            crashes=self.shell("logcat","-d","-b","crash")
            self.check(crashes==self.crashes_before,"No new crash-buffer entry")
            self.meta["status"]="PASS"
        except Exception as error:
            failure=error;self.meta["status"]="FAIL";self.log("FAIL "+str(error));self.log(traceback.format_exc())
            try:self.capture("failure")
            except Exception:pass
        finally:
            try:self.restore()
            except Exception as error:self.meta["status"]="FAIL";self.meta["restore_error"]=str(error);failure=error;self.log("RESTORE FAILURE "+str(error))
            self.meta["checks"]=self.checks;(self.out/"report.json").write_text(json.dumps(self.meta,indent=2)+"\n")
            (self.out/"report.txt").write_text("\n".join(self.messages)+"\nSCOPE: candidate-only UI; no move, purchase, ad or Google login.\n")
        if failure:raise failure

if __name__=="__main__":
    parser=argparse.ArgumentParser();parser.add_argument("--serial",required=True,choices=["emulator-5580"]);parser.add_argument("--adb")
    parser.add_argument("--variant",choices=["lab","production"],default="lab");parser.add_argument("--locales",default="en,fr,de,es,pt,it")
    parser.add_argument("--label",default="candidate1");parser.add_argument("--cases",default="menus,languages,compact,font");args=parser.parse_args()
    if not re.fullmatch(r'[a-z0-9-]{1,40}',args.label):parser.error("Safe artifact label required")
    if any(x not in ("menus","languages","compact","font","audio") for x in args.cases.split(",")):parser.error("Unknown case")
    if any(x not in LANGS for x in args.locales.split(",")):parser.error("Unsupported locale")
    if args.variant=="production":PACKAGE="com.androidhit.lumadrift";COMPONENT=PACKAGE+"/com.androidhit.lumadrift.MainActivity"
    Smoke(args).run()
