package com.androidhit.lumadrift.data;

import java.util.Locale;

/** Clear role labels: account XP, copy evolution, earned Coins and account Gold are distinct. */
public final class Menu121Strings {
    private Menu121Strings(){}
    public static String get(String language,String key,Object...args){
        String lang=language==null?"en":language.toLowerCase(Locale.ROOT);
        int i=lang.startsWith("fr")?1:lang.startsWith("de")?2:lang.startsWith("es")?3:lang.startsWith("pt")?4:lang.startsWith("it")?5:0;
        String[] row;
        switch(key){
            case "player_level":row=new String[]{"PLAYER LV.%d","JOUEUR NV.%d","SPIELER LV.%d","JUGADOR NV.%d","JOGADOR NV.%d","GIOCATORE LV.%d"};break;
            case "xp_progress":row=new String[]{"%d / %d XP","%d / %d XP","%d / %d XP","%d / %d XP","%d / %d XP","%d / %d XP"};break;
            case "xp_rule":row=new String[]{"First clears earn account XP. Replays do not.","Les premières réussites donnent de l’XP au joueur. Pas les replays.","Erste Lösungen geben Spieler-XP. Wiederholungen nicht.","Las primeras victorias dan XP al jugador. Las repeticiones no.","As primeiras vitórias dão XP ao jogador. As repetições não.","Le prime vittorie danno XP al giocatore. Le ripetizioni no."};break;
            case "avatar_role":row=new String[]{"YOUR AVATAR · YOUR STYLE","TON AVATAR · TON STYLE","DEIN AVATAR · DEIN STIL","TU AVATAR · TU ESTILO","O TEU AVATAR · O TEU ESTILO","IL TUO AVATAR · IL TUO STILE"};break;
            case "companion_level":row=new String[]{"LV.%d","NV.%d","LV.%d","NV.%d","NV.%d","LV.%d"};break;
            case "style_action":row=new String[]{"Style & outfits","Look et tenues","Stil und Outfits","Estilo y trajes","Visual e roupas","Stile e abiti"};break;
            case "evolution_action":row=new String[]{"Evolution & skills","Évolution et talents","Entwicklung & Talente","Evolución y talentos","Evolução e talentos","Evoluzione e talenti"};break;
            case "evolve_by_copies":row=new String[]{"Copies evolve your companion’s form.","Les copies font évoluer sa forme.","Kopien entwickeln seine Gestalt.","Las copias evolucionan su forma.","As cópias evoluem a sua forma.","Le copie evolvono la sua forma."};break;
            case "copies_progress":row=new String[]{"%d / %d copies","%d / %d copies","%d / %d Kopien","%d / %d copias","%d / %d cópias","%d / %d copie"};break;
            case "max_evolution":row=new String[]{"Final form unlocked","Forme finale débloquée","Letzte Form freigeschaltet","Forma final desbloqueada","Forma final desbloqueada","Forma finale sbloccata"};break;
            case "team_role":row=new String[]{"YOUR PUZZLE TEAM","TON ÉQUIPE DE PUZZLE","DEIN PUZZLE-TEAM","TU EQUIPO DE PUZLES","A TUA EQUIPA DE PUZZLE","LA TUA SQUADRA PUZZLE"};break;
            case "team_purpose":row=new String[]{"Their skills help you solve.","Leurs talents t’aident à résoudre.","Ihre Talente helfen beim Lösen.","Sus talentos te ayudan a resolver.","Os seus talentos ajudam a resolver.","I loro talenti aiutano a risolvere."};break;
            case "battle_role":row=new String[]{"Tap your team to choose its puzzle skills.","Touche l’équipe pour choisir ses talents de puzzle.","Tippe auf dein Team, um Puzzle-Talente zu wählen.","Toca el equipo para elegir sus talentos de puzle.","Toca na equipa para escolher os seus talentos.","Tocca la squadra per scegliere i talenti puzzle."};break;
            case "choose":row=new String[]{"Choose","Choisir","Wählen","Elegir","Escolher","Scegli"};break;
            case "kitchen":row=new String[]{"Kitchen","Cuisine","Küche","Cocina","Cozinha","Cucina"};break;
            case "coins_role":row=new String[]{"LumaCoins · earned in play","LumaCoins · gagnés en jouant","LumaCoins · beim Spielen verdient","LumaCoins · ganadas jugando","LumaCoins · ganhas a jogar","LumaCoins · guadagnate giocando"};break;
            case "coins_loop":row=new String[]{"Solve → earn Coins → collect & style","Résous → gagne des Coins → collectionne","Lösen → Coins verdienen → sammeln","Resuelve → gana Coins → colecciona","Resolve → ganha Coins → coleciona","Risolvi → guadagna Coins → colleziona"};break;
            case "gold_role":row=new String[]{"LumaGold · account currency","LumaGold · monnaie du compte","LumaGold · Kontowährung","LumaGold · moneda de la cuenta","LumaGold · moeda da conta","LumaGold · valuta dell’account"};break;
            case "gold_closed":row=new String[]{"Real-money purchases are unavailable in this build.","Les achats en argent réel sont indisponibles dans cette version.","Echtgeldkäufe sind in dieser Version nicht verfügbar.","Las compras con dinero real no están disponibles en esta versión.","As compras com dinheiro real estão indisponíveis nesta versão.","Gli acquisti con denaro reale non sono disponibili in questa versione."};break;
            case "economy_route":row=new String[]{"How currencies work","Comprendre les monnaies","So funktionieren Währungen","Cómo funcionan las monedas","Como funcionam as moedas","Come funzionano le valute"};break;
            case "shop_powers":row=new String[]{"Puzzle boosts","Bonus de puzzle","Puzzle-Helfer","Ayudas de puzle","Ajudas de puzzle","Aiuti puzzle"};break;
            case "shop_looks":row=new String[]{"Companions & style","Compagnons et style","Begleiter und Stil","Compañeros y estilo","Companheiros e estilo","Compagni e stile"};break;
            case "shop_gold":row=new String[]{"Account Gold","Gold du compte","Konto-Gold","Gold de cuenta","Gold da conta","Gold dell’account"};break;
            case "preview":row=new String[]{"Preview & details","Aperçu et détails","Vorschau & Details","Vista previa y detalles","Prévia e detalhes","Anteprima e dettagli"};break;
            case "owned":row=new String[]{"Owned","Acquis","Im Besitz","En propiedad","Adquirido","Posseduto"};break;
            case "equipped":row=new String[]{"Equipped","Équipé","Ausgerüstet","Equipado","Equipado","Equipaggiato"};break;
            case "look_only":row=new String[]{"Style only · no puzzle advantage","Style uniquement · aucun bonus de puzzle","Nur Stil · kein Puzzle-Vorteil","Solo estilo · sin ventaja en puzles","Apenas visual · sem vantagem no puzzle","Solo stile · nessun vantaggio puzzle"};break;
            case "companion_draw":row=new String[]{"COMPANION DISCOVERY","DÉCOUVERTE COMPAGNON","BEGLEITER ENTDECKEN","DESCUBRIR COMPAÑERO","DESCOBRIR COMPANHEIRO","SCOPRI COMPAGNO"};break;
            case "outfit_draw":row=new String[]{"SIGNATURE COLLECTION","COLLECTION SIGNATURE","SIGNATUR-KOLLEKTION","COLECCIÓN EXCLUSIVA","COLEÇÃO EXCLUSIVA","COLLEZIONE ESCLUSIVA"};break;
            case "draw_note":row=new String[]{"Random draw · exact odds before spending","Tirage aléatoire · probabilités avant achat","Zufallsziehung · genaue Chancen vor Kauf","Sorteo aleatorio · probabilidades antes de gastar","Sorteio aleatório · probabilidades antes de gastar","Estrazione casuale · probabilità prima della spesa"};break;
            case "direct_buy":row=new String[]{"DIRECT CHOICE","CHOIX DIRECT","DIREKTE AUSWAHL","ELECCIÓN DIRECTA","ESCOLHA DIRETA","SCELTA DIRETTA"};break;
            case "coin_price":row=new String[]{"%d LumaCoins","%d LumaCoins","%d LumaCoins","%d LumaCoins","%d LumaCoins","%d LumaCoins"};break;
            case "earn_coins":row=new String[]{"Earn Coins & gifts","Gagner des Coins et cadeaux","Coins und Geschenke verdienen","Ganar Coins y regalos","Ganhar Coins e presentes","Guadagna Coins e regali"};break;
            case "next_form":row=new String[]{"NEXT FORM","PROCHAINE FORME","NÄCHSTE FORM","SIGUIENTE FORMA","PRÓXIMA FORMA","PROSSIMA FORMA"};break;
            case "copies_skills_rule":row=new String[]{"Copies unlock forms and companion levels. Puzzle skills keep their shown effects.","Les copies débloquent formes et niveaux du compagnon. Les talents de puzzle gardent leurs effets indiqués.","Kopien schalten Gestalten und Begleiterlevel frei. Puzzle-Talente behalten ihre angegebenen Effekte.","Las copias desbloquean formas y niveles. Los talentos de puzle mantienen sus efectos indicados.","As cópias desbloqueiam formas e níveis. Os talentos de puzzle mantêm os efeitos indicados.","Le copie sbloccano forme e livelli. I talenti puzzle mantengono gli effetti indicati."};break;
            default:throw new IllegalArgumentException("Unknown 1.21 menu string: "+key);
        }
        return String.format(Locale.forLanguageTag(lang),row[i],args);
    }
}
