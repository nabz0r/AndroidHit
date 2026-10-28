package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * Original little shop treasures. Geometry is reused; there are no bitmaps, clocks, labels,
 * transactions or per-frame allocations here. The caller owns the interaction and motion setting.
 */
public final class ShopIllustrations {
    private static final int INK=0xff343149, WHITE=0xfffffcff;
    private static final int[] TINTS={0xffffd2c4,0xffbdebd8,0xffd3c3f7,0xffffe6af};
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();

    private void fill(int color){paint.setShader(null);paint.setColor(color);paint.setStyle(Paint.Style.FILL);}
    private void disk(Canvas c,float x,float y,float r,int color){fill(color);c.drawCircle(x,y,r,paint);}
    private void oval(Canvas c,float l,float t,float r,float b,int color){fill(color);c.drawOval(l,t,r,b,paint);}
    private void round(Canvas c,float l,float t,float r,float b,float radius,int color){fill(color);c.drawRoundRect(l,t,r,b,radius,radius,paint);}
    private void line(Canvas c,float x,float y,float xx,float yy,float width,int color){fill(color);paint.setStrokeWidth(width);paint.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,paint);}
    private void stroke(Canvas c,float width,int color){fill(color);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(width);paint.setStrokeCap(Paint.Cap.ROUND);c.drawPath(path,paint);paint.setStyle(Paint.Style.FILL);}
    private void drawPath(Canvas c,int color){fill(color);c.drawPath(path,paint);}
    private static float bob(long time,boolean still,float phase){return still?0:(float)Math.sin(time*.0022+phase)*.035f;}
    private void begin(Canvas c,float x,float y,float r,long time,boolean still,float phase){c.save();c.translate(x,y+r*bob(time,still,phase));c.scale(r,r);}
    private void star(Canvas c,float x,float y,float r,int color){
        path.reset();for(int i=0;i<10;i++){double angle=i*Math.PI/5-Math.PI/2;float radius=i%2==0?r:r*.48f;float xx=x+(float)Math.cos(angle)*radius,yy=y+(float)Math.sin(angle)*radius;if(i==0)path.moveTo(xx,yy);else path.lineTo(xx,yy);}path.close();drawPath(c,color);
    }
    private void twinkle(Canvas c,float x,float y,float r,int color){
        path.reset();path.moveTo(x,y-r);path.quadTo(x+r*.18f,y-r*.16f,x+r,y);path.quadTo(x+r*.18f,y+r*.16f,x,y+r);path.quadTo(x-r*.18f,y+r*.16f,x-r,y);path.quadTo(x-r*.18f,y-r*.16f,x,y-r);path.close();drawPath(c,color);
    }
    private void face(Canvas c,float x,float y,float size){
        oval(c,x-size*.34f,y-size*.19f,x-size*.18f,y+size*.08f,INK);oval(c,x+size*.18f,y-size*.19f,x+size*.34f,y+size*.08f,INK);
        disk(c,x-size*.29f,y-size*.13f,size*.035f,WHITE);disk(c,x+size*.23f,y-size*.13f,size*.035f,WHITE);
        oval(c,x-size*.57f,y+size*.02f,x-size*.31f,y+size*.18f,0xfff2a5b6);oval(c,x+size*.31f,y+size*.02f,x+size*.57f,y+size*.18f,0xfff2a5b6);
        fill(INK);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(size*.065f);paint.setStrokeCap(Paint.Cap.ROUND);c.drawArc(x-size*.12f,y+size*.02f,x+size*.12f,y+size*.25f,5,170,false,paint);paint.setStyle(Paint.Style.FILL);
    }
    private void leaf(Canvas c,float x,float y,float r,int color){
        path.reset();path.moveTo(x-r*.70f,y+r*.40f);path.cubicTo(x-r*.95f,y-r*.48f,x+r*.25f,y-r*.80f,x+r*.82f,y-r*.55f);path.cubicTo(x+r*.80f,y+r*.32f,x-r*.06f,y+r*.86f,x-r*.70f,y+r*.40f);path.close();drawPath(c,color);line(c,x-r*.50f,y+r*.30f,x+r*.43f,y-r*.29f,r*.11f,WHITE);
    }

    /** Earned LumaCoins: a round amber token with an embossed leaf; never a premium gem. */
    public void coin(Canvas c,float x,float y,float r,long time,boolean still){
        begin(c,x,y,r,time,still,.4f);
        oval(c,-.76f,.75f,.76f,.95f,0x18343149);
        disk(c,0,.045f,.84f,0xffbd793e);disk(c,0,-.035f,.84f,0xffffca69);disk(c,0,-.035f,.65f,0xfff3a74e);
        fill(0xffffe5a5);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(.045f);c.drawCircle(0,-.035f,.67f,paint);paint.setStyle(Paint.Style.FILL);
        leaf(c,0,-.045f,.58f,0xffffefc7);oval(c,-.49f,-.64f,-.15f,-.50f,0xdbffffff);
        twinkle(c,.80f,-.70f,.14f,0xffffe6aa);c.restore();
    }

    /** LumaGold: a violet, cyan-faceted diamond. Shape and color differ from earned coins. */
    public void gold(Canvas c,float x,float y,float r,long time,boolean still){
        begin(c,x,y,r,time,still,1.5f);
        oval(c,-.66f,.80f,.66f,.97f,0x18343149);
        path.reset();path.moveTo(-.52f,-.77f);path.lineTo(.52f,-.77f);path.lineTo(.94f,-.20f);path.lineTo(0,.90f);path.lineTo(-.94f,-.20f);path.close();drawPath(c,0xff7452ad);
        path.reset();path.moveTo(-.49f,-.76f);path.lineTo(.49f,-.76f);path.lineTo(.87f,-.23f);path.lineTo(-.87f,-.23f);path.close();drawPath(c,0xffe6b7ff);
        path.reset();path.moveTo(-.87f,-.23f);path.lineTo(0,.85f);path.lineTo(-.27f,-.23f);path.lineTo(-.49f,-.76f);path.close();drawPath(c,0xffa7a4f2);
        path.reset();path.moveTo(.49f,-.76f);path.lineTo(.87f,-.23f);path.lineTo(0,.85f);path.lineTo(.27f,-.23f);path.close();drawPath(c,0xffb57ddc);
        path.reset();path.moveTo(-.27f,-.23f);path.lineTo(0,-.76f);path.lineTo(.27f,-.23f);path.lineTo(0,.85f);path.close();drawPath(c,0xffa6f2ed);
        line(c,-.74f,-.26f,.74f,-.26f,.04f,0xf4ffffff);line(c,-.35f,-.56f,-.47f,-.35f,.085f,WHITE);
        twinkle(c,.80f,-.83f,.19f,0xffb0e4f1);twinkle(c,-.80f,.45f,.13f,0xffe6b7ff);c.restore();
    }

    /** Six distinct, friendly silhouettes. Unknown ids render a neutral star instead of another power. */
    public void power(Canvas c,String id,float x,float y,float r,long time,boolean still){
        String power=id==null?"":id;
        begin(c,x,y,r,time,still,power.hashCode()%19);
        oval(c,-.65f,.83f,.65f,.99f,0x18343149);
        switch(power){
            case "hint": lantern(c);break;
            case "vision": crystal(c);break;
            case "flow": wind(c);break;
            case "shield": shield(c);break;
            case "focus": glasses(c);break;
            case "trail": comet(c);break;
            default:star(c,0,0,.83f,0xffd3c3f7);face(c,0,.04f,.62f);break;
        }
        c.restore();
    }

    private void lantern(Canvas c){
        // A hanging lantern, with a bright star suspended behind the little face.
        fill(0xffba864c);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(.11f);c.drawArc(-.25f,-1.02f,.25f,-.47f,180,180,false,paint);paint.setStyle(Paint.Style.FILL);
        path.reset();path.moveTo(-.42f,-.70f);path.cubicTo(-.79f,-.39f,-.77f,.24f,-.43f,.57f);path.lineTo(.43f,.57f);path.cubicTo(.77f,.24f,.79f,-.39f,.42f,-.70f);path.close();drawPath(c,0xffe4ad53);
        path.reset();path.moveTo(-.39f,-.69f);path.cubicTo(-.66f,-.35f,-.66f,.19f,-.38f,.47f);path.lineTo(.38f,.47f);path.cubicTo(.66f,.19f,.66f,-.35f,.39f,-.69f);path.close();drawPath(c,0xffffeab1);
        star(c,0,-.26f,.37f,WHITE);face(c,0,.17f,.51f);round(c,-.47f,-.76f,.47f,-.58f,.08f,0xffb88450);round(c,-.40f,.51f,.40f,.72f,.09f,0xffb88450);line(c,-.24f,.56f,.24f,.56f,.04f,0xffffe6ab);
        twinkle(c,.87f,-.39f,.18f,0xffffd37d);twinkle(c,-.85f,.02f,.12f,0xffffd37d);
    }

    private void crystal(Canvas c){
        // A rounded crystal teardrop with an eye, distinct from the faceted currency diamond.
        path.reset();path.moveTo(0,-.98f);path.cubicTo(.26f,-.69f,.77f,-.22f,.77f,.17f);path.cubicTo(.77f,1.07f,-.77f,1.07f,-.77f,.17f);path.cubicTo(-.77f,-.22f,-.26f,-.69f,0,-.98f);path.close();drawPath(c,0xff9b7dc9);
        path.reset();path.moveTo(0,-.90f);path.cubicTo(.24f,-.62f,.69f,-.18f,.69f,.16f);path.cubicTo(.69f,.95f,-.69f,.95f,-.69f,.16f);path.cubicTo(-.69f,-.18f,-.24f,-.62f,0,-.90f);path.close();drawPath(c,0xffcbb8f7);
        oval(c,-.53f,-.10f,.53f,.39f,WHITE);disk(c,0,.145f,.22f,0xff6a5c9d);disk(c,0,.145f,.12f,INK);disk(c,-.055f,.075f,.057f,WHITE);
        path.reset();path.moveTo(-.45f,-.24f);path.quadTo(-.38f,-.45f,-.20f,-.61f);stroke(c,.075f,0xd9ffffff);
        oval(c,-.29f,.49f,.29f,.61f,0xffe5cdff);twinkle(c,.87f,-.65f,.18f,0xffc2b4ee);twinkle(c,-.82f,.41f,.12f,0xffead7ff);
    }

    private void wind(Canvas c){
        // Cloud wings and three wind trails communicate movement, without a generic lightning bolt.
        path.reset();path.moveTo(-.58f,.40f);path.cubicTo(-1.02f,.37f,-1.03f,-.28f,-.54f,-.29f);path.cubicTo(-.54f,-.95f,.36f,-1.02f,.48f,-.33f);path.cubicTo(1.06f,-.40f,1.16f,.32f,.69f,.42f);path.close();drawPath(c,0xffe6ab83);
        path.reset();path.moveTo(-.60f,.32f);path.cubicTo(-.96f,.30f,-.96f,-.22f,-.49f,-.23f);path.cubicTo(-.49f,-.86f,.33f,-.90f,.43f,-.26f);path.cubicTo(.97f,-.33f,1.06f,.25f,.65f,.34f);path.close();drawPath(c,0xfffff7ee);
        face(c,.08f,.03f,.61f);oval(c,-.22f,-.61f,.12f,-.47f,WHITE);
        path.reset();path.moveTo(-1.0f,.55f);path.lineTo(-.17f,.55f);path.cubicTo(.14f,.55f,.11f,.81f,-.07f,.81f);stroke(c,.12f,0xffefb087);
        line(c,-.81f,.79f,-.42f,.79f,.10f,0xffffcea6);line(c,-.97f,.98f,-.13f,.98f,.065f,0xffffcea6);twinkle(c,.79f,-.72f,.18f,0xffffcb8b);
    }

    private void shield(Canvas c){
        // Ears belong to the shield's silhouette, so this is recognizable even without its hue.
        oval(c,-.69f,-.93f,-.22f,-.42f,0xffb6e8ee);oval(c,.22f,-.93f,.69f,-.42f,0xffb6e8ee);
        oval(c,-.56f,-.82f,-.32f,-.48f,0xffe7fbff);oval(c,.32f,-.82f,.56f,-.48f,0xffe7fbff);
        path.reset();path.moveTo(0,-.78f);path.cubicTo(.26f,-.60f,.54f,-.55f,.78f,-.52f);path.lineTo(.63f,.31f);path.quadTo(.48f,.74f,0,.98f);path.quadTo(-.48f,.74f,-.63f,.31f);path.lineTo(-.78f,-.52f);path.quadTo(-.32f,-.57f,0,-.78f);path.close();drawPath(c,0xff63afc0);
        c.save();c.scale(.90f,.90f);drawPath(c,0xffa6e8ef);c.restore();star(c,0,-.25f,.30f,WHITE);face(c,0,.27f,.58f);
        line(c,-.50f,-.39f,-.31f,-.44f,.07f,WHITE);twinkle(c,.89f,.25f,.13f,0xff96d6e0);
    }

    private void glasses(Canvas c){
        // A tiny leaf on the bridge gives these mint spectacles their own little personality.
        disk(c,-.41f,.04f,.48f,0xff569b8c);disk(c,.41f,.04f,.48f,0xff569b8c);disk(c,-.41f,-.02f,.46f,0xffa8e8ce);disk(c,.41f,-.02f,.46f,0xffa8e8ce);
        disk(c,-.41f,-.02f,.32f,0xfff3fffb);disk(c,.41f,-.02f,.32f,0xfff3fffb);line(c,-.07f,-.07f,.07f,-.07f,.13f,0xff569b8c);
        line(c,-.85f,-.05f,-1.0f,-.29f,.11f,0xff569b8c);line(c,.85f,-.05f,1.0f,-.29f,.11f,0xff569b8c);
        disk(c,-.39f,.01f,.135f,INK);disk(c,.39f,.01f,.135f,INK);disk(c,-.44f,-.05f,.047f,WHITE);disk(c,.34f,-.05f,.047f,WHITE);
        leaf(c,0,-.65f,.42f,0xff91d5b6);oval(c,-.18f,.46f,.18f,.59f,0xfff2b5c3);twinkle(c,.88f,.59f,.12f,0xffb4e8ca);
    }

    private void comet(Canvas c){
        path.reset();path.moveTo(-.99f,.46f);path.cubicTo(-.53f,.44f,-.20f,-.29f,.12f,-.37f);path.lineTo(.48f,.08f);path.cubicTo(-.07f,.26f,-.30f,.89f,-.79f,.89f);path.close();drawPath(c,0xfff9c5df);
        line(c,-.88f,.26f,-.28f,-.34f,.13f,0xffffe3ee);line(c,-.42f,.94f,.08f,.46f,.10f,0xffe99fc6);
        star(c,.23f,-.25f,.82f,0xffb9689c);star(c,.23f,-.31f,.77f,0xffffb9d5);face(c,.23f,-.19f,.55f);oval(c,-.10f,-.69f,.17f,-.57f,WHITE);
        twinkle(c,-.80f,-.65f,.16f,0xffeeb2d9);twinkle(c,.94f,.53f,.12f,0xffffd0e0);
    }

    /** A candy capsule; decorative only. Odds, ownership and the purchase gate remain caller-owned. */
    public void capsule(Canvas c,float x,float y,float r,long time,boolean still){
        begin(c,x,y,r,time,still,2.8f);
        oval(c,-.68f,.84f,.68f,.99f,0x18343149);
        path.reset();path.moveTo(0,-.95f);path.cubicTo(.49f,-.95f,.82f,-.26f,.82f,.22f);path.cubicTo(.82f,1.16f,-.82f,1.16f,-.82f,.22f);path.cubicTo(-.82f,-.26f,-.49f,-.95f,0,-.95f);path.close();drawPath(c,0xff9b83c3);
        path.reset();path.moveTo(0,-.90f);path.cubicTo(.44f,-.90f,.75f,-.25f,.75f,.19f);path.cubicTo(.75f,1.05f,-.75f,1.05f,-.75f,.19f);path.cubicTo(-.75f,-.25f,-.44f,-.90f,0,-.90f);path.close();drawPath(c,0xffe5ddfb);
        star(c,0,-.13f,.40f,0xffffd685);star(c,-.42f,.26f,.16f,0xffb0e2cc);star(c,.45f,.28f,.15f,0xfff5aec9);
        path.reset();path.moveTo(-.72f,.19f);path.quadTo(0,.48f,.72f,.19f);path.cubicTo(.63f,1.02f,-.63f,1.02f,-.72f,.19f);path.close();drawPath(c,0xffc1a6e8);
        face(c,0,.55f,.48f);path.reset();path.moveTo(-.46f,-.31f);path.quadTo(-.40f,-.57f,-.21f,-.68f);stroke(c,.08f,WHITE);
        twinkle(c,.91f,-.57f,.16f,0xffd6c1f0);twinkle(c,-.86f,.05f,.10f,0xffe8c8e3);c.restore();
    }

    /** Bounded, open flowing decoration behind a shop header; deliberately no enclosing box. */
    public void heroAccent(Canvas c,RectF box,int tone,long time,boolean still){
        float w=box.width(),h=box.height(),x=box.left,y=box.top;
        if(w<=0||h<=0)return;
        int color=TINTS[Math.floorMod(tone,4)];
        path.reset();path.moveTo(x+w*.48f,y);path.cubicTo(x+w*.82f,y+h*.03f,x+w*.61f,y+h*.55f,x+w,y+h*.48f);path.lineTo(x+w,y+h*.77f);path.cubicTo(x+w*.63f,y+h*.92f,x+w*.89f,y+h*.17f,x+w*.48f,y+h*.13f);path.close();drawPath(c,color);
        float drift=still?0:(float)Math.sin(time*.0016)*h*.015f;
        twinkle(c,x+w*.88f,y+h*.17f+drift,Math.min(w,h)*.065f,WHITE);
        twinkle(c,x+w*.66f,y+h*.65f-drift,Math.min(w,h)*.035f,0xb3ffffff);
        disk(c,x+w*.95f,y+h*.91f,Math.min(w,h)*.025f,color);
    }

    /** Small celebratory swash under a section label; no text, timer or artificial scarcity. */
    public void ribbon(Canvas c,RectF box,int tone){
        if(box.width()<=0||box.height()<=0)return;
        float l=box.left,t=box.top,w=box.width(),h=box.height();int color=TINTS[Math.floorMod(tone,4)];
        path.reset();path.moveTo(l,t+h*.18f);path.quadTo(l+w*.50f,t-h*.10f,l+w,t+h*.17f);path.lineTo(l+w*.95f,t+h*.52f);path.lineTo(l+w,t+h*.91f);path.quadTo(l+w*.51f,t+h*.69f,l,t+h);path.lineTo(l+w*.035f,t+h*.55f);path.close();drawPath(c,color);
    }
}
