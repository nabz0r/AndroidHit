#!/usr/bin/env python3
"""Independently read retained home SDK PNG pixels; never generate an expected drawing."""
from pathlib import Path
import argparse,hashlib,json,re
from PIL import Image,ImageChops,ImageStat
ROOT=Path(__file__).resolve().parents[2]
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    p=argparse.ArgumentParser();p.add_argument('--label',required=True);a=p.parse_args();assert re.fullmatch(r'[A-Za-z0-9_-]{1,40}',a.label)
    evidence=ROOT/'docs/validation/v1.14/ui-bugs'/('ui-sdk-'+a.label);shots=ROOT/'docs/screenshots/v1.14/ui-bugs'/('ui-sdk-'+a.label)
    output=evidence/'host-house-raster-verification.json';assert not output.exists(),'Keep original independent results; use a new run label'
    host=json.loads((evidence/'host-report.json').read_text());verified=[]
    pairs={'actual-cuddle-raster':('house-companion-moved-en.png','house-cuddle-idle-analysis-crop.png','house-cuddle-active-en.png','house-cuddle-active-analysis-crop.png'),'actual-lamp-switch-raster':('house-lamp-state-a-en.png','house-lamp-state-a-analysis-crop.png','house-lamp-state-b-en.png','house-lamp-state-b-analysis-crop.png')}
    for phase in ['prepare','cold']:
        actions=evidence/phase/'actions.json'
        if not actions.is_file():continue
        for native in json.loads(actions.read_text()):
            kind=native.get('observation')
            if kind not in pairs:continue
            before_frame,before_crop,after_frame,after_crop=pairs[kind];bounds=tuple(map(int,re.findall(r'-?\d+',native['bounds'])));assert len(bounds)==4
            observations=[]
            for frame_name,crop_name in [(before_frame,before_crop),(after_frame,after_crop)]:
                frame=shots/phase/frame_name;crop=shots/phase/crop_name
                with Image.open(frame) as original,Image.open(crop) as actual:
                    extracted=original.convert('RGB').crop(bounds);actual=actual.convert('RGB');assert actual.size==extracted.size and actual.tobytes()==extracted.tobytes(),'Retained crop differs from exact original native pixels: '+crop_name
                    observations.append({'frame':str(frame.relative_to(ROOT)),'crop':str(crop.relative_to(ROOT)),'frame_sha256':digest(frame),'crop_sha256':digest(crop),'pixel_exact_unscaled_crop':True})
            with Image.open(shots/phase/before_crop) as before,Image.open(shots/phase/after_crop) as after:
                before=before.convert('RGB');after=after.convert('RGB');assert before.size==after.size
                delta=ImageChops.difference(before,after);red,green,blue=delta.split();maximum=ImageChops.lighter(ImageChops.lighter(red,green),blue);changed=sum(maximum.histogram()[9:]);mean=sum(ImageStat.Stat(delta).mean)/3
                assert changed==native['changedPixels'],'Independent RGB pixel count differs: '+kind
                assert abs(mean-native['meanAbsoluteRgb'])<1e-10,'Independent RGB mean differs: '+kind
            verified.append({'phase':phase,'kind':kind,'bounds':bounds,'native_count_matches':True,'changed_pixels':changed,'mean_absolute_rgb':mean,'observations':observations})
    assert verified,'No native home raster observations in this run'
    report={'measurement_verified':True,'product_native_passed':host.get('passed',False),'scope':'Independent Pillow/ImageChops read of retained native PNG pixels; no expected renderer image or human preference score','method':'Exact unscaled original crop at observed native bounds; maximum RGB channel difference above8','host_report_sha256':digest(evidence/'host-report.json'),'analysis_source_sha256':digest(Path(__file__)),'comparisons':verified,'limits':'Only retained before/after frames are independently re-read. Idle noise sampling and any third restoration observation remain separate native checks.'}
    output.write_text(json.dumps(report,indent=2)+'\n');print(json.dumps({'measurement_verified':True,'product_native_passed':report['product_native_passed'],'comparisons':len(verified),'output':str(output)}))
if __name__=='__main__':main()
