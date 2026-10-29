import test from 'node:test';
import assert from 'node:assert/strict';
import {once} from 'node:events';
import {hash} from '../src/core.js';
import {SKUS} from '../src/catalog.js';
import {MemoryStore} from '../src/store.js';
import {createHttpServer} from '../src/http.js';
import {readConfig} from '../src/config.js';
import {fixture,UID,OTHER} from './helpers.js';

const SKU='energy_refill10';
async function funded(options={}){
  const f=fixture({energyRefillsEnabled:true,...options});f.play.add('energy-funding');
  await f.service.verifyPurchase(UID,'luma_gold_500','energy-funding');return f;
}
test('energy offer has a fixed, explicit Gold-only price and is closed by default',async()=>{
  assert.deepEqual(SKUS[SKU],{kind:'energy',itemId:SKU,amount:10,price:50});
  const f=await funded({energyRefillsEnabled:false});
  await assert.rejects(()=>f.service.spend(UID,'energy-disabled',SKU),{code:'SKU_NOT_AVAILABLE'});
  assert.equal((await f.service.getWallet(UID)).balance,500);
  const env={GOOGLE_CLOUD_PROJECT:'luma-drift-production',SERVICE_URL:'https://wallet.example.com',PUBSUB_PUSH_SERVICE_ACCOUNT:'pub@luma-drift-production.iam.gserviceaccount.com',SCHEDULER_SERVICE_ACCOUNT:'clock@luma-drift-production.iam.gserviceaccount.com',RTDN_SUBSCRIPTION:'projects/luma-drift-production/subscriptions/rtdn'};
  assert.equal(readConfig(env).energyRefillsEnabled,false);
  assert.equal(readConfig({...env,ENERGY_REFILLS_ENABLED:'true'}).energyRefillsEnabled,true);
  assert.throws(()=>readConfig({...env,ENERGY_REFILLS_ENABLED:'yes'}));
});
test('64 concurrent spend and use replays debit one pack and issue one owner-bound ten-unit receipt',async()=>{
  const f=await funded();
  const bought=await Promise.all(Array.from({length:64},()=>f.service.spend(UID,'energy-buy-once',SKU)));
  assert.ok(bought.every(x=>x.balance===450&&x.inventory.energyRefills===1));
  assert.ok(bought.every(x=>x.entitlement.owner===hash(UID)&&x.entitlement.amount===10));
  const used=await Promise.all(Array.from({length:64},()=>f.service.use(UID,'energy-use-once',SKU)));
  assert.ok(used.every(x=>x.balance===450&&x.inventory.energyRefills===0&&x.version===3));
  assert.deepEqual(used[0].use,{id:'energy-use-once',sku:SKU,kind:'energy',itemId:SKU,amount:10,owner:hash(UID)});
  assert.ok(used.every(x=>JSON.stringify(x.use)===JSON.stringify(used[0].use)));
  await assert.rejects(()=>f.service.spend(UID,'energy-use-once',SKU),{code:'REQUEST_CONFLICT'});
  await assert.rejects(()=>f.service.use(UID,'energy-buy-once',SKU),{code:'REQUEST_CONFLICT'});
});
test('distinct concurrent attempts never overdraw Gold or inventory',async()=>{
  const f=await funded();
  const bought=await Promise.allSettled(Array.from({length:50},(_,i)=>f.service.spend(UID,`energy-buy-${i}`,SKU)));
  assert.equal(bought.filter(x=>x.status==='fulfilled').length,10);
  assert.equal((await f.service.getWallet(UID)).balance,0);
  assert.equal((await f.service.getWallet(UID)).inventory.energyRefills,10);
  const used=await Promise.allSettled(Array.from({length:50},(_,i)=>f.service.use(UID,`energy-use-${i}`,SKU)));
  assert.equal(used.filter(x=>x.status==='fulfilled').length,10);
  assert.equal((await f.service.getWallet(UID)).inventory.energyRefills,0);
  assert.equal((await f.service.getWallet(UID)).balance,0);
});
test('lost database replies after either commit recover with the original IDs',async()=>{
  class LostReply extends MemoryStore{armed=false;async transact(keys,fn){const value=await super.transact(keys,fn);if(this.armed){this.armed=false;throw new Error('Committed response lost');}return value;}}
  const store=new LostReply(),f=await funded({store});store.armed=true;
  await assert.rejects(()=>f.service.spend(UID,'energy-buy-lost',SKU),/response lost/u);
  const recovered=await f.service.spend(UID,'energy-buy-lost',SKU);assert.equal(recovered.balance,450);assert.equal(recovered.inventory.energyRefills,1);
  store.armed=true;await assert.rejects(()=>f.service.use(UID,'energy-use-lost',SKU),/response lost/u);
  const use=await f.service.use(UID,'energy-use-lost',SKU);assert.equal(use.balance,450);assert.equal(use.inventory.energyRefills,0);assert.equal(use.use.amount,10);assert.equal(use.version,3);
});
test('stock restores independently of a device, cannot cross accounts and remains redeemable when sales pause',async()=>{
  const f=await funded();await f.service.spend(UID,'energy-pack-held',SKU);
  const second=fixture({store:f.store,energyRefillsEnabled:false});
  assert.equal((await second.service.getWallet(UID)).inventory.energyRefills,1);
  await assert.rejects(()=>second.service.use(OTHER,'energy-use-owner',SKU),{code:'INSUFFICIENT_ITEM'});
  assert.equal((await second.service.spend(UID,'energy-pack-held',SKU)).balance,450);
  await assert.rejects(()=>second.service.spend(UID,'energy-new-paused',SKU),{code:'SKU_NOT_AVAILABLE'});
  const used=await second.service.use(UID,'energy-use-owner',SKU);assert.equal(used.use.owner,hash(UID));
  assert.equal((await second.service.getWallet(OTHER)).balance,0);
});
test('inventory cap is checked atomically before a new energy debit',async()=>{
  const f=await funded(),key=`wallets/${hash(UID)}`;
  await f.store.transact([key],tx=>{const wallet=tx.get(key);wallet.inventory.energyRefills=10000;tx.set(key,wallet);});
  await assert.rejects(()=>f.service.spend(UID,'energy-stock-cap',SKU),{code:'INVENTORY_LIMIT'});
  assert.equal((await f.service.getWallet(UID)).balance,500);
});
test('HTTP energy route authenticates, rejects client prices and recovers committed requests after503',async t=>{
  class LostReply extends MemoryStore{armed=false;async transact(keys,fn){const value=await super.transact(keys,fn);if(this.armed&&keys.some(key=>key.startsWith('requests/'))){this.armed=false;throw new Error('Commit reply lost');}return value;}}
  const store=new LostReply(),f=await funded({store});
  const server=createHttpServer({...f,auth:{verifyIdToken:async token=>({uid:token==='other'?OTHER:UID,firebase:{sign_in_provider:'google.com'}})},internalAuth:{},allowedOrigins:[]});
  server.listen(0,'127.0.0.1');await once(server,'listening');
  t.after(async()=>{server.closeAllConnections();await new Promise(resolve=>server.close(resolve));});
  const base=`http://127.0.0.1:${server.address().port}`;
  const post=async(path,body,token='valid')=>{const response=await fetch(base+path,{method:'POST',headers:{Authorization:`Bearer ${token}`,'Content-Type':'application/json'},body:JSON.stringify(body)});return {status:response.status,value:await response.json()};};
  const body={requestId:'energy-http-buy',sku:SKU};
  assert.equal((await post('/v1/wallet/spend',{...body,price:0})).status,400);
  store.armed=true;assert.equal((await post('/v1/wallet/spend',body)).status,503);
  assert.equal((await post('/v1/wallet/spend',body)).value.balance,450);
  assert.equal((await post('/v1/inventory/use',{requestId:'energy-http-use',sku:SKU},'other')).value.error.code,'INSUFFICIENT_ITEM');
  store.armed=true;assert.equal((await post('/v1/inventory/use',{requestId:'energy-http-use',sku:SKU})).status,503);
  const used=await post('/v1/inventory/use',{requestId:'energy-http-use',sku:SKU});
  assert.equal(used.status,200);assert.equal(used.value.use.owner,hash(UID));assert.equal(used.value.use.amount,10);
  assert.deepEqual((await post('/v1/inventory/use',{requestId:'energy-http-use',sku:SKU})).value,used.value);
});
