#!/usr/bin/env python3
"""Apply reviewed, provisional attachment estimates without changing extraction or art."""
import argparse, copy, hashlib, json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--source', type=Path, required=True)
    parser.add_argument('--receipt', type=Path, required=True)
    args = parser.parse_args()
    args.receipt.mkdir(parents=True, exist_ok=False)
    backup = args.receipt / 'before'; backup.mkdir()
    records = []
    for proposal in sorted(args.source.glob('*-poses.json')):
        target = ROOT / 'app/src/main/assets/art/v118' / proposal.name
        current = json.loads(target.read_text())
        proposed = json.loads(proposal.read_text())
        assert current['id'] == proposed['id'] and current['form'] == proposed['form']
        before = sha(target)
        (backup / target.name).write_bytes(target.read_bytes())
        original = ROOT / current['original']
        image = ROOT / 'app/src/main/assets' / current['image']
        raster_hashes = [sha(original), sha(image)]
        retained = copy.deepcopy(current)
        assert len(current['frames']) == len(proposed['frames']) == 20
        for old, new in zip(current['frames'], proposed['frames']):
            assert old['poseIndex'] == new['poseIndex']
            old['attachments'] = new['attachments']
            old['garmentNeckY'] = new['garmentNeckY']
            old['anchorsMeasured'] = False
            old['apparelQualified'] = False
            old['anchorProvenance'] = 'Local palette and visually seeded pose estimates; not segmented limbs or fitting approval'
        current['anchorsMeasured'] = False
        current['apparelQualified'] = False
        current['rigProposal'] = proposed['rigProposal']
        current['rigBaselineSha256'] = before
        target.write_text(json.dumps(current, ensure_ascii=False, indent=2) + '\n')
        assert raster_hashes == [sha(original), sha(image)]
        for old, new in zip(retained['frames'], current['frames']):
            for key in ('sourceRect', 'sourceClip', 'originX', 'originY', 'pixelsPerUnit', 'bodyBounds'):
                assert old.get(key) == new.get(key), (target.name, key)
        records.append({'file':target.name, 'before':before, 'after':sha(target),
                        'originalSha256':raster_hashes[0], 'webpSha256':raster_hashes[1]})
    assert records
    (args.receipt / 'applied.json').write_text(json.dumps({
        'scope':'Provisional pose attachment estimates; no raster or source-extraction changes; no fitting qualification',
        'sheets':records}, indent=2) + '\n')
    print(json.dumps({'sheets':len(records), 'receipt':str(args.receipt)}))

if __name__ == '__main__': main()
