import com.androidhit.lumadrift.ui.*;
public class HudAudit {public static void main(String[] args){
for(int[] config:new int[][]{{320,480,24,24,100},{480,720,36,36,150},{240,320,24,24,100},{480,800,36,36,150}}){
 float density=config[4]/100f;ExperienceLayout e=new ExperienceLayout(config[0],config[1],config[2],config[3],density,1.3f);
 float touch=48*density/e.scale,inset=42+CompanionBattleLayout.heightFor(touch);
 PlaySceneLayout p=new PlaySceneLayout(e.viewportHeight,true,touch,42,e.minimumTextSize,inset);
 float maxScroll=p.contentHeight-e.viewportHeight;
 System.out.printf(java.util.Locale.ROOT,"pixels=%dx%d density=%.2f safe=%d/%d font=1.3 viewport=%.4f boardY=%.4f H=%.4f maxScroll=%.4f boardTopAtMax=%.4f row0centerU11=%.4f feedbackInput=42\n",config[0],config[1],density,config[2],config[3],e.viewportHeight,p.boardY,p.contentHeight,maxScroll,p.boardY-maxScroll,p.boardY-maxScroll+p.boardWidth/10);
}}}