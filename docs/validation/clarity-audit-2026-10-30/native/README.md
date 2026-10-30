# Parcours natif de lisibilité — 30 octobre 2026

Audit du jeu **déjà livré**, production `1.22.0-rc1`, code 30, candidat C. Le programme passe par les vrais contrôles de l'interface avec l'observateur SDK existant ; il ne remplace pas un essai humain. Il n'importe pas les classes du jeu, ne modifie pas les préférences, n'appelle pas de méthode privée et ne consulte pas de certificat ou solveur pour jouer.

L'identité autorisée est liée à la signature, au nom/version/code et aux octets de l'APK installé : SHA-256 `9b2593388de22b1f50f49bea05b9b02008d4574f5cbb9012fb0df5a64c47a44e`. L'observateur a SHA-256 `de0c47173b4268f0d0b81c17c6af7afea600eb6cc691a3590249de495bf3eb59`. Seul l'AVD détenu par cette tâche, `Luma119-Battle-QA` / `emulator-5602`, est utilisé, Android API 36, ARM64, 1080×2400, pages mémoire 16 Kio. Aucun APK candidat n'est installé ou mis à jour ; seul l'observateur temporaire est installé puis désinstallé par le runner.

## Tentative et périmètre

La découverte retrouve une vraie tentative U42 : historique `0,1,2`, trois coups utilisés sur neuf, équipe `nova,miko,orbit`, charge six, deux coquilles restantes (eau une couche et glace deux). Aucun nouveau niveau n'est créé, aucune sortie supplémentaire n'est exécutée. Les talents et équipements sont inspectés sans activation, achat ou application. La langue passe de l'anglais au français par les Réglages natifs.

Le [programme continu](programs/navigation-complete.json) reprend le niveau, ouvre le talent de Nova puis revient au plateau ; utilise Pause→Quitter le niveau ; visite Équipe, Collection, Mes compagnons puis Nova ; rejoint le Dressing par le chemin réellement disponible ; inspecte Équipement, les catégories et une coiffe ; revient à l'accueil et reprend le même U42. Il finit par Pause→Quitter pour conserver la tentative avant l'arrêt de l'AVD.

Le parcours continu passe **39 assertions et 20 touches**, avec **11 PNG originaux inspectés via `view_image`**. Les assertions comparent les six champs `run_id`, `history`, `budget_used`, `undos`, `elements_v121` et `battle_v119` pendant les menus et après la reprise. Les instantanés lisent les sept ensembles de préférences déjà prévus par l'observateur, en omettant ses clés contenant « seed ». Parmi tous ces champs observés, seule la durée `elapsed_ms` varie : 14 930→19 335 ms, pendant les passages sur le plateau. Monnaies, propriété, équipement et progression observés restent identiques. Les détails finaux, captures et empreintes sont réunis dans [l'index d'exécution](index.json).

## Ce que le parcours révèle

1. Le plateau affiche monnaies, grand budget, trois compagnons, charge/résonance, Focus, objectif, progression, un second budget et chrono avant la grille. La capture montre les marques glace/eau traversant les flèches. Ce sont des constats de présentation ; ils ne mesurent pas la difficulté ressentie.
2. Le talent de Nova ouvert depuis le plateau s'appelle **Étoile guide** et décrit un prochain geste sûr pendant huit secondes, pour deux charges. La fiche du même Nova depuis la collection explique **Voir plus loin**, une rencontre future avant le choix d'une route. Ce deuxième texte concerne l'expédition ; le premier écran de cette fiche ne nomme pas ce contexte. La différence fonctionnelle mérite une séparation explicite des usages.
3. La fiche de collection s'intitule **Aperçu**, répète les monnaies sous le HUD fixe et place ses informations/actions sur une page longue. Dans les contrôles observés, elle ne propose ni Dressing direct ni retour au niveau. Pour rejoindre la tenue, le chemin exécuté repasse par liste→hub Collection→Accueil→Style et tenues. La sauvegarde ne se perd pas, mais le retour contextuel n'est pas proposé sur cette fiche.
4. Équipe précise que les changements serviront à la prochaine partie. Cette séparation est utile, mais elle coexiste avec une apparence portée, une garde-robe, des équipements et plusieurs capacités d'un même personnage. Une fiche unique devrait indiquer clairement « apparence », « talent de puzzle » et « talent d'expédition », avec accès contextuel, sans promettre que toutes ces fonctions s'appliquent immédiatement à la tentative active.

Ces constats soutiennent le [diagnostic visuel et ses sources primaires](../visual-research/README.md). Ils ne démontrent ni incompréhension chez tous les joueurs, ni plaisir, ni rétention. Aucun nouvel habillage n'est livré par cet audit.

## Exécutions préservées

- [Découverte](../../v1.22/native/clarity-retained-discovery/report.json) : sept assertions, aucune touche, un original et état réel avant navigation.
- [Premier programme interrompu](../../v1.22/native/clarity-retained-navigation-a/report.json) : la garde du runner refuse un nom de capture commençant par un chiffre, après trois touches de navigation. Le résultat reste FAIL, avec capture et trace originales. Il ne s'agit pas d'une erreur de l'application.
- [Programme A corrigé](../../v1.22/native/clarity-retained-navigation-a-retry/report.json) : 29 assertions et onze touches, jusqu'à la fiche Nova ; six champs de tentative inchangés. Les originaux restent distincts du parcours continu final.
- Le [parcours continu final](../../v1.22/native/clarity-retained-navigation-complete/report.json) passe 39 assertions, 20 touches et onze captures. Ses sorties originales, assertions, actions et captures sont conservées ; le détail exact est repris dans l'index.

Les programmes [initial](programs/navigation-a.json) et [corrigé](programs/navigation-a-retry.json) restent tous deux accessibles. Une relance de la découverte vers son répertoire déjà existant a été refusée avant toute interaction ; ce verrou d'immutabilité n'a rien écrasé. Aucun échec n'est renommé en succès.

La [preuve de nettoyage](cleanup.json) confirme la désinstallation de l'observateur, son absence avant arrêt et l'arrêt normal de l'AVD appartenant à cette tâche. Le jeu et ses données restent installés ; aucune suppression de profil n'est effectuée.
