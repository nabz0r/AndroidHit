# Exécution, budget et argent

Les montants et seuils de décision ci-dessous sont **des hypothèses internes**, sauf les frais et obligations Store explicitement sourcés. Aucun revenu, coût d’acquisition ou taux de rétention du futur jeu n’est encore mesuré.

## Calendrier de travail proposé

Hypothèses : une personne expérimentée, 25–35 h/semaine, Mac et téléphone Android déjà disponibles, assistance IA existante. Une première expérience Android ou un recrutement difficile de testeurs rallongera ce calendrier.

| Période | Livrable | Condition de passage |
|---|---|---|
| Semaine 1 | Prototype installé, 15 niveaux, règle d’extraction et une porte | Compréhension observée auprès de 5–10 personnes ; tester aussi sans la porte |
| Semaines 2–3 | Progression, solveur, packs sélectionnés, sauvegarde, accessibilité | Niveaux validés, commandes claires, reprise fiable |
| Semaine 4 | Version de test fermée, fiche provisoire, suivi des cohortes | Retours réels, qualité sur plusieurs appareils |
| Semaines 5–8 | Corrections, monétisation limitée si justifiée, candidature publication | Conditions Play et droits traités ; aucun calendrier de validation garanti |

Recruter les testeurs tôt et faire chevaucher le test fermé avec les corrections dès qu’une version stable est prête. **La présence dans le Store et les recettes publicitaires ne sont pas acquises à la fin de la semaine 4.**

## Budget plafonné

| Poste | Enveloppe proposée | Condition |
|---|---:|---|
| Compte Play Console | 25 USD, une fois | Seulement si aucun compte adapté n’existe |
| Moteur et serveur de jeu | 0 € de redevance moteur ; pas de serveur de jeu | Godot et fonctionnement local dans le périmètre retenu |
| Graphismes, sons, polices | 0–40 € | Formes et sons propres ; provenance documentée |
| Hébergement statique / domaine | 0–30 € | Politique de confidentialité et, plus tard, app-ads.txt ; offre gratuite possible à vérifier |
| Acquisition expérimentale | 0–200 € | Seulement après un premier signal de rétention |
| Marge | 0–30 € | Petites dépenses identifiées |
| **Total additionnel proposé** | **25 USD + 0–300 €** | Pas de conversion de change implicite |

