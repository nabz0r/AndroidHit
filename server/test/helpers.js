import { WalletService, AppError, hash } from '../src/core.js';
import { MemoryStore } from '../src/store.js';

export const UID='google-user-one';
export const OTHER='google-user-two';
export function purchase(uid=UID,productId='luma_gold_500',overrides={}) {
  return {obfuscatedExternalAccountId:hash(uid),orderId:'GPA.1234-5678-9012-34567',purchaseStateContext:{purchaseState:'PURCHASED'},productLineItem:[{productId,productOfferDetails:{quantity:1,refundableQuantity:1,consumptionState:'CONSUMPTION_STATE_YET_TO_BE_CONSUMED'}}],...overrides};
}
export class FakePlay {
  constructor(){this.purchases=new Map();this.consumptions=0;this.gets=0;this.unavailable=false;this.consumeUnavailable=false;this.voided=[];this.packages=[];}
  add(token,raw=purchase()){this.purchases.set(token,structuredClone(raw));return this;}
  async getPurchase(packageName,token){this.packages.push(packageName);this.gets++;if(this.unavailable)throw new AppError(503,'PLAY_UNAVAILABLE');const raw=this.purchases.get(token);if(!raw)throw new AppError(422,'INVALID_PURCHASE');return structuredClone(raw);}
  async consume(packageName,productId,token){this.packages.push(packageName);this.consumptions++;if(this.consumeUnavailable)throw new AppError(503,'PLAY_UNAVAILABLE');const raw=this.purchases.get(token);if(raw.productLineItem[0].productId!==productId)throw new Error('Product mismatch');if(raw.productLineItem[0].productOfferDetails.consumptionState==='CONSUMPTION_STATE_CONSUMED')throw new Error('Already consumed');raw.productLineItem[0].productOfferDetails.consumptionState='CONSUMPTION_STATE_CONSUMED';}
  async listVoided(){if(this.unavailable)throw new AppError(503,'PLAY_UNAVAILABLE');return {voidedPurchases:structuredClone(this.voided)};}
}
export function fixture(options={}) {
  const store=options.store||new MemoryStore(),play=options.play||new FakePlay(),deleted=[];
  let time=1780000000000;
  const identity=options.identity||{deleteUser:async uid=>{deleted.push(uid);}};
  const service=new WalletService({store,play,identity,packageName:'com.androidhit.lumadrift',now:()=>time,consumablesEnabled:true,...options});
  return {store,play,service,deleted,advance:ms=>{time+=ms;},now:()=>time};
}
export function notification(token,overrides={}){
  return {subscription:'projects/luma-drift-production/subscriptions/luma-rtdn',message:{messageId:'1234567890',data:Buffer.from(JSON.stringify({version:'1.0',packageName:'com.androidhit.lumadrift',eventTimeMillis:'1780000000000',oneTimeProductNotification:{notificationType:1,purchaseToken:token,sku:'luma_gold_500'},...overrides})).toString('base64')}};
}
