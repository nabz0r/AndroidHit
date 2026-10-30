#!/usr/bin/env python3
"""Offline receipt-coherence check for the exact 1.23 E / C2 delivery.

Only reads supplied receipts and adjacent original evidence. No network, Git,
subprocess, app/public/docs mutation, or fresh browser/native acceptance.
An unknown browser schema returns INCOMPLETE (exit 2), never PASS.
"""
import argparse
import datetime
import hashlib
import json
from pathlib import Path
import re
import sys

C1 = '2db51f1f208d5a13c023f67df32450410b873c96'
C2 = 'c99ac52903eca6707d3f4d644d7c480c79b63e68'
TREES = {C1: '2f24ea1a1710adae23aca0f0bca7a9f1ebef534a', C2: 'fde4e32a94e6210c8f7215acdcf4b9c69e213752'}
CI_HASHES = {C1: 'f8070930d595c9196c601ac399e8e9049b79823c7acaac965eebbd31443e1176', C2: '2077db329962533437274ffa2507750c863334d748bc54e1b69652b2fd176f21'}
SOURCE_HASH = '78994a2e02ad2e74b7439e112852bfbc69b7fd596998cd62f7933e435584507c'
PUBLIC_TREE = '62e59825f561bb4857510d3e602d43ec87535dd5'
OLD = 'b122c0acbd172a7912c69b0b6914b8b0f9b2a243'
LIVE = 'sites/luma-drift-production/versions/24e34cf6e6af1ed8'
ORIGIN = 'https://luma-drift-production.web.app'
BASE_PROVIDER_HASH = 'cb526cf375735a23466dd00916d9b32d89407dd246af2597d8157513820d8fb8'
CONFIG_HASH = '8f3767ca6d19f7f5b465636dcf961865adfbe7d52203afa5c6f044d54df9e3d7'
APKS = {
    '/releases/luma-drift-1.23.0-rc2.apk': {'sha256': '429a443c9228787c7b854d36a40f0bcf7d52fdc8a3601214cd27b5cfae6945fb', 'bytes': 369002457},
    '/releases/luma-drift-1.23.0-sandbox-rc2.apk': {'sha256': '099d4dc7d38cb4552b3b1ad46f7e3ccb70b304c7c8a1c6c332e16ada00ba558c', 'bytes': 368985756},
}
RETIRED = frozenset('/releases/' + name for name in (
    'luma-drift-1.15.0-rc1.aab', 'luma-drift-1.15.0-rc1.apk', 'luma-drift-1.16.0-rc1.aab', 'luma-drift-1.16.0-rc1.apk',
    'luma-drift-1.17.0-rc6.aab', 'luma-drift-1.17.0-rc6.apk', 'luma-drift-1.18.0-prototype1.apk', 'luma-drift-1.18.0-sandbox-prototype1.apk',
    'luma-drift-1.19.0-prototype2.apk', 'luma-drift-1.19.0-sandbox-prototype2.apk', 'luma-drift-1.20.0-rc1.apk', 'luma-drift-1.20.0-sandbox-rc1.apk',
    'luma-drift-1.21.0-rc1.apk', 'luma-drift-1.21.0-sandbox-rc1.apk'))
SELECTED = frozenset(['/index.html', '/assets/site-v123.css', '/assets/site-v123.js', '/releases/manifest-1.23.0-rc2.json', *APKS] +
    ['/assets/screenshots/v1.23/' + k + '-' + lang + '.png' for k in ('puzzle', 'skills', 'wardrobe', 'map') for lang in ('fr', 'en')] +
    ['/lab/clarity-123/' + name for name in ('index.html', 'report.css', 'report.js', 'data/view.json', 'data/snapshot.js', 'data/provenance.json', 'cognitive-lab-1.23-source.zip')])


class Incomplete(ValueError):
    pass


def need(ok, message):
    if not ok:
        raise ValueError(message)


def digest(path):
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def canonical_hash(value):
    return hashlib.sha256(json.dumps(value, sort_keys=True, separators=(',', ':')).encode()).hexdigest()


def read(path):
    path = Path(path)
    need(path.is_file() and not path.is_symlink(), 'Missing ordinary input file: ' + str(path))
    return json.loads(path.read_text(encoding='utf-8'))


