package com.androidhit.lumadrift.commerce;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.androidhit.lumadrift.MainActivity;
import com.androidhit.lumadrift.identity.AccountActivity;
import com.androidhit.lumadrift.wallet.ProductionWalletClient;
import com.androidhit.lumadrift.data.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/** Lab simulation and production account wallet remain separate surfaces and balances. */
// Framework switches intentionally match the native Theme.Material used on API 26+.
@android.annotation.SuppressLint("UseSwitchCompatOrMaterialCode")
public final class CommerceActivity extends Activity {
    public static final String EXTRA_CONTINUATION_REQUEST="com.androidhit.lumadrift.extra.CONTINUATION_REQUEST";
    public static final String EXTRA_CONTINUATION_MOVES="com.androidhit.lumadrift.extra.CONTINUATION_MOVES";
    public static final String EXTRA_POWER_ID="com.androidhit.lumadrift.extra.POWER_ID";
    private static final String CONTINUATION_PREFS="luma_ad_continuations_v1";
    private static final Object CONTINUATION_LOCK=new Object();
    private static final int BG=0xff11142b,CARD=0xff232744,WHITE=0xfffaf7ff,MUTED=0xffb9bfd8,MINT=0xff7ee8d1,GOLD=0xffffe29a;
    private GameStore store;private PlayJournal journal;private SharedPreferences simulations;
    private AdsController ads;private PlayGoldBilling billing;
    private LinearLayout content,catalog,paidCatalog;private TextView wallet,adStatus,billingStatus,pending,serverBalance,walletStatus;
    private ProductionWalletClient server;private ProductionWalletClient.Wallet currentWallet;
    private boolean walletBusy,spendBusy;private String observedUid="";private final Set<String> verifying=new HashSet<>();
    private Button watch,finishPending,replay,returnToRun;private Switch optIn,metrics;private Spinner ages;
    private boolean updating,alive,returning,powerRewarded;
    private String continuationRequest="";
    private int continuationMoves;
    private String powerId="";

