# 1.11 RC2 — new K installation and wardrobe verification

**PASS: 83 fresh-install/tutorial assertions, 45 avatar assertions, and 36 language assertions**, in three separate native runs against the exact final Lab RC2 APK. **47 original full-frame PNGs** are retained. These checks qualify the recorded routes on this Android 36 arm64 16 KiB QA device, rather than every game path or physical device.

The APK is `releases/luma-drift-1.11.0-sandbox-rc2.apk`, SHA-256 `65bfbeab4f8b488e483a1622568540caf0315f72cf33d94876c75eafc9b4b642`, version `1.11.0-sandbox-rc2` / code 12. The existing native test APK is SHA-256 `7a0e2b6a48a19eaf6ea80a1338f420aea7253249c32172e0716203f47c16c61f`; the copied Candidate source is `5e29a15ec184114010a70255c0c29d7ae68a99baa691dc0265712860fee589db`. [Provenance](runner-provenance.json) records the exact arguments and source snapshot. This reviewer changed no product/native test source and built no APK.

## Separate from preliminary J / RC1

The [earlier J evidence](../fresh-install/README.md) remains unchanged. Its native checks passed, but direct inspection reproduced a starter glove label shortened to `Gloves · D…`; its attempted `run-as` disk observer also missed the initial state. Neither limitation is erased or relabelled as a success here.

Root corrected the label in RC2 by removing the redundant slot prefix before ` · ` and reserving two fitted lines when a multiword name is long. The clothing card and hit height changed from 88 to 90 design units. Full catalog names remain in accessible descriptions and list/offers. The [additive compatibility report](../source-compatibility.json) keeps the original RC1/88-unit review and adds RC2 hashes and geometry. All 41 previously fingerprinted engine/catalog/onboarding/support sources still match; among the previously changed Java sources, only `LumaView.java` changed from the RC1 review to RC2.

K was separately created; no J userdata was copied or reset. The previous guest and the new guest both remain on their own AVDs.

## New-device preconditions and persisted starter

`Luma-Release111-K`, port **5586**, uses the already installed Android 36 Google APIs 16 KiB arm64 image, with 2 GiB RAM, 4 CPU cores, `1080×2340` display and density 440. [Before-install observations](absence-before-any-install.json) show Lab, production and the test package all absent before any installation. The helper independently verifies K's exact AVD name, page size and package absence, then installs and hashes the exact bytes. Production remains absent after the tutorial run.

The corrected [observer](observe-prefs.py) starts **before installation and launch**, using only read-only `adb exec-out su 0 cat` of the actual persisted XML. Its [first initialized sample](first-initialized.json) and [original XML](first-initialized-prefs.xml) directly show:

- 180 LumaCoins, 3 Hint, 3 Vision, 2 Flow;
- Nova and Lumina selected and owned;
- `initialized=true`, fresh-profile flag true;
- no welcome receipt and no tutorial-state entry yet.

The first durable XML was observed at `2026-10-28T11:42:33.167617Z`, 185 seconds after the observer started. Its SHA-256 is `9b93303b29f778136d14a04e18d7384668c6fabb6b602920769bbe9724c5812a`. This is a direct persisted-file observation, **not a cold-process relaunch of the starter state**. The 644 unavailable/incomplete reads occurred while waiting for the new package/XML to exist; 36 distinct persisted states were subsequently captured. The [observer report](persisted-observer-report.json) confirms the exact starter and the final post-gift 280/5/3/2 state. It was stopped before the follow-ups; its files are frozen.

## Three real SDK runs

| Run | Assertions | Original PNGs | State retained |
| --- | ---: | ---: | --- |
| [First installation / tutorial](host-report.json) | 83 PASS | 15 | Seven lessons completed, welcome receipt granted once; replay paused at lesson 1 |
| [Avatar](avatar-followup/host-report.json) | 45 PASS | 7 | Authorized cosmetic goal `cosmetic:hat_cupid` pinned; no item grant |
| [Six languages](languages-followup/host-report.json) | 36 PASS | 25 | English restored through settings; free tutorial actually advanced to `T1\|2\|0\|SKIPPED\|` |

The tutorial case checks optional arrival, skip/resume, exact phase/history restoration, real SDK rotation, gate dragging/double confirmation, all seven lessons, no practice resource spending, +100 LumaCoins/+2 Sparks once, and no repeat gift. Several piece steps use accessibility clicks; this is not a claim that every lesson was completed with a drag. Rotation settings are restored; the first-install guest is retained instead of pretending to restore a previous profile.

The avatar case previews and closes an unowned Cupid hat, then explicitly pins it, checks appearance/equipment tabs, rotates, and verifies visible footer targets. Its sole game/meta preference change is the authorized goal. The language case uses actual settings for EN/FR/DE/ES/PT/IT, opens maps/objectives/shop without claims, and performs free tutorial steps. Its sole net game/meta change is the actual tutorial-state advance. No profile restore, seed, purchase, campaign clear, account, ad or payment operation is used. The [follow-up host runner](run-followup.py) invokes only these existing cases and reads evidence.

## Native visual result

The original English [wardrobe frame](../../../screenshots/v1.11/fresh-install-rc2/avatar-followup/avatar-slots-en.png) and the localized [English](../../../screenshots/v1.11/fresh-install-rc2/languages-followup/avatar-en.png) / [French](../../../screenshots/v1.11/fresh-install-rc2/languages-followup/avatar-fr.png) frames were inspected. **Dew / Rosée is fully visible under each of the four clothing slots, including the gloves**. The companion, its four clothes and three equipment slots remain visible. The observed EN/FR gear labels are also complete. The full catalog names remain in the original accessible snapshots.

This establishes the starter-label fix on the observed English/French device frames. It does not claim that all 144 catalog-language combinations fit in full, that every larger-font setting was exercised, or that draw-call assertions alone prove readability. Longer components can still use the intentional ellipsis, with full names available in the accessible/list/dialogue descriptions.

## Frozen originals and scope

The first-install helper originally writes into `ui-bugs/ui-sdk-fresh-k-rc2`; [relocation provenance](native-evidence-relocation.json) records the byte-exact move of all 37 native outputs into this requested directory and its screenshot counterpart. The first 15 PNGs have a [manifest](screenshot-manifest.json); the final [evidence manifest](evidence-manifest.json) covers all 47 original frames and all retained reports/source. Portrait frames are `1080×2340`; the original landscape frame is `2340×1080`. No screenshots were edited or resized for these files.

The before/after crash buffers are identical, including both follow-ups. No new native crash is observed. Emulator stdout has always gone to `/tmp/luma-v111-fresh-k-emulator.log`; only a frozen excerpt is in this directory. The observer is stopped. The [device shutdown receipt](../qa-devices-shutdown.json) records subsequent cleanup of **only J5584 and K5586**, preserving their AVD directories and guests. Ports 5580 and 5582 are untouched.

This folder does not declare global release, Play publication, monetization or Hosting status. Parent-owned package and other native acceptance evidence qualify those scopes separately.
