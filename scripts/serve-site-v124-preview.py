#!/usr/bin/env python3
"""Serve the staged1.24 homepage locally, without applying or publishing it."""
import argparse
import http.server
import json
from pathlib import Path
import urllib.parse

ROOT = Path(__file__).resolve().parents[1]
STAGING = ROOT / 'docs/validation/site-v1.24/staging'
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--port', type=int, default=38241)
parser.add_argument('--final-preview', action='store_true', help='Use the exact accepted capture/download map produced by prepare-site-v124 --preview')
args = parser.parse_args()
paths = {'/': 'docs/validation/site-v1.24/staging/index.preview.html', '/index.html': 'docs/validation/site-v1.24/staging/index.preview.html'}
if args.final_preview:
    metadata = json.loads((STAGING / 'final-preview-files.json').read_text())
    assert metadata['edition'] == '1.24', 'Accepted final preview map required'
    paths = metadata['files']
for route, name in paths.items():
    target = (ROOT / name).resolve()
    assert route.startswith('/') and ROOT.resolve() in target.parents and target.is_file(), 'Unbounded or missing preview map input'


class Preview(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *values, **options):
        super().__init__(*values, directory=str(ROOT / 'public'), **options)

    def translate_path(self, path):
        route = urllib.parse.urlsplit(path).path
        return str((ROOT / paths[route]).resolve()) if route in paths else super().translate_path(path)


server = http.server.ThreadingHTTPServer(('127.0.0.1', args.port), Preview)
print('Local staging preview: http://127.0.0.1:' + str(args.port), flush=True)
try:
    server.serve_forever()
finally:
    server.server_close()
