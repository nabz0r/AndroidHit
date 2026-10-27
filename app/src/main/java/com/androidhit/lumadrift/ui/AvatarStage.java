package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import com.androidhit.lumadrift.data.AvatarStyles;
import com.androidhit.lumadrift.data.Catalog;

/**
 * Original code-drawn companion wardrobe: 24 silhouettes × 12 free treatments.
 * One instance belongs to one UI thread. Paint and paths are reused; no bitmap, shader,
 * array or path is allocated by draw. Decorations never determine touch coordinates.
 */
public final class AvatarStage {
    public enum Pose { IDLE, THINK, HAPPY, GATE, WIN }
    private static final int INK = 0xff40314f, WHITE = 0xfffffcff;
    private static final int[] SIDES = {-1, 1};
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private boolean comic;

    /** Radius is the body radius; maximum art extent is 1.65r horizontally/vertically. */
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
        comic = false;
        oval(canvas, -.88f, .98f, .88f, 1.17f, 0x2040314f);
        canvas.translate(0, bob);
        canvas.rotate(tilt);
        comic = style.family == AvatarStyles.Family.COMIC;
        int body = blend(who.color, style.accent, style.index == 0 ? .02f : .17f);
        if (style.family == AvatarStyles.Family.PIXEL) {
            pixelBody(canvas, who.shape, body, style);
            pixelFace(canvas, actualPose, style.index == 2 ? .115f : .09f);
        } else {
            body(canvas, who.shape, body, style.secondary);
            face(canvas, actualPose, who.shape == 6 ? -.2f : 0);
        }
        costume(canvas, style, actualPose);
        if (actualPose == Pose.THINK) {
            circle(canvas, 1.03f, -.87f, .09f, style.secondary);
            circle(canvas, 1.2f, -1.08f, .13f, blend(style.secondary, WHITE, .35f));
        } else if (actualPose == Pose.GATE) {
            key(canvas, .98f, .44f, style.secondary);
        } else if (actualPose == Pose.WIN) {
            star(canvas, -1.1f, -.98f, .15f, style.secondary);
            star(canvas, 1.14f, -.65f, .11f, style.accent);
        }
        canvas.restore();
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
        paint.setShader(null);
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

