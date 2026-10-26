package com.androidhit.lumadrift.commerce;
import android.app.Activity;
import java.util.*;
public final class PlayGoldBilling {
    public interface Listener {void onStatus(String key);void onProducts(List<GoldProduct> products);void onPurchasePending(String id);void onPurchaseNeedsVerification(String id,String token);}
    public static final class GoldProduct {}
    public static final List<PlayGoldBilling> created=new ArrayList<>();
    public final Listener listener;public int connects;public boolean closed;
    public PlayGoldBilling(Activity owner,Listener listener){this.listener=listener;created.add(this);}
    public void connectForRecovery(){connects++;}
    public void close(){closed=true;}
}
