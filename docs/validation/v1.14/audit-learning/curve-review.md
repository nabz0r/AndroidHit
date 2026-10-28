# Courbe N — revue du corpus RC2

Corpus `0a6de06054a377d37b5b8ab1e2374cdbe694c03983adeb1b5714f86aec54dafc`. Les rapports RC1 et leur source complète restent dans [audit-learning-rc1](../audit-learning-rc1/). Aucune preuve native RC1 n'est présentée comme une validation RC2.

La difficulté croît par paliers et par fréquence des défis. Elle n'est pas monotone par niveau : des plateaux Normal restent intercalés jusqu'à N1000 et chaque pic d'épisode est suivi d'une récupération mesurée. Les étiquettes correspondent aux scores recalculés après les nouvelles portes initiales.

| Fenêtre | Taille médiane | Pièces médianes | Minimum médian | Switchs médians | Clés médianes | Décisions médianes | Score médian | Hard + Expert | Récupérations |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 1–100 | 5 | 12 | 14 | 3 | 0 | 2 | 31.5 | 10 | 22 |
| 101–200 | 6 | 16 | 18.5 | 5 | 0 | 7 | 58.5 | 30 | 30 |
| 201–300 | 6 | 18 | 20 | 8 | 0 | 9 | 62.5 | 40 | 35 |
| 301–400 | 6 | 18.5 | 21 | 8 | 1 | 10 | 66 | 40 | 40 |
| 401–500 | 7 | 18 | 21 | 9 | 1 | 11.5 | 79.5 | 50 | 23 |
| 501–600 | 7 | 18 | 21 | 9 | 0 | 11.5 | 73.5 | 50 | 28 |
| 601–700 | 7 | 19 | 21 | 9 | 0.5 | 14 | 90 | 60 | 29 |
| 701–800 | 7 | 18 | 21 | 9 | 0 | 15 | 94 | 60 | 28 |
| 801–900 | 7 | 18 | 21 | 9 | 1 | 16 | 100 | 70 | 20 |
| 901–1000 | 7 | 19 | 21.5 | 9 | 1 | 17 | 104 | 70 | 24 |

Médianes de score par 200 : **46 → 66 → 79.5 → 90 → 100**. Le creux RC1 N401–600 est supprimé. Une baisse locale de 79,5 à 73,5 subsiste entre les fenêtres N401–500 et N501–600 ; les deux ont 50 défis, et la médiane des décisions reste 11,5. Ce n'est donc pas une promesse de croissance pour chaque fenêtre de 100.

| Paliers après introduction | Hard/Expert par épisode de 10 | Normaux minimum par épisode |
|---|---:|---:|
| 31–40 | 0 | 10 |
| 41–60 | 1 | 9 |
| 61–100 | 2 | 8 |
| 101–200 | 3 | 7 |
| 201–400 | 4 | 6 |
| 401–600 | 5 | 5 |
| 601–800 | 6 | 4 |
| 801–1000 | 7 | 3 |

Le premier Hard est N49 ; le premier Expert N429, après les introductions et l'accumulation de pratique. Les 38 Experts sont tous après N400. La série maximale Hard/Expert est **4**, contre 153 en RC1. Chacun des 97 épisodes garde trois états initiaux avec une sortie libre ; les 96 épisodes contenant un Hard terminent par un Normal dont le score baisse d'au moins 20 % après le pic.

Le pont N30→N31 passe de 10 à 12 pièces, de 10 à 13 actions minimales et de 38 à 42 points. N31 offre deux sorties immédiates et exige une ouverture manuelle ; N32, N33 et N34 exigent respectivement deux, trois et quatre ouvertures. C'est une hausse graduelle de charge avec les mécaniques déjà introduites, pas une nouvelle règle. Après N30, les plus grands bonds positifs sont **35 points, deux pièces, quatre actions minimales et deux ouvertures**. Ces bornes sont vérifiées sur chaque transition.

**291 des 970 bases** ont des portes initiales supplémentaires ouvertes. Taille, positions, flèches, switchs et associations clé/sceau restent exactement ceux de leur source L ; l'état initial N est un sur-ensemble des portes ouvertes L. Chaque état est atteint depuis L par ces seules ouvertures, puis son minimum et ses métriques sont recalculés. Les 30 introductions sont inchangées. Au total, 321/1000 plateaux commencent avec une sortie libre.

Le score combine les bifurcations de coût le long d'un certificat optimal, les ouvertures nécessaires, les clés, la pénalité maximale d'un choix et la profondeur des blocages. Il ne mesure pas la compréhension humaine ni le taux de victoire. Les budgets restent réels : minimum +5 jusqu'à N16, +4 jusqu'à N100, puis +3 ; les erreurs productives peuvent donc faire perdre. La progression donne davantage de décisions et de défis tardifs, avec une densité physique plafonnée, plutôt que de grossir indéfiniment les grilles.

Les [contrats indépendants](audit-learning-contracts.json), les [97 épisodes et fenêtres détaillées](audit-curve.json) et les [branches de budget](audit-learning-branches.csv) décrivent ce corpus. Ces preuves ne remplacent pas un premier essai humain ni la qualification native RC2.
