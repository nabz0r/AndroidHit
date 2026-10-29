package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import com.androidhit.lumadrift.data.CosmeticCatalog;

/** Original plush inventory illustrations. Reuses its drawing objects and does not allocate bitmaps or shaders per frame. */
public final class KawaiiArt {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private static final int INK=0xff343149,WHITE=0xfffffcff;
    private static final int[] SIDES={-1,1};
    private void paint(int color){p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);}
    private void circle(Canvas c,float x,float y,float r,int color){paint(color);c.drawCircle(x,y,r,p);}
    private void line(Canvas c,float x,float y,float xx,float yy,int color,float width){paint(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,p);}
    private void oval(Canvas c,float x,float y,float xx,float yy,int color){paint(color);c.drawOval(x,y,xx,yy,p);}
    private void rounded(Canvas c,float x,float y,float w,float h,float r,int color){paint(color);c.drawRoundRect(x,y,x+w,y+h,r,r,p);}
    private static int mix(int a,int b,float k){return Color.rgb(Math.round(Color.red(a)*(1-k)+Color.red(b)*k),Math.round(Color.green(a)*(1-k)+Color.green(b)*k),Math.round(Color.blue(a)*(1-k)+Color.blue(b)*k));}
    private void star(Canvas c,float x,float y,float r,int color){path.reset();for(int i=0;i<10;i++){double a=i*Math.PI/5-Math.PI/2;float rr=i%2==0?r:r*.48f;float xx=x+(float)Math.cos(a)*rr,yy=y+(float)Math.sin(a)*rr;if(i==0)path.moveTo(xx,yy);else path.lineTo(xx,yy);}path.close();paint(color);c.drawPath(path,p);}
    private void heart(Canvas c,float x,float y,float r,int color){path.reset();path.moveTo(x,y+r*.65f);path.cubicTo(x-r*1.3f,y-r*.12f,x-r*.7f,y-r*1.18f,x,y-r*.48f);path.cubicTo(x+r*.7f,y-r*1.18f,x+r*1.3f,y-r*.12f,x,y+r*.65f);path.close();paint(color);c.drawPath(path,p);}
    private void face(Canvas c,float x,float y,float r){
        oval(c,x-r*.36f,y-r*.18f,x-r*.20f,y+r*.09f,INK);oval(c,x+r*.20f,y-r*.18f,x+r*.36f,y+r*.09f,INK);
        circle(c,x-r*.31f,y-r*.12f,r*.04f,WHITE);circle(c,x+r*.25f,y-r*.12f,r*.04f,WHITE);
        oval(c,x-r*.57f,y+r*.04f,x-r*.31f,y+r*.20f,0xfff7a9b7);oval(c,x+r*.31f,y+r*.04f,x+r*.57f,y+r*.20f,0xfff7a9b7);
        paint(INK);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.065f);p.setStrokeCap(Paint.Cap.ROUND);c.drawArc(x-r*.13f,y+r*.03f,x+r*.13f,y+r*.29f,5,170,false,p);p.setStyle(Paint.Style.FILL);
    }
    public int powerColor(String id){switch(id){case "hint":return 0xffffdb8f;case "vision":return 0xffc7b5f5;case "shield":return 0xff9fdff0;case "focus":return 0xffa0e9ce;case "trail":return 0xffffb6d0;default:return 0xffffc499;}}

    /** Each power has a different silhouette and symbol; small icons never depend only on their color. */
    public void power(Canvas c,String id,float x,float y,float r,long time,boolean still){
        int col=powerColor(id),edge=mix(col,INK,.27f);float bob=still?0:(float)Math.sin(time*.003+id.hashCode())*r*.035f;
        c.save();c.translate(x,y+bob);c.scale(r,r);oval(c,-.71f,.83f,.71f,1.04f,0x29343149);
        if(id.equals("hint")){
            // A little star lantern: handle, glowing globe and brass base.
            paint(edge);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.11f);c.drawArc(-.25f,-1.02f,.25f,-.52f,180,180,false,p);p.setStyle(Paint.Style.FILL);
            rounded(c,-.65f,-.73f,1.30f,1.32f,.42f,edge);rounded(c,-.62f,-.79f,1.24f,1.27f,.42f,col);
            oval(c,-.47f,-.65f,.38f,-.28f,0x98ffffff);star(c,0,-.13f,.45f,0xfffff6ce);face(c,0,.14f,.45f);
            rounded(c,-.38f,.53f,.76f,.25f,.09f,0xffb48d66);line(c,-.21f,.58f,.21f,.58f,WHITE,.04f);
            star(c,.88f,-.7f,.18f,0xffffecb0);star(c,-.88f,-.38f,.12f,0xffffecb0);
        }else if(id.equals("vision")){
            // Soft crystal with an unmistakable eye.
            path.reset();path.moveTo(0,-.97f);path.lineTo(.69f,-.45f);path.quadTo(.9f,.35f,.32f,.88f);path.quadTo(0,1.04f,-.32f,.88f);path.quadTo(-.9f,.35f,-.69f,-.45f);path.close();
            c.save();c.translate(0,.05f);paint(edge);c.drawPath(path,p);c.restore();paint(col);c.drawPath(path,p);
            line(c,-.45f,-.42f,-.1f,-.71f,0xaaffffff,.1f);oval(c,-.61f,-.25f,.61f,.42f,WHITE);circle(c,0,.08f,.29f,0xff8269b5);circle(c,0,.08f,.15f,INK);circle(c,-.08f,-.015f,.065f,WHITE);
            star(c,.83f,-.78f,.17f,0xfff9e5ff);
        }else if(id.equals("shield")){
            path.reset();path.moveTo(0,-.92f);path.cubicTo(.34f,-.73f,.71f,-.65f,.75f,-.55f);path.lineTo(.60f,.30f);path.quadTo(.46f,.75f,0,1);path.quadTo(-.46f,.75f,-.6f,.30f);path.lineTo(-.75f,-.55f);path.quadTo(-.34f,-.73f,0,-.92f);path.close();
            c.save();c.translate(0,.05f);paint(edge);c.drawPath(path,p);c.restore();paint(col);c.drawPath(path,p);
            paint(0xd9ffffff);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.08f);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
            line(c,-.45f,-.48f,-.2f,-.57f,0xaaffffff,.09f);star(c,0,-.07f,.36f,WHITE);face(c,0,.40f,.35f);
        }else if(id.equals("focus")){
            // Round spectacles with visible lenses and a focus reticle.
            for(int s:SIDES){circle(c,s*.40f,.0f,.49f,edge);circle(c,s*.4f,-.04f,.46f,col);circle(c,s*.4f,-.04f,.32f,WHITE);circle(c,s*.4f,-.04f,.17f,0xff547d76);circle(c,s*.46f,-.12f,.055f,WHITE);}
            line(c,-.1f,-.08f,.1f,-.08f,edge,.14f);line(c,-.87f,-.13f,-1.02f,-.31f,edge,.12f);line(c,.87f,-.13f,1.02f,-.31f,edge,.12f);
            line(c,0,-.84f,0,-.62f,col,.1f);line(c,-.12f,-.74f,.12f,-.74f,col,.1f);
            oval(c,-.53f,.48f,.53f,.65f,0xffffc6ce);
        }else if(id.equals("trail")){
            // A comet leaves a trail; the large star is also the hint target.
            line(c,-.85f,.70f,-.05f,-.10f,mix(col,WHITE,.3f),.34f);line(c,-.99f,.25f,-.28f,-.37f,col,.18f);line(c,-.4f,.96f,.18f,.35f,col,.15f);
            star(c,.22f,-.22f,.83f,edge);star(c,.22f,-.28f,.78f,col);star(c,.02f,-.57f,.13f,WHITE);face(c,.22f,-.11f,.49f);star(c,-.83f,-.66f,.14f,WHITE);
        }else{
            // A warm wind-cloud moves several pieces in a row.
            oval(c,-.7f,-.27f,.04f,.50f,edge);oval(c,-.41f,-.64f,.53f,.47f,edge);oval(c,.11f,-.26f,.92f,.50f,edge);
            rounded(c,-.58f,-.03f,1.38f,.56f,.25f,edge);
            oval(c,-.7f,-.32f,.04f,.43f,WHITE);oval(c,-.41f,-.70f,.53f,.42f,WHITE);oval(c,.11f,-.32f,.92f,.43f,WHITE);rounded(c,-.58f,-.09f,1.38f,.56f,.25f,WHITE);
            face(c,.12f,.04f,.48f);line(c,-1,.56f,-.36f,.56f,col,.14f);line(c,-.86f,.80f,-.12f,.80f,col,.11f);star(c,.74f,-.79f,.24f,col);
        }
        c.restore();
    }

    /** Native catalogue order stays stable while every preview uses its authored raster garment. */
    public void cosmetic(Canvas c,String slot,int variant,float x,float y,float r,int color){
        if(c==null||slot==null||!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(r)||r<=0)return;
        CosmeticCatalog.Slot selected;
        try{selected=CosmeticCatalog.Slot.valueOf(slot);}catch(IllegalArgumentException unknown){return;}
        int v=Math.floorMod(variant,6),index=0;
        for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS){
            if(item.slot!=selected)continue;
            if(index++==v){CharacterSprites.drawGarmentPreview(c,item,x-r*1.03f,y-r*1.04f,x+r*1.03f,y+r*1.04f);return;}
        }
    }
}
