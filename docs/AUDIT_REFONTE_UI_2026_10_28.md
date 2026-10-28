# Luma Drift — audit de refonte du jeu et de l’interface

28 octobre 2026 · Base examinée : 1.14 RC2 / code 16, dépôt `adc51c8f`.

**Statut : analyse et proposition de conception. Aucune des modifications ci-dessous n’est encore intégrée à l’application.**

Le retour du joueur est le point de départ : ouvrir un mur par un clic est peu intéressant, la maison est trop petite, les objets changent peu la scène, les pages concentrent trop de fonctions et les fenêtres ressemblent à des listes de texte. L’objectif est une refonte de la boucle de jeu, de la navigation et de la présentation, dans un esprit puzzle japonais expressif, avec des surfaces douces et des objets immédiatement reconnaissables.

## 1. Ce qui a été examiné

- Code et documentation de la version livrée, sans modification ni lancement du jeu pendant le test utilisateur.
- Captures natives finales : `public/assets/screenshots/v1.14/puzzle-fr.png`, `home-fr.png`, `furniture-fr.png`, `house-decor-fr.png`.
- Sources officielles de Crash Fever, Dream Games, King, Playrix et Nintendo, consultées le 28 octobre 2026. Les captures de support King sont des exemples publiés par l’éditeur, pas une session jouée par l’auditeur. Les pages marketing n’établissent pas le comportement complet de l’application actuelle.

Les constats du code, les observations des sources et les propositions sont séparés ci-dessous. Aucun score esthétique, résultat de rétention ou temps humain de complétion n’est inventé.

## 2. Diagnostic : le problème dépasse les couleurs

| Constat vérifié | Conséquence pour le joueur | Changement proposé |
| --- | --- | --- |
| `MainActivity` installe une grande `LumaView` ; navigation, pages et overlays partagent leur état. Largeur logique 390 et hauteurs de pages fixées manuellement. | On agrandit les pages en empilant les fonctions ; l’espace se recompose peu selon le téléphone. | Destinations avec leur propre état, composants partagés et disposition adaptable. Garder des vues spécialisées pour les scènes de jeu. |
| Dans le jeu, titre, personnage, explication et budget précèdent un plateau fixe de 280 × 280 ; les commandes et pouvoirs suivent. | Le plateau rivalise avec l’habillage ; les petits écrans doivent faire défiler des commandes importantes. | HUD compact, plateau dominant, une seule rangée d’actions essentielles ; détails accessibles à la demande. |
| La maison est aussi le hub : scène, câlin, vêtements, décoration, Jouer, souhait, récompenses et autres destinations se suivent. | La pièce est perçue comme une illustration dans un long menu. | Maison consacrée à la scène, mode décoration distinct ; Aventure et activités ont leur destination. |
| Garde-robe, équipement, inventaire et acquisition se côtoient dans de longues pages. | Comparer les apparences et comprendre les possessions demande de lire et de naviguer dans beaucoup de lignes. | Collection visuelle, cabine d’essayage dédiée et acquisition accessibles depuis la fiche. |
| La victoire additionne étoiles, monnaies, cadeau, essai de pouvoir ou souhait, gardien, suivant, partage, maison et récompenses. | La récompense principale et la prochaine action perdent leur hiérarchie. | Victoire courte : résultat visuel, récompense réelle, gros bouton Suivant ; les récompenses annexes rejoignent leur destination. |
| Dialogues Android textuels, overlays dessinés et écrans secondaires utilisent des présentations différentes. | Le produit manque d’un langage visuel commun. | Un petit nombre de composants de panneaux, avec illustration, titre, conséquence et action principale. |

Pointeurs : `MainActivity.java:20`, `LumaView.java:241,379,1003,1031,1151,1240,1378,1501,1680,1720,1761,1870,2052`, `ui/ExperienceLayout.java:10` dans `app/src/main/java/com/androidhit/lumadrift/`.

## 3. Les portes : remplacer le geste obligatoire sans décision

### Faits

Le « −1 » est le coût d’une ouverture manuelle. `Board.previewAction` permet d’ouvrir toute porte fermée sans prérequis de clé ou de position. Dans les quatre leçons N13–N16, une porte est fermée et aucun interrupteur n’existe. L’ouvrir au début ou à la fin ne change donc pas le raisonnement : le joueur apprend une commande et paie une action.

Dans les niveaux suivants, les interrupteurs peuvent refermer une porte : leur ordre apporte alors de vraies conséquences. Le défaut ne s’applique donc pas à toutes les interactions avec des portes.