    private void body(Canvas c, int shape, int color, int accent) {
        int light = blend(color, WHITE, .5f), dark = blend(color, INK, .27f);
        switch (Math.floorMod(shape, 24)) {
            case 0:
                star(c, 0, 0, 1.05f, color); circle(c, 0, 0, .82f, color); break;
            case 1:
                triangle(c, -.88f, -.35f, -.84f, -1.14f, -.25f, -.63f, color);
                triangle(c, .88f, -.35f, .84f, -1.14f, .25f, -.63f, color);
                oval(c, -.95f, -.83f, .95f, .93f, color); break;
            case 2:
                rounded(c, -.92f, -.83f, .92f, .91f, .29f, color);
                line(c, 0, -.82f, 0, -1.13f, dark, .09f); circle(c, 0, -1.14f, .12f, accent);
                rounded(c, -.66f, -.42f, .66f, .29f, .2f, light); break;
            case 3:
                rounded(c, -.65f, -1.4f, -.23f, -.35f, .2f, color);
                rounded(c, .23f, -1.4f, .65f, -.35f, .2f, color);
                oval(c, -.93f, -.79f, .93f, .94f, color); break;
            case 4:
                triangle(c, -.4f, .3f, -1.42f, -.8f, .4f, -.6f, accent);
                oval(c, -.87f, -.86f, .9f, .92f, color); break;
            case 5:
                for (int i = -1; i <= 1; i++) circle(c, i * .53f, .77f, .29f, dark);
                oval(c, -.94f, -.88f, .94f, .91f, color); break;
            case 6:
                path.reset(); path.moveTo(.56f, -.98f);
                path.cubicTo(-.12f, -1.26f, -1.13f, -.58f, -1.04f, .2f);
                path.cubicTo(-.91f, 1.1f, .25f, 1.24f, .87f, .5f);
                path.cubicTo(.1f, .72f, .06f, -.25f, .56f, -.98f); path.close(); drawPath(c, color);
                star(c, .64f, -.53f, .2f, accent); break;
            case 7:
                triangle(c, -.91f, -.1f, -.94f, -1.11f, -.15f, -.61f, color);
                triangle(c, .91f, -.1f, .94f, -1.11f, .15f, -.61f, color);
                diamond(c, 0, .05f, .98f, .98f, color);
                triangle(c, -.86f, .2f, -.12f, .15f, 0, .86f, light);
                triangle(c, .86f, .2f, .12f, .15f, 0, .86f, light); break;
            case 8:
                triangle(c, -.8f, -.25f, -.73f, -1.04f, -.16f, -.63f, color);
                triangle(c, .8f, -.25f, .73f, -1.04f, .16f, -.63f, color);
                oval(c, -.94f, -.83f, .94f, 1, color);
                oval(c, -.78f, -.57f, .01f, .35f, light); oval(c, -.01f, -.57f, .78f, .35f, light);
                triangle(c, -.12f, .25f, .12f, .25f, 0, .44f, accent); break;
            case 9:
                oval(c, -1.22f, -.95f, -.23f, .04f, light); oval(c, .23f, -.95f, 1.22f, .04f, light);
                oval(c, -.86f, -.83f, .86f, .97f, color);
                rounded(c, -.7f, .48f, .7f, .63f, .07f, dark);
                rounded(c, -.48f, .76f, .48f, .87f, .05f, dark); break;
            case 10:
                for (int s : SIDES) { oval(c, s < 0 ? -1.06f : .59f, -.52f, s < 0 ? -.59f : 1.06f, .15f, dark); oval(c, s < 0 ? -.95f : .52f, .39f, s < 0 ? -.52f : .95f, .99f, dark); }
                oval(c, -.77f, -1.02f, .77f, .97f, color); oval(c, -.62f, -.72f, .62f, .83f, light); break;
            case 11:
                for (int i = 0; i < 5; i++) rounded(c, -.85f + i * .34f, .3f, -.61f + i * .34f, 1.18f + i % 2 * .12f, .12f, i % 2 == 0 ? color : light);
                oval(c, -.99f, -.98f, .99f, .69f, color); rounded(c, -.99f, .27f, .99f, .63f, .18f, light); break;
            case 12:
                for (int s : SIDES) { oval(c, s < 0 ? -1.31f : .06f, -1.03f, s < 0 ? -.06f : 1.31f, .31f, color); oval(c, s < 0 ? -1.08f : .06f, -.06f, s < 0 ? -.06f : 1.08f, .92f, light); }
                oval(c, -.57f, -.79f, .57f, .89f, color); break;
            case 13:
                cloud(c, 0, 0, 1, color); break;
            case 14:
                for (int i = 0; i < 12; i++) { double a = i * Math.PI / 6; float xx = (float)Math.cos(a), yy = (float)Math.sin(a); triangle(c, xx * 1.24f, yy * 1.24f, xx * .81f - yy * .14f, yy * .81f + xx * .14f, xx * .81f + yy * .14f, yy * .81f - xx * .14f, color); }
                circle(c, 0, 0, .88f, light); break;
            case 15:
                rounded(c, -1.05f, .56f, 1.22f, .95f, .18f, dark); oval(c, .45f, -.13f, 1.21f, .84f, light);
                line(c, .68f, .05f, .64f, -.5f, dark, .08f); line(c, 1.03f, .05f, 1.14f, -.48f, dark, .08f);
                oval(c, -.98f, -.99f, .86f, .85f, color); arc(c, -.67f, -.73f, .58f, .52f, 40, 290, light, .075f); break;
            case 16:
                for (int i = 0; i < 3; i++) for (int s : SIDES) line(c, s * .76f, -.58f + i * .4f, s * 1.24f, -.58f + i * .4f + (i - 1) * .2f, dark, .15f);
                oval(c, -.97f, -.86f, .97f, .95f, color); break;
            case 17:
                rounded(c, -1.08f, -.35f, -.43f, .62f, .25f, dark); rounded(c, .43f, -.72f, 1.08f, .53f, .25f, dark);
                rounded(c, -.64f, -1.04f, .64f, .97f, .54f, color); flower(c, .34f, -1.02f, .21f, accent); break;
            case 18:
                spikes(c, 0, .03f, 1.13f, 14, dark); oval(c, -.82f, -.73f, .82f, .98f, light); break;
            case 19:
                oval(c, -1.05f, -.05f, -.5f, .85f, dark); oval(c, .5f, -.05f, 1.05f, .85f, dark);
                oval(c, -.87f, -1.02f, .87f, .98f, color); oval(c, -.65f, -.72f, .65f, .85f, light);
                rounded(c, -.66f, .87f, -.17f, 1.05f, .07f, accent); rounded(c, .17f, .87f, .66f, 1.05f, .07f, accent); break;
            case 20:
                arc(c, -1.34f, -.34f, 1.34f, .34f, 180, 180, accent, .17f);
                circle(c, 0, 0, .88f, color); arc(c, -1.34f, -.34f, 1.34f, .34f, 0, 180, accent, .17f); break;
            case 21:
                triangle(c, -.57f, -.15f, -1.31f, -.63f, -.99f, .65f, dark); triangle(c, .57f, -.15f, 1.31f, -.63f, .99f, .65f, dark);
                triangle(c, -.67f, -.55f, -.62f, -1.16f, -.17f, -.75f, accent); triangle(c, .67f, -.55f, .62f, -1.16f, .17f, -.75f, accent);
                oval(c, -.88f, -.92f, .88f, .97f, color); oval(c, -.49f, .32f, .49f, .87f, light); break;
            case 22:
                circle(c, -.63f, -.75f, .4f, dark); circle(c, .63f, -.75f, .4f, dark); oval(c, -.96f, -.94f, .96f, .98f, color);
                oval(c, -.66f, -.46f, -.11f, .3f, light); oval(c, .11f, -.46f, .66f, .3f, light); break;
            default:
                for (int i = 0; i < 8; i++) rounded(c, -.92f + i * .23f, .4f, -.75f + i * .23f, 1.03f + i % 3 * .13f, .08f, i % 2 == 0 ? color : light);
                triangle(c, 0, -1.17f, -.94f, .53f, .94f, .53f, color); oval(c, -.9f, -.46f, .9f, .67f, color); break;
        }
        if (!comic) oval(c, -.54f, -.71f, -.08f, -.52f, 0x69ffffff);
    }

