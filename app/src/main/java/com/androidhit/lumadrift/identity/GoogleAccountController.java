package com.androidhit.lumadrift.identity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.CancellationSignal;
import androidx.credentials.*;
import androidx.credentials.exceptions.*;
import com.google.android.libraries.identity.googleid.*;
import com.google.firebase.*;
import com.google.firebase.auth.*;
import java.security.SecureRandom;
import java.util.Base64;

/** Credential Manager -> Google token -> Firebase server verification. No fabricated profile. */
public final class GoogleAccountController {
    public interface Listener {void onState(String statusKey,String displayName,boolean signedIn);}
    private final Activity activity;private final Listener listener;private final IdentityConfig config;
    private final SharedPreferences prefs;private final CredentialManager manager;
    private FirebaseAuth auth;private FirebaseApp firebase;private CancellationSignal cancellation;
    private com.androidhit.lumadrift.wallet.ProductionWalletClient wallet;
    private long epoch;private boolean closed,busy;
    public GoogleAccountController(Activity activity,Listener listener){
        this.activity=activity;this.listener=listener;config=IdentityConfig.load(activity);
        prefs=activity.getSharedPreferences("luma_identity_state_v1",0);manager=CredentialManager.create(activity);
        if(config.isConfigured()&&prefs.getBoolean("previously_signed_in",false))initialize();
    }
    public boolean isConfigured(){return config.isConfigured();}
    public boolean isSignedIn(){return auth!=null&&auth.getCurrentUser()!=null;}
    public String displayName(){FirebaseUser user=auth==null?null:auth.getCurrentUser();return user==null?"":user.getDisplayName()!=null?user.getDisplayName():user.getEmail()!=null?user.getEmail():"Google";}
    public void announce(){report(isSignedIn()?"account_signed_in":"account_guest");}
    private boolean initialize(){
        if(auth!=null)return true;if(!config.isConfigured())return false;
        try{
            try{firebase=FirebaseApp.getInstance("LumaIdentity");}
            catch(IllegalStateException absent){firebase=FirebaseApp.initializeApp(activity.getApplicationContext(),new FirebaseOptions.Builder().setApiKey(config.apiKey).setApplicationId(config.appId).setProjectId(config.projectId).build(),"LumaIdentity");}
            firebase.setDataCollectionDefaultEnabled(false);auth=FirebaseAuth.getInstance(firebase);return true;
        }catch(RuntimeException invalid){report("google_error");return false;}
    }
    public void signIn(){
        if(closed||busy)return;if(!config.isConfigured()){report("google_not_configured");return;}
        if(!initialize())return;busy=true;long request=++epoch;report("google_loading");
        byte[] random=new byte[32];new SecureRandom().nextBytes(random);String nonce=Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        GetSignInWithGoogleOption option=new GetSignInWithGoogleOption.Builder(config.webClientId).setNonce(nonce).build();
        GetCredentialRequest wanted=new GetCredentialRequest.Builder().addCredentialOption(option).build();
        cancellation=new CancellationSignal();
        manager.getCredentialAsync(activity,wanted,cancellation,androidx.core.content.ContextCompat.getMainExecutor(activity),new CredentialManagerCallback<GetCredentialResponse,GetCredentialException>(){
            public void onResult(GetCredentialResponse response){
                if(!current(request))return;
                try{
                    Credential credential=response.getCredential();
                    if(!(credential instanceof CustomCredential)||!GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())){failed(request,"google_error");return;}
                    GoogleIdTokenCredential google=GoogleIdTokenCredential.createFrom(credential.getData());
                    String token=google.getIdToken();String[] parts=token.split("\\.");
                    if(parts.length!=3||token.length()>16384){failed(request,"google_error");return;}
                    org.json.JSONObject claims=new org.json.JSONObject(new String(android.util.Base64.decode(parts[1],android.util.Base64.URL_SAFE),java.nio.charset.StandardCharsets.UTF_8));
                    if(!nonce.equals(claims.optString("nonce"))||!config.webClientId.equals(claims.optString("aud"))){failed(request,"google_error");return;}
                    // Firebase verifies the Google assertion before exposing an authenticated user.
                    // Never store/log the ID token or treat a parsed JWT as a trusted account.
                    auth.signInWithCredential(GoogleAuthProvider.getCredential(google.getIdToken(),null)).addOnCompleteListener(activity,task->{
                        if(!current(request))return;busy=false;
                        if(task.isSuccessful()&&auth.getCurrentUser()!=null){prefs.edit().putBoolean("previously_signed_in",true).apply();report("account_signed_in");}
                        else report("google_error");
                    });
                }catch(Exception malformed){failed(request,"google_error");}
            }
            public void onError(GetCredentialException error){if(error instanceof NoCredentialException){failed(request,"google_error");return;}failed(request,error instanceof GetCredentialCancellationException?"google_cancelled":"google_error");}
        });
    }
    public void signOut(){if(closed)return;epoch++;busy=false;if(cancellation!=null)cancellation.cancel();if(auth!=null)auth.signOut();prefs.edit().putBoolean("previously_signed_in",false).apply();clearProvider();report("account_guest");}
    public void deleteAccount(){
        if(closed||busy||auth==null||auth.getCurrentUser()==null)return;busy=true;long request=++epoch;report("google_loading");
        if(!com.androidhit.lumadrift.commerce.CommerceConfig.MODE_SANDBOX&&com.androidhit.lumadrift.commerce.CommerceConfig.productionBillingReady()){
            if(wallet==null)wallet=new com.androidhit.lumadrift.wallet.ProductionWalletClient(activity);
            wallet.deleteAccount(new com.androidhit.lumadrift.wallet.ProductionWalletClient.Callback(){
                public void onSuccess(com.androidhit.lumadrift.wallet.ProductionWalletClient.Wallet ignored){
                    if(!current(request))return;busy=false;auth.signOut();prefs.edit().putBoolean("previously_signed_in",false).apply();clearProvider();report("account_deleted");
                }
                public void onError(String key){if(current(request)){busy=false;report(key.equals("account_recent_login")?key:"google_error");}}
            });
            return;
        }
        auth.getCurrentUser().delete().addOnCompleteListener(activity,task->{if(!current(request))return;busy=false;
            if(task.isSuccessful()){prefs.edit().putBoolean("previously_signed_in",false).apply();clearProvider();report("account_deleted");}
            else report(task.getException() instanceof FirebaseAuthRecentLoginRequiredException?"account_recent_login":"google_error");});
    }
    private void clearProvider(){manager.clearCredentialStateAsync(new ClearCredentialStateRequest(),null,androidx.core.content.ContextCompat.getMainExecutor(activity),new CredentialManagerCallback<Void,ClearCredentialException>(){public void onResult(Void value){}public void onError(ClearCredentialException error){}});}
    private boolean current(long request){return !closed&&request==epoch&&!activity.isFinishing()&&!activity.isDestroyed();}
    private void failed(long request,String key){if(current(request)){busy=false;report(key);}}
    private void report(String key){if(!closed)listener.onState(key,displayName(),isSignedIn());}
    public void close(){closed=true;epoch++;if(cancellation!=null)cancellation.cancel();if(wallet!=null)wallet.close();}
}
