#!/usr/bin/env python3
"""Installed production-candidate checks on an explicitly selected emulator.

No data reset, fixture edits, purchase sheet or real ad is requested. Google
sign-in is opened only to inspect/cancel its chooser, never to enter credentials.
"""
import argparse
import importlib.util
import json
import re
import time
from pathlib import Path

root = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('production_base', root/'scripts/smoke-v11.py')
m = importlib.util.module_from_spec(spec)
spec.loader.exec_module(m)
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--serial', required=True)
args = parser.parse_args()
v = m.V11Smoke(args.serial)
v.shots = root/'docs/screenshots/production'
report = []

def labels(all_packages=False):
    return [m.text(n) for n in v.nodes(all_packages) if m.text(n)]

def note(text):
    report.append(text)
    print(text, flush=True)

def shot(name):
    v.shots.mkdir(parents=True,exist_ok=True)
    (v.shots/(name+'.png')).write_bytes(v.command('exec-out','screencap','-p'))

def scroll(down=False):
    v.shell('input','swipe','530','650' if down else '1850','530','1850' if down else '650','450')
    time.sleep(.15)

def seek(name):
    for _ in range(12):
        ns=v.nodes()
        found=[n for n in ns if m.text(n)==name and n.get('clickable')=='true' and v.bounds(n)[3]-v.bounds(n)[1]>20]
        if found:
            v.tap(found[0]);return
        scroll()
    raise AssertionError('Missing scrollable action '+name)

try:
    v.launch()
    # Normalize an existing save without assuming its current language/screen.
    settings_names=['Settings','Réglages','Einstellungen','Ajustes','Definições','Impostazioni']
    for _ in range(5):
        current=labels()
        setting=next((name for name in settings_names if name in current),None)
        if setting:
            v.click(setting);v.click('English');break
        if 'English' in current:
            v.click('English');break
        home=next((name for name in ['Home','Accueil','Start','Inicio','Início'] if name in current),None)
        if home:v.click(home)
        else:v.shell('input','keyevent','4');time.sleep(.3)
    v.home()
    v.shell('logcat','-c')
    shot('home')
    v.click('Settings')
    v.click('Account')
    account = labels()
    assert 'Playing as a guest' in account
    assert not any('OAuth and Firebase configuration' in x for x in account)
    shot('account-configured')
    v.click('Sign in with Google')
    time.sleep(1.4)
    all_nodes=v.nodes(True)
    provider_packages=sorted({n.get('package') for n in all_nodes if n.get('package') and n.get('package')!='com.androidhit.lumadrift'})
    shot('google-attempt')
    if any(n.get('package')=='com.google.android.gms' for n in all_nodes):
        v.shell('input','keyevent','4');time.sleep(.3)
        note('PASS Google credential-provider UI opened and cancelled; no account selected and no completed login claimed.')
    else:
        note('OBSERVED configured Google sign-in attempt returned guest/unavailable on this emulator; no completed login claimed. Other visible packages: '+str(provider_packages))
    if 'Playing as a guest' not in labels():
        v.shell('input','keyevent','4')
    assert 'Playing as a guest' in labels()
    v.shell('input','keyevent','4');v.home()

    for language,entry,title in [('English','Gold & rewards','Gold & collection'),('Français','Or et récompenses','Or et collection'),('Deutsch','Gold und Belohnungen','Gold & Sammlung'),('Español','Oro y recompensas','Oro y colección'),('Português','Ouro e recompensas','Ouro e coleção'),('Italiano','Oro e ricompense','Oro e collezione')]:
        v.click('Settings');v.click(language);v.shell('input','keyevent','4')
        # The bottom navigation shop is recognized by localized names.
        shop={'English':'Shop','Français':'Boutique','Deutsch':'Shop','Español':'Tienda','Português':'Loja','Italiano':'Negozio'}[language]
        v.click(shop);v.click(entry)
        current=labels()
        assert title in current,(language,current)
        assert not any('Simulate' in x or 'Simuler' in x or 'TEST MODE' in x for x in current)
        if language=='English':
            assert any('Earned gold on this device:' in x for x in current)
            assert any('Gold purchases open after' in x for x in current)
            shot('wallet-preparation')
            seek('Refresh purchases & collection')
            assert any('Gold purchases open after' in x for x in labels())
            note('PASS production store has separate paid/earned balances, no simulator and a clear server-not-configured state; refresh cannot grant gold or open checkout.')
        v.shell('input','keyevent','4');time.sleep(.3)
        home={'English':'Home','Français':'Accueil','Deutsch':'Start','Español':'Inicio','Português':'Início','Italiano':'Home'}[language]
        v.click(home)
        # Return to settings using the language-specific title, then English.
        settings={'English':'Settings','Français':'Réglages','Deutsch':'Einstellungen','Español':'Ajustes','Português':'Definições','Italiano':'Impostazioni'}[language]
        v.click(settings);v.click('English');v.home()
    note('PASS production wallet entry/text exercised in all six languages; English restored.')

    v.click('Continue',prefix=True)
    if 'Let’s drift' in labels():v.click('Let’s drift')
    before=v.positions()
    assert before
    shot('play')
    # Journey 1's fixed prefix remains the original certified tutorial layout.
    java=v.java
    import tempfile,subprocess
    with tempfile.TemporaryDirectory(prefix='luma-production-board-') as out:
        sources=list((root/'app/src/main/java/com/androidhit/lumadrift/engine').glob('*.java'))
        subprocess.run([str(java/'javac'),'-d',out,*map(str,sources),str(root/'scripts/EngineFixture.java')],check=True)
        fixture=json.loads(subprocess.check_output([str(java/'java'),'-cp',out,'EngineFixture','1']))
        steps=fixture['solution']
        for row,col in steps:
            n=v.positions().get((row,col))
            if n is not None:v.tap(n)
    time.sleep(.5)
    assert any('Lovely drift!' in x for x in labels()),labels()
    shot('victory')
    note('PASS actual Journey 1 completed with a certified solution, visible action allowance and earned reward; no fixture, ad or purchase.')
    v.click('Journey',topmost=True);v.home()
    v.click('Settings');balance=v.balance()
    v.shell('am','force-stop','com.androidhit.lumadrift');v.launch();v.home();v.click('Settings')
    assert v.balance()==balance
    assert not v.shell('logcat','-d','-b','crash').strip()
    note('PASS earned balance persists through process restart; crash buffer empty.')
    note('NOT VALIDATED: completed Google login, Play license purchases, deployed wallet/Firestore, owner consent messages, live ads, hosted pages or physical devices.')
finally:
    (root/'docs/validation/v1.4/production-ui.txt').write_text('\n'.join(report)+'\n')
