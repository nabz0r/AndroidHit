import com.androidhit.lumadrift.data.Catalog;
import com.androidhit.lumadrift.data.EventSchedule;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

/** Independent java.time oracle for the Android Calendar event countdown and catalog invariants. */
public final class CollectionEventsTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static long oracle(long now, boolean weekly, String zoneName) {
        ZoneId zone = ZoneId.of(zoneName);
        LocalDate day = Instant.ofEpochMilli(now).atZone(zone).toLocalDate();
        int days = weekly ? 8 - day.getDayOfWeek().getValue() : 1;
        return day.plusDays(days).atStartOfDay(zone).toInstant().toEpochMilli();
    }
    private static void testClock() {
        int zones=0, points=0;
        // Every IANA zone known to this JDK; before and after US/EU DST, year and Monday rollovers.
        String[] anchors = {
            "2026-01-01T00:00:00Z", "2026-03-08T06:59:59Z", "2026-03-08T07:00:01Z",
            "2026-03-29T00:59:59Z", "2026-03-29T01:00:01Z", "2026-10-25T00:59:59Z",
            "2026-10-25T01:00:01Z", "2026-11-01T05:59:59Z", "2026-11-01T06:00:01Z",
            "2026-12-31T23:59:59Z", "2027-01-04T00:00:00Z", "2028-02-29T23:59:59Z"
        };
        for (String name : ZoneId.getAvailableZoneIds()) {
            zones++;
            TimeZone zone=TimeZone.getTimeZone(name);
            for (String anchor : anchors) {
                long now=Instant.parse(anchor).toEpochMilli();
                for (boolean weekly : new boolean[]{false,true}) {
                    long reset=EventSchedule.nextResetMillis(now,weekly,zone), expected=oracle(now,weekly,name);
                    check(reset==expected, "Reset differs from local-date oracle: "+name+" "+anchor+" weekly="+weekly);
                    check(reset>now, "Next reset is in the future");
                    check(EventSchedule.remainingMinutes(now,weekly,zone)>0, "Nonzero countdown before reset");
                    check(EventSchedule.nextResetMillis(reset-1,weekly,zone)==reset, "Last millisecond belongs to old period");
                    check(EventSchedule.nextResetMillis(reset,weekly,zone)>reset, "At boundary a new period starts");
                    check(EventSchedule.remainingMinutes(reset-1,weekly,zone)==1, "Round up the last minute");
                    points++;
                }
            }
        }
        TimeZone lux=TimeZone.getTimeZone("Europe/Luxembourg");
        long spring=LocalDate.of(2026,3,29).atStartOfDay(ZoneId.of("Europe/Luxembourg")).toInstant().toEpochMilli();
        long autumn=LocalDate.of(2026,10,25).atStartOfDay(ZoneId.of("Europe/Luxembourg")).toInstant().toEpochMilli();
        check(EventSchedule.nextResetMillis(spring,false,lux)-spring==23*3600000L,"Spring daily period has 23 hours");
        check(EventSchedule.nextResetMillis(autumn,false,lux)-autumn==25*3600000L,"Autumn daily period has 25 hours");
        boolean rejected=false;try{EventSchedule.nextResetMillis(0,false,null);}catch(IllegalArgumentException expected){rejected=true;}
        check(rejected,"Missing time zone rejected");
        System.out.println("Event clocks: "+zones+" time zones, "+points+" boundary scenarios; DST 23/25-hour days verified.");
    }
    private static void testCatalog() {
        String[] legacy={"nova","miko","orbit","pip","astra","zig","lune","fenn","hoot","buzz","tula","jelli","vela","nimbi","soli","mochi","nixi","prickly","quill","waddles","halo","emberling","bambu","inky"};
        int[] prices={0,160,220,300,420,600,180,240,280,320,360,400,440,480,520,560,620,680,740,800,860,920,1000,1100};
        String[] guests={"lol_ashe","lol_ezreal","lol_soraka","lol_ahri","lol_jinx","lol_lux","lol_teemo","lol_yasuo","lol_annie","lol_lulu","lol_sona","lol_miss_fortune","lol_vi"};
        check(Catalog.CHARACTERS.length==37,"24 preserved companions and 13 local guests");Set<String> ids=new HashSet<>();Set<Integer> shapes=new HashSet<>();int total=0,legacyTotal=0;
        for(int i=0;i<Catalog.CHARACTERS.length;i++){
            Catalog.CharacterItem item=Catalog.CHARACTERS[i];
            check(ids.add(item.id),"Unique companion ID");check(shapes.add(item.shape),"Unique silhouette index");
            check(Catalog.character(item.id)==item,"Catalog lookup round trip");check(item.price>=0&&item.price<=1100,"Earnable bounded cosmetic price");
            check(item.nameKey.equals("character_"+item.id),"Stable localized name key");
            if(i<legacy.length){check(item.id.equals(legacy[i]),"Preserve original companion ID and order");check(item.price==prices[i],"Preserve original companion price");check(item.shape==i,"Preserve original shape");legacyTotal+=item.price;}
            else{check(item.id.equals(guests[i-legacy.length]),"Stable local guest ID and order");check(item.price==520&&item.shape==i,"Earned guest price and distinct appended shape");}
            total+=item.price;
        }
        check(shapes.size()==37,"Every companion has a distinct shape index");
        check(legacyTotal==12200&&legacyTotal<=240*55+180,"Original 24 companion prices retain their historical first-240-clear income bound");
        check(total==18960&&total<=Catalog.LEVEL_COUNT*55+180,"Expanded direct-purchase collection fits the current campaign income bound");
        check(Catalog.SKINS.length==6,"Existing six themes preserved");
        System.out.println("Companions: 37 unique IDs and silhouettes; original 24 cost "+legacyTotal+" coins unchanged; 13 local guests cost "+(total-legacyTotal)+" coins.");
    }
    public static void main(String[] args){testCatalog();testClock();System.out.println("Collection/event checks: "+checks+" passed.");}
}
