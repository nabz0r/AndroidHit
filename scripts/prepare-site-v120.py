#!/usr/bin/env python3
"""Prepare the 1.20 Firebase homepage from exact signed APK metadata and accepted native captures; never deploy."""
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
EVIDENCE=ROOT/'docs/validation/site-v1.20'
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
    base='assets/screenshots/v1.20/'+key
    return f'<img src="{base}-fr.png" data-screenshot-fr="{base}-fr.png" data-screenshot-en="{base}-en.png" alt="{esc(fr)}" data-alt-fr="{esc(fr)}" data-alt-en="{esc(en)}" width="1080" height="2400" decoding="async" '+('fetchpriority="high"' if eager else 'loading="lazy"')+'>'
def section(source,id):
    match=re.search(r'<section id="'+re.escape(id)+r'"(?=\s|>).*?</section>',source,re.S)
    assert match,'Existing preserved section missing: '+id
    return match.group(0)

def page(release,old):
    prod=release['apks']['production'];lab=release['apks']['lab'];version=prod['version'];download='/releases/'+Path(prod['path']).name
    button=f'<a class="button primary" href="{download}">'+w('Télécharger sur Android','Download for Android')+' <span aria-hidden="true">↓</span></a>'
    head=re.search(r'<head>(.*?)</head>',old,re.S).group(1)
    description=('Luma Drift 1.20 : un accueil plus doux, une boutique illustrée, de vrais cadeaux et des puzzles avec tes compagnons.','Luma Drift 1.20: a softer home, an illustrated boutique, real earned gifts and puzzles with your companions.')
    head=re.sub(r'<meta name="description"[^>]*>',f'<meta name="description" content="{esc(description[0])}" data-content-fr="{esc(description[0])}" data-content-en="{esc(description[1])}">',head)
    title_fr='Luma Drift 1.20 — Ton petit moment de jeu.';title_en='Luma Drift 1.20 — Your little moment to play.'
    head=re.sub(r'<meta property="og:title"[^>]*>',f'<meta property="og:title" content="{esc(title_fr)}" data-content-fr="{esc(title_fr)}" data-content-en="{esc(title_en)}">',head)
    head=re.sub(r'<meta property="og:image"[^>]*>','<meta property="og:image" content="https://luma-drift-production.web.app/assets/screenshots/v1.20/home-fr.png">',head)
    head=re.sub(r'<title>.*?</title>',f'<title>{esc(title_fr)}</title>',head)
    head=re.sub(r'assets/site-v(?:118|119|120)\.js\?v=[a-f0-9]+','assets/site-v120.js?v='+sha(STAGING/'site-v120.js')[:16],head)
    head=re.sub(r'<link rel="stylesheet" href="assets/site-v(?:119|120)\.css[^>]*>','',head)
    head+='<link rel="stylesheet" href="assets/site-v120.css?v='+sha(STAGING/'site-v120.css')[:16]+'">'
    header=re.search(r'<header\b.*?</header>',old,re.S).group(0)
    header=re.sub(r'<nav aria-label="Navigation">.*?</nav>','<nav aria-label="Navigation"><a href="#little-world">'+w('Le jeu','The game')+'</a><a href="#companions">'+w('Compagnons','Companions')+'</a><a href="#download">Android</a></nav>',header,flags=re.S)
    screen_copy=[('home','Un accueil tout doux','A softer home','Ton compagnon équipé, ton équipe et un bouton Jouer clair.','Your equipped companion, your squad and one clear Play button.'),('shop','Une petite boutique à explorer','A little boutique to explore','Un objet à la fois, ses vrais prix et un aperçu avant de choisir.','One item at a time, real prices and a preview before choosing.'),('victory','Le plaisir du geste réussi','A little win to enjoy','Un résultat lisible et les cadeaux réellement prêts à récupérer.','A clear result and gifts that are actually ready to collect.')]
    screens=''.join('<figure><div class="phone">'+picture(key,fr+' · capture Android 1.20',en+' · Android 1.20 capture')+'</div><figcaption><h3>'+w(fr,en)+'</h3><p>'+w(desc,eng)+'</p></figcaption></figure>' for key,fr,en,desc,eng in screen_copy)
    footer=re.search(r'<footer\b.*?</footer>',old,re.S).group(0)
    size=w(f"{prod['bytes']/1e6:.2f}".replace('.',',')+' Mo',f"{prod['bytes']/1e6:.2f} MB")
    return f'''<!doctype html><html lang="fr"><head>{head}</head><body class="landing-v118 landing-v120" data-edition="1.20"><a class="skip" href="#main">{w('Aller au contenu','Skip to content')}</a>{header}<main id="main">
<section class="hero wrap" aria-labelledby="hero-title"><div class="hero-copy"><p class="eyebrow">LUMA DRIFT · 1.20</p><h1 id="hero-title">{w('Ton petit<br><em>moment de jeu.</em>','Your little<br><em>moment to play.</em>')}</h1><p class="lead">{w('Un compagnon à retrouver. Un puzzle à démêler. Un petit cadeau à ouvrir. Explore un accueil plus léger et une boutique pleine de personnalités, puis repars à l’aventure avec ton équipe.','A companion to come back to. A puzzle to untangle. A little gift to open. Explore a lighter home and a boutique full of personality, then head into the next adventure with your squad.')}</p><div class="actions">{button}<a class="text-link" href="#little-world">{w('Découvrir le jeu','Discover the game')} ↓</a></div><p class="download-note">{esc(version)} · {size} · Android 8+</p><p class="fineprint">{w('Candidate à essayer · Annonces de test · Activation commerciale séparée','Playtest candidate · Test ads · Commercial activation remains separate')}</p><div class="hero-note">{w('Tes soldes restent visibles. Tes cadeaux attendent ton clic.','Your balances stay visible. Your gifts wait for your tap.')}</div></div><figure class="hero-visual"><div class="hero-disc" aria-hidden="true"></div><div class="phone">{picture('home','Vrai accueil Android 1.20 avec ses soldes et son compagnon','Actual Android 1.20 home with wallets and companion',True)}</div><figcaption class="capture-label">{w('Capture Android native non retouchée','Unedited native Android capture')}</figcaption></figure></section>
<div class="facts wrap"><div><strong>1–3</strong>{w('compagnons dans ton équipe','companions in your squad')}</div><div><strong>6</strong>{w('familles de talents','skill families')}</div><div><strong>37</strong>{w('compagnons à rencontrer','companions to meet')}</div><div><strong>1 000</strong>{w('puzzles de la route U','U Journey puzzles')}</div></div>
<section id="little-world" class="section wrap" aria-labelledby="little-world-title"><div class="section-heading"><div><p class="eyebrow">{w('UN PETIT MONDE À EXPLORER','A LITTLE WORLD TO EXPLORE')}</p><h2 id="little-world-title">{w('Moins de bruit.<br><em>Plus de petits plaisirs.</em>','Less clutter.<br><em>More little delights.</em>')}</h2></div><p class="section-lead">{w('Accueil, Équipe, Collection, Cadeaux, Boutique : cinq repères pour retrouver tes envies. La maison et l’atelier restent à portée de main.','Home, Squad, Collection, Gifts, Boutique: five clear places to find what you want. Your room and workshop remain close by.')}</p></div><div class="feature-strip"><article><h3>{w('Un vrai cadeau gagné','A gift you have earned')}</h3><p>{w('Les quêtes du jour et de la semaine affichent leur progression réelle. Une fois prêt, ouvre ton cadeau : il reste là jusqu’à ton clic. Un cadeau récupéré ne se réclame pas deux fois.','Daily and weekly quests show real progress. Once ready, open your gift: it stays until your tap. A collected gift cannot be claimed twice.')}</p></article><article><h3>{w('Une boutique à regarder','A boutique to browse')}</h3><p>{w('Compagnons, looks et petits coups de pouce se découvrent en carrousel. Prix, stock, possession et aperçu restent explicites avant une action. Les probabilités des rencontres se consultent avant le tirage.','Browse companions, looks and little boosts in a carousel. Prices, stock, ownership and previews stay clear before an action. Arrival odds are available before a draw.')}</p></article><article><h3>{w('Ton équipe joue avec toi','Your squad plays with you')}</h3><p>{w('Retrouve les six familles de talents : guider, révéler, prévoir, pousser, enchaîner et revenir. Deux familles utilisées chargent une Résonance. Les tenues changent le look ; chaque compagnon apporte son talent.','Keep the six skill families: guide, reveal, preview, push, flow and rewind. Two used families charge Resonance. Outfits change the look; each companion brings their skill.')}</p></article></div><p class="wallet-note">{w('LumaCoins : gagnés en jouant. LumaGold : monnaie premium de compte, avec un solde test séparé dans Lab. Les achats réels restent fermés. Énergie de session : distincte de la Charge des talents. Un solde indisponible reste indiqué comme tel.','LumaCoins: earned in play. LumaGold: a separate account wallet for premium currency, with an isolated test balance in Lab. Live checkout remains closed. Session energy: separate from talent charge. An unavailable balance is shown as unavailable.')}</p><p class="wallet-note">{w('10 énergie maximum, +1 toutes les 10 minutes. Un nouvel essai coûte 1 à partir du niveau 11 et dans les défis, Remix et expéditions. Reprendre la même partie, le tutoriel, les niveaux 1–10 et l’entraînement entre amis restent gratuits. Recharge de 10 pour 50 LumaGold préparée, uniquement testable dans Lab.','Up to 10 energy, +1 every 10 minutes. A new attempt costs 1 from level 11 and in challenges, Remix and expeditions. Resuming the same run, the tutorial, levels 1–10 and friend practice stay free. The 10-energy refill for 50 LumaGold is prepared and testable only in Lab.')}</p><p class="role-footnote">{w('Les règles et budgets des plateaux restent explicites. La progression et les LumaCoins restent sur cet appareil ; la connexion Google ne synchronise pas la sauvegarde.','Board rules and budgets remain explicit. Progress and LumaCoins stay on this device; Google sign-in does not synchronize the save.')}</p></section>
<section id="screens" class="section wrap" aria-labelledby="screens-title"><div class="section-heading"><div><p class="eyebrow">{w('VU SUR ANDROID','SEEN ON ANDROID')}</p><h2 id="screens-title">{w('Du premier clic<br><em>au petit triomphe.</em>','From the first tap<br><em>to a little triumph.</em>')}</h2></div><p class="section-lead">{w('Accueil, boutique et victoire : trois vues réelles de cette candidate, en français et en anglais. Les sons et les mouvements se règlent dans le jeu.','Home, boutique and victory: three real views of this candidate, in French and English. Sound and motion can be adjusted in the game.')}</p></div><div class="native-grid">{screens}</div><p class="role-footnote">{w('Captures natives non retouchées. Les contrôles techniques et les simulations ne mesurent ni le plaisir, ni la rétention, ni le confort sur tous les téléphones.','Unedited native captures. Technical checks and simulations do not measure enjoyment, retention or comfort on every phone.')}</p></section>
{section(old,'companions')}{section(old,'sound')}
<section id="download" class="section wrap download-section" aria-labelledby="download-title"><div><p class="eyebrow">{w('À TOI DE JOUER','YOUR TURN TO PLAY')}</p><h2 id="download-title">{w('Un petit monde.<br><em>À essayer.</em>','A little world.<br><em>To playtest.</em>')}</h2><div class="actions">{button}</div><p class="download-note">{esc(version)} · code 28 · {prod['bytes']:,} {w('octets','bytes')}</p></div><div class="release-information"><p>{w('Distribution directe en APK. Les annonces restent en mode test ; les services et transactions réels ont leur qualification séparée. Aucune publication Play n’est impliquée.','Direct APK distribution. Ads remain in test mode; live services and transactions require separate qualification. No Play publication is implied.')}</p><details><summary>{w('Fichiers et empreintes','Files and checksums')}</summary><p>APK SHA-256</p><code>{prod['sha256']}</code><p><a href="/releases/manifest-{esc(version)}.json">{w('Manifeste de cette candidate','Candidate manifest')}</a></p><p><a href="/releases/{esc(Path(lab['path']).name)}">{w('Variante de test séparée','Separate test variant')}</a></p><code>{lab['sha256']}</code><p><a href="/releases/luma-drift-1.19.0-prototype2.apk">{w('Archive Android 1.19','Android 1.19 archive')}</a></p></details></div></section></main>{footer}</body></html>'''

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--artifact-manifest',type=Path,default=ROOT/'releases/manifest-1.20.0-rc1.json')
    parser.add_argument('--capture-map',type=Path,required=True,help='JSON list: key, locale, source, report; keys home/shop/victory × fr/en')
    parser.add_argument('--artifact-commit',help='Immutable Git commit containing the exact artifact metadata')
    parser.add_argument('--write',action='store_true',help='Copy verified files and write homepage/source manifest; never deploy')
    args=parser.parse_args();manifest=bounded(args.artifact_manifest);release=json.loads(manifest.read_text())
    assert release['edition']=='1.20' and release['version']=='1.20.0-rc1' and release['version_code']==28
    assert args.artifact_commit is None or re.fullmatch('[a-f0-9]{40}',args.artifact_commit)
    candidate=bounded(release['candidate_report']);assert sha(candidate)==release['candidate_report_sha256'],'Frozen package report changed'
    pending=[(STAGING/'site-v120.js',PUBLIC/'assets/site-v120.js'),(STAGING/'site-v120.css',PUBLIC/'assets/site-v120.css')]
    for channel in ('production','lab'):
        entry=release['apks'][channel];source=bounded(entry['path']);assert sha(source)==entry['sha256'] and source.stat().st_size==entry['bytes'] and entry['code']==28 and entry['signer_certificate_sha256']==SIGNER,'Wrong exact signed APK metadata'
        target=PUBLIC/'releases'/source.name;relative=str(target.relative_to(ROOT))
        assert subprocess.run(['git','check-ignore','--quiet',relative],cwd=ROOT).returncode==0,'Physical external APK must be ignored'
        if target.exists():assert sha(target)==entry['sha256'] and target.stat().st_size==entry['bytes'],'Never replace earlier download bytes'
        pending.append((source,target))
    capture_map=bounded(args.capture_map);captures=json.loads(capture_map.read_text());assert isinstance(captures,list) and len(captures)==6,'Exactly six current home/shop/victory captures required'
    seen=set();proof=[];frozen={str(manifest.relative_to(ROOT)):sha(manifest),str(capture_map.relative_to(ROOT)):sha(capture_map)}
    for capture in captures:
        key,locale=capture['key'],capture['locale'];assert key in ('home','shop','victory') and locale in ('fr','en') and (key,locale) not in seen;seen.add((key,locale))
        source=bounded(capture['source']);report_path=bounded(capture['report']);report=json.loads(report_path.read_text());assert report['passed'] and report['native']['passed'] and report['apk_sha256']==report['installed_sha256']==release['apks']['production']['sha256'],'Capture is not accepted exact-candidate native evidence'
        assert report['expected_version']==release['version'] and report['expected_code']==28 and report['package']=='com.androidhit.lumadrift','Native capture package/version identity differs'
        relative=str(source.relative_to(report_path.parent));digest=sha(source);assert report['evidence_sha256'].get(relative)==digest,'Native report does not bind capture bytes'
        with source.open('rb') as image:header=image.read(24)
        assert header[:8]==b'\x89PNG\r\n\x1a\n' and struct.unpack('>II',header[16:24])==(1080,2400),'Expected original full native PNG'
        target=PUBLIC/f'assets/screenshots/v1.20/{key}-{locale}.png';pending.append((source,target))
        proof.append({'key':key,'locale':locale,'source':str(source.relative_to(ROOT)),'public':str(target.relative_to(ROOT)),'sha256':digest,'report':str(report_path.relative_to(ROOT)),'reportSha256':sha(report_path),'apkSha256':report['apk_sha256'],'screenshotsEdited':False})
        frozen[str(report_path.relative_to(ROOT))]=sha(report_path);frozen[str(source.relative_to(ROOT))]=digest
    old=(PUBLIC/'index.html').read_text();gallery=section(old,'companions');audio=section(old,'sound')
    preserved={name:sha(path) for name,path in payloads().items() if name!='index.html' and not name.startswith('assets/screenshots/v1.20/') and not name.startswith('assets/site-v120.') and '1.20' not in name}
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
    for asset in ('site-v120.js','site-v120.css'):
        path=STAGING/asset;frozen[str(path.relative_to(ROOT))]=sha(path)
    for name in ('prepare-site-v120.py','package-release-v120.py','check-site-v120.py','deploy-site-v120.py','verify-site-v120-publication.py'):
        path=ROOT/'scripts'/name;frozen[str(path.relative_to(ROOT))]=sha(path)
    evidence={'status':'PASS','edition':'1.20','release':release['version'],'artifact_manifest':str(manifest.relative_to(ROOT)),'artifact_manifest_sha256':sha(manifest),'artifact_source_commit':args.artifact_commit,'production_apk_sha256':release['apks']['production']['sha256'],'lab_apk_sha256':release['apks']['lab']['sha256'],'package_identity_verified':True,'native_capture_provenance_verified':True,'screenshots':proof,'preserved_files':preserved,'intentional_legal_updates':legal_updates,'gallery_markup_sha256':hashlib.sha256(gallery.encode()).hexdigest(),'audio_markup_sha256':hashlib.sha256(audio.encode()).hexdigest(),'frozen_inputs':frozen,'firebase_config_sha256':sha(ROOT/'firebase.json'),'network_verified':False,'deployed':False}
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
    print(json.dumps({'passed':True,'written':args.write,'edition':'1.20','screenshots':len(proof),'gallery_preserved':True,'account_files_preserved':True,'intentional_privacy_updates':len(legal_updates),'artifact_commit':args.artifact_commit,'deployed':False}))

if __name__=='__main__':main()
