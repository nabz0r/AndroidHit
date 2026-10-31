#!/usr/bin/env python3
"""Prepare the1.24 homepage after eight exact D native captures; no auth or deployment.

The reviewed pending template remains an immutable input. --preview writes a
separate final preview. --write additionally applies the same verified bytes to
public and writes its source manifest. Existing1.23 payloads cannot be removed
or altered, except /index.html. APKs stay ignored external files.
"""
import argparse
import hashlib
import html
import json
from pathlib import Path
import re
import shutil
import struct
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PUBLIC = ROOT / 'public'
EVIDENCE = ROOT / 'docs/validation/site-v1.24'
STAGING = EVIDENCE / 'staging'
TEMPLATE = STAGING / 'index.preview.html'
BASELINE = 'docs/validation/site-v1.23/release/source-manifest.json'
PREVIOUS123 = '5cc870701c8039e664d1c00a90d0586c3d00521c'
PREVIOUS123_DEPLOY_SOURCE = 'c99ac52903eca6707d3f4d644d7c480c79b63e68'
PREVIOUS123_PUBLIC_TREE = '62e59825f561bb4857510d3e602d43ec87535dd5'
RELEASE = '1.24.0-rc2'
SCREEN_KEYS = ('puzzle', 'map', 'companion', 'home')
SIGNER = 'dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'


def sha(path):
    result = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            result.update(block)
    return result.hexdigest()


def bounded(name):
    value = Path(name)
    value = value if value.is_absolute() else ROOT / value
    relative = value.relative_to(ROOT)
    assert all(part not in ('', '.', '..') for part in str(relative).split('/')), 'Unbounded input'
    assert not any(ROOT.joinpath(*relative.parts[:i]).is_symlink() for i in range(1, len(relative.parts)+1)), 'Symlink input refused'
    value = value.resolve()
    assert ROOT.resolve() in value.parents and value.is_file(), 'Missing bounded input: ' + str(name)
    return value


def relative(path):
    return str(path.relative_to(ROOT))


def section(source, key):
    result = re.search(r'<section id="' + re.escape(key) + r'"(?=\s|>).*?</section>', source, re.S)
    assert result, 'Missing preserved section: ' + key
    return result.group(0)


def w(fr, en):
    return '<span data-lang="fr" lang="fr">' + fr + '</span><span data-lang="en" lang="en" hidden>' + en + '</span>'


def validate_captures(captures, release):
    assert isinstance(captures, list) and len(captures) == 8, 'Exactly eight original captures required'
    seen, proof, pending, frozen = set(), [], [], {}
    for row in captures:
        key, locale = row['key'], row['locale']
        assert key in SCREEN_KEYS and locale in ('fr', 'en') and (key, locale) not in seen, 'Capture associations must be unique and complete'
        seen.add((key, locale))
        source, report_path = bounded(row['source']), bounded(row['report'])
        report = json.loads(report_path.read_text())
        assert report['passed'] and report['native']['passed'], 'Native report must pass'
        assert report['apk_sha256'] == report['installed_sha256'] == release['apks']['production']['sha256'], 'Capture must bind exact production D APK'
        assert report['expected_version'] == RELEASE and report['expected_code'] == 33 and report['package'] == 'com.androidhit.lumadrift', 'Native package/version differs'
        digest = sha(source)
        assert report['evidence_sha256'].get(str(source.relative_to(report_path.parent))) == digest, 'Report does not bind original PNG bytes'
        with source.open('rb') as stream:
            header = stream.read(24)
        assert header[:8] == b'\x89PNG\r\n\x1a\n' and len(header) == 24 and struct.unpack('>II', header[16:24]) == (1080, 2400), 'Expected full1080x2400 native PNG'
        target = PUBLIC / ('assets/screenshots/v1.24/' + key + '-' + locale + '.png')
        assert not target.exists() or sha(target) == digest, 'Never overwrite earlier native capture bytes'
        pending.append((source, target))
        proof.append({'key': key, 'locale': locale, 'source': relative(source), 'public': relative(target), 'sha256': digest, 'report': relative(report_path), 'reportSha256': sha(report_path), 'apkSha256': report['apk_sha256'], 'screenshotsEdited': False})
        frozen.update({relative(source): digest, relative(report_path): sha(report_path)})
    return proof, pending, frozen


