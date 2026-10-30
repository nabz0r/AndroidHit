#!/usr/bin/env python3
"""Build a private model fork; gate shell ablation on exact disabled-control replay."""
import difflib, hashlib, json, os, pathlib, shutil, subprocess, tempfile, datetime

HERE=pathlib.Path(__file__).resolve().parent
ROOT=HERE.parents[3]
BASE=HERE.parent/'simulation/run-2026103001'
MODEL=HERE/'model'
PROPERTY='cognitive.lab.experimentalNoShells'
SEED=2026103001
CSV_NAMES=['trajectories.csv.gz','level-results.csv.gz','blocked-events.csv.gz',
           'day-results.csv.gz','chapter-results.csv.gz','chapter-stage-results.csv.gz']
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def write(name,obj):(HERE/name).write_text(json.dumps(obj,indent=2)+'\n')
original=json.loads((BASE/'provenance.json').read_text())
assert len(original['sourceSha256'])==51 and not MODEL.exists()
original_inputs=dict(original['sourceSha256'])
stub='scripts/test-support/android/graphics/Color.java'
original_inputs[stub]=sha(ROOT/stub)
for path,expected in original_inputs.items():
    source=ROOT/path;assert sha(source)==expected,path
    target=MODEL/path;target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(source,target)
changes=[]
def patch(path, old, new):
    p=MODEL/path;before=p.read_text();assert before.count(old)==1,path
    orig=HERE/'originals'/path;orig.parent.mkdir(parents=True,exist_ok=True);orig.write_text(before)
    after=before.replace(old,new);p.write_text(after)
    changes.extend(difflib.unified_diff(before.splitlines(keepends=True),after.splitlines(keepends=True),fromfile='original/'+path,tofile='experimental/'+path))
patch('scripts/cognitive_lab/Environment.java',
      'shells=ElementalShell121.create(source,0,level);',
      'shells=Boolean.getBoolean("'+PROPERTY+'") ? ElementalShell121.empty(source) : ElementalShell121.create(source,0,level);')
patch('scripts/cognitive_lab/CognitiveLab122.java',
      'static final String VERSION="1.22-causal-cues-companions-1";',
      'static final String VERSION="1.22-causal-cues-companions-1-clarity-shell-ablation-v1";')
# Metadata-only change: same Java file already archived above, preserve its true original.
p=MODEL/'scripts/cognitive_lab/CognitiveLab122.java';before=p.read_text()
old='Map<String,Object> config=Json.obj("modelVersion",VERSION,'
new='Map<String,Object> config=Json.obj("experimentalFork",Json.obj("id","clarity-no-shell-v1","noShells",Boolean.getBoolean("'+PROPERTY+'"),"interventionScope","campaign and held-out transfer; tactical already shell-free","shipped",false),"modelVersion",VERSION,'
assert before.count(old)==1;p.write_text(before.replace(old,new))
# Rebuild a single exact diff against the archived originals, including both metadata edits.
changes=[]
for path in ['scripts/cognitive_lab/Environment.java','scripts/cognitive_lab/CognitiveLab122.java']:
    changes.extend(difflib.unified_diff((HERE/'originals'/path).read_text().splitlines(keepends=True),(MODEL/path).read_text().splitlines(keepends=True),fromfile='original/'+path,tofile='experimental/'+path))
(HERE/'minimal-model.patch').write_text(''.join(changes))
fork_inputs={path:sha(MODEL/path) for path in original_inputs}
changed=[path for path in original_inputs if original_inputs[path]!=fork_inputs[path]]
assert changed==['scripts/cognitive_lab/CognitiveLab122.java','scripts/cognitive_lab/Environment.java'] or set(changed)=={'scripts/cognitive_lab/CognitiveLab122.java','scripts/cognitive_lab/Environment.java'}
java_home=os.environ.get('JAVA_HOME','')
if java_home and (pathlib.Path(java_home)/'bin/javac').is_file():jdk=pathlib.Path(java_home)/'bin'
elif pathlib.Path('/opt/homebrew/opt/openjdk@17/bin/javac').exists():jdk=pathlib.Path('/opt/homebrew/opt/openjdk@17/bin')
else:jdk=pathlib.Path(shutil.which('javac')).parent
protocol={'schemaVersion':1,'id':'clarity-no-shell-v1','kind':'private_experimental_model_fork_not_shipped',
          'seed':SEED,'profiles':100,'repetitions':30,'scenarios':7,'trajectoriesPerFlag':21000,
          'property':PROPERTY,'flags':[False,True],'baseProvenanceSha256':sha(BASE/'provenance.json'),
          'baseSourceCommit':json.loads((HERE.parent/'simulation/source-state-before.json').read_text())['sourceCommit'],
          'originalInputsSha256':original_inputs,'forkInputsSha256':fork_inputs,'changedPaths':changed,
          'patchSha256':sha(HERE/'minimal-model.patch'),
          'hypothesis':'Remove shell interactions only; all Board geometry/budget, energy/economy, attention/search/learning/utility and explicit help policies remain unchanged.',
          'transfer':'Shells removed in both training and held-out environments for true flag. Transfer is therefore changed-environment performance, not isolated acquired learning.',
          'chapter':'Already shell-free under both flags, required byte-identical negative control.',
          'originalRuntimeUntouched':True,'humanParticipants':0}
