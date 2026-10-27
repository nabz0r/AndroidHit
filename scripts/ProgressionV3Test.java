import com.androidhit.lumadrift.engine.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Exact offline oracle, save compatibility and reproducible V2/V3 policy comparisons. */
public final class ProgressionV3Test {
    static long checks;
    static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    static String decimal(double value){return String.format(Locale.ROOT,"%.4f",value);}
    static String percent(double value){return String.format(Locale.ROOT,"%.2f%%",value*100);}

    static void validate(Level level,ProgressionAnalysis metrics,int mode) {
        Board board=new Board(level);MoveBudget budget=MoveBudget.start(mode,level);
        for(int id:level.solution) {
            long mask=board.remainingMask();int gates=board.gateMask();
            for(Tile tile:level.tiles) {
                boolean legal=metrics.can(mask,gates,tile);
                check(board.canMove(tile.id)==legal,"Independent oracle and Board legal exits agree");
                if(legal)check(board.isSafeMove(tile.id)==(metrics.p(mask&~(1L<<tile.id),gates^tile.switchMask)>0),"Exact safe hint agrees with independent probability/reachability");
            }
            check(budget.consumeAction()&&board.move(id).success,"Certified path fits its initial budget without powers or payment");
            for(Barrier wall:level.barriers)check(board.isBarrierActive(wall)==board.isPresent(wall.keyTileId),"Seal state is derived exactly from its key");
            Board restored=new Board(level);
            check(restored.restoreHistory(board.history())&&restored.remainingMask()==board.remainingMask()&&restored.gateMask()==board.gateMask(),"Every history prefix restores the exact board");
            MoveBudget restoredBudget=MoveBudget.restore(mode,level,budget.used(),0,0,board.moves(),0);
            check(restoredBudget!=null&&restoredBudget.base()==budget.base()&&restoredBudget.remaining()==budget.remaining(),"Every budget prefix restores unchanged");
            check(board.undo()&&board.remainingMask()==mask&&board.gateMask()==gates,"Undo restores gates, pieces and seals");
            check(board.move(id).success,"Redo certified exit");
        }
        check(board.isSolved()&&budget.extensions()==0,"Completed with no continuation");
        board.restart();check(board.remainingMask()==level.allTilesMask&&board.gateMask()==level.initialGateMask,"Restart reproduces frozen board");
    }

    static boolean oracle(Level level,long mask,int gates,Map<Long,Boolean> memo) {
        if(mask==0)return true;
        long key=(mask<<4)|gates;Boolean cached=memo.get(key);if(cached!=null)return cached;
        for(Tile tile:level.tiles) {
            if((mask&(1L<<tile.id))==0||(gates&tile.direction.bit)==0)continue;
            boolean free=true;
            for(Tile other:level.tiles)if(tile.id!=other.id&&(mask&(1L<<other.id))!=0&&ProgressionAnalysis.onRay(tile,other.row,other.col)){free=false;break;}
            for(Barrier wall:level.barriers)if((mask&(1L<<wall.keyTileId))!=0&&ProgressionAnalysis.onRay(tile,wall.row,wall.col)){free=false;break;}
            if(free&&oracle(level,mask&~(1L<<tile.id),gates^tile.switchMask,memo)){memo.put(key,true);return true;}
        }
        memo.put(key,false);return false;
    }

    static void alternateStates(Level level) {
        Random random=new Random(level.seed);Map<Long,Boolean> memo=new HashMap<>();
        for(int i=0;i<16;i++) {
            long mask=random.nextLong()&level.allTilesMask;int gates=i;
            int[] solution=Solver.solve(level,mask,gates);
            check((solution!=null)==oracle(level,mask,gates,memo),"Independent arbitrary-subset/gate oracle agrees");
            if(solution!=null) {
                ProgressionAnalysis reference=new ProgressionAnalysis(level);
                for(int id:solution){Tile tile=level.tile(id);check(reference.can(mask,gates,tile),"Returned subset solution respects keys and gates");mask&=~(1L<<id);gates^=tile.switchMask;}
                check(mask==0,"Subset solution removes every requested piece");
            }
        }
    }

