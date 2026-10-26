package com.androidhit.lumadrift.engine;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.CRC32;

/**
 * Small, versioned board identifiers. A checksum catches copying mistakes; it is
 * not authentication. No player identity, score, reward or save travels in a code.
 */
public final class ChallengeCode {
    public enum Kind { CAMPAIGN, REMIX, EVENT }
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
        char family = kind == Kind.CAMPAIGN ? 'C' : kind == Kind.REMIX ? 'R' : 'E';
        String body = "LD1-" + family + "-" + Long.toString(value, 36).toUpperCase(Locale.ROOT);
        return body + "-" + checksum(body);
    }

    private static boolean valid(Kind kind, long value) {
        return kind == Kind.EVENT ? value >= 0 && value <= MAX_EVENT_SEED
                : value >= 1 && value <= (kind == Kind.CAMPAIGN ? LevelCatalog.LEVEL_COUNT : LevelCatalog.REMIX_COUNT);
    }

    private static String checksum(String body) {
        CRC32 crc = new CRC32();
        crc.update(body.getBytes(StandardCharsets.US_ASCII));
        return String.format(Locale.ROOT, "%08X", crc.getValue());
    }
}
