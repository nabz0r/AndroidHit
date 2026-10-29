package com.androidhit.lumadrift.ui;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.Log;
import com.androidhit.lumadrift.data.Catalog;
import com.androidhit.lumadrift.data.CosmeticCatalog;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Optional, atomic per-sheet v1.18 source/rig metadata. Missing exact forms use the legacy renderer explicitly. */
public final class CompanionArt118 {
    private static final String ROOT="art/v118/",TAG="LumaCompanion118";
    // Bounded for the twenty source contours and nine per-pose attachment annotations.
    private static final int MAX_METADATA_BYTES=512*1024;
    private final ArtLibrary library;
    private final Map<String,Entry> frames;
    private final Map<String,String> errors;
    public static final class Entry {
        public final CompanionMotion.AtlasFrame source;
        public final CompanionRig118.Rig rig;
        public final boolean anchorsMeasured,apparelQualified;
        public final String image;
        public final int sourceClipVertices;
        private final Path sourceClip;
        private final int replacementClipAnnotations;
        private Entry(CompanionMotion.AtlasFrame source,CompanionRig118.Rig rig,String image,boolean anchorsMeasured,boolean apparelQualified,CompanionRig118.Polygon sourceClip){
            this.source=source;this.rig=rig;this.image=image;this.anchorsMeasured=anchorsMeasured;this.apparelQualified=apparelQualified;this.sourceClip=path(sourceClip);this.sourceClipVertices=sourceClip==null?0:sourceClip.size();
            int annotations=0;for(CompanionRig118.Anchor anchor:rig.drawOrder())if(anchor.replacementClip!=null)annotations++;replacementClipAnnotations=annotations;
        }
        /** Available provisional source annotations, not a count of applied body removals. */
        public int replacementClipCount(){return replacementClipAnnotations;}
    }
    private CompanionArt118(ArtLibrary library,Map<String,Entry> frames,Map<String,String> errors){this.library=library;this.frames=Collections.unmodifiableMap(new HashMap<>(frames));this.errors=Collections.unmodifiableMap(new LinkedHashMap<>(errors));}
    public static CompanionArt118 load(AssetManager assets,ArtLibrary library){
        Map<String,Entry> loaded=new HashMap<>();Map<String,String> errors=new LinkedHashMap<>();
        try{
            String[] manifests=assets.list("art/v118");if(manifests==null)manifests=new String[0];Arrays.sort(manifests);
            for(String file:manifests){if(!file.endsWith("-poses.json"))continue;
                try{loadSheet(assets,library,ROOT+file,loaded);}catch(IOException|JSONException|IllegalArgumentException invalid){errors.put(file,invalid.getClass().getSimpleName()+": "+invalid.getMessage());Log.w(TAG,"Optional companion sheet refused: "+file+": "+invalid.getMessage());}
            }
        }catch(IOException unavailable){errors.put(ROOT,unavailable.getClass().getSimpleName()+": "+unavailable.getMessage());}
        return new CompanionArt118(library,loaded,errors);
    }
    private static void loadSheet(AssetManager assets,ArtLibrary library,String manifest,Map<String,Entry> loaded)throws IOException,JSONException{
        JSONObject root=new JSONObject(read(assets,manifest));if(integer(root,"schema")!=1||integer(root,"columns")!=4||integer(root,"rows")!=5)throw new IllegalArgumentException("Unsupported companion sheet schema");
        String id=text(root,"id");if(Catalog.character(id)==null)throw new IllegalArgumentException("Unknown companion identity");int form=integer(root,"form");if(form<0||form>=4||integer(root,"levelMilestone")!=CompanionMotion.formLevel(form))throw new IllegalArgumentException("Wrong evolution milestone");
        String image=text(root,"image");if(!image.startsWith(ROOT)||image.contains("..")||image.contains("\\")||!(image.endsWith(".png")||image.endsWith(".webp")))throw new IllegalArgumentException("Invalid packaged companion image");
        int width=integer(root,"width"),height=integer(root,"height");if(width<16||height<16||width>16384||height>16384)throw new IllegalArgumentException("Invalid source image bounds");
        BitmapFactory.Options header=new BitmapFactory.Options();header.inJustDecodeBounds=true;try(InputStream input=assets.open(image)){BitmapFactory.decodeStream(input,null,header);}if(header.outWidth!=width||header.outHeight!=height)throw new IllegalArgumentException("Manifest dimensions differ from actual image");
        if(!root.optBoolean("sourceRectsMeasured",false))throw new IllegalArgumentException("Measured source rectangles required");
        JSONArray poses=root.getJSONArray("frames");if(poses.length()!=20)throw new IllegalArgumentException("Exactly twenty distinct companion poses required");
        String[] keys=new String[20];int[][] regions=new int[20][];Map<String,Entry> incoming=new HashMap<>();
        for(int n=0;n<poses.length();n++){
            JSONObject pose=poses.getJSONObject(n);int index=integer(pose,"poseIndex");if(index<0||index>=20||keys[index]!=null)throw new IllegalArgumentException("Duplicate or invalid pose index");
            JSONArray rectangle=pose.getJSONArray("sourceRect");if(rectangle.length()!=4)throw new IllegalArgumentException("Measured source rectangle requires four integers");
            int[] region=new int[4];for(int c=0;c<4;c++)region[c]=integer(rectangle,c);if(region[0]<0||region[1]<0||region[2]<=region[0]||region[3]<=region[1]||region[2]>width||region[3]>height)throw new IllegalArgumentException("Companion crop outside actual image");
            float ppu=number(pose,"pixelsPerUnit");if(Math.abs(ppu-number(root,"pixelsPerUnit"))>.001f)throw new IllegalArgumentException("Pose scale differs from fixed standing scale");
            CompanionMotion.AtlasFrame source=new CompanionMotion.AtlasFrame(id,form,index,region[0],region[1],region[2],region[3],number(pose,"originX"),number(pose,"originY"),ppu);
            if(source.originX<region[0]-width||source.originX>region[2]+width||source.originY<0||source.originY>height+32)throw new IllegalArgumentException("Invalid source ground origin");
            JSONArray attachments=pose.getJSONArray("attachments");if(attachments.length()!=CompanionRig118.Attachment.values().length)throw new IllegalArgumentException("All nine equipment attachments required");
            List<CompanionRig118.PixelAnchor> anchors=new ArrayList<>();for(int a=0;a<attachments.length();a++){
                JSONObject anchor=attachments.getJSONObject(a);CompanionRig118.Attachment part=CompanionRig118.Attachment.valueOf(text(anchor,"name"));
                CompanionRig118.Layer layer=CompanionRig118.Layer.valueOf(text(anchor,"layer"));
                // Legacy dresses show a frontal bodice: the authored rear body/cape must occlude that panel.
                if((index==1||index==5)&&part==CompanionRig118.Attachment.DRESS)layer=CompanionRig118.Layer.FAR_LIMB;
                anchors.add(new CompanionRig118.PixelAnchor(part,number(anchor,"x"),number(anchor,"y"),number(anchor,"width"),number(anchor,"height"),number(anchor,"rotation"),anchor.getBoolean("visible"),layer,optionalBoolean(anchor,"mirrorX",false),polygon(anchor,"replacementClip",region)));
            }
            boolean measured=root.optBoolean("anchorsMeasured",false)&&pose.optBoolean("anchorsMeasured",false),qualified=measured&&root.optBoolean("apparelQualified",false)&&pose.optBoolean("apparelQualified",false);
            float neck=pose.has("garmentNeckY")?number(pose,"garmentNeckY"):Float.NaN;if(!Float.isNaN(neck)&&(neck<region[1]||neck>region[3]))throw new IllegalArgumentException("Garment neck plane outside source frame");
            CompanionRig118.Rig rig=CompanionRig118.fromAtlasPixels(source,anchors,manifest+"#"+index+(measured?" measured anchors":" provisional anchors; apparel not qualified"),measured,neck);
            CompanionRig118.Polygon sourceClip=polygon(pose,"sourceClip",region);
            String key=source.frameKey();if(loaded.containsKey(key))throw new IllegalArgumentException("Duplicate registered companion frame");keys[index]=key;regions[index]=region;incoming.put(key,new Entry(source,rig,image,measured,qualified,sourceClip==null?null:sourceClip.toLocal(source)));
        }
        int already=0;for(String key:keys)if(library.contains(key))already++;
        if(already!=0&&already!=20)throw new IllegalArgumentException("Partial existing companion sheet registry");if(already==0)library.registerRegions(image,keys,regions);
        loaded.putAll(incoming);
    }
    private static String read(AssetManager assets,String path)throws IOException{
        try(InputStream input=assets.open(path);ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];for(int count;(count=input.read(buffer))!=-1;){if(out.size()+count>MAX_METADATA_BYTES)throw new IOException("Companion metadata too large");out.write(buffer,0,count);}return out.toString(StandardCharsets.UTF_8.name());
        }
    }
    private static String text(JSONObject object,String key)throws JSONException{Object value=object.get(key);if(!(value instanceof String)||((String)value).isEmpty()||!value.equals(((String)value).trim()))throw new IllegalArgumentException("Invalid text field "+key);return (String)value;}
    private static int integer(JSONObject object,String key)throws JSONException{return integerValue(object.get(key));}
    private static int integer(JSONArray array,int index)throws JSONException{return integerValue(array.get(index));}
    private static int integerValue(Object value){if(!(value instanceof Number))throw new IllegalArgumentException("Integer required");double number=((Number)value).doubleValue();if(!Double.isFinite(number)||number!=Math.rint(number)||number<Integer.MIN_VALUE||number>Integer.MAX_VALUE)throw new IllegalArgumentException("Exact bounded integer required");return (int)number;}
    private static float number(JSONObject object,String key)throws JSONException{Object value=object.get(key);if(!(value instanceof Number))throw new IllegalArgumentException("Numeric field required "+key);float n=((Number)value).floatValue();if(!Float.isFinite(n))throw new IllegalArgumentException("Finite number required "+key);return n;}
    private static boolean optionalBoolean(JSONObject object,String key,boolean fallback)throws JSONException{if(!object.has(key))return fallback;Object value=object.get(key);if(!(value instanceof Boolean))throw new IllegalArgumentException("Boolean field required "+key);return (Boolean)value;}
    private static CompanionRig118.Polygon polygon(JSONObject object,String key,int[] region)throws JSONException{
        if(!object.has(key))return null;JSONArray supplied=object.getJSONArray(key);if(supplied.length()<3||supplied.length()>256)throw new IllegalArgumentException("Polygon requires 3..256 vertices");float[] points=new float[supplied.length()*2];
        for(int n=0;n<supplied.length();n++){JSONArray point=supplied.getJSONArray(n);if(point.length()!=2)throw new IllegalArgumentException("Polygon vertex requires two coordinates");for(int axis=0;axis<2;axis++){Object raw=point.get(axis);if(!(raw instanceof Number))throw new IllegalArgumentException("Numeric polygon coordinates required");float value=((Number)raw).floatValue();if(!Float.isFinite(value)||value<region[axis]||value>region[axis+2])throw new IllegalArgumentException("Polygon outside exact source frame");points[n*2+axis]=value;}}
        return new CompanionRig118.Polygon(points);
    }
    private static Path path(CompanionRig118.Polygon polygon){if(polygon==null)return null;Path result=new Path();result.moveTo(polygon.x(0),polygon.y(0));for(int n=1;n<polygon.size();n++)result.lineTo(polygon.x(n),polygon.y(n));result.close();return result;}
    public Entry frame(CompanionMotion.Frame frame){return frame==null?null:frames.get(frame.frameKey());}
    public int frameCount(){return frames.size();}
    public int qualifiedFrameCount(){int count=0;for(Entry entry:frames.values())if(entry.apparelQualified)count++;return count;}
    public Map<String,String> failures(){return errors;}
    public String fallbackReason(CompanionMotion.Frame frame){return frame(frame)==null?"Exact authored form/pose is unavailable: "+(frame==null?"null":frame.frameKey()):"";}
    public boolean preload(CompanionMotion.Frame frame){return frame(frame)!=null&&library.preload(frame.frameKey());}
    /**
     * Caller supplies the single shared transform for body, apparel, gear and evolution details.
     * The body stays intact under gloves and shoes: current landmark hulls are not source-part
     * segmentation, and removing their transparent surround cuts holes through the character.
     * The two replacement booleans retain the existing API; no present metadata authorizes erasure.
     */
    public boolean drawBody(Canvas canvas,CompanionMotion.Frame frame,boolean replaceHands,boolean replaceShoes){
        Entry entry=frame(frame);if(canvas==null||entry==null)return false;int save=canvas.save();
        if(entry.sourceClip!=null)canvas.clipPath(entry.sourceClip);
        CompanionMotion.AtlasFrame source=entry.source;boolean drawn=library.drawFrameUntrimmed(canvas,frame.frameKey(),source.destinationLeft(),source.destinationTop(),source.destinationRight(),source.destinationBottom(),255);canvas.restoreToCount(save);return drawn;
    }
    public boolean drawGarment(Canvas canvas,CosmeticCatalog.Item item,CompanionRig118.Anchor anchor){
        return drawGarment(canvas,item,anchor,Float.NEGATIVE_INFINITY);
    }
    /** Garment transforms and body removal share the caller's common-coordinate pose space. */
    public boolean drawGarment(Canvas canvas,CosmeticCatalog.Item item,CompanionRig118.Anchor anchor,float garmentNeckY){
        if(canvas==null||item==null||anchor==null||!anchor.visible)return false;int save=canvas.save();if(item.slot==CosmeticCatalog.Slot.DRESS&&Float.isFinite(garmentNeckY))canvas.clipRect(-6,garmentNeckY,6,6);canvas.translate(anchor.x,anchor.y);canvas.rotate(anchor.rotation);if(anchor.mirrorX)canvas.scale(-1,1);float l=-anchor.width*.5f,t=-anchor.height*.5f,r=anchor.width*.5f,b=anchor.height*.5f;
        boolean pair=item.slot==CosmeticCatalog.Slot.GLOVES||item.slot==CosmeticCatalog.Slot.SHOES;boolean left=anchor.attachment==CompanionRig118.Attachment.LEFT_GLOVE||anchor.attachment==CompanionRig118.Attachment.LEFT_SHOE;
        boolean drawn=pair?library.drawPart(canvas,"cosmetic/"+item.id,left?0:.5f,0,left?.5f:1,1,l,t,r,b,.5f,.5f,255):library.draw(canvas,"cosmetic/"+item.id,l,t,r,b,.5f,.5f,255);canvas.restoreToCount(save);return drawn;
    }
}
