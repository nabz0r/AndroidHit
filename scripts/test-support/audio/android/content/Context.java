package android.content;
import android.media.AudioManager;
import java.io.File;
public final class Context {
    public static final String AUDIO_SERVICE = "audio";
    private final File cache;
    private final AudioManager manager;
    public Context(File cache, AudioManager manager) { this.cache = cache; this.manager = manager; }
    public Context getApplicationContext() { return this; }
    public Object getSystemService(String name) { return manager; }
    public File getCacheDir() { return cache; }
}
