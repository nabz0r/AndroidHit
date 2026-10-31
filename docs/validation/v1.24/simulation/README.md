# Simulation du cœur 1.24 — 31 octobre 2026

**La progression XP → rang → copie → usages supplémentaires a une conséquence mécanique réelle. Notre politique synthétique utilise cependant trop souvent l’aide : avec Nova évolutive, elle avance un peu moins loin que son contrôle à un usage, malgré moins de budgets épuisés et moins de dépenses en Undo. Aucune baisse réelle chez des joueurs n’est établie ; ce résultat ne justifie pas un nerf.**

Les demandes de talents sont examinées avant le suivi d’un indice déjà visible. Le modèle peut donc redemander GUIDE et rafraîchir le même indice. Les activations supplémentaires comptées ci-dessous ne sont pas autant d’informations nouvelles différentes. Aucun gain de motivation, d’attachement ou de plaisir n’est ajouté aux formules lorsque les copies progressent.

## Nouveau lot exécuté et périmètre

- **8 000 trajectoires** : 100 profils synthétiques × 20 répétitions × 4 conditions, sur 8 journées modélisées. Nouvelle graine : **2026103102**.
- **350 874 tentatives**, **346 581 victoires**, **2 610 518 décisions**, **143 142 blocages** et **64 000 journées configurées**. Les tentatives restantes comprennent **769 épuisements de budget** et **3 524 censures de fin d’horizon** à budget encore positif ; aucune censure à budget nul.
- **95 395 récompenses de rang** simulées, avec identité du capitaine capturée ; **350 874 snapshots de capacités** et **341 696 demandes de talents** exportés exhaustivement.
- **8 000 sondes froides exécutées une seule fois**, puis **32 000 sondes après entraînement**. Les **104 contrôles oracle** sont des certifications de moteur séparées, jamais des trajectoires d’agent.
- **68 fichiers de moteur, driver et contrats hachés**, stables avant/après l’exécution. Les **261 fichiers de preuves préexistantes** protégés restent inchangés, notamment le [lot 1.23 du 31 octobre](../../playtest-2026-10-31/simulation/README.md). Aucune sortie publique, release, ZIP ou archive n’a été écrasée.

Le moteur exécute réellement les plateaux à flèches sans préparation élémentaire, le budget natif, les coûts d’Undo, les effets de `CompanionBattleAction`, les capacités de `CompanionBattle.State v124_1`, `CompanionProgression` et les formules pures de `RankCopyRules124`. Le driver utilise un adaptateur de récompenses en mémoire. **Il ne teste pas les transactions Android, l’UI, les publicités, LumaGold, l’équipement ou la décoration.** Ces périmètres demandent leurs propres essais natifs.

Chaque profil modélisé part avec XP 0, plancher de rang 1, Nova possédée à 1 copie, 180 pièces de budget de simulation et 10 énergies. Ce n’est pas une reproduction complète du portefeuille d’une première installation. Le trio prêté est une condition distincte : Hoot et Orbit ne sont pas artificiellement présentés comme déjà acquis par un nouveau joueur.

## Ce que le nouveau lien de progression fait effectivement

Pour la condition Nova évolutive, sur 2 000 trajectoires :

- **2 000 / 2 000** franchissent leur premier rang après U4 dans ce lot et passent Nova à 2 copies, donc au niveau 2. Ce taux parfait est un résultat du modèle et de ces premiers plateaux ; il ne mesure pas la réussite de personnes débutantes.
- **1 682 / 2 000** terminent avec une capacité future de 3 usages ; les 318 autres avec 2 usages. Aucun profil n’atteint naturellement le palier de 4 usages sur cet horizon. Les capacités 1 à 4 sont couvertes séparément par les tests du modèle.
- **36 510 activations au-delà du premier usage** sont réellement préparées et appliquées. **1 988 / 2 000** trajectoires en font au moins une ; **28 547 / 78 096 tentatives** disposant d’au moins 2 usages au capitaine en utilisent plusieurs, soit **36,55 %**.
- Les copies finales ont une médiane de **12**, un intervalle interquartile **9–16** et un maximum **27**. Le niveau final médian vaut **3** ; les niveaux 4 et 5 ne procurent pas un usage de plus que le niveau 3.
- Les **23 532 copies de rang** de cette condition ne changent aucune capacité de plateau déjà engagé. Les snapshots suivants sont calculés à partir des copies persistées dans l’adaptateur. Le contrôle à un usage reçoit lui aussi des copies, mais conserve explicitement son ancienne limite pour isoler la capacité.

