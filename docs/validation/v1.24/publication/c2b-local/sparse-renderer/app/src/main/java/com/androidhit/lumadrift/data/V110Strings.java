package com.androidhit.lumadrift.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Avatar, objective and navigation copy. No ownership, purchase or reward side effects. */
public final class V110Strings {
    private static final Map<String,String[]> ROWS = new LinkedHashMap<>();
    static {
        add("avatar", "My companion", "Mon compagnon", "Mein Begleiter", "Mi compañero", "O meu companheiro", "Il mio compagno");
        add("appearance", "Appearance", "Apparence", "Aussehen", "Apariencia", "Aparência", "Aspetto");
        add("equipment", "Equipment", "Équipement", "Ausrüstung", "Equipo", "Equipamento", "Equipaggiamento");
        add("visual_styles", "Visual styles", "Styles visuels", "Grafikstile", "Estilos visuales", "Estilos visuais", "Stili visivi");
        add("today", "Today", "Aujourd’hui", "Heute", "Hoy", "Hoje", "Oggi");
        add("this_week", "This week", "Cette semaine", "Diese Woche", "Esta semana", "Esta semana", "Questa settimana");
        add("journey_rewards", "Journey gifts", "Cadeaux du parcours", "Reisegeschenke", "Regalos del camino", "Presentes do percurso", "Regali del percorso");
        add("journey_short", "Journey", "Parcours", "Reise", "Camino", "Percurso", "Percorso");
        add("rewards_all", "Ready to collect", "À récupérer", "Bereit zum Abholen", "Listo para recoger", "Pronto a receber", "Pronto da ritirare");
        add("story_book", "Prism journal", "Carnet du Prisme", "Prisma-Tagebuch", "Diario del Prisma", "Diário do Prisma", "Diario del Prisma");
        add("visit_note", "One visit gift per local date. Your cycle stays when you take a break.", "Un cadeau de visite par date locale. Ton cycle est conservé quand tu fais une pause.", "Ein Besuchsgeschenk pro lokalem Datum. Dein Zyklus bleibt erhalten, wenn du eine Pause machst.", "Un regalo de visita por fecha local. Tu ciclo se conserva cuando haces una pausa.", "Um presente de visita por data local. O teu ciclo mantém-se quando fazes uma pausa.", "Un regalo di visita per data locale. Il tuo ciclo si conserva quando fai una pausa.");
        add("period_changed", "The period changed. The list has been refreshed.", "La période a changé. La liste a été actualisée.", "Der Zeitraum hat sich geändert. Die Liste wurde aktualisiert.", "El período ha cambiado. La lista se ha actualizado.", "O período mudou. A lista foi atualizada.", "Il periodo è cambiato. L’elenco è stato aggiornato.");
        add("ready_count", "%d ready", "%d à récupérer", "%d bereit", "%d disponibles", "%d disponíveis", "%d disponibili");
        add("objectives", "Objectives", "Objectifs", "Ziele", "Objetivos", "Objetivos", "Obiettivi");
        add("track_item", "Set as my goal", "Choisir comme objectif", "Als Ziel wählen", "Elegir como objetivo", "Escolher como objetivo", "Scegli come obiettivo");
        add("untrack_item", "Remove this goal", "Retirer cet objectif", "Ziel entfernen", "Quitar este objetivo", "Remover este objetivo", "Rimuovi questo obiettivo");
        add("tracked_goal", "My chosen goal", "Mon objectif choisi", "Mein gewähltes Ziel", "Mi objetivo elegido", "O meu objetivo escolhido", "Il mio obiettivo scelto");
        add("view_source", "How to obtain", "Comment l’obtenir", "So erhältst du es", "Cómo conseguirlo", "Como obter", "Come ottenerlo");
        add("preview", "Preview", "Aperçu", "Vorschau", "Vista previa", "Pré-visualizar", "Anteprima");
        add("preview_only", "Visual preview · no item granted", "Aperçu visuel · aucun objet accordé", "Nur Vorschau · kein Gegenstand freigeschaltet", "Solo vista previa · no recibes objetos", "Apenas pré-visualização · nenhum objeto atribuído", "Solo anteprima · nessun oggetto assegnato");
        add("wear_preview", "Apply owned items", "Porter les objets possédés", "Eigene Gegenstände anlegen", "Usar los objetos que posees", "Usar os objetos que possuis", "Indossa gli oggetti posseduti");
        add("reset_preview", "Reset preview", "Réinitialiser l’aperçu", "Vorschau zurücksetzen", "Restablecer vista previa", "Repor a pré-visualização", "Ripristina anteprima");
        add("look", "My look", "Mon look", "Mein Look", "Mi estilo", "O meu visual", "Il mio look");
        add("benefits", "Equipment effects", "Effets de l’équipement", "Ausrüstungseffekte", "Efectos del equipo", "Efeitos do equipamento", "Effetti dell’equipaggiamento");
        add("cosmetic_slots", "4 appearance slots", "4 emplacements d’apparence", "4 Plätze fürs Aussehen", "4 espacios de apariencia", "4 espaços de aparência", "4 spazi per l’aspetto");
        add("gear_slots", "3 equipment slots", "3 emplacements d’équipement", "3 Ausrüstungsplätze", "3 espacios de equipo", "3 espaços de equipamento", "3 spazi per l’equipaggiamento");
        add("owned_count", "Owned: %d / %d", "Possédés : %d / %d", "Im Besitz: %d / %d", "En propiedad: %d / %d", "Possuídos: %d / %d", "Posseduti: %d / %d");
        add("collection_goal", "Collection goal", "Objectif de collection", "Sammlungsziel", "Objetivo de colección", "Objetivo de coleção", "Obiettivo di collezione");
        add("goal_owned", "You own this item", "Tu possèdes cet objet", "Du besitzt diesen Gegenstand", "Ya tienes este objeto", "Já possuis este objeto", "Possiedi questo oggetto");
        add("goal_progress", "%d / %d completed", "%d / %d accomplis", "%d / %d geschafft", "%d / %d completados", "%d / %d concluídos", "%d / %d completati");
        add("next_action", "Next step", "Prochaine étape", "Nächster Schritt", "Siguiente paso", "Próximo passo", "Prossimo passo");
        add("claim_daily", "Collect today’s gift", "Récupérer le cadeau du jour", "Heutiges Geschenk abholen", "Recoger el regalo de hoy", "Receber o presente de hoje", "Ritira il regalo di oggi");
        add("play_journey", "Continue the Journey", "Continuer le parcours", "Reise fortsetzen", "Continuar el camino", "Continuar o percurso", "Continua il percorso");
        add("play_daily", "Play today’s stages", "Jouer les étapes du jour", "Heutige Etappen spielen", "Jugar las etapas de hoy", "Jogar as etapas de hoje", "Gioca le tappe di oggi");
        add("play_weekly", "Play this week’s stages", "Jouer les étapes de la semaine", "Wochenetappen spielen", "Jugar las etapas semanales", "Jogar as etapas da semana", "Gioca le tappe settimanali");
        add("all_claimed", "All collected", "Tout est récupéré", "Alles abgeholt", "Todo recogido", "Tudo recebido", "Tutto ritirato");
        add("ready", "Ready to collect", "À récupérer", "Bereit zum Abholen", "Listo para recoger", "Pronto a receber", "Pronto da ritirare");
        add("not_ready", "Not ready yet", "Pas encore disponible", "Noch nicht bereit", "Aún no disponible", "Ainda não disponível", "Non ancora disponibile");
        add("progress", "Progress", "Progression", "Fortschritt", "Progreso", "Progresso", "Progressi");
        add("prism_story", "Restore the Prism at your pace.", "Restaure le Prisme à ton rythme.", "Stelle das Prisma in deinem Tempo wieder her.", "Restaura el Prisma a tu ritmo.", "Restaura o Prisma ao teu ritmo.", "Ripristina il Prisma al tuo ritmo.");
        add("chapter", "Chapter %d", "Chapitre %d", "Kapitel %d", "Capítulo %d", "Capítulo %d", "Capitolo %d");
        add("story_progress", "Story: %d / %d", "Histoire : %d / %d", "Geschichte: %d / %d", "Historia: %d / %d", "História: %d / %d", "Storia: %d / %d");
        add("back_journey", "Back to Journey", "Retour au parcours", "Zurück zur Reise", "Volver al camino", "Voltar ao percurso", "Torna al percorso");
        add("claim_reward", "Collect reward", "Récupérer la récompense", "Belohnung abholen", "Recoger recompensa", "Receber recompensa", "Ritira ricompensa");
        add("open_inventory", "Open inventory", "Ouvrir l’inventaire", "Inventar öffnen", "Abrir inventario", "Abrir inventário", "Apri inventario");
        add("empty_goal", "No collection goal selected", "Aucun objectif de collection choisi", "Kein Sammlungsziel gewählt", "Ningún objetivo de colección elegido", "Nenhum objetivo de coleção escolhido", "Nessun obiettivo di collezione scelto");
        add("choose_goal", "Choose an item to work towards", "Choisir un objet à obtenir", "Wähle einen Gegenstand als Ziel", "Elige un objeto como objetivo", "Escolhe um objeto como objetivo", "Scegli un oggetto da ottenere");
        add("show_avatar", "View my companion", "Voir mon compagnon", "Meinen Begleiter ansehen", "Ver a mi compañero", "Ver o meu companheiro", "Vedi il mio compagno");
        add("unlock_source", "Obtain through", "S’obtient avec", "Erhältlich durch", "Se consigue con", "Obtém-se com", "Si ottiene con");
        add("source_not_purchase", "View the source · no purchase made", "Voir la source · aucun achat effectué", "Quelle ansehen · kein Kauf", "Ver la fuente · no realiza compras", "Ver a origem · sem efetuar compra", "Vedi la fonte · nessun acquisto");
        add("gold_account", "Account wallet", "Portefeuille du compte", "Kontoguthaben", "Monedero de la cuenta", "Carteira da conta", "Portafoglio dell’account");
        add("gold_closed", "Real checkout stays closed in this candidate.", "Les achats réels restent fermés dans cette version candidate.", "Echte Käufe bleiben in dieser Testversion deaktiviert.", "Las compras reales siguen desactivadas en esta versión candidata.", "As compras reais continuam desativadas nesta versão candidata.", "Gli acquisti reali restano disattivati in questa versione candidata.");
        add("view_wallet", "View wallet", "Voir le portefeuille", "Guthaben ansehen", "Ver monedero", "Ver carteira", "Vedi portafoglio");
        add("source_account", "Owned by your account", "Possédé sur ton compte", "Im Besitz deines Kontos", "En propiedad de tu cuenta", "Pertence à tua conta", "Posseduto dal tuo account");
        add("source_capsule", "Earned cosmetic capsule", "Capsule cosmétique gagnée", "Verdiente Kosmetikkapsel", "Cápsula cosmética ganada", "Cápsula cosmética ganha", "Capsula cosmetica guadagnata");
        add("acquired_with", "Obtained through %s", "Obtenu avec %s", "Erhalten durch %s", "Obtenido con %s", "Obtido com %s", "Ottenuto con %s");
    }
    private static final Set<String> KEYS = Collections.unmodifiableSet(ROWS.keySet());
    private V110Strings() { }
    private static void add(String key, String... values) {
        if (values.length != 6 || ROWS.put(key, values) != null) throw new IllegalArgumentException(key);
    }
    public static String get(String language, String key, Object... arguments) {
        String[] row = ROWS.get(key);
        if (row == null) throw new IllegalArgumentException("Unknown 1.10 string: " + key);
        String lang = Strings.normalize(language);
        int column = lang.equals("fr") ? 1 : lang.equals("de") ? 2 : lang.equals("es") ? 3 : lang.equals("pt") ? 4 : lang.equals("it") ? 5 : 0;
        return String.format(Locale.forLanguageTag(lang), row[column], arguments);
    }
    public static Set<String> keys() { return KEYS; }
}
