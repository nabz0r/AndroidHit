# Nouveau run de progression — 31 octobre 2026

**Le trio prêté progresse moins dans cette politique synthétique, mais cela
n’établit aucune baisse chez des joueurs.** La politique sollicite les talents
très fréquemment et n’optimise pas l’évitement des refus : 152 281 demandes pour
643 304 décisions, dont 11 706 refus. Elle modifie aussi les expositions
d’apprentissage et les tirages ultérieurs. Ce résultat ne justifie pas un
« nerf » des talents.

Le nouveau lot a réellement exécuté **8 000 trajectoires**, produisant
**372 673 victoires sur 377 413 tentatives**, avec 2 633 398 décisions et
173 154 événements de blocage. Il utilise le moteur courant, une nouvelle
graine et un dossier isolé. Il n’a testé ni les graphismes, ni les décors,
ni les équipements, ni LumaGold, ni les pubs. Ces parcours natifs sont un
périmètre distinct confié à l’autre agent.

## Exécution et provenance

- Source : `5cc870701c8039e664d1c00a90d0586c3d00521c`, arbre app identique
  à la source artefact 1.23 C1.
- Graine nouvelle : **2026103101** ; 100 profils générés × 20 répétitions
  × 4 conditions = **8 000 trajectoires** sur huit jours modélisés.
- **64 000 journées configurées**, 8 000 états finaux de règles, 40 000 sondes
  réellement exécutées et **104 contrôles oracle séparés**.
- Les 8 000 sondes froides sont exécutées une fois, puis comparées à
  32 000 sondes entraînées. Elles ne sont pas comptées quatre fois.
- **58 sources moteur/runner** identiques avant/après et identiques à la 1.23
  gelée ; **235 fichiers préexistants** de release, public, kit et audit
  vérifiés inchangés. Aucun runner historique n’a été modifié.
- Durée hôte du lancement complet : 16,89 secondes. Ce temps informatique
  n’est pas une durée de jeu humaine.

La [préparation](preflight.json), le [journal original](run-original.log),
le [reçu d’exécution](execution-receipt.json), la
[provenance du run](run-2026103101/provenance.json) et la
[validation exhaustive](validation.json) conservent les preuves.
Le validateur trouve **zéro censure à budget nul** et attribue chaque épuisement
au vrai jour de fin. Il ne relabellise pas les données du 30 octobre.

## Quatre cohortes, les mêmes paramètres appariés

Chaque condition contient 2 000 trajectoires et 16 000 journées configurées.
Le témoin conserve le coach U1, le Focus gagné, la cause immédiate du blocage
et l’avertissement d’impasse, sans talent de compagnon activé. Cause masquée,
trio entièrement prêté et énergie illimitée sont des contrôles expérimentaux.
Ils ne changent pas le jeu livré.

| Mesure | Témoin natif sans talents | Cause masquée | Trio prêté | Énergie illimitée |
| --- | ---: | ---: | ---: | ---: |
| Victoires | 90 382 | 90 035 | 88 128 | 104 128 |
| Tentatives commencées | 91 450 | 91 112 | 89 138 | 105 713 |
| Budgets épuisés | 277 | 266 | 184 | 315 |
| Tentatives encore ouvertes à l’horizon | 791 | 811 | 826 | 1 270 |
| Niveaux terminés / trajectoire | 45,191 | 45,0175 | 44,064 | 52,064 |
| Refus de tap | 44 472 | 45 339 | 32 051 | 51 292 |
| Sorties manuelles | 572 420 | 570 012 | 454 674 | 663 208 |
| Sorties automatisées | 0 | 0 | 115 019 | 0 |
| Coins payés pour Undo | 19 466 | 19 118 | 14 768 | 22 748 |
| Énergie dépensée | 71 476 | 71 136 | 69 175 | 0, réservation contournée |

Les issues se réconcilient exactement : **372 673 victoires + 1 042 épuisements
+ 3 698 censures = 377 413 tentatives**. La censure désigne une tentative
ouverte à budget positif lorsque l’étude s’arrête après huit jours modélisés.
Elle ne désigne ni un abandon humain ni une défaite.

### Distributions par cohorte

Les quantiles sont calculés sur les 2 000 trajectoires de chaque condition,
par sélection de l’observation triée la plus proche. Les agrégats ne représentent
pas 2 000 personnes.

