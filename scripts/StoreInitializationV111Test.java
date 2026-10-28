import com.androidhit.lumadrift.data.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/** First preference commit failures using actual constructors and public store operations. */
public final class StoreInitializationV111Test {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean refused(Runnable action){try{action.run();return false;}catch(GameStore.LocalSaveException expected){return true;}}
    private static RewardsCosmeticsV17Test.Ctx context(){RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();c.getSharedPreferences("luma_drift_save_v1",0);return c;}
    private static GameStore store(RewardsCosmeticsV17Test.Ctx c){return new GameStore(c,null,new RewardsCosmeticsV17Test.Clock("2026-10-28"));}
    public static void main(String[] args)throws Exception{
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=context();c.save().fail(memory);GameStore first=store(c);
            check(c.save().disk.isEmpty(),"constructor failure leaves all initial fields absent on disk");
            c.save().fail(memory);check(refused(first::coins),"first balance read surfaces repeatedly refused initialization");
            c.save().fail(memory);GameStore second=store(c);
            check(c.save().disk.isEmpty(),"new constructor may defer only the exact refused transaction");
            c.save().fail(memory);check(refused(second::equippedSkin),"first syncStyle selection read exposes the unresolved initialization");
            check(c.save().disk.isEmpty(),"no initial receipt/stock/balance is partly durable after repeated failure");
            second.retryPendingLocalSave();check(first.coins()==180&&second.coins()==180,"same and second handles observe exactly the original initial balance");
            check(Boolean.TRUE.equals(c.save().disk.get("initialized"))&&Boolean.TRUE.equals(c.save().disk.get("tutorial_v18_fresh_profile")),"initialization and first-arrival flag become durable together");
            check(first.boosterStock("hint")==3&&first.boosterStock("vision")==3&&first.boosterStock("flow")==2,"initial stock remains exact rather than accumulated");
            check(first.equippedCharacter().equals("nova")&&first.equippedSkin().equals("lumina")&&first.ownsCharacter("nova")&&first.ownsSkin("lumina"),"valid initial choices and ownership survive recovery");
            check(first.purchaseCharacter("miko")&&first.equipCharacter("miko")&&first.coins()==20,"first real purchase consumes160 only after durable initialization");
            GameStore another=store(c);check(another.coins()==20&&another.equippedCharacter().equals("miko")&&!another.purchaseCharacter("miko"),"new handle cannot recreate the180 grant or a duplicate purchase");
            GameStore cold=store(c.reboot());check(cold.coins()==20&&cold.equippedCharacter().equals("miko")&&cold.ownsCharacter("miko"),"cold reconstruction retains the20 balance and owned selection");

            RewardsCosmeticsV17Test.Ctx interrupted=context();interrupted.save().fail(memory);store(interrupted);
            RewardsCosmeticsV17Test.Ctx coldContext=interrupted.reboot();GameStore freshCold=store(coldContext);check(freshCold.coins()==180&&freshCold.purchaseCharacter("miko")&&freshCold.coins()==20,"cold restart before any initialization commit gets one legitimate initial balance");
            check(store(coldContext.reboot()).coins()==20,"a second cold restart cannot reset that legitimate purchase balance");

            RewardsCosmeticsV17Test.Ctx blocked=context();blocked.save().fail(memory);GameStore pending=store(blocked);
            blocked.save().fail(memory);check(refused(()->pending.purchaseCharacter("miko")),"purchase cannot bypass a refused initial transaction");
            blocked.save().fail(memory);check(refused(()->pending.completeLevel(1,3)),"campaign reward cannot bypass refused initial transaction");
            blocked.save().fail(memory);check(refused(()->pending.grantMilestone("v111:init-guard",100)),"another grant cannot bypass refused initial transaction");
            check(blocked.save().disk.isEmpty(),"blocked actions expose no partly durable purchase or campaign reward");
            check(pending.coins()==180&&!pending.ownsCharacter("miko")&&pending.stars(1)==0,"recovery initializes once without replaying denied actions");
        }
        RewardsCosmeticsV17Test.Ctx raced=context();CountDownLatch start=new CountDownLatch(1);AtomicInteger purchases=new AtomicInteger(),errors=new AtomicInteger();List<Thread> workers=new ArrayList<>();
        for(int index=0;index<24;index++){Thread worker=new Thread(()->{try{start.await();GameStore s=store(raced);if(s.purchaseCharacter("miko"))purchases.incrementAndGet();}catch(Throwable error){errors.incrementAndGet();}});workers.add(worker);worker.start();}
        start.countDown();for(Thread worker:workers)worker.join();GameStore finalStore=store(raced);
        check(errors.get()==0&&purchases.get()==1&&finalStore.coins()==20,"24 concurrent first handles initialize once and debit one purchase");
        check(store(raced.reboot()).coins()==20,"concurrent first-handle balance remains durable");
        boolean otherError=false;try{new GameStore(raced,null,null);}catch(IllegalArgumentException expected){otherError=true;}check(otherError,"constructor does not swallow other exception classes");
        String json="{\n  \"passed\": true,\n  \"assertions\": "+checks+",\n  \"memory_before_disk_modes\": [false,true],\n  \"concurrent_first_handles\": 24,\n  \"scope\": \"Actual GameStore first constructors and public reads/actions with explicit preference failure and cold-reconstruction doubles; no Android profile or physical-flash injection.\"\n}\n";
        if(args.length>0){Path path=Path.of(args[0]);Files.createDirectories(path.getParent());Files.writeString(path,json);}
        System.out.println("PASS "+checks+" first-initialization durability checks");
    }
}
