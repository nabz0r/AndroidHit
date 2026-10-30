package com.androidhit.lumadrift.engine;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.CRC32;

/**
 * Small, versioned board identifiers. A checksum catches copying mistakes; it is
 * not authentication. No player identity, score, reward or save travels in a code.
 */
public final class ChallengeCode {
    public enum Kind { CAMPAIGN, REMIX, EVENT, BALANCED, BALANCED_REMIX, BALANCED_V2, BALANCED_REMIX_V2, SEALED,
        BALANCED_V3, BALANCED_REMIX_V3, STRATEGY, STRATEGY_REMIX, STRATEGY_EVENT, LEARNING, RELAY }
    /** Full nonnegative Java long range; no floating-point conversion occurs. */
    public static final long MAX_EVENT_SEED = Long.MAX_VALUE;
    public final Kind kind;
    public final long value;

    private ChallengeCode(Kind kind, long value) { this.kind = kind; this.value = value; }

    public static String encodeCampaign(int number) {
        return encode(Kind.CAMPAIGN, number);
    }

    public static String encodeRemix(int number) {
        return encode(Kind.REMIX, number);
    }

    public static String encodeBalanced(int number){return encode(Kind.BALANCED,number);}
    public static String encodeBalancedRemix(int number){return encode(Kind.BALANCED_REMIX,number);}
    public static String encodeBalancedV2(int number){return encode(Kind.BALANCED_V2,number);}
    public static String encodeBalancedRemixV2(int number){return encode(Kind.BALANCED_REMIX_V2,number);}
    public static String encodeBalancedV3(int number){return encode(Kind.BALANCED_V3,number);}
    public static String encodeBalancedRemixV3(int number){return encode(Kind.BALANCED_REMIX_V3,number);}
    public static String encodeSealed(int number){return encode(Kind.SEALED,number);}
    public static String encodeStrategy(int number){return encode(Kind.STRATEGY,number);}
    public static String encodeStrategyRemix(int number){return encode(Kind.STRATEGY_REMIX,number);}
    public static String encodeStrategyEvent(long seed){return encode(Kind.STRATEGY_EVENT,seed);}
    public static String encodeLearning(int number){return encode(Kind.LEARNING,number);}
    public static String encodeRelay(int number){return encode(Kind.RELAY,number);}

    public static String encodeEvent(long seed) { return encode(Kind.EVENT, seed); }

    /** Strict canonical format. Invalid input returns null and never throws. */
    public static ChallengeCode parse(String code) {
        if (code == null || code.length() < 16 || code.length() > 28) return null;
        if (!code.startsWith("LD1-")) return null;
        Kind kind;
        switch (code.charAt(4)) {
            case 'C': kind = Kind.CAMPAIGN; break;
            case 'R': kind = Kind.REMIX; break;
            case 'E': kind = Kind.EVENT; break;
            case 'B': kind = Kind.BALANCED; break;
            case 'Q': kind = Kind.BALANCED_REMIX; break;
            case 'G': kind = Kind.BALANCED_V2; break;
            case 'H': kind = Kind.BALANCED_REMIX_V2; break;
            case 'P': kind = Kind.SEALED; break;
            case 'J': kind = Kind.BALANCED_V3; break;
            case 'K': kind = Kind.BALANCED_REMIX_V3; break;
            case 'L': kind = Kind.STRATEGY; break;
            case 'M': kind = Kind.STRATEGY_REMIX; break;
            case 'T': kind = Kind.STRATEGY_EVENT; break;
            case 'N': kind = Kind.LEARNING; break;
            case 'U': kind = Kind.RELAY; break;
            default: return null;
        }
        if (code.charAt(5) != '-') return null;
        int dash = code.length() - 9;
        if (dash <= 6 || code.charAt(dash) != '-') return null;
        String digits = code.substring(6, dash);
        if (digits.length() > 13 || (digits.length() > 1 && digits.charAt(0) == '0')) return null;
        long value = 0;
        for (int i = 0; i < digits.length(); i++) {
            char c = digits.charAt(i);
            int digit = c >= '0' && c <= '9' ? c - '0' : c >= 'A' && c <= 'Z' ? c - 'A' + 10 : -1;
            if (digit < 0 || value > (MAX_EVENT_SEED - digit) / 36L) return null;
            value = value * 36L + digit;
        }
        if (!valid(kind, value)) return null;
        String checksum = code.substring(dash + 1);
        for (int i = 0; i < checksum.length(); i++) {
            char c = checksum.charAt(i);
            if (!(c >= '0' && c <= '9') && !(c >= 'A' && c <= 'F')) return null;
        }
        return checksum(code.substring(0, dash)).equals(checksum) ? new ChallengeCode(kind, value) : null;
    }

