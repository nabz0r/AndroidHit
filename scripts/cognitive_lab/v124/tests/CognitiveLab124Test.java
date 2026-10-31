package cognitive_lab;

import com.androidhit.lumadrift.data.CompanionProgression;
import com.androidhit.lumadrift.data.RankCopyRules124;
import com.androidhit.lumadrift.engine.Board;
import com.androidhit.lumadrift.engine.Direction;
import com.androidhit.lumadrift.engine.Level;
import com.androidhit.lumadrift.engine.Tile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

/** Real environment transitions and explicit fresh-save adapter boundaries. */
public final class CognitiveLab124Test {
    private static int checks;
    private static void check(boolean good,String message){checks++;if(!good)throw new AssertionError(message);}
    private static void progression(){
        Progression124 p=new Progression124(Arrays.asList("nova","hoot","orbit"));
        check(p.xp==0&&p.rank()==1&&p.copies.get("nova")==1,"fresh snapshot starts without invented progress");
        Environment124 before=new Environment124(4,1,0,"native_rank_124",p.levelsFor(Collections.singletonList("nova")));
        String encoded=before.companion.encode();
        for(int n=1;n<=4;n++){
            Map<String,Object> win=p.win("journey:"+n,3,"nova");check(win!=null,"unique clear accepted");
            check(((Number)win.get("xpEarned")).intValue()==RankCopyRules124.xpForStars(3),"native XP helper used");
        }
        check(p.xp==140&&p.rankAwards==1&&p.copies.get("nova")==2,"first rank gives one captain copy");
        check(p.copies.get("hoot")==1&&p.copies.get("orbit")==1,"other team members never receive captain award");
        check(before.companion.capacity(0)==1&&before.companion.encode().equals(encoded),"awarded rank cannot retrofit active state");
        Environment124 after=new Environment124(5,2,0,"native_rank_124",p.levelsFor(Collections.singletonList("nova")));
        check(after.companion.capacity(0)==2,"next run captures real increased capacity");
        check(p.win("journey:1",3,"nova")==null&&p.xp==140&&p.copies.get("nova")==2,"replay does not duplicate XP/copy");
        p.xp=120*100;p.copies.put("nova",CompanionProgression.MAX_COPIES);
        p.xp+=100;
        Map<String,Object> max=p.win("journey:max",3,"nova");
        check(((Number)max.get("ranksAwarded")).intValue()==1&&p.copies.get("nova")==92
                &&((Number)max.get("overflowCoins")).intValue()==RankCopyRules124.MAX_CAPTAIN_DUPLICATE_COINS,"max-copy rank compensation follows native helper");
    }
    private static void environments(){
        for(int level=1;level<=1000;level++)for(String condition:CognitiveLab124.SCENARIOS){
            java.util.List<Integer> levels=condition.equals("loaned_trio_rank_124")?Arrays.asList(2,1,1):Collections.singletonList(2);
            Environment124 e=new Environment124(level,1,0,condition,levels);
            check(e.shells.totalLayers()==0&&e.row122("fixture",0,0,"horizon_censored").get("scenario").equals(condition),"native geometry/factory and export label");
            if(condition.equals("manual_control_124"))check(e.companion==null,"manual comparison exposes no activated talent");
            else check(e.companion.capacity(0)==(condition.equals("single_use_control_124")?1:2),"control capacity differs only by declared policy");
        }
        Environment124 e=new Environment124(43,1,0,"native_rank_124",Collections.singletonList(2));
        Agent122.Observation observation=e.observe(0,180,false);
        Environment.Result first=e.apply122(Agent122.decision(Agent122.SKILL_BASE,"fixture_skill"),observation,0,1);
        check(first.kind.equals("skill_guide")&&e.companion.usedCount(0)==1,"first actual GUIDE applied");
        observation=e.observe(1,180,false);check(observation.board.cueSource.equals("companion_guide"),"policy sees only exposed first cue");
        int id=observation.board.visibleHint;
        Environment.Result exit=e.apply122(Agent122.decision(id,"fixture_visible_exit"),observation,1,1);
        check(exit.kind.equals("exit")&&e.companion.energy()==2,"actual exit credits shared charge");
        observation=e.observe(2,180,false);
        Environment.Result second=e.apply122(Agent122.decision(Agent122.SKILL_BASE,"fixture_skill"),observation,2,1);
        check(second.kind.equals("skill_guide")&&e.companion.usedCount(0)==2,"second actual GUIDE applied with native action executor");
        observation=e.observe(3,180,true);
        check(observation.board.visibleHint==-1&&observation.causeRule==-1&&observation.skillFamilies.length==0
                &&observation.visionLegal==0&&!observation.board.focusReady&&!observation.board.deadEndWarning,"common probes remove all declared assistance");
        Environment124 noHelp=new Environment124(43,1,0,"manual_control_124",Collections.singletonList(2));
        Environment.Result refused=noHelp.apply122(Agent122.decision(Agent122.SKILL_BASE,"fixture_refusal"),noHelp.observe(0,180,false),0,1);
        check(refused.kind.equals("skill_unavailable")&&noHelp.automatedExits==0,"disabled companion cannot automate a probe");
    }
    private static void noOracle(Path root)throws Exception{
        for(String name:new String[]{"CognitiveAgent","Agent122"}){
            String source=Files.readString(root.resolve("scripts/cognitive_lab/"+name+".java"));
            for(String forbidden:new String[]{"import com.androidhit",".solve(",".hint(",".guidance(",".solution",".isSafeMove("})
                check(!source.contains(forbidden),"bounded policy no oracle "+forbidden);
        }
        String current=Files.readString(root.resolve("scripts/cognitive_lab/v124/Agent124.java"));
        for(String forbidden:new String[]{"import com.androidhit",".solve(",".hint(",".guidance(",".solution",".isSafeMove("})
            check(!current.contains(forbidden),"versioned scope has no oracle "+forbidden);
    }
    private static Agent122.Observation projection(Level level){
        CognitiveAgent.Piece[] pieces=new CognitiveAgent.Piece[level.tiles.size()];
        for(Tile t:level.tiles)pieces[t.id]=new CognitiveAgent.Piece(t.id,t.row,t.col,t.direction.ordinal(),t.switchMask);
        return new Agent122.Observation(new CognitiveAgent.Observation(pieces,new CognitiveAgent.Wall[0],level.allTilesMask,level.initialGateMask,
                new int[pieces.length],10,-1,false,false,false,0,"none"),-1,"none",0,new int[]{0},new boolean[]{true},false);
    }
    private static boolean considers(CognitiveAgent.Decision decision,int id){
        for(Map<String,Object> row:decision.candidates)if(((Number)row.get("action")).intValue()==id)return true;return false;
    }
    private static Random attentive(){return new Random(124){private static final long serialVersionUID=1L;@Override public double nextDouble(){return .99;}};}
    private static void memoryScope(){
        Level a=new Level(7001,0,3,15,Arrays.asList(new Tile(0,1,0,Direction.RIGHT,0),new Tile(1,1,1,Direction.UP,0)),
                new int[]{1,0},false,1,Collections.emptyList(),6);
        Level b=new Level(7002,0,3,15,Arrays.asList(new Tile(0,1,0,Direction.LEFT,0),new Tile(1,1,1,Direction.UP,0)),
                new int[]{0,1},false,2,Collections.emptyList(),6);
        Board physicalA=new Board(a),physicalB=new Board(b);
        check(physicalA.remainingMask()==physicalB.remainingMask()&&physicalA.gateMask()==physicalB.gateMask()
                &&!physicalA.canMove(0)&&physicalB.canMove(0),"real geometries share mask/gates but differ in piece-zero freedom");
        Agent122.Observation oa=projection(a),ob=projection(b);Profile profile=Profile.generate(1,124).get(0);
        Agent122 legacy=new Agent122(profile);Arrays.fill(legacy.base.knowledge,1);legacy.base.remember(oa.board.present,oa.board.gates,0);
        legacy.refused.add(Agent122.key(oa,Agent122.SKILL_BASE));
        check(legacy.refused.contains(Agent122.key(ob,Agent122.SKILL_BASE)),"regression reproduces cross-geometry skill refusal alias");
        check(!considers(legacy.choose(ob,attentive(),false),0),"regression reproduces valid action suppressed by another geometry");
        Agent124 fixed=new Agent124(profile);fixed.enterBoard("fixture",oa.board.pieces,oa.board.walls);
        fixed.feedback(oa,Agent122.decision(0,"blocked_fixture"),"blocked","BLOCKED_PATH",1);
        fixed.feedback(oa,Agent122.decision(Agent122.SKILL_BASE,"refused_fixture"),"skill_unavailable","NONE",1);
        Arrays.fill(fixed.base.knowledge,1);fixed.base.decisions=7;fixed.base.totalNodes=9;
        CognitiveAgent before=fixed.base;double fatigue=before.fatigue,frustration=before.frustration;
        fixed.enterBoard("fixture",oa.board.pieces,oa.board.walls);
        check(fixed.base==before&&fixed.delegate.refused.contains(Agent122.key(oa,Agent122.SKILL_BASE)),"same-geometry retries retain useful negative memories");
        fixed.enterBoard("fixture",ob.board.pieces,ob.board.walls);
        check(fixed.base!=before&&fixed.delegate.refused.isEmpty()&&fixed.base.consecutiveBlocked==0,"different geometry clears only old episodic negatives");
        check(Arrays.equals(before.knowledge,fixed.base.knowledge)&&fixed.base.fatigue==fatigue&&fixed.base.frustration==frustration
                &&fixed.base.decisions==7&&fixed.base.totalNodes==9,"learned rules and accumulated state remain transferable");
        check(considers(fixed.choose(ob,attentive(),false),0),"new geometry allows genuinely free previously blocked ID");
        CognitiveAgent same=fixed.base;fixed.enterBoard("another-native-board",ob.board.pieces,ob.board.walls);
        check(fixed.base!=same,"actual native identity also scopes identical visible geometry");
    }
    public static void main(String[] args)throws Exception{progression();environments();memoryScope();noOracle(Paths.get(args[0]));
        System.out.println(Json.encode(Json.obj("status","PASS","checks",checks,"scope","Fresh-save rank-to-captain copy adapter,1000 native factories across4conditions,real second GUIDE,no cue/oracle in common probes;not Android persistence or human behavior.")));}
}
