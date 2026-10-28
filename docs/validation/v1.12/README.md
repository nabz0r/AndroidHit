# 1.12 qualification

Scope: native shop/Home/item-page presentation and the earned-power purchase transaction. Production 1.12.0-rc2 and isolated Lab 1.12.0-sandbox-rc2, code 13. Final Lab installed qualification passes 1,179 checks / 40 real Journey clears, including cold persistence; production startup (7 checks), earned shop (35 checks), and account-wallet UI (45 checks in six languages) also pass in separate runs.

| Evidence | Scope |
| --- | --- |
| [Final RC2 art](art-rc2/README.md) | Independent raster assertions, actual original glyphs, method, source identities and limits. The exact [intermediate art source](art/README.md) is retained. |
| [Wallet/cache/copy](commerce/) | Actual Java cache with fail-closed provider doubles, six-language copy, retained local store tests. No request or real account is used. |
| [Power purchase failure](store/) | Reproduction against the old method; six-power transaction, refusal, retry, cold reconstruction and capacity checks. |
| [Research and UX hypotheses](../../research/ux-v112.md) | Primary platform/motivation evidence; no retention promise. |
| [Intermediate retention](intermediate-retention.json) | Original RC1 APK/AAB bytes retained locally in ignored build storage; original metadata and source differences retained. |
| [Final Lab](ui-bugs/ui-sdk-final-rc2-lab40/README.md) | 1,179 checks / 40 actual clears; 96 original frames + 19 crops, six shop locales, real earned buys, empty-power return/rotation and distinct cold process. Profile/settings restored. |
| [Production startup and shop](ui-bugs/SDK_UI_FINAL.md) | 7 startup/audio checks + 35 narrow shop checks, 9 original frames; original profiles restored. |
| [Account-wallet UI](commerce-native/README.md) | Isolated RC2-b:45 checks/36 originals/6 languages; exact game/test APKs, opt-out and disabled spending, original preferences restored. Earlier RC2-a packaging gap excluded from current scope. |
| [Exact candidate](candidate.json) | Four RC2 artifacts, source fingerprints and compiled commercial gates. |
| [Build/lint](build/lint-summary.json) | Both flavors build; lint 0 errors/11 warnings each. |
| [Signed packages](binaries-rc2/binary-final-candidate.json) | Four signatures, pinned official bundletool and 16 KiB ZIP/ELF checks; 107 app inputs stable. |
| [Source compatibility](source-compatibility.json) | Engine/catalog/onboarding inputs unchanged; shared SweetUi presentation intentionally changed. |
| [SDK harness provenance](../../../scripts/v112-ui-test/provenance-v111.json) | Separate new harness; historical 1.11 inputs/fixtures are not rewritten. |

Native observations concern exact installed APKs. Host rasters, source models, emulator screenshots, physical-device quality and human comprehension have distinct scopes. The signed commercial gates remain closed; no Firebase public-origin probe, production ad, payment or Play upload is part of the qualification.
