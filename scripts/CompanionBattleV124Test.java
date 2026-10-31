import com.androidhit.lumadrift.data.CompanionBattle;
import com.androidhit.lumadrift.data.CompanionBattleAction;
import com.androidhit.lumadrift.data.CompanionProgression;
import com.androidhit.lumadrift.engine.Board;
import com.androidhit.lumadrift.engine.MoveBudget;
import com.androidhit.lumadrift.engine.RelayCatalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Captured level capacities, legacy saves and actual native action preparation. */
public final class CompanionBattleV124Test {
    private static int checks;
    private static void check(boolean good,String message){checks++;if(!good)throw new AssertionError(message);}
    private static CompanionBattle.State start(String id,int level){
        CompanionBattle.State state=CompanionBattle.State.start(Collections.singletonList(id),Collections.singletonList(level));
        check(state!=null,"valid captured level");return state;
    }
    private static void roundTrip(CompanionBattle.State state){
        CompanionBattle.State restored=CompanionBattle.State.decode(state.encode());
        check(restored!=null&&restored.encode().equals(state.encode()),"exact canonical restore");
        for(int slot=0;slot<state.team().size();slot++)check(restored.capacity(slot)==state.capacity(slot)
                &&restored.usedCount(slot)==state.usedCount(slot)&&restored.remaining(slot)==state.remaining(slot),"capacity and uses survive restore");
    }
    private static String field(String encoded,int index,String value){String[] fields=encoded.split("\\|",-1);fields[index]=value;return String.join("|",fields);}

    private static void validationAndSnapshot(){
        check(CompanionBattle.State.start(Arrays.asList("nova"),null)==null,"missing levels refused");
        check(CompanionBattle.State.start(Arrays.asList("nova"),Collections.emptyList())==null,"short levels refused");
        check(CompanionBattle.State.start(Arrays.asList("nova"),Arrays.asList(1,2))==null,"extra levels refused");
        for(Integer invalid:new Integer[]{null,-1,0,11,Integer.MAX_VALUE})
            check(CompanionBattle.State.start(Arrays.asList("nova"),Arrays.asList(invalid))==null,"invalid level refused");
        check(CompanionBattle.State.start(Arrays.asList("nova","nova"),Arrays.asList(1,2))==null,"duplicate identity refused");
        check(CompanionBattle.State.start(Arrays.asList("nova","fake"),Arrays.asList(1,2))==null,"unknown identity refused");
        List<String> ids=new ArrayList<>(Arrays.asList("nova","miko","orbit"));
        List<Integer> levels=new ArrayList<>(Arrays.asList(1,2,6));
        CompanionBattle.State state=CompanionBattle.State.start(ids,levels);String frozen=state.encode();
        ids.set(0,"hoot");levels.set(0,10);
        check(state.encode().equals(frozen)&&state.team().get(0).equals("nova")&&state.capacity(0)==1,"defensive level/team snapshots");
        try{state.levels().set(0,10);throw new AssertionError("mutable levels");}catch(UnsupportedOperationException expected){check(true,"immutable captured levels");}
        check(state.isLevelled()&&state.policyVersion().equals("v124_1"),"explicit current policy");
        check(state.capacity(1)==2&&state.capacity(2)==4,"per-member levels captured independently");
        for(int slot:new int[]{-1,3,999})check(state.capacity(slot)==0&&state.usedCount(slot)==0&&state.remaining(slot)==0
                &&!state.used(slot)&&!state.canUse(slot)&&state.use(slot)==null,"invalid slot harmless");
        roundTrip(state);
    }

