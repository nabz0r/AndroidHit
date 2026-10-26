# Third-party facilities and notices

The original game source, companion drawings, interface illustrations and synthesized audio remain under the repository's [AGPL-3.0 license](../LICENSE). This does not relicense external SDKs, platform fonts or Google branding under AGPL.

Stable 1.1 uses Android platform facilities and no third-party runtime library. The isolated Lab adds the following independently licensed components. Their licenses and transitive notices remain applicable; this table is provenance information and not a legal compatibility opinion.

| Component | Lab version | Primary terms/source |
| --- | --- | --- |
| GMA Next-Gen | 1.6.0 | [Google SDK terms](https://developer.android.com/studio/terms.html); Maven POM identifies Android Software Development Kit License |
| User Messaging Platform | 4.0.0 | [Google SDK terms](https://developer.android.com/studio/terms.html) |
| Play Billing | 9.1.0 | [Google SDK terms](https://developer.android.com/studio/terms.html) |
| AndroidX Credential Manager | 1.6.0 | [AndroidX source/licenses](https://android.googlesource.com/platform/frameworks/support/) |
| Google ID for Credential Manager | 1.2.1 | [Google Identity](https://developers.google.com/identity/android-credential-manager/releases) / supplied artifact terms |
| Firebase Authentication | 25.0.0 | [Firebase Android source/licenses](https://github.com/firebase/firebase-android-sdk) and supplied dependency notices |
| Desugar JDK libraries | 2.1.5 | [Desugar libraries source/licenses](https://github.com/google/desugar_jdk_libs) |
| Gradle wrapper | 8.14.3 | [Gradle Apache-2.0 license](https://github.com/gradle/gradle/blob/master/LICENSE) |
| Google G on sign-in control | Official original PNG | [Branding guidelines](https://developers.google.com/identity/branding-guidelines); [original asset](https://developers.google.com/static/identity/images/g-logo.png) |

The dependency graph also contains AndroidX, Kotlin, Google Play services and other transitive facilities. Inspect the Gradle dependency report and distributed artifact notices before production redistribution. No claim is made that Google's SDKs, trademark or account services belong to this project.

The Google G image is bundled unchanged for its prescribed sign-in control. No other game's characters, art or recordings were imported. Comparisons in research and UI inspiration documents are references, not licensed franchises or endorsements.
