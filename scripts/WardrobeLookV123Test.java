import android.content.Context;
import android.content.SharedPreferences;
import com.androidhit.lumadrift.data.*;
import java.nio.file.*;
import java.util.*;

/** Actual public GameStore application with failed-commit and cold-process doubles. */
public final class WardrobeLookV123Test {
    private static int checks,refusals,failures;
    private static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
    static final class CountPrefs implements SharedPreferences {
        final Map<String,Object> values=new HashMap<>(),disk=new HashMap<>();
        final List<Map<String,Object>> writes=new ArrayList<>();
        boolean fail,applyBeforeFailure;
        public Map<String,?> getAll(){return new HashMap<>(values);}
        public boolean contains(String key){return values.containsKey(key);}
        public int getInt(String key,int fallback){return (Integer)values.getOrDefault(key,fallback);}
        public String getString(String key,String fallback){return (String)values.getOrDefault(key,fallback);}
        public boolean getBoolean(String key,boolean fallback){return (Boolean)values.getOrDefault(key,fallback);}
        public Editor edit(){
            Map<String,Object> changed=new HashMap<>();
            return new Editor(){
                public Editor putInt(String key,int value){changed.put(key,value);return this;}
                public Editor putString(String key,String value){changed.put(key,value);return this;}
                public Editor putBoolean(String key,boolean value){changed.put(key,value);return this;}
                public boolean commit(){
                    writes.add(new HashMap<>(changed));
                    if(fail){fail=false;if(applyBeforeFailure)values.putAll(changed);return false;}
                    values.putAll(changed);disk.putAll(changed);return true;
                }
            };
        }
        void fail(boolean memoryFirst){fail=true;applyBeforeFailure=memoryFirst;}
        CountPrefs reboot(){CountPrefs result=new CountPrefs();result.values.putAll(disk);result.disk.putAll(disk);return result;}
    }
    static final class Ctx extends Context {
        final Map<String,CountPrefs> files=new HashMap<>();
        public SharedPreferences getSharedPreferences(String name,int mode){return files.computeIfAbsent(name,key->new CountPrefs());}
        CountPrefs save(){return files.get("luma_drift_save_v1");}
        CountPrefs counted(){return save();}
        Ctx reboot(){Ctx result=new Ctx();files.forEach((key,value)->result.files.put(key,value.reboot()));return result;}
    }
    private static GameStore store(Ctx context){return new GameStore(context,null,()->1793059200000L);}
    private static Map<CosmeticCatalog.Slot,String> current(GameStore store){
        Map<CosmeticCatalog.Slot,String> result=new EnumMap<>(CosmeticCatalog.Slot.class);
        for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())result.put(slot,store.equippedCosmetic(slot));return result;
    }
    private static Map<CosmeticCatalog.Slot,String> purchased(GameStore store){
        for(int n=0;n<4;n++)check(store.grantMilestone("wardrobe-v123-test-funding-"+n,500)==500,"public bounded funding receipt succeeds");
        Map<CosmeticCatalog.Slot,String> result=new EnumMap<>(CosmeticCatalog.Slot.class);
        for(String id:new String[]{"hat_cupid","dress_witch","shoes_cupid","gloves_witch"}){
            check(store.purchaseCosmetic(id),"owned sample acquired through public price/debit API");result.put(CosmeticCatalog.find(id).slot,id);
        }
        return result;
    }
    private static void rejected(GameStore store,Ctx context,Map<CosmeticCatalog.Slot,String> draft,boolean original){
        Map<String,Object> before=new HashMap<>(context.save().disk);context.counted().writes.clear();
        check(!store.applyCosmeticLook123(draft,original),"invalid complete draft refuses");
        check(context.save().disk.equals(before)&&context.counted().writes.isEmpty(),"refusal never writes any selected slot or unrelated value");refusals++;
    }
    private static void normal(){
        Ctx context=new Ctx();GameStore store=store(context);Map<CosmeticCatalog.Slot,String> starters=current(store),look=purchased(store);
        rejected(store,context,null,false);rejected(store,context,new HashMap<>(),false);
        Map<CosmeticCatalog.Slot,String> bad=new HashMap<>(look);bad.remove(CosmeticCatalog.Slot.GLOVES);rejected(store,context,bad,false);
        bad=new HashMap<>(look);bad.put(CosmeticCatalog.Slot.HAT,"dress_witch");rejected(store,context,bad,false);
        bad=new HashMap<>(look);bad.put(CosmeticCatalog.Slot.HAT,"unknown");rejected(store,context,bad,false);
        bad=new HashMap<>(look);bad.put(CosmeticCatalog.Slot.HAT,"hat_aurora");rejected(store,context,bad,false);rejected(store,context,bad,true);
        bad=new HashMap<>(look);bad.put(CosmeticCatalog.Slot.HAT,null);rejected(store,context,bad,false);
        bad=new HashMap<>(look);bad.remove(CosmeticCatalog.Slot.HAT);bad.put(null,"hat_cupid");rejected(store,context,bad,false);
        int coins=store.coins(),gearBonus=store.avatarLoadout().winBonusCoins();Map<String,Object> before=new HashMap<>(context.save().disk);context.counted().writes.clear();
        check(store.applyCosmeticLook123(look,false),"one explicit valid draft applies");
        check(context.counted().writes.size()==1&&context.counted().writes.get(0).size()==5,"all four slots and presentation flag use one five-key commit");
        Set<String> keys=new HashSet<>(context.counted().writes.get(0).keySet());
        for(String key:before.keySet())if(!keys.contains(key))check(Objects.equals(before.get(key),context.save().disk.get(key)),"applying look preserves unrelated persisted value "+key);
        check(current(store).equals(look)&&!store.usesOriginalCompanionCostume(),"all four visible items applied together");
        check(store.coins()==coins&&store.avatarLoadout().winBonusCoins()==gearBonus,"applying owned look has no currency/gameplay effect");
        GameStore cold=store(context.reboot());check(current(cold).equals(look)&&!cold.usesOriginalCompanionCostume(),"complete look survives process reconstruction");
        check(store.applyCosmeticLook123(look,true)&&store.usesOriginalCompanionCostume()&&current(store).equals(look),"original costume preserves all four valid stored choices");
        check(store.applyCosmeticLook123(starters,false)&&current(store).equals(starters),"starter look can be restored explicitly");
    }
    private static void failed(boolean memoryFirst){
        Ctx context=new Ctx();GameStore store=store(context);Map<CosmeticCatalog.Slot,String> look=purchased(store),accepted=new EnumMap<>(look);
        Map<String,Object> disk=new HashMap<>(context.save().disk);int coins=store.coins();
        context.save().fail(memoryFirst);boolean threw=false;
        try{store.applyCosmeticLook123(look,false);}catch(GameStore.LocalSaveException expected){threw=true;failures++;}
        check(threw&&context.save().disk.equals(disk),"failed write has typed retry boundary and no partial durable look");
        GameStore coldBeforeRetry=store(context.reboot());check(coldBeforeRetry.usesOriginalCompanionCostume()&&!current(coldBeforeRetry).equals(accepted),"process death before retry sees entire old durable look");
        look.put(CosmeticCatalog.Slot.HAT,"hat_aurora");
        context.save().fail(memoryFirst);threw=false;
        try{store.retryPendingLocalSave();}catch(GameStore.LocalSaveException expected){threw=true;failures++;}
        check(threw&&context.save().disk.equals(disk),"second failed commit cannot partially apply accepted draft");
        store.retryPendingLocalSave();check(current(store).equals(accepted)&&!store.usesOriginalCompanionCostume(),"retry uses accepted immutable values despite caller draft mutation");
        check(store.coins()==coins&&store(context.reboot()).coins()==coins,"retry cannot debit or grant currency");
        GameStore cold=store(context.reboot());check(current(cold).equals(accepted)&&!cold.usesOriginalCompanionCostume(),"retry commits complete selection and flag before cold reconstruction");
    }
    public static void main(String[] args)throws Exception{
        normal();failed(false);failed(true);
        String report="{\n  \"passed\":true,\n  \"assertions\":"+checks+",\n  \"invalid_drafts_refused_without_write\":"+refusals+",\n  \"failed_commits_exercised\":"+failures+",\n  \"scope\":\"Real GameStore complete-look API, four owned slots plus original-costume flag in one transaction; invalid/no ownership/slot mismatch/partial/null refusal, no unrelated currency/gameplay mutation, cold reconstruction and repeated disk failures with both memory-before-disk modes. Host persistence doubles, not physical storage or native wardrobe UI acceptance.\"\n}\n";
        Path out=Path.of(args[0]);Files.createDirectories(out.getParent());Files.writeString(out,report);System.out.print(report);
    }
}
