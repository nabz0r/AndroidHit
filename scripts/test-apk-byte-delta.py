#!/usr/bin/env python3
"""Transport and refusal tests. These are byte fixtures, not signed Android acceptance."""
import copy
import gzip
import hashlib
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
from unittest import mock
import zipfile

spec = importlib.util.spec_from_file_location("apk_delta", Path(__file__).with_name("apk-byte-delta.py"))
delta = importlib.util.module_from_spec(spec)
spec.loader.exec_module(delta)


class ExactApkDeltaTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory(prefix="luma-delta-tests-")
        self.directory = Path(self.temporary.name)
        self.base, self.target = self.directory / "base.apk", self.directory / "target.apk"
        self.patch, self.output = self.directory / "delta.json.gz", self.directory / "restored.apk"
        for path, changed, extra in [(self.base, b"old executable", b""), (self.target, b"new executable", b"\xfe\xca\x04\x00tail")]:
            with zipfile.ZipFile(path, "w") as archive:
                archive.comment = b"central directory comment " + changed
                for name, data, method in [("art/unchanged.webp", bytes(range(256))*4097, zipfile.ZIP_STORED),
                                           ("text/unchanged.txt", b"same payload "*1000, zipfile.ZIP_DEFLATED),
                                           ("classes.dex", changed, zipfile.ZIP_DEFLATED)]:
                    info = zipfile.ZipInfo(name, (2026, 1, 1, 0, 0, 0))
                    info.compress_type, info.extra = method, extra
                    archive.writestr(info, data)
        self.base_hash, self.target_hash = delta.digest_path(self.base), delta.digest_path(self.target)
        self.built = delta.build(self.base, self.target, self.patch, self.base_hash, self.target_hash)
        self.document = json.loads(gzip.decompress(self.patch.read_bytes()))

    def tearDown(self):
        self.temporary.cleanup()

    def changed_patch(self, document):
        patch = self.directory / "changed.json.gz"
        patch.write_bytes(gzip.compress(json.dumps(document).encode(), mtime=0))
        return patch

    def assert_refused(self, patch=None, **kwargs):
        self.output.write_bytes(b"existing destination stays intact")
        with self.assertRaises((delta.DeltaError, ValueError, OSError)):
            delta.apply(self.base, patch or self.patch, self.output, self.target_hash, replace=True, **kwargs)
        self.assertEqual(self.output.read_bytes(), b"existing destination stays intact")
        self.assertFalse(list(self.directory.glob(".restored.apk.*.tmp")))

    def test_round_trip_preserves_full_bytes_offsets_headers_and_tail(self):
        result = delta.apply(self.base, self.patch, self.output, self.target_hash, self.built["patch"]["sha256"])
        self.assertEqual(self.output.read_bytes(), self.target.read_bytes())
        self.assertEqual(result["verified_output_sha256"], self.target_hash)
        self.assertEqual(self.built["statistics"]["copy_operations"], 2)
        self.assertGreater(self.built["statistics"]["copied_bytes"], 1024*1024)
        self.assertLess(self.patch.stat().st_size, 4096)

    def test_build_is_deterministic(self):
        other = self.directory / "another-filename.json.gz"
        delta.build(self.base, self.target, other, self.base_hash, self.target_hash)
        self.assertEqual(other.read_bytes(), self.patch.read_bytes())

    def test_hash_equality_alone_cannot_authorize_copy(self):
        with mock.patch.object(delta, "ranges_equal", return_value=False):
            with self.assertRaises(delta.DeltaError):
                delta.build(self.base, self.target, self.directory / "no.gz", self.base_hash, self.target_hash)
        self.assertFalse((self.directory / "no.gz").exists())

    def test_wrong_base_refuses_before_publication(self):
        data = bytearray(self.base.read_bytes());data[100] ^= 1;self.base.write_bytes(data)
        self.assert_refused()

    def test_wrong_external_target_hash_refuses(self):
        with self.assertRaises(delta.DeltaError):
            delta.apply(self.base, self.patch, self.output, "0"*64)
        self.assertFalse(self.output.exists())

    def test_wrong_patch_hash_refuses(self):
        self.assert_refused(patch_sha="0"*64)

    def test_bad_copy_hash_keeps_existing_output(self):
        document = copy.deepcopy(self.document)
        next(op["copy"] for op in document["operations"] if "copy" in op)["sha256"] = "0"*64
        self.assert_refused(self.changed_patch(document))

    def test_bad_literal_hash_keeps_existing_output(self):
        document = copy.deepcopy(self.document)
        next(op["literal"] for op in document["operations"] if "literal" in op)["sha256"] = "0"*64
        self.assert_refused(self.changed_patch(document))

    def test_copy_outside_base_refuses(self):
        document = copy.deepcopy(self.document)
        next(op["copy"] for op in document["operations"] if "copy" in op)["offset"] = self.base.stat().st_size
        self.assert_refused(self.changed_patch(document))

    def test_corrupt_base64_refuses(self):
        document = copy.deepcopy(self.document)
        literal = next(op["literal"] for op in document["operations"] if "literal" in op)
        literal["data"] = "!" + literal["data"][1:]
        self.assert_refused(self.changed_patch(document))

    def test_false_statistics_refuse(self):
        document = copy.deepcopy(self.document);document["statistics"]["copied_bytes"] += 1
        self.assert_refused(self.changed_patch(document))

    def test_duplicate_json_keys_refuse(self):
        patch = self.directory / "duplicate.gz"
        patch.write_bytes(gzip.compress(b'{"schema":"x","schema":"y"}', mtime=0))
        self.assert_refused(patch)

    def test_expanded_gzip_bound_is_enforced(self):
        with mock.patch.object(delta, "MAX_JSON", 64):
            self.assert_refused()

    def test_compressed_file_bound_is_enforced(self):
        with mock.patch.object(delta, "MAX_PATCH", 16):
            self.assert_refused()

    def test_existing_output_requires_explicit_replace(self):
        self.output.write_bytes(b"existing")
        with self.assertRaises(delta.DeltaError):
            delta.apply(self.base, self.patch, self.output, self.target_hash)
        self.assertEqual(self.output.read_bytes(), b"existing")

    def test_input_alias_and_symlink_outputs_refuse(self):
        with self.assertRaises(delta.DeltaError):
            delta.apply(self.base, self.patch, self.base, self.target_hash, replace=True)
        self.output.symlink_to(self.base)
        with self.assertRaises(delta.DeltaError):
            delta.apply(self.base, self.patch, self.output, self.target_hash, replace=True)
        self.assertEqual(delta.digest_path(self.base), self.base_hash)

    def test_full_target_hash_checked_after_individually_valid_operations(self):
        document = copy.deepcopy(self.document)
        document["target"]["sha256"] = "1"*64
        self.output.write_bytes(b"existing")
        with self.assertRaises(delta.DeltaError):
            delta.apply(self.base, self.changed_patch(document), self.output, "1"*64, replace=True)
        self.assertEqual(self.output.read_bytes(), b"existing")

    def test_report_cannot_clobber_an_artifact(self):
        with self.assertRaises(delta.DeltaError):
            delta.apply(self.base, self.patch, self.output, self.target_hash, report=self.base)
        with self.assertRaises(delta.DeltaError):
            delta.build(self.base, self.target, self.directory / "other.gz", self.base_hash, self.target_hash, report=self.target)
        self.assertEqual(delta.digest_path(self.base), self.base_hash)
        self.assertEqual(delta.digest_path(self.target), self.target_hash)


if __name__ == "__main__":
    unittest.main(verbosity=2)
