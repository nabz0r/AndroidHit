#!/usr/bin/env python3
"""Read-only source audit, NOT native Board execution or synthetic-player games.

Replays all frozen certificates and independently enumerates the first 100 U
boards using the obstruction/XOR contract in Board and RelaySolver. Shells,
UI, budgets, assistance, player learning and emotions are deliberately absent.
"""

import functools
import hashlib
import json
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[4]
SOURCE = "app/src/main/java/com/androidhit/lumadrift/engine/RelayLayouts.java"
DR, DC = (-1, 0, 1, 0), (0, 1, 0, -1)


def inspect(number, row):
    fields = row.split("|")
    size, initial_gates, minimum, margin = map(int, fields[:4])
    pieces = [tuple(map(int, item.split(","))) for item in fields[4].split(";")]
    walls = [tuple(map(int, item.split(","))) for item in fields[5].split(";") if item]
    certificate = list(map(int, fields[6].split(",")))
    full = (1 << len(pieces)) - 1

    def on_ray(tile, row, col):
        tr, tc, direction, _ = tile
        if DR[direction] == 0:
            return row == tr and (col - tc) * DC[direction] > 0
        return col == tc and (row - tr) * DR[direction] > 0

    obstructions = []
    for tile_id, tile in enumerate(pieces):
        mask = sum(1 << other_id for other_id, other in enumerate(pieces)
                   if other_id != tile_id and on_ray(tile, other[0], other[1]))
        for wr, wc, key in walls:
            if on_ray(tile, wr, wc):
                mask |= 1 << key
        obstructions.append(mask)

    def legal(mask, gates):
        return [i for i, tile in enumerate(pieces)
                if mask & (1 << i) and not (mask & obstructions[i])
                and gates & (1 << tile[2])]

    @functools.lru_cache(None)
    def solvable(mask, gates):
        return not mask or any(solvable(mask ^ (1 << i), gates ^ pieces[i][3])
                               for i in legal(mask, gates))

    def witness(prefix, tile_id):
        tile = pieces[tile_id]
        return {"prefixIdsZeroBased": prefix, "choiceIdZeroBased": tile_id,
                "rowOneBased": tile[0] + 1, "columnOneBased": tile[1] + 1,
                "exitDirection": ("UP", "RIGHT", "DOWN", "LEFT")[tile[2]],
                "switchMask": tile[3]}

    assert len(certificate) == minimum == len(pieces), number
    mask, gates, certificate_traps = full, initial_gates, []
    for step, tile_id in enumerate(certificate):
        choices = legal(mask, gates)
        assert tile_id in choices, (number, step, tile_id)
        for candidate in choices:
            if not solvable(mask ^ (1 << candidate), gates ^ pieces[candidate][3]):
                certificate_traps.append(witness(certificate[:step], candidate))
        mask ^= 1 << tile_id
        gates ^= pieces[tile_id][3]
    assert mask == 0, number

    result = {"level": number, "size": size, "pieces": len(pieces),
              "correctionMargin": margin, "lesson": fields[14],
              "initialGateMask": initial_gates,
              "relayCount": sum(bool(tile[3]) for tile in pieces),
              "multiGateRelayCount": sum(bin(tile[3]).count("1") > 1 for tile in pieces),
              "barrierCount": len(walls), "certificateReplayed": True,
              "initialLegalIdsZeroBased": legal(full, initial_gates),
              "unsafeChoicesAlongCertificate": len(certificate_traps),
              "firstUnsafeAlongCertificate": certificate_traps[0] if certificate_traps else None,
              "authoredDependencyDepth": int(fields[10]),
              "authoredDecisionPoints": int(fields[7])}
    if number <= 100:
        pending = [(full, initial_gates, [])]
        seen, unsafe_edges, unsafe_states = set(), 0, 0
        while pending:
            state_mask, state_gates, prefix = pending.pop()
            state = (state_mask, state_gates)
            if state in seen:
                continue
            seen.add(state)
            bad = False
            for candidate in legal(state_mask, state_gates):
                after = (state_mask ^ (1 << candidate), state_gates ^ pieces[candidate][3])
                if not solvable(*after):
                    unsafe_edges += 1
                    bad = True
                pending.append((after[0], after[1], prefix + [candidate]))
            unsafe_states += int(bad)
        result.update({"allReachableStatesEnumerated": len(seen),
                       "reachableStatesWithUnsafeLegalExit": unsafe_states,
                       "unsafeLegalEdgesFromReachableStates": unsafe_edges})
    return result


def main():
    payload = (ROOT / SOURCE).read_bytes()
    rows = re.findall(r'^\s*"([^"\n]+)"[,;]?$', payload.decode(), re.M)
    assert len(rows) == 1000
    levels = [inspect(i + 1, row) for i, row in enumerate(rows)]
    first_unsafe = next(level["level"] for level in levels[:100]
                        if level["unsafeLegalEdgesFromReachableStates"])
    output = {
        "scope": "Independent static source replay and exact small-state enumeration; no Java/native execution, player games, UI perception or human evidence",
        "source": SOURCE,
        "sourceSha256": hashlib.sha256(payload).hexdigest(),
        "allCertificateCount": len(levels),
        "allReachableEnumerationLevels": [1, 100],
        "firstLevelWithUnsafeLegalExit": first_unsafe,
        "allCorrectionMargins": sorted(set(level["correctionMargin"] for level in levels)),
        "firstMultiGateRelayLevel": next(level["level"] for level in levels if level["multiGateRelayCount"]),
        "levels": levels,
    }
    destination = Path(__file__).with_name("catalog-audit.json")
    destination.write_text(json.dumps(output, indent=2) + "\n")
    print(json.dumps({key: value for key, value in output.items() if key != "levels"}, indent=2))


if __name__ == "__main__":
    main()
