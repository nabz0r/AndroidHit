package com.androidhit.lumadrift.ui;

/** Pure geometry for the 390-wide puzzle canvas; its parent owns vertical scrolling. */
public final class BattleDockLayout124 {
    public static final float LEFT=24f, WIDTH=342f, GAP=8f, MINIMUM_TOUCH=48f;
    public final float minimumTouch,minimumText,headerHeight,cellHeight,height;
    public final int columns,rows;
    private final Rect[] slots=new Rect[4];

    public BattleDockLayout124(float minimumTouch,float minimumText){
        if(!Float.isFinite(minimumTouch)||minimumTouch<=0||!Float.isFinite(minimumText)||minimumText<=0)
            throw new IllegalArgumentException("Finite positive touch and text floors required");
        this.minimumTouch=Math.max(MINIMUM_TOUCH,minimumTouch);this.minimumText=minimumText;
        if(this.minimumTouch>(WIDTH-GAP)/2f)
            throw new IllegalArgumentException("Touch floor cannot fit two columns in the fixed canvas");
        headerHeight=Math.max(24f,1.5f*minimumText);
        cellHeight=Math.max(Math.max(104f,this.minimumTouch),3f*minimumText+24f);
        columns=(WIDTH-3*GAP)/4f>=Math.max(this.minimumTouch,4f*minimumText)?4:2;rows=4/columns;
        float width=(WIDTH-(columns-1)*GAP)/columns;
        height=headerHeight+GAP+rows*cellHeight+(rows-1)*GAP+12f;
        if(!Float.isFinite(headerHeight)||!Float.isFinite(cellHeight)||!Float.isFinite(height))
            throw new IllegalArgumentException("Text floor overflows finite layout geometry");
        for(int slot=0;slot<4;slot++){
            float x=LEFT+(slot%columns)*(width+GAP),y=headerHeight+GAP+(slot/columns)*(cellHeight+GAP);
            slots[slot]=new Rect(x,y,x+width,y+cellHeight);
        }
    }
    /** Slots 0–2 are companions; slot 3 is resonance. Returned rectangles are immutable. */
    public Rect slot(int index){if(index<0||index>=4)throw new IllegalArgumentException("Slot 0..3 required");return slots[index];}
    public int slotAt(float x,float y){for(int index=0;index<4;index++)if(slots[index].contains(x,y))return index;return -1;}
    public static final class Rect {
        public final float left,top,right,bottom;
        private Rect(float left,float top,float right,float bottom){this.left=left;this.top=top;this.right=right;this.bottom=bottom;}
        public float width(){return right-left;}
        public float height(){return bottom-top;}
        public float centerX(){return (left+right)*.5f;}
        public float centerY(){return (top+bottom)*.5f;}
        public boolean contains(float x,float y){return x>=left&&x<right&&y>=top&&y<bottom;}
        public boolean intersects(Rect other){return left<other.right&&other.left<right&&top<other.bottom&&other.top<bottom;}
    }
}
