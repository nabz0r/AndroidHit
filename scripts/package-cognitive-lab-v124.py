#!/usr/bin/env python3
"""Prepare the isolated 1.24 report and exact-source kit. Never deploy or rerun Java."""
import argparse,hashlib,json,pathlib,re,shutil,subprocess,tempfile,zipfile
ROOT=pathlib.Path(__file__).resolve().parents[1]
DOC=ROOT/'docs/validation/v1.24/simulation'; DATA=DOC/'run-2026103102'
REPORT=ROOT/'docs/validation/v1.24/report'; WEB=ROOT/'public/lab/clarity-124'
ap=argparse.ArgumentParser();ap.add_argument('--ui-context-sha256');args=ap.parse_args()
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def load(p):return json.loads(p.read_text())
def encoded(x):return (json.dumps(x,ensure_ascii=False,sort_keys=True,indent=2)+'\n').encode()
subprocess.run(['python3',str(ROOT/'scripts/verify-cognitive-lab-v124.py'),'--data',str(DATA),'--require-current-source'],cwd=ROOT,check=True,capture_output=True)
summary,analysis,provenance=load(DATA/'summary.json'),load(DOC/'analysis.json'),load(DATA/'provenance.json')
assert summary['totals']['trajectories']==8000 and summary['config']['seed']==2026103102
assert analysis['provenanceSha256']==sha(DATA/'provenance.json')
historical={str(p.relative_to(ROOT)):sha(p) for folder in ['public/lab/cognitive','public/lab/clarity-123'] for p in (ROOT/folder).rglob('*') if p.is_file()}
view={'schemaVersion':1,'modelVersion':summary['config']['modelVersion'],'seed':2026103102,'humanParticipants':0,'counts':analysis['counts'],'sourceCount':len(provenance['sourceSha256']),'provenanceSha256':sha(DATA/'provenance.json'),'scenarios':[],'transfer':analysis['commonEnvironmentTransfer'],'paired':analysis['pairedComparisons'],'inputHashes':{str(p.relative_to(ROOT)):sha(p) for p in [DATA/'summary.json',DOC/'analysis.json',DOC/'protocol.json',DATA/'provenance.json']}}
for row in summary['scenarios']:
 a=analysis['scenarios'][row['id']]; t=a['totals']; o=a['outcomes']; days=a['dailyStops']
 view['scenarios'].append({'id':row['id'],'meanLevels':row['meanLevelsCleared'],'starts':a['starts'],'exits':t['exits'],'blocked':t['blocked'],'exhausted':o.get('budget_exhausted',0),'horizon':o.get('horizon_censored',0),'manualExits':t['manualExits'],'automatedExits':t['automatedExits'],'directCueManual':row['directCueFollowedManualExits'],'causeVisibleManual':row['manualExitsWithCauseVisible'],'energySpent':a['resources']['energySpent'],'coinsSpent':a['resources']['coinsSpent'],'undos':t['undos'],'repeatedErrors':t['repeatedErrors'],'days':a['configuredDays'],'energyEmpty':days.get('energy_empty',0),'dayCap':days.get('day_action_cap',0),'quantiles':a['trajectoryQuantiles'],'progression':a['companionProgression'],'levels':[{'level':n,'arrivals':a['perLevel'][str(n)]['arrivingTrajectories'],'starts':a['perLevel'][str(n)]['starts'],'wins':a['perLevel'][str(n)]['outcomes'].get('completed',0),'exhausted':a['perLevel'][str(n)]['outcomes'].get('budget_exhausted',0),'horizon':a['perLevel'][str(n)]['outcomes'].get('horizon_censored',0),'undos':a['perLevel'][str(n)]['totals']['undos']} for n in [27,33,36,43,48]]})
assert len(view['scenarios'])==4 and view['sourceCount']==68
(WEB/'data').mkdir(exist_ok=True);(WEB/'raw').mkdir(exist_ok=True);REPORT.mkdir(exist_ok=True)
(WEB/'data/view.json').write_bytes(encoded(view));(WEB/'data/snapshot.js').write_text('window.CLARITY124 = '+json.dumps(view,ensure_ascii=False,sort_keys=True,separators=(',',':'))+';\n')
shutil.copyfile(DATA/'provenance.json',WEB/'data/provenance.json')
raw=[]
for p in sorted(DATA.glob('*.csv.gz')):
 shutil.copyfile(p,WEB/'raw'/p.name);raw.append({'path':'raw/'+p.name,'sha256':sha(p),'bytes':p.stat().st_size})
assert len(raw)==11
(WEB/'data/raw-manifest.json').write_bytes(encoded(raw))
index=(WEB/'index.html').read_text()
index=re.sub(r'(<nav id="raw-downloads"[^>]*>).*?(</nav>)',lambda m:m[1]+''.join('<a href="https://luma-drift-production.web.app/lab/clarity-124/'+p['path']+'">'+pathlib.Path(p['path']).name+'</a>' for p in raw)+m[2],index,flags=re.S)
for name,attribute in [('report.css','href'),('report.js','src'),('data/snapshot.js','src')]:
 index,n=re.subn(attribute+'="'+re.escape(name)+r'(?:\?v=[^"]*)?"',attribute+'="'+name+'?v='+sha(WEB/name)[:16]+'"',index);assert n==1
