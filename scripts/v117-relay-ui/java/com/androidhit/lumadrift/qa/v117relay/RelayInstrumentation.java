package com.androidhit.lumadrift.qa.v117relay;

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
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
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

/** Actual SDK touches and read-only preference observations; no target game classes or reflection. */
public final class RelayInstrumentation extends Instrumentation {
    private final JSONArray checks=new JSONArray(),levels=new JSONArray(),captures=new JSONArray();
    private final Map<String,Rect> cells=new HashMap<>();private final Map<String,String> cellLabels=new HashMap<>();
    private static final Pattern PIECE=Pattern.compile("Piece, row (\\d+), column (\\d+), arrow ");
    private static final String[] DIRECTIONS={"up","right","down","left"};
    private String originalLanguage;private String initialProtected;
    private Bundle args;private UiAutomation ui;private File output;private String target;private PrintWriter eventLog,checkLog;private int checkCount,touchCount,from,through,completed;private long started;private int cacheLevel;
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);args=arguments==null?new Bundle():arguments;start();}
    @Override public void onStart(){new Thread(()->{
        boolean passed=false;Bundle result=new Bundle();started=SystemClock.uptimeMillis();
        try{
            target=getTargetContext().getPackageName();check(target.equals("com.androidhit.lumadrift")||target.equals("com.androidhit.lumadrift.sandbox"),"Exact Luma target package");
            String label=args.getString("label","");check(label.matches("[A-Za-z0-9_-]{1,50}"),"Unique bounded evidence label");
            File requestedOutput=new File(getTargetContext().getExternalFilesDir(null),"v117-relay-native/"+label);if(requestedOutput.exists())throw new AssertionError("Evidence directory already exists");output=requestedOutput;if(!output.mkdirs())throw new IOException("Cannot create observer evidence");eventLog=new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(output,"events.jsonl")),StandardCharsets.UTF_8)));checkLog=new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(output,"checks.jsonl")),StandardCharsets.UTF_8)));for(int i=0;i<checks.length();i++)checkLog.println(checks.getJSONObject(i).toString());
            PackageInfo version=getTargetContext().getPackageManager().getPackageInfo(target,0);check(version.versionName.equals(args.getString("expectedVersion",""))&&version.versionCode==Integer.parseInt(args.getString("expectedCode","")),"Exact frozen candidate version/code");
            check(args.getString("runToken","").matches("[a-f0-9-]{36}"),"Unique host execution token");from=Integer.parseInt(args.getString("from","1"));through=Integer.parseInt(args.getString("through","1000"));check(from>=1&&through>=from&&through<=1000,"Bounded genuine Journey range 1..1000");
            ui=getUiAutomation();AccessibilityServiceInfo service=ui.getServiceInfo();service.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;ui.setServiceInfo(service);
            write("fixture-manifest.json",asset("fixtures/manifest.json"));originalLanguage=save().getString("language","auto");initialProtected=protectedState().toString();write("protected-before.json",initialProtected);launch();prepare();runJourney();restoreLanguage();check(protectedState().toString().equals(initialProtected),"Replay preserves coins, all earned stars, ownership, appearance, profile, summon receipts, room layouts and power stock");write("protected-after.json",protectedState().toString());capture("retained-profile-after-replay");passed=true;result.putString("summary","PASS U"+from+"–U"+through+"; "+checkCount+" native checks; "+touchCount+" actual touches");
        }catch(Throwable failure){result.putString("failure",failure.toString());try{if(output!=null){StringWriter stack=new StringWriter();failure.printStackTrace(new PrintWriter(stack));write("failure.txt",stack.toString());if(ui!=null)capture("failure");}}catch(Throwable ignored){}}
        finally{try{if(eventLog!=null){eventLog.flush();eventLog.close();}if(checkLog!=null){checkLog.flush();checkLog.close();}if(output!=null)write("report.json",new JSONObject().put("passed",passed).put("runToken",args.getString("runToken","")).put("label",args.getString("label","")).put("from",from).put("through",through).put("completed",completed).put("checkCount",checkCount).put("touchCount",touchCount).put("elapsedMs",SystemClock.uptimeMillis()-started).put("checkPreview",checks).put("checkJournal","checks.jsonl").put("levels",levels).put("captures",captures).put("package",target).put("processId",android.os.Process.myPid()).put("sdk",Build.VERSION.SDK_INT).put("scope","Replay of already-earned U boards on retained task QA profile through actual Android touchscreen exits and Next objects. Map jump and language use visible controls and IME Done. Read-only target preferences; host-frozen certificate coordinates; no target solver, renderer, reflection, progression/history/coin injection, ad, account or purchase. Replay earns zero new first-win reward. Native correctness and execution timing, not human difficulty or enjoyment.").toString(2));}catch(Throwable ignored){}finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);}
    },"luma-v117-native-relay").start();}
    private void launch(){Intent intent=new Intent().setClassName(target,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);startActivitySync(intent);SystemClock.sleep(1000);}
    private SharedPreferences save(){return getTargetContext().getSharedPreferences("luma_drift_save_v1",0);}
    private SharedPreferences session(){return getTargetContext().getSharedPreferences("luma_session",0);}
    private int coins(){return save().getInt("coins",0);}
    private String history(){return session().getString("history","");}
    private JSONObject core()throws Exception{return new JSONObject().put("history",history()).put("used",session().getInt("budget_used",-1)).put("undos",session().getInt("undos",-1)).put("level",session().getInt("level",-1)).put("contentVersion",session().getInt("content_version",-1)).put("policy",session().getInt("budget_policy",-1)).put("coins",coins()).put("stocks",stocks());}
    private JSONObject stocks()throws Exception{JSONObject values=new JSONObject();for(String key:new TreeSet<>(save().getAll().keySet()))if(key.startsWith("booster_"))values.put(key,save().getAll().get(key));return values;}
    private JSONObject protectedState()throws Exception{
        JSONObject result=new JSONObject();Map<String,?> all=save().getAll();
        for(String key:new TreeSet<>(all.keySet()))if(key.equals("coins")||key.equals("unlocked")||key.equals("character")||key.equals("skin")||key.equals("sound")||key.equals("music")||key.equals("haptics")||key.startsWith("stars_")||key.startsWith("owned_")||key.startsWith("booster_")||key.startsWith("avatar_")||key.startsWith("cosmetic_")||key.startsWith("player_")||key.startsWith("summon_")||key.startsWith("home_"))result.put(key,all.get(key));
        result.put("_session_reduce_motion",session().getBoolean("reduce_motion",false));return result;
    }
    private void openSettings()throws Exception{
        String[] settings={"Settings","Paramètres","Réglages","Einstellungen","Ajustes","Definições","Impostazioni"};
        String[] exits={"Exit","Quitter","Verlassen","Salir","Sair","Esci"};
        String[] backAdventure={"Back to adventure","Retour à l’aventure","Zurück zum Abenteuer","Volver a la aventura","Voltar à aventura","Torna all’avventura"};
        for(int i=0;i<12;i++){
            if(has(x->x.equals("English"),true))return;
            AccessibilityNodeInfo settingsNode=find(x->Arrays.asList(settings).contains(x),true);
            if(settingsNode!=null){touchNode(settingsNode,"open-settings-object");await(()->has(x->x.equals("English"),true),8000,"Visible settings language objects");return;}
            AccessibilityNodeInfo exit=find(x->Arrays.asList(exits).contains(x)||Arrays.asList(backAdventure).contains(x),true);
            if(exit!=null){touchNode(exit,"return-from-board-through-visible-object");SystemClock.sleep(350);}else back();
        }
        throw new AssertionError("Cannot reach settings through visible scene navigation: "+labels());
    }
    private void prepare()throws Exception{
        check(save().getInt("unlocked",1)>=through,"Entire requested range was already genuinely unlocked");
        for(int n=from;n<=through;n++)check(save().getInt("stars_"+n,0)==3,"U"+n+" already has its retained three-star completion; this is replay evidence");
        openSettings();control(x->x.equals("English"),"English");await(()->save().getString("language","").equals("en"),3000,"English selected through visible language object");back();
        for(int i=0;i<4&&!has(x->x.equals("All levels"),true);i++)back();
        control(x->x.equals("All levels"),"All levels");control(x->x.equals("Explore a level"),"Explore a level");
        AccessibilityNodeInfo field=null;long end=SystemClock.uptimeMillis()+6000;
        while(field==null&&SystemClock.uptimeMillis()<end){for(AccessibilityNodeInfo node:nodes()){if(field==null&&"android.widget.EditText".contentEquals(node.getClassName())&&node.isVisibleToUser())field=AccessibilityNodeInfo.obtain(node);node.recycle();}if(field==null)SystemClock.sleep(40);}
        check(field!=null,"Map jump exposes a real visible numeric EditText");Bundle text=new Bundle();text.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,String.valueOf(from));
        check(field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,text),"Actual map jump field accepts requested level text");event(new JSONObject().put("input","Accessibility ACTION_SET_TEXT").put("value",from).put("reason","visible-map-jump-field"));
        boolean submitted=Build.VERSION.SDK_INT>=30&&field.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.getId());field.recycle();
        if(!submitted){long time=SystemClock.uptimeMillis();check(ui.injectInputEvent(new KeyEvent(time,time,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0),true),"Visible keyboard Enter down");check(ui.injectInputEvent(new KeyEvent(time,SystemClock.uptimeMillis(),KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ENTER,0),true),"Visible keyboard Enter up");}
        event(new JSONObject().put("input",submitted?"Accessibility ACTION_IME_ENTER":"UiAutomation keyboard Enter").put("reason","map-jump-done"));
        final Pattern level=Pattern.compile("^Level "+from+"(?:,|$)");control(x->level.matcher(x).find(),"Requested visible map level "+from);
        control(x->x.equals("Play")||x.startsWith("Play "),"Play briefing");
        await(()->session().getInt("level",-1)==from&&session().getInt("content_version",-1)==7,7000,"Expected replay entry U board");
        capture("replay-entry");
    }
    private void restoreLanguage()throws Exception{
        control(x->x.startsWith("Back to adventure"),"Back to adventure after replay victory");
        openSettings();String[] codes={"auto","en","fr","de","es","pt","it"},names={"System","English","Français","Deutsch","Español","Português","Italiano"};int index=Arrays.asList(codes).indexOf(originalLanguage);check(index>=0,"Original saved language is supported");String label=names[index];
        control(x->x.equals(label),"Restore original language "+originalLanguage);await(()->save().getString("language","auto").equals(originalLanguage),3000,"Original language durably restored through visible UI");back();
    }
    private void runJourney()throws Exception{
        for(int n=from;n<=through;n++){
            long levelStarted=SystemClock.uptimeMillis();JSONObject fixture=new JSONObject(asset("fixtures/u"+n+".json"));JSONArray steps=fixture.getJSONArray("steps");
            await(()->countPieces()==fixture.getInt("pieces"),5000,"Native initial piece count");
            check(session().getInt("level",-1)==n&&session().getInt("content_version",-1)==7&&session().getInt("budget_policy",-1)==5,"Actual U"+n+" uses content 7 / relay policy 5");
            check(history().isEmpty()&&session().getInt("budget_used",-1)==0,"U"+n+" starts with a genuine empty action history");
            observeBoard(n,fixture.getInt("pieces"));check(gates()==fixture.getInt("initialGates"),"U"+n+" actual initial gate states match frozen board");
            boolean capture=n<=30||n==from||n==through||n%50==0||n==99||n==101||n==999;
            if(capture)capture("u"+n+"-initial");int initialCoins=coins(),initialStars=save().getInt("stars_"+n,0);JSONObject initialStocks=stocks();int probeExits=0;
            if((n==5||n==9||n==13||n==31||n==50||n==100)&&!fixture.isNull("blockedFirst")){JSONObject blocked=fixture.getJSONObject("blockedFirst");String before=core().toString();for(int i=0;i<5;i++){touchCell(blocked.getInt("row"),blocked.getInt("col"),"blocked-free-probe");SystemClock.sleep(25);}check(core().toString().equals(before),"Five actual blocked taps on U"+n+" preserve history, action count, Undo, coins and power stock");if(capture)capture("u"+n+"-blocked-free");}
            if(n==13){String before=core().toString();AccessibilityNodeInfo closed=find(x->x.startsWith("Gate closed"),false);check(closed!=null,"U13 exposes its real closed gate");touchNode(closed,"closed-gate-observation");SystemClock.sleep(45);check(core().toString().equals(before),"Touching U13 closed gate spends nothing and does not open it");
                action(steps.getJSONObject(0));action(steps.getJSONObject(1));check(session().getInt("budget_used",-1)==2,"Two probe exits were really played");int priorUndos=session().getInt("undos",0);undo();undo();check(history().isEmpty()&&session().getInt("budget_used",-1)==0&&session().getInt("undos",-1)==priorUndos+2,"U13 permits two real refunded Undo operations in one attempt");check(gates()==fixture.getInt("initialGates"),"U13 two Undo operations restore the initial gates");check(coins()==initialCoins&&stocks().toString().equals(initialStocks.toString()),"Repeated U13 Undo is free in wallet and stock");observeBoard(n,fixture.getInt("pieces"));probeExits=2;capture("u13-two-undos-free");}
            for(int i=0;i<steps.length();i++)action(steps.getJSONObject(i));
            final int level=n;await(()->save().getInt("stars_"+level,0)>0&&has(x->x.startsWith("Next"),true),5000,"Native victory and Next");
            check(history().split(",").length==steps.length()&&session().getInt("budget_used",-1)==steps.length(),"U"+n+" commits exactly one exit per piece, no manual gate cost");
            check(initialStars==3&&save().getInt("stars_"+n,0)==3&&coins()==initialCoins,"U"+n+" replay keeps retained three stars and awards no duplicate first-win coins");
            check(stocks().toString().equals(initialStocks.toString()),"U"+n+" certificate route consumes no power stock");if(capture)capture("u"+n+"-victory");completed++;
            levels.put(new JSONObject().put("number",n).put("certificateExits",steps.length()).put("extraProbeExits",probeExits).put("initialStars",initialStars).put("stars",save().getInt("stars_"+n,0)).put("coinDelta",coins()-initialCoins).put("history",history()).put("undos",session().getInt("undos",0)).put("elapsedMs",SystemClock.uptimeMillis()-levelStarted));
            write("progress.json",new JSONObject().put("completed",completed).put("lastCompleted",n).put("checks",checkCount).put("touches",touchCount).put("elapsedMs",SystemClock.uptimeMillis()-started).put("levels",levels).toString(2));eventLog.flush();checkLog.flush();Bundle progress=new Bundle();progress.putString("stream","PASS real U"+n+"; completed="+completed+"; elapsed_ms="+(SystemClock.uptimeMillis()-started)+"\n");sendStatus(0,progress);
            if(n<through){control(x->x.startsWith("Next"),"Next");await(()->session().getInt("level",-1)==level+1&&history().isEmpty(),5000,"Next starts the next genuine U board");}
        }
    }
    private void action(JSONObject step)throws Exception{
        String before=history();int used=session().getInt("budget_used",-1);int id=step.getInt("action");String expected=before.isEmpty()?Integer.toString(id):before+","+id;
        touchCell(step.getInt("row"),step.getInt("col"),"certificate-exit");await(()->history().equals(expected),3000,"Persisted exactly-once exit");
        check(session().getInt("budget_used",-1)==used+1,"Actual U exit "+id+" changes its action ledger exactly once");
        // The persisted history after this single physical tap already proves direct commitment:
        // an unconfirmed action modal could not satisfy that condition. Do not query the same UI again.
        if(step.getInt("switch")!=0)awaitGates(step.getInt("gatesAfter"),"Actual relay exit "+id+" changes exactly its linked gates");
    }
    private void undo()throws Exception{String before=history();int comma=before.lastIndexOf(',');String expected=comma<0?"":before.substring(0,comma);int used=session().getInt("budget_used",-1);control(x->x.contains("Undo"),"Undo");await(()->history().equals(expected),3000,"Real Undo restores previous history");check(session().getInt("budget_used",-1)==used-1,"Real Undo refunds exactly its exit");}
    private int countPieces(){int count=0;for(AccessibilityNodeInfo node:nodes()){if(PIECE.matcher(name(node)).find()&&node.isVisibleToUser())count++;node.recycle();}return count;}
    private void observeBoard(int level,int expected)throws Exception{
        cells.clear();cellLabels.clear();cacheLevel=level;JSONArray observed=new JSONArray();
        for(AccessibilityNodeInfo node:nodes()){String label=name(node);Matcher match=PIECE.matcher(label);if(match.find()){Rect bounds=new Rect();node.getBoundsInScreen(bounds);if(!node.isVisibleToUser()||!node.isEnabled()||bounds.isEmpty())throw new AssertionError("Native piece inaccessible: "+label);String key=(Integer.parseInt(match.group(1))-1)+","+(Integer.parseInt(match.group(2))-1);cells.put(key,new Rect(bounds));cellLabels.put(key,label);observed.put(new JSONObject().put("key",key).put("label",label).put("bounds",new JSONArray(new int[]{bounds.left,bounds.top,bounds.right,bounds.bottom})));}node.recycle();}
        check(cells.size()==expected,"Observed all "+expected+" actual native piece targets on U"+level);event(new JSONObject().put("observation","initial-native-piece-bounds").put("level",level).put("targets",observed));
    }
    private void touchCell(int row,int col,String reason)throws Exception{check(session().getInt("level",-1)==cacheLevel,"Observed board geometry belongs to current level");String key=row+","+col;Rect bounds=cells.get(key);if(bounds==null)throw new AssertionError("No observed native target for "+key);touch(bounds,cellLabels.get(key),reason);}
    private int gates(){int mask=0,seen=0;for(AccessibilityNodeInfo node:nodes()){String label=name(node);if(label.startsWith("Gate open")||label.startsWith("Gate closed")){for(int d=0;d<4;d++)if(Pattern.compile("\\b"+DIRECTIONS[d]+"\\b").matcher(label.toLowerCase(Locale.ROOT)).find()){seen|=1<<d;if(label.startsWith("Gate open"))mask|=1<<d;break;}}node.recycle();}if(seen!=15)return -1;return mask;}
    private void awaitGates(int expected,String description)throws Exception{long end=SystemClock.uptimeMillis()+2500;int observed=gates();while(observed!=expected&&SystemClock.uptimeMillis()<end){SystemClock.sleep(12);observed=gates();}check(observed==expected,description);}
    private static String name(AccessibilityNodeInfo node){CharSequence text=node.getContentDescription();if(text==null||text.length()==0)text=node.getText();return text==null?"":text.toString();}
    private List<AccessibilityNodeInfo> nodes(){if(Build.VERSION.SDK_INT>=33)ui.clearCache();List<AccessibilityNodeInfo> result=new ArrayList<>();AccessibilityNodeInfo root=ui.getRootInActiveWindow();if(root==null)return result;ArrayDeque<AccessibilityNodeInfo> pending=new ArrayDeque<>();pending.add(root);while(!pending.isEmpty()&&result.size()<700){AccessibilityNodeInfo node=pending.removeFirst();result.add(node);for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo child=node.getChild(i);if(child!=null)pending.addLast(child);}}return result;}
    private AccessibilityNodeInfo find(Predicate<String> predicate,boolean clickable){AccessibilityNodeInfo found=null;for(AccessibilityNodeInfo node:nodes()){Rect bounds=new Rect();node.getBoundsInScreen(bounds);if(found==null&&node.isVisibleToUser()&&node.isEnabled()&&(!clickable||node.isClickable())&&!bounds.isEmpty()&&predicate.test(name(node)))found=AccessibilityNodeInfo.obtain(node);node.recycle();}return found;}
    private boolean has(Predicate<String> predicate,boolean clickable){AccessibilityNodeInfo node=find(predicate,clickable);if(node==null)return false;node.recycle();return true;}
    private String labels(){List<String> labels=new ArrayList<>();for(AccessibilityNodeInfo node:nodes()){if(!name(node).isEmpty())labels.add(name(node));node.recycle();}return labels.toString();}
    private void control(Predicate<String> predicate,String description)throws Exception{long end=SystemClock.uptimeMillis()+4500;AccessibilityNodeInfo node;while((node=find(predicate,true))==null&&SystemClock.uptimeMillis()<end)SystemClock.sleep(35);if(node==null)throw new AssertionError("Missing native "+description+": "+labels());touchNode(node,description);SystemClock.sleep(300);}
    private void touchNode(AccessibilityNodeInfo node,String reason)throws Exception{if(node==null)throw new AssertionError("Missing node "+reason);Rect bounds=new Rect();node.getBoundsInScreen(bounds);String label=name(node);node.recycle();touch(bounds,label,reason);}
    private void touch(Rect bounds,String label,String reason)throws Exception{if(bounds.isEmpty())throw new AssertionError("Empty touch bounds");long down=SystemClock.uptimeMillis();inject(down,MotionEvent.ACTION_DOWN,bounds.exactCenterX(),bounds.exactCenterY());SystemClock.sleep(12);inject(down,MotionEvent.ACTION_UP,bounds.exactCenterX(),bounds.exactCenterY());touchCount++;event(new JSONObject().put("input","UiAutomation touchscreen").put("touch",touchCount).put("reason",reason).put("label",label).put("bounds",new JSONArray(new int[]{bounds.left,bounds.top,bounds.right,bounds.bottom})).put("level",session().getInt("level",-1)).put("uptimeMs",SystemClock.uptimeMillis()));}
    private void inject(long down,int action,float x,float y){MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);try{if(!ui.injectInputEvent(event,true))throw new AssertionError("SDK touchscreen event refused");}finally{event.recycle();}}
    private void back(){ui.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);SystemClock.sleep(350);}
    private interface Condition {boolean get()throws Exception;}
    private void await(Condition condition,long milliseconds,String description)throws Exception{long end=SystemClock.uptimeMillis()+milliseconds;boolean observed=condition.get();while(!observed&&SystemClock.uptimeMillis()<end){SystemClock.sleep(12);observed=condition.get();}check(observed,description);}
    private void check(boolean condition,String message)throws Exception{checkCount++;JSONObject value=new JSONObject().put("index",checkCount).put("passed",condition).put("message",message);if(checkLog!=null)checkLog.println(value.toString());if(checks.length()<100)checks.put(value);if(!condition)throw new AssertionError(message);}
    private void event(JSONObject value){if(eventLog!=null)eventLog.println(value.toString());}
    private void capture(String title)throws Exception{Bitmap bitmap=ui.takeScreenshot();if(bitmap==null)throw new IOException("Native screenshot unavailable");try(OutputStream file=new FileOutputStream(new File(output,title+".png"))){if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,file))throw new IOException("Native PNG encoding failed");}finally{bitmap.recycle();}JSONArray nodes=new JSONArray();for(AccessibilityNodeInfo node:nodes()){Rect bounds=new Rect();node.getBoundsInScreen(bounds);nodes.put(new JSONObject().put("label",name(node)).put("bounds",new JSONArray(new int[]{bounds.left,bounds.top,bounds.right,bounds.bottom})).put("clickable",node.isClickable()).put("visible",node.isVisibleToUser()));node.recycle();}write(title+".json",nodes.toString(2));captures.put(title);}
    private String asset(String file)throws Exception{try(InputStream input=getContext().getAssets().open(file);ByteArrayOutputStream bytes=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int n;while((n=input.read(buffer))!=-1)bytes.write(buffer,0,n);return bytes.toString("UTF-8");}}
    private void write(String name,String value)throws Exception{try(OutputStream file=new FileOutputStream(new File(output,name))){file.write(value.getBytes(StandardCharsets.UTF_8));}}
}
