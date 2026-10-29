package com.androidhit.lumadrift.ui;

/** Shared battle dock geometry in the same logical coordinates as the puzzle canvas. */
public final class CompanionBattleLayout {
    public static final float HEIGHT=144f, MINIMUM_WIDTH=264f, GAP=8f, MINIMUM_TOUCH=48f;
    public final Rect bounds,combo;
    public final Rect[] actions=new Rect[3];
    public final float minimumTouch;

    public CompanionBattleLayout(float left,float top,float right,float bottom){
        this(left,top,right,bottom,MINIMUM_TOUCH);
    }
    public CompanionBattleLayout(float left,float top,float right,float bottom,float minimumTouch){
        if(!Float.isFinite(minimumTouch)||minimumTouch<=0)throw new IllegalArgumentException("Finite positive touch floor required");
        this.minimumTouch=Math.max(MINIMUM_TOUCH,minimumTouch);
        if(!Float.isFinite(left)||!Float.isFinite(top)||!Float.isFinite(right)||!Float.isFinite(bottom)
                ||right-left<Math.max(MINIMUM_WIDTH,3*this.minimumTouch+2*GAP)
                ||bottom-top<heightFor(this.minimumTouch))
            throw new IllegalArgumentException("Battle dock requires finite bounds covering its physical touch floor");
        bounds=new Rect(left,top,right,bottom);
        float width=(right-left-2*GAP)/3f;
        for(int n=0;n<3;n++)actions[n]=new Rect(left+n*(width+GAP),top,left+n*(width+GAP)+width,bottom-this.minimumTouch-6f);
        combo=new Rect(left,bottom-this.minimumTouch,right,bottom);
    }
    public static float heightFor(float minimumTouch){return Math.max(HEIGHT,96f+Math.max(MINIMUM_TOUCH,minimumTouch));}

    /** Half-open targets agree with canvas routing and never share an edge hit. */
    public int slotAt(float x,float y){for(int n=0;n<3;n++)if(actions[n].contains(x,y))return n;return -1;}
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
