# Qualification amis et cadeaux — deux Android virtuels

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
