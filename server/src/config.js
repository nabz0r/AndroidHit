export function readConfig(env=process.env){
  const required=name=>{const value=env[name];if(!value)throw new Error(`${name} is required`);return value;};
  if(env.FIRESTORE_EMULATOR_HOST||env.FIREBASE_AUTH_EMULATOR_HOST||env.GOOGLE_APPLICATION_CREDENTIALS)throw new Error('Production requires managed credentials and real providers; emulator/key-file environment refused');
  const projectId=required('GOOGLE_CLOUD_PROJECT');
  if(!/^[a-z][a-z0-9-]{4,28}[a-z0-9]$/u.test(projectId))throw new Error('Invalid GOOGLE_CLOUD_PROJECT');
  const serviceUrl=required('SERVICE_URL');
  const url=new URL(serviceUrl);
  if(url.protocol!=='https:'||url.pathname!=='/'||url.search||url.hash||url.username||url.password||serviceUrl.endsWith('/'))throw new Error('SERVICE_URL must be an HTTPS origin without trailing slash');
  const email=name=>{const value=required(name);if(!/^[a-zA-Z0-9._-]+@[a-z0-9.-]+\.iam\.gserviceaccount\.com$/u.test(value))throw new Error(`Invalid ${name}`);return value;};
  const subscription=required('RTDN_SUBSCRIPTION');
  if(!new RegExp(`^projects/${projectId}/subscriptions/[A-Za-z][A-Za-z0-9._~-]{2,254}$`,'u').test(subscription))throw new Error('Invalid RTDN_SUBSCRIPTION');
  const allowedOrigins=(env.ALLOWED_WEB_ORIGINS||'').split(',').filter(Boolean);
  for(const origin of allowedOrigins){const parsed=new URL(origin);if(parsed.protocol!=='https:'||parsed.origin!==origin)throw new Error('Only exact HTTPS origins allowed');}
  const retentionDays=Number(env.AUDIT_RETENTION_DAYS||365);
  if(!Number.isInteger(retentionDays)||retentionDays<30||retentionDays>3650)throw new Error('AUDIT_RETENTION_DAYS must be30..3650');
  if(env.ALLOW_TEST_PURCHASES&&!['true','false'].includes(env.ALLOW_TEST_PURCHASES))throw new Error('ALLOW_TEST_PURCHASES must be true or false');
  if(env.PAID_COSMETIC_DRAWS_ENABLED&&!['true','false'].includes(env.PAID_COSMETIC_DRAWS_ENABLED))throw new Error('PAID_COSMETIC_DRAWS_ENABLED must be true or false');
  const port=Number(env.PORT||8080);if(!Number.isInteger(port)||port<1||port>65535)throw new Error('Invalid PORT');
  return {projectId,serviceUrl,packageName:env.PLAY_PACKAGE_NAME||'com.androidhit.lumadrift',pubsubEmail:email('PUBSUB_PUSH_SERVICE_ACCOUNT'),schedulerEmail:email('SCHEDULER_SERVICE_ACCOUNT'),subscription,allowedOrigins,retentionDays,allowTestPurchases:env.ALLOW_TEST_PURCHASES!=='false',paidCosmeticDrawsEnabled:env.PAID_COSMETIC_DRAWS_ENABLED==='true',port};
}
