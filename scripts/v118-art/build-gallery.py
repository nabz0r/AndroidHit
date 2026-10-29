#!/usr/bin/env python3
"""Index existing generated art for the local review gallery; never alter image pixels."""
import json,re
from pathlib import Path
root=Path(__file__).resolve().parents[2]
catalog=(root/'app/src/main/java/com/androidhit/lumadrift/data/Catalog.java').read_text()
names=dict(re.findall(r'add\("character_([^" ]+)", "([^"]+)"', (root/'app/src/main/java/com/androidhit/lumadrift/data/Strings.java').read_text()))
rows=[]
for identity in re.findall(r'new CharacterItem\("([^"]+)"',catalog):
    forms=[]
    for level in [1,3,6,10]:
        source=root/f'app/src/main/assets/art/v118/{identity}-evo{level:02}-poses.json'
        if not source.is_file(): continue
        data=json.loads(source.read_text())
        forms.append({'form':data['form'],'image':data['image'],'frames':[{k:f[k]for k in ['poseIndex','sourceRect','originX','originY','pixelsPerUnit','sourceClip'] if k in f}for f in data['frames']]})
    rows.append({'id':identity,'name':names.get(identity,identity),'forms':forms})
target=root/'docs/art/v118/gallery-manifest.json';target.write_text(json.dumps(rows,ensure_ascii=False,separators=(',',':'))+'\n')
print(json.dumps({'characters':len(rows),'sheets':sum(len(x['forms'])for x in rows),'manifest':str(target)}))
