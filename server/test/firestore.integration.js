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
