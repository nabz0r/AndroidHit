package com.androidhit.lumadrift.commerce;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import com.androidhit.lumadrift.data.AvatarStyles;
import com.androidhit.lumadrift.data.Catalog;
import com.androidhit.lumadrift.ui.AvatarStage;
import com.androidhit.lumadrift.ui.CompanionPortrait;
import com.androidhit.lumadrift.ui.ShopIllustrations;

/** Decorative, stationary shop artwork. All meaning remains in adjacent accessible text. */
final class CommerceArt extends View {
    static final int GOLD=0,COIN=1,HERO=2;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private final CompanionPortrait portrait=new CompanionPortrait();
    private final ShopIllustrations treasures=new ShopIllustrations();
    private final int kind;
    private final Catalog.CharacterItem character;
    private final Catalog.SkinItem skin;
    CommerceArt(Context context,int kind){this(context,kind,null,null);}
    CommerceArt(Context context,Catalog.CharacterItem who){this(context,-1,who,null);}
    CommerceArt(Context context,Catalog.SkinItem theme){this(context,-1,null,theme);}
    private CommerceArt(Context context,int kind,Catalog.CharacterItem who,Catalog.SkinItem theme){
        super(context);this.kind=kind;character=who;skin=theme;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);canvas.save();canvas.scale(getWidth()/100f,getHeight()/100f);
        if(character!=null){
            oval(canvas,5,9,95,96,blend(character.color,0xffffffff,.6f));
            portrait.draw(canvas,character,50,49,22,AvatarStyles.find(null),AvatarStage.Pose.HAPPY,0,true,null,null);
        }else if(skin!=null){
            oval(canvas,3,4,97,97,skin.bg);oval(canvas,12,44,91,104,skin.accent2);
            oval(canvas,-3,61,79,110,skin.accent);oval(canvas,58,17,79,38,0xfffff9e8);
            star(canvas,32,28,8,0xffffffff);star(canvas,73,55,5,0xffffffff);
        }else if(kind==HERO){
            oval(canvas,5,15,96,89,0xffe2d6fc);oval(canvas,23,32,97,103,0xfffbdde6);
            portrait.draw(canvas,Catalog.CHARACTERS[0],44,51,22,AvatarStyles.find(null),AvatarStage.Pose.HAPPY,0,true,null,null);
            treasures.gold(canvas,77,30,12,0,true);treasures.coin(canvas,77,69,12,0,true);star(canvas,13,25,5,0xffb7e6d3);
        }else if(kind==COIN)treasures.coin(canvas,50,50,39,0,true);
        else treasures.gold(canvas,50,50,39,0,true);
        canvas.restore();
    }
    private void oval(Canvas c,float l,float t,float r,float b,int color){paint.setColor(color);c.drawOval(l,t,r,b,paint);}
    private void star(Canvas c,float x,float y,float r,int color){
        path.reset();for(int i=0;i<8;i++){double angle=Math.PI*i/4-Math.PI/2;float radius=(i%2==0?r:r*.34f);float px=x+(float)Math.cos(angle)*radius,py=y+(float)Math.sin(angle)*radius;if(i==0)path.moveTo(px,py);else path.lineTo(px,py);}path.close();paint.setColor(color);c.drawPath(path,paint);
    }
    private static int blend(int a,int b,float amount){int red=Math.round(((a>>16)&255)*(1-amount)+((b>>16)&255)*amount),green=Math.round(((a>>8)&255)*(1-amount)+((b>>8)&255)*amount),blue=Math.round((a&255)*(1-amount)+(b&255)*amount);return 0xff000000|(red<<16)|(green<<8)|blue;}
}
