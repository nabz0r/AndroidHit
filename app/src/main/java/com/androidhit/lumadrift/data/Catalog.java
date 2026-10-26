package com.androidhit.lumadrift.data;

import android.graphics.Color;

/** Original, code-drawn cosmetics. All prices use coins earned by playing. */
public final class Catalog {
    public static final int LEVEL_COUNT = 240;
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
        /** Shape index: 0 star, 1 cat, 2 robot, 3 bunny, 4 comet, 5 ghost. */
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
            this.text = Color.rgb(247, 248, 255);
            this.muted = Color.rgb(174, 181, 210);
        }
    }

    public static final class BoosterItem {
        public final String id;
        public final String nameKey;
        public final String descriptionKey;
        public final int price;
        public final int amount;
        private BoosterItem(String id, int price, int amount) {
            this.id = id;
            this.nameKey = "booster_" + id;
            this.descriptionKey = "booster_" + id + "_desc";
            this.price = price;
            this.amount = amount;
        }
    }

    public static final CharacterItem[] CHARACTERS = {
            new CharacterItem("nova", 0, "#FFE29A", 0),
            new CharacterItem("miko", 160, "#FF94C2", 1),
            new CharacterItem("orbit", 220, "#8DEBDD", 2),
            new CharacterItem("pip", 300, "#CBB0FF", 3),
            new CharacterItem("astra", 420, "#8DC8FF", 4),
            new CharacterItem("zig", 600, "#DAF79D", 5)
    };

    public static final SkinItem[] SKINS = {
            new SkinItem("lumina", 0, "#11142B", "#232744", "#A99AFF", "#7EE8D1"),
            new SkinItem("lagoon", 180, "#081F2A", "#123745", "#70E1CF", "#A5CCFF"),
            new SkinItem("blossom", 240, "#291429", "#46223F", "#FF9CC7", "#E2B0FF"),
            new SkinItem("ember", 300, "#281A1C", "#442B2B", "#FFBB81", "#FF8A9F"),
            new SkinItem("midnight", 360, "#0B1229", "#192747", "#8AB9FF", "#D1B0FF"),
            new SkinItem("aurora", 450, "#152120", "#263C36", "#C1EE9C", "#86DFC2")
    };

    public static final BoosterItem[] BOOSTERS = {
            new BoosterItem(HINT, 45, 3),
            new BoosterItem(VISION, 40, 3),
            new BoosterItem(FLOW, 60, 2)
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
}
