# Luma Drift 1.17 — direction artistique et navigation sans boutons visibles

Recherche réalisée le 29 octobre 2026. Sources primaires consultées en ligne ce jour. Cette note distingue les faits documentés, le diagnostic des captures Luma 1.16 et les décisions proposées. Elle ne constitue pas une preuve qu'un choix visuel produira une audience de plusieurs millions de joueurs.

## Ce que les références établissent

### Crash Fever : simplicité d'entrée et identité des collections

Le site officiel présente le jeu par une manipulation au toucher, les Crash Skills et le jeu automatique. Son catalogue sépare les personnages par identité, type et interprète. Le site est actif : une annonce datée du 28 octobre 2026 détaille l'événement Halloween ouvert le 29 octobre, avec un personnage à obtenir, des quêtes et des variantes de décor jour/nuit. Ce sont des éléments vérifiés, pas une mesure de rétention ni une preuve de supériorité de son interface. Pour Luma, la transposition utile est une action simple, un héros clairement identifiable et des variations de collection qui racontent quelque chose, plutôt qu'un habillage de cases de catalogue.

Sources : [comment jouer](https://crashfever.com/how_to_play/), [catalogue officiel](https://crashfever.com/character/), [événement actuel, 28 octobre 2026](https://crashfever.com/campaign/2026102845706/).

### Candy Crush : le personnage détermine sa forme

Dans un échange direct avec le studio, la directrice artistique de Candy Crush Friends explique que personnalité et histoire précèdent l'apparence ; formes, couleurs, vêtements et comportement en découlent. Le studio décrit aussi l'itération par croquis, puis la définition du modèle, de la couleur et des textures avant la production. L'environnement suit la même démarche de conception. L'enseignement applicable est concret : donner une fonction et un tempérament à chaque Luma, puis construire sa silhouette et sa tenue ; éviter 24 recolorations du même corps. Ce témoignage historique explique une méthode de production ; il ne décrit pas à lui seul la version 2026 de Saga.

Source : [Q&A officiel avec le studio Candy Crush Friends](https://community.king.com/en/candy-crush-friends-saga/discussion/271740/q-a-you-asked-the-ccfs-studio-they-answered).

### Candy Crush : la progression doit se lire

Le cofondateur de King décrit le premier prototype comme une boucle courte sans évolution suffisante. Dans son récit, la carte Saga et la progression partagée ont donné une suite compréhensible aux niveaux. Il associe également la longévité au sentiment de dépasser une difficulté et à l'adaptation continue. Cela soutient une carte et des récompenses compréhensibles ; cela ne démontre pas que l'absence de boutons améliorerait le jeu.

Source : [entretien Apple avec Sebastian Knutsson](https://apps.apple.com/us/iphone/story/id1462263204).

### Actualisation King 2025–2026

En mai 2025, l'équipe communautaire officielle présente de nouveaux repères de difficulté associant icônes, couleurs et décors, et demande explicitement si les catégories sont plus faciles à reconnaître. Le 27 novembre 2025, une publication officielle mise à jour le 9 janvier 2026 présente les Candy Pins avec la senior narrative designer : une collection à découvrir dans l'univers du jeu. Le 9 juin 2026, King demande des questions pour une nouvelle saison de sa série de coulisses. Ces publications confirment un travail actuel sur reconnaissance, collection et retour joueur. Les commentaires et votes d'un forum ne sont pas une étude représentative ; aucun gain de performance n'est inféré ici.

Sources : [repères de difficulté](https://community.king.com/en/candy-crush-saga/discussion/496696/new-hard-level-labels-what-do-you-think), [Candy Pins](https://community.king.com/en/candy-crush-saga/discussion/513963/crush-tell-episode-3-candy-pins-can-you-collect-them-all), [préparation de la saison 2, juin 2026](https://community.king.com/en/candy-crush-saga/discussion/comment/3546240).

### Gestes : modernité et découvrabilité doivent coexister

Apple recommande les gestes familiers, un résultat immédiat et prévisible, et plusieurs moyens d'effectuer une action importante. Les gestes propres à un produit doivent rester découvrables et simples. Android recommande des zones interactives d'au moins 48 × 48 dp. « Zéro bouton visible » peut donc être tenu sans supprimer les contrôles sémantiques : un personnage, une porte ou un objet dessiné devient une cible nommée et focusable. Un balayage facultatif peut accélérer la navigation ; il ne doit pas devenir l'unique moyen d'atteindre les quatre scènes. Supprimer les repères pour ne garder que des gestes secrets augmenterait le travail de mémorisation.

Sources : [Apple, Gestures](https://developer.apple.com/design/human-interface-guidelines/gestures), [Android, Make apps more accessible](https://developer.android.com/guide/topics/ui/accessibility/views/apps-views).

### Mouvement : continuité et contrôle

Le système de mouvement Material 3 a été renouvelé en mai 2025 autour de ressorts physiques, avec des mouvements expressifs ou plus sobres et une gestion des interruptions. Le principe utile est une même dynamique pour une famille d'objets, plutôt que des effets indépendants partout. King documente dans Friends des réglages réduisant déplacements de caméra, secousses et particules. Pour Luma, les déplacements doivent expliquer la relation entre deux scènes ; les halos et rebonds restent secondaires. Le réglage de mouvement réduit doit également s'appliquer aux nouvelles transitions.

Sources : [Material 3, système physique actuel](https://m3.material.io/styles/motion/overview/how-it-works), [King, réglages d'accessibilité de Friends](https://community.king.com/en/candy-crush-friends-saga/discussion/313075/new-check-out-the-accessibility-features).

## Diagnostic visuel de Luma 1.16

Captures réellement inspectées : `public/assets/screenshots/v1.16/collection-fr.png` et `public/assets/screenshots/v1.16/home-fr.png`.

- Le personnage possède une grande surface mais peu d'information : corps presque circulaire, face identique, vêtements plats, matières peu distinctes. Augmenter sa taille ne résout pas cette pauvreté de silhouette et de détail.
- Le décor et les objets sont plus fins que l'interface : grands aplats pâles, contours discrets, peu de contraste de matière. Les capsules de navigation deviennent les éléments les plus lourds.
- L'écran Collection empile carte de joueur, personnage, bandeau, actions, trois cases et barre inférieure. La répétition de surfaces arrondies met au même niveau identité, décoration et déplacement.
- La maison est bordée de sélecteurs, de zoom et d'actions. Son espace est déjà largement utilisé, mais les contrôles rappellent constamment un outil de configuration.

Ces points sont une analyse de ces deux images, pas une mesure utilisateur.

## Direction proposée : un atelier astral de petites créatures

Une production originale cohérente : créatures de collection à silhouette forte, matières de confiserie et de jouet, vêtements de petits aventuriers, habitat de conte. Garder les identités et les possessions des joueurs. Ne reprendre ni personnage, ni costume, ni logo, ni composition exacte des références.

1. **Personnages** : une silhouette identifiable même en ombre, une pose avec intention, trois masses dominantes, visage expressif lisible à la taille réelle du jeu. Exemple de brief original : Nova, éclaireur curieux, grande mèche comète, écharpe courte et sac lumineux ; Miko, artisan rusé, oreilles anguleuses et veste asymétrique ; Halo, gardien rêveur, anneau suspendu, cape et cristal. Chaque identité exige son propre dessin.
2. **Matières** : une lumière principale constante ; volume peint doux, ombre de contact, reflets limités. Différencier cuir, tissu, verre et bois par leur réponse lumineuse, pas seulement leur couleur. Les petits détails se concentrent sur visage, col, mains et accessoire signature.
3. **Vêtements** : une tenue modifie la silhouette et le récit, pas uniquement la teinte. Les ensembles lune, étoile, récolte, cœur et aurore ont chacun col, manche, chaussure et accessoire distincts. Les 24 pièces existantes doivent rester combinables ; tester leurs ancrages sur les 24 corps.
4. **Maison et meubles** : coque de maison plus narrative, éclairage chaud, angles arrondis, vrais pieds et volumes, tissus épais, objets sur les étagères. Garder les meubles comme couches indépendantes, avec emprise au sol et sélection cohérentes ; une illustration prémeublée ne peut remplacer un espace réellement éditable.
5. **Hiérarchie** : une scène occupe l'essentiel du cadre, un seul foyer lumineux attire l'œil, les ressources restent petites et lisibles. Bannir les grilles de cartes avec fond répété lorsque les objets eux-mêmes peuvent porter la sélection.

## Contrat de navigation : aucun bouton visible

Le mot « bouton » décrit ici une apparence visuelle, et non la disparition des actions ou de leur accessibilité. Pas de capsule, rectangle, barre d'onglets, rangée de commandes ou case transformée en faux bouton. Les noms courts flottent près d'un objet lorsque leur sens est ambigu.

| Besoin | Objet et comportement proposés | Repère qui reste compréhensible |
| --- | --- | --- |
| Jouer | Chemin de la carte, niveau actuel lumineux | Prochain niveau et état accompli visibles |
| Aller à la maison | Maison illustrée / porte visible | Retour par même lieu et retour Android |
| Ouvrir son profil | Toucher son personnage ou son portrait | Pseudo près du personnage, léger regard au toucher |
| Changer de vêtements | Armoire réelle ouverte sur la scène | Pièces sur cintres, aperçu sur le corps |
| Explorer les compagnons | Galerie de personnages sur socles | Noms, possession et rareté près de chaque silhouette |
| Décorer | Toucher un meuble puis le déplacer | Ombre d'emprise, emplacement invalide perceptible sans couleur seule |
| Changer de pièce | Porte / arche propre à chaque destination | Chambre et jardin réellement représentés |
| Invocation | Cristal / machine de rencontre original | Prix et monnaie explicites avant tout débit |
| Récompense | Ouvrir l'objet obtenu | Résultat durable, doublon et compensation lisibles |
| Badges et drapeau | Écussons portés et fanion dans la carte de joueur | État équipé distinct de l'état seulement possédé |

Les gestes complètent ces objets : balayage entre scènes, défilement du catalogue, glisser du mobilier et pincement pour la caméra. Ne pas déclencher la navigation globale sur un glissement destiné à déplacer un meuble. Préserver une zone atteignable d'une main, les cibles de 48 dp, les libellés d'accessibilité et le retour système. Une cible invisible ou un symbole décoratif sans indication d'action ne répond pas au contrat.

Le débit d'une invocation doit être une action distincte d'un simple aperçu. Les probabilités et la réouverture du dernier résultat restent atteignables depuis des objets lisibles ; l'élimination des boutons ne justifie pas leur suppression.

## Chorégraphie proposée et critères de validation

- Entrée de scène : transition courte issue de l'objet touché, avec continuité d'échelle ou déplacement dans un axe cohérent. Budget proposé de départ : 220–300 ms, à ajuster sur appareil ; ce budget est une décision Luma, pas une valeur imposée par les sources actuelles.
- Interaction : réaction dès le contact, puis résultat ; rebond de personnage discret et rare, aucune animation qui retarde une action répétitive.
- Invocation : anticipation, silhouette, révélation, puis résultat stable. La mise en scène ne rejoue jamais le débit. Interruption et retour d'arrière-plan ne doivent pas créer un écran bloqué.
- Mouvement réduit : suppression de secousses, mouvements de caméra et oscillations ; changement court en fondu ou immédiat. La sélection et les informations restent les mêmes.
- Acceptation visuelle : comparer les captures réelles avant/après, en taille normale et compacte ; inspecter tous les personnages et les vêtements réellement équipés, pas seulement une belle image promotionnelle.
- Acceptation d'usage : rejoindre chaque scène, trouver son profil, mettre une tenue et déplacer un meuble sans instruction orale ; relever hésitations, retours inutiles et touchers manqués. L'absence de test avec des joueurs réels doit être indiquée.
- Acceptation technique : aucune possession perdue ; aucune zone tactile recouverte ; retour Android cohérent ; aperçu sans achat accidentel ; transitions répétées sans saut, fuite de mémoire ni frame vide.

Le choix final doit être jugé sur l'art effectivement intégré et sur ces parcours. L'esthétique des grandes références fournit des hypothèses de conception ; leur succès commercial ne garantit pas celui de Luma.
