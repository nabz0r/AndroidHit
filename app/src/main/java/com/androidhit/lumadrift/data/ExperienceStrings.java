package com.androidhit.lumadrift.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Short, six-language navigation and inventory copy. Detailed rules remain in their original catalogs. */
public final class ExperienceStrings {
    private static final String[] LANGUAGES = {"en", "fr", "de", "es", "pt", "it"};
    private static final Map<String, String[]> TEXT = new LinkedHashMap<>();
    static {
        add("nav_map", "Map", "Carte", "Karte", "Mapa", "Mapa", "Mappa");
        add("nav_inventory", "Inventory", "Inventaire", "Inventar", "Inventario", "Inventário", "Inventario");
        add("nav_events", "Events", "Événements", "Events", "Eventos", "Eventos", "Eventi");
        add("tab_powers", "Powers", "Pouvoirs", "Kräfte", "Poderes", "Poderes", "Poteri");
        add("tab_companions", "Companions", "Compagnons", "Begleiter", "Compañeros", "Companheiros", "Compagni");
        add("tab_equipment", "Equipment", "Équipement", "Ausrüstung", "Equipo", "Equipamento", "Oggetti");
        add("tab_themes", "Themes", "Thèmes", "Themen", "Temas", "Temas", "Temi");
        add("selected", "Selected", "Sélectionné", "Ausgewählt", "Seleccionado", "Selecionado", "Selezionato");
        add("equipped", "Equipped", "Équipé", "Angelegt", "Equipado", "Equipado", "Equipaggiato");
        add("owned", "Owned", "Possédé", "Im Besitz", "Obtenido", "Adquirido", "Posseduto");
        add("to_unlock", "To unlock", "À débloquer", "Freischalten", "Por desbloquear", "Por desbloquear", "Da sbloccare");
        add("ready_to_claim", "Ready to claim", "À récupérer", "Abholbereit", "Para recoger", "Para receber", "Da ritirare");
        add("filter_all", "All", "Tout", "Alle", "Todo", "Tudo", "Tutto");
        add("filter_owned", "Owned", "Possédés", "Im Besitz", "Obtenidos", "Adquiridos", "Posseduti");
        add("filter_locked", "To unlock", "À débloquer", "Freischalten", "Por desbloquear", "Por desbloquear", "Da sbloccare");
        add("collection_count", "%d / %d owned", "%d / %d possédés", "%d / %d im Besitz", "%d / %d obtenidos", "%d / %d adquiridos", "%d / %d posseduti");
        add("collection_empty", "Nothing here yet", "Rien ici pour le moment", "Hier ist noch nichts", "Aún no hay nada", "Ainda sem itens", "Ancora nessun oggetto");
        add("action_equip", "Equip", "Équiper", "Anlegen", "Equipar", "Equipar", "Equipaggia");
        add("action_select", "Select", "Choisir", "Auswählen", "Elegir", "Escolher", "Scegli");
        add("action_view", "Details", "Détails", "Details", "Detalles", "Detalhes", "Dettagli");
        add("action_claim", "Claim", "Récupérer", "Abholen", "Recoger", "Receber", "Ritira");
        add("action_shop", "Open shop", "Voir la boutique", "Zum Shop", "Ir a la tienda", "Abrir loja", "Apri negozio");
        add("action_play", "Play", "Jouer", "Spielen", "Jugar", "Jogar", "Gioca");
        add("action_continue", "Continue", "Continuer", "Weiter", "Continuar", "Continuar", "Continua");
        add("source_starter", "Free from the start", "Offert au départ", "Von Anfang an gratis", "Gratis desde el inicio", "Grátis desde o início", "Gratis dall’inizio");
        add("source_journey", "Journey clears · %d/%d", "Plateaux du parcours · %d/%d", "Gelöste Reise-Rätsel · %d/%d", "Puzles del recorrido · %d/%d", "Puzzles do percurso · %d/%d", "Puzzle del percorso · %d/%d");
        add("source_weekly", "Weekly wins · %d/%d total", "Victoires hebdo · %d/%d au total", "Wochensiege · %d/%d insgesamt", "Victorias semanales · %d/%d total", "Vitórias semanais · %d/%d total", "Vittorie settimanali · %d/%d totali");
        add("source_events", "Event stars · %d/%d total", "Étoiles d’événement · %d/%d au total", "Eventsterne · %d/%d insgesamt", "Estrellas de eventos · %d/%d total", "Estrelas de eventos · %d/%d total", "Stelle degli eventi · %d/%d totali");
        add("source_shop", "Shop · %d earned coins", "Boutique · %d pièces gagnées", "Shop · %d erspielte Münzen", "Tienda · %d monedas ganadas", "Loja · %d moedas ganhas", "Negozio · %d monete guadagnate");
        add("earned_price", "%d earned coins", "%d pièces gagnées", "%d erspielte Münzen", "%d monedas ganadas", "%d moedas ganhas", "%d monete guadagnate");
        add("earned_short", "Earned coins", "Pièces gagnées", "Erspielte Münzen", "Monedas ganadas", "Moedas ganhas", "Monete guadagnate");
        add("effect_appearance", "Appearance only", "Apparence uniquement", "Nur Aussehen", "Solo apariencia", "Apenas aparência", "Solo aspetto");
        add("effect_win_one", "+1 earned coin on first clears", "+1 pièce aux premières victoires", "+1 Münze bei ersten Siegen", "+1 moneda en primeras victorias", "+1 moeda nas primeiras vitórias", "+1 moneta alle prime vittorie");
        add("effect_win", "+%d earned coins on first clears", "+%d pièces aux premières victoires", "+%d Münzen bei ersten Siegen", "+%d monedas en primeras victorias", "+%d moedas nas primeiras vitórias", "+%d monete alle prime vittorie");
        add("effect_discount_one", "Power packs: −1 earned coin", "Packs de pouvoirs : −1 pièce", "Kraftpakete: −1 Münze", "Packs de poderes: −1 moneda", "Pacotes de poderes: −1 moeda", "Pacchetti poteri: −1 moneta");
        add("effect_discount", "Power packs: −%d earned coins", "Packs de pouvoirs : −%d pièces", "Kraftpakete: −%d Münzen", "Packs de poderes: −%d monedas", "Pacotes de poderes: −%d moedas", "Pacchetti poteri: −%d monete");
        add("effect_grace", "1 blocked attempt protected per run", "1 essai bloqué protégé par partie", "1 Fehlversuch pro Runde geschützt", "1 intento bloqueado protegido por partida", "1 tentativa bloqueada protegida por partida", "1 tentativo bloccato protetto per partita");
        add("equipment_local", "Equipment is saved on this device", "Équipement sauvegardé sur cet appareil", "Ausrüstung wird auf diesem Gerät gespeichert", "Equipo guardado en este dispositivo", "Equipamento guardado neste dispositivo", "Oggetti salvati su questo dispositivo");
        add("equipment_slots", "One item per slot", "Un objet par emplacement", "Ein Gegenstand je Platz", "Un objeto por ranura", "Um item por espaço", "Un oggetto per spazio");
        add("power_active", "Active", "Actif", "Aktiv", "Activo", "Ativo", "Attivo");
        add("power_available", "Ready", "Prêt", "Bereit", "Listo", "Pronto", "Pronto");
        add("power_empty", "No charges", "Aucune charge", "Keine Ladungen", "Sin cargas", "Sem cargas", "Nessuna carica");
        add("power_locked", "Locked", "Verrouillé", "Gesperrt", "Bloqueado", "Bloqueado", "Bloccato");
        add("power_stock", "Stock: %d", "Stock : %d", "Vorrat: %d", "Reservas: %d", "Stock: %d", "Scorte: %d");
        add("power_unlock_at", "Unlock at Journey level %d", "Disponible au niveau %d du parcours", "Ab Reise-Level %d", "Se desbloquea en el nivel %d", "Desbloqueia no nível %d do percurso", "Si sblocca al livello %d del percorso");
        add("power_pack", "%d charges · %d earned coins", "%d charges · %d pièces gagnées", "%d Ladungen · %d erspielte Münzen", "%d cargas · %d monedas ganadas", "%d cargas · %d moedas ganhas", "%d cariche · %d monete guadagnate");
        add("power_no_second_charge", "Already active · no charge used", "Déjà actif · aucune charge utilisée", "Schon aktiv · keine Ladung verbraucht", "Ya activo · sin gastar otra carga", "Já ativo · sem gastar outra carga", "Già attivo · nessuna carica usata");
        add("unlock_next", "Next: %s · level %d", "Ensuite : %s · niveau %d", "Als Nächstes: %s · Level %d", "Siguiente: %s · nivel %d", "A seguir: %s · nível %d", "Prossimo: %s · livello %d");
        add("all_powers_unlocked", "All six powers unlocked", "Les six pouvoirs sont débloqués", "Alle sechs Kräfte freigeschaltet", "Los seis poderes desbloqueados", "Os seis poderes desbloqueados", "Tutti e sei i poteri sbloccati");
        add("objective", "Goal", "Objectif", "Ziel", "Objetivo", "Objetivo", "Obiettivo");
        add("objective_clear", "Release all pieces (%d)", "Libère toutes les pièces (%d)", "Löse alle Teile (%d)", "Libera todas las piezas (%d)", "Liberta todas as peças (%d)", "Libera tutte le tessere (%d)");
        add("objective_open_seals", "Release keys, then clear the board", "Libère les clés, puis vide le plateau", "Löse Schlüssel, dann alle Teile", "Libera las llaves y vacía el tablero", "Liberta as chaves e limpa o tabuleiro", "Libera le chiavi, poi svuota il campo");
        add("action_budget", "Action limit: %d", "Limite de coups : %d", "Aktionslimit: %d", "Límite de acciones: %d", "Limite de ações: %d", "Limite di azioni: %d");
        add("level_type_direction", "Directions", "Directions", "Richtungen", "Direcciones", "Direções", "Direzioni");
        add("level_type_gates", "Gates", "Portes", "Tore", "Puertas", "Portas", "Porte");
        add("level_type_pairs", "Dual gates", "Portes doubles", "Doppeltore", "Puertas dobles", "Portas duplas", "Porte doppie");
        add("level_type_keys", "Keys & walls", "Clés et murs", "Schlüssel und Mauern", "Llaves y muros", "Chaves e muros", "Chiavi e muri");
        add("difficulty_normal", "Normal", "Normal", "Normal", "Normal", "Normal", "Normale");
        add("difficulty_hard", "Hard", "Difficile", "Schwer", "Difícil", "Difícil", "Difficile");
        add("difficulty_expert", "Expert", "Expert", "Experte", "Experto", "Especialista", "Esperto");
        add("stage_intro", "Discover", "Découverte", "Entdecken", "Descubre", "Descobre", "Scopri");
        add("stage_practice", "Practice", "Entraînement", "Üben", "Practica", "Pratica", "Allenati");
        add("stage_peak", "Challenge", "Défi", "Herausforderung", "Desafío", "Desafio", "Sfida");
        add("stage_recovery", "Breathe", "Respiration", "Durchatmen", "Respira", "Respira", "Respira");
        add("saga_chapter", "Chapter %d", "Chapitre %d", "Kapitel %d", "Capítulo %d", "Capítulo %d", "Capitolo %d");
        add("saga_range", "Levels %d–%d", "Niveaux %d–%d", "Level %d–%d", "Niveles %d–%d", "Níveis %d–%d", "Livelli %d–%d");
        add("saga_page", "%d / %d", "%d / %d", "%d / %d", "%d / %d", "%d / %d", "%d / %d");
        add("saga_next", "Next chapter", "Chapitre suivant", "Nächstes Kapitel", "Siguiente capítulo", "Próximo capítulo", "Capitolo seguente");
        add("saga_previous", "Previous chapter", "Chapitre précédent", "Vorheriges Kapitel", "Capítulo anterior", "Capítulo anterior", "Capitolo precedente");
        add("saga_complete", "Journey complete", "Parcours terminé", "Reise abgeschlossen", "Recorrido completado", "Percurso concluído", "Percorso completato");
        alias("beat_introduction", "stage_intro");
        alias("beat_practice", "stage_practice");
        alias("beat_peak", "stage_peak");
        alias("beat_recovery", "stage_recovery");
        alias("objective_clear_board", "objective_clear");
        alias("element_arrows", "level_type_direction");
        alias("element_gates", "level_type_gates");
        alias("element_dual_gates", "level_type_pairs");
        alias("element_keys_and_seals", "level_type_keys");
    }
    private ExperienceStrings() { }
    private static void add(String key, String... values) {
        if (values.length != LANGUAGES.length || TEXT.put(key, values) != null)
            throw new IllegalArgumentException("Invalid experience copy: " + key);
    }
    private static void alias(String key, String original) { add(key, TEXT.get(original)); }
    public static boolean has(String key) { return TEXT.containsKey(key); }
    public static Set<String> keys() { return Collections.unmodifiableSet(TEXT.keySet()); }
    public static String get(String language, String key, Object... args) {
        String[] row = TEXT.get(key);
        if (row == null) throw new IllegalArgumentException("Missing experience copy: " + key);
        String lang = Strings.normalize(language);
        int index = 0;
        for (int i = 0; i < LANGUAGES.length; i++) if (LANGUAGES[i].equals(lang)) index = i;
        return args.length == 0 ? row[index] : String.format(Locale.forLanguageTag(lang), row[index], args);
    }
}
