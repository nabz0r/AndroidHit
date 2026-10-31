# Reçus Cloud avant déploiement — autorisation absente

Les 31 fichiers originaux ont été récupérés par 28 fragments visibles dans le terminal, puis vérifiés par SHA avant extraction bornée. L’archive contient aussi leur index original, soit 32 membres. Aucune capture Cloud Shell ni fichier de credential n’a été récupéré.

Le staging initial 39c0 puis le restage C2b 6611 passent ; les deux APK D sont réutilisés au second passage. Le premier finaliseur échoue pendant l’acquisition du jeton, avant tout appel au déployeur Hosting. Aucun upload ni release n’est établi. La variante final2 reste non exécutée.

- [Contrôle de collecte](collection.json) et [archive originale](inbound.tgz)
- [Index original](originals/complete-pre-deploy-export-index.json)
- [Stage initial](originals/receipts/operator.json) et [restage C2b](originals/receipts-c2b/restage-operator.json)
- [Échec d’authentification](originals/receipts-c2b/final-operator.json) et [diagnostic borné](originals/auth-diagnostic-after-refresh.json)
