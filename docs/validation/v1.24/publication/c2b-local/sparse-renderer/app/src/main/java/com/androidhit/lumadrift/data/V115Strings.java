package com.androidhit.lumadrift.data;

import java.util.Locale;

/** Short, localized 1.15 screen copy. Rules, prices, rewards and ownership remain domain data. */
public final class V115Strings {
    private V115Strings(){}
    public static String get(String language,String key,Object... arguments){
        String lang=Strings.normalize(language);int column=lang.equals("fr")?1:lang.equals("de")?2:lang.equals("es")?3:lang.equals("pt")?4:lang.equals("it")?5:0;
        String[] row;
        switch(key){
            case "adventure":case "nav_adventure":row=new String[]{"Adventure","Aventure","Abenteuer","Aventura","Aventura","Avventura"};break;
            case "house":case "nav_home":row=new String[]{"Home","Maison","Zuhause","Casa","Casa","Casa"};break;
            case "collection":case "nav_collection":row=new String[]{"Collection","Collection","Sammlung","Colección","Coleção","Collezione"};break;
            case "atelier":case "nav_atelier":row=new String[]{"Atelier","Atelier","Atelier","Taller","Atelier","Atelier"};break;
            case "adventure_intro":row=new String[]{"A new path, at your own pace.","Un nouveau chemin, à ton rythme.","Ein neuer Weg, in deinem Tempo.","Un nuevo camino, a tu ritmo.","Um novo caminho, ao teu ritmo.","Un nuovo percorso, al tuo ritmo."};break;
            case "home_intro":row=new String[]{"A little time for you and your companion.","Du temps pour toi et ton compagnon.","Zeit für dich und deinen Begleiter.","Un rato para ti y tu compañero.","Um momento para ti e o teu companheiro.","Un momento per te e il tuo compagno."};break;
            case "collection_intro":row=new String[]{"Your companions and little discoveries.","Tes compagnons et tes trouvailles.","Deine Begleiter und Fundstücke.","Tus compañeros y tus hallazgos.","Os teus companheiros e descobertas.","I tuoi compagni e le tue scoperte."};break;
            case "atelier_intro":row=new String[]{"A look that feels like you.","Une silhouette qui te ressemble.","Ein Look, der zu dir passt.","Un estilo que te representa.","Um estilo que combina contigo.","Uno stile che ti somiglia."};break;
            case "next_adventure":row=new String[]{"Continue the adventure","Continuer l’aventure","Abenteuer fortsetzen","Continuar la aventura","Continuar a aventura","Continua l’avventura"};break;
            case "chapter":row=new String[]{"Chapter %d","Chapitre %d","Kapitel %d","Capítulo %d","Capítulo %d","Capitolo %d"};break;
            case "new_treasure":row=new String[]{"A new treasure","Un nouveau trésor","Ein neuer Schatz","Un nuevo tesoro","Um novo tesouro","Un nuovo tesoro"};break;
            case "reward_saved":row=new String[]{"Ready for your next adventure.","Pour ta prochaine aventure.","Für dein nächstes Abenteuer.","Para tu próxima aventura.","Para a tua próxima aventura.","Per la tua prossima avventura."};break;
            case "view_collection":row=new String[]{"My collection","Ma collection","Meine Sammlung","Mi colección","A minha coleção","La mia collezione"};break;
            case "view_house":row=new String[]{"See it at home","Voir dans la maison","Zu Hause ansehen","Ver en casa","Ver em casa","Vedi a casa"};break;
            case "decorate":row=new String[]{"Decorate this room","Décorer cette pièce","Zimmer gestalten","Decorar esta habitación","Decorar esta divisão","Decora questa stanza"};break;
            case "decoration_title":row=new String[]{"Your own little world","À ta façon","Deine kleine Welt","A tu manera","À tua maneira","A modo tuo"};break;
            case "decoration_intro":row=new String[]{"Choose an object, then its place.","Choisis un objet, puis sa place.","Wähle ein Objekt und seinen Platz.","Elige un objeto y su lugar.","Escolhe um objeto e o seu lugar.","Scegli un oggetto e il suo posto."};break;
            case "touch_object":row=new String[]{"Touch an object to get closer.","Touche un objet pour t’en approcher.","Tippe ein Objekt an.","Toca un objeto para acercarte.","Toca num objeto para o veres melhor.","Tocca un oggetto per avvicinarti."};break;
            case "room_living":row=new String[]{"Living room","Salon","Wohnzimmer","Salón","Sala","Salotto"};break;
            case "room_garden":row=new String[]{"Garden","Jardin","Garten","Jardín","Jardim","Giardino"};break;
            case "room_bedroom":row=new String[]{"Bedroom","Chambre","Schlafzimmer","Dormitorio","Quarto","Camera"};break;
            case "keep_look":row=new String[]{"Keep this look","Garder ce look","Look behalten","Guardar este estilo","Guardar este visual","Mantieni questo stile"};break;
            case "shop":row=new String[]{"Shop","Boutique","Shop","Tienda","Loja","Negozio"};break;
            case "events":row=new String[]{"Events","Événements","Events","Eventos","Eventos","Eventi"};break;
            case "objectives":row=new String[]{"Objectives","Objectifs","Ziele","Objetivos","Objetivos","Obiettivi"};break;
            case "all":row=new String[]{"All","Tout","Alle","Todo","Tudo","Tutto"};break;
            case "companions":row=new String[]{"Companions","Compagnons","Begleiter","Compañeros","Companheiros","Compagni"};break;
            case "objects":row=new String[]{"Objects","Objets","Objekte","Objetos","Objetos","Oggetti"};break;
            case "looks":row=new String[]{"Looks","Styles","Stile","Estilos","Visuais","Stili"};break;
            case "actions_short":row=new String[]{"%d actions","%d actions","%d Aktionen","%d acciones","%d ações","%d azioni"};break;
            case "choose_arrow":row=new String[]{"Choose an arrow.","Choisis une flèche.","Wähle einen Pfeil.","Elige una flecha.","Escolhe uma seta.","Scegli una freccia."};break;
            case "blocked_free":row=new String[]{"Blocked · no action used","Bloqué · aucune action retirée","Blockiert · keine Aktion verbraucht","Bloqueado · sin gastar acciones","Bloqueado · nenhuma ação gasta","Bloccato · nessuna azione usata"};break;
            case "free_retry":row=new String[]{"Try again, for free","Recommencer gratuitement","Kostenlos neu versuchen","Reintentar gratis","Tentar de novo, sem custo","Riprova gratis"};break;
            case "try_again":row=new String[]{"Another try?","Un nouvel essai ?","Noch ein Versuch?","¿Otro intento?","Mais uma tentativa?","Un altro tentativo?"};break;
            case "retry_intro":row=new String[]{"Your progress is safe. Take a fresh look.","Ta progression est gardée. Change de regard.","Dein Fortschritt bleibt. Versuch einen neuen Blick.","Tu progreso está guardado. Míralo de nuevo.","O teu progresso está guardado. Experimenta outra ideia.","I tuoi progressi sono al sicuro. Prova un’altra idea."};break;
            case "return_to_game":row=new String[]{"Back to the game","Retour au jeu","Zurück zum Spiel","Volver al juego","Voltar ao jogo","Torna al gioco"};break;
            case "one_goal":row=new String[]{"Clear every arrow","Libère toutes les flèches","Befreie alle Pfeile","Libera todas las flechas","Liberta todas as setas","Libera tutte le frecce"};break;
            case "home_short":row=new String[]{"Your home","Ta maison","Dein Zuhause","Tu casa","A tua casa","La tua casa"};break;
            case "all_levels":row=new String[]{"All levels","Tous les niveaux","Alle Level","Todos los niveles","Todos os níveis","Tutti i livelli"};break;
            case "rewards":row=new String[]{"Rewards","Récompenses","Belohnungen","Recompensas","Recompensas","Ricompense"};break;
            case "wardrobe":row=new String[]{"Wardrobe","Garde-robe","Garderobe","Vestuario","Guarda-roupa","Guardaroba"};break;
            case "equipment":row=new String[]{"Equipment","Équipement","Ausrüstung","Equipo","Equipamento","Equipaggiamento"};break;
            case "powers":row=new String[]{"Powers","Pouvoirs","Kräfte","Poderes","Poderes","Poteri"};break;
            case "room":row=new String[]{"Room","Pièce","Zimmer","Habitación","Divisão","Stanza"};break;
            case "zoom_in":row=new String[]{"Zoom in","Agrandir","Vergrößern","Ampliar","Ampliar","Ingrandisci"};break;
            case "zoom_out":row=new String[]{"Zoom out","Réduire","Verkleinern","Reducir","Reduzir","Riduci"};break;
            case "finish":row=new String[]{"Done","Terminer","Fertig","Terminar","Concluir","Fine"};break;
            case "furniture":row=new String[]{"Furniture","Mobilier","Möbel","Muebles","Mobiliário","Arredi"};break;
            case "preview":row=new String[]{"Preview","Aperçu","Vorschau","Vista previa","Pré-visualizar","Anteprima"};break;
            case "choose":row=new String[]{"Choose","Choisir","Wählen","Elegir","Escolher","Scegli"};break;
            case "owned":row=new String[]{"Owned","Possédé","Im Besitz","En propiedad","Adquirido","Posseduto"};break;
            case "buy":row=new String[]{"Buy","Acheter","Kaufen","Comprar","Comprar","Acquista"};break;
            case "place":row=new String[]{"Place","Placer","Platzieren","Colocar","Colocar","Posiziona"};break;
            case "movement_tip":row=new String[]{"Follow each arrow toward an open exit.","Suis chaque flèche vers une sortie ouverte.","Folge jedem Pfeil zu einem offenen Ausgang.","Sigue cada flecha hacia una salida abierta.","Segue cada seta até uma saída aberta.","Segui ogni freccia verso un’uscita aperta."};break;
            case "relay_tip":row=new String[]{"Relays change the garden’s gates.","Les relais changent les portes du jardin.","Relais verändern die Gartentore.","Los relés cambian las puertas del jardín.","Os relés alteram os portões do jardim.","I relè cambiano i cancelli del giardino."};break;
            case "relay_rule":row=new String[]{"A relay opens or closes the exits shown on it.","Un relais ouvre ou ferme les sorties qu’il indique.","Ein Relais öffnet oder schließt die angezeigten Ausgänge.","Un relé abre o cierra las salidas indicadas.","Um relé abre ou fecha as saídas indicadas.","Un relè apre o chiude le uscite indicate."};break;
            case "key_tip":row=new String[]{"Free the key to open its matching lock.","Libère la clé pour ouvrir son verrou.","Befreie den Schlüssel für das passende Schloss.","Libera la llave para abrir su cerradura.","Liberta a chave para abrir a sua fechadura.","Libera la chiave per aprire la sua serratura."};break;
            case "puzzle_goal":row=new String[]{"Free %d arrows","Libère %d flèches","Befreie %d Pfeile","Libera %d flechas","Liberta %d setas","Libera %d frecce"};break;
            case "victory_note":row=new String[]{"A little further on your journey.","Un peu plus loin dans ton aventure.","Ein Stück weiter auf deiner Reise.","Un poco más lejos en tu aventura.","Mais um passo na tua aventura.","Un passo avanti nella tua avventura."};break;
            case "keep_playing":row=new String[]{"Keep playing","Continuer à jouer","Weiterspielen","Seguir jugando","Continuar a jogar","Continua a giocare"};break;
            case "back_adventure":row=new String[]{"Back to adventure","Retour à l’aventure","Zurück zum Abenteuer","Volver a la aventura","Voltar à aventura","Torna all’avventura"};break;
            case "home_goal":row=new String[]{"Your next little wish","Ta prochaine envie","Dein nächster Wunsch","Tu próximo deseo","O teu próximo desejo","Il tuo prossimo desiderio"};break;
            case "sets":row=new String[]{"Worlds","Univers","Welten","Mundos","Mundos","Mondi"};break;
            case "calm":row=new String[]{"Calm","Douceur","Ruhe","Calma","Calma","Calma"};break;
            case "forest":row=new String[]{"Forest","Forêt","Wald","Bosque","Floresta","Foresta"};break;
            case "stellar":row=new String[]{"Stellar","Stellaire","Sterne","Estelar","Estelar","Stellare"};break;
            case "neon":row=new String[]{"Neon","Néon","Neon","Neón","Néon","Neon"};break;
            case "profile":row=new String[]{"Profile","Profil","Profil","Perfil","Perfil","Profilo"};break;
            default:throw new IllegalArgumentException("Unknown1.15 string: "+key);
        }
        return String.format(Locale.forLanguageTag(lang),row[column],arguments);
    }
}
