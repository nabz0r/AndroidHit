#!/usr/bin/env python3
"""Offline full-response and retirement probes; no actual public request is made."""
import contextlib
import copy
import hashlib
import io
import json
from pathlib import Path
import ssl
import types
import unittest
from unittest.mock import patch
import urllib.error

ROOT=Path(__file__).resolve().parents[1]
SCRIPT=ROOT/'scripts/collect-site-v123-public-bytes.py'
MODULE=types.ModuleType('collector123');MODULE.__file__=str(SCRIPT);exec(compile(SCRIPT.read_bytes(),str(SCRIPT),'exec'),MODULE.__dict__)

class Response(io.BytesIO):
    def __init__(self,url,body,mime,status=200,headers=None):
        super().__init__(body);self.url,self.status=url,status;self.headers={'Content-Type':mime,'Content-Length':str(len(body)),**(headers or {})};self.read_amounts=[]
    def geturl(self):return self.url
    def read(self,amount):self.read_amounts.append(amount);return super().read(amount)
class Opener:
    def __init__(self,factory):self.factory,self.requests=factory,[]
    def open(self,request,timeout):self.requests.append(request);return self.factory(request)

class Collector123Tests(unittest.TestCase):
    def setUp(self):
        self.baseline=json.loads((ROOT/MODULE.BASELINE).read_text());self.source=copy.deepcopy(self.baseline);self.commit='c'*40
        for name in MODULE.RETIRED_PATHS:self.source['files'].pop(name)
        self.bodies={name:('fictional response '+name).encode() for name in MODULE.REQUIRED_ADDITIONS|{'/index.html','/lab/clarity-123/attempts.csv.gz'}}
        for name,body in self.bodies.items():self.source['files'][name]={'sha256':hashlib.sha256(body).hexdigest(),'bytes':len(body)}
        self.source.update(status='PASS',edition='1.23',release='1.23.0-rc2',native_capture_provenance_verified=True,package_identity_verified=True,
                           retired_files={name:self.baseline['files'][name] for name in MODULE.RETIRED_PATHS},
                           production_apk_sha256=self.source['files']['/releases/luma-drift-1.23.0-rc2.apk']['sha256'],
                           lab_apk_sha256=self.source['files']['/releases/luma-drift-1.23.0-sandbox-rc2.apk']['sha256'])
        self.selected=MODULE.select(self.source,self.baseline)
        self.guard=patch.object(MODULE.urllib.request.OpenerDirector,'open',side_effect=AssertionError('Network prohibited'));self.guard.start()
    def tearDown(self):self.guard.stop()
    def response(self,request):
        name=MODULE.urllib.parse.urlsplit(request.full_url).path
        if name in MODULE.RETIRED_PATHS:raise urllib.error.HTTPError(request.full_url,404,'Not Found',{'Content-Type':'text/html'},io.BytesIO(b'404 fixture'))
        return Response(request.full_url,self.bodies[name],sorted(MODULE.MIMES[Path(name).suffix])[0])
    def test_exact_c3_baseline_selects_new_bodies_and_only_homepage_overwrite(self):
        self.assertEqual(MODULE.PREVIOUS122,'b122c0acbd172a7912c69b0b6914b8b0f9b2a243');self.assertEqual(len(self.baseline['files']),570)
        self.assertEqual(set(self.selected),set(self.bodies));self.assertEqual(len(self.selected),len(MODULE.REQUIRED_ADDITIONS)+2)
        self.assertEqual(sum(name.endswith('.apk') for name in self.selected),2)
        self.assertFalse(any(name.startswith('/lab/cognitive/') for name in self.selected))
    def test_complete_mocked_responses_plus14_http404_checks_have_exact_receipt(self):
        opener=Opener(self.response);receipt={'status':'RUNNING','files':{},'responses':{}};saved=[]
        with contextlib.redirect_stdout(io.StringIO()):MODULE.collect(self.selected,self.commit,receipt,opener,lambda row:saved.append(copy.deepcopy(row)),MODULE.RETIRED_PATHS)
        self.assertEqual(receipt['status'],'PASS');self.assertEqual(receipt['files'],self.selected);self.assertEqual(set(receipt['retired_responses']),MODULE.RETIRED_PATHS)
        self.assertEqual(len(opener.requests),len(self.selected)+14)
        for row in receipt['retired_responses'].values():self.assertEqual(row['status'],404);self.assertFalse(row['bodyRead'])
        for request in opener.requests:self.assertEqual(request.headers,{'Accept-encoding':'identity'});self.assertTrue(request.full_url.startswith(MODULE.ORIGIN+'/'));self.assertEqual(MODULE.urllib.parse.urlsplit(request.full_url).query,'source_commit='+self.commit)
    def test_retired200_body_is_never_read_and_collection_stops(self):
        name=sorted(MODULE.RETIRED_PATHS)[0];response=Response(MODULE.ORIGIN+name+'?source_commit='+self.commit,b'obsolete-binary','application/octet-stream')
        opener=Opener(lambda request:response)
        with self.assertRaises(MODULE.CollectionError):MODULE.retired_response(name,self.commit,opener)
        self.assertEqual(response.read_amounts,[]);self.assertEqual(len(opener.requests),1)
    def test_access_refusal_stops_before_next_request(self):
        def refused(request):raise urllib.error.HTTPError(request.full_url,403,'Forbidden',{},io.BytesIO(b'refusal fixture'))
        opener=Opener(refused);receipt={'status':'RUNNING','files':{},'responses':{}}
        with self.assertRaises(MODULE.CollectionError):MODULE.collect(self.selected,self.commit,receipt,opener,lambda row:None,MODULE.RETIRED_PATHS)
        self.assertEqual(len(opener.requests),1);self.assertEqual(receipt['status'],'REFUSED');self.assertTrue(receipt['no_fallback_attempted'])
    def test_hash_type_encoding_length_or_extra_bytes_fail_closed(self):
        expected={'sha256':hashlib.sha256(b'good').hexdigest(),'bytes':4}
        for raw,mime,headers in ((b'evil','text/html',{}),(b'good','application/json',{}),(b'good','text/html',{'Content-Encoding':'gzip'}),(b'longer','text/html',{'Content-Length':'4'})):
            with self.subTest(raw=raw,mime=mime,headers=headers):
                response=Response(MODULE.ORIGIN+'/index.html?source_commit='+self.commit,raw,mime,headers=headers)
                with self.assertRaises(MODULE.CollectionError):MODULE.response_identity('/index.html',expected,self.commit,Opener(lambda request:response))
    def test_unapproved_deletion_overwrite_addition_or_retirement_hash_refuses(self):
        mutations=[lambda s:s['files'].pop('/lab/cognitive/index.html'),lambda s:s['files']['/lab/cognitive/index.html'].update(sha256='0'*64),lambda s:s['files'].update({'/other.json':{'sha256':'0'*64,'bytes':1}}),lambda s:s['retired_files'][next(iter(MODULE.RETIRED_PATHS))].update(sha256='0'*64),lambda s:s.update(release='1.23.0-rc1')]
        for mutation in mutations:
            source=copy.deepcopy(self.source);mutation(source)
            with self.subTest(mutation=mutation),self.assertRaises(MODULE.CollectionError):MODULE.select(source,self.baseline)
    def test_missing_any_required_capture_asset_or_release_refuses(self):
        for name in MODULE.REQUIRED_ADDITIONS:
            source=copy.deepcopy(self.source);source['files'].pop(name)
            with self.subTest(name=name),self.assertRaises(MODULE.CollectionError):MODULE.select(source,self.baseline)
    def test_redirect_tls_and_retired_access_refusal_have_no_fallback(self):
        with self.assertRaises(MODULE.CollectionError):MODULE.NoRedirect().redirect_request(None,None,302,'redirect',{},'https://evil.invalid')
        def tls(request):raise urllib.error.URLError(ssl.SSLCertVerificationError('offline refusal'))
        with self.assertRaises(MODULE.CollectionError) as error:MODULE.retired_response(next(iter(MODULE.RETIRED_PATHS)),self.commit,Opener(tls))
        self.assertTrue(error.exception.refused)
        def denied(request):raise urllib.error.HTTPError(request.full_url,403,'Forbidden',{},io.BytesIO(b'refusal fixture'))
        with self.assertRaises(MODULE.CollectionError) as error:MODULE.retired_response(next(iter(MODULE.RETIRED_PATHS)),self.commit,Opener(denied))
        self.assertTrue(error.exception.refused)
    def test_noncanonical_paths_and_unlisted_retirement_never_request(self):
        for name in ('//evil.invalid/index.html','/lab/clarity-123/../escape.json','/lab/clarity-123//bad.json'):
            with self.assertRaises(MODULE.CollectionError):MODULE.bounded_path(name)
        opener=Opener(lambda request:self.fail('Unexpected network'))
        with self.assertRaises(MODULE.CollectionError):MODULE.retired_response('/releases/luma-drift-1.22.0-rc1.apk',self.commit,opener)
        self.assertFalse(opener.requests)

if __name__=='__main__':unittest.main()
