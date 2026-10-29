#!/usr/bin/env python3
"""Hand-inspected Nova level-1 joint boxes; no sprite pixel mutation.
Clothing anchors are manual visual measurements of the twenty original drawings.
Gear/aura positions are explicit art-direction placements around measured head/torso.
This does not qualify wardrobe fit; that requires native rendered inspection.
"""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[2]
# pose: hat LTRB, torso/dress LTRB, left palm XYangle, right palm XYangle,
#       left foot X,bottom, right foot X,bottom. LEFT/RIGHT refer to screen-side source halves.
DATA=[
([68,8,223,139],[102,146,204,232],[94,205,0],[211,201,0],[121,274],[183,274]),
([351,8,507,132],[385,145,481,232],[370,207,0],[472,206,0],[394,273],[447,273]),
([613,8,754,138],[623,146,690,233],[655,204,0],[638,192,0],[638,274],[679,273]),
([864,8,1008,137],[923,147,990,231],[982,186,0],[944,202,0],[940,274],[980,274]),
([69,281,225,414],[104,416,206,502],[103,431,15],[208,450,-20],[153,552],[198,522]),
([347,280,509,412],[385,413,483,501],[365,467,15],[473,453,-20],[423,552],[466,518]),
([600,281,758,413],[626,416,729,492],[603,433,80],[730,453,-35],[614,551],[727,548]),
([873,281,1020,414],[924,414,1020,494],[927,463,35],[1042,439,-80],[944,551],[1033,547]),
([64,565,222,688],[90,676,208,752],[65,666,110],[233,633,-125],[110,787],[154,812]),
([335,575,506,706],[373,706,478,781],[414,798,0],[495,709,-75],[367,804],[481,804]),
([605,558,769,692],[636,697,741,780],[608,701,65],[764,700,-65],[644,819],[724,819]),
([887,576,1051,710],[924,710,1030,781],[971,798,0],[1002,798,0],[916,805],[1047,805]),
([64,817,226,947],[98,948,203,1036],[83,956,80],[216,941,-80],[119,1092],[185,1060]),
([349,824,509,958],[382,958,481,1045],[415,1036,0],[438,1036,0],[398,1088],[466,1088]),
([607,822,767,952],[633,961,739,1047],[645,970,160],[719,971,-160],[643,1090],[720,1090]),
([896,822,1053,954],[923,960,1033,1048],[942,1024,0],[990,969,-145],[944,1091],[1020,1091]),
([72,1097,228,1231],[101,1238,204,1326],[94,1309,0],[212,1308,0],[123,1380],[185,1380]),
([351,1097,514,1230],[390,1243,491,1334],[395,1267,75],[497,1282,-40],[368,1378],[498,1368]),
([608,1099,774,1231],[641,1241,746,1335],[688,1265,60],[743,1292,-45],[622,1380],[751,1374]),
([894,1097,1052,1233],[922,1240,1024,1336],[940,1293,25],[1024,1293,-25],[941,1380],[1009,1380]),
]

def box(anchor,l,t,r,b):anchor.update(x=(l+r)*.5,y=(t+b)*.5,width=r-l,height=b-t)
def main():
 path=ROOT/'app/src/main/assets/art/v118/nova-evo01-poses.json';data=json.loads(path.read_text())
 for frame,points in zip(data['frames'],DATA):
  index=frame['poseIndex'];hat,dress,left,right,lf,rf=points;anchors={a['name']:a for a in frame['attachments']}
  box(anchors['HAT'],*hat);box(anchors['DRESS'],*dress)
  for name,point in [('LEFT_GLOVE',left),('RIGHT_GLOVE',right)]:
   a=anchors[name];glove_size=(53,58) if index==10 else (48,53) if index==8 else (43,48) if index==12 else (37,43)
   a.update(x=point[0],y=point[1],width=glove_size[0],height=glove_size[1],rotation=point[2],visible=True)
  for name,point in [('LEFT_SHOE',lf),('RIGHT_SHOE',rf)]:
   a=anchors[name];a.update(x=point[0],y=point[1]-31,width=57,height=66,rotation=0,visible=True)
  if index in (2,6):
   anchors['RIGHT_GLOVE']['layer']='FAR_LIMB';anchors['RIGHT_SHOE']['layer']='FAR_LIMB'
   if index==2:anchors['RIGHT_GLOVE']['visible']=False
  if index in (3,7):
   anchors['LEFT_GLOVE']['layer']='FAR_LIMB';anchors['LEFT_SHOE']['layer']='FAR_LIMB'
   if index==3:anchors['LEFT_GLOVE']['visible']=False
  if index in (1,5):anchors['DRESS']['layer']='FAR_LIMB'
  hx=(hat[0]+hat[2])*.5;hy=(hat[1]+hat[3])*.5;tx=(dress[0]+dress[2])*.5;ty=(dress[1]+dress[3])*.5
  anchors['HEAD_GEAR'].update(x=hat[2]+8,y=hy,width=43,height=43)
  anchors['CHARM'].update(x=dress[2]+10,y=ty+19,width=35,height=35)
  anchors['AURA'].update(x=hx,y=(hat[1]+max(lf[1],rf[1]))*.5,width=246,height=246)
  for anchor in anchors.values():
   anchor['measurement']='manual visual source joint/box' if anchor['name'] not in ('HEAD_GEAR','CHARM','AURA') else 'authored placement around manually measured head and torso'
  frame['anchorsMeasured']=True;frame['apparelQualified']=False
  frame['anchorProvenance']='Nova original20-pose visual measurement; gear placements authored around head/torso; wardrobe fit pending native render QA'
 data['anchorsMeasured']=True;data['apparelQualified']=False
 data['anchorReview']='Manual visual annotations for Nova level1 only. Source rects and encoded pixels unchanged. Native wardrobe rendering still required.'
 path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
 print('Nova base20manual joint placements written; apparelQualified remains false.')
if __name__=='__main__':main()
