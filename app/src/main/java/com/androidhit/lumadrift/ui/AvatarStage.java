package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import com.androidhit.lumadrift.data.AvatarStyles;
import com.androidhit.lumadrift.data.Catalog;

/**
 * Original companion sprites with twelve free stage treatments.
 * Sprite colours remain intact; style motifs and pose feedback surround the character.
 * One instance belongs to one UI thread. Decorations never determine touch coordinates.
 */
public final class AvatarStage {
    public enum Pose { IDLE, THINK, HAPPY, GATE, WIN }
    private static final int INK = 0xff40314f, WHITE = 0xfffffcff;
    private static final int[] SIDES = {-1, 1};
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private boolean comic;

    /** Radius is the portrait unit; full-height original body art fits x±1.9r and y−1.5r..+1.65r. */
    public void draw(Canvas canvas, Catalog.CharacterItem character, float x, float y, float r,
                     String styleId, Pose pose, long time, boolean reduceMotion) {
        if (canvas == null || !finite(x) || !finite(y) || !finite(r) || r <= 0) return;
        Catalog.CharacterItem who = character == null ? Catalog.CHARACTERS[0] : character;
        AvatarStyles.Style style = AvatarStyles.find(styleId);
        Pose actualPose = pose == null ? Pose.IDLE : pose;
        float clock = reduceMotion ? 0 : (time % 120000L) * .001f;
        float bob = reduceMotion ? 0 : (float) Math.sin(clock * 2 + who.shape * .27f) * .025f;
        float tilt = reduceMotion ? 0 : (float) Math.sin(clock * 2.5f) * (actualPose == Pose.WIN ? 4 : 1.2f);
        if (!reduceMotion && (actualPose == Pose.HAPPY || actualPose == Pose.WIN)) bob -= Math.abs((float)Math.sin(clock * 4)) * .07f;
        canvas.save();
        canvas.translate(x, y);
        canvas.scale(r, r);
        canvas.translate(0, bob);
        canvas.rotate(tilt);
        drawStyleBackdrop(canvas, style);
        CharacterSprites.drawBody(canvas, who, 0, 0, 1);
        drawStyleAccent(canvas, style, actualPose);
        canvas.restore();
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
        paint.setShader(null);
    }

