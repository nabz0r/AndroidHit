package cognitive_lab;

import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;

/** Versioned causal boundaries, actual talent parity, denominators and historical control. */
public final class CognitiveLab122Test {
    static int checks;static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    static void parity()throws Exception{
        CognitiveLab.Config c=new CognitiveLab.Config();c.days=8;
        for(Profile p:Profile.generate(12,c.seed))for(int rep=0;rep<3;rep++){
            CognitiveLab.Trajectory old=CognitiveLab.run(c,p,rep,"current_model",null,false);CognitiveLab122.Run control=CognitiveLab122.run(c,p,rep,"control_v121",null);
            check(old.cleared==control.cleared&&old.actions==control.actions&&old.blocked==control.blocked&&old.undos==control.undos&&old.shellTaps==control.shellTaps,"historical exact-key control mechanics");
            check(old.coins==control.coins&&old.energySpent==control.energySpent&&old.knowledge==control.knowledge&&old.sessionMinutes==control.sessionMinutes,"historical control economy learning timing");
            check(control.automatedExits==0&&control.skillUses==0&&control.cueExposures==0,"control no candidate intervention");
            check(control.attempts==control.cleared+control.budgetFailures+control.horizonCensored+control.administrativeCensored+control.outcomes.getOrDefault("agent_give_up",0),"all starts have exact mutually exclusive outcome");
        }
    }
    static void cues(){
        Environment122 e=new Environment122(14,1,0,"cause_immediate");int bad=-1;for(Tile p:e.board.level.tiles)if(!e.board.canMove(p.id)&&e.shells.layers(p.id)==0){bad=p.id;break;}
        check(bad>=0,"test blocked piece exists");Agent122.Observation before=e.observe(100,180,false);double[] knowledge=new Agent122(CognitiveLabTest.middle()).base.knowledge.clone();
        e.apply122(Agent122.decision(bad,"test"),before,100,2);Agent122.Observation cue=e.observe(102,180,false);check(cue.causeRule>=0&&cue.causeSource.startsWith("visible_blocked_"),"actual last-error content exposed");check(e.observe(106,180,false).causeRule==-1,"cause expires after3200ms");check(e.observe(102,180,true).causeRule==-1,"transfer removes cause");
        Agent122 a=new Agent122(CognitiveLabTest.middle());a.choose(cue,new Random(14));check(Arrays.equals(knowledge,a.base.knowledge),"visible cue does not grant permanent knowledge bonus");
        Environment122 r=new Environment122(14,1,0,"cause_repeat");Agent122.Observation o=r.observe(100,180,false);r.apply122(Agent122.decision(bad,"test"),o,100,1);check(r.observe(101,180,false).causeRule==-1,"no repeat cue on first error");r.apply122(Agent122.decision(bad,"test"),r.observe(101,180,false),101,1);check(r.observe(102,180,false).causeRule>=0&&r.repeatedErrors==1,"second identical error cue, exact counter");
    }
    static void skills(){
        boolean[] covered=new boolean[6];for(CompanionBattle.Definition definition:CompanionBattle.all()){
            int family=definition.effect.ordinal();if(covered[family])continue;covered[family]=true;
            Environment122 e=new Environment122(43,1,0,"control_v121");e.companion=CompanionBattle.State.start(Arrays.asList(definition.id));
            if(definition.effect==CompanionBattle.Effect.REWIND){int first=e.board.solve()[0];while(e.shells.layers(first)>0)e.shells.tap(first);e.apply122(Agent122.decision(first,"setup"),e.observe(0,180,false),0,1);}
            int used=e.budget.used(),remaining=e.board.remainingCount(),undos=e.undos;CompanionBattle.State state=e.companion;CompanionBattleAction.Draft expected=CompanionBattleAction.prepare(e.board,e.budget,0,undos,state,0,false,e.board.solve());
            Environment.Result result=e.apply122(Agent122.decision(Agent122.SKILL_BASE,"test"),e.observe(1,180,false),1,1);
            check(expected!=null&&!result.kind.equals("skill_unavailable"),"family effect actually applicable "+definition.effect);
            check(e.board.remainingMask()==expected.board.remainingMask()&&e.board.gateMask()==expected.board.gateMask()&&e.budget.used()==expected.budget.used()&&e.companion.encode().equals(expected.state.encode()),"actual native CompanionBattleAction parity "+definition.effect);
            check(e.automatedExits==expected.performed.length,"automatic exit counter "+definition.effect);check(e.undos-undos==expected.rewound.length,"rewind counter "+definition.effect);
            check(e.budget.used()>=used,"talent never refunds policy6 actions");
            if(definition.effect==CompanionBattle.Effect.FLOW||definition.effect==CompanionBattle.Effect.PULSE)check(e.board.remainingCount()<remaining&&e.automatedExits>0,"execution changes actual board");
            if(definition.effect==CompanionBattle.Effect.GUIDE||definition.effect==CompanionBattle.Effect.TRAIL)check(e.observe(2,180,false).board.visibleHint>=0,"information reaches agent only as exposed cue");
            Agent122.Observation removed=e.observe(2,180,true);check(removed.board.visibleHint==-1&&removed.visionLegal==0&&removed.skillFamilies.length==0&&!removed.board.deadEndWarning&&!removed.board.focusReady,"all aids off for transfer");
        }
        for(boolean family:covered)check(family,"all six actual families covered");
    }
    static void boundary(Path root)throws Exception{
        String source=Files.readString(root.resolve("scripts/cognitive_lab/Agent122.java"));for(String forbidden:new String[]{"import com.androidhit",".solve(",".hint(",".guidance(",".solution",".isSafeMove("})check(!source.contains(forbidden),"new policy no oracle "+forbidden);
        check(Arrays.equals(new Agent122(CognitiveLabTest.middle()).base.knowledge,new CognitiveAgent(CognitiveLabTest.middle(),"current_model").knowledge),"same initial knowledge");
        CognitiveLab.Config c=new CognitiveLab.Config();for(String s:CognitiveLab122.SCENARIOS){CognitiveLab122.Run t=CognitiveLab122.run(c,CognitiveLabTest.middle(),1,s,null);check(t.blocked==t.blockedReasons[0]+t.blockedReasons[1]+t.blockedReasons[2],"exhaustive reasons sum "+s);check(t.coins==180+t.coinsEarned-t.coinsSpent,"coins conserved "+s);check(t.manualExits+t.automatedExits>=t.cleared*2,"exit counter bounded "+s);}
    }
    public static void main(String[] args)throws Exception{parity();cues();skills();boundary(Paths.get(args[0]));System.out.println(Json.encode(Json.obj("passed",true,"checks",checks,"scope","Frozen control behavior, cue timing/content/no knowledge bonus, actual native six-family effects, manual/automated exits, exhaustive reason/outcome conservation, no policy solver import. All behavioral outputs synthetic.")));}
}
