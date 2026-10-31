package com.androidhit.lumadrift.data;
import java.util.*;
/** Six-language 1.7 presentation. Currency brand names are intentionally shared. */
public final class SagaStrings {
 private static final Map<String,String[]> M=new LinkedHashMap<>();
 static {
 a("wardrobe","Dressing room","Dressing","Ankleidezimmer","Vestidor","Guarda-roupa","Guardaroba");
 a("friends","Friends & gifts","Amis et cadeaux","Freunde & Geschenke","Amigos y regalos","Amigos e presentes","Amici e regali");
 a("rewards","Daily rewards","Récompenses du jour","Tägliche Belohnungen","Premios diarios","Recompensas diárias","Premi giornalieri");
 a("daily_quests","Daily quests","Quêtes quotidiennes","Tagesaufgaben","Misiones diarias","Missões diárias","Missioni giornaliere");
 a("weekly_quests","Weekly quests","Quêtes hebdomadaires","Wochenaufgaben","Misiones semanales","Missões semanais","Missioni settimanali");
 a("earned","Earn by playing","Gagnés en jouant","Im Spiel verdient","Ganadas jugando","Ganhas jogando","Ottenute giocando");
 a("premium","Account currency · real purchases","Monnaie du compte · achats réels","Kontowährung · echte Käufe","Moneda de cuenta · compras reales","Moeda da conta · compras reais","Valuta account · acquisti reali");
 a("separate","Two balances. Never converted automatically.","Deux soldes. Aucune conversion automatique.","Zwei Guthaben. Keine automatische Umwandlung.","Dos saldos. Sin conversión automática.","Dois saldos. Sem conversão automática.","Due saldi. Nessuna conversione automatica.");
 a("premium_closed","Real checkout is not open in this candidate.","Les achats réels restent fermés dans cette version.","Echte Käufe sind hier noch nicht verfügbar.","Las compras reales aún no están abiertas.","As compras reais ainda não estão abertas.","Gli acquisti reali non sono ancora aperti.");
 a("current_level","My level","Mon niveau","Mein Level","Mi nivel","Meu nível","Il mio livello");
 a("jump","Explore a level","Explorer un niveau","Level ansehen","Explorar nivel","Explorar nível","Esplora livello");
 a("jump_note","Enter 1–1000. Locked boards can be viewed on the map.","Entre 1–1000. Les niveaux verrouillés restent visibles sur la carte.","1–1000 eingeben. Gesperrte Level bleiben auf der Karte sichtbar.","Introduce 1–1000. Los niveles bloqueados se ven en el mapa.","Insere 1–1000. Níveis bloqueados aparecem no mapa.","Inserisci 1–1000. I livelli bloccati sono visibili sulla mappa.");
 a("dead_end","This path is closed","Ce chemin est fermé","Dieser Weg ist blockiert","Este camino está cerrado","Este caminho está fechado","Questo percorso è bloccato");
 a("dead_end_note","A gate choice closed the solution. Extra moves alone cannot repair it. Restart free or undo to an earlier position.","Un choix de porte a fermé la solution. Des coups en plus ne suffisent pas. Recommence gratuitement ou annule pour revenir en arrière.","Eine Torwahl blockiert die Lösung. Mehr Züge allein helfen nicht. Kostenlos neu starten oder zurückgehen.","Una puerta bloqueó la solución. Más movimientos no bastan. Reinicia gratis o deshaz jugadas.","Uma porta bloqueou a solução. Mais jogadas não bastam. Recomeça grátis ou desfaz jogadas.","Una porta ha bloccato la soluzione. Altre mosse non bastano. Ricomincia gratis o annulla.");
 a("not_enough_moves","Not enough moves to clear this attempt","Il manque des coups pour finir cette tentative","Zu wenige Züge für diesen Versuch","Faltan movimientos en este intento","Faltam jogadas nesta tentativa","Mosse insufficienti per questo tentativo");
 a("path_warning","A gate closed the solution · restart or undo","Une porte bloque la solution · annule ou recommence","Tor blockiert die Lösung · zurück oder Neustart","Una puerta bloquea la solución · deshaz o reinicia","Uma porta bloqueia a solução · desfaz ou recomeça","Una porta blocca la soluzione · annulla o ricomincia");
 a("hint_ready","Next safe move","Prochain coup sûr","Nächster sicherer Zug","Siguiente jugada segura","Próxima jogada segura","Prossima mossa sicura");
 a("hint_pulse","Follow the glowing piece","Suis la pièce qui clignote","Folge dem leuchtenden Teil","Sigue la pieza brillante","Segue a peça brilhante","Segui la tessera luminosa");
 a("cosmetic_only","Appearance only · no puzzle advantage","Cosmétique uniquement · aucun avantage","Nur Aussehen · kein Rätselvorteil","Solo cosmético · sin ventaja","Apenas visual · sem vantagem","Solo estetica · nessun vantaggio");
 a("hat","Hat","Chapeau","Hut","Sombrero","Chapéu","Cappello");
 a("dress","Outfit","Tenue","Outfit","Traje","Roupa","Abito");
 a("shoes","Shoes","Chaussures","Schuhe","Zapatos","Sapatos","Scarpe");
 a("gloves","Gloves","Gants","Handschuhe","Guantes","Luvas","Guanti");
 a("power_total","%d power charges","%d charges de pouvoirs","%d Kraftladungen","%d cargas de poderes","%d cargas de poderes","%d cariche poteri");
 a("cosmetics","Looks","Styles","Looks","Estilos","Estilos","Stili");
 a("loot","Star capsule","Capsule étoilée","Sternkapsel","Cápsula estelar","Cápsula estelar","Capsula stellare");
 a("loot_full_odds","Each common item 7.5%%; each rare 3.75%%; each epic 1.25%%. No resale or cash value.","Chaque commun 7,5%% ; chaque rare 3,75%% ; chaque épique 1,25%%. Sans revente ni valeur monétaire.","Je häufigem Look7,5%%; seltenem3,75%%; epischem1,25%%. Kein Verkauf oder Geldwert.","Cada común7,5%%; raro3,75%%; épico1,25%%. Sin reventa ni valor en dinero.","Cada comum7,5%%; raro3,75%%; épico1,25%%. Sem revenda ou valor monetário.","Ogni comune7,5%%; raro3,75%%; epico1,25%%. Senza rivendita o valore monetario.");
 a("open_capsule","Open · 150 LumaCoins","Ouvrir · 150 LumaCoins","Öffnen ·150 LumaCoins","Abrir ·150 LumaCoins","Abrir ·150 LumaCoins","Apri ·150 LumaCoins");
 a("duplicate","Already owned · +%d LumaCoins","Déjà possédé · +%d LumaCoins","Schon vorhanden · +%d LumaCoins","Ya obtenido · +%d LumaCoins","Já adquirido · +%d LumaCoins","Già posseduto · +%d LumaCoins");
 a("login_day","Day %d","Jour %d","Tag %d","Día %d","Dia %d","Giorno %d");
 a("login_note","Seven visits, at your pace. Missing a day never resets the cycle.","Sept visites, à ton rythme. Une absence ne remet jamais le cycle à zéro.","Sieben Besuche in deinem Tempo. Pausen setzen den Zyklus nicht zurück.","Siete visitas a tu ritmo. Faltar un día no reinicia el ciclo.","Sete visitas ao teu ritmo. Faltar um dia não reinicia o ciclo.","Sette visite al tuo ritmo. Un giorno di pausa non azzera il ciclo.");
 a("claimed_today","Collected today","Récupéré aujourd’hui","Heute abgeholt","Recogido hoy","Recebido hoje","Ritirato oggi");
 a("no_skin","No item equipped","Aucun objet équipé","Nichts angelegt","Sin objeto equipado","Sem item equipado","Nessun oggetto equipaggiato");
 a("premium_capsule","Premium capsule · preparing","Capsule premium · en préparation","Premiumkapsel · in Vorbereitung","Cápsula premium · preparando","Cápsula premium · em preparação","Capsula premium · in preparazione");
 a("friends_note","Share your map position by choice. Limited gifts contain a hint charge.","Partage ta position si tu le souhaites. Les cadeaux limités contiennent un indice.","Teile deinen Kartenstand freiwillig. Begrenzte Geschenke enthalten einen Hinweis.","Comparte tu posición si quieres. Los regalos limitados contienen una pista.","Partilha a posição se quiseres. Presentes limitados contêm uma dica.","Condividi la posizione se vuoi. I regali limitati contengono un indizio.");
 String[][] names={{"First Light","Première lueur","Erstes Licht","Primera luz","Primeira luz","Prima luce"},{"Mint Meadows","Prairies menthe","Minzwiesen","Prados menta","Prados menta","Prati menta"},{"Peach Coast","Côte pêche","Pfirsichküste","Costa melocotón","Costa pêssego","Costa pesca"},{"Cloud Gardens","Jardins nuage","Wolkengärten","Jardines nube","Jardins nuvem","Giardini nuvola"},{"Crystal Lagoon","Lagon cristal","Kristalllagune","Laguna cristal","Lagoa cristal","Laguna cristallo"},{"Star Orchard","Verger étoilé","Sternenobstgarten","Huerto estelar","Pomar estelar","Frutteto stellare"},{"Moon Festival","Fête lunaire","Mondfest","Festival lunar","Festival lunar","Festa lunare"},{"Pumpkin Grove","Bois citrouille","Kürbishain","Bosque calabaza","Bosque abóbora","Bosco zucca"},{"Rose Aurora","Aurore rose","Rosenaurora","Aurora rosa","Aurora rosa","Aurora rosa"},{"Dream Summit","Sommet des rêves","Traumgipfel","Cima de sueños","Cume dos sonhos","Cima dei sogni"}};
 for(int i=0;i<names.length;i++)a("biome_"+i,names[i]);
 }
 private static void a(String k,String...v){if(v.length!=6||M.put(k,v)!=null)throw new IllegalStateException(k);}
 public static String get(String language,String key,Object...args){String[]row=M.get(key);if(row==null)throw new IllegalArgumentException(key);String lang=Strings.normalize(language);int i=Arrays.asList("en","fr","de","es","pt","it").indexOf(lang);String value=row[Math.max(0,i)];return String.format(Locale.forLanguageTag(lang),value,args);}
 public static Set<String> keys(){return Collections.unmodifiableSet(M.keySet());}
 private SagaStrings(){}
}
