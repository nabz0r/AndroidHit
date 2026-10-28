package com.androidhit.lumadrift.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** House copy in the same six languages as the puzzle. Regional tags use Strings normalization. */
public final class HomeStrings {
    private static final String[] CODES={"en","fr","de","es","pt","it"};
    private static final Map<String,String[]> TEXT=new HashMap<>();
    static {
        add("title","My little home","Mon petit chez-moi","Mein kleines Zuhause","Mi pequeño hogar","O meu cantinho","La mia casetta");
        add("home","Home","Maison","Zuhause","Casa","Casa","Casa");
        add("scene_accessible","Home scene","Pièce de la maison","Zimmeransicht","Vista de la casa","Vista da casa","Vista della casa");
        add("companion_accessible","Home companion","Compagnon à la maison","Begleiter zu Hause","Compañero en casa","Companheiro em casa","Compagno a casa");
        add("walking","On my way!","J’arrive !","Ich komme!","¡Ya voy!","Já vou!","Arrivo!");
        add("decorate","Decorate","Décorer","Einrichten","Decorar","Decorar","Arreda");
        add("room","My room","Ma pièce","Mein Zimmer","Mi habitación","O meu quarto","La mia stanza");
        add("move","Come here","Viens ici","Komm her","Ven aquí","Vem cá","Vieni qui");
        add("hug","A little hug","Un petit câlin","Eine Umarmung","Un abrazo","Um abraço","Un abbraccio");
        add("hugging","A moment together","Un moment ensemble","Ein Moment zusammen","Un momento juntos","Um momento juntos","Un momento insieme");
        add("wardrobe","Dress up","Habiller","Anziehen","Vestir","Vestir","Vesti");
        add("catalog","Furniture","Meubles","Möbel","Muebles","Móveis","Mobili");
        add("place","Place","Placer","Aufstellen","Colocar","Colocar","Posiziona");
        add("rotate","Rotate","Tourner","Drehen","Girar","Rodar","Ruota");
        add("remove","Store away","Ranger","Wegräumen","Guardar","Guardar","Riponi");
        add("use","Use","Utiliser","Benutzen","Usar","Usar","Usa");
        add("done","Done","Terminé","Fertig","Listo","Feito","Fatto");
        add("back","Back","Retour","Zurück","Volver","Voltar","Indietro");
        add("floor","Floor","Sol","Boden","Suelo","Chão","Pavimento");
        add("wallpaper","Walls","Murs","Wände","Paredes","Paredes","Pareti");
        add("locked","Not owned yet","Pas encore acquis","Noch nicht im Besitz","Aún no es tuyo","Ainda não adquirido","Non ancora tuo");
        add("price","%d LumaCoins","%d LumaCoins","%d LumaCoins","%d LumaCoins","%d LumaCoins","%d LumaCoins");
        add("owned","Yours","À toi","Deins","Tuyo","Teu","Tuo");
        add("placed","In your room","Dans ta pièce","In deinem Zimmer","En tu habitación","No teu quarto","Nella tua stanza");
        add("goal","My next wish","Ma prochaine envie","Mein nächster Wunsch","Mi próximo deseo","O meu próximo desejo","Il mio prossimo desiderio");
        add("set_goal","Save as my wish","Garder comme envie","Als Wunsch merken","Guardar como deseo","Guardar como desejo","Salva come desiderio");
        add("clear_goal","Clear wish","Retirer l’envie","Wunsch entfernen","Quitar deseo","Retirar desejo","Rimuovi desiderio");
        add("goal_ready","Your wish is within reach","Ton envie est à portée de main","Dein Wunsch ist erreichbar","Tu deseo está a tu alcance","O teu desejo está ao alcance","Il tuo desiderio è a portata di mano");
        add("goal_progress","%d / %d LumaCoins","%d / %d LumaCoins","%d / %d LumaCoins","%d / %d LumaCoins","%d / %d LumaCoins","%d / %d LumaCoins");
        add("buy","Get for %d","Obtenir pour %d","Für %d holen","Obtener por %d","Obter por %d","Ottieni per %d");
        add("free","A welcome gift","Un cadeau de bienvenue","Ein Willkommensgeschenk","Un regalo de bienvenida","Um presente de boas-vindas","Un regalo di benvenuto");
        add("not_enough","Play a few puzzles to earn more LumaCoins","Joue quelques puzzles pour gagner des LumaCoins","Spiele Rätsel für mehr LumaCoins","Juega puzles para ganar más LumaCoins","Joga puzzles para ganhar mais LumaCoins","Gioca per guadagnare altri LumaCoins");
        add("play","Play puzzles","Jouer aux puzzles","Rätsel spielen","Jugar puzles","Jogar puzzles","Gioca ai rompicapi");
        add("placement_hint","Choose a free tile, then place your object","Choisis une case libre, puis place ton objet","Wähle ein freies Feld und stelle dein Möbel auf","Elige una casilla libre y coloca tu objeto","Escolhe uma casa livre e coloca o objeto","Scegli una casella libera e posiziona l’oggetto");
        add("move_hint","Touch the floor to call your companion","Touche le sol pour appeler ton compagnon","Tippe auf den Boden, um deinen Begleiter zu rufen","Toca el suelo para llamar a tu compañero","Toca no chão para chamar o teu companheiro","Tocca il pavimento per chiamare il tuo compagno");
        add("blocked","Leave a little room here","Laisse un peu de place ici","Lass hier etwas Platz","Deja un poco de espacio aquí","Deixa um pouco de espaço aqui","Lascia un po’ di spazio qui");
        add("unreachable","There is no clear path here","Le chemin est bloqué ici","Der Weg hierher ist versperrt","El camino está bloqueado","O caminho está bloqueado","Il percorso è bloccato");
        add("selected","Selected","Sélectionné","Ausgewählt","Seleccionado","Selecionado","Selezionato");
        add("welcome","A little place to make your own","Un petit endroit bien à toi","Ein kleiner Ort ganz für dich","Un rinconcito para hacer tuyo","Um cantinho para tornares teu","Un angolino da rendere tuo");
        add("welcome_detail","Play puzzles, earn LumaCoins and make room for your next wish.","Joue, gagne des LumaCoins et fais une place à ta prochaine envie.","Spiele Rätsel, verdiene LumaCoins und richte dein Zuhause ein.","Juega, gana LumaCoins y haz sitio para tu próximo deseo.","Joga, ganha LumaCoins e cria espaço para o teu próximo desejo.","Gioca, guadagna LumaCoins e fai spazio al tuo prossimo desiderio.");
        add("rest","Take a little rest","Faire une petite sieste","Kurz ausruhen","Descansar un poco","Descansar um pouco","Riposati un po’");
        add("sit","Sit together","Se poser ensemble","Zusammensitzen","Sentarse juntos","Sentar juntos","Siediti qui");
        add("light","Switch the light","Allumer la lumière","Licht umschalten","Encender la luz","Acender a luz","Accendi la luce");
        add("admire","Enjoy the leaves","Admirer les feuilles","Blätter bewundern","Admirar las hojas","Admirar as folhas","Ammira le foglie");
        add("relax","Stretch out","S’étirer","Strecken","Estirarse","Espreguiçar","Stiracchiati");
        add("tea","A little tea break","Une petite pause thé","Eine kleine Teepause","Una pausa para el té","Uma pausa para chá","Una pausa per il tè");
        add("read","Read a little","Lire un peu","Ein wenig lesen","Leer un poco","Ler um pouco","Leggi un po’");
        add("cuddle","Cuddle the plush","Câliner la peluche","Kuscheltier drücken","Abrazar el peluche","Abraçar a pelúcia","Abbraccia il peluche");
        add("switch","A familiar little click","Un petit clic familier","Ein vertrautes Klicken","Un clic familiar","Um clique familiar","Un clic familiare");
        add("watch","Follow the little arrows","Suivre les petites flèches","Den Pfeilen folgen","Seguir las flechitas","Seguir as setinhas","Segui le freccette");
        add("first_title","Let’s find the way","Trouvons le passage","Finden wir den Weg","Encontremos el camino","Vamos encontrar o caminho","Troviamo il passaggio");
        add("first_note","Tap a piece. It follows its arrow when the path is clear.","Touche une pièce. Elle suit sa flèche si le passage est libre.","Tippe ein Teil an. Es folgt seinem Pfeil, wenn der Weg frei ist.","Toca una pieza. Sigue su flecha si el camino está libre.","Toca numa peça. Segue a seta se o caminho estiver livre.","Tocca un pezzo. Segue la freccia se il percorso è libero.");
        add("first_gate","Watch the switch: it changes the gate.","Regarde l’interrupteur : il change la porte.","Achte auf den Schalter: Er verändert das Tor.","Mira el interruptor: cambia la puerta.","Olha para o interruptor: muda a porta.","Guarda l’interruttore: cambia il varco.");
        add("first_done","You found the way!","Tu as trouvé le passage !","Du hast den Weg gefunden!","¡Encontraste el camino!","Encontraste o caminho!","Hai trovato il passaggio!");
        add("first_home","Discover your little home","Découvre ton petit chez-toi","Entdecke dein kleines Zuhause","Descubre tu pequeño hogar","Descobre o teu cantinho","Scopri la tua casetta");
        add("first_skip","I know how to play","Je sais déjà jouer","Ich kenne das Spiel","Ya sé jugar","Já sei jogar","So già giocare");
        add("cuddle_note","A little pause, just the two of you.","Une petite pause, rien que vous deux.","Eine kleine Pause, nur für euch beide.","Una pequeña pausa, solo para los dos.","Uma pequena pausa, só para vocês os dois.","Una piccola pausa, solo per voi due.");
        alias("room_title","title");alias("room_note","welcome_detail");alias("cuddle","hug");alias("dress_up","wardrobe");alias("furniture","catalog");
        alias("move_here","move");alias("choose_goal","set_goal");alias("placement_blocked","blocked");
        alias("activity_rest","rest");alias("activity_read","read");alias("activity_lamp","light");alias("activity_play","watch");
        add("bed_name","Moonfold bed","Lit Pli de lune","Mondfaltenbett","Cama Pliegue de luna","Cama Dobra de lua","Letto Piega di luna");
        add("bed_desc","A soft blanket for a little daydream.","Une couverture douce pour rêvasser.","Eine weiche Decke zum Tagträumen.","Una manta suave para soñar despierto.","Uma manta macia para sonhar acordado.","Una coperta morbida per sognare a occhi aperti.");
        add("sofa_name","Cloud nook sofa","Canapé Coin de nuage","Wolkenecksofa","Sofá Rincón de nube","Sofá Cantinho de nuvem","Divano Angolo di nuvola");
        add("sofa_desc","A little seat with room to curl up.","Un petit siège où se blottir.","Ein kleiner Sitz zum Einkuscheln.","Un asiento para acurrucarse.","Um lugar para te aconchegares.","Una seduta per accoccolarsi.");
        add("lamp_name","Warmglow lamp","Lampe Lueur douce","Wärmelichtlampe","Lámpara Luz cálida","Candeeiro Luz morna","Lampada Luce calda");
        add("lamp_desc","A golden pool of light for your evening.","Une lumière dorée pour la soirée.","Goldenes Licht für deinen Abend.","Una luz dorada para la tarde.","Uma luz dourada para o serão.","Una luce dorata per la sera.");
        add("plant_name","Little leaf plant","Plante Petite feuille","Kleine Blattpflanze","Planta Hojita","Planta Folhinha","Pianta Fogliolina");
        add("plant_desc","A little green friend by the window.","Un petit coin de verdure près de la fenêtre.","Ein grüner Freund am Fenster.","Una amiga verde junto a la ventana.","Uma amiga verde junto à janela.","Un’amica verde accanto alla finestra.");
        add("rug_name","Soft orbit rug","Tapis Orbite douce","Sanfter Orbitteppich","Alfombra Órbita suave","Tapete Órbita suave","Tappeto Orbita morbida");
        add("rug_desc","A soft landing. Furniture can sit on top.","Un sol tout doux. Les meubles peuvent se poser dessus.","Weicher Boden. Möbel passen darauf.","Un suelo suave. Puedes poner muebles encima.","Um chão macio. Podes pôr móveis por cima.","Un appoggio morbido. Puoi metterci sopra i mobili.");
        add("table_name","Teatime table","Table Heure du thé","Teezeittisch","Mesa Hora del té","Mesa Hora do chá","Tavolo Ora del tè");
        add("table_desc","A round table for a quiet little break.","Une table ronde pour une petite pause.","Ein runder Tisch für eine ruhige Pause.","Una mesa redonda para una pausa tranquila.","Uma mesa redonda para uma pausa tranquila.","Un tavolo rotondo per una pausa tranquilla.");
        add("bookshelf_name","Story nook shelf","Bibliothèque Coin d’histoire","Geschichtenregal","Estante Rincón de cuentos","Estante Cantinho de histórias","Scaffale Angolo di storie");
        add("bookshelf_desc","Tiny books, plenty of adventures.","De petits livres, plein d’aventures.","Kleine Bücher, viele Abenteuer.","Libritos con muchas aventuras.","Livrinhos com muitas aventuras.","Piccoli libri, tante avventure.");
        add("cushion_name","Suncrumb cushion","Coussin Miette de soleil","Sonnenkrümelkissen","Cojín Miguita de sol","Almofada Migalha de sol","Cuscino Briciola di sole");
        add("cushion_desc","A sunny spot to settle down.","Une petite place au soleil.","Ein sonniger Platz zum Ausruhen.","Un rincón soleado para descansar.","Um cantinho ao sol para descansar.","Un posticino al sole per riposare.");
        add("plush_name","Pocket star plush","Peluche Étoile de poche","Taschensternplüschtier","Peluche Estrella de bolsillo","Pelúcia Estrela de bolso","Peluche Stellina tascabile");
        add("plush_desc","A little star that gives very good hugs.","Une petite étoile qui adore les câlins.","Ein kleiner Stern für große Umarmungen.","Una estrellita que da buenos abrazos.","Uma estrelinha que dá bons abraços.","Una stellina che abbraccia forte.");
        add("gate_lamp_name","Clicklight lantern","Lanterne Clic-lumière","Klicklichtlaterne","Farol Luz de clic","Lanterna Luz de clique","Lanterna Luce a clic");
        add("gate_lamp_desc","The puzzle gate’s little click, brought home.","Le petit clic des portes du puzzle, à la maison.","Das Klicken der Rätseltore für dein Zuhause.","El clic de las puertas del puzle, en casa.","O clique das portas do puzzle, em casa.","Il clic dei varchi del rompicapo, a casa.");
        add("mobile_name","Drifting arrows mobile","Mobile Flèches en balade","Schwebepfeilmobile","Móvil Flechas al viento","Móbile Setas à deriva","Mobile Frecce in volo");
        add("mobile_desc","Little arrows dance gently overhead.","De petites flèches dansent doucement.","Kleine Pfeile tanzen sanft über dir.","Flechitas que bailan suavemente.","Setinhas que dançam devagar.","Freccette che danzano dolcemente.");
        add("stool_name","Pebble stool","Tabouret Petit galet","Kieselstein-Hocker","Taburete Guijarro","Banco Seixinho","Sgabello Sassolino");
        add("stool_desc","A small perch for a quiet moment.","Un petit siège pour un moment tranquille.","Ein kleiner Sitz für einen ruhigen Moment.","Un asiento para un momento de calma.","Um assento para um momento de calma.","Una seduta per un momento tranquillo.");
        add("wallpaper_cream_desc","Soft vanilla for a warm little room.","Une vanille douce pour une pièce chaleureuse.","Sanfte Vanille für ein gemütliches Zimmer.","Vainilla suave para una habitación acogedora.","Baunilha suave para um quarto acolhedor.","Vaniglia delicata per una stanza accogliente.");
        add("wallpaper_sage_desc","A quiet green, like a little garden.","Un vert calme, comme un petit jardin.","Ein ruhiges Grün wie ein kleiner Garten.","Un verde tranquilo, como un jardincito.","Um verde tranquilo, como um jardim.","Un verde tranquillo, come un giardinetto.");
        add("wallpaper_dusk_desc","A gentle violet for the end of the day.","Un violet tendre pour la fin de journée.","Sanftes Violett für den Tagesausklang.","Un violeta suave para el final del día.","Um violeta suave para o fim do dia.","Un viola tenue per la fine del giorno.");
        add("wallpaper_peach_desc","A touch of peach to brighten your home.","Une touche de pêche pour égayer la maison.","Ein Hauch Pfirsich für dein Zuhause.","Un toque de melocotón para alegrar tu casa.","Um toque de pêssego para alegrar a casa.","Un tocco di pesca per rallegrare la casa.");
        add("floor_oak_desc","Honey-coloured boards under little paws.","Des lames couleur miel sous les petites pattes.","Honigfarbene Dielen unter kleinen Pfoten.","Tablas color miel bajo las patitas.","Tábuas cor de mel sob as patinhas.","Assi color miele sotto le zampette.");
        add("floor_cloud_desc","A light floor for a room in the clouds.","Un sol clair pour une pièce dans les nuages.","Ein heller Boden für ein Wolkenzimmer.","Un suelo claro para una habitación en las nubes.","Um chão claro para um quarto nas nuvens.","Un pavimento chiaro per una stanza tra le nuvole.");
        add("floor_rose_desc","A soft rose finish for your favourite corner.","Une finition rose tendre pour ton coin préféré.","Sanftes Rosa für deine Lieblingsecke.","Un rosa suave para tu rincón favorito.","Um rosa suave para o teu cantinho favorito.","Un rosa tenue per il tuo angolo preferito.");
        add("floor_mint_desc","A fresh mint floor, ready for your ideas.","Un sol menthe frais pour toutes tes idées.","Frisches Minzgrün für deine Ideen.","Un suelo menta fresco para tus ideas.","Um chão menta fresco para as tuas ideias.","Un pavimento menta fresco per le tue idee.");
        add("wallpaper_cream_name","Vanilla walls","Murs Vanille","Vanillewände","Paredes Vainilla","Paredes Baunilha","Pareti Vaniglia");
        add("wallpaper_sage_name","Sage walls","Murs Sauge","Salbeiwände","Paredes Salvia","Paredes Sálvia","Pareti Salvia");
        add("wallpaper_dusk_name","Dusk walls","Murs Crépuscule","Dämmerungswände","Paredes Crepúsculo","Paredes Crepúsculo","Pareti Crepuscolo");
        add("wallpaper_peach_name","Peach walls","Murs Pêche","Pfirsichwände","Paredes Melocotón","Paredes Pêssego","Pareti Pesca");
        add("floor_oak_name","Honey oak floor","Parquet Chêne miel","Honigeichenboden","Suelo Roble miel","Chão Carvalho mel","Pavimento Rovere miele");
        add("floor_cloud_name","Cloud floor","Sol Nuage","Wolkenboden","Suelo Nube","Chão Nuvem","Pavimento Nuvola");
        add("floor_rose_name","Rose floor","Sol Rose","Rosenboden","Suelo Rosa","Chão Rosa","Pavimento Rosa");
        add("floor_mint_name","Mint floor","Sol Menthe","Minzboden","Suelo Menta","Chão Menta","Pavimento Menta");
    }
    private HomeStrings(){}
    private static void alias(String key,String original){TEXT.put(key,TEXT.get(original).clone());}
    private static void add(String key,String... values){if(values.length!=6)throw new IllegalArgumentException("Six home translations required");TEXT.put(key,values);}
    public static boolean has(String key){return TEXT.containsKey(key);}
    public static Set<String> keys(){return Collections.unmodifiableSet(TEXT.keySet());}
    public static String get(String language,String key,Object... args){
        String[] values=TEXT.get(key);if(values==null)return key==null?"":key.replace('_',' ');String code=Strings.normalize(language);int index=0;for(int i=0;i<CODES.length;i++)if(CODES[i].equals(code)){index=i;break;}
        String value=values[index];try{return args.length==0?value:String.format(java.util.Locale.forLanguageTag(code),value,args);}catch(RuntimeException badFormat){return value;}
    }
    public static String name(String language,String id){return get(language,id+"_name");}
    public static String description(String language,String id){return get(language,id+"_desc");}
}
