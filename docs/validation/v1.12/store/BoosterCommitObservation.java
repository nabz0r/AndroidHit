import com.androidhit.lumadrift.data.GameStore;
import java.util.*;
import java.nio.file.*;
public class BoosterCommitObservation {
 public static void main(String[] args)throws Exception {
  StringBuilder result=new StringBuilder("{\n  \"product_passed\": false,\n  \"scope\": \"Actual GameStore API on existing JVM SharedPreferences failure double; no device/provider claim\",\n  \"cases\": [\n");
  int i=0;
  for(boolean memory:new boolean[]{false,true}){
   RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();
   GameStore store=new GameStore(c,null,new RewardsCosmeticsV17Test.Clock("2026-10-28"));
   int price=store.boosterPrice("hint");c.save().fail(memory);
   boolean returned=false,threw=false;try{returned=store.purchaseBooster("hint");}catch(GameStore.LocalSaveException e){threw=true;}
   int liveCoins=c.save().getInt("coins",-1),liveStock=c.save().getInt("booster_hint",-1);
   store.retryPendingLocalSave();
   GameStore cold=new GameStore(c.reboot(),null,new RewardsCosmeticsV17Test.Clock("2026-10-28"));
   boolean reproduced=returned&&!threw&&cold.coins()==180&&cold.boosterStock("hint")==3;
   if(!reproduced)throw new AssertionError("Expected recorded baseline defect");
   if(i++>0)result.append(",\n");
   result.append("    {\"memory_before_failed_disk\": "+memory+", \"price\": "+price+", \"reported_success\": "+returned+", \"typed_failure\": "+threw+", \"live_coins_after_failure\": "+liveCoins+", \"live_stock_after_failure\": "+liveStock+", \"disk_coins_after_explicit_retry\": "+cold.coins()+", \"disk_stock_after_explicit_retry\": "+cold.boosterStock("hint")+", \"bug_reproduced\": true}");
  }
  result.append("\n  ]\n}\n");Files.writeString(Path.of(args[0]),result);System.out.println(result);
 }
}
