#!/usr/bin/env python3
"""Check1.24 source/link/art contracts. Browser/native/Hosting remain separate gates."""
import argparse,hashlib,json,re,urllib.parse
from html.parser import HTMLParser
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];PUBLIC=ROOT/'public';STAGING=ROOT/'docs/validation/site-v1.24/staging'
def sha(p):
 h=hashlib.sha256()
 with p.open('rb')as f:
  for b in iter(lambda:f.read(1024*1024),b''):h.update(b)
 return h.hexdigest()
class Page(HTMLParser):
 def __init__(self):super().__init__();self.ids=set();self.refs=[];self.languages=set();self.edition='';self.images=[];self.audio=0;self.native_slots=[]
 def handle_starttag(self,tag,values):
  a=dict(values)
  if 'id'in a:assert a['id'] not in self.ids,'Duplicate HTML ID';self.ids.add(a['id'])
  if 'data-lang'in a:self.languages.add(a['data-lang'])
  if tag=='body':self.edition=a.get('data-edition','')
  for k in ('href','src','data-screenshot-fr','data-screenshot-en'):
   if k in a:self.refs.append(a[k])
  if tag=='img':self.images.append(a)
  if tag=='audio':self.audio+=1;assert 'autoplay'not in a and a.get('preload')=='none','Audio must remain user-started'
  if 'data-native-slot'in a:self.native_slots.append(a['data-native-slot'])
def check(index,structure_only=False):
 text=index.read_text();page=Page();page.feed(text);checks=0
 assert page.edition in ('1.24-pending','1.24')and page.languages=={'fr','en'};checks+=2
 for key in ('main','hero-title','little-world','progression','home-world','simulation','screens','companions','sound','cognitive-lab','download'):assert key in page.ids,key;checks+=1
 for ref in page.refs:
  url=urllib.parse.urlsplit(ref)
  if url.scheme or url.netloc:continue
  if not url.path:
   if url.fragment:assert url.fragment in page.ids,'Missing anchor '+ref
  else:
   path=(PUBLIC/url.path.lstrip('/')).resolve();assert PUBLIC in path.parents and '..'not in Path(url.path).parts,'Escaping reference '+ref
   if not structure_only:assert path.is_file(),'Missing local payload '+ref
  checks+=1
 for name in ('site-v124.css','site-v124.js','companion-gallery-v118.mjs'):assert name in text;checks+=1
 for phrase in ('120 XP','92 copies','45 Coins','25, 30 or 35 XP','Two Undos are free per new attempt','capped at 40','48 furniture','4 rooms','18 pieces of equipment','GUIDE costs 2 Charge','level 1 = 1 use','level 2 = 2','levels 3–5 = 3','levels 6–10 = 4','not human perception or emotion'):assert phrase in text,phrase;checks+=1
 assert page.audio==4;checks+=1
 for key in ('companions','sound'):
  expected=json.loads((STAGING/'preserved-sections.json').read_text())['sections'][key]
  actual=re.search(r'<section id="'+key+r'"(?=\s|>).*?</section>',text,re.S).group(0)
  assert hashlib.sha256(actual.encode()).hexdigest()==expected,'Retained '+key+' markup changed';checks+=1
 for name,row in json.loads((STAGING/'art-copies.json').read_text())['files'].items():
  assert re.fullmatch('[a-f0-9]{64}',row['sha256'])and row['bytes']>0 and Path(name).name==name;checks+=1
  if not structure_only:
   target=PUBLIC/'assets/art-v124'/name;assert target.stat().st_size==row['bytes']and sha(target)==row['sha256']and sha(ROOT/row['source'])==row['sha256'],'Native art/font differs '+name;checks+=1
 previous=json.loads((ROOT/'docs/validation/site-v1.23/release/source-manifest.json').read_text())
 protected={name:row['sha256'] for name,row in previous['files'].items() if name.lstrip('/')!='index.html'}
 if not structure_only:
  for name,digest in protected.items():assert sha(PUBLIC/name.lstrip('/'))==digest,'Historical public payload changed '+name;checks+=1
 home=(ROOT/'app/src/main/java/com/androidhit/lumadrift/data/HomeCatalog.java').read_text();gear=(ROOT/'app/src/main/java/com/androidhit/lumadrift/data/AvatarGearCatalog.java').read_text()
 assert len(re.findall(r'new Item\("',home))==48 and len(re.findall(r'new Item\("',gear))==18;checks+=2
 rooms=re.search(r'ROOMS=.*?Arrays.asList\((.*?)\)\)',home).group(1);assert len(re.findall(r'"[a-z]+"',rooms))==4;checks+=1
 if page.edition.endswith('pending'):
  assert set(page.native_slots)=={'puzzle','map','companion','home'}and len(page.native_slots)==4;checks+=1
  assert '1.24 in preparation'in text and '1.24.0-rc2 · code 33'in text;checks+=2
  assert not any('/screenshots/v1.'in ref for ref in page.refs),'Preview must not present old/missing Android images as current';checks+=1
  assert not any(re.search(r'/releases/[^/]*1\.24',ref)for ref in page.refs),'Unverified1.24 artifact link';checks+=1
  if not structure_only:assert sha(PUBLIC/'index.html')==json.loads((STAGING/'preserved-sections.json').read_text())['sourceIndexSha256'],'Active public index changed during staging';checks+=1
 elif not structure_only:
  proof=json.loads((ROOT/'docs/validation/site-v1.24/release/source-manifest.json').read_text());assert proof['status']=='PASS'and proof['edition']=='1.24'and proof['package_identity_verified']and proof['native_capture_provenance_verified'];checks+=4
  assert len(proof['screenshots'])==8 and {(s['key'],s['locale'])for s in proof['screenshots']}=={(k,l)for k in ('puzzle','map','companion','home')for l in ('fr','en')};checks+=1
  assert all(s['screenshotsEdited']is False and s['apkSha256']==proof['production_apk_sha256']for s in proof['screenshots']);checks+=1
  for name,row in proof['files'].items():assert (PUBLIC/name.lstrip('/')).stat().st_size==row['bytes']and sha(PUBLIC/name.lstrip('/'))==row['sha256'];checks+=1
  for name,digest in proof['frozen_inputs'].items():assert sha(ROOT/name)==digest,'Frozen release input changed '+name;checks+=1
 return {'passed':True,'checks':checks,'edition':page.edition,'index':str(index.relative_to(ROOT)),'indexSha256':sha(index),'structureOnly':structure_only,'historicalPublicFilesUnchanged':0 if structure_only else len(protected),'historicalProtectedFilesDeclared':len(protected),'nativeEvidenceRequired':not structure_only and page.edition=='1.24','networkRequested':False,'scope':'Local source contracts; full mode additionally verifies payload/byte preservation. Pending preview has no current Android captures or1.24downloads. No browser/native/human/Hosting acceptance inferred.'}
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--preview',action='store_true');p.add_argument('--structure-only',action='store_true');p.add_argument('--output',type=Path);a=p.parse_args();r=check(STAGING/'index.preview.html'if a.preview else PUBLIC/'index.html',a.structure_only)
 if a.output:a.output.parent.mkdir(parents=True,exist_ok=True);assert not a.output.exists(),'Preserve previous receipt';a.output.write_text(json.dumps(r,indent=2)+'\n')
 print(json.dumps(r))
if __name__=='__main__':main()
