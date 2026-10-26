import com.androidhit.lumadrift.engine.Board;
import com.androidhit.lumadrift.engine.Direction;
import com.androidhit.lumadrift.engine.Level;
import com.androidhit.lumadrift.engine.LevelCatalog;
import com.androidhit.lumadrift.engine.Tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Dependency-free, repeatable correctness checks; run with scripts/test-engine.sh. */
public final class EngineTest {
    private static int assertions;
    private static int alternatives;
    private static int unsafeAlternatives;

    public static void main(String[] args) {
        long started = System.nanoTime();
        fixtures();
        Set<String> boards = new HashSet<String>();
        for (int number = 1; number <= LevelCatalog.LEVEL_COUNT; number++) {
            Level level = LevelCatalog.get(number);
            check(level.number == number, "Campaign numbering");
            check(level.chapter == (number - 1) / 30 + 1, "Chapter numbering");
            check(boards.add(signature(level)), "Unique campaign layout " + number);
            verify(level, true);
            if (number <= 3) check(new Board(level).legalMoves().size() == level.tiles.size(), "Free tutorial rays");
            if (number <= 5) {
                check(level.initialGateMask == 15, "Tutorial gates open");
                for (Tile tile : level.tiles) check(tile.switchMask == 0, "No early switches");
            }
        }
        for (int i = 0; i < 1000; i++) {
            long seed = i < 500 ? 20260000L + i : new Random(i * 31L).nextLong();
            Level event = LevelCatalog.event(seed);
            check(event.isEvent && event.chapter == 0 && event.seed == seed, "Event identity");
            verify(event, i % 20 == 0);
            if (i % 10 == 0) check(signature(event).equals(signature(LevelCatalog.event(seed))), "Deterministic event seed");
        }
        verify(LevelCatalog.event(Long.MIN_VALUE), false);
        verify(LevelCatalog.event(Long.MAX_VALUE), false);
        System.out.println("PASS: 240 campaign levels + 1002 event seeds; " + assertions
                + " assertions, " + alternatives + " alternate legal starts (" + unsafeAlternatives
                + " correctly require undo), " + String.format(java.util.Locale.ROOT, "%.2f", (System.nanoTime() - started) / 1e9) + " s.");
    }

