import com.androidhit.lumadrift.engine.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Exact-board structural audit. Scores are declared heuristics, not human difficulty. */
public final class DifficultyTest {
    private static int checks;
    private static void check(boolean ok, String message) {
        checks++; if (!ok) throw new AssertionError(message);
    }
    private static final class Metrics {
        int pieces, blocked, closed, switches, paired, edges, forced, legal, unsafe, score;
        Metrics(Level level) {
            pieces=level.tiles.size(); Board board=new Board(level);
            for (Tile tile:level.tiles) {
                if (board.blockingTile(tile.id)>=0) blocked++;
                if (!board.isGateOpen(tile.direction)) closed++;
                if (tile.switchMask!=0) switches++;
                if (Integer.bitCount(tile.switchMask)>1) paired++;
                for (Tile other:level.tiles) if (tile.id!=other.id) {
                    int dr=other.row-tile.row,dc=other.col-tile.col;
                    if (tile.direction.dr==0?dr==0&&dc*tile.direction.dc>0:dc==0&&dr*tile.direction.dr>0) edges++;
                }
            }
            for (int next:level.solution) {
                List<Tile> candidates=board.legalMoves();
                if (candidates.size()==1) forced++;
                for (Tile candidate:candidates) { legal++; if (!board.isSafeMove(candidate.id)) unsafe++; }
                check(board.move(next).success,"certified solution must be legal");
            }
            check(board.isSolved(),"certified board solved");
            int risk=legal==0?0:(20*unsafe+legal/2)/legal;
            score=10*pieces+5*blocked+4*closed+10*switches+12*paired+2*edges+2*forced+risk;
        }
        String csv(){return pieces+","+blocked+","+closed+","+switches+","+paired+","+edges+","+forced+","+legal+","+unsafe+","+score;}
    }
    public static void main(String[] args) throws Exception {
        long began=System.nanoTime();
        boolean emit=args.length>0&&args[0].equals("--emit-order");
        Path folder=Paths.get(emit?args.length>1?args[1]:"/private/tmp/luma-difficulty":args.length>0?args[0]:"docs/data");
        Files.createDirectories(folder);
        StringBuilder csv=new StringBuilder("family,balanced_number,original_number,chapter,original_position_score,pieces,initial_blocked,initial_closed,switches,paired_switches,ray_edges,forced_steps,path_legal_choices,path_unsafe_choices,structural_score\n");
        StringBuilder report=new StringBuilder("# Difficulty audit\n\n");
        StringBuilder orders=new StringBuilder();
        for (boolean remix:new boolean[]{false,true}) {
            Metrics[] source=new Metrics[241];
            for (int i=1;i<=240;i++) source[i]=new Metrics(remix?LevelCatalog.remix(i):LevelCatalog.get(i));
            int[] proposed=new int[240]; for(int i=0;i<240;i++) proposed[i]=i+1;
            final int prefix=remix?3:6;
            for(int start=1;start<=240;start+=30){
                int first=Math.max(start,prefix+1),last=Math.min(240,start+29);
                List<Integer> candidates=new ArrayList<>(); for(int i=first;i<=last;i++)candidates.add(i);
                candidates.sort((a,b)->source[a].score!=source[b].score?Integer.compare(source[a].score,source[b].score):Integer.compare(a,b));
                for(int i=first;i<=last;i++)proposed[i-1]=candidates.get(i-first);
            }
            orders.append(remix?"REMIX":"JOURNEY").append("=").append(Arrays.toString(proposed)).append('\n');
            boolean[] seen=new boolean[241];
            int[] before=new int[240],after=new int[240];
            for(int i=1;i<=240;i++){
                int old=emit?proposed[i-1]:BalancedCatalog.sourceNumber(i,remix);
                check(emit||old==proposed[i-1],"frozen order follows the declared score and stable tie break");
                check(old>=1&&old<=240&&!seen[old],"mapping is a permutation"); seen[old]=true;
                check((old-1)/30==(i-1)/30,"content remains in its original chapter");
                check(i>prefix||old==i,"teaching prefix stays frozen");
                Level original=remix?LevelCatalog.remix(old):LevelCatalog.get(old);
                if(!emit){
                    Level mapped=remix?BalancedCatalog.remix(i):BalancedCatalog.get(i);
                    check(mapped.number==i&&mapped.chapter==(i-1)/30+1,"balanced numbering");
                    check(mapped.size==original.size&&mapped.seed==original.seed&&mapped.initialGateMask==original.initialGateMask&&mapped.isEvent==original.isEvent,"layout metadata unchanged");
                    check(mapped.tiles.equals(original.tiles)&&Arrays.equals(mapped.solution,original.solution),"tiles/order unchanged");
                    Board b=new Board(mapped);for(int id:mapped.solution)check(b.move(id).success,"mapped solution");check(b.isSolved(),"mapped solvability");
                    check(Arrays.equals(new Board(mapped).solve(),new Board(original).solve()),"exact solver preserved");
                    check(mapped==(remix?BalancedCatalog.remix(i):BalancedCatalog.get(i)),"cache reuse");
                }
                before[i-1]=source[i].score;after[i-1]=source[old].score;
                csv.append(remix?"remix":"journey").append(',').append(i).append(',').append(old).append(',').append((i-1)/30+1).append(',').append(source[i].score).append(',').append(source[old].csv()).append('\n');
            }
            check(adjacent(after,false)<adjacent(before,false),"absolute local variability reduced");
            check(adjacent(after,true)<adjacent(before,true),"mean positive rise reduced");
            check(maxRise(after)<=maxRise(before),"worst upward spike not increased");
            check(spikes(after)<spikes(before),"large upward spike count reduced");
            report.append("## ").append(remix?"Remix":"Journey").append("\n\n")
                    .append("| Measure | Original | Balanced |\n|---|---:|---:|\n")
                    .append("| Mean absolute adjacent change | ").append(decimal(adjacent(before,false))).append(" | ").append(decimal(adjacent(after,false))).append(" |\n")
                    .append("| Mean positive adjacent rise | ").append(decimal(adjacent(before,true))).append(" | ").append(decimal(adjacent(after,true))).append(" |\n")
                    .append("| Largest positive adjacent rise | ").append(maxRise(before)).append(" | ").append(maxRise(after)).append(" |\n")
                    .append("| Positive jumps greater than 30 points | ").append(spikes(before)).append(" | ").append(spikes(after)).append(" |\n\n")
                    .append("| Chapter | Mean score | Minimum | Maximum |\n|---|---:|---:|---:|\n");
            int previousMean=-1;
            for(int ch=0;ch<8;ch++){int total=0,originalTotal=0,min=Integer.MAX_VALUE,max=0;for(int i=ch*30;i<(ch+1)*30;i++){total+=after[i];originalTotal+=before[i];min=Math.min(min,after[i]);max=Math.max(max,after[i]);}check(total==originalTotal,"chapter content and mean conserved");check(total>previousMean,"chapter mean increases");previousMean=total;report.append('|').append(ch+1).append('|').append(decimal(total/30.0)).append('|').append(min).append('|').append(max).append("|\n");}
            report.append('\n');
        }
        if(!emit)for(int invalid:new int[]{Integer.MIN_VALUE,-1,0,241,Integer.MAX_VALUE})for(boolean remix:new boolean[]{false,true}){
            boolean rejected=false;try{if(remix)BalancedCatalog.remix(invalid);else BalancedCatalog.get(invalid);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"invalid index rejected");
        }
        Files.write(folder.resolve("difficulty.csv"),csv.toString().getBytes(StandardCharsets.UTF_8));
        Files.write(folder.resolve("difficulty-summary.md"),report.toString().getBytes(StandardCharsets.UTF_8));
        if(emit)Files.write(folder.resolve("difficulty-orders.txt"),orders.toString().getBytes(StandardCharsets.UTF_8));
        double seconds=(System.nanoTime()-began)/1e9;
        System.out.println("Difficulty checks: "+checks+" passed; 240 Journey +240 Remix; exact certified solutions and structural sampling.");
        System.out.println("Elapsed host time: "+decimal(seconds)+" seconds; not an Android performance measurement.");
        System.out.println("Reports: "+folder.toAbsolutePath());
    }
    private static double adjacent(int[] a,boolean rises){int sum=0;for(int i=1;i<a.length;i++)sum+=rises?Math.max(0,a[i]-a[i-1]):Math.abs(a[i]-a[i-1]);return sum/(double)(a.length-1);}
    private static int maxRise(int[] a){int max=0;for(int i=1;i<a.length;i++)max=Math.max(max,a[i]-a[i-1]);return max;}
    private static int spikes(int[] a){int n=0;for(int i=1;i<a.length;i++)if(a[i]-a[i-1]>30)n++;return n;}
    private static String decimal(double value){return String.format(Locale.ROOT,"%.2f",value);}
}
