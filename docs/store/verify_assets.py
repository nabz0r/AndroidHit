#!/usr/bin/env python3
"""Verify exported dimensions and regenerate the public integrity manifest."""
import hashlib
import json
import struct
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(__file__).resolve().parent
assets = []
for stem, expected in [('luma-drift-feature', (1024, 500)), ('luma-drift-icon', (512, 512))]:
    for suffix in ['png', 'svg']:
        path = root / f'{stem}.{suffix}'
        data = path.read_bytes()
        entry = {'file': path.name, 'bytes': len(data), 'sha256': hashlib.sha256(data).hexdigest(), 'format': suffix.upper()}
        if suffix == 'png':
            assert data[:8] == b'\x89PNG\r\n\x1a\n' and data[12:16] == b'IHDR', path
            width, height = struct.unpack('>II', data[16:24])
        else:
            document = ET.fromstring(data)
            width, height = int(document.attrib['width']), int(document.attrib['height'])
            assert document.attrib['viewBox'] == f'0 0 {width} {height}', path
        assert (width, height) == expected, (path, width, height)
        entry.update(width=width, height=height)
        assets.append(entry)
manifest = {'schema': 'androidhit-brand-kit-v1', 'created': '2026-10-26', 'provenance': 'Original code-native art derived from Luma Drift companion geometry and palette; no claim of store approval.', 'assets': assets}
(root / 'assets.json').write_text(json.dumps(manifest, indent=2) + '\n', encoding='utf-8')
print('PASS SVG syntax, exact PNG/SVG dimensions and SHA-256 asset manifest')