| Distribution | Témoin | Cause masquée | Trio prêté | Énergie illimitée |
| --- | ---: | ---: | ---: | ---: |
| Niveaux : médiane [Q25 ; Q75] | 44 [32 ; 58] | 44 [32 ; 58] | 43 [31 ; 56] | 48 [33 ; 68] |
| Niveaux : P10 / P90 | 22 / 70 | 22 / 69 | 22 / 68 | 23 / 89 |
| Niveaux : minimum / maximum | 4 / 94 | 4 / 94 | 4 / 94 | 4 / 131 |
| Décisions : médiane [Q25 ; Q75] | 313 [218 ; 409] | 312 [219 ; 409] | 314 [220 ; 422] | 339 [229 ; 484] |
| Undo : médiane [Q25 ; Q75] | 6 [1 ; 10] | 5 [1 ; 10] | 12 [6 ; 18] | 6 [2 ; 12] |
| Coins dépensés : médiane / P90 | 0 / 31 | 0 / 31 | 0 / 30 | 0 / 34 |
| Énergie dépensée : médiane [Q25 ; Q75] | 35 [23 ; 48] | 34 [23 ; 48] | 33 [22 ; 47] | 0 |

Les Undo du trio comprennent les retours du talent REWIND ; ses Coins ne
comptent que les Undo manuels payants. L’intégralité des quantiles figure dans
l’[analyse descriptive](analysis.json) et l’[analyse complémentaire](analysis-detail.json).

## Pourquoi le trio est moins performant ici

La comparaison apparie chaque profil et répétition aux mêmes graines d’entrée :
**2 000 paires, regroupées en 100 profils**. Le trio termine en moyenne
**1,127 niveau de moins** que le témoin, avec un intervalle de sensibilité
à 95 % de **[−1,499 ; −0,758]**. Cet intervalle rééchantillonne 1 000 fois
les moyennes des profils entiers ; il ne décrit aucune population humaine.
Sur les 100 profils, la moyenne baisse pour **70**, monte pour **28** et reste
égale pour **2**. La médiane de la différence par profil est −1,05 niveau,
avec Q25 = −2,70 et Q75 = +0,25.

Les données et le code donnent plusieurs mécanismes concrets :

1. **Sollicitation très fréquente.** Les demandes de talent occupent
   152 281 / 643 304 décisions, soit **23,67 %**. La politique les envisage sous
   pression — erreur mémorisée, impasse visible ou plus de huit pièces — mais
   aussi toutes les six décisions. Elle mélange les slots prêts et choisit
   probabilistiquement ; elle ne planifie pas leur meilleur enchaînement.
   REWIND exige en plus une erreur ou une impasse.
2. **Refus coûteux dans le modèle de temps.** 11 706 / 152 281 demandes,
   soit **7,69 %**, n’appliquent aucun effet. Un refus natif ne dépense ni Charge
   ni Coins, mais le simulateur lui attribue une décision et une durée/fatigue
   hypothétiques. Il ne mesure pas le temps réel du panneau Android.
3. **Coûts et contraintes natifs conservés.** Par plateau : Charge initiale 3,
   plafond 6, +1 par pièce sortie distincte, un usage par slot. GUIDE coûte 2,
   FLOW 3, REWIND 2 ; le combo PULSE coûte 0 Charge après deux familles distinctes.
   Les 50 050 GUIDE, 52 548 FLOW et 21 086 REWIND représentent
   **299 916 unités de Charge**, calculées à partir des coûts et usages réussis.
   Les 16 891 combos sont séparés. Ce n’est pas le chapitre tactique à réserve
   partagée, qui n’a pas été rejoué ici.
4. **Exposition d’apprentissage différente.** Le trio automatise
   115 019 / 569 693 sorties, soit **20,19 %**. Il conserve 454 674 sorties
   manuelles contre 572 420 pour le témoin. Le feedback apprend sur les issues
   manuelles « exit », pas sur « skill_flow/skill_pulse » : l’automatisation
   retire donc des occasions d’apprentissage manuel dans ce modèle.
   La moyenne non calibrée des quatre règles finit à 0,7465 contre 0,7782.

Au total, le trio produit 12 421 refus de tap en moins, mais 12 765 décisions
supplémentaires et moins de niveaux terminés. Les actions, états, expositions
et indices des tirages aléatoires divergent après les premiers choix :
l’appariement n’impose pas une suite d’actions identique.

**La part causale de chaque mécanisme n’est pas identifiée.** Le contraste est
l’effet total de cette politique et de ses hypothèses. Il faudrait comparer
une politique qui réserve les talents aux situations utiles et évite les refus
pour séparer cet usage imparfait de la puissance des talents. Aucune nouvelle
simulation de cette politique n’a été effectuée ici, et aucune modification
de balance native n’est proposée comme résultat acquis.

## Difficulté : arrivée, départ et épuisement

