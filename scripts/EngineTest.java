import com.androidhit.lumadrift.engine.Board;
import com.androidhit.lumadrift.engine.ChallengeCode;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.zip.CRC32;

/** Dependency-free, repeatable correctness checks; run with scripts/test-engine.sh. */
public final class EngineTest {
    private static int assertions;
    private static int alternatives;
    private static int unsafeAlternatives;

    public static void main(String[] args) {
        long started = System.nanoTime();
        fixtures();
        challengeCodes();
        for(int n=1;n<=240;n++){
            roundtrip(ChallengeCode.encodeBalanced(n),ChallengeCode.Kind.BALANCED,n);
            roundtrip(ChallengeCode.encodeBalancedRemix(n),ChallengeCode.Kind.BALANCED_REMIX,n);
        }
        expectIllegal(()->ChallengeCode.encodeBalanced(0),"Balanced lower bound");
        expectIllegal(()->ChallengeCode.encodeBalancedRemix(241),"Balanced remix upper bound");
        check(contentDigest(false).equals("7cdbb8a4d94fe8ddb468a2ade2fa214b258cedd6bc5cbe24a74ee7c029b2893a"),
                "All 240 original board layouts remain v1.0 identical");
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
        remix();
        System.out.println("PASS: 240 campaign + 240 remix + 1002 event + 1002 paired-switch seeds; " + assertions
                + " assertions, " + alternatives + " alternate legal starts (" + unsafeAlternatives
                + " correctly require undo), " + String.format(java.util.Locale.ROOT, "%.2f", (System.nanoTime() - started) / 1e9) + " s.");
    }

    private static void remix() {
        check(contentDigest(true).equals("9839d4f6fcf785e14f6a8e66c368a2a797c8d77a1594813d56735c1ddab35208"),
                "All 240 LD1 remix identifiers remain board-stable");
        Set<String> boards = new HashSet<String>();
        Metrics total = new Metrics();
        System.out.println("Remix design: level,tiles,switches,paired,initial choices,unsafe choices,path blockers,closed gates,ray dependencies,ray depth");
        for (int number = 1; number <= LevelCatalog.REMIX_COUNT; number++) {
            Level level = LevelCatalog.remix(number);
            check(level.number == number && !level.isEvent, "Separate remix identity");
            check(level.chapter == (number - 1) / 30 + 1, "Remix chapter numbering");
            check(boards.add(signature(level)), "Unique remix layout " + number);
            check(level.size == (number <= 60 ? 5 : 6), "Remix board-size pacing");
            check(level.tiles.size() >= 10 && level.tiles.size() <= 18, "Remix content bound");
            Metrics m = metrics(level);
            check(m.paired >= 1, "Every remix teaches paired gates");
            check(m.dependencies >= 3 && m.depth >= 2, "Real ray ordering dependencies");
            if (number <= 6) check(m.unsafe == 0, "Opening teaching boards have safe initial choices");
            if (number <= 3) check(m.switches == 1 && m.paired == 1, "One paired rule at a time");
            if (number <= 12) System.out.println(number + "," + m.csv());
            total.add(m);
            verify(level, true);
        }
        for (int i = 0; i < 1000; i++) {
            long seed = i < 500 ? 20261000L + i : new Random(i * 73L).nextLong();
            Level level = LevelCatalog.remixEvent(seed);
            check(level.isEvent && level.seed == seed && level.chapter == 0, "Paired seed identity");
            Metrics m = metrics(level);
            check(m.paired == 4 && m.switches == 5, "Paired seed composition");
            verify(level, i % 20 == 0);
            if (i % 10 == 0) check(signature(level).equals(signature(LevelCatalog.remixEvent(seed))), "Stable paired seed");
        }
        verify(LevelCatalog.remixEvent(Long.MIN_VALUE), true);
        verify(LevelCatalog.remixEvent(Long.MAX_VALUE), true);
        System.out.println("240 remix averages: " + total.average(240));
        expectIllegal(() -> LevelCatalog.remix(0), "Remix lower bound");
        expectIllegal(() -> LevelCatalog.remix(241), "Remix upper bound");
    }

