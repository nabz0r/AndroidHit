import com.androidhit.lumadrift.data.*;
import java.nio.file.*;
import java.util.*;

/** Real-store upgrade, independent rooms and atomic failure checks. No simulated Android rendering. */
public final class HomeV115Test {
    private static int checks,funds;
    private static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    private static GameStore store(RewardsCosmeticsV17Test.Ctx c){return new GameStore(c,null,new RewardsCosmeticsV17Test.Clock("2026-10-28"));}
    private static void fund(GameStore s,int amount){while(s.coins()<amount)check(s.grantMilestone("home-v115-funding:"+funds++,Math.min(500,amount-s.coins()))>0,"earned funding");}
    private static boolean refused(Runnable task){try{task.run();return false;}catch(GameStore.LocalSaveException expected){return true;}}
    private static void catalog(){
        check(HomeCatalog.SIZE==10&&HomeCatalog.ITEMS.size()==42,"100 tiles and 42 distinctive furniture types");
        check(HomeCatalog.ROOMS.subList(0,3).equals(Arrays.asList("living","bedroom","garden")),"three original room identities and ordering stay stable");
        check(HomeCatalog.ROOMS.equals(Arrays.asList("living","bedroom","garden","kitchen")),"1.21 adds only the separately saved kitchen");
        Set<String> ids=new HashSet<>();Set<String> themes=new HashSet<>();int large=0;
        for(HomeCatalog.Item item:HomeCatalog.ITEMS){
            check(ids.add(item.id),"unique furniture id");check(item.width>0&&item.depth>0&&item.width<=3&&item.depth<=3,"bounded footprint");
            if(item.width>=3||item.depth>=3)large++;themes.add(HomeCatalog.theme(item.id));
            for(String language:new String[]{"en","fr","de","es","pt","it"}){
                check(!item.name(language).equals(item.id+"_name")&&!item.description(language).equals(item.id+"_desc"),"explicit catalog translations "+language+" "+item.id);
                check(!item.name(language).isBlank()&&!item.description(language).isBlank(),"nonempty item copy");
            }
        }
        check(large>=7&&themes.size()==3,"catalog includes large silhouettes in three art directions");
        for(String language:new String[]{"en","fr","de","es","pt","it"})for(String key:HomeStrings.keys())check(!HomeStrings.get(language,key).equals(key)&&!HomeStrings.get(language,key).isBlank(),"complete six-language home copy");
        check(HomeCatalog.price("piano")==560&&HomeCatalog.price("sofa")==240,"new earned prices and original prices stable");
    }
    private static void layouts(){
        HomeLayout empty=new HomeLayout(Collections.emptyList(),0,0);List<HomeLayout.Cell> path=empty.pathTo(9,9);check(path.size()==18,"shortest path uses entire 10x10 floor");
        HomeLayout.Cell previous=new HomeLayout.Cell(0,0);for(HomeLayout.Cell step:path){check(Math.abs(step.x-previous.x)+Math.abs(step.y-previous.y)==1&&!empty.isBlocked(step.x,step.y),"clear orthogonal path");previous=step;}
        check(empty.tryMove(10,9)==null&&empty.tryMove(-1,0)==null,"new room bounds enforced");
        for(String room:HomeCatalog.ROOMS){HomeLayout starter=HomeLayout.starter(room);check(!starter.placements().isEmpty(),"every new room furnished");check(starter.encode().equals(HomeLayout.decode(starter.encode()).encode()),"stable v2 room roundtrip");for(HomeLayout.Placement item:starter.placements())check(HomeCatalog.item(item.id).starter(),"starter room never grants paid objects");}
        for(HomeCatalog.Item item:HomeCatalog.ITEMS){
            HomeLayout base=new HomeLayout(Collections.emptyList(),0,0);
            for(int rotation=0;rotation<2;rotation++){
                int width=rotation==0?item.width:item.depth,depth=rotation==0?item.depth:item.width;
                HomeLayout placed=base.tryPlace(item.id,10-width,10-depth,rotation);check(placed!=null,"edge placement valid "+item.id+rotation);
                check(placed.placement(item.id).width()==width&&placed.placement(item.id).depth()==depth,"quarter-turn changes full footprint");
                check(base.tryPlace(item.id,11-width,10-depth,rotation)==null,"whole object cannot protrude "+item.id+rotation);
                check(placed.encode().equals(HomeLayout.decode(placed.encode()).encode()),"new object save roundtrip");
            }
        }
        HomeLayout library=empty.tryPlace("library",2,2,0);check(library.tryPlace("aquarium",3,2,0)==null,"large furniture collisions guarded");check(library.tryPlace("garden_path",2,2,0)!=null,"walkable path can underlay solid furniture");
        List<HomeLayout.Placement> barrier=new ArrayList<>();String[] items={"lamp","plant","table","bookshelf","cushion","plush","gate_lamp","mobile","stool","record_player"};for(int x=0;x<10;x++)barrier.add(new HomeLayout.Placement(items[x],x,4,0));
        HomeLayout separated=new HomeLayout(barrier,5,5);check(separated.pathTo(5,3).isEmpty()&&separated.tryMove(5,3)==null,"BFS cannot cross a ten-tile barrier");
        String legacy="1|3,3|bed,0,0,0;lamp,5,0,0;plant,0,5,0;rug,2,2,0;cushion,4,4,0;";
        HomeLayout upgraded=HomeLayout.decode(legacy);check(upgraded!=null&&upgraded.companionX==3&&upgraded.placement("lamp").x==5,"old six-by-six placement coordinates preserved exactly");
        check(upgraded.tryPlace("aquarium",7,8,0)!=null,"migrated room can immediately use new floor extension");
        for(String invalid:new String[]{"1|6,3|","1|3,3|lamp,7,1,0;","2|10,1|","2|1,1|piano,9,9,0;","2|1,1|lamp,2,2,2;","2|1,1|lamp,2,2,0;lamp,3,3,0;","2|1,1|unknown,2,2,0;"})check(HomeLayout.decode(invalid)==null,"corrupt or impossible record rejected atomically");
    }
    private static void independentRooms(){
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);Map<String,Object> before=new HashMap<>(c.save().disk);
        String living=s.homeLayout("living").encode(),bedroom=s.homeLayout("bedroom").encode(),garden=s.homeLayout("garden").encode();
        check(c.save().disk.equals(before)&&s.coins()==180,"room reads cause no migration writes or debit");check(s.homeRoom().equals("living")&&s.homeLayout("unknown")==null&&!s.setHomeRoom("unknown"),"invalid room selection guarded");
        fund(s,560);int coins=s.coins();check(s.setHomeGoal("piano")&&s.purchaseHomeItem("piano")&&s.coins()==coins-560&&s.homeGoal().isEmpty(),"purchase grants once and clears wish atomically");
        check(s.placeHomeItem("piano",7,7,0),"large object placed in extended living room");String furnished=s.homeLayout().encode();
        check(s.setHomeRoom("bedroom")&&s.homeLayout().encode().equals(bedroom),"bedroom has independent placement state");
        check(s.placeHomeItem("piano",7,7,1)&&s.setHomeWallpaper("wallpaper_sage")&&s.setHomeFloor("floor_cloud"),"owned design usable in another room without buying again");String furnishedBedroom=s.homeLayout().encode();
        check(s.homeWallpaper("living").equals("wallpaper_cream")&&s.homeFloor("living").equals("floor_oak")&&s.homeLayout("living").encode().equals(furnished),"decorating bedroom never changes living layout or surfaces");
        check(s.setHomeRoom("garden")&&s.homeLayout().encode().equals(garden)&&!s.placeHomeItem("greenhouse",0,7,0),"garden starter independent and unowned greenhouse refused");
        check(s.moveHomeCompanion(9,9),"companion reaches newly added far corner");check(s.setHomeRoom("bedroom")&&s.removeHomeItem("piano")&&s.ownsHomeItem("piano")&&s.homeLayout("living").placement("piano")!=null,"store away is local to room and retains ownership");
        GameStore cold=store(c.reboot());check(cold.homeRoom().equals("bedroom")&&cold.homeLayout("garden").companionX==9&&cold.homeLayout("living").encode().equals(furnished)&&cold.homeLayout().placement("piano")==null&&cold.homeFloor().equals("floor_cloud"),"all rooms and selected destination survive cold restart");
        check(!s.purchaseHomeItem("piano")&&s.coins()==coins-560,"moving between rooms cannot charge ownership again");
    }
    private static void legacyAndFaults(){
        RewardsCosmeticsV17Test.Ctx legacy=new RewardsCosmeticsV17Test.Ctx();GameStore old=store(legacy);fund(old,240);check(old.purchaseHomeItem("sofa"),"legacy paid ownership setup");
        String v1="1|3,3|bed,0,0,0;lamp,5,0,0;sofa,4,1,1;";legacy.save().edit().putString("home_layout_v113",v1).putString("home_wallpaper_v113","wallpaper_sage").commit();Map<String,Object> original=new HashMap<>(legacy.save().disk);
        GameStore upgraded=store(legacy.reboot());check(upgraded.homeLayout().placement("sofa").x==4&&upgraded.homeLayout().placement("sofa").rotation==1&&upgraded.homeWallpaper().equals("wallpaper_sage")&&upgraded.coins()==old.coins(),"legacy furniture purchases, rotation, finishes and wallet retained");check(legacy.save().disk.equals(original),"legacy reads never rewrite old record");
        check(upgraded.placeHomeItem("sofa",8,8,0)&&upgraded.homeLayout().encode().startsWith("2|"),"first successful edit upgrades one complete room record");
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore a=store(c),b=store(c);fund(a,640);int coins=a.coins();c.save().fail(memory);check(refused(()->a.purchaseHomeItem("greenhouse")),"purchase write refusal surfaced");
            check(!c.save().disk.containsKey("home_owned_v113_greenhouse")&&c.save().disk.get("coins").equals(coins),"failed purchase leaves whole previous durable wallet");
            c.save().fail(memory);check(refused(()->b.setHomeRoom("garden")),"new room action cannot bypass refused pending purchase");b.retryPendingLocalSave();check(b.ownsHomeItem("greenhouse")&&b.coins()==coins-640&&!b.purchaseHomeItem("greenhouse"),"multi-handle retry applies exactly one debit");
            check(b.setHomeRoom("garden"),"garden selected");String before=b.homeLayout().encode();c.save().fail(memory);check(refused(()->a.placeHomeItem("greenhouse",7,7,0)),"placement failure surfaced");check(!c.save().disk.containsKey("home_layout_v115_garden"),"failed placement never partially writes room");
            check(b.setHomeRoom("bedroom"),"room switch flushes exact pending edit first");check(b.homeLayout("garden").placement("greenhouse")!=null&&b.homeLayout("bedroom").placement("greenhouse")==null,"pending placement retains originating room during switch");
            c.save().fail(memory);check(refused(()->a.setHomeFloor("floor_cloud")),"per-room surface failure surfaced");b.retryPendingLocalSave();check(b.homeFloor("bedroom").equals("floor_cloud")&&b.homeFloor("living").equals("floor_oak"),"surface retry retains originating room");
            c.save().fail(memory);check(refused(()->a.setHomeRoom("garden")),"selected room failure surfaced");GameStore cold=store(c.reboot());check(cold.homeRoom().equals("bedroom")&&cold.homeLayout("garden").placement("greenhouse")!=null&&cold.coins()==coins-640,"process loss restores complete last durable state");
        }
    }
    public static void main(String[] args)throws Exception{
        catalog();layouts();independentRooms();legacyAndFaults();String report="{\n  \"passed\":true,\n  \"checks\":"+checks+",\n  \"rooms\":"+HomeCatalog.ROOMS.size()+",\n  \"tiles_per_room\":100,\n  \"furniture_types\":42,\n  \"languages\":6,\n  \"scope\":\"Actual GameStore on host JVM: v1 migration without relocation, complete ten-by-ten paths, collisions, independent rooms and surfaces, exact ownership, failed writes before/after memory and cold restart. Rendering and touch require device validation.\"\n}\n";
        if(args.length>0){Path out=Path.of(args[0]);if(out.getParent()!=null)Files.createDirectories(out.getParent());Files.writeString(out,report);}System.out.println("PASS "+checks+" v1.15 home upgrade and durability checks");
    }
}
