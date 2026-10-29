#!/usr/bin/env python3
"""Read-only alpha analysis generates source rectangles and small rendering exclusions, never pixels."""
from pathlib import Path
from PIL import Image
import numpy as np,json,hashlib
ROOT=Path(__file__).resolve().parents[4];ART=ROOT/'app/src/main/assets/art/v117'
def labelled(mask):
 parent=[];runs=[];previous=[]
 def find(i):
  while parent[i]!=i:parent[i]=parent[parent[i]];i=parent[i]
  return i
 def union(a,b):
  a=find(a);b=find(b)
  if a!=b:parent[b]=a
 for y,row in enumerate(mask):
  edges=np.flatnonzero(np.diff(np.r_[False,row,False].astype(np.int8)));current=[];j=0
  for l,r in zip(edges[::2],edges[1::2]):
   l=int(l);r=int(r);i=len(parent);parent.append(i);runs.append((l,y,r,i));current.append((l,r,i))
   while j<len(previous) and previous[j][1]<l:j+=1
   k=j
   while k<len(previous) and previous[k][0]<=r:union(i,previous[k][2]);k+=1
  previous=current
 labels=np.zeros(mask.shape,np.int32);cs={}
 for l,y,r,i in runs:
  q=find(i)+1;labels[y,l:r]=q
  if q not in cs:cs[q]=[l,y,r,y+1,r-l]
  else:
   p=cs[q];p[0]=min(p[0],l);p[1]=min(p[1],y);p[2]=max(p[2],r);p[3]=max(p[3],y+1);p[4]+=r-l
 return labels,cs

def dilation(mask):
 pad=np.pad(mask,1);out=mask.copy()
 for dy in range(3):
  for dx in range(3):out|=pad[dy:dy+mask.shape[0],dx:dx+mask.shape[1]]
 return out

def rectangles(foreign,protected,offset):
 result=[]
 def split(l,t,r,b):
  yy,xx=np.nonzero(foreign[t:b,l:r])
  if not len(xx):return
  l0=l+int(xx.min());r0=l+int(xx.max())+1;t0=t+int(yy.min());b0=t+int(yy.max())+1
  if not protected[t0:b0,l0:r0].any():
   result.append([l0+offset[0],t0+offset[1],r0+offset[0],b0+offset[1]]);return
  if r0-l0>=b0-t0:
   mid=(l0+r0)//2;split(l0,t0,mid,b0);split(mid,t0,r0,b0)
  else:
   mid=(t0+b0)//2;split(l0,t0,r0,mid);split(l0,mid,r0,b0)
 split(0,0,foreign.shape[1],foreign.shape[0]);return result

