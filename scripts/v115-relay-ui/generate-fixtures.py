#!/usr/bin/env python3
"""Freeze all U certificates for the separate SDK observer; no device access."""
from pathlib import Path
import hashlib,json,os,subprocess,tempfile

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    output=HERE/'assets/fixtures'
    if output.exists() and any(output.iterdir()):raise SystemExit('Fixtures already frozen; preserve this inventory before an explicit new generation.')
    sources=sorted((ROOT/'app/src/main/java/com/androidhit/lumadrift/engine').glob('*.java'))
    oracle=HERE/'RelayUiFixture.java';hashes={str(p.relative_to(ROOT)):sha(p) for p in [*sources,oracle]}
    jdk=Path(os.environ.get('JAVA_HOME','/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home'))
    with tempfile.TemporaryDirectory(prefix='luma-u-native-fixtures-') as directory:
        classes=Path(directory)/'classes';frozen=Path(directory)/'fixtures';classes.mkdir()
        subprocess.run([str(jdk/'bin/javac'),'-Xlint:all','-d',str(classes),*[str(p) for p in sources],str(oracle)],check=True)
        subprocess.run([str(jdk/'bin/java'),'-cp',str(classes),'RelayUiFixture',str(frozen)],check=True)
        assert hashes=={str(p.relative_to(ROOT)):sha(p) for p in [*sources,oracle]},'Catalogue sources changed during fixture generation'
        output.mkdir(parents=True,exist_ok=True)
        for p in sorted(frozen.glob('u*.json')):(output/p.name).write_bytes(p.read_bytes())
    manifest={'family':'U','count':1000,'scope':'Host mechanically replayed frozen U certificates; Android observer uses fixture rows/columns only to match actual accessible bounds. No target solver, renderer or preference writes.','source_sha256':hashes,'fixture_sha256':{p.name:sha(p) for p in sorted(output.glob('u*.json'))}}
    (output/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
    print(json.dumps({'fixtures':1000,'manifest_sha256':sha(output/'manifest.json')}))
if __name__=='__main__':main()
