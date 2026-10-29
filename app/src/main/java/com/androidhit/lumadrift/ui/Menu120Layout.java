package com.androidhit.lumadrift.ui;

/** Logical 390-wide menu geometry. The fixed wallet is outside the scrolling content. */
public final class Menu120Layout {
    public static final float WIDTH=390f, NAV_RESERVE=96f, GUTTER=24f;
    public static final float SHOP_PRODUCT_LIFT=144f,SHOP_CONTENT_HEIGHT=841f;
    public final float viewportHeight, minimumTouch, heroTop, heroHeight, contentHeight;
    public final Rect profile, settings, team, play, gifts, shop, map, expedition, events, room, atelier;
    public Menu120Layout(float viewportHeight,float minimumTouch){
        this.viewportHeight=positive(viewportHeight,700f);
        this.minimumTouch=Math.max(48f,positive(minimumTouch,48f));
        settings=new Rect(318,12,366,72).grow(this.minimumTouch);profile=new Rect(24,12,Math.min(308,settings.left-10),78).grow(this.minimumTouch);
        heroTop=96f;heroHeight=Math.max(190f,Math.min(230f,190f+(this.viewportHeight-620f)*.12f));
        team=new Rect(30,heroTop,360,heroTop+heroHeight);
        float y=team.bottom+12;
        play=new Rect(24,y,366,y+Math.max(64f,this.minimumTouch));y=play.bottom+20;
        gifts=new Rect(24,y,188,y+108);shop=new Rect(202,y,366,y+108);y=gifts.bottom+20;
        float row=Math.max(72f,this.minimumTouch);
        map=new Rect(24,y,130,y+row);expedition=new Rect(142,y,248,y+row);events=new Rect(260,y,366,y+row);y=map.bottom+14;
        room=new Rect(24,y,188,y+Math.max(54f,this.minimumTouch));atelier=new Rect(202,y,366,y+Math.max(54f,this.minimumTouch));
        contentHeight=Math.max(this.viewportHeight,room.bottom+NAV_RESERVE);
    }
    public static float hudHeight(float minimumTouch){return Math.max(76f,positive(minimumTouch,48f)+20f);}
    public static Rect wallet(int index,float minimumTouch){
        if(index<0||index>2)throw new IllegalArgumentException("wallet index");
        float width=(WIDTH-32f-16f)/3f,left=16f+index*(width+8f);
        return new Rect(left,8,left+width,hudHeight(minimumTouch)-8);
    }
    public static Rect navigation(int index,float viewportHeight,float minimumTouch){
        if(index<0||index>4)throw new IllegalArgumentException("navigation index");
        float height=Math.max(61,minimumTouch),middle=viewportHeight-42.5f;
        return new Rect(index*78f,middle-height/2,(index+1)*78f,middle+height/2);
    }
    public static Rect shopPrimary(){return new Rect(24,658-SHOP_PRODUCT_LIFT,366,720-SHOP_PRODUCT_LIFT);}
    public static float rewardHeight(int rows,float minimumTouch){return 170f+Math.max(0,rows)*rewardRowHeight(minimumTouch)+NAV_RESERVE;}
    public static float rewardRowHeight(float minimumTouch){return Math.max(154f,positive(minimumTouch,48f)+94f);}
    public static boolean canClaim(boolean ready,boolean claimed){return ready&&!claimed;}
    public static float viewY(float contentY,float scroll,float hudInset){return contentY-scroll+hudInset;}
    public static float contentY(float viewY,float scroll,float hudInset){return viewY+scroll-hudInset;}
    /** Negative close elapsed means still open. Reading time is never capped. */
    public static float revealAlpha(long elapsed,long closeElapsed,boolean reduced){
        if(elapsed<0)return 0;
        if(closeElapsed>=0)return reduced?0:Math.max(0,1-closeElapsed/180f);
        return reduced?1:Math.min(1,elapsed/220f);
    }
    public static boolean revealFinished(long closeElapsed,boolean reduced){return closeElapsed>=0&&(reduced||closeElapsed>=180);}
    public static final class Reveal {
        public final boolean compact;
        public final float centerY,illustrationSize,haloRadius,titleY,giftY,detailY,continueY;
        public Reveal(float viewportHeight){
            float height=Math.max(180f,positive(viewportHeight,650));compact=height<430;
            centerY=compact?height*.41f:Math.max(150,Math.min(320,height*.44f));
            illustrationSize=compact?(height<300?52:70):105;haloRadius=compact?58:99;
            titleY=compact?30:centerY-137;giftY=compact?height-84:centerY+112;
            detailY=compact?height-54:centerY+155;continueY=compact?height-22:Math.min(height-35,centerY+220);
        }
    }
    private static float positive(float value,float fallback){return Float.isFinite(value)&&value>0?value:fallback;}
    public static final class Rect {
        public final float left,top,right,bottom;
        public Rect(float left,float top,float right,float bottom){this.left=left;this.top=top;this.right=right;this.bottom=bottom;}
        public float width(){return right-left;}public float height(){return bottom-top;}
        public float centerX(){return (left+right)*.5f;}public float centerY(){return (top+bottom)*.5f;}
        public boolean contains(float x,float y){return x>=left&&x<right&&y>=top&&y<bottom;}
        public boolean intersects(Rect other){return left<other.right&&other.left<right&&top<other.bottom&&other.top<bottom;}
        private Rect grow(float minimum){float w=Math.max(width(),minimum),h=Math.max(height(),minimum);return new Rect(centerX()-w/2,centerY()-h/2,centerX()+w/2,centerY()+h/2);}
    }
}
