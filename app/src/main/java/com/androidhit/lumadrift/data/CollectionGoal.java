package com.androidhit.lumadrift.data;

import java.util.Objects;

/** Read-only view of one explicitly pinned local catalog item. No unlock or equip occurs here. */
public final class CollectionGoal {
    public enum Kind { COSMETIC, GEAR }
    public enum Slot { HAT, DRESS, SHOES, GLOVES, HEAD, AURA, CHARM }
    public enum Route { JOURNEY, DAILY, WEEKLY, REWARDS, SHOP }

    public final String key, id, nameKey, slotKey, acquisitionSource;
    public final Kind kind;
    public final Slot slot;
    /** Primary acquisition activity; source is an alias retained for simple UI callers. */
    public final Route route, source;
    public final boolean owned, equipped, completed, canAfford;
    public final int progress, target, priceCoins;

    private CollectionGoal(String key, String id, String nameKey, Kind kind, Slot slot, String slotKey,
                           Route route, boolean owned, boolean equipped, int progress, int target,
                           int priceCoins, int coins, String acquisitionSource) {
        this.key=key; this.id=id; this.nameKey=nameKey; this.kind=kind; this.slot=slot; this.slotKey=slotKey;
        this.route=route; this.source=route; this.owned=owned; this.equipped=equipped; this.completed=owned;
        this.target=Math.max(0,target); this.progress=Math.min(this.target,Math.max(0,progress));
        this.priceCoins=Math.max(0,priceCoins); this.canAfford=!owned&&priceCoins>0&&coins>=priceCoins;
        this.acquisitionSource=acquisitionSource==null?"":acquisitionSource;
    }

    /** Catalog identity only. Empty means no goal and is cleared through the explicit clear API. */
    public static boolean validKey(String key) {
        if(key==null)return false;
        if(key.startsWith("cosmetic:"))return CosmeticCatalog.find(key.substring(9))!=null;
        return key.startsWith("gear:")&&AvatarGearCatalog.find(key.substring(5))!=null;
    }

    /** Null means the player has not chosen a valid goal. Completing it does not clear or equip it. */
    public static CollectionGoal snapshot(GameStore store) {
        Objects.requireNonNull(store,"store");
        String key=store.trackedCollectionGoal();
        if(key.isEmpty())return null;
        if(key.startsWith("cosmetic:")) {
            CosmeticCatalog.Item item=CosmeticCatalog.find(key.substring(9));
            boolean owned=store.ownsCosmetic(item.id),equipped=item.id.equals(store.equippedCosmetic(item.slot));
            Route route=item.source==CosmeticCatalog.Source.WEEKLY_WINS?Route.WEEKLY:
                    item.source==CosmeticCatalog.Source.EVENT_STARS?Route.DAILY:
                    item.source==CosmeticCatalog.Source.EARNED_COINS?Route.SHOP:Route.REWARDS;
            int coins=item.priceCoins>0?store.coins():0;
            int progress=item.priceCoins>0?coins:store.cosmeticProgress(item.id);
            String slotKey=item.slot.name().toLowerCase(java.util.Locale.ROOT);
            return new CollectionGoal(key,item.id,item.nameKey,Kind.COSMETIC,Slot.valueOf(item.slot.name()),slotKey,
                    route,owned,equipped,progress,item.requirement,item.priceCoins,coins,store.cosmeticAcquisitionSource(item.id));
        }
        AvatarGearCatalog.Item item=AvatarGearCatalog.find(key.substring(5));
        boolean owned=store.ownsAvatarGear(item.id),equipped=item.id.equals(store.avatarLoadout().item(item.slot).id);
        Route route=item.source==AvatarGearCatalog.Source.JOURNEY_CLEARS?Route.JOURNEY:
                item.source==AvatarGearCatalog.Source.WEEKLY_WINS?Route.WEEKLY:
                item.source==AvatarGearCatalog.Source.EVENT_STARS?Route.DAILY:
                item.source==AvatarGearCatalog.Source.EARNED_COINS?Route.SHOP:Route.REWARDS;
        int coins=item.priceCoins>0?store.coins():0;
        int progress=item.priceCoins>0?coins:store.avatarGearProgress().value(item.source);
        return new CollectionGoal(key,item.id,item.nameKey,Kind.GEAR,Slot.valueOf(item.slot.name()),item.slot.key(),
                route,owned,equipped,progress,item.requirement,item.priceCoins,coins,store.avatarGearAcquisitionSource(item.id));
    }
}
