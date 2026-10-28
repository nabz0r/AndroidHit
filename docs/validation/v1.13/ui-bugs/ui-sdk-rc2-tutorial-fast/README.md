# RC2 native quick-resume regression

Frozen **1.13.0-sandbox-rc2/code14** passes **90 native checks** with **15 original frames**. The blocked-move lesson is paused and resumed in **766 ms**, and the gate preview in **1145 ms**, each measured from the real SDK ACTION_UP and strictly within the original 2400 ms feedback deadline. The exact observed practice state is retained across both quick resumes and real rotation. All seven lessons and their earned gift/replay behavior complete without a new native crash.

The earlier RC1 crash remains preserved in ui-sdk-final1-full13. RC2's cleared transient feedback deadline and guarded empty key are tested here by the quick path that triggered it. Original preference bytes and rotation are restored and isolated hometest removed. [Host report](host-report.json), [native checks](prepare/report.json), [timed native actions](prepare/actions.json), [exact pre-run source](source-snapshot.json).
