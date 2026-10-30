#!/usr/bin/env python3
"""Offline API-state tests for1.22 clone/overlay deployment. No token acquisition, network or real APKs."""
import contextlib
import copy
import importlib.util
import io
import json
from pathlib import Path
import tempfile
import types
import unittest
from unittest.mock import patch
import urllib.request

def module(name,path):
    result=types.ModuleType(name);result.__file__=str(path);exec(compile(path.read_text(),str(path),'exec'),result.__dict__);return result

SCRIPTS=Path(__file__).resolve().parent
DEPLOY=module('deploy122',SCRIPTS/'deploy-site-v122.py')
PREVIOUS=module('prior_offline_firebase_tests',SCRIPTS/'test-deploy-cognitive-overlay.py')
HELPER=PREVIOUS.MODULE

class Site122RestTests(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory();self.root=Path(self.temp.name)/'source';self.root.mkdir();self.output=Path(self.temp.name)/'receipts';self.output.mkdir();self.previous_root=DEPLOY.ROOT;self.previous_run=DEPLOY.run;DEPLOY.ROOT=self.root;DEPLOY.run=lambda *args:'fixture-public-tree'
        before={name:{'sha256':'a'*64,'bytes':12} for name in ['/index.html','/lab/cognitive/index.html','/lab/cognitive/lab.js','/lab/cognitive/lab.css']}
        before.update({'/historical/large-%03d.apk'%i:{'sha256':'b'*64,'bytes':300_000_000} for i in range(535)})
        current=copy.deepcopy(before);overlay={}
        for name in ['/index.html','/lab/cognitive/index.html','/lab/cognitive/lab.js','/lab/cognitive/lab.css','/lab/cognitive/data-v122/summary.json','/lab/ui-1-22/index.html','/assets/site-v122.js','/releases/luma-drift-1.22.0-rc1.apk']:
            raw=('small offline fixture for '+name).encode();path=self.root/('public'+name);path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(raw);current[name]={'sha256':HELPER.sha(raw),'bytes':len(raw)};encoded=HELPER.compressed(raw);overlay[name]={'gzipSha256':HELPER.sha(encoded)}
        baseline=self.root/'docs/validation/cognitive-lab/site-manifest.json';baseline.parent.mkdir(parents=True);baseline.write_text(json.dumps({'files':before}))
        self.source={'files':current};self.client_source={'baselineFiles':before,'overlay':overlay};self.old=HELPER.EXPECTED_LIVE
    def tearDown(self):
        DEPLOY.ROOT=self.previous_root;DEPLOY.run=self.previous_run;self.temp.cleanup()
    def execute(self,deploy=True,fault=None):
        client=PREVIOUS.FakeClient(self.client_source,fault)
        with contextlib.redirect_stdout(io.StringIO()):result=DEPLOY.rest_deploy(self.source,'c'*40,self.old,self.output,deploy,'offline-fixture-not-a-credential',client=client,helper=HELPER)
        return result,client
    def test_read_only_preflight_preserves541_without_any_old_apk_on_disk(self):
        result,client=self.execute(False);self.assertEqual(result['historicalProviderPaths'],541);self.assertEqual(result['historicalPhysicalPaths'],539);self.assertFalse((self.root/'public/historical').exists());self.assertTrue(all(call[0]=='GET' for call in client.calls));self.assertFalse(client.uploads);self.assertFalse(result['publicOriginRequested'])
    def test_full_overlay_preserves_every_untouched_provider_hash_and_config(self):
        result,client=self.execute();self.assertEqual(result['status'],'DEPLOY/FINALIZED');self.assertEqual(result['finalExpectedProviderPaths'],545);self.assertEqual(result['preservedHistoricalPaths'],537);self.assertTrue(result['providerServingConfigUnchanged']);self.assertFalse(result['historicalApksUploaded']);self.assertFalse(result['publicOriginRequested']);self.assertFalse(result['publicOriginBytesVerified']);self.assertFalse(result['publicOriginRenderingVerified'])
        for path,row in client.maps[client.old].items():
            if path not in result['intentionalChanges']:self.assertEqual(client.maps[client.new][path],row)
        self.assertEqual(client.message,'Website1.22 from '+'c'*40);self.assertEqual(len([call for call in client.calls if call[1].endswith('/releases')]),1);self.assertEqual(len(client.uploads),7)
    def test_unapproved_historical_change_is_refused_before_any_provider_mutation(self):
        self.source['files']['/historical/large-000.apk']={'sha256':'e'*64,'bytes':1}
        client=PREVIOUS.FakeClient(self.client_source)
        with self.assertRaises(AssertionError),contextlib.redirect_stdout(io.StringIO()):DEPLOY.rest_deploy(self.source,'c'*40,self.old,self.output,True,'offline-fixture-not-a-credential',client=client,helper=HELPER)
        self.assertFalse(client.calls)
    def test_clone_hash_or_config_damage_never_releases(self):
        for fault in ['unknown_reserved_extra','clone_file_changed','clone_config_changed','untrusted_upload','unexpected_hash','race_before_clone','race_before_release','release_other_id']:
            with self.subTest(fault=fault):
                client=PREVIOUS.FakeClient(self.client_source,fault)
                with self.assertRaises((AssertionError,HELPER.DeploymentError)),contextlib.redirect_stdout(io.StringIO()):DEPLOY.rest_deploy(self.source,'c'*40,self.old,self.output,True,'offline-fixture-not-a-credential',client=client,helper=HELPER)
                released=[call for call in client.calls if call[1].endswith('/releases')]
                self.assertEqual(len(released),1 if fault=='release_other_id' else 0)
    def test_exact_live_release_alias_is_accepted(self):
        result,_=self.execute(fault='release_live_alias');self.assertEqual(result['status'],'DEPLOY/FINALIZED')
    def test_client_can_never_send_credentials_to_public_origin_or_redirect(self):
        for url in ['https://luma-drift-production.web.app/index.html','https://firebasehosting.googleapis.com.evil.invalid/v1beta1/sites/x','http://firebasehosting.googleapis.com/v1beta1/sites/x']:
            with self.assertRaises(HELPER.DeploymentError):HELPER.Client.check_url(url)
        with self.assertRaises(HELPER.DeploymentError):HELPER.NoRedirect().redirect_request(None,None,302,'redirect',{},'https://example.invalid')

    def test_upload_timeout_hook_extends_only_pinned_post_uploads(self):
        class Opener:
            def __init__(self):self.calls=[]
            def open(self,request,timeout):self.calls.append((request.full_url,timeout));return io.BytesIO(b'{}')
        original=Opener();client=HELPER.Client('offline-fixture-not-a-credential');client._opener=original
        DEPLOY.configure_upload_timeout(client,HELPER)
        client.request('GET',HELPER.API+'sites/luma-drift-production/channels/live')
        client.request('POST',HELPER.API+'sites/luma-drift-production/versions',{})
        client.request('POST','https://'+HELPER.UPLOAD_HOST+'/upload/sites/luma-drift-production/versions/fixture/files/'+'a'*64,b'bytes',upload=True)
        self.assertEqual([row[1] for row in original.calls],[60,60,600])
        with self.assertRaises(HELPER.DeploymentError):client._opener.open(urllib.request.Request('https://example.invalid/',method='POST'),timeout=60)
        self.assertEqual(len(original.calls),3)


class Incremental122Tests(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory();self.root=Path(self.temp.name)/'source';self.root.mkdir();self.output=Path(self.temp.name)/'receipts';self.output.mkdir();self.previous_root=DEPLOY.ROOT;self.previous_run=DEPLOY.run;DEPLOY.ROOT=self.root;DEPLOY.run=lambda *args:'fixture-public-tree'
        names=set(DEPLOY.INCREMENTAL_ALLOWED)|{'/index.html','/releases/luma-drift-1.22.0-rc1.apk','/releases/luma-drift-1.22.0-sandbox-rc1.apk'}
        before={name:{'sha256':'a'*64,'bytes':12} for name in names}
        before.update({'/historical/large-%03d.apk'%i:{'sha256':'b'*64,'bytes':300_000_000} for i in range(570-len(before))})
        common={'status':'PASS','edition':'1.22','release':'1.22.0-rc1','artifact_source_commit':'1'*40,'artifact_manifest':'releases/manifest-1.22.0-rc1.json','artifact_manifest_sha256':'c'*64,'production_apk_sha256':'d'*64,'lab_apk_sha256':'e'*64,'firebase_config_sha256':'f'*64,'screen_keys':['map','keys','tactical','home'],'screenshots':[{'fixture':'same-eight-native-proofs'}],'native_capture_provenance_verified':True,'package_identity_verified':True}
        self.previous=dict(common,files=before);current=copy.deepcopy(before);overlay={}
        for name in DEPLOY.INCREMENTAL_ALLOWED:
            raw=('corrected offline fixture '+name).encode();path=self.root/('public'+name);path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(raw);current[name]={'sha256':HELPER.sha(raw),'bytes':len(raw)};overlay[name]={'gzipSha256':HELPER.sha(HELPER.compressed(raw))}
        self.source=dict(common,files=current);self.client_source={'baselineFiles':before,'overlay':overlay}
        self.reader=patch.object(DEPLOY,'committed_source_bytes',side_effect=lambda commit:json.dumps(self.previous).encode() if commit==DEPLOY.PREVIOUS122 else self.fail('Unknown commit read'));self.reader.start()
    def tearDown(self):
        self.reader.stop();DEPLOY.ROOT=self.previous_root;DEPLOY.run=self.previous_run;self.temp.cleanup()
    def client(self,fault=None):
        result=PREVIOUS.FakeClient(self.client_source,fault);old=result.old;result.old=DEPLOY.PREVIOUS122_LIVE;result.maps[result.old]=result.maps.pop(old);result.states[result.old]=result.states.pop(old);result.live=result.old;result.message='Website1.22 from '+DEPLOY.PREVIOUS122
        return result
    def execute(self,deploy=True,fault=None):
        client=self.client(fault)
        with contextlib.redirect_stdout(io.StringIO()):result=DEPLOY.rest_deploy(self.source,'c'*40,DEPLOY.PREVIOUS122_LIVE,self.output,deploy,'offline-fixture-not-a-credential',client=client,helper=HELPER,previous_source_commit=DEPLOY.PREVIOUS122)
        return result,client
    def test_rest_overlay_preserves572_without_local_apks_cli_physical_gate_is_separate(self):
        result,client=self.execute();self.assertEqual(result['status'],'DEPLOY/FINALIZED');self.assertEqual(result['historicalPhysicalPaths'],570);self.assertEqual(result['historicalProviderPaths'],572);self.assertEqual(result['finalExpectedProviderPaths'],572);self.assertEqual(result['preservedHistoricalPaths'],569);self.assertEqual(result['previousSourceCommit'],DEPLOY.PREVIOUS122);self.assertEqual(set(result['intentionalChanges']),DEPLOY.INCREMENTAL_ALLOWED);self.assertEqual(result['addedPaths'],[]);self.assertEqual(len(client.uploads),2);self.assertFalse((self.root/'public/releases').exists())
        for path,entry in client.maps[client.old].items():
            if path not in DEPLOY.INCREMENTAL_ALLOWED:self.assertEqual(client.maps[client.new][path],entry)
    def test_incremental_readonly_makes_only_management_gets(self):
        result,client=self.execute(False);self.assertEqual(result['historicalProviderPaths'],572);self.assertTrue(all(call[0]=='GET' for call in client.calls));self.assertFalse(client.uploads)
    def test_unknown_changed_added_deleted_or_android_identity_refuses_before_provider_calls(self):
        cases=[lambda value:value['files']['/index.html'].update(sha256='0'*64),lambda value:value['files'].update({'/unknown.json':{'sha256':'0'*64,'bytes':1}}),lambda value:value['files'].pop('/index.html'),lambda value:value.update(production_apk_sha256='0'*64)]
        for mutation in cases:
            with self.subTest(mutation=mutation):
                source=copy.deepcopy(self.source);mutation(source);client=self.client()
                with self.assertRaises(AssertionError):DEPLOY.rest_deploy(source,'c'*40,DEPLOY.PREVIOUS122_LIVE,self.output,True,'offline-fixture-not-a-credential',client=client,helper=HELPER,previous_source_commit=DEPLOY.PREVIOUS122)
                self.assertFalse(client.calls)
    def test_unknown_previous_commit_or_live_release_binding_refuses(self):
        client=self.client()
        with self.assertRaises(AssertionError):DEPLOY.rest_deploy(self.source,'c'*40,DEPLOY.PREVIOUS122_LIVE,self.output,True,'offline-fixture-not-a-credential',client=client,helper=HELPER,previous_source_commit='f'*40)
        self.assertFalse(client.calls)
        client.message='Different source'
        with self.assertRaises(AssertionError):DEPLOY.rest_deploy(self.source,'c'*40,DEPLOY.PREVIOUS122_LIVE,self.output,True,'offline-fixture-not-a-credential',client=client,helper=HELPER,previous_source_commit=DEPLOY.PREVIOUS122)
        self.assertTrue(all(call[0]=='GET' for call in client.calls))
    def test_incremental_clone_damage_or_race_never_releases(self):
        for fault in ('clone_file_changed','clone_config_changed','race_before_release'):
            with self.subTest(fault=fault):
                client=self.client(fault)
                with self.assertRaises((AssertionError,HELPER.DeploymentError)),contextlib.redirect_stdout(io.StringIO()):DEPLOY.rest_deploy(self.source,'c'*40,DEPLOY.PREVIOUS122_LIVE,self.output,True,'offline-fixture-not-a-credential',client=client,helper=HELPER,previous_source_commit=DEPLOY.PREVIOUS122)
                self.assertFalse([call for call in client.calls if call[1].endswith('/releases')])

if __name__=='__main__':unittest.main()
