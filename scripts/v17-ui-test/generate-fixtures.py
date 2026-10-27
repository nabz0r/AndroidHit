#!/usr/bin/env python3
"""Read-only certified source oracle. Never starts or modifies an Android app."""
from pathlib import Path
import json, os, subprocess, tempfile
ROOT=Path(__file__).resolve().parents[2]
JAVA=Path(os.environ.get('JAVA_HOME','/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home'))/'bin'
with tempfile.TemporaryDirectory(prefix='luma-v17-oracle-') as tmp:
    subprocess.run([str(JAVA/'javac'),'-d',tmp,*map(str,sorted((ROOT/'app/src/main/java/com/androidhit/lumadrift/engine').glob('*.java'))),str(ROOT/'scripts/V15Fixture.java')],check=True)
    for n in range(1,14):
        raw=subprocess.check_output([str(JAVA/'java'),'-cp',tmp,'V15Fixture','J',str(n)],text=True)
        value=json.loads(raw)
        assert value['family']=='J' and value['number']==n and value['steps']
        (ROOT/'scripts/v17-ui-test/assets/fixtures'/('j%d.json'%n)).write_text(json.dumps(value,separators=(',',':'))+'\n')
print('Generated J1–13 certified UI-action fixtures from unmodified engine source')
