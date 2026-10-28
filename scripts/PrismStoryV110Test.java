import com.androidhit.lumadrift.data.PrismStoryStrings;
import com.androidhit.lumadrift.engine.GuardianCatalog;
import com.androidhit.lumadrift.engine.GuardianEncounter;
import com.androidhit.lumadrift.engine.Direction;
import com.androidhit.lumadrift.engine.PrismStory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Real read-only story API. No profile, receipt, reward, game board or preference is mutated. */
public final class PrismStoryV110Test {
    static long checks;
    static void check(boolean ok, String reason) { checks++; if (!ok) throw new AssertionError(reason); }
    static void invalid(Runnable action) { boolean rejected=false;try { action.run(); } catch (IllegalArgumentException expected) { rejected=true; }check(rejected,"invalid chapter rejected"); }
    static void immutable(Runnable action) { boolean rejected=false;try { action.run(); } catch (UnsupportedOperationException expected) { rejected=true; }check(rejected,"collection mutation rejected"); }
    static Set<Integer> prefix(int count) { Set<Integer> s=new HashSet<>();for(int n=1;n<=count;n++)s.add(n);return s; }
    static String csv(String value) { return "\""+value.replace("\"","\"\"")+"\""; }
    public static void main(String[] args) throws Exception {
        Path out=Path.of(args[0]);Files.createDirectories(out);
        check(PrismStory.chapters().size()==10,"ten original chapter definitions");
        immutable(()->PrismStory.chapters().clear());
        StringBuilder boundaries=new StringBuilder("journey_clears,current_chapter,chapter_progress,chapter_fragments,global_next_guardian,unlocked_guardians\n");
        int snapshots=0;
        for(int clears=0;clears<=1000;clears++) {
            Set<Integer> owned=prefix(clears/10);
            PrismStory.Snapshot normal=PrismStory.snapshot(clears,owned);snapshots++;
            check(normal.journeyClears==clears,"valid clear count kept");
            check(normal.fragmentsClaimed==owned.size(),"supplied distinct receipts counted");
            check(normal.nextGuardian()==(owned.size()==100?0:owned.size()+1),"global next missing receipt");
            check(normal.currentChapter().chapter.number==Math.min(10,clears/100+1),"current chapter at exact boundary");
            int unlocked=0,progress=0,fragments=0;
            for(int n=1;n<=100;n++) {
                if(normal.guardianUnlocked(n))unlocked++;
                check(normal.guardianUnlocked(n)==(clears>=n*10),"same ten-clear Guardian threshold");
                check(normal.guardianClaimed(n)==owned.contains(n),"receipt ownership read without grant");
            }
            check(unlocked==clears/10,"exact unlocked count including10/50/1000");
            for(PrismStory.Chapter chapter:PrismStory.chapters()) {
                PrismStory.ChapterProgress p=normal.forChapter(chapter.number);
                check(p.chapter==chapter,"snapshot uses immutable chapter definition");
                check(p.unlocked==(clears>=chapter.unlockJourneyClears),"chapter unlock is Journey boundary");
                check(p.journeyTarget==100&&p.fragmentTarget==10,"explicit independent progress targets");
                check(p.journeyProgress>=0&&p.journeyProgress<=100,"progress bounded");
                check(p.fragmentsClaimed>=0&&p.fragmentsClaimed<=10,"fragment count bounded");
                check(p.completed==(p.fragmentsClaimed==10),"completion needs ten existing receipts");
                check(p.nextGuardian()==0||p.nextGuardian()>=chapter.firstGuardian&&p.nextGuardian()<=chapter.lastGuardian,"next receipt stays in chapter");
                check(p.nextGuardian()==0||!normal.guardianClaimed(p.nextGuardian()),"next receipt actually missing");
                check(p.storyIntroKey.equals(chapter.storyIntroKey)&&p.storyAfterKey.equals(chapter.storyAfterKey),"narrative keys preserved");
                progress+=p.journeyProgress;fragments+=p.fragmentsClaimed;
                // A route clear is not a claimed Guardian reward: empty receipts remain empty.
                PrismStory.ChapterProgress empty=PrismStory.forChapter(chapter.number,clears,Collections.emptySet());
                check(empty.fragmentsClaimed==0&&!empty.completed&&empty.nextGuardian()==chapter.firstGuardian,"clears never invent receipts");
            }
            check(progress==clears&&fragments==owned.size(),"chapters partition actual progress and supplied receipts");
            check(owned.equals(prefix(clears/10)),"input set unchanged");
            boundaries.append(clears).append(',').append(normal.currentChapter().chapter.number).append(',').append(normal.currentChapter().journeyProgress).append(',').append(normal.currentChapter().fragmentsClaimed).append(',').append(normal.nextGuardian()).append(',').append(unlocked).append('\n');
        }
        Set<Integer> odd=new HashSet<>(Arrays.asList(null,-99,0,1,1,3,10,11,99,100,101,Integer.MAX_VALUE));
        Set<Integer> original=new HashSet<>(odd);PrismStory.Snapshot copied=PrismStory.snapshot(0,odd);
        check(copied.guardianReceipts().equals(new HashSet<>(Arrays.asList(1,3,10,11,99,100))),"invalid and repeated receipt identifiers do not create ownership");
        check(copied.fragmentsClaimed==6,"only six distinct valid receipts count");
        check(copied.forChapter(1).fragmentsClaimed==3&&copied.forChapter(2).fragmentsClaimed==1&&copied.forChapter(10).fragmentsClaimed==2,"sparse receipts assigned to their exact chapters");
        check(copied.nextGuardian()==2&&copied.forChapter(1).nextGuardian()==2,"earliest gap rather than maximum receipt");
        check(odd.equals(original),"normalization leaves source receipt set untouched");odd.clear();check(copied.fragmentsClaimed==6&&copied.guardianClaimed(100),"defensive copy survives caller mutation");
        immutable(()->copied.guardianReceipts().add(2));immutable(()->copied.chapters.clear());
        check(PrismStory.snapshot(-1,null).journeyClears==0&&PrismStory.snapshot(Integer.MIN_VALUE,null).journeyClears==0,"negative clear count clamped");
        check(PrismStory.snapshot(1001,null).journeyClears==1000&&PrismStory.snapshot(Integer.MAX_VALUE,null).journeyClears==1000,"over-range clear count clamped without overflow");
        check(PrismStory.snapshot(1000,null).fragmentsClaimed==0&&!PrismStory.snapshot(1000,null).forChapter(10).completed,"1000 clears alone do not complete story");
        PrismStory.Snapshot all=PrismStory.snapshot(1000,prefix(100));check(all.nextGuardian()==0&&all.fragmentsClaimed==100,"all receipts leave no invented next guardian");
        PrismStory.Snapshot historical=PrismStory.snapshot(0,prefix(100));
        check(historical.fragmentsClaimed==100&&historical.forChapter(10).completed&&!historical.forChapter(10).unlocked,"supplied ownership is not erased or inferred from clear count");
        for(int n:new int[]{Integer.MIN_VALUE,-1,0,11,Integer.MAX_VALUE}) { invalid(()->copied.forChapter(n));invalid(()->PrismStory.forChapter(n,0,null)); }
        for(int n:new int[]{Integer.MIN_VALUE,-1,0,101,Integer.MAX_VALUE}) check(!copied.guardianClaimed(n)&&!copied.guardianUnlocked(n),"invalid Guardian has no progress");
        StringBuilder text=new StringBuilder("chapter,language,key,text\n");String[] languages={"en","fr","de","es","pt","it"};
        check(PrismStoryStrings.keys().size()==40,"four distinct narrative keys per chapter");
        immutable(()->PrismStoryStrings.keys().clear());
        Set<String> seenKeys=new HashSet<>();
        for(PrismStory.Chapter chapter:PrismStory.chapters()) {
            check(chapter.firstJourney==chapter.unlockJourneyClears+1&&chapter.lastJourney-chapter.firstJourney+1==100,"exact hundred-clear chapter range");
            check(chapter.lastGuardian-chapter.firstGuardian+1==10&&chapter.firstGuardian*10==chapter.unlockJourneyClears+10,"ten actual Guardian thresholds per chapter");
            check(chapter.biomeKey.equals("biome_"+(chapter.number-1)),"existing biome key, no invented map coordinate");
            for(String key:new String[]{chapter.titleKey,chapter.storyIntroKey,chapter.storyAfterKey,chapter.objectiveKey}) {
                check(seenKeys.add(key),"no reused chapter text key");
                for(String language:languages) {
                    String value=PrismStoryStrings.get(language,key);
                    check(!value.trim().isEmpty()&&!value.equals(key),"complete six-language text");
                    check(value.length()<240,"narrative stays bounded for notebook card");
                    check(!value.matches(".*%[0-9$]*[sd].*"),"no unfilled format placeholder");
                    check(value.equals(PrismStoryStrings.get(language.toUpperCase(Locale.ROOT)+"-XX",key)),"regional locale resolves to same supported language");
                    text.append(chapter.number).append(',').append(language).append(',').append(key).append(',').append(csv(value)).append('\n');
                }
                check(PrismStoryStrings.get("zz",key).equals(PrismStoryStrings.get("en",key)),"unsupported locale has English fallback");
            }
        }
        invalid(()->PrismStoryStrings.get("en","unknown_key"));
        int horizontal=0,vertical=0,pieces=0;
        for(int n=1;n<=100;n++) for(GuardianEncounter.Piece piece:GuardianCatalog.get(n).pieces) {
            pieces++;if(piece.direction==Direction.LEFT||piece.direction==Direction.RIGHT)horizontal++;else vertical++;
        }
        check(pieces==690&&horizontal==690&&vertical==0,"frozen G1 baseline characterized, not rebalanced");
        check(GuardianCatalog.get(4).minimumActions==7&&GuardianCatalog.get(5).minimumActions==28,"existing Guardian4to5 work discontinuity retained");
        Files.writeString(out.resolve("chapter-boundaries.csv"),boundaries.toString());Files.writeString(out.resolve("six-language-story.csv"),text.toString());
        String report="{\n  \"passed\":true,\n  \"assertions\":"+checks+",\n  \"journey_clear_states\":"+snapshots+",\n  \"chapters\":10,\n  \"narrative_keys\":40,\n  \"languages\":6,\n  \"localized_texts\":240,\n  \"existing_guardian_pieces\":"+pieces+",\n  \"existing_guardian_horizontal_pieces\":"+horizontal+",\n  \"existing_guardian_vertical_pieces\":"+vertical+",\n  \"guardian4_minimum\":7,\n  \"guardian5_minimum\":28,\n  \"scope\":\"Read-only narrative and boundary API source tests. Inputs are supplied clear counts and receipt sets, not observed people. No rewards, inventory, puzzle rules, budgets, saves, account or provider traffic are changed. Native presentation is separate.\"\n}\n";
        Files.writeString(out.resolve("summary.json"),report);System.out.print(report);
    }
}
