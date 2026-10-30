package android.graphics;

/** Pure JVM implementation of the two color helpers used by Catalog. */
public final class Color {
    public static int parseColor(String text) {
        return (int) (0xff000000L | Long.parseLong(text.substring(1), 16));
    }
    public static int rgb(int red, int green, int blue) {
        return 0xff000000 | (red << 16) | (green << 8) | blue;
    }
}
