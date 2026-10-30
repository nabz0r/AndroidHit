# Audit de lisibilité et de parcours — design natif 1.22

Lecture du code et des règles au commit `557724f56e45aa8df1cfab629f20848e0f3c4fd0`.
Cet audit ne modifie ni le jeu ni le site. Les observations visuelles et les
gestes sur émulateur sont dans le lot séparé de l'audit ; ce document ne prétend
pas les avoir réalisés. La qualité perçue des sprites et l'émotion restent à
juger sur les images et en jouant. Aucune conclusion de compréhension humaine
ne vient d'une réussite de solveur.

La complexité utile est l'ordre de sortie : dégager un trajet, libérer une clé,
garder une porte disponible pour la suite. Les problèmes vérifiables viennent
du langage nécessaire pour lire cet ordre, de l'enseignement de sa première
conséquence dangereuse et des détours pour utiliser ou gérer les compagnons.
Une nouvelle couche de pouvoirs ajouterait du travail avant de résoudre cela.

## Ce que contient réellement le début du Voyage

| Niveaux | Ajout dans les plateaux U | Autres règles présentes |
| --- | --- | --- |
| 1–4 | Grande flèche : faire sortir la pièce | Équipe, Charge, Résonance et Focus sont déjà construits pour les tentatives U ; le premier plateau reçoit un indice natif |
| 5–8 | Pièces qui bloquent le trajet | Réserve de sorties +3, chronomètre informatif, retours et pouvoirs |
| 9–10 | Une clé et son cadenas | Le même geste fait sortir une pièce et libère son cadenas |
| 11–12 | Même leçon « clé » | Trois éléments arrivent ensemble : glace 2 taps, feu 2 taps, eau 1 tap. Le nouvel essai commence aussi à coûter 1 énergie |
| 13–16 | Un relais change une porte en sortant | Les coquilles persistent ; une petite flèche désigne une autre règle que la grande flèche |
| 17–20 | Deux relais, leçon « ordre » | Dans ces contenus, les sorties physiquement autorisées ne détruisent pas encore la solution |
| 21–24 | Relais + clé | Même préparation élémentaire et mêmes systèmes d'aide |
| 25–26 | « Maîtrise », 9 puis 10 pièces | Toujours aucun choix légal qui supprime toute continuation |
| 27 | Premier choix légal dangereux, sur seulement 5 pièces | Leçon toujours « maîtrise » ; première invite explicite à maintenir une pièce seulement à U29 |
| 43 / 48 | Nouveaux agencements des règles existantes | Pas une nouvelle règle élémentaire : le nouveau run du labo y concentre encore ses épuisements |
| 61 | Premier relais agissant sur plusieurs portes | Leçon toujours `relay_order`, pas une introduction distincte |

Sources : `engine/RelayLayouts.java:7–68`, `RelayCatalog.java:19–22`,
`ElementalShell121.java:32–39,125–136`, `LumaView.java:3232–3234`,
`SessionEnergyStore.java:33`, `LumaView.java:2985–2989`.

La [sonde statique](audit_catalog.py) rejoue les **1 000 certificats** et
énumère tous les états atteignables des **100 premiers plateaux** dans un
modèle indépendant de l'obstruction/XOR. Elle confirme U27 comme premier
plateau ayant une sortie légale sans continuation, U61 comme premier relais
multiple et une marge de correction **+3 aux 1 000 niveaux**.
[Résultat détaillé](catalog-audit.json). Ce sont des vérifications du catalogue,
pas 1 000 parties d'agents, ni une exécution Java/native supplémentaire.

## Cinq problèmes à traiter, par priorité

### 1. L'action « compagnon » change de tâche et de sens selon son point d'entrée

**Preuve.** Sur le plateau, toucher un membre ouvre `battle_skill`, une page
entière avec portrait, textes, Charge, bouton Activer et bouton Retour au puzzle
(`LumaView.java:1583–1621`). Toucher une place vide ouvre plutôt `squad`, un
éditeur de composition dont les changements ne s'appliquent qu'à la prochaine
tentative (`1648–1681`). Sa navigation Collection quitte cette origine avec
`goRoot`, qui vide l'historique (`898–906,365`).

La collection des compagnons mène encore à une autre fiche. Cette fiche décrit
le talent **d'expédition**, les copies et la galerie, mais n'affiche pas la
définition du talent de puzzle (`2649–2654`). « Équiper » y change l'apparence
globale par `equipCharacter`, pas l'équipe du plateau ; l'action envoie ensuite
vers la garde-robe (`2660–2666,2174–2176`). Le premier membre/capitaine, le
compagnon d'apparence, le compagnon d'expédition et l'acteur du talent sont des
identités distinctes en code. Leurs écrans les présentent pourtant sous les
mêmes portraits et un vocabulaire voisin.

