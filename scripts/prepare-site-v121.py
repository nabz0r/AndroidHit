#!/usr/bin/env python3
"""Prepare the 1.21 Firebase homepage from exact signed APK metadata and accepted native captures; never deploy."""
import argparse
import hashlib
import html
import json
from pathlib import Path
import re
import shutil
import struct
import subprocess

ROOT=Path(__file__).resolve().parents[1]
PUBLIC=ROOT/'public'
EVIDENCE=ROOT/'docs/validation/site-v1.21'
STAGING=EVIDENCE/'staging'
SIGNER='dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'

def sha(path):
    digest=hashlib.sha256()
    with path.open('rb') as source:
        for chunk in iter(lambda:source.read(1024*1024),b''):digest.update(chunk)
    return digest.hexdigest()
def bounded(name):
    path=Path(name);path=path if path.is_absolute() else ROOT/path;path=path.resolve()
    assert ROOT in path.parents and path.is_file(),'Missing bounded input: '+str(name)
    return path

def payloads():return {str(path.relative_to(PUBLIC)):path for path in PUBLIC.rglob('*') if path.is_file() and not any(part.startswith('.') or part=='tests' for part in path.relative_to(PUBLIC).parts)}
def esc(value):return html.escape(str(value),quote=True)
def w(fr,en):return '<span data-lang="fr" lang="fr">'+fr+'</span><span data-lang="en" lang="en" hidden>'+en+'</span>'
def picture(key,fr,en,eager=False):
    base='assets/screenshots/v1.21/'+key
    return f'<img src="{base}-fr.png" data-screenshot-fr="{base}-fr.png" data-screenshot-en="{base}-en.png" alt="{esc(fr)}" data-alt-fr="{esc(fr)}" data-alt-en="{esc(en)}" width="1080" height="2400" decoding="async" '+('fetchpriority="high"' if eager else 'loading="lazy"')+'>'
def section(source,id):
    match=re.search(r'<section id="'+re.escape(id)+r'"(?=\s|>).*?</section>',source,re.S)
    assert match,'Existing preserved section missing: '+id
    return match.group(0)

