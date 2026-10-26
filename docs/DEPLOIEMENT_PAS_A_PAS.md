# Luma Drift : déployer le serveur et les pages, puis activer les revenus

Procédure vérifiée dans les documentations officielles le **26 octobre 2026**. Ce guide prépare les manipulations du propriétaire ; sa rédaction n'a créé aucune ressource cloud, publié aucune page et activé aucun paiement.

On utilise ton projet existant **`luma-drift-production`** : Firebase Hosting pour les pages, Cloud Run pour le serveur, Firestore pour le portefeuille. Aucun domaine à acheter pour commencer. L'adresse prévue du site, après déploiement, est `https://luma-drift-production.web.app` ; le serveur recevra sa propre adresse HTTPS attribuée par Cloud Run.

**Avancement de ce projet :** le propriétaire a confirmé le passage à Blaze et indiqué un budget de 10 €. Aucun plafond bloquant n'a été vérifié ; les budgets d'alerte ne coupent pas les dépenses. La configuration Firebase Web a été reçue et intégrée : application `1:369472826910:web:47e79b67e7a3abd0c3080f`. La prochaine étape accompagnée est la vérification des domaines autorisés Firebase Auth. Le serveur, le site hébergé, les coordonnées publiques et les tests réels restent à terminer. Les instructions ci-dessous conservent le parcours complet pour référence.

Fais une étape à la fois. Si une commande échoue, garde son message d'erreur et arrête le bloc avant de poursuivre. Ne relance pas aveuglément les commandes `create` : une ressource déjà créée se vérifie et se réutilise. Les commandes Cloud Run ci-dessous créent des ressources facturables. Les alertes de budget préviennent d'une dépense ; elles ne la plafonnent pas.

## 1. Activer la facturation et préparer les informations publiques

