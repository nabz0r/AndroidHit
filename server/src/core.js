import { createHash, timingSafeEqual, randomUUID } from 'node:crypto';
import { PRODUCTS, SKUS } from './catalog.js';

export class AppError extends Error {
  constructor(status, code, message = code) { super(message); this.status = status; this.code = code; }
}
export const hash = value => createHash('sha256').update(value, 'utf8').digest('hex');
const walletKey = uid => `wallets/${hash(uid)}`;
const purchaseKey = token => `purchases/${hash(token)}`;
const requestKey = (uid, id) => `requests/${hash(`${uid}\n${id}`)}`;
const deletionKey = uid => `deletions/${hash(uid)}`;
const blankWallet = () => ({ balance:0, lifetimeCredits:0, version:0, characters:[], skins:[], inventory:{ boosters:{}, continuations:0 }, state:'ACTIVE' });
const publicWallet = wallet => ({ balance:wallet.balance, version:wallet.version, characters:wallet.characters, skins:wallet.skins, inventory:wallet.inventory });
const ownerOf = uid => hash(uid);
const safeEqual = (a,b) => typeof a === 'string' && typeof b === 'string' && /^[a-f0-9]{64}$/u.test(a) && /^[a-f0-9]{64}$/u.test(b) && timingSafeEqual(Buffer.from(a), Buffer.from(b));
const MAX_BALANCE = 1_000_000_000;
const MAX_INVENTORY = 10000;
function requireActive(wallet) {
  if (wallet.state !== 'ACTIVE') throw new AppError(410, 'ACCOUNT_DELETED');
  return wallet;
}
export function validateUid(uid) {
  if (typeof uid !== 'string' || uid.length < 1 || uid.length > 128 || /[\u0000-\u001f\u007f]/u.test(uid)) throw new AppError(401, 'INVALID_IDENTITY');
  return uid;
}
export function validateToken(token) {
  // Purchase tokens are opaque; permit printable ASCII without inventing their format.
  if (typeof token !== 'string' || token.length < 1 || token.length > 4096 || !/^[\x21-\x7e]+$/u.test(token)) throw new AppError(400, 'INVALID_PURCHASE_TOKEN');
  return token;
}
export function validateRequest(id) {
  if (typeof id !== 'string' || !/^[a-zA-Z0-9_:-]{8,160}$/u.test(id)) throw new AppError(400, 'INVALID_REQUEST_ID');
  return id;
}
function validateSku(sku) {
  if (typeof sku !== 'string' || !Object.hasOwn(SKUS, sku)) throw new AppError(400, 'UNKNOWN_SKU');
  return SKUS[sku];
}
function snapshotPurchase(raw, productId, uidHash) {
  if (!raw || !Array.isArray(raw.productLineItem) || raw.productLineItem.length !== 1) throw new AppError(422, 'INVALID_PURCHASE');
  const line = raw.productLineItem[0];
  const offer = line.productOfferDetails;
  const state = raw.purchaseStateContext?.purchaseState;
  if (line.productId !== productId || !Object.hasOwn(PRODUCTS, productId) || !offer || offer.rentOfferDetails || offer.preorderOfferDetails) throw new AppError(422, 'PRODUCT_MISMATCH');
  if (!safeEqual(raw.obfuscatedExternalAccountId, uidHash)) throw new AppError(403, 'PURCHASE_OWNER_MISMATCH');
  if (!['PURCHASED','PENDING','CANCELLED'].includes(state)) throw new AppError(422, 'INVALID_PURCHASE_STATE');
  // Catalog disables multi-quantity. Reject surprise quantities rather than guess.
  const refundableQuantity=offer.refundableQuantity??0;
  if ((offer.quantity !== 1 && !(state!=='PURCHASED'&&offer.quantity===undefined)) || !Number.isInteger(refundableQuantity) || refundableQuantity < 0 || refundableQuantity > 1) throw new AppError(422, 'UNSUPPORTED_QUANTITY');
  if (state==='PURCHASED' && !['CONSUMPTION_STATE_YET_TO_BE_CONSUMED','CONSUMPTION_STATE_CONSUMED'].includes(offer.consumptionState)) throw new AppError(422, 'INVALID_CONSUMPTION_STATE');
  return { state, refundableQuantity, consumed:offer.consumptionState === 'CONSUMPTION_STATE_CONSUMED', orderId:typeof raw.orderId === 'string' ? raw.orderId.slice(0,256) : null, test:!!raw.testPurchaseContext };
}

