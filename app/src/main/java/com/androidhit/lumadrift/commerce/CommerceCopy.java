package com.androidhit.lumadrift.commerce;

import com.androidhit.lumadrift.data.Strings;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

/** Short, six-language labels for the shop hierarchy; prices never come from this copy. */
final class CommerceCopy {
    private static final String[] LANG={"en","fr","de","es","pt","it"};
    private static final Map<String,String[]> COPY=new HashMap<>();
    static {
        add("shop","Little wonders","Petites merveilles","Kleine Wunder","Pequeñas maravillas","Pequenas maravilhas","Piccole meraviglie");
        add("intro","Your next favourite companion, a new world of colour.","Ton prochain compagnon préféré, un nouvel univers de couleurs.","Dein nächster Lieblingsbegleiter, eine neue Welt voller Farben.","Tu próximo compañero favorito, un nuevo mundo de color.","O teu próximo companheiro favorito, um novo mundo de cor.","Il tuo prossimo compagno preferito, un nuovo mondo di colori.");
        add("premium","Purchased · Google account","Achetés · compte Google","Gekauft · Google-Konto","Comprados · cuenta Google","Comprados · conta Google","Acquistati · account Google");
        add("earned","Earned by playing · this device","Gagnés en jouant · cet appareil","Im Spiel verdient · dieses Gerät","Ganados jugando · este dispositivo","Ganhos a jogar · este dispositivo","Guadagnati giocando · questo dispositivo");
        add("separate","Two currencies, two balances. No automatic conversion.","Deux monnaies, deux soldes. Aucune conversion automatique.","Zwei Währungen, zwei Guthaben. Keine automatische Umwandlung.","Dos monedas, dos saldos. Sin conversión automática.","Duas moedas, dois saldos. Sem conversão automática.","Due valute, due saldi. Nessuna conversione automatica.");
        add("lab","Lab · simulated coins only","Lab · pièces simulées uniquement","Lab · nur simulierte Münzen","Lab · solo monedas simuladas","Lab · apenas moedas simuladas","Lab · solo monete simulate");
        add("lab_note","This Lab does not show or spend your purchased LumaGold.","Ce Lab n’affiche ni ne dépense tes LumaGold achetés.","Dieses Lab zeigt und verwendet dein gekauftes LumaGold nicht.","Este Lab no muestra ni gasta tus LumaGold comprados.","Este Lab não mostra nem gasta os teus LumaGold comprados.","Questo Lab non mostra né spende i tuoi LumaGold acquistati.");
        add("packs","A little sparkle","Une touche d’éclat","Ein wenig Glanz","Un toque de brillo","Um toque de brilho","Un tocco di luce");
        add("pack_note","Each pack shows its Google Play price before payment.","Chaque pack affiche son prix Google Play avant paiement.","Jedes Paket zeigt vor der Zahlung seinen Google-Play-Preis.","Cada pack muestra su precio de Google Play antes de pagar.","Cada pacote mostra o preço Google Play antes do pagamento.","Ogni pacchetto mostra il prezzo Google Play prima del pagamento.");
        add("collection_note","Keep them forever. Equip your favourites in the collection.","Garde-les pour toujours. Équipe tes favoris dans la collection.","Sie bleiben dir erhalten. Rüste deine Favoriten in der Sammlung aus.","Son tuyos para siempre. Equipa tus favoritos en la colección.","Ficam contigo para sempre. Equipa os favoritos na coleção.","Restano tuoi per sempre. Equipaggia i preferiti nella collezione.");
        add("optional","A reward, if you want one","Une récompense, si tu en as envie","Eine Belohnung, wenn du möchtest","Una recompensa, si te apetece","Uma recompensa, se quiseres","Una ricompensa, se ti va");
        add("account","Account & purchases","Compte et achats","Konto & Käufe","Cuenta y compras","Conta e compras","Account e acquisti");
        add("owned","In your collection","Dans ta collection","In deiner Sammlung","En tu colección","Na tua coleção","Nella tua collezione");
        add("buy","Unlock","Débloquer","Freischalten","Desbloquear","Desbloquear","Sblocca");
        add("google","Google Play price","Prix Google Play","Google-Play-Preis","Precio Google Play","Preço Google Play","Prezzo Google Play");
        add("ads_watch","Watch an ad · +%d LumaCoins","Voir une publicité · +%d LumaCoins","Anzeige ansehen · +%d LumaCoins","Ver anuncio · +%d LumaCoins","Ver anúncio · +%d LumaCoins","Guarda annuncio · +%d LumaCoins");
        add("ads_reward_note","Optional. +%d LumaCoins after the reward is confirmed. Closing early gives no reward.","Facultatif. +%d LumaCoins après confirmation de la récompense. Fermer trop tôt ne donne pas de récompense.","Freiwillig. +%d LumaCoins nach bestätigter Belohnung. Vorzeitiges Schließen gibt keine Belohnung.","Opcional. +%d LumaCoins tras confirmar la recompensa. Cerrar antes no da recompensa.","Opcional. +%d LumaCoins após confirmação da recompensa. Fechar antes não dá recompensa.","Facoltativo. +%d LumaCoins dopo la conferma della ricompensa. Chiudendo prima non si riceve il premio.");
    }
    private static void add(String key,String...values){COPY.put(key,values);}
    static String get(String language,String key,Object...args){
        String[] values=COPY.get(key);if(values==null)throw new IllegalArgumentException("Unknown commerce copy: "+key);
        String normalized=Strings.normalize(language);int index=0;
        for(int i=0;i<LANG.length;i++)if(LANG[i].equals(normalized))index=i;
        return args.length==0?values[index]:String.format(Locale.forLanguageTag(normalized),values[index],args);
    }
    private CommerceCopy(){}
}
