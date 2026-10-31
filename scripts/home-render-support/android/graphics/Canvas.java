package android.graphics;
import java.awt.*;import java.awt.geom.*;import java.awt.image.BufferedImage;import java.util.*;
/** Host-only Java2D approximation for geometry and art inspection. Never included in an APK. */
public final class Canvas {
    private final Graphics2D g;private final ArrayDeque<AffineTransform> transforms=new ArrayDeque<>();private final ArrayList<Shape> clips=new ArrayList<>();
    public Canvas(BufferedImage image){g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);}
    public int save(){int count=transforms.size()+1;transforms.push(g.getTransform());clips.add(g.getClip());return count;}public void restore(){g.setTransform(transforms.pop());g.setClip(clips.remove(clips.size()-1));}
    public void restoreToCount(int count){while(transforms.size()>=count)restore();}
    public void translate(float x,float y){g.translate(x,y);}public void scale(float x,float y){g.scale(x,y);}public void scale(float x,float y,float px,float py){g.translate(px,py);g.scale(x,y);g.translate(-px,-py);}
    public void rotate(float degrees,float x,float y){g.rotate(Math.toRadians(degrees),x,y);}public void skew(float x,float y){g.shear(x,y);}
    public void clipPath(Path path){g.clip(path.shape);}public void clipRect(float l,float t,float r,float b){g.clip(new Rectangle2D.Float(l,t,r-l,b-t));}
    public void clipOutPath(Path path){Area area=new Area(g.getClip()==null?new Rectangle2D.Float(-100000,-100000,200000,200000):g.getClip());area.subtract(new Area(path.shape));g.setClip(area);}
    private void paint(Shape shape,Paint paint){g.setColor(new java.awt.Color(paint.color,true));g.setStroke(new BasicStroke(Math.max(.1f,paint.width),BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));if(paint.style==Paint.Style.STROKE)g.draw(shape);else g.fill(shape);}
    public void drawPath(Path path,Paint paint){paint(path.shape,paint);}public void drawOval(float l,float t,float r,float b,Paint paint){paint(new Ellipse2D.Float(l,t,r-l,b-t),paint);}
    public void drawRoundRect(float l,float t,float r,float b,float rx,float ry,Paint paint){paint(new RoundRectangle2D.Float(l,t,r-l,b-t,rx*2,ry*2),paint);}
    public void drawLine(float x,float y,float xx,float yy,Paint paint){Paint.Style previous=paint.style;paint.style=Paint.Style.STROKE;paint(new Line2D.Float(x,y,xx,yy),paint);paint.style=previous;}
    public void drawArc(float l,float t,float r,float b,float start,float sweep,boolean center,Paint paint){paint(new Arc2D.Float(l,t,r-l,b-t,-start,-sweep,center?Arc2D.PIE:Arc2D.OPEN),paint);}
}
