# Luma Drift 1.16 — scène, identité et collection

Recherche du 29 octobre 2026 (Europe/Luxembourg), effectuée sur des sources officielles. Diagnostic du code 1.15 avant les changements 1.16. Les mesures proposées ci-dessous sont des objectifs de conception pour Luma Drift, pas des résultats d'une étude utilisateur.

## Décisions à appliquer

1. **Maison : la scène devient le contenu principal.** Réserver presque toute la hauteur utile à la pièce, garder les onglets de pièces compacts, et mettre les commandes de décoration en barre flottante ou panneau inférieur. Démarrer sur une vue de détails avec déplacement ; offrir une commande « Vue entière » explicite.
2. **Collection : le personnage porte l'écran.** Un grand portrait habillé, son nom, sa rareté et une action principale. L'identité du joueur apparaît sur une carte séparée : pseudo éditable, drapeau choisi et trois badges exposés. Les catégories servent à naviguer sous le portrait ; elles ne doivent plus diviser l'écran en quatre boîtes équivalentes.
3. **Actions : remplacer l'uniformité par une hiérarchie.** Une action principale de 52–60 unités logiques, avec couleur saturée, profondeur et icône ; les actions secondaires deviennent de petites capsules ou des icônes dans des cibles de 48 unités. Les cartes illustrées sont tactiles sans bouton interne répétitif.
4. **Invocations : distinguer les compagnons des tenues.** Chaque bannière montre ce qu'on peut obtenir, avec un grand personnage réellement différent. Catalogue, chances et compensation des doublons sont accessibles avant le tirage. La révélation agrandit le résultat et propose « Équiper ». Utiliser la monnaie gagnée en jeu ; préserver les garde-fous actuels sur les achats réels.
5. **Badges : exposer un accomplissement vérifiable.** Débloquer des emblèmes sur des faits déjà persistés — première victoire, paliers de niveaux, nombre de meubles possédés/placés, collection — et montrer condition, progression et état acquis. Ne pas confondre décor acheté et réussite.

## Pourquoi la maison reste petite

`HomeScene` projette un monde de 640 × 480 dans un cadre de 390 × 440 avec un facteur interne `FIT=.62`. `LumaView.drawPremiumHome` réduit ensuite ce cadre par `homeSceneScale=min(1, homeSceneHeight/440)`. La bande de scène commence à 148 et s'arrête à `max(330, viewportH−225)`.

Sur une fenêtre logique de hauteur 550, cela donne 182 unités de scène et un facteur externe d'environ .414. Le facteur combiné vaut environ .257 : les dessins des meubles sont affichés à un quart de leur taille monde. La pièce plus grande de la 1.15 a augmenté le terrain, mais cette double réduction limite précisément les détails demandés par le joueur.

La correction doit partir du **rectangle réellement visible** et partager sa transformation avec les points de sol et les cibles tactiles. Agrandir uniquement le dessin casserait sélection et placement. Le changement de pièce appelle actuellement `resetCamera(1,0,0)`, donc remet aussi la petite vue d'ensemble.

Critères de validation proposés :

- La maison utilise au moins 80 % de la largeur disponible au premier affichage ; en vue détails, un objet de deux cases doit être clairement plus grand que sa vignette de catalogue.
- Le cadrage par défaut montre le compagnon et un groupe utile de meubles. La commande « Vue entière » permet de retrouver toutes les limites de la pièce.
- Le déplacement ne mène jamais à un écran intégralement vide. Les meubles aux bords restent atteignables.
- Toucher, rotation, placement et zone de sol suivent exactement le dessin à chaque zoom, sur écran compact et normal.
- La visite favorise la scène ; les longues commandes n'apparaissent que pendant la décoration ou la sélection d'un objet.

## Pourquoi les contrôles ressemblent encore à une grille

`PremiumUi.heroButton` produit toujours une surface indigo uniforme ; `quietButton` une surface papier. Dans `LumaView`, de nombreuses actions partagent donc les mêmes dimensions et le même rythme. `drawCollectionHub` plafonne son personnage à 205 unités, en consomme une partie pour le lien « Garde-robe », puis aligne quatre cartes et deux actions. Le lien « Profil » ouvre les réglages, sans véritable écran d'identité.

Le problème ne se résout pas par davantage de dégradés sur chaque rectangle. Il faut différencier l'objet à regarder, l'action à faire et les destinations secondaires. Le personnage ou la pièce doit dominer ; les contrôles occupent la place nécessaire à leur usage.

## Ce que montrent les références

