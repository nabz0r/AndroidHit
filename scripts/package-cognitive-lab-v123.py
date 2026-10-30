#!/usr/bin/env python3
"""Prepare an isolated1.23 report and reproducible source kit; never deploys."""
import argparse,hashlib,json,pathlib,re,shutil,subprocess,tempfile,zipfile
ROOT=pathlib.Path(__file__).resolve().parents[1]
DOC=ROOT/'docs/validation/cognitive-lab-v123';DATA=DOC/'latest';WEB=ROOT/'public/lab/clarity-123'
ap=argparse.ArgumentParser();ap.add_argument('--ui-context-sha256');args=ap.parse_args()
def sha_bytes(b):return hashlib.sha256(b).hexdigest()
def sha(p):return sha_bytes(p.read_bytes())
def load(p):return json.loads(p.read_text())
def encoded(x):return (json.dumps(x,ensure_ascii=False,sort_keys=True,indent=2)+'\n').encode()
subprocess.run(['python3',str(ROOT/'scripts/verify-cognitive-lab-v123.py'),'--require-current-source'],cwd=ROOT,check=True,capture_output=True)
summary,analysis,paired,provenance=load(DATA/'summary.json'),load(DOC/'analysis.json'),load(DATA/'paired-comparisons.json'),load(DATA/'provenance.json')
assert summary['totals']['trajectories']==8000 and summary['config']['seed']==2026103002
assert analysis['provenanceSha256']==sha(DATA/'provenance.json')
old=ROOT/'public/lab/cognitive';old_files={str(p.relative_to(ROOT)):sha(p) for p in old.rglob('*') if p.is_file()}
ui=ROOT/'app/src/main/java/com/androidhit/lumadrift/LumaView.java';ui_sha=sha(ui)
if args.ui_context_sha256:assert ui_sha==args.ui_context_sha256,'Final UI context changed'
view={'schemaVersion':1,'modelVersion':summary['config']['modelVersion'],'seed':summary['config']['seed'],'humanParticipants':0,'counts':{**analysis['counts'],'blockedEvents':load(DOC/'validation.json')['blockedEvents']},'sourceCount':len(provenance['sourceSha256']),'provenanceSha256':sha(DATA/'provenance.json'),'scenarios':[],'transfer':paired['commonEnvironmentTransfer']}
for row in summary['scenarios']:
 a=analysis['scenarios'][row['id']];t=a['totals'];o=a['outcomes'];days=a['dailyStops']
 view['scenarios'].append({'id':row['id'],'meanLevels':row['meanLevelsCleared'],'starts':a['starts'],'exits':t['exits'],'blocked':t['blocked'],'exhausted':o.get('budget_exhausted',0),'horizon':o.get('horizon_censored',0),'manualExits':t['manualExits'],'automatedExits':t['automatedExits'],'directCueManual':row['directCueFollowedManualExits'],'causeVisibleManual':row['manualExitsWithCauseVisible'],'energySpent':a['resources']['energySpent'],'coinsSpent':a['resources']['coinsSpent'],'undos':t['undos'],'repeatedErrors':t['repeatedErrors'],'days':a['configuredDays'],'energyEmpty':days.get('energy_empty',0),'dayCap':days.get('day_action_cap',0),'levels':[{'level':n,'starts':a['perLevel'][str(n)]['starts'],'exhausted':a['perLevel'][str(n)]['outcomes'].get('budget_exhausted',0),'undos':a['perLevel'][str(n)]['totals']['undos']} for n in [27,43,48]]})
(WEB/'data').mkdir(exist_ok=True)
(WEB/'data/view.json').write_bytes(encoded(view))
(WEB/'data/snapshot.js').write_text('window.CLARITY123 = '+json.dumps(view,ensure_ascii=False,sort_keys=True,separators=(',',':'))+';\n')
shutil.copyfile(DATA/'provenance.json',WEB/'data/provenance.json')
index=(WEB/'index.html').read_text()
for name,attribute in [('report.css','href'),('report.js','src'),('data/snapshot.js','src')]:
 pattern=attribute+'="'+re.escape(name)+r'(?:\?v=[^"]*)?"'
 index,n=re.subn(pattern,attribute+'="'+name+'?v='+sha(WEB/name)[:16]+'"',index);assert n==1,name
(WEB/'index.html').write_text(index)
entries={}
def add(path):
 p=pathlib.Path(path);assert not p.is_absolute() and '..' not in p.parts
 entries[str(p)]=(ROOT/p).read_bytes()
