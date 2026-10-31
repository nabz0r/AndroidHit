# Rang → copie du capitaine : source locale 1.24

Le nouveau lien de progression est implémenté et vérifié sur les vrais modèles Java. Aucun APK corrigé n’est assemblé ou qualifié par ce dossier ; la version publique 1.23 E reste distincte. Le rendu du résultat et sa future qualification native appartiennent au lot 1.24 global.

## Règle

Une première victoire sur un plateau éligible conserve son XP existante : 25 / 30 / 35 XP pour 1 / 2 / 3 étoiles, avec 120 XP par rang. Chaque nouveau rang donne une copie au capitaine enregistré au départ, `team[0]`. Modifier ensuite le personnage d’accueil ou l’équipe ne déplace pas la récompense. Une copie augmente le niveau selon les seuils existants ; la capacité de talent correspondante est capturée à la prochaine partie, sans changer une partie déjà commencée.

Un capitaine à 92 copies reçoit exactement 45 LumaCoins à la place. Au plafond du portefeuille, la récompense reste en attente ; son rang n’est pas marqué comme payé. Aucune nouvelle monnaie ni modification d’énergie, de prix, de coût de talent, de CONTINUATION ou de POWER n’est introduite par ce lot.

Lors de la première application de cette politique, le rang déjà atteint devient un plancher durable. Aucun cadeau rétrospectif. Une ancienne partie sans capitaine durable reçoit son XP normale, avec `LEGACY_NO_CAPTAIN` et aucune copie supposée. Une ancienne session possédant un `battle_v119` valide possède déjà un véritable instantané d’équipe. Un ancien résultat prêt reste inchangé.

## Contrat de reprise

Le coordinator n’exécute aucune action du plateau. Son appel de production est effectué par `finishLevel()` seulement après `board.isSolved()`. L’argument booléen du harness représente ce garde ; ce n’est pas une preuve cryptographique de victoire pour des appelants arbitraires.

1. Le reçu de victoire en attente et le capitaine sont sauvegardés avant les gains.
2. Les gains de base conservent leurs API idempotentes existantes. Le plancher de migration est durable avant la nouvelle XP.
3. `freezeVictoryCoins124` enregistre le montant de base déjà obtenu pour ce run. Il ne crédite rien. Un achat ultérieur ne fausse plus le total affiché d’un rang en attente.
4. `MetaProgress.recordWin124` écrit ensemble l’XP, le premier gain du plateau, les missions et un reçu run/plateau/capitaine/XP avant-après. Un reçu déjà gagné reste récupérable après changement de jour ou de semaine ; un nouvel événement expiré reste refusé.
5. `GameStore.claimRankCopy124` écrit copie ou compensation et marqueur rang/run/capitaine dans une même transaction `LocalSaveTransactions`. Les tentatives répétées renvoient le gain historique sans le recréditer.
6. Le résultat prêt porte les montants exacts de ces reçus. Une fermeture après le crédit mais avant l’écriture du résultat reconstruit ce même affichage.

Les fichiers XP et GameStore restent deux fichiers de préférences : l’ensemble utilise un journal rejouable, pas une transaction Android entre plusieurs fichiers. Une écriture refusée conserve la tentative en mémoire et la montre comme bloquée ; une reprise froide s’appuie uniquement sur les écritures déjà durables. Les garanties supposent des préférences intègres et un stockage redevenu inscriptible. Une suppression/corruption externe des reçus, ou la perte de tout écrit durable, n’est pas couverte. L’enregistrement initial des Coins s’appuie encore sur la différence de solde du chemin de victoire existant : il ne prouve pas l’isolation d’une autre transaction de monnaie concurrente avant cette première capture.

## Champs pour le rendu

`VictoryReceipt.VERSION` et les 17 champs historiques `v120_1` restent compatibles. Les nouveaux reçus utilisent `v124_1` et 23 champs.

| Champ | Usage |
| --- | --- |
| `captainId` | Identité enregistrée au départ, indépendante du personnage d’accueil |
| `rankBefore`, `rankAfter` | Rangs encadrant l’XP de cette victoire |
| `rankCopies` | Copies effectivement attribuées par cette victoire |
| `rankCoins` | Compensation du capitaine au maximum |
| `rankRewardStatus` | `COPY`, `MAX_COMPENSATION`, `LEGACY_NO_CAPTAIN`, `NONE`, `REPLAY`, `INELIGIBLE` ou `MIGRATED` |
| `coins` | Total déjà acquis du run, **compensation incluse** |
| `xp` | XP exacte du run, zéro sur un nouveau replay |

Ne pas ajouter `rankCoins` une seconde fois à `coins`. Le rendu doit nommer le capitaine du reçu, pas le personnage actuellement équipé. `PENDING` est réservé aux reçus non finalisés. Les modes événement et Remix capturent l’équipe dans `resetMoveRun()` même si l’interface ne propose pas de talent dans ce contexte. Les modes défi ami et expédition conservent leur absence d’XP de plateau dans `MetaProgress`.

## Preuves

- [Suite finale du coordinator](attempt-7/report.json) : 31 298 assertions, 4 570 contrôles de plateaux réellement résolus, 1 004 cas de reprise d’échec d’écriture, 506 reconstructions froides et 24 handles concurrents. Les répétitions de contrôles ne sont pas 4 570 plateaux distincts ni des joueurs.
- [Reçu et sources du run](attempt-7/receipt.json) : vrais `GameStore`, `MetaProgress`, coordinator et `VictoryReceipt`, adaptateurs de préférences/horloge. Les 320 victoires uniques atteignant le maximum utilisent les vrais certificats et `Board.performAction`. Les tests événement vérifient les API de progression et l’instantané ; ils ne rejouent pas graphiquement ces niveaux.
- [Régressions](regressions-1/receipt.json) : Meta 362 assertions et export JSON, Focused Journey 178 587, CompanionBattle 94 458, Journey Clarity 52 861, tenue 79. Chaque reçu lie les sources à son propre instant d’exécution.
- [Compilation Java intermédiaire Production et Lab](java-compilation-1/receipt.json) : PASS, 7 secondes, aucun drift des sources Java/XML capturées. Avertissements SDK XML et dépréciations conservés dans les journaux. Aucun assemblage/signature d’APK.
- [Sources figées du lot](source-snapshot/manifest.json) et [historique des tentatives](attempt-dispositions.json), y compris échecs initiaux. Le seul solde directement injecté est le cas limite explicite à 2 milliards de Coins ; les 92 copies sont obtenues par la progression réelle préalable.
- [Revue indépendante ciblée](independent-review.json) : l’écart de reprise après changement de période est corrigé et testé ; aucun autre défaut matériel trouvé dans les classes et méthodes relues. Cette revue ne couvre pas tout `LumaView`.

Reproduction : `python3 scripts/playtest-2026-10-31/run-rank-copy-tests.py --output /tmp/luma-rank-copy-new-run` depuis le dépôt. Le répertoire de sortie doit être nouveau. Les contrôles ne qualifient ni rendu Android, appareil physique, fournisseur publicitaire, paiement réel, plaisir, compréhension ou rétention humaine.
