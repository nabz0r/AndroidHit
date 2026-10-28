package android.graphics;

/** Java2D adapter, not Android pixels. Alpha follows Android setColor/setAlpha semantics. */
public class Paint {
    public static final int ANTI_ALIAS_FLAG=1;
    public enum Style { FILL, STROKE }
    public enum Cap { ROUND }
    int color=0xff000000,alpha=255; float stroke; boolean antialias; Style style=Style.FILL;
    public Paint(int flags){antialias=(flags&ANTI_ALIAS_FLAG)!=0;}
    public void setColor(int value){color=value;alpha=value>>>24;}
    public void setAlpha(int value){alpha=Math.max(0,Math.min(255,value));}
    public void setShader(Object value){if(value!=null)throw new AssertionError("Adapter excludes shaders");}
    public void setStyle(Style value){style=value;}
    public void setStrokeWidth(float value){stroke=value;}
    public void setStrokeCap(Cap value){}
    public void setAntiAlias(boolean value){antialias=value;}
}
