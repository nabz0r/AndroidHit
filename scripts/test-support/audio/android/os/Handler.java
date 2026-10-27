package android.os;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
public final class Handler {
    private static final List<Entry> tasks = new ArrayList<>();
    private record Entry(Runnable runnable, long time) { }
    public Handler(Looper looper) { }
    public boolean post(Runnable runnable) { return postDelayed(runnable, 0); }
    public boolean postDelayed(Runnable runnable, long delay) {
        synchronized (tasks) { tasks.add(new Entry(runnable, SystemClock.now + delay)); } return true;
    }
    public void removeCallbacks(Runnable runnable) {
        synchronized (tasks) { tasks.removeIf(entry -> entry.runnable == runnable); }
    }
    public static int pending() { synchronized (tasks) { return tasks.size(); } }
    public static void advanceBy(long duration) {
        long end = SystemClock.now + duration;
        while (true) {
            Entry next;
            synchronized (tasks) {
                next = tasks.stream().min(Comparator.comparingLong(Entry::time)).orElse(null);
                if (next == null || next.time > end) break;
                tasks.remove(next);
            }
            SystemClock.now = next.time;
            next.runnable.run();
        }
        SystemClock.now = end;
    }
}
