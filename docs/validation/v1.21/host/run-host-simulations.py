#!/usr/bin/env python3
"""Run existing host suites against copied inputs while app work continues."""
import concurrent.futures
import datetime as dt
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import time

ROOT = Path(__file__).resolve().parents[4]
EDITION = sys.argv[1] if len(sys.argv) > 1 else "baseline"
OUT = Path(__file__).resolve().parent / EDITION
OUT.mkdir(parents=True, exist_ok=False)
SNAP = Path(tempfile.mkdtemp(prefix="luma-v121-host-"))

def utc():
    return dt.datetime.now(dt.timezone.utc).isoformat()

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

inputs = []
for folder in ("app/src/main/java", "scripts"):
    inputs.extend(p for p in (ROOT / folder).rglob("*") if p.is_file())
for name in ("app/build.gradle", ".github/workflows/puzzle-contracts.yml",
             "docs/validation/v1.6/legacy-content-before.sha256",
             "docs/validation/v1.14/audit-learning-rc1/LearningLayouts.java",
             "docs/research/2026-new-player/profile-anchors.json",
             "docs/validation/site-v1.21/staging/site-v121.js",
             "docs/validation/site-v1.21/staging/lab/ui-1-21/simulation-core.mjs",
             "docs/validation/site-v1.21/staging/lab/ui-1-21/simulation.mjs",
             "docs/validation/site-v1.21/staging/native-economy-fixture.json"):
    inputs.append(ROOT / name)
manifest = {}
for source in sorted(set(inputs)):
    name = str(source.relative_to(ROOT))
    target = SNAP / name
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, target)
    manifest[name] = sha(target)

source_record = {
    "recorded_utc": utc(),
    "repository_head": subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=ROOT, text=True).strip(),
    "working_tree_status": subprocess.check_output(["git", "status", "--short"], cwd=ROOT, text=True),
    "snapshot_directory": str(SNAP),
    "method": "Copy exact inputs once, then run unchanged existing tests in that isolated copy; do not read concurrently edited application inputs during compilation.",
    "sha256": manifest,
    "orchestrator_sha256": sha(Path(__file__).resolve()),
    "runtime": {
        "python": sys.version,
        "java": subprocess.run(["/opt/homebrew/opt/openjdk@17/bin/java", "-version"], capture_output=True, text=True).stderr.strip(),
    },
}
(OUT / "source-inputs.json").write_text(json.dumps(source_record, indent=2) + "\n")

jobs = []
def suite(label, script, *args, shell="bash"):
    jobs.append((label, [shell, "scripts/" + script, *map(str, args)]))
def dest(name):
    return OUT / name

suite("relay-all-u-states", "test-relay-v115.sh", dest("relay"), shell="sh")
suite("companion-all-u-identities", "test-companion-action-v119.sh", dest("companion-policy6"))
suite("focused-journey", "test-focused-journey-v120.sh", dest("focused-journey.json"))
suite("learning-all-n", "test-learning-v114.sh", dest("learning"), shell="sh")
suite("strategy-all-l-m-events", "test-strategy-v4.sh", dest("strategy"), shell="sh")
suite("engine", "test-engine.sh", shell="sh")
suite("sealed", "test-sealed.sh")
suite("legacy-progression", "test-progression-v3.sh", dest("progression-v3"), shell="sh")
suite("move-budget", "test-move-budget.sh")
suite("guardians-all-states", "test-guardian-v19.sh", dest("guardians"))
suite("guardian-store", "test-guardian-store-v19.sh", dest("guardian-store"))
suite("data", "test-data.sh")
suite("collection-events", "test-collection-events.sh")
suite("economy", "test-economy.sh")
suite("meta", "test-meta.sh")
suite("deliberate-play", "test-deliberate-play-v114.sh", shell="sh")
suite("learning-guide", "test-learning-guide.sh", shell="sh")
suite("companion-battle", "test-companion-battle-v119.sh")
suite("squad-store", "test-squad-store-v119.sh")
suite("companion-battle-geometry", "test-companion-battle-v119-geometry.sh")
suite("adventure-geometry", "test-adventure-v119-geometry.sh")
suite("expedition-model", "test-expedition-v118-model.sh", dest("expedition-model.json"))
suite("expedition-store", "test-expedition-v118-store.sh", dest("expedition-store.json"))
suite("companion-motion", "test-companion-motion-v118.sh", dest("companion-motion.json"))
suite("session-energy", "test-session-energy-v120.sh")
suite("menu", "test-menu-v120.sh")
suite("power-offers", "test-power-offers-v114.sh", dest("power-offers.json"), shell="sh")
suite("profile-summon", "test-profile-summon-v116.sh", dest("profile-summon.json"))
suite("objectives", "test-objective-models-v110.sh", dest("objective-models.json"))
suite("journey-rewards", "test-journey-rewards-v19.sh", dest("journey-rewards.json"))
suite("local-save", "test-local-save-v19.sh", dest("local-save"))
suite("rewards-cosmetics", "test-rewards-cosmetics-v17.sh")
suite("tutorial", "test-tutorial-v18.sh")
suite("first-light", "test-first-light-v113.sh")
suite("inventory-presentation", "test-inventory-presentation.sh")
suite("board-audit", "test-board-audit-v17.sh", dest("board-audit"))
suite("difficulty", "test-difficulty.sh")
suite("saga-map", "test-saga-map-v17.sh")
suite("store-appearance", "test-store-appearance-v111.sh")
suite("store-external-faults", "test-store-externals-v111.sh")
suite("store-init", "test-store-init-v111.sh")
suite("store-booster", "test-store-booster-v112.sh")
suite("billing-policy", "test-billing-policy.sh")
suite("wallet-recovery", "test-wallet-recovery.sh")
suite("energy-purchase", "test-energy-purchase-v120.sh")
suite("wallet-energy-transport", "test-wallet-energy-transport-v120.sh")
suite("home-store", "test-home-v115.sh")
suite("home-geometry-115", "test-home-v115-geometry.sh")
suite("home-geometry-116", "test-home-v116-geometry.sh")
suite("scene-transition", "test-scene-transition-v117.sh")
suite("root-scenes", "test-root-scenes-v117.sh")
suite("play-geometry", "test-play-scene-v117-geometry.sh")
suite("elemental-shells", "test-elemental-shell-v121.sh", dest("elemental-shells.json"))
suite("arcade-presentation", "test-arcade-presentation-v121.sh", dest("arcade-presentation.json"))
suite("home-kitchen", "test-home-kitchen-v121.sh", dest("home-kitchen.json"))
suite("economy-curve", "test-economy-curve-v121.sh", dest("economy-curve.json"))
suite("session-energy-121", "test-session-energy-v121.sh", dest("session-energy-121.json"))
suite("menu-121", "test-menu-v121.sh")
suite("summon-economy-simulation", "simulate-summon-economy-v121.sh", dest("summon-simulation"))
jobs.append(("site-and-simulation-121", ["node", "--test", "scripts/test-site-v121-runtime.mjs", "scripts/test-simulation-v121.mjs"]))

