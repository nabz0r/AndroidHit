# Collection et garde-robe — proposition1.19

Le retour humain sur l’APK1.18H prime sur les anciennes contraintes de présentation1.17. La sélection des personnages, des vêtements et des équipements paraît peu claire, trop binaire et peu dynamique. Les parcours Android ont passé leurs contrôles fonctionnels ; cela ne valide ni leur compréhension ni la fluidité ressentie. Cette proposition n’est pas une implémentation de l’APK et ne change pas son économie.

## Ce que les écrans expliquent mal

| Observation vérifiée | Conséquence probable à vérifier avec le joueur | Changement proposé |
|---|---|---|
| La collection juxtapose quatre catégories, quatre filtres, quatre compagnons et une pagination. Un appui quitte cette grille pour une page générique. | Il faut reconstruire son contexte à chaque consultation ; le personnage courant et l’objet essayé n’occupent pas une position stable. | Une grille reconnaissable et une fiche qui conserve un grand aperçu, avec retour au filtre et à la position exacts. |
| La fiche met les deux monnaies avant le grand aperçu, puis empile état, rareté, niveau, copies, règles, compétence et actions. | L’action utile peut arriver après un long défilement ; des informations secondaires concurrencent le choix. | Identité, état actuel et aperçu en premier. Onglets Évolution/Tenue/Compétence et une action principale ancrée. |
| La garde-robe place le portrait versy222 et son choix original/tenue versy887 ; les captures de la partie basse ne montrent plus le résultat sur le corps. | On sélectionne un objet sans comparer simultanément l’avant et l’après. | Aperçu persistant, quatre vrais emplacements visibles et liste d’objets qui défile séparément. |
| Le même texte « Costume du compagnon · changer » désigne une préférence durable en garde-robe et un aperçu temporaire dans le studio. | La portée du choix est ambiguë, même si les deux comportements sont correctement séparés dans le modèle. | Distinguer « En essai » de « Porté » et nommer l’action « Appliquer cette tenue ». |
| Acheter ajoute la possession ; un second appui équipe. Après application, `showAppliedLook` redirige vers la garde-robe. | La sécurité transactionnelle est utile mais l’étape suivante et le changement de lieu sont insuffisamment annoncés. | Après acquisition, rester dans le même contexte et proposer explicitement « Utiliser ». Ne pas équiper ni changer de lieu silencieusement. |

Sources locales : `LumaView.java`, méthodes `drawCollection` (~1795), `openItem` (~1834), `drawItemDetail` (~1899), `drawWardrobe` (~1598), `showAppliedLook` (~1644), `drawCompanionMotion` (~1868) et `backMenu` (~298). Les coordonnées ci-dessus sont celles du dessin logique, pas des mesures de temps. Les preuves visuelles H lues sont les captures françaises Collection, garde-robe et studio sous `docs/validation/v1.18/native-fresh-h/final-art-h-six-languages/native/`.

## Un parcours, trois vues reliées

**Collection.** Les37identités restent dans une grille, avec filtre, position et compagnon actif mémorisés. Une vignette distingue « Actuel », « Possédé » et « À obtenir ». La rareté est un petit attribut d’identité ; le niveau et les copies ont leur propre ligne. Toucher ouvre la fiche ; un appui long peut être un raccourci, jamais une obligation.

**Fiche du compagnon.** Le même grand aperçu reste présent quand on change d’onglet. Évolution montre la forme actuelle, la prochaine forme et les copies manquantes sans modifier le niveau. Tenue ouvre la garde-robe du même compagnon. Compétence décrit uniquement Nova/révélation, Miko/changement d’un puzzle et Orbit/route de projet ; les autres indiquent honnêtement l’absence de signature disponible. Une action « Choisir ce compagnon » applique uniquement le choix du personnage.

**Garde-robe.** Le portrait reste visible au-dessus des quatre emplacements Chapeau/Tenue/Chaussures/Gants. Toucher un objet modifie un essai local, pas la sauvegarde. Le pied d’écran affiche le changement en cours et « Appliquer cette tenue ». Après application, « Porté » remplace « En essai » sans ouvrir une autre page. Le costume original est un choix nommé et visible au même endroit ; le retirer de la présentation ne détruit aucun objet ni aucune sélection. Les équipements restent un mode distinct et annoncent leur effet de jeu.

