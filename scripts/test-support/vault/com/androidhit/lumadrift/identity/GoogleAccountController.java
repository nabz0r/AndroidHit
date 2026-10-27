package com.androidhit.lumadrift.identity;
import android.content.Context;
import com.google.firebase.auth.FirebaseUser;
public final class GoogleAccountController {
    public static FirebaseUser user;
    public static FirebaseUser currentWalletUser(Context context){return user;}
}
