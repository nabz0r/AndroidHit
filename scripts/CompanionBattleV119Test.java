import com.androidhit.lumadrift.data.Catalog;
import com.androidhit.lumadrift.data.CompanionBattle;
import com.androidhit.lumadrift.data.CompanionBattle.Definition;
import com.androidhit.lumadrift.data.CompanionBattle.Effect;
import com.androidhit.lumadrift.data.CompanionBattle.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

/** Pure-state contract tests; no claim of native rendering, board execution or provider qualification. */
public final class CompanionBattleV119Test {
    private static int checks;
    private static void check(boolean good,String message){checks++;if(!good)throw new AssertionError(message);}
    private static State state(String... ids){State result=State.start(Arrays.asList(ids));check(result!=null,"valid team");return result;}
    private static void roundTrip(State state){check(state!=null,"state exists");State restored=State.decode(state.encode());check(restored!=null&&restored.encode().equals(state.encode()),"exact canonical restore");}
    private static String field(String encoded,int index,String value){String[] fields=encoded.split("\\|",-1);fields[index]=value;return String.join("|",fields);}
    private static void definitions(){
        check(CompanionBattle.all().size()==37,"all 37 current catalogue identities covered");
        EnumSet<Effect> effects=EnumSet.noneOf(Effect.class);
        for(Catalog.CharacterItem character:Catalog.CHARACTERS){
            Definition definition=CompanionBattle.find(character.id);
            check(definition!=null&&definition.id.equals(character.id),"exact identity definition");
            check(definition.color==character.color&&definition.signatureKey.equals("battle_signature_"+character.id),"identity visual signature");
            check(definition.cost>=2&&definition.cost<=3,"bounded per-use energy cost");effects.add(definition.effect);
            switch(definition.effect){
                case GUIDE:check(definition.amount==1&&definition.durationMs==8000,"one visible hint");break;
                case VISION:check(definition.amount==1&&definition.durationMs>=5000&&definition.durationMs<=7000,"bounded legal-highlight duration");break;
                case TRAIL:check(definition.amount>=2&&definition.amount<=4&&definition.durationMs==8000,"safe trail limits");break;
                case PULSE:check(definition.amount==1&&definition.durationMs==0,"one certified exit");break;
                case FLOW:check(definition.amount>=2&&definition.amount<=3&&definition.durationMs==0,"flow limits");break;
                case REWIND:check(definition.amount>=1&&definition.amount<=2&&definition.durationMs==0,"rewind limits");break;
                default:throw new AssertionError("Unexpected family");
            }
        }
        check(effects.size()==6,"six engine families, not 37 unique engines");
        check(CompanionBattle.find("nova").effect==Effect.GUIDE,"Nova guide");
        check(CompanionBattle.find("miko").effect==Effect.TRAIL,"Miko trail");
        check(CompanionBattle.find("orbit").effect==Effect.FLOW,"Orbit flow");
        check(CompanionBattle.find(null)==null&&CompanionBattle.find("made_up")==null,"unknown identity refused");
    }
    private static void teamValidation(){
        check(State.start(null)==null&&State.start(Collections.emptyList())==null,"empty team refused");
        check(State.start(Arrays.asList("nova","nova"))==null,"duplicate character refused");
        check(State.start(Arrays.asList("nova",null))==null,"null identity refused");
        check(State.start(Arrays.asList("nova","unknown"))==null,"unknown identity refused");
        check(State.start(Arrays.asList("nova","miko","orbit","pip"))==null,"four companions refused");
        roundTrip(state("nova"));roundTrip(state("nova","miko"));roundTrip(state("nova","miko","orbit"));
        List<String> ids=new ArrayList<>(Arrays.asList("nova","miko"));State run=State.start(ids);ids.set(0,"orbit");
        check(run.team().equals(Arrays.asList("nova","miko")),"defensive input copy");
        try{run.team().set(0,"orbit");throw new AssertionError("mutable team");}catch(UnsupportedOperationException expected){check(true,"immutable team getter");}
        check(!run.canUse(-1)&&!run.canUse(2)&&run.use(-1)==null&&run.use(2)==null,"invalid slot refused");
        check(!run.used(-1)&&!run.used(2),"invalid used query false");
    }
    private static void energyAndRetry(){
        State initial=state("nova","miko","orbit");String original=initial.encode();
        State used=initial.use(0);check(used.energy()==1&&used.used(0),"exact talent charge and use");
        check(initial.encode().equals(original)&&initial.energy()==3&&!initial.used(0),"old state unchanged");
        check(used.use(0)==null&&!used.canUse(0),"same avatar cannot repeat");
        check(used.use(1)==null&&used.use(2)==null,"insufficient energy refused");
        check(initial.use(0,null)==null&&initial.use(0,"old")==null,"missing or stale state identity refused");
        check(initial.use(0,original).encode().equals(used.encode()),"same request retry deterministic");
        check(used.use(1,original)==null,"request created before transition refused");
        for(int n=0;n<64;n++){
            State previous=used;used=used.creditExit(n);check(used.energy()==Math.min(6,previous.energy()+1),"new exit bounded energy");
            check(used.creditedExit(n),"piece receipt retained");check(used.creditExit(n)==used,"undo and re-exit cannot farm");roundTrip(used);
        }
        check(used.creditExit(-1)==null&&used.creditExit(64)==null&&used.creditExit(67)==null&&used.creditExit(1000)==null,"gates and invalid IDs never credit");
        check(used.creditedExit(63),"top mask bit supported");check(!used.creditedExit(64),"gate not a credited tile");
        used=used.use(1);check(used.energy()==4,"spend after cap");
        for(int n=63;n>=0;n--)check(used.creditExit(n)==used&&used.energy()==4,"credited cap overflow cannot become later energy");
        roundTrip(used);
    }
    private static void combinations(){
        for(Definition first:CompanionBattle.all())for(Definition second:CompanionBattle.all()){
            if(first.id.equals(second.id))continue;
            State initial=state(first.id,second.id);State run=initial.creditExit(0).creditExit(1).creditExit(2);
            check(run.energy()==6&&!run.comboReady()&&run.useCombo()==null,"no combo before two skills");
            run=run.use(0);check(run!=null&&!run.comboReady(),"one family alone does not charge combo");
            run=run.use(1);check(run!=null,"sufficient cap for two skills");
            boolean different=first.effect!=second.effect;
            check(run.comboReady()==different,"two different families required");
            if(different){
                String token=run.encode();check(run.useCombo("stale")==null,"stale combo cannot consume");
                State combined=run.useCombo(token);check(combined!=null&&combined.comboSpent()&&!combined.comboReady(),"combo explicitly used once");
                check(combined.energy()==run.energy(),"combo exact zero energy cost");
                check(combined.useCombo()==null&&run.comboReady(),"double combo refused and predecessor intact");roundTrip(combined);
            }else check(run.useCombo()==null,"same family combo refused");
            roundTrip(run);
        }
        State three=state("nova","miko","orbit").creditExit(0).creditExit(1).creditExit(2).use(0).use(1).useCombo();
        three=three.creditExit(3).creditExit(4).creditExit(5).use(2);
        check(three!=null&&three.comboSpent()&&!three.comboReady()&&three.useCombo()==null,"third family never rearms spent combo");roundTrip(three);
    }
    private static void corruptRecords(){
        State run=state("nova","miko").creditExit(63).use(0).creditExit(1).use(1);String code=run.encode();roundTrip(run);
        check(State.decode(null)==null&&State.decode("")==null&&State.decode(code+"|")==null,"bad shape refused");
        for(int field=0;field<9;field++)check(State.decode(field(code,field,"corrupt"))==null,"corrupt field "+field);
        check(State.decode(field(code,0,"v120_1"))==null,"future version refused");
        check(State.decode(field(code,1,"nova,nova"))==null,"duplicate encoded team refused");
        check(State.decode(field(code,2,"6"))==null,"fabricated energy refused");
        check(State.decode(field(code,3,"0"))==null,"erased usage refused");
        check(State.decode(field(code,4,"0"))==null,"erased anti-farm mask refused");
        check(State.decode(field(code,5,"FLOW"))==null,"fabricated last family refused");
        check(State.decode(field(code,6,"0"))==null,"fabricated combo flag refused");
        check(State.decode(field(code,7,"1"))==null,"fabricated spent flag refused");
        String events=code.split("\\|",-1)[8];
        check(State.decode(field(code,8,events+",U0"))==null,"duplicate use event refused");
        check(State.decode(field(code,8,events+",E63"))==null,"duplicate credit event refused");
        check(State.decode(field(code,8,"E064"))==null&&State.decode(field(code,8,"E64"))==null,"noncanonical and gate credit refused");
        check(State.decode(field(code,8,"U00"))==null&&State.decode(field(code,8,"C"))==null,"noncanonical or premature action refused");
        check(State.decode(String.join("",Collections.nCopies(5000,"x")))==null,"oversized input refused");
    }
    private static void randomized(){
        Random random=new Random(119);
        for(int trial=0;trial<200;trial++){
            List<String> ids=new ArrayList<>();List<Definition> definitions=CompanionBattle.all();int count=1+random.nextInt(3);
            while(ids.size()<count){String id=definitions.get(random.nextInt(definitions.size())).id;if(!ids.contains(id))ids.add(id);}
            State run=State.start(ids);
            for(int event=0;event<90;event++){
                String before=run.encode();State next;
                switch(random.nextInt(4)){
                    case 0:next=run.use(random.nextInt(5)-1);break;
                    case 1:next=run.useCombo();break;
                    default:next=run.creditExit(random.nextInt(70)-2);break;
                }
                check(run.encode().equals(before),"every attempted transition preserves prior state");
                if(next!=null)run=next;
                check(run.energy()>=0&&run.energy()<=6,"random energy range");roundTrip(run);
            }
        }
    }
    public static void main(String[] args){definitions();teamValidation();energyAndRetry();combinations();corruptRecords();randomized();System.out.println("CompanionBattle v1.19: "+checks+" checks passed (pure model only)");}
}