La navigation inférieure garde quatre destinations nommées : Maison, Jouer, Collection, Atelier. Les expéditions sont une entrée explicite de Jouer et de la compétence éligible. Retour reste visible dans la fiche et la garde-robe ; Maison garde une position stable. Les gestes sont des raccourcis facultatifs. Cette proposition remplace délibérément l’ancienne interdiction générale des barres/boutons visibles, conformément au dernier retour humain.

## Quatre états à ne plus confondre

| État | Signification | Ce qui peut écrire en sauvegarde |
|---|---|---|
| Possédé | L’objet appartient au joueur | Une acquisition confirmée, via les transactions existantes |
| Porté / Actuel | Le choix actuellement enregistré | « Appliquer » ou « Choisir ce compagnon » |
| En essai | L’aperçu local diffère du choix enregistré | Rien |
| À obtenir | L’objet n’est pas possédé | Rien tant que l’acquisition n’est pas confirmée |

Retour garde le brouillon d’essai et la position de liste pendant cette visite. Maison permet de retrouver le contexte au retour ; aucune préférence n’est appliquée silencieusement. La reprise après recréation restaure l’état de présentation sans fabriquer une acquisition ou une nouvelle compétence.

## Architecture proposée, sans migration économique

`CollectionRoute` porte catégorie, filtre, identité sélectionnée, ancre de défilement et origine du retour. `CollectionPreviewDraft` porte le personnage essayé, le choix original/tenue, les quatre sélections cosmétiques et les équipements essayés. `CommittedPresentation` reflète seulement les données enregistrées. Une seule projection produit le portrait, les états des vignettes et le résumé de l’action.

`CollectionFlow` traite les intentions utilisateur et garde ces états séparés. `CollectionScene` et `WardrobeScene` dessinent une géométrie adaptative, avec grand aperçu et action ancrés ; seule la zone de choix défile. Les règles de progression, les prix et les probabilités restent les références existantes. **Appliquer un brouillon multislots nécessite une nouvelle transaction atomique groupée** : les API actuelles équipent un cosmétique ou un équipement à la fois ; les appeler successivement ne rendrait pas l’ensemble atomique. Le résultat confirme le changement dans la même vue et les appuis consécutifs ne peuvent pas doubler une acquisition.

Le mode original/tenue, les quatre cosmétiques, les trois équipements et le style sont actuellement globaux au profil. La garde-robe affiche donc « Tenue commune à tes compagnons », même quand on l’essaie sur Nova. Seule la stance est enregistrée par compagnon. Le studio actuel enregistre immédiatement un choix de stance, alors que son aperçu de vêtements est temporaire ; cette différence doit être nommée ou regroupée sous une application explicite, jamais masquée par un seul mot « aperçu ». Maison montre toujours le choix enregistré, tout en conservant un essai nommé que l’on peut reprendre explicitement.

La validation de l’application groupée vérifie tous les emplacements et toutes les possessions avant une seule écriture. Un refus conserve le brouillon, ne débite rien et n’affiche pas « Porté ». Un changement d’équipement seul conserve le mode costume original ; il ne déclenche pas le mode vêtements. La sauvegarde de présentation de l’Activity peut restaurer le brouillon pendant la visite/recréation ; une survie au force-stop n’est pas promise sans stockage dédié.

Le défilement actuel est global. Pour un aperçu fixe et une liste mobile, le propriétaire du geste doit être choisi au DOWN, avec verrouillage de l’axe et annulation de l’appui après le seuil de glissement. Parent et enfant ne doivent jamais consommer deux fois le même geste. Le retour porte route, filtre, page, identifiant d’ancre et décalage, plus l’identifiant de focus ; une simple chaîne de nom d’écran ne suffit pas.

L’intégration éventuelle remplace progressivement les branches de présentation concernées dans `LumaView` ; elle ne réécrit pas le moteur de puzzle, le catalogue ni l’historique des reçus. Les cibles sémantiques conservent une identité stable et annoncent le statut courant, l’objet essayé et son prix/source. À320dp et police1,3, l’aperçu et l’action restent visibles ; les choix et textes secondaires défilent.

## Dynamisme utile et performance encore inconnue

