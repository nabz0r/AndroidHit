#!/usr/bin/env python3
"""Exact APK transport using verified unchanged ZIP compressed byte ranges, Python 3.9+.

No ZIP extraction, decompression/recompression of members, signing, execution or networking.
The complete final APK hash is pinned by the caller; the APK signing block stays literal.
"""
import argparse
import base64
import binascii
import gzip
import hashlib
import json
import os
from pathlib import Path
import re
import struct
import sys
import tempfile
import zipfile
import zlib

SCHEMA = "luma-apk-byte-delta-v1"
CHUNK = 1024 * 1024
MAX_APK = 1024 * CHUNK
MAX_PATCH = 10 * CHUNK
MAX_JSON = 64 * CHUNK
MAX_LITERAL_TOTAL = 32 * CHUNK
MAX_OPERATIONS = 20000
MAX_ENTRIES = 10000
SHA = re.compile(r"^[0-9a-f]{64}$")


class DeltaError(ValueError):
    pass


def require(condition, message):
    if not condition:
        raise DeltaError(message)


def digest_file(stream):
    stream.seek(0)
    digest = hashlib.sha256()
    for block in iter(lambda: stream.read(CHUNK), b""):
        digest.update(block)
    return digest.hexdigest()


def digest_path(path):
    with Path(path).open("rb") as stream:
        return digest_file(stream)


def integer(value, low, high, label):
    require(type(value) is int and low <= value <= high, "Invalid " + label)
    return value


def exact_keys(value, expected, label):
    require(type(value) is dict and set(value) == set(expected), "Invalid " + label + " fields")


def sha(value, label):
    require(type(value) is str and SHA.fullmatch(value), "Invalid " + label + " SHA-256")
    return value


def meta(path, stream, expected):
    size = os.fstat(stream.fileno()).st_size
    integer(size, 1, MAX_APK, "APK size")
    actual = digest_file(stream)
    require(actual == sha(expected, "expected input"), "Input SHA-256 mismatch: " + path.name)
    return {"name": path.name, "bytes": size, "sha256": actual}


def range_digest(stream, offset, size):
    stream.seek(offset)
    digest = hashlib.sha256()
    while size:
        block = stream.read(min(CHUNK, size))
        require(block, "Unexpected end of compressed data")
        digest.update(block)
        size -= len(block)
    return digest.hexdigest()


def ranges_equal(a, ao, b, bo, size):
    a.seek(ao)
    b.seek(bo)
    while size:
        amount = min(CHUNK, size)
        left, right = a.read(amount), b.read(amount)
        if len(left) != amount or left != right:
            return False
        size -= amount
    return True


def zip_regions(stream, size):
    """Read local data offsets only. Never extract or trust CRC as equality proof."""
    regions = {}
    with zipfile.ZipFile(stream) as archive:
        entries = archive.infolist()
        require(len(entries) <= MAX_ENTRIES, "Too many ZIP entries")
        for entry in entries:
            require(entry.filename not in regions, "Duplicate ZIP member")
            require(not entry.flag_bits & 1, "Encrypted ZIP members are unsupported")
            require(entry.compress_type in (zipfile.ZIP_STORED, zipfile.ZIP_DEFLATED), "Unsupported ZIP compression")
            stream.seek(entry.header_offset)
            local = stream.read(30)
            require(len(local) == 30 and local[:4] == b"PK\x03\x04", "Invalid ZIP local header")
            header = struct.unpack("<4s5H3I2H", local)
            require(header[3] == entry.compress_type, "ZIP local compression disagrees")
            offset = entry.header_offset + 30 + header[-2] + header[-1]
            require(0 <= offset <= size and 0 <= entry.compress_size <= size - offset, "ZIP range outside APK")
            regions[entry.filename] = (offset, entry.compress_size, entry.compress_type, entry.file_size)
    ordered = sorted(regions.values())
    for previous, current in zip(ordered, ordered[1:]):
        require(previous[0] + previous[1] <= current[0], "Overlapping compressed ZIP ranges")
    return regions