    private static void allCapacitiesAndCharge(){
        for(CompanionBattle.Definition definition:CompanionBattle.all())for(int level=1;level<=10;level++){
            CompanionBattle.State state=start(definition.id,level);int capacity=CompanionProgression.skillCharges(level),nextPiece=0;
            check(state.capacity(0)==capacity&&state.remaining(0)==capacity&&state.usedCount(0)==0,"same progression table for all identities");
            for(int use=0;use<capacity;use++){
                while(state.energy()<definition.cost)state=state.creditExit(nextPiece++);
                CompanionBattle.State before=state;String token=state.encode();
                state=state.use(0,token);
                check(state!=null&&state.energy()==before.energy()-definition.cost,"each use charges native cost");
                check(state.usedCount(0)==use+1&&state.remaining(0)==capacity-use-1,"one use consumed");
                check(state.used(0)==(use+1==capacity),"used means capacity exhausted");
                check(before.use(0,token).encode().equals(state.encode()),"retry on same immutable predecessor deterministic");
                check(state.use(0,token)==null&&state.use(0,null)==null,"stale and missing identities refused");
                check(before.remaining(0)==capacity-use,"predecessor retained");roundTrip(state);
            }
            while(state.energy()<6)state=state.creditExit(nextPiece++);
            check(state.used(0)&&!state.canUse(0)&&state.use(0)==null,"charge cannot buy a fifth/cap-excess use");
        }
        CompanionBattle.State state=start("nova",2).use(0);
        check(state.remaining(0)==1&&!state.used(0)&&!state.canUse(0),"remaining use differs from insufficient charge");
        String before=state.encode();check(state.use(0)==null&&state.encode().equals(before),"charge refusal spends no use");
        state=state.creditExit(63);check(state.canUse(0),"one new unique exit restores required charge");
        state=state.use(0);check(state.remaining(0)==0,"second real use available");
        check(state.creditExit(63)==state,"undo/re-exit cannot farm charge or restore capacity");
    }

    private static void legacyAndCorruption(){
        String legacy="v119_1|nova,miko|1|1|0|GUIDE|0|0|U0";
        CompanionBattle.State state=CompanionBattle.State.decode(legacy);
        check(state!=null&&state.encode().equals(legacy)&&!state.isLevelled(),"historical bytes preserved");
        check(state.capacity(0)==1&&state.remaining(0)==0&&state.capacity(1)==1,"legacy capacity fixed at one");
        check(CompanionBattle.State.start(Arrays.asList("nova")).encode().startsWith("v119_1|"),"old callers remain historical controls");
        state=state.creditExit(0).creditExit(1).creditExit(2);
        check(state.use(0)==null,"old run not retrofitted by current progression policy");roundTrip(state);
        String code=start("nova",2).creditExit(63).use(0).encode();
        for(int index=0;index<11;index++)check(CompanionBattle.State.decode(field(code,index,"corrupt"))==null,"corrupt field refused "+index);
        check(CompanionBattle.State.decode(field(code,2,"02"))==null,"noncanonical level refused");
        check(CompanionBattle.State.decode(field(code,2,"1"))==null,"level/capacity mismatch refused");
        check(CompanionBattle.State.decode(field(code,3,"4"))==null,"fabricated capacity refused");
        check(CompanionBattle.State.decode(field(code,5,"0"))==null,"erased use refused");
        check(CompanionBattle.State.decode(field(code,6,"0"))==null,"erased exit receipt refused");
        check(CompanionBattle.State.decode(field(code,10,"E63,E63,U0"))==null,"duplicate credited exit refused");
        check(CompanionBattle.State.decode(field(code,10,"U00"))==null,"noncanonical use refused");
        check(CompanionBattle.State.decode(code+"|")==null&&CompanionBattle.State.decode(null)==null,"bad record shape refused");
    }

    private static void comboAndMaximumLog(){
        CompanionBattle.State state=CompanionBattle.State.start(Arrays.asList("nova","miko","orbit"),Arrays.asList(10,10,10));
        int nextPiece=0;
        state=state.use(0).creditExit(nextPiece++).use(0);
        check(!state.comboReady(),"repeated same family does not unlock combo");
        while(state.energy()<2)state=state.creditExit(nextPiece++);
        state=state.use(1);check(state.comboReady(),"different family unlocks combo");
        String token=state.encode();check(state.useCombo("stale")==null,"stale combo refused");
        state=state.useCombo(token);check(state.comboSpent()&&!state.comboReady(),"combo one use despite higher skill capacities");
        for(int slot=0;slot<3;slot++)while(state.remaining(slot)>0){
            while(!state.canUse(slot))state=state.creditExit(nextPiece++);
            state=state.use(slot);
        }
        while(nextPiece<64)state=state.creditExit(nextPiece++);
        check(state.encode().split("\\|",-1)[10].split(",").length==77,"maximum valid log covers 64 exits + 12 uses + combo");
        check(state.useCombo()==null&&!state.comboReady(),"more families never recharge spent combo");roundTrip(state);
    }

