package android.graphics;

/** Records draw commands, not pixels; never describe this double as an Android render. */
public class Canvas {
    private final StringBuilder commands = new StringBuilder();
    private final StringBuilder geometry = new StringBuilder();
    public int depth;
    public int drawCalls;
    public int bitmapCalls;
    private void values(String method, float... values) { commands.append(method); geometry.append(method); for (float value : values) { if (!Float.isFinite(value)) throw new AssertionError("Nonfinite drawing coordinate"); commands.append(':').append(value); geometry.append(':').append(value); } commands.append(';'); geometry.append(';'); }
    private void paint(Paint paint) { commands.append(paint.snapshot()).append(';'); geometry.append(paint.style).append(':').append(paint.stroke).append(':').append(paint.antialias).append(';'); drawCalls++; }
    public int save() { depth++; commands.append("save;"); geometry.append("save;"); return depth; }
    public void restore() { if (--depth < 0) throw new AssertionError("Unbalanced restore"); commands.append("restore;"); geometry.append("restore;"); }
    public void translate(float x, float y) { values("translate", x, y); }
    public void scale(float x, float y) { values("scale", x, y); }
    public void rotate(float angle) { values("rotate", angle); }
    public void drawOval(float l,float t,float r,float b,Paint p) { if (r < l || b < t) throw new AssertionError("Reversed oval"); values("oval",l,t,r,b); paint(p); }
    public void drawRoundRect(float l,float t,float r,float b,float rx,float ry,Paint p) { if (r < l || b < t) throw new AssertionError("Reversed rectangle"); values("roundRect",l,t,r,b,rx,ry); paint(p); }
    public void drawRect(float l,float t,float r,float b,Paint p) { if (r < l || b < t) throw new AssertionError("Reversed rectangle"); values("rect",l,t,r,b); paint(p); }
    public void drawLine(float x,float y,float xx,float yy,Paint p) { values("line",x,y,xx,yy); paint(p); }
    public void drawArc(float l,float t,float r,float b,float start,float sweep,boolean center,Paint p) { values("arc",l,t,r,b,start,sweep); commands.append(center); geometry.append(center); paint(p); }
    public void drawPath(Path path,Paint p) { commands.append("path:").append(path.snapshot()); geometry.append("path:").append(path.snapshot()); paint(p); }
    public String signature() { return commands.toString(); }
    public String geometrySignature() { return geometry.toString(); }
    public void reset() { commands.setLength(0); geometry.setLength(0); depth=0; drawCalls=0; }
}
