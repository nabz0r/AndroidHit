package android.graphics;

/** Host geometry recorder only. */
public class Path {
    private final StringBuilder commands = new StringBuilder();
    public void reset() { commands.setLength(0); }
    public void moveTo(float x, float y) { commands.append('M').append(x).append(',').append(y); }
    public void lineTo(float x, float y) { commands.append('L').append(x).append(',').append(y); }
    public void cubicTo(float a,float b,float c,float d,float e,float f) { commands.append('C').append(a).append(',').append(b).append(',').append(c).append(',').append(d).append(',').append(e).append(',').append(f); }
    public void close() { commands.append('Z'); }
    String snapshot() { return commands.toString(); }
}
