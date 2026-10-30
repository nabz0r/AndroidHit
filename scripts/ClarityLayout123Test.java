import com.androidhit.lumadrift.ui.CompanionSheetLayout123;
import com.androidhit.lumadrift.ui.PlaySceneLayout;
/** Independent reachability/target invariants; native text/art is qualified separately. */
public final class ClarityLayout123Test {
 static int checks;
 static void require(boolean value,String reason){checks++;if(!value)throw new AssertionError(reason);}
 public static void main(String[]args){
  for(int width:new int[]{240,280,320,360,390,480})for(float vh:new float[]{320,433,600,805,960})for(float prose:new float[]{36,80,140,220}){
   float touch=48f*390/width;
   CompanionSheetLayout123 sheet=new CompanionSheetLayout123(vh,touch,prose);
   require(sheet.buttonHeight*width/390>=47.999,"Physical skill target");
   require(sheet.controlsY>=sheet.top+166+prose+30,"Description before action");
   require(sheet.controlsY+sheet.buttonHeight+10<=sheet.outfitY+.01,"Activate and outfit do not overlap");
   require(sheet.outfitY+sheet.buttonHeight+10<=sheet.returnY+.01,"Outfit and return do not overlap");
   require(sheet.returnY+sheet.buttonHeight+12<=sheet.contentHeight,"Last control in scroll content");
   float scroll=Math.min(sheet.contentHeight-vh,Math.max(0,sheet.returnY+sheet.buttonHeight-vh));
   require(sheet.returnY-scroll>=0&&sheet.returnY+sheet.buttonHeight-scroll<=vh+.01,"Full return target reachable");
   PlaySceneLayout layout=new PlaySceneLayout(vh,true,touch,42,12,0,true);
   require(layout.compactActions,"Thinking surface uses one action row");
   require(layout.boardY<240,"Grid starts above legacy companion rails");
   require(layout.gateTargets[0].top>144,"Gate clears objective and progress");
   require(layout.feedbackRect.top>layout.gateTargets[2].bottom,"Feedback clears bottom gate");
  }
  for(float invalid:new float[]{0,-1,Float.NaN,Float.POSITIVE_INFINITY}){boolean rejected=false;try{new CompanionSheetLayout123(invalid,48,40);}catch(IllegalArgumentException expected){rejected=true;}require(rejected,"Reject invalid viewport");}
  System.out.println("Clarity123 geometry PASS: "+checks+" checks. No native glyph, art or comprehension claim.");
 }
}
