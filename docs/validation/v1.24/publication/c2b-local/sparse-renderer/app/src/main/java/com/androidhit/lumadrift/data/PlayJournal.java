package com.androidhit.lumadrift.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * Optional, bounded device-local observations. No identifiers or network transport.
 * D1/D7 describe this device's enabled observation window, never global retention.
 */
@SuppressLint("ApplySharedPref")
public final class PlayJournal {
    public interface Clock { long now(); }
    public static final int MAX_EVENTS = 2000;
    public static final String UNKNOWN = "UNKNOWN";
    public static final String OBSERVED = "OBSERVED";
    public static final String NOT_OBSERVED_LOCAL = "NOT_OBSERVED_LOCAL";
    private final SharedPreferences prefs;
    private final Clock clock;

    public PlayJournal(Context context) { this(context, System::currentTimeMillis); }

    public PlayJournal(Context context, Clock clock) {
        this.clock = clock;
        prefs = context.getApplicationContext().getSharedPreferences("luma_drift_journal_v1", Context.MODE_PRIVATE);
    }

    public synchronized boolean isEnabled() { return prefs.getBoolean("enabled", false); }

    public synchronized void setEnabled(boolean enabled) {
        SharedPreferences.Editor edit = prefs.edit().putBoolean("enabled", enabled);
        if (!enabled && isEnabled() && !firstOpenDate().isEmpty()) edit.putBoolean("observation_paused", true);
        edit.commit();
        if (enabled && firstOpenDate().isEmpty()) prefs.edit().putString("first_observed", dayId()).commit();
    }

    /** First opt-in date, not the install date. Empty until the first opt-in. */
    public synchronized String firstOpenDate() { return prefs.getString("first_observed", ""); }
    public synchronized String firstObservedDate() { return firstOpenDate(); }
    public synchronized int eventCount() { return events().size(); }
    public synchronized int sessions() { return Math.max(0, prefs.getInt("sessions", 0)); }
    public synchronized int activeDays() { return dates().size(); }
    public synchronized String returnDay1() { return returnStatus(1); }
    public synchronized String returnDay7() { return returnStatus(7); }

    /** Call with session_start once for each foreground session. Other names are arbitrary local observations. */
    public synchronized void record(String name, Map<String, String> fields) {
        if (!isEnabled() || name == null || name.trim().isEmpty()) return;
        String day = dayId();
        List<String> days = dates();
        if (!days.contains(day)) days.add(day);
        StringBuilder event = new StringBuilder("{\"at\":").append(quote(utcTimestamp()))
                .append(",\"day\":").append(quote(day))
                .append(",\"event\":").append(quote(limit(name.trim(), 64)))
                .append(",\"fields\":{");
        int count = 0;
        if (fields != null) {
            // Stable field order makes exported runs comparable. Restrict payload size, not player behavior.
            Map<String, String> sorted = new TreeMap<>();
            for (Map.Entry<String, String> field : fields.entrySet()) if (field.getKey() != null) sorted.put(field.getKey(), field.getValue());
            for (Map.Entry<String, String> field : sorted.entrySet()) {
                if (count == 12) break;
                if (count++ > 0) event.append(',');
                event.append(quote(limit(field.getKey(), 48))).append(':').append(quote(limit(field.getValue(), 160)));
            }
        }
        event.append("}}");
        List<String> records = events();
        records.add(event.toString());
        if (records.size() > MAX_EVENTS) records = new ArrayList<>(records.subList(records.size() - MAX_EVENTS, records.size()));
        SharedPreferences.Editor edit = prefs.edit().putString("events", join(records, "\n"))
                .putString("active_dates", join(days, ","));
        if ("session_start".equals(name)) edit.putInt("sessions", Math.min(1_000_000_000, sessions() + 1));
        if (firstOpenDate().isEmpty()) edit.putString("first_observed", day);
        edit.commit();
    }

    /** Clears observations and starts a new window if enabled. No gameplay save is touched. */
    public synchronized void clear() {
        prefs.edit().putString("events", "").putString("active_dates", "").putInt("sessions", 0)
                .putString("first_observed", isEnabled() ? dayId() : "")
                .putBoolean("observation_paused", false).commit();
    }

    public synchronized String exportJson() {
        StringBuilder result = new StringBuilder("{\n  \"schema\": \"luma-drift-local-journal-v1\",\n")
                .append("  \"scope\": \"single-device opt-in observations; no global retention or acquisition metrics\",\n")
                .append("  \"summary_scope\": \"totals since first observation or last clear; event list retains only the latest 2000 records\",\n")
                .append("  \"enabled\": ").append(isEnabled()).append(",\n")
                .append("  \"exported_at\": ").append(quote(utcTimestamp())).append(",\n")
                .append("  \"first_observed_local_date\": ").append(quote(firstOpenDate())).append(",\n")
                .append("  \"sessions\": ").append(sessions()).append(",\n")
                .append("  \"active_days\": ").append(activeDays()).append(",\n")
                .append("  \"day_1\": ").append(quote(returnDay1())).append(",\n")
                .append("  \"day_7\": ").append(quote(returnDay7())).append(",\n")
                .append("  \"return_definition\": \"any recorded event on first_observed_local_date + N calendar days; UNKNOWN until that day ends, when disabled, or if observation was paused\",\n")
                .append("  \"max_events\": ").append(MAX_EVENTS).append(",\n  \"events\": [\n");
        List<String> records = events();
        for (int i = 0; i < records.size(); i++) result.append("    ").append(records.get(i)).append(i + 1 < records.size() ? ",\n" : "\n");
        return result.append("  ]\n}\n").toString();
    }

    private String returnStatus(int offset) {
        if (firstOpenDate().isEmpty()) return UNKNOWN;
        String target = addDays(firstOpenDate(), offset);
        if (target.isEmpty()) return UNKNOWN;
        if (dates().contains(target)) return OBSERVED;
        if (!isEnabled() || prefs.getBoolean("observation_paused", false)) return UNKNOWN;
        return dayId().compareTo(target) > 0 ? NOT_OBSERVED_LOCAL : UNKNOWN;
    }

    private List<String> events() { return split(prefs.getString("events", ""), "\n"); }
    private List<String> dates() { return split(prefs.getString("active_dates", ""), ","); }
    private String dayId() { return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(new Date(clock.now())); }
    private String utcTimestamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date(clock.now()));
    }

    private static String addDays(String day, int offset) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
            format.setLenient(false);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(format.parse(day));
            calendar.add(Calendar.DAY_OF_MONTH, offset);
            return format.format(calendar.getTime());
        } catch (Exception invalidDate) { return ""; }
    }
    private static List<String> split(String text, String separator) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) return result;
        for (String part : text.split(separator)) if (!part.isEmpty()) result.add(part);
        return result;
    }
    private static String join(List<String> values, String separator) {
        StringBuilder result = new StringBuilder();
        for (String value : values) { if (result.length() > 0) result.append(separator); result.append(value); }
        return result.toString();
    }
    private static String limit(String value, int max) { return value == null ? "" : value.substring(0, Math.min(value.length(), max)); }

    private static String quote(String value) {
        StringBuilder result = new StringBuilder("\"");
        if (value != null) for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"': result.append("\\\""); break;
                case '\\': result.append("\\\\"); break;
                case '\n': result.append("\\n"); break;
                case '\r': result.append("\\r"); break;
                case '\t': result.append("\\t"); break;
                default:
                    if (ch < 32 || Character.isSurrogate(ch)) result.append(String.format(Locale.ROOT, "\\u%04x", (int) ch));
                    else result.append(ch);
            }
        }
        return result.append('"').toString();
    }
}
