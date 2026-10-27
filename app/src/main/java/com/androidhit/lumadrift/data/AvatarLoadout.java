package com.androidhit.lumadrift.data;

/** One validated item per slot; benefits never sum across owned inventory. */
public final class AvatarLoadout {
    public final String headId, auraId, charmId;
    public AvatarLoadout(String headId,String auraId,String charmId) {
        this.headId=validated(headId,AvatarGearCatalog.Slot.HEAD);
        this.auraId=validated(auraId,AvatarGearCatalog.Slot.AURA);
        this.charmId=validated(charmId,AvatarGearCatalog.Slot.CHARM);
    }
    private static String validated(String id,AvatarGearCatalog.Slot slot) {
        AvatarGearCatalog.Item item=AvatarGearCatalog.find(id);
        return item!=null&&item.slot==slot?item.id:AvatarGearCatalog.starter(slot).id;
    }
    public AvatarGearCatalog.Item item(AvatarGearCatalog.Slot slot) {
        if(slot==null)return null;
        return AvatarGearCatalog.find(slot==AvatarGearCatalog.Slot.HEAD?headId:slot==AvatarGearCatalog.Slot.AURA?auraId:charmId);
    }
    public int winBonusCoins() { return item(AvatarGearCatalog.Slot.HEAD).winBonusCoins; }
    public int packDiscountCoins() { return item(AvatarGearCatalog.Slot.AURA).packDiscountCoins; }
    public int blockedGracePerRun() { return item(AvatarGearCatalog.Slot.CHARM).blockedGrace; }
    public int discountedPackPrice(int basePrice) { return basePrice<=0?0:Math.max(1,basePrice-packDiscountCoins()); }
}
