# Qualification native 1.23 finale E — rc2 / code31

**Production E : PASS495 assertions,142 gestes réels,4 phases. Lab E : PASS27
assertions,4 gestes et7 fichiers de préférences production byte-identiques.**
Identités, sources et périmètres sont dans [candidate-e-acceptance.json](candidate-e-acceptance.json).
Aucun commit, publication ou acceptation humaine n'est établi par ce dossier.

- Production SHA256 : `429a443c9228787c7b854d36a40f0bcf7d52fdc8a3601214cd27b5cfae6945fb`.
- Lab SHA256 : `099d4dc7d38cb4552b3b1ad46f7e3ccb70b304c7c8a1c6c332e16ada00ba558c`.
- Signer conservé : `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`.

| Phase E réellement exécutée | Assertions / gestes | Preuve |
| --- | --- | --- |
| Upgrade D→E du profil conservé, deux swipes tactiles, tenue Try/Cancel/Apply/restauration, retour exact U29 |64 /17|[rapport](candidate-e-retained-context/report.json)|
| Première installation E réellement absente sur nouveau5634, préférences vides, introduction jouée, victoire U1, U2 et Nova FR/EN |72 /29|[rapport](candidate-e-genuine-fresh-fr-en/report.json)|
| Hoot sans historique : refus sans débit ; équipe restaurée ; U29 sans coques,8 médias ; Guide, Bundle, essai tenue et timer/effets |214 /78|[rapport](candidate-e-critical-fr-en/report.json)|
| Pan, zoom à deux doigts, minimap sans déplacement du compagnon, recentrage, destination verrouillée refusée, marche réelle |145 /18|[rapport](candidate-e-world-navigation-fr/report.json)|

L'observateur envoie les gestes aux coordonnées d'accessibilité réellement vues.
Il ne lit pas le plateau par une API privée et n'importe aucune classe du jeu.
Les sorties U1/U27/U29 éventuellement certifiées sont des gestes de test issus
certificats publics figés ; elles ne mesurent aucune compréhension d'un joueur.
La recréation Bundle utilise explicitement le SDK public `Activity.recreate()` :
Android appelle save/destroy/create, sans Bundle injecté ni méthode privée.
Elle est séparée des gestes tactiles. Les chronomètres sont vérifiés par de
vrais retours Home/foreground et les durées persistées.

## Médias originaux

[capture-map.json](capture-map.json) associe exactement8 PNG E1080×2400 non
retouchés au rapport PASS et à l'APK installé : puzzle/talents/tenue/carte ×FR/EN.
La revue visuelle, les octets et empreintes sont dans
[candidate-e-canonical-media.json](candidate-e-canonical-media.json).
Le CTA carte est visiblement vert et réellement actif. Les8 captures ont été
inspectées, ainsi que les captures de refus, Bundle, nouvelle partie et Lab.
Les originaux privés D restent conservés et distincts.

## Preuves D conservées, pont limité vers E

[candidate-d-private-qualification.json](candidate-d-private-qualification.json)
indexe8 phases D,506 assertions/182 gestes. Elles comprennent U27 inspection
annulée, U29 cinq sorties réelles clé/cadenas puis Undo, et640×960/font1.3 avec
réduction des animations FR/EN. Le retour depuis la tenue conserve les6 champs
core et les coordonnées du dock, donc son défilement observé.
[Revue compacte](candidate-d-short-font13-reduced/scroll-and-visual-review.json).

Les501 inputs E ont été re-hashés :499 identiques à D ; seuls la version et
un bloc de peinture `modernAction` diffèrent. Le
[diff figé](../candidate-e/source-delta-from-d.diff) ne change aucune action.
Les résultats compacts/clé/Undo sont donc des preuves **D reliées par ce pont**,
pas des exécutions E ré-étiquetées. B/C et les premières installations C/D restent
attribués à leurs vrais APK. L'[audit des observateurs](compiled-observer-source-binding.json)
vérifie12 archives acceptées D/E contre leurs véritables inputs compilés/APK.

## Lab et isolation

[Lab E FR/EN](../native-lab/candidate-e-launch-fr-en/report.json) exige le SHA
préexistant1.22 `882481a2…3490f8`, vérifie l'upgrade vers E, puis les préférences
sandbox inchangées avant le premier Activity. L'application production est
vérifiée à son SHA E ; ses7 fichiers de préférences restent identiques en octets
avant/après les4 gestes Lab. Aucun achat ni grant n'est effectué.
Les400 LumaGold de test sont un état Lab préexistant et restent inchangés.

## Limites et échecs conservés

Deux sélecteurs de programme étaient incorrects (`hat_rose`, `Undo.*` sans
glyphe initial). Les rapports C et D échoués restent intacts, avec traces et
PNG ; des programmes distincts corrigés ont passé. Une première création AVD
sans Java a échoué avant toute création/installation ; sa sortie originale et
le reçu de la création réussie séparée restent conservés.

À640×960/font1.3, certains noms d'objets/onglets sont abrégés ; les labels AX et
le footer restent disponibles. La barre système d'horloge est partiellement
coupée dans les premières captures de l'AVD E vide ; le contenu du jeu reste
dans sa zone et les8 médias canoniques du profil conservé sont normaux.

[normal-cleanup.json](normal-cleanup.json) confirme les arrêts normaux des4 AVD
utilisés, sans effacer leurs profils, la désinstallation des seuls observateurs
et la restauration1080×2400/densité420/font1.0. Aucun reset de5626 ni d'un autre
profil. Ces vérifications SDK ne prouvent ni confort sur téléphone physique,
ni plaisir/rétention humaine, ni acceptation Play ou commerce réel.
