# Cognitive Lab 1.23 — protocole avant gel du run

Le protocole exécute le moteur courant du Voyage **sans préparations
élémentaires**, au moyen de la même factory `ElementalShell121.createJourney123`
que le natif. Le Board, les géométries, le budget politique6 et sa marge +3,
l'économie et le registre d'énergie restent ceux du jeu courant. Les anciens
constructeurs de laboratoire gardent explicitement `create`, pour préserver
l'exécution historique 1.21/1.22. Les audits et données gelés ne sont pas réécrits.

## Configuration prévue

- Graine nouvelle : **2026103002**.
- **100 profils générés ×20 répétitions ×4 conditions =8 000 trajectoires**.
- Huit jours modélisés ; cap administratif de 120 décisions/jour et de
  110 décisions/tentative ; périmètre configuré U1–U400.
- Les mêmes paramètres de profil, graines d'entrée et géométries sont appariés.
  Les états, expositions et tirages ultérieurs peuvent diverger avec les actions.
- Zéro humain ; paramètres, utilités, durées, reconnaissance et apprentissage
  sont des hypothèses non calibrées.

| Condition | Différence testée |
| --- | --- |
| `manual_native_123` | Mécanique native 1.23 sans talent activé ; cause immédiate, coach U1, Focus gagné et avertissement d'impasse natifs |
| `cause_hidden_123` | Même moteur et politique, explication de la dernière cause masquée ; contrôle expérimental non livré |
| `loaned_mixed_123` | Trio Nova/Hoot/Orbit entièrement prêté ; coûts et effets natifs, sorties automatisées séparées ; acquisition et propriété exclues |
| `energy_unlimited_123` | Réservation d'énergie de session contournée ; sensibilité expérimentale, sans modification des Coins, budgets ou aides et sans recommandation de balance native implicite |

La reconnaissance d'une règle pendant le cue de 3,2 secondes reste supposée
parfaite temporairement. Aucun bonus de connaissance initiale ou multiplicateur
d'apprentissage n'est ajouté. Les pièces, clés, relations et directions sont
déjà structurées dans la perception du modèle : aucun sprite ni texte n'est lu.
Le nouvel enseignement par inspection, la navigation et le HUD 1.23 ne sont pas
simulés. Les talents activés utilisent le vrai exécuteur natif ; les suites
certifiées restent confinées aux aides demandées, jamais à la recherche bornée.

## Correction de classification versionnée

Après **chaque action**, l'ordre est : victoire, budget épuisé, abandon demandé,
cap administratif de tentative, puis arrêt de temps/utilité/journée. L'effet
d'échec sur la frustration est appliqué immédiatement. Seule une tentative
ouverte avec budget positif peut être censurée à l'horizon.

Une régression construit les transitions réelles de budget au dernier coup,
y compris victoire au budget zéro et collision avec un cap. Le validateur
exhaustif exige que chaque épuisement soit attribué à son vrai jour de fin,
et refuse toute censure à budget zéro. Cette correction est une nouvelle
sémantique de protocole, pas une relabellisation silencieuse des anciens runs.

## Probes et contrôles séparés

Tous les régimes sont évalués dans **le même environnement sans coques**,
sans coach, Focus, avertissement d'impasse, cause visible ou compagnon. Fatigue,
frustration et mémoire sont réinitialisées ; seuls les états de règle finaux
sont transplantés. Les quatre géométries retenues sont U401/U573/U721/U1000.
Les politiques continuent à recevoir le feedback et à s'adapter pendant chaque
sonde. Le contraste porte donc sur des politiques adaptatives initialisées
différemment, pas sur une lecture figée de l'état appris avant la sonde.

- **32 000 sondes entraînées** : 8 000 trajectoires ×4 niveaux.
- **8 000 sondes froides** : 100 profils ×20 répétitions ×4 niveaux, exécutées
  une seule fois et réutilisées comme référence commune aux quatre régimes.
- Donc **40 000 lignes de sonde réellement exécutées**, sans multiplier
  artificiellement les observations froides par le nombre de régimes.
- **104 contrôles oracle** exécutent séparément des solutions natives sur
  U1–U100 et les quatre sondes. Ils certifient le moteur et ne sont ni des
  trajectoires d'agents, ni des données d'entraînement, ni des humains.

Les expositions d'entraînement diffèrent encore entre régimes. Le contraste
entraîné/froid est un contrôle fonctionnel dans ce modèle, pas une preuve de
transfert cognitif humain. Les états des quatre règles communes sont exportés
séparément de la règle de coque devenue inutilisée ; la moyenne historique de
cinq règles n'est pas interprétée comme une mesure d'apprentissage comparable.
La formule historique de durée à cinq règles est conservée comme hypothèse et
ne justifie aucune mesure de vitesse humaine.

## Traces, dénominateurs et provenance

Les CSV couvrent toutes les tentatives, trajectoires, journées et causes de
blocage ; les liens entre cue et sortie manuelle sont exhaustifs dans un CSV
distinct. Une sortie par tap ayant suivi un indice reste manuelle **assistée**.
Les sondes sans aucune aide sont le seul périmètre appelé sans aide.

Les sorties automatisées des talents sont séparées. U27/U43/U48, les Undo/Coins,
les caps administratifs, l'énergie et les censures sont analysés avec leurs
numérateurs et dénominateurs. Les moyennes de taux par trajectoire restent
distinctes des taux calculés sur toutes les interactions regroupées.

Les hashes du moteur et du runner sont pris avant/après l'exécution et doivent
être identiques. `LumaView.java` n'est pas compilé par ce driver et est exclu du
gel d'exécution : sa qualification finale et son binding de contexte UI sont
séparés. Le run final attend le gel commun du moteur et des sources de laboratoire.

## Commandes

```sh
bash scripts/test-cognitive-lab-v123.sh
bash scripts/run-cognitive-lab-v123.sh --profiles 100 --repetitions 20 \
  --seed 2026103002 --output docs/validation/cognitive-lab-v123/latest
python3 scripts/verify-cognitive-lab-v123.py --require-current-source \
  --receipt docs/validation/cognitive-lab-v123/validation.json
```

Le runner refuse d'écraser un répertoire de sortie existant. La CI vérifie les
données préenregistrées sans relancer la grande campagne. Les checks historiques
1.22 vérifient leurs archives et factories préservées ; ils n'exigent pas que le
moteur courant soit encore une release 1.22.
