package com.androidhit.lumadrift.qa;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import com.androidhit.lumadrift.qa.atlas.data.AvatarStyles;
import com.androidhit.lumadrift.qa.atlas.data.Catalog;
import com.androidhit.lumadrift.qa.atlas.ui.AvatarStage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import org.json.JSONArray;
import org.json.JSONObject;

/** Test-only real Android Canvas exporter. Does not start an Activity or edit a save. */
public final class AvatarAtlasInstrumentation extends Instrumentation {
    private final AvatarStage stage = new AvatarStage();
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final JSONArray files = new JSONArray();
    private File output;

    @Override public void onCreate(Bundle arguments) { super.onCreate(arguments); start(); }

    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            output = new File(getTargetContext().getExternalFilesDir(null), "avatar-v19");
            if (!output.isDirectory() && !output.mkdirs()) throw new IllegalStateException("Cannot create export folder");
            for (AvatarStyles.Style style : AvatarStyles.all()) atlas(style);
            poses(); backdrops();
            JSONObject report = new JSONObject();
            report.put("passed", true); report.put("renderer", "Native Android Canvas/Bitmap PNG export");
            report.put("application", getTargetContext().getPackageName());
            report.put("sdk_int", android.os.Build.VERSION.SDK_INT);
            report.put("variants", 288); report.put("styles", 12); report.put("companions", 24);
            report.put("notes", "Exported art, not screenshots of the integrated app; no playthrough, frame-rate or appeal claim.");
            report.put("files", files);
            try (FileOutputStream stream = new FileOutputStream(new File(output, "native-export.json"))) {
                stream.write((report.toString(2) + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            result.putString("output", output.getAbsolutePath());
            result.putString("summary", "Native export complete: 12 companion atlases, pose sheet, backdrop contact sheet.");
            finish(-1, result);
        } catch (Throwable failure) {
            result.putString("error", failure.toString()); finish(0, result);
        }
    }

    private void atlas(AvatarStyles.Style style) throws Exception {
        int width=960, height=792;
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        stage.drawBackdrop(canvas,width,height,style.id,.37f,0,true);
        label(canvas,style.name("en")+" · 24 companions",24,35,23);
        for(int i=0;i<Catalog.CHARACTERS.length;i++) {
            float x=(i%6)*160+80, top=56+(i/6)*184;
            text.setColor(0xbfffffff);
            canvas.drawRoundRect(x-74,top+6,x+74,top+176,20,20,text);
            stage.draw(canvas,Catalog.CHARACTERS[i],x,top+87,44,style.id,AvatarStage.Pose.IDLE,0,true);
            label(canvas,Catalog.CHARACTERS[i].id,x-64,top+164,16);
        }
        save(bitmap,"atlas-"+style.id+".png",style.id);
    }

    private void poses() throws Exception {
        int width=800,height=1984;
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);canvas.drawColor(0xfffff9f5);
        AvatarStage.Pose[] poses=AvatarStage.Pose.values();
        for(int i=0;i<poses.length;i++) label(canvas,poses[i].name(),i*160+15,29,17);
        for(int row=0;row<AvatarStyles.all().size();row++) {
            AvatarStyles.Style style=AvatarStyles.all().get(row);
            float top=48+row*160;
            label(canvas,style.name("en"),10,top+15,14);
            for(int col=0;col<poses.length;col++) stage.draw(canvas,Catalog.CHARACTERS[row*2],col*160+80,top+90,37,style.id,poses[col],0,true);
        }
        save(bitmap,"pose-contact-sheet.png","all");
    }

    private void backdrops() throws Exception {
        int width=800,height=960;
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);canvas.drawColor(0xfffff9f5);
        for(int i=0;i<AvatarStyles.all().size();i++) {
            AvatarStyles.Style style=AvatarStyles.all().get(i);
            canvas.save(); canvas.translate(i%4*200,i/4*320);
            canvas.clipRect(0,0,200,320);
            stage.drawBackdrop(canvas,200,320,style.id,.6f,0,true);
            label(canvas,style.name("en"),9,24,13);
            stage.draw(canvas,Catalog.CHARACTERS[i*2],100,149,40,style.id,AvatarStage.Pose.HAPPY,0,true);
            canvas.restore();
        }
        save(bitmap,"backdrop-contact-sheet.png","all");
    }

    private void label(Canvas canvas,String label,float x,float y,float size) {
        text.setColor(0xff40314f);text.setStyle(Paint.Style.FILL);text.setTextSize(size);
        canvas.drawText(label,x,y,text);
    }

    private void save(Bitmap bitmap,String name,String style) throws Exception {
        File file=new File(output,name);
        try(FileOutputStream stream=new FileOutputStream(file)) {
            if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,stream)) throw new IllegalStateException("PNG encoder failed");
        }
        MessageDigest digest=MessageDigest.getInstance("SHA-256");
        byte[] bytes=new byte[8192];int read;
        try(FileInputStream stream=new FileInputStream(file)) { while((read=stream.read(bytes))!=-1) digest.update(bytes,0,read); }
        StringBuilder hash=new StringBuilder();for(byte value:digest.digest())hash.append(String.format(java.util.Locale.ROOT,"%02x",value&255));
        JSONObject item=new JSONObject();item.put("name",name);item.put("style",style);item.put("width",bitmap.getWidth());item.put("height",bitmap.getHeight());item.put("bytes",file.length());item.put("sha256",hash.toString());files.put(item);
        bitmap.recycle();
    }
}