    private void face(Canvas c, Pose pose, float offset) {
        float eye = -.12f;
        if (pose == Pose.HAPPY || pose == Pose.WIN) {
            arc(c, offset - .48f, eye - .1f, offset - .19f, eye + .16f, 190, 160, INK, .065f);
            arc(c, offset + .19f, eye - .1f, offset + .48f, eye + .16f, 190, 160, INK, .065f);
        } else {
            oval(c, offset - .43f, eye - .17f, offset - .25f, eye + .13f, INK);
            oval(c, offset + .25f, eye - .17f, offset + .43f, eye + .13f, INK);
            circle(c, offset - .31f, eye - .09f, .035f, WHITE); circle(c, offset + .37f, eye - .09f, .035f, WHITE);
        }
        oval(c, offset - .64f, .17f, offset - .37f, .3f, 0xffeda9b9);
        oval(c, offset + .37f, .17f, offset + .64f, .3f, 0xffeda9b9);
        if (pose == Pose.THINK) circle(c, offset + .03f, .33f, .075f, INK);
        else arc(c, offset - .14f, .16f, offset + .14f, .43f, 5, 170, INK, .045f);
    }

    /** Pixel bodies use an original occupancy grid, not an antialiased vector recolour. */
    private void pixelBody(Canvas c, int shape, int color, AvatarStyles.Style style) {
        boolean retro = style.index == 2;
        float cell = retro ? .14f : .105f;
        int count = retro ? 19 : 25;
        comic = false;
        paint.setAntiAlias(false);
        for (int row = 0; row < count; row++) for (int col = 0; col < count; col++) {
            float xx = (col - (count - 1) * .5f) * cell, yy = (row - (count - 1) * .5f) * cell;
            if (!inside(shape, xx, yy)) continue;
            boolean border = !inside(shape, xx - cell, yy) || !inside(shape, xx + cell, yy) || !inside(shape, xx, yy - cell) || !inside(shape, xx, yy + cell);
            int ink = border ? INK : yy > .5f ? blend(color, INK, .15f) : yy < -.35f && xx < -.2f ? blend(color, WHITE, .4f) : color;
            fill(ink); c.drawRect(xx - cell * .5f, yy - cell * .5f, xx + cell * .51f, yy + cell * .51f, paint);
        }
        paint.setAntiAlias(true);
    }

