# Qualification amis et cadeaux — état 1.9 et protocole à deux comptes

## État actuel · 27 octobre 2026

Les correctifs du tutoriel, les 100 premiers niveaux, les cadeaux locaux, les styles et les gardiens de [Luma Drift 1.9](RELEASE_1_9.md) ont des preuves distinctes dans l’[index de qualification](validation/v1.9/README.md). **Aucun de ces tests ne qualifie une vraie session Google, une amitié acceptée ou un cadeau échangé entre deux comptes.** Les six profils comportementaux reliés à la recherche 2026 sont des politiques synthétiques, sans personnes, identités Google ou trafic social fictifs. [Méthode et limites](PLAYER_AUDIT_2026.md).

La candidate production **FINAL5 — 1.9.0-rc1 / code 10** a l’empreinte APK `b02225c0464bf35cc1c04f64f0a4c8b1db610bde36b293bae9790f1a9e2c2b05`. La Lab **1.9.0-sandbox**, empreinte `acb3e1b0cfa71f84435d6cb0cf0fc73e3d4b434009deef972c54d86d9e7c09c7`, sert aux essais invités isolés ; son package `.sandbox` ne doit pas être présenté comme une qualification OAuth du package production.

Les qualifications natives **FINAL5** passent sur leurs APK exactes : [parcours complet et menus/gardiens/langues](validation/v1.9/ui-sdk-final5-full100/host-report.json), **2 277 contrôles / 79 PNG**, environnement sauvegardé restauré ; [première installation D](validation/v1.9/ui-sdk-fresh-guest-d/host-report.json), **85 contrôles / 17 PNG**, Lab absente avant, production absente avant/après, nouvel invité conservé et rotation restaurée ; [démarrage production](validation/v1.9/ui-sdk-production-final5/host-report.json), **7 contrôles / 1 PNG**, environnement sauvegardé restauré. Le parcours complet et le démarrage utilisent API 36 ARM64 avec pages de 16 Kio ; D utilise des pages de 4 Kio. Le [troisième essai natif d’écriture bloquée](validation/v1.9/ui-sdk-final5-storage-fault3/host-report.json) passe **524 contrôles / 14 PNG** sur la Lab exacte : deux cycles réels de permissions `500 → 771`, action de retrait de couche conservée malgré Retour, bouton physique de reprise, reçu et +100 coins une seule fois (**1 830 → 1 930**), reprise encore verrouillée, rotation et seconde réclamation sans crédit. Cache mémoire et XML disque sont contrôlés séparément ; permissions, rotation et environnement de test sont restaurés. Cette preuve couvre une recréation Activity dans le même processus, pas la mort du processus pendant la transaction en attente. Les deux essais bruts échoués restent conservés dans l’index : délai de rotation du banc avant verrouillage pour le premier, attente du libellé « Retry » au lieu du bouton réel « Try again » pour le deuxième. Aucun de ces essais n’est présenté comme une validation de connexion Google. FINAL5 conserve la reprise exacte Studio/liste des gardiens/rencontre active introduite en FINAL4, bloque la navigation tant qu’un essai gardien reste non sauvegardé, ajoute une reprise typée des écritures locales en échec et attribue le geste au véritable bouton ou pion touché. Ces changements et les essais JVM n’établissent aucune vraie session Google.

La [première installation Lab sur B](validation/v1.9/ui-sdk-fresh-guest-b/host-report.json), API 36 et pages de 4 Kio, a passé 85 contrôles sur l’ancienne FINAL3, empreinte `a16fbc03a75455805b7f3f491a8fc26cddc4e33637ec0b8bc1668700e83de36f`. Son nouveau profil de test est conservé, les réglages de rotation restaurés et l’APK production 1.7 déjà présente inchangée. La [première installation C](validation/v1.9/ui-sdk-fresh-guest-c/host-report.json) a passé **85 contrôles / 17 PNG** sur l’ancienne FINAL4, empreinte `f8d554a446d4511baafb4edc3012f4014d0ae420c02e6fffdc29fc58b93a9e08` : Lab absente avant, production absente avant comme après, nouvel invité conservé et rotation restaurée. Le [parcours complet normal FINAL4](validation/v1.9/ui-sdk-final4-full100/host-report.json) a passé **2 277 contrôles / 79 PNG** puis restauré son environnement de test. Ces preuves historiques gardent leurs empreintes ; elles ne qualifient ni FINAL5, ni ses nouveaux chemins d’échec persistant, ni un compte Google. Aucun ancien PASS ou échec brut n’est réétiqueté. Les états des AVD du protocole ci-dessous restent datés et doivent être relus avant leur prochaine utilisation.

