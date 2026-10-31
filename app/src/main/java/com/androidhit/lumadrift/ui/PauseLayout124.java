package com.androidhit.lumadrift.ui;
/** Pause uses two action rows, retaining physical targets on short displays. */
public final class PauseLayout124 {
 public final float top,height,portraitSize,titleY,bodyY,bodyHeight,costY,buttonHeight;
 public final Rect primary,secondary,tertiary;
 public PauseLayout124(float viewport,float minimumTouch,float minimumText,float bodyHeight,boolean restart){
  if(!Float.isFinite(viewport)||viewport<=0||!Float.isFinite(minimumTouch)||minimumTouch<=0||!Float.isFinite(minimumText)||minimumText<=0||!Float.isFinite(bodyHeight)||bodyHeight<0)throw new IllegalArgumentException("Finite positive pause geometry required");
  buttonHeight=Math.max(60,Math.max(minimumTouch,minimumText*2.8f+12));
  portraitSize=viewport<510?0:66;
  float title=portraitSize==0?34:24+portraitSize*.38f,body=portraitSize==0?58:portraitSize+34;
  float costRow=restart?minimumText*1.5f+15:0;
  boolean singleRow=body+15+costRow+12+buttonHeight*2+12+22>viewport-16&&minimumTouch<=(restart?153:100);
  int rows=singleRow?1:2;
  this.bodyHeight=Math.min(bodyHeight,Math.max(0,viewport-16-body-15-costRow-12-buttonHeight*rows-(rows-1)*12-22));
  float cost=body+this.bodyHeight+15,controls=cost+costRow+12;
  height=controls+buttonHeight*rows+(rows-1)*12+22;top=Math.max(8,(viewport-height)*.5f);
  titleY=top+title;bodyY=top+body;costY=top+cost;
  primary=new Rect(36,top+controls,singleRow?(restart?189:136):354,top+controls+buttonHeight);
  secondary=singleRow?new Rect(restart?201:145,primary.top,restart?354:245,primary.bottom):new Rect(36,primary.bottom+12,restart?354:189,primary.bottom+12+buttonHeight);
  tertiary=restart?null:singleRow?new Rect(254,primary.top,354,primary.bottom):new Rect(201,secondary.top,354,secondary.bottom);
 }
 public static final class Rect {public final float left,top,right,bottom;private Rect(float l,float t,float r,float b){left=l;top=t;right=r;bottom=b;}public float width(){return right-left;}public float height(){return bottom-top;} }
}
