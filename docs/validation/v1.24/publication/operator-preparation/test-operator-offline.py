from pathlib import Path
import importlib.util,types,tempfile,json,sys,contextlib,io,unittest,hashlib
from unittest.mock import patch
SCRIPT=Path('/tmp/luma124-cloud-run.py');C2='c'*40;C1='787fba142b8edd1f840b9b70cb7749167edacd24';TOKEN=b'offline-dummy-token-no-credential-0123456789'
def module():
 spec=importlib.util.spec_from_file_location('cloud_operator124',SCRIPT);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m);return m
class OfflineOperator(unittest.TestCase):
 def exercise(self,mode,fault=None):
  m=module();work=Path(tempfile.mkdtemp(prefix='luma124-operator-mock-'));calls=[];output=io.StringIO();source={'status':'PASS','edition':'1.24','release':'1.24.0-rc2','native_capture_provenance_verified':True,'package_identity_verified':True,'production_apk_sha256':m.PROD,'lab_apk_sha256':m.LAB,'artifact_source_commit':C1}
  def run(cmd,**kw):
   calls.append((list(cmd),kw.get('input')))
   if 'clone' in cmd:
    checkout=work/'source';(checkout/'.git/info').mkdir(parents=True);p=checkout/m.SOURCE;p.parent.mkdir(parents=True);p.write_text(json.dumps(source))
   elif len(cmd)>1 and cmd[1]=='scripts/stage-site-v124-rest.py':
    p=Path(cmd[cmd.index('--report')+1]);p.write_text(json.dumps({'passed':True,'sourceCommit':C2,'apks':{'production':{'sha256':m.PROD},'lab':{'sha256':m.LAB}}}))
    if fault=='stage':return types.SimpleNamespace(returncode=1)
   elif cmd[:3]==['gcloud','auth','print-access-token']:
    return types.SimpleNamespace(returncode=1 if fault=='auth' else 0,stdout=TOKEN)
   elif len(cmd)>1 and cmd[1]=='scripts/deploy-site-v124.py':
    assert kw['input']==TOKEN+b'\n';assert TOKEN.decode() not in ' '.join(cmd)
    p=Path(cmd[cmd.index('--output')+1]);p.mkdir();(p/'deployment.json').write_text(json.dumps({'status':'DEPLOY/FINALIZED','sourceCommit':C2,'version':'sites/luma-drift-production/versions/offlinefixture'}))
   if hasattr(kw.get('stdout'),'write'):kw['stdout'].write(b'offline fixture only\n')
   return types.SimpleNamespace(returncode=0)
  def check(cmd,**kw):
   parts=cmd[5:]
   if parts[:1]==['status']:return b' M dirty\n' if fault=='dirty' else b''
   if parts[:1]==['rev-parse']:return ((C2 if parts[1]=='HEAD' else parts[1].removesuffix('^{commit}'))+'\n').encode()
   raise AssertionError('Unexpected process '+repr(cmd))
  m.subprocess=types.SimpleNamespace(run=run,check_output=check,DEVNULL=-3,PIPE=-1,STDOUT=-2)
  m.tempfile=types.SimpleNamespace(mkdtemp=lambda **kw:str(work));m.shutil=types.SimpleNamespace(which=lambda name:'/offline/'+name)
  m.sys=types.SimpleNamespace(argv=[str(SCRIPT),C2,mode],platform='linux',executable=sys.executable,stderr=output)
  with contextlib.redirect_stdout(output), patch.object(sys, 'argv', [str(SCRIPT), C2, mode]):rc=m.main()
  receipts=work/'receipts';texts='\n'.join(p.read_text(errors='replace') for p in receipts.rglob('*') if p.is_file())
  self.assertNotIn(TOKEN.decode(),texts+output.getvalue())
  return m,rc,calls,json.loads((receipts/'operator.json').read_text())
 def test_constants_match_exact_D_and_baselines(self):
  m=module();r=json.loads(Path('/Users/n/androidhit/releases/manifest-1.24.0-rc2.json').read_text())
  self.assertEqual((m.PROD,m.LAB),(r['apks']['production']['sha256'],r['apks']['lab']['sha256']));self.assertEqual(m.LIVE,'sites/luma-drift-production/versions/cf8b817321bf33d5');self.assertEqual(m.PREVIOUS_DEPLOY,'c99ac52903eca6707d3f4d644d7c480c79b63e68');self.assertEqual(m.PREVIOUS,'5cc870701c8039e664d1c00a90d0586c3d00521c');self.assertEqual(r['version_code'],33)
 def test_plan_is_explicitly_offline(self):
  m=module();p=m.plan(C2);self.assertFalse(p['network_executed']);self.assertEqual(tuple(p['sparse_initial_files']),('.gitignore','scripts/stage-site-v124-rest.py',m.SOURCE))
 def test_stage_only_no_auth_or_deploy(self):
  m,rc,calls,r=self.exercise('--stage-only');self.assertEqual(rc,0);self.assertEqual(r['status'],'STAGED_ONLY');self.assertFalse(any(c[0][0]=='gcloud' or 'scripts/deploy-site-v124.py' in c[0] for c in calls))
 def test_deploy_order_exact_refs_token_stdin_and_no_token_logs(self):
  m,rc,calls,r=self.exercise('--deploy');self.assertEqual(rc,0);self.assertEqual(r['status'],'DEPLOY/FINALIZED')
  fetched=[c[-1] for c,_ in calls if 'fetch' in c];self.assertEqual(fetched,[C2,m.PREVIOUS,m.PREVIOUS_DEPLOY,C1]);stage=next(i for i,(c,_) in enumerate(calls) if 'scripts/stage-site-v124-rest.py' in c);auth=next(i for i,(c,_) in enumerate(calls) if c[0]=='gcloud');deploy=next(i for i,(c,_) in enumerate(calls) if 'scripts/deploy-site-v124.py' in c);self.assertLess(stage,auth);self.assertLess(auth,deploy);cmd,data=calls[deploy];self.assertIn('--deploy',cmd);self.assertIn('--token-stdin',cmd);self.assertEqual(cmd[cmd.index('--expected-live')+1],m.LIVE)
 def test_stage_failure_stops_before_auth(self):
  _,rc,calls,r=self.exercise('--deploy','stage');self.assertEqual(rc,1);self.assertEqual(r['status'],'FAIL');self.assertFalse(any(c[0]=='gcloud' for c,_ in calls))
 def test_dirty_checkout_stops_before_stage_auth(self):
  _,rc,calls,r=self.exercise('--deploy','dirty');self.assertEqual(rc,1);self.assertFalse(any('scripts/stage-site-v124-rest.py' in c for c,_ in calls))
 def test_auth_failure_stops_before_deployment_no_token_logs(self):
  _,rc,calls,r=self.exercise('--deploy','auth');self.assertEqual(rc,1);self.assertFalse(any('scripts/deploy-site-v124.py' in c for c,_ in calls))
if __name__=='__main__':unittest.main()
