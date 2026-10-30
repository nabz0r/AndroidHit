# Luma Drift — du laboratoire à une aventure lisible et vivante

Étude du 30 octobre 2026, Europe/Luxembourg. Base inspectée : Android 1.21.0-rc1, code 29 ; dépôt `1f403cdbc6fec5b6fe2b4291c930fa6cde3f446b` avant cette étude. **Ce document propose le prochain chantier ; il ne décrit pas une nouvelle version Android livrée.**

La demande associe trois objectifs : exploiter davantage les statistiques du laboratoire, tirer des leçons précises de Crash Fever et des recherches cognitives, et obtenir une interface réellement dynamique. La dernière précision du propriétaire donne une priorité concrète : clés/cadenas plus visuels, déplacement du compagnon sur la carte et minimap.

## 1. Ce que les données changent dans nos priorités

La nouvelle extraction lit les **305 510 tentatives et 9 000 trajectoires**, conserve les trois scénarios séparément et rapproche CSV, agrégats et sources. [Résultats avec numérateurs, dénominateurs et empreintes](lab-analysis.json) · [Script reproductible](analyze-lab.py).

| Constat du scénario actuel | Mesure | Décision à examiner |
| --- | --- | --- |
| Deux pics concentrent les échecs de budget | U43 + U48 : **618 / 1 002**, soit **61,7 %** des tentatives épuisées, pour **2 275 / 109 932**, soit **2,1 %** des tentatives | Examiner l'anticipation des relais et les transitions de difficulté autour de ces passages avant d'aplatir toute la campagne |
| Les corrections coûtent surtout aux mêmes endroits | U43 + U48 : **33 739 / 59 977 Coins**, soit **56,3 %** des dépenses d'Undo | Séparer coût de récupération, incompréhension et choix stratégique ; aucune hausse de prix n'est déduite |
| L'apprentissage renforcé résout peu ces pics | U43 : 383/1 393 tentatives épuisées → 414/1 522 ; U48 : 235/882 → 252/986 | L'aide après un blocage physique ne suffit probablement pas à traiter les conséquences différées d'un coup légal ; hypothèse à tester sur le vrai moteur |
| Les coquilles occupent beaucoup de gestes | **393 644 / 1 160 522**, soit **33,9 %** des interactions ; **43,6 %** entre U11 et U20 | Comparer suppression de gestes répétitifs, regroupement visuel et véritable choix de cible ; ces données ne mesurent pas l'ennui |
| U11 n'est pas le mur observé | **0 / 2 975** échecs budget ; 8 tentatives censurées à l'horizon | Introduire clairement les éléments reste utile, mais un long tutoriel U11 n'est pas notre priorité de difficulté |
| Le scénario pédagogique aide inégalement | 100/100 profils réduisent leur part de blocages ; 88 progressent davantage, 9 moins, 3 autant | Examiner les parcours et le transfert, au-delà d'une moyenne générale |

Les premières tentatives des mêmes trajectoires arrivées à U43 dans les deux scénarios donnent 290/926 épuisements contre 273/926 ; à U48, 173/576 contre 157/576. Cela évite une partie des différences de composition, mais reste une sélection des trajectoires parvenues au niveau dans les deux variantes.

Les 25 profils au coefficient d'apprentissage le plus faible gagnent en moyenne 2,12 niveaux et perdent 2,04 points de part de blocages ; les 25 plus élevés gagnent 0,62 niveau et perdent 0,80 point. Ce sont des strates de paramètres générés, pas des segments humains ni des catégories d'âge ou de QI.

### Précautions nécessaires pour choisir une amélioration

