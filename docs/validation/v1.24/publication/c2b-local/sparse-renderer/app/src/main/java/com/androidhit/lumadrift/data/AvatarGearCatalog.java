package com.androidhit.lumadrift.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Original deterministic local equipment. No random drops, paid currency or cloud entitlements. */
public final class AvatarGearCatalog {
    public enum Slot { HEAD, AURA, CHARM;
        public String key() { return "gear_slot_" + name().toLowerCase(Locale.ROOT); }
    }
    public enum Rarity { COMMON, RARE, EPIC;
        public String key() { return "gear_rarity_" + name().toLowerCase(Locale.ROOT); }
    }
    public enum Source { STARTER, JOURNEY_CLEARS, WEEKLY_WINS, EVENT_STARS, EARNED_COINS;
        public String key() { return "gear_source_" + name().toLowerCase(Locale.ROOT); }
    }
    public static final class Item {
        public final String id, nameKey;
        public final Slot slot;
        public final Rarity rarity;
        public final Source source;
        /** Lifetime source threshold, or earned-coin purchase price. */
        public final int requirement, priceCoins, winBonusCoins, packDiscountCoins, blockedGrace;
        private Item(String id, Slot slot, Rarity rarity, Source source, int requirement, int effect) {
            this.id=id;nameKey="gear_"+id;this.slot=slot;this.rarity=rarity;this.source=source;this.requirement=requirement;
            priceCoins=source==Source.EARNED_COINS?requirement:0;
            winBonusCoins=slot==Slot.HEAD?Math.min(3,Math.max(0,effect)):0;
            packDiscountCoins=slot==Slot.AURA?Math.min(5,Math.max(0,effect)):0;
            blockedGrace=slot==Slot.CHARM?Math.min(1,Math.max(0,effect)):0;
        }
    }
    public static final class Progress {
        public final int journeyClears, weeklyWins, eventStars;
        public Progress(int journeyClears,int weeklyWins,int eventStars) {
            this.journeyClears=Math.max(0,Math.min(Catalog.LEVEL_COUNT,journeyClears));
            this.weeklyWins=Math.max(0,weeklyWins);this.eventStars=Math.max(0,eventStars);
        }
        public int value(Source source) {
            return source==Source.JOURNEY_CLEARS?journeyClears:source==Source.WEEKLY_WINS?weeklyWins:source==Source.EVENT_STARS?eventStars:0;
        }
    }
    public static final List<Item> ITEMS=Collections.unmodifiableList(Arrays.asList(
        new Item("moonbud",Slot.HEAD,Rarity.COMMON,Source.STARTER,0,0),
        new Item("lantern_leaf",Slot.HEAD,Rarity.COMMON,Source.JOURNEY_CLEARS,8,1),
        new Item("crescent_knot",Slot.HEAD,Rarity.RARE,Source.WEEKLY_WINS,3,2),
        new Item("tide_crown",Slot.HEAD,Rarity.RARE,Source.EVENT_STARS,18,2),
        new Item("starfold_visor",Slot.HEAD,Rarity.RARE,Source.EARNED_COINS,600,2),
        new Item("prism_antlers",Slot.HEAD,Rarity.EPIC,Source.JOURNEY_CLEARS,100,3),
        new Item("firefly_ring",Slot.AURA,Rarity.COMMON,Source.STARTER,0,0),
        new Item("mint_orbit",Slot.AURA,Rarity.COMMON,Source.JOURNEY_CLEARS,12,1),
        new Item("violet_ripple",Slot.AURA,Rarity.RARE,Source.WEEKLY_WINS,4,2),
        new Item("sunset_ribbon",Slot.AURA,Rarity.RARE,Source.EVENT_STARS,21,3),
        new Item("comet_halo",Slot.AURA,Rarity.RARE,Source.EARNED_COINS,800,4),
        new Item("aurora_bloom",Slot.AURA,Rarity.EPIC,Source.JOURNEY_CLEARS,150,5),
        new Item("pebble_heart",Slot.CHARM,Rarity.COMMON,Source.STARTER,0,0),
        new Item("dew_compass",Slot.CHARM,Rarity.COMMON,Source.JOURNEY_CLEARS,20,0),
        new Item("copper_wish",Slot.CHARM,Rarity.COMMON,Source.EARNED_COINS,450,0),
        new Item("tide_anchor",Slot.CHARM,Rarity.RARE,Source.EVENT_STARS,24,1),
        new Item("star_bell",Slot.CHARM,Rarity.RARE,Source.WEEKLY_WINS,7,1),
        new Item("moon_seed",Slot.CHARM,Rarity.EPIC,Source.EARNED_COINS,1100,1)
    ));
    private AvatarGearCatalog() { }
    public static Item find(String id) { for(Item item:ITEMS)if(item.id.equals(id))return item;return null; }
    public static Item starter(Slot slot) { for(Item item:ITEMS)if(item.slot==slot&&item.source==Source.STARTER)return item;return null; }
    public static boolean claimable(Item item,Progress progress) {
        return item!=null&&progress!=null&&item.source!=Source.EARNED_COINS
                && (item.source==Source.STARTER||progress.value(item.source)>=item.requirement);
    }
}
