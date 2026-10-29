# Luma Drift — le puzzle au cœur d'une aventure de collection

Brainstorm de conception et d'économie, 29 octobre 2026.
Recommandation de prototype, à distinguer d'une fonctionnalité livrée ou d'un résultat de joueurs.
Le code audité est celui de `962fc2d69423a5e09c411a2594a8325fe6c20e04`.

## 1. La proposition à tester

**Résoudre des puzzles avec un compagnon, choisir sa route, puis revenir chez soi avec une collection et un lieu qui ont changé.**

Le puzzle donne le plaisir immédiat : comprendre une situation et produire une solution.
L'aventure donne une direction aux prochaines parties.
La collection change les possibilités de cette aventure.
La maison conserve les traces visibles de ce que le joueur a vécu.

Le gacha intervient dans la découverte et dans l'évolution de la collection. Il doit aboutir à une nouvelle décision, un effet à essayer ou un changement à regarder. Une ouverture de plus sans conséquence finit par perdre sa valeur.

Le mot « run » désigne ici une petite expédition renouvelable. Sa route et ses choix temporaires peuvent changer ; les personnages possédés, leurs niveaux et la maison restent permanents. Aucun reset de collection n'est proposé.

Contraintes fixées par l'utilisateur :
- raretés **N / R / SR / SSR**, liées à l'apparence et aux compétences ;
- niveau **1 à 10 par copies du même personnage**, sans XP de personnage ni second axe d'éveil ;
- exemples 1 / 2 / 8 interprétés comme des seuils cumulés ;
- le puzzle reste le cœur du jeu.

Les seuils suivants, les taux, les récompenses et la cadence de nouveaux personnages restent à équilibrer. La courbe à 92 exemplaires du premier document est une illustration, pas une décision.

Une copie doit désigner une identité jouable précise. Les vêtements actuels restent des cosmétiques distincts. Si une future variante change à la fois apparence et compétence, il faut définir son identité et la compatibilité de ses copies ; partager un nom ne suffit pas à partager un niveau. Aucune conversion entre variantes n'est présumée.

## 2. Pourquoi un personnage peut avoir de la valeur

La valeur n'est pas un prix unique. Un même personnage répond à plusieurs attentes :

| Valeur | Question du joueur | Ce qu'il faut produire |
| --- | --- | --- |
| Visuelle | « Est-ce que j'ai envie de le regarder ? » | Silhouette lisible, poses, effets et présence en maison |
| Affective | « Pourquoi celui-ci devient mon préféré ? » | Réactions, petite personnalité, souvenirs et interactions |
| Fonctionnelle | « Que me permet-il de faire ? » | Compétence compréhensible et effectivement utilisable |
| Stratégique | « Dans quel contexte est-il intéressant ? » | Situations différentes, spécialisations et choix |
| De collection | « Qu'est-ce qui me manque et pourquoi ? » | Ensembles cohérents et objectifs choisis |
| De progression | « Que change la prochaine copie ? » | Prochain effet visible et délai crédible |
| D'expression | « Qu'est-ce que cela raconte de ma façon de jouer ? » | Maison, compagnon exposé, souvenirs de maîtrise |
| De nouveauté | « Qu'est-ce que je vais découvrir ? » | Incertitude réelle et renouvellement de situations |

La rareté peut accentuer l'anticipation. Elle ne remplit pas automatiquement les autres lignes.
Un SSR jamais utilisé n'a pas la même valeur qu'un SR qui change régulièrement un choix apprécié.
La même copie peut avoir une forte valeur juste avant un palier et une faible valeur quand le prochain effet est extrêmement lointain.

Conséquence : concevoir d'abord une situation où le personnage est désirable, puis son mode d'acquisition.

## 3. Les références : mécanismes observés et limites

### Crash Fever : relier besoin, personnage, événement et collection

Le Festival d'octobre 2026 introduit un danger qui réduit le maximum de touches ; une capacité précise permet de restaurer ce maximum. L'utilité du personnage s'observe dans une situation. Cela ne démontre ni son caractère indispensable ni un gain de rétention. [Annonce officielle](https://crashfever.com/campaign/2026093045526/).