/** All value mutations use one store transaction; provider calls never run in it. */
export class WalletService {
  constructor({ store, play, identity, packageName, now = Date.now, retentionDays = 365, allowTestPurchases = true, batchSize = 100, consumablesEnabled = false }) {
    if (!/^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*){2,}$/u.test(packageName || '')) throw new Error('Valid production packageName required');
    if (!Number.isInteger(retentionDays) || retentionDays < 30 || retentionDays > 3650) throw new Error('retentionDays must be 30..3650');
    this.store=store; this.play=play; this.identity=identity; this.packageName=packageName; this.now=now; this.retentionMs=retentionDays*86400000; this.allowTestPurchases=allowTestPurchases; this.batchSize=batchSize;this.consumablesEnabled=consumablesEnabled;
  }
  async getWallet(uid) {
    validateUid(uid);
    return this.store.transact([walletKey(uid)], tx => publicWallet(requireActive(tx.get(walletKey(uid)) || blankWallet())));
  }
  async verifyPurchase(uid, productId, token) {
    validateUid(uid); validateToken(token);
    if (typeof productId !== 'string' || !Object.hasOwn(PRODUCTS, productId)) throw new AppError(400, 'UNKNOWN_PRODUCT');
    await this.getWallet(uid);
    const existing = await this.store.read(purchaseKey(token));
    if (existing && existing.owner !== ownerOf(uid)) throw new AppError(403, 'PURCHASE_ALREADY_CLAIMED');
    if (existing?.deletedAtMs || existing?.terminal) throw new AppError(410, 'PURCHASE_REVOKED');
    const raw = await this.play.getPurchase(this.packageName, token);
    const snapshot = snapshotPurchase(raw, productId, ownerOf(uid));
    if (snapshot.test && !this.allowTestPurchases) throw new AppError(422, 'TEST_PURCHASE_DISABLED');
    const result = await this.applyPurchase(uid, productId, token, snapshot, false);
    let consumptionPending = false;
    if (result.consume) consumptionPending = !await this.consume(token);
    return { ...await this.getWallet(uid), purchase:{ status:snapshot.state, productId, credited:result.credited, consumptionPending } };
  }
  async applyPurchase(uid, productId, token, snapshot, fromServer) {
    const wk=walletKey(uid), pk=purchaseKey(token), owner=ownerOf(uid), now=this.now();
    return this.store.transact([wk,pk], tx => {
      const wallet=requireActive(tx.get(wk)||blankWallet());
      let purchase=tx.get(pk);
      if (purchase && purchase.owner !== owner) throw new AppError(403, 'PURCHASE_ALREADY_CLAIMED');
      if (purchase?.deletedAtMs || purchase?.terminal) return { credited:false, consume:false };
      // A previously consumed token absent from our ledger may belong to an older
      // installation/ledger. Never manufacture a fresh credit for it.
      if (!purchase && snapshot.consumed) throw new AppError(409, 'CONSUMED_PURCHASE_NOT_RECORDED');
      if (purchase && purchase.productId !== productId) throw new AppError(409, 'PRODUCT_MISMATCH');
      purchase ||= { owner, productId, quantity:1, purchaseToken:token, credited:0, refunded:0, createdAtMs:now, needsConsume:false, consumed:false, terminal:false,personalDataPresent:true };
      // Refund state is monotonic: an out-of-order success cannot re-credit it.
      const revoked = snapshot.state === 'CANCELLED' || (snapshot.state==='PURCHASED' && snapshot.refundableQuantity === 0);
      let credited=false;
      if (revoked) {
        const refund=purchase.credited-purchase.refunded;
        if (refund>0) { wallet.balance-=refund; wallet.version++; purchase.refunded+=refund; }
        purchase.terminal=true; purchase.needsConsume=false;
      } else if (snapshot.state === 'PURCHASED' && purchase.credited===0) {
        if (snapshot.consumed) throw new AppError(409,'CONSUMED_PURCHASE_NOT_RECORDED');
        const gold=PRODUCTS[productId];
        if (wallet.balance+gold>MAX_BALANCE || (wallet.lifetimeCredits||0)+gold>MAX_BALANCE) throw new AppError(409,'WALLET_LIMIT');
        wallet.balance+=gold; wallet.lifetimeCredits=(wallet.lifetimeCredits||0)+gold; wallet.version++; purchase.credited=gold; credited=true;
      }
      purchase.state=snapshot.state; purchase.orderId=snapshot.orderId; purchase.test=snapshot.test; purchase.checkedAtMs=now;
      if(snapshot.state==='PENDING')purchase.nextCheckAtMs=now+5*60000;else delete purchase.nextCheckAtMs;
      purchase.consumed ||= snapshot.consumed;
      purchase.needsConsume = purchase.credited>0 && !purchase.terminal && !purchase.consumed;
      if(purchase.needsConsume)purchase.nextConsumeAtMs ??= now;else delete purchase.nextConsumeAtMs;
      // uid is needed only for durable retry/deletion jobs; the purchase ledger
      // binds to a hash, not a user email/profile.
      tx.set(wk,wallet); tx.set(pk,purchase);
      return { credited, consume:purchase.needsConsume, fromServer };
    });
  }
  async consume(token) {
    const pk=purchaseKey(token), purchase=await this.store.read(pk);
    if (!purchase || !purchase.needsConsume || purchase.terminal || !purchase.purchaseToken) return true;
    try {
      await this.play.consume(this.packageName,purchase.productId,token);
    } catch {
      // A crash/network loss after Google consumed is resolved by querying state.
      try {
        const raw=await this.play.getPurchase(this.packageName,token);
        const snapshot=snapshotPurchase(raw,purchase.productId,purchase.owner);
        if (!snapshot.consumed || snapshot.state !== 'PURCHASED') {await this.retryLater(pk,'nextConsumeAtMs');return false;}
      } catch {await this.retryLater(pk,'nextConsumeAtMs');return false;}
    }
    await this.store.transact([pk],tx=>{
      const current=tx.get(pk);
      if (current && !current.deletedAtMs) {current.consumed=true;current.needsConsume=false;current.consumedAtMs=this.now();delete current.nextConsumeAtMs;tx.set(pk,current);}
    });
    return true;
  }
  async retryLater(key,field){
    await this.store.transact([key],tx=>{const current=tx.get(key);if(!current||current.deletedAtMs||current.terminal||current.state==='COMPLETE')return;tx.set(key,{...current,[field]:this.now()+5*60000});});
  }
  async spend(uid, requestId, sku) {
    validateUid(uid); validateRequest(requestId); const item=validateSku(sku);
    if(!this.consumablesEnabled&&['booster','continuation'].includes(item.kind))throw new AppError(400,'SKU_NOT_AVAILABLE');
    const wk=walletKey(uid), rk=requestKey(uid,requestId);
    return this.store.transact([wk,rk],tx=>{
      const wallet=requireActive(tx.get(wk)||blankWallet()), previous=tx.get(rk);
      if (previous) {
        if (previous.action!=='spend' || previous.sku!==sku) throw new AppError(409,'REQUEST_CONFLICT');
        return { ...publicWallet(wallet), entitlement:previous.entitlement };
      }
      if (wallet.balance<item.price) throw new AppError(409,'INSUFFICIENT_GOLD');
      if ((item.kind==='character' && wallet.characters.includes(item.itemId)) || (item.kind==='skin' && wallet.skins.includes(item.itemId))) throw new AppError(409,'ALREADY_OWNED');
      if (item.kind==='character') wallet.characters.push(item.itemId);
      if (item.kind==='skin') wallet.skins.push(item.itemId);
      if (item.kind==='booster') {
        const count=wallet.inventory.boosters[item.itemId]||0;
        if (count+item.amount>MAX_INVENTORY) throw new AppError(409,'INVENTORY_LIMIT');
        wallet.inventory.boosters[item.itemId]=count+item.amount;
      }
      if (item.kind==='continuation') {
        if (wallet.inventory.continuations>=MAX_INVENTORY) throw new AppError(409,'INVENTORY_LIMIT');
        wallet.inventory.continuations++;
      }
      wallet.balance-=item.price; wallet.version++;
      const entitlement={ id:requestId,sku,kind:item.kind,itemId:item.itemId,amount:item.amount };
      tx.set(wk,wallet); tx.set(rk,{ owner:ownerOf(uid), action:'spend',sku,entitlement,createdAtMs:this.now() });
      return { ...publicWallet(wallet),entitlement };
    });
  }
  async use(uid, requestId, sku) {
    validateUid(uid); validateRequest(requestId); const item=validateSku(sku);
    if(!this.consumablesEnabled)throw new AppError(400,'SKU_NOT_AVAILABLE');
    if (!['booster','continuation'].includes(item.kind)) throw new AppError(400,'NOT_CONSUMABLE');
    const wk=walletKey(uid),rk=requestKey(uid,requestId);
    return this.store.transact([wk,rk],tx=>{
      const wallet=requireActive(tx.get(wk)||blankWallet()),previous=tx.get(rk);
      if (previous) {
        if (previous.action!=='use' || previous.sku!==sku) throw new AppError(409,'REQUEST_CONFLICT');
        return { ...publicWallet(wallet),use:previous.use };
      }
      if (item.kind==='booster') {
        if (!(wallet.inventory.boosters[item.itemId]>0)) throw new AppError(409,'INSUFFICIENT_ITEM');
        wallet.inventory.boosters[item.itemId]--;
      } else {
        if (!(wallet.inventory.continuations>0)) throw new AppError(409,'INSUFFICIENT_ITEM');
        wallet.inventory.continuations--;
      }
      wallet.version++;
      const use={ id:requestId,sku,kind:item.kind,itemId:item.itemId,amount:1 };
      tx.set(wk,wallet);tx.set(rk,{ owner:ownerOf(uid),action:'use',sku,use,createdAtMs:this.now() });
      return { ...publicWallet(wallet),use };
    });
  }
  async reconcileToken(token) {
    validateToken(token);
    const pk=purchaseKey(token),purchase=await this.store.read(pk);
    if (!purchase || purchase.deletedAtMs || purchase.terminal) return { ignored:true };
    const raw=await this.play.getPurchase(this.packageName,token);
    const snapshot=snapshotPurchase(raw,purchase.productId,purchase.owner);
    const wk=`wallets/${purchase.owner}`;
    const result=await this.store.transact([wk,pk],tx=>{
      const current=tx.get(pk),wallet=tx.get(wk);
      if (!current || current.terminal || current.deletedAtMs || !wallet || wallet.state!=='ACTIVE') return { ignored:true };
      const revoked=snapshot.state==='CANCELLED'||(snapshot.state==='PURCHASED'&&snapshot.refundableQuantity===0);
      if (revoked) {
        const refund=current.credited-current.refunded;
        if (refund>0) { wallet.balance-=refund;wallet.version++;current.refunded+=refund; }
        current.terminal=true; current.needsConsume=false;
      } else if (snapshot.state==='PURCHASED' && current.credited===0) {
        if (snapshot.consumed) throw new AppError(409,'CONSUMED_PURCHASE_NOT_RECORDED');
        const gold=PRODUCTS[current.productId];
        if(wallet.balance+gold>MAX_BALANCE || (wallet.lifetimeCredits||0)+gold>MAX_BALANCE) throw new AppError(409,'WALLET_LIMIT');
        wallet.balance+=gold;wallet.lifetimeCredits=(wallet.lifetimeCredits||0)+gold;wallet.version++;current.credited=gold;
      }
      current.state=snapshot.state;current.checkedAtMs=this.now();if(snapshot.state==='PENDING')current.nextCheckAtMs=this.now()+5*60000;else delete current.nextCheckAtMs;current.consumed ||= snapshot.consumed;
      current.needsConsume=current.credited>0&&!current.terminal&&!current.consumed;
      if(current.needsConsume)current.nextConsumeAtMs ??= this.now();else delete current.nextConsumeAtMs;
      tx.set(wk,wallet);tx.set(pk,current);
      return { ignored:false,consume:current.needsConsume };
    });
    if(result.consume) await this.consume(token);
    return result;
  }
  async applyVoided(item) {
    // This method accepts only rows returned directly by authenticated Google
    // Voided Purchases API calls, never notification/client-supplied row data.
    const token=validateToken(item.purchaseToken),pk=purchaseKey(token),purchase=await this.store.read(pk);
    if(!purchase||purchase.deletedAtMs||purchase.terminal)return {ignored:true};
    if(typeof item.orderId!=='string'||!item.orderId||item.orderId.length>256||(purchase.orderId&&item.orderId!==purchase.orderId))throw new AppError(422,'VOIDED_ORDER_MISMATCH');
    if(item.voidedQuantity!==undefined&&item.voidedQuantity!==1)throw new AppError(422,'UNSUPPORTED_VOIDED_QUANTITY');
    const wk=`wallets/${purchase.owner}`;
    return this.store.transact([wk,pk],tx=>{
      const current=tx.get(pk),wallet=tx.get(wk);
      if(!current||current.terminal||current.deletedAtMs||!wallet||wallet.state!=='ACTIVE')return {ignored:true};
      const refund=current.credited-current.refunded;
      if(refund>0){wallet.balance-=refund;wallet.version++;current.refunded+=refund;}
      current.terminal=true;current.needsConsume=false;current.state='CANCELLED';current.checkedAtMs=this.now();current.revocationSource='GOOGLE_VOIDED_PURCHASES';delete current.nextCheckAtMs;delete current.nextConsumeAtMs;
      tx.set(wk,wallet);tx.set(pk,current);return {ignored:false};
    });
  }
  async reconcileVoided(targetToken) {
    let pageToken,pages=0,reconciled=0;
    do{
      const page=await this.play.listVoided(this.packageName,{startTime:this.now()-29*86400000,pageToken});
      for(const item of page.voidedPurchases||[]){
        if(targetToken&&item.purchaseToken!==targetToken)continue;
        await this.applyVoided(item);reconciled++;
      }
      pageToken=page.tokenPagination?.nextPageToken;
      if(++pages>=100&&pageToken)throw new AppError(503,'RECONCILIATION_BACKLOG');
    }while(pageToken);
    return reconciled;
  }
  async handleNotification(envelope) {
    if (!envelope || typeof envelope.message?.messageId!=='string' || !/^[A-Za-z0-9_-]{1,128}$/u.test(envelope.message.messageId) || typeof envelope.message.data!=='string' || envelope.message.data.length>32768) throw new AppError(400,'INVALID_NOTIFICATION');
    let notification;
    try {
      if (!/^[A-Za-z0-9+/]*={0,2}$/u.test(envelope.message.data)) throw new Error('base64');
      notification=JSON.parse(Buffer.from(envelope.message.data,'base64').toString('utf8'));
    } catch { throw new AppError(400,'INVALID_NOTIFICATION'); }
    if(notification.packageName!==this.packageName) throw new AppError(400,'PACKAGE_MISMATCH');
    const key=`notifications/${hash(envelope.message.messageId)}`;
    if(await this.store.read(key)) return { duplicate:true };
    const types=['oneTimeProductNotification','voidedPurchaseNotification','testNotification','subscriptionNotification','pendingRefundReviewNotification'].filter(type=>notification[type]);
    if(types.length!==1) throw new AppError(400,'INVALID_NOTIFICATION');
    const event=notification.oneTimeProductNotification||notification.voidedPurchaseNotification;
    // RTDN is a hint only: never trust a notification's amount/status to grant.
    if(event){
      const token=validateToken(event.purchaseToken);
      if(notification.voidedPurchaseNotification)await this.reconcileVoided(token);
      await this.reconcileToken(token);
    }
    await this.store.transact([key],tx=>tx.set(key,{ createdAtMs:this.now(),expiresAtMs:this.now()+30*86400000 }));
    return { received:true };
  }
  async deleteAccount(uid, authTimeSeconds) {
    validateUid(uid);
    const age=this.now()/1000-authTimeSeconds;
    if(!Number.isFinite(age)||age< -60||age>300) throw new AppError(401,'RECENT_LOGIN_REQUIRED');
    const wk=walletKey(uid),dk=deletionKey(uid);
    await this.store.transact([wk,dk],tx=>{
      const wallet=tx.get(wk)||blankWallet(),job=tx.get(dk);
      if(wallet.state==='DELETED') return;
      tx.set(wk,{...wallet,state:'DELETING'});
      tx.set(dk,job||{ owner:ownerOf(uid),uid,createdAtMs:this.now(),nextAttemptAtMs:this.now(),state:'PENDING' });
    });
    await this.finishDeletion(uid);
    return { deleted:true };
  }
  async finishDeletion(uid) {
    const owner=ownerOf(uid),wk=walletKey(uid),dk=deletionKey(uid),now=this.now();
    try { await this.identity.deleteUser(uid); } catch(error) { if(error.code!=='auth/user-not-found') {await this.retryLater(dk,'nextAttemptAtMs');throw new AppError(503,'DELETION_RETRY_PENDING');} }
    // Redact each purchase and erase each request before marking complete. A
    // crash leaves the durable job in PENDING for the maintenance worker.
    for (const collection of ['purchases','requests']) {
      while(true) {
        const docs=await this.store.find(collection,{field:'owner',value:owner,limit:this.batchSize,field2:collection==='purchases'?'personalDataPresent':undefined,value2:true});
        if(!docs.length) break;
        for(const {key,value} of docs) await this.store.transact([key],tx=>{
          const current=tx.get(key); if(!current) return;
          if(collection==='requests') tx.delete(key);
          else {
            const audit={ owner,productId:current.productId,quantity:current.quantity,credited:current.credited,refunded:current.refunded,createdAtMs:current.createdAtMs,deletedAtMs:now,expiresAtMs:now+this.retentionMs,terminal:true,needsConsume:false,personalDataPresent:false };
            tx.set(key,audit);
          }
        });
        if(docs.length<this.batchSize) break;
      }
    }
    await this.store.transact([wk,dk],tx=>{
      tx.set(wk,{state:'DELETED',deletedAtMs:now,expiresAtMs:now+this.retentionMs});
      tx.set(dk,{owner,state:'COMPLETE',deletedAtMs:now,expiresAtMs:now+this.retentionMs});
    });
  }
  async maintenance() {
    const key='system/maintenance',token=randomUUID();
    const acquired=await this.store.transact([key],tx=>{const lock=tx.get(key);if(lock?.untilMs>this.now())return false;tx.set(key,{token,untilMs:this.now()+4*60000});return true;});
    if(!acquired)return {busy:true};
    try{return await this.maintenanceBatch();}
    finally{await this.store.transact([key],tx=>{if(tx.get(key)?.token===token)tx.delete(key);});}
  }
  async maintenanceBatch() {
    const results={ consumed:0,reconciled:0,deleted:0,purged:0,failures:0 };
    for (const {key,value} of await this.store.find('deletions',{field:'nextAttemptAtMs',op:'<=',value:this.now(),limit:this.batchSize})) {
      try { await this.finishDeletion(value.uid);results.deleted++; } catch {await this.retryLater(key,'nextAttemptAtMs');results.failures++;}
    }
    for(const {key,value} of await this.store.find('purchases',{field:'nextConsumeAtMs',op:'<=',value:this.now(),limit:this.batchSize})) {
      try { if(await this.consume(value.purchaseToken)) results.consumed++; else results.failures++; } catch {await this.retryLater(key,'nextConsumeAtMs');results.failures++;}
    }
    for(const {key,value} of await this.store.find('purchases',{field:'nextCheckAtMs',op:'<=',value:this.now(),limit:this.batchSize})) {
      if(!value.purchaseToken||value.terminal)continue;
      try{await this.reconcileToken(value.purchaseToken);results.reconciled++;}catch{await this.retryLater(key,'nextCheckAtMs');results.failures++;}
    }
    // Overlap a full 30-day window, including quantity-based refunds. The
    // refund ledger makes replay and overlapping schedules harmless.
    results.reconciled+=await this.reconcileVoided();
    for(const collection of ['purchases','wallets','deletions','notifications','rateLimits']) {
      const docs=await this.store.find(collection,{field:'expiresAtMs',op:'<=',value:this.now(),limit:this.batchSize});
      for(const {key} of docs) await this.store.transact([key],tx=>{const value=tx.get(key);if(value?.expiresAtMs<=this.now())tx.delete(key);});
      results.purged+=docs.length;
    }
    if(results.failures) throw new AppError(503,'MAINTENANCE_RETRY_REQUIRED');
    return results;
  }
}
