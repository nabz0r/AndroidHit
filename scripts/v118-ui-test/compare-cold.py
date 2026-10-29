#!/usr/bin/env python3
"""Require a real new native process and exact persisted state at its initial observation."""
from pathlib import Path
import argparse, hashlib, json

def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--checkpoint',required=True,type=Path)
    p.add_argument('--checkpoint-report',required=True,type=Path)
    p.add_argument('--next-before',required=True,type=Path)
    p.add_argument('--next-report',required=True,type=Path)
    p.add_argument('--output',required=True,type=Path)
    a=p.parse_args();assert not a.output.exists(),'Cold comparison evidence must be new'
    first=json.loads(a.checkpoint_report.read_text());second=json.loads(a.next_report.read_text())
    before=json.loads(a.checkpoint.read_text());after=json.loads(a.next_before.read_text())
    passed=first['passed'] and second['passed'] and first['native']['processId']!=second['native']['processId'] and first['run_token']!=second['run_token'] and before==after
    result={'passed':passed,'process_ids':[first['native']['processId'],second['native']['processId']],'distinct_process':first['native']['processId']!=second['native']['processId'],'complete_observed_state_equal':before==after,'checkpoint_sha256':sha(a.checkpoint),'next_before_sha256':sha(a.next_before),'scope':'Exact durable game/meta/originalcampaign/isolatedexpedition snapshots observed before any new Activity launch in a genuinelydistinctforced-stopnativeprocess; bothphaseexecutionsmustpass.'}
    a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
    if not passed:raise SystemExit(1)

if __name__=='__main__':main()
