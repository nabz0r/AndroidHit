#!/usr/bin/env python3
"""Prepare versioned 1.22 lab assets; never overwrite historical 1.21 data."""
import hashlib,json,pathlib,re,shutil,zipfile
ROOT=pathlib.Path(__file__).resolve().parents[1]
SOURCE=ROOT/'docs/validation/cognitive-lab-v122/latest'
PUBLIC=ROOT/'public/lab/cognitive'
DEST=PUBLIC/'data-v122'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
    old={str(p.relative_to(ROOT)):sha(p) for p in (PUBLIC/'data').iterdir() if p.is_file()}
    provenance=json.loads((SOURCE/'provenance.json').read_text())
    for name,record in provenance['outputs'].items():assert sha(SOURCE/name)==record['sha256'],name
    DEST.mkdir(exist_ok=True)
    for p in SOURCE.iterdir():
        if p.is_file() and p.suffix in {'.json','.gz'}:shutil.copyfile(p,DEST/p.name)
    report=json.loads((DEST/'report.json').read_text())
    bundle={'summary':report['summary'],'profiles':report['profiles'],'paired':report['pairedComparisons'],'traces':report['traces']}
    (PUBLIC/'snapshot-v122.js').write_text('window.COGNITIVE_LAB_SNAPSHOT_V122 = '+json.dumps(bundle,ensure_ascii=False,separators=(',',':'))+';\n')
    index=PUBLIC/'index.html';text=index.read_text()
    for name in ['snapshot.js','snapshot-v122.js','lab-core.js','lab.js','lab.css']:
        text=re.sub(r'(["\'])'+re.escape(name)+r'(?:\?v=[a-f0-9]+)?(["\'])',lambda m:m[1]+name+'?v='+sha(PUBLIC/name)[:16]+m[2],text)
    index.write_text(text)
    files={}
    for folder in ['scripts/cognitive_lab','scripts/test-support/android','app/src/main/java/com/androidhit/lumadrift/engine','app/src/main/java/com/androidhit/lumadrift/data','app/src/main/java/com/androidhit/lumadrift/onboarding','public/lab/cognitive']:
        for p in (ROOT/folder).rglob('*'):
            if p.is_file() and p.suffix in {'.java','.html','.css','.js','.mjs','.json','.gz','.md'}:files[str(p.relative_to(ROOT))]=p
    for name in ['scripts/run-cognitive-lab.sh','scripts/run-cognitive-lab-v122.sh','scripts/test-cognitive-lab.sh','scripts/test-cognitive-lab-v122.sh','scripts/package-cognitive-lab-v122.py','scripts/verify-cognitive-lab-v122.py','app/src/main/java/com/androidhit/lumadrift/LumaView.java','docs/COGNITIVE_LAB.md','docs/validation/cognitive-lab-v122/README.md']:
        files[name]=ROOT/name
    zip_path=PUBLIC/'cognitive-lab-v122-local.zip'
    with zipfile.ZipFile(zip_path,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as z:
        for name,p in sorted(files.items()):
            info=zipfile.ZipInfo(name,(2026,10,30,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=(0o755 if p.suffix=='.sh' else 0o644)<<16;z.writestr(info,p.read_bytes())
    assert old=={str(p.relative_to(ROOT)):sha(p) for p in (PUBLIC/'data').iterdir() if p.is_file()},'Historical data changed'
    payloads={str(p.relative_to(ROOT)):{'sha256':sha(p),'bytes':p.stat().st_size} for p in PUBLIC.rglob('*') if p.is_file() and 'tests' not in p.parts}
    receipt={'schemaVersion':1,'scope':'Versioned synthetic1.22 dashboard assets only; no hosting deployment asserted','historical121FilesPreserved':old,'simulationProvenanceSha256':sha(SOURCE/'provenance.json'),'files':payloads}
    (ROOT/'docs/validation/cognitive-lab-v122/site-manifest.json').write_text(json.dumps(receipt,indent=2)+'\n')
    print(json.dumps({'prepared':True,'historicalFilesUnchanged':len(old),'newVersion':'data-v122','localZipBytes':zip_path.stat().st_size,'labPayloads':len(payloads)}))
if __name__=='__main__':main()
