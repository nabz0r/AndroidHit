# Cognitive Lab 1.22: exposed feedback and actual companion actions

This is a versioned synthetic experiment, with **zero human participants**. It
preserves the historical 1.21 data under `public/lab/cognitive/data`. It does not
rename the earlier hypothetical learning boost as a shipped tutorial.

The new experiment runs **100 generated profiles × 30 repetitions × 7 campaign
conditions = 21,000 trajectories**, **84,000 assistance-free transfer probes**,
and **54,000 separate four-board chapter runs**. The seed is `20261030`.

## What actually changes

The frozen campaign control uses the same 1.21 Board, policy-6 budget, five
additional elemental layers from U11, economy, session-energy ledger, first-board
coach, earned Focus and visible dead-end warning. Control behavior reproduces the
historical totals exactly: 106,582 cleared levels, 1,160,522 decisions, 64,913
blocked interactions. Day-stop attribution is corrected in this new version.

The immediate-cause condition exposes the actual last blocking rule for 3.2
modeled seconds, matching the 1.22 native feedback duration. The agent recognizes
that displayed rule for the decision where it is visible. Initial knowledge and
learning rates stay identical. Recognition with certainty while displayed is an
explicit, uncalibrated comprehension assumption. After-repeat and fading variants
are experimental; they are not claimed to be shipped UI.

Information, execution and mixed teams use the real `CompanionBattleAction` and
all six native families: GUIDE, VISION, TRAIL, PULSE, FLOW and REWIND. The teams
have fully loaned access to isolate mechanical effects; ownership and acquisition
are excluded. Shared per-board energy, one-use skills, actual prepared effects
and resonance remain enforced. PULSE, FLOW and resonance exits are counted as
**automated exits**, separately from manual puzzle performance. Even the
information team can earn the native automated resonance.

The chapter is a distinct `T122` family, not campaign U901. It uses the actual
`TacticalChapterRun` and `TacticalChapterAction`: four boards, fixed loan team
Nova/Hoot/Orbit, charges shared across boards, no charge refill, no shells,
coins, campaign session-energy, purchases or return utility. One attempt is
observed per selected stage. Native capacity is six; four and eight and the
spending policies are explicit experimental controls. Route seeds are identical
across capacity and policy conditions.

## Results and limits

| Campaign condition | Mean levels cleared | Blocked interactions | Automated exits | Budget-exhausted attempts |
| --- | ---: | ---: | ---: | ---: |
| 1.21 control | 35.527 | 64,913 | 0 | 1,002 |
| Immediate cause | 35.668 | 63,668 | 0 | 1,002 |
| Cause after repetition | 35.528 | 64,833 | 0 | 1,005 |
| Fading cause | 35.653 | 64,150 | 0 | 1,001 |
| Information team | 32.252 | 33,551 | 18,919 | 493 |
| Execution team | 39.684 | 45,558 | 263,257 | 816 |
| Fading cause + mixed team | 37.530 | 43,637 | 182,977 | 810 |

These are different kinds of performance. Immediate feedback changes block rates
modestly and leaves the number of actual budget failures unchanged. It does not
remove the U43/U48 concentration: control U43 has 383 exhausted attempts out of
1,393 starts; immediate feedback has 388 out of 1,407. Control U48 has 235/882;
immediate feedback has 237/882. The sampled arrivals and number of attempts vary.

The execution team clears more levels partly through automated moves. The
information team reduces blocked interactions but clears fewer levels under this
skill-request and session model. Fewer blocked taps is therefore insufficient as
a single optimization objective. The current skill timing policy is declared and
bounded; it is not a validated model of human ability selection.

Without **any** cue, dead-end warning, Focus, coach or companion in the held-out
probe, completion is 9,483/12,000 for the control training regime and
9,503/12,000 for immediate feedback. It is 9,221/12,000 after execution-team
training and 9,250/12,000 after mixed-team training. Different campaign exposure
remains, so this compares training regimes, not an isolated causal learning
coefficient. Assisted progress cannot be presented as unaided mastery.