**Conséquence vérifiable.** Chercher une aide immédiate peut ouvrir la gestion
d'un essai futur. Chercher « le talent de ce compagnon » dans la collection
montre celui d'un autre mode. Cela ne prouve pas que le Board est perdu : il
reste en mémoire et sauvegardé. Le contexte de navigation et la tâche sont
perdus plus facilement que la partie elle-même.

**Simplification prioritaire.** Le plateau garde ses trois talents actuels.
Toucher un portrait ouvre une feuille courte au-dessus du plateau : effet
concret, coût, état, Activer/Fermer. Une place vide reste une place vide avec
« Équipe modifiable avant le prochain essai », sans grand détour pendant la
résolution. La gestion hors partie réunit, pour le compagnon choisi, **Talent
de puzzle / Apparence / Évolution**. Le talent d'expédition apparaît dans le
contexte Expédition. Le choix d'un membre pour le prochain essai ne doit pas
être exprimé par le même verbe que le port d'une tenue.

### 2. Le premier choix dangereux arrive après un long apprentissage où toute sortie légale convient

**Preuve.** L'énumération U1–26 ne trouve aucune sortie légale qui supprime la
continuation. À U27, la pièce **ligne 5, colonne 3, flèche vers le bas** (id 2)
peut sortir immédiatement. Son relais ferme la porte du bas. Deux autres
pièces vers le bas restent sur la ligne 5 ; elles ne peuvent plus sortir. La
pièce L5C4 vers la gauche, qui pourrait rouvrir cette porte, est bloquée derrière
elles. Ce choix légal impose un retour. Le catalogue réduit pourtant la taille
de 10 pièces à U26 à 5 à U27 et conserve `relay_mastery` pour les deux.

L'aperçu existe, mais sur U un tap normal exécute immédiatement l'action
(`LumaView.java:2894–2909`). Il faut un maintien de **480 ms** pour l'ouvrir
(`3498`). La seule invitation persistante au maintien concerne U29, U31, U43
et U48 (`2986`) : elle vient après le premier piège. Maintenir une pièce encore
recouverte explique d'abord sa coquille et n'affiche pas les conséquences du
relais (`2905`). Le premier double relais de U61 conserve également la leçon
ordinaire « deux relais ».

**Simplification prioritaire.** Enseigner l'effet dangereux sur un petit
plateau dédié avant de le combiner avec une clé : une sortie ouvre, une autre
ferme, la pièce affectée doit être visible. L'aperçu doit montrer les pièces
qui gagnent **et qui perdent** leur sortie ; l'aperçu actuel compte seulement
les nouvelles pièces mobiles (`2924`). Introduire le même geste d'inspection à
ce moment, puis proposer un vrai réemploi sans indication. Garder ensuite les
pièges d'ordre : c'est la profondeur du puzzle. Ne pas les confondre avec le
nombre de pièces ni ajouter un long texte à U43.

### 3. Le décor élémentaire impose trois lectures et cinq gestes sans choix stratégique associé

**Preuve.** Chaque nouvel essai U11+ reçoit trois coquilles par tri de hash des
IDs, sans rapport avec leur fonction pédagogique : glace 2, feu 2, eau 1
(`ElementalShell121.java:125–136`). Un tap enlève exactement une couche
(`55–60`). Aucune propagation, interaction eau/feu, limite de préparation ou
choix de ressource n'existe. Ces taps sont gratuits, persistent à travers Undo
et les talents de sortie les traversent (`11–13`, `LumaView.java:2896,3093,
1637`). Même une pièce physiquement bloquée reçoit d'abord ses taps de
préparation, avant qu'un essai révèle la cause réelle (`2896–2898`).

À U11, une résolution manuelle sans correction demande donc **6 sorties +5
taps de coquille**. À U13, elle demande **4 sorties +5 taps**, alors que le
nouveau raisonnement doit porter sur la première porte. Ces ratios sont un
compte de gestes, pas une mesure d'ennui. La DA peut faire attendre des éléments
des comportements que le moteur ne possède pas.

**Simplification prioritaire.** Retirer ces préparations du Voyage principal,
ou conserver un seul état visuel de préparation avec une seule interaction si
le geste a une valeur expressive observée. Réserver les éléments distincts à
un mode qui leur donne des règles et des décisions propres, après certification
de ses plateaux. Conserver flèches, obstruction, clés et relais suffit à garder
un puzzle d'ordre. Changer seulement les sprites ne réduit pas les cinq taps.

### 4. Les corrections sont décrites comme libres, puis comptées et monétisées dans le même essai

