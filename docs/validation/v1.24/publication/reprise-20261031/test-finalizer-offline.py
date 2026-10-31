from pathlib import Path
import importlib.util,types,tempfile,json,sys,contextlib,io,unittest,hashlib
from unittest.mock import patch
SCRIPT=Path('/tmp/reprise-20261031-cloud-finalize.py');PREFIX='reprise-20261031-final2-';TOKEN=b'offline-dummy-not-a-credential-0123456789'
def digest(b):return hashlib.sha256(b).hexdigest()
def module():
 s=importlib.util.spec_from_file_location('fresh_finalizer124',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class FreshFinalizerOffline(unittest.TestCase):
 def fixture(self):
  m=module();w=Path(tempfile.mkdtemp(prefix='reprise124-offline-')).resolve();m.WORK=w;c=w/'source';r=w/'receipts'
  for p in (c/'.git',r):p.mkdir(parents=True,exist_ok=True)
  files={}
  def write(name,value):
   b=value if isinstance(value,bytes) else (json.dumps(value,sort_keys=True)+'\n').encode();p=c/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b);files[name]=b;return digest(b)
  def save(p,value):p.write_text(json.dumps(value,sort_keys=True)+'\n');return digest(p.read_bytes())
  frozen={name:write(name,('fixture '+name).encode()) for name in ['scripts/deploy-site-v124.py','scripts/deploy-cognitive-overlay.py']+['fixture/%02d.java'%i for i in range(67)]}
  configsha=write('firebase.json',{'hosting':{'site':'luma-drift-production','public':'public'}})
  release={'edition':'1.24','version':'1.24.0-rc2','version_code':33,'apks':{}};public={};apks={}
  for ch,b in [('production',b'fake production'),('lab',b'fake lab')]:
   v='1.24.0-sandbox-rc2' if ch=='lab' else '1.24.0-rc2';name='releases/luma-drift-'+v+'.apk';h=write('public/'+name,b)
   setattr(m,'PROD' if ch=='production' else 'LAB',h);setattr(m,'PROD_BYTES' if ch=='production' else 'LAB_BYTES',len(b))
   release['apks'][ch]={'path':name,'sha256':h,'bytes':len(b),'version':v,'code':33,'signer_certificate_sha256':m.SIGNER};public['/'+name]={'sha256':h,'bytes':len(b)};apks[ch]={'path':'public/'+name,'sha256':h,'bytes':len(b),'reconstructed':True}
  manifest='releases/manifest-1.24.0-rc2.json';manifestsha=write(manifest,release)
  source={'status':'PASS','edition':'1.24','release':'1.24.0-rc2','native_capture_provenance_verified':True,'package_identity_verified':True,'production_apk_sha256':m.PROD,'lab_apk_sha256':m.LAB,'artifact_source_commit':m.C1,'previous_source_commit':m.PREVIOUS,'previous_deployment_source_commit':m.PREVIOUS_DEPLOY,'baseline_public_tree':m.BASELINE_PUBLIC_TREE,'frozen_inputs':frozen,'firebase_config_sha256':configsha,'artifact_manifest':manifest,'artifact_manifest_sha256':manifestsha,'files':public};m.SOURCE_SHA=write(m.SOURCE,source)
  op={'status':'STAGED_ONLY','staging_passed':True,'source_commit':m.C2,'repository':m.REPO,'script_sha256':m.OPERATOR_SHA,'artifact_source_commit':m.C1,'previous_source_commit':m.PREVIOUS,'expected_live':m.LIVE,'production_apk_sha256':m.PROD,'lab_apk_sha256':m.LAB,'workspace':str(w),'checkout':str(c),'receipts':str(r),'staging':str(r/'staging.json'),'deployment':str(r/'deployment/deployment.json')}
  stage={'passed':True,'sourceCommit':m.C2,'sourceManifestSha256':m.SOURCE_SHA,'previousSourceCommit':m.PREVIOUS,'previousDeploymentSourceCommit':m.PREVIOUS_DEPLOY,'baselinePhysicalPaths':576,'baselinePublicTree':m.BASELINE_PUBLIC_TREE,'retiredPaths':[],'downloadBasesRequested':True,'downloadedBaseChannels':['production','lab'],'authenticated':False,'uploaded':False,'deployed':False,'apks':apks}
  m.OPERATOR_RECEIPT_SHA=save(r/'operator.json',op);m.STAGING_SHA=save(r/'staging.json',stage)
  calls=[];fault={'auth':False,'dirty':False,'map':False}
  def git(cmd,**kw):
   calls.append((cmd,None));a=cmd[5:]
   if a==['rev-parse','HEAD']:return (m.C2+'\n').encode()
   if a==['rev-parse','HEAD^{tree}']:return (m.C2_TREE+'\n').encode()
   if a==['rev-parse','HEAD:public']:return (m.PUBLIC_TREE+'\n').encode()
   if a==['rev-parse','HEAD:app']:return (m.APP_TREE+'\n').encode()
   if a[:1]==['status']:return b' M dirty\n' if fault['dirty'] else b''
   if a==['remote','get-url','origin']:return (m.REPO+'\n').encode()
   if a[:1]==['show']:return files[a[1].split(':',1)[1]]
   raise AssertionError('Unexpected process '+repr(cmd))
  def run(cmd,**kw):
   calls.append((cmd,kw.get('input')))
   if cmd[:3]==['gcloud','auth','print-access-token']:return types.SimpleNamespace(returncode=1 if fault['auth'] else 0,stdout=TOKEN)
   assert cmd[1]=='scripts/deploy-site-v124.py';assert kw['input']==TOKEN+b'\n';assert TOKEN.decode() not in ' '.join(cmd)
   out=Path(cmd[cmd.index('--output')+1]);out.mkdir();(out/'deployment.json').write_text(json.dumps({'status':'DEPLOY/FINALIZED','sourceCommit':m.C2,'version':'sites/luma-drift-production/versions/offline-fixture','finalExpectedProviderPaths':630 if fault['map'] else 631,'providerServingConfigUnchanged':True}));kw['stdout'].write(b'offline fake deploy only\n');return types.SimpleNamespace(returncode=0)
  m.subprocess=types.SimpleNamespace(check_output=git,run=run,DEVNULL=-3,PIPE=-1,STDOUT=-2);m.shutil=types.SimpleNamespace(which=lambda _:'/offline/gcloud');m.sys=types.SimpleNamespace(platform='linux',executable=sys.executable)
  return m,w,calls,fault
 def call(self,m,w,mode):
  out=io.StringIO()
  with patch.object(sys,'argv',[str(SCRIPT),str(w),m.C2,mode]),contextlib.redirect_stdout(out):rc=m.main()
  self.assertNotIn(TOKEN.decode(),out.getvalue())
  for p in w.rglob('*'):
   if p.is_file():self.assertNotIn(TOKEN,p.read_bytes())
  return rc,out.getvalue()
 def test_real_pins_match_frozen_release(self):
  m=module();r=json.loads(Path('/Users/n/androidhit/releases/manifest-1.24.0-rc2.json').read_text());self.assertEqual((m.PROD,m.LAB,m.PROD_BYTES,m.LAB_BYTES),(r['apks']['production']['sha256'],r['apks']['lab']['sha256'],r['apks']['production']['bytes'],r['apks']['lab']['bytes']));self.assertEqual(r['version_code'],33);self.assertEqual(m.C2,'66119799d6fbe5254a8d37d50db76afe8daffbdc');self.assertEqual(m.LIVE,'sites/luma-drift-production/versions/cf8b817321bf33d5')
 def test_check_only_no_auth_writes_clone_fetch_or_stage(self):
  m,w,c,f=self.fixture();before={str(p):p.read_bytes() for p in w.rglob('*') if p.is_file()};rc,out=self.call(m,w,'--check-only');self.assertEqual(rc,0);self.assertIn('VALIDATED_ONLY',out);self.assertEqual(before,{str(p):p.read_bytes() for p in w.rglob('*') if p.is_file()});self.assertTrue(all(x[0]=='git' for x,_ in c));self.assertFalse(any('fetch' in x or 'clone' in x for x,_ in c))
 def test_deploy_only_stdin_new_receipts_preserves_stage(self):
  m,w,c,f=self.fixture();before={p:p.read_bytes() for p in (w/'receipts').glob('*.json')};rc,_=self.call(m,w,'--deploy');self.assertEqual(rc,0);self.assertTrue(all(p.read_bytes()==b for p,b in before.items()));self.assertEqual(json.loads((w/'receipts'/(PREFIX+'operator.json')).read_text())['status'],'DEPLOY/FINALIZED');self.assertEqual(sum(x[0]=='gcloud' for x,_ in c),1);self.assertEqual(sum(len(x)>1 and x[1]=='scripts/deploy-site-v124.py' for x,_ in c),1)
 def test_auth_failure_no_deployer_or_token_leak(self):
  m,w,c,f=self.fixture();f['auth']=True;rc,_=self.call(m,w,'--deploy');self.assertEqual(rc,1);self.assertFalse(any(len(x)>1 and x[1]=='scripts/deploy-site-v124.py' for x,_ in c));self.assertEqual(json.loads((w/'receipts'/(PREFIX+'operator.json')).read_text())['status'],'FAIL')
 def test_existing_final2_refused_before_auth(self):
  m,w,c,f=self.fixture();(w/'receipts'/(PREFIX+'operator.json')).write_text('{}')
  with self.assertRaises(RuntimeError):self.call(m,w,'--deploy')
  self.assertEqual(c,[])
 def test_operator_tamper_refused_before_auth(self):
  m,w,c,f=self.fixture();(w/'receipts/operator.json').write_text('{}')
  with self.assertRaises(RuntimeError):self.call(m,w,'--deploy')
  self.assertEqual(c,[])
 def test_dirty_checkout_refused_before_auth(self):
  m,w,c,f=self.fixture();f['dirty']=True
  with self.assertRaises(RuntimeError):self.call(m,w,'--deploy')
  self.assertTrue(all(x[0]=='git' for x,_ in c))
 def test_wrong_target_refused_before_auth(self):
  m,w,c,f=self.fixture()
  with self.assertRaises(RuntimeError):m.validate(str(w),'39c0985b24ecc4cf6ef00b6eb1c1392ebe3758da',m.environment())
  self.assertEqual(c,[])
 def test_apk_corruption_refused_before_auth(self):
  m,w,c,f=self.fixture();(w/'source/public/releases/luma-drift-1.24.0-rc2.apk').write_bytes(b'bad')
  with self.assertRaises(RuntimeError):self.call(m,w,'--deploy')
  self.assertTrue(all(x[0]=='git' for x,_ in c))
 def test_wrong_management_map_not_reported_pass(self):
  m,w,c,f=self.fixture();f['map']=True;rc,_=self.call(m,w,'--deploy');self.assertEqual(rc,1);self.assertEqual(json.loads((w/'receipts'/(PREFIX+'operator.json')).read_text())['status'],'FAIL')
if __name__=='__main__':unittest.main()
