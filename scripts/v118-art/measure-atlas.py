#!/usr/bin/env python3
"""Measure generated twenty-pose originals and encode pixel-exact lossless WebP.
No pixels are painted, erased, recolored, resized, alpha-thresholded or cleaned.
Adaptive alpha thresholds are used ONLY to locate separate body components in source metadata.
Automatic anatomical estimates remain explicitly unqualified until rendered QA.
"""
from __future__ import annotations
import argparse, hashlib, importlib.util, json, shutil, statistics, subprocess
from pathlib import Path
import numpy as np
from PIL import Image

POSES = ['front','back','left','right','walk_front','walk_back','walk_left','walk_right',
         'jump_up','land','hug','crouch','happy','sad','surprise','think',
         'calm','ready','guard','proud']
LEVELS = [1,3,6,10]

def components(alpha):
    h,w=alpha.shape; mask=(alpha>=128).ravel(); result=[]
    for seed in np.flatnonzero(mask):
        if not mask[seed]: continue
        todo=[int(seed)];mask[seed]=False;count=0;left=w;top=h;right=bottom=0
        while todo:
            i=todo.pop(); y,x=divmod(i,w);count+=1
            left=min(left,x);right=max(right,x);top=min(top,y);bottom=max(bottom,y)
            if x and mask[i-1]:mask[i-1]=False;todo.append(i-1)
            if x<w-1 and mask[i+1]:mask[i+1]=False;todo.append(i+1)
            if y and mask[i-w]:mask[i-w]=False;todo.append(i-w)
            if y<h-1 and mask[i+w]:mask[i+w]=False;todo.append(i+w)
        if count>=max(600,w*h//2000): result.append({'area':count,'bounds':[left,top,right+1,bottom+1]})
    result.sort(key=lambda c:c['area'],reverse=True)
    if len(result)<20: raise ValueError(f'Only {len(result)} separated bodies; generation must be reviewed')
    picked=sorted(result[:20],key=lambda c:(c['bounds'][1]+c['bounds'][3])/2)
    ordered=[]
    for row in range(5): ordered.extend(sorted(picked[row*4:row*4+4],key=lambda c:c['bounds'][0]))
    return ordered,result[20:]

def region_for(bounds,others,w,h):
    l,t,r,b=bounds; out=[max(0,l-8),max(0,t-8),min(w,r+8),min(h,b+8)]
    for other in others:
        if other is bounds:continue
        ol,ot,orr,ob=other
        if max(l,ol)<min(r,orr):
            if ob<=t:out[1]=max(out[1],(ob+t)//2)
            if ot>=b:out[3]=min(out[3],(b+ot)//2)
        if max(t,ot)<min(b,ob):
            if orr<=l:out[0]=max(out[0],(orr+l)//2)
            if ol>=r:out[2]=min(out[2],(r+ol)//2)
    return out

def anchor(name,x,y,width,height,layer,rotation=0,visible=True):
    return {'name':name,'x':round(float(x),4),'y':round(float(y),4),
            'width':round(float(width),4),'height':round(float(height),4),
            'rotation':rotation,'layer':layer,'visible':visible}

def provisional_anchors(index,bounds,head_x,ground,standing):
    l,t,r,b=bounds; bh=b-t; width=r-l
    head_width=min(width,standing*.76); neck=t+bh*.51
    torso_y=neck+standing*.18; torso_x=head_x
    left=[head_x-standing*.20,t+bh*.73,35];right=[head_x+standing*.22,t+bh*.73,-35]
    feet=[[head_x-standing*.11,ground],[head_x+standing*.14,ground]]
    left_visible=right_visible=True
    left_layer=right_layer='NEAR_HAND';left_shoe=right_shoe='SHOE'
    if index in (2,6):right_visible=False;right_layer='FAR_LIMB';right_shoe='FAR_LIMB';left[0]=head_x-standing*.05
    if index in (3,7):left_visible=False;left_layer='FAR_LIMB';left_shoe='FAR_LIMB';right[0]=head_x-standing*.02
    if index in (4,5,6,7):
        left[1]-=standing*.1;right[1]-=standing*.04
        feet[0][1]-=standing*.05;feet[1][1]-=standing*.02
    if index==8:
        left=[head_x-standing*.29,t+bh*.36,25];right=[head_x+standing*.31,t+bh*.25,-25]
        feet=[[head_x-standing*.12,b-standing*.05],[head_x+standing*.13,b]]
    elif index in (9,11):
        torso_y=t+bh*.65;left=[head_x-standing*.12,b-standing*.04,0];right=[head_x+standing*.17,b-standing*.04,0]
        feet=[[head_x-standing*.25,b],[head_x+standing*.26,b]]
    elif index==10:left=[head_x-standing*.28,t+bh*.56,65];right=[head_x+standing*.30,t+bh*.56,-65]
    elif index==12:left=[head_x-standing*.24,t+bh*.57,40];right=[head_x+standing*.27,t+bh*.52,-40]
    elif index==13:left=[head_x-standing*.05,t+bh*.76,0];right=[head_x+standing*.05,t+bh*.76,0]
    elif index==14:left=[head_x-standing*.14,t+bh*.57,0];right=[head_x+standing*.16,t+bh*.57,0]
    elif index==15:right=[head_x-standing*.02,t+bh*.54,15]
    elif index==17:left=[head_x-standing*.17,t+bh*.56,35];right=[head_x+standing*.20,t+bh*.65,-35]
    elif index==18:left=[head_x-standing*.07,t+bh*.54,30];right=[head_x+standing*.11,t+bh*.62,-30]
    elif index==19:left=[head_x-standing*.18,t+bh*.71,20];right=[head_x+standing*.22,t+bh*.71,-20]
    ppu=standing/3.15;gear='FAR_LIMB' if index in (1,5) else 'FRONT_GEAR'
    return [anchor('HAT',head_x,t+bh*.23,head_width*.90,standing*.43,'HAT'),
            anchor('DRESS',torso_x,torso_y,head_width*.75,standing*.40,'FAR_LIMB' if index in (1,5) else 'DRESS'),
            anchor('LEFT_GLOVE',*left[:2],standing*.15,standing*.17,left_layer,left[2],left_visible),
            anchor('RIGHT_GLOVE',*right[:2],standing*.15,standing*.17,right_layer,right[2],right_visible),
            anchor('LEFT_SHOE',feet[0][0],feet[0][1]-standing*.12,standing*.22,standing*.26,left_shoe),
            anchor('RIGHT_SHOE',feet[1][0],feet[1][1]-standing*.12,standing*.22,standing*.26,right_shoe),
            anchor('HEAD_GEAR',head_x+ppu*.98,t+bh*.35,ppu*.52,ppu*.52,gear),
            anchor('CHARM',torso_x+ppu*.90,neck+standing*.22,ppu*.42,ppu*.42,gear),
            anchor('AURA',head_x,ground-standing*.52,standing*.90,standing*.90,'BACK_EFFECT')]

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--source',type=Path,required=True)
    parser.add_argument('--id',required=True);parser.add_argument('--level',type=int,choices=LEVELS,required=True)
    parser.add_argument('--project',type=Path,default=Path(__file__).resolve().parents[2])
    args=parser.parse_args();root=args.project.resolve();stem=f'{args.id}-evo{args.level:02}-poses'
    originals=root/'docs/art/v118/originals';assets=root/'app/src/main/assets/art/v118'
    originals.mkdir(parents=True,exist_ok=True);assets.mkdir(parents=True,exist_ok=True)
    original=originals/f'{stem}.png';output=assets/f'{stem}.webp';metadata=assets/f'{stem}.json'
    if args.source.resolve()!=original.resolve():shutil.copyfile(args.source,original)
    with Image.open(original) as opened: rgba=np.asarray(opened.convert('RGBA'));w,h=opened.size
    alpha=rgba[:,:,3];parts=extra=None;component_threshold=128;failures=[]
    for component_threshold in [128,160,192,208,224,240,248,249,250,251,252]:
        try:
            candidate,remaining=components(np.where(alpha>=component_threshold,255,0).astype('uint8'))
            heights=[c['bounds'][3]-c['bounds'][1] for c in candidate]
            median_height=statistics.median(heights)
            if max(heights)>median_height*1.50 or min(heights)<median_height*.45:
                raise ValueError('Implausible merged or fragmented body bounds')
            for row in range(5):
                centres=[(c['bounds'][0]+c['bounds'][2])*.5 for c in candidate[row*4:row*4+4]]
                if any(centres[i+1]-centres[i]<w*.10 for i in range(3)):
                    raise ValueError('Twenty bodies do not form four separated columns')
            parts,extra=candidate,remaining;break
        except ValueError as exc:failures.append(str(exc))
    if parts is None:raise ValueError('No reliable twenty-pose segmentation: '+str(failures))
    bounds=[item['bounds'] for item in parts]
    standing=float(statistics.median(b[3]-b[1] for b in bounds[:4]));ppu=standing/3.15
    frames=[]
    for index,item in enumerate(parts):
        l,t,r,b=item['bounds'];head=alpha[t:min(b,t+int((b-t)*.48)),l:r]>=128
        ys,xs=np.nonzero(head);head_x=l+float(xs.mean()) if len(xs) else (l+r)*.5
        row=index//4;ground=float(max(cell[3] for cell in bounds[row*4:row*4+4]))
        frames.append({'poseIndex':index,'pose':POSES[index],
            'sourceRect':region_for(item['bounds'],bounds,w,h),'bodyBounds':item['bounds'],
            'bodyArea':item['area'],'originX':round(head_x,4),'originY':ground,'pixelsPerUnit':round(ppu,6),
            'attachments':provisional_anchors(index,item['bounds'],head_x,ground,standing),
            'anchorsMeasured':False,'apparelQualified':False,
            'anchorProvenance':'Automatic pose-aware estimates; require visual inspection and apparel render QA'})
    encoder=shutil.which('cwebp') or '/opt/homebrew/bin/cwebp'
    subprocess.run([encoder,'-quiet','-lossless','-z','9','-exact',str(original),'-o',str(output)],check=True)
    with Image.open(output) as decoded: decoded_rgba=np.asarray(decoded.convert('RGBA'))
    equivalent=bool(np.array_equal(rgba,decoded_rgba))
    if not equivalent:raise ValueError('Lossless WebP pixels differ from generated original')
    hist=np.bincount(alpha.ravel(),minlength=256)
    manifest={'schema':1,'id':args.id,'form':LEVELS.index(args.level),'levelMilestone':args.level,
              'image':f'art/v118/{stem}.webp','width':w,'height':h,'columns':4,'rows':5,
              'standingHeightPixels':standing,'pixelsPerUnit':round(ppu,6),
              'coordinateSystem':'Absolute atlas pixels. originY is ground. localY=(pixelY-originY)/pixelsPerUnit+1.65.',
              'sourceRectsMeasured':True,'componentAlphaThreshold':component_threshold,'anchorsMeasured':False,'apparelQualified':False,
              'original':str(original.relative_to(root)),'originalSha256':hashlib.sha256(original.read_bytes()).hexdigest(),
              'webpSha256':hashlib.sha256(output.read_bytes()).hexdigest(),'pixelEquivalent':equivalent,
              'pngBytes':original.stat().st_size,'webpBytes':output.stat().st_size,
              'alpha':{'zero':int(hist[0]),'low1to15':int(hist[1:16].sum()),'main128to255':int(hist[128:].sum())},
              'unassignedLargeComponents':extra,'frames':frames}
    # Components establish stable body scale and origins. Final rendering
    # regions use measured transparent gutters so detached crowns, tails and
    # translucent veils are retained without duplicating a neighbouring pose.
    bounds_script=Path(__file__).with_name('repair-frame-bounds.py')
    specification=importlib.util.spec_from_file_location('luma_frame_bounds',bounds_script)
    bounds_module=importlib.util.module_from_spec(specification);specification.loader.exec_module(bounds_module)
    manifest,bounds_diagnostics=bounds_module.propose_bounds(manifest,alpha)
    manifest['frameBoundsDiagnostics']={
        'matchedInitialConnectedComponents':bounds_diagnostics['matchedInitialConnectedComponents'],
        'initialConnectedComponentPixelsOutsideAssignedCorridors':bounds_diagnostics['initialConnectedComponentPixelsOutsideAssignedCorridors'],
        'note':'Connected components can include neighbouring crowns through generated fringe. Visual review remains required.'}
    metadata.write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
    print(json.dumps({'image':str(output),'metadata':str(metadata),'poses':len(frames),'pixelEquivalent':equivalent,
                      'size':[w,h],'pngBytes':manifest['pngBytes'],'webpBytes':manifest['webpBytes']}))
if __name__=='__main__':main()
