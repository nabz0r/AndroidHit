# Qualification native — profil neuf 1.24, candidat B

Le lot fonctionnel passe **320 assertions et 54 gestes réels**, répartis en deux processus de test. Les originaux, gestes demandés, arbres d’accessibilité, états lus et PNG sont indexés dans [fresh-candidate-b.json](fresh-candidate-b.json).

Le jeu a été installé absent sur `Luma124-Fresh-QA` / `emulator-5636`, sans copie de profil ni injection. Les quatre premières victoires donnent 140 XP, le rang 2 et une copie à Nova. U5 capture son niveau 2 et ses 2 usages. Les deux activations GUIDE sont appliquées, avec une sortie manuelle entre elles : Charge 3 → 1 → 2 → 0. Une relance froide retrouve la même partie, ses 2 usages consommés, 0 Charge, 140 XP et 2 copies. Le second lot change la langue dans les vrais Réglages et conserve ces valeurs.

**Réserve graphique sur B** : la feuille compagnon et les boutons sont anormalement transparents ; les flèches et les textes du plateau passent au travers. La victoire U4 affiche clairement rang, copie et capacité suivante. Le défaut de peinture est transmis à root et au responsable UI ; la réussite fonctionnelle ne vaut pas acceptation de ce défaut. Les captures B doivent rester attribuées à B si un candidat corrigé est construit.

Sources observer : [harness](../../../../../scripts/v124-native/production/README.md). L’observer SDK ne fait que des gestes réels et des lectures. Les certificats publics dictent les pièces à toucher ; ce lot n’est ni une politique à perception limitée, ni un test humain. Il ne teste pas pubs, achats, équipement ou décoration. U1–U5 ne dépensent pas d’énergie par conception. Aucun fichier public ni code app modifié par ce lot.
