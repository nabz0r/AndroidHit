import com.androidhit.lumadrift.engine.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/** All reachable subsets, using an independent cell-ray model and exact reachability. */
public final class BoardAuditV17 {
    private static long checks, states, legalEdges, deadWithLegalExits, losingStates;
    private static int boards;
    private static final StringBuilder rows = new StringBuilder(
            "family,number,pieces,base_actions,spare_actions,keys,seals,reachable_states,losing_states,dead_with_legal_exits,legal_edges,initial_solution\n");

    private static void check(boolean good, String detail) {
        checks++;
        if (!good) throw new AssertionError(detail);
    }

    /**
     * Model independently walks cells, rather than calling Board's geometry,
     * Solver, or a level certificate. Gate state is XOR-derived from the subset.
     * Each reachable subset is fully evaluated, including every losing branch.
     */
    private static final class Reference {
        final Level level;
        final long[] obstruction;
        final byte[] result;
        final boolean[] visited;
        long reached, dead, deadLegal, edges;

        Reference(Level level) {
            this.level = level;
            check(level.tiles.size() <= 22, "Reference subset size cap");
            obstruction = new long[level.tiles.size()];
            result = new byte[1 << level.tiles.size()];
            visited = new boolean[result.length];
            result[0] = 2;
            for (Tile tile : level.tiles) {
                for (int row = tile.row + tile.direction.dr, col = tile.col + tile.direction.dc;
                     row >= 0 && row < level.size && col >= 0 && col < level.size;
                     row += tile.direction.dr, col += tile.direction.dc) {
                    for (Tile other : level.tiles) if (other.row == row && other.col == col)
                        obstruction[tile.id] |= 1L << other.id;
                    for (Barrier barrier : level.barriers) if (barrier.row == row && barrier.col == col)
                        obstruction[tile.id] |= 1L << barrier.keyTileId;
                }
            }
            evaluate(level.allTilesMask);
        }

        int gates(long mask) {
            int gates = level.initialGateMask;
            for (Tile tile : level.tiles) if ((mask & (1L << tile.id)) == 0) gates ^= tile.switchMask;
            return gates;
        }

        boolean can(long mask, Tile tile) {
            return (mask & (1L << tile.id)) != 0 && (gates(mask) & tile.direction.bit) != 0
                    && (mask & obstruction[tile.id]) == 0;
        }

        boolean evaluate(long mask) {
            int index = (int) mask;
            if (result[index] != 0) return result[index] == 2;
            boolean wins = false;
            for (Tile tile : level.tiles) if (can(mask, tile)) {
                // Deliberately evaluate every child even after finding a completion.
                boolean child = evaluate(mask & ~(1L << tile.id));
                wins |= child;
            }
            result[index] = (byte) (wins ? 2 : 1);
            return wins;
        }
    }

