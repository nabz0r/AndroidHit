#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JAVA_HOME=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_BUILD=$(mktemp -d "${TMPDIR:-/tmp}/luma-cosmetic-vault-v17.XXXXXX")
trap 'rm -rf "$TASK_BUILD"' EXIT HUP INT TERM
cd "$TASK_ROOT"
TASK_REPORT=${1:-docs/validation/v1.7/vault}
TASK_JSON_JAR=${LUMA_JSON_JAR:-}
if [ -z "$TASK_JSON_JAR" ]; then
    TASK_JSON_JAR=$(rg --files "$HOME/.gradle/caches/modules-2/files-2.1/org.json/json" | rg '/json-[^/]+\.jar$' | sort | tail -1)
fi
test -f "$TASK_JSON_JAR"
rg -q "buildConfigField 'boolean', 'PAID_COSMETIC_DRAWS_ENABLED', 'false'" app/build.gradle
node scripts/generate-cosmetic-vault-fixtures.mjs "$TASK_REPORT"
find scripts/test-support/vault -name '*.java' -print0 | xargs -0 "$TASK_JAVA_HOME/bin/javac" \
    -Xlint:all -cp "$TASK_JSON_JAR" -d "$TASK_BUILD" \
    scripts/test-support/audio/android/os/Handler.java \
    scripts/test-support/audio/android/os/Looper.java scripts/test-support/audio/android/os/SystemClock.java \
    app/src/main/java/com/androidhit/lumadrift/commerce/BillingPolicy.java \
    app/src/main/java/com/androidhit/lumadrift/data/CosmeticCatalog.java \
    app/src/main/java/com/androidhit/lumadrift/data/Strings.java app/src/main/java/com/androidhit/lumadrift/data/V17Strings.java \
    app/src/main/java/com/androidhit/lumadrift/social/CosmeticVaultProtocol.java \
    app/src/main/java/com/androidhit/lumadrift/social/CosmeticVaultClient.java \
    app/src/main/java/com/androidhit/lumadrift/social/CosmeticVaultStrings.java scripts/CosmeticVaultTest.java
"$TASK_JAVA_HOME/bin/java" -cp "$TASK_BUILD:$TASK_JSON_JAR" com.androidhit.lumadrift.social.CosmeticVaultTest "$TASK_REPORT/server-fixtures.json"
