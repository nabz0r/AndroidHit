> Archive de recherche antérieure au développement. La version livrée est décrite dans [le README actuel](../README.md) et [les notes de version](RELEASE.md).

# Étape 3 — Le jeu recommandé

**Choix : un puzzle 2D d’extraction, avec une petite règle de portes basculantes.** Nom de travail : **Signal Sortie**, sans vérification de disponibilité. Ce concept est proposé pour un test ; ni sa nouveauté mondiale ni son potentiel commercial ne sont établis.

## Pourquoi ce choix

Évaluation de conception, non statistique. Les délais sont des ordres de grandeur pour une personne déjà à l’aise avec le développement, aidée par l’IA, à 25–35 heures par semaine.

| Option | Atout | Travail caché | Premier candidat de test estimé | Décision |
|---|---|---|---|---|
| Extraction avec règle propre | Formes simples, état discret, démonstration courte | Génération, difficulté, répétition | 3–4 semaines | **Premier choix** |
| Placement de blocs original | Peu d’assets, rejouabilité | Distribution des pièces et différenciation difficiles | 2–4 semaines | Second choix |
| Tri d’objets / bus | But visuel évident | Files, capacités, nombreux cas de blocage et niveaux | 4–6 semaines | Garder en réserve |
| Rotation d’anneaux 3D / vis 3D | Transformations visuelles | Caméra, occlusion, modèles et interactions | 5–8 semaines ou plus | Écarter pour cette première sortie |
| Merge physique | Moments surprenants partageables | Physique et comportement sur appareils variés | 4–6 semaines | Risque de réglage supérieur |
| Runner | Compréhension immédiate | Animations, obstacles, renouvellement des décors | 6–10 semaines ou plus | Trop de contenu |
| Jeu social / combat en ligne | Dimension sociale | Réseau, serveurs, modération, triche, exploitation | Hors objectif | Écarter |

Ces familles ont été repérées dans les [sources marché](01-marche.md). Les durées et le choix relèvent de notre jugement de production. Les références [Arrows](https://play.google.com/store/apps/details?id=com.ecffri.arrows) et [Block Blast!](https://play.google.com/store/apps/details?id=com.block.juggle) montrent des boucles simples à comprendre ; leur succès ne prédit pas celui de ce projet.

## Règles du prototype

1. Un plateau de 5 × 5, puis 6 × 6 cases, contient des jetons portant une flèche dans une des quatre directions.
2. Toucher un jeton le fait sortir en ligne droite si aucun autre jeton ni obstacle fixe ne bloque son trajet. Un coup impossible laisse le plateau inchangé.
3. Quelques sorties de bord possèdent une porte ouverte ou fermée. Un jeton ne traverse une porte que si elle est ouverte.
4. Certains jetons portent un symbole d’interrupteur. **Après leur sortie**, ils inversent l’état de la porte portant le même symbole. La prévisualisation indique ce changement avant le coup.
5. On gagne quand tous les jetons sont sortis. L’ordre compte : une sortie possible immédiatement peut fermer une porte utile plus tard.
6. Annuler et recommencer sont gratuits et disponibles. Le jeu ne rend pas un niveau impossible pour vendre une solution.

Les symboles, les directions et les états ouverts/fermés restent lisibles sans distinguer les couleurs. Introduire une seule porte après les premiers niveaux ; deux portes seulement si les tests montrent que la règle reste compréhensible. Aucun chronomètre obligatoire au départ.

**Promesse à tester : « Fais-les sortir dans le bon ordre. Un départ change la suite. »** Une vidéo de 6 à 12 secondes doit montrer une porte qui change, un mauvais ordre possible, puis une résolution. Il s’agit d’une hypothèse de présentation ; les essais doivent mesurer si les joueurs la comprennent.

## Version minimale

| Inclure | Reporter |
|---|---|
| Portrait, un doigt, jeu hors ligne | Compte utilisateur, connexion obligatoire |
| 25–30 niveaux d’apprentissage conçus à la main | Histoire et personnages animés |
| Environ 100–150 niveaux sélectionnés si la boucle tient | Promesse de niveaux infinis |
| Sauvegarde locale, reprise, son et vibration réglables | Classement mondial et services serveur |
| Annuler, recommencer, indice lisible | Boutique de monnaies et économie complexe |
| Français et anglais au test ; autres langues selon résultats | Traduction automatique non relue dans des dizaines de langues |
| Défi quotidien local et partage d’un code si utile | Synchronisation, saisons et événements permanents |

Le défi quotidien hors ligne repose sur une graine et une version de règles identiques. Il n’offre ni anti-triche ni classement mondial authentifié. Le code partagé doit reproduire le plateau ; un simple bouton de partage ne suffit pas à démontrer une acquisition virale.

## Niveaux : résoudre avant de publier

Préférer un petit ensemble de niveaux intéressants à une quantité produite automatiquement.

- Limiter le premier solveur à environ seize jetons et deux portes, afin de contenir l’espace de recherche. Cette limite doit être mesurée sur l’implémentation réelle.
- Représenter les jetons présents et l’état des portes ; explorer les sorties légales pour trouver une solution complète.
- Rejeter tout candidat sans solution prouvée, toute recherche interrompue et les doublons. Conserver une solution témoin et une version du générateur.
- Classer les candidats par dépendances entre coups et par erreurs possibles ; jouer humainement chaque lot retenu. Une solution trouvée ne garantit ni plaisir ni difficulté appropriée.
- Générer et valider les packs en dehors du téléphone au début. Éviter un solveur coûteux en arrière-plan pendant la partie.

Tests utiles à la future implémentation : légalité des sorties, basculement des portes après le coup, annulation exacte, sauvegarde/reprise et rejouabilité de chaque solution témoin. Ce sont des vérifications à réaliser ; elles ne sont pas déjà passées.

## Rôle de Sol Ultra et du développeur

L’assistance IA demandée peut servir à découper les tâches, implémenter le moteur de règles, proposer des générateurs, écrire des vérifications, préparer des variantes de textes et analyser des retours anonymisés. Le développeur joue, teste sur téléphone, sélectionne les niveaux, vérifie les contenus et tranche les choix de produit.

Le plan n’exige **aucun appel à un modèle pendant une partie**. Il n’ajoute donc pas de coût d’inférence par joueur dans ce périmètre. L’abonnement ou l’outil IA existant reste un coût à compter séparément. Aucun gain de productivité chiffré ni aucune capacité particulière à garantir un hit n’est attribué au modèle.

## Choix technique proposé

Godot avec logique séparée du rendu, graphiques géométriques et stockage local. Le moteur autorise l’usage commercial sous MIT avec notices. [Licence](https://godotengine.org/license/), [export Android](https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_android.html).

Avant de figer une version de moteur ou un plugin publicitaire, réaliser un export Android de preuve sur le matériel existant et vérifier les exigences Play actuelles. Le dossier ne prétend pas que cette chaîne a déjà été installée, compilée ou testée. Voir aussi la [licence AGPL préexistante](02-droits-licences.md).
