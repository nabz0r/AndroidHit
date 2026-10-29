#!/usr/bin/env python3
"""Export original passed native 1080x2400 screenshots with exact source receipts for the site."""
from pathlib import Path
import argparse,hashlib,json,shutil,struct
ROOT=Path(__file__).resolve().parents[2]
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--phase',required=True,type=Path,action='append');p.add_argument('--apk-sha256',required=True);p.add_argument('--out',required=True,type=Path);p.add_argument('--names',nargs='+',required=True);args=p.parse_args();args.out=args.out.resolve()
phases=[]
for requested in args.phase:
 phase=requested.resolve();assert phase.is_relative_to((ROOT/'docs/validation/v1.21/native').resolve()),'Only current native task evidence'
 report=json.loads((phase/'report.json').read_text());assert report['passed'] and report['native']['passed'] and report['apk_sha256']==args.apk_sha256 and report['installed_sha256']==args.apk_sha256,'Exact passed native APK phase required'
 phases.append((phase,report))
assert not args.out.exists(),'Never overwrite earlier media exports';args.out.mkdir(parents=True)
entries=[]
for name in args.names:
 candidates=[(phase,report) for phase,report in phases if name in report['native']['captures']];assert len(candidates)==1,'Screenshot must belong to exactly one supplied passed actual execution'
 phase,report=candidates[0];source=phase/'native'/(name+'.png');raw=source.read_bytes();assert raw[:8]==b'\x89PNG\r\n\x1a\n','Original PNG required';width,height=struct.unpack('>II',raw[16:24]);assert (width,height)==(1080,2400),'Full original device pixels required'
 digest=hashlib.sha256(raw).hexdigest();assert report['evidence_sha256']['native/'+source.name]==digest,'Original recorded screenshot hash differs'
 target=args.out/source.name;shutil.copyfile(source,target);assert hashlib.sha256(target.read_bytes()).hexdigest()==digest
 entries.append({'name':name,'path':str(target.relative_to(ROOT)),'native_source':str(source.relative_to(ROOT)),'sha256':digest,'width':width,'height':height,'source_phase':report['phase'],'phase_report_sha256':hashlib.sha256((phase/'report.json').read_bytes()).hexdigest(),'apk_sha256':args.apk_sha256,'scope':'Original unedited full task-emulator pixels from exact passed native phase; no resizing, recomposition, mockup or caption replacement.'})
(args.out/'provenance.json').write_text(json.dumps({'apk_sha256':args.apk_sha256,'media':entries},ensure_ascii=False,indent=2)+'\n')
site_map=[]
for entry in entries:
 key,locale=entry['name'].rsplit('-',1);assert key in ('home','shop','map','puzzle') and locale in ('fr','en'),'Standard accepted site capture name required'
 source=ROOT/entry['native_source'];site_map.append({'key':key,'locale':locale,'source':entry['native_source'],'report':str((source.parent.parent/'report.json').relative_to(ROOT)),'sha256':entry['sha256'],'width':entry['width'],'height':entry['height'],'screenshotsEdited':False})
(args.out/'capture-map.json').write_text(json.dumps(site_map,ensure_ascii=False,indent=2)+'\n');print(json.dumps({'media':len(entries),'out':str(args.out)}))
