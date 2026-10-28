#!/usr/bin/env python3
"""Offline U authoring. Freeze deterministic exit-only boards and exact prefix-choice metrics."""
import csv
import functools
import json
from pathlib import Path
import random
import re
import sys
import time

ROOT = Path(__file__).resolve().parents[1]
DIRS = ((-1, 0), (0, 1), (1, 0), (0, -1))

def on_ray(tile, row, col):
    r,c,d,_=tile;dr,dc=DIRS[d]
    return (row==r and (col-c)*dc>0) if dr==0 else (col==c and (row-r)*dr>0)

def analyse(size,gates,tiles,walls,order):
    count=len(tiles);allmask=(1<<count)-1;block=[]
    for i,t in enumerate(tiles):
        blocked=sum(1<<j for j,o in enumerate(tiles) if i!=j and on_ray(t,o[0],o[1]))
        for r,c,key in walls:
            if on_ray(t,r,c):blocked|=1<<key
        block.append(blocked)
    def legal(mask,state,i):return mask&(1<<i) and state&(1<<tiles[i][2]) and not mask&block[i]
    @functools.lru_cache(None)
    def viable(mask,state):
        if not mask:return True
        for i in order:
            if legal(mask,state,i) and viable(mask&~(1<<i),state^tiles[i][3]):return True
        return False
    mask=allmask;state=gates;decisions=forced=0;initial=sum(bool(legal(mask,state,i)) for i in order)
    for expected in order:
        moves=[i for i in order if legal(mask,state,i)]
        if expected not in moves:raise ValueError("Invalid certificate")
        safe=sum(viable(mask&~(1<<i),state^tiles[i][3]) for i in moves)
        if len(moves)>safe and safe:decisions+=1
        if safe==1:forced+=1
        mask&=~(1<<expected);state^=tiles[expected][3]
    @functools.lru_cache(None)
    def depth(i):return 1+max((depth(j) for j in range(count) if block[i]&(1<<j)),default=0)
    dependency=max(depth(i) for i in range(count))
    switches=sum(bool(t[3]) for t in tiles);paired=sum(bin(t[3]).count("1")>1 for t in tiles)
    score=decisions*8+dependency*2+switches*2+paired*3
    return (decisions,forced,0,dependency,score),initial,viable.cache_info().currsize

def encode(size,gates,tiles,walls,order,metrics,lesson,difficulty,beat):
    return "|".join(map(str,(size,gates,len(tiles),3,";".join(",".join(map(str,t)) for t in tiles),";".join(",".join(map(str,w)) for w in walls),",".join(map(str,order)),*metrics,difficulty,beat,lesson,0)))

