package com.androidhit.lumadrift.data;

import java.util.Locale;

/** Clear choices, honest wallet state and short descriptions; no price or policy is defined here. */
public final class V112Strings {
    private V112Strings() {}
    public static String get(String language,String key,Object... arguments) {
        String lang=Strings.normalize(language);int column=lang.equals("fr")?1:lang.equals("de")?2:lang.equals("es")?3:lang.equals("pt")?4:lang.equals("it")?5:0;
        String[] row;
        switch(key) {
            case "shop_tag": row=new String[]{"Small wonders. Clear choices.","Des merveilles à choisir.","Kleine Wunder. Klare Wahl.","Pequeñas maravillas para elegir.","Pequenas maravilhas à escolha.","Piccole meraviglie da scegliere."};break;
            case "workshop": row=new String[]{"Your little atelier","Ton petit atelier","Dein kleines Atelier","Tu pequeño taller","O teu pequeno atelier","Il tuo piccolo atelier"};break;
            case "shop_note": row=new String[]{"Look, try a style, then choose.","Regarde, essaie un style, puis choisis.","Ansehen, Stil testen, dann wählen.","Mira, prueba un estilo y elige.","Vê, experimenta um estilo e escolhe.","Guarda, prova uno stile e scegli."};break;
            case "coins_note": row=new String[]{"Earned through play · saved on this device","Gagnées en jouant · sur cet appareil","Erspeilt · auf diesem Gerät gespeichert","Ganadas jugando · en este dispositivo","Ganhas a jogar · neste dispositivo","Guadagnate giocando · su questo dispositivo"};break;
            case "gold_note": row=new String[]{"LumaGold belongs to your signed-in account. It is separate from the LumaCoins earned through play.","Les LumaGold sont liés à ton compte connecté. Ils sont séparés des LumaCoins gagnées en jouant.","LumaGold gehört zu deinem angemeldeten Konto. Es ist von erspielten LumaCoins getrennt.","LumaGold pertenece a tu cuenta conectada y es independiente de las LumaCoins ganadas jugando.","LumaGold pertence à tua conta ligada. É separado das LumaCoins ganhas a jogar.","LumaGold appartiene al tuo account connesso. È separato dalle LumaCoins guadagnate giocando."};break;
            case "gold_unloaded": row=new String[]{"Balance not loaded · open wallet","Solde non chargé · ouvrir le portefeuille","Guthaben nicht geladen · Wallet öffnen","Saldo sin cargar · abrir cartera","Saldo não carregado · abrir carteira","Saldo non caricato · apri portafoglio"};break;
            case "gold_last": row=new String[]{"Last confirmed balance","Dernier solde confirmé","Zuletzt bestätigtes Guthaben","Último saldo confirmado","Último saldo confirmado","Ultimo saldo confermato"};break;
            case "capsule_review": row=new String[]{"See the possible looks, exact odds and duplicate returns before opening.","Découvre les tenues, les probabilités et les doublons avant d’ouvrir.","Sieh dir Looks, Chancen und Duplikate vor dem Öffnen an.","Consulta aspectos, probabilidades y duplicados antes de abrir.","Vê visuais, probabilidades e duplicados antes de abrir.","Consulta stili, probabilità e duplicati prima di aprire."};break;
            case "power_hint": row=new String[]{"One safe next move","Un prochain coup sûr","Ein sicherer nächster Zug","Una próxima jugada segura","Uma próxima jogada segura","Una prossima mossa sicura"};break;
            case "power_vision": row=new String[]{"Spot the movable pieces","Repère les pièces libres","Bewegliche Teile erkennen","Detecta las piezas libres","Encontra as peças livres","Individua i pezzi liberi"};break;
            case "power_flow": row=new String[]{"Up to three safe moves","Jusqu’à trois coups sûrs","Bis zu drei sichere Züge","Hasta tres jugadas seguras","Até três jogadas seguras","Fino a tre mosse sicure"};break;
            case "power_shield": row=new String[]{"Protect three blocked attempts","Protège trois essais bloqués","Schützt drei blockierte Versuche","Protege tres intentos bloqueados","Protege três tentativas bloqueadas","Protegge tre tentativi bloccati"};break;
            case "power_focus": row=new String[]{"Find moves that keep a solution","Repère les coups qui gardent une solution","Finde Züge, die lösbar bleiben","Busca jugadas que mantengan solución","Encontra jogadas que mantêm solução","Trova mosse che mantengono una soluzione"};break;
            case "power_trail": row=new String[]{"See up to five solution steps","Vois jusqu’à cinq étapes de solution","Bis zu fünf Lösungsschritte sehen","Ve hasta cinco pasos de solución","Vê até cinco passos de solução","Vedi fino a cinque passi della soluzione"};break;
            case "gold_short": row=new String[]{"Not loaded","Non chargé","Nicht geladen","Sin cargar","Não carregado","Non caricato"};break;
            case "gold_cached_short": row=new String[]{"Last known","Solde connu","Stand zuletzt","Último saldo","Último saldo","Ultimo saldo"};break;
            default: throw new IllegalArgumentException("Unknown1.12 string: "+key);
        }
        return String.format(Locale.forLanguageTag(lang),row[column],arguments);
    }
}
