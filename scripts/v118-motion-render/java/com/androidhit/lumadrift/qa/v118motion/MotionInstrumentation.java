package com.androidhit.lumadrift.qa.v118motion;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.ui.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.json.*;

/** Separate Android Canvas render samples. No Activity, GameStore, engine, input or save operations. */
public final class MotionInstrumentation extends Instrumentation {
    private Bundle args; private File output;
    private final JSONArray checks=new JSONArray(),renders=new JSONArray();
    private final JSONObject images=new JSONObject();
    private final CompanionPortrait portrait=new CompanionPortrait();
    private int sourceClippedFrames,replacementClipFrames,replacementClipCount;
    private final Paint text=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int W=240,H=310,BG=0xfffff8f0;
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);args=arguments==null?new Bundle():arguments;start();}
    @Override public void onStart(){new Thread(()->runGallery(),"luma118-motion-qa").start();}
    private void runGallery(){boolean passed=false;Bundle result=new Bundle();int count=0;
        try{
            PackageInfo info=getTargetContext().getPackageManager().getPackageInfo(getTargetContext().getPackageName(),0);
            check(info.versionCode==25&&info.versionName.equals(args.getString("expectedVersion")),"Exact v118 prototype version/code");
            String token=args.getString("runToken","");check(token.matches("[a-f0-9-]{36}"),"Fresh execution token");
            output=new File(getTargetContext().getExternalFilesDir(null),"v118-motion-render/"+token);check(!output.exists()&&output.mkdirs(),"Fresh native output");
            CharacterSprites.install(getTargetContext().getAssets());CompanionArt118 art=CharacterSprites.motionArt();
            check(art!=null&&art.failures().isEmpty(),"All packaged metadata parse without refused sheets");
            boolean complete=args.getString("complete","true").equals("true");
            if(complete)check(art.frameCount()==Catalog.CHARACTERS.length*80,"Every catalogue identity x4 forms x20 exact authored frames");
            verifyBodyOverlayInvariance(art,complete);
            List<CosmeticCatalog.Item> dew=look("dew");
            AvatarLoadout gear=new AvatarLoadout(AvatarGearCatalog.ITEMS.stream().filter(x->x.slot==AvatarGearCatalog.Slot.HEAD).findFirst().get().id,AvatarGearCatalog.ITEMS.stream().filter(x->x.slot==AvatarGearCatalog.Slot.AURA).findFirst().get().id,AvatarGearCatalog.ITEMS.stream().filter(x->x.slot==AvatarGearCatalog.Slot.CHARM).findFirst().get().id);
            for(Catalog.CharacterItem who:Catalog.CHARACTERS)for(int form=0;form<4;form++){
                if(!complete&&art.frame(frame(who.id,form,0))==null)continue;
                for(int look=0;look<2;look++){
                    Bitmap sheet=Bitmap.createBitmap(W*4,H*5,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(sheet);c.drawColor(BG);
                    List<CosmeticCatalog.Item> clothing=look==0?Collections.emptyList():dew;
                    for(int pose=0;pose<20;pose++){
                        CompanionMotion.Frame motion=frame(who.id,form,pose);
                        check(art.frame(motion)!=null&&art.preload(motion),"Exact generated source decodes "+motion.frameKey());
                        if(look==0){CompanionArt118.Entry exact=art.frame(motion);if(exact.sourceClipVertices>0)sourceClippedFrames++;int masks=exact.replacementClipCount();if(masks>0)replacementClipFrames++;replacementClipCount+=masks;}
                        c.save();c.translate((pose%4)*W,(pose/4)*H);text.setColor(0xff463653);text.setTextSize(14);c.drawText(who.id+" L"+motion.formLevel+" / "+pose+" "+(look==0?"body":"dew+gear"),7,22,text);
                        portrait.draw(c,who,W*.5f,H*.50f,55,AvatarStyles.find("plush_classic"),AvatarStage.Pose.IDLE,0,true,look==0?null:gear,clothing,motion);
                        check(portrait.lastMotionFallback().isEmpty(),"No static fallback "+motion.frameKey());c.restore();count++;
                    }
                    String name=who.id+"-form"+form+"-"+(look==0?"body":"dew-gear")+".png";save(sheet,name);sheet.recycle();renders.put(new JSONObject().put("character",who.id).put("form",form).put("look",look).put("poses",20).put("image",name));
                }
                Bundle progress=new Bundle();progress.putString("stream","Rendered "+who.id+" form"+form+" / "+count+" composites\n");sendStatus(0,progress);
            }
            // Every original garment design also renders on directional, interaction and stance samples.
            for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS){Bitmap sample=Bitmap.createBitmap(W*4,H*2,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(sample);c.drawColor(BG);int idx=0;
                for(int pose:new int[]{0,1,2,3,10,11,17,18}){CompanionMotion.Frame motion=frame("nova",0,pose);c.save();c.translate((idx%4)*W,(idx/4)*H);portrait.draw(c,Catalog.character("nova"),W*.5f,H*.5f,55,AvatarStyles.find("plush_classic"),AvatarStage.Pose.IDLE,0,true,null,Collections.singletonList(item),motion);check(portrait.lastMotionFallback().isEmpty(),"Individual garment rendered "+item.id+" pose"+pose);c.restore();idx++;count++;}save(sample,"garment-"+item.id+".png");sample.recycle();}
            check(CharacterSprites.library().failures().isEmpty(),"No source decode failures");check(CharacterSprites.library().residentBytes()<=CharacterSprites.library().cacheLimitBytes(),"Native raster cache stays bounded");
            write("registry.json",new JSONObject().put("frames",art.frameCount()).put("qualifiedFrames",art.qualifiedFrameCount()).put("sourceClippedFrames",sourceClippedFrames).put("replacementClipFrames",replacementClipFrames).put("replacementClipCount",replacementClipCount).put("metadataFailures",new JSONObject(art.failures())).put("decodeFailures",new JSONObject(CharacterSprites.library().failures())).put("cacheBytes",CharacterSprites.library().residentBytes()).put("cacheLimit",CharacterSprites.library().cacheLimitBytes()).toString(2));
            passed=true;result.putString("summary","PASS "+count+" native motion render samples");
        }catch(Throwable failure){result.putString("failure",failure.toString());try{if(output!=null){StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));write("failure.txt",trace.toString());}}catch(Exception ignored){}}
        finally{try{if(output!=null)write("report.json",new JSONObject().put("passed",passed).put("runToken",args.getString("runToken")).put("version",args.getString("expectedVersion")).put("code",25).put("renders",renders).put("composites",count).put("checks",checks).put("images",images).put("scope","Isolated real Android Canvas rendering through public pure APIs. Body+dew+three gear slots across exact forms/poses and24 individual garment designs on8 Nova poses. No Activity/UI/save/ownership evidence. Successful decode/render is not anatomical visual approval.").toString(2));}catch(Exception ignored){}finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);}
    }
    /** Real Canvas pixels: requesting transparent apparel must not erase any of the authored body. */
    private void verifyBodyOverlayInvariance(CompanionArt118 art,boolean complete)throws Exception{
        JSONArray evidence=new JSONArray();String[] ids={"nova","nova","miko","tula","tula","lol_teemo"};int[] forms={0,0,0,0,0,3},poses={14,15,15,14,15,8};
        for(int n=0;n<ids.length;n++){
            CompanionMotion.Frame motion=frame(ids[n],forms[n],poses[n]);
            if(!complete&&art.frame(motion)==null)continue;
            check(art.frame(motion)!=null&&art.preload(motion),"Exact overlay-invariance source decodes "+motion.frameKey());
            Bitmap reference=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas original=new Canvas(reference);original.translate(W*.5f,H*.5f);original.scale(55,55);
            check(art.drawBody(original,motion,false,false),"Unmodified authored body renders "+motion.frameKey());
            int[] pixels=new int[W*H];reference.getPixels(pixels,0,W,0,0,W,H);int visible=0;for(int pixel:pixels)if((pixel>>>24)>0)visible++;
            check(visible>500,"Overlay comparison contains visible authored body pixels "+motion.frameKey());
            JSONArray combinations=new JSONArray();combinations.put(new JSONObject().put("hands",false).put("shoes",false).put("samePixels",true));
            for(int flags=1;flags<4;flags++){
                boolean hands=(flags&1)!=0,shoes=(flags&2)!=0;Bitmap candidate=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas layer=new Canvas(candidate);layer.translate(W*.5f,H*.5f);layer.scale(55,55);
                check(art.drawBody(layer,motion,hands,shoes),"Apparel request body renders "+motion.frameKey()+" combination"+flags);
                boolean equal=reference.sameAs(candidate);check(equal,"All authored body pixels survive apparel request "+motion.frameKey()+" hands="+hands+" shoes="+shoes);
                combinations.put(new JSONObject().put("hands",hands).put("shoes",shoes).put("samePixels",equal));candidate.recycle();
            }
            evidence.put(new JSONObject().put("frame",motion.frameKey()).put("visiblePixels",visible).put("combinations",combinations));reference.recycle();
        }
        write("body-overlay-contracts.json",evidence.toString(2));
    }
    private CompanionMotion.Frame frame(String id,int form,int pose){CompanionMotion.Action action=pose<4?CompanionMotion.Action.IDLE:pose<8?CompanionMotion.Action.WALK:pose==8?CompanionMotion.Action.JUMP:pose==9?CompanionMotion.Action.LAND:pose==10?CompanionMotion.Action.HUG:pose==11?CompanionMotion.Action.CROUCH:pose==12?CompanionMotion.Action.HAPPY:pose==13?CompanionMotion.Action.SAD:pose==14?CompanionMotion.Action.SURPRISE:pose==15?CompanionMotion.Action.THINK:CompanionMotion.Action.STANCE;return CompanionMotion.sample(id,CompanionMotion.formLevel(form),action,CompanionMotion.Facing.values()[pose<8?pose%4:0],pose>=16?CompanionMotion.Stance.values()[pose-16]:CompanionMotion.Stance.CALM,0,true);}
    private List<CosmeticCatalog.Item> look(String name){List<CosmeticCatalog.Item> result=new ArrayList<>();for(String slot:new String[]{"hat_","dress_","shoes_","gloves_"})result.add(CosmeticCatalog.find(slot+name));return result;}
    private void check(boolean value,String message)throws Exception{checks.put(new JSONObject().put("passed",value).put("message",message));if(!value)throw new AssertionError(message);}
    private void save(Bitmap bitmap,String name)throws Exception{File file=new File(output,name);try(OutputStream stream=new FileOutputStream(file)){if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,stream))throw new IOException("PNG write failed");}MessageDigest d=MessageDigest.getInstance("SHA-256");try(InputStream in=new FileInputStream(file)){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)d.update(b,0,n);}StringBuilder h=new StringBuilder();for(byte b:d.digest())h.append(String.format(Locale.ROOT,"%02x",b&255));images.put(name,h.toString());}
    private void write(String name,String value)throws Exception{try(OutputStream stream=new FileOutputStream(new File(output,name))){stream.write(value.getBytes(StandardCharsets.UTF_8));}}
}
