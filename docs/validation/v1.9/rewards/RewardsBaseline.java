import com.androidhit.lumadrift.data.*;
import java.util.*;

/** Source-level baseline: actual production stores, deterministic preference doubles.
 * This is an economy trace, not a claim that a person completes any board. */
public final class RewardsBaseline {
    static String stocks(GameStore s) {
        StringJoiner j=new StringJoiner(",","{","}");
        for(Catalog.BoosterItem p:Catalog.BOOSTERS)j.add("\""+p.id+"\":"+s.boosterStock(p.id));
        return j.toString();
    }
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        for(boolean immediate:new boolean[]{true,false}) {
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();
            RewardsCosmeticsV17Test.Clock clock=new RewardsCosmeticsV17Test.Clock("2026-10-27");
            GameStore s=new GameStore(c,null,clock); MetaProgress m=new MetaProgress(c,clock);
            s.saveTutorialState("T1|7|0|COMPLETE|");s.claimTutorialReward();s.claimDailyLogin();
            System.out.println("{\"scenario\":\""+(immediate?"claim_each_milestone":"defer_all_milestones")+"\",\"stage\":\"after_tutorial_login\",\"coins\":"+s.coins()+",\"stock\":"+stocks(s)+"}");
            for(int n=1;n<=100;n++) {
                int earned=s.completeLevel(n,3);m.recordWin(0,n,3,"");s.claimAvatarGearUnlocks();s.claimCosmeticUnlocks();
                String loot=immediate&&n%5==0?s.claimMilestoneLoot():"";
                boolean unlock=false;for(Catalog.BoosterItem p:Catalog.BOOSTERS)if(p.unlockLevel==s.unlockedLevel())unlock=true;
                if(n%5==0||unlock||n==1||n==8||n==12)
                    System.out.println("{\"scenario\":\""+(immediate?"claim_each_milestone":"defer_all_milestones")+"\",\"completed\":"+n+",\"unlocked_level\":"+s.unlockedLevel()+",\"coins\":"+s.coins()+",\"earned\":"+earned+",\"rank\":"+m.rank()+",\"hint_pack_price\":"+s.boosterPrice("hint")+",\"next_loot\":"+s.nextLootMilestone()+",\"claimed_loot\":\""+loot+"\",\"stock\":"+stocks(s)+"}");
            }
            if(!immediate) {
                StringJoiner claimed=new StringJoiner(",","[","]");
                for(int i=0;i<20;i++)claimed.add("\""+s.claimMilestoneLoot()+"\"");
                System.out.println("{\"scenario\":\"defer_all_milestones\",\"stage\":\"after_claiming_20\",\"items\":"+claimed+",\"stock\":"+stocks(s)+"}");
            }
        }
    }
}
