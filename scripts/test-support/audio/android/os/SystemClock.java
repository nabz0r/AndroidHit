package android.os;
public final class SystemClock {
    static volatile long now;
    public static long uptimeMillis() { return now; }
}
