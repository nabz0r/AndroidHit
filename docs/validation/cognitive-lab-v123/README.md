# Cognitive Lab 1.23 — run courant et rapport isolé

Le run exécute **8 000 trajectoires**, **380 137 tentatives** et **40 000 sondes**
sur le moteur courant du Voyage sans préparations élémentaires. Il ne comporte
aucun participant humain. Le rapport FR/EN est préparé séparément dans
[`public/lab/clarity-123`](../../../public/lab/clarity-123/index.html) ; les rapports
et données publics 1.21/1.22 restent préservés. La préparation locale du rapport
et du ZIP ne constitue pas une publication.

## Périmètre réellement exécuté

- Graine **2026103002**, 100 profils générés ×20 répétitions ×4 conditions,
  huit jours modélisés : **8 000 trajectoires et 64 000 journées configurées**.
- **380 137** lignes de tentative, **170 431** événements de blocage exhaustifs,
  **8 000** états finaux de règles. Les traces détaillées ne sont qu'un échantillon
  des quatre premiers profils, répétition zéro, plafonné à80 par condition.
- **8 000 sondes froides réellement exécutées une fois**, référence commune à
  **32 000 sondes entraînées** : 40 000 lignes, et non64 000 observations.
- **104 contrôles oracle séparés** : U1–U100 et U401/U573/U721/U1000.
  Ils certifient le moteur ; ils ne sont ni des trajectoires ni des entraînements.
- **58 sources moteur/runner** approuvées avant le run et identiques après.
  `LumaView.java` ne fait pas partie de ces sources compilées : le ZIP lie
  séparément son contexte UI final E, sans le faire passer pour un rendu testé.

Le run original a duré21,370389625 secondes sur cet hôte avec Java17.0.20.1+0.
Il s'agit d'une durée d'exécution du laboratoire, pas d'une durée de jeu humaine.
Le [protocole](PROTOCOL.md), l'[autorisation de gel](run-authorization.json),
le [binding des sources](source-binding.json), la [provenance](latest/provenance.json)
et le [journal original](run-original.log) permettent de vérifier ce périmètre.

## Résultats par condition

Chaque colonne observe **2 000 trajectoires** et **16 000 journées configurées**.
« Sans talents » conserve coach U1, Focus gagné, cause immédiate et avertissement
d'impasse. Les trois autres conditions sont des contrôles expérimentaux, pas des
variantes annoncées comme livrées.

| Mesure | Mécanique native, sans talents | Cause masquée | Trio prêté | Énergie illimitée |
| --- | ---: | ---: | ---: | ---: |
| Niveaux terminés / trajectoire | 45,337 | 45,3365 | 44,354 | 52,7285 |
| Tentatives commencées | 91 713 | 91 701 | 89 676 | 107 047 |
| Tentatives terminées | 90 674 | 90 673 | 88 708 | 105 457 |
| Budgets épuisés / départs | 274 /91 713 | 255 /91 701 | 165 /89 676 | 342 /107 047 |
| Tentatives censurées à l'horizon | 765 | 773 | 803 | 1 248 |
| Refus de tap | 43 527 | 44 631 | 31 496 | 50 777 |
| Sorties manuelles | 573 217 | 573 012 | 456 968 | 671 010 |
| Sorties automatisées | 0 | 0 | 115 256 | 0 |
| Sorties manuelles suivant directement un indice | 3 986 | 4 072 | 41 711 | 4 061 |
| Sorties manuelles avec cause visible | 38 458 | 0 | 4 094 | 44 553 |
| Coins payés pour Undo | 18 891 | 18 146 | 12 545 | 23 045 |
| Énergie dépensée | 71 764 | 71 750 | 69 707 | 0 — réservation contournée |
| Arrêts énergie vide / jours configurés | 3 671 /16 000 | 3 677 /16 000 | 3 381 /16 000 | 0 /16 000 |
| Caps administratifs quotidiens / jours | 30 /16 000 | 31 /16 000 | 99 /16 000 | 2 001 /16 000 |
| Erreurs répétées au même état | 1 768 | 1 827 | 453 | 2 151 |

Une sortie manuelle désigne un tap, y compris lorsqu'il suit GUIDE, le coach ou
Focus. Les sous-ensembles « indice direct » et « cause visible » peuvent se
chevaucher : ils ne s'ajoutent pas aux sorties manuelles. Les contrôles d'énergie
ne suppriment ni le budget de coups ni les autres arrêts d'utilité/disponibilité.
**Zéro tap de coque** a été exécuté dans ces quatre conditions.

Les différences appariées de niveaux terminés, par rapport au témoin natif,
sont −0,0005 pour cause masquée, −0,983 pour trio prêté et +7,3915 pour énergie
illimitée. Les intervalles de sensibilité à95 % sont respectivement
[−0,1675 ; +0,1655], [−1,3405 ; −0,6385] et [+6,099 ; +8,835]. Ils rééchantillonnent
1 000 fois les différences moyennes des **100 profils entiers**, sur2 000 paires,
et ne représentent aucune population humaine. Le trio réduit les refus et
automatise des sorties dans ce modèle ; cela ne suffit pas à augmenter la
progression. L'énergie illimitée déplace une partie de l'arrêt vers les caps et
autres contraintes du modèle ; ce résultat ne prescrit aucun prix natif.

## U27, U43 et U48 : dénominateurs d'arrivée

