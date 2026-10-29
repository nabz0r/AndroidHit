package com.androidhit.lumadrift.qa.v117;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.KeyEvent;
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
    private static final String[][] NAV={{"Adventure","My home","Collection","Atelier"},{"Aventure","Ma maison","Collection","Atelier"},{"Abenteuer","Mein Zuhause","Sammlung","Atelier"},{"Aventura","Mi hogar","Colección","Taller"},{"Aventura","A minha casa","Coleção","Atelier"},{"Avventura","La mia casa","Collezione","Atelier"}};
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
            output=new File(getTargetContext().getExternalFilesDir(null),"v117-interface-ui");if(!output.exists()&&!output.mkdirs())throw new IllegalStateException("Cannot create SDK output");
            PackageInfo info=getTargetContext().getPackageManager().getPackageInfo(target,0);check(args.getString("expectedVersion","").equals(info.versionName)&&info.versionCode==Integer.parseInt(args.getString("expectedCode","19")),"Exact installed candidate version and code");
            String phase=args.getString("phase","prepare");
            if("baseline".equals(phase))baseline();else if("assets".equals(phase))decodeAssets();else{
                launch();
                if("prepare".equals(phase))prepare();else if("cold".equals(phase))cold();else if("browse".equals(phase))browse();else if("banners".equals(phase))banners();else throw new IllegalArgumentException("Unknown phase "+phase);
            }
            passed=true;result.putString("summary","PASS "+count+" native interface checks");
        }catch(Throwable failure){result.putString("failure",failure.toString());try{if(output!=null){capture("failure");StringWriter stack=new StringWriter();failure.printStackTrace(new PrintWriter(stack));write("failure.txt",stack.toString());}}catch(Throwable ignored){}}
        finally{
            try{if(output!=null)write("report.json",new JSONObject().put("passed",passed).put("checkCount",count).put("checks",checks).put("actions",actions).put("captures",captures).put("processId",android.os.Process.myPid()).put("phase",args.getString("phase","prepare")).put("package",target).put("sdk",Build.VERSION.SDK_INT).put("abi",Build.SUPPORTED_ABIS[0]).put("scope",nativeScope()).toString(2));}catch(Throwable ignored){}
            finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
        }
    },"luma-v117-native-interface").start();}

    private String nativeScope(){
        if(args.getString("phase","").equals("baseline"))return "Read-only installed-base baseline including legacy save, profile, badges, receipts and complete session; seeds excluded; no Activity launch or input";
        if(args.getString("phase","").equals("assets"))return "SDK BitmapFactory inspection of102 packaged original sprites and3 scene paintings; separate from actual app UI evidence; no renderer calls or state writes";
        return "Retained task-owned U1000 profile; actual SDK touchscreen/object/gesture/IME actions, read-only preference observations and real app screenshots; no spending, app/engine/private-field calls, seeding, provider action or user devices";
    }
    private void launch(){Intent intent=new Intent().setClassName(target,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);startActivitySync(intent);SystemClock.sleep(1200);}

    private static final String[] PROFILES={"Profile","Profil","Perfil","Profilo"};
    private static final String[] COMPANION_TABS={"Companions","Compagnons","Begleiter","Compañeros","Companheiros","Compagni"};
    private static final String[] AVATAR_NAMES={"Miko","Orbit","Pip","Astra","Zig","Lune","Fenn","Hoot","Buzz","Tula","Jelli","Vela","Nimbi","Soli","Mochi","Nixi","Prickly","Quill","Waddles","Halo","Emberling","Bambu","Inky"};
    private static final String[][] OUTFIT_NAMES={
        {"Moonlight","Star parade","Midnight harvest","Heart festival","Aurora dream"},
        {"Clair de lune","Parade stellaire","Récolte de minuit","Festival des cœurs","Rêve d’aurore"},
        {"Mondschein","Sternenparade","Mitternachtsernte","Herzfestival","Auroratraum"},
        {"Luz de luna","Desfile estelar","Cosecha nocturna","Festival de corazones","Sueño de aurora"},
        {"Luar","Desfile estelar","Colheita da meia-noite","Festival dos corações","Sonho de aurora"},
        {"Chiaro di luna","Parata stellare","Raccolto di mezzanotte","Festival dei cuori","Sogno d’aurora"}
    };
    private static final String[][] COPY={
        {"Star arrivals","Rencontres stellaires","Sternenbegegnungen","Encuentros estelares","Encontros estelares","Incontri stellari"},
        {"Signature outfits","Tenues signatures","Signatur-Outfits","Conjuntos exclusivos","Conjuntos de assinatura","Completi esclusivi"},
        {"Pool & exact odds","Contenu et probabilités","Inhalt und genaue Chancen","Contenido y probabilidades","Conteúdo e probabilidades","Contenuto e probabilità"},
        {"Badge slot 1","Emplacement de badge 1","Abzeichenplatz 1","Espacio de insignia 1","Espaço de distintivo 1","Spazio distintivo 1"},
        {"Edit nickname","Modifier le pseudo","Spitzname ändern","Cambiar apodo","Alterar alcunha","Modifica soprannome"},
        {"See last arrival","Revoir la dernière rencontre","Letzte Begegnung ansehen","Ver último encuentro","Ver último encontro","Rivedi l’ultimo incontro"},
        {"Moonlight","Clair de lune","Mondschein","Luz de luna","Luar","Chiaro di luna"}
    };
    private static final String[] SCENES={"Home scene","Pièce de la maison","Zimmeransicht","Vista de la casa","Vista da casa","Vista della casa"};
    private static final String[] MY_COMPANIONS={"My companions","Mes compagnons","Meine Begleiter","Mis compañeros","Os meus companheiros","I miei compagni"};
    private static final String[] ALL_AVATAR_NAMES={"Nova","Miko","Orbit","Pip","Astra","Zig","Lune","Fenn","Hoot","Buzz","Tula","Jelli","Vela","Nimbi","Soli","Mochi","Nixi","Prickly","Quill","Waddles","Halo","Emberling","Bambu","Inky"};
    private void baseline()throws Exception{
        JSONObject original=legacySave(),session=playState();
        check(original.getInt("coins")==51510,"Exact retained earned balance51510 before any upgrade or input");
        check(save().getInt("stars_1000",0)>0,"Baseline is the genuinely completed U1000 task-owned profile");
        check(original.has("home_layout_v113")&&original.has("player_nickname_v116")&&original.has("summon_last_request_v116_1"),"Baseline includes furnished home, custom profile and completed original summon receipt");
        write("baseline.json",new JSONObject().put("legacySave",original).put("session",session).put("processId",android.os.Process.myPid()).put("version",args.getString("expectedVersion","")).put("versionCode",Integer.parseInt(args.getString("expectedCode","0"))).toString(2));
        check(original.toString().equals(legacySave().toString())&&session.toString().equals(playState().toString()),"Read-only baseline does not alter any observed save or puzzle value");
    }
    private void prepare()throws Exception{
        JSONObject base=new JSONObject(read("baseline.json"));
        check(base.getInt("processId")!=android.os.Process.myPid(),"Upgrade validation runs after the independent installed-base baseline process");
        check(base.getJSONObject("legacySave").toString().equals(legacySave().toString()),"Installing and launching1.17 preserves legacy ownership, coins, progression, rooms, profile, badges and every observed cosmetic/summon receipt");
        check(base.getJSONObject("session").toString().equals(playState().toString()),"Upgrade preserves the complete original puzzle session");
        JSONObject progress=progression(),stock=inventory(),session=playState(),home=homeState(),receipt=receipts();int coins=save().getInt("coins",-1);
        check(coins==51510&&save().getInt("stars_1000",0)>0,"Retained earned balance and real U1000 progression survive upgrade");
        english();rootLoop();homeCamera();navigate(0,2);capture("collection-before-en");tap(PROFILES);capture("profile-before-en");
        tap("Edit nickname");setNickname("Luma QA 117");submitNickname();
        check("Luma QA 117".equals(save().getString("player_nickname_v116","")),"Actual visible nickname field saves via IME Done without a dialog button");
        profileTop();tap("Select flag");tap("FR");check("FR".equals(save().getString("player_flag_v116","")),"An actual flag object updates local identity");tap("LU");check("LU".equals(save().getString("player_flag_v116","")),"Original Luxembourg flag can be restored using the visible object");
        top();tap("Badge slot 1");capture("badges-en");JSONObject worn=badgeSlots();tapPrefix("First spark,");
        check("first_win".equals(save().getString("player_badge_v116_0","")),"Already-earned first badge remains equipable through its medal object");capture("profile-custom-en");
        navigate(0,2);tap(COPY[0][0]);tap(COPY[5][0]);capture("last-reveal-en");back();
        checkNoSpend(coins,receipt,progress,stock,session,home,"Upgrade object, gesture, camera, profile, flag, badge and existing-reveal route");
        settings();top();tap("Français");back();navigate(1,1);capture("delivery-home-fr");navigate(1,2);capture("delivery-collection-fr");tap(PROFILES);capture("delivery-profile-fr");
        write("checkpoint.json",new JSONObject().put("preferences",persistent()).put("economy",economy()).put("inventory",inventory()).put("playState",playState()).put("home",homeState()).put("preparePid",android.os.Process.myPid()).toString(2));
    }
    private void cold()throws Exception{
        JSONObject checkpoint=new JSONObject(read("checkpoint.json"));
        check(checkpoint.getInt("preparePid")!=android.os.Process.myPid(),"Cold qualification runs in a genuinely distinct process");
        check(checkpoint.getJSONObject("preferences").toString().equals(persistent().toString()),"Actual nickname, flag, badges, exact receipts, equipment, ownership and room selection survive cold start");
        check(checkpoint.getJSONObject("economy").toString().equals(economy().toString()),"Earned balance and all observed progression survive cold start");
        check(checkpoint.getJSONObject("inventory").toString().equals(inventory().toString())&&checkpoint.getJSONObject("playState").toString().equals(playState().toString()),"Unchanged stock and complete puzzle session survive cold start");
        check(checkpoint.getJSONObject("home").toString().equals(homeState().toString()),"All exact furnished layouts survive cold start");
        navigate(1,1);capture("cold-home-fr");navigate(1,2);tap(PROFILES);capture("cold-profile-fr");navigate(1,2);tap(COPY[0][1]);tap(COPY[5][1]);capture("cold-last-reveal-fr");
        check(checkpoint.getJSONObject("economy").toString().equals(economy().toString()),"Cold existing-receipt preview performs no spend or reward");
    }
    private void rootLoop()throws Exception{
        for(int index=0;index<4;index++){navigate(0,index);capture("root-"+new String[]{"adventure","home","collection","atelier"}[index]+"-en");}
        rootSwipe(true);check(currentRoot()==2,"Actual rightward swipe returns from Atelier to Collection");capture("root-gesture-collection-en");
        rootSwipe(false);check(currentRoot()==3,"Actual leftward swipe advances from Collection to Atelier");capture("root-gesture-atelier-en");
    }
    private void rootSwipe(boolean right)throws Exception{
        Rect app=appBounds();float y=app.top+app.height()*.94f,start=app.left+app.width()*(right?.24f:.76f),end=app.left+app.width()*(right?.76f:.24f);
        swipe(start,y,end,y,14);actions.put(new JSONObject().put("input","Actual touchscreen horizontal navigation gesture").put("direction",right?"right":"left").put("appBounds",app.toShortString()));SystemClock.sleep(450);
    }
    private Rect appBounds(){AccessibilityNodeInfo root=ui.getRootInActiveWindow();if(root==null)throw new AssertionError("No app root bounds");Rect box=new Rect();root.getBoundsInScreen(box);root.recycle();return box;}
    private void swipe(float x,float y,float endX,float endY,int steps){long down=SystemClock.uptimeMillis();inject(down,MotionEvent.ACTION_DOWN,x,y);for(int step=1;step<=steps;step++){SystemClock.sleep(16);inject(down,MotionEvent.ACTION_MOVE,x+(endX-x)*step/steps,y+(endY-y)*step/steps);}inject(down,MotionEvent.ACTION_UP,endX,endY);}
    private void homeCamera()throws Exception{
        navigate(0,1);tap("Living room");capture("home-detail-en");
        Rect scene=bounds("Home scene");check(scene!=null&&scene.width()>0&&scene.height()>0,"Actual enlarged home exposes its accessible scene");
        Rect aquarium=bounds("Moon reef aquarium");check(aquarium!=null&&scene.contains(aquarium.centerX(),aquarium.centerY()),"Retained aquarium exposes a native in-scene target");
        tap("Decorate");tap("Moon reef aquarium");check(waitHas(new String[]{"Cancel"},true,4000),"Actual furniture object opens its retained placement preview");capture("home-aquarium-selected-en");tap("Cancel");tap("Done");
        panHome();capture("home-panned-en");
    }
    private void submitNickname()throws Exception{
        boolean submitted=false;for(AccessibilityNodeInfo node:nodes()){
            if(!submitted&&node.isVisibleToUser()&&node.isEditable())submitted=node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.getId());node.recycle();
        }
        if(!submitted){long now=SystemClock.uptimeMillis();ui.injectInputEvent(new KeyEvent(now,now,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0),true);ui.injectInputEvent(new KeyEvent(now,SystemClock.uptimeMillis(),KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ENTER,0),true);}
        actions.put(new JSONObject().put("input",submitted?"SDK accessibility IME enter":"Actual KEYCODE_ENTER"));SystemClock.sleep(500);
    }
    private void browse()throws Exception{browseLanguages(true);}
    private void banners()throws Exception{browseLanguages(false);}
    private void browseLanguages(boolean includeRoots)throws Exception{
        JSONObject progress=progression(),stock=inventory(),session=playState(),receipt=receipts(),home=homeState();int coins=save().getInt("coins",-1);
        for(int language=0;language<LANG_CODES.length;language++){
            String code=LANG_CODES[language];settings();top();tap(LANG_NAMES[language]);back();
            if(includeRoots){
                for(int index=0;index<4;index++){navigate(language,index);capture(new String[]{"adventure","home","collection","atelier"}[index]+"-"+code);}
                navigate(language,2);tap(PROFILES);check(has(new String[]{COPY[4][language]},true),"Localized nickname object is accessible in "+code);capture("profile-"+code);tap(COPY[3][language]);capture("badges-"+code);
                navigate(language,2);tap(MY_COMPANIONS[language]);catalogCharacters(language);navigate(language,2);
            }else navigate(language,2);
            tap(COPY[0][language]);tap(COMPANION_TABS[language]);capture("companions-banner-"+code);tap(COPY[2][language]);
            check(hasPrefix("Miko,"),"Explicit companion pool starts at Miko in "+code);capture("avatar-pool-top-"+code);poolEnd(AVATAR_NAMES,"Inky",code,"avatars");capture("avatar-pool-bottom-"+code);back();
            tap(COPY[1][language]);capture("signature-banner-"+code);tap(COPY[2][language]);
            poolEnd(OUTFIT_NAMES[language],OUTFIT_NAMES[language][4],code,"outfits");capture("outfit-pool-bottom-"+code);
            for(int outfit=0;outfit<5;outfit++){tapPrefix(OUTFIT_NAMES[language][outfit]+",");capture("outfit-"+outfit+"-preview-"+code);back();}
            back();tap(COPY[5][language]);capture("reveal-"+code);back();
        }
        checkNoSpend(coins,receipt,progress,stock,session,home,"Six-language real native art browsing");
        settings();top();tap("Français");back();navigate(1,1);capture("delivery-house-fr");navigate(1,2);capture("delivery-collection-fr");tap(PROFILES);capture("delivery-profile-fr");
    }
    private void catalogCharacters(int language)throws Exception{
        Set<String> seen=new java.util.TreeSet<>();String code=LANG_CODES[language];
        for(int page=0;page<6;page++){
            top();poolFrame(ALL_AVATAR_NAMES,seen);capture("catalog-page-"+page+"-top-"+code);
            scroll(true);poolFrame(ALL_AVATAR_NAMES,seen);capture("catalog-page-"+page+"-bottom-"+code);
            if(page<5)tap("›");
        }
        check(seen.size()==24&&seen.containsAll(Arrays.asList(ALL_AVATAR_NAMES)),"All24 actual companion catalogue objects observed across six pages in "+code);
        write("catalog-coverage-"+code+".json",new JSONObject().put("expectedCount",24).put("visibleNamesObserved",new JSONArray(new ArrayList<>(seen))).toString(2));
    }
    private void checkNoSpend(int coins,JSONObject receipt,JSONObject progress,JSONObject stock,JSONObject session,JSONObject home,String scope)throws Exception{
        check(coins==save().getInt("coins",-1)&&receipt.toString().equals(receipts().toString()),scope+" creates no purchase, draw, compensation or coin change");
        check(progress.toString().equals(progression().toString())&&stock.toString().equals(inventory().toString())&&session.toString().equals(playState().toString()),scope+" preserves progression, stock and complete puzzle session");
        check(home.toString().equals(homeState().toString()),scope+" preserves all furniture ownership and exact room layouts");
    }
    private void decodeAssets()throws Exception{
        String[] filenames={"companions-0.png","companions-1.png","companions-2.png","companions-3.png","hats.png","dresses.png","shoes.png","gloves.png","furniture-0.png","furniture-1.png","furniture-2.png","furniture-3.png","furniture-4.png","furniture-5.png","furniture-6.png","actions-0.png","actions-1.png"};
        JSONArray sheets=new JSONArray();int spriteCount=0;
        for(String filename:filenames){
            String asset="art/v117/"+filename;byte[] bytes;try(InputStream input=getTargetContext().getAssets().open(asset);ByteArrayOutputStream buffer=new ByteArrayOutputStream()){byte[] block=new byte[8192];int n;while((n=input.read(block))!=-1)buffer.write(block,0,n);bytes=buffer.toByteArray();}
            BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;options.inPreferredConfig=Bitmap.Config.ARGB_8888;Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
            check(bitmap!=null&&bitmap.hasAlpha(),"Native decoder loads real alpha pixels for "+filename);JSONArray cells=new JSONArray();int[] row=new int[bitmap.getWidth()];
            try{
                for(int cell=0;cell<6;cell++){
                    int column=cell%3,rr=cell/3,left=column*bitmap.getWidth()/3,right=(column+1)*bitmap.getWidth()/3,top=rr*bitmap.getHeight()/2,bottom=(rr+1)*bitmap.getHeight()/2;
                    int l=right,t=bottom,r=left-1,b=top-1,transparent=0,painted=0;
                    for(int y=top;y<bottom;y++){bitmap.getPixels(row,0,right-left,left,y,right-left,1);for(int x=0;x<right-left;x++){if((row[x]>>>24)<=8){transparent++;continue;}painted++;l=Math.min(l,left+x);r=Math.max(r,left+x);t=Math.min(t,y);b=y;}}
                    check(painted>1024&&transparent>256&&r-l>=32&&b-t>=32,"Original sprite cell has visible detail and transparent separation: "+filename+" #"+cell);
                    cells.put(new JSONObject().put("index",cell).put("alphaBounds",new JSONArray(new int[]{l,t,r+1,b+1})).put("paintedPixels",painted).put("transparentPixels",transparent));spriteCount++;
                }
                sheets.put(new JSONObject().put("path",asset).put("sha256",hex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes))).put("width",bitmap.getWidth()).put("height",bitmap.getHeight()).put("cells",cells));
            }finally{bitmap.recycle();}
        }
        check(spriteCount==102,"Native SDK decoded24 companions,24 garments,42 furniture sprites and12 navigation objects");
        JSONArray backgrounds=new JSONArray();
        for(String name:new String[]{"adventure","collection","atelier"}){
            String asset="art/v117/world-"+name+".png";byte[] bytes;try(InputStream input=getTargetContext().getAssets().open(asset);ByteArrayOutputStream buffer=new ByteArrayOutputStream()){byte[] block=new byte[8192];int n;while((n=input.read(block))!=-1)buffer.write(block,0,n);bytes=buffer.toByteArray();}
            Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length);check(bitmap!=null&&bitmap.getWidth()>=512&&bitmap.getHeight()>=768,"Native decoder loads the original "+name+" scene painting");
            backgrounds.put(new JSONObject().put("path",asset).put("sha256",hex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes))).put("width",bitmap.getWidth()).put("height",bitmap.getHeight()));bitmap.recycle();
        }
        write("assets.json",new JSONObject().put("passed",true).put("spriteCount",spriteCount).put("sheets",sheets).put("backgrounds",backgrounds).put("scope","Native packaged bitmap decode and alpha bounds; actual app screens separately captured").toString(2));
    }
    private String hex(byte[] bytes){StringBuilder value=new StringBuilder();for(byte part:bytes)value.append(String.format(java.util.Locale.ROOT,"%02x",part&255));return value.toString();}

    private boolean hasPrefix(String prefix){boolean result=false;for(AccessibilityNodeInfo node:nodes()){Rect box=new Rect();node.getBoundsInScreen(box);if(node.isVisibleToUser()&&node.isClickable()&&box.width()>0&&box.height()>0&&name(node).startsWith(prefix))result=true;node.recycle();}return result;}
    private String poolFrame(String[] names,Set<String> seen){
        List<String> frame=new ArrayList<>();for(AccessibilityNodeInfo node:nodes()){
            Rect box=new Rect();node.getBoundsInScreen(box);String label=name(node);
            if(node.isVisibleToUser()&&node.isClickable()&&box.width()>0&&box.height()>0)for(String expected:names)if(label.startsWith(expected+",")){seen.add(expected);frame.add(label+":"+box.toShortString());break;}
            node.recycle();
        }Collections.sort(frame);return frame.toString();
    }
    private void poolEnd(String[] expected,String last,String language,String kind)throws Exception{
        Set<String> seen=new java.util.TreeSet<>();String previous=poolFrame(expected,seen);boolean stableEnd=false;int scrolls=0;
        for(;scrolls<50;scrolls++){
            boolean moved=scroll(true);String after=poolFrame(expected,seen);
            if(!moved||after.equals(previous)){stableEnd=true;break;}previous=after;
        }
        check(stableEnd,"Native "+kind+" pool reaches a stable scroll end in "+language);
        check(hasPrefix(last+","),"Native "+kind+" final entry "+last+" is visible at the actual bottom in "+language);
        check(seen.size()==expected.length&&seen.containsAll(Arrays.asList(expected)),"All "+expected.length+" native "+kind+" entries were observed during scrolling in "+language);
        write(kind+"-coverage-"+language+".json",new JSONObject().put("expectedCount",expected.length).put("visibleNamesObserved",new JSONArray(new ArrayList<>(seen))).put("finalVisibleEntry",last).put("stableEnd",stableEnd).put("scrollActions",scrolls+1).toString(2));
    }
    private void setNickname(String value)throws Exception{
        boolean changed=false;for(AccessibilityNodeInfo node:nodes()){
            if(!changed&&node.isVisibleToUser()&&node.isEditable()){
                Bundle text=new Bundle();text.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,value);changed=node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,text);
            }node.recycle();
        }
        check(changed,"Framework ACTION_SET_TEXT edits only the visible native nickname field");SystemClock.sleep(150);
    }
    private void profileTop(){top();}
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
            if(key.contains("seed"))continue;
            if(key.startsWith("player_")||key.startsWith("badge_")||key.startsWith("summon_")||key.equals("coins")||key.equals("language")||key.equals("unlocked")||key.contains("stars_")||key.equals("character")||key.equals("skin")||key.startsWith("home_")||key.startsWith("owned_")||key.startsWith("equipped")||key.startsWith("avatar_")||key.startsWith("cosmetic_")||key.startsWith("booster_")||key.startsWith("power_")||key.startsWith("pending_booster")||key.startsWith("relay_unlocked")||key.startsWith("journey_unlocked"))values.put(key,entry.getValue());
        }return json(values);
    }

    private JSONObject economy()throws Exception{
        TreeMap<String,Object> values=new TreeMap<>();for(Map.Entry<String,?> entry:save().getAll().entrySet()){String key=entry.getKey();if(key.equals("coins")||key.equals("unlocked")||key.contains("stars_")||key.startsWith("relay_unlocked")||key.startsWith("journey_unlocked"))values.put(key,entry.getValue());}return json(values);
    }
    private JSONObject json(Map<String,Object> values)throws Exception{JSONObject result=new JSONObject();for(Map.Entry<String,Object> item:values.entrySet()){Object value=item.getValue();if(value instanceof Set){List<String> list=new ArrayList<>();for(Object x:(Set<?>)value)list.add(String.valueOf(x));Collections.sort(list);value=new JSONArray(list);}result.put(item.getKey(),value);}return result;}
    private void english()throws Exception{settings();top();tap("English");back();}
    private void settings()throws Exception{
        for(int attempts=0;attempts<16;attempts++){
            top();if(has(SETTINGS,true)){tap(SETTINGS);return;}
            int actual=currentRoot();
            if(actual>0)tap(rootLabels(actual-1));else back();
        }
        throw new AssertionError("No visible Settings object: "+labels());
    }
    private String[] rootLabels(int index){String[] labels=new String[NAV.length];for(int i=0;i<NAV.length;i++)labels[i]=NAV[i][index];return labels;}
    private int currentRoot(){
        if(has(SETTINGS,true))return 0;
        if(has(new String[]{"Home scene","Pièce de la maison","Zimmeransicht","Vista de la casa","Vista da casa","Vista della casa"},false))return 1;
        if(has(PROFILES,true))return 2;
        if(has(new String[]{"Walls","Murs","Wände","Paredes","Pareti"},true)&&has(new String[]{"Floor","Sol","Boden","Suelo","Chão","Pavimento"},true))return 3;
        return -1;
    }
    private void navigate(int language,int index)throws Exception{
        for(int attempts=0;attempts<14;attempts++){
            top();int actual=currentRoot();
            if(actual==index){check(true,"Actual native object destination: "+NAV[language][index]);return;}
            if(actual>=0){int next=actual+(actual<index?1:-1);tap(NAV[language][next]);}
            else back();
        }
        throw new AssertionError("Cannot reach object destination "+index+"; "+labels());
    }
    private String name(AccessibilityNodeInfo node){CharSequence description=node.getContentDescription();if(description!=null&&!description.toString().isEmpty())return description.toString();return node.getText()==null?"":node.getText().toString();}
    private boolean matches(String value,String[] requested){for(String label:requested)if(value.equalsIgnoreCase(label)||value.equalsIgnoreCase(label+"  →")||value.equalsIgnoreCase(label+" →"))return true;return false;}
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
        actions.put(new JSONObject().put("label",observed).put("bounds",bounds.toShortString()).put("input","UiAutomation touchscreen"));SystemClock.sleep(400);
    }
    private void inject(long down,int action,float x,float y){MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);boolean sent=ui.injectInputEvent(event,true);event.recycle();if(!sent)throw new AssertionError("Touch event refused");}
    private boolean scroll(boolean forward){AccessibilityNodeInfo root=ui.getRootInActiveWindow();if(root==null)return false;boolean sent=root.performAction(forward?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);if(!sent){List<AccessibilityNodeInfo> all=nodes();for(AccessibilityNodeInfo node:all){if(!sent&&node.isScrollable())sent=node.performAction(forward?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);node.recycle();}}root.recycle();SystemClock.sleep(130);return sent;}
    private void top(){String before=labels();for(int i=0;i<12;i++){if(!scroll(false))break;String after=labels();if(after.equals(before))break;before=after;}}
    private void back(){ui.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);SystemClock.sleep(400);}
    private void capture(String id)throws Exception{
        for(int i=0;i<captures.length();i++)if(id.equals(captures.getString(i)))throw new AssertionError("Duplicate screenshot identifier in one native phase: "+id);
        // Public browsing screenshots settle beyond the game's transient notice lifetime.
        SystemClock.sleep(650);
        Bitmap bitmap=ui.takeScreenshot();if(bitmap==null)throw new IllegalStateException("No native screenshot");try(OutputStream file=new FileOutputStream(new File(output,id+".png"))){if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,file))throw new IllegalStateException("PNG encoding failed");}finally{bitmap.recycle();}
        JSONArray snapshot=new JSONArray();for(AccessibilityNodeInfo node:nodes()){Rect box=new Rect();node.getBoundsInScreen(box);snapshot.put(new JSONObject().put("label",name(node)).put("bounds",new JSONArray(new int[]{box.left,box.top,box.right,box.bottom})).put("clickable",node.isClickable()).put("selected",node.isSelected()).put("visible",node.isVisibleToUser()));node.recycle();}write(id+".json",snapshot.toString(2));captures.put(id);
    }
    private void check(boolean passed,String message)throws Exception{count++;checks.put(new JSONObject().put("passed",passed).put("message",message));if(!passed)throw new AssertionError(message);Bundle update=new Bundle();update.putString("stream","PASS "+message+"\n");sendStatus(0,update);}
    private String read(String name)throws Exception{try(InputStream in=new FileInputStream(new File(output,name));ByteArrayOutputStream bytes=new ByteArrayOutputStream()){byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)bytes.write(buffer,0,n);return bytes.toString("UTF-8");}}
    private void write(String name,String value)throws Exception{try(OutputStream file=new FileOutputStream(new File(output,name))){file.write(value.getBytes(StandardCharsets.UTF_8));}}
}
