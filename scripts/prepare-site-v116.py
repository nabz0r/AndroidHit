#!/usr/bin/env python3
"""Prepare the existing Firebase site only from the exact frozen 1.16 release and native captures.

All inputs pass before any public file is written. Images are copied byte for byte;
there is no image generation/transformation, build, browser, network or deployment.
"""
import argparse
import hashlib
import json
from pathlib import Path
import re
import struct

ROOT=Path(__file__).resolve().parents[1]
FOLDER=ROOT/'docs/validation/site-v1.16'
VERSION='1.16.0-rc1'
SCREEN_KEYS=('adventure','home','collection','atelier','profile','summon','reveal','badges')

def sha(raw):return hashlib.sha256(raw).hexdigest()

def safe(value,prefix):
    assert isinstance(value,str) and value.startswith(prefix),value
    path=(ROOT/value).resolve()
    assert ROOT.resolve() in path.parents,value
    return path

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apk-sha256',required=True,help='Independent hash of the accepted final signed APK')
    parser.add_argument('--capture-map',type=Path,default=FOLDER/'capture-map.json')
    parser.add_argument('--check-only',action='store_true',help='Validate final inputs without writing any output')
    args=parser.parse_args()
    assert re.fullmatch(r'[a-f0-9]{64}',args.apk_sha256),'Independent final APK hash required'
    release_path=ROOT/'releases'/('manifest-'+VERSION+'.json')
    release=json.loads(release_path.read_text())
    assert release['version']==VERSION and release['version_code']==18
    assert release['application_id']=='com.androidhit.lumadrift' and release['channel']=='production'
    config=release['configuration']
    assert config.get('paid_cosmetic_draws_enabled') is False and config.get('force_test_ads') is True
    assert config.get('wallet_api_configured') is False and config.get('live_services_validated') is False
    pending=[];sizes={}
    for extension in ('apk','aab'):
        name='luma-drift-'+VERSION+'.'+extension;data=(ROOT/'releases'/name).read_bytes()
        entry=release['artifacts'][name]
        assert sha(data)==entry['sha256'] and len(data)==entry['bytes'],name
        if extension=='apk':assert sha(data)==args.apk_sha256,'Manifest and independent final APK differ'
        sizes[extension]=len(data);pending.append((ROOT/'public/releases'/name,data))
    pending.append((ROOT/'public/releases'/release_path.name,release_path.read_bytes()))
    declared=json.loads(args.capture_map.read_text())['images'];records=[];seen=set()
    expected={'public/assets/screenshots/v1.16/'+key+'-'+locale+'.png' for key in SCREEN_KEYS for locale in ('fr','en')}
    for item in declared:
        public=safe(item['public'],'public/assets/screenshots/v1.16/')
        assert item['public'] in expected and item['public'] not in seen,item['public'];seen.add(item['public'])
        source_prefix=next((prefix for prefix in ('docs/screenshots/v1.16/','docs/validation/v1.16/') if item['source'].startswith(prefix)),None)
        assert source_prefix,item['source'];original=safe(item['source'],source_prefix)
        evidence=safe(item['evidence'],'docs/validation/v1.16/')
        assert not any(token in item['source'].lower() for token in ('analysis-crop','analysis_crop','/analysis/')),item['source']
        data=original.read_bytes();assert data.startswith(b'\x89PNG\r\n\x1a\n') and len(data)>=24
        width,height=struct.unpack('>II',data[16:24]);assert (width,height)==(1080,2340),'Full-size native QA display expected'
        digest=sha(data);report=json.loads(evidence.read_text())
        assert report.get('passed') is True and (report.get('apk_sha256') or report.get('game_apk_sha256'))==args.apk_sha256,'Capture must pass on the accepted final APK'
        indexed=report.get('screenshots',report.get('evidence_sha256',{}))
        assert any(value==digest and (item['source']==key or item['source'].endswith('/'+key)) for key,value in indexed.items()),item['source']
        assert item['locale'] in ('fr','en') and item['public'].endswith('-'+item['locale']+'.png')
        original_locale=re.search(r'-(fr|en)\.png$',item['source'])
        if original_locale:assert original_locale.group(1)==item['locale'],'Capture filename and declared locale differ'
        assert item.get('description'),'Actual capture scope must be described'
        record=dict(item,sha256=digest,width=width,height=height);records.append(record);pending.append((public,data))
    assert seen==expected,'Exactly eight final screens in both FR and EN are required'
    existing={str(path.relative_to(ROOT)) for path in (ROOT/'public/assets/screenshots/v1.16').glob('*.png')}
    assert existing<=expected,'Unexpected current-edition PNG; resolve explicitly before preparing'
    template=(FOLDER/'index.template.html').read_text()
    replacements={'__APK_SHA256__':args.apk_sha256,'__APK_BYTES__':f'{sizes["apk"]:,}'.replace(',',' ')}
    for kind in ('apk','aab'):
        size=f'{sizes[kind]/1e6:.2f}'
        replacements['__'+kind.upper()+'_SIZE_EN__']=size
        replacements['__'+kind.upper()+'_SIZE_FR__']=size.replace('.',',')
    for key,value in replacements.items():
        assert key in template,'Unused preparation field: '+key;template=template.replace(key,value)
    assert not re.search(r'__[A-Z0-9_]+__|TODO|TBD',template),'Unresolved public placeholder'
    # Publish entry HTML last locally, after all final binary and capture copies exist.
    pending.append((ROOT/'public/index.html',template.encode()))
    output={'schemaVersion':1,'release':VERSION,'production_apk_sha256':args.apk_sha256,'screenshotsEdited':False,'images':records}
    if not args.check_only:
        for path,data in pending:path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(data)
        (FOLDER/'screenshot-provenance.json').write_text(json.dumps(output,indent=2)+'\n')
    print(json.dumps({'passed':True,'check_only':args.check_only,'release':VERSION,'native_originals':len(records),'hosted_production_apk_sha256':args.apk_sha256,'binary_files':3,'entry_html_generated_from_exact_release':True}))

if __name__=='__main__':main()
