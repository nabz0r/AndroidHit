import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;import java.util.*;

/** Real first-win/XP/store APIs and solved engine controls; explicit disk-failure adapters. */
public final class RankCopyV124Test {
 static long checks;static int solvedBoards,failureCases,coldCases;static int nextId=1;
 static void ck(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static String run(){return String.format(Locale.ROOT,"00000000-0000-0000-0000-%012d",nextId++);}
 static class Profile{
  EconomyStateStress.Ctx c;GameStore g;MetaProgress m;RankCopyCoordinator124 coordinator;
  long now=java.time.Instant.parse("2026-10-31T12:00:00Z").toEpochMilli();
  Profile(){this(new EconomyStateStress.Ctx());}
  Profile(EconomyStateStress.Ctx c){this.c=c;g=new GameStore(c,null,()->now);m=new MetaProgress(c,()->now);coordinator=new RankCopyCoordinator124(g,m);}
  Profile cold(){coldCases++;return new Profile(c.reboot());}
  EconomyStateStress.Prefs meta(){return (EconomyStateStress.Prefs)c.getSharedPreferences("luma_drift_meta_v1",0);}
  EconomyStateStress.Prefs victory(){return (EconomyStateStress.Prefs)c.getSharedPreferences("test_victory",0);}
 }
 static Board solved(int number){Board b=new Board(RelayCatalog.get(number));for(int action:b.level.solution)ck(b.performAction(action).success,"actual certified action");ck(b.isSolved(),"actual board solved");solvedBoards++;return b;}
 static VictoryReceipt pending(Profile p,int number,String captain){return VictoryReceipt.pending124(run(),0,number,0,"",3,p.g.coins(),p.m.xp(),1000,solved(number).moves(),RelayCatalog.get(number).minimumActions+3,0,p.g.equippedCharacter(),captain);}
 static VictoryReceipt play(Profile p,int number,String captain){var r=pending(p,number,captain);p.g.completeLevel(number,3,true);return p.coordinator.settleVictoryReceipt(r,true);}
 static void firstThree(Profile p){for(int n=1;n<=3;n++){var v=play(p,n,"nova");ck(v.xp==35&&v.rankCopies==0,"first three unique wins do not cross rank");}ck(p.m.xp()==105&&p.g.companionCopies("nova")==1,"pre-boundary state real XP");}
 static void freshAndSnapshot(){
  var p=new Profile();ck(p.g.purchaseCharacter("miko"),"owned captain acquired through public purchase");ck(p.g.setCompanionSquad(List.of("miko","nova")),"real squad owns captain");firstThree(p);
  var before=CompanionBattle.State.start(p.g.companionSquad(),List.of(1,1));var v=pending(p,4,before.team().get(0));
  ck(p.g.equippedCharacter().equals("nova"),"home actor differs from actual played captain");p.g.setCompanionSquad(List.of("nova"));p.g.equipCharacter("miko");p.g.completeLevel(4,3,true);
  var result=p.coordinator.settleVictoryReceipt(v,true);ck(result.ready&&result.xp==35&&result.rankBefore==1&&result.rankAfter==2&&result.rankCopies==1&&result.captainId.equals("miko"),"rank copy and display target actual run captain");ck(p.g.companionCopies("miko")==2&&p.g.companionCopies("nova")==1,"one copy belongs only to snapshotted captain");
  ck(before.capacity(0)==1,"an already-started state keeps original capacity");var next=CompanionBattle.State.start(List.of("miko"),List.of(p.g.companionStats("miko").level));ck(next.capacity(0)==2,"next board captures increased real capacity");
  ck(VictoryReceipt.decode(result.encode()).encode().equals(result.encode()),"new rank result codec roundtrip");for(int k=0;k<20;k++)ck(p.coordinator.settleVictoryReceipt(v,true).rankCopies==1&&p.g.companionCopies("miko")==2,"pending receipt replay displays historical copy without regrant");
  var replay=pending(p,4,"nova");p.g.completeLevel(4,3,true);var no=p.coordinator.settleVictoryReceipt(replay,true);ck(no.xp==0&&no.rankCopies==0&&no.rankCoins==0,"new attempt of old victory earns neither XP nor copy");
  var bad=VictoryReceipt.pending124(v.runId,0,4,0,"",3,v.beforeCoins,v.beforeXp,1000,v.used,v.total,0,"nova","nova");boolean changed=false;try{p.coordinator.settleVictoryReceipt(bad,true);}catch(IllegalStateException expected){changed=true;}ck(changed&&p.g.companionCopies("nova")==1,"same run cannot move reward to another captain");
 }
 static void migrationAndLegacy(){
  var p=new Profile();for(int n=1;n<=8;n++){p.g.completeLevel(n,3);p.m.recordWin(0,n,3,"");}ck(p.m.rank()==3,"old profile has existing rank");ck(p.coordinator.initializeMigrationFloor()==3&&p.g.companionCopies("nova")==1,"migration gifts no historical copies");for(int n=9;n<=10;n++)ck(play(p,n,"nova").rankCopies==0,"no backlog before next real rank");ck(play(p,11,"nova").rankCopies==1&&p.g.companionCopies("nova")==2,"first post-migration rank gives exactly one");
  p=new Profile();firstThree(p);var legacy=VictoryReceipt.pending(run(),0,4,0,"",3,p.g.coins(),p.m.xp(),1000,4,7,0,"nova");p.g.completeLevel(4,3,true);var v=p.coordinator.settleVictoryReceipt(legacy,true);ck(v.xp==35&&v.rankCopies==0&&v.rankCoins==0&&v.captainId.isEmpty()&&v.rankRewardStatus.equals("LEGACY_NO_CAPTAIN"),"old pending run without captain gets XP and explicit legacy event, no guessed recipient");ck(p.g.companionCopies("nova")==1,"legacy event never falls back to home avatar");
  var historical=VictoryReceipt.pending(run(),0,1,0,"",3,180,0,1000,4,7,0,"nova").settle(235,35);ck(!historical.rankAware&&VictoryReceipt.decode(historical.encode()).encode().equals(historical.encode()),"historical ready v120 receipt preserved byte for byte");ck(p.coordinator.settleVictoryReceipt(historical,true)==historical,"old ready result never creates new reward");
  p=new Profile();firstThree(p);legacy=VictoryReceipt.pending(run(),0,4,0,"",3,p.g.coins(),p.m.xp(),1000,4,7,0,"nova");p.g.completeLevel(4,3,true);p.m.recordWin(0,4,3,"");v=p.coordinator.settleVictoryReceipt(legacy,true);ck(v.xp==35&&v.rankCopies==0&&v.rankRewardStatus.equals("LEGACY_NO_CAPTAIN")&&p.m.xp()==140,"legacy XP already durable is displayed but never retroactively converted to a copy");
 }
 static void maxCompensation(){
  var p=new Profile();VictoryReceipt last=null;int compensations=0;
  for(int n=1;n<=320;n++){last=play(p,n,"nova");if(last.rankCoins>0){compensations++;ck(last.rankCoins==45&&last.rankCopies==0&&last.rankRewardStatus.equals("MAX_COMPENSATION"),"max captain has explicit45Coin compensation");int b=p.g.coins();ck(p.coordinator.settleVictoryReceipt(last,true)==last&&p.g.coins()==b,"ready compensation cannot be claimed again");}}
  ck(p.g.companionCopies("nova")==92&&compensations>0,"actual repeated rank grants reach max without direct copies injection");
  // Only this unreachable-in-a-short-test wallet boundary is a direct preference fixture.
  // All 92 companion copies above were obtained from actual unique engine victories.
  for(boolean memory:new boolean[]{false,true})for(boolean cold:new boolean[]{false,true}){
   var edge=new Profile(p.c.reboot());play(edge,321,"nova");play(edge,322,"nova");ck(edge.c.save().edit().putInt("coins",2_000_000_000).commit(),"explicit full-wallet boundary fixture");
   var pending=pending(edge,323,"nova");edge.g.completeLevel(323,3,true);boolean refused=false;
   try{edge.coordinator.settleVictoryReceipt(pending,true);}catch(IllegalStateException expected){refused=true;}
   ck(refused&&edge.m.xp()==11305&&edge.g.coins()==2_000_000_000,"full wallet leaves compensation pending without consuming its rank marker");
   if(cold)edge=edge.cold();ck(edge.g.purchaseCharacter("miko"),"real purchase makes wallet space during pending compensation");
   int coins=edge.g.coins();edge.c.save().memoryFirst=memory;edge.c.save().fail=true;refused=false;
   try{edge.coordinator.settleVictoryReceipt(pending,true);}catch(IllegalStateException expected){refused=true;}
   ck(refused,"atomic compensation write failure is visible");if(cold)edge=edge.cold();
   var result=edge.coordinator.settleVictoryReceipt(pending,true);
   ck(result.ready&&result.coins==45&&result.rankCoins==45&&result.xp==35&&edge.g.coins()==coins+45,"pending compensation survives purchase, failure and cold restart with exact per-run totals");
   edge.coordinator.settleVictoryReceipt(pending,true);ck(edge.g.coins()==coins+45&&edge.g.companionCopies("nova")==92,"compensation receipt prevents a second credit");failureCases++;
  }
 }
 static void failures(){
  for(int phase=0;phase<5;phase++)for(boolean memory:new boolean[]{false,true})for(boolean cold:new boolean[]{false,true})for(int repeat=0;repeat<50;repeat++){
   var p=new Profile();firstThree(p);var receipt=pending(p,4,"nova");p.victory().edit().putString("receipt",receipt.encode()).commit();p.g.completeLevel(4,3,true);
   // Each phase isolates a different durable boundary, without changing progression fixtures.
   if(phase==0){/* floor exists from firstThree; use an old-policy profile with no floor instead. */
    p=new Profile();for(int n=1;n<=3;n++){p.g.completeLevel(n,3);p.m.recordWin(0,n,3,"");}receipt=pending(p,4,"nova");p.victory().edit().putString("receipt",receipt.encode()).commit();p.g.completeLevel(4,3,true);p.c.save().memoryFirst=memory;p.c.save().fail=true;
   }else {p.coordinator.initializeMigrationFloor();if(phase>=2)p.g.freezeVictoryCoins124(receipt.runId,receipt.beforeCoins);if(phase>=3)p.m.recordWin124(0,4,3,"",receipt.runId,"nova");if(phase==2){p.meta().memoryFirst=memory;p.meta().fail=true;}else if(phase==1||phase==3){p.c.save().memoryFirst=memory;p.c.save().fail=true;}}
   boolean failed=false;VictoryReceipt ready=null;
   try{ready=p.coordinator.settleVictoryReceipt(receipt,true);if(phase==4){p.victory().memoryFirst=memory;p.victory().fail=true;if(!p.victory().edit().putString("receipt",ready.encode()).commit())throw new IllegalStateException("result write rejected");}}catch(IllegalStateException expected){failed=true;}
   ck(failed,"each isolated failure is visible at exact boundary");
   if(cold)p=p.cold();
   var restored=VictoryReceipt.decode(p.victory().getString("receipt",""));var settled=p.coordinator.settleVictoryReceipt(restored,true);ck(settled.ready&&settled.xp==35&&settled.rankCopies==1&&p.m.xp()==140&&p.g.companionCopies("nova")==2,"failure/cold recovery grants one copy and exactXP");
   for(int k=0;k<3;k++){p.coordinator.settleVictoryReceipt(restored,true);ck(p.g.companionCopies("nova")==2&&p.m.xp()==140,"post-recovery duplicate no extra XP/copy");}failureCases++;
  }
 }
 static void refusalsAndModes()throws Exception{
  var p=new Profile();var pending=pending(p,1,"nova");int xp=p.m.xp(),copies=p.g.companionCopies("nova");boolean failed=false;try{p.coordinator.settleVictoryReceipt(pending,false);}catch(IllegalArgumentException expected){failed=true;}ck(failed&&p.m.xp()==xp&&p.g.companionCopies("nova")==copies,"unsolved/failure/cancel does not award XP or copy");
  p.coordinator.initializeMigrationFloor();ck(p.g.claimRankCopy124(null,2)==null,"no rank gift without durable XP receipt");
  for(int mode:new int[]{1,2,3}){p=new Profile();firstThree(p);String period=mode==1?p.m.dayId():mode==2?p.m.weekId():"remix-v1";var r=VictoryReceipt.pending124(run(),mode,1,0,period,3,p.g.coins(),p.m.xp(),1000,4,7,0,"nova","nova");var actual=p.coordinator.settleVictoryReceipt(r,true);ck(actual.rankCopies==1&&actual.captainId.equals("nova"),"event/remix explicit persisted team snapshot can earn next rank");}
  for(int mode:new int[]{1,2}){p=new Profile();firstThree(p);String period=mode==1?p.m.dayId():p.m.weekId();var r=VictoryReceipt.pending124(run(),mode,1,0,period,3,p.g.coins(),p.m.xp(),1000,4,7,0,"nova","nova");p.g.freezeVictoryCoins124(r.runId,r.beforeCoins);p.m.recordWin124(mode,0,3,period,r.runId,"nova");p=p.cold();p.now+=8L*86400000;var actual=p.coordinator.settleVictoryReceipt(r,true);ck(actual.rankCopies==1&&actual.xp==35&&p.g.companionCopies("nova")==2,"earned event rank remains recoverable after period rollover");ck(p.m.recordWin124(mode,1,3,period,run(),"nova")==null,"a newly submitted expired event earns no XP or rank");}
  p=new Profile();firstThree(p);var r=pending(p,4,"miko");p.g.completeLevel(4,3,true);failed=false;try{p.coordinator.settleVictoryReceipt(r,true);}catch(IllegalStateException expected){failed=true;}ck(failed&&p.g.companionCopies("miko")==0&&p.g.companionCopies("nova")==1,"unowned captain cannot redirect reward to owned home actor");ck(p.g.coins()>=0,"ownership refusal never debits currency");
  final var shared=new Profile();firstThree(shared);var target=pending(shared,4,"nova");shared.g.completeLevel(4,3,true);List<Throwable> errors=Collections.synchronizedList(new ArrayList<>());List<Thread> threads=new ArrayList<>();for(int n=0;n<24;n++){Thread t=new Thread(()->{try{var handle=new Profile(shared.c);handle.coordinator.settleVictoryReceipt(target,true);}catch(Throwable e){errors.add(e);}});threads.add(t);t.start();}for(var t:threads){t.join(5000);ck(!t.isAlive(),"multi-handle rank transaction terminates");}ck(errors.isEmpty()&&shared.m.xp()==140&&shared.g.companionCopies("nova")==2,"24handles grant one first win and one rank copy");
  ck(RankCopyRules124.ranksCrossed(0,120)==1&&RankCopyRules124.ranksCrossed(119,120)==1&&RankCopyRules124.ranksCrossed(120,121)==0&&RankCopyRules124.migrationFloor(350)==3,"pure rank boundaries and migration");
  for(int stars=1;stars<=3;stars++){var graded=new Profile();var gradeReceipt=VictoryReceipt.pending124(run(),0,1,0,"",stars,graded.g.coins(),0,1000,4,7,0,"nova","nova");graded.g.completeLevel(1,stars,true);ck(graded.coordinator.settleVictoryReceipt(gradeReceipt,true).xp==20+5*stars,"each grade preserves its real native XP award");}
 }
 public static void main(String[] args)throws Exception{long start=System.nanoTime();freshAndSnapshot();migrationAndLegacy();maxCompensation();failures();refusalsAndModes();String report="{\n  \"status\":\"PASS\",\n  \"assertions\":"+checks+",\n  \"actualSolvedBoardControls\":"+solvedBoards+",\n  \"failureRecoveryCases\":"+failureCases+",\n  \"coldReconstructions\":"+coldCases+",\n  \"concurrentHandles\":24,\n  \"elapsedSeconds\":"+((System.nanoTime()-start)/1e9)+",\n  \"scope\":\"Actual GameStore/MetaProgress/coordinator/VictoryReceipt and solved Board controls with preference/clock adapters. Fresh/migration/legacy snapshots, owned real-run captain versus mutable home/squad, max92 compensation45, duplicates, XP and copy atomic failures, result-write failure/cold recovery and concurrent handles. No Android/render/provider/human qualification. Existing battle-state capacities remain unchanged; next factory captures new owned levels.\"\n}\n";Path out=Path.of(args[0]);Files.createDirectories(out.getParent());Files.writeString(out,report);System.out.print(report);}
}
