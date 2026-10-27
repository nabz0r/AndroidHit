package android.content;

/** Only the Android API surface used by GameStore. Not included in the APK. */
public interface SharedPreferences {
    java.util.Map<String, ?> getAll();
    boolean contains(String key);
    int getInt(String key, int fallback);
    String getString(String key, String fallback);
    boolean getBoolean(String key, boolean fallback);
    Editor edit();

    interface Editor {
        Editor putInt(String key, int value);
        Editor putString(String key, String value);
        Editor putBoolean(String key, boolean value);
        boolean commit();
    }
}
