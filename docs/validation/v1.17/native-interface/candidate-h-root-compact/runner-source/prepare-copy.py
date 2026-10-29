#!/usr/bin/env python3
"""Snapshot local six-language labels for an SDK-only UI observer; never execute target code."""
from pathlib import Path
import hashlib,json,re
ROOT=Path(__file__).resolve().parents[2]
ALIASES={'t':'Strings','n':'V18Strings','r':'V115Strings','d':'V116Strings','a':'V110Strings','h':'HomeStrings','e':'ExperienceStrings','v':'SagaStrings','g':'GearStrings','b':'V111Strings','z':'V112Strings','q':'V19Strings','u':'UiStrings'}
rows,sources={},{}
for prefix,cls in ALIASES.items():
    path=ROOT/'app/src/main/java/com/androidhit/lumadrift/data'/f'{cls}.java'
    source=path.read_text();sources[str(path.relative_to(ROOT))]=hashlib.sha256(path.read_bytes()).hexdigest();table={}
    for match in re.finditer(r'\b(?:add|a)\(("(?:\\.|[^"\\])*")\s*,\s*((?:"(?:\\.|[^"\\])*"\s*,?\s*){6})\)',source):
        values=re.findall(r'"(?:\\.|[^"\\])*"',match.group(2))
        if len(values)==6:table[json.loads(match.group(1))]=[json.loads(value) for value in values]
    for match in re.finditer(r'case\s+"([^\"]+)"\s*:\s*row\s*=\s*new String\[\]\s*\{([^}]+)\}',source):
        values=re.findall(r'"(?:\\.|[^"\\])*"',match.group(2))
        if len(values)==6:table[match.group(1)]=[json.loads(value) for value in values]
    for match in re.finditer(r'\bTEXT\.put\("([^"]+)",\s*new String\[\]\s*\{([^}]+)\}',source):
        values=re.findall(r'"(?:\\.|[^"\\])*"',match.group(2))
        if len(values)==6:table[match.group(1)]=[json.loads(value) for value in values]
    for key,original in re.findall(r'alias\("([^"]+)","([^"]+)"\)',source):table[key]=table[original]
    assert table,cls
    rows[prefix]=table
output=ROOT/'scripts/v117-ui-test/assets/ui-copy.json'
output.write_text(json.dumps({'source_sha256':sources,'rows':rows},indent=2,ensure_ascii=False)+'\n')
print(json.dumps({'tables':len(rows),'labels':sum(map(len,rows.values())),'sha256':hashlib.sha256(output.read_bytes()).hexdigest()}))
