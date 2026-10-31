#!/usr/bin/env python3
"""Advance one exact staged C2 checkout to reviewed C2b; no auth or deploy."""
from pathlib import Path
import datetime,hashlib,json,os,subprocess,sys
WORK=Path('/tmp/luma124-cloud-39c0985b24ec-g5pa_wji')
OLD='39c0985b24ecc4cf6ef00b6eb1c1392ebe3758da'
TARGET='66119799d6fbe5254a8d37d50db76afe8daffbdc'
TREE='ee94f27e3c9dbbbc49c0d95568424c9d4af2118a'
PUBLIC='d003cdc3ca4a4aa87ea19a5ece1222fa49c9975b'
APP='81eba0e25202124ebebb5f4468d5dd8ba8ac3897'
ALLOWED=['.github/workflows/puzzle-contracts.yml','docs/validation/v1.24/publication/operator-preparation/stage-c2-observation.json','scripts/HomeV115GeometryTest.java','scripts/HomeV115Test.java','scripts/ProfileSummonV116Test.java','scripts/test-journey-world-v122.sh']
SOURCE='docs/validation/site-v1.24/release/source-manifest.json'
SOURCE_SHA='67237fb745f52863e075c476f8d8eca938469bba5288bf4e65edff196a5384bd'
OP_SHA='f547d2ee51fae21cbd31a57d63040f7258126593135de228c672c129f22f15fc'
STAGE_SHA='cd4410973f4734e4373cd6b168696d57e285ae34aa5aefe087ba73fa67c41eba'

def require(ok,message):
 if not ok:raise RuntimeError(message)
def digest(path):
 h=hashlib.sha256()
 with path.open('rb') as f:
  for b in iter(lambda:f.read(1024*1024),b''):h.update(b)
 return h.hexdigest()
