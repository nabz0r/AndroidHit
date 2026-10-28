package com.androidhit.lumadrift.qa.v114;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.InputDevice;
import android.view.MotionEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

/** Actual SDK accessibility actions; application preferences are read only, never seeded or edited. */
public final class CandidateUiInstrumentation extends Instrumentation {
    private String TARGET="com.androidhit.lumadrift.sandbox";
    private static final String[] SETTINGS={"Settings","Paramètres","Réglages","Einstellungen","Ajustes","Definições","Impostazioni"};
    private static final String[] SKIP={"Play at my pace","Jouer à mon rythme","In meinem Tempo spielen","Jugar a mi ritmo","Jogar no meu ritmo","Gioca al tuo ritmo"};
    private static final String[] FIRST_SKIP={"I know how to play","Je sais déjà jouer","Ich kenne das Spiel","Ya sé jugar","Já sei jogar","So già giocare"};
    private static final String[] LATER={"Later","Plus tard","Später","Más tarde","Mais tarde","Più tardi"};
    private Bundle args;private UiAutomation ui;private File output;private int count;private Activity activity;private boolean gestures;private boolean allowV18;private boolean previewCancellationChecked;private boolean guardianListRotationChecked;private String originalSize;private RotationFrame initialViewport;
    private int visualSoftFailures;private long lastUiTouchUpAt;private boolean bulk,bulkCache,journeyCacheActive,journeyDiagnostic;private int cachedLevel,cachedRotation;private Rect cachedPreviewConfirm;private final Map<String,Rect> cachedBoardBounds=new HashMap<>();private final Map<String,String> cachedBoardLabels=new HashMap<>();
    private final JSONArray checks=new JSONArray(),captures=new JSONArray(),actions=new JSONArray();
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);args=arguments==null?new Bundle():arguments;start();}
    @Override public void onStart(){new Thread(()->{
        Bundle result=new Bundle();boolean passed=false;
        try{
            TARGET=getTargetContext().getPackageName();bulk="true".equals(args.getString("bulk","false"));bulkCache=bulk&&"true".equals(args.getString("bulkCache","false"));if(!TARGET.equals("com.androidhit.lumadrift.sandbox")&&!TARGET.equals("com.androidhit.lumadrift"))throw new AssertionError("Exact Lab/production target only");
            ui=getUiAutomation();AccessibilityServiceInfo info=ui.getServiceInfo();info.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;ui.setServiceInfo(info);
            output=new File(getTargetContext().getExternalFilesDir(null),"v114-sdk-ui");if(!output.exists()&&!output.mkdirs())throw new IOException("Cannot create output");
            PackageInfo version=getTargetContext().getPackageManager().getPackageInfo(TARGET,0);allowV18=false;int expectedCode=Integer.parseInt(args.getString("expectedCode","15"));String expectedVersion=args.getString("expectedVersion","1.14.0-sandbox-rc1");check(version.versionCode==expectedCode&&expectedVersion.equals(version.versionName),"Installed exact target "+expectedVersion+"/code"+expectedCode);
            if("true".equals(args.getString("compact","false"))){originalSize=shellDiagnostic("wm size");check(shellDiagnostic("wm size 1080x1920").trim().isEmpty(),"SDK configures owned QA display1080x1920 for compact portrait");ui.setRotation(UiAutomation.ROTATION_FREEZE_0);SystemClock.sleep(600);}
            String requested=args.getString("cases","tutorial,gestures,map,journey,recovery");Set<String> cases=new HashSet<>(Arrays.asList(requested.split(",")));Set<String> pending=new HashSet<>(Arrays.asList("event-retry","guardian-storage-fault"));Set<String> allowed=new HashSet<>(Arrays.asList("tutorial","gestures","map","challenge","journey","recovery","powers","landscape","guardians","hub","avatar","story","menus","rewards","styles","languages","avatar-visual","shop-focus","shop-locales","power-return","house-smoke","house","house-locales","house-layout","firstlight","puzzle-locales","level15-audit","learning-core","legacy-upgrade-prepare","legacy-upgrade-verify","legacy-upgrade-cold"));for(String item:cases){if(pending.contains(item))throw new AssertionError("UI case awaits stable1.10 contract: "+item);if(!allowed.contains(item))throw new AssertionError("Unknown or empty UI case: "+item);}if(TARGET.equals("com.androidhit.lumadrift"))check(new HashSet<>(Arrays.asList("journey","puzzle-locales","house-smoke","house-locales","house-layout")).containsAll(cases),"Production qualification is restricted to bounded Journey and house browsing without provider controls");gestures=cases.contains("gestures");
            launch();boolean firstArrival=has(x->Arrays.asList(FIRST_SKIP).contains(x),true);if("true".equals(args.getString("expectFreshHome","false")))check(firstArrival&&!save().getBoolean("first_light_seen_v113",false),"Genuinely fresh guest opens the three-piece first-light puzzle");if(firstArrival){if(cases.contains("firstlight")){firstLight();if("pause".equals(args.getString("firstLightPhase","complete"))){passed=true;result.putString("summary","PASS first-light checkpoint before real cold relaunch");return;}}else click(x->Arrays.asList(FIRST_SKIP).contains(x),true);}else if(cases.contains("firstlight"))throw new AssertionError("First-light qualification requires a genuinely fresh profile; no reset or seed is permitted");boolean offered=has(x->(Arrays.asList(SKIP).contains(x)||Arrays.asList(LATER).contains(x)),true);if("true".equals(args.getString("expectFreshTutorial","false")))check(offered&&save().getBoolean("tutorial_v18_fresh_profile",false)&&!save().getBoolean("tutorial_v18_reward_claimed",false),"Fresh guest installation automatically offers optional real practice");check(true,offered?"First arrival displays the optional learning invitation":"Existing profile preserved; automatic first-arrival offer is not claimed on this run");
            if(offered)click(x->(Arrays.asList(SKIP).contains(x)||Arrays.asList(LATER).contains(x)),false);
            english();initialViewport=observeRotationFrame();capture("home-en");
            if(bulk)ensureReducedMotion();
            if(cases.contains("avatar-visual"))ensureReducedMotion();
            if("cold".equals(args.getString("visualPhase","prepare"))){check(cases.equals(Collections.singleton("avatar-visual"))||cases.equals(Collections.singleton("house"))||cases.equals(Collections.singleton("learning-core")),"Cold phase contains explicit persistent verification and recovery only");if(cases.contains("learning-core"))learningCoreCold();else if(cases.contains("house"))houseColdVerify();else visualColdVerify();passed=visualSoftFailures==0;result.putString("summary",(passed?"PASS ":"FAIL ")+count+" cold checks");return;}
            if(cases.contains("tutorial"))tutorial();if(cases.contains("hub"))hubV110();if(cases.contains("menus"))menus();if(cases.contains("avatar"))avatarV110();if(cases.contains("story"))storyV110();if(cases.contains("map"))map();
            if(cases.contains("challenge"))challenge();if(gestures)campaignGesture();if(cases.contains("journey"))journey();if(cases.contains("level15-audit"))level15Audit();if(cases.contains("legacy-upgrade-prepare"))legacyUpgradePrepare();if(cases.contains("legacy-upgrade-verify"))legacyUpgradeVerify();if(cases.contains("legacy-upgrade-cold")){legacyUpgradeCold();learningCorePrepare();}if(cases.contains("learning-core"))learningCorePrepare();
            if(cases.contains("recovery"))recovery();if(cases.contains("powers"))powers();
            if(cases.contains("power-return"))powerReturn();
            if(cases.contains("puzzle-locales"))puzzleLocales();
            if(cases.contains("rewards"))rewards();if(cases.contains("styles"))styles();if(cases.contains("guardians"))guardians();if(cases.contains("landscape"))landscape();if(cases.contains("languages"))languages();
            if(cases.contains("house-smoke"))houseSmoke();
            if(cases.contains("house"))house();
            if(cases.contains("house-locales"))houseLocales();
            if(cases.contains("house-layout"))houseLayout();
            if(cases.contains("shop-focus"))shopFocus();
            if(cases.contains("shop-locales"))shopLocales();
            if(cases.contains("avatar-visual"))avatarVisual();
            passed=visualSoftFailures==0;result.putString("summary",(passed?"PASS ":"FAIL ")+count+" checks; "+captures.length()+" captured native frames/labelled analysis crops");
        }catch(Throwable failure){result.putString("failure",failure.toString());try{if(output!=null){capture("failure");write("failure.txt",stack(failure));}}catch(Throwable ignored){}}
        finally{
            try{if(originalSize!=null){String override=null;for(String line:originalSize.split("\\n"))if(line.startsWith("Override size:"))override=line.substring(14).trim();shellDiagnostic(override==null?"wm size reset":"wm size "+override);}}catch(Throwable restore){result.putString("display_restore_error",restore.toString());passed=false;}
            try{if(output!=null){write("actions.json",actions.toString(2));write("report.json",new JSONObject().put("passed",passed).put("checks",checks).put("captures",captures).put("checkCount",count).put("package",TARGET).put("sdk",Build.VERSION.SDK_INT).put("abi",Build.SUPPORTED_ABIS[0]).put("processId",android.os.Process.myPid()).put("visualPhase",args.getString("visualPhase","prepare")).put("firstLightPhase",args.getString("firstLightPhase","none")).put("visualSoftFailures",visualSoftFailures).put("visualCheckpointReady",new File(output,"visual-checkpoint.json").isFile()).put("houseCheckpointReady",new File(output,"house-checkpoint.json").isFile()).put("firstLightCheckpointReady",new File(output,"first-light-checkpoint.json").isFile()).put("legacyCheckpointReady",new File(output,"legacy-upgrade-checkpoint.json").isFile()).put("learningCheckpointReady",new File(output,"learning-core-checkpoint.json").isFile()).put("scope","Actual SDK AccessibilityNodeProvider actions and native touchscreen SDK drags from observed bounds; read-only preference assertions; no profile injection, account, ad or payment").toString(2));}}catch(Throwable ignored){}
            finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
        }
    },"luma-v114-sdk-ui-tests").start();}
    private String shellDiagnostic(String command)throws IOException{try(InputStream input=new android.os.ParcelFileDescriptor.AutoCloseInputStream(ui.executeShellCommand(command));ByteArrayOutputStream bytes=new ByteArrayOutputStream()){byte[] buffer=new byte[4096];int n;while((n=input.read(buffer))!=-1)bytes.write(buffer,0,n);return bytes.toString("UTF-8");}}
    private void replayFromSettings()throws Exception{
        home();click(x->Arrays.asList(SETTINGS).contains(x),false);clickExact("Replay the interactive tutorial",true);
        check(lesson()==0&&phase()==0,"Visible settings replay starts lesson1 without editing save state");
    }
    private void gateDriftRegression()throws Exception{
        if(!allowV18)throw new AssertionError("Baseline case requires explicit allowV18 and frozen code9");
        openTutorial();check(lesson()<=3,"Stored practice is at or before the target lesson; no profile reset is used");int iterations=0;
        while(lesson()<3){if(iterations++>25)throw new AssertionError("Cannot reach manual gate lesson");int l=lesson(),p=phase();
            if(l==0&&p==0)piece(1,1);else if(l==0)nextLesson();
            else if(l==1&&(p==0||p==2))piece(1,0);else if(l==1&&p==1)piece(1,2);else if(l==1)nextLesson();
            else if(l==2&&(p==0||p==2))piece(1,0);else if(l==2&&p==1)piece(0,0);else nextLesson();
        }
        check(lesson()==3&&phase()==0,"Lesson4 reached solely through native teaching controls");top();capture("lesson4-before-coach-drift");
        String before=tutorialState();JSONObject gesture=dragGateCoach();SystemClock.sleep(250);gesture.put("before",before).put("after",tutorialState()).put("phase",phase()).put("expectedPhase",1);write("gate-drift.json",gesture.toString(2));capture("lesson4-after-coach-drift");
        check(phase()==1,"REGRESSION: gate coach15 logical point drift opens exactly one preview");
    }
    private JSONObject dragGateCoach()throws Exception{
        AccessibilityNodeInfo node=waitFor(x->x.contains("Gate closed")&&x.toLowerCase(Locale.ROOT).contains("up"),true,3000);Rect bounds=new Rect();node.getBoundsInScreen(bounds);String observed=name(node);node.recycle();
        float logicalScale=bounds.width()/50f,x=bounds.exactCenterX(),endY=bounds.exactCenterY(),startY=endY+15f*logicalScale;long down=SystemClock.uptimeMillis();
        check(bounds.width()>0&&bounds.height()>0,"Gate gesture uses observed visible accessibility bounds");
        injectTouch(down,MotionEvent.ACTION_DOWN,x,startY);for(int i=1;i<=6;i++){SystemClock.sleep(30);injectTouch(down,MotionEvent.ACTION_MOVE,x,startY+(endY-startY)*i/6);}SystemClock.sleep(25);injectTouch(down,MotionEvent.ACTION_UP,x,endY);SystemClock.sleep(150);
        JSONObject event=new JSONObject().put("gesture","gate-coach-drift").put("observedLabel",observed).put("observedBounds",bounds.toShortString()).put("logicalTravel",15).put("startX",x).put("startY",startY).put("endY",endY).put("derivedLogicalScale",logicalScale).put("durationMs",SystemClock.uptimeMillis()-down).put("input","UiAutomation.injectInputEvent; touchscreen; six MOVE events");actions.put(event);return event;
    }
    private void launch()throws Exception{Intent intent=new Intent().setClassName(TARGET,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);activity=startActivitySync(intent);SystemClock.sleep(650);waitFor(x->x.startsWith("Luma Drift · "),false,6000);}
    private SharedPreferences save(){return getTargetContext().getSharedPreferences("luma_drift_save_v1",0);}
    private SharedPreferences session(){return getTargetContext().getSharedPreferences("luma_session",0);}
    private int coins(){return save().getInt("coins",0);}
    private int stock(String power){return save().getInt("booster_"+power,0);}
    private String tutorialState(){return save().getString("tutorial_v18_state","");}
    private int lesson(){String[] p=tutorialState().split("\\|",-1);return p.length==5?Integer.parseInt(p[1]):-1;}
    private int phase(){String[] p=tutorialState().split("\\|",-1);return p.length==5?Integer.parseInt(p[2]):-1;}
    private void english()throws Exception{
        if(!has(x->Arrays.asList(SETTINGS).contains(x),true))home();click(x->Arrays.asList(SETTINGS).contains(x),false);clickExact("English",true);back();waitFor(x->x.equals("Home"),true,4000);
    }
    private List<AccessibilityNodeInfo> nodes(){if(Build.VERSION.SDK_INT>=33)ui.clearCache();List<AccessibilityNodeInfo> out=new ArrayList<>();AccessibilityNodeInfo root=ui.getRootInActiveWindow();if(root==null)return out;ArrayDeque<AccessibilityNodeInfo> queue=new ArrayDeque<>();queue.add(root);while(!queue.isEmpty()&&out.size()<700){AccessibilityNodeInfo n=queue.remove();out.add(n);for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo child=n.getChild(i);if(child!=null)queue.add(child);}}return out;}
    private static String name(AccessibilityNodeInfo n){CharSequence value=n.getContentDescription();if(value==null||value.length()==0)value=n.getText();return value==null?"":value.toString();}
    private AccessibilityNodeInfo find(Predicate<String> wanted,boolean clickable){AccessibilityNodeInfo found=null;for(AccessibilityNodeInfo n:nodes()){Rect r=new Rect();n.getBoundsInScreen(r);if(found==null&&(!clickable||n.isClickable())&&n.isEnabled()&&n.isVisibleToUser()&&r.width()>0&&r.height()>0&&wanted.test(name(n)))found=AccessibilityNodeInfo.obtain(n);n.recycle();}return found;}
    private boolean has(Predicate<String> wanted,boolean clickable){AccessibilityNodeInfo n=find(wanted,clickable);if(n==null)return false;n.recycle();return true;}
    private AccessibilityNodeInfo waitFor(Predicate<String> wanted,boolean clickable,long timeout)throws Exception{long end=SystemClock.uptimeMillis()+timeout;do{AccessibilityNodeInfo n=find(wanted,clickable);if(n!=null)return n;SystemClock.sleep(bulk?8:70);}while(SystemClock.uptimeMillis()<end);throw new AssertionError("Missing control; visible="+labels());}
    private List<String> labels(){List<String> out=new ArrayList<>();for(AccessibilityNodeInfo n:nodes()){out.add(name(n));n.recycle();}return out;}
    private void clickExact(String label,boolean seek)throws Exception{click(x->x.equalsIgnoreCase(label),seek);}
    private void click(Predicate<String> wanted,boolean seek)throws Exception{
        for(int i=0;i<(seek?16:1);i++){
            AccessibilityNodeInfo n=find(wanted,true);
            if(n!=null){String label=name(n);if(label.startsWith("Sign in with Google")||label.contains("Simulate")||label.contains("Watch ad")||(label.startsWith("LumaGold")&&!label.equals("LumaGold"))||label.contains("Buy gold"))throw new AssertionError("Provider/payment control forbidden: "+label);
                boolean ok=n.performAction(AccessibilityNodeInfo.ACTION_CLICK);n.recycle();if(!ok)throw new AssertionError("SDK rejected click: "+label);
                actions.put(new JSONObject().put("label",label).put("time",SystemClock.uptimeMillis()));SystemClock.sleep(150);return;
            }if(seek)scroll(true);
        }throw new AssertionError("Missing clickable control; visible="+labels());
    }
    private boolean scroll(boolean forward){AccessibilityNodeInfo host=find(x->x.startsWith("Luma Drift · "),false);if(host==null)return false;boolean ok=host.performAction(forward?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);host.recycle();SystemClock.sleep(120);return ok;}
    private String nativeLayoutSignature(){StringBuilder value=new StringBuilder();for(AccessibilityNodeInfo node:nodes()){Rect box=new Rect();node.getBoundsInScreen(box);value.append(name(node)).append('@').append(box.toShortString()).append(';');node.recycle();}return value.toString();}
    // The canvas may acknowledge a no-op scroll at its boundary; stop only after two actual stable native layouts.
    private void top(){String previous=nativeLayoutSignature();int stable=0;for(int i=0;i<18;i++){if(!scroll(false))return;String current=nativeLayoutSignature();stable=current.equals(previous)?stable+1:0;if(stable>=2)return;previous=current;}}
    private void back(){ui.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);SystemClock.sleep(190);}
    private void home()throws Exception{if(has(x->(Arrays.asList(SKIP).contains(x)||Arrays.asList(LATER).contains(x)),true)){click(x->(Arrays.asList(SKIP).contains(x)||Arrays.asList(LATER).contains(x)),false);}for(int i=0;i<6;i++){if(has(x->x.equals("Home")||x.equals("Accueil"),true)){click(x->x.equals("Home")||x.equals("Accueil"),false);return;}back();}launch();}
    private void setInput(String text)throws Exception{AccessibilityNodeInfo edit=null;for(AccessibilityNodeInfo n:nodes()){if(n.isEditable()&&edit==null)edit=AccessibilityNodeInfo.obtain(n);n.recycle();}if(edit==null)throw new AssertionError("Native dialog input absent");Bundle value=new Bundle();value.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,text);check(edit.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,value),"Native dialog text input accepts SDK action");edit.recycle();SystemClock.sleep(90);}
    private JSONObject fixture(int number)throws Exception{return fixtureNamed((args.getString("catalogFamily","N").equals("L")?"l":"n")+number);}
    private JSONObject fixtureNamed(String name)throws Exception{try(InputStream in=getContext().getAssets().open("fixtures/"+name+".json");ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] bytes=new byte[4096];int n;while((n=in.read(bytes))!=-1)out.write(bytes,0,n);return new JSONObject(out.toString("UTF-8"));}}
    private void piece(int row,int col)throws Exception{Predicate<String> wanted=x->x.contains("Piece, row "+(row+1)+", column "+(col+1)+", arrow ");if(allowV18)click(wanted,false);else touchNode(wanted,false);}
    private void seekControl(Predicate<String> wanted)throws Exception{for(int i=0;i<16;i++){AccessibilityNodeInfo node=find(wanted,true);if(node!=null){Rect r=new Rect();node.getBoundsInScreen(r);node.recycle();android.graphics.Point display=new android.graphics.Point();((android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(display);if(r.exactCenterY()>0&&r.bottom<display.y-24*getTargetContext().getResources().getDisplayMetrics().density)return;}scroll(true);}throw new AssertionError("Cannot bring native touch control fully into the viewport: "+labels());}
    private Rect touchNode(Predicate<String> wanted,boolean doubleTap)throws Exception{
        AccessibilityNodeInfo node=waitFor(wanted,true,3000);Rect bounds=new Rect();node.getBoundsInScreen(bounds);String observed=name(node);node.recycle();
        if(observed.contains("Google")||observed.contains("Watch ad")||(observed.contains("LumaGold")&&!observed.equals("LumaGold")))throw new AssertionError("Provider control forbidden");
        int repeats=doubleTap?2:1;for(int i=0;i<repeats;i++){long down=SystemClock.uptimeMillis();injectTouch(down,MotionEvent.ACTION_DOWN,bounds.exactCenterX(),bounds.exactCenterY());SystemClock.sleep(bulk?8:30);injectTouch(down,MotionEvent.ACTION_UP,bounds.exactCenterX(),bounds.exactCenterY());if(i+1<repeats)SystemClock.sleep(40);}
        actions.put(new JSONObject().put("gesture",doubleTap?"double-tap":"tap").put("observedLabel",observed).put("observedBounds",bounds.toShortString()).put("input","UiAutomation.injectInputEvent; touchscreen"));if(!bulk)SystemClock.sleep(180);return bounds;
    }
    private void piece(JSONObject step)throws Exception{piece(step.getInt("row"),step.getInt("col"));}
    private void dismissPreviewIfPresent()throws Exception{
        if(has(x->x.equals("Make this move · 1 action"),true)){if(allowV18)clickExact("Make this move · 1 action",false);else touchNode(x->x.equals("Make this move · 1 action"),false);}
        else if(has(x->x.startsWith("Open ")&&x.endsWith(" gate · 1 action")||x.equals("Open · 1 action"),true)){Predicate<String> target=x->x.startsWith("Open ")&&x.endsWith(" gate · 1 action")||x.equals("Open · 1 action");if(allowV18)click(target,false);else touchNode(target,false);}
    }
    private void action(JSONObject step)throws Exception{
        if(bulkCache&&journeyCacheActive){cachedJourneyAction(step);return;}
        String before=session().getString("history","");int used=session().getInt("budget_used",-1);
        if(step.getString("type").equals("OPEN_GATE")){
            String direction=step.getString("directionLabel");
            Predicate<String> gate=x->x.contains("Gate closed")&&x.toLowerCase(Locale.ROOT).contains(direction.toLowerCase(Locale.ROOT));
            if(allowV18)click(gate,false);else touchNode(gate,false);
            if(bulk)waitFor(x->x.equals("Keep planning"),true,3000).recycle();
            if(!allowV18&&!previewCancellationChecked){
                waitFor(x->x.equals("Keep planning"),true,3000).recycle();capture("gate-preview-visible-before-cancel-en");touchNode(x->x.equals("Keep planning"),false);
                check(session().getString("history","").equals(before)&&session().getInt("budget_used",-1)==used,"Real native preview cancellation spends no action and changes no history");
                check(!has(x->x.equals("Keep planning"),true),"Canceled native preview returns to actual board controls");
                touchNode(gate,false);waitFor(x->x.equals("Keep planning"),true,3000).recycle();previewCancellationChecked=true;
            }
            check(session().getString("history","").equals(before)&&session().getInt("budget_used",-1)==used,"Opening preview changes neither history nor action allowance");
            dismissPreviewIfPresent();
        }else{piece(step);if(bulk&&step.optInt("switch",0)!=0)waitFor(x->x.equals("Make this move · 1 action"),true,3000).recycle();dismissPreviewIfPresent();}
        if(bulk){String expected=before.isEmpty()?Integer.toString(step.getInt("action")):before+","+step.getInt("action");long until=SystemClock.uptimeMillis()+3000;while(!session().getString("history","").equals(expected)&&SystemClock.uptimeMillis()<until)SystemClock.sleep(4);}
        check(session().getString("history","").equals(before.isEmpty()?Integer.toString(step.getInt("action")):before+","+step.getInt("action")),"Typed action "+step.getInt("action")+" is recorded exactly once through UI");
    }
    private String pieceCacheKey(int row,int col){return "piece:"+row+","+col;}
    private void observeJourneyGeometry(int number)throws Exception{
        cachedBoardBounds.clear();cachedBoardLabels.clear();cachedPreviewConfirm=null;cachedLevel=number;cachedRotation=((android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();JSONArray observed=new JSONArray();
        java.util.regex.Pattern pattern=java.util.regex.Pattern.compile("Piece, row (\\d+), column (\\d+),");String[] directions={"up","right","down","left"};
        for(AccessibilityNodeInfo node:nodes()){
            String label=name(node),key=null;java.util.regex.Matcher match=pattern.matcher(label);if(match.find())key=pieceCacheKey(Integer.parseInt(match.group(1))-1,Integer.parseInt(match.group(2))-1);
            else if(label.startsWith("Gate open")||label.startsWith("Gate closed"))for(int d=0;d<directions.length;d++)if(label.toLowerCase(Locale.ROOT).contains(directions[d])){key="gate:"+d;break;}
            if(key!=null){Rect bounds=new Rect();node.getBoundsInScreen(bounds);if(!node.isVisibleToUser()||!node.isEnabled()||bounds.isEmpty())throw new AssertionError("Unreachable initial native cache target: "+label);cachedBoardBounds.put(key,new Rect(bounds));cachedBoardLabels.put(key,label);observed.put(new JSONObject().put("key",key).put("label",label).put("bounds",bounds.toShortString()));}node.recycle();
        }
        actions.put(new JSONObject().put("observation","initial-native-board-geometry").put("level",number).put("rotation",cachedRotation).put("targets",observed));journeyCacheActive=true;
    }
    private void cachedTouch(String key)throws Exception{
        if(session().getInt("level",-1)!=cachedLevel||((android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation()!=cachedRotation)throw new AssertionError("Observed geometry invalidated by level or rotation");Rect bounds=cachedBoardBounds.get(key);if(bounds==null)throw new AssertionError("No actual initial UI bounds for "+key);touchRect(bounds);
        actions.put(new JSONObject().put("gesture","tap").put("observedLabelAtLevelStart",cachedBoardLabels.get(key)).put("observedBounds",bounds.toShortString()).put("level",cachedLevel).put("input","UiAutomation.injectInputEvent; touchscreen at cached observed immutable board geometry; action history verified afterwards"));
    }
    private void touchRect(Rect bounds)throws Exception{long down=SystemClock.uptimeMillis();injectTouch(down,MotionEvent.ACTION_DOWN,bounds.exactCenterX(),bounds.exactCenterY());SystemClock.sleep(8);injectTouch(down,MotionEvent.ACTION_UP,bounds.exactCenterX(),bounds.exactCenterY());}
    private void awaitNativeDraw()throws Exception{
        java.util.concurrent.CountDownLatch drawn=new java.util.concurrent.CountDownLatch(1);runOnMainSync(()->activity.getWindow().getDecorView().postOnAnimation(()->activity.getWindow().getDecorView().postOnAnimation(drawn::countDown)));if(!drawn.await(1500,java.util.concurrent.TimeUnit.MILLISECONDS))throw new AssertionError("Actual preview frame did not render");
    }
    private void cachedJourneyAction(JSONObject step)throws Exception{
        long startedAt=SystemClock.uptimeMillis();String before=session().getString("history","");int used=session().getInt("budget_used",-1),id=step.getInt("action");boolean gate=step.getString("type").equals("OPEN_GATE"),preview=gate||step.optInt("switch",0)!=0;String key=gate?"gate:"+step.getInt("direction"):pieceCacheKey(step.getInt("row"),step.getInt("col"));cachedTouch(key);
        if(preview){
            awaitNativeDraw();
            if(gate&&!previewCancellationChecked){waitFor(x->x.equals("Keep planning"),true,3000).recycle();capture("gate-preview-visible-before-cancel-en");touchNode(x->x.equals("Keep planning"),false);check(session().getString("history","").equals(before)&&session().getInt("budget_used",-1)==used,"Real cached native preview cancellation spends no action and changes no history");awaitNativeDraw();check(!has(x->x.equals("Keep planning"),true),"Canceled cached native preview returns to actual board controls");cachedTouch(key);awaitNativeDraw();previewCancellationChecked=true;}
            if(cachedPreviewConfirm==null){AccessibilityNodeInfo node=waitFor(x->x.equals("Make this move · 1 action")||x.startsWith("Open ")&&x.endsWith(" gate · 1 action")||x.equals("Open · 1 action"),true,3000);cachedPreviewConfirm=new Rect();node.getBoundsInScreen(cachedPreviewConfirm);node.recycle();actions.put(new JSONObject().put("observation","native-preview-confirm-geometry").put("level",cachedLevel).put("bounds",cachedPreviewConfirm.toShortString()));}
            check(session().getString("history","").equals(before)&&session().getInt("budget_used",-1)==used,"Observed cached preview changes neither history nor action allowance");touchRect(cachedPreviewConfirm);
        }
        String expected=before.isEmpty()?Integer.toString(id):before+","+id;long deadline=SystemClock.uptimeMillis()+3000;while(!session().getString("history","").equals(expected)&&SystemClock.uptimeMillis()<deadline)SystemClock.sleep(4);
        check(session().getString("history","").equals(expected),"Typed action "+id+" is recorded exactly once through cached observed UI coordinates");check(session().getInt("budget_used",-1)==used+1,"Cached native action "+id+" spends exactly one productive move");
        actions.put(new JSONObject().put("observation","typed-action-timing").put("level",cachedLevel).put("diagnostic",journeyDiagnostic).put("action",id).put("kind",gate?"manual-gate":preview?"switch-exit":"ordinary-exit").put("durationMs",SystemClock.uptimeMillis()-startedAt).put("budgetBefore",used).put("budgetAfter",session().getInt("budget_used",-1)).put("historyAfter",expected));
    }
    private void openTutorial()throws Exception{
        home();top();click(x->x.equals("Little star school")||x.equals("Continue learning")||x.equals("Replay the interactive tutorial")||x.startsWith("Little star school,"),true);
        if(has(x->x.equals("Let's try!"),true))clickExact("Let's try!",false);
    }
    private void quickTutorialResume(String saved,String label)throws Exception{
        long feedbackAt=lastUiTouchUpAt;check(feedbackAt>0,"Rapid resume timing starts at a real observed SDK ACTION_UP: "+label);clickExact("Later",false);check(tutorialState().contains("|SKIPPED|"),"Rapid Later stores actual paused practice: "+label);
        Predicate<String> learning=x->x.equals("Little star school")||x.equals("Continue learning")||x.equals("Replay the interactive tutorial")||x.startsWith("Little star school,");boolean opened=false;
        for(int i=0;i<4;i++){if(has(learning,true)){click(learning,false);opened=true;break;}scroll(true);}check(opened,"Visible home learning control reopens practice directly: "+label);long resumedAt=SystemClock.uptimeMillis(),elapsed=resumedAt-feedbackAt;
        check(tutorialState().equals(saved),"Rapid Resume restores the exact observed lesson, phase and history: "+label);check(elapsed>=0&&elapsed<2400,"Real Later/Resume finishes inside the original2400ms feedback window: "+label+" elapsed="+elapsed+"ms");actions.put(new JSONObject().put("observation","rapid-tutorial-resume").put("label",label).put("feedbackSdkActionUpAt",feedbackAt).put("resumedAt",resumedAt).put("elapsedMs",elapsed).put("deadlineMs",2400).put("savedBefore",saved).put("restoredAfter",tutorialState()));
    }
    private void tutorial()throws Exception{
        home();int beforeCoins=coins(),beforeHints=stock("hint");boolean already=save().getBoolean("tutorial_v18_reward_claimed",false);
        openTutorial();
        // A completed existing lesson is replayed through its visible replay action.
        if(lesson()==7&&has(x->x.equals("Replay the interactive tutorial"),true))clickExact("Replay the interactive tutorial",false);
        check(lesson()>=0&&lesson()<7,"Optional tutorial opens real saved practice rather than an informational slideshow");capture("tutorial-start-en");
        // Replay creates a clean practice sequence, without editing any save record.
        if(lesson()!=0||phase()!=0){if(has(x->x.equals("Replay the interactive tutorial"),true))clickExact("Replay the interactive tutorial",false);else if(has(x->x.equals("Try this lesson again"),true))clickExact("Try this lesson again",false);}
        int iterations=0;boolean skipChecked=false,rotationChecked=false,gateResumeChecked=false;
        while(lesson()<7){if(iterations++>70)throw new AssertionError("Tutorial did not advance");int l=lesson(),p=phase();
            if(!skipChecked&&l==1&&p==1){String saved=tutorialState();quickTutorialResume(saved,"Blocked lesson2 phase1");check(coins()==beforeCoins&&stock("hint")==beforeHints,"Skipping/resuming tutorial grants no reward and consumes no stock");skipChecked=true;capture("tutorial-resumed-en");}
            if(!rotationChecked&&l==2&&p==1){String saved=tutorialState();rotatePractice(saved);rotationChecked=true;}
            capture("tutorial-"+(l+1)+"-phase-"+p+"-en");
            switch(l){
                case 0: if(p==0){
                    if(gestures){String saved=tutorialState();dragPiece(1,1,0,"en");check(tutorialState().equals(saved),"Wrong-direction native SDK drag leaves the practice phase and history unchanged");capture("tutorial-wrong-drag-en");dragPiece(1,1,1,"en");check(phase()==1&&tutorialState().endsWith("|0"),"A real rightward SDK drag completes the exit lesson and records exactly one piece");}
                    else piece(1,1);
                }else nextLesson();break;
                case 1: if(p==0||p==2)piece(1,0);else if(p==1)piece(1,2);else nextLesson();break;
                case 2: if(p==0||p==2)piece(1,0);else if(p==1)piece(0,0);else nextLesson();break;
                case 3: if(p==0){
                    if(allowV18)click(x->x.contains("Gate closed")&&x.toLowerCase(Locale.ROOT).contains("up"),false);
                    else {String prior=tutorialState();JSONObject drift=dragGateCoach();drift.put("before",prior).put("after",tutorialState());write("gate-drift-fixed.json",drift.toString(2));check(phase()==1,"Real gate coach15-point native drift opens the expected preview in compact portrait");capture("tutorial-gate-drift-fixed-en");}
                }else if(p==1){
                    if(allowV18)dismissPreviewIfPresent();else{String saved=tutorialState();quickTutorialResume(saved,"Gate preview lesson4 phase1");gateResumeChecked=true;rotatePractice(saved);check(lesson()==3&&phase()==1,"Gate preview survives real rotation and activity recreation");
                        seekControl(x->x.startsWith("Open ")&&x.endsWith(" gate · 1 action")||x.equals("Open · 1 action"));touchNode(x->x.startsWith("Open ")&&x.endsWith(" gate · 1 action")||x.equals("Open · 1 action"),true);
                        check(phase()==2&&tutorialState().endsWith("|64"),"Double native gate confirmation records one opening only");capture("tutorial-gate-double-confirm-en");
                    }
                }else if(p==2)piece(1,1);else nextLesson();break;
                case 4: if(p==0)piece(1,1);else if(p==1){if(has(x->x.equals("Make this move · 1 action"),true))clickExact("Make this move · 1 action",false);else piece(1,1);}else if(p==2)piece(0,0);else nextLesson();break;
                case 5: if(p==0||p==2)piece(0,0);else if(p==1)clickExact("Try undoing for free",false);else if(p==3)piece(2,2);else nextLesson();break;
                case 6: if(p==0)clickExact("Try a free Spark",false);else if(p==1)piece(1,2);else if(p==2)piece(1,0);else nextLesson();break;
                default:throw new AssertionError("Unexpected lesson");
            }
            check(lesson()!=l||phase()!=p,"Lesson "+(l+1)+" phase "+p+" advances only after its requested teaching action");
            check(coins()==beforeCoins&&stock("hint")==beforeHints,"Practice phase spends no earned coins or real power charges");
        }
        check(skipChecked&&gateResumeChecked,"Both blocked-move and gate-preview rapid resume windows are actually exercised");check(tutorialState().equals("T1|7|0|COMPLETE|"),"All seven interactive lessons durably complete");capture("tutorial-complete-en");
        if(!already){clickExact("Collect my welcome gift",false);check(coins()==beforeCoins+100&&stock("hint")==beforeHints+2,"First completion grants exactly100 LumaCoins and2 earned Sparks");}
        else check(coins()==beforeCoins&&stock("hint")==beforeHints,"An existing welcome receipt cannot be claimed again");
        int claimedCoins=coins(),claimedHints=stock("hint");
        check(save().getBoolean("tutorial_v18_reward_claimed",false),"Welcome gift has one durable receipt");
        if(has(x->x.equals("Collect my welcome gift")||x.equals("Welcome gift already collected"),true))click(x->x.equals("Collect my welcome gift")||x.equals("Welcome gift already collected"),false);
        check(coins()==claimedCoins&&stock("hint")==claimedHints,"Repeated reward action adds no second gift");capture("tutorial-reward-en");
        if(has(x->x.equals("Start my adventure"),true))clickExact("Start my adventure",false);home();
        openTutorial();if(has(x->x.equals("Replay the interactive tutorial"),true))clickExact("Replay the interactive tutorial",false);
        check(coins()==claimedCoins&&stock("hint")==claimedHints,"Replaying remains free and preserves the original welcome receipt");
        if(has(x->x.equals("Play at my pace"),true))clickExact("Play at my pace",true);home();
    }
    private void dragPiece(int row,int col,int direction,String language)throws Exception{
        Predicate<String> label=language.equals("fr")?x->x.contains("Pièce, ligne "+(row+1)+", colonne "+(col+1)+","):x->x.contains("Piece, row "+(row+1)+", column "+(col+1)+", arrow ");
        AccessibilityNodeInfo node=waitFor(label,true,3000);Rect bounds=new Rect();node.getBoundsInScreen(bounds);String observed=name(node);node.recycle();
        check(bounds.width()>0&&bounds.height()>0,"SDK gesture target is the observed accessible piece: "+observed);
        int[] dc={0,1,0,-1},dr={-1,0,1,0};float distance=Math.max(42*getTargetContext().getResources().getDisplayMetrics().density,Math.min(bounds.width(),bounds.height())*.82f);
        float x=bounds.exactCenterX(),y=bounds.exactCenterY(),dx=dc[direction]*distance,dy=dr[direction]*distance;long down=SystemClock.uptimeMillis();
        injectTouch(down,MotionEvent.ACTION_DOWN,x,y);
        for(int i=1;i<=6;i++){SystemClock.sleep(30);injectTouch(down,MotionEvent.ACTION_MOVE,x+dx*i/6,y+dy*i/6);}
        SystemClock.sleep(25);injectTouch(down,MotionEvent.ACTION_UP,x+dx,y+dy);SystemClock.sleep(150);
        actions.put(new JSONObject().put("gesture","drag").put("observedLabel",observed).put("observedBounds",bounds.toShortString()).put("direction",direction).put("durationMs",SystemClock.uptimeMillis()-down).put("input","UiAutomation.injectInputEvent; touchscreen; six native MOVE events"));
    }
    private void injectTouch(long down,int action,float x,float y)throws Exception{
        if(action==MotionEvent.ACTION_UP)lastUiTouchUpAt=SystemClock.uptimeMillis();
        MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        try{if(!ui.injectInputEvent(event,true))throw new AssertionError("SDK rejected native touch event "+action);}finally{event.recycle();}
    }
    private void campaignGesture()throws Exception{
        openJourney(1);restart();JSONObject first=fixture(1).getJSONArray("steps").getJSONObject(0);int before=session().getInt("budget_used",-1);
        dragPiece(first.getInt("row"),first.getInt("col"),first.getInt("direction"),"en");
        check(session().getInt("budget_used",-1)==before+1&&session().getString("history","bad").equals(Integer.toString(first.getInt("action"))),"A real campaign SDK drag follows the actual arrow and charges exactly one action");capture("journey-native-drag-en");home();
    }
    private void nextLesson()throws Exception{click(x->x.startsWith("Next little lesson")||x.equals("Ready for your adventure!")||x.equals("Start my adventure"),false);}
    private void rotatePractice(String saved)throws Exception{
        android.view.WindowManager wm=(android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE);int original=wm.getDefaultDisplay().getRotation();RotationFrame viewport=observeRotationFrame();
        try{rotateAndAwait(UiAutomation.ROTATION_FREEZE_90,viewport,"Tutorial landscape layout");check(tutorialState().equals(saved),"Tutorial retains its exact teaching phase and preview through activity recreation");capture("tutorial-key-landscape-en");}
        finally{rotateAndAwait(original,viewport,"Portrait return layout");}check(tutorialState().equals(saved),"Tutorial phase remains durable after portrait return");
    }
    private void menus()throws Exception{
        hubV110();home();Map<String,Object> before=presentationState(false);clickExact("Shop",false);expectParent("Shop");
        clickExact("Powers",false);check(selectedControl("Powers"),"Shop opens its earned-power category");assertOrganicSelection("Powers");clickExact("Appearance",false);check(selectedControl("Appearance"),"Shop separates cosmetic appearances");assertOrganicSelection("Appearance");check(has(x->x.equals("LumaGold"),true),"Distinct premium category is labelled without entering wallet or purchase flows");check(presentationState(false).equals(before),"Browsing shop categories does not debit or grant anything");capture("shop-appearance-en");home();
    }

    /** Scoped 1.13 shop flow. No account, ad, premium balance request or real-money control is clicked. */
    private void shopFocus()throws Exception{
        home();top();Map<String,Object> before=presentationState(false);assertCurrencyPair("Home");capture("ux-home-wallets-en");
        clickExact("Shop",false);assertCurrencyPair("Shop");
        for(String tab:new String[]{"Powers","Appearance","LumaGold"}){
            top();touchNode(x->x.equals(tab),false);check(selectedControl(tab),"Shop selects the explicit named category: "+tab);
            check(presentationState(false).equals(before),"Category selection is read-only: "+tab);capture("ux-shop-"+tab.toLowerCase(Locale.ROOT)+"-en");
        }
        top();touchNode(x->x.equals("Powers"),false);top();
        seekControl(x->x.startsWith("Spark,")||x.startsWith("Hint,"));
        touchNode(x->x.startsWith("Spark,")||x.startsWith("Hint,"),false);
        check(has(x->x.startsWith("Luma Drift · "),false),"Power details retain the normal canvas page instead of an AlertDialog");
        assertVisibleTouchTarget(x->x.equalsIgnoreCase("Close"),"Power detail Close");
        check(presentationState(false).equals(before),"Opening a power explanation does not buy stock");check(has(x->x.equals("In stock: "+stock("hint")),false),"Power stock is accessible as actual text rather than decorative art");check(has(x->x.equals("Show one safe move for up to 8 seconds without playing it. Uses no move."),false),"Spark detail exposes its complete accessible effect and no-move cost");capture("ux-power-detail-en");
        touchNode(x->x.equalsIgnoreCase("Close"),false);check(has(x->x.equals("Luma Drift · Shop"),false),"Explicit Close returns to the Shop caller");
        check(presentationState(false).equals(before),"Closing power details leaves the entire economy unchanged");
        top();seekControl(x->x.startsWith("Spark,"));touchNode(x->x.startsWith("Spark,"),false);
        seekControl(x->x.matches("Get ×[0-9]+ · [0-9]+ earned coins"));
        AccessibilityNodeInfo buy=waitFor(x->x.matches("Get ×[0-9]+ · [0-9]+ earned coins"),true,3000);String offer=name(buy);buy.recycle();
        java.util.regex.Matcher pack=java.util.regex.Pattern.compile("Get ×([0-9]+) · ([0-9]+) earned coins").matcher(offer);check(pack.matches(),"Earned-power offer states both exact quantity and exact currency price");
        int amount=Integer.parseInt(pack.group(1)),price=Integer.parseInt(pack.group(2)),balance=coins(),hintStock=stock("hint");
        check(balance>=price&&amount==3,"Real earned balance can fund the explicitly displayed three-Spark pack");
        assertVisibleTouchTarget(x->x.equals(offer),"Earned Spark purchase");touchNode(x->x.equals(offer),false);
        check(coins()==balance-price&&stock("hint")==hintStock+amount,"Actual earned purchase grants exactly the displayed stock and debits exactly the displayed price");
        check(has(x->x.startsWith("Luma Drift · "),false),"Earned purchase feedback leaves the normal item page usable");capture("ux-power-purchased-en");
        clickExact("Close",true);check(has(x->x.equals("Luma Drift · Shop"),false),"Close after an earned purchase returns to its original shop");before=presentationState(false);
        home();openCompanion();wardrobeItem("Hat","Hat · Cupid",true);
        check(has(x->x.startsWith("Luma Drift · "),false)&&has(x->x.startsWith("Visual preview · no item granted"),false),"Clothing has a normal canvas preview and explicit no-grant state");
        assertVisibleTouchTarget(x->x.equalsIgnoreCase("Close"),"Clothing detail Close");capture("ux-clothing-detail-en");
        touchNode(x->x.equalsIgnoreCase("Close"),false);check(has(x->x.equals("Luma Drift · My companion"),false),"Clothing Close returns to the companion caller");
        check(presentationState(false).equals(before),"Clothing preview and Close grant, equip and debit nothing");home();
    }
    private void shopLocales()throws Exception{
        String[] ids={"en","fr","de","es","pt","it"},names={"English","Français","Deutsch","Español","Português","Italiano"};
        String[] homes={"Home","Accueil","Start","Inicio","Início","Home"},shops={"Shop","Boutique","Shop","Tienda","Loja","Negozio"};
        String[] powers={"Powers","Pouvoirs","Kräfte","Poderes","Poderes","Poteri"},appearance={"Appearance","Apparence","Aussehen","Apariencia","Aparência","Aspetto"};
        String[] sparks={"Spark","Étincelle","Funke","Chispa","Faísca","Scintilla"},closes={"Close","Fermer","Schließen","Cerrar","Fechar","Chiudi"};
        for(int i=0;i<ids.length;i++){
            home();click(x->Arrays.asList(SETTINGS).contains(x),false);clickExact(names[i],true);back();top();
            Map<String,Object> before=presentationState(false);assertCurrencyPair("Localized Home "+ids[i]);capture("ux-home-wallets-"+ids[i]);
            clickExact(shops[i],false);top();assertCurrencyPair("Localized Shop "+ids[i]);clickExact(powers[i],false);
            check(selectedControl(powers[i]),"Actual "+ids[i]+" power category is translated and selected");capture("ux-shop-powers-"+ids[i]);
            final String spark=sparks[i];seekControl(x->x.startsWith(spark+","));touchNode(x->x.startsWith(spark+","),false);
            check(has(x->x.startsWith("Luma Drift · "),false),"Actual "+ids[i]+" power detail remains on the canvas");
            final String close=closes[i];assertVisibleTouchTarget(x->x.equalsIgnoreCase(close),"Localized "+ids[i]+" Close");capture("ux-power-detail-"+ids[i]);
            touchNode(x->x.equalsIgnoreCase(close),false);top();clickExact(appearance[i],false);check(selectedControl(appearance[i]),"Actual "+ids[i]+" appearance category is translated and selected");capture("ux-shop-appearance-"+ids[i]);
            top();touchNode(x->x.equals("LumaGold"),false);check(selectedControl("LumaGold"),"Actual "+ids[i]+" premium category remains a distinct named destination");capture("ux-shop-gold-"+ids[i]);
            check(presentationState(false).equals(before),"Actual "+ids[i]+" shop/category/Close navigation makes no economic or progression change");
            clickExact(homes[i],false);click(x->Arrays.asList(SETTINGS).contains(x),false);clickExact("English",true);back();
        }
    }
    private void assertCurrencyPair(String location)throws Exception{
        AccessibilityNodeInfo earned=waitFor(x->x.startsWith("LumaCoins")&&!x.equals("LumaCoins"),false,3000);
        AccessibilityNodeInfo premium=waitFor(x->x.startsWith("LumaGold")&&!x.equals("LumaGold"),false,3000);
        Rect a=new Rect(),b=new Rect();earned.getBoundsInScreen(a);premium.getBoundsInScreen(b);String earnedLabel=name(earned),premiumLabel=name(premium);earned.recycle();premium.recycle();
        check(earnedLabel.contains(Integer.toString(coins())),location+" earned currency exposes the exact actual balance");
        check(!Rect.intersects(a,b),location+" named currency nodes have distinct, nonoverlapping bounds");
        check(!premiumLabel.matches("LumaGold[: ·]*0"),location+" guest premium state is not represented as a verified zero");
        actions.put(new JSONObject().put("observation","actual-currency-pair").put("location",location).put("earnedLabel",earnedLabel).put("premiumLabel",premiumLabel).put("earnedBounds",a.toShortString()).put("premiumBounds",b.toShortString()));
    }
    private void assertVisibleTouchTarget(Predicate<String> wanted,String label)throws Exception{
        seekControl(wanted);AccessibilityNodeInfo node=waitFor(wanted,true,3000);Rect bounds=new Rect();node.getBoundsInScreen(bounds);node.recycle();
        Rect game=actualGameBounds();float density=getTargetContext().getResources().getDisplayMetrics().density;
        float width=bounds.width()/density,height=bounds.height()/density;
        check(width>=47.5f&&height>=47.5f,label+" has an actual native touch target of at least48dp (rounding tolerance0.5dp): "+width+"x"+height);
        check(game!=null&&game.contains(bounds),label+" native bounds are within the visible game viewport");
        actions.put(new JSONObject().put("observation","actual-touch-target").put("label",label).put("bounds",bounds.toShortString()).put("widthDp",width).put("heightDp",height));
    }
    private void assertOrganicSelection(String label)throws Exception{
        check(selectedControl(label),"Organic category retains selected semantics: "+label);
        assertVisibleTouchTarget(x->x.equals(label),"Selected shop category "+label);
    }

    private void map()throws Exception{
        home();clickExact("Map",true);for(int n:new int[]{1,7,8,13,100,101,500,1000}){clickExact("Explore a level",false);setInput(Integer.toString(n));clickExact("Continue",false);check(has(x->x.startsWith("Level "+n+","),true),"Map jump exposes real level "+n);capture("map-"+n+"-en");}
        check(scroll(false),"Map supports the SDK's backward scroll action");capture("map-scroll-en");home();
    }
    private void openJourney(int number)throws Exception{home();clickExact("Map",true);clickExact("Explore a level",false);setInput(Integer.toString(number));clickExact("Continue",false);click(x->x.startsWith("Level "+number+","),false);click(x->x.equals("Play")||x.startsWith("Play "),true);}
    private void journey()throws Exception{
        int from=Integer.parseInt(args.getString("journeyFrom","1")),through=Integer.parseInt(args.getString("journeyThrough","100")),captureFirst=Integer.parseInt(args.getString("captureFirst","30"));boolean all="true".equals(args.getString("captureAll","false"));check(from>=1&&from<=through&&through<=1000,"Journey batch range is bounded1..1000");JSONArray observations=new JSONArray();long entryStartedAt=SystemClock.uptimeMillis();openJourney(from);
        for(int n=from;n<=through;n++){
            JSONObject f=fixture(n);JSONArray steps=f.getJSONArray("steps");
            boolean captureLevel=all||n<=captureFirst||n==from||n==through||n%50==0||n==99||n==101||n==999;
            long enteredAt=SystemClock.uptimeMillis();int balanceBefore=coins(),starsBefore=save().getInt("stars_"+n,0),observedPieces=0,gateSteps=0,switchSteps=0;JSONArray initialLabels=new JSONArray();
            for(String label:labels()){initialLabels.put(label);if(label.contains("Piece, row "))observedPieces++;}
            for(int i=0;i<steps.length();i++){JSONObject step=steps.getJSONObject(i);if(step.getString("type").equals("OPEN_GATE"))gateSteps++;else if(step.optInt("switch",0)!=0)switchSteps++;}
            check(session().getInt("level",-1)==n&&session().getString("history","bad").isEmpty()&&session().getInt("budget_used",-1)==0,"Journey "+n+" starts its real fresh board with no injected or inherited actions");
            check(observedPieces==f.getInt("pieces"),"Journey "+n+" native piece count matches the certified fixture: "+observedPieces);
            if(bulkCache)observeJourneyGeometry(n);
            if(captureLevel)capture("journey-"+n+"-start-en");
            if(n==28&&"true".equals(args.getString("exerciseBudgetFailure","false")))learningBudgetFailure();
            long solveStartedAt=SystemClock.uptimeMillis();
            for(int i=0;i<steps.length();i++)action(steps.getJSONObject(i));
            waitFor(x->x.startsWith("Lovely drift!")||x.equals("Lovely! You did it."),false,5000);
            long wonAt=SystemClock.uptimeMillis();
            check(save().getInt("stars_"+n,0)>0,"Journey "+n+" cleared by certified real UI actions, without profile injection");
            check(has(x->x.contains("3 Stars"),false),"Journey "+n+" perfect action path visibly awards three stars");
            check(session().getInt("budget_used",-1)<=f.getInt("base"),"Journey "+n+" exact productive path stays within its certified allowance");
            JSONObject measured=new JSONObject().put("level",n).put("fixtureGridSize",f.getInt("size")).put("observedInitialPieces",observedPieces).put("certifiedProductiveActions",steps.length()).put("manualGateActions",gateSteps).put("switchPieceExits",switchSteps).put("fixtureMinimum",f.getInt("minimum")).put("fixtureAllowance",f.getInt("base")).put("fixtureMargin",f.getInt("margin")).put("enteredAtUptimeMs",enteredAt).put("entryUiDurationMs",enteredAt-entryStartedAt).put("solveStartedAtUptimeMs",solveStartedAt).put("winObservedAtUptimeMs",wonAt).put("automatedSolveDurationMs",wonAt-solveStartedAt).put("savedBudgetUsedAtWin",session().getInt("budget_used",-1)).put("savedElapsedAtWinMs",session().getLong("elapsed_ms",-1)).put("starsBefore",starsBefore).put("starsAfter",save().getInt("stars_"+n,0)).put("coinsBefore",balanceBefore).put("coinsAfter",coins()).put("coinDelta",coins()-balanceBefore).put("initialLabels",initialLabels).put("winLabels",new JSONArray(labels()));
            observations.put(measured);write("journey-observations.json",new JSONObject().put("scope","Real native UI replay of pre-certified solutions; SDK timings include automation waits, animation and preview confirmation, not human solve time or fun").put("levels",observations).toString(2));
            if(captureLevel)capture("journey-"+n+"-cleared-en");
            journeyCacheActive=false;if(n<through){entryStartedAt=SystemClock.uptimeMillis();click(x->x.startsWith("Next"),false);click(x->x.equals("Play")||x.startsWith("Play "),true);}
        }journeyCacheActive=false;home();
    }
    private void learningBudgetFailure()throws Exception{
        JSONObject f=fixtureNamed("budget-failure");check(session().getInt("level",-1)==f.getInt("number")&&session().getInt("budget_policy",-1)==4,"Budget failure uses the actual unlocked N28 deliberate attempt");int balance=coins(),hints=stock("hint");JSONArray prefix=f.getJSONArray("prefix");journeyDiagnostic=true;
        try{
            for(int i=0;i<prefix.length();i++)action(prefix.getJSONObject(i));check(session().getInt("budget_used",-1)==f.getInt("base")&&session().getString("history","").split(",").length==prefix.length(),"N28 spends its whole budget through source-certified legal productive choices, without blocked taps");check(!has(x->x.startsWith("Lovely drift!"),false),"N28 genuinely fails before all pieces are cleared");capture("n28-real-budget-failure-return-available");
            coreUndo();check(session().getInt("budget_used",-1)==f.getInt("base")-1&&session().getInt("undos",-1)==1,"N28 real failure permits exactly one refunding return");String history=session().getString("history","");coreUndo();check(session().getString("history","").equals(history)&&session().getInt("budget_used",-1)==f.getInt("base")-1&&session().getInt("undos",-1)==1,"N28 second return is refused with one action left");
            action(prefix.getJSONObject(prefix.length()-1));check(session().getInt("budget_used",-1)==f.getInt("base")&&session().getInt("undos",-1)==1,"Replaying the returned productive action reaches real zero budget with no return quota");capture("n28-real-budget-failure-no-return");check(!has(x->x.startsWith("Undo ×1"),true),"Exhausted N28 cannot offer a renewed return quota");clickExact("Restart · Free",true);check(session().getInt("budget_used",-1)==0&&session().getInt("undos",-1)==0&&session().getString("history","").isEmpty(),"Free N28 failure Restart restores full attempt budget and exactly one return");check(coins()==balance&&stock("hint")==hints,"The real failure, limited return and free Restart spend no currency or hint stock");capture("n28-free-restart-after-failure");write("budget-failure-observation.json",new JSONObject().put("scope","Actual N28 failed attempt through productive legal choices, one refunding return, refused second return, and free Restart; normal Journey route then wins").put("failureBase",f.getInt("base")).put("prefix",prefix).put("coins",coins()).put("hints",stock("hint")).put("restart",coreState()).toString(2));
        }finally{journeyDiagnostic=false;}
    }
    private void challenge()throws Exception{
        home();JSONObject f=fixture(13);int before=coins(),hints=stock("hint");clickExact("Enter a challenge",true);setInput(f.getString("code"));clickExact("Play",false);check(session().getInt("mode",-1)==4,"Typed strategy challenge opens fair imported mode");
        JSONArray steps=f.getJSONArray("steps");for(int i=0;i<steps.length();i++)action(steps.getJSONObject(i));waitFor(x->x.startsWith("Lovely drift!")||x.equals("Lovely! You did it."),false,5000);
        check(coins()==before&&stock("hint")==hints,"Imported strategy challenge grants no coins and spends no stock");capture("challenge-13-cleared-en");home();
    }
    private void restart()throws Exception{
        click(x->x.contains("Restart"),true);clickExact("Restart",false);
        check(session().getInt("budget_used",-1)==0&&session().getString("history","bad").isEmpty(),"Visible free restart restores a fresh attempt");
    }
    private void audit15Observe(JSONArray events,String name)throws Exception{
        JSONArray gates=new JSONArray();for(String label:labels())if(label.startsWith("Gate open")||label.startsWith("Gate closed"))gates.put(label);
        events.put(new JSONObject().put("name",name).put("atUptimeMs",SystemClock.uptimeMillis()).put("history",session().getString("history","")).put("budgetUsed",session().getInt("budget_used",-1)).put("budgetPolicy",session().getInt("budget_policy",-1)).put("hintId",session().getInt("hint_id",-1)).put("hintStock",stock("hint")).put("coins",coins()).put("gates",gates).put("labels",new JSONArray(labels())));
        capture("level15-"+name+"-en");write("level15-audit.json",new JSONObject().put("scope","Actual RC2 Journey15 player-like branches via observed native touchscreen controls; read-only save observations; no board/profile injection").put("events",events).toString(2));
    }
    private void audit15Action(JSONObject fixture,int id)throws Exception{
        JSONArray steps=fixture.getJSONArray("steps");for(int i=0;i<steps.length();i++)if(steps.getJSONObject(i).getInt("action")==id){action(steps.getJSONObject(i));return;}throw new AssertionError("No observed level15 fixture coordinate for "+id);
    }
    private JSONObject coreState()throws Exception{return new JSONObject().put("level",session().getInt("level",-1)).put("content",session().getInt("content_version",-1)).put("policy",session().getInt("budget_policy",-1)).put("history",session().getString("history","")).put("used",session().getInt("budget_used",-1)).put("undos",session().getInt("undos",-1)).put("coins",coins()).put("hints",stock("hint"));}
    private void resumeSavedCore()throws Exception{click(x->x.startsWith("Resume ·")||x.startsWith("Resume  ")||x.startsWith("Resume "),true);waitFor(x->x.contains("Restart"),true,3000).recycle();}
    private void coreUndo()throws Exception{click(x->x.contains("Undo"),true);}
    private void legacyUpgradePrepare()throws Exception{
        check(save().getInt("stars_14",0)>0,"Legacy upgrade earns access through actual L1–14");openJourney(15);restart();JSONObject f=fixtureNamed("l15");audit15Action(f,0);for(int i=0;i<5;i++)piece(2,2);
        check(session().getInt("content_version",-1)==5&&session().getInt("budget_policy",-1)==3&&session().getString("history","").equals("0")&&session().getInt("budget_used",-1)==6&&session().getInt("undos",-1)==0,"Real RC2 legacy L15 checkpoint contains one exit and five rejected paid attempts");
        JSONObject state=coreState();state.put("processId",android.os.Process.myPid());write("legacy-upgrade-checkpoint.json",state.toString(2));capture("legacy-l15-rc2-paid-blocks");
    }
    private void legacyUpgradeVerify()throws Exception{
        JSONObject before=new JSONObject(readFile(new File(output,"legacy-upgrade-checkpoint.json")));resumeSavedCore();JSONObject after=coreState();
        check(after.getInt("level")==15&&after.getInt("content")==5&&after.getString("history").equals(before.getString("history")),"Update preserves the actual L15 identity and exact productive board history");
        check(after.getInt("policy")==4&&after.getInt("used")==1&&after.getInt("undos")==0,"Update removes five rejected-attempt charges and adopts one-Undo policy without inventing a move");
        check(after.getInt("coins")==before.getInt("coins")&&after.getInt("hints")==before.getInt("hints"),"Update changes neither earned wallet nor stock");capture("legacy-l15-migrated");
        piece(2,2);check(session().getInt("budget_used",-1)==1&&session().getString("history","").equals("0"),"A still-blocked legacy tile is now free after update");coreUndo();check(session().getInt("budget_used",-1)==0&&session().getInt("undos",-1)==1&&session().getString("history","").isEmpty(),"Legacy L15 has exactly one real refunding Undo after migration");
        audit15Action(fixtureNamed("l15"),0);String history=session().getString("history","");coreUndo();check(session().getString("history","").equals(history)&&session().getInt("budget_used",-1)==1&&session().getInt("undos",-1)==1,"Second Undo is refused on migrated L15 with no hidden budget or board change");capture("legacy-l15-undo-quota-spent");write("legacy-upgrade-quota-checkpoint.json",coreState().toString(2));
    }
    private void legacyUpgradeCold()throws Exception{
        JSONObject prior=new JSONObject(readFile(new File(output,"legacy-upgrade-quota-checkpoint.json")));resumeSavedCore();JSONObject now=coreState();check(now.toString().equals(prior.toString()),"Legacy L15 board, policy, spent Undo quota, wallet and stock survive a distinct cold process");
        coreUndo();check(coreState().toString().equals(prior.toString()),"Cold restart cannot renew the legacy Undo quota");capture("legacy-l15-cold-quota-retained");restart();check(session().getInt("undos",-1)==0&&session().getInt("budget_used",-1)==0&&session().getInt("content_version",-1)==5,"Free Restart resets the legacy attempt and its single Undo while retaining L geometry");
        JSONObject f=fixtureNamed("l15");JSONArray plan=f.getJSONArray("steps");for(int i=0;i<plan.length();i++)action(plan.getJSONObject(i));waitFor(x->x.startsWith("Lovely drift!")||x.equals("Lovely! You did it."),false,5000);check(save().getInt("stars_15",0)==3,"The migrated real legacy L15 remains completable with three stars");capture("legacy-l15-upgraded-win");home();
    }
    private void learningCorePrepare()throws Exception{
        check(save().getInt("stars_14",0)>0,"New N15 core audit uses legitimately earned level access");openJourney(15);restart();check(session().getInt("content_version",-1)==6&&session().getInt("budget_policy",-1)==4&&session().getInt("undos",-1)==0,"A new N15 attempt has the distinct N identity and one-Undo policy");capture("n15-initial");
        String blocked=null;for(String label:labels())if(label.contains("Piece, row ")&&label.contains("Path blocked")){blocked=label;break;}check(blocked!=null,"N15 exposes a real blocked piece for the five-attempt regression");final String target=blocked;String empty=session().getString("history","");int balance=coins(),hints=stock("hint");for(int i=0;i<5;i++)touchNode(x->x.equals(target),false);
        check(session().getString("history","").equals(empty)&&session().getInt("budget_used",-1)==0&&session().getInt("undos",-1)==0&&coins()==balance&&stock("hint")==hints,"Five N15 blocked taps are free and preserve board, quota, wallet and stock");capture("n15-five-blocks-free");
        check(hints>0,"N15 Spark regression uses genuine existing stock");click(x->x.startsWith("Spark:"),true);clickExact("Use one charge",false);check(stock("hint")==hints-1&&session().getInt("hint_id",-1)>=0&&session().getInt("budget_used",-1)==0,"Spark still works after five blocked N15 taps, consuming one stock and no action");capture("n15-spark-after-blocks");restart();
        JSONObject first=fixtureNamed("n15").getJSONArray("steps").getJSONObject(0);action(first);coreUndo();check(session().getString("history","").isEmpty()&&session().getInt("budget_used",-1)==0&&session().getInt("undos",-1)==1,"N15 first Undo restores its action and spends its only return quota");action(first);String history=session().getString("history","");coreUndo();check(session().getString("history","").equals(history)&&session().getInt("budget_used",-1)==1&&session().getInt("undos",-1)==1,"N15 second Undo is refused without changing history or action budget");capture("n15-second-undo-refused");write("learning-core-checkpoint.json",coreState().toString(2));
    }
    private void learningCoreCold()throws Exception{
        JSONObject prior=new JSONObject(readFile(new File(output,"learning-core-checkpoint.json")));resumeSavedCore();check(coreState().toString().equals(prior.toString()),"N15 exact board, spent one-Undo quota, policy, coins and stock survive a distinct cold process");coreUndo();check(coreState().toString().equals(prior.toString()),"Cold relaunch cannot renew N15 Undo quota");capture("n15-cold-quota-retained");restart();check(session().getInt("budget_used",-1)==0&&session().getInt("undos",-1)==0&&session().getString("history","").isEmpty(),"Free N15 Restart creates a fresh full-budget attempt and renews exactly one Undo");
        JSONArray steps=fixtureNamed("n15").getJSONArray("steps");for(int i=0;i<steps.length();i++)action(steps.getJSONObject(i));waitFor(x->x.startsWith("Lovely drift!")||x.equals("Lovely! You did it."),false,5000);check(save().getInt("stars_15",0)==3,"New N15 wins natively on its authored path after blocked taps, quota refusal, cold relaunch and free Restart");capture("n15-restart-win");home();
    }
    private void level15Audit()throws Exception{
        check(save().getInt("stars_14",0)>0,"L15 branch audit uses genuinely earned access through level14");
        JSONObject fixture=fixture(15);JSONArray events=new JSONArray();openJourney(15);restart();int initialCoins=coins(),initialHints=stock("hint");check(initialHints>0,"L15 diagnostic has an actual earned Spark stock, never injected");audit15Observe(events,"initial");
        // A direction error is different from a blocked attempt: it must not spend a move.
        dragPiece(3,0,0,"en");check(session().getString("history","bad").isEmpty()&&session().getInt("budget_used",-1)==0,"L15 opposite-direction drag leaves both board history and budget unchanged");audit15Observe(events,"opposite-drag");
        dragPiece(3,0,2,"en");check(session().getString("history","bad").equals("0")&&session().getInt("budget_used",-1)==1,"L15 arrow-direction drag exits its real tile exactly once");audit15Observe(events,"correct-drag");
        click(x->x.contains("Undo"),true);check(session().getString("history","bad").isEmpty()&&session().getInt("budget_used",-1)==0,"L15 Undo refunds the successful drag and restores its tile");
        audit15Action(fixture,0);check(session().getInt("budget_used",-1)==1,"L15 a physical tap follows the same fixed-arrow exit rule as a correct drag");click(x->x.contains("Undo"),true);
        // Open gates do not manually toggle shut, and a switch preview can be cancelled freely.
        touchNode(x->x.startsWith("Gate open")&&x.contains("up"),false);check(session().getString("history","bad").isEmpty()&&session().getInt("budget_used",-1)==0,"L15 touching an already open gate neither closes it nor spends a move");
        touchNode(x->x.startsWith("Gate closed")&&x.contains("left"),false);audit15Observe(events,"left-preview");touchNode(x->x.equals("Keep planning"),false);check(session().getString("history","bad").isEmpty()&&session().getInt("budget_used",-1)==0,"L15 cancelling a manual gate preview is free");
        audit15Action(fixture,67);touchNode(x->x.contains("Piece, row 2, column 2,"),false);audit15Observe(events,"switch-preview");touchNode(x->x.equals("Keep planning"),false);check(session().getString("history","bad").equals("67")&&session().getInt("budget_used",-1)==1,"L15 cancelling a switch preview leaves its piece and gates unchanged");
        audit15Action(fixture,4);check(has(x->x.startsWith("Gate closed")&&x.contains("up"),true),"L15 taking the legal switch early visibly closes the up gate");audit15Observe(events,"early-switch-up-closed");
        audit15Action(fixture,64);check(has(x->x.startsWith("Gate open")&&x.contains("up"),true),"L15 the closed up gate can be reopened with one visible productive action");audit15Observe(events,"up-reopened");
        click(x->x.contains("Undo"),true);check(session().getString("history","bad").equals("67,4")&&session().getInt("budget_used",-1)==2&&has(x->x.startsWith("Gate closed")&&x.contains("up"),true),"L15 Undo reverses the manual up opening and refunds it");
        click(x->x.contains("Undo"),true);check(session().getString("history","bad").equals("67")&&session().getInt("budget_used",-1)==1&&has(x->x.startsWith("Gate open")&&x.contains("up"),true),"L15 Undo restores the switch tile and its prior open up gate");click(x->x.contains("Undo"),true);check(session().getString("history","bad").isEmpty()&&session().getInt("budget_used",-1)==0,"L15 the gate/switch branch can be fully reversed without remaining move overhead");audit15Observe(events,"gate-branch-fully-undone");
        // Repeat a plausible blocked tap. Failed attempts have no Board action to undo.
        piece(2,2);check(session().getString("history","bad").isEmpty()&&session().getInt("budget_used",-1)==1,"L15 one blocked tile tap spends a move while recording no productive history");click(x->x.contains("Undo"),true);check(session().getString("history","bad").isEmpty()&&session().getInt("budget_used",-1)==1,"L15 Undo cannot refund a blocked attempt when no productive move exists");
        for(int i=0;i<4;i++)piece(2,2);check(session().getString("history","bad").isEmpty()&&session().getInt("budget_used",-1)==5,"L15 five blocked attempts spend five moves with the initial board still unchanged");SystemClock.sleep(2700);audit15Observe(events,"five-blocks-no-piece-moved");
        int hints=stock("hint");click(x->x.startsWith("Spark:"),true);check(stock("hint")==hints&&!has(x->x.equals("Use one charge"),true),"L15 Spark offers no usable hint charge after blocked attempts leave fewer than the necessary12 actions");audit15Observe(events,"five-blocks-spark-refused");
        restart();check(session().getInt("budget_used",-1)==0&&session().getString("history","bad").isEmpty(),"L15 free Restart clears the unrecoverable failed-attempt budget");
        click(x->x.startsWith("Spark:"),true);clickExact("Use one charge",false);check(stock("hint")==hints-1&&session().getInt("hint_id",-1)>=0&&session().getInt("budget_used",-1)==0&&session().getString("history","bad").isEmpty(),"L15 the same actual Spark works after Restart, without consuming a move or playing a tile");audit15Observe(events,"fresh-spark-works");
        restart();audit15Action(fixture,0);for(int i=0;i<15;i++)piece(2,2);check(session().getInt("budget_used",-1)==16&&session().getString("history","bad").equals("0"),"L15 repeated blocked taps genuinely exhaust all16 moves after one productive exit");audit15Observe(events,"zero-after-blocks");
        click(x->x.contains("Undo")||x.equals("Return to an open path"),true);check(session().getInt("budget_used",-1)==15&&session().getString("history","bad").isEmpty(),"L15 zero-move Undo restores the productive tile but refunds only one move, leaving15 failed-attempt charges");audit15Observe(events,"zero-undone-still-insufficient");restart();audit15Observe(events,"restart-restores-sixteen");
        // A legal player order intentionally differs from the pre-certified ordering.
        for(int id:new int[]{67,4,8,0,2,1,3,7,9,64,5,6})audit15Action(fixture,id);
        waitFor(x->x.startsWith("Lovely drift!")||x.equals("Lovely! You did it."),false,5000);check(save().getInt("stars_15",0)==3&&session().getInt("budget_used",-1)==12,"L15 also finishes with three stars in an alternate legal gate/switch-first order");check(coins()==initialCoins+55&&stock("hint")==initialHints-1,"L15 branch audit earns only its real victory reward and spends exactly the one confirmed Spark");audit15Observe(events,"alternate-order-won");home();
    }
    private void recovery()throws Exception{
        JSONObject f=fixtureNamed("recovery");int n=f.getInt("number");
        check(save().getInt("unlocked",save().getInt("unlocked_level",1))>=n||save().getInt("stars_"+n,0)>0,"Recovery level is legitimately unlocked by existing or tested progress");
        openJourney(n);restart();JSONArray prefix=f.getJSONArray("prefix");for(int i=0;i<prefix.length();i++)action(prefix.getJSONObject(i));
        String history=session().getString("history","");int last=f.getInt("undoAction");check(history.endsWith(","+last)||history.equals(Integer.toString(last)),"Recovery fixture ends with a genuine typed gate opening");
        JSONObject blocked=f.getJSONObject("blocked");int target=session().getInt("budget_base",f.getInt("base"))+session().getInt("budget_extra",0),iterations=0;
        int beforeCoins=coins(),beforeHints=stock("hint");
        while(session().getInt("budget_used",-1)<target){if(iterations++>64)throw new AssertionError("Blocked attempts did not reach zero allowance");piece(blocked);dismissPreviewIfPresent();}
        check(session().getInt("budget_used",-1)==target,"Actual blocked UI attempts reach exactly zero actions");capture("zero-actions-recovery-en");
        click(x->x.contains("Undo")||x.equals("Return to an open path"),true);
        String expected=history.contains(",")?history.substring(0,history.lastIndexOf(',')):"";
        check(session().getString("history","bad").equals(expected),"Undo remains usable at zero and removes exactly the last typed gate opening");
        check(session().getInt("budget_used",-1)==target-1,"Undo refunds exactly one productive action with no extra Undo cost");
        check(coins()==beforeCoins&&stock("hint")==beforeHints,"Free zero-action recovery spends neither currency nor hints");capture("zero-actions-undone-en");home();
    }
    private void powers()throws Exception{
        JSONObject f=fixtureNamed("powers");int n=f.getInt("number");
        check(save().getInt("stars_12",0)>0||save().getInt("unlocked",save().getInt("unlocked_level",1))>=12,"Flow is unlocked by actual Journey progress");
        check(stock("hint")>0&&stock("flow")>0,"Real earned hint and Flow charges exist; no stock is injected");
        openJourney(n);restart();JSONArray prefix=f.getJSONArray("prefix");for(int i=0;i<prefix.length();i++)action(prefix.getJSONObject(i));
        int hints=stock("hint"),used=session().getInt("budget_used",-1),gate=f.getInt("gateAction");String history=session().getString("history","");
        click(x->x.startsWith("Spark:"),true);clickExact("Use one charge",false);
        check(stock("hint")==hints-1,"Gate hint reserves and commits exactly one earned Spark");
        check(session().getInt("hint_id",-1)==gate,"The exact safe hint points to a typed gate opening");
        check(session().getInt("budget_used",-1)==used&&session().getString("history","").equals(history),"Showing a gate hint plays no action and spends no move");capture("hint-gate-en");
        click(x->x.startsWith("Spark:"),true);check(stock("hint")==hints-1&&!has(x->x.equals("Use one charge"),true),"An active gate hint cannot be bought a second time");
        restart();for(int i=0;i<prefix.length();i++)action(prefix.getJSONObject(i));
        int flow=stock("flow");used=session().getInt("budget_used",-1);history=session().getString("history","");
        click(x->x.startsWith("Flow:"),true);clickExact("Use one charge",false);
        check(stock("flow")==flow-1,"Flow consumes exactly one real earned charge");
        if(has(x->x.startsWith("Flow:"),true))click(x->x.startsWith("Flow:"),false);
        check(stock("flow")==flow-1,"Tapping an active Flow sequence cannot consume another charge");
        long end=SystemClock.uptimeMillis()+7000;while(session().getInt("flow_remaining",0)>0&&SystemClock.uptimeMillis()<end)SystemClock.sleep(90);
        JSONArray steps=f.getJSONArray("flow");String expected=history;for(int i=0;i<steps.length();i++)expected+=(expected.isEmpty()?"":",")+steps.getJSONObject(i).getInt("action");
        check(session().getInt("flow_remaining",-1)==0&&session().getString("history","bad").equals(expected),"Flow executes the certified gate-and-exit sequence exactly once");
        check(session().getInt("budget_used",-1)==used+steps.length(),"Every real gate opening and exit in Flow costs exactly one productive action");capture("flow-gate-sequence-en");home();
    }
    /** Exhaust actual earned Spark charges through legal hint confirmations; never inject an empty stock. */
    private void powerReturn()throws Exception{
        check(actualJourneyClears()>=13,"Empty-power return uses a legitimately unlocked level13");
        check(stock("hint")>=0&&stock("hint")<=20,"Bounded real hint stock is eligible for the empty-power route");
        openJourney(13);restart();int balance=coins(),spent=0;
        while(stock("hint")>0){int before=stock("hint");click(x->x.startsWith("Spark:"),true);clickExact("Use one charge",false);spent++;
            check(stock("hint")==before-1,"Preparing empty stock uses exactly one actual Spark charge");restart();}
        action(fixture(13).getJSONArray("steps").getJSONObject(0));
        String history=session().getString("history",""),snapshot=session().getString("run_id","" );int used=session().getInt("budget_used",-1),mode=session().getInt("mode",-1);
        click(x->x.startsWith("Spark:"),true);check(has(x->x.startsWith("Luma Drift · "),false)&&has(x->x.startsWith("Visual preview · no item granted"),false),"An actually empty gameplay power opens its normal item detail");
        check(!has(x->x.equals("Home")||x.equals("Shop"),true),"A detail opened from live play does not expose root-navigation footer actions");capture("ux-empty-power-from-play-en");
        assertVisibleTouchTarget(x->x.equalsIgnoreCase("Close"),"Empty power detail Close");touchNode(x->x.equalsIgnoreCase("Close"),false);
        waitFor(x->x.equals("Luma Drift · Play"),false,3000).recycle();
        check(coins()==balance&&stock("hint")==0&&session().getInt("budget_used",-2)==used&&session().getInt("mode",-2)==mode&&session().getString("history","bad").equals(history)&&session().getString("run_id","bad").equals(snapshot),"Close from the empty-power detail resumes the exact board/history/budget without granting stock or spending coins");capture("ux-empty-power-return-en");
        click(x->x.startsWith("Spark:"),true);android.view.WindowManager wm=(android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE);int rotation=wm.getDefaultDisplay().getRotation();RotationFrame viewport=observeRotationFrame();
        try{rotateAndAwait(UiAutomation.ROTATION_FREEZE_90,viewport,"Empty power detail landscape layout");capture("ux-empty-power-detail-landscape-en");}
        finally{rotateAndAwait(rotation,viewport,"Empty power detail portrait return");}
        back();waitFor(x->x.equals("Luma Drift · Play"),false,3000).recycle();
        check(coins()==balance&&stock("hint")==0&&session().getInt("budget_used",-2)==used&&session().getString("history","bad").equals(history),"System Back after detail rotation resumes live play without an accidental purchase or board reset");
        actions.put(new JSONObject().put("observation","actual-empty-power-return").put("chargesActuallyUsed",spent).put("history",history).put("mode",mode).put("budgetUsed",used).put("coins",balance));home();
    }
    /** Assertions read the real profile; no test code grants, clears or equips an item. */
    private static final class RotationFrame {
        final int rotation,pixelWidth,pixelHeight;final Rect viewBounds;
        RotationFrame(int rotation,int width,int height,Rect view){this.rotation=rotation;pixelWidth=width;pixelHeight=height;viewBounds=new Rect(view);}
    }
    private Rect actualGameBounds(){AccessibilityNodeInfo node=find(x->x.startsWith("Luma Drift · "),false);if(node==null)return null;Rect bounds=new Rect();node.getBoundsInScreen(bounds);node.recycle();return bounds;}
    private RotationFrame observeRotationFrame()throws Exception{
        long end=SystemClock.uptimeMillis()+5000;
        do{Rect view=actualGameBounds();Bitmap pixels=ui.takeScreenshot();if(view!=null&&pixels!=null){int width=pixels.getWidth(),height=pixels.getHeight();pixels.recycle();if(view.width()>0&&view.height()>0)return new RotationFrame(((android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation(),width,height,view);}else if(pixels!=null)pixels.recycle();SystemClock.sleep(70);}while(SystemClock.uptimeMillis()<end);
        throw new AssertionError("Cannot observe the actual game view and screenshot before rotation");
    }
    /** Wait for observed dimensions, not merely a same-named header from the old Activity. */
    private void awaitRotationFrame(int target,RotationFrame before,String caption)throws Exception{
        boolean swapped=Math.floorMod(target-before.rotation,2)==1;int expectedWidth=swapped?before.pixelHeight:before.pixelWidth,expectedHeight=swapped?before.pixelWidth:before.pixelHeight;
        long end=SystemClock.uptimeMillis()+9000;String stable=null,last="none";int consecutive=0;
        do{
            int rotation=((android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();Rect view=actualGameBounds();Bitmap pixels=ui.takeScreenshot();int width=-1,height=-1;
            if(pixels!=null){width=pixels.getWidth();height=pixels.getHeight();pixels.recycle();}
            last=rotation+":"+width+"x"+height+":"+(view==null?"no-game-view":view.toShortString());
            boolean exactReturn=target!=before.rotation||view!=null&&Math.abs(view.width()-before.viewBounds.width())<=3&&Math.abs(view.height()-before.viewBounds.height())<=3;
            boolean observed=rotation==target&&width==expectedWidth&&height==expectedHeight&&view!=null&&view.width()>0&&view.height()>0&&exactReturn;
            if(observed){consecutive=last.equals(stable)?consecutive+1:1;stable=last;if(consecutive>=2){check(true,caption+": actual rotation "+target+", PNG "+width+"×"+height+", game view "+view.toShortString());actions.put(new JSONObject().put("observation","rotation-layout-barrier").put("caption",caption).put("rotation",rotation).put("pngWidth",width).put("pngHeight",height).put("gameBounds",view.toShortString()).put("consecutiveStableObservations",consecutive));return;}}
            else{consecutive=0;stable=null;}SystemClock.sleep(80);
        }while(SystemClock.uptimeMillis()<end);
        throw new AssertionError(caption+" did not reach observed rotation/layout; expected "+target+":"+expectedWidth+"x"+expectedHeight+", last="+last);
    }
    private void rotateAndAwait(int target,RotationFrame before,String caption)throws Exception{check(ui.setRotation(target),caption+": SDK rotation accepted");awaitRotationFrame(target,before,caption);}

    private Map<String,Object> presentationState(boolean omitGoal){
        Map<String,Object> values=new TreeMap<>();
        for(Map.Entry<String,?> entry:save().getAll().entrySet())if(!omitGoal||!entry.getKey().equals("collection_goal_v110"))values.put("save/"+entry.getKey(),entry.getValue());
        for(Map.Entry<String,?> entry:getTargetContext().getSharedPreferences("luma_drift_meta_v1",0).getAll().entrySet())values.put("meta/"+entry.getKey(),entry.getValue());
        return values;
    }
    private boolean selectedControl(String label){AccessibilityNodeInfo node=find(x->x.equals(label),true);if(node==null)return false;boolean result=node.isSelected();node.recycle();return result;}
    private void expectParent(String expected)throws Exception{
        for(String label:new String[]{"Home","Map","Events","Inventory","Shop"})check(selectedControl(label)==label.equals(expected),"Navigation marks only its actual parent: "+expected+" / "+label);
    }
    private void seekText(Predicate<String> target)throws Exception{for(int i=0;i<18;i++){if(has(target,false))return;scroll(true);}throw new AssertionError("Missing visible semantic text: "+labels());}
    private void openObjectives()throws Exception{home();top();click(x->x.matches("[0-9]+ ready"),false);waitFor(x->x.equals("Today"),true,3000).recycle();}
    private void openCompanion()throws Exception{home();top();if(has(x->x.equals("Dress up"),true))clickExact("Dress up",false);else clickExact("View my companion",false);waitFor(x->x.equals("Luma Drift · My companion"),false,3000).recycle();}
    private void openStory()throws Exception{home();top();click(x->x.startsWith("Prism journal,"),true);waitFor(x->x.equals("Luma Drift · Prism journal"),false,3000).recycle();}
    private int actualJourneyClears(){int count=0;for(int level=1;level<=1000;level++)if(save().getInt("stars_"+level,0)>0)count++;return count;}
    private int claimedFragments(int first,int last){int result=0;for(int guardian=first;guardian<=last;guardian++)if(save().getBoolean("guardian_v19_"+guardian,false))result++;return result;}
    /** Real SDK pixels protect against a selected semantic node whose flat fill is transparent. */
    private void assertMintSelection(String label)throws Exception{
        top();seekControl(x->x.equals(label));Rect bounds=new Rect();boolean selected=false,fullyVisible=false;
        for(int attempt=0;attempt<18;attempt++){
            AccessibilityNodeInfo node=find(x->x.equals(label),true);if(node==null){scroll(true);continue;}node.getBoundsInScreen(bounds);selected=node.isSelected();node.recycle();
            Rect game=actualGameBounds();int visibleBottom=game==null?Integer.MAX_VALUE:game.bottom;AccessibilityNodeInfo homeNode=find(x->x.equals("Home"),true);
            if(homeNode!=null){Rect footer=new Rect();homeNode.getBoundsInScreen(footer);homeNode.recycle();visibleBottom=Math.min(visibleBottom,footer.top);}
            if(bounds.top>=0&&bounds.bottom<=visibleBottom){fullyVisible=true;break;}scroll(bounds.bottom>visibleBottom);
        }
        if(!fullyVisible)throw new AssertionError("Cannot expose entire selected control above the native footer: "+label+" "+bounds.toShortString());
        Bitmap pixels=ui.takeScreenshot();if(pixels==null)throw new AssertionError("No native screenshot for selected fill: "+label);
        int width=pixels.getWidth(),height=pixels.getHeight(),sampleX=Math.round(bounds.exactCenterX()),sampleY=Math.round(bounds.top+bounds.height()*.20f),actual;
        try{
            if(bounds.left<0||bounds.top<0||bounds.right>width||bounds.bottom>height||bounds.width()<=0||bounds.height()<=0)throw new AssertionError("Selected control must be fully visible before pixel read: "+label+" "+bounds.toShortString()+" / "+width+"x"+height);
            if(sampleX<0||sampleX>=width||sampleY<0||sampleY>=height)throw new AssertionError("Observed native sample falls outside screenshot");
            actual=pixels.getPixel(sampleX,sampleY);
        }finally{pixels.recycle();}
        int expected=0xffe1fff2;boolean opaque=(actual>>>24)==255,mint=Math.abs(((actual>>>16)&255)-225)<=3&&Math.abs(((actual>>>8)&255)-255)<=3&&Math.abs((actual&255)-242)<=3;
        actions.put(new JSONObject().put("observation","native-flat-selection-pixel").put("label",label).put("nativeSelected",selected).put("observedBounds",bounds.toShortString()).put("sampleX",sampleX).put("sampleY",sampleY).put("yFraction",.20).put("actualArgb",String.format(Locale.ROOT,"%08x",actual)).put("expectedArgb",String.format(Locale.ROOT,"%08x",expected)).put("rgbTolerance",3).put("pngWidth",width).put("pngHeight",height).put("input","UiAutomation.takeScreenshot; unmodified Bitmap.getPixel"));
        check(selected,"Flat selection is semantically selected: "+label);check(opaque&&mint,"Flat selection has actual opaque mint pixels above its text: "+label+" actual="+String.format(Locale.ROOT,"%08x",actual));
    }
    private void assertMintSelection(Predicate<String> wanted)throws Exception{
        top();seekControl(wanted);AccessibilityNodeInfo node=waitFor(wanted,true,3000);String observedLabel=name(node);node.recycle();assertMintSelection(observedLabel);
    }

    /** A real footer control must remain a full touch target above the fixed native navigation. */
    private void assertFooterTarget(String label)throws Exception{
        Predicate<String> wanted=x->x.equals(label)||x.equals(label+"  →");Rect bounds=new Rect(),footer=new Rect();String observedLabel="",previous="";int stable=0;
        // The SDK scroll provider can acknowledge an already clamped scroll. Observe stable bounds.
        for(int attempt=0;attempt<24;attempt++){
            scroll(true);AccessibilityNodeInfo node=find(wanted,true);
            if(node==null){stable=0;continue;}node.getBoundsInScreen(bounds);observedLabel=name(node);node.recycle();
            String now=bounds.toShortString();stable=now.equals(previous)?stable+1:1;previous=now;if(stable>=3)break;
        }
        AccessibilityNodeInfo node=waitFor(wanted,true,3000);node.getBoundsInScreen(bounds);observedLabel=name(node);boolean enabled=node.isEnabled()&&node.isClickable();node.recycle();
        AccessibilityNodeInfo homeNode=waitFor(x->x.equals("Home"),true,3000);homeNode.getBoundsInScreen(footer);homeNode.recycle();
        Rect game=actualGameBounds();float density=getTargetContext().getResources().getDisplayMetrics().density,dpHeight=bounds.height()/density;
        actions.put(new JSONObject().put("observation","native-footer-target").put("label",observedLabel).put("observedBounds",bounds.toShortString()).put("observedFooterBounds",footer.toShortString()).put("heightDp",dpHeight).put("displayDensity",density).put("stableBottomObservations",stable).put("input","AccessibilityNodeProvider SDK scroll; observed native node bounds"));
        check(stable>=3,"Footer target reaches stable observed bottom position: "+label);
        check(enabled&&dpHeight>=47f,"Footer target retains at least47dp of actual visible native height: "+label+" actual="+dpHeight);
        check(game!=null&&bounds.top>=game.top&&bounds.left>=game.left&&bounds.right<=game.right&&bounds.bottom<=footer.top,"Entire footer target stays above fixed native navigation: "+label);
        String slug=label.equals("›")?"collection-next-page":label.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-");capture("footer-target-"+slug+"-en");
    }

    private void hubV110()throws Exception{
        home();Map<String,Object> before=presentationState(false);assertFooterTarget("Enter a challenge");check(presentationState(false).equals(before),"Scrolling Home footer cannot claim or spend anything");openObjectives();expectParent("Events");
        String[] tabs={"Today","This week","Journey","Ready to collect"};
        for(String tab:tabs){top();touchNode(x->x.equals(tab),false);for(String other:tabs)check(selectedControl(other)==other.equals(tab),"Only requested objective tab is selected: "+tab+" / "+other);expectParent("Events");check(presentationState(false).equals(before),"Opening objective tab never auto-claims currency, stock, receipts or XP: "+tab);capture("objectives-"+tab.toLowerCase(Locale.ROOT).replace(' ','-')+"-en");}
        // Reproduce a deep list scroll before changing tabs: stale rows must not intercept it.
        top();clickExact("Journey",false);for(int i=0;i<3;i++)scroll(true);top();touchNode(x->x.equals("This week"),false);check(selectedControl("This week"),"Reward tab stays physically reachable after a deeply scrolled list");check(presentationState(false).equals(before),"Tab switching after deep scroll does not trigger a hidden claim");
        back();waitFor(x->x.equals("Luma Drift · Home"),false,3000).recycle();expectParent("Home");check(presentationState(false).equals(before),"Objectives Back returns to its caller without a gift");capture("hub-home-en");
    }
    private void avatarV110()throws Exception{
        openCompanion();Map<String,Object> before=presentationState(false);Map<String,Object> withoutGoal=presentationState(true);expectParent("Inventory");
        top();click(x->x.startsWith("Hat, "),false);clickExact("Appearance",true);top();clickExact("›",true);top();seekControl(x->x.startsWith("Hat · Cupid,"));touchNode(x->x.startsWith("Hat · Cupid,"),false);
        check(has(x->x.startsWith("Visual preview · no item granted"),false),"Avatar offer exposes a real companion preview with an explicit no-grant label");check(presentationState(false).equals(before),"Preview grants no cosmetic and changes no coins, equipment, stock, receipt or XP");capture("avatar-preview-cupid-en");clickExact("Close",false);check(presentationState(false).equals(before),"Closing a preview leaves the profile unchanged");
        // Pin through the actual dialog. A pre-existing pin on this item is toggled off then back on.
        top();seekControl(x->x.startsWith("Hat · Cupid,"));touchNode(x->x.startsWith("Hat · Cupid,"),false);
        if(has(x->x.equals("Remove this goal"),true)){clickExact("Remove this goal",false);check(presentationState(true).equals(withoutGoal),"Removing a pin changes only the chosen-goal key");top();seekControl(x->x.startsWith("Hat · Cupid,"));touchNode(x->x.startsWith("Hat · Cupid,"),false);}
        clickExact("Set as my goal",false);check(save().getString("collection_goal_v110","").equals("cosmetic:hat_cupid"),"Explicit native pin stores the real catalog identity");check(presentationState(true).equals(withoutGoal),"Pinning changes neither ownership nor balance, equipment, stock, receipts or XP");
        home();top();seekText(x->x.contains("My chosen goal")&&x.contains("Hat · Cupid"));capture("hub-pinned-goal-en");check(presentationState(true).equals(withoutGoal),"Home reads the pinned goal without claiming it");clickExact("Preview",true);check(has(x->x.startsWith("Visual preview · no item granted"),false),"Pinned Home goal opens the same explicit preview");check(presentationState(true).equals(withoutGoal),"Pinned goal preview still does not grant or equip");clickExact("Close",false);
        openCompanion();top();clickExact("Equipment",true);check(selectedControl("Equipment"),"Avatar separates equipment from cosmetic appearance");assertMintSelection("Equipment");check(presentationState(true).equals(withoutGoal),"Equipment tab is read-only before an explicit action");expectParent("Inventory");capture("avatar-equipment-en");top();clickExact("Appearance",true);check(selectedControl("Appearance"),"Appearance remains its distinct selected tab");assertOrganicSelection("Appearance");
        android.view.WindowManager wm=(android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE);int rotation=wm.getDefaultDisplay().getRotation();RotationFrame viewport=observeRotationFrame();
        try{rotateAndAwait(UiAutomation.ROTATION_FREEZE_90,viewport,"Landscape layout");waitFor(x->x.equals("Luma Drift · My companion"),false,5000).recycle();check(presentationState(true).equals(withoutGoal),"Avatar recreation does not apply a preview or grant an item");}finally{rotateAndAwait(rotation,viewport,"Portrait return layout");}
        waitFor(x->x.equals("Luma Drift · My companion"),false,5000).recycle();expectParent("Inventory");check(presentationState(true).equals(withoutGoal),"Avatar portrait return keeps economic and equipped state");capture("avatar-slots-en");assertFooterTarget("Open inventory");touchNode(x->x.equals("Open inventory  →"),false);waitFor(x->x.equals("Luma Drift · Collection")||x.equals("Luma Drift · Inventory"),false,3000).recycle();top();clickExact("Companions",false);clickExact("All",false);assertFooterTarget("›");check(presentationState(true).equals(withoutGoal),"Avatar and Collection footer inspection changes no ownership, equipment or currency");home();
    }
    private void storyV110()throws Exception{
        openStory();Map<String,Object> before=presentationState(false);int clears=actualJourneyClears(),allFragments=claimedFragments(1,100);expectParent("Map");
        for(int chapter=1;chapter<=10;chapter++){final int number=chapter;top();click(x->x.startsWith("Chapter "+number+","),false);check(selectedControlLabel(x->x.startsWith("Chapter "+number+",")),"Notebook selects actual chapter "+number);assertMintSelection(x->x.startsWith("Chapter "+number+","));
            check(has(x->x.contains("Story: "+allFragments+" / 100"),false),"Chapter narrative exposes the actual total claimed fragments");int first=(chapter-1)*100+1,last=chapter*100,progress=Math.max(0,Math.min(100,clears-(chapter-1)*100));seekText(x->x.equals("Journey "+first+"–"+last+", "+progress+" / 100"));int fragments=claimedFragments((chapter-1)*10+1,chapter*10);seekText(x->x.equals("Prism guardians, Story: "+fragments+" / 10"));check(presentationState(false).equals(before),"Reading chapter "+number+" grants no gift, receipt, clear, equipment or XP");if(chapter==1||chapter==10)capture("prism-chapter-"+chapter+"-en");
        }
        top();click(x->x.startsWith("Chapter 10,"),false);android.view.WindowManager wm=(android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE);int rotation=wm.getDefaultDisplay().getRotation();RotationFrame viewport=observeRotationFrame();
        try{rotateAndAwait(UiAutomation.ROTATION_FREEZE_90,viewport,"Landscape layout");waitFor(x->x.equals("Luma Drift · Prism journal"),false,5000).recycle();seekControl(x->x.startsWith("Chapter 10,"));check(selectedControlLabel(x->x.startsWith("Chapter 10,")),"Selected narrative chapter survives real activity recreation");}finally{rotateAndAwait(rotation,viewport,"Portrait return layout");}
        waitFor(x->x.equals("Luma Drift · Prism journal"),false,5000).recycle();check(presentationState(false).equals(before),"Story recreation never claims a fragment");
        assertFooterTarget("Prism guardians");clickExact("Prism guardians",true);expectParent("Map");int locked=clears/10+1;
        if(locked<=100){top();click(x->x.startsWith("Guardian "+locked+","),true);check(has(x->x.equals("Luma Drift · Prism guardians"),false),"Locked Guardian does not open an encounter or bypass ten-clear threshold");check(presentationState(false).equals(before),"Inspecting a locked Guardian cannot create its receipt");capture("prism-locked-guardian-en");}
        back();waitFor(x->x.equals("Luma Drift · Prism journal"),false,3000).recycle();expectParent("Map");back();waitFor(x->x.equals("Luma Drift · Home"),false,3000).recycle();expectParent("Home");check(presentationState(false).equals(before),"Story/list parent navigation leaves all progression and rewards untouched");
    }
    private boolean selectedControlLabel(Predicate<String> wanted){AccessibilityNodeInfo node=find(wanted,true);if(node==null)return false;boolean selected=node.isSelected();node.recycle();return selected;}
    /** Locate a row action by its real semantic row and native bounds, never by a guessed coordinate. */
    private void touchRowAction(Predicate<String> rowName,String actionLabel)throws Exception{
        for(int attempt=0;attempt<20;attempt++){
            AccessibilityNodeInfo row=find(rowName,false);Rect rowBounds=new Rect();if(row!=null){row.getBoundsInScreen(rowBounds);row.recycle();Rect best=null;int distance=Integer.MAX_VALUE;
                for(AccessibilityNodeInfo node:nodes()){Rect box=new Rect();node.getBoundsInScreen(box);int gap=box.centerY()-rowBounds.bottom;if(node.isClickable()&&node.isEnabled()&&node.isVisibleToUser()&&name(node).equals(actionLabel)&&gap>=0&&gap<rowBounds.height()*1.25&&box.centerX()>=rowBounds.left&&box.centerX()<=rowBounds.right&&gap<distance){best=box;distance=gap;}node.recycle();}
                if(best!=null){long down=SystemClock.uptimeMillis();injectTouch(down,MotionEvent.ACTION_DOWN,best.exactCenterX(),best.exactCenterY());SystemClock.sleep(30);injectTouch(down,MotionEvent.ACTION_UP,best.exactCenterX(),best.exactCenterY());SystemClock.sleep(200);actions.put(new JSONObject().put("gesture","tap-semantic-row-action").put("label",actionLabel).put("observedRow",rowBounds.toShortString()).put("observedActionBounds",best.toShortString()).put("input","UiAutomation.injectInputEvent; touchscreen"));return;}
            }scroll(true);
        }throw new AssertionError("No visible action "+actionLabel+" for semantic row: "+labels());
    }

    private void rewards()throws Exception{
        openObjectives();top();clickExact("Journey",false);check(selectedControl("Journey"),"Journey rewards are their actual selected tab");
        int milestone=5;while(save().contains("power_loot_v1_"+milestone)||save().contains("power_loot_v19_"+milestone))milestone+=5;
        check(milestone<=1000&&actualJourneyClears()>=milestone,"Gift is earned by real existing/tested Journey clears");
        List<String> eligible=new ArrayList<>();String[] ids={"hint","vision","flow","shield","focus","trail"};int[] unlock={1,5,12,25,45,75};for(int i=0;i<6;i++)if(unlock[i]<=milestone+1)eligible.add(ids[i]);String gift=eligible.get((milestone/5-1)%eligible.size());int before=stock(gift),balance=coins();
        touchNode(x->x.startsWith("Collect ")&&x.endsWith(" · ×1"),false);check(stock(gift)==before+1&&save().getString("power_loot_v19_"+milestone,"").equals(gift)&&coins()==balance,"Visible named gift grants its stable preview item once without currency");capture("rewards-first-gift-en");
        String[] names={"Spark","Vision","Flow","Shield","Focus","Trail"};
        for(int i=0;i<ids.length;i++){final String name=names[i],id=ids[i];top();
            if(save().getInt("unlocked",1)<unlock[i]){check(!save().getBoolean("power_trial_v19_"+id,false),"Locked power does not acquire a sample receipt");continue;}
            if(!save().getBoolean("power_trial_v19_"+id,false)){int stockBefore=stock(id),coinsBefore=coins();touchRowAction(x->x.equals(name+", Try "+name+" · free ×1"),"Collect reward");check(stock(id)==stockBefore+1&&coins()==coinsBefore&&save().getBoolean("power_trial_v19_"+id,false),"Explicit semantic-row claim grants one real sample: "+id);}
            int afterStock=stock(id),afterCoins=coins();top();touchRowAction(x->x.equals(name+", Collected"),"Collected");check(save().getBoolean("power_trial_v19_"+id,false)&&stock(id)==afterStock&&coins()==afterCoins,"Collected power sample repeat changes no stock or currency: "+id);
        }top();capture("rewards-trials-claimed-en");home();
    }

    private void styles()throws Exception{
        home();clickExact("Visual styles",true);top();int balance=coins();String companion=save().getString("character","nova");
        String[] ids={"plush_classic","cyberpunk_neon","retro_arcade","modern_pixel","sakura","ocean","space","steampunk","candy_cloud","halloween","valentine","ice"};
        String[] names={"Plush classic","Cyberpunk neon","Retro arcade","Modern pixel","Sakura","Ocean","Space","Steampunk","Candy cloud","Halloween","Valentine","Ice crystal"};
        for(int i=0;i<ids.length;i++){final String label=names[i];top();click(x->x.startsWith(label+","),true);
            check(save().getString("avatar_style_v19","plush_classic").equals(ids[i]),"Actual style selection persists "+ids[i]);
            check(coins()==balance&&save().getString("character","nova").equals(companion),"Free style keeps currency and companion ownership unchanged");
            if(i==0||i==1||i==2||i==11)capture("avatar-style-"+ids[i]+"-en");
        }
        home();clickExact("Visual styles",true);top();click(x->x.startsWith("Cyberpunk neon,"),true);home();clickExact("Visual styles",true);top();
        check(has(x->x.equals("Cyberpunk neon, Active style"),true)&&save().getString("avatar_style_v19","").equals("cyberpunk_neon"),"Leaving and reopening studio retains selected free style");capture("avatar-style-persisted-en");
        android.view.WindowManager wm=(android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE);int original=wm.getDefaultDisplay().getRotation();RotationFrame viewport=observeRotationFrame();
        try{rotateAndAwait(UiAutomation.ROTATION_FREEZE_90,viewport,"Landscape layout");check(has(x->x.equals("Luma Drift · Avatar studio"),false)&&save().getString("avatar_style_v19","").equals("cyberpunk_neon"),"Studio screen and selected style survive landscape activity recreation");}
        finally{rotateAndAwait(original,viewport,"Portrait return layout");}
        check(has(x->x.equals("Cyberpunk neon, Active style"),true),"Studio returns to its actual selected-style control after portrait restoration");home();
    }
    private String guardianState(){return session().getString("guardian_v19_session","");}
    private void openGuardian(int number)throws Exception{openStory();clickExact("Prism guardians",true);
        if(!guardianListRotationChecked){android.view.WindowManager wm=(android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE);int original=wm.getDefaultDisplay().getRotation();RotationFrame viewport=observeRotationFrame();
            try{rotateAndAwait(UiAutomation.ROTATION_FREEZE_90,viewport,"Landscape layout");check(has(x->x.equals("Luma Drift · Prism guardians"),false),"Guardian list stays on its own screen through activity recreation");}
            finally{rotateAndAwait(original,viewport,"Portrait return layout");}
            waitFor(x->x.startsWith("Guardian "+number+","),true,5000).recycle();
            check(wm.getDefaultDisplay().getRotation()==original,"Guardian list returns to the requested portrait orientation and visible encounter controls");guardianListRotationChecked=true;
        }
        click(x->x.startsWith("Guardian "+number+","),true);
    }
    private void guardianStep(JSONObject step)throws Exception{
        int id=step.getInt("id");top();Rect before=touchNode(x->x.startsWith("Piece "+(id+1)+" · "),false);
        check(guardianState().equals(step.getString("save")),"Guardian action "+id+" records exactly the source-certified layer/step state");
        if(step.getBoolean("stepped")){AccessibilityNodeInfo target=waitFor(x->x.startsWith("Piece "+(id+1)+" · "),true,3000);Rect after=new Rect();target.getBoundsInScreen(after);target.recycle();
            float dx=(step.getInt("afterCol")-step.getInt("col"))*before.width(),dy=(step.getInt("afterRow")-step.getInt("row"))*before.height();
            check(Math.abs(after.exactCenterX()-before.exactCenterX()-dx)<=3&&Math.abs(after.exactCenterY()-before.exactCenterY()-dy)<=3,"A native cellwise action visibly moves exactly one observed grid cell");
        }
        if(step.getBoolean("removed"))check(!has(x->x.startsWith("Piece "+(id+1)+" · "),true),"A guardian exit removes its actual accessible piece");
        if(step.getBoolean("peeled")){int layersAfter=step.getInt("layersAfter");check(has(x->x.startsWith("Piece "+(id+1)+" · ")&&x.contains(" · "+layersAfter+" layer(s)"),true),"A real first hit leaves one fewer visible layer and the piece present");}
    }
    private void guardians()throws Exception{
        for(int number:new int[]{1,5}){
            JSONObject fixture=fixtureNamed("guardian"+number);openGuardian(number);
            if(!guardianState().equals(fixture.getString("initial"))){clickExact("Restart · Free",true);top();}
            check(guardianState().equals(fixture.getString("initial")),"Guardian "+number+" starts or visibly restarts with its real authored layers");capture("guardian-"+number+"-start-en");
            JSONArray plan=fixture.getJSONArray("steps");String initial=guardianState();int balance=coins();guardianStep(plan.getJSONObject(0));
            check(plan.getJSONObject(0).getBoolean("peeled"),"First guardian action really peels a double layer");capture("guardian-"+number+"-peeled-en");
            clickExact("Undo is always free",true);check(guardianState().equals(initial)&&coins()==balance,"Guardian free Undo restores the layer and refunds its action without cost");
            guardianStep(plan.getJSONObject(0));boolean resumed=false;
            for(int index=1;index<plan.length();index++){
                JSONObject step=plan.getJSONObject(index);guardianStep(step);
                if(number==5&&!resumed&&step.getBoolean("stepped")){
                    int id=step.getInt("id");check(has(x->x.startsWith("Piece "+(id+1)+" · ")&&x.endsWith(" · one cell"),true),"Cellwise guardian piece remains present after moving one cell");
                    String saved=guardianState();capture("guardian-5-one-cell-en");clickExact("Prism guardians",true);click(x->x.startsWith("Guardian 5,"),true);check(guardianState().equals(saved),"Leaving and reopening a partial one-cell encounter restores exact history, positions and used actions");
                    AccessibilityNodeInfo observed=waitFor(x->x.startsWith("Piece "+(id+1)+" · "),true,3000);Rect beforeRotation=new Rect();observed.getBoundsInScreen(beforeRotation);observed.recycle();
                    android.view.WindowManager wm=(android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE);int rotation=wm.getDefaultDisplay().getRotation();RotationFrame viewport=observeRotationFrame();
                    try{rotateAndAwait(UiAutomation.ROTATION_FREEZE_90,viewport,"Landscape layout");check(guardianState().equals(saved)&&has(x->x.equals("Luma Drift · Prism guardians"),false),"Guardian partial save and active encounter screen survive real activity recreation");}
                    finally{rotateAndAwait(rotation,viewport,"Portrait return layout");}
                    AccessibilityNodeInfo restored=waitFor(x->x.startsWith("Piece "+(id+1)+" · "),true,3000);Rect afterRotation=new Rect();restored.getBoundsInScreen(afterRotation);restored.recycle();
                    check(guardianState().equals(saved)&&Math.abs(afterRotation.exactCenterX()-beforeRotation.exactCenterX())<=3&&Math.abs(afterRotation.exactCenterY()-beforeRotation.exactCenterY())<=3,"Portrait returns to the exact visible guardian piece position and history");resumed=true;
                }
            }
            if(number==5)check(resumed,"Guardian5 native run actually includes a persistent one-cell step");
            String solvedSave=guardianState();capture("guardian-"+number+"-cleared-en");clickExact("Prism guardians",true);click(x->x.startsWith("Guardian "+number+","),true);
            check(guardianState().equals(solvedSave),"Solved unclaimed guardian reopens its exact completed save without another playthrough");
            seekControl(x->x.equals("Collect the Prism fragment · +100 LumaCoins")||x.equals("Prism fragment restored"));
            check(has(x->x.equals("Collect the Prism fragment · +100 LumaCoins")||x.equals("Prism fragment restored"),true),"Guardian reward is reachable through normal page scrolling");int before=coins();String receipt="guardian_v19_"+number;boolean already=save().getBoolean(receipt,false);
            click(x->x.equals("Collect the Prism fragment · +100 LumaCoins")||x.equals("Prism fragment restored"),true);
            check(save().getBoolean(receipt,false)&&coins()==before+(already?0:100),"Guardian "+number+" explicit fragment reward credits exactly once");
            int claimed=coins();clickExact("Prism fragment restored",true);check(coins()==claimed,"Repeated guardian reward does not duplicate100 LumaCoins");capture("guardian-"+number+"-reward-en");home();
        }
    }
    /** Test-owned handshake only; the host changes directory permissions, never profile values. */
    private void requestStorageFault(String phase,String stage)throws Exception{
        if(!"true".equals(args.getString("storageFaultInfrastructure","false")))throw new AssertionError("Explicit owned QA infrastructure fault controller required");
        String request="guardian3-"+phase+"-"+stage;File temp=new File(output,"storage-fault-request.tmp"),target=new File(output,"storage-fault-request.json");
        try(OutputStream file=new FileOutputStream(temp)){file.write(new JSONObject().put("stage",stage).put("request",request).toString().getBytes(StandardCharsets.UTF_8));}
        if(!temp.renameTo(target))throw new IOException("Cannot publish test-only infrastructure handshake");
        long until=SystemClock.uptimeMillis()+20000;File ack=new File(output,"storage-fault-ack.json");
        while(SystemClock.uptimeMillis()<until){
            if(ack.isFile())try(InputStream in=new FileInputStream(ack);ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
                byte[] buffer=new byte[1024];int n;while((n=in.read(buffer))!=-1)bytes.write(buffer,0,n);JSONObject value=new JSONObject(bytes.toString("UTF-8"));
                if(request.equals(value.optString("request"))&&stage.equals(value.optString("stage"))&&value.optBoolean("ok")){check(true,"Owned QA host acknowledges storage permission "+stage);return;}
            }catch(IOException|org.json.JSONException incomplete){/* A partial acknowledgement is retried without touching app data. */}
            SystemClock.sleep(100);
        }
        throw new AssertionError("Owned QA storage-fault handshake timed out: "+stage);
    }
    private String diskPreference(String fileName,String key)throws Exception{
        File file=new File(getTargetContext().getDataDir(),"shared_prefs/"+fileName+".xml");
        try(InputStream input=new FileInputStream(file)){
            org.xmlpull.v1.XmlPullParser parser=android.util.Xml.newPullParser();parser.setInput(input,"UTF-8");
            for(int event=parser.getEventType();event!=org.xmlpull.v1.XmlPullParser.END_DOCUMENT;event=parser.next())
                if(event==org.xmlpull.v1.XmlPullParser.START_TAG&&key.equals(parser.getAttributeValue(null,"name")))return "string".equals(parser.getName())?parser.nextText():parser.getAttributeValue(null,"value");
        }
        return "";
    }
    private String guardianDiskState()throws Exception{return diskPreference("luma_session","guardian_v19_session");}
    private int diskCoins()throws Exception{return Integer.parseInt(diskPreference("luma_drift_save_v1","coins"));}
    private void physicalRetry()throws Exception{
        AccessibilityNodeInfo retry=waitFor(x->x.equals("Retry")||x.equals("Try again"),true,3000);Rect bounds=new Rect();retry.getBoundsInScreen(bounds);String label=name(retry);retry.recycle();
        float x=bounds.left+bounds.width()*.6f,y=bounds.top+bounds.height()*.5f;long down=SystemClock.uptimeMillis();
        injectTouch(down,MotionEvent.ACTION_DOWN,x,y);SystemClock.sleep(30);injectTouch(down,MotionEvent.ACTION_UP,x,y);SystemClock.sleep(250);
        actions.put(new JSONObject().put("gesture","tap-storage-retry").put("observedLabel",label).put("observedBounds",bounds.toShortString()).put("fractionX",.6).put("fractionY",.5).put("touchX",x).put("touchY",y).put("input","UiAutomation.injectInputEvent; touchscreen"));
    }
    private void guardianStorageFault()throws Exception{
        check(save().getInt("unlocked",1)>=31||save().getInt("stars_30",0)>0,"Storage fault Guardian3 is unlocked by at least30 real campaign clears");
        JSONObject fixture=fixtureNamed("guardian3");openGuardian(3);
        if(!guardianState().equals(fixture.getString("initial"))){clickExact("Restart · Free",true);top();}
        check(guardianState().equals(fixture.getString("initial")),"Storage fault starts the real authored Guardian3 without seeding a profile");
        JSONObject first=fixture.getJSONArray("steps").getJSONObject(0);check(first.getBoolean("peeled"),"Fault action is a source-certified real layer peel");
        String before=guardianDiskState();check(before.equals(fixture.getString("initial")),"Guardian3 initial state is actually persisted before the infrastructure fault");
        boolean locked=false;
        try{
            requestStorageFault("session","lock");locked=true;top();touchNode(x->x.startsWith("Piece "+(first.optInt("id")+1)+" · "),false);
            waitFor(x->x.equals("Retry")||x.equals("Try again"),true,4000).recycle();
            check(guardianState().equals(first.getString("save")),"Failed write retains the exact pending layer peel in memory for explicit retry");
            check(guardianDiskState().equals(before),"Actual filesystem write failure leaves the previous guardian disk state intact");capture("guardian-storage-fault-blocked-en");
            check(ui.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK),"SDK sends actual Back while the guardian write is pending");SystemClock.sleep(200);check(has(x->x.equals("Retry")||x.equals("Try again"),true)&&guardianState().equals(first.getString("save"))&&guardianDiskState().equals(before),"Back cannot abandon the exact pending guardian action while its disk write is blocked");
        }finally{if(locked)requestStorageFault("session","restore");}
        physicalRetry();
        check(!has(v->v.equals("Retry")||v.equals("Try again"),true)&&has(v->v.startsWith("Piece "+(first.optInt("id")+1)+" · "),true),"Physical Retry touch exits the blocked screen despite the old underlying piece region");
        check(guardianState().equals(first.getString("save"))&&guardianDiskState().equals(first.getString("save")),"Retry persists the exact single pending layer peel without playing it twice");capture("guardian-storage-fault-recovered-en");
        JSONArray plan=fixture.getJSONArray("steps");for(int index=1;index<plan.length();index++)guardianStep(plan.getJSONObject(index));
        String receipt="guardian_v19_3";check(!save().getBoolean(receipt,false),"Reward fault uses an actually unclaimed Guardian3, without resetting any receipt");
        seekControl(x->x.equals("Collect the Prism fragment · +100 LumaCoins"));int balance=coins();check(diskCoins()==balance&&!"true".equals(diskPreference("luma_drift_save_v1",receipt)),"Before reward fault the actual coin balance is durable and Guardian3 has no disk receipt");
        boolean rewardLocked=false;
        try{
            requestStorageFault("reward","lock");rewardLocked=true;touchNode(x->x.equals("Collect the Prism fragment · +100 LumaCoins"),false);waitFor(x->x.equals("Retry")||x.equals("Try again"),true,4000).recycle();
            check(coins()==balance+100&&save().getBoolean(receipt,false),"Failed reward write retains exactly100 pending coins and the same receipt in memory");
            check(diskCoins()==balance&&!"true".equals(diskPreference("luma_drift_save_v1",receipt)),"Failed reward write leaves both the previous disk balance and absent receipt intact");
            physicalRetry();check(has(x->x.equals("Retry")||x.equals("Try again"),true)&&coins()==balance+100&&save().getBoolean(receipt,false),"Retry while storage is still locked stays blocked without duplicating the pending reward");
            int rotation=((android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();RotationFrame viewport=observeRotationFrame();
            try{rotateAndAwait(UiAutomation.ROTATION_FREEZE_90,viewport,"Landscape layout");waitFor(x->x.equals("Retry")||x.equals("Try again"),true,5000).recycle();check(true,"Pending reward remains an actual retry screen through landscape activity recreation");}
            finally{rotateAndAwait(rotation,viewport,"Portrait return layout");}
            waitFor(x->x.equals("Retry")||x.equals("Try again"),true,5000).recycle();
            check(has(x->x.equals("Retry")||x.equals("Try again"),true)&&coins()==balance+100&&save().getBoolean(receipt,false)&&diskCoins()==balance&&!"true".equals(diskPreference("luma_drift_save_v1",receipt)),"Pending reward survives portrait restoration with its exact single in-memory balance and unchanged disk state");capture("guardian-reward-storage-fault-rotated-en");
        }finally{if(rewardLocked)requestStorageFault("reward","restore");}
        physicalRetry();check(!has(x->x.equals("Retry")||x.equals("Try again"),true)&&coins()==balance+100&&save().getBoolean(receipt,false),"Restored storage Retry exits the barrier with exactly one Guardian3 reward");
        check(diskCoins()==balance+100&&"true".equals(diskPreference("luma_drift_save_v1",receipt)),"Recovered100 coins and Guardian3 receipt are both present in the actual disk file");
        seekControl(x->x.equals("Prism fragment restored"));touchNode(x->x.equals("Prism fragment restored"),false);check(coins()==balance+100&&diskCoins()==balance+100,"Repeating the recovered guardian claim adds zero coins in memory and on disk");
        write("guardian-storage-fault.json",new JSONObject().put("passed",true).put("scope","Two real storage permission failures, pending guardian action and reward receipt; native Retry/back/rotation; only infrastructure permissions changed, no preference values injected").put("beforeGuardian",before).put("pendingAndPersistedGuardian",first.getString("save")).put("coinsBefore",balance).put("coinsAfter",balance+100).put("receipt",receipt).put("fractionX",.6).put("fractionY",.5).toString(2));capture("guardian-reward-storage-fault-recovered-en");home();
    }
    private void landscape()throws Exception{
        home();clickExact("Map",true);android.view.WindowManager wm=(android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE);int original=wm.getDefaultDisplay().getRotation();RotationFrame viewport=observeRotationFrame();
        try{rotateAndAwait(UiAutomation.ROTATION_FREEZE_90,viewport,"Map landscape layout");clickExact("Explore a level",false);setInput("101");clickExact("Continue",false);
            AccessibilityNodeInfo jump=waitFor(x->x.equals("Explore a level"),true,3000),level=waitFor(x->x.startsWith("Level 101,"),true,3000);Rect header=new Rect(),node=new Rect();jump.getBoundsInScreen(header);level.getBoundsInScreen(node);jump.recycle();level.recycle();check(!Rect.intersects(header,node)&&node.top>=header.bottom,"Landscape level101 remains below the pinned header");check(header.height()/getTargetContext().getResources().getDisplayMetrics().density>=48,"Landscape jump control keeps at least48dp touch height");capture("map-landscape-en");}
        finally{rotateAndAwait(original,viewport,"Portrait return layout");}home();
    }
    private void languages()throws Exception{
        String[] ids={"en","fr","de","es","pt","it"},names={"English","Français","Deutsch","Español","Português","Italiano"};
        String[] homes={"Home","Accueil","Start","Inicio","Início","Home"},maps={"Map","Carte","Karte","Mapa","Mapa","Mappa"};
        String[] resume={"Continue learning","Reprendre le tutoriel","Weiterlernen","Seguir aprendiendo","Continuar aprendendo","Continua a imparare"};
        String[] learn={"Replay the interactive tutorial","Rejouer le tutoriel interactif","Interaktive Einführung wiederholen","Repetir el tutorial interactivo","Repetir o tutorial interativo","Ripeti il tutorial interattivo"};
        for(int i=0;i<ids.length;i++){
            home();awaitRotationFrame(initialViewport.rotation,initialViewport,"Localized Home viewport "+ids[i]);click(x->Arrays.asList(SETTINGS).contains(x),false);clickExact(names[i],true);back();awaitRotationFrame(initialViewport.rotation,initialViewport,"Localized capture layout "+ids[i]);top();capture(i<2?"home-"+ids[i]+"-localized":"locale-home-"+ids[i]);
            String expected=homes[i];check(has(x->x.equals(expected),true),"Native "+ids[i]+" navigation has a localized Home control");
            clickExact(maps[i],true);
            if(i<2){String explore=i==0?"Explore a level":"Explorer un niveau",cont=i==0?"Continue":"Continuer";clickExact(explore,false);setInput("1000");clickExact(cont,false);capture("map-1000-"+ids[i]);}
            else capture("locale-map-"+ids[i]);check(has(x->x.equals(expected),true),"Native "+ids[i]+" map retains its localized Home destination");clickExact(expected,false);
            if(i<2){
                String wardrobe=i==0?"View my companion":"Voir mon compagnon",rewards=i==0?"[0-9]+ ready":"[0-9]+ à récupérer";
                click(x->x.equals(wardrobe),true);capture("avatar-"+ids[i]);clickExact(expected,true);
                click(x->x.matches(rewards),true);clickExact(i==0?"Ready to collect":"À récupérer",false);capture("ready-"+ids[i]);clickExact(expected,true);
                String journal=i==0?"Prism journal,":"Carnet du Prisme,";click(x->x.startsWith(journal),true);capture("story-"+ids[i]);clickExact(expected,true);
                clickExact(i==0?"Shop":"Boutique",false);clickExact(i==0?"Appearance":"Apparence",false);capture("shop-looks-"+ids[i]);clickExact(expected,true);
                clickExact(i==0?"Events":"Événements",false);capture("events-"+ids[i]);clickExact(expected,true);
            }
            String learnLabel=learn[i],resumeLabel=resume[i];click(x->x.equals(learnLabel)||x.startsWith(learnLabel+",")||x.equals(resumeLabel)||x.startsWith(resumeLabel+","),true);check(lesson()>=0&&lesson()<7,"Native "+ids[i]+" practice retains the real saved lesson");
            if(i<2){practiceToKey(ids[i]);capture("tutorial-key-"+ids[i]);}else capture("locale-tutorial-board-"+ids[i]);clickExact(SKIP[i],true);
            // Return explicitly through the visible localized settings, without synthetic language injection.
            click(x->Arrays.asList(SETTINGS).contains(x),false);clickExact("English",true);back();
        }
    }
    private void practiceToKey(String language)throws Exception{
        boolean fr=language.equals("fr");int tries=0;
        while(lesson()<2){if(tries++>12)throw new AssertionError("Cannot reach the key practice through real controls");int l=lesson(),p=phase();
            if(l==0&&p==0)pieceLocalized(1,1,fr);
            else if(l==0)click(x->x.startsWith(fr?"La petite leçon suivante":"Next little lesson"),true);
            else if(l==1&&p==0||l==1&&p==2)pieceLocalized(1,0,fr);
            else if(l==1&&p==1)pieceLocalized(1,2,fr);
            else click(x->x.startsWith(fr?"La petite leçon suivante":"Next little lesson"),true);
        }
        check(lesson()==2,"Localized "+language+" key lesson reached through real practice actions");
    }
    private void pieceLocalized(int row,int col,boolean french)throws Exception{
        if(!french){piece(row,col);return;}click(x->x.contains("Pièce, ligne "+(row+1)+", colonne "+(col+1)+","),false);
    }
    private void visualCheck(boolean value,String message)throws Exception{
        count++;checks.put(new JSONObject().put("pass",value).put("message",message));if(!value)visualSoftFailures++;
        Bundle status=new Bundle();status.putString("stream",(value?"PASS ":"FAIL ")+message+"\n");sendStatus(0,status);
    }
    private void ensureReducedMotion()throws Exception{
        home();click(x->Arrays.asList(SETTINGS).contains(x),false);
        AccessibilityNodeInfo control=waitFor(x->x.startsWith("Reduce motion:"),true,3000);String current=name(control);control.recycle();
        if(!session().getBoolean("reduce_motion",false))touchNode(x->x.startsWith("Reduce motion:"),false);
        check(has(x->x.equals("Reduce motion: On"),true)&&session().getBoolean("reduce_motion",false),"Reduced motion is enabled by the actual visible setting, never by preference injection");back();
    }
    private Rect identityBounds()throws Exception{
        AccessibilityNodeInfo portrait=find(x->x.startsWith("Equipped companion portrait"),false);Rect result=new Rect();
        if(portrait!=null){portrait.getBoundsInScreen(result);boolean actionable=portrait.isClickable()||portrait.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);check(!actionable,"Equipped portrait semantics expose information without advertising a click");portrait.recycle();actions.put(new JSONObject().put("observation","portrait-crop-anchor").put("method","observed portrait accessibility region").put("bounds",result.toShortString()));return result;}
        check(Integer.parseInt(args.getString("expectedCode","15"))<=11,"1.13 requires its actual semantic portrait crop; the RC3 geometry fallback is historical only");AccessibilityNodeInfo slot=waitFor(x->x.startsWith("Hat, "),true,3000);Rect anchor=new Rect();slot.getBoundsInScreen(anchor);slot.recycle();
        float scale=anchor.width()/68f;result.set(Math.round(anchor.right+5*scale),Math.round(anchor.top-3*scale),Math.round(anchor.right+177*scale),Math.round(anchor.top+169*scale));
        actions.put(new JSONObject().put("observation","portrait-crop-anchor").put("method","RC3 fallback: observed Hat slot68logical units; central art x109..281/y127..299; no text or slot icons").put("anchor",anchor.toShortString()).put("bounds",result.toShortString()).put("scale",scale));return result;
    }
    private Rect backgroundBounds()throws Exception{
        Rect game=actualGameBounds();check(game!=null&&game.width()>0,"Background crop has an observed native game window");float scale=game.width()/390f;
        // Blank left gutter below the header; no text, cards, avatar, board, status bar or navigation.
        return new Rect(Math.round(game.left+2*scale),Math.round(game.top+102*scale),Math.round(game.left+18*scale),Math.round(game.top+320*scale));
    }
    private void saveBitmap(String title,Bitmap pixels)throws Exception{
        try(OutputStream file=new FileOutputStream(new File(output,title+".png"))){if(!pixels.compress(Bitmap.CompressFormat.PNG,100,file))throw new IOException("PNG compression failed");}captures.put(title);
    }
    private RasterObservation observeRaster(String title,boolean background)throws Exception{
        top();Rect box=background?backgroundBounds():identityBounds();Bitmap previous=null,last=null;long noise=0;JSONArray repeats=new JSONArray();
        for(int sample=0;sample<3;sample++){
            SystemClock.sleep(180);Bitmap full=ui.takeScreenshot();if(full==null)throw new IOException("Missing real SDK bitmap");
            check(box.left>=0&&box.top>=0&&box.right<=full.getWidth()&&box.bottom<=full.getHeight(),"Observed raster crop is entirely inside the unscaled screenshot: "+title);
            Bitmap crop=Bitmap.createBitmap(full,box.left,box.top,box.width(),box.height());
            if(sample==2)saveBitmap(title+"-original",full);full.recycle();
            if(previous!=null){JSONObject delta=RasterObservation.difference(previous,crop);repeats.put(delta);noise=Math.max(noise,delta.getLong("changedPixels"));previous.recycle();}
            previous=crop;last=crop;
        }
        saveBitmap(title+"-analysis-crop",last);double ratio=noise/((double)box.width()*box.height());
        actions.put(new JSONObject().put("observation","stable-real-raster").put("title",title).put("bounds",box.toShortString()).put("kind",background?"blank-canvas-theme-background":"equipped-companion-art-only").put("repeatDifferences",repeats).put("worstRepeatChangedPixels",noise).put("worstRepeatChangedRatio",ratio).put("reducedMotion",session().getBoolean("reduce_motion",false)).put("derivativePolicy","Original PNG plus explicitly labelled unscaled analysis crop; no resize, recolour or compositing"));
        visualCheck(ratio<=.001,"Three observed reduced-motion crops are stable within0.1% above RGB8: "+title+" noise="+noise);
        return new RasterObservation(last,box,noise);
    }
    private void compareRaster(String label,RasterObservation before,RasterObservation after,boolean shouldChange)throws Exception{
        JSONObject delta=RasterObservation.difference(before.pixels,after.pixels);long threshold=Math.max(64,Math.max(before.noisePixels,after.noisePixels)*4+32);
        boolean changed=delta.getLong("changedPixels")>threshold;
        delta.put("observation","real-raster-comparison").put("label",label).put("expectedChange",shouldChange).put("minimumChangedPixelsExclusive",threshold).put("beforeBounds",before.bounds.toShortString()).put("afterBounds",after.bounds.toShortString());actions.put(delta);
        visualCheck(changed==shouldChange,label+": actual changedPixels="+delta.getLong("changedPixels")+", threshold="+threshold+", expectedChange="+shouldChange);
    }
    private JSONObject visualIdentity()throws Exception{
        JSONObject values=new JSONObject();for(String key:new String[]{"character","skin","avatar_style_v19","cosmetic_slot_v17_HAT","cosmetic_slot_v17_DRESS","cosmetic_slot_v17_SHOES","cosmetic_slot_v17_GLOVES","avatar_slot_v1_HEAD","avatar_slot_v1_AURA","avatar_slot_v1_CHARM"})values.put(key,save().contains(key)?save().getString(key,""):JSONObject.NULL);values.put("coins",coins());return values;
    }
    private void wardrobeItem(String slot,String item,boolean secondPage)throws Exception{
        top();touchNode(x->x.startsWith(slot+", "),false);if(secondPage){seekControl(x->x.equals("›"));touchNode(x->x.equals("›"),false);}
        top();seekControl(x->x.startsWith(item+","));touchNode(x->x.startsWith(item+","),false);
    }
    private void visualEquipStarter(String slot,String item)throws Exception{
        wardrobeItem(slot,item,false);if(has(x->x.equalsIgnoreCase("Equip"),true))touchNode(x->x.equalsIgnoreCase("Equip"),false);else clickExact("Close",true);
    }
    private void visualCollectionOffer(String tab,String label)throws Exception{
        home();clickExact("Inventory",false);top();clickExact(tab,false);clickExact("All",false);top();seekControl(x->x.startsWith(label+","));touchNode(x->x.startsWith(label+","),false);
    }
    private void visualSelectBase(String tab,String label)throws Exception{
        visualCollectionOffer(tab,label);if(has(x->x.equalsIgnoreCase("Equip"),true))touchNode(x->x.equalsIgnoreCase("Equip"),false);else clickExact("Close",true);
    }
    private void visualSelectStyle(String label)throws Exception{
        home();clickExact("Visual styles",true);top();seekControl(x->x.startsWith(label+","));touchNode(x->x.startsWith(label+","),false);home();
    }
    private void visualHeadStarter()throws Exception{
        top();Set<String> names=new HashSet<>(Arrays.asList("Moonbud","Lantern Leaf","Crescent Knot","Tide Crown","Starfold Visor","Prism Antlers"));
        Predicate<String> headHeader=x->{for(String name:names)if(x.equals(name+", Equipped"))return true;return false;};
        seekControl(headHeader);touchNode(headHeader,false);
        if(!save().getString("avatar_slot_v1_HEAD","moonbud").equals("moonbud")){top();seekControl(x->x.startsWith("Moonbud,"));touchNode(x->x.startsWith("Moonbud,"),false);touchNode(x->x.equalsIgnoreCase("Equip"),false);}
        check(save().getString("avatar_slot_v1_HEAD","moonbud").equals("moonbud"),"Head slot is explicitly selected and normalized through its real starter offer before raster comparison");
    }
    private void avatarVisual()throws Exception{
        check(actualJourneyClears()>=20&&coins()>=2100,"Visual purchase qualification requires20+ actual Journey clears and2100 genuinely earned coins; no seeded balance");
        visualSelectBase("Companions","Nova");visualSelectBase("Themes","Lumina");visualSelectStyle("Plush classic");openCompanion();
        String[] slots={"Hat","Outfit","Shoes","Gloves"},starters={"Hat · Dew","Outfit · Dew","Shoes · Dew","Gloves · Dew"};
        for(int i=0;i<slots.length;i++)visualEquipStarter(slots[i],starters[i]);visualHeadStarter();
        RasterObservation previous=observeRaster("visual-00-starter",false);Map<String,Object> initial=presentationState(false);
        wardrobeItem("Hat","Hat · Cupid",true);capture("visual-preview-cupid");clickExact("Close",true);
        check(presentationState(false).equals(initial),"Browsing and closing a real clothing preview grants, equips and debits nothing");
        RasterObservation afterPreview=observeRaster("visual-01-closed-preview",false);compareRaster("Closed preview leaves actual equipped art unchanged",previous,afterPreview,false);previous.recycle();previous=afterPreview;
        String[] ids={"hat_cupid","dress_witch","shoes_cupid","gloves_witch"},names={"Hat · Cupid","Outfit · Little witch","Shoes · Cupid","Gloves · Little witch"},keys={"HAT","DRESS","SHOES","GLOVES"};int[] prices={450,500,400,400};int purchases=0;
        for(int i=0;i<ids.length;i++){
            String id=ids[i],name=names[i],key="cosmetic_slot_v17_"+keys[i],priceLabel=prices[i]+" LumaCoins";wardrobeItem(slots[i],name,true);int beforeCoins=coins();String equippedBefore=save().getString(key,"");
            if(has(x->x.equalsIgnoreCase(priceLabel),true)){
                final String price=prices[i]+" LumaCoins";touchNode(x->x.equalsIgnoreCase(price),false);purchases++;
                check(!save().getString("cosmetic_owned_v17_"+id,"").isEmpty()&&coins()==beforeCoins-prices[i],"Explicit earned-coin purchase persists ownership and the exact debit: "+id);
                check(save().getString(key,"").equals(equippedBefore),"Clothing purchase alone does not silently equip "+id);
                if(Integer.parseInt(args.getString("expectedCode","15"))>=12){waitFor(x->x.startsWith("Visual preview · no item granted"),false,3000).recycle();check(has(x->x.equalsIgnoreCase("Equip"),true),"Post-purchase offer explicitly provides Equip without silently applying the item");clickExact("Close",true);}
                RasterObservation purchased=observeRaster("visual-purchased-"+id,false);compareRaster("Purchase without Equip leaves actual worn art unchanged: "+id,previous,purchased,false);previous.recycle();previous=purchased;
                wardrobeItem(slots[i],name,true);
            }
            touchNode(x->x.equalsIgnoreCase("Equip"),false);check(save().getString(key,"").equals(id),"Explicit visible Equip stores the requested clothing identity: "+id);
            RasterObservation equipped=observeRaster("visual-equipped-"+id,false);compareRaster("Equipping visibly changes the art for "+id,previous,equipped,true);previous.recycle();previous=equipped;
            int repeatCoins=coins();wardrobeItem(slots[i],name,true);check(!has(x->x.equalsIgnoreCase("Equip")||x.equalsIgnoreCase(priceLabel),true),"Already worn clothing offers no second purchase or equip: "+id);clickExact("Close",true);check(coins()==repeatCoins,"Reopening equipped clothing has no repeated debit: "+id);
        }
        check(purchases>0,"At least one genuine new earned-coin clothing purchase was exercised");
        top();touchNode(x->x.equals("Moonbud, Equipped"),false);top();seekControl(x->x.startsWith("Lantern Leaf,"));touchNode(x->x.startsWith("Lantern Leaf,"),false);touchNode(x->x.equalsIgnoreCase("Equip"),false);
        check(save().getString("avatar_slot_v1_HEAD","").equals("lantern_leaf"),"Actual20+ earned clears supply Lantern Leaf, equipped only through its visible offer");
        RasterObservation gear=observeRaster("visual-equipped-lantern-leaf",false);compareRaster("Earned gear visibly changes worn portrait",previous,gear,true);previous.recycle();previous=gear;
        visualCollectionOffer("Companions","Miko");int beforeMiko=coins();boolean mikoOwned=save().getBoolean("owned_character_miko",false);
        if(!mikoOwned){seekControl(x->x.equalsIgnoreCase("160 LumaCoins"));touchNode(x->x.equalsIgnoreCase("160 LumaCoins"),false);check(save().getBoolean("owned_character_miko",false)&&coins()==beforeMiko-160,"Visible Miko purchase persists ownership with an exact earned debit");check(save().getString("character","").equals("nova"),"Buying Miko does not silently equip the companion");}
        seekControl(x->x.equalsIgnoreCase("Equip"));touchNode(x->x.equalsIgnoreCase("Equip"),false);check(save().getString("character","").equals("miko")&&coins()==beforeMiko-(mikoOwned?0:160),"Separate visible Miko Equip changes actual identity without another debit");
        openCompanion();RasterObservation character=observeRaster("visual-equipped-miko",false);compareRaster("Companion identity visibly changes independent of its label",previous,character,true);previous.recycle();previous=character;
        visualSelectStyle("Retro arcade");openCompanion();RasterObservation style=observeRaster("visual-equipped-retro",false);compareRaster("Free visual style changes the actual portrait, excluding text",previous,style,true);previous.recycle();previous=style;
        home();RasterObservation themeHomeBefore=observeRaster("visual-theme-home-lumina",true);openJourney(1);restart();RasterObservation themePlayBefore=observeRaster("visual-theme-play-lumina",true);
        visualCollectionOffer("Themes","Lagoon");int beforeTheme=coins();boolean themeOwned=save().getBoolean("owned_skin_lagoon",false);
        if(!themeOwned){seekControl(x->x.equalsIgnoreCase("180 LumaCoins"));touchNode(x->x.equalsIgnoreCase("180 LumaCoins"),false);check(save().getBoolean("owned_skin_lagoon",false)&&coins()==beforeTheme-180,"Visible Lagoon purchase persists ownership with an exact earned debit");check(save().getString("skin","").equals("lumina"),"Buying Lagoon does not silently apply the theme");}
        seekControl(x->x.equalsIgnoreCase("Equip"));touchNode(x->x.equalsIgnoreCase("Equip"),false);
        check(save().getString("skin","").equals("lagoon")&&coins()==beforeTheme-(themeOwned?0:180),"Separate visible theme Equip stores Lagoon without another debit");
        home();RasterObservation themeHomeAfter=observeRaster("visual-theme-home-lagoon",true);compareRaster("Theme changes the actual blank Home canvas, excluding labels/cards",themeHomeBefore,themeHomeAfter,true);themeHomeBefore.recycle();themeHomeAfter.recycle();
        openJourney(1);restart();RasterObservation themePlayAfter=observeRaster("visual-theme-play-lagoon",true);compareRaster("Theme changes the actual blank gameplay canvas",themePlayBefore,themePlayAfter,true);themePlayBefore.recycle();themePlayAfter.recycle();
        openCompanion();RasterObservation finalPortrait=observeRaster("visual-final-equipped",false);compareRaster("Changing theme leaves the worn portrait identity unchanged",previous,finalPortrait,false);previous.recycle();
        JSONObject checkpoint=new JSONObject().put("identity",visualIdentity()).put("portraitCrop","visual-final-equipped-analysis-crop.png").put("portraitBounds",finalPortrait.bounds.toShortString()).put("portraitNoise",finalPortrait.noisePixels).put("prepareSoftFailures",visualSoftFailures).put("coldRequired",true).put("scope","Expected state and exact SDK crop observed after real UI actions; not a seed");write("visual-checkpoint.json",checkpoint.toString(2));finalPortrait.recycle();home();
    }
    private void visualColdVerify()throws Exception{
        JSONObject checkpoint=new JSONObject(readFile(new File(output,"visual-checkpoint.json")));JSONObject identity=checkpoint.getJSONObject("identity"),actual=visualIdentity();
        for(Iterator<String> keys=identity.keys();keys.hasNext();){String key=keys.next();check(Objects.equals(identity.opt(key),actual.opt(key)),"After host force-stop, durable actual identity/balance matches completed UI actions: "+key);}
        openCompanion();RasterObservation after=observeRaster("visual-cold-equipped",false);Bitmap prior=android.graphics.BitmapFactory.decodeFile(new File(output,checkpoint.getString("portraitCrop")).getAbsolutePath());check(prior!=null,"Cold verification reads the prior original unscaled observation, never a generated reference");
        RasterObservation before=new RasterObservation(prior,after.bounds,checkpoint.getLong("portraitNoise"));compareRaster("Actual equipped drawing survives a cold process relaunch",before,after,false);before.recycle();after.recycle();home();
    }
    private void decorFrame(boolean french)throws Exception{
        home();top();String before=roomRecord(),decorate=french?"Décorer":"Decorate",bed=french?"Lit Pli de lune":"Moonfold bed",cancel=french?"Annuler":"Cancel",done=french?"Terminé":"Done";
        seekHouseControl(x->x.equals(decorate));touchNode(x->x.equals(decorate),false);seekHouseControl(x->x.startsWith(bed+",")&&x.contains(decorate));touchNode(x->x.startsWith(bed+",")&&x.contains(decorate),false);top();capture(french?"house-decor-fr":"house-decor-en");
        check(roomRecord().equals(before),"Actual "+(french?"French":"English")+" bed selection shows a decoration preview without a saved edit");clickExact(cancel,true);clickExact(done,true);check(roomRecord().equals(before),"Cancel and Done preserve the exact prior room after the "+(french?"French":"English")+" decoration preview");top();
    }
    private void puzzleLocales()throws Exception{
        check(save().getInt("stars_15",0)>0,"Bilingual puzzle captures require level15 genuinely cleared in this real test or retained guest");openJourney(15);restart();Map<String,Object> before=boardAndEconomy();capture("puzzle-current-en");decorFrame(false);click(x->Arrays.asList(SETTINGS).contains(x),false);clickExact("Français",true);back();click(x->x.startsWith("Reprendre"),true);
        check(session().getInt("level",0)==15&&session().getString("history","bad").isEmpty()&&session().getInt("budget_used",-1)==0,"French Resume restores the exact genuinely unlocked restarted puzzle without synthetic state");capture("puzzle-current-fr");decorFrame(true);click(x->Arrays.asList(SETTINGS).contains(x),false);clickExact("English",true);back();check(boardAndEconomy().equals(before),"FR/EN puzzle and decoration presentation changes preserve the board, stock, economy and progression");
    }
    // v1.14 home protocol: all state changes use the real accessible controls.
    private static final String HOME_LAYOUT="home_layout_v113";
    private String roomRecord(){return save().getString(HOME_LAYOUT,"1|3,3|bed,0,0,0;lamp,5,0,0;plant,0,5,0;rug,2,2,0;cushion,4,4,0;");}
    private Map<String,Object> roomState(){
        Map<String,Object> state=new TreeMap<>();state.put(HOME_LAYOUT,roomRecord());
        for(Map.Entry<String,?> value:save().getAll().entrySet())if(value.getKey().startsWith("home_"))state.put(value.getKey(),value.getValue());
        return state;
    }
    private Map<String,Object> boardAndEconomy(){
        Map<String,Object> state=new TreeMap<>();
        for(Map.Entry<String,?> value:save().getAll().entrySet())if(!value.getKey().startsWith("home_")&&!value.getKey().equals("language"))state.put("save/"+value.getKey(),value.getValue());
        for(String key:new String[]{"active","mode","level","event","history","challenge_code","content_version","budget_policy","run_id","budget_used","budget_extensions","budget_extra","undos","hints"})state.put("session/"+key,session().getAll().get(key));
        return state;
    }
    private void atHouse()throws Exception{
        home();top();waitFor(x->x.equals("Luma Drift · My little home")||x.equals("Luma Drift · Home"),false,4000).recycle();
        for(int i=0;i<40;i++){if(has(x->x.equals("Done"),true)){clickExact("Done",false);break;}if(has(x->x.equals("Decorate"),true))break;fineHomeScroll(true);}
        seekHouseControl(x->x.equals("Dress up"));seekHouseControl(x->x.equals("Decorate"));check(has(x->x.equals("Dress up"),true)&&has(x->x.equals("Decorate"),true),"Actual home exposes reachable companion wardrobe and room editor controls");top();
    }
    private void fineHomeScroll(boolean forward)throws Exception{
        Rect game=actualGameBounds();check(game!=null&&game.width()>0&&game.height()>0,"Fine native scrolling uses the observed game viewport");float x=game.exactCenterX(),start=game.top+game.height()*(forward?.50f:.34f),travel=Math.min(game.height()*.16f,getTargetContext().getResources().getDisplayMetrics().density*64),end=start+(forward?-travel:travel);long down=SystemClock.uptimeMillis();
        injectTouch(down,MotionEvent.ACTION_DOWN,x,start);for(int step=1;step<=6;step++){SystemClock.sleep(25);injectTouch(down,MotionEvent.ACTION_MOVE,x,start+(end-start)*step/6);}injectTouch(down,MotionEvent.ACTION_UP,x,end);SystemClock.sleep(100);actions.put(new JSONObject().put("gesture","fine-menu-scroll").put("direction",forward?"forward":"backward").put("observedBounds",game.toShortString()).put("startY",start).put("endY",end).put("input","UiAutomation touchscreen; no shell input"));
    }
    private void seekHouseControl(Predicate<String> wanted)throws Exception{
        top();Rect game=actualGameBounds();float scale=Math.min(game.width()/390f,getTargetContext().getResources().getDisplayMetrics().density*1.25f);
        for(int i=0;i<40;i++){AccessibilityNodeInfo node=find(wanted,true);if(node!=null){Rect r=new Rect();node.getBoundsInScreen(r);node.recycle();if(r.height()>=47.0f*scale&&game.contains(r))return;}fineHomeScroll(true);}throw new AssertionError("House control cannot be brought into the short viewport at its full48-unit height: "+labels());
    }
    private Predicate<String> cuddleControl(){return x->x.equals("Cuddle")||x.equals("A little hug");}
    private Predicate<String> moveCell(int x,int y){String coordinate="("+x+","+y+")";return label->(label.startsWith("Move here")||label.startsWith("Come here"))&&label.replace(" ","").contains(coordinate);}
    private String[] companionCell(){return roomRecord().split("\\|",-1)[1].split(",",-1);}
    private Rect observedBounds(Predicate<String> wanted)throws Exception{AccessibilityNodeInfo n=waitFor(wanted,false,3000);Rect r=new Rect();n.getBoundsInScreen(r);n.recycle();check(r.width()>0&&r.height()>0,"Observed home art has a nonempty real native region");return r;}
    private Bitmap artCrop(Rect region)throws Exception{Bitmap full=ui.takeScreenshot();check(full!=null&&region.left>=0&&region.top>=0&&region.right<=full.getWidth()&&region.bottom<=full.getHeight(),"Home raster region lies inside its actual unscaled SDK screenshot");Bitmap result=Bitmap.createBitmap(full,region.left,region.top,region.width(),region.height());full.recycle();return result;}
    private void houseSmoke()throws Exception{
        ensureReducedMotion();atHouse();Map<String,Object> before=boardAndEconomy();
        assertVisibleTouchTarget(x->x.equals("Dress up"),"House wardrobe");assertVisibleTouchTarget(x->x.equals("Decorate"),"House decorate");assertVisibleTouchTarget(cuddleControl(),"House cuddle");capture("house-initial-en");
        String record=roomRecord();int tx=1,ty=4;if(companionCell()[0].equals("1")&&companionCell()[1].equals("4")){tx=2;ty=4;}
        final int mx=tx,my=ty;touchNode(moveCell(mx,my),false);SystemClock.sleep(650);
        check(companionCell()[0].equals(Integer.toString(mx))&&companionCell()[1].equals(Integer.toString(my)),"Touching an observed free floor tile persistently moves the real companion to that tile");
        check(!roomRecord().equals(record),"Companion movement changes the complete saved room record");capture("house-companion-moved-en");
        Rect character=observedBounds(x->x.equals("Home companion"));Bitmap idle=artCrop(character);SystemClock.sleep(180);Bitmap repeat=artCrop(character);long noise=RasterObservation.difference(idle,repeat).getLong("changedPixels");repeat.recycle();
        touchNode(cuddleControl(),false);Bitmap hugging=artCrop(character);saveBitmap("house-cuddle-idle-analysis-crop",idle);saveBitmap("house-cuddle-active-analysis-crop",hugging);
        JSONObject hugDelta=RasterObservation.difference(idle,hugging);long threshold=Math.max(64,noise*4+32);actions.put(hugDelta.put("observation","actual-cuddle-raster").put("idleNoisePixels",noise).put("threshold",threshold).put("bounds",character.toShortString()));check(hugDelta.getLong("changedPixels")>threshold,"Cuddle visibly changes the actual companion drawing beyond observed idle noise");idle.recycle();hugging.recycle();capture("house-cuddle-active-en");
        houseObjectInteractions();JSONObject outfit=visualIdentity();clickExact("Dress up",false);waitFor(x->x.equals("Luma Drift · My companion"),false,3000).recycle();check(visualIdentity().toString().equals(outfit.toString()),"Home wardrobe opens the exact existing companion and equipment without silent changes");capture("house-wardrobe-en");atHouse();
        check(boardAndEconomy().equals(before),"Moving, cuddling and opening the wardrobe leave actual puzzle history, ownership, balance and rewards unchanged");
    }
    private void houseObjectInteractions()throws Exception{
        top();touchNode(x->x.startsWith("Moonfold bed,")&&x.contains("Take a little rest"),false);SystemClock.sleep(650);check(has(x->x.equals("Luma Drift · My little home"),false),"A physical bed touch stays in the actual house");Rect resting=observedBounds(x->x.equals("Home companion"));Bitmap rest=artCrop(resting);capture("house-rest-on-bed-en");
        SystemClock.sleep(8100);Rect later=observedBounds(x->x.equals("Home companion"));Bitmap restLater=artCrop(later);check(resting.equals(later)&&RasterObservation.difference(rest,restLater).getLong("changedPixels")<=16,"The reduced-motion companion remains visibly resting at the same bed position after8seconds");rest.recycle();restLater.recycle();capture("house-rest-after-eight-seconds-en");touchNode(x->x.endsWith(", A little hug"),false);check(has(x->x.equals("Luma Drift · My little home"),false),"A physical touch on the resting companion reaches its current visible cuddle target");capture("house-cuddle-from-bed-en");
        top();Predicate<String> lamp=x->x.startsWith("Warmglow lamp,")&&x.contains("Switch the light");touchNode(lamp,false);SystemClock.sleep(500);check(has(x->x.equals("Luma Drift · My little home"),false),"A physical lamp touch toggles its interaction without opening the puzzle door");Rect lampBounds=observedBounds(lamp);Bitmap first=artCrop(lampBounds);saveBitmap("house-lamp-state-a-analysis-crop",first);capture("house-lamp-state-a-en");
        touchNode(lamp,false);SystemClock.sleep(250);Bitmap second=artCrop(lampBounds);saveBitmap("house-lamp-state-b-analysis-crop",second);JSONObject delta=RasterObservation.difference(first,second);actions.put(delta.put("observation","actual-lamp-switch-raster").put("bounds",lampBounds.toShortString()));check(delta.getLong("changedPixels")>32,"Toggling the same reachable lamp changes actual lamp pixels with the companion in the same position");capture("house-lamp-state-b-en");
        touchNode(lamp,false);SystemClock.sleep(250);Bitmap third=artCrop(lampBounds);check(RasterObservation.difference(first,third).getLong("changedPixels")<=16,"A second lamp toggle restores the exact prior reduced-motion lamp drawing");first.recycle();second.recycle();third.recycle();
    }
    private void homeOffer(String tab,String name)throws Exception{
        atHouse();seekHouseControl(x->x.equals("Decorate"));touchNode(x->x.equals("Decorate"),false);seekHouseControl(x->x.equals("Furniture"));touchNode(x->x.equals("Furniture"),false);top();if(!tab.equals("Furniture"))clickExact(tab,false);
        seekControl(x->x.startsWith(name+","));touchNode(x->x.startsWith(name+","),false);waitFor(x->x.startsWith(name+","),false,3000).recycle();
    }
    private boolean placedAt(String id,int x,int y,int rotation){return roomRecord().split("\\|",-1)[2].contains(id+","+x+","+y+","+rotation+";");}
    private void house()throws Exception{
        ensureReducedMotion();atHouse();touchNode(moveCell(1,4),false);SystemClock.sleep(300);check(companionCell()[0].equals("1")&&companionCell()[1].equals("4"),"Furniture qualification calls the companion to an actual free floor tile before editing");check(coins()>=240,"Home purchase test has at least240 genuinely earned LumaCoins; no injected balance");
        check(!save().getBoolean("home_owned_v113_sofa",false),"Qualification starts before the new sofa is owned, so an actual purchase can be observed");
        homeOffer("Furniture","Cloud nook sofa");int balance=coins();String initial=roomRecord();JSONObject wardrobe=visualIdentity();Map<String,Object> preview=roomState();
        capture("house-sofa-preview-en");check(coins()==balance&&roomRecord().equals(initial),"Opening furniture details leaves the wallet and placed room unchanged");
        clickExact("Save as my wish",true);check(save().getString("home_goal_v113","").equals("sofa"),"Explicit visible wish action persists the selected furniture goal");
        atHouse();seekControl(x->x.startsWith("My next wish,")&&x.contains("Cloud nook sofa")&&x.contains("240 / 240 LumaCoins"));capture("house-wish-progress-en");
        homeOffer("Furniture","Cloud nook sofa");seekControl(x->x.equals("Get for 240 LumaCoins"));touchNode(x->x.equals("Get for 240 LumaCoins"),false);
        check(save().getBoolean("home_owned_v113_sofa",false)&&coins()==balance-240,"Actual visible sofa purchase stores ownership and debits exactly240 earned LumaCoins");
        check(roomRecord().equals(initial),"Purchasing furniture does not automatically place it or move the companion");
        JSONObject afterPurchase=visualIdentity();wardrobe.put("coins",coins());check(afterPurchase.toString().equals(wardrobe.toString()),"Furniture purchase does not change the existing companion, visual style or worn equipment");capture("house-sofa-owned-unplaced-en");
        clickExact("Place",true);touchNode(moveCell(0,0),false);String prior=roomRecord();capture("house-placement-blocked-en");
        seekControl(x->x.equals("Place"));touchNode(x->x.equals("Place"),false);check(roomRecord().equals(prior),"Explicit invalid placement overlapping the starter bed cannot alter the persisted room");
        top();touchNode(moveCell(2,0),false);clickExact("Rotate",true);check(roomRecord().equals(prior),"Choosing a valid tile and rotating the preview make no persisted edit before Place");capture("house-sofa-rotated-preview-en");
        android.view.WindowManager wm=(android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE);int rotation=wm.getDefaultDisplay().getRotation();RotationFrame viewport=observeRotationFrame();
        try{rotateAndAwait(UiAutomation.ROTATION_FREEZE_90,viewport,"Furniture preview landscape");check(roomRecord().equals(prior),"Activity recreation preserves the uncommitted furniture preview without silently placing it");seekControl(x->x.equals("Rotate"));capture("house-preview-landscape-en");}
        finally{rotateAndAwait(rotation,viewport,"Furniture preview portrait return");}
        seekControl(x->x.equals("Place"));touchNode(x->x.equals("Place"),false);check(placedAt("sofa",2,0,1),"Explicit Place commits exactly the selected cell and rotated two-cell footprint");capture("house-sofa-placed-en");
        top();seekControl(x->x.startsWith("Cloud nook sofa,")&&x.contains("Decorate"));touchNode(x->x.startsWith("Cloud nook sofa,")&&x.contains("Decorate"),false);top();touchNode(moveCell(3,0),false);clickExact("Place",true);check(placedAt("sofa",3,0,1),"Selecting an already placed sofa and confirming a new free tile persists its move");
        top();seekControl(x->x.startsWith("Cloud nook sofa,")&&x.contains("Decorate"));touchNode(x->x.startsWith("Cloud nook sofa,")&&x.contains("Decorate"),false);clickExact("Store away",true);
        check(!roomRecord().contains("sofa,")&&save().getBoolean("home_owned_v113_sofa",false)&&coins()==balance-240,"Store away removes only the placement while retaining ownership and the exact wallet");capture("house-sofa-stored-en");
        homeOffer("Furniture","Cloud nook sofa");clickExact("Place",true);top();touchNode(moveCell(3,0),false);clickExact("Place",true);check(placedAt("sofa",3,0,0),"Owned sofa can be placed again with a newly selected orientation without another debit");
        homeOffer("Floor","Cloud floor");clickExact("Equip",true);check(save().getString("home_floor_v113","").equals("floor_cloud"),"Explicit free floor Equip persists the requested surface");
        homeOffer("Walls","Sage walls");clickExact("Equip",true);check(save().getString("home_wallpaper_v113","").equals("wallpaper_sage")&&coins()==balance-240,"Explicit free wall Equip persists Sage walls without another debit");atHouse();capture("house-personalised-en");
        openJourney(1);restart();JSONObject first=fixture(1).getJSONArray("steps").getJSONObject(0);action(first);check(!session().getString("history","").isEmpty(),"Cold persistence checkpoint includes an actual unfinished puzzle action");capture("house-checkpoint-partial-puzzle-en");atHouse();
        JSONObject checkpoint=new JSONObject().put("room",new JSONObject(roomState())).put("boardAndEconomy",new JSONObject(boardAndEconomy())).put("coins",coins()).put("scope","Observed real UI purchase, placement, surfaces and partial board; no seeded expectations");write("house-checkpoint.json",checkpoint.toString(2));capture("house-final-checkpoint-en");
    }
    private void houseColdVerify()throws Exception{
        JSONObject checkpoint=new JSONObject(readFile(new File(output,"house-checkpoint.json")));atHouse();
        check(new JSONObject(roomState()).toString().equals(checkpoint.getJSONObject("room").toString()),"Exact room layout, owned items, surfaces and wish survive a distinct cold process");
        check(new JSONObject(boardAndEconomy()).toString().equals(checkpoint.getJSONObject("boardAndEconomy").toString()),"Actual current puzzle history, economy and progression survive a distinct cold process");capture("house-cold-restored-en");
    }
    private void houseLocales()throws Exception{
        ensureReducedMotion();atHouse();Map<String,Object> before=boardAndEconomy(),room=roomState();
        String[] ids={"en","fr","de","es","pt","it"},languages={"English","Français","Deutsch","Español","Português","Italiano"};
        String[] homes={"Home","Accueil","Start","Inicio","Início","Home"},decorate={"Decorate","Décorer","Einrichten","Decorar","Decorar","Arreda"};
        String[] dress={"Dress up","Habiller","Anziehen","Vestir","Vestir","Vesti"},hug={"A little hug","Un petit câlin","Eine Umarmung","Un abrazo","Um abraço","Un abbraccio"};
        String[] furniture={"Furniture","Meubles","Möbel","Muebles","Móveis","Mobili"},walls={"Walls","Murs","Wände","Paredes","Paredes","Pareti"},floor={"Floor","Sol","Boden","Suelo","Chão","Pavimento"};
        String[] scenes={"Home scene","Pièce de la maison","Zimmeransicht","Vista de la casa","Vista da casa","Vista della casa"},companions={"Home companion","Compagnon à la maison","Begleiter zu Hause","Compañero en casa","Companheiro em casa","Compagno a casa"};
        String[] bed={"Moonfold bed","Lit Pli de lune","Mondfaltenbett","Cama Pliegue de luna","Cama Dobra de lua","Letto Piega di luna"};
        for(int i=0;i<Math.min(ids.length,Integer.parseInt(args.getString("houseLocaleCount","6")));i++){
            atHouse();click(x->Arrays.asList(SETTINGS).contains(x),false);clickExact(languages[i],true);back();top();final int n=i;
            assertVisibleTouchTarget(x->x.equals(dress[n]),"Localized "+ids[i]+" wardrobe");assertVisibleTouchTarget(x->x.equals(decorate[n]),"Localized "+ids[i]+" decorate");assertVisibleTouchTarget(x->x.equals(hug[n]),"Localized "+ids[i]+" cuddle");
            check(has(x->x.equals(scenes[n]),false)&&has(x->x.equals(companions[n]),false),"Localized "+ids[i]+" room exposes the actual scene and current companion art");capture("house-locale-room-"+ids[i]);
            clickExact(decorate[i],false);clickExact(furniture[i],false);top();for(String tab:new String[]{furniture[i],walls[i],floor[i]})assertVisibleTouchTarget(x->x.equals(tab),"Localized "+ids[i]+" category "+tab);
            capture("house-locale-catalog-"+ids[i]);seekControl(x->x.startsWith(bed[n]+","));touchNode(x->x.startsWith(bed[n]+","),false);
            check(has(x->x.startsWith(bed[n]+","),false),"Localized "+ids[i]+" detail names the observed furniture rather than exposing a copy key");capture("house-locale-detail-"+ids[i]);
            clickExact(homes[i],false);top();click(x->Arrays.asList(SETTINGS).contains(x),false);clickExact("English",true);back();
            check(roomState().equals(room)&&boardAndEconomy().equals(before),"Localized "+ids[i]+" house/category/detail browsing preserves room, current board, ownership and exact economy");
        }atHouse();
    }
    private void assertReachable(Predicate<String> wanted,String label)throws Exception{
        seekHouseControl(wanted);Rect bounds=observedBounds(wanted),game=actualGameBounds();check(game!=null&&game.contains(bounds)&&bounds.width()>0&&bounds.height()>0,label+" has a nonempty native target fully reachable inside the observed viewport");
    }
    private void houseReachability(String suffix)throws Exception{
        atHouse();assertReachable(cuddleControl(),"Cuddle "+suffix);assertReachable(x->x.equals("Dress up"),"Wardrobe "+suffix);assertReachable(x->x.equals("Decorate"),"Decorate "+suffix);top();capture("house-layout-"+suffix);
        String navigationRoom=roomRecord();touchNode(x->x.equals("Inventory"),false);check(has(x->x.equals("Luma Drift · Inventory")||x.equals("Luma Drift · Collection"),false),"Actual footer touch opens Inventory instead of a floor action "+suffix);touchNode(x->x.equals("Home"),false);check(has(x->x.equals("Luma Drift · My little home"),false)&&roomRecord().equals(navigationRoom),"Actual footer Home touch restores the house without moving its companion "+suffix);
        seekHouseControl(x->x.equals("Decorate"));touchNode(x->x.equals("Decorate"),false);assertReachable(x->x.equals("Furniture"),"Furniture catalogue "+suffix);assertReachable(x->x.equals("Done"),"Done "+suffix);top();
        seekHouseControl(x->x.startsWith("Moonfold bed,")&&x.contains("Decorate"));touchNode(x->x.startsWith("Moonfold bed,")&&x.contains("Decorate"),false);
        for(String action:new String[]{"Rotate","Store away","Place","Cancel"})assertReachable(x->x.equals(action),"Room editor "+action+" "+suffix);capture("house-layout-editor-"+suffix);clickExact("Cancel",true);atHouse();
        seekHouseControl(x->x.equals("Decorate"));touchNode(x->x.equals("Decorate"),false);seekHouseControl(x->x.equals("Furniture"));touchNode(x->x.equals("Furniture"),false);for(String tab:new String[]{"Furniture","Walls","Floor"})assertReachable(x->x.equals(tab),"Catalogue "+tab+" "+suffix);
        top();seekControl(x->x.startsWith("Pebble stool,"));check(has(x->x.startsWith("Pebble stool,"),true),"Last furniture card is reachable through ordinary native scrolling "+suffix);capture("house-layout-catalog-last-"+suffix);atHouse();
    }
    private void houseLayout()throws Exception{
        ensureReducedMotion();atHouse();Map<String,Object> room=roomState(),before=boardAndEconomy();if(originalSize==null)originalSize=shellDiagnostic("wm size");
        float density=getTargetContext().getResources().getDisplayMetrics().density;int width=Math.round(320*density),height=Math.round(640*density);
        ui.setRotation(UiAutomation.ROTATION_FREEZE_0);check(shellDiagnostic("wm size "+width+"x"+height).trim().isEmpty(),"Owned QA display is configured to an actual320dp-wide portrait surface");SystemClock.sleep(850);atHouse();
        Rect actual=actualGameBounds();check(actual!=null&&Math.abs(actual.width()/getTargetContext().getResources().getDisplayMetrics().density-320)<=1,"Observed native house viewport is actually320dp wide");houseReachability("compact320-portrait-en");
        RotationFrame viewport=observeRotationFrame();try{rotateAndAwait(UiAutomation.ROTATION_FREEZE_90,viewport,"320dp house landscape");houseReachability("compact320-landscape-en");}
        finally{rotateAndAwait(UiAutomation.ROTATION_FREEZE_0,viewport,"320dp house portrait return");}
        check(session().getBoolean("reduce_motion",false),"Room controls and furniture remain usable with the actual reduced-motion preference enabled");check(roomState().equals(room)&&boardAndEconomy().equals(before),"Compact, landscape, catalogue and uncommitted editor browsing preserve room and actual puzzle/economy state");
    }
    private void firstLightPiece(int row,int column)throws Exception{String[] starts={"Piece, row "+(row+1)+", column "+(column+1)+",","Pièce, ligne "+(row+1)+", colonne "+(column+1)+",","Teil, Zeile "+(row+1)+", Spalte "+(column+1)+",","Pieza, fila "+(row+1)+", columna "+(column+1)+",","Peça, linha "+(row+1)+", coluna "+(column+1)+",","Pezzo, riga "+(row+1)+", colonna "+(column+1)+","};touchNode(x->{for(String start:starts)if(x.startsWith(start))return true;return false;},false);}
    private void firstLight()throws Exception{
        check(!save().getBoolean("first_light_seen_v113",false),"Fresh first-light puzzle has not been completed or skipped");int balance=coins();int clears=actualJourneyClears();capture("first-light-before-en");
        String prior=save().getString("first_light_history_v113","");if("resume".equals(args.getString("firstLightPhase","complete"))){JSONObject checkpoint=new JSONObject(readFile(new File(output,"first-light-checkpoint.json")));check(prior.equals("0")&&checkpoint.getString("history").equals(prior)&&balance==checkpoint.getInt("coins")&&clears==checkpoint.getInt("clears"),"First-light switch history, earned balance and actual campaign clears survive a distinct cold process");capture("first-light-cold-resumed-en");}else{check(prior.isEmpty(),"New first-light qualification starts with no prior piece history");firstLightPiece(2,0);}check(save().getString("first_light_history_v113","").equals("0"),"First real piece exit saves the switch action and opens the right gate");capture("first-light-switch-en");
        android.view.WindowManager wm=(android.view.WindowManager)getTargetContext().getSystemService(Context.WINDOW_SERVICE);int rotation=wm.getDefaultDisplay().getRotation();RotationFrame viewport=observeRotationFrame();try{rotateAndAwait(UiAutomation.ROTATION_FREEZE_90,viewport,"First-light landscape");check(save().getString("first_light_history_v113","").equals("0"),"First-light switch history survives activity recreation");}
        finally{rotateAndAwait(rotation,viewport,"First-light portrait return");}
        if("pause".equals(args.getString("firstLightPhase","complete"))){write("first-light-checkpoint.json",new JSONObject().put("history","0").put("coins",balance).put("clears",clears).put("processId",android.os.Process.myPid()).toString(2));return;}
        firstLightPiece(1,2);firstLightPiece(3,1);check(save().getString("first_light_history_v113","").equals("0,1,2"),"All three real first-light exits persist in their actual order");capture("first-light-solved-en");click(x->x.startsWith("Discover your little home")||x.startsWith("Découvre ton petit chez-toi")||x.startsWith("Entdecke dein kleines Zuhause")||x.startsWith("Descubre tu pequeño hogar")||x.startsWith("Descobre o teu cantinho")||x.startsWith("Scopri la tua casetta"),true);
        check(save().getBoolean("first_light_seen_v113",false)&&coins()==balance&&actualJourneyClears()==clears,"Discovering the house completes first light without fabricating campaign wins or earned currency");capture("first-light-discovered-home-en");
    }

    private static String readFile(File file)throws IOException{try(InputStream in=new FileInputStream(file);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return out.toString("UTF-8");}}
    private void check(boolean value,String message)throws Exception{count++;checks.put(new JSONObject().put("pass",value).put("message",message));if(!value)throw new AssertionError(message);Bundle status=new Bundle();status.putString("stream","PASS "+message+"\n");sendStatus(0,status);}
    private void capture(String title)throws Exception{
        if(!allowV18&&!"true".equals(args.getString("captureAll","false"))){
            if(title.matches("tutorial-[1-7]-phase-[1-9]-en")||title.startsWith("locale-map-"))return;
            if(title.matches("map-(7|8|100|101|500)-en"))return;
        }
        SystemClock.sleep(100);Bitmap pixels=ui.takeScreenshot();if(pixels==null)throw new IOException("SDK screenshot unavailable");try(OutputStream f=new FileOutputStream(new File(output,title+".png"))){if(!pixels.compress(Bitmap.CompressFormat.PNG,100,f))throw new IOException("PNG failed");}finally{pixels.recycle();}
        JSONArray snapshot=new JSONArray();for(AccessibilityNodeInfo n:nodes()){Rect box=new Rect();n.getBoundsInScreen(box);snapshot.put(new JSONObject().put("name",name(n)).put("clickable",n.isClickable()).put("enabled",n.isEnabled()).put("selected",n.isSelected()).put("class",String.valueOf(n.getClassName())).put("bounds",box.toShortString()));n.recycle();}write(title+".json",snapshot.toString(2));captures.put(title);
    }
    private void write(String name,String text)throws IOException{try(OutputStream f=new FileOutputStream(new File(output,name))){f.write(text.getBytes(StandardCharsets.UTF_8));}}
    private static String stack(Throwable failure){StringWriter text=new StringWriter();failure.printStackTrace(new PrintWriter(text));return text.toString();}
}
