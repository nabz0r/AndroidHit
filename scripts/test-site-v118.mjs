import { test } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';
import { BoundedCache, LEVELS, POSES, validateSheet, sheetViewport, drawFrame } from '../public/assets/gallery-core-v118.mjs';

// Current 1.18 site contracts; historical test runners and reports remain unchanged.
// A narrow local DOM double is used. No browser or network is started.
const source=fs.readFileSync(new URL('../public/assets/site-v118.js',import.meta.url),'utf8');
class Element {
  constructor(attrs={},text=''){this.attrs={...attrs};this.dataset={};this.textContent=text;this.hidden=Object.hasOwn(attrs,'hidden');this.listeners={};this.children=[];
    for(const [key,value] of Object.entries(attrs))if(key.startsWith('data-'))this.dataset[key.slice(5).replace(/-([a-z])/g,(_,c)=>c.toUpperCase())]=value;
  }
  getAttribute(key){return this.attrs[key]??null;}
  setAttribute(key,value){this.attrs[key]=String(value);}
  addEventListener(type,fn){this.listeners[type]=fn;}
  replaceChildren(...children){this.children=[...children];}
  append(...children){this.children.push(...children);}
  set href(value){this.attrs.href=value;}
  get href(){return this.attrs.href;}
}
function boot({url='https://luma.example/index.html',hero=true,preparation=true}={}){
  const fr=new Element({'data-lang':'fr'}),en=new Element({'data-lang':'en',hidden:''});
  const frButton=new Element({'data-set-lang':'fr'}),enButton=new Element({'data-set-lang':'en'});
  const image=new Element({'data-screenshot-fr':'assets/screenshots/v1.18/home-fr.png','data-screenshot-en':'assets/screenshots/v1.18/home-en.png','data-alt-fr':'Accueil réel','data-alt-en':'Actual home'});
  const meta=new Element({'data-content-fr':'Description française','data-content-en':'English description'});
  const privacy=new Element({href:'privacy/index.html?view=full#main'}),external=new Element({href:'https://github.com/nabz0r/AndroidHit'}),apk=new Element({href:'/releases/luma-drift-1.18.0-prototype1.apk'});
  const notice=new Element(),publisher=new Element(),contact=new Element();
  const location={href:url};Object.defineProperty(location,'origin',{get(){return new URL(location.href).origin;}});
  const players=[new Element(),new Element(),new Element(),new Element()];players.forEach(player=>{player.paused=true;player.pause=()=>{player.paused=true;player.pauseCalls=(player.pauseCalls||0)+1;};});
  const document={body:{dataset:{edition:"1.18"}},documentElement:{lang:'fr'},title:hero?'Initial home':'Privacy original',
    querySelector(selector){assert.equal(selector,'#hero-title');return hero?{}:null;},
    querySelectorAll(selector){const items={'[data-preparation-notice]':[notice],'[data-publisher]':[publisher],'[data-contact]':[contact],'[data-lang]':[fr,en],'[data-set-lang]':[frButton,enButton],'a[href]':[privacy,external,apk],'[data-screenshot-fr]':[image],'[data-alt-fr]':[image],'[data-content-fr]':[meta],'audio[data-scene-preview], .audio-section audio':players};assert.ok(selector in items,'known actual selector '+selector);return items[selector];},
    createTextNode(text){return {textContent:text};},createElement(tag){assert.equal(tag,'a');return new Element();}
  };
  const events=[];const window={LUMA_SITE_CONFIG:{publisherName:'nabilux',supportEmail:'nabz0r+gaming@gmail.com',preparation},dispatchEvent(event){events.push(event);}};
  const history={replaceState(_state,_unused,destination){location.href=String(destination);}};
  vm.runInNewContext(source,{window,document,location,history,URL,CustomEvent:class{constructor(type,data){this.type=type;this.detail=data.detail;}}},{timeout:1000});
  return {fr,en,frButton,enButton,image,meta,privacy,external,apk,notice,publisher,contact,location,document,events,players};
}
test('French default exposes only French and exact current title',()=>{
  const s=boot();assert.equal(s.document.documentElement.lang,'fr');assert.equal(s.fr.hidden,false);assert.equal(s.en.hidden,true);
  assert.equal(s.frButton.attrs['aria-pressed'],'true');assert.equal(s.enButton.attrs['aria-pressed'],'false');
  assert.equal(s.document.title,'Luma Drift 1.18 — Petits compagnons. Grandes aventures.');
  assert.equal(s.meta.attrs.content,'Description française');assert.equal(s.image.src,'assets/screenshots/v1.18/home-fr.png');assert.equal(s.image.alt,'Accueil réel');
});
test('English URL switches real source, alt, metadata and pressed language',()=>{
  const s=boot({url:'https://luma.example/index.html?lang=en'});assert.equal(s.document.documentElement.lang,'en');assert.equal(s.fr.hidden,true);assert.equal(s.en.hidden,false);
  assert.equal(s.document.title,'Luma Drift 1.18 — Small companions. Bigger adventures.');
  assert.equal(s.image.src,'assets/screenshots/v1.18/home-en.png');assert.equal(s.image.alt,'Actual home');assert.equal(s.meta.attrs.content,'English description');
  assert.equal(s.enButton.attrs['aria-pressed'],'true');assert.equal(s.privacy.href,'/privacy/index.html?view=full&lang=en#main');
});
test('Switching back keeps unrelated route state and removes only language parameter',()=>{
  const s=boot({url:'https://luma.example/index.html?view=all&lang=en#gameplay'});s.frButton.listeners.click();
  const url=new URL(s.location.href);assert.equal(url.searchParams.get('view'),'all');assert.equal(url.searchParams.has('lang'),false);assert.equal(url.hash,'#gameplay');
  assert.equal(s.privacy.href,'/privacy/index.html?view=full#main');assert.equal(s.document.documentElement.lang,'fr');assert.equal(s.events.at(-1).type,'luma-language');assert.equal(s.events.at(-1).detail,'fr');
});
test('Same-origin APK download and GitHub addresses never acquire a language parameter',()=>{
  const s=boot({url:'https://luma.example/index.html?lang=en'});
  assert.equal(s.external.href,'https://github.com/nabz0r/AndroidHit');
  assert.equal(s.apk.href,'/releases/luma-drift-1.18.0-prototype1.apk');
});
test('Legal page does not inherit the product-page title',()=>{
  const s=boot({hero:false,url:'https://luma.example/privacy/index.html?lang=en'});assert.equal(s.document.title,'Privacy original');
  s.frButton.listeners.click();assert.equal(s.document.title,'Privacy original');assert.equal(s.document.documentElement.lang,'fr');
});
test('Exact support plus-tag is an encoded mailto and remains a text node',()=>{
  const s=boot();assert.equal(s.publisher.children[0].textContent,'nabilux · ');
  assert.equal(s.publisher.children[1].href,'mailto:nabz0r%2Bgaming@gmail.com');assert.equal(s.publisher.children[1].textContent,'nabz0r+gaming@gmail.com');
  assert.match(s.contact.children[1].href,/^mailto:nabz0r%2Bgaming@gmail\.com\?subject=/);
});
test('Existing qualification control is preserved',()=>{
  assert.equal(boot({preparation:true}).notice.hidden,false);assert.equal(boot({preparation:false}).notice.hidden,true);
});
test('Unsupported requested language keeps the French site default',()=>{
  const s=boot({url:'https://luma.example/index.html?lang=de'});assert.equal(s.document.documentElement.lang,'fr');assert.equal(new URL(s.location.href).searchParams.has('lang'),false);
});

