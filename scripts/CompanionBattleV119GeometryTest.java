import com.androidhit.lumadrift.ui.CompanionBattleLayout;
import com.androidhit.lumadrift.ui.CompanionBattleLayout.Rect;

/** Spatial contracts for the exact live helper; no gameplay or native glyph claim. */
public final class CompanionBattleV119GeometryTest {
    private static int checks,layouts;
    public static void main(String[] args){
        for(float width:new float[]{264,280,320,342,358,390,480,640})
            for(float height:new float[]{144,160,192,256})
                for(float left:new float[]{-10,0,16,95})
                    for(float top:new float[]{0,83,140}){
                        CompanionBattleLayout l=new CompanionBattleLayout(left,top,left+width,top+height);layouts++;
                        valid(l.combo,l.bounds);check(l.combo.height()>=48,"48 logical unit combo target");
                        check(l.slotAt(l.combo.centerX(),l.combo.centerY())==-1,"combo never routes to a slot");
                        for(int n=0;n<3;n++){
                            Rect r=l.actions[n];valid(r,l.bounds);check(r.width()>=48&&r.height()>=48,"slot touch floor");
                            check(!r.intersects(l.combo),"slot and combo do not overlap");
                            check(l.slotAt(r.centerX(),r.centerY())==n,"slot centre routes exactly");
                            check(l.slotAt(r.left,r.top)==n,"top-left included");
                            check(l.slotAt(r.right,r.centerY())==-1,"right edge excluded into gutter");
                            check(l.slotAt(r.centerX(),r.bottom)==-1,"bottom edge excluded into gutter");
                            for(int j=n+1;j<3;j++)check(!r.intersects(l.actions[j]),"different slot targets separate");
                        }
                        check(l.slotAt(Float.NaN,top)==-1,"NaN cannot hit");
                        check(l.slotAt(left-1,top)==-1,"outside cannot hit");
                    }
        invalid(0,0,263,144);invalid(0,0,358,143);invalid(Float.NaN,0,358,144);
        invalid(0,0,Float.POSITIVE_INFINITY,144);invalid(0,Float.NEGATIVE_INFINITY,358,144);
        for(float minimumTouch:new float[]{48,50,52,58.5f,62.4f,72,78})
            for(float width:new float[]{264,320,358,390}){
                float h=CompanionBattleLayout.heightFor(minimumTouch);
                CompanionBattleLayout l=new CompanionBattleLayout(16,83,16+width,83+h,minimumTouch);layouts++;
                valid(l.combo,l.bounds);
                check(l.combo.height()+.001f>=minimumTouch,"combo respects supplied physical target");
                check(l.combo.height()*48/minimumTouch>=47.999f,"combo physical48dp after caller canvas transform");
                for(Rect r:l.actions){
                    valid(r,l.bounds);
                    check(r.width()+.001f>=minimumTouch&&r.height()+.001f>=minimumTouch,"slots respect supplied physical target");
                    check(r.width()*48/minimumTouch>=47.999f&&r.height()*48/minimumTouch>=47.999f,"slot physical48dp after caller canvas transform");
                    check(!r.intersects(l.combo),"enlarged combo stays separate");
                }
            }
        try{new CompanionBattleLayout(0,0,358,144,78);throw new AssertionError("unexpanded physical target accepted");}
        catch(IllegalArgumentException expected){checks++;}
        try{new CompanionBattleLayout(0,0,358,144,Float.NaN);throw new AssertionError("invalid touch floor accepted");}
        catch(IllegalArgumentException expected){checks++;}
        System.out.println("Companion battle 1.19 geometry PASS: "+checks+" checks across "+layouts+" layouts; distinct logical targets, bounds and exact hit routing. Physical text/touch adaptation and native artwork require host/native validation.");
    }
    private static void valid(Rect r,Rect bounds){
        check(Float.isFinite(r.left)&&Float.isFinite(r.top)&&Float.isFinite(r.right)&&Float.isFinite(r.bottom),"finite");
        check(r.width()>0&&r.height()>0,"positive");
        check(r.left>=bounds.left-.001f&&r.right<=bounds.right+.001f&&r.top>=bounds.top-.001f&&r.bottom<=bounds.bottom+.001f,"inside dock");
    }
    private static void invalid(float left,float top,float right,float bottom){
        try{new CompanionBattleLayout(left,top,right,bottom);throw new AssertionError("invalid bounds accepted");}
        catch(IllegalArgumentException expected){checks++;}
    }
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
}
