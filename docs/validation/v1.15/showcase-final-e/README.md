# Captures natives finales — profil Showcase distinct

Ces images proviennent du deuxième profil QA neuf, après **100 victoires réelles U1–U100** dans la production E `1.15.0-rc1` (code17). Elles ne représentent pas le profil du parcours principal U1–U1000. Les récompenses des 100 victoires ont gagné 5500 LumaCoins ; aucun solde, stock, niveau ou placement n’a été injecté.

**Réduire les animations est activé** : contrôle natif observé Off→On, puis On après relance froide. Les panneaux ont aussi été vérifiés en 320dp avec restauration explicite du format d’écran. Une charge Étincelle possédée a réellement été consommée, avec reçu durable et indice observé, sans modifier l’historique des pièces ni le budget.

L’aquarium 420, le piano 560 et la serre 640 ont été **réellement achetés pour 1620 LumaCoins**, puis placés par les contrôles de l’application dans le Jardin. Propriété, disposition exacte, solde et stock ont été contrôlés après relance froide.

Solde avant achats : 5680 ; après : 4060 LumaCoins. APK SHA-256 : `07b5bfc2632edc3d34f7583ac71c8343b52d7b926ed526aeb7a88599253df0f9`.

- [delivery-house-garden-fr](/Users/n/androidhit/docs/screenshots/v1.15/ui-bugs/ui-sdk-final-e-showcase/showcaseCold/delivery-house-garden-fr.png) : Maison/Jardin — profil QA neuf après 100 victoires ; aquarium, piano et serre réellement payés et placés.
- [delivery-adventure-fr](/Users/n/androidhit/docs/screenshots/v1.15/ui-bugs/ui-sdk-final-e-showcase/showcaseCold/delivery-adventure-fr.png) : Aventure — progression réellement gagnée jusqu’au niveau 101 sur ce profil QA distinct.
- [delivery-play-fr](/Users/n/androidhit/docs/screenshots/v1.15/ui-bugs/ui-sdk-final-e-showcase/showcaseCold/delivery-play-fr.png) : Jeu — vrai niveau 101 en français ; aucune pièce touchée pour cette capture.

Les PNG sont les originaux natifs non retouchés. Leurs empreintes et celles des rapports sont conservées dans summary.json. La preuve concerne un émulateur API36 arm64/16KB ; elle ne remplace pas un essai sur téléphone physique.

La serre placée automatiquement masque partiellement l’aquarium sur la vue finale. Les captures natives aquarium-placed et piano-placed montrent ces objets sans cette occlusion. Le deuxième AVD a été arrêté après les preuves ; son disque/profil reste conservé.
