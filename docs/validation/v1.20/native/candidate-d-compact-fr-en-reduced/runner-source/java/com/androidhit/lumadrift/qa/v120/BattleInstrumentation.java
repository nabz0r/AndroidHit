package com.androidhit.lumadrift.qa.v120;

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
    private static final String[] CODES={"en","fr","de","es","pt","it"};
    private static final String[] LANGUAGES={"English","Français","Deutsch","Español","Português","Italiano"};
    private static final String[] SETTINGS={"Settings","Réglages","Einstellungen","Ajustes","Definições","Impostazioni"};
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);args=arguments==null?new Bundle():arguments;start();}
    @Override public void onStart(){new Thread(()->{
        boolean passed=false;Bundle result=new Bundle();
        try{
            target=getTargetContext().getPackageName();check(target.equals("com.androidhit.lumadrift"),"Exact production package");
            String token=args.getString("runToken","");check(token.matches("[a-f0-9-]{36}"),"Unique host execution token");
            String phase=args.getString("phase","");check(phase.matches("[a-z][a-z0-9_-]{1,50}"),"Bounded explicit phase");
            output=new File(getTargetContext().getExternalFilesDir(null),"v120-native/"+token+"/"+phase);check(!output.exists()&&output.mkdirs(),"Fresh evidence output");
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
            try{if(output!=null)write("report.json",new JSONObject().put("passed",passed).put("phase",args.getString("phase")).put("runToken",args.getString("runToken")).put("processId",android.os.Process.myPid()).put("checkCount",checks.length()).put("checks",checks).put("touchCount",touches).put("actions",actions).put("captures",captures).put("package",target).put("scope","Actual native touchscreen input at observed accessibility bounds and read-only preferences. Existing U certificates script genuine board touches. No target imports, target solver, reflection, preference writes, injected progress/ownership/currency, external identity, ads, real-money purchase or publication. Earned coin draws only when expressly listed.").toString(2));}catch(Throwable ignored){}
            finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
        }
    },"luma-v120-native-observer").start();}
    private SharedPreferences save(){return getTargetContext().getSharedPreferences("luma_drift_save_v1",0);}
    private SharedPreferences session(){return getTargetContext().getSharedPreferences("luma_session",0);}
    private SharedPreferences boardSession(){SharedPreferences expedition=getTargetContext().getSharedPreferences("luma_expedition_session_v118",0);return expedition.getBoolean("active",false)&&expedition.getInt("mode",-1)==5?expedition:session();}
    private void launch(){startActivitySync(new Intent().setClassName(target,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));SystemClock.sleep(1800);}
    private void step(JSONObject step)throws Exception{
        String op=step.getString("op");
        if(op.equals("capture"))capture(step.getString("name"));
        else if(op.equals("capture-fast"))capture(step.getString("name"),0);
        else if(op.equals("snapshot"))snapshot(step.getString("name"));
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
        else if(op.equals("assert-energy")){String[] fields=getTargetContext().getSharedPreferences("luma_session_energy_v120",0).getString("session_energy_v120","").split("\\|",-1);check(fields.length==5&&fields[0].equals("1")&&Integer.parseInt(fields[1])==step.getInt("count"),"Actual durable session energy = "+step.getInt("count"));}
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
        else if(op.equals("draw-avatars"))drawAvatars(step.getInt("count"));
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
        for(int i=0;i<18;i++){AccessibilityNodeInfo node=findId("journey_entry",true);if(node==null)node=find(x->x.equals("All levels")||x.equals("Tous les niveaux"),true);if(node!=null){node.recycle();return;}AccessibilityNodeInfo nav=findId("root_nav_adventure",true);if(nav!=null){touchNode(nav,"return-actual-adventure-tab");continue;}back();}
        throw new AssertionError("Cannot reach actual adventure: "+labels());
    }
    private void setLanguage(String code)throws Exception{
        int chosen=Arrays.asList(CODES).indexOf(code);check(chosen>=0,"Supported language selected");adventure();seekTap(x->Arrays.asList(SETTINGS).contains(x),"open-real-settings");seekTap(x->x.equals(LANGUAGES[chosen]),"select-real-language-"+code);check(save().getString("language","").equals(code),"Language saved by actual UI");back();adventure();
    }
    private void openLevel(int level)throws Exception{
        check(level>=1&&level<=40,"Only existing frozen U1–U40 certificates");adventure();
        AccessibilityNodeInfo entry=findId("journey_entry",true);if(entry!=null)touchNode(entry,"open-real-level-map");else seekTap(x->x.equals("All levels")||x.equals("Tous les niveaux"),"open-real-level-map");
        seekTap(x->x.startsWith("Level "+level+",")||x.equals("Level "+level)||x.startsWith("Niveau "+level+",")||x.equals("Niveau "+level),"open-real-U"+level);
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
            JSONObject step=steps.getJSONObject(n);piece(step.getInt("row"),step.getInt("col"));
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
    private void drawAvatars(int count)throws Exception{
        check(count>=1&&count<=12,"Bounded explicitly requested earned draws");adventure();
        AccessibilityNodeInfo collection=findId("root_nav_collection_hub",true);
        if(collection!=null)touchNode(collection,"open-real-collection-tab");
        else {
            collection=find(x->x.equals("Collection"),true);
            if(collection!=null)touchNode(collection,"open-real-collection");
            else {seekTap(x->x.equals("My home")||x.equals("Ma maison"),"open-real-home-tab");seekTap(x->x.equals("Collection"),"open-real-collection-tab");}
        }
        seekTap(x->x.equals("Star arrivals")||x.equals("Rencontres stellaires"),"open-real-earned-companion-draw");
        for(int n=0;n<count;n++){
            int before=save().getInt("coins",-1);check(before>=180,"Draw funded only by real earned balance");Set<String> beforeOwned=owned();
            seekTap(x->x.contains("180")&&x.contains("LumaCoins"),"real-earned-avatar-draw-"+(n+1));
            String request=save().getString("summon_last_request_v116_1","");check(!request.isEmpty(),"Actual draw request durably recorded");
            byte[] digest=java.security.MessageDigest.getInstance("SHA-256").digest(request.getBytes(StandardCharsets.UTF_8));StringBuilder hex=new StringBuilder();for(byte value:digest)hex.append(String.format(Locale.ROOT,"%02x",value&255));
            String receipt=save().getString("summon_v116_1_"+hex,"");String[] fields=receipt.split(":",-1);
            check(fields.length==4&&fields[0].equals("AVATARS"),"Actual avatar draw has a committed receipt");
            int compensation=Integer.parseInt(fields[3]);check(save().getInt("coins",-1)==before-180+compensation,"Exact earned draw debit and disclosed duplicate compensation");
            check(owned().containsAll(beforeOwned),"Draw never loses already owned characters");
            write("genuine-draw-"+(n+1)+".json",new JSONObject().put("requestId",request).put("receipt",receipt).put("coinsBefore",before).put("coinsAfter",save().getInt("coins",-1)).put("owned",new JSONArray(owned())).toString(2));capture("genuine-draw-"+(n+1));
            back();
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
        top();for(int scan=0;scan<35;scan++){AccessibilityNodeInfo found=findId(id,true);if(found!=null)return found;String before=labels();scroll(true);if(labels().equals(before))break;}return null;
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
    private void piece(int row,int col)throws Exception{String en="Piece, row "+(row+1)+", column "+(col+1)+",";String fr="Pièce, ligne "+(row+1)+", colonne "+(col+1)+",";seekTap(x->x.contains(en)||x.contains(fr),"real-piece-"+row+"-"+col);}
    private void snapshot(String name)throws Exception{
        JSONObject result=new JSONObject();for(String file:new String[]{"luma_drift_save_v1","luma_drift_meta_v1","luma_session","luma_expedition_session_v118","luma_victory_v120","luma_session_energy_v120"}){
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
    private int clockSeconds()throws Exception{
        AccessibilityNodeInfo node=find(x->x.startsWith("Your time ")||x.startsWith("Ton temps "),false);check(node!=null,"Actual visible informational clock label");String label=name(node);node.recycle();
        java.util.regex.Matcher match=java.util.regex.Pattern.compile("(\\d+):(\\d{2})").matcher(label);check(match.find(),"Clock has visible minutes and seconds");return Integer.parseInt(match.group(1))*60+Integer.parseInt(match.group(2));
    }
    private void seekTap(Predicate<String> match,String reason)throws Exception{
        for(int attempt=0;attempt<20;attempt++){AccessibilityNodeInfo node=find(match,true);if(node!=null){touchNode(node,reason);return;}if(attempt<3)SystemClock.sleep(200);else scroll(false);}
        for(int attempt=0;attempt<45;attempt++){AccessibilityNodeInfo node=find(match,true);if(node!=null){touchNode(node,reason);return;}String before=labels();scroll(true);if(labels().equals(before))break;}
        throw new AssertionError("Missing actual native target "+reason+": "+labels());
    }
    private void seekId(String id,String reason)throws Exception{
        AccessibilityNodeInfo node=findId(id,true);if(node!=null){touchNode(node,reason);return;}top();
        for(int n=0;n<45;n++){node=findId(id,true);if(node!=null){touchNode(node,reason);return;}String before=labels();scroll(true);if(labels().equals(before))break;}
        throw new AssertionError("Missing actual semantic target "+id+": "+labels());
    }
    private void inject(long down,int action,float x,float y){MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);try{if(!ui.injectInputEvent(event,true))throw new AssertionError("Native touchscreen refused");}finally{event.recycle();}}
    private void back(){foreground();ui.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);SystemClock.sleep(450);}
    private void scroll(boolean forward){foreground();AccessibilityNodeInfo root=ui.getRootInActiveWindow();boolean sent=false;if(root!=null){sent=root.performAction(forward?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);root.recycle();}if(!sent)for(AccessibilityNodeInfo node:nodes()){if(!sent&&node.isScrollable())sent=node.performAction(forward?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);node.recycle();}SystemClock.sleep(250);}
    private void top(){for(int n=0;n<25;n++){String before=labels();scroll(false);if(labels().equals(before))break;}}
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