def evidence_file(base, name, row):
    need(isinstance(name, str) and name and not name.startswith('/') and all(p not in ('', '.', '..') for p in name.split('/')), 'Unbounded evidence path')
    p = base / name
    need(base.resolve() in p.resolve().parents and not p.is_symlink(), 'Evidence escapes its root')
    need(p.is_file() and digest(p) == row['sha256'] and ('bytes' not in row or p.stat().st_size == row['bytes']), 'Evidence hash/size differs: ' + str(p))
    return p


def ci(path, commit):
    need(digest(path) == CI_HASHES[commit], 'Exact frozen CI receipt hash differs')
    v = read(path)
    need(v['status'] == 'PASS' and v['sourceCommit'] == commit and v['sourceTree'] == TREES[commit], 'CI source identity/verdict differs')
    need((v['workflowCount'], v['jobCount'], v['stepCount']) == (2, 3, 51) and len(v['workflowRuns']) == 2 and len(v['jobs']) == 3, 'CI coverage differs')
    need(all(r['head_sha'] == commit and r['run_attempt'] == 1 and r['status'] == 'completed' and r['conclusion'] == 'success' for r in v['workflowRuns']), 'CI run not successful exact attempt1')
    need(sum(len(j['steps']) for j in v['jobs']) == 51, 'CI step denominator differs')
    need(all(j['head_sha'] == commit and j['run_attempt'] == 1 and j['conclusion'] == 'success' and all(s['status'] == 'completed' and s['conclusion'] == 'success' for s in j['steps']) for j in v['jobs']), 'CI job/step failed or changed')
    for name, row in v['evidenceFiles'].items():
        evidence_file(path.parent, name, row)
    if commit == C2:
        d = read(path.parent / v['inputDelta'])
        need(d['baselineCommit'] == C1 and d['sourceCommit'] == C2 and d['sourceTree'] == TREES[C2] and d['changedPathCount'] == len(d['changedPaths']) == 22, 'C1/C2 delta identity differs')
        need(d['appPublicScriptsWorkflowsUnchanged'] and all(p.startswith('docs/') for p in d['changedPaths']), 'C2 changed app/public/scripts/workflows')
    return {'status': 'PASS', 'workflows': 2, 'jobs': 3, 'steps': 51, 'evidenceFiles': len(v['evidenceFiles'])}


def source_and_retirements(source_path, retired_path):
    need(digest(source_path) == SOURCE_HASH, 'Source is not the exact committed C2 manifest')
    s, r = read(source_path), read(retired_path)
    need(digest(retired_path) == s['frozen_inputs']['docs/validation/v1.23/cleanup/public-retirements.json'], 'Original frozen local-retirement receipt differs')
    need(s['status'] == 'PASS' and s['release'] == '1.23.0-rc2' and s['artifact_source_commit'] == C1 and s['edition'] == '1.23', 'Source release/artifact identity differs')
    need(s['package_identity_verified'] and s['native_capture_provenance_verified'] and len(s['files']) == 576, 'Source qualification/map differs')
    need(all(s['files'].get(p) == row for p, row in APKS.items()), 'Exact signed E APK pair differs')
    need(set(s['retired_files']) == RETIRED and len(r['retirements']) == r['retired_count'] == 14, 'Exactly14 retirements required')
    rows = {x['path']: {'sha256': x['sha256'], 'bytes': x['bytes']} for x in r['retirements']}
    need(len(rows) == 14 and rows == s['retired_files'], 'Retirement rows differ from immutable C2 source')
    need(r['baseline_source_commit'] == OLD and sum(x['bytes'] for x in rows.values()) == r['retired_logical_bytes'] == 3136669533, 'Retirement baseline/size differs')
    need(all(x['sha256'] == x['canonical_sha256'] and x['bytes'] == x['canonical_bytes'] for x in r['retirements']), 'Canonical-copy preservation identity differs')
    return s


