package com.androidhit.lumadrift.ui;

import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

/** Original luminous toy UI. Paint only: callers retain bounds, input and game state. */
public final class PulseUi124 {
    public static final int WHITE=0xffffffff, BACKGROUND=0xfff3fcff, INK=0xff163b62,
            MUTED=0xff58738c, CYAN=0xff27cbed, BLUE=0xff438dff,
            CORAL=0xffff877d, ORANGE=0xffffbc56, MINT=0xff56d9c1,
            VIOLET=0xffa297ff, LINE=0xffc7eaf7, DISABLED=0xffe0ebf1,
            BLUE_INK=0xff1462b8, CORAL_INK=0xffae3945, ORANGE_INK=0xff855200;
    public static volatile Typeface NUMBER_FACE=Typeface.create("sans-serif",Typeface.BOLD);
    private static Typeface normalFace=Typeface.create("sans-serif",Typeface.NORMAL),strongFace=NUMBER_FACE,mediumFace=NUMBER_FACE;
    private static boolean fontInstalled;
    public static synchronized void install(AssetManager assets){
        if(fontInstalled)return;
        normalFace=font(assets,400);mediumFace=font(assets,600);strongFace=font(assets,800);NUMBER_FACE=font(assets,900);fontInstalled=true;
    }
    private static Typeface font(AssetManager assets,int weight){return new Typeface.Builder(assets,"fonts/Nunito.ttf").setFontVariationSettings("'wght' "+weight).setWeight(weight).build();}
    public static Typeface face(int weight){return weight>=900?NUMBER_FACE:weight>=700?strongFace:weight>=500?mediumFace:normalFace;}
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private final RectF inset=new RectF();
    private final TextPaint numbers=new TextPaint(Paint.ANTI_ALIAS_FLAG);