def page(template, release):
    assert 'data-edition="1.24-pending"' in template, 'Reviewed pending template required'
    result = template.replace('data-edition="1.24-pending"', 'data-edition="1.24"')
    result = result.replace(section(result, 'release-124-pending'), '')
    result = result.replace(w('Le jeu,<br><em>tel qu’il sera capturé.</em>', 'The game,<br><em>as it will be captured.</em>'), w('Le jeu,<br><em>sur Android.</em>', 'The game,<br><em>on Android.</em>'))
    result = result.replace(w('Les quatre vues FR/EN attendent la candidate exacte. Aucun ancien écran n’est présenté comme une capture 1.24.', 'The four FR/EN views await the exact candidate. No older screen is presented as a 1.24 capture.'), w('Quatre vues natives de cette candidate exacte, en français et en anglais. Captures originales non retouchées.', 'Four native views from this exact candidate, in French and English. Unedited original captures.'))
    for key in SCREEN_KEYS:
        pattern = r'(<figure data-native-slot="' + key + r'">)<div class="native-pending">.*?</div>(<figcaption>)'
        base = 'assets/screenshots/v1.24/' + key
        image = '<div class="phone"><img src="' + base + '-fr.png" data-screenshot-fr="' + base + '-fr.png" data-screenshot-en="' + base + '-en.png" alt="Android 1.24 · ' + key + '" data-alt-fr="Android 1.24 · ' + key + '" data-alt-en="Android 1.24 · ' + key + '" width="1080" height="2400" decoding="async" loading="lazy"></div>'
        result, count = re.subn(pattern, lambda match: match.group(1) + image + match.group(2), result, flags=re.S)
        assert count == 1, 'Missing unique native slot: ' + key
    result = result.replace(w('Capitaine, niveau, copies et usages restent distincts.', 'Captain, level, copies and uses remain distinct.'), w('Un talent, sa Charge, ses usages et un retour au puzzle.', 'A skill, its Charge, its uses and a return to the puzzle.'))
    prod, lab = release['apks']['production'], release['apks']['lab']
    button = '<a class="button primary" href="/' + prod['path'] + '">' + w('Télécharger sur Android', 'Download for Android') + ' ↓</a>'
    previous_cta = '<a class="button primary" href="#progression">' + w('Voir comment il grandit', 'See how they grow') + ' <span aria-hidden="true">↗</span></a>'
    assert previous_cta in result, 'Missing reviewed hero action'
    result = result.replace(previous_cta, button, 1)
    download = '<section id="download" class="section wrap download-section" aria-labelledby="download-title"><div><p class="eyebrow">ANDROID · 1.24</p><h2 id="download-title">' + w('Le prochain départ<br><em>t’attend.</em>', 'Your next start<br><em>is waiting.</em>') + '</h2><div class="actions">' + button + '</div><p class="download-note">1.24.0-rc2 · code 33 · ' + w('340,29 Mo', '340.29 MB') + ' · Android 8+</p></div><div class="release-information"><p>' + w('Candidate à essayer. Annonces de test ; achats réels indisponibles. Distribution directe en APK, sans publication Play. Les contrôles techniques et simulations ne prouvent ni plaisir ni rétention humaine.', 'Playtest candidate. Test ads; real purchases unavailable. Direct APK distribution, without a Play publication. Technical checks and simulations establish neither human enjoyment nor retention.') + '</p><details><summary>' + w('Fichiers et empreintes', 'Files and checksums') + '</summary><p>' + str(prod['bytes']) + ' ' + w('octets', 'bytes') + ' · APK SHA-256</p><code>' + prod['sha256'] + '</code><p><a href="/releases/manifest-1.24.0-rc2.json">' + w('Manifeste de cette candidate', 'Candidate manifest') + '</a></p><p><a href="/' + lab['path'] + '">' + w('Variante Lab séparée', 'Separate Lab variant') + '</a> · ' + str(lab['bytes']) + ' ' + w('octets', 'bytes') + '</p><code>' + lab['sha256'] + '</code><p><a href="/releases/luma-drift-1.23.0-rc2.apk">' + w('Archive Android 1.23', 'Android 1.23 archive') + '</a> · <a href="/releases/luma-drift-1.22.0-rc1.apk">' + w('Archive Android 1.22', 'Android 1.22 archive') + '</a></p></details></div></section>'
    result = result.replace(section(result, 'download'), download)
    for asset in ('site-v124.js', 'site-v124.css'):
        result = re.sub(r'assets/' + re.escape(asset) + r'(?:\?v=[a-f0-9]+)?', 'assets/' + asset + '?v=' + sha(PUBLIC / 'assets' / asset)[:16], result)
    assert 'native-pending' not in result and 'release-124-pending' not in result, 'Final page cannot remain pending'
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--artifact-manifest', type=Path, default=ROOT / 'releases/manifest-1.24.0-rc2.json')
    parser.add_argument('--capture-map', type=Path, required=True)
    parser.add_argument('--artifact-commit', help='Exact commit containing immutable signed artifact metadata; mandatory with --write')
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--write', action='store_true')
    args = parser.parse_args()
    assert args.preview or args.write, 'Choose explicit --preview or --write; nothing is applied by default'
    assert args.artifact_commit is None or re.fullmatch('[a-f0-9]{40}', args.artifact_commit), 'Exact artifact commit required'
    assert not args.write or args.artifact_commit, '--write requires the immutable artifact metadata commit'
    for commit in (PREVIOUS123, PREVIOUS123_DEPLOY_SOURCE):
        assert subprocess.check_output(['git', 'rev-parse', commit + ':public'], cwd=ROOT, text=True).strip() == PREVIOUS123_PUBLIC_TREE, 'Accepted1.23 public-tree bridge differs'
    baseline_bytes = subprocess.check_output(['git', 'show', PREVIOUS123 + ':' + BASELINE], cwd=ROOT)
    baseline = json.loads(baseline_bytes)
    assert sha(bounded(BASELINE)) == hashlib.sha256(baseline_bytes).hexdigest() and len(baseline['files']) == 576, 'Pinned baseline differs'
    manifest = bounded(args.artifact_manifest)
    release = json.loads(manifest.read_text())
    assert relative(manifest) == 'releases/manifest-1.24.0-rc2.json' and release['edition'] == '1.24' and release['version'] == RELEASE and release['version_code'] == 33, 'Exact D release metadata required'
    if args.artifact_commit:
        committed = subprocess.check_output(['git', 'show', args.artifact_commit + ':' + relative(manifest)], cwd=ROOT)
        assert hashlib.sha256(committed).hexdigest() == sha(manifest), 'Artifact commit differs'
    candidate = bounded(release['candidate_report'])
    assert sha(candidate) == release['candidate_report_sha256'], 'Frozen package report changed'
    pending = []
    for channel, entry in release['apks'].items():
        version = '1.24.0-sandbox-rc2' if channel == 'lab' else RELEASE
        source = bounded(entry['path'])
        assert entry['version'] == version and entry['code'] == 33 and entry['path'] == 'releases/luma-drift-' + version + '.apk' and entry['signer_certificate_sha256'] == SIGNER, 'Unexpected channel identity'
        assert source.stat().st_size == entry['bytes'] and sha(source) == entry['sha256'], 'Exact APK bytes differ'
        target = PUBLIC / entry['path']
        assert subprocess.run(['git', 'check-ignore', '--quiet', relative(target)], cwd=ROOT).returncode == 0, 'Physical APK must be ignored'
        assert not subprocess.check_output(['git', 'ls-files', '--', relative(target)], cwd=ROOT).strip(), 'APK cannot be a Git blob'
        assert not target.exists() or sha(target) == entry['sha256'], 'Never overwrite earlier APK bytes'
        pending.append((source, target))
    assert set(release['apks']) == {'production', 'lab'}, 'Both exact channels required'
    capture_map = bounded(args.capture_map)
    proof, images, frozen = validate_captures(json.loads(capture_map.read_text()), release)
    pending += images
    frozen.update({relative(manifest): sha(manifest), relative(candidate): sha(candidate), relative(capture_map): sha(capture_map), relative(TEMPLATE): sha(TEMPLATE), BASELINE: sha(ROOT / BASELINE)})
    template = TEMPLATE.read_text()
    current = (PUBLIC / 'index.html').read_text()
    final = page(template, release)
    for key in ('companions', 'sound'):
        assert section(current, key) == section(final, key), 'Existing gallery/audio markup changed'
    preserved = {name.lstrip('/'): row['sha256'] for name, row in baseline['files'].items() if name != '/index.html'}
    for name, digest in preserved.items():
        assert sha(PUBLIC / name) == digest, 'Existing public payload changed: ' + name
    for name in ('prepare-site-v124.py', 'check-site-v124.py', 'deploy-site-v124.py', 'stage-site-v124-rest.py', 'collect-site-v124-public-bytes.py', 'test-deploy-site-v124.py', 'test-stage-site-v124-rest.py', 'test-collect-site-v124-public-bytes.py', 'test-prepare-site-v124.py', 'apk-byte-delta.py', 'deploy-cognitive-overlay.py'):
        frozen['scripts/' + name] = sha(bounded('scripts/' + name))
    for folder in (PUBLIC / 'assets/art-v124', PUBLIC / 'lab/clarity-124'):
        assert folder.is_dir(), 'Required new public content missing'
        for path in folder.rglob('*'):
            if path.is_file():
                frozen[relative(path)] = sha(path)
    for asset in ('site-v124.js', 'site-v124.css'):
        path = PUBLIC / 'assets' / asset
        frozen[relative(path)] = sha(path)
    evidence = {'status': 'PASS', 'edition': '1.24', 'release': RELEASE, 'artifact_manifest': relative(manifest), 'artifact_manifest_sha256': sha(manifest), 'artifact_source_commit': args.artifact_commit, 'production_apk_sha256': release['apks']['production']['sha256'], 'lab_apk_sha256': release['apks']['lab']['sha256'], 'package_identity_verified': True, 'native_capture_provenance_verified': True, 'screen_keys': SCREEN_KEYS, 'screenshots': proof, 'preserved_files': preserved, 'retired_files': {}, 'intentional_legal_updates': [], 'gallery_markup_sha256': hashlib.sha256(section(current, 'companions').encode()).hexdigest(), 'audio_markup_sha256': hashlib.sha256(section(current, 'sound').encode()).hexdigest(), 'frozen_inputs': frozen, 'firebase_config_sha256': sha(ROOT / 'firebase.json'), 'network_verified': False, 'deployed': False, 'previous_source_commit': PREVIOUS123, 'previous_deployment_source_commit': PREVIOUS123_DEPLOY_SOURCE, 'baseline_public_tree': PREVIOUS123_PUBLIC_TREE}
    # Qualify the complete planned public map before any publication-source mutation.
    planned = {'/' + str(path.relative_to(PUBLIC)): {'sha256': sha(path), 'bytes': path.stat().st_size} for path in sorted(PUBLIC.rglob('*')) if path.is_file() and not any(part.startswith('.') or part == 'tests' for part in path.relative_to(PUBLIC).parts)}
    for source, target in pending:
        planned['/' + str(target.relative_to(PUBLIC))] = {'sha256': sha(source), 'bytes': source.stat().st_size}
    planned['/index.html'] = {'sha256': hashlib.sha256(final.encode()).hexdigest(), 'bytes': len(final.encode())}
    planned['/releases/' + manifest.name] = {'sha256': sha(manifest), 'bytes': manifest.stat().st_size}
    manifest_target = PUBLIC / 'releases' / manifest.name
    assert not manifest_target.exists() or sha(manifest_target) == sha(manifest), 'Never replace earlier manifest bytes'
    evidence['files'] = planned
    namespace = {'__file__': str(ROOT / 'scripts/deploy-site-v124.py'), '__name__': 'prepared_overlay_gate124'}
    exec(compile((ROOT / 'scripts/deploy-site-v124.py').read_bytes(), namespace['__file__'], 'exec'), namespace)
    namespace['deployment_baseline'](evidence)
    if args.preview:
        STAGING.mkdir(parents=True, exist_ok=True)
        (STAGING / 'index.final.preview.html').write_text(final)
        files = {'/': relative(STAGING / 'index.final.preview.html'), '/index.html': relative(STAGING / 'index.final.preview.html'), '/releases/manifest-1.24.0-rc2.json': relative(manifest)}
        files.update({'/' + str(target.relative_to(PUBLIC)): relative(source) for source, target in pending})
        (STAGING / 'final-preview-files.json').write_text(json.dumps({'edition': '1.24', 'scope': 'Exact local preview; not applied or deployed', 'production_apk_sha256': evidence['production_apk_sha256'], 'files': files}, indent=2) + '\n')
    if args.write:
        for source, target in pending:
            target.parent.mkdir(parents=True, exist_ok=True)
            if not target.exists():
                shutil.copyfile(source, target)
        manifest_target = PUBLIC / 'releases' / manifest.name
        assert not manifest_target.exists() or sha(manifest_target) == sha(manifest), 'Never replace earlier manifest bytes'
        manifest_target.write_bytes(manifest.read_bytes())
        (PUBLIC / 'index.html').write_text(final)
        evidence['files'] = {'/' + str(path.relative_to(PUBLIC)): {'sha256': sha(path), 'bytes': path.stat().st_size} for path in sorted(PUBLIC.rglob('*')) if path.is_file() and not any(part.startswith('.') or part == 'tests' for part in path.relative_to(PUBLIC).parts)}
        assert evidence['files'] == planned, 'Applied public bytes differ from the fully qualified plan'
        (EVIDENCE / 'release').mkdir(parents=True, exist_ok=True)
        (EVIDENCE / 'release/source-manifest.json').write_text(json.dumps(evidence, indent=2) + '\n')
        (EVIDENCE / 'preparation.json').write_text(json.dumps({key: value for key, value in evidence.items() if key != 'files'}, indent=2) + '\n')
    print(json.dumps({'passed': True, 'edition': '1.24', 'screenshots': len(proof), 'written': args.write, 'previewWritten': args.preview, 'artifactCommit': args.artifact_commit, 'deployed': False}))


if __name__ == '__main__':
    main()
