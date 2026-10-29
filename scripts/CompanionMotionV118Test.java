import com.androidhit.lumadrift.data.Catalog;
import com.androidhit.lumadrift.ui.CompanionMotion;
import com.androidhit.lumadrift.ui.CompanionRig118;
import java.nio.file.*;
import java.util.*;

/** Pure pose/evolution and measured-anchor contracts; generated atlas pixels are qualified separately. */
public final class CompanionMotionV118Test {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static void near(float a,float b,String message){check(Math.abs(a-b)<.0001f,message+" expected"+b+" actual"+a);}
    private static CompanionMotion.Frame frame(String id,int level,CompanionMotion.Action action,CompanionMotion.Facing facing,CompanionMotion.Stance stance,long time,boolean reduced){return CompanionMotion.sample(id,level,action,facing,stance,time,reduced);}
    private static void evolution(){
        int[] forms={0,0,1,1,1,2,2,2,2,3},details={0,1,0,1,2,0,1,2,3,0};Set<String> visible=new HashSet<>();
        for(int level=1;level<=10;level++){
            CompanionMotion.Frame f=frame("nova",level,CompanionMotion.Action.IDLE,CompanionMotion.Facing.FRONT,CompanionMotion.Stance.CALM,0,true);
            check(f.form==forms[level-1]&&f.detailCount==details[level-1],"four authored forms and explicit intermediate-level details");
            check(visible.add(f.form+":"+f.detailCount),"every level has its own form/detail visual identity");
            check(f.sheetPath().equals("art/v118/nova-evo"+(f.formLevel<10?"0":"")+f.formLevel+"-poses.webp"),"path matches authored milestone naming");
        }
        check(CompanionMotion.POSES==20&&CompanionMotion.COLUMNS==4&&CompanionMotion.ROWS==5,"frozen atlas order includes four stances");
        for(int invalid:new int[]{0,11}){boolean rejected=false;try{CompanionMotion.formForLevel(invalid);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"unowned or nonexistent level cannot request an evolution");}
    }
    private static void posesAndAttachments(){
        Set<String> allKeys=new HashSet<>();
        for(Catalog.CharacterItem who:Catalog.CHARACTERS)for(int level=1;level<=10;level++)for(CompanionMotion.Action action:CompanionMotion.Action.values())for(CompanionMotion.Facing facing:CompanionMotion.Facing.values())for(boolean reduced:new boolean[]{false,true}){
            CompanionMotion.Frame f=frame(who.id,level,action,facing,CompanionMotion.Stance.READY,440,reduced);
            check(f.characterId.equals(who.id)&&f.poseIndex>=0&&f.poseIndex<20&&f.row==f.poseIndex/4&&f.column==f.poseIndex%4,"all identities retain valid authored frame order");
            check(f.requestedFacing==facing,"requested movement orientation survives semantic pose override");
            if(reduced){near(f.bodyDX,0,"reduced motion has no temporal X drift");near(f.bodyDY,0,"reduced motion has no temporal bob or jump");near(f.rotation,0,"reduced motion has no temporal tilt");}
            CompanionRig118.Rig rig=CompanionRig118.resolve(f,null);check(!rig.measured&&rig.characterId.equals(who.id)&&rig.form==f.form&&rig.poseIndex==f.poseIndex,"unmeasured art never acquires a false equipment-fit claim");
            check(rig.drawOrder().size()==CompanionRig118.Attachment.values().length,"every apparel and gear attachment has an explicit anchor");
            int last=-1;for(CompanionRig118.Anchor anchor:rig.drawOrder()){
                check(anchor.layer.ordinal()>=last,"occlusion draw order sorted across body plane");last=anchor.layer.ordinal();
                check(Float.isFinite(f.transformX(anchor.x,anchor.y))&&Float.isFinite(f.transformY(anchor.x,anchor.y)),"body and gear share one finite motion transform");
                check(anchor.width>0&&anchor.height>0,"garment retains nonzero anchor bounds");
            }
            check(rig.anchor(CompanionRig118.Attachment.AURA).layer==CompanionRig118.Layer.BACK_EFFECT,"aura remains behind the body");
            if(f.facing==CompanionMotion.Facing.RIGHT)check(rig.anchor(CompanionRig118.Attachment.LEFT_GLOVE).layer==CompanionRig118.Layer.FAR_LIMB,"far hand is occluded behind right-facing body");
            if(f.facing==CompanionMotion.Facing.LEFT)check(rig.anchor(CompanionRig118.Attachment.RIGHT_GLOVE).layer==CompanionRig118.Layer.FAR_LIMB,"far hand is occluded behind left-facing body");
            allKeys.add(f.frameKey());
        }
        check(allKeys.size()>=Catalog.CHARACTERS.length*4*16,"each companion has independent authored-form and pose registry identities");
        for(CompanionMotion.Facing facing:CompanionMotion.Facing.values()){
            check(frame("nova",1,CompanionMotion.Action.WALK,facing,null,0,false).poseIndex==facing.ordinal(),"walk first beat uses actual facing");
            check(frame("nova",1,CompanionMotion.Action.WALK,facing,null,400,false).poseIndex==facing.ordinal()+4,"walk second beat uses actual directional stride art");
            check(frame("nova",1,CompanionMotion.Action.WALK,facing,null,0,true).poseIndex==facing.ordinal()+4,"reduced motion preserves directional stride pose");
        }
        for(CompanionMotion.Stance stance:CompanionMotion.Stance.values())check(frame("nova",1,CompanionMotion.Action.STANCE,CompanionMotion.Facing.FRONT,stance,0,true).poseIndex==16+stance.ordinal(),"chosen stance has distinct authored pose and survives reduced motion");
        CompanionMotion.Action[] actions={CompanionMotion.Action.JUMP,CompanionMotion.Action.LAND,CompanionMotion.Action.HUG,CompanionMotion.Action.CROUCH,CompanionMotion.Action.HAPPY,CompanionMotion.Action.SAD,CompanionMotion.Action.SURPRISE,CompanionMotion.Action.THINK};
        for(int n=0;n<actions.length;n++)check(frame("nova",1,actions[n],CompanionMotion.Facing.BACK,null,0,true).poseIndex==8+n,"every semantic action/emotion remains an authored pose with motion reduced");
    }
    private static void measuredSources(){
        float pixelsPerUnit=266/CompanionMotion.BODY_HEIGHT;
        CompanionMotion.AtlasFrame standing=new CompanionMotion.AtlasFrame("nova",0,0,100,100,300,366,200,366,pixelsPerUnit);
        CompanionMotion.AtlasFrame crouch=new CompanionMotion.AtlasFrame("nova",0,11,100,200,300,366,200,366,pixelsPerUnit);
        near(standing.destinationTop(),-1.5f,"fixed standing scale preserves canonical top");near(standing.destinationBottom(),1.65f,"measured floor preserves existing portrait coordinate contract");
        check(crouch.destinationBottom()-crouch.destinationTop()<standing.destinationBottom()-standing.destinationTop(),"crouch is physically shorter instead of stretched to standing height");
        List<CompanionRig118.PixelAnchor> pixels=new ArrayList<>();for(CompanionRig118.Attachment part:CompanionRig118.Attachment.values())pixels.add(new CompanionRig118.PixelAnchor(part,200,300,50,60,15,true,part==CompanionRig118.Attachment.AURA?CompanionRig118.Layer.BACK_EFFECT:CompanionRig118.Layer.FRONT_GEAR));
        CompanionRig118.Rig measured=CompanionRig118.fromAtlasPixels(standing,pixels,"explicit synthetic pixel fixture; actual-pilot measurement validated separately");
        check(measured.measured&&measured.frameKey().equals(standing.frameKey()),"explicit measured metadata binds the precise pose and form");
        CompanionRig118.Anchor hand=measured.anchor(CompanionRig118.Attachment.LEFT_GLOVE);near(hand.x,0,"measured source X shares body origin");near(hand.y,standing.localY(300),"measured source Y shares body floor and scale");near(hand.width,50/pixelsPerUnit,"garment width shares fixed body scale");
        CompanionRig118.Registry empty=new CompanionRig118.Registry(),one=empty.with(measured);check(empty.size()==0&&one.size()==1,"measured metadata registry is immutable");
        CompanionMotion.Frame neutral=frame("nova",1,CompanionMotion.Action.IDLE,CompanionMotion.Facing.FRONT,null,0,true);
        check(CompanionRig118.resolve(neutral,one)==measured,"drawing resolves exact measured pose metadata");
        check(!CompanionRig118.resolve(frame("nova",3,CompanionMotion.Action.IDLE,CompanionMotion.Facing.FRONT,null,0,true),one).measured,"another evolution never borrows unqualified anchors silently");
        boolean refused=false;try{CompanionRig118.measured("nova",0,0,Collections.emptyList(),"bad");}catch(IllegalArgumentException expected){refused=true;}check(refused,"incomplete equipment metadata rejected");
        refused=false;try{empty.with(CompanionRig118.fallback(neutral));}catch(IllegalArgumentException expected){refused=true;}check(refused,"a provisional rig cannot enter the measured registry");
    }
    private static void homeAndDirection(){
        check(CompanionMotion.activityAction("hug")==CompanionMotion.Action.HUG&&CompanionMotion.activityAction("walk")==CompanionMotion.Action.WALK&&CompanionMotion.activityAction("rest")==CompanionMotion.Action.CROUCH,"home actions request actual semantic body poses");
        check(CompanionMotion.facingForVelocity(1,0,null)==CompanionMotion.Facing.RIGHT&&CompanionMotion.facingForVelocity(-1,0,null)==CompanionMotion.Facing.LEFT,"horizontal travel faces travel direction");
        check(CompanionMotion.facingForVelocity(0,-1,null)==CompanionMotion.Facing.BACK&&CompanionMotion.facingForVelocity(0,1,null)==CompanionMotion.Facing.FRONT,"vertical travel faces travel direction");
        check(CompanionMotion.facingForVelocity(0,0,CompanionMotion.Facing.LEFT)==CompanionMotion.Facing.LEFT,"stopping preserves last facing");
        check(CompanionMotion.parseStance("GUARD")==CompanionMotion.Stance.GUARD&&CompanionMotion.parseStance("bad")==CompanionMotion.Stance.CALM,"safe persisted stance parsing");
        near(CompanionMotion.movementProgress(0),0,"movement starts at saved origin");near(CompanionMotion.movementProgress(1),1,"movement reaches saved destination");float previous=-1;for(int n=0;n<=100;n++){float now=CompanionMotion.movementProgress(n/100f);check(now>=previous,"movement smoothing never reverses path progress");previous=now;}
    }
    private static void explicitSourceMasks(){
        CompanionMotion.AtlasFrame source=new CompanionMotion.AtlasFrame("nova",0,18,100,100,300,366,200,366,266/CompanionMotion.BODY_HEIGHT);
        float[] raw={160,310,200,300,215,350,170,360};CompanionRig118.Polygon nativeBoot=new CompanionRig118.Polygon(raw);raw[0]=999;
        near(nativeBoot.x(0),160,"source mask owns an immutable copy of supplied points");check(nativeBoot.size()==4,"source mask retains every annotated vertex");
        List<CompanionRig118.PixelAnchor> anchors=new ArrayList<>();
        for(CompanionRig118.Attachment part:CompanionRig118.Attachment.values())anchors.add(new CompanionRig118.PixelAnchor(part,180,330,48,66,37,true,CompanionRig118.Layer.SHOE,part==CompanionRig118.Attachment.LEFT_SHOE,part==CompanionRig118.Attachment.LEFT_SHOE?nativeBoot:null));
        CompanionRig118.Rig rig=CompanionRig118.fromAtlasPixels(source,anchors,"synthetic source mask fixture, no artwork-fit qualification",false,270);
        CompanionRig118.Anchor shoe=rig.anchor(CompanionRig118.Attachment.LEFT_SHOE);
        check(shoe.mirrorX&&shoe.rotation==37,"garment orientation and mirroring remain explicit independent annotations");
        near(shoe.replacementClip.x(0),source.localX(160),"source removal polygon shares fixed body X origin without a second garment rotation");
        near(shoe.replacementClip.y(0),source.localY(310),"source removal polygon shares fixed body Y origin without a second garment rotation");
        near(rig.garmentNeckY,source.localY(270),"dress neck plane converts the exact absolute atlas coordinate");
        check(!rig.measured,"source masks never promote provisional fitting metadata to measured");
        for(float[] invalid:new float[][]{{0,0,1,1},{0,0,1,1,2,2},{0,0,1,0,Float.NaN,1},new float[514]}){boolean rejected=false;try{new CompanionRig118.Polygon(invalid);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"invalid source-mask geometry rejected");}
        CompanionRig118.Polygon concave=new CompanionRig118.Polygon(0,0,4,0,4,4,2,2,0,4);check(concave.size()==5,"single source contour supports concave separation seams");
    }
    public static void main(String[] args)throws Exception{
        evolution();posesAndAttachments();measuredSources();homeAndDirection();explicitSourceMasks();
        String report="{\n  \"passed\": true,\n  \"checks\": "+checks+",\n  \"identities\": 37,\n  \"authored_forms_contract\": 4,\n  \"poses_per_form_contract\": 20,\n  \"scope\": \"Pure source/frame/evolution/stance, reduced-motion semantics and equipment-anchor contracts. Explicit synthetic pixel fixtures test conversion; generated atlas pixels, garment fit and native presentation must be qualified separately.\"\n}\n";
        if(args.length>0){Path output=Path.of(args[0]);Files.createDirectories(output.getParent());Files.writeString(output,report);}System.out.println(report);
    }
}