    private static void nativeRepeatedGuide(){
        Board board=new Board(RelayCatalog.get(43));MoveBudget budget=MoveBudget.start(0,board.level);
        CompanionBattle.State state=start("nova",2);String before=state.encode();int[] certificate=board.solve();
        CompanionBattleAction.Draft first=CompanionBattleAction.prepare(board,budget,0,0,state,0,false,certificate);
        check(first!=null&&first.hintId==certificate[0]&&first.hintDurationMs==8000,"first guide exposes actual certified hint");
        check(state.encode().equals(before)&&board.moves()==0&&budget.used()==0,"prepared action does not mutate original session");
        state=first.state;board=first.board;budget=first.budget;
        check(state.remaining(0)==1&&state.energy()==1,"first guide decrements capacity and charge separately");
        int id=certificate[0];check(budget.attempt(board,id).success,"actual productive exit follows hint");state=state.creditExit(id);
        int[] nextCertificate=board.solve();
        CompanionBattleAction.Draft second=CompanionBattleAction.prepare(board,budget,0,0,state,0,false,nextCertificate);
        check(second!=null&&second.hintId==nextCertificate[0]&&second.hintId!=first.hintId,"second guide produces current-board effect");
        check(second.state.remaining(0)==0&&second.budget.used()==1&&second.board.remainingCount()==board.remainingCount(),"second guide spends no productive action");
        check(CompanionBattleAction.prepare(board,budget,0,0,second.state,0,false,nextCertificate)==null,"no third activation after captured capacity");
        CompanionBattle.State single=start("nova",1).use(0).creditExit(id);
        check(CompanionBattleAction.prepare(board,budget,0,0,single,0,false,nextCertificate)==null,"level-one control cannot produce second hint");
        state=start("nova",2);before=state.encode();
        check(CompanionBattleAction.prepare(board,budget,0,0,state,0,false,new int[]{999})==null&&state.encode().equals(before),"invalid/no-effect draft spends nothing");
    }

    private static void randomized(){
        Random random=new Random(124);
        for(int trial=0;trial<120;trial++){
            List<String> ids=new ArrayList<>();List<Integer> levels=new ArrayList<>();int members=1+random.nextInt(3);
            while(ids.size()<members){String id=CompanionBattle.all().get(random.nextInt(CompanionBattle.all().size())).id;if(!ids.contains(id)){ids.add(id);levels.add(1+random.nextInt(10));}}
            CompanionBattle.State state=CompanionBattle.State.start(ids,levels);
            for(int action=0;action<100;action++){
                String before=state.encode();int slot=random.nextInt(5)-1;CompanionBattle.State next;
                switch(random.nextInt(4)){
                    case 0:next=state.use(slot);break;
                    case 1:next=state.useCombo();break;
                    default:next=state.creditExit(random.nextInt(70)-2);break;
                }
                check(state.encode().equals(before),"immutable randomized predecessor");if(next!=null)state=next;
                check(state.energy()>=0&&state.energy()<=6,"charge bounds");
                for(int index=0;index<members;index++)check(state.usedCount(index)>=0&&state.remaining(index)>=0
                        &&state.usedCount(index)+state.remaining(index)==state.capacity(index),"captured-capacity conservation");
                roundTrip(state);
            }
        }
    }
    public static void main(String[] args){validationAndSnapshot();allCapacitiesAndCharge();legacyAndCorruption();comboAndMaximumLog();nativeRepeatedGuide();randomized();System.out.println("CompanionBattle v1.24: "+checks+" checks passed (pure state and native action model; no rendering/human claims)");}
}
