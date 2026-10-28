import android.graphics.Canvas;
import android.graphics.RectF;
import com.androidhit.lumadrift.ui.ShopIllustrations;
import java.io.File;
import javax.imageio.ImageIO;
/** Host-only visual sheet: accent on left, label swash on right, four palettes. */
public class AccentRasterPreview {
 public static void main(String[] args)throws Exception{
  Canvas c=new Canvas(800,920);ShopIllustrations art=new ShopIllustrations();
  android.graphics.Paint p=new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);p.setColor(0xfff4eff8);c.drawRect(0,0,800,920,p);
  for(int i=0;i<4;i++){int y=20+i*225;art.heroAccent(c,new RectF(20,y,380,y+200),i,0,true);art.ribbon(c,new RectF(440,y+40,775,y+130),i);}
  if(!c.balanced())throw new AssertionError("unbalanced preview canvas");
  ImageIO.write(c.image,"png",new File(args[0]));
 }
}
