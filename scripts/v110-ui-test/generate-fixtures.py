#!/usr/bin/env python3
"""Generate source-certified V4 actions. Never launches or modifies Android."""
from pathlib import Path
import argparse, hashlib, json, os, re, shutil, subprocess, tempfile
ROOT=Path(__file__).resolve().parents[2]
JAVA=Path(os.environ.get('JAVA_HOME','/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home'))/'bin'
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    p=argparse.ArgumentParser();p.add_argument('--count',type=int,default=100);a=p.parse_args()
    if not 1<=a.count<=1000:raise SystemExit('count must be1..1000')
    sources=sorted((ROOT/'app/src/main/java/com/androidhit/lumadrift/engine').glob('*.java'))
    oracle=ROOT/'scripts/v110-ui-test/StrategyUiFixture.java'
    source_hashes={str(p.relative_to(ROOT)):digest(p) for p in [*sources,oracle]}
    output=ROOT/'scripts/v110-ui-test/assets/fixtures'
    with tempfile.TemporaryDirectory(prefix='luma-v110-ui-oracle-') as temp:
        tmp=Path(temp);classes=tmp/'classes';generated=tmp/'generated';classes.mkdir()
        subprocess.run([str(JAVA/'javac'),'-d',str(classes),*map(str,sources),str(oracle)],check=True)
        subprocess.run([str(JAVA/'java'),'-Xmx1g','-cp',str(classes),'StrategyUiFixture',str(generated),str(a.count)],check=True)
        assert source_hashes=={str(p.relative_to(ROOT)):digest(p) for p in [*sources,oracle]},'Source changed during oracle compilation; rerun after it settles'
        for n in range(1,a.count+1):
            value=json.loads((generated/f'l{n}.json').read_text())
            assert value['family']=='L' and value['number']==n and value['minimum']==len(value['steps']) and value['minimum']<=value['base']
        output.mkdir(parents=True,exist_ok=True)
        names={p.name for p in generated.glob('*.json')}
        for old in output.glob('*.json'):
            if old.name not in names and (re.fullmatch(r'l[0-9]+\.json',old.name) or old.name in ['recovery.json','powers.json']):old.unlink()
        for file in generated.glob('*.json'):shutil.copyfile(file,output/file.name)
    proof={'scope':'Read-only source oracle; no runtime or human difficulty claim','count':a.count,'source_sha256':source_hashes,'fixture_sha256':{p.name:digest(p) for p in sorted(output.glob('*.json')) if p.name!='manifest.json'}}
    (output/'manifest.json').write_text(json.dumps(proof,indent=2)+'\n')
if __name__=='__main__':main()
