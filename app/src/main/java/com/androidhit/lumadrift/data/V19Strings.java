package com.androidhit.lumadrift.data;
import java.util.*;
/** Visible journey, reward and guardian copy. No demographic personalization. */
public final class V19Strings {
 private static final Map<String,String[]> ROWS=new LinkedHashMap<>();
 static {
  add("gifts","Journey gifts","Cadeaux du parcours","Reisegeschenke","Regalos del camino","Presentes do percurso","Premi del percorso");
  add("claim_gift","Collect %s · ×1","Récupérer %s · ×1","%s abholen · ×1","Recoger %s · ×1","Receber %s · ×1","Ritira %s · ×1");
  add("gift_progress","%d / %d clears · next: %s","%d / %d victoires · prochain : %s","%d / %d Siege · als Nächstes: %s","%d / %d victorias · siguiente: %s","%d / %d vitórias · próximo: %s","%d / %d vittorie · prossimo: %s");
  add("trial","Try %s · free ×1","Essayer %s · offert ×1","%s testen · gratis ×1","Probar %s · gratis ×1","Testar %s · grátis ×1","Prova %s · gratis ×1");
  add("trials","New powers to try","De nouveaux pouvoirs à essayer","Neue Kräfte zum Testen","Nuevos poderes para probar","Novos poderes para testar","Nuovi poteri da provare");
  add("trial_note","One trial charge per power. Keep it until you choose to use it.","Une charge offerte par pouvoir. Garde-la pour le moment de ton choix.","Eine kostenlose Ladung je Kraft. Nutze sie, wann du möchtest.","Una carga gratis por poder. Úsala cuando quieras.","Uma carga grátis por poder. Usa-a quando quiseres.","Una carica gratis per potere. Usala quando vuoi.");
  add("stock_full","Inventory full · gift kept for later","Stock plein · cadeau conservé pour plus tard","Inventar voll · Geschenk bleibt verfügbar","Inventario lleno · regalo guardado","Inventário cheio · presente guardado","Inventario pieno · premio conservato");
  add("score","3★: up to %d actions · hints and Undo do not lower stars","3★ : jusqu’à %d coups · indices et Annuler ne retirent pas d’étoile","3★: bis %d Aktionen · Hinweise und Zurück ohne Sternabzug","3★: hasta %d acciones · pistas y deshacer sin penalización","3★: até %d ações · pistas e desfazer sem penalização","3★: fino a %d azioni · indizi e annulla senza penalità");
  add("next_plan","More gate choices ahead. Preview switches and undo freely.","Plus de choix de portes ici. Prévisualise les interrupteurs et annule librement.","Mehr Torentscheidungen. Vorschau nutzen und kostenlos zurückgehen.","Más decisiones de puertas. Mira la vista previa y deshaz gratis.","Mais escolhas de portas. Vê a prévia e desfaz grátis.","Più scelte tra porte. Guarda l’anteprima e annulla gratis.");
  add("practice_gate","Try the gate lesson again","Revoir la leçon des portes","Torlektion wiederholen","Repetir lección de puertas","Rever a lição das portas","Ripeti la lezione delle porte");
  add("preview_gate","Preview the glowing gate","Voir l’aperçu de la porte lumineuse","Vorschau des leuchtenden Tors","Ver la puerta brillante","Ver a porta brilhante","Anteprima della porta luminosa");
  add("styles","Avatar studio","Studio des avatars","Avatarstudio","Estudio de avatares","Estúdio de avatares","Studio degli avatar");
  add("style_count","24 companions × 12 styles = 288 looks · all free","24 compagnons × 12 styles = 288 looks · tous gratuits","24 Begleiter × 12 Stile = 288 Looks · kostenlos","24 compañeros × 12 estilos = 288 looks · gratis","24 companheiros × 12 estilos = 288 visuais · grátis","24 compagni × 12 stili = 288 look · gratis");
  add("style_choose","Choose this style","Choisir ce style","Diesen Stil wählen","Elegir este estilo","Escolher este estilo","Scegli questo stile");
  add("style_active","Active style","Style actif","Aktiver Stil","Estilo activo","Estilo ativo","Stile attivo");
  add("guardians","Prism guardians","Les gardiens du Prisme","Prismawächter","Guardianes del Prisma","Guardiões do Prisma","Guardiani del Prisma");
  add("guardian","Guardian %d","Gardien %d","Wächter %d","Guardián %d","Guardião %d","Guardiano %d");
  add("guardian_unlock","A new encounter every 10 Journey clears","Une rencontre tous les 10 niveaux terminés","Eine Begegnung je 10 abgeschlossene Level","Un encuentro cada 10 niveles completados","Um encontro a cada 10 níveis concluídos","Un incontro ogni 10 livelli completati");
  add("guardian_locked","Complete %d Journey levels","Termine %d niveaux du parcours","Schließe %d Reiselevel ab","Completa %d niveles del camino","Conclui %d níveis do percurso","Completa %d livelli del percorso");
  add("guardian_story","Nova restores the Prism, one guardian at a time. This encounter is optional; your Journey remains open.","Nova restaure le Prisme, un gardien après l’autre. Cette rencontre est facultative ; ton parcours reste ouvert.","Nova stellt das Prisma wieder her. Die Begegnung ist optional; die Reise bleibt offen.","Nova restaura el Prisma. Este encuentro es opcional; tu camino sigue abierto.","Nova restaura o Prisma. Este encontro é opcional; o percurso continua aberto.","Nova ripristina il Prisma. L’incontro è facoltativo; il percorso resta aperto.");
  add("guardian_work","%d / %d layers and steps cleared","%d / %d couches et pas libérés","%d / %d Schichten und Schritte geschafft","%d / %d capas y pasos despejados","%d / %d camadas e passos libertos","%d / %d strati e passi liberati");
  add("guardian_double","Thick pieces: first tap peels a layer, next tap moves it.","Pièces épaisses : le premier geste retire une couche, le suivant les déplace.","Dicke Teile: erst eine Schicht lösen, dann bewegen.","Piezas gruesas: primero quita una capa, luego mueve.","Peças espessas: primeiro retira uma camada, depois move.","Pezzi spessi: prima rimuovi uno strato, poi muovi.");
  add("guardian_step","Footprints: move one cell per action, then leave the edge.","Empreintes : avance d’une case par coup, puis sors par le bord.","Fußspuren: ein Feld je Aktion, dann über den Rand hinaus.","Huellas: una casilla por acción, luego sal por el borde.","Pegadas: uma casa por ação, depois sai pela borda.","Impronte: una casella per azione, poi esci dal bordo.");
  add("guardian_reward","Collect the Prism fragment · +100 LumaCoins","Récupérer le fragment du Prisme · +100 LumaCoins","Prismafragment abholen · +100 LumaCoins","Recoger fragmento del Prisma · +100 LumaCoins","Receber fragmento do Prisma · +100 LumaCoins","Ritira frammento del Prisma · +100 LumaCoins");
  add("guardian_done","Prism fragment restored","Fragment du Prisme restauré","Prismafragment wiederhergestellt","Fragmento del Prisma restaurado","Fragmento do Prisma restaurado","Frammento del Prisma ripristinato");
  add("guardian_hint","Show the next gesture · free","Montrer le prochain geste · gratuit","Nächsten Schritt zeigen · gratis","Mostrar siguiente gesto · gratis","Mostrar próximo gesto · grátis","Mostra prossimo gesto · gratis");
  add("guardian_piece","Piece %d · %s · %d layer(s)%s","Pièce %d · %s · %d couche(s)%s","Teil %d · %s · %d Schicht(en)%s","Pieza %d · %s · %d capa(s)%s","Peça %d · %s · %d camada(s)%s","Pezzo %d · %s · %d strato/i%s");
  add("guardian_cell"," · one cell"," · une case"," · ein Feld"," · una casilla"," · uma casa"," · una casella");
  add("guardian_empty","Every layer is cleared. Your fragment is ready.","Toutes les couches sont libérées. Ton fragment est prêt.","Alle Schichten sind frei. Dein Fragment ist bereit.","Todas las capas están libres. Tu fragmento está listo.","Todas as camadas estão livres. O fragmento está pronto.","Tutti gli strati sono liberi. Il frammento è pronto.");
  add("guardian_retry","No moves left. Undo or restart for free; nothing is lost.","Plus de coups. Annule ou recommence gratuitement ; rien n’est perdu.","Keine Züge mehr. Kostenlos zurück oder neu starten.","Sin movimientos. Deshaz o reinicia gratis; no pierdes nada.","Sem jogadas. Desfaz ou recomeça grátis; nada se perde.","Mosse finite. Annulla o ricomincia gratis; non perdi nulla.");
 }
 private static void add(String key,String...values){if(values.length!=6||ROWS.put(key,values)!=null)throw new IllegalArgumentException(key);}
 public static String get(String language,String key,Object...args){String[] v=ROWS.get(key);if(v==null)throw new IllegalArgumentException(key);String lang=Strings.normalize(language);int i=Arrays.asList("en","fr","de","es","pt","it").indexOf(lang);return String.format(Locale.forLanguageTag(lang),v[Math.max(0,i)],args);}
 public static Set<String> keys(){return Collections.unmodifiableSet(ROWS.keySet());}
 private V19Strings(){}
}
