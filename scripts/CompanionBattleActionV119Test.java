import com.androidhit.lumadrift.data.CompanionBattle;
import com.androidhit.lumadrift.data.CompanionBattleAction;
import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Real U boards and real MoveBudget; no Android or physical-device acceptance claim. */
public final class CompanionBattleActionV119Test {
    private static long checks,prepared,refusals;
    private static int boards;
    private static final EnumMap<CompanionBattle.Effect,Long> effects=new EnumMap<>(CompanionBattle.Effect.class);
    private static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    private static CompanionBattle.State state(String... team){return CompanionBattle.State.start(Arrays.asList(team));}
    private static int[] suffix(Board board){return Arrays.copyOfRange(board.level.solution,board.moves(),board.level.solution.length);}
    private static String ledger(MoveBudget b){MoveBudget.Snapshot s=b.snapshot();return s.policyVersion+":"+s.mode+":"+s.minimumActions+":"+s.base+":"+s.used+":"+s.extensions+":"+s.extraActions;}
    private static String snapshot(Board b,MoveBudget m,CompanionBattle.State s){return b.remainingMask()+":"+b.gateMask()+":"+Arrays.toString(b.history())+":"+ledger(m)+":"+s.encode();}
    private static CompanionBattleAction.Draft draft(Board b,MoveBudget m,int mode,int undos,CompanionBattle.State s,int slot,boolean combo,int[] plan){
        String before=snapshot(b,m,s);int[] prior=plan==null?null:plan.clone();
        CompanionBattleAction.Draft result=CompanionBattleAction.prepare(b,m,mode,undos,s,slot,combo,plan);
        check(snapshot(b,m,s).equals(before),"every accepted/refused draft leaves source board/ledger/state byte-equivalent");
        check(Arrays.equals(prior,plan),"draft does not mutate supplied certificate");
        if(result==null)refusals++;else{prepared++;check(result.board!=b&&result.budget!=m,"mutable draft owns separate board and ledger");check(result.state!=s,"accepted draft has a new immutable companion state");}
        return result;
    }
    private static void everyBoardAndIdentity(){
        for(int number=1;number<=1000;number++){
            Level level=RelayCatalog.get(number);boards++;
            for(CompanionBattle.Definition definition:CompanionBattle.all()){
                Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);CompanionBattle.State run=state(definition.id);int undos=number%3;
                if(definition.effect==CompanionBattle.Effect.REWIND){
                    int count=Math.min(definition.amount,level.tiles.size()-1);
                    for(int index=0;index<count;index++){int id=level.solution[index];check(budget.attempt(board,id).success,"rewind fixture actual exit");run=run.creditExit(id);}
                }
                int[] plan=suffix(board);int beforeMoves=board.moves(),beforeEnergy=run.energy();String original=snapshot(board,budget,run);
                CompanionBattleAction.Draft out=draft(board,budget,number%2==0?0:5,undos,run,0,false,plan);
                if(definition.effect==CompanionBattle.Effect.REWIND&&beforeMoves==0){check(out==null,"no history refuses rewind");continue;}
                check(out!=null,"available actual identity skill U"+number+" "+definition.id);
                effects.put(definition.effect,effects.getOrDefault(definition.effect,0L)+1);
                check(out.effect==definition.effect&&!out.combo&&out.color==definition.color,"real definition controls feedback type and color");
                check(out.state.used(0)&&CompanionBattle.State.decode(out.state.encode())!=null,"accepted skill use has valid canonical saved state");
                check(out.hintDurationMs>=0&&out.visionDurationMs>=0&&out.trailDurationMs>=0,"duration outputs nonnegative");
                switch(definition.effect){
                    case GUIDE:
                        check(out.hintId==plan[0]&&out.hintDurationMs==definition.durationMs,"guide exact safe ID and 8 seconds");
                        check(out.board.moves()==beforeMoves&&out.budget.used()==budget.used(),"guide does not play a move");
                        break;
                    case VISION:
                        check(out.visionDurationMs==definition.durationMs&&!out.board.legalMoves().isEmpty(),"vision actual available legal pieces and duration");
                        check(out.hintId==-1&&out.hintDurationMs==0&&out.trail.length==0,"vision never invents a safe hint");
                        break;
                    case TRAIL:
                        check(Arrays.equals(out.trail,Arrays.copyOf(plan,Math.min(definition.amount,plan.length)))&&out.trailDurationMs==8000,"trail is exact safe prefix");
                        check(out.board.moves()==beforeMoves,"trail remains a preview");break;
                    case PULSE:
                    case FLOW:
                        int actions=Math.min(definition.amount,plan.length);
                        check(Arrays.equals(out.performed,Arrays.copyOf(plan,actions)),"actual certified exits listed exactly");
                        check(out.board.moves()==beforeMoves+actions&&out.budget.used()==budget.used()+actions,"every auto exit consumes one action");
                        int energy=beforeEnergy-definition.cost;
                        for(int id:out.performed){check(!out.board.isPresent(id)&&out.state.creditedExit(id),"performed exit is real and receives anti-farm receipt");if(!run.creditedExit(id))energy=Math.min(CompanionBattle.MAX_ENERGY,energy+1);}
                        check(out.state.energy()==energy,"automatic exit energy exactly charged and credited");break;
                    case REWIND:
                        int amount=Math.min(definition.amount,beforeMoves);int[] expected=new int[amount];for(int n=0;n<amount;n++)expected[n]=board.history()[beforeMoves-1-n];
                        check(Arrays.equals(out.rewound,expected),"rewind reports restored IDs newest first");
                        check(out.board.moves()==beforeMoves-amount&&out.budget.used()==budget.used()-amount&&out.undos==undos+amount,"rewind refunds actual ledger action and updates historical undo count");
                        for(int id:expected)check(out.board.isPresent(id)&&out.state.creditedExit(id),"rewound piece restored while energy receipt remains spent");break;
                    default:throw new AssertionError("unknown family");
                }
                if(definition.effect!=CompanionBattle.Effect.PULSE&&definition.effect!=CompanionBattle.Effect.FLOW)check(out.state.energy()==beforeEnergy-definition.cost,"information/rewind exact shared energy debit");
                check(out.board.plannerCachedStates()==0,"prepare never executes planner search");
                MoveBudget restored=MoveBudget.restoreVersion(out.budget.policyVersion(),0,level,out.budget.used(),out.budget.extensions(),out.budget.extraActions(),out.board.moves(),out.undos);
                check(restored!=null&&ledger(restored).equals(ledger(out.budget)),"resulting board/ledger/undo tuple restores coherently");
                if(!out.board.isSolved())check(draft(out.board,out.budget,0,out.undos,out.state,0,false,suffix(out.board))==null,"used member cannot activate twice");
                for(int id:suffix(out.board))check(out.budget.attempt(out.board,id).success,"each returned candidate completes its actual remaining certificate");
                check(out.board.isSolved(),"all identity-board candidates finish the actual U puzzle");
                check(snapshot(board,budget,run).equals(original),"later mutation of draft board/ledger cannot reach original session");
            }
            if(number%250==0)System.out.println("PASS actual U1–U"+number+" × all "+CompanionBattle.all().size()+" identities");
        }
    }
    private static void badCertificatesAndInputs(){
        Level level=RelayCatalog.get(1000);Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);
        for(CompanionBattle.Definition definition:CompanionBattle.all()){
            if(definition.effect==CompanionBattle.Effect.VISION||definition.effect==CompanionBattle.Effect.REWIND)continue;
            CompanionBattle.State run=state(definition.id);int[] valid=level.solution.clone(),lateInvalid=valid.clone();lateInvalid[lateInvalid.length-1]=valid[0];
            check(draft(board,budget,0,0,run,0,false,null)==null,"safe skill refuses missing plan");
            check(draft(board,budget,0,0,run,0,false,new int[0])==null,"safe skill refuses empty plan");
            check(draft(board,budget,0,0,run,0,false,Arrays.copyOf(valid,valid.length-1))==null,"prefix-only certificate is not complete proof");
            check(draft(board,budget,0,0,run,0,false,lateInvalid)==null,"valid first action cannot hide a corrupt later suffix");
            for(int invalid:new int[]{-1,64,67,1000}){int[] bad=valid.clone();bad[bad.length-1]=invalid;check(draft(board,budget,0,0,run,0,false,bad)==null,"gate/out-of-range late action refuses entire draft");}
            Board after=new Board(level);MoveBudget changed=MoveBudget.start(0,level);check(changed.attempt(after,valid[0]).success,"stale plan fixture actual move");
            check(draft(after,changed,0,0,run,0,false,valid)==null,"initial plan refuses after current board changes");
            check(draft(board,budget,0,0,run,-1,false,valid)==null&&draft(board,budget,0,0,run,1,false,valid)==null,"invalid slot cannot consume");
        }
        CompanionBattle.State nova=state("nova");
        for(int mode:new int[]{-1,1,2,3,4,6,99})check(draft(board,budget,mode,0,nova,0,false,level.solution)==null,"outside U Journey/expedition modes refused");
        check(draft(board,budget,0,-1,nova,0,false,level.solution)==null,"negative undo count refused");
        check(CompanionBattleAction.prepare(null,budget,0,0,nova,0,false,level.solution)==null,"missing board refused");
        check(CompanionBattleAction.prepare(board,null,0,0,nova,0,false,level.solution)==null,"missing ledger refused");
        check(CompanionBattleAction.prepare(board,budget,0,0,null,0,false,level.solution)==null,"missing companion state refused");
        MoveBudget inconsistent=MoveBudget.start(0,level);check(inconsistent.consumeAction(),"inconsistent fixture ledger changes without board");
        check(draft(board,inconsistent,0,0,nova,0,false,level.solution)==null,"incoherent board and budget rejected");
        for(Level old:new Level[]{LearningCatalog.get(13),StrategyCatalog.get(13),BalancedCatalog.get(13)}){
            Board preserved=new Board(old);MoveBudget oldBudget=MoveBudget.start(0,old);
            check(draft(preserved,oldBudget,0,0,nova,0,false,old.actionSolution)==null,"preserved historical board never gains companion action policy");
        }
        Board solved=new Board(level);MoveBudget done=MoveBudget.start(0,level);for(int id:level.solution)check(done.attempt(solved,id).success,"solved fixture complete");
        check(draft(solved,done,0,0,nova,0,false,new int[0])==null,"solved board rejects no-effect use");
        CompanionBattle.State tired=state("nova","miko").use(0);
        check(draft(board,budget,0,0,tired,1,false,level.solution)==null,"insufficient shared energy refuses skill without playing board");
    }
    private static CompanionBattle.Definition member(CompanionBattle.Effect effect){for(CompanionBattle.Definition d:CompanionBattle.all())if(d.effect==effect)return d;throw new AssertionError("missing family");}
    private static void visionDeadEndsAndRewindFarm(){
        boolean sawDeadEnd=false,sawLegalDeadEnd=false;
        for(int number=31;number<=1000&&!sawLegalDeadEnd;number++){
            Level level=RelayCatalog.get(number);Board board=new Board(level);MoveBudget ledger=MoveBudget.start(0,level);int[] plan=level.solution;
            for(int at=0;at<plan.length;at++){
                for(Tile tile:board.legalMoves())if(!board.isSafeMove(tile.id)){
                    Board bad=new Board(level);check(bad.restoreHistory(board.history()),"dead-end history clones");MoveBudget b=MoveBudget.restoreVersion(5,0,level,ledger.used(),0,0,bad.moves(),0);
                    check(b.attempt(bad,tile.id).success,"actual wrong legal action fixture");boolean legal=!bad.legalMoves().isEmpty();
                    CompanionBattle.State vision=state(member(CompanionBattle.Effect.VISION).id);
                    CompanionBattleAction.Draft view=draft(bad,b,0,0,vision,0,false,null);
                    check((view!=null)==legal,"Vision follows actual legality, not complete solvability");
                    check(draft(bad,b,0,0,state("nova"),0,false,level.solution)==null,"safe guidance never accepts stale initial plan in dead end");
                    while(!bad.legalMoves().isEmpty())check(b.attempt(bad,bad.legalMoves().get(0).id).success,"continue doomed actual path to immobile position");
                    check(!bad.isSolved(),"doomed path cannot accidentally complete");
                    check(draft(bad,b,0,0,vision,0,false,null)==null,"Vision with no legal piece refuses without a target");
                    sawDeadEnd=true;if(legal)sawLegalDeadEnd=true;break;
                }
                if(sawLegalDeadEnd)break;
                check(ledger.attempt(board,plan[at]).success,"dead-end search certificate step");
            }
        }
        check(sawDeadEnd&&sawLegalDeadEnd,"real U route supplies a legal-but-doomed Vision distinction");
        Level level=RelayCatalog.get(1000);Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);String rewind=member(CompanionBattle.Effect.REWIND).id;
        CompanionBattle.State run=state("nova",rewind);int exited=level.solution[0];check(budget.attempt(board,exited).success,"farm fixture actual exit");run=run.creditExit(exited);
        CompanionBattleAction.Draft back=draft(board,budget,0,0,run,1,false,null);check(back!=null&&back.state.creditedExit(exited)&&back.board.isPresent(exited),"rewind preserves credited receipt");
        int energy=back.state.energy();check(back.budget.attempt(back.board,exited).success,"same physical exit after rewind");
        check(back.state.creditExit(exited)==back.state&&back.state.energy()==energy,"actual undo/re-exit grants no second energy");
        check(draft(new Board(level),MoveBudget.start(0,level),0,0,state(rewind),0,false,null)==null,"rewind without history has no effect and no charge");
        check(draft(board,budget,0,Integer.MAX_VALUE,run,1,false,null)==null,"undo-counter overflow refuses entire draft");
    }
    private static void combosAndShortPrefixes(){
        for(int number:new int[]{1,13,100,500,1000}){
            Level level=RelayCatalog.get(number);Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);CompanionBattle.State run=state("nova","miko","orbit");
            check(draft(board,budget,0,0,run,-1,true,level.solution)==null,"premature combo refuses");
            int first=level.solution[0];check(budget.attempt(board,first).success,"combo fixture actual exit");run=run.creditExit(first);
            CompanionBattleAction.Draft guide=draft(board,budget,0,0,run,0,false,suffix(board));check(guide!=null,"first real skill guide");
            CompanionBattleAction.Draft trail=draft(guide.board,guide.budget,0,0,guide.state,1,false,suffix(guide.board));check(trail!=null&&trail.state.comboReady(),"second different real skill charges Resonance");
            int[] invalid=suffix(trail.board);invalid[invalid.length-1]=-1;
            check(draft(trail.board,trail.budget,0,0,trail.state,-1,true,invalid)==null,"invalid combo target cannot consume readiness");
            CompanionBattleAction.Draft burst=draft(trail.board,trail.budget,0,0,trail.state,-1,true,suffix(trail.board));
            check(burst!=null&&burst.combo&&burst.performed.length==1&&burst.state.comboSpent(),"explicit combo performs exactly one real exit");
            check(burst.budget.used()==trail.budget.used()+1,"combo consumes usual board action");
            check(burst.state.energy()==Math.min(6,trail.state.energy()+1),"combo no skill debit, usual once-per-piece exit credit");
            check(draft(burst.board,burst.budget,0,0,burst.state,-1,true,suffix(burst.board))==null,"spent combo cannot run twice");
        }
        Level level=RelayCatalog.get(1000);Board last=new Board(level);MoveBudget budget=MoveBudget.start(0,level);
        for(int i=0;i<level.solution.length-1;i++)check(budget.attempt(last,level.solution[i]).success,"one-piece prefix fixture");
        CompanionBattleAction.Draft flow=draft(last,budget,0,0,state("orbit"),0,false,suffix(last));
        check(flow!=null&&flow.performed.length==1&&flow.board.isSolved(),"Flow with one piece left executes one, reports one and solves");
        CompanionBattleAction.Draft trail=draft(last,budget,0,0,state("miko"),0,false,suffix(last));
        check(trail!=null&&trail.trail.length==1&&!trail.board.isSolved(),"Trail with one piece left previews one without automatic play");
    }
    private static void writeReport(Path report)throws Exception {
        Files.createDirectories(report);StringBuilder coverage=new StringBuilder();for(CompanionBattle.Effect effect:CompanionBattle.Effect.values()){if(coverage.length()>0)coverage.append(",");coverage.append("\n    \"").append(effect).append("\": ").append(effects.getOrDefault(effect,0L));}
        String json="{\n  \"passed\": true,\n  \"checks\": "+checks+",\n  \"boards\": "+boards+",\n  \"identities\": "+CompanionBattle.all().size()+",\n  \"prepared_drafts\": "+prepared+",\n  \"refused_drafts\": "+refusals+",\n  \"identity_board_coverage\": {"+coverage+"\n  },\n  \"scope\": \"Actual host U boards and MoveBudget, all 1000 boards times every identity, pure draft nonmutation and canonical restore. Not Android commits, gestures, rendering, disk failures or player acceptance.\"\n}\n";
        Files.writeString(report.resolve("host-report.json"),json,StandardCharsets.UTF_8);
        Path root=Path.of(System.getProperty("luma.test.root",".")).toAbsolutePath().normalize();StringBuilder sources=new StringBuilder("{\n");
        List<String> files=new ArrayList<>();for(String folder:new String[]{"app/src/main/java/com/androidhit/lumadrift/engine"})try(java.util.stream.Stream<Path> stream=Files.walk(root.resolve(folder))){stream.filter(p->p.toString().endsWith(".java")).forEach(p->files.add(root.relativize(p).toString()));}
        Collections.addAll(files,"app/src/main/java/com/androidhit/lumadrift/data/Catalog.java","app/src/main/java/com/androidhit/lumadrift/data/CompanionBattle.java","app/src/main/java/com/androidhit/lumadrift/data/CompanionBattleAction.java","scripts/CompanionBattleActionV119Test.java","scripts/test-companion-action-v119.sh");Collections.sort(files);
        for(int n=0;n<files.size();n++){String file=files.get(n);byte[] digest=MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(root.resolve(file)));StringBuilder hash=new StringBuilder();for(byte b:digest)hash.append(String.format(Locale.ROOT,"%02x",b&255));sources.append("  \"").append(file).append("\": \"").append(hash).append("\"").append(n+1==files.size()?"\n":",\n");}
        sources.append("}\n");Files.writeString(report.resolve("source-sha256.json"),sources,StandardCharsets.UTF_8);System.out.print(json);
    }
    public static void main(String[] args)throws Exception {everyBoardAndIdentity();badCertificatesAndInputs();visionDeadEndsAndRewindFarm();combosAndShortPrefixes();writeReport(Path.of(args.length>0?args[0]:"docs/validation/v1.19/actions"));}
}
