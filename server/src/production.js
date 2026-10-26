import { initializeApp, applicationDefault, getApps } from 'firebase-admin/app';
import { getAuth } from 'firebase-admin/auth';
import { getFirestore, FieldPath } from 'firebase-admin/firestore';
import { GoogleAuth, OAuth2Client } from 'google-auth-library';
import { AppError } from './core.js';

export class FirestoreStore {
  constructor(db){this.db=db;}
  async transact(keys,fn){
    const unique=[...new Set(keys)];
    return this.db.runTransaction(async transaction=>{
      const refs=unique.map(key=>this.db.doc(key));
      const snapshots=await transaction.getAll(...refs);
      const values=new Map(snapshots.map((snapshot,i)=>[unique[i],snapshot.exists?snapshot.data():undefined]));
      const writes=new Map();
      const check=key=>{if(!values.has(key))throw new Error('Undeclared transaction key');};
      const tx={get:key=>{check(key);return structuredClone(writes.has(key)?writes.get(key):values.get(key));},set:(key,value)=>{check(key);writes.set(key,structuredClone(value));},delete:key=>{check(key);writes.set(key,undefined);}};
      const result=fn(tx);
      if(result instanceof Promise)throw new Error('Transaction callbacks must be synchronous');
      for(const [key,value]of writes){if(value===undefined)transaction.delete(this.db.doc(key));else transaction.set(this.db.doc(key),value);}
      return result;
    },{maxAttempts:10});
  }
  async read(key){const snapshot=await this.db.doc(key).get();return snapshot.exists?snapshot.data():undefined;}
  async find(collection,{field,op='==',value,field2,value2,limit=100,cursor}={}){
    let query=this.db.collection(collection);
    if(field)query=query.where(field,op,value);
    if(field2)query=query.where(field2,'==',value2);
    // Inequality query has to order by its range field first. Purge does not
    // paginate: removed rows disappear before the next maintenance run.
    query=query.orderBy(op==='=='?FieldPath.documentId():field);
    if(cursor)query=query.startAfter(cursor);
    const snapshot=await query.limit(limit).get();
    return snapshot.docs.map(doc=>({key:doc.ref.path,value:doc.data()}));
  }
}
function upstream(error){
  const status=Number(error.response?.status||error.code);
  if(status===404||status===400)throw new AppError(422,'INVALID_PURCHASE');
  throw new AppError(503,'PLAY_UNAVAILABLE');
}
export class GooglePlay {
  constructor(client){this.client=client;}
  async request(path,method='GET'){
    try{
      const response=await this.client.request({url:`https://androidpublisher.googleapis.com/androidpublisher/v3/${path}`,method,timeout:10000,retry:true,retryConfig:{retry:2,noResponseRetries:1}});
      return response.data;
    }catch(error){return upstream(error);}
  }
  getPurchase(packageName,token){return this.request(`applications/${encodeURIComponent(packageName)}/purchases/productsv2/tokens/${encodeURIComponent(token)}`);}
  consume(packageName,productId,token){return this.request(`applications/${encodeURIComponent(packageName)}/purchases/products/${encodeURIComponent(productId)}/tokens/${encodeURIComponent(token)}:consume`,'POST');}
  listVoided(packageName,{startTime,pageToken}){
    const params=new URLSearchParams({startTime:String(startTime),type:'0',includeQuantityBasedPartialRefund:'true','pageSelection.maxResults':'1000'});
    if(pageToken)params.set('pageSelection.token',pageToken);
    return this.request(`applications/${encodeURIComponent(packageName)}/purchases/voidedpurchases?${params}`);
  }
}
export class InternalOidcAuth {
  constructor({client=new OAuth2Client(),audience,pubsubEmail,schedulerEmail}){this.client=client;this.audience=audience;this.pubsubEmail=pubsubEmail;this.schedulerEmail=schedulerEmail;}
  async verify(token,purpose){
    try{
      const ticket=await this.client.verifyIdToken({idToken:token,audience:this.audience});
      const claims=ticket.getPayload(),email=purpose==='pubsub'?this.pubsubEmail:this.schedulerEmail;
      if(!email||claims.email!==email||claims.email_verified!==true||!['accounts.google.com','https://accounts.google.com'].includes(claims.iss))throw new Error('Unexpected identity');
      return claims;
    }catch{throw new AppError(403,'INVALID_SERVICE_IDENTITY');}
  }
}
export async function productionAdapters(config){
  const app=getApps()[0]||initializeApp({credential:applicationDefault(),projectId:config.projectId});
  const auth=getAuth(app),store=new FirestoreStore(getFirestore(app));
  const client=await new GoogleAuth({scopes:['https://www.googleapis.com/auth/androidpublisher']}).getClient();
  return {store,auth,identity:{deleteUser:uid=>auth.deleteUser(uid)},play:new GooglePlay(client),internalAuth:new InternalOidcAuth({audience:config.serviceUrl,pubsubEmail:config.pubsubEmail,schedulerEmail:config.schedulerEmail})};
}