    /** The twelve existing free styles remain recognizable without repainting the new character art. */
    public void drawStyleBackdrop(Canvas c, AvatarStyles.Style selected) {
        if (c == null) return;
        AvatarStyles.Style style = selected == null ? AvatarStyles.find(null) : selected;
        comic = false;
        int a = withAlpha(style.accent,110), b = withAlpha(style.secondary,120);
        oval(c, -.94f, 1.48f, .94f, 1.73f, 0x24312a49);
        switch (style.index) {
            case 0:
                arc(c, -1.18f, -1.38f, 1.18f, 1.5f, 175, 188, withAlpha(blend(a, WHITE, .74f),90), .035f); break;
            case 1:
                for (int sign : SIDES) {
                    line(c, sign * 1.06f, -.96f, sign * 1.30f, -.68f, b, .055f);
                    line(c, sign * 1.30f, -.68f, sign * 1.30f, .84f, a, .055f);
                    line(c, sign * 1.30f, .84f, sign * 1.07f, 1.09f, b, .055f);
                    circle(c, sign * 1.30f, .18f, .06f, WHITE);
                }
                break;
            case 2:
            case 3:
                paint.setAntiAlias(false);
                for (int sign : SIDES) for (int i = 0; i < 4; i++) {
                    fill(i % 2 == 0 ? a : b);
                    float x = sign * (1.11f + (i % 2) * .10f), y = -.98f + i * .64f;
                    c.drawRect(x - .075f, y - .075f, x + .075f, y + .075f, paint);
                }
                fill(style.index == 2 ? b : a);
                c.drawRect(-1.03f, 1.48f, 1.03f, 1.58f, paint);
                paint.setAntiAlias(true); break;
            case 4:
                for (int sign : SIDES) {
                    flower(c, sign * 1.10f, -.58f, .15f, a);
                    flower(c, sign * 1.21f, .86f, .11f, withAlpha(blend(a, WHITE, .35f),105));
                    leaf(c, sign * 1.0f, 1.31f, sign * 1.25f, 1.13f, b);
                }
                break;
            case 5:
                for (int sign : SIDES) {
                    arc(c, sign < 0 ? -1.38f : .95f, -.95f, sign < 0 ? -.95f : 1.38f, -.52f, 0, 360, b, .025f);
                    circle(c, sign * 1.17f, .84f, .075f, withAlpha(blend(a, WHITE, .60f),105));
                }
                arc(c, -1.1f, 1.34f, 1.1f, 1.73f, 0, 180, a, .06f); break;
            case 6:
                arc(c, -1.27f, -1.45f, 1.27f, 1.52f, 190, 318, withAlpha(blend(a, WHITE, .50f),90), .035f);
                line(c, -1.12f, -.80f, -1.26f, .18f, b, .018f);
                star(c, -1.12f, -.80f, .09f, b); star(c, -1.26f, .18f, .06f, WHITE);
                star(c, 1.13f, .94f, .10f, b); break;
            case 7:
                gear(c, -1.13f, .93f, .21f, b); gear(c, 1.15f, -.68f, .14f, a);
                arc(c, -1.12f, -1.35f, 1.12f, 1.54f, 45, 270, withAlpha(blend(a, WHITE, .5f),90), .04f); break;
            case 8:
                for (int sign : SIDES) {
                    circle(c, sign * 1.14f, -.63f, .13f, withAlpha(blend(a, WHITE, .50f),90));
                    heart(c, sign * 1.20f, .99f, .12f, b);
                }
                arc(c, -1.09f, 1.39f, 1.09f, 1.73f, 0, 180, a, .07f); break;
            case 9:
                crescent(c, -1.11f, -.72f, .20f, b);
                star(c, 1.15f, -.30f, .11f, a); star(c, -1.22f, .76f, .08f, b);
                arc(c, -1.14f, 1.35f, 1.14f, 1.70f, 0, 180, a, .06f); break;
            case 10:
                heart(c, -1.15f, -.59f, .16f, a); heart(c, 1.16f, .94f, .13f, b);
                heart(c, -1.18f, 1.17f, .09f, b); break;
            default:
                for (int sign : SIDES) {
                    diamond(c, sign * 1.13f, -.76f, .11f, .22f, b);
                    snowflake(c, sign * 1.16f, 1.02f, .14f, a);
                }
                arc(c, -1.15f, 1.36f, 1.15f, 1.72f, 0, 180, a, .05f); break;
        }
    }

    /** Pose information remains readable around the immutable raster face. */
    public void drawStyleAccent(Canvas c, AvatarStyles.Style selected, Pose expression) {
        if (c == null) return;
        AvatarStyles.Style style = selected == null ? AvatarStyles.find(null) : selected;
        Pose pose = expression == null ? Pose.IDLE : expression;
        comic = false;
        if (pose == Pose.THINK) {
            circle(c, 1.12f, -.98f, .055f, style.secondary);
            circle(c, 1.27f, -1.16f, .085f, blend(style.secondary, WHITE, .35f));
        } else if (pose == Pose.GATE) key(c, 1.10f, .44f, style.secondary);
        else if (pose == Pose.WIN || pose == Pose.HAPPY) {
            star(c, -1.14f, -1.05f, .10f, style.secondary);
            star(c, 1.14f, -.75f, .075f, style.accent);
        }
    }

