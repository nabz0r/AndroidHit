#!/usr/bin/env python3
"""Read original alpha only; emit connected-component metadata, never alter PNG pixels."""
from pathlib import Path
from PIL import Image
import numpy as np,json
ROOT=Path(__file__).resolve().parents[4]
ART=ROOT/'app/src/main/assets/art/v117'
def components(mask):
 parent=[];parts=[];prev=[]
 def find(i):
  while parent[i]!=i:
   parent[i]=parent[parent[i]];i=parent[i]
  return i
 def union(a,b):
  a=find(a);b=find(b)
  if a!=b:parent[b]=a
 for y,row in enumerate(mask):
  edges=np.flatnonzero(np.diff(np.r_[False,row,False].astype(np.int8)));runs=[];j=0
  for l,r in zip(edges[::2],edges[1::2]):
   l=int(l);r=int(r);idx=len(parent);parent.append(idx);parts.append([l,y,r,y+1,r-l]);runs.append((l,r,idx))
   while j<len(prev) and prev[j][1]<l:j+=1
   k=j
   while k<len(prev) and prev[k][0]<=r:
    union(idx,prev[k][2]);k+=1
  prev=runs
 merged={}
 for i,(l,t,r,b,n) in enumerate(parts):
  q=find(i)
  if q not in merged:merged[q]=[l,t,r,b,n]
  else:
   p=merged[q];p[0]=min(p[0],l);p[1]=min(p[1],t);p[2]=max(p[2],r);p[3]=max(p[3],b);p[4]+=n
 return sorted(merged.values(),key=lambda x:x[4],reverse=True)
result={}
for p in sorted(ART.glob('*.png')):
 if p.name.startswith('world-'):continue
 a=np.asarray(Image.open(p).getchannel('A'));cs=components(a>8);result[p.name]={'width':a.shape[1],'height':a.shape[0],'components':[c for c in cs if c[4]>=8]}
 print(p.name,[(c[:4],c[4]) for c in cs[:14]])
(Path(__file__).parent/'alpha-components.json').write_text(json.dumps(result,indent=2)+'\n')
