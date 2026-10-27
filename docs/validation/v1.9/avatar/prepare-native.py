#!/usr/bin/env python3
"""Namespace-only copies keep the atlas exporter independent of app R8 inlining."""
import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parents[4]
source = root / 'app/src/main/java/com/androidhit/lumadrift'
target = root / 'build/generated/v19-avatar'
old, new = 'com.androidhit.lumadrift', 'com.androidhit.lumadrift.qa.atlas'
records = []
for name in ('data/Catalog.java', 'data/AvatarStyles.java', 'ui/AvatarStage.java'):
    original = (source / name).read_bytes()
    copied = original.replace(old.encode(), new.encode())
    assert copied.replace(new.encode(), old.encode()) == original
    path = target / name
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(copied)
    records.append({'source': str((source / name).relative_to(root)),
                    'source_sha256': hashlib.sha256(original).hexdigest(),
                    'generated_sha256': hashlib.sha256(copied).hexdigest()})
report = {'transformation': 'Replace Java package/import prefix only; reverse replacement byte-identical',
          'reason': 'Signed release R8 inlines/removes catalogue methods. Rasterize identical renderer bodies in test namespace; integrated app evidence is separate.',
          'sources': records}
(Path(__file__).parent / 'native-source-manifest.json').write_text(json.dumps(report, indent=2) + '\n')
print('Prepared three namespace-only source copies')
