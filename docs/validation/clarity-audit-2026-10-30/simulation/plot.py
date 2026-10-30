#!/usr/bin/env python3
"""Dependency-free SVG of the descriptive audit (no model/data mutation)."""
import html, json, pathlib

P=pathlib.Path(__file__).resolve().parent
A=json.loads((P/'analysis.json').read_text())
svg=['<svg xmlns="http://www.w3.org/2000/svg" width="1100" height="740" viewBox="0 0 1100 740">',
     '<rect width="1100" height="740" fill="#fff"/>',
     '<style>text{font-family:Arial,sans-serif;fill:#15283b}.title{font-size:25px;font-weight:bold}.small{font-size:14px}.axis{font-size:13px}.label{font-size:18px;font-weight:bold}</style>']
def text(x,y,s,cls='small',extra=''):
    svg.append(f'<text x="{x}" y="{y}" class="{cls}" {extra}>{html.escape(str(s))}</text>')
def line(x1,y1,x2,y2,c='#d9e0e8',w=1): svg.append(f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{c}" stroke-width="{w}"/>')
def frame(top,title,maxy):
    text(70,top-18,title,'label')
    for n in range(0,61,10):
        x=75+n/60*900;line(x,top,x,top+190);text(x,top+210,n,'axis','text-anchor="middle"')
    for n in range(0,maxy+1,10):
        y=top+190-n/maxy*190;line(75,y,975,y);text(62,y+4,f'{n} %','axis','text-anchor="end"')
    line(75,top+190,975,top+190,'#586e84');text(1040,top+210,'Niveau U','axis','text-anchor="middle"')
text(70,40,'Nouveau run : les pics restent U43 et U48','title')
text(70,66,'100 profils synthétiques × 30 répétitions par condition · graine 2026103001 · zéro participant humain')
frame(125,'Tentatives épuisées / tentatives commencées (U1–U60)',40)
for scenario,color,label in [('control_v121','#214f91','Témoin'),('cause_immediate','#d56920','Cause immédiate')]:
    pts=[]
    for n in range(1,61):
        v=A['scenarios'][scenario]['perLevel'][str(n)]; rate=v['exhaustionPerStart']['ratio']*100
        pts.append(f'{75+n/60*900:.2f},{125+190-rate/40*190:.2f}')
    svg.append(f'<polyline points="{" ".join(pts)}" fill="none" stroke="{color}" stroke-width="2.4"/>')
line(650,91,678,91,'#214f91',3);text(685,96,'Témoin')
line(790,91,818,91,'#d56920',3);text(825,96,'Cause immédiate')
for n,label,tx in [(43,'U43 : 456 / 1 382',570),(48,'U48 : 288 / 852',800)]:
    v=A['scenarios']['control_v121']['perLevel'][str(n)];x=75+n/60*900;y=315-v['exhaustionPerStart']['ratio']*100/40*190
    svg.append(f'<circle cx="{x}" cy="{y}" r="4" fill="#214f91"/>');text(tx,113,label)
text(70,366,'Dénominateurs variables : seules les trajectoires arrivées au niveau sont observées. Courbes descriptives, sans causalité humaine.')
frame(430,'Taps de coque / toutes les interactions du témoin',60)
pts=[]
for n in range(1,61):
    rate=A['scenarios']['control_v121']['perLevel'][str(n)]['shellShareAllInteractions']['ratio']*100
    pts.append(f'{75+n/60*900:.2f},{620-rate/60*190:.2f}')
svg.append(f'<polyline points="{" ".join(pts)}" fill="none" stroke="#567a58" stroke-width="2.4"/>')
line(240,430,240,620,'#8a5264',1.2)
text(250,556,'U11 : 3 types de coques + énergie')
text(250,578,'5 taps de coque par plateau complet sans automatisation')
text(70,690,'Coques : actions gratuites en budget, mais comptées dans les interactions. Le modèle reçoit leurs données structurées.')
text(70,713,'Source : analysis.json ; 21 000 trajectoires et 766 177 tentatives exécutées. Ni lisibilité graphique ni émotion mesurées.')
svg.append('</svg>')
(P/'difficulty-and-shell-burden.svg').write_text('\n'.join(svg)+'\n')
print(P/'difficulty-and-shell-burden.svg')
