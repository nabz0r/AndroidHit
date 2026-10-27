#!/usr/bin/env python3
"""Observe real production AudioFlinger playback on the designated QA emulator; no sound is fabricated."""
import argparse
import json
from pathlib import Path
import re
import subprocess
import time

parser=argparse.ArgumentParser()
parser.add_argument("--serial",required=True,choices=["emulator-5580"])
parser.add_argument("--apk-sha256",required=True)
parser.add_argument("--adb",default=str(Path.home()/"Library/Android/sdk/platform-tools/adb"))
parser.add_argument("--out",default=str(Path(__file__).resolve().parents[1]/"docs/validation/v1.6/audio-runtime.json"))
args=parser.parse_args()
if not re.fullmatch(r'[0-9a-f]{64}',args.apk_sha256):parser.error("An exact expected APK SHA-256 is required")
command=[args.adb,"-s",args.serial];package="com.androidhit.lumadrift"
def shell(*values):return subprocess.check_output(command+["shell",*values],text=True,stderr=subprocess.STDOUT,timeout=40)
pid=shell("pidof",package).strip()
if not re.fullmatch(r'\d+',pid):raise AssertionError("Production application must already be running in foreground with music enabled")
path=shell("pm","path",package).strip().removeprefix("package:")
sha=shell("su","0","sha256sum",path).split()[0]
if sha!=args.apk_sha256:raise AssertionError("Installed APK hash differs from requested production candidate")
def tracks():
    raw=shell("dumpsys","media.audio_flinger")
    lines=[line.strip() for line in raw.splitlines() if re.match(r'^\s*S\s+\d+\s+(yes|no)\s+',line) and re.search(r'\b'+pid+r'/\s*',line)]
    result=[]
    for line in lines:
        parts=line.split();match=re.search(r'\b(?:false|true)\s+([0-9A-Fa-f]{8})\s+(\d+)\s+',line)
        result.append({"track_id":parts[1],"active":parts[2]=="yes","frames":int(match.group(1),16) if match else None,"row":line})
    return result
time.sleep(1);first=tracks();time.sleep(1);second=tracks()
active=next((track for track in first if track["active"] and track["frames"] is not None),None)
if active is None:raise AssertionError("No active production AudioFlinger track observed; music may be off")
advanced=next((track for track in second if track["active"] and track["track_id"]==active["track_id"] and track["frames"] is not None),None)
if advanced is None or advanced["frames"]<=active["frames"]:raise AssertionError("No frame advance on the same actual audio track")
shell("input","keyevent","3");time.sleep(.4);background=tracks()
if any(track["active"] for track in background):raise AssertionError("Production audio remained active after HOME")
shell("am","start","-n",package+"/com.androidhit.lumadrift.MainActivity");time.sleep(.6);resumed=tracks()
if not any(track["active"] for track in resumed):raise AssertionError("Production music did not resume in foreground")
log=shell("logcat","-d","-s","LumaAudio:I","AndroidRuntime:E")
report={"status":"PASS","serial":args.serial,"package":package,"apk_sha256":sha,"pid":pid,
        "foreground_first":first,"foreground_second":second,"observed_frame_advance":advanced["frames"]-active["frames"],
        "background":background,"foreground_after_resume":resumed,
        "audio_log":[line for line in log.splitlines() if re.search(r'\b'+pid+r'\b',line) and "LumaAudio" in line],
        "scope":"Actual Android AudioFlinger track, frame advance, background pause and foreground resume. Digital playback observation only; no physical speaker/headphone audition or real external focus interruption."}
output=Path(args.out);output.parent.mkdir(parents=True,exist_ok=True);output.write_text(json.dumps(report,indent=2)+"\n")
print(json.dumps({"status":"PASS","frame_advance":report["observed_frame_advance"],"background_active":False,"resumed":True,"sha256":sha}))
