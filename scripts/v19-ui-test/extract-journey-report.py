#!/usr/bin/env python3
"""Extract individual native Journey checks without changing the raw suite result."""
from pathlib import Path
import argparse,csv,hashlib,json,re

ROOT=Path(__file__).resolve().parents[2]
PATTERNS={
    'cleared':r'Journey ([0-9]+) cleared by certified real UI actions, without profile injection',
    'three_stars_visible':r'Journey ([0-9]+) perfect action path visibly awards three stars',
    'within_certified_budget':r'Journey ([0-9]+) exact productive path stays within its certified allowance',
}

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--label',required=True);parser.add_argument('--through',type=int,default=100);args=parser.parse_args()
    assert re.fullmatch(r'[a-zA-Z0-9_-]{1,40}',args.label) and 1<=args.through<=1000
    folder=ROOT/'docs/validation/v1.9'/('ui-sdk-'+args.label);source=folder/'report.json';raw=json.loads(source.read_text());host=json.loads((folder/'host-report.json').read_text())
    rows={number:{'level':number} for number in range(1,args.through+1)}
    for check in raw['checks']:
        for kind,pattern in PATTERNS.items():
            match=re.fullmatch(pattern,check['message'])
            if not match:continue
            number=int(match.group(1))
            if number not in rows:continue
            assert kind not in rows[number],'Duplicate individual Journey check'
            rows[number][kind]={'passed':check['pass'],'native_message':check['message']}
    assert all(all(row.get(kind,{}).get('passed') is True for kind in PATTERNS) for row in rows.values()),'Every requested level needs all three actual passed native checks'
    result={
        'scope':'Individual actual SDK Journey clear, visible grade and budget checks extracted from the unchanged raw report. This does not relabel the whole suite as passing or measure human play.',
        'source_report':str(source.relative_to(ROOT)),
        'source_report_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),
        'source_suite_passed':raw['passed'],
        'installed_apk_sha256':host['apk_sha256'],
        'source_profile_restored':host['restored'],
        'first_level':1,'last_level':args.through,'individual_assertions_passed':3*args.through,
        'levels':list(rows.values()),
    }
    path=folder/('journey-'+str(args.through)+'-individual.json');path.write_text(json.dumps(result,indent=2)+'\n')
    with path.with_suffix('.csv').open('w',newline='') as output:
        writer=csv.writer(output);writer.writerow(['level',*PATTERNS]);writer.writerows([number,*[rows[number][kind]['passed'] for kind in PATTERNS]] for number in rows)
    print(json.dumps({key:result[key] for key in ['source_suite_passed','installed_apk_sha256','source_profile_restored','individual_assertions_passed']},indent=2))

if __name__=='__main__':main()
