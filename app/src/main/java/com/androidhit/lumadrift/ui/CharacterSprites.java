package com.androidhit.lumadrift.ui;

import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Path;
import com.androidhit.lumadrift.data.Catalog;
import com.androidhit.lumadrift.data.CosmeticCatalog;
import com.androidhit.lumadrift.data.HomeCatalog;

/** Original raster art mapped explicitly onto the unchanged companion and wardrobe inventory IDs. */
public final class CharacterSprites {
    private static final String[][] CHARACTER_IDS = {
        {"nova", "miko", "orbit", "pip", "astra", "zig"},
        {"lune", "fenn", "hoot", "buzz", "tula", "jelli"},
        {"vela", "nimbi", "soli", "mochi", "nixi", "prickly"},
        {"quill", "waddles", "halo", "emberling", "bambu", "inky"}
    };
    private static final String[][] COSMETIC_IDS = {
        {"hat_dew", "hat_moon", "hat_star", "hat_pumpkin", "hat_cupid", "hat_aurora"},
        {"dress_dew", "dress_moon", "dress_star", "dress_witch", "dress_valentine", "dress_aurora"},
        {"shoes_dew", "shoes_moon", "shoes_star", "shoes_pumpkin", "shoes_cupid", "shoes_aurora"},
        {"gloves_dew", "gloves_moon", "gloves_star", "gloves_witch", "gloves_valentine", "gloves_aurora"}
    };
    private static final String[] GARMENT_SHEETS = {"hats", "dresses", "shoes", "gloves"};
    private static final ThreadLocal<Path> CLIP_PATH = ThreadLocal.withInitial(Path::new);
    private static volatile ArtLibrary installed;

    private CharacterSprites() { }

    /** Install once with application assets before rendering. No Activity is retained. */
    public static ArtLibrary install(AssetManager assets) {
        ArtLibrary library = new ArtLibrary(assets);
        register(library);
        installed = library;
        return library;
    }

    public static void install(ArtLibrary library) {
        if (library == null) throw new IllegalArgumentException("ArtLibrary required");
        installed = library;
    }

    /** Allows HomeScene to register additional furniture sheets in the same bounded cache. */
    public static ArtLibrary library() { return installed; }

    public static void register(ArtLibrary library) {
        if (library == null) throw new IllegalArgumentException("ArtLibrary required");
        for (int sheet = 0; sheet < CHARACTER_IDS.length; sheet++) {
            ArtRegions117.register(library,ArtLibrary.ROOT + "companions-" + sheet + ".png",
                    namespaced("character/", CHARACTER_IDS[sheet]));
        }
        for (int sheet = 0; sheet < COSMETIC_IDS.length; sheet++) {
            ArtRegions117.register(library,ArtLibrary.ROOT + GARMENT_SHEETS[sheet] + ".png",
                    namespaced("cosmetic/", COSMETIC_IDS[sheet]));
        }
        for (int sheet = 0; sheet < 7; sheet++) {
            String[] keys = new String[6];
            for (int cell = 0; cell < keys.length; cell++) {
                keys[cell] = "furniture/" + HomeCatalog.ITEMS.get(sheet * 6 + cell).id;
            }
            ArtRegions117.register(library,ArtLibrary.ROOT + "furniture-" + sheet + ".png", keys);
        }
    }

    /** No bob or tilt here: callers apply their one shared transform to body, garments and gear. */
    public static boolean drawBody(Canvas canvas, Catalog.CharacterItem character, float x, float y, float radius) {
        return drawBody(canvas,character,x,y,radius,false,false);
    }

    /** Replacing a pair masks only its original hand/boot ink before the authored garment is composed. */
    public static boolean drawBody(Canvas canvas,Catalog.CharacterItem character,float x,float y,float radius,
                                   boolean replaceHands,boolean replaceBoots){
        if (!finite(x) || !finite(y) || !finite(radius) || radius <= 0) return false;
        ArtLibrary library = installed;
        if (library == null || canvas == null) return false;
        String id = character == null ? Catalog.CHARACTERS[0].id : character.id;
        CompanionRig rig=CompanionRig.forCharacter(character);
        canvas.save();canvas.translate(x,y);canvas.scale(radius,radius);
        if(replaceHands){hideHand(canvas,rig.leftHand);hideHand(canvas,rig.rightHand);}
        if(replaceBoots){hideFoot(canvas,rig.leftFoot);hideFoot(canvas,rig.rightFoot);}
        boolean drawn=library.draw(canvas,"character/"+id,-CompanionRig.BODY_WIDTH*.5f,-1.5f,
                CompanionRig.BODY_WIDTH*.5f,1.65f,.5f,1,255);
        canvas.restore();return drawn;
    }

    /** Each isolated garment is fitted at its anatomical anchor, without recolouring the pixels. */
    public static boolean drawGarment(Canvas canvas, CosmeticCatalog.Item item, float x, float y, float radius) {
        return drawGarment(canvas,null,item,x,y,radius);
    }

