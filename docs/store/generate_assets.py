#!/usr/bin/env python3
"""Deterministic, original Luma 1.5 vector artwork; Python standard library only."""
from html import escape
from math import cos, pi, sin
from pathlib import Path
import json

OUT = Path(__file__).resolve().parent
NIGHT, CARD, INK = '#12142E', '#252944', '#14182E'
IVORY, MINT, PEACH, LILAC, GOLD = '#F7F5FF', '#99ECDA', '#FFB194', '#BAA5FF', '#FFE29A'
SPECS = [
    dict(stem='luma-drift-repo-banner', width=1600, height=640, alpha=False, role='Repository hero'),
    dict(stem='luma-drift-feature', width=1024, height=500, alpha=False, role='Proposed store feature graphic'),
    dict(stem='luma-drift-icon', width=512, height=512, alpha=True, role='Proposed store icon'),
    dict(stem='nova-companion', width=640, height=640, alpha=True, role='Transparent site companion'),
    dict(stem='brand-scene', width=1440, height=720, alpha=False, role='Text-free site illustration'),
]


def star_points(radius, inner=.47):
    return ' '.join(f'{cos(-pi/2+i*pi/5)*radius*(1 if i%2==0 else inner):.3f},{sin(-pi/2+i*pi/5)*radius*(1 if i%2==0 else inner):.3f}' for i in range(10))


def definitions():
    nova = ' '.join(f'{cos(-pi/2+i*pi/5)*(112 if i%2==0 else 81):.3f},{sin(-pi/2+i*pi/5)*(112 if i%2==0 else 81):.3f}' for i in range(10))
    return f'''<defs>
 <linearGradient id="night" x1="0" y1="0" x2="1" y2="1"><stop stop-color="{NIGHT}"/><stop offset=".56" stop-color="#292641"/><stop offset="1" stop-color="#19323C"/></linearGradient>
 <linearGradient id="nova" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#FFF0C6"/><stop offset=".5" stop-color="{GOLD}"/><stop offset="1" stop-color="#E8CA8A"/></linearGradient>
 <radialGradient id="halo"><stop stop-color="{LILAC}" stop-opacity=".23"/><stop offset="1" stop-color="{LILAC}" stop-opacity="0"/></radialGradient>
 <path id="arrow" d="M0 18V-18 M-13 -5L0 -18 13 -5" fill="none" stroke="{INK}" stroke-width="6.5" stroke-linecap="round" stroke-linejoin="round"/>
 <g id="padlock" fill="none" stroke="{GOLD}" stroke-width="4" stroke-linecap="round"><path d="M-12 -3V-11C-12 -28 12 -28 12 -11V-3"/><rect x="-17" y="-3" width="34" height="28" rx="6" fill="{GOLD}" stroke="none"/><path d="M0 6V14" stroke="{CARD}"/><circle cy="6" r="3" fill="{CARD}" stroke="none"/></g>
 <g id="nova-face">
  <polygon points="{nova}" fill="url(#nova)"/><circle r="83" fill="url(#nova)"/>
  <ellipse cx="-33.5" cy="-15.5" rx="9.5" ry="15.5" fill="{INK}"/><ellipse cx="33.5" cy="-15.5" rx="9.5" ry="15.5" fill="{INK}"/>
  <circle cx="-31" cy="-22" r="4" fill="{IVORY}"/><circle cx="36" cy="-22" r="4" fill="{IVORY}"/>
  <ellipse cx="-51" cy="23.5" rx="14" ry="6.5" fill="#EE6F92" fill-opacity=".39"/><ellipse cx="51" cy="23.5" rx="14" ry="6.5" fill="#EE6F92" fill-opacity=".39"/>
  <path d="M-17 28Q0 48 17 28" fill="none" stroke="{INK}" stroke-width="4.5" stroke-linecap="round"/>
  <ellipse cx="-29" cy="-63.5" rx="26" ry="11.5" fill="{IVORY}" fill-opacity=".47"/>
 </g>
</defs>'''


def stars(width, height, count=36):
    return ''.join(f'<circle cx="{(i*137+37)%width}" cy="{(i*211+53)%height}" r="{1.8 if i%7==0 else 1}" fill="{IVORY}" fill-opacity="{.23 if i%2 else .11}"/>' for i in range(count))


