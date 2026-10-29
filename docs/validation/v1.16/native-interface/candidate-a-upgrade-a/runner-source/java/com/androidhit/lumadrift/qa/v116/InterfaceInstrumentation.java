package com.androidhit.lumadrift.qa.v116;

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
            output=new File(getTargetContext().getExternalFilesDir(null),"v116-interface-ui");if(!output.exists()&&!output.mkdirs())throw new IllegalStateException("Cannot create SDK output");
            PackageInfo info=getTargetContext().getPackageManager().getPackageInfo(target,0);check(args.getString("expectedVersion","").equals(info.versionName)&&info.versionCode==Integer.parseInt(args.getString("expectedCode","18")),"Exact installed candidate version and code");
            String phase=args.getString("phase","prepare");
            if("baseline".equals(phase))baseline();else{
                launch();
                if("prepare".equals(phase))prepare();else if("cold".equals(phase))cold();else if("browse".equals(phase))browse();else throw new IllegalArgumentException("Unknown phase "+phase);
            }
            passed=true;result.putString("summary","PASS "+count+" native interface checks");
        }catch(Throwable failure){result.putString("failure",failure.toString());try{if(output!=null){capture("failure");StringWriter stack=new StringWriter();failure.printStackTrace(new PrintWriter(stack));write("failure.txt",stack.toString());}}catch(Throwable ignored){}}
        finally{
            try{if(output!=null)write("report.json",new JSONObject().put("passed",passed).put("checkCount",count).put("checks",checks).put("actions",actions).put("captures",captures).put("processId",android.os.Process.myPid()).put("phase",args.getString("phase","prepare")).put("package",target).put("sdk",Build.VERSION.SDK_INT).put("abi",Build.SUPPORTED_ABIS[0]).put("scope",nativeScope()).toString(2));}catch(Throwable ignored){}
            finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
        }
    },"luma-v116-native-interface").start();}

    private String nativeScope(){if(args.getString("phase","").equals("baseline"))return "Read-only installed-base preference and complete puzzle-session snapshot before upgrade; no Activity launch, UI input, app/engine/private-field calls, seeding or provider action";return "Retained task-owned U1000 profile; actual SDK touchscreen and editable-text accessibility input, read-only preference observations, home camera, local identity, earned badges and two earned-coin banners with cold persistence; no app/engine/private-field calls, profile seeding, provider actions or user devices";}
    private void launch(){Intent intent=new Intent().setClassName(target,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);startActivitySync(intent);SystemClock.sleep(1200);}

    private static final String[] PROFILES={"Profile","Profil","Perfil","Profilo"};
    private static final String[][] COPY={
        {"Star arrivals","Rencontres stellaires","Sternenbegegnungen","Encuentros estelares","Encontros estelares","Incontri stellari"},
        {"Signature outfits","Tenues signatures","Signatur-Outfits","Conjuntos exclusivos","Conjuntos de assinatura","Completi esclusivi"},
        {"Pool & exact odds","Contenu et probabilités","Inhalt und genaue Chancen","Contenido y probabilidades","Conteúdo e probabilidades","Contenuto e probabilità"},
        {"Badge slot 1","Emplacement de badge 1","Abzeichenplatz 1","Espacio de insignia 1","Espaço de distintivo 1","Spazio distintivo 1"},
        {"Edit nickname","Modifier le pseudo","Spitzname ändern","Cambiar apodo","Alterar alcunha","Modifica soprannome"},
        {"See last arrival","Revoir la dernière rencontre","Letzte Begegnung ansehen","Ver último encuentro","Ver último encontro","Rivedi l’ultimo incontro"},
        {"Moonlight","Clair de lune","Mondschein","Luz de luna","Luar","Chiaro di luna"}
    };
    private void prepare()throws Exception{
        JSONObject baseline=new JSONObject(read("baseline.json"));
        check(baseline.getInt("processId")!=android.os.Process.myPid(),"Upgrade validation is independent of the original baseline process");
        check(baseline.getJSONObject("legacySave").toString().equals(legacySave().toString()),"Installing and launching1.16 preserves every observed legacy ownership, coin, progression, home, equipment and cosmetic receipt value");
        check(baseline.getJSONObject("session").toString().equals(playState().toString()),"Installing and launching1.16 preserves the complete original puzzle session");
        check(save().getInt("stars_1000",0)>0,"Upgrade uses the genuinely completed U1000 QA profile");
        String salon=save().getString("home_layout_v113","");check(salon.contains("aquarium,")&&salon.contains("piano,")&&salon.contains("record_player,"),"Retained furnished living room is present before testing");
        JSONObject progress=progression(),stock=inventory(),session=playState(),home=homeState();int initial=save().getInt("coins",-1);check(initial>=440,"Existing earned balance funds exactly two banners");
        write("before.json",new JSONObject().put("progression",progress).put("inventory",stock).put("playState",session).put("home",home).put("initialCoins",initial).toString(2));
        english();homeCamera();navigate(0,2);capture("collection-before-en");tap(PROFILES);capture("profile-before-en");
        tap("Edit nickname");setNickname("Luma QA 116");tap("Save");check("Luma QA 116".equals(save().getString("player_nickname_v116","")),"Visible nickname editing saves the actual chosen local identity");
        tap("LU");check("LU".equals(save().getString("player_flag_v116","")),"Visible flag control saves Luxembourg without changing account identity");
        String[][] chosen={{"First spark","first_win"},{"Star traveller","journey_100"},{"Interior artist","decorator_10"}};
        for(int slot=0;slot<3;slot++){
            profileTop();
            tap("Badge slot "+(slot+1));tapPrefix(chosen[slot][0]);
            check(chosen[slot][1].equals(save().getString("player_badge_v116_"+slot,"")),"Earned badge equips in explicit slot "+(slot+1));
        }
        profileTop();tap("Badge slot 1");
        if(!save().getBoolean("badge_earned_v116_collector_5",false)){
            JSONObject before=badgeSlots();tapPrefix("Circle of friends");check(before.toString().equals(badgeSlots().toString()),"A locked collector badge cannot replace an earned badge");
        }
        capture("badges-en");back();capture("profile-custom-en");navigate(0,2);tap("Star arrivals");capture("summon-companions-en");
        JSONObject avatar=drawBanner("AVATARS",180,"avatar"),outfit;
        tap("Signature outfits");capture("summon-signatures-en");outfit=drawBanner("SIGNATURES",260,"outfit");
        int refunds=avatar.getInt("compensation")+outfit.getInt("compensation");
        check(save().getInt("coins",-1)==initial-440+refunds,"Two banner transactions charge180+260 with exactly the documented duplicate returns");
        check(progress.toString().equals(progression().toString()),"Profile, badges and banners preserve every completed level and unlock");
        check(stock.toString().equals(inventory().toString()),"Profile and cosmetic banners preserve all power stock and receipts");
        check(session.toString().equals(playState().toString()),"Profile and collection preserve the exact puzzle session");
        check(home.toString().equals(homeState().toString()),"Camera and collection preserve all furniture ownership and exact room layouts");
        settings();top();tap("Français");back();navigate(1,1);tap("Salon");capture("home-furnished-fr");
        navigate(1,2);capture("collection-fr");tap(PROFILES);capture("profile-fr");
        write("checkpoint.json",new JSONObject().put("preferences",persistent()).put("economy",economy()).put("inventory",inventory()).put("playState",playState()).put("home",homeState()).put("avatarReceipt",avatar).put("outfitReceipt",outfit).put("initialCoins",initial).put("grossSpentCoins",440).put("duplicateReturns",refunds).put("preparePid",android.os.Process.myPid()).toString(2));
    }
    /** No Activity launch and no input: observe the installed base before in-place upgrade. */
    private void baseline()throws Exception{
        JSONObject save=legacySave(),session=playState();
        check(save.has("coins")&&save.has("home_layout_v113"),"Read-only baseline observes an existing furnished legacy profile");
        write("baseline.json",new JSONObject().put("legacySave",save).put("session",session).put("processId",android.os.Process.myPid()).put("version",args.getString("expectedVersion","")).put("versionCode",Integer.parseInt(args.getString("expectedCode","0"))).toString(2));
        check(save.toString().equals(legacySave().toString())&&session.toString().equals(playState().toString()),"Baseline inspection makes no preference or puzzle-session change");
    }
    private void homeCamera()throws Exception{
        navigate(0,1);tap("Living room");check(waitHas(new String[]{"Whole room"},true,4000),"Upgraded home starts at the enlarged detail scale");capture("home-detail-en");
        Rect detail=bounds("Home scene");check(detail!=null&&detail.width()>0&&detail.height()>0,"Actual accessible home viewport is available");
        tap("Whole room");check(has(new String[]{"Close-up"},true),"Explicit overview offers return to close-up");capture("home-overview-en");
        Rect aquarium=bounds("Moon reef aquarium");check(aquarium!=null&&detail.contains(aquarium.centerX(),aquarium.centerY()),"Furnished aquarium exposes an in-scene native touch target");
        tap("Decorate");tap("Moon reef aquarium");check(has(new String[]{"Cancel"},true),"Native aquarium target opens its actual decoration preview");capture("home-aquarium-selection-en");tap("Cancel");tap("Done");
        tap("Close-up");tap("Zoom in");capture("home-zoom-en");panHome();capture("home-panned-en");tap("Zoom out");
        tap("Whole room");tap("Bedroom");capture("home-bedroom-overview-en");check(has(new String[]{"Close-up"},true),"Changing rooms preserves the user's overview zoom");tap("Living room");tap("Close-up");
    }
    private JSONObject drawBanner(String banner,int price,String prefix)throws Exception{
        JSONObject before=receipts();int coins=save().getInt("coins",-1);
        tap("Pool & exact odds");capture(prefix+"-pool-en");check(scroll(true),"Exact odds pool supports real native scrolling");capture(prefix+"-pool-scrolled-en");back();
        check(coins==save().getInt("coins",-1)&&before.toString().equals(receipts().toString()),"Viewing the "+banner+" pool creates no draw or debit");
        tap("Draw once · "+price+" LumaCoins");check(waitHas(new String[]{"Equip this look","Equipped"},true,7000),"A completed "+banner+" draw opens its full result");capture(prefix+"-reveal-en");
        JSONObject after=receipts();List<String> added=new ArrayList<>();for(java.util.Iterator<String> it=after.keys();it.hasNext();){String key=it.next();if(!before.has(key))added.add(key);}
        check(added.size()==1,"One visible draw creates exactly one durable "+banner+" receipt");String key=added.get(0),record=after.getString(key);String[] parts=record.split(":",-1);
        check(parts.length==4&&banner.equals(parts[0])&&(parts[2].equals("0")||parts[2].equals("1")),"Receipt records the original banner, result and duplicate decision");
        String item=parts[1];int compensation=Integer.parseInt(parts[3]);check(save().getInt("coins",-1)==coins-price+compensation,"Exact "+banner+" coin charge and duplicate return");
        check(key.equals("summon_v116_1_"+sha256(save().getString("summon_last_request_v116_1",""))),"Last reveal resolves to the same durable request receipt");
        if("AVATARS".equals(banner))check(save().getBoolean("owned_character_"+item,false),"Drawn companion is genuinely owned");
        else for(String cosmetic:outfitPieces(item))check(save().contains("cosmetic_owned_v17_"+cosmetic),"Drawn signature owns original clothing piece "+cosmetic);
        if(has(new String[]{"Equip this look"},true))tap("Equip this look");
        if("AVATARS".equals(banner))check(item.equals(save().getString("character","")),"Reveal equips the exact drawn companion");
        else {String[] slots={"HAT","DRESS","SHOES","GLOVES"},pieces=outfitPieces(item);for(int i=0;i<slots.length;i++)check(pieces[i].equals(save().getString("cosmetic_slot_v17_"+slots[i],"")),"Reveal equips signature clothing slot "+slots[i]);}
        capture(prefix+"-equipped-en");tap("Back to arrivals");tap("See last arrival");capture(prefix+"-replayed-en");
        check(after.toString().equals(receipts().toString())&&save().getInt("coins",-1)==coins-price+compensation,"Reopening the completed reveal cannot charge or draw again");tap("Back to arrivals");
        return new JSONObject().put("key",key).put("record",record).put("item",item).put("banner",banner).put("charged",price).put("compensation",compensation);
    }
    private String[] outfitPieces(String id){
        if(id.equals("moon"))return new String[]{"hat_moon","dress_moon","shoes_moon","gloves_moon"};
        if(id.equals("star"))return new String[]{"hat_star","dress_star","shoes_star","gloves_star"};
        if(id.equals("harvest"))return new String[]{"hat_pumpkin","dress_witch","shoes_pumpkin","gloves_witch"};
        if(id.equals("heart"))return new String[]{"hat_cupid","dress_valentine","shoes_cupid","gloves_valentine"};
        if(id.equals("aurora"))return new String[]{"hat_aurora","dress_aurora","shoes_aurora","gloves_aurora"};
        throw new AssertionError("Unknown signature "+id);
    }
    private void cold()throws Exception{
        JSONObject checkpoint=new JSONObject(read("checkpoint.json"));check(checkpoint.getInt("preparePid")!=android.os.Process.myPid(),"Cold verification runs in a distinct process");
        check(checkpoint.getJSONObject("preferences").toString().equals(persistent().toString()),"Nickname, flag, three badges, exact draws, ownership, equipment and house survive cold launch");
        check(checkpoint.getJSONObject("economy").toString().equals(economy().toString()),"Actual180+260 debits and complete progression survive cold launch");
        check(checkpoint.getJSONObject("inventory").toString().equals(inventory().toString())&&checkpoint.getJSONObject("playState").toString().equals(playState().toString()),"Unchanged power stock and puzzle session survive cold launch");
        check(checkpoint.getJSONObject("home").toString().equals(homeState().toString()),"Exact furnished layout survives cold launch");
        navigate(1,1);tap("Salon");capture("cold-home-fr");navigate(1,2);capture("cold-collection-fr");tap(PROFILES);capture("cold-profile-fr");
        navigate(1,2);tap("Rencontres stellaires");tap("Revoir la dernière rencontre");capture("cold-last-reveal-fr");
        check(checkpoint.getJSONObject("economy").toString().equals(economy().toString()),"Cold last-reveal recovery adds no new charge");
    }
    private void browse()throws Exception{
        JSONObject progress=progression(),stock=inventory(),session=playState(),receipt=receipts(),home=homeState();int coins=save().getInt("coins",-1);
        for(int language=0;language<LANG_CODES.length;language++){
            settings();top();tap(LANG_NAMES[language]);back();navigate(language,0);capture("adventure-"+LANG_CODES[language]);navigate(language,1);capture("home-"+LANG_CODES[language]);navigate(language,2);capture("collection-"+LANG_CODES[language]);
            tap(PROFILES);check(has(new String[]{COPY[4][language]},true),"Localized nickname editor is accessible in "+LANG_CODES[language]);capture("profile-"+LANG_CODES[language]);
            tap(COPY[3][language]);capture("badges-"+LANG_CODES[language]);back();navigate(language,2);tap(COPY[0][language]);capture("summon-companions-"+LANG_CODES[language]);
            tap(COPY[2][language]);capture("avatar-pool-"+LANG_CODES[language]);check(scroll(true),"Companion odds pool scrolls in "+LANG_CODES[language]);capture("avatar-pool-bottom-"+LANG_CODES[language]);back();
            tap(COPY[1][language]);capture("summon-signatures-"+LANG_CODES[language]);tap(COPY[2][language]);capture("outfit-pool-"+LANG_CODES[language]);tapPrefix(COPY[6][language]);capture("outfit-preview-"+LANG_CODES[language]);back();back();
            tap(COPY[5][language]);capture("reveal-"+LANG_CODES[language]);back();navigate(language,3);capture("atelier-"+LANG_CODES[language]);
        }
        check(coins==save().getInt("coins",-1)&&receipt.toString().equals(receipts().toString()),"Six-language browsing creates no purchase, draw or coin change");
        check(progress.toString().equals(progression().toString())&&stock.toString().equals(inventory().toString())&&session.toString().equals(playState().toString()),"Language and compact browsing preserve game progression, stock and puzzle");
        check(home.toString().equals(homeState().toString()),"Language browsing preserves all exact room layouts and home ownership");
        settings();top();tap("Français");back();navigate(1,1);capture("delivery-house-fr");navigate(1,2);capture("delivery-collection-fr");tap(PROFILES);capture("delivery-profile-fr");
    }
    private void setNickname(String value)throws Exception{
        boolean changed=false;for(AccessibilityNodeInfo node:nodes()){
            if(!changed&&node.isVisibleToUser()&&node.isEditable()){
                Bundle text=new Bundle();text.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,value);changed=node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,text);
            }node.recycle();
        }
        check(changed,"Framework ACTION_SET_TEXT edits only the visible native nickname field");SystemClock.sleep(150);
    }
    private void profileTop(){top();if(!has(new String[]{"Edit nickname"},true)){back();top();}}
    private Rect bounds(String label){Rect result=null;for(AccessibilityNodeInfo node:nodes()){if(result==null&&node.isVisibleToUser()&&matches(name(node),new String[]{label})){result=new Rect();node.getBoundsInScreen(result);}node.recycle();}return result;}
    private void panHome()throws Exception{
        Rect scene=bounds("Home scene"),before=bounds("Moon reef aquarium");check(scene!=null,"Pan uses actual accessible scene bounds");float y=scene.top+scene.height()*.58f,x=scene.left+scene.width()*.68f,end=scene.left+scene.width()*.32f;
        long down=SystemClock.uptimeMillis();inject(down,MotionEvent.ACTION_DOWN,x,y);for(int step=1;step<=12;step++){SystemClock.sleep(16);inject(down,MotionEvent.ACTION_MOVE,x+(end-x)*step/12f,y);}inject(down,MotionEvent.ACTION_UP,end,y);
        actions.put(new JSONObject().put("input","UiAutomation touchscreen pan within observed home viewport").put("bounds",scene.toShortString()));SystemClock.sleep(200);
        Rect after=bounds("Moon reef aquarium");check(before!=null&&after!=null&&before.centerX()!=after.centerX(),"A real pan moves the visible aquarium target with the scene");
    }
    private void tapPrefix(String prefix)throws Exception{top();for(int i=0;i<28;i++){String found="";for(AccessibilityNodeInfo node:nodes()){if(found.isEmpty()&&node.isVisibleToUser()&&node.isClickable()&&name(node).startsWith(prefix))found=name(node);node.recycle();}if(!found.isEmpty()){tap(found);return;}if(!scroll(true))break;}throw new AssertionError("Missing item prefix "+prefix+";"+labels());}
    private JSONObject homeState()throws Exception{TreeMap<String,Object> values=new TreeMap<>();for(Map.Entry<String,?> entry:save().getAll().entrySet()){String key=entry.getKey();if(key.startsWith("home_")&&!key.equals("home_room_v115"))values.put(key,entry.getValue());}return json(values);}
    private JSONObject badgeSlots()throws Exception{JSONObject result=new JSONObject();for(int slot=0;slot<3;slot++)result.put("slot"+slot,save().getString("player_badge_v116_"+slot,""));return result;}
    private JSONObject receipts()throws Exception{TreeMap<String,Object> values=new TreeMap<>();for(Map.Entry<String,?> entry:save().getAll().entrySet())if(entry.getKey().startsWith("summon_v116_1_"))values.put(entry.getKey(),entry.getValue());return json(values);}
    private String sha256(String text)throws Exception{byte[] bytes=java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));StringBuilder value=new StringBuilder();for(byte part:bytes)value.append(String.format(java.util.Locale.ROOT,"%02x",part&255));return value.toString();}
    private JSONObject progression()throws Exception{TreeMap<String,Object> values=new TreeMap<>();for(Map.Entry<String,?> entry:save().getAll().entrySet()){String key=entry.getKey();if(key.equals("unlocked")||key.contains("stars_")||key.startsWith("relay_unlocked")||key.startsWith("journey_unlocked"))values.put(key,entry.getValue());}return json(values);}
    private boolean progressionsEqual(JSONObject before)throws Exception{return before.toString().equals(progression().toString());}
    private JSONObject inventory()throws Exception{TreeMap<String,Object> values=new TreeMap<>();for(Map.Entry<String,?> entry:save().getAll().entrySet()){String key=entry.getKey();if(key.startsWith("booster_")||key.startsWith("power_")||key.startsWith("pending_booster"))values.put(key,entry.getValue());}return json(values);}
    private JSONObject playState()throws Exception{TreeMap<String,Object> values=new TreeMap<>();values.putAll(getTargetContext().getSharedPreferences("luma_session",0).getAll());return json(values);}
    private JSONObject actionState()throws Exception{JSONObject full=playState(),result=new JSONObject();for(String key:new String[]{"active","level","mode","event","event_id","history","undos","content_version","budget_policy","budget_used","budget_extensions","budget_extra"})if(full.has(key))result.put(key,full.get(key));return result;}
    private SharedPreferences save(){return getTargetContext().getSharedPreferences("luma_drift_save_v1",0);}
    private JSONObject persistent()throws Exception{
        TreeMap<String,Object> values=new TreeMap<>();for(Map.Entry<String,?> entry:save().getAll().entrySet()){String key=entry.getKey();
            if(key.contains("seed"))continue;
            if(key.equals("language")||key.equals("character")||key.equals("skin")||key.startsWith("home_")||key.startsWith("equipped")||key.startsWith("avatar_style")||key.startsWith("player_")||key.startsWith("badge_")||key.startsWith("owned_character_")||key.startsWith("cosmetic_")||key.startsWith("summon_")&&!key.startsWith("summon_seed_"))values.put(key,entry.getValue());
        }return json(values);
    }
    private JSONObject legacySave()throws Exception{
        TreeMap<String,Object> values=new TreeMap<>();for(Map.Entry<String,?> entry:save().getAll().entrySet()){String key=entry.getKey();
            if(key.contains("seed")||key.startsWith("player_")||key.startsWith("badge_")||key.startsWith("summon_"))continue;
            if(key.equals("coins")||key.equals("language")||key.equals("unlocked")||key.contains("stars_")||key.equals("character")||key.equals("skin")||key.startsWith("home_")||key.startsWith("owned_")||key.startsWith("equipped")||key.startsWith("avatar_")||key.startsWith("cosmetic_")||key.startsWith("booster_")||key.startsWith("power_")||key.startsWith("pending_booster")||key.startsWith("relay_unlocked")||key.startsWith("journey_unlocked"))values.put(key,entry.getValue());
        }return json(values);
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
