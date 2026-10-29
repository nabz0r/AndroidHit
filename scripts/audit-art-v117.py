#!/usr/bin/env python3
"""Inspect original packaged PNGs without modifying their pixels."""
from pathlib import Path
import hashlib
import json
import argparse
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'app/src/main/assets/art/v117'

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--out', type=Path, default=ROOT/'docs/validation/v1.17/assets/final-art-audit.json')
    args = parser.parse_args()
    names = [f'companions-{i}' for i in range(4)] + ['hats','dresses','shoes','gloves'] + [f'furniture-{i}' for i in range(7)] + ['actions-0','actions-1']
    scenes = ['world-adventure','world-collection','world-atelier','wall-living','wall-bedroom','floor-parquet']
    expected = {name+'.png' for name in names+scenes}
    assert {p.name for p in ASSETS.glob('*.png')} == expected, 'Exact original asset set required'
    records = []
    for name in names+scenes:
        path = ASSETS/(name+'.png')
        with Image.open(path) as image:
            assert image.width >= 900 and image.height >= (512 if name.startswith('wall-') else 900), name
            record = {'path':str(path.relative_to(ROOT)), 'sha256':hashlib.sha256(path.read_bytes()).hexdigest(), 'bytes':path.stat().st_size, 'size':[image.width,image.height], 'pixelsEdited':False}
            if name in names:
                assert image.mode == 'RGBA', name+' true alpha required'
                alpha = image.getchannel('A'); cells = []
                for i in range(6):
                    col,row = i%3,i//3
                    box = (col*image.width//3,row*image.height//2,(col+1)*image.width//3,(row+1)*image.height//2)
                    sample = alpha.crop(box)
                    low,high = sample.getextrema()
                    assert low == 0 and high >= 200, (name,i,'Nonempty transparent sprite required')
                    visible = sample.point(lambda value:255 if value>8 else 0)
                    trim = visible.getbbox()
                    assert trim and trim[2]-trim[0] >= 80 and trim[3]-trim[1] >= 80, (name,i,'Undersized sprite')
                    occupied = sum(visible.histogram()[1:])/sample.width/sample.height
                    assert .02 < occupied < .96, (name,i,'Degenerate alpha coverage')
                    cells.append({'index':i,'cell':box,'trimAboveAlpha8':trim,'alphaRange':[low,high],'visibleFraction':round(occupied,5)})
                record['cells'] = cells
            records.append(record)
    prompts=json.loads((ROOT/'docs/design/v1.17/art-prompts.json').read_text())
    assert prompts['mode']=='built-in imagegen' and len(prompts['assets'])==23
    for entry in prompts['assets']:
        source=Path(entry['source']); assert source.is_file(), source
        assert hashlib.sha256(source.read_bytes()).hexdigest() in {r['sha256'] for r in records}, 'Project bytes must equal generated original'
        assert entry['prompt'], 'Full generation prompt required'
    result={'passed':True,'mode':'built-in imagegen','originalPNGs':23,'companions':24,'garments':24,'furnishings':42,'navigationObjects':12,'sceneBackgrounds':3,'roomMaterials':3,'originalPixelHashesPreserved':True,'scope':'Packaged pixel/alpha/identity coverage only. Native visual anatomy and usability require separate review.','assets':records}
    args.out.parent.mkdir(parents=True,exist_ok=True);args.out.write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps({k:v for k,v in result.items() if k!='assets'}))

if __name__=='__main__': main()
