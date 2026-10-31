package com.androidhit.lumadrift.ui;

import com.androidhit.lumadrift.data.Catalog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-frame equipment anchors and occlusion contract. Measured authored overrides must be supplied
 * after inspecting actual atlas pixels. The legacy-derived fallback is explicitly unqualified for
 * new body art and must not be reported as demonstrated fit of a generated pose.
 */
public final class CompanionRig118 {
    public enum Attachment { HAT, DRESS, LEFT_GLOVE, RIGHT_GLOVE, LEFT_SHOE, RIGHT_SHOE, HEAD_GEAR, CHARM, AURA }
    public enum Layer { BACK_EFFECT, FAR_LIMB, BODY, DRESS, SHOE, NEAR_HAND, HAT, FRONT_GEAR }
    /** Immutable single-contour annotation. Pixel polygons and converted local polygons share this representation. */
    public static final class Polygon {
        private final float[] points;
        public Polygon(float... supplied){
            if(supplied==null||supplied.length<6||supplied.length>512||supplied.length%2!=0)throw new IllegalArgumentException("Polygon requires 3..256 coordinate pairs");
            points=supplied.clone();double area=0;
            for(int n=0;n<points.length;n+=2){if(!finite(points[n])||!finite(points[n+1])||Math.abs(points[n])>100000||Math.abs(points[n+1])>100000)throw new IllegalArgumentException("Finite bounded polygon coordinates required");int next=(n+2)%points.length;area+=(double)points[n]*points[next+1]-(double)points[next]*points[n+1];}
            if(Math.abs(area)<.000001)throw new IllegalArgumentException("Polygon must enclose an area");
        }
        public int size(){return points.length/2;}
        public float x(int index){if(index<0||index>=size())throw new IndexOutOfBoundsException();return points[index*2];}
        public float y(int index){if(index<0||index>=size())throw new IndexOutOfBoundsException();return points[index*2+1];}
        public Polygon toLocal(CompanionMotion.AtlasFrame source){if(source==null)throw new IllegalArgumentException("Atlas source required");float[] converted=new float[points.length];for(int n=0;n<size();n++){converted[n*2]=source.localX(x(n));converted[n*2+1]=source.localY(y(n));}return new Polygon(converted);}
    }
    public static final class Anchor {
        public final Attachment attachment;
        public final float x,y,width,height,rotation;
        public final boolean visible;
        public final boolean mirrorX;
        /** Provisional source-part annotation in common body coordinates; not reliable body segmentation. */
        public final Polygon replacementClip;
        public final Layer layer;
        public Anchor(Attachment attachment,float x,float y,float width,float height,float rotation,boolean visible,Layer layer){
            this(attachment,x,y,width,height,rotation,visible,layer,false,null);
        }
        public Anchor(Attachment attachment,float x,float y,float width,float height,float rotation,boolean visible,Layer layer,boolean mirrorX,Polygon replacementClip){
            if(attachment==null||layer==null||!finite(x)||!finite(y)||!finite(width)||!finite(height)||!finite(rotation)
                    ||width<=0||height<=0||Math.abs(x)>4||Math.abs(y)>4||width>6||height>6||Math.abs(rotation)>360)
                throw new IllegalArgumentException("Invalid companion attachment anchor");
            this.attachment=attachment;this.x=x;this.y=y;this.width=width;this.height=height;this.rotation=rotation;this.visible=visible;this.layer=layer;this.mirrorX=mirrorX;this.replacementClip=replacementClip;
        }
    }
    /** Annotation coordinates in the full atlas, converted using one fixed standing scale and a measured ground origin. */
    public static final class PixelAnchor {
        public final Attachment attachment;
        public final float x,y,width,height,rotation;
        public final boolean visible;
        public final boolean mirrorX;
        public final Polygon replacementClip;
        public final Layer layer;
        public PixelAnchor(Attachment attachment,float x,float y,float width,float height,float rotation,boolean visible,Layer layer){
            this(attachment,x,y,width,height,rotation,visible,layer,false,null);
        }
        public PixelAnchor(Attachment attachment,float x,float y,float width,float height,float rotation,boolean visible,Layer layer,boolean mirrorX,Polygon replacementClip){
            if(attachment==null||layer==null||!finite(x)||!finite(y)||!finite(width)||!finite(height)||!finite(rotation)
                    ||width<=0||height<=0||Math.abs(x)>100000||Math.abs(y)>100000||width>100000||height>100000||Math.abs(rotation)>360)
                throw new IllegalArgumentException("Invalid measured pixel anchor");
            this.attachment=attachment;this.x=x;this.y=y;this.width=width;this.height=height;this.rotation=rotation;this.visible=visible;this.layer=layer;this.mirrorX=mirrorX;this.replacementClip=replacementClip;
        }
    }
    public static final class Rig {
        public final String characterId,provenance;
        public final int form,poseIndex;
        /** True only for an explicitly provided, measured authored override; the pure model does not perform visual qualification. */
        public final boolean measured;
        /** Common-coordinate plane below the chin, used only for dress occlusion. */
        public final float garmentNeckY;
        private final Map<Attachment,Anchor> anchors;
        private Rig(String id,int form,int poseIndex,List<Anchor> supplied,String provenance,boolean measured){
            this(id,form,poseIndex,supplied,provenance,measured,Float.NaN);
        }
        private Rig(String id,int form,int poseIndex,List<Anchor> supplied,String provenance,boolean measured,float garmentNeckY){
            if(Catalog.character(id)==null||form<0||form>=4||poseIndex<0||poseIndex>=20||provenance==null||provenance.isEmpty()||supplied==null)
                throw new IllegalArgumentException("Invalid companion frame rig");
            EnumMap<Attachment,Anchor> copy=new EnumMap<>(Attachment.class);for(Anchor anchor:supplied)if(anchor==null||copy.put(anchor.attachment,anchor)!=null)throw new IllegalArgumentException("Duplicate companion attachment");
            if(copy.size()!=Attachment.values().length)throw new IllegalArgumentException("Every apparel and gear attachment requires an explicit anchor");
            Anchor hat=copy.get(Attachment.HAT);float neck=Float.isNaN(garmentNeckY)?hat.y+hat.height*.65f:garmentNeckY;
            if(!finite(neck)||Math.abs(neck)>4)throw new IllegalArgumentException("Invalid garment neck plane");
            characterId=id;this.form=form;this.poseIndex=poseIndex;this.provenance=provenance;this.measured=measured;anchors=Collections.unmodifiableMap(copy);this.garmentNeckY=neck;
        }
        public Anchor anchor(Attachment attachment){return anchors.get(attachment);}
        /** Draw BACK_EFFECT and FAR_LIMB, then body, then the remaining layers in enum order. */
        public List<Anchor> drawOrder(){List<Anchor> order=new ArrayList<>(anchors.values());order.sort((a,b)->{int layer=a.layer.ordinal()-b.layer.ordinal();return layer!=0?layer:a.attachment.ordinal()-b.attachment.ordinal();});return Collections.unmodifiableList(order);}
        public String frameKey(){return "companion118/"+characterId+"/"+form+"/"+poseIndex;}
    }
    /** Immutable snapshot of measured per-frame overrides, safe to hand to drawing code without mutation races. */
    public static final class Registry {
        private final Map<String,Rig> frames;
        public Registry(){frames=Collections.emptyMap();}
        private Registry(Map<String,Rig> frames){this.frames=Collections.unmodifiableMap(frames);}
        public Registry with(Rig rig){if(rig==null||!rig.measured)throw new IllegalArgumentException("Measured frame rig required");Map<String,Rig> next=new HashMap<>(frames);next.put(rig.frameKey(),rig);return new Registry(next);}
        public Rig find(String key){return frames.get(key);}
        public int size(){return frames.size();}
    }
    private CompanionRig118(){}
    public static Rig measured(String id,int form,int poseIndex,List<Anchor> anchors,String provenance){return new Rig(id,form,poseIndex,anchors,provenance,true);}
    public static Rig fromAtlasPixels(CompanionMotion.AtlasFrame source,List<PixelAnchor> supplied,String provenance){return fromAtlasPixels(source,supplied,provenance,true);}
    public static Rig fromAtlasPixels(CompanionMotion.AtlasFrame source,List<PixelAnchor> supplied,String provenance,boolean measured){
        return fromAtlasPixels(source,supplied,provenance,measured,Float.NaN);
    }
    /** garmentNeckPixelY is an absolute full-atlas coordinate, or NaN for the existing provisional head-derived plane. */
    public static Rig fromAtlasPixels(CompanionMotion.AtlasFrame source,List<PixelAnchor> supplied,String provenance,boolean measured,float garmentNeckPixelY){
        if(source==null||supplied==null)throw new IllegalArgumentException("Measured atlas frame and anchors required");
        List<Anchor> converted=new ArrayList<>();for(PixelAnchor anchor:supplied){if(anchor==null)throw new IllegalArgumentException("Null pixel anchor");
            converted.add(new Anchor(anchor.attachment,source.localX(anchor.x),source.localY(anchor.y),anchor.width/source.pixelsPerUnit,
                    anchor.height/source.pixelsPerUnit,anchor.rotation,anchor.visible,anchor.layer,anchor.mirrorX,anchor.replacementClip==null?null:anchor.replacementClip.toLocal(source)));}
        return new Rig(source.characterId,source.form,source.poseIndex,converted,provenance,measured,Float.isNaN(garmentNeckPixelY)?Float.NaN:source.localY(garmentNeckPixelY));
    }
    public static Rig resolve(CompanionMotion.Frame frame,Registry registry){
        if(frame==null)throw new IllegalArgumentException("Motion frame required");Rig measured=registry==null?null:registry.find(frame.frameKey());return measured==null?fallback(frame):measured;
    }
    private static Anchor at(Attachment item,float x,float y,float w,float h,float angle,Layer layer){return new Anchor(item,x,y,w,h,angle,true,layer);}
    /** Provisional transforms are useful for a pilot render, while the final atlas receives its own measured anchors. */
    public static Rig fallback(CompanionMotion.Frame frame){
        Catalog.CharacterItem character=Catalog.character(frame.characterId);CompanionRig legacy=CompanionRig.forCharacter(character);
        float torso=legacy.torsoX,leftX=legacy.leftHand.x,leftY=legacy.leftHand.y,leftAngle=legacy.leftHand.angle;
        float rightX=legacy.rightHand.x,rightY=legacy.rightHand.y,rightAngle=legacy.rightHand.angle;
        float leftFoot=legacy.leftFoot.x,leftBottom=legacy.leftFoot.bottom,rightFoot=legacy.rightFoot.x,rightBottom=legacy.rightFoot.bottom;
        Layer leftLayer=Layer.NEAR_HAND,rightLayer=Layer.NEAR_HAND,leftShoe=Layer.SHOE,rightShoe=Layer.SHOE;
        if(frame.facing==CompanionMotion.Facing.BACK){leftX=-leftX;rightX=-rightX;leftFoot=-leftFoot;rightFoot=-rightFoot;torso=-torso;leftAngle=-leftAngle;rightAngle=-rightAngle;}
        else if(frame.facing==CompanionMotion.Facing.LEFT||frame.facing==CompanionMotion.Facing.RIGHT){
            float shift=frame.facing==CompanionMotion.Facing.RIGHT?.1f:-.1f;torso=torso*.72f+shift;leftX=leftX*.72f+shift;rightX=rightX*.72f+shift;leftFoot=leftFoot*.72f+shift;rightFoot=rightFoot*.72f+shift;
            if(frame.facing==CompanionMotion.Facing.RIGHT){leftLayer=Layer.FAR_LIMB;leftShoe=Layer.FAR_LIMB;}else{rightLayer=Layer.FAR_LIMB;rightShoe=Layer.FAR_LIMB;}
        }
        if(frame.poseIndex>=4&&frame.poseIndex<=7){leftY-=.10f;rightY+=.10f;leftBottom-=.10f;rightFoot+=.05f;}
        else if(frame.poseIndex==8){leftY-=.20f;rightY-=.20f;leftX-=.08f;rightX+=.08f;leftBottom-=.12f;rightBottom-=.12f;}
        else if(frame.poseIndex==10){leftX=torso-.32f;rightX=torso+.32f;leftY=rightY=.40f;leftAngle=25;rightAngle=-25;}
        else if(frame.poseIndex==11||frame.poseIndex==9){leftY+=.18f;rightY+=.18f;leftX-=.05f;rightX+=.05f;}
        else if(frame.poseIndex==18){leftX=torso-.33f;rightX=torso+.33f;leftY=rightY=.08f;leftAngle=15;rightAngle=-15;}
        else if(frame.poseIndex==19){leftX=torso-.62f;rightX=torso+.62f;leftY=rightY=.35f;}
        List<Anchor> anchors=new ArrayList<>();
        anchors.add(at(Attachment.HAT,torso,-1.205f+legacy.hatY,1.96f,1.45f,0,Layer.HAT));
        anchors.add(at(Attachment.DRESS,torso,.645f,2,1.45f,0,Layer.DRESS));
        anchors.add(at(Attachment.LEFT_GLOVE,leftX,leftY,.58f,.68f,leftAngle,leftLayer));
        anchors.add(at(Attachment.RIGHT_GLOVE,rightX,rightY,.58f,.68f,rightAngle,rightLayer));
        anchors.add(at(Attachment.LEFT_SHOE,leftFoot,leftBottom-.455f,.74f,.97f,0,leftShoe));
        anchors.add(at(Attachment.RIGHT_SHOE,rightFoot,rightBottom-.455f,.74f,.97f,0,rightShoe));
        Layer gear=frame.facing==CompanionMotion.Facing.BACK?Layer.FAR_LIMB:Layer.FRONT_GEAR;
        anchors.add(at(Attachment.HEAD_GEAR,torso+1.05f,-.75f,.52f,.52f,0,gear));
        anchors.add(at(Attachment.CHARM,torso+1.02f,.96f,.42f,.42f,0,gear));
        anchors.add(at(Attachment.AURA,0,0,2.4f,2.4f,0,Layer.BACK_EFFECT));
        return new Rig(frame.characterId,frame.form,frame.poseIndex,anchors,"legacy v117 measured base; provisional pose/evolution transform",false);
    }
    private static boolean finite(float n){return !Float.isNaN(n)&&!Float.isInfinite(n);}
}
