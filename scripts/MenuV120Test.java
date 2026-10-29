import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.ui.*;
import java.nio.file.*;
import java.util.*;

/** Current layout and actual claim receipts. No native-rendering or provider claim. */
public final class MenuV120Test {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static void near(float a,float b,String message){check(Math.abs(a-b)<.002f,message+": "+a+" / "+b);}
    private static void geometry(){
        for(int widthDp:new int[]{240,280,320,360,390,480,600})for(int heightDp:new int[]{360,568,640,720,900,1200})for(float density:new float[]{1,1.5f,2,2.625f,3,4})for(float font:new float[]{1,1.3f}){
            ExperienceLayout physical=new ExperienceLayout(Math.round(widthDp*density),Math.round(heightDp*density),0,0,density,font);
            float touch=48*density/physical.scale,hud=Menu120Layout.hudHeight(touch),viewport=Math.max(180,physical.viewportHeight-hud);
            Menu120Layout menu=new Menu120Layout(viewport,touch);
            Menu120Layout.Rect[] controls={menu.profile,menu.settings,menu.team,menu.play,menu.gifts,menu.shop,menu.map,menu.expedition,menu.events,menu.room,menu.atelier};
            for(int i=0;i<controls.length;i++){
                Menu120Layout.Rect r=controls[i];check(r.left>=0&&r.right<=390&&r.top>=0&&r.bottom<menu.contentHeight,"all menu controls in measured content");
                check(r.width()*physical.scale/density>=47.99f&&r.height()*physical.scale/density>=47.99f,"48dp physical targets");
                for(int j=i+1;j<controls.length;j++)check(!r.intersects(controls[j]),"independent actual centre targets "+i+"/"+j);
                float maximum=Math.max(0,menu.contentHeight-viewport),scroll=Math.max(0,Math.min(maximum,r.centerY()-viewport/2));
                float view=Menu120Layout.viewY(r.centerY(),scroll,hud);
                check(view>=hud&&view<physical.viewportHeight-77,"control centre reachable outside wallet and fixed nav");
                near(Menu120Layout.contentY(view,scroll,hud),r.centerY(),"scroll/HUD inverse");
                float pixels=16+view*physical.scale;near((pixels-16)/physical.scale,view,"touch and AX physical transform");
            }
            for(int i=0;i<3;i++){
                Menu120Layout.Rect r=Menu120Layout.wallet(i,touch);check(r.top>=0&&r.bottom<=hud&&r.left>=0&&r.right<=390,"fixed HUD in reserved band");
                check(r.height()*physical.scale/density>=48,"HUD physical touch floor");
                for(int j=i+1;j<3;j++)check(!r.intersects(Menu120Layout.wallet(j,touch)),"wallet target independence");
            }
            check(menu.contentHeight>=menu.atelier.bottom+Menu120Layout.NAV_RESERVE,"secondary places clear fixed nav");
            for(int i=0;i<5;i++){
                Menu120Layout.Rect nav=Menu120Layout.navigation(i,viewport,touch);check(nav.width()*physical.scale/density>=47.999f&&nav.height()*physical.scale/density>=47.999f,"five navigation destinations retain48dp at240dp width");
                check(nav.top>=viewport-84&&nav.bottom<=viewport,"fixed nav remains inside reserved band");
                for(int j=i+1;j<5;j++)check(!nav.intersects(Menu120Layout.navigation(j,viewport,touch)),"fixed navigation touch targets never overlap");
            }
            check(Menu120Layout.rewardHeight(17,touch)>17*Menu120Layout.rewardRowHeight(touch)+hud,"all ready rows scrollable");
            Menu120Layout.Reveal reveal=new Menu120Layout.Reveal(viewport);
            check(reveal.titleY>0&&reveal.giftY>reveal.titleY+40&&reveal.detailY>=reveal.giftY+28&&reveal.continueY>=reveal.detailY+28,"reveal labels have independent reading rows even in landscape");
            check(reveal.continueY<viewport&&reveal.detailY<viewport,"reveal confirmation and reward remain visible");
        }
        Menu120Layout fallback=new Menu120Layout(Float.NaN,Float.NaN);check(Float.isFinite(fallback.contentHeight),"non-finite layout fallback");
        for(boolean ready:new boolean[]{false,true})for(boolean claimed:new boolean[]{false,true})check(Menu120Layout.canClaim(ready,claimed)==(ready&&!claimed),"claimed gifts never enabled");
        near(Menu120Layout.revealAlpha(-1,-1,false),0,"future reveal hidden");near(Menu120Layout.revealAlpha(0,-1,false),0,"fade starts transparent");
        near(Menu120Layout.revealAlpha(220,-1,false),1,"fade finishes");near(Menu120Layout.revealAlpha(120000,-1,false),1,"gift stays open for reading without timeout");
        near(Menu120Layout.revealAlpha(120000,90,false),.5f,"explicit close fades halfway");near(Menu120Layout.revealAlpha(120000,180,false),0,"explicit close ends");
        check(!Menu120Layout.revealFinished(-1,false)&&!Menu120Layout.revealFinished(179,false)&&Menu120Layout.revealFinished(180,false),"only explicit completed fade dismisses");
        near(Menu120Layout.revealAlpha(0,-1,true),1,"reduced motion stable");check(Menu120Layout.revealFinished(0,true)&&!Menu120Layout.revealFinished(-1,true),"reduced motion closes instantly only on request");
    }
    private static void copy(){
        String[] keys={"home","hello","guest","signed_in","team_hint","gifts","gift_ready","gift_soon","shop","shop_hint","quests","open_gift","collected","in_progress","gift_for_you","tap_continue","earned","gold","energy","wallet_unknown","shop_powers","shop_looks","shop_feature","shop_free","view","next","previous"};
        for(String lang:new String[]{"en","fr","de","es","pt","it"})for(String key:keys){Object[] args=key.equals("hello")?new Object[]{"Nova"}:key.equals("gift_ready")?new Object[]{7}:new Object[0];String text=V120MenuStrings.get(lang,key,args);check(!text.isEmpty()&&!text.contains("%")&&!text.contains("null"),"formatted six-language copy "+lang+"/"+key);}
        check(V120MenuStrings.get("ja","gifts").equals(V120MenuStrings.get("en","gifts")),"unknown language uses English");
        boolean refused=false;try{V120MenuStrings.get("en","missing");}catch(IllegalArgumentException good){refused=true;}check(refused,"unknown label fails visibly in test");
    }
    private static void claims(){
        RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();RewardsCosmeticsV17Test.Clock clock=new RewardsCosmeticsV17Test.Clock("2026-10-29");
        GameStore store=new GameStore(context,null,clock);MetaProgress meta=new MetaProgress(context,clock);
        ObjectiveDashboard.Snapshot first=ObjectiveDashboard.snapshot(store,meta);check(first.loginDailyReady,"actual fresh login gift ready");int before=store.coins(),amount=store.claimDailyLogin();
        check(amount>0&&store.coins()==before+amount,"real login claim returns committed Coins");
        check(!ObjectiveDashboard.snapshot(store,meta).loginDailyReady&&store.claimDailyLogin()==0,"fresh snapshot disables claimed login and duplicate grants zero");
        for(int level=1;level<=10;level++){store.completeLevel(level,3);meta.recordWin(0,level,3,"");}
        ObjectiveDashboard.Snapshot earned=ObjectiveDashboard.snapshot(store,meta);
        for(ObjectiveDashboard.Quest quest:earned.dailyQuests)if(quest.ready){before=store.coins();amount=meta.claimMission(quest.index,store);check(amount==quest.coins&&store.coins()==before+amount,"daily quest actual payout");check(meta.claimMission(quest.index,store)==0,"daily quest duplicate zero");check(ObjectiveDashboard.snapshot(store,meta).dailyQuests.get(quest.index).claimed&&!ObjectiveDashboard.snapshot(store,meta).dailyQuests.get(quest.index).ready,"daily claimed state replaces ready state");}
        for(ObjectiveDashboard.Quest quest:earned.weeklyQuests)if(quest.ready){before=store.coins();amount=meta.claimWeeklyMission(quest.index,store);check(amount==quest.coins&&store.coins()==before+amount,"weekly quest actual payout");check(meta.claimWeeklyMission(quest.index,store)==0,"weekly quest duplicate zero");check(ObjectiveDashboard.snapshot(store,meta).weeklyQuests.get(quest.index).claimed&&!ObjectiveDashboard.snapshot(store,meta).weeklyQuests.get(quest.index).ready,"weekly claimed state replaces ready state");}
        for(boolean weekly:new boolean[]{false,true}){
            for(int i=0;i<(weekly?Catalog.WEEKLY_COUNT:Catalog.DAILY_COUNT);i++)if(weekly)store.completeWeekly(i,3);else store.completeDaily(i,3);
            ObjectiveDashboard.Snapshot eligible=ObjectiveDashboard.snapshot(store,meta);check(weekly?eligible.weeklyEventReady:eligible.dailyEventReady,"actual event completion gift ready");
            amount=weekly?store.claimWeeklyCompletionReward():store.claimDailyCompletionReward();check(amount==(weekly?250:100),"actual event reward");
            ObjectiveDashboard.Snapshot claimed=ObjectiveDashboard.snapshot(store,meta);check(!(weekly?claimed.weeklyEventReady:claimed.dailyEventReady),"event claimed gift leaves ready set");check((weekly?store.claimWeeklyCompletionReward():store.claimDailyCompletionReward())==0,"event duplicate zero");
        }
        for(boolean memoryFirst:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx failed=new RewardsCosmeticsV17Test.Ctx();GameStore target=new GameStore(failed,null,clock);new MetaProgress(failed,clock);before=target.coins();failed.save().fail(memoryFirst);
            boolean threw=false;try{target.claimDailyLogin();}catch(GameStore.LocalSaveException expected){threw=true;}check(threw,"failed commit never returns a success result for reveal");
            check(((Integer)failed.save().disk.get("coins"))==before,"failed claim retains durable balance");
            target.retryPendingLocalSave();check(target.claimDailyLogin()==0&&target.dailyLoginClaimed(),"pending exact receipt settles once without duplicate reveal result");
            check(new GameStore(failed.reboot(),null,clock).dailyLoginClaimed(),"claimed state survives cold restart");
        }
        clock.set("2026-10-30");check(ObjectiveDashboard.snapshot(store,meta).loginDailyReady,"new legitimate daily period reopens login gift");
    }
    public static void main(String[] args)throws Exception{
        geometry();copy();claims();System.out.println("PASS menu1.20: "+checks+" assertions; pure layout, six-language copy, actual claim receipts");
        if(args.length>0){Path out=Path.of(args[0]);if(out.getParent()!=null)Files.createDirectories(out.getParent());Files.writeString(out,"{\"passed\":true,\"checks\":"+checks+",\"scope\":\"Pure menu geometry and coordinate transforms across 504 physical configurations, six-language labels, actual GameStore/MetaProgress claims and refused commits. No Android rendering, human or provider acceptance.\"}\n");}
    }
}
