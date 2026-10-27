package com.androidhit.lumadrift;

import android.app.Activity;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowManager;

/** Luma Drift. Distributed under the repository's AGPL-3.0 license. */
public final class MainActivity extends Activity {
    private LumaView game;
    private com.androidhit.lumadrift.wallet.WalletRecoveryController walletRecovery;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(false);
        getWindow().setStatusBarColor(0x00000000);
        getWindow().setNavigationBarColor(0x00000000);
        game = new LumaView(this);
        setContentView(game);
        walletRecovery = new com.androidhit.lumadrift.wallet.WalletRecoveryController(this, () -> { if (game != null) game.invalidate(); });
        if(state!=null)game.restoreUiState(state);
        if(state==null&&getIntent().getData()!=null)game.openChallenge(getIntent().getData().toString());
        if(getIntent().getBooleanExtra("open_journal",false)){game.openJournal();getIntent().removeExtra("open_journal");}
        game.setOnApplyWindowInsetsListener((v, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets safe = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                game.setSafeInsets(safe.top, safe.bottom);
            } else game.setSafeInsets(insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        if (Build.VERSION.SDK_INT >= 33) getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0, () -> game.back());
    }
    @Override protected void onNewIntent(android.content.Intent intent){super.onNewIntent(intent);setIntent(intent);if(intent.getData()!=null)game.openChallenge(intent.getData().toString());if(intent.getBooleanExtra("open_journal",false)){game.openJournal();intent.removeExtra("open_journal");}}
    // API 33+ registers OnBackInvokedCallback above; this fallback serves API 26–32.
    @SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed() { game.back(); }
    @Override protected void onSaveInstanceState(Bundle out){game.saveUiState(out);super.onSaveInstanceState(out);}
    @Override protected void onResume() { super.onResume(); if (game != null) game.resumeApp(); if (walletRecovery != null) walletRecovery.onForeground(); }
    @Override protected void onPause() { if (walletRecovery != null) walletRecovery.onBackground(); if (game != null) game.pauseApp(); super.onPause(); }
    @Override protected void onDestroy() { if (walletRecovery != null) walletRecovery.close(); if (game != null) game.release(); super.onDestroy(); }
}
