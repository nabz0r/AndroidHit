import { mkdir, writeFile } from 'node:fs/promises';
import { resolve } from 'node:path';
import { CosmeticService, COSMETIC_CATALOG } from '../server/src/cosmetic.js';
import { MemoryStore } from '../server/src/store.js';
import { hash } from '../server/src/core.js';

// Entirely offline MemoryStore fixtures. No server deployment, identity, Play or money API.
const uid = 'vault-test-user-A';
const key = `wallets/${hash(uid)}`;
const store = new MemoryStore();
const service = new CosmeticService({ store, enabled: false });
const fresh = await service.catalog(uid);
await store.transact([key], tx => tx.set(key, {
  state: 'ACTIVE', balance: 500, version: 3, cosmeticPity: 19,
  cosmetics: COSMETIC_CATALOG.filter(item => item.id !== 'gloves_aurora').map(item => item.id),
}));
const guaranteed = await service.catalog(uid);
await store.transact([key], tx => tx.set(key, {
  state: 'ACTIVE', balance: 500, version: 4, cosmeticPity: 19,
  cosmetics: COSMETIC_CATALOG.map(item => item.id),
}));
const complete = await service.catalog(uid);
const drawStore = new MemoryStore();
await drawStore.transact([key], tx => tx.set(key, { state: 'ACTIVE', balance: 500, version: 0, cosmetics: [] }));
const modelDraw = new CosmeticService({ store: drawStore, enabled: true, seed: () => Buffer.alloc(32, 7) });
const receipt = await modelDraw.draw(uid, 'fixture-draw-01');
const directory = resolve(process.argv[2] || 'docs/validation/v1.7/vault');
await mkdir(directory, { recursive: true });
await writeFile(resolve(directory, 'server-fixtures.json'), JSON.stringify({ fresh, guaranteed, complete, receipt }, null, 2) + '\n');
console.log('Offline real-server fixtures written: fresh, guarantee-conditioned, complete and deterministic receipt.');