Une **arrivée** est une trajectoire distincte ayant atteint le niveau ; un
**départ** commence une tentative et peut être un retry ; un **épuisement**
termine une tentative à budget nul. Une victoire fait progresser ce niveau une
fois par trajectoire. Les niveaux tardifs ne voient que les trajectoires arrivées.

Dans le témoin, 2 000 trajectoires commencent U1 ; 1 695 atteignent U27,
1 088 U43, 877 U48 et 441 U61 pendant la fenêtre finie. Cette décroissance
combine les disponibilités, utilités, énergie et décisions du modèle. Elle ne
mesure pas un décrochage de joueurs et ne prouve pas que les niveaux non atteints
ont causé un arrêt.

| Niveau témoin | Arrivées distinctes | Départs, retries compris | Victoires | Épuisements / départs | Undo |
| --- | ---: | ---: | ---: | ---: | ---: |
| U27 | 1 695 | 1 695 | 1 685 | 0 / 1 695 | 425 |
| U33 | 1 502 | 1 591 | 1 456 | 98 / 1 591 · 6,16 % | 1 742 |
| U36 | 1 361 | 1 399 | 1 326 | 43 / 1 399 · 3,07 % | 1 353 |
| U43 | 1 088 | 1 125 | 1 069 | 39 / 1 125 · 3,47 % | 1 062 |
| U48 | 877 | 887 | 863 | 11 / 887 · 1,24 % | 416 |
| U68 | 263 | 277 | 250 | 16 / 277 · 5,78 % | 233 |

**U33, U36 et U43 réunissent 180 / 277 épuisements du témoin, soit 64,98 %.**
U33 concentre aussi 5 855 / 19 466 Coins d’Undo. U68 mérite une vérification,
avec seulement 263 trajectoires arrivées. U27 a zéro épuisement ici mais 425 Undo :
cela ne signifie pas absence de difficulté ou de piège.

Les refus ont des causes mécaniques distinctes. À U43, **719 / 735 refus**
sont dus à une pièce sur le chemin ; il n’y a aucun refus de barrière.
À U48, les 340 refus se répartissent en 148 barrières, 119 pièces et 73 portes
fermées. À U29, 908 / 1 238 refus concernent une barrière. Ces comptages ne
prouvent pas à eux seuls la cause d’un épuisement ultérieur.

### Progression conditionnelle à U43 et U48

« Progression / arrivées » compte les trajectoires ayant finalement terminé
le niveau parmi celles l’ayant commencé, retries autorisés. « Victoires / départs »
est un taux par tentative. Les cohortes d’arrivée et les expositions antérieures
diffèrent : ces taux ne classent pas les talents sur un même sous-échantillon.

| Condition | U43 : progression / arrivées | U43 : victoires / départs | U48 : progression / arrivées | U48 : victoires / départs |
| --- | ---: | ---: | ---: | ---: |
| Témoin | 1 069 / 1 088 · 98,25 % | 1 069 / 1 125 · 95,02 % | 863 / 877 · 98,40 % | 863 / 887 · 97,29 % |
| Cause masquée | 1 056 / 1 085 · 97,33 % | 1 056 / 1 123 · 94,03 % | 844 / 859 · 98,25 % | 844 / 874 · 96,57 % |
| Trio prêté | 1 003 / 1 024 · 97,95 % | 1 003 / 1 045 · 95,98 % | 789 / 806 · 97,89 % | 789 / 811 · 97,29 % |
| Énergie illimitée | 1 162 / 1 199 · 96,91 % | 1 162 / 1 237 · 93,94 % | 1 011 / 1 026 · 98,54 % | 1 011 / 1 036 · 97,59 % |

Pour le témoin, la première tentative seule gagne U43 dans 1 038 / 1 088 cas,
avec 37 premiers épuisements et 13 horizons. À U48 : 854 / 877 premières
victoires, 11 épuisements et 12 horizons. Les tableaux exhaustifs gardent
séparément tous les retries et les censures.

## Énergie et Coins : deux constats bornés

Dans le témoin, **3 604 / 16 000 journées configurées** s’arrêtent sur énergie
vide, soit 22,53 %. Elles ont toutes comporté des actions. Au moins un tel arrêt
survient dans 1 378 / 2 000 trajectoires. Le cap administratif quotidien ne
termine que 19 / 16 000 journées.

Contournant uniquement la réservation d’énergie, le contrôle termine
**6,873 niveaux de plus** par trajectoire appariée, intervalle de sensibilité
[5,5235 ; 8,264]. Le gain moyen est positif pour 92 / 100 profils.
Il déplace les caps administratifs à **1 831 / 16 000 journées**.
Cela montre une contrainte de progression dans ce modèle ; cela ne choisit
ni un prix ni une recharge pour le jeu natif.