    private static Metrics metrics(Level level) {
        Metrics m = new Metrics();
        m.tiles = level.tiles.size();
        Board board = new Board(level);
        m.legal = board.legalMoves().size();
        int[] depth = new int[m.tiles];
        for (Tile tile : level.tiles) {
            if (board.blockingTile(tile.id) >= 0) m.blocked++;
            if (!board.isGateOpen(tile.direction)) m.closed++;
            if (tile.isSwitch()) m.switches++;
            if (Integer.bitCount(tile.switchMask) == 2) m.paired++;
            check(Integer.bitCount(tile.switchMask) <= 2, "No opaque all-four switch");
        }
        for (Tile tile : board.legalMoves()) if (!board.isSafeMove(tile.id)) m.unsafe++;
        // Certified order makes every spatial blocker earlier in the dependency DAG.
        for (int id : level.solution) {
            Tile tile = level.tile(id);
            for (Tile other : level.tiles) {
                if (other.id == tile.id) continue;
                boolean onRay = tile.direction.dr == 0
                        ? tile.row == other.row && (other.col - tile.col) * tile.direction.dc > 0
                        : tile.col == other.col && (other.row - tile.row) * tile.direction.dr > 0;
                if (onRay) {
                    check(other.id < tile.id, "Dependencies follow certified order");
                    m.dependencies++;
                    depth[id] = Math.max(depth[id], depth[other.id] + 1);
                }
            }
            m.depth = Math.max(m.depth, depth[id]);
        }
        return m;
    }

    private static final class Metrics {
        int tiles, switches, paired, legal, unsafe, blocked, closed, dependencies, depth;
        String csv() { return tiles + "," + switches + "," + paired + "," + legal + "," + unsafe
                + "," + blocked + "," + closed + "," + dependencies + "," + depth; }
        void add(Metrics m) {
            tiles += m.tiles; switches += m.switches; paired += m.paired; legal += m.legal;
            unsafe += m.unsafe; blocked += m.blocked; closed += m.closed;
            dependencies += m.dependencies; depth += m.depth;
        }
        String average(int count) {
            return String.format(Locale.ROOT, "%.2f tiles, %.2f switches (%.2f paired), %.2f legal / %.2f unsafe starts, %.2f blocked tiles, %.2f closed-gate tiles, %.2f ray dependencies, %.2f ray depth",
                    tiles / (double) count, switches / (double) count, paired / (double) count,
                    legal / (double) count, unsafe / (double) count, blocked / (double) count,
                    closed / (double) count, dependencies / (double) count, depth / (double) count);
        }
    }

