import { test } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

// Executes the shipped scripts against the actual shipped markup. The generic
// DOM/event/clock double provides no map, key, language or camera logic of its
// own. This qualifies state transitions, not browser layout, native UI or Hosting.
const homeMarkup=fs.readFileSync(new URL('../public/index.html',import.meta.url),'utf8');
const homeScript=fs.readFileSync(new URL('../public/assets/site-v122.js',import.meta.url),'utf8');
const demoMarkup=fs.readFileSync(new URL('../public/lab/ui-1-22/index.html',import.meta.url),'utf8');
const demoScript=fs.readFileSync(new URL('../public/lab/ui-1-22/simulation.mjs',import.meta.url),'utf8');
const decode=text=>text.replace(/&amp;/g,'&').replace(/&quot;/g,'"').replace(/&#39;/g,"'").replace(/&lt;/g,'<').replace(/&gt;/g,'>');
const dataKey=name=>name.slice(5).replace(/-([a-z])/g,(_,c)=>c.toUpperCase());

class Element {
  constructor(env,tag,attrs={}){this.env=env;this.tagName=tag.toLowerCase();this.attrs={...attrs};this.dataset={};this.style={};this.children=[];this.parentNode=null;this.listeners=new Map();this._hidden=Object.hasOwn(attrs,'hidden');this._text='';this.value='';this.disabled=false;this.paused=true;
    for(const [key,value] of Object.entries(attrs))if(key.startsWith('data-'))this.dataset[dataKey(key)]=value;
    this.classList={contains:value=>this.className.split(/\s+/).includes(value),add:value=>{const set=new Set(this.className.split(/\s+/).filter(Boolean));set.add(value);this.className=[...set].join(' ');},remove:value=>{this.className=this.className.split(/\s+/).filter(x=>x!==value).join(' ');},toggle:(value,force)=>{const next=force??!this.classList.contains(value);next?this.classList.add(value):this.classList.remove(value);return next;}};
  }
  get className(){return this.attrs.class||'';}set className(value){this.attrs.class=String(value);}
  get hidden(){return this._hidden;}set hidden(value){this._hidden=Boolean(value);this.env.resize();}
  get textContent(){return this._text+this.children.map(child=>child.textContent).join('');}
  set textContent(value){this._text=String(value);this.children=[];}
  get href(){return this.attrs.href;}set href(value){this.attrs.href=String(value);}
  get src(){return this.attrs.src;}set src(value){this.attrs.src=String(value);}
  get alt(){return this.attrs.alt;}set alt(value){this.attrs.alt=String(value);}
  get id(){return this.attrs.id||'';}
  set innerHTML(markup){this.children=[];this._text='';parse(this.env,markup,this);}
  setAttribute(key,value){this.attrs[key]=String(value);if(key.startsWith('data-'))this.dataset[dataKey(key)]=String(value);}
  getAttribute(key){return this.attrs[key]??null;}
  append(...children){for(const item of children){const child=typeof item==='string'?{textContent:item}:item;child.parentNode=this;this.children.push(child);}}
  replaceChildren(...children){this.children=[];this._text='';this.append(...children);}
  addEventListener(type,callback){if(!this.listeners.has(type))this.listeners.set(type,[]);this.listeners.get(type).push(callback);}
  dispatch(type,values={}){const event={type,target:this,button:0,pointerId:1,preventDefault(){this.defaultPrevented=true;},...values};for(const callback of this.listeners.get(type)||[])callback(event);return event;}
  click(){if(!this.disabled)this.dispatch('click');}
  setPointerCapture(){}
  pause(){this.paused=true;this.pauseCalls=(this.pauseCalls||0)+1;}
  closest(selector){for(let node=this;node;node=node.parentNode)if(matches(node,selector))return node;return null;}
  querySelectorAll(selector){const result=[];function visit(node){for(const child of node.children||[]){if(child instanceof Element){if(matches(child,selector))result.push(child);visit(child);}}}visit(this);return result;}
  querySelector(selector){return this.querySelectorAll(selector)[0]||null;}
  getBoundingClientRect(){for(let node=this;node;node=node.parentNode)if(node.hidden)return {left:0,top:0,width:0,height:0};return {...(this.env.sizes[this.id]||{left:0,top:0,width:this.env.width,height:44})};}
}

function compound(node,selector){
  if(!(node instanceof Element))return false;
  for(const match of selector.matchAll(/\[([^\]=]+)(?:=(?:"([^"]*)"|'([^']*)'|([^\]]+)))?\]/g)){const [,key,a,b,c]=match;const value=a??b??c;if(node.getAttribute(key)===null||(value!==undefined&&node.getAttribute(key)!==value))return false;}
  const base=selector.replace(/\[[^\]]*\]/g,'');const tag=base.match(/^[A-Za-z][\w:-]*/)?.[0];if(tag&&node.tagName!==tag.toLowerCase())return false;
  for(const id of base.matchAll(/#([\w-]+)/g))if(node.id!==id[1])return false;
  for(const cls of base.matchAll(/\.([\w-]+)/g))if(!node.classList.contains(cls[1]))return false;return true;
}
function matches(node,selector){return selector.split(',').some(part=>{const chain=part.trim().split(/\s+/);if(!compound(node,chain.pop()))return false;let ancestor=node.parentNode;while(chain.length){const wanted=chain.pop();while(ancestor&&!compound(ancestor,wanted))ancestor=ancestor.parentNode;if(!ancestor)return false;ancestor=ancestor.parentNode;}return true;});}

function parse(env,markup,parent){
  const stack=[parent],voids=new Set(['area','base','br','col','embed','hr','img','input','link','meta','param','source','track','wbr']);let previous=0;
  const pattern=/<\/?([A-Za-z][\w:-]*)\b([^<>]*)>/g;let tag;
  while((tag=pattern.exec(markup))){const text=decode(markup.slice(previous,tag.index));if(text)stack.at(-1).append({textContent:text});previous=pattern.lastIndex;
    if(tag[0].startsWith('</')){if(stack.length>1)stack.pop();continue;}
    const attrs={};for(const item of tag[2].matchAll(/([:\w-]+)(?:\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s"'=<>]+)))?/g)){attrs[item[1]]=decode(item[2]??item[3]??item[4]??'');}
    const element=new Element(env,tag[1],attrs);stack.at(-1).append(element);if(!voids.has(element.tagName)&&!tag[0].endsWith('/>'))stack.push(element);
  }
  if(previous<markup.length)stack.at(-1).append({textContent:decode(markup.slice(previous))});
}

function boot(kind,{language='fr',width=736,reduced=false}={}){
  const env={width:width<420?width-28:width-44,now:0,next:0,frames:new Map(),observers:[],events:[],sizes:{}};
  env.sizes={'lc-viewport':{left:0,top:0,width:env.width,height:width<420?300:330},'lc-key-field':{left:0,top:0,width:env.width,height:268},'lc-minimap':{left:40,top:400,width:176,height:106}};
  env.resize=()=>{for(const observer of env.observers){const rect=observer.node.getBoundingClientRect();if(observer.width!==rect.width||observer.height!==rect.height){observer.width=rect.width;observer.height=rect.height;observer.callback([{contentRect:rect}]);}}};
  const document=new Element(env,'#document');parse(env,kind==='home'?homeMarkup:demoMarkup,document);document.getElementById=id=>document.querySelector('#'+id);document.createElement=tag=>new Element(env,tag);document.createElementNS=(_ns,tag)=>new Element(env,tag);document.createTextNode=text=>({textContent:text});document.documentElement=document.querySelector('html');document.body=document.querySelector('body');document.title=document.querySelector('title').textContent;
  let address='https://luma.example/'+(kind==='home'?'index.html':'lab/ui-1-22/index.html')+(language==='en'?'?lang=en':'');
  const location={get href(){return address;},set href(value){address=new URL(value,address).href;},get origin(){return new URL(address).origin;}};
  const window={matchMedia:()=>({matches:reduced}),LUMA_SITE_CONFIG:{},dispatchEvent:event=>env.events.push(event)};
  const sandbox={document,window,location,history:{replaceState(_state,_unused,value){location.href=value;}},URL,performance:{now:()=>env.now},requestAnimationFrame:callback=>{const id=++env.next;env.frames.set(id,callback);return id;},cancelAnimationFrame:id=>env.frames.delete(id),ResizeObserver:class{constructor(callback){this.callback=callback;}observe(node){env.observers.push({node,callback:this.callback});env.resize();}},CustomEvent:class{constructor(type,data){this.type=type;this.detail=data.detail;}}};
  vm.runInNewContext(kind==='home'?homeScript:demoScript,sandbox,{timeout:2000});
  env.document=document;env.location=location;env.el=id=>document.getElementById(id);env.advance=ms=>{const end=env.now+ms;while(env.now<end){env.now=Math.min(end,env.now+16);const pending=[...env.frames.values()];env.frames.clear();for(const frame of pending)frame(env.now);}return env;};
  return env;
}
const number=(env,id,field)=>Number(env.el(id).getAttribute(field));
const avatar=env=>[number(env,'lc-mini-avatar','cx'),number(env,'lc-mini-avatar','cy')];
const frame=env=>[number(env,'lc-mini-frame','x'),number(env,'lc-mini-frame','y')];
const choose=(env,level)=>{const select=env.el('lc-destination');select.value=String(level);select.dispatch('change');};

for(const language of ['fr','en'])test(`actual homepage ${language}: locale, every paired capture, exact download URLs`,()=>{
  const env=boot('home',{language}),doc=env.document;assert.equal(doc.documentElement.lang,language);
  for(const node of doc.querySelectorAll('[data-lang]'))assert.equal(node.hidden,node.dataset.lang!==language);
  const images=doc.querySelectorAll('[data-screenshot-fr]');assert.ok(images.length>=4);
  for(const image of images){assert.equal(image.src,language==='en'?image.dataset.screenshotEn:image.dataset.screenshotFr);assert.equal(image.alt,language==='en'?image.dataset.altEn:image.dataset.altFr);assert.match(image.src,new RegExp(`-${language}\\.png$`));}
  const downloads=doc.querySelectorAll('a[href]').filter(link=>link.href.endsWith('.apk'));assert.ok(downloads.length>=2);for(const link of downloads)assert.equal(new URL(link.href,env.location.href).search,'');
  const pending=doc.body.dataset.edition==='1.22-pending';assert.ok(downloads.some(link=>link.href.includes(pending?'1.21.0-rc1.apk':'1.22.0-rc1.apk')));assert.ok(doc.querySelectorAll('[data-set-lang]').find(button=>button.dataset.setLang===language).getAttribute('aria-pressed')==='true');
  if(pending){assert.ok(doc.getElementById('release-122-pending'));assert.ok(images.every(image=>image.src.includes('/v1.21/')));}else{assert.ok(images.every(image=>image.src.includes('/v1.22/')));assert.match(doc.title,/Luma Drift 1.22/);}
});

test('homepage language switch keeps binary identity and preserves selected route parameters',()=>{
  const env=boot('home'),links=env.document.querySelectorAll('a[href]').filter(link=>link.href.endsWith('.apk')),before=links.map(link=>link.href);env.document.querySelector('[data-set-lang="en"]').click();assert.deepEqual(links.map(link=>link.href),before);
  const demo=env.document.querySelectorAll('a[href]').find(link=>link.href.includes('/lab/ui-1-22/'));assert.equal(new URL(demo.href,env.location.href).searchParams.get('lang'),'en');env.document.querySelector('[data-set-lang="fr"]').click();assert.equal(new URL(demo.href,env.location.href).searchParams.has('lang'),false);
});

for(const language of ['fr','en'])test(`demo ${language}: travel follows path, camera explores independently and recenter works`,()=>{
  const env=boot('demo',{language}),initial=avatar(env);choose(env,9);assert.match(env.el('lc-map-status').textContent,language==='en'?/moving/:/en chemin/);env.advance(700);const mid=avatar(env);assert.notDeepEqual(mid,initial);env.advance(5000);const arrived=avatar(env);assert.notDeepEqual(arrived,mid);assert.match(env.el('lc-map-status').textContent,language==='en'?/Current level: 9/:/Niveau actuel : 9/);
  const followed=frame(env),mini=env.el('lc-minimap'),rect=mini.getBoundingClientRect();mini.dispatch('pointerdown',{clientX:rect.left+rect.width*.2,clientY:rect.top+rect.height*.8});mini.dispatch('pointerup');assert.deepEqual(avatar(env),arrived);assert.notDeepEqual(frame(env),followed);assert.match(env.el('lc-map-status').textContent,language==='en'?/free view/:/vue libre/);
  mini.dispatch('keydown',{key:'ArrowRight'});const free=frame(env);env.el('lc-viewport').dispatch('pointerdown',{clientX:100,clientY:100});env.el('lc-viewport').dispatch('pointermove',{clientX:180,clientY:160});env.el('lc-viewport').dispatch('pointerup');assert.notDeepEqual(frame(env),free);assert.deepEqual(avatar(env),arrived);
  env.el('lc-recenter').click();assert.deepEqual(frame(env),followed);assert.deepEqual(avatar(env),arrived);choose(env,11);env.advance(6000);assert.deepEqual(avatar(env),arrived);assert.equal(env.el('lc-destination').value,'9');assert.match(env.el('lc-map-status').textContent,language==='en'?/locked/:/verrouillée/);
});

for(const language of ['fr','en'])test(`demo ${language}: selecting a key only highlights; explicit use opens only its locks`,()=>{
  const env=boot('demo',{language}),initial=avatar(env);env.el('lc-key-tab').click();assert.equal(env.el('lc-key-panel').hidden,false);assert.equal(env.el('lc-map-panel').hidden,true);
  env.el('lc-key-b').click();assert.equal(env.el('lc-lock-b1').classList.contains('is-linked'),true);assert.equal(env.el('lc-lock-a1').classList.contains('is-linked'),false);assert.equal(env.el('lc-lock-b1').classList.contains('is-open'),false);
  env.el('lc-key-test').click();assert.equal(env.el('lc-key-runner').hidden,false);env.advance(300);assert.ok(parseFloat(env.el('lc-key-runner').style.left)>28);assert.equal(env.el('lc-lock-b1').classList.contains('is-open'),false);env.advance(500);assert.equal(env.el('lc-lock-b1').classList.contains('is-open'),true);assert.equal(env.el('lc-lock-a1').classList.contains('is-open'),false);
  env.el('lc-key-a').click();env.el('lc-key-test').click();env.advance(800);assert.equal(env.el('lc-lock-a1').classList.contains('is-open'),true);assert.equal(env.el('lc-lock-a2').classList.contains('is-open'),false);env.advance(800);assert.equal(env.el('lc-lock-a2').classList.contains('is-open'),true);assert.equal(env.el('lc-lock-b1').classList.contains('is-open'),true);assert.equal(env.el('lc-key-test').disabled,true);assert.deepEqual(avatar(env),initial);assert.match(env.el('lc-key-status').textContent,language==='en'?/path clear/:/passage libre/);
  env.el('lc-key-b').click();assert.equal(env.el('lc-lock-a1').classList.contains('is-open'),true);assert.equal(env.el('lc-key-test').disabled,true);
});

test('English demo has translated navigation labels and a language-bound return link',()=>{
  const env=boot('demo',{language:'en'}),doc=env.document;assert.equal(doc.documentElement.lang,'en');assert.equal(doc.querySelector('[role=tablist]').getAttribute('aria-label'),'Luma preview');assert.equal(env.el('lc-destination').getAttribute('aria-label'),'Choose the destination level');assert.equal(doc.querySelector('.lc-key-controls').getAttribute('aria-label'),'Choose a key');assert.equal(new URL(doc.querySelector('a.back').href,env.location.href).searchParams.get('lang'),'en');
  doc.querySelector('[data-language="fr"]').click();assert.equal(new URL(env.location.href).searchParams.get('lang'),'fr');
});

test('reduced motion and narrow viewport retain the same final mechanical state',()=>{
  const env=boot('demo',{language:'en',width:320,reduced:true});choose(env,8);env.advance(20);assert.match(env.el('lc-map-status').textContent,/Current level: 8/);assert.equal(number(env,'lc-mini-frame','width'),292);assert.equal(number(env,'lc-mini-frame','height'),300);
  env.el('lc-key-tab').click();env.el('lc-key-test').click();env.advance(40);assert.equal(env.el('lc-lock-a1').classList.contains('is-open'),true);assert.equal(env.el('lc-lock-a2').classList.contains('is-open'),true);assert.equal(env.el('lc-lock-b1').classList.contains('is-open'),false);
});