def hosting(path, s):
    d = read(path)
    need(d['status'] == 'DEPLOY/FINALIZED' and d['sourceCommit'] == C2 and d['previousSourceCommit'] == OLD and d['expectedLiveVersion'] == LIVE, 'Hosting release/source/baseline differs')
    need(d['sourcePublicTree'] == PUBLIC_TREE and d['historicalProviderPaths'] == 572 and d['historicalPhysicalPaths'] == 570 and d['retiredPhysicalPaths'] == 14, 'Hosting source tree/baseline counts differ')
    need(d['publicOriginBytesVerified'] is False and d['publicOriginRenderingVerified'] is False, 'Management receipt overstates its public scope')
    need(re.fullmatch(r'sites/luma-drift-production/versions/[A-Za-z0-9_-]+', d['version']) and d['version'] != LIVE, 'Unexpected Hosting version')
    need(d['live']['versionName'] == d['version'] and d['live']['versionStatus'] == 'FINALIZED' and d['live']['releaseType'] == 'DEPLOY' and d['live']['releaseMessage'] == 'Website1.23 from ' + C2, 'Live Hosting identity differs')
    need(d['providerServingConfigUnchanged'] and d['retainedHistoricalProviderHashesPreserved'] and d['intentionalRetirementsVerified'], 'Provider preservation not accepted')
    before = read(path.parent / 'baseline-provider-files.json')
    config = read(path.parent / 'baseline-provider-config.json')
    overlay = read(path.parent / 'overlay-source-and-gzip-hashes.json')
    after = read(path.parent / 'verified-provider-files.json')
    need(len(before) == 572 and canonical_hash(before) == BASE_PROVIDER_HASH and canonical_hash(config) == CONFIG_HASH, 'Original accepted C3 provider map/config differs')
    need(set(overlay) == SELECTED and set(d['retiredPaths']) == RETIRED and d['intentionalChanges'] == ['/index.html'], 'Hosting overlay/retirement scope differs')
    need(set(d['addedPaths']) == SELECTED - {'/index.html'}, 'Hosting additions differ')
    for p, row in overlay.items():
        need(row['rawSha256'] == s['files'][p]['sha256'] and row['rawBytes'] == s['files'][p]['bytes'] and re.fullmatch(r'[a-f0-9]{64}', row['gzipSha256']), 'Provider overlay raw/gzip identity differs: ' + p)
    expected = {p: h for p, h in before.items() if p not in RETIRED}
    expected.update({p: row['gzipSha256'] for p, row in overlay.items()})
    need(after == expected and len(after) == d['finalExpectedProviderPaths'] == 578, 'Final578 provider hashes differ')
    need(d['preservedHistoricalPaths'] == 557 and not (set(after) & RETIRED), 'Retained/retired provider count differs')
    f = read(path.parent / 'finalized-version.json')
    need(f['name'] == d['version'] and f['status'] == 'FINALIZED' and (f.get('fileCount') is None or int(f['fileCount']) == 578), 'Finalized provider record differs')
    return d['version']


def public_origin(path, s):
    p = read(path)
    need(p['status'] == 'PASS' and p['source_commit'] == C2 and p['source_manifest_sha256'] == SOURCE_HASH and p['origin'] == ORIGIN, 'Public collector source/verdict differs')
    expected = {name: s['files'][name] for name in SELECTED}
    need(p['expected_files'] == p['files'] == expected and p['selected_paths'] == 21 and p['verified_bytes'] == sum(x['bytes'] for x in expected.values()), 'Complete21 response bodies differ')
    need(p['network_requested'] and not p['credentials_used'] and not p['redirects_followed'] and p['rendering_verified'] is False, 'Public collection scope is mislabeled')
    need(set(p['responses']) == SELECTED and set(p['retired_responses']) == RETIRED and set(p['retired_paths']) == RETIRED, 'Public response/retirement set differs')
    for name, row in p['responses'].items():
        need(row['status'] == 200 and row['url'] == ORIGIN + name + '?source_commit=' + C2 and row['requestedEncoding'] == 'identity' and row.get('contentEncoding', '') in ('', 'identity'), 'Public response metadata differs: ' + name)
        need({'sha256': row['sha256'], 'bytes': row['bytes']} == expected[name], 'Recorded response digest/length differs: ' + name)
    for name, row in p['retired_responses'].items():
        need(row['status'] == 404 and row['bodyRead'] is False and row['url'] == ORIGIN + name + '?source_commit=' + C2, 'Retired path lacks original no-body HTTP404: ' + name)
    return {'status': 'PASS', 'completeResponseBodies': 21, 'verifiedBytes': p['verified_bytes'], 'retiredHttp404': 14, 'renderingInferred': False}


