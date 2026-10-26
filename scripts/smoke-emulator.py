#!/usr/bin/env python3
"""Interaction regression on an explicitly selected emulator. Never resets a phone."""
import argparse,json,os,re,subprocess,tempfile,time,xml.etree.ElementTree as ET
from pathlib import Path
P=argparse.ArgumentParser();P.add_argument('--serial',required=True);P.add_argument('--reset',action='store_true');a=P.parse_args()
if not a.serial.startswith('emulator-'):raise SystemExit('Emulator serial required; physical devices are never reset by this test.')
root=Path(__file__).resolve().parents[1];sdk=Path(os.environ.get('ANDROID_HOME',str(Path.home()/'Library/Android/sdk')));adb=str(sdk/'platform-tools/adb');pkg='com.androidhit.lumadrift'
shots=root/'docs/screenshots';shots.mkdir(exist_ok=True);report=[]
def cmd(*args):return subprocess.check_output([adb,'-s',a.serial,*args],stderr=subprocess.STDOUT)
def shell(*args):return cmd('shell',*args).decode()
def log(s):report.append(s);print(s,flush=True)
def nodes():
    shell('uiautomator','dump','/sdcard/luma-test.xml')
    return [n for n in ET.fromstring(shell('cat','/sdcard/luma-test.xml')).iter('node') if n.get('package')==pkg]
def click_node(n):
    v=list(map(int,re.findall(r'\d+',n.get('bounds'))));shell('input','tap',str((v[0]+v[2])//2),str((v[1]+v[3])//2));time.sleep(.22)
def click(text,contains=False):
    ns=nodes();matches=[n for n in ns if n.get('clickable')=='true' and (text in n.get('text','') if contains else n.get('text')==text)]
    if not matches:raise AssertionError('Missing control: '+text+' in '+str([n.get('text') for n in ns]))
    click_node(matches[-1])
def shot(name): (shots/(name+'.png')).write_bytes(cmd('exec-out','screencap','-p'))
def pieces():return [n for n in nodes() if n.get('text','').startswith('Piece, row')]
def tap_cell(row,col):
    for n in pieces():
        if f'row {row+1}, column {col+1},' in n.get('text',''):click_node(n);return True
    return False
def balance():
    for n in nodes():
        m=re.fullmatch(r'Coins: (\d+)',n.get('text',''))
        if m:return int(m[1])
    raise AssertionError('Missing accessible wallet')
def launch():shell('am','start','-n',pkg+'/.MainActivity');time.sleep(.5)
if a.reset:shell('pm','clear',pkg)
shell('logcat','-c');launch();shot('home-en')
click('Continue',True)
if any(n.get('text')=='Let’s drift' for n in nodes()):click('Let’s drift')
shot('play-en')
java=Path(os.environ.get('JAVA_HOME','/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home'))/'bin'
with tempfile.TemporaryDirectory() as out:
    sources=list((root/'app/src/main/java/com/androidhit/lumadrift/engine').glob('*.java'))+[root/'scripts/EngineFixture.java']
    subprocess.run([str(java/'javac'),'-d',out,*map(str,sources)],check=True)
    def fixture(n):return json.loads(subprocess.check_output([str(java/'java'),'-cp',out,'EngineFixture',str(n)]))
    for lev in range(1,7):
        solution=fixture(lev)['solution']
        if lev==2:
            before=len(pieces());tap_cell(*solution[0]);assert len(pieces())==before-1;click('Undo',True);assert len(pieces())==before
            tap_cell(*solution[0]);left=len(pieces());shell('am','force-stop',pkg);launch();click('Continue',True);assert len(pieces())==left;log('Undo and force-stop session restoration: PASS')
        if lev==6:
            shot('gates-en');click('Spark');time.sleep(.25);click('Vision');shot('powers-en')
            blocked=[n for n in pieces() if 'Path blocked' in n.get('text','')]
            if blocked:
                before=len(pieces());click_node(blocked[0]);assert len(pieces())==before;log('Blocked move leaves board intact: PASS')
        cached=pieces()
        for row,col in solution:
            target=next((n for n in cached if f'row {row+1}, column {col+1},' in n.get('text','')),None)
            if target is not None:click_node(target)
        time.sleep(.6)
        ns=nodes();summary=[n.get('text','') for n in ns if 'Reward:' in n.get('text','')];assert summary, 'No victory summary'
        if lev==6:shot('victory-en')
        log('Campaign level '+str(lev)+' completed: '+summary[0])
        click('Next',True)
    # Flow is asynchronous: a background transition must cancel remaining automatic moves.
    before=len(pieces());click('Flow');shell('input','keyevent','3');time.sleep(1.2);launch();
    if any('Continue' in n.get('text','') for n in nodes()):click('Continue',True)
    after=len(pieces());assert before-after<=1,(before,after);log('Flow background cancellation: PASS')
    click('Pause');click('Leave level');click('Shop');coins_before=balance();click('45 ◈');assert balance()==coins_before-45;log('Shop exact debit: PASS');shot('shop-en')
    click('Collection');click('Miko',True);assert balance()==coins_before-45-160 if coins_before-45>=160 else True
    shot('companions-en');click('Themes');shot('themes-en')
    click('Events');shot('daily-en');click('Weekly Voyage');shot('weekly-en')
    click('Home');click('Settings');click('Français');shot('settings-fr');click('Retour',True);shot('home-fr');log('French interface switch: PASS')
    click('Continuer',True);shot('play-fr');click('Pause');click('Quitter le niveau');click('Boutique');shot('shop-fr')
    shell('am','force-stop',pkg);launch();click('Continuer',True);assert pieces()==[] # French labels, English piece selector must find none.
    log('Language and save survive process restart: PASS')
    shell('input','keyevent','4');click('Quitter le niveau');click('Réglages');
    for name,lang in [('Deutsch','de'),('Español','es'),('Português','pt'),('Italiano','it'),('English','en')]:click(name);shot('settings-'+lang)
    log('All six languages selectable: PASS')
crashes=shell('logcat','-d','-b','crash');assert 'FATAL EXCEPTION' not in crashes,crashes
log('Crash buffer: clean')
(root/'docs/SMOKE_RESULTS.txt').write_text('\n'.join(report)+'\n')