Halloween 2026 réunit personnage thématique, quêtes, échanges, missions, retours d'anciens contenus et décor d'accueil. Des activités arrivent en plusieurs vagues. L'ensemble donne au personnage un contexte et au calendrier plusieurs moments de découverte. La leçon transférable est de construire un événement cohérent autour d'un usage, d'un objectif et d'un souvenir. [Programme officiel du 28 octobre 2026](https://crashfever.com/campaign/2026102845706/).

Traversal rend temporairement indisponible pour les étapes suivantes une unité déjà utilisée pour terminer une étape. Posséder davantage de personnages reçoit donc un usage. Luma peut retenir l'idée de varier les spécialités, sans imposer d'emblée plusieurs équipes ou une collection obligatoire. [Règles officielles](https://crashfever.com/faq/2023031632231/).

MY ALICE fournit une autre valeur : espaces décorables, interactions avec les unités et informations personnelles déverrouillées par l'affection. C'est particulièrement pertinent pour le lien entre notre collection et notre maison. Cela ne justifie pas d'ajouter une XP d'affection dans le modèle demandé. [FAQ officielle](https://crashfever.com/faq/2025072342072/).

### Brawl Stars : surprise, progression choisie et erreurs de valeur

Dans son bilan du 1er juillet 2025, Supercell attribue une partie du redressement de Brawl Stars au retour des récompenses aléatoires, tout en réaffirmant l'intérêt des récompenses déterministes. Le même texte décrit des systèmes de maîtrise peu compris, du grind et une redistribution de récompenses mal reçue. Leçon : la quantité totale, sa présentation et la manière de l'obtenir comptent ensemble. Ce récit interne n'isole pas un effet causal du hasard. [Bilan officiel](https://supercell.com/en/games/brawlstars/blog/game-updates/about-the-update-records-changes/).

Le bilan du 7 mars 2026 expose aussi les effets d'un personnage temporaire trop puissant et omniprésent, des conséquences inattendues sur les crédits et l'intérêt de thèmes événementiels cohérents. Une acquisition généreuse peut dégrader les décisions ou vider une autre récompense de son sens. [Bilan officiel 2025](https://supercell.com/en/games/brawlstars/blog/community/2025-in-review-franks-blog-post/).

### Genshin : acquisition finie et protection contre la malchance

Ses copies ont des usages bornés et une compensation après le maximum. Ses garanties distinguent l'obtention d'une rareté et celle du personnage vedette. Pour Luma, retenir des promesses lisibles : obtenir un SSR, obtenir son personnage choisi et faire évoluer un personnage déjà possédé sont trois objectifs différents. Son système d'XP n'est pas transposé. [Doublons](https://support.hoyoverse.com/hc/en-us/articles/52089402385945-What-rewards-do-I-get-for-obtaining-a-duplicate-character-near-or-at-C6) · [Garanties](https://support.hoyoverse.com/hc/en-us/articles/50333940684953-How-does-the-Wish-guarantee-system-work).

Ces jeux servent à comprendre des mécanismes. Leur audience existante, leur distribution, leurs équipes et leurs univers ne sont pas ceux de Luma Drift.

## 4. Psychologie : ce qu'on peut soutenir

L'étude originale de Ryan, Rigby et Przybylski relie autonomie, compétence perçue et liens sociaux au plaisir et à l'intention de poursuivre. Elle soutient l'intérêt d'un objectif choisi et d'une maîtrise ressentie ; elle ne mesure pas notre D7 ni un gacha mobile moderne. [Article de 2006](https://selfdeterminationtheory.org/SDT/documents/2006_RyanRigbyPrzybylski_MandE.pdf).

Les expériences de Larche et collègues observent davantage d'excitation, de valeur subjective et d'envie de rouvrir pour des récompenses rares dans des ouvertures contrôlées d'Overwatch. Elles ne prouvent pas que baisser indéfiniment un taux augmente la rétention, ni qu'une animation provoque un succès commercial. [Publication en ligne 2019, numéro 2021](https://pmc.ncbi.nlm.nih.gov/articles/PMC7882574/).

L'enquête de Zendle et Cairns trouve une association entre dépenses de loot boxes et gravité de problèmes de jeu d'argent. L'échantillon volontaire et transversal ne détermine pas la direction causale. Il faut donc distinguer envie de jouer, envie de rouvrir, dépenses et perte de contrôle ; une hausse d'ouvertures ne suffit pas à démontrer une meilleure expérience. [Étude originale 2018 et lien vers sa correction](https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0206767).

Pour le design, les hypothèses utiles sont concrètes :
- anticipation : j'ai une chance de découvrir quelque chose que je désire ;
- autonomie : j'ai choisi un projet et ma session le fait avancer ;
- maîtrise : mes choix de puzzle et de parcours comptent ;
- continuité : ce que j'ai gagné aujourd'hui reste utilisable ;
- nouveauté : la prochaine sortie peut proposer une situation différente ;
- attachement : mon compagnon et ma maison portent une histoire.

La boucle doit donner un résultat satisfaisant quand aucun SSR ne tombe. Sinon, presque toutes les sessions ordinaires deviennent des déceptions.

## 5. Trois concepts possibles

| Concept | Décision principale | Ce que la collection change | Coût de production et risque |
| --- | --- | --- | --- |
| **Les expéditions de Luma** | Choisir un itinéraire, un compagnon et l'usage de sa signature | Manière d'aborder une sortie, choix et découvertes | Intermédiaire ; éviter les branches toujours optimales |
| **Les quartiers vivants** | Composer des scènes de maison et choisir un projet d'habitants | Interactions, scènes, souvenirs et compositions | Art/animation élevés ; éviter un album réduit à une liste d'achats |
| **L'atelier des signatures** | Choisir le bon outil et le moment d'une intervention sur le puzzle | Nouvelles façons de résoudre | Validation élevée ; éviter une compétence dominante ou des plateaux triviaux |

**Recommandation : prototyper les expéditions, puis enrichir leurs retours par des scènes de maison.**
Les signatures modifiant les règles de puzzle constituent une piste suivante, après démonstration de leur intérêt et qualification des nouvelles règles.

## 6. L'expédition : le jeu dans le jeu

### Le départ

Le joueur choisit :
1. un compagnon actif ;
2. un objectif personnel, par exemple développer ce compagnon ou un projet de maison ;
3. une destination dont le thème, le principe et le gain principal sont annoncés.

Un seul compagnon actif au départ rend son rôle compréhensible. Une équipe n'est pas nécessaire pour tester l'intérêt de la collection.

### La route

Première proposition : **quatre ou cinq puzzles**, avec sauvegarde après chaque étape. Leur durée réelle reste à mesurer.
La route est une succession de décisions qui modifient la situation suivante.

Une bifurcation montre deux destinations : aperçu du prochain défi, particularité de la situation et récompense principale.
Le défi immédiatement suivant est visible. Une rencontre située au-delà de cette étape peut rester inconnue : c'est cette information future que la signature de révélation peut dévoiler.
Choisir « copies ou meuble » donne une direction économique. Pour devenir du jeu, la route doit aussi changer ce que l'on va affronter.

Exemples à prototyper séparément :
- une route de dépendances de portes contre un détour de clés ;
- un défi de lecture contre une variante de planification ;
- un trajet prévisible contre une rencontre à contenu inconnu, avec gain principal annoncé.

Les branches doivent avoir des intérêts différents. Une branche plus facile et toujours plus rentable supprimerait le choix.

### La signature du compagnon

Première famille de prototypes : des compétences de parcours, qui conservent les règles des puzzles existants.
- Révéler la rencontre qui suit le prochain défi sur une branche avant de choisir, alors que l'autre branche conserve cette incertitude.
- Remplacer une proposition de destination une fois dans la sortie.
- Ouvrir une option de projet dans un contexte défini.

Ces exemples sont des idées nouvelles, sans attribution aux personnages actuels ni valeurs validées.
La compétence est disponible au niveau 1. Les copies améliorent cette même compétence selon dix niveaux, avec des plafonds explicites.
Une compétence qui augmente tous les gains deviendrait difficile à concurrencer ; une spécialisation doit conserver des contextes où une autre est préférable.

Seconde famille, plus coûteuse : intervention limitée et prévisualisée sur un puzzle d'expédition. Elle doit créer une situation à lire plutôt qu'exécuter automatiquement une solution.
Les plateaux, certificats, sauvegardes et interactions avec les pouvoirs doivent alors être validés pour ces règles nouvelles.

### Les choix temporaires d'une sortie

Une piste de profondeur ultérieure : rapporter d'une étape un outil pour la suivante, parmi deux choix visibles.
Un seul outil transporté et un seul usage évitent un inventaire supplémentaire.
Cette piste transforme la continuité entre puzzles ; elle nécessite des variantes conçues pour ces outils. Elle n'est pas incluse dans le premier périmètre recommandé.

### Le retour

Le résultat montre ce qui a réellement changé :
- une découverte ou une copie du compagnon ;
- le total de stacks et l'effet du prochain niveau ;
- une contribution à un projet choisi ;
- un objet, une animation ou un souvenir visible dans la maison.

Le tirage apporte une découverte supplémentaire ; une progression principale annoncée demeure acquise même sans rareté exceptionnelle.
Les gains de chaque étape sont enregistrés avant leur animation. Une interruption conserve la sortie et les gains déjà acquis.

### Exemple de séance, sans promesse de durée

Le joueur choisit son compagnon et un projet de jardin.
Après un puzzle, il voit un détour d'observatoire et une route de serre, dont les défis diffèrent.
Sa signature lui permet de révéler l'une des destinations suivantes. Il choisit sa route à partir de cette information.
La finale donne le gain principal annoncé et une ouverture de collection.
À la maison, son projet change visiblement ; la nouvelle copie rapproche le compagnon du prochain effet.
La session suivante peut partir d'un autre objectif, d'un autre compagnon ou d'une autre destination.

Le signe de profondeur recherché est que le joueur raconte une décision ou une surprise de parcours, en plus de son butin.

## 7. Économie : partir de l'état réel

L'audit actuel établit :
- tirage compagnon : 180 pièces ; 23 personnages, dont cinq épiques à 2 % chacun ;
- remboursements de doublons : 45 / 75 / 120 pièces ;
- tous les compagnons sont aussi achetables directement ; le rachat d'une copie est actuellement refusé ;
- aucune compétence ni niveau propre au compagnon ;
- maison : 10 030 pièces de meubles payants et 680 de surfaces ;
- compagnons payants : 12 200 pièces ; thèmes : 1 530 ;
- ces trois catalogues totalisent **24 440 pièces** d'achats définitifs ;
- 1 000 premières victoires de campagne à trois étoiles représentent **55 000 pièces** ;
- le scénario complet de récompenses récurrentes représente **815 pièces/jour en moyenne arithmétique**.

Les 815 pièces supposent toutes les récompenses quotidiennes/hebdomadaires à trois étoiles, toutes les missions et tous les cadeaux. Ce n'est ni le joueur moyen ni une durée de session.
24 440 / 815 donne environ 30 équivalents de journée complète pour ces trois catalogues, en ignorant les autres achats et les revenus de campagne. Cela met en évidence un stock fini de dépenses face à des sources récurrentes, pas une durée observée pour terminer le jeu.

[HomeCatalog](../../../app/src/main/java/com/androidhit/lumadrift/data/HomeCatalog.java) · [Catalog](../../../app/src/main/java/com/androidhit/lumadrift/data/Catalog.java) · [GameStore](../../../app/src/main/java/com/androidhit/lumadrift/data/GameStore.java) · [MetaProgress](../../../app/src/main/java/com/androidhit/lumadrift/data/MetaProgress.java).

### Une modification des doublons modifie aussi le prix réel

Avec toute la collection possédée, le remboursement moyen actuel vaut :
60 % × 45 + 30 % × 75 + 10 % × 120 = **61,5 pièces**.
Le coût net moyen est donc 180 − 61,5 = **118,5 pièces**.

Remplacer le remboursement par une copie, sans rendre de pièces, porte ce coût net à 180 : **environ 52 % de dépense nette supplémentaire** dans cet état de collection.
Conserver remboursement et copie ajoute une nouvelle valeur au coût actuel.
Il faut comparer explicitement ces politiques. Un prix affiché inchangé ne signifie pas une économie inchangée.

### Les flux à modéliser

Solde de pièces suivant = solde présent + récompenses de jeu + remboursements − tirages − achats permanents − pouvoirs.

Copies d'un personnage suivantes = copies présentes + tirages de ce personnage + copies ciblées + échanges compatibles.
Le niveau est déduit des seuils ; il n'y a pas de dépense d'XP.
Les copies au maximum produisent une conversion connue ; les niveaux acquis ne reculent pas.

Les compétences peuvent agir sur plusieurs flux à la fois : augmenter les gains, réduire les dépenses et améliorer la réussite. Il faut mesurer leur effet combiné.

### Trois états de joueur

| État | Valeur dominante à tester | Risque |
| --- | --- | --- |
| Débutant | Comprendre, choisir un premier favori, voir un changement | Trop de systèmes avant le plaisir du puzzle |
| Installé | Développer un favori, essayer une spécialité, construire un lieu | Tout objectif semble exiger la même monnaie |
| Avancé | Maîtrise, expressions de collection, événements renouvelés | Monnaies abondantes et récompenses sans utilité |

Ajouter des prix toujours plus élevés ne remplace pas le renouvellement de situations intéressantes.

### Les pièces, la maison et la progression

Quand les personnages deviennent fonctionnels, dépenser dans la maison peut sembler sacrifier sa puissance.
Une progression ciblée gagnée en jouant permet de décorer tout en développant un favori.
Il faut comparer cette option à une économie entièrement en pièces, en conservant les droits et acquisitions existants.

Recommandation initiale : minimiser les monnaies supplémentaires. Les pièces actuelles, les copies par personnage et, si nécessaire, une monnaie d'événement explicitement bornée suffisent pour un premier prototype.
Un ticket ne doit être ajouté que s'il apporte une fonction compréhensible que ces registres ne remplissent pas.

## 8. Rareté, niveaux et taille du catalogue

Les seuils 1 / 2 / 8 signifient déjà une copie supplémentaire pour Lv2, puis six pour Lv3.
La courbe après Lv3 doit découler :
1. de l'effet obtenu ;
2. du délai souhaité et de ses quantiles ;
3. du flux de copies accessible au rythme du joueur ;
4. du rôle de la rareté dans la première acquisition.

À 0,005 % pour un personnage précis, huit copies demandent 160 000 tirages en moyenne depuis zéro sans garantie. Cette hypothèse rend une source ciblée structurelle si l'évolution est une promesse normale du jeu.
Si le taux concerne la catégorie SSR entière, le personnage visé est encore plus rare.

L'augmentation du catalogue peut diluer une cible : à probabilité de catégorie fixe P et M personnages équiprobables, son taux individuel vaut P / M.
Introduire davantage de personnages sans réviser pools, sélection et garanties rend donc certaines évolutions plus lointaines.

Une bannière doit annoncer :
- ses personnages et leurs probabilités individuelles ;
- la différence entre nouveau personnage, copie utile et surplus ;
- sa garantie de rareté et son éventuelle garantie ciblée ;
- les compteurs conservés ou remis à zéro ;
- la destination des ressources en fin de période.

Les objectifs choisis peuvent tenir compte de l'inventaire. Les taux de tirage ne doivent pas changer secrètement selon le solde ou les dépenses du joueur.

## 9. Événements : renouveler l'usage de la collection

Un événement complet possède six éléments :
1. **une promesse** : lieu, thème et situation immédiatement compréhensibles ;
2. **une variation jouable** : route, défi ou décision particulière ;
3. **un personnage à comprendre et essayer**, avec un prêt clairement temporaire si nécessaire ;
4. **un chemin principal accessible** avec les compagnons disponibles ;
5. **un projet choisi** : copie ciblée, contribution ou récompense annoncée ;
6. **une trace permanente** : personnage, niveau, souvenir ou scène de maison.

Une nouvelle apparence et un nouveau compteur ne suffisent pas à renouveler le jeu.
Le personnage vedette peut ouvrir une approche différente ; il ne doit pas constituer l'unique moyen d'obtenir les récompenses qui financent sa propre acquisition.

Exemple de thème : un observatoire à remettre en lumière.
Une route travaille les dépendances de portes, une autre les clés.
Le compagnon proposé se découvre dans une sortie d'essai ; plusieurs compagnons existants offrent d'autres approches.
Le joueur choisit un projet, gagne sa progression et ramène un souvenir de l'observatoire.
Une seconde vague peut ajouter une route de maîtrise au même cadre, sans effacer le progrès antérieur.

Les cadences session / semaine / événement sont des hypothèses de contenu.
Le calendrier doit être compatible avec le travail réellement nécessaire pour un personnage, ses compétences aux dix niveaux, son art, ses interactions, ses textes et sa validation.
Un nouveau SSR chaque mois n'est pas une capacité de production établie.

## 10. La valeur commerciale et l'ambition de hit

Le potentiel commercial dépend d'un enchaînement :
- une promesse de jeu compréhensible et reconnaissable ;
- une première expérience satisfaisante ;
- un projet qui donne une raison de continuer ;
- des événements et des personnages qui rendent le retour intéressant ;
- une distribution capable de toucher le public visé ;
- une production et une exploitation soutenables.

Le gacha peut contribuer à l'attachement, à l'anticipation et aux sujets à partager.
L'économie doit aussi conserver sa valeur pour le joueur gratuit, l'ancien joueur et celui qui revient après une pause.
Les revenus futurs, la conversion, les coûts d'acquisition et la rétention de Luma ne sont pas mesurés ici.

La monétisation constitue une décision ultérieure sur la valeur proposée et ses modalités. Aucun paiement ni tirage premium n'est activé par cette étude.
Le prototype de la boucle peut être évalué avec des ressources gagnées en jouant.

## 11. Ce qu'on simule et ce qu'on doit observer

### Simulation économique

Déclarer les hypothèses de fréquence de jeu, réussite, réclamations, dépenses et choix.
Calculer par état de collection :
- médiane et quantiles du temps vers un favori, Lv2, Lv3 et Lv10 ;
- probabilité de terminer une période sans copie utile ni palier atteint ;
- proportion de nouvelles acquisitions, copies utiles et surplus ;
- soldes de pièces, pouvoirs et ressources inutilisées ;
- concurrence entre maison, tirages et compétences ;
- effets de chaque politique de remboursement ;
- dilution par nouveaux personnages ;
- avantage d'une compétence économique et stratégies dominantes ;
- effets d'une pause ou d'un événement manqué.

Une trajectoire financièrement possible ne prouve pas qu'elle soit désirable.

### Observation de joueurs

Vérifier si le joueur :
- comprend la compétence et peut raconter pourquoi il a choisi ce compagnon ;
- distingue les branches pour leurs situations et leurs gains ;
- essaie une acquisition après l'avoir reçue ;
- sait quel est son prochain projet ;
- retourne jouer après la récompense ;
- retrouve sa progression après une interruption ;
- revient volontairement, avec un plaisir déclaré qui se maintient.

### Comparaison de variantes

Premier test : intérêt du parcours jouable à valeur de récompenses comparable.
Second test, quand la population le permet : progression déterministe contre mélange de choix, surprise et garantie, à contenu et budget comparables.

Mesurer D1/D7/D30 avec définitions de cohortes, source d'acquisition, version, maturité et incertitude statistique.
Quelques entretiens révèlent des problèmes de compréhension ; ils ne suffisent pas à établir une différence fiable de rétention.
Un gain d'ouvertures sans meilleur usage des personnages ni retour supplémentaire ne valide pas la promesse.

## 12. Périmètre recommandé du premier prototype

- Une destination et une petite expédition de quatre ou cinq puzzles vérifiés.
- Deux bifurcations qui changent le prochain défi.
- Trois compagnons existants dotés de signatures de parcours différentes ; attribution et valeurs encore à concevoir.
- Une prévisualisation des compétences et une progression par copies respectant les trois premiers seuils.
- Un objectif de compagnon et un objectif de maison entre lesquels choisir.
- Une progression principale annoncée et une découverte aléatoire en finale.
- Une scène de retour et un souvenir dans la maison.
- Sauvegarde après chaque étape et reprise exacte.
- Le catalogue complet N/R/SR/SSR, les taux définitifs et tous les dix niveaux sont décrits avant généralisation, mais la première expérience peut tester peu de personnages.
- Aucun besoin de multijoueur, d'équipe complète, de nouvelle XP, de dix monnaies ou de paiement pour vérifier cette boucle.

L'accès et les choix doivent prolonger la présentation actuelle : destination illustrée, objets nommés et touchables, informations accessibles avant engagement. La carte de sortie ne doit pas réintroduire une grille de tâches et des rangées de boutons comme structure principale. Les chances et conditions restent lisibles dans un détail accessible.

Une fois cette petite boucle comprise et rejouée volontairement, on peut équilibrer les niveaux suivants et étendre les événements. Le résultat recherché est un parcours où les puzzles, la collection et la maison se donnent mutuellement de la valeur.

