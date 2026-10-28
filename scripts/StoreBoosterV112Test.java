import com.androidhit.lumadrift.data.*;
import java.nio.file.*;
import java.util.*;

/** Actual public store calls with the existing preference refusal double; no device/provider seeding. */
public final class StoreBoosterV112Test {
    private static int checks,fundingRequest;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean refused(Runnable action){try{action.run();return false;}catch(GameStore.LocalSaveException expected){return true;}}
    private static RewardsCosmeticsV17Test.Clock clock(){return new RewardsCosmeticsV17Test.Clock("2026-10-28");}
    private static GameStore store(RewardsCosmeticsV17Test.Ctx c){return new GameStore(c,null,clock());}
    private static GameStore unlocked(RewardsCosmeticsV17Test.Ctx c,Catalog.BoosterItem item){
        GameStore s=store(c);for(int level=1;level<item.unlockLevel;level++)check(s.completeLevel(level,3)>0,"public progression "+item.id+" "+level);
        check(s.boosterUnlocked(item.id),"actual milestone unlock "+item.id);return s;
    }
    private static void fund(GameStore s,String id,int price){int request=fundingRequest++,n=0;while(s.coins()<price)check(s.grantMilestone("v112:"+id+":"+request+":"+n++,Math.min(500,price-s.coins()))>0,"explicit earned funding");}
    private static int disk(RewardsCosmeticsV17Test.Ctx c,String key){return (Integer)c.save().disk.getOrDefault(key,0);}

    private static void ordinaryPurchases(){
        for(Catalog.BoosterItem item:Catalog.BOOSTERS){
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=unlocked(c,item);fund(s,item.id,s.boosterPrice(item.id));
            int before=s.coins(),stock=s.boosterStock(item.id),price=s.boosterPrice(item.id);
            check(s.purchaseBooster(item.id),"ordinary purchase succeeds "+item.id);
            check(s.coins()==before-price&&s.boosterStock(item.id)==stock+item.amount,"exact ordinary debit and grant "+item.id);
            GameStore cold=store(c.reboot());check(cold.coins()==before-price&&cold.boosterStock(item.id)==stock+item.amount,"ordinary purchase durable "+item.id);
        }
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);int before=s.coins();
        check(!s.purchaseBooster("unknown")&&!s.purchaseBooster(null)&&!s.purchaseBooster("trail"),"invalid and locked purchase rejected");
        check(s.coins()==before,"rejected purchase has no debit");
        while(s.coins()>=s.boosterPrice("hint"))check(s.purchaseBooster("hint"),"spend existing earned balance");
        int coins=s.coins(),stock=s.boosterStock("hint");check(!s.purchaseBooster("hint")&&s.coins()==coins&&s.boosterStock("hint")==stock,"underfunded purchase is read only");
    }
    private static void failedPurchaseAndRetry(){
        for(Catalog.BoosterItem item:Catalog.BOOSTERS)for(boolean memory:new boolean[]{false,true}){
            String label=item.id+" memory-before-disk="+memory;
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore first=unlocked(c,item),second=store(c);fund(first,item.id,first.boosterPrice(item.id));
            int before=first.coins(),stock=first.boosterStock(item.id),price=first.boosterPrice(item.id);
            c.save().fail(memory);check(refused(()->first.purchaseBooster(item.id)),"failed purchase never reports success "+label);
            check(disk(c,"coins")==before&&disk(c,"booster_"+item.id)==stock,"failed disk has no partial debit or grant "+label);
            check(c.save().getInt("coins",-1)==(memory?before-price:before),"known memory-before-disk model observed "+label);
            c.save().fail(memory);check(refused(second::retryPendingLocalSave),"repeated refusal remains visible "+label);
            check(disk(c,"coins")==before&&disk(c,"booster_"+item.id)==stock,"repeated refusal retains old durable pair "+label);
            c.save().fail(memory);check(refused(()->second.boosterStock(item.id)),"guarded stock read cannot expose unpersisted grant "+label);
            second.retryPendingLocalSave();
            check(disk(c,"coins")==before-price&&disk(c,"booster_"+item.id)==stock+item.amount,"retry persists one exact pair "+label);
            for(int retry=0;retry<4;retry++)second.retryPendingLocalSave();
            check(second.coins()==before-price&&second.boosterStock(item.id)==stock+item.amount,"repeated retry does not double debit or grant "+label);
            GameStore cold=store(c.reboot());check(cold.coins()==before-price&&cold.boosterStock(item.id)==stock+item.amount,"recovered pair survives cold recreation "+label);
            GameStore unrelated=store(new RewardsCosmeticsV17Test.Ctx());check(unrelated.coins()==180&&unrelated.boosterStock("hint")==3,"pending transaction does not cross profiles "+label);
        }
    }
    private static void processLossBeforeRetry(){
        for(Catalog.BoosterItem item:Catalog.BOOSTERS)for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore first=unlocked(c,item);fund(first,item.id,first.boosterPrice(item.id));
            int before=first.coins(),stock=first.boosterStock(item.id),price=first.boosterPrice(item.id);
            c.save().fail(memory);check(refused(()->first.purchaseBooster(item.id)),"cold failure surfaced "+item.id);
            GameStore cold=store(c.reboot());check(cold.coins()==before&&cold.boosterStock(item.id)==stock,"cold process sees entire previous durable pair "+item.id);
            check(cold.purchaseBooster(item.id),"explicit fresh purchase succeeds after lost process "+item.id);
            cold.retryPendingLocalSave();check(cold.coins()==before-price&&cold.boosterStock(item.id)==stock+item.amount,"fresh request charges and grants once "+item.id);
        }
    }
    private static void capacity(){
        Catalog.BoosterItem item=Catalog.booster("hint");RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);
        while(s.boosterStock(item.id)<=s.boosterStockLimit()-item.amount){fund(s,"capacity",s.boosterPrice(item.id));check(s.purchaseBooster(item.id),"valid pack up to capacity");}
        int coins=s.coins(),stock=s.boosterStock(item.id);check(!s.purchaseBooster(item.id)&&s.coins()==coins&&s.boosterStock(item.id)==stock,"capacity rejection cannot charge or partially grant");
        GameStore cold=store(c.reboot());check(cold.coins()==coins&&cold.boosterStock(item.id)==stock,"capacity balance durable");
    }
    public static void main(String[] args)throws Exception{
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));ordinaryPurchases();failedPurchaseAndRetry();processLossBeforeRetry();capacity();
        String report="{\n  \"passed\": true,\n  \"checks\": "+checks+",\n  \"powers\": 6,\n  \"failure_models\": [\"commit refused before memory update\", \"Android memory updated before disk refusal\"],\n  \"scope\": \"Actual store API on JVM preference doubles with public progression and earned grants; failure, exact retry, multi-handle, cold recreation and stock capacity. No Android device or payment acceptance claim.\"\n}\n";
        if(args.length>0){Path out=Path.of(args[0]);Files.createDirectories(out.getParent());Files.writeString(out,report);}
        System.out.println("PASS "+checks+" booster purchase durability checks");
    }
}
