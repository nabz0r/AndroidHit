package com.androidhit.lumadrift.ui;

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
import java.util.Locale;

/**
 * Original world objects for 1.17's navigation. These are illustrations, not
 * control backgrounds: no pill, tab bar or rectangular action surface is drawn.
 * The caller owns actions, saved state, 48 dp minimum hit areas and accessibility.
 * Keep one instance on the UI thread; drawing never advances transaction state.
 */
public final class ScenicUi {
    public enum Kind {
        BOOK, PORTAL, HOUSE, MIRROR, DUMMY, BLUEPRINT, KEY, CAPSULE, CHEST,
        GEAR, DIAL, FEATHER, CLOTH_RAIL, SATCHEL, FLAG, MEDAL, HEART,
        COMPASS, LANTERN, COIN, RETURN, CLOSE, ZOOM_IN, ZOOM_OUT, ROTATE,
        CONFIRM, CANCEL, UNDO, EXPAND, LEAF, MUSIC, MUTE, HELP, SHIELD,
        EYE, LANGUAGE, PROFILE, STARS, PLAY, PAUSE, RETRY, MAIL
    }

    public static final int INK=0xff263049, CREAM=0xfffff7e6, GOLD=0xffffcf79,
            TEAL=0xff31bda9, CORAL=0xfff38f9d, VIOLET=0xff8672d4;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint text=new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private final RectF area=new RectF();
    private float minimumTextSize=11f;
    private ArtLibrary registeredArt;

    public void setMinimumTextSize(float size) {
        minimumTextSize=Float.isFinite(size)?Math.max(11f,Math.min(30f,size)):11f;
    }

    /** An explicit semantic mapping is preferable to deriving intent from translated labels. */
    public static Kind kindFor(String action) {
        String key=action==null?"":action.toLowerCase(Locale.ROOT);
        if(key.contains("zoom_in")||key.equals("plus"))return Kind.ZOOM_IN;
        if(key.contains("zoom_out")||key.equals("minus"))return Kind.ZOOM_OUT;
        if(key.contains("overview")||key.contains("expand"))return Kind.EXPAND;
        if(key.contains("rotate"))return Kind.ROTATE;
        if(key.contains("retry")||key.contains("replay"))return Kind.RETRY;
        if(key.contains("undo"))return Kind.UNDO;
        if(key.contains("back")||key.contains("return"))return Kind.RETURN;
        if(key.contains("cancel")||key.contains("remove"))return Kind.CANCEL;
        if(key.contains("close")||key.contains("dismiss"))return Kind.CLOSE;
        if(key.contains("confirm")||key.contains("save")||key.contains("equip")||key.contains("place"))return Kind.CONFIRM;
        if(key.contains("badge")||key.contains("medal"))return Kind.MEDAL;
        if(key.contains("flag"))return Kind.FLAG;
        if(key.contains("profile")||key.contains("nickname"))return Kind.PROFILE;
        if(key.contains("wardrobe")||key.contains("outfit")||key.contains("dress")||key.contains("cloth"))return Kind.CLOTH_RAIL;
        if(key.contains("appearance")||key.contains("style")||key.contains("skin"))return Kind.MIRROR;
        if(key.contains("summon")||key.contains("draw")||key.contains("loot"))return Kind.CAPSULE;
        if(key.contains("reward")||key.contains("claim")||key.contains("chest"))return Kind.CHEST;
        if(key.contains("adventure")||key.contains("map")||key.contains("world"))return Kind.COMPASS;
        if(key.contains("house")||key.contains("home")||key.contains("room"))return Kind.HOUSE;
        if(key.contains("garden"))return Kind.LEAF;
        if(key.contains("decor")||key.contains("furniture")||key.contains("atelier"))return Kind.BLUEPRINT;
        if(key.contains("companion")||key.contains("character"))return Kind.PORTAL;
        if(key.contains("cuddle")||key.contains("heart"))return Kind.HEART;
        if(key.contains("buy")||key.contains("coin")||key.contains("wallet")||key.contains("price"))return Kind.COIN;
        if(key.contains("equipment")||key.contains("shield"))return Kind.SHIELD;
        if(key.contains("collection")||key.contains("catalog")||key.contains("pool")||key.contains("odds"))return Kind.BOOK;
        if(key.contains("language"))return Kind.LANGUAGE;
        if(key.contains("mute"))return Kind.MUTE;
        if(key.contains("music")||key.contains("sound"))return Kind.MUSIC;
        if(key.contains("setting")||key.contains("option"))return Kind.GEAR;
        if(key.contains("help")||key.contains("tutorial"))return Kind.HELP;
        if(key.contains("pause"))return Kind.PAUSE;
        if(key.contains("play")||key.contains("start")||key.contains("next")||key.contains("continue"))return Kind.PLAY;
        if(key.contains("mail")||key.contains("account"))return Kind.MAIL;
        if(key.contains("view")||key.contains("preview"))return Kind.EYE;
        if(key.contains("edit")||key.contains("name"))return Kind.FEATHER;
        if(key.contains("inventory")||key.contains("bag")||key.contains("shop"))return Kind.SATCHEL;
        return Kind.LANTERN;
    }

    public void objectAction(Canvas c,RectF box,String label,int accent,boolean pressed,String kind) {
        Kind value;
        try { value=Kind.valueOf(kind==null?"LANTERN":kind.toUpperCase(Locale.ROOT)); }
        catch(IllegalArgumentException e) { value=kindFor(kind); }
        objectAction(c,box,label,accent,pressed,value);
    }

