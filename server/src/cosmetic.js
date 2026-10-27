import { randomBytes, createHash } from 'node:crypto';
import { AppError, hash, validateUid, validateRequest } from './core.js';
const IDS=['hat_dew','hat_moon','hat_star','hat_pumpkin','hat_cupid','hat_aurora','dress_dew','dress_moon','dress_star','dress_valentine','dress_witch','dress_aurora','shoes_dew','shoes_moon','shoes_star','shoes_pumpkin','shoes_cupid','shoes_aurora','gloves_dew','gloves_moon','gloves_star','gloves_valentine','gloves_witch','gloves_aurora'];
export const COSMETIC_CATALOG=Object.freeze(IDS.map((id,index)=>Object.freeze({id,slot:id.split('_')[0],rarity:index%6<2?'COMMON':index%6<4?'RARE':'EPIC',weight:index%6<2?750:index%6<4?375:125})));
export const DRAW_PRICE=100, DUPLICATE_REFUND=50, NEW_ITEM_GUARANTEE=20;
const wk=uid=>`wallets/${hash(uid)}`;
const active=wallet=>{if(wallet&&wallet.state!=='ACTIVE')throw new AppError(410,'ACCOUNT_DELETED');};
function pool(wallet){const owned=wallet?.cosmetics||[],unowned=COSMETIC_CATALOG.filter(item=>!owned.includes(item.id));return (wallet?.cosmeticPity||0)>=NEW_ITEM_GUARANTEE-1&&unowned.length?unowned:COSMETIC_CATALOG;}
/** Rejection sampling from one private seed is deterministic across Firestore retries and unbiased. */
function choose(seed,total){const span=2**32,limit=span-span%total;for(let i=0;;i++){const value=createHash('sha256').update(seed).update(String(i)).digest().readUInt32BE();if(value<limit)return value%total;}}
export class CosmeticService {
  constructor({store,now=Date.now,enabled=false,seed=()=>randomBytes(32)}){this.store=store;this.now=now;this.enabled=enabled;this.seed=seed;}
  async catalog(uid){validateUid(uid);const wallet=await this.store.read(wk(uid));active(wallet);const eligible=pool(wallet),total=eligible.reduce((sum,item)=>sum+item.weight,0);return {enabled:this.enabled,currency:'LumaGold',balance:wallet?.balance||0,version:wallet?.version||0,price:DRAW_PRICE,duplicateRefund:DUPLICATE_REFUND,guarantee:NEW_ITEM_GUARANTEE,pity:wallet?.cosmeticPity||0,owned:wallet?.cosmetics||[],gameplayBenefit:false,items:COSMETIC_CATALOG.map(item=>({...item,probability:eligible.includes(item)?item.weight/total:0}))};}
  async draw(uid,requestId){
    validateUid(uid);validateRequest(requestId);if(!this.enabled)throw new AppError(403,'PAID_COSMETIC_DRAWS_DISABLED');
    const walletKey=wk(uid),receipt=`requests/${hash(`${uid}\n${requestId}`)}`,seed=this.seed();
    return this.store.transact([walletKey,receipt],tx=>{
      const wallet=tx.get(walletKey);active(wallet);const previous=tx.get(receipt);
      if(previous){if(previous.action!=='cosmetic-draw')throw new AppError(409,'REQUEST_CONFLICT');return previous.result;}
      if(!wallet||wallet.balance<DRAW_PRICE)throw new AppError(409,'INSUFFICIENT_GOLD');
      wallet.cosmetics||=[];const eligible=pool(wallet),total=eligible.reduce((sum,item)=>sum+item.weight,0);let roll=choose(seed,total),item;
      for(const candidate of eligible){roll-=candidate.weight;if(roll<0){item=candidate;break;}}
      const duplicate=wallet.cosmetics.includes(item.id);wallet.balance-=DRAW_PRICE;
      if(duplicate){wallet.balance+=DUPLICATE_REFUND;wallet.cosmeticPity=Math.min(NEW_ITEM_GUARANTEE-1,(wallet.cosmeticPity||0)+1);}else{wallet.cosmetics.push(item.id);wallet.cosmeticPity=0;}
      wallet.version++;
      const result={id:requestId,itemId:item.id,rarity:item.rarity,duplicate,charged:DRAW_PRICE,refund:duplicate?DUPLICATE_REFUND:0,currency:'LumaGold',balance:wallet.balance,version:wallet.version,owned:wallet.cosmetics,pity:wallet.cosmeticPity};
      tx.set(walletKey,wallet);tx.set(receipt,{owner:hash(uid),action:'cosmetic-draw',result,createdAtMs:this.now()});return result;
    });
  }
}
