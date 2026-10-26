package android.content;

/** Minimal JVM-only adapter for exercising GameStore without an Android runtime. */
public abstract class Context {
    public static final int MODE_PRIVATE = 0;
    public Context getApplicationContext() { return this; }
    public abstract SharedPreferences getSharedPreferences(String name, int mode);
}
