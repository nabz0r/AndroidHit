import com.androidhit.lumadrift.ui.Menu120Layout;
import com.androidhit.lumadrift.ui.Menu121Layout;
import com.androidhit.lumadrift.data.Menu121Strings;

/** Host geometry/translation checks; not evidence of Android rendering or human usability. */
public final class Menu121LayoutTest {
    private static int checks;
    private static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
    public static void main(String[] args){
        for(float viewport:new float[]{180,320,480,617,700,1000,1600})for(float minimum:new float[]{48,52,60,72,96}){
            Menu121Layout layout=new Menu121Layout(viewport,minimum);
            Menu120Layout.Rect[] targets={layout.profile,layout.settings,layout.avatar,layout.evolution,layout.style,layout.play,layout.team,layout.gifts,layout.shop,layout.map,layout.expedition,layout.events,layout.room,layout.atelier};
            for(int i=0;i<targets.length;i++){
                Menu120Layout.Rect target=targets[i];check(target.width()>=minimum&&target.height()>=minimum,"Physical target "+i+" at "+minimum);
                check(target.left>=0&&target.right<=390,"Target stays within logical width");
                for(int j=i+1;j<targets.length;j++)check(!target.intersects(targets[j]),"Distinct actions must not overlap: "+i+", "+j);
            }
            check(layout.hero.height()>=340,"Full body retains its stage on compact viewports");
            check(layout.contentHeight>=viewport,"No negative scroll bounds");
            float maximum=layout.contentHeight-viewport;
            if(maximum>0)check(layout.room.bottom-maximum<=viewport-96,"Last row can scroll above fixed navigation");
            check(layout.play.bottom<layout.team.top,"Play precedes secondary team controls");
            for(int i=0;i<4;i++){Menu120Layout.Rect quick=layout.environments[i];check(quick.width()>=minimum&&quick.height()>=minimum,"Room shortcut keeps physical touch size");check(quick.top>layout.play.bottom&&quick.bottom<layout.team.top,"Room shortcuts come before squad");for(int j=i+1;j<4;j++)check(!quick.intersects(layout.environments[j]),"Room shortcuts do not overlap");}
        }
        Menu121Layout standard=new Menu121Layout(617,48);
        check(standard.play.bottom<=Menu120Layout.navigation(0,617,48).top,"Play is clear of navigation on a standard 390x693 viewport with wallet");
        for(String language:new String[]{"en","fr","de","es","pt","it","unknown"}){
            for(String key:new String[]{"xp_rule","avatar_role","style_action","evolution_action","evolve_by_copies","max_evolution","team_role","team_purpose","battle_role","choose","coins_role","coins_loop","gold_role","gold_closed","economy_route","shop_powers","shop_looks","shop_gold","preview","owned","equipped","look_only","companion_draw","outfit_draw","draw_note","direct_buy","earn_coins","next_form","copies_skills_rule"}){
                String text=Menu121Strings.get(language,key);check(text!=null&&!text.trim().isEmpty(),"Complete translated label: "+language+" "+key);check(!text.contains("%"),"No unresolved format token");
            }
            check(Menu121Strings.get(language,"player_level",7).contains("7"),"Real account rank");check(Menu121Strings.get(language,"xp_progress",35,120).contains("35"),"Real XP progress");check(Menu121Strings.get(language,"companion_level",3).contains("3"),"Real companion level");check(Menu121Strings.get(language,"copies_progress",2,8).contains("8"),"Real copy threshold");check(Menu121Strings.get(language,"coin_price",180).contains("180"),"Exact Coin price");
        }
        System.out.println("Menu121LayoutTest: "+checks+" checks passed; host geometry and six-language formatting only.");
    }
}
