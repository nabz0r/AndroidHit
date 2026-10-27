package com.androidhit.lumadrift.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.androidhit.lumadrift.R;

/** One original dream garden, decoded once and reused. Biomes are restrained color variations of that shared scene. */
public final class WorldBackdrop {
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private Bitmap bitmap;
    private int biome;
    private static final int[] TINTS={
        0x00fff0e8,0x1a9ce0c0,0x1cffc4aa,0x1abdc5ff,0x1a99dcec,
        0x1cffdc92,0x20c9b1e9,0x1cecae8d,0x20ffc8e0,0x1ecab8ef
    };
    public void setBiome(int value){biome=Math.max(0,Math.min(9,value));}
    public WorldBackdrop(Context context) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.dream_glade_v18, options);
    }
    public void draw(Canvas canvas, float width, float height, boolean quiet, int background) {
        if (bitmap == null || bitmap.isRecycled() || width<=0 || height<=0) return;
        float ratio = width / height;
        int cropWidth = Math.min(bitmap.getWidth(), Math.max(1,Math.round(bitmap.getHeight() * ratio)));
        int cropHeight = Math.min(bitmap.getHeight(), Math.max(1,Math.round(bitmap.getWidth() / ratio)));
        int left = (bitmap.getWidth() - cropWidth) / 2;
        int top = bitmap.getHeight() - cropHeight;
        source.set(left, top, left + cropWidth, top + cropHeight);
        destination.set(0, 0, width, height);
        paint.setAlpha(255);
        canvas.drawBitmap(bitmap, source, destination, paint);
        paint.setColor(TINTS[biome]);canvas.drawRect(destination,paint);
        // Quiet scenes keep the garden's warmth while making the board and inventory the focus.
        // The veil is always light, including when an older save has selected a previously dark theme.
        paint.setColor(quiet ? 0x87fff8f3 : 0x25fff8f3);
        canvas.drawRect(destination, paint);
    }
    public void close() {
        if (bitmap != null) { bitmap.recycle(); bitmap = null; }
    }
}
