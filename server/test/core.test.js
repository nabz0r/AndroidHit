import test from 'node:test';
import assert from 'node:assert/strict';
import { MemoryStore } from '../src/store.js';
import { hash, AppError } from '../src/core.js';
import { PRODUCTS, SKUS } from '../src/catalog.js';
import { fixture,purchase,notification,UID,OTHER } from './helpers.js';

test('each allowlisted Play pack credits exact server quantity and consumes after durable credit',async()=>{
  for(const [productId,gold]of Object.entries(PRODUCTS)){
    const f=fixture();f.play.add('token',purchase(UID,productId));
    const result=await f.service.verifyPurchase(UID,productId,'token');
    assert.equal(result.balance,gold);assert.equal(result.version,1);assert.equal(result.purchase.credited,true);assert.equal(result.purchase.consumptionPending,false);
    assert.equal((await f.store.read(`purchases/${hash('token')}`)).consumed,true);
    assert.ok(f.play.packages.every(value=>value==='com.androidhit.lumadrift'));
    const repeated=await f.service.verifyPurchase(UID,productId,'token');assert.equal(repeated.balance,gold);assert.equal(repeated.purchase.credited,false);
  }
});
test('64 concurrent claims of one purchase produce exactly one durable credit',async()=>{
  const f=fixture();f.play.add('race');
  const results=await Promise.all(Array.from({length:64},()=>f.service.verifyPurchase(UID,'luma_gold_500','race')));
  assert.equal(results.filter(result=>result.purchase.credited).length,1);
  assert.equal((await f.service.getWallet(UID)).balance,500);assert.equal((await f.service.getWallet(UID)).version,1);
});
test('different users cannot claim same token before or after ledger exists',async()=>{
  const f=fixture();f.play.add('owner-token');
  await assert.rejects(()=>f.service.verifyPurchase(OTHER,'luma_gold_500','owner-token'),{code:'PURCHASE_OWNER_MISMATCH'});
  await f.service.verifyPurchase(UID,'luma_gold_500','owner-token');const calls=f.play.gets;
  await assert.rejects(()=>f.service.verifyPurchase(OTHER,'luma_gold_500','owner-token'),{code:'PURCHASE_ALREADY_CLAIMED'});
  assert.equal(f.play.gets,calls);assert.equal((await f.service.getWallet(OTHER)).balance,0);
});
test('malformed provider data, wrong product/owner and unsupported quantities cannot credit',async()=>{
  for(const mutate of [
    raw=>{raw.productLineItem[0].productId='luma_gold_3600';},
    raw=>{raw.productLineItem.push(raw.productLineItem[0]);},
    raw=>{raw.productLineItem[0].productOfferDetails.quantity=2;},
    raw=>{delete raw.productLineItem[0].productOfferDetails.quantity;},
    raw=>{raw.productLineItem[0].productOfferDetails.refundableQuantity=2;},
    raw=>{raw.productLineItem[0].productOfferDetails.rentOfferDetails={};},
    raw=>{raw.purchaseStateContext.purchaseState='WHAT';},
    raw=>{raw.obfuscatedExternalAccountId='';},
    raw=>{raw.productLineItem[0].productOfferDetails.consumptionState='';}
  ]){
    const f=fixture(),raw=purchase();mutate(raw);f.play.add('bad',raw);
    await assert.rejects(()=>f.service.verifyPurchase(UID,'luma_gold_500','bad'));
    assert.equal((await f.service.getWallet(UID)).balance,0);assert.equal(f.play.consumptions,0);
  }
});
test('invalid IDs and opaque-token payload limits fail before provider requests',async()=>{
  const f=fixture();
  for(const token of ['', 'x'.repeat(4097),'token\nsecret','two words'])await assert.rejects(()=>f.service.verifyPurchase(UID,'luma_gold_500',token),{code:'INVALID_PURCHASE_TOKEN'});
  await assert.rejects(()=>f.service.verifyPurchase(UID,'other','token'),{code:'UNKNOWN_PRODUCT'});
  await assert.rejects(()=>f.service.getWallet('a'.repeat(129)),{code:'INVALID_IDENTITY'});
  await assert.rejects(()=>f.service.spend(UID,'short','skin:lagoon'),{code:'INVALID_REQUEST_ID'});
  await assert.rejects(()=>f.service.spend(UID,'12345678','__proto__'),{code:'UNKNOWN_SKU'});
  assert.equal(f.play.gets,0);
});
test('PENDING grants nothing; authenticated RTDN hint rechecks Google and fulfills completed purchase',async()=>{
  const f=fixture();f.play.add('pending',purchase(UID,'luma_gold_500',{purchaseStateContext:{purchaseState:'PENDING'}}));
  const pending=await f.service.verifyPurchase(UID,'luma_gold_500','pending');assert.equal(pending.balance,0);assert.equal(pending.purchase.status,'PENDING');assert.equal(f.play.consumptions,0);
  f.play.purchases.get('pending').purchaseStateContext.purchaseState='PURCHASED';
  const envelope=notification('pending');await f.service.handleNotification(envelope);
  assert.equal((await f.service.getWallet(UID)).balance,500);
  const calls=f.play.gets;assert.equal((await f.service.handleNotification(envelope)).duplicate,true);assert.equal(f.play.gets,calls);
});
test('scheduled reconciliation recovers missed completion notification',async()=>{
  const f=fixture();f.play.add('pending',purchase(UID,'luma_gold_500',{purchaseStateContext:{purchaseState:'PENDING'}}));
  await f.service.verifyPurchase(UID,'luma_gold_500','pending');f.play.purchases.get('pending').purchaseStateContext.purchaseState='PURCHASED';f.advance(6*60000);
  await f.service.maintenance();assert.equal((await f.service.getWallet(UID)).balance,500);
});
test('PENDING with zero or omitted refundable quantity/consumption state stays nonterminal and later credits',async()=>{
  for(const omitted of [false,true]){
    const f=fixture(),raw=purchase(UID,'luma_gold_500',{purchaseStateContext:{purchaseState:'PENDING'}});
    if(omitted)delete raw.productLineItem[0].productOfferDetails.refundableQuantity;else raw.productLineItem[0].productOfferDetails.refundableQuantity=0;
    delete raw.productLineItem[0].productOfferDetails.consumptionState;f.play.add('pending-zero',raw);
    await f.service.verifyPurchase(UID,'luma_gold_500','pending-zero');assert.equal((await f.store.read(`purchases/${hash('pending-zero')}`)).terminal,false);
    f.play.add('pending-zero',purchase());await f.service.reconcileToken('pending-zero');assert.equal((await f.service.getWallet(UID)).balance,500);
  }
});
test('non-ASCII obfuscated account identifiers fail safely rather than throwing crypto length errors',async()=>{
  const f=fixture();f.play.add('unicode',purchase(UID,'luma_gold_500',{obfuscatedExternalAccountId:'é'.repeat(64)}));await assert.rejects(()=>f.service.verifyPurchase(UID,'luma_gold_500','unicode'),{code:'PURCHASE_OWNER_MISMATCH'});
});
test('cancelled/refunded initial purchase never grants; terminal state prevents stale purchase replay',async()=>{
  const f=fixture();f.play.add('cancelled',purchase(UID,'luma_gold_500',{purchaseStateContext:{purchaseState:'CANCELLED'}}));
  const result=await f.service.verifyPurchase(UID,'luma_gold_500','cancelled');assert.equal(result.balance,0);assert.equal(f.play.consumptions,0);
  f.play.purchases.get('cancelled').purchaseStateContext.purchaseState='PURCHASED';
  await assert.rejects(()=>f.service.verifyPurchase(UID,'luma_gold_500','cancelled'),{code:'PURCHASE_REVOKED'});
});
test('unknown already-consumed token cannot recreate previously delivered gold',async()=>{
  const f=fixture(),raw=purchase();raw.productLineItem[0].productOfferDetails.consumptionState='CONSUMPTION_STATE_CONSUMED';f.play.add('old',raw);
  await assert.rejects(()=>f.service.verifyPurchase(UID,'luma_gold_500','old'),{code:'CONSUMED_PURCHASE_NOT_RECORDED'});assert.equal((await f.service.getWallet(UID)).balance,0);
});
test('credit persists across provider consumption outage and is retried by maintenance without re-credit',async()=>{
  const f=fixture();f.play.add('consume-outage');f.play.consumeUnavailable=true;
  const result=await f.service.verifyPurchase(UID,'luma_gold_500','consume-outage');assert.equal(result.balance,500);assert.equal(result.purchase.consumptionPending,true);
  f.play.consumeUnavailable=false;f.advance(5*60000);await f.service.maintenance();
  assert.equal((await f.service.getWallet(UID)).balance,500);assert.equal((await f.store.read(`purchases/${hash('consume-outage')}`)).needsConsume,false);
});
test('crash after Google consumption but before local consumed marker is recovered',async()=>{
  class CrashStore extends MemoryStore{count=0;async transact(keys,fn){if(++this.count===3)throw new Error('simulated crash');return super.transact(keys,fn);}}
  const f=fixture({store:new CrashStore()});f.play.add('crash');
  await assert.rejects(()=>f.service.verifyPurchase(UID,'luma_gold_500','crash'),/simulated crash/u);
  assert.equal((await f.service.getWallet(UID)).balance,500);
  const repeated=await f.service.verifyPurchase(UID,'luma_gold_500','crash');assert.equal(repeated.balance,500);assert.equal(repeated.purchase.credited,false);
  await f.service.maintenance();assert.equal((await f.store.read(`purchases/${hash('crash')}`)).needsConsume,false);
});
test('ambiguous database response after durable credit retries without a second credit',async()=>{
  class AmbiguousStore extends MemoryStore{count=0;async transact(keys,fn){const result=await super.transact(keys,fn);if(++this.count===2)throw new Error('commit reply lost');return result;}}
  const f=fixture({store:new AmbiguousStore()});f.play.add('ambiguous');await assert.rejects(()=>f.service.verifyPurchase(UID,'luma_gold_500','ambiguous'),/commit reply lost/u);assert.equal(f.play.consumptions,0);
  const retried=await f.service.verifyPurchase(UID,'luma_gold_500','ambiguous');assert.equal(retried.balance,500);assert.equal(retried.purchase.credited,false);assert.equal((await f.service.getWallet(UID)).version,1);
});
test('single spend request repeated concurrently produces one debit and a stable entitlement',async()=>{
  const f=fixture();f.play.add('fund');await f.service.verifyPurchase(UID,'luma_gold_500','fund');
  const results=await Promise.all(Array.from({length:50},()=>f.service.spend(UID,'request-123','character:miko')));
  assert.ok(results.every(result=>result.balance===340&&result.characters.includes('miko')));
  assert.equal((await f.service.getWallet(UID)).version,2);assert.deepEqual(results[0].entitlement,{id:'request-123',sku:'character:miko',kind:'character',itemId:'miko',amount:1});
  await assert.rejects(()=>f.service.spend(UID,'request-123','skin:lagoon'),{code:'REQUEST_CONFLICT'});
  await assert.rejects(()=>f.service.spend(UID,'request-456','character:miko'),{code:'ALREADY_OWNED'});
});
test('concurrent distinct requests cannot overdraft wallet',async()=>{
  const f=fixture();f.play.add('fund');await f.service.verifyPurchase(UID,'luma_gold_500','fund');
  const results=await Promise.allSettled(Array.from({length:20},(_,i)=>f.service.spend(UID,`request-${i}`,'continue_5')));
  assert.equal(results.filter(r=>r.status==='fulfilled').length,5);assert.equal((await f.service.getWallet(UID)).balance,0);assert.equal((await f.service.getWallet(UID)).inventory.continuations,5);
});
test('server inventory restores and use requests consume exactly once without touching gold',async()=>{
  const f=fixture();f.play.add('fund');await f.service.verifyPurchase(UID,'luma_gold_500','fund');
  const bought=await f.service.spend(UID,'power-buy-1','booster:hint');assert.equal(bought.inventory.boosters.hint,3);assert.equal(bought.balance,455);
  await Promise.all(Array.from({length:10},()=>f.service.use(UID,'power-use-1','booster:hint')));
  const result=await f.service.getWallet(UID);assert.equal(result.inventory.boosters.hint,2);assert.equal(result.balance,455);
  await assert.rejects(()=>f.service.use(UID,'power-use-1','continue_5'),{code:'REQUEST_CONFLICT'});
  await assert.rejects(()=>f.service.use(UID,'power-use-2','character:miko'),{code:'NOT_CONSUMABLE'});
  await assert.rejects(()=>f.service.use(UID,'power-use-3','booster:flow'),{code:'INSUFFICIENT_ITEM'});
});
test('verified refund reverses credit once, preserves audit, creates debt after spending and blocks further spending',async()=>{
  const f=fixture();f.play.add('refunded');await f.service.verifyPurchase(UID,'luma_gold_500','refunded');await f.service.spend(UID,'spend-0001','character:miko');
  f.play.purchases.get('refunded').productLineItem[0].productOfferDetails.refundableQuantity=0;
  f.play.voided=[{purchaseToken:'refunded',orderId:'GPA.1234-5678-9012-34567'}];await f.service.maintenance();
  const result=await f.service.getWallet(UID);assert.equal(result.balance,-160);assert.deepEqual(result.characters,['miko']);
  await f.service.maintenance();assert.equal((await f.service.getWallet(UID)).balance,-160);
  await assert.rejects(()=>f.service.spend(UID,'spend-0002','skin:lagoon'),{code:'INSUFFICIENT_GOLD'});
  assert.equal((await f.store.read(`purchases/${hash('refunded')}`)).refunded,500);
});
test('Google Voided API chargeback revokes exactly once even while product V2 still says purchased',async()=>{
  const f=fixture();f.play.add('chargeback');await f.service.verifyPurchase(UID,'luma_gold_500','chargeback');
  f.play.voided=[{purchaseToken:'chargeback',orderId:'GPA.1234-5678-9012-34567',voidedReason:7,voidedTimeMillis:String(f.now())}];
  assert.equal(f.play.purchases.get('chargeback').productLineItem[0].productOfferDetails.refundableQuantity,1);
  await f.service.maintenance();assert.equal((await f.service.getWallet(UID)).balance,0);await f.service.maintenance();assert.equal((await f.service.getWallet(UID)).balance,0);
  await assert.rejects(()=>f.service.verifyPurchase(UID,'luma_gold_500','chargeback'),{code:'PURCHASE_REVOKED'});
});
test('voided purchase with mismatched order cannot debit a different valid order',async()=>{
  const f=fixture();f.play.add('order');await f.service.verifyPurchase(UID,'luma_gold_500','order');f.play.voided=[{purchaseToken:'order',orderId:'GPA.wrong'}];
  await assert.rejects(()=>f.service.maintenance(),{code:'VOIDED_ORDER_MISMATCH'});assert.equal((await f.service.getWallet(UID)).balance,500);
});
test('notification payload alone cannot mint gold, wrong package and malformed base64 rejected',async()=>{
  const f=fixture();f.play.add('unknown');await f.service.handleNotification(notification('unknown'));assert.equal((await f.service.getWallet(UID)).balance,0);
  await assert.rejects(()=>f.service.handleNotification(notification('x',{packageName:'com.other.game'})),{code:'PACKAGE_MISMATCH'});
  await assert.rejects(()=>f.service.handleNotification({message:{messageId:'1',data:'###'}}),{code:'INVALID_NOTIFICATION'});
});
test('account deletion requires recent login, erases wallet/request/token personal data and blocks token replay',async()=>{
  const f=fixture();f.play.add('delete-token');await f.service.verifyPurchase(UID,'luma_gold_500','delete-token');await f.service.spend(UID,'delete-spend','character:miko');
  await assert.rejects(()=>f.service.deleteAccount(UID,f.now()/1000-301),{code:'RECENT_LOGIN_REQUIRED'});
  assert.deepEqual(await f.service.deleteAccount(UID,f.now()/1000),{deleted:true});assert.deepEqual(f.deleted,[UID]);
  await assert.rejects(()=>f.service.getWallet(UID),{code:'ACCOUNT_DELETED'});
  const audit=await f.store.read(`purchases/${hash('delete-token')}`);assert.equal(audit.purchaseToken,undefined);assert.equal(audit.orderId,undefined);assert.equal(audit.owner,hash(UID));assert.equal(audit.terminal,true);
  assert.equal((await f.store.find('requests',{})).length,0);
  await assert.rejects(()=>f.service.verifyPurchase(OTHER,'luma_gold_500','delete-token'),{code:'PURCHASE_ALREADY_CLAIMED'});
  f.advance(366*86400000);await f.service.maintenance();assert.equal(await f.store.read(`purchases/${hash('delete-token')}`),undefined);
  await assert.rejects(()=>f.service.verifyPurchase(UID,'luma_gold_500','delete-token'),{code:'CONSUMED_PURCHASE_NOT_RECORDED'});
});
test('deletion provider outage freezes wallet; durable maintenance job completes after recovery',async()=>{
  let fail=true;const f=fixture({identity:{deleteUser:async()=>{if(fail)throw new Error('auth down');}}});f.play.add('deletion-job');await f.service.verifyPurchase(UID,'luma_gold_500','deletion-job');
  await assert.rejects(()=>f.service.deleteAccount(UID,f.now()/1000),{code:'DELETION_RETRY_PENDING'});await assert.rejects(()=>f.service.getWallet(UID),{code:'ACCOUNT_DELETED'});
  assert.equal((await f.store.read(`deletions/${hash(UID)}`)).state,'PENDING');fail=false;f.advance(5*60000);await f.service.maintenance();assert.equal((await f.store.read(`deletions/${hash(UID)}`)).state,'COMPLETE');
});
test('crash after deleting Firebase user is recovered by durable deletion job without that user logging in',async()=>{
  let crash=false,deleted=false;
  class CrashRedactionStore extends MemoryStore{async transact(keys,fn){if(crash&&keys.some(key=>key.startsWith('purchases/'))){crash=false;throw new Error('redaction commit failed');}return super.transact(keys,fn);}}
  const f=fixture({store:new CrashRedactionStore(),identity:{deleteUser:async()=>{if(deleted)throw {code:'auth/user-not-found'};deleted=true;crash=true;}}});f.play.add('deletion-crash');await f.service.verifyPurchase(UID,'luma_gold_500','deletion-crash');
  await assert.rejects(()=>f.service.deleteAccount(UID,f.now()/1000),/redaction commit failed/u);assert.equal(deleted,true);await assert.rejects(()=>f.service.getWallet(UID),{code:'ACCOUNT_DELETED'});
  await f.service.maintenance();const audit=await f.store.read(`purchases/${hash('deletion-crash')}`);assert.equal(audit.purchaseToken,undefined);assert.equal((await f.store.read(`deletions/${hash(UID)}`)).state,'COMPLETE');
});
test('deletion freezes concurrent new purchases and spends before waiting for Firebase deletion',async()=>{
  let release,entered;const deleting=new Promise(resolve=>{entered=resolve;});
  const f=fixture({identity:{deleteUser:async()=>{entered();await new Promise(resolve=>{release=resolve;});}}});f.play.add('deleting');await f.service.verifyPurchase(UID,'luma_gold_500','deleting');
  const completion=f.service.deleteAccount(UID,f.now()/1000);await deleting;
  await assert.rejects(()=>f.service.spend(UID,'during-delete','character:miko'),{code:'ACCOUNT_DELETED'});await assert.rejects(()=>f.service.verifyPurchase(UID,'luma_gold_500','new-token'),{code:'ACCOUNT_DELETED'});
  release();await completion;assert.equal((await f.store.read(`wallets/${hash(UID)}`)).state,'DELETED');
});
test('test-purchase flag, refund amount and lifetime credit guards fail closed',async()=>{
  const f=fixture({allowTestPurchases:false});f.play.add('test',purchase(UID,'luma_gold_500',{testPurchaseContext:{fopType:'TEST'}}));await assert.rejects(()=>f.service.verifyPurchase(UID,'luma_gold_500','test'),{code:'TEST_PURCHASE_DISABLED'});
  const g=fixture();g.play.add('cap');await g.service.verifyPurchase(UID,'luma_gold_500','cap');
  const key=`wallets/${hash(UID)}`;await g.store.transact([key],tx=>{const wallet=tx.get(key);wallet.lifetimeCredits=1_000_000_000;tx.set(key,wallet);});g.play.add('over-cap');await assert.rejects(()=>g.service.verifyPurchase(UID,'luma_gold_500','over-cap'),{code:'WALLET_LIMIT'});
});
test('retry backoff prevents a permanently broken consume from starving later purchases at batch size one',async()=>{
  const f=fixture({batchSize:1});f.play.add('bad').add('good');f.play.consumeUnavailable=true;
  await f.service.verifyPurchase(UID,'luma_gold_500','bad');await f.service.verifyPurchase(UID,'luma_gold_500','good');
  const get=f.play.getPurchase.bind(f.play),consume=f.play.consume.bind(f.play);f.play.consumeUnavailable=false;
  f.play.getPurchase=async(pkg,token)=>{if(token==='bad')throw new AppError(503,'PLAY_UNAVAILABLE');return get(pkg,token);};
  f.play.consume=async(pkg,product,token)=>{if(token==='bad')throw new AppError(503,'PLAY_UNAVAILABLE');return consume(pkg,product,token);};
  f.advance(5*60000);await assert.rejects(()=>f.service.maintenance(),{code:'MAINTENANCE_RETRY_REQUIRED'});
  await f.service.maintenance();assert.equal((await f.store.read(`purchases/${hash('good')}`)).needsConsume,false);assert.equal((await f.store.read(`purchases/${hash('bad')}`)).needsConsume,true);assert.equal((await f.service.getWallet(UID)).balance,1000);
});
test('production service disables consumable redemption until Android server-use integration ships',async()=>{
  const f=fixture({consumablesEnabled:false});f.play.add('production');await f.service.verifyPurchase(UID,'luma_gold_500','production');await assert.rejects(()=>f.service.spend(UID,'production-1','booster:hint'),{code:'SKU_NOT_AVAILABLE'});await assert.rejects(()=>f.service.use(UID,'production-2','booster:hint'),{code:'SKU_NOT_AVAILABLE'});
  assert.equal((await f.service.spend(UID,'production-3','character:miko')).balance,340);
});
test('production paid-catalog prices and IDs match Android source catalog',async()=>{
  const {readFile}=await import('node:fs/promises');const source=await readFile(new URL('../../app/src/main/java/com/androidhit/lumadrift/data/Catalog.java',import.meta.url),'utf8');
  for(const [sku,item]of Object.entries(SKUS))if(['character','skin'].includes(item.kind)){
    const type=item.kind==='character'?'CharacterItem':'SkinItem';assert.ok(source.includes(`new ${type}("${item.itemId}", ${item.price},`),sku);
  }
});
