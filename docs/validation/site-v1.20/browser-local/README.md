# Local browser review —1.20 F

Actual CUA browser observations on `http://127.0.0.1:8791` only. The final APK identity is `e4fa1a70dbb2e842c9a16a9f22d0f2a49a4316ccbc6013fe0acfa233093f5d26`.

[Report](report.json) records17 captures/observations,14 accepted states and3 retained diagnostic states. Measured layouts include2360×1295,1280×800,375×812 and320×720. All accepted states have no horizontal overflow, loaded visible images and no autoplay. Offscreen lazy assets are not counted as visually inspected. FR/EN headings, original Android captures, download destinations and final1.20 privacy title/body were checked. Real Tab input focuses the skip link with a visible solid outline. The unchanged gallery selects Orbit/Jump and reports a loaded preview; this does not qualify every drawing or ownership.

One initial anchor capture still showed Home, and two initial privacy captures exposed the obsolete1.13 title. Those states remain diagnostics; the corrected1.20 title has separate FR/EN captures. Gallery-control status is a separate DOM observation from its returned-hero screenshot.

[Payload binding](payload-binding.json) confirms that all508 prepared public payload hashes remain identical after binding the immutable artifact commit. [Source contracts](../source-contracts.json) are independent of browser rendering.

Public-origin observation was refused earlier by the saved browser permission. No alternate tool or network path was used to inspect that origin. This local review is not public-byte, public-render, physical-phone, listening or retention acceptance.
