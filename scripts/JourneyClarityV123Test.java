import com.androidhit.lumadrift.engine.*;
import com.androidhit.lumadrift.progression.*;
import java.nio.file.*;
import java.util.*;

/** Actual engine/codec checks. Certified controls are not player trajectories. */
public final class JourneyClarityV123Test {
    private static long checks, legacyStates, reachableStates;
    private static int certifiedBoards, correctionRecoveries;
    private static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
    private static String position(Board board){return board.remainingMask()+":"+board.gateMask()+":"+Arrays.toString(board.history())+":"+board.plannerCachedStates();}
    private static void campaign(){
        for(int number=1;number<=1000;number++){
            Level level=RelayCatalog.get(number);Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);
            String before=position(board);int[] certificate=level.solution.clone();
            ElementalShell121 current=ElementalShell121.createJourney123(level,0,number);
            check(current.totalLayers()==0&&current.remainingCount()==0,"new Journey has no preparation at U"+number);
            check(ElementalShell121.decode(level,current.encode()).encode().equals(current.encode()),"bare state cold roundtrip U"+number);
            check(position(board).equals(before)&&Arrays.equals(certificate,level.solution),"factory never changes puzzle U"+number);
            check(budget.policyVersion()==6&&budget.mistakeAllowance()==3&&budget.total()==level.tiles.size()+3,"unchanged explicit budget U"+number);
            for(int id:certificate){
                check(current.tap(id)==ElementalShell121.TapResult.NONE,"tap cannot become artificial preparation U"+number);
                check(budget.attempt(board,id).success,"actual original certificate exits U"+number);
            }
            check(board.isSolved()&&budget.used()==level.minimumActions&&budget.remaining()==3,"manual certified control solves U"+number);certifiedBoards++;
            ElementalShell121 historical=ElementalShell121.create(level,0,number);
            check(historical.totalLayers()==(number<=10?0:5),"historical factory unchanged U"+number);
            check(ElementalShell121.createJourney123(level,5,number).encode().equals(historical.encode()),"expedition contract unchanged U"+number);
            while(true){
                String saved=historical.encode();ElementalShell121 resumed=ElementalShell121.decode(level,saved);legacyStates++;
                check(resumed!=null&&resumed.encode().equals(saved),"every original partial preparation resumes U"+number);
                int next=-1;for(Tile tile:level.tiles)if(historical.layers(tile.id)>0){next=tile.id;break;}
                if(next<0)break;
                historical.tap(next);
            }
        }
        check(certifiedBoards==1000&&legacyStates==5950,"all actual boards and all six monotone legacy preparation prefixes covered");
        Level level=RelayCatalog.get(42);boolean refused=false;
        try{ElementalShell121.createJourney123(level,0,41);}catch(IllegalArgumentException expected){refused=true;}
        check(refused,"new bare factory still refuses mismatched U identity");
        check(ElementalShell121.createJourney123(LearningCatalog.get(42),0,42).totalLayers()==0,"preserved N content remains bare");
    }
    private static void learning(){
        for(int number=1;number<=1000;number++){
            Board board=new Board(RelayCatalog.get(number));String before=position(board);
            LearningGuide.Context historical=LearningGuide.context(board,RelayCatalog.lesson(number));
            LearningGuide.Context current=LearningGuide.contextJourney123(board,RelayCatalog.lesson(number));
            String expected=number>=13&&number<=16?"relay_inspection":number>=17&&number<=28?"relay_anticipation":number>=61&&number<=64?"relay_double":historical.lessonKey;
            check(current.lessonKey.equals(expected),"versioned authored teaching sequence U"+number);
            boolean relay=false;for(Tile tile:board.remainingTiles())relay|=tile.isSwitch();
            check(LearningGuide.inspectionRecommended(board)==relay,"affordance reads remaining relay marks U"+number);
            check(position(board).equals(before),"teaching and affordance do not query planner or mutate state U"+number);
            for(int id:board.level.solution)check(board.move(id).success,"teaching does not alter original certificate U"+number);
            check(!LearningGuide.inspectionRecommended(board)&&LearningGuide.contextJourney123(board,RelayCatalog.lesson(number)).lessonKey.equals("solved"),"completed board has no stale inspection or lesson");
        }
        check(!LearningGuide.inspectionRecommended(null),"null affordance safely absent");
        check(!LearningGuide.inspectionRecommended(new Board(StrategyCatalog.get(20))),"historical manual gate content keeps its own instruction");
        for(String language:new String[]{"en","fr","de","es","pt","it"}){
            for(String lesson:new String[]{"relay_inspection","relay_anticipation","relay_double"}){
                for(String key:new String[]{"hud_"+lesson,"lesson_"+lesson+"_title","lesson_"+lesson+"_note"}){
                    String copy=LearningStrings.get(language,key);check(!copy.isBlank()&&!copy.equals(key),"localized new lesson "+key+"/"+language);
                    if(key.startsWith("hud_"))check(copy.codePointCount(0,copy.length())<=75,"new HUD remains short "+language+"/"+key);
                }
            }
            String undo=LearningStrings.get(language,"relay_undo_current_note");
            check(undo.contains("2")&&undo.contains("Coin"),"current Undo instruction states two free and later Coin cost "+language);
        }
    }
    private static void preserveChallenge(){
        for(int number=1;number<=27;number++){
            Level level=RelayCatalog.get(number);ArrayDeque<int[]> pending=new ArrayDeque<>();pending.add(new int[0]);Set<Long> seen=new HashSet<>();boolean dangerous=false;
            while(!pending.isEmpty()){
                int[] path=pending.removeFirst();Board board=new Board(level);check(board.restoreHistory(path),"reachable engine prefix restores");
                if(!seen.add(board.remainingMask()))continue;reachableStates++;
                for(Tile tile:board.legalMoves()){
                    dangerous|=!board.isSafeMove(tile.id);int[] next=Arrays.copyOf(path,path.length+1);next[path.length]=tile.id;pending.addLast(next);
                }
            }
            check(dangerous==(number==27),"first actual legal trap is still U27, earlier inspection changes no puzzle");
        }
        Board trap=new Board(RelayCatalog.get(27));MoveBudget budget=MoveBudget.start(0,trap.level);
        check(budget.attempt(trap,2).success&&!trap.hasSolution(),"U27 L5C3 remains a real legal order error");
        check(budget.undo(trap,0)&&budget.used()==1,"undo restores position without refunding original exit");
        for(int id:trap.level.solution)check(budget.attempt(trap,id).success,"causal mistake remains recoverable within real budget");
        check(trap.isSolved()&&budget.used()==6&&budget.remaining()==2,"one bad choice recovery has exact action accounting");
        for(int number:new int[]{27,29,31,43,48,61}){
            Level level=RelayCatalog.get(number);Board board=new Board(level);MoveBudget allowance=MoveBudget.start(0,level);
            for(int correction=0;correction<3;correction++){
                check(allowance.attempt(board,level.solution[0]).success&&allowance.undo(board,correction),"three corrections remain possible without changing geometry");
            }
            for(int id:level.solution)check(allowance.attempt(board,id).success,"original route completes after full correction margin");
            check(board.isSolved()&&allowance.remaining()==0,"three corrections exactly use allowance U"+number);correctionRecoveries++;
        }
    }
    public static void main(String[] args)throws Exception{
        campaign();learning();preserveChallenge();
        String report="{\n  \"passed\":true,\n  \"assertions\":"+checks+",\n  \"certified_control_boards\":"+certifiedBoards+",\n  \"legacy_preparation_states_roundtripped\":"+legacyStates+",\n  \"reachable_engine_states_U1_U27\":"+reachableStates+",\n  \"full_margin_recovery_boards\":"+correctionRecoveries+",\n  \"scope\":\"Real Java Board/MoveBudget and ES121 codecs;1000 preserved certificates with new Journey factory, historical partial-save compatibility, six-language teaching/no planner mutation, first actual trap U27 and exact recovery budgets. Certified controls are not synthetic players or human learning/rendering evidence.\"\n}\n";
        Path out=Path.of(args[0]);Files.createDirectories(out.getParent());Files.writeString(out,report);System.out.print(report);
    }
}