Les tests de modèle exécutent un second GUIDE sur un plateau réellement modifié après une sortie de pièce, et vérifient que l’indice correspond à la position actuelle. Les données de simulation comptent toutes les activations ; elles ne supposent pas que chaque seconde activation apporte une nouvelle information.

## Distributions et comparaisons appariées

Chaque ligne compte 2 000 trajectoires et 16 000 journées configurées.

| Condition | Plateaux gagnés, moyenne | Médiane [Q25–Q75] | P10–P90 | Victoires / tentatives | Budgets épuisés |
| --- | ---: | ---: | ---: | ---: | ---: |
| Nova et capacités 1.24 | 42,5065 | 40 [29–55] | 20–69 | 85 013 / 86 096 | 160 |
| Nova limitée à 1 usage | 43,1440 | 41 [29–56] | 20–70 | 86 288 / 87 391 | 197 |
| Talents désactivés | 44,4295 | 43 [31–58] | 21–71 | 88 859 / 89 913 | 244 |
| Trio prêté, capitaine Nova | 43,2105 | 42 [29–56] | 21–70 | 86 421 / 87 474 | 168 |

La comparaison utilise les mêmes profils et les mêmes clés de graines d’entrée par paire. Les décisions et les états peuvent diverger ensuite ; ce ne sont pas les mêmes trajectoires auxquelles on retire simplement quelques taps. Les intervalles suivants décrivent la variabilité de 100 moyennes de profils synthétiques, rééchantillonnées par profil, pas une population humaine.

| Comparaison à Nova évolutive | Différence moyenne de plateaux | Intervalle bootstrap des profils | Paires |
| --- | ---: | ---: | ---: |
| Nova limitée à 1 usage − Nova évolutive | 0,6375 | [0,3610 ; 0,9020] | 2 000 |
| Talents désactivés − Nova évolutive | 1,9230 | [1,5350 ; 2,3445] | 2 000 |
| Trio prêté, capitaine Nova − Nova évolutive | 0,7040 | [0,3590 ; 1,0500] | 2 000 |

Face au contrôle à un usage, Nova évolutive effectue **25 965 décisions de plus**, dont **38 298 demandes de talent supplémentaires**, mais rencontre **2 440 blocages de moins**, **37 épuisements de budget de moins** et dépense **2 866 pièces de moins en Undo**. Elle finit avec **1 275 victoires de moins** sur l’ensemble des 2 000 trajectoires. Ce bilan décrit une politique qui peut dépenser ses usages trop tôt ou rafraîchir une aide au lieu de la suivre. Il ne mesure pas le bénéfice émotionnel de faire grandir Nova.

Le trio automatise des sorties et modifie l’exposition à la résolution manuelle. Sa comparaison ne mesure ni la valeur d’acquisition de Hoot/Orbit ni le meilleur choix d’équipe. Aucun algorithme de planification optimal des talents n’est utilisé.

## Ressources : trois unités distinctes

| Condition | Énergie dépensée | Journées finissant sur énergie vide / 16 000 | Pièces dépensées en Undo | Charge dépensée en talents | Refus / demandes de talents |
| --- | ---: | ---: | ---: | ---: | ---: |
| Nova et capacités 1.24 | 66 143 | 2 863 | 11 375 | 210 090 | 6 997 / 112 042 |
| Nova limitée à 1 usage | 67 440 | 3 031 | 14 241 | 138 276 | 4 606 / 73 744 |
| Talents désactivés | 69 962 | 3 548 | 17 289 | 0 | 0 / 0 |
| Trio prêté, capitaine Nova | 67 533 | 3 117 | 13 167 | 304 987 | 11 780 / 155 910 |

La Charge de talent n’est ni une pièce ni une énergie de session. GUIDE coûte 2 Charges ; une sortie de pièce unique peut en rendre 1. Une demande refusée ne débite ni Charge ni pièce, mais elle prend une décision et une durée dans le modèle. L’énergie suit la politique actuelle dans toutes les conditions : nouveaux départs payants après U10, récupération d’une unité par 10 minutes modélisées, retry compté comme nouveau départ. Il n’y a pas de condition « énergie illimitée » dans ce lot.

