#!/usr/bin/env python3
"""Actually complete pinned Journey1 at zero actions, without a synthetic save.

Uses four real Undo cycles then the certified five-piece tutorial route. No
private preference edits, ad, purchase or physical-device interaction.
"""
import argparse
import importlib.util
import time
from pathlib import Path
s = importlib.util.spec_from_file_location('move_smoke', Path(__file__).with_name('smoke-move-power-ui.py'))
m = importlib.util.module_from_spec(s)
s.loader.exec_module(m)
p = argparse.ArgumentParser(); p.add_argument('--serial',required=True); a=p.parse_args()
v=m.MovePowerSmoke(a.serial,report='docs/validation/v1.3/exact-zero-ui.txt')
try:
    v.launch(); v.click('Shop',v.home()); before=v.game_balance(); v.home()
    v.start_campaign(1)
    assert v.moves()==(13,13)
    for _ in range(4):
        v.tap(v.positions()[(0,1)])
        v.click('↶  Undo')
    assert v.moves()==(5,13)
    for row,col in [(0,1),(1,4),(4,3),(3,0),(2,2)]:
        v.tap(v.positions()[(row,col)])
    time.sleep(.7)
    results=[m.BASE.VICTORY.fullmatch(m.BASE.text(n))for n in v.nodes()]
    matches=[x for x in results if x]
    assert len(matches)==1
    stars,coins,xp=map(int,matches[0].groups())
    assert stars==2
    assert all(m.BASE.text(n)!='Restart · Free'for n in v.nodes())
    v.screenshot('exact-zero-win')
    v.click('Journey');v.click('Shop')
    after=v.game_balance();assert after==before+coins
    v.log('PASS actual Journey1: four real exit/Undo cycles then five certified exits; last action wins at exactly0, no exhaustion offer')
    v.log(f'PASS no fixture/save edits: {stars} stars (4 Undos), {coins} earned coins, {xp} XP, wallet {before}->{after}')
    v.home();assert 'FATAL EXCEPTION'not in v.shell('logcat','-d','-b','crash')
    v.log('PASS English Home, crash buffer clean; no ad, purchase or recipient selected')
finally:
    v.report_path.write_text('\n'.join(v.messages)+'\n')
