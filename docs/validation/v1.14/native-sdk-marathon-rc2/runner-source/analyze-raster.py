#!/usr/bin/env python3
"""Independently verify SDK crops and recorded pixel counts; never render an expected avatar."""
from pathlib import Path
import argparse,hashlib,json,re
from PIL import Image,ImageChops,ImageStat

ROOT=Path(__file__).resolve().parents[2]

def main():
 p=argparse.ArgumentParser();p.add_argument('--label',required=True);a=p.parse_args();assert re.fullmatch(r'[A-Za-z0-9_-]{1,40}',a.label)
 evidence=ROOT/'docs/validation/v1.14/ui-bugs'/('ui-sdk-'+a.label);shots=ROOT/'docs/screenshots/v1.14/ui-bugs'/('ui-sdk-'+a.label)
 host=json.loads((evidence/'host-report.json').read_text());observations=[];recorded={};verified=[]
 for phase in ['prepare','cold']:
  for entry in json.loads((evidence/phase/'actions.json').read_text()):
   if entry.get('observation')=='real-raster-comparison':recorded[(phase,entry['label'])]=entry
   if entry.get('observation')!='stable-real-raster':continue
   title=entry['title'];coordinates=list(map(int,re.findall(r'\d+',entry['bounds'])));assert len(coordinates)==4
   original=shots/phase/(title+'-original.png');crop=shots/phase/(title+'-analysis-crop.png')
   with Image.open(original) as original_image,Image.open(crop) as crop_image:
    extracted=original_image.convert('RGB').crop(tuple(coordinates));actual=crop_image.convert('RGB')
    assert extracted.size==actual.size and extracted.tobytes()==actual.tobytes(),'Analysis crop differs from exact original SDK pixels: '+title
    observations.append({'phase':phase,'title':title,'bounds':coordinates,'original_sha256':hashlib.sha256(original.read_bytes()).hexdigest(),'crop_sha256':hashlib.sha256(crop.read_bytes()).hexdigest(),'pixel_exact_unscaled_crop':True,'width':actual.width,'height':actual.height})
 def compare(phase,label,before_phase,before,after_phase,after):
  native=recorded[(phase,label)]
  before_file=shots/before_phase/(before+'-analysis-crop.png');after_file=shots/after_phase/(after+'-analysis-crop.png')
  with Image.open(before_file) as aa,Image.open(after_file) as bb:
   aa=aa.convert('RGB');bb=bb.convert('RGB');assert aa.size==bb.size
   difference=ImageChops.difference(aa,bb);red,green,blue=difference.split();maximum=ImageChops.lighter(ImageChops.lighter(red,green),blue)
   histogram=maximum.histogram();changed=sum(histogram[9:]);mean=sum(ImageStat.Stat(difference).mean)/3
   assert changed==native['changedPixels'],'Independent pixel count differs: '+label
   assert abs(mean-native['meanAbsoluteRgb'])<1e-10,'Independent RGB mean differs: '+label
   verified.append({'phase':phase,'label':label,'before_crop':str(before_file.relative_to(ROOT)),'after_crop':str(after_file.relative_to(ROOT)),'changed_pixels':changed,'mean_absolute_rgb':mean,'native_count_matches':True,'expected_change':native['expectedChange'],'native_threshold_exclusive':native['minimumChangedPixelsExclusive']})
 compare('prepare','Closed preview leaves actual equipped art unchanged','prepare','visual-00-starter','prepare','visual-01-closed-preview')
 previous='visual-01-closed-preview'
 for item in ['hat_cupid','dress_witch','shoes_cupid','gloves_witch']:
  purchased='visual-purchased-'+item
  if (shots/'prepare'/(purchased+'-analysis-crop.png')).exists():
   compare('prepare','Purchase without Equip leaves actual worn art unchanged: '+item,'prepare',previous,'prepare',purchased);previous=purchased
  equipped='visual-equipped-'+item;compare('prepare','Equipping visibly changes the art for '+item,'prepare',previous,'prepare',equipped);previous=equipped
 for title,label in [('visual-equipped-lantern-leaf','Earned gear visibly changes worn portrait'),('visual-equipped-miko','Companion identity visibly changes independent of its label'),('visual-equipped-retro','Free visual style changes the actual portrait, excluding text')]:
  compare('prepare',label,'prepare',previous,'prepare',title);previous=title
 compare('prepare','Theme changes the actual blank Home canvas, excluding labels/cards','prepare','visual-theme-home-lumina','prepare','visual-theme-home-lagoon')
 compare('prepare','Theme changes the actual blank gameplay canvas','prepare','visual-theme-play-lumina','prepare','visual-theme-play-lagoon')
 compare('prepare','Changing theme leaves the worn portrait identity unchanged','prepare',previous,'prepare','visual-final-equipped')
 compare('cold','Actual equipped drawing survives a cold process relaunch','prepare','visual-final-equipped','cold','visual-cold-equipped')
 assert len(verified)==len(recorded),'Unverified native raster comparison'
 report={'measurement_verified':True,'product_native_passed':host['passed'],'scope':'Independent Pillow/ImageChops comparison of actual retained PNG pixels; not product acceptance or a renderer-derived golden image','host_report_sha256':hashlib.sha256((evidence/'host-report.json').read_bytes()).hexdigest(),'method':'Raw RGB; maximum channel difference above8; original frame cropped at observed native bounds; no scaling, recolour or compositing','observation_count':len(observations),'comparison_count':len(verified),'observations':observations,'comparisons':verified,'limits':'Only the retained third frame per state is independently re-read. Three-frame stability remains a native observation in the raw actions; it is not independently reconstructed from missing earlier frames. No cross-version pixel equality or human-perception score is claimed.'}
 (evidence/'host-raster-verification.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps({'measurement_verified':True,'product_native_passed':host['passed'],'observations':len(observations),'comparisons':len(verified)}))

if __name__=='__main__':main()
