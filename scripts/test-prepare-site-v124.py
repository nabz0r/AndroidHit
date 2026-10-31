#!/usr/bin/env python3
"""Bounded offline preparation gates; fake PNG headers/receipts are not native QA."""
import copy
import hashlib
import json
from pathlib import Path
import struct
import tempfile
import types
import unittest
from unittest.mock import patch
import urllib.request

SCRIPTS = Path(__file__).resolve().parent
PREP = types.ModuleType('prepare124')
PREP.__file__ = str(SCRIPTS / 'prepare-site-v124.py')
exec(compile(Path(PREP.__file__).read_bytes(), PREP.__file__, 'exec'), PREP.__dict__)


class PreparationTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name).resolve()
        self.previous_root, self.previous_public = PREP.ROOT, PREP.PUBLIC
        PREP.ROOT, PREP.PUBLIC = self.root, self.root / 'public'
        self.report = self.root / 'native/report.json'
        self.report.parent.mkdir()
        self.release = {'apks': {'production': {'sha256': 'a' * 64}}}
        self.rows = []
        data = b'\x89PNG\r\n\x1a\n' + b'\x00\x00\x00\rIHDR' + struct.pack('>II', 1080, 2400)
        report = {'passed': True, 'native': {'passed': True}, 'apk_sha256': 'a' * 64, 'installed_sha256': 'a' * 64, 'expected_version': '1.24.0-rc2', 'expected_code': 33, 'package': 'com.androidhit.lumadrift', 'evidence_sha256': {}}
        for key in PREP.SCREEN_KEYS:
            for locale in ('fr', 'en'):
                path = self.report.parent / (key + '-' + locale + '.png')
                path.write_bytes(data + key.encode() + locale.encode())
                report['evidence_sha256'][path.name] = PREP.sha(path)
                self.rows.append({'key': key, 'locale': locale, 'source': str(path.relative_to(self.root)), 'report': str(self.report.relative_to(self.root))})
        self.report.write_text(json.dumps(report))
        self.network = patch.object(urllib.request.OpenerDirector, 'open', side_effect=AssertionError('Network prohibited'))
        self.network.start()

    def tearDown(self):
        self.network.stop()
        PREP.ROOT, PREP.PUBLIC = self.previous_root, self.previous_public
        self.temp.cleanup()

    def test_eight_unique_d_associations_and_original_hashes_are_required(self):
        proof, pending, frozen = PREP.validate_captures(self.rows, self.release)
        self.assertEqual(len(proof), 8)
        self.assertEqual(len(pending), 8)
        self.assertEqual(len(frozen), 9)
        self.assertTrue(all(row['screenshotsEdited'] is False and row['apkSha256'] == 'a' * 64 for row in proof))
        for rows in (self.rows[:-1], self.rows[:-1] + [self.rows[0]]):
            with self.assertRaises(AssertionError):
                PREP.validate_captures(rows, self.release)

    def test_wrong_apk_version_or_changed_capture_has_no_qualification(self):
        baseline = json.loads(self.report.read_text())
        for values in ({'apk_sha256': 'b' * 64}, {'expected_code': 32}, {'expected_version': '1.24.0-rc1'}, {'native': {'passed': False}}):
            self.report.write_text(json.dumps(dict(baseline, **values)))
            with self.subTest(values=values), self.assertRaises(AssertionError):
                PREP.validate_captures(self.rows, self.release)
        self.report.write_text(json.dumps(baseline))
        (self.root / self.rows[0]['source']).write_bytes(b'edited')
        with self.assertRaisesRegex(AssertionError, 'original PNG'):
            PREP.validate_captures(self.rows, self.release)

    def test_existing_capture_is_never_replaced_and_paths_are_bounded(self):
        target = PREP.PUBLIC / 'assets/screenshots/v1.24/puzzle-fr.png'
        target.parent.mkdir(parents=True)
        target.write_bytes(b'existing different capture')
        with self.assertRaisesRegex(AssertionError, 'Never overwrite'):
            PREP.validate_captures(self.rows, self.release)
        for name in ('../escape', 'native/../report.json'):
            with self.assertRaises((AssertionError, ValueError)):
                PREP.bounded(name)

    def test_final_markup_replaces_all_slots_and_preserves_gallery_audio(self):
        template = (SCRIPTS.parent / 'docs/validation/site-v1.24/staging/index.preview.html').read_text()
        PREP.PUBLIC = SCRIPTS.parent / 'public'
        release = {'apks': {'production': {'path': 'releases/luma-drift-1.24.0-rc2.apk', 'bytes': 340290322, 'sha256': 'a' * 64}, 'lab': {'path': 'releases/luma-drift-1.24.0-sandbox-rc2.apk', 'bytes': 340290009, 'sha256': 'b' * 64}}}
        final = PREP.page(template, release)
        self.assertIn('data-edition="1.24"', final)
        self.assertNotIn('native-pending', final)
        self.assertEqual(final.count('data-screenshot-fr='), 4)
        for key in ('companions', 'sound'):
            self.assertEqual(PREP.section(template, key), PREP.section(final, key))
        self.assertIn('/releases/luma-drift-1.24.0-rc2.apk', final)
        self.assertIn('/releases/luma-drift-1.24.0-sandbox-rc2.apk', final)
        self.assertIn('a' * 64, final)


if __name__ == '__main__':
    unittest.main()
