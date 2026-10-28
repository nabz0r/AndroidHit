package com.androidhit.lumadrift.qa.v115relay;

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
    private Bundle args;private UiAutomation ui;private File output;private String target;private PrintWriter eventLog;private int checkCount,touchCount,from,through,completed;private long started;private int cacheLevel;
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);args=arguments==null?new Bundle():arguments;start();}
    @Override public void onStart(){new Thread(()->{
        boolean passed=false;Bundle result=new Bundle();started=SystemClock.uptimeMillis();
        try{
            target=getTargetContext().getPackageName();check(target.equals("com.androidhit.lumadrift")||target.equals("com.androidhit.lumadrift.sandbox"),"Exact Luma target package");
            String label=args.getString("label","");check(label.matches("[A-Za-z0-9_-]{1,50}"),"Unique bounded evidence label");
            output=new File(getTargetContext().getExternalFilesDir(null),"v115-relay-native/"+label);if(output.exists())throw new AssertionError("Evidence directory already exists");if(!output.mkdirs())throw new IOException("Cannot create observer evidence");eventLog=new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(output,"events.jsonl")),StandardCharsets.UTF_8)));
            PackageInfo version=getTargetContext().getPackageManager().getPackageInfo(target,0);check(version.versionName.equals(args.getString("expectedVersion",""))&&version.versionCode==Integer.parseInt(args.getString("expectedCode","17")),"Exact frozen candidate version/code");
            from=Integer.parseInt(args.getString("from","1"));through=Integer.parseInt(args.getString("through","100"));check(from>=1&&through>=from&&through<=1000,"Bounded genuine Journey range 1..1000");
            ui=getUiAutomation();AccessibilityServiceInfo service=ui.getServiceInfo();service.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;ui.setServiceInfo(service);
            write("fixture-manifest.json",asset("fixtures/manifest.json"));launch();prepare();runJourney();passed=true;result.putString("summary","PASS U"+from+"–U"+through+"; "+checkCount+" native checks; "+touchCount+" actual touches");
        }catch(Throwable failure){result.putString("failure",failure.toString());try{if(output!=null){capture("failure");StringWriter stack=new StringWriter();failure.printStackTrace(new PrintWriter(stack));write("failure.txt",stack.toString());}}catch(Throwable ignored){}}
        finally{try{if(eventLog!=null){eventLog.flush();eventLog.close();}if(output!=null)write("report.json",new JSONObject().put("passed",passed).put("from",from).put("through",through).put("completed",completed).put("checkCount",checkCount).put("touchCount",touchCount).put("elapsedMs",SystemClock.uptimeMillis()-started).put("checks",checks).put("levels",levels).put("captures",captures).put("package",target).put("processId",android.os.Process.myPid()).put("sdk",Build.VERSION.SDK_INT).put("scope","Actual Android touchscreen exits and Next controls; observed accessible board bounds; target preferences read only. Host-frozen certificates guide touches. No target solver, renderer, reflection, profile seeding, history injection, ad, account or purchase. Native correctness and execution timing, not human difficulty or enjoyment.").toString(2));}catch(Throwable ignored){}finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);}
    },"luma-v115-native-relay").start();}
    private void launch(){Intent intent=new Intent().setClassName(target,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);startActivitySync(intent);SystemClock.sleep(1000);}
    private SharedPreferences save(){return getTargetContext().getSharedPreferences("luma_drift_save_v1",0);}
    private SharedPreferences session(){return getTargetContext().getSharedPreferences("luma_session",0);}
    private int coins(){return save().getInt("coins",0);}
    private String history(){return session().getString("history","");}
    private JSONObject core()throws Exception{return new JSONObject().put("history",history()).put("used",session().getInt("budget_used",-1)).put("undos",session().getInt("undos",-1)).put("level",session().getInt("level",-1)).put("contentVersion",session().getInt("content_version",-1)).put("policy",session().getInt("budget_policy",-1)).put("coins",coins()).put("stocks",stocks());}
    private JSONObject stocks()throws Exception{JSONObject values=new JSONObject();for(String key:new TreeSet<>(save().getAll().keySet()))if(key.startsWith("booster_"))values.put(key,save().getAll().get(key));return values;}
    private void prepare()throws Exception{
        String[] skip={"I know how to play","Je sais déjà jouer","Ich kenne das Spiel","Ya sé jugar","Já sei jogar","So già giocare","Play at my pace","Jouer à mon rythme","Later","Plus tard"};
        for(int i=0;i<4;i++){AccessibilityNodeInfo node=find(x->Arrays.asList(skip).contains(x),true);if(node==null)break;touchNode(node,"intro-skip");SystemClock.sleep(150);}
        String[] settings={"Settings","Paramètres","Réglages","Einstellungen","Ajustes","Definições","Impostazioni"};
        if(!has(x->Arrays.asList(settings).contains(x),true)){for(int i=0;i<4&&!has(x->Arrays.asList(settings).contains(x),true);i++)back();}
        control(x->Arrays.asList(settings).contains(x),"Settings");control(x->x.equals("English"),"English");back();
        if(has(x->x.equals("Adventure"),true))control(x->x.equals("Adventure"),"Adventure");
        check(save().getInt("unlocked",1)>=from,"Entry range is already genuinely earned; no injected unlock");
        check(from!=1||save().getInt("stars_1",0)==0,"U1 fresh qualification starts before its first earned win");
        control(x->x.equals("Play")||x.startsWith("Play ")||x.equals("Resume")||x.startsWith("Resume "),"Play/Resume");
        await(()->session().getInt("level",-1)==from&&session().getInt("content_version",-1)==7,5000,"Expected U entry board");
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
            check(save().getInt("stars_"+n,0)==3&&coins()>=initialCoins,"U"+n+" victory is genuinely saved with its real earned reward");
            check(stocks().toString().equals(initialStocks.toString()),"U"+n+" certificate route consumes no power stock");if(capture)capture("u"+n+"-victory");completed++;
            levels.put(new JSONObject().put("number",n).put("certificateExits",steps.length()).put("extraProbeExits",probeExits).put("initialStars",initialStars).put("stars",save().getInt("stars_"+n,0)).put("coinDelta",coins()-initialCoins).put("history",history()).put("undos",session().getInt("undos",0)).put("elapsedMs",SystemClock.uptimeMillis()-levelStarted));
            write("progress.json",new JSONObject().put("completed",completed).put("lastCompleted",n).put("checks",checkCount).put("touches",touchCount).put("elapsedMs",SystemClock.uptimeMillis()-started).put("levels",levels).toString(2));eventLog.flush();Bundle progress=new Bundle();progress.putString("stream","PASS real U"+n+"; completed="+completed+"; elapsed_ms="+(SystemClock.uptimeMillis()-started)+"\n");sendStatus(0,progress);
            if(n<1000){control(x->x.startsWith("Next"),"Next");await(()->session().getInt("level",-1)==level+1&&history().isEmpty(),5000,"Next starts the next genuine U board");}
        }
    }
    private void action(JSONObject step)throws Exception{
        String before=history();int used=session().getInt("budget_used",-1);int id=step.getInt("action");String expected=before.isEmpty()?Integer.toString(id):before+","+id;
        touchCell(step.getInt("row"),step.getInt("col"),"certificate-exit");await(()->history().equals(expected),3000,"Persisted exactly-once exit");
        check(session().getInt("budget_used",-1)==used+1,"Actual U exit "+id+" changes its action ledger exactly once");
        check(!has(x->x.equals("Make this move · 1 action")||x.equals("Keep planning"),true),"U exit is direct and creates no old action-confirmation modal");
        if(step.getInt("switch")!=0){await(()->gates()==step.getInt("gatesAfter"),2500,"Visible relay gate effect");check(gates()==step.getInt("gatesAfter"),"Actual relay exit "+id+" changes exactly its linked gates");}
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
    private void await(Condition condition,long milliseconds,String description)throws Exception{long end=SystemClock.uptimeMillis()+milliseconds;while(!condition.get()&&SystemClock.uptimeMillis()<end)SystemClock.sleep(12);check(condition.get(),description);}
    private void check(boolean condition,String message)throws Exception{checkCount++;checks.put(new JSONObject().put("passed",condition).put("message",message));if(!condition)throw new AssertionError(message);}
    private void event(JSONObject value){if(eventLog!=null)eventLog.println(value.toString());}
    private void capture(String title)throws Exception{Bitmap bitmap=ui.takeScreenshot();if(bitmap==null)throw new IOException("Native screenshot unavailable");try(OutputStream file=new FileOutputStream(new File(output,title+".png"))){if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,file))throw new IOException("Native PNG encoding failed");}finally{bitmap.recycle();}JSONArray nodes=new JSONArray();for(AccessibilityNodeInfo node:nodes()){Rect bounds=new Rect();node.getBoundsInScreen(bounds);nodes.put(new JSONObject().put("label",name(node)).put("bounds",new JSONArray(new int[]{bounds.left,bounds.top,bounds.right,bounds.bottom})).put("clickable",node.isClickable()).put("visible",node.isVisibleToUser()));node.recycle();}write(title+".json",nodes.toString(2));captures.put(title);}
    private String asset(String file)throws Exception{try(InputStream input=getContext().getAssets().open(file);ByteArrayOutputStream bytes=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int n;while((n=input.read(buffer))!=-1)bytes.write(buffer,0,n);return bytes.toString("UTF-8");}}
    private void write(String name,String value)throws Exception{try(OutputStream file=new FileOutputStream(new File(output,name))){file.write(value.getBytes(StandardCharsets.UTF_8));}}
}