    /** Object plus free-standing caption. Labels are optional and never get a button plate. */
    public void objectAction(Canvas c,RectF box,String label,int accent,boolean pressed,Kind kind) {
        if(!valid(box))return;
        if(label==null||label.trim().length()==0){object(c,box,accent,pressed,kind);return;}
        float textSize=Math.max(minimumTextSize,box.height()<70?13f:14f);
        if(box.width()>box.height()*1.65f) {
            float size=Math.min(box.height(),66f);
            area.set(box.left,box.centerY()-size*.5f,box.left+size,box.centerY()+size*.5f);
            object(c,area,accent,pressed,kind);
            caption(c,label,box.left+size+5,box.centerY()+textSize*.34f,
                    Math.max(1,box.width()-size-7),textSize,false);
        } else {
            float captionSpace=Math.max(20,textSize*1.5f);
            area.set(box.left,box.top,box.right,Math.max(box.top+1,box.bottom-captionSpace));
            object(c,area,accent,pressed,kind);
            caption(c,label,box.centerX(),box.bottom-4,box.width()-2,textSize,true);
        }
    }

    /** Draws a complete object inside the supplied art box, with no text or hit registration. */
    public void object(Canvas c,RectF box,int accent,boolean pressed,Kind kind) {
        if(!valid(box))return;
        int color=tone(accent);
        float scale=Math.min(box.width(),box.height())/112f;
        c.save();c.translate(box.centerX(),box.centerY()+(pressed?2*scale:0));
        c.scale(scale*(pressed?.94f:1),scale*(pressed?.94f:1));
        ellipse(c,-39,39,39,49,0x20263049);
        if(rasterObject(c,kind)){c.restore();return;}
        switch(kind==null?Kind.LANTERN:kind) {
            case BOOK: book(c,color,false);break;
            case HELP: book(c,color,true);break;
            case PORTAL: portal(c,color);break;
            case HOUSE: house(c,color);break;
            case MIRROR: mirror(c,color,false);break;
            case EYE: mirror(c,color,true);break;
            case DUMMY: dummy(c,color);break;
            case BLUEPRINT: blueprint(c,color);break;
            case KEY: key(c,color);break;
            case CAPSULE: capsule(c,color);break;
            case CHEST: chest(c,color);break;
            case GEAR: gear(c,color);break;
            case DIAL: dialObject(c,color,.66f);break;
            case FEATHER: feather(c,color);break;
            case CLOTH_RAIL: rail(c,color);break;
            case SATCHEL: satchel(c,color);break;
            case FLAG: flag(c,color,false);break;
            case LANGUAGE: flag(c,color,true);break;
            case MEDAL: medal(c,color);break;
            case HEART: heart(c,color);break;
            case COMPASS: compass(c,color);break;
            case COIN: coin(c,color);break;
            case RETURN: ribbon(c,color,-1,false);break;
            case CLOSE: foldedWing(c,color,true);break;
            case CANCEL: foldedWing(c,color,false);break;
            case ZOOM_IN: lens(c,color,true);break;
            case ZOOM_OUT: lens(c,color,false);break;
            case ROTATE: turntable(c,color,false);break;
            case UNDO: ribbon(c,color,-1,true);break;
            case RETRY: turntable(c,color,true);break;
            case CONFIRM: seal(c,color);break;
            case EXPAND: telescope(c,color);break;
            case LEAF: leaf(c,color);break;
            case MUSIC: music(c,color,false);break;
            case MUTE: music(c,color,true);break;
            case SHIELD: shield(c,color);break;
            case PROFILE: profile(c,color);break;
            case STARS: stars(c,color);break;
            case PLAY: sailboat(c,color);break;
            case PAUSE: hourglass(c,color);break;
            case MAIL: mail(c,color);break;
            default: lantern(c,color);break;
        }
        c.restore();
    }

    /** The main destinations use the same original painted assets as the cast and home. */
    private boolean rasterObject(Canvas c,Kind kind) {
        ArtLibrary library=CharacterSprites.library();
        if(library==null)return false;
        if(registeredArt!=library) {
            if(!library.contains("action/portal"))ArtRegions117.register(library,ArtLibrary.ROOT+"actions-0.png",
                    "action/portal","action/book","action/chest","action/capsule","action/house","action/wardrobe");
            if(!library.contains("action/badge"))ArtRegions117.register(library,ArtLibrary.ROOT+"actions-1.png",
                    "action/badge","action/mirror","action/mannequin","action/blueprint","action/map","action/settings");
            registeredArt=library;
        }
        String key;
        if(kind==null)return false;
        switch(kind) {
            case PORTAL:key="portal";break;
            case BOOK:key="book";break;
            case CHEST:key="chest";break;
            case CAPSULE:key="capsule";break;
            case HOUSE:key="house";break;
            case CLOTH_RAIL:key="wardrobe";break;
            case MEDAL:key="badge";break;
            case MIRROR:key="mirror";break;
            case DUMMY:key="mannequin";break;
            case BLUEPRINT:key="blueprint";break;
            case COMPASS:key="map";break;
            case GEAR:key="settings";break;
            default:return false;
        }
        return library.draw(c,"action/"+key,-53,-53,53,45,.5f,1,255);
    }

    /** A visible object selector. The active state lights its floor, not an enclosing chip. */
    public void selector(Canvas c,RectF box,int accent,boolean selected,Kind kind) {
        if(!valid(box))return;
        if(selected) {
            float y=box.bottom-3,r=Math.min(box.width()*.32f,26);
            stroke(c,tone(accent),2.2f);c.drawArc(box.centerX()-r,y-r*.22f,box.centerX()+r,y+r*.22f,7,166,false,p);
            sparkle(c,box.centerX()-r-4,y-2,3,GOLD);
            sparkle(c,box.centerX()+r+4,y-2,3,GOLD);
        }
        object(c,box,accent,false,kind);
    }

    /** Objects float above a quiet ground shadow; captions stay on clear background. */
    public void shelf(Canvas c,RectF box,int accent) {
        if(!valid(box))return;
        fill(0x14746283);
        c.drawOval(box.left+9,box.bottom+3,box.right-9,box.bottom+12,p);
    }

