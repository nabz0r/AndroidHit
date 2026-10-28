#!/usr/bin/env python3
"""Describe the frozen RC2 content, without treating structural scores as player results."""
import csv
import hashlib
import json
import statistics
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs/validation/v1.14/audit-learning"
LAYOUT = ROOT / "app/src/main/java/com/androidhit/lumadrift/engine/LearningLayouts.java"
RELEASE = "1.14.0-sandbox-rc2"


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read_rows(path):
    result = []
    for row in csv.DictReader(path.open()):
        result.append({k: v if k in {"family", "lesson"} else int(v) for k, v in row.items()})
    return result


def window(rows):
    fields = ("size", "pieces", "minimum_actions", "required_gate_opens", "switches", "keys",
              "decision_points", "dependency_depth", "structural_score", "margin")
    return {
        "first": rows[0]["number"], "last": rows[-1]["number"], "count": len(rows),
        "medians": {field: statistics.median(row[field] for row in rows) for field in fields},
        "ranges": {field: [min(row[field] for row in rows), max(row[field] for row in rows)] for field in fields},
        "normal": sum(row["difficulty"] == 0 for row in rows),
        "hard": sum(row["difficulty"] == 1 for row in rows),
        "expert": sum(row["difficulty"] == 2 for row in rows),
        "recovery": sum(row["beat"] == 3 for row in rows),
        "initialFreeExit": sum(row["initial_legal_exits"] > 0 for row in rows),
    }