    public static int mix(int a,int b,float amount){
        float f=Math.max(0,Math.min(1,amount));
        return Color.argb(Math.round(Color.alpha(a)+(Color.alpha(b)-Color.alpha(a))*f),
                Math.round(Color.red(a)+(Color.red(b)-Color.red(a))*f),
                Math.round(Color.green(a)+(Color.green(b)-Color.green(a))*f),
                Math.round(Color.blue(a)+(Color.blue(b)-Color.blue(a))*f));
    }
    public static int alpha(int color,int opacity){return (color&0x00ffffff)|(Math.max(0,Math.min(255,opacity))<<24);}
    /** Old pastel token colors receive the same small, explicit accent family. */
    public static int accent(int color){
        if(color>=0&&color<=3)return new int[]{CORAL,CYAN,BLUE,VIOLET}[color];
        float[] hsv=new float[3];Color.colorToHSV(color,hsv);
        if(hsv[1]<.06f)return CYAN;
        return hsv[0]<18||hsv[0]>=335?CORAL:hsv[0]<65?ORANGE:hsv[0]<170?MINT:hsv[0]<210?CYAN:hsv[0]<270?BLUE:VIOLET;
    }
    private static boolean valid(RectF b){return b!=null&&Float.isFinite(b.left)&&Float.isFinite(b.top)&&Float.isFinite(b.right)&&Float.isFinite(b.bottom)&&b.width()>0&&b.height()>0;}
    private void fill(int color){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);}
    private void stroke(int color,float width){p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setColor(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}

    /** White floating surface, with a crisp cyan edge and a restrained lower rim. */
    public void panel(Canvas c,RectF b,int color){
        if(!valid(b))return;float r=Math.min(b.width()*.09f,b.height()*.22f),edge=Math.max(1,b.width()/250f),drop=Math.min(b.width()*.014f,b.height()*.05f);
        fill(0x151a78b0);c.drawRoundRect(b.left,b.top+drop,b.right,b.bottom+drop,r,r,p);
        fill(WHITE);p.setShader(new LinearGradient(b.left,b.top,b.right,b.bottom,new int[]{WHITE,mix(WHITE,accent(color),.055f)},null,Shader.TileMode.CLAMP));
        c.drawRoundRect(b,r,r,p);p.setShader(null);stroke(LINE,edge);c.drawRoundRect(b,r,r,p);fill(WHITE);
    }
    /** Glossy capsule; no text, icon, hitbox, animation clock or action is owned here. */
    public void action(Canvas c,RectF b,int color,boolean pressed,boolean enabled){
        if(!valid(b))return;int tone=enabled?accent(color):DISABLED;float depth=b.height()*.065f,shift=pressed?depth*.5f:0,r=b.height()*.49f,line=Math.max(1,b.height()*.026f);
        inset.set(b.left,b.top+shift,b.right,b.bottom-depth+shift);
        fill(enabled?alpha(mix(tone,INK,.32f),58):0x1c58738c);c.drawRoundRect(b.left,b.top+depth,b.right,b.bottom,r,r,p);
        fill(WHITE);p.setShader(new LinearGradient(0,inset.top,0,inset.bottom,new int[]{mix(tone,WHITE,.60f),mix(tone,WHITE,.20f),mix(tone,WHITE,.10f)},new float[]{0,.55f,1},Shader.TileMode.CLAMP));
        c.drawRoundRect(inset,r,r,p);p.setShader(null);stroke(enabled?mix(tone,INK,.18f):0xffc5d7e1,line);c.drawRoundRect(inset,r,r,p);
        int save=c.save();c.clipRect(inset.left+line*2,inset.top+line,inset.right-line*2,inset.top+inset.height()*.48f);
        stroke(0xafffffff,line*1.4f);c.drawRoundRect(inset.left+line*2,inset.top+line,inset.right-line*2,inset.bottom-line,r-line,r-line,p);c.restoreToCount(save);fill(WHITE);
    }
    /** Read-only number capsule. Distinct from the filled action face. */
    public void metric(Canvas c,RectF b,int color){
        if(!valid(b))return;int tone=accent(color);float r=b.height()*.48f,line=Math.max(1,b.height()*.023f),drop=b.height()*.045f;
        fill(0x0d1671a0);c.drawRoundRect(b.left,b.top+drop,b.right,b.bottom+drop,r,r,p);
        fill(WHITE);c.drawRoundRect(b,r,r,p);stroke(mix(tone,WHITE,.40f),line);c.drawRoundRect(b,r,r,p);
        fill(mix(tone,WHITE,.84f));c.drawOval(b.left+5,b.top+5,b.left+Math.min(b.height()-5,b.width()*.32f),b.bottom-5,p);fill(WHITE);
    }
    public void focus(Canvas c,RectF b){if(!valid(b))return;float line=Math.max(2,b.height()*.04f);stroke(BLUE,line);c.drawRoundRect(b.left-line,b.top-line,b.right+line,b.bottom+line,b.height()*.5f,b.height()*.5f,p);fill(WHITE);}
    public void number(Canvas c,String value,RectF b,float requestedSize,float minimumSize,int color){
        if(!valid(b)||value==null)return;numbers.setTypeface(NUMBER_FACE);numbers.setColor(color);numbers.setTextAlign(Paint.Align.CENTER);
        float size=Math.max(minimumSize,requestedSize);numbers.setTextSize(size);float width=numbers.measureText(value);
        if(width>b.width())numbers.setTextSize(Math.max(minimumSize,size*b.width()/width));
        String shown=TextUtils.ellipsize(value,numbers,b.width(),TextUtils.TruncateAt.END).toString();Paint.FontMetrics f=numbers.getFontMetrics();
        c.drawText(shown,b.centerX(),b.centerY()-(f.ascent+f.descent)*.5f,numbers);
    }
    public void orb(Canvas c,RectF b,int color,boolean pressed){
        if(!valid(b))return;int save=c.save();float r=Math.min(b.width(),b.height())*.5f;
        c.translate(b.centerX(),b.centerY()+(pressed?1:0));fill(0x1726678b);c.drawOval(-r,r*.72f,r,r*1.08f,p);
        fill(WHITE);p.setShader(new LinearGradient(0,-r,0,r,new int[]{WHITE,mix(accent(color),WHITE,.64f)},null,Shader.TileMode.CLAMP));c.drawCircle(0,0,r*.93f,p);p.setShader(null);
        stroke(mix(accent(color),WHITE,.22f),Math.max(1,r*.045f));c.drawCircle(0,0,r*.93f,p);fill(0xaaffffff);c.drawOval(-r*.55f,-r*.77f,r*.35f,-r*.43f,p);c.restoreToCount(save);fill(WHITE);
    }
    /** Crisp original symbols shared by Canvas and real Android Button views. */
    public void symbol(Canvas c,RectF b,ScenicUi.Kind kind,int color){
        if(!valid(b))return;int save=c.save();c.translate(b.centerX(),b.centerY());float s=Math.min(b.width(),b.height())/32f;c.scale(s,s);
        stroke(color,2.5f);path.reset();
        switch(kind==null?ScenicUi.Kind.STARS:kind){
            case PLAY: case PORTAL:path.moveTo(-5,-9);path.lineTo(9,0);path.lineTo(-5,9);path.close();fill(color);c.drawPath(path,p);break;
            case RETURN: case UNDO:path.moveTo(-3,-8);path.lineTo(-11,0);path.lineTo(-3,8);path.moveTo(-10,0);path.lineTo(10,0);c.drawPath(path,p);break;
            case CLOSE: case CANCEL:path.moveTo(-7,-7);path.lineTo(7,7);path.moveTo(7,-7);path.lineTo(-7,7);c.drawPath(path,p);break;
            case PAUSE:c.drawLine(-5,-8,-5,8,p);c.drawLine(5,-8,5,8,p);break;
            case CONFIRM:path.moveTo(-9,0);path.lineTo(-3,6);path.lineTo(10,-7);c.drawPath(path,p);break;
            case RETRY: case ROTATE:c.drawArc(-9,-9,9,9,-55,290,false,p);path.moveTo(4,-10);path.lineTo(9,-8);path.lineTo(9,-14);c.drawPath(path,p);break;
            case ZOOM_IN: case ZOOM_OUT:c.drawCircle(-2,-2,8,p);c.drawLine(4,4,11,11,p);c.drawLine(-6,-2,2,-2,p);if(kind==ScenicUi.Kind.ZOOM_IN)c.drawLine(-2,-6,-2,2,p);break;
            case COIN:c.drawCircle(0,0,10,p);c.drawLine(-2,-5,3,-5,p);c.drawLine(3,-5,-3,5,p);c.drawLine(-3,5,2,5,p);break;
            case HEART:path.moveTo(0,10);path.cubicTo(-20,-1,-8,-17,0,-6);path.cubicTo(8,-17,20,-1,0,10);c.drawPath(path,p);break;
            case LANTERN:path.moveTo(2,-12);path.lineTo(-8,2);path.lineTo(-1,2);path.lineTo(-3,12);path.lineTo(9,-3);path.lineTo(2,-3);path.close();fill(color);c.drawPath(path,p);break;
            case HOUSE:path.moveTo(-11,-1);path.lineTo(0,-10);path.lineTo(11,-1);path.moveTo(-8,-3);path.lineTo(-8,10);path.lineTo(8,10);path.lineTo(8,-3);path.moveTo(-2,10);path.lineTo(-2,2);path.lineTo(3,2);path.lineTo(3,10);c.drawPath(path,p);break;
            case PROFILE:c.drawCircle(0,-5,5,p);c.drawArc(-10,1,10,18,185,170,false,p);break;
            case MIRROR:c.drawOval(-7,-11,7,6,p);c.drawLine(0,6,0,11,p);c.drawLine(-5,11,5,11,p);c.drawLine(-3,-6,3,-3,p);break;
            case EYE:c.drawOval(-10,-7,10,7,p);c.drawCircle(0,0,3,p);break;
            case CLOTH_RAIL:path.moveTo(-4,-9);path.lineTo(-10,-6);path.lineTo(-7,0);path.lineTo(-4,-1);path.lineTo(-4,10);path.lineTo(5,10);path.lineTo(5,-1);path.lineTo(8,0);path.lineTo(11,-6);path.lineTo(5,-9);path.quadTo(0,-3,-4,-9);c.drawPath(path,p);break;
            case BLUEPRINT:c.drawRoundRect(-10,-8,10,8,2,2,p);c.drawLine(-5,-4,5,-4,p);c.drawLine(-5,0,2,0,p);c.drawLine(-5,4,-1,4,p);c.drawLine(4,10,12,2,p);break;
            case BOOK: case HELP:path.moveTo(0,-7);path.quadTo(-6,-11,-11,-8);path.lineTo(-11,9);path.quadTo(-6,6,0,10);path.quadTo(6,6,11,9);path.lineTo(11,-8);path.quadTo(6,-11,0,-7);path.lineTo(0,10);c.drawPath(path,p);break;
            case SATCHEL:c.drawRoundRect(-10,-5,10,10,4,4,p);c.drawArc(-5,-12,5,1,180,180,false,p);c.drawLine(-2,1,2,1,p);break;
            case CHEST:c.drawRoundRect(-11,-3,11,10,3,3,p);c.drawArc(-11,-12,11,6,180,180,false,p);c.drawLine(-10,1,10,1,p);c.drawRoundRect(-2,-1,2,5,1,1,p);break;
            case CAPSULE:c.save();c.rotate(28);c.drawRoundRect(-7,-12,7,12,7,7,p);c.drawLine(-7,0,7,0,p);c.drawLine(-3,-7,-3,-3,p);c.restore();break;
            case SHIELD: case DUMMY:path.moveTo(0,-11);path.lineTo(9,-7);path.lineTo(8,3);path.quadTo(6,8,0,12);path.quadTo(-6,8,-8,3);path.lineTo(-9,-7);path.close();c.drawPath(path,p);break;
            case GEAR: case DIAL:c.drawCircle(0,0,7,p);c.drawCircle(0,0,2,p);for(int i=0;i<8;i++){c.save();c.rotate(i*45);c.drawLine(0,-8,0,-11,p);c.restore();}break;
            case KEY:c.drawCircle(-4,-4,6,p);c.drawLine(0,0,9,9,p);c.drawLine(5,5,8,2,p);break;
            case COMPASS:c.drawCircle(0,0,11,p);path.moveTo(4,-6);path.lineTo(1,2);path.lineTo(-5,6);path.lineTo(-1,-2);path.close();c.drawPath(path,p);break;
            case LEAF:path.moveTo(-9,9);path.quadTo(-13,-11,10,-11);path.quadTo(13,10,-9,9);path.moveTo(-9,9);path.lineTo(5,-5);c.drawPath(path,p);break;
            case FEATHER:path.moveTo(-10,11);path.lineTo(7,-7);path.moveTo(-7,7);path.quadTo(-13,-3,1,-10);path.quadTo(15,-16,10,-2);path.quadTo(5,9,-7,7);c.drawPath(path,p);break;
            case FLAG:path.moveTo(-8,12);path.lineTo(-8,-12);path.moveTo(-8,-10);path.quadTo(-2,-13,3,-9);path.quadTo(7,-6,12,-9);path.lineTo(12,3);path.quadTo(6,6,1,2);path.quadTo(-3,-1,-8,2);c.drawPath(path,p);break;
            case MEDAL:c.drawCircle(0,-3,7,p);path.moveTo(-5,3);path.lineTo(-7,12);path.lineTo(0,8);path.lineTo(7,12);path.lineTo(5,3);c.drawPath(path,p);break;
            case LANGUAGE:c.drawCircle(0,0,11,p);c.drawOval(-5,-11,5,11,p);c.drawLine(-10,-3,10,-3,p);c.drawLine(-10,3,10,3,p);break;
            case EXPAND:path.moveTo(-10,-3);path.lineTo(-10,-10);path.lineTo(-3,-10);path.moveTo(3,-10);path.lineTo(10,-10);path.lineTo(10,-3);path.moveTo(10,3);path.lineTo(10,10);path.lineTo(3,10);path.moveTo(-3,10);path.lineTo(-10,10);path.lineTo(-10,3);c.drawPath(path,p);break;
            case MAIL:c.drawRoundRect(-11,-8,11,8,3,3,p);path.moveTo(-10,-6);path.lineTo(0,2);path.lineTo(10,-6);c.drawPath(path,p);break;
            case MUSIC: case MUTE:path.moveTo(-3,5);path.lineTo(-3,-8);path.lineTo(8,-10);path.lineTo(8,3);c.drawPath(path,p);c.drawOval(-9,3,-3,8,p);if(kind==ScenicUi.Kind.MUSIC)c.drawOval(2,1,8,6,p);else c.drawLine(-11,-11,11,11,p);break;
            default:for(int i=0;i<8;i++){double a=-Math.PI/2+i*Math.PI/4;float r=(i&1)==0?11:4;float x=(float)Math.cos(a)*r,y=(float)Math.sin(a)*r;if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}path.close();fill(color);c.drawPath(path,p);break;
        }
        c.restoreToCount(save);fill(WHITE);
    }
}
