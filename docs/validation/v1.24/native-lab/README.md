# Lab natif 1.24 — candidat D rc2, code33

Qualification SDK figée le31octobre2026 sur le seul APK Lab SHA-256 `1929ee6bc8644ee936bfe7db917fd02e3000598f6b83d053af51a8c5d5cd238b`. [L’index](index.json) relie les reçus originaux et leurs empreintes. Les522 entrées app du candidat D ont été revérifiées à la clôture. Aucun fichier app/public n’a changé pendant cette qualification.

Les11 phases terminées PASS totalisent568 assertions,118 actions tactiles et39 PNG originaux. Les3 vérifications supplémentaires de cycle de vie utilisent les API publiques Android Home/foreground et Activity.recreate ; elles ne sont pas ajoutées au compteur de gestes. Les4 phases arrêtées restent conservées et ne sont pas comptées comme des phases PASS. Un PNG SDK en lecture seule, pris après un arrêt, reste également hors des39 PNG des phases PASS.

## Supplément SDK ultérieur, distinct de l’index figé

Le [nouveau diagnostic SDK](sdk-diagnosis/summary.json) ajoute trois phases PASS,
142 assertions/22 gestes/10 PNG, sans modifier l’index ni les résultats ci-dessous.
Un vrai Test Ad accorde +35 Coins, 730→765 ; un processus froid conserve 765,
Gold test 450 et énergie 10, sans second crédit. Android Retour et fermeture
anticipée restent non qualifiés : zéro action Back et zéro clic SDK Close ont
été envoyés. Trois arrêts du diagnostic restent séparés. Opt-out et
[arrêt normal de 5638](sdk-diagnosis/cleanup.json) sont enregistrés.

Le README original du diagnostic conserve la mention « hors dépôt », qui décrit
sa provenance avant collecte. Les preuves ont ensuite été copiées ici sans
changer leurs octets : le [reçu de collecte](sdk-diagnosis/collection-receipt.json)
vérifie 823 fichiers locaux, dont neuf APK observers conservés localement et
ignorés dans Git. Ce nombre ne signifie pas que 823 fichiers sont suivis ou
publiés. Les sections et le tableau suivants décrivent le bilan antérieur de
568 assertions ; son index et ses anciens rapports restent immuables.

La source C2b `66119799d6fbe5254a8d37d50db76afe8daffbdc` a ses trois jobs CI
verts, mais le site reste en 1.23 : le [passage Hosting](../publication/operator-preparation/c2b/auth-retry/handoff.json)
est AUTH_REQUIRED, faute de compte Google actif dans Cloud Shell, avant tout
appel Firebase, clonage de version, upload ou release. Ces preuves natives ne
signifient donc pas que le site 1.24 est livré.

## Profil conservé —5634

[Résumé retained](retained-d-summary.json) :330 assertions,53 gestes,22 PNG. Upgrade E→B→D, avec15 fichiers de préférences Lab identiques juste après B→D et avant première Activity. L’APK de production E et ses5 fichiers de préférences sont restés inchangés. Le refus Gold insuffisant hérité de B devient terminal REFUSED sous D en conservant les mêmes identifiants, puis reste stable après retour froid FR/EN.

Les véritables écrans home, compagnon, pause et confirmation de redémarrage sont opaques ; la capsule XP reste séparée de Réglages. À1080×1300 et police1.3, les contrôles de48dp restent accessibles par le vrai défilement et la configuration initiale est restaurée. Un GUIDE réel coûte2 Charge et consomme1 usage de l’ancien essai. Background avec feuille ouverte conserve exactement le temps ; la recréation Activity conserve plateau/historique/budget/run/effets. Le retour au plateau fait à nouveau avancer le temps. Voir [le reçu visuel D](../ui-pulse/native-retest-d.json).

Les confirmations réelles de redémarrage consomment l’énergie, l’annulation conserve l’essai, et Gold0 refuse la recharge sans débit/crédit. [L’observation de régénération naturelle](real-time-energy-regeneration.json) relie le même profil B→D :4→6 énergies, deux échéances de10minutes réelles, ancre+1200000ms. Elle ne constitue pas un nouveau test20minutes entièrement sous D. [Arrêt normal5634](cleanup-retained.json), profil conservé.

## Profil réellement neuf —5638

[Résumé fresh](fresh-d-summary.json) :238 assertions,65 gestes,17 PNG dans5 phases PASS. [Création officielle avant boot/install](device-creation-fresh/creation.json), API36/arm64/16K, sans copie ni remise à zéro. Le package production est absent avant/après toutes les phases. La première installation D depuis absence et les préférences de jeu initialement vides sont observées dans la phase d07, dont l’arrêt ultérieur reste conservé.