def backdrop(width, height):
    return f'<rect width="{width}" height="{height}" fill="url(#night)"/><ellipse cx="{width*.76}" cy="{height*.44}" rx="{width*.42}" ry="{height*.7}" fill="url(#halo)"/>'+stars(width,height)


def text(x, y, words, size=22, color=IVORY, bold=False, spacing=0):
    family='Arial Black,Arial,sans-serif' if bold else 'Arial,sans-serif'
    return f'<text x="{x}" y="{y}" font-family="{family}" font-size="{size}" font-weight="{900 if bold else 400}" letter-spacing="{spacing}" fill="{color}">{escape(words)}</text>'


def pill(x, y, width, label, color=MINT):
    return f'<rect x="{x}" y="{y}" width="{width}" height="44" rx="22" fill="{color}" fill-opacity=".09" stroke="{color}" stroke-opacity=".22"/>'+text(x+20,y+28,label,17,color,True)


def tile(x, y, size=96, color=MINT, direction=0, rotation=0, key=False, label=True):
    half=size/2
    badge = (f'<rect x="{-half+3}" y="{-half+3}" width="{size*.37}" height="{size*.27}" rx="{size*.055}" fill="{GOLD}"/>'+text(-half+size*.07,-half+size*.23,'K1',size*.19,INK,True)) if key and label else ''
    if key and not label:
        badge=f'<circle cx="{-size*.28}" cy="{-size*.28}" r="{size*.095}" fill="{GOLD}" stroke="{INK}" stroke-width="2"/><path d="M{-size*.22} {-size*.22}L{-size*.1} {-size*.1}" stroke="{INK}" stroke-width="3"/>'
    return f'<g transform="translate({x} {y}) rotate({rotation})"><rect x="{-half}" y="{-half+6}" width="{size}" height="{size}" rx="{size*.22}" fill="#090C21" fill-opacity=".5"/><rect x="{-half}" y="{-half}" width="{size}" height="{size}" rx="{size*.22}" fill="{color}"/><path d="M{-half+size*.24} {-half+3}H{half-size*.24}" stroke="{IVORY}" stroke-opacity=".28" stroke-width="2"/><use href="#arrow" transform="rotate({direction}) scale({size/92})"/>{badge}</g>'


def wall(x,y,size=96,label=True):
    offset=-size*.06 if label else 0
    return f'<g transform="translate({x} {y})"><rect x="{-size/2}" y="{-size/2}" width="{size}" height="{size}" rx="{size*.22}" fill="#3B3544" stroke="{GOLD}" stroke-width="2.5"/><use href="#padlock" transform="translate(0 {offset}) scale({size/94})"/>{text(-size*.14,size*.36,"K1",size*.19,GOLD,True) if label else ""}</g>'


def companion(x,y,scale=1):
    # Keep use references one level deep for AppKit's SVG renderer.
    sparks=''.join(f'<polygon points="{star_points(7)}" fill="{MINT}" transform="translate({sx} {sy})"/>' for sx,sy in [(120,0),(-60,98),(-60,-98)])
    return f'''<g transform="translate({x} {y}) scale({scale})">
<ellipse rx="125" ry="113" fill="none" stroke="{MINT}" stroke-opacity=".35" stroke-width="3.5"/>{sparks}<use href="#nova-face"/>
<path d="M0 -88V-113" stroke="{MINT}" stroke-width="7" stroke-linecap="round"/><ellipse cx="10.5" cy="-115" rx="12.5" ry="11" fill="{MINT}"/>
<path d="M76 59V88" stroke="{GOLD}" stroke-width="3.5" stroke-linecap="round"/>
<path d="M76 105C48 84 66 76 76 88C86 76 104 84 76 105Z" fill="none" stroke="{PEACH}" stroke-width="3.5" stroke-linejoin="round"/>
</g>'''