selected = set(sys.argv[2:])
if selected:
    jobs = [job for job in jobs if job[0] in selected]
env = dict(os.environ)
env["JAVA_HOME"] = "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
json_jars = list((Path.home() / ".gradle/caches/modules-2/files-2.1/org.json/json").rglob("json-*.jar"))
if json_jars:
    selected_jar = sorted(json_jars)[-1]
    env["LUMA_JSON_JAR"] = str(selected_jar)
    source_record["json_dependency"] = {"path": str(selected_jar), "sha256": sha(selected_jar)}
(OUT / "source-inputs.json").write_text(json.dumps(source_record, indent=2) + "\n")

def execute(job):
    label, command = job
    started_utc, started = utc(), time.perf_counter()
    log = OUT / (label + ".log")
    with log.open("w") as output:
        output.write("COMMAND: " + " ".join(command) + "\nSNAPSHOT: " + str(SNAP) + "\n")
        output.flush()
        try:
            run = subprocess.run(command, cwd=SNAP, env=env, stdout=output, stderr=subprocess.STDOUT, timeout=600)
            code = run.returncode
        except subprocess.TimeoutExpired:
            code = 124
            output.write("\nFAIL: 600-second bounded host-suite timeout\n")
    result = {"label": label, "command": command, "cwd": str(SNAP), "started_utc": started_utc,
              "finished_utc": utc(), "elapsed_seconds": round(time.perf_counter() - started, 3),
              "exit_code": code, "passed": code == 0, "log": str(log.relative_to(ROOT)),
              "log_sha256": sha(log)}
    (OUT / (label + ".result.json")).write_text(json.dumps(result, indent=2) + "\n")
    print(json.dumps({"label": label, "exit_code": code, "seconds": result["elapsed_seconds"]}), flush=True)
    return result

started_utc, started = utc(), time.perf_counter()
with concurrent.futures.ThreadPoolExecutor(max_workers=3) as pool:
    results = list(pool.map(execute, jobs))
summary = {"scope": "Existing pure host/model/transaction/geometry regressions only. No Android package, device execution, real account/provider/payment or human playthrough claim.",
           "source_inputs": str((OUT / "source-inputs.json").relative_to(ROOT)),
           "source_inputs_sha256": sha(OUT / "source-inputs.json"), "started_utc": started_utc,
           "finished_utc": utc(), "elapsed_seconds": round(time.perf_counter() - started, 3),
           "parallel_workers": 3, "suites": len(results), "passed": sum(r["passed"] for r in results),
           "failed": sum(not r["passed"] for r in results), "results": results}
(OUT / "run-summary.json").write_text(json.dumps(summary, indent=2) + "\n")
print(json.dumps({key: summary[key] for key in ("suites", "passed", "failed", "elapsed_seconds")}), flush=True)
sys.exit(0 if summary["failed"] == 0 else 1)
