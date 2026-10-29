#!/usr/bin/env python3
"""Verify frozen signatures, source art and strict production/Lab asset policy."""
from pathlib import Path
import argparse, hashlib, json, os, re, subprocess, zipfile
ROOT=Path(__file__).resolve().parents[2]
SIGNER='dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'
IDENTITY_ASSET='assets/identity-config.json'
DEXOPT_ASSETS=frozenset({'assets/dexopt/baseline.prof','assets/dexopt/baseline.profm'})
def digest(data):return hashlib.sha256(data).hexdigest()
def compare_packaged_assets(production,lab):
    """Allow only production identity and compiled baseline-profile variation."""
    production_only=set(production)-set(lab)
    lab_only=set(lab)-set(production)
    common=set(production)&set(lab)
    different={name for name in common if production[name]!=lab[name]}
    assert production_only=={IDENTITY_ASSET},'Unexpected production-only assets: '+str(sorted(production_only))
    assert not lab_only,'Unexpected Lab-only assets: '+str(sorted(lab_only))
    assert DEXOPT_ASSETS<=common,'Both variants must contain both compiled baseline profiles'
    assert different<=DEXOPT_ASSETS,'Unexpected differing packaged assets: '+str(sorted(different-DEXOPT_ASSETS))
    art={name for name in common if name.startswith('assets/art/v118/')}
    assert len(art)==296,'Both variants must share exactly 296 v1.18 art files'
    assert len([name for name in art if name.endswith('-poses.webp')])==148,'Both variants must share 148 authored sheets'
    assert len([name for name in art if name.endswith('-poses.json')])==148,'Both variants must share 148 measured metadata files'
    assert not art&different,'Production and Lab v1.18 art bytes differ'
    return {'productionOnly':sorted(production_only),'labOnly':sorted(lab_only),
            'commonDifferent':sorted(different),'commonEqualCount':len(common-different),
            'v118ArtFiles':len(art),'v118ArtEqual':True,
            'allowedCommonDifferencePaths':sorted(DEXOPT_ASSETS)}
def main():
    os.environ.setdefault('JAVA_HOME','/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home')
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--production',type=Path,required=True);p.add_argument('--production-sha256',required=True)
    p.add_argument('--lab',type=Path,required=True);p.add_argument('--lab-sha256',required=True)
    p.add_argument('--production-version',required=True);p.add_argument('--lab-version',required=True);p.add_argument('--code',type=int,required=True);p.add_argument('--out',type=Path,required=True)
    a=p.parse_args();assert not a.out.exists(),'Preserve previous evidence'
    a.out.mkdir(parents=True)
    sdk=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'));tools=sdk/'build-tools/36.0.0'
    result={'passed':False,'scope':'Frozen APK bytes, signing identity, manifest identity and decoded packaged asset bytes. Native presentation qualification is separate.'}
    try:
        assets={}
        for variant,apk,expected in [('production',a.production,a.production_sha256),('lab',a.lab,a.lab_sha256)]:
            assert re.fullmatch('[a-f0-9]{64}',expected) and digest(apk.read_bytes())==expected,variant+' frozen bytes differ'
            signature=subprocess.check_output([str(tools/'apksigner'),'verify','--verbose','--print-certs',str(apk)],stderr=subprocess.STDOUT).decode();(a.out/(variant+'-signature.txt')).write_text(signature)
            assert re.findall(r'Signer #1 certificate SHA-256 digest: ([a-f0-9]+)',signature)==[SIGNER],variant+' signer differs'
            badging=subprocess.check_output([str(tools/'aapt2'),'dump','badging',str(apk)],stderr=subprocess.STDOUT).decode();(a.out/(variant+'-manifest.txt')).write_text(badging)
            expected_package='com.androidhit.lumadrift'+('.sandbox' if variant=='lab' else '')
            package=re.search(r"package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'",badging)
            expected_version=a.production_version if variant=='production' else a.lab_version
            assert package and package.group(1)==expected_package and int(package.group(2))==a.code and package.group(3)==expected_version,variant+' package/version/code differs'
            with zipfile.ZipFile(apk) as archive:
                names=archive.namelist();assert len(names)==len(set(names)),variant+' duplicate ZIP entry'
                assets[variant]={name:digest(archive.read(name)) for name in names if name.startswith('assets/') and not name.endswith('/')}
                art={name:sha for name,sha in assets[variant].items() if name.startswith('assets/art/v118/')}
                assert len([x for x in art if x.endswith('-poses.webp')])==148,variant+' must contain all 148 authored sheets'
                assert len([x for x in art if x.endswith('-poses.json')])==148,variant+' must contain all 148 measured metadata files'
                assert len(art)==296,variant+' must contain exactly 296 v1.18 art files'
                for name,sha in art.items():assert digest((ROOT/'app/src/main'/name).read_bytes())==sha,variant+' packaged art differs from measured source '+name
            result[variant]={'path':str(apk.resolve()),'sha256':expected,'package':expected_package,'assetCount':len(assets[variant]),'v118ArtFiles':len(art)}
        (a.out/'all-assets-sha256.json').write_text(json.dumps(assets,indent=2,sort_keys=True)+'\n')
        policy=compare_packaged_assets(assets['production'],assets['lab'])
        assert assets['production'][IDENTITY_ASSET]==digest((ROOT/'app/src/production'/IDENTITY_ASSET).read_bytes()),'Production identity asset differs from its variant source'
        (a.out/'asset-difference.json').write_text(json.dumps(policy,indent=2,sort_keys=True)+'\n')
        (a.out/'asset-sha256.json').write_text(json.dumps(assets['production'],indent=2,sort_keys=True)+'\n')
        result['passed']=True;result['all_packaged_assets_equal']=assets['production']==assets['lab']
        result['variant_asset_policy_passed']=True;result['v118_art_assets_equal']=True
        result['variant_asset_differences']=policy;result['signer_sha256']=SIGNER
    except BaseException as failure:result['error']=str(failure);raise
    finally:(a.out/'report.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result))
if __name__=='__main__':main()
