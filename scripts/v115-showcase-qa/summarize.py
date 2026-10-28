#!/usr/bin/env python3
"""Summarize only completed, independently retained native showcase evidence."""
from pathlib import Path
import json,hashlib
ROOT=Path(__file__).resolve().parents[2]
BASE=ROOT/'docs/validation/v1.15'
SHA='07b5bfc2632edc3d34f7583ac71c8343b52d7b926ed526aeb7a88599253df0f9'
paths={'bootstrap':BASE/'relay-native-showcase-bootstrap100/host-report.json','motion':BASE/'ui-bugs/showcase-reduce-motion/report.json','panels':BASE/'ui-bugs/ui-sdk-final-e-panels/host-report.json','showcase':BASE/'ui-bugs/ui-sdk-final-e-showcase/host-report.json'}
r={name:json.loads(p.read_text()) for name,p in paths.items()}
for name,value in r.items():
 assert value['passed'],name
 assert value['serial']=='emulator-5588' and value['avd']=='Luma-Refonte115-Showcase-QA',name
 assert value['apk_sha256']==SHA,name
assert r['bootstrap']['native']['completed']==100
for name in ['panels','showcase']:
 assert r[name]['display_restored'] and r[name]['isolated_test_package_absent'] and r[name]['exact_candidate_retained'],name
check=json.loads((paths['showcase'].parent/'showcase/showcase-checkpoint.json').read_text())
assert check['initialCoins']-check['economy']['coins']==1620
assert check['spentCoins']==1620
output=BASE/'showcase-final-e';assert not output.exists();output.mkdir()
shots={}
for id in ['delivery-house-garden-fr','delivery-adventure-fr','delivery-play-fr']:
 key='showcaseCold/'+id+'.png';p=ROOT/'docs/screenshots/v1.15/ui-bugs/ui-sdk-final-e-showcase'/key
 digest=hashlib.sha256(p.read_bytes()).hexdigest();assert digest==r['showcase']['screenshots'][key]
 shots[id]={'path':str(p.relative_to(ROOT)),'sha256':digest}
summary={'passed':True,'candidate':'production 1.15.0-rc1 / code17','apk_sha256':SHA,'scope':'Separate fresh showcase profile after100 real U victories; distinct from the primary1000-level qualification profile','avd':'Luma-Refonte115-Showcase-QA','serial':'emulator-5588','bootstrapped_real_victories':100,'earned_victory_coins':sum(x['coinDelta'] for x in r['bootstrap']['native']['levels']),'bootstrap_checks':r['bootstrap']['native']['checkCount'],'bootstrap_actual_touches':r['bootstrap']['native']['touchCount'],'reduced_motion_observed_before':r['motion']['observed_before'],'reduced_motion_observed_after':r['motion']['observed_after'],'reduced_motion_observed_after_cold':r['motion']['observed_cold'],'panel_checks':sum(x['native']['checkCount'] for x in r['panels']['phases']),'showcase_checks':sum(x['native']['checkCount'] for x in r['showcase']['phases']),'initial_coins':check['initialCoins'],'real_furniture_spend':1620,'coins_after_purchases':check['economy']['coins'],'spark_stock_after_one_real_activation':check['inventory']['booster_hint'],'exact_garden_layout':check['preferences']['home_layout_v115_garden'],'native_showcase_processes':[x['native']['processId'] for x in r['showcase']['phases']],'reports':{name:str(p.relative_to(ROOT)) for name,p in paths.items()},'delivery_originals':shots,'limits':'Emulator API36 arm64 16KB pages, reduced motion; no physical device or human comprehension/playtime inference; no synthetic balance, stock, stars or layouts'}
summary['report_sha256']={name:hashlib.sha256(p.read_bytes()).hexdigest() for name,p in paths.items()}
(output/'summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n')
text='''# Captures natives finales — profil Showcase distinct\n\nCes images proviennent du deuxième profil QA neuf, après **100 victoires réelles U1–U100** dans la production E `1.15.0-rc1` (code17). Elles ne représentent pas le profil du parcours principal U1–U1000. Les récompenses des 100 victoires ont gagné 5500 LumaCoins ; aucun solde, stock, niveau ou placement n’a été injecté.\n\n**Réduire les animations est activé** : contrôle natif observé Off→On, puis On après relance froide. Les panneaux ont aussi été vérifiés en 320dp avec restauration explicite du format d’écran. Une charge Étincelle possédée a réellement été consommée, avec reçu durable et indice observé, sans modifier l’historique des pièces ni le budget.\n\nL’aquarium 420, le piano 560 et la serre 640 ont été **réellement achetés pour 1620 LumaCoins**, puis placés par les contrôles de l’application dans le Jardin. Propriété, disposition exacte, solde et stock ont été contrôlés après relance froide.\n\n'''
text+=f"Solde avant achats : {check['initialCoins']} ; après : {check['economy']['coins']} LumaCoins. APK SHA-256 : `{SHA}`.\n\n"
captions={'delivery-house-garden-fr':'Maison/Jardin — profil QA neuf après 100 victoires ; aquarium, piano et serre réellement payés et placés.','delivery-adventure-fr':'Aventure — progression réellement gagnée jusqu’au niveau 101 sur ce profil QA distinct.','delivery-play-fr':'Jeu — vrai niveau 101 en français ; aucune pièce touchée pour cette capture.'}
for id,shot in shots.items():text+=f"- [{id}]({ROOT/shot['path']}) : {captions[id]}\n"
text+='\nLes PNG sont les originaux natifs non retouchés. Leurs empreintes et celles des rapports sont conservées dans summary.json. La preuve concerne un émulateur API36 arm64/16KB ; elle ne remplace pas un essai sur téléphone physique.\n'
(output/'README.md').write_text(text)
print(json.dumps({'passed':True,'summary':str(output/'summary.json'),'coins_after':summary['coins_after_purchases'],'showcase_checks':summary['showcase_checks']},ensure_ascii=False))
