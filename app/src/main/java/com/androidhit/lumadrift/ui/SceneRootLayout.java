package com.androidhit.lumadrift.ui;

/** Shared root-scene placements, in the same 390-unit coordinates as the live canvas.
 * Objects retain their size in compact windows; captions and the bottom navigation
 * have their own space instead of being squeezed into proportional rows.
 */
public final class SceneRootLayout {
    public static final float WIDTH=390f;
    public final float height, minimumText, minimumTouch;
    public final Destination previous, next;
    public final Destination adventurePortal, adventureLevels, adventureEvents, adventureRewards, adventureSettings;
    public final Rect adventureLevelCaption;
    public final float adventureLevelBaseline;
    public final float collectionPortraitY, collectionPortraitRadius, collectionRowY;
    public final Rect collectionProfileHit, collectionPortraitHit, collectionNameCaption, collectionCompanionsHit, collectionCompanionsCaption;
    public final float collectionNameBaseline;
    public final Destination collectionWardrobe, collectionSummon, collectionBadges, collectionEquipment;
    public final float atelierSofaY, atelierGroupY;
    public final Rect atelierSofaArt, atelierSofaCaption, atelierSofaHit;
    public final float atelierSofaBaseline;
    public final Rect[] atelierGroupArt=new Rect[3], atelierGroupCaption=new Rect[3], atelierGroupHit=new Rect[3];
    public final Destination atelierWalls, atelierFloor, homeWardrobe, homeDecorate;

    public SceneRootLayout(float viewportHeight,float minimumTextSize,float minimumTouchSize){
        height=Math.max(1,viewportHeight);minimumText=Math.max(1,minimumTextSize);minimumTouch=Math.max(1,minimumTouchSize);
        // The existing 51-unit objects and their captions fit fully above the safe bottom.
        float siblingY=height-51;
        previous=destination(42,siblingY,51);next=destination(348,siblingY,51);

        float sideY=Math.min(height*.70f,height-170);
        float portalY=Math.min(height*.45f,sideY-166);
        adventurePortal=destination(195,portalY,178);
        adventureLevels=destination(76,sideY,92);adventureEvents=destination(304,sideY,92);
        adventureRewards=destination(195,Math.min(height*.77f,height-114),97);
        adventureSettings=destination(349,99,43);
        // Put the level before the portal, so the lower Play caption stays separate
        // from the book and event art even at 320dp and the supported font floor.
        adventureLevelBaseline=portalY-108;
        adventureLevelCaption=caption(195,adventureLevelBaseline,210,22);

        collectionPortraitY=height*.41f;collectionPortraitRadius=Math.min(84,height*.105f);
        collectionProfileHit=target(new Rect(24,78,330,124));
        float portraitTop=Math.max(132,collectionPortraitY-collectionPortraitRadius*1.8f);
        collectionPortraitHit=target(new Rect(125,portraitTop,265,collectionPortraitY+collectionPortraitRadius*1.8f));
        collectionNameBaseline=collectionPortraitY+collectionPortraitRadius*1.95f;
        collectionNameCaption=caption(195,collectionNameBaseline,280,22);
        collectionWardrobe=destination(62,height*.54f,70);collectionSummon=destination(313,height*.54f,83);
        // Keep a full 8-unit gap between the last content hit and sibling hit.
        collectionRowY=Math.min(height*.76f,previous.hit.top-8-64);
        collectionCompanionsHit=target(new Rect(30,collectionRowY-50,148,collectionRowY+64));
        collectionCompanionsCaption=caption(88,collectionRowY+49,132,14);
        collectionBadges=destination(195,collectionRowY,79);collectionEquipment=destination(303,collectionRowY,83);

        atelierSofaY=height*.31f;
        atelierSofaArt=new Rect(78,atelierSofaY-88,319,atelierSofaY+100);
        atelierSofaBaseline=atelierSofaY+120;
        atelierSofaCaption=caption(195,atelierSofaBaseline,330,22);
        atelierSofaHit=target(new Rect(48,atelierSofaY-105,342,atelierSofaY+149));
        // Move the group objects after the sofa legend; never shrink their bitmaps.
        atelierGroupY=Math.max(height*.61f,atelierSofaCaption.bottom+10+65);
        for(int i=0;i<3;i++){
            float x=68+i*127;
            atelierGroupArt[i]=new Rect(x-49,atelierGroupY-65,x+49,atelierGroupY+40);
            atelierGroupCaption[i]=caption(x,atelierGroupY+66,117,14);
            atelierGroupHit[i]=target(new Rect(x-56,atelierGroupY-73,x+56,atelierGroupY+84));
        }
        // These two material objects share the last row with the only Atelier sibling.
        // Horizontal lanes keep their art, captions and touch centres independent.
        float materialsY=Math.max(height*.80f,atelierGroupCaption[0].bottom+12+78*.52f);
        materialsY=Math.min(materialsY,height-54);
        atelierWalls=destination(157,materialsY,78);atelierFloor=destination(285,materialsY,78);
        homeWardrobe=destination(64,height-174,74);homeDecorate=destination(320,height-174,74);
    }
    public Destination destination(float x,float y,float size){return new Destination(x,y,size);}
    public Rect caption(float x,float baseline,float width,float size){
        float textSize=Math.max(minimumText,size);
        // Conservative sans-serif glyph bounds, including accents and the shadow.
        return new Rect(x-width/2,baseline-textSize*1.15f,x+width/2,baseline+textSize*.30f);
    }
    public Rect target(Rect raw){
        float cx=raw.centerX(),cy=raw.centerY(),w=Math.max(raw.width(),minimumTouch),h=Math.max(raw.height(),minimumTouch);
        return new Rect(Math.max(0,cx-w/2),Math.max(0,cy-h/2),Math.min(WIDTH,cx+w/2),Math.min(height,cy+h/2));
    }
    public final class Destination {
        public final float x,y,size,baseline;
        public final Rect art,caption,hit;
        private Destination(float x,float y,float size){
            this.x=x;this.y=y;this.size=size;baseline=y+size*.57f;
            art=new Rect(x-size/2,y-size*.52f,x+size/2,y+size*.48f);
            caption=SceneRootLayout.this.caption(x,baseline,Math.max(76,size+30),15);
            hit=target(new Rect(art.left-6,art.top-5,art.right+6,art.bottom+24));
        }
    }
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
