package android.graphics;
public final class Paint {
    public static final int ANTI_ALIAS_FLAG=1;
    public enum Style{FILL,STROKE} public enum Cap{ROUND} public enum Join{ROUND}
    int color;float width;Style style=Style.FILL;
    public Paint(int flags){} public void setShader(Object shader){} public void setColor(int value){color=value;}
    public void setStyle(Style value){style=value;} public void setStrokeWidth(float value){width=value;}
    public void setStrokeCap(Cap cap){} public void setStrokeJoin(Join join){}
}
