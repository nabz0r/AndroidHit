import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;

/** Deterministic offline authoring. This tool never runs on a player's device. */
public final class GenerateLearningV114 {
    static final class Candidate {
        final Level level;final int source;final String lesson;final int[] metrics;final int score;
        Candidate(Level l,int source,String lesson){this.level=l;this.source=source;this.lesson=lesson;StrategySolver.Session solver=new StrategySolver.Session(l);metrics=GenerateStrategyV4.choiceMetrics(l,solver,l.actionSolution);Set<Integer> keys=new HashSet<>();for(Barrier b:l.barriers)keys.add(b.keyTileId);score=(int)Math.round(100.0*metrics[0]/l.minimumActions)+3*(l.minimumActions-l.tiles.size())+2*keys.size()+2*metrics[2]+metrics[3];}
    }
    static Tile tile(int id,int r,int c,Direction d,int sw){return new Tile(id,r,c,d,sw);}
    static int rotateMask(int mask,int turn){int out=mask;for(int i=0;i<turn;i++)out=((out<<1)|(out>>>3))&15;return out;}
    static Candidate authored(int n,int size,int gates,List<Tile> base,List<Barrier> walls,int count,int turn,String lesson){
        ArrayList<Tile> tiles=new ArrayList<>();for(int i=0;i<count;i++){Tile t=base.get(i);int row=t.row,col=t.col;for(int k=0;k<turn;k++){int next=col;col=size-1-row;row=next;}tiles.add(tile(i,row,col,Direction.values()[(t.direction.ordinal()+turn)%4],rotateMask(t.switchMask,turn)));}
        ArrayList<Barrier> seals=new ArrayList<>();for(Barrier b:walls){int row=b.row,col=b.col;for(int k=0;k<turn;k++){int next=col;col=size-1-row;row=next;}seals.add(new Barrier(row,col,b.keyTileId));}
        int[] rank=new int[count];for(int i=0;i<count;i++)rank[i]=i;
        Level draft=new Level(n,(n-1)/30+1,size,rotateMask(gates,turn),tiles,rank,false,0x4e31313400000000L+n,seals,3);
        StrategySolver.Session planner=new StrategySolver.Session(draft);int[] plan=planner.solve(draft.allTilesMask,draft.initialGateMask);int[] exits=Arrays.stream(plan).filter(a->!Board.isGateAction(a)).toArray();
        return new Candidate(new Level(n,(n-1)/30+1,size,draft.initialGateMask,tiles,exits,false,draft.seed,seals,4,plan.length,plan),0,lesson);
    }
    static List<Tile> list(Tile...tiles){return Arrays.asList(tiles);}
    static List<Candidate> introductions(){ArrayList<Candidate> out=new ArrayList<>();List<Barrier> none=Collections.emptyList();
        List<Tile> exits=list(tile(0,0,1,Direction.UP,0),tile(1,1,3,Direction.RIGHT,0),tile(2,3,2,Direction.DOWN,0),tile(3,2,0,Direction.LEFT,0),tile(4,1,1,Direction.LEFT,0));
        for(int n=1;n<=4;n++)out.add(authored(n,4,15,exits,none,n+1,0,"exits"));
        List<Tile> blockers=list(tile(0,0,1,Direction.UP,0),tile(1,1,1,Direction.UP,0),tile(2,1,4,Direction.RIGHT,0),tile(3,4,3,Direction.DOWN,0),tile(4,3,0,Direction.LEFT,0),tile(5,2,1,Direction.UP,0),tile(6,2,3,Direction.RIGHT,0),tile(7,2,4,Direction.UP,0));
        for(int n=5;n<=8;n++)out.add(authored(n,5,15,blockers,none,n,0,"blockers"));
        List<Tile> key=list(tile(0,0,2,Direction.UP,0),tile(1,4,2,Direction.UP,0),tile(2,1,4,Direction.RIGHT,0),tile(3,4,4,Direction.DOWN,0),tile(4,3,0,Direction.LEFT,0),tile(5,0,0,Direction.LEFT,0),tile(6,1,1,Direction.UP,0));
        for(int n=9;n<=12;n++)out.add(authored(n,5,15,key,Collections.singletonList(new Barrier(2,2,0)),n-5,n%4,"key"));
        List<Tile> gate=list(tile(0,0,1,Direction.UP,0),tile(1,1,4,Direction.RIGHT,0),tile(2,4,3,Direction.DOWN,0),tile(3,3,0,Direction.LEFT,0),tile(4,2,1,Direction.UP,0),tile(5,2,2,Direction.RIGHT,0));
        for(int n=13;n<=16;n++)out.add(authored(n,5,14,gate,none,n-10,(n-13)%4,"gate"));
        List<Tile> sw=list(tile(0,0,2,Direction.UP,2),tile(1,1,4,Direction.RIGHT,0),tile(2,4,3,Direction.DOWN,0),tile(3,3,0,Direction.LEFT,0),tile(4,1,1,Direction.UP,0),tile(5,4,1,Direction.LEFT,0),tile(6,2,3,Direction.RIGHT,0));
        for(int n=17;n<=20;n++)out.add(authored(n,5,13,sw,none,n-13,(n-17)%4,"switch"));
        List<Tile> order=list(tile(0,0,2,Direction.UP,2),tile(1,1,4,Direction.RIGHT,4),tile(2,4,3,Direction.DOWN,0),tile(3,3,0,Direction.LEFT,0),tile(4,1,1,Direction.UP,0),tile(5,4,1,Direction.LEFT,0),tile(6,2,3,Direction.RIGHT,0),tile(7,3,4,Direction.DOWN,0));
        for(int n=21;n<=24;n++)out.add(authored(n,5,9,order,none,n-16,(n-21)%4,"switch_order"));
        List<Tile> keySwitch=list(tile(0,0,2,Direction.UP,2),tile(1,1,4,Direction.RIGHT,4),tile(2,4,3,Direction.DOWN,0),tile(3,3,0,Direction.LEFT,0),tile(4,3,4,Direction.LEFT,0),tile(5,1,1,Direction.UP,0),tile(6,4,1,Direction.LEFT,0),tile(7,2,3,Direction.RIGHT,0),tile(8,2,4,Direction.DOWN,0));
        for(int n=25;n<=27;n++)out.add(authored(n,5,9,keySwitch,Collections.singletonList(new Barrier(3,2,3)),n-18,(n-25)%4,"key_switch"));
        List<Tile> combination=new ArrayList<>(keySwitch);combination.set(2,tile(2,4,3,Direction.DOWN,8));combination.add(tile(9,0,4,Direction.LEFT,0));
        for(int n=28;n<=30;n++)out.add(authored(n,5,1,combination,Collections.singletonList(new Barrier(3,2,3)),n-20,(n-28)%4,"combination"));
        return out;
    }
    static int switches(Candidate c){int count=0;for(Tile t:c.level.tiles)if(t.switchMask!=0)count++;return count;}
    static void addWaves(List<Candidate> output,List<Candidate> pool){
        // Bands bound mechanics first. Within them, an increasing score quantile
        // supplies small peaks/recoveries without selecting a dense board merely
        // because its structural score happens to be low.
        ArrayList<Candidate> ranked=new ArrayList<>(pool);ranked.sort(Comparator.comparingInt((Candidate c)->c.score).thenComparingInt(c->c.source));int total=pool.size();
        int[] wave={0,5,10,-8,3,8,-5,4,12,-10};
        for(int step=0;!pool.isEmpty();step++){Candidate previous=output.get(output.size()-1);double target=ranked.get(Math.min(total-1,step)).score+wave[step%wave.length];Candidate best=null;double bestValue=Double.POSITIVE_INFINITY;
            for(Candidate c:pool){int pieceJump=c.level.tiles.size()-previous.level.tiles.size(),actionJump=c.level.minimumActions-previous.level.minimumActions,switchJump=switches(c)-switches(previous),depthJump=c.metrics[3]-previous.metrics[3],scoreJump=c.score-previous.score;
                if(scoreJump>35)continue;
                double value=Math.abs(c.score-target)+40*Math.max(0,pieceJump-2)+30*Math.max(0,actionJump-4)+60*Math.max(0,switchJump-1)+20*Math.max(0,depthJump-3)+4*Math.max(0,pieceJump)+2*Math.max(0,actionJump)+8*Math.max(0,switchJump)+c.source*.000001;
                if(value<bestValue){bestValue=value;best=c;}}
            if(best==null)throw new AssertionError("No bounded score transition after N"+output.size());output.add(best);pool.remove(best);
        }
    }
    static String join(int[] values){StringJoiner out=new StringJoiner(",");for(int value:values)out.add(Integer.toString(value));return out.toString();}
    static String row(Candidate c,int margin,int difficulty,int beat){Level l=c.level;StringJoiner tiles=new StringJoiner(";");for(Tile t:l.tiles)tiles.add(t.row+","+t.col+","+t.direction.ordinal()+","+t.switchMask);StringJoiner walls=new StringJoiner(";");for(Barrier b:l.barriers)walls.add(b.row+","+b.col+","+b.keyTileId);return l.size+"|"+l.initialGateMask+"|"+l.minimumActions+"|"+margin+"|"+tiles+"|"+walls+"|"+join(l.actionSolution)+"|"+c.metrics[0]+"|"+c.metrics[1]+"|"+c.metrics[2]+"|"+c.metrics[3]+"|"+c.score+"|"+difficulty+"|"+beat+"|"+c.lesson+"|"+c.source;}
    public static void main(String[] args)throws Exception{Path root=Paths.get(args.length>0?args[0]:".");List<Candidate> all=introductions();int[] phases={30,60,100,160,240,400,1000};
        for(int phase=1;phase<phases.length;phase++){List<Candidate> pool=new ArrayList<>();for(int source=phases[phase-1]+1;source<=phases[phase];source++){Level old=StrategyCatalog.get(source);StrategySolver.Session solver=new StrategySolver.Session(old);int[] plan=solver.solve(old.allTilesMask,old.initialGateMask);if(plan.length!=old.minimumActions)throw new AssertionError("Old minimum changed");Level recertified=new Level(old.number,old.chapter,old.size,old.initialGateMask,old.tiles,Arrays.stream(plan).filter(a->!Board.isGateAction(a)).toArray(),false,old.seed,old.barriers,4,plan.length,plan);pool.add(new Candidate(recertified,source,"practice"));}addWaves(all,pool);}
        if(all.size()!=1000)throw new AssertionError("Expected 1000 boards");
        StringBuilder java=new StringBuilder("package com.androidhit.lumadrift.engine;\n\n/** Frozen N Journey. Generated offline by GenerateLearningV114; never personalised at runtime. */\nfinal class LearningLayouts {\n    private LearningLayouts() {}\n    static final String[] JOURNEY = {\n");
        StringBuilder csv=new StringBuilder("family,number,source,lesson,size,pieces,minimum_actions,required_gate_opens,margin,initial_gates,initial_legal_exits,switches,max_switch_bits,keys,seals,decision_points,forced_optimal,max_one_choice_penalty,dependency_depth,structural_score,difficulty,beat\n");int previousScore=0;
        for(int i=0;i<all.size();i++){int n=i+1;Candidate c=all.get(i);Level l=c.level;Set<Integer> keys=new HashSet<>();for(Barrier b:l.barriers)keys.add(b.keyTileId);int switches=0,bits=0;for(Tile t:l.tiles){if(t.switchMask!=0)switches++;bits=Math.max(bits,Integer.bitCount(t.switchMask));}int margin=n<=16?5:n<=30?4:n<=100?4:3;int difficulty=n<=30?0:c.score>=110?2:c.score>=80?1:0;int beat=n==1||n==5||n==9||n==13||n==17||n==21||n==25||n==28?0:c.score<previousScore*.8?3:difficulty>0?2:1;int increase=c.score-previousScore;if(n>31&&increase>35)throw new AssertionError("Unbounded positive score step N"+n+" +"+increase);previousScore=c.score;
            Board board=new Board(l);int initial=board.legalMoves().size();for(int action:l.actionSolution)if(!board.performAction(action).success)throw new AssertionError("Illegal certificate N"+n);if(!board.isSolved())throw new AssertionError("Incomplete N"+n);
            java.append("        \"").append(row(c,margin,difficulty,beat)).append('"').append(n==1000?'\n':',').append(n==1000?"":"\n");csv.append(String.format(Locale.ROOT,"N,%d,%d,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n",n,c.source,c.lesson,l.size,l.tiles.size(),l.minimumActions,l.minimumActions-l.tiles.size(),margin,l.initialGateMask,initial,switches,bits,keys.size(),l.barriers.size(),c.metrics[0],c.metrics[1],c.metrics[2],c.metrics[3],c.score,difficulty,beat));}
        java.append("    };\n}\n");Files.writeString(root.resolve("app/src/main/java/com/androidhit/lumadrift/engine/LearningLayouts.java"),java);Path report=root.resolve("docs/validation/v1.14/audit-learning");Files.createDirectories(report);Files.writeString(report.resolve("audit-learning-authoring.csv"),csv);System.out.println("PASS 1000 frozen N boards; first30explicit +970 recertified/reordered legacy geometries; positive score step afterN31<=35.");
    }
}
