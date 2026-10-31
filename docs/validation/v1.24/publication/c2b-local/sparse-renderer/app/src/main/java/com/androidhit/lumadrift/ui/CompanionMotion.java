package com.androidhit.lumadrift.ui;

import com.androidhit.lumadrift.data.Catalog;

/**
 * Pure visual snapshot contract for all 24 identities. It describes requested art; it neither
 * asserts that an atlas exists nor changes rarity, ownership, level, skill or equipment.
 * One authored form has 20 poses in four columns and five rows. Source rectangles are measured
 * from actual pixels by the asset registry; equal cells here describe order only, not crop bounds.
 */
public final class CompanionMotion {
    public static final int COLUMNS=4,ROWS=5,POSES=20,FORMS=4;
    public static final float BODY_HEIGHT=3.15f,GROUND_Y=1.65f;
    private static final int[] FORM_LEVELS={1,3,6,10};
    public enum Facing { FRONT, BACK, LEFT, RIGHT }
    public enum Action { IDLE, WALK, JUMP, LAND, HUG, CROUCH, HAPPY, SAD, SURPRISE, THINK, STANCE }
    public enum Stance { CALM, READY, GUARD, PROUD }
    public static final class Frame {
        public final String characterId;
        public final int level,form,formLevel,detailCount,poseIndex,row,column;
        public final Facing requestedFacing,facing;
        public final Action action;
        public final Stance stance;
        public final boolean reducedMotion;
        public final float phase,bodyDX,bodyDY,scaleX,scaleY,rotation;
        private Frame(String id,int level,int form,int details,int pose,Facing requested,Facing rendered,
                      Action action,Stance stance,boolean reduced,float phase,float dx,float dy,float rotation){
            characterId=id;this.level=level;this.form=form;formLevel=FORM_LEVELS[form];detailCount=details;
            poseIndex=pose;row=pose/COLUMNS;column=pose%COLUMNS;requestedFacing=requested;facing=rendered;
            this.action=action;this.stance=stance;reducedMotion=reduced;this.phase=phase;bodyDX=dx;bodyDY=dy;
            scaleX=scaleY=1;this.rotation=rotation;
        }
        /** Stable visual registry identity; inventory and summons retain the original character ID. */
        public String frameKey(){return "companion118/"+characterId+"/"+form+"/"+poseIndex;}
        /** Canonical lossless packaged location. Loading and exact measured crops belong to the art registry. */
        public String sheetPath(){return "art/v118/"+characterId+"-evo"+(formLevel<10?"0":"")+formLevel+"-poses.webp";}
        public float transformX(float x,float y){double radians=Math.toRadians(rotation);return bodyDX+x*scaleX*(float)Math.cos(radians)-y*scaleY*(float)Math.sin(radians);}
        public float transformY(float x,float y){double radians=Math.toRadians(rotation);return bodyDY+x*scaleX*(float)Math.sin(radians)+y*scaleY*(float)Math.cos(radians);}
    }
    /** Measured crop and fixed ground coordinate. A crouch is drawn shorter, rather than stretched to standing height. */
    public static final class AtlasFrame {
        public final String characterId;
        public final int form,poseIndex,left,top,right,bottom;
        public final float originX,originY,pixelsPerUnit;
        public AtlasFrame(String id,int form,int poseIndex,int left,int top,int right,int bottom,float originX,float originY,float pixelsPerUnit){
            if(Catalog.character(id)==null||form<0||form>=FORMS||poseIndex<0||poseIndex>=POSES||left<0||top<0||right<=left||bottom<=top
                    ||right>16384||bottom>16384||!finite(originX)||!finite(originY)||!finite(pixelsPerUnit)||pixelsPerUnit<=0||pixelsPerUnit>16384)
                throw new IllegalArgumentException("Invalid measured companion source frame");
            characterId=id;this.form=form;this.poseIndex=poseIndex;this.left=left;this.top=top;this.right=right;this.bottom=bottom;
            this.originX=originX;this.originY=originY;this.pixelsPerUnit=pixelsPerUnit;
        }
        public String frameKey(){return "companion118/"+characterId+"/"+form+"/"+poseIndex;}
        public float localX(float pixelX){return (pixelX-originX)/pixelsPerUnit;}
        public float localY(float pixelY){return (pixelY-originY)/pixelsPerUnit+GROUND_Y;}
        public float destinationLeft(){return localX(left);}
        public float destinationTop(){return localY(top);}
        public float destinationRight(){return localX(right);}
        public float destinationBottom(){return localY(bottom);}
    }
    private CompanionMotion(){}
    public static int formForLevel(int level){requireLevel(level);return level>=10?3:level>=6?2:level>=3?1:0;}
    public static int formLevel(int form){if(form<0||form>=FORMS)throw new IllegalArgumentException("Form 0..3 required");return FORM_LEVELS[form];}
    /** Small authored detail overlays distinguish levels within a form, rather than implying ten distinct body atlases. */
    public static int detailCount(int level){int form=formForLevel(level);return level-FORM_LEVELS[form];}
    private static void requireLevel(int level){if(level<1||level>10)throw new IllegalArgumentException("Level 1..10 required");}
    private static int duration(Action action){switch(action){case WALK:return 680;case JUMP:case LAND:return 900;case HUG:return 1200;case CROUCH:return 900;case HAPPY:return 900;case SAD:return 1400;case SURPRISE:return 1100;case THINK:return 1800;default:return 2000;}}
    public static Frame sample(String id,int level,Action action,Facing facing,Stance stance,long elapsedMs,boolean reducedMotion){
        requireLevel(level);String identity=Catalog.character(id)==null?"nova":id;Action actual=action==null?Action.IDLE:action;
        Facing requested=facing==null?Facing.FRONT:facing,rendered=requested;Stance attitude=stance==null?Stance.CALM:stance;
        float phase=reducedMotion?.5f:Math.floorMod(elapsedMs,(long)duration(actual))/(float)duration(actual);
        int pose=requested.ordinal();float dx=0,dy=0,rotation=0;
        switch(actual){
            case WALK:pose=requested.ordinal()+((reducedMotion||phase>=.5f)?4:0);break;
            case JUMP:pose=reducedMotion||phase<.68f?8:9;rendered=Facing.FRONT;break;
            case LAND:pose=9;rendered=Facing.FRONT;break;
            case HUG:pose=10;rendered=Facing.FRONT;break;
            case CROUCH:pose=11;rendered=Facing.FRONT;break;
            case HAPPY:pose=12;rendered=Facing.FRONT;break;
            case SAD:pose=13;rendered=Facing.FRONT;break;
            case SURPRISE:pose=14;rendered=Facing.FRONT;break;
            case THINK:pose=15;rendered=Facing.FRONT;break;
            case STANCE:pose=16+attitude.ordinal();rendered=Facing.FRONT;break;
            default:break;
        }
        if(!reducedMotion){
            double wave=phase*Math.PI*2;
            if(actual==Action.WALK){dy=-Math.abs((float)Math.sin(wave))*.045f;rotation=(float)Math.sin(wave)*1.4f;}
            else if(actual==Action.JUMP){dy=-(float)Math.sin(Math.PI*phase)*.22f;}
            else if(actual==Action.HAPPY){dy=-Math.abs((float)Math.sin(wave))*.045f;}
            else if(actual==Action.HUG){rotation=(float)Math.sin(wave)*1.1f;}
            else if(actual==Action.IDLE||actual==Action.STANCE){dy=(float)Math.sin(wave)*.015f;}
        }
        int form=formForLevel(level);return new Frame(identity,level,form,detailCount(level),pose,requested,rendered,actual,attitude,reducedMotion,phase,dx,dy,rotation);
    }
    public static Action activityAction(String activity){
        if("walk".equals(activity))return Action.WALK;if("hug".equals(activity))return Action.HUG;
        if("rest".equals(activity)||"tea".equals(activity))return Action.CROUCH;if("read".equals(activity))return Action.THINK;
        if("play".equals(activity))return Action.JUMP;if("lamp".equals(activity))return Action.SURPRISE;return Action.STANCE;
    }
    /** Screen-space direction; a stationary companion retains its last orientation instead of flickering. */
    public static Facing facingForVelocity(float dx,float dy,Facing previous){
        Facing still=previous==null?Facing.FRONT:previous;
        if(!finite(dx)||!finite(dy)||Math.abs(dx)+Math.abs(dy)<.002f)return still;
        return Math.abs(dx)>Math.abs(dy)?(dx<0?Facing.LEFT:Facing.RIGHT):(dy<0?Facing.BACK:Facing.FRONT);
    }
    /** Smooth endpoint-preserving movement. Reduced motion only affects flourish, never saved path progress. */
    public static float movementProgress(float progress){if(!finite(progress))return 0;float t=Math.max(0,Math.min(1,progress));return t*t*(3-2*t);}
    public static Stance parseStance(String value){try{return Stance.valueOf(value);}catch(IllegalArgumentException|NullPointerException invalid){return Stance.CALM;}}
    private static boolean finite(float n){return !Float.isNaN(n)&&!Float.isInfinite(n);}
}
