package com.androidhit.lumadrift.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Badges record actual local progression. Earned awards remain earned after redecorating. */
public final class BadgeCatalog {
    public enum Metric { LEVELS, PLACED_ITEMS, AVATARS }
    public static final int EQUIPPED_SLOTS = 3;
    public static final class Item {
        public final String id, nameKey;
        public final Metric metric;
        public final int requirement;
        private Item(String id, Metric metric, int requirement) {
            this.id=id; this.nameKey="badge_"+id; this.metric=metric; this.requirement=requirement;
        }
    }
    public static final List<Item> ITEMS=Collections.unmodifiableList(Arrays.asList(
            new Item("first_win",Metric.LEVELS,1), new Item("journey_10",Metric.LEVELS,10),
            new Item("journey_100",Metric.LEVELS,100), new Item("decorator_5",Metric.PLACED_ITEMS,5),
            new Item("decorator_10",Metric.PLACED_ITEMS,10), new Item("collector_5",Metric.AVATARS,5)));
    private BadgeCatalog() { }
    public static Item find(String id) { for(Item item:ITEMS)if(item.id.equals(id))return item;return null; }
}
