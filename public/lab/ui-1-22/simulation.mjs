const lang=new URL(location.href).searchParams.get('lang')==='en'?'en':'fr';
const tr=(fr,en)=>lang==='en'?en:fr;
document.documentElement.lang=lang;
const copy={
'independent':['DÉMO SCHÉMATIQUE INDÉPENDANTE','INDEPENDENT SCHEMATIC DEMO'],
'title':['Un compagnon. Une caméra. Des liens.','A companion. A camera. Connections.'],
'intro':['Explore les mouvements et les associations. Ce n’est ni une capture Android ni la partie native complète. Les niveaux et les serrures sont fictifs ; aucun compte, gain ou fichier Android ne change.','Explore movement and associations. This is neither an Android capture nor the full native game. Levels and locks are fictional; no account, reward or Android file changes.'],
'movement':['Aperçu de mouvement','Movement preview'],'destination':['Destination','Destination'],'drag':['Glisser : déplacer la vue','Drag to move the view'],'mini':['Minicarte · flèches au clavier','Minimap · arrow keys'],'scope':['Démo interactive · aucune progression réelle','Interactive demo · no real progress'],'back':['Retour au jeu et aux APK','Back to the game and APKs'],
'lc-map-tab':['Carte','Map'],'lc-key-tab':['Clés','Keys'],'lc-recenter':['Mon compagnon','My companion'],'lc-key-a':['Clé A','Key A'],'lc-key-b':['Clé B','Key B'],'lc-key-test':['Tester la clé','Test the key']};
document.querySelectorAll('[data-copy]').forEach(el=>{const item=copy[el.dataset.copy];if(item)el.textContent=item[lang==='en'?1:0];});
document.querySelectorAll('[data-language]').forEach(button=>{button.setAttribute('aria-pressed',String(button.dataset.language===lang));button.addEventListener('click',()=>{const url=new URL(location.href);url.searchParams.set('lang',button.dataset.language);location.href=url.pathname+url.search;});});
document.title=tr('Luma Drift1.22 · Démo schématique','Luma Drift1.22 · Schematic demo');
for(const [id,fr,en] of [['lc-map-title','Chemin des niveaux de Luma','Luma level path'],['lc-map-desc','Choisir un niveau déplace le compagnon. Glisser déplace uniquement la caméra.','Choosing a level moves the companion. Dragging moves only the camera.'],['lc-key-title','Clés et serrures associées','Keys and associated locks'],['lc-key-desc','Clé A : serrures A1 et A2. Clé B : serrure B1. La sélection ne joue aucun coup.','Key A: locks A1 and A2. Key B: lock B1. Selection does not play a move.']])document.getElementById(id).textContent=tr(fr,en);
document.getElementById('lc-minimap').setAttribute('aria-label',tr('Minicarte. Cliquer ou utiliser les flèches pour déplacer uniquement la caméra.','Minimap. Click or use arrow keys to move only the camera.'));
document.querySelector('#luma-carte-vivante [role=tablist]').setAttribute('aria-label',tr('Aperçu Luma','Luma preview'));
document.getElementById('lc-destination').setAttribute('aria-label',tr('Choisir le niveau de destination','Choose the destination level'));
document.querySelector('.lc-key-controls').setAttribute('aria-label',tr('Choisir une clé','Choose a key'));
document.querySelectorAll('a.back,a.brand').forEach(link=>{if(lang==='en')link.href='/index.html?lang=en';});

