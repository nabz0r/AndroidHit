import com.androidhit.lumadrift.data.*;
import java.nio.file.*;
import java.util.*;

/** Actual store APIs on JVM preference doubles; no device/profile injection or renderer claim. */
public final class StoreAppearanceV111Test {
    private static int checks;
    private static boolean baseline;
    private static final List<String> bugs=new ArrayList<>();
    private static final String[] IDS={"miko","lagoon","hat_cupid","starfold_visor"};
    private static final String[] LABELS={"character","theme","clothing","gear"};
    private static final String[] OWNER_KEYS={"owned_character_miko","owned_skin_lagoon","cosmetic_owned_v17_hat_cupid","avatar_owned_v1_starfold_visor"};
    private static final String[] SLOT_KEYS={"character","skin","cosmetic_slot_v17_HAT","avatar_slot_v1_HEAD"};
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static void bug(boolean value,String message){if(value)check(true,message);else if(baseline)bugs.add(message);else check(false,message);}
    private static RewardsCosmeticsV17Test.Clock clock(){return new RewardsCosmeticsV17Test.Clock("2026-10-28");}
    private static GameStore store(RewardsCosmeticsV17Test.Ctx context,RewardsCosmeticsV17Test.Clock clock){return new GameStore(context,null,clock);}
    private static boolean buy(GameStore s,int kind){return kind==0?s.purchaseCharacter(IDS[kind]):kind==1?s.purchaseSkin(IDS[kind]):kind==2?s.purchaseCosmetic(IDS[kind]):s.purchaseAvatarGear(IDS[kind]);}
    private static boolean equip(GameStore s,int kind){return kind==0?s.equipCharacter(IDS[kind]):kind==1?s.equipSkin(IDS[kind]):kind==2?s.equipCosmetic(IDS[kind]):s.equipAvatarGear(IDS[kind]);}
    private static boolean owns(GameStore s,int kind){return kind==0?s.ownsCharacter(IDS[kind]):kind==1?s.ownsSkin(IDS[kind]):kind==2?s.ownsCosmetic(IDS[kind]):s.ownsAvatarGear(IDS[kind]);}
    private static String selected(GameStore s,int kind){return kind==0?s.equippedCharacter():kind==1?s.equippedSkin():kind==2?s.equippedCosmetic(CosmeticCatalog.Slot.HAT):s.avatarLoadout().headId;}
    private static boolean typedFailure(Runnable action){try{action.run();return false;}catch(GameStore.LocalSaveException expected){return true;}}
    private static void fund(GameStore s,String identity,int required){int index=0;while(s.coins()<required)check(s.grantMilestone("v111:"+identity+":"+index++,Math.min(500,required-s.coins()))>0,"valid explicit earned-coin funding");}
    private static InventoryPresentation.Entry entry(GameStore s,int kind){return kind==0?InventoryPresentation.companion(s,IDS[kind]):kind==1?InventoryPresentation.theme(s,IDS[kind]):InventoryPresentation.gear(s,IDS[kind]);}

