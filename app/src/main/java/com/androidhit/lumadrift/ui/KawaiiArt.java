package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

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

    /** Twenty-four recognizable silhouettes: six per slot, aligned with the public cosmetic catalog order. */
    public void cosmetic(Canvas c,String slot,int variant,float x,float y,float r,int color){
        c.save();c.translate(x,y);c.scale(r,r);int v=Math.floorMod(variant,6),edge=mix(color,INK,.25f),light=mix(color,WHITE,.5f);
        if(slot.equals("HAT")){
            if(v==0){ // Dew beret and leaf.
                oval(c,-.91f,.10f,.91f,.42f,edge);oval(c,-.78f,-.57f,.72f,.29f,color);oval(c,-.55f,-.48f,.32f,-.26f,light);
                line(c,.12f,-.57f,.23f,-.83f,edge,.08f);oval(c,.15f,-.84f,.57f,-.56f,0xff78bf9d);
            }else if(v==1){ // Moon sleeping cap.
                path.reset();path.moveTo(-.66f,.19f);path.quadTo(-.57f,-1.04f,.22f,-.87f);path.quadTo(.67f,-.8f,.72f,-.30f);path.quadTo(.30f,-.55f,.24f,.17f);path.close();paint(color);c.drawPath(path,p);circle(c,.72f,-.30f,.22f,WHITE);rounded(c,-.72f,.09f,1.38f,.30f,.12f,light);star(c,-.21f,-.40f,.19f,WHITE);
            }else if(v==2){ // Star wizard hat.
                path.reset();path.moveTo(-.59f,.17f);path.quadTo(-.32f,-.65f,.02f,-1.03f);path.quadTo(.22f,-.55f,.62f,.17f);path.close();paint(color);c.drawPath(path,p);
                oval(c,-.94f,.03f,.94f,.44f,edge);oval(c,-.92f,-.01f,.92f,.35f,color);star(c,0,-.42f,.26f,0xffffe7a5);star(c,.35f,-.14f,.08f,WHITE);
            }else if(v==3){ // Pumpkin cap with a green stem.
                oval(c,-.78f,-.57f,.78f,.30f,edge);oval(c,-.71f,-.62f,.71f,.22f,color);oval(c,-.30f,-.63f,.3f,.22f,light);
                line(c,0,-.61f,.07f,-.89f,0xff5f977b,.15f);oval(c,.04f,-.89f,.43f,-.67f,0xff91cda2);oval(c,-.88f,.13f,.88f,.36f,color);
            }else if(v==4){ // Cupid halo and heart.
                paint(edge);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.15f);c.drawOval(-.72f,-.55f,.72f,-.04f,p);p.setStyle(Paint.Style.FILL);
                paint(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.12f);c.drawOval(-.72f,-.60f,.72f,-.09f,p);p.setStyle(Paint.Style.FILL);
                heart(c,0,-.60f,.39f,color);heart(c,0,-.65f,.22f,WHITE);
            }else{ // Aurora tiara: tall central gem and two rounded side gems.
                rounded(c,-.75f,.03f,1.5f,.24f,.10f,edge);rounded(c,-.75f,-.02f,1.5f,.24f,.10f,color);
                for(int s:SIDES){path.reset();path.moveTo(s*.18f,.1f);path.lineTo(s*.63f,-.58f);path.quadTo(s*.88f,-.14f,s*.70f,.13f);path.close();paint(light);c.drawPath(path,p);}
                path.reset();path.moveTo(0,-.99f);path.lineTo(.29f,-.42f);path.lineTo(0,.09f);path.lineTo(-.29f,-.42f);path.close();paint(color);c.drawPath(path,p);line(c,-.06f,-.65f,-.13f,-.42f,WHITE,.065f);
            }
        }else if(slot.equals("DRESS")){
            float hem=v==1||v==5?.86f:.71f;
            path.reset();path.moveTo(-.4f,-.70f);path.quadTo(0,-.49f,.40f,-.70f);path.lineTo(.78f,hem*.75f);
            if(v==4){for(int i=0;i<5;i++)path.lineTo(.78f-i*.39f,hem+(i%2==0?0:.16f));}
            else path.quadTo(0,hem+.23f,-.78f,hem*.75f);
            path.lineTo(-.4f,-.70f);path.close();c.save();c.translate(0,.035f);paint(edge);c.drawPath(path,p);c.restore();paint(color);c.drawPath(path,p);
            line(c,-.47f,-.24f,.47f,-.24f,light,.12f);line(c,-.22f,.0f,-.32f,hem*.57f,light,.045f);line(c,.22f,.0f,.32f,hem*.57f,light,.045f);
            if(v==0){oval(c,-.20f,-.36f,.20f,-.09f,WHITE);circle(c,0,-.23f,.06f,0xffffda92);}
            else if(v==1){oval(c,-.17f,-.45f,.17f,-.07f,WHITE);oval(c,-.02f,-.49f,.22f,-.17f,color);for(int i=-1;i<=1;i++)star(c,i*.42f,.40f,.075f,WHITE);}
            else if(v==2){star(c,0,-.23f,.23f,0xffffe4a1);for(int i=-1;i<=1;i++)star(c,i*.40f,.42f,.12f,WHITE);}
            else if(v==3){heart(c,0,-.24f,.22f,WHITE);for(int s:SIDES)heart(c,s*.38f,.35f,.12f,light);}
            else if(v==4){rounded(c,-.17f,-.36f,.34f,.24f,.04f,0xffffe4a1);rounded(c,-.08f,-.31f,.16f,.14f,.02f,edge);star(c,.41f,.42f,.15f,WHITE);}
            else{for(int i=-1;i<=1;i++)star(c,i*.37f,.35f+Math.abs(i)*.15f,.12f,WHITE);line(c,-.44f,.69f,.44f,.69f,light,.1f);}
        }else if(slot.equals("SHOES")){
            for(int s:SIDES){
                c.save();c.translate(s*.48f,0);float tall=v==1||v==3||v==5?.34f:0;
                rounded(c,-.28f,-.28f-tall,.56f,.60f+tall,.20f,edge);rounded(c,-.30f,-.34f-tall,.60f,.60f+tall,.20f,color);
                oval(c,-.34f,-.05f,.38f,.38f,color);rounded(c,-.37f,.20f,.77f,.19f,.085f,WHITE);oval(c,-.18f,-.24f-tall,.08f,-.10f-tall,light);
                if(v==0)line(c,-.1f,.02f,.16f,.02f,WHITE,.07f);
                else if(v==1){rounded(c,-.30f,-.40f-tall,.60f,.19f,.075f,WHITE);star(c,.03f,-.17f,.10f,WHITE);}
                else if(v==2)star(c,.03f,-.05f,.16f,0xffffecac);
                else if(v==3){line(c,-.08f,-.25f,.08f,-.25f,edge,.07f);line(c,-.08f,-.1f,.08f,-.1f,edge,.07f);circle(c,.25f,.02f,.12f,0xff7ab997);}
                else if(v==4)heart(c,0,-.09f,.16f,WHITE);
                else{star(c,0,-.27f,.13f,WHITE);line(c,-.28f,.10f,.3f,.10f,light,.06f);}
                c.restore();
            }
        }else{
            for(int s:SIDES){
                c.save();c.translate(s*.66f,0);float tall=v>=4?.25f:0;
                oval(c,-.31f,-.47f,.31f,.29f,color);oval(c,-.25f,-.42f,.15f,-.24f,light);circle(c,-s*.25f,-.04f,.18f,edge);circle(c,-s*.26f,-.08f,.17f,color);
                rounded(c,-.25f,.13f,.50f,.20f+tall,.075f,edge);rounded(c,-.25f,.10f,.50f,.17f+tall,.075f,v==1?WHITE:light);
                if(v==0)circle(c,0,-.1f,.06f,WHITE);
                else if(v==1){circle(c,0,-.15f,.13f,WHITE);circle(c,.07f,-.19f,.11f,color);}
                else if(v==2)star(c,0,-.10f,.16f,0xffffe4a1);
                else if(v==3)heart(c,0,-.10f,.16f,WHITE);
                else if(v==4){star(c,0,-.12f,.13f,WHITE);line(c,-.13f,.22f,.13f,.22f,color,.06f);}
                else{star(c,0,-.12f,.14f,WHITE);circle(c,0,.24f,.06f,WHITE);}
                c.restore();
            }
        }
        c.restore();
    }
}
