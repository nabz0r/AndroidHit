#!/usr/bin/env python3
"""Verify frozen Luma 1.17 production/Lab APKs and AABs; never build, sign or use adb.

Requires JDK 17, Android build-tools 36.0.0 and the pinned official bundletool JAR.
Download that public tool if absent:
curl --fail --location --output /tmp/bundletool-all-1.18.1.jar \
  https://github.com/google/bundletool/releases/download/1.18.1/bundletool-all-1.18.1.jar

The recorded SHA256 pins the tool downloaded from that official release; GitHub's
release metadata supplied no separate published asset checksum. No private key,
signing configuration or credential is read. Reports contain only public binary
metadata, hashes and verifier output.
"""
import argparse
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import re
import struct
import subprocess
import sys
import tempfile
import zipfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
CERTIFICATE = "dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287"
BUNDLETOOL_VERSION = "1.18.1"
BUNDLETOOL_SHA256 = "675786493983787ffa11550bdb7c0715679a44e1643f3ff980a529e9c822595c"
BUNDLETOOL_URL = "https://github.com/google/bundletool/releases/download/1.18.1/bundletool-all-1.18.1.jar"


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def command(*arguments):
    result = subprocess.run([str(argument) for argument in arguments], capture_output=True,
                            text=True, timeout=120)
    if result.returncode:
        raise RuntimeError("Verifier failed: " + " ".join(map(str, arguments)) + "\n"
                           + result.stdout + result.stderr)
    return result.stdout + result.stderr


def artifact_paths():
    result = []
    for variant in ("production", "lab"):
        result.append((variant, "apk", ROOT / ("app/build/outputs/apk/" + variant
                      + "/release/app-" + variant + "-release.apk")))
        result.append((variant, "aab", ROOT / ("app/build/outputs/bundle/" + variant
                      + "Release/app-" + variant + "-release.aab")))
    return result


def source_inputs():
    inputs = sorted(path for path in (ROOT / "app/src").rglob("*") if path.is_file())
    inputs.extend(ROOT / name for name in ("app/build.gradle", "build.gradle", "settings.gradle",
                                          "gradle.properties", "app/proguard-rules.pro")
                  if (ROOT / name).exists())
    return inputs


def native_audit(path):
    container = path.read_bytes()
    entries = []
    with zipfile.ZipFile(path) as archive:
        selected = [info for info in archive.infolist() if info.filename.endswith(".so")
                    and re.search(r"(?:^|/)(?:arm64-v8a|x86_64)/", info.filename)]
        if not selected:
            raise AssertionError("No expected 64-bit native payloads in " + str(path))
        for info in selected:
            elf = archive.read(info)
            if elf[:4] != b"\x7fELF" or elf[4] != 2 or elf[5] != 1:
                raise AssertionError("Unsupported 64-bit little-endian ELF: " + info.filename)
            machine = struct.unpack_from("<H", elf, 18)[0]
            if machine not in (62, 183):
                raise AssertionError("Unexpected ELF machine: " + info.filename)
            phoff = struct.unpack_from("<Q", elf, 32)[0]
            phsize, phnum = struct.unpack_from("<HH", elf, 54)
            if phsize < 56:
                raise AssertionError("Malformed ELF program-header size")
            segments = []
            for index in range(phnum):
                start = phoff + index * phsize
                if struct.unpack_from("<I", elf, start)[0] != 1:
                    continue
                offset, address = struct.unpack_from("<QQ", elf, start + 8)
                alignment = struct.unpack_from("<Q", elf, start + 48)[0]
                if alignment < 16384 or offset % 16384 != address % 16384:
                    raise AssertionError("ELF does not satisfy 16KB LOAD alignment: " + info.filename)
                segments.append({"offset": offset, "virtual_address": address, "alignment": alignment})
            if not segments:
                raise AssertionError("No PT_LOAD segment in " + info.filename)
            entry = {"name": info.filename, "bytes": len(elf), "sha256": hashlib.sha256(elf).hexdigest(),
                     "elf_machine": machine, "load_segments": segments}
            if path.suffix == ".apk" and info.compress_type == zipfile.ZIP_STORED:
                name_size, extra_size = struct.unpack_from("<HH", container, info.header_offset + 26)
                payload = info.header_offset + 30 + name_size + extra_size
                if payload % 16384:
                    raise AssertionError("Uncompressed native APK payload is not 16KB aligned: " + info.filename)
                entry["uncompressed_zip_payload_offset"] = payload
            entries.append(entry)
    return entries


