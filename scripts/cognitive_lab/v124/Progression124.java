package cognitive_lab;

import com.androidhit.lumadrift.data.CompanionProgression;
import com.androidhit.lumadrift.data.RankCopyRules124;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** In-memory fresh-save adapter. Native pure rules are reused; Android persistence is not simulated. */
final class Progression124 {
    int xp,rankAwards,overflowCoins;
    final Map<String,Integer> copies=new LinkedHashMap<>();
    private final Set<String> firstVictories=new HashSet<>();
    Progression124(List<String> initiallyOwned){for(String id:initiallyOwned)copies.put(id,1);}
    List<Integer> levelsFor(List<String> team){
        List<Integer> levels=new ArrayList<>();for(String id:team){Integer count=copies.get(id);if(count==null)throw new IllegalArgumentException("Unowned model team");levels.add(CompanionProgression.levelForCopies(count));}return levels;
    }
    int rank(){return RankCopyRules124.rankForXp(xp);}
    Map<String,Object> win(String victoryId,int stars,String capturedCaptain){
        if(!copies.containsKey(capturedCaptain))throw new IllegalArgumentException("Unowned captured captain");
        int earnedXp=RankCopyRules124.xpForStars(stars);
        if(!firstVictories.add(victoryId))return null;
        int beforeXp=xp,beforeRank=rank(),beforeCopies=copies.get(capturedCaptain),count=beforeCopies,coins=0;
        xp=Math.addExact(xp,earnedXp);int ranks=RankCopyRules124.ranksCrossed(beforeXp,xp);
        for(int award=0;award<ranks;award++){
            if(count<CompanionProgression.MAX_COPIES)count++;
            else coins+=RankCopyRules124.MAX_CAPTAIN_DUPLICATE_COINS;
        }
        copies.put(capturedCaptain,count);rankAwards+=ranks;overflowCoins+=coins;
        return Json.obj("captain",capturedCaptain,"xpBefore",beforeXp,"xpEarned",earnedXp,"xpAfter",xp,
                "rankBefore",beforeRank,"rankAfter",rank(),"ranksAwarded",ranks,"copiesBefore",beforeCopies,"copiesAfter",count,
                "levelBefore",CompanionProgression.levelForCopies(beforeCopies),"levelAfter",CompanionProgression.levelForCopies(count),
                "nextCapacityBefore",CompanionProgression.skillCharges(CompanionProgression.levelForCopies(beforeCopies)),
                "nextCapacityAfter",CompanionProgression.skillCharges(CompanionProgression.levelForCopies(count)),"overflowCoins",coins);
    }
}
