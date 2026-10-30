#!/usr/bin/env python3
"""Prepare the 1.23 Firebase homepage from exact signed APK metadata and accepted native captures; never deploy."""
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
EVIDENCE=ROOT/'docs/validation/site-v1.23'
STAGING=EVIDENCE/'staging'
SCREEN_KEYS=('puzzle','skills','wardrobe','map')
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
    base='assets/screenshots/v1.23/'+key
    return f'<img src="{base}-fr.png" data-screenshot-fr="{base}-fr.png" data-screenshot-en="{base}-en.png" alt="{esc(fr)}" data-alt-fr="{esc(fr)}" data-alt-en="{esc(en)}" width="1080" height="2400" decoding="async" '+('fetchpriority="high"' if eager else 'loading="lazy"')+'>'
def section(source,id):
    match=re.search(r'<section id="'+re.escape(id)+r'"(?=\s|>).*?</section>',source,re.S)
    assert match,'Existing preserved section missing: '+id
    return match.group(0)

def page_original(release,old):
    prod=release['apks']['production'];lab=release['apks']['lab'];version=prod['version'];download='/releases/'+Path(prod['path']).name
    button=f'<a class="button primary" href="{download}">'+w('Télécharger sur Android','Download for Android')+' <span aria-hidden="true">↓</span></a>'
    demo='<a class="button simulation-link" href="lab/ui-1-21/index.html">'+w('Essayer la simulation','Try the simulation')+' <span aria-hidden="true">↗</span></a>'
    head=re.search(r'<head>(.*?)</head>',old,re.S).group(1)
    description=('Luma Drift 1.23 : ton avatar au premier plan, des talents de puzzle, une carte vivante et des évolutions lisibles. Essaie la simulation, puis la candidate Android.','Luma Drift 1.23: your avatar in the spotlight, puzzle skills, a living map and clear evolution. Try the simulation, then the Android candidate.')
    head=re.sub(r'<meta name="description"[^>]*>',f'<meta name="description" content="{esc(description[0])}" data-content-fr="{esc(description[0])}" data-content-en="{esc(description[1])}">',head)
    title_fr='Luma Drift 1.23 — Ton compagnon. Ta prochaine aventure.';title_en='Luma Drift 1.23 — Your companion. Your next adventure.'
    head=re.sub(r'<meta property="og:title"[^>]*>',f'<meta property="og:title" content="{esc(title_fr)}" data-content-fr="{esc(title_fr)}" data-content-en="{esc(title_en)}">',head)
    head=re.sub(r'<meta property="og:image"[^>]*>','<meta property="og:image" content="https://luma-drift-production.web.app/assets/screenshots/v1.23/map-fr.png">',head)
    head=re.sub(r'<title>.*?</title>',f'<title>{esc(title_fr)}</title>',head)
    head=re.sub(r'assets/site-v(?:118|119|120|121|122|123)\.js\?v=[a-f0-9]+','assets/site-v123.js?v='+sha(PUBLIC/'assets/site-v123.js')[:16],head)
    head=re.sub(r'<link rel="stylesheet" href="assets/site-v(?:119|120|121|122|123)\.css[^>]*>','',head)
    head+='<link rel="stylesheet" href="assets/site-v123.css?v='+sha(PUBLIC/'assets/site-v123.css')[:16]+'">'
    header=re.search(r'<header\b.*?</header>',old,re.S).group(0)
    header=re.sub(r'<nav aria-label="Navigation">.*?</nav>','<nav aria-label="Navigation"><a href="#little-world">'+w('Le jeu','The game')+'</a><a href="lab/ui-1-21/index.html">'+w('Simulation','Simulation')+'</a><a href="#download">Android</a></nav>',header,flags=re.S)
    copies={
        'map':('Un chemin à suivre','A path to follow','Compagnon, destination et caméra ont des repères distincts. Glisse la carte ou touche la minimap pour explorer.','Companion, destination and camera have distinct markers. Drag the map or touch the minimap to explore.'),
        'keys':('Une clé, ses cadenas','One key, its locks','La sortie réelle d’une clé ouvre toutes ses serrures. Undo restaure les obstacles et les portes.','An actual key exit opens all its locks. Undo restores the barriers and gates.'),
        'tactical':('Quatre puzzles, une réserve','Four puzzles, one reserve','L’Observatoire prête Nova, Hoot et Orbit : lis la situation et choisis quand utiliser leurs rôles.','The Observatory lends Nova, Hoot and Orbit: read the situation and choose when to use their roles.'),
        'home':('Un lieu à retrouver','A place to return to','Retrouve ton compagnon équipé, les progrès du joueur et tes pièces. Les prêts ne changent pas ta collection.','Return to your equipped companion, player progress and rooms. Loans do not change your collection.'),
        'puzzle':('Des conséquences lisibles','Readable consequences','Le retour montre la cause immédiate d’un blocage ; un coup légal peut encore fermer une issue utile.','Feedback shows the immediate cause of a block; a legal move can still close a useful exit.'),
        'skills':('Un talent à portée de main','A skill close at hand','Lis l’effet et son coût, puis reviens au même puzzle.','Read the effect and cost, then return to the same puzzle.'),
        'wardrobe':('Essaie. Puis choisis.','Try it. Then choose.','La tenue reste en essai jusqu’à Appliquer. Annuler retrouve le look porté.','An outfit stays a try-on until Apply. Cancel restores the worn look.'),
        'shop':('Un choix avant la découverte','A choice before discovery','Prix, propriété et probabilités restent visibles avant une dépense.','Prices, ownership and odds remain visible before spending.')}
    screen_copy=[(key,*copies[key]) for key in SCREEN_KEYS]
    screens=''.join('<figure><div class="phone">'+picture(key,fr+' · capture Android 1.23',en+' · Android 1.23 capture')+'</div><figcaption><h3>'+w(fr,en)+'</h3><p>'+w(desc,eng)+'</p></figcaption></figure>' for key,fr,en,desc,eng in screen_copy)
    footer=re.search(r'<footer\b.*?</footer>',old,re.S).group(0)
    size=w(f"{prod['bytes']/1e6:.2f}".replace('.',',')+' Mo',f"{prod['bytes']/1e6:.2f} MB")
    return f'''<!doctype html><html lang="fr"><head>{head}</head><body class="landing-v118 landing-v123" data-edition="1.23"><a class="skip" href="#main">{w('Aller au contenu','Skip to content')}</a>{header}<main id="main">
<section class="hero wrap" aria-labelledby="hero-title"><div class="hero-copy"><p class="eyebrow">LUMA DRIFT · 1.23</p><h1 id="hero-title">{w('Ton compagnon.<br><em>Ta prochaine aventure.</em>','Your companion.<br><em>Your next adventure.</em>')}</h1><p class="lead">{w('Un avatar qui prend sa place. Une équipe dont les talents servent dans le puzzle. Des îles à explorer, des formes à découvrir et un petit chez-toi à retrouver. La boucle devient visible : joue, progresse et choisis ce qui te plaît.','An avatar in the spotlight. A squad whose skills help inside the puzzle. Islands to explore, forms to discover and a little home to return to. See the loop: play, grow and choose what you love.')}</p><div class="actions">{button}{demo}</div><p class="download-note">{esc(version)} · {size} · Android 8+</p><p class="fineprint">{w('Candidate à essayer · Annonces de test · Achats réels indisponibles','Playtest candidate · Test ads · Real purchases unavailable')}</p><div class="hero-note">{w('Ton style. Tes talents. Des progrès que tu peux expliquer.','Your style. Your skills. Progress you can understand.')}</div></div><figure class="hero-visual"><div class="hero-disc" aria-hidden="true"></div><div class="phone">{picture(SCREEN_KEYS[0],'Vue native de la candidate Android 1.23','Native view from Android candidate 1.23',True)}</div><figcaption class="capture-label">{w('Capture Android native non retouchée','Unedited native Android capture')}</figcaption></figure></section>
<div class="facts wrap"><div><strong>1–3</strong>{w('compagnons dans ton équipe','companions in your squad')}</div><div><strong>6</strong>{w('familles de talents','skill families')}</div><div><strong>4</strong>{w('formes par compagnon','forms per companion')}</div><div><strong>10</strong>{w('zones sur ta route','zones along your journey')}</div></div>
<section id="little-world" class="section wrap" aria-labelledby="little-world-title"><div class="section-heading"><div><p class="eyebrow">{w('CHAQUE CHOSE A SON RÔLE','EVERYTHING HAS A ROLE')}</p><h2 id="little-world-title">{w('Du style.<br><em>Et du sens.</em>','Personality.<br><em>And purpose.</em>')}</h2></div><p class="section-lead">{w('L’avatar montre ton style. L’équipe apporte ses talents. L’XP suit tes premières victoires. Les copies font évoluer les compagnons. Chaque progression garde un rôle clair.','Your avatar expresses your style. Your squad brings puzzle skills. XP tracks first wins. Copies evolve companions. Every progress track has a clear purpose.')}</p></div><div class="feature-strip"><article><span class="feature-mark">✦</span><h3>{w('Ton compagnon en grand','Your companion, centre stage')}</h3><p>{w('Une scène lumineuse montre le compagnon équipé, sa rareté, son niveau et ses copies. La fiche Évolution montre les quatre formes réelles et explique son talent. Les tenues changent le look, sans avantage caché de puzzle.','A luminous stage shows your equipped companion, rarity, level and copies. Evolution displays the four actual forms and explains its skill. Outfits change the look without a hidden puzzle advantage.')}</p></article><article><span class="feature-mark">↗</span><h3>{w('Une aventure qui réagit','An adventure that reacts')}</h3><p>{w('La carte traverse dix zones dessinées. Les décors de puzzle prennent vie, les éléments glace, feu et eau demandent des taps gratuits et trois sorties manuelles distinctes sans correction donnent un Focus gratuit, une fois par essai.','The map crosses ten illustrated zones. Puzzle backdrops come alive, ice, fire and water shells clear with free taps, and three distinct manual exits without correction earn one free Focus per attempt.')}</p></article><article><span class="feature-mark">⌂</span><h3>{w('Ton monde à portée de main','Your world close at hand')}</h3><p>{w('Depuis l’accueil, retrouve directement la maison, le salon, la cuisine et le jardin. Déplace ton compagnon, aménage chaque pièce et reviens à l’aventure avec ton équipe.','From Home, jump straight to your house, living room, kitchen or garden. Move your companion, decorate each room and return to the adventure with your squad.')}</p></article></div><p class="role-footnote">{w('Les éléments concernent les nouvelles parties U à partir du niveau 11 et les expéditions éligibles. Les sauvegardes déjà en cours gardent leur état. Les mouvements et les sons se règlent séparément.','Elements apply to eligible new U Journey attempts from level 11 and expeditions. Existing runs keep their state. Motion and audio have separate settings.')}</p></section>
<section id="progression" class="section wrap" aria-labelledby="progression-title"><div class="section-heading"><div><p class="eyebrow">{w('JOUE → PROGRESSE → CHOISIS','PLAY → GROW → CHOOSE')}</p><h2 id="progression-title">{w('Tu sais ce que<br><em>tu gagnes.</em>','Know what<br><em>you earn.</em>')}</h2></div><p class="section-lead">{w('L’économie ne doit pas devenir une énigme de plus. La fiche Progression relie les soldes à leurs usages et les compteurs s’animent uniquement lorsque les valeurs changent réellement.','The economy should not become another puzzle. Progression links each balance to its purpose, and counters animate only when the actual value changes.')}</p></div><div class="progression-grid"><article><b>LumaCoins</b><p>{w('Gagnés en jouant et en récupérant les cadeaux prêts. Dépensés en bonus, tenues ou rencontres aléatoires à probabilités fixes. Les prix et l’aperçu précèdent la confirmation.','Earned in play and from ready gifts. Spend on boosts, outfits or random arrivals with fixed odds. Prices and previews come before confirmation.')}</p></article><article><b>{w('XP du joueur','Player XP')}</b><p>{w('Une première réussite donne de l’XP. Chaque tranche de 120 augmente le niveau joueur. Rejouer un plateau déjà récompensé ne redonne pas cette XP.','A first clear earns XP. Every 120 raises player level. Replaying a previously rewarded board does not grant that XP again.')}</p></article><article><b>{w('Copies et formes','Copies and forms')}</b><p>{w('Les copies font monter le compagnon. Ses formes changent aux niveaux 1, 3, 6 et 10. Les talents de puzzle gardent leurs effets indiqués. Une expédition peut viser une copie précise.','Copies raise companion level. Forms change at levels 1, 3, 6 and 10. Puzzle skills retain their stated effects. Expeditions can target a specific copy.')}</p></article><article><b>LumaGold</b><p>{w('Monnaie du compte séparée des Coins. Un solde inconnu reste indisponible. Les achats réels sont fermés dans cette candidate ; les soldes de test Lab restent isolés.','Account currency separate from Coins. An unknown balance remains unavailable. Real purchases are closed in this candidate; Lab test balances stay isolated.')}</p></article></div><p class="wallet-note">{w('Énergie de session : un plafond qui progresse de 10 à 16, +1 toutes les 10 minutes. Agrandir le plafond ne remplit pas le compteur. Un nouvel essai éligible coûte 1 ; reprendre le même essai est gratuit. La Charge des talents reste distincte. La recharge de 10 pour 50 Gold est réservée aux tests Lab, à zéro énergie.','Session energy: capacity grows from 10 to 16, +1 every 10 minutes. A larger capacity does not fill the counter. An eligible new attempt costs 1; resuming the same attempt is free. Talent Charge stays separate. The 10-energy refill for 50 Gold is limited to Lab tests at zero energy.')}</p><p class="wallet-note">{w('Les niveaux U1–10 conservent leurs récompenses et prix de base. Une courbe logarithmique porte les premières récompenses jusqu’à ×2 au niveau U1000, et les prix de base des bonus jusqu’à ×1,4 avant la remise d’avatar. Deux Undo sont gratuits par nouvel essai ; ensuite, le prix en Coins dépend du niveau et du nombre de retours payants, avec un plafond de 40, affiché et confirmé avant chaque retour. Le coup annulé reste compté.','U1–10 keep their base rewards and prices. A logarithmic curve takes first-clear rewards up to ×2 at U1000 and base boost prices up to ×1.4 before the avatar discount. Two Undos are free per new attempt; later Undo Coin prices depend on level and paid-Undo count, capped at 40, shown and confirmed before each Undo. The original move stays counted.')}</p><p class="role-footnote">{w('Les probabilités de rencontre sont fixes. Un doublon ne rapproche pas d’un résultat rare. Progression et Coins restent sur cet appareil ; la connexion Google ne synchronise pas la sauvegarde.','Arrival odds are fixed. A duplicate does not bring a rare result closer. Progress and Coins stay on this device; Google sign-in does not synchronize the save.')}</p></section>
<section id="simulation" class="section wrap simulation-section" aria-labelledby="simulation-title"><div><p class="eyebrow">{w('À ESSAYER DANS TON NAVIGATEUR','TRY IT IN YOUR BROWSER')}</p><h2 id="simulation-title">{w('Fais un tour.<br><em>Comprends la boucle.</em>','Take a turn.<br><em>See the loop.</em>')}</h2><p class="lead">{w('Dégage la glace, utilise Guidage, charge un Focus et vois les Coins et l’XP avancer. Compare les probabilités de rencontre, explore les courbes avec le curseur de niveau ou simule une copie ciblée pour révéler une nouvelle forme de Nova.','Clear ice, use Guide, earn a Focus and watch Coins and XP move. Compare arrival odds, explore the curves with the level slider or simulate a targeted copy to reveal a new Nova form.')}</p><div class="actions">{demo}</div></div><div class="simulation-explainer"><b>{w('Une simulation indépendante','An independent simulation')}</b><p>{w('Mini puzzle et état fictif réinitialisable. Les dessins et seuils d’évolution viennent du jeu ; les gains de 45 Coins / 35 XP servent à expliquer. Aucun paiement, compte ou fichier Android n’est modifié.','A resettable mini puzzle with fictional state. Artwork and evolution thresholds come from the game; 45 Coins / 35 XP rewards teach the loop. No payment, account or Android file is changed.')}</p></div></section>
<section id="screens" class="section wrap" aria-labelledby="screens-title"><div class="section-heading"><div><p class="eyebrow">{w('VU SUR ANDROID','SEEN ON ANDROID')}</p><h2 id="screens-title">{w('De l’avatar<br><em>à l’aventure.</em>','From avatar<br><em>to adventure.</em>')}</h2></div><p class="section-lead">{w('Accueil, boutique, carte et puzzle : quatre vues natives de cette candidate exacte, en français et en anglais.','Home, boutique, map and puzzle: four native views from this exact candidate, in French and English.')}</p></div><div class="native-grid">{screens}</div><p class="role-footnote">{w('Captures Android natives non retouchées. Les contrôles techniques et simulations ne mesurent ni le plaisir, ni la rétention, ni le confort sur tous les téléphones.','Unedited native Android captures. Technical checks and simulations do not measure enjoyment, retention or comfort on every phone.')}</p></section>
{section(old,'companions')}{section(old,'sound')}
<section id="download" class="section wrap download-section" aria-labelledby="download-title"><div><p class="eyebrow">{w('À TOI DE JOUER','YOUR TURN TO PLAY')}</p><h2 id="download-title">{w('La prochaine île<br><em>t’attend.</em>','Your next island<br><em>is waiting.</em>')}</h2><div class="actions">{button}</div><p class="download-note">{esc(version)} · code 31 · {prod['bytes']:,} {w('octets','bytes')}</p></div><div class="release-information"><p>{w('Distribution directe en APK. Les annonces restent en mode test ; les services et transactions réels ont leur qualification séparée. Aucune publication Play n’est impliquée.','Direct APK distribution. Ads remain in test mode; live services and transactions require separate qualification. No Play publication is implied.')}</p><details><summary>{w('Fichiers et empreintes','Files and checksums')}</summary><p>APK SHA-256</p><code>{prod['sha256']}</code><p><a href="/releases/manifest-{esc(version)}.json">{w('Manifeste de cette candidate','Candidate manifest')}</a></p><p><a href="/releases/{esc(Path(lab['path']).name)}">{w('Variante de test séparée','Separate test variant')}</a></p><code>{lab['sha256']}</code><p><a href="/releases/luma-drift-1.20.0-rc1.apk">{w('Archive Android 1.20','Android 1.20 archive')}</a> · <a href="/releases/luma-drift-1.19.0-prototype2.apk">{w('Archive Android 1.19','Android 1.19 archive')}</a></p></details></div></section></main>{footer}</body></html>'''

