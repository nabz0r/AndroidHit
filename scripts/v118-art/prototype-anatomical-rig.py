#!/usr/bin/env python3
"""Read-only pose landmark proposals. Never changes a source raster or runtime JSON.

Colour evidence is local to an identity/form's authored neutral hands and boots.
These are estimates for visual review, not measured/qualified anatomy.
"""
from pathlib import Path
import argparse, copy, importlib.util, json, math, sys
import numpy as np
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'build/v118-art-analysis-deps'))
from scipy import ndimage
from scipy.spatial import ConvexHull

HANDS={
 0:[(-.23,.72),(.23,.72)],1:[(-.23,.72),(.23,.72)],
 2:[(-.01,.73),(.07,.73)],3:[(-.07,.73),(.01,.73)],
 4:[(-.15,.61),(.22,.71)],5:[(-.17,.64),(.20,.69)],
 6:[(-.22,.63),(.13,.72)],7:[(-.13,.72),(.22,.63)],
 8:[(-.29,.33),(.30,.28)],9:[(-.12,.93),(.20,.83)],
 10:[(-.31,.59),(.31,.59)],11:[(-.15,.93),(.15,.93)],
 12:[(-.23,.56),(.23,.56)],13:[(-.07,.78),(.07,.78)],
 14:[(-.17,.62),(.17,.62)],15:[(-.22,.74),(.01,.60)],
 16:[(-.23,.73),(.23,.73)],17:[(-.18,.61),(.22,.69)],
 18:[(-.09,.60),(.15,.65)],19:[(-.21,.74),(.21,.74)]}
FEET={i:[(-.115,.93),(.115,.93)]for i in range(20)}
FEET.update({2:[(-.13,.93),(.06,.93)],3:[(-.06,.93),(.13,.93)],
 4:[(-.04,.93),(.11,.84)],5:[(-.08,.93),(.11,.84)],
 6:[(-.27,.88),(.11,.92)],7:[(-.11,.92),(.27,.88)],
 8:[(-.12,.86),(.10,.93)],9:[(-.23,.91),(.23,.91)],
 11:[(-.23,.91),(.23,.91)],17:[(-.25,.92),(.24,.91)],
 18:[(-.25,.92),(.24,.91)]})

# Neutral colour samples must come from the limb, not a neighbouring cape or
# shell. These measured visual proposals only seed the local search; all
# resulting anchors remain provisional and are reviewed as rendered items.
NEUTRAL_HANDS={
 'tula':[(-.14,.62),(.18,.62)],
 'lol_soraka':[(-.20,.75),(.19,.60)],
 'lol_teemo':[(-.25,.83),(.25,.83)],
 'lol_vi':[(-.28,.69),(.28,.69)],
}
POSE_HANDS={
 'tula':{0:[(-.14,.62),(.18,.62)],2:[(-.03,.62),(.08,.62)],3:[(-.08,.62),(.03,.62)],
         4:[(-.21,.61),(.20,.61)],8:[(-.28,.42),(.28,.36)],12:[(-.25,.50),(.25,.50)],
         14:[(-.01,.52),(.25,.52)],16:[(-.09,.73),(.12,.73)],19:[(-.13,.64),(.17,.64)]},
 'lol_teemo':{8:[(-.31,.43),(.30,.40)],12:[(-.31,.42),(.31,.42)]},
 'lol_soraka':{0:[(-.20,.75),(.19,.60)],4:[(-.18,.58),(.18,.60)],
               8:[(-.30,.46),(.29,.34)],10:[(-.31,.54),(.30,.54)],
               12:[(-.27,.46),(.24,.38)],14:[(-.12,.56),(.12,.56)],
               16:[(-.20,.75),(.20,.61)]},
 'jelli':{12:[(-.27,.37),(.27,.47)],14:[(-.08,.49),(.08,.49)],
          15:[(-.05,.63),(.01,.52)],16:[(-.04,.75),(.04,.75)],
          17:[(-.28,.58),(.25,.66)],19:[(-.09,.68),(.09,.68)]},
}

def palette(rgba,centre,radius,skin=False):
 x,y=centre;h,w=rgba.shape[:2];l=max(0,int(x-radius));r=min(w,int(x+radius+1));t=max(0,int(y-radius));b=min(h,int(y+radius+1))
 p=rgba[t:b,l:r];rgb=p[:,:,:3][p[:,:,3]>=240].astype(float)
 if not len(rgb):return np.empty((0,3))
 if skin:
  red,green,blue=rgb.T
  warm=(red-green>22)&(red-green<70)&(green-blue>10)&(green-blue<65)&(blue>105)
  violet=(red-green>22)&(red-green<110)&(blue-green>-10)&(np.abs(red-blue)<55)&(green>95)
  chosen=warm if np.count_nonzero(warm)>=12 else violet
  if np.count_nonzero(chosen)>=12:rgb=rgb[chosen]
 bins=np.floor(rgb/28).astype(int);codes=bins[:,0]*100+bins[:,1]*10+bins[:,2]
 u,c=np.unique(codes,return_counts=True);order=u[np.argsort(-c)[:12]]
 return np.asarray([rgb[codes==n].mean(axis=0)for n in order])