    private static void walk(Board board, Reference oracle, int margin) {
        long mask = board.remainingMask();
        int index = (int) mask;
        if (oracle.visited[index]) return;
        oracle.visited[index] = true;
        oracle.reached++;
        states++;
        int gates = oracle.gates(mask), remaining = Long.bitCount(mask), legal = 0;
        boolean solvable = oracle.result[index] == 2;
        check(board.gateMask() == gates, "Independent gate XOR agrees");
        for (Tile tile : board.level.tiles) {
            boolean allowed = oracle.can(mask, tile);
            check(board.canMove(tile.id) == allowed, "Independent cell-ray legality agrees");
            if (allowed) { legal++; oracle.edges++; legalEdges++; }
        }
        if (!solvable) { oracle.dead++; losingStates++; }
        if (!solvable && legal > 0) { oracle.deadLegal++; deadWithLegalExits++; }

        int[] history = board.history();
        int[] completion = board.solve();
        check((completion != null) == solvable, "Solver and independent reachability agree");
        if (completion != null) {
            long finalMask = mask;
            for (int id : completion) {
                check(oracle.can(finalMask, board.level.tile(id)), "Returned completion obeys independent cell/gate/key model");
                finalMask &= ~(1L << id);
            }
            check(finalMask == 0 && completion.length == remaining, "Returned completion clears every remaining piece");
        }
        check(board.hint() == (completion == null || completion.length == 0 ? -1 : completion[0]),
                "Current-position hint matches exact completion");
        check(board.isDeadEnd() == (!solvable && remaining != 0), "Dead-end API includes legal-but-doomed states");
        check(board.isStuck() == (remaining != 0 && legal == 0), "Legacy immediate-stuck semantics retained");

        int available = remaining + margin;
        Board.Guidance guidance = board.guidance(available);
        Board.PositionStatus expected = remaining == 0 ? Board.PositionStatus.SOLVED
                : solvable ? Board.PositionStatus.PLAYABLE : Board.PositionStatus.DEAD_END;
        check(guidance.status == expected, "Guidance accurately distinguishes solved/playable/dead");
        check(guidance.remainingActions == available && guidance.remainingPieces == remaining, "Guidance exposes exact counters");
        check(guidance.canUseHint() == (solvable && remaining > 0), "A paid hint is usable only on a finishable run");
        check(guidance.hintTileId == (completion == null || remaining == 0 ? -1 : completion[0]), "Guidance hint is exact");
        check(guidance.actionsToFinish == (solvable ? remaining : -1), "Extra actions cannot fix gate-order dead ends");

        int undos = 0;
        if (!solvable) {
            long prior = mask;
            undos = -1;
            for (int i = history.length - 1; i >= 0; i--) {
                prior |= 1L << history[i];
                if (oracle.evaluate(prior)) { undos = history.length - i; break; }
            }
        }
        int recoveryActions = undos < 0 ? -1 : remaining + 2 * undos;
        check(guidance.undoSteps == undos, "Fewest undo recovery steps verified independently");
        check(guidance.actionsToRecoverAndFinish == recoveryActions, "Recovery charges undo and restored exits");
        check(guidance.recoveryFitsBudget == (recoveryActions >= 0 && recoveryActions <= available), "Recovery budget exact");
        check(board.remainingMask() == mask && board.gateMask() == gates && Arrays.equals(board.history(), history),
                "Solver, hint and guidance never mutate a run");

        for (Tile tile : board.level.tiles) if (oracle.can(mask, tile)) {
            check(board.move(tile.id).success, "Reachable-state transition succeeds");
            walk(board, oracle, margin);
            check(board.undo() && board.remainingMask() == mask && board.gateMask() == gates, "Traversal undo is exact");
        }
    }

    private static void verifyBoard(Level level, boolean remix) {
        Reference oracle = new Reference(level);
        check(oracle.result[(int) level.allTilesMask] == 2, "Every initial board is solvable independently of its certificate");
        Board board = new Board(level);
        MoveBudget budget = MoveBudget.start(remix ? 3 : 0, level);
        check(budget.base() >= level.tiles.size(), "Initial actions cover all necessary exits");
        for (int id : level.solution) check(budget.consumeAction() && board.move(id).success, "Certificate fits initial budget");
        check(board.isSolved() && budget.extensions() == 0, "No power, ad, extension or currency needed");
        board.restart();
        String code = remix ? ChallengeCode.encodeBalancedRemixV3(level.number) : ChallengeCode.encodeBalancedV3(level.number);
        check(ChallengeCode.parse(code).resolve() == level, "Frozen J/K challenge identity resolves unchanged");
        walk(board, oracle, budget.mistakeAllowance());
        check(oracle.reached > 1, "At least initial and completed subsets visited");
        rows.append(remix ? "remix" : "journey").append(',').append(level.number).append(',')
                .append(level.tiles.size()).append(',').append(budget.base()).append(',').append(budget.mistakeAllowance()).append(',')
                .append(BalancedCatalog.info(level.number, remix).keyCount).append(',').append(level.barriers.size()).append(',')
                .append(oracle.reached).append(',').append(oracle.dead).append(',').append(oracle.deadLegal).append(',')
                .append(oracle.edges).append(',').append('"').append(Arrays.toString(level.solution)).append('"').append('\n');
        boards++;
    }

