package com.androidhit.lumadrift.data;
import java.util.Locale;
/** Concrete pause, charge and usage vocabulary; no gameplay state lives in copy. */
public final class Clarity124Strings {
 private static final String[][] COPY={
 {"pause","Petite pause","Take a breath","Kurze Pause","Una pausa","Uma pausa","Una pausa"},
 {"pause_note","Ton essai t’attend. Le temps est en pause.","Your attempt is waiting. The clock is paused.","Dein Versuch wartet. Die Zeit pausiert.","Tu intento te espera. El reloj está en pausa.","A tua tentativa espera. O relógio está em pausa.","Il tentativo ti aspetta. Il tempo è in pausa."},
 {"restart","Repartir du début ?","Start again?","Von vorne beginnen?","¿Volver a empezar?","Recomeçar?","Ricominciare?"},
 {"restart_note","Les flèches reviennent au départ. Tes gains déjà obtenus restent acquis.","The arrows return to the start. You keep rewards already earned.","Die Pfeile kehren zum Start zurück. Verdiente Belohnungen bleiben.","Las flechas vuelven al inicio. Conservas las recompensas obtenidas.","As setas voltam ao início. Manténs as recompensas recebidas.","Le frecce tornano all’inizio. I premi già ottenuti restano tuoi."},
 {"restart_free","Énergie : gratuit","Energy: free","Energie: kostenlos","Energía: gratis","Energia: grátis","Energia: gratis"},
 {"restart_paid","Coût : 1 énergie","Cost: 1 energy","Kosten: 1 Energie","Coste: 1 energía","Custo: 1 energia","Costo: 1 energia"},
 {"session_energy","Énergie de session","Session energy","Sitzungsenergie","Energía de sesión","Energia de sessão","Energia di sessione"},
 {"player_rank","Rang","Rank","Rang","Rango","Patamar","Grado"},
 {"talent_charge","Charge des talents","Skill Charge","Talentladung","Carga de talentos","Carga dos talentos","Carica talenti"},
 {"uses","Usages : %d/%d","Uses: %d/%d","Nutzungen: %d/%d","Usos: %d/%d","Usos: %d/%d","Usi: %d/%d"},
 {"uses_ax","Usages restants : %d sur %d","Uses remaining: %d out of %d","Verbleibende Nutzungen: %d von %d","Quedan %d de %d usos","Restam %d de %d usos","Restano %d usi su %d"},
 {"uses_empty","Usages épuisés pour cet essai","No uses left this attempt","Keine Nutzungen in diesem Versuch übrig","Sin usos en este intento","Sem usos nesta tentativa","Usi esauriti per questo tentativo"},
 {"skill_cost","Coût : %d Charge","Cost: %d Charge","Kosten: %d Ladung","Coste: %d de Carga","Custo: %d de Carga","Costo: %d Carica"},
 {"skill_attempt","Utilisable pendant cet essai","Available during this attempt","In diesem Versuch nutzbar","Disponible en este intento","Disponível nesta tentativa","Disponibile in questo tentativo"},
 {"outfit_only","Tenue · visuel seulement","Outfit · appearance only","Kleidung · nur Aussehen","Atuendo · solo apariencia","Roupa · só aparência","Abito · solo aspetto"},
 {"next_attempt","Entre deux essais","Between attempts","Zwischen Versuchen","Entre intentos","Entre tentativas","Tra i tentativi"},
 {"rank_reached","Rang %d atteint","Rank %d reached","Rang %d erreicht","Rango %d alcanzado","Patamar %d alcançado","Grado %d raggiunto"},
 {"captain_copy","%s · Copies : +%d","%s · Copies: +%d","%s · Kopien: +%d","%s · Copias: +%d","%s · Cópias: +%d","%s · Copie: +%d"},
 {"captain_max","%s au maximum · +%d LumaCoins","%s at maximum · +%d LumaCoins","%s auf Maximum · +%d LumaCoins","%s al máximo · +%d LumaCoins","%s no máximo · +%d LumaCoins","%s al massimo · +%d LumaCoins"},
 {"next_capacity","%s au prochain puzzle : niveau %d · %d usages","%s next puzzle: level %d · %d uses","%s im nächsten Puzzle: Level %d · %d Nutzungen","%s en el próximo puzzle: nivel %d · %d usos","%s no próximo puzzle: nível %d · %d usos","%s al prossimo puzzle: livello %d · %d usi"},
 {"rank_toward","Rang %d · %d/120 XP","Rank %d · %d/120 XP","Rang %d · %d/120 XP","Rango %d · %d/120 XP","Patamar %d · %d/120 XP","Grado %d · %d/120 XP"},
 {"legacy_captain","Ancien essai : aucun capitaine enregistré","Older attempt: no captain recorded","Alter Versuch: kein Kapitän gespeichert","Intento anterior: sin capitán registrado","Tentativa anterior: sem capitão registado","Tentativo precedente: nessun capitano registrato"},
 {"capacity_fixed","Les usages sont fixés au départ de chaque essai","Uses are set at the start of each attempt","Nutzungen werden zu Beginn jedes Versuchs festgelegt","Los usos se fijan al iniciar cada intento","Os usos são definidos no início de cada tentativa","Gli usi sono fissati all’inizio di ogni tentativo"},
 {"try_on","Essayer","Try on","Anprobieren","Probar","Experimentar","Prova"}
 };
 public static String get(String lang,String key,Object...args){int col="fr".equals(lang)?1:"de".equals(lang)?3:"es".equals(lang)?4:"pt".equals(lang)?5:"it".equals(lang)?6:2;for(String[]row:COPY)if(row[0].equals(key))return String.format(Locale.ROOT,row[col],args);throw new IllegalArgumentException("Unknown pulse copy: "+key);}
 private Clarity124Strings(){}
}
