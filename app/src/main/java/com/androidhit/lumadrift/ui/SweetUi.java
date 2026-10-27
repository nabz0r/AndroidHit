package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.androidhit.lumadrift.engine.Direction;

/** Original soft-volume illustration primitives. Labels and touch/accessibility nodes belong to the view. */
public final class SweetUi {
    public static final int PEACH = 0, MINT = 1, LILAC = 2, CREAM = 3;
    public static final int HAPPY = 0, THINKING = 1, CELEBRATE = 2;
    public static final int INK = 0xff343149, MUTED = 0xff625d78;
    private static final int WHITE = 0xfffffcff;
    private static final int[] LIGHT = {0xffffebe4,0xffe1fff2,0xfff0eaff,0xfffff7dc};
    private static final int[] MAIN = {0xffffbbad,0xffa6e8cf,0xffc7b3f5,0xffffdf94};
    private static final int[] EDGE = {0xffd28581,0xff6baa97,0xff9780c4,0xffc2a164};
    private static final int[] DIRECTION_COLORS = {0xff91e5cc,0xffffb398,0xffceb3f7,0xffa4cefa};
    private static final int[] DARK = {0xff81394f,0xff315f58,0xff56427a,0xff76602d};
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();

    public static int fill(int tone) { return MAIN[Math.floorMod(tone,4)]; }
    public static int light(int tone) { return LIGHT[Math.floorMod(tone,4)]; }
    public static int ink(int tone) { return DARK[Math.floorMod(tone,4)]; }
    private void paint(int color) { p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL); }
    private void round(Canvas c,float l,float t,float r,float b,float radius,int color) {
        paint(color);c.drawRoundRect(l,t,r,b,radius,radius,p);
    }
    private void disk(Canvas c,float x,float y,float r,int color) { paint(color);c.drawCircle(x,y,r,p); }
    private void ellipse(Canvas c,float l,float t,float r,float b,int color) {paint(color);c.drawOval(l,t,r,b,p);}
    private void line(Canvas c,float x,float y,float xx,float yy,int color,float width) {
        paint(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,p);
    }
    private void outline(Canvas c,RectF box,float radius,int color,float width) {
        paint(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);c.drawRoundRect(box,radius,radius,p);p.setStyle(Paint.Style.FILL);
    }
    private static int alpha(int color,int value) {return (color&0xffffff)|((value&255)<<24);}
    private static int mix(int color,int target,float t) {
        return Color.rgb(Math.round(Color.red(color)*(1-t)+Color.red(target)*t),
                Math.round(Color.green(color)*(1-t)+Color.green(target)*t),
                Math.round(Color.blue(color)*(1-t)+Color.blue(target)*t));
    }

    /** A readable cream/pastel paper surface with a real lower edge and a restrained top highlight. */
    public void surface(Canvas c,RectF box,int tone,boolean elevated) {
        int i=Math.floorMod(tone,4);float rad=Math.min(25,box.height()*.24f);
        if(elevated) {
            round(c,box.left,box.top+5,box.right,box.bottom+5,rad,0x1f3a3159);
            round(c,box.left+2,box.top+2,box.right-2,box.bottom+3,rad,alpha(EDGE[i],165));
        }
        round(c,box.left,box.top,box.right,box.bottom,rad,LIGHT[i]);
        rect.set(box.left+.7f,box.top+.7f,box.right-.7f,box.bottom-.7f);outline(c,rect,rad,0xcfffffff,1.4f);
        if(box.width()>80&&box.height()>35) {
            line(c,box.left+rad,box.top+5,box.right-rad,box.top+5,0xadffffff,1.6f);
        }
    }

    /** The caller draws a dark label with ink(tone). Reserve at least 48 logical units for a touch target. */
    public void button(Canvas c,RectF box,int tone,boolean pressed) {
        int i=Math.floorMod(tone,4);float rad=Math.min(22,box.height()*.4f),drop=pressed?1:4;
        round(c,box.left,box.top+drop,box.right,box.bottom+drop,rad,EDGE[i]);
        round(c,box.left,box.top,box.right,box.bottom,rad,pressed?mix(MAIN[i],EDGE[i],.12f):MAIN[i]);
        rect.set(box.left+1,box.top+1,box.right-1,box.bottom-1);outline(c,rect,rad,0xcaffffff,1.5f);
        round(c,box.left+7,box.top+4,box.right-7,box.top+box.height()*.38f,rad*.6f,0x50ffffff);
        disk(c,box.right-13,box.top+11,2.2f,0x98ffffff);
    }

    /** Progress always represents measured completion; no fabricated near-miss or changing goal. */
    public void progress(Canvas c,RectF box,float amount,int tone) {
        float value=Float.isNaN(amount)?0:Math.max(0,Math.min(1,amount)),rad=box.height()/2;
        round(c,box.left,box.top,box.right,box.bottom,rad,0x20343149);
        round(c,box.left+1,box.top+1,box.right-1,box.bottom-1,Math.max(1,rad-1),0x70ffffff);
        float width=(box.width()-2)*value;
        if(width>0) {
            c.save();c.clipRect(box.left+1,box.top+1,box.left+1+width,box.bottom-1);
            round(c,box.left+1,box.top+1,box.right-1,box.bottom-1,Math.max(1,rad-1),fill(tone));
            line(c,box.left+rad,box.top+box.height()*.3f,box.right-rad,box.top+box.height()*.3f,0x9cffffff,Math.max(1,box.height()*.14f));c.restore();
        }
    }

    public void stepChip(Canvas c,float x,float y,float r,boolean active,boolean done) {
        disk(c,x,y+2,r,0x23343149);disk(c,x,y,r,done?MAIN[MINT]:active?MAIN[PEACH]:0xffe7e2ee);
        disk(c,x-r*.25f,y-r*.4f,r*.2f,0x77ffffff);
        if(done) {line(c,x-r*.42f,y,x-r*.08f,y+r*.31f,DARK[MINT],r*.16f);line(c,x-r*.08f,y+r*.31f,x+r*.48f,y-r*.31f,DARK[MINT],r*.16f);}
        else if(active)disk(c,x,y,r*.24f,DARK[PEACH]);
    }

    /** A marshmallow star, drawn in its own coordinate system; animation stops completely in reduced motion. */
    public void mascot(Canvas c,float x,float y,float r,long time,boolean still,int mood) {
        float bob=still?0:(float)Math.sin(time*.0027)*r*.045f;
        c.save();c.translate(x,y+bob);c.scale(r,r);
        ellipse(c,-.71f,.93f,.71f,1.09f,0x223d3558);
        float sway=still?0:(float)Math.sin(time*.0027)*3;
        c.rotate(sway);
        path.reset();path.moveTo(0,-1.03f);
        path.cubicTo(.24f,-1.04f,.30f,-.64f,.51f,-.55f);
        path.cubicTo(.71f,-.49f,1.06f,-.6f,1.12f,-.34f);
        path.cubicTo(1.17f,-.13f,.81f,.1f,.79f,.32f);
        path.cubicTo(.79f,.57f,.99f,.83f,.78f,.98f);
        path.cubicTo(.57f,1.12f,.22f,.83f,0,.84f);
        path.cubicTo(-.25f,.84f,-.57f,1.12f,-.78f,.98f);
        path.cubicTo(-.99f,.83f,-.79f,.57f,-.79f,.32f);
        path.cubicTo(-.81f,.1f,-1.17f,-.13f,-1.12f,-.34f);
        path.cubicTo(-1.06f,-.6f,-.71f,-.49f,-.51f,-.55f);
        path.cubicTo(-.3f,-.64f,-.24f,-1.04f,0,-1.03f);path.close();
        c.save();c.translate(0,.055f);paint(0xff77bfa9);c.drawPath(path,p);c.restore();
        paint(0xffb6f0d7);c.drawPath(path,p);paint(0xefffffff);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.027f);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
        ellipse(c,-.48f,-.66f,-.12f,-.5f,0x90ffffff);
        ellipse(c,-.59f,.15f,-.26f,.34f,0xfff7b1bb);ellipse(c,.26f,.15f,.59f,.34f,0xfff7b1bb);
        boolean blink=!still&&time%5200>5060;
        if(mood==CELEBRATE||blink) {
            p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(.058f);p.setColor(INK);
            c.drawArc(-.4f,-.08f,-.16f,.13f,192,156,false,p);c.drawArc(.16f,-.08f,.4f,.13f,192,156,false,p);p.setStyle(Paint.Style.FILL);
        } else {
            ellipse(c,-.36f,-.12f,-.20f,.15f,INK);ellipse(c,.20f,-.12f,.36f,.15f,INK);
            disk(c,-.30f,-.065f,.034f,WHITE);disk(c,.255f,-.065f,.034f,WHITE);
        }
        if(mood==THINKING) {ellipse(c,-.055f,.27f,.055f,.35f,INK);line(c,-.41f,-.24f,-.22f,-.28f,INK,.037f);}
        else {
            paint(INK);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.047f);p.setStrokeCap(Paint.Cap.ROUND);
            c.drawArc(-.15f,.13f,.15f,.42f,15,150,false,p);p.setStyle(Paint.Style.FILL);
            if(mood==CELEBRATE) {star(c,-1.23f,-.68f,.18f,MAIN[CREAM]);star(c,1.17f,-.91f,.22f,MAIN[PEACH]);}
        }
        c.restore();
    }

    private void star(Canvas c,float x,float y,float r,int color) {
        path.reset();for(int i=0;i<10;i++){double a=i*Math.PI/5-Math.PI/2;float s=i%2==0?r:r*.47f;float xx=x+(float)Math.cos(a)*s,yy=y+(float)Math.sin(a)*s;if(i==0)path.moveTo(xx,yy);else path.lineTo(xx,yy);}path.close();paint(color);c.drawPath(path,p);
    }

    /** A visible drag instruction, with an arrow trail and an original illustrated fingertip. */
    public void handCoach(Canvas c,float fromX,float fromY,float toX,float toY,long time,boolean still) {
        float dx=toX-fromX,dy=toY-fromY,d=(float)Math.hypot(dx,dy);
        float phase=still?.42f:(time%2100)/2100f;
        float travel=still?.42f:Math.max(0,Math.min(1,(phase-.14f)/.62f));travel=travel*travel*(3-2*travel);
        float x=fromX+dx*travel,y=fromY+dy*travel;
        line(c,fromX,fromY,toX,toY,0xaaffffff,7);
        line(c,fromX,fromY,toX,toY,0xffa7769c,2.3f);
        if(d>1) {
            float ux=dx/d,uy=dy/d;
            line(c,toX,toY,toX-ux*10-uy*5,toY-uy*10+ux*5,0xffa7769c,2.5f);
            line(c,toX,toY,toX-ux*10+uy*5,toY-uy*10-ux*5,0xffa7769c,2.5f);
        }
        float pulse=still?0:(float)Math.sin(phase*Math.PI);
        paint(alpha(0xffffd989,Math.round(135-pulse*50)));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.2f);c.drawCircle(x,y,11+pulse*5,p);p.setStyle(Paint.Style.FILL);
        c.save();c.translate(x,y);c.rotate(-17);
        path.reset();path.moveTo(-2,1);path.lineTo(-2,-14);path.cubicTo(-2,-20,5,-20,5,-14);path.lineTo(5,-5);
        path.cubicTo(8,-8,11,-5,11,-2);path.cubicTo(15,-4,18,-.5f,17,3);
        path.cubicTo(21,3,23,7,21,11);path.lineTo(18,20);path.cubicTo(15,26,4,26,0,20);
        path.lineTo(-8,10);path.cubicTo(-12,5,-6,1,-2,6);path.lineTo(-2,1);path.close();
        c.save();c.translate(1.5f,2.5f);paint(0x3d4f365f);c.drawPath(path,p);c.restore();
        paint(0xffffe9dc);c.drawPath(path,p);paint(0xff8f697d);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setStrokeJoin(Paint.Join.ROUND);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
        line(c,5,-3,5,7,0xffe6b9ac,1);line(c,11,0,11,9,0xffe6b9ac,1);line(c,17,5,16,11,0xffe6b9ac,1);
        round(c,-1,19,18,25,3,0xffc5b4ed);line(c,3,21,14,21,0xafffffff,1);c.restore();
    }

    /** Soft plush puzzle piece. The arrow is the primary signal; key and switch are secondary corner badges. */
    public void tile(Canvas c,RectF box,Direction direction,int bodyColor,boolean isSwitch,
                     boolean isKey,boolean selected,long time,boolean still) {
        float size=Math.min(box.width(),box.height()),radius=size*.27f;
        int edge=mix(bodyColor,INK,.29f),light=mix(bodyColor,WHITE,.58f);
        round(c,box.left,box.top+size*.065f,box.right,box.bottom+size*.065f,radius,alpha(INK,75));
        round(c,box.left,box.top+size*.025f,box.right,box.bottom+size*.025f,radius,edge);
        round(c,box.left,box.top,box.right,box.bottom,radius,bodyColor);
        rect.set(box.left+.7f,box.top+.7f,box.right-.7f,box.bottom-.7f);outline(c,rect,radius,alpha(WHITE,210),Math.max(.8f,size*.025f));
        round(c,box.left+size*.12f,box.top+size*.07f,box.right-size*.12f,box.top+size*.34f,radius*.6f,alpha(WHITE,120));
        ellipse(c,box.left+size*.14f,box.top+size*.11f,box.left+size*.26f,box.top+size*.18f,WHITE);
        c.save();c.translate(box.centerX(),box.centerY()+size*.015f);c.rotate(direction.ordinal()*90);
        float arrow=size*.27f;
        line(c,0,arrow*.58f,0,-arrow*.65f,INK,size*.105f);
        line(c,0,-arrow*.65f,-arrow*.62f,-arrow*.03f,INK,size*.105f);
        line(c,0,-arrow*.65f,arrow*.62f,-arrow*.03f,INK,size*.105f);
        c.restore();
        if(isSwitch) {
            float badge=size*.13f,bx=box.right-size*.11f,by=box.top+size*.13f;
            disk(c,bx,by,badge*1.22f,WHITE);disk(c,bx,by,badge,0xff6c5a90);
            line(c,bx-badge*.45f,by,bx+badge*.45f,by,WHITE,badge*.22f);
            disk(c,bx+badge*.15f,by,badge*.4f,light);
        }
        if(isKey)key(c,box.left+size*.10f,box.bottom-size*.16f,size*.13f,-1,time,true);
        if(selected) {
            float pulse=still?0:(float)Math.sin(time*.006)*1.5f;
            rect.set(box.left-3-pulse,box.top-3-pulse,box.right+3+pulse,box.bottom+3+pulse);
            outline(c,rect,radius+4,0xffffd583,2.4f);
        }
    }

    /** Draw under a navigation icon and label. The raised active tab is readable without a glow effect. */
    public void navCapsule(Canvas c,RectF box,int tone,boolean active) {
        float rad=Math.min(21,box.height()*.38f);
        if(active) {
            round(c,box.left,box.top+2.5f,box.right,box.bottom+2.5f,rad,EDGE[Math.floorMod(tone,4)]);
            round(c,box.left,box.top,box.right,box.bottom,rad,fill(tone));
            line(c,box.left+rad*.8f,box.top+4,box.right-rad*.8f,box.top+4,0x9affffff,1.3f);
        } else round(c,box.left,box.top,box.right,box.bottom,rad,0x50ffffff);
    }

    public void gate(Canvas c,float x,float y,float r,Direction direction,boolean open,boolean highlight,long time,boolean still) {
        gate(c,x,y,r,direction.ordinal(),open,highlight,time,still);
    }

    /** direction: UP=0, RIGHT=1, DOWN=2, LEFT=3. Open and closed differ by silhouette, never by color alone. */
    public void gate(Canvas c,float x,float y,float r,int direction,boolean open,boolean highlight,long time,boolean still) {
        c.save();c.translate(x,y);c.scale(r/24,r/24);c.rotate(Math.floorMod(direction,4)*90);
        int color=DIRECTION_COLORS[Math.floorMod(direction,4)],edge=mix(color,INK,.60f);
        if(highlight) {float pulse=still?0:(float)Math.sin(time*.006)*1.8f;paint(alpha(color,170));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawCircle(0,0,23+pulse,p);p.setStyle(Paint.Style.FILL);}
        round(c,-25,-11,25,13,8,0x30343149);round(c,-25,-13,25,10,8,color);
        line(c,-19,-9,19,-9,0xcaffffff,2);
        if(open) {
            round(c,-12,-15,12,13,4,0xfff4f0ff);
            line(c,0,6,0,-7,edge,3);line(c,0,-7,-5,-1,edge,3);line(c,0,-7,5,-1,edge,3);
        } else {
            paint(edge);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);c.drawArc(-5,-9,5,3,180,180,false,p);p.setStyle(Paint.Style.FILL);
            round(c,-8,-2,8,9,3,edge);disk(c,0,2,1.6f,WHITE);line(c,0,3,0,5,WHITE,1.5f);
        }c.restore();
    }

    public void key(Canvas c,float x,float y,float r,Direction direction,long time,boolean still) {
        key(c,x,y,r,direction==null?-1:direction.ordinal(),time,still);
    }

    /** A key remains a key at small sizes: ring, shaft and two teeth. */
    public void key(Canvas c,float x,float y,float r,int direction,long time,boolean still) {
        c.save();c.translate(x,y);c.rotate(still?-35:-35+(float)Math.sin(time*.003)*5);c.scale(r/20,r/20);
        line(c,0,-1,0,19,0xffb47c48,7);line(c,0,0,0,17,0xffffdb8c,5);
        line(c,0,11,7,11,0xffb47c48,6);line(c,0,16,5,16,0xffb47c48,6);
        line(c,0,10,6,10,0xffffdb8c,4);line(c,0,15,4,15,0xffffdb8c,4);
        disk(c,0,-8,11,0xffb47c48);disk(c,0,-9,10,0xffffdb8c);disk(c,0,-9,5,0xfff6effc);disk(c,-4,-15,1.9f,WHITE);
        if(direction>=0) {disk(c,9,17,5,DIRECTION_COLORS[Math.floorMod(direction,4)]);disk(c,9,17,2,INK);}c.restore();
    }
}
