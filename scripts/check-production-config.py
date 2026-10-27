#!/usr/bin/env python3
"""Check public configuration and distinguish an internal candidate from live activation.

This is a local configuration check, not a Google approval or runtime test.
Use --require-live as a publication gate after real qualification evidence exists.
"""
import argparse
import json
import re
from urllib.parse import urlsplit
from pathlib import Path

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--require-live', action='store_true')
parser.add_argument('--evidence', help='JSON containing explicit manual qualification records; see config/activation.example.json')
parser.add_argument('--report', default='docs/validation/v1.4/production-readiness.json')
args = parser.parse_args()
identity = json.loads((root/'app/src/production/assets/identity-config.json').read_text())
config = (root/'app/src/main/java/com/androidhit/lumadrift/commerce/CommerceConfig.java').read_text()
compiled = root/'app/build/generated/source/buildConfig/production/release/com/androidhit/lumadrift/BuildConfig.java'
build = compiled.read_text() if compiled.exists() else ''
checks = {
    'firebase_project_matches': identity.get('projectId') == 'luma-drift-production',
    'firebase_app_matches': identity.get('appId') == '1:369472826910:android:dfb29318d5a24e71c3080f',
    'firebase_public_api_key_present': bool(re.fullmatch(r'AIza[A-Za-z0-9_-]{30,60}', identity.get('apiKey',''))),
    'oauth_web_client_present': identity.get('webClientId') == '369472826910-is1dc25hc6fdtbecfhf0h04o40n4us21.apps.googleusercontent.com',
    'owner_admob_app_present': 'ca-app-pub-6914185503996225~9670436379' in config,
    'three_owner_rewarded_units_present': all('ca-app-pub-6914185503996225/'+unit in config for unit in ['7013317595','7504272185','7093208640']),
    'production_variant_compiled': bool(build),
    'production_package': 'APPLICATION_ID = "com.androidhit.lumadrift";' in build,
    'sandbox_disabled': 'boolean MODE_SANDBOX = false;' in build,
}
blocked = []
if 'boolean FORCE_TEST_ADS = false;' not in build:
    blocked.append('Candidate deliberately uses sample creatives; live AdMob/UMP qualification and a new build are required.')
match = re.search(r'String WALLET_API_BASE_URL = "([^"]*)";', build)
origin_valid = False
if match:
    try:
        u = urlsplit(match.group(1))
        origin_valid = u.scheme == 'https' and bool(u.hostname) and not u.username and not u.password and u.port in (None, 443) and u.path in ('', '/') and not u.query and not u.fragment
    except ValueError:
        pass
if not origin_valid:
    blocked.append('Wallet origin is empty in this build; checkout remains blocked. Server deployment is a separate milestone.')
milestones = {
    'google_identity_and_deletion': 'This configuration check has no supplied record of actual Google login/deletion qualification; client IDs alone do not prove it.',
    'play_license_purchases': 'Supply qualification evidence for Play products, merchant profile, signing identity and license-test purchase lifecycle.',
    'wallet_deployment_and_recovery': 'Supply deployment and recovery/refund/RTDN qualification evidence for the cloud wallet; absence here does not mean no server has been deployed.',
    'public_privacy_and_deletion': 'Supply the public privacy/deletion qualification record; this script does not inspect hosting or infer that an existing deployment is absent.',
    'admob_and_store_approval': 'Supply console qualification evidence for AdMob consent, app/account approval, app-ads.txt and store declarations.',
}
manual = json.loads(Path(args.evidence).read_text()) if args.evidence else {}
manual_checks = {}
for key, description in milestones.items():
    record = manual.get(key, {})
    reference = record.get('evidence', '') if isinstance(record, dict) else ''
    # These are human-reviewed evidence references, never automatic Google approval.
    referenced = isinstance(reference, str) and bool(reference.strip()) and (reference.startswith('https://') or (root/reference).is_file())
    manual_checks[key] = isinstance(record, dict) and record.get('passed') is True and referenced
    if not manual_checks[key]: blocked.append(description)
result = {'scope':'Local configuration and recorded outstanding milestones only',
          'internal_candidate_configuration_valid':all(checks.values()),'live_activation_ready':all(checks.values()) and not blocked,
          'checks':checks,'manual_qualification_records':manual_checks,'live_blockers':blocked}
path = root/args.report
path.parent.mkdir(parents=True,exist_ok=True)
path.write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps(result,indent=2))
raise SystemExit(1 if not all(checks.values()) or args.require_live and not result['live_activation_ready'] else 0)
