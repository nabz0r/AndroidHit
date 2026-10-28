import com.androidhit.lumadrift.data.V111Strings;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IllegalFormatException;
import java.util.Locale;

/** Host formatting/selection contract; not a native screen-reader or translator review. */
public final class V111StringsTest {
    private static int assertions;
    private static final String[] LANGUAGES={"en","fr","de","es","pt","it"};
    private static final String[] KEYS={"portrait_current","look_applied","item_unavailable","clothes_owned"};
    private static void check(boolean value,String note){assertions++;if(!value)throw new AssertionError(note);}
    private static String formatted(String language,String key){
        if(key.equals("portrait_current"))return V111Strings.get(language,key,"Pip 50%","Océan $1","Étoile + Ruban");
        if(key.equals("look_applied"))return V111Strings.get(language,key,"Chapeau 50% $1");
        if(key.equals("clothes_owned"))return V111Strings.get(language,key,7,24);
        return V111Strings.get(language,key);
    }
    public static void main(String[] args)throws Exception{
        String[] portraitPrefixes={"Equipped companion portrait:","Portrait du compagnon équipé :","Porträt des ausgerüsteten Begleiters:","Retrato del compañero equipado:","Retrato do companheiro equipado:","Ritratto del compagno equipaggiato:"};
        String[] appliedPrefixes={"Applied:","Appliqué :","Angewendet:","Aplicado:","Aplicado:","Applicato:"};
        String[] ownedPrefixes={"Clothes:","Vêtements :","Kleidung:","Ropa:","Roupa:","Abiti:"};
        for(int i=0;i<LANGUAGES.length;i++){
            String lang=LANGUAGES[i];
            for(String key:KEYS){
                String text=formatted(lang,key);
                check(!text.isBlank(),"nonblank "+lang+":"+key);
                check(!text.contains("%s")&&!text.contains("%d"),"all placeholders substituted "+lang+":"+key);
                for(String region:new String[]{lang+"-XX",lang.toUpperCase(Locale.ROOT)+"_xx",lang+"-XX_variant"})
                    check(text.equals(formatted(region,key)),"region normalizes consistently "+region+":"+key);
            }
            String portrait=formatted(lang,"portrait_current");
            check(portrait.startsWith(portraitPrefixes[i]),"portrait locale "+lang);
            check(portrait.contains("Pip 50%")&&portrait.contains("Océan $1")&&portrait.contains("Étoile + Ruban"),"literal input preserved "+lang);
            check(formatted(lang,"look_applied").equals(appliedPrefixes[i]+" Chapeau 50% $1"),"applied locale/format "+lang);
            check(formatted(lang,"clothes_owned").equals(ownedPrefixes[i]+" 7 / 24"),"owned locale/format "+lang);
            check(formatted(lang,"clothes_owned").contains("7 / 24"),"owned argument order "+lang);
            if(!lang.equals("en"))check(!formatted(lang,"item_unavailable").equals(formatted("en","item_unavailable")),"unavailable is localized "+lang);
            boolean unknown=false;try{V111Strings.get(lang,"nonexistent_v111");}catch(IllegalArgumentException expected){unknown=true;}
            check(unknown,"unknown key rejected "+lang);
            boolean missing=false;try{V111Strings.get(lang,"portrait_current","Only one");}catch(IllegalFormatException expected){missing=true;}
            check(missing,"missing format arguments rejected "+lang);
            boolean wrong=false;try{V111Strings.get(lang,"clothes_owned","not a number",24);}catch(IllegalFormatException expected){wrong=true;}
            check(wrong,"wrong integer argument type rejected "+lang);
        }
        for(String fallback:new String[]{"","ja","zz-ZZ","unknown","tr"})for(String key:KEYS)
            check(formatted(fallback,key).equals(formatted("en",key)),"unsupported language falls back to English "+fallback+":"+key);
        Locale original=Locale.getDefault();
        try{
            for(String lang:LANGUAGES){Locale.setDefault(Locale.forLanguageTag(lang+"-XX"));for(String key:KEYS){
                check(formatted(null,key).equals(formatted(lang,key)),"null uses supported device locale "+lang+":"+key);
                check(formatted("auto",key).equals(formatted(lang,key)),"auto uses supported device locale "+lang+":"+key);
            }}
            Locale.setDefault(Locale.JAPANESE);for(String key:KEYS){
                check(formatted(null,key).equals(formatted("en",key)),"unsupported device locale falls back null "+key);
                check(formatted("auto",key).equals(formatted("en",key)),"unsupported device locale falls back auto "+key);
            }
        }finally{Locale.setDefault(original);}
        String report="{\n  \"passed\": true,\n  \"assertions\": "+assertions+",\n  \"keys\": 4,\n  \"languages\": 6,\n  \"scope\": \"Host locale selection and String.format contract: all keys, literal substitutions, regional/default/unsupported languages, unknown keys, missing and wrongly typed arguments. Not a translator review, Android layout, TalkBack or runtime accessibility test.\"\n}\n";
        Files.writeString(Path.of(args[0]).resolve("v111-strings.json"),report);System.out.print(report);
    }
}
