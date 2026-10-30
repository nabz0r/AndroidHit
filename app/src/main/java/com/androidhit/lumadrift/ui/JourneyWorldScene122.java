package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import java.util.HashMap;
import java.util.Map;

/** Canvas art for the pure world model. Caller owns camera transform, actual hits, art and progress. */
public final class JourneyWorldScene122 {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private final RectF rect=new RectF();
    private final Map<Long,Geometry> geometryCache=new HashMap<>();
    /** Built once per actual sector/catalogue pair. Paths are private and never mutated after construction. */
    private static final class Geometry {
        final int first,last;
        final JourneyWorld122.Point[] nodes;
        final Path[] edges;
        Geometry(int sector,int total){
            first=JourneyWorld122.firstInSector(sector,total);last=JourneyWorld122.lastInSector(sector,total);
            nodes=new JourneyWorld122.Point[last-first+1];edges=new Path[Math.max(0,nodes.length-1)];
            for(int i=0;i<nodes.length;i++)nodes[i]=JourneyWorld122.node(first+i);
            for(int i=0;i<edges.length;i++){
                Path edge=new Path();
                for(int sample=0;sample<=16;sample++){
                    JourneyWorld122.Point p=JourneyWorld122.edge(first+i,first+i+1,sample/16f);
                    if(sample==0)edge.moveTo(p.x,p.y);else edge.lineTo(p.x,p.y);
                }
                edges[i]=edge;
            }
        }
    }
    private Geometry geometry(int sector,int total){
        long key=((long)total<<32)|(sector&0xffffffffL);Geometry found=geometryCache.get(key);
        if(found==null){found=new Geometry(sector,total);geometryCache.put(key,found);}return found;
    }

