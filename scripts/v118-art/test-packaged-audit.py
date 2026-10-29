#!/usr/bin/env python3
"""Exercise packaged-only validation without original PNGs, APKs or native captures."""
from pathlib import Path
import hashlib
import json
import runpy
import shutil
import subprocess
import sys
import tempfile
import unittest

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
AUDIT = runpy.run_path(str(HERE / "audit-assets.py"))
NAME = "nova-evo01-poses.json"


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


class PackagedAuditTests(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory(prefix="luma-v118-packaged-audit-")
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.assets = self.root / "app/src/main/assets/art/v118"
        self.assets.mkdir(parents=True)
        self.metadata = self.assets / NAME
        for name in (NAME, NAME.replace(".json", ".webp")):
            shutil.copyfile(ROOT / "app/src/main/assets/art/v118" / name, self.assets / name)
        revision = ROOT / "docs/validation/v1.18/rig-g-all-proposals"
        self.receipt_path = self.root / "revision/applied.json"
        self.before = self.receipt_path.parent / "before" / NAME
        self.before.parent.mkdir(parents=True)
        shutil.copyfile(revision / "before" / NAME, self.before)
        record = next(item for item in json.loads((revision / "applied.json").read_text())["sheets"]
                      if item["file"] == NAME)
        self.record = dict(record)

    def audit(self, packaged=True):
        self.receipt_path.write_text(json.dumps({"sheets": [self.record]}))
        revision = {"path": self.receipt_path, "sha256": digest(self.receipt_path),
                    "records": {NAME: self.record}}
        return AUDIT["audit_sheet"](self.root, "nova", 1, revision, packaged_only=packaged)

    def change_metadata(self, mutate, update_receipt=False):
        data = json.loads(self.metadata.read_text())
        mutate(data)
        self.metadata.write_text(json.dumps(data))
        if update_receipt:
            self.record["after"] = digest(self.metadata)

    def test_packaged_pixels_validate_without_originals_and_make_no_equivalence_claim(self):
        self.assertFalse((self.root / "docs/art/v118/originals").exists())
        result = self.audit()
        self.assertTrue(result["passed"], result["errors"])
        self.assertIsNone(result["actualPixelEquivalent"])
        self.assertFalse(result["originalPngVerified"])
        self.assertEqual(result["frameCount"], 20)
        self.assertTrue(result["rigRevisionProtectedFieldsPreserved"])

    def test_default_original_audit_still_refuses_a_missing_png(self):
        result = self.audit(packaged=False)
        self.assertFalse(result["passed"])
        self.assertTrue(any("Missing original:" in error for error in result["errors"]))

    def test_packaged_cli_requires_an_explicit_revision_receipt(self):
        result = subprocess.run([sys.executable, str(HERE / "audit-assets.py"),
                                 "--project", str(self.root), "--packaged-only"],
                                capture_output=True, text=True)
        self.assertEqual(result.returncode, 2)
        self.assertIn("requires an explicit --rig-revision-receipt", result.stderr)

    def test_stale_after_hash_is_rejected(self):
        self.change_metadata(lambda data: data.update(rigProposal="unrecorded revision"))
        result = self.audit()
        self.assertFalse(result["passed"])
        self.assertIn("Metadata differs from frozen rig revision receipt", result["errors"])

    def test_archived_baseline_hash_is_required(self):
        self.before.write_bytes(self.before.read_bytes() + b" ")
        result = self.audit()
        self.assertFalse(result["passed"])
        self.assertIn("Rig revision before hash differs", result["errors"])

    def test_protected_geometry_cannot_be_changed_by_a_new_after_hash(self):
        self.change_metadata(lambda data: data["frames"][0].update(
            originX=data["frames"][0]["originX"] + .25), update_receipt=True)
        result = self.audit()
        self.assertFalse(result["passed"])
        self.assertTrue(any("changed geometry" in error for error in result["errors"]))

    def test_allowed_attachment_revision_must_still_fit_runtime_bounds(self):
        self.change_metadata(lambda data: data["frames"][0]["attachments"][0].update(
            width=data["pixelsPerUnit"] * 7), update_receipt=True)
        result = self.audit()
        self.assertFalse(result["passed"])
        self.assertTrue(any("converted geometry exceeds runtime limits" in error for error in result["errors"]))

    def test_recorded_webp_hash_must_match_the_actual_packaged_bytes(self):
        self.change_metadata(lambda data: data.update(webpSha256="0" * 64), update_receipt=True)
        result = self.audit()
        self.assertFalse(result["passed"])
        self.assertIn("Metadata mismatch: webpSha256", result["errors"])

    def test_read_limit_matches_runtime_512_kib(self):
        self.change_metadata(lambda data: data.update(padding="x" * (512 * 1024)), update_receipt=True)
        result = self.audit()
        self.assertFalse(result["passed"])
        self.assertIn("Metadata exceeds runtime 512 KiB read bound", result["errors"])


if __name__ == "__main__":
    unittest.main(verbosity=2)
