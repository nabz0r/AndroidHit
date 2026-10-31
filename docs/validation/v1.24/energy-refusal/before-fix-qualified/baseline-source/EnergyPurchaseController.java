package com.androidhit.lumadrift.wallet;

import android.content.Context;
import com.androidhit.lumadrift.commerce.BillingPolicy;
import com.androidhit.lumadrift.commerce.CommerceConfig;
import com.androidhit.lumadrift.data.SessionEnergyStore;
import org.json.JSONObject;

/** Explicit energy offer; no ads, billing checkout, Firebase sign-in or Coins grants. */
public final class EnergyPurchaseController {
    public interface Listener { void onChanged(); void onError(String key); }
    /** Remains closed until the energy endpoint and real-money offer are qualified. */
    public static final boolean PRODUCTION_ENERGY_ENABLED=false;
    private final Context context;
    private final SessionEnergyStore energy;
    private final Listener listener;
    private final EnergyPurchaseJournal journal;
    private final boolean lab,productionEnabled;
    private ProductionWalletClient server;
    private String session="",lastError="";
    private boolean busy,closed;
    private long generation;

    public EnergyPurchaseController(Context context,SessionEnergyStore energy,Listener listener){
        this(context,energy,listener,CommerceConfig.MODE_SANDBOX,PRODUCTION_ENERGY_ENABLED);
    }
    /** JVM contract injection; production callers use the public closed-gate constructor. */
    EnergyPurchaseController(Context context,SessionEnergyStore energy,Listener listener,boolean lab,boolean enabled){
        if(context==null||energy==null||listener==null)throw new IllegalArgumentException("Energy purchase owner required");
        this.context=context.getApplicationContext();this.energy=energy;this.listener=listener;this.lab=lab;this.productionEnabled=enabled;
        journal=new EnergyPurchaseJournal(this.context.getSharedPreferences("luma_energy_purchases_v120",0));
    }
    private String activeUid(){return lab?EnergyPurchaseJournal.LAB_UID:ProductionWalletClient.currentUid(context);}
    private void session(){
        String uid=activeUid();if(uid.equals(session))return;
        generation++;busy=false;lastError="";if(server!=null)server.close();server=null;session=uid;
    }
    private boolean current(long token,String uid){return !closed&&generation==token&&session.equals(uid)&&uid.equals(activeUid());}
    private void changed(){if(!closed)listener.onChanged();}
    private void error(String key){busy=false;lastError=key;if(!closed){listener.onError(key);listener.onChanged();}}
    public int priceGold(){return EnergyPurchaseJournal.PRICE;}
    public int packAmount(){return EnergyPurchaseJournal.AMOUNT;}
    public boolean isTestMode(){return lab;}
    public synchronized boolean isBusy(){return busy;}
    public synchronized int ownedRefills(){
        String uid=activeUid();if(uid.isEmpty())return 0;
        try{return journal.stock(uid)+(journal.state(uid).phase.equals("CREDIT")?1:0);}catch(RuntimeException unavailable){return 0;}
    }
    /** Dedicated Lab premium balance. It never reads or modifies GameStore Coins. */
    public synchronized int testGoldBalance(){if(!lab)return 0;try{journal.seedLab();return journal.balance(EnergyPurchaseJournal.LAB_UID);}catch(RuntimeException unavailable){return 0;}}
    public synchronized boolean available(){
        String uid=activeUid();if(closed||uid.isEmpty())return false;
        if(lab)return true;
        try{return CommerceConfig.productionBillingReady()&&(productionEnabled||journal.state(uid).pending()||journal.stock(uid)>0);}catch(RuntimeException unavailable){return false;}
    }
    public synchronized void onForeground(){
        if(closed)return;session();
        try{
            if(lab){journal.seedLab();resume();return;}
            // Guests and a closed, unused offer never initialize an HTTP client.
            if(session.isEmpty()||!available()){changed();return;}
            if(journal.state(session).pending())resume();else refresh();
        }catch(RuntimeException unavailable){error("commerce_save_error");}
    }
    /** Call only after displaying explicit 50 LumaGold -> one ten-energy pack confirmation. */
    public synchronized void buyRefill(){
        if(closed)return;session();if(busy)return;
        try{
            if(!session.isEmpty()&&journal.state(session).pending()){resume();return;}
            if(!lab&&!productionEnabled){error("energy_not_configured");return;}
            if(!available()){error(session.isEmpty()?"wallet_sign_in_required":"energy_not_configured");return;}
            if(energy.snapshot(System.currentTimeMillis()).count!=0){error("energy_only_empty");return;}
            if(journal.stock(session)>0){error("energy_pack_owned");return;}
            journal.start(session,true);lastError="";resume();
        }catch(IllegalArgumentException rejected){error("wallet_insufficient");}
        catch(RuntimeException unavailable){error("commerce_save_error");}
    }
    public synchronized void useStoredRefill(){
        if(closed)return;session();if(busy)return;
        try{
            if(!available()){error(session.isEmpty()?"wallet_sign_in_required":"energy_not_configured");return;}
            if(energy.snapshot(System.currentTimeMillis()).count!=0){error("energy_only_empty");return;}
            if(!journal.state(session).pending()){
                if(journal.stock(session)<1){error("energy_no_pack");return;}
                journal.start(session,false);
            }
            lastError="";resume();
        }catch(RuntimeException unavailable){error("commerce_save_error");}
    }
    private ProductionWalletClient server(){if(server==null)server=new ProductionWalletClient(context);return server;}
    private void accept(ProductionWalletClient.Wallet wallet,String uid){
        if(wallet==null||!uid.equals(wallet.uid))throw new IllegalArgumentException("Wallet account mismatch");
        Object value=wallet.inventory.opt("energyRefills");long stock=value==null?0:integer(value);
        if(stock<0||stock>10000)throw new IllegalArgumentException("Invalid refill stock");
        journal.snapshot(uid,(int)stock,wallet.balance,wallet.version);
    }
    private void refresh(){
        if(busy||session.isEmpty()||!server().isConfigured())return;
        busy=true;final long token=generation;final String uid=session;changed();
        server.refresh(new ProductionWalletClient.Callback(){
            public void onSuccess(ProductionWalletClient.Wallet value){synchronized(EnergyPurchaseController.this){if(!current(token,uid))return;busy=false;try{accept(value,uid);lastError="";changed();}catch(RuntimeException malformed){error("wallet_invalid_response");}}}
            public void onError(String key){synchronized(EnergyPurchaseController.this){if(current(token,uid))error(key);}}
        });
    }
    private static long integer(Object value){if(!(value instanceof Integer||value instanceof Long))throw new IllegalArgumentException("Exact integer required");return ((Number)value).longValue();}
    private static void receipt(JSONObject value,String uid,EnergyPurchaseJournal.State state,boolean use){
        try{
            if(value==null||!value.getString("id").equals(use?state.useId:state.spendId)||!value.getString("sku").equals(EnergyPurchaseJournal.SKU)
                    ||!value.getString("kind").equals("energy")||!value.getString("itemId").equals(EnergyPurchaseJournal.SKU)
                    ||integer(value.get("amount"))!=EnergyPurchaseJournal.AMOUNT||!value.getString("owner").equals(BillingPolicy.accountHash(uid)))throw new IllegalArgumentException("Receipt mismatch");
        }catch(Exception invalid){throw new IllegalArgumentException("Invalid energy receipt");}
    }
    private void resume(){
        if(closed||busy||session.isEmpty())return;
        EnergyPurchaseJournal.State state=journal.state(session);
        if(!state.pending()){changed();return;}
        if(state.phase.equals("CREDIT")){credit(state);return;}
        // Restoring the original spend can recover a paid pack at any energy count.
        // Inventory consumption and local full credit only happen at zero.
        if(state.phase.equals("USE")&&energy.snapshot(System.currentTimeMillis()).count!=0){changed();return;}
        if(lab){
            if(state.phase.equals("SPEND")){journal.labSpend(state);journal.advance(session,state,"USE");}
            else{journal.labUse(state);journal.advance(session,state,"CREDIT");}
            resume();return;
        }
        if(!server().isConfigured()){error("energy_not_configured");return;}
        busy=true;final long token=generation;final String uid=session;changed();
        ProductionWalletClient.Callback callback=new ProductionWalletClient.Callback(){
            public void onSuccess(ProductionWalletClient.Wallet wallet){synchronized(EnergyPurchaseController.this){
                if(!current(token,uid))return;busy=false;
                try{
                    receipt(state.phase.equals("SPEND")?wallet.entitlement:wallet.use,uid,state,!state.phase.equals("SPEND"));
                    if(!journal.state(uid).same(state)){changed();return;}
                    accept(wallet,uid);
                    // A second controller's late answer cannot advance a different purchase.
                    if(!journal.advance(uid,state,state.phase.equals("SPEND")?"USE":"CREDIT")){changed();return;}
                    lastError="";resume();
                }catch(IllegalArgumentException invalid){error("wallet_invalid_response");}
                catch(RuntimeException unavailable){error("commerce_save_error");}
            }}
            public void onError(String key){synchronized(EnergyPurchaseController.this){
                if(!current(token,uid))return;
                try{
                    if(state.phase.equals("USE")&&key.equals("wallet_item_insufficient")){journal.discardUnconsumedUse(uid,state);error("energy_no_pack");}
                    else error(key);
                }catch(RuntimeException unavailable){error("commerce_save_error");}
            }}
        };
        if(state.phase.equals("SPEND"))server.spend(EnergyPurchaseJournal.SKU,state.spendId,callback);
        else server.useInventory(EnergyPurchaseJournal.SKU,state.useId,callback);
    }
    private void credit(EnergyPurchaseJournal.State state){
        String hash=EnergyPurchaseJournal.receiptHash(session,state.useId);long now=System.currentTimeMillis();
        if(!energy.hasReceipt(hash)){
            if(energy.snapshot(now).count!=0){changed();return;}
            energy.creditReceipt(hash,EnergyPurchaseJournal.AMOUNT,now);
        }
        if(!energy.hasReceipt(hash))throw new IllegalStateException("Energy credit not durable");
        journal.clear(session,state);lastError="";changed();
    }
    public synchronized void close(){if(closed)return;closed=true;generation++;busy=false;if(server!=null)server.close();server=null;}