def main():
    rows = read_rows(OUT / "audit-learning-authoring.csv")
    old = read_rows(ROOT / "docs/validation/v1.14/audit-learning-rc1/audit-learning-authoring.csv")
    old_by_source = {r["source"]: r for r in old if r["source"]}
    assert len(rows) == 1000 and rows[:30] == old[:30]
    changes = []
    for row in rows[30:]:
        previous = old_by_source[row["source"]]
        assert row["initial_gates"] | previous["initial_gates"] == row["initial_gates"]
        if row["initial_gates"] != previous["initial_gates"]:
            changes.append({"number": row["number"], "source": row["source"],
                            "oldInitial": previous["initial_gates"], "newInitial": row["initial_gates"]})
    jumps = []
    for before, after in zip(rows[29:], rows[30:]):
        jumps.append({"from": before["number"], "to": after["number"],
                      **{field: after[field] - before[field] for field in
                         ("structural_score", "pieces", "minimum_actions", "required_gate_opens")}})
    maximum = {field: max(jump[field] for jump in jumps) for field in
               ("structural_score", "pieces", "minimum_actions", "required_gate_opens")}
    assert all(maximum[key] <= limit for key, limit in
               (("structural_score", 35), ("pieces", 2), ("minimum_actions", 4), ("required_gate_opens", 2)))
    run = maximum_run = 0
    for row in rows:
        run = run + 1 if row["difficulty"] else 0
        maximum_run = max(maximum_run, run)
    assert maximum_run <= 4
    episodes = [window(rows[first - 1:first + 9]) for first in range(31, 1001, 10)]
    assert all(x["normal"] >= 3 and x["initialFreeExit"] == 3 for x in episodes)
    windows100 = [window(rows[i:i + 100]) for i in range(0, 1000, 100)]
    windows200 = [window(rows[i:i + 200]) for i in range(0, 1000, 200)]
    assert all(a["medians"]["structural_score"] <= b["medians"]["structural_score"]
               for a, b in zip(windows200, windows200[1:]))
    report = {
        "release": RELEASE, "learningLayoutsSha256": sha(LAYOUT),
        "scope": "RC2 content composition and structural metrics; not human learning, win rates, engagement, native testing or binary qualification",
        "first30UnchangedFromRc1": True, "preservedPhysicalBases": 970,
        "initialGateStatesReauthoredByOpeningOnly": len(changes),
        "initialStatesWithFreeExit": sum(row["initial_legal_exits"] > 0 for row in rows),
        "normalBoards": sum(row["difficulty"] == 0 for row in rows),
        "hardBoards": sum(row["difficulty"] == 1 for row in rows),
        "expertBoards": sum(row["difficulty"] == 2 for row in rows),
        "firstHard": next(row["number"] for row in rows if row["difficulty"] == 1),
        "firstExpert": next(row["number"] for row in rows if row["difficulty"] == 2),
        "maximumHardExpertRun": maximum_run, "maximumPositiveJumps": maximum,
        "largestPositiveScoreSteps": sorted(jumps, key=lambda x: -x["structural_score"])[:12],
        "windows100": windows100, "windows200": windows200, "episodes": episodes,
        "openingOnlyChanges": changes,
    }
    (OUT / "audit-curve.json").write_text(json.dumps(report, indent=2) + "\n")
    branches = list(csv.DictReader((OUT / "audit-learning-branches.csv").open()))
    onset = {"release": RELEASE, "learningLayoutsSha256": sha(LAYOUT),
             "scope": "Existence of a finite legal path exceeding the actual initial N budget; no blocked taps, powers, Undo or extensions; not player failure rates",
             "boards": len(branches), "policies": {}}
    for field in ("maximum_all_legal", "maximum_immediately_useful", "maximum_exits_first"):
        over = [r for r in branches if int(r[field]) > int(r["goal"])]
        onset["policies"][field] = {"boardsWithSomePathOverBudget": len(over),
                                    "firstLevel": int(over[0]["level"]) if over else None,
                                    "first30BoardsWithSomePathOverBudget": sum(int(r["level"]) <= 30 for r in over)}
    (OUT / "audit-challenge-onset.json").write_text(json.dumps(onset, indent=2) + "\n")
    files = [str(path.relative_to(ROOT)) for path in
             sorted((ROOT / "app/src/main/java/com/androidhit/lumadrift/engine").glob("*.java"))]
    files += ["scripts/" + name for name in ("GenerateLearningV114.java", "LearningV114Test.java",
              "AuditJourneyV114.java", "ProgressionAnalysis.java", "GenerateStrategyV4.java",
              "generate-learning-v114.sh", "test-learning-v114.sh", "audit-learning-curve-v114.py")]
    identity = {"schemaVersion": 2, "release": RELEASE,
                "scope": "RC2 offline authoring and content qualification inputs; native RC1 evidence remains archived and does not qualify this corpus",
                "files": [{"path": p, "sha256": sha(ROOT / p), "bytes": (ROOT / p).stat().st_size} for p in files]}
    (OUT / "audit-source.json").write_text(json.dumps(identity, indent=2) + "\n")
    lines = ["# Courbe N — revue du corpus RC2", "",
             f"Corpus `{sha(LAYOUT)}`. Les rapports RC1 et leur source complète restent dans [audit-learning-rc1](../audit-learning-rc1/). Aucune preuve native RC1 n'est présentée comme une validation RC2.", "",
             "La difficulté croît par paliers et par fréquence des défis. Elle n'est pas monotone par niveau : des plateaux Normal restent intercalés jusqu'à N1000 et chaque pic d'épisode est suivi d'une récupération mesurée. Les étiquettes correspondent aux scores recalculés après les nouvelles portes initiales.", "",
             "| Fenêtre | Taille médiane | Pièces médianes | Minimum médian | Switchs médians | Clés médianes | Décisions médianes | Score médian | Hard + Expert | Récupérations |",
             "|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|"]
    for w in windows100:
        m = w["medians"]
        lines.append(f"| {w['first']}–{w['last']} | {m['size']:g} | {m['pieces']:g} | {m['minimum_actions']:g} | {m['switches']:g} | {m['keys']:g} | {m['decision_points']:g} | {m['structural_score']:g} | {w['hard'] + w['expert']} | {w['recovery']} |")
    scores200 = " → ".join(f"{w['medians']['structural_score']:g}" for w in windows200)
    lines += ["", f"Médianes de score par 200 : **{scores200}**. Le creux RC1 N401–600 est supprimé. Une baisse locale de 79,5 à 73,5 subsiste entre les fenêtres N401–500 et N501–600 ; les deux ont 50 défis, et la médiane des décisions reste 11,5. Ce n'est donc pas une promesse de croissance pour chaque fenêtre de 100.", "",
              "| Paliers après introduction | Hard/Expert par épisode de 10 | Normaux minimum par épisode |",
              "|---|---:|---:|", "| 31–40 | 0 | 10 |", "| 41–60 | 1 | 9 |", "| 61–100 | 2 | 8 |", "| 101–200 | 3 | 7 |", "| 201–400 | 4 | 6 |", "| 401–600 | 5 | 5 |", "| 601–800 | 6 | 4 |", "| 801–1000 | 7 | 3 |", "",
              f"Le premier Hard est N{report['firstHard']} ; le premier Expert N{report['firstExpert']}, après les introductions et l'accumulation de pratique. Les 38 Experts sont tous après N400. La série maximale Hard/Expert est **{maximum_run}**, contre 153 en RC1. Chacun des 97 épisodes garde trois états initiaux avec une sortie libre ; les 96 épisodes contenant un Hard terminent par un Normal dont le score baisse d'au moins 20 % après le pic.", "",
              "Le pont N30→N31 passe de 10 à 12 pièces, de 10 à 13 actions minimales et de 38 à 42 points. N31 offre deux sorties immédiates et exige une ouverture manuelle ; N32, N33 et N34 exigent respectivement deux, trois et quatre ouvertures. C'est une hausse graduelle de charge avec les mécaniques déjà introduites, pas une nouvelle règle. Après N30, les plus grands bonds positifs sont **35 points, deux pièces, quatre actions minimales et deux ouvertures**. Ces bornes sont vérifiées sur chaque transition.", "",
              f"**{len(changes)} des 970 bases** ont des portes initiales supplémentaires ouvertes. Taille, positions, flèches, switchs et associations clé/sceau restent exactement ceux de leur source L ; l'état initial N est un sur-ensemble des portes ouvertes L. Chaque état est atteint depuis L par ces seules ouvertures, puis son minimum et ses métriques sont recalculés. Les 30 introductions sont inchangées. Au total, {report['initialStatesWithFreeExit']}/1000 plateaux commencent avec une sortie libre.", "",
              "Le score combine les bifurcations de coût le long d'un certificat optimal, les ouvertures nécessaires, les clés, la pénalité maximale d'un choix et la profondeur des blocages. Il ne mesure pas la compréhension humaine ni le taux de victoire. Les budgets restent réels : minimum +5 jusqu'à N16, +4 jusqu'à N100, puis +3 ; les erreurs productives peuvent donc faire perdre. La progression donne davantage de décisions et de défis tardifs, avec une densité physique plafonnée, plutôt que de grossir indéfiniment les grilles.", "",
              "Les [contrats indépendants](audit-learning-contracts.json), les [97 épisodes et fenêtres détaillées](audit-curve.json) et les [branches de budget](audit-learning-branches.csv) décrivent ce corpus. Ces preuves ne remplacent pas un premier essai humain ni la qualification native RC2.", ""]
    (OUT / "curve-review.md").write_text("\n".join(lines))
    print(json.dumps({k: report[k] for k in ("release", "learningLayoutsSha256", "initialGateStatesReauthoredByOpeningOnly",
                     "initialStatesWithFreeExit", "maximumHardExpertRun", "firstHard", "firstExpert", "maximumPositiveJumps")}, indent=2))


if __name__ == "__main__":
    main()
