#!/usr/bin/env python3
"""Bind the final signed D bytes to every captured physical build input, without replacing earlier artifacts."""
from pathlib import Path
import json,hashlib,shutil,subprocess,re,datetime,os
ROOT=Path(__file__).resolve().parents[2]
os.chdir(ROOT)
out=Path('docs/validation/v1.24/candidate-d')
inputs=json.loads((out/'build-inputs.json').read_text())['source_inputs']
def sha(p):return hashlib.sha256(Path(p).read_bytes()).hexdigest()
for item in inputs:
    path=Path(item['path'])
    assert path.is_file() and sha(path)==item['sha256'],'Physical source drift: '+str(path)
signer='dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'
sdk=Path('/Users/n/Library/Android/sdk/build-tools/36.0.0')
env=os.environ.copy();env['JAVA_HOME']='/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home'
apks={};shutil.copyfile('/tmp/luma124-build-d.txt',out/'build.txt')
for flavor,suffix,package in [('production','','com.androidhit.lumadrift'),('lab','-sandbox','com.androidhit.lumadrift.sandbox')]:
    version='1.24.0'+suffix+'-rc2'
    source=Path('app/build/outputs/apk')/flavor/'release'/('app-'+flavor+'-release.apk')
    target=Path('releases')/('luma-drift-'+version+'.apk')
    if target.exists():assert sha(target)==sha(source),'Never replace a differing artifact'
    else:shutil.copyfile(source,target)
    signature=subprocess.check_output([str(sdk/'apksigner'),'verify','--verbose','--print-certs',str(target)],text=True,env=env)
    (out/(flavor+'-signature.txt')).write_text(signature)
    assert re.findall(r'Signer #1 certificate SHA-256 digest: ([a-f0-9]+)',signature)==[signer]
    badging=subprocess.check_output([str(sdk/'aapt'),'dump','badging',str(target)],text=True,env=env)
    (out/(flavor+'-badging.txt')).write_text(badging)
    assert "name='"+package+"'" in badging and "versionCode='33'" in badging and "versionName='"+version+"'" in badging
    apks[flavor]={'path':str(target),'sha256':sha(target),'bytes':target.stat().st_size,'version':version,'code':33,'application_id':package,'signer_certificate_sha256':signer}
flags={}
for flavor in ('production','lab'):
    path=Path('app/build/generated/source/buildConfig')/flavor/'release/com/androidhit/lumadrift/BuildConfig.java'
    text=path.read_text();expected={'MODE_SANDBOX':flavor=='lab','FORCE_TEST_ADS':True,'PAID_COSMETIC_DRAWS_ENABLED':False}
    for key,value in expected.items():assert re.search(r'boolean '+key+r' = '+str(value).lower()+r';',text),'Generated build flag differs'
    flags[flavor]={'path':str(path),'sha256':sha(path),'values':expected}
candidate={'status':'BUILT_SIGNED_NATIVE_QUALIFICATION_SEPARATE','recorded_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'source_inputs':inputs,'version_code':33,'apks':apks,'generated_flags':flags}
(out/'candidate.json').write_text(json.dumps(candidate,indent=2)+'\n')
manifest={'edition':'1.24','version':'1.24.0-rc2','version_code':33,'artifact_policy':'Ignored external signed APK files; never require Git blobs','candidate_report':str(out/'candidate.json'),'candidate_report_sha256':sha(out/'candidate.json'),'apks':apks,'publicly_published':False,'live_services_validated':False}
path=Path('releases/manifest-1.24.0-rc2.json');assert not path.exists(),'Final manifest must be newly created'
path.write_text(json.dumps(manifest,indent=2)+'\n')
print(json.dumps({'manifest_sha256':sha(path),'apks':apks},indent=2))
