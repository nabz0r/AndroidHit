package com.androidhit.lumadrift.qa.v115upgrade;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
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

/** Read-only preference observer. State changes occur only through visible native controls/touch. */
public final class UpgradeInstrumentation extends Instrumentation {
    private static final String TARGET="com.androidhit.lumadrift.sandbox";
    private static final String[] SESSION_KEYS={"active","mode","level","event","event_id","history","challenge_code","content_version","budget_policy","run_id","budget_used","budget_extensions","budget_extra","undos","hints","shield_charges","gear_grace"};
    private static final String[] SKIP={"I know how to play","Je sais déjà jouer","Ich kenne das Spiel","Ya sé jugar","Já sei jogar","So già giocare","Later","Plus tard","Später","Más tarde","Mais tarde","Più tardi"};
    private static final String[] SETTINGS={"Settings","Paramètres","Réglages","Einstellungen","Ajustes","Definições","Impostazioni"};
    private static final String LEGACY_ROOM="1|3,3|bed,0,0,0;lamp,5,0,0;plant,0,5,0;rug,2,2,0;cushion,4,4,0;";
    private final JSONArray checks=new JSONArray(),actions=new JSONArray(),captures=new JSONArray();
    private Bundle args;private UiAutomation ui;private File output;private JSONObject fixture;private String phase;private int count;
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);args=arguments==null?new Bundle():arguments;start();}
    @Override public void onStart(){new Thread(()->{
        boolean passed=false;Bundle result=new Bundle();
        try{
            phase=args.getString("phase","");if(!phase.equals("old")&&!phase.equals("new"))throw new IllegalArgumentException("Explicit old/new phase required");
            if(!TARGET.equals(getTargetContext().getPackageName()))throw new AssertionError("Lab target only");
            output=new File(getTargetContext().getExternalFilesDir(null),"v115-upgrade-ui");if(!output.exists()&&!output.mkdirs())throw new IOException("Cannot create output");
            ui=getUiAutomation();AccessibilityServiceInfo service=ui.getServiceInfo();service.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;ui.setServiceInfo(service);
            fixture=new JSONObject(readAsset("fixtures/n15.json"));
            PackageInfo info=getTargetContext().getPackageManager().getPackageInfo(TARGET,0);
            check(info.versionCode==Integer.parseInt(args.getString("expectedCode",phase.equals("old")?"16":"17"))&&info.versionName.equals(args.getString("expectedVersion",phase.equals("old")?"1.14.0-sandbox-rc2":"1.15.0-sandbox-rc1")),"Exact installed Lab version and code");
            if(phase.equals("old"))oldPhase();else newPhase();passed=true;result.putString("summary","PASS "+count+" native upgrade checks");
        }catch(Throwable failure){result.putString("failure",failure.toString());try{if(output!=null){capture("failure");StringWriter stack=new StringWriter();failure.printStackTrace(new PrintWriter(stack));write("failure-"+phase+".txt",stack.toString());}}catch(Throwable ignored){}}
        finally{
            try{if(output!=null)write("report-"+phase+".json",new JSONObject().put("passed",passed).put("checkCount",count).put("phase",phase).put("checks",checks).put("actions",actions).put("captures",captures).put("processId",android.os.Process.myPid()).put("scope","Native Lab-only upgrade: observed first real N15 piece exit, preserved historical challenge/session/coins/ownership, untouched six-by-six placement record and actual resumed board geometry. Preferences are read-only; no app/engine calls or synthetic progress. Optional genuine130-coin cactus purchase and explicit placement when requested; optional finish only when explicitly requested.").toString(2));}catch(Throwable ignored){}
            finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
        }
    },"luma-native-upgrade-observer").start();}
    private void oldPhase()throws Exception{
        check(!new File(output,"checkpoint-old.json").exists(),"Fresh upgrade checkpoint; no previous observation overwritten");
        if(args.getString("resumeObservedOld","false").equals("true")){resumeObservedOld();return;}
        launch(null);for(int i=0;i<4&&!has(x->Arrays.asList(SETTINGS).contains(x),true);i++){if(has(x->Arrays.asList(SKIP).contains(x),true))tap(x->Arrays.asList(SKIP).contains(x),true);else back();}
        english();home();top();capture("old-house-before");
        String oldRecord=save().getString("home_layout_v113",LEGACY_ROOM);
        tap(x->(x.startsWith("Come here")||x.startsWith("Move here"))&&x.replace(" ","").contains("(1,4)"),false);
        SystemClock.sleep(1100);String moved=save().getString("home_layout_v113","");check(moved.startsWith("1|1,4|")&&!moved.equals(oldRecord),"Actual observed floor tile moves and saves companion in original six-by-six room");capture("old-house-moved");
        JSONObject saveBefore=criticalSave();launch("lumadrift://challenge/"+fixture.getString("code"));dismissHelp();waitFor(x->x.startsWith("Piece, row "),true,4000);capture("old-n15-initial");
        check(session().getBoolean("active",false)&&session().getInt("mode",-1)==4&&fixture.getString("code").equals(session().getString("challenge_code",""))&&session().getString("history","bad").isEmpty(),"Genuine historical N15 challenge starts empty through public deep link");
        JSONObject first=fixture.getJSONArray("steps").getJSONObject(0);check(first.getString("type").equals("EXIT"),"Read-only host certificate begins with a genuine piece exit");piece(first);confirmIfPresent();
        waitHistory(Integer.toString(first.getInt("action")));check(session().getInt("budget_used",-1)==0&&session().getInt("budget_policy",-1)==4,"Real N15 exit uses historical policy4; unlimited friend mode keeps its budget counter at zero");
        check(criticalSave().toString().equals(saveBefore.toString()),"Partial standalone N15 grants no fake coins, ownership or campaign progress");capture("old-n15-partial");
        JSONObject checkpoint=new JSONObject().put("session",stableSession()).put("criticalSave",criticalSave()).put("homeRecord",moved).put("geometry",geometry()).put("fullSessionReadOnly",all(session())).put("fullSaveReadOnly",all(save())).put("oldPid",android.os.Process.myPid()).put("challengeCode",fixture.getString("code")).put("firstAction",first.getInt("action"));
        check(checkpoint.getJSONArray("geometry").length()==fixture.getInt("pieces")-1,"Native board exposes four remaining pieces after the exit");write("checkpoint-old.json",checkpoint.toString(2));
    }
    /** Continue the genuine first exit already captured by a preserved failed observation. */
    private void resumeObservedOld()throws Exception{
        String moved=save().getString("home_layout_v113","");int first=fixture.getJSONArray("steps").getJSONObject(0).getInt("action");
        check(moved.startsWith("1|1,4|")&&save().getInt("coins",-1)==180,"Retained old profile still has the genuinely moved6x6 house and exact180 starter coins");
        check(session().getBoolean("active",false)&&session().getInt("mode",-1)==4&&fixture.getString("code").equals(session().getString("challenge_code",""))&&session().getString("history","").equals(Integer.toString(first)),"Preserved failed run contains exactly its already observed first N15 exit");
        check(session().getInt("budget_policy",-1)==4&&session().getInt("budget_used",-1)==0,"Preserved unlimited N15 uses policy4 and correctly retains a zero budget counter");
        JSONObject stable=stableSession(),saved=criticalSave();launch(null);if(!has(x->x.startsWith("Piece, row "),true))tap(x->x.startsWith("Resume"),true);dismissHelp();waitFor(x->x.startsWith("Piece, row "),true,4500);
        check(stable.toString().equals(stableSession().toString())&&saved.toString().equals(criticalSave().toString()),"Continuation observes the exact retained session and room without replay, reset or preference edits");capture("old-n15-retained-first-exit");
        actions.put(new JSONObject().put("continuationOfPriorNativeReportSha256",args.getString("priorOldReportSha","")).put("scope","The first exit was already touched and captured in the preserved earlier run; no new exit is claimed in this continuation."));
        JSONObject checkpoint=new JSONObject().put("session",stableSession()).put("criticalSave",criticalSave()).put("homeRecord",moved).put("geometry",geometry()).put("fullSessionReadOnly",all(session())).put("fullSaveReadOnly",all(save())).put("oldPid",android.os.Process.myPid()).put("challengeCode",fixture.getString("code")).put("firstAction",first).put("continuedPriorNativeReportSha256",args.getString("priorOldReportSha",""));
        check(checkpoint.getJSONArray("geometry").length()==fixture.getInt("pieces")-1,"Retained native N15 exposes the same four remaining pieces");write("checkpoint-old.json",checkpoint.toString(2));
    }
    private void newPhase()throws Exception{
        JSONObject previous=new JSONObject(read("checkpoint-old.json"));check(previous.getInt("oldPid")!=android.os.Process.myPid(),"Upgrade observation runs in a new app process");
        check(previous.getJSONObject("criticalSave").toString().equals(criticalSave().toString()),"Installed upgrade initially retains exact historical coins, ownership and room record");
        launch(null);dismissHelp();
        if(!has(x->x.startsWith("Piece, row "),true))tap(x->x.startsWith("Resume"),true);
        dismissHelp();waitFor(x->x.startsWith("Piece, row "),true,4500);
        check(previous.getJSONObject("session").toString().equals(stableSession().toString()),"Old N15 active session, exact history, run id and budget policy survive native upgrade and Resume");
        check(previous.getJSONArray("geometry").toString().equals(geometry().toString()),"Resumed N15 keeps exact observed rows, columns and arrow directions");
        check(previous.getJSONObject("criticalSave").toString().equals(criticalSave().toString()),"Resume preserves coins, earned ownership and all original room placement bytes");capture("new-n15-resumed");
        home();top();capture("new-house-migrated");check(previous.getString("homeRecord").equals(save().getString("home_layout_v113","")),"Viewing enlarged house preserves the exact saved six-by-six room record and companion position");
        check(previous.getJSONObject("criticalSave").toString().equals(criticalSave().toString()),"Viewing enlarged house grants no ownership and consumes no coins");
        tap(x->x.equals("Adventure")||x.equals("Adventure  →"),false);tap(x->x.startsWith("Resume"),true);dismissHelp();
        check(previous.getJSONObject("session").toString().equals(stableSession().toString()),"Home and Adventure round trip retains the exact old partial challenge");capture("new-n15-after-house-roundtrip");
        if(args.getString("purchaseCactus","false").equals("true"))purchaseCactus(previous);
        if(args.getString("finishOld","false").equals("true"))finishOldAndStartNew();
        write("checkpoint-new.json",new JSONObject().put("session",stableSession()).put("criticalSave",criticalSave()).put("fullSessionReadOnly",all(session())).put("fullSaveReadOnly",all(save())).put("geometry",geometry()).toString(2));
    }
    /** A bounded paid-item route using only the same native controls available to a player. */
    private void purchaseCactus(JSONObject previous)throws Exception{
        check(save().getInt("coins",-1)==180&&!save().getBoolean("home_owned_v113_cactus",false),"Cactus purchase begins with actual180 starter coins and no paid cactus ownership");
        String originalRoom=save().getString("home_layout_v113","");
        check(originalRoom.equals(previous.getString("homeRecord")),"Paid-item route starts after exact native house migration proof");
        home();top();
        if(has(x->x.equals("Living room"),true))tap(x->x.equals("Living room"),false);
        check(save().getString("home_room_v115","living").equals("living"),"Real room control selects Living room before purchase and placement");
        tap(x->x.equals("Decorate"),true);tap(x->x.equals("Furniture"),true);tap(x->x.startsWith("Desert cactus,"),true);capture("new-cactus-unowned-preview");
        check(save().getInt("coins",-1)==180&&!save().getBoolean("home_owned_v113_cactus",false)&&originalRoom.equals(save().getString("home_layout_v113","")),"Actual cactus preview does not charge, grant ownership or place anything");
        tap(x->x.equals("Get for 130 LumaCoins"),true);
        check(save().getInt("coins",-1)==50&&save().getBoolean("home_owned_v113_cactus",false),"Real visible purchase debits exactly130 LumaCoins and grants one permanent cactus ownership");
        check(originalRoom.equals(save().getString("home_layout_v113","")),"Purchase never silently places or relocates furniture");capture("new-cactus-owned-preview");
        tap(x->x.equals("Place"),true);capture("new-cactus-placement-preview");
        check(originalRoom.equals(save().getString("home_layout_v113","")),"Room placement preview leaves saved placements untouched");
        tap(x->x.equals("Cancel"),true);
        check(originalRoom.equals(save().getString("home_layout_v113",""))&&save().getInt("coins",-1)==50&&save().getBoolean("home_owned_v113_cactus",false),"Visible Cancel discards only the placement preview and retains exact purchase and old room");capture("new-cactus-placement-cancelled");
        tap(x->x.equals("Furniture"),true);tap(x->x.startsWith("Desert cactus,"),true);tap(x->x.equals("Place"),true);tap(x->x.equals("Rotate"),true);capture("new-cactus-quarter-turn-preview");
        check(originalRoom.equals(save().getString("home_layout_v113","")),"Real Rotate changes the draft without changing saved room or charging again");
        tap(x->x.equals("Place"),true);String placed=save().getString("home_layout_v113","");JSONObject cactus=validatePlacedCactus(originalRoom,placed);
        check(save().getInt("coins",-1)==50&&save().getBoolean("home_owned_v113_cactus",false),"Actual placement and quarter-turn consume no second payment");
        check(has(x->x.equals("Desert cactus"),true),"Actual placed cactus exposes its own visible native object label");capture("new-cactus-rotated-placed");
        write("cactus-purchase-proof.json",new JSONObject().put("passed",true).put("coinsBefore",180).put("price",130).put("coinsAfter",50).put("ownershipKey","home_owned_v113_cactus").put("roomBefore",originalRoom).put("roomAfter",placed).put("placement",cactus).put("scope","One genuine paid cactus acquisition, owned preview, visible Cancel without placement, explicit Place/Rotate/Place in Living room. Record parsed independently from documented values; no app layout model invoked.").toString(2));
        if(has(x->x.equals("Done"),true))tap(x->x.equals("Done"),true);
        tap(x->x.equals("Adventure"),false);tap(x->x.startsWith("Resume"),true);dismissHelp();
        check(previous.getJSONObject("session").toString().equals(stableSession().toString()),"Paid decoration route leaves exact historical partial N15 session untouched");capture("new-n15-after-paid-cactus");
    }
    /** Independent record check: existing five starter objects plus the single acquired cactus. */
    private JSONObject validatePlacedCactus(String before,String after)throws Exception{
        String[] old=before.split("\\|",-1),next=after.split("\\|",-1);
        check(old.length==3&&next.length==3&&next[0].equals("2")&&old[1].equals(next[1]),"Actual placement saves complete version2 ten-by-ten record and keeps companion coordinates");
        Map<String,String> original=new TreeMap<>(),placed=new TreeMap<>();for(String value:old[2].split(";"))if(!value.isEmpty())original.put(value.split(",",-1)[0],value);
        int[][] occupancy=new int[10][10];String[] companion=next[1].split(",",-1);check(companion.length==2,"Room companion record contains exactly two coordinates");int cx=Integer.parseInt(companion[0]),cy=Integer.parseInt(companion[1]);check(cx>=0&&cx<10&&cy>=0&&cy<10,"Companion remains within the enlarged floor");
        JSONObject cactus=null;
        for(String value:next[2].split(";"))if(!value.isEmpty()){
            String[] entry=value.split(",",-1);check(entry.length==4,"Every recorded object has one id, position and orientation");String id=entry[0];check(placed.put(id,value)==null,"No duplicate furniture ids in committed room");int x=Integer.parseInt(entry[1]),y=Integer.parseInt(entry[2]),rotation=Integer.parseInt(entry[3]);check(rotation==0||rotation==1,"Committed orientation is valid");
            int width,depth;boolean solid=true;switch(id){case "bed":width=2;depth=1;break;case "rug":width=2;depth=2;solid=false;break;case "lamp":case "plant":case "cushion":case "cactus":width=1;depth=1;break;default:throw new AssertionError("Unexpected furniture outside bounded native cactus route: "+id);}
            if(rotation==1){int swap=width;width=depth;depth=swap;}check(x>=0&&y>=0&&x+width<=10&&y+depth<=10,"Entire committed object footprint is inside10x10 floor");
            if(solid)for(int xx=x;xx<x+width;xx++)for(int yy=y;yy<y+depth;yy++){check(++occupancy[xx][yy]==1&&!(cx==xx&&cy==yy),"Committed solid furniture does not overlap another object or companion");}
            if(id.equals("cactus")){check(rotation==1,"Actual Rotate persists a quarter-turn for the acquired cactus");cactus=new JSONObject().put("id",id).put("x",x).put("y",y).put("rotation",rotation).put("width",width).put("depth",depth);}
        }
        check(cactus!=null&&placed.size()==original.size()+1,"Exactly one acquired cactus is added to the original furnished room");
        for(Map.Entry<String,String> entry:original.entrySet())check(entry.getValue().equals(placed.get(entry.getKey())),"Original object placement preserved exactly: "+entry.getKey());return cactus;
    }
    private void finishOldAndStartNew()throws Exception{
        JSONArray steps=fixture.getJSONArray("steps");for(int i=1;i<steps.length();i++){JSONObject step=steps.getJSONObject(i);String before=session().getString("history","");if(step.getString("type").equals("EXIT"))piece(step);else tap(x->x.contains("Gate closed")&&x.toLowerCase(Locale.ROOT).contains(step.optString("directionLabel")),false);confirmIfPresent();if(i+1<steps.length())waitHistory(before+","+step.getInt("action"));else SystemClock.sleep(500);}
        check(!session().getBoolean("active",true),"Historical N15 remains completable through actual native piece/gate controls after upgrade");capture("new-old-n15-finished");
        if(has(x->x.equals("Next")||x.startsWith("Next "),true))tap(x->x.equals("Next")||x.startsWith("Next "),true);else home();
        if(has(x->x.equals("Adventure"),true))tap(x->x.equals("Adventure"),false);
        tap(x->x.startsWith("Play"),true);dismissHelp();
        check(session().getBoolean("active",false)&&session().getInt("mode",-1)==0&&session().getInt("content_version",-1)==7&&session().getInt("budget_policy",-1)==5,"New Journey starts in the U family with policy5 after historical challenge completion");capture("new-u-journey-started");
    }
    private SharedPreferences save(){return getTargetContext().getSharedPreferences("luma_drift_save_v1",0);}
    private SharedPreferences session(){return getTargetContext().getSharedPreferences("luma_session",0);}
    private JSONObject stableSession()throws Exception{TreeMap<String,Object> map=new TreeMap<>();Map<String,?> all=session().getAll();for(String key:SESSION_KEYS)if(all.containsKey(key))map.put(key,all.get(key));return json(map);}
    private JSONObject criticalSave()throws Exception{TreeMap<String,Object> map=new TreeMap<>();for(Map.Entry<String,?> e:save().getAll().entrySet()){String k=e.getKey();if(k.equals("coins")||k.equals("unlocked")||k.startsWith("stars_")||k.startsWith("home_")||k.startsWith("owned_")||k.startsWith("equipped")||k.startsWith("avatar_style")||k.startsWith("booster_"))map.put(k,e.getValue());}return json(map);}
    private JSONObject all(SharedPreferences prefs)throws Exception{return json(new TreeMap<String,Object>(prefs.getAll()));}
    private JSONObject json(Map<String,?> map)throws Exception{JSONObject value=new JSONObject();for(Map.Entry<String,?> e:map.entrySet()){Object v=e.getValue();if(v instanceof Set){TreeSet<String> sorted=new TreeSet<>();for(Object x:(Set<?>)v)sorted.add(String.valueOf(x));v=new JSONArray(sorted);}value.put(e.getKey(),v);}return value;}
    private JSONArray geometry(){TreeSet<String> pieces=new TreeSet<>();Pattern pattern=Pattern.compile("^Piece, row ([0-9]+), column ([0-9]+), arrow ([A-Za-z]+)");for(AccessibilityNodeInfo n:nodes()){Matcher m=pattern.matcher(name(n));if(n.isVisibleToUser()&&m.find())pieces.add(m.group(1)+","+m.group(2)+","+m.group(3).toLowerCase(Locale.ROOT));n.recycle();}return new JSONArray(pieces);}
    private void launch(String uri){Intent intent=new Intent().setClassName(TARGET,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);if(uri!=null)intent.setAction(Intent.ACTION_VIEW).setData(Uri.parse(uri));startActivitySync(intent);SystemClock.sleep(1000);}
    private void english()throws Exception{tap(x->Arrays.asList(SETTINGS).contains(x),true);top();tap(x->x.equals("English"),true);back();}
    private void home()throws Exception{for(int i=0;i<6;i++){if(has(x->x.equals("Home")||x.equals("Accueil"),true)){tap(x->x.equals("Home")||x.equals("Accueil"),true);return;}if(has(x->x.equals("Leave level"),true)){tap(x->x.equals("Leave level"),true);continue;}back();}throw new AssertionError("Cannot reach actual Home control: "+labels());}
    private void dismissHelp()throws Exception{if(has(x->x.equals("Let’s drift")||x.equals("Let us drift")||x.equals("Let’s go"),true))tap(x->x.equals("Let’s drift")||x.equals("Let us drift")||x.equals("Let’s go"),false);}
    private void piece(JSONObject step)throws Exception{final String prefix="Piece, row "+(step.getInt("row")+1)+", column "+(step.getInt("col")+1)+", arrow ";tap(x->x.startsWith(prefix),false);}
    private void confirmIfPresent()throws Exception{Predicate<String> confirm=x->x.equals("Make this move · 1 action")||x.equals("Open · 1 action")||(x.startsWith("Open ")&&x.endsWith(" gate · 1 action"));if(has(confirm,true))tap(confirm,false);}
    private void waitHistory(String expected)throws Exception{long until=SystemClock.uptimeMillis()+3500;while(!session().getString("history","").equals(expected)&&SystemClock.uptimeMillis()<until)SystemClock.sleep(60);check(session().getString("history","").equals(expected),"Actual native action durably saves exact history "+expected);}
    private List<AccessibilityNodeInfo> nodes(){if(android.os.Build.VERSION.SDK_INT>=33)ui.clearCache();List<AccessibilityNodeInfo> result=new ArrayList<>();AccessibilityNodeInfo root=ui.getRootInActiveWindow();if(root==null)return result;ArrayDeque<AccessibilityNodeInfo> queue=new ArrayDeque<>();queue.add(root);while(!queue.isEmpty()&&result.size()<700){AccessibilityNodeInfo node=queue.removeFirst();result.add(node);for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo child=node.getChild(i);if(child!=null)queue.addLast(child);}}return result;}
    private static String name(AccessibilityNodeInfo n){CharSequence value=n.getContentDescription();if(value==null||value.length()==0)value=n.getText();return value==null?"":value.toString();}
    private boolean has(Predicate<String> wanted,boolean clickable){boolean found=false;for(AccessibilityNodeInfo n:nodes()){if(n.isVisibleToUser()&&n.isEnabled()&&(!clickable||n.isClickable())&&wanted.test(name(n)))found=true;n.recycle();}return found;}
    private List<String> labels(){List<String> result=new ArrayList<>();for(AccessibilityNodeInfo n:nodes()){if(!name(n).isEmpty())result.add(name(n));n.recycle();}return result;}
    private void waitFor(Predicate<String> wanted,boolean clickable,long timeout)throws Exception{long end=SystemClock.uptimeMillis()+timeout;while(SystemClock.uptimeMillis()<end){if(has(wanted,clickable))return;SystemClock.sleep(65);}throw new AssertionError("Missing native control: "+labels());}
    private void tap(Predicate<String> wanted,boolean seek)throws Exception{Rect bounds=null;String observed="";for(int attempt=0;attempt<(seek?16:12)&&bounds==null;attempt++){for(AccessibilityNodeInfo n:nodes()){if(bounds==null&&n.isVisibleToUser()&&n.isEnabled()&&n.isClickable()&&wanted.test(name(n))){bounds=new Rect();n.getBoundsInScreen(bounds);observed=name(n);}n.recycle();}if(bounds==null){if(seek)scroll(true);else SystemClock.sleep(80);}}
        if(bounds==null)throw new AssertionError("No observed native touch target: "+labels());if(bounds.isEmpty())throw new AssertionError("Empty native bounds");long down=SystemClock.uptimeMillis();touch(down,MotionEvent.ACTION_DOWN,bounds.exactCenterX(),bounds.exactCenterY());SystemClock.sleep(28);touch(down,MotionEvent.ACTION_UP,bounds.exactCenterX(),bounds.exactCenterY());actions.put(new JSONObject().put("label",observed).put("bounds",bounds.toShortString()).put("input","actual touchscreen events at SDK-observed bounds"));SystemClock.sleep(260);
    }
    private void touch(long down,int action,float x,float y){MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);boolean ok=ui.injectInputEvent(event,true);event.recycle();if(!ok)throw new AssertionError("SDK touchscreen action refused");}
    private boolean scroll(boolean forward){boolean sent=false;for(AccessibilityNodeInfo n:nodes()){if(!sent&&n.isScrollable())sent=n.performAction(forward?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);n.recycle();}SystemClock.sleep(140);return sent;}
    private void top(){List<String> previous=labels();for(int i=0;i<18;i++){if(!scroll(false))break;List<String> now=labels();if(now.equals(previous))break;previous=now;}}
    private void back(){ui.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);SystemClock.sleep(220);}
    private void capture(String id)throws Exception{String key=phase+"-"+id;Bitmap image=ui.takeScreenshot();if(image==null)throw new IOException("No SDK screenshot");try(OutputStream out=new FileOutputStream(new File(output,key+".png"))){if(!image.compress(Bitmap.CompressFormat.PNG,100,out))throw new IOException("PNG encoding refused");}finally{image.recycle();}JSONArray nodes=new JSONArray();for(AccessibilityNodeInfo n:nodes()){Rect box=new Rect();n.getBoundsInScreen(box);nodes.put(new JSONObject().put("label",name(n)).put("bounds",new JSONArray(new int[]{box.left,box.top,box.right,box.bottom})).put("clickable",n.isClickable()).put("visible",n.isVisibleToUser()));n.recycle();}write(key+".json",nodes.toString(2));captures.put(key);}
    private void check(boolean good,String message)throws Exception{checks.put(new JSONObject().put("passed",good).put("message",message));count++;if(!good)throw new AssertionError(message);Bundle status=new Bundle();status.putString("stream","PASS "+message+"\n");sendStatus(0,status);}
    private String readAsset(String path)throws Exception{try(InputStream in=getContext().getAssets().open(path)){return readStream(in);}}
    private String read(String name)throws Exception{try(InputStream in=new FileInputStream(new File(output,name))){return readStream(in);}}
    private String readStream(InputStream in)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return out.toString("UTF-8");}
    private void write(String name,String text)throws Exception{try(OutputStream out=new FileOutputStream(new File(output,name))){out.write(text.getBytes(StandardCharsets.UTF_8));}}
}
