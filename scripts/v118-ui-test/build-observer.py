#!/usr/bin/env python3
"""Build a separately signed SDK-only observer; never compile or install the game."""
from pathlib import Path
import argparse, hashlib, json, os, shutil, subprocess, zipfile

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--variant',choices=['lab','production'],default='lab')
    a=p.parse_args()
    fixtures=HERE/'assets/fixtures'
    fixture_manifest=json.loads((fixtures/'manifest.json').read_text())
    for name,digest in fixture_manifest['fixture_sha256'].items():
        assert hashlib.sha256((fixtures/name).read_bytes()).hexdigest()==digest,'Frozen existing U certificate changed: '+name
    sdk=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))
    java=Path(os.environ.get('JAVA_HOME','/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home'))
    env=os.environ.copy();env['JAVA_HOME']=str(java)
    tools=sdk/'build-tools/36.0.0';jar=sdk/'platforms/android-36/android.jar'
    out=ROOT/'app/build/qualification-artifacts/v1.18'/a.variant
    out.mkdir(parents=True,exist_ok=True)
    build=out/'build';shutil.rmtree(build,ignore_errors=True)
    (build/'classes').mkdir(parents=True);(build/'dex').mkdir()
    def call(*args):subprocess.run([str(x) for x in args],check=True,env=env)
    call(java/'bin/javac','-source','17','-target','17','-classpath',jar,'-d',build/'classes',*sorted((HERE/'java').rglob('*.java')))
    call(tools/'d8','--release','--min-api','26','--lib',jar,'--output',build/'dex',*sorted((build/'classes').rglob('*.class')))
    package='com.androidhit.lumadrift'+('.sandbox' if a.variant=='lab' else '')
    manifest=build/'AndroidManifest.xml'
    manifest.write_text((HERE/'AndroidManifest.xml').read_text().replace('TARGET_PACKAGE',package))
    call(tools/'aapt2','link','-I',jar,'--manifest',manifest,'-o',build/'linked.apk','--version-code','1','--version-name','1.18-expedition-qa')
    with zipfile.ZipFile(build/'linked.apk') as source,zipfile.ZipFile(build/'unsigned.apk','w') as target:
        for entry in source.infolist():target.writestr(entry,source.read(entry.filename))
        target.write(build/'dex/classes.dex','classes.dex')
        for source in sorted((HERE/'assets').rglob('*')):
            if source.is_file():target.write(source,'assets/'+str(source.relative_to(HERE/'assets')))
    call(tools/'zipalign','-f','4',build/'unsigned.apk',build/'aligned.apk')
    keys={}
    for line in (ROOT/'signing.properties').read_text().splitlines():
        if '=' in line and not line.lstrip().startswith('#'):
            key,value=line.split('=',1);keys[key.strip()]=value.strip()
    env['LUMA118_QA_STORE_PASS']=keys['storePassword'];env['LUMA118_QA_KEY_PASS']=keys['keyPassword']
    apk=out/'expedition-observer.apk'
    call(tools/'apksigner','sign','--ks',ROOT/keys['storeFile'],'--ks-key-alias',keys['keyAlias'],'--ks-pass','env:LUMA118_QA_STORE_PASS','--key-pass','env:LUMA118_QA_KEY_PASS','--out',apk,build/'aligned.apk')
    call(tools/'apksigner','verify','--verbose','--print-certs',apk)
    print(json.dumps({'apk':str(apk),'sha256':hashlib.sha256(apk.read_bytes()).hexdigest(),'target':package}))

if __name__=='__main__':main()
