import com.androidhit.lumadrift.ui.Adventure119Layout;
import com.androidhit.lumadrift.ui.Adventure119Layout.Rect;

/** Checks the observed compact regression without duplicating the helper's layout equations. */
public final class AdventureV119GeometryTest {
    private static int checks,layouts;
    private static String context="";
    public static void main(String[] args){
        for(int widthDp:new int[]{240,280,320,360,390,411,480})
            for(float height:new float[]{200,240,320,433,480,550,610,650,720,790.8333f,960,1200}){
                float touch=48f*390f/widthDp;
                context="widthDp="+widthDp+", viewport="+height;
                Adventure119Layout l=new Adventure119Layout(height,touch);layouts++;
                check(l.sceneHeight>=650&&l.sceneHeight>=height,"complete scene preserves its minimum usable height");
                check(l.contentHeight>=l.sceneHeight&&Float.isFinite(l.contentHeight),"complete content is finite and includes scene");
                Rect[] targets={l.continueRect,l.cards[0],l.cards[1],l.cards[2],l.rewardsRect};
                Rect fixedNavigation=new Rect(10,height-77,380,height-9);
                for(int n=0;n<targets.length;n++){
                    Rect target=targets[n];
                    check(target.left>=0&&target.right<=390&&target.top>=0&&target.bottom<=l.contentHeight,"whole action lies inside complete scene");
                    check(target.width()*widthDp/390f>=47.999f&&target.height()*widthDp/390f>=47.999f,"action preserves physical48dp target");
                    for(int m=n+1;m<targets.length;m++)check(!target.intersects(targets[m]),"different destination actions never overlap");
                    // Each full action can be brought above the pinned navigation without clipping its top.
                    float scroll=Math.max(0,Math.min(l.maximumScroll(),target.bottom-(height-Adventure119Layout.NAVIGATION_RESERVE-12)));
                    Rect visible=shift(target,scroll);
                    check(visible.top>=-.001f&&visible.bottom<=height+.001f,"full target is reachable by actual content scroll");
                    check(!visible.intersects(fixedNavigation),"reachable action stays clear of pinned navigation");
                    check(!shift(target,l.maximumScroll()).intersects(fixedNavigation),"no action sits under navigation at maximum scroll");
                }
                check(l.rewardsRect.top>=l.cards[0].bottom+11.999f,"rewards has its own reading/action band after destination cards");
                check(l.rewardsBaseline>l.rewardsRect.top&&l.rewardsBaseline<l.rewardsRect.bottom,"reward caption baseline stays inside its action");
                check(l.contentHeight-l.rewardsRect.bottom>=Adventure119Layout.NAVIGATION_RESERVE+Adventure119Layout.FINAL_GAP-.001f,"last target keeps navigation reserve plus final breathing room");
            }
        // Native B compact FR evidence: destination and Rewards actually occupied these pixel ranges.
        // Source: candidate-b-compact-fr-en/native/compact-fr-adventure-accessibility.json.
        Rect oldLevels=new Rect(33,928,236,1070),oldRewards=new Rect(92,992,627,1076);
        check(oldLevels.intersects(oldRewards),"the independent no-intersection rule catches the recorded B witness");
        Adventure119Layout fallback=new Adventure119Layout(Float.NaN,Float.POSITIVE_INFINITY);
        check(Float.isFinite(fallback.contentHeight)&&fallback.minimumTouch>=48,"invalid measurements use finite physical defaults");
        System.out.println("Adventure 1.19 geometry PASS: "+checks+" checks across "+layouts+" layouts; distinct CTA/cards/rewards, physical48dp targets, full scroll reachability and pinned-navigation clearance. No native glyph or rendering claim.");
    }
    private static Rect shift(Rect rect,float scroll){return new Rect(rect.left,rect.top-scroll,rect.right,rect.bottom-scroll);}
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(context+": "+message);}
}