test('A deliberate preview stops another preview without autoplaying anything',()=>{
 const s=boot();assert.equal(s.players.every(player=>player.paused),true);s.players[0].paused=false;s.players[2].paused=false;s.players[2].listeners.play();assert.equal(s.players[0].paused,true);assert.equal(s.players[0].pauseCalls,1);assert.equal(s.players[2].paused,false);
});
test('Staged product has four accessible manually started audio sources',()=>{
 const html=fs.readFileSync(new URL('../public/index.html',import.meta.url),'utf8');const players=[...html.matchAll(/<audio\b([^>]+)>/g)];assert.equal(players.length,4);players.forEach(([,attrs])=>{assert.match(attrs,/\bcontrols\b/);assert.match(attrs,/preload="none"/);assert.match(attrs,/aria-labelledby="audio-[a-z]+-title"/);assert.doesNotMatch(attrs,/autoplay/);});assert.equal([...html.matchAll(/<source src="assets\/audio\/v1\.17\/scene-[a-z]+-preview\.wav"/g)].length,4);
});

test('Gallery does not download artwork or metadata before a deliberate choice',()=>{
 const gallery=fs.readFileSync(new URL('../public/assets/companion-gallery-v118.mjs',import.meta.url),'utf8');
 assert.match(gallery,/let opened = false/);assert.match(gallery,/if \(!opened\) return/);
 assert.doesNotMatch(gallery,/IntersectionObserver|preload|setInterval|requestAnimationFrame/);
 assert.match(gallery,/new BoundedCache\(4\)/);
 const html=fs.readFileSync(new URL('../public/index.html',import.meta.url),'utf8');
 assert.doesNotMatch(html,/<(?:img|link)[^>]+companions-v118\/[^>]+\.webp/);
 assert.match(html,/data-gallery-load/);assert.match(html,/data-gallery-status role="status" aria-live="polite"/);
});

