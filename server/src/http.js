import { createServer } from 'node:http';
import { AppError, hash, validateUid } from './core.js';
import { enforceUserRate } from './store.js';

function bearer(req) {
  const value=req.headers.authorization;
  if(typeof value!=='string'||value.length>8192||!/^Bearer [A-Za-z0-9._~-]+$/u.test(value))throw new AppError(401,'UNAUTHENTICATED');
  return value.slice(7);
}
function exactObject(body,keys) {
  if(!body||Array.isArray(body)||typeof body!=='object'||Object.keys(body).some(key=>!keys.includes(key))||keys.some(key=>!Object.hasOwn(body,key)))throw new AppError(400,'INVALID_BODY');
  return body;
}
async function readBody(req,limit=16384) {
  const length=req.headers['content-length'];
  if(length!==undefined&&(!/^\d+$/u.test(length)||Number(length)>limit))throw new AppError(413,'BODY_TOO_LARGE');
  const type=req.headers['content-type'];
  if(type&&!/^application\/json(?:\s*;\s*charset=utf-8)?$/iu.test(type))throw new AppError(415,'JSON_REQUIRED');
  let size=0;const chunks=[];
  for await(const chunk of req){size+=chunk.length;if(size>limit)throw new AppError(413,'BODY_TOO_LARGE');chunks.push(chunk);}
  if(!size)return {};
  if(!type)throw new AppError(415,'JSON_REQUIRED');
  try{return JSON.parse(Buffer.concat(chunks).toString('utf8'));}catch{throw new AppError(400,'INVALID_JSON');}
}
function send(res,status,body) {
  res.statusCode=status;
  if(body===undefined){res.end();return;}
  res.setHeader('Content-Type','application/json; charset=utf-8');res.end(JSON.stringify(body));
}

export function createHttpServer({ service, auth, internalAuth, store, allowedOrigins=[], subscription, now=Date.now, maxConcurrentRequests=64, userLimit=60, onError=()=>{} }) {
  const origins=new Set(allowedOrigins);let activeRequests=0;
  return createServer({maxHeaderSize:16384,requestTimeout:15000,headersTimeout:10000},async(req,res)=>{
    let counted=false;
    res.setHeader('Cache-Control','no-store');res.setHeader('X-Content-Type-Options','nosniff');res.setHeader('X-Frame-Options','DENY');res.setHeader('Strict-Transport-Security','max-age=31536000');
    try{
      // Cloud Run terminates TLS behind a proxy: socket.remoteAddress is not a
      // reliable player IP. Bound in-flight work and rate-limit verified UIDs;
      // configure any IP policy at a trusted load-balancer edge instead.
      // Cloud Run reserves some paths ending in z. Use /health publicly;
      // retain /healthz only for existing local probes.
      if(req.url!=='/health'&&req.url!=='/healthz'){
        if(activeRequests>=maxConcurrentRequests)throw new AppError(429,'RATE_LIMITED');
        activeRequests++;counted=true;
      }
      const origin=req.headers.origin;
      if(origin){
        if(!origins.has(origin))throw new AppError(403,'ORIGIN_NOT_ALLOWED');
        res.setHeader('Access-Control-Allow-Origin',origin);res.setHeader('Vary','Origin');
      }
      if(req.method==='OPTIONS'){
        if(!origin)throw new AppError(400,'ORIGIN_REQUIRED');
        const requested=req.headers['access-control-request-headers'];
        if(requested&&requested.toLowerCase().split(',').some(name=>!['authorization','content-type'].includes(name.trim())))throw new AppError(403,'HEADER_NOT_ALLOWED');
        if(!['GET','POST','DELETE'].includes(req.headers['access-control-request-method']))throw new AppError(403,'METHOD_NOT_ALLOWED');
        res.setHeader('Access-Control-Allow-Methods','GET, POST, DELETE, OPTIONS');res.setHeader('Access-Control-Allow-Headers','Authorization, Content-Type');res.setHeader('Access-Control-Max-Age','600');send(res,204);return;
      }
      const path=new URL(req.url,'http://localhost').pathname;
      if(req.url.includes('?'))throw new AppError(400,'QUERY_NOT_ALLOWED');
      if((path==='/health'||path==='/healthz')&&req.method==='GET'){send(res,200,{status:'ok'});return;}
      if(path==='/internal/rtdn'&&req.method==='POST'){
        await internalAuth.verify(bearer(req),'pubsub');
        const body=await readBody(req,65536);
        if(!subscription||body.subscription!==subscription)throw new AppError(403,'SUBSCRIPTION_MISMATCH');
        send(res,200,await service.handleNotification(body));return;
      }
      if(path==='/internal/maintenance'&&req.method==='POST'){
        await internalAuth.verify(bearer(req),'scheduler');exactObject(await readBody(req),[]);
        send(res,200,await service.maintenance());return;
      }
      const supported=(['/v1/wallet','/v1/inventory'].includes(path)&&req.method==='GET')||(['/v1/purchases/verify','/v1/wallet/spend','/v1/inventory/use'].includes(path)&&req.method==='POST')||(path==='/v1/account'&&req.method==='DELETE');
      if(!supported)throw new AppError(404,'NOT_FOUND');
      let claims;
      try{claims=await auth.verifyIdToken(bearer(req),true);}catch(error){if(error instanceof AppError)throw error;throw new AppError(401,'UNAUTHENTICATED');}
      const uid=validateUid(claims.uid);
      if(claims.firebase?.sign_in_provider!=='google.com')throw new AppError(403,'GOOGLE_LOGIN_REQUIRED');
      await enforceUserRate(store,hash(uid),now(),userLimit);
      if(req.method==='GET'){send(res,200,await service.getWallet(uid));return;}
      const body=await readBody(req);
      if(path==='/v1/account'){exactObject(body,[]);send(res,200,await service.deleteAccount(uid,claims.auth_time));return;}
      if(path==='/v1/purchases/verify'){exactObject(body,['productId','purchaseToken']);send(res,200,await service.verifyPurchase(uid,body.productId,body.purchaseToken));return;}
      exactObject(body,['requestId','sku']);
      send(res,200,await (path==='/v1/wallet/spend'?service.spend(uid,body.requestId,body.sku):service.use(uid,body.requestId,body.sku)));
    }catch(error){
      const safe=error instanceof AppError?error:new AppError(503,'SERVICE_UNAVAILABLE');
      if(!(error instanceof AppError))onError({code:'UNEXPECTED_SERVICE_ERROR'});
      if(safe.status===429)res.setHeader('Retry-After','60');
      if(!res.headersSent)send(res,safe.status,{error:{code:safe.code}});else res.end();
    }finally{if(counted)activeRequests--;}
  });
}
