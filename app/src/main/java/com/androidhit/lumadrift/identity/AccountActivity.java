package com.androidhit.lumadrift.identity;

import android.app.*;
import android.os.Bundle;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.*;
import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.ui.SceneActionView;
import com.androidhit.lumadrift.ui.ScenicUi;

/** Optional account surface. Guest play and local progress never depend on login. */
public final class AccountActivity extends Activity {
    private GameStore store;private GoogleAccountController account;private TextView status,profile;
    private LinearLayout body;private SceneActionView signIn,signOut,delete;
    private static final int BG=0xfffff6ec,WHITE=0xff263049,MINT=0xff237d75,MUTED=0xff6c667b;
    private String t(String key,Object...args){return Strings.get(store.resolvedLanguage(),key,args);}
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);store=new GameStore(this);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        ScrollView scroll=new ScrollView(this);scroll.setBackground(SceneActionView.atmosphere());
        scroll.setOnApplyWindowInsetsListener((view,insets)->{if(android.os.Build.VERSION.SDK_INT>=30){android.graphics.Insets s=insets.getInsets(android.view.WindowInsets.Type.systemBars()|android.view.WindowInsets.Type.displayCutout());view.setPadding(s.left,s.top,s.right,s.bottom);}else view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
        FrameLayout center=new FrameLayout(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(24),dp(24),dp(24),dp(36));center.addView(body,new FrameLayout.LayoutParams(Math.min(getResources().getDisplayMetrics().widthPixels,dp(520)),-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL));scroll.addView(center);setContentView(scroll);
        addButton("‹  "+t("back"),()->finish());text("LUMA DRIFT",17,MINT);text(t("account"),34,WHITE);text(t("account_note"),16,MUTED);
        profile=text(t("account_guest"),24,WHITE);status=text("",15,MUTED);
        signIn=addButton(t("google_sign_in"),()->account.signIn());
        android.graphics.drawable.Drawable google=androidx.core.content.ContextCompat.getDrawable(this,com.androidhit.lumadrift.R.drawable.google_g);google.setBounds(0,0,dp(22),dp(22));signIn.setCompoundDrawables(google,null,null,null);signIn.setCompoundDrawablePadding(dp(10));signIn.setPadding(dp(12),dp(12),dp(12),dp(12));
        signOut=addButton(t("google_sign_out"),()->SceneActionView.showChoices(this,t("google_sign_out"),t("account_sign_out_note"),
                new SceneActionView.Choice(t("cancel"),ScenicUi.Kind.CANCEL,null),new SceneActionView.Choice(t("yes"),ScenicUi.Kind.CONFIRM,()->account.signOut())));
        delete=addButton(t("account_delete"),()->SceneActionView.showChoices(this,t("account_delete"),t(com.androidhit.lumadrift.commerce.CommerceConfig.MODE_SANDBOX?"account_delete_note":"production_account_delete_note"),
                new SceneActionView.Choice(t("cancel"),ScenicUi.Kind.CANCEL,null),new SceneActionView.Choice(t("yes"),ScenicUi.Kind.CONFIRM,ScenicUi.CORAL,()->account.deleteAccount())));
        text(t("account_no_cloud"),14,MINT);
        account=new GoogleAccountController(this,state->{
            if(status==null)return;
            status.setText(state.signedIn?t("account_signed_in",state.displayName):t(state.statusKey));
            profile.setText(state.signedIn?t("account_signed_in",state.displayName):t(state.state==SessionSnapshot.State.REFRESHING?"account_refreshing":"account_guest"));
            // Keep the branded button's standard label; the status explains when reconnecting is needed.
            signIn.setText(t("google_sign_in"));
            signIn.setVisibility(state.signedIn?android.view.View.GONE:android.view.View.VISIBLE);
            signIn.setEnabled(state.canSignIn);
            signOut.setVisibility(state.canSignOut?android.view.View.VISIBLE:android.view.View.GONE);
            signOut.setEnabled(state.canSignOut);
            delete.setVisibility(state.signedIn?android.view.View.VISIBLE:android.view.View.GONE);
            delete.setEnabled(state.canDeleteAccount);
        });account.announce();
        if(!account.isConfigured())status.setText(t("google_not_configured"));
    }
    private TextView text(String value,int size,int color){TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(color);v.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));v.setLineSpacing(dp(3),1);v.setPadding(0,dp(12),0,dp(16));body.addView(v);return v;}
    private SceneActionView addButton(String value,Runnable action){ScenicUi.Kind kind=value.equals(t("google_sign_in"))?ScenicUi.Kind.MAIL:value.equals(t("account_delete"))?ScenicUi.Kind.CANCEL:ScenicUi.Kind.RETURN;SceneActionView b=SceneActionView.create(this,value,kind,MINT,action);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(12);body.addView(b,p);return b;}
    private int dp(float x){return Math.round(x*getResources().getDisplayMetrics().density);}
    @Override protected void onResume(){super.onResume();if(account!=null)account.onResume();}
    @Override protected void onPause(){if(account!=null)account.onPause();super.onPause();}
    @Override protected void onDestroy(){if(account!=null)account.close();super.onDestroy();}
}
