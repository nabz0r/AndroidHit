import { test } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

// Current 1.19 language/runtime contract. Historical gallery and 1.19 HTML contracts run separately.
// A narrow local DOM double is used. No browser or network is started.
const source=fs.readFileSync(new URL('../public/assets/site-v119.js',import.meta.url),'utf8');
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
  const image=new Element({'data-screenshot-fr':'assets/screenshots/v1.19/home-fr.png','data-screenshot-en':'assets/screenshots/v1.19/home-en.png','data-alt-fr':'Accueil réel','data-alt-en':'Actual home'});
  const meta=new Element({'data-content-fr':'Description française','data-content-en':'English description'});
  const privacy=new Element({href:'privacy/index.html?view=full#main'}),external=new Element({href:'https://github.com/nabz0r/AndroidHit'}),apk=new Element({href:'/releases/luma-drift-1.19.0-prototype2.apk'});
  const notice=new Element(),publisher=new Element(),contact=new Element();
  const location={href:url};Object.defineProperty(location,'origin',{get(){return new URL(location.href).origin;}});
  const players=[new Element(),new Element(),new Element(),new Element()];players.forEach(player=>{player.paused=true;player.pause=()=>{player.paused=true;player.pauseCalls=(player.pauseCalls||0)+1;};});
  const document={body:{dataset:{edition:"1.19"}},documentElement:{lang:'fr'},title:hero?'Initial home':'Privacy original',
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
  assert.equal(s.document.title,'Luma Drift 1.19 — Ton équipe. Ton prochain déclic.');
  assert.equal(s.meta.attrs.content,'Description française');assert.equal(s.image.src,'assets/screenshots/v1.19/home-fr.png');assert.equal(s.image.alt,'Accueil réel');
});
test('English URL switches real source, alt, metadata and pressed language',()=>{
  const s=boot({url:'https://luma.example/index.html?lang=en'});assert.equal(s.document.documentElement.lang,'en');assert.equal(s.fr.hidden,true);assert.equal(s.en.hidden,false);
  assert.equal(s.document.title,'Luma Drift 1.19 — Your squad. Your next move.');
  assert.equal(s.image.src,'assets/screenshots/v1.19/home-en.png');assert.equal(s.image.alt,'Actual home');assert.equal(s.meta.attrs.content,'English description');
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
  assert.equal(s.apk.href,'/releases/luma-drift-1.19.0-prototype2.apk');
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

const currentHtml=()=>fs.readFileSync(new URL('../public/index.html',import.meta.url),'utf8');
test('Current homepage selects the 1.19 runtime and every advertised navigation destination exists',()=>{
 const html=currentHtml();assert.match(html,/data-edition="1\.19"/);assert.match(html,/assets\/site-v119\.js\?v=[a-f0-9]+/);assert.match(html,/assets\/site-v119\.css\?v=[a-f0-9]+/);
 assert.doesNotMatch(html,/<script[^>]+assets\/site-v118\.js/);
 for(const id of ['main','squad','screens','companions','sound','download'])assert.match(html,new RegExp('\\bid="'+id+'"'));
 assert.match(html,/href="privacy\/index\.html"/);assert.match(html,/href="delete-account\/index\.html"/);
});

test('Current native screenshots switch as exactly three paired 1.19 FR/EN views',()=>{
 const pairs=[...currentHtml().matchAll(/<img\b[^>]*data-screenshot-fr="([^"]+)"[^>]*data-screenshot-en="([^"]+)"[^>]*>/g)];
 const fr=new Set(),en=new Set();assert.ok(pairs.length>=3);
 for(const [tag,french,english] of pairs){
  assert.match(french,/^assets\/screenshots\/v1\.19\/(home|squad|puzzle)-fr\.png$/);
  assert.equal(english,french.replace('-fr.png','-en.png'));
  assert.match(tag,/data-alt-fr="[^"]+"/);assert.match(tag,/data-alt-en="[^"]+"/);
  assert.match(tag,/width="1080"/);assert.match(tag,/height="2400"/);fr.add(french);en.add(english);
 }
 assert.equal(fr.size,3);assert.equal(en.size,3);
});

test('Squad copy explains once-only energy, two-role combo and local save limits in both languages',()=>{
 const html=currentHtml();assert.equal([...html.matchAll(/class="role-card"/g)].length,6);
 for(const text of ['3 points d’énergie au départ','Start with 3 energy','Un talent par compagnon','One skill per companion','À deux, Résonance','Two roles, Resonance','compagnons à rencontrer','companions to meet','La connexion Google ne synchronise pas cette sauvegarde.','Google sign-in does not synchronize this save.'])assert.ok(html.includes(text),text);
 assert.match(html,/prochain plateau/);assert.match(html,/next board/);
});

test('Only the signed current APK is primary and provider availability remains explicit',()=>{
 const html=currentHtml();const primary=[...html.matchAll(/<a class="button primary" href="([^"]+)"/g)];
 const manifestLink=html.match(/href="(\/releases\/manifest-1\.19\.0-prototype2\.json)"/);assert.ok(manifestLink,'exact accepted prototype2 metadata link');
 // Read only the small public record: no APK download, signature execution or native claim here.
 const manifest=JSON.parse(fs.readFileSync(new URL('../public'+manifestLink[1],import.meta.url),'utf8'));
 assert.equal(manifest.edition,'1.19');assert.equal(manifest.version,'1.19.0-prototype2');assert.equal(manifest.version_code,27);
 for(const channel of ['production','lab']){
  const entry=manifest.apks[channel];assert.equal(entry.code,27);assert.ok(Number.isSafeInteger(entry.bytes)&&entry.bytes>0);assert.match(entry.sha256,/^[a-f0-9]{64}$/);
  assert.equal(entry.signer_certificate_sha256,'dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287');
  assert.ok(html.includes(entry.sha256),'current '+channel+' hash follows independently prepared record');
 }
 assert.equal(manifest.apks.production.version,'1.19.0-prototype2');assert.equal(manifest.apks.lab.version,'1.19.0-sandbox-prototype2');
 assert.equal(primary.length,2);primary.forEach(([,href])=>assert.equal(href,'/releases/'+manifest.apks.production.path.split('/').at(-1)));
 assert.ok(html.includes('/releases/'+manifest.apks.lab.path.split('/').at(-1)));
 assert.match(html,/Annonces de test/);assert.match(html,/Test ads/);assert.match(html,/achats réels et tirages premium payants désactivés/i);assert.match(html,/real checkout and paid premium draws disabled/i);
});

test('Current homepage retains four manually started audio previews and a deliberate gallery load',()=>{
 const html=currentHtml(),players=[...html.matchAll(/<audio\b([^>]+)>/g)];assert.equal(players.length,4);
 for(const [,attrs] of players){assert.match(attrs,/\bcontrols\b/);assert.match(attrs,/preload="none"/);assert.match(attrs,/aria-labelledby="audio-[a-z]+-title"/);assert.doesNotMatch(attrs,/autoplay/);}
 assert.equal([...html.matchAll(/<source src="assets\/audio\/v1\.17\/scene-[a-z]+-preview\.wav"/g)].length,4);
 assert.match(html,/data-gallery-load/);assert.match(html,/data-gallery-status role="status" aria-live="polite"/);
 assert.doesNotMatch(html,/<(?:img|link)[^>]+companions-v118\/[^>]+\.webp/);
});
