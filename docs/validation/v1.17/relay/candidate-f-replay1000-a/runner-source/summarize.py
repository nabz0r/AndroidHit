#!/usr/bin/env python3
"""Require actual passing native U1-U1000 replay records on one exact signed final APK."""
from pathlib import Path
import argparse
import hashlib
import json

ROOT = Path(__file__).resolve().parents[2]
FIXTURES = ROOT / 'scripts/v115-relay-ui/assets/fixtures'


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--report', action='append', required=True, help='Passing host-report.json; repeat for continuous disjoint batches')
    parser.add_argument('--output', default='docs/validation/v1.17/relay/summary.json')
    args = parser.parse_args()
    paths = [Path(path) if Path(path).is_absolute() else ROOT / path for path in args.report]
    pairs = sorted([(path, json.loads(path.read_text())) for path in paths], key=lambda pair: pair[1]['from'])
    assert all(report.get('passed') and report['native'].get('passed') for _, report in pairs), 'Every contributing native batch must pass'
    reports = [report for _, report in pairs]
    assert len({report['apk_sha256'] for report in reports}) == 1, 'All batches require the identical final APK'
    assert len({(report['expected_version'], report['expected_code']) for report in reports}) == 1, 'Exact identical version/code across every batch'
    assert reports[0]['expected_version'].startswith('1.17.0-rc') and reports[0]['expected_code'] > 0
    assert all(report['installed_apk_sha256'] == report['installed_apk_sha256_after'] == report['apk_sha256'] for report in reports), 'Exact installed bytes before and after'
    assert all(report['fixture_manifest_sha256'] == sha(FIXTURES / 'manifest.json') for report in reports), 'Identical frozen certificate inventory'
    for path, report in pairs:
        native = path.parent / 'native'
        assert report['native']['runToken'] == report['run_token'], 'Execution tokens must match'
        for relative, digest in report['evidence_sha256'].items():
            assert sha(path.parent / relative) == digest, 'Evidence modified after report: ' + relative
        events = [json.loads(line) for line in (native / 'events.jsonl').read_text().splitlines() if line]
        touches = [event for event in events if event.get('input') == 'UiAutomation touchscreen']
        assert len(touches) == report['native']['touchCount']
        assert [event['touch'] for event in touches] == list(range(1, len(touches) + 1)), 'Continuous real gesture journal'
        assert all(event['bounds'][2] > event['bounds'][0] and event['bounds'][3] > event['bounds'][1] for event in touches)
        checks = [json.loads(line) for line in (native / 'checks.jsonl').read_text().splitlines() if line]
        assert len(checks) == report['native']['checkCount'] and all(check['passed'] for check in checks), 'Every native check must be recorded and pass'
        assert [check['index'] for check in checks] == list(range(1, len(checks) + 1)), 'Continuous check journal'
        assert (native / 'protected-before.json').read_bytes() == (native / 'protected-after.json').read_bytes(), 'Earned profile state must survive replay exactly'
    levels = [level for report in reports for level in report['native']['levels']]
    assert [level['number'] for level in levels] == list(range(1, 1001)), 'Require every U1-U1000 exactly once; no planned, missing or duplicate level'
    for level in levels:
        fixture = json.loads((FIXTURES / ('u' + str(level['number']) + '.json')).read_text())
        assert level['history'] == ','.join(str(step['action']) for step in fixture['steps']), 'Exact certificate history must be actually saved'
        assert level['certificateExits'] == fixture['pieces']
        assert level['initialStars'] == level['stars'] == 3, 'Retained three-star replay, not a fresh first-win run'
        assert level['coinDelta'] == 0, 'Replays must not duplicate first-win reward'
    captures = [path.parent / 'native' / (name + '.png') for path, report in pairs for name in report['native']['captures']]
    assert all(path.is_file() for path in captures), 'Every reported screenshot must exist'
    summary = {'passed': True, 'candidate_apk_sha256': reports[0]['apk_sha256'], 'version': reports[0]['expected_version'], 'code': reports[0]['expected_code'], 'fixture_manifest_sha256': sha(FIXTURES / 'manifest.json'), 'levels': 1000, 'actual_certificate_exits': sum(level['certificateExits'] for level in levels), 'extra_probe_exits': sum(level['extraProbeExits'] for level in levels), 'native_checks': sum(report['native']['checkCount'] for report in reports), 'actual_touch_gestures': sum(report['native']['touchCount'] for report in reports), 'native_original_pngs': len(captures), 'new_first_win_coins': sum(level['coinDelta'] for level in levels), 'reports': [{'path': str(path.relative_to(ROOT)), 'sha256': sha(path), 'observer_apk_sha256': report['test_apk_sha256'], 'runner_sha256': report['runner_sha256'], 'completed': report['native']['completed'], 'wall_seconds': report['wall_seconds'], 'timing': report['timing']} for path, report in pairs], 'scope': 'Every U1-U1000 replayed through actual Android SDK touchscreen input and real Next objects on the exact explicitly identified signed production 1.17 APK. The existing task QA profile had already earned all stars. Zero duplicate first-win coins, unchanged ownership/profile/house/power stock/settings and restored language are required. Certificates are host-frozen guidance; native action histories and gate states are observed. No app/engine hooks or state injection. Representative original screenshots supplement all-board target observations. This does not measure human enjoyment, audio quality or real-phone performance.'}
    destination = ROOT / args.output
    assert not destination.exists(), 'Preserve previous summary; choose a fresh output'
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(summary, indent=2, ensure_ascii=False) + '\n')
    print(json.dumps(summary, indent=2))


if __name__ == '__main__':
    main()
