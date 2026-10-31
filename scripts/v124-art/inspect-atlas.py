#!/usr/bin/env python3
"""Read-only alpha/component measurements. Never edits source or packaged pixels."""
from pathlib import Path
import json,re
from PIL import Image

ROOT=Path(__file__).resolve().parents[2]
manifest=json.loads((ROOT/'docs/art/v1.24/generation-jobs.json').read_text())
def components(mask,w,h):
    parent=[];boxes=[];areas=[]
    def find(n):
        while parent[n]!=n: parent[n]=parent[parent[n]];n=parent[n]
        return n
    prev=[]
    for y in range(h):
        now=[];row=mask[y*w:(y+1)*w]
        for match in re.finditer(b'\x01+',row):
            left,right=match.span();links=[find(p[2]) for p in prev if p[0]<right and p[1]>left]
            if not links:
                n=len(parent);parent.append(n);boxes.append([left,y,right,y+1]);areas.append(right-left)
            else:
                n=min(links)
                for other in set(links):
                    other=find(other)
                    if other==n:continue
                    parent[other]=n;b=boxes[other];a=boxes[n];boxes[n]=[min(a[0],b[0]),min(a[1],b[1]),max(a[2],b[2]),max(a[3],b[3])];areas[n]+=areas[other]
                a=boxes[n];boxes[n]=[min(a[0],left),min(a[1],y),max(a[2],right),max(a[3],y+1)];areas[n]+=right-left
            now.append((left,right,n))
        prev=now
    return [{'bounds':boxes[n],'area':areas[n]} for n in range(len(parent)) if find(n)==n and areas[n]>=40]

reports=[]
for job in manifest['jobs']:
    if not job['transparent']:continue
    im=Image.open(ROOT/job['packaged_path']).convert('RGBA');alpha=im.getchannel('A');w,h=im.size
    count=4 if job['key']=='arrows' else 8 if job['key']=='locks' else 3 if job['key']=='elements' else 6;cols=2 if count==4 else 4 if count==8 else 3;rows=1 if count==3 else 2
    parts=components(alpha.point(lambda p:1 if p>=64 else 0).tobytes(),w,h)
    groups=[[] for _ in range(count)]
    for part in parts:
        l,t,r,b=part['bounds'];idx=min(rows-1,int((t+b)*.5*rows/h))*cols+min(cols-1,int((l+r)*.5*cols/w));groups[idx].append(part)
    regions=[]
    for i,group in enumerate(groups):
        assert group, f'Missing authored item {job["key"]}/{i}'
        boxes=[x['bounds'] for x in group];region=[max(0,min(x[0] for x in boxes)-2),max(0,min(x[1] for x in boxes)-2),min(w,max(x[2] for x in boxes)+2),min(h,max(x[3] for x in boxes)+2)]
        regions.append(region)
    hist=alpha.histogram()
    reports.append({'key':job['key'],'width':w,'height':h,'alpha_min':min(i for i,n in enumerate(hist) if n),'alpha_max':max(i for i,n in enumerate(hist) if n),'transparent_fraction':round(hist[0]/(w*h),5),'regions':regions,'edge_core_components':[p for p in parts if p['area']>1000 and (p['bounds'][0]==0 or p['bounds'][1]==0 or p['bounds'][2]==w or p['bounds'][3]==h)],'components':parts})
out=ROOT/'docs/art/v1.24/alpha-measurements.json';out.write_text(json.dumps({'scope':'Read-only measurements, threshold64 groups; source windows require visual review, no pixels edited.','atlases':reports},indent=2)+'\n')
for r in reports:print(r['key'],r['regions'],'edge cores',len(r['edge_core_components']))
