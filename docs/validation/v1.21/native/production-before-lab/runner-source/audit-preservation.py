#!/usr/bin/env python3
"""Compare read-only pre-launch preferences with the preserved earned production-F profile."""
from pathlib import Path
import argparse,hashlib,json
HERE=Path(__file__).resolve().parent
PRIOR='e4fa1a70dbb2e842c9a16a9f22d0f2a49a4316ccbc6013fe0acfa233093f5d26'
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--observed',required=True,type=Path);p.add_argument('--out',required=True,type=Path);p.add_argument('--baseline',type=Path,default=HERE/'assets/baseline-production-f.json');p.add_argument('--prior-sha256',default=PRIOR);args=p.parse_args()
assert not args.out.exists(),'Preserve every failed or accepted audit'
base=args.baseline;expected=json.loads(base.read_text());observed=json.loads(args.observed.read_text())
differences=[]
for preference,values in expected.items():
 actual=observed.get(preference,{})
 for key in sorted(set(values)|set(actual)):
  if key not in values or key not in actual or values.get(key)!=actual.get(key):differences.append({'preference':preference,'key':key,'expected':values.get(key),'observed':actual.get(key)})
report={'passed':not differences,'prior_installed_sha256':args.prior_sha256,'baseline_sha256':hashlib.sha256(base.read_bytes()).hexdigest(),'observed':str(args.observed),'observed_sha256':hashlib.sha256(args.observed.read_bytes()).hexdigest(),'differences':differences,'scope':'Read-only full six-map comparison before first new Activity launch. Seed fields remain absent from both snapshots; no profile writes.'}
args.out.parent.mkdir(parents=True,exist_ok=True);args.out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n');print(json.dumps({'passed':report['passed'],'differences':len(differences),'out':str(args.out)}));assert report['passed'],'Earned profile changed; inspect immutable differences'
