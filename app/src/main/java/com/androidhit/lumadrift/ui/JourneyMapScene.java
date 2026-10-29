package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Original, deterministic sky archipelago for Journey. UI-thread drawing only;
 * owns no progress, hit regions, transactions or puzzle identities. Its clouds,
 * route lights and current-node halo all have a stationary reduced-motion pose.
 */
public final class JourneyMapScene {
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Path Q=new Path(), ROUTE=new Path(), ACTIVE=new Path();
    private static final RectF R=new RectF();
    private static final int[][] PALETTE={
        {0xffeee2ff,0xffd9e9fa,0xffba9ade,0xff8d74bf,0xffffd77d},
        {0xffd4f3eb,0xffd8e9ff,0xff9ed8bf,0xff5f9e9e,0xfff4cf7e},
        {0xffffe0d0,0xfff8d9ed,0xffedb39e,0xffbc8d9f,0xffffdd93},
        {0xffdce9ff,0xffeee3fa,0xffc0c8ef,0xff8f9fd0,0xffffd994},
        {0xffcdeef3,0xffdce1ff,0xff99d3dd,0xff678fbb,0xffd4b5fc},
        {0xffe6e7c9,0xffe4dcff,0xffbdce9c,0xff8b9a99,0xffffd37c},
        {0xffddd8fc,0xfff1d9f1,0xffbca9dd,0xff8776b3,0xffffcf79},
        {0xffffe2ba,0xfff1d9df,0xffd4bd91,0xffa48c9f,0xffffb66c},
        {0xfff6d9ed,0xffe3dcff,0xffdeb1d5,0xffa67ab6,0xffffd295},
        {0xffdce4ff,0xffebe0ff,0xffbdb6e6,0xff827db5,0xffffde9b}
    };
    private JourneyMapScene() { }

    public static int accent(int level){return PALETTE[SagaMap.biome(level)][4];}
    public static int ink(int level){return PALETTE[SagaMap.biome(level)][3];}

    public static void terrain(Canvas c,float scroll,float viewport,int first,int last,
                               int total,long time,boolean reduced){
        int[] palette=PALETTE[SagaMap.biome(SagaMap.at(scroll+viewport*.5f,total))];
        P.setStyle(Paint.Style.FILL);
        P.setShader(new LinearGradient(0,scroll,390,scroll+viewport,palette[0],palette[1],Shader.TileMode.CLAMP));
        c.drawRect(0,scroll,390,scroll+viewport,P);P.setShader(null);
        // Tall coherent island pairs; the path crosses bridges between them.
        int from=Math.max(1,first-4),to=Math.min(total,last+4);
        from=1+((from-1)/4)*4;
        for(int start=from;start<=to;start+=4){
            int biome=SagaMap.biome(start);int[] colors=PALETTE[biome];
            float anchor=SagaMap.y(start),cx=195+(float)Math.sin(start*.39)*21;
            float sway=reduced?0:(float)Math.sin(time*.00022+start*.7)*8;
            cloud(c,41+sway,anchor-59,1.10f,0x9cffffff);
            cloud(c,336-sway*.7f,anchor+231,.86f,0x96ffffff);
            island(c,cx,anchor+105,174,217,colors);
            island(c,40+(start%3)*159,anchor+295,39,23,colors);
            // A landmark sits away from the route rather than beneath a label.
            float landmarkX=SagaMap.x(start+1)<195?326:61;
            landmark(c,landmarkX,anchor+90,biome,colors);
            for(int j=0;j<4;j++){
                float x=29+(j*101+start*17)%333,y=anchor-20+j*97;
                float drift=reduced?0:(float)Math.sin(time*.0006+j+start)*5;
                sparkle(c,x+drift,y,2.6f+(j%2),0x80ffffff);
            }
        }
    }

