# Qualification native du candidat D — profil neuf conservé

Les deux lots acceptés passent **254 assertions et 46 taps réels**. Le [reçu global](fresh-candidate-d.json) lie les rapports originaux, les captures et leurs SHA256. Le profil a été créé vide pour B, puis mis à jour vers **D, 1.24.0-rc2 / code 33**, en conservant les données. Ce lot ne prétend pas être une première installation de D sur un profil vide.

La première introduction et U1–U4 avaient été jouées sur B : rang 2, 140 XP et 2 copies de Nova. L’update B→D conserve la partie U5, ses deux usages consommés, sa Charge à zéro et son historique. U5 est ensuite gagné sur D. Le nouveau départ U6 capture Nova niveau 2 et capacité 2 ; les captures FR/EN montrent les deux usages disponibles avant activation. Deux GUIDE réels sont déclenchés, séparés par une vraie sortie manuelle : Charge **3 → 1 → 2 → 0**, usages **0 → 1 → 2**.

Après fermeture forcée par le runner et relance, le vrai bouton Reprendre retrouve U6, son historique, ses deux usages consommés et sa Charge à zéro. Les victoires U6–U8 passent ; U7 franchit le rang 3 et donne une copie à Nova. Les valeurs finales lues sont **280 XP, 3 copies de Nova, 620 LumaCoins et 3 étoiles sur U1–U8**. Sur la capture U7, les compteurs animés de récompense sont encore en progression ; les reçus persistants vérifiés contiennent bien +55 Coins et +35 XP.

| Phase | Verdict | Assertions | Taps |
| --- | --- | ---: | ---: |
| [Update et premier programme](candidate-d-upgrade-u6-fr-en/report.json) | Échec de scénario conservé : bouton Reprendre omis | 30 | 0 |
| [Suite avec Reprendre](candidate-d-u6-fr-en-resume/report.json) | Échec de scénario conservé : le helper de langue doit sortir par Pause | 88 | 14 |
| [U6 et langue via Pause](candidate-d-u6-explicit-pause/report.json) | PASS | 68 | 13 |
| [Reprise froide et U6–U8](candidate-d-cold-u6-through-u8/report.json) | PASS | 186 | 33 |

Les deux échecs sont des attentes inadaptées du scénario : le lancement ouvre l’accueil avec Reprendre, et Back depuis le puzzle ne remplace pas Pause → Quitter le niveau. Les programmes corrigés utilisent les contrôles réellement affichés. Les originaux et les résultats partiels sont conservés ; aucun fichier d’app n’a été modifié pour les faire passer.

Captures finales non retouchées, provenant du lot PASS sur D :

- [Puzzle FR](candidate-d-u6-explicit-pause/native/puzzle-fr.png) et [EN](candidate-d-u6-explicit-pause/native/puzzle-en.png).
- [Compagnon FR](candidate-d-u6-explicit-pause/native/companion-fr.png) et [EN](candidate-d-u6-explicit-pause/native/companion-en.png).
- [Rang 3 et copie de Nova](candidate-d-cold-u6-through-u8/native/u7-rank3-copy-nova-fr.png).

Les quatre premières images ont été inspectées visuellement : feuille opaque, prix en Charge, usages et action lisibles à 1080 × 2400, densité 420 et police 1,0. Cela ne mesure ni émotion ni compréhension humaine et n’étend pas le verdict aux autres formats. Le [contrôle logcat borné](candidate-d-runtime-logcat.json) ne trouve aucun des motifs de crash/décodage recherchés dans le PID réel final ; il ne prouve pas que tous les assets et états UI ont été visités.

L’[observer SDK](../../../../../scripts/v124-native/production-final/README.md) lit les préférences et envoie de vrais gestes. Les certificats U publics déterminent les touches : ce sont des contrôles d’acceptation, distincts des agents à perception limitée. Aucun clear, désinstallation du jeu, copie de profil ou injection de progression n’a lieu. Le package observer est retiré après chaque lot. Ce périmètre ne qualifie pas timers/recreate, équipement, décor, pubs ou commerce réel. U1–U8 sont gratuits en énergie par les règles natives actuelles.