    /** Full, readable offer/status text. No price hidden behind a second currency. */
    public synchronized String statusLabel(String language){
        int l=language!=null&&language.startsWith("fr")?0:language!=null&&language.startsWith("de")?2:language!=null&&language.startsWith("es")?3:language!=null&&language.startsWith("it")?4:language!=null&&language.startsWith("pt")?5:1;
        String[][] text={
            {"Confirmation en cours…","Confirming…","Wird bestätigt…","Confirmando…","Conferma in corso…","A confirmar…"},
            {"Recharge LumaGold bientôt disponible.","LumaGold refills are not available yet.","LumaGold-Aufladungen sind noch nicht verfügbar.","Las recargas LumaGold aún no están disponibles.","Ricariche LumaGold non ancora disponibili.","Recargas LumaGold ainda indisponíveis."},
            {"Utilise la recharge à 0 énergie : les 10 points sont conservés.","Use the refill at 0 energy: all 10 points are saved.","Aufladung bei 0 Energie nutzen: alle 10 Punkte bleiben erhalten.","Usa la recarga a 0 energia: se conservan los 10 puntos.","Usa la ricarica a 0 energia: tutti i 10 punti sono conservati.","Usa a recarga a 0 energia: os 10 pontos ficam guardados."},
            {"Recharge de 10 prête à utiliser.","A 10-energy refill is ready.","Eine Aufladung mit 10 Energie ist bereit.","Recarga de 10 de energía lista.","Ricarica da 10 energia pronta.","Recarga de 10 de energia pronta."},
            {"10 énergie = 50 LumaGold · achat à 0 énergie.","10 energy = 50 LumaGold · buy at 0 energy.","10 Energie = 50 LumaGold · Kauf bei 0 Energie.","10 de energía = 50 LumaGold · compra a 0 energia.","10 energia = 50 LumaGold · acquisto a 0 energia.","10 de energia = 50 LumaGold · compra a 0 energia."},
            {"Connexion Google requise.","Google sign-in required.","Google-Anmeldung erforderlich.","Inicia sesión con Google.","Accedi con Google.","Inicia sessão com o Google."},
            {"LumaGold insuffisants. Aucun nouveau débit.","Not enough LumaGold. No new debit.","Nicht genug LumaGold. Keine neue Abbuchung.","LumaGold insuficientes. Sin nuevo cargo.","LumaGold insufficienti. Nessun nuovo addebito.","LumaGold insuficientes. Sem novo débito."},
            {"Confirmation en attente. Réessaie pour reprendre la même recharge.","Confirmation pending. Retry to recover the same refill.","Bestätigung ausstehend. Dieselbe Aufladung erneut abrufen.","Confirmación pendiente. Reintenta la misma recarga.","Conferma in attesa. Riprova la stessa ricarica.","Confirmação pendente. Tenta recuperar a mesma recarga."}
        };
        if(busy)return text[0][l];
        if(lastError.equals("wallet_sign_in_required"))return text[5][l];
        if(lastError.equals("wallet_insufficient"))return text[6][l];
        if(!lastError.isEmpty()&&!lastError.equals("energy_only_empty")&&!lastError.equals("energy_pack_owned")&&!lastError.equals("energy_not_configured"))return text[7][l];
        if(!available())return text[1][l];
        if(ownedRefills()>0)return text[energy.snapshot(System.currentTimeMillis()).count==0?3:2][l];
        if(lab)return "LAB · "+testGoldBalance()+" LumaGold test · 10 = 50";
        return text[4][l];
    }
}
