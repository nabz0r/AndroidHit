package com.androidhit.lumadrift.data;
import java.util.*;
/** Lobby and quality-release copy, with explicit six-language parity. */
public final class UiStrings {
 private static final Map<String,String[]> TEXT = new HashMap<>();
 static {
  TEXT.put("welcome",new String[]{"Welcome, %s","Bienvenue, %s","Willkommen, %s","Hola, %s","Olá, %s","Benvenuto, %s"});
  TEXT.put("guest",new String[]{"Welcome, explorer","Bienvenue, explorateur","Willkommen, Entdecker","Hola, explorador","Olá, explorador","Benvenuto, esploratore"});
  TEXT.put("guest_note",new String[]{"Guest · tap to sign in","Invité · toucher pour se connecter","Gast · zum Anmelden tippen","Invitado · toca para entrar","Convidado · toca para entrar","Ospite · tocca per accedere"});
  TEXT.put("signed",new String[]{"Google connected · local progress","Google connecté · progression locale","Google verbunden · lokaler Fortschritt","Google conectado · progreso local","Google ligado · progresso local","Google connesso · progressi locali"});
  TEXT.put("local",new String[]{"Saved on this device","Sauvegardé sur cet appareil","Auf diesem Gerät gespeichert","Guardado en este dispositivo","Guardado neste dispositivo","Salvato su questo dispositivo"});
  TEXT.put("equipment",new String[]{"Equipment","Équipement","Ausrüstung","Equipo","Equipamento","Equipaggiamento"});
  TEXT.put("owned",new String[]{"Owned","Possédé","Im Besitz","En propiedad","Adquirido","Posseduto"});
  TEXT.put("get",new String[]{"To unlock","À obtenir","Freischalten","Por conseguir","Por obter","Da ottenere"});
  TEXT.put("all",new String[]{"All","Tout","Alle","Todo","Tudo","Tutto"});
  TEXT.put("owned_count",new String[]{"%d / %d owned","%d / %d possédés","%d / %d im Besitz","%d / %d obtenidos","%d / %d adquiridos","%d / %d posseduti"});
  TEXT.put("earned",new String[]{"Earned coins","Pièces gagnées","Erspielte Münzen","Monedas ganadas","Moedas ganhas","Monete guadagnate"});
  TEXT.put("pack",new String[]{"%d charges · %d LumaCoins","%d charges · %d LumaCoins","%d Ladungen · %d LumaCoins","%d cargas · %d LumaCoins","%d cargas · %d LumaCoins","%d cariche · %d LumaCoins"});
  TEXT.put("power_confirm",new String[]{"Use one charge","Utiliser une charge","Eine Ladung verwenden","Usar una carga","Usar uma carga","Usa una carica"});
  TEXT.put("power_cost",new String[]{"One activation uses exactly 1 charge. Stock: %d.","Une activation utilise exactement 1 charge. Stock : %d.","Eine Aktivierung verbraucht genau 1 Ladung. Vorrat: %d.","Una activación usa exactamente 1 carga. Reservas: %d.","Uma ativação usa exatamente 1 carga. Stock: %d.","Ogni attivazione usa esattamente 1 carica. Scorte: %d."});
  TEXT.put("active",new String[]{"Active","Actif","Aktiv","Activo","Ativo","Attivo"});
  TEXT.put("flow_running",new String[]{"Flow in progress","Flux en cours","Fluss läuft","Flujo en curso","Fluxo em curso","Flusso in corso"});
  TEXT.put("power_busy",new String[]{"Let the current effect finish. No charge used.","Laisse cet effet se terminer. Aucune charge utilisée.","Warte auf das laufende Ereignis. Keine Ladung verbraucht.","Espera a que termine el efecto. Sin gasto.","Espera pelo fim do efeito. Sem gasto.","Attendi la fine dell’effetto. Nessuna carica usata."});
  TEXT.put("budget",new String[]{"%d exits + %d spare actions","%d sorties + %d coups de marge","%d Ausgänge + %d Zusatzversuche","%d salidas + %d intentos extra","%d saídas + %d ações extra","%d uscite + %d azioni extra"});
  TEXT.put("zone",new String[]{"Zone %d","Zone %d","Zone %d","Zona %d","Zona %d","Zona %d"});
  TEXT.put("map_count",new String[]{"1,000 levels · 240 Remix","1 000 niveaux · 240 Remix","1.000 Level · 240 Remix","1.000 niveles · 240 Remix","1.000 níveis · 240 Remix","1.000 livelli · 240 Remix"});
  TEXT.put("journey_progress",new String[]{"%d / %d levels cleared","%d / %d niveaux terminés","%d / %d Level geschafft","%d / %d niveles superados","%d / %d níveis concluídos","%d / %d livelli completati"});
  TEXT.put("lobby_event",new String[]{"Daily & weekly rewards","Récompenses du jour et de la semaine","Tages- und Wochenbelohnungen","Premios diarios y semanales","Prémios diários e semanais","Premi giornalieri e settimanali"});
  TEXT.put("lobby_gear",new String[]{"Looks & useful gear","Styles et équipements utiles","Looks und nützliche Ausrüstung","Estilo y equipo útil","Estilo e equipamento útil","Stile e oggetti utili"});
  TEXT.put("lobby_shop",new String[]{"Powers & equipment","Pouvoirs et équipements","Kräfte und Ausrüstung","Poderes y equipo","Poderes e equipamento","Poteri e oggetti"});
  TEXT.put("wardrobe",new String[]{"Your loadout","Ton équipement","Deine Ausrüstung","Tu equipamiento","O teu equipamento","Il tuo equipaggiamento"});
  TEXT.put("gear_rules",new String[]{"3 slots · fixed benefits · earned through play","3 emplacements · effets fixes · obtenus en jouant","3 Plätze · feste Boni · erspielbar","3 ranuras · beneficios fijos · se gana jugando","3 espaços · efeitos fixos · obtidos a jogar","3 spazi · bonus fissi · ottenibili giocando"});
  TEXT.put("gear_protect",new String[]{"Charm: %d protected block","Charme : %d erreur protégée","Talisman: %d geschützter Fehlversuch","Amuleto: %d bloqueo protegido","Amuleto: %d bloqueio protegido","Amuleto: %d blocco protetto"});
  TEXT.put("gear_new",new String[]{"Equipment available","Équipement disponible","Ausrüstung verfügbar","Equipo disponible","Equipamento disponível","Oggetti disponibili"});
  TEXT.put("cosmetic_note",new String[]{"Companions and themes change your look. Equipment has the stated benefits.","Les compagnons et thèmes changent le style. Les équipements ont les effets indiqués.","Begleiter und Themen ändern den Look. Ausrüstung gibt die angegebenen Boni.","Compañeros y temas cambian el estilo. El equipo ofrece los beneficios indicados.","Companheiros e temas mudam o estilo. Equipamento dá os efeitos indicados.","Compagni e temi cambiano lo stile. Gli oggetti danno i bonus indicati."});
  TEXT.put("claim_gear",new String[]{"Claim equipment","Obtenir l’équipement","Ausrüstung abholen","Recoger equipo","Receber equipamento","Ritira oggetto"});
  TEXT.put("gear_hint",new String[]{"Play events to unlock permanent equipment","Joue les événements pour des équipements permanents","Spiele Events für dauerhafte Ausrüstung","Juega eventos para equipo permanente","Joga eventos para equipamento permanente","Gioca eventi per oggetti permanenti"});
  TEXT.put("ready",new String[]{"Ready to claim","À récupérer","Abholbereit","Listo para recoger","Pronto a receber","Da ritirare"});
  TEXT.put("loot_progress",new String[]{"Next free reward: %d / %d boards","Prochain cadeau : %d / %d plateaux","Nächster Bonus: %d / %d Bretter","Próximo regalo: %d / %d tableros","Próxima oferta: %d / %d tabuleiros","Prossimo regalo: %d / %d schemi"});
  TEXT.put("stock",new String[]{"Stock: %d","Stock : %d","Vorrat: %d","Reservas: %d","Stock: %d","Scorte: %d"});
  TEXT.put("coin_price",new String[]{"%d earned coins","%d pièces gagnées","%d erspielte Münzen","%d monedas ganadas","%d moedas ganhas","%d monete guadagnate"});
  TEXT.put("view",new String[]{"View","Voir","Ansehen","Ver","Ver","Vedi"});
  TEXT.put("no_owned",new String[]{"No items in this filter yet.","Aucun objet dans ce filtre.","Noch keine Objekte in diesem Filter.","Aún no hay objetos en este filtro.","Ainda sem itens neste filtro.","Nessun oggetto in questo filtro."});
  TEXT.put("sealed",new String[]{"Sealed passages","Passages scellés","Versiegelte Wege","Pasajes sellados","Passagens seladas","Passaggi sigillati"});
  TEXT.put("sealed_note",new String[]{"6 puzzle prototypes · no rewards or powers","6 puzzles prototypes · sans récompense ni pouvoir","6 Puzzle-Prototypen · ohne Belohnung oder Kräfte","6 puzles de prueba · sin premios ni poderes","6 puzzles de teste · sem prémios nem poderes","6 puzzle prototipo · senza premi o poteri"});
  TEXT.put("sealed_rule",new String[]{"Release a key piece to dissolve every matching wall. Undo brings the wall back. Gates still matter: plan the order.","Libère une pièce-clé pour dissoudre ses murs. Annuler rétablit les murs. Les portes comptent toujours : prépare ton ordre.","Löse ein Schlüsselteil, um passende Mauern aufzulösen. Rückgängig stellt sie wieder her. Beachte auch die Tore.","Libera una pieza llave para disolver sus muros. Deshacer los restaura. Las puertas siguen importando: planea el orden.","Liberta uma peça-chave para dissolver os seus muros. Anular repõe os muros. As portas também contam: planeia a ordem.","Libera una tessera chiave per dissolvere i suoi muri. Annullare ripristina i muri. Pianifica anche le porte."});
  TEXT.put("sealed_key",new String[]{"Key %d","Clé %d","Schlüssel %d","Llave %d","Chave %d","Chiave %d"});
  TEXT.put("sealed_wall",new String[]{"Wall · release key %d","Mur · libère la clé %d","Mauer · löse Schlüssel %d","Muro · libera la llave %d","Muro · liberta a chave %d","Muro · libera la chiave %d"});
  TEXT.put("sealed_open",new String[]{"Passage open","Passage ouvert","Weg offen","Paso abierto","Passagem aberta","Passaggio aperto"});
  TEXT.put("sealed_unlimited",new String[]{"Explore freely · unlimited actions","Explore librement · coups illimités","Frei erkunden · unbegrenzte Züge","Explora libremente · sin límite de movimientos","Explora livremente · ações ilimitadas","Esplora liberamente · mosse illimitate"});
  TEXT.put("commerce_short",new String[]{"Gold & ads","Gold et pubs","Gold und Werbung","Oro y anuncios","Ouro e anúncios","Oro e annunci"});
 }
 public static String get(String lang,String key,Object...args){String[] row=TEXT.get(key);if(row==null)throw new IllegalArgumentException("Missing UI copy: "+key);int i="fr".equals(lang)?1:"de".equals(lang)?2:"es".equals(lang)?3:"pt".equals(lang)?4:"it".equals(lang)?5:0;return String.format(Locale.ROOT,row[i],args);}
 private UiStrings(){}
}
