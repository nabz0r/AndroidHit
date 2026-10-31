package com.androidhit.lumadrift.data;
import java.util.Locale;
/** Context and reversible wardrobe vocabulary, in the six shipped languages. */
public final class Clarity123Strings {
 private static final String[][] COPY={
 {"companions_charge","Compagnons · Charge %d/%d","Companions · Charge %d/%d","Begleiter · Ladung %d/%d","Compañeros · Carga %d/%d","Companheiros · Carga %d/%d","Compagni · Carica %d/%d"},
 {"charge_available","%d Charge disponible","%d Charge available","%d Ladung verfügbar","%d de Carga disponible","%d de Carga disponível","%d Carica disponibile"},
 {"talents","Talents et Focus","Skills and Focus","Talente und Fokus","Talentos y Enfoque","Talentos e Foco","Talenti e Focus"},
 {"puzzle_talent","Talent du puzzle · une fois par essai","Puzzle skill · once per attempt","Puzzletalent · einmal je Versuch","Talento del puzle · una vez por intento","Talento do puzzle · uma vez por tentativa","Talento puzzle · una volta per tentativo"},
 {"team_next","L’équipe se prépare entre deux essais.","Prepare your squad between attempts.","Bereite das Team zwischen Versuchen vor.","Prepara el equipo entre intentos.","Prepara a equipa entre tentativas.","Prepara la squadra tra i tentativi."},
 {"equipment_next","L’équipement de jeu se prépare entre deux essais. La tenue change seulement l’apparence.","Prepare gameplay equipment between attempts. Outfits only change appearance.","Spielausrüstung zwischen Versuchen vorbereiten. Kleidung ändert nur das Aussehen.","Prepara el equipo entre intentos. El atuendo solo cambia la apariencia.","Prepara o equipamento entre tentativas. A roupa só muda a aparência.","Prepara l’equipaggiamento tra i tentativi. L’abito cambia solo l’aspetto."},
 {"outfit","Tenue","Outfit","Kleidung","Atuendo","Roupa","Abito"},
 {"equipment","Équipement","Equipment","Ausrüstung","Equipo","Equipamento","Equipaggiamento"},
 {"return_puzzle","Revenir au puzzle","Return to puzzle","Zurück zum Puzzle","Volver al puzle","Voltar ao puzzle","Torna al puzzle"},
 {"trying","En essai","Trying on","Anprobe","En prueba","Em prova","In prova"},
 {"worn","Porté","Worn","Getragen","Puesto","Vestido","Indossato"},
 {"apply","Appliquer","Apply","Anwenden","Aplicar","Aplicar","Applica"},
 {"cancel","Annuler l’essai","Cancel try-on","Anprobe abbrechen","Cancelar prueba","Cancelar prova","Annulla prova"},
 {"outfit_context","%s · tenue visuelle commune","%s · shared visual outfit","%s · gemeinsame Kleidung","%s · atuendo visual compartido","%s · roupa visual comum","%s · abito visivo condiviso"},
 {"outfit_note","Tenue complète · sans effet sur le puzzle.","Full outfit · no puzzle effect.","Komplettes Outfit · kein Puzzleeffekt.","Atuendo completo · sin efecto en el puzle.","Roupa completa · sem efeito no puzzle.","Abito completo · nessun effetto sul puzzle."},
 {"show_home","Afficher à l’accueil","Show at Home","Zu Hause anzeigen","Mostrar en Inicio","Mostrar no Início","Mostra in Casa"},
 {"home_active","Présent à l’accueil · équipe inchangée","Shown at Home · squad unchanged","Zu Hause angezeigt · Team unverändert","En Inicio · equipo sin cambios","No Início · equipa inalterada","In Casa · squadra invariata"},
 {"expedition_skill","Talent d’expédition","Expedition skill","Expeditionstalent","Talento de expedición","Talento de expedição","Talento spedizione"},
 {"saved_elements","Essai repris · anciens éléments conservés","Resumed attempt · original elements kept","Fortgesetzter Versuch · alte Elemente behalten","Intento retomado · elementos originales","Tentativa retomada · elementos originais","Tentativo ripreso · elementi originali"}
 };
 public static String get(String language,String key,Object...args){int col="fr".equals(language)?1:"de".equals(language)?3:"es".equals(language)?4:"pt".equals(language)?5:"it".equals(language)?6:2;for(String[]row:COPY)if(row[0].equals(key))return String.format(Locale.ROOT,row[col],args);throw new IllegalArgumentException("Unknown clarity string: "+key);}
 private Clarity123Strings(){}
}