    private static void budgetEdges() {
        Level level = BalancedCatalog.get(13);
        Board board = new Board(level);
        int count = level.tiles.size();
        check(board.guidance(count).status == Board.PositionStatus.PLAYABLE, "Exact N actions suffice");
        check(board.guidance(count - 1).status == Board.PositionStatus.NEEDS_ACTIONS, "One short action is detected before zero");
        check(!board.guidance(count - 1).canUseHint(), "Do not spend hint stock when current actions cannot finish");
        check(board.guidance(0).status == Board.PositionStatus.NEEDS_ACTIONS, "Exhaustion is distinct from dead end");
        check(board.guidance(-1).status == Board.PositionStatus.PLAYABLE && board.guidance(-1).recoveryFitsBudget,
                "Unlimited friend run is supported");
        try { board.guidance(-2); throw new AssertionError("Invalid negative action count"); }
        catch (IllegalArgumentException expected) { checks++; }
        for (int id : level.solution) check(board.move(id).success, "Budget edge completion");
        check(board.guidance(0).status == Board.PositionStatus.SOLVED && !board.guidance(0).canUseHint(), "Win has priority at exact zero");

        // An externally-created malformed certificate may describe an impossible initial board.
        // Guidance still returns a safe 'no recovery' result without claiming the catalog is broken.
        Level impossible = new Level(1, 1, 2, 0, Arrays.asList(new Tile(0, 0, 0, Direction.UP, 0)),
                new int[]{0}, false, 0);
        Board.Guidance none = new Board(impossible).guidance(100);
        check(none.status == Board.PositionStatus.DEAD_END && none.undoSteps == -1
                && none.actionsToRecoverAndFinish == -1 && !none.recoveryFitsBudget, "No invented recovery from impossible initial fixture");
    }

    private static List<Integer> shortestDeadPrefix(Level level, boolean requireLegal) {
        Reference oracle = new Reference(level);
        ArrayDeque<long[]> queue = new ArrayDeque<>();
        HashMap<Long, int[]> parents = new HashMap<>();
        queue.add(new long[]{level.allTilesMask});
        parents.put(level.allTilesMask, new int[0]);
        while (!queue.isEmpty()) {
            long mask = queue.remove()[0];
            int[] path = parents.get(mask);
            boolean hasLegal = false;
            for (Tile tile : level.tiles) if (oracle.can(mask, tile)) hasLegal = true;
            if (oracle.result[(int) mask] == 1 && (!requireLegal || hasLegal)) {
                ArrayList<Integer> found = new ArrayList<>();
                for (int id : path) found.add(id);
                return found;
            }
            for (Tile tile : level.tiles) if (oracle.can(mask, tile)) {
                long child = mask & ~(1L << tile.id);
                if (!parents.containsKey(child)) {
                    int[] next = Arrays.copyOf(path, path.length + 1); next[path.length] = tile.id;
                    parents.put(child, next); queue.add(new long[]{child});
                }
            }
        }
        return Collections.emptyList();
    }

