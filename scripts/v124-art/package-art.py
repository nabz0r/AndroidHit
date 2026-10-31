#!/usr/bin/env python3
"""Encode unmodified generated originals; retain paths, prompts and content hashes."""
import hashlib,json,subprocess,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
manifest_path=ROOT/'docs/art/v1.24/generation-jobs.json'
manifest=json.loads(manifest_path.read_text())
jobs=json.loads(Path(sys.argv[1]).read_text())
for job in jobs:
    source=Path(job['source']);target=ROOT/'app/src/main/assets/art/v124'/f"{job['key']}.webp"
    assert source.is_file() and source.suffix=='.png'
    subprocess.run(['cwebp','-quiet','-q','92','-alpha_q','100',str(source),'-o',str(target)],check=True)
    entry={**job,'packaged_path':str(target.relative_to(ROOT)),'source_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'packaged_sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'packaged_bytes':target.stat().st_size,'processing':'Standard WebP encoding only; no compositing, repainting or source pixel edits. Authored sprite windows measured separately.'}
    manifest['jobs']=[e for e in manifest['jobs'] if e['key']!=job['key']]+[entry]
    print(job['key'],entry['packaged_bytes'])
manifest_path.write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
