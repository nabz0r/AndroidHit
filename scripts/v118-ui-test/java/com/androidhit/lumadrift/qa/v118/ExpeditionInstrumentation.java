package com.androidhit.lumadrift.qa.v118;

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

/** SDK-only native observer: no target imports, reflection, preference editor or solver. */
public final class ExpeditionInstrumentation extends Instrumentation {
    private Bundle args;private UiAutomation ui;private File output;private String target;
    private volatile Activity currentActivity;
    private final JSONArray checks=new JSONArray(),actions=new JSONArray(),captures=new JSONArray();
    private int touches;
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);args=arguments==null?new Bundle():arguments;start();}
    @Override public void callActivityOnCreate(Activity activity,Bundle state){super.callActivityOnCreate(activity,state);currentActivity=activity;}
    @Override public void onStart(){new Thread(()->{
        boolean passed=false;Bundle result=new Bundle();
        try{
            target=getTargetContext().getPackageName();check(target.equals("com.androidhit.lumadrift")||target.equals("com.androidhit.lumadrift.sandbox"),"Exact Luma target package");
            check(args.getString("runToken","").matches("[a-f0-9-]{36}"),"Unique host execution token");
            String phase=args.getString("phase","");check(phase.matches("[a-z][a-z0-9_-]{1,50}"),"Bounded named qualification phase");
            output=new File(getTargetContext().getExternalFilesDir(null),"v118-native/"+args.getString("runToken")+"/"+phase);check(!output.exists()&&output.mkdirs(),"Fresh native output directory");
            PackageInfo info=getTargetContext().getPackageManager().getPackageInfo(target,0);check(info.versionName.equals(args.getString("expectedVersion"))&&info.versionCode==Integer.parseInt(args.getString("expectedCode")),"Exact installed version/code");
            ui=getUiAutomation();AccessibilityServiceInfo service=ui.getServiceInfo();service.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS|AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;ui.setServiceInfo(service);
            snapshot("before");
            if(!phase.equals("snapshot")){
                launch();
                if(phase.equals("baseline-play")||phase.equals("baseline-continue"))baselinePlay();
                else if(phase.equals("cold"))capture("cold-resume");
                else if(phase.equals("fresh-firstlight"))firstLight();
                else scenario(phase);
            }
            snapshot("after");passed=true;result.putString("summary","PASS "+checks.length()+" native checks and "+touches+" actual touches");
        }catch(Throwable failure){result.putString("failure",failure.toString());try{if(output!=null){StringWriter stack=new StringWriter();failure.printStackTrace(new PrintWriter(stack));write("failure.txt",stack.toString());if(ui!=null)capture("failure");}}catch(Throwable ignored){}}
        finally{try{if(ui!=null)ui.setRotation(UiAutomation.ROTATION_UNFREEZE);}catch(Throwable ignored){}try{if(output!=null)write("report.json",new JSONObject().put("passed",passed).put("phase",args.getString("phase")).put("runToken",args.getString("runToken")).put("processId",android.os.Process.myPid()).put("checkCount",checks.length()).put("checks",checks).put("touchCount",touches).put("actions",actions).put("captures",captures).put("package",target).put("scope","Actual accessibility-bounded SDK touchscreen UI and read-only game/meta/session preference observations. Earned-coin unlocks use the real UI. No target imports, reflection, solver, fixture writes, external purchases, ads or external identity. Mini LoL is local prototype fan art; no publication or license claim.").toString(2));}catch(Throwable ignored){}finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);}
    },"luma-v118-native").start();}
    private SharedPreferences save(){return getTargetContext().getSharedPreferences("luma_drift_save_v1",0);}
    private SharedPreferences session(){return getTargetContext().getSharedPreferences("luma_session",0);}
    private void launch(){startActivitySync(new Intent().setClassName(target,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));SystemClock.sleep(1500);}
    private void firstLight()throws Exception{
        check(!save().getBoolean("first_light_seen_v113",false),"Genuinely fresh guest has not completed or skipped first light");
        check(save().getString("first_light_history_v113","").isEmpty(),"Genuinely new first-light history");capture("firstlight-new");
        piece(2,0);piece(1,2);piece(3,1);check(save().getString("first_light_history_v113","").equals("0,1,2"),"All three real first-light exits persisted");capture("firstlight-solved");
        tap(x->x.startsWith("Discover your little home")||x.startsWith("Découvre ton petit chez-toi"),"complete-firstlight");
        check(save().getBoolean("first_light_seen_v113",false),"First-light completion durably stored");
    }
    private void baselinePlay()throws Exception{
        if(args.getString("phase").equals("baseline-play"))firstLight();else check(save().getBoolean("first_light_seen_v113",false),"Continuation retains the genuinely completed first light from its original failed observer run");
        tap(x->x.equals("Settings")||x.equals("Paramètres"),"open-settings");tap(x->x.equals("English"),"select-English");back();
        tap(x->x.equals("All levels"),"open-all-levels");tap(x->x.startsWith("Level 1,")||x.equals("Level 1"),"open-U1");
        tap(x->x.startsWith("Play"),"begin-visible-U1-briefing");solve(1);check(save().getInt("stars_1",0)>0,"Genuine U1 clear earned through native piece touches");capture("genuine-u1-victory");
        back();snapshot("legacy-baseline");
    }
    private void scenario(String phase)throws Exception{
        JSONArray steps=new JSONObject(asset("scenarios.json")).getJSONArray(phase);
        for(int n=0;n<steps.length();n++){
            JSONObject step=steps.getJSONObject(n);String op=step.getString("op");
            if(op.equals("tap")){String expression=step.getString("regex");tap(x->x.matches(expression),step.optString("reason",expression));}
            else if(op.equals("tap-id"))seekId(step.getString("id"),step.optString("reason",step.getString("id")));
            else if(op.equals("tap-scroll")){String expression=step.getString("regex");seekTap(x->x.matches(expression),step.optString("reason",expression));}
            else if(op.equals("assert-label")){String expression=step.getString("regex");AccessibilityNodeInfo found=find(x->x.matches(expression),false);boolean visible=found!=null;if(found!=null)found.recycle();check(visible,"Visible label: "+expression);}
            else if(op.equals("assert-label-scroll"))seekLabel(step.getString("regex"));
            else if(op.equals("capture"))capture(step.getString("name"));
            else if(op.equals("snapshot"))snapshot(step.getString("name"));
            else if(op.equals("solve"))solve(step.getInt("level"));
            else if(op.equals("solve-current")){int level=boardSession().getInt("level",-1);check(level>=1&&level<=30,"Actual selected board has a frozen U1-U30 certificate");solve(level);}
            else if(op.equals("back"))back();
            else if(op.equals("scroll"))scroll(step.optBoolean("forward",true));
            else if(op.equals("top"))top();
            else if(op.equals("recreate"))recreate(step.getString("name"));
            else if(op.equals("victory-route")){AccessibilityNodeInfo next=find(x->x.startsWith("Next")||x.startsWith("Suivant"),true);if(next!=null){touchNode(next,"return-from-real-victory");}else{AccessibilityNodeInfo journal=find(x->x.startsWith("Luma Drift · Expeditions")||x.startsWith("Luma Drift · Expéditions"),false);boolean restored=journal!=null;if(journal!=null)journal.recycle();check(restored,"Completed expedition recreation safely restores its actual route journal");}}
            else if(op.equals("firstlight"))firstLight();
            else if(op.equals("assert-pref")){String pref=step.optString("pref","luma_drift_save_v1");Object value=getTargetContext().getSharedPreferences(pref,0).getAll().get(step.getString("key"));check(String.valueOf(value).equals(step.getString("value")),"Preference observation "+step.getString("key")+"="+step.getString("value"));}
            else if(op.equals("assert-same-snapshot")){String saved=read(step.getString("name")+".json");snapshot("compare-current");check(saved.equals(read("compare-current.json")),"Complete read-only state is unchanged from "+step.getString("name"));}
            else if(op.equals("assert-run"))assertRun(step);
            else if(op.equals("assert-reward"))assertReward(step);
            else if(op.equals("adventure"))adventure();
            else if(op.equals("motion-cycle"))motionCycle(step.getString("character"),step.optString("capturePrefix","studio"));
            else if(op.equals("motion-forms"))motionForms(step.getString("character"));
            else if(op.equals("guest-pages"))guestPages();
            else if(op.equals("open-companion"))openCompanion(step.getString("id"),step.getString("name"));
            else if(op.equals("unlock-companion"))unlockCompanion(step.getString("id"),step.getString("name"),step.getInt("price"));
            else if(op.equals("earn-campaign"))earnCampaign(step.getInt("from"),step.getInt("through"),step.getInt("baseCoins"));
            else if(op.equals("finish-expedition"))finishExpedition();
            else if(op.equals("remember-branch"))rememberBranch(step.getString("id"),step.getString("name"));
            else if(op.equals("assert-branch-changed"))assertBranchChanged(step.getString("id"),step.getString("name"));
            else if(op.equals("assert-selected-branch"))assertSelectedBranch(step.getString("name"));
            else if(op.equals("assert-three-routes")){Set<Integer> boards=new HashSet<>();JSONArray observed=new JSONArray();for(String id:new String[]{"expedition_left","expedition_right","expedition_third"}){String label=branchLabel(id);int board=branchBoard(label);check(boards.add(board),"Project route announces a distinct actual puzzle "+board);observed.put(new JSONObject().put("id",id).put("board",board).put("label",label));}write("three-project-routes.json",observed.toString(2));}
            else if(op.equals("assert-id")){seekBranchVisible(step.getString("id"));AccessibilityNodeInfo found=findId(step.getString("id"));boolean visible=found!=null;if(found!=null)found.recycle();check(visible,"Visible actual semantic target "+step.getString("id"));}
            else throw new IllegalArgumentException("Unknown qualification operation: "+op);
        }
    }
    private void assertReward(JSONObject expected)throws Exception{
        String[] run=save().getString("expedition_v118_run","").split("\\|",-1);check(run.length==8,"Completed reward belongs to a durable current expedition");
        byte[] digest=java.security.MessageDigest.getInstance("SHA-256").digest(run[1].getBytes(StandardCharsets.UTF_8));StringBuilder hex=new StringBuilder();for(byte value:digest)hex.append(String.format(Locale.ROOT,"%02x",value&255));
        String key="expedition_v118_reward_"+hex;String[] receipt=save().getString(key,"").split("\\|",-1);check(receipt.length==11&&receipt[0].equals(run[1]),"Atomic reward receipt belongs to this exact run");
        check(save().getBoolean("owned_character_"+receipt[2],false),"Actual random discovery is durably owned");
        int discoveryAfter=Integer.parseInt(receipt[6]);check(save().getInt("companion_copies_v118_"+receipt[2],-1)==discoveryAfter,"Discovery copy balance equals its atomic receipt");
        if(!receipt[1].isEmpty()){
            int targetAfter=Integer.parseInt(receipt[4]);int expectedAfter=receipt[1].equals(receipt[2])?discoveryAfter:targetAfter;
            check(save().getInt("companion_copies_v118_"+receipt[1],-1)==expectedAfter,"Target copy balance equals the composed atomic receipt");
            check(targetAfter-Integer.parseInt(receipt[3])==1,"Finishing the copies project genuinely awards its announced one target copy");
        }
        if(expected.has("completed"))check(save().getInt("expedition_v118_completed",0)==expected.getInt("completed"),"Exactly the expected completed expedition count is durable");
        if(expected.has("coins"))check(save().getInt("coins",-1)==expected.getInt("coins"),"Expedition does not silently spend the retained wallet");
        write("reward-observation-"+checks.length()+".json",new JSONObject().put("receiptKey",key).put("receipt",new JSONArray(Arrays.asList(receipt))).toString(2));
    }
    private void assertRun(JSONObject expected)throws Exception{
        String[] fields=save().getString("expedition_v118_run","").split("\\|",-1);check(fields.length==8,"Current expedition has its complete durable event record");
        JSONObject observed=new JSONObject().put("id",fields[1]).put("companion",fields[3]).put("level",Integer.parseInt(fields[4])).put("goal",fields[5]).put("target",fields[6]);
        int wins=0,skills=0,choices=0,claims=0;for(String event:fields[7].split(";")){if(event.startsWith("W,"))wins++;if(event.startsWith("S,"))skills++;if(event.startsWith("C,"))choices++;if(event.equals("Q"))claims++;}
        observed.put("wins",wins).put("skills",skills).put("choices",choices).put("claims",claims);write("run-observation-"+checks.length()+".json",observed.toString(2));
        for(Iterator<String> keys=expected.keys();keys.hasNext();){String key=keys.next();if(!key.equals("op"))check(observed.has(key)&&String.valueOf(observed.get(key)).equals(String.valueOf(expected.get(key))),"Durable expedition "+key+"="+expected.get(key));}
    }
    private void adventure()throws Exception{
        for(int attempt=0;attempt<18;attempt++){AccessibilityNodeInfo levels=find(x->x.equals("All levels")||x.equals("Tous les niveaux"),true);if(levels!=null){levels.recycle();return;}back();}
        throw new AssertionError("Cannot return to actual adventure through native back navigation: "+labels());
    }
    private void visibleLabel(String expression)throws Exception{AccessibilityNodeInfo node=find(x->x.matches(expression),false);boolean visible=node!=null;if(node!=null)node.recycle();check(visible,"Actual semantic preview label "+expression);}
    private void seekLabel(String expression)throws Exception{for(int direction=0;direction<2;direction++)for(int attempt=0;attempt<(direction==0?18:40);attempt++){AccessibilityNodeInfo node=find(x->x.matches(expression),false);if(node!=null){node.recycle();check(true,"Actual scroll-reachable label "+expression);return;}String before=labels();scroll(direction==1);if(labels().equals(before))break;}throw new AssertionError("Missing scroll-reachable label "+expression+": "+labels());}
    private void motionCycle(String character,String capturePrefix)throws Exception{
        check(capturePrefix.matches("[a-z][a-z0-9_-]{0,45}"),"Bounded distinct native pose capture prefix");
        int wallet=save().getInt("coins",-1),copies=save().getInt("companion_copies_v118_"+character.toLowerCase(Locale.ROOT),0);String equipped=save().getString("character","");
        String[] names={"Front","Back","Left","Right","Walking · Front","Walking · Back","Walking · Left","Walking · Right","Jump","Landing","Hug","Crouch","Happy","Sad","Surprised","Thinking","Calm","Ready","Guard","Proud"};
        seekId("motion_stance_calm","select-real-calm-before-pose-cycle");seekId("motion_form_0","select-first-authored-form");top();JSONArray visited=new JSONArray();
        for(int n=0;n<20;n++){int pose=(16+n)%20;if(n>0)seekId("motion_next","actual-next-pose-"+pose);top();visibleLabel(java.util.regex.Pattern.quote(character+". "+names[pose]+". ")+".*");capture(capturePrefix+"-pose-"+String.format(Locale.ROOT,"%02d",pose));visited.put(new JSONObject().put("pose",pose).put("semanticName",names[pose]));}
        check(save().getInt("coins",-1)==wallet&&save().getInt("companion_copies_v118_"+character.toLowerCase(Locale.ROOT),0)==copies&&save().getString("character","").equals(equipped),"Twenty actual pose previews preserve wallet, copies and equipped identity");write(capturePrefix.equals("studio")?"motion-poses-observed.json":capturePrefix+"-poses-observed.json",visited.toString(2));
    }
    private void motionForms(String character)throws Exception{
        int wallet=save().getInt("coins",-1);Object copies=save().getAll().get("companion_copies_v118_"+character.toLowerCase(Locale.ROOT));String equipped=save().getString("character","");
        for(int form=0;form<4;form++){seekId("motion_form_"+form,"preview-authored-form-"+form);top();String level=String.valueOf(new int[]{1,3,6,10}[form]);if(form>0)visibleLabel(java.util.regex.Pattern.quote(character+". ")+".*Preview at level "+level+" · your saved level stays the same\\..*");capture("studio-evolution-form-"+form);}
        check(save().getInt("coins",-1)==wallet&&Objects.equals(save().getAll().get("companion_copies_v118_"+character.toLowerCase(Locale.ROOT)),copies)&&save().getString("character","").equals(equipped),"Four actual evolution previews do not grant copies, spend coins or equip a different identity");seekId("motion_form_0","restore-actual-first-form-preview");
    }
    private void guestPages()throws Exception{
        String[][] names={{"Ashe","Ezreal","Soraka","Ahri"},{"Jinx","Lux","Teemo","Yasuo"},{"Annie","Lulu","Sona","Miss Fortune"},{"Vi"}};JSONArray observed=new JSONArray();
        for(int page=0;page<4;page++){top();Set<String> seen=new TreeSet<>();for(int scan=0;scan<6;scan++){for(AccessibilityNodeInfo node:nodes()){if(node.isClickable()&&node.isVisibleToUser())for(String expected:names[page])if(name(node).startsWith(expected+", "))seen.add(expected);node.recycle();}String before=labels();scroll(true);if(labels().equals(before))break;}
            check(seen.equals(new TreeSet<>(Arrays.asList(names[page]))),"All actual Mini LoL names reachable on page "+(page+1)+": "+seen);top();capture("mini-lol-page-"+(page+1));observed.put(new JSONObject().put("page",page+1).put("names",new JSONArray(seen)));if(page<3)seekTap(x->x.equals("›"),"actual-next-guest-page");}
        for(int page=0;page<3;page++)seekTap(x->x.equals("‹"),"return-to-first-guest-page");top();write("guest-pages-observed.json",observed.toString(2));
    }
    private void openCompanion(String id,String title)throws Exception{
        check(id.matches("[a-z][a-z0-9_]{1,40}")&&!title.isEmpty(),"Bounded named companion UI target");adventure();seekTap(x->x.matches("My home|Ma maison|Home|Maison"),"open-real-home");seekTap(x->x.matches("Collection|Collection book|Lookbook|Look book|Mon carnet|Carnet de collection"),"open-real-collection-book");seekTap(x->x.matches("My companions|Mes compagnons|Companions|Compagnons"),"open-real-companion-inventory");
        if(id.startsWith("lol_"))seekTap(x->x.equals("Mini LoL"),"select-actual-Mini-LoL-filter");seekTap(x->x.startsWith(title+", "),"open-actual-companion-"+id);
    }
    private void unlockCompanion(String id,String title,int price)throws Exception{
        openCompanion(id,title);boolean owned=save().getBoolean("owned_character_"+id,false);int before=save().getInt("coins",-1);
        if(!owned){check(price>0&&before>=price,"Companion unlock is covered by genuinely earned wallet");seekTap(x->x.equals(price+" LumaCoins"),"actual-earned-coins-purchase-"+id);check(save().getBoolean("owned_character_"+id,false)&&save().getInt("coins",-1)==before-price,"Actual UI purchase owns "+id+" after exact earned debit");}
        else check(save().getInt("coins",-1)==before,"Existing random discovery ownership requires no duplicate purchase");
    }
    private void earnCampaign(int from,int through,int baseCoins)throws Exception{
        check(from>=2&&through>=from&&through<=30,"Earned funding uses only frozen public U2-U30 certificates");check(save().getInt("stars_"+(from-1),0)==3,"Preceding campaign puzzle was genuinely cleared at three stars");int earned=0;
        for(int level=from;level<=through;level++){int stars=save().getInt("stars_"+level,0);check(stars==0||stars==3,"Funding resumes only an untouched or three-star native campaign board "+level);if(stars==3)earned+=55;}
        check(save().getInt("coins",-1)==baseCoins+earned,"Funding wallet equals baseline plus actual existing first-clear rewards; no synthetic credit");
        for(int level=from;level<=through;level++){if(save().getInt("stars_"+level,0)==3)continue;final int selected=level;adventure();seekTap(x->x.equals("All levels")||x.equals("Tous les niveaux"),"open-real-campaign-map");seekTap(x->x.startsWith("Level "+selected+",")||x.equals("Level "+selected),"open-real-U"+selected);seekTap(x->x.startsWith("Play"),"begin-real-U"+selected);check(session().getInt("mode",-1)==0,"Funding is real campaign mode, not an expedition reward");int before=save().getInt("coins",-1);solve(selected);check(save().getInt("stars_"+selected,0)==3&&save().getInt("coins",-1)==before+55,"Actual first campaign victory earns exactly55 coins for U"+selected);earned+=55;}
        check(save().getInt("coins",-1)==baseCoins+55*(through-from+1),"All funding comes from the announced real campaign clears");capture("earned-campaign-through-u"+through);adventure();
    }
    private void finishExpedition()throws Exception{
        for(int stage=0;stage<4;stage++){String[] run=save().getString("expedition_v118_run","").split("\\|",-1);check(run.length==8,"Actual expedition record available to finish");int wins=0,choices=0;for(String event:run[7].split(";")){if(event.startsWith("W,"))wins++;if(event.startsWith("C,"))choices++;}if(wins==4)return;check(choices==wins||choices==wins+1,"Actual route has at most one selected unsolved stage");seekId(choices>wins?"expedition_continue":"expedition_left","select-actual-expedition-stage-"+wins);int level=boardSession().getInt("level",-1);check(level>=1&&level<=30,"Selected expedition board has existing public certificate");solve(level);tap(x->x.startsWith("Next")||x.startsWith("Suivant"),"real-expedition-victory-return");}
        assertRun(new JSONObject().put("op","assert-run").put("wins",4).put("claims",0));
    }
    private String branchLabel(String id)throws Exception{seekBranchVisible(id);AccessibilityNodeInfo node=findId(id);check(node!=null,"Actual route branch visible "+id);String label=name(node);node.recycle();return label;}
    private void seekBranchVisible(String id)throws Exception{top();for(int i=0;i<35;i++){AccessibilityNodeInfo node=findId(id);if(node!=null){node.recycle();return;}String before=labels();scroll(true);if(labels().equals(before))break;}}
    private void rememberBranch(String id,String key)throws Exception{write("branch-"+key+".json",new JSONObject().put("id",id).put("label",branchLabel(id)).toString(2));}
    private int branchBoard(String label)throws Exception{java.util.regex.Matcher board=java.util.regex.Pattern.compile("Puzzle (\\d+)").matcher(label);check(board.find(),"Actual visible branch announces its puzzle number");return Integer.parseInt(board.group(1));}
    private void assertBranchChanged(String id,String key)throws Exception{JSONObject before=new JSONObject(read("branch-"+key+".json"));String after=branchLabel(id);check(branchBoard(after)!=branchBoard(before.getString("label")),"Actual reroute changes the announced puzzle number");write("branch-"+key+"-after.json",new JSONObject().put("id",id).put("label",after).toString(2));}
    private void assertSelectedBranch(String key)throws Exception{String path="branch-"+key+(new File(output,"branch-"+key+"-after.json").exists()?"-after":"")+".json";String label=new JSONObject(read(path)).getString("label");check(boardSession().getInt("level",-1)==branchBoard(label),"Choice selects the actual announced native board");}
    private void solve(int number)throws Exception{
        JSONObject fixture=new JSONObject(asset("fixtures/u"+number+".json"));JSONArray steps=fixture.getJSONArray("steps");
        SharedPreferences actual=boardSession();check(actual.getInt("level",-1)==number,"Visible board is requested immutable U"+number);
        check(actual.getInt("content_version",-1)==fixture.getInt("contentVersion")&&actual.getInt("budget_policy",-1)==fixture.getInt("policy"),"Actual U"+number+" session preserves the frozen content and budget identities");
        String prefix="board-"+captures.length()+"-u"+number;capture(prefix+"-before");String history="";
        for(int i=0;i<steps.length();i++){JSONObject step=steps.getJSONObject(i);piece(step.getInt("row"),step.getInt("col"));history+=(i==0?"":",")+step.getInt("action");check(actual.getString("history","").equals(history),"Actual U"+number+" exactly-once persisted certificate exit "+(i+1));}
        check(actual.getInt("budget_used",-1)==steps.length(),"Actual U"+number+" consumes precisely its real certificate actions");
        check(!hasPieces(),"Frozen native U"+number+" certificate exits all pieces");capture(prefix+"-solved");
    }
    private SharedPreferences boardSession(){SharedPreferences expedition=getTargetContext().getSharedPreferences("luma_expedition_session_v118",0);return expedition.getBoolean("active",false)&&expedition.getInt("mode",-1)==5?expedition:session();}
    private boolean hasPieces(){AccessibilityNodeInfo node=find(x->x.contains("Piece, row ")||x.contains("Pièce, ligne "),false);if(node==null)return false;node.recycle();return true;}
    private void piece(int row,int col)throws Exception{
        String en="Piece, row "+(row+1)+", column "+(col+1)+",";String fr="Pièce, ligne "+(row+1)+", colonne "+(col+1)+",";
        tap(x->x.contains(en)||x.contains(fr),"certificate-piece-"+row+"-"+col);
    }
    private void recreate(String name)throws Exception{
        final Activity before=currentActivity;check(before!=null,"Framework observed the current Activity before recreation");runOnMainSync(()->before.recreate());
        long end=SystemClock.uptimeMillis()+10000;while((currentActivity==before||currentActivity==null)&&SystemClock.uptimeMillis()<end)SystemClock.sleep(80);
        check(currentActivity!=null&&currentActivity!=before,"Android created a genuinely different Activity instance");SystemClock.sleep(1100);capture(name+"-recreated");
        actions.put(new JSONObject().put("lifecycle","Android Activity.recreate").put("beforeInstance",System.identityHashCode(before)).put("afterInstance",System.identityHashCode(currentActivity)));
    }
    private void snapshot(String name)throws Exception{
        JSONObject result=new JSONObject();for(String pref:new String[]{"luma_drift_save_v1","luma_drift_meta_v1","luma_session","luma_expedition_session_v118"}){
            JSONObject values=new JSONObject();for(Map.Entry<String,?> entry:new TreeMap<>(getTargetContext().getSharedPreferences(pref,0).getAll()).entrySet()){
                if(entry.getKey().toLowerCase(Locale.ROOT).contains("seed"))continue;Object value=entry.getValue();
                if(value instanceof Set){List<String> sorted=new ArrayList<>();for(Object x:(Set<?>)value)sorted.add(String.valueOf(x));Collections.sort(sorted);value=new JSONArray(sorted);}values.put(entry.getKey(),value);
            }result.put(pref,values);
        }write(name+".json",result.toString(2));
    }
    private static String name(AccessibilityNodeInfo node){CharSequence value=node.getContentDescription();if(value==null||value.length()==0)value=node.getText();return value==null?"":value.toString();}
    private List<AccessibilityNodeInfo> nodes(){if(Build.VERSION.SDK_INT>=33)ui.clearCache();List<AccessibilityNodeInfo> result=new ArrayList<>();AccessibilityNodeInfo root=ui.getRootInActiveWindow();if(root==null)return result;ArrayDeque<AccessibilityNodeInfo> pending=new ArrayDeque<>();pending.add(root);while(!pending.isEmpty()&&result.size()<800){AccessibilityNodeInfo node=pending.removeFirst();result.add(node);for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo child=node.getChild(i);if(child!=null)pending.addLast(child);}}return result;}
    private AccessibilityNodeInfo find(Predicate<String> match,boolean clickable){AccessibilityNodeInfo found=null;for(AccessibilityNodeInfo node:nodes()){Rect bounds=new Rect();node.getBoundsInScreen(bounds);if(found==null&&node.isVisibleToUser()&&node.isEnabled()&&(!clickable||node.isClickable())&&!bounds.isEmpty()&&match.test(name(node)))found=AccessibilityNodeInfo.obtain(node);node.recycle();}return found;}
    private String labels(){List<String> labels=new ArrayList<>();for(AccessibilityNodeInfo node:nodes()){if(!name(node).isEmpty())labels.add(name(node));node.recycle();}return labels.toString();}
    private void foreground(){AccessibilityNodeInfo root=ui.getRootInActiveWindow();String pkg=root==null?"":String.valueOf(root.getPackageName());if(root!=null)root.recycle();if(!target.equals(pkg))throw new AssertionError("Refuse input outside target package: "+pkg);}
    private void tap(Predicate<String> match,String reason)throws Exception{
        AccessibilityNodeInfo node=null;for(int attempt=0;attempt<30&&node==null;attempt++){node=find(match,true);if(node==null)SystemClock.sleep(100);}
        if(node==null)throw new AssertionError("Missing native target "+reason+": "+labels());touchNode(node,reason);
    }
    private void touchNode(AccessibilityNodeInfo node,String reason)throws Exception{
        foreground();Rect box=new Rect();node.getBoundsInScreen(box);String label=name(node);String semantic=node.getViewIdResourceName();node.recycle();
        long down=SystemClock.uptimeMillis();inject(down,MotionEvent.ACTION_DOWN,box.exactCenterX(),box.exactCenterY());SystemClock.sleep(25);inject(down,MotionEvent.ACTION_UP,box.exactCenterX(),box.exactCenterY());touches++;
        actions.put(new JSONObject().put("input","SDK touchscreen").put("reason",reason).put("label",label).put("resourceId",semantic).put("bounds",new JSONArray(new int[]{box.left,box.top,box.right,box.bottom})));SystemClock.sleep(400);
    }
    private void seekTap(Predicate<String> match,String reason)throws Exception{
        for(int i=0;i<16;i++){AccessibilityNodeInfo node=find(match,true);if(node!=null){node.recycle();tap(match,reason);return;}String before=labels();scroll(false);if(labels().equals(before))break;}
        for(int i=0;i<32;i++){AccessibilityNodeInfo node=find(match,true);if(node!=null){node.recycle();tap(match,reason);return;}String before=labels();scroll(true);if(labels().equals(before))break;}
        throw new AssertionError("Missing scrollable native target "+reason+": "+labels());
    }
    private AccessibilityNodeInfo findId(String id){AccessibilityNodeInfo found=null;for(AccessibilityNodeInfo node:nodes()){Rect box=new Rect();node.getBoundsInScreen(box);if(found==null&&node.isVisibleToUser()&&node.isEnabled()&&node.isClickable()&&!box.isEmpty()&&(target+":id/"+id).equals(node.getViewIdResourceName()))found=AccessibilityNodeInfo.obtain(node);node.recycle();}return found;}
    private void top(){for(int i=0;i<20;i++){String before=labels();scroll(false);if(labels().equals(before))break;}}
    private void seekId(String id,String reason)throws Exception{AccessibilityNodeInfo node=findId(id);if(node!=null){touchNode(node,reason);return;}top();for(int i=0;i<40;i++){node=findId(id);if(node!=null){touchNode(node,reason);return;}String before=labels();scroll(true);if(labels().equals(before))break;}throw new AssertionError("Missing semantic native target "+id+": "+labels());}
    private void inject(long down,int action,float x,float y){MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);try{if(!ui.injectInputEvent(event,true))throw new AssertionError("SDK touch refused");}finally{event.recycle();}}
    private void back(){foreground();ui.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);SystemClock.sleep(400);}
    private void scroll(boolean forward){foreground();AccessibilityNodeInfo root=ui.getRootInActiveWindow();boolean sent=false;if(root!=null){sent=root.performAction(forward?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);root.recycle();}if(!sent)for(AccessibilityNodeInfo node:nodes()){if(!sent&&node.isScrollable())sent=node.performAction(forward?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);node.recycle();}SystemClock.sleep(250);}
    private void capture(String title)throws Exception{
        // Portrait scenes may decode new original art on the first frame. Allow the
        // real app's transition to present before attributing a screenshot to its destination.
        SystemClock.sleep(1600);foreground();Bitmap bitmap=ui.takeScreenshot();if(bitmap==null)throw new IOException("Native screenshot unavailable");try(OutputStream file=new FileOutputStream(new File(output,title+".png"))){if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,file))throw new IOException("PNG encoding failed");}finally{bitmap.recycle();}
        JSONArray observed=new JSONArray();for(AccessibilityNodeInfo node:nodes()){Rect box=new Rect();node.getBoundsInScreen(box);observed.put(new JSONObject().put("label",name(node)).put("resourceId",node.getViewIdResourceName()).put("bounds",new JSONArray(new int[]{box.left,box.top,box.right,box.bottom})).put("clickable",node.isClickable()).put("visible",node.isVisibleToUser()));node.recycle();}write(title+"-accessibility.json",observed.toString(2));captures.put(title);
    }
    private void check(boolean condition,String message)throws Exception{checks.put(new JSONObject().put("passed",condition).put("message",message));if(!condition)throw new AssertionError(message);}
    private String asset(String name)throws Exception{try(InputStream input=getContext().getAssets().open(name);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] bytes=new byte[8192];int n;while((n=input.read(bytes))!=-1)out.write(bytes,0,n);return out.toString("UTF-8");}}
    private String read(String name)throws Exception{try(InputStream input=new FileInputStream(new File(output,name));ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] bytes=new byte[8192];int n;while((n=input.read(bytes))!=-1)out.write(bytes,0,n);return out.toString("UTF-8");}}
    private void write(String name,String text)throws Exception{try(OutputStream file=new FileOutputStream(new File(output,name))){file.write(text.getBytes(StandardCharsets.UTF_8));}}
}
