# Clarté, parcours et direction artistique — audit 1.22

Le geste de base manque de priorité. Le plateau, les préparations élémentaires,
les talents, l'équipe, la collection et l'équipement demandent des apprentissages
qui se cumulent, avec des mots et des parcours différents. La prochaine refonte
doit réduire ces tâches et clarifier les conséquences avant d'ajouter des effets.

Cet audit répond aux retours sur les éléments feu/glace/eau, l'interface et les
allers-retours plateau → compagnons → équipement. Il conserve les résultats
défavorables et distingue moteur, agents synthétiques, écrans Android et proposition.

## Travail réellement effectué

Source étudiée : `557724f56e45aa8df1cfab629f20848e0f3c4fd0`, version Android
`1.22.0-rc1`, code 30, APK SHA256
`9b2593388de22b1f50f49bea05b9b02008d4574f5cbb9012fb0df5a64c47a44e`.

| Périmètre | Exécution et portée |
| --- | --- |
| [Nouveau run sur le modèle courant](simulation/README.md) | 21 000 trajectoires, 766 177 tentatives ; 100 profils synthétiques × 30 répétitions × 7 conditions, graine 2026103001 |
| [Comparaison sans coques](experiment-no-shell/README.md) | 42 000 autres trajectoires et 1 703 039 tentatives : 21 000 reproduisent le run source à sept conditions, 21 000 retirent les coques dans une copie privée du modèle |
| [Structure des puzzles](design/README.md) | 1 000 certificats de résolution rejoués et 2 818 états examinés sur U1–U100 ; ces contrôles ne sont pas des joueurs |
| [Parcours natif](native/README.md) | APK C exact ; parcours continu de 20 gestes, 39 assertions et 11 PNG originaux inspectés ; aucun solveur, achat, équipement ou injection de progression |
| [Proposition de navigation](prototype/README.md) | 23 observations réelles du navigateur, quatre largeurs, fiche contextuelle, essai/annulation/application et reprise du plateau |
| [Lecture visuelle et références](visual-research/README.md) | Quatre captures Android exactes et quatre références primaires ; les bots ne perçoivent pas les sprites |

Les 42 000 exécutions expérimentales réutilisent les profils et graines du nouveau
run. Il ne s'agit pas de 63 000 personnes ou de 63 000 profils indépendants. Les
sondes sans aide et les chapitres tactiques ont leurs propres dénominateurs dans
les rapports ; ils ne sont pas additionnés aux tentatives de campagne.

## Les problèmes à traiter

**Les trois éléments ajoutent des préparations plutôt que trois décisions.**
Glace et feu demandent deux pressions gratuites, eau une. Leur opération est la
même : retirer une couche. Dans le témoin, ces gestes représentent
384 330/1 142 634 interactions, soit **33,6 %**, et **45,4 % à U11**. Les trois
arrivent ensemble à U11, pendant la leçon des clés et au début du coût d'énergie.
Visuellement, les fissures traversent la flèche, les flammes dépassent vers la
serrure voisine et les compteurs ressemblent aux numéros de clé. Un badge de
relais et la silhouette de clé occupent aussi le même coin sur la clé-relais U29.

**La courbe comporte des ruptures que le feedback immédiat ne corrige guère.**
Le premier choix légal menant à une impasse apparaît à U27 ; l'invitation à
inspecter une pièce arrive à U29. Le contrôle structurel en donne un exemple
rejouable. La marge de correction reste de trois actions sur les 1 000 niveaux
étudiés ; le budget total correspond au nombre de pièces plus trois.
Dans le témoin, U43/U48 concentrent **744/1 150 épuisements enregistrés (64,7 %)**,
pour 2 234/108 046 départs de tentative. La cause immédiate donne seulement
**+0,1567 niveau moyen par trajectoire** dans ce modèle. La limite de classement
des 52 horizons déjà à budget zéro, toutes conditions confondues dont dix dans
le témoin, est conservée ; la sensibilité porte la part
U43/U48 à 752/1 160, soit 64,8 %.

**Les écrans changent de tâche sous le même personnage.** Depuis le plateau,
Nova explique son indice de puzzle. Depuis la collection, sa fiche « Aperçu »
explique une révélation de rencontre d'expédition. Pour aller de cette fiche à
la tenue dans le parcours observé, le chemin passe par liste, hub Collection et
Accueil. Le hub possède aussi un accès direct au Dressing : ce détour n'est
pas une preuve du chemin minimal nécessaire. La partie
n'est pas perdue : les six champs essentiels du puzzle restent exacts, et seul
le temps de jeu avance de 4,405 secondes dans le parcours continu observé.
Le problème est l'orientation et la signification des actions. Équiper une
apparence ne compose pas l'équipe active ; modifier l'équipe prépare le prochain
essai. Ces différences doivent être visibles au moment du choix.

