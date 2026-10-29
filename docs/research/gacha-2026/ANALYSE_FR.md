# Luma Drift — rareté et évolution des personnages par copies

Analyse et proposition du 29 octobre 2026, Europe/Luxembourg.
Base inspectée : `962fc2d69423a5e09c411a2594a8325fe6c20e04`.
Statut : étude de conception ; aucun changement du jeu, des probabilités ou des sauvegardes.

Le [concept et l'économie de l'aventure de collection](CONCEPT_ET_ECONOMIE_FR.md) constituent le cadrage principal. La courbe au-delà du niveau 3 présentée ici reste une illustration, à revoir après choix des effets, des délais et des flux de copies.

## 1. Modèle demandé, après clarification

La rareté et le niveau sont deux informations indépendantes :

- **N / R / SR / SSR** : rareté fixe du personnage, apparence propre et compétences propres.
- **Niveau 1 à 10** : évolution obtenue en accumulant des copies du même personnage.
- Aucun système d'XP de personnage ni axe supplémentaire d'éveil n'est ajouté à ce modèle.
- Un personnage SR niveau 10 reste SR. Un SSR niveau 1 est déjà SSR.
- Les copies ne doivent pas remplacer le personnage ni effacer une compétence acquise.

Les exemples fournis sont interprétés comme des **seuils cumulés** :
Lv1 = 1 copie au total ; Lv2 = 2 ; Lv3 = 8.
Cette interprétation est une hypothèse explicite : si les nombres désignent des copies à dépenser à chaque étape, les coûts cumulés seraient différents.

Le taux de 0,005 % est l'exemple donné pour le haut de rareté. Il n'est ni un taux attribué à Crash Fever ni une décision déjà appliquée à Luma Drift.

## 2. Comment définir les raretés

N, R, SR, SSR sont des catégories définies par le jeu ; il n'existe pas de norme universelle leur attribuant une puissance ou une probabilité.

| Axe | Ce qui le définit | Ce que voit le joueur |
| --- | --- | --- |
| Rareté | Catalogue : identité visuelle, compétence, possibilités de spécialisation | N, R, SR ou SSR |
| Niveau | Total de copies compatibles acquises et seuils publiés | Niveau 1 à 10, par exemple « 8 / 14 copies » |
| Efficacité | Résultat réel selon le mode, le rôle et les autres effets équipés | Description des effets et contexte d'usage |

Une rareté élevée doit avoir une identité visuelle et mécanique identifiable. La seule couleur d'un contour ne crée pas une compétence ni une différence de jeu.
Le niveau renforce l'effet prévu pour ce personnage ; il ne change pas son étiquette de rareté.

Un N niveau 10 peut être meilleur qu'un SSR niveau 1 dans une spécialité. Cela doit venir des compétences et de leurs valeurs explicites, pas d'une règle cachée selon laquelle toutes les statistiques du SSR seraient supérieures.
Pour départager deux personnages, comparer au même niveau, avec le même équipement et dans le même mode, puis vérifier les usages spécialisés et les synergies.

## 3. Ce que Crash Fever apporte à l'analyse

La version japonaise documente une rareté officielle ★1 à ★6, avec des éveils possibles indiqués par des étoiles grises. Les coûts de composition constituent une autre distinction. Ce n'est pas exactement une échelle native N/R/SR/SSR. [Étoiles](https://crashfever.com/faq/2021043026176/) · [Coût](https://crashfever.com/faq/2020102923986/).

Dans Crash Fever, les copies compatibles font monter le Bug ; des matériaux universels peuvent aussi le faire. Le Bug peut débloquer des capacités et des bonus de récompense. Les acquisitions donnent également du Deep-Log utilisable pour le potentiel. Leçons pour notre modèle : un doublon doit produire un progrès visible, une source universelle peut compenser la rareté, et la progression a besoin d'un plafond défini. Nous retenons ces principes dans notre unique niveau par copies. [Copies](https://crashfever.com/faq/2024100425847/) · [Bug](https://crashfever.com/faq/2024100427225/) · [Deep-Log](https://crashfever.com/faq/2024100327345/) · [Guide officiel](https://crashfever.com/syosinnsyagaido/).

La lettre du producteur du 25 septembre 2026 annonce des changements à partir du 1er octobre : chances de vedette de 1 % ou 1,4 % selon les bannières, échanges à 150/200/300 pièces, retrait de certains anciens paliers garantis et révision de la distribution mensuelle de monnaie. Elle montre qu'il faut comparer ensemble les taux, les plafonds et le nombre de tirages accessibles. Le texte ne chiffre pas la nouvelle distribution mensuelle ; il ne permet pas de conclure à une meilleure accessibilité gratuite globale. [Lettre officielle](https://crashfever.com/campaign/2026092545575/).

