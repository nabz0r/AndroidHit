import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;

/** Offline-only authoring of the separately versioned, reversible L/M strategy route. */
public final class GenerateStrategyV4 {
    static int dependencyDepth(Level level){int[] depth=new int[level.tiles.size()];int max=0;for(int i=0;i<level.solution.length;i++){Tile t=level.tile(level.solution[i]);int value=1;for(Tile other:level.tiles)if(other.id!=t.id&&ray(t,other.row,other.col))value=Math.max(value,1+depth[other.id]);for(Barrier wall:level.barriers)if(ray(t,wall.row,wall.col))value=Math.max(value,1+depth[wall.keyTileId]);depth[t.id]=value;max=Math.max(max,value);}return max;}
    static int[] choiceMetrics(Level level,StrategySolver.Session planner,int[]plan){
        long mask=level.allTilesMask;int gates=level.initialGateMask,decisions=0,forced=0,penalty=0;
        Board board=new Board(level);
        for(int action:plan){int optimum=planner.minimumActions(mask,gates),optimal=0,choices=0,maxCost=optimum;
            for(Tile tile:level.tiles)if(board.canMove(tile.id)){int cost=1+planner.minimumActions(mask&~(1L<<tile.id),gates^tile.switchMask);choices++;if(cost==optimum)optimal++;maxCost=Math.max(maxCost,cost);}
            for(Direction d:Direction.values())if((gates&d.bit)==0){boolean productive=false;for(Tile tile:level.tiles)if(board.isPresent(tile.id)&&tile.direction==d&&board.blockingTile(tile.id)<0&&board.blockingBarrier(tile.id)==null)productive=true;if(productive){int cost=1+planner.minimumActions(mask,gates|d.bit);choices++;if(cost==optimum)optimal++;maxCost=Math.max(maxCost,cost);}}
            if(choices>1&&maxCost>optimum)decisions++;if(optimal==1)forced++;penalty=Math.max(penalty,maxCost-optimum);
            if(!board.performAction(action).success)throw new AssertionError("Choice metric action");mask=board.remainingMask();gates=board.gateMask();
        }
        return new int[]{decisions,forced,penalty,dependencyDepth(level)};
    }
    static boolean ray(Tile t,int r,int c){return t.direction.dr==0?r==t.row&&(c-t.col)*t.direction.dc>0:c==t.col&&(r-t.row)*t.direction.dr>0;}
    static List<Barrier> walls(Level old,List<Tile>tiles,int n,boolean remix){
        ArrayList<Barrier> result=new ArrayList<>();
        if(n>=6&&n<=10){result.addAll(old.barriers);return result;}
        if(n<21)return result;
        result.addAll(old.barriers);
        if(n%3!=0)return result;
        int wanted=n<60?1:n<480?2:3;int[]rank=new int[tiles.size()];for(int i=0;i<old.solution.length;i++)rank[old.solution[i]]=i;
        ArrayList<int[]> candidates=new ArrayList<>();
        for(int r=0;r<old.size;r++)for(int c=0;c<old.size;c++){
            boolean occupied=false;for(Tile t:tiles)if(t.row==r&&t.col==c)occupied=true;for(Barrier b:result)if(b.row==r&&b.col==c)occupied=true;if(occupied)continue;
            for(Tile key:tiles){int first=999,affected=0;for(Tile t:tiles)if(ray(t,r,c)){affected++;first=Math.min(first,rank[t.id]);}
                if(affected>0&&first>rank[key.id]&&rank[key.id]<tiles.size()/2)candidates.add(new int[]{r,c,key.id,affected});}
        }
        candidates.sort(Comparator.<int[]>comparingInt(a->-a[3]).thenComparingInt(a->a[2]).thenComparingInt(a->a[0]).thenComparingInt(a->a[1]));
        for(int[]candidate:candidates){boolean occupied=false;for(Barrier b:result)if(b.row==candidate[0]&&b.col==candidate[1])occupied=true;if(occupied)continue;
            result.add(new Barrier(candidate[0],candidate[1],candidate[2]));if(result.size()>=wanted)break;}
        return result;
    }
    public static void main(String[]args)throws Exception{
        Path root=Path.of(args.length==0?".":args[0]);StringBuilder java=new StringBuilder("package com.androidhit.lumadrift.engine;\n\n/** Offline frozen source|min|margin|gates|switchLimit|seals|optimal typed actions. */\nfinal class StrategyV4Layouts {\n    private StrategyV4Layouts() { }\n");
        StringBuilder csv=new StringBuilder("family,number,source,pieces,minimum_actions,required_gate_opens,margin,initial_gates,switches,keys,seals,planner_states,planner_ns,meaningful_decisions,forced_optimal_steps,max_one_choice_cost_penalty,dependency_depth,structural_score,difficulty,beat\n");
        for(boolean remix:new boolean[]{false,true}){int total=remix?240:1000,previousScore=0;java.append("    static final String[] ").append(remix?"REMIX":"JOURNEY").append(" = {\n");
            for(int n=1;n<=total;n++){
                int source=n;if(n>=6&&n<=10)source=(remix?new int[]{9,17,25,33,41}:new int[]{12,14,17,22,28})[n-6];
                Level old=remix?BalancedCatalog.remix(source):BalancedCatalog.get(source);int limit=n<=12?0:n<=15?1:n<=20?2:-1,used=0;
                ArrayList<Tile>tiles=new ArrayList<>();for(Tile t:old.tiles){int sw=t.switchMask;if(sw!=0&&limit>=0&&used++>=limit)sw=0;
                    if(n>=21&&sw!=0){int rotate=1+Math.floorMod(n+t.id,3);int changed=((sw<<rotate)|(sw>>>(4-rotate)))&15;if(changed==sw)changed=((sw<<1)|(sw>>>3))&15;sw=changed;}
                    tiles.add(new Tile(t.id,t.row,t.col,t.direction,sw));}
                List<Barrier>barriers=walls(old,tiles,n,remix);int initial=15;
                if(n>=11){int free=0;for(Tile t:tiles){boolean blocked=false;for(Tile b:tiles)if(b.id!=t.id&&ray(t,b.row,b.col))blocked=true;for(Barrier b:barriers)if(ray(t,b.row,b.col))blocked=true;if(!blocked)free|=t.direction.bit;}initial=n%3==0?0:15&~free;}
                long seed=old.seed^0x56344C4D00000000L^n;
                // Geometry-only draft: its exit order is not a V4 action certificate yet.
                Level draft=new Level(n,(n-1)/30+1,old.size,initial,tiles,old.solution,false,seed,barriers,3);
                StrategySolver.Session planner=new StrategySolver.Session(draft);
                if(n>=11&&n<=20){int wanted=n<=13?1:n<=16?2:3,bestLoss=999,bestOpen=-1;for(int gates=0;gates<16;gates++){int opens=planner.minimumActions(draft.allTilesMask,gates)-tiles.size();if(opens<1)continue;int loss=Math.abs(opens-wanted),openness=Integer.bitCount(gates);if(loss<bestLoss||loss==bestLoss&&openness>bestOpen){bestLoss=loss;bestOpen=openness;initial=gates;}}}
                long started=System.nanoTime();int[]plan=planner.solve(draft.allTilesMask,initial);long elapsed=System.nanoTime()-started;
                Level level=new Level(n,(n-1)/30+1,old.size,initial,tiles,old.solution,false,seed,barriers,4,plan.length,plan);
                Board board=new Board(level);for(int action:plan)if(!board.performAction(action).success)throw new AssertionError("Invalid V4 action certificate");if(!board.isSolved())throw new AssertionError("V4 certificate incomplete");
                if(n>=11&&plan.length<=tiles.size())throw new AssertionError("Productive operation missing");if(n>=6&&n<=10&&barriers.isEmpty())throw new AssertionError("Key lesson missing");
                int margin=n<=10?5:n<=20?4:((n-1)%10==0||(n-1)%10==4||(n-1)%10==7)?4:(n-1)%10==9?2:3;
                StringJoiner wall=new StringJoiner(";");Set<Integer>keys=new HashSet<>();for(Barrier b:barriers){wall.add(b.row+","+b.col+","+b.keyTileId);keys.add(b.keyTileId);}
                StringJoiner certificate=new StringJoiner(",");for(int action:plan)certificate.add(""+action);
                int[] metrics=choiceMetrics(level,planner,plan);int score=(int)Math.round(100.0*metrics[0]/plan.length)+(plan.length-tiles.size())*3+keys.size()*2+metrics[2]*2+metrics[3];
                int difficulty=n<=20?0:score>=110?2:score>=80?1:0;
                int beat=n==1||n==6||n==11||n==13?0:score<previousScore*.8?3:difficulty>0?2:1;previousScore=score;
                StringJoiner masks=new StringJoiner(",");for(Tile t:tiles)masks.add(""+t.switchMask);
                java.append("        \"").append(source).append('|').append(plan.length).append('|').append(margin).append('|').append(initial).append('|').append(limit).append('|').append(wall).append('|').append(certificate).append('|').append(masks).append('|').append(difficulty).append('|').append(beat).append('|').append(metrics[0]).append('|').append(metrics[1]).append('|').append(metrics[2]).append('|').append(metrics[3]).append('|').append(score).append('"').append(n==total?'\n':',').append(n==total?"":"\n");
                int switches=0;for(Tile t:tiles)if(t.switchMask!=0)switches++;
                csv.append(remix?"M":"L").append(',').append(n).append(',').append(source).append(',').append(tiles.size()).append(',').append(plan.length).append(',').append(plan.length-tiles.size()).append(',').append(margin).append(',').append(initial).append(',').append(switches).append(',').append(keys.size()).append(',').append(barriers.size()).append(',').append(planner.cachedStates()).append(',').append(elapsed).append(',').append(metrics[0]).append(',').append(metrics[1]).append(',').append(metrics[2]).append(',').append(metrics[3]).append(',').append(score).append(',').append(difficulty).append(',').append(beat).append('\n');
            }java.append("    };\n");}
        java.append("}\n");Files.writeString(root.resolve("app/src/main/java/com/androidhit/lumadrift/engine/StrategyV4Layouts.java"),java);
        Path report=root.resolve("docs/validation/v1.8/engine");Files.createDirectories(report);Files.writeString(report.resolve("strategy-authoring.csv"),csv);
        System.out.println("PASS: 1000 L + 240 M typed productive-action certificates authored offline.");
    }
}
