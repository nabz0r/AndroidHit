# Contrat retenu — progression utile dans Journey

**Statut : arbitrage de root du 31 octobre, implémentation répartie entre modèle, récompenses et UI.** Journey, les flèches et la progression existante sont conservés. Aucune promotion d’un satellite ni création d’un nouveau mode n’est retenue. Ce document remplace la proposition exploratoire de sortie courte ; il décrit le contrat à qualifier, pas une livraison native déjà prouvée.

## Une chaîne utile, sans nouveau compteur

Première victoire sur un plateau → XP joueur existant → chaque nouveau rang donne 1 copie au capitaine du run → niveau de ce compagnon → capacité réelle supplémentaire de son talent sur les puzzles suivants.

| Étape | Contrat |
| --- | --- |
| Départ d’un puzzle | Équipe de 1 à 3 compagnons possédés et niveaux réels capturés ; le premier membre est le capitaine de ce run. |
| Talent | Effet natif existant ; chaque activation consomme 1 usage et son coût de Charge. Niveau 1 : 1 usage, niveau 2 : 2, niveaux 3–5 : 3, niveaux 6–10 : 4. |
| Charge | 3 au départ, plafond 6, +1 par pièce unique sortie ; coûts et Résonance existants conservés. Aucun remboursement par simple Undo/re-sortie de la même pièce. |
| Première victoire | XP de 20 + 5 × étoiles, donc 25/30/35, selon les règles d’unicité historiques. Réussir sans talent donne la même progression. |
| Rang | `1 + xp / 120`. Chaque rang nouvellement franchi et non déjà traité donne une copie au capitaine capturé, jamais au personnage d’accueil choisi après coup. |
| Prochain puzzle | Lire les copies réellement persistantes, en déduire le niveau et capturer la nouvelle capacité. Aucun retrofit d’un plateau déjà engagé. |

Le premier passage utile est concret : un compagnon possédé à 1 copie passe à 2 copies, niveau 2 ; au prochain départ, il dispose de 2 usages, sous réserve de Charge. Il ne devient pas automatiquement plus puissant à chaque victoire : les seuils de copies et les paliers de capacité sont distincts.

## API pure du talent, implémentée

`CompanionBattle.State.start(List<String> ids, List<Integer> verifiedLevels)` crée un état `v124_1`. Le caller doit avoir vérifié la possession et fournir les niveaux réels dans l’ordre de l’équipe. Le modèle appelle `CompanionProgression.skillCharges`, puis conserve une copie immuable des niveaux et capacités.

- `capacity(slot)` : capacité figée au départ.
- `usedCount(slot)` : nombre d’activations effectuées.
- `remaining(slot)` : usages encore disponibles.
- `used(slot)` : vrai lorsque tous les usages sont consommés.
- `canUse(slot)` : il reste un usage ET la Charge suffit.
- `levels()`, `policyVersion()`, `isLevelled()` : contexte exact du run.

Un manque de Charge peut refuser un talent alors qu’il reste des usages. Un effet inapplicable refuse le draft sans consommer l’état original. Les demandes d’UI portent toujours l’identité encodée exacte pour refuser une requête périmée.

`State.start(ids)` reste l’initialisation historique à 1 usage par membre, pour les anciens callers et contrôles. `decode(v119_1)` conserve exactement ce codec et sa capacité historique. `decode(v124_1)` rejoue les événements et valide les niveaux, capacités, compteurs, Charge, crédits de sorties et combo. Les sauvegardes anciennes ne sont pas enrichies avec le niveau actuel.

Le nouveau codec a 11 champs : version, équipe, niveaux, capacités, Charge, usages, masque de sorties créditées, dernière famille, combo prêt, combo consommé, événements. Au maximum : 64 crédits de pièces + 12 activations + 1 combo = 77 événements. Le codec legacy à 9 champs reste inchangé.

## XP et récompense de rang