1. Ouvre [ton projet Firebase](https://console.firebase.google.com/project/luma-drift-production/overview).
2. Dans le choix de forfait, clique **Mettre à niveau → Blaze** et rattache ton compte de facturation Google Cloud. Les informations bancaires restent chez Google.
3. Dans [Google Cloud → Facturation](https://console.cloud.google.com/billing), sélectionne ce compte, puis **Budgets et alertes**. Tu peux commencer par un budget de surveillance de **10 € par mois**, avec des alertes à 50 %, 90 % et 100 %. Ce montant est un choix d'alerte, pas une estimation ni une limite de la facture. Vérifie la devise réelle du compte.
4. Choisis le **nom légal de l'éditeur** et une **adresse email publique de support** que tu consultes. Une adresse existante dédiée au jeu convient ; aucun domaine payant n'est nécessaire.

La facturation de ce projet et le profil marchand Google Play sont deux réglages distincts. Le premier fait fonctionner le serveur ; le second permet les ventes du jeu. [Forfaits Firebase](https://firebase.google.com/docs/projects/billing/firebase-pricing-plans).

## 2. Ajouter l'application Web Firebase pour la suppression de compte

L'application Android est déjà enregistrée. Les pages ont besoin de leur propre inscription **Web** dans le même projet.

1. Firebase → roue dentée → **Paramètres du projet → Général**.
2. Dans **Vos applications**, clique **Ajouter une application → Web `</>`**.
3. Nom : **Luma Drift Web**. Clique **Enregistrer l'application**. Si Hosting est proposé, tu peux l'activer ; la configuration du dépôt est déjà préparée. Ne remplace aucun fichier du site par un exemple Firebase.
4. Copie le bloc public `firebaseConfig` affiché. Nous utiliserons `apiKey`, `authDomain`, `projectId` et `appId`. L'`appId` doit contenir **`:web:`** ; l'identifiant Android ne convient pas ici.
5. Firebase → **Authentication → Paramètres → Domaines autorisés** : vérifie la présence de `luma-drift-production.web.app` et de `luma-drift-production.firebaseapp.com`. Ajoute-les s'ils manquent. Garde le fournisseur Google activé.
6. Firebase → **Hosting classique** → **Commencer**, si le site n'est pas encore initialisé. Le site voulu a pour identifiant `luma-drift-production`. Nous utilisons Hosting pour des fichiers statiques, sans App Hosting et sans réécriture de toutes les pages vers l'accueil.

**À me transmettre à cette étape :** le bloc `firebaseConfig`, le nom de l'éditeur et l'email de support. Ce sont des informations publiques destinées au site. Aucun secret OAuth ou fichier de clé de compte de service n'est nécessaire.

[Inscription Web](https://firebase.google.com/docs/web/setup), [connexion Google Web](https://firebase.google.com/docs/auth/web/google-signin), [Hosting](https://firebase.google.com/docs/hosting/quickstart).

## 3. Ouvrir Cloud Shell et récupérer le dépôt

1. Ouvre [Google Cloud avec le bon projet](https://console.cloud.google.com/home/dashboard?project=luma-drift-production).
2. Clique sur l'icône terminal **Activer Cloud Shell**, en haut à droite. Autorise son accès quand Google le demande. `gcloud` y est déjà installé ; tu n'as pas besoin d'installer Docker sur ton Mac.
3. Dans le terminal Cloud Shell, colle ce premier bloc, une seule fois :

```sh
git clone --depth 1 https://github.com/nabz0r/AndroidHit.git AndroidHit-deploiement
cd AndroidHit-deploiement
gcloud config set project luma-drift-production
gcloud auth list
gcloud billing projects describe luma-drift-production
node --version
```

Vérifie que le compte actif est le tien, que le projet est correct et que `billingEnabled` vaut `true`. Pour les commandes Firebase du guide, utilise Node 24, ou une version compatible avec la CLI épinglée. Si Node 24 manque dans Cloud Shell et que `nvm` est disponible :

```sh
nvm install 24
nvm use 24
```

La CLI Firebase nécessite sa propre connexion. Le code affiché pendant cette connexion reste dans le parcours Google et Cloud Shell ; ne le colle pas dans le chat.

```sh
npx --yes firebase-tools@15.34.1 login --no-localhost
npx --yes firebase-tools@15.34.1 projects:list
npx --yes firebase-tools@15.34.1 hosting:sites:list --project luma-drift-production
```

Vérifie que le projet et le site `luma-drift-production` apparaissent. Si le dossier existe déjà, réutilise-le au lieu de refaire `git clone`. Avant toute mise à jour du dépôt, conserve les valeurs publiques que tu aurais modifiées dans `public/site-config.js`.

## 4. Créer les identités du serveur et la base de données

À partir du dossier `AndroidHit-deploiement`, entre dans `server` :

```sh
cd server
```

Puis exécute **les deux blocs de la section 1 du [runbook serveur](../server/DEPLOYMENT.md#1-enable-apis-and-create-managed-identities)**. Ils sont prévus pour ce dossier et ce projet. Le premier initialise les variables `task_project`, `task_region`, `task_runtime`, `task_push` et `task_scheduler`, active les API et crée trois identités distinctes. Le second vérifie le compte de compilation et lui accorde ses droits.

| Identité | Rôle |
|---|---|
| `luma-wallet@luma-drift-production.iam.gserviceaccount.com` | Serveur : accès au portefeuille et vérification/suppression des utilisateurs Firebase |
| `luma-rtdn@luma-drift-production.iam.gserviceaccount.com` | Livraison des notifications Google Play |
| `luma-scheduler@luma-drift-production.iam.gserviceaccount.com` | Appel périodique de maintenance |

Aucune clé privée à créer ou à télécharger. Les identités Google gérées suffisent. Le compte qui construit le serveur a ses propres permissions : ne donne pas ses droits au portefeuille.

Dans un autre onglet, ouvre **Play Console → Utilisateurs et autorisations → Inviter un utilisateur** :

1. Email : `luma-wallet@luma-drift-production.iam.gserviceaccount.com`.
2. Limite son accès à **Luma Drift**.
3. Accorde les autorisations documentées pour **consulter les données financières/commandes** et **gérer les commandes et abonnements**. Aucun rôle d'administrateur global.
4. Enregistre l'invitation. Il n'y a pas de boîte Gmail à ouvrir pour ce compte de service.

Ces droits Play sont distincts des droits Google Cloud. [Configuration officielle](https://developers.google.com/android-publisher/getting_started).

Reviens au terminal pour vérifier la base existante :

```sh
gcloud firestore databases list --project "$task_project"
```

S'il n'existe pas de base **`(default)`**, crée-la avec la première commande de la **[section 2 du runbook](../server/DEPLOYMENT.md#2-create-the-database-and-close-all-direct-client-access)**. Nous proposons le mode Native, région `europe-west1` en Belgique. Si la base existe, conserve sa localisation et saute cette création. La localisation se décide avant la création.

Exécute ensuite la commande Firebase de cette même section pour déployer les règles et index depuis `server/firebase.json`. Les clients sont entièrement interdits dans Firestore ; seul le serveur passe par son identité IAM. Attends la fin de construction des index dans **Firestore Database → Index**. N'utilise pas le mode test ouvert.

## 5. Déployer le serveur et récupérer son adresse

Toujours dans `server`, exécute le bloc de la **[section 3 du runbook serveur](../server/DEPLOYMENT.md#3-builddeploy-cloud-run-then-record-its-canonical-https-url)**.

Il construit le Dockerfile du dépôt et déploie **`luma-wallet`**, avec zéro instance minimale et trois au maximum pour démarrer. Le déploiement peut prendre plusieurs minutes. Cloud Run doit accepter l'invocation réseau publique : le code du portefeuille vérifie ensuite lui-même les identités Firebase, et les routes internes leurs identités Google dédiées.

Le bloc récupère l'adresse réelle dans `task_service_url`, puis remplace l'adresse temporaire `https://bootstrap.invalid`. Il autorise l'origine du site `https://luma-drift-production.web.app`. Ne copie jamais l'adresse temporaire dans l'application ou les pages.

Vérifie ensuite :

```sh
printf '%s\n' "$task_service_url"
curl --fail-with-body "$task_service_url/healthz"
curl -i "$task_service_url/v1/wallet"
curl -i -X OPTIONS "$task_service_url/v1/account" \
  -H 'Origin: https://luma-drift-production.web.app' \
  -H 'Access-Control-Request-Method: DELETE' \
  -H 'Access-Control-Request-Headers: authorization,content-type'
```

Résultats attendus : `/healthz` répond `200` avec `{"status":"ok"}` ; le portefeuille sans connexion répond `401` ; la requête préparatoire du site répond `204` avec l'origine exacte autorisée. **Le contrôle de santé seul ne valide ni la base, ni les droits Play, ni un achat.**

**À me transmettre :** l'adresse HTTPS Cloud Run affichée. Elle est publique. Nous l'intégrerons au site et à la prochaine version Android de test. Si Cloud Shell se ferme, réinitialise les variables de la section 1 sans rejouer les créations ; récupère aussi `task_service_url` avec la commande `gcloud run services describe` de la section 3.

## 6. Activer les notifications d'achats et la maintenance

1. Exécute le bloc de la **[section 4 du runbook](../server/DEPLOYMENT.md#4-configure-authenticated-pubsub-rtdn)**. Il crée le sujet Pub/Sub, sa livraison authentifiée et une file pour les notifications en échec.
2. Play Console → Luma Drift → paramètres de monétisation → **Notifications développeur en temps réel**. Les libellés exacts peuvent varier selon la console.
3. Sujet complet : **`projects/luma-drift-production/topics/luma-rtdn`**.
4. Active les notifications nécessaires pour les produits ponctuels et les achats annulés/remboursés, puis envoie une notification de test. Vérifie son traitement avec un statut réussi dans Cloud Run.
5. Exécute le bloc de la **[section 5 du runbook](../server/DEPLOYMENT.md#5-schedule-maintenance-retries-audit-purge-and-alerts)**. La maintenance s'exécute toutes les cinq minutes ; le bloc permet aussi un lancement immédiat.
6. Dans **Cloud Scheduler**, vérifie le résultat effectif de la dernière exécution. Une création de tâche réussie ne prouve pas que son appel a fonctionné. Vérifie aussi l'absence de notifications bloquées et configure les alertes décrites dans le runbook.

Ces deux fonctions récupèrent notamment les traitements interrompus et les achats remboursés. Elles font partie du déploiement avant commercialisation.

`ALLOW_TEST_PURCHASES=true` accepte les achats de test en plus des achats ordinaires : **ce réglage ne transforme pas tous les paiements en paiements gratuits**. Les cartes de test dépendent du compte testeur de licence Google Play.

## 7. Configurer les pages et les héberger

Nous finalisons ensemble `public/site-config.js` avec le nom de l'éditeur, l'email, le bloc Firebase Web et l'adresse Cloud Run. Nous relisons aussi les textes **français et anglais** pour décrire les services réellement déployés et la conservation choisie. Le simple passage de `preparation` à `false` ne réécrit pas la politique.

Pour tester d'abord les pages configurées sans les annoncer comme parcours définitif, garde le bandeau de préparation et utilise une adresse de prévisualisation. Depuis la **racine du dépôt** :

```sh
cd ..
node --test public/tests/deletion-core.test.mjs
node public/tests/check-static.mjs
npx --yes firebase-tools@15.34.1 hosting:channel:deploy qualification \
  --expires 7d --project luma-drift-production --config firebase.json
```

La commande affiche une adresse de prévisualisation temporaire. **Cette adresse utilise le vrai Firebase et le vrai serveur : une suppression est effective dès la prévisualisation. Utilise un compte Google de qualification dédié avant le premier essai.** La prévisualisation est accessible aux personnes qui possèdent son URL. [Fonctionnement des prévisualisations Firebase](https://firebase.google.com/docs/hosting/test-preview-deploy).

Pour y tester Google et la suppression :

1. Dans les domaines autorisés Firebase Auth, ajoute **l'hôte exact**, sans `https://` ni chemin : par exemple `HOTE-PREVIEW.web.app`, à remplacer par le nom réellement affiché.
2. Cloud Run → **luma-wallet → Modifier et déployer une nouvelle révision → Variables et secrets** : modifie `ALLOWED_WEB_ORIGINS` pour conserver l'origine finale et ajouter l'origine de prévisualisation. Exemple de format : `https://luma-drift-production.web.app,https://HOTE-PREVIEW.web.app`, avec le vrai hôte de prévisualisation, **sans espace, sans chemin et sans barre finale**. Clique **Déployer**. Utiliser l'interface évite l'ambiguïté des virgules dans l'option `gcloud`.
3. Garde `firebase.authDomain` dans le site sur `luma-drift-production.firebaseapp.com`, tel qu'indiqué par la configuration Firebase Web. Ne le remplace pas par l'adresse de prévisualisation.
4. Après qualification, retire l'origine et le domaine temporaires en conservant les domaines et l'origine de production. Ne mets pas l'adresse de prévisualisation dans Play ou AdMob.

La [configuration Hosting racine](../firebase.json) sert déjà `public/` et exclut les tests ; elle conserve les pages séparées et les en-têtes documentés. Il n'est pas nécessaire de lancer `firebase init` ni d'écraser les fichiers HTML.

Après relecture et qualification, désactive explicitement le mode préparation, puis :

```sh
node public/tests/check-static.mjs --production
npx --yes firebase-tools@15.34.1 deploy --only hosting \
  --project luma-drift-production --config firebase.json
```

Vérifie depuis une fenêtre privée les quatre adresses finales :

| Utilisation | Adresse prévue après publication |
|---|---|
| Site développeur | `https://luma-drift-production.web.app/` |
| Confidentialité | `https://luma-drift-production.web.app/privacy/index.html` |
| Suppression du compte | `https://luma-drift-production.web.app/delete-account/index.html` |
| Autorisation publicitaire | `https://luma-drift-production.web.app/app-ads.txt` |

Teste aussi la connexion et la suppression réelles sur le domaine final, avec un compte de qualification dédié. La suppression est effective : n'utilise pas ton compte joueur principal. Le contrôle automatique ne constitue pas une validation juridique ou une preuve d'identité Google fonctionnelle.

## 8. Publier un test interne Play et créer les produits

1. Transmets-moi l'adresse Cloud Run. Je prépare une **nouvelle version** Android avec cette adresse et les annonces de test conservées. Le livrable actuel `1.4.0-rc1` ne connaît pas cette adresse ; le déploiement du serveur ne suffit donc pas à activer ses achats.
2. Play Console → Luma Drift → **Tests internes** : publie l'AAB de cette nouvelle version. Un envoi initial du candidat existant peut préparer l'application ; il faudra la version reliée au serveur pour qualifier les achats.
3. Dans **Intégrité de l'application / Signature d'application Play**, copie les SHA-1 et SHA-256 du **certificat de signature de l'application** et ajoute-les à l'application Android Firebase. Ce certificat Play peut différer de la clé qui signe l'APK livré directement.
4. Complète le profil de paiement marchand et crée les produits ponctuels suivants. Pour chacun : option d'achat **Buy**, prix/pays réels, quantité multiple désactivée, produit et option activés.

| Identifiant exact | Crédit |
|---|---:|
| `luma_gold_500` | 500 gold |
| `luma_gold_1400` | 1 400 gold |
| `luma_gold_3600` | 3 600 gold |

5. Ajoute ton compte Android aux **testeurs du test interne**, puis séparément aux **testeurs de licence** dans les paramètres Play Console.
6. Accepte le lien d'invitation au test et installe la version depuis Google Play. Dans la fenêtre d'achat, vérifie la mention de test et choisis un **moyen de paiement de test**. Un simple testeur interne peut être réellement débité s'il n'est pas testeur de licence.
7. Qualifie achat accepté, refusé, en attente, annulation, double rappel, redémarrage, deux appareils, déconnexion/changement de compte, restauration des cosmétiques, remboursement et suppression de compte. Le serveur doit créditer chaque achat une seule fois et consommer le produit après livraison durable.

Dans la version actuelle, le **gold acheté** sert aux compagnons et thèmes permanents du portefeuille en ligne. Les pièces gagnées, pouvoirs et continuations gardent leur circuit local ; ne présente pas encore le gold acheté comme un moyen d'acheter ces consommables.

[Produits et prérequis](https://developer.android.com/google/play/billing/getting-ready), [tests d'achat](https://developer.android.com/google/play/billing/test), [sécurité](https://developer.android.com/google/play/billing/security). La disponibilité publique reste soumise aux étapes et examens que Play affiche pour ton compte, dont le test fermé si applicable.

## 9. Faire valider les annonces AdMob

1. Play Console → coordonnées de la fiche : renseigne le **site développeur final** et l'email de support. Déclare aussi la confidentialité, la suppression de compte, les annonces et les données réellement utilisées.
2. Dans AdMob, rattache Luma Drift à sa fiche Play quand elle est publiquement accessible. Une fiche uniquement disponible en test privé ne termine pas l'examen de préparation AdMob.
3. Ouvre le fichier `/app-ads.txt` publié. Compare avec l'extrait personnalisé AdMob ; le dépôt contient déjà :

```text
google.com, pub-6914185503996225, DIRECT, f08c47fec0942fa0
```

4. AdMob → application → **Valider l'application / Rechercher des mises à jour**, puis vérifie l'état du fichier et de l'examen de préparation. La présence du fichier et l'approbation de diffusion sont deux étapes.
5. **Confidentialité et messages** : publie le message pour les réglementations européennes, associé à Luma Drift et à la politique publiée ; configure les autres messages applicables aux pays distribués. Vérifie les langues, le refus et le retour aux choix de confidentialité.
6. Nous qualifions acceptation, refus, retrait du choix, hors-ligne, indisponibilité d'annonce et attribution unique des récompenses sur un appareil de test.
7. Après ces résultats et l'approbation AdMob, je prépare une nouvelle version avec les vrais blocs activés. Pour les vérifications du développeur, les appareils restent déclarés comme appareils de test. Ne clique pas sur tes propres annonces réelles.

Les identifiants AdMob sont déjà intégrés ; il n'est pas nécessaire de les renvoyer. Le serveur d'achats ne commande pas l'approbation AdMob. [Vérification de l'application](https://support.google.com/admob/answer/14538460?hl=fr), [préparation](https://support.google.com/admob/answer/10564477?hl=en), [message européen](https://support.google.com/admob/answer/10113207?hl=en), [consentement Android](https://developers.google.com/admob/android/privacy).

## Ce qu'il faut me renvoyer, dans l'ordre

1. **Déjà reçus** : confirmation Blaze et bloc public Firebase Web. Il reste le nom de l'éditeur, l'email de support et la confirmation des domaines autorisés.
2. **Après Cloud Run** : l'adresse HTTPS réelle du serveur et le résultat des trois vérifications de la section 5.
3. **Après Play** : les empreintes du certificat de signature Play, l'état des trois produits et le lien du test interne.
4. **Après AdMob** : l'état de validation `app-ads.txt`, l'état de préparation et la confirmation du message de consentement publié.

En cas d'erreur, envoie le texte correspondant en retirant tout jeton ou donnée personnelle. Les mots de passe, codes de connexion, clés privées, documents d'identité et coordonnées bancaires restent hors du dépôt et du chat.
