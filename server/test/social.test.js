import test from 'node:test';
import assert from 'node:assert/strict';
import { fixture } from './helpers.js';
import { SocialService } from '../src/social.js';
import { hash } from '../src/core.js';
const A='social-user-a', B='social-user-b';
async function connect(f,a=A,b=B){const x=await f.service.social.snapshot(a),y=await f.service.social.snapshot(b);await f.service.social.request(a,y.me.code,`invite-${hash(b).slice(0,12)}`);await f.service.social.respond(b,x.me.code,true,`accept-${hash(a).slice(0,12)}`);return [x.me.code,y.me.code];}
test('social profile is private by default, clamps map level, exposes no identity UID or email',async()=>{
 const f=fixture(),s=f.service.social;const [a,b]=await connect(f);
 assert.match(a,/^[A-Z0-9]{16}$/u);assert.notEqual(a,b);
 await s.profile(A,{nickname:'Nova ✨',avatar:'nova',level:5000,shareProgress:false});
 let view=await s.snapshot(B);assert.equal(view.friends[0].level,null);assert.equal(view.friends[0].nickname,'Nova ✨');assert.equal(view.friends[0].progressVerified,false);assert.ok(!JSON.stringify(view).includes(A));
 await s.profile(A,{nickname:'Nova',avatar:'nova',level:5000,shareProgress:true});view=await s.snapshot(B);assert.equal(view.friends[0].level,1000);
 await s.profile(A,{nickname:'Nova',avatar:'nova',level:-9,shareProgress:true});assert.equal((await s.snapshot(B)).friends[0].level,1);
 for(const bad of [{nickname:'<script>'},{level:1.5},{shareProgress:'true'},{avatar:'https://evil.example/x'},{nickname:'\u202efake'}])await assert.rejects(()=>s.profile(A,{nickname:'Nova',avatar:'nova',level:5,shareProgress:true,...bad}),{status:400});
});
test('friend requests need explicit acceptance, expire, decline, withdraw and remove mutually',async()=>{
 const f=fixture(),s=f.service.social,a=(await s.snapshot(A)).me.code,b=(await s.snapshot(B)).me.code;
 await assert.rejects(()=>s.request(A,a,'self-00001'),{code:'SELF_FRIEND'});
 await assert.rejects(()=>s.request(A,'BAD','invalid-01'),{code:'INVALID_FRIEND_CODE'});
 await assert.rejects(()=>s.request(A,'0000000000000000','unknown-01'),{code:'FRIEND_NOT_FOUND'});
 await s.request(A,b,'request-0001');assert.equal((await s.snapshot(A)).friends.length,0);assert.equal((await s.snapshot(B)).incoming.length,1);
 await assert.rejects(()=>s.request(B,a,'cross-00001'),{code:'INCOMING_REQUEST_EXISTS'});
 await s.respond(B,a,false,'decline-001');assert.equal((await s.snapshot(A)).outgoing.length,0);
 await s.request(A,b,'request-0002');f.advance(8*86400000);await assert.rejects(()=>s.respond(B,a,true,'accept-old01'),{code:'REQUEST_NOT_FOUND'});
 await s.request(A,b,'request-0003');await s.remove(A,b,'withdraw001');assert.equal((await s.snapshot(B)).incoming.length,0);
 await s.request(A,b,'request-0004');await s.respond(B,a,true,'accept-new01');assert.equal((await s.snapshot(A)).friends.length,1);
 await s.remove(A,b,'remove-0001');assert.equal((await s.snapshot(B)).friends.length,0);
});
test('shared request IDs are idempotent and cannot be rebound to another social operation',async()=>{
 const f=fixture(),s=f.service.social,a=(await s.snapshot(A)).me.code,b=(await s.snapshot(B)).me.code;
 await Promise.all(Array.from({length:12},()=>s.request(A,b,'same-request1')));
 assert.equal((await s.snapshot(B)).incoming.length,1);await assert.rejects(()=>s.remove(A,b,'same-request1'),{code:'REQUEST_CONFLICT'});
 await s.respond(B,a,true,'same-accept1');await s.respond(B,a,true,'same-accept1');assert.equal((await s.snapshot(B)).friends.length,1);
});
test('gifts are server-fixed earned hint packets with concurrent exactly-once send and claim receipts',async()=>{
 const f=fixture(),s=f.service.social,[a,b]=await connect(f);
 const gifts=await Promise.all(Array.from({length:20},(_,i)=>s.gift(A,b,`gift-send-${i}`)));assert.equal(new Set(gifts.map(x=>x.giftId)).size,1);
 const view=await s.snapshot(B);assert.equal(view.gifts.length,1);assert.deepEqual(view.gifts[0].reward,{kind:'power',itemId:'hint',amount:1});assert.equal((await s.snapshot(A)).limits.giftsRemaining,2);
 const id=gifts[0].giftId,claims=await Promise.all(Array.from({length:15},(_,i)=>s.claim(B,id,`gift-claim-${i}`)));assert.ok(claims.every(x=>x.grant.id===id));assert.equal((await s.snapshot(B)).gifts[0].claimed,true);
 await assert.rejects(()=>s.claim(A,id,'steal-claim1'),{code:'GIFT_NOT_FOUND'});assert.equal((await f.service.getWallet(B)).balance,0);
 await assert.rejects(()=>s.gift(B,b,'self-gift01'),{code:'SELF_FRIEND'});
 await s.remove(A,b,'unfriend-01');await assert.rejects(()=>s.gift(A,b,'nofriend-01'),{code:'FRIEND_REQUIRED'});
 f.advance(8*86400000);await assert.rejects(()=>s.claim(B,id,'expired-001'),{code:'GIFT_EXPIRED'});await s.maintenance();assert.equal((await s.snapshot(B)).gifts.length,0);
});
test('three gifts per UTC day applies across recipients and resets only at server day boundary',async()=>{
 const f=fixture(),s=f.service.social,targets=[];
 for(let i=0;i<4;i++){const [a,b]=await connect(f,A,`gift-peer-${i}`);targets.push(b);}
 for(let i=0;i<3;i++)await s.gift(A,targets[i],`quota-gift-${i}`);
 await assert.rejects(()=>s.gift(A,targets[3],'quota-gift-4'),{code:'GIFT_DAILY_LIMIT'});
 f.advance(86400000);await s.gift(A,targets[3],'quota-gift-5');assert.equal((await s.snapshot(A)).limits.giftsRemaining,2);
});
test('pending request and daily invitation caps bound fanout',async()=>{
 const f=fixture(),s=f.service.social;
 for(let i=0;i<20;i++){const b=(await s.snapshot(`cap-user-${i}`)).me.code;await s.request(A,b,`cap-request-${i}`);}
 const b=(await s.snapshot('cap-user-21')).me.code;await assert.rejects(()=>s.request(A,b,'cap-request-21'),{code:'FRIEND_REQUEST_QUOTA'});
 assert.equal((await s.snapshot(A)).outgoing.length,20);
});
test('durable account deletion removes code, mutual relationships, invitations, gifts and receipts',async()=>{
 const f=fixture(),s=f.service.social,[a,b]=await connect(f);await s.gift(A,b,'delete-gift1');
 const c=(await s.snapshot('pending-peer')).me.code;await s.request(A,c,'pending-delete');
 await f.service.deleteAccount(A,f.now()/1000);
 assert.equal((await s.snapshot(B)).friends.length,0);assert.equal((await s.snapshot(B)).gifts.length,0);assert.equal((await s.snapshot('pending-peer')).incoming.length,0);
 assert.equal(await f.store.read(`socialCodes/${a}`),undefined);assert.equal(await f.store.read(`socialProfiles/${hash(A)}`),undefined);assert.equal((await f.store.find('socialRequests',{field:'owner',value:hash(A)})).length,0);
 await assert.rejects(()=>s.snapshot(A),{code:'ACCOUNT_DELETED'});
});
test('deletion retries safely after provider failure and forbids resurrection during job',async()=>{
 let fail=true;const f=fixture({identity:{deleteUser:async()=>{if(fail)throw Error('provider-down');}}});const [a,b]=await connect(f);
 await assert.rejects(()=>f.service.deleteAccount(A,f.now()/1000),{code:'DELETION_RETRY_PENDING'});
 await assert.rejects(()=>f.service.social.snapshot(A),{code:'ACCOUNT_DELETED'});assert.equal((await f.service.social.snapshot(B)).friends.length,0);
 fail=false;f.advance(6*60000);await f.service.maintenance();assert.equal(await f.store.read(`socialCodes/${a}`),undefined);
});
test('friend code collision retries without stealing an existing profile',async()=>{
 const f=fixture(),codes=['AAAAAAAAAAAAAAAA','AAAAAAAAAAAAAAAA','BBBBBBBBBBBBBBBB'];const s=new SocialService({store:f.store,random:()=>codes.shift()});
 assert.equal((await s.snapshot(A)).me.code,'AAAAAAAAAAAAAAAA');assert.equal((await s.snapshot(B)).me.code,'BBBBBBBBBBBBBBBB');
});
test('recipient claim quota bounds many-sender farming and keeps older gifts claimable tomorrow',async()=>{
 const f=fixture(),s=f.service.social,giftIds=[];
 for(let i=0;i<6;i++){const sender=`farm-sender-${i}`,[a,b]=await connect(f,sender,B);const sent=await s.gift(sender,b,`farm-gift-${i}`);giftIds.push(sent.giftId);}
 for(let i=0;i<5;i++)await s.claim(B,giftIds[i],`farm-claim-${i}`);
 await assert.rejects(()=>s.claim(B,giftIds[5],'farm-claim-5'),{code:'GIFT_CLAIM_DAILY_LIMIT'});
 assert.equal((await s.claim(B,giftIds[0],'farm-retry-0')).grant.id,giftIds[0]);
 f.advance(86400000);assert.equal((await s.claim(B,giftIds[5],'farm-claim-6')).grant.id,giftIds[5]);
});
test('expired social data is purged even when the Play refund poll is unavailable',async()=>{
 const f=fixture(),s=f.service.social,[a,b]=await connect(f);await s.gift(A,b,'purge-outage-1');f.advance(31*86400000);f.play.unavailable=true;await assert.rejects(()=>f.service.maintenance(),{code:'PLAY_UNAVAILABLE'});
 assert.equal((await f.store.find('socialGifts')).length,0);assert.equal((await f.store.find('socialRequests')).length,0);
});
test('friend capacity and both pending capacities reject further fanout atomically',async()=>{
 const f=fixture(),s=f.service.social;
 for(let i=0;i<40;i++){if(i===20)f.advance(86400000);await connect(f,'full-circle',`circle-peer-${i}`);}
 f.advance(86400000);const full=(await s.snapshot('full-circle')).me.code,extra=(await s.snapshot('circle-extra')).me.code;
 await assert.rejects(()=>s.request('full-circle',extra,'full-friend-1'),{code:'FRIEND_LIMIT'});await assert.rejects(()=>s.request('circle-extra',full,'full-friend-2'),{code:'FRIEND_LIMIT'});assert.equal((await s.snapshot('full-circle')).friends.length,40);
 const receive=(await s.snapshot('full-incoming')).me.code;
 for(let i=0;i<20;i++)await s.request(`incoming-${i}`,receive,`incoming-req-${i}`);
 await assert.rejects(()=>s.request('incoming-extra',receive,'incoming-extra-1'),{code:'FRIEND_LIMIT'});assert.equal((await s.snapshot('full-incoming')).incoming.length,20);
 for(let i=0;i<20;i++){const target=(await s.snapshot(`outgoing-${i}`)).me.code;await s.request('full-outgoing',target,`outgoing-req-${i}`);}
 f.advance(86400000);await assert.rejects(()=>s.request('full-outgoing',extra,'outgoing-extra-1'),{code:'FRIEND_LIMIT'});assert.equal((await s.snapshot('full-outgoing')).outgoing.length,20);
});
