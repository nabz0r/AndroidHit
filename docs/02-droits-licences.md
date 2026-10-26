> Archive de recherche antérieure au développement. La version livrée est décrite dans [le README actuel](../README.md) et [les notes de version](RELEASE.md).

# Étape 2 — Que faire sans acheter de licence de franchise ?

**Aucun titre commercial cité n’est déclaré libre à recopier.** L’objectif est de créer une réalisation indépendante d’une famille de jeux, sans utiliser la franchise d’un autre éditeur.

Le [ministère luxembourgeois de l’Économie](https://meco.gouvernement.lu/fr/domaines-activites/propriete-intellectuelle/droits-auteur-voisin.html) distingue les œuvres originales matérialisées des idées et méthodes en tant que telles. Cette distinction permet de réfléchir à une mécanique générique ; elle n’autorise pas la reprise du code, des images, de la musique, des textes ou d’autres expressions originales d’un concurrent. Une combinaison visuelle reconnaissable et des niveaux originaux peuvent également poser problème.

Les marques, dessins et modèles, brevets et règles de concurrence nécessitent aussi une attention. L’[OEB](https://www.epo.org/en/legal/guidelines-epc/2026/g_ii_3_5_2.html) distingue les règles de jeu en tant que telles des réalisations techniques susceptibles d’être examinées. Les territoires ne sont pas uniformes ; l’[OMPI](https://www.wipo.int/en/web/wipo-magazine/articles/video-games-and-ip-a-global-perspective-38773) décrit la pluralité des droits dans le jeu vidéo. Cette étude n’est pas une recherche mondiale de liberté d’exploitation.

## Matrice des titres repérés

« Envisageable » signifie : création indépendante de cette famille, sans reprendre le produit cité. Cela ne certifie pas l’absence de brevet, de marque ou d’autre droit sur un projet précis.

| Référence observée | Famille réutilisable comme point de départ | Position pour notre projet |
|---|---|---|
| Block Blast!, Color Block: Combo Blast | Placement de blocs et suppression de lignes | Envisageable ; identité, rendu, règles détaillées et progression propres |
| Arrow Puzzle, Arrows – Puzzle Escape | Extraction de pièces, obstacles et ordre des coups | Envisageable ; notre code, nos niveaux et une présentation indépendante |
| Bus Fever Party!, Magic Sort! | Tri, files, correspondance de couleurs, capacité limitée | Envisageable ; ne pas reprendre niveaux, interface ou habillage |
| Rotate Rings | Rotation, déverrouillage, logique spatiale | Envisageable en principe ; ressemblance globale et éventuels droits à examiner |
| Meowdoku | Logique de placement sur grille | Envisageable ; pas de reprise du nom, des chats, des puzzles ou du produit reconnaissable |
| Ludo King, Vita Mahjong | Règles traditionnelles du ludo ou du mahjong solitaire | Bonne base de règles ; habillage moderne, corpus de plateaux et marques exclus |
| Paper.io 2, Mob Control | Capture de territoire, croissance, contrôle de foule | Envisageable ; réalisation et identité indépendantes |
| Subway Surfers | Runner à obstacles | Genre envisageable ; univers, personnages et assets exclus |
| Royal Smash!, Brain Puzzle 3 | Physique ou énigmes | Familles possibles ; scènes, énigmes écrites et niveaux originaux à créer |
| Gossip Harbor | Fusion d’objets | Mécanique envisageable ; histoire, personnages et masse de contenus coûteux |
| Roblox | Plateforme de créations et expériences sociales | Ce produit ne peut pas être repris ; projet similaire hors budget solo rapide |
| Free Fire / MAX × NARUTO | Combat multijoueur | Aucun droit sur Free Fire ou NARUTO accordé ; trop coûteux dans ce périmètre |
| Football League 2026 | Football | Genre possible ; aucun droit supposé sur logos, clubs, joueurs ou compétitions |

Les descriptions et classements de ces références sont documentés dans le [marché](01-marche.md). La colonne de position est notre analyse, pas une information publiée par les éditeurs.

## Franchises à exclure

Ne pas utiliser Pokémon, Disney, MONOPOLY, des équipes de football, des célébrités, des musiques connues ou des personnages d’anime sans droits adaptés. Changer quelques couleurs ou produire une imitation avec une IA ne règle pas les droits. [Politique Google Play sur la propriété intellectuelle](https://support.google.com/googleplay/android-developer/answer/9888072?hl=en)

Ne pas appeler un jeu de blocs « Tetris » ni considérer son apparence comme générique : le titulaire revendique notamment le nom, les Tetriminos, le thème musical et la présentation distinctive. [Mentions du titulaire](https://www.tetris.com/news)

## Moteur, contenus et licence du dépôt

- **Godot :** usage commercial sans redevance du moteur, sous licence MIT ; conserver les notices requises, ainsi que celles des composants concernés. [Licence officielle](https://godotengine.org/license/)
- **Contenus :** formes, animations et sons simples créés pour le projet ; pour toute police, image ou bibliothèque externe, noter URL, auteur, licence, version et attribution. « Gratuit » ne veut pas dire libre pour un usage commercial.
- **IA :** ne pas lui demander de reproduire un écran, un logo ou des personnages d’un concurrent. Vérifier les contenus proposés et leur provenance ; l’assistance IA ne certifie pas les droits.
- **Dépôt :** le fichier [LICENSE](../LICENSE) préexistant contient l’AGPL version 3. Il est conservé, sans introduire de nouvelle licence ni d’exception. L’usage commercial reste possible sous les conditions applicables, dont les obligations de source lors de la distribution d’un programme couvert. [FAQ GNU](https://www.gnu.org/licenses/gpl-faq.en.html#GPLCommercially)

Avant d’intégrer des SDK publicitaires ou de paiement, examiner leur licence et leur mode de combinaison avec le code du jeu. La compatibilité avec l’AGPL n’est pas validée ici ; ce point peut modifier le choix d’intégration. Une éventuelle exception ou autre licence relève des titulaires de droits, pas d’une modification automatique du fichier LICENSE. [FAQ GNU sur les bibliothèques incompatibles](https://www.gnu.org/licenses/gpl-faq.en.html#GPLIncompatibleLibs)

## Vérifications avant la fiche Store

1. Chercher le nom et les signes proches sur Play, sur le web et dans les registres de marques des territoires visés, dont EUIPO/TMview pour l’Europe. **Signal Sortie est un nom de travail non vérifié.**
2. Comparer les écrans et les premières parties aux références : retirer les ressemblances distinctives évitables, conserver les traces de conception indépendante.
3. Tenir l’inventaire des dépendances et des droits, y compris la fiche Store, les captures et les sons.
4. Examiner les éventuels droits techniques pertinents si la mécanique précise ou les marchés choisis le nécessitent. Aucun feu vert juridique mondial n’est affirmé.
