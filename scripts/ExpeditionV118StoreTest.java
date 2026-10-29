import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

/** Actual GameStore atomicity on durable JVM preferences. Seed/count fixtures are explicit; no native-UI claim. */
public final class ExpeditionV118StoreTest {
    private static int checks,certificates,certificateActions,fundingSerial;
    private static final String RUN_KEY="expedition_v118_run",SEED_KEY="summon_seed_"+SummonCatalog.VERSION;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static RewardsCosmeticsV17Test.Clock clock(){return new RewardsCosmeticsV17Test.Clock("2026-10-29");}
    private static GameStore store(RewardsCosmeticsV17Test.Ctx context){return new GameStore(context,null,clock());}
    private static void fund(GameStore s){while(s.coins()<5000)check(s.grantMilestone("v118:fund:"+fundingSerial++,500)>0,"earned test funding");}
    private static void refused(Runnable action,String message){boolean typed=false;try{action.run();}catch(GameStore.LocalSaveException expected){typed=true;}check(typed,message);}
    private static String hash(String value)throws Exception{byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));StringBuilder out=new StringBuilder();for(byte b:bytes)out.append(String.format(Locale.ROOT,"%02x",b&255));return out.toString();}
    private static String requestFor(String seed,SummonCatalog.Banner banner,String target,String prefix){for(int n=0;n<100000;n++){String id=prefix+":"+n;if(SummonCatalog.draw(seed,banner,id).id.equals(target))return id;}throw new AssertionError("deterministic request missing");}
    private static String copyKey(String id){return "companion_copies_v118_"+id;}
    private static void assertDisk(RewardsCosmeticsV17Test.Ctx c,Map<String,Object> before,String label){check(c.save().disk.equals(before),"no partial durable mutation after "+label);}
    private static void certificate(int number){Level real=RelayCatalog.get(number);Board actual=new Board(real);for(int action:real.actionSolution){check(actual.performAction(action).success,"actual selected U action remains legal");certificateActions++;}check(actual.isSolved(),"actual selected U board solved");certificates++;}
    private static SortedMap<String,String> sourceHashes()throws Exception{
        Path root=Path.of(System.getProperty("luma.test.root",".")).toAbsolutePath().normalize();SortedMap<String,String> hashes=new TreeMap<>();
        for(String directory:new String[]{"app/src/main/java/com/androidhit/lumadrift/data","app/src/main/java/com/androidhit/lumadrift/engine","scripts/test-support/android"})try(java.util.stream.Stream<Path> files=Files.walk(root.resolve(directory))){for(Path file:(Iterable<Path>)files.filter(p->p.toString().endsWith(".java"))::iterator)hashes.put(root.relativize(file).toString(),hash(Files.readString(file)));}
        for(String file:new String[]{"scripts/ExpeditionV118StoreTest.java","scripts/RewardsCosmeticsV17Test.java","scripts/test-expedition-v118-store.sh"})hashes.put(file,hash(Files.readString(root.resolve(file))));return hashes;
    }
    private static String sourceManifest(SortedMap<String,String> hashes){StringJoiner entries=new StringJoiner(",\n","{\n","\n}\n");for(Map.Entry<String,String> entry:hashes.entrySet())entries.add("  \""+entry.getKey()+"\": \""+entry.getValue()+"\"");return entries.toString();}
    private static ExpeditionRun finish(GameStore s,String id){
        ExpeditionRun run=s.expeditionRun();check(run!=null&&run.id.equals(id),"actual stored active run");
        while(!run.finished()){
            if(run.selectedOption()==null){ExpeditionCatalog.Option option=run.options().get(0);run=s.chooseExpedition(id,option.id);check(run!=null&&run.selectedOption()!=null,"route choice persisted before board");}
            int stage=run.completedStages,board=run.selectedOption().boardNumber;String victory=id+":stage:"+stage;certificate(board);
            check(s.completeExpeditionStage(id,victory,board+1000,3)==null,"mismatched board cannot progress actual store");
            run=s.completeExpeditionStage(id,victory,board,3);check(run!=null&&run.completedStages==stage+1,"one solved-board receipt advances actual store once");
            check(s.completeExpeditionStage(id,victory,board,3).completedStages==stage+1,"same stage receipt never advances twice");
        }
        return run;
    }
    private static void migrationAndPaid()throws Exception{
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);fund(s);
        check(s.purchaseCharacter("miko"),"real direct legacy ownership fixture");int coins=s.coins();
        // Construct an actual v1.16 durable receipt, with no v1.18 copy key, as an upgrade fixture.
        String old="legacy:duplicate",receiptKey="summon_"+SummonCatalog.VERSION+"_"+hash(old);
        c.save().edit().putString(SEED_KEY,"v118-store-seed").putString(receiptKey,"AVATARS:miko:1:45").commit();
        Map<String,Object> before=new HashMap<>(c.save().disk);
        check(s.companionCopies("nova")==1&&s.companionCopies("miko")==1&&s.companionCopies("orbit")==0,"all preexisting ownership migrates to one copy only");
        check(s.drawSummon(SummonCatalog.Banner.AVATARS,old).replayed&&s.companionCopies("miko")==1&&s.coins()==coins,"old duplicate receipt does not backfill a copy or refund twice");
        check(c.save().disk.equals(before),"migration reads do not rewrite historical receipt or save");
        String duplicate=requestFor("v118-store-seed",SummonCatalog.Banner.AVATARS,"miko","paid:duplicate");
        GameStore.SummonResult result=s.drawSummon(SummonCatalog.Banner.AVATARS,duplicate);
        check(result.duplicate&&result.chargedCoins==180&&result.compensationCoins==45,"new paid duplicate retains old price and refund");
        check(s.coins()==coins-180+45&&s.companionCopies("miko")==2&&s.companionStats("miko").level==2,"new paid duplicate grants exactly one persistent copy");
        int balance=s.coins();check(s.drawSummon(SummonCatalog.Banner.AVATARS,duplicate).chargedCoins==0&&s.companionCopies("miko")==2&&s.coins()==balance,"same paid request has no second debit or copy");
        String discovery=requestFor("v118-store-seed",SummonCatalog.Banner.AVATARS,"orbit","paid:new");
        GameStore.SummonResult first=s.drawSummon(SummonCatalog.Banner.AVATARS,discovery);
        check(!first.duplicate&&s.companionCopies("orbit")==1,"first paid discovery starts at one copy");
        Map<String,Integer> counts=new HashMap<>();for(CompanionProgression.Definition d:CompanionProgression.all())counts.put(d.id,s.companionCopies(d.id));
        GameStore.SummonResult outfit=s.drawSummon(SummonCatalog.Banner.SIGNATURES,"paid:outfit");check(outfit!=null,"signature draw still available");
        for(CompanionProgression.Definition d:CompanionProgression.all())check(s.companionCopies(d.id)==counts.get(d.id),"outfit receipt cannot evolve a companion");
        GameStore cold=store(c.reboot());check(cold.companionCopies("miko")==2&&cold.companionCopies("orbit")==1&&cold.summonReceipt(old).compensationCoins==45,"copies and historical receipts survive cold recreation");
        check(s.companionCopies("missing")==0&&s.beginExpedition("inky",ExpeditionRun.Goal.COPIES,"nova","locked")==null,"unknown identities and unavailable prototype signatures refused");
    }
    private static void fullRun(){
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);int coins=s.coins();
        check(s.claimExpedition("main:run")==null,"no payout without complete route");
        ExpeditionRun run=s.beginExpedition("nova",ExpeditionRun.Goal.COPIES,"nova","main:run");check(run!=null&&run.companionLevel==1&&s.coins()==coins,"earned-play run starts without charge with level snapshot");
        check(s.beginExpedition("nova",ExpeditionRun.Goal.COPIES,"nova","main:other")==null,"another request cannot overwrite an active run");
        String option=run.options().get(0).id;run=s.useExpeditionSkill(run.id,option);check(run!=null&&run.revealed(option)&&run.skillRemaining()==0,"Nova skill persists exactly one spent charge");
        check(s.useExpeditionSkill(run.id,option)==null,"same reveal cannot spend or duplicate skill use");
        GameStore cold=store(c.reboot());check(cold.expeditionRun().encode().equals(run.encode()),"run and skill resume exactly after cold recreation");
        run=finish(s,run.id);String discovery=SummonCatalog.draw("expedition:"+run.seed,SummonCatalog.Banner.AVATARS,run.id).avatarId;
        int targetBefore=s.companionCopies("nova"),discoveryBefore=s.companionCopies(discovery);
        GameStore.ExpeditionReward reward=s.claimExpedition(run.id);check(reward!=null&&reward.targetId.equals("nova")&&reward.discoveryId.equals(discovery),"announced target and deterministic published-pool discovery granted");
        check(s.companionCopies("nova")==targetBefore+1&&s.companionCopies(discovery)==discoveryBefore+1,"one targeted copy and one discovery copy, separately");
        check(reward.coins==0&&s.coins()==coins&&s.expeditionsCompleted()==1&&s.expeditionRun().claimed,"claim, ownership, count and copy changes commit together");
        int copies=s.companionCopies("nova");check(s.claimExpedition(run.id).discoveryId.equals(discovery)&&s.companionCopies("nova")==copies&&s.expeditionsCompleted()==1,"claimed receipt replay does not pay twice");
        ExpeditionRun replay=s.beginExpedition("nova",ExpeditionRun.Goal.COPIES,"nova",run.id);check(replay==null||replay.claimed,"reusing a claimed run identity cannot reset expedition progress");
        GameStore restarted=store(c.reboot());check(restarted.expeditionRun().claimed&&restarted.expeditionReward(run.id).discoveryId.equals(discovery)&&restarted.companionCopies("nova")==2,"cold recreation preserves claim receipt and both copy grants");
    }
    private static void fixtureRun(RewardsCosmeticsV17Test.Ctx c,String id,String target,int count){
        long seed=0;while(!SummonCatalog.draw("expedition:"+seed,SummonCatalog.Banner.AVATARS,id).avatarId.equals(target))seed++;
        ExpeditionRun run=ExpeditionRun.start(id,seed,"nova",1,ExpeditionRun.Goal.COPIES,target);
        c.save().edit().putBoolean("owned_character_"+target,true).putInt(copyKey(target),count).putString(RUN_KEY,run.encode()).commit();
    }
    private static void sameIdentityAndCap(){
        for(int initial:new int[]{1,91,92}){
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);String id="same:"+initial;fixtureRun(c,id,"miko",initial);
            finish(s,id);int coins=s.coins();GameStore.ExpeditionReward reward=s.claimExpedition(id);
            check(reward.targetBefore==initial&&reward.targetAfter==Math.min(92,initial+1),"target leg reports its actual one-copy transition");
            check(reward.discoveryBefore==Math.min(92,initial+1)&&reward.discoveryAfter==Math.min(92,initial+2),"same-identity discovery starts after targeted leg");
            check(s.companionCopies("miko")==Math.min(92,initial+2),"same identity grants two copies cumulatively without overwriting");
            int overflow=initial==1?0:initial==91?45:90;check(reward.coins==overflow&&s.coins()==coins+overflow,"maximum converts each surplus leg under published prototype policy");
            check(s.claimExpedition(id).coins==overflow&&s.coins()==coins+overflow,"surplus replay cannot mint compensation twice");
            check(store(c.reboot()).companionCopies("miko")==Math.min(92,initial+2),"cap and same-identity ledger survive cold creation");
        }
    }
    private static void home(){
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);
        for(int index=1;index<=4;index++){
            String id="home:"+index;ExpeditionRun run=s.beginExpedition("nova",ExpeditionRun.Goal.HOME,ExpeditionCatalog.SOUVENIR_ID,id);check(run!=null,"next home project departure");
            finish(s,id);int coins=s.coins();GameStore.ExpeditionReward reward=s.claimExpedition(id);
            check(reward.targetId.isEmpty()&&reward.targetBefore==0&&reward.targetAfter==0,"home choice does not silently award an extra targeted copy");
            check(reward.homeBefore==Math.min(12,(index-1)*4)&&reward.homeAfter==Math.min(12,index*4)&&s.expeditionHomeProgress()==reward.homeAfter,"home project progresses 4 then8 then12");
            check(reward.coins==(index==4?45:0)&&s.coins()==coins+reward.coins,"completed home project repeat has stated compensation");
            check(store(c.reboot()).expeditionHomeProgress()==Math.min(12,index*4),"home contribution persists across cold restarts");
        }
    }
    private static void paidFailure(){
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);fund(s);s.purchaseCharacter("miko");c.save().edit().putString(SEED_KEY,"paid-failure").commit();
            String request=requestFor("paid-failure",SummonCatalog.Banner.AVATARS,"miko","fail:paid");int coins=s.coins();Map<String,Object> before=new HashMap<>(c.save().disk);
            c.save().fail(memory);refused(()->s.drawSummon(SummonCatalog.Banner.AVATARS,request),"paid copy write refusal surfaced "+memory);assertDisk(c,before,"paid duplicate");
            c.save().fail(memory);refused(()->s.companionCopies("miko"),"copy reads expose repeated write refusal "+memory);
            GameStore.SummonResult recovered=s.drawSummon(SummonCatalog.Banner.AVATARS,request);
            check(recovered.replayed&&recovered.chargedCoins==0&&s.companionCopies("miko")==2&&s.coins()==coins-180+45,"pending paid copy and exact debit recover once "+memory);
            check(store(c.reboot()).companionCopies("miko")==2,"paid copy durable after recovery "+memory);
        }
    }
    private static void failures(){
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);String id="failure:"+memory;Map<String,Object> before=new HashMap<>(c.save().disk);
            c.save().fail(memory);refused(()->s.beginExpedition("nova",ExpeditionRun.Goal.COPIES,"nova",id),"departure refusal surfaced "+memory);assertDisk(c,before,"departure");
            check(store(c.reboot()).expeditionRun()==null,"failed departure absent from cold disk "+memory);
            c.save().fail(memory);refused(s::expeditionRun,"repeated pending departure refusal surfaced "+memory);
            ExpeditionRun recovered=s.expeditionRun();check(recovered!=null&&recovered.id.equals(id),"exact pending departure recovers "+memory);
            check(s.beginExpedition("nova",ExpeditionRun.Goal.COPIES,"nova",id).encode().equals(recovered.encode()),"departure retry preserves seed and route "+memory);
            String option=recovered.options().get(0).id;before=new HashMap<>(c.save().disk);c.save().fail(memory);
            refused(()->s.useExpeditionSkill(id,option),"skill refusal surfaced "+memory);assertDisk(c,before,"skill");
            check(store(c.reboot()).expeditionRun().skillRemaining()==1,"failed skill leaves cold state unchanged "+memory);
            recovered=s.expeditionRun();check(recovered.skillRemaining()==0&&recovered.revealed(option),"exact skill recovers once "+memory);
            before=new HashMap<>(c.save().disk);c.save().fail(memory);refused(()->s.chooseExpedition(id,option),"choice refusal surfaced "+memory);assertDisk(c,before,"route choice");
            check(store(c.reboot()).expeditionRun().selectedOption()==null,"failed choice does not partially select cold board "+memory);
            recovered=s.expeditionRun();int board=recovered.selectedOption().boardNumber;certificate(board);before=new HashMap<>(c.save().disk);c.save().fail(memory);
            refused(()->s.completeExpeditionStage(id,"failure:first",board,3),"victory refusal surfaced "+memory);assertDisk(c,before,"stage completion");
            check(store(c.reboot()).expeditionRun().completedStages==0,"failed stage has no cold partial advance "+memory);
            check(s.completeExpeditionStage(id,"failure:first",board,3).completedStages==1,"exact victory retry advances once "+memory);
            finish(s,id);before=new HashMap<>(c.save().disk);int coins=s.coins(),copies=s.companionCopies("nova");c.save().fail(memory);
            refused(()->s.claimExpedition(id),"full reward refusal surfaced "+memory);assertDisk(c,before,"claim");
            GameStore cold=store(c.reboot());check(!cold.expeditionRun().claimed&&cold.expeditionReward(id)==null&&cold.companionCopies("nova")==copies&&cold.coins()==coins&&cold.expeditionsCompleted()==0,"no partial durable claim/copy/receipt/count "+memory);
            c.save().fail(memory);refused(()->s.expeditionReward(id),"receipt read exposes repeated claim refusal "+memory);
            GameStore.ExpeditionReward reward=s.claimExpedition(id);check(reward!=null&&s.expeditionRun().claimed&&s.companionCopies("nova")==copies+1&&s.expeditionsCompleted()==1,"exact pending full reward recovers once "+memory);
            check(s.claimExpedition(id).discoveryId.equals(reward.discoveryId)&&s.expeditionsCompleted()==1,"recovered reward replay stable "+memory);
            GameStore durable=store(c.reboot());check(durable.expeditionReward(id)!=null&&durable.companionCopies("nova")==copies+1&&durable.expeditionsCompleted()==1,"recovered reward fully durable "+memory);
        }
    }
    private static void corruptReceipts()throws Exception{
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);String id="corrupt:receipt";
        s.beginExpedition("nova",ExpeditionRun.Goal.COPIES,"nova",id);finish(s,id);check(s.claimExpedition(id)!=null,"valid receipt fixture paid exactly once");
        String key="expedition_v118_reward_"+hash(id),valid=c.save().getString(key,"");String[] original=valid.split("\\|",-1);
        int copies=s.companionCopies("nova"),coins=s.coins(),completed=s.expeditionsCompleted();
        int[] indexes={0,3,4,6,8,9,10};String[] values={"other:run","93","3","0","13","166","1"};
        for(int n=0;n<indexes.length;n++){
            String[] corrupt=original.clone();corrupt[indexes[n]]=values[n];c.save().edit().putString(key,String.join("|",corrupt)).commit();
            check(s.expeditionReward(id)==null,"receipt identity and bounded one-copy/home/coin transitions reject corruption field "+indexes[n]);
            check(s.claimExpedition(id)==null&&s.companionCopies("nova")==copies&&s.coins()==coins&&s.expeditionsCompleted()==completed,"corrupt claimed receipt never triggers a replacement payout");
        }
        c.save().edit().putString(key,valid).commit();check(s.expeditionReward(id)!=null&&s.claimExpedition(id)!=null&&s.companionCopies("nova")==copies,"restored exact valid receipt replays without minting copies");
    }
    private static void concurrency()throws Exception{
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore first=store(c),second=store(c);ExecutorService pool=Executors.newFixedThreadPool(2);
        try{
            List<Future<ExpeditionRun>> starts=pool.invokeAll(Arrays.asList(()->first.beginExpedition("nova",ExpeditionRun.Goal.COPIES,"nova","parallel"),()->second.beginExpedition("nova",ExpeditionRun.Goal.COPIES,"nova","parallel")));
            check(starts.get(0).get(10,TimeUnit.SECONDS).encode().equals(starts.get(1).get(10,TimeUnit.SECONDS).encode()),"concurrent departures retain one route and seed");
            ExpeditionRun active=first.expeditionRun();String option=active.options().get(0).id;first.chooseExpedition("parallel",option);int board=first.expeditionRun().selectedOption().boardNumber;certificate(board);
            List<Future<ExpeditionRun>> wins=pool.invokeAll(Arrays.asList(()->first.completeExpeditionStage("parallel","parallel:stage:0",board,3),()->second.completeExpeditionStage("parallel","parallel:stage:0",board,3)));
            check(wins.get(0).get(10,TimeUnit.SECONDS).completedStages==1&&wins.get(1).get(10,TimeUnit.SECONDS).completedStages==1,"concurrent victory receipt advances one stage");
            finish(first,"parallel");int copies=first.companionCopies("nova");
            List<Future<GameStore.ExpeditionReward>> claims=pool.invokeAll(Arrays.asList(()->first.claimExpedition("parallel"),()->second.claimExpedition("parallel")));
            GameStore.ExpeditionReward a=claims.get(0).get(10,TimeUnit.SECONDS),b=claims.get(1).get(10,TimeUnit.SECONDS);
            check(a.discoveryId.equals(b.discoveryId)&&first.companionCopies("nova")==copies+1&&second.expeditionsCompleted()==1,"two handles share one exact discovery/target/count claim");
            check(store(c.reboot()).expeditionsCompleted()==1,"concurrent claim durable exactly once");
        }finally{pool.shutdownNow();}
    }
    private static void stances(){
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);int coins=s.coins(),copies=s.companionCopies("nova");
        check(s.companionStance("nova").equals("CALM"),"Existing companion defaults to calm without granting anything");
        Map<String,Object> before=new HashMap<>(c.save().disk);
        check(!s.setCompanionStance("nova","BAD")&&!s.setCompanionStance("miko","GUARD")&&!s.setCompanionStance("unknown","CALM"),"Invalid and unowned stance edits are refused");assertDisk(c,before,"invalid stance");
        for(String stance:new String[]{"CALM","READY","GUARD","PROUD"}){check(s.setCompanionStance("nova",stance),"Owned stance applies "+stance);check(store(c.reboot()).companionStance("nova").equals(stance),"Chosen stance survives cold recreation "+stance);check(s.coins()==coins&&s.companionCopies("nova")==copies,"Stance changes neither wallet nor copies "+stance);}
        for(boolean memory:new boolean[]{false,true}){RewardsCosmeticsV17Test.Ctx ctx=new RewardsCosmeticsV17Test.Ctx();GameStore selected=store(ctx);before=new HashMap<>(ctx.save().disk);ctx.save().fail(memory);refused(()->selected.setCompanionStance("nova","GUARD"),"Stance refusal is surfaced "+memory);assertDisk(ctx,before,"failed stance");check(store(ctx.reboot()).companionStance("nova").equals("CALM"),"Failed stance leaves old disk value "+memory);check(selected.companionStance("nova").equals("GUARD"),"Exact pending stance recovers once "+memory);check(store(ctx.reboot()).companionStance("nova").equals("GUARD"),"Recovered stance durable "+memory);}
    }
    private static void guestCollection(){
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);
        for(Catalog.CharacterItem who:Catalog.CHARACTERS)if(who.id.startsWith("lol_")){fund(s);int wallet=s.coins();check(s.purchaseCharacter(who.id),"Guest unlock is a real earned-coin purchase "+who.id);check(s.coins()==wallet-who.price&&s.companionCopies(who.id)==1,"Guest starts at one copy after exact debit "+who.id);check(SummonCatalog.find(SummonCatalog.Banner.AVATARS,who.id)==null,"Guest does not alter historical banner probabilities "+who.id);}
        check(SummonCatalog.items(SummonCatalog.Banner.AVATARS).size()==23,"Historical23-entry banner remains intact");
        int wallet=s.coins();ExpeditionRun run=s.beginExpedition("nova",ExpeditionRun.Goal.COPIES,"lol_vi","guest:vi");check(run!=null&&run.targetId.equals("lol_vi")&&run.companionId.equals("nova"),"Expedition actor and owned copy target are independent");finish(s,run.id);GameStore.ExpeditionReward reward=s.claimExpedition(run.id);check(reward!=null&&reward.targetId.equals("lol_vi")&&s.companionCopies("lol_vi")==2&&s.companionStats("lol_vi").level==2,"Guest evolves through a completed four-puzzle target project");check(s.coins()==wallet&&s.companionStats("nova").level<=2,"Target project preserves wallet and does not replace the actor identity");check(s.claimExpedition(run.id).targetAfter==2&&store(c.reboot()).companionCopies("lol_vi")==2,"Guest target receipt is idempotent and survives cold recreation");
    }
    private static final String COSTUME_KEY="companion_original_costume_v118";
    private static Map<String,Object> cosmeticWrite(Map<String,Object> before,String id){
        Map<String,Object> expected=new HashMap<>(before);CosmeticCatalog.Item item=CosmeticCatalog.find(id);expected.put("cosmetic_slot_v17_"+item.slot.name(),id);expected.put(COSTUME_KEY,false);return expected;
    }
    private static SignatureOutfits.Outfit earnedSignature(GameStore s,String request){
        fund(s);GameStore.SummonResult result=s.drawSummon(SummonCatalog.Banner.SIGNATURES,request);
        check(result!=null,"Actual signature draw supplies wardrobe ownership fixture");SummonCatalog.Item item=SummonCatalog.find(result.itemId);SignatureOutfits.Outfit outfit=SignatureOutfits.find(item.outfitId);
        check(outfit!=null&&s.ownsSignatureOutfit(outfit.id),"Actual signature discovery owns all four outfit items");return outfit;
    }
    private static Map<String,Object> signatureWrite(Map<String,Object> before,SignatureOutfits.Outfit outfit){
        Map<String,Object> expected=new HashMap<>(before);for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())expected.put("cosmetic_slot_v17_"+slot.name(),outfit.item(slot));expected.put(COSTUME_KEY,false);return expected;
    }
    private static void costumeChoice(){
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);Map<String,Object> before=new HashMap<>(c.save().disk);
        check(s.usesOriginalCompanionCostume(),"Implicit starter wardrobe displays the authored companion costume by default");
        for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())check(s.equippedCosmetic(slot).equals(CosmeticCatalog.starter(slot).id),"Legacy equipped item API still returns the same implicit starter "+slot);
        assertDisk(c,before,"read-only original-costume default and legacy item reads");
        for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values()){
            RewardsCosmeticsV17Test.Ctx old=new RewardsCosmeticsV17Test.Ctx();GameStore legacy=store(old);String selected=CosmeticCatalog.starter(slot).id;
            old.save().edit().putString("cosmetic_slot_v17_"+slot.name(),selected).commit();Map<String,Object> oldDisk=new HashMap<>(old.save().disk);
            check(!legacy.usesOriginalCompanionCostume(),"An old explicit starter choice remains visible for slot "+slot);
            check(!store(old.reboot()).usesOriginalCompanionCostume()&&legacy.equippedCosmetic(slot).equals(selected),"Old explicit wardrobe choice survives cold migration "+slot);assertDisk(old,oldDisk,"non-mutating old-choice migration "+slot);
        }
        int wallet=s.coins(),copies=s.companionCopies("nova");
        for(boolean original:new boolean[]{false,true,false,true}){
            before=new HashMap<>(c.save().disk);check(s.setUseOriginalCompanionCostume(original),"Explicit costume choice is accepted "+original);before.put(COSTUME_KEY,original);
            check(c.save().disk.equals(before),"Costume choice changes exactly its own boolean and preserves all selections/progression "+original);
            check(store(c.reboot()).usesOriginalCompanionCostume()==original&&s.coins()==wallet&&s.companionCopies("nova")==copies,"Mode survives cold recreation without changing wallet or copies "+original);
        }
        before=new HashMap<>(c.save().disk);check(!s.equipCosmetic("missing")&&!s.equipCosmetic("hat_cupid")&&!s.equipSignatureOutfit("missing")&&!s.equipSignatureOutfit("moon"),"Invalid and unowned wardrobe equip requests are refused");assertDisk(c,before,"refused wardrobe requests preserve original mode and slots");
        check(s.equipCosmetic("hat_dew"),"Equipping an owned starter is an explicit wardrobe choice");check(c.save().disk.equals(cosmeticWrite(before,"hat_dew")),"Single garment selection and wardrobe mode commit atomically with no other changes");
        check(!store(c.reboot()).usesOriginalCompanionCostume(),"Actual garment equip activates wardrobe after cold recreation");
        check(s.setUseOriginalCompanionCostume(true)&&s.equippedCosmetic(CosmeticCatalog.Slot.HAT).equals("hat_dew"),"Switching to authored costume retains the selected garment");
        SignatureOutfits.Outfit outfit=earnedSignature(s,"wardrobe:signature");check(s.usesOriginalCompanionCostume(),"Acquiring a signature does not silently equip it or switch display mode");
        before=new HashMap<>(c.save().disk);check(s.equipSignatureOutfit(outfit.id),"Owned complete signature equips through its actual API");check(c.save().disk.equals(signatureWrite(before,outfit)),"All four signature slots and wardrobe mode are one atomic change with gear/wallet/copies untouched");
        GameStore cold=store(c.reboot());check(!cold.usesOriginalCompanionCostume(),"Signature wardrobe mode survives a cold recreation");for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())check(cold.equippedCosmetic(slot).equals(outfit.item(slot)),"Cold signature retains its exact selected item "+slot);
    }
    private static void costumeFailures(){
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(c);s.setUseOriginalCompanionCostume(false);Map<String,Object> before=new HashMap<>(c.save().disk);
            c.save().fail(memory);refused(()->s.setUseOriginalCompanionCostume(true),"Failed costume-mode write surfaces a typed refusal "+memory);assertDisk(c,before,"failed costume mode "+memory);
            c.save().fail(!memory);GameStore second=store(c);assertDisk(c,before,"safe construction after repeated pending-mode save refusal "+memory);
            c.save().fail(memory);refused(()->second.usesOriginalCompanionCostume(),"A guarded mode read surfaces a repeated pending save refusal "+memory);assertDisk(c,before,"repeated failed guarded costume read "+memory);
            check(!store(c.reboot()).usesOriginalCompanionCostume(),"Process death after refused mode write keeps the prior durable mode "+memory);
            GameStore recovered=store(c);before.put(COSTUME_KEY,true);check(recovered.usesOriginalCompanionCostume()&&c.save().disk.equals(before),"Another handle recovers exactly the pending mode and no unrelated writes "+memory);

            RewardsCosmeticsV17Test.Ctx single=new RewardsCosmeticsV17Test.Ctx();GameStore one=store(single);one.setUseOriginalCompanionCostume(true);Map<String,Object> singleBefore=new HashMap<>(single.save().disk);
            single.save().fail(memory);refused(()->one.equipCosmetic("gloves_dew"),"Failed garment/mode compound write surfaces refusal "+memory);assertDisk(single,singleBefore,"failed compound garment selection "+memory);
            RewardsCosmeticsV17Test.Ctx singleCold=single.reboot();check(store(singleCold).usesOriginalCompanionCostume()&&!singleCold.save().disk.containsKey("cosmetic_slot_v17_GLOVES"),"Cold refusal exposes neither a selected-slot fragment nor changed mode "+memory);
            GameStore oneRecovered=store(single);check(!oneRecovered.usesOriginalCompanionCostume()&&oneRecovered.equippedCosmetic(CosmeticCatalog.Slot.GLOVES).equals("gloves_dew")&&single.save().disk.equals(cosmeticWrite(singleBefore,"gloves_dew")),"Garment and display mode recover together through another store handle "+memory);

            RewardsCosmeticsV17Test.Ctx all=new RewardsCosmeticsV17Test.Ctx();GameStore four=store(all);SignatureOutfits.Outfit outfit=earnedSignature(four,"wardrobe:failed-signature:"+memory);four.setUseOriginalCompanionCostume(true);Map<String,Object> allBefore=new HashMap<>(all.save().disk);
            all.save().fail(memory);refused(()->four.equipSignatureOutfit(outfit.id),"Failed four-slot/mode signature write surfaces refusal "+memory);assertDisk(all,allBefore,"failed complete signature equip "+memory);
            RewardsCosmeticsV17Test.Ctx allCold=all.reboot();check(store(allCold).usesOriginalCompanionCostume(),"Process death after refused signature keeps authored costume "+memory);for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())check(!allCold.save().disk.containsKey("cosmetic_slot_v17_"+slot.name()),"No partial signature slot is durable after refusal "+slot+" "+memory);
            GameStore fourRecovered=store(all);check(!fourRecovered.usesOriginalCompanionCostume()&&all.save().disk.equals(signatureWrite(allBefore,outfit)),"All four selected items and display mode recover exactly together "+memory);
        }
    }
    public static void main(String[] args)throws Exception{
        SortedMap<String,String> sources=sourceHashes();String manifest=sourceManifest(sources),sourceDigest=hash(manifest);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));migrationAndPaid();fullRun();sameIdentityAndCap();home();paidFailure();failures();corruptReceipts();concurrency();stances();guestCollection();int costumeChecksStart=checks;costumeChoice();costumeFailures();int costumeChecks=checks-costumeChecksStart;
        if(!sources.equals(sourceHashes()))throw new AssertionError("Compilation input source changed during store qualification");
        String report="{\n  \"passed\": true,\n  \"checks\": "+checks+",\n  \"wardrobe_mode_checks\": "+costumeChecks+",\n  \"actual_U_certificates_played\": "+certificates+",\n  \"certificate_actions_played\": "+certificateActions+",\n  \"checks_excluding_certificate_actions\": "+(checks-certificateActions)+",\n  \"count_note\": \"Each real certificate action is one assertion. Random departure routes choose different real U boards, so the aggregate assertion count can vary; fixed-scope checks and52 played certificates remain explicit.\",\n  \"source_manifest_sha256\": \""+sourceDigest+"\",\n  \"source_file_count\": "+sources.size()+",\n  \"scope\": \"Actual GameStore APIs with durable JVM preference doubles, explicit historical receipt/maximum-copy/old-wardrobe-choice fixtures, earned paid draws, real U certificates, four-stage route, skill/choice resume, copy/discovery/home rewards, cap/surplus, failed writes before/after memory, repeated refusal, cold recreation, corrupt receipt refusal and concurrent store handles. Wardrobe mode preserves explicit selections and changes atomically with single-item/four-slot signature equip, including failed-write recovery. No Android rendering or device performance claim.\"\n}\n";
        if(args.length>0){Path output=Path.of(args[0]);Files.createDirectories(output.getParent());Files.writeString(output,report);Files.writeString(output.resolveSibling(output.getFileName().toString().replaceFirst("\\.json$","")+"-sources.json"),manifest);}System.out.println(report);
    }
}
