import com.androidhit.lumadrift.data.*;
import java.nio.file.*;
import java.util.*;

/** Actual home store and pure layout contract, including Android memory-before-disk fault recovery. */
public final class HomeV113Test {
    private static int checks,funding;
    private static void check(boolean value,String note){checks++;if(!value)throw new AssertionError(note);}
    private static boolean refused(Runnable task){try{task.run();return false;}catch(GameStore.LocalSaveException expected){return true;}}
    private static GameStore store(RewardsCosmeticsV17Test.Ctx c){return new GameStore(c,null,new RewardsCosmeticsV17Test.Clock("2026-10-28"));}
    private static void fund(GameStore s,int cost){while(s.coins()<cost)check(s.grantMilestone("home-test:"+funding++,Math.min(500,cost-s.coins()))>0,"earned funding");}
    private static void catalogue(){
        check(HomeCatalog.ITEMS.size()==12,"twelve original objects");Set<String> ids=new HashSet<>();int free=0,usable=0;
        for(HomeCatalog.Item item:HomeCatalog.ITEMS){check(ids.add(item.id),"unique furniture id");check(item.width>0&&item.depth>0&&item.width<=2&&item.depth<=2,"small valid footprint");if(item.starter())free++;if(!item.interaction.isEmpty())usable++;}
        check(free>=4&&usable==12,"several usable welcome objects");
        for(List<HomeCatalog.Surface> surfaces:Arrays.asList(HomeCatalog.FLOORS,HomeCatalog.WALLPAPERS)){int starters=0,paid=0;for(HomeCatalog.Surface surface:surfaces){check(ids.add(surface.id),"surface ids disjoint");if(surface.starter())starters++;else paid++;}check(starters==2&&paid==2,"two starter and two earned finishes");}
        for(String language:new String[]{"en","fr","de","es","pt","it"}){
            for(String key:HomeStrings.keys()){String text=HomeStrings.get(language,key);check(text!=null&&!text.isBlank()&&!text.equals(key),"complete home translation "+language+" "+key);}
            for(HomeCatalog.Item item:HomeCatalog.ITEMS){check(!item.name(language).isBlank()&&!item.description(language).isBlank(),"localized furniture");}
            check(HomeStrings.get(language,"goal_progress",12,240).contains("12")&&HomeStrings.get(language,"goal_progress",12,240).contains("240"),"localized numeric formats");
        }
        check(HomeStrings.get("fr-CA","title").equals(HomeStrings.get("fr","title")),"regional language fallback");
    }
    private static void layouts(){
        HomeLayout initial=HomeLayout.starter();check(initial.placements().size()==5,"five placed starters");check(initial.encode().equals(HomeLayout.decode(initial.encode()).encode()),"exact stable roundtrip");
        check(initial.tryPlace("sofa",5,5,0)==null&&initial.tryPlace("sofa",5,4,1)!=null,"rotation respects full footprint");
        check(initial.tryPlace("sofa",0,0,0)==null,"solid collision rejected");check(initial.tryPlace("table",3,3,0)==null,"companion cannot be covered");check(initial.tryPlace("table",2,2,0)!=null,"furniture may sit over rug");
        check(initial.tryPlace("rug",0,0,0)!=null,"rug may be slid under furniture");check(initial.tryPlace("lamp",2,2,-1)==null&&initial.tryPlace("lamp",2,2,2)==null,"invalid rotations rejected");
        check(initial.tryPlace(null,2,2,0)==null&&initial.tryPlace("missing",2,2,0)==null,"invalid ids rejected");
        check(initial.tryMove(0,0)==null&&initial.tryMove(-1,3)==null&&initial.tryMove(6,3)==null,"blocked and invalid companion moves rejected");
        List<HomeLayout.Cell> path=initial.pathTo(2,0);check(!path.isEmpty(),"reachable path exists");HomeLayout.Cell previous=new HomeLayout.Cell(initial.companionX,initial.companionY);
        for(HomeLayout.Cell cell:path){check(Math.abs(cell.x-previous.x)+Math.abs(cell.y-previous.y)==1&&!initial.isBlocked(cell.x,cell.y),"path uses clear orthogonal steps");previous=cell;}
        check(previous.x==2&&previous.y==0,"path includes destination");check(initial.encode().equals(HomeLayout.starter().encode()),"operations leave original immutable");
        List<HomeLayout.Placement> wall=new ArrayList<>();String[] objects={"lamp","plant","table","bookshelf","plush","stool"};for(int x=0;x<6;x++)wall.add(new HomeLayout.Placement(objects[x],x,2,0));
        HomeLayout separated=new HomeLayout(wall,3,3);check(separated.pathTo(3,1).isEmpty()&&separated.tryMove(3,1)==null,"BFS forbids walking through a solid wall");
        boolean immutable=false;try{initial.placements().clear();}catch(UnsupportedOperationException expected){immutable=true;}check(immutable,"placement view immutable");
        for(String invalid:new String[]{"","1|3,3|bed,0,0,2;","1|3,3|lamp,3,3,0;","1|6,3|","1|3,3|lamp,2,2,0;plant,2,2,0;","1|3,3|lamp,2,2,0;lamp,4,4,0;","1|3,3|missing,1,1,0;"})check(HomeLayout.decode(invalid)==null,"malformed layout rejected atomically");
    }
    private static void storeActions(){
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);String initial=s.homeLayout().encode();
        check(s.coins()==180&&!c.save().contains("home_layout_v113"),"starter room costs nothing and read needs no migration write");
        check(s.ownsHomeItem("bed")&&s.ownsHomeItem("floor_cloud")&&!s.ownsHomeItem("sofa"),"starter and paid ownership separated");
        check(!s.placeHomeItem("sofa",2,0,0)&&!s.purchaseHomeItem("unknown")&&!s.purchaseHomeItem(null),"invalid and unowned actions rejected");
        check(!s.setHomeFloor("wallpaper_sage")&&!s.setHomeWallpaper("floor_oak")&&!s.setHomeWallpaper("wallpaper_dusk"),"surface kind and ownership guarded");
        check(!s.setHomeGoal("lamp")&&!s.setHomeGoal("wallpaper_dusk")&&!s.setHomeGoal(null),"wish selects only unowned furniture");
        check(s.setHomeGoal("sofa")&&s.homeGoal().equals("sofa"),"furniture wish saved");check(!s.purchaseHomeItem("sofa")&&s.homeGoal().equals("sofa")&&s.coins()==180,"unfunded purchase retains wish and balance");
        fund(s,240);int before=s.coins();check(s.purchaseHomeItem("sofa")&&s.coins()==before-240&&s.ownsHomeItem("sofa"),"purchase exact earned coin debit");
        check(s.homeGoal().isEmpty()&&s.homeLayout().encode().equals(initial),"buy clears wish and never silently places");check(!s.purchaseHomeItem("sofa")&&s.coins()==before-240,"duplicate purchase idempotent");
        check(s.placeHomeItem("sofa",2,0,0)&&s.homeLayout().placement("sofa").x==2,"owned item places");
        String placed=s.homeLayout().encode();check(!s.placeHomeItem("sofa",0,0,0)&&!s.placeHomeItem("sofa",3,3,0)&&s.homeLayout().encode().equals(placed),"refused placements preserve exact state");
        check(s.placeHomeItem("sofa",5,2,1)&&s.homeLayout().placement("sofa").rotation==1,"moving rotation persisted");check(s.moveHomeCompanion(1,1),"companion walks to free tile");
        check(s.setHomeWallpaper("wallpaper_sage")&&s.setHomeFloor("floor_cloud"),"free finishes equip");
        fund(s,180);check(s.purchaseHomeItem("wallpaper_dusk")&&s.setHomeWallpaper("wallpaper_dusk"),"earned finish purchase and equip");
        GameStore cold=store(c.reboot());check(cold.homeWallpaper().equals("wallpaper_dusk")&&cold.homeFloor().equals("floor_cloud")&&cold.homeLayout().encode().equals(s.homeLayout().encode()),"exact house survives cold restart");
        check(cold.removeHomeItem("sofa")&&cold.ownsHomeItem("sofa")&&cold.homeLayout().placement("sofa")==null,"store away retains permanent ownership");
    }
    private static void failures(){
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore first=store(c),second=store(c);fund(first,240);int coins=first.coins();check(first.setHomeGoal("sofa"),"failure wish setup");
            c.save().fail(memory);check(refused(()->first.purchaseHomeItem("sofa")),"failed purchase surfaced "+memory);
            check(c.save().disk.get("coins").equals(coins)&&!c.save().disk.containsKey("home_owned_v113_sofa")&&c.save().disk.get("home_goal_v113").equals("sofa"),"failed purchase leaves complete old disk state "+memory);
            c.save().fail(memory);check(refused(()->second.ownsHomeItem("sofa")),"ownership read flushes refused transaction "+memory);
            check(!second.purchaseHomeItem("sofa")&&second.ownsHomeItem("sofa")&&second.coins()==coins-240&&second.homeGoal().isEmpty(),"second handle persists one purchase "+memory);
            String initial=first.homeLayout().encode();c.save().fail(memory);check(refused(()->first.placeHomeItem("sofa",2,0,0)),"failed placement surfaced "+memory);check(!c.save().disk.containsKey("home_layout_v113"),"refused placement preserves durable starter fallback "+memory);
            c.save().fail(memory);check(refused(second::homeLayout),"layout read guarded during failure "+memory);second.retryPendingLocalSave();check(second.homeLayout().placement("sofa")!=null&&!second.homeLayout().encode().equals(initial),"placement exact retry "+memory);
            c.save().fail(memory);check(refused(()->first.moveHomeCompanion(1,1)),"failed companion move surfaced "+memory);second.retryPendingLocalSave();check(second.homeLayout().companionX==1&&second.homeLayout().companionY==1,"companion exact retry "+memory);
            c.save().fail(memory);check(refused(()->first.setHomeWallpaper("wallpaper_sage")),"failed appearance surfaced "+memory);second.retryPendingLocalSave();check(second.homeWallpaper().equals("wallpaper_sage"),"appearance exact retry "+memory);
            c.save().fail(memory);check(refused(()->first.setHomeGoal("table")),"failed goal surfaced "+memory);second.retryPendingLocalSave();check(second.homeGoal().equals("table"),"goal exact retry "+memory);
            c.save().fail(memory);check(refused(()->first.removeHomeItem("sofa")),"failed removal surfaced "+memory);second.retryPendingLocalSave();check(second.homeLayout().placement("sofa")==null&&second.ownsHomeItem("sofa"),"removal exact retry retains ownership "+memory);
            for(int retry=0;retry<4;retry++)second.retryPendingLocalSave();GameStore cold=store(c.reboot());check(cold.coins()==coins-240&&cold.ownsHomeItem("sofa")&&cold.homeWallpaper().equals("wallpaper_sage")&&cold.homeGoal().equals("table")&&cold.homeLayout().encode().equals(second.homeLayout().encode()),"all retry state durable with no duplicate debit "+memory);
            GameStore unrelated=store(new RewardsCosmeticsV17Test.Ctx());check(unrelated.coins()==180&&!unrelated.ownsHomeItem("sofa"),"pending writes do not cross profiles "+memory);
        }
    }
    private static void coldFailureAndLegacy(){
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);fund(s,240);int coins=s.coins();c.save().fail(memory);check(refused(()->s.purchaseHomeItem("sofa")),"cold purchase failure setup");
            GameStore cold=store(c.reboot());check(cold.coins()==coins&&!cold.ownsHomeItem("sofa"),"process loss restores complete previous purchase state");check(cold.purchaseHomeItem("sofa")&&cold.coins()==coins-240,"fresh request after process loss grants once");
        }
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);s.completeLevel(1,3);check(s.purchaseCharacter("miko")&&s.equipCharacter("miko"),"legacy earned companion setup");s.equipAvatarStyle("ocean");String character=s.equippedCharacter(),skin=s.equippedSkin(),style=s.avatarStyle();int coins=s.coins(),stars=s.stars(1),unlocked=s.unlockedLevel();
        Map<String,Object> previous=new HashMap<>(c.save().disk);RewardsCosmeticsV17Test.Ctx upgraded=c.reboot();GameStore updated=store(upgraded);check(updated.homeLayout().placements().size()==5&&updated.coins()==coins&&updated.stars(1)==stars&&updated.unlockedLevel()==unlocked&&updated.equippedCharacter().equals(character)&&updated.equippedSkin().equals(skin)&&updated.avatarStyle().equals(style),"legacy save retains progression wardrobe balance");
        check(upgraded.save().disk.equals(previous),"legacy house reads do not mutate upgraded save");
        c.save().edit().putString("home_layout_v113","1|3,3|sofa,2,0,0;").commit();check(s.homeLayout().placement("sofa")==null,"unowned imported furniture filtered");
        c.save().edit().putString("home_layout_v113","broken").commit();check(s.homeLayout().encode().equals(HomeLayout.starter().encode()),"corrupt layout falls back safely without touching wallet");
    }
    private static void firstLight(){
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);check(s.shouldStartFirstLight()&&!s.firstLightSeen()&&s.firstLightHistory().isEmpty(),"new profile gets first touch");
        s.saveFirstLightHistory("0,1");check(store(c.reboot()).firstLightHistory().equals("0,1"),"first touch order durably resumes");
        for(String invalid:new String[]{null,"3","0,0","0,1,2,0","0,","x","-1"}){boolean rejected=false;try{s.saveFirstLightHistory(invalid);}catch(IllegalArgumentException expected){rejected=true;}check(rejected&&s.firstLightHistory().equals("0,1"),"invalid first touch history read only");}
        int coins=s.coins(),hints=s.boosterStock("hint");s.finishFirstLight();s.finishFirstLight();s.saveFirstLightHistory("2");check(s.firstLightSeen()&&!s.shouldStartFirstLight()&&s.firstLightHistory().isEmpty(),"finish suppresses automatic restart and cannot resurrect history");
        check(s.coins()==coins&&s.boosterStock("hint")==hints&&s.unlockedLevel()==1&&s.completedLevels()==0&&!s.tutorialRewardClaimed(),"first touch completion grants no fake progression or gift");
        GameStore skipped=store(new RewardsCosmeticsV17Test.Ctx());skipped.finishFirstLight();check(skipped.coins()==180&&skipped.completedLevels()==0&&skipped.firstLightSeen(),"immediate skip has no fake reward");
        RewardsCosmeticsV17Test.Ctx tutorial=new RewardsCosmeticsV17Test.Ctx();GameStore t=store(tutorial);t.saveTutorialState("T1|0|0|ACTIVE|");check(!t.shouldStartFirstLight(),"existing tutorial resumes without new intro");
        GameStore progressed=store(new RewardsCosmeticsV17Test.Ctx());progressed.completeLevel(1,1);check(!progressed.shouldStartFirstLight(),"progressed save never forced into new intro");
        RewardsCosmeticsV17Test.Ctx legacy=new RewardsCosmeticsV17Test.Ctx();GameStore old=store(legacy);legacy.save().edit().putBoolean("tutorial_v18_fresh_profile",false).commit();check(!old.shouldStartFirstLight(),"legacy nonfresh profile no intro");
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx f=new RewardsCosmeticsV17Test.Ctx();GameStore first=store(f),second=store(f);f.save().fail(memory);check(refused(()->first.saveFirstLightHistory("1")),"history save refusal surfaced");f.save().fail(memory);check(refused(second::firstLightHistory),"history reads flush pending save");second.retryPendingLocalSave();check(second.firstLightHistory().equals("1")&&store(f.reboot()).firstLightHistory().equals("1"),"history exact multi-handle durable retry");
            f.save().fail(memory);check(refused(first::finishFirstLight),"finish save refusal surfaced");check(!f.save().disk.containsKey("first_light_seen_v113")&&f.save().disk.get("first_light_history_v113").equals("1"),"failed finish leaves complete previous durable history");f.save().fail(memory);check(refused(second::firstLightSeen),"seen read flushes refused finish");second.finishFirstLight();GameStore cold=store(f.reboot());check(cold.firstLightSeen()&&cold.firstLightHistory().isEmpty()&&cold.coins()==180&&cold.completedLevels()==0,"finish retries once with no progress or gift");
        }
    }
    public static void main(String[] args)throws Exception{
        catalogue();layouts();storeActions();failures();coldFailureAndLegacy();firstLight();
        String report="{\n  \"passed\": true,\n  \"checks\": "+checks+",\n  \"objects\": 12,\n  \"languages\": 6,\n  \"failure_models\": [\"refused before memory\", \"memory before disk refusal\"],\n  \"scope\": \"JVM actual GameStore API, six-by-six immutable room, paths, placements, earned coin ownership, six-language copy, multi-handle retries, cold recreation and legacy preservation. Separate device interaction validation required.\"\n}\n";
        if(args.length>0){Path out=Path.of(args[0]);if(out.getParent()!=null)Files.createDirectories(out.getParent());Files.writeString(out,report);}
        System.out.println("PASS "+checks+" home model and durability checks");
    }
}