    static void legacySessions() {
        for(boolean remix:new boolean[]{false,true})for(int number=1;number<=(remix?240:1000);number++) {
            Level level=remix?BalancedCatalog.remixV2(number):BalancedCatalog.getV2(number);
            check(level.progressionVersion==2&&level.barriers.isEmpty(),"Frozen G/H content remains obstacle free");
            String code=remix?ChallengeCode.encodeBalancedRemixV2(number):ChallengeCode.encodeBalancedV2(number);
            check(ChallengeCode.parse(code).resolve()==level,"Historical challenge resolves the original cached board");
            check(MoveBudget.start(4,level).isUnlimited(),"G/H friend import remains unlimited");
            Board board=new Board(level);MoveBudget budget=MoveBudget.start(remix?3:0,level);
            check(budget.mistakeAllowance()==BalancedCatalog.mistakeAllowanceV2(number,remix),"G/H keeps its exact original allowance");
            int prefix=Math.min(3,level.tiles.size());
            for(int i=0;i<prefix;i++){check(board.move(level.solution[i]).success&&budget.consumeAction(),"Old run prefix");}
            check(board.undo()&&budget.consumeAction(),"Old run undo");
            Board restored=new Board(ChallengeCode.parse(code).resolve());
            check(restored.restoreHistory(board.history())&&restored.remainingMask()==board.remainingMask()&&restored.gateMask()==board.gateMask(),"Active 1.5 G/H history remains exact");
            MoveBudget old=MoveBudget.restore(remix?3:0,level,budget.used(),budget.extensions(),budget.extraActions(),board.moves(),1);
            check(old!=null&&old.base()==budget.base()&&old.used()==budget.used()&&old.remaining()==budget.remaining(),"Active 1.5 policy-2 counters remain exact");
        }
        for(long seed:new long[]{0,1,123,Long.MAX_VALUE})for(int mode:new int[]{1,2,4}) {
            Level event=LevelCatalog.event(seed);Board board=new Board(event);MoveBudget budget=MoveBudget.start(mode,event);
            check(event.progressionVersion==0&&ChallengeCode.parse(ChallengeCode.encodeEvent(seed)).resolve().seed==seed,"Events keep original identity");
            int id=event.solution[0];check(board.move(id).success&&budget.consumeAction(),"Event move");
            MoveBudget restored=MoveBudget.restore(mode,event,budget.used(),0,0,board.moves(),0);
            check(restored!=null&&restored.base()==budget.base()&&restored.policyVersion()==2,"Event policy remains save compatible");
        }
    }

