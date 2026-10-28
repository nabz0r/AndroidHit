import com.androidhit.lumadrift.data.*;
public final class InitProbe {
 public static void main(String[] args){for(boolean memory:new boolean[]{false,true}){
  RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();c.getSharedPreferences("luma_drift_save_v1",0);c.save().fail(memory);
  RewardsCosmeticsV17Test.Clock clock=new RewardsCosmeticsV17Test.Clock("2026-10-28");GameStore s=new GameStore(c,null,clock);
  int initial=s.coins();boolean initialized=c.save().disk.containsKey("initialized");boolean bought=s.purchaseCharacter("miko");int after=s.coins();GameStore cold=new GameStore(c.reboot(),null,clock);
  System.out.println("{\"memoryBeforeDisk\":"+memory+",\"initializedDurable\":"+initialized+",\"initialCoins\":"+initial+",\"boughtMiko\":"+bought+",\"coinsAfterPurchase\":"+after+",\"coldCoins\":"+cold.coins()+",\"coldOwnsMiko\":"+cold.ownsCharacter("miko")+"}");
 }}
}