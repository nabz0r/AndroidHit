package com.androidhit.lumadrift.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Version 1.16 earned-coin collection banners, separate from legacy clothing capsules. */
public final class SummonCatalog {
    public static final String VERSION="v116_1";
    public enum Banner { AVATARS, SIGNATURES }
    public enum Kind { AVATAR, OUTFIT }
    public enum Rarity { COMMON, RARE, EPIC }
    public static final class Item {
        public final String id, nameKey, avatarId, outfitId;
        public final Kind kind;
        public final Rarity rarity;
        public final int chanceBasisPoints, duplicateCoins;
        private Item(String id,Kind kind,Rarity rarity,int chance,int duplicate) {
            this.id=id;this.kind=kind;this.rarity=rarity;chanceBasisPoints=chance;duplicateCoins=duplicate;
            avatarId=kind==Kind.AVATAR?id:"";outfitId=kind==Kind.OUTFIT?id:"";
            nameKey=kind==Kind.AVATAR?Catalog.character(id).nameKey:SignatureOutfits.find(id).nameKey;
        }
    }
    private static final List<Item> AVATARS;
    private static final List<Item> SIGNATURES;
    static {
        List<Item> avatars=new ArrayList<>();
        // Nova is the free starting companion and is explicitly excluded from this banner.
        // Explicit IDs keep this receipt version stable when the direct-purchase catalog expands.
        String[] avatarIds={"miko","orbit","pip","astra","zig","lune","fenn","hoot","buzz","tula",
                "jelli","vela","nimbi","soli","mochi","nixi","prickly","quill","waddles","halo","emberling","bambu","inky"};
        for(int index=0;index<avatarIds.length;index++) {
            Rarity rarity=index<10?Rarity.COMMON:index<18?Rarity.RARE:Rarity.EPIC;
            avatars.add(new Item(avatarIds[index],Kind.AVATAR,rarity,
                    index<10?600:index<18?375:200,index<10?45:index<18?75:120));
        }
        AVATARS=Collections.unmodifiableList(avatars);
        List<Item> outfits=new ArrayList<>();
        String[] outfitIds={"moon","star","harvest","heart","aurora"};
        int[] chances={4000,2500,1500,1200,800};
        for(int index=0;index<outfitIds.length;index++)outfits.add(new Item(
                outfitIds[index],Kind.OUTFIT,index==0?Rarity.COMMON:index<=2?Rarity.RARE:Rarity.EPIC,
                chances[index],index==0?65:index<=2?100:160));
        SIGNATURES=Collections.unmodifiableList(outfits);
        validatePool(AVATARS);validatePool(SIGNATURES);
    }
    private SummonCatalog() { }
    public static int priceCoins(Banner banner) { return banner==null?0:banner==Banner.AVATARS?180:260; }
    public static List<Item> items(Banner banner) {
        return banner==null?Collections.emptyList():banner==Banner.AVATARS?AVATARS:SIGNATURES;
    }
    public static Item find(Banner banner,String id) { for(Item item:items(banner))if(item.id.equals(id))return item;return null; }
    public static Item find(String id) { Item item=find(Banner.AVATARS,id);return item==null?find(Banner.SIGNATURES,id):item; }
    private static void validatePool(List<Item> pool) {
        int total=0;for(Item item:pool)total+=item.chanceBasisPoints;
        if(total!=10000)throw new IllegalStateException("Invalid versioned summon odds");
    }
    /** Fixed unconditional odds, no changing pity state and no relationship to real payments. */
    public static Item draw(String seed,Banner banner,String requestId) {
        if(seed==null||seed.isEmpty()||banner==null||requestId==null||requestId.isEmpty())throw new IllegalArgumentException("Missing summon identity");
        long value;int retry=0;
        do {
            byte[] bytes=digest(VERSION+":"+seed+":"+banner.name()+":"+requestId+":"+retry++);
            value=((long)(bytes[0]&255)<<24)|((long)(bytes[1]&255)<<16)|((long)(bytes[2]&255)<<8)|(bytes[3]&255L);
        }while(value>=4_294_960_000L);
        int roll=(int)(value%10_000);
        for(Item item:items(banner)){roll-=item.chanceBasisPoints;if(roll<0)return item;}
        throw new IllegalStateException("Invalid summon pool");
    }
    private static byte[] digest(String value) {
        try{return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));}
        catch(NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
    }
}
