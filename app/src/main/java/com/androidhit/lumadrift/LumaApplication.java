package com.androidhit.lumadrift;

import android.app.Application;
import com.androidhit.lumadrift.ui.CharacterSprites;

/** Makes the same original artwork available to the game and its standalone activities. */
public final class LumaApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        CharacterSprites.install(getAssets());
        com.androidhit.lumadrift.ui.RoomMaterials.install(getAssets());
    }
}
