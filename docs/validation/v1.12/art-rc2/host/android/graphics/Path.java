package android.graphics;
public class Path {
    final java.awt.geom.Path2D.Float shape=new java.awt.geom.Path2D.Float();
    public void reset(){shape.reset();}
    public void moveTo(float x,float y){shape.moveTo(x,y);}
    public void lineTo(float x,float y){shape.lineTo(x,y);}
    public void quadTo(float a,float b,float c,float d){shape.quadTo(a,b,c,d);}
    public void cubicTo(float a,float b,float c,float d,float e,float f){shape.curveTo(a,b,c,d,e,f);}
    public void close(){shape.closePath();}
}
