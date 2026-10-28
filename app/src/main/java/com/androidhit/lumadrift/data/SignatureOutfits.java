package com.androidhit.lumadrift.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Complete visual looks assembled from original existing four-slot wardrobe designs. */
public final class SignatureOutfits {
    public static final class Outfit {
        public final String id, nameKey;
        /** Slot order follows CosmeticCatalog.Slot: hat, dress, shoes, gloves. */
        public final List<String> cosmeticIds;
        private Outfit(String id,String hat,String dress,String shoes,String gloves) {
            this.id=id;nameKey="outfit_"+id;
            cosmeticIds=Collections.unmodifiableList(Arrays.asList(hat,dress,shoes,gloves));
        }
        public String item(CosmeticCatalog.Slot slot) { return slot==null?"":cosmeticIds.get(slot.ordinal()); }
    }
    public static final List<Outfit> ITEMS=Collections.unmodifiableList(Arrays.asList(
            new Outfit("moon","hat_moon","dress_moon","shoes_moon","gloves_moon"),
            new Outfit("star","hat_star","dress_star","shoes_star","gloves_star"),
            new Outfit("harvest","hat_pumpkin","dress_witch","shoes_pumpkin","gloves_witch"),
            new Outfit("heart","hat_cupid","dress_valentine","shoes_cupid","gloves_valentine"),
            new Outfit("aurora","hat_aurora","dress_aurora","shoes_aurora","gloves_aurora")));
    private SignatureOutfits() { }
    public static Outfit find(String id) { for(Outfit outfit:ITEMS)if(outfit.id.equals(id))return outfit;return null; }
}