    /**
     * Decorative edge layers only. progress is the caller's real completion fraction [0,1].
     * Motion never changes content progress. Reduced motion freezes temporal AND spatial
     * parallax; only a discrete milestone motif remains at its fixed position.
     */
    public void drawBackdrop(Canvas canvas, float width, float height, String styleId,
                             float progress, long time, boolean reduceMotion) {
        if (canvas == null || !finite(width) || !finite(height) || width <= 0 || height <= 0) return;
        AvatarStyles.Style style = AvatarStyles.find(styleId);
        float completed = finite(progress) ? clamp(progress) : 0;
        float phase = reduceMotion ? 0 : (time % 120000L) * .00007f;
        float travel = reduceMotion ? 0 : completed;
        comic = false;
        canvas.save();
        fill(style.background);
        canvas.drawRect(0, 0, width, height, paint);
        // All illustrated scenery is kept in the outer 16% and lower 12%.
        for (int i = 0; i < 7; i++) {
            float seed = (i * .173f + style.index * .037f) % 1f;
            float xx = ((seed - travel * .14f + 1f) % 1f) * width;
            float yy = height * (.03f + (i % 3) * .025f);
            float drift = reduceMotion ? 0 : (float)Math.sin(phase + i) * width * .003f;
            cloud(canvas, xx + drift, yy, Math.min(width, height) * .035f, blend(style.accent, WHITE, .82f));
        }
        float unit = Math.min(width, height) * .09f;
        for (int i = 0; i < 6; i++) {
            float xx = ((i * .211f - travel * .38f + 1.2f) % 1.2f) * width;
            oval(canvas, xx - unit * 1.8f, height * .94f, xx + unit * 1.8f, height * 1.08f,
                    blend(style.ground, WHITE, .32f));
        }
        // A fixed semantic landmark records real progress without autoplaying the journey.
        int milestone = Math.min(10, (int)(completed * 10));
        for (int side : SIDES) {
            canvas.save();
            canvas.translate(side < 0 ? width * .07f : width * .93f, height * .85f);
            canvas.scale(unit * .6f, unit * .6f);
            landmark(canvas, style, milestone, side);
            canvas.restore();
        }
        // Quiet central surface: decorative elements do not pass through board/text safe area.
        fill(0x38fffaff);
        canvas.drawRoundRect(width * .16f, height * .12f, width * .84f, height * .88f,
                unit * .3f, unit * .3f, paint);
        canvas.restore();
    }

    private void landmark(Canvas c, AvatarStyles.Style s, int milestone, int side) {
        switch (s.index) {
            case 1:
                rounded(c, -.45f, -.6f - milestone * .05f, .45f, .8f, .05f, s.accent); for (int i = 0; i < 3; i++) line(c, -.23f, -.32f + i * .23f, .23f, -.32f + i * .23f, s.secondary, .07f); break;
            case 2:
            case 3:
                fill(s.accent); c.drawRect(-.49f, -.6f, .49f, .8f, paint); fill(s.secondary); c.drawRect(-.3f, -.38f, .3f, -.03f, paint); fill(WHITE); c.drawRect(-.23f, .19f, -.07f, .35f, paint); break;
            case 4:
                line(c, 0, .8f, 0, -.65f, s.ground, .13f); flower(c, 0, -.62f, .57f, s.accent); flower(c, side * .35f, -.05f, .27f, s.secondary); break;
            case 5:
                shell(c, 0, .18f, .65f, s.secondary); for (int i = 0; i < 3; i++) circle(c, side * .51f, -.29f - i * .3f, .1f + i * .025f, s.accent); break;
            case 6:
                circle(c, 0, -.07f, .57f, s.accent); arc(c, -.86f, -.25f, .86f, .2f, 0, 330, s.secondary, .12f); star(c, side * .45f, -.89f, .18f, s.secondary); break;
            case 7:
                gear(c, 0, -.1f, .6f, s.accent); line(c, 0, .36f, 0, .8f, s.secondary, .13f); break;
            case 8:
                line(c, 0, -.16f, 0, .8f, s.secondary, .12f); circle(c, 0, -.36f, .55f, s.accent); heart(c, 0, -.36f, .22f, WHITE); break;
            case 9:
                oval(c, -.64f, -.28f, .64f, .72f, s.accent); line(c, 0, -.27f, .1f, -.59f, s.ground, .15f); triangle(c, -.35f, .07f, -.12f, .07f, -.25f, -.08f, INK); triangle(c, .35f, .07f, .12f, .07f, .25f, -.08f, INK); break;
            case 10:
                heart(c, 0, -.2f, .67f, s.accent); line(c, 0, .14f, 0, .8f, s.secondary, .1f); break;
            case 11:
                diamond(c, 0, -.05f, .48f, .92f, s.accent); snowflake(c, 0, -.1f, .3f, WHITE); break;
            default:
                line(c, 0, .8f, 0, -.24f, s.ground, .11f); flower(c, 0, -.3f, .46f, s.accent); break;
        }
        // Only the quantity changes at actual deciles; the landmarks do not imply future rewards.
        for (int i = 0; i < Math.min(3, milestone / 3); i++) star(c, -.4f + i * .4f, -1.13f, .11f, s.secondary);
    }

