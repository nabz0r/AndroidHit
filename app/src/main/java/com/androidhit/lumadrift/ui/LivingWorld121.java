package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Original bounded arcade scenery: ten world palettes, layered terrain and reactive currents. */
public final class LivingWorld121 {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private final RectF rect=new RectF();
    private final PulseUi124 pulse124=new PulseUi124();
    private static final int[] SKY={0xffe6f8ff,0xffe4faf0,0xffffeee0,0xffe7e8ff,0xffdff8fb,0xfffff3d7,0xffeee3ff,0xffffe5dc,0xffffe6f4,0xffe5e4ff};
    private static final int[] INK={0xff68adc1,0xff65bba4,0xffda9e79,0xff9894d3,0xff6ebdcc,0xffcfad62,0xffad83cc,0xffcc8b85,0xffc782af,0xff8d8ccb};
    private void fill(int color){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);}
    private int alpha(int color,int a){return color&0xffffff|Math.max(0,Math.min(255,a))<<24;}
    public void draw(Canvas c,float h,int level,long now,boolean still,float cleared){
        if(h<=0)return;int biome=Math.floorMod((Math.max(1,level)-1)/100,10),tone=INK[biome];
        fill(SKY[biome]);c.drawRect(0,0,390,h,p);
        float t=still?0:(now%180000)/1000f;
        // Slow parallax planes use distinct velocities; no camera movement or flashing.
        for(int layer=0;layer<3;layer++){
            float base=h*(.42f+layer*.21f),wave=8+layer*8;
            path.reset();path.moveTo(-20,h+20);path.lineTo(-20,base);
            for(int x=-20;x<=420;x+=20){float yy=base+(float)Math.sin(x*.012+layer*1.7+t*(.06+layer*.025))*wave;path.lineTo(x,yy);}
            path.lineTo(420,h+20);path.close();fill(alpha(tone,20+layer*11));c.drawPath(path,p);
        }
        // Cloud islands, swaying stems and a landmark give the world a recognisable silhouette.
        for(int i=0;i<4;i++){
            float x=((i*137+level*17+t*(2+i*.4f))%530)-70,y=h*(.12f+i*.19f);
            fill(0xb8ffffff);c.drawOval(x-39,y-8,x+39,y+10,p);c.drawCircle(x-13,y-8,17,p);c.drawCircle(x+12,y-13,22,p);
        }
        for(int side=0;side<2;side++)for(int n=0;n<5;n++){
            float x=side==0?8+n*4:382-n*4,y=h*(.39f+n*.12f),sway=still?0:(float)Math.sin(t*.8+n)*5;
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(alpha(tone,85));path.reset();path.moveTo(x,y+35);path.quadTo(x+sway,y+8,x+(side==0?14:-14)+sway,y-12);c.drawPath(path,p);
            fill(alpha(tone,90));c.drawOval(x-9+sway,y-18,x+9+sway,y-5,p);
        }
        // 18 stable seeded particles remain at the edges, away from arrow-reading space.
        for(int i=0;i<18;i++){
            float phase=(i*73+level*11)%997/997f;
            float x=i%2==0?8+phase*27:355+phase*27;
            float y=((phase*h+t*(4+i%3))%(h+50))-25;
            float r=2+i%3+(float)Math.sin(t*.7+i)*.5f;
            fill(alpha(tone,90));c.drawCircle(x,y,r,p);
            if(i%4==0){p.setStrokeWidth(1.5f);c.drawLine(x-r*2,y,x+r*2,y,p);c.drawLine(x,y-r*2,x,y+r*2,p);}
        }
        // The world opens as real pieces leave; it never changes a rule or indicates fake rewards.
        fill(alpha(0xffffffff,Math.round(Math.max(0,Math.min(1,cleared))*34)));c.drawRect(0,0,390,h,p);
    }
    public void shell(Canvas c,float x,float y,float size,int type,int layers,long now,boolean still){
        float r=size*.5f;int tone=type==1?0xff3a96c6:type==2?0xffd25f4b:0xff367dc5;
        // The arrow remains visible. A round rim and a separate element medallion identify the shell.
        fill(alpha(tone,18));c.drawCircle(x,y,r*.97f,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);p.setColor(alpha(tone,210));c.drawCircle(x,y,r*.97f,p);
        float badge=Math.max(11,size*.20f),xx=x+r*.69f,yy=y-r*.67f;
        fill(0xffffffff);c.drawCircle(xx,yy,badge,p);
        ArtLibrary art=CharacterSprites.library();String key=type==1?"ice":type==2?"fire":"water";
        if(art!=null)art.draw(c,"pulse/element/"+key,xx-badge*.82f,yy-badge*.82f,xx+badge*.82f,yy+badge*.82f,.5f,.5f,255);
        rect.set(xx+badge*.12f,yy+badge*.16f,xx+badge*1.12f,yy+badge*1.16f);
        fill(tone);c.drawOval(rect,p);pulse124.number(c,String.valueOf(layers),rect,12,10,0xffffffff);
    }
}