Les dépenses en pièces restent faibles face aux récompenses : pour Nova évolutive, **11 375 pièces dépensées** contre **5 137 357 gagnées** ; la médiane individuelle de dépense vaut **0**, P90 **16**. Cela qualifie uniquement l’Undo de cette simulation. Elle ne consomme pas de meubles, tenues, packs, invocations ou continuations publicitaires et ne démontre donc pas l’équilibre de l’économie complète. Aucune compensation de rang à 92 copies n’est atteinte naturellement ici ; sa règle de 45 pièces est testée séparément.

## Courbe de difficulté : arrivée, départ et épuisement

Une **arrivée** est une trajectoire distincte ayant effectivement commencé le niveau. Un **départ de tentative** inclut ses retries. Une **victoire** est une réussite unique de ce niveau. Un **épuisement** concerne une tentative à budget nul avec des pièces restantes ; une **censure d’horizon** laisse encore du budget. Aucun de ces termes n’est un taux de départ humain.

Condition Nova évolutive :

| Niveau | Arrivées distinctes | Départs de tentative | Victoires | Épuisements / départs | Censures d’horizon |
| --- | ---: | ---: | ---: | ---: | ---: |
| U27 | 1 581 | 1 581 | 1 568 | 0 / 1 581 | 13 |
| U33 | 1 386 | 1 436 | 1 335 | 58 / 1 436 | 43 |
| U36 | 1 236 | 1 252 | 1 197 | 21 / 1 252 | 34 |
| U43 | 932 | 946 | 908 | 16 / 946 | 22 |
| U48 | 738 | 747 | 726 | 10 / 747 | 11 |

**U33, U36 et U43 concentrent 95 des 160 épuisements** de Nova évolutive. U27 ne produit aucun épuisement dans cette condition, mais demande 335 Undo et rencontre 417 blocages : la récupération peut masquer un piège dans le seul taux de victoire. À U43, 506 des 532 blocages viennent du chemin occupé par une autre pièce ; à U48, les 212 blocages se répartissent entre pièces (98), portes (62) et barrières (52).

Comparaisons conditionnelles de U43 et U48 :

| Condition / niveau | Victoires / arrivées | Victoires / départs de tentative | Épuisements / départs |
| --- | ---: | ---: | ---: |
| Nova et capacités 1.24 · U43 | 908 / 932 (97,42 %) | 908 / 946 | 16 / 946 |
| Nova et capacités 1.24 · U48 | 726 / 738 (98,37 %) | 726 / 747 | 10 / 747 |
| Nova limitée à 1 usage · U43 | 932 / 957 (97,39 %) | 932 / 982 | 26 / 982 |
| Nova limitée à 1 usage · U48 | 746 / 763 (97,77 %) | 746 / 768 | 5 / 768 |
| Talents désactivés · U43 | 1 004 / 1 035 (97,00 %) | 1 004 / 1 070 | 40 / 1 070 |
| Talents désactivés · U48 | 805 / 829 (97,10 %) | 805 / 833 | 5 / 833 |
| Trio prêté, capitaine Nova · U43 | 946 / 977 (96,83 %) | 946 / 994 | 20 / 994 |
| Trio prêté, capitaine Nova · U48 | 754 / 772 (97,67 %) | 754 / 775 | 4 / 775 |

Les cohortes qui arrivent tard ne constituent plus les mêmes sélections et n’ont pas les mêmes expositions antérieures. Ces taux conditionnels ne permettent pas de classer la puissance des compagnons ni de déduire pourquoi des personnes décrocheraient. Les journées limitées, la disponibilité et les formules d’utilité contribuent aux arrivées.

## Sondes communes sans aide

Les quatre plateaux tenus hors campagne sont U401, U573, U721 et U1000. Même géométrie sans coques, même budget, 180 pièces, aucune information GUIDE/TRAIL/VISION, cause de blocage, coach, Focus ou avertissement d’impasse. L’Undo ordinaire reste possible. Seules les connaissances finales de règles sont transplantées ; fatigue, frustration et mémoire repartent à zéro. **Le feedback continue pendant chaque sonde** : c’est la performance de politiques adaptatives initialisées différemment, pas une lecture figée de leur seul état appris.

