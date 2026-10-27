import { randomBytes } from 'node:crypto';
import { AppError, hash, validateUid, validateRequest } from './core.js';

export const SOCIAL_LIMITS = Object.freeze({ friends:40, pending:20, requestsPerUtcDay:20, giftsPerUtcDay:3, giftsPerPairUtcDay:1, giftClaimsPerUtcDay:5, giftLifetimeDays:7 });
const DAY=86400000, GIFT_MS=7*DAY, RECEIPT_MS=30*DAY;
const pk=owner=>`socialProfiles/${owner}`;
const wk=owner=>`wallets/${owner}`;
const ck=code=>`socialCodes/${code}`;
const gk=id=>`socialGifts/${id}`;
const rk=(owner,id)=>`socialRequests/${hash(`${owner}\n${id}`)}`;
const blank=(owner,code)=>({owner,code,nickname:'Luma',avatar:'nova',level:1,shareProgress:false,friends:{},incoming:{},outgoing:{},inbox:{},sentDay:-1,sentCount:0,sentTo:[],requestDay:-1,requestCount:0,claimedDay:-1,claimedCount:0});
const active=w=>{if(w&&w.state!=='ACTIVE')throw new AppError(410,'ACCOUNT_DELETED');};
const codeValue=value=>{if(typeof value!=='string'||!/^[A-Z0-9]{16}$/u.test(value))throw new AppError(400,'INVALID_FRIEND_CODE');return value;};
const giftValue=value=>{if(typeof value!=='string'||!/^[a-f0-9]{64}$/u.test(value))throw new AppError(400,'INVALID_GIFT_ID');return value;};
function prune(p,now){for(const key of ['incoming','outgoing','inbox'])for(const [id,value]of Object.entries(p[key]))if(value.expiresAtMs<=now)delete p[key][id];return p;}
function projection(p,friend=false){return {code:p.code,nickname:p.nickname,avatar:p.avatar,level:friend&&!p.shareProgress?null:p.level,shareProgress:p.shareProgress,progressVerified:false};}
/** Bounded mutual relationships. No emails, Firebase UIDs or public profile search are returned. */
export class SocialService {
  constructor({store,now=Date.now,random=()=>randomBytes(10).toString('hex').slice(0,16).toUpperCase()}){this.store=store;this.now=now;this.random=random;}
  async ensure(uid){
    const owner=hash(validateUid(uid));
    for(let attempt=0;attempt<8;attempt++){
      const code=codeValue(this.random());
      const value=await this.store.transact([pk(owner),wk(owner),ck(code)],tx=>{
        active(tx.get(wk(owner)));const current=tx.get(pk(owner));if(current)return current;
        if(tx.get(ck(code)))return null;
        const profile=blank(owner,code);tx.set(pk(owner),profile);tx.set(ck(code),{owner});return profile;
      });
      if(value)return value;
    }
    throw new AppError(503,'CODE_UNAVAILABLE');
  }
  async snapshot(uid){
    const initial=await this.ensure(uid), owner=initial.owner;
    const linked=[...new Set([...Object.keys(initial.friends),...Object.keys(initial.incoming),...Object.keys(initial.outgoing)])];
    const giftIds=Object.keys(initial.inbox);
    return this.store.transact([pk(owner),wk(owner),...linked.map(pk),...giftIds.map(gk)],tx=>{
      active(tx.get(wk(owner)));const me=prune(tx.get(pk(owner)),this.now());
      const friends=[],incoming=[],outgoing=[],gifts=[];
      for(const id of linked){const other=tx.get(pk(id));if(!other)continue;
        if(me.friends[id]&&other.friends[owner])friends.push(projection(other,true));
        if(me.incoming[id]&&other.outgoing[owner])incoming.push(projection(other,true));
        if(me.outgoing[id]&&other.incoming[owner])outgoing.push(projection(other,true));
      }
      for(const id of giftIds){if(!me.inbox[id])continue;const gift=tx.get(gk(id));if(gift&&gift.recipient===owner&&gift.expiresAtMs>this.now())gifts.push({id,fromName:gift.fromName,fromAvatar:gift.fromAvatar,reward:gift.reward,claimed:gift.claimed,expiresAtMs:gift.expiresAtMs});}
      tx.set(pk(owner),me);const sent=me.sentDay===Math.floor(this.now()/DAY)?me.sentCount:0;
      return {me:projection(me),friends,incoming,outgoing,gifts,limits:{...SOCIAL_LIMITS,giftsRemaining:SOCIAL_LIMITS.giftsPerUtcDay-sent},serverTimeMs:this.now()};
    });
  }
  async profile(uid,{nickname,avatar,level,shareProgress}){
    if(typeof nickname!=='string'||nickname.length>32||!nickname.trim()||/[\p{Cc}\p{Cf}<>]/u.test(nickname))throw new AppError(400,'INVALID_NICKNAME');
    if(typeof avatar!=='string'||!/^[a-z][a-z0-9_]{0,31}$/u.test(avatar))throw new AppError(400,'INVALID_AVATAR');
    if(!Number.isSafeInteger(level)||typeof shareProgress!=='boolean')throw new AppError(400,'INVALID_PROFILE');
    const initial=await this.ensure(uid);return this.store.transact([pk(initial.owner),wk(initial.owner)],tx=>{
      active(tx.get(wk(initial.owner)));const p=tx.get(pk(initial.owner));Object.assign(p,{nickname:nickname.trim(),avatar,level:Math.max(1,Math.min(1000,level)),shareProgress});tx.set(pk(initial.owner),p);return {me:projection(p)};
    });
  }
  async target(uid,code){
    codeValue(code);const me=await this.ensure(uid);if(me.code===code)throw new AppError(400,'SELF_FRIEND');
    const target=await this.store.read(ck(code));if(!target)throw new AppError(404,'FRIEND_NOT_FOUND');return {owner:me.owner,target:target.owner,code};
  }
  async pair(uid,code,requestId,action,fn){
    validateRequest(requestId);const {owner,target}=await this.target(uid,code), receipt=rk(owner,requestId), now=this.now();
    return this.store.transact([pk(owner),pk(target),wk(owner),wk(target),ck(code),receipt],tx=>{
      active(tx.get(wk(owner)));const a=tx.get(pk(owner)),b=tx.get(pk(target));
      if(!a||!b||tx.get(ck(code))?.owner!==target||tx.get(wk(target))&&tx.get(wk(target)).state!=='ACTIVE')throw new AppError(404,'FRIEND_NOT_FOUND');
      const previous=tx.get(receipt),binding=`${action}:${target}`;
      if(previous){if(previous.binding!==binding)throw new AppError(409,'REQUEST_CONFLICT');return previous.result;}
      prune(a,now);prune(b,now);const result=fn(a,b,tx);
      tx.set(pk(owner),a);tx.set(pk(target),b);tx.set(receipt,{owner,binding,result,expiresAtMs:now+RECEIPT_MS});return result;
    });
  }
  async request(uid,code,requestId){return this.pair(uid,code,requestId,'request',(a,b)=>{
    if(a.friends[b.owner]||a.outgoing[b.owner])return {ok:true};
    if(a.incoming[b.owner])throw new AppError(409,'INCOMING_REQUEST_EXISTS');
    const day=Math.floor(this.now()/DAY);if(a.requestDay!==day){a.requestDay=day;a.requestCount=0;}
    if(a.requestCount>=SOCIAL_LIMITS.requestsPerUtcDay)throw new AppError(429,'FRIEND_REQUEST_QUOTA');
    if(Object.keys(a.friends).length>=SOCIAL_LIMITS.friends||Object.keys(b.friends).length>=SOCIAL_LIMITS.friends||Object.keys(a.outgoing).length>=SOCIAL_LIMITS.pending||Object.keys(b.incoming).length>=SOCIAL_LIMITS.pending)throw new AppError(409,'FRIEND_LIMIT');
    a.requestCount++;a.outgoing[b.owner]={expiresAtMs:this.now()+GIFT_MS};b.incoming[a.owner]={expiresAtMs:this.now()+GIFT_MS};return {ok:true};
  });}
  async respond(uid,code,accept,requestId){
    if(typeof accept!=='boolean')throw new AppError(400,'INVALID_RESPONSE');
    return this.pair(uid,code,requestId,accept?'accept':'decline',(a,b)=>{
      if(accept&&a.friends[b.owner]&&b.friends[a.owner])return {ok:true};
      if(!a.incoming[b.owner]||!b.outgoing[a.owner])throw new AppError(404,'REQUEST_NOT_FOUND');
      if(accept){if(Object.keys(a.friends).length>=SOCIAL_LIMITS.friends||Object.keys(b.friends).length>=SOCIAL_LIMITS.friends)throw new AppError(409,'FRIEND_LIMIT');a.friends[b.owner]=true;b.friends[a.owner]=true;}
      delete a.incoming[b.owner];delete b.outgoing[a.owner];return {ok:true};
    });
  }
  async remove(uid,code,requestId){return this.pair(uid,code,requestId,'remove',(a,b)=>{
    for(const field of ['friends','incoming','outgoing']){delete a[field][b.owner];delete b[field][a.owner];}return {ok:true};
  });}
  async gift(uid,code,requestId){
    validateRequest(requestId);const {owner,target}=await this.target(uid,code),day=Math.floor(this.now()/DAY),id=hash(`${owner}\n${target}\n${day}`),receipt=rk(owner,requestId),now=this.now();
    return this.store.transact([pk(owner),pk(target),wk(owner),wk(target),ck(code),gk(id),receipt],tx=>{
      active(tx.get(wk(owner)));const a=tx.get(pk(owner)),b=tx.get(pk(target));
      if(!a||!b||tx.get(ck(code))?.owner!==target||tx.get(wk(target))&&tx.get(wk(target)).state!=='ACTIVE')throw new AppError(404,'FRIEND_NOT_FOUND');
      const prior=tx.get(receipt),binding=`gift:${target}`;
      if(prior){if(prior.binding!==binding)throw new AppError(409,'REQUEST_CONFLICT');return prior.result;}
      if(!a.friends[target]||!b.friends[owner])throw new AppError(403,'FRIEND_REQUIRED');
      if(tx.get(gk(id)))return {ok:true,giftId:id,duplicate:true};
      prune(a,now);prune(b,now);
      if(a.sentDay!==day){a.sentDay=day;a.sentCount=0;a.sentTo=[];}
      if(a.sentCount>=SOCIAL_LIMITS.giftsPerUtcDay)throw new AppError(429,'GIFT_DAILY_LIMIT');
      if(a.sentTo.includes(target))throw new AppError(409,'GIFT_PAIR_LIMIT');
      if(Object.keys(b.inbox).length>=64)throw new AppError(409,'GIFT_INBOX_FULL');
      const gift={sender:owner,recipient:target,fromName:a.nickname,fromAvatar:a.avatar,reward:{kind:'power',itemId:'hint',amount:1},claimed:false,createdAtMs:now,expiresAtMs:now+GIFT_MS};
      a.sentCount++;a.sentTo.push(target);b.inbox[id]={expiresAtMs:gift.expiresAtMs};
      const result={ok:true,giftId:id};tx.set(pk(owner),a);tx.set(pk(target),b);tx.set(gk(id),gift);tx.set(receipt,{owner,binding,result,expiresAtMs:now+RECEIPT_MS});return result;
    });
  }
  async claim(uid,giftId,requestId){
    validateUid(uid);validateRequest(requestId);giftValue(giftId);const owner=hash(uid),receipt=rk(owner,requestId);
    return this.store.transact([pk(owner),wk(owner),gk(giftId),receipt],tx=>{
      active(tx.get(wk(owner)));const profile=tx.get(pk(owner)),gift=tx.get(gk(giftId));
      if(!profile||!gift||gift.recipient!==owner)throw new AppError(404,'GIFT_NOT_FOUND');
      if(gift.expiresAtMs<=this.now())throw new AppError(410,'GIFT_EXPIRED');
      const prior=tx.get(receipt),binding=`claim:${giftId}`;if(prior&&prior.binding!==binding)throw new AppError(409,'REQUEST_CONFLICT');
      const grant={id:giftId,...gift.reward};if(!gift.claimed){const day=Math.floor(this.now()/DAY);if(profile.claimedDay!==day){profile.claimedDay=day;profile.claimedCount=0;}if(profile.claimedCount>=SOCIAL_LIMITS.giftClaimsPerUtcDay)throw new AppError(429,'GIFT_CLAIM_DAILY_LIMIT');profile.claimedCount++;gift.claimed=true;gift.claimedAtMs=this.now();tx.set(gk(giftId),gift);tx.set(pk(owner),profile);}
      const result={ok:true,grant};if(!prior)tx.set(receipt,{owner,binding,result,expiresAtMs:this.now()+RECEIPT_MS});return result;
    });
  }
  /** Runs inside the wallet's durable deletion job before Firebase Auth is removed. */
  async deleteOwner(uid){
    const owner=hash(validateUid(uid)),initial=await this.store.read(pk(owner));
    if(initial){
      const peers=[...new Set([...Object.keys(initial.friends),...Object.keys(initial.incoming),...Object.keys(initial.outgoing)])];
      await this.store.transact([pk(owner),ck(initial.code),...peers.map(pk)],tx=>{
        for(const peer of peers){const p=tx.get(pk(peer));if(!p)continue;for(const field of ['friends','incoming','outgoing'])delete p[field][owner];tx.set(pk(peer),p);}
        tx.delete(pk(owner));tx.delete(ck(initial.code));
      });
    }
    for(const field of ['sender','recipient'])while(true){const rows=await this.store.find('socialGifts',{field,value:owner,limit:100});if(!rows.length)break;for(const {key,value}of rows)await this.store.transact([key,pk(value.recipient)],tx=>{const recipient=tx.get(pk(value.recipient));if(recipient){delete recipient.inbox[key.split('/')[1]];tx.set(pk(value.recipient),recipient);}tx.delete(key);});}
    while(true){const rows=await this.store.find('socialRequests',{field:'owner',value:owner,limit:100});if(!rows.length)break;for(const {key}of rows)await this.store.transact([key],tx=>tx.delete(key));}
  }
  async maintenance(batchSize=100){
    for(const collection of ['socialGifts','socialRequests'])for(const {key}of await this.store.find(collection,{field:'expiresAtMs',op:'<=',value:this.now(),limit:batchSize}))await this.store.transact([key],tx=>{const value=tx.get(key);if(value?.expiresAtMs<=this.now())tx.delete(key);});
  }
}