write('protocol.json',protocol)
with tempfile.TemporaryDirectory(prefix='luma-clarity-shell-ablation-') as build:
    source_names=[stub]+[str(p.relative_to(MODEL)) for p in sorted((MODEL/'app/src/main/java/com/androidhit/lumadrift/engine').glob('*.java'))]
    source_names += ['app/src/main/java/com/androidhit/lumadrift/data/'+n+'.java' for n in ['EconomyCurve121','Catalog','CompanionBattle','CompanionBattleAction','TacticalChapterCatalog','TacticalChapterRun','TacticalChapterAction']]
    source_names += [str(p.relative_to(MODEL)) for p in sorted((MODEL/'scripts/cognitive_lab').glob('*.java'))]
    compile_cmd=[str(jdk/'javac'),'-d',build]+[str(MODEL/p) for p in source_names]
    with (HERE/'compile-original.log').open('wb') as f:subprocess.run(compile_cmd,cwd=MODEL,stdout=f,stderr=subprocess.STDOUT,check=True)
    write('compile-receipt.json',{'command':compile_cmd,'javaVersion':subprocess.run([str(jdk/'java'),'-version'],capture_output=True,text=True).stderr,'sourceCount':len(source_names),'sourceHashes':{p:sha(MODEL/p) for p in source_names},'temporaryClassesRemovedAtEnd':True})
    for flag,name in [(False,'control-disabled'),(True,'no-shell-enabled')]:
        output=HERE/name;assert not output.exists()
        command=[str(jdk/'java'),'-Xmx768m','-D'+PROPERTY+'='+str(flag).lower(),'-cp',build,'cognitive_lab.CognitiveLab122','--root',str(MODEL),'--profiles','100','--repetitions','30','--seed',str(SEED),'--output',str(output)]
        started=datetime.datetime.now(datetime.timezone.utc).isoformat()
        with (HERE/(name+'-original.log')).open('wb') as f:subprocess.run(command,cwd=MODEL,stdout=f,stderr=subprocess.STDOUT,check=True)
        summary=json.loads((output/'summary.json').read_text());assert summary['config']['experimentalFork']['noShells']==flag
        write(name+'-execution.json',{'command':command,'startedAtUtc':started,'finishedAtUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'exitCode':0,'noShells':flag,'provenanceSha256':sha(output/'provenance.json')})
        if not flag:
            comparison={n:{'base':sha(BASE/n),'forkDisabled':sha(output/n),'byteIdentical':(BASE/n).read_bytes()==(output/n).read_bytes()} for n in CSV_NAMES}
            assert all(x['byteIdentical'] for x in comparison.values()),comparison
            assert (BASE/'profiles.json').read_bytes()==(output/'profiles.json').read_bytes()
            write('disabled-replay-gate.json',{'status':'PASS','sameProfilesBytes':True,'csvFiles':comparison,'enabledExperimentMayProceed':True})
            print('Disabled fork gate PASS: six compressed CSV files byte-identical',flush=True)
        else:
            assert all((HERE/'control-disabled'/n).read_bytes()==(output/n).read_bytes() for n in CSV_NAMES[-2:])
            print('No-shell run complete; chapter negative controls byte-identical',flush=True)
for path,expected in original_inputs.items():assert sha(ROOT/path)==expected,path
print('Both full experiments complete; original source inputs untouched',flush=True)