    private static String journey13() {
        Level level = BalancedCatalog.get(13);
        Board board = new Board(level);
        MoveBudget budget = MoveBudget.start(0, level);
        Reference oracle = new Reference(level);
        StringBuilder text = new StringBuilder("# Journey 13: reproducible diagnosis\n\n")
                .append("The reported player save and exact installed version were not available. This reproduces the frozen 1.6 J13 board and checks the other historical board-13 identities separately. It does not identify the user's exact tap sequence.\n\n")
                .append("- Identity: `").append(ChallengeCode.encodeBalancedV3(13)).append("`; source G")
                .append(BalancedCatalog.info(13, false).sourceNumber).append("; progression version ").append(level.progressionVersion).append(".\n")
                .append("- Initial gate bits: ").append(level.initialGateMask).append("; pieces: ").append(level.tiles.size())
                .append("; base actions: ").append(budget.base()).append("; error/undo allowance: ").append(budget.mistakeAllowance())
                .append("; sealed cells: ").append(level.barriers.size()).append(".\n")
                .append("- Solvable with the initial action allowance, no power or purchase: **yes**.\n\n")
                .append("Coordinates below are one based; IDs remain zero based. A gate bit is UP=1, RIGHT=2, DOWN=4, LEFT=8. The exit occurs before its switch XORs the gates.\n\n")
                .append("| Tile ID | Row | Column | Exit | Switch bits |\n|---:|---:|---:|---|---:|\n");
        for (Tile tile : level.tiles) text.append('|').append(tile.id).append('|').append(tile.row + 1).append('|')
                .append(tile.col + 1).append('|').append(tile.direction).append('|').append(tile.switchMask).append("|\n");
        text.append("\n## Full exact completion\n\n| Step | Tile | Row | Column | Gates after exit | Actions left |\n|---:|---:|---:|---:|---:|---:|\n");
        for (int i = 0; i < level.solution.length; i++) {
            Tile tile = level.tile(level.solution[i]);
            check(board.move(tile.id).success && budget.consumeAction(), "J13 report completion exact");
            text.append('|').append(i + 1).append('|').append(tile.id).append('|').append(tile.row + 1).append('|')
                    .append(tile.col + 1).append('|').append(board.gateMask()).append('|').append(budget.remaining()).append("|\n");
        }
        List<Integer> trap = shortestDeadPrefix(level, true);
        check(!trap.isEmpty(), "J13 has a reachable doomed position with legal exits");
        board.restart();
        for (int id : trap) check(board.move(id).success, "J13 trap prefix is legal");
        Board.Guidance guidance = board.guidance(budget.base() - trap.size());
        text.append("\n## Smallest legal-but-doomed prefix\n\nExit tile IDs `").append(trap)
                .append("` in that order. Every exit is legal. The remaining gate mask is `").append(board.gateMask())
                .append("`; the remaining-piece bit mask is `").append(board.remainingMask()).append("`. Legal exits still visible: `");
        ArrayList<Integer> visible = new ArrayList<>(); for (Tile tile : board.legalMoves()) visible.add(tile.id);
        text.append(visible).append("`. The independent model and runtime solver both find **no completion**.\n\n")
                .append("The old `isStuck()` returns `").append(board.isStuck()).append("` here, because it checks only whether any exit is legal. That leaves a doomed run unannounced. `guidance()` now returns `")
                .append(guidance.status).append("`, no spendable hint, and `undoSteps=").append(guidance.undoSteps)
                .append("`. Recovery requires ").append(guidance.actionsToRecoverAndFinish).append(" actions including undo and restored exits; ")
                .append(guidance.remainingActions).append(" remain. Restart remains free. Extra actions alone do not reopen a gate.\n\n")
                .append("## Other board-13 identities\n\n| Family | Pieces | Initially solvable |\n|---|---:|---|\n");
        for (String family : new String[]{"C", "B", "G", "J", "R", "Q", "H", "K"}) {
            Level old;
            switch (family) {
                case "C": old = LevelCatalog.get(13); break;
                case "B": old = BalancedCatalog.legacyGet(13); break;
                case "G": old = BalancedCatalog.getV2(13); break;
                case "J": old = level; break;
                case "R": old = LevelCatalog.remix(13); break;
                case "Q": old = BalancedCatalog.legacyRemix(13); break;
                case "H": old = BalancedCatalog.remixV2(13); break;
                default: old = BalancedCatalog.remix(13);
            }
            check(new Reference(old).evaluate(old.allTilesMask), "Historical 13 initially solvable: " + family);
            text.append('|').append(family).append("13|").append(old.tiles.size()).append("|yes|\n");
        }
        return text.toString();
    }