    private static String encode(Kind kind, long value) {
        if (!valid(kind, value)) throw new IllegalArgumentException("Challenge value outside supported range");
        char family = kind == Kind.CAMPAIGN ? 'C' : kind == Kind.REMIX ? 'R' : kind == Kind.BALANCED ? 'B'
                : kind == Kind.BALANCED_REMIX ? 'Q' : kind == Kind.BALANCED_V2 ? 'G' : kind == Kind.BALANCED_REMIX_V2 ? 'H'
                : kind == Kind.SEALED ? 'P' : kind == Kind.BALANCED_V3 ? 'J' : kind == Kind.BALANCED_REMIX_V3 ? 'K'
                : kind == Kind.STRATEGY ? 'L' : kind == Kind.STRATEGY_REMIX ? 'M' : kind == Kind.STRATEGY_EVENT ? 'T' : kind == Kind.LEARNING ? 'N' : kind == Kind.RELAY ? 'U' : 'E';
        String body = "LD1-" + family + "-" + Long.toString(value, 36).toUpperCase(Locale.ROOT);
        return body + "-" + checksum(body);
    }

    private static boolean valid(Kind kind, long value) {
        if(kind == Kind.EVENT || kind == Kind.STRATEGY_EVENT) return value >= 0 && value <= MAX_EVENT_SEED;
        int maximum = kind == Kind.SEALED ? SealedCatalog.COUNT : kind == Kind.RELAY ? RelayCatalog.LEVEL_COUNT : kind == Kind.LEARNING ? LearningCatalog.LEVEL_COUNT : kind == Kind.BALANCED_V2 || kind == Kind.BALANCED_V3 || kind==Kind.STRATEGY ? BalancedCatalog.LEVEL_COUNT
                : kind == Kind.BALANCED_REMIX_V2 || kind == Kind.BALANCED_REMIX_V3 || kind==Kind.STRATEGY_REMIX ? BalancedCatalog.REMIX_COUNT
                : kind == Kind.CAMPAIGN ? LevelCatalog.LEVEL_COUNT : LevelCatalog.REMIX_COUNT;
        return value >= 1 && value <= maximum;
    }

    /** Resolves every historical code to its original content, never to a current reorder. */
    public Level resolve() {
        int number = (int) value;
        switch (kind) {
            case CAMPAIGN: return LevelCatalog.get(number);
            case REMIX: return LevelCatalog.remix(number);
            case BALANCED: return BalancedCatalog.legacyGet(number);
            case BALANCED_REMIX: return BalancedCatalog.legacyRemix(number);
            case BALANCED_V2: return BalancedCatalog.getV2(number);
            case BALANCED_REMIX_V2: return BalancedCatalog.remixV2(number);
            case BALANCED_V3: return BalancedCatalog.get(number);
            case BALANCED_REMIX_V3: return BalancedCatalog.remix(number);
            case STRATEGY: return StrategyCatalog.get(number);
            case STRATEGY_REMIX: return StrategyCatalog.remix(number);
            case STRATEGY_EVENT: return StrategyCatalog.event(value);
            case LEARNING: return LearningCatalog.get(number);
            case RELAY: return RelayCatalog.get(number);
            case SEALED: return SealedCatalog.get(number);
            default: return LevelCatalog.event(value);
        }
    }

    private static String checksum(String body) {
        CRC32 crc = new CRC32();
        crc.update(body.getBytes(StandardCharsets.US_ASCII));
        return String.format(Locale.ROOT, "%08X", crc.getValue());
    }
}
