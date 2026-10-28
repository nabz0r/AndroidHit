import com.androidhit.lumadrift.engine.*;
import java.util.*;

/** Host-only search/replay. No Android code, saved progress, or engine changes. */
public final class DeadEndFixture {
    private static int examined;

    public static void main(String[] args) {
        int number = args.length == 0 ? 31 : Integer.parseInt(args[0]);
        String code = ChallengeCode.encodeRelay(number);
        ChallengeCode parsed = ChallengeCode.parse(code);
        require(parsed != null && parsed.kind == ChallengeCode.Kind.RELAY, "Public U code round trip");
        Level level = parsed.resolve();
        require(level.progressionVersion == 6, "Resolved relay rules");
        int[] witness = findShortest(level);
        require(witness != null && witness.length > 0, "Legal dead-end witness exists on U" + number);
        Map<String,Object> result = object(
                "schemaVersion", 1, "family", "U", "levelNumber", number,
                "size", level.size, "code", code, "deepLink", "lumadrift://challenge/" + code,
                "coordinateBase", 0, "progressionVersion", level.progressionVersion,
                "gateBits", object("UP", 1, "RIGHT", 2, "DOWN", 4, "LEFT", 8),
                "expectedSession", object("mode", 4, "content_version", 4,
                        "budget_policy", MoveBudget.start(4,level).policyVersion(),
                        "challenge_code", code, "budget_used", 0),
                "scope", "Host engine search and complete legal replay, not independent rules or native UI evidence. No Android device or emulator is accessed.",
                "search", object("algorithm", "Breadth-first legal exits from soluble prefixes; stable ascending tile order", "shortestLength", witness.length, "examinedTransitions", examined));
        List<Object> tiles = new ArrayList<>();
        for (Tile tile : level.tiles) tiles.add(tile(tile));
        result.put("tiles", tiles);
        List<Object> barriers = new ArrayList<>();
        for (Barrier wall : level.barriers) barriers.add(object("row", wall.row, "col", wall.col, "keyTileId", wall.keyTileId));
        result.put("barriers", barriers);

        Board board = new Board(level);
        require(board.hasSolution(), "Initial board soluble");
        result.put("initial", state(board));
        List<Object> steps = new ArrayList<>();
        for (int i = 0; i < witness.length; i++) {
            int id = witness[i];
            Tile tile = level.tile(id);
            Map<String,Object> step = object("step", i + 1, "actionId", id, "tileId", id,
                    "row", tile.row, "col", tile.col, "direction", tile.direction.name(),
                    "switchMask", tile.switchMask,
                    "accessibilityLabelPrefix", label(tile), "before", state(board));
            require(board.hasSolution(), "Every prefix before final exit is soluble");
            Board.MoveResult move = board.performAction(id);
            require(move.success && move.reason == Board.BlockReason.NONE, "Every witness exit is legal");
            step.put("legal", move.success);
            step.put("after", state(board));
            steps.add(step);
        }
        result.put("steps", steps);
        require(board.isDeadEnd() && !board.isSolved(), "Final position is insoluble and unfinished");
        result.put("deadEnd", state(board));

        int restoredId = witness[witness.length - 1];
        Map<String,Object> undo = object("action", "UNDO", "restoredActionId", restoredId,
                "restoredTile", tile(level.tile(restoredId)), "before", state(board));
        require(board.undo(), "One actual Board.undo succeeds");
        require(board.hasSolution() && !board.isDeadEnd(), "One undo restores a soluble position");
        int[] prefix = Arrays.copyOf(witness, witness.length - 1);
        require(Arrays.equals(prefix, board.history()), "Undo restores exact history prefix");
        undo.put("after", state(board));
        int[] recovery = board.solve();
        require(recovery != null, "Solution from recovered state exists");
        undo.put("solutionActionIds", recovery);
        List<Object> recoverySteps = new ArrayList<>();
        for (int i = 0; i < recovery.length; i++) {
            Tile tile = level.tile(recovery[i]);
            Map<String,Object> step = object("step", i + 1, "actionId", tile.id,
                    "tileId", tile.id, "row", tile.row, "col", tile.col,
                    "direction", tile.direction.name(), "switchMask", tile.switchMask,
                    "accessibilityLabelPrefix", label(tile), "before", state(board));
            require(board.performAction(tile.id).success, "Recovered solution replay is legal");
            step.put("after", state(board));
            recoverySteps.add(step);
        }
        require(board.isSolved() && board.remainingCount() == 0, "Recovered solution replay completes board");
        undo.put("solutionSteps", recoverySteps);
        undo.put("completed", state(board));
        result.put("undo", undo);
        result.put("verified", object("allWitnessExitsLegal", true, "finalInsoluble", true,
                "oneUndoSoluble", true, "exactHistoryRestored", true,
                "fullRecoverySolutionLegallyReplayed", true, "recoverySolved", true));
        System.out.println(json(result));
    }

