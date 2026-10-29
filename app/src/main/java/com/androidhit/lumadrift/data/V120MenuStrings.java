package com.androidhit.lumadrift.data;

import java.util.Locale;

/** Short menu labels. Unknown languages deliberately use the complete English dictionary. */
public final class V120MenuStrings {
    private V120MenuStrings(){}
    public static String get(String language,String key,Object...args){
        String lang=language==null?"en":language.toLowerCase(Locale.ROOT);
        int i=lang.startsWith("fr")?1:lang.startsWith("de")?2:lang.startsWith("es")?3:lang.startsWith("pt")?4:lang.startsWith("it")?5:0;
        String[] row;
        switch(key){
            case "home":row=new String[]{"Home","Accueil","Start","Inicio","Início","Inizio"};break;
            case "hello":row=new String[]{"Hi, %s!","Coucou, %s !","Hallo, %s!","¡Hola, %s!","Olá, %s!","Ciao, %s!"};break;
            case "guest":row=new String[]{"Guest · saved on this device","Invité · sauvegarde sur cet appareil","Gast · auf diesem Gerät gespeichert","Invitado · guardado en este dispositivo","Convidado · guardado neste dispositivo","Ospite · salvato su questo dispositivo"};break;
            case "signed_in":row=new String[]{"Google connected","Google connecté","Google verbunden","Google conectado","Google ligado","Google connesso"};break;
            case "team_hint":row=new String[]{"Your little team · tap to change","Ta petite équipe · touche pour changer","Dein kleines Team · zum Ändern tippen","Tu pequeño equipo · toca para cambiar","A tua pequena equipa · toca para mudar","La tua piccola squadra · tocca per cambiare"};break;
            case "gifts":row=new String[]{"Gifts","Cadeaux","Geschenke","Regalos","Presentes","Regali"};break;
            case "gift_ready":row=new String[]{"%d ready to open","%d à ouvrir","%d zum Öffnen","%d para abrir","%d para abrir","%d da aprire"};break;
            case "gift_soon":row=new String[]{"A little goal for today","Un petit objectif pour aujourd’hui","Ein kleines Ziel für heute","Un pequeño objetivo para hoy","Um pequeno objetivo para hoje","Un piccolo obiettivo per oggi"};break;
            case "shop":row=new String[]{"Boutique","Boutique","Laden","Tienda","Loja","Negozio"};break;
            case "shop_hint":row=new String[]{"Looks, friends & little boosts","Looks, amis et petits coups de pouce","Looks, Freunde und kleine Helfer","Estilos, amigos y pequeñas ayudas","Visuais, amigos e pequenas ajudas","Look, amici e piccoli aiuti"};break;
            case "quests":row=new String[]{"Quests & gifts","Quêtes et cadeaux","Aufgaben und Geschenke","Misiones y regalos","Missões e presentes","Missioni e regali"};break;
            case "open_gift":row=new String[]{"Open gift","Ouvrir le cadeau","Geschenk öffnen","Abrir regalo","Abrir presente","Apri regalo"};break;
            case "collected":row=new String[]{"Collected","Récupéré","Abgeholt","Recogido","Recolhido","Ritirato"};break;
            case "in_progress":row=new String[]{"In progress","En cours","In Arbeit","En curso","Em curso","In corso"};break;
            case "gift_for_you":row=new String[]{"A little gift for you!","Un petit cadeau pour toi !","Ein kleines Geschenk für dich!","¡Un regalito para ti!","Um presente para ti!","Un piccolo regalo per te!"};break;
            case "tap_continue":row=new String[]{"Tap to continue","Touche pour continuer","Zum Fortfahren tippen","Toca para continuar","Toca para continuar","Tocca per continuare"};break;
            case "earned":row=new String[]{"Earned in play","Gagnés en jouant","Beim Spielen verdient","Ganadas jugando","Ganhas a jogar","Guadagnate giocando"};break;
            case "gold":row=new String[]{"Premium wallet","Portefeuille premium","Premium-Guthaben","Monedero prémium","Carteira premium","Portafoglio premium"};break;
            case "energy":row=new String[]{"Energy","Énergie","Energie","Energía","Energia","Energia"};break;
            case "wallet_unknown":row=new String[]{"Balance unavailable","Solde indisponible","Guthaben nicht verfügbar","Saldo no disponible","Saldo indisponível","Saldo non disponibile"};break;
            case "shop_powers":row=new String[]{"Little boosts","Petits coups de pouce","Kleine Helfer","Pequeñas ayudas","Pequenas ajudas","Piccoli aiuti"};break;
            case "shop_looks":row=new String[]{"Looks & friends","Looks et amis","Looks und Freunde","Estilos y amigos","Visuais e amigos","Look e amici"};break;
            case "shop_feature":row=new String[]{"Find your next favourite","Trouve ton prochain coup de cœur","Finde deinen nächsten Liebling","Encuentra tu próximo favorito","Encontra o teu próximo favorito","Trova il tuo prossimo preferito"};break;
            case "shop_free":row=new String[]{"Earn gifts by playing","Des cadeaux en jouant","Geschenke durchs Spielen","Regalos por jugar","Presentes a jogar","Regali giocando"};break;
            case "view":row=new String[]{"Take a look","Découvrir","Ansehen","Descubrir","Descobrir","Scopri"};break;
            case "next":row=new String[]{"Next","Suivant","Weiter","Siguiente","Seguinte","Avanti"};break;
            case "previous":row=new String[]{"Previous","Précédent","Zurück","Anterior","Anterior","Precedente"};break;
            default:throw new IllegalArgumentException("Unknown 1.20 menu string: "+key);
        }
        return String.format(Locale.forLanguageTag(lang),row[i],args);
    }
}