    private static String contentDigest(boolean remix) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (int number = 1; number <= LevelCatalog.LEVEL_COUNT; number++) {
                Level level = remix ? LevelCatalog.remix(number) : LevelCatalog.get(number);
                StringBuilder row = new StringBuilder().append(number).append(':').append(level.size).append(':').append(level.initialGateMask);
                for (Tile tile : level.tiles) row.append('|').append(tile.id).append(',').append(tile.row)
                        .append(',').append(tile.col).append(',').append(tile.direction.ordinal()).append(',').append(tile.switchMask);
                digest.update(row.toString().getBytes(StandardCharsets.UTF_8));
            }
            StringBuilder hex = new StringBuilder();
            for (byte b : digest.digest()) hex.append(String.format(Locale.ROOT, "%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) { throw new AssertionError(exception); }
    }

    private static void challengeCodes() {
        for (int number = 1; number <= 240; number++) {
            roundtrip(ChallengeCode.encodeCampaign(number), ChallengeCode.Kind.CAMPAIGN, number);
            roundtrip(ChallengeCode.encodeRemix(number), ChallengeCode.Kind.REMIX, number);
        }
        long[] edges = {0, 1, 35, 36, 20261026, 3522558831355441801L, Long.MAX_VALUE};
        for (long seed : edges) roundtrip(ChallengeCode.encodeEvent(seed), ChallengeCode.Kind.EVENT, seed);
        for (int i = 0; i < 1000; i++) {
            long seed = new Random(i * 41L).nextLong() & Long.MAX_VALUE;
            String code = ChallengeCode.encodeEvent(seed);
            roundtrip(code, ChallengeCode.Kind.EVENT, seed);
            char last = code.charAt(code.length() - 1);
            check(ChallengeCode.parse(code.substring(0, code.length() - 1) + (last == '0' ? '1' : '0')) == null,
                    "Checksum detects altered code");
        }
        String valid = ChallengeCode.encodeCampaign(1);
        String[] malformed = {null, "", "LD1", " " + valid, valid + " ", valid.toLowerCase(Locale.ROOT),
                valid.replace("LD1", "LD2"), valid.replace("-C-", "-Z-"), valid + "-x",
                withChecksum("LD1-C-0"), withChecksum("LD1-C-6P"), withChecksum("LD1-R-6P"),
                withChecksum("LD1-C-01"), withChecksum("LD1-E-3W5E11264SGSG"),
                withChecksum("LD1-E--1"), withChecksum("LD1-E-+1"), withChecksum("LD1-E-1.0"),
                withChecksum("LD1-E-∞"), "lumadrift://challenge/" + valid,
                "LD1-E-" + "9".repeat(10000) + "-12345678"};
        for (String code : malformed) check(ChallengeCode.parse(code) == null, "Malformed challenge rejected");
        for (int i = 0; i < valid.length(); i++) {
            String altered = valid.substring(0, i) + (valid.charAt(i) == 'A' ? 'B' : 'A') + valid.substring(i + 1);
            check(ChallengeCode.parse(altered) == null, "Every single-character tamper rejected");
        }
        expectIllegal(() -> ChallengeCode.encodeCampaign(0), "Campaign challenge lower bound");
        expectIllegal(() -> ChallengeCode.encodeCampaign(241), "Campaign challenge upper bound");
        expectIllegal(() -> ChallengeCode.encodeRemix(0), "Remix challenge lower bound");
        expectIllegal(() -> ChallengeCode.encodeRemix(241), "Remix challenge upper bound");
        expectIllegal(() -> ChallengeCode.encodeEvent(-1), "Negative seed rejected");
        Random random = new Random(72732);
        for (int i = 0; i < 10000; i++) {
            int length = random.nextInt(65);
            StringBuilder fuzz = new StringBuilder();
            for (int j = 0; j < length; j++) fuzz.append((char) random.nextInt(65536));
            check(ChallengeCode.parse(fuzz.toString()) == null, "Parser robust to arbitrary input");
        }
        System.out.println("Challenge codes: campaign/remix exhaustive 1..240, 1007 event round trips including Long.MAX_VALUE; malformed/overflow/tamper + 10000 fuzz inputs rejected.");
    }

    private static void roundtrip(String code, ChallengeCode.Kind kind, long value) {
        ChallengeCode decoded = ChallengeCode.parse(code);
        check(decoded != null && decoded.kind == kind && decoded.value == value, "Challenge round trip");
    }

    private static String withChecksum(String body) {
        CRC32 crc = new CRC32();
        crc.update(body.getBytes(StandardCharsets.US_ASCII));
        return body + "-" + String.format(Locale.ROOT, "%08X", crc.getValue());
    }

    private static void expectIllegal(Runnable action, String message) {
        try { action.run(); throw new AssertionError(message); }
        catch (IllegalArgumentException expected) { check(true, message); }
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
        List<Tile> pair = Arrays.asList(new Tile(0, 0, 0, Direction.UP, Direction.RIGHT.bit | Direction.LEFT.bit),
                new Tile(1, 2, 2, Direction.LEFT, 0));
        Board exchange = new Board(new Level(1, 1, 3, Direction.UP.bit | Direction.RIGHT.bit,
                pair, new int[] {0, 1}, false, 0));
        check(!exchange.canMove(1), "Paired fixture initially closed left gate");
        Board.MoveResult result = exchange.move(0);
        check(result.success && result.gateMask == (Direction.UP.bit | Direction.LEFT.bit), "Paired switch exchanges both gates atomically");
        check(exchange.move(1).success && exchange.isSolved(), "Paired switch unlocks its dependent exit");
        check(exchange.undo() && exchange.undo() && exchange.gateMask() == 3, "Paired switch undo restores both gates");
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