Pointeurs : `engine/Board.java:156,269`, `engine/LearningLayouts.java:19–22`, `engine/StrategySolver.java:58`, `LumaView.java:1795`.

### Proposition recommandée

**Prototyper une route où les pièces commandent les portes.** Une clé qui sort libère son sceau ; une pièce relais ouvre une sortie ou change deux accès. Le joueur touche une pièce pour modifier la situation du plateau. Une trajectoire ou un lien bref montre la relation entre la pièce et la porte avant et après le geste.

- Un déplacement choisi coûte une action ; ses effets automatiques sur portes et sceaux ne demandent pas un deuxième clic payant.
- L’animation confirme un effet réel et garde la lecture de l’état final claire.
- Les premiers niveaux n’introduisent qu’une relation causale, puis deux conséquences opposées.
- L’erreur doit être compréhensible et récupérable ; comparer le Return limité actuel à une correction plus permissive pendant le prototype, avant de fixer la règle.
- Les essais bloqués restent gratuits et recommencer reste gratuit.

Deux alternatives secondaires : un relais qui ouvre une porte en fermant une autre ; une charge obtenue sur le plateau à affecter à une sortie. La seconde ajoute une ressource et une règle à apprendre : elle n’est utile que si elle crée un choix perceptible.

**Première tranche : 12–20 niveaux prototypes.** Retirer l’ouverture manuelle des 1 000 plateaux actuels sans les réécrire peut les rendre impossibles. Un changement de règle exige une nouvelle famille de contenu, des certificats adaptés et la conservation des anciennes parties actives. Un modèle avec activations cycliques nécessite aussi une révision du solveur.

## 4. Ce que les références apportent réellement

