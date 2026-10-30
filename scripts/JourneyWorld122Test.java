import com.androidhit.lumadrift.ui.JourneyCamera122;
import com.androidhit.lumadrift.ui.JourneyWorld122;
import com.androidhit.lumadrift.ui.JourneyWorld122.Point;
import com.androidhit.lumadrift.ui.JourneyWorld122.Rect;
import com.androidhit.lumadrift.ui.JourneyWorld122.Travel;
import com.androidhit.lumadrift.ui.JourneyWorld122.TravelFrame;
import java.util.Random;

/** Behavioural journeys: every identity, continuous route changes, independent camera and minimap. */
public final class JourneyWorld122Test {
    private static int checks;
    private static void check(boolean value,String reason){checks++;if(!value)throw new AssertionError(reason);}
    private static void close(float a,float b,String reason){check(Math.abs(a-b)<.012f,reason+" "+a+" vs "+b);}
    private static void point(Point a,Point b,String reason){close(a.x,b.x,reason+" x");close(a.y,b.y,reason+" y");}
    private static void bounded(JourneyCamera122 camera){JourneyCamera122.Frame frame=camera.frame();check(frame.left>=-.001f&&frame.top>=-.001f&&frame.right<=JourneyWorld122.WIDTH+.001f&&frame.bottom<=JourneyWorld122.HEIGHT+.001f,"camera remains in world");check(frame.zoom>=camera.minZoom()-.001f&&frame.zoom<=camera.maxZoom()+.001f,"zoom bounded");}
    private static void rejects(Runnable command){boolean failed=false;try{command.run();}catch(IllegalArgumentException expected){failed=true;}check(failed,"invalid presentation state rejected");}