    public static void route(Canvas c,int first,int last,int unlocked,long time,boolean reduced){
        ROUTE.reset();ACTIVE.reset();
        for(int n=first;n<=last;n++){
            float x=SagaMap.x(n),y=SagaMap.y(n);
            if(n==first){ROUTE.moveTo(x,y);ACTIVE.moveTo(x,y);}
            else{
                float px=SagaMap.x(n-1),py=SagaMap.y(n-1);
                ROUTE.cubicTo(px,py+48,x,y-48,x,y);
                if(n<=unlocked)ACTIVE.cubicTo(px,py+48,x,y-48,x,y);
            }
        }
        P.setStyle(Paint.Style.STROKE);P.setStrokeCap(Paint.Cap.ROUND);P.setStrokeJoin(Paint.Join.ROUND);
        P.setColor(0x33916bba);P.setStrokeWidth(28);c.save();c.translate(0,6);c.drawPath(ROUTE,P);c.restore();
        P.setColor(0xffb2a7d0);P.setStrokeWidth(23);c.drawPath(ROUTE,P);
        P.setColor(0xfff8f4ff);P.setStrokeWidth(16);c.drawPath(ROUTE,P);
        P.setColor(0xffefd7a1);P.setStrokeWidth(17);c.drawPath(ACTIVE,P);
        P.setColor(0xfffff2bf);P.setStrokeWidth(9);c.drawPath(ACTIVE,P);
        P.setStyle(Paint.Style.FILL);
        // Light travelling along the exact Bezier, never across a node hit area.
        if(!reduced)for(int n=first+1;n<=Math.min(last,unlocked);n++){
            float t=(time%2600)/2600f,u=1-t,px=SagaMap.x(n-1),py=SagaMap.y(n-1),x=SagaMap.x(n),y=SagaMap.y(n);
            float xx=u*u*u*px+3*u*u*t*px+3*u*t*t*x+t*t*t*x;
            float yy=u*u*u*py+3*u*u*t*(py+48)+3*u*t*t*(y-48)+t*t*t*y;
            oval(c,xx-2,yy-2,xx+2,yy+2,0xb3ffffff);
        }
    }

    /** Surface art remains inside existing +/-41 node badge and hit contracts. */
    public static void node(Canvas c,int number,boolean open,boolean current,int stars,int difficulty,long time,boolean reduced){
        float x=SagaMap.x(number),y=SagaMap.y(number);int accent=accent(number),ink=ink(number);
        oval(c,x-35,y+15,x+35,y+38,0x33907aab);
        if(current){
            float breath=reduced?0:(float)Math.sin(time*.0026)*2.5f;
            oval(c,x-41-breath,y-41-breath,x+41+breath,y+41+breath,0x40ffffff);
            P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(2);P.setColor(0x99ffffff);
            c.drawCircle(x,y,38+breath,P);P.setStyle(Paint.Style.FILL);
        }
        oval(c,x-32,y-25,x+32,y+35,open?ink:0xffa4a0b7);
        oval(c,x-32,y-31,x+32,y+29,open?(current?accent:stars>0?0xffbbe5d0:0xffeee8fb):0xffd5d3e4);
        P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(2);P.setColor(open?0xcfffffff:0x68ffffff);
        c.drawCircle(x,y-1,28,P);P.setStyle(Paint.Style.FILL);
        oval(c,x-19,y-25,x+16,y-16,open?0x61ffffff:0x32ffffff);
        if(current){
            Q.reset();Q.moveTo(x-7,y-45);Q.lineTo(x+7,y-45);Q.lineTo(x,y-36);Q.close();P.setColor(ink);c.drawPath(Q,P);
        }
    }

    private static void island(Canvas c,float x,float y,float rx,float ry,int[] colors){
        Q.reset();Q.moveTo(x-rx*.93f,y);Q.cubicTo(x-rx*.78f,y+ry*.75f,x-rx*.22f,y+ry*.81f,x,y+ry);
        Q.cubicTo(x+rx*.27f,y+ry*.67f,x+rx*.81f,y+ry*.63f,x+rx*.94f,y);Q.close();P.setColor(colors[3]);c.drawPath(Q,P);
        Q.reset();Q.moveTo(x-rx*.65f,y+15);Q.lineTo(x-rx*.26f,y+ry*.83f);Q.lineTo(x-rx*.11f,y+ry*.36f);Q.lineTo(x+rx*.27f,y+ry*.52f);Q.lineTo(x+rx*.63f,y);Q.close();P.setColor(withAlpha(colors[2],130));c.drawPath(Q,P);
        oval(c,x-rx,y-ry*.64f,x+rx,y+ry*.37f,colors[2]);
        oval(c,x-rx*.93f,y-ry*.61f,x+rx*.92f,y+ry*.22f,mix(colors[2],0xffffffff,.17f));
        // Edge foliage leaves enough open surface for the numbered route.
        for(int i=0;i<5;i++){float xx=x-rx*.77f+i*rx*.38f,yy=y+ry*.13f+(float)Math.sin(i*2.1)*ry*.08f;oval(c,xx-8,yy-3,xx+8,yy+4,withAlpha(colors[3],36));}
    }

