package com.androidhit.lumadrift.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Original room collection. Every paid object uses existing, earned LumaCoins. */
public final class HomeCatalog {
    public static final int SIZE = 6;
    public static final String DEFAULT_WALLPAPER = "wallpaper_cream";
    public static final String DEFAULT_FLOOR = "floor_oak";
    public static final class Item {
        public final String id, interaction;
        public final int width, depth, price, color, accent;
        public final boolean solid;
        private Item(String id,int width,int depth,int price,boolean solid,String interaction,int color,int accent) {
            this.id=id;this.width=width;this.depth=depth;this.price=price;this.solid=solid;this.interaction=interaction;this.color=color;this.accent=accent;
        }
        public boolean starter(){return price==0;}
        public String name(String language){return HomeStrings.name(language,id);}
        public String description(String language){return HomeStrings.description(language,id);}
    }
    public static final class Surface {
        public final String id;
        public final int price,color,accent;
        private Surface(String id,int price,int color,int accent){this.id=id;this.price=price;this.color=color;this.accent=accent;}
        public String name(String language){return HomeStrings.name(language,id);}
        public boolean starter(){return price==0;}
    }
    public static final List<Item> ITEMS=Collections.unmodifiableList(Arrays.asList(
        new Item("bed",2,1,0,true,"rest",0xffdfadc4,0xfffff5e9),
        new Item("sofa",2,1,240,true,"sit",0xffb6c6d9,0xfff3d6a6),
        new Item("lamp",1,1,0,true,"light",0xffe6c482,0xffffecc0),
        new Item("plant",1,1,0,true,"admire",0xff91b6a0,0xffdcaf98),
        new Item("rug",2,2,0,false,"relax",0xffc8b6d7,0xffeee5f4),
        new Item("table",1,1,140,true,"tea",0xffd7b18f,0xfff8e5c9),
        new Item("bookshelf",1,1,220,true,"read",0xffcba98e,0xffa8bfd3),
        new Item("cushion",1,1,0,true,"sit",0xffe3c391,0xfff8e2b8),
        new Item("plush",1,1,120,true,"cuddle",0xffd6b6ca,0xfffbefd9),
        new Item("gate_lamp",1,1,180,true,"switch",0xff9dbdc2,0xfff9d98d),
        new Item("mobile",1,1,160,true,"watch",0xffb7a7d2,0xfff4d497),
        new Item("stool",1,1,100,true,"sit",0xffc6ac94,0xffc1d8c1)
    ));
    public static final List<Surface> WALLPAPERS=Collections.unmodifiableList(Arrays.asList(
        new Surface("wallpaper_cream",0,0xfff8eadb,0xffefcfbf),
        new Surface("wallpaper_sage",0,0xffdfebdf,0xffbdd1be),
        new Surface("wallpaper_dusk",180,0xffded5ec,0xffbdacd5),
        new Surface("wallpaper_peach",160,0xfff3d8ce,0xffdcae9d)
    ));
    public static final List<Surface> FLOORS=Collections.unmodifiableList(Arrays.asList(
        new Surface("floor_oak",0,0xffd9bea0,0xffb99375),
        new Surface("floor_cloud",0,0xffdfe5ec,0xffbdc9d8),
        new Surface("floor_rose",180,0xffdec2c9,0xffbd929e),
        new Surface("floor_mint",160,0xffc9dccf,0xff9db8a9)
    ));
    private HomeCatalog(){}
    public static Item item(String id){for(Item item:ITEMS)if(item.id.equals(id))return item;return null;}
    public static Surface wallpaper(String id){for(Surface item:WALLPAPERS)if(item.id.equals(id))return item;return null;}
    public static Surface floor(String id){for(Surface item:FLOORS)if(item.id.equals(id))return item;return null;}
    public static Surface surface(String id){Surface result=wallpaper(id);return result==null?floor(id):result;}
    public static boolean known(String id){return item(id)!=null||surface(id)!=null;}
    public static int price(String id){Item item=item(id);Surface surface=surface(id);return item!=null?item.price:surface!=null?surface.price:-1;}
}
