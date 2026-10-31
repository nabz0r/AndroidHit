import com.androidhit.lumadrift.data.*;
import java.nio.file.*;
import java.util.*;

/** Fourth room through the actual store; intrinsic fixtures grant no paid furniture. */
public final class HomeKitchenV121Test {
    private static long checks;
    private static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
    private static GameStore store(RewardsCosmeticsV17Test.Ctx context){return new GameStore(context,null,new RewardsCosmeticsV17Test.Clock("2026-10-30"));}
    private static void starter(){
        check(HomeCatalog.ITEMS.size()==48,"expanded inventory has 48 stable furnishing IDs");
        for(HomeCatalog.Item item:HomeCatalog.ITEMS)for(String lang:new String[]{"en","fr","de","es","pt","it"})check(!HomeStrings.name(lang,item.id).equals(item.id),"each furniture has six-language names");
        check(HomeCatalog.ROOMS.equals(Arrays.asList("living","bedroom","garden","kitchen")),"original three IDs remain ordered and kitchen appended once");
        check(HomeLayout.starter("living").encode().equals("2|3,3|bed,0,0,0;lamp,5,0,0;plant,0,5,0;rug,2,2,0;cushion,4,4,0;"),"original living starter byte-identical");
        check(HomeLayout.starter("bedroom").encode().equals("2|5,5|bed,2,1,0;lamp,7,1,0;rug,4,4,0;cushion,7,6,0;"),"original bedroom starter byte-identical");
        String legacyGarden="2|5,5|plant,1,1,0;flowerbed,5,1,0;garden_path,4,4,0;";
        check(HomeLayout.decode(legacyGarden).encode().equals(legacyGarden),"saved original garden remains byte-identical");
        check(HomeLayout.starter("garden").placements().size()==5,"new garden contains five free furnishings");
        HomeLayout kitchen=HomeLayout.starter("kitchen");Set<String> contents=new HashSet<>();
        for(HomeLayout.Placement placement:kitchen.placements()){
            contents.add(placement.id);check(HomeCatalog.item(placement.id).starter(),"kitchen starter uses existing free items only");
            check(!placement.id.equals("kitchen"),"paid kitchen furniture is not granted as architecture");
        }
        check(contents.equals(new HashSet<>(Arrays.asList("plant","rug","cushion","herb_cart","seed_shelf"))),"expanded kitchen welcome content explicit");
        String legacyKitchen="2|5,5|plant,1,1,0;rug,4,4,0;cushion,7,6,0;";
        check(HomeLayout.decode(legacyKitchen).encode().equals(legacyKitchen),"saved original kitchen remains byte-identical");
        for(int x=0;x<HomeCatalog.SIZE;x++)for(int y=0;y<HomeCatalog.SIZE;y++)if(!kitchen.isBlocked(x,y)){
            List<HomeLayout.Cell> path=kitchen.pathTo(x,y);
            check(x==kitchen.companionX&&y==kitchen.companionY||!path.isEmpty(),"every free kitchen floor cell remains reachable");
            int px=kitchen.companionX,py=kitchen.companionY;
            for(HomeLayout.Cell cell:path){check(Math.abs(cell.x-px)+Math.abs(cell.y-py)==1&&!kitchen.isBlocked(cell.x,cell.y),"each actual kitchen BFS step stays clear");px=cell.x;py=cell.y;}
        }
        check(HomeLayout.decode(kitchen.encode()).encode().equals(kitchen.encode()),"kitchen complete layout roundtrip");
        check(HomeCatalog.price("kitchen")==440 && !HomeCatalog.item("kitchen").starter(),"paid kitchen decoration keeps exact 440-Coin price");
        for(String language:new String[]{"en","fr","de","es","pt","it"})check(!HomeStrings.get(language,"room_kitchen").equals("room_kitchen")&&!HomeStrings.get(language,"room_kitchen").isBlank(),"explicit room name in six languages");
    }
    private static void independentPersistence(){
        RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();GameStore value=store(context);
        Map<String,String> original=new LinkedHashMap<>(),walls=new LinkedHashMap<>(),floors=new LinkedHashMap<>();
        for(String room:Arrays.asList("living","bedroom","garden")){original.put(room,value.homeLayout(room).encode());walls.put(room,value.homeWallpaper(room));floors.put(room,value.homeFloor(room));}
        Map<String,Object> untouched=new HashMap<>(context.save().disk);int coins=value.coins();
        check(value.homeLayout("kitchen").encode().equals(HomeLayout.starter("kitchen").encode()),"new room loads safely from existing save");
        check(context.save().disk.equals(untouched)&&value.coins()==coins&&!value.ownsHomeItem("kitchen"),"kitchen read adds no wallet write or paid ownership");
        check(value.setHomeRoom("kitchen")&&value.placeHomeItem("plant",8,8,0),"kitchen selected and independently decorated");
        check(value.setHomeWallpaper("wallpaper_sage")&&value.setHomeFloor("floor_cloud")&&value.moveHomeCompanion(9,9),"kitchen free finishes and far-corner movement supported");
        String edited=value.homeLayout().encode();
        check(context.save().disk.containsKey("home_layout_v115_kitchen"),"new room uses its own key without renaming old keys");
        check(!value.placeHomeItem("kitchen",0,8,0)&&!value.ownsHomeItem("kitchen")&&value.coins()==coins,"intrinsic backdrop cannot authorize paid furnishing");
        for(String room:original.keySet())check(value.homeLayout(room).encode().equals(original.get(room))&&value.homeWallpaper(room).equals(walls.get(room))&&value.homeFloor(room).equals(floors.get(room)),"kitchen edits preserve each original layout and actual finishes");
        GameStore cold=store(context.reboot());
        check(cold.homeRoom().equals("kitchen")&&cold.homeLayout().encode().equals(edited)&&cold.homeFloor().equals("floor_cloud")&&cold.homeWallpaper().equals("wallpaper_sage"),"selected kitchen placement finishes and companion position survive cold reconstruction");
        check(!cold.ownsHomeItem("kitchen")&&cold.coins()==coins,"cold reconstruction cannot grant the paid decoration");
        check(cold.grantMilestone("kitchen-v121-earned-test-funding",300)==300,"test earns declared funding through real existing claim API");
        int funded=cold.coins();check(cold.purchaseHomeItem("kitchen")&&cold.coins()==funded-440,"paid kitchen remains an explicit exact debit");
        check(cold.homeLayout().placement("kitchen")==null&&!cold.purchaseHomeItem("kitchen"),"purchase neither auto-places nor debits a second time");
        check(cold.placeHomeItem("kitchen",0,8,0)&&cold.homeLayout("living").placement("kitchen")==null,"owned decoration can be explicitly placed only in chosen room");
    }
    private static void failedKitchenEdits(){
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();GameStore value=store(context);
            check(value.setHomeRoom("kitchen"),"failure fixture selects real kitchen");String before=value.homeLayout().encode();
            context.save().fail(memory);boolean refused=false;
            try{value.placeHomeItem("plant",8,8,0);}catch(GameStore.LocalSaveException expected){refused=true;}
            check(refused&&!context.save().disk.containsKey("home_layout_v115_kitchen"),"refused kitchen edit cannot partly reach durable save");
            GameStore cold=store(context.reboot());check(cold.homeLayout("kitchen").encode().equals(before),"process reconstruction restores old complete kitchen after failed edit");
        }
    }
    public static void main(String[] args)throws Exception{
        starter();independentPersistence();failedKitchenEdits();
        String report="{\n  \"passed\":true,\n  \"checks\":"+checks+",\n  \"rooms\":4,\n  \"scope\":\"Actual host GameStore kitchen separation, preserved living/bedroom starters and old serialized garden/kitchen saves, enriched free welcome layouts, exact paid ownership/440-Coin debit, all free floor paths, six-language room names, cold reconstruction and explicit failure doubles. Intrinsic rear-wall scenery is not paid furnishing. No native rendering or touch acceptance.\"\n}\n";
        if(args.length>0){Path out=Path.of(args[0]);Files.createDirectories(out.getParent());Files.writeString(out,report);}System.out.print(report);
    }
}
