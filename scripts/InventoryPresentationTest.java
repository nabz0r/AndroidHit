import android.content.Context;
import android.content.SharedPreferences;
import com.androidhit.lumadrift.data.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Presentation regressions: real GameStore adapters, catalog boundaries and exact six-language contracts. */
public final class InventoryPresentationTest {
    private static int checks;
    private static final class Prefs implements SharedPreferences {
        private final Map<String,Object> values=new HashMap<>();
        public Map<String,?> getAll(){return new HashMap<>(values);}
        public boolean contains(String key){return values.containsKey(key);}
        public int getInt(String key,int fallback){return values.containsKey(key)?(Integer)values.get(key):fallback;}
        public String getString(String key,String fallback){return values.containsKey(key)?(String)values.get(key):fallback;}
        public boolean getBoolean(String key,boolean fallback){return values.containsKey(key)?(Boolean)values.get(key):fallback;}
        public Editor edit(){return new Editor(){
            final Map<String,Object> updates=new HashMap<>();
            public Editor putInt(String key,int value){updates.put(key,value);return this;}
            public Editor putString(String key,String value){updates.put(key,value);return this;}
            public Editor putBoolean(String key,boolean value){updates.put(key,value);return this;}
            public boolean commit(){values.putAll(updates);return true;}
        };}
    }
    private static final class TestContext extends Context {
        final Prefs prefs=new Prefs();
        public SharedPreferences getSharedPreferences(String name,int mode){return prefs;}
    }
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    private static InventoryPresentation.Entry find(List<InventoryPresentation.Entry> entries,String id){
        for(InventoryPresentation.Entry entry:entries)if(entry.id.equals(id))return entry;
        throw new AssertionError("Missing entry "+id);
    }
    private static InventoryPresentation.Power power(List<InventoryPresentation.Power> entries,String id){
        for(InventoryPresentation.Power entry:entries)if(entry.id.equals(id))return entry;
        throw new AssertionError("Missing power "+id);
    }
    private static void immutable(List<?> list,String message){
        boolean rejected=false;try{list.clear();}catch(UnsupportedOperationException expected){rejected=true;}
        check(rejected,message);
    }
    private static void ownershipAndReadOnlyAdapters(){
        TestContext context=new TestContext(); Set<String> cloudCharacters=new HashSet<>(),cloudThemes=new HashSet<>();
        GameStore store=new GameStore(context,new GameStore.ExternalOwnership(){
            public boolean ownsCharacter(String id){return cloudCharacters.contains(id);}
            public boolean ownsSkin(String id){return cloudThemes.contains(id);}
        });
        context.prefs.edit().putInt("coins",10000).commit(); // Explicit synthetic earned-coin fixture, no production wallet.
        Map<String,?> before=context.prefs.getAll();
        List<InventoryPresentation.Entry> companions=InventoryPresentation.companions(store),themes=InventoryPresentation.themes(store);
        List<InventoryPresentation.Entry> gear=InventoryPresentation.gear(store,(AvatarGearCatalog.Slot)null);
        check(companions.size()==24&&themes.size()==6&&gear.size()==18,"Full catalogs represented exactly once");
        check(InventoryPresentation.counts(companions).owned==1,"Only starter companion on a fresh profile");
        check(InventoryPresentation.counts(themes).owned==1,"Only starter theme on a fresh profile");
        InventoryPresentation.Counts gearCounts=InventoryPresentation.counts(gear);
        check(gearCounts.owned==3&&gearCounts.selected==3&&gearCounts.locked==15,"One owned selected starter per equipment slot");
        check(find(companions,"nova").state==InventoryPresentation.State.SELECTED,"Selected companion is distinguished");
        check(find(companions,"nova").stateKey().equals("selected")&&find(themes,"lumina").stateKey().equals("selected")
                &&find(gear,"moonbud").stateKey().equals("equipped"),"Selected cosmetics and equipped gear have precise labels");
        check(find(companions,"miko").state==InventoryPresentation.State.LOCKED,"Unowned companion stays locked");
        check(find(companions,"miko").canAfford,"Earned shop affordability uses actual coin balance");
        check(!find(companions,"nova").canAfford,"Owned starter never offered as a second purchase");
        check(InventoryPresentation.state(false,true)==InventoryPresentation.State.LOCKED,"A stale selection cannot manufacture ownership");
        check(InventoryPresentation.companion(store,"missing")==null&&InventoryPresentation.theme(store,null)==null
                &&InventoryPresentation.gear(store,"missing")==null,"Unknown IDs are absent, never phantom owned items");
        for(AvatarGearCatalog.Slot slot:AvatarGearCatalog.Slot.values()){
            List<InventoryPresentation.Entry> items=InventoryPresentation.gear(store,slot);
            check(items.size()==6,"Six items in "+slot);
            for(InventoryPresentation.Entry entry:items)check(entry.gear.slot==slot,"Slot filters preserve item type");
        }
        List<InventoryPresentation.Entry> owned=InventoryPresentation.filter(gear,InventoryPresentation.Filter.OWNED);
        List<InventoryPresentation.Entry> locked=InventoryPresentation.filter(gear,InventoryPresentation.Filter.LOCKED);
        check(owned.size()==3&&locked.size()==15,"Owned filter includes selected equipment");
        check(InventoryPresentation.filter(gear,InventoryPresentation.Filter.ALL).size()==18,"All filter retains all catalog entries");
        immutable(companions,"Companion snapshot is immutable");immutable(themes,"Theme snapshot is immutable");
        immutable(gear,"Gear snapshot is immutable");immutable(owned,"Filtered snapshot is immutable");
        check(before.equals(context.prefs.getAll()),"Building/filtering/rendering snapshots performs no storage mutation");

        cloudCharacters.add("miko");cloudThemes.add("aurora");
        check(InventoryPresentation.companion(store,"miko").state==InventoryPresentation.State.OWNED,"External paid ownership appears in the same collection");
        check(InventoryPresentation.theme(store,"aurora").owned(),"External theme ownership appears without a local purchase");
        context.prefs.edit().putString("character","miko").putString("skin","aurora").commit();
        check(InventoryPresentation.companion(store,"miko").selected()&&InventoryPresentation.theme(store,"aurora").selected(),"Selected cloud items are represented accurately");
        cloudCharacters.clear();cloudThemes.clear();
        check(!InventoryPresentation.companion(store,"miko").owned()&&!InventoryPresentation.theme(store,"aurora").owned(),"Account ownership revocation immediately removes stale possession");
        check(InventoryPresentation.companion(store,"nova").selected()&&InventoryPresentation.theme(store,"lumina").selected(),"Store fallback selection remains consistent after account switch");
        context.prefs.edit().putBoolean("owned_character_miko",true).commit();
        check(InventoryPresentation.companion(store,"miko").selected(),"Local earned cosmetic remains valid without cloud ownership");
        check(store.coins()==10000,"Presenting cloud/local ownership never debits earned coins");
    }
    private static void exactUnlockProgress(){
        for(AvatarGearCatalog.Item item:AvatarGearCatalog.ITEMS){
            AvatarGearCatalog.Progress below=new AvatarGearCatalog.Progress(Math.max(0,item.requirement-1),Math.max(0,item.requirement-1),Math.max(0,item.requirement-1));
            AvatarGearCatalog.Progress reached=new AvatarGearCatalog.Progress(item.requirement,item.requirement,item.requirement);
            InventoryPresentation.Entry low=InventoryPresentation.gear(item,false,true,below,10000);
            InventoryPresentation.Entry ready=InventoryPresentation.gear(item,false,false,reached,10000);
            check(low.state==InventoryPresentation.State.LOCKED,"Unclaimed selection cannot imply ownership: "+item.id);
            if(item.source==AvatarGearCatalog.Source.EARNED_COINS){
                check(!ready.readyToClaim&&ready.canAfford&&ready.priceCoins==item.priceCoins,"Shop gear never becomes free from a progression threshold: "+item.id);
                check(ready.progress==0&&ready.required==0,"Coin prices are not shown as board progress: "+item.id);
                check(!InventoryPresentation.gear(item,false,false,reached,item.priceCoins-1).canAfford,"Exact earned affordability threshold: "+item.id);
            }else if(item.source==AvatarGearCatalog.Source.STARTER){
                check(ready.readyToClaim,"Starter condition has no progress requirement");
            }else{
                check(!low.readyToClaim&&ready.readyToClaim,"Only exact threshold unlocks item: "+item.id);
                check(low.progress==item.requirement-1&&ready.progress==item.requirement,"Source progress is exact at boundary: "+item.id);
                InventoryPresentation.Entry capped=InventoryPresentation.gear(item,false,false,new AvatarGearCatalog.Progress(1000,Integer.MAX_VALUE,Integer.MAX_VALUE),0);
                check(capped.progress==item.requirement,"Completed progress caption is capped to its requirement: "+item.id);
            }
            check(!InventoryPresentation.gear(item,true,false,reached,10000).readyToClaim,"Already owned gear does not advertise a duplicate claim: "+item.id);
        }
        TestContext context=new TestContext();GameStore store=new GameStore(context);
        SharedPreferences.Editor fixture=context.prefs.edit();for(int level=1;level<=8;level++)fixture.putInt("stars_"+level,1);
        fixture.putInt("weekly_2001-01-01_0",3).putInt("weekly_2001-01-01_1",3).putInt("weekly_2001-01-01_2",3)
                .putInt("daily_2001-01-01_0",3).putInt("daily_2001-01-01_1",3).putInt("daily_2001-01-01_2",3).commit();
        Map<String,?> before=context.prefs.getAll();
        check(InventoryPresentation.gear(store,"lantern_leaf").readyToClaim,"Journey progress unlock condition comes from actual distinct clears");
        InventoryPresentation.Entry weekly=InventoryPresentation.gear(store,"crescent_knot"),event=InventoryPresentation.gear(store,"tide_crown");
        check(weekly.progress==3&&weekly.readyToClaim,"Weekly progress includes recorded prior periods");
        check(event.progress==18&&event.readyToClaim,"Event stars include both weekly and daily history");
        check(!weekly.owned()&&!event.owned(),"Available rewards remain unowned until the existing claim action runs");
        check(before.equals(context.prefs.getAll()),"Reading earned unlocks never claims or persists them");
    }
    private static void powersAndPages(){
        TestContext context=new TestContext();GameStore store=new GameStore(context);
        context.prefs.edit().putInt("booster_hint",0).putInt("booster_vision",6).commit();
        Map<String,?> before=context.prefs.getAll();
        List<InventoryPresentation.Power> powers=InventoryPresentation.powers(store,Set.of("hint","unknown"));
        check(powers.size()==5,"Exactly five current offers; retired/unknown active IDs cannot create another");
        check(power(powers,"hint").state==InventoryPresentation.PowerState.ACTIVE&&!power(powers,"hint").canUse(),"Last spent charge remains visibly active and cannot be activated again");
        check(power(powers,"vision").state==InventoryPresentation.PowerState.LOCKED&&!power(powers,"vision").canUse(),"Saved stock does not bypass an unlock milestone");
        check(power(InventoryPresentation.powers(store,null),"hint").state==InventoryPresentation.PowerState.EMPTY,"Empty unlocked power is distinct from locked and active");
        check(before.equals(context.prefs.getAll()),"Power snapshot does not spend, reserve or grant stock");immutable(powers,"Power snapshot is immutable");
        context.prefs.edit().putInt("unlocked",75).putInt("booster_hint",6).commit();
        for(InventoryPresentation.Power entry:InventoryPresentation.powers(store,Set.of())){
            check(entry.unlocked,"Final milestone unlocks "+entry.id);
            check(entry.packPrice==store.boosterPrice(entry.id),"Displayed pack price uses store economy/aura: "+entry.id);
        }
        check(power(InventoryPresentation.powers(store,Set.of()),"hint").canUse(),"Positive stock unlocked power is available");
        check("vision".equals(InventoryPresentation.nextPower(1).id)&&"flow".equals(InventoryPresentation.nextPower(5).id),"Next milestone advances at exact unlock levels");
        check(InventoryPresentation.nextPower(75)==null,"No fictional unlock after all current powers");
        for(int total:new int[]{0,1,6,10,11,240,1000,Integer.MAX_VALUE}){
            InventoryPresentation.Page page=InventoryPresentation.sagaPage(total,Integer.MAX_VALUE);
            check(page.pageCount==(total==0?0:(total-1)/10+1),"Page count rounds up: "+total);
            check(page.last==total&&!page.hasNext(),"Last page ends exactly at catalog boundary: "+total);
            check(!page.contains(total+1)||total==Integer.MAX_VALUE,"No phantom next board: "+total);
            check(page.size()<=10,"No page exceeds ten level nodes: "+total);
        }
        InventoryPresentation.Page finalJourney=InventoryPresentation.sagaPage(1000,99),finalRemix=InventoryPresentation.sagaPage(240,23);
        check(finalJourney.first==991&&finalJourney.last==1000&&finalJourney.pageCount==100,"Journey final chapter is 991–1000");
        check(finalRemix.first==231&&finalRemix.last==240&&finalRemix.pageCount==24,"Remix bounds stay independent");
        check(InventoryPresentation.sagaPage(1000,-1).first==1,"Negative page request clamps to first chapter");
        check(InventoryPresentation.page(-1,0,4).size()==0,"Empty/invalid totals cannot create a level zero card");
        check(InventoryPresentation.page(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE).last==Integer.MAX_VALUE,"Extreme page sizes cannot overflow end bounds");
    }
    @SuppressWarnings("unchecked") private static void languages() throws Exception {
        Field field=ExperienceStrings.class.getDeclaredField("TEXT");field.setAccessible(true);
        Map<String,String[]> rows=(Map<String,String[]>)field.get(null);
        Pattern placeholder=Pattern.compile("%[ds]");
        for(Map.Entry<String,String[]> row:rows.entrySet()){
            check(row.getValue().length==6,"Six translations for "+row.getKey());
            List<String> expected=new ArrayList<>();Matcher english=placeholder.matcher(row.getValue()[0]);while(english.find())expected.add(english.group());
            Object[] args=new Object[expected.size()];Arrays.fill(args,7);
            int index=0;for(String lang:new String[]{"en","fr","de","es","pt","it"}){
                String text=row.getValue()[index++];List<String> actual=new ArrayList<>();Matcher m=placeholder.matcher(text);while(m.find())actual.add(m.group());
                check(expected.equals(actual),"Placeholder parity "+lang+"/"+row.getKey());
                String formatted=ExperienceStrings.get(lang,row.getKey(),args);
                check(!formatted.isBlank()&&!formatted.contains("%d")&&!formatted.contains("%s"),"Formatted nonempty copy "+lang+"/"+row.getKey());
            }
        }
        check(ExperienceStrings.get("fr-FR","nav_inventory").equals("Inventaire"),"Regional language normalization");
        check(ExperienceStrings.get(null,"nav_map").equals("Map"),"Missing locale safely falls back to English");
        check(!ExperienceStrings.has("missing"),"Missing-key probe explicit");
        boolean rejected=false;try{ExperienceStrings.get("fr","missing");}catch(IllegalArgumentException expected){rejected=true;}
        check(rejected,"Unknown copy fails visibly in development, not as a raw key in UI");
        TestContext context=new TestContext();GameStore store=new GameStore(context);
        for(String lang:new String[]{"en","fr","de","es","pt","it"}){
            for(InventoryPresentation.Entry entry:InventoryPresentation.gear(store,(AvatarGearCatalog.Slot)null)){
                check(!InventoryPresentation.sourceCaption(lang,entry).isBlank(),"Localized source "+lang+"/"+entry.id);
                check(!InventoryPresentation.effectCaption(lang,entry).isBlank(),"Localized effect "+lang+"/"+entry.id);
            }
            for(InventoryPresentation.State state:InventoryPresentation.State.values())check(ExperienceStrings.has(state.key()),"All ownership states have UI copy");
            for(InventoryPresentation.PowerState state:InventoryPresentation.PowerState.values())check(ExperienceStrings.has(state.key()),"All power states have UI copy");
        }
    }
    public static void main(String[] args)throws Exception{
        ownershipAndReadOnlyAdapters();exactUnlockProgress();powersAndPages();languages();
        System.out.println("PASS "+checks+" inventory presentation checks (synthetic preferences; no Android runtime or external services)");
    }
}
