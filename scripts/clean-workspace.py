#!/usr/bin/env python3
"""Remove only ignored, reproducible local outputs. Dry-run is the default."""

import argparse
import datetime
import json
import os
from pathlib import Path
import re
import shutil
import stat
import subprocess
import sys


DIRECTORIES = ("app/build", "build", ".gradle", "server/node_modules")
RUNTIME_NAMES = {"node", "nodejs", "npm", "npx", "java", "python", "python3", "firebase"}


class SafetyError(RuntimeError):
    pass


def command(args, root, records, env=None, timeout=45, required=True):
    result = subprocess.run(args, cwd=str(root), env=env, text=True,
                            stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=timeout)
    records.append({"argv": args, "returnCode": result.returncode,
                    "stdout": result.stdout[-12000:], "stderr": result.stderr[-12000:]})
    if required and result.returncode != 0:
        raise SafetyError("Command failed: " + " ".join(args))
    return result


def tracked_files(root):
    result = subprocess.run(["git", "ls-files", "-z"], cwd=str(root), check=True,
                            stdout=subprocess.PIPE)
    return set(os.fsdecode(p) for p in result.stdout.split(b"\0") if p)


def guard_target(root, relative, tracked):
    rel = Path(relative)
    if rel.is_absolute() or not rel.parts or any(p in ("..", ".git", ".signing") for p in rel.parts):
        raise SafetyError("Unsafe target path: " + str(relative))
    if str(rel) not in DIRECTORIES and rel.name not in ("__pycache__", ".DS_Store"):
        raise SafetyError("Target is outside the cleanup allowlist: " + str(relative))
    path = root / rel
    if path == root or root not in path.parents:
        raise SafetyError("Target escapes the repository")
    for current in [path] + list(path.parents):
        if current == root:
            break
        if current.is_symlink():
            raise SafetyError("Symlink target/ancestor refused: " + str(relative))
    prefix = str(rel) + "/"
    if any(p == str(rel) or p.startswith(prefix) for p in tracked):
        raise SafetyError("Tracked file found in cleanup target: " + str(relative))
    result = subprocess.run(["git", "check-ignore", "-q", "--no-index", "--", str(rel)],
                            cwd=str(root))
    if result.returncode != 0:
        raise SafetyError("Target is not ignored by Git: " + str(relative))
    if path.exists() and not (path.is_dir() if str(rel) in DIRECTORIES or rel.name == "__pycache__" else path.is_file()):
        raise SafetyError("Unexpected target file type: " + str(relative))
    return path


def size(path):
    result = {"files": 0, "directories": 0, "symlinks": 0, "logicalBytes": 0, "allocatedBytes": 0}
    if not path.exists():
        return result
    paths = [path]
    while paths:
        current = paths.pop()
        info = current.lstat()
        if stat.S_ISLNK(info.st_mode):
            result["symlinks"] += 1
        elif stat.S_ISDIR(info.st_mode):
            result["directories"] += 1
            paths.extend(current.iterdir())
            continue
        elif not stat.S_ISREG(info.st_mode):
            raise SafetyError("Special file refused: " + str(current))
        result["files"] += 1
        result["logicalBytes"] += info.st_size
        result["allocatedBytes"] += getattr(info, "st_blocks", 0) * 512
    return result


def candidates(root, tracked):
    result = []
    for relative in DIRECTORIES:
        path = root / relative
        if path.exists() or path.is_symlink():
            guard_target(root, relative, tracked)
            result.append(relative)
    for directory, dirs, files in os.walk(str(root), followlinks=False):
        relative = Path(directory).relative_to(root)
        dirs[:] = [d for d in dirs if d not in (".git", ".signing") and not (Path(directory) / d).is_symlink()
                   and str(relative / d) not in DIRECTORIES]
        if "__pycache__" in dirs:
            candidate = str(relative / "__pycache__")
            guard_target(root, candidate, tracked)
            result.append(candidate)
            dirs.remove("__pycache__")
        if ".DS_Store" in files:
            candidate = str(relative / ".DS_Store")
            guard_target(root, candidate, tracked)
            result.append(candidate)
    return sorted(result)


def backend_processes(root, records):
    listing = command(["ps", "-axo", "pid=,comm=,args="], root, records)
    # Record only relevant process findings, rather than unrelated user commands.
    records[-1]["stdout"] = "Process list inspected; unrelated commands omitted."
    cwd_listing = command(["lsof", "-n", "-P", "-a", "-d", "cwd", "-Fpn"], root, records, required=False)
    if cwd_listing.returncode not in (0, 1) or not cwd_listing.stdout:
        raise SafetyError("Cannot verify active backend working directories")
    records[-1]["stdout"] = "Working directories inspected; unrelated paths omitted."
    cwd_by_pid = {}
    pid = None
    for line in cwd_listing.stdout.splitlines():
        if line.startswith("p"):
            pid = line[1:]
        elif line.startswith("n") and pid:
            cwd_by_pid[pid] = line[1:]
    found = []
    server = str(root / "server")
    for line in listing.stdout.splitlines():
        values = line.strip().split(None, 2)
        if len(values) != 3:
            continue
        pid, executable, args = values
        name = Path(executable).name
        if name not in RUNTIME_NAMES and not re.fullmatch(r"python\d+(\.\d+)?", name):
            continue
        cwd = cwd_by_pid.get(pid, "")
        if cwd == server or cwd.startswith(server + "/") or server + "/" in args or (cwd == str(root) and "server/" in args):
            found.append({"pid": int(pid), "command": args, "cwd": cwd})
    return found


