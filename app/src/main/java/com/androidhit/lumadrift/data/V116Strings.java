package com.androidhit.lumadrift.data;

import java.util.Locale;

/** Localized player, collection and camera copy for 1.16. */
public final class V116Strings {
    private V116Strings(){}
    public static String get(String language,String key,Object... args){
        String lang=Strings.normalize(language);int col=lang.equals("fr")?1:lang.equals("de")?2:lang.equals("es")?3:lang.equals("pt")?4:lang.equals("it")?5:0;String[] row;
        switch(key){
            case "discover":row=new String[]{"Discover","Découvrir","Entdecken","Descubrir","Descobrir","Scopri"};break;
            case "overview":row=new String[]{"Whole room","Vue entière","Ganzer Raum","Toda la habitación","Divisão inteira","Stanza intera"};break;
            case "detail_view":row=new String[]{"Close-up","Vue rapprochée","Nahansicht","Vista cercana","Ver de perto","Vista ravvicinata"};break;
            case "pan_hint":row=new String[]{"Drag to explore","Glisse pour explorer","Ziehen zum Erkunden","Arrastra para explorar","Arrasta para explorar","Trascina per esplorare"};break;
            case "player_card":row=new String[]{"Player card","Carte de joueur","Spielerkarte","Tarjeta de jugador","Cartão de jogador","Carta giocatore"};break;
            case "local_profile":row=new String[]{"Your local identity","Ton identité dans le jeu","Dein Spielprofil","Tu identidad en el juego","A tua identidade no jogo","La tua identità nel gioco"};break;
            case "edit_nickname":row=new String[]{"Edit nickname","Modifier le pseudo","Spitzname ändern","Cambiar apodo","Alterar alcunha","Modifica soprannome"};break;
            case "nickname_hint":row=new String[]{"1 to 18 characters","1 à 18 caractères","1 bis 18 Zeichen","De 1 a 18 caracteres","1 a 18 caracteres","Da 1 a 18 caratteri"};break;
            case "nickname_error":row=new String[]{"Choose 1 to 18 visible characters.","Choisis de 1 à 18 caractères visibles.","Wähle 1 bis 18 sichtbare Zeichen.","Elige de 1 a 18 caracteres visibles.","Escolhe 1 a 18 caracteres visíveis.","Scegli da 1 a 18 caratteri visibili."};break;
            case "save":row=new String[]{"Save","Enregistrer","Speichern","Guardar","Guardar","Salva"};break;
            case "saved":row=new String[]{"Saved","Enregistré","Gespeichert","Guardado","Guardado","Salvato"};break;
            case "choose_flag":row=new String[]{"Select flag","Choisir le drapeau","Flagge wählen","Elegir bandera","Escolher bandeira","Scegli bandiera"};break;
            case "flag_note":row=new String[]{"Choose the flag you want to display.","Choisis le drapeau à afficher.","Wähle deine angezeigte Flagge.","Elige la bandera que quieras mostrar.","Escolhe a bandeira a apresentar.","Scegli la bandiera da mostrare."};break;
            case "world":row=new String[]{"World","Monde","Welt","Mundo","Mundo","Mondo"};break;
            case "badges":row=new String[]{"Badges","Badges","Abzeichen","Insignias","Distintivos","Distintivi"};break;
            case "badge_slots":row=new String[]{"Three badges to tell your story","Trois badges pour raconter ton parcours","Drei Abzeichen für deinen Weg","Tres insignias para contar tu historia","Três distintivos para contar o teu percurso","Tre distintivi per raccontare il tuo percorso"};break;
            case "badge_slot":row=new String[]{"Badge slot %d","Emplacement de badge %d","Abzeichenplatz %d","Espacio de insignia %d","Espaço de distintivo %d","Spazio distintivo %d"};break;
            case "empty_badge":row=new String[]{"Choose a badge","Choisir un badge","Abzeichen wählen","Elegir insignia","Escolher distintivo","Scegli un distintivo"};break;
            case "badge_earned":row=new String[]{"Earned","Gagné","Verdient","Ganada","Ganho","Ottenuto"};break;
            case "badge_locked":row=new String[]{"Keep going to earn this badge","Poursuis pour gagner ce badge","Spiele weiter für dieses Abzeichen","Sigue para ganar esta insignia","Continua para ganhar este distintivo","Continua per ottenere questo distintivo"};break;
            case "badge_first_win":row=new String[]{"First spark","Première étincelle","Erster Funke","Primera chispa","Primeira faísca","Prima scintilla"};break;
            case "badge_journey_10":row=new String[]{"Trailblazer","Éclaireur","Wegbereiter","Explorador","Explorador","Apripista"};break;
            case "badge_journey_100":row=new String[]{"Star traveller","Voyageur des étoiles","Sternenreisender","Viajero estelar","Viajante das estrelas","Viaggiatore stellare"};break;
            case "badge_decorator_5":row=new String[]{"A place of your own","Chez soi","Dein eigenes Reich","Un lugar propio","Um lugar teu","Un posto tutto tuo"};break;
            case "badge_decorator_10":row=new String[]{"Interior artist","Artiste d’intérieur","Raumkünstler","Artista de interiores","Artista de interiores","Artista d’interni"};break;
            case "badge_collector_5":row=new String[]{"Circle of friends","Cercle d’amis","Freundeskreis","Círculo de amigos","Círculo de amigos","Cerchia di amici"};break;
            case "metric_levels":row=new String[]{"%d / %d levels cleared","%d / %d niveaux réussis","%d / %d Level geschafft","%d / %d niveles superados","%d / %d níveis concluídos","%d / %d livelli superati"};break;
            case "metric_placed":row=new String[]{"%d / %d different objects placed","%d / %d objets différents placés","%d / %d verschiedene Objekte platziert","%d / %d objetos distintos colocados","%d / %d objetos diferentes colocados","%d / %d oggetti diversi posizionati"};break;
            case "metric_avatars":row=new String[]{"%d / %d companions owned","%d / %d compagnons possédés","%d / %d Begleiter im Besitz","%d / %d compañeros en propiedad","%d / %d companheiros adquiridos","%d / %d compagni posseduti"};break;
            case "summon":row=new String[]{"Star arrivals","Rencontres stellaires","Sternenbegegnungen","Encuentros estelares","Encontros estelares","Incontri stellari"};break;
            case "summon_intro":row=new String[]{"A new face. A whole new look.","Un nouveau visage. Un tout autre style.","Ein neues Gesicht. Ein neuer Look.","Una cara nueva. Otro estilo.","Uma nova cara. Um novo visual.","Un volto nuovo. Un altro stile."};break;
            case "companions_banner":row=new String[]{"Companions","Compagnons","Begleiter","Compañeros","Companheiros","Compagni"};break;
            case "outfits_banner":row=new String[]{"Signature outfits","Tenues signatures","Signatur-Outfits","Conjuntos exclusivos","Conjuntos de assinatura","Completi esclusivi"};break;
            case "companions_detail":row=new String[]{"23 companions · Nova stays yours","23 compagnons · Nova est déjà à toi","23 Begleiter · Nova gehört dir schon","23 compañeros · Nova ya es tuya","23 companheiros · Nova já é tua","23 compagni · Nova è già tua"};break;
            case "outfits_detail":row=new String[]{"5 complete looks · 4 pieces per outfit","5 tenues complètes · 4 pièces par tenue","5 komplette Looks · je 4 Teile","5 conjuntos completos · 4 piezas cada uno","5 conjuntos completos · 4 peças cada","5 completi · 4 pezzi ciascuno"};break;
            case "summon_once":row=new String[]{"Draw once · %d LumaCoins","Tirer une fois · %d LumaCoins","Einmal ziehen · %d LumaCoins","Tirar una vez · %d LumaCoins","Sortear uma vez · %d LumaCoins","Estrai una volta · %d LumaCoins"};break;
            case "summon_funds":row=new String[]{"Not enough earned LumaCoins","Pas assez de LumaCoins gagnés","Nicht genug erspielte LumaCoins","Faltan LumaCoins ganadas","Faltam LumaCoins ganhos","LumaCoins guadagnati insufficienti"};break;
            case "odds":row=new String[]{"Pool & exact odds","Contenu et probabilités","Inhalt und genaue Chancen","Contenido y probabilidades","Conteúdo e probabilidades","Contenuto e probabilità"};break;
            case "odds_note":row=new String[]{"Every draw is independent. Duplicates return LumaCoins.","Chaque tirage est indépendant. Les doublons rendent des LumaCoins.","Jede Ziehung ist unabhängig. Duplikate geben LumaCoins zurück.","Cada tirada es independiente. Los duplicados devuelven LumaCoins.","Cada sorteio é independente. Os duplicados devolvem LumaCoins.","Ogni estrazione è indipendente. I doppioni restituiscono LumaCoins."};break;
            case "earned_only":row=new String[]{"Use LumaCoins earned in the game.","Avec les LumaCoins gagnés en jouant.","Mit im Spiel verdienten LumaCoins.","Con LumaCoins ganadas jugando.","Com LumaCoins ganhos a jogar.","Con LumaCoins guadagnati giocando."};break;
            case "last_reveal":row=new String[]{"See last arrival","Revoir la dernière rencontre","Letzte Begegnung ansehen","Ver último encuentro","Ver último encontro","Rivedi l’ultimo incontro"};break;
            case "new_arrival":row=new String[]{"New arrival!","Nouvelle rencontre !","Neue Begegnung!","¡Nuevo encuentro!","Novo encontro!","Nuovo incontro!"};break;
            case "duplicate":row=new String[]{"Already in your collection","Déjà dans ta collection","Schon in deiner Sammlung","Ya en tu colección","Já na tua coleção","Già nella tua collezione"};break;
            case "duplicate_coins":row=new String[]{"Duplicate · +%d LumaCoins","Doublon · +%d LumaCoins","Duplikat · +%d LumaCoins","Duplicado · +%d LumaCoins","Duplicado · +%d LumaCoins","Doppione · +%d LumaCoins"};break;
            case "equip":row=new String[]{"Equip this look","Équiper ce look","Look anlegen","Equipar este estilo","Equipar este visual","Indossa questo stile"};break;
            case "equipped":row=new String[]{"Equipped","Équipé","Angelegt","Equipado","Equipado","Equipaggiato"};break;
            case "back_banner":row=new String[]{"Back to arrivals","Retour aux rencontres","Zurück zu Begegnungen","Volver a encuentros","Voltar aos encontros","Torna agli incontri"};break;
            case "rarity_common":row=new String[]{"Bright","Éclat","Leuchtend","Brillante","Brilhante","Splendente"};break;
            case "rarity_rare":row=new String[]{"Radiant","Radieux","Strahlend","Radiante","Radiante","Radioso"};break;
            case "rarity_epic":row=new String[]{"Celestial","Céleste","Himmlisch","Celestial","Celestial","Celestiale"};break;
            case "outfit_moon":row=new String[]{"Moonlight","Clair de lune","Mondschein","Luz de luna","Luar","Chiaro di luna"};break;
            case "outfit_star":row=new String[]{"Star parade","Parade stellaire","Sternenparade","Desfile estelar","Desfile estelar","Parata stellare"};break;
            case "outfit_harvest":row=new String[]{"Midnight harvest","Récolte de minuit","Mitternachtsernte","Cosecha nocturna","Colheita da meia-noite","Raccolto di mezzanotte"};break;
            case "outfit_heart":row=new String[]{"Heart festival","Festival des cœurs","Herzfestival","Festival de corazones","Festival dos corações","Festival dei cuori"};break;
            case "outfit_aurora":row=new String[]{"Aurora dream","Rêve d’aurore","Auroratraum","Sueño de aurora","Sonho de aurora","Sogno d’aurora"};break;
            case "look_book":row=new String[]{"Look book","Carnet de styles","Stilbuch","Libro de estilos","Livro de estilos","Libro degli stili"};break;
            case "my_companions":row=new String[]{"My companions","Mes compagnons","Meine Begleiter","Mis compañeros","Os meus companheiros","I miei compagni"};break;
            case "try_outfit":row=new String[]{"Preview outfit","Essayer la tenue","Outfit ansehen","Ver conjunto","Ver conjunto","Anteprima completo"};break;
            case "remove_badge":row=new String[]{"Remove badge","Retirer le badge","Abzeichen entfernen","Quitar insignia","Remover distintivo","Rimuovi distintivo"};break;
            case "profile_note":row=new String[]{"Saved on this device. Your account settings are separate.","Enregistré sur cet appareil. Le compte se règle séparément.","Auf diesem Gerät gespeichert. Das Konto wird separat verwaltet.","Guardado en este dispositivo. La cuenta se configura aparte.","Guardado neste dispositivo. A conta configura-se à parte.","Salvato su questo dispositivo. L’account si gestisce a parte."};break;
            case "chance":row=new String[]{"%s%% · duplicate +%d coins","%s %% · doublon +%d pièces","%s %% · Duplikat +%d Münzen","%s %% · duplicado +%d monedas","%s %% · duplicado +%d moedas","%s%% · doppione +%d monete"};break;
            default:throw new IllegalArgumentException("Unknown1.16 string: "+key);
        }
        return String.format(Locale.forLanguageTag(lang),row[col],args);
    }
}
