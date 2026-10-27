import android.content.Context;
import android.content.SharedPreferences;
import com.androidhit.lumadrift.data.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** Real-store JVM checks including Android's memory-before-disk failure behavior. */
public final class RewardsCosmeticsV17Test {
    static int checks;
    static void check(boolean good,String message) { checks++;if(!good)throw new AssertionError(message); }
    static final class Prefs implements SharedPreferences {
        final Map<String,Object> values=new HashMap<>(), disk=new HashMap<>();
        boolean fail,applyBeforeFailure;
        public Map<String,?> getAll(){return new HashMap<>(values);}
        public boolean contains(String k){return values.containsKey(k);}
        public int getInt(String k,int d){return (Integer)values.getOrDefault(k,d);}
        public String getString(String k,String d){return (String)values.getOrDefault(k,d);}
        public boolean getBoolean(String k,boolean d){return (Boolean)values.getOrDefault(k,d);}
        public Editor edit(){return new Editor(){final Map<String,Object> write=new HashMap<>();
            public Editor putInt(String k,int v){write.put(k,v);return this;}
            public Editor putString(String k,String v){write.put(k,v);return this;}
            public Editor putBoolean(String k,boolean v){write.put(k,v);return this;}
            public boolean commit(){if(fail){fail=false;if(applyBeforeFailure)values.putAll(write);return false;}
                values.putAll(write);disk.putAll(write);return true;}
        };}
        void fail(boolean applied){fail=true;applyBeforeFailure=applied;}
        Prefs reboot(){Prefs p=new Prefs();p.values.putAll(disk);p.disk.putAll(disk);return p;}
    }
    static final class Ctx extends Context {
        final Map<String,Prefs> files=new HashMap<>();
        public SharedPreferences getSharedPreferences(String n,int m){return files.computeIfAbsent(n,k->new Prefs());}
        Prefs save(){return files.get("luma_drift_save_v1");}
        Prefs meta(){return files.get("luma_drift_meta_v1");}
        Ctx reboot(){Ctx c=new Ctx();files.forEach((k,v)->c.files.put(k,v.reboot()));return c;}
    }
    static final class Clock implements GameStore.Clock,MetaProgress.Clock {
        long time;
        Clock(String day){set(day);}
        void set(String day){try{time=new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).parse(day).getTime()+43_200_000;}catch(Exception e){throw new AssertionError(e);}}
        public long now(){return time;}
    }
    static GameStore store(Ctx c,Clock clock){return new GameStore(c,null,clock);}
    static void throwsSave(Runnable action,String label){boolean threw=false;try{action.run();}catch(IllegalStateException expected){threw=true;}check(threw,label);}
    static void login(){
        Ctx c=new Ctx();Clock clock=new Clock("2026-10-26");GameStore s=store(c,clock);
        check(s.coins()==180&&s.dailyLoginCount()==0&&s.dailyLoginDay()==1&&!s.dailyLoginClaimed(),"fresh login does not invent currency");
        int total=180;
        for(int i=0;i<15;i++){
            // Gaps alternate deliberately. There is no consecutive-day requirement.
            clock.time+=86_400_000L*(i%3+1);
            int day=i%7+1,amount=s.dailyLoginCoins(day);
            check(s.dailyLoginDay()==day,"cycle next day "+i);
            check(s.claimDailyLogin()==amount,"correct login amount "+i);total+=amount;
            check(s.coins()==total&&s.dailyLoginClaimed()&&s.dailyLoginCount()==i+1,"one persisted visit "+i);
            check(s.dailyLoginDay()==day&&s.claimDailyLogin()==0&&s.coins()==total,"duplicate idempotent "+i);
            c=c.reboot();s=store(c,clock);
            check(s.coins()==total&&s.dailyLoginCount()==i+1,"cycle survives process recreation "+i);
        }
    }
    static void loginRollover(){
        Ctx c=new Ctx();Clock k=new Clock("2026-12-31");GameStore s=store(c,k);
        check(s.claimDailyLogin()==40,"first claimed visit");
        k.set("2027-01-07");check(s.dailyLoginDay()==2&&s.claimDailyLogin()==50,"absence carries cycle across year");
        k.set("2026-12-30");check(s.dailyLoginClaimed()&&s.claimDailyLogin()==0,"rollback cannot revisit earlier unclaimed date");
        k.set("2027-01-08");check(s.claimDailyLogin()==60,"next future date resumes unchanged");
        Ctx reboot=c.reboot();GameStore after=store(reboot,k);check(after.coins()==330&&after.dailyLoginCount()==3,"login persisted across cold recreation");
        check(after.dailyLoginCoins(0)==0&&after.dailyLoginCoins(8)==0,"invalid cycle indexes rejected");
    }
    static void failures(){
        for(boolean memory:new boolean[]{false,true}){
            Ctx c=new Ctx();Clock k=new Clock("2026-10-26");GameStore s=store(c,k);c.save().fail(memory);
            throwsSave(()->s.claimDailyLogin(),"login failure surfaced "+memory);
            check(c.save().disk.get("coins").equals(180)&&!c.save().disk.containsKey("login_latest_v17"),"no partial login on disk "+memory);
            GameStore second=store(c,k);check(second.claimDailyLogin()==0,"retry flushes original login without double return "+memory);
            check(store(c.reboot(),k).coins()==220&&second.dailyLoginCount()==1,"login retry exact durable reward "+memory);
            // Direct purchase, equip and loot are equally atomic.
            second.grantMilestone("funding",500);int before=second.coins();c.save().fail(memory);
            throwsSave(()->second.purchaseCosmetic("hat_cupid"),"purchase failure surfaced "+memory);
            check(second.ownsCosmetic("hat_cupid")&&!second.purchaseCosmetic("hat_cupid"),"purchase retry retains one ownership "+memory);
            check(second.coins()==before-450&&store(c.reboot(),k).ownsCosmetic("hat_cupid"),"purchase exact one debit "+memory);
            c.save().fail(memory);throwsSave(()->second.equipCosmetic("hat_cupid"),"equip failure surfaced "+memory);
            check(second.equippedCosmetic(CosmeticCatalog.Slot.HAT).equals("hat_cupid"),"equip recovered "+memory);
            second.grantMilestone("funding2",500);int coins=second.coins();c.save().fail(memory);
            throwsSave(()->second.drawCosmeticLoot("request1"),"draw failure surfaced "+memory);
            GameStore.LootResult result=store(c,k).drawCosmeticLoot("request1");
            check(result!=null&&result.replayed&&result.chargedCoins==0,"draw replay has no second debit "+memory);
            check(second.coins()==coins-150+result.compensationCoins,"draw compensation exact "+memory);
            check(store(c.reboot(),k).drawCosmeticLoot("request1").itemId.equals(result.itemId),"draw receipt survives cold restart "+memory);
        }
        // A process killed immediately after a failed write cannot expose partial durable rewards.
        Ctx c=new Ctx();Clock k=new Clock("2026-10-26");GameStore s=store(c,k);c.save().fail(true);
        throwsSave(()->s.claimDailyLogin(),"cold failure setup");Ctx boot=c.reboot();GameStore fresh=store(boot,k);
        check(fresh.claimDailyLogin()==40&&fresh.coins()==220,"cold retry from old durable save awards once");
    }
    static void wardrobe(){
        Ctx c=new Ctx();Clock k=new Clock("2026-10-26");GameStore s=store(c,k);
        check(CosmeticCatalog.ITEMS.size()==24&&s.ownedCosmeticCount()==4,"24 cosmetics with four free starters");
        int total=0;Set<String> ids=new HashSet<>();int[] rarity=new int[3];
        for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS){
            check(ids.add(item.id),"unique cosmetic "+item.id);rarity[item.rarity.ordinal()]++;total+=item.chanceBasisPoints();
            for(String lang:new String[]{"en","fr","de","es","pt","it"})check(V17Strings.has(item.nameKey)&&!V17Strings.get(lang,item.nameKey).equals(item.nameKey),"translated cosmetic "+item.id+lang);
            if(item.source!=CosmeticCatalog.Source.STARTER)check(!s.equipCosmetic(item.id),"unowned cosmetic cannot equip "+item.id);
        }
        check(total==10_000&&Arrays.equals(rarity,new int[]{8,8,8}),"public individual probabilities sum to 100 percent");
        for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())check(s.equippedCosmetic(slot).equals(CosmeticCatalog.starter(slot).id),"starter fallback "+slot);
        check(!s.ownsCosmetic("unknown")&&!s.equipCosmetic("unknown")&&!s.purchaseCosmetic("unknown")&&s.equippedCosmetic(null).isEmpty(),"unknown item safety");
        check(!s.purchaseCosmetic("hat_cupid")&&!s.purchaseCosmetic("hat_moon"),"no underfunded or earned-unlock purchase");
        for(int i=0;i<10;i++){s.claimDailyLogin();k.time+=86_400_000L;}
        List<String> earned=s.claimCosmeticUnlocks();check(earned.size()==4&&s.ownedCosmeticCount()==8,"ten visits unlock four moon cosmetics");
        check(s.claimCosmeticUnlocks().isEmpty(),"unlock claims idempotent");
        for(int i=0;i<7;i++)s.completeWeekly(i,3);
        for(int i=0;i<3;i++)s.completeDaily(i,3);
        s.claimCosmeticUnlocks();check(s.ownsCosmetic("hat_star")&&s.ownsCosmetic("dress_star")&&s.ownsCosmetic("shoes_star")&&!s.ownsCosmetic("gloves_star"),"weekly lifetime wins threshold exact");
        check(s.ownsCosmetic("hat_pumpkin")&&s.ownsCosmetic("dress_valentine")&&s.ownsCosmetic("shoes_pumpkin")&&s.ownsCosmetic("gloves_valentine"),"event archive unlocks seasonal designs");
        k.time+=7*86_400_000L;s.completeWeekly(0,1);s.claimCosmeticUnlocks();check(s.ownsCosmetic("gloves_star"),"weekly source accumulates across rotations");
        int bonus=s.avatarLoadout().winBonusCoins(),discount=s.avatarLoadout().packDiscountCoins(),grace=s.avatarLoadout().blockedGracePerRun();
        for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS)if(s.ownsCosmetic(item.id))s.equipCosmetic(item.id);
        check(s.avatarLoadout().winBonusCoins()==bonus&&s.avatarLoadout().packDiscountCoins()==discount&&s.avatarLoadout().blockedGracePerRun()==grace,"new wardrobe has no gameplay effects");
        k.set("2030-02-15");GameStore after=store(c.reboot(),k);check(after.ownsCosmetic("hat_pumpkin")&&after.ownsCosmetic("dress_valentine"),"seasonal ownership never expires");
        int coins=after.coins();check(after.purchaseCosmetic("hat_aurora")&&after.coins()==coins-650,"direct purchase explicit earned-coin price");
        check(!after.purchaseCosmetic("hat_aurora")&&after.coins()==coins-650,"duplicate shop buy no debit");
    }
    static void loot(){
        int[] counts=new int[3];for(int i=0;i<100_000;i++){
            CosmeticCatalog.Item item=CosmeticCatalog.draw("fixed-seed","draw:"+i);counts[item.rarity.ordinal()]++;
            check(item.id.equals(CosmeticCatalog.draw("fixed-seed","draw:"+i).id),"outcome deterministic "+i);
        }
        check(Math.abs(counts[0]-60_000)<600&&Math.abs(counts[1]-30_000)<600&&Math.abs(counts[2]-10_000)<400,"100k draw distribution matches declared probabilities "+Arrays.toString(counts));
        Ctx c=new Ctx();Clock k=new Clock("2026-10-26");GameStore s=store(c,k);
        c.save().edit().putInt("coins",100_000).putString("cosmetic_seed_v17","fixed-seed").commit();
        for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS)c.save().edit().putString("cosmetic_owned_v17_"+item.id,"TEST").commit();
        for(int i=0;i<100;i++){
            int before=s.coins();GameStore.LootResult r=s.drawCosmeticLoot("draw:"+i);CosmeticCatalog.Item item=CosmeticCatalog.find(r.itemId);
            check(r.duplicate&&r.compensationCoins==item.duplicateCoins()&&r.chargedCoins==150,"duplicate compensation explicit "+i);
            check(s.coins()==before-150+r.compensationCoins,"one draw exact balance "+i);
            int after=s.coins();check(s.drawCosmeticLoot("draw:"+i).replayed&&s.coins()==after,"receipt replay no reroll/debit "+i);
        }
        check(s.drawCosmeticLoot("bad/id")==null&&s.drawCosmeticLoot(null)==null,"invalid receipt cannot spend");
        c.save().edit().putInt("coins",149).commit();check(s.drawCosmeticLoot("poor")==null&&s.coins()==149,"unfunded draw no receipt or debit");
        s.grantMilestone("topup",1);check(s.drawCosmeticLoot("poor")!=null,"unfunded request can retry after earning coins");
        System.out.println("DRAW_SIMULATION 100000 outcomes: common="+counts[0]+", rare="+counts[1]+", epic="+counts[2]);
    }
    static void quests(){
        Ctx c=new Ctx();Clock k=new Clock("2026-10-26");GameStore s=store(c,k);MetaProgress m=new MetaProgress(c,k);
        check(m.weeklyMissionTarget(0)==10&&m.weeklyMissionTarget(1)==24&&m.weeklyMissionTarget(2)==3,"weekly quests differ from daily");
        check(m.claimWeeklyMission(0,s)==0&&!m.weeklyMissionComplete(-1)&&m.weeklyMissionProgress(3)==0,"invalid/incomplete weekly quests cannot pay");
        for(int i=1;i<=7;i++)m.recordWin(0,i,3,null);
        m.recordWin(1,0,3,"2026-10-26");m.recordWin(1,1,3,"2026-10-26");m.recordWin(2,0,3,"2026-10-26");
        int before=s.coins();for(int i=0;i<3;i++){
            check(m.weeklyMissionComplete(i),"weekly completed "+i);
            check(m.claimWeeklyMission(i,s)==m.weeklyMissionCoins(i)&&m.weeklyMissionClaimed(i),"weekly claim "+i);
            check(m.claimWeeklyMission(i,s)==0,"weekly duplicate "+i);
        }
        check(s.coins()==before+530,"weekly rewards exact, separate from daily rewards");
        k.set("2026-10-27");check(m.missionProgress(0)==0&&m.weeklyMissionProgress(0)==10&&m.weeklyMissionClaimed(0),"daily rollover retains weekly state");
        k.set("2026-11-02");check(m.weeklyMissionProgress(0)==0&&!m.weeklyMissionClaimed(0)&&m.claimWeeklyMission(0,s)==0,"Monday rotates weekly quests without erasing coins");
        check(m.recordWin(0,1,3,null)==0&&m.weeklyMissionProgress(0)==0,"old Journey wins cannot farm new week quests");
        k.set("2026-10-26");check(m.weeklyMissionClaimed(0)&&m.claimWeeklyMission(0,s)==0,"return to old week cannot reclaim");
        // Wallet write failure, both Android and simple adapters.
        for(boolean memory:new boolean[]{false,true}){
            Ctx f=new Ctx();GameStore wallet=store(f,k);MetaProgress meta=new MetaProgress(f,k);
            for(int i=1;i<=10;i++)meta.recordWin(0,i,3,null);
            f.save().fail(memory);throwsSave(()->meta.claimWeeklyMission(0,wallet),"weekly wallet failure surfaced "+memory);
            check(!meta.weeklyMissionClaimed(0),"failed wallet does not mark meta claim "+memory);
            meta.claimWeeklyMission(0,wallet);check(wallet.coins()==330&&meta.weeklyMissionClaimed(0),"weekly wallet retry exactly once "+memory);
            Ctx f2=new Ctx();GameStore w2=store(f2,k);MetaProgress m2=new MetaProgress(f2,k);for(int i=1;i<=10;i++)m2.recordWin(0,i,3,null);
            f2.meta().fail(memory);throwsSave(()->m2.claimWeeklyMission(0,w2),"weekly meta failure surfaced "+memory);
            check(w2.coins()==330,"wallet paid before failed meta save "+memory);
            MetaProgress recovered=new MetaProgress(f2,k);check(recovered.claimWeeklyMission(0,w2)==0&&recovered.weeklyMissionClaimed(0)&&w2.coins()==330,"meta claim recovery cannot duplicate "+memory);
        }
        Ctx f=new Ctx();MetaProgress meta=new MetaProgress(f,k);f.meta().fail(true);
        throwsSave(()->meta.recordWin(0,1,3,null),"first clear meta memory-before-disk failure surfaced");
        check(meta.recordWin(0,1,3,null)==0&&meta.completedBoards()==1&&meta.weeklyMissionProgress(0)==1,"first-clear retry flushes progress once");
    }
    static void gifts(){
        String id=String.join("",Collections.nCopies(64,"a"));Ctx c=new Ctx();Clock k=new Clock("2026-10-26");GameStore s=store(c,k);int coins=s.coins();
        check(s.claimSocialGift("uid-one",id)&&s.boosterStock("hint")==4,"social gift grants exactly one hint");
        check(!s.claimSocialGift("uid-one",id.toUpperCase(Locale.ROOT))&&s.boosterStock("hint")==4,"same case-normalized gift cannot duplicate");
        check(s.claimSocialGift("uid-two",id)&&s.boosterStock("hint")==5,"account-specific gifts have independent receipts");
        check(!store(c.reboot(),k).claimSocialGift("uid-two",id)&&s.coins()==coins,"gift receipt survives reboot without currency conversion");
        check(!s.claimSocialGift("",id)&&!s.claimSocialGift("  ",id)&&!s.claimSocialGift("a\nb\nc",id)&&!s.claimSocialGift("uid","bad"),"malformed gift identifiers rejected");
        for(boolean memory:new boolean[]{false,true}){
            Ctx f=new Ctx();GameStore w=store(f,k);f.save().fail(memory);throwsSave(()->w.claimSocialGift("uid",id),"gift failure surfaced "+memory);
            check(!store(f,k).claimSocialGift("uid",id)&&w.boosterStock("hint")==4,"gift retry exactly once "+memory);
            check(store(f.reboot(),k).boosterStock("hint")==4,"gift recovery durable "+memory);
        }
        Ctx mixed=new Ctx();GameStore power=store(mixed,k);mixed.save().fail(true);
        throwsSave(()->power.claimSocialGift("uid",id),"gift failure before an old power path");
        check(power.useBooster("hint")&&power.boosterStock("hint")==3,"old power use flushes pending gift before spending");
        check(!power.claimSocialGift("uid",id)&&store(mixed.reboot(),k).boosterStock("hint")==3,"gift retry cannot restore consumed hint");
        c.save().edit().putInt("booster_hint",999).commit();check(!s.claimSocialGift("uid-three",id)&&s.boosterStock("hint")==999,"full inventory does not burn receipt");
        c.save().edit().putInt("booster_hint",998).commit();check(s.claimSocialGift("uid-three",id)&&s.boosterStock("hint")==999,"full-inventory gift can retry later");
    }
    static void copy(){
        String[] keys={"daily_login","login_note","login_day","claim","claimed","daily_quests","weekly_quests","quest_boards","quest_stars","quest_events","quest_note","wardrobe","cosmetic_only","slot_hat","slot_dress","slot_shoes","slot_gloves","common","rare","epic","owned","locked","equipped","equip","capsule","capsule_note","capsule_odds","capsule_item_odds","capsule_duplicates","open_capsule","not_enough_coins","source_starter","source_login","source_weekly","source_event","source_coins","source_capsule","new_cosmetic","duplicate","coins_note","gold_note"};
        for(String key:keys)for(String lang:new String[]{"en","fr","de","es","pt","it"})check(V17Strings.has(key)&&!V17Strings.get(lang,key).equals(key),"translated UI "+key+lang);
        check(V17Strings.format("fr","login_day",3).contains("3")&&V17Strings.get("unknown","claim").equals("Claim"),"format and English fallback");
    }
    static void concurrentClaims(){
        Ctx c=new Ctx();Clock clock=new Clock("2026-10-26");GameStore initial=store(c,clock);
        GameStore[] handles=new GameStore[12];MetaProgress[] metas=new MetaProgress[12];
        for(int i=0;i<handles.length;i++){handles[i]=store(c,clock);metas[i]=new MetaProgress(c,clock);}
        List<Throwable> failures=Collections.synchronizedList(new ArrayList<>());
        int[] rewards=new int[handles.length];List<Thread> threads=new ArrayList<>();
        for(int i=0;i<handles.length;i++){final int at=i;Thread t=new Thread(()->{try{rewards[at]=handles[at].claimDailyLogin();metas[at].recordWin(0,1,3,null);}catch(Throwable e){failures.add(e);}});threads.add(t);t.start();}
        for(Thread t:threads){try{t.join(5000);}catch(InterruptedException e){throw new AssertionError(e);}check(!t.isAlive(),"concurrent claim terminates without lock inversion");}
        check(failures.isEmpty(),"concurrent claims do not throw "+failures);
        check(Arrays.stream(rewards).sum()==40&&initial.coins()==220&&initial.dailyLoginCount()==1,"independent handles claim only one daily reward");
        check(metas[0].completedBoards()==1&&metas[0].weeklyMissionProgress(0)==1,"independent meta handles award one first-clear credit");
        initial.grantMilestone("concurrentfund",500);int before=initial.coins();
        List<GameStore.LootResult> results=Collections.synchronizedList(new ArrayList<>());threads.clear();
        for(GameStore handle:handles){Thread t=new Thread(()->{try{results.add(handle.drawCosmeticLoot("shared-request"));}catch(Throwable e){failures.add(e);}});threads.add(t);t.start();}
        for(Thread t:threads){try{t.join(5000);}catch(InterruptedException e){throw new AssertionError(e);}check(!t.isAlive(),"concurrent capsule terminates");}
        check(failures.isEmpty()&&results.size()==handles.length,"all concurrent draw receipts returned");
        int debits=0;String item=results.get(0).itemId;for(GameStore.LootResult result:results){debits+=result.chargedCoins;check(result.itemId.equals(item),"concurrent draw cannot reroll");}
        check(debits==150&&initial.coins()==before-150+results.get(0).compensationCoins,"concurrent draw exact one debit and compensation");
    }
    public static void main(String[] args){TimeZone.setDefault(TimeZone.getTimeZone("UTC"));login();loginRollover();failures();wardrobe();loot();quests();gifts();copy();concurrentClaims();System.out.println("PASS "+checks+" v1.7 rewards, wardrobe, earned capsules, quests and gift persistence checks");}
}
