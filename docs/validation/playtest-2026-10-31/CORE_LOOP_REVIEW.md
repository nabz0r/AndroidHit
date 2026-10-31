# Revue du cœur de jeu — 31 octobre 2026

**Diagnostic : la progression du joueur, la progression des compagnons et les talents du parcours ne ferment pas la même boucle.** Le parcours récompense surtout en pièces et en XP joueur. Le compagnon utilisé n'y gagne pas de copie ; son niveau ne modifie pas son talent de puzzle. Une meilleure explication ne suffirait donc pas à donner une utilité à toutes les jauges.

Ce diagnostic porte sur la production 1.23 et les sources lues avant les changements du cœur 1.24. La revue elle-même ne modifie aucun modèle, écran, prix ou sauvegarde. Le contrat retenu ci-dessous est en cours d’implémentation séparée ; il ne constitue pas une qualification native ou une validation par des joueurs.

## Ce qui fonctionne réellement aujourd'hui

| Système | Ce qui le fait évoluer | Conséquence vérifiable | Rupture dans la boucle principale |
| --- | --- | --- | --- |
| Carte du parcours | Réussir un nouveau plateau | Accès au plateau suivant, pièces, conditions de collection et d'équipement | C'est actuellement la progression la plus directement utile. |
| XP et niveau joueur | Première victoire distincte : 25, 30 ou 35 XP selon les étoiles ; 120 XP par rang | Le rang et sa barre s'affichent dans le profil et l'accueil | Les usages de `meta.rank()` trouvés dans le code applicatif sont des affichages. Ni les talents, ni la capacité d'énergie, ni les niveaux de compagnons n'en dépendent. Les missions et le badge des 7 plateaux ont leurs propres conditions. |
| Énergie de session | Réserve initiale de 10 ; un débit à un nouveau départ payant ; une unité récupérée en 10 minutes ; plafond progressif de 10 à 16 | Autorise ou refuse une nouvelle tentative | C'est une limite d'accès, pas une décision de résolution ni une récompense qui rend le compagnon utile. |
| Charge des talents | 3 au départ d'un plateau, plafond 6, +1 par pièce effectivement sortie ; talents coûtant 2 ou 3 | GUIDE, VISION, TRAIL, PULSE, FLOW ou REWIND ; chaque membre une fois ; Résonance après deux familles différentes | C'est une ressource tactique réelle, mais indépendante de l'énergie de session, du rang joueur et du niveau du compagnon. |
| Copies et niveaux de compagnon | Arrivées payées en pièces, découverte d'expédition, copie ciblée d'expédition | Niveaux 1 à 10, apparence évolutive ; capacité de talent de route accrue pour Nova, Miko et Orbit | Le talent du parcours reste une définition fixe par identité. Faire gagner le compagnon choisi dans Journey ne le fait pas grandir. |
| Équipement | Déblocages par victoires, événements ou achat en pièces | Selon l'objet : petit bonus de pièces, remise sur un pack ou grâce sur un blocage ; certains objets sont purement visuels | Ce n'est pas une progression de talent du compagnon. Un objet acheté n'est pas nécessairement plus puissant. |
| Décoration | Achat et placement de mobilier | La pièce change et conserve le choix | C'est un but d'expression personnelle légitime ; lui ajouter artificiellement de la puissance ne réparerait pas la boucle puzzle–compagnon. |

Sources : [MetaProgress.java](../../../app/src/main/java/com/androidhit/lumadrift/data/MetaProgress.java), [SessionEnergyStore.java](../../../app/src/main/java/com/androidhit/lumadrift/data/SessionEnergyStore.java), [CompanionBattle.java](../../../app/src/main/java/com/androidhit/lumadrift/data/CompanionBattle.java), [CompanionProgression.java](../../../app/src/main/java/com/androidhit/lumadrift/data/CompanionProgression.java), [AvatarGearCatalog.java](../../../app/src/main/java/com/androidhit/lumadrift/data/AvatarGearCatalog.java), [AvatarLoadout.java](../../../app/src/main/java/com/androidhit/lumadrift/data/AvatarLoadout.java).

### Pourquoi l'énergie peut sembler immobile

Les achats, l'équipement et la décoration observés ne dépensent pas d'énergie. U1 à U10 sont gratuits, le défi entre amis est gratuit, et reprendre la même tentative n'est pas une nouvelle réservation. En revanche, une nouvelle tentative après U10 coûte une unité, y compris un redémarrage explicite. La récupération pendant la navigation peut masquer un débit ancien. Les captures de la maison à 11/11 ne démontrent donc aucun défaut de débit.

