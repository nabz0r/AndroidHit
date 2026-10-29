#!/usr/bin/env python3
"""Prepare the existing Firebase site only from the exact frozen 1.17 release and native captures.

All inputs pass before any public file is written. Images are copied byte for byte;
there is no image generation/transformation, build, browser, network or deployment.
"""
import argparse
import hashlib
import json
from pathlib import Path
import re
import struct
import subprocess
import sys
import tempfile

ROOT=Path(__file__).resolve().parents[1]
FOLDER=ROOT/'docs/validation/site-v1.17'
VERSION='1.17.0-rc6'
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
    parser.add_argument('--level-proof',type=Path,default=ROOT/'docs/validation/v1.17/relay/summary.json')
    parser.add_argument('--source-freeze',type=Path,required=True,help='Actual final APK build source-freeze.json')
    parser.add_argument('--check-only',action='store_true',help='Validate final inputs without writing any output')
    args=parser.parse_args()
    assert re.fullmatch(r'[a-f0-9]{64}',args.apk_sha256),'Independent final APK hash required'
    release_path=ROOT/'releases'/('manifest-'+VERSION+'.json')
    release=json.loads(release_path.read_text())
    assert release['version']==VERSION and release['version_code']==24
    assert release['application_id']=='com.androidhit.lumadrift' and release['channel']=='production'
    config=release['configuration']
    assert config.get('paid_cosmetic_draws_enabled') is False and config.get('force_test_ads') is True
    assert config.get('wallet_api_configured') is False and config.get('live_services_validated') is False
    # Every public 1000-level claim must be revalidated from real passing native batches.
    level_path=args.level_proof.resolve();assert ROOT.resolve() in level_path.parents and 'v1.17' in level_path.parts
    level=json.loads(level_path.read_text());assert level.get('passed') is True and level.get('candidate_apk_sha256')==args.apk_sha256
    assert level.get('levels')==1000 and level.get('code')==24 and level.get('new_first_win_coins')==0
    with tempfile.TemporaryDirectory(prefix='luma-site117-relay-audit-') as temp:
        audited=Path(temp)/'summary.json';command=[sys.executable,str(ROOT/'scripts/v117-relay-ui/summarize.py'),'--output',str(audited)]
        for entry in level['reports']:command+=['--report',entry['path']]
        subprocess.run(command,cwd=ROOT,check=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
        assert json.loads(audited.read_text())==level,'1000-level proof no longer matches its actual native evidence'
    source_freeze=args.source_freeze.resolve();assert ROOT.resolve() in source_freeze.parents and 'v1.17' in source_freeze.parts
    frozen=json.loads(source_freeze.read_text());assert frozen and all(sha((ROOT/name).read_bytes())==digest for name,digest in frozen.items()),'Final build sources changed'
    audio_path=FOLDER/'audio-provenance.json';audio=json.loads(audio_path.read_text());assert audio['release']==VERSION and audio['audioEdited'] is False
    audio_inputs={str(level_path.relative_to(ROOT)),str(source_freeze.relative_to(ROOT)),str(audio_path.relative_to(ROOT))}
    pending=[];sizes={}
    assert len(audio['files'])==4
    for entry in audio['files']:
        target=safe(entry['public'],'public/assets/audio/v1.17/');original=safe(entry['source'],'docs/validation/v1.17/audio/scenes/');evidence=safe(entry['evidence'],'docs/validation/v1.17/audio/scenes/')
        proof=json.loads(evidence.read_text());data=original.read_bytes();assert proof.get('passed') is True and proof.get('scene_biome_loops')==50 and proof.get('new_place_arrangements')==40
        assert sha(data)==entry['sha256']==proof['outputs_sha256'][original.name] and data[:4]==b'RIFF' and data[8:12]==b'WAVE'
        for name,digest in proof['source_sha256'].items():
            if name.startswith('app/') :assert frozen.get(name)==digest,'Audio previews differ from final APK synthesizer source'
        assert entry['gain']==.27 and entry['seconds']==10 and entry['biome']==0
        pending.append((target,data));audio_inputs.update((entry['source'],entry['evidence']))
    for extension in ('apk','aab'):
        name='luma-drift-'+VERSION+'.'+extension;data=(ROOT/'releases'/name).read_bytes()
        entry=release['artifacts'][name]
        assert sha(data)==entry['sha256'] and len(data)==entry['bytes'],name
        if extension=='apk':assert sha(data)==args.apk_sha256,'Manifest and independent final APK differ'
        sizes[extension]=len(data);pending.append((ROOT/'public/releases'/name,data))
    pending.append((ROOT/'public/releases'/release_path.name,release_path.read_bytes()))
    declared=json.loads(args.capture_map.read_text())['images'];records=[];seen=set()
    expected={'public/assets/screenshots/v1.17/'+key+'-'+locale+'.png' for key in SCREEN_KEYS for locale in ('fr','en')}
    for item in declared:
        public=safe(item['public'],'public/assets/screenshots/v1.17/')
        assert item['public'] in expected and item['public'] not in seen,item['public'];seen.add(item['public'])
        source_prefix=next((prefix for prefix in ('docs/screenshots/v1.17/','docs/validation/v1.17/') if item['source'].startswith(prefix)),None)
        assert source_prefix,item['source'];original=safe(item['source'],source_prefix)
        evidence=safe(item['evidence'],'docs/validation/v1.17/')
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
    existing={str(path.relative_to(ROOT)) for path in (ROOT/'public/assets/screenshots/v1.17').glob('*.png')}
    assert existing<=expected,'Unexpected current-edition PNG; resolve explicitly before preparing'
    template=(FOLDER/'index.template.html').read_text()
    replacements={'__LEVEL_PROOF__':'<span data-lang="fr" lang="fr">Les 1 000 niveaux ont été rejoués par de vrais gestes Android sur cet APK exact, dans une partie locale déjà terminée : aucun gain de première victoire n’a été recrédité. Les vérifications des écrans, tenues, ambiances et sons sont documentées séparément. La fluidité sur ton téléphone et le plaisir dans la durée restent à éprouver.</span><span data-lang="en" lang="en" hidden>All 1,000 levels were replayed through real Android gestures on this exact APK, using an already completed local game: no first-win reward was credited again. Screens, outfits, soundscapes and audio checks are documented separately. Performance on your phone and long-term enjoyment still need playtesting.</span>','__APK_SHA256__':args.apk_sha256,'__APK_BYTES__':f'{sizes["apk"]:,}'.replace(',',' ')}
    for kind in ('apk','aab'):
        size=f'{sizes[kind]/1e6:.2f}'
        replacements['__'+kind.upper()+'_SIZE_EN__']=size
        replacements['__'+kind.upper()+'_SIZE_FR__']=size.replace('.',',')
    for key,value in replacements.items():
        assert key in template,'Unused preparation field: '+key;template=template.replace(key,value)
    assert not re.search(r'__[A-Z0-9_]+__|TODO|TBD',template),'Unresolved public placeholder'
    # Publish entry HTML last locally, after all final binary and capture copies exist.
    pending.append((ROOT/'public/index.html',template.encode()))
    output={'schemaVersion':1,'release':VERSION,'production_apk_sha256':args.apk_sha256,'screenshotsEdited':False,'images':records,'level_proof':str(level_path.relative_to(ROOT)),'level_proof_sha256':sha(level_path.read_bytes()),'source_freeze':str(source_freeze.relative_to(ROOT)),'source_freeze_sha256':sha(source_freeze.read_bytes()),'additional_inputs':sorted(audio_inputs)}
    if not args.check_only:
        for path,data in pending:path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(data)
        (FOLDER/'screenshot-provenance.json').write_text(json.dumps(output,indent=2)+'\n')
    print(json.dumps({'passed':True,'check_only':args.check_only,'release':VERSION,'native_originals':len(records),'hosted_production_apk_sha256':args.apk_sha256,'binary_files':3,'entry_html_generated_from_exact_release':True}))

if __name__=='__main__':main()
