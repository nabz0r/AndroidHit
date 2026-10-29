import com.androidhit.lumadrift.engine.*;
import com.androidhit.lumadrift.progression.LearningGuide;
import com.androidhit.lumadrift.progression.LearningStrings;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Independent ray model, full reachable-state traversal, legacy codes and actual U save/undo contracts. */
public final class RelayV115Test {
    static long checks,states,transitions;static int choices,keys,relays,maxStates,maxCache;static long slowestNanos;
    static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    static final class Model {
        final Level level;final int[] obstruction;final byte[] viable;final int[] gates;
        Model(Level l){level=l;int count=l.tiles.size(),end=1<<count;obstruction=new int[count];viable=new byte[end];gates=new int[end];int removed=0;for(Tile t:l.tiles)removed^=t.switchMask;gates[0]=l.initialGateMask^removed;viable[0]=2;
            // Walk individual grid cells, rather than using Board's nearest-piece ray implementation.
            for(Tile t:l.tiles){int r=t.row+t.direction.dr,c=t.col+t.direction.dc;while(r>=0&&c>=0&&r<l.size&&c<l.size){for(Tile o:l.tiles)if(o.row==r&&o.col==c)obstruction[t.id]|=1<<o.id;for(Barrier b:l.barriers)if(b.row==r&&b.col==c)obstruction[t.id]|=1<<b.keyTileId;r+=t.direction.dr;c+=t.direction.dc;}}
            for(int mask=1;mask<end;mask++){int low=Integer.lowestOneBit(mask),id=Integer.numberOfTrailingZeros(low);gates[mask]=gates[mask^low]^l.tile(id).switchMask;}
        }
        boolean legal(int mask,int id){Tile t=level.tile(id);return (mask&(1<<id))!=0&&(gates[mask]&t.direction.bit)!=0&&(mask&obstruction[id])==0;}
        boolean possible(int mask){if(viable[mask]!=0)return viable[mask]==2;for(int id=0;id<level.tiles.size();id++)if(legal(mask,id)&&possible(mask&~(1<<id))){viable[mask]=2;return true;}viable[mask]=1;return false;}
    }
    static void walk(Board board,Model model,boolean[] visited){int mask=(int)board.remainingMask();if(visited[mask])return;visited[mask]=true;states++;
        check(board.gateMask()==model.gates[mask],"gate state U"+board.level.number);
        long at=System.nanoTime();boolean possible=board.hasSolution();slowestNanos=Math.max(slowestNanos,System.nanoTime()-at);
        check(possible==model.possible(mask),"exact viability U"+board.level.number+" mask"+mask);
        check(board.isSolved()==(mask==0),"completion");
        for(Tile tile:board.level.tiles){int id=tile.id;boolean legal=model.legal(mask,id);check(board.canMove(id)==legal,"physical legality");Board.ActionPreview preview=board.previewAction(id);check(preview.legal==legal,"preview legality");
            check(board.isSafeMove(id)==(legal&&model.possible(mask&~(1<<id))),"exact safe move");
            if(!legal)continue;transitions++;int[] history=board.history();int gates=board.gateMask();
            check(preview.gateMask==(gates^tile.switchMask),"preview causal gate effect");
            Board.MoveResult result=board.performAction(id);check(result.success&&result.tile==tile,"successful exit");check(board.remainingMask()==(mask&~(1<<id)),"one piece exits");
            walk(board,model,visited);check(board.undo(),"undo succeeds");check(board.remainingMask()==mask&&board.gateMask()==gates&&Arrays.equals(board.history(),history),"undo restores exact state");
        }
        for(Direction d:Direction.values()){int[] before=board.history();check(!board.previewAction(Board.gateAction(d)).legal,"gate preview unavailable");check(!board.performAction(Board.gateAction(d)).success,"gate action unavailable");check(Arrays.equals(before,board.history()),"gate refusal never mutates history");}
    }
    static String fingerprint(Level level){StringBuilder s=new StringBuilder().append(level.size).append(':').append(level.initialGateMask);for(Tile t:level.tiles)s.append('|').append(t.row).append(',').append(t.col).append(',').append(t.direction.ordinal()).append(',').append(t.switchMask);for(Barrier b:level.barriers)s.append(';').append(b.row).append(',').append(b.col).append(',').append(b.keyTileId);return s.toString();}
    static int[] point(int r,int c,int size,int rotation,boolean reflect){if(reflect)c=size-1-c;for(int i=0;i<rotation;i++){int old=r;r=c;c=size-1-old;}return new int[]{r,c};}
    static int direction(int d,int rotation,boolean reflect){int r=Direction.values()[d].dr,c=Direction.values()[d].dc;if(reflect)c=-c;for(int i=0;i<rotation;i++){int old=r;r=c;c=-old;}for(Direction v:Direction.values())if(v.dr==r&&v.dc==c)return v.ordinal();throw new AssertionError();}
    static int gateMask(int mask,int rotation,boolean reflect){int result=0;for(Direction d:Direction.values())if((mask&d.bit)!=0)result|=1<<direction(d.ordinal(),rotation,reflect);return result;}
    static String canonical(Level l){String best=null;for(int reflect=0;reflect<2;reflect++)for(int rotation=0;rotation<4;rotation++){int minR=100,minC=100;int[][] positions=new int[l.tiles.size()][];for(Tile t:l.tiles){positions[t.id]=point(t.row,t.col,l.size,rotation,reflect==1);minR=Math.min(minR,positions[t.id][0]);minC=Math.min(minC,positions[t.id][1]);}for(Barrier b:l.barriers){int[] p=point(b.row,b.col,l.size,rotation,reflect==1);minR=Math.min(minR,p[0]);minC=Math.min(minC,p[1]);}List<String> objects=new ArrayList<>();for(Tile t:l.tiles){int[] p=positions[t.id];objects.add("T"+(p[0]-minR)+","+(p[1]-minC)+","+direction(t.direction.ordinal(),rotation,reflect==1)+","+gateMask(t.switchMask,rotation,reflect==1));}for(Barrier b:l.barriers){int[] p=point(b.row,b.col,l.size,rotation,reflect==1),key=positions[b.keyTileId];objects.add("K"+(p[0]-minR)+","+(p[1]-minC)+","+(key[0]-minR)+","+(key[1]-minC));}Collections.sort(objects);String text=l.size+":"+gateMask(l.initialGateMask,rotation,reflect==1)+objects;if(best==null||text.compareTo(best)<0)best=text;}return best;}
    static void verifyChoices(Level level){Model model=new Model(level);int mask=(int)level.allTilesMask;int decisions=0,forced=0;boolean sampled=false;Board board=new Board(level);for(int expected:level.solution){int good=0,bad=0;for(int id=0;id<level.tiles.size();id++)if(model.legal(mask,id)){boolean safe=model.possible(mask&~(1<<id));if(safe)good++;else {bad++;if(!sampled){check(board.performAction(id).success,"sample wrong exit");Board.Guidance feedback=board.guidance(level.tiles.size());check(feedback.status==Board.PositionStatus.DEAD_END&&feedback.undoSteps>=1,"exact recovery guidance");check(board.undo(),"wrong exit freely reversible");sampled=true;}}}if(good>0&&bad>0)decisions++;if(good==1)forced++;check(board.performAction(expected).success,"choice metric certificate");mask&=~(1<<expected);}ProgressionInfo info=RelayCatalog.info(level.number);check(info.decisionPoints==decisions&&info.forcedOptimalActions==forced,"independent authored choice metrics");}
    static void savedBudget(Level level){Board board=new Board(level);MoveBudget budget=MoveBudget.restoreVersion(MoveBudget.RELAY_POLICY_VERSION,0,level,0,0,0,0,0);check(budget!=null&&budget.policyVersion()==MoveBudget.RELAY_POLICY_VERSION&&budget.rejectsAreFree(),"preserved policy5 snapshot");
        for(int step=0;step<Math.min(4,level.solution.length);step++)check(budget.attempt(board,level.solution[step]).success,"budget exit");
        int[] history=board.history();MoveBudget.Snapshot saved=budget.snapshot();Board restored=new Board(level);check(restored.restoreHistory(history),"U history restores");MoveBudget ledger=MoveBudget.restoreVersion(saved.policyVersion,0,level,saved.used,saved.extensions,saved.extraActions,restored.moves(),0);check(ledger!=null,"U budget restores");
        int undos=0;while(restored.canUndo()){check(ledger.undo(restored,undos++),"every exit can undo freely");}check(ledger.used()==0&&restored.remainingMask()==level.allTilesMask,"complete rewind refunds all");check(!ledger.undo(restored,undos),"no phantom undo");
        check(MoveBudget.restoreVersion(4,0,level,0,0,0,0,0)==null,"reject old policy for U");check(!restored.restoreHistory(new int[]{Board.gateAction(Direction.UP)}),"no manual action in U history");
        boolean refused=false;try{MoveBudget.startDeliberate(0,level);}catch(IllegalArgumentException expected){refused=true;}check(refused,"U cannot start with N policy");
        int before=budget.used();budget.attempt(board,Board.gateAction(Direction.UP));check(budget.used()==before,"blocked gate free");
    }
    static void guide(Level level){Board board=new Board(level);LearningGuide.Context c=LearningGuide.context(board,RelayCatalog.lesson(level.number));check(c.focusGate==null,"guide never directs gate tap");
        for(String language:new String[]{"en","fr","de","es","pt","it"}){check(!LearningStrings.get(language,c.hudKey).isEmpty(),"localized HUD");check(!LearningStrings.get(language,c.titleKey).isEmpty(),"localized title");check(!LearningStrings.get(language,c.descriptionKey).isEmpty(),"localized note");}
        for(Tile t:level.tiles)if(!board.isGateOpen(t.direction)){LearningGuide.Feedback f=LearningGuide.blockedFeedback(board,t.id);check(f.key.equals("blocked_gate_relay"),"relay guidance");for(String language:new String[]{"en","fr","de","es","pt","it"})check(!LearningStrings.get(language,f.shortKey,f.args).isEmpty(),"localized closed gate");break;}
    }
    static void legacy(){for(int n:new int[]{1,15,30,100,1000}){Level old=LearningCatalog.get(n);ChallengeCode parsed=ChallengeCode.parse(ChallengeCode.encodeLearning(n));check(parsed!=null&&parsed.resolve()==old,"N identity preserved");check(new Board(old).supportsManualGates(),"N manual rules preserved");check(MoveBudget.start(0,old).policyVersion()==4,"N budget preserved");check(MoveBudget.restoreVersion(5,0,old,0,0,0,0,0)==null,"U policy cannot reinterpret N");
            Level strategy=StrategyCatalog.get(n);check(ChallengeCode.parse(ChallengeCode.encodeStrategy(n)).resolve()==strategy,"L identity preserved");check(new Board(strategy).supportsManualGates(),"L manual rules preserved");
            Level balanced=BalancedCatalog.get(n);check(ChallengeCode.parse(ChallengeCode.encodeBalancedV3(n)).resolve()==balanced,"J identity preserved");check(!new Board(balanced).supportsManualGates(),"J switch-only rule preserved");}
        for(int n=1;n<=240;n++){check(ChallengeCode.parse(ChallengeCode.encodeStrategyRemix(n)).resolve()==StrategyCatalog.remix(n),"M identity preserved");check(ChallengeCode.parse(ChallengeCode.encodeBalancedRemix(n)).resolve()==BalancedCatalog.legacyRemix(n),"Q identity preserved");}
        for(long seed:new long[]{0,1,Long.MAX_VALUE})check(ChallengeCode.parse(ChallengeCode.encodeStrategyEvent(seed)).resolve().seed==seed,"T event identity preserved");
    }
    public static void main(String[] args)throws Exception{long started=System.nanoTime();Path report=Path.of(args.length==0?"docs/validation/v1.15/relay-engine":args[0]);Files.createDirectories(report);StringBuilder csv=new StringBuilder("number,pieces,states,transitions,decisions,keys,cache_bytes\n");Set<String> unique=new HashSet<>();
        check(RelayCatalog.LEVEL_COUNT==1000,"1000 U boards");for(int n=1;n<=1000;n++){Level l=RelayCatalog.get(n);check(l.number==n&&l.progressionVersion==6,"versioned U identity");check(l.minimumActions==l.tiles.size()&&Arrays.equals(l.solution,l.actionSolution),"exits-only certificate");check(unique.add(canonical(l)),"unique under rotation/reflection/translation/IDs U"+n);check(!new Board(l).supportsManualGates(),"no manual gates");
            Board certificate=new Board(l);for(int id:l.actionSolution){check(!Board.isGateAction(id),"certificate no gate");check(certificate.performAction(id).success,"certified legal exit U"+n);}check(certificate.isSolved(),"certificate completes U"+n);
            ChallengeCode code=ChallengeCode.parse(ChallengeCode.encodeRelay(n));check(code!=null&&code.kind==ChallengeCode.Kind.RELAY&&code.resolve()==l,"U code roundtrip");
            long beforeStates=states,beforeTransitions=transitions;Board board=new Board(l);walk(board,new Model(l),new boolean[1<<l.tiles.size()]);maxStates=Math.max(maxStates,(int)(states-beforeStates));maxCache=Math.max(maxCache,board.plannerCacheBytes());
            ProgressionInfo info=RelayCatalog.info(n);if(n>30){check(info.decisionPoints>=1,"real order consequence U"+n);choices++;}if(info.keyCount>0)keys++;for(Tile t:l.tiles)if(t.isSwitch())relays++;
            check(info.requiredGateOpens==0&&info.sourceFamily.equals("U"),"metadata no opening cost");verifyChoices(l);savedBudget(l);guide(l);csv.append(n).append(',').append(l.tiles.size()).append(',').append(states-beforeStates).append(',').append(transitions-beforeTransitions).append(',').append(info.decisionPoints).append(',').append(info.keyCount).append(',').append(board.plannerCacheBytes()).append('\n');
            if(n%100==0)System.out.println("PASS U1–U"+n+" reachable_states="+states+" transitions="+transitions);
        }legacy();check(ChallengeCode.parse(ChallengeCode.encodeRelay(1000).replace("-U-","-Q-"))==null,"CRC binds family");
        double seconds=(System.nanoTime()-started)/1e9;String json=String.format(Locale.ROOT,"{\n  \"passed\": true,\n  \"boards\": 1000,\n  \"checks\": %d,\n  \"reachable_states\": %d,\n  \"legal_transitions\": %d,\n  \"post_teaching_boards_with_order_consequence\": %d,\n  \"boards_with_keys\": %d,\n  \"relay_pieces\": %d,\n  \"max_reachable_states_per_board\": %d,\n  \"max_planner_cache_bytes\": %d,\n  \"slowest_host_viability_ms\": %.3f,\n  \"seconds\": %.3f,\n  \"scope\": \"Host exact model, all reachable U states, transitions, previews, Undo, save/budget, six-language guide and historical identifiers. Not Android execution or human enjoyment.\"\n}\n",checks,states,transitions,choices,keys,relays,maxStates,maxCache,slowestNanos/1e6,seconds);
        Files.writeString(report.resolve("relay-host.json"),json,StandardCharsets.UTF_8);Files.writeString(report.resolve("relay-states.csv"),csv,StandardCharsets.UTF_8);System.out.println(json);
    }
}