Le propriétaire indique que son **compte développeur Play est approuvé**. Il s’agit de sa déclaration, sans observation indépendante de la Console ; l’application Play reste à créer après cette livraison. Cette approbation de compte ne valide ni SSO, ni produits LumaGold, ni pubs réelles. Les APK finales forcent les annonces de test, n’embarquent pas d’origine de portefeuille payant et gardent les achats/tirages premium fermés. [Frontière production et activation](PRODUCTION_RELEASE.md).

Pour reprendre le protocole, identifier d’abord la version production réellement testée sur chaque appareil et conserver son empreinte. Si les deux appareils passent de la base 1.7 à la production 1.9, consigner cette mise à jour et la préservation des données ; ne pas réétiqueter les anciennes captures. Utiliser deux vrais comptes Google distincts et le protocole ci-dessous, puis observer chaque résultat. Une préparation serveur ou une simulation JVM ne peut pas remplacer ces observations. La connexion reste facultative et ne synchronise pas le parcours, les LumaCoins, les essais de pouvoirs, le tutoriel ou les gardiens locaux.

## Protocole conservé — base de préparation 1.7 et suivi 1.8

Le texte suivant conserve sa version, ses ports, ses empreintes et ses observations d’origine. Il décrit un parcours à exécuter, pas des échanges qui auraient déjà réussi. Les mentions « prêt » ou « arrêté » décrivent ce précédent état de préparation.

**Priorité actuelle : niveaux, tutoriel et UX 1.8.** Les comptes Google et échanges réels restent à qualifier. La nouvelle candidate [1.8](RELEASE_1_8.md) a ses tests Android invités séparés ; ils ne valident aucun compte Google ni cadeau serveur. Le protocole ci-dessous conserve sa base 1.7 installée sur les deux appareils : ne pas mélanger ces captures avec les tests Lab 1.8. L’appareil B est arrêté, ses données conservées ; A conserve sa production 1.7 et dispose aussi d’une installation Lab 1.8 invitée distincte.

27 octobre 2026 · base figée **Luma Drift production 1.7.0-rc1 / code8**.

Deux appareils locaux neufs et indépendants sont préparés : **Luma-Social-A** (`emulator-5582`) et **Luma-Social-B** (`emulator-5584`). Ils utilisent l'image officielle Android36 GooglePlay ARM64 révision7. Le Play Store et les services Google sont installés. La même APK production est installée et son empreinte relue sur chacun : `7eeaa816853533f441acfc73f4ad3f6af0fb2cbdd6fd0eae1fda3f64c388a2bf`. Aucun profil utilisateur précédent n'a été cloné. [État de préparation observé](validation/social-two-devices-v1.7/setup.json).

**État : appareils prêts ; connexion Google, échanges d'amis et cadeaux réels encore à effectuer.** Une installation et un modèle serveur qui passe ses tests ne constituent pas une réussite OAuth réelle. Les résultats seront consignés après chaque étape observée ou rapportée par le propriétaire.

Le test utilise le vrai projet Firebase `luma-drift-production` et le serveur CloudRun existant. Aucun compte Google/Firebase fictif ne sera créé pour présenter une fausse qualification. Utiliser deux comptes Google distincts ; un alias Gmail `+...` ne crée pas une deuxième identité. Les mots de passe, codes de validation, jetons et données de paiement restent dans les interfaces Google : ne pas les transmettre dans le chat ni les placer dans le dépôt.

## Étape1 — connecter le premier appareil à Google

Dans la fenêtre **Luma-Social-A**, ouvrir **Play Store → Se connecter** et terminer le parcours avec le premier compte Google. Le propriétaire saisit lui-même les informations et validations demandées par Google. Résultat attendu : le Play Store affiche le compte connecté. S'arrêter ici pour confirmer ce résultat avant de continuer.

## Étape2 — connecter le jeu au même premier compte

Ouvrir **Luma Drift** sur A. Sur l'accueil, toucher la carte de profil en haut → **Se connecter avec Google** et choisir le compte A. Résultat attendu : **Connecté en tant que …**. Revenir à l'accueil et vérifier que le profil n'est plus présenté comme invité. En cas d'erreur, relever uniquement son message visible.

## Étape3 — préparer le second compte