**Preuve.** Les descriptions de relais disent « Annuler est gratuit », « annule
autant de sorties que nécessaire » ou « chaque sortie peut être annulée
gratuitement » (`LearningStrings.java:20,22,26`). `drawHelp` affiche ces textes
pour la politique actuelle, puis un second paragraphe explique que le coup
consommé n'est pas rendu (`LumaView.java:3426–3432`,
`V120ResultStrings.java:14`). Le troisième Undo coûte pourtant des Coins
(`EconomyCurve121.java:10,37–43` ; `LumaView.java:3127–3144`).

La marge productive est +3 partout : un retour ne consomme pas lui-même un
coup, mais la sortie annulée reste comptée ; ressortir la pièce consomme donc
une correction. À l'épuisement, le panneau politique6 affiche surtout réserve,
reprises pub/Gold si disponibles, recommencer/énergie et sortir
(`3312–3324`). Le panneau d'impasse propose les mêmes règles économiques
générales sans expliquer quelle sortie antérieure a créé l'impasse
(`2887–2892`).

**Simplification prioritaire.** Unifier les textes visibles et l'économie de
l'apprentissage. Dans l'introduction d'une règle, permettre la correction de
sa première mauvaise conséquence sans coût de Coin ni nouvelle énergie ;
conserver les limites/étoiles comme objectifs de maîtrise facultatifs. Dans les
plateaux plus avancés, dire ce qui reste réellement : retours gratuits,
corrections restantes et effet d'un retour, sans trois formulations contradictoires.
Le retour d'échec doit relier la dernière fermeture à une pièce encore en
attente et permettre de revoir ce choix. Ajouter une cause à un refus physique
ne suffit pas à expliquer une sortie légale faite trop tôt.

### 5. Des systèmes d'aide voisins occupent une place de règle principale dès le début

**Preuve.** À chaque nouvelle tentative U, le jeu construit l'équipe, la Charge,
la chaîne de sorties et le Focus (`LumaView.java:3232–3234`). Le plateau affiche
trois portraits/talents et Résonance, puis un autre rail de Focus, l'objectif,
le progrès, le budget et le temps, avant le plateau (`2933–2955`). Il affiche
ensuite Undo, recommencer et les pouvoirs/leur stock (`2991–3007`). Le layout
réserve à la composition un rail d'au moins **144 unités** plus celui du Focus
(`CompanionBattleLayout.java:5,25`, `LumaView.java:404`). Ces unités sont des
coordonnées logiques, pas des pixels physiques mesurés ici.

Nova/Guide révèle une prochaine sortie sûre ; le Focus gratuit révèle aussi
une prochaine sortie sûre ; l'objet Focus de stock et la Vision ont encore des
conditions et garanties différentes (`GAMEPLAY_1_19.md`,
`Arcade121Strings.java:38–40`, `Strings.java:413`, `V119Strings.java:47–49`).
La grande flèche exprime une direction de mouvement, les petites flèches
expriment des portes modifiées. Un chevauchement géométrique précis existe
pour une clé-relais : le premier badge de relais est centré en
`x+size*.32,y-size*.31`, et la silhouette de clé est dessinée autour de
`x+cell*.26,y-cell*.28` (`LumaView.java:3048–3049`,
`KeyLinkScene122.java:37–42`). Puisque `size=cell-8`, les deux marques utilisent
la même région supérieure droite. Exemple confirmé : **U29, id4 / L2C5↑**, clé
du cadenas L4C2 et relais de droite. Le numéro de clé est à gauche ; c'est sa
silhouette à droite qui croise le badge. La gêne exacte sur l'image doit être
inspectée sur le rendu ; le taux de réussite du moteur ne la teste pas.

**Simplification prioritaire.** Donner la priorité au plateau et à un seul
contrôle d'aide contextualisé. Les portraits peuvent réagir aux sorties sans
requérir immédiatement une lecture de trois systèmes d'aide. Montrer le
talent disponible au besoin, expliquer sa conséquence sur le plateau, puis
introduire composition/Charge et Résonance dans un objectif ultérieur. Mettre
temps, grades et stocks dans les détails tant qu'ils ne changent pas la
décision actuelle. Rendre le badge de relais distinct de la flèche de mouvement
et séparer son emplacement de la clé : une silhouette différente doit encoder
une fonction différente, pas seulement une couleur.

## Circuit proposé, avec retour exact

Le circuit minimal de résolution est **plateau → feuille talent → plateau**.
La feuille garde en arrière-plan le même Board, la même caméra/position de
défilement, la sélection et la destination de retour ; fermer ne dépense rien.
Activer conserve uniquement l'effet annoncé et reprend ce même contexte.

La gestion devient **carte/accueil → compagnon → équipe du prochain essai ou
apparence → carte/accueil**, avec un seul bouton « Reprendre Uxx » tant qu'une
partie est en cours. Le joueur doit pouvoir aller voir sa tenue en pause, puis
reprendre exactement son essai. Le routage doit porter ce contexte explicitement,
au lieu de compter uniquement sur un historique global effacé par `goRoot`.

