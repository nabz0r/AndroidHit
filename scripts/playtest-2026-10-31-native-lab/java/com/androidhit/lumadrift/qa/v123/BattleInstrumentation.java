package com.androidhit.lumadrift.qa.v123;

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
import android.util.Base64;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

/** Framework-only observer: real UI touches and read-only preferences, never target methods or writes. */
public final class BattleInstrumentation extends Instrumentation {
    private Bundle args;private UiAutomation ui;private File output;private String target;
    private final JSONArray checks=new JSONArray(),actions=new JSONArray(),captures=new JSONArray();
    private final Map<String,String> remembered=new TreeMap<>();private int touches;
    private static final Set<String> ALLOWED_OPS=new HashSet<>(Arrays.asList("snapshot","capture","capture-fast","adventure","language","assert-pref","assert-label","assert-id","remember-value","assert-value-unchanged","tap","tap-id","back","top","scroll","wait","firstlight","earn-through","open-level","solve","solve-active","piece","drain-energy","assert-energy","assert-int-delta","assert-victory"));
    private static final String[] CODES={"en","fr","de","es","pt","it"};
    private static final String[] LANGUAGES={"English","Français","Deutsch","Español","Português","Italiano"};
    private static final String[] SETTINGS={"Settings","Réglages","Einstellungen","Ajustes","Definições","Impostazioni"};
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);args=arguments==null?new Bundle():arguments;start();}
    @Override public void onStart(){new Thread(()->{
        boolean passed=false;Bundle result=new Bundle();
        try{
            target=getTargetContext().getPackageName();check(target.equals("com.androidhit.lumadrift.sandbox"),"Exact separate Lab package");
            String token=args.getString("runToken","");check(token.matches("[a-f0-9-]{36}"),"Unique host execution token");
            String phase=args.getString("phase","");check(phase.matches("[a-z][a-z0-9_-]{1,50}"),"Bounded explicit phase");
            output=new File(getTargetContext().getExternalFilesDir(null),"v123-native-lab/"+token+"/"+phase);check(!output.exists()&&output.mkdirs(),"Fresh evidence output");
            PackageInfo info=getTargetContext().getPackageManager().getPackageInfo(target,0);
            check(info.versionName.equals(args.getString("expectedVersion"))&&info.versionCode==Integer.parseInt(args.getString("expectedCode")),"Exact installed version and code");
            ui=getUiAutomation();AccessibilityServiceInfo service=ui.getServiceInfo();service.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS|AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;ui.setServiceInfo(service);
            snapshot("before");
            if(args.getString("fresh","false").equals("true"))check(save().getAll().isEmpty()&&session().getAll().isEmpty(),"Genuine first launch has no game or board preferences");
            if(!args.getString("snapshotOnly","false").equals("true")){
                launch();
                String program=new String(Base64.decode(args.getString("stepsBase64",""),Base64.NO_WRAP),StandardCharsets.UTF_8);
                JSONArray steps=new JSONArray(program);check(steps.length()>0&&steps.length()<=250,"Explicit bounded gesture program");write("requested-steps.json",steps.toString(2));
                for(int n=0;n<steps.length();n++)step(steps.getJSONObject(n));
            }else check(true,"Snapshot-only phase never launches an Activity or sends UI input");
            snapshot("after");passed=true;result.putString("summary","PASS "+checks.length()+" native assertions and "+touches+" genuine touches");
        }catch(Throwable failure){result.putString("failure",failure.toString());try{if(output!=null){StringWriter stack=new StringWriter();failure.printStackTrace(new PrintWriter(stack));write("failure.txt",stack.toString());if(ui!=null)capture("failure");}}catch(Throwable ignored){}}
        finally{
            try{if(ui!=null)ui.setRotation(UiAutomation.ROTATION_UNFREEZE);}catch(Throwable ignored){}
            try{if(output!=null)write("report.json",new JSONObject().put("passed",passed).put("phase",args.getString("phase")).put("runToken",args.getString("runToken")).put("processId",android.os.Process.myPid()).put("checkCount",checks.length()).put("checks",checks).put("touchCount",touches).put("actions",actions).put("captures",captures).put("package",target).put("scope","Actual SDK touchscreen input on the separate Lab package; read-only preferences. Explicit Lab simulation choices, test ads, earned campaign exits and local cosmetic transactions only. No target imports, target solver, reflection, preference writes, injected progress/ownership/currency, external identity, production ads or real purchases. Frozen authored certificates script real touches only.").toString(2));}catch(Throwable ignored){}
            finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
        }
    },"luma-v123-lab-native-observer").start();}
    private SharedPreferences save(){return getTargetContext().getSharedPreferences("luma_drift_save_v1",0);}
    private SharedPreferences session(){return getTargetContext().getSharedPreferences("luma_session",0);}
    private SharedPreferences boardSession(){SharedPreferences expedition=getTargetContext().getSharedPreferences("luma_expedition_session_v118",0);return expedition.getBoolean("active",false)&&expedition.getInt("mode",-1)==5?expedition:session();}
    private void launch(){startActivitySync(new Intent().setClassName(target,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));SystemClock.sleep(1800);}
    private void step(JSONObject step)throws Exception{
        String op=step.getString("op");check(ALLOWED_OPS.contains(op),"Explicit bounded Lab SDK UI operation; direct target writes are unavailable");
        if(op.equals("capture"))capture(step.getString("name"));
        else if(op.equals("capture-fast"))capture(step.getString("name"),0);
        else if(op.equals("snapshot"))snapshot(step.getString("name"));
        else if(op.equals("seek-id")){AccessibilityNodeInfo node=acrossScroll(step.getString("id"));check(node!=null,"Actual semantic control reachable by real UI scrolling: "+step.getString("id"));if(node!=null)node.recycle();}
        else if(op.equals("tap-id"))seekId(step.getString("id"),step.optString("reason",step.getString("id")));
        else if(op.equals("tap")){String regex=step.getString("regex");seekTap(x->x.matches(regex),step.optString("reason",regex));}
        else if(op.equals("back"))back();
        else if(op.equals("top"))top();
        else if(op.equals("scroll"))scroll(step.optBoolean("forward",true));
        else if(op.equals("adventure"))adventure();
        else if(op.equals("language"))setLanguage(step.getString("code"));
        else if(op.equals("firstlight"))firstLight();
        else if(op.equals("open-level"))openLevel(step.getInt("level"));
        else if(op.equals("solve"))solve(step.getInt("level"));
        else if(op.equals("solve-active"))solve(boardSession().getInt("level",-1));
        else if(op.equals("piece"))piece(step.getInt("row"),step.getInt("col"));
        else if(op.equals("earn-through"))earnThrough(step.getInt("level"));
        else if(op.equals("assert-label")){String regex=step.getString("regex");AccessibilityNodeInfo node=find(x->x.matches(regex),false);boolean found=node!=null;if(node!=null)node.recycle();check(found,"Actual visible label matches "+regex);}
        else if(op.equals("assert-id")){AccessibilityNodeInfo node=findId(step.getString("id"),false);boolean found=node!=null;if(node!=null)node.recycle();check(found,"Actual visible semantic target "+step.getString("id"));}
        else if(op.equals("assert-pref")){
            SharedPreferences prefs=getTargetContext().getSharedPreferences(step.optString("pref","luma_drift_save_v1"),0);
            String key=step.getString("key");Object value=prefs.getAll().get(key);
            if(step.optBoolean("absent",false))check(!prefs.contains(key),"Preference remains absent: "+key);
            else check(String.valueOf(value).equals(step.getString("value")),"Read-only preference "+key+" = "+step.getString("value"));
        }
        else if(op.equals("wait")){int ms=step.getInt("ms");check(ms>=0&&ms<=15000,"Bounded real-time wait");SystemClock.sleep(ms);}
        else if(op.equals("assert-victory")){
            String[] fields=getTargetContext().getSharedPreferences("luma_victory_v120",0).getString("receipt","").split("\\|",-1);
            check(fields.length==17&&fields[0].equals("v120_1")&&fields[1].equals("R"),"Actual ready durable victory receipt");
            for(String key:new String[]{"stars","coins","xp"})if(step.has(key)){int index=key.equals("stars")?7:key.equals("coins")?10:11;check(Integer.parseInt(fields[index])==step.getInt(key),"Actual receipt "+key+" = "+step.getInt(key));}
            check(!boardSession().getBoolean("active",true),"Won session inactive only after real result");
        }
        else if(op.equals("remember-value")){SharedPreferences prefs=getTargetContext().getSharedPreferences(step.getString("pref"),0);remembered.put(step.getString("name"),String.valueOf(prefs.getAll().get(step.getString("key"))));}
        else if(op.equals("assert-value-unchanged")){SharedPreferences prefs=getTargetContext().getSharedPreferences(step.getString("pref"),0);check(remembered.get(step.getString("name")).equals(String.valueOf(prefs.getAll().get(step.getString("key")))),"Actual value unchanged "+step.getString("name"));}
        else if(op.equals("assert-int-delta")){
            SharedPreferences prefs=getTargetContext().getSharedPreferences(step.getString("pref"),0);check(remembered.containsKey(step.getString("name")),"Actual numeric baseline was observed");
            int before=Integer.parseInt(remembered.get(step.getString("name"))),after=prefs.getInt(step.getString("key"),Integer.MIN_VALUE);
            check(after-before==step.getInt("delta"),"Actual "+step.getString("key")+" delta = "+step.getInt("delta"));
        }
        else if(op.equals("assert-paid-undo")){
            String request=boardSession().getString("last_paid_undo_v121","");check(request.matches("[a-f0-9-]{36}")&&boardSession().getString("run_id","").matches("[a-f0-9-]{36}"),"The actual committed run stores its paid Undo request identity");
            check(save().getString("undo121_receipt_"+request,"").equals("used:"+step.getInt("cost")),"Exact paid Undo receipt is complete with disclosed Coin price");
            check(save().getString("undo121_pending","").isEmpty(),"No pending paid Undo debit remains");
        }
        else if(op.equals("drain-energy"))drainEnergy();
        else if(op.equals("wallet-swipe-neutral"))walletSwipeNeutral();
        else if(op.equals("assert-energy")){String[] fields=getTargetContext().getSharedPreferences("luma_session_energy_v120",0).getString("session_energy_v120","").split("\\|",-1);check(fields.length==5&&fields[0].equals("1")&&Integer.parseInt(fields[1])==step.getInt("count"),"Actual durable session energy = "+step.getInt("count"));}
        else if(op.equals("assert-shells")){
            String encoded=boardSession().getString("elements_v121","");String[] fields=encoded.split("\\|",-1);
            check(fields.length==4&&fields[0].equals("ES121"),"Actual durable elemental-shell identity");
            int count=0,total=0;Set<String> kinds=new HashSet<>();if(!fields[3].isEmpty())for(String row:fields[3].split(";")){
                String[] parts=row.split(":",-1);check(parts.length==3&&Arrays.asList("ICE","FIRE","WATER").contains(parts[1]),"Actual shell kind is spoken and supported");
                int layers=Integer.parseInt(parts[2]);check(layers>=1&&layers<=2,"Actual remaining shell layer is bounded");count++;total+=layers;kinds.add(parts[1]);
            }
            if(step.has("count"))check(count==step.getInt("count"),"Actual shelled piece count = "+count);
            if(step.has("layers"))check(total==step.getInt("layers"),"Actual remaining shell layers = "+total);
            if(step.optBoolean("allKinds",false))check(kinds.size()==3,"Fresh actual U board contains Ice, Fire and Water");
        }
        else if(op.equals("assert-chain")){
            String[] fields=boardSession().getString("exit_chain_v121","").split(":",-1);check(fields.length==5&&fields[0].equals("1"),"Actual durable manual-exit Focus codec");
            if(step.has("progress"))check(Integer.parseInt(fields[1])==step.getInt("progress"),"Actual manual-exit Focus progress = "+step.getInt("progress"));
            if(step.has("earned"))check(fields[2].equals(step.getBoolean("earned")?"1":"0"),"Actual manual-exit Focus earned state");
            if(step.has("spent"))check(fields[3].equals(step.getBoolean("spent")?"1":"0"),"Actual one-use free Focus spent state");
        }
        else if(op.equals("unseal-step")){JSONObject point=new JSONObject(asset("fixtures/u"+step.getInt("level")+".json")).getJSONArray("steps").getJSONObject(step.getInt("index"));unseal(point);}
        else if(op.equals("certificate-exit")){
            JSONObject point=new JSONObject(asset("fixtures/u"+step.getInt("level")+".json")).getJSONArray("steps").getJSONObject(step.getInt("index"));unseal(point);
            String before=boardSession().getString("history","");piece(point.getInt("row"),point.getInt("col"));String expected=before+(before.isEmpty()?"":",")+point.getInt("action");
            check(boardSession().getString("history","").equals(expected),"Exactly one authored real exit after free shell clearing");
        }
        else if(op.equals("certificate-step")){JSONObject point=new JSONObject(asset("fixtures/u"+step.getInt("level")+".json")).getJSONArray("steps").getJSONObject(step.getInt("index"));piece(point.getInt("row"),point.getInt("col"));}
        else if(op.equals("certificate-step-fast")){
            JSONObject point=new JSONObject(asset("fixtures/u"+step.getInt("level")+".json")).getJSONArray("steps").getJSONObject(step.getInt("index"));int row=point.getInt("row"),col=point.getInt("col");
            AccessibilityNodeInfo node=find(x->x.contains("Piece, row "+(row+1)+", column "+(col+1)+",")||x.contains("Pièce, ligne "+(row+1)+", colonne "+(col+1)+","),true);
            check(node!=null,"Fast reaction gesture target is actually visible");touchNode(node,"fast-real-piece-"+row+"-"+col,50);
        }
        else if(op.equals("remember-clock"))remembered.put(step.getString("name"),String.valueOf(clockSeconds()));
        else if(op.equals("assert-clock-advance")){int delta=clockSeconds()-Integer.parseInt(remembered.get(step.getString("name")));check(delta>=step.getInt("min")&&delta<=step.getInt("max"),"Real visible clock advance "+delta+" seconds excludes paused wait");}
        else if(op.equals("assert-battle"))assertBattle(step);
        else if(op.equals("remember-battle"))remembered.put(step.getString("name"),boardSession().getString("battle_v119",""));
        else if(op.equals("assert-battle-unchanged"))check(remembered.containsKey(step.getString("name"))&&remembered.get(step.getString("name")).equals(boardSession().getString("battle_v119","")),"Battle remains exactly unchanged: "+step.getString("name"));
        else if(op.equals("assert-owned-team"))assertOwnedTeam(step.optInt("count",-1));
        else if(op.equals("assert-expedition-wins")){
            String[] fields=save().getString("expedition_v118_run","").split("\\|",-1);check(fields.length==8,"Actual durable expedition event record");
            int wins=0;for(String event:fields[7].split(";"))if(event.startsWith("W,"))wins++;
            check(wins==step.getInt("count"),"Exactly "+step.getInt("count")+" genuine expedition stages persisted");
        }
        else if(op.equals("assert-starter")){
            check(owned().equals(Collections.singleton("nova")),"Only genuine starter Nova is owned");
            check(save().getInt("coins",-1)==180&&save().getInt("unlocked",1)==1,"Genuine starter balance and campaign unlock");
        }
        else if(op.equals("record-owned"))write(step.getString("name")+".json",new JSONObject().put("owned",new JSONArray(owned())).put("coins",save().getInt("coins",-1)).put("squad",save().getString("companion_squad_v119","")).toString(2));
        else if(op.equals("draw-avatars"))drawAvatars(step.getInt("count"),step.optBoolean("verifyCancel",false));
        else if(op.equals("buy-earned-avatar"))buyEarnedAvatar(step.getString("id"),step.getInt("price"));
        else if(op.equals("choose-owned-team"))chooseOwnedTeam(step.getJSONArray("ids"));
        else throw new IllegalArgumentException("Unknown explicit UI operation "+op);
    }
    private Set<String> owned(){Set<String> owned=new TreeSet<>();for(Map.Entry<String,?> entry:save().getAll().entrySet())if(entry.getKey().startsWith("owned_character_")&&Boolean.TRUE.equals(entry.getValue()))owned.add(entry.getKey().substring("owned_character_".length()));return owned;}
    private void assertOwnedTeam(int count)throws Exception{
        String encoded=save().getString("companion_squad_v119",save().getString("character","nova"));List<String> team=Arrays.asList(encoded.split(",",-1));
        check(team.size()>=1&&team.size()<=3&&new HashSet<>(team).size()==team.size(),"Saved team has 1–3 distinct identities");
        check(owned().containsAll(team),"Every saved team member is genuinely owned");if(count>=0)check(team.size()==count,"Saved team exact size "+count);
    }
    private void assertBattle(JSONObject expected)throws Exception{
        String encoded=boardSession().getString("battle_v119","");String[] fields=encoded.split("\\|",-1);
        check(fields.length==9&&fields[0].equals("v119_1"),"Actual durable battle codec identity");
        int energy=Integer.parseInt(fields[2]),used=Integer.parseInt(fields[3]);check(energy>=0&&energy<=6,"Actual energy remains bounded");
        if(expected.has("team"))check(fields[1].equals(expected.getString("team")),"Actual board team = "+expected.getString("team"));
        if(expected.has("energy"))check(energy==expected.getInt("energy"),"Actual board energy = "+expected.getInt("energy"));
        if(expected.has("used"))check(used==expected.getInt("used"),"Actual once-per-avatar usage mask = "+expected.getInt("used"));
        if(expected.has("credited"))check(fields[4].equals(expected.getString("credited")),"Actual once-per-piece receipt mask");
        if(expected.has("comboReady"))check(fields[6].equals(expected.getBoolean("comboReady")?"1":"0"),"Actual combo readiness");
        if(expected.has("comboSpent"))check(fields[7].equals(expected.getBoolean("comboSpent")?"1":"0"),"Actual once-per-board combo consumption");
    }
    private void firstLight()throws Exception{
        check(!save().getBoolean("first_light_seen_v113",false)&&save().getString("first_light_history_v113","").isEmpty(),"Untouched real first-light introduction");capture("firstlight-new");
        piece(2,0);piece(1,2);piece(3,1);check(save().getString("first_light_history_v113","").equals("0,1,2"),"Three real introduction exits persisted");capture("firstlight-solved");
        seekTap(x->x.startsWith("Discover your little home")||x.startsWith("Découvre ton petit chez-toi"),"complete-genuine-firstlight");check(save().getBoolean("first_light_seen_v113",false),"First light completed through UI");
    }
    private void adventure()throws Exception{
        for(int i=0;i<18;i++){AccessibilityNodeInfo node=findId("home_profile",true);if(node==null)node=findId("journey_continue",true);if(node==null)node=findId("journey_entry",true);if(node==null)node=find(x->x.equals("All levels")||x.equals("Tous les niveaux"),true);if(node!=null){node.recycle();return;}AccessibilityNodeInfo nav=findId("root_nav_adventure",true);if(nav!=null){touchNode(nav,"return-actual-adventure-tab");continue;}AccessibilityNodeInfo leave=find(x->x.equals("Leave level")||x.equals("Quitter le niveau"),true);if(leave!=null){touchNode(leave,"leave-real-level-with-save");continue;}AccessibilityNodeInfo pause=findId("play_pause",true);if(pause!=null){touchNode(pause,"open-real-pause-to-leave");continue;}back();}
        throw new AssertionError("Cannot reach actual adventure: "+labels());
    }
    private void setLanguage(String code)throws Exception{
        int chosen=Arrays.asList(CODES).indexOf(code);check(chosen>=0,"Supported language selected");adventure();seekTap(x->Arrays.asList(SETTINGS).contains(x),"open-real-settings");seekTap(x->x.equals(LANGUAGES[chosen]),"select-real-language-"+code);check(save().getString("language","").equals(code),"Language saved by actual UI");back();adventure();
    }
    private void openLevel(int level)throws Exception{
        check(level>=1&&level<=60,"Only existing frozen U1–U60 certificates");openMap();mapSelect(level,1700);seekId("world_play","open-selected-real-U"+level);
        AccessibilityNodeInfo play=findId("briefing_play",true);if(play!=null)touchNode(play,"start-real-U"+level);else seekTap(x->x.startsWith("Play")||x.startsWith("Jouer"),"start-real-U"+level);
        check(session().getInt("mode",-1)==0&&session().getInt("level",-1)==level,"Actual campaign board selected");
    }
    private void solve(int level)throws Exception{
        JSONObject fixture=new JSONObject(asset("fixtures/u"+level+".json"));JSONArray steps=fixture.getJSONArray("steps");
        SharedPreferences actual=boardSession();int mode=actual.getInt("mode",-1);
        check(actual.getInt("level",-1)==level&&actual.getInt("content_version",-1)==fixture.getInt("contentVersion")&&(actual.getInt("budget_policy",-1)==5||actual.getInt("budget_policy",-1)==6),"Actual board preserves existing U certificate and budget identities");
        String history=actual.getString("history","");List<String> prefix=history.isEmpty()?Collections.emptyList():Arrays.asList(history.split(","));
        check(prefix.size()<=steps.length(),"Existing genuine board history is a bounded certificate prefix");
        for(int n=0;n<prefix.size();n++)check(prefix.get(n).equals(String.valueOf(steps.getJSONObject(n).getInt("action"))),"Already performed real exit agrees with public certificate");
        for(int n=prefix.size();n<steps.length();n++){
            JSONObject step=steps.getJSONObject(n);unseal(step);piece(step.getInt("row"),step.getInt("col"));
            history+=(history.isEmpty()?"":",")+step.getInt("action");check(actual.getString("history","").equals(history),"Exactly one certificate exit persisted for U"+level+" step "+(n+1));
        }
        if(mode==5)check(save().getString("expedition_v118_run","").contains("W,"),"Real expedition victory stored after all actual exits");
        else {long until=SystemClock.uptimeMillis()+5000;while(save().getInt("stars_"+level,0)==0&&SystemClock.uptimeMillis()<until)SystemClock.sleep(100);check(save().getInt("stars_"+level,0)>0,"Real native U"+level+" victory awarded");}
    }
    private void earnThrough(int through)throws Exception{
        check(through>=1&&through<=30,"Genuine funding stays within public certificates");
        for(int level=1;level<=through;level++){
            if(save().getInt("stars_"+level,0)>0)continue;
            openLevel(level);int before=save().getInt("coins",-1);solve(level);
            check(save().getInt("coins",-1)==before+55,"Real first-clear campaign reward is exactly 55 earned coins for U"+level);
        }
        capture("genuine-campaign-through-u"+through);adventure();
    }
    private void drawAvatars(int count,boolean verifyCancel)throws Exception{
        check(count>=1&&count<=12,"Bounded explicitly requested earned draws");adventure();
        AccessibilityNodeInfo collection=findId("root_nav_collection_hub",true);
        if(collection!=null)touchNode(collection,"open-real-collection-tab");
        else {
            collection=find(x->x.equals("Collection"),true);
            if(collection!=null)touchNode(collection,"open-real-collection");
            else {seekTap(x->x.equals("My home")||x.equals("Ma maison"),"open-real-home-tab");seekTap(x->x.equals("Collection"),"open-real-collection-tab");}
        }
        seekTap(x->x.equals("Star arrivals")||x.equals("Rencontres stellaires"),"open-real-earned-companion-draw");
        seekTap(x->x.equals("Pool & exact odds")||x.equals("Contenu et probabilités"),"open-real-disclosed-odds-before-draw");capture("gacha-actual-fixed-odds");back();
        for(int n=0;n<count;n++){
            int before=save().getInt("coins",-1);check(before>=180,"Draw funded only by real earned balance");Set<String> beforeOwned=owned();Map<String,?> beforeValues=save().getAll();
            String last=save().getString("summon_last_request_v116_1","");seekId("summon_draw","request-real-earned-avatar-draw-"+(n+1));
            check(save().getInt("coins",-1)==before&&save().getString("summon_last_request_v116_1","").equals(last),"Confirmation alone never debits or creates an arrival");
            capture("gacha-confirm-"+(n+1));
            if(verifyCancel&&n==0){back();check(save().getInt("coins",-1)==before&&save().getString("summon_last_request_v116_1","").equals(last),"Actual cancellation preserves Coins and last draw identity");capture("gacha-cancelled-no-debit");seekId("summon_draw","request-real-earned-avatar-after-cancel");}
            seekTap(x->x.equals("Yes")||x.equals("Oui"),"confirm-exact-real-earned-180-coin-draw-"+(n+1));
            String request=save().getString("summon_last_request_v116_1","");check(!request.isEmpty(),"Actual draw request durably recorded");
            byte[] digest=java.security.MessageDigest.getInstance("SHA-256").digest(request.getBytes(StandardCharsets.UTF_8));StringBuilder hex=new StringBuilder();for(byte value:digest)hex.append(String.format(Locale.ROOT,"%02x",value&255));
            String receipt=save().getString("summon_v116_1_"+hex,"");String[] fields=receipt.split(":",-1);
            check(fields.length==4&&fields[0].equals("AVATARS"),"Actual avatar draw has a committed receipt");
            String[] ids={"miko","orbit","pip","astra","zig","lune","fenn","hoot","buzz","tula","jelli","vela","nimbi","soli","mochi","nixi","prickly","quill","waddles","halo","emberling","bambu","inky"};int itemIndex=Arrays.asList(ids).indexOf(fields[1]);check(itemIndex>=0,"Actual arrival belongs to the disclosed fixed avatar pool");
            int expectedCompensation=beforeOwned.contains(fields[1])?(itemIndex<10?45:itemIndex<18?75:120):0;int compensation=Integer.parseInt(fields[3]);
            check(fields[2].equals(beforeOwned.contains(fields[1])?"1":"0")&&compensation==expectedCompensation,"Exact disclosed rarity compensation and duplicate state match prior ownership");
            check(save().getInt("coins",-1)==before-180+compensation,"Exact earned draw debit and disclosed duplicate compensation");
            check(owned().containsAll(beforeOwned),"Draw never loses already owned characters");
            String copyKey="companion_copies_v118_"+fields[1];Object savedCopies=beforeValues.get(copyKey);int copies=savedCopies instanceof Integer?(Integer)savedCopies:0;if(beforeOwned.contains(fields[1]))copies=Math.max(1,copies);
            check(save().getInt(copyKey,-1)==Math.min(92,copies+1),"Exactly one genuine companion copy recorded atomically with the arrival");
            write("genuine-draw-"+(n+1)+".json",new JSONObject().put("requestId",request).put("receipt",receipt).put("coinsBefore",before).put("coinsAfter",save().getInt("coins",-1)).put("owned",new JSONArray(owned())).toString(2));capture("genuine-draw-"+(n+1));
            seekId("summon_done","close-actual-committed-reveal");
        }
        adventure();
    }
    private void chooseOwnedTeam(JSONArray requested)throws Exception{
        List<String> ids=new ArrayList<>();for(int n=0;n<requested.length();n++)ids.add(requested.getString(n));
        check(ids.size()>=1&&ids.size()<=3&&new HashSet<>(ids).size()==ids.size()&&owned().containsAll(ids),"Requested UI team contains only distinct genuinely owned members");
        adventure();seekId("squad_entry","open-real-team-editor");
        for(int slot=0;slot<ids.size();slot++){seekId("squad_slot_"+slot,"select-real-team-slot-"+slot);squadMember(ids.get(slot));}
        if(!save().getString("companion_squad_v119","").equals(String.join(",",ids)))seekId("squad_save","save-real-team");
        check(save().getString("companion_squad_v119","").equals(String.join(",",ids)),"Actual UI durably saves the requested owned team");assertOwnedTeam(ids.size());top();
    }
    private void buyEarnedAvatar(String id,int price)throws Exception{
        check(id.matches("[a-z][a-z0-9_]{0,40}")&&price>0&&price<=1200,"Explicit bounded earned-currency companion offer");
        int before=save().getInt("coins",-1);String appearance=save().getString("character","");
        if(owned().contains(id)){check(save().getInt("coins",-1)==before,"Already owned companion needs no duplicate purchase");return;}
        check(before>=price,"Genuine earned wallet covers explicit companion offer");adventure();seekId("squad_entry","open-real-team-to-view-locked-companion");squadMember(id);
        capture("before-earned-purchase-"+id);seekTap(x->x.equals(price+" LumaCoins"),"buy-real-earned-companion-"+id);
        check(save().getBoolean("owned_character_"+id,false)&&save().getInt("coins",-1)==before-price,"Exact real earned-currency debit and ownership for "+id);
        check(save().getString("character","").equals(appearance),"Purchase never silently equips the appearance");capture("after-earned-purchase-"+id);adventure();
    }
    private AccessibilityNodeInfo acrossScroll(String id){
        top();int unchanged=0;for(int scan=0;scan<35;scan++){AccessibilityNodeInfo found=findId(id,true);if(found!=null)return found;String before=scrollSignature();scroll(true);unchanged=scrollSignature().equals(before)?unchanged+1:0;if(unchanged>=3)break;}return null;
    }
    private void squadMember(String id)throws Exception{
        for(int page=0;page<7;page++){AccessibilityNodeInfo previous=acrossScroll("squad_page_back");if(previous==null)break;touchNode(previous,"return-real-team-catalogue-page");}
        for(int page=0;page<7;page++){
            AccessibilityNodeInfo member=acrossScroll("squad_member_"+id);
            if(member!=null){touchNode(member,"select-real-owned-member-"+id);return;}
            AccessibilityNodeInfo next=findId("squad_page_next",true);if(next==null)next=acrossScroll("squad_page_next");
            if(next==null)break;touchNode(next,"advance-real-team-catalogue-page");
        }
        throw new AssertionError("Owned member is not reachable through actual team catalogue pages: "+id+"; "+labels());
    }
    private int shellLayers(int id)throws Exception{
        String encoded=boardSession().getString("elements_v121","");if(encoded.isEmpty())return 0;
        String[] fields=encoded.split("\\|",-1);check(fields.length==4&&fields[0].equals("ES121"),"Read actual encoded shell state only");
        if(!fields[3].isEmpty())for(String row:fields[3].split(";")){String[] parts=row.split(":",-1);if(Integer.parseInt(parts[0])==id)return Integer.parseInt(parts[2]);}return 0;
    }
    private void unseal(JSONObject point)throws Exception{
        int id=point.getInt("action"),remaining=shellLayers(id);check(remaining>=0&&remaining<=2,"At most two real free taps for an observed shell");
        String[] keys={"run_id","history","budget_used","undos","battle_v119","exit_chain_v121"};Map<String,String> before=new TreeMap<>();for(String key:keys)before.put(key,String.valueOf(boardSession().getAll().get(key)));int coins=save().getInt("coins",-1);
        for(int tap=0;tap<remaining;tap++){
            int layers=shellLayers(id);piece(point.getInt("row"),point.getInt("col"));check(shellLayers(id)==layers-1,"One real piece tap removes exactly one shell layer");
            for(String key:keys)check(before.get(key).equals(String.valueOf(boardSession().getAll().get(key))),"Free shell tap preserves actual "+key);
            check(coins==save().getInt("coins",-1),"Free shell tap preserves earned Coins");
        }
    }
    private void piece(int row,int col)throws Exception{String en="Piece, row "+(row+1)+", column "+(col+1)+",";String fr="Pièce, ligne "+(row+1)+", colonne "+(col+1)+",";seekTap(x->x.contains(en)||x.contains(fr),"real-piece-"+row+"-"+col);}
    private void snapshot(String name)throws Exception{
        JSONObject result=new JSONObject();for(String file:new String[]{"luma_drift_save_v1","luma_drift_meta_v1","luma_session","luma_expedition_session_v118","luma_victory_v120","luma_session_energy_v120","luma_energy_purchases_v120","luma_lab_purchases_v1","luma_ads_preferences_v1","luma_ads_v1","luma_commerce_privacy_v1"}){
            JSONObject values=new JSONObject();for(Map.Entry<String,?> entry:new TreeMap<>(getTargetContext().getSharedPreferences(file,0).getAll()).entrySet()){
                if(entry.getKey().toLowerCase(Locale.ROOT).contains("seed"))continue;Object value=entry.getValue();
                if(value instanceof Set){List<String> sorted=new ArrayList<>();for(Object member:(Set<?>)value)sorted.add(String.valueOf(member));Collections.sort(sorted);value=new JSONArray(sorted);}values.put(entry.getKey(),value);
            }result.put(file,values);
        }write(name+".json",result.toString(2));
    }
    private static String name(AccessibilityNodeInfo node){CharSequence value=node.getContentDescription();if(value==null||value.length()==0)value=node.getText();return value==null?"":value.toString();}
    private List<AccessibilityNodeInfo> nodes(){if(Build.VERSION.SDK_INT>=33)ui.clearCache();List<AccessibilityNodeInfo> result=new ArrayList<>();AccessibilityNodeInfo root=ui.getRootInActiveWindow();if(root==null)return result;ArrayDeque<AccessibilityNodeInfo> pending=new ArrayDeque<>();pending.add(root);while(!pending.isEmpty()&&result.size()<800){AccessibilityNodeInfo node=pending.removeFirst();result.add(node);for(int n=0;n<node.getChildCount();n++){AccessibilityNodeInfo child=node.getChild(n);if(child!=null)pending.addLast(child);}}return result;}
    private AccessibilityNodeInfo find(Predicate<String> match,boolean clickable){AccessibilityNodeInfo found=null;for(AccessibilityNodeInfo node:nodes()){Rect bounds=new Rect();node.getBoundsInScreen(bounds);if(found==null&&node.isVisibleToUser()&&node.isEnabled()&&(!clickable||node.isClickable())&&!bounds.isEmpty()&&match.test(name(node)))found=AccessibilityNodeInfo.obtain(node);node.recycle();}return found;}
    private AccessibilityNodeInfo findId(String id,boolean enabled){AccessibilityNodeInfo found=null;for(AccessibilityNodeInfo node:nodes()){Rect bounds=new Rect();node.getBoundsInScreen(bounds);if(found==null&&node.isVisibleToUser()&&(!enabled||node.isEnabled()&&node.isClickable())&&!bounds.isEmpty()&&(target+":id/"+id).equals(node.getViewIdResourceName()))found=AccessibilityNodeInfo.obtain(node);node.recycle();}return found;}
    private String labels(){List<String> labels=new ArrayList<>();for(AccessibilityNodeInfo node:nodes()){if(!name(node).isEmpty())labels.add(name(node));node.recycle();}return labels.toString();}
    private void foreground(){AccessibilityNodeInfo root=ui.getRootInActiveWindow();String packageName=root==null?"":String.valueOf(root.getPackageName());if(root!=null)root.recycle();if(!target.equals(packageName))throw new AssertionError("Refuse input outside the target: "+packageName);}
    private void touchNode(AccessibilityNodeInfo node,String reason)throws Exception{
        touchNode(node,reason,480);
    }
    private void touchNode(AccessibilityNodeInfo node,String reason,int settleMs)throws Exception{
        foreground();Rect box=new Rect();node.getBoundsInScreen(box);String label=name(node),id=node.getViewIdResourceName();node.recycle();
        long down=SystemClock.uptimeMillis();inject(down,MotionEvent.ACTION_DOWN,box.exactCenterX(),box.exactCenterY());SystemClock.sleep(25);inject(down,MotionEvent.ACTION_UP,box.exactCenterX(),box.exactCenterY());touches++;
        actions.put(new JSONObject().put("reason",reason).put("input","actual SDK touchscreen").put("uptimeMs",SystemClock.uptimeMillis()).put("label",label).put("resourceId",id).put("bounds",new JSONArray(new int[]{box.left,box.top,box.right,box.bottom})));SystemClock.sleep(settleMs);
    }
    private void walletSwipeNeutral()throws Exception{
        AccessibilityNodeInfo wallet=findId("play_wallet",true);check(wallet!=null,"Actual fixed play wallet hit is visible");Rect band=new Rect();wallet.getBoundsInScreen(band);wallet.recycle();
        List<float[]> starts=new ArrayList<>();for(int lane=1;lane<=3;lane++)starts.add(new float[]{band.left+band.width()*lane/4f,band.exactCenterY()});JSONArray overlaps=new JSONArray();for(AccessibilityNodeInfo node:nodes()){String label=name(node);Rect bounds=new Rect();node.getBoundsInScreen(bounds);if((label.startsWith("Piece, row ")||label.startsWith("Pièce, ligne "))&&Rect.intersects(band,bounds)){overlaps.put(new JSONObject().put("label",label).put("bounds",new JSONArray(new int[]{bounds.left,bounds.top,bounds.right,bounds.bottom})));starts.add(new float[]{bounds.exactCenterX(),(Math.max(band.top,bounds.top)+Math.min(band.bottom,bounds.bottom))/2f});}node.recycle();}
        String[] keys={"run_id","history","budget_used","undos","battle_v119"};Map<String,String> before=new TreeMap<>();for(String key:keys)before.put(key,String.valueOf(session().getAll().get(key)));int coins=save().getInt("coins",-1);
        write("wallet-swipe-initial-overlap.json",new JSONObject().put("walletBounds",new JSONArray(new int[]{band.left,band.top,band.right,band.bottom})).put("observedPieceIntersections",overlaps).toString(2));
        for(int lane=0;lane<starts.size();lane++)for(int direction=0;direction<4;direction++){
            for(int reset=0;reset<3;reset++)scroll(true);foreground();float x=starts.get(lane)[0],y=starts.get(lane)[1],distance=80f;float dx=direction==1?distance:direction==3?-distance:0,dy=direction==0?-distance:direction==2?distance:0;
            long down=SystemClock.uptimeMillis();inject(down,MotionEvent.ACTION_DOWN,x,y);for(int part=1;part<=5;part++){SystemClock.sleep(25);inject(down,MotionEvent.ACTION_MOVE,x+dx*part/5f,y+dy*part/5f);}inject(down,MotionEvent.ACTION_UP,x+dx,y+dy);touches++;
            actions.put(new JSONObject().put("reason","actual-fixed-wallet-swipe-lane-"+lane+"-direction-"+direction).put("input","actual SDK touchscreen swipe").put("uptimeMs",SystemClock.uptimeMillis()).put("from",new JSONArray(new float[]{x,y})).put("to",new JSONArray(new float[]{x+dx,y+dy})));SystemClock.sleep(300);
            for(String key:keys)check(before.get(key).equals(String.valueOf(session().getAll().get(key))),"Wallet swipe leaves actual "+key+" unchanged");check(save().getInt("coins",-1)==coins,"Wallet swipe does not change actual Coins");
        }
        write("wallet-swipe-overlap.json",new JSONObject().put("walletBounds",new JSONArray(new int[]{band.left,band.top,band.right,band.bottom})).put("observedPieceIntersections",overlaps).put("swipes",starts.size()*4).put("scope","Before each real swipe the actual UI is scrolled back to its maximum. Starts use observed fixed HUD positions including every intersecting piece bounds centre. Recorded AX intersections are observations, not hidden-board injection.").toString(2));
    }
    private void drainEnergy()throws Exception{
        check(session().getInt("mode",-1)==0&&session().getInt("level",-1)>10&&session().getBoolean("active",false),"Energy drain uses only a real active eligible campaign attempt");
        int attempts=0;
        for(;attempts<18;attempts++){
            String[] before=getTargetContext().getSharedPreferences("luma_session_energy_v120",0).getString("session_energy_v120","").split("\\|",-1);
            check(before.length==5&&before[0].equals("1"),"Read actual durable energy before bounded real restart");
            if(Integer.parseInt(before[1])==0)break;
            String previous=session().getString("run_id","");
            seekTap(x->{String label=x.replace("↻","").trim();return label.equals("Restart")||label.equals("Recommencer");},"request-real-paid-attempt-restart-"+(attempts+1));
            seekTap(x->{String label=x.replace("↻","").trim();return label.startsWith("Restart · 1 ")||label.startsWith("Recommencer · 1 ");},"confirm-real-one-energy-restart-"+(attempts+1));
            String current=session().getString("run_id","");String[] after=getTargetContext().getSharedPreferences("luma_session_energy_v120",0).getString("session_energy_v120","").split("\\|",-1);
            check(!previous.equals(current)&&current.matches("[a-f0-9-]{36}"),"Actual explicit restart creates exactly one new run identity");
            check(after.length==5&&after[3].isEmpty()&&after[4].equals(current),"Actual energy debit confirmed against committed board run");
            int ticks=(int)Math.max(0,(Long.parseLong(after[2])-Long.parseLong(before[2]))/600000L);
            check(Integer.parseInt(after[1])==Math.min(getTargetContext().getSharedPreferences("luma_session_energy_v120",0).getInt("session_energy_cap_v121",10),Integer.parseInt(before[1])+ticks)-1,"Exactly one energy debited, accounting only for actual elapsed refill ticks");
        }
        String[] finalRecord=getTargetContext().getSharedPreferences("luma_session_energy_v120",0).getString("session_energy_v120","").split("\\|",-1);
        check(Integer.parseInt(finalRecord[1])==0,"Real bounded eligible restarts exhaust current session energy");
        write("genuine-energy-restarts.json",new JSONObject().put("restarts",attempts).put("record",String.join("|",finalRecord)).put("scope","Only observed native restart controls. No preference or clock writes, refills, ads or purchases.").toString(2));
    }
    private int clockSeconds()throws Exception{
        AccessibilityNodeInfo node=find(x->x.startsWith("Your time ")||x.startsWith("Ton temps "),false);check(node!=null,"Actual visible informational clock label");String label=name(node);node.recycle();
        java.util.regex.Matcher match=java.util.regex.Pattern.compile("(\\d+):(\\d{2})").matcher(label);check(match.find(),"Clock has visible minutes and seconds");return Integer.parseInt(match.group(1))*60+Integer.parseInt(match.group(2));
    }
    private void seekTap(Predicate<String> match,String reason)throws Exception{
        for(int attempt=0;attempt<20;attempt++){AccessibilityNodeInfo node=find(match,true);if(node!=null){touchNode(node,reason);return;}if(attempt<3)SystemClock.sleep(200);else scroll(false);}
        int unchanged=0;for(int attempt=0;attempt<45;attempt++){AccessibilityNodeInfo node=find(match,true);if(node!=null){touchNode(node,reason);return;}String before=scrollSignature();scroll(true);unchanged=scrollSignature().equals(before)?unchanged+1:0;if(unchanged>=3)break;}
        throw new AssertionError("Missing actual native target "+reason+": "+labels());
    }
    private void seekId(String id,String reason)throws Exception{
        AccessibilityNodeInfo node=findId(id,true);if(node!=null){touchNode(node,reason);return;}top();
        int unchanged=0;for(int n=0;n<45;n++){node=findId(id,true);if(node!=null){touchNode(node,reason);return;}String before=scrollSignature();scroll(true);unchanged=scrollSignature().equals(before)?unchanged+1:0;if(unchanged>=3)break;}
        throw new AssertionError("Missing actual semantic target "+id+": "+labels());
    }
    private void inject(long down,int action,float x,float y){MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);try{if(!ui.injectInputEvent(event,true))throw new AssertionError("Native touchscreen refused");}finally{event.recycle();}}
    private void back(){foreground();ui.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);SystemClock.sleep(450);}
    private void scroll(boolean forward){foreground();AccessibilityNodeInfo root=ui.getRootInActiveWindow();boolean sent=false;if(root!=null){sent=root.performAction(forward?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);root.recycle();}if(!sent)for(AccessibilityNodeInfo node:nodes()){if(!sent&&node.isScrollable())sent=node.performAction(forward?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);node.recycle();}SystemClock.sleep(250);}
    private String scrollSignature(){StringBuilder out=new StringBuilder();for(AccessibilityNodeInfo node:nodes()){Rect box=new Rect();node.getBoundsInScreen(box);if(node.isVisibleToUser())out.append(name(node)).append('|').append(node.getViewIdResourceName()).append('|').append(box.toShortString()).append(';');node.recycle();}return out.toString();}
    private void top(){int unchanged=0;for(int n=0;n<25;n++){String before=scrollSignature();scroll(false);unchanged=scrollSignature().equals(before)?unchanged+1:0;if(unchanged>=3)break;}}
    private Rect observedBounds(String id)throws Exception{
        AccessibilityNodeInfo node=findId(id,true);check(node!=null,"Actual visible pointer target "+id);Rect box=new Rect();node.getBoundsInScreen(box);node.recycle();check(!box.isEmpty(),"Nonempty actual target bounds "+id);return box;
    }
    private Rect windowBounds()throws Exception{foreground();AccessibilityNodeInfo root=ui.getRootInActiveWindow();check(root!=null,"Actual target window exists");Rect box=new Rect();root.getBoundsInScreen(box);root.recycle();return box;}
    private void pointer(float x,float y,String reason,int settle)throws Exception{
        foreground();check(settle>=0&&settle<=15000,"Bounded pointer settle");Rect window=windowBounds();check(window.contains((int)x,(int)y),"Pointer remains in actual target window");
        long down=SystemClock.uptimeMillis();inject(down,MotionEvent.ACTION_DOWN,x,y);SystemClock.sleep(25);inject(down,MotionEvent.ACTION_UP,x,y);touches++;
        actions.put(new JSONObject().put("reason",reason).put("input","actual SDK touchscreen").put("uptimeMs",SystemClock.uptimeMillis()).put("point",new JSONArray(new float[]{x,y})));SystemClock.sleep(settle);
    }
    private void openMap()throws Exception{
        AccessibilityNodeInfo present=findId("world_minimap",true);if(present!=null){present.recycle();return;}adventure();
        AccessibilityNodeInfo entry=findId("journey_entry",true);if(entry!=null)touchNode(entry,"open-real-world-map");else seekTap(x->x.equals("All levels")||x.equals("Tous les niveaux"),"open-real-world-map");
        check(findAndRecycle("world_minimap"),"Actual native world minimap is visible");check(findAndRecycle("world_play"),"Actual native world Play is visible");
    }
    private boolean findAndRecycle(String id){AccessibilityNodeInfo node=findId(id,true);if(node==null)return false;node.recycle();return true;}
    private void mapFrameAt(int level)throws Exception{
        check(level>=1&&level<=100,"Native gesture fixture currently selects the actual first sector only");Rect mini=observedBounds("world_minimap");int local=level-1,row=local/10,pos=local%10,col=(row&1)==0?pos:9-pos;
        // Public presentation coordinates only: this touches the visible minimap, then observes the actual node AX bounds.
        float wx=111+82*col,wy=164+148*row+(float)Math.sin(col*.72+row*.61)*15;
        pointer(mini.left+mini.width()*wx/960f,mini.top+mini.height()*wy/1680f,"actual-minimap-frame-U"+level,500);
    }
    private void mapSelect(int level,int settle)throws Exception{
        check(level<=save().getInt("unlocked",1),"World selection uses genuinely unlocked campaign node");mapFrameAt(level);AccessibilityNodeInfo node=findId("world_node_"+level,true);check(node!=null,"Actual world node visible after real minimap framing");touchNode(node,"actual-world-walk-to-U"+level,settle);
        check(findAndRecycle("world_play"),"Selecting destination remains on the real map");check(session().getInt("map_avatar_journey122",save().getInt("unlocked",1))==level,"Actual visual companion destination agrees with confirmed arrival or its genuine initial default");
    }
    private void capture(String title)throws Exception{capture(title,1300);}
    private void capture(String title,int delayMs)throws Exception{
        check(title.matches("[a-z][a-z0-9_-]{0,75}")&&!new File(output,title+".png").exists(),"Distinct bounded native capture label");
        SystemClock.sleep(delayMs);foreground();Bitmap bitmap=ui.takeScreenshot();if(bitmap==null)throw new IOException("Native screenshot unavailable");
        try(OutputStream file=new FileOutputStream(new File(output,title+".png"))){if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,file))throw new IOException("PNG encoding failed");}finally{bitmap.recycle();}
        JSONArray observed=new JSONArray();for(AccessibilityNodeInfo node:nodes()){Rect box=new Rect();node.getBoundsInScreen(box);observed.put(new JSONObject().put("label",name(node)).put("resourceId",node.getViewIdResourceName()).put("bounds",new JSONArray(new int[]{box.left,box.top,box.right,box.bottom})).put("clickable",node.isClickable()).put("enabled",node.isEnabled()).put("visible",node.isVisibleToUser()));node.recycle();}write(title+"-accessibility.json",observed.toString(2));captures.put(title);
    }
    private void check(boolean condition,String message)throws Exception{checks.put(new JSONObject().put("passed",condition).put("message",message));if(!condition)throw new AssertionError(message);}
    private String asset(String name)throws Exception{try(InputStream input=getContext().getAssets().open(name);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int count;while((count=input.read(buffer))!=-1)out.write(buffer,0,count);return out.toString("UTF-8");}}
    private void write(String name,String text)throws Exception{if(!name.matches("[a-z][a-z0-9_-]{0,100}\\.(json|txt)"))throw new IllegalArgumentException("Unsafe evidence filename");try(OutputStream file=new FileOutputStream(new File(output,name))){file.write(text.getBytes(StandardCharsets.UTF_8));}}
}
