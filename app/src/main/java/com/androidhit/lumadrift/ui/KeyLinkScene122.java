package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import com.androidhit.lumadrift.engine.Barrier;
import com.androidhit.lumadrift.engine.Board;
import com.androidhit.lumadrift.engine.Tile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** Presentation of actual key state. Owns no board command, reward or persistence. */
public final class KeyLinkScene122 {
    private final Paint paint=new Paint(3);
    private final Path path=new Path();
    private final Map<Integer,Boolean> active=new HashMap<>();
    private final Map<Integer,Long> changed=new HashMap<>();
    private String level;
    private int inspected=-1;
    private long inspectUntil;
    public void reset(){level=null;active.clear();changed.clear();inspected=-1;}
    public void inspect(int key,long now){inspected=key;inspectUntil=now+3400;}
    public boolean highlighted(int key,long now){return key==inspected&&now<inspectUntil||changed.containsKey(key)&&now-changed.get(key)<950;}
    public boolean animating(long now,boolean reduced){if(reduced)return false;for(long at:changed.values())if(now-at<950)return true;return false;}
    public void sync(Board board,long now){
        String identity=board.level.progressionVersion+":"+board.level.number+":"+board.level.seed+":"+board.level.size;if(!identity.equals(level)){reset();level=identity;}
        for(Barrier wall:board.level.barriers){boolean on=board.isPresent(wall.keyTileId);Boolean old=active.put(wall.keyTileId,on);if(old!=null&&old!=on){changed.put(wall.keyTileId,now);inspected=wall.keyTileId;inspectUntil=now+2200;}}
    }
    public static int number(Board board,int key){ArrayList<Integer> ids=new ArrayList<>();for(Barrier wall:board.level.barriers)if(!ids.contains(wall.keyTileId))ids.add(wall.keyTileId);return ids.indexOf(key)+1;}
    private int color(int n){return new int[]{0xff7c52b4,0xff217f83,0xffae526a,0xff936317}[(Math.max(1,n)-1)%4];}
    private void mark(Canvas c,float x,float y,float radius,int number,int color){
        paint.setStyle(Paint.Style.FILL);paint.setColor(color);c.drawCircle(x,y,radius,paint);
        paint.setColor(0xffffffff);paint.setTextAlign(Paint.Align.CENTER);paint.setTypeface(android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.BOLD));paint.setTextSize(Math.max(11,radius));c.drawText(String.valueOf(number),x,y+radius*.34f,paint);
    }
    public void key(Canvas c,Board board,int id,float x,float y,float cell,long now){
        int n=number(board,id),color=color(n);float radius=Math.max(11,cell*.21f),xx=x-cell*.34f,yy=y-cell*.34f;
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(3);paint.setColor(0xffffffff);c.drawCircle(xx,yy,radius+1,paint);
        mark(c,xx,yy,radius,n,color);
        // A key silhouette, separate from the tile's direction arrow and number.
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(2,cell*.045f));paint.setStrokeCap(Paint.Cap.ROUND);paint.setColor(color);
        float kx=x+cell*.26f,ky=y-cell*.28f;c.drawCircle(kx-4,ky-3,4,paint);c.drawLine(kx,ky,kx+8,ky+8,paint);c.drawLine(kx+5,ky+5,kx+8,ky+2,paint);paint.setStyle(Paint.Style.FILL);
    }
    public void barrier(Canvas c,Board board,Barrier wall,float x,float y,float cell,long now,boolean reduced){
        int n=number(board,wall.keyTileId),ink=color(n);boolean closed=board.isBarrierActive(wall);
        Long at=changed.get(wall.keyTileId);float progress=reduced||at==null?1:Math.min(1,Math.max(0,(now-at)/700f));
        if(!closed&&(at==null||progress>=1))return;
        float fade=closed?1:1-progress;paint.setStyle(Paint.Style.FILL);paint.setColor(0xfff0e8fa);paint.setAlpha((int)(255*fade));
        c.drawRoundRect(x-cell*.43f,y-cell*.43f,x+cell*.43f,y+cell*.43f,12,12,paint);
        paint.setColor(ink);paint.setAlpha((int)(255*fade));paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(highlighted(wall.keyTileId,now)?3.5f:2);
        c.drawRoundRect(x-cell*.43f,y-cell*.43f,x+cell*.43f,y+cell*.43f,12,12,paint);
        float lift=closed||reduced?0:progress*cell*.3f;
        c.drawArc(x-cell*.15f,y-cell*.29f-lift,x+cell*.15f,y+cell*.07f-lift,180,closed?180:145,false,paint);
        paint.setStyle(Paint.Style.FILL);c.drawRoundRect(x-cell*.23f,y-cell*.05f,x+cell*.23f,y+cell*.25f,5,5,paint);paint.setAlpha(255);
        mark(c,x+cell*.28f,y-cell*.29f,Math.max(10,cell*.17f),n,ink);
    }
    public void links(Canvas c,Board board,float left,float top,float cell,long now,boolean reduced){
        for(Barrier wall:board.level.barriers){if(!highlighted(wall.keyTileId,now))continue;Tile key=board.level.tile(wall.keyTileId);if(key==null)continue;
            float x=left+(key.col+.5f)*cell,y=top+(key.row+.5f)*cell,wx=left+(wall.col+.5f)*cell,wy=top+(wall.row+.5f)*cell;
            int ink=color(number(board,wall.keyTileId));paint.setColor(ink);paint.setAlpha(150);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2);
            path.reset();path.moveTo(x,y);path.quadTo((x+wx)/2,(y+wy)/2-cell*.4f,wx,wy);c.drawPath(path,paint);
            Long at=changed.get(wall.keyTileId);if(at!=null&&!reduced&&now-at<700){float t=Math.max(0,(now-at)/700f),u=1-t;paint.setStyle(Paint.Style.FILL);paint.setAlpha(255);c.drawCircle(u*u*x+2*u*t*(x+wx)/2+t*t*wx,u*u*y+2*u*t*((y+wy)/2-cell*.4f)+t*t*wy,5,paint);}
            paint.setAlpha(255);paint.setStyle(Paint.Style.FILL);
        }
    }
}