Pour le témoin natif sans talents :

| Niveau | Départs, retries compris | Terminés | Budgets épuisés | Horizon | Undo |
| --- | ---: | ---: | ---: | ---: | ---: |
| U27 | 1 657 | 1 642 | 2 | 13 | 399 |
| U43 | 1 115 | 1 060 | 39 | 16 | 1 030 |
| U48 | 893 | 879 | 3 | 11 | 358 |

U43 concentre39/274 épuisements de budget de ce témoin, sur1 115 départs
effectivement arrivés. Ce tableau ne compare pas des cohortes d'arrivée égales ;
les trajectoires arrêtées plus tôt n'y apparaissent pas. Les autres niveaux,
conditions et causes sont dans l'[analyse exhaustive](analysis.json) et les CSV.

## Sondes sans aucune aide

Les quatre géométries U401/U573/U721/U1000 emploient **le même** moteur sans
coques, budget et180 Coins. Coach, Focus, cause visible, avertissement d'impasse
et talents sont tous masqués/désactivés. Fatigue, frustration et mémoire sont
réinitialisées ; seuls les états finaux de règles appris sont transplantés.
Le feedback et l'adaptation continuent pendant chaque sonde : la comparaison
observe des politiques adaptatives initialisées différemment, pas des états
de connaissance maintenus figés pendant toute la sonde.

| Entraînement | Sondes terminées / exécutées | Entraîné seul réussit | Froid seul réussit |
| --- | ---: | ---: | ---: |
| Référence froide, exécutée une fois | 4 587 /8 000 | — | — |
| Mécanique native sans talents | 6 167 /8 000 | 2 330 | 750 |
| Cause masquée | 6 168 /8 000 | 2 332 | 751 |
| Trio prêté | 6 062 /8 000 | 2 250 | 775 |
| Énergie illimitée | 6 164 /8 000 | 2 342 | 765 |

Ces résultats décrivent l'effet fonctionnel d'états appris dans le modèle sur
un environnement commun. Les quantités et types d'exposition d'entraînement
diffèrent encore. Ils ne prouvent ni compréhension du texte ni apprentissage
ou transfert cognitif humain. La règle de coque devenue inutilisée est exportée
mais exclue de la moyenne des quatre règles communes. La formule historique de
durée à cinq règles reste une hypothèse, pas une mesure de vitesse humaine.

## Correction et compatibilité historique

Le protocole1.23 classe victoire, épuisement, abandon et cap de tentative **après
chaque action et avant les arrêts de journée**. La frustration d'échec est
appliquée immédiatement. Le validateur exhaustif trouve **zéro censure à budget
zéro** et attribue tous les épuisements au vrai jour de fin. Une victoire au
dernier coup reste terminée. Les tests nouveaux couvrent ces transitions réelles,
le cap simultané et la factory native : **3 039 assertions**.

La refactorisation des constructeurs `Environment`/`Environment122` permet une
injection de la factory1.23 sans changer la factory historique. Une réexécution
complète1.22 a réellement exécuté21 000 trajectoires, graine2026103001 : les
**six CSV gzip et `profiles.json` sont identiques en octets** au run d'audit
gelé. Seuls le [reçu de comparaison](preflight/legacy-replay-comparison.json),
la [provenance du replay](preflight/legacy-replay-provenance.json) et le
[journal](preflight/legacy-replay-original.log) sont conservés ici ; les copies
identiques des CSV ne sont pas dupliquées dans Git. Deux smokes1.23 de48
trajectoires ont également reproduit13 payloads à l'identique.

Les anciens chiffres ne sont pas relabellisés. Une comparaison avec eux mêlerait
nouvelle graine, mécanique sans coques et nouvelle classification ; elle ne
mesurerait pas un effet causal global de la release.

## Limites et reproduction

L'agent perçoit déjà pièces, directions et liens de clés sous forme structurée.
Il ne voit ni feu/glace, ni petite icône, ni animation, ni écran d'inspection,
ni HUD. La reconnaissance parfaite pendant le cue est une hypothèse explicite.
Le solveur est confiné aux certifications et aux aides explicitement demandées ;
il n'alimente pas la recherche bornée. Préférences, durée, fatigue, frustration,
retour et apprentissage sont des formules non calibrées. **Aucune preuve de
plaisir, simplicité ressentie, émotion, rétention, QI ou validité clinique**
n'est produite.

```sh
bash scripts/test-cognitive-lab-v123.sh
python3 scripts/verify-cognitive-lab-v123.py --require-current-source
node --test scripts/test-cognitive-report-v123.mjs
bash scripts/run-cognitive-lab-v123.sh --profiles 100 --repetitions 20 \
  --seed 2026103002 --output /tmp/luma-new-run
```

Le runner refuse d'écraser une sortie existante. Les tests rapides et la CI ne
relancent pas la grande campagne. Le ZIP déterministe contient une seule copie
des données brutes, les58 sources gelées et le contexte UI final distinct.
[Validation des lignes](validation.json), [manifest du kit](source-kit-manifest.json)
et [manifest du rapport](site-manifest.json) lient les données et fichiers publics.

SHA256 provenance : `b4324d42b1d7c919c6f12a95bb47dcfdef6832a113053d3e008ae6ffc34f8c57`.
SHA256 analyse : `fe4b29490b86f11b97f2f490ca779723e46a3beed43854d40ae96741b9760ada`.