- **Une tentative inachevée n'est pas un échec.** 2 348 des 3 350 tentatives non terminées du scénario actuel sont censurées par l'horizon de huit jours. La plus haute tentative de ce scénario atteint U85 ; U88 est le maximum tous scénarios confondus. Le périmètre de catalogue U1–U400 ne constitue pas une couverture de jeu jusqu'à U400.
- **Les CSV ne contiennent pas la cause de chaque blocage.** Les 300 traces sélectionnées sont limitées aux premiers profils et à certaines situations. Impossible d'affirmer aujourd'hui que « x % des blocages viennent des clés » pour toute l'étude.
- **Les motifs d'arrêt ont un défaut d'étiquette.** Le code peut conserver `budget_exhausted` après un échec non terminal si le plafond quotidien arrête ensuite la boucle. Les 266 étiquettes ne désignent ni les 1 002 tentatives épuisées, ni 266 journées à trois échecs. Les CSV comptent seulement 11 journées à trois échecs. L'audit distingue les 255 étiquettes persistantes inférées et les plafonds explicites. Corriger l'export avant toute nouvelle interprétation comportementale ; conserver le jeu historique tel quel.
- **Changer le nombre de taps change l'exposition.** Avec un plafond quotidien de décisions, retirer des taps peut mécaniquement permettre davantage de niveaux. Les prochains essais doivent présenter à la fois des horizons de décisions comparables et des horizons d'actions productives comparables.
- **Le scénario `teaching_support` modifie l'agent.** Il ajoute 0,16 à sa connaissance initiale et multiplie son apprentissage par 1,7. Il ne teste pas une interface Android précise. Les talents, la collection, les expéditions, la maison et leur rendu restent exclus du laboratoire actuel.

## 2. Les mécanismes des références à retenir

### Crash Fever : une situation donne du sens au personnage

Le Festival d'octobre 2026 introduit des panneaux qui réduisent le maximum de taps ; une capacité identifiée permet de restaurer ce maximum. Le lien **situation → capacité → conséquence** est explicite. L'adaptation à Luma consiste à créer des contextes où lire, réparer ou intervenir répondent à des besoins différents. La documentation officielle ne mesure pas l'effet causal de cette mécanique sur la rétention. [Annonce officielle](https://crashfever.com/campaign/2026093045526/).

