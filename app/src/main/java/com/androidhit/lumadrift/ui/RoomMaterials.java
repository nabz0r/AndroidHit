package com.androidhit.lumadrift.ui;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/** Original painted materials mapped onto the actual editable room planes. */
public final class RoomMaterials {
    private static AssetManager assets;
    private static final Map<String,Bitmap> textures = new HashMap<>();
    private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private static final Matrix matrix = new Matrix();
    private static final float[] source = new float[8];
    private static final float[] LEFT_WALL = {40,155,320,0,320,140,40,295};
    private static final float[] RIGHT_WALL = {320,0,600,155,600,295,320,140};
    private static final float[] FLOOR = {320,140,600,295,320,450,40,295};
    private static final Map<Integer,PorterDuffColorFilter> tints = new HashMap<>();
    private RoomMaterials() { }
    public static void install(AssetManager manager) { assets = manager; }

    public static boolean wall(Canvas canvas, boolean left, boolean bedroom, int tint) {
        return plane(canvas, bedroom ? "wall-bedroom" : "wall-living", tint,
                left ? LEFT_WALL : RIGHT_WALL);
    }

    public static boolean floor(Canvas canvas, int tint) {
        return plane(canvas,"floor-parquet",tint,FLOOR);
    }

    private static boolean plane(Canvas canvas,String name,int tint,float[] corners) {
        if (assets == null || canvas == null) return false;
        Bitmap bitmap = textures.get(name);
        if (!textures.containsKey(name)) {
            try (InputStream input = assets.open("art/v117/"+name+".png")) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false; options.inSampleSize = 2;
                bitmap = BitmapFactory.decodeStream(input,null,options);
            } catch (java.io.IOException failure) { bitmap = null; }
            textures.put(name,bitmap);
        }
        if (bitmap == null) return false;
        source[0]=source[1]=source[3]=source[6]=0;
        source[2]=source[4]=bitmap.getWidth(); source[5]=source[7]=bitmap.getHeight();
        matrix.reset();
        if (!matrix.setPolyToPoly(source,0,corners,0,4)) return false;
        PorterDuffColorFilter filter=tints.get(tint);
        if(filter==null){filter=new PorterDuffColorFilter(tint,PorterDuff.Mode.MULTIPLY);tints.put(tint,filter);}
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap,matrix,paint);
        paint.setColorFilter(null);
        return true;
    }
}
