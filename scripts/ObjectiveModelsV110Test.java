import com.androidhit.lumadrift.data.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/** Real data models with memory-before-disk Android preference doubles; no UI/provider claims. */
public final class ObjectiveModelsV110Test {
    private static int checks;
    private static void check(boolean good,String message){checks++;if(!good)throw new AssertionError(message);}
    private static void saveFails(Runnable action,String message){boolean threw=false;try{action.run();}catch(GameStore.LocalSaveException expected){threw=true;}check(threw,message);}
    private static RewardsCosmeticsV17Test.Ctx context(){return new RewardsCosmeticsV17Test.Ctx();}
    private static RewardsCosmeticsV17Test.Clock clock(String day){return new RewardsCosmeticsV17Test.Clock(day);}
    private static GameStore store(RewardsCosmeticsV17Test.Ctx context,RewardsCosmeticsV17Test.Clock clock){return new GameStore(context,null,clock);}
    private static void finishEvents(GameStore store,boolean weekly){for(int i=0;i<(weekly?7:3);i++)if(weekly)store.completeWeekly(i,3);else store.completeDaily(i,3);}
    private static int claim(GameStore store,boolean weekly){return weekly?store.claimWeeklyCompletionReward():store.claimDailyCompletionReward();}
    private static boolean claimed(GameStore store,boolean weekly){return weekly?store.weeklyBonusClaimed():store.dailyBonusClaimed();}
    private static String receipt(GameStore store,boolean weekly){return (weekly?"weekly_bonus_":"daily_bonus_")+(weekly?store.weeklyId():store.dailyId());}

