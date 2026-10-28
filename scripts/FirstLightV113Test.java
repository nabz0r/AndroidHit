import com.androidhit.lumadrift.onboarding.FirstLight;
import com.androidhit.lumadrift.engine.Board;

/** Check the actual first encounter, its causal switch and every persisted prefix. */
public final class FirstLightV113Test {
    private static int count;
    private static void check(boolean result,String message){count++;if(!result)throw new AssertionError(message);}
    public static void main(String[] args){
        Board initial=FirstLight.restore("");check(initial.remainingCount()==3,"Three real pieces");
        check(!initial.previewAction(1).legal,"Right exit really waits for the switch");
        check(!initial.move(1).success&&FirstLight.history(initial).isEmpty(),"Blocked exploration changes no history");
        check(initial.move(0).success,"Switch piece can leave immediately");
        check(initial.previewAction(1).legal,"Switch immediately enables the right exit");
        check(FirstLight.history(initial).equals("0"),"First gesture persists as a real action");
        Board cold=FirstLight.restore("0");check(cold.remainingCount()==2&&cold.previewAction(1).legal,"Cold reconstruction retains the changed gate");
        check(cold.move(1).success&&cold.move(2).success&&cold.isSolved(),"Three exits complete without a power or gate purchase");
        check(FirstLight.restore(FirstLight.history(cold)).isSolved(),"Solved first encounter restores before Home acknowledgement");
        for(String bad:new String[]{"1","0,0","2,1","9","0,1,2,2","garbage"})check(FirstLight.restore(bad).remainingCount()==3,"Invalid history rejects atomically: "+bad);
        check(FirstLight.restore("2").remainingCount()==2,"Free alternative first move restores");
        Board alternate=FirstLight.restore("2");check(alternate.move(0).success&&alternate.move(1).success&&alternate.isSolved(),"Exploration retains a complete free solution");
        check(initial.undo()&&FirstLight.history(initial).isEmpty()&&!initial.previewAction(1).legal,"Undo restores both the piece and its gate effect");
        System.out.println("PASS "+count+" FirstLight actual-board checks");
    }
}
