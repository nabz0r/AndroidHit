import com.androidhit.lumadrift.data.*;
import java.nio.file.*;
import java.util.*;

/** Mutable external ownership is a JVM provider double, not a real account or entitlement. */
public final class StoreExternalV111Test {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();RewardsCosmeticsV17Test.Clock clock=new RewardsCosmeticsV17Test.Clock("2026-10-28");
        Set<String> externalCharacters=new HashSet<>(),externalThemes=new HashSet<>(),externalClothes=new HashSet<>();
        GameStore.ExternalOwnership ownership=new GameStore.ExternalOwnership(){
            public boolean ownsCharacter(String id){return externalCharacters.contains(id);}
            public boolean ownsSkin(String id){return externalThemes.contains(id);}
            public boolean ownsCosmetic(String id){return externalClothes.contains(id);}
        };
        GameStore store=new GameStore(context,ownership,clock);
        externalCharacters.add("miko");externalThemes.add("lagoon");externalClothes.add("hat_cupid");
        int before=store.coins();check(store.ownsCharacter("miko")&&store.ownsSkin("lagoon")&&store.ownsCosmetic("hat_cupid"),"actual adapter ownership is visible");
        check(!store.purchaseCharacter("miko")&&!store.purchaseSkin("lagoon")&&!store.purchaseCosmetic("hat_cupid")&&store.coins()==before,"external ownership prevents a duplicate local debit");
        check(store.equipCharacter("miko")&&store.equipSkin("lagoon")&&store.equipCosmetic("hat_cupid"),"owned provider choices can be equipped");
        GameStore cold=new GameStore(context.reboot(),ownership,clock);check(cold.equippedCharacter().equals("miko")&&cold.equippedSkin().equals("lagoon")&&cold.equippedCosmetic(CosmeticCatalog.Slot.HAT).equals("hat_cupid"),"external selections survive a cold read while their source remains available");
        check(!context.save().disk.containsKey("owned_character_miko")&&!context.save().disk.containsKey("owned_skin_lagoon")&&!context.save().disk.containsKey("cosmetic_owned_v17_hat_cupid"),"equipment does not fabricate local ownership receipts");

        Map<String,?> durableBefore=new HashMap<>(context.save().disk);externalCharacters.clear();externalThemes.clear();externalClothes.clear();
        check(!store.ownsCharacter("miko")&&!store.ownsSkin("lagoon")&&!store.ownsCosmetic("hat_cupid"),"revocation is reflected without a new store or login callback");
        check(store.equippedCharacter().equals("nova")&&store.equippedSkin().equals("lumina")&&store.equippedCosmetic(CosmeticCatalog.Slot.HAT).equals("hat_dew"),"revoked external selection uses the original valid starter fallback");
        check(!store.equipCharacter("miko")&&!store.equipSkin("lagoon")&&!store.equipCosmetic("hat_cupid"),"equip callback must respect false after ownership disappeared");
        check(!InventoryPresentation.companion(store,"miko").owned()&&!InventoryPresentation.theme(store,"lagoon").owned(),"inventory does not retain revoked ownership");
        check(durableBefore.equals(context.save().disk)&&store.coins()==before,"revocation reads and denied equipment do not change the local save or balance");

        for(int grant=0;grant<3;grant++)check(store.grantMilestone("v111:external-local-fund:"+grant,500)==500,"valid public earned-coin funding");
        check(store.purchaseCharacter("orbit")&&store.purchaseSkin("blossom")&&store.purchaseCosmetic("dress_witch")&&store.purchaseAvatarGear("copper_wish"),"real local earned purchases remain independent of the provider");
        check(store.equipCharacter("orbit")&&store.equipSkin("blossom")&&store.equipCosmetic("dress_witch")&&store.equipAvatarGear("copper_wish"),"local choices equip in their own slots");
        GameStore guestCold=new GameStore(context.reboot(),null,clock);
        check(guestCold.ownsCharacter("orbit")&&guestCold.ownsSkin("blossom")&&guestCold.ownsCosmetic("dress_witch")&&guestCold.ownsAvatarGear("copper_wish"),"cold guest keeps every real local ownership receipt");
        check(guestCold.equippedCharacter().equals("orbit")&&guestCold.equippedSkin().equals("blossom")&&guestCold.equippedCosmetic(CosmeticCatalog.Slot.DRESS).equals("dress_witch")&&guestCold.avatarLoadout().charmId.equals("copper_wish"),"cold guest retains local choices without an account");
        String json="{\n  \"passed\": true,\n  \"assertions\": "+checks+",\n  \"scope\": \"Actual GameStore and InventoryPresentation with mutable JVM external-ownership and memory/disk preference doubles; no real account, payment, native callback or rendering validation.\"\n}\n";
        if(args.length>0){Path file=Path.of(args[0]);Files.createDirectories(file.getParent());Files.writeString(file,json);}
        System.out.println("PASS "+checks+" external ownership and local wardrobe lifecycle checks");
    }
}
