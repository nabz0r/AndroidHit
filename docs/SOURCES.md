> **Current1.17 design sources:** [29October2026 primary-source review](design/v1.17/reference-research.md) links official CrashFever/King/Apple/Android/Material evidence, separates inference from facts and records original Luma decisions. [Generation provenance](design/v1.17/art-prompts.json) identifies bundled original art; no franchise character/art/audio is included. Earlier sources below retain their dated purpose.

# Sources et méthode

Consultation : **26 octobre 2026**. Les pages vivantes peuvent évoluer ; les rangs sélectionnés sont retranscrits dans le dépôt. Aucune base payante, donnée privée d’éditeur ou mesure interne d’acquisition n’a été obtenue.

## Marché

| Source | Période / périmètre | Utilisation et limite |
|---|---|---|
| [SocialPeta — Google Play Games Worldwide](https://socialpeta.com/en/downloads-revenue-charts/google-play/games) | Septembre 2026, monde Google Play | Série principale de téléchargements estimés et variations mensuelles ; page dynamique, modèles non audités |
| [AppBrain USA](https://www.appbrain.com/stats/google-play-rankings/top_free/game) | 26/10/2026, Top Free Games | Relevé quotidien ; source indépendante de Google |
| [AppBrain Inde](https://www.appbrain.com/stats/google-play-rankings/top_free/game/in) | Même date et catégorie | Rang pays, pas volume mondial |
| [AppBrain Brésil](https://www.appbrain.com/stats/google-play-rankings/top_free/game/br) | Même date et catégorie | Idem |
| [AppBrain Indonésie](https://www.appbrain.com/stats/google-play-rankings/top_free/game/id) | Même date et catégorie | Idem |
| [AppBrain France](https://www.appbrain.com/stats/google-play-rankings/top_free/game/fr) | Même date et catégorie | Idem |
| [AppBrain Japon](https://www.appbrain.com/stats/google-play-rankings/top_free/game/jp) | Même date et catégorie | Idem |
| [AppBrain Corée du Sud](https://www.appbrain.com/stats/google-play-rankings/top_free/game/kr) | Même date et catégorie | Idem |
| [AppBrain Turquie](https://www.appbrain.com/stats/google-play-rankings/top_free/game/tr) | Même date et catégorie | Idem |
| [AppBrain Rotate Rings](https://www.appbrain.com/app/rotate-rings/com.nebula.rotaterings) | Profil mis à jour 25/10/2026 | Estimations cumulées et trente jours, distinctes du relevé pays du 26 |
| [AppBrain Meowdoku](https://www.appbrain.com/app/meowdoku-brain-puzzle-games/com.oakever.meowdoku) | Profil mis à jour 25/10/2026 | Idem ; identification de la famille de jeu |
| [Sensor Tower — septembre 2026](https://sensortower.com/blog/top-10-worldwide-mobile-games-by-revenue-and-downloads-in-september-2026) | Septembre 2026, iOS + Google Play | Corroboration de croissance, pas classement Android isolé |
| [FoxData — septembre 2026](https://foxdata.com/en/blogs/global-mobile-game-rankings-september-2026-top-15-downloads-revenue/) | Septembre, article publié 13/10, mis à jour 14/10 | Désaccord de classements consigné ; chiffres non fusionnés |

Fiches officielles pour les descriptions de gameplay : [Rotate Rings](https://play.google.com/store/apps/details?id=com.nebula.rotaterings), [Meowdoku](https://play.google.com/store/apps/details?id=com.oakever.meowdoku), [Arrows](https://play.google.com/store/apps/details?id=com.ecffri.arrows), [Block Blast!](https://play.google.com/store/apps/details?id=com.block.juggle). Les badges de téléchargements de Play sont cumulés ; les avis visibles ne sont pas un échantillon représentatif.

## Droits et outils

- [Ministère de l’Économie du Luxembourg — droit d’auteur](https://meco.gouvernement.lu/fr/domaines-activites/propriete-intellectuelle/droits-auteur-voisin.html), page modifiée le 30/12/2024 : œuvres et idées.
- [OEB — directives 2026, jeux](https://www.epo.org/en/legal/guidelines-epc/2026/g_ii_3_5_2.html) : distinction entre règles et contribution technique.
- [OMPI — jeux vidéo et propriété intellectuelle](https://www.wipo.int/en/web/wipo-magazine/articles/video-games-and-ip-a-global-perspective-38773) : diversité des droits et territoires.
- [Tetris — mentions du titulaire](https://www.tetris.com/news) : identité et éléments revendiqués.
- [Google Play — propriété intellectuelle](https://support.google.com/googleplay/android-developer/answer/9888072?hl=en) : droits sur l’app et sa présentation.
- [Godot — licence](https://godotengine.org/license/) et [export Android](https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_android.html) : conditions moteur et chaîne d’export à vérifier au prototype.
- [Licence déjà présente dans le dépôt](../LICENSE) et [FAQ GNU](https://www.gnu.org/licenses/gpl-faq.en.html) : fichier AGPL conservé ; compatibilité des intégrations à examiner.

## Publication et monétisation

- [Compte Play : frais et vérifications](https://support.google.com/googleplay/android-developer/answer/6112435).
- [Tests des nouveaux comptes personnels](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en).
- [API cible](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en).
- [Android App Bundle](https://developer.android.com/guide/app-bundle).
- [Données utilisateur](https://support.google.com/googleplay/android-developer/answer/10144311) et [Data safety](https://support.google.com/googleplay/android-developer/answer/10787469).
- [Public cible](https://support.google.com/googleplay/android-developer/answer/9867159).
- [Consentement AdMob](https://support.google.com/admob/answer/13554116) et [UMP](https://developers.google.com/admob/android/privacy/gdpr?hl=en).
- [app-ads.txt et vérification AdMob](https://support.google.com/admob/answer/14538460?hl=en-GB).
- [Paiements Play](https://support.google.com/googleplay/android-developer/answer/10281818).
- [Emplacement des publicités](https://support.google.com/googleplay/android-developer/answer/12271244) et [annonces récompensées](https://developers.google.com/admob/android/rewarded).
- [AdMob — eCPM](https://support.google.com/admob/answer/15337570) : définition ; les valeurs de simulation du dossier sont nos hypothèses.

## Ce que l’étude ne mesure pas

Pas de probabilité chiffrée de hit, de coût d’acquisition observé, de rétention du futur jeu, de revenu net prédit ni de validation juridique mondiale. Les délais, budgets, concepts, seuils et stratégies sont des propositions. La recherche et la publication de ce dossier ne constituent pas une implémentation du jeu.
