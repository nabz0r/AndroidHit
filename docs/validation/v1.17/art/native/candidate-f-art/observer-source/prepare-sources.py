#!/usr/bin/env python3
"""Package read-only art source witnesses for the separate renderer observer."""
from pathlib import Path
import hashlib
import json

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
names=[
    'ui/CompanionPortrait.java','ui/CharacterSprites.java','ui/CompanionRig.java',
    'ui/ArtLibrary.java','ui/ArtRegions117.java','ui/AvatarStage.java',
    'data/Catalog.java','data/CosmeticCatalog.java','data/AvatarStyles.java',
    'data/AvatarLoadout.java','data/AvatarGearCatalog.java','data/SignatureOutfits.java',
]
files=[ROOT/'app/src/main/java/com/androidhit/lumadrift'/name for name in names]
files+=sorted((ROOT/'app/src/main/assets/art/v117').glob('*'))
assert files and all(file.is_file() for file in files)
manifest={str(file.relative_to(ROOT)):hashlib.sha256(file.read_bytes()).hexdigest() for file in files}
output=HERE/'assets/source-manifest.json';output.parent.mkdir(parents=True,exist_ok=True)
output.write_text(json.dumps(manifest,indent=2,sort_keys=True)+'\n')
print(json.dumps({'source_files':len(manifest),'sha256':hashlib.sha256(output.read_bytes()).hexdigest(),'scope':'Host source witnesses only; no Android execution'}))