    /** Invoke inside the camera's clip/translate/scale. This method never changes that transform. */
    public void drawWorld(Canvas canvas,int sector,int total,int unlocked,long now,boolean reduced,JourneyWorld122.Rect visible){
        Geometry geometry=geometry(sector,total);int first=geometry.first,last=geometry.last;
        int ink=JourneyMapScene.ink(first),accent=JourneyMapScene.accent(first);
        paint.setStyle(Paint.Style.FILL);paint.setShader(new LinearGradient(0,0,JourneyWorld122.WIDTH,JourneyWorld122.HEIGHT,mix(ink,0xffffffff,.87f),mix(ink,0xffffffff,.64f),Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,JourneyWorld122.WIDTH,JourneyWorld122.HEIGHT,paint);paint.setShader(null);
        int rows=(last-first)/10+1;
        for(int row=0;row<rows;row++){
            float y=164+148*row;
            if(visible!=null&&(y+140<visible.top||y-120>visible.bottom))continue;
            float drift=reduced?0:(float)Math.sin(now*.00027+row)*12;
            cloud(canvas,72+drift,y-89,1.2f,0xb8ffffff);cloud(canvas,890-drift,y+80,.8f,0x99ffffff);
            // A broad island carries each row; the route bends across real end bridges.
            path.reset();path.moveTo(52,y+13);path.cubicTo(115,y+116,366,y+135,480,y+134);
            path.cubicTo(627,y+130,842,y+93,908,y+13);path.close();fill(ink);canvas.drawPath(path,paint);
            fill(mix(ink,0xffffffff,.51f));canvas.drawOval(44,y-70,916,y+65,paint);
            fill(mix(ink,0xffffffff,.67f));canvas.drawOval(55,y-70,905,y+44,paint);
            for(int stem=0;stem<9;stem++){
                float x=81+stem*99,yy=y-62+(stem%3)*4;
                paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeWidth(3);paint.setColor(alpha(ink,100));
                canvas.drawLine(x,yy+16,x,yy,paint);fill(stem%3==0?accent:mix(ink,0xffffffff,.25f));canvas.drawOval(x-6,yy-5,x+6,yy+4,paint);
            }
        }
        for(int level=first;level<last;level++){
            JourneyWorld122.Point a=geometry.nodes[level-first],b=geometry.nodes[level-first+1];
            if(visible!=null&&(Math.max(a.y,b.y)+85<visible.top||Math.min(a.y,b.y)-85>visible.bottom||Math.max(a.x,b.x)+70<visible.left||Math.min(a.x,b.x)-70>visible.right))continue;
            Path edge=geometry.edges[level-first];
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(alpha(ink,90));paint.setStrokeWidth(27);canvas.drawPath(edge,paint);
            paint.setColor(level+1<=unlocked?0xffedcf88:0xffbcb4cc);paint.setStrokeWidth(20);canvas.drawPath(edge,paint);
            paint.setColor(level+1<=unlocked?0xffffedb5:0xffe9e2ef);paint.setStrokeWidth(11);canvas.drawPath(edge,paint);
            if(!reduced&&level+1<=unlocked){JourneyWorld122.Point light=JourneyWorld122.edge(level,level+1,(now%2200)/2200f);fill(0xd9ffffff);canvas.drawCircle(light.x,light.y,3,paint);}
        }
    }

    public void drawNode(Canvas canvas,int level,boolean open,boolean current,int stars,long now,boolean reduced){
        JourneyWorld122.Point point=JourneyWorld122.node(level);float x=point.x,y=point.y;int ink=JourneyMapScene.ink(level),accent=JourneyMapScene.accent(level);
        fill(0x319079a9);canvas.drawOval(x-32,y+17,x+32,y+35,paint);
        if(current){float pulse=reduced?0:(float)Math.sin(now*.003)*3;fill(0x91ffffff);canvas.drawCircle(x,y,39+pulse,paint);}
        fill(open?ink:0xff9790a5);canvas.drawCircle(x,y+5,JourneyWorld122.NODE_RADIUS,paint);
        fill(open?(current?accent:stars>0?0xffc8ecda:0xfffaf2ff):0xffddd8e7);canvas.drawCircle(x,y,JourneyWorld122.NODE_RADIUS,paint);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2);paint.setColor(0xcfffffff);canvas.drawCircle(x,y,JourneyWorld122.NODE_RADIUS-4,paint);
        fill(open?0xff403753:0xff777180);paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(open?19:14);
        if(open)canvas.drawText(String.valueOf(level),x,y+7,paint);
        else{
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.4f);rect.set(x-5,y-15,x+5,y-5);canvas.drawArc(rect,180,180,false,paint);
            fill(0xff777180);rect.set(x-8,y-9,x+8,y+3);canvas.drawRoundRect(rect,3,3,paint);canvas.drawText(String.valueOf(level),x,y+22,paint);
        }
        for(int star=0;star<3;star++)star(canvas,x+(star-1)*13,y+38,5.3f,star<stars?0xffd49b35:0xffbeb5cb);
        paint.setTextAlign(Paint.Align.LEFT);paint.setTypeface(Typeface.DEFAULT);
    }

    /** Fixed-screen overlay. Minimap interaction must use miniToWorld and must not move the avatar. */
    public void drawMinimap(Canvas canvas,int sector,int total,int unlocked,int avatarLevel,float avatarX,float avatarY,JourneyCamera122.Frame camera,JourneyWorld122.Rect mini){
        Geometry geometry=geometry(sector,total);int first=geometry.first,last=geometry.last;
        fill(0xf5fffaff);rect.set(mini.left-5,mini.top-5,mini.right+5,mini.bottom+5);canvas.drawRoundRect(rect,11,11,paint);
        int save=canvas.save();canvas.clipRect(mini.left,mini.top,mini.right,mini.bottom);
        fill(mix(JourneyMapScene.ink(first),0xffffffff,.84f));canvas.drawRect(mini.left,mini.top,mini.right,mini.bottom,paint);
        int routeSave=canvas.save();canvas.translate(mini.left,mini.top);float miniScaleX=mini.width()/JourneyWorld122.WIDTH,miniScaleY=mini.height()/JourneyWorld122.HEIGHT;canvas.scale(miniScaleX,miniScaleY);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2/Math.max(.001f,Math.min(miniScaleX,miniScaleY)));paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);
        for(int level=first;level<last;level++){paint.setColor(level+1<=unlocked?0xffc39138:0xffb4a9c5);canvas.drawPath(geometry.edges[level-first],paint);}
        canvas.restoreToCount(routeSave);
        for(int level=first;level<=last;level++){
            JourneyWorld122.Point p=geometry.nodes[level-first];fill(level<=unlocked?0xff8d75ac:0xffc8bfd5);canvas.drawCircle(miniX(p.x,mini),miniY(p.y,mini),level==unlocked?2.8f:1.25f,paint);
        }
        if(avatarLevel>=first&&avatarLevel<=last){float x=miniX(avatarX,mini),y=miniY(avatarY,mini);fill(0xffffffff);canvas.drawCircle(x,y,4.2f,paint);fill(0xff533880);canvas.drawCircle(x,y,2.6f,paint);}
        float left=miniX(camera.left,mini),top=miniY(camera.top,mini),right=miniX(camera.right,mini),bottom=miniY(camera.bottom,mini);
        fill(0x2b7256bb);canvas.drawRect(left,top,right,bottom,paint);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.8f);paint.setColor(0xff62448e);canvas.drawRect(left,top,right,bottom,paint);
        canvas.restoreToCount(save);paint.setStyle(Paint.Style.FILL);
    }
    private static float miniX(float x,JourneyWorld122.Rect mini){return mini.left+JourneyWorld122.clamp(x,0,JourneyWorld122.WIDTH)/JourneyWorld122.WIDTH*mini.width();}
    private static float miniY(float y,JourneyWorld122.Rect mini){return mini.top+JourneyWorld122.clamp(y,0,JourneyWorld122.HEIGHT)/JourneyWorld122.HEIGHT*mini.height();}
    private void cloud(Canvas canvas,float x,float y,float scale,int color){fill(color);canvas.drawOval(x-39*scale,y-6*scale,x+39*scale,y+12*scale,paint);canvas.drawCircle(x-12*scale,y-9*scale,16*scale,paint);canvas.drawCircle(x+11*scale,y-13*scale,20*scale,paint);}
    private void star(Canvas canvas,float x,float y,float size,int color){path.reset();for(int i=0;i<10;i++){double angle=-Math.PI/2+i*Math.PI/5;float r=(i&1)==0?size:size*.45f,xx=x+(float)Math.cos(angle)*r,yy=y+(float)Math.sin(angle)*r;if(i==0)path.moveTo(xx,yy);else path.lineTo(xx,yy);}path.close();fill(color);canvas.drawPath(path,paint);}
    private void fill(int color){paint.setShader(null);paint.setStyle(Paint.Style.FILL);paint.setColor(color);}
    private static int alpha(int color,int a){return color&0xffffff|a<<24;}
    private static int mix(int a,int b,float t){int r=(int)(((a>>16)&255)*(1-t)+((b>>16)&255)*t),g=(int)(((a>>8)&255)*(1-t)+((b>>8)&255)*t),bl=(int)((a&255)*(1-t)+(b&255)*t);return 0xff000000|(r<<16)|(g<<8)|bl;}
}
