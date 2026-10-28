package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import com.androidhit.lumadrift.data.AvatarGearCatalog;
import com.androidhit.lumadrift.data.AvatarLoadout;
import com.androidhit.lumadrift.data.AvatarStyles;
import com.androidhit.lumadrift.data.Catalog;
import com.androidhit.lumadrift.data.CosmeticCatalog;
import java.util.List;

/**
 * One coherent portrait for a caller-supplied visual snapshot. No GameStore or ownership
 * operations are performed here. One instance per UI thread; drawing reuses all objects.
 */
public final class CompanionPortrait {
    /** Reserve this multiple of body radius around the centre for the fully dressed art. */
    public static final float ART_EXTENT = 2.0f;
    private static final int INK = 0xff40314f, WHITE = 0xfffffcff;
    private static final int[] SIDES = {-1, 1};
    private static final CosmeticCatalog.Slot[] CLOTHES = {
        CosmeticCatalog.Slot.DRESS, CosmeticCatalog.Slot.SHOES,
        CosmeticCatalog.Slot.GLOVES, CosmeticCatalog.Slot.HAT
    };
    private final AvatarStage avatar = new AvatarStage();
    private final KawaiiArt clothes = new KawaiiArt();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    /**
     * Last valid item per cosmetic slot wins, allowing a non-persisted preview override.
     * Null loadout/list means no corresponding overlay. Character/style/pose have safe defaults.
     */
    public void draw(Canvas canvas, Catalog.CharacterItem character, float x, float y, float r,
                     AvatarStyles.Style style, AvatarStage.Pose pose, long time, boolean reduceMotion,
                     AvatarLoadout loadout, List<CosmeticCatalog.Item> cosmetics) {
        if (canvas == null || !finite(x) || !finite(y) || !finite(r) || r <= 0) return;
        Catalog.CharacterItem who = character == null ? Catalog.CHARACTERS[0] : character;
        AvatarStyles.Style selected = style == null ? AvatarStyles.find(null) : style;
        AvatarStage.Pose expression = pose == null ? AvatarStage.Pose.IDLE : pose;
        float clock = reduceMotion ? 0 : (time % 120000L) * .001f;
        float bob = reduceMotion ? 0 : (float)Math.sin(clock * 2 + who.shape * .27f) * .025f;
        if (!reduceMotion && (expression == AvatarStage.Pose.HAPPY || expression == AvatarStage.Pose.WIN)) {
            bob -= Math.abs((float)Math.sin(clock * 4)) * .07f;
        }
        float tilt = reduceMotion ? 0 : (float)Math.sin(clock * 2.5f) * (expression == AvatarStage.Pose.WIN ? 4 : 1.2f);
        canvas.save(); canvas.translate(x, y); canvas.scale(r, r);
        canvas.translate(0, bob); canvas.rotate(tilt);
        if (loadout != null) aura(canvas, loadout.item(AvatarGearCatalog.Slot.AURA), selected);
        // Apply motion once to every layer: clothes never lag behind an independently moving body.
        avatar.draw(canvas, who, 0, 0, 1, selected.id, expression, 0, true);
        for (int slot = 0; slot < CLOTHES.length; slot++) {
            CosmeticCatalog.Item item = lastInSlot(cosmetics, CLOTHES[slot]);
            if (item == null) continue;
            int variant = CosmeticCatalog.ITEMS.indexOf(item) % 6;
            int color = cosmeticColor(item);
            if (item.slot == CosmeticCatalog.Slot.HAT) clothes.cosmetic(canvas, item.slot.name(), variant, 0, -.85f, .72f, color);
            else if (item.slot == CosmeticCatalog.Slot.DRESS) clothes.cosmetic(canvas, item.slot.name(), variant, 0, .85f, .70f, color);
            else if (item.slot == CosmeticCatalog.Slot.SHOES) clothes.cosmetic(canvas, item.slot.name(), variant, 0, 1.4f, .71f, color);
            else clothes.cosmetic(canvas, item.slot.name(), variant, 0, .63f, 1.08f, color);
        }
        if (loadout != null) {
            // The smaller legacy head ornament sits to one side of a wardrobe hat, not over its face.
            drawGearGlyph(canvas, loadout.item(AvatarGearCatalog.Slot.HEAD), .65f, -.96f, .32f);
            line(canvas, .75f, .49f, .75f, .77f, selected.secondary, .035f);
            drawGearGlyph(canvas, loadout.item(AvatarGearCatalog.Slot.CHARM), .75f, .9f, .25f);
        }
        canvas.restore();
    }

