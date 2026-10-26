#!/usr/bin/env bash
set -euo pipefail

task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-meta-tests.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT

if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then
    task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then
    task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else
    task_java_bin="$(dirname "$(command -v javac)")"
fi

"$task_java_bin/javac" -d "$task_build" \
    "$task_root"/scripts/test-support/android/annotation/*.java \
    "$task_root"/scripts/test-support/android/content/*.java \
    "$task_root"/scripts/test-support/android/graphics/*.java \
    "$task_root"/app/src/main/java/com/androidhit/lumadrift/data/*.java \
    "$task_root/scripts/MetaTest.java"
"$task_java_bin/java" -cp "$task_build" MetaTest
"$task_java_bin/java" -cp "$task_build" MetaTest --export > "$task_build/export.json"
python3 - "$task_build/export.json" <<'PY'
import json, sys
with open(sys.argv[1], encoding="utf-8") as source:
    document = json.load(source)
assert document["sessions"] == 1
assert document["events"][0]["fields"]["message"] == 'quotation" backslash\\ newline\n tab\t control\x01 emoji🙂'
print("PASS exported journal parses as JSON and round-trips Unicode and control characters")
PY
