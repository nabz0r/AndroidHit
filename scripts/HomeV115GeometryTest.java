import com.androidhit.lumadrift.data.*;import com.androidhit.lumadrift.ui.HomeScene;import android.graphics.*;
import java.util.*;import java.awt.image.BufferedImage;import java.nio.file.*;import javax.imageio.ImageIO;
public final class HomeV115GeometryTest{
    static int checks;
    static void check(boolean value,String note){checks++;if(!value)throw new AssertionError(note);}
    static void geometry(){
        HomeScene scene=new HomeScene();
        for(float zoom:new float[]{.85f,1,1.4f,2,2.5f})for(float panX:new float[]{-500,0,500})for(float panY:new float[]{-500,0,500}){
            scene.setCamera(zoom,panX,panY);
            for(int x=0;x<10;x++)for(int y=0;y<10;y++){PointF p=scene.projectedFloorPoint(x,y);int[] inverse=scene.cellAtCamera(p.x,p.y);check(inverse!=null&&inverse[0]==x&&inverse[1]==y,"same camera projection and inverse for every cell");}
            for(HomeCatalog.Item item:HomeCatalog.ITEMS){HomeLayout.Placement p=new HomeLayout.Placement(item.id,4,4,0);RectF bounds=new RectF();scene.projectedBoundsFor(p,bounds);check(Float.isFinite(bounds.left)&&bounds.width()>0&&bounds.height()>0,"finite projected sprite bounds");}
        }
        scene.setCamera(Float.NaN,Float.POSITIVE_INFINITY,Float.NaN);check(scene.cameraZoom()==1&&scene.cameraPanX()==0&&scene.cameraPanY()==0,"invalid gestures reset safely");
        scene.resetCamera();HomeLayout layout=new HomeLayout(Collections.singletonList(new HomeLayout.Placement("aquarium",7,7,0)),0,0);RectF box=new RectF();scene.projectedBoundsFor(layout.placement("aquarium"),box);
        check(scene.targetAt(layout,box.centerX(),box.centerY(),0,0,"idle")==0,"visual object hit uses camera and painter order");
        scene.setCamera(2,80,-40);scene.projectedBoundsFor(layout.placement("aquarium"),box);check(scene.targetAt(layout,box.centerX(),box.centerY(),0,0,"idle")==0,"object hit follows zoom and pan");
        check(scene.cellAtCamera(Float.NaN,30)==null&&scene.targetAt(layout,Float.NaN,20,0,0,"idle")==HomeScene.TARGET_OUTSIDE,"nonfinite pointer input refused");
        for(int x=0;x<10;x++)for(int y=0;y<10;y++){PointF p=HomeScene.floorPoint(x,y);int[] tile=HomeScene.cellAt(p.x,p.y);check(tile!=null&&tile[0]==x&&tile[1]==y,"raw ten-by-ten projection remains stable");}
    }
    static void preview(String path)throws Exception{
        if(path==null)return;int cols=6,cellW=230,cellH=230;int rows=(HomeCatalog.ITEMS.size()*2+cols-1)/cols;BufferedImage sheet=new BufferedImage(cols*cellW,rows*cellH,BufferedImage.TYPE_INT_ARGB);java.awt.Graphics2D labels=sheet.createGraphics();labels.setColor(new java.awt.Color(0xfffbf6ed,true));labels.fillRect(0,0,sheet.getWidth(),sheet.getHeight());labels.setFont(new java.awt.Font("SansSerif",java.awt.Font.PLAIN,13));Canvas c=new Canvas(sheet);HomeScene scene=new HomeScene();
        int index=0;for(HomeCatalog.Item item:HomeCatalog.ITEMS)for(int rotation=0;rotation<2;rotation++){
            int x=index%cols*cellW,y=index/cols*cellH;c.save();c.translate(x,y);scene.drawItem(c,item.id,new RectF(18,12,cellW-18,cellH-42),0,true,rotation);c.restore();labels.setColor(new java.awt.Color(0xff59475f,true));labels.drawString(item.id+" · "+(rotation==0?"A":"B"),x+12,y+cellH-14);index++;
        }
        labels.dispose();java.nio.file.Path out=java.nio.file.Path.of(path);if(out.getParent()!=null)Files.createDirectories(out.getParent());ImageIO.write(sheet,"png",out.toFile());
    }
    public static void main(String[] args)throws Exception{geometry();preview(args.length>0?args[0]:null);System.out.println("PASS "+checks+" home camera and geometry checks (Java2D preview is an approximation; native UI QA required)");}
}
