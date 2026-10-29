package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;

/**
 * Open, portrait-led team dock. The snapshot is descriptive: this renderer cannot spend energy,
 * select a character, own an item, change a board, or decide that a skill is legal.
 */
public final class CompanionBattleScene {
    public static final float HEIGHT=CompanionBattleLayout.HEIGHT;
    public static final long EFFECT_DURATION_MS=760;
    private static final int INK=0xff34344e, MUTED=0xff666778, LIGHT=0xfff5f5fc, ACCENT=0xff7160db;
    private static final Typeface REGULAR=Typeface.create("sans-serif",Typeface.NORMAL);
    private static final Typeface MEDIUM=Typeface.create("sans-serif-medium",Typeface.NORMAL);
    private static final ThreadLocal<Brush> BRUSH=ThreadLocal.withInitial(Brush::new);
    private CompanionBattleScene(){}

    public interface Portrait {
        void draw(Canvas canvas,String id,RectF box,CompanionMotion.Action action,
                  CompanionMotion.Facing facing,long elapsed,boolean reduced);
    }
    public static final class Slot {
        public final String id,name,signature,family,statusLabel;
        public final int color,energyCost;
        public final boolean ready,used,selected;
        public Slot(String id,String name,String signature,String family,int color,int energyCost,
                    boolean ready,boolean used,boolean selected,String statusLabel){
            this.id=text(id);this.name=text(name);this.signature=text(signature);this.family=text(family);
            this.color=color;this.energyCost=Math.max(0,energyCost);this.ready=ready;this.used=used;
            this.selected=selected;this.statusLabel=text(statusLabel);
        }
        public boolean empty(){return id.isEmpty();}
    }
    public static final class Snapshot {
        public final Slot[] slots;
        public final int energy,maxEnergy;
        public final String energyLabel,comboLabel,comboStatus;
        public final boolean comboReady,comboSpent;
        public Snapshot(Slot[] slots,int energy,int maxEnergy,String energyLabel,String comboLabel,
                        String comboStatus,boolean comboReady,boolean comboSpent){
            this.slots=new Slot[3];if(slots!=null)System.arraycopy(slots,0,this.slots,0,Math.min(3,slots.length));
            this.maxEnergy=Math.max(1,Math.min(6,maxEnergy));this.energy=Math.max(0,Math.min(this.maxEnergy,energy));
            this.energyLabel=text(energyLabel);this.comboLabel=text(comboLabel);this.comboStatus=text(comboStatus);
            this.comboReady=comboReady;this.comboSpent=comboSpent;
        }
    }
    public static final class Layout {
        public final RectF bounds,combo;
        public final RectF[] actions=new RectF[3];
        private final CompanionBattleLayout geometry;
        public Layout(RectF bounds){
            this(bounds,CompanionBattleLayout.MINIMUM_TOUCH);
        }
        public Layout(RectF bounds,float minimumTouchLogical){
            if(bounds==null)throw new IllegalArgumentException("Battle dock bounds required");
            geometry=new CompanionBattleLayout(bounds.left,bounds.top,bounds.right,bounds.bottom,minimumTouchLogical);
            this.bounds=rect(geometry.bounds);combo=rect(geometry.combo);
            for(int n=0;n<3;n++)actions[n]=rect(geometry.actions[n]);
        }
        public int slotAt(float x,float y){return geometry.slotAt(x,y);}
    }

    /** Bounds must be at least 264 x 144; the caller scrolls short screens rather than shrinking it. */
    public static Layout draw(Canvas canvas,RectF bounds,Snapshot snapshot,long elapsed,
                              boolean reduced,Portrait portrait){
        return draw(canvas,bounds,snapshot,elapsed,reduced,portrait,48f,12f);
    }
    public static Layout draw(Canvas canvas,RectF bounds,Snapshot snapshot,long elapsed,
                              boolean reduced,Portrait portrait,float minimumTouchLogical,float minimumText){
        Layout layout=new Layout(bounds,minimumTouchLogical);
        if(canvas==null||snapshot==null)return layout;
        float textFloor=Float.isFinite(minimumText)?Math.max(12,minimumText):12;
        Brush b=BRUSH.get();
        for(int n=0;n<3;n++)drawSlot(canvas,layout.actions[n],snapshot.slots[n],elapsed,reduced,portrait,b,textFloor);
        drawCombo(canvas,layout.combo,snapshot,b,textFloor);
        return layout;
    }

