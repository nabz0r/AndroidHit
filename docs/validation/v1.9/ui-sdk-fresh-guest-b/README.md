# Genuine first installation — QA device B

[Host report](host-report.json): **PASS**, with 85 native SDK checks and 17 original PNG captures. The exact installed Lab APK SHA-256 is `a16fbc03a75455805b7f3f491a8fc26cddc4e33637ec0b8bc1668700e83de36f`.

Before installation, the explicit owned emulator5584 target had **no Lab package**. The wrapper refuses an existing package and never clears a profile to manufacture a first arrival. The older production application's APK was recorded before the Lab installation and verified byte-identical afterward; no production activity, Google account, ad or payment was opened.

The actual first arrival offered optional interactive practice. The run used the real skip/resume controls and completed all seven lessons with real board actions. It exercised the 15 logical-point gate gesture, preview rotation, skip/resume of the gate preview and double confirmation yielding one opening. Completion granted exactly 100 earned LumaCoins and 2 hint charges, with one receipt. Replay and repeated collection granted no second reward. Wrong-direction and correct-direction SDK drags, plus a real campaign drag, were checked.

This device used API 36, arm64 and actual 4 KiB pages. The separate primary QA device provides 16 KiB Journey coverage; this report does not claim a 4 KiB device has 16 KiB pages. Original rotation settings were verified restored. The new Lab guest profile was retained and stopped at the end of this run; it was not replaced with an older save.

All PNGs are original `UiAutomation.takeScreenshot()` encodings. [The exact runner source](runner-source.java.txt) used for the recorded test is retained. Preference reads inside that target package were assertions only, without mutation, reflection or app hooks. The report does not validate Google authentication, social services, ad delivery, purchases or human retention.

The original capture count belongs to this run; redundant superseded-candidate PNGs are omitted from the Git payload with their hashes retained in the [curation manifest](../pilot-screenshot-curation.json). Selected FINAL5 native captures are separate.
