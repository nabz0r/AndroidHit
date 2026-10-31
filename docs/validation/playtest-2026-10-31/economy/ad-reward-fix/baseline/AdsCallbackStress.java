import android.app.Activity;
import android.content.SharedPreferences;
import com.androidhit.lumadrift.commerce.AdsController;
import com.androidhit.lumadrift.data.GameStore;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import java.nio.file.*;import java.util.*;

/** Real AdsController, manually ordered SDK callback fixtures, actual GameStore reward listener. */
public final class AdsCallbackStress {
 static long checks,loads,earned,refused;static Set<String> ids=new HashSet<>();
 static void ck(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static class Owner extends Activity {final EconomyStateStress.Ctx ctx=new EconomyStateStress.Ctx();public SharedPreferences getSharedPreferences(String n,int mode){return ctx.getSharedPreferences(n,mode);}}
 static class Listener implements AdsController.Listener {final GameStore store;int calls;String lastStatus="",lastId="";Listener(GameStore g){store=g;}public void onStatus(String s){lastStatus=s;}public void onReward(String id,int amount){calls++;lastId=id;store.grantGold(id,amount);}public void onEvent(String e,Map<String,String> f){}}
 static RewardedAd ready(AdsController ads)throws Exception{int before=RewardedAd.loads;ads.loadRewarded();ck(RewardedAd.loads==before,"unknown/minor no SDK load");ads.setAgeStatus(AdsController.AgeStatus.ADULT);ads.setExplicitOptIn(true);ads.loadRewarded();long end=System.nanoTime()+2_000_000_000L;while(RewardedAd.loads==before&&System.nanoTime()<end)Thread.sleep(1);ck(RewardedAd.loads==before+1,"one explicit load after consent");ads.loadRewarded();ck(RewardedAd.loads==before+1,"pending load guarded");var ad=RewardedAd.deliver();ck(ads.isRewardedReady(),"loaded callback accepted");ads.loadRewarded();ck(RewardedAd.loads==before+1,"cached load guarded");ads.showRewarded();ck(ads.isAdShowing(),"real controller owns presentation");ads.showRewarded();ck(ad.shows==1,"second show cannot reuse creative");loads++;return ad;}
 public static void main(String[] args)throws Exception{
  Path out=Path.of(args[0]);Files.createDirectories(out);int sessions=Integer.parseInt(args[1]);long started=System.nanoTime();
  for(int n=0;n<sessions;n++){
   Owner owner=new Owner();var time=new EconomyStateStress.Time();var g=EconomyStateStress.store(owner.ctx,time);var l=new Listener(g);var ads=new AdsController(owner,l);ads.onAppLaunch();var ad=ready(ads);int scenario=n%8;
   switch(scenario){
    case 0-> {ad.earn();ad.earn();ad.dismiss();}
    case 1-> {ad.dismiss();ad.earn();ad.earn();}
    case 2-> {ads.setExplicitOptIn(false);ad.earn();ad.dismiss();}
    case 3-> {ads.setAgeStatus(AdsController.AgeStatus.MINOR);ad.earn();ad.dismiss();}
    case 4-> {ad.callbacks.onAdFailedToShowFullScreenContent(new FullScreenContentError());ad.earn();ad.dismiss();}
    case 5-> {ads.close();ad.earn();ad.dismiss();}
    case 6-> {ad.earn();ads.setExplicitOptIn(false);ad.earn();ad.dismiss();}
    case 7-> {ad.dismiss();}
   }
   boolean reward=scenario==0||scenario==1||scenario==6;ck(l.calls==(reward?1:0),"one earned event only for valid consent/show lifecycle");ck(g.coins()==180+(reward?35:0),"fixed35Coins only, never provider amount");var cold=EconomyStateStress.store(owner.ctx.reboot(),time);ck(cold.coins()==g.coins(),"reward survives successful host disk commit");if(reward){earned++;ck(ids.add(l.lastId)&&l.lastId.startsWith("ad:"),"show identity unique and stable");}else refused++;ads.close();ad.earn();ck(l.calls==(reward?1:0),"closed controller rejects late callback");
  }
  StringBuilder findings=new StringBuilder();
  for(boolean memoryFirst:new boolean[]{false,true}){
   Owner owner=new Owner();var time=new EconomyStateStress.Time();var g=EconomyStateStress.store(owner.ctx,time);var l=new Listener(g);var ads=new AdsController(owner,l);var ad=ready(ads);owner.ctx.save().fail=true;owner.ctx.save().memoryFirst=memoryFirst;ad.earn();ck(l.calls==1&&l.lastStatus.equals("commerce_save_error"),"actual save error surfaced");ad.earn();ck(l.calls==1,"same earned callback cannot retry listener after failed fulfillment");ad.dismiss();ads.close();var cold=EconomyStateStress.store(owner.ctx.reboot(),time);ck(cold.coins()==180,"failed earned reward absent after cold reconstruction");findings.append("    {\"memoryBeforeFailedDiskCommit\":").append(memoryFirst).append(",\"reportedStatus\":\"commerce_save_error\",\"listenerCallsAfterDuplicateEarnedCallback\":1,\"coldCoins\":180,\"expectedEarnedCoinsIfDurablyFulfilled\":215}");if(!memoryFirst)findings.append(',');findings.append('\n');
  }
  String json="{\n  \"status\":\"PASS_WITH_FINDING\",\n  \"lifecycleSessions\":"+sessions+",\n  \"lifecycleVariants\":8,\n  \"assertions\":"+checks+",\n  \"acceptedUniqueEarnedShows\":"+earned+",\n  \"unearnedOrInvalidatedShows\":"+refused+",\n  \"injectedRewardSaveFailures\":2,\n  \"finding\":{\"id\":\"AD_EARNED_SAVE_FAILURE_HAS_NO_RETRY\",\"status\":\"REPRODUCED_IN_HOST_FIXTURE\",\"cases\":[\n"+findings+"  ]},\n  \"elapsedSeconds\":"+((System.nanoTime()-started)/1e9)+",\n  \"scope\":\"Actual AdsController+GameStore; current production BuildConfig. Consent, SDK init/load/show/earned/dismiss/error callbacks and Android preferences are controlled host doubles, no UMP/Google Ads request or rendered creative. Eight deterministic lifecycle variants repeat1250 times; this is callback stress, not10k real ad views or human sessions.\"\n}\n";Files.writeString(out.resolve("summary.json"),json);System.out.print(json);
 }
}
