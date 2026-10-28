package com.androidhit.lumadrift.data;

import java.util.Locale;

/** Bug-fix feedback and accessible descriptions for the actually equipped look. */
public final class V111Strings {
    private V111Strings() { }
    public static String get(String language, String key, Object... arguments) {
        String lang=Strings.normalize(language);int column=lang.equals("fr")?1:lang.equals("de")?2:lang.equals("es")?3:lang.equals("pt")?4:lang.equals("it")?5:0;
        String[] row;
        switch(key) {
            case "portrait_current": row=new String[]{"Equipped companion portrait: %s. Style: %s. Clothing and equipment: %s.","Portrait du compagnon équipé : %s. Style : %s. Vêtements et équipement : %s.","Porträt des ausgerüsteten Begleiters: %s. Stil: %s. Kleidung und Ausrüstung: %s.","Retrato del compañero equipado: %s. Estilo: %s. Ropa y equipo: %s.","Retrato do companheiro equipado: %s. Estilo: %s. Roupa e equipamento: %s.","Ritratto del compagno equipaggiato: %s. Stile: %s. Abiti ed equipaggiamento: %s."};break;
            case "look_applied": row=new String[]{"Applied: %s","Appliqué : %s","Angewendet: %s","Aplicado: %s","Aplicado: %s","Applicato: %s"};break;
            case "item_unavailable": row=new String[]{"This item is unavailable. Choose an item you own.","Cet objet est indisponible. Choisis un objet que tu possèdes.","Dieser Gegenstand ist nicht verfügbar. Wähle einen Gegenstand in deinem Besitz.","Este objeto no está disponible. Elige uno que poseas.","Este objeto está indisponível. Escolhe um objeto que possuis.","Questo oggetto non è disponibile. Scegli un oggetto che possiedi."};break;
            case "clothes_owned": row=new String[]{"Clothes: %d / %d","Vêtements : %d / %d","Kleidung: %d / %d","Ropa: %d / %d","Roupa: %d / %d","Abiti: %d / %d"};break;
            default: throw new IllegalArgumentException("Unknown1.11 string: "+key);
        }
        return String.format(Locale.forLanguageTag(lang),row[column],arguments);
    }
}
