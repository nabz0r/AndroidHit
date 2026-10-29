#!/usr/bin/env python3
"""Check the unpublished 1.17 template only; never approve a release or use fake captures."""
import argparse
import hashlib
import importlib.util
import json
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
SPEC = importlib.util.spec_from_file_location('site117', ROOT/'scripts/check-site-v117.py')
SITE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(SITE)

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output',type=Path,default=ROOT/'docs/validation/site-v1.17/staging-check.json')
    args=parser.parse_args()
    template = ROOT/'docs/validation/site-v1.17/index.template.html'
    document = SITE.BASE.Document(template)
    audit = SITE.Audit.__new__(SITE.Audit)
    audit.documents = {(ROOT/'public/index.html').resolve(): document}
    audit.checks = []
    audit.native_inputs = set()
    # Reuse the actual final edition checks, retaining the expected placeholder
    # failure as an explicit staging condition rather than forging release data.
    audit.edition_contract()
    expected = [x for x in audit.checks if x['message']=='No unresolved release placeholders']
    assert len(expected)==1 and expected[0]['pass'] is False
    expected[0].update(pass_expected_in_staging=True)
    placeholders = sorted(set(re.findall(r'__[A-Z0-9_]+__', template.read_text())))
    audit.check(set(placeholders)=={'__APK_SHA256__','__APK_BYTES__','__APK_SIZE_EN__','__APK_SIZE_FR__','__AAB_SIZE_EN__','__AAB_SIZE_FR__','__LEVEL_PROOF__'}, 'Exactly the seven final evidence placeholders remain')
    audit.check(document.doctype and not document.duplicates, 'HTML doctype and unique identifiers')
    audit.check(document.tags('body')[0].attrs.get('data-edition')=='1.17', 'New edition controller is scoped explicitly')
    for lang in ('fr','en'):
        for node in document.tags('a')+document.tags('button')+document.tags('audio'):
            if node.visible(lang):
                audit.check(bool(SITE.BASE.accessible_name(node,document,lang)), 'Accessible '+lang+' '+node.tag+' at line '+str(node.line))
    for node in document.tags('a'):
        href=node.attrs.get('href','')
        if href.startswith('#'):
            audit.check(href[1:] in document.ids, 'Anchor destination exists: '+href)
    screen_paths = {node.attrs[attr] for node in document.tags('img') for attr in ('src','data-screenshot-fr','data-screenshot-en') if attr in node.attrs and 'screenshots/' in node.attrs[attr]}
    expected_screens={'assets/screenshots/v1.17/'+key+'-'+lang+'.png' for key in ('adventure','home','collection','atelier','profile','summon','reveal','badges') for lang in ('fr','en')}
    audit.check(screen_paths==expected_screens, 'Sixteen final native screen references await genuine captures')
    audit.check(not document.tags('iframe') and not document.tags('video'), 'No external media embed or background video')
    failures=[x for x in audit.checks if not x['pass'] and not x.get('pass_expected_in_staging')]
    report={'passed':not failures,'scope':'Unpublished template DOM, copy and exact local DSP files only; not browser rendering, native capture, APK or release acceptance','release_ready':False,'checks':len(audit.checks),'failures':failures,'expected_final_placeholders':placeholders,'template_sha256':hashlib.sha256(template.read_bytes()).hexdigest(),'public_index_sha256':hashlib.sha256((ROOT/'public/index.html').read_bytes()).hexdigest()}
    output=args.output.resolve()
    assert ROOT/'docs/validation/site-v1.17' in output.parents,'Staging evidence must remain inside its current-edition folder'
    output.parent.mkdir(parents=True,exist_ok=True)
    output.write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report,indent=2))
    return 0 if not failures else 1

if __name__=='__main__':
    sys.exit(main())
