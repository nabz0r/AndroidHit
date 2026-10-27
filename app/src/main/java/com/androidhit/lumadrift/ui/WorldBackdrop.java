package com.androidhit.lumadrift.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.androidhit.lumadrift.R;

/** One decoded environment, reused across scenes; no bitmap allocation in draw(). */
public final class WorldBackdrop {
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private Bitmap bitmap;
    private int biome;
    private static final int[] TINTS={0x00212649,0x2c36d6a3,0x32ef9873,0x2c9ba9e7,0x2c53bfea,0x30edb963,0x406a5daa,0x40c36537,0x3fe296c8,0x36c7b8f7};
    public void setBiome(int value){biome=Math.max(0,Math.min(9,value));}
    public WorldBackdrop(Context context) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.aurora_atlas_v1, options);
    }
    public void draw(Canvas canvas, float width, float height, boolean quiet, int background) {
        if (bitmap == null || bitmap.isRecycled()) return;
        float ratio = width / height;
        int cropWidth = Math.min(bitmap.getWidth(), Math.round(bitmap.getHeight() * ratio));
        int cropHeight = Math.min(bitmap.getHeight(), Math.round(bitmap.getWidth() / ratio));
        int left = (bitmap.getWidth() - cropWidth) / 2;
        int top = bitmap.getHeight() - cropHeight;
        source.set(left, top, left + cropWidth, top + cropHeight);
        destination.set(0, 0, width, height);
        paint.setAlpha(quiet ? 95 : 230);
        canvas.drawBitmap(bitmap, source, destination, paint);
        paint.setAlpha(255);paint.setColor(TINTS[biome]);canvas.drawRect(destination,paint);
        paint.setColor((background & 0xffffff) | ((quiet ? 140 : 30) << 24));
        canvas.drawRect(destination, paint);
    }
    public void close() {
        if (bitmap != null) { bitmap.recycle(); bitmap = null; }
    }
}
