package com.androidhit.lumadrift.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Read-only presentation snapshots. No unlock, purchase, equipment or stock mutation occurs here. */
public final class InventoryPresentation {
    public enum Kind { COMPANION, THEME, EQUIPMENT }
    public enum State {
        SELECTED("equipped"), OWNED("owned"), LOCKED("to_unlock");
        private final String key;
        State(String key) { this.key = key; }
        public String key() { return key; }
    }
    public enum Filter { ALL, OWNED, LOCKED }
    public enum PowerState {
        ACTIVE("power_active"), AVAILABLE("power_available"), EMPTY("power_empty"), LOCKED("power_locked");
        private final String key;
        PowerState(String key) { this.key = key; }
        public String key() { return key; }
    }
    public static final class Entry {
        public final Kind kind;
        public final String id, nameKey;
        public final State state;
        public final int priceCoins, progress, required;
        public final boolean canAfford, readyToClaim;
        /** Catalog acquisition condition, not a claim about how a cloud cosmetic was purchased. */
        public final AvatarGearCatalog.Source source;
        /** Null for companions/themes. Equipment benefits are bounded by the original catalog. */
        public final AvatarGearCatalog.Item gear;
        private Entry(Kind kind, String id, String nameKey, boolean owned, boolean selected, int price,
                      int coins, AvatarGearCatalog.Source source, int progress, int required,
                      boolean readyToClaim, AvatarGearCatalog.Item gear) {
            this.kind = kind; this.id = id; this.nameKey = nameKey; this.state = state(owned, selected);
            this.priceCoins = Math.max(0, price); this.canAfford = !owned && price > 0 && coins >= price;
            this.source = source; this.required = Math.max(0, required);
            this.progress = Math.min(this.required, Math.max(0, progress));
            this.readyToClaim = !owned && readyToClaim; this.gear = gear;
        }
        public boolean owned() { return state != State.LOCKED; }
        public boolean selected() { return state == State.SELECTED; }
        /** Cosmetics are selected; items worn in an equipment slot are equipped. */
        public String stateKey() { return selected() && kind != Kind.EQUIPMENT ? "selected" : state.key(); }
    }
    public static final class Power {
        public final String id, nameKey;
        public final int stock, unlockLevel, packAmount, packPrice;
        public final boolean unlocked, active;
        public final PowerState state;
        private Power(Catalog.BoosterItem item, int stock, boolean unlocked, boolean active, int price) {
            id=item.id; nameKey=item.nameKey; this.stock=Math.max(0,stock); unlockLevel=item.unlockLevel;
            packAmount=item.amount; packPrice=Math.max(1,price); this.unlocked=unlocked; this.active=active;
            state=powerState(this.stock,unlocked,active);
        }
        public boolean canUse() { return state == PowerState.AVAILABLE; }
    }
    public static final class Counts {
        public final int total, owned, selected, locked, readyToClaim;
        private Counts(List<Entry> entries) {
            total=entries.size(); int own=0, equipped=0, ready=0;
            for(Entry entry:entries) { if(entry.owned())own++; if(entry.selected())equipped++; if(entry.readyToClaim)ready++; }
            owned=own; selected=equipped; locked=total-own; readyToClaim=ready;
        }
    }
    /** Zero-based pages; level numbers are one-based. An empty catalog has first=last=0. */
    public static final class Page {
        public final int total, pageSize, page, pageCount, first, last;
        private Page(int total, int pageSize, int requestedPage) {
            this.total=Math.max(0,total); this.pageSize=Math.max(1,pageSize);
            pageCount=this.total==0?0:(this.total-1)/this.pageSize+1;
            page=pageCount==0?0:Math.max(0,Math.min(pageCount-1,requestedPage));
            first=pageCount==0?0:page*this.pageSize+1;
            last=pageCount==0?0:(int)Math.min((long)this.total,(long)first+this.pageSize-1);
        }
        public int size() { return first==0?0:last-first+1; }
        public boolean contains(int level) { return first>0 && level>=first && level<=last; }
        public boolean hasPrevious() { return page>0; }
        public boolean hasNext() { return page+1<pageCount; }
    }
    private InventoryPresentation() { }
    public static State state(boolean owned, boolean selected) {
        return !owned?State.LOCKED:selected?State.SELECTED:State.OWNED;
    }
    /** An effect may still be active after its final stock charge was consumed. */
    public static PowerState powerState(int stock, boolean unlocked, boolean active) {
        return active?PowerState.ACTIVE:!unlocked?PowerState.LOCKED:stock>0?PowerState.AVAILABLE:PowerState.EMPTY;
    }
    public static Entry companion(GameStore store, String id) {
        Objects.requireNonNull(store,"store"); Catalog.CharacterItem item=Catalog.character(id); if(item==null)return null;
        return new Entry(Kind.COMPANION,id,item.nameKey,store.ownsCharacter(id),id.equals(store.equippedCharacter()),
                item.price,store.coins(),item.price==0?AvatarGearCatalog.Source.STARTER:AvatarGearCatalog.Source.EARNED_COINS,0,0,false,null);
    }
    public static Entry theme(GameStore store, String id) {
        Objects.requireNonNull(store,"store"); Catalog.SkinItem item=Catalog.skin(id); if(item==null)return null;
        return new Entry(Kind.THEME,id,item.nameKey,store.ownsSkin(id),id.equals(store.equippedSkin()),
                item.price,store.coins(),item.price==0?AvatarGearCatalog.Source.STARTER:AvatarGearCatalog.Source.EARNED_COINS,0,0,false,null);
    }
    public static Entry gear(GameStore store, String id) {
        Objects.requireNonNull(store,"store"); AvatarGearCatalog.Item item=AvatarGearCatalog.find(id); if(item==null)return null;
        return gear(item,store.ownsAvatarGear(id),id.equals(store.avatarLoadout().item(item.slot).id),store.avatarGearProgress(),store.coins());
    }
    /** Pure equipment snapshot, useful when the caller has already read progression/loadout once. */
    public static Entry gear(AvatarGearCatalog.Item item, boolean owned, boolean selected,
                             AvatarGearCatalog.Progress progress, int coins) {
        if(item==null)return null;
        int value=progress==null?0:progress.value(item.source);
        return new Entry(Kind.EQUIPMENT,item.id,item.nameKey,owned,selected,item.priceCoins,coins,item.source,
                value,item.source==AvatarGearCatalog.Source.EARNED_COINS?0:item.requirement,
                AvatarGearCatalog.claimable(item,progress),item);
    }
    public static List<Entry> companions(GameStore store) {
        List<Entry> entries=new ArrayList<>(); for(Catalog.CharacterItem item:Catalog.CHARACTERS)entries.add(companion(store,item.id));
        return Collections.unmodifiableList(entries);
    }
    public static List<Entry> themes(GameStore store) {
        List<Entry> entries=new ArrayList<>(); for(Catalog.SkinItem item:Catalog.SKINS)entries.add(theme(store,item.id));
        return Collections.unmodifiableList(entries);
    }
    /** Null slot includes all 18 items; a slot returns its six catalog items in stable order. */
    public static List<Entry> gear(GameStore store, AvatarGearCatalog.Slot slot) {
        Objects.requireNonNull(store,"store");
        AvatarGearCatalog.Progress progress=store.avatarGearProgress(); AvatarLoadout loadout=store.avatarLoadout(); int coins=store.coins();
        List<Entry> entries=new ArrayList<>();
        for(AvatarGearCatalog.Item item:AvatarGearCatalog.ITEMS)if(slot==null||item.slot==slot)
            entries.add(gear(item,store.ownsAvatarGear(item.id),item.id.equals(loadout.item(item.slot).id),progress,coins));
        return Collections.unmodifiableList(entries);
    }
    public static List<Entry> filter(List<Entry> entries, Filter filter) {
        Objects.requireNonNull(entries,"entries"); Objects.requireNonNull(filter,"filter"); List<Entry> result=new ArrayList<>();
        for(Entry entry:entries)if(filter==Filter.ALL||(filter==Filter.OWNED)==entry.owned())result.add(entry);
        return Collections.unmodifiableList(result);
    }
    public static Counts counts(List<Entry> entries) { return new Counts(Objects.requireNonNull(entries,"entries")); }
    /** activeIds is supplied from actual run state; inventory stock is not evidence of an active effect. */
    public static List<Power> powers(GameStore store, Set<String> activeIds) {
        Objects.requireNonNull(store,"store"); List<Power> result=new ArrayList<>();
        for(Catalog.BoosterItem item:Catalog.BOOSTERS)result.add(new Power(item,store.boosterStock(item.id),store.boosterUnlocked(item.id),
                activeIds!=null&&activeIds.contains(item.id),store.boosterPrice(item.id)));
        return Collections.unmodifiableList(result);
    }
    public static Catalog.BoosterItem nextPower(int unlockedJourneyLevel) {
        Catalog.BoosterItem next=null;
        for(Catalog.BoosterItem item:Catalog.BOOSTERS)if(item.unlockLevel>unlockedJourneyLevel&&(next==null||item.unlockLevel<next.unlockLevel))next=item;
        return next;
    }
    public static String sourceCaption(String language, Entry entry) {
        if(entry==null)return "";
        switch(entry.source) {
            case STARTER: return ExperienceStrings.get(language,"source_starter");
            case JOURNEY_CLEARS: return ExperienceStrings.get(language,"source_journey",entry.progress,entry.required);
            case WEEKLY_WINS: return ExperienceStrings.get(language,"source_weekly",entry.progress,entry.required);
            case EVENT_STARS: return ExperienceStrings.get(language,"source_events",entry.progress,entry.required);
            default: return ExperienceStrings.get(language,"source_shop",entry.priceCoins);
        }
    }
    public static String effectCaption(String language, Entry entry) {
        if(entry==null)return ""; AvatarGearCatalog.Item item=entry.gear;
        if(item==null)return ExperienceStrings.get(language,"effect_appearance");
        if(item.winBonusCoins==1)return ExperienceStrings.get(language,"effect_win_one");
        if(item.winBonusCoins>1)return ExperienceStrings.get(language,"effect_win",item.winBonusCoins);
        if(item.packDiscountCoins==1)return ExperienceStrings.get(language,"effect_discount_one");
        if(item.packDiscountCoins>1)return ExperienceStrings.get(language,"effect_discount",item.packDiscountCoins);
        return ExperienceStrings.get(language,item.blockedGrace>0?"effect_grace":"effect_appearance");
    }
    public static Page page(int total, int pageSize, int requestedPage) { return new Page(total,pageSize,requestedPage); }
    public static Page sagaPage(int total, int requestedPage) { return page(total,10,requestedPage); }
}
