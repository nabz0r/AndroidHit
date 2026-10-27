import test from 'node:test';
import assert from 'node:assert/strict';
import { readConfig } from '../src/config.js';
import { GooglePlay, InternalOidcAuth } from '../src/production.js';

const env={GOOGLE_CLOUD_PROJECT:'luma-drift-production',SERVICE_URL:'https://luma-wallet-123456789012.europe-west1.run.app',PUBSUB_PUSH_SERVICE_ACCOUNT:'luma-rtdn@luma-drift-production.iam.gserviceaccount.com',SCHEDULER_SERVICE_ACCOUNT:'luma-scheduler@luma-drift-production.iam.gserviceaccount.com',RTDN_SUBSCRIPTION:'projects/luma-drift-production/subscriptions/luma-rtdn',ALLOWED_WEB_ORIGINS:'https://nabz0r.github.io'};
test('production config fails closed on missing IDs, private keyfiles, emulators and unsafe CORS',()=>{
  assert.equal(readConfig(env).packageName,'com.androidhit.lumadrift');assert.equal(readConfig(env).paidCosmeticDrawsEnabled,false);assert.equal(readConfig({...env,PAID_COSMETIC_DRAWS_ENABLED:'true'}).paidCosmeticDrawsEnabled,true);
  for(const overrides of [{GOOGLE_CLOUD_PROJECT:''},{SERVICE_URL:'http://localhost:8080'},{SERVICE_URL:'https://example.com/'},{ALLOWED_WEB_ORIGINS:'*'},{ALLOWED_WEB_ORIGINS:'https://example.com/path'},{GOOGLE_APPLICATION_CREDENTIALS:'/secret/key.json'},{FIRESTORE_EMULATOR_HOST:'127.0.0.1:8080'},{FIREBASE_AUTH_EMULATOR_HOST:'127.0.0.1:9099'},{AUDIT_RETENTION_DAYS:'-1'},{PUBSUB_PUSH_SERVICE_ACCOUNT:'user@gmail.com'},{ALLOW_TEST_PURCHASES:'yes'},{PAID_COSMETIC_DRAWS_ENABLED:'yes'},{PORT:'0'}])assert.throws(()=>readConfig({...env,...overrides}));
});
test('Google Play adapter pins API host, encodes opaque tokens and uses productsv2, consume, partial-refund pagination',async()=>{
  const requests=[],play=new GooglePlay({request:async options=>{requests.push(options);return {data:{ok:true}};}});
  await play.getPurchase('com.androidhit.lumadrift','opaque/token+plus');await play.consume('com.androidhit.lumadrift','luma_gold_500','opaque/token+plus');await play.listVoided('com.androidhit.lumadrift',{startTime:123,pageToken:'next/page'});
  assert.equal(requests[0].url,'https://androidpublisher.googleapis.com/androidpublisher/v3/applications/com.androidhit.lumadrift/purchases/productsv2/tokens/opaque%2Ftoken%2Bplus');
  assert.equal(requests[1].method,'POST');assert.ok(requests[1].url.endsWith('/products/luma_gold_500/tokens/opaque%2Ftoken%2Bplus:consume'));assert.equal(requests[1].data,undefined);
  const query=new URL(requests[2].url).searchParams;assert.equal(query.get('includeQuantityBasedPartialRefund'),'true');assert.equal(query.get('pageSelection.token'),'next/page');assert.ok(requests.every(value=>value.timeout===10000));
});
test('upstream invalid purchases and missing API permissions map to safe distinct failure classes',async()=>{
  for(const [status,code]of [[400,'INVALID_PURCHASE'],[404,'INVALID_PURCHASE'],[401,'PLAY_UNAVAILABLE'],[403,'PLAY_UNAVAILABLE'],[500,'PLAY_UNAVAILABLE']]){
    const play=new GooglePlay({request:async()=>{throw {response:{status},message:'secret token'};}});await assert.rejects(()=>play.getPurchase('com.androidhit.lumadrift','private'),{code});
  }
});
test('OIDC verifier checks Google signature/audience via library plus exact service email, issuer and email_verified',async()=>{
  const claims={email:env.PUBSUB_PUSH_SERVICE_ACCOUNT,email_verified:true,iss:'https://accounts.google.com'};
  let audience;
  const client={verifyIdToken:async options=>{audience=options.audience;return {getPayload:()=>claims};}};
  const auth=new InternalOidcAuth({client,audience:env.SERVICE_URL,pubsubEmail:env.PUBSUB_PUSH_SERVICE_ACCOUNT,schedulerEmail:env.SCHEDULER_SERVICE_ACCOUNT});
  await auth.verify('signed','pubsub');assert.equal(audience,env.SERVICE_URL);await assert.rejects(()=>auth.verify('signed','scheduler'),{code:'INVALID_SERVICE_IDENTITY'});
  claims.email_verified=false;await assert.rejects(()=>auth.verify('signed','pubsub'),{code:'INVALID_SERVICE_IDENTITY'});claims.email_verified=true;claims.iss='https://attacker.example';await assert.rejects(()=>auth.verify('signed','pubsub'),{code:'INVALID_SERVICE_IDENTITY'});
});