def generate(number,attempt):
    rng=random.Random(0x5531313500000000+number*1009+attempt*9176)
    pos=(number-31)%10
    hard=pos in (2,5,7,8)
    base=min(15,6+(number-31)//90)
    count=min(16,max(5,base+(1 if hard else -1 if pos==9 else 0)))
    size=5 if number<=300 else 6
    placed=[];occupied=set()
    for i in range(count-1,-1,-1):
        options=[]
        for r in range(size):
            for c in range(size):
                if (r,c) in occupied:continue
                for d in range(4):
                    t=(r,c,d,0)
                    if any(on_ray(t,o[0],o[1]) for _,o in placed):continue
                    dependencies=sum(on_ray(o,r,c) for _,o in placed)
                    options.append((rng.random()*5+dependencies*.8,(r,c,d,0)))
        if not options:return None
        t=max(options)[1];placed.append((i,t));occupied.add(t[:2])
    tiles=[t for _,t in sorted(placed)];post=15
    probability=.32 if number<=100 else .4 if number<=300 else .48
    if hard:probability+=.12
    for i in range(count-1,-1,-1):
        r,c,d,_=tiles[i]
        options=[mask for mask in range(16) if bin(mask).count("1")<= (2 if number>=61 else 1) and (post^mask)&(1<<d)]
        nonzero=[m for m in options if m]
        mask=rng.choice(nonzero) if nonzero and (0 not in options or i<count-1 and rng.random()<probability) else 0
        tiles[i]=(r,c,d,mask);post^=mask
    gates=post;walls=[]
    key_target=0 if number%3==1 else 2 if number>400 and hard else 1
    for _ in range(key_target):
        options=[]
        for r in range(size):
            for c in range(size):
                if (r,c) in occupied:continue
                affected=[i for i,t in enumerate(tiles) if on_ray(t,r,c)]
                if affected and min(affected)>0:
                    for key in range(min(affected)):options.append((r,c,key))
        if not options:break
        wall=rng.choice(options);walls.append(wall);occupied.add(wall[:2])
    order=list(range(count));metrics,initial,cached=analyse(size,gates,tiles,walls,order)
    switches=sum(bool(t[3]) for t in tiles)
    if switches<2 or initial<2 or metrics[0]<1:return None
    target_decisions=1 if not hard else 2 if number<=150 else 3
    if metrics[0]<target_decisions:return None
    score_target=(24 if number<100 else 32 if number<300 else 40 if number<600 else 48)+(20 if hard else -8 if pos==9 else 0)
    cost=abs(metrics[4]-score_target)+max(0,initial-5)*3
    lesson="relay_key" if walls else "relay_order"
    difficulty=2 if metrics[4]>=100 else 1 if metrics[4]>=60 else 0
    beat=3 if pos==9 else 2 if difficulty else 1
    return cost,(size,gates,tiles,walls,order,metrics,lesson,difficulty,beat),cached

def main():
    started=time.monotonic();rows=[];report=[];seen=set();maximum_cache=0
    old=(ROOT/"app/src/main/java/com/androidhit/lumadrift/engine/LearningLayouts.java").read_text()
    source=re.findall(r'^\s*"([^"\n]+)"',old,re.M)
    # Preserve familiar movement/key lessons, replace every standalone gate-button lesson.
    teaching=list(range(1,13))+list(range(17,31))
    for number,old_number in enumerate(teaching,1):
        r=source[old_number-1].split("|");tiles=[tuple(map(int,t.split(','))) for t in r[4].split(';')];walls=[tuple(map(int,t.split(','))) for t in r[5].split(';') if t]
        order=[int(i) for i in r[6].split(',')]
        if any(i>=64 for i in order):raise ValueError("Teaching source has a manual gate")
        lesson="exits" if number<=4 else "blockers" if number<=8 else "key" if number<=12 else "relay" if number<=16 else "relay_order" if number<=20 else "relay_key" if number<=24 else "relay_mastery"
        metrics,initial,cached=analyse(int(r[0]),int(r[1]),tiles,walls,order)
        beat=0 if number in (1,5,9,13,17,21,25) else 1
        rows.append(encode(int(r[0]),int(r[1]),tiles,walls,order,metrics,lesson,0,beat))
    for number in range(27,1001):
        best=None
        for attempt in range(150):
            candidate=generate(number,attempt)
            if candidate is None:continue
            cost,values,cached=candidate
            size,gates,tiles,walls,order,metrics,lesson,difficulty,beat=values
            fingerprint=(size,gates,tuple(tiles),tuple(walls))
            if fingerprint in seen:continue
            if best is None or cost<best[0]:best=(cost,values,cached,fingerprint)
            if cost<=3 and attempt>=8:break
        if best is None:raise ValueError(f"No candidate U{number}")
        _,values,cached,fingerprint=best;seen.add(fingerprint);maximum_cache=max(maximum_cache,cached)
        size,gates,tiles,walls,order,metrics,lesson,difficulty,beat=values
        if number<=30:difficulty=0;lesson="relay_mastery";beat=1
        rows.append(encode(size,gates,tiles,walls,order,metrics,lesson,difficulty,beat))
        if number%100==0:print(f"Frozen U1–U{number}; authoring cache peak {maximum_cache}",flush=True)
    for number,row in enumerate(rows,1):
        r=row.split('|');report.append(["U",number,r[14],r[0],len(r[4].split(';')),r[1],r[7],r[8],r[10],r[11],r[12],r[13]])
    java='package com.androidhit.lumadrift.engine;\n\n/** Frozen U Journey, authored offline; no generator, difficulty adaptation or manual gate actions at runtime. */\nfinal class RelayLayouts {\n    private RelayLayouts() {}\n    static final String[] JOURNEY = {\n'+',\n'.join('        "'+row+'"' for row in rows)+'\n    };\n}\n'
    (ROOT/'app/src/main/java/com/androidhit/lumadrift/engine/RelayLayouts.java').write_text(java)
    target=ROOT/'docs/validation/v1.15/relay-engine';target.mkdir(parents=True,exist_ok=True)
    with (target/'authoring.csv').open('w') as out:
        writer=csv.writer(out);writer.writerow(['family','number','lesson','size','pieces','initial_gates','decision_points','forced_safe_steps','dependency_depth','structural_score','difficulty','beat']);writer.writerows(report)
    summary={'family':'U','boards':len(rows),'manual_gate_actions':0,'teaching_boards':30,'authoring_seconds':round(time.monotonic()-started,3),'cache_peak_authoring_states':maximum_cache,'seed_scheme':'0x5531313500000000 + number*1009 + attempt*9176','note':'Metrics are exact consequences on one certified solution prefix, not human difficulty measurements.'}
    (target/'authoring.json').write_text(json.dumps(summary,indent=2)+'\n');print(json.dumps(summary),flush=True)

if __name__=='__main__':main()
