#!/usr/bin/env python3
"""Compare meaningful read-only run state around an actual force-stop and subsequent UI resume."""
from pathlib import Path
import argparse,json,hashlib
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--before',required=True,type=Path);p.add_argument('--after',required=True,type=Path);p.add_argument('--out',required=True,type=Path);p.add_argument('--keys',nargs='+',required=True);args=p.parse_args();assert not args.out.exists(),'Preserve failed or accepted cold evidence'
before=json.loads(args.before.read_text());after=json.loads(args.after.read_text());checks=[]
for item in args.keys:
 pref,key=item.split(':',1);a=before.get(pref,{});b=after.get(pref,{});checks.append({'preference':pref,'key':key,'passed':key in a and key in b and a[key]==b[key],'before':a.get(key),'after':b.get(key)})
report={'passed':all(c['passed'] for c in checks),'checks':checks,'before_sha256':hashlib.sha256(args.before.read_bytes()).hexdigest(),'after_sha256':hashlib.sha256(args.after.read_bytes()).hexdigest(),'scope':'Exact state equality across actual native process restart/resume. Elapsed-time fields are outside selected keys. No preferences written.'};args.out.parent.mkdir(parents=True,exist_ok=True);args.out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n');print(json.dumps({'passed':report['passed'],'checks':len(checks),'out':str(args.out)}));assert report['passed'],'Cold state changed; inspect exact fields'
