package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.TextUtils;

/**
 * Original 1.15 visual language. Drawing only: navigation, touch targets, copy and
 * ownership remain with the caller. Coordinates are the caller's logical units.
 * Keep one instance per UI thread. No remote assets or random reward presentation.
 */
public final class PremiumUi {
    public static final int BG=0xfff6f5fa, PAPER=0xfffffdfb, INK=0xff30344b,
            MUTED=0xff686a80, INDIGO=0xff56538b, ON_INDIGO=0xffffffff,
            MINT=0xff8ccbb5, PEACH=0xffe9ae96, LILAC=0xffb5a7d9,
            GOLD=0xffe6c16e;
    public static final float PAD=24f, GAP=12f, TOUCH=48f;
    private static final int INDIGO_DEEP=0xff3d3b69, LINE=0xffdedce8;
    private static final int[] TONE={PEACH,MINT,LILAC,GOLD};
    private static final int[] SOFT={0xfff5e2da,0xffdceee6,0xffe8e3f3,0xfff5ebd1};
    private static final int[] DEEP={0xff8c5149,0xff386f5d,0xff64518d,0xff80652e};
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final android.text.TextPaint text=new android.text.TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private final RectF scratch=new RectF();
    private final Typeface regular=Typeface.create("sans-serif",Typeface.NORMAL);
    private final Typeface medium=Typeface.create("sans-serif-medium",Typeface.NORMAL);
    private float minimumTextSize=11f;

    /** Pass ExperienceLayout.minimumTextSize after layout so narrow windows keep the physical font floor. */
    public void setMinimumTextSize(float size){minimumTextSize=Float.isFinite(size)?Math.max(11,Math.min(30,size)):11f;}

