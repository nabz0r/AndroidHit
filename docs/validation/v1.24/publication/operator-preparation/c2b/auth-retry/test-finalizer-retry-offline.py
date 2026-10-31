from pathlib import Path
import importlib.util,types,tempfile,json,sys,contextlib,io,unittest,hashlib
from unittest.mock import patch
SCRIPT=Path('/tmp/luma124-cloud-finalize-retry.py')
TOKEN=b'offline-dummy-token-not-a-credential-0123456789'
def digest(b):return hashlib.sha256(b).hexdigest()
def module():
 s=importlib.util.spec_from_file_location('finalizer124',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class FinalizerOffline(unittest.TestCase):
 def fixture(self):
  m=module();work=Path(tempfile.mkdtemp(prefix='luma124-finalizer-offline-')).resolve();m.WORK=work
  checkout=work/'source';original=work/'receipts';receipts=work/'receipts-c2b'
  for p in (checkout/'.git',original,receipts):p.mkdir(parents=True,exist_ok=True)
  files={}
  def write(name,data):
   b=data if isinstance(data,bytes) else (json.dumps(data,sort_keys=True)+'\n').encode();p=checkout/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b);files[name]=b;return digest(b)
  frozen={}
  for name in ['scripts/deploy-site-v124.py','scripts/deploy-cognitive-overlay.py']+['fixture/input-%02d.java'%i for i in range(67)]:frozen[name]=write(name,('fixture '+name).encode())
  cfgsha=write('firebase.json',{'hosting':{'site':'luma-drift-production','public':'public'}})
  release={'edition':'1.24','version':'1.24.0-rc2','version_code':33,'apks':{}}
  public={};apks={}
  for channel,b in [('production',b'fake production bytes'),('lab',b'fake lab bytes')]:
   version='1.24.0-sandbox-rc2' if channel=='lab' else '1.24.0-rc2';name='releases/luma-drift-'+version+'.apk';sha=write('public/'+name,b)
   setattr(m,'PROD' if channel=='production' else 'LAB',sha);setattr(m,'PROD_BYTES' if channel=='production' else 'LAB_BYTES',len(b))
   release['apks'][channel]={'path':name,'sha256':sha,'bytes':len(b),'version':version,'code':33,'signer_certificate_sha256':m.SIGNER}
   public['/'+name]={'sha256':sha,'bytes':len(b)};apks[channel]={'path':'public/'+name,'sha256':sha,'bytes':len(b),'reconstructed':False}
  manifest='releases/manifest-1.24.0-rc2.json';manifestsha=write(manifest,release)
  source={'status':'PASS','edition':'1.24','release':'1.24.0-rc2','native_capture_provenance_verified':True,'package_identity_verified':True,'production_apk_sha256':m.PROD,'lab_apk_sha256':m.LAB,'artifact_source_commit':m.C1,'previous_source_commit':m.PREVIOUS,'previous_deployment_source_commit':m.PREVIOUS_DEPLOY,'baseline_public_tree':m.BASELINE_PUBLIC_TREE,'frozen_inputs':frozen,'firebase_config_sha256':cfgsha,'artifact_manifest':manifest,'artifact_manifest_sha256':manifestsha,'files':public}
  m.SOURCE_SHA=write(m.SOURCE,source)
  op={'status':'STAGED_ONLY','source_commit':m.ORIGINAL_COMMIT,'staging_passed':True,'script_sha256':m.OPERATOR_SHA,'repository':m.REPO,'expected_live':m.LIVE,'previous_source_commit':m.PREVIOUS,'production_apk_sha256':m.PROD,'lab_apk_sha256':m.LAB,'artifact_source_commit':m.C1,'workspace':str(work),'checkout':str(checkout),'receipts':str(original),'staging':str(original/'staging.json'),'deployment':str(original/'deployment/deployment.json')}
  old={'passed':True,'sourceCommit':m.ORIGINAL_COMMIT,'sourceManifestSha256':m.SOURCE_SHA,'downloadBasesRequested':True,'authenticated':False,'uploaded':False,'deployed':False}
  def save(p,x):p.write_text(json.dumps(x,sort_keys=True)+'\n');return digest(p.read_bytes())
  m.ORIGINAL_OP_SHA=save(original/'operator.json',op);m.ORIGINAL_STAGE_SHA=save(original/'staging.json',old)
  stage={'passed':True,'sourceCommit':m.C2,'sourceManifestSha256':m.SOURCE_SHA,'previousSourceCommit':m.PREVIOUS,'previousDeploymentSourceCommit':m.PREVIOUS_DEPLOY,'baselinePhysicalPaths':576,'baselinePublicTree':m.BASELINE_PUBLIC_TREE,'retiredPaths':[],'downloadBasesRequested':False,'downloadedBaseChannels':[],'authenticated':False,'uploaded':False,'deployed':False,'apks':apks}
  stagehash=save(receipts/'staging.json',stage)
  lift={'status':'STAGED_ONLY','originalStageCommit':m.ORIGINAL_COMMIT,'sourceCommit':m.C2,'originalOperatorSha256':m.ORIGINAL_OP_SHA,'originalStagingSha256':m.ORIGINAL_STAGE_SHA,'scriptSha256':m.RESTAGE_SHA,'stagingSha256':stagehash,'frozenInputsChecked':69,'workspace':str(work),'checkout':str(checkout),'authenticated':False,'uploaded':False,'deployed':False}
  save(receipts/'restage-operator.json',lift)
  m.PREVIOUS_AUTH_FAILURE_SHA=save(receipts/'final-operator.json',{'status':'FAIL','source_commit':m.C2,'error':'gcloud token acquisition failed; no auth fallback attempted'})
  calls=[];fault={'auth':False,'git':None}
  def git(cmd,**kw):
   calls.append((cmd,None));parts=cmd[5:]
   if parts==['rev-parse','HEAD']:return ((m.ORIGINAL_COMMIT if fault['git']=='head' else m.C2)+'\n').encode()
   if parts==['rev-parse','HEAD^{tree}']:return (m.C2_TREE+'\n').encode()
   if parts==['rev-parse','HEAD:public']:return (m.PUBLIC_TREE+'\n').encode()
   if parts==['rev-parse','HEAD:app']:return (m.APP_TREE+'\n').encode()
   if parts[:1]==['status']:return b' M dirty\n' if fault['git']=='dirty' else b''
   if parts==['remote','get-url','origin']:return (m.REPO+'\n').encode()
   if parts[:1]==['diff']:return ('\n'.join(m.ALLOWED_DELTA)+('\nunexpected.java' if fault['git']=='delta' else '')+'\n').encode()
   if parts[:1]==['show']:return files[parts[1].split(':',1)[1]]
   raise AssertionError('Unexpected process '+repr(cmd))
  def run(cmd,**kw):
   calls.append((cmd,kw.get('input')))
   if cmd[:3]==['gcloud','auth','print-access-token']:return types.SimpleNamespace(returncode=1 if fault['auth'] else 0,stdout=TOKEN)
   assert cmd[1]=='scripts/deploy-site-v124.py';assert kw['input']==TOKEN+b'\n';assert TOKEN.decode() not in ' '.join(cmd)
   out=Path(cmd[cmd.index('--output')+1]);out.mkdir();(out/'deployment.json').write_text(json.dumps({'status':'DEPLOY/FINALIZED','sourceCommit':m.C2,'version':'sites/luma-drift-production/versions/offline-only'}))
   kw['stdout'].write(b'offline fake deploy only\n');return types.SimpleNamespace(returncode=0)
  m.subprocess=types.SimpleNamespace(check_output=git,run=run,PIPE=-1,DEVNULL=-3,STDOUT=-2);m.shutil=types.SimpleNamespace(which=lambda _: '/offline/gcloud')
  m.sys=types.SimpleNamespace(platform='linux',executable=sys.executable)
  return m,work,calls,fault
 def maincall(self,m,work,mode):
  out=io.StringIO()
  with patch.object(sys,'argv',[str(SCRIPT),str(work),m.C2,mode]),contextlib.redirect_stdout(out):rc=m.main()
  self.assertNotIn(TOKEN.decode(),out.getvalue())
  for p in work.rglob('*'):
   if p.is_file():self.assertNotIn(TOKEN,p.read_bytes())
  return rc,out.getvalue()
 def test_production_constants_match_current_accepted_metadata(self):
  m=module();r=json.loads(Path('/Users/n/androidhit/releases/manifest-1.24.0-rc2.json').read_text());self.assertEqual((m.PROD,m.LAB,m.PROD_BYTES,m.LAB_BYTES),(r['apks']['production']['sha256'],r['apks']['lab']['sha256'],r['apks']['production']['bytes'],r['apks']['lab']['bytes']));self.assertEqual(r['version_code'],33)
 def test_complete_check_only_has_no_auth_writes_clone_fetch_or_stage(self):
  m,w,c,f=self.fixture();before={str(p):p.read_bytes() for p in w.rglob('*') if p.is_file()};rc,out=self.maincall(m,w,'--check-only');self.assertEqual(rc,0);self.assertIn('VALIDATED_ONLY',out);self.assertEqual(before,{str(p):p.read_bytes() for p in w.rglob('*') if p.is_file()});self.assertTrue(all(x[0]=='git' for x,_ in c));self.assertFalse(any('fetch' in x or 'clone' in x for x,_ in c))
 def test_deploy_uses_stdin_and_separate_receipts_preserves_both_stages(self):
  m,w,c,f=self.fixture();before={p:p.read_bytes() for d in ('receipts','receipts-c2b') for p in (w/d).glob('*.json')};rc,_=self.maincall(m,w,'--deploy');self.assertEqual(rc,0);self.assertTrue(all(p.read_bytes()==b for p,b in before.items()));self.assertEqual(json.loads((w/'receipts-c2b/final2-operator.json').read_text())['status'],'DEPLOY/FINALIZED');deploy=[x for x,_ in c if len(x)>1 and x[1]=='scripts/deploy-site-v124.py'];self.assertEqual(len(deploy),1);self.assertEqual(deploy[0][deploy[0].index('--commit')+1],m.C2);self.assertIn('--token-stdin',deploy[0]);self.assertFalse(any('clone' in x or 'fetch' in x for x,_ in c))
 def test_auth_failure_records_fail_never_calls_deployer(self):
  m,w,c,f=self.fixture();f['auth']=True;rc,_=self.maincall(m,w,'--deploy');self.assertEqual(rc,1);self.assertFalse(any(len(x)>1 and x[1]=='scripts/deploy-site-v124.py' for x,_ in c));self.assertEqual(json.loads((w/'receipts-c2b/final2-operator.json').read_text())['status'],'FAIL')
 def test_dirty_checkout_stops_before_auth(self):
  m,w,c,f=self.fixture();f['git']='dirty'
  with self.assertRaises(RuntimeError):self.maincall(m,w,'--deploy')
  self.assertTrue(all(x[0]=='git' for x,_ in c))
 def test_unexpected_delta_stops_before_auth(self):
  m,w,c,f=self.fixture();f['git']='delta'
  with self.assertRaises(RuntimeError):self.maincall(m,w,'--deploy')
  self.assertTrue(all(x[0]=='git' for x,_ in c))
 def test_original_receipt_tamper_stops_before_auth(self):
  m,w,c,f=self.fixture();(w/'receipts/operator.json').write_text('{}')
  with self.assertRaises(RuntimeError):self.maincall(m,w,'--deploy')
  self.assertEqual(c,[])
 def test_existing_finalization_refused(self):
  m,w,c,f=self.fixture();(w/'receipts-c2b/final2-operator.json').write_text('{}')
  with self.assertRaises(RuntimeError):self.maincall(m,w,'--deploy')
  self.assertEqual(c,[])
 def test_wrong_target_refused(self):
  m,w,c,f=self.fixture()
  with self.assertRaises(RuntimeError):m.validate(str(w),m.ORIGINAL_COMMIT,m.environment())
  self.assertEqual(c,[])
 def test_apk_corruption_stops_before_auth(self):
  m,w,c,f=self.fixture();(w/'source/public/releases/luma-drift-1.24.0-rc2.apk').write_bytes(b'bad')
  with self.assertRaises(RuntimeError):self.maincall(m,w,'--deploy')
  self.assertTrue(all(x[0]=='git' for x,_ in c))
if __name__=='__main__':unittest.main()
