package com.androidhit.lumadrift.ui;

/** Aspect-preserving sprite placement. Pure geometry, shared by native art and host checks. */
public final class SpriteGeometry {
    private SpriteGeometry() { }

    /**
     * Fits visible source pixels in a target box. Alignment is 0=start, .5=centre, 1=end.
     * The caller owns out[4] (left, top, right, bottom); invalid inputs leave it untouched.
     */
    public static boolean fit(float sourceWidth, float sourceHeight,
                              float left, float top, float right, float bottom,
                              float alignX, float alignY, float[] out) {
        if (out == null || out.length < 4 || !finite(sourceWidth) || !finite(sourceHeight)
                || sourceWidth <= 0 || sourceHeight <= 0 || !finite(left) || !finite(top)
                || !finite(right) || !finite(bottom) || right <= left || bottom <= top
                || !finite(alignX) || !finite(alignY)) return false;
        float scale = Math.min((right - left) / sourceWidth, (bottom - top) / sourceHeight);
        float width = sourceWidth * scale, height = sourceHeight * scale;
        if (!finite(scale) || !finite(width) || !finite(height) || width <= 0 || height <= 0) return false;
        float x = left + (right - left - width) * clamp(alignX);
        float y = top + (bottom - top - height) * clamp(alignY);
        out[0] = x; out[1] = y; out[2] = x + width; out[3] = y + height;
        return true;
    }

    /** Integer cells partition every source pixel, including sheets with odd dimensions. */
    public static boolean cell(int width, int height, int columns, int rows, int index, int[] out) {
        if (out == null || out.length < 4 || width <= 0 || height <= 0 || columns <= 0 || rows <= 0
                || columns > width || rows > height || index < 0 || (long) index >= (long) columns * rows) return false;
        int column = index % columns, row = index / columns;
        out[0] = (int) ((long) column * width / columns);
        out[1] = (int) ((long) row * height / rows);
        out[2] = (int) ((long) (column + 1) * width / columns);
        out[3] = (int) ((long) (row + 1) * height / rows);
        return out[2] > out[0] && out[3] > out[1];
    }

    private static float clamp(float value) { return Math.max(0, Math.min(1, value)); }
    private static boolean finite(float value) { return !Float.isNaN(value) && !Float.isInfinite(value); }
}
