package com.androidhit.lumadrift.qa.v114;

import android.graphics.Bitmap;
import android.graphics.Rect;
import org.json.JSONObject;

/** Compares real, unscaled SDK bitmap crops. It knows no product renderer or catalog. */
final class RasterObservation {
    static final int CHANNEL_TOLERANCE=8;
    final Bitmap pixels;
    final Rect bounds;
    final long noisePixels;
    RasterObservation(Bitmap pixels,Rect bounds,long noisePixels){this.pixels=pixels;this.bounds=new Rect(bounds);this.noisePixels=noisePixels;}
    static JSONObject difference(Bitmap a,Bitmap b)throws Exception{
        if(a.getWidth()!=b.getWidth()||a.getHeight()!=b.getHeight())throw new AssertionError("Unscaled crop dimensions changed");
        int width=a.getWidth(),height=a.getHeight();int[] aa=new int[width],bb=new int[width];long changed=0,total=(long)width*height,absolute=0;
        for(int y=0;y<height;y++){a.getPixels(aa,0,width,0,y,width,1);b.getPixels(bb,0,width,0,y,width,1);for(int x=0;x<width;x++){
            int maximum=0;for(int shift=0;shift<=16;shift+=8){int delta=Math.abs(((aa[x]>>shift)&255)-((bb[x]>>shift)&255));absolute+=delta;maximum=Math.max(maximum,delta);}
            if(maximum>CHANNEL_TOLERANCE)changed++;
        }}
        return new JSONObject().put("width",width).put("height",height).put("pixels",total).put("changedPixels",changed).put("changedRatio",changed/(double)total).put("meanAbsoluteRgb",absolute/(3.0*total)).put("channelTolerance",CHANNEL_TOLERANCE);
    }
    void recycle(){pixels.recycle();}
}
