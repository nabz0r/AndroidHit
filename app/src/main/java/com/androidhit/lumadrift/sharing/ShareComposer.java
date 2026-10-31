package com.androidhit.lumadrift.sharing;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import com.androidhit.lumadrift.data.Strings;
import com.androidhit.lumadrift.engine.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/** Original challenge card, rendered from the actual initial board. No fabricated rankings. */
public final class ShareComposer {
    private ShareComposer() { }
    public static final String REPOSITORY="https://github.com/nabz0r/AndroidHit";
    public static String uri(String code) { return "lumadrift://challenge/"+code; }
    private static File directory(Activity activity) throws IOException {
        File dir=new File(activity.getCacheDir(),"shared");if(!dir.isDirectory()&&!dir.mkdirs())throw new IOException("Share cache unavailable");
        File[] files=dir.listFiles();if(files!=null&&files.length>12){Arrays.sort(files,Comparator.comparingLong(File::lastModified));for(int i=0;i<files.length-10;i++)files[i].delete();}
        return dir;
    }
    private static Uri uri(Activity a,File f){return new Uri.Builder().scheme("content").authority(a.getPackageName()+".share").appendPath(f.getName()).build();}
    public static void shareChallenge(Activity activity,Level level,String code,String language,int stars,int characterColor) throws IOException {
        if(ChallengeCode.parse(code)==null)throw new IOException("Invalid challenge");
        Bitmap bitmap=card(level,code,language,stars,characterColor);
        File file=new File(directory(activity),"luma_challenge_"+System.currentTimeMillis()+".png");
        try(OutputStream stream=new FileOutputStream(file)){if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,stream))throw new IOException("Card export failed");}finally{bitmap.recycle();}
        Uri content=uri(activity,file);
        String caption=Strings.get(language,"share_caption",code,uri(code),REPOSITORY);
        Intent send=new Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,content)
                .putExtra(Intent.EXTRA_TEXT,caption).putExtra(Intent.EXTRA_TITLE,"Luma Drift · "+code);
        send.setClipData(ClipData.newUri(activity.getContentResolver(),"Luma Drift",content));
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(Intent.createChooser(send,Strings.get(language,"share_challenge")));
    }
    public static void exportJournal(Activity activity,String json,String language) throws IOException {
        File f=new File(directory(activity),"luma_journal_"+System.currentTimeMillis()+".json");
        try(Writer writer=new OutputStreamWriter(new FileOutputStream(f),java.nio.charset.StandardCharsets.UTF_8)){writer.write(json);}
        Uri content=uri(activity,f);Intent send=new Intent(Intent.ACTION_SEND).setType("application/json")
                .putExtra(Intent.EXTRA_STREAM,content).putExtra(Intent.EXTRA_TEXT,Strings.get(language,"export_note"));
        send.setClipData(ClipData.newUri(activity.getContentResolver(),"Luma Drift journal",content));
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(Intent.createChooser(send,Strings.get(language,"export_journal")));
    }
    public static void clearJournalExports(Activity activity){
        File[] files=new File(activity.getCacheDir(),"shared").listFiles();
        if(files!=null)for(File file:files)if(file.getName().matches("luma_journal_[0-9]+\\.json")){
            activity.revokeUriPermission(uri(activity,file),Intent.FLAG_GRANT_READ_URI_PERMISSION);file.delete();
        }
    }
    public static Bitmap card(Level level,String code,String lang,int stars,int color){
        Bitmap bitmap=Bitmap.createBitmap(1080,1350,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bitmap);Paint p=new Paint(3);
        p.setShader(new LinearGradient(0,0,1080,1350,new int[]{0xff10152c,0xff373159,0xff102d39},null,Shader.TileMode.CLAMP));c.drawRect(0,0,1080,1350,p);p.setShader(null);
        for(int i=0;i<45;i++){p.setColor(0x66e5d9ff);c.drawCircle((i*137+37)%1080,(i*211+53)%1350,i%4==0?3:1.5f,p);}
        text(c,p,"LUMA DRIFT",78,115,51,0xfffaf7ff,true);
        text(c,p,Strings.get(lang,"share_card_title"),78,174,29,0xff91ebd5,false);
        p.setColor(0xff252a47);c.drawRoundRect(65,220,1015,1138,48,48,p);
        float size=720f/level.size,bx=180,by=292;
        for(int row=0;row<level.size;row++)for(int col=0;col<level.size;col++){p.setColor(0x0cffffff);c.drawRoundRect(bx+col*size+6,by+row*size+6,bx+(col+1)*size-6,by+(row+1)*size-6,22,22,p);}
        for(Direction d:Direction.values()){
            boolean open=(level.initialGateMask&d.bit)!=0;
            p.setColor(open?0xff91ebd5:0xffffb398);p.setStrokeWidth(13);p.setStrokeCap(Paint.Cap.ROUND);
            c.save();c.rotate(d.ordinal()*90,bx+360,by+360);
            if(open){c.drawLine(bx+200,by-23,bx+290,by-23,p);c.drawLine(bx+430,by-23,bx+520,by-23,p);}
            else c.drawLine(bx+200,by-23,bx+520,by-23,p);
            c.restore();
        }
        ArrayList<Integer> keys=new ArrayList<>();
        for(Barrier wall:level.barriers)if(!keys.contains(wall.keyTileId))keys.add(wall.keyTileId);
        for(Barrier wall:level.barriers){
            float x=bx+(wall.col+.5f)*size,y=by+(wall.row+.5f)*size;
            sealedCell(c,p,x,y,size,keys.indexOf(wall.keyTileId)+1);
        }
        int[] colors={0xff91ebd5,0xffffb398,0xffb8a5ff,0xff9dc9ff};
        for(Tile tile:level.tiles){float x=bx+(tile.col+.5f)*size,y=by+(tile.row+.5f)*size;int shade=colors[tile.direction.ordinal()];p.setColor(shade);c.drawRoundRect(x-size*.43f,y-size*.43f,x+size*.43f,y+size*.43f,22,22,p);arrow(c,p,x,y,tile.direction,size*.27f,0xff14182e);
            if(tile.switchMask!=0){int n=Integer.bitCount(tile.switchMask),k=0;for(Direction d:Direction.values())if((tile.switchMask&d.bit)!=0){float xx=x+size*.29f,yy=y-size*.29f+k*size*.24f;p.setColor(0xff171b32);c.drawCircle(xx,yy,size*.15f,p);p.setColor(colors[d.ordinal()]);c.drawCircle(xx,yy,size*.12f,p);arrow(c,p,xx,yy,d,size*.09f,0xff171b32);k++;}}
            int key=keys.indexOf(tile.id)+1;
            if(key>0){float left=x-size*.43f,top=y-size*.43f;p.setColor(0xffffd992);c.drawRoundRect(left,top,left+size*.35f,top+size*.25f,7,7,p);text(c,p,"K"+key,left+size*.035f,top+size*.18f,size*.17f,0xff14182e,true);}
        }
        String score=stars>0?(stars==1?"★":stars==2?"★★":"★★★"):Strings.get(lang,"share_card_subtitle");
        text(c,p,score,108,1079,35,0xffffd992,true);
        p.setColor(color);c.drawCircle(907,1063,37,p);p.setColor(0xff171b32);c.drawOval(891,1053,899,1066,p);c.drawOval(916,1053,924,1066,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawArc(898,1067,917,1080,0,180,false,p);p.setStyle(Paint.Style.FILL);
        p.setColor(0xff91ebd5);c.drawRoundRect(65,1172,1015,1244,24,24,p);text(c,p,code,95,1221,29,0xff14182e,true);
        text(c,p,Strings.get(lang,"share_card_footer"),75,1303,22,0xffc3c5db,false);return bitmap;
    }
    private static void sealedCell(Canvas c,Paint p,float x,float y,float size,int key){
        p.setColor(0xff4a4056);c.drawRoundRect(x-size*.43f,y-size*.43f,x+size*.43f,y+size*.43f,22,22,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(0xffffd992);c.drawRoundRect(x-size*.43f,y-size*.43f,x+size*.43f,y+size*.43f,22,22,p);
        p.setStrokeWidth(size*.04f);c.drawArc(x-size*.12f,y-size*.28f,x+size*.12f,y-size*.02f,180,180,false,p);p.setStyle(Paint.Style.FILL);
        c.drawRoundRect(x-size*.18f,y-size*.13f,x+size*.18f,y+size*.12f,6,6,p);
        p.setColor(0xff4a4056);c.drawCircle(x,y-size*.025f,size*.025f,p);c.drawRect(x-size*.013f,y-size*.025f,x+size*.013f,y+size*.055f,p);
        String label="K"+key;p.setTypeface(com.androidhit.lumadrift.ui.PulseUi124.face(900));p.setTextSize(size*.17f);p.setColor(0xffffd992);c.drawText(label,x-p.measureText(label)/2,y+size*.32f,p);
    }
    private static void text(Canvas c,Paint p,String s,float x,float y,float size,int color,boolean bold){p.setColor(color);p.setTypeface(com.androidhit.lumadrift.ui.PulseUi124.face(bold?900:400));p.setTextSize(size);float w=p.measureText(s);if(w>925)p.setTextSize(size*925/w);c.drawText(s,x,y,p);}
    private static void arrow(Canvas c,Paint p,float x,float y,Direction d,float r,int color){c.save();c.translate(x,y);c.rotate(d.ordinal()*90);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.2f);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(color);Path path=new Path();path.moveTo(0,r*.7f);path.lineTo(0,-r*.7f);path.moveTo(-r*.5f,-r*.15f);path.lineTo(0,-r*.7f);path.lineTo(r*.5f,-r*.15f);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);c.restore();}
}