Reproduire les étapes1 et2 dans **Luma-Social-B**, avec un autre compte Google. Ne pas utiliser Luma Drift Lab : son package `.sandbox` n'est pas celui de la configuration OAuth Android enregistrée pour ce test. La connexion ne synchronise pas la progression ni les LumaCoins locaux.

## Étape4 — créer deux profils sociaux privés

Sur chaque appareil : accueil → faire défiler → **Amis et cadeaux**. L'écran **LUMA CIRCLE** affiche **TON CODE AMI**, en quatre groupes de quatre caractères. Enregistrer deux surnoms distincts, par exemple `Luma Test A` et `Luma Test B`, avec **Enregistrer mon profil**. Laisser **Partager ma position sur la carte** désactivé. Les codes ne sont pas des mots de passe ; ne pas publier les identifiants privés ou emails des comptes dans les preuves.

## Étape5 — invitation et acceptation mutuelle

Sur A, saisir le code de B puis **Envoyer une invitation**. Sur B, **Actualiser le cercle → INVITATIONS REÇUES → Accepter**. Actualiser A : les deux écrans doivent afficher **TON CERCLE · 1 / 40**, le bon surnom et **Position privée**. Vérifier qu'une invitation seule ne crée pas encore une amitié acceptée.

## Étape6 — un cadeau et une seule charge

Avant l'envoi, relever sur B le stock de **Inventaire → Pouvoirs → Étincelle** (l'indice). Sur A, choisir **Offrir 1 indice** à B : le quota passe de3/3 à2/3. Sur B, **Actualiser le cercle → Recevoir l'indice**. Le stock doit augmenter exactement de1, sans variation de LumaGold et sans achat.

Recommencer l'envoi A→B le même jour UTC : le serveur renvoie le même cadeau, pas nécessairement un message d'erreur. Le quota doit rester2/3 ; B ne doit pas recevoir une deuxième charge. **Vérifier la réception** et une fermeture/réouverture doivent également ajouter zéro. En cas d'inventaire plein, utiliser normalement un indice puis réessayer le reçu : une réception bloquée ne doit pas être perdue.

La déduplication du stock est locale à l'installation et liée à l'UID/reçu. Elle n'est pas une garantie de stock synchronisé entre appareils ni après réinstallation. Deux comptes permettent de vérifier une paire et ses répétitions ; ils ne suffisent pas à éprouver les plafonds globaux de3 destinataires envoyés et5 réceptions quotidiennes.

## Étape7 — partage puis retrait du partage de position

Sur A, activer volontairement **Partager ma position sur la carte → Enregistrer mon profil**. Sur B, actualiser le cercle puis revenir à la carte : le niveau déclaré par A doit apparaître. C'est un instantané du progrès local, pas une progression vérifiée par le serveur. Retirer ensuite le partage et enregistrer ; après actualisation, B doit retrouver **Position privée** et ne plus afficher le marqueur de A. Les marqueurs en cache peuvent nécessiter un rafraîchissement réseau, notamment après changement de compte.

## Avant l'ouverture commerciale

Après qualification sociale : vérifier les unités AdMob du propriétaire, le consentement UMP et les appareils déclarés de test ; utiliser les annonces de test pendant nos essais. Puis qualifier PlayBilling avec les produits réellement configurés et des comptes testeurs de licence : achat accepté/refusé/en attente, répétition/récupération, remboursement, compte et crédit serveur corrects. Une piste interne seule ne rend pas les paiements gratuits. Activer le trafic et les ventes réels après ces contrôles et les réglages Play/AdMob ; les tirages premium ont leur propre qualification et restent fermés.

Sources officielles : [images Android et services Google](https://developer.android.com/studio/run/managing-avds), [annonces de test GMA NextGen](https://developers.google.com/admob/android/next-gen/test-ads), [tests PlayBilling et testeurs de licence](https://developer.android.com/google/play/billing/test).

## Reprendre les appareils plus tard

Les AVD restent locaux et leurs futurs comptes Google ne doivent pas être archivés dans Git. Pour les redémarrer après leur fermeture, utiliser l'émulateur Android déjà installé avec les noms `Luma-Social-A` et `Luma-Social-B`, sur les ports5582 et5584. Ne pas effacer leurs données pendant les tests de persistance. [Contrat serveur et limites](../server/SOCIAL.md) · [Revue du problème de niveaux](LEVEL_MANAGEMENT_REVIEW.md).