| État initial | Sondes réussies / 8 000 | Réussites après entraînement seulement | Réussites froides seulement |
| --- | ---: | ---: | ---: |
| Référence froide commune | 4 701 / 8 000 | — | — |
| Nova et capacités 1.24 | 6 190 / 8 000 | 2 236 | 747 |
| Nova limitée à 1 usage | 6 163 / 8 000 | 2 210 | 748 |
| Talents désactivés | 6 207 / 8 000 | 2 260 | 754 |
| Trio prêté, capitaine Nova | 6 112 / 8 000 | 2 174 | 763 |

Ces résultats ne démontrent pas une compréhension visuelle ou un apprentissage humain. Le régime de campagne change l’exposition, l’usage des indices et la quantité de résolution manuelle avant la sonde.

## Politique corrigée et limites

Le nouveau `Agent124` attache les souvenirs de refus, de pièces bloquées et les suites d’échecs à l’identité réelle du plateau et à sa géométrie visible. Les retries du même plateau conservent ces souvenirs ; les règles apprises, la fatigue, la frustration et les compteurs accumulés restent transférables. Une régression reproduit deux vrais plateaux au même masque et aux mêmes portes, mais où une pièce bloquée dans le premier est libre dans le second. La politique historique confondait ces situations ; le nouveau lot ne les confond plus.

**Une comparaison de moyennes 1.23 → 1.24 ne serait pas causale : le gameplay, la mémoire du modèle, les profils générés et la graine changent.** Les quatre conditions du présent lot utilisent toutes la même politique corrigée. Les anciennes preuves ne sont ni réécrites ni renommées.

Les pièces, directions, barrières et relations de clés sont déjà identifiées dans les observations. Une cause affichée est reconnue avec certitude pendant son exposition. L’agent ne lit pas des sprites, n’éprouve pas d’émotion et ne comprend pas du texte comme une personne. Les formules de temps, fatigue, disponibilité et retour restent des hypothèses non calibrées. Les étiquettes de QI des profils sont fictives, indépendantes et jamais utilisées par la politique. Aucun participant humain ni taux de rétention humain n’est mesuré.

Les seuls accès à une continuation certifiée sont les aides natives explicites et les contrôles oracle séparés ; elle n’entre jamais dans la recherche bornée de l’agent. La preuve d’absence d’oracle dans les classes de politique fait partie des tests.

## Reproduire et vérifier

Commande exécutée (un dossier existant est refusé) :

```text
bash scripts/run-cognitive-lab-v124.sh --profiles 100 --repetitions 20 --seed 2026103102 --output NOUVEAU_DOSSIER
```

Le temps hôte mesuré de 20,47 secondes décrit uniquement l’exécution informatique. Il ne représente pas la durée d’une partie ou d’une session humaine.

- [Protocole approuvé et gel de règles](protocol.json), [précontrôle des anciennes preuves](preflight.json), [log original](execution-original.log), [reçu d’exécution](execution-receipt.json).
- [Validation exhaustive](validation.json), [analyse descriptive et quantiles](analysis.json), [comparaisons et sondes](run-2026103102/paired-comparisons.json).
- [Résumé du run](run-2026103102/summary.json), [provenance et 68 hashes de sources](run-2026103102/provenance.json), [preuves hôtes du modèle et des contrôles](../companions/host-contracts.json).
- [11 CSV compressés et autres sorties brutes](run-2026103102/), [manifeste des preuves](manifest.json).
- [Runner](../../../../scripts/run-cognitive-lab-v124.sh), [validateur](../../../../scripts/verify-cognitive-lab-v124.py), [analyseur](../../../../scripts/analyze-cognitive-lab-v124.py).

Provenance SHA-256 : `aa7cdefd473e14dcb1c7125091c32dee9c4603361f2441067df98af3582d14cf`. Les sorties et leurs tailles sont vérifiées contre cette provenance. Le manifeste de ce dossier couvre également le rapport et les reçus. Aucun build, déploiement ou publication Git n’a été effectué par cette simulation.
