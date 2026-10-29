#!/usr/bin/env python3
"""Exercise only staged 1.20 tools; never create candidate/public HTML or contact the origin."""
import argparse
import hashlib
import importlib.util
import json
from pathlib import Path
import subprocess

ROOT=Path(__file__).resolve().parents[1]

def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output',type=Path,default=ROOT/'docs/validation/site-v1.20/staging/tool-checks')
    args=parser.parse_args();args.output.mkdir(parents=True,exist_ok=True);checks=0
    def check(value,message):
        nonlocal checks
        checks+=1
        if not value:raise AssertionError(message)
    index=ROOT/'public/index.html';before=digest(index)
    result=subprocess.run(['python3','scripts/prepare-site-v120.py','--artifact-manifest','releases/manifest-1.19.0-prototype2.json','--capture-map','docs/validation/site-v1.19/capture-map.json','--write'],cwd=ROOT,capture_output=True,text=True)
    check(result.returncode!=0,'Old release metadata must be refused')
    check(digest(index)==before,'Negative guard must not change the current public page')
    (args.output/'rejects-v119.txt').write_text(result.stdout+result.stderr)
    spec=importlib.util.spec_from_file_location('prepare120',ROOT/'scripts/prepare-site-v120.py');module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
    # Synthetic placeholders are template-only data, never persisted as a candidate or webpage.
    release={'version':'1.20.0-rc1','apks':{'production':{'version':'1.20.0-rc1','path':'releases/luma-drift-1.20.0-rc1.apk','bytes':1,'sha256':'0'*64},'lab':{'version':'1.20.0-sandbox-rc1','path':'releases/luma-drift-1.20.0-sandbox-rc1.apk','bytes':1,'sha256':'1'*64}}}
    page=module.page(release,index.read_text())
    for token in ['data-edition="1.20"','home-fr.png','shop-fr.png','victory-fr.png','site-v120.js','site-v120.css','A gift you have earned','A boutique to browse','separate from talent charge','code 28']:
        check(token in page,'Missing template contract: '+token)
    for key in ['companions','sound']:check(module.section(index.read_text(),key)==module.section(page,key),'Historical section changed: '+key)
    check('squad-fr.png' not in page and 'puzzle-fr.png' not in page,'Wrong screenshot vocabulary')
    check(digest(index)==before,'Template exercise must not write public HTML')
    js=subprocess.run(['node','--check','docs/validation/site-v1.20/staging/site-v120.js'],cwd=ROOT,capture_output=True,text=True)
    check(js.returncode==0,'Staged JavaScript syntax differs: '+js.stderr)
    blocked=subprocess.run(['python3','scripts/verify-site-v120-publication.py'],cwd=ROOT,capture_output=True,text=True)
    check(blocked.returncode==2 and 'No public-origin request made' in blocked.stderr,'Origin verification must remain blocked without a workaround')
    files=['scripts/'+name+'-v120.py' for name in ['package-release','prepare-site','check-site','deploy-site','verify-site']]
    files[-1]='scripts/verify-site-v120-publication.py'
    syntax=subprocess.run(['python3','-m','py_compile',*files],cwd=ROOT,capture_output=True,text=True)
    check(syntax.returncode==0,'Staged Python syntax differs: '+syntax.stderr)
    report={'passed':True,'checks':checks,'scope':'Staged-tool syntax, synthetic in-memory HTML structure, preservation of historical gallery/audio markup and actual refusal of 1.19 metadata before writes. No native1.20 image, package collection, prepared public source, network, browser or deployment claim.','public_index_sha256_before':before,'public_index_sha256_after':digest(index),'legacy_metadata_rejected':True,'synthetic_html_saved':False,'public_mutated':False,'network_requested':False,'source_sha256':{f:digest(ROOT/f) for f in files+['scripts/test-site-v120-staging.py','docs/validation/site-v1.20/staging/site-v120.js','docs/validation/site-v1.20/staging/site-v120.css']}}
    (args.output/'report.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps({'passed':True,'checks':checks,'public_mutated':False,'network_requested':False}))

if __name__=='__main__':main()
