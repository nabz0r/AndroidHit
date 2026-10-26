package com.androidhit.lumadrift.data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** All visible game copy, with complete English, French, German, Spanish, Portuguese and Italian sets. */
public final class Strings {
    public static final String[] LANGUAGE_CODES = {"auto", "en", "fr", "de", "es", "pt", "it"};
    public static final String[] LANGUAGE_NAMES = {"System", "English", "Français", "Deutsch", "Español", "Português", "Italiano"};
    private static final Map<String, String[]> TEXT = new HashMap<>();
    private static final String[] CODES = {"en", "fr", "de", "es", "pt", "it"};

    static {
        add("app_name", "LUMA DRIFT", "LUMA DRIFT", "LUMA DRIFT", "LUMA DRIFT", "LUMA DRIFT", "LUMA DRIFT");
        add("tagline", "A little light. A clever escape.", "Une petite lumière. Une belle échappée.", "Ein kleines Licht. Ein kluger Ausweg.", "Una pequeña luz. Una gran escapada.", "Uma pequena luz. Uma fuga inteligente.", "Una piccola luce. Una fuga ingegnosa.");
        add("home", "Home", "Accueil", "Start", "Inicio", "Início", "Home");
        add("play", "Play", "Jouer", "Spielen", "Jugar", "Jogar", "Gioca");
        add("journey", "Journey", "Voyage", "Reise", "Viaje", "Jornada", "Viaggio");
        add("shop", "Shop", "Boutique", "Shop", "Tienda", "Loja", "Negozio");
        add("collection", "Collection", "Collection", "Sammlung", "Colección", "Coleção", "Collezione");
        add("events", "Events", "Événements", "Events", "Eventos", "Eventos", "Eventi");
        add("settings", "Settings", "Réglages", "Einstellungen", "Ajustes", "Definições", "Impostazioni");
        add("chapter", "Chapter", "Chapitre", "Kapitel", "Capítulo", "Capítulo", "Capitolo");
        add("chapter_number", "Chapter %d", "Chapitre %d", "Kapitel %d", "Capítulo %d", "Capítulo %d", "Capitolo %d");
        add("level", "Level", "Niveau", "Level", "Nivel", "Nível", "Livello");
        add("level_number", "Level %d", "Niveau %d", "Level %d", "Nivel %d", "Nível %d", "Livello %d");
        add("coins", "Coins", "Pièces", "Münzen", "Monedas", "Moedas", "Monete");
        add("stars", "Stars", "Étoiles", "Sterne", "Estrellas", "Estrelas", "Stelle");
        add("continue", "Continue", "Continuer", "Weiter", "Continuar", "Continuar", "Continua");
        add("start", "Start", "Commencer", "Starten", "Empezar", "Começar", "Inizia");
        add("next", "Next", "Suivant", "Weiter", "Siguiente", "Seguinte", "Avanti");
        add("retry", "Try again", "Rejouer", "Erneut spielen", "Reintentar", "Tentar de novo", "Riprova");
        add("restart", "Restart", "Recommencer", "Neustart", "Reiniciar", "Reiniciar", "Ricomincia");
        add("back", "Back", "Retour", "Zurück", "Volver", "Voltar", "Indietro");
        add("close", "Close", "Fermer", "Schließen", "Cerrar", "Fechar", "Chiudi");
        add("pause", "Pause", "Pause", "Pause", "Pausa", "Pausa", "Pausa");
        add("paused", "Take a breath", "Une petite pause", "Kurz durchatmen", "Tómate un respiro", "Respira um pouco", "Prenditi una pausa");
        add("resume", "Resume", "Reprendre", "Fortsetzen", "Continuar", "Retomar", "Riprendi");
        add("exit", "Leave level", "Quitter le niveau", "Level verlassen", "Salir del nivel", "Sair do nível", "Esci dal livello");
        add("buy", "Get", "Obtenir", "Holen", "Obtener", "Obter", "Ottieni");
        add("owned", "Owned", "Acquis", "Im Besitz", "En propiedad", "Adquirido", "Posseduto");
        add("equip", "Equip", "Équiper", "Auswählen", "Equipar", "Equipar", "Equipaggia");
        add("equipped", "Equipped", "Équipé", "Ausgewählt", "Equipado", "Equipado", "Equipaggiato");
        add("not_enough", "Earn more coins by playing", "Joue pour gagner plus de pièces", "Spiele, um mehr Münzen zu verdienen", "Juega para ganar más monedas", "Joga para ganhar mais moedas", "Gioca per guadagnare altre monete");
        add("locked", "Locked", "Verrouillé", "Gesperrt", "Bloqueado", "Bloqueado", "Bloccato");
        add("unlock_previous", "Complete the previous puzzle first", "Termine d’abord le plateau précédent", "Löse zuerst das vorherige Rätsel", "Completa primero el puzle anterior", "Completa primeiro o puzzle anterior", "Completa prima il rompicapo precedente");
        add("free", "Free", "Offert", "Gratis", "Gratis", "Grátis", "Gratis");
        add("daily", "Daily Drift", "Virée du jour", "Täglicher Drift", "Ruta diaria", "Jornada diária", "Viaggio del giorno");
        add("weekly", "Weekly Voyage", "Voyage de la semaine", "Wochenreise", "Viaje semanal", "Viagem semanal", "Viaggio settimanale");
        add("event_offline", "Local date · Fresh puzzles · Fully offline", "Date locale · Nouveaux plateaux · Hors ligne", "Lokales Datum · Neue Rätsel · Offline", "Fecha local · Nuevos puzles · Sin conexión", "Data local · Novos puzzles · Offline", "Data locale · Nuovi rompicapi · Offline");
        add("daily_desc", "3 puzzles. A little daily ritual.", "3 plateaux. Ton petit rituel quotidien.", "3 Rätsel. Dein kleines tägliches Ritual.", "3 puzles. Tu pequeño ritual diario.", "3 puzzles. O teu pequeno ritual diário.", "3 rompicapi. Il tuo piccolo rito quotidiano.");
        add("weekly_desc", "7 stages. One luminous adventure.", "7 étapes. Une aventure lumineuse.", "7 Etappen. Ein leuchtendes Abenteuer.", "7 etapas. Una aventura luminosa.", "7 etapas. Uma aventura luminosa.", "7 tappe. Un’avventura luminosa.");
        add("event_complete", "Event complete", "Événement terminé", "Event abgeschlossen", "Evento completado", "Evento concluído", "Evento completato");
        add("claim", "Collect", "Récupérer", "Einsammeln", "Recoger", "Recolher", "Raccogli");
        add("claimed", "Collected", "Récupéré", "Eingesammelt", "Recogido", "Recolhido", "Raccolto");
        add("bonus", "Completion bonus", "Bonus de fin", "Abschlussbonus", "Bono por completar", "Bónus de conclusão", "Bonus completamento");
        add("refresh_daily", "New puzzles at local midnight", "Nouveaux plateaux à minuit, heure locale", "Neue Rätsel um Mitternacht Ortszeit", "Puzles nuevos a medianoche, hora local", "Novos puzzles à meia-noite local", "Nuovi rompicapi a mezzanotte locale");
        add("refresh_weekly", "A new voyage every Monday", "Un nouveau voyage chaque lundi", "Jeden Montag eine neue Reise", "Un viaje nuevo cada lunes", "Uma nova viagem a cada segunda-feira", "Un nuovo viaggio ogni lunedì");
        add("hint", "Spark", "Étincelle", "Funke", "Chispa", "Faísca", "Scintilla");
        add("vision", "Vision", "Vision", "Weitblick", "Visión", "Visão", "Visione");
        add("flow", "Flow", "Élan", "Fluss", "Flujo", "Fluxo", "Flusso");
        add("booster_hint", "Spark", "Étincelle", "Funke", "Chispa", "Faísca", "Scintilla");
        add("booster_vision", "Vision", "Vision", "Weitblick", "Visión", "Visão", "Visione");
        add("booster_flow", "Flow", "Élan", "Fluss", "Flujo", "Fluxo", "Flusso");
        add("booster_hint_desc", "Reveal one safe move", "Révèle un coup sûr", "Zeigt einen sicheren Zug", "Revela una jugada segura", "Revela uma jogada segura", "Rivela una mossa sicura");
        add("booster_vision_desc", "Highlight all free pieces for 8 seconds", "Éclaire les pièces libres pendant 8 secondes", "Markiert 8 Sekunden lang alle freien Teile", "Resalta las piezas libres durante 8 segundos", "Destaca as peças livres durante 8 segundos", "Evidenzia i pezzi liberi per 8 secondi");
        add("booster_flow_desc", "Play 3 safe moves automatically", "Joue 3 coups sûrs automatiquement", "Spielt automatisch 3 sichere Züge", "Juega 3 movimientos seguros automáticamente", "Joga 3 movimentos seguros automaticamente", "Esegue automaticamente 3 mosse sicure");
        add("no_boosters", "No items left. Find more in the shop.", "Plus d’objets. Retrouve-les en boutique.", "Keine Extras übrig. Mehr gibt es im Shop.", "No quedan objetos. Consigue más en la tienda.", "Sem itens. Encontra mais na loja.", "Oggetti esauriti. Cercane altri nel negozio.");
        add("no_safe_move", "Undo a move or restart this puzzle", "Annule un coup ou recommence ce plateau", "Nimm einen Zug zurück oder starte neu", "Deshaz una jugada o reinicia el puzle", "Desfaz uma jogada ou reinicia o puzzle", "Annulla una mossa o ricomincia il rompicapo");
        add("undo", "Undo", "Annuler", "Zurücknehmen", "Deshacer", "Desfazer", "Annulla");
        add("undo_free", "Undo is always free", "Annuler un coup est toujours gratuit", "Züge zurücknehmen ist immer gratis", "Deshacer siempre es gratis", "Desfazer é sempre grátis", "Annullare è sempre gratis");
        add("victory", "Lovely drift!", "Belle échappée !", "Wunderbarer Drift!", "¡Gran escapada!", "Que bela jornada!", "Che bella fuga!");
        add("reward", "Reward", "Récompense", "Belohnung", "Recompensa", "Recompensa", "Ricompensa");
        add("moves", "Moves", "Coups", "Züge", "Movimientos", "Jogadas", "Mosse");
        add("perfect", "Perfect!", "Parfait !", "Perfekt!", "¡Perfecto!", "Perfeito!", "Perfetto!");
        add("cleared", "Board cleared", "Plateau libéré", "Brett geleert", "Tablero despejado", "Tabuleiro limpo", "Tabellone completato");
        add("best", "Best", "Record", "Bestwert", "Mejor", "Melhor", "Record");
        add("new_best", "New best!", "Nouveau record !", "Neuer Bestwert!", "¡Nuevo récord!", "Novo recorde!", "Nuovo record!");
        add("replay_reward", "Previously earned rewards stay collected", "Les récompenses déjà gagnées restent acquises", "Bereits verdiente Belohnungen bleiben erhalten", "Las recompensas ya ganadas se conservan", "As recompensas já ganhas ficam guardadas", "Le ricompense già ottenute restano tue");
        add("all_complete", "All 240 levels complete. Try the events!", "Les 240 niveaux sont terminés. Place aux événements !", "Alle 240 Level geschafft. Probiere die Events!", "Has completado los 240 niveles. ¡Prueba los eventos!", "Os 240 níveis estão concluídos. Experimenta os eventos!", "Tutti i 240 livelli completati. Prova gli eventi!");
        add("tutorial_title", "Let the light out", "Libère la lumière", "Lass das Licht frei", "Libera la luz", "Liberta a luz", "Libera la luce");
        add("tutorial_tap", "Tap a piece to send it in its arrow’s direction.", "Touche une pièce pour la faire partir dans le sens de sa flèche.", "Tippe ein Teil an. Es folgt seinem Pfeil.", "Toca una pieza para moverla en la dirección de su flecha.", "Toca numa peça para a mover na direção da seta.", "Tocca un pezzo per farlo uscire nella direzione della freccia.");
        add("tutorial_path", "The path must be clear all the way to the edge.", "Le chemin doit être libre jusqu’au bord.", "Der Weg bis zum Rand muss frei sein.", "El camino hasta el borde debe estar libre.", "O caminho até à borda tem de estar livre.", "Il percorso fino al bordo deve essere libero.");
        add("tutorial_gate", "A glowing switch changes the gates when its piece leaves.", "Une pièce avec un interrupteur lumineux change les portes en sortant.", "Ein leuchtender Schalter verändert die Tore, wenn sein Teil hinausgleitet.", "Una pieza con interruptor cambia las puertas al salir.", "Uma peça com interruptor muda as portas ao sair.", "Un pezzo con un interruttore cambia i varchi quando esce.");
        add("tutorial_plan", "Choose the order. Free every piece to finish.", "Choisis le bon ordre. Fais sortir toutes les pièces.", "Wähle die Reihenfolge. Befreie alle Teile.", "Elige el orden. Libera todas las piezas para terminar.", "Escolhe a ordem. Liberta todas as peças para concluir.", "Scegli l’ordine. Libera tutti i pezzi per finire.");
        add("tutorial_stars", "3 stars: no powers, at most 2 undos. 2 stars: at most 2 powers. Blocked taps have no penalty.", "3 étoiles : sans pouvoir et 2 annulations max. 2 étoiles : 2 pouvoirs max. Les touches bloquées ne pénalisent pas.", "3 Sterne: keine Kräfte, höchstens 2 Rücknahmen. 2 Sterne: höchstens 2 Kräfte. Blockierte Versuche kosten nichts.", "3 estrellas: sin poderes y hasta 2 deshacer. 2 estrellas: hasta 2 poderes. Los toques bloqueados no penalizan.", "3 estrelas: sem poderes e até 2 jogadas desfeitas. 2 estrelas: até 2 poderes. Os toques bloqueados não penalizam.", "3 stelle: nessun potere e fino a 2 annullamenti. 2 stelle: fino a 2 poteri. I tocchi bloccati non penalizzano.");
        add("star_rules", "3★ no powers, ≤2 undos · 2★ ≤2 powers · otherwise 1★", "3★ sans pouvoir, ≤2 annulations · 2★ ≤2 pouvoirs · sinon 1★", "3★ keine Kräfte, ≤2 Rücknahmen · 2★ ≤2 Kräfte · sonst 1★", "3★ sin poderes, ≤2 deshacer · 2★ ≤2 poderes · si no, 1★", "3★ sem poderes, ≤2 jogadas desfeitas · 2★ ≤2 poderes · senão 1★", "3★ senza poteri, ≤2 annullamenti · 2★ ≤2 poteri · altrimenti 1★");
        add("blocked_no_penalty", "Blocked taps have no penalty", "Les touches bloquées ne pénalisent pas", "Blockierte Versuche kosten nichts", "Los toques bloqueados no penalizan", "Os toques bloqueados não penalizam", "I tocchi bloccati non penalizzano");
        add("got_it", "Let’s drift", "C’est parti", "Los geht’s", "Vamos allá", "Vamos a isso", "Si parte");
        add("gate_open", "Gate open", "Porte ouverte", "Tor offen", "Puerta abierta", "Porta aberta", "Varco aperto");
        add("gate_closed", "Gate closed", "Porte fermée", "Tor geschlossen", "Puerta cerrada", "Porta fechada", "Varco chiuso");
        add("switch", "Switch", "Interrupteur", "Schalter", "Interruptor", "Interruptor", "Interruttore");
        add("blocked", "Path blocked", "Chemin bloqué", "Weg blockiert", "Camino bloqueado", "Caminho bloqueado", "Percorso bloccato");
        add("clear_board", "Free every piece", "Libère toutes les pièces", "Befreie alle Teile", "Libera todas las piezas", "Liberta todas as peças", "Libera tutti i pezzi");
        add("tap_to_escape", "Tap to drift out", "Touche pour t’échapper", "Tippen und hinausgleiten", "Toca para salir", "Toca para sair", "Tocca per uscire");
        add("sound", "Sound effects", "Effets sonores", "Soundeffekte", "Efectos de sonido", "Efeitos sonoros", "Effetti sonori");
        add("music", "Music", "Musique", "Musik", "Música", "Música", "Musica");
        add("haptics", "Haptics", "Vibrations", "Vibration", "Vibración", "Vibração", "Vibrazione");
        add("reduce_motion", "Reduce motion", "Réduire les animations", "Bewegung reduzieren", "Reducir animaciones", "Reduzir animações", "Riduci animazioni");
        add("language", "Language", "Langue", "Sprache", "Idioma", "Idioma", "Lingua");
        add("system", "System language", "Langue du système", "Systemsprache", "Idioma del sistema", "Idioma do sistema", "Lingua di sistema");
        add("on", "On", "Activé", "An", "Activado", "Ativado", "Attivo");
        add("off", "Off", "Désactivé", "Aus", "Desactivado", "Desativado", "Disattivo");
        add("how_to_play", "How to play", "Comment jouer", "Spielanleitung", "Cómo jugar", "Como jogar", "Come si gioca");
        add("privacy", "Privacy", "Confidentialité", "Datenschutz", "Privacidad", "Privacidade", "Privacy");
        add("privacy_text", "Your progress stays on this device. Optional play journal stays local until you export it. No account, ads or network tracking.", "Ta progression reste sur cet appareil. Le journal facultatif reste local jusqu’à son export. Aucun compte, publicité ou suivi réseau.", "Dein Fortschritt bleibt auf diesem Gerät. Das optionale Spielprotokoll bleibt bis zum Export lokal. Kein Konto, keine Werbung, kein Netzwerk-Tracking.", "Tu progreso se guarda en este dispositivo. El diario opcional sigue local hasta que lo exportes. Sin cuenta, anuncios ni rastreo en red.", "O teu progresso fica neste dispositivo. O diário opcional fica local até o exportares. Sem conta, anúncios nem rastreio em rede.", "I progressi restano su questo dispositivo. Il diario facoltativo resta locale fino all’esportazione. Nessun account, pubblicità o tracciamento in rete.");
        add("privacy_local", "Offline · Local saves · No tracking", "Hors ligne · Sauvegarde locale · Sans suivi", "Offline · Lokal gespeichert · Kein Tracking", "Sin conexión · Guardado local · Sin rastreo", "Offline · Dados locais · Sem rastreio", "Offline · Salvataggi locali · Nessun tracciamento");
        add("saved_local", "Saved on this device", "Sauvegardé sur cet appareil", "Auf diesem Gerät gespeichert", "Guardado en este dispositivo", "Guardado neste dispositivo", "Salvato su questo dispositivo");
        add("version", "Version", "Version", "Version", "Versión", "Versão", "Versione");
        add("credits", "Credits & license", "Crédits et licence", "Mitwirkende & Lizenz", "Créditos y licencia", "Créditos e licença", "Crediti e licenza");
        add("source_code", "Source code", "Code source", "Quellcode", "Código fuente", "Código-fonte", "Codice sorgente");
        add("event_expired", "This event has ended. Fresh puzzles are ready in Events.", "Cet événement est terminé. De nouveaux plateaux t’attendent dans Événements.", "Dieses Event ist beendet. Neue Rätsel warten unter Events.", "Este evento ha terminado. Hay nuevos puzles en Eventos.", "Este evento terminou. Há novos puzzles em Eventos.", "Questo evento è terminato. Nuovi rompicapi ti aspettano in Eventi.");
        add("characters", "Companions", "Compagnons", "Begleiter", "Compañeros", "Companheiros", "Compagni");
        add("skins", "Themes", "Thèmes", "Designs", "Temas", "Temas", "Temi");
        add("boosters", "Little helpers", "Petits coups de pouce", "Kleine Helfer", "Pequeñas ayudas", "Pequenas ajudas", "Piccoli aiuti");
        add("shop_note", "Everything is earned by playing. No real money.", "Tout se gagne en jouant. Aucun argent réel.", "Alles wird erspielt. Kein echtes Geld.", "Todo se gana jugando. Sin dinero real.", "Tudo se ganha a jogar. Sem dinheiro real.", "Tutto si guadagna giocando. Nessun denaro reale.");
        add("no_real_money", "Only earned coins. No real money.", "Uniquement des pièces gagnées. Aucun argent réel.", "Nur erspielte Münzen. Kein echtes Geld.", "Solo monedas ganadas. Sin dinero real.", "Só moedas ganhas. Sem dinheiro real.", "Solo monete guadagnate. Nessun denaro reale.");
        add("earned_coins", "Earn coins by clearing puzzles", "Gagne des pièces en terminant les plateaux", "Verdiene Münzen durch gelöste Rätsel", "Gana monedas completando puzles", "Ganha moedas ao concluir puzzles", "Guadagna monete completando i rompicapi");
        add("cosmetic_only", "New looks. Same rules.", "Un nouveau look, les mêmes règles.", "Neuer Look. Gleiche Regeln.", "Nuevos estilos. Mismas reglas.", "Novo visual. Mesmas regras.", "Nuovi look. Stesse regole.");
        add("collection_note", "Your light, your style", "Ta lumière, ton style", "Dein Licht, dein Stil", "Tu luz, tu estilo", "A tua luz, o teu estilo", "La tua luce, il tuo stile");
        add("purchased", "Added to your collection", "Ajouté à ta collection", "Zur Sammlung hinzugefügt", "Añadido a tu colección", "Adicionado à tua coleção", "Aggiunto alla collezione");
        add("purchase_success", "It’s yours!", "C’est à toi !", "Gehört jetzt dir!", "¡Ya es tuyo!", "É teu!", "È tuo!");
        add("items_added", "Items added", "Objets ajoutés", "Extras hinzugefügt", "Objetos añadidos", "Itens adicionados", "Oggetti aggiunti");
        add("stock", "In stock", "En réserve", "Vorrat", "Disponibles", "Em reserva", "Disponibili");
        add("pack", "Pack of %d", "Lot de %d", "%d-er Pack", "Paquete de %d", "Pacote de %d", "Pacchetto da %d");
        add("character_nova", "Nova", "Nova", "Nova", "Nova", "Nova", "Nova");
        add("character_miko", "Miko", "Miko", "Miko", "Miko", "Miko", "Miko");
        add("character_orbit", "Orbit", "Orbit", "Orbit", "Orbit", "Orbit", "Orbit");
        add("character_pip", "Pip", "Pip", "Pip", "Pip", "Pip", "Pip");
        add("character_astra", "Astra", "Astra", "Astra", "Astra", "Astra", "Astra");
        add("character_zig", "Zig", "Zig", "Zig", "Zig", "Zig", "Zig");
        add("skin_lumina", "Lumina", "Lumina", "Lumina", "Lumina", "Lumina", "Lumina");
        add("skin_lagoon", "Lagoon", "Lagon", "Lagune", "Laguna", "Lagoa", "Laguna");
        add("skin_blossom", "Blossom", "Floraison", "Blüte", "Floración", "Floração", "Fioritura");
        add("skin_ember", "Ember", "Braise", "Glut", "Ascua", "Brasa", "Brace");
        add("skin_midnight", "Midnight", "Minuit", "Mitternacht", "Medianoche", "Meia-noite", "Mezzanotte");
        add("skin_aurora", "Aurora", "Aurore", "Polarlicht", "Aurora", "Aurora", "Aurora");
        add("world_1", "First Light", "Première lumière", "Erstes Licht", "Primera luz", "Primeira luz", "Prima luce");
        add("world_2", "Moon Garden", "Jardin lunaire", "Mondgarten", "Jardín lunar", "Jardim lunar", "Giardino lunare");
        add("world_3", "Coral Dreams", "Rêves coralliens", "Korallenträume", "Sueños de coral", "Sonhos de coral", "Sogni di corallo");
        add("world_4", "Velvet Skies", "Ciels de velours", "Samthimmel", "Cielos de terciopelo", "Céus de veludo", "Cieli di velluto");
        add("world_5", "Amber Trails", "Sentiers d’ambre", "Bernsteinpfade", "Senderos de ámbar", "Trilhos de âmbar", "Sentieri d’ambra");
        add("world_6", "Starfall", "Pluie d’étoiles", "Sternenregen", "Lluvia de estrellas", "Chuva de estrelas", "Pioggia di stelle");
        add("world_7", "Crystal Tides", "Marées de cristal", "Kristallgezeiten", "Mareas de cristal", "Marés de cristal", "Maree di cristallo");
        add("world_8", "Aurora Gate", "Porte de l’aurore", "Tor zum Polarlicht", "Puerta de la aurora", "Porta da aurora", "Porta dell’aurora");
        add("level_progress", "%d / 240 levels", "%d / 240 niveaux", "%d / 240 Level", "%d / 240 niveles", "%d / 240 níveis", "%d / 240 livelli");
        add("progress", "Progress", "Progression", "Fortschritt", "Progreso", "Progresso", "Progressi");
        add("tap_play", "Your next little escape", "Ta prochaine petite échappée", "Dein nächster kleiner Ausflug", "Tu próxima pequeña escapada", "A tua próxima pequena fuga", "La tua prossima piccola fuga");
        add("remaining", "Remaining", "Restantes", "Übrig", "Restantes", "Restantes", "Rimanenti");
        add("mistakes", "Blocked taps", "Touches bloquées", "Blockierte Versuche", "Toques bloqueados", "Toques bloqueados", "Tocchi bloccati");
        add("calm_mode", "No timer. Find your own rhythm.", "Sans chrono. Trouve ton rythme.", "Ohne Zeitlimit. Finde deinen Rhythmus.", "Sin cronómetro. Encuentra tu ritmo.", "Sem cronómetro. Encontra o teu ritmo.", "Senza timer. Trova il tuo ritmo.");
        add("stage", "Stage", "Étape", "Etappe", "Etapa", "Etapa", "Tappa");
        add("stage_number", "Stage %d", "Étape %d", "Etappe %d", "Etapa %d", "Etapa %d", "Tappa %d");
        add("confirm_restart", "Restart this board? Your collected rewards are safe.", "Recommencer ce plateau ? Tes récompenses sont conservées.", "Dieses Brett neu starten? Deine Belohnungen bleiben erhalten.", "¿Reiniciar el tablero? Tus recompensas se conservan.", "Reiniciar este tabuleiro? As tuas recompensas ficam guardadas.", "Ricominciare il tabellone? Le ricompense restano tue.");
        add("cancel", "Cancel", "Annuler", "Abbrechen", "Cancelar", "Cancelar", "Annulla");
        add("yes", "Yes", "Oui", "Ja", "Sí", "Sim", "Sì");
        add("loading", "Waking the lights…", "Les lumières s’éveillent…", "Die Lichter erwachen…", "Despertando las luces…", "A despertar as luzes…", "Le luci si risvegliano…");
        add("accessibility", "Clear arrows, relaxed pace", "Des flèches lisibles, un rythme calme", "Klare Pfeile, entspanntes Tempo", "Flechas claras, ritmo tranquilo", "Setas claras, ritmo tranquilo", "Frecce chiare, ritmo rilassato");
        add("direction_up", "up", "haut", "oben", "arriba", "cima", "alto");
        add("direction_right", "right", "droite", "rechts", "derecha", "direita", "destra");
        add("direction_down", "down", "bas", "unten", "abajo", "baixo", "basso");
        add("direction_left", "left", "gauche", "links", "izquierda", "esquerda", "sinistra");
        add("piece_description", "Piece, row %d, column %d, arrow %s", "Pièce, ligne %d, colonne %d, flèche : %s", "Teil, Zeile %d, Spalte %d, Pfeil nach %s", "Pieza, fila %d, columna %d, flecha: %s", "Peça, linha %d, coluna %d, seta: %s", "Pezzo, riga %d, colonna %d, direzione %s");
        add("remix", "Remix", "Remix", "Remix", "Remix", "Remix", "Remix");
        add("remix_desc", "A second journey. New orders, fresh escapes.", "Un second voyage. De nouveaux ordres à découvrir.", "Eine zweite Reise. Neue Reihenfolgen, neue Auswege.", "Otro viaje. Nuevos órdenes, nuevas escapadas.", "Uma segunda jornada. Novas ordens, novas fugas.", "Un secondo viaggio. Nuovi ordini, nuove fughe.");
        add("remix_unlock", "Complete 12 Journey levels to unlock Remix", "Termine 12 niveaux du Voyage pour ouvrir Remix", "Schließe 12 Reise-Level ab, um Remix freizuschalten", "Completa 12 niveles de Viaje para desbloquear Remix", "Completa 12 níveis da Jornada para desbloquear Remix", "Completa 12 livelli del Viaggio per sbloccare Remix");
        add("share_challenge", "Challenge a friend", "Défier un ami", "Freunde herausfordern", "Reta a un amigo", "Desafia um amigo", "Sfida un amico");
        add("import_challenge", "Enter a challenge", "Entrer un défi", "Challenge eingeben", "Introducir un reto", "Introduzir um desafio", "Inserisci una sfida");
        add("challenge_code", "Challenge code", "Code du défi", "Challenge-Code", "Código del reto", "Código do desafio", "Codice sfida");
        add("challenge_invalid", "That code is not valid. Check the full LUMA code.", "Ce code est invalide. Vérifie le code LUMA complet.", "Dieser Code ist ungültig. Prüfe den vollständigen LUMA-Code.", "Ese código no es válido. Revisa el código LUMA completo.", "Esse código não é válido. Confirma o código LUMA completo.", "Codice non valido. Controlla il codice LUMA completo.");
        add("friend_challenge", "Friend challenge", "Défi entre amis", "Freundes-Challenge", "Reto entre amigos", "Desafio entre amigos", "Sfida tra amici");
        add("challenge_note", "Same board, fair start. No powers, XP or coins.", "Même plateau, même départ. Sans pouvoir, XP ni pièces.", "Gleiches Brett, fairer Start. Ohne Kräfte, XP oder Münzen.", "Mismo tablero, salida justa. Sin poderes, XP ni monedas.", "Mesmo tabuleiro, início justo. Sem poderes, XP nem moedas.", "Stesso tabellone, partenza equa. Niente poteri, XP o monete.");
        add("share_result", "Share this escape", "Partager cette échappée", "Diesen Ausweg teilen", "Comparte esta escapada", "Partilha esta fuga", "Condividi questa fuga");
        add("copy_code", "Copy code", "Copier le code", "Code kopieren", "Copiar código", "Copiar código", "Copia codice");
        add("code_copied", "Challenge code copied", "Code du défi copié", "Challenge-Code kopiert", "Código del reto copiado", "Código do desafio copiado", "Codice sfida copiato");
        add("import_hint", "Paste a LUMA challenge code", "Colle un code de défi LUMA", "LUMA-Challenge-Code einfügen", "Pega un código de reto LUMA", "Cola um código de desafio LUMA", "Incolla un codice sfida LUMA");
        add("share_handoff", "Choose an app to share your challenge", "Choisis une app pour partager ton défi", "Wähle eine App zum Teilen deiner Challenge", "Elige una app para compartir el reto", "Escolhe uma app para partilhar o desafio", "Scegli un’app per condividere la sfida");
        add("share_error", "Could not share this challenge. Please try again.", "Impossible de partager ce défi. Réessaie.", "Diese Challenge konnte nicht geteilt werden. Bitte erneut versuchen.", "No se pudo compartir este reto. Inténtalo de nuevo.", "Não foi possível partilhar este desafio. Tenta novamente.", "Impossibile condividere questa sfida. Riprova.");
        add("share_caption", "Can you solve my Luma Drift puzzle?\nCode: %s\nOpen: %s\nGet the game: %s", "Peux-tu résoudre mon plateau Luma Drift ?\nCode : %s\nOuvrir : %s\nTélécharger le jeu : %s", "Kannst du mein Luma-Drift-Rätsel lösen?\nCode: %s\nÖffnen: %s\nSpiel herunterladen: %s", "¿Puedes resolver mi puzle de Luma Drift?\nCódigo: %s\nAbrir: %s\nDescarga el juego: %s", "Consegues resolver o meu puzzle Luma Drift?\nCódigo: %s\nAbrir: %s\nDescarrega o jogo: %s", "Riesci a risolvere il mio rompicapo Luma Drift?\nCodice: %s\nApri: %s\nScarica il gioco: %s");
        add("share_card_title", "Can you find the way out?", "Trouveras-tu la sortie ?", "Findest du den Ausweg?", "¿Encontrarás la salida?", "Consegues encontrar a saída?", "Riesci a trovare l’uscita?");
        add("share_card_subtitle", "Same board. Your own way.", "Même plateau. Ton propre chemin.", "Gleiches Brett. Dein eigener Weg.", "Mismo tablero. Tu propio camino.", "Mesmo tabuleiro. O teu caminho.", "Stesso tabellone. La tua strada.");
        add("share_card_footer", "Get the game, then enter this code.", "Télécharge le jeu, puis entre ce code.", "Lade das Spiel und gib diesen Code ein.", "Descarga el juego e introduce este código.", "Descarrega o jogo e introduz este código.", "Scarica il gioco e inserisci questo codice.");
        add("local_journal", "Play journal", "Journal de jeu", "Spielprotokoll", "Diario de juego", "Diário de jogo", "Diario di gioco");
        add("journal", "Play journal", "Journal de jeu", "Spielprotokoll", "Diario de juego", "Diário de jogo", "Diario di gioco");
        add("journal_desc", "Optional. Saves up to 2,000 play events on this device. Nothing is sent automatically.", "Facultatif. Jusqu’à 2 000 événements de jeu sur cet appareil. Aucun envoi automatique.", "Optional. Speichert bis zu 2.000 Spielereignisse auf diesem Gerät. Kein automatischer Versand.", "Opcional. Guarda hasta 2.000 eventos en este dispositivo. Nada se envía automáticamente.", "Opcional. Guarda até 2.000 eventos neste dispositivo. Nada é enviado automaticamente.", "Facoltativo. Salva fino a 2.000 eventi su questo dispositivo. Nessun invio automatico.");
        add("export_journal", "Export journal", "Exporter le journal", "Protokoll exportieren", "Exportar diario", "Exportar diário", "Esporta diario");
        add("clear_journal", "Clear journal", "Effacer le journal", "Protokoll löschen", "Borrar diario", "Apagar diário", "Cancella diario");
        add("journal_empty", "No recorded play events", "Aucun événement de jeu enregistré", "Keine Spielereignisse aufgezeichnet", "No hay eventos registrados", "Sem eventos de jogo registados", "Nessun evento registrato");
        add("journal_cleared", "Journal cleared. Your game progress is safe.", "Journal effacé. Ta progression est conservée.", "Protokoll gelöscht. Dein Fortschritt bleibt erhalten.", "Diario borrado. Tu progreso se conserva.", "Diário apagado. O teu progresso foi mantido.", "Diario cancellato. I progressi sono conservati.");
        add("journal_error", "Could not export the journal. Please try again.", "Impossible d’exporter le journal. Réessaie.", "Protokoll konnte nicht exportiert werden. Bitte erneut versuchen.", "No se pudo exportar el diario. Inténtalo de nuevo.", "Não foi possível exportar o diário. Tenta novamente.", "Impossibile esportare il diario. Riprova.");
        add("confirm_clear_journal", "Delete this device’s journal and temporary exports? Your game progress stays. Copies already exported remain with their recipients.", "Effacer le journal de cet appareil et les exports temporaires ? Ta progression reste. Les copies déjà exportées restent chez leurs destinataires.", "Protokoll und temporäre Exporte auf diesem Gerät löschen? Dein Fortschritt bleibt erhalten. Bereits exportierte Kopien bleiben bei ihren Empfängern.", "¿Borrar el diario de este dispositivo y las exportaciones temporales? Tu progreso se conserva. Las copias ya exportadas permanecen con sus destinatarios.", "Apagar o diário deste dispositivo e as exportações temporárias? O teu progresso mantém-se. As cópias já exportadas ficam com os destinatários.", "Eliminare il diario di questo dispositivo e le esportazioni temporanee? I progressi restano. Le copie già esportate rimangono ai destinatari.");
        add("save_error", "Could not save your reward. Please try again.", "Impossible d’enregistrer ta récompense. Réessaie.", "Deine Belohnung konnte nicht gespeichert werden. Bitte erneut versuchen.", "No se pudo guardar tu recompensa. Inténtalo de nuevo.", "Não foi possível guardar a tua recompensa. Tenta novamente.", "Impossibile salvare la ricompensa. Riprova.");
        add("export_note", "Export contains this device’s play events. You choose where to save or send it.", "L’export contient les événements de cet appareil. Tu choisis où l’enregistrer ou l’envoyer.", "Der Export enthält Spielereignisse dieses Geräts. Du bestimmst, wo du ihn speicherst oder teilst.", "La exportación contiene eventos de este dispositivo. Tú eliges dónde guardarla o enviarla.", "A exportação contém eventos deste dispositivo. Tu escolhes onde guardar ou enviar.", "L’esportazione contiene gli eventi di questo dispositivo. Scegli tu dove salvarla o inviarla.");
        add("stats", "Local observations", "Observations locales", "Lokale Beobachtungen", "Observaciones locales", "Observações locais", "Osservazioni locali");
        add("local_only", "This device only · Not global player data", "Cet appareil uniquement · Pas de données globales", "Nur dieses Gerät · Keine globalen Spielerdaten", "Solo este dispositivo · Sin datos globales", "Só este dispositivo · Sem dados globais", "Solo questo dispositivo · Nessun dato globale");
        add("journal_sessions", "%d sessions · %d active days", "%d sessions · %d jours actifs", "%d Sitzungen · %d aktive Tage", "%d sesiones · %d días activos", "%d sessões · %d dias ativos", "%d sessioni · %d giorni attivi");
        add("journal_returns", "D1: %s · D7: %s", "J1 : %s · J7 : %s", "T1: %s · T7: %s", "D1: %s · D7: %s", "D1: %s · D7: %s", "G1: %s · G7: %s");
        add("journal_unknown", "not measured", "non mesuré", "nicht gemessen", "sin medir", "não medido", "non misurato");
        add("journal_observed", "return observed", "retour observé", "Rückkehr beobachtet", "regreso observado", "regresso observado", "ritorno osservato");
        add("journal_not_observed", "no local observation", "sans observation locale", "lokal nicht beobachtet", "sin observación local", "sem observação local", "nessuna osservazione locale");
        add("missions", "Today’s sparks", "Étincelles du jour", "Heutige Funken", "Chispas de hoy", "Faíscas do dia", "Scintille del giorno");
        add("mission_boards", "Clear 3 new boards", "Terminer 3 nouveaux plateaux", "Löse 3 neue Bretter", "Completa 3 tableros nuevos", "Conclui 3 tabuleiros novos", "Completa 3 nuovi tabelloni");
        add("mission_stars", "Earn 6 stars on new boards", "Gagner 6 étoiles sur de nouveaux plateaux", "Verdiene 6 Sterne auf neuen Brettern", "Gana 6 estrellas en tableros nuevos", "Ganha 6 estrelas em tabuleiros novos", "Ottieni 6 stelle su nuovi tabelloni");
        add("mission_event", "Clear 1 Daily or Weekly board", "Terminer 1 plateau du jour ou de la semaine", "Löse 1 Tages- oder Wochenbrett", "Completa 1 tablero diario o semanal", "Conclui 1 tabuleiro diário ou semanal", "Completa 1 tabellone giornaliero o settimanale");
        add("mission_one", "Clear 3 new boards", "Terminer 3 nouveaux plateaux", "Löse 3 neue Bretter", "Completa 3 tableros nuevos", "Conclui 3 tabuleiros novos", "Completa 3 nuovi tabelloni");
        add("mission_two", "Earn 6 stars on new boards", "Gagner 6 étoiles sur de nouveaux plateaux", "Verdiene 6 Sterne auf neuen Brettern", "Gana 6 estrellas en tableros nuevos", "Ganha 6 estrelas em tabuleiros novos", "Ottieni 6 stelle su nuovi tabelloni");
        add("mission_three", "Clear 1 Daily or Weekly board", "Terminer 1 plateau du jour ou de la semaine", "Löse 1 Tages- oder Wochenbrett", "Completa 1 tablero diario o semanal", "Conclui 1 tabuleiro diário ou semanal", "Completa 1 tabellone giornaliero o settimanale");
        add("mission_note", "First clears count. Play at your pace. Your rank stays earned.", "Seules les premières réussites comptent. Joue à ton rythme. Ton rang reste acquis.", "Nur erste Lösungen zählen. Spiele in deinem Tempo. Dein Rang bleibt erhalten.", "Cuentan las primeras victorias. Juega a tu ritmo. Tu rango se conserva.", "Contam as primeiras vitórias. Joga ao teu ritmo. O teu escalão mantém-se.", "Contano le prime vittorie. Gioca al tuo ritmo. Il tuo grado resta acquisito.");
        add("mission_progress", "%d / %d", "%d / %d", "%d / %d", "%d / %d", "%d / %d", "%d / %d");
        add("mission_reward", "+%d coins", "+%d pièces", "+%d Münzen", "+%d monedas", "+%d moedas", "+%d monete");
        add("rank", "Lightkeeper %d", "Gardien de lumière %d", "Lichthüter %d", "Guardián de luz %d", "Guardião de luz %d", "Custode della luce %d");
        add("xp", "%d XP", "%d XP", "%d XP", "%d XP", "%d XP", "%d XP");
        add("xp_earned", "+%d XP", "+%d XP", "+%d XP", "+%d XP", "+%d XP", "+%d XP");
        add("rank_progress", "%d / 120 XP", "%d / 120 XP", "%d / 120 XP", "%d / 120 XP", "%d / 120 XP", "%d / 120 XP");
        add("days_played", "%d days explored", "%d jours d’exploration", "%d Erkundungstage", "%d días explorados", "%d dias de exploração", "%d giorni di esplorazione");
        add("days_note", "A day away never takes anything away", "Une pause ne retire jamais rien", "Eine Pause nimmt dir nichts weg", "Descansar nunca te quita nada", "Uma pausa nunca te tira nada", "Una pausa non ti toglie nulla");
        add("badge", "Starlight badge", "Insigne Éclat d’étoile", "Sternenlicht-Abzeichen", "Insignia Luz estelar", "Emblema Luz estelar", "Distintivo Luce stellare");
        add("badge_desc", "Free cosmetic reward for 7 first clears", "Récompense cosmétique offerte après 7 premières réussites", "Gratis-Kosmetik für 7 erstmals gelöste Bretter", "Recompensa estética gratis por 7 primeras victorias", "Recompensa visual grátis por 7 primeiras vitórias", "Premio estetico gratuito dopo 7 prime vittorie");
        add("badge_unlocked", "Starlight badge unlocked!", "Insigne Éclat d’étoile débloqué !", "Sternenlicht-Abzeichen freigeschaltet!", "¡Insignia Luz estelar desbloqueada!", "Emblema Luz estelar desbloqueado!", "Distintivo Luce stellare sbloccato!");
        add("free_opening", "A clear start", "Un départ lumineux", "Ein klarer Anfang", "Un comienzo claro", "Um início luminoso", "Un inizio luminoso");
        add("tutorial_next", "Try the next rule", "Découvrir la règle suivante", "Nächste Regel ausprobieren", "Prueba la siguiente regla", "Experimenta a próxima regra", "Prova la regola successiva");
        add("tutorial_gate_live", "Release a glowing switch piece. Watch its matching gates change.", "Fais sortir une pièce lumineuse à interrupteur. Observe les portes de sa couleur.", "Lass ein leuchtendes Schalterteil hinausgleiten. Beobachte die passenden Tore.", "Libera una pieza con interruptor brillante. Observa sus puertas del mismo color.", "Liberta uma peça com interruptor luminoso. Observa as portas da mesma cor.", "Libera un pezzo con interruttore luminoso. Osserva i varchi dello stesso colore.");
        add("tutorial_dual_live", "Some switches change two colors. Plan the order. Undo is always free.", "Certains interrupteurs changent deux couleurs. Prévois l’ordre. Annuler reste gratuit.", "Manche Schalter ändern zwei Farben. Plane die Reihenfolge. Zurücknehmen ist gratis.", "Algunos interruptores cambian dos colores. Planea el orden. Deshacer es gratis.", "Alguns interruptores mudam duas cores. Planeia a ordem. Desfazer é grátis.", "Alcuni interruttori cambiano due colori. Pianifica l’ordine. Annullare è gratuito.");
        add("coach_motion", "Tap a piece. It follows its arrow when the path is clear.", "Touche une pièce. Elle suit sa flèche si le chemin est libre.", "Tippe ein Teil an. Bei freiem Weg folgt es seinem Pfeil.", "Toca una pieza. Sigue su flecha si el camino está libre.", "Toca numa peça. Ela segue a seta se o caminho estiver livre.", "Tocca un pezzo. Segue la freccia se il percorso è libero.");
        add("coach_order", "Find the right order. Free a path before moving the next piece.", "Trouve le bon ordre. Libère un chemin pour la pièce suivante.", "Finde die richtige Reihenfolge. Mache den Weg für das nächste Teil frei.", "Encuentra el orden. Abre un camino para la siguiente pieza.", "Encontra a ordem certa. Abre caminho para a próxima peça.", "Trova l’ordine giusto. Libera il percorso per il prossimo pezzo.");
        add("coach_gate", "Glowing switches change matching gates as they leave.", "Les interrupteurs lumineux changent les portes de leur couleur en sortant.", "Leuchtende Schalter ändern beim Hinausgleiten die passenden Tore.", "Los interruptores brillantes cambian sus puertas del mismo color al salir.", "Os interruptores luminosos mudam as portas da mesma cor ao sair.", "Gli interruttori luminosi cambiano i varchi dello stesso colore quando escono.");
        add("coach_dual", "Two-color switches change two gate groups. Try, observe, undo for free.", "Les interrupteurs bicolores changent deux groupes de portes. Essaie, observe, annule gratuitement.", "Zweifarbige Schalter ändern zwei Torgruppen. Probiere, beobachte und nimm gratis zurück.", "Los interruptores bicolores cambian dos grupos de puertas. Prueba, observa y deshaz gratis.", "Os interruptores de duas cores mudam dois grupos de portas. Experimenta, observa e desfaz grátis.", "Gli interruttori bicolore cambiano due gruppi di varchi. Prova, osserva e annulla gratis.");
        add("tutorial_badge", "Your first 7 clears earn a free Starlight badge. Every day played counts; taking a break costs nothing.", "Tes 7 premières réussites offrent l’insigne Éclat d’étoile. Chaque jour joué compte ; une pause ne coûte rien.", "Deine ersten 7 Lösungen bringen ein gratis Sternenlicht-Abzeichen. Jeder Spieltag zählt; eine Pause kostet nichts.", "Tus primeras 7 victorias dan la insignia Luz estelar gratis. Cada día jugado cuenta; descansar no cuesta nada.", "As tuas primeiras 7 vitórias dão o emblema Luz estelar grátis. Cada dia jogado conta; uma pausa não custa nada.", "Le prime 7 vittorie danno il distintivo Luce stellare gratis. Ogni giorno giocato conta; una pausa non costa nulla.");
        add("journey_tab", "Journey", "Voyage", "Reise", "Viaje", "Jornada", "Viaggio");
        add("remix_tab", "Remix", "Remix", "Remix", "Remix", "Remix", "Remix");
        add("journey_progress", "%d / 240 boards", "%d / 240 plateaux", "%d / 240 Bretter", "%d / 240 tableros", "%d / 240 tabuleiros", "%d / 240 tabelloni");
        add("remix_progress", "%d / 240 boards", "%d / 240 plateaux", "%d / 240 Bretter", "%d / 240 tableros", "%d / 240 tabuleiros", "%d / 240 tabelloni");
    }