    public static boolean drawGarment(Canvas canvas,Catalog.CharacterItem character,CosmeticCatalog.Item item,float x,float y,float radius){
        if (item == null || !finite(x) || !finite(y) || !finite(radius) || radius <= 0) return false;
        ArtLibrary library = installed;
        if (library == null || canvas == null) return false;
        CompanionRig rig=CompanionRig.forCharacter(character);String key="cosmetic/"+item.id;
        canvas.save();canvas.translate(x,y);canvas.scale(radius,radius);
        if(item.slot==CosmeticCatalog.Slot.GLOVES){
            boolean left=hand(canvas,library,key,rig.leftHand,0,.5f),right=hand(canvas,library,key,rig.rightHand,.5f,1);
            canvas.restore();return left&&right;
        }
        if(item.slot==CosmeticCatalog.Slot.SHOES){
            boolean left,right;
            if(rig.leftFoot.bottom>=rig.rightFoot.bottom){right=foot(canvas,library,key,rig.rightFoot,.5f,1);left=foot(canvas,library,key,rig.leftFoot,0,.5f);}
            else{left=foot(canvas,library,key,rig.leftFoot,0,.5f);right=foot(canvas,library,key,rig.rightFoot,.5f,1);}
            canvas.restore();return left&&right;
        }
        float left, top, right, bottom;
        if (item.slot == CosmeticCatalog.Slot.HAT) {
            left=rig.torsoX-.98f;top=-1.93f+rig.hatY;right=rig.torsoX+.98f;bottom=-.48f+rig.hatY;
        } else {
            left=rig.torsoX-1.00f;top=-.08f;right=rig.torsoX+1.00f;bottom=1.37f;
        }
        boolean drawn=library.draw(canvas,key,left,top,right,bottom,.5f,.5f,255);
        canvas.restore();return drawn;
    }

    private static boolean hand(Canvas c,ArtLibrary library,String key,CompanionRig.Hand hand,float sourceLeft,float sourceRight){
        c.save();c.translate(hand.x,hand.y);c.rotate(hand.angle);
        boolean drawn=library.drawPart(c,key,sourceLeft,0,sourceRight,1,-.29f,-.34f,.29f,.34f,.5f,.5f,255);
        c.restore();return drawn;
    }
    private static boolean foot(Canvas c,ArtLibrary library,String key,CompanionRig.Foot foot,float sourceLeft,float sourceRight){
        return library.drawPart(c,key,sourceLeft,0,sourceRight,1,foot.x-.37f,foot.bottom-.94f,foot.x+.37f,foot.bottom+.03f,.5f,1,255);
    }
    private static void hideHand(Canvas c,CompanionRig.Hand hand){
        // A palm is rounded; a box also removes the cape, wing or tentacle behind its corners.
        Path palm=CLIP_PATH.get();palm.reset();palm.addOval(hand.x-.25f,hand.y-.24f,hand.x+.25f,hand.y+.24f,Path.Direction.CW);
        c.clipOutPath(palm);
    }
    private static void hideFoot(Canvas c,CompanionRig.Foot foot){
        // Follow the narrow shaft and flared toe of the original boot. In particular the
        // upper outer corners must retain Fenn's tail and the wide coats of Quill/Bambu.
        float x=foot.x,y=foot.bottom;Path boot=CLIP_PATH.get();boot.reset();
        boot.moveTo(x-.18f,y-.66f);boot.lineTo(x+.20f,y-.66f);
        boot.lineTo(x+.26f,y-.34f);boot.quadTo(x+.29f,y-.23f,x+.43f,y-.18f);
        boot.quadTo(x+.52f,y-.14f,x+.52f,y-.02f);boot.quadTo(x+.51f,y+.08f,x+.37f,y+.08f);
        boot.lineTo(x-.37f,y+.08f);boot.quadTo(x-.51f,y+.08f,x-.52f,y-.04f);
        boot.quadTo(x-.51f,y-.17f,x-.31f,y-.24f);boot.lineTo(x-.24f,y-.39f);boot.close();
        c.clipOutPath(boot);
    }

    /** Catalogue/atelier preview: a real original garment rather than a procedural slot glyph. */
    public static boolean drawGarmentPreview(Canvas canvas, CosmeticCatalog.Item item,
                                             float left, float top, float right, float bottom) {
        ArtLibrary library = installed;
        return library != null && item != null
                && library.draw(canvas, "cosmetic/" + item.id, left, top, right, bottom, .5f, .5f, 255);
    }

    private static String[] namespaced(String prefix, String[] ids) {
        String[] keys = new String[ids.length];
        for (int i = 0; i < ids.length; i++) keys[i] = prefix + ids[i];
        return keys;
    }

    private static boolean finite(float value) { return !Float.isNaN(value) && !Float.isInfinite(value); }
}