    private void fill(int color) { paint.setShader(null); paint.setStyle(Paint.Style.FILL); paint.setStrokeWidth(0); paint.setColor(color); paint.setAlpha((color >>> 24) & 255); }
    private void drawPath(Canvas c, int color) { fill(color); c.drawPath(path, paint); outline(c); }
    private void outline(Canvas c) { if (comic) { paint.setColor(INK); paint.setAlpha(255); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(.035f); c.drawPath(path, paint); paint.setStyle(Paint.Style.FILL); } }
    private void oval(Canvas c, float l, float t, float r, float b, int color) { fill(color); c.drawOval(l, t, r, b, paint); if (comic) { paint.setColor(INK); paint.setAlpha(255); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(.035f); c.drawOval(l, t, r, b, paint); paint.setStyle(Paint.Style.FILL); } }
    private void circle(Canvas c, float x, float y, float r, int color) { oval(c, x - r, y - r, x + r, y + r, color); }
    private void rounded(Canvas c, float l, float t, float r, float b, float radius, int color) { fill(color); c.drawRoundRect(l, t, r, b, radius, radius, paint); if (comic) { paint.setColor(INK); paint.setAlpha(255); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(.035f); c.drawRoundRect(l, t, r, b, radius, radius, paint); paint.setStyle(Paint.Style.FILL); } }
    private void line(Canvas c, float x, float y, float xx, float yy, int color, float width) { fill(color); paint.setStrokeWidth(width); paint.setStrokeCap(Paint.Cap.ROUND); c.drawLine(x, y, xx, yy, paint); }
    private void arc(Canvas c, float l, float t, float r, float b, float start, float sweep, int color, float width) { fill(color); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(width); paint.setStrokeCap(Paint.Cap.ROUND); c.drawArc(l, t, r, b, start, sweep, false, paint); paint.setStyle(Paint.Style.FILL); }
    private void triangle(Canvas c, float x, float y, float xx, float yy, float xxx, float yyy, int color) { path.reset(); path.moveTo(x, y); path.lineTo(xx, yy); path.lineTo(xxx, yyy); path.close(); drawPath(c, color); }
    private void diamond(Canvas c, float x, float y, float w, float h, int color) { path.reset(); path.moveTo(x, y - h); path.lineTo(x + w, y); path.lineTo(x, y + h); path.lineTo(x - w, y); path.close(); drawPath(c, color); }
    private void spikes(Canvas c, float x, float y, float r, int count, int color) { path.reset(); for (int i = 0; i < count * 2; i++) { double a = -Math.PI / 2 + i * Math.PI / count; float radius = i % 2 == 0 ? r : r * .79f; float xx = x + (float)Math.cos(a) * radius, yy = y + (float)Math.sin(a) * radius; if (i == 0) path.moveTo(xx, yy); else path.lineTo(xx, yy); } path.close(); drawPath(c, color); }
    private void star(Canvas c, float x, float y, float r, int color) { spikes(c, x, y, r, 5, color); }
    private void heart(Canvas c, float x, float y, float r, int color) { path.reset(); path.moveTo(x, y + r * .68f); path.cubicTo(x - r * 1.3f, y, x - r * .7f, y - r * 1.15f, x, y - r * .4f); path.cubicTo(x + r * .7f, y - r * 1.15f, x + r * 1.3f, y, x, y + r * .68f); path.close(); drawPath(c, color); }
    private void flower(Canvas c, float x, float y, float r, int color) { for (int i = 0; i < 5; i++) { double a = -Math.PI / 2 + i * Math.PI * 2 / 5; circle(c, x + (float)Math.cos(a) * r * .52f, y + (float)Math.sin(a) * r * .52f, r * .45f, color); } circle(c, x, y, r * .28f, 0xffffdc8f); }
    private void cloud(Canvas c, float x, float y, float r, int color) { oval(c, x - r * 1.14f, y - r * .35f, x - r * .1f, y + r * .69f, color); oval(c, x - r * .72f, y - r * .96f, x + r * .49f, y + r * .55f, color); oval(c, x + r * .03f, y - r * .64f, x + r * 1.09f, y + r * .64f, color); rounded(c, x - r * 1.03f, y - r * .06f, x + r * 1.04f, y + r * .73f, r * .3f, color); }
    private void shell(Canvas c, float x, float y, float r, int color) { path.reset(); path.moveTo(x, y + r * .66f); path.cubicTo(x - r * 1.5f, y - r * .8f, x + r * 1.5f, y - r * .8f, x, y + r * .66f); path.close(); drawPath(c, color); for (int i = -1; i <= 1; i++) line(c, x, y + r * .48f, x + i * r * .44f, y - r * .22f, WHITE, r * .08f); }
    private void gear(Canvas c, float x, float y, float r, int color) { spikes(c, x, y, r, 10, color); circle(c, x, y, r * .4f, WHITE); circle(c, x, y, r * .21f, color); }
    private void snowflake(Canvas c, float x, float y, float r, int color) { for (int i = 0; i < 3; i++) { double a = i * Math.PI / 3; float xx = (float)Math.cos(a) * r, yy = (float)Math.sin(a) * r; line(c, x - xx, y - yy, x + xx, y + yy, color, r * .18f); } }
    private void leaf(Canvas c,float x,float y,float xx,float yy,int color) { path.reset();path.moveTo(x,y);path.quadTo(x,yy,xx,yy);path.quadTo(xx,y,x,y);path.close();drawPath(c,color); }
    private void crescent(Canvas c,float x,float y,float r,int color) { path.reset();path.moveTo(x+r*.5f,y-r*.86f);path.cubicTo(x-r*.55f,y-r*1.1f,x-r*1.15f,y+r*.12f,x-r*.6f,y+r*.7f);path.cubicTo(x,y+r*1.2f,x+r*.7f,y+r*.7f,x+r*.84f,y+r*.3f);path.cubicTo(x,y+r*.6f,x-r*.1f,y-r*.3f,x+r*.5f,y-r*.86f);path.close();drawPath(c,color); }
    private void key(Canvas c, float x, float y, int color) { circle(c, x, y, .17f, color); circle(c, x, y, .075f, WHITE); line(c, x, y + .1f, x, y + .51f, color, .09f); line(c, x, y + .42f, x + .15f, y + .42f, color, .07f); }
    private static int withAlpha(int color,int alpha){return (color&0xffffff)|(alpha<<24);}
    private static boolean ellipse(float x, float y, float cx, float cy, float rx, float ry) { float xx = (x - cx) / rx, yy = (y - cy) / ry; return xx * xx + yy * yy <= 1; }
    private static int blend(int a, int b, float k) { int r = Math.round(((a >> 16) & 255) * (1-k) + ((b >> 16) & 255) * k), g = Math.round(((a >> 8) & 255) * (1-k) + ((b >> 8) & 255) * k), bb = Math.round((a & 255) * (1-k) + (b & 255) * k); return 0xff000000 | r << 16 | g << 8 | bb; }
    private static boolean finite(float value) { return !Float.isNaN(value) && !Float.isInfinite(value); }
    private static float clamp(float value) { return Math.max(0, Math.min(1, value)); }
}
