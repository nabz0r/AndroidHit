# 1.10 Companion & Objectives — source ready, Hosting pending

The website source now presents **production 1.10.0-rc3 / code 11** with the exact signed APK route, **7,757,410 bytes / 7.76 MB**, SHA-256 `29a68cdcaeb61850d55afd101f03bd8c42a075cc6d6631205fc176e499dd70b4`. [Production manifest](../../../releases/manifest-1.10.0-rc3.json). **This does not establish a deployed 1.10 site.** The last confirmed Firebase Hosting delivery remains [1.9](../site-v1.9/deployment.json).

## Actual native images

[Eighteen original PNG copies](captures.json) come from passing exact-rc3 records: 17 from the [2,514-check main native run](../v1.10/ui-sdk-final3-full100/host-report.json), one from [fresh guest I](../v1.10/ui-sdk-fresh-guest-i/host-report.json), 91 checks. The main Lab hash is `d1b085b4d9bbcf450037069ed15c7cea89b0e3e759b16e587cf3ca016a7c493f`; original preferences and rotation are restored. Fresh I was genuinely absent before install and retains its newly created guest; it has no prior guest to restore.

The five core French frames—Home, companion, ready objectives, journal and appearance shop—have original 1080×2340 dimensions and EN alternatives. The fresh English tutorial invitation is 1080×2400. Every public PNG is byte-identical to its host-report hash and actual native capture sequence, with the corresponding accessibility snapshot. No frame is cropped, repainted or relabelled from rc1/rc2.

The main captures show a real local guest after 100 Journey clears and later replays. Home resumes L11; the selected Cupid goal shows 450 / 450 available coins but remains unowned. The journal shows chapter 2, two claimed guardian fragments out of 100 and 0 / 100 wins in 101–200. Browsing the map at 1,000 does not unlock it. Six available rewards appear in the captured ready list. These states are neither a zero-clear guest nor a Google-account, live-ad or purchase demonstration.

The five French images and the English native preview were visually inspected as local PNGs. The selected mint backgrounds are visible and the journal ornament no longer touches its first line. Long quest titles may still be ellipsized; their complete accessible names are separate from visible text. This is an image review, not a website browser render or human usability study. [Readability matrix](../v1.10/ux-readability/README.md).

## Local source checks

The [release-source check](release/static-check.json) passes **3,271 assertions / three HTML pages / 78 local references**. Its [manifest](release/source-manifest.json) fingerprints **89 payload files** and its [asset index](release/source-assets.json) separates current rc3 captures from retained historical images and original artwork. [32 current language/deletion unit cases](node-tests-final.txt) pass. Historical site 1.7/1.8/1.9 reports remain byte-identical during the check.

The 1.10 checker accepts only the two exact production/Lab **rc3** filenames for current native provenance. It verifies independent production SHA, manifest version/code/bytes, closed commercial flags, actual PNG dimensions, passing host/native records, profile-preservation scope and at least five referenced current images. Earlier source preparation and intermediate candidates remain records for their own bytes, not current acceptance.

```sh
python3 scripts/check-site-v110.py --phase release \
  --apk-sha256 29a68cdcaeb61850d55afd101f03bd8c42a075cc6d6631205fc176e499dd70b4
node --test public/tests/*.test.mjs
```

## Delivery boundary

**Local CUA browser review and Firebase Hosting 1.10 deployment have not been performed.** The desktop is locked, with no authenticated local Firebase CLI available. [Delivery status](deployment-status.json) records the pending step; source publication on main is distinct from Hosted-site delivery. No Firebase public-origin probe is made to substitute for the unavailable authenticated deployment path.

Public runtime configuration, Google identity/deletion modules, separate confirmation and server-first deletion behaviour remain unchanged. No provider login, real account deletion, ad impression, purchase or remote analytics is exercised by these source tests. There is no claim of Play publication, physical accessibility certification, native-speaker review, retention improvement or revenue.
