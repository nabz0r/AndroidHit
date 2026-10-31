import datetime,hashlib,json,re,subprocess
from pathlib import Path
root=Path('/Users/n/androidhit');out=Path('/tmp/luma124-ci-home-fix')
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def bound(p):return {'path':str(p),'sha256':sha(p),'bytes':p.stat().st_size}
def git(*args):return subprocess.check_output(['git',*args],cwd=root,text=True).strip()
first=json.loads((out/'remaining/receipt.json').read_text());retry=json.loads((out/'remaining-fixed-20-22/receipt.json').read_text());sparse=json.loads((out/'sparse-renderer/receipt.json').read_text())
assert len(first['steps'])==20 and first['steps'][-1]['exitCode']!=0
assert retry['status']=='PASS' and len(retry['steps'])==3 and sparse['status']=='PASS'
accepted=first['steps'][:19]+retry['steps'];assert len(accepted)==22 and all(row['exitCode']==0 for row in accepted)
site=json.loads((root/'docs/validation/site-v1.24/release/source-manifest.json').read_text());native=json.loads((root/'docs/validation/v1.24/candidate-d/candidate.json').read_text())
for name,digest in site['frozen_inputs'].items():assert sha(root/name)==digest,name
for row in native['source_inputs']:assert sha(root/row['path'])==row['sha256'],row['path']
changed=['.github/workflows/puzzle-contracts.yml','scripts/HomeV115GeometryTest.java','scripts/HomeV115Test.java','scripts/ProfileSummonV116Test.java','scripts/test-journey-world-v122.sh']
base='39c0985b24ecc4cf6ef00b6eb1c1392ebe3758da';head='66119799d6fbe5254a8d37d50db76afe8daffbdc';assert git('rev-parse','HEAD')==head
actual=git('diff','--name-only',base,head).splitlines();assert sorted(actual)==sorted(changed+['docs/validation/v1.24/publication/operator-preparation/stage-c2-observation.json']),actual
assert not git('status','--porcelain');subprocess.run(['git','diff','--check',base,head],cwd=root,check=True)
references={};todo=['.github/workflows/puzzle-contracts.yml'];seen=set()
while todo:
 path=todo.pop()
 if path in seen or not(root/path).is_file():continue
 seen.add(path);refs=sorted(set(re.findall(r'scripts/[A-Za-z0-9_./-]+\.(?:sh|java|mjs|py)',(root/path).read_text(errors='replace'))));references[path]=refs;todo.extend(ref for ref in refs if ref not in seen)