État actuel à conserver : les edits d'équipe utilisent déjà un brouillon
`squadDraft`, puis un bouton d'enregistrement explicite ; une simple visite ne
remplace pas l'équipe active. Le Board, son historique et ses compteurs restent
sauvegardés. En revanche, `go()` remet systématiquement `contentScroll=0`,
annule sélection/aperçu et `showAppliedLook()` dirige l'application d'un
accessoire vers `wardrobe` (`352–361,2174–2176`). Le retour au contenu précis
n'est donc pas le contrat actuel.

La garde-robe native propose une fiche d'aperçu pour **un objet**, puis
« Équiper » qui applique immédiatement. Son bouton costume original applique
aussi immédiatement (`2128–2153,2624–2629,2660–2666`). Le prototype
`docs/design/v1.19/collection-prototype.html:20–29` contient déjà le contrat
plus clair **Porté / En essai / Rétablir / Appliquer**, avec brouillon multiobjet,
liste défilante et position de collection retenue. Ce contrat reste une
**maquette navigateur**, sans sauvegarde Android. Il peut guider le flux natif ;
il ne faut pas annoncer qu'une session d'essai réversible est déjà livrée.

## Ce que le labo permet et ne permet pas de conclure

Le labo courant utilise bien `Board`, policy6, les coquilles et l'exécuteur
des talents. Mais la perception reçoit directement les coordonnées, directions,
masques de relais, liens de clés et nombre de couches :
`scripts/cognitive_lab/Environment.java:19–31`. Elle ne doit pas identifier
la petite flèche dans un sprite, distinguer un élément décoratif d'une règle,
lire une phrase française, trouver un bouton de retour ou naviguer entre
équipe, tenue et expédition.

`Environment122.java:23–36` fournit en outre des familles de talent et des
disponibilités déjà structurées. Pour la cause affichée, `Agent122.java:35–37`
met temporairement la connaissance de la règle à **1** : reconnaissance
certaine déclarée, non mesurée. Les inspections natives clé/cadenas et la
prévision des portes/pièces ne sont pas simulées. Le périmètre les exclut
explicitement (`CognitiveLab122.java:70`). Ainsi un test logique de la cause ne
peut ni valider ni invalider sa mise en scène visuelle.

Le [nouveau run indépendant du lot labo](../simulation/analysis.json) (seed
`2026103001`) garde **744/1 150 tentatives étiquetées `budget_exhausted`** du
témoin à U43/U48, soit **64,7 %**, malgré seulement les mêmes règles de base.
L'explication immédiate passe à 1 144 étiquettes, pas à la disparition du
problème. Les premiers essais des mêmes arrivées communes donnent U43
334/901 contre 326/901 ; U48 205/523 contre 206/523. Le lot labo conserve
séparément une limite d'étiquetage : 52 tentatives tous scénarios à l'horizon
ont déjà une réserve nulle (10 témoin, 11 immédiat). Leur sensibilité ne doit
pas être confondue avec les étiquettes brutes ci-dessus. Les preuves complètes
et appariements appartiennent au lot labo, pas au présent replay statique. Les sorties dites
`manualExits` incluent celles suivies après un indice ; elles ne signifient pas
« résolu sans aide ». Une réussite avec Flow mesure aussi l'automatisation du
puzzle. Aucun de ces nombres ne mesure le plaisir ou la simplicité ressentie.

## Ordre de validation de la simplification

1. **Même Board, interface simplifiée.** Comparer l'actuel et un plateau
   débarrassé des détours/rails permanents, sans changer la géométrie ni les
   solutions. Observer recherche d'aide, anticipation d'un relais et retour
   exact. C'est le test qui peut isoler l'effet d'interface du niveau.
2. **Même règle, apprentissage mieux séquencé.** Utiliser U27, U43, U48 et leurs
   prérequis ; vérifier qu'une personne peut prédire une fermeture et désigner
   la pièce qui en dépend, puis résoudre un autre agencement sans indice.
   Mesurer séparément correction, dépense et solution autonome.
3. **Même difficulté logique, moins de préparation.** Comparer les mêmes
   plateaux avec/sans les cinq taps élémentaires ; séparer nombre de gestes,
   erreurs de règle, besoin d'aide et appréciation. Une baisse du nombre de
   taps ne démontre pas à elle seule un meilleur plaisir.

Les nouveaux plateaux de cette proposition ne doivent pas être construits en
se contentant d'une note de difficulté : vérifier toutes les sorties légales,
les pièges, les prérequis et des certificats de résolution. Les milliers de
parties synthétiques complètent ces vérifications du moteur ; la lisibilité de
la DA, l'anticipation et l'émotion exigent l'observation de vraies personnes.
