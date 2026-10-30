package cognitive_lab;

import java.util.*;

/**
 * Decision boundary: visible immutable projections only. This class cannot access
 * Board, Level, catalog certificates, exact guidance, or evaluator references.
 * Its forward search is a bounded, fallible mental model, not the game solver.
 */
final class CognitiveAgent {
    static final int UNDO=-1,FOCUS=-2,STOP=-3;
    static final String[] RULES={"gate","path","switch","barrier","shell"};
    static final class Piece {final int id,row,col,direction,switchMask;Piece(int id,int row,int col,int direction,int switches){this.id=id;this.row=row;this.col=col;this.direction=direction;switchMask=switches;}}
    static final class Wall {final int row,col,key;Wall(int row,int col,int key){this.row=row;this.col=col;this.key=key;}}
    static final class Observation {
        final Piece[] pieces;final Wall[] walls;final long present;final int gates,remainingBudget,visibleHint,undoCost;final int[] shells;
        final boolean focusReady,canUndo,deadEndWarning;final String cueSource;
        Observation(Piece[] pieces,Wall[] walls,long present,int gates,int[] shells,int budget,int hint,boolean focus,boolean undo,boolean dead,int cost,String source){
            this.pieces=pieces;this.walls=walls;this.present=present;this.gates=gates;this.shells=shells;remainingBudget=budget;visibleHint=hint;focusReady=focus;canUndo=undo;deadEndWarning=dead;undoCost=cost;cueSource=source;}
    }
    static final class Decision {
        final int action,nodes;final String reason;final double value;final List<Map<String,Object>> candidates;
        Decision(int action,int nodes,String reason,double value,List<Map<String,Object>> candidates){this.action=action;this.nodes=nodes;this.reason=reason;this.value=value;this.candidates=candidates;}
        Map<String,Object> json(){return Json.obj("action",action,"kind",action==UNDO?"undo":action==FOCUS?"request_earned_focus":action==STOP?"stop":"tap_piece","reason",reason,"nodes",nodes,"estimatedValue",value,"candidates",candidates);}
    }
    final Profile profile;final String variant;final double[] knowledge=new double[5];
    double fatigue,frustration;int consecutiveBlocked,decisions,totalNodes;private final LinkedHashMap<String,Integer> memories=new LinkedHashMap<>();
    private int nodes,nodeCap,depthLimit;private boolean[] recognized;private Observation view;
    CognitiveAgent(Profile p,String variant){profile=p;this.variant=variant;for(int r=0;r<5;r++)knowledge[r]=.28+(r==1?.22:0);if(variant.equals("teaching_support"))for(int r=0;r<5;r++)knowledge[r]=Math.min(.95,knowledge[r]+.16);}
    Decision choose(Observation o,Random rng){
        decisions++;view=o;List<Map<String,Object>> candidates=new ArrayList<>();
        double lapse=Math.min(.7,profile.attentionLapse+fatigue*.14+frustration*.045);
        if(o.visibleHint>=0&&rng.nextDouble()>lapse)return new Decision(o.visibleHint,0,"follow_visible_"+o.cueSource+"_cue",1,candidates);
        if(o.deadEndWarning&&o.canUndo&&rng.nextDouble()>lapse)return new Decision(UNDO,0,"respond_to_visible_dead_end_warning",0,candidates);
        if(o.focusReady&&(frustration>.22||Long.bitCount(o.present)>9)&&rng.nextDouble()<.55+.35*profile.explorationInterest)return new Decision(FOCUS,0,"request_native_earned_focus_cue",0,candidates);
        List<Integer> present=new ArrayList<>();for(Piece piece:o.pieces)if((o.present&(1L<<piece.id))!=0)present.add(piece.id);
        if(present.isEmpty())return new Decision(STOP,0,"empty_board",0,candidates);
        if(rng.nextDouble()<lapse)return new Decision(present.get(rng.nextInt(present.size())),0,"attention_lapse_visible_piece",0,candidates);
        recognized=new boolean[5];for(int r=0;r<5;r++)recognized[r]=rng.nextDouble()<knowledge[r];
        depthLimit=variant.equals("low_planning")?1:Math.max(1,Math.min(profile.planningDepth,profile.workingMemory-1)-(int)(fatigue*1.5));
        nodeCap=variant.equals("low_planning")?12:Math.max(6,(int)(profile.searchCap/(1+fatigue)));nodes=0;
        List<Integer> possible=mentalLegal(o.present,o.gates);Collections.shuffle(possible,rng);
        int best=-99;double bestValue=-1e20;int considered=0;
        for(int id:possible){if(considered>=profile.workingMemory||nodes>=nodeCap)break;if(memories.containsKey(key(o.present,o.gates,id)))continue;considered++;
            Piece p=o.pieces[id];long next=o.present&~(1L<<id);int gates=recognized[2]?o.gates^p.switchMask:o.gates;
            // Recognizing shells changes anticipated interaction/time burden;
            // an unlearned shell is visible but its extra taps are not understood.
            double score=1+search(next,gates,depthLimit-1)+.012*rng.nextDouble()-(recognized[4]?.09*o.shells[id]:0);
            candidates.add(Json.obj("action",id,"value",score));if(score>bestValue){bestValue=score;best=id;}}
        totalNodes+=nodes;
        if(best>=0)return new Decision(best,nodes,"bounded_perceived_rules_depth_"+depthLimit,bestValue,candidates);
        if(o.canUndo)return new Decision(UNDO,nodes,"no_remembered_productive_action",0,candidates);
        if(consecutiveBlocked>=5)return new Decision(STOP,nodes,"repeated_blocked_attempts",0,candidates);
        return new Decision(present.get(rng.nextInt(present.size())),nodes,"explore_unrecognized_rule",0,candidates);
    }
    private double search(long mask,int gates,int depth){
        nodes++;if(mask==0)return 2;List<Integer> legal=mentalLegal(mask,gates);if(legal.isEmpty())return -3;
        if(depth==0||nodes>=nodeCap)return .025*Math.min(6,legal.size());
        double best=-3;int count=0;for(int id:legal){if(nodes>=nodeCap||count++>=Math.max(1,profile.workingMemory-2))break;Piece p=view.pieces[id];
            best=Math.max(best,.92*(1+search(mask&~(1L<<id),recognized[2]?gates^p.switchMask:gates,depth-1)));}return best;
    }
    private List<Integer> mentalLegal(long mask,int gates){
        List<Integer> out=new ArrayList<>();for(Piece p:view.pieces){if((mask&(1L<<p.id))==0)continue;if(recognized[0]&&(gates&(1<<p.direction))==0)continue;boolean blocked=false;
            if(recognized[1])for(Piece q:view.pieces)if(q.id!=p.id&&(mask&(1L<<q.id))!=0&&onRay(p,q.row,q.col)){blocked=true;break;}
            if(!blocked&&recognized[3])for(Wall w:view.walls)if((mask&(1L<<w.key))!=0&&onRay(p,w.row,w.col)){blocked=true;break;}
            if(!blocked)out.add(p.id);}return out;
    }
    private static boolean onRay(Piece p,int row,int col){return p.direction==0?col==p.col&&row<p.row:p.direction==1?row==p.row&&col>p.col:p.direction==2?col==p.col&&row>p.row:row==p.row&&col<p.col;}
    void feedback(Observation before,int action,String outcome,String blockedReason,double seconds){
        if(!variant.equals("no_fatigue"))fatigue=Math.min(2,fatigue+seconds/60*profile.fatigueRate);
        if(outcome.equals("blocked")){consecutiveBlocked++;frustration=Math.min(2,frustration+.10*profile.frustrationSensitivity);remember(before.present,before.gates,action);
            learn(blockedReason.equals("CLOSED_GATE")?0:blockedReason.equals("LOCKED_BARRIER")?3:1,.9);}
        else if(outcome.equals("exit")){consecutiveBlocked=0;frustration=Math.max(0,frustration-.055);learn(0,.13);learn(1,.13);if(before.pieces[action].switchMask!=0)learn(2,.9);}
        else if(outcome.equals("shell")){learn(4,.9);frustration=Math.max(0,frustration-.012);}
        else if(outcome.equals("undo")){consecutiveBlocked=0;frustration=Math.max(0,frustration-.02);}
    }
    void learn(int rule,double evidence){if(variant.equals("no_learning"))return;double rate=profile.learningRate*(variant.equals("teaching_support")?1.7:1);knowledge[rule]=Math.min(.995,knowledge[rule]+rate*evidence*(1-knowledge[rule]));}
    void remember(long mask,int gates,int id){memories.put(key(mask,gates,id),id);while(memories.size()>profile.workingMemory)memories.remove(memories.keySet().iterator().next());}
    void overnight(){for(int r=0;r<knowledge.length;r++)knowledge[r]=.15+(knowledge[r]-.15)*(1-profile.forgettingRate);fatigue*=.1;frustration*=.55;memories.clear();consecutiveBlocked=0;}
    double meanKnowledge(){double n=0;for(double v:knowledge)n+=v;return n/knowledge.length;}
    private static String key(long mask,int gates,int id){return mask+":"+gates+":"+id;}
}