def cognitive_section():
    return '<section id="cognitive-lab" class="section wrap" aria-labelledby="cognitive-title"><p class="eyebrow">LUMA LAB · 1.23</p><h2 id="cognitive-title">'+w('Le parcours<br><em>à l’épreuve.</em>','The journey<br><em>under examination.</em>')+'</h2><p class="lead">'+w('8 000 trajectoires synthétiques sur le moteur 1.23. Causes visibles, talents et énergie examinés séparément. Zéro participant humain.','8,000 synthetic trajectories on the 1.23 engine. Visible causes, skills and energy examined separately. Zero human participants.')+'</p><div class="actions"><a class="button simulation-link" href="lab/clarity-123/index.html">'+w('Lire le nouveau rapport FR/EN','Read the new FR/EN report')+' ↗</a></div><p class="fineprint">'+w('Ces agents ne voient pas les graphismes et ne prouvent ni plaisir ni rétention.','These agents do not see the artwork and establish neither enjoyment nor retention.')+' <a href="lab/cognitive/index.html">'+w('Laboratoire 1.22 et archives','Lab 1.22 and archives')+'</a></p></section>'

def pending_page(old):
    assert 'data-edition="1.23"' not in old,'Never downgrade an accepted1.23 source to pending'
    old=re.sub(r'<section id="release-122-pending".*?</section>','',old,flags=re.S)
    notice='<section id="release-122-pending" class="wrap release-pending" aria-labelledby="pending-title"><p class="eyebrow">1.23.0-rc2 · CODE 30</p><h2 id="pending-title">'+w('1.23 en préparation','1.23 in preparation')+'</h2><p>'+w('Carte avec minimap, liens clé–cadenas et chapitre tactique : la qualification Android est en cours. Les APK et captures 1.23 seront ajoutés après validation de leurs octets exacts. Les téléchargements et images Android ci-dessous restent ceux de la candidate 1.21.','Map with minimap, key–lock links and tactical chapter: Android qualification is in progress. The 1.23 APKs and captures will be added after their exact bytes are accepted. Android downloads and images below remain from candidate 1.21.')+'</p><div class="actions"><a class="button simulation-link" href="lab/ui-1-22/index.html">'+w('Explorer la démo schématique','Explore the schematic demo')+'</a><a class="button" href="lab/cognitive/index.html">'+w('Lire le laboratoire 1.23','Read Lab 1.23')+'</a></div></section>'
    old=old.replace('<main id="main">','<main id="main">'+notice,1).replace('data-edition="1.21"','data-edition="1.23-pending"')
    if 'landing-v123' not in old:old=old.replace('landing-v121','landing-v121 landing-v123',1)
    old=re.sub(r'assets/site-v(?:121|122)\.js\?v=[a-f0-9]+','assets/site-v123.js?v='+sha(PUBLIC/'assets/site-v123.js')[:16],old)
    if 'assets/site-v123.css' not in old:old=old.replace('</head>','<link rel="stylesheet" href="assets/site-v123.css?v='+sha(PUBLIC/'assets/site-v123.css')[:16]+'"></head>',1)
    old=old.replace(section(old,'cognitive-lab'),cognitive_section())
    return old

