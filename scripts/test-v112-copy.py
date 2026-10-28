#!/usr/bin/env python3
"""Check the public six-language copy contract against its actual Java implementation."""
from pathlib import Path
import subprocess,os,tempfile,re,json,hashlib
root=Path(__file__).resolve().parents[1]
source=root/'app/src/main/java/com/androidhit/lumadrift/data/V112Strings.java'
keys=re.findall(r'case "([a-z_]+)":',source.read_text())
assert len(keys)>=16 and len(keys)==len(set(keys))
java=Path(os.environ['JAVA_HOME'])/'bin'
main='''import com.androidhit.lumadrift.data.V112Strings;public class CopyV112Test {public static void main(String[] args) {int n=0;String[] keys=KEYS;for(String key:keys)for(String lang:new String[]{"en","fr","de","es","pt","it"}){String s=V112Strings.get(lang,key);if(s==null||s.isBlank()||s.equals(key)||s.contains("%")||s.contains("null"))throw new AssertionError(lang+":"+key);n++;}if(!V112Strings.get("fr-FR","workshop").equals(V112Strings.get("fr","workshop")))throw new AssertionError("region");n++;if(!V112Strings.get("unknown","workshop").equals(V112Strings.get("en","workshop")))throw new AssertionError("fallback");n++;try{V112Strings.get("fr","not_a_key");throw new AssertionError("invalid key");}catch(IllegalArgumentException good){n++;}System.out.println("PASS "+n+" public copy checks / "+keys.length+" keys / 6 languages");}}'''.replace('KEYS','new String[]{'+','.join(json.dumps(k) for k in keys)+'}')
with tempfile.TemporaryDirectory(prefix='luma-v112-copy-') as td:
 p=Path(td);(p/'CopyV112Test.java').write_text(main)
 actual=[source,root/'app/src/main/java/com/androidhit/lumadrift/data/Strings.java']
 subprocess.run([str(java/'javac'),'-d',str(p),*[str(x) for x in actual],str(p/'CopyV112Test.java')],check=True)
 subprocess.run([str(java/'java'),'-cp',str(p),'CopyV112Test'],check=True)
 print(json.dumps({'source_sha256':{str(x.relative_to(root)):hashlib.sha256(x.read_bytes()).hexdigest() for x in actual}},indent=2))