Le toucher doit produire un état pressé et un changement de sélection au prochain rendu disponible. Le portrait peut ensuite se charger de façon asynchrone dans un espace de taille stable ; une génération de requête empêche une ancienne réponse de remplacer un choix récent. La transition courte relie le même personnage entre la grille et la fiche ; la réduction des animations garde immédiatement le même résultat et les mêmes actions. Les retours préservent filtre, position et focus.

Ce sont des contrats à vérifier, pas des FPS mesurés. La lecture de source relève des demandes de redessin des menus espacées de50ms, une transition de220ms qui bloque les entrées, un cache d’art48MiB, et du décodage de régions/scannage alpha synchrone en cas de cache manquant. Il ne s’agit pas de décoder les148WebP entiers à chaque affichage. Le chargement initial lit toutefois les148JSON et les en-têtes d’image ; `InventoryPresentation` reconstruit les37entrées de collection lors du dessin. Ces points sont des pistes de profilage, pas des causes prouvées de la lenteur ressentie. Une future qualification Android doit distinguer latence tactile, décodage, temps des images, mémoire et compréhension humaine. Les contrôles fonctionnels H ne sont pas annulés ; leur portée reste différente.

## Direction artistique à pousser après la maquette

La cible est une collection de personnages désirables à regarder et à manipuler, avec une hiérarchie portrait → identité → état du choix → prochaine action. Des cadres de rareté peuvent varier par silhouette, matière et accent, avec le texte N/R/SR/SSR toujours lisible ; ils n’annoncent ni le niveau ni une compétence inexistante. Les personnages restent le centre visuel, les fonds et ornements soutiennent leur lecture au lieu de concurrencer leurs contours.

Les animations doivent expliquer un changement : la vignette devient le même portrait, l’emplacement essayé répond au toucher, l’application fait passer « En essai » à « Porté », une évolution permet de comparer les deux formes. Les effets de révélation sont réservés aux vraies acquisitions. Aucun clignotement constant, pluie d’effets ou nouvelle couche de récompenses n’est ajouté pour donner artificiellement une impression de richesse. Cette direction reste à dessiner et à essayer après le parcours ; le style sobre de la maquette n’est pas proposé comme la direction artistique finale.

## Ce qu’on reprend de Crash Fever

Le [guide officiel](https://crashfever.com/syosinnsyagaido/) montre le résultat attendu et les ressources nécessaires avant de lancer une amélioration. Ses images portent un chemin daté2018/11 : c’est un exemple historique, pas une preuve de l’interface actuelle de l’application en2026. La [FAQ officielle des unités](https://crashfever.com/faq/faq_cat/faq5/), consultée le29octobre2026, documente l’accès au détail depuis la vignette, les apparences depuis la fiche et les renseignements d’obtention. Ces sources soutiennent une continuité vignette → fiche → aperçu → décision ; elles ne prouvent pas que cette copie de parcours améliorera la rétention de Luma.

On reprend la lisibilité et l’anticipation du résultat, pas les couches d’XP/éveil/matériaux de Crash Fever : Luma reste sur des niveaux par copies. On garde la protection contre les appuis consécutifs et les doubles transactions. Les sources ont été consultées le29octobre2026 ; la proposition visuelle est une inférence de conception, pas un résultat d’essai utilisateur.

## Proposition à essayer avant toute nouvelle APK

La maquette locale `collection-prototype.html` relie les trois vues, les37identités, les filtres, les retours, les onglets et les quatre emplacements. Elle utilise les dessins du jeu ; les essais et applications y restent en mémoire de démonstration. Elle n’écrit aucune préférence Android, ne donne aucune copie, ne dépense aucune monnaie et ne qualifie pas l’ajustement des vêtements. Les aperçus et le code sont intégrés au HTML ; elle fonctionne sans serveur local ni dépendance externe. Une copie byte-identique est préparée sous `public/lab/ui-1-19/index.html`, accessible par lien direct et sans ajout de CTA à la page principale.

Avant d’implémenter, faire réaliser au joueur trois tâches : choisir un compagnon possédé puis revenir à sa place ; essayer deux vêtements et dire lequel est réellement porté ; voir la prochaine forme et retrouver comment obtenir les copies nécessaires. Observer hésitations, retours involontaires et erreurs d’état. Aucun taux de succès, temps cible ou résultat positif n’est inventé ici.
