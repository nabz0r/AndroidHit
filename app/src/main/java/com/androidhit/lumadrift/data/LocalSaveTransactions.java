package com.androidhit.lumadrift.data;

import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** Small local atomic writes. Android may update memory before a failing disk commit. */
final class LocalSaveTransactions {
    static final Object LOCK = new Object();
    private static final Map<SharedPreferences, Map<String, Object>> PENDING = new WeakHashMap<>();
    private LocalSaveTransactions() { }
    static void flush(SharedPreferences prefs) {
        synchronized (LOCK) {
            Map<String, Object> pending = PENDING.get(prefs);
            if (pending == null) return;
            if (!editor(prefs, pending).commit()) throw new IllegalStateException("Could not persist pending local reward");
            PENDING.remove(prefs);
        }
    }
    static void write(SharedPreferences prefs, Map<String, Object> values) {
        synchronized (LOCK) {
            flush(prefs);
            PENDING.put(prefs, new HashMap<>(values));
            if (!editor(prefs, values).commit()) throw new IllegalStateException("Could not persist local reward");
            PENDING.remove(prefs);
        }
    }
    private static SharedPreferences.Editor editor(SharedPreferences prefs, Map<String, Object> values) {
        SharedPreferences.Editor edit = prefs.edit();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Integer) edit.putInt(entry.getKey(), (Integer) value);
            else if (value instanceof Boolean) edit.putBoolean(entry.getKey(), (Boolean) value);
            else if (value instanceof String) edit.putString(entry.getKey(), (String) value);
            else throw new IllegalArgumentException("Unsupported local save type");
        }
        return edit;
    }
}
