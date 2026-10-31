# Opérateur C2b, avant authentification

Le checkout Cloud original `39c0` a été avancé vers C2b `66119799d6fbe5254a8d37d50db76afe8daffbdc`. Les reçus originaux restent dans `receipts/` ; le nouveau staging est dans `receipts-c2b/`. Les deux APK ont été réutilisés, sans téléchargement de bases ni reconstruction. Les 69 inputs gelés et les arbres app/public sont identiques.

Le finaliseur accepte explicitement cette chaîne de révisions et ce workspace d’origine. Il refuse une nouvelle tentative si les sorties finales existent déjà. Il ne clone ni ne restage. Le jeton réel reste à acquérir, uniquement après le GO explicite du coordinateur sur la CI complète. Il n’est jamais placé dans les arguments, l’environnement ou un fichier.

Les dix tests complets et quatre tests indépendants sont hors réseau : leurs APK et jeton sont factices. Ils vérifient les garde-fous et le passage par stdin ; ils ne prouvent aucun appel Hosting ni rendu public. Le contrôle réel `--check-only` Cloud est distinct.

- [Hashes et préparation](preparation.json)
- [Wrapper de restage](cloud-restage.py)
- [Finaliseur](cloud-finalize.py)
- [Tests offline](test-finalizer-offline.py) et [log original](offline-tests.log)
- [Revue indépendante restage](restage-independent-review.json) et [finaliseur](finalizer-independent-review.json)
