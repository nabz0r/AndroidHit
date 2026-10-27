# Candidate2 compact viewport finding — incomplete run

This run tested the actual installed Lab code10 candidate with SHA-256 `dd4bc97a7325e679f0af5c70f0b74ef769dffcef656051672cacdb24ea34e652`. Its label was chosen before execution. Despite the directory name, the raw result is **FAIL** and is not a final release pass.

The [native report](report.json) contains 232 checks. Tutorial 7 lessons, the 15 logical-point gate gesture, rotation/preview/skip/resume, double confirmation, native piece drags and menus passed. Journey levels 1 through 10 were completed through real native actions and visibly awarded 3 stars. The run stopped at the first manual left-gate action on level 11.

At that point the action preview card was anchored below the board at logical y587. Its confirmation button was below the compact 1080×1920 viewport, and the initial accessible window exposed no visible action controls. [The original failure screenshot](../../../screenshots/v1.9/ui-sdk-final-full100/failure.png) shows only the preview heading at the bottom edge. The board state was unchanged, as a preview should be, so the attempted action could not yet be recorded.

This is a visible reachability/friction defect in the action-preview placement, rather than a puzzle dead end. Host scrolling could theoretically reveal the offscreen controls; the harness was not changed to hide this first-use friction. The root implementation was asked to position the main action preview within the visible viewport before the next candidate.

The [host report](host-report.json) verifies the exact installed bytes, original preference-file restoration, original display size and rotation restoration. No profile progress, account, stock or currency was injected. All evidence remains preserved as a pre-fix finding.

An additional visual review of [the Rewards screen](../../../screenshots/v1.9/ui-sdk-final-full100/rewards-en.png) found locked trial labels truncating their unlock thresholds. Root was asked to use the existing compact `power_level` label while keeping the full explanation available. This did not cause the stopped Journey assertion.
