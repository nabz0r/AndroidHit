#!/usr/bin/env python3
"""Detach an explicitly identified replay after the owner's exclusive 5586 handoff.

The wrapper independently checks exact AVD, installed APK, observer signatures,
frozen fixtures and returned execution identity. No candidate/version/hash default
may silently qualify superseded bytes.
"""
from pathlib import Path
import argparse,hashlib,json,re,subprocess,sys,time

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--label',required=True)
    parser.add_argument('--candidate',required=True,help='Human-readable frozen candidate label')
    parser.add_argument('--apk',required=True,type=Path)
    parser.add_argument('--sha256',required=True)
    parser.add_argument('--expected-version',required=True)
    parser.add_argument('--expected-code',required=True,type=int)
    parser.add_argument('--observer',type=Path,default=ROOT/'app/build/qualification-artifacts/v1.17/relay-production-test.apk')
    args=parser.parse_args()
    assert re.fullmatch(r'[A-Za-z0-9_-]{1,50}',args.label)
    assert re.fullmatch(r'[A-Za-z0-9_-]{1,30}',args.candidate)
    assert re.fullmatch(r'[a-f0-9]{64}',args.sha256) and sha(args.apk)==args.sha256
    assert re.fullmatch(r'1\.17\.0-rc[1-9][0-9]*',args.expected_version) and args.expected_code>0
    assert args.observer.is_file()
    folder=ROOT/'docs/validation/v1.17/relay';folder.mkdir(parents=True,exist_ok=True)
    assert not (folder/args.label).exists() and not (folder/(args.label+'-launch.json')).exists(),'Preserve every prior attempt'
    command=[sys.executable,str(HERE/'run.py'),'--serial','emulator-5586','--expected-avd','Luma-Refonte115-QA','--expected-apk',str(args.apk.resolve()),'--sha256',args.sha256,'--test-apk',str(args.observer.resolve()),'--expected-version',args.expected_version,'--expected-code',str(args.expected_code),'--label',args.label,'--from','1','--through','1000']
    log=folder/(args.label+'-console.txt')
    with log.open('xb') as output:process=subprocess.Popen(command,cwd=ROOT,stdout=output,stderr=subprocess.STDOUT,start_new_session=True)
    record={'status':'started; only real completed native levels count','pid':process.pid,'command':command,'candidate':args.candidate,'version':args.expected_version,'code':args.expected_code,'apk_sha256':args.sha256,'observer_sha256':sha(args.observer),'label':args.label,'console':str(log.relative_to(ROOT)),'expected_report':str((folder/args.label/'host-report.json').relative_to(ROOT)),'remote_progress':'/sdcard/Android/data/com.androidhit.lumadrift/files/v117-relay-native/'+args.label+'/progress.json','started_unix':time.time(),'scope':'Detached actual native U1-U1000 replay. Explicit exclusive5586 handoff is mandatory before this command. No claimed result until passing host/native reports and independent all1000 summarization.','remaining_on_success':['Run summarize.py against the passing host-report.json','Verify restored language and protected before/after equality','Report exact totals to root; do not publish or stop another owner without coordination']}
    encoded=json.dumps(record,indent=2)+'\n';(folder/(args.label+'-launch.json')).write_text(encoded);(folder/'active-handoff.json').write_text(encoded)
    print(encoded)
if __name__=='__main__':main()
