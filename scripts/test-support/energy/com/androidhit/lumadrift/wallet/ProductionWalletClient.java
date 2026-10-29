package com.androidhit.lumadrift.wallet;
import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
public final class ProductionWalletClient {
    public interface Callback {void onSuccess(Wallet wallet);void onError(String key);}
    public static String currentUid="google-A";
    public static boolean configured=true;
    public static final List<ProductionWalletClient> created=new ArrayList<>();
    public static final class Wallet {
        public String uid;public long balance,version;public JSONObject inventory,entitlement,use;
        public Wallet(String uid,long balance,long version,JSONObject inventory,JSONObject entitlement,JSONObject use){this.uid=uid;this.balance=balance;this.version=version;this.inventory=inventory;this.entitlement=entitlement;this.use=use;}
    }
    public static final class Call {public final String id;public final Callback callback;Call(String id,Callback callback){this.id=id;this.callback=callback;}}
    public final List<Call> spends=new ArrayList<>(),uses=new ArrayList<>();
    public final List<Callback> refreshes=new ArrayList<>();
    public boolean closed;
    public ProductionWalletClient(Context context){created.add(this);}
    public static String currentUid(Context context){return currentUid;}
    public boolean isConfigured(){return configured;}
    public void spend(String sku,String id,Callback callback){if(!sku.equals("energy_refill10"))throw new AssertionError();spends.add(new Call(id,callback));}
    public void useInventory(String sku,String id,Callback callback){if(!sku.equals("energy_refill10"))throw new AssertionError();uses.add(new Call(id,callback));}
    public void refresh(Callback callback){refreshes.add(callback);}
    public void close(){closed=true;}
}