    /** Stationary catalogue glyph. Slot and item silhouettes remain distinguishable without colour. */
    public void drawGearGlyph(Canvas canvas, AvatarGearCatalog.Item item, float x, float y, float r) {
        drawGearGlyph(canvas, item, x, y, r, 0, true);
    }

    /** The optional time arguments use the same reduced-motion contract as the full portrait. */
    public void drawGearGlyph(Canvas canvas, AvatarGearCatalog.Item item, float x, float y, float r,
                              long time, boolean reduceMotion) {
        if (canvas == null || item == null || !finite(x) || !finite(y) || !finite(r) || r <= 0) return;
        int index = AvatarGearCatalog.ITEMS.indexOf(item);
        if (index < 0) return;
        int variant = index % 6;
        int color = gearColor(item), light = blend(color, WHITE, .44f);
        canvas.save(); canvas.translate(x, y); canvas.scale(r, r);
        if (!reduceMotion) canvas.rotate((float)Math.sin((time % 120000L) * .0018f) * 2);
        if (item.slot == AvatarGearCatalog.Slot.HEAD) {
            if (variant == 0) {
                line(canvas, 0, .74f, 0, -.24f, color, .1f);
                oval(canvas, -.04f, -.72f, .7f, -.18f, color);
                oval(canvas, -.55f, -.35f, .03f, .13f, light);
            } else if (variant == 1) {
                arc(canvas, -.35f, -.87f, .35f, -.16f, 180, 180, color, .1f);
                rounded(canvas, -.56f, -.49f, .56f, .76f, .2f, color);
                rounded(canvas, -.39f, -.32f, .39f, .49f, .12f, light); star(canvas, 0, .07f, .27f, WHITE);
                oval(canvas, .3f, -.93f, .89f, -.48f, 0xff88b995);
            } else if (variant == 2) {
                crescent(canvas, -.05f, -.13f, .67f, color);
                triangle(canvas, -.6f, .58f, 0, .31f, 0, .84f, light);
                triangle(canvas, .6f, .58f, 0, .31f, 0, .84f, light);
            } else if (variant == 3) {
                path.reset(); path.moveTo(-.86f, .67f); path.lineTo(-.91f, -.51f); path.lineTo(-.43f, -.15f);
                path.lineTo(0, -.82f); path.lineTo(.43f, -.15f); path.lineTo(.91f, -.51f); path.lineTo(.86f, .67f); path.close(); shape(canvas, color);
                rounded(canvas, -.87f, .42f, .87f, .69f, .09f, light); circle(canvas, 0, .03f, .16f, WHITE);
            } else if (variant == 4) {
                rounded(canvas, -.96f, -.36f, .96f, .38f, .2f, color);
                rounded(canvas, -.73f, -.18f, .73f, .2f, .1f, INK);
                star(canvas, -.24f, .01f, .25f, light); line(canvas, .27f, -.08f, .57f, -.08f, WHITE, .07f);
            } else {
                for (int sign : SIDES) {
                    line(canvas, sign * .23f, .7f, sign * .61f, -.84f, color, .14f);
                    line(canvas, sign * .44f, -.05f, sign * .91f, -.35f, light, .12f);
                    diamond(canvas, sign * .62f, -.85f, .13f, .2f, WHITE);
                }
            }
        } else if (item.slot == AvatarGearCatalog.Slot.AURA) {
            auraGlyph(canvas, variant, color, light);
        } else {
            if (variant == 0) {
                heart(canvas, 0, .05f, .8f, color); heart(canvas, -.03f, -.04f, .48f, light);
            } else if (variant == 1) {
                diamond(canvas, 0, 0, .86f, .95f, color); diamond(canvas, 0, 0, .55f, .64f, light);
                triangle(canvas, 0, -.61f, .22f, .24f, -.2f, .15f, INK);
            } else if (variant == 2) {
                circle(canvas, 0, .03f, .78f, color); ring(canvas, 0, .03f, .57f, light, .09f);
                star(canvas, 0, .03f, .31f, WHITE); line(canvas, -.48f, .68f, -.75f, .94f, color, .1f);
            } else if (variant == 3) {
                ring(canvas, 0, -.69f, .2f, color, .1f); line(canvas, 0, -.51f, 0, .78f, color, .12f);
                line(canvas, -.5f, -.13f, .5f, -.13f, color, .12f);
                arc(canvas, -.76f, -.34f, .76f, .86f, 0, 180, color, .14f);
                triangle(canvas, -.76f, .22f, -.46f, .39f, -.75f, .55f, light);
                triangle(canvas, .76f, .22f, .46f, .39f, .75f, .55f, light);
            } else if (variant == 4) {
                path.reset(); path.moveTo(-.71f, .56f); path.cubicTo(-.48f, .15f, -.64f, -.74f, 0, -.78f);
                path.cubicTo(.64f, -.74f, .48f, .15f, .71f, .56f); path.close(); shape(canvas, color);
                rounded(canvas, -.8f, .43f, .8f, .68f, .1f, light); circle(canvas, 0, .79f, .17f, color);
                star(canvas, 0, -.15f, .26f, WHITE);
            } else {
                crescent(canvas, 0, -.13f, .71f, color); oval(canvas, -.68f, .27f, -.02f, .83f, light);
                line(canvas, -.58f, .84f, .12f, .28f, color, .08f);
            }
        }
        canvas.restore();
    }