def page(release,old):
    prod=release['apks']['production'];lab=release['apks']['lab'];version=prod['version'];download='/releases/'+Path(prod['path']).name
    button=f'<a class="button primary" href="{download}">'+w('Télécharger sur Android','Download for Android')+' <span aria-hidden="true">↓</span></a>'
    demo='<a class="button simulation-link" href="lab/ui-1-21/index.html">'+w('Essayer la simulation','Try the simulation')+' <span aria-hidden="true">↗</span></a>'
    head=re.search(r'<head>(.*?)</head>',old,re.S).group(1)
    description=('Luma Drift 1.21 : ton avatar au premier plan, des talents de puzzle, une carte vivante et des évolutions lisibles. Essaie la simulation, puis la candidate Android.','Luma Drift 1.21: your avatar in the spotlight, puzzle skills, a living map and clear evolution. Try the simulation, then the Android candidate.')
    head=re.sub(r'<meta name="description"[^>]*>',f'<meta name="description" content="{esc(description[0])}" data-content-fr="{esc(description[0])}" data-content-en="{esc(description[1])}">',head)
    title_fr='Luma Drift 1.21 — Ton compagnon. Ta prochaine aventure.';title_en='Luma Drift 1.21 — Your companion. Your next adventure.'
    head=re.sub(r'<meta property="og:title"[^>]*>',f'<meta property="og:title" content="{esc(title_fr)}" data-content-fr="{esc(title_fr)}" data-content-en="{esc(title_en)}">',head)
    head=re.sub(r'<meta property="og:image"[^>]*>','<meta property="og:image" content="https://luma-drift-production.web.app/assets/screenshots/v1.21/home-fr.png">',head)
    head=re.sub(r'<title>.*?</title>',f'<title>{esc(title_fr)}</title>',head)
    head=re.sub(r'assets/site-v(?:118|119|120|121)\.js\?v=[a-f0-9]+','assets/site-v121.js?v='+sha(STAGING/'site-v121.js')[:16],head)
    head=re.sub(r'<link rel="stylesheet" href="assets/site-v(?:119|120|121)\.css[^>]*>','',head)
    head+='<link rel="stylesheet" href="assets/site-v121.css?v='+sha(STAGING/'site-v121.css')[:16]+'">'
    header=re.search(r'<header\b.*?</header>',old,re.S).group(0)
    header=re.sub(r'<nav aria-label="Navigation">.*?</nav>','<nav aria-label="Navigation"><a href="#little-world">'+w('Le jeu','The game')+'</a><a href="lab/ui-1-21/index.html">'+w('Simulation','Simulation')+'</a><a href="#download">Android</a></nav>',header,flags=re.S)
    screen_copy=[('home','Ton avatar au premier plan','Your avatar in the spotlight','Un grand compagnon, ton XP réel et l’accès direct à la maison, au salon, à la cuisine et au jardin.','A full-body companion, real player XP and direct access to your home, living room, kitchen and garden.'),('shop','Un choix avant la découverte','A choice before discovery','Des objets illustrés, des prix en Coins et les probabilités affichées avant tout tirage.','Illustrated items, Coin prices and visible odds before a draw.'),('map','Une route à parcourir','A journey to explore','Des archipels, dix zones et une progression dessinée autour de ton prochain puzzle.','Islands, ten zones and a route built around your next puzzle.'),('puzzle','Des gestes qui font réagir le monde','A world that reacts to your moves','Flèches plus claires, talents d’équipe et éléments à dégager sur les nouvelles parties éligibles.','Clearer arrows, squad skills and elemental shells on eligible new attempts.')]
    screens=''.join('<figure><div class="phone">'+picture(key,fr+' · capture Android 1.21',en+' · Android 1.21 capture')+'</div><figcaption><h3>'+w(fr,en)+'</h3><p>'+w(desc,eng)+'</p></figcaption></figure>' for key,fr,en,desc,eng in screen_copy)
    footer=re.search(r'<footer\b.*?</footer>',old,re.S).group(0)
    size=w(f"{prod['bytes']/1e6:.2f}".replace('.',',')+' Mo',f"{prod['bytes']/1e6:.2f} MB")
    return f'''<!doctype html><html lang="fr"><head>{head}</head><body class="landing-v118 landing-v121" data-edition="1.21"><a class="skip" href="#main">{w('Aller au contenu','Skip to content')}</a>{header}<main id="main">
<section class="hero wrap" aria-labelledby="hero-title"><div class="hero-copy"><p class="eyebrow">LUMA DRIFT · 1.21</p><h1 id="hero-title">{w('Ton compagnon.<br><em>Ta prochaine aventure.</em>','Your companion.<br><em>Your next adventure.</em>')}</h1><p class="lead">{w('Un avatar qui prend sa place. Une équipe dont les talents servent dans le puzzle. Des îles à explorer, des formes à découvrir et un petit chez-toi à retrouver. La boucle devient visible : joue, progresse et choisis ce qui te plaît.','An avatar in the spotlight. A squad whose skills help inside the puzzle. Islands to explore, forms to discover and a little home to return to. See the loop: play, grow and choose what you love.')}</p><div class="actions">{button}{demo}</div><p class="download-note">{esc(version)} · {size} · Android 8+</p><p class="fineprint">{w('Candidate à essayer · Annonces de test · Achats réels indisponibles','Playtest candidate · Test ads · Real purchases unavailable')}</p><div class="hero-note">{w('Ton style. Tes talents. Des progrès que tu peux expliquer.','Your style. Your skills. Progress you can understand.')}</div></div><figure class="hero-visual"><div class="hero-disc" aria-hidden="true"></div><div class="phone">{picture('home','Vrai accueil Android 1.21 : avatar, progression et accès à la maison','Actual Android 1.21 home: avatar, progression and home access',True)}</div><figcaption class="capture-label">{w('Capture Android native non retouchée','Unedited native Android capture')}</figcaption></figure></section>
<div class="facts wrap"><div><strong>1–3</strong>{w('compagnons dans ton équipe','companions in your squad')}</div><div><strong>6</strong>{w('familles de talents','skill families')}</div><div><strong>4</strong>{w('formes par compagnon','forms per companion')}</div><div><strong>10</strong>{w('zones sur ta route','zones along your journey')}</div></div>
<section id="little-world" class="section wrap" aria-labelledby="little-world-title"><div class="section-heading"><div><p class="eyebrow">{w('CHAQUE CHOSE A SON RÔLE','EVERYTHING HAS A ROLE')}</p><h2 id="little-world-title">{w('Du style.<br><em>Et du sens.</em>','Personality.<br><em>And purpose.</em>')}</h2></div><p class="section-lead">{w('L’avatar montre ton style. L’équipe apporte ses talents. L’XP suit tes premières victoires. Les copies font évoluer les compagnons. Chaque progression garde un rôle clair.','Your avatar expresses your style. Your squad brings puzzle skills. XP tracks first wins. Copies evolve companions. Every progress track has a clear purpose.')}</p></div><div class="feature-strip"><article><span class="feature-mark">✦</span><h3>{w('Ton compagnon en grand','Your companion, centre stage')}</h3><p>{w('Une scène lumineuse montre le compagnon équipé, sa rareté, son niveau et ses copies. La fiche Évolution montre les quatre formes réelles et explique son talent. Les tenues changent le look, sans avantage caché de puzzle.','A luminous stage shows your equipped companion, rarity, level and copies. Evolution displays the four actual forms and explains its skill. Outfits change the look without a hidden puzzle advantage.')}</p></article><article><span class="feature-mark">↗</span><h3>{w('Une aventure qui réagit','An adventure that reacts')}</h3><p>{w('La carte traverse dix zones dessinées. Les décors de puzzle prennent vie, les éléments glace, feu et eau demandent des taps gratuits et trois sorties manuelles distinctes sans correction donnent un Focus gratuit, une fois par essai.','The map crosses ten illustrated zones. Puzzle backdrops come alive, ice, fire and water shells clear with free taps, and three distinct manual exits without correction earn one free Focus per attempt.')}</p></article><article><span class="feature-mark">⌂</span><h3>{w('Ton monde à portée de main','Your world close at hand')}</h3><p>{w('Depuis l’accueil, retrouve directement la maison, le salon, la cuisine et le jardin. Déplace ton compagnon, aménage chaque pièce et reviens à l’aventure avec ton équipe.','From Home, jump straight to your house, living room, kitchen or garden. Move your companion, decorate each room and return to the adventure with your squad.')}</p></article></div><p class="role-footnote">{w('Les éléments concernent les nouvelles parties U à partir du niveau 11 et les expéditions éligibles. Les sauvegardes déjà en cours gardent leur état. Les mouvements et les sons se règlent séparément.','Elements apply to eligible new U Journey attempts from level 11 and expeditions. Existing runs keep their state. Motion and audio have separate settings.')}</p></section>
<section id="progression" class="section wrap" aria-labelledby="progression-title"><div class="section-heading"><div><p class="eyebrow">{w('JOUE → PROGRESSE → CHOISIS','PLAY → GROW → CHOOSE')}</p><h2 id="progression-title">{w('Tu sais ce que<br><em>tu gagnes.</em>','Know what<br><em>you earn.</em>')}</h2></div><p class="section-lead">{w('L’économie ne doit pas devenir une énigme de plus. La fiche Progression relie les soldes à leurs usages et les compteurs s’animent uniquement lorsque les valeurs changent réellement.','The economy should not become another puzzle. Progression links each balance to its purpose, and counters animate only when the actual value changes.')}</p></div><div class="progression-grid"><article><b>LumaCoins</b><p>{w('Gagnés en jouant et en récupérant les cadeaux prêts. Dépensés en bonus, tenues ou rencontres aléatoires à probabilités fixes. Les prix et l’aperçu précèdent la confirmation.','Earned in play and from ready gifts. Spend on boosts, outfits or random arrivals with fixed odds. Prices and previews come before confirmation.')}</p></article><article><b>{w('XP du joueur','Player XP')}</b><p>{w('Une première réussite donne de l’XP. Chaque tranche de 120 augmente le niveau joueur. Rejouer un plateau déjà récompensé ne redonne pas cette XP.','A first clear earns XP. Every 120 raises player level. Replaying a previously rewarded board does not grant that XP again.')}</p></article><article><b>{w('Copies et formes','Copies and forms')}</b><p>{w('Les copies font monter le compagnon. Ses formes changent aux niveaux 1, 3, 6 et 10. Les talents de puzzle gardent leurs effets indiqués. Une expédition peut viser une copie précise.','Copies raise companion level. Forms change at levels 1, 3, 6 and 10. Puzzle skills retain their stated effects. Expeditions can target a specific copy.')}</p></article><article><b>LumaGold</b><p>{w('Monnaie du compte séparée des Coins. Un solde inconnu reste indisponible. Les achats réels sont fermés dans cette candidate ; les soldes de test Lab restent isolés.','Account currency separate from Coins. An unknown balance remains unavailable. Real purchases are closed in this candidate; Lab test balances stay isolated.')}</p></article></div><p class="wallet-note">{w('Énergie de session : un plafond qui progresse de 10 à 16, +1 toutes les 10 minutes. Agrandir le plafond ne remplit pas le compteur. Un nouvel essai éligible coûte 1 ; reprendre le même essai est gratuit. La Charge des talents reste distincte. La recharge de 10 pour 50 Gold est réservée aux tests Lab, à zéro énergie.','Session energy: capacity grows from 10 to 16, +1 every 10 minutes. A larger capacity does not fill the counter. An eligible new attempt costs 1; resuming the same attempt is free. Talent Charge stays separate. The 10-energy refill for 50 Gold is limited to Lab tests at zero energy.')}</p><p class="wallet-note">{w('Les niveaux U1–10 conservent leurs récompenses et prix de base. Une courbe logarithmique porte les premières récompenses jusqu’à ×2 au niveau U1000, et les prix de base des bonus jusqu’à ×1,4 avant la remise d’avatar. Deux Undo sont gratuits par nouvel essai ; ensuite, le prix en Coins dépend du niveau et du nombre de retours payants, avec un plafond de 40, affiché et confirmé avant chaque retour. Le coup annulé reste compté.','U1–10 keep their base rewards and prices. A logarithmic curve takes first-clear rewards up to ×2 at U1000 and base boost prices up to ×1.4 before the avatar discount. Two Undos are free per new attempt; later Undo Coin prices depend on level and paid-Undo count, capped at 40, shown and confirmed before each Undo. The original move stays counted.')}</p><p class="role-footnote">{w('Les probabilités de rencontre sont fixes. Un doublon ne rapproche pas d’un résultat rare. Progression et Coins restent sur cet appareil ; la connexion Google ne synchronise pas la sauvegarde.','Arrival odds are fixed. A duplicate does not bring a rare result closer. Progress and Coins stay on this device; Google sign-in does not synchronize the save.')}</p></section>
<section id="simulation" class="section wrap simulation-section" aria-labelledby="simulation-title"><div><p class="eyebrow">{w('À ESSAYER DANS TON NAVIGATEUR','TRY IT IN YOUR BROWSER')}</p><h2 id="simulation-title">{w('Fais un tour.<br><em>Comprends la boucle.</em>','Take a turn.<br><em>See the loop.</em>')}</h2><p class="lead">{w('Dégage la glace, utilise Guidage, charge un Focus et vois les Coins et l’XP avancer. Compare les probabilités de rencontre, explore les courbes avec le curseur de niveau ou simule une copie ciblée pour révéler une nouvelle forme de Nova.','Clear ice, use Guide, earn a Focus and watch Coins and XP move. Compare arrival odds, explore the curves with the level slider or simulate a targeted copy to reveal a new Nova form.')}</p><div class="actions">{demo}</div></div><div class="simulation-explainer"><b>{w('Une simulation indépendante','An independent simulation')}</b><p>{w('Mini puzzle et état fictif réinitialisable. Les dessins et seuils d’évolution viennent du jeu ; les gains de 45 Coins / 35 XP servent à expliquer. Aucun paiement, compte ou fichier Android n’est modifié.','A resettable mini puzzle with fictional state. Artwork and evolution thresholds come from the game; 45 Coins / 35 XP rewards teach the loop. No payment, account or Android file is changed.')}</p></div></section>
<section id="screens" class="section wrap" aria-labelledby="screens-title"><div class="section-heading"><div><p class="eyebrow">{w('VU SUR ANDROID','SEEN ON ANDROID')}</p><h2 id="screens-title">{w('De l’avatar<br><em>à l’aventure.</em>','From avatar<br><em>to adventure.</em>')}</h2></div><p class="section-lead">{w('Accueil, boutique, carte et puzzle : quatre vues natives de cette candidate exacte, en français et en anglais.','Home, boutique, map and puzzle: four native views from this exact candidate, in French and English.')}</p></div><div class="native-grid">{screens}</div><p class="role-footnote">{w('Captures Android natives non retouchées. Les contrôles techniques et simulations ne mesurent ni le plaisir, ni la rétention, ni le confort sur tous les téléphones.','Unedited native Android captures. Technical checks and simulations do not measure enjoyment, retention or comfort on every phone.')}</p></section>
{section(old,'companions')}{section(old,'sound')}
<section id="download" class="section wrap download-section" aria-labelledby="download-title"><div><p class="eyebrow">{w('À TOI DE JOUER','YOUR TURN TO PLAY')}</p><h2 id="download-title">{w('La prochaine île<br><em>t’attend.</em>','Your next island<br><em>is waiting.</em>')}</h2><div class="actions">{button}</div><p class="download-note">{esc(version)} · code 29 · {prod['bytes']:,} {w('octets','bytes')}</p></div><div class="release-information"><p>{w('Distribution directe en APK. Les annonces restent en mode test ; les services et transactions réels ont leur qualification séparée. Aucune publication Play n’est impliquée.','Direct APK distribution. Ads remain in test mode; live services and transactions require separate qualification. No Play publication is implied.')}</p><details><summary>{w('Fichiers et empreintes','Files and checksums')}</summary><p>APK SHA-256</p><code>{prod['sha256']}</code><p><a href="/releases/manifest-{esc(version)}.json">{w('Manifeste de cette candidate','Candidate manifest')}</a></p><p><a href="/releases/{esc(Path(lab['path']).name)}">{w('Variante de test séparée','Separate test variant')}</a></p><code>{lab['sha256']}</code><p><a href="/releases/luma-drift-1.20.0-rc1.apk">{w('Archive Android 1.20','Android 1.20 archive')}</a> · <a href="/releases/luma-drift-1.19.0-prototype2.apk">{w('Archive Android 1.19','Android 1.19 archive')}</a></p></details></div></section></main>{footer}</body></html>'''

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--artifact-manifest',type=Path,default=ROOT/'releases/manifest-1.21.0-rc1.json')
    parser.add_argument('--capture-map',type=Path,required=True,help='JSON list: key, locale, source, report; keys home/shop/map/puzzle × fr/en')
    parser.add_argument('--artifact-commit',help='Immutable Git commit containing the exact artifact metadata')
    parser.add_argument('--preview',action='store_true',help='Write an exact local preview page and source URL map in staging only')
    parser.add_argument('--write',action='store_true',help='Copy verified files and write homepage/source manifest; never deploy')
    args=parser.parse_args();manifest=bounded(args.artifact_manifest);release=json.loads(manifest.read_text())
    assert release['edition']=='1.21' and release['version']=='1.21.0-rc1' and release['version_code']==29
    assert args.artifact_commit is None or re.fullmatch('[a-f0-9]{40}',args.artifact_commit)
    candidate=bounded(release['candidate_report']);assert sha(candidate)==release['candidate_report_sha256'],'Frozen package report changed'
    pending=[(STAGING/'site-v121.js',PUBLIC/'assets/site-v121.js'),(STAGING/'site-v121.css',PUBLIC/'assets/site-v121.css')]
    demo_files=sorted(path for path in (STAGING/'lab/ui-1-21').rglob('*') if path.is_file())
    assert {path.name for path in demo_files}=={'index.html','simulation.css','simulation.mjs','simulation-core.mjs'},'Incomplete reviewed simulation payload'
    pending.extend((path,PUBLIC/'lab/ui-1-21'/path.relative_to(STAGING/'lab/ui-1-21')) for path in demo_files)
    for channel in ('production','lab'):
        entry=release['apks'][channel];source=bounded(entry['path']);assert sha(source)==entry['sha256'] and source.stat().st_size==entry['bytes'] and entry['code']==29 and entry['signer_certificate_sha256']==SIGNER,'Wrong exact signed APK metadata'
        target=PUBLIC/'releases'/source.name;relative=str(target.relative_to(ROOT))
        assert subprocess.run(['git','check-ignore','--quiet',relative],cwd=ROOT).returncode==0,'Physical external APK must be ignored'
        if target.exists():assert sha(target)==entry['sha256'] and target.stat().st_size==entry['bytes'],'Never replace earlier download bytes'
        pending.append((source,target))
    capture_map=bounded(args.capture_map);captures=json.loads(capture_map.read_text());assert isinstance(captures,list) and len(captures)==8,'Exactly eight current home/shop/map/puzzle captures required'
    seen=set();proof=[];frozen={str(manifest.relative_to(ROOT)):sha(manifest),str(capture_map.relative_to(ROOT)):sha(capture_map)}
    for capture in captures:
        key,locale=capture['key'],capture['locale'];assert key in ('home','shop','map','puzzle') and locale in ('fr','en') and (key,locale) not in seen;seen.add((key,locale))
        source=bounded(capture['source']);report_path=bounded(capture['report']);report=json.loads(report_path.read_text());assert report['passed'] and report['native']['passed'] and report['apk_sha256']==report['installed_sha256']==release['apks']['production']['sha256'],'Capture is not accepted exact-candidate native evidence'
        assert report['expected_version']==release['version'] and report['expected_code']==29 and report['package']=='com.androidhit.lumadrift','Native capture package/version identity differs'
        relative=str(source.relative_to(report_path.parent));digest=sha(source);assert report['evidence_sha256'].get(relative)==digest,'Native report does not bind capture bytes'
        with source.open('rb') as image:header=image.read(24)
        assert header[:8]==b'\x89PNG\r\n\x1a\n' and struct.unpack('>II',header[16:24])==(1080,2400),'Expected original full native PNG'
        target=PUBLIC/f'assets/screenshots/v1.21/{key}-{locale}.png';pending.append((source,target))
        proof.append({'key':key,'locale':locale,'source':str(source.relative_to(ROOT)),'public':str(target.relative_to(ROOT)),'sha256':digest,'report':str(report_path.relative_to(ROOT)),'reportSha256':sha(report_path),'apkSha256':report['apk_sha256'],'screenshotsEdited':False})
        frozen[str(report_path.relative_to(ROOT))]=sha(report_path);frozen[str(source.relative_to(ROOT))]=digest
    old=(PUBLIC/'index.html').read_text();gallery=section(old,'companions');audio=section(old,'sound')
    preserved={name:sha(path) for name,path in payloads().items() if name!='index.html' and not name.startswith('assets/screenshots/v1.21/') and not name.startswith('assets/site-v121.') and not name.startswith('lab/ui-1-21/') and '1.21' not in name}
    new_page=page(release,old);assert gallery in new_page and audio in new_page,'Existing gallery/audio markup changed'
    frozen[str(candidate.relative_to(ROOT))]=sha(candidate)
    legal_updates=[]
    legal_record=STAGING/'legal-update-final.json'
    if legal_record.exists():
        legal=json.loads(legal_record.read_text())
        previous=legal['source_commit'];target=bounded(legal['original_public_path']);staged=bounded(legal['staged_path'])
        assert re.fullmatch('[a-f0-9]{40}',previous) and target==PUBLIC/'privacy/index.html'
        original=subprocess.check_output(['git','show',previous+':'+legal['original_public_path']],cwd=ROOT)
        assert hashlib.sha256(original).hexdigest()==legal['original_sha256'],'Prior legal source differs'
        assert sha(target)==sha(staged)==legal['staged_sha256'],'Reviewed legal update must be explicitly applied before homepage preparation'
        frozen[str(legal_record.relative_to(ROOT))]=sha(legal_record)
        frozen[str(staged.relative_to(ROOT))]=sha(staged)
        legal_updates.append({'public_path':str(target.relative_to(ROOT)),'previous_source_commit':previous,'previous_sha256':legal['original_sha256'],'current_sha256':sha(target),'review_record':str(legal_record.relative_to(ROOT)),'scope':'Intentional privacy update applied before homepage generation; account/auth/deletion files are preserved. This is not a deployment receipt.'})
    for asset in ('site-v121.js','site-v121.css'):
        path=STAGING/asset;frozen[str(path.relative_to(ROOT))]=sha(path)
    for path in demo_files:frozen[str(path.relative_to(ROOT))]=sha(path)
    fixture=STAGING/'native-economy-fixture.json';frozen[str(fixture.relative_to(ROOT))]=sha(fixture)
    for name in ('prepare-site-v121.py','package-release-v121.py','check-site-v121.py','deploy-site-v121.py','verify-site-v121-publication.py','test-simulation-v121.mjs','test-site-v121-runtime.mjs'):
        path=ROOT/'scripts'/name;frozen[str(path.relative_to(ROOT))]=sha(path)
    evidence={'status':'PASS','edition':'1.21','release':release['version'],'artifact_manifest':str(manifest.relative_to(ROOT)),'artifact_manifest_sha256':sha(manifest),'artifact_source_commit':args.artifact_commit,'production_apk_sha256':release['apks']['production']['sha256'],'lab_apk_sha256':release['apks']['lab']['sha256'],'package_identity_verified':True,'native_capture_provenance_verified':True,'screenshots':proof,'preserved_files':preserved,'intentional_legal_updates':legal_updates,'gallery_markup_sha256':hashlib.sha256(gallery.encode()).hexdigest(),'audio_markup_sha256':hashlib.sha256(audio.encode()).hexdigest(),'frozen_inputs':frozen,'firebase_config_sha256':sha(ROOT/'firebase.json'),'network_verified':False,'deployed':False}
    if args.preview:
        preview_page=STAGING/'index.preview.html';preview_page.write_text(new_page)
        preview_files={'/':str(preview_page.relative_to(ROOT)),'/index.html':str(preview_page.relative_to(ROOT))}
        preview_files.update({'/'+str(target.relative_to(PUBLIC)):str(source.relative_to(ROOT)) for source,target in pending})
        preview_files['/releases/'+manifest.name]=str(manifest.relative_to(ROOT))
        (STAGING/'preview-files.json').write_text(json.dumps({'edition':'1.21','scope':'Exact candidate local preview; not a public source application, release approval or deployment.','production_apk_sha256':release['apks']['production']['sha256'],'files':preview_files},indent=2)+'\n')
    if args.write:
        EVIDENCE.mkdir(parents=True,exist_ok=True)
        for source,target in pending:
            target.parent.mkdir(parents=True,exist_ok=True)
            if not target.exists() or sha(target)!=sha(source):shutil.copyfile(source,target)
        (PUBLIC/'releases'/manifest.name).write_bytes(manifest.read_bytes());(PUBLIC/'index.html').write_text(new_page)
        assert all(sha(PUBLIC/name)==digest for name,digest in preserved.items()),'Existing gallery/legal/account/audio/archive payload changed'
        evidence['files']={'/'+name:{'bytes':path.stat().st_size,'sha256':sha(path)} for name,path in sorted(payloads().items())}
        (EVIDENCE/'release').mkdir(exist_ok=True);(EVIDENCE/'release/source-manifest.json').write_text(json.dumps(evidence,indent=2)+'\n')
        (EVIDENCE/'preparation.json').write_text(json.dumps({k:v for k,v in evidence.items() if k!='files'},indent=2)+'\n')
    print(json.dumps({'passed':True,'written':args.write,'preview_written':args.preview,'edition':'1.21','screenshots':len(proof),'gallery_preserved':True,'account_files_preserved':True,'intentional_privacy_updates':len(legal_updates),'artifact_commit':args.artifact_commit,'deployed':False}))

if __name__=='__main__':main()