    private Strings() { }

    private static void add(String key, String en, String fr, String de, String es, String pt, String it) {
        TEXT.put(key, new String[]{en, fr, de, es, pt, it});
    }

    public static boolean has(String key) { return TEXT.containsKey(key); }

    public static boolean supports(String language) {
        for (String code : CODES) if (code.equals(language)) return true;
        return false;
    }

    public static String normalize(String language) {
        if (language == null || "auto".equals(language)) language = Locale.getDefault().getLanguage();
        String code = language.toLowerCase(Locale.ROOT);
        int dash = code.indexOf('-');
        int underscore = code.indexOf('_');
        int separator = dash < 0 ? underscore : underscore < 0 ? dash : Math.min(dash, underscore);
        if (separator >= 0) code = code.substring(0, separator);
        return supports(code) ? code : "en";
    }

    public static String get(String language, String key, Object... args) {
        String[] values = TEXT.get(key);
        if (values == null) return key == null ? "" : key.replace('_', ' ');
        String code = normalize(language);
        int index = 0;
        for (int i = 0; i < CODES.length; i++) if (CODES[i].equals(code)) { index = i; break; }
        String value = values[index];
        if (args.length == 0) return value;
        try {
            return String.format(Locale.forLanguageTag(code), value, args);
        } catch (RuntimeException invalidFormat) {
            return value;
        }
    }
}
