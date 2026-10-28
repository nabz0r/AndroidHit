#!/usr/bin/env python3
"""Bind the local 1.14 page to exact packaged bytes and passed native captures.

No build, image editing, HTTP request, browser, Git mutation or deployment.
The capture map contains nine {public, source, evidence, locale, description} entries.
Every input is validated before any public file is written.
"""
import argparse
import datetime
import hashlib
import json
from pathlib import Path
import re
import struct

ROOT = Path(__file__).resolve().parents[1]
VERSION = "1.14.0-rc2"
LAB_VERSION = "1.14.0-sandbox-rc2"
EXPECTED = {name + "-" + locale + ".png" for name in ("home", "puzzle", "furniture", "house-decor") for locale in ("fr", "en")} | {"house-hug-en.png"}


def digest(raw):
    return hashlib.sha256(raw).hexdigest()


def inside(value, prefix):
    assert value.startswith(prefix), "Wrong edition directory: " + value
    path = (ROOT / value).resolve()
    assert ROOT.resolve() in path.parents, "Path escapes checkout"
    return path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--artifact-commit", help="Immutable artifact commit; omit only for local visual preparation")
    parser.add_argument("--capture-map", required=True, type=Path)
    args = parser.parse_args()
    assert args.artifact_commit is None or re.fullmatch(r"[a-f0-9]{40}", args.artifact_commit), "Exact immutable commit required"
    known = set()
    production = None
    for version in (VERSION, LAB_VERSION):
        manifest = json.loads((ROOT / "releases" / ("manifest-" + version + ".json")).read_text())
        assert manifest["version"] == version and manifest["version_code"] == 16
        # Release manifests also retain the historical repository inventory.
        # Only this edition's two binaries may authorize current screenshots.
        for extension in (".apk", ".aab"):
            name = "luma-drift-" + version + extension
            entry = manifest["artifacts"][name]
            raw = (ROOT / "releases" / name).read_bytes()
            assert len(raw) == entry["bytes"] and digest(raw) == entry["sha256"], "Packaged bytes differ: " + name
            if name.endswith(".apk"):
                known.add(entry["sha256"])
            if name == "luma-drift-" + VERSION + ".apk":
                production = entry
    assert production is not None
    entries = json.loads(args.capture_map.read_text())
    if isinstance(entries, dict):
        entries = entries["images"]
    assert len(entries) == 9
    prepared, seen = [], set()
    for entry in entries:
        name = Path(entry["public"]).name
        assert name in EXPECTED and name not in seen, "Unexpected/duplicate public capture"
        seen.add(name)
        public = inside(entry["public"], "public/assets/screenshots/v1.14/")
        source = inside(entry["source"], "docs/screenshots/v1.14/")
        evidence = inside(entry["evidence"], "docs/validation/v1.14/")
        assert not any(token in str(source).lower() for token in ("analysis-crop", "analysis_crop", "/analysis/")), "Analysis crop is not a product frame"
        raw = source.read_bytes()
        assert raw.startswith(b"\x89PNG\r\n\x1a\n") and len(raw) >= 24
        width, height = struct.unpack(">II", raw[16:24])
        assert (width, height) in ((1080, 2340), (2340, 1080), (1080, 2400), (2400, 1080))
        sha = digest(raw)
        report = json.loads(evidence.read_text())
        assert report.get("passed") is True and report.get("restored") is True, "Native run did not pass/restore"
        assert (report.get("apk_sha256") or report.get("game_apk_sha256")) in known, "Native report does not identify frozen 1.14 bytes"
        assert any(value == sha and (entry["source"] == key or entry["source"].endswith("/" + key)) for key, value in report.get("screenshots", {}).items()), "Passing report does not index this exact original frame"
        assert entry["locale"] in ("fr", "en") and name.endswith("-" + entry["locale"] + ".png") and entry.get("description")
        record = {key: entry[key] for key in ("public", "source", "evidence", "locale", "description")}
        record.update(sha256=sha, width=width, height=height)
        prepared.append((public, raw, record))
    assert seen == EXPECTED
    page = ROOT / "public/index.html"
    html = page.read_text()
    if args.artifact_commit is not None:
        html = re.sub(r"https://raw\.githubusercontent\.com/nabz0r/AndroidHit/(?:__APK_SOURCE_COMMIT__|[a-f0-9]{40})/releases/luma-drift-1\.14\.0-rc2\.apk", "https://raw.githubusercontent.com/nabz0r/AndroidHit/" + args.artifact_commit + "/releases/luma-drift-" + VERSION + ".apk", html)
    html = html.replace("__APK_MB_FR__", format(production["bytes"] / 1e6, ".2f").replace(".", ","))
    html = html.replace("__APK_MB_EN__", format(production["bytes"] / 1e6, ".2f"))
    html = html.replace("__APK_BYTES__", format(production["bytes"], ",").replace(",", " "))
    html = html.replace("__APK_SHA256__", production["sha256"])
    assert "__APK_" not in (html if args.artifact_commit else html.replace("__APK_SOURCE_COMMIT__", "")), "Unresolved release input"
    for public, raw, _record in prepared:
        public.parent.mkdir(parents=True, exist_ok=True)
        public.write_bytes(raw)
    page.write_text(html)
    provenance = {"schemaVersion": 1, "release": VERSION, "screenshotsEdited": False, "images": [record for _, _, record in prepared]}
    out = ROOT / "docs/validation/site-v1.14"
    (out / "screenshot-provenance.json").write_text(json.dumps(provenance, indent=2) + "\n")
    record = {"recorded_at_utc": datetime.datetime.now(datetime.timezone.utc).isoformat(), "release": VERSION, "artifact_source_commit": args.artifact_commit, "artifact_link_bound": args.artifact_commit is not None, "production_apk_sha256": production["sha256"], "native_full_frames": len(prepared), "scope": "Local copies and release binding only; a missing immutable commit is preparation, not acceptance. Offline checks, browser review and publication remain separate."}
    (out / "preparation.json").write_text(json.dumps(record, indent=2) + "\n")
    print(json.dumps(record))


if __name__ == "__main__":
    main()
