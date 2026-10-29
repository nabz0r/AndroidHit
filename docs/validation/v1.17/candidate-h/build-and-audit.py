#!/usr/bin/env python3
"""Build and independently audit immutable candidate H. No device or publication."""
from pathlib import Path
import datetime, hashlib, json, os, shutil, subprocess, time, xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[4]
OUT=Path(__file__).resolve().parent
TMP=Path('/tmp/luma-v117-candidate-h')
JDK=Path('/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home')
COMMIT='bb622960f52e5ba41e75a6dfa8b30dbb4b98a291'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def now():return datetime.datetime.now(datetime.timezone.utc).isoformat()
def write(name,d):(OUT/name).write_text(json.dumps(d,indent=2)+'\n')
def inputs():
    files=[p for p in (ROOT/'app/src').rglob('*') if p.is_file()]
    files.extend(ROOT/n for n in ['app/build.gradle','build.gradle','settings.gradle','gradle.properties','app/proguard-rules.pro'])
    return {str(p.relative_to(ROOT)):sha(p) for p in sorted(files)}
def main():
    assert not (OUT/'build-command.json').exists(),'Preserve previous attempt'
    assert subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip()==COMMIT
    frozen=inputs();assert len(frozen)==162
    write('source-freeze.json',frozen)
    old=json.loads((ROOT/'docs/validation/v1.17/candidate-g/source-freeze.json').read_text())
    changed=[n for n in old if frozen.get(n)!=old[n]];added=[n for n in frozen if n not in old];same=[n for n in old if frozen.get(n)==old[n]]
    assert changed==['app/build.gradle','app/src/main/java/com/androidhit/lumadrift/LumaView.java']
    assert added==['app/src/main/java/com/androidhit/lumadrift/ui/PlaySceneLayout.java'] and len(same)==159
    protected={kind:{n:old[n] for n in old if predicate(n)} for kind,predicate in {
        'engine':lambda n:'/engine/' in n,
        'audio':lambda n:'/audio/' in n,
        'art_v117_png':lambda n:'/art/v117/' in n and n.endswith('.png')}.items()}
    assert all(frozen[n]==digest for group in protected.values() for n,digest in group.items())
    write('change-from-g.json',{'candidate':'H 1.17.0-rc6/code24','source_commit':COMMIT,'old_inputs':len(old),'new_inputs':len(frozen),'unchanged_inputs':len(same),'changed':changed,'added':added,'protected_unchanged_counts':{k:len(v) for k,v in protected.items()},'protected_inputs':protected})
    command=['./gradlew',':app:assembleProductionRelease',':app:bundleProductionRelease',':app:assembleLabRelease',':app:bundleLabRelease',':app:lintProductionRelease',':app:lintLabRelease','--console=plain']
    env=dict(os.environ,JAVA_HOME=str(JDK));begin=time.monotonic()
    write('build-command.json',{'command':command,'cwd':str(ROOT),'java_home':str(JDK),'started_at_utc':now(),'source_commit':COMMIT})
    with (OUT/'build.log').open('w') as output:
        result=subprocess.run(command,cwd=ROOT,env=env,stdout=output,stderr=subprocess.STDOUT)
    write('build-exit.json',{'exit_code':result.returncode,'seconds':round(time.monotonic()-begin,3),'finished_at_utc':now(),'source_inputs_unchanged':inputs()==frozen})
    assert result.returncode==0 and inputs()==frozen
    lint=[]
    for variant in ['production','lab']:
        src=ROOT/f'app/build/reports/lint-results-{variant}Release.xml';dest=OUT/src.name;shutil.copyfile(src,dest)
        issues=ET.parse(dest).getroot().findall('issue');counts={severity:sum(i.attrib.get('severity')==severity for i in issues) for severity in ['Fatal','Error','Warning','Information']}
        assert counts['Fatal']==counts['Error']==0
        lint.append({'variant':variant,'source':str(src.relative_to(ROOT)),'report_sha256':sha(dest),'counts':counts})
    write('lint-summary.json',{'passed':True,'reports':lint})
    command=['python3','scripts/verify-v117-binaries.py','--out',str(OUT/'binaries'),'--source-freeze',str(OUT/'source-freeze.json'),'--expected-version-code','24','--production-version','1.17.0-rc6','--lab-version','1.17.0-sandbox-rc6','--build-log',str(OUT/'build.log'),'--bundletool','/tmp/bundletool-all-1.18.1.jar']
    write('binary-audit-command.json',{'command':command,'cwd':str(ROOT),'runner_sha256':sha(ROOT/'scripts/verify-v117-binaries.py'),'scope':'Public binary metadata/signature/native16KB verification. No signing secrets, device or publication.'})
    with (OUT/'binary-audit.log').open('w') as output:
        result=subprocess.run(command,cwd=ROOT,env=env,stdout=output,stderr=subprocess.STDOUT)
    assert result.returncode==0,'Binary audit failed; preserve logs'
    assert inputs()==frozen
    TMP.mkdir(exist_ok=True)
    artifacts=[]
    for variant,package,version in [('production','com.androidhit.lumadrift','1.17.0-rc6'),('lab','com.androidhit.lumadrift.sandbox','1.17.0-sandbox-rc6')]:
        for kind in ['apk','aab']:
            src=ROOT/(f'app/build/outputs/apk/{variant}/release/app-{variant}-release.apk' if kind=='apk' else f'app/build/outputs/bundle/{variant}Release/app-{variant}-release.aab')
            dest=TMP/f'luma-drift-{version}.{kind}';assert not dest.exists(),'Never overwrite frozen artifacts'
            shutil.copyfile(src,dest);dest.chmod(0o444);assert sha(src)==sha(dest)
            artifacts.append({'path':str(dest),'bytes':dest.stat().st_size,'sha256':sha(dest),'package':package})
    record={'candidate':'H 1.17.0-rc6/code24','source_commit':COMMIT,'source_freeze_sha256':sha(OUT/'source-freeze.json'),'audit_sha256':sha(OUT/'binaries/binary-final-candidate.json'),'verified_at_utc':now(),'artifacts':artifacts,'scope':'Four built and audited immutable local binaries. No native acceptance or public delivery inferred.'}
    write('candidate.json',record);(TMP/'candidate.json').write_text(json.dumps(record,indent=2)+'\n')
    print(json.dumps(record,indent=2),flush=True)
if __name__=='__main__':main()