def version_metadata(variant, kind, path, java_dir, build_tools, bundletool, args):
    expected = {"package": "com.androidhit.lumadrift" + (".sandbox" if variant == "lab" else ""),
                "versionCode": str(args.expected_version_code),
                "versionName": args.lab_version if variant == "lab" else args.production_version,
                "minSdk": "26", "targetSdk": "36"}
    if kind == "apk":
        raw = command(build_tools / "aapt2", "dump", "badging", path)
        package = re.search(r"^package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'", raw, re.M)
        minimum = re.search(r"^(?:minSdkVersion|sdkVersion):'([^']+)'", raw, re.M)
        target = re.search(r"^targetSdkVersion:'([^']+)'", raw, re.M)
        if not package or not minimum or not target:
            raise AssertionError("APK metadata not readable: " + str(path))
        actual = dict(zip(("package", "versionCode", "versionName"), package.groups()))
        actual.update(minSdk=minimum.group(1), targetSdk=target.group(1))
        if "application-debuggable" in raw:
            raise AssertionError("Release APK is debuggable")
    else:
        raw = command(java_dir / "java", "-jar", bundletool, "dump", "manifest", "--bundle=" + str(path), "--module=base")
        document = ET.fromstring(raw)
        android = "{http://schemas.android.com/apk/res/android}"
        sdk = document.find("uses-sdk")
        if sdk is None:
            raise AssertionError("Bundle uses-sdk missing")
        actual = {"package": document.get("package"), "versionCode": document.get(android + "versionCode"),
                  "versionName": document.get(android + "versionName"), "minSdk": sdk.get(android + "minSdkVersion"),
                  "targetSdk": sdk.get(android + "targetSdkVersion")}
        application = document.find("application")
        if application is None or application.get(android + "debuggable", "false") != "false":
            raise AssertionError("Release bundle application missing or debuggable")
    if actual != expected:
        raise AssertionError("Exact release metadata mismatch " + str(path) + ": " + repr(actual) + " expected " + repr(expected))
    return actual, raw


