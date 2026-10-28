#!/usr/bin/env python3
"""Reject palette-tone indices at direct Canvas color boundaries, offline.

Complements the native screenshot sample; does not infer rendered contrast or layout.
"""
from pathlib import Path
import json,re,argparse
ROOT=Path(__file__).resolve().parents[1]
COLOR_ARGUMENTS={'round':(6,), 'stroke':(3,), 'text':(5,), 'center':(5,), 'fit':(6,), 'wrap':(6,), 'icon':(5,), 'star':(4,), 'alpha':(0,), 'blend':(0,1)}
TONE=r'SweetUi\.(?:PEACH|MINT|LILAC|CREAM)\b'
CONVERTED=re.compile(r'SweetUi\.(?:light|fill|ink)\(\s*'+TONE+r'\s*\)')
def mask(source):
    # Keep offsets/newlines while excluding comments and quoted literal contents.
    return re.sub(r'//[^\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'',lambda m:''.join('\n' if c=='\n' else ' ' for c in m.group()),source)
def violations(source):
    clean=mask(source);out=[]
    for call in re.finditer(r'(?<![\w.])('+'|'.join(COLOR_ARGUMENTS)+r')\s*\(',clean):
        name=call.group(1);start=call.end();parts=[];last=start;depth=0;end=start
        for end in range(start,len(clean)):
            ch=clean[end]
            if ch in '([{':depth+=1
            elif ch in ')]}':
                if ch==')' and depth==0:parts.append(clean[last:end]);break
                depth-=1
            elif ch==',' and depth==0:parts.append(clean[last:end]);last=end+1
        for index in COLOR_ARGUMENTS[name]:
            if index<len(parts) and re.search(TONE,CONVERTED.sub('',parts[index])):
                out.append({'line':source.count('\n',0,call.start())+1,'function':name,'argument_index':index})
    return out

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--report',default='docs/validation/v1.10/palette-boundary.json');args=parser.parse_args()
    bad=['round(c,1,2,3,4,5,SweetUi.MINT);','fit(c,s,x,y,12,100,on?SweetUi.MINT:skin.card,true,true);','alpha(SweetUi.LILAC, 255);']
    good=['round(c,1,2,3,4,5,SweetUi.light(SweetUi.MINT));','fit(c,s,x,y,12,100,on?SweetUi.fill(SweetUi.MINT):skin.card,true,true);','sweet.surface(c,rect,SweetUi.MINT,true);','text(c,"SweetUi.MINT",1,2,3,0xff000000,true);']
    assert all(violations(s) for s in bad) and all(not violations(s) for s in good)
    source=ROOT/'app/src/main/java/com/androidhit/lumadrift/LumaView.java';current=violations(source.read_text());prior=ROOT/'docs/validation/v1.10/rc1-source/LumaView.java';observed=violations(prior.read_text())
    assert len(observed)==7,observed
    report={'passed':not current,'scope':'Source API-type boundary invariant; native observed pixel samples are separate. Tone indices are accepted by SweetUi surfaces/progress; Canvas color wrappers require converted ARGB.','self_tests':len(bad)+len(good),'retained_rc1_reproduced_violations':observed,'current_violations':current}
    path=ROOT/args.report;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2));assert not current
if __name__=='__main__':main()