    /** Only the game creates a request, after durably saving the exhausted run. */
    public static Intent continuationIntent(Context context,String requestId){
        if(!validRequest(requestId))throw new IllegalArgumentException("Valid continuation request required");
        return new Intent(context,CommerceActivity.class).putExtra(EXTRA_CONTINUATION_REQUEST,requestId);
    }
    public static Intent powerIntent(Context context,String powerId){
        if(Catalog.booster(powerId)==null)throw new IllegalArgumentException("Known power required");
        return new Intent(context,CommerceActivity.class).putExtra(EXTRA_POWER_ID,powerId);
    }
    public static boolean hasContinuationReward(Context context,String requestId){
        if(!validRequest(requestId))return false;
        synchronized(CONTINUATION_LOCK){
            try{
                SharedPreferences receipts=continuationPrefs(context);String key=receiptKey(requestId),showId=receipts.getString(key,"");
                // A failed Android commit may already have changed memory. Only
                // expose an existing earned receipt after its retry reaches disk.
                return !showId.isEmpty()&&receipts.edit().putString(key,showId).commit();
            }
            catch(ClassCastException invalidReceipt){return false;}
        }
    }
    /** Call only AFTER the run atomically saves its extension and lastAppliedRequestId. */
    public static boolean acknowledgeContinuationReward(Context context,String requestId){
        if(!validRequest(requestId))return false;
        synchronized(CONTINUATION_LOCK){return continuationPrefs(context).edit().remove(receiptKey(requestId)).commit();}
    }
    private static boolean validRequest(String requestId){return requestId!=null&&requestId.matches("[a-zA-Z0-9_:-]{1,160}");}
    private static SharedPreferences continuationPrefs(Context context){return context.getApplicationContext().getSharedPreferences(CONTINUATION_PREFS,Context.MODE_PRIVATE);}
    private static String receiptKey(String requestId){
        try{
            byte[] hash=MessageDigest.getInstance("SHA-256").digest(requestId.getBytes(StandardCharsets.UTF_8));StringBuilder result=new StringBuilder("earned_");
            for(byte part:hash)result.append(Character.forDigit((part&255)>>>4,16)).append(Character.forDigit(part&15,16));return result.toString();
        }catch(NoSuchAlgorithmException unavailable){throw new IllegalStateException("SHA-256 unavailable",unavailable);}
    }
    private boolean saveContinuationReward(String showId){
        if(!validRequest(continuationRequest)||showId==null||!showId.matches("ad:[a-f0-9-]{36}"))return false;
        synchronized(CONTINUATION_LOCK){
            SharedPreferences receipts=continuationPrefs(this);String key=receiptKey(continuationRequest);
            String previous=receipts.getString(key,"");
            // Flush an existing receipt too: Android updates preference memory
            // before commit reports disk failure. A retry must really persist it.
            return receipts.edit().putString(key,previous.isEmpty()?showId:previous).commit();
        }
    }
    private boolean isContinuation(){return !continuationRequest.isEmpty();}
    private boolean isPower(){return !powerId.isEmpty();}
    private boolean isAdPurpose(){return isContinuation()||isPower();}
    private boolean isRewardComplete(){return isContinuation()?hasContinuationReward(this,continuationRequest):isPower()&&powerRewarded;}
    private String powerName(){Catalog.BoosterItem item=Catalog.booster(powerId);return item==null?"":t(item.nameKey);}
    private boolean powerAvailable(){return isPower()&&Catalog.booster(powerId)!=null&&store.boosterUnlocked(powerId)&&store.boosterStock(powerId)<store.boosterStockLimit();}
    private boolean allowPurposeRequest(){
        if(isRewardComplete())return false;
        if(isPower()&&!powerAvailable()){adStatus.setText(t("ads_power_unavailable"));refresh();return false;}
        return true;
    }
    private void returnToGame(){
        if(!alive||returning||!isAdPurpose()||!isRewardComplete()||ads.isAdShowing())return;
        returning=true;
        // The intent is navigation only. The game validates its own saved request
        // against the receipt, then commits the budget before acknowledging it.
        if(isContinuation())startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP));
        finish();
    }
    private String t(String key,Object...args){return CommerceConfig.MODE_SANDBOX?Strings.get(store.resolvedLanguage(),key,args):WalletStrings.get(store.resolvedLanguage(),key,args);}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);alive=true;
        if(getIntent().hasExtra(EXTRA_CONTINUATION_REQUEST)){
            String request=getIntent().getStringExtra(EXTRA_CONTINUATION_REQUEST);
            int moves=getIntent().getIntExtra(EXTRA_CONTINUATION_MOVES,0);
            if(!validRequest(request)||moves<1||moves>52){finish();return;}continuationRequest=request;continuationMoves=moves;
        }
        if(getIntent().hasExtra(EXTRA_POWER_ID)){
            String requestedPower=getIntent().getStringExtra(EXTRA_POWER_ID);
            if(isContinuation()||Catalog.booster(requestedPower)==null){finish();return;}powerId=requestedPower;
            powerRewarded=state!=null&&state.getBoolean("power_rewarded",false);
        }
        store=new GameStore(this);journal=new PlayJournal(this);simulations=getSharedPreferences("luma_lab_purchases_v1",0);
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        if(!CommerceConfig.MODE_SANDBOX)server=new ProductionWalletClient(this);
        ads=new AdsController(this,new AdsController.Listener(){
            public void onStatus(String key){runOnUiThread(()->{if(alive){adStatus.setText(t(key,powerName()));refresh();if(isAdPurpose()&&(key.equals("ads_closed")||key.equals("ads_continue_earned")||key.equals("ads_power_earned")))returnToGame();}});}
            public void onReward(String id,int amount){
                if(!alive)throw new IllegalStateException("Reward owner is closed");
                if(isContinuation()){
                    if(!saveContinuationReward(id))throw new IllegalStateException("Could not persist continuation reward");
                    refresh();return;
                }
                if(isPower()){
                    if(!powerAvailable())throw new AdsController.RewardUnavailableException("ads_power_unavailable");
                    int got=store.grantBoosterOnce(powerId,id);
                    if(got!=1&&!store.hasBoosterGranted(powerId,id))throw new AdsController.RewardUnavailableException("ads_power_unavailable");
                    powerRewarded=true;refresh();return;
                }
                int got=store.grantGold(id,amount);refresh();toast(t("gold_reward_received",got));
            }
            public void onEvent(String event,Map<String,String> fields){journal.record(event,fields);}
        },isContinuation()?AdsController.RewardPlacement.CONTINUATION:isPower()?AdsController.RewardPlacement.POWER:AdsController.RewardPlacement.GOLD,powerId);
        billing=new PlayGoldBilling(this,new PlayGoldBilling.Listener(){
            public void onProducts(List<PlayGoldBilling.GoldProduct> products){runOnUiThread(()->showCatalog(products));}
            public void onStatus(String key){runOnUiThread(()->{if(alive&&billingStatus!=null)billingStatus.setText(t(key));});}
            public void onPurchasePending(String productId){toast(t(CommerceConfig.MODE_SANDBOX?"billing_pending":"wallet_purchase_pending"));}
            public void onPurchaseNeedsVerification(String productId,String token){verifyPurchase(productId,token);}
        });
        build();ads.setLocalMetricsEnabled(journal.isEnabled());
        if(!isRewardComplete())ads.onAppLaunch();
        if(!isAdPurpose()){billing.connectAndQuery();if(server!=null)refreshServerWallet();}refresh();
    }
    private void build(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{BG,0xff252342,BG}));
        scroll.setOnApplyWindowInsetsListener((view,insets)->{
            if(android.os.Build.VERSION.SDK_INT>=30){android.graphics.Insets safe=insets.getInsets(android.view.WindowInsets.Type.systemBars()|android.view.WindowInsets.Type.displayCutout());view.setPadding(safe.left,safe.top,safe.right,safe.bottom);}
            else view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;});
        FrameLayout center=new FrameLayout(this);content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(20),dp(22),dp(20),dp(36));
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(Math.min(getResources().getDisplayMetrics().widthPixels,dp(560)),-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);center.addView(content,cp);scroll.addView(center);setContentView(scroll);
        Button back=button("‹  "+t("back"),()->finish());content.addView(back,new LinearLayout.LayoutParams(-2,dp(48)));
        ImageView nova=new ImageView(this);nova.setImageResource(com.androidhit.lumadrift.R.drawable.ic_launcher);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(80),dp(80));ip.topMargin=dp(18);content.addView(nova,ip);
        heading(content,CommerceConfig.MODE_SANDBOX?"LUMA DRIFT LAB":"LUMA DRIFT",30,GOLD);
        if(CommerceConfig.MODE_SANDBOX){paragraph(content,t("sandbox_label"),MINT);paragraph(content,t("sandbox_note"),MUTED);wallet=heading(content,t("gold_balance",store.coins()),27,WHITE);}
        else{
            heading(content,t("wallet_title"),25,GOLD);serverBalance=heading(content,t("wallet_balance_unknown"),23,WHITE);
            wallet=paragraph(content,t("wallet_local",store.coins()),MINT);paragraph(content,t("wallet_note"),MUTED);
            walletStatus=paragraph(content,t("wallet_loading"),MUTED);
            content.addView(button(t("wallet_sign_in"),()->startActivity(new Intent(this,AccountActivity.class))));
            content.addView(button(t("wallet_refresh"),()->{refreshServerWallet();billing.connectAndQuery();}));
        }
        LinearLayout adCard=card();heading(adCard,t("ads_privacy_title"),23,WHITE);paragraph(adCard,t(CommerceConfig.MODE_SANDBOX?"ads_privacy_note":"wallet_ads_note"),MUTED);if(!CommerceConfig.MODE_SANDBOX&&CommerceConfig.FORCE_TEST_ADS)paragraph(adCard,t("wallet_test_ads"),GOLD);
        heading(adCard,t("age_label"),15,MINT);ages=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{t("age_unknown"),t("age_minor"),t("age_adult")});ages.setAdapter(adapter);ages.setSelection(ads.ageStatus().ordinal());adCard.addView(ages,new LinearLayout.LayoutParams(-1,dp(52)));
        ages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int position,long id){if(!updating){ads.setAgeStatus(AdsController.AgeStatus.values()[position]);refresh();}}});
        optIn=toggle(adCard,t("ads_opt_in"),ads.isExplicitOptIn());optIn.setOnCheckedChangeListener((v,on)->{if(!updating){ads.setExplicitOptIn(on);refresh();}});
        adCard.addView(button(t("privacy_choices"),()->ads.showPrivacyOptions()));
        adStatus=paragraph(adCard,t("ads_opt_in_required"),MINT);adStatus.setContentDescription(t("ad_status")+": "+adStatus.getText());
        Object offer=isContinuation()?Integer.valueOf(continuationMoves):powerName();
        adCard.addView(button(t("ads_load"),()->{if(allowPurposeRequest())ads.loadRewarded();}));watch=button(t(isContinuation()?"ads_continue_watch":isPower()?"ads_power_watch":"ads_watch",offer),()->{if(allowPurposeRequest())ads.showRewarded();});adCard.addView(watch);paragraph(adCard,t(isContinuation()?"ads_continue_note":isPower()?"ads_power_note":"ads_reward_note",offer),MUTED);
        if(isAdPurpose()){
            returnToRun=button(t(isContinuation()?"ads_continue_return":"ads_power_return"),()->returnToGame());adCard.addView(returnToRun);
            if(isRewardComplete())adStatus.setText(t(isContinuation()?"ads_continue_earned":"ads_power_earned",powerName()));
            return;
        }
        if(CommerceConfig.MODE_SANDBOX){LinearLayout packCard=card();heading(packCard,t("gold_packs"),23,GOLD);paragraph(packCard,t("simulation_note"),MUTED);
        for(String product:CommerceConfig.GOLD_PRODUCT_IDS)packCard.addView(button(t("simulate_purchase",CommerceConfig.goldAmount(product)),()->simulate(product)));
        pending=paragraph(packCard,"",MINT);finishPending=button(t("complete_pending"),()->completePending());packCard.addView(finishPending);replay=button(t("repeat_test"),()->repeatLast());packCard.addView(replay);}
        LinearLayout playCard=card();heading(playCard,t("play_catalog"),20,WHITE);billingStatus=paragraph(playCard,t("billing_connecting"),MUTED);catalog=new LinearLayout(this);catalog.setOrientation(LinearLayout.VERTICAL);playCard.addView(catalog);if(CommerceConfig.MODE_SANDBOX)paragraph(playCard,t("billing_test_only"),MUTED);
        else{paidCatalog=new LinearLayout(this);paidCatalog.setOrientation(LinearLayout.VERTICAL);content.addView(paidCatalog);showPaidCatalog();return;}
        LinearLayout observation=card();heading(observation,t("tracking_local"),21,WHITE);metrics=toggle(observation,t("local_journal"),journal.isEnabled());paragraph(observation,t("tracking_note"),MUTED);
        metrics.setOnCheckedChangeListener((v,on)->{journal.setEnabled(on);ads.setLocalMetricsEnabled(on);if(on)event("session_start","version","1.3.0-sandbox","surface","commerce_lab");});
        observation.addView(button(t("open_journal"),()->{startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("open_journal",true));finish();}));
        LinearLayout curve=card();heading(curve,t("price_curve"),21,WHITE);paragraph(curve,t("curve_note"),MUTED);
        for(Catalog.BoosterItem item:Catalog.BOOSTERS)paragraph(curve,t(item.nameKey)+" ×"+item.amount+"   "+store.boosterPrice(item.id)+" ◈",WHITE);
    }
    private void simulate(String product){
        if(!CommerceConfig.MODE_SANDBOX){toast(t("production_blocked"));return;}
        if(!simulations.getString("pending_tx","").isEmpty()){toast(t("pending_note"));return;}
        int amount=CommerceConfig.goldAmount(product);String tx="sandbox:"+UUID.randomUUID();
        new AlertDialog.Builder(this).setTitle(t("simulate_title")).setMessage(t("gold_pack",amount)+"\n\n"+t("simulation_note"))
          .setNegativeButton(t("cancel"),(d,w)->event("test_purchase_cancelled","product",product))
          .setNeutralButton(t("simulate_pending"),(d,w)->{if(savePending(tx,product,amount)){event("test_purchase_pending","product",product);toast(t("pending_note"));refresh();}})
          .setPositiveButton(t("simulate_complete"),(d,w)->{if(savePending(tx,product,amount))completePending();}).show();
    }
    private boolean savePending(String tx,String product,int amount){boolean saved=simulations.edit().putString("pending_tx",tx).putString("pending_product",product).putInt("pending_amount",amount).commit();if(!saved)toast(t("commerce_save_error"));return saved;}
    private void completePending(){
        String tx=simulations.getString("pending_tx","");String product=simulations.getString("pending_product","");int amount=simulations.getInt("pending_amount",0);
        if(tx.isEmpty()){toast(t("no_pending"));return;}if(!CommerceConfig.MODE_SANDBOX||!tx.startsWith("sandbox:")||CommerceConfig.goldAmount(product)!=amount||amount<=0){toast(t("production_blocked"));return;}
        try{int got=store.grantGold(tx,amount);if(got==0&&!store.isGoldGranted(tx)){toast(t("gold_wallet_full"));return;}boolean saved=simulations.edit().putString("last_tx",tx).putInt("last_amount",amount).remove("pending_tx").remove("pending_amount").remove("pending_product").commit();
            if(!saved){toast(t("commerce_save_error"));return;}event("test_purchase_complete","product",product,"gold",""+got);refresh();toast(t(got>0?"test_gold_added":"already_credited",got));
        }catch(IllegalStateException failure){toast(t("commerce_save_error"));}
    }
    private void repeatLast(){String tx=simulations.getString("last_tx","");if(tx.isEmpty())return;try{int got=store.grantGold(tx,simulations.getInt("last_amount",0));refresh();toast(t(got==0?"already_credited":"test_gold_added",got));}catch(IllegalStateException failed){toast(t("commerce_save_error"));}}
    private void refresh(){if(wallet==null)return;wallet.setText(t(CommerceConfig.MODE_SANDBOX?"gold_balance":"wallet_local",store.coins()));updating=true;optIn.setChecked(ads.isExplicitOptIn());ages.setSelection(ads.ageStatus().ordinal());updating=false;boolean earned=isRewardComplete();watch.setEnabled(ads.isRewardedReady()&&!earned&&(!isPower()||powerAvailable()));if(returnToRun!=null)returnToRun.setVisibility(earned&&!ads.isAdShowing()?View.VISIBLE:View.GONE);if(pending!=null){boolean waiting=!simulations.getString("pending_tx","").isEmpty();pending.setText(waiting?t("pending_note"):t("no_pending"));finishPending.setVisibility(waiting?View.VISIBLE:View.GONE);replay.setVisibility(simulations.getString("last_tx","").isEmpty()?View.GONE:View.VISIBLE);}adStatus.setContentDescription(t("ad_status")+": "+adStatus.getText());}
    private void showCatalog(List<PlayGoldBilling.GoldProduct> products){
        if(!alive||catalog==null)return;catalog.removeAllViews();
        for(PlayGoldBilling.GoldProduct p:products){Button buy=button(t("gold_pack",p.gold)+" · "+p.localizedPrice,()->billing.launchPurchase(p.id));
            if(!CommerceConfig.MODE_SANDBOX)buy.setEnabled(billing.isRealPurchaseEnabled()&&currentWallet!=null&&!spendBusy&&!walletBusy);catalog.addView(buy);}
    }
    private void refreshServerWallet(){
        if(server==null||walletBusy||!alive)return;walletBusy=true;walletStatus.setText(t("wallet_loading"));showCatalog(billing.products());
        server.refresh(new ProductionWalletClient.Callback(){
            public void onSuccess(ProductionWalletClient.Wallet result){walletBusy=false;acceptWallet(result);walletStatus.setText(t(result.balance<0?"wallet_refund_debt":"wallet_ready"));}
            public void onError(String key){walletBusy=false;currentWallet=null;serverBalance.setText(t("wallet_balance_unknown"));walletStatus.setText(t(key));showPaidCatalog();showCatalog(billing.products());}
        });
    }
    private void verifyPurchase(String productId,String token){
        if(CommerceConfig.MODE_SANDBOX){toast(t("billing_verification_required"));return;}
        if(server==null||!verifying.add(token))return;
        if(walletStatus!=null)walletStatus.setText(t("wallet_verifying"));
        server.verify(productId,token,new ProductionWalletClient.Callback(){
            public void onSuccess(ProductionWalletClient.Wallet result){verifying.remove(token);acceptWallet(result);walletStatus.setText(t("PENDING".equals(result.purchaseStatus)?"wallet_purchase_pending":result.balance<0?"wallet_refund_debt":"wallet_ready"));}
            public void onError(String key){verifying.remove(token);if(alive&&walletStatus!=null)walletStatus.setText(t(key));}
        });
    }
    private void acceptWallet(ProductionWalletClient.Wallet value){
        if(!alive||value==null)return;
        if(currentWallet==null||!value.uid.equals(currentWallet.uid)||value.version>=currentWallet.version)currentWallet=value;
        serverBalance.setText(t("wallet_balance",currentWallet.balance));showPaidCatalog();showCatalog(billing.products());
    }
    private void showPaidCatalog(){
        if(!alive||paidCatalog==null)return;paidCatalog.removeAllViews();
        heading(paidCatalog,t("wallet_companions"),22,GOLD);
        for(Catalog.CharacterItem item:Catalog.CHARACTERS)if(item.price>0)addPaidItem("character:"+item.id,t(item.nameKey),item.price,ProductionWalletClient.ownsCharacter(this,item.id)||store.ownsCharacter(item.id));
        heading(paidCatalog,t("wallet_skins"),22,GOLD);
        for(Catalog.SkinItem item:Catalog.SKINS)if(item.price>0)addPaidItem("skin:"+item.id,t(item.nameKey),item.price,ProductionWalletClient.ownsSkin(this,item.id)||store.ownsSkin(item.id));
    }
    private void addPaidItem(String sku,String name,int price,boolean owned){
        Button buy=button(t(owned?"wallet_owned":"wallet_buy",name,price),()->new AlertDialog.Builder(this).setTitle(name)
                .setMessage(t("wallet_confirm",name,price)).setNegativeButton(t("cancel"),null)
                .setPositiveButton(t("yes"),(dialog,which)->spend(sku)).show());
        buy.setEnabled(!owned&&server!=null&&server.isConfigured()&&server.isSignedIn()&&currentWallet!=null&&currentWallet.balance>=price&&!spendBusy&&!walletBusy);paidCatalog.addView(buy);
    }
    private void spend(String sku){
        if(server==null||spendBusy||currentWallet==null)return;
        String signedUid=server.uid();if(signedUid.isEmpty()||!signedUid.equals(currentWallet.uid)){walletStatus.setText(t("wallet_sign_in_required"));return;}
        spendBusy=true;walletStatus.setText(t("wallet_loading"));showPaidCatalog();showCatalog(billing.products());
        // Keep an ambiguous network attempt's id until a successful response so a
        // retry after process death cannot debit the same cosmetic twice.
        SharedPreferences requests=getSharedPreferences("luma_wallet_requests_v1",0);
        String key=BillingPolicy.accountHash(signedUid)+"_"+sku;String existing=requests.getString(key,"");
        String request=existing.isEmpty()?UUID.randomUUID().toString():existing;
        if(!requests.edit().putString(key,request).commit()){spendBusy=false;walletStatus.setText(t("commerce_save_error"));showPaidCatalog();return;}
        server.spend(sku,request,new ProductionWalletClient.Callback(){
            public void onSuccess(ProductionWalletClient.Wallet result){requests.edit().remove(key).commit();spendBusy=false;acceptWallet(result);walletStatus.setText(t("wallet_unlocked"));}
            public void onError(String status){spendBusy=false;walletStatus.setText(t(status));showPaidCatalog();showCatalog(billing.products());}
        });
    }
    private void event(String name,String...values){Map<String,String> fields=new HashMap<>();for(int i=0;i+1<values.length;i+=2)fields.put(values[i],values[i+1]);journal.record(name,fields);}
    private void toast(String value){runOnUiThread(()->{if(alive)Toast.makeText(this,value,Toast.LENGTH_LONG).show();});}
    private int dp(float x){return Math.round(x*getResources().getDisplayMetrics().density);}
    private TextView heading(LinearLayout parent,String text,int size,int color){TextView v=new TextView(this);v.setText(text);v.setTextColor(color);v.setTextSize(size);v.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));v.setPadding(0,dp(9),0,dp(9));parent.addView(v);return v;}
    private TextView paragraph(LinearLayout parent,String text,int color){TextView v=new TextView(this);v.setText(text);v.setTextSize(15);v.setTextColor(color);v.setLineSpacing(dp(3),1);v.setPadding(0,dp(6),0,dp(12));parent.addView(v);return v;}
    private LinearLayout card(){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),dp(14),dp(18),dp(16));GradientDrawable background=new GradientDrawable();background.setColor(CARD);background.setCornerRadius(dp(22));background.setStroke(dp(1),0x22ffffff);box.setBackground(background);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(18);content.addView(box,p);return box;}
    private Button button(String text,Runnable action){Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setTextSize(15);b.setTextColor(BG);b.setMinHeight(dp(52));b.setPadding(dp(12),dp(10),dp(12),dp(10));GradientDrawable background=new GradientDrawable();background.setColor(MINT);background.setCornerRadius(dp(14));b.setBackground(background);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(9);p.bottomMargin=dp(4);b.setLayoutParams(p);b.setOnClickListener(v->action.run());return b;}
    private Switch toggle(LinearLayout parent,String label,boolean checked){Switch v=new Switch(this);v.setText(label);v.setTextColor(WHITE);v.setTextSize(15);v.setPadding(0,dp(10),0,dp(10));v.setMinHeight(dp(58));v.setChecked(checked);parent.addView(v,new LinearLayout.LayoutParams(-1,-2));return v;}
    @Override protected void onResume(){super.onResume();if(server!=null&&!server.uid().equals(observedUid)){observedUid=server.uid();walletBusy=false;spendBusy=false;currentWallet=null;verifying.clear();if(serverBalance!=null)serverBalance.setText(t("wallet_balance_unknown"));showPaidCatalog();}
        if(billing!=null&&!isAdPurpose()){billing.refreshPurchases();if(server!=null)refreshServerWallet();}if(ads!=null){ads.setLocalMetricsEnabled(journal.isEnabled());refresh();}}
    @Override protected void onSaveInstanceState(Bundle out){out.putBoolean("power_rewarded",powerRewarded);super.onSaveInstanceState(out);}
    @Override protected void onDestroy(){alive=false;if(ads!=null)ads.close();if(billing!=null)billing.close();if(server!=null)server.close();verifying.clear();super.onDestroy();}
}