    private static void everyCatalogChain(){
        RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();RewardsCosmeticsV17Test.Clock clock=clock();GameStore s=store(context,clock);
        for(int level=1;level<=150;level++)check(s.completeLevel(level,3)>0,"real store first-clear progression "+level);
        for(int index=0;index<7;index++)check(s.completeWeekly(index,3)>0,"real weekly progress "+index);
        for(int index=0;index<3;index++)check(s.completeDaily(index,3)>0,"real daily progress "+index);
        clock.time+=7*86_400_000L;check(s.completeWeekly(0,3)>0,"eighth lifetime weekly win");
        for(int visit=0;visit<10;visit++){check(s.claimDailyLogin()>0,"real claimed login gift "+visit);clock.time+=86_400_000L;}
        s.claimCosmeticUnlocks();s.claimAvatarGearUnlocks();
        for(Catalog.CharacterItem item:Catalog.CHARACTERS){
            if(!s.ownsCharacter(item.id)){fund(s,"character-"+item.id,item.price);check(s.purchaseCharacter(item.id),"purchase "+item.id);}
            int coins=s.coins();check(s.equipCharacter(item.id)&&s.equippedCharacter().equals(item.id),"equip character "+item.id);
            InventoryPresentation.Entry entry=InventoryPresentation.companion(s,item.id);check(entry.owned()&&entry.selected(),"character inventory state "+item.id);
            GameStore cold=store(context.reboot(),clock);check(cold.equippedCharacter().equals(item.id)&&cold.coins()==coins&&!cold.purchaseCharacter(item.id),"character cold read and no duplicate debit "+item.id);
        }
        for(Catalog.SkinItem item:Catalog.SKINS){
            if(!s.ownsSkin(item.id)){fund(s,"theme-"+item.id,item.price);check(s.purchaseSkin(item.id),"purchase "+item.id);}
            int coins=s.coins();check(s.equipSkin(item.id)&&s.equippedSkin().equals(item.id),"equip theme "+item.id);
            check(InventoryPresentation.theme(s,item.id).selected(),"theme inventory state "+item.id);
            GameStore cold=store(context.reboot(),clock);check(cold.equippedSkin().equals(item.id)&&cold.coins()==coins&&!cold.purchaseSkin(item.id),"theme cold read and no duplicate debit "+item.id);
        }
        for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS){
            if(item.source==CosmeticCatalog.Source.EARNED_COINS){fund(s,"clothing-"+item.id,item.priceCoins);check(s.purchaseCosmetic(item.id),"purchase "+item.id);}
            check(s.ownsCosmetic(item.id),"actual source unlock owns clothing "+item.id);int coins=s.coins();
            Map<CosmeticCatalog.Slot,String> before=new EnumMap<>(CosmeticCatalog.Slot.class);for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())before.put(slot,s.equippedCosmetic(slot));
            check(s.equipCosmetic(item.id)&&s.equippedCosmetic(item.slot).equals(item.id),"equip clothing "+item.id);
            for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())if(slot!=item.slot)check(before.get(slot).equals(s.equippedCosmetic(slot)),"other clothes slot retained "+item.id+" "+slot);
            GameStore cold=store(context.reboot(),clock);check(cold.equippedCosmetic(item.slot).equals(item.id)&&cold.coins()==coins&&!cold.purchaseCosmetic(item.id),"clothing cold read and no duplicate debit "+item.id);
        }
        for(AvatarGearCatalog.Item item:AvatarGearCatalog.ITEMS){
            if(item.source==AvatarGearCatalog.Source.EARNED_COINS){fund(s,"gear-"+item.id,item.priceCoins);check(s.purchaseAvatarGear(item.id),"purchase "+item.id);}
            check(s.ownsAvatarGear(item.id),"actual source unlock owns gear "+item.id);int coins=s.coins();AvatarLoadout before=s.avatarLoadout();
            check(s.equipAvatarGear(item.id)&&s.avatarLoadout().item(item.slot).id.equals(item.id),"equip gear "+item.id);
            for(AvatarGearCatalog.Slot slot:AvatarGearCatalog.Slot.values())if(slot!=item.slot)check(before.item(slot).id.equals(s.avatarLoadout().item(slot).id),"other gear slot retained "+item.id+" "+slot);
            check(InventoryPresentation.gear(s,item.id).selected(),"gear inventory state "+item.id);
            GameStore cold=store(context.reboot(),clock);check(cold.avatarLoadout().item(item.slot).id.equals(item.id)&&cold.coins()==coins&&!cold.purchaseAvatarGear(item.id),"gear cold read and no duplicate debit "+item.id);
        }
    }

    private static void pendingReadBoundary(){
        for(int kind=0;kind<4;kind++)for(boolean memory:new boolean[]{false,true}){
            final int type=kind;String label=LABELS[kind]+" memoryBeforeDisk="+memory;RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();RewardsCosmeticsV17Test.Clock clock=clock();GameStore first=store(context,clock);fund(first,"pending-"+kind,1000);GameStore second=store(context,clock);
            context.save().fail(memory);check(typedFailure(()->buy(first,type)),"purchase refusal is typed "+label);
            context.save().fail(memory);boolean rejected=typedFailure(()->owns(second,type));bug(rejected,"Ownership read must expose repeatedly refused pending purchase: "+label);
            // A legacy getter might not attempt commit, leaving the next refusal armed.
            if(!rejected)context.save().fail=false;
            boolean owned=owns(second,kind);boolean durable=context.save().disk.containsKey(OWNER_KEYS[kind]);
            bug(owned&&durable,"Ownership read must flush exact purchase before returning: "+label+" owned="+owned+" durable="+durable);
            second.retryPendingLocalSave();check(owns(second,kind)&&owns(store(context.reboot(),clock),kind),"exact purchase remains durable after explicit recovery "+label);

            context.save().fail(memory);check(typedFailure(()->equip(first,type)),"equip refusal is typed "+label);
            context.save().fail(memory);boolean blocked=typedFailure(()->selected(second,type));bug(blocked,"Selection read must expose repeatedly refused pending equipment: "+label);
            if(!blocked)context.save().fail=false;
            String observed=selected(second,kind);boolean saved=IDS[kind].equals(context.save().disk.get(SLOT_KEYS[kind]));
            bug(observed.equals(IDS[kind])&&saved,"Selection read must flush exact equipment before returning: "+label+" selected="+observed+" durable="+saved);
            second.retryPendingLocalSave();check(selected(store(context.reboot(),clock),kind).equals(IDS[kind]),"recovered equipment is durable "+label);
        }
    }

    private static void coherentInventory(){
        for(int kind:new int[]{0,1,3}){
            final int type=kind;RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();RewardsCosmeticsV17Test.Clock clock=clock();GameStore store=store(context,clock);fund(store,"inventory-"+kind,1000);
            context.save().fail(false);check(typedFailure(()->buy(store,type)),"inventory purchase setup");
            InventoryPresentation.Entry recovered=entry(store,kind);
            bug(recovered.owned()&&context.save().disk.containsKey(OWNER_KEYS[kind]),"Inventory must not mix pre-flush ownership with post-flush balance: "+LABELS[kind]+" state="+recovered.state);
            store.retryPendingLocalSave();check(owns(store,kind),"inventory case exact ownership recovered");
        }
        RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();RewardsCosmeticsV17Test.Clock clock=clock();GameStore store=store(context,clock);fund(store,"inventory-gear-slot",1000);check(store.purchaseAvatarGear("starfold_visor"),"gear list setup");
        context.save().fail(false);check(typedFailure(()->store.equipAvatarGear("starfold_visor")),"gear list equip setup");
        InventoryPresentation.Entry found=null;for(InventoryPresentation.Entry entry:InventoryPresentation.gear(store,(AvatarGearCatalog.Slot)null))if(entry.id.equals("starfold_visor"))found=entry;
        bug(found!=null&&found.selected(),"Gear list must not retain pre-flush loadout with post-flush ownership: state="+(found==null?"missing":found.state));
        store.retryPendingLocalSave();
    }

    private static String quote(String value){return "\""+value.replace("\\","\\\\").replace("\"","\\\"")+"\"";}
    public static void main(String[] args)throws Exception{
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));baseline=Arrays.asList(args).contains("--baseline");everyCatalogChain();pendingReadBoundary();coherentInventory();
        StringBuilder json=new StringBuilder("{\n  \"mode\": "+quote(baseline?"baseline_observation":"regression")+",\n  \"assertions\": "+checks+",\n  \"catalog_identities_exercised\": 72,\n  \"reproduced_bug_count\": "+bugs.size()+",\n  \"reproduced_bugs\": [");
        for(int index=0;index<bugs.size();index++){if(index>0)json.append(',');json.append("\n    ").append(quote(bugs.get(index)));}
        json.append("\n  ],\n  \"scope\": \"Actual store/catalog/inventory APIs with JVM preference doubles and explicit public progression/grant calls; no native device/profile injection, rendering, account or payment acceptance.\"\n}\n");
        if(args.length>0&&!args[0].startsWith("--")){Path report=Path.of(args[0]);Files.createDirectories(report.getParent());Files.writeString(report,json);}
        System.out.println((baseline?"BASELINE":"PASS")+" "+checks+" store appearance checks; reproduced bugs="+bugs.size());
    }
}
