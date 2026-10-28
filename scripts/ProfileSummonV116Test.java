import com.androidhit.lumadrift.data.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Store transactions and pure catalogs on JVM preference doubles. No rendering/device acceptance claim. */
public final class ProfileSummonV116Test {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static RewardsCosmeticsV17Test.Clock clock(){return new RewardsCosmeticsV17Test.Clock("2026-10-29");}
    private static GameStore store(RewardsCosmeticsV17Test.Ctx context){return new GameStore(context,null,clock());}
    private static void fund(GameStore store,String id,int total){int i=0;while(store.coins()<total)check(store.grantMilestone("v116:"+id+":"+i++,Math.min(500,total-store.coins()))>0,"earned funding "+id);}
    private static void refused(Runnable action,String message){boolean typed=false;try{action.run();}catch(GameStore.LocalSaveException expected){typed=true;}check(typed,message);}
    private static void profile(){
        RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(context);
        check(s.playerNickname().equals("Luma")&&s.playerFlag().equals("WORLD"),"safe explicit local defaults");
        check(s.equippedBadges().equals(Arrays.asList("","","")),"no automatically worn badges");
        check(PlayerProfile.normalizeNickname("  E\u0301toile\n  lune \u202E ").equals("Étoile lune"),"Unicode normalization, whitespace and bidi cleanup");
        check(PlayerProfile.normalizeNickname("A\u0000B\ud800").equals("AB"),"controls and malformed surrogate excluded");
        for(String invisible:new String[]{"\u034f","\u0301","\u0301\u0327","\ufe0f","\udb40\udd00","\u115f\u1160","\u17b4\u17b5","\u2800","\u3164","\uffa0"})
            check(PlayerProfile.normalizeNickname(invisible).isEmpty()&&!s.setPlayerNickname(invisible),"marks-only and blank filler nicknames rejected");
        check(PlayerProfile.normalizeNickname("\u0301A\u034f\ufe0f\u3164").equals("A"),"orphan marks and invisible fillers removed around visible text");
        check(PlayerProfile.normalizeNickname("☀\ufe0f Luma 😀").equals("☀ Luma 😀"),"basic emoji and visible symbols survive selector cleanup");
        check(PlayerProfile.normalizeNickname("q\u0301 étoile").equals("q\u0301 étoile"),"noncomposing accents retain a visible letter base");
        check(PlayerProfile.normalizeNickname("😀".repeat(18)).codePointCount(0,36)==18,"nickname bound counts Unicode code points");
        for(String invalid:new String[]{null,""," \n\t ","a".repeat(19),"a".repeat(1025),"\u200e\u202e"})
            check(!s.setPlayerNickname(invalid)&&s.playerNickname().equals("Luma"),"invalid nickname cannot replace local name");
        check(s.setPlayerNickname("  Étoile   42  ")&&s.playerNickname().equals("Étoile 42"),"clean local nickname saved");
        for(String flag:PlayerProfile.FLAGS)check(s.setPlayerFlag(flag)&&s.playerFlag().equals(flag),"explicit supported flag "+flag);
        for(String flag:new String[]{null,"fr","ZZ","FR\n","EU"})check(!s.setPlayerFlag(flag)&&s.playerFlag().equals("IT"),"unknown flags refused");
        GameStore cold=store(context.reboot());check(cold.playerNickname().equals("Étoile 42")&&cold.playerFlag().equals("IT"),"local identity durable on cold recreation");
        check(s.coins()==180&&s.equippedCharacter().equals("nova"),"profile changes neither economy nor avatar ownership");
        for(boolean memory:new boolean[]{false,true}){
            context.save().fail(memory);refused(()->s.setPlayerNickname("Mochi"),"nickname failure typed "+memory);
            check(store(context.reboot()).playerNickname().equals("Étoile 42"),"failed nickname has no partial disk state "+memory);
            context.save().fail(memory);refused(s::playerNickname,"repeated refusal exposed by name read "+memory);
            check(s.playerNickname().equals("Mochi")&&store(context.reboot()).playerNickname().equals("Mochi"),"exact nickname retry durable "+memory);
            check(s.setPlayerNickname("Étoile 42"),"restore original name for next refusal");
            context.save().fail(memory);refused(()->s.setPlayerFlag("LU"),"flag failure typed "+memory);
            check(s.playerFlag().equals("LU")&&store(context.reboot()).playerFlag().equals("LU"),"exact flag retry durable "+memory);
        }
    }
    private static boolean placeAnywhere(GameStore store,String id){
        for(String room:HomeCatalog.ROOMS){store.setHomeRoom(room);for(int y=0;y<HomeCatalog.SIZE;y++)for(int x=0;x<HomeCatalog.SIZE;x++)for(int r=0;r<2;r++)
            if(store.homeLayout().tryPlace(id,x,y,r)!=null)return store.placeHomeItem(id,x,y,r);}
        return false;
    }
    private static void badges(){
        RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(context);
        check(!s.badgeUnlocked("first_win")&&!s.badgeUnlocked("journey_10")&&!s.badgeUnlocked("journey_100")&&!s.badgeUnlocked("collector_5"),"unearned progression badges locked");
        check(s.badgeProgress("decorator_5")==7&&s.badgeUnlocked("decorator_5")&&!s.badgeUnlocked("decorator_10"),"starter room designs count once across rooms");
        check(!s.equipBadge(0,"first_win")&&!s.equipBadge(-1,"decorator_5")&&!s.equipBadge(3,"decorator_5")&&!s.equipBadge(0,"unknown")&&!s.equipBadge(0,null),"locked and invalid badge assignments rejected");
        check(s.equipBadge(0,"decorator_5")&&s.equippedBadges().get(0).equals("decorator_5"),"earned badge equipped");
        check(s.equipBadge(2,"decorator_5")&&s.equippedBadges().equals(Arrays.asList("","","decorator_5")),"badge move cannot duplicate across slots");
        for(int level=1;level<=100;level++){check(s.completeLevel(level,3)>0,"actual completed level "+level);if(level==1)check(s.badgeUnlocked("first_win"),"first native-progress marker unlocks award");if(level==10)check(s.badgeUnlocked("journey_10"),"ten actual clears unlock award");}
        check(s.badgeUnlocked("journey_100")&&s.badgeProgress("journey_100")==100,"hundred actual clears unlock award");
        check(s.equipBadge(0,"first_win")&&s.equipBadge(1,"journey_100"),"three different earned badges supported");
        for(String id:new String[]{"miko","orbit","pip","astra"})check(s.purchaseCharacter(id),"direct ownership remains available "+id);
        check(s.badgeUnlocked("collector_5")&&s.badgeProgress("collector_5")==5,"five owned avatars includes free Nova");
        for(String id:new String[]{"sofa","bookshelf","plush"})check(s.purchaseHomeItem(id)&&placeAnywhere(s,id),"real furniture ownership and placement "+id);
        check(s.badgeUnlocked("decorator_10")&&s.badgeProgress("decorator_10")>=10,"ten distinct room designs earned");
        // Removing every object cannot revoke an already reached room award.
        for(String room:HomeCatalog.ROOMS){s.setHomeRoom(room);for(HomeLayout.Placement item:new ArrayList<>(s.homeLayout().placements()))check(s.removeHomeItem(item.id),"remove placed object "+item.id);}
        check(s.badgeUnlocked("decorator_10")&&s.badgeProgress("decorator_10")==10,"decorating badge permanently records achieved threshold");
        GameStore cold=store(context.reboot());check(cold.badgeUnlocked("decorator_10")&&cold.equippedBadges().equals(Arrays.asList("first_win","journey_100","decorator_5")),"earned and selected badges survive cold restart");
        for(boolean memory:new boolean[]{false,true}){
            context.save().fail(memory);refused(()->s.equipBadge(2,"collector_5"),"badge equip refusal typed "+memory);
            check(s.equippedBadges().get(2).equals("collector_5")&&store(context.reboot()).equippedBadges().get(2).equals("collector_5"),"badge selection recovery durable "+memory);
            check(s.equipBadge(2,"decorator_5"),"restore decorated award");
        }
        check(s.equipBadge(1,"")&&s.equippedBadges().get(1).isEmpty(),"explicit empty badge slot supported");
    }
    private static void catalogs(){
        Set<String> ids=new HashSet<>();
        for(SummonCatalog.Banner banner:SummonCatalog.Banner.values()){
            int total=0;Map<String,Integer> observed=new HashMap<>();
            for(SummonCatalog.Item item:SummonCatalog.items(banner)){
                check(ids.add(item.id)&&SummonCatalog.find(item.id)==item,"globally unique summon identity "+item.id);
                check(item.chanceBasisPoints>0&&item.duplicateCoins>0&&item.duplicateCoins<SummonCatalog.priceCoins(banner),"explicit probability and bounded compensation "+item.id);
                total+=item.chanceBasisPoints;
            }
            check(total==10_000,"unconditional banner odds total exactly100% "+banner);
            for(int index=0;index<30_000;index++){
                SummonCatalog.Item item=SummonCatalog.draw("stable-host-seed",banner,"simulation:"+index);
                check(SummonCatalog.draw("stable-host-seed",banner,"simulation:"+index)==item,"retry deterministic "+banner+index);
                observed.merge(item.id,1,Integer::sum);
            }
            for(SummonCatalog.Item item:SummonCatalog.items(banner)){
                double expected=30_000*item.chanceBasisPoints/10000.0;
                check(Math.abs(observed.getOrDefault(item.id,0)-expected)<Math.max(90,5*Math.sqrt(expected)),"distribution consistent with declared exact item probability "+item.id);
            }
        }
        check(SummonCatalog.items(SummonCatalog.Banner.AVATARS).size()==23&&SummonCatalog.find(SummonCatalog.Banner.AVATARS,"nova")==null,"23 avatars, free Nova explicitly excluded");
        check(SummonCatalog.items(SummonCatalog.Banner.SIGNATURES).size()==5,"five full signature sets");
        check(SummonCatalog.items(null).isEmpty()&&SummonCatalog.find("bad")==null&&SummonCatalog.priceCoins(null)==0,"invalid catalog query safety");
        Set<String> looks=new HashSet<>();
        for(SignatureOutfits.Outfit outfit:SignatureOutfits.ITEMS){
            check(outfit.cosmeticIds.size()==4&&looks.add(outfit.cosmeticIds.toString()),"four slots and unique original look "+outfit.id);
            for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())check(CosmeticCatalog.find(outfit.item(slot)).slot==slot,"signature references actual correct wardrobe slot "+outfit.id+slot);
        }
    }
    private static String requestFor(GameStore s,RewardsCosmeticsV17Test.Ctx c,SummonCatalog.Banner banner,String itemId,String prefix){
        String seed=c.save().getString("summon_seed_"+SummonCatalog.VERSION,"");
        for(int i=0;i<100_000;i++){String request=prefix+":"+i;if(SummonCatalog.draw(seed,banner,request).id.equals(itemId))return request;}
        throw new AssertionError("No deterministic request for "+itemId);
    }
    private static void draws(){
        RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(context);fund(s,"draws",30_000);
        int before=s.coins();GameStore.LootResult legacy=s.drawCosmeticLoot("legacy:preserved");String legacyItem=legacy.itemId;int legacyBalance=s.coins();
        check(legacy.chargedCoins==150&&legacyBalance==before-150+legacy.compensationCoins,"old clothing capsule and price preserved");
        GameStore.SummonResult first=s.drawSummon(SummonCatalog.Banner.AVATARS,"first:avatar");
        check(first!=null&&!first.duplicate&&!first.replayed&&first.chargedCoins==180&&s.ownsCharacter(first.itemId),"first avatar ownership and exact charge");
        check(s.lastSummonRequestId().equals(first.requestId)&&s.lastSummonResult().itemId.equals(first.itemId),"same-commit reveal identity recoverable");
        check(s.equippedCharacter().equals("nova"),"draw never silently equips outcome");
        int balance=s.coins();check(s.drawSummon(SummonCatalog.Banner.SIGNATURES,"first:avatar")==null&&s.coins()==balance,"one request cannot debit another banner");
        check(s.drawSummon(SummonCatalog.Banner.AVATARS,"first:avatar").replayed&&s.coins()==balance,"replay no debit");
        for(SummonCatalog.Banner banner:SummonCatalog.Banner.values())for(SummonCatalog.Item item:SummonCatalog.items(banner)){
            String request=requestFor(s,context,banner,item.id,"own:"+banner+":"+item.id);
            int coins=s.coins();boolean already=s.ownsSummonItem(item.id);GameStore.SummonResult result=s.drawSummon(banner,request);
            check(result!=null&&result.itemId.equals(item.id)&&result.duplicate==already,"actual draw ownership result "+item.id);
            check(s.coins()==coins-SummonCatalog.priceCoins(banner)+result.compensationCoins,"atomic outcome debit and compensation "+item.id);
            check(s.ownsSummonItem(item.id)&&s.equipSummonItem(item.id),"new actual item equips "+item.id);
            if(item.kind==SummonCatalog.Kind.OUTFIT)for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())check(s.equippedCosmetic(slot).equals(SignatureOutfits.find(item.id).item(slot)),"full outfit four-slot equipment "+item.id+slot);
            GameStore cold=store(context.reboot());check(cold.ownsSummonItem(item.id)&&cold.summonReceipt(request).itemId.equals(item.id)&&cold.summonReceipt(request).chargedCoins==0,"cold receipt and ownership "+item.id);
            String duplicate=requestFor(s,context,banner,item.id,"duplicate:"+banner+":"+item.id);int old=s.coins();GameStore.SummonResult dup=s.drawSummon(banner,duplicate);
            check(dup.duplicate&&dup.compensationCoins==item.duplicateCoins&&s.coins()==old-SummonCatalog.priceCoins(banner)+item.duplicateCoins,"declared duplicate policy "+item.id);
            int after=s.coins();check(s.drawSummon(banner,duplicate).replayed&&s.coins()==after,"duplicate receipt cannot refund twice "+item.id);
        }
        check(s.drawCosmeticLoot("legacy:preserved").itemId.equals(legacyItem)&&s.drawCosmeticLoot("legacy:preserved").replayed,"new banners retain legacy receipt identity");
        for(AvatarStyles.Style style:AvatarStyles.all())check(s.equipAvatarStyle(style.id)&&s.avatarStyle().equals(style.id),"all legacy free visual styles remain free "+style.id);
        check(!s.ownsSummonItem("bad")&&!s.equipSummonItem("bad")&&!s.equipSignatureOutfit("bad"),"invalid outcomes cannot grant or equip");
        for(String invalid:new String[]{null,"","bad/id","x".repeat(161)})check(s.drawSummon(SummonCatalog.Banner.AVATARS,invalid)==null&&s.summonReceipt(invalid)==null,"invalid receipt cannot spend");
        RewardsCosmeticsV17Test.Ctx freshContext=new RewardsCosmeticsV17Test.Ctx();GameStore fresh=store(freshContext);
        check(!fresh.equipSignatureOutfit("aurora")&&!fresh.ownsSignatureOutfit("aurora"),"unowned full signature cannot equip");
        check(fresh.drawSummon(SummonCatalog.Banner.SIGNATURES,"unfunded")==null&&fresh.lastSummonResult()==null&&fresh.coins()==180,"unfunded attempt creates neither receipt nor last reveal");
        check(fresh.grantMilestone("later:earned",80)==80&&fresh.drawSummon(SummonCatalog.Banner.SIGNATURES,"unfunded")!=null,"unfunded request can succeed after actually earning coins");
    }
    private static void failures(){
        for(SummonCatalog.Banner banner:SummonCatalog.Banner.values())for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(context);fund(s,"failure",2000);int before=s.coins();
            context.save().fail(memory);refused(()->s.drawSummon(banner,"refused:draw"),"draw failure typed "+banner+memory);
            GameStore disk=store(context.reboot());check(disk.coins()==before&&disk.summonReceipt("refused:draw")==null&&disk.lastSummonResult()==null,"no partial durable draw after failed commit "+banner+memory);
            context.save().fail(memory);refused(()->s.summonReceipt("refused:draw"),"receipt read exposes repeated save refusal "+banner+memory);
            GameStore.SummonResult recovered=s.drawSummon(banner,"refused:draw");
            check(recovered.replayed&&recovered.chargedCoins==0&&s.coins()==before-SummonCatalog.priceCoins(banner)+recovered.compensationCoins,"recovery retries exact original debit "+banner+memory);
            GameStore cold=store(context.reboot());check(cold.lastSummonResult().itemId.equals(recovered.itemId)&&cold.ownsSummonItem(recovered.itemId),"recovered outcome and last reveal survive cold start "+banner+memory);
            if(banner==SummonCatalog.Banner.SIGNATURES){
                context.save().fail(memory);refused(()->s.equipSignatureOutfit(recovered.itemId),"four-slot outfit failure typed "+memory);
                GameStore beforeRecovery=store(context.reboot());for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())check(beforeRecovery.equippedCosmetic(slot).equals(CosmeticCatalog.starter(slot).id),"no partial outfit on disk "+slot+memory);
                for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())check(s.equippedCosmetic(slot).equals(SignatureOutfits.find(recovered.itemId).item(slot)),"four-slot outfit exact recovery "+slot+memory);
                GameStore equippedCold=store(context.reboot());for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())check(equippedCold.equippedCosmetic(slot).equals(SignatureOutfits.find(recovered.itemId).item(slot)),"four-slot outfit durable "+slot+memory);
            }
        }
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();GameStore s=store(context);
            context.save().fail(memory);refused(()->s.badgeUnlocked("decorator_5"),"award persistence refusal typed "+memory);
            check(!context.save().disk.containsKey("badge_earned_v116_decorator_5"),"refused award never partially durable "+memory);
            context.save().fail(memory);refused(s::equippedBadges,"pending earned award blocks repeated refusal "+memory);
            check(s.badgeUnlocked("decorator_5")&&store(context.reboot()).badgeUnlocked("decorator_5"),"exact earned award recovery durable "+memory);
        }
    }
    private static void upgradeAndPartialOwnership(){
        RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();GameStore legacy=store(context);fund(legacy,"upgrade",4000);
        check(legacy.purchaseCharacter("miko")&&legacy.equipCharacter("miko"),"legacy character fixture through public APIs");
        check(legacy.purchaseSkin("lagoon")&&legacy.equipSkin("lagoon"),"legacy theme fixture through public APIs");
        check(legacy.purchaseCosmetic("hat_aurora")&&legacy.equipCosmetic("hat_aurora"),"partial signature fixture through old direct purchase");
        check(legacy.equipAvatarStyle("plush_classic"),"legacy free style fixture");
        GameStore.LootResult oldReceipt=legacy.drawCosmeticLoot("upgrade:old-capsule");
        Map<String,Object> oldDisk=new HashMap<>(context.save().disk);
        RewardsCosmeticsV17Test.Ctx upgradedContext=context.reboot();GameStore upgraded=store(upgradedContext);
        check(upgraded.playerNickname().equals("Luma")&&upgraded.playerFlag().equals("WORLD"),"legacy profile obtains local defaults without resetting save");
        check(upgraded.equippedCharacter().equals("miko")&&upgraded.equippedSkin().equals("lagoon")&&upgraded.equippedCosmetic(CosmeticCatalog.Slot.HAT).equals("hat_aurora"),"old chosen avatar/theme/clothes retained");
        check(upgraded.drawCosmeticLoot("upgrade:old-capsule").itemId.equals(oldReceipt.itemId),"preexisting legacy receipt still exact");
        for(Map.Entry<String,Object> item:oldDisk.entrySet())check(Objects.equals(upgradedContext.save().disk.get(item.getKey()),item.getValue()),"read-only upgrade keeps every old durable entry "+item.getKey());
        // Initialize the new seed with an actual draw, then target a public-model deterministic request.
        check(legacy.drawSummon(SummonCatalog.Banner.AVATARS,"partial:seed")!=null,"new pool initialization for partial ownership case");
        String request=requestFor(legacy,context,SummonCatalog.Banner.SIGNATURES,"aurora","partial:aurora");
        GameStore.SummonResult result=legacy.drawSummon(SummonCatalog.Banner.SIGNATURES,request);
        check(!result.duplicate&&result.compensationCoins==0&&legacy.ownsSignatureOutfit("aurora"),"partial outfit grants missing designs without duplicate compensation");
        check(legacy.cosmeticAcquisitionSource("hat_aurora").equals("EARNED_COINS"),"signature draw preserves already owned piece provenance");
        for(String id:SignatureOutfits.find("aurora").cosmeticIds)check(legacy.ownsCosmetic(id),"partial signature obtains every missing actual piece "+id);
        check(legacy.equippedCharacter().equals("miko")&&legacy.equippedSkin().equals("lagoon")&&legacy.equippedCosmetic(CosmeticCatalog.Slot.DRESS).equals("dress_dew"),"new ownership never replaces legacy selections");
    }
    private static void concurrency()throws Exception{
        RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();GameStore one=store(context),two=store(context);fund(one,"concurrency",1000);int before=one.coins();
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try{
            List<Future<GameStore.SummonResult>> calls=pool.invokeAll(Arrays.asList(
                    ()->one.drawSummon(SummonCatalog.Banner.SIGNATURES,"parallel:shared"),
                    ()->two.drawSummon(SummonCatalog.Banner.SIGNATURES,"parallel:shared")));
            GameStore.SummonResult a=calls.get(0).get(10,TimeUnit.SECONDS),b=calls.get(1).get(10,TimeUnit.SECONDS);
            check(a.itemId.equals(b.itemId)&&a.replayed!=b.replayed&&a.chargedCoins+b.chargedCoins==260,"two handles share one exact outcome/debit");
            check(one.coins()==before-260+a.compensationCoins&&store(context.reboot()).ownsSignatureOutfit(a.itemId),"concurrent ownership and balance durable");
        }finally{pool.shutdownNow();}
    }
    public static void main(String[] args)throws Exception{
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));profile();badges();catalogs();draws();failures();upgradeAndPartialOwnership();concurrency();
        String report="{\n  \"status\": \"PASS\",\n  \"assertions\": "+checks+",\n  \"simulated_draws\": 60000,\n  \"banner_items\": 28,\n  \"scope\": \"Pure catalogs and actual GameStore APIs with JVM durable-preference doubles; Unicode profile, earned badges, probabilities, both banners, all ownership/equipment, legacy capsule/style preservation, failed writes before/after memory changes, repeated refusal, exact replay, cold recreation and concurrent handles. No native rendering or real-device performance claim.\"\n}\n";
        if(args.length>0){Path path=Path.of(args[0]);Files.createDirectories(path.getParent());Files.writeString(path,report);}
        System.out.println("PASS "+checks+" v1.16 profile, badge and summon store checks");
    }
}