Traversal retire des étapes suivantes les unités utilisées pour terminer une étape. C'est un exemple de ressource qui traverse plusieurs puzzles et donne un intérêt à la diversité d'une collection. Pour notre premier chapitre, un trio prêté et une réserve limitée peuvent examiner ce type de décision sans demander une collection déjà complète. [Règles officielles](https://crashfever.com/faq/2023031632231/).

Halloween relie quêtes, personnages, missions, échanges et décor. Luma peut reprendre cette cohérence de thème et d'usage : une petite aventure qui change visiblement le lieu de retour. Les événements, leur cadence et leurs récompenses ne démontrent pas à eux seuls un effet commercial. [Programme officiel du 28 octobre 2026](https://crashfever.com/campaign/2026102845706/).

### Recherche cognitive : faire constater une maîtrise et préserver le choix

Brändle, Wu et Schulz (2025) analysent notamment 7 994 niveaux de Robozzle et 376 341 votes, avec deux expériences supplémentaires. Difficulté intermédiaire, cohérence avec les attentes et réussite contribuent aux préférences observées. Proposition Luma : **découverte → réemploi autonome → combinaison → défi facultatif**. Le progrès d'apprentissage des historiques de jeux est simulé ; il ne faut pas le confondre avec une mesure longitudinale individuelle. [Article et PDF des auteurs](https://hcai-munich.com/pubs/Braendle2025Fun.pdf).

Andersen et collègues (CHI 2012) comparent huit tutoriels sur trois jeux et plus de 45 000 joueurs. L'aide bénéficie au jeu le plus complexe, sans améliorer significativement l'engagement des deux jeux plus simples. Conséquence : explications courtes lors des nouveautés, puis occasions de jouer sans aide ; un tutoriel plus long n'est pas une amélioration automatique. [Publication des auteurs](https://grail.cs.washington.edu/projects/game-abtesting/chi2012/).

Ryan, Rigby et Przybylski (2006) associent compétence et autonomie perçues au plaisir et à l'envie de poursuivre dans quatre études de jeux. Cela motive des décisions dont le joueur comprend la conséquence et plusieurs objectifs valables. Ce résultat ne prouve pas qu'ajouter des options, des monnaies ou des récompenses accroît mécaniquement le plaisir. [Article original](https://selfdeterminationtheory.org/SDT/documents/2006_RyanRigbyPrzybylski_MandE.pdf).

Il n'en découle aucun taux de réussite universel, aucune promesse d'entraînement du QI et aucune estimation de millions de joueurs.

## 3. Priorité du propriétaire : clés, cadenas et déplacement

### État réel de la 1.21

La carte conserve un ruban vertical de coordonnées fixes. Le capitaine est dessiné à côté du niveau courant ; il ne marche pas sur les liens. Les sauts changent directement le défilement. Aucun dispositif de minimap n'a été trouvé dans la source principale. Les nuages et les halos animés enrichissent le décor, sans assurer cette continuité de navigation. [SagaMap](../../../app/src/main/java/com/androidhit/lumadrift/ui/SagaMap.java) · [Périmètre de la carte 1.21](../../validation/v1.21/journey-map-source-checks.md).

La clé est un petit badge numéroté ; les cadenas utilisent une représentation jaune similaire et disparaissent presque immédiatement après la sortie de leur clé. Le moteur connaît pourtant la relation exacte d'une clé avec toutes ses barrières. Les quatre directions de marche existent déjà dans les dessins du compagnon. [Association clé/barrière](../../../app/src/main/java/com/androidhit/lumadrift/engine/Barrier.java) · [Animation disponible](../../../app/src/main/java/com/androidhit/lumadrift/ui/CompanionMotion.java).

### A. Clé et cadenas : une relation visible

1. Donner à chaque paire un motif et un numéro communs, reconnaissables sans dépendre de la couleur ; agrandir la clé en conservant la flèche de la pièce.
2. Inspecter une clé ou un cadenas révèle la clé et **tous** les cadenas liés, avec une liaison temporaire. La sélection ne joue pas le coup.
3. Une sortie réellement validée déclenche une impulsion clé → serrures, l'ouverture de l'anse puis l'effacement de l'obstacle. L'état moteur est enregistré avant sa présentation.
4. Undo referme lisiblement les serrures qui redeviennent actives. Les sorties par talents et les reprises après interruption suivent le même état réel.
5. Un blocage expose sa cause précise à l'écran avec son coût. Cette cause est déjà calculée pour l'accessibilité.

Le mouvement réduit conserve motif, numéro, contour stable et changement d'état immédiat. L'animation ne doit jamais retarder une commande nécessaire ou masquer les autres pièces.

### B. Compagnon, caméra et minimap : des fonctions distinctes

- **Glisser la carte** déplace la caméra ; le compagnon garde sa position sur le monde.
- **Choisir un niveau accessible** montre immédiatement sa fiche et fait marcher le compagnon sur les chemins. « Jouer » reste disponible sans attendre la fin du trajet. Les longues distances ont une transition abrégée.
- **Toucher la minimap** cadre une région ; cela ne téléporte pas le compagnon et ne lance pas un niveau. Un repère « Me suivre » recadre sa position.
- Le compagnon, la destination choisie, la progression débloquée et le cadre de caméra ont des représentations différentes. Se déplacer visuellement ne gagne pas un niveau et ne consomme pas d'énergie.
- Après une victoire, l'ouverture du chemin suivant et le trajet du compagnon relient les deux puzzles. Le retour à la carte conserve ce contexte.

Le périmètre visé est une carte de zone en deux dimensions, avec rendu limité aux objets visibles. La minimap locale affiche les grands chemins et le cadre de caméra ; une vue des dix biomes évite de miniaturiser 1 000 numéros illisibles. Une première livraison sur un secteur peut établir le comportement avant d'étendre la composition aux dix zones.

### C. Le dynamisme suit les actions

Pression, relâchement, déplacement, ouverture et retour ont des réactions cohérentes. Une destination réagit quand elle est sélectionnée ; le compagnon s'oriente dans son déplacement ; la caméra poursuit avec une inertie bornée et s'arrête dès que le joueur reprend la main. Les sons courts ont une fonction et suivent les réglages existants. Une effervescence permanente n'est pas un critère de réussite.

Ce lot demande une vérification par gestes et vidéo Android : caméra interrompue puis reprise, minimap tactile, petit écran, grande police, lecteur d'écran, mouvement réduit et sauvegarde/reprise. Le laboratoire sans rendu peut vérifier les informations exposées et les transitions, mais pas qualifier la sensation de mouvement ou le confort tactile.

## 4. Le chantier de profondeur qui vient ensuite

Les équipes, six familles, Résonance, branches d'expédition et formes par copies existent déjà. Proposer de les ajouter serait redondant. La différence recherchée est que **le contexte et le moment d'usage créent une décision**, au-delà d'aides qui montrent ou exécutent un préfixe certifié.

Proposition : un chapitre autonome de **12 nouveaux layouts**, répartis en deux petites expéditions de six layouts possibles. Une partie traverse quatre plateaux : entrée, deux bifurcations, finale. Les routes Clés et Relais offrent des dépendances différentes ; toutes restent résolubles manuellement. Chaque nouveau layout conserve les règles U et reçoit son propre certificat.

Un trio prêté donne trois rôles distincts : information, réparation et exécution. Éviter de mettre GUIDE et TRAIL au même coût dans ce trio, car le second peut dominer le premier. La réserve de talents peut traverser les quatre plateaux, avec un usage limité de chaque talent par parcours. Les capacités 4/6/8 sont des variantes à comparer, pas un équilibre choisi par avance. Ce mode exige un registre et des sauvegardes propres ; il ne modifie pas silencieusement les règles de Charge de la campagne.

Une finale inédite réemploie les mécanismes précédents. La progression principale annoncée reste obtenable sans objectif de maîtrise ; le souvenir du chapitre représente réellement la route achevée. L'évolution par copies conserve son contrat actuel : elle ne reçoit pas une hausse de puissance de talent qui n'existe pas dans la 1.21.

## 5. Comparer des changements réellement représentés

Ordre de travail recommandé :

1. **Lot lisibilité et navigation** : association clé/cadenas, cause visible, déplacement du compagnon, caméra, minimap et retours tactiles/sonores cohérents. C'est la priorité explicite du propriétaire.
2. **Lot anticipation** : réunir la prévision de portes déjà disponible et la lecture des pièces affectées, puis travailler U43/U48 et leurs prérequis. Aucun état caché du solveur ne doit entrer dans la perception des agents.
3. **Lot chapitre tactique** : véritables routes, prêt de trio et réserve partagée ; comparer les options de ressource après intégration de leurs mécanismes dans le labo.

Les nouvelles expériences conservent mêmes profils, graines, plateaux et récompenses quand on étudie seulement l'aide. Elles distinguent épisodes d'apprentissage, test sans aide et géométries de transfert. Les essais contrôlés au milieu de campagne doivent annoncer leurs états d'accès et ne pas être présentés comme des parcours de nouveaux joueurs depuis U1.

Mesures : épuisement sur première tentative, erreurs répétées par règle, coût d'Undo, information demandée, aide encore nécessaire au transfert, sorties manuelles/automatiques, moment d'usage du talent, réserve inutilisée et dominance d'une stratégie sur l'autre. Un agent qui résout tout automatiquement ne constitue pas à lui seul une amélioration de jeu.

Ajouter un export exhaustif des causes de blocage et corriger les étiquettes d'arrêt avant les nouveaux essais. Garder les résultats historiques intacts, et l'oracle de référence séparé des agents à perception limitée. La recherche en ligne et les statistiques servent dès maintenant à concevoir et éliminer des variantes ; leur résultat ne remplace pas une mesure de plaisir ou de retour humain.

## Reproduire cette extraction

Depuis la racine du dépôt :

```sh
python3 docs/research/cognitive-design-2026/analyze-lab.py
```

Le script ne relance pas la simulation et ne modifie ni APK, ni sauvegarde, ni donnée source du labo. Il vérifie les comptes de tentatives, trajectoires, actions, dépenses et motifs de fin contre les agrégats, puis réécrit uniquement le résultat descriptif local. Le JSON conserve les empreintes des données et des sources utilisées pour interpréter leurs unités.
