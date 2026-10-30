# Proposition de plateau et de navigation contextuelle

La proposition place le plateau avant les systèmes secondaires et garde le
compagnon dans la partie. Sa fiche s'ouvre sur le même écran, avec un retour
explicite au niveau 42 et au budget conservé. Le parcours distingue talent de
puzzle, tenue en essai/appliquée et équipement à préparer pour la prochaine partie.

[Fragment reproductible](plateau-clair.fragment.html) ·
[23 observations dans le navigateur](browser/report.json).

Le fragment affiché dans la conversation et sa copie ici ont les mêmes octets.
Le rapport de navigateur en conserve la SHA256. Une capture Android originale
est intégrée sans modifier ses octets et sert de référence dans le second volet.
La proposition utilise une vue de ce même portrait pour garder Nova comme repère.

## Interactions vérifiées

- Tap bloqué : cause visible, aucun coup dépensé ; sortie de clé : passage ouvert.
- Fiche compagnon : plateau conservé, contenu arrière inerte et clavier contenu.
- Tenue : « En essai », fermeture qui annule le brouillon, « Appliquer », « Porté ».
  L'action d'annulation n'apparaît que pendant un essai.
- Retour : mêmes pièces et même budget ; talent d'indication sans sortie automatique.
- Undo de démonstration : pièce rétablie, coup déjà dépensé et charge conservés.
- Six sorties effectives dans le modèle réduit puis état de réussite.
- Quatre largeurs de navigateur : 320, 375, 736, 1024. Cibles de pièce au moins
  44,59 px, aucun débordement horizontal observé. Quatre vues de plateau, deux
  fiches et la référence native sont conservées en JPEG originaux.
- Navigation au clavier, cycle de focus, Échap et comparaison entre les volets.
- Aucun avertissement ou erreur console observé dans le périmètre final.

Les premières vérifications ont trouvé un retour d'état du conteneur qui fermait
la fiche et vidait l'historique. Le [constat initial](initial-state-echo-failure.json)
est conservé ; le runtime ignore désormais son propre écho d'état. Un premier
rapport et ses observations avant les derniers réglages restent dans
`browser/report-before-final-layout.json` et ne sont pas l'acceptation finale.

## Portée de la maquette

La géométrie visuelle s'inspire de U42, avec six flèches et une association clé/
verrou. La proposition retire les coques. Ses collisions, son talent, son Undo
et son état sont un modèle réduit de démonstration, sans dépendance au moteur
Android. Elle n'implémente ni l'intégralité des relais/portes, ni l'économie,
les prix d'Undo après les retours gratuits ou les transactions d'équipement.

Les deux pastilles de tenue simulent un essai de couleur. Elles ne dessinent pas
de nouveaux vêtements et ne modifient aucun personnage Android. La proposition
n'est ni une capture Android, ni une nouvelle release, ni une preuve que cette
DA plaît aux joueurs. Sa palette et sa grille sont encore schématiques ; la
relecture visuelle relève un rendu générique et un compagnon encore discret.
Les symboles de clé ont ensuite été agrandis à 11 px et les cases vides atténuées.
Cette correction de hiérarchie ne constitue pas une DA finalisée.

Le moteur, les APK et le site public restent ceux de la 1.22 qualifiée. Les
résultats sans coques sont dans un [protocole expérimental séparé](../experiment-no-shell/README.md).
