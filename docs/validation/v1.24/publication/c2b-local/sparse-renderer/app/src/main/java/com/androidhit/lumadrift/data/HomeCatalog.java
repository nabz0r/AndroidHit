package com.androidhit.lumadrift.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Original room collection. Every paid object uses existing, earned LumaCoins. */
public final class HomeCatalog {
    public static final int SIZE = 10;
    public static final String DEFAULT_ROOM = "living";
    public static final List<String> ROOMS=Collections.unmodifiableList(Arrays.asList("living","bedroom","garden","kitchen"));
    public static boolean validRoom(String id){return ROOMS.contains(id);}
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
        new Item("stool",1,1,100,true,"sit",0xffc6ac94,0xffc1d8c1),
        new Item("aquarium",3,1,420,true,"watch",0xff83d4df,0xffe8b987),
        new Item("piano",3,2,560,true,"watch",0xff47425c,0xfffff1d7),
        new Item("greenhouse",3,3,640,true,"admire",0xffa1d6bc,0xfff2d18c),
        new Item("library",3,1,380,true,"read",0xffbb8d69,0xffc9bcdf),
        new Item("bay_window",3,1,280,true,"watch",0xffa4d8e8,0xfff3d5b6),
        new Item("telescope",2,2,390,true,"watch",0xffd8ba80,0xffa8b9d7),
        new Item("fireplace",2,1,350,true,"relax",0xffc99380,0xffffd080),
        new Item("desk",2,1,230,true,"read",0xffcda780,0xffa7c9c2),
        new Item("easel",1,1,190,true,"watch",0xffc89b72,0xffedb3cb),
        new Item("record_player",1,1,210,true,"watch",0xffbc8e71,0xff675a75),
        new Item("arcade",1,1,400,true,"watch",0xff9f8fd0,0xff8ce3da),
        new Item("terrarium",1,1,170,true,"admire",0xffa6d8c9,0xffd5ad8c),
        new Item("bonsai",1,1,150,true,"admire",0xff84b294,0xffbfa083),
        new Item("cactus",1,1,130,true,"admire",0xff8fbb9d,0xffd8a08f),
        new Item("cherry_tree",2,2,460,true,"admire",0xffeab5cc,0xffbd967a),
        new Item("fountain",2,2,370,true,"watch",0xffb7cbd6,0xff9ddfea),
        new Item("pond",3,2,420,true,"watch",0xff8dd0d1,0xff9ac49c),
        new Item("hammock",3,1,270,true,"rest",0xffe5b7c9,0xffc4a080),
        new Item("swing",2,2,320,true,"sit",0xffd3b185,0xffb1cfbc),
        new Item("bench",2,1,180,true,"sit",0xffa3c4b2,0xffd5b78f),
        new Item("picnic",2,2,220,true,"tea",0xffe3bd9a,0xfff2d6c7),
        new Item("flowerbed",2,1,0,true,"admire",0xffa5c993,0xffebb6c7),
        new Item("garden_path",2,2,0,false,"relax",0xffc8d0c9,0xffe6eadf),
        new Item("lantern",1,1,160,true,"light",0xffbba5cc,0xffffd48e),
        new Item("canopy_bed",3,2,520,true,"rest",0xffc8b5df,0xfff4e3dc),
        new Item("wardrobe",2,1,280,true,"watch",0xffc7aed5,0xfff1dbb2),
        new Item("bath",2,1,340,true,"relax",0xffdce9ea,0xffa5d7db),
        new Item("mirror",1,1,150,true,"watch",0xffdac29a,0xffb8dce5),
        new Item("kitchen",3,1,440,true,"tea",0xffa9c6bc,0xfff1d1af),
        new Item("chess_table",2,2,240,true,"watch",0xffbd9c86,0xffefdfc6),
        new Item("tea_station",2,1,180,true,"tea",0xff8edcf1,0xffffffff),
        new Item("herb_cart",1,1,0,true,"admire",0xff9cdcb7,0xffffffff),
        new Item("bubble_fridge",1,1,280,true,"watch",0xff8acdf2,0xffffaca2),
        new Item("cooking_island",2,2,320,true,"tea",0xff9bdff1,0xffffb89d),
        new Item("seed_shelf",2,1,0,true,"admire",0xff9bddb5,0xffffffff),
        new Item("bloom_arch",2,1,260,true,"admire",0xffa2e0bb,0xffffa2a0)
    ));
    public static final List<Surface> WALLPAPERS=Collections.unmodifiableList(Arrays.asList(
        new Surface("wallpaper_cream",0,0xfff7fcff,0xffc6edfa),
        new Surface("wallpaper_sage",0,0xffe1f8ed,0xffb4e8d2),
        new Surface("wallpaper_dusk",180,0xffeee7ff,0xffc6bcf4),
        new Surface("wallpaper_peach",160,0xffffeee9,0xffffc2b7)
    ));
    public static final List<Surface> FLOORS=Collections.unmodifiableList(Arrays.asList(
        new Surface("floor_oak",0,0xffe7f3f8,0xffbedeea),
        new Surface("floor_cloud",0,0xffedf8ff,0xffc6e5f8),
        new Surface("floor_rose",180,0xffffe5e9,0xfff1b6c4),
        new Surface("floor_mint",160,0xffdef7eb,0xffb4e2d0)
    ));
    /** Art direction used by the workshop; ownership never depends on a theme. */
    public static String theme(String id){
        if(id==null)return "stellar";
        switch(id){
            case "greenhouse":case "plant":case "terrarium":case "bonsai":case "cactus":case "cherry_tree":case "fountain":case "pond":case "hammock":case "swing":case "bench":case "picnic":case "flowerbed":case "garden_path":case "herb_cart":case "seed_shelf":case "bloom_arch":return "forest";
            case "arcade":case "record_player":case "kitchen":case "desk":case "easel":case "piano":case "chess_table":case "gate_lamp":case "tea_station":case "bubble_fridge":case "cooking_island":return "neon";
            default:return "stellar";
        }
    }
    private HomeCatalog(){}
    public static Item item(String id){for(Item item:ITEMS)if(item.id.equals(id))return item;return null;}
    public static Surface wallpaper(String id){for(Surface item:WALLPAPERS)if(item.id.equals(id))return item;return null;}
    public static Surface floor(String id){for(Surface item:FLOORS)if(item.id.equals(id))return item;return null;}
    public static Surface surface(String id){Surface result=wallpaper(id);return result==null?floor(id):result;}
    public static boolean known(String id){return item(id)!=null||surface(id)!=null;}
    public static int price(String id){Item item=item(id);Surface surface=surface(id);return item!=null?item.price:surface!=null?surface.price:-1;}
}
