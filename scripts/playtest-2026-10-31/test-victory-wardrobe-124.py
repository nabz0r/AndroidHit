#!/usr/bin/env python3
"""Execute current LumaView layout/navigation fragments with host UI callback adapters."""
import pathlib,argparse,tempfile,subprocess,json,hashlib,datetime,os
root=pathlib.Path(__file__).resolve().parents[2]
p=argparse.ArgumentParser();p.add_argument('--output',type=pathlib.Path,required=True);a=p.parse_args();out=a.output.resolve();out.mkdir(parents=True,exist_ok=False)
path=root/'app/src/main/java/com/androidhit/lumadrift/LumaView.java';source=path.read_text()
def method(name):
 start=source.index('    private '+name);pos=source.index('{',start);depth=1;i=pos+1
 while depth:
  if source[i]=='{':depth+=1
  elif source[i]=='}':depth-=1
  i+=1
 return source[start:i]
geometry=method('VictoryLayout victoryLayout124()')
methods='\n'.join(method(n) for n in ['void returnToMenu(String destination)','void backMenu()','void showAppliedLook(String name,boolean showCompanion)'])
start=source.index('        boolean returningWardrobe=');end=source.index('        if(!s.equals("play"))battleSheet123=false;',start)
transition=source[start:end]
push='        if(!navigatingBack&&!screen.equals(s)&&!screen.equals("play"))menuHistory.push(screen);'
scroll='        contentScroll=returningWardrobe?wardrobePreviewScroll124:0;scrolling=false;'
assert push in source and scroll in source
code='''import java.util.*;import com.androidhit.lumadrift.ui.*;
public class VictoryWardrobe124Host {
 static int checks,geometries,transitions;static void ck(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 float viewportH=600,font=14,touch=48,panelHeight=160;int gifts;
 float minimumTextSize(){return font;}float battleMinimumTouch(){return touch;}int readyGiftCount120(){return gifts;}
 static class Panel{float height;Panel(float height){this.height=height;}}
 Panel victoryPanel124(){return new Panel(panelHeight);}
'''+geometry+'''
 static class AudioEngine{enum Sound{SELECT}}static class SceneTransition{enum Direction{BACK}}
 SceneTransition.Direction nextSceneDirection;ArrayDeque<String>menuHistory=new ArrayDeque<>();boolean navigatingBack,wardrobePreview124,wardrobeFromPlay123,scrolling;
 String screen="home",wardrobeActor123="nova";int wardrobeMode,wardrobeGearSlot,wardrobePage,beginCalls;float wardrobePreviewScroll124,contentScroll;long actionGeneration;
 Map<String,String>wardrobeDraft123=new HashMap<>();List<Object>hits=new ArrayList<>();
 static class Store{String equippedCharacter(){return "nova";}}Store store=new Store();
 void beginWardrobe123(){beginCalls++;wardrobeDraft123.clear();wardrobeDraft123.put("hat","worn");}
 void go(String s){
'''+transition+push+'\n'+scroll+'''
 screen=s;
 }
 void sfx(AudioEngine.Sound s){}void haptic(){}void notice(String s){}String b(String key,String name){return key+name;}
'''+methods+'''
 void geometry(){
 for(float h:new float[]{180,240,320,390,480,600,720,960,1280})for(float f:new float[]{12,15,18,24,38,52})for(float t:new float[]{48,64,96,156})for(float p:new float[]{78,140,240,500,1000})for(int g=0;g<2;g++){
 viewportH=h;font=f;touch=t;panelHeight=p;gifts=g;VictoryLayout a=victoryLayout124();
 ck(a.height>=h,"result remains at least viewport height");ck(a.metricsY>=a.starsY+30+p+23.9f,"real measured rank panel cannot overlap metrics");
 var controls=new ArrayList<PlaySceneLayout.Rect>(List.of(a.next,a.map,a.shop));if(a.gifts!=null)controls.add(a.gifts);
 for(var r:controls){ck(r.left>=0&&r.right<=390&&r.top>=0&&r.bottom<=a.height,"control stays in complete scroll extent");ck(r.height()+.01f>=t,"physical touch floor preserved");float scroll=Math.max(0,Math.min(a.height-h,r.top));ck(r.top-scroll>=-.01f&&r.bottom-scroll<=h+.01f,"each control can be fully reached by bounded scrolling");}
 ck(a.next.bottom<a.map.top,"next and secondary controls do not overlap");if(a.gifts!=null)ck(a.gifts.bottom<a.next.top,"gifts and next do not overlap");geometries++;
 }
 }
 void navigation(){
 for(int slot=0;slot<3;slot++)for(int page=0;page<2;page++)for(float scroll:new float[]{0,180,450})for(boolean apply:new boolean[]{false,true}){
 menuHistory.clear();menuHistory.add("home");menuHistory.add("adventure");screen="wardrobe";wardrobeMode=1;wardrobeGearSlot=slot;wardrobePage=page;contentScroll=scroll;wardrobeDraft123.clear();wardrobeDraft123.put("hat","trial");int starts=beginCalls;
 go("item");ck(wardrobePreview124&&wardrobePreviewScroll124==scroll&&wardrobeDraft123.get("hat").equals("trial"),"preview preserves wardrobe context and uncommitted try-on");
 if(apply)showAppliedLook("gear",true);else backMenu();
 ck(screen.equals("wardrobe")&&wardrobeMode==1&&wardrobeGearSlot==slot&&wardrobePage==page&&contentScroll==scroll,"close/apply restores exact equipment tab, slot, page and scroll");
 ck(beginCalls==starts&&wardrobeDraft123.get("hat").equals("trial"),"return never silently resets the look draft");
 backMenu();ck(screen.equals("home")&&!menuHistory.contains("item"),"wardrobe Back reaches original parent, never old preview");transitions++;
 }
 menuHistory.clear();menuHistory.add("collection");menuHistory.add("adventure");screen="item";wardrobePreview124=false;showAppliedLook("actor",true);ck(screen.equals("wardrobe")&&menuHistory.peek().equals("collection"),"applied appearance from collection discards item as a back destination");backMenu();ck(screen.equals("collection"),"non-wardrobe preview keeps original collection parent");
 screen="home";wardrobeMode=1;contentScroll=200;go("wardrobe");ck(wardrobeMode==0&&contentScroll==0&&wardrobeDraft123.get("hat").equals("worn"),"a fresh wardrobe visit starts an explicit clean outfit draft");
 }
 public static void main(String[]args){var t=new VictoryWardrobe124Host();t.geometry();t.navigation();System.out.println("PASS assertions="+checks+" geometries="+geometries+" wardrobeRoundTrips="+transitions);}
}
'''
(out/'extracted-host.java').write_text(code)
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
inputs=[path,root/'app/src/main/java/com/androidhit/lumadrift/ui/VictoryLayout.java',root/'app/src/main/java/com/androidhit/lumadrift/ui/PlaySceneLayout.java',pathlib.Path(__file__)]
r={'status':'RUNNING','startedAtUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'sourceInputs':{str(p.relative_to(root)):sha(p) for p in inputs},'scope':'Exact extracted LumaView victory layout and wardrobe navigation blocks, with real VictoryLayout/PlaySceneLayout and explicit host callbacks/preferences-free UI field adapters. Measured text heights are fixtures, not Android typography/native interaction proof. No reward/economy mutation or native APK test.'}
try:
 j=pathlib.Path(os.environ.get('JAVA_HOME','/opt/homebrew/opt/openjdk@17'))/'bin'
 with tempfile.TemporaryDirectory(prefix='luma124-victory-wardrobe-')as t:
  src=pathlib.Path(t)/'VictoryWardrobe124Host.java';src.write_text(code)
  for name,cmd in [('compile',[str(j/'javac'),'-d',t,*map(str,inputs[1:3]),str(src)]),('run',[str(j/'java'),'-cp',t,'VictoryWardrobe124Host'])]:
   q=subprocess.run(cmd,capture_output=True);(out/(name+'.stdout.log')).write_bytes(q.stdout);(out/(name+'.stderr.log')).write_bytes(q.stderr)
   if q.returncode:raise RuntimeError(name+' failed; original logs retained')
   if name=='run':r['result']=q.stdout.decode().strip();print(r['result'])
 r['status']='PASS'
except BaseException as e:r['status']='FAIL';r['error']=str(e);raise
finally:
 r['completedAtUtc']=datetime.datetime.now(datetime.timezone.utc).isoformat();r['outputs']={str(p.relative_to(out)):{'sha256':sha(p),'bytes':p.stat().st_size} for p in out.glob('*') if p.is_file()};(out/'receipt.json').write_text(json.dumps(r,indent=2)+'\n')
