# Revue finale du cœur — RC2 / code 16

**Aucun nouveau défaut bloquant trouvé dans les chemins examinés.** Cette revue porte sur la source gelée RC2 ; elle ne constitue ni un playtest humain ni le résultat du marathon natif en cours de préparation.

Le gel complet de 116 entrées est celui de `source-inputs-rc2.json` (SHA-256 `7e0ff59e8fd0bffcc60620f03975817cb1ab473110c10d024f2e23bfdc6112e4`). La carte brute est identique à celle vérifiée avant et après l'audit des quatre binaires ; aucune source app n'a été modifiée par cette revue.

| Chemin vérifié | Résultat et preuve concrète |
|---|---|
| Historique, budget et Retour | `LumaView.saveSession` écrit historique typé, politique, actions utilisées, quota `undos`, UUID de tentative et continuations dans le même commit. `persistMove` bloque les actions après un échec de sauvegarde. `MoveBudget` policy4 impose `used == boardMoves` à la restauration ; un Retour rembourse une action, incrémente `undos` et ne peut être renouvelé par un redémarrage de processus. Un redémarrage volontaire de la tentative crée un nouvel UUID et son unique Retour. |
| Anciens plateaux et sauvegardes valides | `restoreSession` route explicitement les contenus2/3/4/5/6, sans réinterpréter une ancienne histoire L comme N. Policy1 refuse la conversion et garde sa base originale jusqu'à la fin ou au restart. Policies2/3 conservent la géométrie et les extensions acquises ; la migration policy4 utilise l'historique productif validé et retire les anciennes charges de refus. Les anciens compteurs de Retour sont conservés, même supérieurs à un. Les schémas non supportés restent traités par la notice de migration existante ; cette revue ne promet pas de restaurer tous les schémas historiques arbitraires. |
| Identités partagées | `ChallengeCode` conserve ses anciens caractères/familles et leurs résolveurs dédiés. N est une famille distincte. Les pratiques entre amis restent sans récompense de victoire/XP et sans consommation de pouvoir, même si leur budget d'actions est illimité. |
| Continuation déjà acquise | `applyPendingContinuation` exige reçu acquis, UUID actif et ordinal suivant dans le plafond de deux. `settleEarnedExtension` honore le reçu même après remboursement des anciennes charges de refus. Budget étendu et `last_continuation` sont sauvés ensemble avant l'acquittement. Après échec, le candidat en mémoire est rétabli ; après sauvegarde réussie, le même reçu ne réaccorde pas l'avantage. Mouvement, pouvoir, Retour et restart attendent le règlement d'un reçu acquis. Policy4 n'offre aucune nouvelle continuation payante. |
| Commerce et reçus | Les reçus publicitaires existants sont réécrits durablement avant d'être considérés acquis. `spendGoldOnce` retente le marqueur et le solde du débit existant avant de conclure, sans second débit. La retraite de Shield filtre les nouvelles offres, sans effacer anciens stocks, réservations ou décisions de cadeaux déjà écrites. |
| Minimum exact et physique | `Board` distingue coût restant exact et budget restant. Les portes manuelles ne sont disponibles que pour les versions4/5 ; un Undo inverse le dernier effet réel, dont les clés/switchs/ouvertures. L'optimisation de suffixe repose sur des certificats dont les minima ont été vérifiés indépendamment. Les 1 000 minima N, leurs métriques et les 970 états initiaux préservés ou ouverts en plus sont couverts par les contrats RC2 ; les anciennes géométries ont leur audit exhaustif séparé. |

Références : `engine/MoveBudget.java` (création/restauration/conversion, quota, tentative et règlement acquis), `engine/Board.java` (actions, Undo, restauration atomique et solveur), `engine/Level.java`, `engine/ChallengeCode.java`, `LumaView.java:300–350`, `1822–1990`, `2021–2037`, `commerce/CommerceActivity.java:57–89`, `data/GameStore.java:1140–1180`.

