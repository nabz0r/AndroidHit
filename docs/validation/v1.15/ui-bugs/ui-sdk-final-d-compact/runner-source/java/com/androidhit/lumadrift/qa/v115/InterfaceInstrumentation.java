package com.androidhit.lumadrift.qa.v115;

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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Framework-only native UI observer. It sends actual touchscreen events to observed
 * accessible bounds. It never calls the engine, renderer, private fields or preference edits.
 */
public final class InterfaceInstrumentation extends Instrumentation {
    private static final String[][] NAV={{"Adventure","Home","Collection","Atelier"},{"Aventure","Maison","Collection","Atelier"},{"Abenteuer","Zuhause","Sammlung","Atelier"},{"Aventura","Casa","Colección","Taller"},{"Aventura","Casa","Coleção","Atelier"},{"Avventura","Casa","Collezione","Atelier"}};
    private static final String[] LANG_CODES={"en","fr","de","es","pt","it"};
    private static final String[] LANG_NAMES={"English","Français","Deutsch","Español","Português","Italiano"};
    private static final String[] SETTINGS={"Settings","Paramètres","Réglages","Einstellungen","Ajustes","Definições","Impostazioni"};
    private static final String[] INTRO_SKIP={"I know how to play","Je sais déjà jouer","Ich kenne das Spiel","Ya sé jugar","Já sei jogar","So già giocare","Play at my pace","Jouer à mon rythme","In meinem Tempo spielen","Jugar a mi ritmo","Jogar no meu ritmo","Gioca al tuo ritmo","Later","Plus tard","Später","Más tarde","Mais tarde","Più tardi"};
    private final JSONArray checks=new JSONArray(),actions=new JSONArray(),captures=new JSONArray();
    private Bundle args;private UiAutomation ui;private File output;private String target;private int count;
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);args=arguments==null?new Bundle():arguments;start();}
    @Override public void onStart(){new Thread(()->{
        boolean passed=false;Bundle result=new Bundle();
        try{
            target=getTargetContext().getPackageName();check(target.equals("com.androidhit.lumadrift")||target.equals("com.androidhit.lumadrift.sandbox"),"Exact Luma target package");
            ui=getUiAutomation();AccessibilityServiceInfo service=ui.getServiceInfo();service.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;ui.setServiceInfo(service);
            output=new File(getTargetContext().getExternalFilesDir(null),"v115-interface-ui");if(!output.exists()&&!output.mkdirs())throw new IllegalStateException("Cannot create SDK output");
            PackageInfo info=getTargetContext().getPackageManager().getPackageInfo(target,0);check(args.getString("expectedVersion","").equals(info.versionName)&&info.versionCode==Integer.parseInt(args.getString("expectedCode","17")),"Exact installed candidate version and code");
            launch();
            if("cold".equals(args.getString("phase","prepare")))cold();else prepare();
            passed=true;result.putString("summary","PASS "+count+" native interface checks");
        }catch(Throwable failure){result.putString("failure",failure.toString());try{if(output!=null){capture("failure");StringWriter stack=new StringWriter();failure.printStackTrace(new PrintWriter(stack));write("failure.txt",stack.toString());}}catch(Throwable ignored){}}
        finally{
            try{if(output!=null)write("report.json",new JSONObject().put("passed",passed).put("checkCount",count).put("checks",checks).put("actions",actions).put("captures",captures).put("processId",android.os.Process.myPid()).put("phase",args.getString("phase","prepare")).put("package",target).put("sdk",Build.VERSION.SDK_INT).put("abi",Build.SUPPORTED_ABIS[0]).put("scope","Native accessibility/touch navigation, three rooms, local zoom, decoration mode, six-language UI and cold persistence; no private reflection, profile seeding, purchases, provider action or puzzle solvability claim").toString(2));}catch(Throwable ignored){}
            finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
        }
    },"luma-v115-native-interface").start();}

    private void launch(){Intent intent=new Intent().setClassName(target,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);startActivitySync(intent);SystemClock.sleep(1200);}
    private void prepare()throws Exception{
        for(int i=0;i<3;i++){
            boolean mainNavigation=false;for(String[] language:NAV)if(has(new String[]{language[0]},true)){mainNavigation=true;break;}
            if(mainNavigation)break;
            // The visible skip may be below the first frame on compact portrait; tap seeks it by real scroll.
            tap(INTRO_SKIP);
        }
        english();navigate(0,0);capture("adventure-start-en");JSONObject economy=economy();
        for(int i=1;i<4;i++){navigate(0,i);capture(new String[]{"adventure","house-overview","collection","atelier"}[i]+"-en");}
        navigate(0,1);
        for(String room:new String[]{"Living room","Bedroom","Garden"}){tap(room);capture("house-"+room.toLowerCase().replace(' ','-')+"-en");}
        tap("Zoom in","+");capture("house-garden-zoom-in-en");tap("Zoom out","−","-");capture("house-garden-zoom-out-en");
        tap("Decorate","Decorate this room");check(waitHas(new String[]{"Done","Furniture"},true,4000),"Decoration mode exposes a bounded furniture/done action");capture("house-garden-decorate-en");
        tap("Little leaf plant");check(waitHas(new String[]{"Cancel"},true,4000),"Placement has an explicit visible Cancel action");capture("house-placement-cancel-en");tap("Cancel");tap("Done");
        navigate(0,2);tap("Companions");capture("companions-en");back();navigate(0,2);tap("Equipment");capture("equipment-en");tap("Moonbud, Equipped");capture("equipment-preview-en");back();back();
        navigate(0,2);tap("Wardrobe");capture("wardrobe-en");back();
        navigate(0,3);tap("Stellar");capture("atelier-stellar-catalog-en");tap("Moon reef aquarium, 420 LumaCoins");capture("aquarium-preview-en");back();back();
        navigate(0,3);tap("Neon");tap("Midnight grand piano, 560 LumaCoins");capture("piano-preview-en");back();back();
        navigate(0,3);tap("Forest");tap("Glass garden greenhouse, 640 LumaCoins");capture("greenhouse-preview-en");back();back();
        navigate(0,0);tap("All levels");capture("all-levels-en");back();
        for(int language=0;language<LANG_CODES.length;language++){
            settings();top();tap(LANG_NAMES[language]);back();navigate(language,0);capture("adventure-"+LANG_CODES[language]);navigate(language,1);capture("house-"+LANG_CODES[language]);
        }
        settings();top();tap("Français");back();navigate(1,1);tap("Jardin");capture("checkpoint-house-garden-fr");
        check(economy.toString().equals(economy().toString()),"Navigation and decoration browsing preserve earned coins and progression");
        JSONObject checkpoint=new JSONObject().put("preferences",persistent()).put("economy",economy()).put("language","fr").put("homeRoom","garden").put("preparePid",android.os.Process.myPid());
        check("fr".equals(save().getString("language","")),"French chosen through actual native language control");
        check("garden".equals(save().getString("home_room_v115","")),"Garden selected through the actual room control");
        write("checkpoint.json",checkpoint.toString(2));
    }
    private void cold()throws Exception{
        JSONObject checkpoint=new JSONObject(read("checkpoint.json"));
        check(checkpoint.getInt("preparePid")!=android.os.Process.myPid(),"Cold verification runs in a distinct process");
        check(checkpoint.getJSONObject("preferences").toString().equals(persistent().toString()),"Language, room, home objects and equipped choices survive cold launch byte-for-value");
        check(checkpoint.getJSONObject("economy").toString().equals(economy().toString()),"Earned coins and progression survive cold launch");
        check("fr".equals(save().getString("language","")),"Cold language remains French");navigate(1,1);check("garden".equals(save().getString("home_room_v115","")),"Cold room remains the Garden");capture("cold-house-garden-fr");navigate(1,0);capture("cold-adventure-fr");
    }
    private SharedPreferences save(){return getTargetContext().getSharedPreferences("luma_drift_save_v1",0);}
    private JSONObject persistent()throws Exception{
        TreeMap<String,Object> values=new TreeMap<>();for(Map.Entry<String,?> entry:save().getAll().entrySet()){String key=entry.getKey();if(key.equals("language")||key.startsWith("home_")||key.startsWith("equipped")||key.startsWith("avatar_style"))values.put(key,entry.getValue());}return json(values);
    }
    private JSONObject economy()throws Exception{
        TreeMap<String,Object> values=new TreeMap<>();for(Map.Entry<String,?> entry:save().getAll().entrySet()){String key=entry.getKey();if(key.equals("coins")||key.equals("unlocked")||key.contains("stars_")||key.startsWith("relay_unlocked")||key.startsWith("journey_unlocked"))values.put(key,entry.getValue());}return json(values);
    }
    private JSONObject json(Map<String,Object> values)throws Exception{JSONObject result=new JSONObject();for(Map.Entry<String,Object> item:values.entrySet()){Object value=item.getValue();if(value instanceof Set){List<String> list=new ArrayList<>();for(Object x:(Set<?>)value)list.add(String.valueOf(x));Collections.sort(list);value=new JSONArray(list);}result.put(item.getKey(),value);}return result;}
    private void english()throws Exception{settings();top();tap("English");back();}
    private void settings()throws Exception{
        if(has(SETTINGS,true)){tap(SETTINGS);return;}
        for(int i=0;i<5;i++){back();if(has(SETTINGS,true)){tap(SETTINGS);return;}}
        throw new AssertionError("No visible Settings entry: "+labels());
    }
    private void navigate(int language,int index)throws Exception{
        top();tap(NAV[language][index]);
        boolean selected=false;for(AccessibilityNodeInfo node:nodes()){if(node.isClickable()&&node.isSelected()&&matches(name(node),new String[]{NAV[language][index]}))selected=true;node.recycle();}
        check(selected,"Native destination selected: "+NAV[language][index]);
        for(String label:NAV[language])check(has(new String[]{label},true),"Four-destination navigation exposes "+label);
    }
    private String name(AccessibilityNodeInfo node){CharSequence description=node.getContentDescription();if(description!=null&&!description.toString().isEmpty())return description.toString();return node.getText()==null?"":node.getText().toString();}
    private boolean matches(String value,String[] requested){for(String label:requested)if(value.equals(label)||value.equals(label+"  →")||value.equals(label+" →"))return true;return false;}
    private void collect(AccessibilityNodeInfo node,List<AccessibilityNodeInfo> list){if(node==null||list.size()>600)return;list.add(AccessibilityNodeInfo.obtain(node));for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo child=node.getChild(i);if(child!=null){collect(child,list);child.recycle();}}}
    private List<AccessibilityNodeInfo> nodes(){if(Build.VERSION.SDK_INT>=33)ui.clearCache();List<AccessibilityNodeInfo> result=new ArrayList<>();AccessibilityNodeInfo node=ui.getRootInActiveWindow();if(node!=null){collect(node,result);node.recycle();}return result;}
    private boolean has(String[] requested,boolean clickable){boolean found=false;for(AccessibilityNodeInfo node:nodes()){if(node.isVisibleToUser()&&node.isEnabled()&&(!clickable||node.isClickable())&&matches(name(node),requested))found=true;node.recycle();}return found;}
    private boolean waitHas(String[] requested,boolean clickable,long timeout){long end=SystemClock.uptimeMillis()+timeout;do{if(has(requested,clickable))return true;SystemClock.sleep(60);}while(SystemClock.uptimeMillis()<end);return false;}
    private String labels(){List<String> result=new ArrayList<>();for(AccessibilityNodeInfo node:nodes()){String name=name(node);if(!name.isEmpty())result.add(name);node.recycle();}return result.toString();}
    private void tap(String... requested)throws Exception{
        long deadline=SystemClock.uptimeMillis()+4500;Rect bounds=null;String observed="";
        while(bounds==null&&SystemClock.uptimeMillis()<deadline){for(AccessibilityNodeInfo node:nodes()){if(bounds==null&&node.isVisibleToUser()&&node.isEnabled()&&node.isClickable()&&matches(name(node),requested)){bounds=new Rect();node.getBoundsInScreen(bounds);observed=name(node);}node.recycle();}if(bounds==null)SystemClock.sleep(80);}
        if(bounds==null){for(int i=0;i<8&&bounds==null;i++){if(!scroll(true))break;for(AccessibilityNodeInfo node:nodes()){if(bounds==null&&node.isVisibleToUser()&&node.isEnabled()&&node.isClickable()&&matches(name(node),requested)){bounds=new Rect();node.getBoundsInScreen(bounds);observed=name(node);}node.recycle();}}}
        if(bounds==null)throw new AssertionError("Missing native control "+Arrays.toString(requested)+"; visible="+labels());
        check(bounds.width()>0&&bounds.height()>0,"Observed visible touch bounds for "+observed);
        long down=SystemClock.uptimeMillis();inject(down,MotionEvent.ACTION_DOWN,bounds.exactCenterX(),bounds.exactCenterY());SystemClock.sleep(28);inject(down,MotionEvent.ACTION_UP,bounds.exactCenterX(),bounds.exactCenterY());
        actions.put(new JSONObject().put("label",observed).put("bounds",bounds.toShortString()).put("input","UiAutomation touchscreen"));SystemClock.sleep(230);
    }
    private void inject(long down,int action,float x,float y){MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);boolean sent=ui.injectInputEvent(event,true);event.recycle();if(!sent)throw new AssertionError("Touch event refused");}
    private boolean scroll(boolean forward){AccessibilityNodeInfo root=ui.getRootInActiveWindow();if(root==null)return false;boolean sent=root.performAction(forward?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);if(!sent){List<AccessibilityNodeInfo> all=nodes();for(AccessibilityNodeInfo node:all){if(!sent&&node.isScrollable())sent=node.performAction(forward?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);node.recycle();}}root.recycle();SystemClock.sleep(130);return sent;}
    private void top(){String before=labels();for(int i=0;i<12;i++){if(!scroll(false))break;String after=labels();if(after.equals(before))break;before=after;}}
    private void back(){ui.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);SystemClock.sleep(210);}
    private void capture(String id)throws Exception{
        for(int i=0;i<captures.length();i++)if(id.equals(captures.getString(i)))throw new AssertionError("Duplicate screenshot identifier in one native phase: "+id);
        Bitmap bitmap=ui.takeScreenshot();if(bitmap==null)throw new IllegalStateException("No native screenshot");try(OutputStream file=new FileOutputStream(new File(output,id+".png"))){if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,file))throw new IllegalStateException("PNG encoding failed");}finally{bitmap.recycle();}
        JSONArray snapshot=new JSONArray();for(AccessibilityNodeInfo node:nodes()){Rect box=new Rect();node.getBoundsInScreen(box);snapshot.put(new JSONObject().put("label",name(node)).put("bounds",new JSONArray(new int[]{box.left,box.top,box.right,box.bottom})).put("clickable",node.isClickable()).put("selected",node.isSelected()).put("visible",node.isVisibleToUser()));node.recycle();}write(id+".json",snapshot.toString(2));captures.put(id);
    }
    private void check(boolean passed,String message)throws Exception{count++;checks.put(new JSONObject().put("passed",passed).put("message",message));if(!passed)throw new AssertionError(message);Bundle update=new Bundle();update.putString("stream","PASS "+message+"\n");sendStatus(0,update);}
    private String read(String name)throws Exception{try(InputStream in=new FileInputStream(new File(output,name));ByteArrayOutputStream bytes=new ByteArrayOutputStream()){byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)bytes.write(buffer,0,n);return bytes.toString("UTF-8");}}
    private void write(String name,String value)throws Exception{try(OutputStream file=new FileOutputStream(new File(output,name))){file.write(value.getBytes(StandardCharsets.UTF_8));}}
}
