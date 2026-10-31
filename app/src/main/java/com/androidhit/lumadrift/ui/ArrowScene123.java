package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import com.androidhit.lumadrift.engine.Direction;

/**
 * Small glazed toy pieces. Presentation only: no board command, inference or animation.
 * The large centre arrow is movement; the left key tag and bottom relay rail have
 * separate reserved areas, including a piece carrying both roles (for example U29).
 * Coordinates, text and touch/accessibility targets remain the caller's responsibility.
 */
public final class ArrowScene123 {
    private static final int INK = 0xff153c64;
    private static final int CREAM = 0xffffffff;
    private static final int RELAY = 0xff4f7de6;
    private static final int[] TONES = {0xff91e5cc, 0xffffb398, 0xffceb3f7, 0xffa4cefa};
    private static final int[] KEY_INKS = {0xff087d76, 0xffb33f62, 0xff7044c0, 0xff067fac};
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private final Shader[] faces = new Shader[4];
    private final Shader gold = new LinearGradient(-14, -17, 15, 27,
            new int[]{0xfffff0ae, 0xfff4cd70, 0xffdca956}, null, Shader.TileMode.CLAMP);

    public ArrowScene123() {
        for (int i = 0; i < faces.length; i++) faces[i] = new LinearGradient(-25, -47, 25, 44,
                new int[]{mix(TONES[i], CREAM, .43f), TONES[i], mix(TONES[i], INK, .09f)},
                new float[]{0, .53f, 1}, Shader.TileMode.CLAMP);
    }

    /** Centre x/y, visible tile size (not cell size), keyNumber=0 when not a key. */
    public void tile(Canvas c, float x, float y, float size, Direction direction,
                     int switchMask, int keyNumber, boolean selected) {
        if (direction == null || !usable(size)) return;
        int save = c.save();
        c.translate(x, y); c.scale(size / 100f, size / 100f);
        ArtLibrary art = CharacterSprites.library();
        if (art != null) {
            float shiftX=keyNumber>0?5:0,shiftY=(switchMask&15)!=0?-4:0;
            float extent=keyNumber>0||(switchMask&15)!=0?42:47;
            oval(c,-41,39,41,49,0x182a79ab);
            if(selected){fill(0xffc4f5ff);c.drawCircle(0,0,53,p);stroke(PulseUi124.CYAN,2.6f);c.drawCircle(0,0,52,p);}
            String key="pulse/arrow/"+new String[]{"up","right","down","left"}[direction.ordinal()];
            if(art.draw(c,key,shiftX-extent,shiftY-extent,shiftX+extent,shiftY+extent,.5f,.5f,255)){
                if(keyNumber>0)keyTag(c,keyNumber,1);
                if((switchMask&15)!=0)relayRail(c,switchMask&15);
                c.restoreToCount(save);return;
            }
        }
        int tone = TONES[direction.ordinal()];
        // A small grounded shadow and opaque coloured rim, not a glowing HUD frame.
        oval(c, -43, 41, 43, 53, 0x1a42334f);
        round(c, -48, -44, 48, 49, 20, mix(tone, INK, .25f));
        fill(tone); p.setShader(faces[direction.ordinal()]);
        c.drawRoundRect(-48, -48, 48, 43, 20, 20, p); p.setShader(null);
        outline(c, -46.4f, -46.4f, 46.4f, 41.4f, 18.7f, 0xdffffcf6, 1.8f);
        // Broad upper reflection, a short glint and a quiet lower lip give material.
        round(c, -34, -42, 31, -29, 7, 0x56ffffff);
        oval(c, -34, -40, -19, -34, 0xeaffffff);
        line(c, -27, 36, 27, 36, 0x36ffffff, 2);
        if (selected) {
            outline(c, -52, -52, 52, 49, 22, 0xffffd77f, 3.6f);
            outline(c, -55, -55, 55, 52, 24, 0x4dc18a40, 1.2f);
        }
        // A sculpted dark pointer with one pale rim: the centre stays unobstructed.
        c.save(); c.rotate(direction.ordinal() * 90f);
        arrowPath();
        c.save(); c.translate(0, 1.7f); fill(0x30332844); c.drawPath(path, p); c.restore();
        stroke(CREAM, 4.4f); c.drawPath(path, p);
        fill(INK); c.drawPath(path, p);
        // Small light on the shaft, never another direction or a decorative icon.
        line(c, -3.7f, 3.5f, -3.7f, 15, 0x337d6a91, 1.6f);
        c.restore();
        if (keyNumber > 0) keyTag(c, keyNumber, 1);
        if ((switchMask & 15) != 0) relayRail(c, switchMask & 15);
        c.restoreToCount(save);
    }

