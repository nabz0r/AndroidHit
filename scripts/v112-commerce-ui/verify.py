#!/usr/bin/env python3
"""Independent integrity checks over retained native bytes; no image modification."""
from pathlib import Path
import argparse,hashlib,json,struct,zlib
root=Path(__file__).resolve().parents[2]
p=argparse.ArgumentParser();p.add_argument('--label',default='rc2-b');a=p.parse_args();assert a.label.replace('-','').replace('_','').isalnum()
out=root/'docs/validation/v1.12/commerce-native'/a.label;shots=root/'docs/screenshots/v1.12/commerce-native'/a.label;r=json.loads((out/'host-report.json').read_text())
assert r['passed'] and r['restored'] and r['lab_unchanged'] and r['prior_test_restored'] and r['installed_test_apk_verified']
assert r['test_package']=='com.androidhit.lumadrift.commercetest' and r['isolated_test_cleanup']=='Success'
assert r['original_prefs']==r['restored_prefs'] and len(r['original_prefs'])==3 and r['native']['checkCount']==45
files=sorted(shots.glob('*.png'));assert len(files)==36;rows=[]
for f in files:
 b=f.read_bytes();sha=hashlib.sha256(b).hexdigest();assert sha==r['screenshots'][f.name] and b[:8]==b'\x89PNG\r\n\x1a\n';offset=8;idat=b'';ended=False
 while offset<len(b):
  size=struct.unpack('>I',b[offset:offset+4])[0];kind=b[offset+4:offset+8];data=b[offset+8:offset+8+size];crc=struct.unpack('>I',b[offset+8+size:offset+12+size])[0];assert zlib.crc32(kind+data)&0xffffffff==crc;offset+=12+size
  if kind==b'IHDR':w,h,depth,color,compression,filtering,interlace=struct.unpack('>IIBBBBB',data);assert (w,h)==(1080,2340) and depth==8 and color in (2,6) and interlace==0
  if kind==b'IDAT':idat+=data
  if kind==b'IEND':ended=True;break
 assert ended and offset==len(b);raw=zlib.decompress(idat);stride=1+w*(3 if color==2 else 4);assert len(raw)==h*stride and all(raw[y*stride]<=4 for y in range(h));rows.append({'name':f.name,'width':w,'height':h,'sha256':sha,'valid_png_crc_and_scanline_stream':True})
record={'passed':True,'scope':'Independent standard-library PNG chunk CRC and decompressed scanline integrity; retained original bytes versus per-run SDK hashes. No image editing or visual preference inference.','original_pref_file_count':3,'byte_exact_prefs_restored':True,'retained_lab_unchanged':True,'ordinary_test_apk_unchanged':True,'installed_isolated_test_apk_verified':True,'test_apk_sha256':r['test_apk_sha256'],'game_apk_sha256':r['game_apk_sha256'],'original_full_frame_count':36,'images':rows}
(out/'integrity.json').write_text(json.dumps(record,indent=2)+'\n');print('PASS: native restoration and36 original PNG hashes, CRC and scanline streams')
