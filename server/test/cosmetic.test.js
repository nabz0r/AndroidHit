import test from 'node:test';
import assert from 'node:assert/strict';
import { fixture,UID } from './helpers.js';
import { hash } from '../src/core.js';
import { CosmeticService,COSMETIC_CATALOG } from '../src/cosmetic.js';
test('cosmetic odds are complete, sum to one and describe server-only currency and duplicate refund',async()=>{
 const f=fixture(),catalog=await f.service.cosmetic.catalog(UID);
 assert.equal(catalog.balance,0);assert.equal(catalog.version,0);assert.equal(catalog.enabled,false);assert.equal(catalog.currency,'LumaGold');assert.equal(catalog.gameplayBenefit,false);assert.equal(catalog.items.length,24);assert.equal(new Set(catalog.items.map(x=>x.id)).size,24);assert.ok(Math.abs(catalog.items.reduce((a,x)=>a+x.probability,0)-1)<1e-12);assert.equal(catalog.duplicateRefund,50);
 await assert.rejects(()=>f.service.cosmetic.draw(UID,'disabled-draw'),{code:'PAID_COSMETIC_DRAWS_DISABLED'});
});
test('paid cosmetic debit and receipt are transactional across concurrent retries, with no earned coin import',async()=>{
 const f=fixture({paidCosmeticDrawsEnabled:true});f.play.add('draw-purchase');await f.service.verifyPurchase(UID,'luma_gold_500','draw-purchase');
 const results=await Promise.all(Array.from({length:20},()=>f.service.cosmetic.draw(UID,'same-draw-0001')));assert.equal(new Set(results.map(x=>x.itemId)).size,1);assert.equal((await f.service.getWallet(UID)).balance,400);assert.equal((await f.service.cosmetic.catalog(UID)).owned.length,1);
 await assert.rejects(()=>f.service.spend(UID,'same-draw-0001','skin:lagoon'),{code:'REQUEST_CONFLICT'});
});
test('duplicates return exactly fifty percent premium compensation without multiplying receipts',async()=>{
 const f=fixture();const key=`wallets/${hash(UID)}`;await f.store.transact([key],tx=>tx.set(key,{balance:500,version:0,state:'ACTIVE',cosmetics:COSMETIC_CATALOG.map(x=>x.id)}));
 const service=new CosmeticService({store:f.store,enabled:true});const result=await service.draw(UID,'all-owned-01');assert.equal(result.duplicate,true);assert.equal(result.balance,450);assert.equal(result.refund,50);assert.equal((await service.draw(UID,'all-owned-01')).balance,450);
});
test('twentieth-draw guarantee excludes owned objects and publishes the actual conditional odds',async()=>{
 const f=fixture();const key=`wallets/${hash(UID)}`,missing='gloves_aurora';await f.store.transact([key],tx=>tx.set(key,{balance:500,version:0,state:'ACTIVE',cosmetics:COSMETIC_CATALOG.filter(x=>x.id!==missing).map(x=>x.id),cosmeticPity:19}));
 const service=new CosmeticService({store:f.store,enabled:true}),catalog=await service.catalog(UID);assert.equal(catalog.items.find(x=>x.id===missing).probability,1);assert.ok(catalog.items.filter(x=>x.id!==missing).every(x=>x.probability===0));
 const result=await service.draw(UID,'guarantee-20');assert.equal(result.itemId,missing);assert.equal(result.duplicate,false);assert.equal(result.balance,400);assert.equal(result.pity,0);
});
test('insufficient balance, deleted account, malformed requests and receipt conflicts fail closed',async()=>{
 const f=fixture({paidCosmeticDrawsEnabled:true});await assert.rejects(()=>f.service.cosmetic.draw(UID,'empty-draw01'),{code:'INSUFFICIENT_GOLD'});await assert.rejects(()=>f.service.cosmetic.draw(UID,'bad'),{code:'INVALID_REQUEST_ID'});
 await f.service.deleteAccount(UID,f.now()/1000);await assert.rejects(()=>f.service.cosmetic.catalog(UID),{code:'ACCOUNT_DELETED'});await assert.rejects(()=>f.service.cosmetic.draw(UID,'deleted-draw'),{code:'ACCOUNT_DELETED'});
});
