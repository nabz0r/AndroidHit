import com.androidhit.lumadrift.data.GameStore;
import java.nio.file.*;import java.util.*;

/** Contract failure/recovery tests for a genuinely earned, fixed35 local Coins record. */
public final class AdCoinRewardRecoveryTest {
 static long checks;static int recoveryCases,firstRecordFailures,fulfillmentFailures,coldRecoveries;
 static void ck(boolean x,String why){checks++;if(!x)throw new AssertionError(why);}
 static String id(int n){return "ad:"+String.format(Locale.ROOT,"00000000-0000-0000-0000-%012d",n);}
 static GameStore store(EconomyStateStress.Ctx c){return EconomyStateStress.store(c,new EconomyStateStress.Time());}
 static void refused(Runnable f){boolean threw=false;try{f.run();}catch(GameStore.LocalSaveException expected){threw=true;}ck(threw,"retryable storage failure is explicit");}
 static void stable(GameStore g,int coins){ck(g.coins()==coins,"exact total Coins");ck(!g.hasPendingAdCoins(),"no pending after durable completion");ck(g.retryPendingAdCoins()==0&&g.coins()==coins,"duplicate retry cannot credit");}
 static void recoveries(){
  for(int n=0;n<1000;n++)for(boolean memory:new boolean[]{false,true})for(boolean fulfillment:new boolean[]{false,true})for(boolean cold:new boolean[]{false,true}){
   var c=new EconomyStateStress.Ctx();var g=store(c);String show=id(n+1);Map<String,Object> before=new HashMap<>(c.save().disk);
   if(fulfillment){g.recordEarnedAdCoins(show);before=new HashMap<>(c.save().disk);fulfillmentFailures++;}
   else firstRecordFailures++;
   c.save().memoryFirst=memory;c.save().fail=true;
   var earnedOwner=g;if(fulfillment)refused(g::retryPendingAdCoins);else refused(()->earnedOwner.recordEarnedAdCoins(show));
   ck(c.save().disk.equals(before),"first failed write cannot partly reach durable save");ck(g.hasPendingAdCoins(),"pending intent remains visible even if Android memory hid row");
   c.save().fail=true;refused(g::retryPendingLocalSave);ck(c.save().disk.equals(before)&&g.hasPendingAdCoins(),"repeated failure preserves exact intent and durable state");
   if(cold&&fulfillment){g=store(c.reboot());coldRecoveries++;}
   else {g.retryPendingLocalSave();if(cold){g=store(c.reboot());coldRecoveries++;}}
   stable(g,215);g.recordEarnedAdCoins(show);g.retryPendingAdCoins();stable(g,215);recoveryCases++;
  }
 }
 static void boundaries()throws Exception{
  var c=new EconomyStateStress.Ctx();var g=store(c);
  for(String bad:new String[]{null,"","fixture","ad:wrong","ad:00000000-0000-0000-0000-000000000001;injected","ad:00000000-0000-0000-0000-000000000001\n"}){int commits=c.save().commits;boolean badId=false;try{g.recordEarnedAdCoins(bad);}catch(IllegalArgumentException expected){badId=true;}ck(badId&&c.save().commits==commits&&g.coins()==180,"invalid show id no write");}
  String legacy=id(9000);ck(g.grantGold(legacy,35)==35,"legacy receipt fixture");g.recordEarnedAdCoins(legacy);g.retryPendingAdCoins();stable(g,215);
  // A full-wallet boundary is an explicit balance fixture, not an acquisition claim.
  c.save().edit().putInt("coins",2_000_000_000).commit();g.recordEarnedAdCoins(id(9001));ck(g.retryPendingAdCoins()==0&&g.hasPendingAdCoins()&&g.coins()==2_000_000_000,"full wallet retains entire earned reward");
  ck(g.spendGoldOnce("full-wallet-test-spend",100)==100,"existing fixed spend frees room");ck(g.retryPendingAdCoins()==35,"pending fulfilled after balance room");stable(g,1_999_999_935);
  c=new EconomyStateStress.Ctx();g=store(c);var same=g;c.save().fail=true;refused(()->same.recordEarnedAdCoins(id(9010)));c.save().fail=true;refused(()->same.recordEarnedAdCoins(id(9011)));ck(g.hasPendingAdCoins(),"two in-memory earned shows not overwritten");g.retryPendingLocalSave();stable(g,250);
  // No successful write means a fresh process has no durable evidence of the SDK event.
  for(boolean memory:new boolean[]{false,true}){var ctx=new EconomyStateStress.Ctx();var local=store(ctx);ctx.save().memoryFirst=memory;ctx.save().fail=true;refused(()->local.recordEarnedAdCoins(id(9020)));var fresh=store(ctx.reboot());stable(fresh,180);}
  c=new EconomyStateStress.Ctx();g=store(c);g.recordEarnedAdCoins(id(9030));String room=g.homeRoom(),layout=g.homeLayout().encode(),actor=g.equippedCharacter();Map<String,Object> before=new HashMap<>(c.save().disk);g.retryPendingAdCoins();for(var p:before.entrySet())if(!p.getKey().equals("coins")&&!p.getKey().startsWith("ad_coins_pending_v1_"))ck(Objects.equals(p.getValue(),c.save().disk.get(p.getKey())),"unrelated saved field unchanged");ck(g.homeRoom().equals(room)&&g.homeLayout().encode().equals(layout)&&g.equippedCharacter().equals(actor),"reward never changes game/home/actor context");
  final var shared=new EconomyStateStress.Ctx();store(shared);List<Throwable> failures=Collections.synchronizedList(new ArrayList<>());List<Thread> threads=new ArrayList<>();for(int i=0;i<32;i++){Thread thread=new Thread(()->{try{var handle=store(shared);for(int j=0;j<10;j++){handle.recordEarnedAdCoins(id(9040));handle.retryPendingAdCoins();}}catch(Throwable e){failures.add(e);}});threads.add(thread);thread.start();}for(var thread:threads){thread.join(5000);ck(!thread.isAlive(),"parallel same show terminates");}ck(failures.isEmpty(),"parallel handles no exception");stable(store(shared),215);
 }
 public static void main(String[] args)throws Exception{long start=System.nanoTime();recoveries();boundaries();String result="{\n  \"status\":\"PASS\",\n  \"assertions\":"+checks+",\n  \"failureRecoveryCases\":"+recoveryCases+",\n  \"firstJournalWriteFailureCases\":"+firstRecordFailures+",\n  \"atomicFulfillmentFailureCases\":"+fulfillmentFailures+",\n  \"coldRecoveryCases\":"+coldRecoveries+",\n  \"concurrentSameShowHandles\":32,\n  \"noDurableWriteProcessDeathCasesWithDocumentedLoss\":2,\n  \"elapsedSeconds\":"+((System.nanoTime()-start)/1e9)+",\n  \"scope\":\"Actual GameStore and new local journal; two failure-memory modes, repeated refusal, initial journal versus atomic credit/receipt/ack, cold reconstruction, multiple earned IDs, legacy receipts, full-wallet retention and32concurrent handles. Host preference doubles, no APK/native/provider test. Process death before every durable write remains unrecoverable by design; two cases explicitly preserve that limit.\"\n}\n";Path out=Path.of(args[0]);Files.createDirectories(out.getParent());Files.writeString(out,result);System.out.print(result);}
}