    private static boolean inside(int shape, float x, float y) {
        boolean round = ellipse(x, y, 0, 0, .94f, .93f);
        switch (Math.floorMod(shape, 24)) {
            case 0: return round || Math.abs(x) + Math.abs(y) < 1.2f;
            case 1: return round || y < -.55f && y > -1.2f && Math.abs(x) > .39f && Math.abs(x) < .86f;
            case 2: return Math.abs(x) < .93f && Math.abs(y) < .83f || Math.abs(x) < .1f && y < -.8f && y > -1.2f;
            case 3: return round || Math.abs(Math.abs(x) - .45f) < .18f && y < -.45f && y > -1.37f;
            case 4: return round || x < -.3f && x > -1.32f && y > x * .6f - .2f && y < .05f;
            case 5: return round || y > .65f && y < 1.1f && Math.abs(Math.sin(x * 6)) < .8f && Math.abs(x) < .85f;
            case 6: return round && !ellipse(x, y, .56f, -.33f, .67f, .86f);
            case 7: return Math.abs(x) + Math.abs(y) < 1.17f || y < -.55f && y > -1.1f && Math.abs(x) > .38f && Math.abs(x) < .87f;
            case 8: return ellipse(x, y, 0, .08f, .88f, 1.01f) || y < -.65f && y > -1.12f && Math.abs(x) > .4f && Math.abs(x) < .72f;
            case 9: return ellipse(x, y, 0, .1f, .76f, .95f) || ellipse(x, y, -.82f, -.55f, .47f, .53f) || ellipse(x, y, .82f, -.55f, .47f, .53f);
            case 10: return ellipse(x, y, 0, 0, .72f, 1.03f) || Math.abs(x) < 1.06f && Math.abs(x) > .6f && (Math.abs(y + .25f) < .28f || Math.abs(y - .63f) < .24f);
            case 11: return ellipse(x, y, 0, -.2f, 1, .8f) || y > .15f && y < 1.15f && Math.abs(x) < .9f && Math.abs(Math.sin(x * 9)) > .3f;
            case 12: return ellipse(x, y, -.7f, -.35f, .62f, .7f) || ellipse(x, y, .7f, -.35f, .62f, .7f) || ellipse(x, y, -.55f, .5f, .51f, .49f) || ellipse(x, y, .55f, .5f, .51f, .49f);
            case 13: return ellipse(x, y, -.65f, .1f, .54f, .56f) || ellipse(x, y, -.22f, -.25f, .65f, .72f) || ellipse(x, y, .55f, -.04f, .58f, .64f) || Math.abs(x) < 1.03f && y > .03f && y < .7f;
            case 14: return ellipse(x, y, 0, 0, .84f, .84f) || Math.hypot(x, y) < 1.22f && Math.cos(Math.atan2(y, x) * 12) > .55;
            case 15: return round || Math.abs(x) < 1.2f && y > .59f && y < .96f || ellipse(x, y, .86f, .37f, .37f, .55f);
            case 16: return round || Math.abs(x) < 1.25f && Math.abs(x) > .7f && Math.abs(y) < .7f && Math.abs(Math.sin(y * 8)) > .55f;
            case 17: return Math.abs(x) < .62f && y > -.99f && y < .96f || x < -.5f && x > -1.08f && y > -.32f && y < .6f || x > .5f && x < 1.08f && y > -.72f && y < .5f;
            case 18: return Math.hypot(x, y) < 1.02f + .1f * Math.cos(Math.atan2(y, x) * 14);
            case 19: return ellipse(x, y, 0, 0, .83f, 1.02f) || ellipse(x, y, -.83f, .37f, .25f, .47f) || ellipse(x, y, .83f, .37f, .25f, .47f);
            case 20: return ellipse(x, y, 0, 0, .84f, .84f) || ellipse(x, y, 0, 0, 1.34f, .3f);
            case 21: return round || Math.abs(x) > .6f && Math.abs(x) < 1.3f && y > -.65f && y < .54f || Math.abs(Math.abs(x) - .5f) < .15f && y > -1.15f && y < -.6f;
            case 22: return round || ellipse(x, y, -.63f, -.79f, .39f, .38f) || ellipse(x, y, .63f, -.79f, .39f, .38f);
            default: return y > -1.15f && y < .65f && Math.abs(x) < (y + 1.15f) * .8f || y > .36f && y < 1.24f && Math.abs(x) < .92f && Math.abs(Math.sin(x * 13)) > .33f;
        }
    }

