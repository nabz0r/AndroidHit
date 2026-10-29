#!/usr/bin/env python3
"""Prepare the 1.19 Firebase homepage from exact signed APK metadata and accepted native captures; never deploy."""
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
EVIDENCE=ROOT/'docs/validation/site-v1.19'
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
    base='assets/screenshots/v1.19/'+key
    return f'<img src="{base}-fr.png" data-screenshot-fr="{base}-fr.png" data-screenshot-en="{base}-en.png" alt="{esc(fr)}" data-alt-fr="{esc(fr)}" data-alt-en="{esc(en)}" width="1080" height="2400" decoding="async" '+('fetchpriority="high"' if eager else 'loading="lazy"')+'>'
def section(source,id):
    match=re.search(r'<section id="'+re.escape(id)+r'"(?=\s|>).*?</section>',source,re.S)
    assert match,'Existing preserved section missing: '+id
    return match.group(0)

def page(release,old):
    prod=release['apks']['production'];lab=release['apks']['lab'];version=prod['version'];download='/releases/'+Path(prod['path']).name
    button=f'<a class="button primary" href="{download}">'+w('Télécharger sur Android','Download for Android')+' <span aria-hidden="true">↓</span></a>'
    head=re.search(r'<head>(.*?)</head>',old,re.S).group(1)
    description=('Luma Drift 1.19 : compose ton équipe, découvre six familles de talents et active Résonance dans les puzzles.','Luma Drift 1.19: choose your squad, discover six skill families and activate Resonance in puzzles.')
    head=re.sub(r'<meta name="description"[^>]*>',f'<meta name="description" content="{esc(description[0])}" data-content-fr="{esc(description[0])}" data-content-en="{esc(description[1])}">',head)
    head=re.sub(r'<meta property="og:title"[^>]*>','<meta property="og:title" content="Luma Drift 1.19 — Ton équipe. Ton prochain déclic." data-content-fr="Luma Drift 1.19 — Ton équipe. Ton prochain déclic." data-content-en="Luma Drift 1.19 — Your squad. Your next move.">',head)
    head=re.sub(r'<meta property="og:image"[^>]*>','<meta property="og:image" content="https://luma-drift-production.web.app/assets/screenshots/v1.19/puzzle-fr.png">',head)
    head=re.sub(r'<title>.*?</title>','<title>Luma Drift 1.19 — Ton équipe. Ton prochain déclic.</title>',head)
    head=re.sub(r'assets/site-v11[89]\.js\?v=[a-f0-9]+','assets/site-v119.js?v='+sha(PUBLIC/'assets/site-v119.js')[:16],head)
    head=re.sub(r'<link rel="stylesheet" href="assets/site-v119\.css[^>]*>','',head)
    head+='<link rel="stylesheet" href="assets/site-v119.css?v='+sha(PUBLIC/'assets/site-v119.css')[:16]+'">'
    header=re.search(r'<header\b.*?</header>',old,re.S).group(0)
    header=re.sub(r'<nav aria-label="Navigation">.*?</nav>','<nav aria-label="Navigation"><a href="#squad">'+w('Équipe','Squad')+'</a><a href="#companions">'+w('Compagnons','Companions')+'</a><a href="#download">Android</a></nav>',header,flags=re.S)
    roles=[('✦','Étoile guide','Guiding star','Un prochain geste sûr, illuminé quelques secondes.','One safe next action, highlighted for a few seconds.'),('◉','Regard clair','Open eyes','Les pièces mobiles maintenant. Un mouvement légal n’est pas forcément une solution.','Pieces that can move now. A legal move is not necessarily a winning continuation.'),('⋆','Chemin d’étoiles','Starlight trail','Un aperçu de tes prochains gestes sûrs.','A preview of your next safe actions.'),('↗','Petit élan','Little lift','Une sortie certifiée, avec le coût de coup habituel.','One certified exit, with the usual action cost.'),('≈','Vague lunaire','Moonlit flow','Une courte suite de sorties certifiées. Chaque sortie compte.','A short sequence of certified exits. Each exit counts.'),('↶','Petit retour','Second thought','Un ou deux gestes rétablis via l’historique et ses limites.','One or two earlier actions restored within the history limits.')]
    cards=''.join('<article class="role-card"><span class="role-mark" aria-hidden="true">'+mark+'</span><h3>'+w(fr,en)+'</h3><p>'+w(desc,eng)+'</p></article>' for mark,fr,en,desc,eng in roles)
    screen_copy=[('home','Entre deux aventures','Between adventures','Retrouve ton compagnon et les lieux du jeu.','Return to your companion and the game’s places.'),('squad','Choisis qui vient avec toi','Choose who comes along','Des places distinctes, des compagnons possédés et un enregistrement explicite.','Distinct slots, owned companions and an explicit save.'),('puzzle','Ton équipe est sur le plateau','Your squad joins the board','Talents, énergie et état d’utilisation restent visibles pendant le puzzle.','Skills, energy and used states stay visible during the puzzle.')]
    screens=''.join('<figure><div class="phone">'+picture(key,fr+' · capture Android 1.19',en+' · Android 1.19 capture')+'</div><figcaption><h3>'+w(fr,en)+'</h3><p>'+w(desc,eng)+'</p></figcaption></figure>' for key,fr,en,desc,eng in screen_copy)
    footer=re.search(r'<footer\b.*?</footer>',old,re.S).group(0)
    return f'''<!doctype html><html lang="fr"><head>{head}</head><body class="landing-v118 landing-v119" data-edition="1.19"><a class="skip" href="#main">{w('Aller au contenu','Skip to content')}</a>{header}<main id="main">
<section class="hero wrap" aria-labelledby="hero-title"><div class="hero-copy"><p class="eyebrow">LUMA DRIFT · 1.19</p><h1 id="hero-title">{w('Ton équipe.<br><em>Ton prochain déclic.</em>','Your squad.<br><em>Your next move.</em>')}</h1><p class="lead">{w('Trois petites personnalités à tes côtés. Choisis tes talents, fais circuler l’énergie et déclenche Résonance au bon moment. Tes compagnons prennent part au puzzle.','Three little personalities by your side. Choose your skills, build shared energy and activate Resonance at the right moment. Your companions take part in the puzzle.')}</p><div class="actions">{button}<a class="text-link" href="#squad">{w('Découvrir l’équipe','Discover the squad')} ↓</a></div><p class="download-note">{esc(version)} · {prod['bytes']/1e6:.2f} MB · Android 8+</p><p class="fineprint">{w('Prototype à essayer · Annonces de test · Achats réels désactivés','Playtest prototype · Test ads · Real checkout disabled')}</p><div class="hero-note">{w('Un à trois compagnons. Six familles de talents. Ton look reste ton choix.','One to three companions. Six skill families. Your look stays your choice.')}</div></div><figure class="hero-visual"><div class="hero-disc" aria-hidden="true"></div><div class="phone">{picture('puzzle','Le puzzle Android 1.19 avec son équipe et son énergie','Android 1.19 puzzle with its squad and energy',True)}</div><figcaption class="capture-label">{w('Vraie capture de la candidate Android','Real Android candidate capture')}</figcaption></figure></section>
<div class="facts wrap"><div><strong>1–3</strong>{w('compagnons dans ton équipe','companions in your squad')}</div><div><strong>6</strong>{w('familles de talents','skill families')}</div><div><strong>37</strong>{w('définitions de compagnon','companion definitions')}</div><div><strong>1 000</strong>{w('puzzles de la route U','U Journey puzzles')}</div></div>
<section id="squad" class="section wrap" aria-labelledby="squad-title"><div class="section-heading"><div><p class="eyebrow">{w('ENSEMBLE SUR LE PLATEAU','TOGETHER ON THE BOARD')}</p><h2 id="squad-title">{w('Chaque place<br><em>compte.</em>','Every slot<br><em>matters.</em>')}</h2></div><p class="section-lead">{w('Compose une équipe avec tes compagnons possédés. Le premier est ton capitaine. Tu peux jouer avec un seul compagnon et changer l’équipe pour ton prochain plateau.','Build a squad from your owned companions. The first is your captain. You can play with just one companion and save changes for your next board.')}</p></div><div class="team-rules"><div><strong>{w('3 énergie au départ','Start with 3 energy')}</strong><p>{w('Jusqu’à 6 en réserve. Chaque nouvelle pièce sortie ajoute 1 énergie ; ressortir la même pièce après un retour n’en ajoute pas.','Store up to 6. Each new piece exited adds 1 energy; exiting it again after Undo adds nothing.')}</p></div><div><strong>{w('Un talent par compagnon','One skill per companion')}</strong><p>{w('Chaque compagnon a un usage par tentative. Prévisualise son effet et son coût avant de l’activer. Tes objets d’inventaire restent séparés.','Each companion has one use per attempt. Preview its effect and cost before activating. Your inventory items stay separate.')}</p></div><div><strong>{w('À deux, Résonance','Two roles, Resonance')}</strong><p>{w('Utilise deux talents de familles différentes, puis active un geste certifié supplémentaire. Une fois par tentative, sans énergie supplémentaire.','Use two skills from different families, then activate one extra certified action. Once per attempt, with no extra energy cost.')}</p></div></div><div class="role-grid">{cards}</div><p class="role-footnote">{w('37 identités déclinent ces six familles avec des paramètres bornés : ce ne sont pas 37 mécaniques uniques. Le compagnon choisit le talent ; les tenues changent le look. Les règles et limites de coups du plateau continuent de s’appliquer.','37 identities vary these six families through bounded parameters: these are not 37 unique mechanics. The companion determines the skill; outfits change the look. The board’s movement rules and action limits still apply.')}</p></section>
<section id="screens" class="section wrap" aria-labelledby="screens-title"><div class="section-heading"><div><p class="eyebrow">{w('VU SUR ANDROID','SEEN ON ANDROID')}</p><h2 id="screens-title">{w('Plus près de toi.<br><em>Plus près du jeu.</em>','Closer to you.<br><em>Closer to the game.</em>')}</h2></div><p class="section-lead">{w('Portrait équipé, humeurs liées aux gestes et effets courts accompagnent la partie. Le mode mouvements réduits conserve des repères stables.','Equipped portraits, reactions tied to play and brief effects accompany the puzzle. Reduced motion keeps stable cues.')}</p></div><div class="native-grid">{screens}</div><p class="role-footnote">{w('Captures natives non retouchées de cette candidate, en français et en anglais. Les vérifications techniques ne prouvent ni le plaisir, ni la rétention, ni le confort sur tous les téléphones.','Unedited native captures of this candidate, in French and English. Technical checks do not prove enjoyment, retention or comfort on every phone.')}</p></section>
{section(old,'companions')}{section(old,'sound')}
<section id="download" class="section wrap download-section" aria-labelledby="download-title"><div><p class="eyebrow">{w('À TOI DE JOUER','YOUR TURN TO PLAY')}</p><h2 id="download-title">{w('Une équipe.<br><em>À essayer.</em>','A squad.<br><em>To playtest.</em>')}</h2><div class="actions">{button}</div><p class="download-note">{esc(version)} · code 26 · {prod['bytes']:,} {w('octets','bytes')}</p></div><div class="release-information"><p>{w('Progression, équipe, objets, maison et LumaCoins restent sur cet appareil. La connexion Google ne synchronise pas cette sauvegarde. LumaGold reste une monnaie de compte distincte.','Progress, squad, items, home and LumaCoins remain on this device. Google sign-in does not synchronize this save. LumaGold remains a separate account currency.')}</p><details><summary>{w('Fichiers et empreintes','Files and checksums')}</summary><p>APK SHA-256</p><code>{prod['sha256']}</code><p><a href="/releases/manifest-{esc(version)}.json">{w('Manifeste de cette candidate','Candidate manifest')}</a></p><p><a href="/releases/{esc(Path(lab['path']).name)}">{w('Variante de test séparée','Separate test variant')}</a></p><code>{lab['sha256']}</code><p><a href="/releases/luma-drift-1.18.0-prototype1.apk">{w('Archive Android 1.18','Android 1.18 archive')}</a></p></details><p class="fineprint">{w('Distribution directe en APK. Annonces de test ; achats réels et tirages premium payants désactivés.','Direct APK distribution. Test ads; real checkout and paid premium draws disabled.')}</p></div></section></main>{footer}</body></html>'''

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--artifact-manifest',type=Path,default=ROOT/'releases/manifest-1.19.0-prototype1.json')
    parser.add_argument('--capture-map',type=Path,required=True,help='JSON list: key, locale, source, report; keys home/squad/puzzle × fr/en')
    parser.add_argument('--artifact-commit',help='Immutable Git commit containing the exact artifact metadata')
    parser.add_argument('--write',action='store_true',help='Copy verified files and write homepage/source manifest; never deploy')
    args=parser.parse_args();manifest=bounded(args.artifact_manifest);release=json.loads(manifest.read_text())
    assert release['edition']=='1.19' and release['version']=='1.19.0-prototype1' and release['version_code']==26
    assert args.artifact_commit is None or re.fullmatch('[a-f0-9]{40}',args.artifact_commit)
    candidate=bounded(release['candidate_report']);assert sha(candidate)==release['candidate_report_sha256'],'Frozen package report changed'
    pending=[]
    for channel in ('production','lab'):
        entry=release['apks'][channel];source=bounded(entry['path']);assert sha(source)==entry['sha256'] and source.stat().st_size==entry['bytes'] and entry['code']==26 and entry['signer_certificate_sha256']==SIGNER,'Wrong exact signed APK metadata'
        target=PUBLIC/'releases'/source.name;relative=str(target.relative_to(ROOT))
        assert subprocess.run(['git','check-ignore','--quiet',relative],cwd=ROOT).returncode==0,'Physical external APK must be ignored'
        if target.exists():assert sha(target)==entry['sha256'] and target.stat().st_size==entry['bytes'],'Never replace earlier download bytes'
        pending.append((source,target))
    capture_map=bounded(args.capture_map);captures=json.loads(capture_map.read_text());assert isinstance(captures,list) and len(captures)==6,'Exactly six current home/team/puzzle captures required'
    seen=set();proof=[];frozen={str(manifest.relative_to(ROOT)):sha(manifest),str(capture_map.relative_to(ROOT)):sha(capture_map)}
    for capture in captures:
        key,locale=capture['key'],capture['locale'];assert key in ('home','squad','puzzle') and locale in ('fr','en') and (key,locale) not in seen;seen.add((key,locale))
        source=bounded(capture['source']);report_path=bounded(capture['report']);report=json.loads(report_path.read_text());assert report['passed'] and report['native']['passed'] and report['apk_sha256']==report['installed_sha256']==release['apks']['production']['sha256'],'Capture is not accepted exact-candidate native evidence'
        assert report['expected_version']==release['version'] and report['expected_code']==26 and report['package']=='com.androidhit.lumadrift','Native capture package/version identity differs'
        relative=str(source.relative_to(report_path.parent));digest=sha(source);assert report['evidence_sha256'].get(relative)==digest,'Native report does not bind capture bytes'
        with source.open('rb') as image:header=image.read(24)
        assert header[:8]==b'\x89PNG\r\n\x1a\n' and struct.unpack('>II',header[16:24])==(1080,2400),'Expected original full native PNG'
        target=PUBLIC/f'assets/screenshots/v1.19/{key}-{locale}.png';pending.append((source,target))
        proof.append({'key':key,'locale':locale,'source':str(source.relative_to(ROOT)),'public':str(target.relative_to(ROOT)),'sha256':digest,'report':str(report_path.relative_to(ROOT)),'reportSha256':sha(report_path),'apkSha256':report['apk_sha256'],'screenshotsEdited':False})
        frozen[str(report_path.relative_to(ROOT))]=sha(report_path);frozen[str(source.relative_to(ROOT))]=digest
    old=(PUBLIC/'index.html').read_text();gallery=section(old,'companions');audio=section(old,'sound')
    preserved={name:sha(path) for name,path in payloads().items() if name!='index.html' and not name.startswith('assets/screenshots/v1.19/') and not name.startswith('assets/site-v119.') and '1.19' not in name}
    new_page=page(release,old);assert gallery in new_page and audio in new_page,'Existing gallery/audio markup changed'
    frozen[str(candidate.relative_to(ROOT))]=sha(candidate)
    for name in ('prepare-site-v119.py','package-release-v119.py','check-site-v119.py','deploy-site-v119.py','verify-site-v119-publication.py'):
        path=ROOT/'scripts'/name;frozen[str(path.relative_to(ROOT))]=sha(path)
    evidence={'status':'PASS','edition':'1.19','release':release['version'],'artifact_manifest':str(manifest.relative_to(ROOT)),'artifact_manifest_sha256':sha(manifest),'artifact_source_commit':args.artifact_commit,'production_apk_sha256':release['apks']['production']['sha256'],'lab_apk_sha256':release['apks']['lab']['sha256'],'package_identity_verified':True,'native_capture_provenance_verified':True,'screenshots':proof,'preserved_files':preserved,'gallery_markup_sha256':hashlib.sha256(gallery.encode()).hexdigest(),'audio_markup_sha256':hashlib.sha256(audio.encode()).hexdigest(),'frozen_inputs':frozen,'firebase_config_sha256':sha(ROOT/'firebase.json'),'network_verified':False,'deployed':False}
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
    print(json.dumps({'passed':True,'written':args.write,'edition':'1.19','screenshots':len(proof),'gallery_preserved':True,'legal_account_files_preserved':True,'artifact_commit':args.artifact_commit,'deployed':False}))

if __name__=='__main__':main()
