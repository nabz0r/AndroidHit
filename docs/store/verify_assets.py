#!/usr/bin/env python3
"""Verify vector references, PNG pixels/formats, size contracts and provenance."""
import hashlib
import json
from pathlib import Path
import struct
import xml.etree.ElementTree as ET
import zlib

ROOT=Path(__file__).resolve().parent
SPECS=json.loads((ROOT/'asset-specs.json').read_text())
assets=[]


def png_info(data, sample=None):
    assert data[:8]==b'\x89PNG\r\n\x1a\n'
    chunks=[];offset=8
    while offset<len(data):
        size=struct.unpack('>I',data[offset:offset+4])[0]
        kind=data[offset+4:offset+8];payload=data[offset+8:offset+8+size]
        crc=struct.unpack('>I',data[offset+8+size:offset+12+size])[0]
        assert zlib.crc32(kind+payload)&0xffffffff==crc,(kind,'CRC')
        chunks.append((kind,payload));offset+=size+12
    assert chunks[0][0]==b'IHDR' and chunks[-1][0]==b'IEND'
    w,h,depth,color,_,_,interlace=struct.unpack('>IIBBBBB',chunks[0][1])
    assert depth==8 and color in (2,6) and interlace==0
    channels=4 if color==6 else 3
    raw=zlib.decompress(b''.join(payload for kind,payload in chunks if kind==b'IDAT'))
    stride=w*channels;assert len(raw)==h*(stride+1)
    previous=bytearray(stride);alpha=[];sample_pixel=None
    def paeth(a,b,c):
        p=a+b-c;distances=(abs(p-a),abs(p-b),abs(p-c))
        return (a,b,c)[distances.index(min(distances))]
    for row in range(h):
        start=row*(stride+1);mode=raw[start];current=bytearray(raw[start+1:start+1+stride]);assert mode<=4
        for i in range(stride):
            a=current[i-channels] if i>=channels else 0;b=previous[i];c=previous[i-channels] if i>=channels else 0
            prediction=(0,a,b,(a+b)//2,paeth(a,b,c))[mode]
            current[i]=(current[i]+prediction)&255
        if color==6:alpha.extend(current[3::4])
        if sample and row==sample[1]:sample_pixel=list(current[sample[0]*channels:sample[0]*channels+channels])
        previous=current
    return dict(width=w,height=h,color_type=color,bit_depth=depth,alpha_min=min(alpha) if alpha else None,alpha_max=max(alpha) if alpha else None,nova_sample_pixel=sample_pixel)


nova_centers={'luma-drift-repo-banner':(1210,338),'luma-drift-feature':(740,312),'luma-drift-icon':(250,284),'nova-companion':(320,326),'brand-scene':(737,366)}
for spec in SPECS['assets']:
    for suffix in ('svg','png'):
        path=ROOT/(spec['stem']+'.'+suffix);data=path.read_bytes()
        entry=dict(file=path.name,bytes=len(data),sha256=hashlib.sha256(data).hexdigest(),format=suffix.upper(),role=spec['role'])
        if suffix=='svg':
            tree=ET.fromstring(data);w,h=int(tree.attrib['width']),int(tree.attrib['height'])
            assert tree.attrib['viewBox']==f'0 0 {w} {h}'
            ids=[node.get('id') for node in tree.iter() if node.get('id')]
            assert len(ids)==len(set(ids)),path
            for node in tree.iter():
                for key,value in node.attrib.items():
                    if key.endswith('href'):assert value.startswith('#') and value[1:] in ids,(path,value)
            assert not any(node.tag.endswith('script') or node.tag.endswith('image') or node.tag.endswith('foreignObject') for node in tree.iter())
            if spec['stem']=='brand-scene':assert not any(node.tag.endswith('text') for node in tree.iter())
        else:
            info=png_info(data,nova_centers[spec['stem']]);entry.update(info);w,h=info['width'],info['height']
            pixel=info['nova_sample_pixel']
            assert pixel[0]>180 and pixel[1]>150 and pixel[2]<220,(path,'Missing gold Nova body',pixel)
            if len(pixel)==4:assert pixel[3]==255
            assert info['color_type']==(6 if spec['alpha'] else 2),(path,info)
            if spec['stem']=='nova-companion':assert info['alpha_min']==0 and info['alpha_max']==255
            if spec['stem']=='luma-drift-icon':assert len(data)<=1024*1024
        assert (w,h)==(spec['width'],spec['height']),(path,w,h)
        entry.update(width=w,height=h);assets.append(entry)

preview=ROOT/'luma-drift-icon-64-preview.png';data=preview.read_bytes();info=png_info(data)
assert (info['width'],info['height'])==(64,64)
assets.append(dict(file=preview.name,bytes=len(data),sha256=hashlib.sha256(data).hexdigest(),format='PNG',role='Visual review only; not a store upload',**info))
manifest=dict(schema='androidhit-brand-kit-v2',created='2026-10-27',release='1.5.0-rc1',palette=SPECS['palette'],provenance='Original code-native geometry derived from the Luma Drift Nova, starter equipment, arrows and sealed-wall motifs. Promotional illustration, not an installed-app screenshot or store approval.',source_references=['app/src/main/java/com/androidhit/lumadrift/LumaView.java','app/src/main/java/com/androidhit/lumadrift/data/Catalog.java','docs/screenshots/v1.5/home-en.png','docs/screenshots/v1.5/sharing/sealed-p6-card.png'],generator_inputs_sha256={name:hashlib.sha256((ROOT/name).read_bytes()).hexdigest() for name in ['generate_assets.py','render_assets.swift','verify_assets.py','asset-specs.json']},assets=assets)
(ROOT/'assets.json').write_text(json.dumps(manifest,indent=2)+'\n')
print('PASS 5 SVG sources, local references/no embedded images, text-free scene, PNG CRC/decompression/pixels, RGB/RGBA formats, exact dimensions, transparent Nova, icon size, 64px preview and SHA-256 manifest.')