**Le plateau partage son attention avec trop de systèmes.** Monnaies, budget,
trois personnages, charge, résonance, Focus/Enchaînement et chrono repoussent les
pièces vers le bas. Le budget est affiché deux fois. Les aides emploient encore
des textes de correction gratuite qui ne décrivent pas complètement le coût
actuel. Une flèche légale peut aussi être une mauvaise décision : expliquer un
tap bloqué ne suffit pas à enseigner ce cas.

## Ce que montre l'expérience sans coques

Les deux variantes ont les mêmes profils, graines, géométries, budgets et règles
de disponibilité. Le flag désactivé reproduit les six CSV du nouveau run **à
l'octet près**. U1–U10 et les CSV des chapitres restent identiques entre variantes.
Seule l'initialisation des coques change dans une copie privée du modèle ; le
code Android et les fichiers publiés ne changent pas.

Condition « cause immédiate », **3 000 trajectoires par variante** :

| Mesure du modèle | Avec coques | Sans coques |
| --- | ---: | ---: |
| Niveaux moyens terminés | 35,020 | 44,823 |
| Interactions exécutées | 1 146 563 | 941 031 |
| Épuisements enregistrés | 1 144 | 504 |
| Journées touchant le cap administratif de 120 décisions | 3 140 / 24 000 | 35 / 24 000 |
| Journées arrêtées par énergie vide | 122 / 24 000 | 5 220 / 24 000 |
| Sondes sans aide terminées | 9 423 / 12 000 | 8 474 / 12 000 |

Retirer les préparations libère le parcours **dans ce modèle**. L'énergie devient
le frein suivant. Les sondes changent elles aussi d'environnement, et l'exposition,
les transitions d'apprentissage et les trajectoires aléatoires peuvent diverger.
La baisse des sondes interdit d'annoncer un meilleur apprentissage. Ni la vitesse
du modèle ni sa formule d'arrêt ne mesurent plaisir, compréhension ou rétention
humaine. Le rapport conserve les contreparties et les contrôles négatifs.

## Direction du prochain lot

1. **Le puzzle comme tâche principale.** Un objectif, un budget, des flèches
   dégagées, des clés et des relais identifiables. L'inventaire, les monnaies et
   les détails de progression sortent de la surface de réflexion.
2. **Retirer les cinq préparations répétitives du Voyage.** Garder un élément
   seulement s'il crée un choix distinct et enseigné. Requalifier ensuite les
   budgets, l'énergie et les sondes à environnement constant ; ne pas transformer
   le gain synthétique en promesse de rétention.
3. **Une fiche contextuelle depuis le plateau.** Talent, effet, coût et retour
   au même puzzle. La composition de l'équipe et les équipements de gameplay se
   préparent entre deux essais. Les tenues conservent « En essai », « Porté » et
   « Appliquer », avec annulation explicite et contexte du compagnon conservé.
4. **Enseigner les conséquences avant le premier piège légal.** Introduire un
   petit choix de relais observable, sa correction et son coût réel, puis laisser
   pratiquer avant de cumuler clé/relais/portes. Revoir U27, U29, U31, U43 et U48.
5. **Une grammaire visuelle commune.** Direction au centre ; clé, verrou et effet
   de relais à des emplacements distincts ; aucune décoration sur la flèche.
   Garder les compagnons existants comme ancrage et réserver leurs réactions
   aux sorties, ouvertures et réussites. Le calme pendant la réflexion et une
   conséquence nette après le geste constituent une direction à tester.

La [proposition interactive](prototype/README.md) matérialise la hiérarchie et
le retour contextuel. Elle reste schématique : elle ne constitue ni une DA
finale, ni un moteur complet, ni une version Android intégrée. Les pastilles de
tenue servent à examiner l'essai/application, pas à valider des vêtements.

## Preuves et limites conservées

- [Manifest du run courant](simulation/manifest.json) et
  [analyse exhaustive](simulation/analysis.json).
- [Manifest de l'expérience](experiment-no-shell/manifest.json),
  [comparaison](experiment-no-shell/comparison.json) et copie privée du modèle.
- [Audit structurel et sources](design/source-binding.json).
- [Natif : index exact et nettoyage](native/index.json) ; l'observateur est
  désinstallé, l'AVD arrêté normalement, ses données conservées.
- [Proposition : interactions, tailles et captures](prototype/browser/report.json).
- [Sources primaires, dates et limites](visual-research/README.md).

Aucun résultat de cet audit n'établit une mesure humaine de compréhension,
d'émotion, de QI ou de rétention. Les tests structurés connaissent déjà les
directions et les règles ; ils ne peuvent pas juger à notre place un sprite ou
un parcours de menu. Le diagnostic visuel et les séquences natives conservent
donc leurs preuves distinctes.
