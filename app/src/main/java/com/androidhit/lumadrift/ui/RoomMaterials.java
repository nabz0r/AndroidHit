package com.androidhit.lumadrift.ui;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Path;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/** Original Pulse materials mapped onto the actual editable room planes. */
public final class RoomMaterials {
    private static AssetManager assets;
    private static final Map<String,Bitmap> textures = new HashMap<>();
    private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private static final Matrix matrix = new Matrix();
    private static final float[] source = new float[8];
    private static final Path clip = new Path();
    private static final float[] LEFT_WALL = {40,155,320,0,320,140,40,295};
    private static final float[] RIGHT_WALL = {320,0,600,155,600,295,320,140};
    private static final float[] FLOOR = {320,140,600,295,320,450,40,295};
    private static final Map<Integer,PorterDuffColorFilter> tints = new HashMap<>();
    private RoomMaterials() { }
    public static void install(AssetManager manager) { assets = manager; }

    public static boolean wall(Canvas canvas, boolean left, boolean bedroom, int tint) {
        return wall(canvas,left,bedroom?"bedroom":"living",tint);
    }

    public static boolean wall(Canvas canvas, boolean left, String room, int tint) {
        return plane(canvas,"kitchen".equals(room)?2:"bedroom".equals(room)?1:0,tint,left?LEFT_WALL:RIGHT_WALL);
    }

    public static boolean floor(Canvas canvas, int tint) {
        return floor(canvas,"living",tint);
    }

    public static boolean floor(Canvas canvas,String room,int tint){return plane(canvas,"garden".equals(room)?5:"kitchen".equals(room)?4:3,tint,FLOOR);}

    private static boolean plane(Canvas canvas,int cell,int tint,float[] corners) {
        if (assets == null || canvas == null) return false;
        String name="pulse-materials";
        Bitmap bitmap = textures.get(name);
        if (!textures.containsKey(name)) {
            try (InputStream input = assets.open("art/v124/materials.webp")) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false; options.inSampleSize = 1;
                bitmap = BitmapFactory.decodeStream(input,null,options);
            } catch (java.io.IOException failure) { bitmap = null; }
            textures.put(name,bitmap);
        }
        if (bitmap == null) return false;
        float l=(cell%3)*bitmap.getWidth()/3f,r=(cell%3+1)*bitmap.getWidth()/3f;
        float t=(cell/3)*bitmap.getHeight()/2f,b=(cell/3+1)*bitmap.getHeight()/2f;
        source[0]=source[6]=l;source[1]=source[3]=t;source[2]=source[4]=r;source[5]=source[7]=b;
        matrix.reset();
        if (!matrix.setPolyToPoly(source,0,corners,0,4)) return false;
        PorterDuffColorFilter filter=tints.get(tint);
        if(filter==null){filter=new PorterDuffColorFilter(tint,PorterDuff.Mode.MULTIPLY);tints.put(tint,filter);}
        paint.setColorFilter(filter);
        // Clip to this physical plane: the other five atlas cells never bleed into the room.
        int save=canvas.save();clip.reset();clip.moveTo(corners[0],corners[1]);
        for(int i=2;i<8;i+=2)clip.lineTo(corners[i],corners[i+1]);clip.close();canvas.clipPath(clip);
        canvas.drawBitmap(bitmap,matrix,paint);canvas.restoreToCount(save);
        paint.setColorFilter(null);
        return true;
    }
}
