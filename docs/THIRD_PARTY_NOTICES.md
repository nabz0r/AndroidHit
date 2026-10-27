# Third-party facilities and notices

The original game source, companion drawings, interface illustrations and synthesized audio remain under the repository's [AGPL-3.0 license](../LICENSE). This does not relicense external SDKs, platform fonts or Google branding under AGPL.

Stable 1.1 uses Android platform facilities and no third-party runtime library. Current 1.5 production and Lab builds include the following independently licensed components. Their licenses and transitive notices remain applicable; this table is provenance information and not a legal compatibility opinion.

| Component | Android version | Primary terms/source |
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

## Server and public website

The separate server pins `firebase-admin` **14.5.0** and `google-auth-library` **11.3.1**, both identified as Apache-2.0 by their distributed package metadata. Their dependencies and license files remain subject to their own notices; inspect the committed server lockfile and installed distribution before deployment. [Firebase Admin source](https://github.com/firebase/firebase-admin-node), [Google authentication library source](https://github.com/googleapis/google-auth-library-nodejs).

The prepared external deletion page lazily imports Firebase Web SDK **13.1.0** from Google's official CDN only after configured explicit Google sign-in. Its upstream notices/terms still apply; no Analytics module is imported. [Firebase JavaScript SDK source](https://github.com/firebase/firebase-js-sdk). Website screenshots show original code-drawn art, and the sign-in button reuses the same unchanged Google G asset.

The Apache notices here are provenance, not a conclusion about all distribution/SDK terms or Google account approval. No server dependencies or private credentials are embedded into the Android APK.

The 1.5 website/GitHub refresh adds no runtime framework, remote font or analytics package. Original vector companions and copied game captures retain their recorded source provenance; Google G remains the separate official branding asset. [Shared design and image categories](DESIGN_SYSTEM.md) · [Brand asset provenance](store/README.md).
