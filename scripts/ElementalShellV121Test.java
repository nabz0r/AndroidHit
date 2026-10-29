import com.androidhit.lumadrift.data.CompanionBattle;
import com.androidhit.lumadrift.data.CompanionBattleAction;
import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;

/** Real U certificates with free shell preparation; no native-input acceptance. */
public final class ElementalShellV121Test {
    private static long checks, taps, automaticExits;
    private static int boards, solved, keyBoards;
    private static final EnumMap<ElementalShell121.Kind,Integer> elements = new EnumMap<>(ElementalShell121.Kind.class);
    private static void check(boolean value, String why) { checks++; if (!value) throw new AssertionError(why); }
    private static String position(Board board, MoveBudget budget) {
        return board.remainingMask()+":"+board.gateMask()+":"+Arrays.toString(board.history())+":"+budget.used()+":"+budget.remaining();
    }
    private static String content(Level level) {
        StringBuilder text = new StringBuilder();
        for (Tile tile : level.tiles) text.append(tile.id).append(':').append(tile.row).append(':').append(tile.col)
                .append(':').append(tile.direction).append(':').append(tile.switchMask).append(':').append(level.isKey(tile.id)).append('|');
        for (Barrier barrier : level.barriers) text.append('K').append(barrier.row).append(':').append(barrier.col).append(':').append(barrier.keyTileId).append('|');
        return text.append(Arrays.toString(level.solution)).append(Arrays.toString(level.actionSolution)).toString();
    }
    private static ElementalShell121 roundtrip(Level level, ElementalShell121 value) {
        ElementalShell121 restored = ElementalShell121.decode(level, value.encode());
        check(restored != null && restored.encode().equals(value.encode()), "actual intermediate state restores canonically");
        check(restored.totalLayers() == value.totalLayers() && restored.remainingCount() == value.remainingCount(), "cold shell counters match");
        for (Tile tile : level.tiles) check(restored.kind(tile.id)==value.kind(tile.id) && restored.layers(tile.id)==value.layers(tile.id), "cold element and layer match every real ID");
        return restored;
    }
    private static void campaign() {
        Set<String> placements = new HashSet<>();
        for (int number=1;number<=1000;number++) {
            Level level=RelayCatalog.get(number); boards++;
            String geometry=content(level);
            ElementalShell121 shell=ElementalShell121.create(level,0,number);
            check(shell.encode().equals(ElementalShell121.create(level,0,number).encode()), "assignment deterministic");
            check(shell.encode().equals(ElementalShell121.create(level,5,number).encode()), "expedition U has the identical deterministic rule");
            check(shell.totalLayers()<=6 && shell.remainingCount()<=3, "finite at most six taps and three present IDs");
            check(shell.totalLayers()==(number<=10?0:5), "first ten tutorial boards free; later ICE2 FIRE2 WATER1");
            check(ElementalShell121.empty(level).totalLayers()==0, "old saved attempts have no introduced shell");
            for(int mode : new int[]{-1,1,2,3,4,6}) check(ElementalShell121.create(level,mode,number).totalLayers()==0, "events remix friend and unknown modes unchanged");
            for(Tile tile:level.tiles) if(shell.layers(tile.id)>0) {
                elements.put(shell.kind(tile.id),elements.getOrDefault(shell.kind(tile.id),0)+1);
                check(shell.layers(tile.id)==(shell.kind(tile.id)==ElementalShell121.Kind.WATER?1:2), "declared element durability");
            }
            placements.add(shell.encode().split("\\|",-1)[3]);
            shell=roundtrip(level,shell);
            String untouched=shell.encode();
            for(int invalid : new int[]{-1,level.tiles.size(),Integer.MAX_VALUE}) {
                check(shell.kind(invalid)==ElementalShell121.Kind.NONE && shell.layers(invalid)==0, "missing ID is bare");
                check(shell.tap(invalid)==ElementalShell121.TapResult.NONE && !shell.clear(invalid), "missing ID refuses without mutation");
                check(shell.encode().equals(untouched), "refused ID preserves state exactly");
            }
            Board board=new Board(level); MoveBudget budget=MoveBudget.start(0,level);
            int prepared=0;
            for(int id:level.solution) {
                while(shell.layers(id)>0) {
                    String before=position(board,budget); int previous=shell.layers(id),total=shell.totalLayers();
                    ElementalShell121.TapResult result=shell.tap(id); taps++; prepared++;
                    check(shell.layers(id)==previous-1 && shell.totalLayers()==total-1, "one tap removes exactly one layer");
                    check(result==(previous==1?ElementalShell121.TapResult.CLEARED:ElementalShell121.TapResult.CRACKED), "tap announces crack versus liberation");
                    check(position(board,budget).equals(before), "shell tap preserves actual board, gate, history and action budget");
                    shell=roundtrip(level,shell);
                }
                check(shell.kind(id)==ElementalShell121.Kind.NONE, "arrow piece becomes bare before normal action");
                check(budget.attempt(board,id).success, "unaltered certified exit remains legal after finite shell taps");
                check(shell.tap(id)==ElementalShell121.TapResult.NONE, "cleared shell cannot replay a liberation");
            }
            check(prepared<=6 && board.isSolved() && budget.used()==level.minimumActions, "all1000 exact plans still solve at original productive-action minimum"); solved++;
            check(shell.totalLayers()==0 && shell.remainingCount()==0, "completed plan leaves no phantom shell");
            check(content(level).equals(geometry), "arrows switches barriers keys identities and certificates stay exact");
            if(!level.barriers.isEmpty()) keyBoards++;
            Board undoBoard=new Board(level);MoveBudget undoBudget=MoveBudget.start(0,level);
            ElementalShell121 cleared=ElementalShell121.create(level,0,number);
            for(Tile tile:level.tiles) while(cleared.layers(tile.id)>0) cleared.tap(tile.id);
            String broken=cleared.encode();
            check(undoBudget.attempt(undoBoard,level.solution[0]).success && undoBudget.undo(undoBoard,0), "real exit and current-policy Undo execute");
            check(cleared.encode().equals(broken) && cleared.totalLayers()==0, "Undo restores the piece while already broken shells stay broken");
            check(ElementalShell121.create(level,0,number).totalLayers()==(number<=10?0:5), "new attempt restarts deterministic shells");
            if(number<1000) check(ElementalShell121.decode(RelayCatalog.get(number+1),untouched)==null, "cross-level state never imports");
        }
        check(placements.size()>20, "assignments vary across real U layouts");
        for(ElementalShell121.Kind kind:new ElementalShell121.Kind[]{ElementalShell121.Kind.ICE,ElementalShell121.Kind.FIRE,ElementalShell121.Kind.WATER}) check(elements.get(kind)==990, "all three elements cover U11–U1000");
    }
    private static void companionExits() {
        for(int number=11;number<=1000;number++) {
            Level level=RelayCatalog.get(number);
            for(CompanionBattle.Definition definition:CompanionBattle.all()) {
                if(definition.effect!=CompanionBattle.Effect.PULSE && definition.effect!=CompanionBattle.Effect.FLOW && definition.effect!=CompanionBattle.Effect.GUIDE && definition.effect!=CompanionBattle.Effect.TRAIL) continue;
                Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);
                ElementalShell121 shell=ElementalShell121.create(level,0,number);String prior=shell.encode();
                CompanionBattle.State state=CompanionBattle.State.start(Collections.singletonList(definition.id));
                CompanionBattleAction.Draft out=CompanionBattleAction.prepare(board,budget,0,0,state,0,false,level.solution);
                check(out!=null, "actual owned-definition draft is certified");
                check(shell.encode().equals(prior), "preparing any companion command cannot clear shells before commit");
                long removed=board.remainingMask() & ~out.board.remainingMask();
                for(Tile tile:level.tiles) if((removed & (1L<<tile.id))!=0) {
                    int previous=shell.layers(tile.id); boolean changed=shell.clear(tile.id); automaticExits++;
                    check(changed==(previous>0) && shell.layers(tile.id)==0 && !out.board.isPresent(tile.id), "only actually exited IDs have shell cleared after commit");
                    check(!shell.clear(tile.id), "duplicate companion receipt cannot clear twice");
                }
                if(definition.effect==CompanionBattle.Effect.GUIDE || definition.effect==CompanionBattle.Effect.TRAIL)
                    check(shell.encode().equals(prior) && removed==0, "hints and trails reveal without bypassing elements");
                check(board.remainingMask()==level.allTilesMask && budget.used()==0, "draft retains original session until caller commits");
            }
        }
        check(automaticExits>0, "real PULSE and FLOW exits covered");
    }
    private static void reject(String encoded, Level level) { check(ElementalShell121.decode(level,encoded)==null, "strict malformed/cross-puzzle rejection"); }
    private static Direction rotate(Direction direction) {
        for(Direction candidate:Direction.values()) if(candidate.dr==direction.dc && candidate.dc==-direction.dr) return candidate;
        throw new AssertionError("missing quarter-turn direction");
    }
    private static int rotateMask(int mask) {
        int result=0;for(Direction direction:Direction.values()) if((mask & direction.bit)!=0) result|=rotate(direction).bit;
        return result;
    }
    private static Level rotated(Level level) {
        List<Tile> tiles=new ArrayList<>();List<Barrier> barriers=new ArrayList<>();
        for(Tile tile:level.tiles) tiles.add(new Tile(tile.id,tile.col,level.size-1-tile.row,rotate(tile.direction),rotateMask(tile.switchMask)));
        for(Barrier barrier:level.barriers) barriers.add(new Barrier(barrier.col,level.size-1-barrier.row,barrier.keyTileId));
        return new Level(level.number,level.chapter,level.size,rotateMask(level.initialGateMask),tiles,level.solution,level.isEvent,level.seed,barriers,level.progressionVersion,level.minimumActions,level.actionSolution);
    }
    private static void malformed() {
        Level level=RelayCatalog.get(1000);ElementalShell121 shell=ElementalShell121.create(level,0,1000);
        String good=shell.encode();String[] fields=good.split("\\|",-1),rows=fields[3].split(";");
        for(String value:new String[]{null,"",good+"|extra"," "+good,good+" ",good.replace("ES121","ES122"),good+";",good.replace(fields[1],fields[1]+"0")}) reject(value,level);
        for(String count:new String[]{"-1","0","01","+1","999999999999999999999","2.0","NaN"}) {
            String[] row=rows[0].split(":");row[2]=count;
            String[] copy=fields.clone();copy[3]=String.join(":",row)+";"+rows[1]+";"+rows[2];reject(String.join("|",copy),level);
        }
        for(String row:new String[]{rows[0]+";"+rows[0],rows[2]+";"+rows[0],"-1:ICE:1","01:ICE:1","0:NONE:1","0:LAVA:1","49:WATER:1","0:ICE:3",";"}) {
            String[] copy=fields.clone();copy[3]=row;reject(String.join("|",copy),level);
        }
        for(int id=0;id<level.tiles.size();id++) {
            ElementalShell121.Kind assigned=shell.kind(id);
            ElementalShell121.Kind wrong=assigned==ElementalShell121.Kind.ICE?ElementalShell121.Kind.FIRE:ElementalShell121.Kind.ICE;
            String[] copy=fields.clone();copy[3]=id+":"+wrong+":1";reject(String.join("|",copy),level);
        }
        Level otherSeed=new Level(level.number,level.chapter,level.size,level.initialGateMask,level.tiles,level.solution,level.isEvent,level.seed+1,level.barriers,level.progressionVersion,level.minimumActions,level.actionSolution);
        reject(good,otherSeed);
        Level otherNumber=new Level(level.number+1,level.chapter,level.size,level.initialGateMask,level.tiles,level.solution,level.isEvent,level.seed,level.barriers,level.progressionVersion,level.minimumActions,level.actionSolution);
        reject(good,otherNumber);
        Level otherGeometry=rotated(level);
        check(otherGeometry.number==level.number && otherGeometry.seed==level.seed && otherGeometry.tiles.size()==level.tiles.size(), "geometry witness keeps number seed and IDs");
        check(new Board(otherGeometry).hasSolution(), "geometry witness is a separately valid puzzle");
        reject(good,otherGeometry);
        check(ElementalShell121.decode(null,good)==null, "null puzzle does not partially load");
        boolean refused=false;try{ElementalShell121.create(level,0,999);}catch(IllegalArgumentException expected){refused=true;}
        check(refused,"caller cannot mislabel a relay index");
        check(ElementalShell121.create(LearningCatalog.get(1000),0,1000).totalLayers()==0,"preserved N puzzle cannot acquire U shells");
        check(ElementalShell121.create(StrategyCatalog.get(1000),0,1000).totalLayers()==0,"preserved L puzzle cannot acquire U shells");
    }
    public static void main(String[] args) throws Exception {
        long started=System.nanoTime();campaign();companionExits();malformed();
        String report="{\n  \"passed\": true,\n  \"checks\": "+checks+",\n  \"boards\": "+boards+",\n  \"certified_plans_solved\": "+solved+",\n  \"shell_taps\": "+taps+",\n  \"certified_companion_exits\": "+automaticExits+",\n  \"boards_with_original_keys\": "+keyBoards+",\n  \"seconds\": "+String.format(Locale.ROOT,"%.3f",(System.nanoTime()-started)/1e9)+",\n  \"scope\": \"Actual U1-U1000 with deterministic bounded shell taps, unchanged board/action budgets/keys/arrows, every intermediate canonical cold roundtrip, strict malformed and cross-puzzle rejection, actual PULSE/FLOW draft exits and non-bypassing hints, Undo retains broken shells. Pure host model, not Android taps/rendering, native persistence, human difficulty or enjoyment.\"\n}\n";
        if(args.length>0){Path out=Path.of(args[0]);Files.createDirectories(out.getParent());Files.writeString(out,report);}
        System.out.print(report);
    }
}
