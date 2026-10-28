#!/usr/bin/env python3
"""Offline 1.15 website audit: bilingual DOM, local downloads and native image provenance.

No HTTP request, browser, deployment or provider transaction is performed. The historical
1.7 parser/configuration helpers are reused; historical edition evidence is never written.
"""
import argparse
import hashlib
import importlib.util
import json
from pathlib import Path
import re
import struct
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
PUBLIC=ROOT/'public'
VERSION='1.15.0-rc1'
APK='luma-drift-'+VERSION+'.apk'
AAB='luma-drift-'+VERSION+'.aab'
MANIFEST='manifest-'+VERSION+'.json'
APK_SHA='07b5bfc2632edc3d34f7583ac71c8343b52d7b926ed526aeb7a88599253df0f9'
UNCHANGED={
 'public/privacy/index.html':'3a4e666de4e8105c80dda2934ae12fec3c98ec4d2a8bb3e9c393254acc0093a3',
 'public/delete-account/index.html':'0c30aed6e846765e3c3befa97d881c2402c7614c99c81fbdea00c8bc7e75e217',
 'public/site-config.js':'6053074f0b45e54e14267e7d2316be8f68d981110c2376641653e039fc0f4f67',
 'public/assets/delete-account.js':'68933dc4981d16a7473776c2cef6635799558923f3a11673951f1f9c768b0ce3',
 'public/assets/deletion-core.mjs':'67815fa6c57a157aa6d712e56b29bb356bdf3bf21da63f9b7275d4dcf2e512ad',
 'public/assets/style.css':'c96901e3af93b34774530dcde920ef9f215b68666d91d858ebbb3858cc7a2a0c',
 'public/app-ads.txt':'1ec22f11296d1bf892fdd26f7019ca10049cc97977d31ce9ea806b888b6dbcba',
 'firebase.json':'1a02cfbadae7edeff76275b7c2c223664b475d0406ba310cf45b07bd20842867',
}
SPEC=importlib.util.spec_from_file_location('site_contract_helpers',ROOT/'scripts/check-site-v17.py')
BASE=importlib.util.module_from_spec(SPEC);SPEC.loader.exec_module(BASE)

def history_snapshot():
    files=[p for folder in (ROOT/'docs/validation').glob('site-v*') if folder.name!='site-v1.15' for p in folder.rglob('*') if p.is_file()]
    files += [p for p in (ROOT/'scripts').glob('*site*v*.py') if 'v115' not in p.name]
    files += [p for p in (PUBLIC/'tests').glob('site-language-v*.test.mjs') if 'v115' not in p.name]
    return {str(p.relative_to(ROOT)):BASE.digest(p) for p in files}