    /**
     * Actual lock state supplied by KeyLinkScene122: opening 0=closed, 1=open;
     * opacity 0..1. This method never starts an opening or changes a barrier.
     */
    public void lock(Canvas c, float x, float y, float size, int keyNumber,
                     float opening, float opacity, boolean highlighted) {
        if (!usable(size) || keyNumber <= 0) return;
        float amount = clamp(opacity); if (amount == 0) return;
        float open = clamp(opening);
        int save = c.save(); c.translate(x, y); c.scale(size / 100f, size / 100f);
        int layer = c.saveLayerAlpha(-59, -59, 59, 59, Math.round(amount * 255));
        int ink = keyColor(keyNumber);
        ArtLibrary art=CharacterSprites.library();
        if(art!=null){
            if(highlighted){fill(0xffdaf9ff);c.drawCircle(0,0,53,p);stroke(PulseUi124.CYAN,2.5f);c.drawCircle(0,0,52,p);}
            String key="pulse/lock/"+(open>.35f?"open/":"closed/")+Math.floorMod(keyNumber-1,4);
            if(art.draw(c,key,-43,-49,45,48,.5f,.5f,255)){
                keyTag(c,keyNumber,1);c.restoreToCount(layer);c.restoreToCount(save);return;
            }
        }
        oval(c, -40, 38, 40, 51, 0x1942334f);
        round(c, -47, -43, 47, 47, 19, mix(ink, INK, .12f));
        round(c, -47, -47, 47, 41, 19, mix(ink, CREAM, .85f));
        outline(c, -45, -45, 45, 39, 18, 0xe6ffffff, 1.7f);
        round(c, -30, -41, 32, -30, 6, 0x7affffff);
        if (highlighted) outline(c, -51, -51, 51, 46, 22, 0xffe6b55a, 3);
        // The shackle pivots only while the real barrier reports its opening.
        c.save(); c.rotate(-open * 31f, 14, -2); c.translate(0, -open * 7);
        path.reset(); path.moveTo(-12, 0); path.lineTo(-12, -13);
        path.cubicTo(-12, -32, 14, -32, 14, -13); path.lineTo(14, 0);
        stroke(mix(ink, INK, .12f), 7.5f); c.drawPath(path, p);
        stroke(0xfffdf6e6, 2.1f); c.drawPath(path, p); c.restore();
        round(c, -20, -6, 25, 28, 8, 0xffb98d46);
        fill(0xfff0cb78); p.setShader(gold); c.drawRoundRect(-20, -9, 25, 24, 8, 8, p); p.setShader(null);
        outline(c, -18.5f, -7.5f, 23.5f, 22.5f, 7, 0xfffff2be, 1.7f);
        round(c, -13, -5, 17, 1, 3, 0x70ffffff);
        fill(ink); c.drawCircle(2.5f, 7, 4.1f, p); round(c, 0, 8, 5, 15, 2, ink);
        keyTag(c, keyNumber, 1);
        c.restoreToCount(layer); c.restoreToCount(save);
    }

    public static int directionColor(Direction direction) {
        return direction == null ? TONES[0] : TONES[direction.ordinal()];
    }

    public static int keyColor(int number) {
        return KEY_INKS[Math.floorMod(Math.max(1, number) - 1, KEY_INKS.length)];
    }

    /** Reserved geometry in caller coordinates, useful to inspect small tile layouts. */
    public static Geometry geometry(float x, float y, float size) {
        if (!usable(size)) throw new IllegalArgumentException("Positive finite tile size required");
        return new Geometry(x, y, size);
    }

    public static final class Box {
        public final float left, top, right, bottom;
        private Box(float x, float y, float size, float l, float t, float r, float b) {
            left = x + l * size / 100; top = y + t * size / 100;
            right = x + r * size / 100; bottom = y + b * size / 100;
        }
        public boolean overlaps(Box other) {
            return left < other.right && other.left < right && top < other.bottom && other.top < bottom;
        }
    }

    public static final class Geometry {
        public final Box direction, key, relay;
        private Geometry(float x, float y, float size) {
            // Direction includes pale rim and all four rotations, not only the path fill.
            direction = new Box(x, y, size, -27, -27, 27, 27);
            key = new Box(x, y, size, -48, -26, -29, 20);
            relay = new Box(x, y, size, -26, 29, 44, 46);
        }
    }

