#!/usr/bin/env python3
"""Original, deterministic SVG brand art. Uses only Python's standard library."""
from pathlib import Path
from math import cos, sin, pi

OUT = Path(__file__).resolve().parent
INK, WHITE, GOLD, MINT, LILAC, PEACH = '#14182E', '#F7F5FF', '#FFE29A', '#7EE8D1', '#A99AFF', '#FFB398'

def points(radius=1):
    return ' '.join(f'{cos(-pi/2+i*pi/5)*(1.12 if i%2==0 else .81)*radius:.3f},{sin(-pi/2+i*pi/5)*(1.12 if i%2==0 else .81)*radius:.3f}' for i in range(10))

def defs():
    return f'''<defs>
  <linearGradient id="night" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#11142B"/><stop offset=".6" stop-color="#252342"/><stop offset="1" stop-color="#0E2935"/></linearGradient>
  <radialGradient id="nebula"><stop stop-color="{LILAC}" stop-opacity=".26"/><stop offset="1" stop-color="{LILAC}" stop-opacity="0"/></radialGradient>
  <radialGradient id="glow"><stop stop-color="{GOLD}" stop-opacity=".25"/><stop offset="1" stop-color="{GOLD}" stop-opacity="0"/></radialGradient>
  <linearGradient id="nova" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#FFF2CE"/><stop offset=".48" stop-color="{GOLD}"/><stop offset="1" stop-color="#DDBE83"/></linearGradient>
  <linearGradient id="mint" x1="0" y1="0" x2=".8" y2="1"><stop stop-color="#B6F6E6"/><stop offset="1" stop-color="{MINT}"/></linearGradient>
  <linearGradient id="peach" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#FFD4BC"/><stop offset="1" stop-color="{PEACH}"/></linearGradient>
  <linearGradient id="lilac" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#D9CDFF"/><stop offset="1" stop-color="{LILAC}"/></linearGradient>
  <path id="arrow" d="M0 16V-16 M-12 -4L0 -16 12 -4" fill="none" stroke="{INK}" stroke-width="6" stroke-linecap="round" stroke-linejoin="round"/>
  <g id="nova-face">
    <polygon points="{points(100)}" fill="url(#nova)"/>
    <circle r="83" fill="url(#nova)"/>
    <ellipse cx="-33.5" cy="-15.5" rx="9.5" ry="15.5" fill="{INK}"/>
    <ellipse cx="33.5" cy="-15.5" rx="9.5" ry="15.5" fill="{INK}"/>
    <circle cx="-31" cy="-22" r="4" fill="{WHITE}"/>
    <circle cx="36" cy="-22" r="4" fill="{WHITE}"/>
    <ellipse cx="-51" cy="23.5" rx="14" ry="6.5" fill="#EE6F92" fill-opacity=".39"/>
    <ellipse cx="51" cy="23.5" rx="14" ry="6.5" fill="#EE6F92" fill-opacity=".39"/>
    <path d="M-17 28 Q0 48 17 28" fill="none" stroke="{INK}" stroke-width="4.5" stroke-linecap="round"/>
    <ellipse cx="-29" cy="-63.5" rx="26" ry="11.5" fill="{WHITE}" fill-opacity=".47" transform="rotate(-14 -29 -63.5)"/>
  </g>
</defs>'''

def stars(width, height, count):
    elements=[]
    for i in range(count):
        x,y=(i*137+37)%width,(i*211+53)%height
        # Small, deterministic stars stay quiet behind the title and character.
        radius=2 if i%9==0 else 1
        elements.append(f'<circle cx="{x}" cy="{y}" r="{radius}" fill="{WHITE}" fill-opacity="{.18 if i%2 else .32}"/>')
    return '\n'.join(elements)

def tile(x,y,size,color,rotation=0,direction=0,switch=False):
    r=size/2
    return f'''<g transform="translate({x} {y}) rotate({rotation})">
 <rect x="{-r}" y="{-r+6}" width="{size}" height="{size}" rx="{size*.21}" fill="#080B1B" fill-opacity=".5"/>
 <rect x="{-r}" y="{-r}" width="{size}" height="{size}" rx="{size*.21}" fill="url(#{color})"/>
 <rect x="{-r+2}" y="{-r+2}" width="{size-4}" height="{size-4}" rx="{size*.19}" fill="none" stroke="{WHITE}" stroke-opacity=".25"/>
 <use href="#arrow" transform="rotate({direction}) scale({size/80})"/>
 {f'<circle cx="{size*.29}" cy="{-size*.28}" r="{size*.16}" fill="{INK}"/><circle cx="{size*.29}" cy="{-size*.28}" r="{size*.11}" fill="{MINT}"/>' if switch else ''}
</g>'''

