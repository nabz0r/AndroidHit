#!/usr/bin/env python3
"""Validate this minimal aggregate corpus; no network or personal data access."""

import hashlib
import json
import re
from datetime import date
from pathlib import Path

ROOT = Path(__file__).resolve().parent
CUTOFF = date(2026, 10, 27)
checks = 0


def require(condition, message):
    global checks
    checks += 1
    if not condition:
        raise AssertionError(message)


sources = json.loads((ROOT / "sources.json").read_text())
profiles = json.loads((ROOT / "profile-anchors.json").read_text())
observations = json.loads((ROOT / "aggregate-observations.json").read_text())
manifest = json.loads((ROOT / "retrieval-manifest.json").read_text())
source_ids, observation_ids = set(), set()
for source in sources["sources"]:
    require(source["id"] not in source_ids, "duplicate source")
    source_ids.add(source["id"])
    require(source["url"].startswith("https://"), "source URL")
    require(source["sample_n"] > 0, "sample size")
    published = source.get("published", source.get("published_month") + "-01" if source.get("published_month") else "")
    require(date.fromisoformat(published) <= CUTOFF, "future publication")
    require(published.startswith("2026-"), "publication outside selected year")
    require(len(source["short_quote"].split()) <= 25, "quote too long")
    require(len(source["limitations"]) >= 2, "missing scope limits")
    require(bool(source["collection"]), "missing fieldwork dates")
    require(bool(source["access"]), "missing access provenance")
    require(bool(source["licence"]), "missing copyright/re-use note")
    for fact in source["observations"]:
        require(fact["id"] not in observation_ids, "duplicate fact")
        observation_ids.add(fact["id"])
        require(bool(fact["base"]), "missing denominator/scope")
        if fact["unit"] == "percent":
            values = fact["value"].values() if isinstance(fact["value"], dict) else [fact["value"]]
            for value in values:
                require(0 <= value <= 100, "percentage out of range")

require(len(source_ids) == 6, "expected six primary sources")
require(len(profiles["profiles"]) == 6, "expected six stress profiles")
require(profiles["population_weights"] is None, "unsupported population weighting")
profile_ids = set()
for profile in profiles["profiles"]:
    require(profile["id"] not in profile_ids, "duplicate profile")
    profile_ids.add(profile["id"])
    require(all(anchor in observation_ids for anchor in profile["anchors"]), "unknown research anchor")
    require(profile["assumed_parameters"]["no_purchase"], "simulation requires payment")
    require(bool(profile["sensitivity"]), "missing sensitivity analysis")
    require(not any(k in profile["assumed_parameters"] for k in ("age", "generation", "conversion", "retention", "d1", "d7")), "demographic or retention parameter")

expected = [{"source_id": source["id"], "url": source["url"], "published": source.get("published", source.get("published_month")), "collection": source["collection"], "sample_n": source["sample_n"], "observations": source["observations"]} for source in sources["sources"]]
require(observations["source_aggregates"] == expected, "aggregate extraction drift")
require(not observations["contains_personal_records"], "personal data retained")
require(not observations["source_raw_bytes_retained"], "raw-report claim")
for file in manifest["files"]:
    path = ROOT / file["path"]
    require(path.is_file(), "manifest path missing")
    require(hashlib.sha256(path.read_bytes()).hexdigest() == file["sha256"], "manifest hash drift")
require(not manifest["hashes_represent_origin_responses"], "misleading source-byte hash")
require("403" in next(s["access"] for s in sources["sources"] if s["id"] == "AARP26"), "AARP access limitation missing")
for text in re.findall(r"\]\(([^)#]+)(?:#[^)]+)?\)", (ROOT / "README.md").read_text()):
    if not text.startswith("https://"):
        require((ROOT / text).exists(), "local documentation link missing")

report = {"passed": True, "checks": checks, "sources": len(source_ids), "aggregate_observations": len(observation_ids), "behavioural_profiles": len(profile_ids), "cutoff": str(CUTOFF), "scope": "Static integrity and scope contracts only; not human-behaviour validation."}
(ROOT / "validation.json").write_text(json.dumps(report, indent=2) + "\n")
print(json.dumps(report))
