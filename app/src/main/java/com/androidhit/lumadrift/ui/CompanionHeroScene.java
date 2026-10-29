package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

/** Original luminous observatory stage. Pure decoration; no purchase, chance or ownership state. */
public final class CompanionHeroScene {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private final RectF ring=new RectF();
    public void draw(Canvas c,RectF box,long time,boolean reduced,int tone){
        if(box.width()<=0||box.height()<=0)return;
        float x=box.centerX(),y=box.top,w=box.width(),h=box.height();
        c.save();path.reset();path.addRoundRect(box,30,30,Path.Direction.CW);c.clipPath(path);
        paint.setStyle(Paint.Style.FILL);paint.setShader(new LinearGradient(box.left,y,box.right,box.bottom,new int[]{0xff282556,0xff5c438b,0xffad83bd},null,Shader.TileMode.CLAMP));c.drawRect(box,paint);paint.setShader(null);
        float haloY=y+h*.42f;
        paint.setShader(new RadialGradient(x,haloY,w*.54f,new int[]{0x9cc2f7ef,0x4c918fd9,0x007c78cb},null,Shader.TileMode.CLAMP));c.drawCircle(x,haloY,w*.54f,paint);paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.2f);paint.setColor(0x6dd2f9f1);
        ring.set(x-w*.35f,y+h*.13f,x+w*.35f,y+h*.75f);c.drawOval(ring,paint);
        ring.inset(14,12);paint.setColor(0x3cd1c4f5);c.drawOval(ring,paint);
        c.save();c.rotate(-24,x,haloY);ring.set(x-w*.49f,haloY-h*.13f,x+w*.49f,haloY+h*.13f);paint.setColor(0x47c4f8fa);c.drawOval(ring,paint);c.restore();
        paint.setStyle(Paint.Style.FILL);
        for(int i=0;i<22;i++){
            float xx=box.left+15+((i*83+17)%313)/313f*(w-30),yy=y+22+((i*61+9)%233)/233f*(h*.75f);
            float pulse=reduced?1f:.65f+.35f*(float)Math.sin(time*.0014+i*1.7);
            paint.setColor(i%3==0?0xffb4f8e9:0xffffe5b5);paint.setAlpha((int)(120+90*pulse));
            c.drawCircle(xx,yy,i%5==0?2.3f:1.1f,paint);
            if(i%5==0){paint.setStrokeWidth(1);c.drawLine(xx-4,yy,xx+4,yy,paint);c.drawLine(xx,yy-4,xx,yy+4,paint);}
        }
        paint.setAlpha(255);
        float floor=y+h*.73f;
        paint.setShader(new RadialGradient(x,floor,w*.40f,new int[]{0xffb5ede8,0x558ccfe4,0x0074a0c9},null,Shader.TileMode.CLAMP));c.drawOval(x-w*.44f,floor-15,x+w*.44f,floor+22,paint);paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.8f);paint.setColor(0x99d5fff4);c.drawOval(x-w*.31f,floor-10,x+w*.31f,floor+9,paint);
        paint.setStyle(Paint.Style.FILL);paint.setShader(new LinearGradient(0,y+h*.69f,0,box.bottom,new int[]{0x00252154,0xdc30254d},null,Shader.TileMode.CLAMP));c.drawRect(box.left,y+h*.69f,box.right,box.bottom,paint);paint.setShader(null);
        // Small facets anchor the stage without imitating another game's logos or character art.
        for(int sign=-1;sign<=1;sign+=2){float xx=x+sign*w*.39f,yy=y+h*.47f;path.reset();path.moveTo(xx,yy-16);path.lineTo(xx+7,yy);path.lineTo(xx,yy+16);path.lineTo(xx-7,yy);path.close();paint.setColor(0x7ac1f7ef);c.drawPath(path,paint);}
        c.restore();paint.setStyle(Paint.Style.FILL);paint.setAlpha(255);paint.setShader(null);
    }
}
