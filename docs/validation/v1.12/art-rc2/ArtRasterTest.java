import android.graphics.Canvas;
import android.graphics.RectF;
import com.androidhit.lumadrift.ui.ShopIllustrations;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

public class ArtRasterTest {
  static final String[] IDS={"hint","vision","flow","shield","focus","trail","coin","gold","capsule"};
  static final ShopIllustrations ART=new ShopIllustrations();
  static int checks=0, minPair=Integer.MAX_VALUE;
  static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
  static Canvas icon(String id,float r,long time,boolean still){Canvas c=new Canvas(192,192);switch(id){case "coin":ART.coin(c,96,96,r,time,still);break;case "gold":ART.gold(c,96,96,r,time,still);break;case "capsule":ART.capsule(c,96,96,r,time,still);break;default:ART.power(c,id,96,96,r,time,still);}return c;}
  static int delta(BufferedImage a,BufferedImage b,boolean silhouette){int n=0;for(int y=0;y<a.getHeight();y++)for(int x=0;x<a.getWidth();x++){int aa=a.getRGB(x,y),bb=b.getRGB(x,y);if(silhouette){aa=aa>>>24;bb=bb>>>24;}if(aa!=bb)n++;}return n;}
  static int count(BufferedImage image){int n=0;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if((image.getRGB(x,y)>>>24)!=0)n++;return n;}
  static void bounded(Canvas c,float r,String id){int outside=0;for(int y=0;y<c.image.getHeight();y++)for(int x=0;x<c.image.getWidth();x++){if((c.image.getRGB(x,y)>>>24)!=0&&(Math.abs(x-96)>r*1.18f+1||Math.abs(y-96)>r*1.18f+1))outside++;}check(outside==0,id+" exceeds documented illustration allocation");check(c.balanced(),id+" leaked canvas transform");check(count(c.image)>r*r*.8,id+" not visibly drawn");}
  public static void main(String[] args)throws Exception{
    BufferedImage sheet=new BufferedImage(9*192,192,BufferedImage.TYPE_INT_ARGB);
    java.awt.Graphics2D g=sheet.createGraphics();g.setColor(new java.awt.Color(0xfff4eff8,true));g.fillRect(0,0,sheet.getWidth(),sheet.getHeight());
    for(int i=0;i<IDS.length;i++){
      String id=IDS[i];
      for(float r:new float[]{14,20,28,40,64}){
        Canvas start=icon(id,r,0,true),later=icon(id,r,987654,true);
        bounded(start,r,id+"@"+r);bounded(later,r,id+"@"+r);
        check(delta(start.image,later.image,false)==0,id+" moves with reduced motion");
      }
      Canvas a=icon(id,50,0,false),b=icon(id,50,921,false);
      check(delta(a.image,b.image,false)>0,id+" animation has no visible response to supplied time");
      bounded(a,50,id+" animated");bounded(b,50,id+" animated");
      g.drawImage(icon(id,60,0,true).image,i*192,0,null);
      ImageIO.write(icon(id,40,0,true).image,"png",new File(args[0]+"/"+id+".png"));
    }
    g.dispose();ImageIO.write(sheet,"png",new File(args[0]+"/shop-art-atlas.png"));
    for(int i=0;i<IDS.length;i++)for(int j=i+1;j<IDS.length;j++){
      Canvas a=icon(IDS[i],40,0,true),b=icon(IDS[j],40,0,true);
      int difference=delta(a.image,b.image,true);minPair=Math.min(minPair,difference);
      check(difference>320,"Silhouette alias between "+IDS[i]+" and "+IDS[j]);
    }
    Canvas neutral=icon("unknown",40,0,true);bounded(neutral,40,"unknown fallback");check(delta(neutral.image,icon("flow",40,0,true).image,false)>1000,"Unknown aliases a real power");
    for(int tone=0;tone<4;tone++){
      Canvas a=new Canvas(192,192),b=new Canvas(192,192);RectF box=new RectF(20,30,170,160);
      ART.heroAccent(a,box,tone,0,true);ART.heroAccent(b,box,tone,9854,true);check(delta(a.image,b.image,false)==0,"Hero accent moves with reduced motion");check(a.balanced()&&b.balanced(),"Accent leaked canvas state");
      int outside=0;for(int y=0;y<192;y++)for(int x=0;x<192;x++)if((a.image.getRGB(x,y)>>>24)!=0&&(x<19||x>171||y<29||y>161))outside++;
      check(outside==0,"Header decoration outside caller's rectangle");
      Canvas ribbon=new Canvas(192,192);ART.ribbon(ribbon,box,tone);check(ribbon.balanced(),"Ribbon canvas state");check(count(ribbon.image)>1000,"Ribbon invisible");
    }
    try(PrintWriter out=new PrintWriter(new File(args[0]+"/raster-result.json"))){out.println("{\"passed\":true,\"checks\":"+checks+",\"glyphs\":9,\"sizes\":[14,20,28,40,64],\"min_pair_alpha_difference_pixels_at_r40\":"+minPair+",\"scope\":\"Independent Java2D adapter, not Android pixels or human recognition\"}");}
    System.out.println("PASS "+checks+" raster checks; minimum silhouette difference "+minPair+" pixels");
  }
}