    public void dial(Canvas c,RectF box,int accent,float value,boolean pressed) {
        if(!valid(box))return;
        c.save();c.translate(box.centerX(),box.centerY());
        float s=Math.min(box.width(),box.height())/112f*(pressed?.95f:1);c.scale(s,s);
        dialObject(c,tone(accent),Float.isFinite(value)?Math.max(0,Math.min(1,value)):0);
        c.restore();
    }

    /** Readable fabric/parchment window for a modal; deliberately not an action silhouette. */
    public void windowModal(Canvas c,RectF box,int accent) {
        if(!valid(box))return;
        float w=box.width(),h=box.height(),fold=Math.min(22,w*.07f),y=box.top;
        path.reset();path.moveTo(box.left+fold,y+5);
        path.cubicTo(box.left+w*.35f,y-3,box.right-w*.3f,y+1,box.right-fold,y+5);
        path.lineTo(box.right-3,y+h*.3f);path.quadTo(box.right+2,y+h*.6f,box.right-fold*.4f,box.bottom-fold);
        path.quadTo(box.right-w*.22f,box.bottom+3,box.centerX(),box.bottom-2);
        path.quadTo(box.left+w*.17f,box.bottom+2,box.left+fold*.4f,box.bottom-fold);
        path.quadTo(box.left-2,y+h*.57f,box.left+3,y+h*.31f);path.close();
        c.save();c.translate(0,7);fill(0x37263049);c.drawPath(path,p);c.restore();
        fill(CREAM);p.setShader(new LinearGradient(box.left,y,box.right,box.bottom,0xfffffcf0,0xffffefdd,Shader.TileMode.CLAMP));c.drawPath(path,p);p.setShader(null);
        line(c,box.left+fold,y+5,box.right-fold,y+5,mix(tone(accent),CREAM,.45f),3);
        circle(c,box.left+fold*.6f,y+5,fold*.3f,GOLD);circle(c,box.right-fold*.6f,y+5,fold*.3f,GOLD);
        line(c,box.left+fold*.6f,y+5,box.left+fold*.6f,y+26,mix(tone(accent),INK,.2f),2);
        line(c,box.right-fold*.6f,y+5,box.right-fold*.6f,y+26,mix(tone(accent),INK,.2f),2);
        sparkle(c,box.centerX(),box.top+6,5,tone(accent));
    }

    /** A static, learnable gesture cue; caller decides when it is needed and owns its copy. */
    public void gestureCue(Canvas c,RectF box,boolean horizontal,int accent) {
        if(!valid(box))return;
        float x=box.centerX(),y=box.centerY(),span=Math.min(horizontal?box.width():box.height(),60)*.34f;
        int ink=mix(tone(accent),INK,.28f);
        line(c,horizontal?x-span:x,horizontal?y:y-span,horizontal?x+span:x,horizontal?y:y+span,0xbfffffff,5);
        line(c,horizontal?x-span:x,horizontal?y:y-span,horizontal?x+span:x,horizontal?y:y+span,ink,1.8f);
        c.save();c.translate(x,y);c.rotate(horizontal?-14:75);
        fill(CREAM);path.reset();path.moveTo(-3,5);path.lineTo(-3,-10);path.cubicTo(-3,-16,4,-16,4,-10);
        path.lineTo(4,-1);path.cubicTo(8,-4,10,-1,10,3);path.cubicTo(15,1,17,7,13,12);
        path.lineTo(10,18);path.quadTo(1,22,-4,15);path.lineTo(-10,8);path.quadTo(-12,2,-6,4);path.close();c.drawPath(path,p);
        stroke(c,ink,1.2f);c.drawPath(path,p);line(c,-1,16,10,16,tone(accent),3);c.restore();
    }