    private static void completionDurability()throws Exception{
        for(boolean weekly:new boolean[]{false,true})for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=context();RewardsCosmeticsV17Test.Clock k=clock("2026-10-28");GameStore s=store(c,k);
            check(claim(s,weekly)==0,"incomplete event bonus cannot pay");finishEvents(s,weekly);
            int amount=weekly?250:100,before=s.coins();String token=receipt(s,weekly);GameStore second=store(c,k);
            c.save().fail(memory);saveFails(()->claim(s,weekly),"initial event write failure is typed, never reported as success");
            check(c.save().disk.get("coins").equals(before)&&!c.save().disk.containsKey(token),"failed event commit retains previous durable balance and no receipt");
            c.save().fail(memory);saveFails(()->claimed(second,weekly),"receipt read must surface a refused pending flush");
            c.save().fail(memory);saveFails(()->claim(second,weekly),"second handle cannot bypass repeated pending failure");
            check(c.save().disk.get("coins").equals(before)&&!c.save().disk.containsKey(token),"repeated refused writes remain absent on disk");
            check(claim(second,weekly)==0&&claimed(s,weekly),"successful retry flushes the same receipt and acknowledges no second grant");
            check(s.coins()==before+amount&&c.save().disk.get("coins").equals(before+amount),"retry credits exactly the original event amount");
            check(claim(s,weekly)==0&&claim(second,weekly)==0,"both handles remain idempotent after recovery");
            GameStore cold=store(c.reboot(),k);check(claimed(cold,weekly)&&cold.coins()==before+amount&&claim(cold,weekly)==0,"balance and old receipt survive cold recreation");

            RewardsCosmeticsV17Test.Ctx killed=context();GameStore interrupted=store(killed,k);finishEvents(interrupted,weekly);int diskBefore=interrupted.coins();
            killed.save().fail(memory);saveFails(()->claim(interrupted,weekly),"cold-loss setup fails visibly");
            GameStore restarted=store(killed.reboot(),k);
            check(restarted.coins()==diskBefore&&!claimed(restarted,weekly),"cold recreation before retry exposes no partial durable reward");
            check(claim(restarted,weekly)==amount&&restarted.coins()==diskBefore+amount&&claim(restarted,weekly)==0,"cold recreation can earn exactly one still-unclaimed bonus");
        }
        for(boolean weekly:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=context();RewardsCosmeticsV17Test.Clock k=clock("2026-10-28");GameStore s=store(c,k);finishEvents(s,weekly);String token=receipt(s,weekly);
            c.save().edit().putInt("coins",1_999_999_975).commit();int amount=weekly?250:100;
            check(claim(s,weekly)==amount&&s.coins()==2_000_000_000&&claimed(s,weekly),"legacy saturating cap and nominal payout semantics remain unchanged");
            c.save().edit().putInt("coins",100).commit();check(claim(s,weekly)==0,"making room never reopens a capped receipt");
            RewardsCosmeticsV17Test.Ctx old=context();GameStore legacy=store(old,k);finishEvents(legacy,weekly);old.save().edit().putBoolean(token,true).commit();
            int balance=legacy.coins();check(claim(legacy,weekly)==0&&legacy.coins()==balance,"pre-1.10 receipt is preserved without a new namespace or payout");

            RewardsCosmeticsV17Test.Ctx race=context();GameStore first=store(race,k);finishEvents(first,weekly);int before=first.coins();AtomicInteger paid=new AtomicInteger(),errors=new AtomicInteger();
            CountDownLatch start=new CountDownLatch(1);List<Thread> threads=new ArrayList<>();
            for(int i=0;i<24;i++){GameStore handle=store(race,k);Thread thread=new Thread(()->{try{start.await();paid.addAndGet(claim(handle,weekly));}catch(Throwable failure){errors.incrementAndGet();}});threads.add(thread);thread.start();}
            start.countDown();for(Thread thread:threads)thread.join();
            check(errors.get()==0&&paid.get()==amount&&first.coins()==before+amount,"concurrent independent handles commit only one event completion bonus");
        }
    }

    private static void completionCalendar(){
        for(boolean weekly:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=context();RewardsCosmeticsV17Test.Clock k=clock(weekly?"2026-11-01":"2026-10-28");GameStore s=store(c,k);finishEvents(s,weekly);
            int before=s.coins(),amount=weekly?250:100;String original=receipt(s,weekly);c.save().fail(true);saveFails(()->claim(s,weekly),"period-bound setup retains a failed exact transaction");
            k.set(weekly?"2026-11-02":"2026-10-29");String current=receipt(s,weekly);check(!current.equals(original),"calendar really crosses the daily/weekly boundary");
            c.save().fail(true);saveFails(()->claim(s,weekly),"rollover cannot replace a pending receipt with the new period");
            check(claim(s,weekly)==0&&!claimed(s,weekly)&&s.coins()==before+amount,"retry settles original bonus without completing the new period");
            check(Boolean.TRUE.equals(c.save().disk.get(original))&&!c.save().disk.containsKey(current),"the exact old period receipt is durable after rollover");
            finishEvents(s,weekly);int now=s.coins();check(claim(s,weekly)==amount&&s.coins()==now+amount,"new period legitimately earns its own distinct bonus");

            RewardsCosmeticsV17Test.Ctx crossing=context();RewardsCosmeticsV17Test.Clock old=clock(weekly?"2026-11-01":"2026-10-28");GameStore originalStore=store(crossing,old);
            finishEvents(originalStore,weekly);String originalToken=receipt(originalStore,weekly);long oldTime=old.time;old.set(weekly?"2026-11-02":"2026-10-29");long newTime=old.time;
            AtomicInteger reads=new AtomicInteger();GameStore midCall=new GameStore(crossing,null,()->reads.getAndIncrement()==0?oldTime:newTime);
            int prior=midCall.coins();check(claim(midCall,weekly)==amount&&midCall.coins()==prior+amount,"a boundary inside a claim keeps the one eligible captured period");
            check(Boolean.TRUE.equals(crossing.save().disk.get(originalToken))&&!crossing.save().disk.containsKey(receipt(midCall,weekly)),"mid-call rollover cannot charge old progress to the new period receipt");
        }
    }

    private static void pinnedGoals(){
        RewardsCosmeticsV17Test.Ctx c=context();RewardsCosmeticsV17Test.Clock k=clock("2026-10-28");GameStore s=store(c,k);
        check(s.trackedCollectionGoal().isEmpty()&&CollectionGoal.snapshot(s)==null,"upgrade/fresh profile starts without an invented goal");
        Map<String,?> before=c.save().getAll();
        for(String invalid:new String[]{null,"","hat_dew","cosmetic:","gear:","COSMETIC:hat_dew","cosmetic:../hat_dew","gear:hint","cosmetic:hat_dew:copy","gear:missing"})
            check(!s.setTrackedCollectionGoal(invalid),"only exact known typed catalog identities can be pinned");
        check(c.save().getAll().equals(before),"invalid targets do not touch the save");
        check(s.setTrackedCollectionGoal("gear:lantern_leaf"),"explicit Journey goal persists");
        CollectionGoal goal=CollectionGoal.snapshot(s);
        check(goal.id.equals("lantern_leaf")&&goal.kind==CollectionGoal.Kind.GEAR&&goal.slot==CollectionGoal.Slot.HEAD&&goal.route==CollectionGoal.Route.JOURNEY,"goal has typed item/slot and actual source route");
        check(goal.progress==0&&goal.target==8&&!goal.owned&&!goal.equipped&&!goal.completed,"tracking never acquires or equips an item");
        for(int level=1;level<=8;level++)s.completeLevel(level,3);
        goal=CollectionGoal.snapshot(s);check(goal.progress==8&&!goal.owned&&!goal.completed,"reaching a threshold is observed without an automatic unlock from rendering");
        s.claimAvatarGearUnlocks();goal=CollectionGoal.snapshot(s);
        check(goal.completed&&goal.owned&&!goal.equipped&&s.trackedCollectionGoal().equals("gear:lantern_leaf"),"explicit normal unlock completes but does not clear/equip the pin");
        check(s.equipAvatarGear(goal.id)&&CollectionGoal.snapshot(s).equipped,"normal equipment action is reflected in the snapshot");
        check(store(c.reboot(),k).trackedCollectionGoal().equals("gear:lantern_leaf"),"pin survives cold recreation");
        s.clearTrackedCollectionGoal();check(CollectionGoal.snapshot(s)==null&&store(c.reboot(),k).trackedCollectionGoal().isEmpty(),"explicit clear is durable");
        for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS){check(s.setTrackedCollectionGoal("cosmetic:"+item.id),"all24 cosmetic identities are supported");CollectionGoal next=CollectionGoal.snapshot(s);check(next.id.equals(item.id)&&next.kind==CollectionGoal.Kind.COSMETIC&&next.nameKey.equals(item.nameKey),"cosmetic metadata comes from the real catalog");}
        for(AvatarGearCatalog.Item item:AvatarGearCatalog.ITEMS){check(s.setTrackedCollectionGoal("gear:"+item.id),"all18 gear identities are supported");CollectionGoal next=CollectionGoal.snapshot(s);check(next.slot.name().equals(item.slot.name())&&next.nameKey.equals(item.nameKey),"gear metadata remains typed");}
        c.save().edit().putString("collection_goal_v110","cosmetic:removed-item").commit();Map<String,?> invalidRead=c.save().getAll();
        check(s.trackedCollectionGoal().isEmpty()&&CollectionGoal.snapshot(s)==null&&c.save().getAll().equals(invalidRead),"unknown stored identity is ignored without a render-time repair/write");
    }

    private static void goalSources(){
        RewardsCosmeticsV17Test.Ctx c=context();RewardsCosmeticsV17Test.Clock k=clock("2026-10-28");GameStore s=store(c,k);MetaProgress meta=new MetaProgress(c,k);
        s.setTrackedCollectionGoal("cosmetic:hat_moon");
        for(int day=0;day<8;day++){k.time+=86_400_000L;meta.recordVisit();}
        check(CollectionGoal.snapshot(s).progress==0&&CollectionGoal.snapshot(s).route==CollectionGoal.Route.REWARDS,"login goal counts claimed gifts, never mere visited days");
        s.claimDailyLogin();check(CollectionGoal.snapshot(s).progress==1&&!s.ownsCosmetic("hat_moon"),"one login claim is one real goal unit");
        k.time+=86_400_000L;s.claimDailyLogin();Map<String,?> before=c.save().getAll();check(CollectionGoal.snapshot(s).progress==2&&!CollectionGoal.snapshot(s).owned&&c.save().getAll().equals(before),"eligible cosmetic remains unclaimed during a clean snapshot");
        s.claimCosmeticUnlocks();check(CollectionGoal.snapshot(s).completed&&!CollectionGoal.snapshot(s).equipped,"normal acquisition completes the same explicit login target");
        s.setTrackedCollectionGoal("gear:crescent_knot");k.set("2026-10-28");s.completeWeekly(0,3);s.completeWeekly(1,3);
        check(CollectionGoal.snapshot(s).progress==2&&CollectionGoal.snapshot(s).route==CollectionGoal.Route.WEEKLY,"weekly goal reads lifetime weekly wins");
        k.set("2026-11-02");s.completeWeekly(0,3);check(CollectionGoal.snapshot(s).progress==3,"new week never resets cumulative item progress");
        s.setTrackedCollectionGoal("cosmetic:hat_pumpkin");CollectionGoal event=CollectionGoal.snapshot(s);
        check(event.route==CollectionGoal.Route.DAILY&&event.progress==9,"event star goal includes actual daily and weekly history");
        s.completeDaily(0,3);check(CollectionGoal.snapshot(s).progress==12&&!CollectionGoal.snapshot(s).completed,"event goal does not fabricate ownership at threshold");
        s.setTrackedCollectionGoal("gear:starfold_visor");CollectionGoal shop=CollectionGoal.snapshot(s);
        check(shop.route==CollectionGoal.Route.SHOP&&shop.priceCoins==600&&shop.target==600,"earned-coin target has a real shop price, not premium currency");
        int amount=Math.max(0,600-s.coins());if(amount>0)s.grantMilestone("v110:test:fund",amount);
        before=c.save().getAll();check(CollectionGoal.snapshot(s).canAfford&&!CollectionGoal.snapshot(s).completed&&c.save().getAll().equals(before),"affordability is an observation, not a purchase");
        s.purchaseAvatarGear("starfold_visor");check(CollectionGoal.snapshot(s).completed,"actual purchase, not a coin threshold, completes the target");
        GameStore external=new GameStore(c,new GameStore.ExternalOwnership(){public boolean ownsCharacter(String id){return false;}public boolean ownsSkin(String id){return false;}public boolean ownsCosmetic(String id){return id.equals("hat_cupid");}},k);
        external.setTrackedCollectionGoal("cosmetic:hat_cupid");CollectionGoal linked=CollectionGoal.snapshot(external);
        check(linked.completed&&linked.acquisitionSource.isEmpty(),"external ownership is respected without inventing a paid acquisition source");
    }

    private static void goalFailures(){
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=context();RewardsCosmeticsV17Test.Clock k=clock("2026-10-28");GameStore s=store(c,k);s.setTrackedCollectionGoal("gear:moonbud");
            c.save().fail(memory);saveFails(()->s.setTrackedCollectionGoal("cosmetic:hat_moon"),"pin disk refusal is typed and never false success");
            check(c.save().disk.get("collection_goal_v110").equals("gear:moonbud"),"pin failure leaves previous durable selection");
            c.save().fail(memory);saveFails(()->store(c,k).trackedCollectionGoal(),"another handle cannot present an undurable pin as acknowledged");
            check(store(c,k).trackedCollectionGoal().equals("cosmetic:hat_moon")&&store(c.reboot(),k).trackedCollectionGoal().equals("cosmetic:hat_moon"),"pin exact pending update retries durably across handles");
            c.save().fail(memory);saveFails(s::clearTrackedCollectionGoal,"clear failure is visible");
            c.save().fail(memory);saveFails(()->CollectionGoal.snapshot(store(c,k)),"goal snapshot surfaces a repeatedly refused clear flush");
            check(store(c,k).trackedCollectionGoal().isEmpty()&&store(c.reboot(),k).trackedCollectionGoal().isEmpty(),"recovered clear does not resurrect the previous target");
        }
    }

    private static void earnedGearDurability(){
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=context();RewardsCosmeticsV17Test.Clock k=clock("2026-10-28");GameStore s=store(c,k);
            s.claimAvatarGearUnlocks();s.setTrackedCollectionGoal("gear:lantern_leaf");for(int level=1;level<=8;level++)s.completeLevel(level,3);
            String token="avatar_owned_v1_lantern_leaf";int balance=s.coins();GameStore second=store(c,k);
            c.save().fail(memory);saveFails(s::claimAvatarGearUnlocks,"earned gear refusal is typed, never a successful grant list");
            check(!c.save().disk.containsKey(token)&&c.save().disk.get("coins").equals(balance),"earned gear failure changes neither ownership nor currency on disk");
            c.save().fail(memory);saveFails(second::claimAvatarGearUnlocks,"another handle must flush the exact refused gear transaction first");
            c.save().fail(memory);saveFails(()->CollectionGoal.snapshot(second),"pinned goal cannot advertise undurable memory ownership while the write is still refused");
            second.claimAvatarGearUnlocks();CollectionGoal recovered=CollectionGoal.snapshot(s);
            check(recovered.completed&&recovered.owned&&!recovered.equipped&&s.coins()==balance,"earned gear retry acknowledges exactly the original free ownership without auto-equipping or paying coins");
            check("JOURNEY_CLEARS".equals(c.save().disk.get(token))&&"JOURNEY_CLEARS".equals(s.avatarGearAcquisitionSource("lantern_leaf")),"existing source strings and receipt keys remain unchanged");
            GameStore cold=store(c.reboot(),k);check(cold.ownsAvatarGear("lantern_leaf")&&cold.claimAvatarGearUnlocks().isEmpty(),"recovered gear ownership is durable and never reported as a fresh grant twice");

            RewardsCosmeticsV17Test.Ctx interrupted=context();GameStore first=store(interrupted,k);first.claimAvatarGearUnlocks();for(int level=1;level<=8;level++)first.completeLevel(level,3);
            interrupted.save().fail(memory);saveFails(first::claimAvatarGearUnlocks,"cold gear setup refuses its first grant");
            GameStore restarted=store(interrupted.reboot(),k);check(!restarted.ownsAvatarGear("lantern_leaf"),"cold recreation before recovery exposes no partial durable gear");
            check(restarted.claimAvatarGearUnlocks().equals(Collections.singletonList("lantern_leaf"))&&restarted.ownsAvatarGear("lantern_leaf"),"uncommitted free gear remains earnable once after a cold recreation");
        }
    }

    private static boolean buyItem(GameStore store,int kind,String id){return kind==0?store.purchaseCharacter(id):kind==1?store.purchaseSkin(id):store.purchaseAvatarGear(id);}
    private static boolean equipItem(GameStore store,int kind,String id){return kind==0?store.equipCharacter(id):kind==1?store.equipSkin(id):store.equipAvatarGear(id);}
    private static boolean ownsItem(GameStore store,int kind,String id){return kind==0?store.ownsCharacter(id):kind==1?store.ownsSkin(id):store.ownsAvatarGear(id);}
    private static String equippedItem(GameStore store,int kind){return kind==0?store.equippedCharacter():kind==1?store.equippedSkin():store.avatarLoadout().headId;}
    private static void fundCollection(GameStore store,String key){for(int grant=0;grant<6;grant++)check(store.grantMilestone(key+grant,500)==500,"collection setup uses valid explicit earned-coin grants");}

    private static void collectionActionsDurability()throws Exception{
        String[] ids={"miko","lagoon","starfold_visor"},alternatives={"orbit","blossom","comet_halo"},slots={"character","skin","avatar_slot_v1_HEAD"};
        int[] prices={160,180,600};
        for(int kind=0;kind<ids.length;kind++)for(boolean memory:new boolean[]{false,true}){
            final int type=kind;String id=ids[kind];RewardsCosmeticsV17Test.Clock k=clock("2026-10-28");RewardsCosmeticsV17Test.Ctx c=context();GameStore s=store(c,k);
            fundCollection(s,"v110:test:collection-funds");int before=s.coins();GameStore second=store(c,k);
            String ownedKey=kind==0?"owned_character_"+id:kind==1?"owned_skin_"+id:"avatar_owned_v1_"+id;
            c.save().fail(memory);saveFails(()->buyItem(s,type,id),"collection purchase refusal raises the typed save error "+kind+" "+memory+" "+before);
            check(c.save().disk.get("coins").equals(before)&&!c.save().disk.containsKey(ownedKey),"purchase debit and ownership are both absent on refused disk commit");
            c.save().fail(memory);saveFails(()->buyItem(second,type,alternatives[type]),"another purchase cannot bypass a refused previous debit");
            check(!buyItem(second,type,id)&&ownsItem(s,type,id)&&s.coins()==before-prices[kind],"same purchase retry settles one exact transaction and preserves owned=false return contract");
            check(c.save().disk.get("coins").equals(before-prices[kind])&&c.save().disk.containsKey(ownedKey)&&!buyItem(s,type,id),"durable receipt prevents every repeated debit");
            GameStore boughtCold=store(c.reboot(),k);check(ownsItem(boughtCold,kind,id)&&boughtCold.coins()==before-prices[kind]&&!buyItem(boughtCold,kind,id),"purchase survives cold reconstruction with one charge");

            String oldSelection=equippedItem(s,kind);Map<String,Object> expected=new HashMap<>(c.save().disk);
            c.save().fail(memory);saveFails(()->equipItem(s,type,id),"collection equipment refusal raises the typed save error");
            check(c.save().disk.equals(expected),"refused equip preserves every durable slot and coin balance");
            c.save().fail(memory);saveFails(()->equipItem(second,type,id),"second handle cannot acknowledge a repeatedly refused slot change");
            check(equipItem(second,kind,id)&&equippedItem(s,kind).equals(id)&&s.coins()==before-prices[kind],"exact equipment retry selects the owned item without another charge");
            expected.put(slots[kind],id);check(c.save().disk.equals(expected),"equipment writes only its own slot, preserving other slots and ownership");
            check(equippedItem(store(c.reboot(),k),kind).equals(id)&&equipItem(s,kind,id),"equipment survives cold reconstruction and an idempotent repeat");

            RewardsCosmeticsV17Test.Ctx interrupted=context();GameStore first=store(interrupted,k);fundCollection(first,"v110:test:cold-funds");int coldBefore=first.coins();
            interrupted.save().fail(memory);saveFails(()->buyItem(first,type,id),"cold purchase setup refuses first transaction");RewardsCosmeticsV17Test.Ctx restartContext=interrupted.reboot();GameStore restarted=store(restartContext,k);
            check(!ownsItem(restarted,kind,id)&&restarted.coins()==coldBefore&&buyItem(restarted,kind,id)&&restarted.coins()==coldBefore-prices[kind],"restart before purchase recovery retains one legitimate unpurchased item and balance");
            RewardsCosmeticsV17Test.Ctx equipColdContext=restartContext.reboot();GameStore equipFirst=store(equipColdContext,k);equipColdContext.save().fail(memory);
            saveFails(()->equipItem(equipFirst,type,id),"cold equipment setup refuses first transaction");GameStore equipRestarted=store(equipColdContext.reboot(),k);
            check(equippedItem(equipRestarted,kind).equals(oldSelection)&&ownsItem(equipRestarted,kind,id)&&equipItem(equipRestarted,kind,id),"restart before equipment recovery preserves prior slot and retains owned target");
        }
        for(int kind=0;kind<ids.length;kind++){
            final int type=kind;RewardsCosmeticsV17Test.Ctx c=context();RewardsCosmeticsV17Test.Clock k=clock("2026-10-28");GameStore s=store(c,k);fundCollection(s,"v110:test:concurrent-collection");int before=s.coins();
            CountDownLatch start=new CountDownLatch(1);AtomicInteger bought=new AtomicInteger(),errors=new AtomicInteger();List<Thread> threads=new ArrayList<>();
            for(int i=0;i<12;i++){GameStore handle=store(c,k);Thread thread=new Thread(()->{try{start.await();if(buyItem(handle,type,ids[type]))bought.incrementAndGet();}catch(Throwable failure){errors.incrementAndGet();}});threads.add(thread);thread.start();}
            start.countDown();for(Thread thread:threads)thread.join();check(errors.get()==0&&bought.get()==1&&s.coins()==before-prices[kind],"concurrent collection handles debit one catalog purchase only");
        }
        RewardsCosmeticsV17Test.Ctx externalContext=context();RewardsCosmeticsV17Test.Clock k=clock("2026-10-28");GameStore external=new GameStore(externalContext,new GameStore.ExternalOwnership(){public boolean ownsCharacter(String id){return id.equals("miko");}public boolean ownsSkin(String id){return id.equals("lagoon");}},k);
        int before=external.coins();check(!external.purchaseCharacter("miko")&&!external.purchaseSkin("lagoon")&&external.coins()==before,"external character and theme ownership blocks a duplicate local purchase");
        check(external.equipCharacter("miko")&&external.equipSkin("lagoon")&&external.equippedCharacter().equals("miko")&&external.equippedSkin().equals("lagoon"),"externally owned character and theme remain equipable with existing ownership policy");
        check(!externalContext.save().disk.containsKey("owned_character_miko")&&!externalContext.save().disk.containsKey("owned_skin_lagoon"),"external equipment does not fabricate a local acquisition receipt");
        Map<String,?> clean=externalContext.save().getAll();check(!external.purchaseCharacter("missing")&&!external.purchaseSkin("missing")&&!external.purchaseAvatarGear("missing")&&!external.equipCharacter("missing")&&!external.equipSkin("missing")&&!external.equipAvatarGear("missing"),"invalid collection IDs remain refused");
        check(!external.purchaseCharacter("zig")&&!external.purchaseSkin("aurora")&&!external.purchaseAvatarGear("starfold_visor")&&!external.equipAvatarGear("starfold_visor")&&clean.equals(externalContext.save().getAll()),"insufficient balance and unowned equipment do not mutate the save");
    }

    private static void dashboards(){
        RewardsCosmeticsV17Test.Ctx c=context();RewardsCosmeticsV17Test.Clock k=clock("2026-10-28");GameStore s=store(c,k);MetaProgress meta=new MetaProgress(c,k);
        Map<String,?> save=c.save().getAll(),progress=c.meta().getAll();ObjectiveDashboard.Snapshot fresh=ObjectiveDashboard.snapshot(s,meta);
        check(fresh.readyCount==2&&fresh.dailyReadyCount==1&&fresh.weeklyReadyCount==0&&fresh.trialsReadyCount==1&&fresh.eventReadyCount==0,"fresh dashboard shows actual login and one explicit hint sample only");
        check(fresh.dailyQuests.size()==3&&fresh.weeklyQuests.size()==3&&fresh.trials.size()==6,"six real typed quests and all catalog samples are present");
        check(fresh.nextAction.kind==ObjectiveDashboard.ActionKind.CLAIM_LOGIN&&fresh.loginCoins==40,"next action has actual existing login reward");
        check(c.save().getAll().equals(save)&&c.meta().getAll().equals(progress),"clean dashboard rendering creates no reward, visit, receipt or stock");
        boolean immutable=false;try{fresh.dailyQuests.clear();}catch(UnsupportedOperationException expected){immutable=true;}check(immutable,"quest collections are immutable snapshots");
        s.claimDailyLogin();s.claimPowerTrial("hint");check(ObjectiveDashboard.snapshot(s,meta).nextAction.kind==ObjectiveDashboard.ActionKind.PLAY_JOURNEY,"after explicit claims fresh guidance chooses a genuinely unseen Journey board");
        for(int level=1;level<=75;level++){s.completeLevel(level,3);meta.recordWin(0,level,3,null);}
        for(int index=0;index<3;index++){s.completeDaily(index,3);meta.recordWin(1,index,3,s.dailyId());}
        for(int index=0;index<7;index++){s.completeWeekly(index,3);meta.recordWin(2,index,3,s.weeklyId());}
        ObjectiveDashboard.Snapshot full=ObjectiveDashboard.snapshot(s,meta);
        check(full.dailyQuestReadyCount==3&&full.weeklyQuestReadyCount==3&&full.eventReadyCount==2&&full.trialsReadyCount==5&&full.journeyReady,"real progress exposes existing earned claims without changing them");
        check(full.readyCount==14&&full.dailyReadyCount==4&&full.weeklyReadyCount==4,"ready total counts visible distinct actions, including one oldest Journey gift");
        for(ObjectiveDashboard.Quest quest:full.dailyQuests)check(quest.ready&&!quest.claimed&&quest.progress==quest.target&&quest.coins==meta.missionCoins(quest.index),"daily quest carries real progress/target/reward/state");
        for(ObjectiveDashboard.Quest quest:full.weeklyQuests)check(quest.ready&&quest.weekly&&quest.period.equals(meta.weekId())&&quest.coins==meta.weeklyMissionCoins(quest.index),"weekly quest keeps its own period and existing amount");
        save=c.save().getAll();progress=c.meta().getAll();ObjectiveDashboard.snapshot(s,meta);check(save.equals(c.save().getAll())&&progress.equals(c.meta().getAll()),"completed dashboard never claims automatically");
        for(int index=0;index<3;index++){meta.claimMission(index,s);meta.claimWeeklyMission(index,s);}s.claimDailyCompletionReward();s.claimWeeklyCompletionReward();
        for(Catalog.BoosterItem item:Catalog.BOOSTERS)if(s.powerTrialAvailable(item.id))s.claimPowerTrial(item.id);
        while(s.completedLevels()>=s.nextLootMilestone()&&s.nextLootMilestone()>0)s.claimMilestoneLoot();
        check(ObjectiveDashboard.snapshot(s,meta).readyCount==0,"every real explicit claim removes only its own readiness");
        k.set("2026-10-29");ObjectiveDashboard.Snapshot tomorrow=ObjectiveDashboard.snapshot(s,meta);
        check(tomorrow.dailyQuestReadyCount==0&&tomorrow.weeklyQuestReadyCount==0&&tomorrow.loginDailyReady&&!tomorrow.dailyEventReady&&tomorrow.weeklyQuests.get(0).claimed,"midnight refreshes daily objectives while preserving same-week receipts");
        k.set("2026-11-02");ObjectiveDashboard.Snapshot monday=ObjectiveDashboard.snapshot(s,meta);
        check(!monday.weeklyQuests.get(0).claimed&&monday.weeklyQuests.get(0).progress==0&&!monday.weeklyEventReady&&monday.weekId.equals("2026-11-02"),"Monday starts fresh weekly objectives without changing ownership");
        s.setTrackedCollectionGoal("gear:crescent_knot");check(CollectionGoal.snapshot(s).progress==3,"calendar-bound dashboard never resets lifetime collection goals");

        RewardsCosmeticsV17Test.Ctx failed=context();GameStore fs=store(failed,k);MetaProgress fm=new MetaProgress(failed,k);finishEvents(fs,false);failed.save().fail(true);
        saveFails(fs::claimDailyCompletionReward,"dashboard pending-write setup");failed.save().fail(true);
        saveFails(()->ObjectiveDashboard.snapshot(fs,fm),"snapshot cannot conceal a refused prior event reward flush");
        check(ObjectiveDashboard.snapshot(fs,fm).dailyEventReady==false&&store(failed.reboot(),k).dailyBonusClaimed(),"successful read retry reports the actually durable event receipt");
    }

    public static void main(String[] args)throws Exception{
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));completionDurability();completionCalendar();pinnedGoals();goalSources();goalFailures();earnedGearDurability();collectionActionsDurability();dashboards();
        String report="{\n  \"passed\": true,\n  \"assertions\": "+checks+",\n  \"event_bonus_modes\": 2,\n  \"catalog_goal_items\": 42,\n  \"concurrent_bonus_claimers_per_category\": 24,\n  \"collection_action_categories\": 3,\n  \"concurrent_collection_buyers_per_category\": 12,\n  \"scope\": \"Actual GameStore, CollectionGoal, ObjectiveDashboard and MetaProgress on a JVM with Android memory-before-disk preference doubles; no native UI, provider or retention claim.\"\n}\n";
        if(args.length>0){Path path=Path.of(args[0]);Files.createDirectories(path.getParent());Files.writeString(path,report);}
        System.out.println("PASS "+checks+" v1.10 objective, pin, event bonus and durability checks");
    }
}
