package com.androidhit.lumadrift.data;

import java.util.Arrays;
import java.util.Locale;

/** Map-specific purpose and progress copy, in the six supported languages. */
public final class JourneyMapStrings {
    private JourneyMapStrings() { }
    public static String get(String language,String key,Object...args){
        String[] row;
        switch(key){
            case "current":row=new String[]{"My level","Mon niveau","Mein Level","Mi nivel","Meu nível","Mio livello"};break;
            case "jump":row=new String[]{"Go to","Aller à","Gehe zu","Ir a","Ir para","Vai a"};break;
            case "journal":row=new String[]{"Journal","Carnet","Journal","Diario","Diário","Diario"};break;
            case "squad":row=new String[]{"Your squad · puzzle skills","Ton équipe · talents de puzzle","Dein Team · Rätselfähigkeiten","Tu equipo · habilidades","A tua equipa · habilidades","La tua squadra · abilità"};break;
            case "world":row=new String[]{"World %1$d · %2$d–%3$d","Monde %1$d · %2$d–%3$d","Welt %1$d · %2$d–%3$d","Mundo %1$d · %2$d–%3$d","Mundo %1$d · %2$d–%3$d","Mondo %1$d · %2$d–%3$d"};break;
            case "progress":row=new String[]{"%1$d / %2$d cleared · %3$d ★","%1$d / %2$d réussis · %3$d ★","%1$d / %2$d geschafft · %3$d ★","%1$d / %2$d superados · %3$d ★","%1$d / %2$d concluídos · %3$d ★","%1$d / %2$d completati · %3$d ★"};break;
            case "next":row=new String[]{"Next puzzle","Prochain puzzle","Nächstes Rätsel","Siguiente puzle","Próximo puzzle","Prossimo puzzle"};break;
            case "stars":row=new String[]{"%d of 3 stars","%d étoiles sur 3","%d von 3 Sternen","%d de 3 estrellas","%d de 3 estrelas","%d stelle su 3"};break;
            default:throw new IllegalArgumentException(key);
        }
        String normalized=Strings.normalize(language);int index=Arrays.asList("en","fr","de","es","pt","it").indexOf(normalized);
        return String.format(Locale.forLanguageTag(normalized),row[Math.max(0,index)],args);
    }
}
