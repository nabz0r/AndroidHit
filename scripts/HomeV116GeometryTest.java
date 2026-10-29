import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.ui.HomeScene;
import android.graphics.*;
import java.awt.image.BufferedImage;
import java.util.Collections;

/** Camera contract for the enlarged native home. The Java2D renderer is only a host approximation. */
public final class HomeV116GeometryTest {
    private static int checks;
    private static final float EPS=.002f;
    private static void check(boolean value,String note){checks++;if(!value)throw new AssertionError(note);}
    private static void near(float actual,float expected,String note){check(Math.abs(actual-expected)<EPS,note+": "+actual+" != "+expected);}
    private static float fit(float w,float h){return Math.min(w/640,h/480);}

    private static void cameraContract(float width,float height){
        HomeScene scene=new HomeScene();scene.setViewport(width,height);
        near(scene.viewportWidth(),width,"effective width");near(scene.viewportHeight(),height,"effective height");
        near(scene.cameraZoom(),HomeScene.DETAIL_ZOOM,"new room opens in detail view");
        HomeLayout.Placement aquarium=new HomeLayout.Placement("aquarium",4,4,0);
        RectF detail=new RectF(),overview=new RectF(),world=new RectF();
        scene.projectedBoundsFor(aquarium,detail);scene.resetOverview();scene.projectedBoundsFor(aquarium,overview);
        near(detail.width()/overview.width(),HomeScene.DETAIL_ZOOM,"furniture detail scale follows the declared close-up camera");
        HomeScene.boundsFor(aquarium,world);
        near(overview.width(),world.width()*fit(width,height),"only one viewport fit applies to furniture");
        for(float[] corner:new float[][]{{-.5f,-.5f},{9.5f,-.5f},{9.5f,9.5f},{-.5f,9.5f}}){
            PointF p=scene.projectedFloorPoint(corner[0],corner[1]);
            check(p.x>=0&&p.x<=width&&p.y>=0&&p.y<=height,"whole floor is visible in overview");
        }
        near(scene.cameraPanX(),0,"overview horizontal pan");near(scene.cameraPanY(),0,"overview vertical pan");
        for(float zoom:new float[]{1,HomeScene.DETAIL_ZOOM,2,2.5f})for(float panX:new float[]{-2000,0,2000})for(float panY:new float[]{-2000,0,2000}){
            scene.setCamera(zoom,panX,panY);
            float scale=fit(width,height)*zoom;
            float maxX=Math.max(0,(640*scale-width)*.5f),maxY=Math.max(0,(480*scale-height)*.5f);
            near(scene.cameraPanX(),Math.max(-maxX,Math.min(maxX,panX)),"pan cannot reveal space beyond world width");
            near(scene.cameraPanY(),Math.max(-maxY,Math.min(maxY,panY)),"pan cannot reveal space beyond world height");
            for(int x=0;x<10;x++)for(int y=0;y<10;y++){
                PointF p=scene.projectedFloorPoint(x,y);int[] cell=scene.cellAtCamera(p.x,p.y);
                check(cell!=null&&cell[0]==x&&cell[1]==y,"viewport projection and floor inverse agree for every tile");
            }
            for(HomeCatalog.Item item:HomeCatalog.ITEMS)for(int rotation=0;rotation<2;rotation++){
                HomeLayout.Placement object=new HomeLayout.Placement(item.id,4,4,rotation);
                HomeLayout layout=new HomeLayout(Collections.singletonList(object),0,0);
                RectF bounds=new RectF();scene.projectedBoundsFor(object,bounds);HomeScene.boundsFor(object,world);
                near(bounds.width(),world.width()*scale,"object width follows shared viewport camera");
                near(bounds.height(),world.height()*scale,"object height follows shared viewport camera");
                check(scene.targetAt(layout,bounds.centerX(),bounds.centerY(),0,0,"idle")==0,"visible object centre picks its placement after zoom and pan");
            }
        }
        scene.setCamera(2.2f,1000,-1000);float zoom=scene.cameraZoom();scene.setRoom("garden");
        near(scene.cameraZoom(),zoom,"switching rooms preserves zoom");
        scene.setViewport(width+50,height+70);near(scene.cameraZoom(),zoom,"changing viewport preserves zoom");
        scene.resetCamera();near(scene.cameraZoom(),HomeScene.DETAIL_ZOOM,"detail reset is explicit");
        near(scene.cameraPanX(),0,"detail reset centres horizontal pan");near(scene.cameraPanY(),0,"detail reset centres vertical pan");
        scene.setCamera(-10,100,100);near(scene.cameraZoom(),1,"zoom cannot shrink below whole room");
        scene.setCamera(20,100,100);near(scene.cameraZoom(),2.5f,"maximum zoom is bounded");
        scene.setCamera(Float.NaN,Float.POSITIVE_INFINITY,Float.NaN);near(scene.cameraZoom(),1,"nonfinite zoom uses safe overview");
        near(scene.cameraPanX(),0,"nonfinite horizontal pan is safe");near(scene.cameraPanY(),0,"nonfinite vertical pan is safe");
        check(scene.cellAtCamera(Float.NaN,30)==null,"nonfinite floor input is refused");
        check(scene.targetAt(HomeLayout.starter(),Float.NaN,20,0,0,"idle")==HomeScene.TARGET_OUTSIDE,"nonfinite object input is refused");
    }

    private static void renderingClip(float width,float height){
        HomeScene scene=new HomeScene();scene.setViewport(width,height);scene.setCamera(2.5f,0,0);
        int top=17;BufferedImage image=new BufferedImage((int)width+30,(int)height+50,BufferedImage.TYPE_INT_ARGB);
        scene.draw(new Canvas(image),top,HomeLayout.starter(),"","",null,null,null,Collections.emptyList(),0,true,3,3,"idle",-1,false);
        int painted=0;
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){
            int alpha=image.getRGB(x,y)>>>24;
            if(x>=width||y<top||y>=top+height)check(alpha==0,"native viewport clip contains the room drawing");
            else if(alpha>0)painted++;
        }
        check(painted>width*height*.35f,"room art fills a material part of the enlarged viewport");
    }

    public static void main(String[] args){
        for(float[] size:new float[][]{{390,440},{390,350},{390,550},{320,300},{600,320},{320,120}})cameraContract(size[0],size[1]);
        renderingClip(390,350);renderingClip(600,320);
        HomeScene scene=new HomeScene();scene.setViewport(Float.NaN,Float.POSITIVE_INFINITY);
        near(scene.viewportWidth(),HomeScene.WIDTH,"invalid width restores default");near(scene.viewportHeight(),HomeScene.HEIGHT,"invalid height restores default");
        scene.setViewport(0,-1);near(scene.viewportWidth(),HomeScene.WIDTH,"zero width is refused");near(scene.viewportHeight(),HomeScene.HEIGHT,"negative height is refused");
        System.out.println("PASS "+checks+" enlarged home viewport, inverse, picking, pan and render clip checks (native device review still required)");
    }
}
