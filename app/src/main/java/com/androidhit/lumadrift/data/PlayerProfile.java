package com.androidhit.lumadrift.data;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** An explicitly chosen local player card, independent from any Google identity. */
public final class PlayerProfile {
    public static final int NICKNAME_MAX_CODE_POINTS = 18;
    public static final String DEFAULT_NICKNAME = "Luma";
    public static final String DEFAULT_FLAG = "WORLD";
    public static final List<String> FLAGS = Collections.unmodifiableList(Arrays.asList(
            "WORLD", "FR", "LU", "BE", "DE", "GB", "US", "JP", "KR", "BR", "CA", "ES", "IT"));
    private PlayerProfile() { }

    /** Normalize a single visible line, retaining accents and basic emoji but discarding invisible fillers. */
    public static String normalizeNickname(String value) {
        if (value == null || value.length() > 1024) return "";
        String input = Normalizer.normalize(value, Normalizer.Form.NFC);
        StringBuilder clean = new StringBuilder();
        boolean space = false, markBase = false, visible = false;
        for (int offset = 0; offset < input.length();) {
            int cp = input.codePointAt(offset); offset += Character.charCount(cp);
            if (Character.isWhitespace(cp) || Character.isSpaceChar(cp)) { space = clean.length() > 0; markBase = false; continue; }
            int type = Character.getType(cp);
            if (Character.isISOControl(cp) || type == Character.FORMAT || type == Character.SURROGATE
                    || type == Character.PRIVATE_USE || type == Character.UNASSIGNED || invisibleFiller(cp)) continue;
            boolean mark = type == Character.NON_SPACING_MARK || type == Character.COMBINING_SPACING_MARK
                    || type == Character.ENCLOSING_MARK;
            if (mark && !markBase) continue;
            if (space) clean.append(' ');
            space = false; clean.appendCodePoint(cp);
            if (!mark) { markBase = true; visible = true; }
        }
        String result = clean.toString();
        return visible && result.codePointCount(0, result.length()) <= NICKNAME_MAX_CODE_POINTS ? result : "";
    }
    private static boolean invisibleFiller(int cp) {
        return cp == 0x034f || cp == 0x115f || cp == 0x1160 || cp == 0x17b4 || cp == 0x17b5
                || cp == 0x2800 || cp == 0x3164 || cp == 0xffa0 || cp >= 0x180b && cp <= 0x180f
                || cp >= 0xfe00 && cp <= 0xfe0f || cp >= 0xe0100 && cp <= 0xe01ef;
    }
    public static boolean validFlag(String value) { return value != null && FLAGS.contains(value); }
}
