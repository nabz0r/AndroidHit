#!/usr/bin/env python3
"""Check current1.22 source contracts; --structure-only needs no APKs, large art or native receipts."""
import argparse
import hashlib
import json
from html.parser import HTMLParser
from pathlib import Path
import re
import urllib.parse

ROOT=Path(__file__).resolve().parents[1]
PUBLIC=ROOT/'public'

def sha(path):
    digest=hashlib.sha256()
    with path.open('rb') as source:
        for chunk in iter(lambda:source.read(1024*1024),b''):digest.update(chunk)
    return digest.hexdigest()

class Page(HTMLParser):
    def __init__(self):super().__init__();self.ids=set();self.refs=[];self.languages=set();self.edition=''
    def handle_starttag(self,tag,values):
        attrs=dict(values)
        if 'id' in attrs:assert attrs['id'] not in self.ids,'Duplicate HTML ID';self.ids.add(attrs['id'])
        if 'data-lang' in attrs:self.languages.add(attrs['data-lang'])
        if tag=='body':self.edition=attrs.get('data-edition','')
        for key in ('href','src','data-screenshot-fr','data-screenshot-en'):
            if key in attrs:self.refs.append(attrs[key])

def links(page,base,structure):
    checks=0
    for reference in page.refs:
        url=urllib.parse.urlsplit(reference)
        if url.scheme or url.netloc:continue
        if not url.path:
            if url.fragment:assert url.fragment in page.ids,'Missing page anchor: '+reference
        else:
            path=((PUBLIC/url.path.lstrip('/')) if url.path.startswith('/') else (base/url.path)).resolve()
            assert PUBLIC in path.parents and '..' not in Path(url.path).parts,'Escaping local reference'
            if not structure:assert path.is_file(),'Missing local payload: '+reference
        checks+=1
    return checks

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--structure-only',action='store_true');parser.add_argument('--pending',action='store_true');args=parser.parse_args()
    text=(PUBLIC/'index.html').read_text();page=Page();page.feed(text);checks=0
    assert page.edition in ('1.22','1.22-pending') and page.languages=={'fr','en'};checks+=2
    if args.pending:assert page.edition=='1.22-pending';checks+=1
    for identifier in ('main','hero-title','little-world','progression','simulation','screens','companions','sound','cognitive-lab','download'):assert identifier in page.ids;checks+=1
    checks+=links(page,PUBLIC,args.structure_only)
    assert 'assets/site-v122.js' in text and 'assets/site-v122.css' in text and 'companion-gallery-v118.mjs' in text;checks+=3
    assert 'lab/cognitive/index.html' in text and 'lab/ui-1-22/index.html' in text;checks+=2
    assert 'Two Undos are free per new attempt' in text and 'capped at 40' in text;checks+=2
    if page.edition.endswith('pending'):
        assert 'id="release-122-pending"' in text and '1.22 in preparation' in text and 'Android downloads and images below remain from candidate 1.21' in text;checks+=3
        assert not re.search(r'(?:src|href)="[^"]*(?:releases/[^"/]*1\.22|screenshots/v1\.22/)',text),'Pending page must not claim current1.22APK/native images';checks+=1
    else:
        assert '6 shared Charge' in text and 'Twelve new boards' in text and '120 Coins' in text and 'No payment, account, reward or Android file changes' in text;checks+=4
    demo=PUBLIC/'lab/ui-1-22/index.html';demo_source=demo.read_text();demo_page=Page();demo_page.feed(demo_source);checks+=links(demo_page,demo.parent,args.structure_only)
    script=(demo.parent/'simulation.mjs').read_text();css=(demo.parent/'simulation.css').read_text()
    assert not re.search(r'\b(?:fetch|XMLHttpRequest|WebSocket|localStorage)\b',script),'Demo must remain memory-only without account/server APIs';checks+=1
    assert 'connect-src \'none\'' in demo_source and 'fictional' in script and 'This is neither an Android capture' in script;checks+=3
    for identifier in ('lc-map-tab','lc-key-tab','lc-viewport','lc-minimap','lc-recenter','lc-key-test'):assert identifier in demo_page.ids;checks+=1
    assert 'prefers-reduced-motion' in css and 'aria-live="polite"' in demo_source and 'Choose the destination level' in script and 'Choose a key' in script;checks+=4
    if not args.structure_only and page.edition=='1.22':
        proof=json.loads((ROOT/'docs/validation/site-v1.22/release/source-manifest.json').read_text())
        assert proof['status']=='PASS' and proof['edition']=='1.22' and proof['native_capture_provenance_verified'] and proof['package_identity_verified'];checks+=4
        for name,value in proof['files'].items():
            path=PUBLIC/name.lstrip('/');assert path.is_file() and path.stat().st_size==value['bytes'] and sha(path)==value['sha256'],'Prepared payload changed: '+name;checks+=1
        for name,digest in proof['preserved_files'].items():assert sha(PUBLIC/name)==digest,'Historical gallery/privacy/auth/audio/APK changed: '+name;checks+=1
        for name,digest in proof['frozen_inputs'].items():assert sha(ROOT/name)==digest,'Frozen input changed: '+name;checks+=1
        assert len(proof['screenshots'])==8 and len(set(proof['screen_keys']))==4 and {(item['key'],item['locale']) for item in proof['screenshots']}=={(key,locale) for key in proof['screen_keys'] for locale in ('fr','en')};checks+=1
        assert all(item['screenshotsEdited'] is False and item['apkSha256']==proof['production_apk_sha256'] for item in proof['screenshots']);checks+=1
        for key,label in (('companions','gallery_markup_sha256'),('sound','audio_markup_sha256')):
            fragment=re.search(r'<section id="'+key+r'"(?=\s|>).*?</section>',text,re.S).group(0);assert hashlib.sha256(fragment.encode()).hexdigest()==proof[label];checks+=1
    print(json.dumps({'passed':True,'checks':checks,'edition':page.edition,'structureOnly':args.structure_only,'nativeEvidenceRequired':not args.structure_only and page.edition=='1.22','networkRequested':False,'scope':'Source/link/provenance scope only. Browser and Hosting/public delivery are separate.'}))

if __name__=='__main__':main()
