package android.graphics;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;

/** Independent Java2D raster: original drawing source, transformed geometry and actual occlusion. */
public class Canvas {
    public final BufferedImage image;
    private Graphics2D graphics;
    private final ArrayDeque<Graphics2D> stack=new ArrayDeque<>();
    public int drawCalls;
    public Canvas(int width,int height){image=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);graphics=image.createGraphics();}
    public int save(){stack.push(graphics);graphics=(Graphics2D)graphics.create();return stack.size();}
    public void restore(){if(stack.isEmpty())throw new AssertionError("restore without save");graphics.dispose();graphics=stack.pop();}
    public void translate(float x,float y){graphics.translate(x,y);}
    public void scale(float x,float y){graphics.scale(x,y);}
    public void rotate(float degrees){graphics.rotate(Math.toRadians(degrees));}
    private void draw(Shape shape,Paint p,boolean alwaysStroke){
        if(p.alpha==0)return;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,p.antialias?RenderingHints.VALUE_ANTIALIAS_ON:RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setColor(new java.awt.Color((p.alpha<<24)|(p.color&0xffffff),true));
        graphics.setComposite(AlphaComposite.SrcOver);
        if(alwaysStroke||p.style==Paint.Style.STROKE){graphics.setStroke(new BasicStroke(Math.max(.001f,p.stroke),BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));graphics.draw(shape);}else graphics.fill(shape);
        drawCalls++;
    }
    public void drawCircle(float x,float y,float r,Paint p){drawOval(x-r,y-r,x+r,y+r,p);}
    public void drawOval(float l,float t,float r,float b,Paint p){draw(new Ellipse2D.Float(l,t,r-l,b-t),p,false);}
    public void drawRoundRect(float l,float t,float r,float b,float rx,float ry,Paint p){draw(new RoundRectangle2D.Float(l,t,r-l,b-t,rx*2,ry*2),p,false);}
    public void drawRect(float l,float t,float r,float b,Paint p){draw(new Rectangle2D.Float(l,t,r-l,b-t),p,false);}
    public void drawLine(float x,float y,float xx,float yy,Paint p){draw(new Line2D.Float(x,y,xx,yy),p,true);}
    public void drawArc(float l,float t,float r,float b,float start,float sweep,boolean center,Paint p){draw(new Arc2D.Float(l,t,r-l,b-t,-start,-sweep,center?Arc2D.PIE:Arc2D.OPEN),p,false);}
    public void drawPath(Path path,Paint p){draw(path.shape,p,false);}
    public boolean balanced(){return stack.isEmpty();}
}
