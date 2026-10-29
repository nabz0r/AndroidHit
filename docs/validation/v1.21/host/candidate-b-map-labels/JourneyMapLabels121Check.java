import com.androidhit.lumadrift.data.JourneyMapStrings;
public final class JourneyMapLabels121Check {
 static int checks; static void check(boolean yes,String why){checks++;if(!yes)throw new AssertionError(why);}
 public static void main(String[] args){
  String[] keys={"current","jump","journal","squad","world","progress","next","stars"};
  for(String language:new String[]{"en","fr","de","es","pt","it","en-GB","fr-LU","de-DE","es-ES","pt-PT","it-IT"})for(String key:keys){
   String value=key.equals("world")?JourneyMapStrings.get(language,key,3,61,90):key.equals("progress")?JourneyMapStrings.get(language,key,8,30,24):key.equals("stars")?JourneyMapStrings.get(language,key,3):JourneyMapStrings.get(language,key);
   check(!value.isBlank()&&!value.equals(key),"known map labels never raw/fallback keys");
   check(!value.matches(".*%[0-9$]*[ds].*"),"all positional formatting resolved");
   if(key.equals("current"))check(value.codePointCount(0,value.length())<=14,"current short caption textual bound");
   if(key.equals("jump"))check(value.codePointCount(0,value.length())<=9,"jump short caption textual bound");
   if(key.equals("world"))check(value.contains("3")&&value.contains("61")&&value.contains("90"),"actual world identity/range preserved");
   if(key.equals("progress"))check(value.contains("8")&&value.contains("30")&&value.contains("24"),"actual completed/total/stars preserved");
  }
  System.out.println("PASS "+checks+" actual JourneyMapStrings checks:8keys/6languages+regionaltags, numeric formatting and short-caption text bounds; no native font/render acceptance");
 }
}
