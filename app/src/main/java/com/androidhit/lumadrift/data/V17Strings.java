package com.androidhit.lumadrift.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Candidate 1.7 text: English, French, German, Spanish, Portuguese and Italian. */
public final class V17Strings {
    private static final Map<String,String[]> TEXT;
    static {
        Map<String,String[]> text=new HashMap<>();
        text.put("lumacoins",new String[]{"LumaCoins","LumaCoins","LumaCoins","LumaCoins","LumaCoins","LumaCoins"});
        text.put("lumagold",new String[]{"LumaGold","LumaGold","LumaGold","LumaGold","LumaGold","LumaGold"});
        text.put("coins_note",new String[]{"Earned by playing · saved on this device","Gagnés en jouant · sauvegardés sur cet appareil","Beim Spielen verdient · auf diesem Gerät gespeichert","Se ganan jugando · guardadas en este dispositivo","Ganhas ao jogar · guardadas neste dispositivo","Guadagnate giocando · salvate su questo dispositivo"});
        text.put("gold_note",new String[]{"Premium account wallet · real purchases unavailable in this candidate","Portefeuille premium du compte · achats réels indisponibles dans cette candidate","Premium-Kontoguthaben · echte Käufe in dieser Version nicht verfügbar","Monedero prémium de la cuenta · compras reales no disponibles en esta versión","Carteira premium da conta · compras reais indisponíveis nesta versão","Portafoglio premium account · acquisti reali non disponibili in questa versione"});
        text.put("daily_login",new String[]{"Daily gift","Cadeau du jour","Tagesgeschenk","Regalo diario","Presente diário","Regalo giornaliero"});
        text.put("login_note",new String[]{"7 claimed visits · no streak to lose","7 visites récompensées · aucune série à perdre","7 belohnte Besuche · keine Serie geht verloren","7 visitas con premio · sin racha que perder","7 visitas premiadas · sem sequência para perder","7 visite premiate · nessuna serie da perdere"});
        text.put("login_day",new String[]{"Gift {0} / 7","Cadeau {0} / 7","Geschenk {0} / 7","Regalo {0} / 7","Presente {0} / 7","Regalo {0} / 7"});
        text.put("claim",new String[]{"Claim","Récupérer","Abholen","Reclamar","Recolher","Riscatta"});
        text.put("claimed",new String[]{"Collected","Récupéré","Abgeholt","Recogido","Recolhido","Riscattato"});
        text.put("daily_quests",new String[]{"Daily quests","Quêtes du jour","Tagesaufgaben","Misiones diarias","Missões diárias","Missioni giornaliere"});
        text.put("weekly_quests",new String[]{"Weekly quests","Quêtes de la semaine","Wochenaufgaben","Misiones semanales","Missões semanais","Missioni settimanali"});
        text.put("quest_boards",new String[]{"Clear {0} different boards","Termine {0} plateaux différents","Schließe {0} verschiedene Felder ab","Completa {0} tableros diferentes","Conclui {0} tabuleiros diferentes","Completa {0} livelli diversi"});
        text.put("quest_stars",new String[]{"Earn {0} stars on new clears","Gagne {0} étoiles sur de nouvelles victoires","Verdiene {0} Sterne bei neuen Abschlüssen","Gana {0} estrellas en nuevas victorias","Ganha {0} estrelas em novas vitórias","Ottieni {0} stelle in nuove vittorie"});
        text.put("quest_events",new String[]{"Clear {0} event boards","Termine {0} plateaux événement","Schließe {0} Eventfelder ab","Completa {0} tableros de evento","Conclui {0} tabuleiros de evento","Completa {0} livelli evento"});
        text.put("quest_note",new String[]{"Only a board’s first clear counts. A missed day never removes earned items.","Seule la première victoire d’un plateau compte. Une absence ne retire aucun objet gagné.","Nur der erste Abschluss eines Feldes zählt. Pausen entfernen keine verdienten Objekte.","Solo cuenta la primera victoria de cada tablero. Faltar un día no elimina objetos.","Só conta a primeira vitória de cada tabuleiro. Uma ausência não remove itens ganhos.","Conta solo la prima vittoria di ogni livello. Le assenze non rimuovono oggetti ottenuti."});
        text.put("wardrobe",new String[]{"Wardrobe","Garde-robe","Garderobe","Vestuario","Guarda-roupa","Guardaroba"});
        text.put("cosmetic_only",new String[]{"Appearance only · no gameplay bonus","Apparence uniquement · aucun bonus de jeu","Nur Aussehen · kein Spielbonus","Solo apariencia · sin ventajas de juego","Só aparência · sem bónus de jogo","Solo aspetto · nessun bonus di gioco"});
        text.put("slot_hat",new String[]{"Hat","Chapeau","Hut","Sombrero","Chapéu","Cappello"});
        text.put("slot_dress",new String[]{"Outfit","Tenue","Kleidung","Vestido","Traje","Abito"});
        text.put("slot_shoes",new String[]{"Shoes","Chaussures","Schuhe","Zapatos","Sapatos","Scarpe"});
        text.put("slot_gloves",new String[]{"Gloves","Gants","Handschuhe","Guantes","Luvas","Guanti"});
        text.put("common",new String[]{"Common","Commun","Gewöhnlich","Común","Comum","Comune"});
        text.put("rare",new String[]{"Rare","Rare","Selten","Raro","Raro","Raro"});
        text.put("epic",new String[]{"Epic","Épique","Episch","Épico","Épico","Epico"});
        text.put("owned",new String[]{"Owned","Possédé","Im Besitz","En propiedad","Obtido","Posseduto"});
        text.put("locked",new String[]{"Not owned","Non possédé","Nicht im Besitz","No obtenido","Não obtido","Non posseduto"});
        text.put("equipped",new String[]{"Equipped","Équipé","Ausgerüstet","Equipado","Equipado","Equipaggiato"});
        text.put("equip",new String[]{"Equip","Équiper","Ausrüsten","Equipar","Equipar","Equipaggia"});
        text.put("capsule",new String[]{"Cosmetic capsule","Capsule cosmétique","Kosmetikkapsel","Cápsula cosmética","Cápsula cosmética","Capsula cosmetica"});
        text.put("capsule_note",new String[]{"150 earned LumaCoins · no LumaGold · appearance only","150 LumaCoins gagnés · aucun LumaGold · apparence uniquement","150 verdiente LumaCoins · kein LumaGold · nur Aussehen","150 LumaCoins ganadas · sin LumaGold · solo apariencia","150 LumaCoins ganhas · sem LumaGold · só aparência","150 LumaCoins guadagnate · nessun LumaGold · solo aspetto"});
        text.put("capsule_odds",new String[]{"Common 60% · Rare 30% · Epic 10%","Commun 60 % · Rare 30 % · Épique 10 %","Gewöhnlich 60% · Selten 30% · Episch 10%","Común 60% · Raro 30% · Épico 10%","Comum 60% · Raro 30% · Épico 10%","Comune 60% · Raro 30% · Epico 10%"});
        text.put("capsule_item_odds",new String[]{"Each item: common 7.5% · rare 3.75% · epic 1.25%","Chaque objet : commun 7,5 % · rare 3,75 % · épique 1,25 %","Je Objekt: gewöhnlich 7,5% · selten 3,75% · episch 1,25%","Cada objeto: común 7,5% · raro 3,75% · épico 1,25%","Cada item: comum 7,5% · raro 3,75% · épico 1,25%","Ogni oggetto: comune 7,5% · raro 3,75% · epico 1,25%"});
        text.put("capsule_duplicates",new String[]{"Duplicate: +25 / +60 / +100 LumaCoins by rarity. No cash value.","Doublon : +25 / +60 / +100 LumaCoins selon la rareté. Aucune valeur en argent.","Doppelt: +25 / +60 / +100 LumaCoins nach Seltenheit. Kein Geldwert.","Duplicado: +25 / +60 / +100 LumaCoins según rareza. Sin valor monetario.","Duplicado: +25 / +60 / +100 LumaCoins por raridade. Sem valor monetário.","Doppione: +25 / +60 / +100 LumaCoins per rarità. Nessun valore monetario."});
        text.put("open_capsule",new String[]{"Open · 150 LumaCoins","Ouvrir · 150 LumaCoins","Öffnen · 150 LumaCoins","Abrir · 150 LumaCoins","Abrir · 150 LumaCoins","Apri · 150 LumaCoins"});
        text.put("not_enough_coins",new String[]{"Not enough LumaCoins","LumaCoins insuffisants","Nicht genug LumaCoins","Faltan LumaCoins","LumaCoins insuficientes","LumaCoins insufficienti"});
        text.put("source_starter",new String[]{"Welcome gift","Cadeau de bienvenue","Willkommensgeschenk","Regalo de bienvenida","Presente de boas-vindas","Regalo di benvenuto"});
        text.put("source_login",new String[]{"{0} daily gifts claimed","{0} cadeaux du jour récupérés","{0} Tagesgeschenke abgeholt","{0} regalos diarios recogidos","{0} presentes diários recolhidos","{0} regali giornalieri riscattati"});
        text.put("source_weekly",new String[]{"{0} weekly event boards cleared","{0} plateaux hebdo terminés","{0} Wochen-Eventfelder abgeschlossen","{0} tableros semanales completados","{0} tabuleiros semanais concluídos","{0} livelli settimanali completati"});
        text.put("source_event",new String[]{"{0} event stars · seasonal collection stays yours","{0} étoiles événement · collection saisonnière conservée","{0} Eventsterne · Saisonobjekte bleiben erhalten","{0} estrellas de evento · conservas la colección estacional","{0} estrelas de evento · a coleção sazonal é tua","{0} stelle evento · la collezione stagionale resta tua"});
        text.put("source_coins",new String[]{"Shop · {0} LumaCoins","Boutique · {0} LumaCoins","Shop · {0} LumaCoins","Tienda · {0} LumaCoins","Loja · {0} LumaCoins","Negozio · {0} LumaCoins"});
        text.put("source_capsule",new String[]{"Earned cosmetic capsule","Capsule cosmétique gagnée","Verdiente Kosmetikkapsel","Cápsula cosmética obtenida","Cápsula cosmética ganha","Capsula cosmetica ottenuta"});
        text.put("new_cosmetic",new String[]{"New wardrobe item","Nouvel objet de garde-robe","Neues Garderobenobjekt","Nuevo objeto de vestuario","Novo item de guarda-roupa","Nuovo oggetto del guardaroba"});
        text.put("duplicate",new String[]{"Duplicate · +{0} LumaCoins","Doublon · +{0} LumaCoins","Doppelt · +{0} LumaCoins","Duplicado · +{0} LumaCoins","Duplicado · +{0} LumaCoins","Doppione · +{0} LumaCoins"});
        text.put("cosmetic_hat_dew",new String[]{"Hat · Dew","Chapeau · Rosée","Hut · Tau","Sombrero · Rocío","Chapéu · Orvalho","Cappello · Rugiada"});
        text.put("cosmetic_hat_moon",new String[]{"Hat · Moon","Chapeau · Lune","Hut · Mond","Sombrero · Luna","Chapéu · Lua","Cappello · Luna"});
        text.put("cosmetic_hat_star",new String[]{"Hat · Starlight","Chapeau · Étoile","Hut · Stern","Sombrero · Estrella","Chapéu · Estrela","Cappello · Stella"});
        text.put("cosmetic_hat_pumpkin",new String[]{"Hat · Pumpkin","Chapeau · Citrouille","Hut · Kürbis","Sombrero · Calabaza","Chapéu · Abóbora","Cappello · Zucca"});
        text.put("cosmetic_hat_cupid",new String[]{"Hat · Cupid","Chapeau · Cupidon","Hut · Amor","Sombrero · Cupido","Chapéu · Cupido","Cappello · Cupido"});
        text.put("cosmetic_hat_aurora",new String[]{"Hat · Aurora","Chapeau · Aurore","Hut · Aurora","Sombrero · Aurora","Chapéu · Aurora","Cappello · Aurora"});
        text.put("cosmetic_dress_dew",new String[]{"Outfit · Dew","Tenue · Rosée","Kleidung · Tau","Vestido · Rocío","Traje · Orvalho","Abito · Rugiada"});
        text.put("cosmetic_dress_moon",new String[]{"Outfit · Moon","Tenue · Lune","Kleidung · Mond","Vestido · Luna","Traje · Lua","Abito · Luna"});
        text.put("cosmetic_dress_star",new String[]{"Outfit · Starlight","Tenue · Étoile","Kleidung · Stern","Vestido · Estrella","Traje · Estrela","Abito · Stella"});
        text.put("cosmetic_dress_valentine",new String[]{"Outfit · Sweetheart","Tenue · Cœur tendre","Kleidung · Herzchen","Vestido · Corazón","Traje · Coração","Abito · Cuore"});
        text.put("cosmetic_dress_witch",new String[]{"Outfit · Little witch","Tenue · Petite sorcière","Kleidung · Kleine Hexe","Vestido · Brujita","Traje · Bruxinha","Abito · Streghetta"});
        text.put("cosmetic_dress_aurora",new String[]{"Outfit · Aurora","Tenue · Aurore","Kleidung · Aurora","Vestido · Aurora","Traje · Aurora","Abito · Aurora"});
        text.put("cosmetic_shoes_dew",new String[]{"Shoes · Dew","Chaussures · Rosée","Schuhe · Tau","Zapatos · Rocío","Sapatos · Orvalho","Scarpe · Rugiada"});
        text.put("cosmetic_shoes_moon",new String[]{"Shoes · Moon","Chaussures · Lune","Schuhe · Mond","Zapatos · Luna","Sapatos · Lua","Scarpe · Luna"});
        text.put("cosmetic_shoes_star",new String[]{"Shoes · Starlight","Chaussures · Étoile","Schuhe · Stern","Zapatos · Estrella","Sapatos · Estrela","Scarpe · Stella"});
        text.put("cosmetic_shoes_pumpkin",new String[]{"Shoes · Pumpkin","Chaussures · Citrouille","Schuhe · Kürbis","Zapatos · Calabaza","Sapatos · Abóbora","Scarpe · Zucca"});
        text.put("cosmetic_shoes_cupid",new String[]{"Shoes · Cupid","Chaussures · Cupidon","Schuhe · Amor","Zapatos · Cupido","Sapatos · Cupido","Scarpe · Cupido"});
        text.put("cosmetic_shoes_aurora",new String[]{"Shoes · Aurora","Chaussures · Aurore","Schuhe · Aurora","Zapatos · Aurora","Sapatos · Aurora","Scarpe · Aurora"});
        text.put("cosmetic_gloves_dew",new String[]{"Gloves · Dew","Gants · Rosée","Handschuhe · Tau","Guantes · Rocío","Luvas · Orvalho","Guanti · Rugiada"});
        text.put("cosmetic_gloves_moon",new String[]{"Gloves · Moon","Gants · Lune","Handschuhe · Mond","Guantes · Luna","Luvas · Lua","Guanti · Luna"});
        text.put("cosmetic_gloves_star",new String[]{"Gloves · Starlight","Gants · Étoile","Handschuhe · Stern","Guantes · Estrella","Luvas · Estrela","Guanti · Stella"});
        text.put("cosmetic_gloves_valentine",new String[]{"Gloves · Sweetheart","Gants · Cœur tendre","Handschuhe · Herzchen","Guantes · Corazón","Luvas · Coração","Guanti · Cuore"});
        text.put("cosmetic_gloves_witch",new String[]{"Gloves · Little witch","Gants · Petite sorcière","Handschuhe · Kleine Hexe","Guantes · Brujita","Luvas · Bruxinha","Guanti · Streghetta"});
        text.put("cosmetic_gloves_aurora",new String[]{"Gloves · Aurora","Gants · Aurore","Handschuhe · Aurora","Guantes · Aurora","Luvas · Aurora","Guanti · Aurora"});
        TEXT=Collections.unmodifiableMap(text);
    }
    private V17Strings() { }
    public static boolean has(String key) { return TEXT.containsKey(key); }
    public static String get(String language,String key) {
        String[] text=TEXT.get(key);if(text==null)return key;
        int index="fr".equals(language)?1:"de".equals(language)?2:"es".equals(language)?3:"pt".equals(language)?4:"it".equals(language)?5:0;
        return text[index];
    }
    public static String format(String language,String key,int value) { return get(language,key).replace("{0}",Integer.toString(value)); }
}
