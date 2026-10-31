# Relecture UX native — 31 octobre 2026

**Deux pertes de contexte de navigation sont confirmées. Les achats observés persistent ; les deux parcours d'automatisation qui les révèlent restent en échec.** Cette revue est secondaire à [l'analyse du cœur de jeu](CORE_LOOP_REVIEW.md). Elle relit les gestes, les arbres d'accessibilité, les préférences observées et les captures de root, sans nouvelle interaction native.

## Périmètre et identité

Production `com.androidhit.lumadrift`, 1.23.0-rc2/code 31, APK SHA-256 `429a443c9228787c7b854d36a40f0bcf7d52fdc8a3601214cd27b5cfae6945fb`. Observer SHA-256 `bcf90611b2a5d3c0a89ab603718491ab46ea3d2437a7af44cf88fe6135fd08dd`. Les rapports datent de 23:29 à 23:35 UTC le 30 octobre, donc du 31 octobre en Europe/Luxembourg. Chaque rapport hôte lie les fichiers de preuve par SHA-256.

| Parcours original | Verdict original | Assertions / gestes | Portée utile ici |
| --- | --- | --- | --- |
| [house-inspect](native-production/house-inspect/report.json) | PASS | 8 / 2 | Maison et accès à la décoration |
| [decor-purchases](native-production/decor-purchases/report.json) | PASS | 23 / 20 | Deux revêtements, une peluche, placement |
| [equipment](native-production/equipment/report.json) | FAIL | 14 / 4 | Retour au mauvais onglet après fermeture d'aperçu |
| [equipment-retry](native-production/equipment-retry/report.json) | FAIL | 27 / 17 | Achat et équipement vérifiés, puis retour inattendu dans l'aperçu |
| [equipment-durable](native-production/equipment-durable/report.json) | PASS | 16 / 9 | Propriété et équipement relus à froid ; changement sans nouveau paiement |

Les assertions PASS à l'intérieur d'un parcours FAIL établissent leurs observations locales ; elles ne changent pas son verdict global.

## Observations de navigation

### 1. Fermer l'aperçu d'un équipement ramène à Tenue

Séquence réelle : « Look et tenues » → « Équipement » → « Feuille lanterne » → « Fermer ». L'assertion du slot de tête confirme qu'une simple prévisualisation n'a rien équipé. Après « Fermer », le prochain accès à « Feuille lanterne » échoue : l'écran expose les articles `wardrobe_item_hat_dew`, `hat_moon`, etc., donc l'onglet **Tenue**. Le retry doit réappuyer sur « Équipement » pour retrouver la liste voulue.

Preuves : [actions et assertions](native-production/equipment/native/report.json), [aperçu](native-production/equipment/native/head-preview.png), [écran après fermeture](native-production/equipment/native/failure.png), [AX après fermeture](native-production/equipment/native/failure-accessibility.json), [erreur originale](native-production/equipment/native/failure.txt). Le [retry](native-production/equipment-retry/native/report.json) vérifie explicitement `wardrobe_item_hat_dew` avant de rouvrir « Équipement ».

**Jugement UX :** la fermeture devrait retrouver l'onglet, le slot et la position consultés. La perte de contexte impose une navigation supplémentaire. Aucune perte d'objet ou de monnaie n'est démontrée par cet échec.

### 2. Après équiper, Retour retrouve l'aperçu précédent

Dans le retry, l'achat de « Vœu de cuivre » débite exactement 450 LumaCoins : 527 → 77. La propriété `avatar_owned_v1_copper_wish=EARNED_COINS` puis le slot `avatar_slot_v1_CHARM=copper_wish` sont constatés. « Équiper » affiche la garde-robe avec le toast « Appliqué : Vœu de cuivre ». L'action réelle `wardrobe_back` revient ensuite à l'écran **Aperçu**, état « Équipé », avec « Choisir comme objectif » et « Fermer ». Le script attendait la navigation racine et échoue à cet endroit.

Preuves : [actions et assertions](native-production/equipment-retry/native/report.json), [prix](native-production/equipment-retry/native/charm-priced-preview.png), [achat](native-production/equipment-retry/native/charm-owned-before-equip.png), [équipement appliqué](native-production/equipment-retry/native/charm-equipped.png), [écran après Retour](native-production/equipment-retry/native/failure.png), [AX final](native-production/equipment-retry/native/failure-accessibility.json), [erreur originale](native-production/equipment-retry/native/failure.txt).