    public static void main(String[] args)throws Exception {
        long began=System.nanoTime();Path folder=Paths.get(args.length>0?args[0]:"docs/data");Files.createDirectories(folder);
        legacySessions();
        StringBuilder csv=new StringBuilder("family,number,source,difficulty,beat,element,keys,seals,pieces,v2_exact_legal,v3_exact_legal,v2_decisions,v3_decisions,v2_unsafe,v3_unsafe,v3_choices,v3_first_trap_step,v3_initial_legal,v3_states,v3_dead_states,v2_margin,v3_margin,v2_blind_wins,v3_blind_wins,blind_trials\n");
        StringBuilder summary=new StringBuilder("# V3 progression: exact model audit\n\nThis is a deterministic technical model, not calibrated human difficulty, retention or enjoyment. The legal policy chooses uniformly among legal exits; its success probability is computed exactly over every reachable subset. Blind taps select any present piece and consume one action even when blocked; 100 fixed seeded runs are simulated per board. No powers, purchases, retries, undo or extensions are used in either baseline.\n\n");
        Set<String> allUnique=new HashSet<>();int allBoards=0;
        for(boolean remix:new boolean[]{false,true}) {
            int total=remix?240:1000,keyBoards=0,seals=0,recoveryPairs=0,rises=0;
            double oldTotal=0,newTotal=0,previous=0,oldPrevious=0,ratio=0,oldRatio=0;
            int oldBlind=0,newBlind=0;
            double[] phase=new double[4],oldPhase=new double[4],tier=new double[3];int[] phaseN=new int[4],tierN=new int[3];
            boolean[] sources=new boolean[total+1];
            summary.append("## ").append(remix?"Remix":"Journey").append("\n\n");
            for(int number=1;number<=total;number++) {
                Level old=remix?BalancedCatalog.remixV2(number):BalancedCatalog.getV2(number);
                Level level=remix?BalancedCatalog.remix(number):BalancedCatalog.get(number);
                ProgressionInfo info=BalancedCatalog.info(number,remix);
                ProgressionAnalysis before=new ProgressionAnalysis(old),now=new ProgressionAnalysis(level);
                validate(level,now,remix?3:0);
                check(level.progressionVersion==3&&info.version==3&&level.number==number,"V3 content metadata");
                check(info.sourceNumber>=1&&info.sourceNumber<=total&&!sources[info.sourceNumber],"Every frozen source is reused once");sources[info.sourceNumber]=true;
                Level source=remix?BalancedCatalog.remixV2(info.sourceNumber):BalancedCatalog.getV2(info.sourceNumber);
                if(!remix&&number==12) {
                    check(level.initialGateMask==15&&level.tiles.size()<=9&&now.probability==1,"Isolated key lesson has no gate-order traps");
                    for(Tile tile:level.tiles){Tile base=source.tile(tile.id);check(tile.row==base.row&&tile.col==base.col&&tile.direction==base.direction&&tile.switchMask==0,"Key lesson reuses frozen positions/arrows with explicitly neutral switches");}
                } else check(level.tiles.equals(source.tiles)&&level.initialGateMask==source.initialGateMask,"Frozen source geometry reused");
                check(Arrays.equals(level.solution,source.solution),"Certified source order retained");
                check(allUnique.add(ProgressionAnalysis.canonical(level)),"Every J/K board differs under D4, translations and piece IDs, including key links");
                String code=remix?ChallengeCode.encodeBalancedRemixV3(number):ChallengeCode.encodeBalancedV3(number);
                check(ChallengeCode.parse(code).resolve()==level,"J/K code resolves exactly");
                check(!code.equals(remix?ChallengeCode.encodeBalancedRemixV2(number):ChallengeCode.encodeBalancedV2(number)),"New identities cannot reinterpret old codes");
                check(info.barrierCount==level.barriers.size()&&info.keyCount<=2&&info.barrierCount<=3,"Sparse explicit seal bounds");
                check(!level.barriers.isEmpty()==GenerateProgressionV3.keys(number,remix),"Declared seal cadence is exact");
                if(!level.barriers.isEmpty()) {
                    keyBoards++;seals+=level.barriers.size();
                    check(info.objective==ProgressionInfo.Objective.OPEN_SEALS&&info.element==ProgressionInfo.Element.KEYS_AND_SEALS,"Pre-level objective describes actual content");
                    for(Barrier wall:level.barriers) {
                        Level withoutSeals=new Level(level.number,level.chapter,level.size,level.initialGateMask,level.tiles,level.solution,false,level.seed);
                        Board prefix=new Board(withoutSeals);for(int id:level.solution){if(id==wall.keyTileId)break;prefix.move(id);}
                        boolean actualBlock=false;for(Tile tile:source.tiles)if(prefix.canMove(tile.id)&&ProgressionAnalysis.onRay(tile,wall.row,wall.col))actualBlock=true;
                        check(actualBlock,"Each seal obstructs a genuinely legal exit before its key leaves");
                    }
                    if(number<=30||number%60<8)alternateStates(level);
                }
                if(!remix&&number<12)check(level.barriers.isEmpty(),"Keys are not introduced before Journey 12");
                if(!remix&&(number==12||number==14||number==17))check(info.keyCount==1&&info.barrierCount==1&&info.difficulty==ProgressionInfo.Difficulty.NORMAL,"Three key lessons remain simple authored introductions");
                int oldMargin=BalancedCatalog.mistakeAllowanceV2(number,remix),margin=BalancedCatalog.mistakeAllowance(number,remix);
                check(margin>=3&&margin<=5&&MoveBudget.start(remix?3:0,level).mistakeAllowance()==margin,"Fixed transparent allowance");
                long seed=0x5633424C494E444CL^(remix?0x52454D4958L:0)^number;
                int oldWins=before.blindWins(oldMargin,100,seed),wins=now.blindWins(margin,100,seed);
                oldBlind+=oldWins;newBlind+=wins;oldTotal+=before.probability;newTotal+=now.probability;
                int beat=info.beat.ordinal(),difficulty=info.difficulty.ordinal();
                phase[beat]+=now.probability;oldPhase[beat]+=before.probability;phaseN[beat]++;
                tier[difficulty]+=now.probability;tierN[difficulty]++;
                if(number>10&&(number-1)%10==0){recoveryPairs++;if(now.probability>previous)rises++;ratio+=now.probability/previous;oldRatio+=before.probability/oldPrevious;}
                previous=now.probability;oldPrevious=before.probability;
                csv.append(remix?"remix":"journey").append(',').append(number).append(',').append(info.sourceNumber).append(',')
                        .append(info.difficulty).append(',').append(info.beat).append(',').append(info.element).append(',').append(info.keyCount).append(',')
                        .append(info.barrierCount).append(',').append(level.tiles.size()).append(',').append(before.probability).append(',').append(now.probability)
                        .append(',').append(before.decisions).append(',').append(now.decisions).append(',').append(before.unsafe).append(',').append(now.unsafe)
                        .append(',').append(now.choices).append(',').append(now.firstTrap).append(',').append(now.initialLegal).append(',').append(now.states)
                        .append(',').append(now.deadStates).append(',').append(oldMargin).append(',').append(margin).append(',').append(oldWins).append(',').append(wins).append(",100\n");
                allBoards++;
            }
            check(keyBoards==(remix?29:167),"Audited sparse key-board count");
            check(phase[3]/phaseN[3]>phase[2]/phaseN[2],"Recovery beats have higher exact naive success than peaks in aggregate");
            check(tier[0]/tierN[0]>tier[1]/tierN[1]&&tier[1]/tierN[1]>tier[2]/tierN[2],"Authored tiers separate on the declared technical metric");
            summary.append("- Boards: ").append(total).append("; key boards: ").append(keyBoards).append("; fixed cells: ").append(seals).append(".\n")
                    .append("- Mean exact legal-random success: V2 ").append(percent(oldTotal/total)).append(" → V3 ").append(percent(newTotal/total)).append(".\n")
                    .append("- Seeded blind-tap wins: V2 ").append(oldBlind).append('/').append(total*100).append(" → V3 ").append(newBlind).append('/').append(total*100).append(".\n")
                    .append("- Peak→next-wave recovery increases: ").append(rises).append('/').append(recoveryPairs).append(" pairs; mean technical ratio V2 ")
                    .append(decimal(oldRatio/recoveryPairs)).append(" → V3 ").append(decimal(ratio/recoveryPairs)).append(".\n\n")
                    .append("| Authored beat | Boards | V2 exact mean at same positions | V3 exact mean |\n|---|---:|---:|---:|\n");
            for(ProgressionInfo.Beat beat:ProgressionInfo.Beat.values()){int i=beat.ordinal();summary.append('|').append(beat).append('|').append(phaseN[i]).append('|').append(percent(oldPhase[i]/phaseN[i])).append('|').append(percent(phase[i]/phaseN[i])).append("|\n");}
            summary.append("\n| Authored label | Boards | V3 exact mean |\n|---|---:|---:|\n");
            for(ProgressionInfo.Difficulty label:ProgressionInfo.Difficulty.values()){int i=label.ordinal();summary.append('|').append(label).append('|').append(tierN[i]).append('|').append(percent(tier[i]/tierN[i])).append("|\n");}
            summary.append('\n');
        }
        check(allUnique.size()==1240,"All 1240 boards unique under the declared geometric equivalence");
        summary.append("## Validation scope\n\nAll 1,240 J/K certified completions fit N exits within the initial N+3..5 allowance. Each legal choice along every certificate is compared with a separate exact reference model; saved histories, gates, key states and budget counters are replayed at every certificate prefix. Selected key boards additionally compare arbitrary piece subsets and all 16 gate masks with a brute-force oracle. All 1,240 G/H active-session prefixes and policy-2 counters are separately restored using their original layouts and allowances. Historical C/R/B/Q/G/H/P and event codes, geometry, seeds, solution orders and allowances are protected by the pre-change SHA-256 fingerprints.\n\nThe reported exact probability is one deliberately naive policy. It excludes human learning, intent, accessibility, frustration, retries and commercial outcomes. Normal/Hard/Expert are authored comparative labels; they have not been calibrated against people. Runtime, Android UI and upgrade installation are separate checks.\n");
        Files.write(folder.resolve("progression-v3-audit.csv"),csv.toString().getBytes(StandardCharsets.UTF_8));
        Files.write(folder.resolve("progression-v3-summary.md"),summary.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("PASS "+checks+" checks; "+allBoards+" V3 boards, independent exact oracle, genuine seals, D4 uniqueness, historical sessions, codes and fixed budgets; "+decimal((System.nanoTime()-began)/1e9)+" s.");
    }
}