    private static void drawSlot(Canvas c,RectF r,Slot slot,long elapsed,boolean reduced,Portrait portrait,Brush b,float textFloor){
        boolean empty=slot==null||slot.empty();
        int color=empty?0xffa7a7bd:opaque(slot.color);
        boolean largeText=textFloor>13f,veryLargeText=textFloor>18f;
        float cx=r.centerX(),radius=veryLargeText?12f:largeText?18f:20f,cy=r.top+radius+5;
        boolean ready=!empty&&slot.ready&&!slot.used;
        float breath=reduced||!ready?0f:(float)Math.sin(Math.floorMod(elapsed,2400L)*Math.PI/1200f)*.8f;
        fill(b,mix(color,0xffffffff,empty?.88f:.84f));
        c.drawOval(cx-radius-9-breath,cy-radius-2-breath,cx+radius+9+breath,cy+radius+2+breath,b.paint);
        if(!empty&&slot.selected){stroke(b,color,2f);c.drawCircle(cx,cy,radius+4,b.paint);}
        if(empty){
            stroke(b,0xffb7b8ca,1.5f);c.drawCircle(cx,cy,Math.min(20,radius+1),b.paint);
            c.drawLine(cx-5,cy,cx+5,cy,b.paint);c.drawLine(cx,cy-5,cx,cy+5,b.paint);
        }else{
            if(portrait!=null){
                b.box.set(cx-radius,cy-radius,cx+radius,cy+radius);
                portrait.draw(c,slot.id,b.box,slot.selected?CompanionMotion.Action.HAPPY:CompanionMotion.Action.STANCE,
                        CompanionMotion.Facing.FRONT,elapsed,reduced);
            }else{
                fill(b,color);c.drawCircle(cx,cy,16,b.paint);
                label(c,b,slot.name.isEmpty()?"?":slot.name.substring(0,1),cx,cy+5,14,0xffffffff,true,26);
            }
            // A numeric badge communicates cost without relying on colour or an undocumented icon.
            float badgeRadius=Math.max(10,textFloor*.65f),bx=Math.min(r.right-badgeRadius,cx+24),by=r.top+badgeRadius;
            fill(b,slot.used?0xffe3e3ed:INK);c.drawCircle(bx,by,badgeRadius,b.paint);
            label(c,b,Integer.toString(slot.energyCost),bx,by+textFloor*.34f,textFloor,slot.used?MUTED:0xffffffff,true,badgeRadius*1.7f);
        }
        if(empty){
            String status=slot==null?"":slot.statusLabel;
            centeredTwoLines(c,b,status,cx,r.top+Math.min(67,90-textFloor*1.05f),textFloor,MUTED,r.width()-4);
            return;
        }
        // Large accessibility fonts keep the signature and state; the host semantic label retains
        // the complete name instead of squeezing three small lines into the same dock.
        if(!largeText)label(c,b,slot.name,cx,r.top+57,Math.max(12.5f,textFloor),INK,true,r.width()-2);
        label(c,b,slot.signature,cx,r.top+(veryLargeText?53:largeText?58:72),textFloor,INK,false,r.width()-2);
        // The status is real supplied state, never inferred from a decorative pulse.
        label(c,b,slot.statusLabel,cx,r.top+(veryLargeText?82:largeText?82:86),textFloor,ready?0xff4b4092:MUTED,ready,r.width()-2);
        if(ready){fill(b,color);c.drawRoundRect(cx-13,r.top+92,cx+13,r.top+94,1,1,b.paint);}
    }

