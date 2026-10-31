package com.androidhit.lumadrift.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Original wardrobe: every item changes appearance only. No gameplay or cash value. */
public final class CosmeticCatalog {
    public enum Slot { HAT, DRESS, SHOES, GLOVES }
    public enum Rarity { COMMON, RARE, EPIC }
    public enum Source { STARTER, LOGIN_DAYS, WEEKLY_WINS, EVENT_STARS, EARNED_COINS }
    public static final int LOOT_PRICE_COINS = 150;
    public static final class Item {
        public final String id, nameKey;
        public final Slot slot;
        public final Rarity rarity;
        public final Source source;
        public final int requirement, priceCoins;
        private Item(String id, Slot slot, Rarity rarity, Source source, int requirement) {
            this.id=id; nameKey="cosmetic_"+id; this.slot=slot; this.rarity=rarity; this.source=source;
            this.requirement=requirement; priceCoins=source==Source.EARNED_COINS?requirement:0;
        }
        /** Public unconditional chance for one earned-coin capsule, in 1/10000 units. */
        public int chanceBasisPoints() { return rarity==Rarity.COMMON?750:rarity==Rarity.RARE?375:125; }
        public int duplicateCoins() { return rarity==Rarity.COMMON?25:rarity==Rarity.RARE?60:100; }
    }
    public static final List<Item> ITEMS=Collections.unmodifiableList(Arrays.asList(
        new Item("hat_dew",Slot.HAT,Rarity.COMMON,Source.STARTER,0),
        new Item("hat_moon",Slot.HAT,Rarity.COMMON,Source.LOGIN_DAYS,2),
        new Item("hat_star",Slot.HAT,Rarity.RARE,Source.WEEKLY_WINS,2),
        new Item("hat_pumpkin",Slot.HAT,Rarity.RARE,Source.EVENT_STARS,12),
        new Item("hat_cupid",Slot.HAT,Rarity.EPIC,Source.EARNED_COINS,450),
        new Item("hat_aurora",Slot.HAT,Rarity.EPIC,Source.EARNED_COINS,650),
        new Item("dress_dew",Slot.DRESS,Rarity.COMMON,Source.STARTER,0),
        new Item("dress_moon",Slot.DRESS,Rarity.COMMON,Source.LOGIN_DAYS,4),
        new Item("dress_star",Slot.DRESS,Rarity.RARE,Source.WEEKLY_WINS,4),
        new Item("dress_valentine",Slot.DRESS,Rarity.RARE,Source.EVENT_STARS,18),
        new Item("dress_witch",Slot.DRESS,Rarity.EPIC,Source.EARNED_COINS,500),
        new Item("dress_aurora",Slot.DRESS,Rarity.EPIC,Source.EARNED_COINS,700),
        new Item("shoes_dew",Slot.SHOES,Rarity.COMMON,Source.STARTER,0),
        new Item("shoes_moon",Slot.SHOES,Rarity.COMMON,Source.LOGIN_DAYS,7),
        new Item("shoes_star",Slot.SHOES,Rarity.RARE,Source.WEEKLY_WINS,6),
        new Item("shoes_pumpkin",Slot.SHOES,Rarity.RARE,Source.EVENT_STARS,24),
        new Item("shoes_cupid",Slot.SHOES,Rarity.EPIC,Source.EARNED_COINS,400),
        new Item("shoes_aurora",Slot.SHOES,Rarity.EPIC,Source.EARNED_COINS,600),
        new Item("gloves_dew",Slot.GLOVES,Rarity.COMMON,Source.STARTER,0),
        new Item("gloves_moon",Slot.GLOVES,Rarity.COMMON,Source.LOGIN_DAYS,10),
        new Item("gloves_star",Slot.GLOVES,Rarity.RARE,Source.WEEKLY_WINS,8),
        new Item("gloves_valentine",Slot.GLOVES,Rarity.RARE,Source.EVENT_STARS,30),
        new Item("gloves_witch",Slot.GLOVES,Rarity.EPIC,Source.EARNED_COINS,400),
        new Item("gloves_aurora",Slot.GLOVES,Rarity.EPIC,Source.EARNED_COINS,600)
    ));
    private CosmeticCatalog() { }
    public static Item find(String id) { for(Item item:ITEMS)if(item.id.equals(id))return item;return null; }
    public static Item starter(Slot slot) { for(Item item:ITEMS)if(item.slot==slot&&item.source==Source.STARTER)return item;return null; }
    /** A saved per-device seed and stable receipt produce the same outcome on every retry. */
    public static Item draw(String seed,String requestId) {
        if(seed==null||seed.isEmpty()||requestId==null||requestId.isEmpty())throw new IllegalArgumentException("Missing capsule identity");
        byte[] bytes; long value; int retry=0;
        do {
            bytes=digest(seed+":"+requestId+":"+retry++);
            value=((long)(bytes[0]&255)<<24)|((long)(bytes[1]&255)<<16)|((long)(bytes[2]&255)<<8)|(bytes[3]&255L);
        } while(value>=4_294_960_000L);
        int roll=(int)(value%10_000), selected=bytes[4]&7;
        Rarity rarity=roll<6000?Rarity.COMMON:roll<9000?Rarity.RARE:Rarity.EPIC;
        for(Item item:ITEMS)if(item.rarity==rarity&&selected--==0)return item;
        throw new IllegalStateException("Invalid capsule pool");
    }
    private static byte[] digest(String value) {
        try { return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); }
        catch(NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
}