def output_guard(output, protected, replace):
    output = Path(output)
    require(not output.is_symlink(), "Refusing a symlink output")
    require(output.resolve() not in [Path(path).resolve() for path in protected], "Output aliases an input")
    require(output.parent.is_dir(), "Output parent must exist")
    require(replace or not output.exists(), "Output already exists; use --replace explicitly")
    return output


def atomic_writer(output, replace, write):
    fd, name = tempfile.mkstemp(prefix="." + output.name + ".", suffix=".tmp", dir=str(output.parent))
    temporary = Path(name)
    try:
        with os.fdopen(fd, "wb") as stream:
            write(stream)
            stream.flush()
            os.fsync(stream.fileno())
        os.chmod(temporary, 0o644)
        if replace:
            os.replace(str(temporary), str(output))
        else:
            # Same-directory hard link provides atomic creation without clobbering a raced file.
            os.link(str(temporary), str(output))
            temporary.unlink()
        directory = os.open(str(output.parent), os.O_RDONLY)
        try:
            os.fsync(directory)
        finally:
            os.close(directory)
    finally:
        if temporary.exists():
            temporary.unlink()


def write_report(path, report):
    if path:
        destination = Path(path)
        destination.parent.mkdir(parents=True, exist_ok=True)
        encoded = (json.dumps(report, indent=2, sort_keys=True) + "\n").encode("utf-8")
        atomic_writer(destination, True, lambda stream: stream.write(encoded))
    return report


def build(base, target, output, expected_base, expected_target, report=None, replace=False):
    base, target = Path(base), Path(target)
    output = output_guard(output, [base, target], replace)
    if report:
        require(not Path(report).is_symlink() and Path(report).resolve() not in [base.resolve(), target.resolve(), output.resolve()], "Report aliases an artifact")
    with base.open("rb") as source, target.open("rb") as final:
        base_meta, target_meta = meta(base, source, expected_base), meta(target, final, expected_target)
        before = zip_regions(source, base_meta["bytes"])
        after = zip_regions(final, target_meta["bytes"])
        copied = []
        for name, region in sorted(after.items(), key=lambda row: row[1][0]):
            prior = before.get(name)
            if not prior or region[1] == 0 or prior[1:] != region[1:]:
                continue
            target_offset, size = region[:2]
            old_hash = range_digest(source, prior[0], size)
            if old_hash != range_digest(final, target_offset, size):
                continue
            # Hash equality is followed by an independent chunk-for-chunk byte comparison.
            require(ranges_equal(source, prior[0], final, target_offset, size), "Compressed range hash collision or concurrent change")
            copied.append((target_offset, prior[0], size, old_hash, name))
        operations, cursor, copied_bytes, literal_bytes = [], 0, 0, 0

        def literal(start, length):
            nonlocal literal_bytes
            final.seek(start)
            while length:
                amount = min(CHUNK, length)
                data = final.read(amount)
                require(len(data) == amount, "Target changed while building delta")
                operations.append({"literal": {"bytes": amount, "sha256": hashlib.sha256(data).hexdigest(), "data": base64.b64encode(data).decode("ascii")}})
                literal_bytes += amount
                require(literal_bytes <= MAX_LITERAL_TOTAL, "Delta has over 32 MiB of literals; use another transport")
                length -= amount

        for target_offset, base_offset, size, data_hash, name in copied:
            require(target_offset >= cursor, "Overlapping target reuse ranges")
            literal(cursor, target_offset - cursor)
            operations.append({"copy": {"offset": base_offset, "bytes": size, "sha256": data_hash}})
            copied_bytes += size
            cursor = target_offset + size
        literal(cursor, target_meta["bytes"] - cursor)
        statistics = {"copy_operations": len(copied), "copied_bytes": copied_bytes, "literal_bytes": literal_bytes, "operations": len(operations)}
        document = {"schema": SCHEMA, "base": base_meta, "target": target_meta, "statistics": statistics, "operations": operations}
        validate(document, expected_target)
        require(digest_file(source) == base_meta["sha256"] and digest_file(final) == target_meta["sha256"], "Input changed while building delta")

        def emit(stream):
            with gzip.GzipFile(filename="", mode="wb", fileobj=stream, compresslevel=9, mtime=0) as compressed:
                encoded_bytes = 0
                for fragment in json.JSONEncoder(separators=(",", ":"), sort_keys=True).iterencode(document):
                    encoded = fragment.encode("utf-8")
                    encoded_bytes += len(encoded)
                    require(encoded_bytes <= MAX_JSON, "Expanded delta JSON exceeds bound")
                    compressed.write(encoded)
            require(stream.tell() <= MAX_PATCH, "Delta exceeds 10 MiB; use another transport")

        atomic_writer(output, replace, emit)
    result = {"passed": True, "operation": "build", "schema": SCHEMA, "base": base_meta, "target": target_meta,
              "patch": {"name": output.name, "bytes": output.stat().st_size, "sha256": digest_path(output)},
              "statistics": statistics, "compressed_entries_verified_byte_for_byte": len(copied),
              "scope": "Exact compressed ZIP data reuse; headers, signing block, changed bytes and ZIP tail remain literals. No APK rewriting or signing."}
    return write_report(report, result)


