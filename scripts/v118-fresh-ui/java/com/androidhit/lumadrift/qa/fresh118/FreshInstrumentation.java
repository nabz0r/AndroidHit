package com.androidhit.lumadrift.qa.fresh118;

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

/** Independent SDK-only native observer. No target imports, reflection or save editor. */
public final class FreshInstrumentation extends Instrumentation {
    private Bundle args; private UiAutomation ui; private File output; private String target;
    private final JSONArray checks=new JSONArray(),actions=new JSONArray(),captures=new JSONArray();
    private int touches;
    private static final String[] CODES={"en","fr","de","es","pt","it"};
    private static final String[] LANGUAGES={"English","Français","Deutsch","Español","Português","Italiano"};
    private static final String[] SETTINGS={"Settings","Réglages","Einstellungen","Ajustes","Definições","Impostazioni"};
    private static final String[] HOMES={"My home","Ma maison","Mein Zuhause","Mi hogar","A minha casa","La mia casa"};
    private static final String[] COLLECTIONS={"Collection","Collection","Sammlung","Colección","Coleção","Collezione"};
    private static final String[] COMPANIONS={"My companions","Mes compagnons","Meine Begleiter","Mis compañeros","Os meus companheiros","I miei compagni"};
    private static final String[] STUDIOS={"Companion studio","Studio des compagnons","Begleiterstudio","Estudio de compañeros","Estúdio de companheiros","Studio dei compagni"};
    private static final String[] GUARDS={"Guard","Garde","Deckung","Guardia","Guarda","Guardia"};
    private static final String[] PREVIEWS={"Preview at level 10 · your saved level stays the same.","Aperçu au niveau 10 · ton niveau enregistré reste le même.","Vorschau auf Level 10 · dein gespeichertes Level bleibt gleich.","Vista previa en el nivel 10 · tu nivel guardado no cambia.","Pré-visualização no nível 10 · o nível guardado mantém-se.","Anteprima al livello 10 · il livello salvato resta invariato."};
    private static final String[] WARDROBES={"Wardrobe","Garde-robe","Garderobe","Vestuario","Guarda-roupa","Guardaroba"};
    private static final String[] ORIGINAL_COSTUMES={"Original costume · change","Costume du compagnon · changer","Originalkostüm · wechseln","Traje original · cambiar","Traje original · mudar","Costume originale · cambia"};
    private static final String[] WORN_COSTUMES={"My outfit · change","Ma tenue · changer","Mein Outfit · wechseln","Mi conjunto · cambiar","A minha roupa · mudar","Il mio completo · cambia"};
    private static final String COSTUME_KEY="companion_original_costume_v118";
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);args=arguments==null?new Bundle():arguments;start();}
    @Override public void onStart(){new Thread(()->{
        boolean passed=false; Bundle result=new Bundle();
        try {
            target=getTargetContext().getPackageName(); check(target.equals("com.androidhit.lumadrift"),"Exact production target package");
            check(args.getString("runToken","").matches("[a-f0-9-]{36}"),"Unique host execution token");
            String phase=args.getString("phase",""); check(Arrays.asList("fresh-firstlight","fresh-continue","six-languages","compact-english","cold").contains(phase),"Explicit bounded fresh qualification phase");
            output=new File(getTargetContext().getExternalFilesDir(null),"v118-fresh/"+args.getString("runToken")+"/"+phase); check(!output.exists()&&output.mkdirs(),"Fresh native evidence directory");
            PackageInfo info=getTargetContext().getPackageManager().getPackageInfo(target,0); check(info.versionName.equals(args.getString("expectedVersion"))&&info.versionCode==Integer.parseInt(args.getString("expectedCode")),"Exact installed version and code");
            ui=getUiAutomation(); AccessibilityServiceInfo service=ui.getServiceInfo(); service.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS|AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS; ui.setServiceInfo(service);
            snapshot("before");
            if(phase.equals("fresh-firstlight")){check(!save().contains("initialized"),"Production save did not exist before its first Activity launch");check(save().getAll().isEmpty(),"Genuinely empty production game preferences before first Activity launch");}
            startActivitySync(new Intent().setClassName(target,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK)); SystemClock.sleep(1800);
            if(phase.equals("fresh-firstlight"))firstLight();
            if(phase.equals("fresh-firstlight")||phase.equals("fresh-continue")){setLanguage(0);capture("en-first-adventure");home(0);capture("en-first-home");openInventory(0);guestPages("en");studio(0,"en");}
            else if(phase.equals("six-languages")){for(int language=0;language<6;language++){setLanguage(language);capture(CODES[language]+"-adventure");home(language);capture(CODES[language]+"-home");openInventory(language);guestPages(CODES[language]);studio(language,CODES[language]);if(costumeControls())wardrobeCostume(language,CODES[language]);}setLanguage(0);}
            else if(phase.equals("compact-english")){setLanguage(0);capture("compact-adventure");home(0);capture("compact-home");openInventory(0);guestPages("compact");studio(0,"compact");if(costumeControls())wardrobeCostume(0,"compact");}
            else if(phase.equals("cold"))capture("cold-entry");
            pristine(); snapshot("after"); passed=true; result.putString("summary","PASS "+checks.length()+" native checks and "+touches+" real touches");
        } catch(Throwable failure) { result.putString("failure",failure.toString());try{if(output!=null){StringWriter stack=new StringWriter();failure.printStackTrace(new PrintWriter(stack));write("failure.txt",stack.toString());if(ui!=null)capture("failure");}}catch(Throwable ignored){} }
        finally {try{if(output!=null)write("report.json",new JSONObject().put("passed",passed).put("phase",args.getString("phase")).put("runToken",args.getString("runToken")).put("processId",android.os.Process.myPid()).put("checkCount",checks.length()).put("checks",checks).put("touchCount",touches).put("actions",actions).put("captures",captures).put("package",target).put("scope","Actual SDK touchscreen input at observed accessibility bounds and read-only preferences on the new empty task-owned AVD. No target imports, reflection, solver, save edits, grants, unlocks, ads, provider, purchase, identity or publication. Original player profiles untouched.").toString(2));}catch(Throwable ignored){}finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);}
    },"luma-v118-fresh-native").start();}
    private SharedPreferences save(){return getTargetContext().getSharedPreferences("luma_drift_save_v1",0);}
    private boolean costumeControls(){return args.getString("costumeControls","false").equals("true");}
    private Map<String,Map<String,?>> allPreferences(){
        Map<String,Map<String,?>> result=new TreeMap<>();
        for(String pref:new String[]{"luma_drift_save_v1","luma_drift_meta_v1","luma_session","luma_expedition_session_v118"})result.put(pref,new TreeMap<>(getTargetContext().getSharedPreferences(pref,0).getAll()));
        return result;
    }
    private void firstLight()throws Exception {
        if(costumeControls()){
            check(!save().contains(COSTUME_KEY),"Fresh original costume is calculated without a synthetic presentation choice");
            for(String key:save().getAll().keySet())check(!key.startsWith("cosmetic_slot_v17_"),"Fresh launch has no explicit historical wardrobe slot: "+key);
        }
        pristine(); check(!save().getBoolean("first_light_seen_v113",false),"Real first light has not been completed or skipped");check(save().getString("first_light_history_v113","").isEmpty(),"Empty first-light action history");capture("firstlight-new");
        piece(2,0);piece(1,2);piece(3,1);check(save().getString("first_light_history_v113","").equals("0,1,2"),"Three actual piece exits persisted in order");capture("firstlight-three-real-exits");
        tap(x->x.startsWith("Discover your little home")||x.startsWith("Découvre ton petit chez-toi"),"finish-real-first-light");check(save().getBoolean("first_light_seen_v113",false),"Real first-light completion persisted");
    }
    private void pristine()throws Exception {
        check(save().getInt("coins",-1)==180,"Fresh wallet remains its documented 180 starter coins");
        check(save().getString("character","").equals("nova"),"Starter Nova remains equipped");
        check(save().getInt("unlocked",1)==1,"Only campaign U1 is unlocked");
        Set<String> owned=new TreeSet<>();
        for(Map.Entry<String,?> entry:save().getAll().entrySet()){
            String key=entry.getKey();Object value=entry.getValue();
            if(key.startsWith("owned_character_")&&Boolean.TRUE.equals(value))owned.add(key.substring("owned_character_".length()));
            if(key.startsWith("stars_"))check(value instanceof Integer&&((Integer)value)==0,"No synthetic campaign star: "+key);
            if(key.startsWith("companion_copies_v118_"))check(value instanceof Integer&&((Integer)value)==0,"Preview does not grant companion copies: "+key);
            check(!key.startsWith("summon_seed_")&&!key.startsWith("summon_result_")&&!key.startsWith("expedition_v118_reward_")&&!key.equals("expedition_v118_run"),"No summon or expedition result injection: "+key);
        }
        check(owned.equals(Collections.singleton("nova")),"Exactly the real starter Nova is owned; all guests remain locked");
        check(save().getInt("expedition_v118_completed",0)==0,"No synthetic expedition clear");
    }
    private void piece(int row,int col)throws Exception {tap(x->x.contains("Piece, row "+(row+1)+", column "+(col+1)+",")||x.contains("Pièce, ligne "+(row+1)+", colonne "+(col+1)+","),"firstlight-real-piece-"+row+"-"+col);}
    private boolean oneOf(String value,String[] options){return Arrays.asList(options).contains(value);}
    private void adventure()throws Exception {for(int i=0;i<16;i++){AccessibilityNodeInfo node=findId("expedition_entry");if(node!=null){node.recycle();return;}back();}throw new AssertionError("Cannot return to actual adventure: "+labels());}
    private void setLanguage(int language)throws Exception {adventure();tap(x->oneOf(x,SETTINGS),"open-actual-settings");seekTap(x->x.equals(LANGUAGES[language]),"choose-language-"+CODES[language]);check(save().getString("language","").equals(CODES[language]),"Actual UI persists chosen language "+CODES[language]);capture(CODES[language]+"-settings-"+captures.length());back();adventure();}
    private void home(int language)throws Exception {adventure();seekTap(x->x.equals(HOMES[language]),"open-actual-home-"+CODES[language]);}
    private void openInventory(int language)throws Exception {seekTap(x->x.equals(COLLECTIONS[language]),"open-collection-root-"+CODES[language]);seekTap(x->x.equals(COMPANIONS[language]),"open-real-companion-book-"+CODES[language]);seekTap(x->x.equals("Mini LoL"),"choose-actual-Mini-LoL-filter");}
    private void guestPages(String prefix)throws Exception {
        String[][] expected={{"Ashe","Ezreal","Soraka","Ahri"},{"Jinx","Lux","Teemo","Yasuo"},{"Annie","Lulu","Sona","Miss Fortune"},{"Vi"}};JSONArray pages=new JSONArray();
        for(int page=0;page<4;page++){
            top();Set<String> seen=new TreeSet<>();JSONArray observations=new JSONArray();
            for(int scan=0;scan<8;scan++){
                for(AccessibilityNodeInfo node:nodes()){String label=name(node);if(node.isClickable()&&node.isVisibleToUser())for(String title:expected[page])if(label.startsWith(title+", ")){seen.add(title);observations.put(label);}node.recycle();}
                capture(prefix+"-mini-lol-page-"+(page+1)+"-body-"+scan);String before=labels();scroll(true);if(labels().equals(before))break;
            }
            check(seen.equals(new TreeSet<>(Arrays.asList(expected[page]))),"Actual reachable Mini LoL names on page "+(page+1)+": "+seen);pages.put(new JSONObject().put("page",page+1).put("names",new JSONArray(seen)).put("observedLabels",observations));
            if(page<3)seekTap(x->x.equals("›"),"actual-next-guest-page");
        }
        write(prefix+"-guest-pages-observed.json",pages.toString(2));
    }
    private void studio(int language,String prefix)throws Exception {
        // Teemo is page 2, third cell; viewing its authored future form must remain free of ownership grants.
        for(int page=0;page<2;page++)seekTap(x->x.equals("‹"),"return-to-Teemo-page");
        seekTap(x->x.startsWith("Teemo, "),"open-locked-Teemo-detail");seekTap(x->x.equals(STUDIOS[language]),"open-real-companion-studio");
        seekId("motion_form_3","preview-authored-form-10");seekId("motion_stance_guard","preview-guard-for-locked-guest");top();
        AccessibilityNodeInfo label=find(x->x.startsWith("Teemo. "+GUARDS[language]+". "+PREVIEWS[language]),false);boolean visible=label!=null;if(label!=null)label.recycle();check(visible,"Localized form10 guard preview announces saved level is unchanged");
        capture(prefix+"-teemo-form10-guard");
        if(costumeControls()){
            Map<String,Map<String,?>> before=allPreferences();
            seekId("motion_outfit_toggle","preview-worn-costume-through-reachable-control");
            checkControlLabel("motion_outfit_toggle",WORN_COSTUMES[language]);
            capture(prefix+"-studio-worn-costume-control");
            top();capture(prefix+"-teemo-form10-guard-worn");
            seekId("motion_outfit_toggle","restore-original-preview-through-reachable-control");
            checkControlLabel("motion_outfit_toggle",ORIGINAL_COSTUMES[language]);
            check(before.equals(allPreferences()),"Studio costume preview leaves all four preference files exactly unchanged");
            capture(prefix+"-studio-original-costume-control");
        }
        pristine();adventure();
    }
    private void checkControlLabel(String id,String expected)throws Exception {
        AccessibilityNodeInfo node=findId(id);boolean matches=node!=null&&name(node).equals(expected);if(node!=null)node.recycle();
        check(matches,"Reachable localized costume control "+id+": "+expected);
    }
    private void wardrobeCostume(int language,String prefix)throws Exception {
        home(language);seekTap(x->x.equals(WARDROBES[language]),"open-actual-wardrobe-"+CODES[language]);
        Map<String,Map<String,?>> before=allPreferences();
        check(save().getBoolean(COSTUME_KEY,true),"Original costume selected before bounded wardrobe check");
        seekId("wardrobe_costume_toggle","wear-selected-items-through-reachable-wardrobe-control");
        check(save().contains(COSTUME_KEY)&&!save().getBoolean(COSTUME_KEY,true),"Actual wardrobe control persists worn costume only");
        checkControlLabel("wardrobe_costume_toggle",WORN_COSTUMES[language]);capture(prefix+"-wardrobe-worn-control");
        seekId("wardrobe_costume_toggle","restore-original-through-reachable-wardrobe-control");
        check(save().contains(COSTUME_KEY)&&save().getBoolean(COSTUME_KEY,false),"Actual wardrobe control persists original costume only");
        checkControlLabel("wardrobe_costume_toggle",ORIGINAL_COSTUMES[language]);capture(prefix+"-wardrobe-original-control");
        Map<String,Map<String,?>> after=allPreferences();before.get("luma_drift_save_v1").remove(COSTUME_KEY);after.get("luma_drift_save_v1").remove(COSTUME_KEY);
        check(before.equals(after),"Wardrobe toggle changes only its explicit costume boolean across all four preference files");
        pristine();adventure();
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
