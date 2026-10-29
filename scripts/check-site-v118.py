#!/usr/bin/env python3
"""Read-only audit of the prepared 1.18 H Firebase site, original captures and lazy art copies."""
from html.parser import HTMLParser
from pathlib import Path
import argparse
import hashlib
import json
import re
import struct
import sys
from urllib.parse import urlsplit

ROOT=Path(__file__).resolve().parents[1]
PUBLIC=ROOT/'public'
EVIDENCE=ROOT/'docs/validation/site-v1.18'


def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()


class Document(HTMLParser):
    def __init__(self, text):
        super().__init__();self.elements=[];self.text={'fr':[],'en':[]};self.stack=[];self.feed(text)
    def handle_starttag(self, tag, attrs):
        attrs=dict(attrs);self.elements.append((tag,attrs));self.stack.append((tag,attrs))
        if tag in {'meta','link','img','input','source','br','hr','wbr'}: self.stack.pop()
    def handle_endtag(self, tag):
        for i in range(len(self.stack)-1,-1,-1):
            if self.stack[i][0]==tag:self.stack=self.stack[:i];break
    def handle_data(self, data):
        langs=[attrs['data-lang'] for _,attrs in self.stack if 'data-lang' in attrs]
        if langs:
            if langs[-1] in self.text:self.text[langs[-1]].append(data)
        elif not any(tag in {'script','style'} for tag,_ in self.stack):
            for locale in self.text:self.text[locale].append(data)
    def tags(self, tag):return [attrs for name,attrs in self.elements if name==tag]


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--report',type=Path,default=EVIDENCE/'local-audit.json')
    parser.add_argument('--publication',action='store_true',help='Also require immutable commit and both hosted APK copies')
    args=parser.parse_args();checks=[]
    def check(condition,label):
        checks.append({'passed':bool(condition),'message':label})
    raw=(PUBLIC/'index.html').read_text();doc=Document(raw)
    preparation=json.loads((EVIDENCE/'preparation.json').read_text())
    release=json.loads((PUBLIC/'releases/manifest-1.18.0-prototype1.json').read_text())
    candidate=json.loads((ROOT/'docs/validation/v1.18/candidates/final-art-h/candidate.json').read_text())
    check(release['sha256']==candidate['apks']['production']['sha256'] and release['labSha256']==candidate['apks']['lab']['sha256'],'Downloads identify exact frozen H production and lab bytes')
    check(release['version']=='1.18.0-prototype1' and release['versionCode']==25,'Current edition is exact1.18 prototype/code25')
    check(not re.search(r'__[A-Z0-9_]+__|TODO|TBD',raw),'No unresolved public placeholder')
    ids=[attrs['id'] for _,attrs in doc.elements if 'id' in attrs]
    check(len(ids)==len(set(ids)),'All HTML IDs are unique')
    check(all(name in ids for name in ('main','hero-title','companions','expeditions','screens','sound','download')),'Named accessible destinations exist')
    labels={attrs.get('for') for attrs in doc.tags('label')}
    check(all(attrs.get('id') in labels for attrs in doc.tags('select')),'Every gallery selector has an associated label')
    check(all(attrs.get('type')=='button' for attrs in doc.tags('button')),'All site controls have an explicit non-submit type')
    check(any(attrs.get('role')=='status' and attrs.get('aria-live')=='polite' for _,attrs in doc.elements),'Gallery loading and error announcements are accessible')
    check(all('src' in attrs for attrs in doc.tags('script')) and not doc.tags('style'),'No inline executable script or style bypasses CSP')
    check(any(attrs.get('rel')=='canonical' and attrs.get('href')=='https://luma-drift-production.web.app/' for attrs in doc.tags('link')),'Existing Firebase canonical host is retained')
    languages=[attrs for _,attrs in doc.elements if attrs.get('data-lang') in ('fr','en')]
    check(sum(a['data-lang']=='fr' for a in languages)==sum(a['data-lang']=='en' for a in languages),'FR/EN translation blocks are paired')
    for lang,text in doc.text.items():
        joined=' '.join(text).lower()
        for token in (('37','4','20','stances','expédition','copies','rareté','costume','prototype') if lang=='fr' else ('37','4','20','stances','expedition','copies','rarity','costume','prototype')):
            check(token in joined,'Current feature content exists in '+lang+': '+token)
    check(all(a.get('data-alt-fr') and a.get('data-alt-en') for a in doc.tags('img') if 'data-screenshot-fr' in a),'Native screenshots have localized alt text')
    check(len(doc.tags('audio'))==4 and all('controls' in a and a.get('preload')=='none' and 'autoplay' not in a for a in doc.tags('audio')),'Four retained audio previews require explicit play and never preload')
    check(all(sha(ROOT/name)==digest for name,digest in preparation['preservedFiles'].items()),'Host configuration, privacy/account pages, legacy script/audio and1.17 archives remain byte-identical')
    for image in preparation['images']:
        source=ROOT/image['source'];target=ROOT/image['public'];proof=json.loads((ROOT/image['report']).read_text())
        check(sha(source)==sha(target)==image['sha256'] and proof['passed'] and proof['apk_sha256']==release['sha256'] and sha(ROOT/image['report'])==image['reportSha256'],'Unedited actual H Android screenshot: '+target.name)
        check(struct.unpack('>II',target.read_bytes()[16:24])==(image['width'],image['height']),'Full native screen bounds: '+target.name)
    roster=json.loads((PUBLIC/'assets/companions-v118/roster.json').read_text())
    check(len(roster)==37 and len({row['id'] for row in roster})==37 and sum(row['guest'] for row in roster)==13,'Exactly24original and13guest identities are indexed')
    geometry_ok=True;image_ok=True;frames=0;metadata_bytes=0
    for record in preparation['sheets']:
        source=ROOT/record['sourceMetadata'];original=json.loads(source.read_text());meta=PUBLIC/record['metadata'];lean=json.loads(meta.read_text());image=PUBLIC/record['image'];metadata_bytes+=meta.stat().st_size
        image_ok &= sha(image)==record['imageSha256']==original['webpSha256'] and image.stat().st_size==record['imageBytes']
        geometry_ok &= sha(source)==record['sourceMetadataSha256']==lean['sourceMetadataSha256'] and len(lean['frames'])==20
        for index,frame in enumerate(lean['frames']):
            geometry_ok &= frame['poseIndex']==index and frame=={key:original['frames'][index][key] for key in ('poseIndex','sourceRect','originX','originY','pixelsPerUnit','sourceClip')}
            frames+=1
    check(len(preparation['sheets'])==148 and frames==2960 and geometry_ok,'All148form metadata preserve2960exact sourceClip/origin/scale records')
    check(image_ok,'All148hosted WebP copies match their original frozen hashes')
    gallery=(PUBLIC/'assets/companion-gallery-v118.mjs').read_text()
    check('let opened = false' in gallery and 'if (!opened) return' in gallery and 'new BoundedCache(4)' in gallery,'Gallery requests are gated by deliberate choice and bounded to4cache entries')
    check(not any('companions-v118/' in attrs.get('src','') or ('companions-v118/' in attrs.get('href','') and attrs.get('rel')=='preload') for _,attrs in doc.elements),'Initial DOM does not preload an atlas')
    check((PUBLIC/'assets/companions-v118/roster.json').stat().st_size<16000,'37identity roster remains below16KB')
    css=(PUBLIC/'assets/site-v118.css').read_text()
    check('@media(max-width:600px)' in css and 'prefers-reduced-motion:reduce' in css and 'min-height:44px' in css,'Mobile layout, reduced-motion and44pxcontrol rules exist')
    for attrs in doc.tags('a'):
        href=attrs.get('href','');parsed=urlsplit(href)
        if href.startswith('#'):check(href[1:] in ids,'Internal anchor resolves: '+href)
        elif parsed.scheme or href.startswith('/releases/'):continue
        else:check((PUBLIC/parsed.path).is_file(),'Local page resolves: '+href)
    if args.publication:
        check(bool(re.fullmatch('[a-f0-9]{40}',release.get('artifactCommit') or '')),'Publication has immutable artifact commit')
        for key,hashkey,sizekey in [('downloadUrl','sha256','bytes'),('labDownloadUrl','labSha256','labBytes')]:
            target=PUBLIC/release[key].lstrip('/')
            check(target.is_file() and sha(target)==release[hashkey] and target.stat().st_size==release[sizekey],'Hosted APK exactly matches frozen artifact: '+target.name)
    failures=[item for item in checks if not item['passed']]
    report={'passed':not failures,'scope':'Prepared local Firebase content, exact H image/metadata provenance and static accessibility/loading contracts. No deployment, HTTP verification, native run or apparel qualification is performed.','publicationReadyInputsChecked':args.publication,'release':release,'checks':checks,'counts':{'companions':len(roster),'sheets':len(preparation['sheets']),'poses':frames,'nativeScreenshots':len(preparation['images']),'checks':len(checks)},'weights':{'atlasBytes':preparation['atlasBytes'],'metadataBytes':metadata_bytes,'rosterBytes':(PUBLIC/'assets/companions-v118/roster.json').stat().st_size,'captureBytes':preparation['captureBytes'],'firstGalleryAtlasBytes':preparation['sheets'][0]['imageBytes'],'initialAtlasRequests':0},'inputSha256':{'public/index.html':sha(PUBLIC/'index.html'),'public/assets/site-v118.js':sha(PUBLIC/'assets/site-v118.js'),'public/assets/companion-gallery-v118.mjs':sha(PUBLIC/'assets/companion-gallery-v118.mjs'),'preparation':sha(EVIDENCE/'preparation.json')}}
    args.report.parent.mkdir(parents=True,exist_ok=True);args.report.write_text(json.dumps(report,indent=2)+'\n')
    if args.publication and not failures:
        frozen_names=['scripts/prepare-site-v118.py','scripts/check-site-v118.py','scripts/test-site-v118.mjs','scripts/deploy-site-v118.py','scripts/verify-site-v118-publication.py','docs/validation/site-v1.18/preparation.json','docs/validation/site-v1.18/preserved-files.json','releases/manifest-1.18.0-prototype1.json']
        frozen={name:sha(ROOT/name) for name in frozen_names}
        files={}
        for path in sorted(PUBLIC.rglob('*')):
            if path.is_file() and not any(part.startswith('.') or part=='tests' for part in path.relative_to(PUBLIC).parts):
                files['/'+str(path.relative_to(PUBLIC))]={'bytes':path.stat().st_size,'sha256':sha(path)}
        source={'status':'PASS','edition':'1.18','release':'1.18.0-prototype1','network_verified':False,'production_apk_sha256':release['sha256'],'lab_apk_sha256':release['labSha256'],'artifact_source_commit':release['artifactCommit'],'artifact_manifest':'releases/manifest-1.18.0-prototype1.json','artifact_manifest_sha256':sha(ROOT/'releases/manifest-1.18.0-prototype1.json'),'apk_storage_policy':'H APKs exceed100MiB and are external signed artifacts; ignored physical Hosting copies are verified by manifest SHA/size, never required as Git blobs.','firebase_config_sha256':sha(ROOT/'firebase.json'),'frozen_inputs':frozen,'files':files,'scope':'Complete exact local Hosting payload, including ignored H APK copies; native capture/art provenance audited locally. Deployment management and public response bytes remain separate.'}
        out=EVIDENCE/'release/source-manifest.json';out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(source,indent=2)+'\n')
    print(json.dumps({'passed':report['passed'],'checks':len(checks),'failures':failures,'weights':report['weights'],'report':str(args.report)}))
    if failures:sys.exit(1)


if __name__=='__main__':main()