    private static int toneIndex(int value){if(value==PEACH)return 0;if(value==MINT)return 1;if(value==LILAC||value==PAPER||value==BG)return 2;if(value==GOLD)return 3;return Math.floorMod(value,4);}
    public static int tone(int value){return TONE[toneIndex(value)];}
    public static int soft(int value){return SOFT[toneIndex(value)];}
    public static int ink(int value){return DEEP[toneIndex(value)];}
    private static int alpha(int color,int a){return (color&0xffffff)|(Math.max(0,Math.min(255,a))<<24);}
    private static int mix(int a,int b,float t){float v=Math.max(0,Math.min(1,t));return Color.rgb(Math.round(Color.red(a)*(1-v)+Color.red(b)*v),Math.round(Color.green(a)*(1-v)+Color.green(b)*v),Math.round(Color.blue(a)*(1-v)+Color.blue(b)*v));}
    private void paint(int color){p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);}
    private void round(Canvas c,float l,float t,float r,float b,float radius,int color){paint(color);c.drawRoundRect(l,t,r,b,radius,radius,p);}
    private void oval(Canvas c,float l,float t,float r,float b,int color){paint(color);c.drawOval(l,t,r,b,p);}
    private void circle(Canvas c,float x,float y,float r,int color){paint(color);c.drawCircle(x,y,r,p);}
    private void line(Canvas c,float x,float y,float xx,float yy,int color,float width){paint(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,p);}
    private void stroke(Canvas c,RectF box,float radius,int color,float width){paint(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);c.drawRoundRect(box,radius,radius,p);p.setStyle(Paint.Style.FILL);}

    /** An opaque bounded surface. Tone uses SweetUi's PEACH/MINT/LILAC/CREAM values. */
    public void surface(Canvas c,RectF box,int tone,boolean elevated){
        float r=Math.min(28,box.height()*.24f);
        if(elevated)round(c,box.left,box.top+4,box.right,box.bottom+4,r,0x143f3a66);
        round(c,box.left,box.top,box.right,box.bottom,r,tone==PAPER||tone==BG?tone:soft(tone));
    }

    /** Primary action background. Caller uses ON_INDIGO for its label and owns the hit area. */
    public void heroButton(Canvas c,RectF box,boolean pressed){
        float radius=Math.min(20,box.height()*.36f),drop=pressed?1:4;
        round(c,box.left,box.top+drop,box.right,box.bottom+drop,radius,INDIGO_DEEP);
        paint(pressed?INDIGO_DEEP:INDIGO);c.drawRoundRect(box,radius,radius,p);
        if(!pressed)line(c,box.left+radius,box.top+2,box.right-radius,box.top+2,0x226e74bd,1.5f);
    }

    /** Secondary action background; dark text on an opaque neutral or selected indigo wash. */
    public void quietButton(Canvas c,RectF box,boolean selected){
        float radius=Math.min(17,box.height()*.34f);
        round(c,box.left,box.top,box.right,box.bottom,radius,selected?SOFT[2]:PAPER);
        if(selected)stroke(c,box,radius,alpha(INDIGO,130),1.4f);
    }

    /** Compact display or selection chip; this does not create a touch target. */
    public void chip(Canvas c,RectF box,int tone,boolean selected){
        boolean neutral=tone==PAPER||tone==BG;float radius=box.height()*.5f;
        round(c,box.left,box.top,box.right,box.bottom,radius,neutral?selected?SOFT[2]:PAPER:selected?PremiumUi.tone(tone):soft(tone));
        if(neutral&&selected)stroke(c,box,radius,alpha(INDIGO,130),1.4f);
    }

    /** A large, uncluttered illustration field, reserved for one actual object or portrait. */
    public void illustrationStage(Canvas c,RectF box,int tone){
        float radius=Math.min(32,box.width()*.12f);
        round(c,box.left,box.top,box.right,box.bottom,radius,soft(tone));
        c.save();c.clipRect(box);
        oval(c,box.left+box.width()*.11f,box.top+box.height()*.12f,box.right-box.width()*.11f,box.bottom+box.height()*.2f,alpha(PAPER,155));
        oval(c,box.left+box.width()*.24f,box.bottom-box.height()*.18f,box.right-box.width()*.24f,box.bottom-box.height()*.08f,alpha(ink(tone),22));
        sparkle(c,box.left+box.width()*.16f,box.top+box.height()*.24f,7,tone(tone));
        sparkle(c,box.right-box.width()*.14f,box.top+box.height()*.43f,5,ink(tone));
        c.restore();
    }

    /** Reward panel paper and illustration backdrop only, leaving title/one sentence/action to the caller. */
    public void rewardPanel(Canvas c,RectF box,int tone){
        round(c,box.left,box.top+5,box.right,box.bottom+5,30,0x203b3455);
        round(c,box.left,box.top,box.right,box.bottom,30,PAPER);
        float radius=Math.min(box.width()*.31f,box.height()*.22f),cy=box.top+radius+28;
        circle(c,box.centerX(),cy,radius,soft(tone));
        sparkle(c,box.centerX()-radius*.96f,cy-radius*.44f,8,tone(tone));
        sparkle(c,box.centerX()+radius*.93f,cy+radius*.24f,5,ink(tone));
        circle(c,box.centerX()+radius*.81f,cy-radius*.71f,3,tone(tone));
    }

    /** Four destinations, in order: Aventure, Maison, Collection, Atelier. Caller adds 4 semantic hit areas. */
    public void navigation(Canvas c,RectF bounds,int active,String[] labels){
        round(c,bounds.left,bounds.top,bounds.right,bounds.bottom,24,PAPER);
        float slot=bounds.width()/4f;
        for(int i=0;i<4;i++){
            boolean selected=i==active;float x=bounds.left+slot*(i+.5f),top=bounds.top+8;
            if(selected)round(c,bounds.left+i*slot+5,top,bounds.left+(i+1)*slot-5,bounds.bottom-7,18,INDIGO);
            destinationIcon(c,i,x,top+18,20,selected?ON_INDIGO:MUTED);
            String label=labels!=null&&i<labels.length?labels[i]:new String[]{"Aventure","Maison","Collection","Atelier"}[i];
            fit(c,label,x,bounds.bottom-17,11.5f,slot-10,selected?ON_INDIGO:MUTED,true,true);
        }
    }

    /** size is the icon's full width. No glyph font dependency. */
    public void destinationIcon(Canvas c,int index,float x,float y,float size,int color){
        c.save();c.translate(x,y);c.scale(size/24f,size/24f);
        if(index==0){
            paint(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.9f);p.setStrokeJoin(Paint.Join.ROUND);
            path.reset();path.moveTo(-10,-7);path.lineTo(-3,-10);path.lineTo(4,-7);path.lineTo(11,-10);path.lineTo(11,7);path.lineTo(4,10);path.lineTo(-3,7);path.lineTo(-10,10);path.close();c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
            line(c,-3,-10,-3,7,color,1.6f);line(c,4,-7,4,10,color,1.6f);
        }else if(index==1){
            paint(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.9f);p.setStrokeJoin(Paint.Join.ROUND);
            path.reset();path.moveTo(-11,-1);path.lineTo(0,-11);path.lineTo(11,-1);path.moveTo(-8,-3);path.lineTo(-8,10);path.lineTo(8,10);path.lineTo(8,-3);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
            round(c,-2.5f,2,2.5f,10,1.5f,color);
        }else if(index==2){
            for(int row=0;row<2;row++)for(int col=0;col<2;col++){
                scratch.set(-10+col*12,-10+row*12,-2+col*12,-2+row*12);stroke(c,scratch,2.5f,color,1.9f);
            }
        }else{
            line(c,-7,9,7,-5,color,4);line(c,-7,9,-10,11,color,2);
            oval(c,3,-12,12,-3,color);sparkle(c,-6,-9,3.5f,color);sparkle(c,9,7,2.5f,color);
        }
        c.restore();
    }

    /** Exact single-line fit. Does not invent line breaks or let large labels overflow a control. */
    public void fit(Canvas c,String value,float x,float baseline,float size,float max,int color,boolean heavy,boolean centered){
        if(value==null||max<=0)return;
        text.setShader(null);text.setTypeface(heavy?medium:regular);text.setTextSize(Math.max(minimumTextSize,size));text.setColor(color);text.setTextAlign(Paint.Align.LEFT);
        String result=TextUtils.ellipsize(value,text,max,TextUtils.TruncateAt.END).toString();
        c.drawText(result,centered?x-text.measureText(result)/2:x,baseline,text);
    }

    /** Short multi-line copy. Caller reserves the returned height; no fixed-height text collision. */
    public float copy(Canvas c,String value,float x,float baseline,float width,float size,int color,int maximumLines){
        if(value==null||maximumLines<=0)return 0;
        text.setShader(null);text.setTypeface(regular);text.setTextSize(Math.max(minimumTextSize,Math.max(12,size)));text.setColor(color);
        android.text.StaticLayout layout=android.text.StaticLayout.Builder.obtain(value,0,value.length(),text,Math.max(1,Math.round(width))).setIncludePad(false).setMaxLines(maximumLines).setEllipsize(TextUtils.TruncateAt.END).setLineSpacing(3,1).build();
        c.save();c.translate(x,baseline+text.ascent());layout.draw(c);c.restore();return layout.getHeight();
    }

    /** A four-point original illustration accent, distinct from earned score stars. */
    private void sparkle(Canvas c,float x,float y,float r,int color){
        path.reset();path.moveTo(x,y-r);path.quadTo(x+r*.15f,y-r*.15f,x+r,y);path.quadTo(x+r*.15f,y+r*.15f,x,y+r);path.quadTo(x-r*.15f,y+r*.15f,x-r,y);path.quadTo(x-r*.15f,y-r*.15f,x,y-r);path.close();paint(color);c.drawPath(path,p);
    }

    /** size is the chest's full body width. Motion affects decoration only and freezes in reduced motion. */
    public void rewardChest(Canvas c,float x,float y,float size,long time,boolean still){
        c.save();c.translate(x,y);c.scale(size/180f,size/180f);
        float bob=still?0:(float)Math.sin(time*.0022)*2;c.translate(0,bob);
        oval(c,-83,66,83,83,0x183e3557);
        round(c,-79,-7,79,69,13,0xff936969);
        round(c,-79,-12,79,60,13,0xffdfac98);
        round(c,-70,-4,70,49,10,0xffedc7ac);
        round(c,-88,-57,88,0,18,INDIGO_DEEP);
        round(c,-88,-64,88,-8,18,INDIGO);
        round(c,-76,-58,76,-16,14,0xff7772a4);
        round(c,-60,-63,-43,58,5,GOLD);round(c,43,-63,60,58,5,GOLD);
        line(c,-56,-59,-48,-59,0xfff7e4ad,2);line(c,47,-59,55,-59,0xfff7e4ad,2);
        round(c,-19,-18,19,22,7,0xffae844a);round(c,-16,-21,16,18,6,0xfff4d590);
        sparkle(c,0,-2,9,INDIGO);circle(c,-68,38,2.5f,0xffad7c6b);circle(c,68,38,2.5f,0xffad7c6b);
        sparkle(c,-107,-40,12,GOLD);sparkle(c,106,-66,9,MINT);sparkle(c,111,35,6,PEACH);
        c.restore();
    }

    /** Original layered garden art, never a screenshot or a representation of puzzle difficulty. */
    public void gardenLandscape(Canvas c,RectF box,int biome,long time,boolean still){
        int i=Math.floorMod(biome,3),sky=i==0?0xffdceee7:i==1?0xffeee1d9:0xffe3dff1;
        int foliage=i==0?0xff709f8d:i==1?0xffba8d80:0xff8c86ad;
        c.save();path.reset();path.addRoundRect(box,30,30,Path.Direction.CW);c.clipPath(path);
        paint(sky);c.drawRect(box,p);c.translate(box.left,box.top);c.scale(box.width()/1000f,box.height()/700f);
        paint(PAPER);p.setAlpha(145);c.drawCircle(780,130,72,p);p.setAlpha(255);
        cloud(c,150,105,1f);cloud(c,850,230,.68f);
        hill(c,-140,430,620,290,mix(foliage,PAPER,.65f));hill(c,540,360,650,340,mix(foliage,PAPER,.53f));
        paint(mix(foliage,PAPER,.38f));path.reset();path.moveTo(0,493);path.cubicTo(180,380,390,555,600,462);path.cubicTo(780,381,880,470,1000,455);path.lineTo(1000,700);path.lineTo(0,700);path.close();c.drawPath(path,p);
        // The open stone arch and stepping path give a destination, without inventing a level route.
        paint(0xffede4d8);path.reset();path.moveTo(395,700);path.cubicTo(455,625,620,583,570,506);path.lineTo(650,506);path.cubicTo(720,590,579,670,576,700);path.close();c.drawPath(path,p);
        for(int k=0;k<4;k++)oval(c,420+k*34,663-k*32,512+k*25,679-k*32,alpha(0xffb6a99b,90));
        round(c,509,288,551,505,14,0xffb5b5c4);round(c,649,288,691,505,14,0xff9f9fb4);
        paint(0xffb5b5c4);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(44);c.drawArc(531,199,669,376,180,180,false,p);p.setStyle(Paint.Style.FILL);
        line(c,531,284,531,483,0xffd8d5dd,7);line(c,669,289,669,482,0xffcfccd8,6);
        round(c,502,488,559,509,7,0xff989ba8);round(c,642,488,698,509,7,0xff8d91a3);
        for(int k=0;k<5;k++){
            float xx=169+k*122,yy=472+(k%2)*27;
            line(c,xx,yy,xx,yy+58,mix(foliage,INK,.22f),10);line(c,xx-20,yy+22,xx+20,yy+22,foliage,7);
        }
        shrub(c,93,571,115,foliage);shrub(c,900,572,148,mix(foliage,INK,.08f));shrub(c,240,647,84,mix(foliage,INK,.05f));
        lantern(c,759,386,1.1f);lantern(c,301,510,.75f);
        for(int k=0;k<11;k++){
            float phase=still?0:(float)Math.sin(time*.0014+k)*5;
            float xx=80+(k*139)%850,yy=210+(k*97)%350+phase;
            circle(c,xx,yy,4+(k%2),0xfff9e5a8);
        }
        for(int k=0;k<6;k++)flower(c,50+k*168,617+(k%3)*21,10+k%2*3,k%2==0?PEACH:GOLD);
        c.restore();
    }

    private void cloud(Canvas c,float x,float y,float size){c.save();c.translate(x,y);c.scale(size,size);oval(c,-70,-2,75,31,alpha(PAPER,160));circle(c,-15,0,30,alpha(PAPER,160));circle(c,23,5,22,alpha(PAPER,160));c.restore();}
    private void hill(Canvas c,float x,float y,float width,float height,int color){oval(c,x,y,x+width,y+height,color);}
    private void shrub(Canvas c,float x,float y,float size,int color){circle(c,x-size*.4f,y,size*.44f,color);circle(c,x,y-size*.24f,size*.5f,color);circle(c,x+size*.4f,y,size*.42f,color);oval(c,x-size*.78f,y-size*.04f,x+size*.78f,y+size*.48f,color);line(c,x,y+size*.25f,x,y-size*.18f,alpha(PAPER,65),3);}
    private void lantern(Canvas c,float x,float y,float scale){c.save();c.translate(x,y);c.scale(scale,scale);line(c,0,-46,0,21,INDIGO_DEEP,7);round(c,-24,-47,24,-6,8,PEACH);round(c,-16,-40,16,-14,5,0xfff7dda6);round(c,-29,-53,29,-43,5,INDIGO);round(c,-27,-8,27,1,4,INDIGO);line(c,-13,-33,13,-33,0xfff9edce,3);c.restore();}
    private void flower(Canvas c,float x,float y,float size,int color){for(int k=0;k<5;k++){double a=k*Math.PI*2/5;circle(c,x+(float)Math.cos(a)*size*.57f,y+(float)Math.sin(a)*size*.57f,size*.48f,color);}circle(c,x,y,size*.3f,0xfff8e3ad);}
}
