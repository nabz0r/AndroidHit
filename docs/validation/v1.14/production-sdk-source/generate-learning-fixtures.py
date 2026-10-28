#!/usr/bin/env python3
"""Freeze authored N UI certificates only after the catalogue owner declares it ready."""
from pathlib import Path
import argparse, hashlib, json, os, shutil, subprocess, tempfile
ROOT=Path(__file__).resolve().parents[2]
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
 p=argparse.ArgumentParser();p.add_argument('--count',type=int,default=1000);p.add_argument('--catalog-ready',action='store_true');a=p.parse_args()
 if not a.catalog_ready:raise SystemExit('Catalogue is not frozen by this command; require its owner readiness before --catalog-ready')
 if not 1<=a.count<=1000:raise SystemExit('Count must be1..1000')
 java=Path(os.environ.get('JAVA_HOME','/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home'))/'bin'
 sources=sorted((ROOT/'app/src/main/java/com/androidhit/lumadrift/engine').glob('*.java'));oracle=ROOT/'scripts/v114-ui-test/LearningUiFixture.java';source_hashes={str(p.relative_to(ROOT)):digest(p)for p in [*sources,oracle]}
 output=ROOT/'scripts/v114-ui-test/assets/fixtures'
 if (output/'learning-manifest.json').exists() or any(output.glob('n[0-9]*.json')):raise SystemExit('N fixtures already frozen; preserve the exact inventory before an explicit new generation')
 with tempfile.TemporaryDirectory(prefix='luma-v114-n-fixtures-')as tmp:
  base=Path(tmp);classes=base/'classes';generated=base/'generated';classes.mkdir()
  subprocess.run([str(java/'javac'),'-d',str(classes),*map(str,sources),str(oracle)],check=True)
  subprocess.run([str(java/'java'),'-Xmx1g','-cp',str(classes),'LearningUiFixture',str(generated),str(a.count)],check=True)
  assert source_hashes=={str(p.relative_to(ROOT)):digest(p)for p in [*sources,oracle]},'Catalogue sources changed during generation; no fixtures copied'
  for n in range(1,a.count+1):
   value=json.loads((generated/f'n{n}.json').read_text());assert value['family']=='N'and value['number']==n and value['minimum']==len(value['steps'])and value['minimum']<=value['base']
  output.mkdir(parents=True,exist_ok=True)
  for file in generated.glob('n*.json'):shutil.copyfile(file,output/file.name)
 proof={'scope':'Authored N certificates mechanically replayed on an independent host Board; no Android/profile injection or independent shortest-path proof','count':a.count,'source_sha256':source_hashes,'fixture_sha256':{p.name:digest(p)for p in sorted(output.glob('n[0-9]*.json'))}}
 (output/'learning-manifest.json').write_text(json.dumps(proof,indent=2)+'\n')
if __name__=='__main__':main()
