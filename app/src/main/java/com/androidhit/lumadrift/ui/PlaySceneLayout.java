package com.androidhit.lumadrift.ui;

/** Puzzle geometry shared by the live canvas and its hit regions.
 * Logical coordinates are 390 units wide. Short windows scroll the complete
 * scene instead of reducing text, gate targets or the authored board size.
 * Text is measured by the caller with the actual Android font/StaticLayout.
 */
public final class PlaySceneLayout {
    public static final float WIDTH=390f;
    public final float viewportHeight, minimumText, minimumTouch, boardX, boardY, boardWidth;
    public final float gateTargetSize, gateRail, feedbackTop, feedbackHeight;
    public final float actionsY, actionHeight, powersY, powerGridHeight, contentHeight;
    public final boolean relay, compactActions;
    public final Rect boardRect, budgetRect, feedbackRect, undoRect, restartRect, powersRect, powersMoreRect;
    /** Direction order is UP, RIGHT, DOWN, LEFT, matching the engine enum. */
    public final float[] gateX=new float[4], gateY=new float[4];
    public final Rect[] gateTargets=new Rect[4], gateVisualBounds=new Rect[4], powerGrid=new Rect[3];

    public PlaySceneLayout(float viewportHeight,boolean relay,float minimumTouchLogical,float feedbackHeightMeasured,float minimumText){
        this(viewportHeight,relay,minimumTouchLogical,feedbackHeightMeasured,minimumText,0);
    }
    public PlaySceneLayout(float viewportHeight,boolean relay,float minimumTouchLogical,float feedbackHeightMeasured,float minimumText,float companionInset){
        this(viewportHeight,relay,minimumTouchLogical,feedbackHeightMeasured,minimumText,companionInset,false);
    }
    public PlaySceneLayout(float viewportHeight,boolean relay,float minimumTouchLogical,float feedbackHeightMeasured,float minimumText,float companionInset,boolean focusOnBoard){
        float topInset=Math.max(0,Math.min(400,companionInset));
        this.viewportHeight=positive(viewportHeight,1);this.relay=relay;
        this.minimumTouch=positive(minimumTouchLogical,48);this.minimumText=positive(minimumText,12);
        gateTargetSize=Math.max(50,this.minimumTouch);
        float halfTouch=gateTargetSize/2;
        // A larger physical target must stay outside the board, not cover its edge cells.
        gateRail=Math.max(26,halfTouch+1);
        float horizontalExtent=Math.max(29,halfTouch); // Trail badge reaches x+29.
        boardWidth=Math.max(1,Math.min(304,WIDTH-2*(gateRail+horizontalExtent+8)));
        boardX=(WIDTH-boardWidth)/2;
        // Even the manual highlight's outer ink stays below the progress rail+8.
        boardY=(focusOnBoard?152+gateRail+Math.max(25,halfTouch):Math.max(182,132+gateRail+25))+topInset;
        boardRect=new Rect(boardX,boardY,boardX+boardWidth,boardY+boardWidth);
        // Numeric remaining/total or infinity; the caller retains full localized AX copy.
        // Narrower unsupported windows can still avoid covering the central UP target.
        budgetRect=new Rect(24,129+topInset,Math.min(156,195-halfTouch-8),175+topInset);
        float midX=195,midY=boardY+boardWidth/2;
        gateX[0]=midX;gateY[0]=boardY-gateRail;
        gateX[1]=boardRect.right+gateRail;gateY[1]=midY;
        gateX[2]=midX;gateY[2]=boardRect.bottom+gateRail;
        gateX[3]=boardX-gateRail;gateY[3]=midY;
        for(int i=0;i<4;i++){
            gateTargets[i]=new Rect(gateX[i]-halfTouch,gateY[i]-halfTouch,gateX[i]+halfTouch,gateY[i]+halfTouch);
            // Conservative union: manual outline, directional preview and Trail badge.
            // Nonmanual gates use their smaller SweetUi halo in the live renderer.
            gateVisualBounds[i]=new Rect(gateX[i]-25,gateY[i]-25,gateX[i]+29,gateY[i]+25);
        }
        feedbackTop=Math.max(gateTargets[2].bottom,gateVisualBounds[2].bottom)+8;
        feedbackHeight=Math.max(42,positive(feedbackHeightMeasured,42));
        feedbackRect=new Rect(26,feedbackTop,364,feedbackTop+feedbackHeight);
        actionsY=feedbackRect.bottom+8;
        float normalActionHeight=Math.max(48,this.minimumTouch);
        powerGridHeight=Math.max(Math.max(66,this.minimumTouch),this.minimumText*1.25f+42);
        float fullGridY=actionsY+normalActionHeight+16;
        float fullMoreY=fullGridY+powerGridHeight+16;
        compactActions=focusOnBoard||fullMoreY+normalActionHeight+16>this.viewportHeight;
        if(compactActions){
            // Compact actions draw a42-unit object, then a4-unit gap and up to
            // two real lines. Keep both that measured copy and a font-growth
            // allowance inside their own target, before placing the next row.
            actionHeight=Math.max(normalActionHeight,Math.max(50+feedbackHeight,54+2*this.minimumText*1.3f));
            // Three separate vertical object/caption columns share one row.
            undoRect=new Rect(24,actionsY,130,actionsY+actionHeight);
            restartRect=new Rect(142,actionsY,248,actionsY+actionHeight);
            powersY=actionsY;
            powersRect=new Rect(260,powersY,366,powersY+actionHeight);
            powersMoreRect=powersRect;
            contentHeight=Math.max(this.viewportHeight,powersRect.bottom+16);
        }else{
            actionHeight=normalActionHeight;
            undoRect=new Rect(24,actionsY,189,actionsY+actionHeight);
            restartRect=new Rect(201,actionsY,366,actionsY+actionHeight);
            powersY=fullGridY;
            powersMoreRect=new Rect(24,fullMoreY,366,fullMoreY+actionHeight);
            powersRect=powersMoreRect;
            contentHeight=Math.max(this.viewportHeight,powersMoreRect.bottom+16);
        }
        for(int i=0;i<3;i++)powerGrid[i]=new Rect(24+i*118,powersY,130+i*118,powersY+powerGridHeight);
    }

    private static float positive(float value,float fallback){return Float.isFinite(value)&&value>0?value:fallback;}
    public static final class Rect {
        public final float left,top,right,bottom;
        public Rect(float left,float top,float right,float bottom){this.left=left;this.top=top;this.right=right;this.bottom=bottom;}
        public float width(){return right-left;}
        public float height(){return bottom-top;}
        public float centerX(){return (left+right)/2;}
        public float centerY(){return (top+bottom)/2;}
        public boolean contains(float x,float y){return x>=left&&x<right&&y>=top&&y<bottom;}
        public boolean intersects(Rect other){return left<other.right&&other.left<right&&top<other.bottom&&other.top<bottom;}
    }
}