test('Roster exposes exactly37 identities, four form metadata paths each and no initial image URLs',()=>{
 const rows=JSON.parse(fs.readFileSync(new URL('../public/assets/companions-v118/roster.json',import.meta.url),'utf8'));
 assert.equal(rows.length,37);assert.equal(new Set(rows.map(r=>r.id)).size,37);
 assert.equal(rows.filter(r=>r.guest).length,13);assert.deepEqual(LEVELS,[1,3,6,10]);
 rows.forEach(row=>{assert.equal(row.forms.length,4);row.forms.forEach((form,i)=>{assert.equal(form.level,LEVELS[i]);assert.match(form.metadata,new RegExp(row.id+'-evo'+String(LEVELS[i]).padStart(2,'0')+'-poses\\.json$'));});});
 assert.equal(POSES.fr.length,20);assert.equal(POSES.en.length,20);
});

test('Image cache deduplicates concurrent requests, bounds references and permits retry',async()=>{
 const cache=new BoundedCache(4);let calls=0;
 const a=cache.get('same',async()=>{calls++;return 7});const b=cache.get('same',async()=>{calls++;return 9});
 assert.deepEqual(await Promise.all([a,b]),[7,7]);assert.equal(calls,1);
 for(let n=0;n<6;n++)await cache.get(String(n),()=>n);
 assert.equal(cache.values.size,4);assert.equal(cache.values.has('same'),false);
 await assert.rejects(cache.get('broken',()=>{throw new Error('expected')}));
 assert.equal(await cache.get('broken',()=>10),10);
});

test('SourceClip is applied before the original source rectangle is drawn',()=>{
 const frames=Array.from({length:20},(_,poseIndex)=>({poseIndex,sourceRect:[10,20,210,320],originX:100,originY:300,pixelsPerUnit:100,sourceClip:[[10,20],[210,20],[160,320],[10,320]]}));
 const sheet=validateSheet({frames});const calls=[];const context=new Proxy({}, {get(_target,key){return(...args)=>calls.push([key,...args]);}});
 drawFrame(context,{},sheet,8);assert.ok(calls.findIndex(c=>c[0]==='clip')<calls.findIndex(c=>c[0]==='drawImage'));
 const draw=calls.find(c=>c[0]==='drawImage');assert.deepEqual(draw.slice(2,6),[10,20,200,300]);
 assert.equal(calls.filter(c=>c[0]==='lineTo').length,3);assert.equal(calls.at(-1)[0],'restore');
 const view=sheetViewport(sheet);assert.ok(view.scale>0);assert.equal(sheetViewport(sheet).scale,view.scale);
 const invalid=structuredClone(sheet);invalid.frames[9].sourceClip[1][0]=211;assert.throws(()=>validateSheet(invalid));
});

test('Every local gallery frame preserves the exact source domain and contains no garment rewrite',()=>{
 const rows=JSON.parse(fs.readFileSync(new URL('../public/assets/companions-v118/roster.json',import.meta.url),'utf8'));
 let frames=0;
 for(const row of rows)for(const form of row.forms){
  const sheet=validateSheet(JSON.parse(fs.readFileSync(new URL('../public/'+form.metadata,import.meta.url),'utf8')));
  assert.equal(sheet.id,row.id);assert.equal(sheet.levelMilestone,form.level);frames+=sheet.frames.length;
  assert.equal(sheet.frames.some(frame=>'attachments' in frame),false);
 }
 assert.equal(frames,2960);
});
