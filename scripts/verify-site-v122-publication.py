#!/usr/bin/env python3
"""Validate an explicitly supplied current public-origin receipt; this tool makes no origin requests."""
import argparse
import json
from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
ORIGIN='https://luma-drift-production.web.app'

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--receipt',type=Path);parser.add_argument('--commit');args=parser.parse_args()
    if args.receipt is None:parser.exit(2,'No public-origin request made. Local source/browser and authenticated Hosting management remain separate evidence. Supply a current authorized response-byte receipt for validation; this tool never bypasses an origin-access refusal.\n')
    assert args.commit and re.fullmatch('[a-f0-9]{40}',args.commit),'Exact source commit required'
    source=json.loads((ROOT/'docs/validation/site-v1.22/release/source-manifest.json').read_text());receipt=json.loads(args.receipt.read_text())
    assert receipt.get('origin')==ORIGIN and receipt.get('source_commit')==args.commit and receipt.get('status')=='PASS','Receipt origin/source identity differs'
    observed=receipt.get('files');assert isinstance(observed,dict) and observed,'An explicit response-byte map is required'
    assert set(observed)<=set(source['files']),'Receipt contains unknown source paths'
    for name,entry in observed.items():assert entry==source['files'][name],'Recorded response bytes differ from source: '+name
    print(json.dumps({'passed':True,'edition':'1.22','receiptValidated':True,'recordedResponsePaths':len(observed),'allSourcePayloadsRecorded':set(observed)==set(source['files']),'requestsMadeByThisTool':False,'renderingVerified':False,'scope':'Checks supplied response-byte identities. Does not independently fetch origin or establish rendering, Play approval or provider transactions.'}))

if __name__=='__main__':main()
