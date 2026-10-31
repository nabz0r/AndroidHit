package com.androidhit.lumadrift.data;

/** Localized labels for the living map and the separate tactical chapter. */
public final class Living122Strings {
    private Living122Strings(){}
    public static String get(String language,String key,Object...args){
        int l="fr".equals(language)?1:"de".equals(language)?2:"es".equals(language)?3:"pt".equals(language)?4:"it".equals(language)?5:0;
        String[] values;
        switch(key){
            case "follow":values=new String[]{"Follow me","Me suivre","Mir folgen","Seguirme","Seguir-me","Seguimi"};break;
            case "overview":values=new String[]{"Minimap","Minicarte","Minikarte","Minimapa","Minimapa","Minimappa"};break;
            case "previous_zone":values=new String[]{"Previous zone","Zone précédente","Vorherige Zone","Zona anterior","Zona anterior","Zona precedente"};break;
            case "next_zone":values=new String[]{"Next zone","Zone suivante","Nächste Zone","Zona siguiente","Zona seguinte","Zona successiva"};break;
            case "free":values=new String[]{"Drag to explore","Glisse pour explorer","Zum Erkunden ziehen","Arrastra para explorar","Arrasta para explorar","Trascina per esplorare"};break;
            case "travel":values=new String[]{"On the way to %s","En route vers %s","Unterwegs zu %s","En camino a %s","A caminho de %s","In cammino verso %s"};break;
            case "position":values=new String[]{"Companion %s · selected %s","Compagnon %s · choix %s","Begleiter %s · Auswahl %s","Compañero %s · selección %s","Companheiro %s · seleção %s","Compagno %s · scelta %s"};break;
            case "inspect_key":values=new String[]{"Key %s and its locks","Clé %s et ses cadenas","Schlüssel %s und seine Schlösser","Llave %s y sus candados","Chave %s e seus cadeados","Chiave %s e i suoi lucchetti"};break;
            case "tactical":values=new String[]{"The Echo Observatory","L’Observatoire des échos","Das Echo-Observatorium","El Observatorio de ecos","O Observatório dos ecos","L’Osservatorio degli echi"};break;
            case "tactical_note":values=new String[]{"Four puzzles. One shared talent reserve. A team on loan.","Quatre puzzles. Une réserve de talents partagée. Une équipe prêtée.","Vier Rätsel. Ein gemeinsamer Talentvorrat. Ein Leihteam.","Cuatro puzles. Una reserva de talentos. Un equipo prestado.","Quatro puzzles. Uma reserva de talentos. Uma equipa emprestada.","Quattro puzzle. Una riserva di talenti. Una squadra in prestito."};break;
            case "keys":values=new String[]{"Keys route","Route des clés","Schlüsselroute","Ruta de llaves","Rota das chaves","Percorso delle chiavi"};break;
            case "relays":values=new String[]{"Relay route","Route des relais","Relaisroute","Ruta de relevos","Rota dos relés","Percorso dei relè"};break;
            case "reserve":values=new String[]{"Reserve: %s · stage %s / 4","Réserve : %s · étape %s / 4","Vorrat: %s · Etappe %s / 4","Reserva: %s · etapa %s / 4","Reserva: %s · etapa %s / 4","Riserva: %s · tappa %s / 4"};break;
            case "loan":values=new String[]{"Loan team · each talent once per journey","Équipe prêtée · chaque talent une fois par parcours","Leihteam · jedes Talent einmal pro Reise","Equipo prestado · cada talento una vez por ruta","Equipa emprestada · cada talento uma vez por percurso","Squadra in prestito · ogni talento una volta per percorso"};break;
            case "chapter_done":values=new String[]{"Chapter complete · claim your souvenir","Chapitre terminé · récupère ton souvenir","Kapitel beendet · Andenken abholen","Capítulo completado · recoge tu recuerdo","Capítulo concluído · recebe a tua lembrança","Capitolo completato · ritira il ricordo"};break;
            case "chapter_claim":values=new String[]{"Claim 120 Coins and the souvenir","Recevoir 120 pièces et le souvenir","120 Münzen und Andenken erhalten","Recibir 120 monedas y el recuerdo","Receber 120 moedas e a lembrança","Ricevi 120 monete e il ricordo"};break;
            case "chapter_claimed":values=new String[]{"Souvenir earned · play again freely","Souvenir gagné · rejoue librement","Andenken erhalten · frei erneut spielen","Recuerdo obtenido · vuelve a jugar","Lembrança recebida · joga novamente","Ricordo ottenuto · gioca ancora"};break;
            case "recover":values=new String[]{"Restart this chapter after a save error. Campaign and wallet are kept.","Recommencer ce chapitre après l’erreur de sauvegarde. Campagne et portefeuille sont conservés.","Dieses Kapitel nach dem Speicherfehler neu starten. Kampagne und Guthaben bleiben erhalten.","Reiniciar este capítulo tras el error. Se conservan campaña y saldo.","Recomeçar este capítulo após o erro. A campanha e o saldo são preservados.","Ricomincia questo capitolo dopo l’errore. Campagna e saldo restano conservati."};break;
            case "inspect":values=new String[]{"Inspect consequences","Examiner les conséquences","Folgen ansehen","Examinar consecuencias","Examinar consequências","Esamina conseguenze"};break;
            case "hold":values=new String[]{"Hold a piece to inspect its consequences","Maintiens une pièce pour voir ses conséquences","Teil halten, um Folgen anzusehen","Mantén una pieza para ver sus consecuencias","Mantém uma peça para ver as consequências","Tieni premuto un pezzo per vedere le conseguenze"};break;
            default:values=new String[]{key,key,key,key,key,key};
        }
        return String.format(java.util.Locale.ROOT,values[l],args);
    }
}