assert 'scripts/HomeV113Test.java' not in seen and 'scripts/ObjectiveModelsV110Test.java' not in seen
receipt={
 'status':'PASS','completedAtUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),
 'scope':'Local host rerun of all 22 remaining test steps from the first HomeV115 step through the end of the puzzle-contracts job. This is not a new GitHub CI run, native execution, APK rebuild or public-site acceptance.',
 'parentPublishedCorrectedCommit':head,'previousC2Commit':base,'parentAdditionalObservation':'docs/validation/v1.24/publication/operator-preparation/stage-c2-observation.json','gitCommitOrPushByThisAgent':False,
 'appOrPublicMutation':False,'networkRequestedByLocalChecks':False,'changedFiles':[bound(root/p)for p in changed],
 'compatibility':{'historicalFurnitureIdsAndPrices':42,'explicitAddedFurniture':6,'totalFurniture':48,'freeIds':9,'initialPlacedDesigns':9,'decorator10RemainsLocked':True,'homeV115Assertions':2440,'profileSummonV116Assertions':60722,'geometryPreviewPoses':96,'geometryPreviewSize':[1380,3680]},
 'audit':{'referenceClosureFiles':len(seen),'javaTestOrSimulationFiles':len([p for p in seen if p.endswith('.java') and ('Test' in p or 'Simulation' in p)]),'method':'Read tests reached by literal scripts/... .java/.sh/.mjs/.py references from puzzle-contracts; inspect 42, starter/free/fallback expectations. Bounded static audit, not arbitrary dynamic-call discovery.','inactiveHistoricalTestsLeftIntact':['scripts/HomeV113Test.java','scripts/ObjectiveModelsV110Test.java'],'note':'HomeV113 has old home counts but is outside active workflow references. ObjectiveModelsV110 catalog_goal_items=42 is a different cosmetics/equipment catalogue and must not be changed to furniture 48.'},
 'renderer':{'android36Compile':'PASS','sourceOnlySparseCompile':'PASS','sparseCopiedFiles':sparse['fileCount'],'noSdkBranch':'PASS, pure 171865 model checks and explicit skipped-renderer message','note':'Resolve real Pulse/Scenic/art dependency closure via sourcepath. Seven needed UI source paths added to sparse. Existing ArtLibrary deprecation warnings retained in original output.'},
 'remainingWorkflowTestSteps':accepted,
 'frozenInputs':{'site':len(site['frozen_inputs']),'native':len(native['source_inputs']),'allSha256Unchanged':True,'siteManifest':bound(root/'docs/validation/site-v1.24/release/source-manifest.json'),'nativeCandidate':bound(root/'docs/validation/v1.24/candidate-d/candidate.json')},
 'runReceipts':[bound(out/'remaining/receipt.json'),bound(out/'remaining-fixed-20-22/receipt.json'),bound(out/'sparse-renderer/receipt.json')],
 'preservedFailures':[bound(out/'before-home115.log'),bound(out/'before-profile.log'),bound(out/'remaining/20.log')],
 'extraOutput':[bound(out/'historical-42-contract.json'),bound(out/'renderer-fixed.log'),bound(out/'no-sdk-branch.log')],
 'limits':['Original failing attempt includes 19 accepted test steps followed by one renderer-compile failure. It remains FAIL; only its first 19 PASS steps are joined with the three-step successful retry.','Cached org.json 20240303 parser has exactly the workflow SHA256. Dependency network-download step was omitted locally; the same two wallet/energy test commands ran.','Home report and Java2D preview output paths moved only to RUNNER_TEMP to preserve historical evidence. Preview is an approximation, not native-art validation.','Other workflow jobs and already-passed pre-HomeV115 stages not rerun locally. Remote CI must still run on final patched source.']}
(out/'final-receipt.json').write_text(json.dumps(receipt,indent=2)+'\n')
(out/'README.md').write_text('''# Local correction of historical home contracts

PASS for all 22 remaining puzzle-contracts test steps, beginning with HomeV115. This is local host validation, not GitHub CI or new native acceptance.

The updated contract preserves each of the 42 historical furniture IDs and earned prices from main 5cc and checks six explicit additions. It verifies 48 furniture identities and nine free designs. ProfileSummon checks the exact nine initial placed designs and keeps decorator10 locked. The optional Java2D contact sheet includes all 96 poses.

The original remaining/receipt.json stays FAIL (19 PASS stages then a renderer-compile failure). The successful remaining-fixed-20-22 receipt covers map/tactical/session, premium refill and wallet transport, and POWER reward policy. final-receipt.json joins only accepted steps and binds original outputs. Historical HomeV113 is outside the active workflow and was not edited; ObjectiveModelsV110 count 42 concerns a different catalogue.

JourneyWorld compilation now resolves actual Pulse/Scenic/art source dependencies. Seven necessary paths were added to workflow sparse checkout. It passes in the full tree and in a source-only sparse copy; the no-SDK branch passes pure model tests and explicitly skips rendering. Existing Android deprecation warnings remain in logs.

All69site frozen inputs and all 522 native candidate-D inputs remain SHA256-identical. No app/public modification, commit, publication, APK build or emulator action was performed by this agent. Parent published/adopted C2b while this local receipt was consolidated.
''')
print(json.dumps({'status':'PASS','receipt':str(out/'final-receipt.json'),'sha256':sha(out/'final-receipt.json'),'steps':len(accepted),'siteInputs':len(site['frozen_inputs']),'nativeInputs':len(native['source_inputs']),'correctedCommit':head},indent=2))