    private void aura(Canvas canvas, AvatarGearCatalog.Item item, AvatarStyles.Style style) {
        if (item == null) return;
        int variant = AvatarGearCatalog.ITEMS.indexOf(item) % 6;
        canvas.save(); canvas.scale(1.34f, 1.19f);
        auraGlyph(canvas, variant, blend(gearColor(item), style.accent, .2f), blend(style.secondary, WHITE, .45f));
        canvas.restore();
    }

    private void auraGlyph(Canvas canvas, int variant, int color, int light) {
        if (variant == 0) {
            ring(canvas, 0, 0, .86f, color, .045f);
            for (int i = 0; i < 3; i++) { double angle = i * Math.PI * 2 / 3; circle(canvas, (float)Math.cos(angle) * .9f, (float)Math.sin(angle) * .9f, .1f, light); }
        } else if (variant == 1) {
            arc(canvas, -.99f, -.62f, .99f, .62f, 0, 360, color, .065f);
            oval(canvas, -.93f, -.74f, -.51f, -.34f, light); oval(canvas, .51f, .34f, .93f, .74f, light);
        } else if (variant == 2) {
            for (int i = 0; i < 3; i++) ring(canvas, 0, 0, .59f + i * .17f, i == 1 ? light : color, .045f);
        } else if (variant == 3) {
            path.reset(); path.moveTo(0, 0); path.cubicTo(-1.4f, -1.23f, -1.4f, 1.23f, 0, 0);
            path.cubicTo(1.4f, -1.23f, 1.4f, 1.23f, 0, 0); fill(color); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(.09f); canvas.drawPath(path, paint);
            line(canvas, -.1f, .12f, -.48f, .94f, light, .08f); line(canvas, .1f, .12f, .48f, .94f, light, .08f);
        } else if (variant == 4) {
            arc(canvas, -.99f, -.65f, .99f, .65f, 25, 300, color, .08f);
            star(canvas, .77f, -.46f, .24f, light); line(canvas, -.9f, .45f, -.53f, .79f, color, .08f);
        } else {
            for (int i = 0; i < 6; i++) { double angle = i * Math.PI / 3; oval(canvas, (float)Math.cos(angle) * .76f - .16f, (float)Math.sin(angle) * .76f - .16f, (float)Math.cos(angle) * .76f + .16f, (float)Math.sin(angle) * .76f + .16f, i % 2 == 0 ? color : light); }
            ring(canvas, 0, 0, .9f, color, .045f);
        }
    }

    private static CosmeticCatalog.Item lastInSlot(List<CosmeticCatalog.Item> items, CosmeticCatalog.Slot slot) {
        if (items == null) return null;
        for (int i = items.size() - 1; i >= 0; i--) {
            CosmeticCatalog.Item item = items.get(i);
            if (item != null && item.slot == slot && CosmeticCatalog.ITEMS.contains(item)) return item;
        }
        return null;
    }

