# Rapport public 1.24 — préparation locale

Rapport séparé : [page FR/EN](../../../../public/lab/clarity-124/index.html). Le libellé de capacité distingue maintenant la capacité liée au niveau, avant contrôle expérimental, du plafond à 1 usage réellement appliqué au contrôle. Le test vérifie ce contraste sur les 87 391 départs concernés ; aucune donnée de simulation n’a changé.

Les quatre conditions reprennent exactement le [run figé](../simulation/README.md), graine 2026103102. Les paramètres de 100 profils × 20 répétitions sont appariés ; les trajectoires divergent selon les actions. La politique demande souvent GUIDE avant de suivre un indice visible : les usages répétés ne sont pas autant d’informations nouvelles. Aucun résultat humain ou effet causal 1.23→1.24 n’est revendiqué.

[Validation](validation.json) : 16 tests runtime passent dans le dépôt. Ils vérifient FR/EN, accès EN direct, changements de conditions, compteurs, quantiles, rang/copie/capacité, arrivées/départs/épuisements et fichiers bruts exacts ; des données partielles empêchent l’affichage. La [reproduction par extraction](source-only-replay.json) relie les 68 sources byte-identiques au kit précédent déjà recompilé et testé (deux petits runs déterministes de 48 trajectoires), puis passe 15 tests runtime actualisés ; le seul test raw est explicitement absent du kit source et reste exécuté dans le dépôt complet.

Le kit ZIP des sources fait moins de 1 Mo et ne contient aucun CSV gzip. Il fournit les 68 sources exécutées, les tests/runner, le protocole, les empreintes et les agrégats. LumaView est une copie supplémentaire non compilée, nécessaire à l’ancien helper de hachage ; ce fichier ne qualifie pas le rendu Android. Les 11 ledgers gzip sont téléchargés séparément avec URL, SHA256 et taille dans le kit et [le manifeste](source-kit-manifest.json).

Le [manifeste de préparation](site-manifest.json) lie les 19 fichiers publics, les sources et le kit. Les 37 fichiers historiques des rapports cognitive et clarity-123 sont inchangés. Cette préparation et ces tests locaux ne prouvent ni déploiement, ni octets publics distants, ni rendu dans un navigateur réel.