Les Undo payants touchent 603 / 2 000 trajectoires témoins. Ils dépensent
19 466 Coins face à 5 456 743 Coins gagnés, soit 0,357 %. Le solde final médian
est 2 793 Coins, minimum 400. Ces soldes finaux ne prouvent pas l’absence de
toute contrainte intermédiaire. Surtout, aucun achat de déco, tenue, équipement
ou rencontre n’est simulé : on ne peut pas extrapoler ce surplus à l’économie
complète ou à LumaGold.

## Aides et sondes d’apprentissage

Le témoin suit directement un indice sur **4 079 / 572 420 sorties manuelles** :
3 544 coach U1 et 535 Focus. Une cause est visible sur 39 245 sorties ;
les catégories se chevauchent sur huit gestes et ne s’additionnent pas.
Dans le trio, 41 178 / 454 674 sorties manuelles suivent directement un indice,
dont 37 508 GUIDE. « Manuelle » signifie exécutée par tap, y compris assistée.

Les sondes U401/U573/U721/U1000 utilisent le même environnement sans coques,
180 Coins et le même budget. Coach, Focus, causes visibles, avertissement d’impasse
et talents sont désactivés ; Undo natif reste disponible. Fatigue, frustration
et mémoire sont réinitialisées, les états appris transplantés. **Le feedback et
l’adaptation continuent durant chaque sonde** : les politiques ne sont pas figées.

| Entraînement | Sondes terminées / exécutées | Entraîné seul réussit | Froid seul réussit |
| --- | ---: | ---: | ---: |
| Référence froide, une fois | 4 602 / 8 000 | — | — |
| Témoin | 6 182 / 8 000 | 2 319 | 739 |
| Cause masquée | 6 182 / 8 000 | 2 316 | 736 |
| Trio prêté | 6 041 / 8 000 | 2 226 | 787 |
| Énergie illimitée | 6 191 / 8 000 | 2 336 | 747 |

La différence entraîné/froid porte sur des politiques adaptatives initialisées
différemment et des expositions de campagne inégales. Ce n’est pas une mesure
de compréhension du texte, de la DA ou de transfert cognitif humain.
Masquer la cause change la progression appariée de −0,1735 niveau,
intervalle [−0,3675 ; +0,0165] : ce run n’établit pas un gain important de cette
seule exposition. L’inspection et le nouvel enseignement visuel ne sont pas simulés.

## Limites et reproduction

Les agents reçoivent des pièces, directions et relations de clés déjà structurées.
Leur recherche est bornée et faillible, sans accès au solveur ni à une solution
cachée. Les certifications restent dans les aides explicitement demandées
et les contrôles oracle séparés. La reconnaissance temporaire parfaite d’une
cause affichée est une hypothèse, pas une observation de lecture.

Décors, tenues et équipement, acquisition/propriété d’équipe, LumaGold,
pubs récompensées, achats, consentement, fournisseur de paiement, persistance
Android et navigation sont **non modélisés**, pas testés avec un résultat zéro.
Disponibilité, utilité, fatigue, frustration, durées et apprentissage sont des
formules non calibrées. Aucun participant humain, QI mesuré, plaisir, émotion
ou rétention réelle n’est établi.
La formule historique de durée utilise encore cinq règles, dont celle de coque
devenue inutilisée ; les moyennes d’apprentissage affichées concernent les
quatre règles communes. Ces durées ne mesurent pas une vitesse humaine.

Depuis la racine du dépôt :

    bash scripts/run-cognitive-lab-v123.sh --profiles 100 --repetitions 20 --seed 2026103101 --output /tmp/luma-new-2026103101
    python3 scripts/verify-cognitive-lab-v123.py --data /tmp/luma-new-2026103101 --require-current-source
    python3 scripts/analyze-cognitive-lab-v123.py --data /tmp/luma-new-2026103101 --output /tmp/luma-new-analysis.json
    python3 docs/validation/playtest-2026-10-31/simulation/analyze-playtest.py

Le dernier script relit les CSV conservés ici. Le runner refuse de remplacer
un dossier de sortie existant. Les sources d’explication principales sont
[la politique](../../../../scripts/cognitive_lab/Agent122.java),
[son feedback](../../../../scripts/cognitive_lab/CognitiveAgent.java),
[le driver](../../../../scripts/cognitive_lab/CognitiveLab123.java)
et [les coûts natifs](../../../../app/src/main/java/com/androidhit/lumadrift/data/CompanionBattle.java).