def colour_mask(rgb,reference):
 if not len(reference):return np.zeros(rgb.shape[:2],bool)
 # 43 RGB units is only a candidate-colour gate, never an alpha edit.
 distance=np.min(np.sum((rgb[:,:,None,:].astype(float)-reference[None,None,:,:])**2,axis=3),axis=2)
 return distance<43**2

def propose_part(rgba,reference,point,body_height,kind):
 h,w=rgba.shape[:2];x,y=point;s=body_height
 rx=s*(.13 if kind=='shoe' else .115);ry=s*(.09 if kind=='shoe' else .105)
 l=max(0,int(x-rx));r=min(w,int(x+rx)+1);t=max(0,int(y-ry));b=min(h,int(y+ry)+1)
 patch=rgba[t:b,l:r];mask=colour_mask(patch[:,:,:3],reference)&(patch[:,:,3]>=240)
 mask=ndimage.binary_closing(mask,iterations=2)
 labels,n=ndimage.label(mask);best=None
 for label in range(1,n+1):
  yy,xx=np.nonzero(labels==label);area=len(xx)
  if area<max(9,s*s*.00018) or np.ptp(xx)<2 or np.ptp(yy)<2:continue
  cx=float(xx.mean()+l);cy=float(yy.mean()+t)
  edge_penalty=.045*(int(xx.min()==0)+int(xx.max()==r-l-1)+int(yy.min()==0)+int(yy.max()==b-t-1))
  score=2*math.hypot((cx-x)/s,(cy-y)/s)-min(.035,area/(s*s))+edge_penalty
  if best is None or score<best[0]:best=(score,xx+l,yy+t)
 if best is None:
  width=s*(.16 if kind=='shoe' else .115);height=s*(.18 if kind=='shoe' else .12)
  return {'x':x,'y':y,'width':width,'height':height,'rotation':0,'replacementClip':[[x-width*.5,y-height*.5],[x+width*.5,y-height*.5],[x+width*.5,y+height*.5],[x-width*.5,y+height*.5]],'landmarkEvidence':{'method':'pose-template-only','matchedPixels':0}}
 _,xx,yy=best;cx=float(xx.mean());cy=float(yy.mean());pad=max(2,s*.012)
 # Exact absolute source-space envelope: nativeCutout must not rotate it again.
 left=max(0,float(xx.min())-pad);right=min(w,float(xx.max()+1)+pad);top=max(0,float(yy.min())-pad);bottom=min(h,float(yy.max()+1)+pad)
 angle=0
 if kind=='shoe' and len(xx)>20:
  cov=np.cov(np.stack([xx,yy]));values,vectors=np.linalg.eigh(cov);axis=vectors[:,np.argmax(values)]
  if axis[1]<0:axis=-axis
  angle=float(np.clip(math.degrees(math.atan2(-axis[0],axis[1])),-68,68))
  if abs(angle)<12:angle=0
 theta=math.radians(angle);dx=xx-cx;dy=yy-cy
 localx=dx*math.cos(theta)+dy*math.sin(theta);localy=-dx*math.sin(theta)+dy*math.cos(theta)
 width=float(np.ptp(localx)+2*pad);height=float(np.ptp(localy)+2*pad)
 width=float(np.clip(width,s*(.10 if kind=='shoe'else .08),s*(.27 if kind=='shoe'else .24)))
 height=float(np.clip(height,s*(.12 if kind=='shoe'else .085),s*(.27 if kind=='shoe'else .23)))
 cloud=np.stack([xx,yy],axis=1).astype(float);hull=cloud[ConvexHull(cloud).vertices]
 delta=hull-np.asarray([cx,cy]);norm=np.maximum(1,np.linalg.norm(delta,axis=1));hull+=delta/norm[:,None]*pad
 hull[:,0]=np.clip(hull[:,0],left,right);hull[:,1]=np.clip(hull[:,1],top,bottom)
 return {'x':cx,'y':cy,'width':width,'height':height,'rotation':round(angle,3),'replacementClip':np.round(hull,3).tolist(),'landmarkEvidence':{'method':'local-neutral-palette-and-pose-window','matchedPixels':len(xx)}}

def neck(rgba,frame,reference):
 l,t,r,b=frame['bodyBounds'];s=b-t;cx=(l+r)/2
 lo=max(0,int(cx-s*.25));hi=min(rgba.shape[1],int(cx+s*.25));tt=max(0,int(t+s*.20));bb=min(rgba.shape[0],int(t+s*.85))
 patch=rgba[tt:bb,lo:hi];mask=colour_mask(patch[:,:,:3],reference)&(patch[:,:,3]>=240)
 labels,n=ndimage.label(ndimage.binary_closing(mask,iterations=1));best=None
 for i in range(1,n+1):
  yy,xx=np.nonzero(labels==i)
  if len(xx)<s*s*.012:continue
  score=len(xx)-abs(float(xx.mean()+lo)-cx)*s
  if best is None or score>best[0]:best=(score,float(yy.max()+tt+1))
 compact=frame['poseIndex']in(9,11)
 fraction=(best[1]-t)/s if best else (.73 if compact else .58)
 fraction=float(np.clip(fraction,.66 if compact else .51,.82 if compact else .69))
 return t+s*fraction

