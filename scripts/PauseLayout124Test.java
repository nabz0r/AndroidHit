import com.androidhit.lumadrift.ui.PauseLayout124;
import com.androidhit.lumadrift.ui.CompanionSheetLayout123;
import com.androidhit.lumadrift.data.Clarity124Strings;
public final class PauseLayout124Test {
 private static int checks;
 private static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
 private static void target(PauseLayout124.Rect r,float floor,float viewport){check(r.width()+.001f>=floor&&r.height()+.001f>=floor,"Physical touch floor (float tolerance .001)");check(r.left>=24&&r.right<=366&&r.top>=0&&r.bottom<=viewport+.001f,"Action visible inside viewport");}
 private static boolean overlap(PauseLayout124.Rect a,PauseLayout124.Rect b){return a.left<b.right&&b.left<a.right&&a.top<b.bottom&&b.top<a.bottom;}
 public static void main(String[] args){
  for(float viewport:new float[]{320,390,470,520,736})for(float touch:new float[]{48,76.8f,96})for(float font:new float[]{12,18,26})for(boolean restart:new boolean[]{false,true}){
   PauseLayout124 p=new PauseLayout124(viewport,touch,font,400,restart);
   target(p.primary,touch,viewport);target(p.secondary,touch,viewport);check(!overlap(p.primary,p.secondary),"Primary/cancel remain separate");
   if(p.tertiary!=null){target(p.tertiary,touch,viewport);check(!overlap(p.primary,p.tertiary)&&!overlap(p.secondary,p.tertiary),"All pause choices remain separate");}
   check(p.bodyHeight>=0&&p.bodyY+p.bodyHeight<=p.costY,"Long copy cannot cover actions/cost");
   check(p.primary.bottom<=p.top+p.height&&p.secondary.bottom<=p.top+p.height,"Surface contains actions");
   check(p.top+p.height<=viewport,"Compact sheet fits its viewport");
  }
  for(float viewport:new float[]{320,470,736})for(float touch:new float[]{48,76.8f,96,132})for(float description:new float[]{0,60,180,400})for(float font:new float[]{12,18,26,32,40}){
   CompanionSheetLayout123 s=new CompanionSheetLayout123(viewport,touch,description,font);
   check(s.buttonHeight>=touch,"Talent actions retain physical floor");check(s.controlsY>=s.detailY+description,"Effect copy precedes action");check(s.outfitY>=s.controlsY+s.buttonHeight&&s.returnY>=s.outfitY+s.buttonHeight,"Talent choices do not overlap");check(s.returnY+s.buttonHeight<=s.contentHeight,"Return stays inside scrollable content");
   check(s.costY-s.familyY+.001f>=1.35f*font&&s.usesY-s.costY+.001f>=1.35f*font,"Family,cost and actual uses each retain a full text line");
   check(s.chargeHeight>=30&&s.chargeHeight>=1.5f*font,"Charge capsule respects text floor");
   check(s.chargeY>=s.portraitTop+102+11.999f&&s.chargeY>=s.usesY+font*.4f+11.999f,"Charge clears portrait and metadata");
   check(s.portraitTop>=s.closeTop+s.closeSize+11.999f&&s.titleRight<=366-s.closeSize-12,"Title/close and metadata occupy separate regions");
   check(s.detailY>=s.chargeY+s.chargeHeight-.001f&&s.controlsY>=s.detailY+description+35.999f,"Effect/actions follow the expanded metadata block");
   CompanionSheetLayout123 legacy=new CompanionSheetLayout123(viewport,touch,description),normal=new CompanionSheetLayout123(viewport,touch,description,12);
   check(legacy.height==normal.height&&legacy.familyY==normal.familyY&&legacy.chargeY==normal.chargeY,"Historical constructor delegates to text floor12");
  }
  for(float bad:new float[]{0,-1,Float.NaN,Float.POSITIVE_INFINITY,Float.MAX_VALUE}){try{new CompanionSheetLayout123(320,48,80,bad);throw new AssertionError("Invalid sheet font accepted");}catch(IllegalArgumentException expected){checks++;}}
  for(String language:new String[]{"fr","en","de","es","pt","it"}){String uses=Clarity124Strings.get(language,"uses",2,4);check(uses.contains("2/4"),"Actual usage values in each locale");check(Clarity124Strings.get(language,"skill_cost",3).contains("3"),"Exact charge cost in each locale");check(!Clarity124Strings.get(language,"skill_attempt").isEmpty(),"New run does not assert a one-use rule");}
  for(float value:new float[]{Float.NaN,Float.POSITIVE_INFINITY,0,-1}){try{new PauseLayout124(value,48,12,30,false);throw new AssertionError("Invalid viewport accepted");}catch(IllegalArgumentException expected){checks++;}}
  System.out.println("PASS "+checks+" pause/sheet/locale checks");
 }
}