    private static int[] findShortest(Level level) {
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        HashSet<Long> seen = new HashSet<>();
        queue.add(new int[0]);
        seen.add(level.allTilesMask);
        while (!queue.isEmpty()) {
            int[] prefix = queue.remove();
            Board board = new Board(level);
            require(board.restoreHistory(prefix), "Search prefix legally replays");
            require(board.hasSolution(), "Search explores only soluble prefixes");
            for (Tile tile : board.legalMoves()) {
                examined++;
                int[] next = Arrays.copyOf(prefix, prefix.length + 1);
                next[prefix.length] = tile.id;
                require(board.performAction(tile.id).success, "Search legal move succeeds");
                if (board.isDeadEnd()) return next;
                if (seen.add(board.remainingMask())) queue.add(next);
                require(board.undo(), "Search undo succeeds");
            }
        }
        return null;
    }

    private static Map<String,Object> state(Board board) {
        List<Integer> remaining = new ArrayList<>(), legal = new ArrayList<>();
        for (Tile tile : board.remainingTiles()) remaining.add(tile.id);
        for (Tile tile : board.legalMoves()) legal.add(tile.id);
        return object("history", board.history(), "gateMask", board.gateMask(),
                "openGates", openGates(board.gateMask()), "remainingMask", board.remainingMask(),
                "remainingTileIds", remaining, "remainingCount", board.remainingCount(),
                "legalActionIds", legal, "soluble", board.hasSolution(),
                "deadEnd", board.isDeadEnd(), "stuck", board.isStuck(), "solved", board.isSolved());
    }

    private static Map<String,Object> tile(Tile tile) {
        return object("tileId", tile.id, "row", tile.row, "col", tile.col,
                "displayRow", tile.row + 1, "displayCol", tile.col + 1,
                "direction", tile.direction.name(), "switchMask", tile.switchMask,
                "accessibilityLabelPrefix", label(tile));
    }

    private static String label(Tile tile) {
        return "Piece, row " + (tile.row + 1) + ", column " + (tile.col + 1)
                + ", arrow " + tile.direction.name().toLowerCase(Locale.ROOT);
    }

    private static List<String> openGates(int gates) {
        List<String> result = new ArrayList<>();
        for (Direction direction : Direction.values()) if ((gates & direction.bit) != 0) result.add(direction.name());
        return result;
    }

    private static Map<String,Object> object(Object... pairs) {
        LinkedHashMap<String,Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) result.put((String)pairs[i], pairs[i + 1]);
        return result;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static String json(Object value) {
        if (value == null) return "null";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof String) return "\"" + ((String)value).replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
        if (value instanceof int[]) {
            List<Integer> result = new ArrayList<>();
            for (int entry : (int[])value) result.add(entry);
            return json(result);
        }
        List<String> entries = new ArrayList<>();
        if (value instanceof Map) {
            for (Map.Entry<?,?> entry : ((Map<?,?>)value).entrySet()) entries.add(json(entry.getKey()) + ":" + json(entry.getValue()));
            return "{" + String.join(",", entries) + "}";
        }
        if (value instanceof Iterable) {
            for (Object entry : (Iterable<?>)value) entries.add(json(entry));
            return "[" + String.join(",", entries) + "]";
        }
        throw new IllegalArgumentException("Unsupported JSON value " + value.getClass());
    }
}