Le helper pur prévu par l’agent récompenses est `RankCopyRules124` : `xpForStars`, `rankForXp`, `ranksCrossed` et `migrationFloor`. Le runner 1.24 doit importer ce helper pour éviter une seconde formule qui dériverait du jeu.

Le coordinator doit enregistrer l’XP et son reçu de rang de manière durable, puis attribuer la copie avec une identité stable de rang, run et capitaine. Une réouverture doit finir une attribution interrompue sans donner de seconde copie. Le capitaine vient du snapshot `battleState.team().get(0)` du run terminé, pas de l’équipe modifiable à l’accueil.

Un profil neuf part à XP 0, rang/plancher 1 et 1 copie de Nova. La migration d’une sauvegarde existante pose le plancher à son rang courant, sans créditer rétroactivement les anciens rangs. Les copies déjà possédées, les reçus historiques et la progression U sont conservés. Un replay connu ne doit créer ni nouvelle XP de première victoire ni nouvelle récompense de rang.

Le plafond de copies doit rester défini par GameStore : ne pas inventer une compensation en pièces ou en Gold dans le modèle du talent. Si un gain atteint un palier en dehors d’un puzzle, seule la partie démarrée ensuite reçoit la capacité nouvelle.

## Énergie, maison et redémarrage

L’énergie de session n’est pas la Charge du compagnon. Son débit, les retries gratuits éventuels et l’entrée unique d’expédition sont des décisions de politique séparées ; le modèle `CompanionBattle` n’effectue aucune réservation d’énergie, aucun débit monétaire et aucune attribution de copie. La simulation 1.24 devra décrire la politique réellement gelée, sans supposer qu’un retry ou un départ a changé parce qu’un libellé UI a changé.

Des objets de maison débloqués au rang peuvent donner un second résultat visible à l’XP, sans nouvelle monnaie. Appliquer les nouvelles conditions uniquement aux nouveaux objets ; conserver les anciens droits de possession et leurs prix. Une récompense annoncée comme plaçable doit réellement modifier la scène de maison. La table des objets et la DA appartiennent à root ; aucun identifiant fictif n’est déclaré acquis ici.

La popup de restart doit annoncer les conséquences réelles du reset. Elle ne doit pas réutiliser une demande de talent périmée, ni changer silencieusement le capitaine de la victoire déjà engagée. Le clic sur le compagnon doit conduire à son effet, ses usages et sa Charge actuels. La fermeture doit retrouver le puzzle, sans remplacer son état par un nouvel état calculé à partir des copies courantes.

## Qualification minimale

1. Contrôle niveau 1 contre niveau 2 sur le même plateau : le premier produit un GUIDE, le second peut produire un deuxième GUIDE actuel après avoir gagné assez de Charge ; le troisième est refusé au niveau 2.
2. Capacités 1/2/3/4, tous les niveaux et identités ; Charge et refus sans consommation ; compteurs conservés à froid et demandes périmées rejetées.
3. Ancien codec restauré octet pour octet, capacité 1 malgré un compagnon aujourd’hui plus avancé. Ancien runner conservé comme contrôle à un usage.
4. Première victoire franchissant un rang → copie au capitaine de départ → copies persistantes → capacité nouvelle seulement au prochain puzzle. Même récompense sans activation de talent.
5. Migration de sauvegarde existante sans rétro-récompense ; replay, double appui et faute d’écriture sans double XP/copie ; réconciliation après crash.
6. QA native : clic compagnon, action et effet réels, victoire, gain compréhensible et puzzle suivant, FR/EN et petit écran. Les tests du modèle ne démontrent ni compréhension, ni intérêt, ni rétention humains.

Preuves initiales du modèle : le nouveau test `scripts/test-companion-battle-v124.sh` passe 94 458 assertions ; le test historique du modèle passe 88 946 assertions, et celui des actions historiques couvre U1–U1000 × 37 identités avec 2 313 118 assertions. Ces résultats ne qualifient pas encore le coordinator Android ni la nouvelle UI.
