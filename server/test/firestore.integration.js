// Explicit local-emulator test only. Never run against a real project.
import test from 'node:test';
import assert from 'node:assert/strict';
import { initializeApp } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import { FirestoreStore } from '../src/production.js';
import { fixture,purchase,UID,OTHER } from './helpers.js';
import { hash } from '../src/core.js';

if(!/^127\.0\.0\.1:\d+$/u.test(process.env.FIRESTORE_EMULATOR_HOST||''))throw new Error('Local Firestore emulator required');
const projectId='demo-luma-wallet',db=getFirestore(initializeApp({projectId}));
test('Firestore adapter survives real transaction contention, restores entitlements, refunds and deletes',async()=>{
  const store=new FirestoreStore(db),f=fixture({store}),other=fixture({store:new FirestoreStore(db),play:f.play});
  f.play.add('firestore-concurrent',purchase());
  const results=await Promise.all(Array.from({length:12},(_,i)=>(i%2?f:other).service.verifyPurchase(UID,'luma_gold_500','firestore-concurrent')));
  assert.equal(results.filter(result=>result.purchase.credited).length,1);assert.equal((await other.service.getWallet(UID)).balance,500);
  await assert.rejects(()=>other.service.verifyPurchase(OTHER,'luma_gold_500','firestore-concurrent'),{code:'PURCHASE_ALREADY_CLAIMED'});
  await Promise.all(Array.from({length:8},(_,i)=>(i%2?f:other).service.spend(UID,'firestore-spend','character:miko')));
  assert.equal((await f.service.getWallet(UID)).balance,340);assert.deepEqual((await other.service.getWallet(UID)).characters,['miko']);
  f.play.voided=[{purchaseToken:'firestore-concurrent',orderId:'GPA.1234-5678-9012-34567',voidedReason:7}];await f.service.maintenance();assert.equal((await other.service.getWallet(UID)).balance,-160);
  await f.service.deleteAccount(UID,f.now()/1000);await assert.rejects(()=>other.service.getWallet(UID),{code:'ACCOUNT_DELETED'});
  const audit=await store.read(`purchases/${hash('firestore-concurrent')}`);assert.equal(audit.purchaseToken,undefined);assert.equal(audit.orderId,undefined);assert.equal(audit.personalDataPresent,false);
  assert.equal((await store.find('requests',{field:'owner',value:hash(UID)})).length,0);
});
test('Firestore rules deny unauthenticated direct reads and writes, including private purchase ledger',async()=>{
  const base=`http://${process.env.FIRESTORE_EMULATOR_HOST}/v1/projects/${projectId}/databases/(default)/documents`;
  for(const path of ['/wallets/example','/purchases/example']){
    const read=await fetch(`${base}${path}`);assert.equal(read.status,403);
    const write=await fetch(`${base}${path}`,{method:'PATCH',headers:{'Content-Type':'application/json'},body:JSON.stringify({fields:{balance:{integerValue:'999999'}}})});assert.equal(write.status,403);
  }
});
test.after(async()=>{await db.terminate();});
test('Firestore social transactions enforce mutual acceptance and atomic per-day gifts across service instances',async()=>{
 const store=new FirestoreStore(db),f=fixture({store}),other=fixture({store:new FirestoreStore(db)}),s=f.service.social,t=other.service.social;
 const a='firestore-social-a',b='firestore-social-b',ac=(await s.snapshot(a)).me.code,bc=(await t.snapshot(b)).me.code;
 await Promise.all(Array.from({length:8},(_,i)=>(i%2?s:t).request(a,bc,'firestore-invite')));await t.respond(b,ac,true,'firestore-accept');
 const sent=await Promise.all(Array.from({length:12},(_,i)=>(i%2?s:t).gift(a,bc,`firestore-gift-${i}`)));assert.equal(new Set(sent.map(x=>x.giftId)).size,1);assert.equal((await t.snapshot(b)).gifts.length,1);assert.equal((await s.snapshot(a)).limits.giftsRemaining,2);
 const claimed=await Promise.all(Array.from({length:8},(_,i)=>(i%2?s:t).claim(b,sent[0].giftId,`firestore-claim-${i}`)));assert.ok(claimed.every(x=>x.grant.id===sent[0].giftId));
 await f.service.deleteAccount(a,f.now()/1000);assert.equal((await t.snapshot(b)).friends.length,0);assert.equal((await t.snapshot(b)).gifts.length,0);assert.equal(await store.read(`socialCodes/${ac}`),undefined);
});
test('Firestore paid energy has one debit and one account-bound redemption across independent service instances',async()=>{
 const store=new FirestoreStore(db),a='firestore-energy-account',b='firestore-energy-other';
 const f=fixture({store,energyRefillsEnabled:true}),other=fixture({store:new FirestoreStore(db),energyRefillsEnabled:true,play:f.play});
 f.play.add('firestore-energy-fund',purchase(a));await f.service.verifyPurchase(a,'luma_gold_500','firestore-energy-fund');
 const purchases=await Promise.all(Array.from({length:12},(_,i)=>(i%2?f:other).service.spend(a,'firestore-energy-buy','energy_refill10')));
 assert.ok(purchases.every(x=>x.balance===450&&x.inventory.energyRefills===1));
 await assert.rejects(()=>other.service.use(b,'firestore-energy-use','energy_refill10'),{code:'INSUFFICIENT_ITEM'});
 const uses=await Promise.all(Array.from({length:12},(_,i)=>(i%2?f:other).service.use(a,'firestore-energy-use','energy_refill10')));
 assert.ok(uses.every(x=>x.balance===450&&x.inventory.energyRefills===0&&x.version===3));
 assert.ok(uses.every(x=>x.use.amount===10&&x.use.owner===hash(a)&&x.use.id==='firestore-energy-use'));
 assert.equal((await other.service.getWallet(a)).inventory.energyRefills,0);
 const request=await store.read(`requests/${hash(`${a}\nfirestore-energy-use`)}`);assert.equal(request.owner,hash(a));assert.equal(request.use.amount,10);
});