def page(release,old):
    result=page_original(release,old)
    result=result.replace('lab/ui-1-21/index.html','lab/ui-1-22/index.html')
    result=result.replace(w('Ton compagnon.<br><em>Ta prochaine aventure.</em>','Your companion.<br><em>Your next adventure.</em>'),w('Suis la flèche.<br><em>Trouve ton chemin.</em>','Follow the arrow.<br><em>Find your way.</em>'))
    hero=re.search(r'<section class="hero wrap".*?</section>',result,re.S).group(0)
    revised=re.sub(r'<p class="lead">.*?</p>','<p class="lead">'+w('Des pièces en relief, des directions nettes et un objectif : libérer le plateau. Trouve la clé, observe ce que le relais change et appelle ton compagnon au bon moment.','Sculpted pieces, clear directions and one goal: clear the board. Find the key, see what the relay changes and call your companion at the right moment.')+'</p>',hero,flags=re.S)
    result=result.replace(hero,revised)
    features='<section id="little-world" class="section wrap" aria-labelledby="world-title"><div class="section-heading"><div><p class="eyebrow">'+w('LIRE → CHOISIR → AGIR','READ → CHOOSE → ACT')+'</p><h2 id="world-title">'+w('Un geste clair.<br><em>Un monde qui répond.</em>','One clear move.<br><em>A world that responds.</em>')+'</h2></div><p class="section-lead">'+w('La direction reste au centre. Chaque signe a sa place.','Direction stays at the centre. Every mark has its place.')+'</p></div><div class="feature-strip"><article><span class="feature-mark">↗</span><h3>'+w('Le plateau d’abord','The board first')+'</h3><p>'+w('Un objectif, un compteur d’actions et des flèches dessinées comme de petits objets. Les nouveaux essais du Voyage commencent sans préparations feu, glace et eau.','One goal, one move counter and arrows shaped like little objects. New Journey attempts start without fire, ice and water preparations.')+'</p></article><article><span class="feature-mark">⌁</span><h3>'+w('La clé et son effet','The key and its effect')+'</h3><p>'+w('La clé porte son signe à gauche ; les relais gardent leur rail en bas. Maintiens une pièce pour examiner ses conséquences avant de choisir. Un coup légal peut fermer une issue utile.','The key carries its mark on the left; relays keep their rail below. Hold a piece to inspect its consequences before choosing. A legal move can close a useful exit.')+'</p></article><article><span class="feature-mark">✦</span><h3>'+w('Ton compagnon, sur place','Your companion, right here')+'</h3><p>'+w('Le talent s’ouvre sur le plateau avec son effet et sa Charge. La tenue se teste avant d’appliquer et le retour retrouve le puzzle. L’équipe et l’équipement de jeu se préparent entre les essais.','Open a skill over the board to see its effect and Charge. Try an outfit before applying, then return to your puzzle. Prepare your squad and gameplay equipment between attempts.')+'</p></article></div><p class="role-footnote">'+w('Les essais déjà sauvegardés gardent leurs anciens éléments ; les expéditions éligibles conservent leurs règles. La carte avec déplacement et minimap, l’Observatoire, la maison et la collection restent accessibles.','Saved attempts retain their original elements; eligible expeditions keep their rules. The moving map and minimap, Observatory, home and collection remain available.')+'</p></section>'
    result=result.replace(section(result,'little-world'),features)
    result=result.replace(w('Accueil, boutique, carte et puzzle : quatre vues natives de cette candidate exacte, en français et en anglais.','Home, boutique, map and puzzle: four native views from this exact candidate, in French and English.'),w('Plateau, talent contextuel, tenue et carte : captures de la candidate exacte 1.23.0-rc2, code 31, en français et en anglais.','Board, contextual skill, outfit and map: captures from exact candidate 1.23.0-rc2, code 31, in French and English.'))
    result=result.replace(section(result,'simulation'),section(old,'simulation'))
    result=result.replace(section(old,'sound'),section(old,'sound')+cognitive_section(),1)
    result=result.replace('/releases/luma-drift-1.20.0-rc1.apk','/releases/luma-drift-1.22.0-rc1.apk').replace('Archive Android 1.20','Archive Android 1.22').replace('Android 1.20 archive','Android 1.22 archive')
    result=re.sub(r' · <a href="/releases/luma-drift-1\.19\.0-prototype2\.apk">.*?</a>','',result)
    fr='Luma Drift 1.23 — Suis la flèche. Trouve ton chemin.';en='Luma Drift 1.23 — Follow the arrow. Find your way.'
    result=re.sub(r'<title>.*?</title>','<title>'+fr+'</title>',result,count=1)
    result=re.sub(r'<meta property="og:title"[^>]*>','<meta property="og:title" content="'+esc(fr)+'" data-content-fr="'+esc(fr)+'" data-content-en="'+esc(en)+'">',result,count=1)
    return result