**Jugement UX :** aperçu → application → garde-robe → Retour → ancien aperçu forme une boucle peu prévisible. L'écran de prix se retrouve devant l'utilisateur alors qu'il vient de terminer l'action. Le retour devrait retrouver le point d'entrée du parcours, ou l'application devrait rester dans l'aperçu jusqu'à une fermeture explicite.

Le [contrôle à froid](native-production/equipment-durable/native/report.json) confirme les trois slots attendus, la propriété du charme et les 77 pièces restantes. Passer au charme déjà possédé « Boussole rosée », puis revenir à « Vœu de cuivre », ne débite rien et ne propose pas de payer à nouveau 450 pièces. La friction observée concerne donc la navigation, pas une transaction perdue.

## Prix, objets et décoration : observations

- Le prix du charme indique **450 LumaCoins** et le porte-monnaie indique 527 avant l'achat, 77 après. LumaGold affiche un tiret ; aucune valeur Gold disponible ou transaction Gold n'est établie par ces images.
- Les murs Crépuscule et le sol Rose débitent chacun 180 pièces, la peluche 120 : 480 au total, de 1 007 à 527. Les préférences observées confirment propriété et revêtements sélectionnés. Le bouton d'achat des murs disparaît après acquisition. La peluche passe par un brouillon, puis un geste « Placer » avant sa présence dans la pièce.
- Les captures « Aperçu » gardent la phrase « Aperçu visuel · aucun objet accordé » même lorsque l'objet est déjà possédé ou équipé. Le statut de l'objet et l'absence d'octroi pendant la seule prévisualisation sont deux informations différentes ; leur juxtaposition peut être trompeuse.
- « Feuille lanterne » annonce un bonus de 1 pièce à une première victoire admissible. « Vœu de cuivre » annonce « Apparence uniquement ». Les deux utilisent presque la même grande mise en scène de compagnon. Un objet équipé ne signifie donc pas nécessairement un pouvoir de puzzle.
- L'énergie reste à 11/11 pendant ces opérations de maison et d'achat. Ces opérations ne sont pas des départs de puzzles payants : aucune dépense d'énergie n'est attendue ici.

Preuves de décoration : [assertions et gestes](native-production/decor-purchases/native/report.json), [offre des murs](native-production/decor-purchases/native/walls-priced-offer.png), [offre du sol](native-production/decor-purchases/native/floor-priced-offer.png), [offre de la peluche](native-production/decor-purchases/native/plush-priced-offer.png), [brouillon de placement](native-production/decor-purchases/native/plush-draft.png), [pièce après placement](native-production/decor-purchases/native/room-customized.png). Les sommes sont vérifiées par les deltas natifs ; aucune estimation visuelle de portefeuille n'est nécessaire.

## Jugement graphique, distinct des faits

La maison et le compagnon ont une présence visible ; placer une peluche produit une modification concrète, donc une récompense personnelle compréhensible. La grille et les verbes « Tourner », « Ranger », « Placer » rendent les actions de décoration plus lisibles que les allers-retours d'équipement.

L'écran d'offre du papier peint est dominé par un grand échantillon plat, tandis que la maison est une scène très ornée. Ce décalage de présentation rend l'effet final moins facile à anticiper. Les captures seules ne prouvent pas que le revêtement est mal appliqué ; la sélection sauvegardée est confirmée.

Dans l'aperçu d'équipement, la très grande présentation du compagnon et deux rangées de portefeuille occupent beaucoup d'espace avant l'effet et l'action. Le prix est lisible, mais le bouton limité à « 450 LumaCoins » décrit moins bien l'action qu'« Acheter pour 450 LumaCoins ». Le toast d'application recouvre temporairement la zone basse de la capture ; une image unique ne prouve pas une obstruction permanente.

Ces jugements décrivent ce que montrent les captures. Ils ne mesurent ni préférence esthétique, ni compréhension humaine, ni attachement, ni conversion. Le défaut prioritaire reste la cohérence de la [boucle de jeu](CORE_LOOP_REVIEW.md), avant une densification des explications ou des menus.

## Proposition locale de navigation, non implémentée

Conserver le contexte du catalogue à l'ouverture d'un aperçu ; après une application réussie, fermer cet aperçu vers ce même contexte et le retirer de la pile Retour. Le test minimal doit rejouer les deux séquences ci-dessus, vérifier onglet/slot/position et propriété, puis confirmer à froid que l'achat n'a été crédité et débité qu'une fois. Aucun correctif app, Ads/GOLD ou nouveau test natif n'a été réalisé dans cette relecture.
