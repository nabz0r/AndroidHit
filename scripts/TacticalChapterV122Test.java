import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import java.util.*;

/** Actual engine/certificates and immutable ledger, not a human engagement experiment. */
public final class TacticalChapterV122Test {
    private static int checks,layouts,routes,talentDrafts;
    private static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    private static TacticalChapterRun roundtrip(TacticalChapterRun run){check(run!=null,"valid run");TacticalChapterRun restored=TacticalChapterRun.decode(run.encode());check(restored!=null&&restored.encode().equals(run.encode()),"canonical cold replay");return restored;}
    private static String ledger(MoveBudget budget){MoveBudget.Snapshot s=budget.snapshot();return s.policyVersion+":"+s.mode+":"+s.base+":"+s.used+":"+s.extensions+":"+s.extraActions;}
    private static String state(Board b,MoveBudget m,TacticalChapterRun run){return run.encode()+":"+b.remainingMask()+":"+b.gateMask()+":"+Arrays.toString(b.history())+":"+ledger(m);}
    private static TacticalChapterAction.Draft draft(Board b,MoveBudget m,int undos,TacticalChapterRun run,int slot,boolean combo,int[] plan){
        String before=state(b,m,run);int[] old=plan==null?null:plan.clone();TacticalChapterAction.Draft out=TacticalChapterAction.prepare(b,m,undos,run,slot,combo,plan,run.encode());
        check(before.equals(state(b,m,run)),"draft preserves all source state");check(Arrays.equals(old,plan),"caller plan unchanged");if(out!=null){talentDrafts++;check(out.board!=b&&out.budget!=m&&out.run!=run,"draft owns mutable outputs and immutable next ledger");check(out.board.plannerCachedStates()==0,"effect does not invoke search");check(TacticalChapterRun.decode(out.run.encode())!=null,"effect ledger cold restores");}return out;
    }
    private static TacticalChapterRun selected(TacticalChapterCatalog.Layout layout,int capacity){TacticalChapterRun run=TacticalChapterRun.start("layout_"+layout.index,42,layout.episode,capacity);for(int stage=0;stage<layout.stage;stage++){run=run.chooseRoute(run.options().get(0).id);Board solved=new Board(run.currentLevel());for(int id:solved.level.solution)check(solved.move(id).success,"prerequisite certificate");run=run.recordWin(run.currentVictoryId(),solved,2);}return run.chooseRoute(layout.id);}
    private static Board solved(Level level){Board b=new Board(level);for(int id:level.solution)check(b.move(id).success,"certificate action legal");check(b.isSolved(),"manual solve needs no talent");return b;}
    private static int[] suffix(Board board){return Arrays.copyOfRange(board.level.solution,board.moves(),board.level.solution.length);}
    private static String geometry(Level level){StringBuilder out=new StringBuilder().append(level.size).append('/').append(level.initialGateMask);for(Tile t:level.tiles)out.append(';').append(t.row).append(',').append(t.col).append(',').append(t.direction).append(',').append(t.switchMask);for(Barrier w:level.barriers)out.append("/B").append(w.row).append(',').append(w.col).append(',').append(w.keyTileId);return out.toString();}
    private static void content(){
        check(TacticalChapterCatalog.all().size()==12,"twelve new layouts");Set<String> ids=new HashSet<>(),fingerprints=new HashSet<>();boolean multiLock=false;int keyEffects=0,relayEffects=0,trapLayouts=0;
        for(TacticalChapterCatalog.Layout layout:TacticalChapterCatalog.all()){
            layouts++;Level level=layout.level();check(ids.add(layout.id)&&fingerprints.add(geometry(level)),"unique stable layout and geometry");check(layout.matches(level)&&!layout.matches(RelayCatalog.get(level.number)),"family identity excludes overlapping campaign U number");
            check(level.progressionVersion==6&&!new Board(level).supportsManualGates(),"unchanged U relay gate rules");
            Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);check(budget.policyVersion()==6,"current non-refunding Undo policy");
            for(int id:level.solution){Tile tile=level.tile(id);int before=board.gateMask();List<Barrier> active=new ArrayList<>();for(Barrier wall:level.barriers)if(wall.keyTileId==id&&board.isBarrierActive(wall))active.add(wall);if(active.size()>1)multiLock=true;
                Board.MoveResult move=budget.attempt(board,id);check(move.success&&move.tile!=null,"certificate through actual budget");check(move.gateMask==(before^tile.switchMask),"XOR switch is real");if(tile.switchMask!=0)relayEffects++;
                for(Barrier wall:active){check(!board.isBarrierActive(wall),"key exit opens its exact linked lock");keyEffects++;}
                if(!active.isEmpty()){check(budget.undo(board,0),"key exit can be undone");for(Barrier wall:active)check(board.isBarrierActive(wall),"Undo visibly authoritative lock reclose");check(budget.attempt(board,id).success,"re-exit consumes normal action");}
            }
            check(board.isSolved(),"all layouts manually finish despite one correction per key");
            // Evaluate traps with solver only in this content test; never feed this verdict to a normal agent.
            Board initial=new Board(level);boolean trap=false;for(Tile tile:initial.legalMoves()){Board candidate=new Board(level);candidate.move(tile.id);if(!candidate.hasSolution()){trap=true;break;}}if(trap)trapLayouts++;
            Level external=layout.level();external.solution[0]=-1;check(layout.level().solution[0]>=0,"public legacy certificate cannot corrupt catalog");
        }
        check(multiLock&&keyEffects>=10&&relayEffects>=20,"keys and relays have actual linked effects");check(trapLayouts>=4,"genuine legal early closures require anticipation or repair");
        for(int number=1;number<=1000;number++)check(!fingerprints.contains(geometry(RelayCatalog.get(number))),"new geometry is distinct from every shipped U board");
        for(TacticalChapterCatalog.Episode episode:TacticalChapterCatalog.Episode.values())for(int stage=0;stage<4;stage++)check(TacticalChapterCatalog.options(episode,stage).size()==(stage==0||stage==3?1:2),"entry two forks finale graph");
    }
    private static void routesAndWins(){
        for(TacticalChapterCatalog.Episode episode:TacticalChapterCatalog.Episode.values())for(int branch1=0;branch1<2;branch1++)for(int branch2=0;branch2<2;branch2++)for(int cap:new int[]{4,6,8}){
            routes++;TacticalChapterRun run=roundtrip(TacticalChapterRun.start("route_"+routes,Long.MIN_VALUE+routes,episode,cap));check(run.currentLevel()==null&&run.copiesReward()==0&&run.claim()==null,"no board or reward before route");
            for(int stage=0;stage<4;stage++){
                check(run.chooseRoute("missing")==null,"unknown route cannot progress");int choice=stage==1?branch1:stage==2?branch2:0;String id=run.options().get(choice).id;run=roundtrip(run.chooseRoute(id));check(run.chooseRoute(id)==run,"same route selection idempotent");if(run.options().size()>1)check(run.chooseRoute(run.options().get(1-choice).id)==null,"cannot replace a live stage");
                Board b=new Board(run.currentLevel());check(run.recordWin(run.currentVictoryId(),b,3)==null,"unsolved board cannot award stage");Board proof=solved(run.currentLevel());check(run.recordWin("wrong_run",proof,3)==null,"exact stage identity required");check(run.recordWin(run.currentVictoryId(),proof,0)==null,"invalid stars refused");
                TacticalChapterRun before=run;String victory=run.currentVictoryId();run=roundtrip(run.recordWin(victory,proof,3));check(run.completedStages==stage+1&&run.chargeRemaining()==cap,"real win advances one stage without replenishing charge");check(run.recordWin(victory,proof,3)==run,"win replay cannot award again");check(run.recordWin(victory,proof,2)==null,"altered replay rejected");check(before.recordWin(victory,"wrong_scope",proof,3,before.encode())==null,"wrong board scope rejected");
            }
            check(run.finished()&&run.options().isEmpty()&&run.currentLevel()==null,"four wins finish with no fifth board");check(run.coinsReward()==120&&run.copiesReward()==0&&run.homeContributionReward()==0&&run.souvenirId().equals(TacticalChapterCatalog.souvenirId(episode)),"fixed completion reward independent of stars/talents");check(run.chooseRoute(TacticalChapterCatalog.all().get(0).id)==null,"finished sortie stays finished");TacticalChapterRun claimed=roundtrip(run.claim());check(claimed.claimed&&claimed.claim()==claimed,"claim marker canonical idempotence");check(claimed.completionRequestId().equals("tactical:"+claimed.id),"stable sortie completion identity");
        }
    }
    private static void effectsAndCharge(){
        check(TacticalChapterCatalog.talent(0).effect==CompanionBattle.Effect.GUIDE&&TacticalChapterCatalog.talent(0).cost==2,"Nova information role2");check(TacticalChapterCatalog.talent(1).id.equals("hoot")&&TacticalChapterCatalog.talent(1).effect==CompanionBattle.Effect.REWIND&&TacticalChapterCatalog.talent(1).amount==1&&TacticalChapterCatalog.talent(1).cost==2,"Hoot one-step repair role2");check(TacticalChapterCatalog.talent(2).effect==CompanionBattle.Effect.FLOW&&TacticalChapterCatalog.talent(2).amount==2&&TacticalChapterCatalog.talent(2).cost==3,"Orbit execution role3");
        for(TacticalChapterCatalog.Layout layout:TacticalChapterCatalog.all())for(int capacity:new int[]{4,6,8})for(int slot=0;slot<3;slot++){
            TacticalChapterRun run=roundtrip(selected(layout,capacity));Board b=new Board(run.currentLevel());MoveBudget budget=MoveBudget.start(0,b.level);int undos=0;
            if(slot==1)check(budget.attempt(b,b.level.solution[0]).success,"real exit before repair");
            TacticalChapterAction.Draft out=draft(b,budget,undos,run,slot,false,suffix(b));check(out!=null,"all three roles have feasible effects on every layout");check(out.run.chargeRemaining()==capacity-TacticalChapterCatalog.talent(slot).cost&&out.run.used(slot),"charge exactly debited once");
            if(slot==0)check(out.hintId==b.level.solution[0]&&out.hintDurationMs==8000&&out.board.moves()==b.moves()&&out.budget.used()==budget.used(),"Guide exposes one safe piece without playing");
            if(slot==1)check(out.rewound.length==1&&out.board.moves()==0&&out.undos==1&&out.budget.used()==budget.used(),"Rewind restores one exit and preserves non-refunding action cost");
            if(slot==2)check(out.performed.length==2&&out.board.moves()==2&&out.budget.used()==2,"Flow two actual paid actions");
            check(draft(out.board,out.budget,out.undos,out.run,slot,false,suffix(out.board))==null,"talent once per entire sortie");
            check(MoveBudget.restoreVersion(6,0,out.board.level,out.budget.used(),out.budget.extensions(),out.budget.extraActions(),out.board.moves(),out.undos)!=null,"draft ledger cold restore coherent");
            for(int id:suffix(out.board))check(out.budget.attempt(out.board,id).success,"effect leaves feasible manual completion");check(out.board.isSolved(),"all effect candidates complete");
            TacticalChapterRun advanced=roundtrip(out.run.recordWin(out.run.currentVictoryId(),out.board,2));check(advanced.chargeRemaining()==out.run.chargeRemaining()&&advanced.used(slot),"stage completion neither recharges nor clears talent use");if(!advanced.finished()){advanced=advanced.chooseRoute(advanced.options().get(0).id);check(!advanced.canUse(slot),"next board cannot refresh used member");}
        }
        // 6 cannot use all three costs (2+2+3); 8 can. 4 allows exactly Guide+Rewind.
        for(int capacity:new int[]{4,6,8}){
            TacticalChapterRun run=selected(TacticalChapterCatalog.all().get(0),capacity);Board b=new Board(run.currentLevel());MoveBudget budget=MoveBudget.start(0,b.level);
            check(draft(b,budget,0,run,1,false,null)==null,"no-effect rewind does not spend");check(draft(b,budget,0,run,-1,true,suffix(b))==null,"premature combo refused");
            TacticalChapterAction.Draft guide=draft(b,budget,0,run,0,false,suffix(b));check(guide!=null,"guide first");check(guide.budget.attempt(guide.board,guide.board.level.solution[0]).success,"manual exit grants no charge");
            TacticalChapterAction.Draft rewind=draft(guide.board,guide.budget,0,guide.run,1,false,null);check(rewind!=null&&rewind.run.comboReady(),"two distinct effective roles unlock run-wide combo");check(rewind.run.chargeRemaining()==capacity-4,"manual exit no charge farm");
            TacticalChapterAction.Draft combo=draft(rewind.board,rewind.budget,rewind.undos,rewind.run,-1,true,suffix(rewind.board));check(combo!=null&&combo.performed.length==1&&combo.run.comboSpent()&&combo.run.chargeRemaining()==capacity-4,"one free-charge combo still pays a board action");check(draft(combo.board,combo.budget,combo.undos,combo.run,-1,true,suffix(combo.board))==null,"combo once per sortie");
            TacticalChapterAction.Draft flow=draft(combo.board,combo.budget,combo.undos,combo.run,2,false,suffix(combo.board));check((flow!=null)==(capacity==8),"capacities have distinct feasible portfolios");
        }
    }
    private static void corruptAndStale(){
        TacticalChapterRun run=selected(TacticalChapterCatalog.all().get(0),6);Board b=new Board(run.currentLevel());MoveBudget budget=MoveBudget.start(0,b.level);String original=run.encode();
        for(int slot:new int[]{-1,3,100})check(draft(b,budget,0,run,slot,false,b.level.solution)==null,"invalid slot refused");
        check(TacticalChapterAction.prepare(b,budget,0,run,0,false,b.level.solution,original+";")==null,"stale ledger request refused");check(run.useTalent(0,"other_board")==null,"talent scope exact");
        check(TacticalChapterAction.prepare(b,budget,-1,run,0,false,b.level.solution,original)==null,"negative undo count refused");
        int[] corrupt=b.level.solution.clone();corrupt[corrupt.length-1]=corrupt[0];check(draft(b,budget,0,run,0,false,corrupt)==null,"bad late certificate refuses whole information command");check(draft(b,budget,0,run,2,false,null)==null,"automatic action cannot invent a plan");
        Board campaign=new Board(RelayCatalog.get(b.level.number));check(TacticalChapterAction.prepare(campaign,MoveBudget.start(0,campaign.level),0,run,0,false,campaign.level.solution,original)==null,"same numbered campaign board cannot spend chapter resource");
        MoveBudget incoherent=MoveBudget.start(0,b.level);incoherent.consumeAction();check(draft(b,incoherent,0,run,0,false,b.level.solution)==null,"incoherent saved budget refused");
        List<String> invalid=Arrays.asList(null,"",original+";",original.replace("t122_1|","t122_2|"),original.replace("|6|","|7|"),original.replace("|6|","|06|"),original+";T,0,wrong",original+";R,"+run.currentBoardScope(),original+";Q",original+";C,"+run.currentLayout().id,original+";W,"+run.currentVictoryId()+","+run.currentLayout().id+",3,0.1",original.replace("|42|","|+42|"));
        for(String value:invalid)check(TacticalChapterRun.decode(value)==null,"corrupt/noncanonical/future save refused atomically");
        TacticalChapterRun used=run.useTalent(0,run.currentBoardScope());check(TacticalChapterRun.decode(used.encode()+";T,0,"+run.currentBoardScope())==null,"duplicate use rejected by codec");check(TacticalChapterRun.start("invalid|id",1,run.episode)==null&&TacticalChapterRun.start("valid",1,null)==null,"bad identities refused");
        check(TacticalChapterRun.decode(original)!=null,"failed saves never disturb valid original");
    }
    private static void realRepairsAndAlternativeWins(){
        int traps=0,alternatives=0;
        for(TacticalChapterCatalog.Layout layout:TacticalChapterCatalog.all()){
            TacticalChapterRun run=selected(layout,6);Board initial=new Board(run.currentLevel());
            for(Tile tile:initial.legalMoves()){
                Board changed=new Board(initial.level);MoveBudget ledger=MoveBudget.start(0,initial.level);check(ledger.attempt(changed,tile.id).success,"legal opening for content evaluation");int[] continuation=changed.solve();
                if(continuation==null){
                    TacticalChapterAction.Draft repair=draft(changed,ledger,0,run,1,false,null);check(repair!=null&&repair.board.remainingMask()==initial.remainingMask()&&repair.board.gateMask()==initial.gateMask()&&repair.board.moves()==0,"real repair restores exact gates and pieces after legal doomed closure");check(repair.budget.used()==1&&repair.undos==1&&repair.run.chargeRemaining()==4,"repair pays role Charge and retains original productive action cost");traps++;
                }else if(tile.id!=initial.level.solution[0]){
                    for(int id:continuation)check(ledger.attempt(changed,id).success,"alternate legal completion through actual engine");check(run.recordWin(run.currentVictoryId(),changed,3)!=null,"valid manual completion need not reproduce catalog's preferred order");alternatives++;
                }
            }
            Level good=initial.level;Level otherSeed=new Level(good.number,0,good.size,good.initialGateMask,good.tiles,good.solution,false,good.seed+1,good.barriers,6);check(run.recordWin(run.currentVictoryId(),solved(otherSeed),3)==null,"same geometry and number with wrong stable seed cannot award exact identity");
        }
        check(traps>=4&&alternatives>=4,"content includes recoverable strategic mistakes and real freedom of manual order");
    }
    public static void main(String[] args){content();routesAndWins();effectsAndCharge();corruptAndStale();realRepairsAndAlternativeWins();System.out.println("{\"passed\":true,\"checks\":"+checks+",\"layouts\":"+layouts+",\"completeRouteCapacityCombinations\":"+routes+",\"actualTalentDrafts\":"+talentDrafts+",\"scope\":\"Twelve hand-authored native U layouts, distinct from U1-U1000, exact mechanical effects and legal traps/repairs, all branches at Charge4/6/8, immutable drafts, per-sortie resource and canonical cold replay. Not human enjoyment or Android disk/gesture/render validation.\"}");}
}
