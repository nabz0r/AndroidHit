package cognitive_lab;

import com.androidhit.lumadrift.data.CompanionBattle;
import com.androidhit.lumadrift.engine.ElementalShell121;
import com.androidhit.lumadrift.engine.Level;
import com.androidhit.lumadrift.engine.RelayCatalog;
import java.util.Arrays;
import java.util.Map;

/** Uses the same versioned Journey factory as native 1.23; no shell-ablation flag. */
final class Environment123 extends Environment122 {
    final String condition;
    Environment123(int level, int ordinal, int day, String condition) {
        this(RelayCatalog.get(level), ordinal, day, condition);
    }

    Environment123(Level level, int ordinal, int day, String condition) {
        super(level, ordinal, day,
                condition.equals("cause_hidden_123") ? "control_v121" : "cause_immediate",
                ElementalShell121.createJourney123(level, 0, level.number));
        this.condition = condition;
        if (condition.equals("loaned_mixed_123"))
            companion = CompanionBattle.State.start(Arrays.asList("nova", "hoot", "orbit"));
        if (shells.totalLayers() != 0) throw new AssertionError("Native Journey 1.23 must be shell-free");
    }

    @Override Map<String,Object> row122(String profile, int repetition, int day, String outcome) {
        Map<String,Object> row = super.row122(profile, repetition, day, outcome);
        row.put("scenario", condition);
        return row;
    }
}
