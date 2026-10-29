package com.androidhit.lumadrift.ui;

/** Viewport-bound victory choreography; controls never depend on board scroll. */
public final class VictoryLayout {
    public final float height,titleY,starsY,avatarX=195,avatarY,avatarRadius,metricsY,rewardY;
    public final PlaySceneLayout.Rect next,map,shop,gifts;
    public VictoryLayout(float viewport,float minimumText,float minimumTouch){
        this(viewport,minimumText,minimumTouch,false);
    }
    public VictoryLayout(float viewport,float minimumText,float minimumTouch,boolean hasGifts){
        height=Math.max(320,Float.isFinite(viewport)?viewport:600);
        float font=Math.max(12,Math.min(24,minimumText)),touch=Math.max(48,minimumTouch);
        float primary=Math.max(54,touch),secondary=Math.max(48,touch),gap=10;
        shop=new PlaySceneLayout.Rect(201,height-12-secondary,366,height-12);
        map=new PlaySceneLayout.Rect(24,shop.top,189,shop.bottom);
        next=new PlaySceneLayout.Rect(24,map.top-gap-primary,366,map.top-gap);
        gifts=hasGifts?new PlaySceneLayout.Rect(24,next.top-gap-secondary,366,next.top-gap):null;
        rewardY=(hasGifts?gifts.top:next.top)-30;
        metricsY=rewardY-62;
        titleY=62+Math.max(24,font*1.45f);starsY=titleY+39;
        float available=metricsY-30-(starsY+25);
        avatarRadius=Math.max(0,Math.min(83,available*.43f));
        avatarY=(starsY+25+metricsY-30)/2;
    }
    public static int displayedStars(int earned,long elapsed,boolean reduced){
        return reduced?earned:elapsed<180?0:Math.max(0,Math.min(earned,(int)((elapsed-180)/250)+1));
    }
    public static float rewardProgress(long elapsed,boolean reduced){return reduced?1:Math.max(0,Math.min(1,(elapsed-750)/650f));}
    public static String clock(long millis){long seconds=Math.max(0,millis)/1000;return String.format(java.util.Locale.ROOT,"%02d:%02d",seconds/60,seconds%60);}
}