(WEB/'index.html').write_text(index)
entries={}
def add(path):
 p=pathlib.Path(path);assert not p.is_absolute() and '..' not in p.parts
 entries[str(p)]=(ROOT/p).read_bytes()
for path,h in provenance['sourceSha256'].items():assert sha(ROOT/path)==h;add(path)
ui_path='app/src/main/java/com/androidhit/lumadrift/LumaView.java';ui_sha=sha(ROOT/ui_path)
if args.ui_context_sha256:assert ui_sha==args.ui_context_sha256,'Uncompiled UI context changed'
add(ui_path)
for name in ['README.md','protocol.json','preflight.json','execution-original.log','execution-receipt.json','analysis.json','validation.json','manifest.json']:add((DOC/name).relative_to(ROOT))
for p in sorted(DATA.glob('*.json')):add(p.relative_to(ROOT))
for path in ['scripts/analyze-cognitive-lab-v124.py','scripts/package-cognitive-lab-v124.py','scripts/test-cognitive-report-v124.mjs']:
 if (ROOT/path).is_file():add(path)
for p in [WEB/'index.html',WEB/'report.css',WEB/'report.js',*sorted((WEB/'data').glob('*'))]:add(p.relative_to(ROOT))
entries['README-KIT.txt']='''Luma Drift 1.24 synthetic study. Zero humans; not Android rendering evidence.
Java17 and Python3. From extracted root:
 bash scripts/test-cognitive-lab-v124.sh
 bash scripts/run-cognitive-lab-v124.sh --profiles 100 --repetitions 20 --seed 2026103102 --days 8 --output /tmp/luma124-new-run
This is a SOURCE-ONLY kit: no raw CSV gzip files are included.
Run the command into a fresh output directory and compare deterministic output hashes against the bundled provenance JSON.
Never overwrite historical data. Open public/lab/clarity-124/index.html offline.
Home and individual public download links require network; report rendering does not.
The 104 oracle controls are separate from 8000 limited-perception trajectories.
GUIDE repeat requests can refresh an existing cue. No human/retention/fun claims.
LumaView.java is an extra uncompiled context required by a historical source-hash helper; it is not one of the 68 executed sources or an Android rendering qualification.

Raw ledgers (download independently and verify SHA256/bytes):
'''.encode()+''.join('https://luma-drift-production.web.app/lab/clarity-124/'+p['path']+' SHA256 '+p['sha256']+' bytes '+str(p['bytes'])+'\n' for p in raw).encode()
kit={'schemaVersion':1,'modelVersion':view['modelVersion'],'runtimeSourceCount':68,'rawLedgersIncluded':False,'rawDownloadManifest':raw,'runProvenanceSha256':view['provenanceSha256'],'noAndroidUiSourceClaim':True,'uncompiledUiContext':{'path':ui_path,'sha256':ui_sha,'notCompiledByDriver':True,'reason':'Historical sourceHashes helper reads this file before excluding it from runtime provenance.'},'files':{name:{'sha256':hashlib.sha256(body).hexdigest(),'bytes':len(body)} for name,body in sorted(entries.items())}}
kit_bytes=encoded(kit);entries['KIT-MANIFEST.json']=kit_bytes
archive=WEB/'cognitive-lab-1.24-source.zip'
with tempfile.TemporaryDirectory(prefix='luma124-kit-') as temp:
 target=pathlib.Path(temp)/archive.name
 with zipfile.ZipFile(target,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=9) as z:
  for name,body in sorted(entries.items()):
   info=zipfile.ZipInfo(name,(2026,10,31,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=0o100644<<16;z.writestr(info,body)
 with zipfile.ZipFile(target) as z:
  assert z.testzip() is None
  for name,body in entries.items():assert z.read(name)==body,name
 shutil.copyfile(target,archive)
for path,h in historical.items():assert sha(ROOT/path)==h,path
(REPORT/'source-kit-manifest.json').write_bytes(kit_bytes)
manifest={'schemaVersion':1,'kind':'isolated124report-local-preparation-not-deployment','runProvenanceSha256':view['provenanceSha256'],'archiveSha256':sha(archive),'archiveBytes':archive.stat().st_size,'kitManifestSha256':hashlib.sha256(kit_bytes).hexdigest(),'runtimeSourceCount':68,'rawLedgersIncludedInZip':False,'uncompiledUiContextSha256':ui_sha,'uiContextPinnedByCaller':bool(args.ui_context_sha256),'historicalFilesUnchanged':historical,'rawLedgers':raw,'publicFiles':{str(p.relative_to(ROOT)):{'sha256':sha(p),'bytes':p.stat().st_size} for p in sorted(WEB.rglob('*')) if p.is_file()}}
(REPORT/'site-manifest.json').write_bytes(encoded(manifest))
print(json.dumps({'status':'PASS','archiveSha256':sha(archive),'archiveBytes':archive.stat().st_size,'kitFiles':len(entries),'historicalFilesPreserved':len(historical)},indent=2))