def browser(path, base, version):
    b = read(path)
    if not isinstance(b, dict) or 'status' not in b or not ({'sourceCommit', 'finalSourceCommit'} & b.keys()) or 'hostingVersion' not in b:
        raise Incomplete('Browser report schema/identity is not yet recognized; keep the browser scope pending until an explicit C2/Hosting-bound PASS index is supplied.')
    need(b.get('status') == 'PASS', 'Browser report does not declare PASS')
    need(b.get('finalSourceCommit', b.get('sourceCommit')) == C2 and b.get('hostingVersion') in (version, version.rsplit('/', 1)[-1]), 'Browser source/Hosting binding differs')
    reports = b.get('reports')
    if not isinstance(reports, dict) or not reports or base is None:
        raise Incomplete('Browser schema pending: provide a PASS index with finalSourceCommit=C2, hostingVersion, nonempty reports {relativePath:{sha256,status:PASS}}, and --browser-evidence-root. Its leaf scopes remain human-reviewed.')
    for name, row in reports.items():
        need(row.get('status') == 'PASS', 'Browser leaf is not accepted: ' + name)
        leaf = read(evidence_file(base, name, row))
        need(leaf.get('status') == 'PASS' and isinstance(leaf.get('scope'), str) and leaf['scope'], 'Browser leaf lacks explicit accepted scope')
        need(leaf.get('sourceCommit', leaf.get('finalSourceCommit')) == C2, 'Browser leaf source differs; an older-source bridge requires separate review')
    return {'status': 'PASS', 'hashedReports': len(reports), 'scope': 'Declared public browser verdict/source and leaf evidence hashes only; no fresh observation or automatic interpretation of pixels, responsive coverage, interactions, console output, native or human behavior.'}


def main():
    p = argparse.ArgumentParser(description=__doc__)
    for name in ('hosting-provider', 'public-origin', 'browser-public', 'retirements', 'source-c2', 'c1-ci', 'c2-ci'):
        p.add_argument('--' + name, required=True, type=Path)
    p.add_argument('--browser-evidence-root', type=Path)
    a = p.parse_args()
    result = {'schemaVersion': 1, 'status': 'RUNNING', 'checkedAtUtc': datetime.datetime.now(datetime.timezone.utc).isoformat(), 'sourceCommit': C2, 'networkRequested': False, 'filesWritten': False, 'checks': {}}
    try:
        result['inputFiles'] = {key: {'path': str(value), 'sha256': digest(value), 'bytes': value.stat().st_size} for key, value in vars(a).items() if isinstance(value, Path) and key != 'browser_evidence_root'}
        s = source_and_retirements(a.source_c2, a.retirements)
        result['checks']['sourceAndRetirements'] = {'status': 'PASS', 'manifestSha256': SOURCE_HASH, 'apkIdentities': APKS, 'retiredPaths': 14}
        result['checks']['c1CI'] = ci(a.c1_ci, C1)
        result['checks']['c2CIAndUnchangedAppPublic'] = ci(a.c2_ci, C2)
        version = hosting(a.hosting_provider, s)
        result['checks']['provider'] = {'status': 'PASS', 'version': version, 'providerPaths': 578, 'untouchedProviderPaths': 557}
        result['checks']['publicOrigin'] = public_origin(a.public_origin, s)
        result['checks']['browserDeclaredScope'] = browser(a.browser_public, a.browser_evidence_root, version)
        result['status'] = 'PASS'
    except Incomplete as error:
        result.update(status='INCOMPLETE', error=str(error))
    except (ValueError, KeyError, TypeError, OSError) as error:
        result.update(status='FAIL', error=str(error))
    result['scope'] = 'Offline consistency of supplied original receipts and pinned identities. No network, re-fetch, deployment, build, new simulation, browser/native replay, human or physical-device acceptance.'
    print(json.dumps(result, indent=2))
    return {'PASS': 0, 'FAIL': 1, 'INCOMPLETE': 2}[result['status']]


if __name__ == '__main__':
    sys.exit(main())