def main():
 p=argparse.ArgumentParser();p.add_argument('--ids',nargs='+',default=['nova','miko','tula','jelli','lol_teemo','lol_soraka','lol_yasuo','lol_vi']);p.add_argument('--out',type=Path,default=ROOT/'build/v118-rig-prototype');a=p.parse_args();a.out.mkdir(parents=True,exist_ok=True)
 index=[]
 for identity in a.ids:
  for milestone in (1,3,6,10):
   source=ROOT/f'app/src/main/assets/art/v118/{identity}-evo{milestone:02}-poses.json';d=json.loads(source.read_text());rgba=np.asarray(Image.open(ROOT/d['original']).convert('RGBA'));out=copy.deepcopy(d)
   neutral=d['frames'][0];l,t,r,b=neutral['bodyBounds'];s=b-t;cx=(l+r)/2
   refs={}
   face_y=.56 if identity in ('lol_lulu',)else .50 if identity in ('lol_teemo','jelli','tula')else .45
   face=palette(rgba,(cx,t+face_y*s),s*.055,skin=True)
   neutral_neck=neck(rgba,neutral,face);hand_shift=float(np.clip((neutral_neck-t)/s-.55,-.04,.18))
   for side in range(2):
    refs['shoe',side]=palette(rgba,(cx+FEET[0][side][0]*s,t+FEET[0][side][1]*s),s*.055)
    hx,hy=NEUTRAL_HANDS.get(identity,HANDS[0])[side]
    if identity not in NEUTRAL_HANDS:hy+=hand_shift
    refs['glove',side]=palette(rgba,(cx+hx*s,t+hy*s),s*.045)
   debug=Image.fromarray(rgba).convert('RGB');draw=ImageDraw.Draw(debug)
   for f in out['frames']:
    i=f['poseIndex'];l,t,r,b=f['bodyBounds'];s=b-t;cx=(l+r)/2
    f['garmentNeckY']=round(neck(rgba,f,face),3);f['anchorsMeasured']=False;f['apparelQualified']=False
    for part in f['attachments']:
     name=part['name'];side=0 if name.startswith('LEFT')else 1
     if name.endswith('SHOE')or name.endswith('GLOVE'):
      kind='shoe'if name.endswith('SHOE')else'glove';dx,dy=(FEET if kind=='shoe'else HANDS)[i][side]
      if kind=='glove':
       dy+=hand_shift if i not in(9,11)else 0
       if identity=='lol_teemo' and i in(8,12):dy+=.06
       if i==15 and side==1:dy=(f['garmentNeckY']-t)/s-.025
       dy=min(.96,dy)
       if i in POSE_HANDS.get(identity,{}):dx,dy=POSE_HANDS[identity][i][side]
      found=propose_part(rgba,refs[kind,side],(cx+dx*s,t+dy*s),s,kind)
      part.update(found)
      sl,st,sr,sb=f['sourceRect']
      part['replacementClip']=[[min(sr,max(sl,x)),min(sb,max(st,y))]for x,y in part['replacementClip']]
      if kind=='shoe' and i in(0,1,2,3,13,14,15,16,19):part['rotation']=0
      part['mirrorX']=kind=='shoe' and ((i in(2,6) and side==1)or(i in(3,7) and side==0))
      x,y=part['x'],part['y'];colour='#00aadd'if kind=='shoe'else'#fa2080';draw.ellipse((x-3,y-3,x+3,y+3),fill=colour)
      draw.line(part['replacementClip']+[part['replacementClip'][0]],fill=colour,width=1)
     elif name=='DRESS':
      n=f['garmentNeckY'];height=max(s*.08,min(s*.28,b-s*.10-n));part.update(x=cx,y=n+height*.50,width=min(r-l,s*.38),height=height,rotation=0)
     elif name=='HAT':
      part.update(x=cx,y=t+s*.18,width=min((r-l)*.88,s*.72),height=s*.34)
    draw.line([(l,f['garmentNeckY']),(r,f['garmentNeckY'])],fill='#7755dd',width=1)
   out['anchorsMeasured']=False;out['apparelQualified']=False;out['rigProposal']='local-palette-landmarks-v2; visually seeded neutral and selected pose search windows; unqualified'
   stem=source.stem;(a.out/f'{stem}.json').write_text(json.dumps(out,indent=2)+'\n');debug.save(a.out/f'{stem}-landmarks.png');index.append({'id':identity,'milestone':milestone,'proposal':str(a.out/f'{stem}.json'),'debug':str(a.out/f'{stem}-landmarks.png')})
 (a.out/'index.json').write_text(json.dumps({'runtimeAssetsModified':False,'imagesModified':False,'sheets':index},indent=2)+'\n');print(json.dumps({'sheets':len(index),'output':str(a.out),'runtimeAssetsModified':False}))
if __name__=='__main__':main()
