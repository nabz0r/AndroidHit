import android.content.Context;
import android.content.SharedPreferences;
import com.androidhit.lumadrift.data.*;
import java.util.*;
import java.util.concurrent.*;

/** Real GameStore checks with durable preferences, refused commits, and account ownership changes. */
public final class SquadV119StoreTest {
    private static final String KEY="companion_squad_v119";
    private static int checks;
    private static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    private static void equal(Object a,Object b,String label){check(Objects.equals(a,b),label+": "+a+" != "+b);}
    private static void refused(Runnable action,String label){boolean failed=false;try{action.run();}catch(GameStore.LocalSaveException expected){failed=true;}check(failed,label);}
    private static final class Prefs implements SharedPreferences {
        final Map<String,Object> memory=new HashMap<>(),disk=new HashMap<>();
        int failures,writes; boolean beforeFailure;
        public Map<String,?> getAll(){return new HashMap<>(memory);}
        public boolean contains(String key){return memory.containsKey(key);}
        public int getInt(String key,int fallback){return (Integer)memory.getOrDefault(key,fallback);}
        public String getString(String key,String fallback){return (String)memory.getOrDefault(key,fallback);}
        public boolean getBoolean(String key,boolean fallback){return (Boolean)memory.getOrDefault(key,fallback);}
        public Editor edit(){return new Editor(){final Map<String,Object> changes=new HashMap<>();
            public Editor putInt(String key,int value){changes.put(key,value);return this;}
            public Editor putString(String key,String value){changes.put(key,value);return this;}
            public Editor putBoolean(String key,boolean value){changes.put(key,value);return this;}
            public boolean commit(){writes++;if(failures>0){failures--;if(beforeFailure)memory.putAll(changes);return false;}memory.putAll(changes);disk.putAll(changes);return true;}
        };}
        void fail(int count,boolean memoryFirst){failures=count;beforeFailure=memoryFirst;}
        Prefs reboot(){Prefs out=new Prefs();out.disk.putAll(disk);out.memory.putAll(disk);return out;}
    }
    private static final class Ctx extends Context {
        final Map<String,Prefs> files=new HashMap<>();
        public SharedPreferences getSharedPreferences(String name,int mode){return files.computeIfAbsent(name,k->new Prefs());}
        Prefs save(){return files.get("luma_drift_save_v1");}
        Ctx reboot(){Ctx out=new Ctx();files.forEach((key,value)->out.files.put(key,value.reboot()));return out;}
    }
    private static final class Ownership implements GameStore.ExternalOwnership {
        final Set<String> characters=new HashSet<>();
        public boolean ownsCharacter(String id){return characters.contains(id);}
        public boolean ownsSkin(String id){return false;}
    }
    private static GameStore store(Ctx context){return new GameStore(context);}
    private static GameStore store(Ctx context,Ownership ownership){return new GameStore(context,ownership);}
    private static List<String> team(String... ids){return Arrays.asList(ids);}
    private static void initialAndOwned(){
        Ctx c=new Ctx();GameStore s=store(c);Map<String,Object> initial=new HashMap<>(c.save().disk);
        equal(s.companionSquad(),team("nova"),"starter is the only implicit member");
        equal(c.save().disk,initial,"legacy/default read performs no durable migration");
        check(!s.setCompanionSquad(null)&&!s.setCompanionSquad(Collections.emptyList()),"null and empty selections refused");
        check(!s.setCompanionSquad(team("miko"))&&!s.setCompanionSquad(team("nova","miko")),"unowned companion refused without purchase");
        check(!s.setCompanionSquad(team("nova","nova"))&&!s.setCompanionSquad(team("nova",null)),"duplicates and null members refused");
        for(String id:new String[]{"","unknown"," nova","Nova","nova,miko","nova\nnova"})check(!s.setCompanionSquad(team(id)),"invalid member refused: "+id);
        equal(c.save().disk,initial,"all invalid selections leave complete save untouched");
        s.grantMilestone("v119:fund",500);check(s.purchaseCharacter("miko")&&s.purchaseCharacter("orbit"),"owned fixtures bought through actual store");
        Map<String,Object> before=new HashMap<>(c.save().disk);int coins=s.coins();
        check(s.setCompanionSquad(team("orbit","nova","miko")),"three distinct owned members accepted");
        Map<String,Object> expected=new HashMap<>(before);expected.put(KEY,"orbit,nova,miko");
        equal(c.save().disk,expected,"team write only changes its own string key");
        equal(s.companionSquad(),team("orbit","nova","miko"),"captain and team order preserved");
        equal(s.equippedCharacter(),"nova","captain does not equip appearance");
        equal(s.coins(),coins,"team selection spends no currency");
        check(!s.setCompanionSquad(team("nova","miko","orbit","nova")),"over-capacity request rejected rather than truncated");
        int writes=c.save().writes;check(s.setCompanionSquad(team("orbit","nova","miko")),"same explicit team is accepted");
        equal(c.save().writes,writes,"same durable selection needs no extra commit");
        check(s.equipCharacter("miko"),"appearance fixture changes independently");
        equal(s.companionSquad(),team("orbit","nova","miko"),"equipping a different appearance keeps saved squad");
        equal(store(c.reboot()).companionSquad(),team("orbit","nova","miko"),"full team survives cold recreation");
        List<String> selected=new ArrayList<>(team("miko","orbit"));check(s.setCompanionSquad(selected),"mutable input accepted by value");selected.clear();
        equal(s.companionSquad(),team("miko","orbit"),"caller cannot mutate durable squad through input list");
        boolean immutable=false;try{s.companionSquad().add("nova");}catch(UnsupportedOperationException expectedError){immutable=true;}
        check(immutable,"returned list is immutable");
    }
    private static void ownershipAndDamagedSave(){
        Ctx c=new Ctx();Ownership external=new Ownership();external.characters.addAll(team("miko","orbit","pip"));GameStore s=store(c,external);
        equal(s.companionSquad(),team("nova"),"external ownership never auto-fills free slots");
        check(s.equipCharacter("miko"),"legacy equipped external member accepted");Map<String,Object> legacy=new HashMap<>(c.save().disk);
        equal(s.companionSquad(),team("miko"),"unconfigured legacy team starts with equipped owned companion");equal(c.save().disk,legacy,"legacy default still read-only");
        check(s.setCompanionSquad(team("orbit","miko","nova")),"external-owned team can be explicitly selected");Map<String,Object> selected=new HashMap<>(c.save().disk);
        external.characters.remove("orbit");equal(s.companionSquad(),team("miko","nova"),"revoked ownership omitted at read time");
        equal(c.save().disk,selected,"account change never erases saved selection");
        external.characters.add("orbit");equal(s.companionSquad(),team("orbit","miko","nova"),"restored account ownership restores original order");
        c.save().edit().putString(KEY,"unknown,miko,miko,,orbit,nova,pip").commit();Map<String,Object> damaged=new HashMap<>(c.save().disk);
        equal(s.companionSquad(),team("miko","orbit","nova"),"damaged duplicate and invalid entries filtered, maximum three");equal(c.save().disk,damaged,"damaged read never rewrites save");
        c.save().edit().putString(KEY,"missing,,missing").commit();equal(s.companionSquad(),team("miko"),"all-invalid saved members fall back to equipped ownership");
        c.save().edit().putInt(KEY,19).commit();damaged=new HashMap<>(c.save().disk);equal(s.companionSquad(),team("miko"),"wrong-type persisted value does not crash");equal(c.save().disk,damaged,"wrong-type read is non-mutating");
        external.characters.clear();equal(s.companionSquad(),team("nova"),"revoked equipped ownership falls back to starter");
        check(!s.setCompanionSquad(team("miko")),"revoked identity cannot be explicitly selected");
    }
    private static void failures(){
        for(boolean memoryFirst:new boolean[]{false,true}){
            Ctx c=new Ctx();Ownership external=new Ownership();external.characters.addAll(team("miko","orbit"));GameStore s=store(c,external);
            check(s.setCompanionSquad(team("nova")),"explicit original team fixture");Map<String,Object> old=new HashMap<>(c.save().disk);
            c.save().fail(3,memoryFirst);refused(()->s.setCompanionSquad(team("miko","orbit")),"typed failure surfaces, memory-first="+memoryFirst);
            equal(c.save().disk,old,"refused team has no partial durable mutation");
            refused(s::companionSquad,"read cannot expose uncommitted memory after another refusal");
            GameStore second=store(c,external); // Constructor safely catches the third refusal.
            equal(c.save().disk,old,"another handle cannot fabricate successful persistence");
            equal(second.companionSquad(),team("miko","orbit"),"read flushes exact pending ordered team");
            Map<String,Object> finalSave=new HashMap<>(old);finalSave.put(KEY,"miko,orbit");equal(c.save().disk,finalSave,"retry changes exactly one durable key");
            equal(store(c.reboot(),external).companionSquad(),team("miko","orbit"),"recovered team survives process recreation");
            c.save().fail(1,memoryFirst);refused(()->second.setCompanionSquad(team("orbit","nova")),"second write can also be refused");
            check(second.setCompanionSquad(team("nova","miko")),"new request first flushes old pending team then writes requested team");
            equal(store(c.reboot(),external).companionSquad(),team("nova","miko"),"new selection cannot be overwritten later by stale retry");

            Ctx cold=new Ctx();GameStore beforeCrash=store(cold,external);check(beforeCrash.setCompanionSquad(team("nova")),"cold fixture original team");
            cold.save().fail(1,memoryFirst);refused(()->beforeCrash.setCompanionSquad(team("miko")),"cold fixture failure");
            equal(store(cold.reboot(),external).companionSquad(),team("nova"),"process killed before retry retains old durable team");
        }
    }
    private static void interleavedEconomy(){
        for(boolean memoryFirst:new boolean[]{false,true}){
            Ctx c=new Ctx();GameStore s=store(c);s.grantMilestone("v119:interleave",500);int before=s.coins();
            c.save().fail(1,memoryFirst);refused(()->s.purchaseCharacter("miko"),"pending ownership fixture");
            check(s.setCompanionSquad(team("miko","nova")),"team settles pending purchase before testing ownership");
            equal(s.coins(),before-Catalog.character("miko").price,"settling team does not duplicate purchase debit");
            equal(store(c.reboot()).companionSquad(),team("miko","nova"),"purchase and team both durable");
            c.save().fail(1,memoryFirst);refused(()->s.setCompanionSquad(team("nova","miko")),"pending team fixture");
            check(s.purchaseBooster(Catalog.HINT),"power purchase flushes pending team first");
            equal(s.companionSquad(),team("nova","miko"),"another transaction cannot clobber team");
        }
    }
    private static void allCatalogMembersAndConcurrency() throws Exception {
        Ctx c=new Ctx();Ownership external=new Ownership();for(Catalog.CharacterItem character:Catalog.CHARACTERS)external.characters.add(character.id);GameStore s=store(c,external);
        int count=Catalog.CHARACTERS.length;check(count>=24,"full current avatar catalog exercised");
        Map<String,Object> before=new HashMap<>(c.save().disk);
        for(int i=0;i<count;i++){
            String a=Catalog.CHARACTERS[i].id,b=Catalog.CHARACTERS[(i+1)%count].id,d=Catalog.CHARACTERS[(i+2)%count].id;
            check(s.setCompanionSquad(team(a,b,d)),"catalog identity accepted "+a);equal(s.companionSquad(),team(a,b,d),"catalog ordered triple "+a);
            check(!s.setCompanionSquad(team(a,b,a)),"catalog duplicate refused "+a);equal(s.companionSquad(),team(a,b,d),"failed request preserves catalog team "+a);
            equal(store(c.reboot(),external).companionSquad(),team(a,b,d),"catalog team survives cold "+a);
        }
        List<Callable<Boolean>> requests=new ArrayList<>();ExecutorService pool=Executors.newFixedThreadPool(6);
        for(int i=0;i<120;i++){final int n=i;requests.add(()->{GameStore handle=store(c,external);List<String> desired=team(Catalog.CHARACTERS[n%count].id,Catalog.CHARACTERS[(n+1)%count].id);return handle.setCompanionSquad(desired);});}
        for(Future<Boolean> result:pool.invokeAll(requests,20,TimeUnit.SECONDS))check(!result.isCancelled()&&result.get(),"concurrent valid selection completes");pool.shutdown();check(pool.awaitTermination(10,TimeUnit.SECONDS),"concurrent team test terminates");
        List<String> finalTeam=store(c.reboot(),external).companionSquad();check(finalTeam.size()==2&&new HashSet<>(finalTeam).size()==2,"concurrent final save is a whole valid selection");
        Map<String,Object> after=new HashMap<>(c.save().disk);after.remove(KEY);equal(after,before,"all catalog and concurrent team edits preserve every unrelated save field");
    }
    private static void copy(){
        String[] plain={"squad","team_title","team_desc","team_empty","choose_squad","captain","captain_note","add_to_squad","remove_from_squad","squad_full","squad_saved","squad_locked","squad_minimum","in_squad","skill","energy","charge_on_exit","once_per_run","ready","used","combo","combo_detail","combo_ready","combo_used","combo_locked","choose_companion","skill_unavailable","skill_no_target","battle_title","apply","back","view_companion","cosmetics_note","run_team_fixed","empty_slot","skill_preview","skill_activate","team_back_play","stage_energy_help","squad_unlock_hint","save_squad","save_squad_next","combo_locked_short"};
        for(String lang:new String[]{"en","fr","de","es","pt","it"}){
            for(String key:plain){String value=V119Strings.get(lang,key);check(!value.isBlank()&&!value.equals(key),"localized interface "+lang+" "+key);}
            for(String key:new String[]{"slot","energy_cost","need_energy","team_pick_slot"})check(V119Strings.get(lang,key,17).contains("17"),"numeric interface contract "+lang+" "+key);
            for(String family:new String[]{"guide","vision","trail","pulse","flow","rewind"}){
                check(!V119Strings.get(lang,"skill_"+family).isBlank(),"localized family name "+lang+" "+family);
                String detail=V119Strings.get(lang,"skill_"+family+"_detail",17,23,29);
                check(detail.contains("29")&&!detail.contains("%"),"all family descriptions include exact energy and format safely "+lang+" "+family);
                if(!family.equals("vision"))check(detail.contains("17"),"family uses real amount "+lang+" "+family);
                if(family.equals("guide")||family.equals("vision")||family.equals("trail"))check(detail.contains("23"),"timed family uses real seconds "+lang+" "+family);
            }
        }
        equal(V119Strings.get("fr-FR","squad"),V119Strings.get("fr","squad"),"regional locale normalizes");
        equal(V119Strings.get("unknown","squad"),"Squad","unsupported locale falls back to English");
        check(!V119Strings.get("en","combo_detail").toLowerCase(Locale.ROOT).contains("three"),"combo never implies all three companions required");
        check(V119Strings.get("en","skill_vision_detail",1,6,2).contains("does not certify"),"Vision never claims certified solvability");
    }
    public static void main(String[] args)throws Exception {initialAndOwned();ownershipAndDamagedSave();failures();interleavedEconomy();allCatalogMembersAndConcurrency();copy();System.out.println("PASS "+checks+" v1.19 squad storage, ownership, failed-commit recovery and six-language copy checks");}
}