Les bannières peuvent donner des pièces d'échange distinctes et datées. Une garantie doit donc préciser sa cible et la conservation de sa progression. [Exemple d'octobre 2026](https://crashfever.com/campaign/2026100545523/?mode=app).

Comme comparaison, Genshin utilise les copies 2 à 7 pour les constellations et prévoit une compensation après le maximum. Son niveau de personnage est un autre système : nous ne le transposons pas. L'intérêt du parallèle est le traitement explicite du surplus et du nombre fini de copies utiles. [FAQ officielle doublons](https://support.hoyoverse.com/hc/en-us/articles/52089402385945-What-rewards-do-I-get-for-obtaining-a-duplicate-character-near-or-at-C6).

Sa garantie distingue également rareté et personnage vedette, avec report entre bannières compatibles. Ce sont deux promesses différentes à afficher séparément. [FAQ officielle garantie](https://support.hoyoverse.com/hc/en-us/articles/50333940684953-How-does-the-Wish-guarantee-system-work).

Ces sources établissent des mécanismes, pas leur effet causal sur la rétention, le plaisir ou les recettes de Luma Drift.

## 4. Ce qui existe réellement dans Luma Drift

| Élément | État du code inspecté |
| --- | --- |
| Compagnons | 24 identités, dont Nova gratuite ; 23 dans la bannière |
| Raretés actuelles | Common, Rare, Epic ; aucune catégorie SSR |
| Tirage compagnon | 180 LumaCoins gagnés en jeu |
| Distribution compagnon | 10 communs à 6 % chacun ; 8 rares à 3,75 % ; 5 épiques à 2 % |
| Doublon compagnon | Rembourse 45 / 75 / 120 pièces ; aucun stack conservé pour évoluer |
| Acquisition ciblée | Achat direct de chaque compagnon, 160 à 1 100 pièces |
| Achat de copies | Impossible actuellement : le magasin refuse un personnage déjà possédé |
| Niveaux de compagnon | Aucun ; propriété enregistrée comme acquise ou non |
| Compétences des compagnons | L'identité du compagnon est actuellement cosmétique |
| Équipement fonctionnel distinct | Certains objets augmentent les gains de première victoire ou réduisent le coût des packs de pouvoirs |
| Garantie des bannières jouables | Aucune |
| Autres tirages | Tenues complètes à 260 pièces et ancienne capsule de vêtements à 150 pièces |
| Paiements | Tirages premium de vêtements préparés côté serveur, désactivés ; ne pas les confondre avec les arrivées jouables |

Sources : [SummonCatalog](../../../app/src/main/java/com/androidhit/lumadrift/data/SummonCatalog.java), [Catalog](../../../app/src/main/java/com/androidhit/lumadrift/data/Catalog.java), [GameStore](../../../app/src/main/java/com/androidhit/lumadrift/data/GameStore.java), [AvatarGearCatalog](../../../app/src/main/java/com/androidhit/lumadrift/data/AvatarGearCatalog.java).

Le rang du joueur déjà affiché vient d'une XP globale. Il ne doit pas devenir silencieusement un niveau de compagnon.

Exemple : Inky coûte 1 100 pièces en achat direct, contre 50 tirages en moyenne à son taux individuel actuel de 2 %, soit 9 000 pièces de dépenses brutes moyennes. L'achat direct est une vraie alternative actuelle. Une refonte doit préciser ce qu'il devient et préserver tous les personnages déjà acquis.

## 5. Ce que représente exactement 0,005 %

Hypothèses : tirages indépendants, taux constant p = 0,00005, aucune garantie ni acquisition alternative.
Probabilité d'avoir obtenu au moins un résultat visé après n tirages : 1 − (1 − p)^n.

| Tirages | Chance d'avoir obtenu au moins un résultat visé |
| ---: | ---: |
| 10 | 0,0500 % |
| 100 | 0,4988 % |
| 1 000 | 4,8772 % |
| 10 000 | 39,3477 % |
| 20 000 | 63,2130 % |
| 60 000 | 95,0217 % |

Une copie demande **20 000 tirages en moyenne**. Le seuil de 50 % est 13 863 tirages ; celui de 95 % est 59 914.
20 000 n'est donc pas une garantie : environ 36,8 % des joueurs n'ont encore aucun résultat visé à ce stade.

Si 0,005 % est le taux d'**un personnage précis** :

| Objectif | Copies totales | Tirages moyens depuis zéro |
| --- | ---: | ---: |
| Obtenir le personnage / Lv1 | 1 | 20 000 |
| Lv2 | 2 | 40 000 |
| Lv3 demandé | 8 | 160 000 |
| Lv10 de la courbe proposée | 92 | 1 840 000 |

Ces moyennes ne sont pas des plafonds. Si le personnage est déjà acquis, enlever une copie : Lv3 demande encore 140 000 tirages moyens.

Si 0,005 % est le taux de **toute la catégorie SSR**, ensuite partagée entre M personnages équiprobables, multiplier ces moyennes ciblées par M. Une garantie « un SSR » ne garantit donc pas le doublon de son SSR.

À titre de budget, les récompenses récurrentes actuelles atteignent en moyenne arithmétique 815 pièces par jour si toutes les victoires quotidiennes/hebdomadaires sont à trois étoiles, toutes les missions sont validées et les cadeaux sont réclamés. Ce n'est pas une observation de joueur.
Cela représente environ 136 tirages de 180 pièces sur 30 jours en consacrant tout ce revenu aux tirages, avant remboursements, sans autres achats. À 0,005 %, 136 tirages ne donnent qu'environ 0,68 % de chance d'au moins un résultat visé.

Conclusion de conception : un tel taux peut porter un événement exceptionnel ; une progression normale par copies exige une autre source ciblée ou une garantie.
L'exemple 0,005 % reste inchangé dans les calculs ; aucune hausse silencieuse du taux n'est proposée comme équivalente.

## 6. Courbe de niveau proposée pour démarrer

Valeurs de travail, à simuler ; seuls 1 / 2 / 8 viennent de l'utilisateur.

| Niveau | Copies cumulées requises | Copies supplémentaires depuis le niveau précédent |
| ---: | ---: | ---: |
| 1 | 1 | 1 |
| 2 | 2 | 1 |
| 3 | 8 | 6 |
| 4 | 14 | 6 |
| 5 | 22 | 8 |
| 6 | 32 | 10 |
| 7 | 44 | 12 |
| 8 | 58 | 14 |
| 9 | 74 | 16 |
| 10 | 92 | 18 |

Au-delà de Lv2, la formule proposée est L² − L + 2. Elle rend le coût total lisible et évite une croissance exponentielle.
Le passage Lv2 → Lv3 est déjà six fois plus coûteux en nouvelles copies que Lv1 → Lv2 ; il faut le montrer avant le premier tirage.

Exemples fictifs de futures fiches (Miko est actuellement Common ; aucun reclassement SR n'est décidé) :
- « Miko · SR · Niveau 2 · 5 / 8 copies totales · encore 3 pour le niveau 3 ».
- « Miko · SR · Niveau 3 · 8 / 14 copies totales · prochain effet : … ».
- Au maximum : « Niveau 10 · copies supplémentaires converties en … ».

Les copies sont conservées dans un total historique propre au personnage. Elles ne sont pas remises à zéro à chaque niveau.
Le niveau est déduit des seuils ; les effets sont calculés depuis ce niveau. Une variante avec confirmation d'évolution est possible mais doit enregistrer total et palier activé sans ambiguïté.
Une acquisition de plusieurs copies peut franchir plusieurs seuils ; le résultat doit montrer le vrai niveau final sans exiger des clics de rattrapage.

## 7. Rendre les copies rares accessibles

Trois mécanismes complémentaires à arbitrer, sans changer le sens de « niveau par stacks » :

1. **Tirage** : première copie ou +1 copie du personnage déjà possédé ; animation distincte et total mis à jour.
2. **Acquisition ciblée** : missions, contrat de collection ou échange permettant d'obtenir une copie du compagnon choisi, même déjà possédé.
3. **Garantie** : compteur public de tirages, avec cible exacte et règle de conservation explicite.

Un matériau universel éventuel doit être présenté comme une fabrication de copie/stack, avec son coût complet. Il n'ajoute pas un deuxième niveau ni une XP.
La première copie rare ne doit pas obligatoirement autoriser un multiplicateur infini de copies à bas prix : le coût, la cadence et les personnages éligibles doivent être définis.

Une récompense d'une copie **ciblée** par mois donne Lv3 après huit mois en partant de zéro ; Lv10 à 92 copies après 92 mois si cette source est seule. Un tirage mensuel de SSR aléatoire ne donne pas ce calendrier ciblé.
La récompense périodique ne résout donc pas seule la progression : le flux total de stacks doit être simulé.

Si une garantie donne le même résultat au plus tard au 100e tirage, avec remise à zéro sur succès, conserver 0,005 % de taux de base donne environ **1,0025 % de fréquence effective à long terme**. Il faut publier les deux valeurs ; on ne peut plus présenter 0,005 % comme fréquence totale des acquisitions.

Choix recommandé à ce stade : réserver le tirage très rare à la surprise, et financer la progression régulière de personnages déjà acquis par une voie ciblée mesurable. C'est une recommandation, pas une fonctionnalité existante ni un taux approuvé.

## 8. Apparence et compétences

Chaque personnage possède son apparence et sa compétence propres à sa rareté. Les niveaux peuvent amplifier une valeur, réduire un délai ou enrichir un effet déjà décrit.
La première copie doit donner accès à une compétence utile ; Lv3 ne doit pas être le premier moment où un SSR fonctionne réellement.

Pour Luma Drift, les compétences peuvent agir sur l'économie bornée, une aide de puzzle explicite ou une interaction de maison. Ces familles sont des propositions. Leur attribution aux personnages et leurs chiffres restent à concevoir.
La rareté seule ne justifie pas d'ajouter attaque, défense ou points de vie à un puzzle qui n'en utilise pas.

Avant intégration, définir pour chaque compétence :
- son effet exact à chacun des dix niveaux ;
- les modes concernés, sa cadence, son coût et ses limites ;
- les interactions avec les pouvoirs et équipements existants ;
- le moment où compagnon et niveau sont figés pour une partie ;
- l'effet sur récompenses, scores et défis partagés.

Un changement de compagnon pendant une récompense ne doit pas permettre de cumuler plusieurs bonus.
Une compétence modifiant les règles des puzzles exige ses propres contrats ; les certificats actuels ne prouvent pas automatiquement la validité d'un nouveau mode assisté.

## 9. Leçons à retenir

- **Désir** : apparence identifiable et compétence qui se comprend avant le tirage.
- **Premier succès** : première copie immédiatement utilisable.
- **Doublons** : chaque copie rapproche d'un seuil affiché.
- **Progression** : les stacks demandés doivent être comparés aux stacks réellement accessibles par semaine.
- **Malchance** : une garantie de catégorie et une copie du personnage choisi répondent à deux besoins différents.
- **Surplus** : au niveau 10, convertir les copies en une récompense connue, sans perte silencieuse.
- **Variété** : les niveaux améliorent un rôle ; les nouvelles raretés ne doivent pas rendre automatiquement toutes les précédentes inutiles.
- **Calendrier** : conserver le progrès obtenu et préciser les limites des périodes.
- **Mesure** : comparer temps vers Lv2/Lv3/Lv10, stocks, distribution entre personnages et intérêt de jouer avec eux. Les tirages simulés ne mesurent pas l'envie de revenir.

## 10. Plan d'implémentation ultérieur

1. Geler la matrice des personnages : rareté fixe, apparence, compétence et valeurs aux dix niveaux.
2. Simuler la courbe 1 / 2 / 8 / … / 92 avec l'économie existante, en séparant joueurs occasionnels, réguliers et progression de collection déjà avancée. Les rythmes de jeu utilisés doivent être déclarés comme hypothèses.
3. Définir la rareté de chaque identité existante et préserver son acquisition. Les anciens doublons ont déjà été remboursés : décider d'un éventuel cadeau de transition plutôt que les recompter implicitement.
4. Ajouter un registre par personnage : total de copies, version de progression et reçus d'acquisition/échange/évolution ; conserver les identifiants actuels.
5. Versionner les nouveaux tirages et conserver les lecteurs des anciens reçus `v116_1`. Le catalogue actuel valide aussi les remboursements historiques.
6. Passer à une précision adaptée : les probabilités actuelles sont en entiers sur 10 000. **0,005 % ne peut pas y être représenté exactement** ; un catalogue sur 1 000 000 permet 50 / 1 000 000. Conserver un échantillonnage sans biais et les anciennes résolutions.
7. Enregistrer dans la même transaction dépense, résultat, stacks, éventuelle garantie et reçu. L'animation révèle une transaction déjà enregistrée.
8. Afficher rareté, niveau, total, prochain seuil, compétence avant/après et chances exactes depuis le même catalogue.
9. Valider sauvegarde froide, rejeu d'une requête, interruption, débordement, dernier niveau, migration, six langues et écran compact. Puis construire et tester l'APK exact.
10. Toute future version payante exige son autorité serveur et sa qualification propres. Le registre local déterministe actuel reste un mécanisme de reprise, pas une loterie payante sécurisée.

L'analyse prépare cette implémentation. Elle ne change pas le jeu livré.

