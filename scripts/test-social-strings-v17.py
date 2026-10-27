#!/usr/bin/env python3
"""Extract every constant social UI copy pair and exercise all six real translations locally."""
from pathlib import Path
import json
import os
import re
import shutil
import subprocess
import tempfile

ROOT=Path(__file__).resolve().parents[1]
source=(ROOT/'app/src/main/java/com/androidhit/lumadrift/social/SocialActivity.java').read_text()
pattern=r'\bt\(\s*("(?:[^"\\]|\\.)*")\s*,\s*("(?:[^"\\]|\\.)*")'
pairs=[(json.loads(a),json.loads(b))for a,b in re.findall(pattern,source)]
assert len(pairs)>=59, 'All social UI and error copy pairs must be extracted'
assert len(re.findall(r'\bt\(',source))==len(pairs)+1, 'No dynamic or untranslated copy expression may bypass the catalogue'
assert 'SocialStrings.get(store.resolvedLanguage(),fr,en,args)' in source, 'Social screen uses the actual selected language'
assert 'private boolean fr()' not in source, 'The French-for-every-other-language fallback must not return'
assert any('%d / 3' in fr for fr,en in pairs), 'Quota is a translatable format, not a concatenated lookup key'
rows=',\n'.join('{'+json.dumps(fr,ensure_ascii=False)+','+json.dumps(en,ensure_ascii=False)+'}' for fr,en in pairs)
program='''import com.androidhit.lumadrift.social.SocialStrings;
public final class SocialStringsCompleteness {
  private static int checks;
  private static void check(boolean pass,String message){checks++;if(!pass)throw new AssertionError(message);}
  public static void main(String[]args){
    String[][]pairs={ROWS};
    check(SocialStrings.keys().size()==pairs.length,"Every source pair has exactly one translation row");
    String[]langs={"en","fr","de","es","pt","it"};
    for(String[]pair:pairs)for(String lang:langs){
      String value=SocialStrings.get(lang,pair[0],pair[1]);
      check(value!=null&&!value.trim().isEmpty(),"Nonempty translation "+lang+" / "+pair[1]);
      check(value.contains("%d")==pair[1].contains("%d"),"Format argument preserved in "+lang);
      if(lang.equals("fr"))check(value.equals(pair[0]),"French accessibility label preserved");
      else if(lang.equals("en"))check(value.equals(pair[1]),"English accessibility label preserved");
      else check(!value.equals(pair[0]),"Actual translation replaces the former French fallback in "+lang);
      if(pair[1].contains("%d")){String formatted=SocialStrings.get(lang,pair[0],pair[1],2);check(formatted.startsWith("2 / 3")&&!formatted.contains("%d"),"Quota formats correctly in "+lang);}
    }
    for(String tag:new String[]{"de-DE","es_ES","pt-PT","it-IT"})check(!SocialStrings.get(tag,pairs[1][0],pairs[1][1]).equals(pairs[1][0]),"Regional locale normalized "+tag);
    check(SocialStrings.get("zz",pairs[1][0],pairs[1][1]).equals(pairs[1][1]),"Unknown language safely uses English");
    boolean rejected=false;try{SocialStrings.get("fr","unknown","unknown");}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"Unknown UI copy cannot silently fall back");
    rejected=false;try{SocialStrings.get("de",pairs[0][0],"changed English");}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"Changed EN pair requires catalogue update");
    System.out.println("PASS "+checks+" social-copy checks: "+pairs.length+" extracted UI/error pairs, all six languages, exact FR/EN labels and dynamic quota formatting");
  }
}
'''.replace('ROWS',rows)
java_home=os.environ.get('JAVA_HOME')
if java_home and (Path(java_home)/'bin/javac').is_file():java_bin=Path(java_home)/'bin'
elif Path('/opt/homebrew/opt/openjdk@17/bin/javac').is_file():java_bin=Path('/opt/homebrew/opt/openjdk@17/bin')
else:java_bin=Path(shutil.which('javac')).parent
with tempfile.TemporaryDirectory(prefix='luma-social-copy-v17-')as directory:
    output=Path(directory);test=output/'SocialStringsCompleteness.java';test.write_text(program)
    sources=list((ROOT/'scripts/test-support/android/annotation').glob('*.java'))+list((ROOT/'scripts/test-support/android/content').glob('*.java'))+list((ROOT/'scripts/test-support/android/graphics').glob('*.java'))+list((ROOT/'app/src/main/java/com/androidhit/lumadrift/data').glob('*.java'))+[ROOT/'app/src/main/java/com/androidhit/lumadrift/social/SocialStrings.java',test]
    subprocess.run([str(java_bin/'javac'),'-d',str(output),*[str(p)for p in sources]],check=True,cwd=ROOT)
    subprocess.run([str(java_bin/'java'),'-cp',str(output),'SocialStringsCompleteness'],check=True,cwd=ROOT)
print('SCOPE: source/JVM localization only; no provider, network, installed screen or native-speaker review is inferred.')
