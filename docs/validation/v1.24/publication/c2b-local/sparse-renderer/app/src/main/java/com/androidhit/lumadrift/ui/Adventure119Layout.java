package com.androidhit.lumadrift.ui;

/**
 * One scrollable adventure scene, with a distinct lower reading/action band.
 * Coordinates are logical units in a 390-unit-wide scene; native text is measured by the host.
 */
public final class Adventure119Layout {
    public static final float WIDTH=390f, NAVIGATION_RESERVE=84f, FINAL_GAP=24f;
    public final float viewportHeight,minimumTouch,sceneHeight,middle,radius,actionY,continueHeight;
    public final float cardsY,cardsHeight,rewardsTop,rewardsHeight,rewardsBaseline,contentHeight;
    public final Rect continueRect,rewardsRect;
    public final Rect[] cards=new Rect[3];

    public Adventure119Layout(float viewportHeight,float minimumTouchLogical){
        this.viewportHeight=positive(viewportHeight,650f);
        minimumTouch=Math.max(48f,positive(minimumTouchLogical,48f));
        sceneHeight=Math.max(650f,this.viewportHeight);
        middle=Math.max(228f,Math.min(348f,sceneHeight*.4f));
        radius=Math.max(48f,Math.min(77f,sceneHeight*.09f));
        actionY=Math.min(sceneHeight-255f,middle+radius*1.85f+15f);
        continueHeight=Math.max(62f,minimumTouch);
        continueRect=new Rect(27f,actionY,363f,actionY+continueHeight);
        cardsY=actionY+continueHeight+21f;
        cardsHeight=Math.max(77f,minimumTouch);
        for(int n=0;n<3;n++)cards[n]=new Rect(18f+n*122f,cardsY,128f+n*122f,cardsY+cardsHeight);
        rewardsTop=cardsY+cardsHeight+12f;
        rewardsHeight=Math.max(54f,minimumTouch);
        rewardsBaseline=rewardsTop+rewardsHeight*.5f+5f;
        rewardsRect=new Rect(50f,rewardsTop,340f,rewardsTop+rewardsHeight);
        contentHeight=Math.max(sceneHeight,rewardsRect.bottom+NAVIGATION_RESERVE+FINAL_GAP);
    }

    public float maximumScroll(){return Math.max(0f,contentHeight-viewportHeight);}
    private static float positive(float value,float fallback){return Float.isFinite(value)&&value>0?value:fallback;}
    public static final class Rect {
        public final float left,top,right,bottom;
        public Rect(float left,float top,float right,float bottom){this.left=left;this.top=top;this.right=right;this.bottom=bottom;}
        public float width(){return right-left;}
        public float height(){return bottom-top;}
        public float centerX(){return (left+right)*.5f;}
        public float centerY(){return (top+bottom)*.5f;}
        public boolean contains(float x,float y){return x>=left&&x<right&&y>=top&&y<bottom;}
        public boolean intersects(Rect other){return left<other.right&&other.left<right&&top<other.bottom&&other.top<bottom;}
    }
}