Le plafond dépend de la progression des plateaux via `EconomyCurve121.energyCapacity`, pas de l'XP joueur. Un plafond accru augmente le stockage, sans remplir immédiatement la jauge. Ce sont des règles cohérentes de comptabilité ; elles ne constituent pas, à elles seules, une raison intéressante de jouer.

Points de code : `SessionEnergyStore` lignes 12–13, 28–33, 44 et 53 ; `LumaView.reserveSessionEnergy` ligne 416 et `restartRun` ligne 3358 ; `EconomyCurve121.energyCapacity` ligne 45. Leurs tests existants vérifient notamment la réservation idempotente, les reprises, les plafonds et la récupération. Ils ont été lus pour cette revue, sans nouvelle exécution.

### Le compagnon affiché, l'équipe et la cible de croissance sont encore trois choix

`GameStore.companionSquad()` conserve une liste de 1 à 3 identités possédées. Elle part du compagnon équipé si aucune équipe valide n'est enregistrée, mais devient ensuite une sélection indépendante. Une expédition choisit encore un acteur et une cible de copie distincts. Le test `ExpeditionV118StoreTest` contient explicitement Nova comme acteur et un invité comme cible.

Cela permet plusieurs usages avancés, mais affaiblit la promesse simple « je joue avec lui, il progresse, il m'aide ensuite ». Le système ne suit pas automatiquement cette promesse.

Sources : [GameStore.java](../../../app/src/main/java/com/androidhit/lumadrift/data/GameStore.java), lignes 1368–1445 ; [ExpeditionV118StoreTest.java](../../../scripts/ExpeditionV118StoreTest.java), cas `guest:vi` ; [CompanionBattleAction.java](../../../app/src/main/java/com/androidhit/lumadrift/data/CompanionBattleAction.java). Le talent du parcours reçoit l'identité et l'état de Charge, sans niveau ni nombre de copies.

## Une boucle retenue dans le parcours existant

**Première victoire → XP joueur → nouveau rang → copie du capitaine réellement joué → niveau du compagnon → usages supplémentaires de son talent dans les puzzles suivants.**

Le choix retenu conserve Journey, ses flèches et sa progression. Il ferme directement la boucle principale ; il ne demande pas au joueur de changer de mode pour donner un sens à ses gains. L’expédition constitue un précédent de capacité utile par niveau, mais elle ne devient pas le nouveau cœur.

1. **Choisir une équipe possédée et son capitaine.** Le puzzle capture cette équipe et ses niveaux au départ. Le bénéficiaire de sa récompense est ce capitaine du run, même si le joueur change ensuite son compagnon d’accueil ou son équipe pour une prochaine partie.
2. **Résoudre le plateau et choisir quand employer un talent.** Les effets, la Charge commune de 3 à 6 et les coûts de 2 ou 3 restent réels. Les niveaux donnent des usages supplémentaires par membre : 1 au niveau 1, 2 au niveau 2, 3 aux niveaux 3–5 et 4 aux niveaux 6–10. Une capacité disponible ne remplace pas le besoin de Charge. Réussir sans aide doit donner la même progression.
3. **Gagner l’XP de première victoire.** Les montants existants restent 25, 30 ou 35 selon les étoiles, sans farming de première victoire par replay. Chaque nouveau rang franchi donne une copie au capitaine capturé, une fois. Le rang n’est pas une seconde monnaie à dépenser.
4. **Voir l’effet à la prochaine partie.** Une copie fait passer un compagnon possédé de 1 à 2 copies, donc au niveau 2 et à deux usages possibles. La capacité du puzzle déjà engagé reste figée : une récompense, un changement d’équipe ou une relance d’écran ne lui ajoute pas un usage en cours de route.

Aucun compteur de lien, XP compagnon, fragment ou score de puissance n’est nécessaire. Les seuils de copies restent 1, 2, 8, 14, 22, 32, 44, 58, 74, 92. Les niveaux 4–5 et 7–10 n’ajoutent pas d’usage ; il ne faut pas promettre un nouveau pouvoir à chaque niveau. Les seuils hauts restent un équilibrage provisoire.

### Conséquences pour les autres systèmes