    public static void main(String[] args){
        Rect mini=new Rect(12,24,108,192);
        for(int total:new int[]{1,40,100,101,240,1000}){
            for(int level=1;level<=total;level++){
                Point node=JourneyWorld122.node(level);int sector=JourneyWorld122.sector(level);
                check(node.x>JourneyWorld122.NODE_RADIUS&&node.x<JourneyWorld122.WIDTH-JourneyWorld122.NODE_RADIUS&&node.y>JourneyWorld122.NODE_RADIUS&&node.y<JourneyWorld122.HEIGHT-JourneyWorld122.NODE_RADIUS,"node fits sector");
                check(JourneyWorld122.hitLevel(node.x,node.y,sector,total,32)==level,"exact catalogue identity hit "+level);
                check(JourneyWorld122.firstInSector(sector,total)<=level&&JourneyWorld122.lastInSector(sector,total)>=level,"sector membership");
                point(JourneyWorld122.miniToWorld(JourneyWorld122.miniPoint(level,mini).x,JourneyWorld122.miniPoint(level,mini).y,mini),node,"minimap identity roundtrip");
                if(level<total&&JourneyWorld122.sector(level+1)==sector){
                    point(JourneyWorld122.edge(level,level+1,0),node,"edge begins at node");
                    point(JourneyWorld122.edge(level,level+1,1),JourneyWorld122.node(level+1),"edge ends at node");
                    for(int sample=0;sample<=20;sample++){
                        float t=sample/20f;Point along=JourneyWorld122.edge(level,level+1,t);
                        point(along,JourneyWorld122.edge(level+1,level,1-t),"reverse follows identical geometry");
                        check(along.x>=0&&along.x<=JourneyWorld122.WIDTH&&along.y>=0&&along.y<=JourneyWorld122.HEIGHT,"edge stays inside world");
                    }
                }
            }
        }
        check(JourneyWorld122.lastInSector(2,240)==240,"final Remix sector terminates at 240");
        check(JourneyWorld122.hitLevel(-100,-100,0,1000,30)==-1,"empty terrain is not a level");
        point(JourneyWorld122.miniToWorld(-900,900,mini),new Point(0,JourneyWorld122.HEIGHT),"minimap clamps to sector");
        for(int start=1;start<=1000;start+=7){
            int finish=Math.min(JourneyWorld122.sectorEnd(start,1000),start+9);
            Travel travel=Travel.start(start,finish,1000,1000,false);
            check(travel.durationMillis()<=1500,"travel never imposes more than 1500 ms");
            float last=start;
            for(long time=1000;time<=2600;time+=17){
                TravelFrame frame=travel.sample(time);
                check(frame.routePosition>=last-.0001f&&frame.routePosition<=finish,"forward journey monotonic");
                point(new Point(frame.x,frame.y),JourneyWorld122.routePoint(frame.routePosition,1000),"avatar remains on real path");last=frame.routePosition;
            }
            TravelFrame end=travel.sample(2600);check(!end.moving&&end.levelReached==finish,"arrival settles at exact selected level");point(new Point(end.x,end.y),JourneyWorld122.node(finish),"final avatar node");
            Travel back=Travel.start(finish,start,1000,3000,false);float prior=finish;
            for(long time=3000;time<=4600;time+=31){TravelFrame frame=back.sample(time);check(frame.routePosition<=prior+.0001f&&frame.routePosition>=start,"reverse journey monotonic");prior=frame.routePosition;}
        }
        Travel original=Travel.start(21,29,1000,100,false);TravelFrame before=original.sample(700);
        Travel interrupted=original.retarget(23,700,false);TravelFrame after=interrupted.sample(700);
        close(before.x,after.x,"retarget keeps x");close(before.y,after.y,"retarget keeps y");
        TravelFrame later=interrupted.sample(900),regressed=interrupted.sample(800);close(later.routePosition,regressed.routePosition,"clock regression cannot reverse travel");
        TravelFrame longJump=Travel.start(1,99,1000,0,false).sample(0);check(longJump.abbreviated&&!longJump.moving&&longJump.levelReached==99,"long trip abbreviated immediately");
        TravelFrame crossing=Travel.start(100,101,1000,0,false).sample(0);check(crossing.abbreviated&&!crossing.moving&&crossing.sector==1,"sector crossing never invents local edge");
        TravelFrame still=Travel.start(22,29,1000,0,true).sample(0);check(!still.moving&&still.levelReached==29,"reduced motion arrives immediately");

        Random random=new Random(122);
        for(float[] viewport:new float[][]{{320,220},{390,480},{390,900},{720,220},{1280,720},{240,140},{2000,2000}}){
            JourneyCamera122 camera=new JourneyCamera122();camera.configure(viewport[0],viewport[1]);bounded(camera);
            camera.resumeFollow(480,840);JourneyCamera122.Frame initial=camera.frame();
            Point anchor=initial.screenToWorld(viewport[0]*.45f,viewport[1]*.48f);
            camera.setZoomAt(camera.maxZoom(),viewport[0]*.45f,viewport[1]*.48f);bounded(camera);
            point(camera.screenToWorld(viewport[0]*.45f,viewport[1]*.48f),anchor,"zoom preserves touched world point away from clamps");
            camera.panBy(73,-47);JourneyCamera122.Frame dragged=camera.frame();camera.follow(110,140);JourneyCamera122.Frame unchanged=camera.frame();
            close(dragged.left,unchanged.left,"manual pan interrupts follow x");close(dragged.top,unchanged.top,"manual pan interrupts follow y");check(!camera.isFollowing(),"follow remains interrupted");
            camera.resumeFollow(849,1490);check(camera.isFollowing(),"explicit recenter resumes follow");bounded(camera);
            for(int step=0;step<500;step++){
                if(step%3==0)camera.panBy(random.nextFloat()*900-450,random.nextFloat()*1200-600);
                else if(step%3==1)camera.setZoomAt(random.nextFloat()*3,random.nextFloat()*viewport[0],random.nextFloat()*viewport[1]);
                else camera.centerOn(random.nextFloat()*1500-250,random.nextFloat()*2500-400);
                bounded(camera);JourneyCamera122.Frame frame=camera.frame();
                Point world=new Point(random.nextFloat()*960,random.nextFloat()*1680),screen=frame.worldToScreen(world.x,world.y);
                point(frame.screenToWorld(screen.x,screen.y),world,"camera reversible transform");
                Rect projected=JourneyWorld122.miniViewport(frame.bounds(),mini);check(projected.left>=mini.left-.001&&projected.right<=mini.right+.001&&projected.top>=mini.top-.001&&projected.bottom<=mini.bottom+.001,"minimap viewport remains bounded");
            }
        }
        rejects(()->JourneyWorld122.node(0));rejects(()->JourneyWorld122.node(1001));rejects(()->JourneyWorld122.edge(100,101,.5f));rejects(()->JourneyWorld122.edge(1,3,.5f));
        rejects(()->JourneyWorld122.firstInSector(3,240));rejects(()->Travel.start(1,241,240,0,false));rejects(()->JourneyWorld122.routePoint(Float.NaN,1000));
        JourneyCamera122 invalid=new JourneyCamera122();rejects(()->invalid.configure(0,500));rejects(()->invalid.panBy(Float.NaN,3));rejects(()->invalid.setZoomAt(Float.POSITIVE_INFINITY,0,0));
        System.out.println("JourneyWorld122: "+checks+" checks passed; catalogue identities, exact edges, reversible travel, interruption, bounded camera and minimap.");
    }
}