    private static void drawCombo(Canvas c,RectF r,Snapshot snapshot,Brush b,float textFloor){
        boolean ready=snapshot.comboReady&&!snapshot.comboSpent;
        fill(b,ready?0xffeee9ff:LIGHT);c.drawRoundRect(r,18,18,b.paint);
        // Six stable cells make stored energy and the maximum directly comparable.
        float x=r.left+12,energyWidth=Math.min(100,r.width()*.31f);
        boolean largeText=textFloor>15;
        labelLeft(c,b,(largeText?"":snapshot.energyLabel+" ")+snapshot.energy+"/"+snapshot.maxEnergy,
                x,r.top+Math.max(18,textFloor+4),textFloor,INK,true,energyWidth-4);
        float pipWidth=Math.min(11,(energyWidth-10-5*3)/6f);
        for(int n=0;n<snapshot.maxEnergy;n++){
            fill(b,n<snapshot.energy?ACCENT:0xffdcdce9);
            float pipTop=Math.max(28,textFloor+12);
            c.drawRoundRect(x+n*(pipWidth+3),r.top+pipTop,x+n*(pipWidth+3)+pipWidth,r.top+pipTop+6,3,3,b.paint);
        }
        stroke(b,0xffdcdbe9,1);float divider=r.left+energyWidth+15;
        c.drawLine(divider,r.top+11,divider,r.bottom-11,b.paint);
        float copyLeft=divider+13,copyWidth=r.right-copyLeft-27;
        float firstBaseline=Math.max(19,textFloor+3),secondBaseline=firstBaseline+Math.max(16,textFloor*1.24f);
        labelLeft(c,b,snapshot.comboLabel,copyLeft,r.top+firstBaseline,Math.max(14,textFloor),ready?0xff4b3c92:INK,true,copyWidth);
        labelLeft(c,b,snapshot.comboStatus,copyLeft,r.top+secondBaseline,textFloor,MUTED,false,copyWidth);
        // A static arrow belongs to the whole 48-unit combo action, rather than a second tiny hit target.
        float ax=r.right-15,ay=r.centerY();stroke(b,ready?ACCENT:0xffa6a4b8,1.8f);
        c.drawLine(ax-3,ay-4,ax+1,ay,b.paint);c.drawLine(ax+1,ay,ax-3,ay+4,b.paint);
    }

    /**
     * Short, board-clipped skill punctuation. Kind order follows GUIDE, VISION, TRAIL, PULSE,
     * FLOW, REWIND. Coordinates are presentation-only; no hint/target/valid move is generated here.
     * Reduced motion keeps a steady, low-opacity mark for the same duration; there is no shake or flash.
     * @return true while this effect still has a drawable frame.
     */
    public static boolean drawSkillEffect(Canvas c,RectF board,int color,long elapsed,boolean reduced,
                                          int kind,float targetX,float targetY){
        if(c==null||board==null||!Float.isFinite(board.left)||!Float.isFinite(board.top)
                ||!Float.isFinite(board.right)||!Float.isFinite(board.bottom)
                ||board.width()<28||board.height()<28||elapsed<0||elapsed>=EFFECT_DURATION_MS)return false;
        Brush b=BRUSH.get();float progress=elapsed/(float)EFFECT_DURATION_MS;
        float eased=reduced?.5f:1-(1-progress)*(1-progress),fade=reduced?.58f:Math.min(1,(1-progress)*3);
        float x=Float.isFinite(targetX)?clamp(targetX,board.left+14,board.right-14):board.centerX();
        float y=Float.isFinite(targetY)?clamp(targetY,board.top+14,board.bottom-14):board.centerY();
        int effect=Math.floorMod(kind,6),ink=withAlpha(opaque(color),Math.round(190*fade));
        c.save();c.clipRect(board);
        stroke(b,ink,2.2f);
        float ring=reduced?18:8+eased*25;
        c.drawCircle(x,y,ring,b.paint);
        if(effect==1){
            b.path.reset();b.path.moveTo(x-27,y);b.path.quadTo(x,y-25,x+27,y);b.path.quadTo(x,y+25,x-27,y);
            c.drawPath(b.path,b.paint);c.drawCircle(x,y,5,b.paint);
        }else if(effect==2||effect==4){
            float startX=board.centerX(),startY=board.bottom-12;
            b.path.reset();b.path.moveTo(startX,startY);
            b.path.cubicTo(startX-35,startY-45,x+30,y+30,x,y);c.drawPath(b.path,b.paint);
            for(int n=0;n<4;n++){
                float t=reduced?(n+1)/5f:clamp(eased-n*.12f,0,1);
                float px=startX+(x-startX)*t-(float)Math.sin(t*Math.PI)*14,py=startY+(y-startY)*t;
                fill(b,withAlpha(opaque(color),Math.round((140-n*22)*fade)));c.drawCircle(px,py,3.3f-n*.4f,b.paint);
            }
        }else if(effect==5){
            b.box.set(x-21,y-21,x+21,y+21);c.drawArc(b.box,-25,-255,false,b.paint);
            b.path.reset();b.path.moveTo(x-21,y-3);b.path.lineTo(x-14,y-10);b.path.lineTo(x-10,y-1);c.drawPath(b.path,b.paint);
        }else{
            int count=effect==3?6:3;
            for(int n=0;n<count;n++){
                double angle=-Math.PI*.5+n*Math.PI*2/count;
                float inner=reduced?23:15+eased*16,outer=inner+(effect==3?8:5);
                c.drawLine(x+(float)Math.cos(angle)*inner,y+(float)Math.sin(angle)*inner,
                        x+(float)Math.cos(angle)*outer,y+(float)Math.sin(angle)*outer,b.paint);
            }
        }
        c.restore();return true;
    }

