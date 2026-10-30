package com.androidhit.lumadrift.ui;

/** A contextual sheet can scroll on short windows without reducing physical controls. */
public final class CompanionSheetLayout123 {
 public final float top,height,contentHeight,buttonHeight,controlsY,outfitY,returnY;
 public CompanionSheetLayout123(float viewport,float minimumTouch,float descriptionHeight){
  if(!Float.isFinite(viewport)||viewport<=0||!Float.isFinite(minimumTouch)||minimumTouch<=0||!Float.isFinite(descriptionHeight)||descriptionHeight<0)throw new IllegalArgumentException("Finite positive geometry required");
  buttonHeight=Math.max(52,minimumTouch);
  float controls=166+descriptionHeight+30;
  height=Math.max(500,controls+3*buttonHeight+38);
  top=Math.max(12,viewport-height);controlsY=top+controls;
  outfitY=controlsY+buttonHeight+10;returnY=outfitY+buttonHeight+10;
  contentHeight=Math.max(viewport,top+height);
 }
}