    private void keyTag(Canvas c, int number, float opacity) {
        int ink = keyColor(number);
        round(c, -47, -24, -30, 20, 7, alpha(mix(ink, INK, .15f), opacity));
        round(c, -47, -26, -30, 17, 7, alpha(CREAM, opacity));
        outline(c, -46, -25, -31, 16, 6, alpha(mix(ink,CREAM,.55f), opacity), 1.4f);
        stroke(ink, 1.8f);
        c.drawCircle(-39.5f, -17, 3.1f, p);
        c.drawLine(-37.3f, -14.8f, -34, -11.5f, p);
        c.drawLine(-35.9f, -13.4f, -33.8f, -15.5f, p);
        fill(ink); p.setTypeface(PulseUi124.face(900));
        p.setTextAlign(Paint.Align.CENTER); p.setTextSize(number < 10 ? 24 : 13);
        c.drawText(String.valueOf(number), -38.5f, 13.5f, p); p.setTextAlign(Paint.Align.LEFT);
    }

    private void relayRail(Canvas c, int mask) {
        int count = Integer.bitCount(mask); float width = 20 + 12 * count, left = 43 - width;
        round(c, left, 31, 43, 46, 6, 0xff523c71);
        round(c, left, 29, 43, 43, 6, RELAY);
        line(c, left + 6, 30.6f, 37, 30.6f, 0x72ffffff, 1);
        float cx = left + 8;
        // A switch mechanism is distinct from the movement pointer beside it.
        line(c, cx - 3.8f, 36.5f, cx + 3.8f, 36.5f, 0xfff8eafc, 1.6f);
        fill(0xffd6bfef); c.drawCircle(cx + 1.8f, 36.5f, 2.7f, p);
        int index = 0;
        for (Direction target : Direction.values()) if ((mask & target.bit) != 0) {
            float xx = left + 21 + index++ * 12;
            smallArrow(c, xx, 36.5f, target, 4.4f, 0xfffffcf8, 1.7f);
        }
    }

    private void arrowPath() {
        path.reset(); path.moveTo(-7.2f, 18); path.lineTo(-7.2f, -2);
        path.lineTo(-16, -2); path.quadTo(-20, -2, -17.4f, -5.1f);
        path.lineTo(-2.2f, -22.3f); path.quadTo(0, -24.8f, 2.2f, -22.3f);
        path.lineTo(17.4f, -5.1f); path.quadTo(20, -2, 16, -2);
        path.lineTo(7.2f, -2); path.lineTo(7.2f, 18);
        path.quadTo(7.2f, 21, 4.2f, 21); path.lineTo(-4.2f, 21);
        path.quadTo(-7.2f, 21, -7.2f, 18); path.close();
    }

    private void smallArrow(Canvas c, float x, float y, Direction direction, float r, int color, float width) {
        c.save(); c.translate(x, y); c.rotate(direction.ordinal() * 90f);
        stroke(color, width); path.reset(); path.moveTo(0, r); path.lineTo(0, -r);
        path.moveTo(-r * .64f, -r * .25f); path.lineTo(0, -r); path.lineTo(r * .64f, -r * .25f);
        c.drawPath(path, p); c.restore();
    }

    private void fill(int color) { p.setShader(null); p.setColor(color); p.setStyle(Paint.Style.FILL); }
    private void stroke(int color, float width) {
        fill(color); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(width);
        p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND);
    }
    private void round(Canvas c, float l, float t, float r, float b, float radius, int color) {
        fill(color); c.drawRoundRect(l, t, r, b, radius, radius, p);
    }
    private void oval(Canvas c, float l, float t, float r, float b, int color) {
        fill(color); c.drawOval(l, t, r, b, p);
    }
    private void line(Canvas c, float x, float y, float xx, float yy, int color, float width) {
        stroke(color, width); c.drawLine(x, y, xx, yy, p);
    }
    private void outline(Canvas c, float l, float t, float r, float b, float radius, int color, float width) {
        stroke(color, width); rect.set(l, t, r, b); c.drawRoundRect(rect, radius, radius, p);
    }
    private static boolean usable(float size) { return size > 0 && !Float.isNaN(size) && !Float.isInfinite(size); }
    private static float clamp(float n) { return Float.isNaN(n) ? 0 : Math.max(0, Math.min(1, n)); }
    private static int alpha(int color, float opacity) { return (color & 0xffffff) | (Math.round(clamp(opacity) * 255) << 24); }
    private static int mix(int a, int b, float t) {
        return Color.rgb(Math.round(Color.red(a) * (1 - t) + Color.red(b) * t),
                Math.round(Color.green(a) * (1 - t) + Color.green(b) * t),
                Math.round(Color.blue(a) * (1 - t) + Color.blue(b) * t));
    }
}
