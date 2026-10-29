#!/usr/bin/env python3
"""Verify the prepared 1.21 homepage, exact payload hashes and preserved gallery/legal/account files; no network."""
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
        if tag=='body':self.edition=attrs.get('data-edition')
        for key in ('href','src','data-screenshot-fr','data-screenshot-en'):
            if key in attrs:self.refs.append(attrs[key])

def main():
    proof=json.loads((ROOT/'docs/validation/site-v1.21/release/source-manifest.json').read_text());checks=0
    assert proof['status']=='PASS' and proof['edition']=='1.21' and proof['native_capture_provenance_verified'];checks+=3
    for name,value in proof['files'].items():
        path=PUBLIC/name.lstrip('/');assert path.is_file() and path.stat().st_size==value['bytes'] and sha(path)==value['sha256'],'Prepared payload changed: '+name;checks+=1
    for name,digest in proof['preserved_files'].items():assert sha(PUBLIC/name)==digest,'Preserved gallery/legal/account/archive input changed: '+name;checks+=1
    for name,digest in proof['frozen_inputs'].items():assert sha(ROOT/name)==digest,'Frozen provenance input changed: '+name;checks+=1
    for update in proof.get('intentional_legal_updates',[]):
        assert update['public_path']=='public/privacy/index.html' and sha(ROOT/update['public_path'])==update['current_sha256']
        assert update['previous_sha256']!=update['current_sha256'] and update['review_record'] in proof['frozen_inputs'];checks+=3
    text=(PUBLIC/'index.html').read_text();page=Page();page.feed(text)
    assert page.edition=='1.21' and page.languages=={'fr','en'};checks+=2
    for anchor in ('main','hero-title','little-world','progression','simulation','screens','companions','sound','download'):assert anchor in page.ids;checks+=1
    for reference in page.refs:
        url=urllib.parse.urlsplit(reference)
        if url.scheme or url.netloc:continue
        if not url.path:
            if url.fragment:assert url.fragment in page.ids,'Missing target: '+reference
        else:
            path=(PUBLIC/url.path.lstrip('/')).resolve();assert PUBLIC in path.parents and path.is_file(),'Missing local page asset: '+reference
        checks+=1
    for key in ('companions','sound'):
        fragment=re.search(r'<section id="'+key+r'"(?=\s|>).*?</section>',text,re.S).group(0)
        assert hashlib.sha256(fragment.encode()).hexdigest()==proof['gallery_markup_sha256' if key=='companions' else 'audio_markup_sha256'];checks+=1
    assert len(proof['screenshots'])==8 and all(capture['screenshotsEdited'] is False for capture in proof['screenshots']);checks+=1
    assert 'assets/site-v121.js' in text and 'assets/site-v121.css' in text and 'companion-gallery-v118.mjs' in text;checks+=3
    assert 'Your companion, centre stage' in text and 'Copies and forms' in text and 'living room, kitchen and garden' in text and 'A duplicate does not bring a rare result closer' in text;checks+=4
    demo=PUBLIC/'lab/ui-1-21/index.html';assert demo.is_file();demo_page=Page();demo_page.feed(demo.read_text())
    for reference in demo_page.refs:
        url=urllib.parse.urlsplit(reference)
        assert not url.scheme and not url.netloc,'Simulation must remain local and independent of account/payment services'
        if not url.path:continue
        target=(PUBLIC/url.path.lstrip('/')) if url.path.startswith('/') else (demo.parent/url.path)
        assert target.resolve().is_file(),'Missing demo asset: '+reference;checks+=1
    assert 'Android' in demo.read_text() and 'simulation-core.mjs' in (demo.parent/'simulation.mjs').read_text();checks+=2
    print(json.dumps({'passed':True,'checks':checks,'edition':'1.21','payloads':len(proof['files']),'screenshots':8,'simulation':'independent fictional browser state','networkRequested':False,'scope':'Source/provenance/link contracts only; browser and Hosting delivery remain separate.'}))

if __name__=='__main__':main()