    public static int cosmeticColor(CosmeticCatalog.Item item) {
        if (item == null) return 0xff91cbb3;
        String id = item.id;
        if (id.contains("pumpkin") || id.contains("witch")) return 0xffffb473;
        if (id.contains("cupid") || id.contains("valentine")) return 0xffffa9cf;
        if (id.contains("moon")) return 0xffb7b0ff;
        if (id.contains("star")) return 0xffffd992;
        if (id.contains("aurora")) return 0xffa8dfff;
        return 0xff91e5cc;
    }

    private static int gearColor(AvatarGearCatalog.Item item) {
        return item.rarity == AvatarGearCatalog.Rarity.EPIC ? 0xffaa87ce : item.rarity == AvatarGearCatalog.Rarity.RARE ? 0xff78bdb2 : 0xffc8a45c;
    }
    private void fill(int color) { paint.setShader(null); paint.setColor(color); paint.setAlpha((color >>> 24) & 255); paint.setStyle(Paint.Style.FILL); paint.setStrokeWidth(0); }
    private void shape(Canvas c, int color) { fill(color); c.drawPath(path, paint); }
    private void oval(Canvas c,float l,float t,float r,float b,int color) { fill(color); c.drawOval(l,t,r,b,paint); }
    private void circle(Canvas c,float x,float y,float r,int color) { oval(c,x-r,y-r,x+r,y+r,color); }
    private void rounded(Canvas c,float l,float t,float r,float b,float corner,int color) { fill(color); c.drawRoundRect(l,t,r,b,corner,corner,paint); }
    private void line(Canvas c,float x,float y,float xx,float yy,int color,float width) { fill(color); paint.setStrokeWidth(width); paint.setStrokeCap(Paint.Cap.ROUND); c.drawLine(x,y,xx,yy,paint); }
    private void arc(Canvas c,float l,float t,float r,float b,float start,float sweep,int color,float width) { fill(color); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(width); paint.setStrokeCap(Paint.Cap.ROUND); c.drawArc(l,t,r,b,start,sweep,false,paint); }
    private void ring(Canvas c,float x,float y,float r,int color,float width) { arc(c,x-r,y-r,x+r,y+r,0,360,color,width); }
    private void triangle(Canvas c,float x,float y,float xx,float yy,float xxx,float yyy,int color) { path.reset();path.moveTo(x,y);path.lineTo(xx,yy);path.lineTo(xxx,yyy);path.close();shape(c,color); }
    private void diamond(Canvas c,float x,float y,float w,float h,int color) { path.reset();path.moveTo(x,y-h);path.lineTo(x+w,y);path.lineTo(x,y+h);path.lineTo(x-w,y);path.close();shape(c,color); }
    private void star(Canvas c,float x,float y,float r,int color) { path.reset();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float rr=i%2==0?r:r*.48f,xx=x+(float)Math.cos(a)*rr,yy=y+(float)Math.sin(a)*rr;if(i==0)path.moveTo(xx,yy);else path.lineTo(xx,yy);}path.close();shape(c,color); }
    private void heart(Canvas c,float x,float y,float r,int color) { path.reset();path.moveTo(x,y+r*.68f);path.cubicTo(x-r*1.3f,y,x-r*.7f,y-r*1.15f,x,y-r*.4f);path.cubicTo(x+r*.7f,y-r*1.15f,x+r*1.3f,y,x,y+r*.68f);path.close();shape(c,color); }
    private void crescent(Canvas c,float x,float y,float r,int color) { path.reset();path.moveTo(x+r*.49f,y-r*.86f);path.cubicTo(x-r*.55f,y-r*1.12f,x-r*1.12f,y+r*.08f,x-r*.63f,y+r*.7f);path.cubicTo(x-r*.06f,y+r*1.22f,x+r*.64f,y+r*.8f,x+r*.82f,y+r*.38f);path.cubicTo(x-r*.04f,y+r*.68f,x-r*.09f,y-r*.31f,x+r*.49f,y-r*.86f);path.close();shape(c,color); }
    private static int blend(int a,int b,float k) { return 0xff000000|Math.round(((a>>16)&255)*(1-k)+((b>>16)&255)*k)<<16|Math.round(((a>>8)&255)*(1-k)+((b>>8)&255)*k)<<8|Math.round((a&255)*(1-k)+(b&255)*k); }
    private static boolean finite(float value) { return !Float.isNaN(value) && !Float.isInfinite(value); }
}
