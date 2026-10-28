# PremiumUi 1.16 — source review

2026-10-29. Scope: `app/src/main/java/com/androidhit/lumadrift/ui/PremiumUi.java` only. The research basis is [the reference audit](../../research/v1.16/reference-audit.md). No brand graphics or downloaded assets are included.

The drawing API keeps its existing signatures. Primary actions now use rounded pill faces with a solid lower edge, a subtle sheen and a pressed state. The overload accepting an accent allows the caller to distinguish action families instead of using one indigo rectangle everywhere. Secondary actions use outlined capsules, selection chips have a short lower edge, and active navigation uses an icon medallion with a separate label. Portrait stages use a light colored halo and a platform shadow. New helpers draw earned badge motifs and a summon capsule; they contain no ownership, reward, persistence, touch or random logic.

Additional APIs:

```java
void heroButton(Canvas canvas, RectF box, boolean pressed, int accent);
static int action(int accent);
void rarityHalo(Canvas canvas, RectF box, int accent, long time, boolean still);
void badge(Canvas canvas, float x, float y, float radius,
           int accent, int motif, boolean unlocked);
void capsule(Canvas canvas, float x, float y, float bodyWidth,
             int accent, long time, boolean still);
```

Badge radius includes ribbons. Motifs 0–5 represent sparkle, crown, leaf, key, moon and house. Capsule height including shadow and glints is approximately 1.3 × its body width. `still` freezes the decorative rotation or bob; neither animation controls an outcome.

Validation completed:

- Java 17 compilation against the Android 36 SDK passed with no diagnostics. This is an isolated source compilation, not a native interface test.
- `git diff --check` passed for this source and the research document.
- White primary-label contrast was computed using sRGB relative luminance. Default indigo is 6.181:1 on its face and 5.008:1 at the strongest combined sheen. Coral/teal/violet/amber faces are 5.721/5.684/6.489/5.981:1; the maximum combined sheen gives 4.764/4.657/5.185/4.833:1. These figures cover primary label backgrounds only, not every screen of the app.

Native screenshot review, integration, screen-size coverage and final binary validation remain with the release qualification. This review does not claim those checks have run.
