# Diagnostic SDK Lab D — 31 octobre 2026

Ces nouvelles preuves sont entièrement hors dépôt. Elles ne modifient pas le bilan natif figé de568 assertions/118gestes/39PNG. Voir `summary.json`, les reçus de compilation des trois forks temporaires et les copies exactes du programme/source dans chaque phase.

Même APK Lab D rc2/code33 SHA1929ee6bc8644ee936bfe7db917fd02e3000598f6b83d053af51a8c5d5cd238b, même AVD existant5638 sans reset/copie, production absente. Profil initial730Coins/450Goldtest/10énergies.

L’observation avant la garde de package identifie l’aide Android « Viewing full screen ». Un seul clic « Got it » est ensuite autorisé et exécuté, uniquement après correspondance unique des trois IDs/libellés/rectangles visibles. Les gardes ordinaires d’entrée restent inchangées.

Un véritable visionnage SDK « Test Ad » termine avec « Reward granted ». Le solde passe réellement730→765Coins ; Goldtest450 et énergie10 restent inchangés. Le reçu ad pending est vide et le marqueur de transaction est true. Un processus froid conserve765 et ne recrédite rien. Aucun callback n’est injecté.

Le second affichage borné pour Retour s’arrête avant tout geste : le composant repris public est `com.androidhit.lumadrift.sandbox/com.google.android.libraries.ads.mobile.sdk.common.AdActivity`, différent de l’ancien composant `com.google.android.gms.ads.AdActivity` demandé par la garde. La correction d’autorisation arrive après la fin de cet affichage ; aucun autre n’est lancé. Zéro Retour Android et zéro clic SDK Close envoyé : fermeture anticipée et comportement Retour restent non qualifiés.

Trois phases PASS :142 assertions/22gestes/10PNG. Trois arrêts conservés séparément :109 assertions/20gestes/7PNG. L’arrêt initial avant affichage correspond au premier Charger qui applique seulement les choix de confidentialité ; le suivant observe volontairement la fenêtre SystemUI avant de s’arrêter à la garde ; le dernier conserve le refus de composant avant Retour.

L’option publicitaire est désactivée par l’UI. L’observer est désinstallé, l’AVD5638 est arrêté normalement et son app/userdata sont conservées. Aucun achat réel, aucune publicité de production, aucune écriture app/public/preuveancienne et aucun autre appareil touché.

Fichiers principaux :

- `observation.json` : fenêtre Android précisément identifiée.
- `acknowledge-identified-and-observe/native/actual-sdk-controls-after-exact-ack.png` : vraie annonce après le seul clic système.
- `single-full-sdk-view/native/full-view-nine-seconds.png` et `full-view-forty-five-seconds.png` : Reward granted et Close.
- `single-full-sdk-view/native/after.json` : crédit réel et reçu durable.
- `one-standard-back-in-identified-sdk/native/sdk-back-identity.json` : composant public réellement observé, aucun Back envoyé.
- `final-cold-optout/native/after.json` et `diagnostic-final-home-fr-765-coins.png` : persistance,765Coins/450Goldtest/10énergies.
- `cleanup.json` : arrêt normal5638.

Le manifest inventorie les preuves et sources utiles ; les intermédiaires SDK de compilation sont exclus. Les APK observers sont conservés localement pour identité/reproduction et peuvent être omis d’un commit documentaire si leur omission est explicite. Aucune donnée de signature privée n’est copiée.
