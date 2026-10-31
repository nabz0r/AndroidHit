package cognitive_lab;

import com.androidhit.lumadrift.data.CompanionBattle;
import com.androidhit.lumadrift.engine.ElementalShell121;
import com.androidhit.lumadrift.engine.Level;
import com.androidhit.lumadrift.engine.RelayCatalog;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Current Journey mechanics; ownership/copy assumptions are explicit in each cohort. */
final class Environment124 extends Environment122 {
    final String condition;
    Environment124(int level,int ordinal,int day,String condition,List<Integer> capturedLevels){
        this(RelayCatalog.get(level),ordinal,day,condition,capturedLevels);
    }
    Environment124(Level level,int ordinal,int day,String condition,List<Integer> capturedLevels){
        super(level,ordinal,day,"cause_immediate",ElementalShell121.createJourney123(level,0,level.number));
        this.condition=condition;
        if(!condition.equals("manual_control_124")){
            List<String> team=team(condition);
            companion=condition.equals("single_use_control_124")?CompanionBattle.State.start(team)
                    :CompanionBattle.State.start(team,capturedLevels);
            if(companion==null)throw new IllegalArgumentException("Invalid captured team levels");
        }
        if(shells.totalLayers()!=0)throw new AssertionError("Current Journey must remain shell-free");
    }
    static List<String> team(String condition){
        return condition.equals("loaned_trio_rank_124")?Arrays.asList("nova","hoot","orbit"):Collections.singletonList("nova");
    }
    @Override Map<String,Object> row122(String profile,int repetition,int day,String outcome){
        Map<String,Object> row=super.row122(profile,repetition,day,outcome);row.put("scenario",condition);return row;
    }
}