    public static void main(String[] args) throws Exception {
        long start = System.nanoTime();
        Path folder = Paths.get(args.length > 0 ? args[0] : "docs/validation/v1.7");
        Files.createDirectories(folder);
        budgetEdges();
        String focus = journey13();
        for (boolean remix : new boolean[]{false, true}) {
            int maximum = remix ? BalancedCatalog.REMIX_COUNT : BalancedCatalog.LEVEL_COUNT;
            for (int number = 1; number <= maximum; number++) {
                verifyBoard(remix ? BalancedCatalog.remix(number) : BalancedCatalog.get(number), remix);
            }
            System.out.println("Completed " + (remix ? "Remix" : "Journey") + "; boards=" + boards + ", reachable states=" + states);
        }
        check(boards == 1240, "All Journey and Remix boards covered");
        check(deadWithLegalExits > 0, "Legal-but-doomed issue reproduced beyond immediate stuck states");
        double seconds = (System.nanoTime() - start) / 1e9;
        String csv = rows.toString();
        Files.write(folder.resolve("board-audit.csv"), csv.getBytes(StandardCharsets.UTF_8));
        Files.write(folder.resolve("board-audit-journey13.md"), focus.getBytes(StandardCharsets.UTF_8));
        String summary = "# Board reachability audit — 1.7\n\n"
                + "All 1,000 frozen Journey J boards and 240 frozen Remix K boards are initially solvable within their existing action budgets. No geometry, seed, certificate, challenge identity or budget allowance was changed.\n\n"
                + "- Boards: " + boards + ".\n- Reachable piece subsets checked: " + states + ".\n"
                + "- Legal state transitions checked: " + legalEdges + ".\n- Losing subsets: " + losingStates + ".\n"
                + "- Losing subsets that still offer legal exits: " + deadWithLegalExits + ".\n"
                + "- Assertions: " + checks + ".\n- Runtime: " + String.format(Locale.ROOT, "%.3f", seconds) + " seconds.\n"
                + "- CSV SHA-256: `" + hex(MessageDigest.getInstance("SHA-256").digest(csv.getBytes(StandardCharsets.UTF_8))) + "`.\n\n"
                + "## Method and limits\n\n"
                + "An independent model walks each ray cell by cell, derives gates from XOR of removed pieces, and computes exact winning/losing reachability over every reachable subset. It does not use the runtime solver, Board geometry, or a certificate as an oracle. Every subset is then reached through legal Board moves. Legal exits, solver completions, safe next hints, nonmutating diagnostics, minimum undo recovery and action cost are compared with that independent model. Keys and sealed cells are included. Gates for a reachable subset are unique because XOR commutes. This makes subset enumeration exhaustive for every legal history's board state; recovery is additionally checked against the actual traversal history.\n\n"
                + "Every certificate is separately replayed within its initial fixed action budget with no power, ad, purchase or extension. Historical C/B/G/J and R/Q/H/K number13 definitions are all independently solvable. Historical code/layout/allowance fingerprints are checked separately by the runner.\n\n"
                + "Journey13 has a reproducible legal choice sequence that permanently closes an exit while other exits remain legal. The previous UI only checked `isStuck()` (zero legal exits), so that doom could go unreported. The user's exact installed version and saved history were not available, so this is a reproduced mechanism rather than proof of their particular sequence. Android presentation and integration are separate tests.\n\n"
                + "## Controller API\n\n"
                + "Call `board.guidance(moveBudget.remaining())` after any exit, undo, restart, restore or continuation, not every render frame. `-1` actions means unlimited. `Board.Guidance.status` is `SOLVED`, `PLAYABLE`, `NEEDS_ACTIONS`, or `DEAD_END`. The result provides `hintTileId`, `actionsToFinish`, `undoSteps`, `actionsToRecoverAndFinish`, and `recoveryFitsBudget`. `canUseHint()` forbids spending hint stock in a dead or already unaffordable run. `Board.isDeadEnd()` differs deliberately from the historical immediate-stuck predicate.\n\n"
                + "In `DEAD_END`, display undo recovery when it fits, plus free restart. Never imply that a rewarded ad or added actions alone repairs gate order. In `NEEDS_ACTIONS`, the layout still has an exact completion but its required exits exceed remaining actions. A hint always follows the actual remaining gates/pieces, not just the initial certificate. Cosmetic equipment must not change this solvability model.\n";
        Files.write(folder.resolve("board-audit.md"), summary.getBytes(StandardCharsets.UTF_8));
        System.out.println("PASS " + checks + " assertions; " + boards + " boards; " + states + " reachable states; "
                + deadWithLegalExits + " legal-but-doomed states; " + String.format(Locale.ROOT, "%.3f", seconds) + "s");
    }

    private static String hex(byte[] bytes) {
        StringBuilder text = new StringBuilder();
        for (byte value : bytes) text.append(String.format(Locale.ROOT, "%02x", value));
        return text.toString();
    }
}