def unique_object(pairs):
    value = {}
    for key, item in pairs:
        require(key not in value, "Duplicate JSON key")
        value[key] = item
    return value


def validate(document, expected_target):
    exact_keys(document, ("schema", "base", "target", "statistics", "operations"), "root")
    require(document["schema"] == SCHEMA, "Unsupported delta schema")
    for key in ("base", "target"):
        value = document[key]
        exact_keys(value, ("name", "bytes", "sha256"), key)
        require(type(value["name"]) is str and 0 < len(value["name"]) <= 200, "Invalid artifact name")
        integer(value["bytes"], 1, MAX_APK, key + " size")
        sha(value["sha256"], key)
    require(document["target"]["sha256"] == sha(expected_target, "expected target"), "Delta target differs from independently pinned target")
    operations = document["operations"]
    require(type(operations) is list and 0 < len(operations) <= MAX_OPERATIONS, "Invalid operation count")
    copied, literals, copy_count, total = 0, 0, 0, 0
    for operation in operations:
        require(type(operation) is dict and len(operation) == 1, "Invalid operation")
        if "copy" in operation:
            value = operation["copy"]
            exact_keys(value, ("offset", "bytes", "sha256"), "copy")
            offset = integer(value["offset"], 0, document["base"]["bytes"] - 1, "copy offset")
            count = integer(value["bytes"], 1, document["base"]["bytes"] - offset, "copy length")
            copied += count
            copy_count += 1
        elif "literal" in operation:
            value = operation["literal"]
            exact_keys(value, ("bytes", "sha256", "data"), "literal")
            count = integer(value["bytes"], 1, CHUNK, "literal length")
            require(type(value["data"]) is str and len(value["data"]) == 4 * ((count + 2) // 3), "Invalid literal encoded length")
            literals += count
            require(literals <= MAX_LITERAL_TOTAL, "Literal total exceeds bound")
        else:
            raise DeltaError("Unknown operation")
        sha(value["sha256"], "operation")
        total += count
        require(total <= document["target"]["bytes"], "Output exceeds declared target size")
    require(total == document["target"]["bytes"], "Output does not cover exact target size")
    exact_keys(document["statistics"], ("copy_operations", "copied_bytes", "literal_bytes", "operations"), "statistics")
    expected = {"copy_operations": copy_count, "copied_bytes": copied, "literal_bytes": literals, "operations": len(operations)}
    require(all(type(value) is int for value in document["statistics"].values()) and document["statistics"] == expected, "Inconsistent statistics")
    return expected


def apply(base, patch, output, expected_target, patch_sha=None, report=None, replace=False):
    base, patch = Path(base), Path(patch)
    output = output_guard(output, [base, patch], replace)
    if report:
        require(not Path(report).is_symlink() and Path(report).resolve() not in [base.resolve(), patch.resolve(), output.resolve()], "Report aliases an artifact")
    integer(patch.stat().st_size, 1, MAX_PATCH, "compressed patch size")
    patch_hash = digest_path(patch)
    if patch_sha:
        require(patch_hash == sha(patch_sha, "patch"), "Patch SHA-256 mismatch")
    with gzip.open(str(patch), "rb") as compressed:
        raw = compressed.read(MAX_JSON + 1)
        require(len(raw) <= MAX_JSON, "Expanded delta JSON exceeds bound")
    document = json.loads(raw.decode("utf-8"), object_pairs_hook=unique_object,
                          parse_constant=lambda value: (_ for _ in ()).throw(DeltaError("Invalid JSON constant")))
    statistics = validate(document, expected_target)
    with base.open("rb") as source:
        require(os.fstat(source.fileno()).st_size == document["base"]["bytes"], "Base size mismatch")
        require(digest_file(source) == document["base"]["sha256"], "Base SHA-256 mismatch")
        target_hash, written = hashlib.sha256(), 0

        def reconstruct(stream):
            nonlocal written
            for operation in document["operations"]:
                if "copy" in operation:
                    value = operation["copy"]
                    source.seek(value["offset"])
                    remaining, copied_hash = value["bytes"], hashlib.sha256()
                    while remaining:
                        block = source.read(min(CHUNK, remaining))
                        require(block, "Base truncated during copy")
                        stream.write(block)
                        copied_hash.update(block)
                        target_hash.update(block)
                        written += len(block)
                        remaining -= len(block)
                    require(copied_hash.hexdigest() == value["sha256"], "Copied compressed-range SHA-256 mismatch")
                else:
                    value = operation["literal"]
                    data = base64.b64decode(value["data"], validate=True)
                    require(len(data) == value["bytes"] and hashlib.sha256(data).hexdigest() == value["sha256"], "Literal SHA-256/length mismatch")
                    stream.write(data)
                    target_hash.update(data)
                    written += len(data)
            require(written == document["target"]["bytes"], "Reconstructed size mismatch")
            require(target_hash.hexdigest() == expected_target, "Reconstructed target SHA-256 mismatch")

        atomic_writer(output, replace, reconstruct)
    result = {"passed": True, "operation": "apply", "schema": SCHEMA, "base": document["base"], "target": document["target"],
              "patch": {"name": patch.name, "bytes": patch.stat().st_size, "sha256": patch_hash}, "statistics": statistics,
              "verified_output_bytes": written, "verified_output_sha256": target_hash.hexdigest(),
              "scope": "Exact byte reconstruction and SHA-256 before atomic publication. Does not substitute for apksigner or native qualification."}
    return write_report(report, result)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    for name in ("build", "apply"):
        command = commands.add_parser(name)
        command.add_argument("--base", required=True)
        command.add_argument("--output", required=True)
        command.add_argument("--expected-target-sha256", required=True)
        command.add_argument("--report")
        command.add_argument("--replace", action="store_true")
        if name == "build":
            command.add_argument("--target", required=True)
            command.add_argument("--expected-base-sha256", required=True)
        else:
            command.add_argument("--patch", required=True)
            command.add_argument("--patch-sha256")
    args = parser.parse_args()
    try:
        if args.command == "build":
            result = build(args.base, args.target, args.output, args.expected_base_sha256, args.expected_target_sha256, args.report, args.replace)
        else:
            result = apply(args.base, args.patch, args.output, args.expected_target_sha256, args.patch_sha256, args.report, args.replace)
    except (DeltaError, OSError, EOFError, zipfile.BadZipFile, UnicodeError, json.JSONDecodeError, binascii.Error, zlib.error, RecursionError) as error:
        print("Delta refused: " + str(error), file=sys.stderr)
        return 1
    print(json.dumps(result, sort_keys=True))
    return 0


if __name__ == "__main__":
    sys.exit(main())