def main():
 require(sys.platform.startswith('linux'),'Owned Cloud Shell Linux only')
 require(WORK.is_dir() and not WORK.is_symlink() and WORK==WORK.resolve() and WORK.stat().st_uid==os.getuid() and WORK.stat().st_mode&0o077==0,'Original owner-only workspace required')
 checkout=WORK/'source';original=WORK/'receipts';out=WORK/'receipts-c2b'
 require(not checkout.is_symlink() and not original.is_symlink(),'No symlinks')
 require(digest(original/'operator.json')==OP_SHA and digest(original/'staging.json')==STAGE_SHA,'Original staged evidence differs')
 op=json.loads((original/'operator.json').read_text());stage=json.loads((original/'staging.json').read_text())
 require(op['status']=='STAGED_ONLY' and op['source_commit']==stage['sourceCommit']==OLD and stage['passed'],'Original exact STAGED_ONLY required')
 require(not any(stage[k] for k in ('authenticated','uploaded','deployed')),'Original stage scope differs')
 env=os.environ.copy()
 for k in list(env):
  if k.startswith(('GIT_TRACE','GIT_CONFIG_')) or k in ('GIT_CURL_VERBOSE','GIT_ASKPASS','SSH_ASKPASS'):env.pop(k,None)
 env.update(GIT_TERMINAL_PROMPT='0',GIT_CONFIG_NOSYSTEM='1',GIT_CONFIG_GLOBAL=os.devnull,GIT_CONFIG_SYSTEM=os.devnull,GIT_ASKPASS='/bin/false')
 prefix=['git','-c','credential.helper=','-c','http.extraHeader=']
 def git(*args):return subprocess.check_output(prefix+list(args),cwd=checkout,env=env,stderr=subprocess.DEVNULL,timeout=180)
 require(git('rev-parse','HEAD').decode().strip()==OLD and not git('status','--porcelain','--untracked-files=all').strip(),'Original checkout must remain exact and clean')
 require(git('remote','get-url','origin').decode().strip()=='https://github.com/nabz0r/AndroidHit.git','Pinned origin required')
 require(digest(checkout/SOURCE)==SOURCE_SHA,'Original source manifest differs')
 for x in stage['apks'].values():
  p=checkout/x['path'];require(p.is_file() and not p.is_symlink() and p.stat().st_size==x['bytes'] and digest(p)==x['sha256'],'Original reconstructed APK differs')
 os.umask(0o077);out.mkdir(mode=0o700)
 record={'status':'STARTED','originalStageCommit':OLD,'sourceCommit':TARGET,'targetTree':TREE,'publicTree':PUBLIC,'originalOperatorSha256':OP_SHA,'originalStagingSha256':STAGE_SHA,'sourceManifestSha256':SOURCE_SHA,'workspace':str(WORK),'checkout':str(checkout),'authenticated':False,'uploaded':False,'deployed':False,'scriptSha256':digest(Path(__file__)),'startedUtc':datetime.datetime.now(datetime.timezone.utc).isoformat()}
 def save():(out/'restage-operator.json').write_text(json.dumps(record,indent=2)+'\n')
 save()
 def logged(args,name,timeout=300):
  with (out/name).open('xb') as f:p=subprocess.run(args,cwd=checkout,env=env,stdin=subprocess.DEVNULL,stdout=f,stderr=subprocess.STDOUT,timeout=timeout)
  require(p.returncode==0,'Command failed; inspect '+name)
 try:
  logged(prefix+['fetch','--no-tags','--depth=1','--filter=blob:none','origin',TARGET],'fetch-c2b.log')
  require(git('rev-parse',TARGET+'^{tree}').decode().strip()==TREE and git('rev-parse',TARGET+':public').decode().strip()==PUBLIC,'Reviewed C2b trees differ')
  require(git('rev-parse',OLD+':app').decode().strip()==git('rev-parse',TARGET+':app').decode().strip()==APP,'App tree changed')
  require(git('diff','--name-only',OLD,TARGET).decode().splitlines()==ALLOWED,'Unexpected C2b source delta')
  source=json.loads((checkout/SOURCE).read_text());require(len(source['frozen_inputs'])==69,'Expected69 frozen inputs')
  require(git('show',TARGET+':'+SOURCE)==(checkout/SOURCE).read_bytes(),'C2b source manifest changed')
  for name,h in source['frozen_inputs'].items():require(hashlib.sha256(git('show',TARGET+':'+name)).hexdigest()==h and digest(checkout/name)==h,'Frozen input changed: '+name)
  logged(prefix+['checkout','--detach',TARGET],'checkout-c2b.log')
  require(git('rev-parse','HEAD').decode().strip()==TARGET and not git('status','--porcelain','--untracked-files=all').strip(),'C2b checkout not clean')
  logged([sys.executable,'scripts/stage-site-v124-rest.py','--commit',TARGET,'--report',str(out/'staging.json')],'staging.log',2400)
  new=json.loads((out/'staging.json').read_text())
  require(new['passed'] and new['sourceCommit']==TARGET and new['sourceManifestSha256']==SOURCE_SHA and not new['downloadBasesRequested'] and new['downloadedBaseChannels']==[],'Exact reused stage required')
  require(all(not x['reconstructed'] for x in new['apks'].values()),'Existing APKs must be reused')
  require(not any(new[k] for k in ('authenticated','uploaded','deployed')),'Staging exceeded scope')
  require(not git('status','--porcelain','--untracked-files=all').strip(),'C2b stage checkout changed')
  require(digest(original/'operator.json')==OP_SHA and digest(original/'staging.json')==STAGE_SHA,'Original evidence changed')
  record.update(status='STAGED_ONLY',reusedApks=new['apks'],frozenInputsChecked=69,stagingSha256=digest(out/'staging.json'))
  rc=0
 except Exception as e:record.update(status='FAIL',error=str(e));rc=1
 record['finishedUtc']=datetime.datetime.now(datetime.timezone.utc).isoformat();save();print(json.dumps(record,indent=2));return rc
if __name__=='__main__':sys.exit(main())
