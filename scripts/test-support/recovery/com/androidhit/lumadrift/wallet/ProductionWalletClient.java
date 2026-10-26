package com.androidhit.lumadrift.wallet;
import android.content.Context;
import java.util.*;
public final class ProductionWalletClient {
    public interface Callback {void onSuccess(Wallet wallet);void onError(String status);}
    public static final class Wallet {}
    public static final List<ProductionWalletClient> created=new ArrayList<>();
    public static String currentUid="";public static boolean configured=true;
    public final List<Callback> refreshes=new ArrayList<>(),verifications=new ArrayList<>();
    public final List<String> tokens=new ArrayList<>();public boolean closed;
    public ProductionWalletClient(Context context){created.add(this);}
    public static String currentUid(Context context){return currentUid;}
    public boolean isConfigured(){return configured;}
    public void refresh(Callback callback){refreshes.add(callback);}
    public void verify(String product,String token,Callback callback){tokens.add(token);verifications.add(callback);}
    public void close(){closed=true;}
}
