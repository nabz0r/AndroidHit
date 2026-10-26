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
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(false);
        getWindow().setStatusBarColor(0x00000000);
        getWindow().setNavigationBarColor(0x00000000);
        game = new LumaView(this);
        setContentView(game);
        if(state!=null)game.restoreScreen(state.getString("screen"));
        if(state==null&&getIntent().getData()!=null)game.openChallenge(getIntent().getData().toString());
        game.setOnApplyWindowInsetsListener((v, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets safe = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                game.setSafeInsets(safe.top, safe.bottom);
            } else game.setSafeInsets(insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        if (Build.VERSION.SDK_INT >= 33) getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0, () -> game.back());
    }
    @Override protected void onNewIntent(android.content.Intent intent){super.onNewIntent(intent);setIntent(intent);if(intent.getData()!=null)game.openChallenge(intent.getData().toString());}
    // API 33+ registers OnBackInvokedCallback above; this fallback serves API 26–32.
    @SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed() { game.back(); }
    @Override protected void onSaveInstanceState(Bundle out){out.putString("screen",game.screenName());super.onSaveInstanceState(out);}
    @Override protected void onResume() { super.onResume(); if (game != null) game.resumeApp(); }
    @Override protected void onPause() { if (game != null) game.pauseApp(); super.onPause(); }
    @Override protected void onDestroy() { if (game != null) game.release(); super.onDestroy(); }
}