    private static final class Brush {final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);final Path path=new Path();final RectF box=new RectF();}
    private static void fill(Brush b,int color){b.paint.setStyle(Paint.Style.FILL);b.paint.setColor(color);b.paint.setStrokeWidth(1);b.paint.setShader(null);}
    private static void stroke(Brush b,int color,float width){fill(b,color);b.paint.setStyle(Paint.Style.STROKE);b.paint.setStrokeWidth(width);b.paint.setStrokeCap(Paint.Cap.ROUND);b.paint.setStrokeJoin(Paint.Join.ROUND);}
    private static void label(Canvas c,Brush b,String value,float x,float baseline,float size,int color,boolean bold,float width){
        fill(b,color);b.paint.setTypeface(bold?MEDIUM:REGULAR);b.paint.setTextSize(size);b.paint.setTextAlign(Paint.Align.CENTER);
        c.drawText(fit(value,b.paint,width),x,baseline,b.paint);
    }
    private static void labelLeft(Canvas c,Brush b,String value,float x,float baseline,float size,int color,boolean bold,float width){
        fill(b,color);b.paint.setTypeface(bold?MEDIUM:REGULAR);b.paint.setTextSize(size);b.paint.setTextAlign(Paint.Align.LEFT);
        c.drawText(fit(value,b.paint,width),x,baseline,b.paint);
    }
    private static void centeredTwoLines(Canvas c,Brush b,String value,float x,float baseline,float size,int color,float width){
        fill(b,color);b.paint.setTypeface(REGULAR);b.paint.setTextSize(size);
        if(b.paint.measureText(value)<=width){label(c,b,value,x,baseline,size,color,false,width);return;}
        int split=-1;float best=Float.MAX_VALUE;
        for(int n=0;n<value.length();n++)if(value.charAt(n)==' '){
            float a=b.paint.measureText(value.substring(0,n)),d=b.paint.measureText(value.substring(n+1));
            if(Math.max(a,d)<best){best=Math.max(a,d);split=n;}
        }
        if(split>0){label(c,b,value.substring(0,split),x,baseline-size*.45f,size,color,false,width);label(c,b,value.substring(split+1),x,baseline+size*.8f,size,color,false,width);}
        else label(c,b,value,x,baseline,size,color,false,width);
    }
    private static String fit(String value,Paint paint,float width){
        String safe=text(value);if(width<=0)return "";if(paint.measureText(safe)<=width)return safe;
        String ellipsis="…";int n=safe.length();
        while(n>0&&paint.measureText(safe.substring(0,n)+ellipsis)>width)n=safe.offsetByCodePoints(n,-1);
        return n==0?(paint.measureText(ellipsis)<=width?ellipsis:""):safe.substring(0,n)+ellipsis;
    }
    private static String text(String value){return value==null?"":value;}
    private static RectF rect(CompanionBattleLayout.Rect r){return new RectF(r.left,r.top,r.right,r.bottom);}
    private static int opaque(int color){return color|0xff000000;}
    private static int withAlpha(int color,int alpha){return (color&0x00ffffff)|(Math.max(0,Math.min(255,alpha))<<24);}
    private static int mix(int a,int b,float fraction){float f=clamp(fraction,0,1);return 0xff000000|Math.round(((a>>16)&255)*(1-f)+((b>>16)&255)*f)<<16|Math.round(((a>>8)&255)*(1-f)+((b>>8)&255)*f)<<8|Math.round((a&255)*(1-f)+(b&255)*f);}
    private static float clamp(float value,float min,float max){return Math.max(min,Math.min(max,value));}
}
