import assert from 'node:assert/strict';
import {writeFile,mkdir} from 'node:fs/promises';
import {createHash} from 'node:crypto';
import {fixture,purchase} from '../../server/test/helpers.js';
import {MemoryStore} from '../../server/src/store.js';
import {SKUS} from '../../server/src/catalog.js';
let checks=0,apiCalls=0,actionGroups=0,lostReplies=0,refundDebts=0,reconstructed=0;
const family={};const seed=Number(process.argv[3]||2026103103),sessions=Number(process.argv[4]||10000),steps=Number(process.argv[5]||24),out=process.argv[2];
const ck=(ok,why)=>{checks++;assert.ok(ok,why);};
function rng(s){let x=s>>>0;return n=>{x^=x<<13;x^=x>>>17;x^=x<<5;return (x>>>0)%n;};}
function count(name){actionGroups++;family[name]=(family[name]||0)+1;}
class LostReplyStore extends MemoryStore {armed=false;async transact(keys,fn){const x=await super.transact(keys,fn);if(this.armed&&keys.some(k=>k.startsWith('requests/'))){this.armed=false;lostReplies++;throw new Error('fixture-committed-response-lost');}return x;}}
const digest=x=>createHash('sha256').update(x).digest('hex');
const rows=['session,seed,actions,finalGold,credits,spend,refunds,energyPacks,traceSha256'];
const errors={};
async function call(fn,expectedCode){apiCalls++;try{const x=await fn();ck(!expectedCode,`expected ${expectedCode}`);return x;}catch(e){if(e.message==='fixture-committed-response-lost')throw e;ck(e.code===expectedCode,`expected ${expectedCode}, got ${e.code||e.message}`);errors[e.code]=(errors[e.code]||0)+1;return null;}}
await mkdir(out,{recursive:true});const started=performance.now();
try{
for(let index=0;index<sessions;index++){
 const rnd=rng(seed+index*1000003),uid=`stress-google-${index}`,store=new LostReplyStore();let f=fixture({store,energyRefillsEnabled:true});let balance=0,credits=0,spent=0,refunds=0,energy=0,continuations=0;const boosters={},owned=new Set(),receipts=new Map(),trace=[];let originalRevoked=false;
 const original=`fund-token-${index}`;f.play.add(original,purchase(uid));await call(()=>f.service.verifyPurchase(uid,'luma_gold_500',original));balance+=500;credits+=500;
 const remember=(id,action,sku)=>receipts.set(id,{action,sku});
 async function spend(id,sku,{lost=false}={}){
  const item=SKUS[sku],old=receipts.get(id);let error=old?(old.action==='spend'&&old.sku===sku?null:'REQUEST_CONFLICT'):balance<item.price?'INSUFFICIENT_GOLD':(item.kind==='character'||item.kind==='skin')&&owned.has(sku)?'ALREADY_OWNED':null;
  if(lost&&!error&&!old)store.armed=true;
  let got;try{got=await call(()=>f.service.spend(uid,id,sku),error);}catch(e){ck(e.message==='fixture-committed-response-lost','only injected lost reply');got=await call(()=>f.service.spend(uid,id,sku));}
  if(!error&&!old){balance-=item.price;spent+=item.price;remember(id,'spend',sku);if(item.kind==='energy')energy++;if(item.kind==='continuation')continuations++;if(item.kind==='booster')boosters[item.itemId]=(boosters[item.itemId]||0)+item.amount;if(item.kind==='character'||item.kind==='skin')owned.add(sku);}
  if(got){ck(got.balance===balance,'spend/replay balance equals independent ledger');ck(got.entitlement.id===id&&got.entitlement.sku===sku&&got.entitlement.amount===item.amount,'receipt id sku amount exact');}
 }
 async function use(id,sku){
  const item=SKUS[sku],old=receipts.get(id),stock=item.kind==='energy'?energy:item.kind==='continuation'?continuations:boosters[item.itemId]||0;
  const error=old?(old.action==='use'&&old.sku===sku?null:'REQUEST_CONFLICT'):stock<1?'INSUFFICIENT_ITEM':null;
  const got=await call(()=>f.service.use(uid,id,sku),error);
  if(!error&&!old){remember(id,'use',sku);if(item.kind==='energy')energy--;else if(item.kind==='continuation')continuations--;else boosters[item.itemId]--;}
  if(got){ck(got.balance===balance&&got.use.id===id&&got.use.sku===sku,'use/replay preserves Gold and request');ck(got.use.amount===(item.kind==='energy'?10:1),'use exact entitlement amount');}
 }
 for(let step=0;step<steps;step++){
  const op=rnd(11),id=`request-${rnd(12)}`,sku=['energy_refill10','continue_5','booster:hint','character:miko','skin:lagoon'][rnd(5)];
  trace.push([op,id,sku]);
  if(op===0){await call(()=>f.service.verifyPurchase(uid,'luma_gold_500',original),originalRevoked?'PURCHASE_REVOKED':undefined);count('verified-purchase-retry');}
  if(op===1){await spend(id,sku);count('spend-request-ownership-refusal');}
  if(op===2){await use(id,['energy_refill10','continue_5','booster:hint'][rnd(3)]);count('use-request-stock-refusal');}
  if(op===3){const request=`concurrent-same-${index}-${step}`,can=balance>=50;const calls=await Promise.allSettled(Array.from({length:8},()=>{apiCalls++;return f.service.spend(uid,request,'energy_refill10');}));ck(calls.every(x=>can?x.status==='fulfilled':x.status==='rejected'&&x.reason.code==='INSUFFICIENT_GOLD'),'concurrent duplicate verdict');if(can){balance-=50;spent+=50;energy++;remember(request,'spend','energy_refill10');ck(calls.every(x=>x.value.balance===balance&&x.value.inventory.energyRefills===energy),'eight duplicates one debit and pack');}count('eight-concurrent-same-request');}
  if(op===4){const before=balance,n=Math.min(8,Math.floor(Math.max(0,before)/50));const calls=await Promise.allSettled(Array.from({length:8},(_,j)=>{apiCalls++;return f.service.spend(uid,`concurrent-distinct-${index}-${step}-${j}`,'energy_refill10');}));ck(calls.filter(x=>x.status==='fulfilled').length===n,'distinct concurrent affordability exact');ck(calls.every(x=>x.status==='fulfilled'||x.reason.code==='INSUFFICIENT_GOLD'),'concurrent no other rejection');balance-=50*n;spent+=50*n;energy+=n;for(let j=0;j<n;j++)remember(`concurrent-distinct-${index}-${step}-${j}`,'spend','energy_refill10');count('eight-concurrent-distinct-requests');}
  if(op===5){const next=new LostReplyStore();next.docs=structuredClone(store.docs);const oldWallet=await call(()=>f.service.getWallet(uid));const handle=fixture({store:next,play:f.play,energyRefillsEnabled:true});const actual=await call(()=>handle.service.getWallet(uid));ck(JSON.stringify(actual)===JSON.stringify(oldWallet),'server reconstruction from cloned persisted document fixture');reconstructed++;count('reconstruct-from-memory-document-snapshot');}
  if(op===6){const closed=fixture({store,play:f.play,energyRefillsEnabled:false,consumablesEnabled:false});await call(()=>closed.service.spend(uid,`disabled-energy-${index}-${step}`,'energy_refill10'),'SKU_NOT_AVAILABLE');await call(()=>closed.service.spend(uid,`disabled-continue-${index}-${step}`,'continue_5'),'SKU_NOT_AVAILABLE');count('closed-offer-gates');}
  if(op===7){await call(()=>f.service.spend(uid,'bad','energy_refill10'),'INVALID_REQUEST_ID');await call(()=>f.service.use('isolated-other-user',`other-use-${index}-${step}`,'energy_refill10'),'INSUFFICIENT_ITEM');count('invalid-request-and-other-account');}
  if(op===8){const token=`provider-down-${index}-${step}`;f.play.add(token,purchase(uid));f.play.unavailable=true;await call(()=>f.service.verifyPurchase(uid,'luma_gold_500',token),'PLAY_UNAVAILABLE');f.play.unavailable=false;count('provider-unavailable-no-credit');}
  if(op===9){const token=`pending-token-${index}-${step}`;f.play.add(token,purchase(uid,'luma_gold_500',{purchaseStateContext:{purchaseState:'PENDING'}}));const pending=await call(()=>f.service.verifyPurchase(uid,'luma_gold_500',token));ck(pending.balance===balance,'pending purchase cannot credit');f.play.purchases.get(token).purchaseStateContext.purchaseState='PURCHASED';await call(()=>f.service.verifyPurchase(uid,'luma_gold_500',token));balance+=500;credits+=500;const repeat=await call(()=>f.service.verifyPurchase(uid,'luma_gold_500',token));ck(repeat.balance===balance,'pending-to-purchased exactly one credit');count('pending-purchased-retry');}
  if(op===10){if(!originalRevoked){f.play.purchases.get(original).purchaseStateContext.purchaseState='CANCELLED';await call(()=>f.service.reconcileToken(original));balance-=500;refunds+=500;originalRevoked=true;if(balance<0)refundDebts++;const w=await call(()=>f.service.getWallet(uid));ck(w.balance===balance,'refund may create disclosed debt');await call(()=>f.service.reconcileToken(original));const twice=await call(()=>f.service.getWallet(uid));ck(twice.balance===balance,'refund cannot repeat');}else await spend(`lost-reply-${index}-${step}`,'energy_refill10',{lost:true});count('refund-or-committed-reply-loss');}
  const w=await call(()=>f.service.getWallet(uid));ck(w.balance===credits-spent-refunds&&w.balance===balance,'account balance independent arithmetic');ck(w.inventory.energyRefills===energy&&energy>=0,'energy inventory nonnegative and exact');ck(w.inventory.continuations===continuations&&continuations>=0,'continuation stock exact');for(const [id,n]of Object.entries(boosters))ck(w.inventory.boosters[id]===n&&n>=0,'booster stock exact');
 }
 rows.push([index,seed+index*1000003,steps,balance,credits,spent,refunds,energy,digest(JSON.stringify(trace))].join(','));
}
// Exact lost-response recovery is directed as well as randomly encountered.
for(let n=0;n<1000;n++){const store=new LostReplyStore(),f=fixture({store,energyRefillsEnabled:true}),uid=`lost-${n}`,token=`lost-token-${n}`;f.play.add(token,purchase(uid));await call(()=>f.service.verifyPurchase(uid,'luma_gold_500',token));store.armed=true;await assert.rejects(()=>{apiCalls++;return f.service.spend(uid,'directed-lost-buy','energy_refill10');},/fixture-committed-response-lost/);const a=await call(()=>f.service.spend(uid,'directed-lost-buy','energy_refill10'));ck(a.balance===450&&a.inventory.energyRefills===1,'directed lost spend one debit');store.armed=true;await assert.rejects(()=>{apiCalls++;return f.service.use(uid,'directed-lost-use','energy_refill10');},/fixture-committed-response-lost/);const b=await call(()=>f.service.use(uid,'directed-lost-use','energy_refill10'));ck(b.balance===450&&b.inventory.energyRefills===0&&b.use.amount===10,'directed lost use one pack');count('directed-lost-buy-and-use');}
await writeFile(`${out}/sessions.csv`,rows.join('\n')+'\n');
const result={status:'PASS',seed,randomizedSessions:sessions,randomizedActionGroups:sessions*steps,directedLostResponseScenarios:1000,actionGroups,publicApiCalls:apiCalls,assertions:checks,committedRepliesLost:lostReplies,modeledRefundDebtsObserved:refundDebts,reconstructedServiceSnapshots:reconstructed,elapsedSeconds:(performance.now()-started)/1000,actionFamilies:family,expectedErrorCounts:errors,scope:'Actual WalletService public methods and catalogue with MemoryStore/FakePlay; eight-way concurrency, controlled provider replies, pending/purchased/revoked fixtures, account isolation and lost committed replies. Consumables and energy enabled only in explicit experimental fixtures; closed gates also tested. No Firestore durability, real authentication, deployed backend, provider network, payment or Android client validation. Negative balances caused solely by modeled purchase refunds are permitted contractual debt, not overspending.'};await writeFile(`${out}/summary.json`,JSON.stringify(result,null,2)+'\n');console.log(JSON.stringify(result,null,2));
}catch(e){await writeFile(`${out}/failure.txt`,e.stack+'\n');throw e;}
