# QA navigateur locale — site 1.23 et rapport cognitif

Le contrôle local est **PASS dans le périmètre ci-dessous**. Les observations
proviennent des gestes et relevés DOM réellement réalisés par l’opérateur root ;
une seconde lecture a inspecté quatre JPEG et rapproché les chiffres, sources et
empreintes. Aucun fichier public ou app n’a été modifié pour rédiger ce rapport.
Cela ne vérifie pas le site de production distant.

## Accueil : huit états et 40 observations d’images

[Observations originales](home-final/observations.json) : FR/EN aux largeurs de
viewport 320,375, 736 et 1280. La largeur de contenu mesurée est respectivement
305, 360, 721 et 1265 ; aucun débordement horizontal n’est relevé dans ces huit états.
Les titres passent bien en français et en anglais.

Les 40 observations d’images sont **cinq occurrences par état**, couvrant huit
assets natifs uniques : le puzzle apparaît dans le héros et dans la galerie,
puis talent, tenue et carte. Chaque occurrence est chargée (`complete=true`),
visible au sens du relevé DOM et possède une dimension intrinsèque 1080×2400.
Ce décompte ne représente ni 40 captures natives différentes ni 40 images affichées
simultanément à l’écran.

L’opérateur root a également sélectionné Hoot, forme 4, action Saut, et observé
le rendu canvas 720. La [capture conservée](home-final/gallery-hoot-form4.jpg)
illustre cet état. Les quatre éléments audio ont été observés en pause, sans
autoplay et avec `preload=none`. **La lecture audio n’a pas été testée.** Ces deux
observations supplémentaires sont issues du passage de relais de l’opérateur ;
elles ne sont pas inventées comme champs des deux relevés JSON.

## Labo : quatre conditions et huit états

[Observations originales](lab-final/observations.json) : entrée directe anglaise,
quatre conditions réellement sélectionnées et six changements de langue
FR→EN alternés, puis FR/EN aux mêmes quatre largeurs. Chaque état responsive
rapporte le statut chargé et aucun débordement horizontal.

Les valeurs de blocage, épuisement, énergie, caps, Coins, Undo et erreurs répétées
relevées pour les quatre conditions correspondent au `data/view.json` final.
L’affichage 45,337 pour les deux premières conditions vient de l’arrondi à trois
décimales de 45,337 et45,3365 ; cela ne constitue pas un état périmé.

Des essais de ciblage de sélecteur par l’outil de pilotage ont été inopérants.
Ils ne démontrent pas un bug du produit et ne sont pas comptés comme tests
réussis : la couverture repose sur les quatre états effectivement relevés.
Les 11 tests automatisés distincts du rapport restent documentés dans la
[validation du kit](../../cognitive-lab-v123/kit-validation.json).

## Inspection visuelle indépendante

Quatre JPEG originaux ont été ouverts sans retouche :

- [Accueil FR 375, héros](home-final/fr-375-hero.jpg) : titre replié et actions
  distinctes, aucun chevauchement visible.
- [Accueil EN 1280, captures](home-final/en-1280-screens.jpg) : carte et légendes
  lisibles ; la quatrième carte occupe le début de la ligne suivante.
- [Labo FR 375](lab-final/fr-375.jpg) : compteurs sur deux colonnes et contenu
  lisible, sans texte coupé observé à cette position.
- [Labo EN 1280](lab-final/en-1280.jpg) : sélecteur, compteurs et métriques rangés
  sans collision visible.

Cette inspection échantillonne quatre positions de défilement ; elle ne prétend
pas examiner chaque pixel de tous les états ni mesurer le confort humain.

## Empreintes et limites

Le [rapport structuré](report.json) lie les deux relevés originaux, les 25 JPEG,
`public/index.html`, `assets/site-v123.css/js` et les sept fichiers finaux du
rapport cognitif, ZIP inclus. Leurs empreintes correspondent au manifeste source
du site 1.23 préparé. Les captures JPEG sont des preuves du navigateur local ; les
PNG natifs du site et leur identité APK sont qualifiés séparément dans le
[précontrôle](../../v1.23/publication/preflight-lab.json).

Ce rapport n’établit aucune livraison distante, lecture audio, installation APK,
qualification Android supplémentaire, compréhension, émotion, plaisir ou
rétention humaine.
