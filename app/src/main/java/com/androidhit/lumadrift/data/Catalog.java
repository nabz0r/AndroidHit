package com.androidhit.lumadrift.data;

import android.graphics.Color;

/** Original, code-drawn cosmetics. Prices use the same coins earned by playing. */
public final class Catalog {
    public static final int LEVEL_COUNT = 1000;
    public static final int REMIX_COUNT = 240;
    public static final int DAILY_COUNT = 3;
    public static final int WEEKLY_COUNT = 7;
    public static final String HINT = "hint";
    public static final String VISION = "vision";
    public static final String FLOW = "flow";

    public static final class CharacterItem {
        public final String id;
        public final String nameKey;
        public final int price;
        public final int color;
        /** Stable shape index, 0–23; the original six IDs and prices remain unchanged. */
        public final int shape;
        private CharacterItem(String id, int price, String color, int shape) {
            this.id = id;
            this.nameKey = "character_" + id;
            this.price = price;
            this.color = Color.parseColor(color);
            this.shape = shape;
        }
    }

    public static final class SkinItem {
        public final String id;
        public final String nameKey;
        public final int price;
        public final int bg;
        public final int card;
        public final int accent;
        public final int accent2;
        public final int text;
        public final int muted;
        private SkinItem(String id, int price, String bg, String card, String accent, String accent2) {
            this.id = id;
            this.nameKey = "skin_" + id;
            this.price = price;
            this.bg = Color.parseColor(bg);
            this.card = Color.parseColor(card);
            this.accent = Color.parseColor(accent);
            this.accent2 = Color.parseColor(accent2);
            this.text = Color.rgb(64, 43, 82);
            this.muted = Color.rgb(111, 92, 125);
        }
    }

    public static final class BoosterItem {
        public final String id;
        public final String nameKey;
        public final String descriptionKey;
        public final int price;
        public final int amount;
        public final int unlockLevel;
        private BoosterItem(String id, int price, int amount, int unlockLevel) {
            this.id = id;
            this.nameKey = "booster_" + id;
            this.descriptionKey = "booster_" + id + "_desc";
            this.price = price;
            this.amount = amount;
            this.unlockLevel = unlockLevel;
        }
    }

    public static final CharacterItem[] CHARACTERS = {
            new CharacterItem("nova", 0, "#FFE29A", 0),
            new CharacterItem("miko", 160, "#FF94C2", 1),
            new CharacterItem("orbit", 220, "#8DEBDD", 2),
            new CharacterItem("pip", 300, "#CBB0FF", 3),
            new CharacterItem("astra", 420, "#8DC8FF", 4),
            new CharacterItem("zig", 600, "#DAF79D", 5),
            new CharacterItem("lune", 180, "#FBE6A1", 6),
            new CharacterItem("fenn", 240, "#FFAF85", 7),
            new CharacterItem("hoot", 280, "#CBB0E9", 8),
            new CharacterItem("buzz", 320, "#FFE084", 9),
            new CharacterItem("tula", 360, "#9BE6BF", 10),
            new CharacterItem("jelli", 400, "#B9B5FF", 11),
            new CharacterItem("vela", 440, "#E5A9DE", 12),
            new CharacterItem("nimbi", 480, "#C5E3FA", 13),
            new CharacterItem("soli", 520, "#FFC58C", 14),
            new CharacterItem("mochi", 560, "#D2B3F5", 15),
            new CharacterItem("nixi", 620, "#FFB6D6", 16),
            new CharacterItem("prickly", 680, "#ADDEA2", 17),
            new CharacterItem("quill", 740, "#DDB8A4", 18),
            new CharacterItem("waddles", 800, "#A5C6EC", 19),
            new CharacterItem("halo", 860, "#A8D9E8", 20),
            new CharacterItem("emberling", 920, "#D0B1F7", 21),
            new CharacterItem("bambu", 1000, "#D4ECE2", 22),
            new CharacterItem("inky", 1100, "#F1ABBC", 23)
    };

    public static final SkinItem[] SKINS = {
            new SkinItem("lumina", 0, "#F6EDFF", "#FFF9FF", "#7750A8", "#297B70"),
            new SkinItem("lagoon", 180, "#E5F7F5", "#F8FFFE", "#287E79", "#4B68A1"),
            new SkinItem("blossom", 240, "#FCEAF4", "#FFF8FC", "#AD467D", "#8553A8"),
            new SkinItem("ember", 300, "#FFF0E5", "#FFFAF6", "#A35D2E", "#AC4963"),
            new SkinItem("midnight", 360, "#EDEDFC", "#FAFAFF", "#4A69AA", "#79549F"),
            new SkinItem("aurora", 450, "#EBF6E7", "#FBFFF7", "#587C35", "#287D68")
    };

    public static final BoosterItem[] BOOSTERS = {
            new BoosterItem(HINT, 45, 3, 1),
            new BoosterItem(VISION, 40, 3, 5),
            new BoosterItem(FLOW, 60, 2, 12),
            new BoosterItem("shield", 70, 2, 25),
            new BoosterItem("focus", 80, 2, 45),
            new BoosterItem("trail", 90, 2, 75)
    };

    private Catalog() { }

    public static CharacterItem character(String id) {
        for (CharacterItem item : CHARACTERS) if (item.id.equals(id)) return item;
        return null;
    }

    public static SkinItem skin(String id) {
        for (SkinItem item : SKINS) if (item.id.equals(id)) return item;
        return null;
    }

    public static BoosterItem booster(String id) {
        for (BoosterItem item : BOOSTERS) if (item.id.equals(id)) return item;
        return null;
    }

    /** Current offers only. Retired definitions remain addressable for old stock and receipts. */
    public static boolean isPowerOffered(String id) {
        return booster(id) != null && !"shield".equals(id);
    }
}