    private void caption(Canvas c,String label,float x,float baseline,float width,float size,boolean centered) {
        if(width<=0)return;
        text.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));text.setTextSize(size);
        text.setTextAlign(Paint.Align.LEFT);text.setShader(null);
        String value=TextUtils.ellipsize(label,text,width,TextUtils.TruncateAt.END).toString();
        float left=centered?x-text.measureText(value)*.5f:x;
        text.setColor(0xe6fff9ef);text.setStyle(Paint.Style.STROKE);text.setStrokeJoin(Paint.Join.ROUND);text.setStrokeWidth(3.1f);
        c.drawText(value,left,baseline,text);text.setStyle(Paint.Style.FILL);text.setColor(INK);c.drawText(value,left,baseline,text);
    }

    private void book(Canvas c,int a,boolean help) {
        polygon(c,new float[]{-40,-27,-7,-34,1,-25,36,-33,42,29,4,37,-3,30,-37,36},mix(a,INK,.5f));
        polygon(c,new float[]{-38,-31,-8,-35,1,-28,34,-35,38,24,4,32,-3,26,-35,32},a);
        polygon(c,new float[]{-31,-29,-8,-29,-3,-23,-3,23,-29,25},CREAM);
        polygon(c,new float[]{3,-23,28,-29,31,20,4,26},0xffffe5bb);
        line(c,-2,-23,0,27,mix(a,INK,.35f),2);
        for(int i=0;i<4;i++){line(c,-25,-17+i*9,-10,-18+i*9,0xffc7aaa0,1.3f);line(c,9,-12+i*9,25,-16+i*9,0xffc7aaa0,1.3f);}
        polygon(c,new float[]{18,17,25,15,27,40,22,35,18,41},CORAL);
        if(help){circle(c,13,-8,9,a);line(c,10,-11,15,-11,CREAM,2);line(c,15,-11,12,-6,CREAM,2);circle(c,12,-2,1.3f,CREAM);}
        else sparkle(c,-18,-10,7,GOLD);
    }

    private void portal(Canvas c,int a) {
        path.reset();path.moveTo(-39,40);path.lineTo(-39,-9);path.cubicTo(-39,-58,39,-58,39,-9);path.lineTo(39,40);path.close();
        fill(mix(a,INK,.46f));c.drawPath(path,p);
        path.reset();path.moveTo(-30,37);path.lineTo(-30,-7);path.cubicTo(-30,-45,30,-45,30,-7);path.lineTo(30,37);path.close();
        fill(a);p.setShader(new LinearGradient(-30,-35,30,38,mix(a,CREAM,.5f),mix(a,INK,.2f),Shader.TileMode.CLAMP));c.drawPath(path,p);p.setShader(null);
        line(c,-35,-5,-35,34,GOLD,3);line(c,35,-5,35,34,GOLD,3);
        ellipse(c,-43,33,43,47,0xffc7a398);ellipse(c,-34,31,34,39,CREAM);
        star(c,2,-6,13,GOLD);sparkle(c,-18,17,5,CREAM);sparkle(c,18,-23,5,CREAM);
        circle(c,-18,-31,3,GOLD);circle(c,18,-31,3,GOLD);
    }

    private void house(Canvas c,int a) {
        polygon(c,new float[]{-35,-10,9,-28,37,-5,34,34,-7,43,-37,25},0xffffe9cf);
        polygon(c,new float[]{9,-28,37,-5,34,34,8,23},mix(a,CREAM,.55f));
        polygon(c,new float[]{-46,-9,-5,-44,10,-43,46,-9,35,2,3,-27,-34,2},mix(a,INK,.3f));
        polygon(c,new float[]{-44,-15,-7,-48,7,-45,41,-13,34,-6,1,-34,-34,-5},a);
        for(int i=0;i<3;i++)line(c,-29+i*10,-18-i*8,-18+i*10,-8-i*8,mix(a,CREAM,.35f),2);
        round(c,-18,4,1,35,8,0xff86617b);round(c,-15,5,-1,34,7,0xffc998aa);circle(c,-5,21,2,GOLD);
        round(c,17,3,29,17,3,0xff779bbc);line(c,23,4,23,16,CREAM,2);line(c,18,10,28,10,CREAM,2);
        ellipse(c,-27,32,3,41,0xffb4a082);leafShape(c,28,26,10,TEAL);circle(c,29,23,5,CORAL);
    }

    private void mirror(Canvas c,int a,boolean eye) {
        line(c,0,24,0,40,0xffaa8274,7);ellipse(c,-27,36,27,45,0xffa37b77);
        ellipse(c,-32,-47,32,29,mix(a,INK,.35f));ellipse(c,-28,-44,28,25,GOLD);
        ellipse(c,-23,-39,23,20,0xffb3e2e7);
        polygon(c,new float[]{-20,-19,7,-37,15,-30,-17,7},0x77ffffff);
        line(c,14,-29,20,-34,CREAM,2);sparkle(c,25,-33,6,CREAM);
        if(eye){ellipse(c,-17,-17,17,2,CREAM);circle(c,0,-8,7,a);circle(c,0,-8,3,INK);circle(c,-2,-10,1.2f,CREAM);}
        else {ellipse(c,-10,-23,10,-2,mix(a,INK,.25f));path.reset();path.moveTo(-17,12);path.cubicTo(-17,-5,17,-5,17,12);path.close();fill(a);c.drawPath(path,p);}
    }

    private void dummy(Canvas c,int a) {
        line(c,0,17,0,40,0xff997778,5);line(c,0,38,-21,45,0xff997778,4);line(c,0,38,21,45,0xff997778,4);
        round(c,-5,-43,5,-31,4,0xff9e7b71);
        path.reset();path.moveTo(-13,-34);path.lineTo(13,-34);path.cubicTo(15,-20,27,-23,24,-10);
        path.cubicTo(18,1,15,1,22,21);path.quadTo(0,32,-22,21);path.cubicTo(-15,1,-18,1,-24,-10);path.cubicTo(-27,-23,-15,-20,-13,-34);path.close();
        fill(a);p.setShader(new LinearGradient(-24,-30,24,28,mix(a,CREAM,.5f),a,Shader.TileMode.CLAMP));c.drawPath(path,p);p.setShader(null);
        line(c,-18,1,18,1,mix(a,INK,.35f),3);line(c,0,-27,0,20,mix(a,CREAM,.6f),1.3f);
        for(int i=0;i<4;i++)circle(c,0,-17+i*9,1.2f,GOLD);
    }

    private void blueprint(Canvas c,int a) {
        polygon(c,new float[]{-40,-26,31,-37,40,25,-28,38},mix(a,INK,.2f));
        polygon(c,new float[]{-35,-29,29,-34,35,25,-27,34},a);
        for(int i=0;i<4;i++){line(c,-27,-20+i*11,28,-25+i*11,0x45ffffff,1);line(c,-20+i*12,-27,-14+i*12,28,0x45ffffff,1);}
        line(c,-18,3,1,-17,CREAM,2);line(c,1,-17,20,-1,CREAM,2);line(c,-14,2,-12,20,CREAM,2);line(c,-12,20,19,16,CREAM,2);line(c,19,16,17,1,CREAM,2);line(c,-1,18,-2,5,CREAM,2);line(c,-2,5,6,4,CREAM,2);line(c,6,4,7,17,CREAM,2);
        line(c,-28,36,39,-35,0xff966876,7);line(c,-28,34,37,-37,GOLD,5);polygon(c,new float[]{35,-40,44,-45,42,-35},CREAM);circle(c,-28,34,3,CORAL);
    }

    private void key(Canvas c,int a) {
        c.save();c.rotate(-32);line(c,0,-5,0,36,0xffae7d52,10);line(c,-1,-8,-1,33,GOLD,7);
        line(c,0,20,14,20,0xffae7d52,8);line(c,0,30,10,30,0xffae7d52,8);line(c,-1,18,12,18,GOLD,5);line(c,-1,28,8,28,GOLD,5);
        circle(c,0,-23,21,0xffae7d52);circle(c,0,-25,20,GOLD);circle(c,0,-25,11,mix(a,CREAM,.5f));
        circle(c,-7,-35,3,CREAM);star(c,0,-25,7,a);c.restore();
        sparkle(c,33,-24,5,CREAM);
    }

    private void capsule(Canvas c,int a) {
        circle(c,0,-5,41,mix(a,INK,.36f));circle(c,0,-8,39,a);
        c.save();path.reset();path.addCircle(0,-8,37,Path.Direction.CW);c.clipPath(path);
        ellipse(c,-42,-55,40,-5,mix(a,CREAM,.45f));polygon(c,new float[]{-42,-2,42,-2,42,41,-42,41},mix(a,INK,.18f));
        line(c,-39,-2,39,-2,GOLD,5);ellipse(c,-23,-37,-3,-25,0xaaffffff);circle(c,-26,-18,3,0xaaffffff);c.restore();
        round(c,-10,-12,10,15,6,GOLD);star(c,0,0,6,mix(a,INK,.2f));
        sparkle(c,-43,-29,6,GOLD);sparkle(c,42,18,4,CREAM);
    }

    private void chest(Canvas c,int a) {
        round(c,-38,-5,38,35,8,0xff986b70);round(c,-36,-9,36,29,7,0xffe4b693);
        round(c,-43,-36,43,-1,10,mix(a,INK,.25f));round(c,-40,-40,40,-7,11,a);
        round(c,-34,-34,34,-12,7,mix(a,CREAM,.24f));
        round(c,-27,-39,-19,28,2,GOLD);round(c,19,-39,27,28,2,GOLD);
        round(c,-10,-14,10,9,4,0xffbc8a53);round(c,-8,-16,8,6,4,GOLD);star(c,0,-5,4,mix(a,INK,.3f));
        circle(c,-30,19,2,0xffa67b74);circle(c,30,19,2,0xffa67b74);sparkle(c,38,-43,6,CREAM);
    }

    private void gear(Canvas c,int a) {
        for(int i=0;i<8;i++){c.save();c.rotate(i*45);round(c,-7,-44,7,-25,3,mix(a,INK,.3f));round(c,-6,-44,6,-28,2,GOLD);c.restore();}
        circle(c,0,0,35,mix(a,INK,.38f));circle(c,0,-2,33,a);circle(c,0,-2,24,GOLD);circle(c,0,-2,17,mix(a,INK,.25f));circle(c,0,-2,11,CREAM);
        line(c,-21,-23,-12,-29,0xafffffff,3);circle(c,25,14,3,CREAM);
    }

    private void dialObject(Canvas c,int a,float value) {
        circle(c,0,0,39,0xff936b69);circle(c,0,-3,38,GOLD);circle(c,0,-3,31,CREAM);
        for(int i=0;i<9;i++){double t=(i*30+150)*Math.PI/180;line(c,(float)Math.cos(t)*24,-3+(float)Math.sin(t)*24,(float)Math.cos(t)*28,-3+(float)Math.sin(t)*28,0xff947786,2);}
        double angle=(150+240*value)*Math.PI/180;line(c,0,-3,(float)Math.cos(angle)*23,-3+(float)Math.sin(angle)*23,a,4);circle(c,0,-3,6,a);circle(c,-2,-5,1.8f,CREAM);
    }

    private void feather(Canvas c,int a) {
        path.reset();path.moveTo(-24,35);path.cubicTo(-27,1,-14,-46,32,-46);path.cubicTo(41,-25,18,5,-24,35);path.close();
        fill(a);p.setShader(new LinearGradient(-26,35,28,-45,mix(a,INK,.25f),mix(a,CREAM,.45f),Shader.TileMode.CLAMP));c.drawPath(path,p);p.setShader(null);
        line(c,-30,44,24,-35,GOLD,3);
        for(int i=0;i<5;i++){float x=-19+i*7,y=25-i*11;line(c,x,y,x-4,y-13,mix(a,CREAM,.45f),1.4f);}
        ellipse(c,17,24,42,42,0xff94728e);ellipse(c,19,21,40,34,0xffbda3ce);ellipse(c,24,22,35,29,INK);sparkle(c,-32,-12,5,CREAM);
    }

    private void rail(Canvas c,int a) {
        line(c,-39,-29,39,-29,0xffb5907d,5);line(c,-35,-29,-35,39,0xffa17b72,4);line(c,35,-29,35,39,0xffa17b72,4);
        line(c,-44,41,-27,41,0xffa17b72,4);line(c,27,41,44,41,0xffa17b72,4);
        stroke(c,GOLD,2);c.drawArc(-4,-31,6,-20,180,250,false,p);line(c,0,-22,0,-15,GOLD,2);
        polygon(c,new float[]{-26,0,0,-17,26,0},GOLD);
        polygon(c,new float[]{-10,-10,-25,-3,-29,12,-17,15,-14,3,-18,32,18,32,14,3,17,15,29,12,25,-3,10,-10,6,-3,-6,-3},a);
        line(c,0,-2,0,29,mix(a,CREAM,.55f),1.5f);line(c,-16,21,16,21,GOLD,3);star(c,8,8,5,GOLD);
    }

    private void satchel(Canvas c,int a) {
        stroke(c,mix(a,INK,.4f),7);c.drawArc(-23,-43,23,-1,180,180,false,p);
        round(c,-37,-16,37,39,12,mix(a,INK,.4f));round(c,-35,-20,35,34,12,a);
        path.reset();path.moveTo(-37,-18);path.quadTo(0,-36,37,-18);path.lineTo(31,6);path.quadTo(0,20,-31,6);path.close();fill(mix(a,CREAM,.25f));c.drawPath(path,p);
        line(c,-25,20,25,20,mix(a,INK,.15f),1.3f);round(c,-9,-1,9,15,3,GOLD);round(c,-4,2,4,9,1,mix(a,INK,.2f));circle(c,-28,22,2,CREAM);circle(c,28,22,2,CREAM);
    }

    private void flag(Canvas c,int a,boolean language) {
        line(c,-24,-44,-24,40,0xffa27b6d,5);circle(c,-24,-45,5,GOLD);
        path.reset();path.moveTo(-21,-37);path.cubicTo(-1,-48,15,-19,38,-34);path.lineTo(36,4);path.cubicTo(10,19,0,-10,-21,1);path.close();
        fill(a);c.drawPath(path,p);line(c,-19,-30,-19,-2,mix(a,CREAM,.6f),2);
        if(language){circle(c,6,-17,11,CREAM);stroke(c,mix(a,INK,.25f),1.3f);c.drawOval(-1,-28,13,-6,p);line(c,-4,-17,16,-17,mix(a,INK,.25f),1.3f);}
        else star(c,7,-18,10,GOLD);
        ellipse(c,-36,36,-9,44,0xffb69c7f);
    }

    private void medal(Canvas c,int a) {
        polygon(c,new float[]{-25,7,-33,43,-17,36,-8,46,0,15},a);polygon(c,new float[]{25,7,33,43,17,36,8,46,0,15},mix(a,INK,.2f));
        circle(c,0,-7,33,0xffac7f55);circle(c,0,-10,32,GOLD);circle(c,0,-10,25,0xffffe6a8);circle(c,0,-10,21,a);star(c,0,-10,14,CREAM);
        stroke(c,0xfff7cd79,1.5f);c.drawCircle(0,-10,28,p);sparkle(c,-27,-32,4,CREAM);
    }

    private void heart(Canvas c,int a) {
        path.reset();path.moveTo(0,35);path.cubicTo(-65,-1,-35,-52,-5,-29);path.quadTo(0,-24,5,-29);path.cubicTo(35,-52,65,-1,0,35);path.close();
        fill(mix(a,INK,.25f));c.save();c.translate(0,4);c.drawPath(path,p);c.restore();
        fill(a);p.setShader(new LinearGradient(-33,-39,28,33,mix(a,CREAM,.3f),a,Shader.TileMode.CLAMP));c.drawPath(path,p);p.setShader(null);
        ellipse(c,-25,-24,-11,-16,0x90ffffff);sparkle(c,34,-25,5,GOLD);
    }

    private void compass(Canvas c,int a) {
        round(c,-8,-46,8,-32,5,0xffa97d5f);circle(c,0,-40,5,CREAM);
        circle(c,0,1,39,0xffb48762);circle(c,0,-2,38,GOLD);circle(c,0,-2,31,CREAM);
        for(int i=0;i<4;i++){c.save();c.rotate(i*90,0,-2);line(c,0,-30,0,-24,0xffb99488,2);c.restore();}
        polygon(c,new float[]{-18,17,-1,-26,6,5},a);polygon(c,new float[]{-18,17,6,5,16,25},mix(a,INK,.35f));
        circle(c,-1,4,4,GOLD);sparkle(c,29,-26,4,CREAM);
    }

    private void lantern(Canvas c,int a) {
        stroke(c,0xffac815b,4);c.drawArc(-12,-47,12,-23,180,180,false,p);
        polygon(c,new float[]{-21,-29,21,-29,29,-18,25,25,0,37,-25,25,-29,-18},mix(a,INK,.2f));
        polygon(c,new float[]{-18,-20,18,-20,17,22,0,29,-17,22},GOLD);
        polygon(c,new float[]{-18,-20,0,-17,0,29,-17,22},0xffffe8a9);
        line(c,0,-17,0,29,0xffac815b,3);line(c,-27,-22,27,-22,0xffac815b,5);line(c,-23,24,23,24,0xffac815b,5);
        star(c,0,-5,10,CREAM);sparkle(c,37,-19,4,GOLD);
    }

    private void coin(Canvas c,int a) {
        for(int i=0;i<3;i++){ellipse(c,-37+i*7,16-i*7,25+i*7,36-i*7,0xffbd8b54);ellipse(c,-37+i*7,11-i*7,25+i*7,30-i*7,GOLD);line(c,-28+i*7,16-i*7,16+i*7,16-i*7,0xffffe9b2,2);}
        ellipse(c,-11,-42,32,15,0xffbd8b54);ellipse(c,-15,-43,28,12,GOLD);ellipse(c,-9,-37,22,6,0xffffe5a0);star(c,7,-15,10,a);sparkle(c,-29,-25,6,CREAM);
    }

    private void ribbon(Canvas c,int a,int direction,boolean undo) {
        c.save();c.scale(direction<0?1:-1,1);
        path.reset();path.moveTo(-39,-7);path.lineTo(-10,-34);path.lineTo(-12,-17);
        path.cubicTo(32,-22,41,5,24,31);path.cubicTo(30,5,14,-2,-13,2);path.lineTo(-11,18);path.close();
        fill(mix(a,INK,.32f));c.save();c.translate(0,4);c.drawPath(path,p);c.restore();fill(a);c.drawPath(path,p);
        line(c,-28,-5,-15,-17,mix(a,CREAM,.55f),3);line(c,-6,-11,15,-8,mix(a,CREAM,.4f),2);
        if(undo){circle(c,25,27,11,GOLD);line(c,25,22,25,28,INK,2);line(c,25,28,29,30,INK,2);}
        else sparkle(c,31,-25,4,GOLD);
        c.restore();
    }

    private void foldedWing(Canvas c,int a,boolean close) {
        polygon(c,new float[]{-39,-29,-4,-12,1,-4,-8,4,-40,-13},a);
        polygon(c,new float[]{39,-29,4,-12,-1,-4,8,4,40,-13},mix(a,CREAM,.25f));
        polygon(c,new float[]{-39,29,-4,12,1,4,-8,-4,-40,13},mix(a,INK,.16f));
        polygon(c,new float[]{39,29,4,12,-1,4,8,-4,40,13},a);
        circle(c,0,0,9,GOLD);circle(c,-2,-2,2.5f,CREAM);
        if(!close){line(c,-30,37,29,37,mix(a,INK,.4f),3);}
    }

    private void lens(Canvas c,int a,boolean plus) {
        line(c,17,20,38,43,0xff8c6873,12);line(c,17,18,38,40,a,9);line(c,31,32,38,39,GOLD,9);
        circle(c,-7,-8,32,0xffa88064);circle(c,-7,-10,31,GOLD);circle(c,-7,-10,24,0xffbee3e7);
        ellipse(c,-23,-29,-11,-20,0xaaffffff);line(c,-19,-9,5,-9,mix(a,INK,.2f),4);
        if(plus)line(c,-7,-21,-7,3,mix(a,INK,.2f),4);
    }

    private void turntable(Canvas c,int a,boolean retry) {
        ellipse(c,-40,15,40,37,0xff8b6874);ellipse(c,-40,10,40,31,a);ellipse(c,-29,7,29,24,0xffffe6a8);
        polygon(c,new float[]{-14,-26,9,-32,24,-17,21,9,-2,16,-18,2},mix(a,CREAM,.35f));
        polygon(c,new float[]{9,-32,24,-17,21,9,7,-6},a);line(c,-14,-26,7,-6,0xbfffffff,1.5f);
        stroke(c,GOLD,5);c.drawArc(-41,-29,41,37,190,retry?300:205,false,p);
        polygon(c,new float[]{32,28,46,29,39,16},GOLD);if(retry)sparkle(c,-35,-31,5,CREAM);
    }

    private void seal(Canvas c,int a) {
        for(int i=0;i<10;i++){double t=i*Math.PI/5;circle(c,(float)Math.cos(t)*22,(float)Math.sin(t)*22,13,mix(a,INK,.25f));}
        circle(c,0,-1,31,a);circle(c,0,-2,24,mix(a,CREAM,.22f));
        line(c,-15,-2,-4,10,CREAM,6);line(c,-4,10,18,-14,CREAM,6);
        polygon(c,new float[]{-18,24,-25,45,-10,39,-3,45,3,26},GOLD);
        line(c,-13,-22,0,-26,0x8fffffff,2);
    }

    private void telescope(Canvas c,int a) {
        line(c,-3,14,-19,42,0xffa58473,4);line(c,-3,14,15,42,0xffa58473,4);line(c,-3,14,-2,44,0xffa58473,4);
        c.save();c.rotate(-26);round(c,-37,-19,31,10,5,a);round(c,-42,-23,-23,14,4,GOLD);round(c,24,-16,43,7,3,GOLD);ellipse(c,-48,-23,-34,14,0xff8e6374);ellipse(c,-45,-20,-35,10,0xffbde2e5);line(c,-16,-14,17,-14,mix(a,CREAM,.5f),3);c.restore();sparkle(c,35,-38,6,CREAM);
    }

    private void leaf(Canvas c,int a) {
        leafShape(c,-10,-6,39,a);line(c,-29,37,24,-28,0xff789268,3);
        for(int i=0;i<4;i++){float x=-18+i*9,y=25-i*11;line(c,x,y,x-8,y-14,mix(a,CREAM,.38f),2);}
        circle(c,17,-16,4,0x99ffffff);ellipse(c,-13,34,33,45,0xffbfa37e);ellipse(c,-9,31,29,40,0xffe5c7a1);
    }

    private void music(Canvas c,int a,boolean mute) {
        line(c,-9,-26,-9,22,a,7);line(c,28,-34,28,12,a,7);polygon(c,new float[]{-12,-30,31,-42,31,-27,-12,-16},a);
        ellipse(c,-31,12,-5,33,mix(a,INK,.2f));ellipse(c,6,2,32,23,a);ellipse(c,-26,14,-13,19,mix(a,CREAM,.45f));
        if(mute){line(c,-39,-40,40,39,CREAM,9);line(c,-39,-40,40,39,CORAL,5);}
        else {sparkle(c,-32,-27,5,GOLD);sparkle(c,39,-4,4,GOLD);}
    }

    private void shield(Canvas c,int a) {
        path.reset();path.moveTo(0,-44);path.quadTo(22,-28,37,-28);path.lineTo(32,14);path.quadTo(24,34,0,45);path.quadTo(-24,34,-32,14);path.lineTo(-37,-28);path.quadTo(-22,-28,0,-44);path.close();
        fill(0xffa57e58);c.drawPath(path,p);c.save();c.scale(.88f,.88f);fill(GOLD);c.drawPath(path,p);c.scale(.87f,.87f);fill(a);c.drawPath(path,p);c.restore();
        polygon(c,new float[]{-5,-28,-18,2,-2,1,-7,27,20,-7,4,-6,11,-28},CREAM);line(c,-25,-19,-22,-3,0x90ffffff,3);
    }

    private void profile(Canvas c,int a) {
        c.save();c.rotate(-9);polygon(c,new float[]{-35,-34,31,-39,39,30,-29,39},0xffb99a78);polygon(c,new float[]{-31,-30,27,-35,35,26,-25,35},CREAM);
        circle(c,0,-13,15,a);path.reset();path.moveTo(-23,20);path.cubicTo(-23,-5,23,-5,23,20);path.quadTo(0,31,-23,20);path.close();fill(mix(a,INK,.15f));c.drawPath(path,p);
        circle(c,-4,-16,2,CREAM);circle(c,5,-16,2,CREAM);line(c,-4,-6,4,-6,CREAM,2);c.restore();star(c,33,25,12,GOLD);
    }

    private void stars(Canvas c,int a) {
        star(c,0,1,33,a);star(c,-35,-25,13,GOLD);star(c,31,-33,17,CORAL);star(c,34,30,10,GOLD);
        line(c,-9,-12,-3,-20,mix(a,CREAM,.6f),4);sparkle(c,-30,32,5,CREAM);
    }

    private void sailboat(Canvas c,int a) {
        polygon(c,new float[]{-43,20,39,20,22,39,-24,39},mix(a,INK,.28f));polygon(c,new float[]{-39,16,40,16,26,31,-25,31},a);
        line(c,-2,-45,-2,22,0xffb58b66,4);polygon(c,new float[]{3,-41,35,10,3,10},CREAM);polygon(c,new float[]{-7,-31,-33,9,-7,9},GOLD);
        line(c,-20,45,27,45,0xff8dcfc8,3);line(c,5,-32,21,-6,0xffffddbd,2);sparkle(c,32,-35,5,CREAM);
    }

    private void hourglass(Canvas c,int a) {
        line(c,-27,-39,27,-39,0xffa47c6e,7);line(c,-27,39,27,39,0xffa47c6e,7);
        path.reset();path.moveTo(-23,-34);path.lineTo(23,-34);path.cubicTo(23,-7,5,-9,5,0);path.cubicTo(5,9,23,7,23,34);path.lineTo(-23,34);path.cubicTo(-23,7,-5,9,-5,0);path.cubicTo(-5,-9,-23,-7,-23,-34);path.close();fill(0xffc6e4e6);c.drawPath(path,p);stroke(c,0xff97bdbf,2);c.drawPath(path,p);
        polygon(c,new float[]{-16,-25,16,-25,0,-5},GOLD);polygon(c,new float[]{-17,29,0,9,17,29},a);line(c,0,-4,0,17,GOLD,2);line(c,-17,-27,-16,-16,0xafffffff,3);
    }

    private void mail(Canvas c,int a) {
        polygon(c,new float[]{-40,-21,34,-30,41,25,-32,35},mix(a,INK,.25f));
        polygon(c,new float[]{-40,-25,34,-34,41,20,-32,30},CREAM);
        polygon(c,new float[]{-40,-25,2,4,34,-34},mix(a,CREAM,.55f));
        line(c,-30,23,-7,-3,0xffc9a8a2,1.5f);line(c,35,14,8,-5,0xffc9a8a2,1.5f);circle(c,1,3,10,a);star(c,1,3,6,GOLD);
    }

    private void leafShape(Canvas c,float x,float y,float r,int color) {
        path.reset();path.moveTo(x-r*.6f,y+r);path.cubicTo(x-r*1.1f,y-r*.2f,x-r*.35f,y-r,x+r*.75f,y-r);path.cubicTo(x+r,y,x+r*.35f,y+r*.75f,x-r*.6f,y+r);path.close();fill(color);c.drawPath(path,p);
    }

    private void star(Canvas c,float x,float y,float r,int color) {
        path.reset();for(int i=0;i<10;i++){double a=i*Math.PI/5-Math.PI/2;float d=i%2==0?r:r*.47f;float xx=x+(float)Math.cos(a)*d,yy=y+(float)Math.sin(a)*d;if(i==0)path.moveTo(xx,yy);else path.lineTo(xx,yy);}path.close();fill(color);c.drawPath(path,p);
    }

    private void sparkle(Canvas c,float x,float y,float r,int color) {
        path.reset();path.moveTo(x,y-r);path.quadTo(x+r*.18f,y-r*.18f,x+r,y);path.quadTo(x+r*.18f,y+r*.18f,x,y+r);path.quadTo(x-r*.18f,y+r*.18f,x-r,y);path.quadTo(x-r*.18f,y-r*.18f,x,y-r);path.close();fill(color);c.drawPath(path,p);
    }

    private static boolean valid(RectF b){return b!=null&&Float.isFinite(b.left)&&Float.isFinite(b.top)&&Float.isFinite(b.right)&&Float.isFinite(b.bottom)&&b.width()>0&&b.height()>0;}
    private static int tone(int color){return color>=0&&color<4?new int[]{CORAL,TEAL,VIOLET,GOLD}[color]:color;}
    private static int mix(int a,int b,float amount){float t=Math.max(0,Math.min(1,amount));return Color.rgb(Math.round(Color.red(a)*(1-t)+Color.red(b)*t),Math.round(Color.green(a)*(1-t)+Color.green(b)*t),Math.round(Color.blue(a)*(1-t)+Color.blue(b)*t));}
    private void fill(int color){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}
    private void stroke(Canvas c,int color,float width){fill(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);}
    private void circle(Canvas c,float x,float y,float r,int color){fill(color);c.drawCircle(x,y,r,p);}
    private void ellipse(Canvas c,float l,float t,float r,float b,int color){fill(color);c.drawOval(l,t,r,b,p);}
    private void round(Canvas c,float l,float t,float r,float b,float radius,int color){fill(color);c.drawRoundRect(l,t,r,b,radius,radius,p);}
    private void line(Canvas c,float x,float y,float xx,float yy,int color,float width){fill(color);p.setStrokeWidth(width);c.drawLine(x,y,xx,yy,p);}
    private void polygon(Canvas c,float[] points,int color){path.reset();for(int i=0;i+1<points.length;i+=2){if(i==0)path.moveTo(points[i],points[i+1]);else path.lineTo(points[i],points[i+1]);}path.close();fill(color);c.drawPath(path,p);}
}