allmeta={};java=[]
for path in sorted(ART.glob('*.png')):
 if path.name.startswith('world-'):continue
 alpha=np.asarray(Image.open(path).getchannel('A'));core=alpha>128
 if path.name=='actions-1.png':core[529,1090:]=False # Two authored bases touch here; split only their antialias seam.
 labels,cs=labelled(core);large={q:c for q,c in cs.items() if c[4]>1000};groups=[[] for _ in range(6)]
 for q,c in large.items():
  col=min(2,int(((c[0]+c[2])*.5)/512));row=min(1,int(((c[1]+c[3])*.5)/512));groups[row*3+col].append(q)
 assert all(groups),(path.name,groups)
 regions=[];cuts=[];audit=[]
 for i,ids in enumerate(groups):
  boxes=[cs[q] for q in ids];l=max(0,min(c[0] for c in boxes)-2);t=max(0,min(c[1] for c in boxes)-2);r=min(alpha.shape[1],max(c[2] for c in boxes)+2);b=min(alpha.shape[0],max(c[3] for c in boxes)+2)
  own=np.isin(labels,ids);foreign=np.isin(labels,[q for q in large if q not in ids]);other=dilation(foreign)
  local=other[t:b,l:r]&(alpha[t:b,l:r]>8);protected=(alpha[t:b,l:r]>8)&~local
  exclusions=rectangles(local,protected,(l,t));regions.append([l,t,r,b]);cuts.append(exclusions)
  excluded=np.zeros_like(alpha,dtype=bool)
  for x,y,xx,yy in exclusions:excluded[y:yy,x:xx]=True
  own_lost=int(np.count_nonzero(own&excluded));assert own_lost==0,(path.name,i,own_lost)
  remaining=int(np.count_nonzero(foreign[t:b,l:r]&~excluded[t:b,l:r]));assert remaining==0,(path.name,i,remaining)
  audit.append({'index':i,'source':[l,t,r,b],'excluded_rectangles':exclusions,'primary_opaque_pixels_retained':int(np.count_nonzero(own)),'primary_opaque_pixels_lost':own_lost,'foreign_primary_pixels_remaining':remaining})
 allmeta[path.name]={'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'width':alpha.shape[1],'height':alpha.shape[0],'regions':audit}
 def ints(a):return '{'+','.join(str(x) for x in a)+'}'
 java.append('        case "'+path.name+'": regions=new int[][]{'+','.join(ints(r) for r in regions)+'}; exclusions=new int[][][]{'+','.join('{'+','.join(ints(r) for r in cuts0)+'}' for cuts0 in cuts)+'};break;')
 print(path.name,'regions',regions,'exclusions',[len(x) for x in cuts])
source='''package com.androidhit.lumadrift.ui;

/** Measured original PNG source windows; source pixels and ownership IDs are never rewritten. */
public final class ArtRegions117 {
    private ArtRegions117() { }
    public static void register(ArtLibrary library,String path,String... keys) {
        if(library==null||path==null)throw new IllegalArgumentException("Art registration required");
        String file=path.substring(path.lastIndexOf('/')+1);int[][] regions;int[][][] exclusions;
        switch(file) {
'''+ '\n'.join(java)+'''
        default:throw new IllegalArgumentException("Unknown measured original atlas: "+path);
        }
        library.registerRegions(path,keys,regions,exclusions);
    }
}
'''
(ROOT/'app/src/main/java/com/androidhit/lumadrift/ui/ArtRegions117.java').write_text(source)
(Path(__file__).parent/'source-regions.json').write_text(json.dumps({'scope':'Original alpha read only; source rectangles and short rendering exclusions. No PNG pixel edits.','primary_threshold':128,'antialias_threshold':8,'atlases':allmeta},indent=2)+'\n')
# Export exact saved identities and source rectangles for the independent SDK observer.
import re
characters=(ROOT/'app/src/main/java/com/androidhit/lumadrift/ui/CharacterSprites.java').read_text()
def table(name):
 block=characters.split('private static final String[][] '+name+' = {',1)[1].split('};',1)[0]
 return [re.findall(r'"([^"]+)"',row) for row in re.findall(r'\{([^{}]+)\}',block)]
ids={}
for index,row in enumerate(table('CHARACTER_IDS')):ids['companions-'+str(index)+'.png']=['character/'+key for key in row]
for name,row in zip(['hats','dresses','shoes','gloves'],table('COSMETIC_IDS')):ids[name+'.png']=['cosmetic/'+key for key in row]
home=re.findall(r'new Item\("([^"]+)"',(ROOT/'app/src/main/java/com/androidhit/lumadrift/data/HomeCatalog.java').read_text())
for index in range(7):ids['furniture-'+str(index)+'.png']=['furniture/'+key for key in home[index*6:index*6+6]]
ids['actions-0.png']=['action/'+key for key in ['portal','book','chest','capsule','house','wardrobe']]
ids['actions-1.png']=['action/'+key for key in ['badge','mirror','mannequin','blueprint','map','settings']]
manifest=[]
for name,atlas in allmeta.items():
 for entry,key in zip(atlas['regions'],ids[name]):
  manifest.append(dict(key=key,path='art/v117/'+name,source_png_sha256=atlas['sha256'],source_width=atlas['width'],source_height=atlas['height'],**entry))
assert len(manifest)==102 and len({v['key'] for v in manifest})==102
(Path(__file__).parent/'render-regions.json').write_text(json.dumps({'version':'v117_source_regions_1','png_pixels_changed':False,'scope':'Exact source rectangles and render exclusions. Independent SDK may decode regions and verify all original PNG hashes.','metadata_java_sha256':hashlib.sha256(source.encode()).hexdigest(),'sprites':manifest},indent=2)+'\n')
