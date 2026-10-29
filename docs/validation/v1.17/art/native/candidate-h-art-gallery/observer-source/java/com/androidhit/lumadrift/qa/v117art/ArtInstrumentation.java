package com.androidhit.lumadrift.qa.v117art;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.SystemClock;
import com.androidhit.lumadrift.data.AvatarStyles;
import com.androidhit.lumadrift.data.Catalog;
import com.androidhit.lumadrift.data.CosmeticCatalog;
import com.androidhit.lumadrift.data.SignatureOutfits;
import com.androidhit.lumadrift.ui.ArtLibrary;
import com.androidhit.lumadrift.ui.AvatarStage;
import com.androidhit.lumadrift.ui.CharacterSprites;
import com.androidhit.lumadrift.ui.CompanionPortrait;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Isolated Android Canvas QA renders. No Activity, GameStore, engine, input, save or ownership API. */
public final class ArtInstrumentation extends Instrumentation {
    private static final int W=480,H=620,BG=0xfffff8f0,INK=0xff463653;
    private final Paint text=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final CompanionPortrait portrait=new CompanionPortrait();
    private final JSONArray checks=new JSONArray(),matrix=new JSONArray(),garments=new JSONArray(),styles=new JSONArray();
    private final JSONObject images=new JSONObject();
    private Bundle args;private File output;private int renderCount;private long started;
    private static final String[] LOOKS={"dew","moon","star","harvest","heart","aurora"};

    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);args=arguments==null?new Bundle():arguments;start();}
    @Override public void onStart(){new Thread(()->runRender(),"luma117-isolated-art-qa").start();}
    private void runRender(){
        boolean passed=false;Bundle result=new Bundle();started=SystemClock.uptimeMillis();
        try{
            String target=getTargetContext().getPackageName();check(target.equals("com.androidhit.lumadrift"),"Exact production target");
            PackageInfo info=getTargetContext().getPackageManager().getPackageInfo(target,0);
            check(info.versionName.equals("1.17.0-rc6")&&info.versionCode==24,"Exact 1.17/code24 candidate");
            check(args.getString("runToken","").matches("[a-f0-9-]{36}"),"Unique host execution identity");
            String label=args.getString("label","");check(label.matches("[A-Za-z0-9_-]{1,50}"),"Bounded fresh evidence label");
            File requested=new File(getTargetContext().getExternalFilesDir(null),"v117-art-native/"+label);
            if(requested.exists())throw new AssertionError("Evidence directory already exists");
            output=requested;if(!output.mkdirs())throw new IOException("Cannot create isolated art evidence");
            write("source-manifest.json",asset("source-manifest.json"));
            if(CharacterSprites.library()==null)CharacterSprites.install(getTargetContext().getAssets());
            ArtLibrary library=CharacterSprites.library();check(library!=null,"Application original art library installed");
            check(Catalog.CHARACTERS.length==24&&CosmeticCatalog.ITEMS.size()==24&&AvatarStyles.all().size()==12,"Complete actual catalogue dimensions");
            Set<String> artHashes=new HashSet<>();
            for(Catalog.CharacterItem character:Catalog.CHARACTERS){
                check(library.preload("character/"+character.id),"Authored companion decoded: "+character.id);
                Bitmap sheet=Bitmap.createBitmap(W*3,H*2,Bitmap.Config.ARGB_8888);
                Canvas canvas=new Canvas(sheet);canvas.drawColor(BG);
                for(int index=0;index<LOOKS.length;index++){
                    String look=LOOKS[index];List<CosmeticCatalog.Item> items=look(look);
                    int x=(index%3)*W,y=(index/3)*H;canvas.save();canvas.translate(x,y);
                    caption(canvas,character.id+" / "+look,"QA RENDER · not application UI");
                    portrait.draw(canvas,character,W*.5f,310,110,AvatarStyles.find(null),AvatarStage.Pose.IDLE,0,true,null,items);
                    bottom(canvas,"24 companions × 6 complete looks",itemIds(items));canvas.restore();
                    String pixels=pixelHash(sheet,x+35,y+95,W-70,435);
                    check(artHashes.add(pixels),"Unique rendered companion/look pixels: "+character.id+"/"+look);
                    matrix.put(new JSONObject().put("character",character.id).put("outfit",look).put("cosmetics",new JSONArray(itemIdsList(items))).put("pixel_sha256",pixels).put("sheet","character-"+character.id+"-six-looks.png").put("tile",index));
                    renderCount++;
                }
                save(sheet,"character-"+character.id+"-six-looks.png");sheet.recycle();
                Bundle progress=new Bundle();progress.putString("stream","QA render "+character.id+" · "+renderCount+" composites\n");sendStatus(0,progress);
            }
            for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS){
                check(library.preload("cosmetic/"+item.id),"Authored garment decoded: "+item.id);
                Bitmap image=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);canvas.drawColor(BG);
                caption(canvas,item.id,item.slot+" · QA RENDER, not inventory evidence");
                check(CharacterSprites.drawGarmentPreview(canvas,item,35,95,W-35,515),"Native original garment preview drawn: "+item.id);
                bottom(canvas,"Original isolated garment",item.rarity.toString());
                String name="garment-"+item.id+".png";save(image,name);garments.put(new JSONObject().put("id",item.id).put("slot",item.slot.toString()).put("image",name));image.recycle();
            }
            for(String id:new String[]{"nova","halo"}){
                Catalog.CharacterItem who=null;for(Catalog.CharacterItem character:Catalog.CHARACTERS)if(character.id.equals(id))who=character;
                check(who!=null,"Style representative exists: "+id);
                Bitmap sheet=Bitmap.createBitmap(W*4,H*3,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(sheet);canvas.drawColor(BG);
                int index=0;for(AvatarStyles.Style style:AvatarStyles.all()){
                    canvas.save();canvas.translate((index%4)*W,(index/4)*H);caption(canvas,id+" / "+style.id,"QA RENDER · free visual treatment");
                    portrait.draw(canvas,who,W*.5f,310,110,style,AvatarStage.Pose.IDLE,0,true,null,look("dew"));
                    bottom(canvas,style.name("en"),"Reduced motion · time 0 · no equipped gear");canvas.restore();
                    styles.put(new JSONObject().put("character",id).put("style",style.id).put("image","styles-"+id+".png").put("tile",index++));
                }
                save(sheet,"styles-"+id+".png");sheet.recycle();
            }
            check(matrix.length()==144&&garments.length()==24&&styles.length()==24,"All 144 outfit composites, 24 garment previews and 24 style samples rendered");
            check(library.failures().isEmpty(),"No original source decode failures: "+library.failures());
            check(library.residentBytes()<=library.cacheLimitBytes(),"Shared raster cache remains within its bound");
            write("art-library.json",new JSONObject().put("failures",new JSONObject(library.failures())).put("residentBytes",library.residentBytes()).put("cacheLimitBytes",library.cacheLimitBytes()).put("residentSpriteCount",library.residentSpriteCount()).put("decodeCount",library.decodeCount()).put("evictionCount",library.evictionCount()).toString(2));
            passed=true;result.putString("summary","PASS isolated native art renders; 144 composites, 24 garment previews, 24 style samples");
        }catch(Throwable failure){result.putString("failure",failure.toString());try{if(output!=null){StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));write("failure.txt",trace.toString());}}catch(Throwable ignored){}}
        finally{try{if(output!=null)write("report.json",new JSONObject().put("passed",passed).put("runToken",args.getString("runToken","")).put("package","com.androidhit.lumadrift").put("version","1.17.0-rc6").put("code",24).put("elapsedMs",SystemClock.uptimeMillis()-started).put("checks",checks).put("composites",matrix).put("garments",garments).put("styles",styles).put("images",images).put("scope","QA RENDER: separate native Android Canvas gallery through public pure renderer APIs. Not application UI captures, inventory ownership, achieved outfits, interaction, gameplay or saved-state evidence. No Activity launch, input injection, GameStore, engine, reflection or preference access. Reduced motion, time 0, no gear overlay.").toString(2));}catch(Throwable ignored){}finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);}
    }
    private List<CosmeticCatalog.Item> look(String id)throws Exception{
        List<String> ids;if(id.equals("dew"))ids=Arrays.asList("hat_dew","dress_dew","shoes_dew","gloves_dew");
        else{SignatureOutfits.Outfit outfit=SignatureOutfits.find(id);check(outfit!=null,"Signature look exists: "+id);ids=outfit.cosmeticIds;}
        List<CosmeticCatalog.Item> result=new ArrayList<>();for(String itemId:ids){CosmeticCatalog.Item item=CosmeticCatalog.find(itemId);check(item!=null&&CharacterSprites.library().preload("cosmetic/"+itemId),"Actual original garment available: "+itemId);result.add(item);}return result;
    }
    private List<String> itemIdsList(List<CosmeticCatalog.Item> items){List<String> ids=new ArrayList<>();for(CosmeticCatalog.Item item:items)ids.add(item.id);return ids;}
    private String itemIds(List<CosmeticCatalog.Item> items){return String.join(" / ",itemIdsList(items));}
    private void caption(Canvas canvas,String title,String detail){text.setColor(INK);text.setTextSize(26);text.setFakeBoldText(true);canvas.drawText(title,22,38,text);text.setFakeBoldText(false);text.setTextSize(17);canvas.drawText(detail,22,70,text);}
    private void bottom(Canvas canvas,String title,String detail){text.setColor(INK);text.setTextSize(20);canvas.drawText(title,22,555,text);text.setTextSize(13);canvas.drawText(detail,22,584,text);}
    private void check(boolean value,String message)throws Exception{checks.put(new JSONObject().put("passed",value).put("message",message));if(!value)throw new AssertionError(message);}
    private void save(Bitmap bitmap,String name)throws Exception{File file=new File(output,name);try(OutputStream stream=new FileOutputStream(file)){if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,stream))throw new IOException("PNG encoding failed");}images.put(name,hash(file));}
    private String pixelHash(Bitmap bitmap,int x,int y,int width,int height)throws Exception{int[] row=new int[width];MessageDigest digest=MessageDigest.getInstance("SHA-256");for(int yy=y;yy<y+height;yy++){bitmap.getPixels(row,0,width,x,yy,width,1);for(int pixel:row){digest.update((byte)(pixel>>>24));digest.update((byte)(pixel>>>16));digest.update((byte)(pixel>>>8));digest.update((byte)pixel);}}return hex(digest.digest());}
    private String hash(File file)throws Exception{MessageDigest digest=MessageDigest.getInstance("SHA-256");try(InputStream stream=new FileInputStream(file)){byte[] bytes=new byte[8192];int n;while((n=stream.read(bytes))!=-1)digest.update(bytes,0,n);}return hex(digest.digest());}
    private String hex(byte[] bytes){StringBuilder text=new StringBuilder();for(byte value:bytes)text.append(String.format(Locale.ROOT,"%02x",value&255));return text.toString();}
    private String asset(String name)throws Exception{try(InputStream stream=getContext().getAssets().open(name);ByteArrayOutputStream bytes=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int count;while((count=stream.read(buffer))!=-1)bytes.write(buffer,0,count);return bytes.toString("UTF-8");}}
    private void write(String name,String value)throws Exception{try(OutputStream stream=new FileOutputStream(new File(output,name))){stream.write(value.getBytes(StandardCharsets.UTF_8));}}
}