def stop_gradle(root, records):
    homes = [os.environ.get("JAVA_HOME", ""), "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"]
    java_home = next((Path(p) for p in homes if p and (Path(p) / "bin/java").is_file()), None)
    if java_home is None:
        raise SafetyError("Java 17 is required to inspect/stop the project Gradle daemon")
    version = command([str(java_home / "bin/java"), "-version"], root, records)
    if not re.search(r'version "17[.\"]', version.stdout + version.stderr):
        raise SafetyError("Selected Java is not version 17")
    environment = dict(os.environ)
    environment["JAVA_HOME"] = str(java_home)
    status = command(["./gradlew", "--status"], root, records, env=environment)
    if re.search(r"\bBUSY\b", status.stdout):
        raise SafetyError("A Gradle build is active; cleanup refused")
    command(["./gradlew", "--stop"], root, records, env=environment)


def total(rows, key):
    return {field: sum(row[key][field] for row in rows) for field in
            ("files", "directories", "symlinks", "logicalBytes", "allocatedBytes")}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apply", action="store_true", help="Apply the reviewed allowlist; otherwise report only")
    parser.add_argument("--receipt", type=Path, help="Write the JSON receipt at this repository-relative path")
    options = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    receipt_target = None
    if options.receipt:
        relative = options.receipt
        if relative.is_absolute() or ".." in relative.parts or not str(relative).startswith("docs/validation/cleanup-"):
            parser.error("Receipts must stay under docs/validation/cleanup-*")
        receipt_target = root / relative
        if any(p.is_symlink() for p in [receipt_target] + list(receipt_target.parents) if p != root):
            parser.error("Symlink receipt path refused")
    records = []
    report = {"schemaVersion": 1, "mode": "apply" if options.apply else "dry-run", "root": str(root),
              "recordedAtUtc": datetime.datetime.now(datetime.timezone.utc).isoformat(),
              "subprocesses": records, "targets": [], "success": False,
              "scope": "Ignored reproducible outputs only; no Git refs/index/history edits, releases, public payloads, source, evidence, signing material or global caches removed."}
    exit_code = 0
    try:
        observed = command(["git", "rev-parse", "--show-toplevel"], root, records).stdout.strip()
        if Path(observed).resolve() != root or not (root / "app/src/main/java/com/androidhit/lumadrift/engine/RelayCatalog.java").is_file():
            raise SafetyError("The script must live in the Luma Drift repository")
        if not shutil.rmtree.avoids_symlink_attacks:
            raise SafetyError("This Python does not provide symlink-safe directory removal")
        tracked = tracked_files(root)
        selected = candidates(root, tracked)
        backend = backend_processes(root, records) if "server/node_modules" in selected else []
        report["activeBackendProcesses"] = backend
        if backend:
            selected.remove("server/node_modules")
            report["skipped"] = [{"path": "server/node_modules", "reason": "Active backend process"}]
        if options.apply and any(p in selected for p in ("app/build", "build", ".gradle")):
            stop_gradle(root, records)
        for relative in selected:
            report["targets"].append({"path": relative, "before": size(root / relative),
                                       "ignored": True, "trackedFiles": 0, "removed": False})
        report["before"] = total(report["targets"], "before")
        command(["df", "-k", str(root)], root, records)
        report["diskFreeBeforeBytes"] = shutil.disk_usage(str(root)).free
        if options.apply:
            for row in report["targets"]:
                path = guard_target(root, row["path"], tracked_files(root))
                # Recheck immediately before each mutation. Nested links are
                # unlinked by fd-based rmtree and are never traversed.
                if path.is_dir():
                    shutil.rmtree(path)
                else:
                    path.unlink()
                row["removed"] = True
        for row in report["targets"]:
            row["after"] = size(root / row["path"])
        report["after"] = total(report["targets"], "after")
        report["logicalBytesRemoved"] = report["before"]["logicalBytes"] - report["after"]["logicalBytes"]
        report["filesRemoved"] = report["before"]["files"] - report["after"]["files"]
        report["diskFreeAfterBytes"] = shutil.disk_usage(str(root)).free
        report["diskFreeChangeBytes"] = report["diskFreeAfterBytes"] - report["diskFreeBeforeBytes"]
        report["diskMeasurementNote"] = "Free-space delta is a volume-wide observation; APFS clones, snapshots and concurrent activity prevent attributing it exactly to logical file sizes."
        command(["df", "-k", str(root)], root, records)
        report["success"] = True
    except (SafetyError, OSError, subprocess.SubprocessError) as error:
        report["error"] = str(error)
        exit_code = 1
    payload = json.dumps(report, indent=2, ensure_ascii=False) + "\n"
    if receipt_target:
        receipt_target.parent.mkdir(parents=True, exist_ok=True)
        receipt_target.write_text(payload, encoding="utf-8")
    sys.stdout.write(payload)
    return exit_code


if __name__ == "__main__":
    sys.exit(main())
