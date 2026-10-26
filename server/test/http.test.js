import test from 'node:test';
import assert from 'node:assert/strict';
import { once } from 'node:events';
import { request as httpRequest } from 'node:http';
import { createHttpServer } from '../src/http.js';
import { AppError } from '../src/core.js';
import { fixture,notification,UID } from './helpers.js';

async function start(t,options={}){
  const f=options.fixture||fixture();
  const claims={uid:UID,firebase:{sign_in_provider:'google.com'},auth_time:f.now()/1000,...options.claims};
  const auth={verifyIdToken:async(token,revoked)=>{assert.equal(revoked,true);if(token==='expired')throw new Error('private provider detail');return claims;}};
  const internalAuth={verify:async(token,purpose)=>{if(token!==`trusted-${purpose}`)throw new AppError(403,'INVALID_SERVICE_IDENTITY');}};
  const server=createHttpServer({...f,auth,internalAuth,allowedOrigins:['https://nabz0r.github.io'],subscription:'projects/luma-drift-production/subscriptions/luma-rtdn',ipLimit:10000,...options});
  server.listen(0,'127.0.0.1');await once(server,'listening');
  t.after(async()=>{server.closeAllConnections();await new Promise(resolve=>server.close(resolve));});
  const base=`http://127.0.0.1:${server.address().port}`;
  const request=async(path,body,extra={})=>{
    const response=await fetch(`${base}${path}`,{method:body===undefined?'GET':'POST',headers:{Authorization:'Bearer valid',...(body===undefined?{}:{'Content-Type':'application/json'}),...extra.headers},...(body===undefined?{}:{body:typeof body==='string'?body:JSON.stringify(body)}),...extra});
    const text=await response.text();return {status:response.status,headers:response.headers,body:text?JSON.parse(text):undefined};
  };
  return {...f,base,request};
}
test('public health endpoints return uncached JSON without authentication or provider calls',async t=>{
  const calls=[];
  const unexpected=name=>async()=>{calls.push(name);throw new Error(`Health request called ${name}`);};
  const service=Object.fromEntries(['getWallet','verifyPurchase','spend','use','deleteAccount','handleNotification','maintenance'].map(name=>[name,unexpected(`service.${name}`)]));
  const f=await start(t,{
    auth:{verifyIdToken:unexpected('Firebase authentication')},
    internalAuth:{verify:unexpected('service authentication')},
    store:{transact:unexpected('database')},
    service
  });
  for(const path of ['/health','/healthz']){
    const response=await f.request(path,undefined,{headers:{}});
    assert.equal(response.status,200,path);
    assert.deepEqual(response.body,{status:'ok'},path);
    assert.match(response.headers.get('content-type'),/^application\/json\b/u,path);
    assert.equal(response.headers.get('cache-control'),'no-store',path);
  }
  const query=await f.request('/health?x=1',undefined,{headers:{}});
  assert.equal(query.status,400);assert.deepEqual(query.body,{error:{code:'QUERY_NOT_ALLOWED'}});
  const post=await f.request('/health',{}, {headers:{'Content-Type':'application/json'}});
  assert.equal(post.status,404);assert.deepEqual(post.body,{error:{code:'NOT_FOUND'}});
  assert.deepEqual(calls,[],'Health checks and rejected variants must not reach authentication, wallet or providers');
});
test('HTTP real socket: paid purchase, wallet refresh and permanent entitlement redemption',async t=>{
  const f=await start(t);f.play.add('http-token');
  assert.deepEqual((await f.request('/v1/wallet')).body,{balance:0,version:0,characters:[],skins:[],inventory:{boosters:{},continuations:0}});
  const verified=await f.request('/v1/purchases/verify',{productId:'luma_gold_500',purchaseToken:'http-token'});assert.equal(verified.status,200);assert.equal(verified.body.balance,500);
  const bought=await f.request('/v1/wallet/spend',{requestId:'http-spend-1',sku:'skin:lagoon'});assert.equal(bought.status,200);assert.equal(bought.body.balance,320);assert.deepEqual(bought.body.skins,['lagoon']);
  const restored=await f.request('/v1/inventory');assert.deepEqual(restored.body.skins,['lagoon']);assert.equal(restored.headers.get('cache-control'),'no-store');
  assert.equal((await f.request('/v1/wallet/spend',{requestId:'http-spend-1',sku:'character:miko'})).body.error.code,'REQUEST_CONFLICT');
});
test('HTTP authentication rejects missing, expired and non-Google identity',async t=>{
  const f=await start(t);
  assert.equal((await f.request('/v1/wallet',undefined,{headers:{}})).status,401);
  const expired=await f.request('/v1/wallet',undefined,{headers:{Authorization:'Bearer expired'}});assert.equal(expired.status,401);assert.deepEqual(expired.body,{error:{code:'UNAUTHENTICATED'}});
  const anonymous=await start(t,{claims:{firebase:{sign_in_provider:'anonymous'}}});assert.equal((await anonymous.request('/v1/wallet')).body.error.code,'GOOGLE_LOGIN_REQUIRED');
});
test('HTTP bounded JSON and exact body schemas reject arbitrary grants and client-provided prices',async t=>{
  const f=await start(t);
  for(const [path,body,status,code]of [
    ['/v1/wallet/spend',{requestId:'body-test-1',sku:'skin:lagoon',price:0},400,'INVALID_BODY'],
    ['/v1/purchases/verify',{productId:'luma_gold_500',purchaseToken:'x',packageName:'com.fake'},400,'INVALID_BODY'],
    ['/v1/wallet/grant',{amount:99999},404,'NOT_FOUND'],
    ['/v1/wallet/spend','{',400,'INVALID_JSON'],
    ['/v1/wallet/spend','x'.repeat(16385),413,'BODY_TOO_LARGE'],
    ['/v1/wallet/spend',[],400,'INVALID_BODY']
  ]){const response=await f.request(path,body);assert.equal(response.status,status);assert.equal(response.body.error.code,code);}
  const plain=await f.request('/v1/wallet/spend','{}',{headers:{Authorization:'Bearer valid','Content-Type':'text/plain'}});assert.equal(plain.status,415);
  assert.equal((await f.request('/v1/wallet?purchaseToken=secret')).body.error.code,'QUERY_NOT_ALLOWED');
});
test('CORS permits only exact configured origin and headers for external account deletion',async t=>{
  const f=await start(t);
  const preflight=await f.request('/v1/account',undefined,{method:'OPTIONS',headers:{Origin:'https://nabz0r.github.io','Access-Control-Request-Method':'DELETE','Access-Control-Request-Headers':'authorization,content-type'}});
  assert.equal(preflight.status,204);assert.equal(preflight.headers.get('access-control-allow-origin'),'https://nabz0r.github.io');assert.equal(preflight.headers.get('access-control-allow-credentials'),null);
  const wrong=await f.request('/v1/account',undefined,{method:'OPTIONS',headers:{Origin:'https://nabz0r.github.io.attacker.example','Access-Control-Request-Method':'DELETE'}});assert.equal(wrong.status,403);assert.equal(wrong.headers.get('access-control-allow-origin'),null);
  const extra=await f.request('/v1/account',undefined,{method:'OPTIONS',headers:{Origin:'https://nabz0r.github.io','Access-Control-Request-Method':'DELETE','Access-Control-Request-Headers':'x-admin'}});assert.equal(extra.status,403);
});
test('durable user rate limit holds across two HTTP service instances',async t=>{
  const shared=fixture(),a=await start(t,{fixture:shared,userLimit:3}),b=await start(t,{fixture:shared,userLimit:3});
  assert.equal((await a.request('/v1/wallet')).status,200);assert.equal((await b.request('/v1/wallet')).status,200);assert.equal((await a.request('/v1/wallet')).status,200);
  const limited=await b.request('/v1/wallet');assert.equal(limited.status,429);assert.equal(limited.headers.get('retry-after'),'60');shared.advance(61000);assert.equal((await b.request('/v1/wallet')).status,200);
});
test('RTDN and maintenance require different trusted OIDC service identities and exact subscription',async t=>{
  const f=await start(t);
  assert.equal((await f.request('/internal/rtdn',notification('x'))).status,403);
  const wrong=notification('x');wrong.subscription='projects/other/subscriptions/fake';assert.equal((await f.request('/internal/rtdn',wrong,{headers:{Authorization:'Bearer trusted-pubsub','Content-Type':'application/json'}})).status,403);
  const accepted=await f.request('/internal/rtdn',notification('x'),{headers:{Authorization:'Bearer trusted-pubsub','Content-Type':'application/json'}});assert.equal(accepted.status,200);
  assert.equal((await f.request('/internal/maintenance',{}, {headers:{Authorization:'Bearer trusted-pubsub','Content-Type':'application/json'}})).status,403);
  assert.equal((await f.request('/internal/maintenance',{}, {headers:{Authorization:'Bearer trusted-scheduler','Content-Type':'application/json'}})).status,200);
});
test('HTTP deletion requires fresh Google login and erases authoritative wallet',async t=>{
  const f=await start(t);f.play.add('delete-http');await f.request('/v1/purchases/verify',{productId:'luma_gold_500',purchaseToken:'delete-http'});
  const deleted=await f.request('/v1/account',undefined,{method:'DELETE'});assert.equal(deleted.status,200);assert.deepEqual(deleted.body,{deleted:true});assert.equal((await f.request('/v1/wallet')).status,410);
  const stale=await start(t,{claims:{auth_time:0}});assert.equal((await stale.request('/v1/account',undefined,{method:'DELETE'})).body.error.code,'RECENT_LOGIN_REQUIRED');
});
test('provider failures return safe errors without leaking purchase or user details',async t=>{
  const f=await start(t);f.play.getPurchase=async()=>{throw new Error('secret purchase-token firebase-email@example.com');};
  const response=await f.request('/v1/purchases/verify',{productId:'luma_gold_500',purchaseToken:'private'});assert.equal(response.status,503);assert.deepEqual(response.body,{error:{code:'SERVICE_UNAVAILABLE'}});
});
test('disconnecting clients cannot release in-flight slots while authentication work continues',async t=>{
  const pending=[];let entered;const bothEntered=new Promise(resolve=>{entered=resolve;});let blocking=true;
  const f=await start(t,{maxConcurrentRequests:2,auth:{verifyIdToken:async()=>{
    if(blocking)await new Promise(resolve=>{pending.push(resolve);if(pending.length===2)entered();});
    return {uid:UID,firebase:{sign_in_provider:'google.com'},auth_time:1780000000};
  }}});
  const sockets=Array.from({length:2},()=>{const req=httpRequest(`${f.base}/v1/wallet`,{headers:{Authorization:'Bearer valid'}});req.on('error',()=>{});req.end();return req;});
  await bothEntered;for(const req of sockets)req.destroy();
  try{
    assert.equal((await f.request('/v1/wallet')).status,429);
    for(const path of ['/health','/healthz']){
      const response=await f.request(path,undefined,{headers:{}});
      assert.equal(response.status,200,path);assert.deepEqual(response.body,{status:'ok'},path);
    }
  }finally{blocking=false;for(const resolve of pending)resolve();await new Promise(resolve=>setImmediate(resolve));}
  assert.equal((await f.request('/v1/wallet')).status,200);
});