For the RELAYS chapter at native capacity six, all four stages complete in
1,785/3,000 manual runs, 1,701/3,000 early-use runs, and 1,859/3,000 runs reserving
talents for the last two boards. At capacity eight, early use completes
1,658/3,000 and reserved use 1,878/3,000. Extra available charge does not guarantee
better results under these fixed policies. These results motivate inspecting
specific role timing and budget consequences; they do not validate human fun or
justify a global economy adjustment.

The main trial has an eight-day horizon and a 120-decision daily administrative
cap. Unfinished attempts are `horizon_censored`, not failures. The 110-decision
attempt limit is separate administrative censoring. Formula-generated day-1 and
day-7 session indicators are retained for model sensitivity only and are not
human retention measurements. No rendered touch interaction, visual clarity,
accessibility comprehension, attachment or enjoyment is measured.

## Complete records and attribution correction

- `level-results.csv.gz`: every attempt, exact outcome, manual/automated exits,
  complete blocking-reason totals, repeated errors, cue exposures and families.
- `trajectories.csv.gz`: every profile/repetition/scenario; no sampling.
- `blocked-events.csv.gz`: **every** blocked interaction with its reason and
  exact visible-state/piece repetition key, not just selected traces.
- `day-results.csv.gz`: every configured day, decisions, actual stop cause and
  explicit action-cap flag. A nonterminal exhausted attempt cannot leave a stale
  final stop label.
- `chapter-results.csv.gz` and `chapter-stage-results.csv.gz`: separate chapter
  conditions, stage outcomes, manual/automated exits and charge before/after.
- `paired-comparisons.json`: profile-cluster paired comparisons, assistance-free
  transfer outcomes and separate tactical chapter conditions.
- `provenance.json`: source hashes and exact output hashes.

Historical 1.21 `stopReasons.budget_exhausted=266` was not 266 failed attempts.
There were 1,002 exhausted attempts and only 11 three-failure days. The old code
kept the budget label after a nonterminal failure when a later daily cap ended
the loop. In the new control, this is correctly separated into **3,278 daily-cap
stops and 11 repeated-budget-exhaustion stops**. The historical files are kept
unchanged, with this limitation documented rather than silently relabeled.

## Reproduce and verify

```sh
bash scripts/test-cognitive-lab-v122.sh
bash scripts/run-cognitive-lab-v122.sh --profiles 100 --repetitions 30 --seed 20261030
python3 scripts/package-cognitive-lab-v122.py
```

The dashboard offers the 1.22 report and the unchanged 1.21 archive separately.
Its bilingual labels identify shipped feedback and experimental interventions.
Packaging verifies source output hashes and historical file preservation; hosting
publication and Android device acceptance are separate checks.

## Corrected protocol metadata and candidate C binding

Before publication, the first1.22 run serialized the seven scenario identifiers
as a Java array identity in `config.scenarios`. The generator now exports the
actual JSON array. The corrected complete experiment was rerun with the same
seed, profiles, repetitions and final candidateC context.

[The exact rerun comparison](rerun-scenario-metadata-comparison.json) verifies
all six compressed CSV streams byte-for-byte and all behavioral JSON results
against the first run. Only the two scenario-list metadata fields and provenance
source/runtime/output hashes differ. All eight historical1.21 files remain
byte-for-byte unchanged.

[The before-correction archive](archives/before-scenario-list-correction/archive.json)
preserves original metadata, output hashes, original ZIP hash and the location
of the complete local before-correction copy. It is outside the public payload.
The superseded B/C source audits remain in that archive.

[The current candidateC source binding](candidate-c-source-binding.json) verifies
all51 recorded source hashes against the corrected run, including `LumaView.java`
as uncompiled UI context. The 50 executable/model sources and the context source
all match exactly. Native APK qualification remains separate evidence.

After a local JSON import, the dashboard exposes only exports from that import.
The ZIP and raw published-record links appear only for published datasets.
