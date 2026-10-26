import { AppError } from './core.js';

/** Deterministic test adapter, serializes atomic commits across concurrent calls. */
export class MemoryStore {
  constructor() { this.docs=new Map();this.queue=Promise.resolve(); }
  async transact(keys,fn) {
    let release;
    const previous=this.queue;this.queue=new Promise(resolve=>{release=resolve;});
    await previous;
    try {
      const values=new Map(keys.map(key=>[key,structuredClone(this.docs.get(key))]));
      const changes=new Map();
      const check=key=>{if(!values.has(key))throw new Error('Undeclared transaction key');};
      const tx={ get:key=>{check(key);return structuredClone(changes.has(key)?changes.get(key):values.get(key));},set:(key,value)=>{check(key);changes.set(key,structuredClone(value));},delete:key=>{check(key);changes.set(key,undefined);} };
      const result=fn(tx);
      if(result instanceof Promise) throw new Error('Transaction callbacks must be synchronous and side effect free');
      for(const [key,value]of changes) {if(value===undefined)this.docs.delete(key);else this.docs.set(key,value);}
      return structuredClone(result);
    }finally{release();}
  }
  async read(key) { await this.queue;return structuredClone(this.docs.get(key)); }
  async find(collection,{field,op='==',value,field2,value2,limit=100,cursor}={}) {
    await this.queue;
    return [...this.docs].filter(([key,doc])=>key.startsWith(`${collection}/`)&&(!cursor||key>cursor)&&(!field||(op==='=='?doc[field]===value:(typeof doc[field]===typeof value&&doc[field]<=value)))&&(!field2||doc[field2]===value2)).sort(([a,ad],[b,bd])=>op==='=='?a.localeCompare(b):(ad[field]-bd[field]||a.localeCompare(b))).slice(0,limit).map(([key,value])=>({key,value:structuredClone(value)}));
  }
}

/** Fixed-window limits backed by transactions, shared by all Cloud Run instances. */
export async function enforceUserRate(store,uidHash,now,limit=60) {
  const minute=Math.floor(now/60000),key=`rateLimits/${uidHash}_${minute}`;
  await store.transact([key],tx=>{
    const value=tx.get(key)||{count:0,expiresAtMs:(minute+2)*60000};
    if(value.count>=limit)throw new AppError(429,'RATE_LIMITED');
    tx.set(key,{...value,count:value.count+1});
  });
}
