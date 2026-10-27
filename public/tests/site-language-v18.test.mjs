import { test } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

// Current site.js contracts. The historical filename is retained; 1.8 reports stay immutable.
// A narrow local DOM double is used. No browser or network is started.
const source=fs.readFileSync(new URL('../assets/site.js',import.meta.url),'utf8');
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
  const image=new Element({'data-screenshot-fr':'assets/screenshots/v1.8/home-fr.png','data-screenshot-en':'assets/screenshots/v1.8/home-en.png','data-alt-fr':'Accueil réel','data-alt-en':'Actual home'});
  const meta=new Element({'data-content-fr':'Description française','data-content-en':'English description'});
  const privacy=new Element({href:'privacy/index.html?view=full#main'}),external=new Element({href:'https://github.com/nabz0r/AndroidHit'}),apk=new Element({href:'https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.8.0-rc1.apk'});
  const notice=new Element(),publisher=new Element(),contact=new Element();
  const location={href:url};Object.defineProperty(location,'origin',{get(){return new URL(location.href).origin;}});
  const document={documentElement:{lang:'fr'},title:hero?'Initial home':'Privacy original',
    querySelector(selector){assert.equal(selector,'#hero-title');return hero?{}:null;},
    querySelectorAll(selector){const items={'[data-preparation-notice]':[notice],'[data-publisher]':[publisher],'[data-contact]':[contact],'[data-lang]':[fr,en],'[data-set-lang]':[frButton,enButton],'a[href]':[privacy,external,apk],'[data-screenshot-fr]':[image],'[data-alt-fr]':[image],'[data-content-fr]':[meta]};assert.ok(selector in items,'known actual selector '+selector);return items[selector];},
    createTextNode(text){return {textContent:text};},createElement(tag){assert.equal(tag,'a');return new Element();}
  };
  const events=[];const window={LUMA_SITE_CONFIG:{publisherName:'nabilux',supportEmail:'nabz0r+gaming@gmail.com',preparation},dispatchEvent(event){events.push(event);}};
  const history={replaceState(_state,_unused,destination){location.href=String(destination);}};
  vm.runInNewContext(source,{window,document,location,history,URL,CustomEvent:class{constructor(type,data){this.type=type;this.detail=data.detail;}}},{timeout:1000});
  return {fr,en,frButton,enButton,image,meta,privacy,external,apk,notice,publisher,contact,location,document,events};
}
test('French default exposes only French and exact current title',()=>{
  const s=boot();assert.equal(s.document.documentElement.lang,'fr');assert.equal(s.fr.hidden,false);assert.equal(s.en.hidden,true);
  assert.equal(s.frButton.attrs['aria-pressed'],'true');assert.equal(s.enButton.attrs['aria-pressed'],'false');
  assert.equal(s.document.title,'Luma Drift 1.9 — Un monde tout doux. Chaque coup compte.');
  assert.equal(s.meta.attrs.content,'Description française');assert.equal(s.image.src,'assets/screenshots/v1.8/home-fr.png');assert.equal(s.image.alt,'Accueil réel');
});
test('English URL switches real source, alt, metadata and pressed language',()=>{
  const s=boot({url:'https://luma.example/index.html?lang=en'});assert.equal(s.document.documentElement.lang,'en');assert.equal(s.fr.hidden,true);assert.equal(s.en.hidden,false);
  assert.equal(s.document.title,'Luma Drift 1.9 — Soft little worlds. Every move matters.');
  assert.equal(s.image.src,'assets/screenshots/v1.8/home-en.png');assert.equal(s.image.alt,'Actual home');assert.equal(s.meta.attrs.content,'English description');
  assert.equal(s.enButton.attrs['aria-pressed'],'true');assert.equal(s.privacy.href,'/privacy/index.html?view=full&lang=en#main');
});
test('Switching back keeps unrelated route state and removes only language parameter',()=>{
  const s=boot({url:'https://luma.example/index.html?view=all&lang=en#gameplay'});s.frButton.listeners.click();
  const url=new URL(s.location.href);assert.equal(url.searchParams.get('view'),'all');assert.equal(url.searchParams.has('lang'),false);assert.equal(url.hash,'#gameplay');
  assert.equal(s.privacy.href,'/privacy/index.html?view=full#main');assert.equal(s.document.documentElement.lang,'fr');assert.equal(s.events.at(-1).type,'luma-language');assert.equal(s.events.at(-1).detail,'fr');
});
test('Download and GitHub addresses never acquire an account or language parameter',()=>{
  const s=boot({url:'https://luma.example/index.html?lang=en'});
  assert.equal(s.external.href,'https://github.com/nabz0r/AndroidHit');
  assert.equal(s.apk.href,'https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.8.0-rc1.apk');
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
