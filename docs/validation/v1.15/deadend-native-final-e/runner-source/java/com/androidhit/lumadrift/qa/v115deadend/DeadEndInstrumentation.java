package com.androidhit.lumadrift.qa.v115deadend;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Intent;
import android.net.Uri;
import android.content.SharedPreferences;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** Actual public intent and SDK touches; only read-only stored state, no target engine imports. */
public final class DeadEndInstrumentation extends Instrumentation {
    private static final Pattern PIECE=Pattern.compile("Piece, row (\\d+), column (\\d+), arrow ");
    private static final String[] DIRECTIONS={"up","right","down","left"};
    private final JSONArray checks=new JSONArray(),captures=new JSONArray();
    private Bundle args;private UiAutomation ui;private File output,shared;private String target;private JSONObject fixture;private PrintWriter eventLog;private int checkCount,touchCount;
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);args=arguments==null?new Bundle():arguments;start();}
    @Override public void onStart(){new Thread(()->{
        boolean passed=false;Bundle result=new Bundle();
        try{
            target=getTargetContext().getPackageName();check(target.equals("com.androidhit.lumadrift"),"Exact production target package");
            String label=args.getString("label","");check(label.matches("[A-Za-z0-9_-]{1,50}"),"Unique safe evidence label");
            shared=new File(getTargetContext().getExternalFilesDir(null),"v115-deadend-native/"+label);String phase=args.getString("phase","prepare");output=new File(shared,phase);if(output.exists()||!output.mkdirs())throw new AssertionError("Native evidence directory must be new");
            eventLog=new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(output,"events.jsonl")),StandardCharsets.UTF_8)));
            PackageInfo info=getTargetContext().getPackageManager().getPackageInfo(target,0);check(info.versionName.equals(args.getString("expectedVersion",""))&&info.versionCode==Integer.parseInt(args.getString("expectedCode","17")),"Exact frozen candidate version and code");
            ui=getUiAutomation();AccessibilityServiceInfo service=ui.getServiceInfo();service.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;ui.setServiceInfo(service);
            fixture=new JSONObject(asset("deadend-fixture.json"));write("fixture.json",fixture.toString(2));
            if(phase.equals("cold"))cold();else prepare();passed=true;result.putString("summary","PASS "+phase+"; "+checkCount+" native checks; "+touchCount+" real touches");
        }catch(Throwable failure){result.putString("failure",failure.toString());try{if(output!=null){capture("failure");StringWriter stack=new StringWriter();failure.printStackTrace(new PrintWriter(stack));write("failure.txt",stack.toString());}}catch(Throwable ignored){}}
        finally{try{if(eventLog!=null){eventLog.flush();eventLog.close();}if(output!=null)write("report.json",new JSONObject().put("passed",passed).put("phase",args.getString("phase","prepare")).put("processId",android.os.Process.myPid()).put("checkCount",checkCount).put("touchCount",touchCount).put("checks",checks).put("captures",captures).put("scope","Actual public U challenge intent, legal wrong order, native dead-end warning/panel, free Undo, cold resume and actual challenge victory; target preferences read only; no injected balance/history/progress, provider action or private engine call").toString(2));}catch(Throwable ignored){}finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);}
    },"luma-v115-native-deadend").start();}
    private void launch(){startActivitySync(new Intent().setClassName(target,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));SystemClock.sleep(700);}
    private SharedPreferences save(){return getTargetContext().getSharedPreferences("luma_drift_save_v1",0);}
    private SharedPreferences session(){return getTargetContext().getSharedPreferences("luma_session",0);}
    private String history(){return session().getString("history","");}
    private JSONObject economy()throws Exception{JSONObject value=new JSONObject();for(String key:new TreeSet<>(save().getAll().keySet()))if(key.equals("coins")||key.contains("gold")||key.startsWith("booster_")||key.startsWith("power_")||key.contains("stars_")||key.equals("unlocked"))value.put(key,save().getAll().get(key));return value;}
    private String joined(JSONArray ids)throws Exception{StringBuilder out=new StringBuilder();for(int i=0;i<ids.length();i++){if(i>0)out.append(',');out.append(ids.getInt(i));}return out.toString();}
    private void prepare()throws Exception{
        launch();control(x->x.equals("Settings")||x.equals("Paramètres")||x.equals("Réglages"),"Settings");control(x->x.equals("English"),"English");back();JSONObject money=economy();
        Intent link=new Intent(Intent.ACTION_VIEW,Uri.parse(fixture.getString("deepLink"))).addCategory(Intent.CATEGORY_BROWSABLE).setPackage(target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        check(getTargetContext().getPackageManager().resolveActivity(link,0)!=null,"Public challenge ACTION_VIEW resolves to the installed application");event(new JSONObject().put("publicIntent",link.toUri(0)));startActivitySync(link);SystemClock.sleep(700);
        contract();state(fixture.getJSONObject("initial"),"Initial public U31");capture("public-u31-initial");
        JSONArray steps=fixture.getJSONArray("steps");for(int i=0;i<steps.length();i++)move(steps.getJSONObject(i),"legal-wrong-order");
        state(fixture.getJSONObject("deadEnd"),"Legal wrong order leaves a dead end with two legal exits");await(()->has(x->x.equals("This path is closed"),true),6000,"Native guidance warns about the real dead end");capture("public-u31-deadend-warning");
        control(x->x.equals("This path is closed"),"Open dead-end panel");await(()->has(x->x.equals("Restart · Free"),true)&&has(x->x.equals("Undo"),true),4000,"Illustrated dead-end recovery panel is visible");
        int buttons=0;for(AccessibilityNodeInfo node:nodes()){if(node.isClickable()&&node.isVisibleToUser()){String label=name(node);check(label.equals("Undo")||label.equals("Restart · Free")||label.equals("×"),"Recovery panel exposes only a free action: "+label);buttons++;}node.recycle();}check(buttons==3,"Dead-end panel offers Undo, free restart and close, with no paid continuation");capture("public-u31-deadend-panel");
        check(money.toString().equals(economy().toString()),"Wrong order and panel spend no currency or stock and change no progression");check(session().getInt("budget_extensions",-1)==0,"Dead-end panel has not applied a continuation");int undos=session().getInt("undos",0);
        control(x->x.equals("Undo"),"Free Undo from dead-end panel");state(fixture.getJSONObject("undo").getJSONObject("after"),"Undo restores the exact soluble board");check(session().getInt("undos",-1)==undos+1,"Exactly one real Undo was persisted");SystemClock.sleep(1000);check(!has(x->x.equals("This path is closed"),true),"Dead-end warning clears after restoring the soluble state");capture("public-u31-undo-restored");
        check(money.toString().equals(economy().toString()),"Free Undo preserves currency, stock and all earned progress");writeShared("checkpoint.json",new JSONObject().put("processId",android.os.Process.myPid()).put("economy",money).put("history",history()).put("undos",session().getInt("undos",-1)).put("code",fixture.getString("code")).toString(2));
    }
    private void cold()throws Exception{
        JSONObject checkpoint=new JSONObject(readShared("checkpoint.json"));check(checkpoint.getInt("processId")!=android.os.Process.myPid(),"Cold verification uses a distinct process");launch();control(x->x.equals("Resume")||x.startsWith("Resume "),"Resume retained public challenge");contract();state(fixture.getJSONObject("undo").getJSONObject("after"),"Cold resume retains the exact restored challenge");check(session().getInt("undos",-1)==checkpoint.getInt("undos"),"Cold resume preserves the real Undo count");SystemClock.sleep(700);check(!has(x->x.equals("This path is closed"),true),"Cold resumed board is free of the dead-end warning");capture("public-u31-cold-resumed");
        JSONArray solution=fixture.getJSONObject("undo").getJSONArray("solutionSteps");for(int i=0;i<solution.length();i++)move(solution.getJSONObject(i),"restored-solution");
        await(()->has(x->x.startsWith("Lovely drift!"),false)&&has(x->x.startsWith("Try again"),true),5000,"Actual restored challenge reaches native victory");check(history().equals(joined(fixture.getJSONObject("undo").getJSONObject("completed").getJSONArray("history"))),"Actual victory has the exact complete recovery history");capture("public-u31-restored-victory");check(checkpoint.getJSONObject("economy").toString().equals(economy().toString()),"Public challenge victory awards or debits no coins, stock or earned progression");
        control(x->x.equals("Back to adventure"),"Return to Adventure");control(x->x.equals("Settings"),"Settings");control(x->x.equals("Français"),"Français");back();control(x->x.equals("Aventure"),"French Adventure");capture("after-probe-adventure-fr");check("fr".equals(save().getString("language","")),"Primary QA profile is restored to French Adventure");
    }
    private void contract()throws Exception{JSONObject expected=fixture.getJSONObject("expectedSession");for(Iterator<String> it=expected.keys();it.hasNext();){String key=it.next();Object value=expected.get(key);check(String.valueOf(session().getAll().get(key)).equals(String.valueOf(value)),"Public challenge stored contract "+key+"="+value);}check(session().getInt("level",-1)==fixture.getInt("levelNumber"),"Public intent opens U31");}
    private void move(JSONObject step,String reason)throws Exception{String prefix=step.getString("accessibilityLabelPrefix");String expected=joined(step.getJSONObject("after").getJSONArray("history"));control(x->x.contains(prefix),reason+" action "+step.getInt("actionId"));await(()->history().equals(expected),3000,"One physical touch commits the expected history "+expected);check(session().getInt("budget_used",-1)==0,"Public mode4 keeps its unlimited used ledger at zero");if(!step.getJSONObject("after").optBoolean("solved",false))state(step.getJSONObject("after"),reason+" after "+step.getInt("actionId"));}
    private void state(JSONObject expected,String title)throws Exception{
        await(()->history().equals(joined(expected.getJSONArray("history"))),3500,title+": exact persisted history");awaitGates(expected.getInt("gateMask"),title+": actual gates");
        int count=0;for(AccessibilityNodeInfo node:nodes()){if(node.isVisibleToUser()&&PIECE.matcher(name(node)).find())count++;node.recycle();}check(count==expected.getInt("remainingCount"),title+": native remaining piece count");
        Set<Integer> remaining=new HashSet<>(),legal=new HashSet<>();JSONArray ids=expected.getJSONArray("remainingTileIds"),moves=expected.getJSONArray("legalActionIds");for(int i=0;i<ids.length();i++)remaining.add(ids.getInt(i));for(int i=0;i<moves.length();i++)legal.add(moves.getInt(i));JSONArray tiles=fixture.getJSONArray("tiles");
        for(int i=0;i<tiles.length();i++){JSONObject tile=tiles.getJSONObject(i);if(!remaining.contains(tile.getInt("tileId")))continue;String prefix=tile.getString("accessibilityLabelPrefix");AccessibilityNodeInfo node=find(x->x.contains(prefix),true);check(node!=null,title+": native target "+tile.getInt("tileId"));String label=name(node);node.recycle();check(label.contains(", Gate open")==legal.contains(tile.getInt("tileId")),title+": legal-exit semantics "+tile.getInt("tileId"));}
    }
    private void writeShared(String name,String data)throws Exception{try(OutputStream file=new FileOutputStream(new File(shared,name))){file.write(data.getBytes(StandardCharsets.UTF_8));}}
    private String readShared(String name)throws Exception{try(InputStream in=new FileInputStream(new File(shared,name));ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);return out.toString("UTF-8");}}
    private int gates(){int mask=0,seen=0;for(AccessibilityNodeInfo node:nodes()){String label=name(node);if(label.startsWith("Gate open")||label.startsWith("Gate closed")){for(int d=0;d<4;d++)if(Pattern.compile("\\b"+DIRECTIONS[d]+"\\b").matcher(label.toLowerCase(Locale.ROOT)).find()){seen|=1<<d;if(label.startsWith("Gate open"))mask|=1<<d;break;}}node.recycle();}if(seen!=15)return -1;return mask;}
    private void awaitGates(int expected,String description)throws Exception{long end=SystemClock.uptimeMillis()+2500;int observed=gates();while(observed!=expected&&SystemClock.uptimeMillis()<end){SystemClock.sleep(12);observed=gates();}check(observed==expected,description);}
    private static String name(AccessibilityNodeInfo node){CharSequence text=node.getContentDescription();if(text==null||text.length()==0)text=node.getText();return text==null?"":text.toString();}
    private List<AccessibilityNodeInfo> nodes(){if(Build.VERSION.SDK_INT>=33)ui.clearCache();List<AccessibilityNodeInfo> result=new ArrayList<>();AccessibilityNodeInfo root=ui.getRootInActiveWindow();if(root==null)return result;ArrayDeque<AccessibilityNodeInfo> pending=new ArrayDeque<>();pending.add(root);while(!pending.isEmpty()&&result.size()<700){AccessibilityNodeInfo node=pending.removeFirst();result.add(node);for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo child=node.getChild(i);if(child!=null)pending.addLast(child);}}return result;}
    private AccessibilityNodeInfo find(Predicate<String> predicate,boolean clickable){AccessibilityNodeInfo found=null;for(AccessibilityNodeInfo node:nodes()){Rect bounds=new Rect();node.getBoundsInScreen(bounds);if(found==null&&node.isVisibleToUser()&&node.isEnabled()&&(!clickable||node.isClickable())&&!bounds.isEmpty()&&predicate.test(name(node)))found=AccessibilityNodeInfo.obtain(node);node.recycle();}return found;}
    private boolean has(Predicate<String> predicate,boolean clickable){AccessibilityNodeInfo node=find(predicate,clickable);if(node==null)return false;node.recycle();return true;}
    private String labels(){List<String> labels=new ArrayList<>();for(AccessibilityNodeInfo node:nodes()){if(!name(node).isEmpty())labels.add(name(node));node.recycle();}return labels.toString();}
    private void control(Predicate<String> predicate,String description)throws Exception{long end=SystemClock.uptimeMillis()+4500;AccessibilityNodeInfo node;while((node=find(predicate,true))==null&&SystemClock.uptimeMillis()<end)SystemClock.sleep(35);if(node==null)throw new AssertionError("Missing native "+description+": "+labels());touchNode(node,description);SystemClock.sleep(90);}
    private void touchNode(AccessibilityNodeInfo node,String reason)throws Exception{if(node==null)throw new AssertionError("Missing node "+reason);Rect bounds=new Rect();node.getBoundsInScreen(bounds);String label=name(node);node.recycle();touch(bounds,label,reason);}
    private void touch(Rect bounds,String label,String reason)throws Exception{if(bounds.isEmpty())throw new AssertionError("Empty touch bounds");long down=SystemClock.uptimeMillis();inject(down,MotionEvent.ACTION_DOWN,bounds.exactCenterX(),bounds.exactCenterY());SystemClock.sleep(12);inject(down,MotionEvent.ACTION_UP,bounds.exactCenterX(),bounds.exactCenterY());touchCount++;event(new JSONObject().put("input","UiAutomation touchscreen").put("touch",touchCount).put("reason",reason).put("label",label).put("bounds",new JSONArray(new int[]{bounds.left,bounds.top,bounds.right,bounds.bottom})).put("level",session().getInt("level",-1)).put("uptimeMs",SystemClock.uptimeMillis()));}
    private void inject(long down,int action,float x,float y){MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);try{if(!ui.injectInputEvent(event,true))throw new AssertionError("SDK touchscreen event refused");}finally{event.recycle();}}
    private void back(){ui.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);SystemClock.sleep(150);}
    private interface Condition {boolean get()throws Exception;}
    private void await(Condition condition,long milliseconds,String description)throws Exception{long end=SystemClock.uptimeMillis()+milliseconds;boolean observed=condition.get();while(!observed&&SystemClock.uptimeMillis()<end){SystemClock.sleep(12);observed=condition.get();}check(observed,description);}
    private void check(boolean condition,String message)throws Exception{checkCount++;checks.put(new JSONObject().put("passed",condition).put("message",message));if(!condition)throw new AssertionError(message);}
    private void event(JSONObject value){if(eventLog!=null)eventLog.println(value.toString());}
    private void capture(String title)throws Exception{Bitmap bitmap=ui.takeScreenshot();if(bitmap==null)throw new IOException("Native screenshot unavailable");try(OutputStream file=new FileOutputStream(new File(output,title+".png"))){if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,file))throw new IOException("Native PNG encoding failed");}finally{bitmap.recycle();}JSONArray nodes=new JSONArray();for(AccessibilityNodeInfo node:nodes()){Rect bounds=new Rect();node.getBoundsInScreen(bounds);nodes.put(new JSONObject().put("label",name(node)).put("bounds",new JSONArray(new int[]{bounds.left,bounds.top,bounds.right,bounds.bottom})).put("clickable",node.isClickable()).put("visible",node.isVisibleToUser()));node.recycle();}write(title+".json",nodes.toString(2));captures.put(title);}
    private String asset(String file)throws Exception{try(InputStream input=getContext().getAssets().open(file);ByteArrayOutputStream bytes=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int n;while((n=input.read(buffer))!=-1)bytes.write(buffer,0,n);return bytes.toString("UTF-8");}}
    private void write(String name,String value)throws Exception{try(OutputStream file=new FileOutputStream(new File(output,name))){file.write(value.getBytes(StandardCharsets.UTF_8));}}
}
