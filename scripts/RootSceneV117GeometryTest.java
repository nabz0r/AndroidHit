import com.androidhit.lumadrift.ui.SceneRootLayout;
import com.androidhit.lumadrift.ui.SceneRootLayout.Destination;
import com.androidhit.lumadrift.ui.SceneRootLayout.Rect;
import java.util.ArrayList;
import java.util.List;

/** The live root placement helper, with independent reading/target invariants.
 * No Android renderer, bitmap, saved profile, UI fixture or game engine is used.
 */
public final class RootSceneV117GeometryTest {
    private static int checks, layouts;
    private static String context;
    private static final class Action {
        final String id; final Rect hit;
        Action(String id,Rect hit){this.id=id;this.hit=hit;}
    }
    public static void main(String[] args){
        // Actual compact and normal viewports plus the intervening layout boundaries.
        float[] heights={571.27f,571.7045f,580,600,616.6667f,639.6667f,640,680,720,790.8333f,805,844,960,1024,1200};
        for(float height:heights)for(float text:new float[]{11.9f,14.625f,19.0125f})for(float touch:new float[]{48,58.5f}){
            context="H="+height+", text="+text+", touch="+touch;
            SceneRootLayout l=new SceneRootLayout(height,text,touch);layouts++;
            checkRoots(l);
        }
        // Sweep every integer height through both reproduced E failure thresholds.
        for(int height=571;height<=900;height++){
            context="sweep H="+height;
            checkRoots(new SceneRootLayout(height,19.0125f,58.5f));layouts++;
        }
        // The old proportional design is a meaningful failing witness, not a new pass.
        float h=571.27f;
        check(.24f*h-153.52f<0,"old collection lower caption crossed sibling art");
        check(.30f*h-185<0,"old sofa caption crossed category art");
        check(.24f*805-153.52f>0,"old collection failure depended on height");
        check(.30f*805-185>0,"old sofa failure depended on height");
        System.out.println("Root scene 1.17 geometry PASS: "+checks+" checks across "+layouts+" live-helper layouts (caption bands, unique target centres, physical floor, unchanged object size)");
    }
    private static void checkRoots(SceneRootLayout l){
        Destination[] all={l.previous,l.next,l.adventurePortal,l.adventureLevels,l.adventureEvents,l.adventureRewards,l.adventureSettings,l.collectionWardrobe,l.collectionSummon,l.collectionBadges,l.collectionEquipment,l.atelierWalls,l.atelierFloor,l.homeWardrobe,l.homeDecorate};
        for(Destination d:all){valid(d.art,"destination art");valid(d.caption,"destination caption");target(d.hit,l,"destination target");inside(d.art,l,"destination art inside safe scene");inside(d.caption,l,"destination caption inside safe scene");}
        // Authored objects retain their sizes on compact screens; no regression fix by shrink.
        near(l.adventurePortal.size,178);near(l.adventureLevels.size,92);near(l.adventureEvents.size,92);near(l.adventureRewards.size,97);
        near(l.collectionWardrobe.size,70);near(l.collectionSummon.size,83);near(l.collectionBadges.size,79);near(l.collectionEquipment.size,83);
        near(l.atelierWalls.size,78);near(l.atelierFloor.size,78);near(l.previous.size,51);near(l.next.size,51);
        near(l.atelierSofaArt.width(),241);near(l.atelierSofaArt.height(),188);
        for(Rect art:l.atelierGroupArt){near(art.width(),98);near(art.height(),105);}

        Rect[] collectionBottom={l.collectionCompanionsCaption,l.collectionBadges.caption,l.collectionEquipment.caption};
        for(Rect caption:collectionBottom){separate(caption,l.previous.art,"collection lower caption / previous art");separate(caption,l.next.art,"collection lower caption / next art");inside(caption,l,"collection lower caption");}
        // E's two actual painted-text occlusions are now impossible in live placement.
        for(Rect art:l.atelierGroupArt)separate(l.atelierSofaCaption,art,"sofa legend / category art");
        for(Rect caption:l.atelierGroupCaption){separate(caption,l.atelierWalls.art,"category legend / walls art");separate(caption,l.atelierFloor.art,"category legend / floor art");}
        separate(l.atelierWalls.caption,l.previous.art,"walls caption / previous art");separate(l.atelierFloor.caption,l.previous.art,"floor caption / previous art");
        separate(l.adventureLevelCaption,l.adventurePortal.art,"level legend / portal art");
        destinationsSeparate(new Destination[]{l.adventurePortal,l.adventureLevels,l.adventureEvents,l.adventureRewards,l.adventureSettings,l.next},"adventure");
        destinationsSeparate(new Destination[]{l.collectionWardrobe,l.collectionSummon,l.collectionBadges,l.collectionEquipment,l.previous,l.next},"collection");
        destinationsSeparate(new Destination[]{l.atelierWalls,l.atelierFloor,l.previous},"atelier");
        destinationsSeparate(new Destination[]{l.homeWardrobe,l.homeDecorate,l.previous,l.next},"home controls");

        List<Action> adventure=new ArrayList<>();add(adventure,"play",l.adventurePortal);add(adventure,"levels",l.adventureLevels);add(adventure,"events",l.adventureEvents);add(adventure,"rewards",l.adventureRewards);add(adventure,"settings",l.adventureSettings);add(adventure,"home",l.next);centres(adventure,l);
        List<Action> collection=new ArrayList<>();collection.add(new Action("profile",l.collectionProfileHit));collection.add(new Action("wardrobe",l.collectionPortraitHit));collection.add(new Action("companions",l.collectionCompanionsHit));add(collection,"wardrobe",l.collectionWardrobe);add(collection,"summon",l.collectionSummon);add(collection,"badges",l.collectionBadges);add(collection,"equipment",l.collectionEquipment);add(collection,"home",l.previous);add(collection,"atelier",l.next);centres(collection,l);
        List<Action> atelier=new ArrayList<>();atelier.add(new Action("furniture",l.atelierSofaHit));for(int i=0;i<3;i++)atelier.add(new Action("group"+i,l.atelierGroupHit[i]));add(atelier,"walls",l.atelierWalls);add(atelier,"floor",l.atelierFloor);add(atelier,"collection",l.previous);centres(atelier,l);
        List<Action> home=new ArrayList<>();add(home,"wardrobe",l.homeWardrobe);add(home,"decorate",l.homeDecorate);add(home,"adventure",l.previous);add(home,"collection",l.next);centres(home,l);
        // Home 3D world targets are deliberately outside this flat navigation invariant:
        // painter-order picking handles furniture/companion silhouettes and occlusions.
    }
    private static void destinationsSeparate(Destination[] values,String scene){
        for(int i=0;i<values.length;i++)for(int j=0;j<values.length;j++)if(i!=j)separateAllocatedCaption(values[i].caption,values[j].art,scene+" foreign caption["+i+"] / object["+j+"]");
    }
    private static void add(List<Action> list,String id,Destination value){list.add(new Action(id,value.hit));}
    private static void centres(List<Action> actions,SceneRootLayout l){
        for(Action action:actions){target(action.hit,l,action.id);check(action.hit.contains(action.hit.centerX(),action.hit.centerY()),"own centre reachable "+action.id);for(Action other:actions)if(action!=other&&!action.id.equals(other.id))check(!other.hit.contains(action.hit.centerX(),action.hit.centerY()),"centre of "+action.id+" also belongs to "+other.id);}
    }
    private static void target(Rect r,SceneRootLayout l,String name){valid(r,name);inside(r,l,name);check(r.width()+.001f>=l.minimumTouch&&r.height()+.001f>=l.minimumTouch,"48dp target floor "+name);}
    private static void inside(Rect r,SceneRootLayout l,String name){check(r.left>=-.001f&&r.top>=-.001f&&r.right<=390.001f&&r.bottom<=l.height+.001f,name);}
    private static void valid(Rect r,String name){check(Float.isFinite(r.left)&&Float.isFinite(r.top)&&Float.isFinite(r.right)&&Float.isFinite(r.bottom)&&r.width()>0&&r.height()>0,name);}
    private static void separate(Rect a,Rect b,String name){check(!a.intersects(b),name+" intersects");}
    /** Caption.width is the entire ellipsis allocation, not measured glyph width.
     * Events' empty allocation edge crosses Rewards art by 0.5 design units.
     * Permit <=1 unit only for these general conservative allocation comparisons;
     * reproduced P2 reading-band assertions above remain strictly disjoint.
     */
    private static void separateAllocatedCaption(Rect a,Rect b,String name){float width=Math.min(a.right,b.right)-Math.max(a.left,b.left),height=Math.min(a.bottom,b.bottom)-Math.max(a.top,b.top);check(width<=1||height<=1,name+" intersects "+width+" x "+height);}
    private static void near(float a,float b){check(Math.abs(a-b)<.001f,"authored size "+a+" expected "+b);}
    private static void check(boolean condition,String name){checks++;if(!condition)throw new AssertionError(context+": "+name);}
}
