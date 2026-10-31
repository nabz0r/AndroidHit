package com.androidhit.lumadrift.data;

import java.util.Locale;

public final class SessionEnergyStrings {
    private SessionEnergyStrings(){}
    private static final String[] LANGS={"fr","en","de","es","pt","it","ja"};
    private static final String[][] TEXT={
        {"title", "Énergie", "Energy", "Energie", "Energía", "Energia", "Energia", "エネルギー"},
        {"label", "Énergie %d/10", "Energy %d/10", "Energie %d/10", "Energía %d/10", "Energia %d/10", "Energia %d/10", "エネルギー %d/10"},
        {"rule", "1 énergie par nouvelle tentative dès le niveau 11. Reprendre un plateau ne coûte rien.", "1 energy per new attempt from level 11. Resuming a board is free.", "Ab Level 11 kostet jeder neue Versuch 1 Energie. Fortsetzen ist kostenlos.", "1 energía por intento nuevo desde el nivel 11. Reanudar es gratis.", "1 energia por nova tentativa a partir do nível 11. Retomar é grátis.", "1 energia per ogni nuovo tentativo dal livello 11. Riprendere una tavola è gratis.", "レベル11から新しい挑戦にエネルギー1を使用。中断した盤面の再開は無料。"},
        {"refill", "+1 gratuite toutes les 10 min, jusqu’à 10.", "+1 free every 10 min, up to 10.", "+1 gratis alle 10 Min., maximal 10.", "+1 gratis cada 10 min, hasta 10.", "+1 grátis a cada 10 min, até 10.", "+1 gratis ogni 10 min, fino a 10.", "10分ごとに1回復、最大10。"},
        {"next", "Prochaine recharge : %s", "Next refill: %s", "Nächste Ladung: %s", "Próxima recarga: %s", "Próxima recarga: %s", "Prossima ricarica: %s", "次の回復：%s"},
        {"full", "Énergie pleine !", "Energy full!", "Energie voll!", "¡Energía completa!", "Energia cheia!", "Energia piena!", "エネルギー満タン！"},
        {"free", "Tutoriel et niveaux 1–10 gratuits. Tu peux refaire le tutoriel à tout moment.", "Tutorial and levels 1–10 are free. You can replay the tutorial anytime.", "Tutorial und Level 1–10 sind kostenlos. Das Tutorial ist jederzeit wiederholbar.", "Tutorial y niveles 1–10 gratis. Puedes repetir el tutorial cuando quieras.", "Tutorial e níveis 1–10 grátis. Podes repetir o tutorial quando quiseres.", "Tutorial e livelli 1–10 gratis. Puoi ripetere il tutorial quando vuoi.", "チュートリアルとレベル1〜10は無料。いつでも練習できます。"},
        {"empty", "Pause recharge", "Time to recharge", "Zeit zum Aufladen", "Hora de recargar", "Hora de recarregar", "È ora di ricaricare", "回復を待とう"},
        {"back", "Retour", "Back", "Zurück", "Volver", "Voltar", "Indietro", "戻る"},
        {"buy", "+10 énergie · 50 LumaGold", "+10 energy · 50 LumaGold", "+10 Energie · 50 LumaGold", "+10 energía · 50 LumaGold", "+10 energia · 50 LumaGold", "+10 energia · 50 LumaGold", "エネルギー+10 · 50 LumaGold"},
        {"buy_note", "Recharge disponible à énergie vide. Aucun LumaCoin n’est débité. Achats de gold selon disponibilité du compte.", "Refill available when empty. LumaCoins are never charged. Gold purchases depend on account availability.", "Aufladen bei leerer Energie. Keine LumaCoins werden abgezogen. Goldkäufe je nach Kontoverfügbarkeit.", "Recarga disponible al agotarse. No se cobran LumaCoins. Compras de gold según disponibilidad de la cuenta.", "Recarga disponível quando vazia. Não cobra LumaCoins. Compras de gold conforme disponibilidade da conta.", "Ricarica disponibile a energia vuota. Nessun LumaCoin viene addebitato. Acquisti gold secondo la disponibilità dell’account.", "エネルギー0で補充可能。LumaCoinsは使用しません。Gold購入はアカウントの利用条件によります。"},
        {"use", "Utiliser une recharge · +10", "Use stored refill · +10", "Aufladung nutzen · +10", "Usar recarga guardada · +10", "Usar recarga guardada · +10", "Usa ricarica · +10", "保存した補充を使用 · +10"},
        {"saved", "+10 énergie reçues !", "+10 energy received!", "+10 Energie erhalten!", "¡+10 energía recibida!", "+10 energia recebida!", "+10 energia ricevuta!", "エネルギー+10を獲得！"},
        {"chain", "Série ×%d !", "Streak ×%d!", "Serie ×%d!", "¡Racha ×%d!", "Série ×%d!", "Serie ×%d!", "連続 ×%d！"}
    };
    public static String get(String language,String key,Object...args){int col=2;for(int i=0;i<LANGS.length;i++)if(LANGS[i].equals(language))col=i+1;for(String[] row:TEXT)if(row[0].equals(key))return String.format(Locale.ROOT,row[col],args);return key;}
}