def svg(width,height,body,label):
    return f'''<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="{width}" height="{height}" viewBox="0 0 {width} {height}" role="img" aria-label="{label}">
{defs()}
{body}
</svg>\n'''

banner=f'''<rect width="1024" height="500" fill="url(#night)"/>
<ellipse cx="770" cy="248" rx="400" ry="330" fill="url(#nebula)"/>
{stars(1024,500,50)}
<path d="M578 417C1004 537 1065 74 723 48" fill="none" stroke="{MINT}" stroke-opacity=".15" stroke-width="1.5"/>
<path d="M654 443C992 421 979 153 648 118" fill="none" stroke="{LILAC}" stroke-opacity=".2" stroke-width="1.5"/>
<text x="64" y="173" font-family="Arial Black,Arial,sans-serif" font-size="101" font-weight="900" letter-spacing="-3" fill="{WHITE}">LUMA</text>
<text x="66" y="263" font-family="Arial Black,Arial,sans-serif" font-size="82" font-weight="900" letter-spacing="7" fill="{MINT}">DRIFT</text>
<rect x="68" y="300" width="44" height="4" rx="2" fill="{LILAC}"/>
<text x="68" y="345" font-family="Arial,sans-serif" font-size="22" fill="#D2D2E8">A little light. A clever escape.</text>
<rect x="67" y="383" width="281" height="37" rx="18.5" fill="{MINT}" fill-opacity=".1" stroke="{MINT}" stroke-opacity=".25"/>
<text x="88" y="407" font-family="Arial,sans-serif" font-size="13" font-weight="700" letter-spacing="1.6" fill="{MINT}">AN ORIGINAL GATE PUZZLE</text>
<ellipse cx="802" cy="275" rx="215" ry="190" fill="url(#glow)"/>
<ellipse cx="792" cy="382" rx="112" ry="19" fill="#0A0D21" fill-opacity=".55"/>
{tile(646,123,76,'mint',-12,0,True)}
{tile(938,112,66,'lilac',12,90)}
{tile(932,385,78,'peach',-10,180)}
{tile(647,389,62,'lilac',13,270)}
<g transform="translate(801 262) rotate(6) scale(1.21)"><use href="#nova-face"/></g>
<path d="M897 235l5 13 13 5-13 5-5 13-5-13-13-5 13-5Z" fill="{WHITE}" fill-opacity=".8"/>
<path d="M724 394l3 8 8 3-8 3-3 8-3-8-8-3 8-3Z" fill="{MINT}"/>
'''

icon=f'''<rect width="512" height="512" fill="url(#night)"/>
<ellipse cx="240" cy="181" rx="340" ry="310" fill="url(#nebula)"/>
{stars(512,512,25)}
<circle cx="253" cy="259" r="219" fill="none" stroke="{LILAC}" stroke-opacity=".13" stroke-width="2"/>
<path d="M51 317C-2 94 332-7 452 176" fill="none" stroke="{MINT}" stroke-opacity=".28" stroke-width="2"/>
<ellipse cx="256" cy="437" rx="143" ry="25" fill="#090D21" fill-opacity=".6"/>
<ellipse cx="251" cy="281" rx="240" ry="231" fill="url(#glow)"/>
<g transform="translate(252 280) rotate(-4) scale(1.58)"><use href="#nova-face"/></g>
{tile(397,113,91,'mint',12,0,True)}
<path d="M87 383l6 16 16 6-16 6-6 16-6-16-16-6 16-6Z" fill="{MINT}"/>
<path d="M395 397l3 8 8 3-8 3-3 8-3-8-8-3 8-3Z" fill="{LILAC}"/>
'''

# AppKit's SVG loader supports xlink:href; include both forms for other renderers.
for name,width,height,body,label in [('luma-drift-feature',1024,500,banner,'Luma Drift: original celestial puzzle brand art'),('luma-drift-icon',512,512,icon,'Nova and an arrow tile on a celestial background')]:
    art=svg(width,height,body,label).replace('href="#','xlink:href="#')
    (OUT/f'{name}.svg').write_text(art,encoding='utf-8')
    print(f'Wrote {name}.svg ({width} x {height})')
