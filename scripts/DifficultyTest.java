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
                    Level mapped=remix?BalancedCatalog.legacyRemix(i):BalancedCatalog.legacyGet(i);
                    check(mapped.number==i&&mapped.chapter==(i-1)/30+1,"balanced numbering");
                    check(mapped.size==original.size&&mapped.seed==original.seed&&mapped.initialGateMask==original.initialGateMask&&mapped.isEvent==original.isEvent,"layout metadata unchanged");
                    check(mapped.tiles.equals(original.tiles)&&Arrays.equals(mapped.solution,original.solution),"tiles/order unchanged");
                    Board b=new Board(mapped);for(int id:mapped.solution)check(b.move(id).success,"mapped solution");check(b.isSolved(),"mapped solvability");
                    check(Arrays.equals(new Board(mapped).solve(),new Board(original).solve()),"exact solver preserved");
                    check(mapped==(remix?BalancedCatalog.legacyRemix(i):BalancedCatalog.legacyGet(i)),"cache reuse");
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
            boolean rejected=false;try{if(remix)BalancedCatalog.legacyRemix(invalid);else BalancedCatalog.legacyGet(invalid);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"invalid index rejected");
        }
        if(!emit)for(boolean remix:new boolean[]{false,true}) {
            int maximum=remix?BalancedCatalog.REMIX_COUNT:BalancedCatalog.LEVEL_COUNT;
            boolean rejected=false;
            try{if(remix)BalancedCatalog.remix(maximum+1);else BalancedCatalog.get(maximum+1);}catch(IllegalArgumentException expected){rejected=true;}
            check(rejected,"Current route upper bound rejected");
        }
        Files.write(folder.resolve("difficulty.csv"),csv.toString().getBytes(StandardCharsets.UTF_8));
        Files.write(folder.resolve("difficulty-summary.md"),report.toString().getBytes(StandardCharsets.UTF_8));
        if(emit)Files.write(folder.resolve("difficulty-orders.txt"),orders.toString().getBytes(StandardCharsets.UTF_8));
        if(!emit) auditVersionTwo(folder);
        double seconds=(System.nanoTime()-began)/1e9;
        System.out.println("Difficulty checks: "+checks+" passed; 480 historical +1000 Journey +240 Remix V2 layouts; exact certified solutions, non-isomorphic uniqueness and naive-policy baselines.");
        System.out.println("Elapsed host time: "+decimal(seconds)+" seconds; not an Android performance measurement.");
        System.out.println("Reports: "+folder.toAbsolutePath());
    }
    private static double adjacent(int[] a,boolean rises){int sum=0;for(int i=1;i<a.length;i++)sum+=rises?Math.max(0,a[i]-a[i-1]):Math.abs(a[i]-a[i-1]);return sum/(double)(a.length-1);}
    /** Exact probability for a deliberately naive policy: choose uniformly among legal exits. */
    private static final class ChoiceMetrics {
        final Level level;
        final long[] rays;
        final double[] probabilities;
        final double randomLegalProbability;
        int choices, unsafe, decisions, forced, edges, switches, paired, initialLegal;
        double score;
        ChoiceMetrics(Level level) {
            this.level=level;
            rays=new long[level.tiles.size()];
            probabilities=new double[1<<level.tiles.size()];
            Arrays.fill(probabilities,-1);probabilities[0]=1;
            for(Tile tile:level.tiles) {
                if(tile.switchMask!=0)switches++;
                if(Integer.bitCount(tile.switchMask)>1)paired++;
                for(Tile other:level.tiles) {
                    int dr=other.row-tile.row,dc=other.col-tile.col;
                    if(tile.id!=other.id&&(tile.direction.dr==0?dr==0&&dc*tile.direction.dc>0:dc==0&&dr*tile.direction.dr>0)) {
                        rays[tile.id]|=1L<<other.id;edges++;
                    }
                }
            }
            randomLegalProbability=probability(level.allTilesMask,level.initialGateMask);
            Board board=new Board(level);
            for(int next:level.solution) {
                List<Tile> legal=board.legalMoves();int dangerous=0;
                if(board.moves()==0)initialLegal=legal.size();
                for(Tile tile:legal) {
                    boolean safe=probability(board.remainingMask()&~(1L<<tile.id),board.gateMask()^tile.switchMask)>0;
                    check(safe==board.isSafeMove(tile.id),"Exact random-policy reachability agrees with independent solver");
                    if(!safe)dangerous++;
                }
                if(legal.size()==1)forced++;
                if(dangerous>0&&dangerous<legal.size())decisions++;
                unsafe+=dangerous;choices+=legal.size();
                check(board.move(next).success,"V2 certified solution remains legal");
            }
            check(board.isSolved(),"V2 certified completion");
            score=3*level.tiles.size()+2*switches+paired+10*-Math.log(Math.max(.0001,randomLegalProbability))+1.5*decisions+.2*edges;
        }
        boolean canMove(long remaining,int gates,Tile tile) {
            return (remaining&(1L<<tile.id))!=0&&(gates&tile.direction.bit)!=0&&(remaining&rays[tile.id])==0;
        }
        double probability(long remaining,int gates) {
            int index=(int)remaining;
            if(probabilities[index]>=0)return probabilities[index];
            double sum=0;int count=0;
            // Gate state is uniquely determined by the removed-piece set: XOR is commutative.
            for(Tile tile:level.tiles)if(canMove(remaining,gates,tile)) {
                sum+=probability(remaining&~(1L<<tile.id),gates^tile.switchMask);count++;
            }
            return probabilities[index]=count==0?0:sum/count;
        }
        int blindWins(int actions,int trials,long randomSeed) {
            int wins=0;
            for(int trial=0;trial<trials;trial++) {
                Random random=new Random(randomSeed^trial);
                Board board=new Board(level);
                for(int step=0;step<actions&&!board.isSolved();step++) {
                    List<Tile> present=board.remainingTiles();
                    board.move(present.get(random.nextInt(present.size())).id);
                }
                if(board.isSolved())wins++;
            }
            return wins;
        }
    }

    /** Remove IDs and canonicalize all eight rotations/reflections, including arrows and gates. */
    private static String canonical(Level level) {
        String best=null;
        for(int transform=0;transform<8;transform++) {
            ArrayList<String> rows=new ArrayList<>();
            int minRow=Integer.MAX_VALUE,minCol=Integer.MAX_VALUE;
            int[][] coordinates=new int[level.tiles.size()][2];
            for(Tile tile:level.tiles) {
                int row=tile.row,col=tile.col;
                if(transform>=4)col=level.size-1-col;
                for(int rotation=0;rotation<transform%4;rotation++){int next=col;col=level.size-1-row;row=next;}
                coordinates[tile.id][0]=row;coordinates[tile.id][1]=col;minRow=Math.min(minRow,row);minCol=Math.min(minCol,col);
            }
            for(Tile tile:level.tiles) {
                int row=coordinates[tile.id][0]-minRow,col=coordinates[tile.id][1]-minCol;
                rows.add(row+","+col+","+transformedDirection(tile.direction.ordinal(),transform)+","+transformedMask(tile.switchMask,transform));
            }
            Collections.sort(rows);
            String key=transformedMask(level.initialGateMask,transform)+":"+String.join(";",rows);
            if(best==null||key.compareTo(best)<0)best=key;
        }
        return best;
    }
    private static int transformedDirection(int direction,int transform) {
        int reflected=transform>=4&&(direction==1||direction==3)?4-direction:direction;
        return (reflected+transform%4)%4;
    }
    private static int transformedMask(int mask,int transform) {
        int result=0;for(int direction=0;direction<4;direction++)if((mask&(1<<direction))!=0)result|=1<<transformedDirection(direction,transform);
        return result;
    }
    private static void auditVersionTwo(Path folder) throws Exception {
        StringBuilder csv=new StringBuilder("family,number,chapter,legacy_pieces,legacy_budget,v2_pieces,v2_budget,v2_margin,legacy_exact_random_legal,v2_exact_random_legal,legacy_choice_points,v2_choice_points,v2_unsafe_path_choices,v2_path_legal_choices,legacy_blind_wins,v2_blind_wins,blind_trials,v2_structural_score\n");
        StringBuilder report=new StringBuilder("# Version 2 progression: reproducible before/after audit\n\n")
                .append("Journey has 1,000 frozen G boards; Remix has 240 frozen H boards. C/R and B/Q retain their original 240-per-family layouts. Every solved board needs exactly N exits; the displayed allowance is N plus a fixed per-board mistake margin. Both policies allow free restart. No spending, ad, failure or individual player signal changes content or allowance.\n\n")
                .append("The exact baseline chooses uniformly among legal exits, with no undo, powers or continuations. It is a deliberately naive technical policy, not human difficulty or a prediction of completion/retention. A separate seeded blind-tap simulation chooses uniformly among all present pieces and counts blocked taps against each version's own allowance (100 runs per board per version). Old/new comparisons cover the same first 240 positions; later Journey boards have no historical equivalent.\n\n");
        for(boolean remix:new boolean[]{false,true}) {
            int total=remix?BalancedCatalog.REMIX_COUNT:BalancedCatalog.LEVEL_COUNT;
            report.append("## ").append(remix?"Remix":"Journey").append("\n\n")
                    .append("| Chapter | Old legal-random success | V2 legal-random success | Old blind success | V2 blind success | V2 pieces | V2 budget | V2 margin | V2 mean choice points |\n|---|---:|---:|---:|---:|---|---|---|---:|\n");
            double oldTotal=0,matchedNewTotal=0,newTotal=0;int oldBlindTotal=0,matchedNewBlind=0,newBlindTotal=0;
            Set<String> unique=new HashSet<>();
            double firstChapter=0,lastChapter=0;
            for(int chapter=1;chapter<=(total+29)/30;chapter++) {
                int first=(chapter-1)*30+1,last=Math.min(total,chapter*30),count=last-first+1;
                double oldChance=0,newChance=0,decisions=0,previousScore=-1;
                int oldBlind=0,newBlind=0,minPieces=99,maxPieces=0,minBase=99,maxBase=0,minMargin=99,maxMargin=0;
                for(int number=first;number<=last;number++) {
                    int mode=remix?3:0;
                    Level legacy=number<=240?(remix?BalancedCatalog.legacyRemix(number):BalancedCatalog.legacyGet(number)):null;
                    Level current=remix?BalancedCatalog.remixV2(number):BalancedCatalog.getV2(number);
                    ChoiceMetrics old=legacy==null?null:new ChoiceMetrics(legacy),now=new ChoiceMetrics(current);
                    MoveBudget oldBudget=legacy==null?null:MoveBudget.startLegacy(mode,legacy),budget=MoveBudget.start(mode,current);
                    check(current.number==number&&current.chapter==chapter,"V2 numbering and chapters");
                    check(unique.add(canonical(current)),"Every V2 board is distinct even under rotations, reflections, translations and reordered IDs");
                    if(number<=3) {
                        Level teaching=remix?LevelCatalog.remix(number):LevelCatalog.get(number);
                        check(canonical(current).equals(canonical(teaching)),"Three introductory layouts preserved");
                        check(now.randomLegalProbability==1,"Introductions permit experimentation with legal exits");
                    } else {
                        check(now.randomLegalProbability>0&&now.randomLegalProbability<1,"Every post-introduction board requires a meaningful gate-order choice");
                        check(now.decisions>=1&&now.unsafe>=1,"Every post-introduction certified path exposes a safe-versus-dangerous branch");
                        check(now.initialLegal>=2,"A reasoning board starts with more than one legal exit");
                        check(now.score+1e-8>=previousScore,"Frozen chapter order avoids structural-score shuffling");
                        previousScore=now.score;
                    }
                    check(budget.base()==current.tiles.size()+budget.mistakeAllowance(),"Allowance is necessary exits plus transparent margin");
                    check(budget.mistakeAllowance()>=2&&budget.mistakeAllowance()<=4,"A legal mistake can be repaired with free undo within initial allowance");
                    Board solved=new Board(current);
                    for(int id:current.solution)check(budget.consumeAction()&&solved.move(id).success,"Every V2 board fits without payment, powers or continuation");
                    check(solved.isSolved()&&budget.extensions()==0,"Certified completion fits initial allowance");
                    long seed=0x425544474554L^(remix?0x52454D4958L:0)^((long)number<<20);
                    int oldWins=old==null?0:old.blindWins(oldBudget.base(),100,seed),newWins=now.blindWins(budget.base(),100,seed);
                    if(old!=null){oldChance+=old.randomLegalProbability;oldBlind+=oldWins;matchedNewTotal+=now.randomLegalProbability;matchedNewBlind+=newWins;}
                    newChance+=now.randomLegalProbability;newBlind+=newWins;decisions+=now.decisions;
                    int n=current.tiles.size(),base=budget.base(),margin=budget.mistakeAllowance();
                    minPieces=Math.min(minPieces,n);maxPieces=Math.max(maxPieces,n);minBase=Math.min(minBase,base);maxBase=Math.max(maxBase,base);minMargin=Math.min(minMargin,margin);maxMargin=Math.max(maxMargin,margin);
                    csv.append(remix?"remix":"journey").append(',').append(number).append(',').append(chapter).append(',').append(legacy==null?"":legacy.tiles.size()).append(',').append(oldBudget==null?"":oldBudget.base()).append(',').append(n).append(',').append(base).append(',').append(margin).append(',').append(old==null?"":old.randomLegalProbability).append(',').append(now.randomLegalProbability).append(',').append(old==null?"":old.decisions).append(',').append(now.decisions).append(',').append(now.unsafe).append(',').append(now.choices).append(',').append(old==null?"":oldWins).append(',').append(newWins).append(",100,").append(now.score).append('\n');
                }
                oldTotal+=oldChance;newTotal+=newChance;oldBlindTotal+=oldBlind;newBlindTotal+=newBlind;
                if(chapter==1)firstChapter=newChance/count;lastChapter=newChance/count;
                report.append('|').append(chapter).append('|').append(first<=240?decimal(oldChance/count*100)+"%":"—").append('|').append(decimal(newChance/count*100)).append("%|").append(first<=240?decimal(oldBlind/(double)count)+"%":"—").append('|').append(decimal(newBlind/(double)count)).append("%|").append(minPieces).append('–').append(maxPieces).append('|').append(minBase).append('–').append(maxBase).append('|').append(minMargin).append('–').append(maxMargin).append('|').append(decimal(decisions/count)).append("|\n");
            }
            check(matchedNewTotal/240<oldTotal/240-.20,"Naive legal completion decreases materially over matched first 240 positions");
            check(lastChapter<firstChapter/2,"Final chapter requires materially more planning than first chapter");
            check(unique.size()==total,"Full route has the advertised count of non-isomorphic layouts");
            report.append("\nMatched first 240 positions: exact legal-random success **").append(decimal(oldTotal/240*100)).append("% → ").append(decimal(matchedNewTotal/240*100)).append("%**; blind taps **").append(decimal(oldBlindTotal/240.0)).append("% → ").append(decimal(matchedNewBlind/240.0)).append("%**.\n\n")
                  .append("Full V2 route: **").append(total).append(" distinct layouts even after canonicalizing all eight rotations/reflections, translations, arrows, switch directions and gate states**. Mean exact legal-random success **").append(decimal(newTotal/total*100)).append("%**; blind-tap success **").append(decimal(newBlindTotal/(double)total)).append("%**.\n\n");
        }
        report.append("Legacy attempts restore legacyGet/legacyRemix with MoveBudget.restoreLegacy and policy 1 until completion/abandonment. G/H attempts use getV2/remixV2 and policy 2. Earned progression and inventory are not reset. G/H codes prevent historical B/Q shares from silently opening different boards. Exact model measurements do not establish player enjoyment or a calibrated human difficulty curve.\n");
        Files.write(folder.resolve("progression-v2-audit.csv"),csv.toString().getBytes(StandardCharsets.UTF_8));
        Files.write(folder.resolve("progression-v2-summary.md"),report.toString().getBytes(StandardCharsets.UTF_8));
    }
    private static int maxRise(int[] a){int max=0;for(int i=1;i<a.length;i++)max=Math.max(max,a[i]-a[i-1]);return max;}
    private static int spikes(int[] a){int n=0;for(int i=1;i<a.length;i++)if(a[i]-a[i-1]>30)n++;return n;}
    private static String decimal(double value){return String.format(Locale.ROOT,"%.2f",value);}
}
