package com.androidhit.lumadrift.ui;

/** Independent presentation camera. Pan/zoom interrupt follow; no game progress is represented here. */
public final class JourneyCamera122 {
    public static final float DEFAULT_ZOOM=.90f, MAX_ZOOM=1.65f;
    private float width=390,height=500,zoom=DEFAULT_ZOOM,centerX=JourneyWorld122.WIDTH/2,centerY=JourneyWorld122.HEIGHT/2;
    private boolean following=true;
    public static final class Frame {
        public final float width,height,zoom,centerX,centerY,left,top,right,bottom;
        public final boolean following;
        private Frame(float width,float height,float zoom,float cx,float cy,boolean following){
            this.width=width;this.height=height;this.zoom=zoom;centerX=cx;centerY=cy;this.following=following;
            left=cx-width/(2*zoom);top=cy-height/(2*zoom);right=left+width/zoom;bottom=top+height/zoom;
        }
        public JourneyWorld122.Rect bounds(){return new JourneyWorld122.Rect(left,top,right,bottom);}
        public JourneyWorld122.Point worldToScreen(float x,float y){return new JourneyWorld122.Point((x-left)*zoom,(y-top)*zoom);}
        public JourneyWorld122.Point screenToWorld(float x,float y){return new JourneyWorld122.Point(left+x/zoom,top+y/zoom);}
    }
    public void configure(float viewWidth,float viewHeight){
        if(!JourneyWorld122.finite(viewWidth)||!JourneyWorld122.finite(viewHeight)||viewWidth<=0||viewHeight<=0)throw new IllegalArgumentException("Positive finite viewport required");
        width=viewWidth;height=viewHeight;zoom=JourneyWorld122.clamp(zoom,minZoom(),maxZoom());clampCenter();
    }
    public Frame frame(){return new Frame(width,height,zoom,centerX,centerY,following);}
    public boolean isFollowing(){return following;}
    public float minZoom(){return Math.max(width/JourneyWorld122.WIDTH,height/JourneyWorld122.HEIGHT);}
    public float maxZoom(){return Math.max(MAX_ZOOM,minZoom());}
    /** Finger displacement in logical viewport units, not world units. */
    public void panBy(float dx,float dy){
        requireFinite(dx,dy);following=false;centerX-=dx/zoom;centerY-=dy/zoom;clampCenter();
    }
    /** Explicit camera selection, including minimap taps. Does not move the avatar. */
    public void centerOn(float worldX,float worldY){requireFinite(worldX,worldY);following=false;centerX=worldX;centerY=worldY;clampCenter();}
    /** Remains interrupted until an explicit resumeFollow call. */
    public void follow(float worldX,float worldY){requireFinite(worldX,worldY);if(following){centerX=worldX;centerY=worldY;clampCenter();}}
    public void resumeFollow(float worldX,float worldY){following=true;follow(worldX,worldY);}
    /** Keeps the touched world point under the same screen point unless a world edge clamps it. */
    public void setZoomAt(float requested,float screenX,float screenY){
        requireFinite(requested,screenX);requireFinite(screenY,screenY);
        Frame old=frame();JourneyWorld122.Point anchor=old.screenToWorld(screenX,screenY);
        zoom=JourneyWorld122.clamp(requested,minZoom(),maxZoom());following=false;
        centerX=anchor.x+(width*.5f-screenX)/zoom;centerY=anchor.y+(height*.5f-screenY)/zoom;clampCenter();
    }
    public JourneyWorld122.Point worldToScreen(float x,float y){requireFinite(x,y);return frame().worldToScreen(x,y);}
    public JourneyWorld122.Point screenToWorld(float x,float y){requireFinite(x,y);return frame().screenToWorld(x,y);}
    private void clampCenter(){float halfW=width/(2*zoom),halfH=height/(2*zoom);centerX=JourneyWorld122.clamp(centerX,halfW,JourneyWorld122.WIDTH-halfW);centerY=JourneyWorld122.clamp(centerY,halfH,JourneyWorld122.HEIGHT-halfH);}
    private static void requireFinite(float a,float b){if(!JourneyWorld122.finite(a)||!JourneyWorld122.finite(b))throw new IllegalArgumentException("Finite camera values required");}
}