| Référence officielle | Élément documenté ou observé | Application proposée à Luma Drift |
| --- | --- | --- |
| [Crash Fever — fonctionnement et MY ALICE](https://crashfever.com/faq/faq_cat/faq2/?mode=app), [types de panneaux](https://crashfever.com/faq/2025070141778/) | Le tap produit des attaques/soins ; les liens génèrent des panneaux spéciaux et des compétences. MY ALICE dispose de trois espaces personnalisables et d’interactions avec les unités. | Chaque geste doit avoir une conséquence visible ; personnages et compétences ont une présence illustrée. Donner à la maison une destination et une identité propres. |
| [Royal Match — direction artistique](https://www.dreamgames.com/games/royal-match) | Dream Games décrit un monde aux formes courbes, cohérent entre objets de puzzle, pouvoirs et lieux. Les zones changent de fonction et de composition. | Définir un langage commun d’illustration et de matière ; une nouvelle pièce doit transformer les volumes et l’ambiance. |
| [Candy Crush — boosters et captures](https://candycrush.zendesk.com/hc/en-us/articles/360000750998-What-are-Boosters) | L’exemple avant niveau regroupe numéro, objectifs illustrés, boosters distincts et bouton principal. | Donner aux panneaux quelques groupes visuels lisibles, une grande image et une action dominante. |
| [Homescapes — mode Design](https://playrix.helpshift.com/hc/en/14-homescapes/faq/5810-can-i-redesign-my-mansion/) | Un appui long permet de changer le design d’un objet ; sa position reste fixe. | Un mode de décoration séparé rend la personnalisation claire. Les emplacements fixes sont une option de portée réduite ; Luma possède déjà le placement libre, qu’il faut rendre plus confortable. |
| [Pocket Camp Complete — présentation Nintendo](https://www.nintendo.com/au/news-and-articles/animal-crossing-pocket-camp-complete-is-now-available/) | Des objets aussi différents qu’une tente, une balançoire ou un manège construisent la scène. | Donner des silhouettes, encombrements et usages distincts aux acquisitions. Une poignée d’objets forts change davantage une pièce que des variations de teinte. |

Ces mécanismes sont des références de conception, pas la preuve qu’ils expliquent seuls le succès commercial de ces jeux. Crash Fever sert de référence pour l’expression visuelle et le lien geste/effet ; reproduire ses menus ou sa complexité accumulée ne résoudrait pas automatiquement la densité de Luma.

[King explique avoir supprimé le menu principal de Candy Crush](https://candycrush.zendesk.com/hc/en-us/articles/39931244875933-Where-can-I-find-my-settings-and-account-options) à la suite de retours sur le nombre d’écrans avant de jouer. Conséquence proposée : spécialiser les destinations, tout en gardant Jouer/Reprendre immédiatement accessible. Le parcours ne doit pas devenir une succession de sas.

## 5. Architecture des écrans proposée

| Destination | Contenu principal | Ce qui s’ouvre séparément |
| --- | --- | --- |
| **Aventure** — arrivée recommandée | Chapitre, carte, niveau courant, Jouer/Reprendre visible sans défilement. | Événements et leurs règles ; paramètres depuis une commande discrète. |
| **Jeu** — plein écran hors navigation globale | Objectif compact, coups, grand plateau, Return/aide, pouvoirs utiles. | Pause, détail d’une règle, bilan. Aucun catalogue de meubles pendant la partie. |
| **Maison** | Pièce ample, compagnon, sélection de pièce, caméra et Décorer. | Mode édition, catalogue de meubles, détail d’un objet. |
| **Collection** | Personnages, vêtements, équipements et possessions en grilles visuelles. | Cabine d’essayage avec grand aperçu et comparaison avant/après ; boutique accessible ici. |
| **Atelier** | Ensembles de décoration, prochains objets à obtenir, aperçu et source réelle. | Fiche d’acquisition ou placement. Les offres monétaires ne doivent pas se confondre avec les possessions. |

Quatre destinations globales : Aventure, Maison, Collection, Atelier. Le jeu et les détails ont leur propre écran avec un retour clair. Les événements restent une entrée visible depuis Aventure. Une autre organisation peut fonctionner ; le critère est le trajet réel, pas le nombre d’onglets.

## 6. Maison : plus d’espace et des changements perceptibles

### Base vérifiée

- Une pièce de **6 × 6**, 12 meubles, un exemplaire maximum par type, 4 murs et 4 sols.
- Scène définie dans un espace fixe de **390 × 380**, sans caméra dédiée ; le sol projeté occupe environ 336 × 186.
- La rotation logique change l’empreinte, mais le dessin du meuble est retourné horizontalement ; ce n’est pas une deuxième orientation dessinée.
- Murs et sols changent surtout de teinte ; leurs lignes et composition restent proches.
- Les interactions partagent plusieurs poses communes et ne persistent pas comme activité ou relation.

Pointeurs : `data/HomeCatalog.java:9,30`, `data/HomeLayout.java:19,49,79`, `ui/HomeScene.java:16,34,141,244,368` ; contrat `docs/HOME_1_13.md`.

### Cible de conception, à réaliser par tranches

1. **Maison immersive** : scène occupant environ 65–75 % de la surface utile ; déplacement de caméra et zoom. Agrandir la grille sans cela réduirait simplement la taille des objets.
2. **Salon 10 × 10** comme cible initiale à essayer ; chambre et serre/terrasse ensuite, avec transitions visibles. Ces dimensions sont une proposition, pas une norme observée chez les références.
3. **36–48 objets distincts** comme horizon de catalogue, déployés par ensembles : céleste, forêt, atelier, urbain lumineux. Le décompte ne doit pas intégrer les simples recolorations comme nouveaux dessins.
4. Fenêtres, étagères murales, tapis, grands meubles, plantes hautes et petites décorations posables : changer la silhouette de toute la scène, pas seulement sa couleur.
5. Plusieurs exemplaires pour les objets adaptés ; véritables orientations dessinées ; aperçu à l’échelle dans la pièce avant acquisition et placement.
6. Deux ou trois interactions abouties au départ : lecture avec livre visible, repos sur le lit, lumière modifiant un coin de la pièce. Les réactions doivent se distinguer réellement.
7. Extensions et pièces obtenues par progression ; choix personnel avec les coins existants. Montrer une transformation avant/après lorsqu’une pièce ou un objet important est débloqué.

L’économie doit suivre cette expansion. Les sept meubles achetables coûtent ensemble 1 160 coins, contre 180 au départ et 35–55 par première victoire. Théoriquement, sans aucune autre dépense, environ 18 victoires à trois étoiles financent ce catalogue mobilier. Ce calcul ne mesure pas une durée de jeu humaine. Une collection plus durable demande des ensembles et sources variés ; multiplier seulement les prix ajouterait de l’attente.

## 7. Langage visuel et panneaux

La douceur vient de la composition, des proportions, de la matière et du mouvement, autant que du rayon des angles.

- Fond calme derrière les commandes ; illustrations riches dans une zone principale. Le paysage détaillé ne doit pas concurrencer chaque carte du catalogue.
- Silhouette et symbole en plus de la couleur : vêtements, pouvoirs et meubles doivent rester identifiables en petite vignette.
- Avatar grand dans sa cabine ; objet grand dans sa fiche ; plateau grand pendant le jeu. Chaque écran met en avant son sujet.
- Typographie avec trois niveaux usuels et alignements stables. Les longues descriptions quittent la vue initiale.
- Panneau de règle : mini-démonstration ciblée, une phrase et « Compris » ; aide approfondie accessible séparément.
- Panneau d’acquisition : grand objet, nom, coût/source, état possédé, action principale. Essayer, acquérir et équiper/placer restent des actions explicites.
- Victoire : scène courte, étoiles et gains réels, Suivant dominant. Pas de succession de sollicitations à fermer.
- Erreur : message près de la cause ; confirmation modale réservée aux décisions qui en ont besoin.
- Animation de pression, mouvement causal bref, retour sonore/haptique optionnel ; respect du mouvement réduit et des réglages audio.

## 8. Ordre d’exécution recommandé

| Tranche | Résultat concret | Critère de passage |
| --- | --- | --- |
| **A — règle et structure** | 12–20 plateaux prototypes et maquettes navigables Aventure → Jeu → Victoire / Maison → Décorer / Collection → Essayer. | Les décisions du puzzle se comprennent par l’action ; aucune ouverture obligatoire sans choix. Jouer ne requiert pas une série de menus. |
| **B — boucle jouable** | Nouvelle navigation, plateau adaptable, panneaux communs, victoire courte, première maison avec caméra et petite sélection d’objets aboutis. | Parcours complet sur un vrai téléphone ; commandes essentielles visibles, aucun panneau qui coupe le texte ou bloque une action. |
| **C — contenu et progression** | Deuxième puis troisième pièce, catalogue de 36–48 objets, ensembles et sources, orientations et instances multiples. | Les acquisitions modifient nettement la scène ; placement et comparaison restent simples. |
| **D — livraison** | Nouvelle version identifiée, sauvegardes migrées, APK signé, captures réelles et documentation correspondant au jeu. | Compatibilité et transactions vérifiées, validation visuelle et tactile, limites explicitement enregistrées. |

Commencer par un petit parcours complet évite de redessiner tous les écrans autour d’une interaction principale encore faible. Le test joueur doit continuer à guider le choix de règle et la densité.

## 9. Critères proposés pour vérifier la refonte

Ces seuils sont des objectifs de conception, pas des performances déjà mesurées.

- Plateau et commandes essentielles entièrement visibles sans défilement à la taille cible ; dispositions alternatives pour paysage et grands textes.
- Cibles tactiles d’au moins 48 dp, textes lisibles et vrai parcours TalkBack pour les actions et les états.
- Une action principale par panneau ; explication initiale de trois lignes maximum, détail séparé si nécessaire.
- Maison occupant au moins 65 % de la surface utile ; caméra permettant de voir de grands objets sans rétrécir tout le contenu.
- Sur les premiers prototypes, demander au joueur de prédire la conséquence d’une pièce et d’expliquer une erreur. Consigner aide demandée, clics sans choix et malentendus.
- Comparer des captures à l’échelle du téléphone : la différence entre deux meubles ou vêtements doit être identifiable par la forme, la matière ou la pose, sans lire leur nom.
- Vérifier la sobriété de la victoire, le retour au jeu, la reprise après interruption et l’annulation de l’édition maison.
- Les contrôles automatisés de solvabilité restent nécessaires ; compréhension, plaisir et retour volontaire se mesurent par des observations humaines distinctes.

## 10. Contraintes d’implémentation à préserver

Extraire progressivement des écrans et états dédiés ; conserver `Board`, `MoveBudget` et les modèles de transaction indépendants de la présentation. Le plateau et la maison peuvent rester des vues graphiques spécialisées ; réécrire tout le moteur pour changer les menus n’est pas nécessaire.

La maison suivante doit distinguer identifiant d’objet, exemplaire, pièce, couche et orientation. Migrer la maison existante dans la première pièce, conserver chaque possession et achat, et garder l’édition réversible jusqu’à validation. Les achats restent atomiques et durables.

Pour le puzzle, conserver les résolveurs historiques et les parties actives ; la nouvelle règle prend un identifiant de contenu distinct. Les solveurs, budgets et certificats doivent représenter exactement les actions proposées au joueur.

La maquette jointe au chat illustre une structure et des interactions locales. Elle ne certifie ni une nouvelle direction artistique finalisée, ni les règles d’un plateau, ni un contenu déjà disponible dans l’APK. La version testée par le joueur reste la 1.14 RC2.