def repo_banner():
    return backdrop(1600,640)+f'''
<path d="M918 143C1098 -15 1594 80 1488 446S1101 654 939 499" fill="none" stroke="{MINT}" stroke-opacity=".12" stroke-width="2"/>
<path d="M1003 530C1353 631 1578 227 1197 81" fill="none" stroke="{LILAC}" stroke-opacity=".19" stroke-width="2"/>
{pill(92,61,226,'LUMA DRIFT · 1.5',LILAC)}
{text(87,229,'LUMA',126,IVORY,True,-4)}
{text(92,345,'DRIFT',112,MINT,True,5)}
{text(96,408,'A small light. A clever escape.',29)}
{text(97,453,'Read the arrows. Plan the escape.',23,'#BCBED5')}
{pill(96,501,230,'1,000 Journey boards')}{pill(342,501,186,'240 Remix')}
{text(97,592,'18 equipment items · 6 sealed-passage prototypes',20,'#C8C9DD')}
{tile(1007,159,106,PEACH,0,-9,True)}
<path d="M1080 129C1225 56 1415 77 1451 159" fill="none" stroke="{GOLD}" stroke-opacity=".42" stroke-width="2.5" stroke-dasharray="6 9"/>
{wall(1450,220,112)}
{tile(1438,464,96,LILAC,270,9)}
{tile(980,504,80,MINT,0,-8)}
{companion(1210,338,1.86)}
'''


def feature():
    # The feature tells the puzzle story; the separate icon is the close-up portrait.
    return backdrop(1024,500)+f'''
<path d="M513 352C573 110 883 50 970 283" fill="none" stroke="{MINT}" stroke-opacity=".16" stroke-width="2"/>
{text(62,105,'LUMA',56,IVORY,True,-1)}{text(256,105,'DRIFT',48,MINT,True,2)}
{text(64,194,'Every arrow',48,IVORY,True,-1)}
{text(64,250,'has its moment.',44,IVORY,True,-1)}
{text(66,310,'Read the gates. Release the key.',21,'#D0D0E3')}
<rect x="66" y="347" width="58" height="4" rx="2" fill="{PEACH}"/>
{text(66,399,'A small light. A clever escape.',20,MINT)}
{tile(646,120,104,PEACH,0,-6,True)}
<path d="M711 102C822 53 932 113 939 178" fill="none" stroke="{GOLD}" stroke-opacity=".45" stroke-width="2" stroke-dasharray="5 7"/>
{wall(923,214,104)}
{tile(904,405,82,LILAC,270,8)}
{companion(740,312,1.25)}
'''


def icon():
    return backdrop(512,512)+f'<ellipse cx="252" cy="427" rx="150" ry="25" fill="#090C21" fill-opacity=".3"/>'+companion(250,284,1.64)+tile(405,110,84,PEACH,0,9,False)


def scene():
    return backdrop(1440,720)+f'''
<path d="M157 517C283 607 448 622 581 483S1011 76 1251 183" fill="none" stroke="{MINT}" stroke-opacity=".18" stroke-width="3"/>
<path d="M212 550C400 338 438 61 730 109S965 635 1275 554" fill="none" stroke="{LILAC}" stroke-opacity=".2" stroke-width="2"/>
{tile(236,226,152,MINT,0,-8)}
{tile(450,497,136,LILAC,90,8)}
{tile(1034,164,138,PEACH,0,7,True,False)}
<path d="M1113 162C1225 145 1265 253 1246 325" fill="none" stroke="{GOLD}" stroke-opacity=".5" stroke-width="3" stroke-dasharray="7 10"/>
{wall(1247,408,152,False)}
{tile(1045,591,110,MINT,270,-7)}
{companion(737,366,2.13)}
'''


def svg(spec,body):
    label=spec['role']+' — original Luma Drift 1.5 vector artwork'
    w,h=spec['width'],spec['height']
    return f'<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="{w}" height="{h}" viewBox="0 0 {w} {h}" role="img" aria-label="{escape(label)}">\n<title>{escape(label)}</title>\n{definitions()}\n{body}\n</svg>\n'.replace('href="#','xlink:href="#')


if __name__=='__main__':
    bodies=[repo_banner(),feature(),icon(),companion(320,326,2.05),scene()]
    for spec,body in zip(SPECS,bodies):
        (OUT/(spec['stem']+'.svg')).write_text(svg(spec,body),encoding='utf-8')
        print('Wrote',spec['stem']+'.svg',f"({spec['width']} × {spec['height']})")
    # Shared export contract: the renderer and verifier consume these dimensions.
    (OUT/'asset-specs.json').write_text(json.dumps({'palette':dict(night=NIGHT,card=CARD,mint=MINT,peach=PEACH,lilac=LILAC,ivory=IVORY,gold=GOLD),'assets':SPECS},indent=2)+'\n')
