#!/usr/bin/env python3
"""Serve the verified1.21 staging URL map on loopback; never modify public source or deploy."""
import argparse
from http.server import SimpleHTTPRequestHandler,ThreadingHTTPServer
import json
from pathlib import Path
from urllib.parse import unquote,urlsplit

ROOT=Path(__file__).resolve().parents[1]
PUBLIC=ROOT/'public'
PREVIEW=ROOT/'docs/validation/site-v1.21/staging/preview-files.json'

class Preview(SimpleHTTPRequestHandler):
    def translate_path(self,value):
        path=unquote(urlsplit(value).path)
        mapping=json.loads(PREVIEW.read_text())['files']
        if path in mapping:
            target=(ROOT/mapping[path]).resolve()
            return str(target) if ROOT in target.parents and target.is_file() else str(PUBLIC/'missing-preview-file')
        target=(PUBLIC/path.lstrip('/')).resolve()
        return str(target) if target==PUBLIC or PUBLIC in target.parents else str(PUBLIC/'missing-preview-file')
    def log_message(self,*values):pass

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--port',type=int,default=8832);args=parser.parse_args()
    assert PREVIEW.is_file(),'Run prepare-site-v121.py --preview first'
    assert 1024<=args.port<=65535
    print('Exact local1.21 preview: http://127.0.0.1:'+str(args.port)+'/index.html',flush=True)
    ThreadingHTTPServer(('127.0.0.1',args.port),Preview).serve_forever()

if __name__=='__main__':main()