Les [contrats contenu RC2](audit-learning/audit-learning-contracts.json) comptent **201 143 contrôles**. Le [registre d'exécution policy4 RC2](deliberate-play-rc2.txt) rapporte **24 474 contrôles et 19 920 actions productives**. Le précédent résultat de 24 775 contrôles / 20 221 actions porte sur le corpus RC1 ; il ne doit pas être présenté comme l'exécution du corpus RC2. La [vérification binaire RC2](binaries-rc2/binary-final-candidate.json) valide les versions, signatures, bundles, alignements et stabilité des 116 sources ; elle n'est pas une preuve d'exécution native.

## Revue du marathon natif avant lancement

Le chemin `bulkCache` du SDK vérifie après **chaque** geste l'ajout exact de son action à l'historique et une dépense de budget égale à un. Chaque board commence avec un historique vide et le bon nombre de pièces observées ; la fin exige le panneau de victoire natif et trois étoiles visibles. Le wrapper limite le batching à Journey, conserve uniquement la progression réellement acquise entre processus et restaure les préférences d'origine en fin de parcours. Un batch échoué n'est pas converti en PASS global.

Une relecture physique indépendante des 1 000 fixtures, avec positions, directions, switchs, clés/sceaux et masques de portes avant/après, a passé **23 946 contrôles** : [résultat](marathon-input-review.json). La source gelée finale régénère aussi les **1 000 JSON byte-identiques** aux assets et au SDK final `6a2f8c634f5acd1a5a7d4a107d8c10058a65e741611299559ac26b3fcd72f83d`, soit 19 920 actions.

Un seul ancien hash du manifeste de génération diffère : `ProgressionInfo.java`, corrigé ensuite pour indiquer la provenance L des parents de N. Son manifeste original et le SDK restent intacts. L'attestation séparée `/tmp/luma-v114-qa/learning-revalidation-final-rc2.json` (SHA-256 `1d24d2de4bee0cd9db922008037f64d21447bd44239ee0b5569d1aa9e8c759ec`) lie le gel final complet, le SDK et les 1 000 comparaisons exactes ; QA l'archive avec ses preuves natives. Aucun rebuild Android, appareil ou préférences n'a été utilisé par cette revalidation hôte.

Limite conservée : ce parcours vérifie des solutions connues et leur exécution par l'UI. Il ne vérifie pas séparément chaque libellé graphique de porte/clé après chaque action, ni la compréhension d'un premier joueur. Il ne devient un résultat natif complet qu'après son rapport global final `passed` et sa restauration vérifiée.

## Cohérence documentaire

Les pages actuelles 1.14 décrivent bien un budget strict et un seul Retour. Les formulations de Retour gratuit/illimité dans les sections datées du README restent explicitement historiques. Trois pages génériques encore titrées « Current » (`GAMEPLAY.md`, `MOVE_BUDGET_POWERUPS.md`, `LEVEL_DESIGN.md`) ont reçu un renvoi en tête vers les règles1.14 ; leur description historique policy3 n'a pas été réécrite. Aucun fichier app, catalogue, SDK ou manifeste gelé n'a été modifié.

## Garde-fou CI ajouté

[Le workflow puzzle](../../../.github/workflows/puzzle-contracts.yml) exécute quatre contrôles hors ligne sur les push vers `main`, les pull requests ciblant `main` et les lancements manuels. Il utilise Ubuntu 24.04, Java 17 Temurin et uniquement `contents: read`. Les identifiants de checkout ne sont pas conservés. Il ne demande aucun secret, accès aux fournisseurs, APK signé ou audit historique de 773 millions de contrôles.

Les actions officielles sont épinglées à leur commit complet, vérifié le 28 octobre 2026 par la page de release officielle et la référence du tag du dépôt officiel :

- [actions/checkout v7.0.1](https://github.com/actions/checkout/releases/tag/v7.0.1) : `3d3c42e5aac5ba805825da76410c181273ba90b1`.
- [actions/setup-java v6.1.0](https://github.com/actions/setup-java/releases/tag/v6.1.0) : `b843ebf9b913146b38da2eaa7aeddf76074ae1ad`.

Les quatre runners sont invoqués par `sh`, sans dépendre du bit exécutable. Le runner PowerOffers utilisait encore des constructions Bash ; elles ont été remplacées par leur équivalent POSIX, puis le même test métier a repassé. Le workflow écrit ses rapports temporaires dans `RUNNER_TEMP`, sans réécrire les preuves de release suivies par Git.

La validation locale du YAML, des déclencheurs, des permissions, des pins et de la syntaxe shell passe. Une exécution ciblée avec le Java 17 local a aussi passé les quatre commandes : **201 143 / 24 474 / 4 867 / 868 contrôles**. Cette reprise était motivée par la version de Java du futur job et la correction POSIX ; les audits lourds déjà verts n'ont pas été relancés. [Rapport et sorties locales](ci-puzzle-local/report.json). Les 116 sources app gelées sont toujours identiques.

**Au moment de cette vérification, le workflow n'est pas encore poussé et aucune exécution GitHub distante n'a été observée.** Les résultats locaux macOS/Homebrew Java 17 ne sont pas présentés comme un run Ubuntu/Temurin distant. Le workflow publiera un statut de contrôle ; son ajout seul ne configure pas les règles de protection de `main`.


Le checkout a ensuite été réduit à une sélection **non-cone** des seuls fichiers compilés ou lus : sources engine/data, deux helpers de progression, quatre runners, leurs tests et stubs Android, workflow et fichiers racine utiles. La seule preuve documentaire nécessaire au runtime des tests est le fichier `audit-learning-rc1/LearningLayouts.java`, qui compare les trente introductions ; les APK/AAB, captures, SDK et autres archives sont exclus. La règle doit donc être accompagnée de l'inclusion de ce baseline dans le commit final.

La profondeur est explicitement limitée à un commit. Aucune option `filter` n'est définie en concurrence avec `sparse-checkout` : [le code officiel de checkout épinglé](https://github.com/actions/checkout/blob/3d3c42e5aac5ba805825da76410c181273ba90b1/src/git-source-provider.ts#L168-L172) choisit lui-même `blob:none` dans ce cas. Les règles ont été vérifiées par `git sparse-checkout check-rules --no-cone`, sans appliquer de sparse checkout au poste de travail. Les 74 fichiers requis sont tous sélectionnés ; avec le fichier racine utile, cela représente **75 fichiers / 1 526 428 octets de fichiers actuels**, sans binaire. Cette taille mesure les fichiers sélectionnés, pas le transfert réseau Git et ses métadonnées. [Couverture précise des entrées](ci-puzzle-local/sparse-checkout.json).

Les quatre commandes métier et leurs entrées sont inchangées ; aucun test métier déjà vert n'a été répété pour ce seul changement de téléchargement. Aucune source app, SDK, préférence ou référence distante n'a été modifiée, et aucun commit n'a été créé par cette optimisation. L'exécution GitHub distante reste à observer après publication.
