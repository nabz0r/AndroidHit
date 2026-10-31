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
        add("title","My home","Ma maison","Mein Zuhause","Mi hogar","A minha casa","La mia casa");
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

        add("room_living","Living room","Salon","Wohnzimmer","Salón","Sala","Soggiorno");
        add("room_bedroom","Bedroom","Chambre","Schlafzimmer","Dormitorio","Quarto","Camera");
        add("room_garden","Garden","Jardin","Garten","Jardín","Jardim","Giardino");
        add("room_kitchen","Kitchen","Cuisine","Küche","Cocina","Cozinha","Cucina");
        add("rooms","Rooms","Pièces","Räume","Estancias","Divisões","Stanze");
        add("camera_hint","Pinch to zoom · Drag to explore","Pince pour zoomer · Glisse pour explorer","Zum Zoomen zusammenziehen · Zum Erkunden ziehen","Pellizca para ampliar · Arrastra para explorar","Aperta para ampliar · Arrasta para explorar","Pizzica per ingrandire · Trascina per esplorare");
        add("camera_reset","Whole room","Vue d’ensemble","Ganzer Raum","Vista completa","Vista completa","Vista completa");
        add("room_size","10 × 10 tiles to make your own","100 cases pour créer ton espace","100 Felder für deine Ideen","100 casillas para crear tu espacio","100 casas para criares o teu espaço","100 caselle per creare il tuo spazio");
        add("aquarium_name","Moon reef aquarium","Aquarium Récif de lune","Mondriff-Aquarium","Acuario Arrecife lunar","Aquário Recife lunar","Acquario Scogliera lunare");
        add("aquarium_desc","A broad glass tank with coral and little fish.","Un grand bassin vitré, des coraux et de petits poissons.","Ein breites Glasbecken mit Korallen und kleinen Fischen.","Una gran pecera con corales y pececitos.","Um aquário largo com corais e peixinhos.","Una grande vasca con coralli e pesciolini.");
        add("piano_name","Midnight grand piano","Piano à queue Minuit","Mitternachtsflügel","Piano de cola Medianoche","Piano de cauda Meia-noite","Pianoforte a coda Mezzanotte");
        add("piano_desc","A raised lid, ivory keys and room for a melody.","Un couvercle ouvert, des touches claires et une mélodie.","Offener Deckel, helle Tasten und Platz für eine Melodie.","Tapa abierta, teclas claras y espacio para una melodía.","Tampa aberta, teclas claras e espaço para uma melodia.","Coperchio aperto, tasti chiari e spazio per una melodia.");
        add("greenhouse_name","Glass garden greenhouse","Serre Jardin de verre","Glasgarten-Gewächshaus","Invernadero Jardín de cristal","Estufa Jardim de vidro","Serra Giardino di vetro");
        add("greenhouse_desc","A little glass house full of climbing leaves.","Une petite maison de verre pleine de plantes grimpantes.","Ein Glashaus voller rankender Blätter.","Una casita de cristal llena de plantas trepadoras.","Uma casinha de vidro cheia de trepadeiras.","Una casetta di vetro piena di rampicanti.");
        add("library_name","Wall of stories","Mur de livres","Geschichtenwand","Muro de historias","Parede de histórias","Parete di storie");
        add("library_desc","Three tall shelves with many colourful book spines.","Trois hautes étagères aux livres multicolores.","Drei hohe Regale voller bunter Buchrücken.","Tres estantes altos con libros de muchos colores.","Três estantes altas com livros de muitas cores.","Tre scaffali alti con libri di tanti colori.");
        add("bay_window_name","Sunrise bay window","Baie vitrée Aurore","Morgenrot-Erkerfenster","Ventanal Amanecer","Janela panorâmica Aurora","Vetrata Aurora");
        add("bay_window_desc","A wide arched view that opens up the room.","Une grande arche vitrée qui ouvre la pièce.","Ein breites Bogenfenster öffnet den Raum.","Un gran ventanal arqueado que abre la estancia.","Uma grande janela em arco que abre a divisão.","Un’ampia vetrata ad arco apre la stanza.");
        add("telescope_name","Stargazer telescope","Télescope Veille d’étoiles","Sternengucker-Teleskop","Telescopio Mirador estelar","Telescópio Olhar estelar","Telescopio Guarda stelle");
        add("telescope_desc","A brass telescope on a tall three-legged stand.","Un télescope en laiton sur son grand trépied.","Ein Messingteleskop auf einem hohen Dreibein.","Un telescopio de latón sobre un trípode alto.","Um telescópio de latão num tripé alto.","Un telescopio in ottone su un alto treppiede.");
        add("fireplace_name","Ember hearth","Cheminée Braise douce","Glutkamin","Chimenea Brasa suave","Lareira Brasa suave","Camino Brace dolce");
        add("fireplace_desc","An arched hearth with a warm bed of embers.","Un foyer en arche et des braises chaleureuses.","Ein Bogenkamin mit einem warmen Glutbett.","Un hogar arqueado con brasas cálidas.","Uma lareira em arco com brasas quentes.","Un focolare ad arco con calde braci.");
        add("desk_name","Letter-writing desk","Bureau Correspondance","Schreibtisch für Briefe","Escritorio Correspondencia","Secretária Correspondência","Scrivania Corrispondenza");
        add("desk_desc","An open notebook and a lamp for quiet ideas.","Un carnet ouvert et une lampe pour tes idées.","Ein offenes Heft und eine Lampe für ruhige Ideen.","Un cuaderno abierto y una lámpara para tus ideas.","Um caderno aberto e um candeeiro para as tuas ideias.","Un quaderno aperto e una lampada per le tue idee.");
        add("easel_name","Daydream easel","Chevalet Rêverie","Tagtraum-Staffelei","Caballete Ensueño","Cavalete Devaneio","Cavalletto Sogno");
        add("easel_desc","A painted canvas and a bright little palette.","Une toile peinte et une petite palette colorée.","Eine bemalte Leinwand und eine bunte Palette.","Un lienzo pintado y una pequeña paleta de colores.","Uma tela pintada e uma pequena paleta colorida.","Una tela dipinta e una piccola tavolozza colorata.");
        add("record_player_name","Vinyl corner","Tourne-disque Vinyle","Schallplattenecke","Tocadiscos Vinilo","Gira-discos Vinil","Giradischi Vinile");
        add("record_player_desc","A spinning record in its open wooden case.","Un disque dans son coffret de bois ouvert.","Eine Schallplatte in einem offenen Holzkasten.","Un disco en su caja de madera abierta.","Um disco na sua caixa de madeira aberta.","Un disco nella sua custodia di legno aperta.");
        add("arcade_name","Starlight arcade","Borne Arcade étoilée","Sternenlicht-Spielautomat","Recreativa Luz estelar","Máquina Arcade estelar","Cabinato Luce stellare");
        add("arcade_desc","A bright screen, chunky buttons and a joystick.","Un écran lumineux, de gros boutons et un joystick.","Ein heller Bildschirm, große Tasten und ein Joystick.","Una pantalla luminosa, botones grandes y un joystick.","Um ecrã luminoso, botões grandes e um joystick.","Uno schermo luminoso, grandi pulsanti e un joystick.");
        add("terrarium_name","Glass leaf terrarium","Terrarium Feuille de verre","Glasblatt-Terrarium","Terrario Hoja de cristal","Terrário Folha de vidro","Terrario Foglia di vetro");
        add("terrarium_desc","A geometric glass garden for tiny plants.","Un jardin de verre géométrique pour les petites plantes.","Ein geometrischer Glasgarten für kleine Pflanzen.","Un jardín geométrico de cristal para plantas pequeñas.","Um jardim geométrico de vidro para pequenas plantas.","Un giardino geometrico di vetro per piccole piante.");
        add("bonsai_name","Cloud branch bonsai","Bonsaï Branche de nuage","Wolkenzweig-Bonsai","Bonsái Rama de nube","Bonsai Ramo de nuvem","Bonsai Ramo di nuvola");
        add("bonsai_desc","A curved trunk with three soft green crowns.","Un tronc courbé et trois couronnes de feuillage.","Ein gebogener Stamm mit drei grünen Kronen.","Un tronco curvo con tres copas verdes.","Um tronco curvo com três copas verdes.","Un tronco curvo con tre chiome verdi.");
        add("cactus_name","Desert cactus","Cactus Désert","Wüstenkaktus","Cactus Desierto","Cato Deserto","Cactus Deserto");
        add("cactus_desc","Two tall arms and a tiny pink flower.","Deux grands bras et une petite fleur rose.","Zwei hohe Arme und eine kleine rosa Blüte.","Dos brazos altos y una florecita rosa.","Dois braços altos e uma flor cor-de-rosa.","Due braccia alte e un piccolo fiore rosa.");
        add("cherry_tree_name","Blossom canopy","Cerisier Nuage de fleurs","Kirschblütendach","Cerezo Nube de flores","Cerejeira Nuvem de flores","Ciliegio Nuvola di fiori");
        add("cherry_tree_desc","A broad pink crown that changes the garden skyline.","Une large couronne rose qui transforme le jardin.","Eine breite rosa Krone verändert die Gartensilhouette.","Una gran copa rosa que transforma el jardín.","Uma grande copa rosa que transforma o jardim.","Un’ampia chioma rosa che trasforma il giardino.");
        add("fountain_name","Ripple fountain","Fontaine Ondine","Wellenbrunnen","Fuente Ondina","Fonte Ondina","Fontana Ondina");
        add("fountain_desc","Two stone bowls and a little jet of water.","Deux vasques de pierre et un petit jet d’eau.","Zwei Steinschalen und ein kleiner Wasserstrahl.","Dos cuencos de piedra y un pequeño chorro de agua.","Duas taças de pedra e um pequeno jato de água.","Due vasche di pietra e un piccolo getto d’acqua.");
        add("pond_name","Water lily pond","Bassin Nénuphar","Seerosenteich","Estanque Nenúfar","Lago Nenúfar","Laghetto Ninfea");
        add("pond_desc","An irregular pond with stepping stones and lily pads.","Un bassin irrégulier, des pierres et des nénuphars.","Ein unregelmäßiger Teich mit Trittsteinen und Seerosen.","Un estanque irregular con piedras y nenúfares.","Um lago irregular com pedras e nenúfares.","Un laghetto irregolare con pietre e ninfee.");
        add("hammock_name","Lazy afternoon hammock","Hamac Sieste d’été","Nachmittagshängematte","Hamaca Siesta de verano","Rede Sesta de verão","Amaca Siesta estiva");
        add("hammock_desc","A curved cloth bed suspended between two posts.","Une toile courbée suspendue entre deux poteaux.","Ein gebogenes Stoffbett zwischen zwei Pfosten.","Una cama de tela curva entre dos postes.","Uma cama de tecido curva entre dois postes.","Un letto di tela curvo sospeso tra due pali.");
        add("swing_name","Garden swing","Balançoire du jardin","Gartenschaukel","Columpio del jardín","Baloiço do jardim","Altalena del giardino");
        add("swing_desc","A hanging seat under a tall wooden frame.","Un siège suspendu sous un grand cadre de bois.","Ein Hängesitz unter einem hohen Holzrahmen.","Un asiento colgante bajo un marco alto de madera.","Um assento suspenso numa armação alta de madeira.","Un sedile sospeso sotto un’alta struttura di legno.");
        add("bench_name","Leaf lane bench","Banc Allée des feuilles","Blätterweg-Bank","Banco Sendero de hojas","Banco Caminho das folhas","Panchina Sentiero di foglie");
        add("bench_desc","A slatted backrest with curved garden arms.","Un dossier à lattes et des accoudoirs arrondis.","Eine Lattenlehne mit gebogenen Armstützen.","Un respaldo de listones y brazos curvos.","Um encosto de ripas com braços curvos.","Uno schienale a doghe con braccioli curvi.");
        add("picnic_name","Picnic table","Table de pique-nique","Picknicktisch","Mesa de pícnic","Mesa de piquenique","Tavolo da picnic");
        add("picnic_desc","A long table, two benches and a gingham cloth.","Une grande table, deux bancs et une nappe à carreaux.","Ein langer Tisch, zwei Bänke und ein Karotuch.","Una mesa larga, dos bancos y un mantel de cuadros.","Uma mesa comprida, dois bancos e uma toalha aos quadrados.","Un lungo tavolo, due panche e una tovaglia a quadri.");
        add("flowerbed_name","Welcome flowerbed","Parterre Bienvenue","Willkommensblumenbeet","Parterre Bienvenida","Canteiro Boas-vindas","Aiuola Benvenuto");
        add("flowerbed_desc","A raised bed full of large colourful blossoms.","Un parterre surélevé aux grandes fleurs colorées.","Ein Hochbeet voller großer bunter Blüten.","Un bancal elevado lleno de grandes flores de colores.","Um canteiro elevado cheio de grandes flores coloridas.","Un’aiuola rialzata piena di grandi fiori colorati.");
        add("garden_path_name","Pebble path","Sentier de galets","Kieselweg","Sendero de guijarros","Caminho de seixos","Sentiero di ciottoli");
        add("garden_path_desc","Flat pale stones that you can walk across.","Des pierres claires et plates où tu peux marcher.","Flache helle Steine, über die du gehen kannst.","Piedras claras y planas por las que puedes caminar.","Pedras claras e lisas por onde podes caminhar.","Pietre chiare e piatte su cui camminare.");
        add("lantern_name","Evening lantern","Lanterne du soir","Abendlaterne","Farol del atardecer","Lanterna do serão","Lanterna della sera");
        add("lantern_desc","A golden light behind a little domed frame.","Une lumière dorée dans un petit cadre bombé.","Goldenes Licht in einem kleinen gewölbten Rahmen.","Una luz dorada tras un pequeño marco abovedado.","Uma luz dourada numa pequena armação abobadada.","Una luce dorata in una piccola cornice a cupola.");
        add("canopy_bed_name","Cloud canopy bed","Lit à baldaquin Nuage","Wolkenhimmelbett","Cama con dosel Nube","Cama de dossel Nuvem","Letto a baldacchino Nuvola");
        add("canopy_bed_desc","Four tall posts and flowing curtains around the bed.","Quatre grands poteaux et de doux rideaux autour du lit.","Vier hohe Pfosten und weiche Vorhänge ums Bett.","Cuatro postes altos y cortinas suaves alrededor de la cama.","Quatro postes altos e cortinas suaves à volta da cama.","Quattro alte colonne e morbide tende intorno al letto.");
        add("wardrobe_name","Ribbon wardrobe","Armoire Ruban","Schleifenkleiderschrank","Armario Lazo","Roupeiro Laço","Armadio Nastro");
        add("wardrobe_desc","Tall double doors with a little ribbon motif.","Deux hautes portes et un petit motif de ruban.","Hohe Doppeltüren mit einem kleinen Schleifenmotiv.","Dos puertas altas con un pequeño lazo.","Duas portas altas com um pequeno motivo de laço.","Due ante alte con un piccolo motivo a nastro.");
        add("bath_name","Bubble bath","Baignoire Bulle","Schaumbadewanne","Bañera Burbuja","Banheira Bolha","Vasca Bollicina");
        add("bath_desc","An oval tub on little feet with a cloud of bubbles.","Une baignoire ovale sur pieds et un nuage de mousse.","Eine ovale Wanne auf Füßen mit einer Schaumwolke.","Una bañera ovalada con patas y una nube de espuma.","Uma banheira oval com pés e uma nuvem de espuma.","Una vasca ovale su piedini con una nuvola di schiuma.");
        add("mirror_name","Moonlit standing mirror","Miroir Pleine lune","Mondlicht-Standspiegel","Espejo Luz de luna","Espelho Luar","Specchio Chiaro di luna");
        add("mirror_desc","A tall oval mirror in a freestanding gold frame.","Un grand miroir ovale dans son cadre doré sur pied.","Ein hoher ovaler Spiegel in einem goldenen Standrahmen.","Un espejo ovalado alto en un marco dorado de pie.","Um espelho oval alto numa moldura dourada de pé.","Un alto specchio ovale in una cornice dorata da terra.");
        add("kitchen_name","Little chef kitchen","Cuisine Petit chef","Kleine Chefküche","Cocina Pequeño chef","Cozinha Pequeno chefe","Cucina Piccolo chef");
        add("kitchen_desc","A cooker, sink and cabinets along one wide counter.","Des plaques, un évier et des placards sur un grand plan de travail.","Herd, Spüle und Schränke an einer breiten Arbeitsplatte.","Fogones, fregadero y armarios en una encimera ancha.","Fogão, lava-loiça e armários numa bancada larga.","Fornelli, lavello e mobili su un ampio piano.");
        add("chess_table_name","Checkmate table","Table Échec et mat","Schachmatttisch","Mesa Jaque mate","Mesa Xeque-mate","Tavolo Scacco matto");
        add("chess_table_desc","A chequered board ready for a quiet game.","Un plateau à damier pour une partie tranquille.","Ein Schachbrett für eine ruhige Partie.","Un tablero de cuadros para una partida tranquila.","Um tabuleiro aos quadrados para uma partida tranquila.","Una scacchiera per una partita tranquilla.");
    }
    static {
        add("tea_station_name","Cloud tea station","Bar à thé Nuage","Wolken-Teestation","Rincón de té Nube","Estação de chá Nuvem","Angolo del tè Nuvola");
        add("tea_station_desc","A steaming blue teapot and cups. Your companion can come over for tea.","Une théière bleue et des tasses. Ton compagnon peut venir boire un thé.","Eine blaue Teekanne mit Tassen. Dein Begleiter kann zum Tee kommen.","Una tetera azul con tazas. Tu compañero puede venir a tomar té.","Um bule azul com chávenas. O companheiro pode vir tomar chá.","Una teiera blu con tazze. Il compagno può venire a bere un tè.");
        add("herb_cart_name","Little herb cart","Chariot des aromates","Kräuterwagen","Carrito de hierbas","Carrinho de ervas","Carrello delle erbe");
        add("herb_cart_desc","A movable kitchen garden. A free starter object to arrange in any room.","Un petit jardin sur roues. Un objet de départ gratuit à placer dans la pièce de ton choix.","Ein Küchengarten auf Rädern. Ein kostenloses Startobjekt für jeden Raum.","Un pequeño huerto con ruedas. Objeto inicial gratuito para cualquier estancia.","Uma horta sobre rodas. Objeto inicial gratuito para qualquer divisão.","Un orto su ruote. Oggetto iniziale gratuito per qualsiasi stanza.");
        add("bubble_fridge_name","Bubble fridge","Réfrigérateur Bulle","Blasenkühlschrank","Frigorífico Burbuja","Frigorífico Bolha","Frigorifero Bolla");
        add("bubble_fridge_desc","Rounded blue doors, a little flower and a fresh kitchen silhouette.","Des portes bleues arrondies et une petite fleur pour donner du caractère à la cuisine.","Runde blaue Türen und eine kleine Blume für deine Küche.","Puertas azules redondeadas y una flor para tu cocina.","Portas azuis arredondadas e uma flor para a cozinha.","Porte blu arrotondate e un fiore per la cucina.");
        add("cooking_island_name","Sunny cooking island","Îlot des petits plats","Kochinsel Sonnenschein","Isla de cocina Sol","Ilha de cozinha Sol","Isola di cucina Sole");
        add("cooking_island_desc","Fruit, a pot and a pastry board. Invite your companion over for a tea break.","Des fruits, une marmite et des pâtisseries. Invite ton compagnon à faire une pause thé.","Obst, Topf und Gebäck. Lade deinen Begleiter zu einer Teepause ein.","Fruta, olla y pasteles. Invita al compañero a una pausa de té.","Fruta, panela e doces. Convida o companheiro para uma pausa de chá.","Frutta, pentola e dolci. Invita il compagno a una pausa tè.");
        add("seed_shelf_name","Seedling shelf","Étagère des semis","Setzlingsregal","Estante de semilleros","Estante de sementes","Scaffale dei germogli");
        add("seed_shelf_desc","Seedlings, pots and a watering can. A free starter object for your garden.","Des semis, des pots et un arrosoir. Un objet de départ gratuit pour ton jardin.","Setzlinge, Töpfe und Gießkanne. Ein kostenloses Startobjekt für deinen Garten.","Semilleros, macetas y regadera. Objeto inicial gratuito para el jardín.","Sementes, vasos e regador. Objeto inicial gratuito para o jardim.","Germogli, vasi e annaffiatoio. Oggetto iniziale gratuito per il giardino.");
        add("bloom_arch_name","Bloom arch","Arche Fleur de Luma","Blütenbogen","Arco Flor de Luma","Arco Flor de Luma","Arco Fiore di Luma");
        add("bloom_arch_desc","A white arch wrapped in green leaves and coral flowers. It occupies two floor tiles.","Une arche blanche couverte de feuilles et de fleurs corail. Elle occupe deux cases au sol.","Ein weißer Bogen mit Blättern und korallenfarbenen Blüten. Er belegt zwei Bodenfelder.","Un arco blanco con hojas y flores coral. Ocupa dos casillas.","Um arco branco com folhas e flores coral. Ocupa duas casas.","Un arco bianco con foglie e fiori corallo. Occupa due caselle.");
        add("picnic_name","Picnic corner","Coin pique-nique","Picknickecke","Rincón de pícnic","Cantinho de piquenique","Angolo picnic");
        add("picnic_desc","A blue checked cloth, a basket and fresh fruit for a quiet break.","Une nappe à carreaux bleus, un panier et des fruits pour une pause tranquille.","Eine blau karierte Decke, Korb und Obst für eine ruhige Pause.","Un mantel azul de cuadros, una cesta y fruta para descansar.","Uma toalha azul aos quadrados, cesto e fruta para uma pausa.","Una tovaglia a quadri blu, un cesto e frutta per una pausa.");
        add("mirror_desc","A tall blue oval mirror in a rounded white frame.","Un grand miroir ovale bleu dans son cadre blanc arrondi.","Ein hoher ovaler blauer Spiegel in einem runden weißen Rahmen.","Un espejo ovalado azul en un marco blanco redondeado.","Um espelho oval azul numa moldura branca arredondada.","Uno specchio ovale blu con cornice bianca arrotondata.");
        add("wardrobe_desc","Rounded white doors with blue glass and clothes inside.","Des portes blanches arrondies, du verre bleu et les vêtements à l’intérieur.","Runde weiße Türen mit blauem Glas und Kleidung dahinter.","Puertas blancas redondeadas con cristal azul y ropa dentro.","Portas brancas arredondadas com vidro azul e roupa no interior.","Porte bianche arrotondate con vetro blu e abiti all’interno.");
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
