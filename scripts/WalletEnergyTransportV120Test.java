package com.androidhit.lumadrift.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import com.androidhit.lumadrift.commerce.BillingPolicy;
import com.androidhit.lumadrift.commerce.CommerceConfig;
import com.androidhit.lumadrift.identity.GoogleAccountController;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

/** Actual ProductionWalletClient, fake ID token task + URLConnection. No sockets or real credentials. */
public final class WalletEnergyTransportV120Test {
    private static int checks;
    private static synchronized void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private record Reply(int status,String body){}
    private static final ArrayDeque<Reply> replies=new ArrayDeque<>();
    private static final List<Connection> connections=java.util.Collections.synchronizedList(new ArrayList<>());
    private static final class Connection extends HttpURLConnection {
        final ByteArrayOutputStream sent=new ByteArrayOutputStream();final Reply reply;boolean disconnected;
        Connection(URL url){super(url);synchronized(replies){reply=replies.removeFirst();}connections.add(this);}
        public void connect(){}public boolean usingProxy(){return false;}public void disconnect(){disconnected=true;}
        public OutputStream getOutputStream(){return sent;}
        public int getResponseCode(){return reply.status;}
        public InputStream getInputStream(){return new ByteArrayInputStream(reply.body.getBytes(StandardCharsets.UTF_8));}
        public InputStream getErrorStream(){return getInputStream();}
    }
    private static final class Prefs implements SharedPreferences {
        final Map<String,String> values=new HashMap<>();
        public String getString(String key,String fallback){return values.getOrDefault(key,fallback);}
        public Editor edit(){return new Editor(){final Map<String,String> changes=new HashMap<>();final Set<String> remove=new HashSet<>();
            public Editor putString(String key,String value){changes.put(key,value);return this;}public Editor remove(String key){remove.add(key);return this;}
            public boolean commit(){for(String key:remove)values.remove(key);values.putAll(changes);return true;}
        };}
    }
    private static final class Ctx extends Context {
        final Prefs prefs=new Prefs();public SharedPreferences getSharedPreferences(String name,int mode){return prefs;}
    }
    private static final class Capture implements ProductionWalletClient.Callback {
        volatile ProductionWalletClient.Wallet value;volatile String error="";volatile int calls;
        public void onSuccess(ProductionWalletClient.Wallet value){this.value=value;calls++;}
        public void onError(String key){error=key;calls++;}
    }
    private static void await(Capture c)throws Exception{
        long deadline=System.nanoTime()+3_000_000_000L;
        while(c.calls==0&&System.nanoTime()<deadline){Handler.advanceBy(0);Thread.sleep(2);}
        Handler.advanceBy(0);check(c.calls==1,"Exactly one bounded completion callback");
    }
    private static void token(FirebaseUser user){user.nextTask=new Task<>(true,new GetTokenResult("fixture-bearer-no-real-credential"),false);}
    private static JSONObject wallet(String uid,String id)throws Exception{
        return new JSONObject().put("balance",450).put("version",3).put("characters",new org.json.JSONArray()).put("skins",new org.json.JSONArray())
            .put("inventory",new JSONObject().put("energyRefills",0)).put("use",new JSONObject().put("id",id).put("sku","energy_refill10").put("kind","energy").put("itemId","energy_refill10").put("amount",10).put("owner",BillingPolicy.accountHash(uid)));
    }
    public static void main(String[] args)throws Exception{
        URL.setURLStreamHandlerFactory(protocol->"https".equals(protocol)?new URLStreamHandler(){protected URLConnection openConnection(URL url){check(url.getHost().equals("wallet.fixture.test"),"Bearer never routed to another host");return new Connection(url);}}:null);
        Ctx context=new Ctx();FirebaseUser user=new FirebaseUser("transport-google-A");GoogleAccountController.user=user;CommerceConfig.ready=true;CommerceConfig.MODE_SANDBOX=false;
        ProductionWalletClient client=new ProductionWalletClient(context);try{String id="energy-use-sdk-123";
        token(user);replies.add(new Reply(200,wallet(user.getUid(),id).toString()));Capture good=new Capture();client.useInventory("energy_refill10",id,good);await(good);
        check(good.error.isEmpty()&&good.value.uid.equals(user.getUid()),"Actual SDK client binds response to current account");check(good.value.use.getInt("amount")==10,"Use receipt survives actual response parser");
        Connection sent=connections.get(0);check(sent.getURL().getPath().equals("/v1/inventory/use")&&sent.getRequestMethod().equals("POST"),"Inventory-use method and path exact");
        JSONObject body=new JSONObject(sent.sent.toString(StandardCharsets.UTF_8));check(body.length()==2&&body.getString("requestId").equals(id)&&body.getString("sku").equals("energy_refill10"),"Client sends only stable request and SKU, never price or amount");
        check(sent.getRequestProperty("Authorization").equals("Bearer fixture-bearer-no-real-credential"),"Expected current Firebase bearer");check(!sent.getInstanceFollowRedirects(),"Automatic credential forwarding is disabled");
        check(sent.getConnectTimeout()==12000&&sent.getReadTimeout()==20000&&sent.disconnected,"Bounded timeouts and closed connection");check(ProductionWalletClient.cachedBalance(context)==450L,"Actual confirmed balance cache updated");
        for(String invalid:new String[]{"","short","id with spaces"}){Capture c=new Capture();int before=connections.size(),tokens=user.tokenCalls;client.useInventory("energy_refill10",invalid,c);await(c);check(c.error.equals("wallet_invalid_response")&&connections.size()==before&&tokens==user.tokenCalls,"Bad request rejected before token/network");}
        Capture sku=new Capture();client.useInventory("booster:hint",id,sku);await(sku);check(sku.error.equals("wallet_invalid_response"),"Energy client cannot consume unrelated inventory");
        token(user);replies.add(new Reply(409,"{\"error\":{\"code\":\"INSUFFICIENT_ITEM\"}}"));Capture empty=new Capture();client.useInventory("energy_refill10",id,empty);await(empty);check(empty.error.equals("wallet_item_insufficient"),"Definitive missing stock differs from ambiguous transport failure");
        token(user);replies.add(new Reply(302,"redirect"));Capture redirect=new Capture();int before=connections.size();client.useInventory("energy_refill10",id,redirect);await(redirect);check(redirect.error.equals("wallet_unavailable")&&connections.size()==before+1,"Redirect is refused, no second host request");
        token(user);replies.add(new Reply(200,"x".repeat(131073)));Capture huge=new Capture();client.useInventory("energy_refill10",id,huge);await(huge);check(huge.error.equals("wallet_invalid_response"),"Oversized success cannot bypass bound");
        token(user);replies.add(new Reply(200,wallet(user.getUid(),id).put("balance",1.5).toString()));Capture fraction=new Capture();client.useInventory("energy_refill10",id,fraction);await(fraction);check(fraction.error.equals("wallet_invalid_response"),"Fractional wallet balance refused");
        Task<GetTokenResult> delayed=new Task<>(true,new GetTokenResult("stale-account-fixture-token"),true);user.nextTask=delayed;Capture switched=new Capture();before=connections.size();client.useInventory("energy_refill10",id,switched);GoogleAccountController.user=new FirebaseUser("transport-google-B");delayed.complete();await(switched);check(switched.error.equals("wallet_sign_in_required")&&connections.size()==before,"Account changes before token completion cannot send old credential");
        GoogleAccountController.user=null;Capture guest=new Capture();client.useInventory("energy_refill10",id,guest);await(guest);check(guest.error.equals("wallet_sign_in_required"),"Guest cannot request inventory use");
        GoogleAccountController.user=user;CommerceConfig.ready=false;Capture gate=new Capture();client.useInventory("energy_refill10",id,gate);await(gate);check(gate.error.equals("wallet_not_configured"),"Unconfigured production endpoint stays closed");client.close();
        }finally{client.close();}
        System.out.println("Wallet energy transport1.20: "+checks+" checks passed (actual client; controlled URLConnection/Firebase doubles, no real socket or payment)");
    }
}
