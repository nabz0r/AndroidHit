#!/usr/bin/env python3
"""Read saved native root nodes only; report distinct-action target overlaps for review.

Overlapping furniture targets in the Home world deliberately follow painter order.
Rectangles alone cannot establish painted-pixel/caption occlusion or visual acceptance.
No Android device is queried and no app state or existing evidence is modified.
"""
from pathlib import Path
import argparse,hashlib,json

ROOT=Path(__file__).resolve().parents[1]
def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
def intersect(a,b):
    value=[max(a[0],b[0]),max(a[1],b[1]),min(a[2],b[2]),min(a[3],b[3])]
    return value if value[2]>value[0] and value[3]>value[1] else None
def contains(bounds,x,y):return bounds[0]<=x<bounds[2] and bounds[1]<=y<bounds[3]
def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--capture-dir',required=True,type=Path)
    p.add_argument('--apk-sha256',required=True)
    p.add_argument('--expected-version',required=True)
    p.add_argument('--expected-code',required=True,type=int)
    p.add_argument('--output',required=True,type=Path)
    a=p.parse_args();assert not a.output.exists(),'Never overwrite a prior audit'
    rows=[]
    for name in ['adventure-en','collection-de','atelier-en','home-fr']:
        path=a.capture_dir/(name+'.json');assert path.is_file(),path
        nodes=[n for n in json.loads(path.read_text()) if n['clickable'] and n['visible']]
        overlaps=[];ambiguous=[]
        for i,node in enumerate(nodes):
            bounds=node['bounds'];x=(bounds[0]+bounds[2])/2;y=(bounds[1]+bounds[3])/2
            for j,other in enumerate(nodes):
                if i==j or node['label']==other['label']:continue
                if contains(other['bounds'],x,y):ambiguous.append({'target':node['label'],'centre':[x,y],'also_inside':other['label'],'later_target_wins':j>i})
            for other in nodes[i+1:]:
                rectangle=intersect(bounds,other['bounds'])
                if rectangle and node['label']!=other['label']:
                    overlaps.append({'a':node['label'],'b':other['label'],'intersection_pixels':rectangle,'width':rectangle[2]-rectangle[0],'height':rectangle[3]-rectangle[1]})
        rows.append({'root':name,'source':str(path.resolve().relative_to(ROOT)),'sha256':sha(path),'actions':len(nodes),'overlaps':overlaps,'centres_inside_another_action':ambiguous,'home_world_painter_order_requires_separate_triage':name.startswith('home')})
    result={'status':'OBSERVED_TARGET_GEOMETRY_REQUIRES_VISUAL_TRIAGE','apk_sha256':a.apk_sha256,'version':a.expected_version,'code':a.expected_code,'scope':'Read-only analysis of previously captured actual native accessibility target rectangles. No device, app mutation, inferred acquisition, new screen execution or automatic painted-pixel acceptance. Extended-target overlap is not automatically a defect. Home furniture/companion targets intentionally use painter-order picking.','roots':rows}
    a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(result,indent=2,ensure_ascii=False)+'\n')
    print(json.dumps({'roots':len(rows),'overlaps':sum(len(r['overlaps']) for r in rows),'ambiguous_centres':sum(len(r['centres_inside_another_action']) for r in rows),'output':str(a.output)}))
if __name__=='__main__':main()