    private static void landmark(Canvas c,float x,float y,int biome,int[] colors){
        c.save();c.translate(x,y);oval(c,-30,22,30,35,0x218073a1);
        if(biome==0||biome==4||biome==8){
            crystal(c,-15,10,15,colors[3]);crystal(c,9,3,27,colors[4]);crystal(c,25,15,11,mix(colors[3],0xffffffff,.36f));
            if(biome==4){P.setColor(0x79efffff);P.setStrokeWidth(6);P.setStrokeCap(Paint.Cap.ROUND);c.drawLine(-14,32,-14,90,P);c.drawLine(-5,32,-5,72,P);}
        }else if(biome==1||biome==5||biome==7){
            P.setColor(colors[3]);P.setStrokeWidth(7);P.setStrokeCap(Paint.Cap.ROUND);c.drawLine(0,25,0,-24,P);
            oval(c,-29,-35,14,6,mix(colors[2],colors[3],.28f));oval(c,-9,-48,30,-1,mix(colors[2],0xffffffff,.20f));oval(c,-24,-20,26,18,colors[2]);
            for(int j=0;j<3;j++)oval(c,-15+j*14,-10-(j%2)*13,-6+j*14,-1-(j%2)*13,colors[4]);
        }else if(biome==2){
            P.setColor(colors[3]);P.setStrokeWidth(5);c.drawLine(-7,26,-7,-32,P);
            Q.reset();Q.moveTo(-7,-37);Q.cubicTo(-5,-42,26,-48,30,-22);Q.cubicTo(4,-34,-5,-18,-7,-37);Q.close();P.setColor(colors[4]);c.drawPath(Q,P);
            oval(c,-23,20,27,33,mix(colors[3],0xffffffff,.26f));
        }else if(biome==6){
            P.setColor(colors[3]);P.setStrokeWidth(3);c.drawLine(-28,-32,28,-24,P);
            for(int j=0;j<3;j++){float xx=-24+j*24,yy=-25+j*4;P.setColor(colors[3]);P.setStrokeWidth(2);c.drawLine(xx,yy-4,xx,yy+5,P);oval(c,xx-8,yy+3,xx+8,yy+23,colors[4]);}
        }else{
            cloud(c,0,14,.58f,0xf0ffffff);
            for(int i=0;i<3;i++){float xx=(i-1)*16,top=-11-(i%2)*24;rect(c,xx-9,top,xx+9,23,mix(colors[3],0xffffffff,.26f));
                Q.reset();Q.moveTo(xx-12,top);Q.lineTo(xx,top-17);Q.lineTo(xx+12,top);Q.close();P.setColor(colors[3]);c.drawPath(Q,P);rect(c,xx-2,top+8,xx+2,top+15,colors[4]);}
        }
        c.restore();
    }
    private static void crystal(Canvas c,float x,float y,float height,int color){
        Q.reset();Q.moveTo(x,y-height);Q.lineTo(x+height*.39f,y-height*.42f);Q.lineTo(x+height*.28f,y+height*.29f);Q.lineTo(x-height*.25f,y+height*.3f);Q.lineTo(x-height*.36f,y-height*.46f);Q.close();P.setColor(color);c.drawPath(Q,P);
        Q.reset();Q.moveTo(x,y-height);Q.lineTo(x+height*.02f,y+height*.27f);Q.lineTo(x-height*.25f,y+height*.3f);Q.lineTo(x-height*.36f,y-height*.46f);Q.close();P.setColor(0x55ffffff);c.drawPath(Q,P);
    }
    private static void cloud(Canvas c,float x,float y,float scale,int color){
        oval(c,x-54*scale,y-8*scale,x+53*scale,y+17*scale,color);oval(c,x-31*scale,y-26*scale,x+5*scale,y+14*scale,color);oval(c,x-6*scale,y-34*scale,x+35*scale,y+15*scale,color);
    }
    private static void sparkle(Canvas c,float x,float y,float radius,int color){Q.reset();Q.moveTo(x,y-radius*1.7f);Q.quadTo(x+radius*.3f,y-radius*.3f,x+radius,y);Q.quadTo(x+radius*.3f,y+radius*.3f,x,y+radius*1.7f);Q.quadTo(x-radius*.3f,y+radius*.3f,x-radius,y);Q.quadTo(x-radius*.3f,y-radius*.3f,x,y-radius*1.7f);Q.close();P.setColor(color);c.drawPath(Q,P);}
    private static void rect(Canvas c,float l,float t,float r,float b,int color){P.setShader(null);P.setStyle(Paint.Style.FILL);P.setColor(color);c.drawRect(l,t,r,b,P);}
    private static void oval(Canvas c,float l,float t,float r,float b,int color){P.setShader(null);P.setStyle(Paint.Style.FILL);P.setColor(color);R.set(l,t,r,b);c.drawOval(R,P);}
    private static int withAlpha(int color,int alpha){return(color&0xffffff)|(alpha<<24);}
    private static int mix(int a,int b,float amount){int r=(int)(((a>>16)&255)*(1-amount)+((b>>16)&255)*amount),g=(int)(((a>>8)&255)*(1-amount)+((b>>8)&255)*amount),bl=(int)((a&255)*(1-amount)+(b&255)*amount);return 0xff000000|(r<<16)|(g<<8)|bl;}
}
