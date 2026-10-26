#!/usr/bin/env python3
"""Check final Lab surfaces on two emulator display sizes without purchases.

Changes only emulator display overrides, restores them in finally, preserves
wallet/save/ad choices, and refuses physical-device serials through the harness.
"""
import argparse
import importlib.util
import time
from pathlib import Path

spec = importlib.util.spec_from_file_location('lab_collection', Path(__file__).with_name('smoke-collection-events.py'))
m = importlib.util.module_from_spec(spec)
spec.loader.exec_module(m)
a = argparse.ArgumentParser()
a.add_argument('--serial', required=True)
args = a.parse_args()
v = m.CollectionEventsSmoke(args.serial)
report = v.root / 'docs/validation/v1.3/layout-ui.txt'


def original_override(kind):
    lines = v.shell('wm', kind).splitlines()
    return next((line.split(':', 1)[1].strip() for line in lines if line.startswith('Override ')), 'reset')


size, density = original_override('size'), original_override('density')
try:
    v.shell('am', 'start', '-n', m.PACKAGE + '/' + m.MAIN)
    v.home()
    v.click('Collection')
    before = v.balance()
    v.home()
    for dimensions in ('720x1280', '1280x720'):
        v.shell('wm', 'size', dimensions)
        v.shell('wm', 'density', '320')
        time.sleep(.6)
        v.width, v.height = 0, 0
        v.home()
        v.click('Collection')
        for page in range(3):
            v.frame_bounds()
            v.screenshot('v13-layout-' + dimensions + '-collection-' + str(page + 1))
            if page < 2:
                v.click('Next')
        v.click('Inky, ', prefix=True)
        v.frame_bounds()
        v.screenshot('v13-layout-' + dimensions + '-preview')
        v.click('Close')
        v.click('Events')
        for tab in ('Daily Drift', 'Weekly Voyage'):
            v.click(tab)
            labels = [m.BASE.node_text(n) for n in v.nodes()]
            assert any('stages' in text for text in labels), labels
            assert any('Resets in' in text for text in labels), labels
            v.frame_bounds()
            v.screenshot('v13-layout-' + dimensions + '-' + ('daily' if tab == 'Daily Drift' else 'weekly'))
        v.home()
        v.click('Shop')
        for page in range(2):
            v.frame_bounds()
            v.screenshot('v13-layout-' + dimensions + '-shop-' + str(page+1))
            # The commerce action must finish above the bottom navigation.
            nodes = v.nodes()
            import re
            commerce = next(n for n in nodes if m.BASE.node_text(n) == 'Gold & rewards · Test')
            nav = next(n for n in nodes if m.BASE.node_text(n) == 'Home')
            cb = list(map(int,re.findall(r'-?\d+',commerce.get('bounds'))))
            nb = list(map(int,re.findall(r'-?\d+',nav.get('bounds'))))
            assert cb[3] <= nb[1], 'Commerce action overlaps navigation'
            if page == 0:
                v.click('Next')
        v.home()
        v.click('Continue', prefix=True)
        v.frame_bounds()
        v.screenshot('v13-layout-' + dimensions + '-play')
        labels = [m.BASE.node_text(n) for n in v.nodes()]
        assert any('moves' in label for label in labels)
        assert any(label.startswith('Trail') for label in labels)
        v.home()
        v.click('Settings')
        v.click('Account')
        labels = [m.BASE.node_text(n) for n in v.nodes()]
        assert 'Playing as a guest' in labels
        for attempt in range(6):
            if 'Sign in with Google' in labels:
                break
            v.shell('input', 'swipe', str(v.width//2), str(int(v.height*.75)), str(v.width//2), str(int(v.height*.3)), '450')
            labels = [m.BASE.node_text(n) for n in v.nodes()]
        assert 'Sign in with Google' in labels
        v.frame_bounds()
        v.screenshot('v13-layout-' + dimensions + '-account')
        v.shell('input', 'keyevent', '4')
        v.shell('input', 'keyevent', '4')
        v.home()
        v.click('Collection')
        assert v.balance() == before
        v.home()
        v.log('PASS ' + dimensions + ': three collection pages, cancelled preview, daily/weekly objective/countdown, two shop pages without nav overlap, six-power play HUD, guest account; controls in frame, wallet unchanged')
    assert 'FATAL EXCEPTION' not in v.shell('logcat', '-d', '-b', 'crash')
    v.log('PASS final Lab1.3 small portrait/landscape smoke; no ad/payment/account selection, crash buffer clean')
finally:
    v.shell('wm', 'size', size)
    v.shell('wm', 'density', density)
    time.sleep(.5)
    v.width, v.height = 0, 0
    v.home()
    report.write_text('\n'.join(v.report) + '\n')
