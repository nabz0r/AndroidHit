import com.androidhit.lumadrift.ui.PlaySceneLayout;
import com.androidhit.lumadrift.ui.PlaySceneLayout.Rect;
import com.androidhit.lumadrift.ui.CompanionBattleLayout;
import java.util.ArrayList;
import java.util.List;

/** Independent spatial/readability contracts against the exact live helper.
 * This does not duplicate layout formulas, import Android or simulate gameplay.
 * Measured text heights are inputs: real glyph measurement remains native QA.
 */
public final class PlaySceneV117GeometryTest {
    private static int checks,layouts;
    private static String context;
    public static void main(String[] args){
        float[] heights={320,433,480,550,571.27f,571.7045f,600,640,680,720,790.8333f,805,844,960,1200};
        for(int widthDp:new int[]{240,280,320,360,390,411,480}){
            float touch=48f*390/widthDp;
            for(float fontScale:new float[]{1,1.3f})for(float height:heights)for(float measuredText:new float[]{14,28,42,46,70,120})for(boolean relay:new boolean[]{false,true}){
                float minimumText=12f*390/widthDp*fontScale;
                context="widthDp="+widthDp+", H="+height+", font="+fontScale+", measured="+measuredText+", relay="+relay;
                PlaySceneLayout l=new PlaySceneLayout(height,relay,touch,measuredText,minimumText);layouts++;
                checkLayout(l,widthDp,measuredText);
            }
        }
        // Test branch boundaries by observing the helper, without restating its formula.
        PlaySceneLayout previous=new PlaySceneLayout(320,false,58.5f,46,19.0125f);
        boolean seenCompact=previous.compactActions,seenGrid=false;
        for(int height=321;height<=1200;height++){
            PlaySceneLayout next=new PlaySceneLayout(height,false,58.5f,46,19.0125f);
            context="height sweep="+height;checkLayout(next,320,46);layouts++;
            check(next.boardWidth==previous.boardWidth,"board never shrinks with window height");
            check(next.boardY==previous.boardY,"board never jumps at power-tray threshold");
            check(!seenGrid||!next.compactActions,"a taller viewport does not return to compact columns");
            if(!next.compactActions)seenGrid=true;previous=next;
        }
        check(seenCompact&&seenGrid,"both real compact-column and full-grid paths exercised");
        PlaySceneLayout small=new PlaySceneLayout(571.27f,false,58.5f,42,19.0125f),large=new PlaySceneLayout(571.27f,false,58.5f,88,19.0125f);
        check(large.actionsY>=small.actionsY+46-.001f,"larger measured copy pushes controls after its actual bottom");
        check(large.contentHeight>=small.contentHeight+46-.001f,"larger copy remains scroll-reachable");
        checkCompanionInsets();
        // Concrete G witness: it must fail independent non-intersection/containment rules.
        Rect oldBudget=new Rect(24,131,366,156),oldUpInk=new Rect(171.3f,140.3f,218.7f,187.7f);
        Rect oldHelp=new Rect(26,535,364,577),oldDownInk=new Rect(171.3f,496.3f,218.7f,543.7f);
        check(oldBudget.intersects(oldUpInk),"G witness really occluded its budget");
        check(oldHelp.intersects(oldDownInk),"G witness really occluded its instruction");
        check(new Rect(-8,317,42,367).left<0,"G witness really clipped a side target");
        System.out.println("Play scene 1.17/1.19 geometry PASS: "+checks+" checks across "+layouts+" live-helper layouts; gate/cell separation, physical48dp gate/action targets, companion dock separation without board shrinkage, reading bands and reachable final controls. No native glyph/gameplay claim.");
    }
    private static void checkCompanionInsets(){
        // Compose the actual two geometry helpers. This catches a dropped top inset, a dock-sized
        // board reduction, a gate left in its previous location, and controls no longer scrollable.
        for(int widthDp:new int[]{240,280,320,360,390,411,480})for(float fontScale:new float[]{1,1.3f})
            for(float height:new float[]{320,433,571.27f,790.8333f,960,1200})for(boolean relay:new boolean[]{false,true}){
                float touch=48f*390/widthDp,minimumText=12f*390/widthDp*fontScale;
                float dockHeight=CompanionBattleLayout.heightFor(touch);
                CompanionBattleLayout dock=new CompanionBattleLayout(16,80,374,80+dockHeight,touch);
                context="companion dock widthDp="+widthDp+", H="+height+", font="+fontScale+", relay="+relay;
                PlaySceneLayout base=new PlaySceneLayout(height,relay,touch,46,minimumText);
                PlaySceneLayout scene=new PlaySceneLayout(height,relay,touch,46,minimumText,dockHeight);layouts++;
                checkLayout(scene,widthDp,46);
                check(scene.boardWidth==base.boardWidth&&scene.boardX==base.boardX,"team dock never reduces board/cell geometry");
                close(scene.boardY-base.boardY,dockHeight,"board follows full actual dock height");
                close(scene.budgetRect.top-base.budgetRect.top,dockHeight,"budget follows actual dock");
                close(scene.feedbackTop-base.feedbackTop,dockHeight,"feedback follows actual dock");
                Rect occupied=new Rect(dock.bounds.left,dock.bounds.top,dock.bounds.right,dock.bounds.bottom);
                separate(occupied,scene.boardRect,"dock / board");separate(occupied,scene.budgetRect,"dock / budget");
                separate(occupied,scene.feedbackRect,"dock / feedback");
                for(int i=0;i<4;i++){
                    close(scene.gateTargets[i].top-base.gateTargets[i].top,dockHeight,"each gate target follows dock");
                    close(scene.gateVisualBounds[i].top-base.gateVisualBounds[i].top,dockHeight,"each gate's ink follows dock");
                    separate(occupied,scene.gateTargets[i],"dock / gate target");separate(occupied,scene.gateVisualBounds[i],"dock / gate ink");
                }
                for(CompanionBattleLayout.Rect target:dock.actions){
                    Rect r=new Rect(target.left,target.top,target.right,target.bottom);physical(r,widthDp,"companion skill");inside(r,scene,"companion skill");
                }
                Rect combo=new Rect(dock.combo.left,dock.combo.top,dock.combo.right,dock.combo.bottom);physical(combo,widthDp,"companion combo");inside(combo,scene,"companion combo");
                for(Rect action:new Rect[]{scene.undoRect,scene.restartRect,scene.powersRect})separate(occupied,action,"dock / puzzle action");
            }
    }
    private static void close(float actual,float expected,String label){check(Math.abs(actual-expected)<.001f,label);}
    private static void checkLayout(PlaySceneLayout l,int widthDp,float measured){
        valid(l.boardRect,"board");valid(l.budgetRect,"budget");valid(l.feedbackRect,"feedback");
        check(l.boardWidth>=180,"authored board stays useful across supported portrait widths");
        check(l.feedbackHeight+.001f>=measured,"measured copy height preserved");
        check(l.feedbackRect.top>=132,"feedback follows the header");
        check(l.feedbackRect.bottom+8<=l.actionsY+.001f,"actions do not cover actual copy");
        for(int i=0;i<4;i++){
            Rect target=l.gateTargets[i],ink=l.gateVisualBounds[i];
            valid(target,"gate target");valid(ink,"gate ink");inside(target,l,"gate target");inside(ink,l,"gate ink");
            check(target.left>=8-.001f&&target.right<=382.001f,"gate target keeps side margin");
            check(ink.left>=8-.001f&&ink.right<=382.001f,"Trail/preview ink keeps side margin");
            physical(target,widthDp,"gate");
            separate(target,l.boardRect,"gate target / edge cells");
            separate(target,l.budgetRect,"gate target / budget");separate(ink,l.budgetRect,"gate ink / budget");
            separate(target,l.feedbackRect,"gate target / feedback");separate(ink,l.feedbackRect,"gate ink / feedback");
            check(ink.top>=132-.001f,"all gate ink clears the progress bottom by8");
        }
        check(l.feedbackRect.top-l.gateTargets[2].bottom>=8-.001f,"feedback has its own lower rail");
        List<Rect> actions=new ArrayList<>();actions.add(l.undoRect);actions.add(l.restartRect);actions.add(l.powersRect);
        if(!l.compactActions)for(Rect r:l.powerGrid)actions.add(r);
        for(int i=0;i<actions.size();i++){
            Rect a=actions.get(i);valid(a,"action");inside(a,l,"action");physical(a,widthDp,"action");
            separate(a,l.feedbackRect,"action / feedback");separate(a,l.boardRect,"action / board");
            for(Rect gate:l.gateTargets)separate(a,gate,"action / gate");
            for(int j=i+1;j<actions.size();j++)separate(a,actions.get(j),"different action targets");
            // An element taller than the viewport cannot be fully revealed at once;
            // tested native-sized viewports exceed every control's height.
            float maxScroll=Math.max(0,l.contentHeight-l.viewportHeight);
            float scroll=Math.max(0,Math.min(maxScroll,a.bottom-l.viewportHeight));
            check(a.top-scroll>=-.001f&&a.bottom-scroll<=l.viewportHeight+.001f,"full target reachable by real content scroll");
        }
        check(l.contentHeight>=l.viewportHeight&&Float.isFinite(l.contentHeight),"finite complete scroll content");
        check(l.contentHeight-l.powersRect.bottom>=16-.001f,"last action has final bottom padding");
        if(l.compactActions){
            check(l.undoRect.centerY()==l.restartRect.centerY()&&l.undoRect.centerY()==l.powersRect.centerY(),"compact object/caption columns share one row");
            check(l.undoRect.right+12<=l.restartRect.left+.001f&&l.restartRect.right+12<=l.powersRect.left+.001f,"compact columns keep12-unit gaps");
            check(l.actionHeight-46>=l.feedbackHeight+4-.001f,"42-unit art and its two measured caption lines fit completely");
        }else{
            check(l.contentHeight<=l.viewportHeight+.001f,"full grid is selected only when it actually fits");
            check(l.undoRect.centerY()==l.restartRect.centerY(),"wide action pair shares its own row");
        }
    }
    private static void physical(Rect r,int widthDp,String name){check(r.width()*widthDp/390+.001f>=48&&r.height()*widthDp/390+.001f>=48,name+" has a real48dp target");}
    private static void valid(Rect r,String name){check(Float.isFinite(r.left)&&Float.isFinite(r.top)&&Float.isFinite(r.right)&&Float.isFinite(r.bottom)&&r.width()>0&&r.height()>0,name+" is finite and nonempty");}
    private static void inside(Rect r,PlaySceneLayout l,String name){check(r.left>=-.001f&&r.right<=390.001f&&r.top>=-.001f&&r.bottom<=l.contentHeight+.001f,name+" is inside complete scene");}
    private static void separate(Rect a,Rect b,String name){check(!a.intersects(b),name+" overlaps");}
    private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(context+": "+message);}
}
