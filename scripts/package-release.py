#!/usr/bin/env python3
"""Collect already built packages and record exact public artifacts/source inputs."""
from pathlib import Path
import hashlib,json,shutil,re,datetime
root=Path(__file__).resolve().parents[1]
release=root/'releases';release.mkdir(exist_ok=True)
version=re.search(r"versionName '([^']+)'",(root/'app/build.gradle').read_text()).group(1)
for source,target in [('app/build/outputs/apk/release/app-release.apk',f'luma-drift-{version}.apk'),('app/build/outputs/bundle/release/app-release.aab',f'luma-drift-{version}.aab')]:
 shutil.copyfile(root/source,release/target)
inputs=[*(root/'app/src/main').rglob('*'),root/'app/build.gradle',root/'build.gradle',root/'settings.gradle',root/'gradle.properties',*(root/'gradle').rglob('*'),root/'gradlew']
paths=sorted({p for p in inputs if p.is_file()},key=str)
record={'version':version,'application_id':'com.androidhit.lumadrift','build_date':datetime.date.today().isoformat(),'source_inputs_sha256':{str(p.relative_to(root)):hashlib.sha256(p.read_bytes()).hexdigest() for p in paths},'artifacts':{p.name:{'bytes':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in sorted(release.iterdir()) if p.suffix in ['.apk','.aab','.pem']}}
(release/'manifest.json').write_text(json.dumps(record,indent=2)+'\n')
(release/'SHA256SUMS.txt').write_text(''.join(v['sha256']+'  '+k+'\n' for k,v in record['artifacts'].items()))
print(json.dumps(record['artifacts'],indent=2))