def main():
    global SCREEN_KEYS
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--artifact-manifest',type=Path,default=ROOT/'releases/manifest-1.23.0-rc2.json')
    parser.add_argument('--capture-map',type=Path,help='JSON list: key,locale,source,report; four screen keys times fr/en')
    parser.add_argument('--screen-keys',default=','.join(SCREEN_KEYS))
    parser.add_argument('--pending',action='store_true',help='Keep 1.21 downloads/captures explicitly labelled until exact native1.23 evidence is accepted')
    parser.add_argument('--artifact-commit',help='Immutable Git commit containing the exact artifact metadata')
    parser.add_argument('--preview',action='store_true',help='Write an exact local preview page and source URL map in staging only')
    parser.add_argument('--write',action='store_true',help='Copy verified files and write homepage/source manifest; never deploy')
    args=parser.parse_args();SCREEN_KEYS=tuple(args.screen_keys.split(','));assert len(SCREEN_KEYS)==4 and len(set(SCREEN_KEYS))==4 and set(SCREEN_KEYS)<={'map','keys','tactical','home','puzzle','shop','skills','wardrobe'}
    STAGING.mkdir(parents=True,exist_ok=True)
    if args.pending:
        updated=pending_page((PUBLIC/'index.html').read_text())
        if args.preview:(STAGING/'index.pending.html').write_text(updated)
        if args.write:(PUBLIC/'index.html').write_text(updated)
        print(json.dumps({'passed':True,'edition':'1.23-pending','written':args.write,'downloads':'1.21','native122_claimed':False,'deployed':False}));return
    assert args.capture_map is not None,'Accepted capture-map required, or use explicit --pending'
    manifest=bounded(args.artifact_manifest);release=json.loads(manifest.read_text())
    assert release['edition']=='1.23' and release['version']=='1.23.0-rc2' and release['version_code']==31
    assert args.artifact_commit is None or re.fullmatch('[a-f0-9]{40}',args.artifact_commit)
    candidate=bounded(release['candidate_report']);assert sha(candidate)==release['candidate_report_sha256'],'Frozen package report changed'
    pending=[]
    demo_files=sorted(path for path in (PUBLIC/'lab/ui-1-22').rglob('*') if path.is_file())
    assert {path.name for path in demo_files}=={'index.html','simulation.css','simulation.mjs'},'Incomplete reviewed simulation payload'
    for channel in ('production','lab'):
        entry=release['apks'][channel];source=bounded(entry['path']);assert sha(source)==entry['sha256'] and source.stat().st_size==entry['bytes'] and entry['code']==31 and entry['signer_certificate_sha256']==SIGNER,'Wrong exact signed APK metadata'
        target=PUBLIC/'releases'/source.name;relative=str(target.relative_to(ROOT))
        assert subprocess.run(['git','check-ignore','--quiet',relative],cwd=ROOT).returncode==0,'Physical external APK must be ignored'
        if target.exists():assert sha(target)==entry['sha256'] and target.stat().st_size==entry['bytes'],'Never replace earlier download bytes'
        pending.append((source,target))
    capture_map=bounded(args.capture_map);captures=json.loads(capture_map.read_text());assert isinstance(captures,list) and len(captures)==8,'Exactly eight current captures required'
    seen=set();proof=[];frozen={str(manifest.relative_to(ROOT)):sha(manifest),str(capture_map.relative_to(ROOT)):sha(capture_map)}
    for capture in captures:
        key,locale=capture['key'],capture['locale'];assert key in SCREEN_KEYS and locale in ('fr','en') and (key,locale) not in seen;seen.add((key,locale))
        source=bounded(capture['source']);report_path=bounded(capture['report']);report=json.loads(report_path.read_text());assert report['passed'] and report['native']['passed'] and report['apk_sha256']==report['installed_sha256']==release['apks']['production']['sha256'],'Capture is not accepted exact-candidate native evidence'
        assert report['expected_version']==release['version'] and report['expected_code']==31 and report['package']=='com.androidhit.lumadrift','Native capture package/version identity differs'
        relative=str(source.relative_to(report_path.parent));digest=sha(source);assert report['evidence_sha256'].get(relative)==digest,'Native report does not bind capture bytes'
        with source.open('rb') as image:header=image.read(24)
        assert header[:8]==b'\x89PNG\r\n\x1a\n' and struct.unpack('>II',header[16:24])==(1080,2400),'Expected original full native PNG'
        target=PUBLIC/f'assets/screenshots/v1.23/{key}-{locale}.png';pending.append((source,target))
        proof.append({'key':key,'locale':locale,'source':str(source.relative_to(ROOT)),'public':str(target.relative_to(ROOT)),'sha256':digest,'report':str(report_path.relative_to(ROOT)),'reportSha256':sha(report_path),'apkSha256':report['apk_sha256'],'screenshotsEdited':False})
        frozen[str(report_path.relative_to(ROOT))]=sha(report_path);frozen[str(source.relative_to(ROOT))]=digest
    old=(PUBLIC/'index.html').read_text();gallery=section(old,'companions');audio=section(old,'sound')
    baseline_data=json.loads((ROOT/'docs/validation/site-v1.22/release/source-manifest.json').read_text());retirement_path=ROOT/'docs/validation/v1.23/cleanup/public-retirements.json';retirement=json.loads(retirement_path.read_text());retired={row['path']:{'sha256':row['sha256'],'bytes':row['bytes']} for row in retirement['retirements']};assert len(retired)==14 and all(baseline_data['files'][name]==entry for name,entry in retired.items());frozen[str(retirement_path.relative_to(ROOT))]=sha(retirement_path);preserved={name.lstrip('/'):row['sha256'] for name,row in baseline_data['files'].items() if name!='/index.html' and name not in retired}
    new_page=page(release,old);assert gallery in new_page and audio in new_page,'Existing gallery/audio markup changed'
    frozen[str(candidate.relative_to(ROOT))]=sha(candidate)
    legal_updates=[]
    for asset in ('site-v123.js','site-v123.css'):
        path=PUBLIC/'assets'/asset;frozen[str(path.relative_to(ROOT))]=sha(path)
    for path in demo_files:frozen[str(path.relative_to(ROOT))]=sha(path)
    for name in ('prepare-site-v123.py','package-release-v123.py','check-site-v123.py','deploy-site-v123.py','stage-site-v123-rest.py','collect-site-v123-public-bytes.py','test-deploy-site-v123.py','test-stage-site-v123-rest.py','test-collect-site-v123-public-bytes.py','deploy-cognitive-overlay.py'):
        path=ROOT/'scripts'/name;frozen[str(path.relative_to(ROOT))]=sha(path)
    baseline=ROOT/'docs/validation/site-v1.22/release/source-manifest.json';frozen[str(baseline.relative_to(ROOT))]=sha(baseline)
    evidence={'status':'PASS','edition':'1.23','release':release['version'],'artifact_manifest':str(manifest.relative_to(ROOT)),'artifact_manifest_sha256':sha(manifest),'artifact_source_commit':args.artifact_commit,'production_apk_sha256':release['apks']['production']['sha256'],'lab_apk_sha256':release['apks']['lab']['sha256'],'package_identity_verified':True,'native_capture_provenance_verified':True,'screen_keys':SCREEN_KEYS,'screenshots':proof,'preserved_files':preserved,'retired_files':retired,'intentional_legal_updates':legal_updates,'gallery_markup_sha256':hashlib.sha256(gallery.encode()).hexdigest(),'audio_markup_sha256':hashlib.sha256(audio.encode()).hexdigest(),'frozen_inputs':frozen,'firebase_config_sha256':sha(ROOT/'firebase.json'),'network_verified':False,'deployed':False}
    if args.preview:
        preview_page=STAGING/'index.preview.html';preview_page.write_text(new_page)
        preview_files={'/':str(preview_page.relative_to(ROOT)),'/index.html':str(preview_page.relative_to(ROOT))}
        preview_files.update({'/'+str(target.relative_to(PUBLIC)):str(source.relative_to(ROOT)) for source,target in pending})
        preview_files['/releases/'+manifest.name]=str(manifest.relative_to(ROOT))
        (STAGING/'preview-files.json').write_text(json.dumps({'edition':'1.23','scope':'Exact candidate local preview; not a public source application, release approval or deployment.','production_apk_sha256':release['apks']['production']['sha256'],'files':preview_files},indent=2)+'\n')
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
    print(json.dumps({'passed':True,'written':args.write,'preview_written':args.preview,'edition':'1.23','screenshots':len(proof),'gallery_preserved':True,'account_files_preserved':True,'intentional_privacy_updates':len(legal_updates),'artifact_commit':args.artifact_commit,'deployed':False}))

if __name__=='__main__':main()