    private void pixelFace(Canvas c, Pose pose, float cell) {
        paint.setAntiAlias(false);
        for (int s : SIDES) {
            float x = s * .34f;
            fill(INK);
            c.drawRect(x - cell, -.26f, x + cell, pose == Pose.HAPPY || pose == Pose.WIN ? -.12f : .05f, paint);
            fill(0xffeda9b9); c.drawRect(s < 0 ? -.67f : .43f, .16f, s < 0 ? -.43f : .67f, .28f, paint);
        }
        fill(INK); c.drawRect(-cell, .27f, cell, .27f + cell, paint);
        paint.setAntiAlias(true);
    }

    private void costume(Canvas c, AvatarStyles.Style style, Pose pose) {
        int a = style.accent, b = style.secondary;
        boolean oldComic = comic;
        switch (style.index) {
            case 0:
                heart(c, -.62f, .59f, .16f, a); line(c, -.76f, .57f, -.48f, .65f, WHITE, .025f); break;
            case 1:
                // Asymmetric headset, angular visor and a visible antenna.
                rounded(c, -.82f, -.48f, .82f, -.24f, .06f, INK); rounded(c, -.76f, -.44f, .65f, -.3f, .03f, b);
                rounded(c, -.99f, -.43f, -.77f, .2f, .07f, a); line(c, -.89f, -.43f, -1.09f, -.93f, a, .08f); circle(c, -1.09f, -.93f, .09f, b);
                line(c, .73f, .49f, 1.05f, .49f, b, .06f); line(c, 1.05f, .49f, 1.05f, .19f, b, .06f); break;
            case 2:
                comic = false; paint.setAntiAlias(false); fill(b);
                c.drawRect(-.62f, -.94f, .62f, -.72f, paint); c.drawRect(-.62f, -1.18f, -.39f, -.91f, paint); c.drawRect(-.13f, -1.27f, .13f, -.91f, paint); c.drawRect(.39f, -1.18f, .62f, -.91f, paint);
                fill(a); c.drawRect(-.12f, -1.03f, .12f, -.82f, paint); paint.setAntiAlias(true); break;
            case 3:
                comic = false; paint.setAntiAlias(false); fill(a); c.drawRect(-.69f, -1.03f, .49f, -.77f, paint); c.drawRect(-.86f, -.86f, .74f, -.71f, paint);
                fill(b); c.drawRect(-.51f, -.94f, -.35f, -.78f, paint); fill(WHITE); c.drawRect(.32f, .57f, .79f, .72f, paint); paint.setAntiAlias(true); break;
            case 4:
                line(c, -.76f, -.79f, .62f, -.83f, b, .08f); flower(c, .53f, -.87f, .24f, a); flower(c, -.59f, -.81f, .14f, blend(a, WHITE, .3f)); break;
            case 5:
                triangle(c, -.73f, .48f, -1.23f, .25f, -1.19f, .83f, b); triangle(c, .73f, .48f, 1.23f, .25f, 1.19f, .83f, b);
                rounded(c, -.68f, .61f, .68f, .78f, .06f, a); circle(c, .51f, .69f, .08f, WHITE); shell(c, .66f, -.86f, .28f, b); break;
            case 6:
                comic = false; arc(c, -1.07f, -1.09f, 1.07f, 1.05f, 185, 350, blend(a, WHITE, .6f), .1f);
                rounded(c, -.73f, .64f, .73f, .89f, .1f, a); star(c, .5f, .76f, .12f, b); oval(c, -.7f, -.92f, -.34f, -.73f, 0x88ffffff); break;
            case 7:
                rounded(c, -.64f, -1.22f, .6f, -.71f, .08f, a); oval(c, -.91f, -.87f, .86f, -.65f, a);
                for (int s : SIDES) { circle(c, s * .34f, -.73f, .22f, INK); circle(c, s * .34f, -.73f, .15f, b); }
                gear(c, .71f, .53f, .2f, b); break;
            case 8:
                cloud(c, 0, -.89f, .61f, WHITE); for (int i = 0; i < 5; i++) line(c, -.5f + i * .23f, -.99f, -.43f + i * .23f, -.91f, i % 2 == 0 ? a : b, .05f);
                triangle(c, -.63f, .59f, 0, .43f, 0, .87f, a); triangle(c, .63f, .59f, 0, .43f, 0, .87f, a); circle(c, 0, .66f, .13f, b); break;
            case 9:
                triangle(c, -.55f, -.75f, .08f, -1.43f, .55f, -.75f, a); oval(c, -.8f, -.87f, .82f, -.66f, INK);
                rounded(c, -.37f, -.99f, .4f, -.83f, .03f, b); star(c, .15f, -1.14f, .1f, b); break;
            case 10:
                heart(c, 0, -1.02f, .29f, a); arc(c, -.68f, -.98f, .68f, -.57f, 180, 180, b, .09f);
                triangle(c, -.58f, .57f, -.05f, .4f, -.05f, .85f, a); triangle(c, .58f, .57f, .05f, .4f, .05f, .85f, a); heart(c, 0, .65f, .16f, b); break;
            default:
                diamond(c, 0, -1.09f, .2f, .4f, b); diamond(c, -.39f, -.94f, .13f, .25f, a); diamond(c, .39f, -.94f, .13f, .25f, a);
                rounded(c, -.71f, .59f, .71f, .78f, .08f, a); line(c, .44f, .72f, .62f, 1.05f, b, .17f); snowflake(c, -.52f, .68f, .12f, WHITE); break;
        }
        comic = oldComic;
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
    private void key(Canvas c, float x, float y, int color) { circle(c, x, y, .17f, color); circle(c, x, y, .075f, WHITE); line(c, x, y + .1f, x, y + .51f, color, .09f); line(c, x, y + .42f, x + .15f, y + .42f, color, .07f); }
    private static boolean ellipse(float x, float y, float cx, float cy, float rx, float ry) { float xx = (x - cx) / rx, yy = (y - cy) / ry; return xx * xx + yy * yy <= 1; }
    private static int blend(int a, int b, float k) { int r = Math.round(((a >> 16) & 255) * (1-k) + ((b >> 16) & 255) * k), g = Math.round(((a >> 8) & 255) * (1-k) + ((b >> 8) & 255) * k), bb = Math.round((a & 255) * (1-k) + (b & 255) * k); return 0xff000000 | r << 16 | g << 8 | bb; }
    private static boolean finite(float value) { return !Float.isNaN(value) && !Float.isInfinite(value); }
    private static float clamp(float value) { return Math.max(0, Math.min(1, value)); }
}