    private static void verify(Level level, boolean alternativesToo) {
        Board board = new Board(level);
        long initialMask = board.remainingMask();
        int initialGates = board.gateMask();
        check(board.remainingCount() == level.tiles.size(), "Initial count");
        check(board.hint() >= 0, "Initial exact hint");
        for (int id : level.solution) {
            Board.Snapshot before = board.snapshot();
            check(board.canMove(id), "Certified move legal, board " + level.number + " tile " + id);
            check(board.isSafeMove(id), "Certified move remains solvable");
            check(board.snapshot().remainingMask == before.remainingMask && board.gateMask() == before.gateMask, "Preview immutable");
            Board.MoveResult result = board.move(id);
            check(result.success, "Certified move succeeds");
            check(result.gateMask == (result.previousGateMask ^ level.tile(id).switchMask), "Gate transition exact");
            check(board.remainingCount() == before.remaining - 1, "One piece per action");
            Board.Snapshot after = board.snapshot();
            check(!board.move(id).success && board.remainingMask() == after.remainingMask
                    && board.moves() == after.moves && board.gateMask() == after.gateMask, "Repeated action immutable");
        }
        check(board.isSolved() && board.hint() == -1 && board.remainingCount() == 0, "Full completion");
        check(board.moves() == level.parMoves, "Par is exact piece count");
        while (board.canUndo()) check(board.undo(), "Undo succeeds");
        check(board.remainingMask() == initialMask && board.gateMask() == initialGates && board.moves() == 0, "Undo restores every state");
        check(!board.undo(), "No undo before first action");

        int[] half = Arrays.copyOf(level.solution, level.solution.length / 2);
        check(board.restoreHistory(half), "Restore validated session");
        check(Arrays.equals(board.history(), half), "History stable");
        Board.Snapshot saved = board.snapshot();
        check(!board.restoreHistory(new int[] {-1}) && board.remainingMask() == saved.remainingMask
                && board.gateMask() == saved.gateMask, "Corrupt save atomic");
        check(!board.restoreHistory(new int[] {0, 0}), "Duplicate save action rejected");
        board.restart();
        check(board.remainingMask() == initialMask && board.gateMask() == initialGates && board.moves() == 0, "Restart exact");

        if (alternativesToo) {
            List<Tile> possible = board.legalMoves();
            for (Tile candidate : possible) {
                board.restart();
                boolean safe = board.isSafeMove(candidate.id);
                check(board.move(candidate.id).success, "Alternative start legal");
                int[] solution = board.solve();
                check(safe == (solution != null), "Safe preview agrees with full search");
                if (solution != null) {
                    for (int id : solution) check(board.move(id).success, "Alternative solution legal");
                    check(board.isSolved(), "Alternative solved");
                } else {
                    unsafeAlternatives++;
                    check(board.hint() == -1 && board.undo() && board.hasSolution(), "Deadlock reversible with undo");
                }
                alternatives++;
            }
            // Player choices can differ from the certified sequence at every step.
            board.restart();
            Random player = new Random(level.seed);
            while (!board.isSolved()) {
                int hint = board.hint();
                if (hint < 0) break;
                List<Tile> legal = board.legalMoves();
                Tile chosen = legal.get(player.nextInt(legal.size()));
                check(board.move(chosen.id).success, "Player path legal");
                if (!board.hasSolution()) {
                    check(board.undo() && board.move(hint).success, "Unsafe choice recoverable");
                }
            }
            check(board.isSolved(), "Player path recovers and completes");
        }
    }

    private static void fixtures() {
        List<Tile> tiles = new ArrayList<Tile>();
        tiles.add(new Tile(0, 2, 1, Direction.RIGHT, Direction.RIGHT.bit));
        tiles.add(new Tile(1, 2, 3, Direction.UP, Direction.RIGHT.bit));
        tiles.add(new Tile(2, 0, 3, Direction.LEFT, 0));
        Level level = new Level(999, 1, 5, 15, tiles, new int[] {2, 1, 0}, false, 0);
        Board board = new Board(level);
        check(board.blockReason(0) == Board.BlockReason.BLOCKED_PATH && board.blockingTile(0) == 1, "Nearest ray blocker");
        check(board.blockReason(-1) == Board.BlockReason.MISSING_TILE, "Unknown tile handled");
        check(!board.move(0).success && board.moves() == 0, "Blocked move no effect");
        check(board.move(2).success && board.move(1).success, "Unlock path");
        check(board.blockReason(0) == Board.BlockReason.CLOSED_GATE, "Switch closes target gate");
        check(!board.hasSolution() && board.hint() == -1, "Unsolvable position reported");
        check(board.undo() && board.gateMask() == 15, "Switch undo restores gate");
        check(board.restoreHistory(new int[] {2}), "Fixture save replay");
        // Open gates are tested before an exiting switch toggles them.
        List<Tile> single = Arrays.asList(new Tile(0, 1, 1, Direction.UP, Direction.UP.bit));
        Board finalSwitch = new Board(new Level(1, 1, 3, 15, single, new int[] {0}, false, 0));
        check(finalSwitch.move(0).success && finalSwitch.isSolved() && finalSwitch.gateMask() == 14,
                "Switch fires after exit even when it closes its own gate");
    }

    private static String signature(Level level) {
        StringBuilder result = new StringBuilder().append(level.size).append('/').append(level.initialGateMask);
        for (Tile tile : level.tiles) result.append('|').append(tile.id).append(',').append(tile.row)
                .append(',').append(tile.col).append(',').append(tile.direction).append(',').append(tile.switchMask);
        return result.toString();
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
}
