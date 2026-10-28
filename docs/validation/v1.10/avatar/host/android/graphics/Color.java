package android.graphics;
/** Pure host colour arithmetic; this class does not implement rasterisation. */
public final class Color {
    public static int parseColor(String value) { return (int)(0xff000000L | Long.parseLong(value.substring(1),16)); }
    public static int rgb(int r,int g,int b) { return 0xff000000 | r<<16 | g<<8 | b; }
    public static int red(int color) { return color>>16 & 255; }
    public static int green(int color) { return color>>8 & 255; }
    public static int blue(int color) { return color & 255; }
}