def verify(args):
    out = Path(args.out)
    if not out.is_absolute():
        out = ROOT / out
    out.mkdir(parents=True, exist_ok=True)
    (out / "BINARY_AUDIT_PENDING.txt").write_text("Candidate binary verification has not completed. No PASS claim until this marker is removed.\n")
    java_dir = Path(args.java_bin)
    build_tools = Path(args.build_tools)
    bundletool = Path(args.bundletool)
    if args.build_log:
        build_log = Path(args.build_log).read_text()
        if "BUILD SUCCESSFUL" not in build_log or build_log.rfind("BUILD FAILED") > build_log.rfind("BUILD SUCCESSFUL"):
            raise AssertionError("Build log does not record a completed successful build")
    if not bundletool.is_file() or digest(bundletool) != BUNDLETOOL_SHA256:
        raise AssertionError("Pinned official bundletool JAR missing or mismatched: " + str(bundletool)
                             + ". Download from " + BUNDLETOOL_URL)
    if command(java_dir / "java", "-jar", bundletool, "version").strip() != BUNDLETOOL_VERSION:
        raise AssertionError("Unexpected bundletool version")
    paths = artifact_paths()
    for _, _, path in paths:
        if not path.is_file():
            raise AssertionError("Missing built artifact: " + str(path))
    sources = source_inputs()
    initial_sources = {str(path.relative_to(ROOT)): digest(path) for path in sources}
    freeze_path = Path(args.source_freeze).resolve()
    freeze_sha = digest(freeze_path)
    frozen_sources = json.loads(freeze_path.read_text())
    if not isinstance(frozen_sources, dict) or frozen_sources != initial_sources:
        differences = [key for key in set(frozen_sources) | set(initial_sources)
                       if frozen_sources.get(key) != initial_sources.get(key)] if isinstance(frozen_sources, dict) else []
        raise AssertionError("Sources do not match the exact candidate freeze: " + repr(sorted(differences)))
    initial_artifacts = {str(path.relative_to(ROOT)): digest(path) for _, _, path in paths}
    latest = max(sources, key=lambda path: path.stat().st_mtime)
    if latest.stat().st_mtime > min(path.stat().st_mtime for _, _, path in paths):
        raise AssertionError("Game/build source input newer than an artifact: " + str(latest.relative_to(ROOT)))
    manifest = {
        "passed": True,
        "release": args.production_version,
        "expected_version_code": args.expected_version_code,
        "source_freeze": {"path": str(freeze_path), "sha256": freeze_sha, "input_count": len(frozen_sources)},
        "verified_at_utc": datetime.now(timezone.utc).isoformat(),
        "scope": "Existing final-candidate binaries. No build, private signing, install or emulator access.",
        "build_log": str(args.build_log) if args.build_log else None,
        "expected_certificate_sha256": CERTIFICATE,
        "toolchain": {"java": command(java_dir / "java", "-version").strip(),
                      "build_tools": str(build_tools), "bundletool_version": BUNDLETOOL_VERSION,
                      "bundletool_sha256": BUNDLETOOL_SHA256, "bundletool_source": BUNDLETOOL_URL},
        "source_inputs_sha256": initial_sources, "artifacts": [],
    }
    reports = {}
    apk_signatures, aab_signatures, zipalign, bundle_lines, native_lines = [], [], [], [], []
    with tempfile.TemporaryDirectory(prefix="luma-public-signature-verifier-") as classes:
        command(java_dir / "javac", "-d", classes, ROOT / "scripts/VerifyBundleSignature.java")
        for variant, kind, path in paths:
            relative = str(path.relative_to(ROOT))
            item = {"path": relative, "bytes": path.stat().st_size, "sha256": initial_artifacts[relative]}
            metadata, metadata_raw = version_metadata(variant, kind, path, java_dir, build_tools, bundletool, args)
            item["release_metadata"] = metadata
            reports["metadata-" + variant + "-" + kind + ".txt"] = metadata_raw
            if kind == "apk":
                signature = command(java_dir / "java", "-jar", build_tools / "lib/apksigner.jar",
                                    "verify", "--verbose", "--print-certs", path)
                fingerprints = re.findall(r"certificate SHA-256 digest: ([a-f0-9]{64})", signature)
                if fingerprints != [CERTIFICATE] or "Number of signers: 1" not in signature:
                    raise AssertionError("Unexpected APK signing identity")
                if "Verified using v2 scheme (APK Signature Scheme v2): true" not in signature:
                    raise AssertionError("Expected APK v2 integrity verification")
                apk_signatures.append(variant.upper() + " APK\nCommand: apksigner verify --verbose --print-certs "
                                      + relative + "\n" + signature)
                alignment = command(build_tools / "zipalign", "-c", "-P", "16", "-v", "4", path)
                if not alignment.rstrip().endswith("Verification successful"):
                    raise AssertionError("ZIP alignment did not report successful verification")
                zipalign.append(variant.upper() + " APK\nCommand: zipalign -c -P 16 -v 4 " + relative + "\n" + alignment)
            else:
                signature = command(java_dir / "java", "-cp", classes, "VerifyBundleSignature", path, CERTIFICATE)
                aab_signatures.append(variant.upper() + " AAB\n" + signature)
                validation = command(java_dir / "java", "-jar", bundletool, "validate", "--bundle=" + str(path))
                reports["bundletool-" + variant + "-raw.txt"] = validation
                bundle_lines.append("PASS " + variant.upper() + " AAB: bundletool " + BUNDLETOOL_VERSION
                                    + " validate --bundle=" + relative + "; exit 0")
            item["native_64_bit"] = native_audit(path)
            native_lines.append(relative + " SHA256 " + item["sha256"])
            for entry in item["native_64_bit"]:
                alignments = sorted({segment["alignment"] for segment in entry["load_segments"]})
                native_lines.append("  PASS " + entry["name"] + ": " + str(len(entry["load_segments"]))
                                    + " PT_LOAD segments, alignments=" + repr(alignments)
                                    + ", native SHA256=" + entry["sha256"])
            manifest["artifacts"].append(item)
    if initial_artifacts != {str(path.relative_to(ROOT)): digest(path) for _, _, path in paths}:
        raise AssertionError("Artifact changed during verification")
    if initial_sources != {str(path.relative_to(ROOT)): digest(path) for path in sources}:
        raise AssertionError("Game/build source changed during verification")
    if digest(freeze_path) != freeze_sha:
        raise AssertionError("Source-freeze manifest changed during verification")
    if {str(path.relative_to(ROOT)): digest(path) for path in source_inputs()} != initial_sources:
        raise AssertionError("Source file set changed during verification")
    manifest["source_freeze_matches_before_and_after"] = True
    manifest["artifact_hashes_stable_during_verification"] = True
    manifest["source_hashes_stable_during_verification"] = True
    manifest["latest_source_input"] = str(latest.relative_to(ROOT))
    manifest["sources_older_than_outputs"] = True
    payloads = sum(len(item["native_64_bit"]) for item in manifest["artifacts"])
    segments = sum(len(entry["load_segments"]) for item in manifest["artifacts"] for entry in item["native_64_bit"])
    reports["apk-signatures.txt"] = "\n".join(apk_signatures)
    reports["aab-signatures.txt"] = "\n".join(aab_signatures)
    reports["zipalign-16kb.txt"] = "\n".join(zipalign)
    reports["native-elf-alignment.txt"] = (
        "64-bit arm64-v8a/x86_64 ELF PT_LOAD p_align >= 16384 and offset/address congruence modulo 16384.\n"
        "Uncompressed APK native ZIP payload offsets additionally checked for 16KB alignment.\n\n"
        + "\n".join(native_lines) + "\n\nPASS " + str(payloads) + " native payloads and " + str(segments)
        + " PT_LOAD segments. Scope: binary alignment, not an OS with 16KB-page runtime test.\n")
    reports["bundletool-validation.txt"] = (
        "Official pinned bundletool " + BUNDLETOOL_VERSION + "\nSource: " + BUNDLETOOL_URL
        + "\nPinned downloaded JAR SHA256: " + BUNDLETOOL_SHA256
        + "\nGitHub release metadata supplied no separate published asset digest.\n\n"
        + "\n".join(bundle_lines) + "\n\nScope: bundle schema verification, not Google Play approval.\n")
    reports["binary-sha256.txt"] = "\n".join(item["sha256"] + "  " + item["path"]
                                            + "  (" + str(item["bytes"]) + " bytes)"
                                            for item in manifest["artifacts"]) + "\n"
    reports["binary-final-candidate.json"] = json.dumps(manifest, indent=2) + "\n"
    reports["source-freeze.json"] = json.dumps(frozen_sources, indent=2) + "\n"
    reports["binary-verification.txt"] = (
        f"PASS exact production/Lab package names, {args.production_version}/{args.lab_version} version names, code{args.expected_version_code}, minSdk26 and targetSdk36 in all four artifacts.\n"
        "PASS production + Lab APK signatures against the pinned public RSA certificate.\n"
        "PASS both AAB signatures, every non-META-INF payload checked by JarFile.\n"
        "PASS both APK ZIP alignments with -P 16.\n"
        "PASS " + str(payloads) + " 64-bit native ELF payloads / " + str(segments) + " LOAD segments.\n"
        "PASS official pinned bundletool " + BUNDLETOOL_VERSION + " validation for both AABs.\n"
        "PASS exact source freeze matched before/after; game/build source and four artifact hashes remained stable.\n"
        "No emulator accessed; no claim of Play acceptance, real checkout or OS 16KB-page execution.\n")
    for name, contents in reports.items():
        (out / name).write_text(contents.rstrip() + "\n", encoding="utf-8")
    (out / "BINARY_AUDIT_PENDING.txt").unlink(missing_ok=True)
    print(reports["binary-verification.txt"], end="")
    print(reports["binary-sha256.txt"], end="")
    print("Reports: " + str(out))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--bundletool", default="/tmp/bundletool-all-1.18.1.jar")
    parser.add_argument("--java-bin", default=str(Path(os.environ.get("JAVA_HOME")
                        or "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home") / "bin"))
    sdk = Path(os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
               or Path.home() / "Library/Android/sdk")
    parser.add_argument("--build-tools", default=str(sdk / "build-tools/36.0.0"))
    parser.add_argument("--out", default="docs/validation/v1.17/binaries")
    parser.add_argument("--source-freeze", required=True, help="Exact source-path to SHA256 map frozen by the build owner")
    parser.add_argument("--expected-version-code", type=int, default=24)
    parser.add_argument("--production-version", default="1.17.0-rc6")
    parser.add_argument("--lab-version", default="1.17.0-sandbox-rc6")
    parser.add_argument("--build-log")
    args = parser.parse_args()
    try:
        verify(args)
        return 0
    except Exception as error:
        print("FAIL binary verification: " + str(error), file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