(() => {
  const root = document.getElementById('luma-carte-vivante');
  const el = id => root.querySelector('#lc-' + id);
  const world = { width:1200, height:720 };
  const points = [
    {x:100,y:580},{x:245,y:600},{x:355,y:480},{x:230,y:350},
    {x:390,y:250},{x:550,y:320},{x:690,y:450},{x:850,y:370},
    {x:980,y:250},{x:835,y:140},{x:665,y:105},{x:1070,y:90}
  ];
  const samples = [], stops = [0];
  let distance = 0;
  for (let segment=0; segment<points.length-1; segment++) {
    const p0=points[Math.max(0,segment-1)],p1=points[segment],p2=points[segment+1],p3=points[Math.min(points.length-1,segment+2)];
    for (let step=segment===0?0:1;step<=30;step++) {
      const t=step/30,t2=t*t,t3=t2*t;
      const calc=k=>.5*((2*p1[k])+(-p0[k]+p2[k])*t+(2*p0[k]-5*p1[k]+4*p2[k]-p3[k])*t2+(-p0[k]+3*p1[k]-3*p2[k]+p3[k])*t3);
      const p={x:calc('x'),y:calc('y')};
      if(samples.length) distance+=Math.hypot(p.x-samples[samples.length-1].x,p.y-samples[samples.length-1].y);
      samples.push({...p,distance});
    }
    stops.push(distance);
  }
  const path=samples.map((p,i)=>(i?'L':'M')+p.x.toFixed(1)+','+p.y.toFixed(1)).join(' ');
  el('route').setAttribute('d',path);
  el('travelled').setAttribute('d',path);
  el('mini-route').setAttribute('d',path);
  points.forEach((p,index)=>{
    const node=document.createElement('button');
    node.type='button';node.className='btn lc-node';node.dataset.level=String(index+1);
    if(index>=10){node.innerHTML='<span aria-hidden="true">🔒</span><span>'+(index+1)+'</span>';node.setAttribute('aria-disabled','true');}else{node.textContent=String(index+1);}
    node.setAttribute('aria-label',tr('Rejoindre le niveau ','Reach level ')+(index+1));
    node.addEventListener('click',()=>travel(index+1));el('nodes').append(node);
    const option=document.createElement('option');option.value=String(index+1);option.textContent=tr('Niveau ','Level ')+(index+1)+(index>=10?tr(' · verrouillé',' · locked'):'');el('destination').append(option);
    const circle=document.createElementNS('http://www.w3.org/2000/svg','circle');circle.setAttribute('cx',p.x);circle.setAttribute('cy',p.y);circle.setAttribute('r','11');el('mini-nodes').append(circle);
  });
  const reduced=window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const state={current:4,target:4,position:stops[3],camera:{x:0,y:0},following:true,key:'a',opened:[],tab:'map'};
  let size={width:0,height:330},mapAnimation=0,keyAnimation=0,moving=false,testing=false,drag=null;
  const clamp=(value,min,max)=>Math.max(min,Math.min(max,value));
  function positionAt(d) {
    const index=samples.findIndex(p=>p.distance>=d);
    if(index<=0)return {...samples[0]};
    const a=samples[index-1],b=samples[index],t=(d-a.distance)/Math.max(.00001,b.distance-a.distance);
    return {x:a.x+(b.x-a.x)*t,y:a.y+(b.y-a.y)*t};
  }
  function clampCamera(){state.camera.x=clamp(state.camera.x,0,Math.max(0,world.width-size.width));state.camera.y=clamp(state.camera.y,0,Math.max(0,world.height-size.height));}
  function centerOnCompanion(){const p=positionAt(state.position);state.camera.x=p.x-size.width/2;state.camera.y=p.y-size.height/2;clampCamera();}
  function drawMap(){
    if(!size.width)return;
    clampCamera();const p=positionAt(state.position),camera=state.camera;
    el('world').setAttribute('transform','translate('+(-camera.x)+','+(-camera.y)+')');
    el('travelled').setAttribute('stroke-dasharray',state.position+' '+(distance+1));
    root.querySelectorAll('.lc-node').forEach((node,index)=>{
      const point=points[index],x=point.x-camera.x,y=point.y-camera.y;
      node.style.left=x+'px';node.style.top=y+'px';node.hidden=x<24||x>size.width-24||y<24||y>size.height-24;
      node.setAttribute('aria-pressed',String(index+1===state.current));
      node.setAttribute('aria-label',tr('Niveau ','Level ')+(index+1)+(index>=10?tr(' · verrouillé',' · locked'):index+1===state.current?tr(' · actuel',' · current'):tr(' · rejoindre',' · reach')));
    });
    el('avatar').style.left=(p.x-camera.x)+'px';el('avatar').style.top=(p.y-camera.y-17)+'px';
    el('avatar').hidden=p.x-camera.x<-30||p.x-camera.x>size.width+30||p.y-camera.y<0||p.y-camera.y>size.height+70;
    el('mini-frame').setAttribute('x',camera.x);el('mini-frame').setAttribute('y',camera.y);el('mini-frame').setAttribute('width',Math.min(world.width,size.width));el('mini-frame').setAttribute('height',size.height);
    el('mini-avatar').setAttribute('cx',p.x);el('mini-avatar').setAttribute('cy',p.y);
    el('region').textContent=camera.x<400?tr('Jardin des lueurs','Glow garden'):camera.x<750?tr('Sentier des mécanismes','Mechanism path'):tr('Crête des étoiles','Star ridge');
  }
  function mapStatus(){el('map-status').textContent=tr('Niveau actuel : ','Current level: ')+state.current+tr(' · Destination : ',' · Destination: ')+(moving?state.target:state.current)+(moving?tr(' · en chemin',' · moving'):state.following?tr(' · caméra sur le compagnon',' · camera follows companion'):tr(' · vue libre',' · free view'));el('destination').value=String(state.target);}

  function save(){}
  function travel(level){
    if(level>=11){el('map-status').textContent=tr('Niveau actuel : ','Current level: ')+state.current+tr(' · destination ',' · destination ')+level+tr(' verrouillée · compagnon immobile',' locked · companion stationary');el('destination').value=String(state.target);return;}
    cancelAnimationFrame(mapAnimation);const start=state.position,end=stops[level-1];state.target=level;state.following=true;moving=Math.abs(end-start)>.5;
    if(!moving){state.current=level;centerOnCompanion();drawMap();mapStatus();save();return;}
    const began=performance.now(),duration=reduced?0:clamp(Math.abs(end-start)*2.2,900,4200);mapStatus();
    function frame(now){const t=duration?clamp((now-began)/duration,0,1):1,ease=t*t*(3-2*t);state.position=start+(end-start)*ease;if(state.following)centerOnCompanion();drawMap();if(t<1){mapAnimation=requestAnimationFrame(frame);}else{state.current=level;moving=false;mapStatus();save();}}
    mapAnimation=requestAnimationFrame(frame);
  }
  el('destination').addEventListener('change',event=>travel(Number(event.target.value)));
  el('recenter').addEventListener('click',()=>{state.following=true;centerOnCompanion();drawMap();mapStatus();save();});
  const viewport=el('viewport');
  viewport.addEventListener('pointerdown',event=>{if(event.target.closest('button')||event.button!==0)return;drag={x:event.clientX,y:event.clientY,camera:{...state.camera}};state.following=false;viewport.setPointerCapture(event.pointerId);viewport.classList.add('is-dragging');mapStatus();});
  viewport.addEventListener('pointermove',event=>{if(!drag)return;state.camera.x=drag.camera.x-(event.clientX-drag.x);state.camera.y=drag.camera.y-(event.clientY-drag.y);drawMap();});
  function endDrag(){if(!drag)return;drag=null;viewport.classList.remove('is-dragging');save();}
  viewport.addEventListener('pointerup',endDrag);viewport.addEventListener('pointercancel',endDrag);
  const mini=el('minimap');let miniDragging=false;
  function miniPan(event){const rect=mini.getBoundingClientRect();state.following=false;state.camera.x=(event.clientX-rect.left)/rect.width*world.width-size.width/2;state.camera.y=(event.clientY-rect.top)/rect.height*world.height-size.height/2;drawMap();mapStatus();}
  mini.addEventListener('pointerdown',event=>{if(event.button!==0)return;miniDragging=true;mini.setPointerCapture(event.pointerId);miniPan(event);});
  mini.addEventListener('pointermove',event=>{if(miniDragging)miniPan(event);});
  function endMini(){if(miniDragging){miniDragging=false;save();}}
  mini.addEventListener('pointerup',endMini);mini.addEventListener('pointercancel',endMini);
  mini.addEventListener('keydown',event=>{const moves={ArrowLeft:[-90,0],ArrowRight:[90,0],ArrowUp:[0,-90],ArrowDown:[0,90]};if(!moves[event.key])return;event.preventDefault();state.following=false;state.camera.x+=moves[event.key][0];state.camera.y+=moves[event.key][1];drawMap();mapStatus();save();});
  new ResizeObserver(entries=>{const rect=entries[0].contentRect;if(!rect.width)return;size={width:rect.width,height:rect.height};el('map-svg').setAttribute('viewBox','0 0 '+size.width+' '+size.height);if(state.following)centerOnCompanion();drawMap();}).observe(viewport);
  let keyWidth=0;
  const locks={a1:{group:'a',x:.34,y:86},a2:{group:'a',x:.65,y:86},b1:{group:'b',x:.5,y:198}};
  function keyPosition(key){return {x:28,y:key==='a'?86:198};}
  function drawKeys(){
    const width=el('key-field').getBoundingClientRect().width;if(!width)return;keyWidth=width;
    el('key-svg').setAttribute('viewBox','0 0 '+width+' 268');
    ['a','b'].forEach(key=>{const y=key==='a'?86:198,p=el('key-path-'+key);p.setAttribute('d','M 28 '+y+' L '+(width-86)+' '+y);p.classList.toggle('is-linked',key===state.key||Object.keys(locks).filter(id=>locks[id].group===key).every(id=>state.opened.includes(id)));p.classList.toggle('lc-route-b',key==='b');const dock=el('dock-'+key);dock.style.left='28px';dock.style.top=y+'px';el('passage-'+key).style.top=y+'px';el('passage-'+key).textContent=tr('Passage ','Path ')+key.toUpperCase()+(Object.keys(locks).filter(id=>locks[id].group===key).every(id=>state.opened.includes(id))?tr(' libre',' clear'):'');});
    Object.entries(locks).forEach(([id,lock])=>{const node=el('lock-'+id),opened=state.opened.includes(id);node.style.left=(lock.x*(width-90)+20)+'px';node.style.top=lock.y+'px';node.classList.toggle('is-linked',lock.group===state.key);node.classList.toggle('is-open',opened);node.setAttribute('aria-label',tr('Serrure ','Lock ')+id.toUpperCase()+' · '+(opened?tr('ouverte','open'):tr('fermée','closed'))+tr(' · clé ',' · key ')+lock.group.toUpperCase());});
    ['a','b'].forEach(key=>el('key-'+key).setAttribute('aria-pressed',String(state.key===key)));
    const targets=Object.keys(locks).filter(id=>locks[id].group===state.key&&!state.opened.includes(id));el('key-test').disabled=testing||!targets.length;el('key-test').textContent=testing?tr('En déplacement…','Moving…'):targets.length?tr('Tester la clé','Test the key'):tr('Passage dégagé','Path cleared');
    if(!testing)el('key-status').textContent=tr('Clé ','Key ')+state.key.toUpperCase()+tr(' sélectionnée · ',' selected · ')+(targets.length?targets.map(id=>id.toUpperCase()).join(tr(' et ',' and '))+tr(' associée'+(targets.length>1?'s':''),' linked'):tr('ses serrures sont ouvertes','its locks are open'));
  }
  ['a','b'].forEach(key=>el('key-'+key).addEventListener('click',()=>{if(testing)return;state.key=key;drawKeys();save();}));
  el('key-test').addEventListener('click',()=>{
    if(testing)return;const key=state.key,targets=Object.keys(locks).filter(id=>locks[id].group===key&&!state.opened.includes(id));if(!targets.length)return;
    testing=true;['a','b'].forEach(k=>el('key-'+k).disabled=true);drawKeys();el('key-runner').hidden=false;
    let from=keyPosition(key),targetIndex=0;
    function next(){
      const id=targets[targetIndex],target=locks[id],to={x:target.x*(keyWidth-90)+20,y:target.y},started=performance.now(),duration=reduced?0:700;
      el('key-status').textContent=tr('Clé ','Key ')+key.toUpperCase()+tr(' → serrure ',' → lock ')+id.toUpperCase();
      function frame(now){const t=duration?clamp((now-started)/duration,0,1):1,e=t*t*(3-2*t);el('key-runner').style.left=(from.x+(to.x-from.x)*e)+'px';el('key-runner').style.top=(from.y+(to.y-from.y)*e)+'px';if(t<1){keyAnimation=requestAnimationFrame(frame);}else{state.opened.push(id);drawKeys();from=to;targetIndex++;if(targetIndex<targets.length){next();}else{testing=false;el('key-runner').hidden=true;['a','b'].forEach(k=>el('key-'+k).disabled=false);drawKeys();el('key-status').textContent=tr('Clé ','Key ')+key.toUpperCase()+' · '+targets.map(v=>v.toUpperCase()).join(tr(' et ',' and '))+tr(' ouverte'+(targets.length>1?'s':''),' open')+tr(' · passage libre',' · path clear');save();}}}
      keyAnimation=requestAnimationFrame(frame);
    }
    next();
  });
  new ResizeObserver(()=>drawKeys()).observe(el('key-field'));
  root.querySelectorAll('[role="tab"]').forEach(tab=>tab.addEventListener('click',()=>{state.tab=tab.id==='lc-key-tab'?'keys':'map';root.querySelectorAll('[role=tab]').forEach(item=>{const active=item===tab;item.setAttribute('aria-selected',String(active));item.classList.toggle('active',active);document.getElementById(item.getAttribute('aria-controls')).hidden=!active;});save();}));

  mapStatus();drawKeys();
})();

