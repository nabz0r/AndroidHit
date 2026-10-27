# Final 1.7 native SDK UI checks

The tested Lab APK is **com.androidhit.lumadrift.sandbox**, 1.7.0-sandbox / code8:

```
d7fea345c1f33cc8e114688ee06d8b60ed1278cc29eeb328c677c6c1858ba049
```

The actual runtime is Android API36, arm64-v8a, **16KB kernel page size**, emulator-5580. No profile, currency, stock, progress, account or clock was injected. The SDK-only framework test runner controls the delivered application's real accessibility nodes and native dialogs. Every PNG is an original SDK screenshot, without editing or composition.

| Final run | Observed result | Evidence |
|---|---|---|
| Menus / map / guest services / earned capsule / FR–EN | **38 checks passed**, 32 unique PNGs | [Host and exact APK binding](ui-sdk-final-menus2/host-report.json), [native assertions](ui-sdk-final-menus2/report.json) |
| Actual first13 Journey clears / challenge / hint / recovery / landscape | **34 checks passed**, 9 PNGs | [Host, rotation and exact APK binding](ui-sdk-final-gameplay/host-report.json), [native assertions](ui-sdk-final-gameplay/report.json) |

Both runs stop the app afterward and report **`restored: true`**, verified against the complete original preference file set and byte contents. The landscape run also reports **`rotation_settings_restored: true`**. Its original settings were `user_rotation=0` and `accelerometer_rotation=1`. The private backup files remain outside the repository.

The menu run observes a real **+40 earned daily gift**, then **+0** on a repeated collection action; four equipped starter slots, their native previews and free equip; unowned previews cancelled without spending; clearly separate earned and closed premium shop sections; a closed guest account vault with no enabled draw; the guest social explanation; complete per-item capsule odds before one actual150-coin earned spend and the correct result/duplicate compensation; map jumps1/13/100/101/1000 and real accessibility scrolling; FR/EN screenshots. No Google chooser or production service request is opened by these guest checks.

The gameplay run imports and genuinely clears the public J13 challenge with no coins awarded or stock spent. It then genuinely clears Journey1–12 through visible piece actions to unlock normal J13, rather than seeding progress. On fresh J13, `move0 → Undo → move0 → Undo` restores all10 pieces with only9 moves left; clicking Spark cannot open a consuming confirmation or spend a charge. A free restart repairs that attempt. After four certified exits, the normal hint targets the exact completion-preserving tile4, consumes one stock charge, consumes no move, and a second tap while active consumes no further charge. The legal wrong branch0,1,2,3,8 produces the explicit gate dead-end explanation; undo/restart recovers, and normal J13 clears within its allowance. Compact landscape checks show node101 below the pinned header and a physical touch target at least48dp high before restoring orientation.

These are direct installed UI observations for the named APK, not a claim that all1000 levels were played on the emulator. Full-catalog engine certificates and independent simulations are separate release evidence. Real Google authentication, real friend exchange, real ads, billing and paid draws are outside this guest UI scope.

The first `ui-sdk-final-menus` attempt failed because its test oracle still expected “Weekly quests”, while the frozen candidate uses “Weekly Voyage”. It is not counted as a passed run; its backup restoration succeeded. The corrected run is **ui-sdk-final-menus2**. Earlier [pilot evidence](SDK_UI_PILOT.md) binds to a different APK and remains explicitly historical.

The [identity controller regression suite](identity-final.txt) separately passes **24 scenarios / 289 assertions** against deterministic platform/provider doubles, including server-first deletion, failure without Firebase-only fallback, a new UID surviving an old deletion callback, and captured-owner cache cleanup after Firebase Auth has already removed the account. This is application transition testing, not a real provider login/deletion qualification.


## Frozen production startup and audio

A separate SDK-only observer ran against **com.androidhit.lumadrift**, version **1.7.0-rc1 / code8**, with installed APK SHA-256:

```
7eeaa816853533f441acfc73f4ad3f6af0fb2cbdd6fd0eae1fda3f64c388a2bf
```

The [production host report](ui-sdk-final-production/host-report.json) and [native report](ui-sdk-final-production/report.json) pass **7 checks**, with **1 original native PNG**. The actual existing guest profile was preserved: the capture shows its 180 earned coins and previously selected friend challenge; no reset, progress or currency was injected. It is startup evidence, not a marketing claim about a new player's initial challenge state.

The [AudioFlinger observation](ui-sdk-final-production/audio-runtime.json) identifies the actual application process PID6917 / UID10216 and its track150. The same active track advanced from44981 to66973 mixed frames (**+21992**); SDK background navigation left that track inactive, and a normal SDK activity resume made it active again. Raw AudioFlinger dumps accompany the report. This validates actual digital music playback and lifecycle pause/resume on the emulator, not physical speaker/headphone audition.

The wrapper reports **`restored: true`** against the complete original production preference set and hashes, leaves the game stopped, and performs no gameplay, menu, account, advertising or payment action. The [test-build integrity record](production-sdk-build-integrity.json) confirms that building the separate test APK changed neither frozen game APK. Production and Lab evidence retain their separate package and hash bindings.
