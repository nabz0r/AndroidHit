#!/usr/bin/env python3
"""Compare all original1.17 preference values with a real1.18 launch snapshot."""
from pathlib import Path
import argparse, hashlib, json

def digest(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--baseline',required=True,type=Path)
    p.add_argument('--baseline-sha256',required=True)
    p.add_argument('--candidate',required=True,type=Path)
    p.add_argument('--output',required=True,type=Path)
    a=p.parse_args()
    assert digest(a.baseline)==a.baseline_sha256,'Exact original baseline hash required'
    assert not a.output.exists(),'Migration evidence must be new'
    before=json.loads(a.baseline.read_text());after=json.loads(a.candidate.read_text())
    differences=[];added=[]
    for pref,values in before.items():
        observed=after.get(pref,{})
        for key,value in values.items():
            if observed.get(key)!=value:differences.append({'pref':pref,'key':key,'before':value,'after':observed.get(key)})
        for key in observed.keys()-values.keys():
            allowed=pref=='luma_drift_save_v1' and key.startswith(('expedition_v118_','companion_copies_v118_'))
            added.append({'pref':pref,'key':key,'allowed_new_v118_key':allowed})
            if not allowed:differences.append({'pref':pref,'key':key,'before':'ABSENT','after':observed[key]})
    newprefs=[pref for pref in after if pref not in before]
    for pref in newprefs:
        if pref!='luma_expedition_session_v118':differences.append({'new_preference_file':pref})
    result={'passed':not differences,'baseline_sha256':digest(a.baseline),'candidate_sha256':digest(a.candidate),'original_value_count':sum(len(x) for x in before.values()),'differences':differences,'new_keys':added,'new_preference_files':newprefs,'scope':'All original observed game/meta/session values, seeds excluded, compared after an actual first1.18 Activity launch; only explicitlynewv118 copy/run keys and isolatedexpeditionsessionmayappear.'}
    a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
    if not result['passed']:raise SystemExit(1)

if __name__=='__main__':main()
