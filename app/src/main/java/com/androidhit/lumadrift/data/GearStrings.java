package com.androidhit.lumadrift.data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Localized equipment names, acquisition conditions and exact bounded benefits. */
public final class GearStrings {
    private static final String[] LANG={"en","fr","de","es","pt","it"};
    private static final Map<String,String[]> COPY=new HashMap<>();
    static {
        add("gear_moonbud", "Moonbud", "Bourgeon lunaire", "Mondknospe", "Brote lunar", "Broto lunar", "Germoglio lunare");
        add("gear_lantern_leaf", "Lantern Leaf", "Feuille lanterne", "Laternenblatt", "Hoja farol", "Folha lanterna", "Foglia lanterna");
        add("gear_crescent_knot", "Crescent Knot", "Nœud croissant", "Mondsichelknoten", "Nudo creciente", "Nó crescente", "Nodo crescente");
        add("gear_tide_crown", "Tide Crown", "Couronne des marées", "Gezeitenkrone", "Corona de mareas", "Coroa das marés", "Corona delle maree");
        add("gear_starfold_visor", "Starfold Visor", "Visière étoilée", "Sternfaltvisier", "Visera estelar", "Viseira estelar", "Visiera stellare");
        add("gear_prism_antlers", "Prism Antlers", "Bois prismatiques", "Prismengeweih", "Astas prisma", "Hastes prisma", "Corna prisma");
        add("gear_firefly_ring", "Firefly Ring", "Ronde de lucioles", "Glühwürmchenring", "Anillo de luciérnagas", "Anel de pirilampos", "Anello di lucciole");
        add("gear_mint_orbit", "Mint Orbit", "Orbite menthe", "Minzumlauf", "Órbita menta", "Órbita menta", "Orbita menta");
        add("gear_violet_ripple", "Violet Ripple", "Onde violette", "Violettwelle", "Onda violeta", "Onda violeta", "Onda viola");
        add("gear_sunset_ribbon", "Sunset Ribbon", "Ruban du crépuscule", "Abendband", "Cinta del ocaso", "Fita do crepúsculo", "Nastro del tramonto");
        add("gear_comet_halo", "Comet Halo", "Halo de comète", "Kometenhalo", "Halo de cometa", "Halo de cometa", "Alone di cometa");
        add("gear_aurora_bloom", "Aurora Bloom", "Floraison d’aurore", "Polarlichtblüte", "Flor de aurora", "Flor da aurora", "Fiore d’aurora");
        add("gear_pebble_heart", "Pebble Heart", "Cœur de galet", "Kieselherz", "Corazón de guijarro", "Coração de seixo", "Cuore di ciottolo");
        add("gear_dew_compass", "Dew Compass", "Boussole de rosée", "Taukompass", "Brújula de rocío", "Bússola de orvalho", "Bussola di rugiada");
        add("gear_copper_wish", "Copper Wish", "Vœu de cuivre", "Kupferwunsch", "Deseo de cobre", "Desejo de cobre", "Desiderio di rame");
        add("gear_tide_anchor", "Tide Anchor", "Ancre des marées", "Gezeitenanker", "Ancla de mareas", "Âncora das marés", "Ancora delle maree");
        add("gear_star_bell", "Star Bell", "Cloche étoilée", "Sternenglocke", "Campana estelar", "Sino estelar", "Campana stellare");
        add("gear_moon_seed", "Moon Seed", "Graine lunaire", "Mondsaat", "Semilla lunar", "Semente lunar", "Seme lunare");
        add("gear_slot_head", "Headwear", "Coiffe", "Kopfschmuck", "Tocado", "Adorno", "Copricapo");
        add("gear_slot_aura", "Aura", "Aura", "Aura", "Aura", "Aura", "Aura");
        add("gear_slot_charm", "Charm", "Charme", "Talisman", "Amuleto", "Amuleto", "Amuleto");
        add("gear_rarity_common", "Common", "Commun", "Gewöhnlich", "Común", "Comum", "Comune");
        add("gear_rarity_rare", "Rare", "Rare", "Selten", "Raro", "Raro", "Raro");
        add("gear_rarity_epic", "Epic", "Épique", "Episch", "Épico", "Épico", "Epico");
        add("gear_source_starter", "Free from the start", "Offert dès le départ", "Von Anfang an gratis", "Gratis desde el inicio", "Grátis desde o início", "Gratis dall’inizio");
        add("gear_source_journey_clears", "Clear %d Journey boards · %d/%d", "Termine %d plateaux du parcours · %d/%d", "Löse %d Reise-Rätsel · %d/%d", "Supera %d puzles del recorrido · %d/%d", "Conclui %d puzzles do percurso · %d/%d", "Completa %d puzzle del percorso · %d/%d");
        add("gear_source_weekly_wins", "Win %d weekly boards · %d/%d lifetime", "Gagne %d plateaux hebdo · %d/%d au total", "Gewinne %d Wochenrätsel · %d/%d insgesamt", "Gana %d puzles semanales · %d/%d acumulados", "Ganha %d puzzles semanais · %d/%d acumulados", "Vinci %d puzzle settimanali · %d/%d totali");
        add("gear_source_event_stars", "Earn %d event stars · %d/%d lifetime", "Gagne %d étoiles en événements · %d/%d au total", "Verdiene %d Eventsterne · %d/%d insgesamt", "Consigue %d estrellas de eventos · %d/%d acumuladas", "Ganha %d estrelas de eventos · %d/%d acumuladas", "Ottieni %d stelle negli eventi · %d/%d totali");
        add("gear_source_earned_coins", "Shop · %d earned coins", "Boutique · %d pièces gagnées", "Shop · %d erspielte Münzen", "Tienda · %d monedas ganadas", "Loja · %d moedas ganhas", "Negozio · %d monete guadagnate");
        add("gear_effect_none", "Appearance only", "Apparence uniquement", "Nur Aussehen", "Solo apariencia", "Apenas aparência", "Solo aspetto");
        add("gear_effect_win_bonus", "+%d earned coins on a Journey, Remix or event board’s first victory. No replay or friend-challenge bonus.", "+%d pièces à la première victoire d’un plateau du parcours, Remix ou événement. Aucun bonus en rejouant ni en défi ami.", "+%d Münzen beim ersten Sieg eines Reise-, Remix- oder Eventrätsels. Kein Bonus bei Wiederholungen oder Freundesduellen.", "+%d monedas en la primera victoria de un puzle del recorrido, Remix o evento. Sin bonus al repetir ni en retos de amigos.", "+%d moedas na primeira vitória de um puzzle do percurso, Remix ou evento. Sem bónus ao repetir ou em desafios de amigos.", "+%d monete alla prima vittoria di un puzzle percorso, Remix o evento. Nessun bonus per ripetizioni o sfide amico.");
        add("gear_effect_win_bonus_one", "+1 earned coin on a Journey, Remix or event board’s first victory. No replay or friend-challenge bonus.", "+1 pièce à la première victoire d’un plateau du parcours, Remix ou événement. Aucun bonus en rejouant ni en défi ami.", "+1 Münze beim ersten Sieg eines Reise-, Remix- oder Eventrätsels. Kein Bonus bei Wiederholungen oder Freundesduellen.", "+1 moneda en la primera victoria de un puzle del recorrido, Remix o evento. Sin bonus al repetir ni en retos de amigos.", "+1 moeda na primeira vitória de um puzzle do percurso, Remix ou evento. Sem bónus ao repetir ou em desafios de amigos.", "+1 moneta alla prima vittoria di un puzzle percorso, Remix o evento. Nessun bonus per ripetizioni o sfide amico.");
        add("gear_effect_pack_discount", "−%d earned coins per power pack. Packs always cost at least 1 coin.", "−%d pièces gagnées par pack de pouvoirs. Le pack coûte toujours au moins 1 pièce.", "−%d erspielte Münzen je Kraftpaket. Pakete kosten immer mindestens 1 Münze.", "−%d monedas ganadas por pack de poderes. Siempre cuesta al menos 1 moneda.", "−%d moedas ganhas por pacote de poderes. Custa sempre pelo menos 1 moeda.", "−%d monete guadagnate per pacchetto poteri. Costa sempre almeno 1 moneta.");
        add("gear_effect_pack_discount_one", "−1 earned coin per power pack. Packs always cost at least 1 coin.", "−1 pièce gagnée par pack de pouvoirs. Le pack coûte toujours au moins 1 pièce.", "−1 erspielte Münze je Kraftpaket. Pakete kosten immer mindestens 1 Münze.", "−1 moneda ganada por pack de poderes. Siempre cuesta al menos 1 moneda.", "−1 moeda ganha por pacote de poderes. Custa sempre pelo menos 1 moeda.", "−1 moneta guadagnata per pacchetto poteri. Costa sempre almeno 1 moneta.");
        add("gear_effect_blocked_grace", "Protect %d blocked attempt per board run. One equipped charm; no extra moves.", "Protège %d essai bloqué par tentative de plateau. Un seul charme équipé ; aucun coup ajouté.", "Schützt %d blockierten Versuch pro Rätselrunde. Ein angelegter Talisman, keine Extrazüge.", "Protege %d intento bloqueado por partida. Un amuleto equipado; sin movimientos extra.", "Protege %d tentativa bloqueada por partida. Um amuleto equipado; sem jogadas extra.", "Protegge %d tentativo bloccato per partita. Un amuleto equipaggiato; nessuna mossa extra.");
    }
    private GearStrings() { }
    private static void add(String key,String... values) { COPY.put(key,values); }
    public static String get(String language,String key,Object... args) {
        String[] values=COPY.get(key);if(values==null)return Strings.get(language,key,args);
        String lang=Strings.normalize(language);int index=0;
        for(int i=0;i<LANG.length;i++)if(LANG[i].equals(lang))index=i;
        return args.length==0?values[index]:String.format(Locale.forLanguageTag(lang),values[index],args);
    }
    public static String sourceCaption(String language,AvatarGearCatalog.Item item,AvatarGearCatalog.Progress progress) {
        if(item==null)return "";
        if(item.source==AvatarGearCatalog.Source.STARTER)return get(language,item.source.key());
        if(item.source==AvatarGearCatalog.Source.EARNED_COINS)return get(language,item.source.key(),item.priceCoins);
        int value=progress==null?0:Math.min(item.requirement,progress.value(item.source));
        return get(language,item.source.key(),item.requirement,value,item.requirement);
    }
    public static String effectCaption(String language,AvatarGearCatalog.Item item) {
        if(item==null)return "";
        if(item.winBonusCoins==1)return get(language,"gear_effect_win_bonus_one");
        if(item.winBonusCoins>0)return get(language,"gear_effect_win_bonus",item.winBonusCoins);
        if(item.packDiscountCoins==1)return get(language,"gear_effect_pack_discount_one");
        if(item.packDiscountCoins>0)return get(language,"gear_effect_pack_discount",item.packDiscountCoins);
        if(item.blockedGrace>0)return get(language,"gear_effect_blocked_grace",item.blockedGrace);
        return get(language,"gear_effect_none");
    }
}
