package android.graphics;
public final class Path {
    final java.awt.geom.Path2D.Float shape=new java.awt.geom.Path2D.Float();
    public void reset(){shape.reset();} public void moveTo(float x,float y){shape.moveTo(x,y);} public void lineTo(float x,float y){shape.lineTo(x,y);}
    public void quadTo(float x,float y,float xx,float yy){shape.quadTo(x,y,xx,yy);} public void cubicTo(float x,float y,float xx,float yy,float xxx,float yyy){shape.curveTo(x,y,xx,yy,xxx,yyy);}
    public void close(){shape.closePath();}
}
