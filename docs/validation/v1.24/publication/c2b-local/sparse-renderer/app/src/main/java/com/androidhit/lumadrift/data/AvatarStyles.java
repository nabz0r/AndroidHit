package com.androidhit.lumadrift.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Free visual treatments. They never change character ownership, rewards or puzzle rules. */
public final class AvatarStyles {
    public static final String DEFAULT_ID = "plush_classic";
    public enum Family { PLUSH, COMIC, PIXEL }

    public static final class Style {
        public final String id;
        public final int index;
        public final Family family;
        public final int accent, secondary, background, ground, outline;
        private final String[] names;

        private Style(String id, int index, Family family, int accent, int secondary,
                      int background, int ground, String... names) {
            this.id = id;
            this.index = index;
            this.family = family;
            this.accent = accent;
            this.secondary = secondary;
            this.background = background;
            this.ground = ground;
            this.outline = 0xff40314f;
            this.names = names.clone();
        }

        /** FR/EN/ES/DE/PT/IT. Unknown and regional language tags have a deterministic fallback. */
        public String name(String language) {
            if (language == null) return names[1];
            if (language.startsWith("fr")) return names[0];
            if (language.startsWith("es")) return names[2];
            if (language.startsWith("de")) return names[3];
            if (language.startsWith("pt")) return names[4];
            if (language.startsWith("it")) return names[5];
            return names[1];
        }
    }

    private static final Style[] VALUES = {
        new Style(DEFAULT_ID, 0, Family.PLUSH, 0xffdf8fab, 0xfff5cc81, 0xfffff5ed, 0xffcde6c5,
                "Peluche classique", "Plush classic", "Peluche clásico", "Klassisches Plüschtier", "Pelúcia clássica", "Peluche classico"),
        new Style("cyberpunk_neon", 1, Family.COMIC, 0xff8667cd, 0xff49cbbd, 0xfff0ecff, 0xffb3bee8,
                "Néon cyberpunk", "Cyberpunk neon", "Neón ciberpunk", "Cyberpunk-Neon", "Neon cyberpunk", "Neon cyberpunk"),
        new Style("retro_arcade", 2, Family.PIXEL, 0xffd47792, 0xffe2be66, 0xfffff2d9, 0xffb1cfb1,
                "Arcade rétro", "Retro arcade", "Arcade retro", "Retro-Arcade", "Arcade retrô", "Arcade rétro"),
        new Style("modern_pixel", 3, Family.PIXEL, 0xff6c9fba, 0xffb9abdf, 0xffedf6ff, 0xffb8dad3,
                "Pixel moderne", "Modern pixel", "Píxel moderno", "Moderne Pixel", "Pixel moderno", "Pixel moderno"),
        new Style("sakura", 4, Family.PLUSH, 0xffdf8fad, 0xffa5bc82, 0xffffeef4, 0xffd0e4ba,
                "Sakura", "Sakura", "Sakura", "Sakura", "Sakura", "Sakura"),
        new Style("ocean", 5, Family.PLUSH, 0xff61a5bd, 0xffe8b791, 0xffeafaff, 0xffb6deda,
                "Océan", "Ocean", "Océano", "Ozean", "Oceano", "Oceano"),
        new Style("space", 6, Family.COMIC, 0xff8874bd, 0xffddbd69, 0xfff0edfc, 0xffc3b9df,
                "Cosmos", "Space", "Espacio", "Weltraum", "Espaço", "Spazio"),
        new Style("steampunk", 7, Family.COMIC, 0xffb88259, 0xff7caf9b, 0xfffff1df, 0xffd9c19d,
                "Steampunk", "Steampunk", "Steampunk", "Steampunk", "Steampunk", "Steampunk"),
        new Style("candy_cloud", 8, Family.PLUSH, 0xffc28fbe, 0xffe9ba83, 0xfffff0fb, 0xffded0ee,
                "Nuage sucré", "Candy cloud", "Nube de caramelo", "Zuckerwolke", "Nuvem doce", "Nuvola di caramelle"),
        new Style("halloween", 9, Family.COMIC, 0xffbe814d, 0xff9581b8, 0xfffff0e5, 0xffc5bcce,
                "Halloween", "Halloween", "Halloween", "Halloween", "Halloween", "Halloween"),
        new Style("valentine", 10, Family.PLUSH, 0xffcd7898, 0xffddbd8f, 0xffffeff4, 0xffe3c1d0,
                "Saint-Valentin", "Valentine", "San Valentín", "Valentinstag", "Dia dos Namorados", "San Valentino"),
        new Style("ice", 11, Family.COMIC, 0xff79a9c4, 0xffaaa5d3, 0xffedf9ff, 0xffc5dce7,
                "Cristal de glace", "Ice crystal", "Cristal de hielo", "Eiskristall", "Cristal de gelo", "Cristallo di ghiaccio")
    };
    private static final List<Style> ALL = Collections.unmodifiableList(Arrays.asList(VALUES));

    private AvatarStyles() { }
    public static List<Style> all() { return ALL; }

    /** Safe saved-preference lookup: unsupported IDs render the original free treatment. */
    public static Style find(String id) {
        for (Style style : VALUES) if (style.id.equals(id)) return style;
        return VALUES[0];
    }

    public static boolean isKnown(String id) {
        for (Style style : VALUES) if (style.id.equals(id)) return true;
        return false;
    }

    public static int variantCount() { return Catalog.CHARACTERS.length * VALUES.length; }

    /** Stable composite identity; it does not grant ownership of the companion. */
    public static String variantId(Catalog.CharacterItem character, String styleId) {
        String characterId = character == null ? Catalog.CHARACTERS[0].id : character.id;
        return characterId + ":" + find(styleId).id;
    }
}
