#!/usr/bin/env python3
"""Offline state-machine fixtures; no credential acquisition, network or real APKs."""
import contextlib
import copy
import hashlib
import io
import json
import re
from pathlib import Path
import subprocess
import tempfile
import types
import unittest
from unittest.mock import patch
import urllib.request

SCRIPTS=Path(__file__).resolve().parent
def module(name,path):
    value=types.ModuleType(name);value.__file__=str(path);exec(compile(path.read_bytes(),str(path),'exec'),value.__dict__);return value
DEPLOY=module('deploy123',SCRIPTS/'deploy-site-v123.py')
PRIOR=module('historical_test_fixtures_unchanged',SCRIPTS/'test-deploy-cognitive-overlay.py')
HELPER=PRIOR.MODULE

class Rest123Tests(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory();self.root=Path(self.temp.name)/'checkout';self.root.mkdir();self.output=Path(self.temp.name)/'receipts';self.output.mkdir()
        self.previous_root,self.previous_run=DEPLOY.ROOT,DEPLOY.run;DEPLOY.ROOT=self.root;DEPLOY.run=lambda *args:'offline-public-tree'
        self.guard=patch.object(urllib.request.OpenerDirector,'open',side_effect=AssertionError('Network prohibited'));self.guard.start()
        before={'/index.html':{'sha256':'a'*64,'bytes':4},'/lab/cognitive/index.html':{'sha256':'b'*64,'bytes':7},'/releases/luma-drift-1.22.0-rc1.apk':{'sha256':'c'*64,'bytes':300_000_000},'/releases/luma-drift-1.22.0-sandbox-rc1.apk':{'sha256':'d'*64,'bytes':300_000_000}}
        before.update({name:{'sha256':'9'*64,'bytes':1234} for name in DEPLOY.RETIRED_PATHS})
        before.update({'/historical/large-%03d.apk'%n:{'sha256':'e'*64,'bytes':300_000_000} for n in range(570-len(before))})
        self.previous={'status':'PASS','edition':'1.22','release':'1.22.0-rc1','files':before,'firebase_config_sha256':'f'*64}
        files=copy.deepcopy(before);self.overlay={}
        for name in DEPLOY.RETIRED_PATHS:files.pop(name)
        for name in sorted(DEPLOY.REQUIRED_ADDITIONS|{'/index.html','/lab/clarity-123/results.json'}):
            raw=('fictional offline bytes '+name).encode();target=self.root/('public'+name);target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(raw)
            files[name]={'sha256':HELPER.sha(raw),'bytes':len(raw)};self.overlay[name]={'gzipSha256':HELPER.sha(HELPER.compressed(raw))}
        self.source={'retired_files':{name:before[name] for name in DEPLOY.RETIRED_PATHS},'status':'PASS','edition':'1.23','release':'1.23.0-rc2','files':files,'firebase_config_sha256':'f'*64,'native_capture_provenance_verified':True,'package_identity_verified':True}
        self.client_source={'baselineFiles':before,'overlay':self.overlay}
        self.reader=patch.object(DEPLOY,'committed_source_bytes',side_effect=lambda commit:json.dumps(self.previous).encode() if commit==DEPLOY.PREVIOUS122 else self.fail('Unpinned baseline read'));self.reader.start()
    def tearDown(self):
        self.reader.stop();self.guard.stop();DEPLOY.ROOT,DEPLOY.run=self.previous_root,self.previous_run;self.temp.cleanup()
    def client(self,fault=None):
        class FilteredClient(PRIOR.FakeClient):
            def api(inner,method,resource,body=None,**query):
                if resource.endswith('/versions:clone'):
                    expected={'sourceVersion':inner.old,'finalize':False,'exclude':{'regexes':['^'+re.escape(name)+'$' for name in sorted(DEPLOY.RETIRED_PATHS)]}}
                    if body!=expected:raise AssertionError('Clone exclusion must contain exact anchored retirement paths')
                    result=super().api(method,resource,{'sourceVersion':inner.old,'finalize':False},**query)
                    inner.calls[-1]=(method,resource,copy.deepcopy(body),query)
                    if inner.fault!='exclude_ignored':
                        for name in DEPLOY.RETIRED_PATHS:inner.maps[inner.new].pop(name)
                    if inner.fault=='exclude_extra':inner.maps[inner.new].pop('/historical/large-001.apk')
                    return result
                return super().api(method,resource,body,**query)
        client=FilteredClient(self.client_source,fault);old=client.old;client.old=DEPLOY.PREVIOUS122_LIVE;client.maps[client.old]=client.maps.pop(old);client.states[client.old]=client.states.pop(old);client.live=client.old;client.message='Website1.22 from '+DEPLOY.PREVIOUS122;return client
    def execute(self,deploy=True,fault=None):
        client=self.client(fault)
        with contextlib.redirect_stdout(io.StringIO()):
            result=DEPLOY.rest_deploy(self.source,'c'*40,DEPLOY.PREVIOUS122_LIVE,self.output,deploy,'offline-fixture-not-a-credential',client=client,helper=HELPER)
        return result,client
    def test_production_constants_are_exact_accepted_c3(self):
        self.assertEqual(DEPLOY.PREVIOUS122,'b122c0acbd172a7912c69b0b6914b8b0f9b2a243');self.assertEqual(DEPLOY.PREVIOUS122_LIVE,'sites/luma-drift-production/versions/24e34cf6e6af1ed8')
    def test_readonly_uses_only_management_gets(self):
        result,client=self.execute(False);self.assertEqual(result['historicalProviderPaths'],572);self.assertTrue(all(row[0]=='GET' for row in client.calls));self.assertFalse(client.uploads)
    def test_filtered_clone_preserves_all_557_untouched_provider_hashes_and_config(self):
        result,client=self.execute();self.assertEqual(result['status'],'DEPLOY/FINALIZED');self.assertEqual(result['preservedHistoricalPaths'],557);self.assertEqual(result['intentionalChanges'],['/index.html']);self.assertEqual(result['finalExpectedProviderPaths'],572-14+len(result['addedPaths']));self.assertTrue(result['providerServingConfigUnchanged'])
        for name,row in client.maps[client.old].items():
            if name in DEPLOY.RETIRED_PATHS:self.assertNotIn(name,client.maps[client.new])
            elif name!='/index.html':self.assertEqual(client.maps[client.new][name],row)
        self.assertEqual(client.message,'Website1.23 from '+'c'*40);self.assertFalse(result['historicalApksUploaded']);self.assertFalse(result['publicOriginBytesVerified']);self.assertFalse((self.root/'public/historical').exists())
    def test_existing_payload_deletion_overwrite_unknown_addition_or_config_refuses_before_provider(self):
        mutations=[lambda s:s['files'].pop('/historical/large-000.apk'),lambda s:s['files']['/lab/cognitive/index.html'].update(sha256='0'*64),lambda s:s['files'].update({'/unrelated.json':{'sha256':'0'*64,'bytes':1}}),lambda s:s.update(firebase_config_sha256='0'*64),lambda s:s['retired_files'].pop(next(iter(DEPLOY.RETIRED_PATHS))),lambda s:s.update(release='1.23.0-rc1')]
        for mutation in mutations:
            with self.subTest(mutation=mutation):
                source=copy.deepcopy(self.source);mutation(source);client=self.client()
                with self.assertRaises(AssertionError):DEPLOY.rest_deploy(source,'c'*40,DEPLOY.PREVIOUS122_LIVE,self.output,True,'offline-fixture-not-a-credential',client=client,helper=HELPER)
                self.assertFalse(client.calls)
    def test_missing_each_required_new_payload_is_rejected(self):
        for name in DEPLOY.REQUIRED_ADDITIONS:
            source=copy.deepcopy(self.source);source['files'].pop(name)
            with self.subTest(name=name),self.assertRaises(AssertionError):DEPLOY.deployment_baseline(source)
    def test_unsafe_new_lab_path_is_rejected_before_provider(self):
        for name in ('/lab/clarity-123/../escape.json','/lab/clarity-123//bad.json','//other.invalid/x.json'):
            source=copy.deepcopy(self.source);source['files'][name]={'sha256':'a'*64,'bytes':1}
            with self.subTest(name=name),self.assertRaises(AssertionError):DEPLOY.deployment_baseline(source)
    def test_unpinned_previous_commit_live_or_message_refuses(self):
        with self.assertRaises(AssertionError):DEPLOY.deployment_baseline(self.source,'f'*40)
        client=self.client()
        with self.assertRaises(AssertionError):DEPLOY.rest_deploy(self.source,'c'*40,'sites/luma-drift-production/versions/other',self.output,True,'offline-fixture-not-a-credential',client=client,helper=HELPER)
        self.assertFalse(client.calls);client.message='Other source'
        with self.assertRaises(AssertionError):DEPLOY.rest_deploy(self.source,'c'*40,DEPLOY.PREVIOUS122_LIVE,self.output,True,'offline-fixture-not-a-credential',client=client,helper=HELPER)
        self.assertTrue(all(row[0]=='GET' for row in client.calls))
    def test_clone_upload_or_race_fault_never_releases(self):
        for fault in ('unknown_reserved_extra','clone_file_changed','clone_config_changed','untrusted_upload','unexpected_hash','race_before_clone','race_before_release','exclude_ignored','exclude_extra'):
            with self.subTest(fault=fault):
                client=self.client(fault)
                with self.assertRaises((AssertionError,HELPER.DeploymentError)),contextlib.redirect_stdout(io.StringIO()):DEPLOY.rest_deploy(self.source,'c'*40,DEPLOY.PREVIOUS122_LIVE,self.output,True,'offline-fixture-not-a-credential',client=client,helper=HELPER)
                self.assertFalse([row for row in client.calls if row[1].endswith('/releases')])
    def test_release_alias_accepted_but_other_release_identity_refuses(self):
        self.assertEqual(self.execute(fault='release_live_alias')[0]['status'],'DEPLOY/FINALIZED')
        with self.assertRaises(AssertionError):self.execute(fault='release_other_id')
    def test_upload_timeout_extends_only_pinned_post(self):
        class Opener:
            def __init__(self):self.calls=[]
            def open(self,request,timeout):self.calls.append((request.full_url,timeout));return io.BytesIO(b'{}')
        original=Opener();client=HELPER.Client('offline-fixture-not-a-credential');client._opener=original;DEPLOY.configure_upload_timeout(client,HELPER)
        client.request('GET',HELPER.API+'sites/luma-drift-production/channels/live');client.request('POST',HELPER.API+'sites/luma-drift-production/versions',{})
        client.request('POST','https://'+HELPER.UPLOAD_HOST+'/upload/sites/luma-drift-production/versions/fake/files/'+'a'*64,b'bytes',upload=True)
        self.assertEqual([row[1] for row in original.calls],[60,60,600])
        with self.assertRaises(HELPER.DeploymentError):client._opener.open(urllib.request.Request('https://evil.invalid/',method='POST'),timeout=60)
    def test_token_never_goes_to_public_origin_or_redirect(self):
        for url in ('https://luma-drift-production.web.app/index.html','http://firebasehosting.googleapis.com/v1beta1/sites/x','https://firebasehosting.googleapis.com.evil.invalid/v1beta1/sites/x'):
            with self.assertRaises(HELPER.DeploymentError):HELPER.Client.check_url(url)
        with self.assertRaises(HELPER.DeploymentError):HELPER.NoRedirect().redirect_request(None,None,302,'redirect',{},'https://evil.invalid')
    def test_overlay_bytes_are_verified_before_provider_mutation(self):
        (self.root/'public/assets/site-v123.js').write_bytes(b'changed');client=self.client()
        with self.assertRaises(AssertionError):DEPLOY.rest_deploy(self.source,'c'*40,DEPLOY.PREVIOUS122_LIVE,self.output,True,'offline-fixture-not-a-credential',client=client,helper=HELPER)
        self.assertTrue(all(row[0]=='GET' for row in client.calls))

class ArtifactGate123Tests(unittest.TestCase):
    """Tiny fake APK bytes exercise metadata gates, not Android signing acceptance."""
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory();self.root=Path(self.temp.name);self.previous_root=DEPLOY.ROOT;DEPLOY.ROOT=self.root
        self.git('init','-q');self.git('config','user.name','Offline fixture');self.git('config','user.email','fixture@example.invalid')
        (self.root/'.gitignore').write_text('public/releases/*.apk\n')
        self.release={'edition':'1.23','version':'1.23.0-rc2','version_code':31,'apks':{}}
        self.source={'artifact_manifest':'releases/manifest-1.23.0-rc2.json','files':{}}
        for channel in ('production','lab'):
            version='1.23.0-sandbox-rc2' if channel=='lab' else '1.23.0-rc2';name='releases/luma-drift-'+version+'.apk';raw=('fictional unsigned '+channel).encode();path=self.root/'public'/name;path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(raw)
            digest=hashlib.sha256(raw).hexdigest();self.release['apks'][channel]={'path':name,'sha256':digest,'bytes':len(raw),'version':version,'code':31,'signer_certificate_sha256':DEPLOY.SIGNER}
            self.source[channel+'_apk_sha256']=digest;self.source['files']['/'+name]={'sha256':digest,'bytes':len(raw)}
        self.commit_release()
    def tearDown(self):DEPLOY.ROOT=self.previous_root;self.temp.cleanup()
    def git(self,*args):return subprocess.check_output(['git',*args],cwd=self.root,stderr=subprocess.DEVNULL,text=True).strip()
    def commit_release(self):
        path=self.root/self.source['artifact_manifest'];path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(self.release))
        self.source['artifact_manifest_sha256']=hashlib.sha256(path.read_bytes()).hexdigest();self.git('add','.');self.git('commit','-qm','fake metadata fixture');self.source['artifact_source_commit']=self.git('rev-parse','HEAD')
    def validate(self):return DEPLOY.validate_artifact_manifest(self.source,self.source['production_apk_sha256'])
    def test_both_physical_apks_and_code31_metadata_are_required(self):self.assertEqual(self.validate(),self.source['artifact_source_commit'])
    def test_old_channel_code_is_refused(self):
        self.release['apks']['lab']['code']=30;self.commit_release()
        with self.assertRaises(AssertionError):self.validate()
    def test_old_release_code_or_wrong_signer_is_refused(self):
        self.release['version_code']=30;self.commit_release()
        with self.assertRaises(AssertionError):self.validate()
        self.release['version_code']=31;self.release['version']='1.23.0-rc1';self.commit_release()
        with self.assertRaises(AssertionError):self.validate()
        self.release['version']='1.23.0-rc2';self.release['apks']['lab']['version']='1.23.0-sandbox-rc1';self.commit_release()
        with self.assertRaises(AssertionError):self.validate()
        self.release['apks']['lab']['version']='1.23.0-sandbox-rc2';self.release['apks']['production']['signer_certificate_sha256']='0'*64;self.commit_release()
        with self.assertRaises(AssertionError):self.validate()
    def test_missing_physical_apk_has_no_fallback(self):
        (self.root/'public'/self.release['apks']['lab']['path']).unlink()
        with self.assertRaises(AssertionError):self.validate()
    def test_physical_byte_mismatch_is_refused(self):
        (self.root/'public'/self.release['apks']['production']['path']).write_bytes(b'changed')
        with self.assertRaises(AssertionError):self.validate()
    def test_apks_cannot_be_tracked_git_blobs(self):
        self.git('add','-f','public/'+self.release['apks']['production']['path'])
        with self.assertRaises(AssertionError):self.validate()

if __name__=='__main__':unittest.main()
