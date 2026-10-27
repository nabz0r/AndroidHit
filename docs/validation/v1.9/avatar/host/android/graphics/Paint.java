package android.graphics;

/** Host recording double; it does not rasterise Android pixels. */
public class Paint {
    public static final int ANTI_ALIAS_FLAG = 1;
    public enum Style { FILL, STROKE }
    public enum Cap { ROUND }
    int color, alpha = 255;
    float stroke;
    boolean antialias;
    Style style = Style.FILL;
    public Paint(int flags) { antialias = (flags & ANTI_ALIAS_FLAG) != 0; }
    public void setColor(int value) { color = value; }
    public void setAlpha(int value) { alpha = value; }
    public void setShader(Object value) { if (value != null) throw new AssertionError("No per-frame shader expected"); }
    public void setStyle(Style value) { style = value; }
    public void setStrokeWidth(float value) { stroke = value; }
    public void setStrokeCap(Cap value) { }
    public void setAntiAlias(boolean value) { antialias = value; }
    String snapshot() { return color + ":" + alpha + ":" + style + ":" + stroke + ":" + antialias; }
}
