package com.androidhit.lumadrift.data;

import java.util.Calendar;
import java.util.TimeZone;

/** Same device-calendar reset boundaries as GameStore, including DST and Monday week starts. */
public final class EventSchedule {
    private EventSchedule() { }

    public static long nextResetMillis(long now, boolean weekly, TimeZone zone) {
        if (zone == null) throw new IllegalArgumentException("Time zone required");
        Calendar next = Calendar.getInstance(zone);
        next.setTimeInMillis(now);
        if (weekly) {
            int daysAfterMonday = (next.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            next.add(Calendar.DAY_OF_MONTH, 7 - daysAfterMonday);
        } else {
            next.add(Calendar.DAY_OF_MONTH, 1);
        }
        next.set(Calendar.HOUR_OF_DAY, 0);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        return next.getTimeInMillis();
    }

    /** Rounded up so a countdown never displays zero minutes before the period changes. */
    public static long remainingMinutes(long now, boolean weekly, TimeZone zone) {
        long remaining = Math.max(0L, nextResetMillis(now, weekly, zone) - now);
        return remaining / 60000L + (remaining % 60000L == 0L ? 0L : 1L);
    }
}