- L’XP a maintenant un débouché de jeu identifiable : la copie ciblée et la capacité de puzzle. Des objets de maison précis peuvent également devenir accessibles à des rangs, en conservant les possessions et prix historiques ; ils ne doivent pas créer une seconde boucle de puissance.
- La Charge du talent reste distincte de l’énergie de session. Une modification du coût des expéditions ou des retries doit être une politique explicite, sans réinterpréter une sauvegarde en cours. Elle n’est pas implémentée par le nouveau `CompanionBattle.State`.
- Les nouveaux niveaux sont capturés au départ. Les anciennes sessions `v119_1` conservent une capacité de 1 usage par membre ; elles ne sont pas rétrofités en fonction du niveau courant.
- Le décor et les tenues donnent un but d’expression personnelle visible. Leur ajouter automatiquement de la puissance ne réparerait pas la boucle du compagnon ; distinguer l’apparence des équipements qui ont réellement un effet.
- Les pubs et LumaGold restent facultatifs et hors des conditions de la copie de rang. Leur correctif local est un autre périmètre, non qualifié par cette revue.

Le [contrat MVP implémentable](CORE_LOOP_MVP_CONTRACT.md) précise le calcul de rang, les migrations, l’état versionné et les vérifications. Le modèle pur du nouveau talent est testé séparément ; son intégration native et sa nouvelle simulation demandent leurs propres preuves.

## Ce que les 8 000 trajectoires peuvent et ne peuvent pas trancher

La [simulation du 31 octobre](simulation/README.md) a exécuté 8 000 trajectoires, 377 413 tentatives et 372 673 victoires. Elle ne simule pas le choix du décor, les achats d'équipement, la compréhension d'un écran, les recettes publicitaires ou l'attachement à un compagnon. Elle ne valide donc pas ce pilote.

Elle donne deux avertissements utiles pour le système existant :

- Dans la condition native, 3 604 journées configurées sur 16 000 finissent sur une énergie vide, toutes après des décisions. La condition sans limite d'énergie atteint en moyenne 52,064 plateaux contre 45,191, soit +6,873 dans les 2 000 paires. C'est un effet mécanique du droit d'entrée dans un modèle borné à 8 jours, pas une estimation de rétention humaine.
- Le trio prêté atteint 44,064 plateaux contre 45,191. Il demande un talent lors de 152 281 décisions sur 643 304 et subit 11 706 refus sur ces demandes. **Cette politique synthétique sollicite trop souvent les talents et n'optimise pas les refus ; aucune baisse réelle chez des joueurs n'est établie.** Ne pas transformer ce résultat en nerf des compagnons. Il justifie de ne pas récompenser le simple nombre d'activations.

L'arrivée à un niveau tardif n'est pas un échec de compréhension. À U43, la condition native compte 1 088 arrivées, 1 125 départs de tentative, 1 069 victoires et 39 épuisements de budget ; à U48, 877 arrivées, 887 départs, 863 victoires et 11 épuisements. Les répétitions, censures positives de budget et limites de journées sont séparées dans le rapport de simulation. L'énergie n'est pas la seule difficulté.

## Contrat de validation avant toute généralisation

Le pilote doit prouver une seule chaîne complète dans Journey : partir avec Nova niveau 1 → terminer des plateaux pour franchir le rang suivant → créditer exactement une copie à Nova → reprendre à froid avec Nova niveau 2 → pouvoir produire deux effets GUIDE réels au prochain puzzle si la Charge suffit. Le même gain doit être obtenu sans utiliser le talent. Un replay connu ne doit pas redonner l’XP ni la copie ; un double appui ou une relance ne doit pas doubler la récompense. Une session ancienne reste à un usage malgré le niveau courant.

Observer ensuite de vraies premières sessions : le joueur peut-il expliquer ce que son choix a changé, ce qu'il vient de gagner et ce qu'il peut maintenant faire au prochain puzzle ? A-t-il un motif de choisir une autre route ou un autre compagnon ? Ces réponses restent inconnues. Elles décideront de l'intérêt du contrat ; une jauge qui monte et un test synthétique ne les remplacent pas.

## Provenance de la revue

Captures et gestes : APK production 1.23.0-rc2/code 31, SHA-256 `429a443c9228787c7b854d36a40f0bcf7d52fdc8a3601214cd27b5cfae6945fb`, rapports liés dans [UX_REVIEW.md](UX_REVIEW.md). Source lue : arbre de travail du 31 octobre ; les règles citées ne constituent pas une qualification des correctifs locaux en cours. Aucun nouvel input natif, aucune simulation supplémentaire et aucune modification app/public n'ont été réalisés pour cette revue.