class Audit(BASE.Audit):
    def __init__(self,args):
        output=Path(args.report_dir).resolve();validation=(ROOT/'docs/validation').resolve()
        if validation in output.parents and not any(p.name=='site-v1.15' for p in (output,)+tuple(output.parents)):
            raise ValueError('The 1.15 audit cannot overwrite another edition evidence')
        self.history_before=history_snapshot();self.native_inputs=set();self.release_record={}
        super().__init__(args)

    def edition_contract(self):
        home=self.documents[(PUBLIC/'index.html').resolve()];raw=home.path.read_text()
        self.check(home.meta('color-scheme')=='light','Homepage declares its light colour scheme')
        self.check('1.15' in home.tags('title')[0].text(),'Homepage title identifies the current edition')
        self.check(not re.search(r'__APK_[A-Z0-9_]+__|TODO|TBD',raw),'No unresolved release placeholders')
        self.check(not re.search(r'screenshots/v1\.(?:14|13|12)/|1\.14\.0-rc2',raw),'Homepage advertises only current captures/downloads')
        for lang in ('fr','en'):
            copy=BASE.normalized(home.root.visible_text(lang));low=copy.lower()
            for word in (('Aventure','Maison','Collection','Atelier') if lang=='fr' else ('Adventure','Home','Collection','Workshop')):
                self.check(word.lower() in low,'Named destination appears in '+lang+': '+word)
            for pattern,label in ((r'\b1[\s,.\u00a0\u202f]?000\b','1000 boards'),(r'\b42\b','42 furnishings'),(r'10\s*[×x]\s*10','10 by 10 rooms')):
                self.check(bool(re.search(pattern,copy)),label+' appears in '+lang)
            self.check(('trois pièces' in low if lang=='fr' else bool(re.search(r'three[^.!?]{0,40}rooms',low))),'Three rooms are explicit in '+lang)
            self.check(bool(re.search(r'porte' if lang=='fr' else r'gate|door',low)),'New door rule is described in '+lang)
            self.check(bool(re.search(r'\bsort(?:ie|ent|ir)?\b|quitt' if lang=='fr' else r'exit|leav',low)),'Door changes are linked to piece exits in '+lang)
            self.check(bool(re.search(r'annul' if lang=='fr' else r'undo',low)) and bool(re.search(r'illimit|autant|répét|sans limite|à volonté' if lang=='fr' else r'unlimited|repeat|as often',low)),'Repeatable Undo is explicit in '+lang)
            self.check(('achats réels' if lang=='fr' else 'real checkout') in low,'Existing live commerce qualification remains explicit in '+lang)
        site=(PUBLIC/'assets/site.js').read_text()
        self.check('Luma Drift 1.15' in site and 'Luma Drift 1.14' not in site,'Runtime title follows edition 1.15')
        self.check(all(x in raw for x in ('assets/site.css','assets/site.js','site-config.js')),'Homepage loads current presentation and retained configuration')

    def release(self,home):
        path=ROOT/'releases'/MANIFEST
        self.check(path.is_file(),'Frozen production release manifest exists')
        if not path.is_file():return
        manifest=json.loads(path.read_text());self.release_record=manifest
        self.check(manifest.get('version')==VERSION and manifest.get('version_code')==17 and manifest.get('application_id')=='com.androidhit.lumadrift','Production release is exactly 1.15.0-rc1/code17')
        self.check(self.args.apk_sha256==APK_SHA,'Independent production APK hash agrees with delivered candidate E')
        commit=self.args.source_commit or ''
        self.check(bool(re.fullmatch(r'[a-f0-9]{40}',commit)),'Immutable local artifact commit supplied')
        for name in (APK,AAB):
            local=ROOT/'releases'/name;public=PUBLIC/'releases'/name;entry=manifest.get('artifacts',{}).get(name,{})
            exists=local.is_file() and public.is_file();self.check(exists,'Both canonical and hosted release bytes exist: '+name)
            if exists:
                self.check(BASE.digest(local)==BASE.digest(public)==entry.get('sha256') and local.stat().st_size==public.stat().st_size==entry.get('bytes'),'Hosted artifact is identical to the signed release: '+name)
                if name==APK:self.check(BASE.digest(local)==APK_SHA,'Signed APK has the independent final candidate hash')
            if re.fullmatch(r'[a-f0-9]{40}',commit):
                result=subprocess.run(['git','show',commit+':releases/'+name],cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.PIPE,timeout=30)
                self.check(result.returncode==0 and hashlib.sha256(result.stdout).hexdigest()==entry.get('sha256') and len(result.stdout)==entry.get('bytes'),'Immutable local artifact commit contains the exact release: '+name)
        public_manifest=PUBLIC/'releases'/MANIFEST
        self.check(public_manifest.is_file() and BASE.digest(public_manifest)==BASE.digest(path),'Hosted release manifest equals its canonical source')
        cfg=manifest.get('configuration',{})
        self.check(cfg.get('paid_cosmetic_draws_enabled') is False and cfg.get('force_test_ads') is True and cfg.get('live_services_validated') is False and cfg.get('wallet_api_configured') is False,'Delivered APK retains its actual commerce/provider qualification gates')
        links=[n.attrs.get('href','') for n in home.tags('a')]
        apk_links=[href for href in links if '.apk' in href]
        self.check(len(apk_links)>=2 and all(href=='/releases/'+APK for href in apk_links),'Every APK action uses the local exact production download')
        self.check('/releases/'+AAB in links and '/releases/'+MANIFEST in links,'Exact hosted AAB and manifest are linked')
        for lang in ('fr','en'):
            notes=BASE.normalized(' '.join(n.visible_text(lang) for n in home.nodes if n.has_class('download-note')))
            self.check(VERSION in notes and re.search(r'\b17\b',notes),'Current version and Android code visible in '+lang)
            sizes=re.findall(r'(\d+(?:[.,]\d+)?)\s*(Mo|MB|Mio|MiB)\b',notes)
            self.check(bool(sizes),'Actual APK size disclosed in '+lang)
            for value,unit in sizes:
                actual=manifest['artifacts'][APK]['bytes']/(1024**2 if unit in ('Mio','MiB') else 1e6)
                self.check(abs(float(value.replace(',','.'))-actual)<=.05,'Visible APK size agrees with actual bytes in '+lang)
        self.check(APK_SHA in ' '.join(n.text() for n in home.nodes if n.has_class('integrity')),'Visible integrity value is the signed APK hash')

    def configuration(self):
        super().configuration()
        for name,sha in UNCHANGED.items():self.check(BASE.digest(ROOT/name)==sha,'Legal/configuration/Hosting bytes retained: '+name)
        css=(PUBLIC/'assets/site.css').read_text()
        for token in ('[hidden]',':focus-visible','prefers-reduced-motion','scroll-behavior:auto'):
            self.check(token in css,'Homepage retains accessibility CSS: '+token)
        self.check(not re.search(r'@import|https?://|url\(',css),'Homepage CSS introduces no external font/resource requests')

    def asset_sources(self):
        path=ROOT/'docs/validation/site-v1.15/screenshot-provenance.json'
        self.check(path.is_file(),'Current native screenshot provenance exists')
        if not path.is_file():return
        provenance=json.loads(path.read_text());declared={}
        self.check(provenance.get('schemaVersion')==1 and provenance.get('release')==VERSION and provenance.get('screenshotsEdited') is False,'Screenshot provenance declares exact edition and unedited native pixels')
        for entry in provenance.get('images',[]):
            public,source,evidence,sha=(entry.get(key,'') for key in ('public','source','evidence','sha256'))
            self.check(public not in declared,'Unique provenance for '+public);declared[public]=entry
            target,original,report_path=[(ROOT/name).resolve() for name in (public,source,evidence)]
            safe=public.startswith('public/assets/screenshots/v1.15/') and (source.startswith('docs/screenshots/v1.15/') or (source.startswith('docs/validation/v1.15/') and '/native/' in source)) and (evidence.startswith('docs/validation/v1.15/') or evidence.startswith('docs/validation/site-v1.15/native-furnished/')) and all(ROOT.resolve() in p.parents for p in (target,original,report_path))
            self.check(safe,'Release-specific source and evidence paths are safe: '+public)
            if not safe:continue
            exists=all(p.is_file() for p in (target,original,report_path));self.check(exists,'Capture, copy and native evidence exist: '+public)
            if not exists:continue
            self.check(BASE.digest(target)==BASE.digest(original)==sha,'Published capture is byte-identical to its original: '+public)
            raw=target.read_bytes();png=raw.startswith(b'\x89PNG\r\n\x1a\n') and len(raw)>=24
            self.check(png,'Original capture is a PNG: '+public)
            if png:
                width,height=struct.unpack('>II',raw[16:24]);self.check(width>=800 and height>width and (width,height)==(entry.get('width'),entry.get('height')),'Original portrait dimensions retained: '+public)
            self.check(entry.get('locale') in ('fr','en') and bool(entry.get('description')),'Actual capture locale/scope is explicit: '+public)
            locale=re.search(r'-(fr|en)\.png$',source)
            if locale:self.check(entry.get('locale')==locale.group(1),'Declared locale agrees with original native filename: '+public)
            self.check(not any(token in source.lower() for token in ('analysis-crop','analysis_crop','/analysis/')),'Analysis crops are not presented as full native captures: '+public)
            report=json.loads(report_path.read_text())
            self.check(report.get('passed') is True and (report.get('apk_sha256') or report.get('game_apk_sha256'))==APK_SHA,'Native capture report passed on the exact delivered APK: '+public)
            records=report.get('screenshots',report.get('evidence_sha256',{}))
            self.check(any(value==sha and (source==key or source.endswith('/'+key)) for key,value in records.items()),'Passing report indexes the original filename and PNG hash: '+public)
            self.native_inputs.update((source,evidence))
        referenced=set()
        for image in self.documents[(PUBLIC/'index.html').resolve()].tags('img'):
            for attr in ('src','data-screenshot-fr','data-screenshot-en'):
                value=image.attrs.get(attr,'')
                if '/screenshots/' not in value:continue
                self.check('screenshots/v1.15/' in value,'Rendered gameplay capture belongs to current edition: '+value)
                referenced.add(str((PUBLIC/value.lstrip('/').split('?',1)[0]).resolve().relative_to(ROOT)))
        self.check(bool(referenced) and referenced==set(declared),'Every displayed native capture has an exact provenance counterpart')
        public_current={str(p.relative_to(ROOT)) for p in (PUBLIC/'assets/screenshots/v1.15').glob('*.png')}
        self.check(public_current==set(declared),'Every current public PNG is covered by native provenance')
        for file in BASE.payloads():
            if file.suffix.lower() not in ('.png','.jpg','.webp','.svg'):continue
            if file.suffix.lower()=='.svg':
                svg=ET.fromstring(file.read_bytes());self.check('viewBox' in svg.attrib and not any(n.tag.rsplit('}',1)[-1] in ('script','foreignObject') for n in svg.iter()),'Scalable vector has no embedded active content: '+str(file.relative_to(ROOT)))
            name=str(file.relative_to(ROOT));self.assets.append({'public':name,'bytes':file.stat().st_size,'sha256':BASE.digest(file),'source_classification':'current-unedited-native-capture' if name in declared else 'retained-existing-asset','exact_source':declared.get(name,{}).get('source'),'native_evidence':declared.get(name,{}).get('evidence')})

    def syntax_and_refs(self):
        super().syntax_and_refs()
        if self.node:
            result=subprocess.run([self.node,'--test',str(PUBLIC/'tests/site-language-v115.test.mjs')],text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=30)
            self.check(result.returncode==0,'Current bilingual controller contracts pass');(self.out/'language-tests.txt').write_text(result.stdout)
        self.check(self.history_before==history_snapshot(),'Historical website scripts/tests/evidence remain unchanged')

    def run(self):
        passed=super().run()
        path=self.out/'static-check.json';report=json.loads(path.read_text())
        report.update(edition='1.15',release=VERSION,release_ready=passed,independent_apk_sha256=APK_SHA,artifact_source_commit=self.args.source_commit)
        path.write_text(json.dumps(report,indent=2)+'\n')
        path=self.out/'source-manifest.json';manifest=json.loads(path.read_text())
        frozen=['scripts/check-site-v115.py','scripts/check-site-v17.py','docs/validation/site-v1.15/screenshot-provenance.json','public/tests/site-language-v115.test.mjs','releases/'+MANIFEST]
        frozen+=sorted(self.native_inputs)
        manifest.update(edition='1.15',release=VERSION,production_apk_sha256=APK_SHA,artifact_source_commit=self.args.source_commit,runner_sha256=BASE.digest(Path(__file__)),base_runner_sha256=BASE.digest(ROOT/'scripts/check-site-v17.py'),frozen_inputs={name:BASE.digest(ROOT/name) for name in frozen if (ROOT/name).is_file()})
        path.write_text(json.dumps(manifest,indent=2)+'\n')
        return passed

if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source-commit');parser.add_argument('--node');parser.add_argument('--apk-sha256',default=APK_SHA)
    parser.add_argument('--report-dir',default=str(ROOT/'docs/validation/site-v1.15/release'));parser.add_argument('--self-test',action='store_true')
    args=parser.parse_args();BASE.parser_self_test()
    if args.self_test:print('PASS inherited DOM visibility and exact plus-tag parser regressions');sys.exit(0)
    sys.exit(0 if Audit(args).run() else 1)
