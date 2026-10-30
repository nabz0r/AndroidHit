#!/usr/bin/env python3
"""Build only a separately signed SDK observer in a unique output directory; never build or install the game."""
from pathlib import Path
import argparse
import datetime
import hashlib
import json
import os
import subprocess
import uuid
import zipfile

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--out',type=Path)
    args=parser.parse_args()
    out=args.out or Path('/tmp/luma122-lab-native-observer')/('observer-'+uuid.uuid4().hex[:12])
    assert not out.exists(), 'Observer outputs must be unique and must not overwrite an earlier attempt'
    sdk=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))
    java=Path(os.environ.get('JAVA_HOME','/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home'))
    tools=sdk/'build-tools/36.0.0'
    jar=sdk/'platforms/android-36/android.jar'
    env=os.environ.copy()
    env['JAVA_HOME']=str(java)
    build=out/'build'
    (build/'classes').mkdir(parents=True)
    (build/'dex').mkdir()
    def call(*parts):subprocess.run([str(part) for part in parts],check=True,env=env)
    call(java/'bin/javac','-source','17','-target','17','-classpath',jar,'-d',build/'classes',*sorted((HERE/'java').rglob('*.java')))
    call(tools/'d8','--release','--min-api','26','--lib',jar,'--output',build/'dex',*sorted((build/'classes').rglob('*.class')))
    call(tools/'aapt2','link','-I',jar,'--manifest',HERE/'AndroidManifest.xml','-o',build/'linked.apk','--version-code','1','--version-name','1.22-lab-native-qa')
    with zipfile.ZipFile(build/'linked.apk') as source,zipfile.ZipFile(build/'unsigned.apk','w') as target:
        for entry in source.infolist():target.writestr(entry,source.read(entry.filename))
        target.write(build/'dex/classes.dex','classes.dex')
        fixtures=json.loads((HERE/'assets/fixtures/manifest.json').read_text())
        for name,digest in sorted(fixtures['fixture_sha256'].items()):
            fixture=HERE/'assets/fixtures'/name
            assert hashlib.sha256(fixture.read_bytes()).hexdigest()==digest, 'Frozen public gesture certificate changed: '+name
            target.write(fixture,'assets/fixtures/'+name)
    call(tools/'zipalign','-f','4',build/'unsigned.apk',build/'aligned.apk')
    keys={}
    for line in (ROOT/'signing.properties').read_text().splitlines():
        if '=' in line and not line.lstrip().startswith('#'):
            key,value=line.split('=',1)
            keys[key.strip()]=value.strip()
    env['LUMA122_QA_STORE_PASS']=keys['storePassword']
    env['LUMA122_QA_KEY_PASS']=keys['keyPassword']
    apk=out/'battle-observer.apk'
    call(tools/'apksigner','sign','--ks',ROOT/keys['storeFile'],'--ks-key-alias',keys['keyAlias'],'--ks-pass','env:LUMA122_QA_STORE_PASS','--key-pass','env:LUMA122_QA_KEY_PASS','--out',apk,build/'aligned.apk')
    call(tools/'apksigner','verify','--verbose','--print-certs',apk)
    receipt={'created_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'apk':str(apk),'sha256':hashlib.sha256(apk.read_bytes()).hexdigest(),'package':'com.androidhit.lumadrift.sandbox.v122test','target':'com.androidhit.lumadrift.sandbox','scope':'SDK javac/d8/aapt2 observer build only; no Gradle shared outputs and no game build or installation.','source_sha256':{str(path.relative_to(HERE)):hashlib.sha256(path.read_bytes()).hexdigest() for path in sorted(HERE.rglob('*')) if path.is_file() and '__pycache__' not in path.parts}}
    (out/'build-receipt.json').write_text(json.dumps(receipt,indent=2)+'\n')
    print(json.dumps(receipt))

if __name__=='__main__':main()
