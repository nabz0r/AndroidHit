#!/usr/bin/env python3
"""Verify the prepared 1.19 homepage, exact payload hashes and preserved gallery/legal/account files; no network."""
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
    proof=json.loads((ROOT/'docs/validation/site-v1.19/release/source-manifest.json').read_text());checks=0
    assert proof['status']=='PASS' and proof['edition']=='1.19' and proof['native_capture_provenance_verified'];checks+=3
    for name,value in proof['files'].items():
        path=PUBLIC/name.lstrip('/');assert path.is_file() and path.stat().st_size==value['bytes'] and sha(path)==value['sha256'],'Prepared payload changed: '+name;checks+=1
    for name,digest in proof['preserved_files'].items():assert sha(PUBLIC/name)==digest,'Preserved gallery/legal/account/archive input changed: '+name;checks+=1
    for name,digest in proof['frozen_inputs'].items():assert sha(ROOT/name)==digest,'Frozen provenance input changed: '+name;checks+=1
    text=(PUBLIC/'index.html').read_text();page=Page();page.feed(text)
    assert page.edition=='1.19' and page.languages=={'fr','en'};checks+=2
    for anchor in ('main','hero-title','squad','screens','companions','sound','download'):assert anchor in page.ids;checks+=1
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
    assert len(proof['screenshots'])==6 and all(capture['screenshotsEdited'] is False for capture in proof['screenshots']);checks+=1
    assert 'assets/site-v119.js' in text and 'assets/site-v119.css' in text and 'companion-gallery-v118.mjs' in text;checks+=3
    assert '37 unique mechanics' in text and 'Two roles, Resonance' in text and 'one use per attempt' in text;checks+=3
    print(json.dumps({'passed':True,'checks':checks,'edition':'1.19','payloads':len(proof['files']),'screenshots':6,'networkRequested':False,'scope':'Source/provenance/link contracts only; browser and Hosting delivery remain separate.'}))

if __name__=='__main__':main()