L’introduction puis U1–U10 sont gagnés par de vrais gestes suivant des certificats de niveaux publics ; aucun solveur/callback ou progrès n’est injecté dans l’app. Au premier U11 payant : énergie10→9 ; reprendre le même plateau reste9→9. Nova atteint niveau2/copies3 et le nouvel essai affiche capacité2. Les9 confirmations suivantes de redémarrage réduisent9→0.

L’annulation de l’offre conserve500 Gold test et0 énergie. La confirmation dépense réellement50 Gold test :500→450 et0→10 énergies. Un seul marqueur spent, un seul used et un seul reçu énergie sont présents ; stock0 et outbox vide. Les730 LumaCoins restent inchangés. Un processus froid final conserve730 Coins,450 Gold test et10 énergies. Les Gold test dédiés à la recharge sont distincts des Coins du portefeuille simulé.

## SDK publicitaire : périmètre et limite

Le refus mineur est exercé dans l’UI. Après choix adulte et activation explicite, le vrai SDK affiche « Publisher Test Ads » UMP. Le vrai bouton « Do not consent » est utilisé, puis le chargement atteint le texte visible « Test ad ready ». L’offre observée est bien « +35 LumaCoins ».

Deux clics réels sur Voir une publicité sont suivis d’une fenêtre active annoncée `com.android.systemui` au contrôle suivant, après1s puis6s. Le harness conserve sa frontière : aucune entrée hors package Lab, aucun Back anticipé et aucune récompense artificielle. Ces deux phases sont arrêtées. Aucun crash Java AndroidRuntime n’y est observé ; cela ne permet pas d’identifier la fenêtre système ni de revendiquer un affichage complet, une fermeture anticipée réussie ou un crédit publicitaire gagné. Le PNG supplémentaire pris après la fin du test montre le launcher, pas la fenêtre précédente.

L’option publicitaire est ensuite désactivée par le véritable contrôle UI et le solde froid est toujours730 Coins. Aucune publicité de production ni aucun achat réel n’a été activé. [Arrêt normal5638](cleanup-fresh.json), observer absent, package Lab et userdata conservés. Le premier contrôle de déconnexion à2s était trop court ; la disparition ADB ultérieure et la fin normale du processus émulateur sont vérifiées.

## Phases originales

| Phase | Statut | Assertions | Gestes | PNG |
|---|---|---:|---:|---:|
| [d01 upgrade/refus](d01-upgrade-refusal/report.json) | PASS |42|8|4|
| [d02 contexte modal](d02-modal-context/report.json) | PASS |55|5|4|
| [d03 petite hauteur/grande police](d03-short-large-context/report.json) | PASS |47|5|5|
| [d04 GUIDE/cycle de vie](d04-guide-lifecycle/report.json) | PASS |65|5|3|
| [d05 restart/énergie/refus](d05-restart-drain-refusal/report.json) | PASS |90|24|4|
| [d06 froid/refus](d06-cold-final-refusal/report.json) | PASS |31|6|2|
| [d07 première installation/intro](d07-fresh-intro-earned-u11/report.json) | STOP harness U10 |329|97|5|
| [d07b suite U10/U11](d07b-fresh-earned-u10-u11/report.json) | PASS |75|17|5|
| [d08 recharge](d08-fresh-refill-cancel-buy/report.json) | PASS |80|26|5|
| [d09 mineur/UMP](d09-ads-gates-setup/report.json) | PASS |34|11|3|
| [d10 ordre UMP](d10-ads-consent-load/report.json) | STOP cible masquée |11|2|1|
| [d10b consentement/chargement](d10b-ads-consent-load-observed/report.json) | PASS |22|4|2|
| [d11 affichage](d11-sdk-early-close/report.json) | STOP frontière système |23|4|0|
| [d11b affichage délai6s](d11b-sdk-early-close-settled/report.json) | STOP frontière système |23|4|0|
| [d12 opt-out/persistance](d12-final-opt-out-persistence/report.json) | PASS |27|7|2|

L’arrêt d07 survient sur la cible U10 cachée par la minimap fixe. L’ajustement du seul observer vérifie que le niveau demandé est réellement le niveau courant déjà sélectionné et que Jouer est visible ; l’ouverture vérifie ensuite l’identité exacte du plateau avant les gestes. L’arrêt d10 correspond à UMP déjà affiché avant Charger ; d10b suit l’ordre des contrôles réellement observés. Les sources compilées du harness et les programmes sont copiés dans chaque phase, sans réécriture des échecs.

Les preuves B restent historiques, notamment [le rejet visuel de translucidité](candidate-b-review.json). Ces tests scriptés sur émulateur n’établissent pas compréhension humaine, agrément, rétention, performance sur téléphone physique, paiement réel, publicité de production, validation Play ou statut du site.
