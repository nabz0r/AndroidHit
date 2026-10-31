package com.androidhit.lumadrift.data;

import java.util.*;

/** Contextual, visible instruction copy for the reversible strategy release. */
public final class V18Strings {
    private static final Map<String,String[]> ROWS=new LinkedHashMap<>();
    static {
        add("focus_desc","Highlight the next optimal exit or gate opening for up to 10 seconds, until your next action.","Éclaire la prochaine sortie ou ouverture optimale pendant 10 s maximum, jusqu’à ta prochaine action.","Markiert den nächsten optimalen Ausgang oder die Toröffnung für bis zu 10 Sekunden, bis zur nächsten Aktion.","Resalta la próxima salida o apertura óptima durante 10 s como máximo, hasta tu siguiente acción.","Destaca a próxima saída ou abertura ótima até 10 s, até à tua próxima ação.","Evidenzia la prossima uscita o apertura ottimale per massimo 10 s, fino alla prossima azione.");
        add("later","Later","Plus tard","Später","Más tarde","Mais tarde","Più tardi");
        add("open_short","Open · 1 action","Ouvrir · 1 action","Öffnen · 1 Aktion","Abrir · 1 acción","Abrir · 1 ação","Apri · 1 azione");
        add("confirm_short","Play · 1 action","Jouer · 1 action","Spielen · 1 Aktion","Jugar · 1 acción","Jogar · 1 ação","Gioca · 1 azione");
        add("thinking","Planning the route…","Je cherche le chemin…","Ich plane den Weg…","Buscando el camino…","A procurar o caminho…","Cerco il percorso…");
        add("learn","Little star school","La petite école des étoiles","Die kleine Sternenschule","La pequeña escuela estelar","A pequena escola das estrelas","La piccola scuola delle stelle");
        add("learn_note","7 tiny puzzles · learn by playing","7 mini puzzles · apprends en jouant","7 kleine Rätsel · spielend lernen","7 minipuzles · aprende jugando","7 minipuzzles · aprende jogando","7 minipuzzle · impara giocando");
        add("learn_reward","First completion: 100 LumaCoins + 2 Sparks","Première réussite : 100 LumaCoins + 2 Étincelles","Erster Abschluss: 100 LumaCoins + 2 Funken","Primera vez: 100 LumaCoins + 2 Chispas","Primeira vez: 100 LumaCoins + 2 Faíscas","Primo completamento: 100 LumaCoins + 2 Scintille");
        add("learn_replay","Replay the interactive tutorial","Rejouer le tutoriel interactif","Interaktive Einführung wiederholen","Repetir el tutorial interactivo","Repetir o tutorial interativo","Ripeti il tutorial interattivo");
        add("learn_continue","Continue learning","Reprendre le tutoriel","Weiterlernen","Seguir aprendiendo","Continuar aprendendo","Continua a imparare");
        add("learn_start","Let's try!","On essaie !","Probieren wir's!","¡Vamos a probar!","Vamos tentar!","Proviamo!");
        add("learn_skip","Play at my pace","Jouer à mon rythme","In meinem Tempo spielen","Jugar a mi ritmo","Jogar no meu ritmo","Gioca al tuo ritmo");
        add("learn_skip_note","You can return here from Help. Your lesson is saved.","Tu pourras revenir depuis l’aide. Ta leçon est sauvegardée.","Du kannst über Hilfe zurückkehren. Deine Lektion wird gespeichert.","Puedes volver desde Ayuda. Tu lección queda guardada.","Podes voltar pela Ajuda. A tua lição fica guardada.","Puoi tornare da Aiuto. La lezione viene salvata.");
        add("learn_practice","Practice · no moves or powers spent","Entraînement · aucun coup ni pouvoir dépensé","Übung · keine Züge oder Kräfte verbraucht","Práctica · sin gastar movimientos ni poderes","Prática · sem gastar jogadas ou poderes","Pratica · senza consumare mosse o poteri");
        add("lesson","Lesson %d / %d","Leçon %d / %d","Lektion %d / %d","Lección %d / %d","Lição %d / %d","Lezione %d / %d");
        add("preview","See what changes","Voir ce qui change","Änderungen ansehen","Ver qué cambia","Ver o que muda","Guarda cosa cambia");
        add("confirm_move","Make this move · 1 action","Jouer ce coup · 1 action","Diesen Zug spielen · 1 Aktion","Jugar · 1 acción","Jogar · 1 ação","Gioca · 1 azione");
        add("cancel_preview","Keep planning","Continuer à réfléchir","Weiter überlegen","Seguir pensando","Continuar pensando","Continua a pensare");
        add("opening","Open %s gate · 1 action","Ouvrir la porte %s · 1 action","Tor %s öffnen · 1 Aktion","Abrir puerta %s · 1 acción","Abrir porta %s · 1 ação","Apri porta %s · 1 azione");
        add("gate_action","Tap a closed gate to open it for 1 action.","Touche une porte fermée pour l’ouvrir en 1 action.","Tippe ein geschlossenes Tor an: Öffnen kostet 1 Aktion.","Toca una puerta cerrada: abrirla cuesta 1 acción.","Toca numa porta fechada: abrir custa 1 ação.","Tocca una porta chiusa: aprirla costa 1 azione.");
        add("opens","%s: opens","%s : s’ouvre","%s: öffnet","%s: se abre","%s: abre","%s: si apre");
        add("closes","%s: closes","%s : se ferme","%s: schließt","%s: se cierra","%s: fecha","%s: si chiude");
        add("stays_open","%s: open","%s : ouverte","%s: offen","%s: abierta","%s: aberta","%s: aperta");
        add("stays_closed","%s: closed","%s : fermée","%s: geschlossen","%s: cerrada","%s: fechada","%s: chiusa");
        add("preview_note","These are the immediate gate changes. Plan the following exits too.","Voici les changements immédiats des portes. Prévois aussi les sorties suivantes.","Dies sind die direkten Toränderungen. Plane auch die nächsten Ausgänge.","Estos son los cambios inmediatos. Planea también las próximas salidas.","Estas são as mudanças imediatas. Planeia também as próximas saídas.","Questi sono i cambi immediati. Pianifica anche le prossime uscite.");
        add("undo_refund","Undo free · last action refunded","Annuler gratuitement · dernier coup remboursé","Kostenlos zurück · letzte Aktion erstattet","Deshacer gratis · última acción devuelta","Desfazer grátis · última ação devolvida","Annulla gratis · ultima azione restituita");
        add("recover","Return to an open path","Revenir à un chemin ouvert","Zu einem offenen Weg zurück","Volver a un camino abierto","Voltar a um caminho aberto","Torna a un percorso aperto");
        add("recover_note","Undo the last gate choice for free, including at zero moves. Restarting is free too.","Annule le dernier choix de porte gratuitement, même à zéro coup. Recommencer est aussi gratuit.","Mache die letzte Torwahl kostenlos rückgängig, auch bei null Zügen. Ein Neustart ist ebenfalls kostenlos.","Deshaz la última puerta gratis, incluso con cero movimientos. Reiniciar también es gratis.","Desfaz a última escolha de porta grátis, mesmo sem jogadas. Recomeçar também é grátis.","Annulla l’ultima scelta di porta gratis, anche a zero mosse. Anche ricominciare è gratis.");
        add("budget_strategy","%d minimum actions · %d extra for planning","%d actions minimum · %d de marge","Mindestens %d Aktionen · %d Reserve","Mínimo %d acciones · %d de margen","Mínimo %d ações · %d de margem","Minimo %d azioni · %d di margine");
        add("goal","%d little stars to guide home","%d petites étoiles à raccompagner","%d kleine Sterne nach Hause führen","%d estrellitas por guiar a casa","%d estrelinhas para levar a casa","%d stelline da guidare a casa");
        add("nearly_home","Just %d to guide home","Encore %d à raccompagner","Nur noch %d nach Hause führen","Solo faltan %d por guiar","Só faltam %d para guiar","Ancora %d da guidare");
        add("path_progress","%d / %d stars home","%d / %d étoiles rentrées","%d / %d Sterne zu Hause","%d / %d estrellas en casa","%d / %d estrelas em casa","%d / %d stelle a casa");
        add("plan_tip","One exit or gate opening = one action. You can undo freely.","Une sortie ou une ouverture de porte = une action. Tu peux annuler gratuitement.","Ein Ausgang oder eine Toröffnung = eine Aktion. Zurückgehen ist kostenlos.","Una salida o abrir una puerta = una acción. Deshacer es gratis.","Uma saída ou abrir uma porta = uma ação. Desfazer é grátis.","Un’uscita o un’apertura = un’azione. Annullare è gratis.");
        add("hint_gate","Follow the glowing gate","Suis la porte lumineuse","Folge dem leuchtenden Tor","Sigue la puerta brillante","Segue a porta brilhante","Segui la porta luminosa");
        add("gift_near","%d more cleared levels to your next gift","Encore %d niveaux pour ton prochain cadeau","Noch %d Level bis zum nächsten Geschenk","Faltan %d niveles para tu regalo","Faltam %d níveis para o presente","Ancora %d livelli per il prossimo regalo");
        add("gift_ready","Your earned gift is ready","Ton cadeau gagné est prêt","Dein erspieltes Geschenk ist bereit","Tu regalo ganado está listo","O teu presente ganho está pronto","Il premio guadagnato è pronto");
        add("collection_goal","%d / 24 looks collected","%d / 24 styles collectionnés","%d / 24 Looks gesammelt","%d / 24 estilos coleccionados","%d / 24 estilos colecionados","%d / 24 stili collezionati");
        add("choose_action","Choose an exit or open a gate","Choisis une sortie ou ouvre une porte","Wähle einen Ausgang oder öffne ein Tor","Elige una salida o abre una puerta","Escolhe uma saída ou abre uma porta","Scegli un’uscita o apri una porta");
        add("well_done","Lovely! You did it.","Joli ! Tu as réussi.","Schön! Geschafft.","¡Bien! Lo lograste.","Boa! Conseguiste.","Bello! Ci sei riuscito.");
        add("practice_hint","Try a free Spark","Essayer une Étincelle gratuite","Einen kostenlosen Funken testen","Probar una Chispa gratis","Experimentar uma Faísca grátis","Prova una Scintilla gratis");
        add("practice_undo","Try undoing for free","Essayer le retour arrière gratuit","Kostenlos zurückgehen testen","Probar deshacer gratis","Experimentar desfazer grátis","Prova ad annullare gratis");
        add("practice_again","Try this lesson again","Refaire cette leçon","Lektion wiederholen","Repetir esta lección","Repetir esta lição","Ripeti questa lezione");
        add("continue_lesson","Next little lesson","La petite leçon suivante","Nächste kleine Lektion","Siguiente lección","Próxima lição","Prossima lezione");
        add("tutorial_done","Ready for your adventure!","Prêt pour ton aventure !","Bereit für dein Abenteuer!","¡Listo para tu aventura!","Pronto para a aventura!","Pronto per l’avventura!");
        add("tutorial_claim","Collect my welcome gift","Récupérer mon cadeau de bienvenue","Willkommensgeschenk abholen","Recoger mi regalo de bienvenida","Receber o presente de boas-vindas","Ritira il premio di benvenuto");
        add("tutorial_collected","Welcome gift already collected","Cadeau de bienvenue déjà reçu","Willkommensgeschenk schon abgeholt","Regalo de bienvenida ya recibido","Presente de boas-vindas já recebido","Premio di benvenuto già ricevuto");
        add("start_adventure","Start my adventure","Commencer mon aventure","Mein Abenteuer starten","Empezar mi aventura","Começar a aventura","Inizia la tua avventura");
    }
    private static void add(String key,String... values){if(values.length!=6||ROWS.put(key,values)!=null)throw new IllegalArgumentException(key);}
    public static String get(String language,String key,Object...args){String[] values=ROWS.get(key);if(values==null)throw new IllegalArgumentException(key);String lang=Strings.normalize(language);int index=Arrays.asList("en","fr","de","es","pt","it").indexOf(lang);return String.format(Locale.forLanguageTag(lang),values[Math.max(0,index)],args);}
    public static Set<String> keys(){return Collections.unmodifiableSet(ROWS.keySet());}
    private V18Strings(){}
}
