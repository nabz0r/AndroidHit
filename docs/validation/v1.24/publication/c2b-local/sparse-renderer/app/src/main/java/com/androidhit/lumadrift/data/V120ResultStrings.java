package com.androidhit.lumadrift.data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Visible result and action-policy copy; no clock deadline or hidden score input. */
public final class V120ResultStrings {
    private static final Map<String,String[]> TEXT=new HashMap<>();
    static {
        add("moves","Moves","Coups","Züge","Movimientos","Jogadas","Mosse");
        add("time","Your time","Ton temps","Deine Zeit","Tu tiempo","O teu tempo","Il tuo tempo");
        add("corrections","%d returns","%d retours","%d Rückschritte","%d vueltas","%d regressos","%d ritorni");
        add("undo_rule","Undo restores the position. It does not refund a spent move; moving the piece out again costs one move.","Annuler rétablit la position. Le coup dépensé n’est pas rendu ; ressortir la pièce consomme un nouveau coup.","Rückgängig stellt die Position wieder her. Der verbrauchte Zug bleibt gezählt; erneutes Herausbewegen kostet einen Zug.","Deshacer restaura la posición. No devuelve el movimiento gastado; sacar la pieza otra vez cuesta otro.","Desfazer repõe a posição. Não devolve a jogada gasta; voltar a retirar a peça custa outra.","Annulla ripristina la posizione. Non restituisce la mossa spesa; far uscire di nuovo la tessera costa una mossa.");
        add("score_rule","3★ no returns · 2★ up to %d · 1★ beyond. Time is informational.","3★ sans retour · 2★ jusqu’à %d · 1★ au-delà. Le temps est informatif.","3★ ohne Rückschritt · 2★ bis %d · danach1★. Zeit nur zur Info.","3★ sin deshacer · 2★ hasta%d · después1★. El tiempo solo informa.","3★ sem desfazer · 2★ até%d · depois1★. O tempo é informativo.","3★ senza annullare · 2★ fino a%d · poi1★. Il tempo è informativo.");
        add("saved","Rewards saved","Gains enregistrés","Belohnungen gespeichert","Premios guardados","Prémios guardados","Premi salvati");
        add("replay","Replay · no new first-clear reward","Rejeu · aucun nouveau gain de première victoire","Wiederholung · keine neue Erstbelohnung","Repetición · sin nuevo premio inicial","Repetição · sem novo prémio inicial","Replay · nessun nuovo premio iniziale");
        add("map","Map","Carte","Karte","Mapa","Mapa","Mappa");
        add("shop","Shop","Boutique","Shop","Tienda","Loja","Negozio");
        add("nice","Nice!","Bien joué !","Gut gemacht!","¡Bien hecho!","Boa!","Ben fatto!");
        add("gifts","%d gifts to open","%d cadeaux à ouvrir","%d Geschenke öffnen","%d regalos por abrir","%d presentes por abrir","%d regali da aprire");
        add("clock_note","Time played, excluding pauses, help and menus. No time limit.","Temps joué, hors pauses, aide et menus. Aucune limite de temps.","Spielzeit ohne Pause, Hilfe und Menüs. Kein Zeitlimit.","Tiempo jugado sin pausas, ayuda ni menús. Sin límite.","Tempo jogado sem pausas, ajuda ou menus. Sem limite.","Tempo giocato senza pause, aiuto e menu. Nessun limite.");
    }
    private V120ResultStrings(){}
    private static void add(String key,String...values){TEXT.put(key,values);}
    public static String get(String language,String key,Object...args){
        String[] row=TEXT.get(key);if(row==null)return key;
        int index="fr".equals(language)?1:"de".equals(language)?2:"es".equals(language)?3:"pt".equals(language)?4:"it".equals(language)?5:0;
        return args.length==0?row[index]:String.format(Locale.ROOT,row[index],args);
    }
}