| Référence officielle | Mécanisme observable ou documenté | Application à Luma Drift |
| --- | --- | --- |
| [Crash Fever — personnages](https://crashfever.com/character/) | Grande composition autour d'une silhouette, accessoires et environnement propres au personnage. La miniature conduit aux informations détaillées selon la [FAQ des unités](https://crashfever.com/faq/faq_cat/faq5/). | Agrandir les compagnons, donner aux familles de skins des silhouettes et accessoires lisibles ; utiliser la grille pour parcourir, le portrait pour désirer et équiper. |
| [Crash Fever — charge gacha](https://crashfever.com/campaign/2025033040730/) | Le tirage est aussi alimenté par des points obtenus en jouant ; les détails sont accessibles depuis le système de tirage. | Lier visuellement la progression aux tickets/monnaie gagnés et montrer la bannière avant l'ouverture. Aucun besoin d'ajouter un achat réel pour créer un rituel de collection. |
| [Crash Fever — lettre du producteur du 25 septembre 2026](https://crashfever.com/campaign/2026092545575/) | Le producteur explique une révision des chances et maintient un accès déterministe via pièces d'échange. Les probabilités diffèrent entre bannières. | Afficher les chances exactes du pool de Luma, ne pas imiter les taux d'un autre jeu. Préserver une voie d'acquisition compréhensible et le traitement des doublons. |
| [Brawl Stars — Battle Cards](https://support.supercell.com/brawl-stars/en/articles/battle-cards-6.html) | Le profil permet de composer une carte avec icônes, pin, titre et fond ; rang et réalisations fournissent des éléments à exposer. | Créer une vraie carte joueur, éditable depuis son portrait, avec pseudo/drapeau et badges équipés. Les acquis doivent aussi apparaître sur les écrans où le joueur se reconnaît. |
| [Brawl Stars — catalogue](https://support.supercell.com/brawl-stars/en/articles/the-catalog-2.html) | Les cosmétiques sont consultables selon type, rareté, personnage et thème, y compris non possédés. | Onglets Compagnons/Tenues, filtres simples, état possédé lisible ; proposer une grande prévisualisation habillée avant équipement ou acquisition. |
| [Pokémon TCG Pocket — collection](https://www.pokemontcgpocket.com/ja/howtoplay/collection/) | Deux échelles complémentaires : grand objet exposé sur une planche et nombreux objets rangés dans un classeur. | Séparer le portrait plein format de la grille de collection ; une maison et ses objets méritent aussi un vrai mode visite, au lieu de rester une vignette dans les commandes. |

Ces références justifient des mécanismes de présentation. Elles ne prouvent pas qu'un choix fera augmenter rétention, plaisir ou achats dans Luma Drift ; cela nécessite des essais de joueurs.

## Références visuelles inspectées

Les images officielles suivantes ont été ouvertes dans un onglet de recherche temporaire, puis l'onglet a été fermé. Elles sont des références de composition ; aucun élément graphique n'est réutilisé dans le jeu.

- [Crash Fever — composition de personnage](https://crashfever.com/wp-content/uploads/2022/12/1671779126-3ca03206f1250b45f01671c3110e56f5.png), liée par la page officielle des personnages : silhouette centrale, thème matérialisé par des objets, contour et contrastes forts. Il s'agit d'une illustration officielle, pas d'une capture de l'interface actuelle.
- [Brawl Stars — fonds de cartes de rang](https://brawlstars.inbox.supercell.com/xdjcscmv3zo3/3PuahCdhflXRwJXISuC6Ua/1af1740684219871b9ac944f2943cf83/0207_ranked_cards_1920x1080.png), publiée dans la [note officielle du 13 février 2025](https://supercell.com/en/games/brawlstars/blog/news/ranked-is-dead-and-ranked-is-back/) : rang, emblème et motif identifient chaque carte à distance.
- [Pokémon TCG Pocket — objet exposé en grand](https://www.pokemontcgpocket.com/assets/img/ja/howtoplay/img09.png), liée par la page collection : objet central grand format, environnement de présentation discret. C'est une illustration promotionnelle de la collection, pas une preuve de tous les contrôles actuels.

## Contrats existants à préserver

La 1.15 possède déjà 24 compagnons à dessins distincts et 24 vêtements, mais sa capsule de 150 LumaCoins ne tire que les vêtements. Elle utilise un reçu stable pour retrouver le même résultat après reprise. Les chances de famille actuelles sont 60 % commune, 30 % rare, 10 % épique ; les doublons rendent 25/60/100 pièces. Renommer cette capsule ne créerait donc pas un tirage de compagnons.

Pour étendre le système, préserver l'idempotence du reçu, la persistance atomique des dépenses/objets et les achats directs/déblocages déjà obtenus. Les probabilités doivent venir du même catalogue utilisé par le tirage. L'animation de révélation ne doit jamais déterminer le résultat ni débiter une seconde fois à la reprise.

Le pseudo et le drapeau sont des préférences choisies, sans inférence de nationalité ni de localisation. Changer un pseudo doit rester gratuit dans ce jeu local. Les badges peuvent être consultés verrouillés avec leur condition, mais seuls les acquis sont équipables. Une identité locale n'implique pas un profil publié en ligne.

## Vérifications utiles pour cette version

1. Ouvrir maison avec le vrai aménagement existant ; comparer les tailles à la capture 1.15, puis vérifier zoom, déplacement, changement de pièce, décoration et reprise.
2. Saisir un pseudo avec accents, changer de drapeau, équiper/déséquiper les badges acquis ; fermer puis relancer et retrouver exactement l'identité enregistrée.
3. Ouvrir le catalogue et les chances sans dépense. Annuler un tirage sans mutation. Effectuer un tirage, fermer pendant la révélation et retrouver le même reçu et le même résultat avec une seule dépense.
4. Montrer un nouveau compagnon et un vêtement sur le grand portrait, puis vérifier que l'équipement se voit aussi dans la maison.
5. Contrôler les textes FR/EN et les autres langues disponibles, les noms longs, les états possédé/verrouillé/équipé, les contrastes et les cibles sur écran compact.
