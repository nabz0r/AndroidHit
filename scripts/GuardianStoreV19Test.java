import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Real store APIs with the established preference failure/reboot adapter. No Google/backend stand-ins. */
public final class GuardianStoreV19Test {
    static long checks,solved;static void check(boolean ok,String reason){checks++;if(!ok)throw new AssertionError(reason);}
    static GameStore store(RewardsCosmeticsV17Test.Ctx c){return new GameStore(c,null,()->1793059200000L);}
    static GameStore unlocked(RewardsCosmeticsV17Test.Ctx c,int clears){GameStore s=store(c);for(int n=1;n<=clears;n++)s.completeLevel(n,3);return s;}
    static void throwsSave(Runnable action,String reason){boolean threw=false;try{action.run();}catch(IllegalStateException expected){threw=true;}check(threw,reason);}
    static void solve(int number){GuardianEncounter e=GuardianEncounter.start(number);for(int id:e.optimalPlan())check(e.move(id).success,"real encounter plan before reward request");check(e.isSolved(),"actual solved encounter before claim");solved++;}
    static void gatesAndRewards(){RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);
        check(s.nextGuardian()==1&&!s.guardianUnlocked(1)&&!s.guardianCleared(1),"fresh pending guardian is honestly locked");int coins=s.coins();
        for(int n:new int[]{-1,0,1,2,100,101,Integer.MAX_VALUE})check(s.completeGuardian(n)==0&&s.coins()==coins&&!s.guardianCleared(n),"invalid/locked guardian refuses reward");
        for(int level=1;level<=1000;level++){
            s.completeLevel(level,3);
            if(level%10!=0){int next=level/10+1;check(!s.guardianUnlocked(next)&&s.completeGuardian(next)==0,"no early ten-clear unlock");continue;}
            int number=level/10;check(s.guardianUnlocked(number)&&s.nextGuardian()==number,"exact threshold unlock/pending order");
            int before=s.coins(),stars=s.totalStars(),completed=s.completedLevels(),unlocked=s.unlockedLevel();solve(number);
            check(s.completeGuardian(number)==100&&s.coins()==before+100&&s.guardianCleared(number),"one real 100-coin receipt");
            check(s.totalStars()==stars&&s.completedLevels()==completed&&s.unlockedLevel()==unlocked,"guardian never advances Journey or changes stars");
            check(s.completeGuardian(number)==0&&store(c).completeGuardian(number)==0&&s.coins()==before+100,"repeat and independent handle cannot duplicate");
            c=c.reboot();s=store(c);check(s.guardianCleared(number)&&s.completeGuardian(number)==0&&s.coins()==before+100,"cold receipt persists once");
        }
        check(s.nextGuardian()==100&&s.guardianCleared(100),"fully cleared list exposes final completed encounter");
    }
    static void capAndFailures(){
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=unlocked(c,10);
        c.save().values.put("coins",1_999_999_901);c.save().disk.put("coins",1_999_999_901);
        check(s.completeGuardian(1)==0&&!s.guardianCleared(1)&&s.coins()==1_999_999_901,"full wallet keeps receipt unclaimed");
        c.save().values.put("coins",1_999_999_900);c.save().disk.put("coins",1_999_999_900);
        check(s.completeGuardian(1)==100&&s.coins()==2_000_000_000&&s.guardianCleared(1),"cap boundary credits whole reward exactly");
        for(boolean memoryFirst:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();GameStore wallet=unlocked(context,10);int before=wallet.coins();context.save().fail(memoryFirst);
            throwsSave(()->wallet.completeGuardian(1),"guardian failure surfaces "+memoryFirst);
            check(context.save().disk.get("coins").equals(before)&&!context.save().disk.containsKey("guardian_v19_1"),"failed commit leaves no partial durable gift "+memoryFirst);
            GameStore other=store(context);check(other.completeGuardian(1)==0&&other.guardianCleared(1)&&other.coins()==before+100,"second handle flushes exact pending transaction "+memoryFirst);
            GameStore boot=store(context.reboot());check(boot.completeGuardian(1)==0&&boot.coins()==before+100,"recovered gift survives cold restart "+memoryFirst);
            RewardsCosmeticsV17Test.Ctx killed=new RewardsCosmeticsV17Test.Ctx();GameStore old=unlocked(killed,10);int oldCoins=old.coins();killed.save().fail(memoryFirst);
            throwsSave(()->old.completeGuardian(1),"process-death setup "+memoryFirst);GameStore after=store(killed.reboot());
            check(!after.guardianCleared(1)&&after.completeGuardian(1)==100&&after.coins()==oldCoins+100&&after.completeGuardian(1)==0,"cold retry grants once from last durable map "+memoryFirst);
        }
    }
    static void concurrent()throws Exception{
        ExecutorService workers=Executors.newFixedThreadPool(2);
        try{for(int i=0;i<24;i++){
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore a=unlocked(c,10),b=store(c);int before=a.coins();CountDownLatch start=new CountDownLatch(1);
            Future<Integer> left=workers.submit(()->{start.await();return a.completeGuardian(1);}),right=workers.submit(()->{start.await();return b.completeGuardian(1);});start.countDown();
            int x=left.get(5,TimeUnit.SECONDS),y=right.get(5,TimeUnit.SECONDS);check(x+y==100&&(x==0||y==0),"concurrent handles return exactly one 100 reward");
            check(a.coins()==before+100&&b.guardianCleared(1)&&store(c.reboot()).coins()==before+100,"concurrent currency and receipt durable");
        }}finally{workers.shutdownNow();}
    }
    static void zeroBudgetUndo(){
        GuardianEncounter encounter=GuardianEncounter.start(3);
        check(encounter.piece(0).layers==2&&encounter.move(0).peeled&&!encounter.canMove(0),"real thick rear piece peels then meets front blocker");
        while(encounter.canAct())check(encounter.move(0).blocked,"blocked rear probe exhausts its own budget");
        int work=encounter.remainingWork(),used=encounter.snapshot().used;
        check(encounter.remainingActions()==0&&encounter.undo(),"Undo is available at zero actions");
        check(encounter.remainingActions()==1&&encounter.snapshot().used==used-1&&encounter.remainingWork()==work+1&&encounter.piece(0).layers==2,"zero-budget Undo returns only productive hit and its one action");
        GuardianEncounter restored=GuardianEncounter.restore(encounter.encode());check(restored!=null&&restored.remainingActions()==1&&restored.remainingWork()==encounter.remainingWork(),"zero-budget recovery save retains failed charges");
    }
    static void styles(){RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=unlocked(c,20);
        check(s.avatarStyle().equals("plush_classic")&&AvatarStyles.variantCount()==444&&AvatarStyles.all().size()==12,"twelve free treatments, stable default and 444 combinations including thirteen guests");
        Map<String,Object> before=new HashMap<>(c.save().values);AvatarLoadout loadout=s.avatarLoadout();int coins=s.coins(),price=s.boosterPrice(Catalog.HINT);
        Set<String> ids=new HashSet<>(),variants=new HashSet<>(),legacyVariants=new HashSet<>(),guestVariants=new HashSet<>();
        for(AvatarStyles.Style style:AvatarStyles.all()){
            check(ids.add(style.id)&&s.equipAvatarStyle(style.id)&&s.avatarStyle().equals(style.id),"known style saved "+style.id);
            check(store(c).avatarStyle().equals(style.id)&&store(c.reboot()).avatarStyle().equals(style.id),"style survives cross handle and cold recreation");
            for(Catalog.CharacterItem character:Catalog.CHARACTERS){String variant=AvatarStyles.variantId(character,style.id);check(variants.add(variant),"unique composite visual ID");(character.id.startsWith("lol_")?guestVariants:legacyVariants).add(variant);}
            check(s.coins()==coins&&s.boosterPrice(Catalog.HINT)==price&&s.avatarLoadout().winBonusCoins()==loadout.winBonusCoins()&&s.avatarLoadout().packDiscountCoins()==loadout.packDiscountCoins()&&s.avatarLoadout().blockedGracePerRun()==loadout.blockedGracePerRun(),"style has zero currency/gameplay effect");
            Map<String,Object> current=new HashMap<>(c.save().values);current.remove("avatar_style_v19");check(current.equals(before),"style does not grant companion ownership, stock, wallet or progression");
            for(String language:new String[]{"en","fr","de","es","pt","it"})check(!style.name(language).isBlank(),"explicit localized style name");
        }
        check(legacyVariants.size()==288&&guestVariants.size()==156&&variants.size()==AvatarStyles.variantCount(),"all 288 original combinations remain available alongside 156 guest combinations");
        String active=s.avatarStyle();for(String bad:new String[]{null,"","admin","plush_classic ","../../../key","nova:ice"})check(!s.equipAvatarStyle(bad)&&s.avatarStyle().equals(active),"invalid style is not equip permission");
        c.save().values.put("avatar_style_v19","invalid-saved-id");check(s.avatarStyle().equals("plush_classic"),"corrupt saved style falls back safely");
        for(boolean memoryFirst:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx f=new RewardsCosmeticsV17Test.Ctx();GameStore wallet=store(f);f.save().fail(memoryFirst);
            throwsSave(()->wallet.equipAvatarStyle("ice"),"style failure surfaced "+memoryFirst);check(!f.save().disk.containsKey("avatar_style_v19"),"failed style leaves old durable treatment");
            check(store(f).avatarStyle().equals("ice")&&store(f.reboot()).avatarStyle().equals("ice"),"pending style flushes unchanged without debit");
        }
    }
    static Object[] args(String key){switch(key){case "claim_gift":case "trial":return new Object[]{"Hint"};case "gift_progress":return new Object[]{3,5,"Hint"};case "score":case "guardian":case "guardian_locked":return new Object[]{13};case "guardian_work":return new Object[]{4,12};case "guardian_piece":return new Object[]{2,"Right",2," · one cell"};default:return new Object[0];}}
    static void strings(){Set<String> keys=V19Strings.keys();check(keys.size()>=30,"new onboarding/guardian vocabulary present");for(String language:new String[]{"en","fr","de","es","pt","it"})for(String key:keys){
            String value=V19Strings.get(language,key,args(key));check(!value.isBlank()&&!value.matches(".*%[ds].*"),"translated/fully substituted V19 key "+language+"/"+key);
        }
        check(V19Strings.get("fr-FR","guardian",4).equals(V19Strings.get("fr","guardian",4)),"regional locale normalized");
        check(V19Strings.get("xx","guardian",4).equals(V19Strings.get("en","guardian",4)),"unknown locale explicit fallback");
        boolean threw=false;try{V19Strings.get("fr","missing");}catch(IllegalArgumentException expected){threw=true;}check(threw,"unknown copy key fails visibly");
    }
    public static void main(String[] args)throws Exception{Path out=Path.of(args[0]);Files.createDirectories(out);long start=System.nanoTime();
        gatesAndRewards();capAndFailures();concurrent();zeroBudgetUndo();styles();strings();
        String result="{\n  \"passed\":true,\n  \"assertions\":"+checks+",\n  \"encounters_actually_solved_before_reward_requests\":"+solved+",\n  \"concurrent_two_handle_trials\":24,\n  \"localized_languages\":6,\n  \"visual_variants\":444,\n  \"legacy_visual_variants\":288,\n  \"elapsed_host_seconds\":"+((System.nanoTime()-start)/1e9)+",\n  \"scope\":\"Real GameStore, GuardianEncounter, AvatarStyles and V19Strings with established Android preference doubles. completeGuardian is a local caller contract after a solved board, not server purchase/anti-cheat verification. No Google or social service validation.\"\n}\n";
        Files.writeString(out.resolve("summary.json"),result);System.out.println(result);
    }
}
