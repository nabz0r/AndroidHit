"""Bounded archive of this task's final Hosting receipts; no credential files."""
from pathlib import Path
import base64, datetime, gzip, hashlib, io, json, os, re, tarfile

WORK=Path('/tmp/luma124-cloud-66119799d6fb-5mb_1ou2')
TARGET='66119799d6fbe5254a8d37d50db76afe8daffbdc'
OUT=Path('/tmp/reprise-20261031-final2-originals.tgz')
RECEIPTS=WORK/'receipts'
OP_SHA='237ad3d890eab5a212e699dbc82408f77792dfa2a3aa7f2bf438c8198ba3e967'
STAGE_SHA='4b9b5b69fa89d29824ae54baf68b005a50a8a8671f26c5c7ffad9acbbea4a9c5'
sha=lambda b: hashlib.sha256(b).hexdigest()

def regular(p):
    assert p.is_file() and not p.is_symlink(), str(p)
    assert p.stat().st_size <= 8*1024*1024, str(p)
    return p.read_bytes()

def main():
    assert not OUT.exists(), 'Archive already exists; preserve original'
    operator=json.loads(regular(RECEIPTS/'reprise-20261031-final2-operator.json'))
    deployment=json.loads(regular(RECEIPTS/'reprise-20261031-final2-deployment/deployment.json'))
    assert operator['status']==deployment['status']=='DEPLOY/FINALIZED'
    assert operator['source_commit']==deployment['sourceCommit']==TARGET
    assert deployment['finalExpectedProviderPaths']==631
    assert deployment['providerServingConfigUnchanged'] is True
    assert sha(regular(RECEIPTS/'operator.json'))==OP_SHA
    assert sha(regular(RECEIPTS/'staging.json'))==STAGE_SHA
    files={}
    for p in sorted(RECEIPTS.rglob('*')):
        if p.is_dir():
            assert not p.is_symlink()
            continue
        assert p.suffix in ('.json','.log'), 'Unapproved receipt type: '+str(p)
        files['receipts/'+p.relative_to(RECEIPTS).as_posix()]=regular(p)
    outer=[
        'reprise-20261031-cloud-run.py',
        'reprise-20261031-cloud-finalize.py',
        'reprise-20261031-collect.py',
        'reprise-20261031-collect-v2.py',
        'reprise-20261031-stage-66119799d6fb.log',
        'reprise-20261031-stage-launch.json',
        'reprise-20261031-credential-ready.json',
        'reprise-20261031-finalizer-check.log',
        'reprise-20261031-final2-launch.log',
        'reprise-20261031-final2-launch.json',
    ]
    for name in outer:
        files['outer/'+name]=regular(Path('/tmp')/name)
    assert 1 <= len(files) < 100
    assert sum(map(len,files.values())) < 16*1024*1024
    for name,data in files.items():
        assert not re.search(rb'ya29\.[A-Za-z0-9_-]{20,}',data) and not re.search(rb'Authorization: Bearer [A-Za-z0-9._~-]{20,}',data), 'Credential-shaped value in '+name
    index={
        'status':'PASS','createdUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),
        'sourceCommit':TARGET,'version':deployment['version'],
        'scope':'Original final management/stage/operator receipts only; public-origin bytes and rendering separate.',
        'originalStagePreserved':True,
        'files':{k:{'bytes':len(v),'sha256':sha(v)} for k,v in sorted(files.items())},
        'privacy':'Explicit receipt/log/script whitelist. No account, token, gcloud credential/config, browser profile or screenshot files collected.'}
    files['archive-index.json']=(json.dumps(index,indent=2)+'\n').encode()
    tarbuf=io.BytesIO()
    with tarfile.open(fileobj=tarbuf,mode='w') as tar:
        for name,data in sorted(files.items()):
            info=tarfile.TarInfo(name);info.size=len(data);info.mode=0o600;info.mtime=0
            tar.addfile(info,io.BytesIO(data))
    compressed=gzip.compress(tarbuf.getvalue(),mtime=0)
    assert len(compressed)<1024*1024
    os.umask(0o077)
    with OUT.open('xb') as stream:stream.write(compressed)
    metadata={'status':'ARCHIVED','path':str(OUT),'bytes':len(compressed),'sha256':sha(compressed),
        'files':len(index['files']),'members':len(files),'base64Characters':len(base64.b64encode(compressed)),
        'chunks1536':(len(base64.b64encode(compressed))+1535)//1536,'sourceCommit':TARGET,'version':deployment['version']}
    with OUT.with_suffix('.json').open('x') as stream:stream.write(json.dumps(metadata,indent=2)+'\n')
    print('LUMA124_REPRISE_ARCHIVE '+json.dumps(metadata))

if __name__=='__main__':main()