for path,h in provenance['sourceSha256'].items():assert sha(ROOT/path)==h;add(path)
# Legacy source-hash helpers read this file, but1.23 removes it from runtime hashes.
# Include its actual bytes as an explicitly uncompiled, separately bound context.
add('app/src/main/java/com/androidhit/lumadrift/LumaView.java')
for path in ['scripts/analyze-cognitive-lab-v123.py','scripts/package-cognitive-lab-v123.py','scripts/test-cognitive-report-v123.mjs']:
 add(path)
for p in DATA.iterdir():
 if p.is_file():add(p.relative_to(ROOT))
for name in ['README.md','PROTOCOL.md','analysis.json','validation.json','source-binding.json','run-authorization.json','run-original.log','preflight/legacy-replay-comparison.json','preflight/legacy-replay-provenance.json','preflight/legacy-replay-original.log','preflight/final-tests-original.log']:
 add((DOC/name).relative_to(ROOT))
for p in [WEB/'index.html',WEB/'report.css',WEB/'report.js',WEB/'data/snapshot.js',WEB/'data/view.json',WEB/'data/provenance.json']:add(p.relative_to(ROOT))
entries['README-KIT.txt']=('Luma Drift 1.23 synthetic local study; no human evidence.\n\nFrom the extracted root, Java 17 and Python3 are required:\n  bash scripts/test-cognitive-lab-v123.sh\n  python3 scripts/verify-cognitive-lab-v123.py --require-current-source\n  bash scripts/run-cognitive-lab-v123.sh --profiles 100 --repetitions 20 --seed 2026103002 --output /tmp/luma-new-run\nUse spaces after option names (for example --profiles 100). Never overwrite the bundled run.\nOpen public/lab/clarity-123/index.html offline for the report. Optional home/ZIP links require network.\nLumaView.java is actual source context only, not compiled by this driver. This is not an Android build kit or qualification of rendering.\n104 oracle boards are separate from 8000 limited-perception trajectories.\n').encode()
kit={'schemaVersion':1,'modelVersion':summary['config']['modelVersion'],'runProvenanceSha256':sha(DATA/'provenance.json'),'runtimeSourceCount':len(provenance['sourceSha256']),'uncompiledUiContext':{'path':'app/src/main/java/com/androidhit/lumadrift/LumaView.java','sha256':ui_sha,'notCompiledByDriver':True},'files':{n:{'sha256':sha_bytes(b),'bytes':len(b)} for n,b in sorted(entries.items())}}
kit_bytes=encoded(kit);entries['KIT-MANIFEST.json']=kit_bytes
archive=WEB/'cognitive-lab-1.23-source.zip'
with tempfile.TemporaryDirectory(prefix='luma123-kit-') as tmp:
 target=pathlib.Path(tmp)/archive.name
 with zipfile.ZipFile(target,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=9) as z:
  for name,body in sorted(entries.items()):
   info=zipfile.ZipInfo(name,(2026,10,30,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=0o100644<<16;z.writestr(info,body)
 with zipfile.ZipFile(target) as z:
  assert z.testzip() is None
  for name,body in entries.items():assert z.read(name)==body,name
 shutil.copyfile(target,archive)
for name,h in old_files.items():assert sha(ROOT/name)==h,('Historical report changed',name)
(DOC/'source-kit-manifest.json').write_bytes(kit_bytes)
public_files={str(p.relative_to(ROOT)):{'sha256':sha(p),'bytes':p.stat().st_size} for p in sorted(WEB.rglob('*')) if p.is_file()}
manifest={'schemaVersion':1,'kind':'isolated123report-local-preparation-not-deployment','runProvenanceSha256':sha(DATA/'provenance.json'),'archiveSha256':sha(archive),'archiveBytes':archive.stat().st_size,'kitManifestSha256':sha_bytes(kit_bytes),'uncompiledUiContextSha256':ui_sha,'uiContextPinnedByCaller':bool(args.ui_context_sha256),'historicalCognitiveFilesUnchanged':old_files,'rawCsvNotDuplicatedIntoPublic':True,'publicFiles':public_files}
(DOC/'site-manifest.json').write_bytes(encoded(manifest))
print(json.dumps({'status':'PASS','archiveSha256':sha(archive),'archiveBytes':archive.stat().st_size,'kitFiles':len(entries),'siteManifestSha256':sha(DOC/'site-manifest.json'),'historicalCognitiveFilesPreserved':len(old_files),'uiContextSha256':ui_sha},indent=2))
