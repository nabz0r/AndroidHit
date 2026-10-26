import { readConfig } from './config.js';
import { productionAdapters } from './production.js';
import { WalletService } from './core.js';
import { createHttpServer } from './http.js';

const config=readConfig();
const adapters=await productionAdapters(config);
const service=new WalletService({...adapters,...config});
const server=createHttpServer({...adapters,service,allowedOrigins:config.allowedOrigins,subscription:config.subscription,onError:event=>process.stderr.write(`${JSON.stringify(event)}\n`)});
server.keepAliveTimeout=5000;
server.listen(config.port,'0.0.0.0',()=>process.stdout.write('Luma wallet listening\n'));
for(const signal of ['SIGTERM','SIGINT'])process.on(signal,()=>server.close(()=>process.exit(0)));
