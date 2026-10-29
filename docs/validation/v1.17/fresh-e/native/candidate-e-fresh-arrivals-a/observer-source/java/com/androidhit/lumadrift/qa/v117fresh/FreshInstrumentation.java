package com.androidhit.lumadrift.qa.v117fresh;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.*;

/** Public SDK UI only. No target class, preference, solver, renderer, reflection or seeded state. */
public final class FreshInstrumentation extends Instrumentation {
    private Bundle args;private UiAutomation ui;private File output;private long started;private int count,touches;
    private final JSONArray checks=new JSONArray(),actions=new JSONArray(),captures=new JSONArray(),wins=new JSONArray();
    private static final Pattern PIECE=Pattern.compile("^Piece, row (\\d+), column (\\d+), arrow ");
    private static final Pattern WALLET=Pattern.compile("^LumaCoins: (\\d+)$");
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);args=arguments==null?new Bundle():arguments;start();}
    @Override public void onStart(){new Thread(()->run(),"luma117-fresh-public-ui").start();}
    private void run(){boolean passed=false;Bundle result=new Bundle();started=SystemClock.uptimeMillis();
        try{
            check(getTargetContext().getPackageName().equals("com.androidhit.lumadrift"),"Exact production package");
            PackageInfo info=getTargetContext().getPackageManager().getPackageInfo("com.androidhit.lumadrift",0);check(info.versionName.equals("1.17.0-rc3")&&info.versionCode==21,"Installed E version/code21");
            check(args.getString("runToken","").matches("[a-f0-9-]{36}"),"Current execution token");String label=args.getString("label","");check(label.matches("[A-Za-z0-9_-]{1,60}"),"Bounded evidence label");
            File requested=new File(getTargetContext().getExternalFilesDir(null),"v117-fresh-native/"+label);if(requested.exists())throw new AssertionError("Evidence label already exists");output=requested;if(!output.mkdirs())throw new IOException("Cannot create evidence folder");
            ui=getUiAutomation();AccessibilityServiceInfo service=ui.getServiceInfo();service.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;ui.setServiceInfo(service);
            Intent intent=new Intent().setClassName("com.androidhit.lumadrift","com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);startActivitySync(intent);SystemClock.sleep(1800);
            String phase=args.getString("phase","onboarding");
            if(phase.equals("onboarding"))onboarding();else if(phase.equals("campaign"))campaign();else if(phase.equals("arrivals"))arrivals();else if(phase.equals("cold"))cold();else throw new IllegalArgumentException(phase);
            passed=true;result.putString("summary","PASS fresh "+phase+"; "+count+" UI checks; "+touches+" touches");
        }catch(Throwable failure){result.putString("failure",failure.toString());try{if(output!=null){if(ui!=null)capture("failure");StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));write("failure.txt",trace.toString());}}catch(Throwable ignored){}}
        finally{try{if(output!=null)write("report.json",new JSONObject().put("passed",passed).put("runToken",args.getString("runToken","")).put("phase",args.getString("phase","")).put("processId",android.os.Process.myPid()).put("checks",checks).put("checkCount",count).put("touchCount",touches).put("actions",actions).put("captures",captures).put("wins",wins).put("elapsedMs",SystemClock.uptimeMillis()-started).put("scope","New empty task-owned AVD, actual public Android UI touch observations only; no target app/engine/model/renderer/private calls, preference access, state injection, seeding, ad/account/payment action or existing AVD access. Certificates are read-only guidance for actual piece touches.").toString(2));}catch(Throwable ignored){}finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);}
    }
    private void onboarding()throws Exception{
        await(()->pieces()==3,10000,"Genuine first-launch three-piece First Light");capture("fresh-first-light-start-en");
        piece(3,1,2);capture("fresh-first-light-switch-en");piece(2,3,1);piece(4,2,0);capture("fresh-first-light-solved-en");
        tap(x->x.startsWith("Discover your little home"),"First Light finish");await(()->has(x->x.equals("All levels"),true),6000,"First Light proceeds to Adventure");
        int initial=coins();check(initial>=0,"Fresh wallet is visible");write("initial-wallet.json",new JSONObject().put("coins",initial).put("source","Actual visible LumaCoins target").toString(2));capture("fresh-adventure-en");
        tapExact("My home");await(()->has(x->x.equals("Home scene"),false),6000,"Starter home scene is accessible");capture("fresh-starter-home-en");
        tapExact("Adventure");tapExact("Settings");capture("fresh-settings-en");tapExact("Little star school");
        capture("tutorial-1-exits-en");piece(2,2,0);nextLesson();
        capture("tutorial-2-blocker-en");int before=pieces();tapPiece(2,1);check(pieces()==before,"Intentionally blocked tutorial tap preserves both pieces");capture("tutorial-2-blocked-feedback-en");piece(2,3,1);piece(2,1,0);nextLesson();
        capture("tutorial-3-key-en");tapPiece(2,1);check(pieces()==2,"Key lesson blocked attempt keeps both pieces");piece(1,1,1);piece(2,1,0);nextLesson();
        capture("tutorial-4-gate-en");tap(x->x.startsWith("Gate closed")&&x.toLowerCase(Locale.ROOT).contains("up"),"Tutorial upper gate preview");capture("tutorial-4-preview-en");tap(x->x.startsWith("Gate closed")&&x.toLowerCase(Locale.ROOT).contains("up"),"Tutorial confirmed gate open");piece(2,2,0);nextLesson();
        capture("tutorial-5-switch-en");tapPiece(2,2);check(pieces()==2,"Switch preview does not play its piece");capture("tutorial-5-preview-en");piece(2,2,1);piece(1,1,0);nextLesson();
        capture("tutorial-6-undo-en");piece(1,1,1);tapExact("Try undoing for free");await(()->pieces()==2,3000,"Real tutorial Undo restores the piece");capture("tutorial-6-refunded-en");piece(1,1,1);piece(3,3,0);nextLesson();
        capture("tutorial-7-spark-en");tapExact("Try a free Spark");check(pieces()==2,"Practice Spark reveals without playing");capture("tutorial-7-hint-en");piece(2,3,1);piece(2,1,0);nextLesson();
        await(()->has(x->x.equals("Collect my welcome gift"),true),5000,"All seven lessons actually completed");capture("tutorial-complete-gift-en");tapExact("Collect my welcome gift");capture("tutorial-gift-collected-en");
        tapExact("Start my adventure");capture("fresh-first-level-briefing-en");
    }
    private void campaign()throws Exception{
        if(has(x->x.equals("My home"),true)&&has(x->x.equals("All levels"),true))tapExact("Play");
        if(has(x->x.startsWith("Play"),true)&&pieces()==0)tap(x->x.equals("Play")||x.startsWith("Play "),"Start first real Journey");
        int reward=0;
        for(int n=1;n<=8;n++){
            final int number=n;JSONObject fixture=new JSONObject(asset("fixtures/u"+n+".json"));JSONArray exits=fixture.getJSONArray("steps");await(()->pieces()==fixture.getInt("pieces"),7000,"U"+n+" actual piece count");capture("u"+n+"-new-player-start-en");
            for(int i=0;i<exits.length();i++){JSONObject step=exits.getJSONObject(i);piece(step.getInt("row")+1,step.getInt("col")+1,exits.length()-i-1);}
            await(()->has(x->x.startsWith("Next"),true),7000,"U"+n+" actual victory object");String victory=findName(x->x.contains("LumaCoins")&&x.toLowerCase(Locale.ROOT).contains("stars"));check(victory!=null,"U"+n+" accessible actual victory/reward label");Matcher earned=Pattern.compile("\\+(\\d+) LumaCoins").matcher(victory);check(earned.find(),"Actual first-win amount is observable");int got=Integer.parseInt(earned.group(1));check(got>0,"U"+n+" genuinely earns first-win coins");reward+=got;
            wins.put(new JSONObject().put("level",number).put("victory",victory).put("reward",got).put("certificateExits",exits.length()));capture("u"+n+"-first-win-en");
            if(n<8)tap(x->x.startsWith("Next"),"Next real board");
        }
        tap(x->x.startsWith("Back to adventure"),"Return after eight real wins");capture("fresh-eight-wins-adventure-en");check(coins()>=440&&reward>=440,"At least440 coins genuinely earned by eight real wins fund both draws");write("earned-funding.json",new JSONObject().put("actual_first_win_rewards",reward).put("visible_balance",coins()).put("wins",wins).toString(2));
    }
    private void arrivals()throws Exception{
        rootCollection();int before=coins();check(before>=440,"Visible genuine wallet covers both fixed local costs");tapExact("Star arrivals");capture("fresh-avatar-arrivals-price-en");tapExact("Draw once · 180 LumaCoins");await(()->has(x->x.equals("Equip this look"),true),6000,"Actual newly drawn companion reveal");check(coins()==before-180,"Exactly180 earned coins debited by real avatar draw");capture("fresh-avatar-reveal-en");tapExact("Equip this look");capture("fresh-avatar-equipped-en");tapExact("Back to arrivals");
        tapExact("Signature outfits");capture("fresh-outfit-arrivals-price-en");tapExact("Draw once · 260 LumaCoins");await(()->has(x->x.equals("Equip this look"),true),6000,"Actual newly drawn four-piece outfit reveal");check(coins()==before-440,"Both actual draws debit exactly440 earned coins");capture("fresh-outfit-reveal-en");tapExact("Equip this look");capture("fresh-outfit-equipped-en");tapExact("Look book");tapExact("Profile");String portrait=portrait();check(portrait!=null,"Actual equipped identity and all worn slots are exposed");capture("fresh-drawn-profile-en");
        write("checkpoint.json",new JSONObject().put("coins",coins()).put("portrait",portrait).put("processId",android.os.Process.myPid()).toString(2));
    }
    private void cold()throws Exception{
        JSONObject checkpoint=new JSONObject(new String(android.util.Base64.decode(args.getString("checkpoint64",""),android.util.Base64.DEFAULT),StandardCharsets.UTF_8));check(checkpoint.getInt("processId")!=android.os.Process.myPid(),"Genuinely distinct cold process");rootCollection();tapExact("Profile");check(coins()==checkpoint.getInt("coins"),"Actual wallet retains exact post-draw balance after cold process");check(checkpoint.getString("portrait").equals(portrait()),"Actual equipped companion and all seven slots persist after cold process");capture("cold-fresh-profile-en");back();tapExact("Star arrivals");tapExact("See last arrival");check(coins()==checkpoint.getInt("coins"),"Reopening saved reveal spends no additional coins");capture("cold-fresh-last-reveal-en");tapExact("Look book");tapExact("My home");capture("cold-fresh-furnished-home-en");
    }
    private void rootCollection()throws Exception{
        for(int attempt=0;attempt<8;attempt++){
            if(has(x->x.equals("Profile"),true)&&has(x->x.equals("Star arrivals"),true))return;
            if(has(x->x.equals("Collection"),true)){tapExact("Collection");continue;}
            if(has(x->x.equals("My home"),true)&&has(x->x.equals("All levels"),true)){tapExact("My home");continue;}
            back();
        }
        throw new AssertionError("Cannot navigate to Collection: "+labels());
    }
    private String portrait(){return findName(x->x.startsWith("Equipped companion portrait:"));}
    private int coins(){String value=findName(x->WALLET.matcher(x).matches());if(value==null)throw new AssertionError("No visible exact wallet: "+labels());return Integer.parseInt(value.substring("LumaCoins: ".length()));}
    private void nextLesson()throws Exception{tap(x->x.startsWith("Next little lesson"),"Advance genuinely completed tutorial lesson");}
    private int pieces(){int n=0;for(AccessibilityNodeInfo node:nodes()){if(node.isVisibleToUser()&&PIECE.matcher(name(node)).find())n++;node.recycle();}return n;}
    private void piece(int row,int col,int remaining)throws Exception{tapPiece(row,col);await(()->pieces()==remaining,3500,"Actual piece exit at row"+row+" col"+col+" leaves"+remaining);}
    private void tapPiece(int row,int col)throws Exception{tap(x->x.startsWith("Piece, row "+row+", column "+col+", arrow "),"Observed piece "+row+","+col);}
    private void tapExact(String name)throws Exception{tap(x->x.equals(name),name);}
    private void tap(Predicate<String> predicate,String reason)throws Exception{
        long deadline=SystemClock.uptimeMillis()+6000;AccessibilityNodeInfo node=find(predicate,true);
        while(node==null&&SystemClock.uptimeMillis()<deadline){SystemClock.sleep(50);node=find(predicate,true);}
        if(node==null)throw new AssertionError("No visible "+reason+": "+labels());Rect rect=new Rect();node.getBoundsInScreen(rect);String label=name(node);node.recycle();long down=SystemClock.uptimeMillis();inject(down,MotionEvent.ACTION_DOWN,rect.exactCenterX(),rect.exactCenterY());SystemClock.sleep(25);inject(down,MotionEvent.ACTION_UP,rect.exactCenterX(),rect.exactCenterY());touches++;
        actions.put(new JSONObject().put("touch",touches).put("reason",reason).put("label",label).put("bounds",new JSONArray(new int[]{rect.left,rect.top,rect.right,rect.bottom})));SystemClock.sleep(380);
    }
    private void inject(long down,int action,float x,float y){MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);try{if(!ui.injectInputEvent(event,true))throw new AssertionError("Actual touchscreen event refused");}finally{event.recycle();}}
    private void back(){ui.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);SystemClock.sleep(400);}
    private static String name(AccessibilityNodeInfo node){CharSequence value=node.getContentDescription();if(value==null||value.length()==0)value=node.getText();return value==null?"":value.toString();}
    private List<AccessibilityNodeInfo> nodes(){if(Build.VERSION.SDK_INT>=33)ui.clearCache();List<AccessibilityNodeInfo> found=new ArrayList<>();AccessibilityNodeInfo root=ui.getRootInActiveWindow();if(root==null)return found;ArrayDeque<AccessibilityNodeInfo> queue=new ArrayDeque<>();queue.add(root);while(!queue.isEmpty()&&found.size()<700){AccessibilityNodeInfo node=queue.removeFirst();found.add(node);for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo child=node.getChild(i);if(child!=null)queue.addLast(child);}}return found;}
    private AccessibilityNodeInfo find(Predicate<String> predicate,boolean clickable){AccessibilityNodeInfo result=null;for(AccessibilityNodeInfo node:nodes()){Rect rect=new Rect();node.getBoundsInScreen(rect);if(result==null&&node.isVisibleToUser()&&node.isEnabled()&&(!clickable||node.isClickable())&&!rect.isEmpty()&&predicate.test(name(node)))result=AccessibilityNodeInfo.obtain(node);node.recycle();}return result;}
    private boolean has(Predicate<String> predicate,boolean clickable){AccessibilityNodeInfo node=find(predicate,clickable);if(node==null)return false;node.recycle();return true;}
    private String findName(Predicate<String> predicate){AccessibilityNodeInfo node=find(predicate,false);if(node==null)return null;String value=name(node);node.recycle();return value;}
    private String labels(){List<String> values=new ArrayList<>();for(AccessibilityNodeInfo node:nodes()){if(node.isVisibleToUser()&&!name(node).isEmpty())values.add(name(node));node.recycle();}return values.toString();}
    private interface Condition{boolean get()throws Exception;}
    private void await(Condition condition,long timeout,String message)throws Exception{long end=SystemClock.uptimeMillis()+timeout;boolean value=condition.get();while(!value&&SystemClock.uptimeMillis()<end){SystemClock.sleep(40);value=condition.get();}check(value,message);}
    private void check(boolean value,String message)throws Exception{count++;checks.put(new JSONObject().put("passed",value).put("message",message));if(!value)throw new AssertionError(message);}
    private void capture(String name)throws Exception{Bitmap bitmap=ui.takeScreenshot();if(bitmap==null)throw new IOException("Screenshot unavailable");try(OutputStream stream=new FileOutputStream(new File(output,name+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);}finally{bitmap.recycle();}write(name+".json",new JSONObject().put("visible_labels",labels()).put("uptimeMs",SystemClock.uptimeMillis()).toString(2));captures.put(name);}
    private String asset(String name)throws Exception{try(InputStream stream=getContext().getAssets().open(name);ByteArrayOutputStream bytes=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int n;while((n=stream.read(buffer))!=-1)bytes.write(buffer,0,n);return bytes.toString("UTF-8");}}
    private void write(String name,String value)throws Exception{try(OutputStream stream=new FileOutputStream(new File(output,name))){stream.write(value.getBytes(StandardCharsets.UTF_8));}}
}