Ce plafond ne comprend ni le temps de travail, ni le matériel manquant, ni l’abonnement IA, ni les impôts, ni un conseil juridique ou une prestation externe. Si un poste indispensable dépasse l’enveloppe, il faut réviser le périmètre et le budget ; le plafond n’est pas une garantie de coût réel. [Frais officiels Play](https://support.google.com/googleplay/android-developer/answer/6112435), [licence Godot](https://godotengine.org/license/).

## Conditions de lancement à intégrer

État des règles consultées le 26 octobre 2026 ; les revérifier avant soumission.

| Point | Conséquence concrète | Source |
|---|---|---|
| Nouveau compte personnel après le 13/11/2023 | Au moins 12 testeurs inscrits continuellement 14 jours en test fermé, puis demande d’accès production ; accès non automatique | [Tests Play](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en) |
| Identité et appareil | Prévoir les vérifications du compte et, selon le cas, de l’appareil Android | [Compte Play](https://support.google.com/googleplay/android-developer/answer/6112435) |
| API cible | Nouveau jeu mobile : Android 16 / API 36 ou supérieur depuis le 31/08/2026 | [API cible](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en) |
| Format | Préparer un Android App Bundle et la signature de publication | [AAB](https://developer.android.com/guide/app-bundle) |
| Confidentialité | Politique accessible publiquement et dans l’app ; déclarations Data safety conformes à l’app et aux SDK | [Données utilisateur](https://support.google.com/googleplay/android-developer/answer/10144311), [Data safety](https://support.google.com/googleplay/android-developer/answer/10787469) |
| Publicité européenne | Gérer les choix de confidentialité et la CMP certifiée selon les exigences applicables ; UMP est une option | [AdMob](https://support.google.com/admob/answer/13554116), [documentation UMP](https://developers.google.com/admob/android/privacy/gdpr?hl=en) |
| Activation des revenus AdMob | app-ads.txt, vérification de l’app et revue de préparation ; pas de diffusion complète garantie dès l’intégration | [Vérification AdMob](https://support.google.com/admob/answer/14538460?hl=en-GB) |
| Achat numérique | Respecter la politique de paiement et le programme applicable avant d’implémenter un achat | [Paiements Play](https://support.google.com/googleplay/android-developer/answer/10281818) |

Déclarer honnêtement le public cible et la classification ; si le produit vise aussi les enfants, examiner les règles correspondantes avant d’activer les SDK. Ne pas supposer qu’un jeu familial est exempt d’exigences. [Public cible](https://support.google.com/googleplay/android-developer/answer/9867159)

## Monétisation proposée

1. Première validation sans publicité : comprendre si les joueurs reviennent pour le jeu lui-même.
2. Puis publicité récompensée explicitement choisie pour un indice. Annuler et recommencer restent gratuits. [Rewarded AdMob](https://developers.google.com/admob/android/rewarded)
3. Si les résultats tiennent, essayer des interstitiels uniquement entre les niveaux, après les premières parties. Point de départ interne : aucun pendant les cinq premières minutes, puis au maximum un toutes les trois minutes ; à ajuster selon les données. [Placement des publicités](https://support.google.com/googleplay/android-developer/answer/12271244)
4. Évaluer un achat unique autour de 2,99 € pour supprimer les interstitiels ; prix à tester et à localiser. Expliquer séparément le fonctionnement des récompensées volontaires. Aucun abonnement au départ.

Ces choix restent conditionnés à la [compatibilité des licences](02-droits-licences.md), aux politiques applicables et à une intégration validée. Ne pas demander de cliquer sur une publicité ; employer les annonces de test pendant le développement.

## Combien de joueurs pour gagner de l’argent ?

Définitions : **DAU** = joueurs uniques actifs par jour ; **eCPM** = recettes pour mille impressions diffusées. La définition officielle ne fournit pas un taux garanti pour notre jeu. [AdMob — eCPM](https://support.google.com/admob/answer/15337570)

**Recettes publicitaires mensuelles = DAU moyen × impressions réellement diffusées par DAU et par jour × 30 × eCPM / 1 000.**

Hypothèse fixe : deux impressions réellement diffusées par joueur actif quotidien. Les trois eCPM sont des valeurs de calcul choisies, **pas des moyennes de marché ni une fourchette promise**.

| DAU moyen | eCPM supposé 1 € | eCPM supposé 4 € | eCPM supposé 8 € |
|---:|---:|---:|---:|
| 100 | 6 €/mois | 24 €/mois | 48 €/mois |
| 1 000 | 60 €/mois | 240 €/mois | 480 €/mois |
| 5 000 | 300 €/mois | 1 200 €/mois | 2 400 €/mois |
| 10 000 | 600 €/mois | 2 400 €/mois | 4 800 €/mois |

Les impressions réellement diffusées incluent déjà l’effet du jeu hors ligne, du consentement, du remplissage et du choix de regarder une annonce. Elles peuvent être bien inférieures à deux. L’eCPM varie avec les pays, formats et périodes ; un nombre d’installations mondial ne donne pas une recette mondiale uniforme.

Ces montants sont des recettes éditeur avant charges et fiscalité, hors achats intégrés. Ce ne sont ni un salaire ni un bénéfice net. Ne pas soustraire mécaniquement la commission Play sur les recettes publicitaires ; traiter séparément les frais applicables aux achats numériques.

Dans le scénario à 4 € et deux impressions quotidiennes, **1 000 €/mois demanderaient environ 4 167 DAU moyens**, et 3 000 €/mois environ 12 500. Un stock d’installations cumulées ne remplace pas ces joueurs actifs chaque jour.

### Pourquoi acheter des installations peut perdre de l’argent

Exemple entièrement hypothétique : huit journées actives sur la vie d’un joueur × deux impressions × 4 €/1 000 = **0,064 € de recettes publicitaires par installation**. Avec un coût d’acquisition de 0,30 €, l’opération perdrait 0,236 € par joueur avant les autres coûts.

Un test à 200 € donnerait environ 667 installations avec ce CPI, mais seulement 133 si le CPI atteignait 1,50 €. Aucun de ces CPI n’est un prix observé. Un petit budget peut donc être insuffisant pour mesurer proprement rétention et valeur vie. Ne pas augmenter les achats tant que la valeur vie prudente ne dépasse pas le coût avec une marge d’incertitude.

## Distribution : une partie du travail de chaque semaine

Produire six vidéos verticales montrant une vraie partie : une explication, une erreur d’ordre et une solution, avec deux variantes de présentation. Montrer exactement le jeu disponible ; éviter les faux écrans publicitaires.

Commencer par des retours volontaires auprès de communautés de puzzles et de proches, puis publier sur ses propres canaux. Séparer testeurs proches et nouveaux joueurs dans l’analyse. Préparer une fiche FR/EN ; tester ensuite, selon la demande, portugais brésilien ou espagnol. La facilité de localisation est un choix de produit, pas la preuve d’un marché rentable.

Suivre le parcours **vue → visite de fiche → installation → première partie → retour J1/J7**. Les vues ou les partages seuls ne prouvent pas que le jeu attire et conserve des joueurs. Ne pas automatiser de sollicitations ni acheter d’avis.

## Mesures et décisions

Objectifs choisis pour ce projet, non benchmarks sectoriels :

| Étape | Mesure | Décision proposée |
|---|---|---|
| Premiers essais | Compréhension sans explication orale ; premier niveau terminé | Si moins de 8 testeurs sur 10 y arrivent, simplifier règle ou présentation |
| Cohortes réelles | Tutoriel terminé, abandons, niveaux joués | Viser au moins 80 % de tutoriels terminés, puis regarder où les abandons se concentrent |
| Retour | D1 et D7, calculés par cohorte | Viser D1 ≥ 30 % et D7 ≥ 10 % avant d’acheter davantage de volume |
| Arrêt | Après deux révisions substantielles et environ 300 nouveaux joueurs par cohorte arrivée à J7 | Si D1 < 20 % et D7 < 5 %, arrêter cette boucle ou la changer nettement |
| Monétisation | Recettes et rétention par cohorte exposée | Conserver les pubs seulement si leur effet est acceptable et documenté |
| Acquisition | CPI et valeur vie prudente par marché | Ne pas augmenter les dépenses si la marge n’est pas établie |

D1 = part des premiers joueurs d’une cohorte revenus le jour suivant ; D7 = part revenue le septième jour, avec une convention de date et fuseau fixe. Ne pas mélanger cette définition avec « revenu au moins une fois en sept jours ». Attendre la maturité des cohortes ; trois cents joueurs donnent une direction, pas une certitude.

Enregistrer les événements minimaux nécessaires au test, par exemple première ouverture, fin du tutoriel, niveau commencé/terminé, annulation, indice et session. Prévoir un identifiant de test pseudonyme, l’information des testeurs, une durée de conservation et les déclarations nécessaires. Aucun outil analytique ne doit être présenté comme « sans données » sans examen de son comportement et de ses SDK.

## Validation technique avant toute livraison du jeu

Le dossier actuel est documentaire. Pour le futur jeu : vérifier le solveur et les règles, produire un paquet signé, installer sur émulateur et téléphones réels, jouer, contrôler la reprise, les gestes, les petits écrans et les publicités de test. Conserver version, empreinte et captures. Un build réussi ne démontrera ni l’expérience sur le téléphone de l’utilisateur ni la rentabilité.

**Prochaine étape concrète : prototype jouable sans publicité, limité à une semaine de travail, puis décision fondée sur les essais.**
