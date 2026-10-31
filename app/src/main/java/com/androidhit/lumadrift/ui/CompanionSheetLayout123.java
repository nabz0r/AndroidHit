package com.androidhit.lumadrift.ui;

/** A contextual sheet can scroll on short windows without reducing physical controls. */
public final class CompanionSheetLayout123 {
 public final float top,height,contentHeight,buttonHeight,controlsY,outfitY,returnY,detailY;
 public final float titleY,titleRight,closeTop,closeSize,portraitTop,familyY,costY,usesY,chargeY,chargeHeight;
 public CompanionSheetLayout123(float viewport,float minimumTouch,float descriptionHeight){
  this(viewport,minimumTouch,descriptionHeight,12);
 }
 public CompanionSheetLayout123(float viewport,float minimumTouch,float descriptionHeight,float minimumText){
  if(!Float.isFinite(viewport)||viewport<=0||!Float.isFinite(minimumTouch)||minimumTouch<=0||!Float.isFinite(descriptionHeight)||descriptionHeight<0||!Float.isFinite(minimumText)||minimumText<=0)throw new IllegalArgumentException("Finite positive geometry required");
  closeSize=Math.max(48,minimumTouch);titleRight=366-closeSize-12;
  if(titleRight<=32)throw new IllegalArgumentException("Title and close control cannot fit the fixed canvas");
  buttonHeight=Math.max(Math.max(56,minimumTouch),minimumText*1.5f+12);
  float title=24+Math.max(24,minimumText)*.8f;
  float portrait=Math.max(60,Math.max(17+closeSize+12,title+minimumText*.4f+12));
  float family=portrait+Math.max(29,minimumText),step=Math.max(30,minimumText*1.35f);
  float cost=family+step,uses=cost+step;
  float charge=Math.max(portrait+102+12,uses+minimumText*.4f+12);
  chargeHeight=Math.max(30,minimumText*1.5f);
  float detail=charge+chargeHeight,controls=detail+descriptionHeight+36;
  height=Math.max(540,controls+3*buttonHeight+44);
  if(!Float.isFinite(height))throw new IllegalArgumentException("Text or description overflows finite geometry");
  top=Math.max(12,viewport-height);controlsY=top+controls;
  titleY=top+title;closeTop=top+17;portraitTop=top+portrait;
  familyY=top+family;costY=top+cost;usesY=top+uses;chargeY=top+charge;detailY=top+detail;
  outfitY=controlsY+buttonHeight+10;returnY=outfitY+buttonHeight+10;
  contentHeight=Math.max(viewport,top+height);
 }
}
