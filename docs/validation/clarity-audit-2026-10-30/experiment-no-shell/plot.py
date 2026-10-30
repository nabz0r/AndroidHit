#!/usr/bin/env python3
"""Standalone SVG summary from the recorded no-shell comparison."""
import pathlib,json,html
P=pathlib.Path(__file__).resolve().parent
A=json.loads((P/'comparison.json').read_text())
variants=['control-disabled','no-shell-enabled']
z=[A['variants'][v]['scenarioResults']['cause_immediate'] for v in variants]
svg=['<svg xmlns="http://www.w3.org/2000/svg" width="1080" height="710" viewBox="0 0 1080 710">','<rect width="1080" height="710" fill="white"/>','<style>text{font-family:Arial,sans-serif;fill:#172d43}.title{font-size:25px;font-weight:bold}.head{font-size:18px;font-weight:bold}.small{font-size:14px}.value{font-size:16px}</style>']
def text(x,y,s,cls='small',extra=''):svg.append(f'<text x="{x}" y="{y}" class="{cls}" {extra}>{html.escape(str(s))}</text>')
colors=['#315d90','#c46e25']
text(55,42,'Sans coques : plus de progression, nouveau frein de ressource','title')
text(55,69,'Modèle privé · cause immédiate de blocage · mêmes 3 000 profil/répétition par flag · graine 2026103001')
for x,c,label in [(55,colors[0],'Coques actuelles'),(260,colors[1],'Sans coques')]:
 svg.append(f'<rect x="{x}" y="91" width="23" height="12" fill="{c}"/>');text(x+31,103,label)
panels=[(55,150,'Niveaux terminés / trajectoire',[t['summaryMeanMetrics']['meanLevelsCleared'] for t in z],50,'niveaux'),
        (570,150,'Interactions / trajectoire',[t['totals']['actions']/3000 for t in z],450,'interactions'),
        (55,365,'Arrêt au cap de 120 décisions / jour',[t['dailyStops'].get('day_action_cap',0)/24000*100 for t in z],15,'% des jours'),
        (570,365,'Arrêt énergie vide / jour',[t['dailyStops'].get('energy_empty',0)/24000*100 for t in z],25,'% des jours')]
for x,y,title,values,maximum,unit in panels:
 text(x,y,title,'head')
 for i,(v,c) in enumerate(zip(values,colors)):
  yy=y+25+i*52;width=350*v/maximum
  svg.append(f'<rect x="{x}" y="{yy}" width="350" height="25" fill="#eff3f7"/>')
  svg.append(f'<rect x="{x}" y="{yy}" width="{width:.3f}" height="25" fill="{c}"/>')
  text(x+363,yy+19,f'{v:.2f}'.replace('.',','),'value')
 text(x,y+139,'0')
 text(x+350,y+139,f'{maximum} {unit}','small','text-anchor="end"')
text(55,565,'Sondes sans aide : 9 423 / 12 000 → 8 474 / 12 000 (78,53 % → 70,62 %).','head')
text(55,590,'Les coques sont aussi retirées des sondes : cette baisse ne mesure pas un effet isolé sur l’apprentissage.')
text(55,625,'Le cap quotidien est administratif ; l’énergie suit le même registre modélisé dans les deux conditions.')
text(55,651,'Les taps, feedbacks, expositions et chemins changent. Aucune perception de sprites, émotion ou rétention humaine mesurée.')
text(55,680,'Source : comparison.json · fork non livré · contrôle désactivé : 6 CSV identiques en octets au run source.')
svg.append('</svg>');(P/'mechanical-comparison.svg').write_text('\n'.join(svg)+'\n')
print(P/'mechanical-comparison.svg')
