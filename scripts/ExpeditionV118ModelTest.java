import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;

/** Contract tests for actual certified U references and pure, immutable, restartable expedition state. */
public final class ExpeditionV118ModelTest {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static ExpeditionRun run(String who,int level,ExpeditionRun.Goal goal,long seed){return ExpeditionRun.start("contract:118",seed,who,level,goal,goal==ExpeditionRun.Goal.HOME?ExpeditionCatalog.SOUVENIR_ID:"nova");}
    private static void progression(){
        int[] thresholds={1,2,8,14,22,32,44,58,74,92};
        for(int n=0;n<thresholds.length;n++){
            check(CompanionProgression.threshold(n+1)==thresholds[n],"declared cumulative threshold");
            check(CompanionProgression.levelForCopies(thresholds[n])==n+1,"exact threshold earns level");
            check(CompanionProgression.levelForCopies(thresholds[n]-1)==n,"one fewer copy does not earn next level");
            CompanionProgression.Stats stats=CompanionProgression.stats(thresholds[n]);
            check(stats.level==n+1&&stats.copiesToNext==(n==9?0:thresholds[n+1]-thresholds[n]),"next level shown cumulatively");
        }
        check(CompanionProgression.levelForCopies(0)==0,"unowned has no character level");
        check(CompanionProgression.stats(100).level==10&&CompanionProgression.stats(100).surplus==8,"maximum never creates an eleventh level");
        check(CompanionProgression.initialCopies(true,0)==1&&CompanionProgression.initialCopies(true,8)==8,"legacy ownership migrates exactly once");
        check(CompanionProgression.initialCopies(false,100)==0,"saved number alone cannot create ownership");
        check(CompanionProgression.addCopies(Integer.MAX_VALUE,1)==Integer.MAX_VALUE,"copy addition does not wrap");
        check(CompanionProgression.skillCharges(1)==1&&CompanionProgression.skillCharges(2)==2&&CompanionProgression.skillCharges(3)==3&&CompanionProgression.skillCharges(6)==4&&CompanionProgression.skillCharges(10)==4,"actual run capacity increases at declared milestones");
        Set<String> all=new HashSet<>();int available=0;for(CompanionProgression.Definition item:CompanionProgression.all()){
            check(all.add(item.id)&&Catalog.character(item.id)!=null,"one fixed definition for each real identity");
            if(item.expeditionAvailable)available++;
        }
        check(all.size()==37&&available==3,"37 metadata definitions and three honestly available signatures");
        for(SummonCatalog.Item legacy:SummonCatalog.items(SummonCatalog.Banner.AVATARS)){
            CompanionProgression.Rarity expected=legacy.rarity==SummonCatalog.Rarity.COMMON?CompanionProgression.Rarity.N:legacy.rarity==SummonCatalog.Rarity.RARE?CompanionProgression.Rarity.R:legacy.id.equals("inky")?CompanionProgression.Rarity.SSR:CompanionProgression.Rarity.SR;
            check(CompanionProgression.find(legacy.id).rarity==expected,"fixed prototype label and unchanged historical pool");
            check(legacy.chanceBasisPoints==(legacy.rarity==SummonCatalog.Rarity.COMMON?600:legacy.rarity==SummonCatalog.Rarity.RARE?375:200),"historical individual probability unchanged");
        }
        check(SummonCatalog.VERSION.equals("v116_1")&&SummonCatalog.priceCoins(SummonCatalog.Banner.AVATARS)==180,"historical draw version and price preserved");
    }
    private static void boards(){
        for(int number:ExpeditionCatalog.boardNumbers()){
            Level level=RelayCatalog.get(number);check(level.progressionVersion==6&&level.number==number,"unchanged certified U identity");
            Board board=new Board(level);for(int action:level.actionSolution)check(board.performAction(action).success,"existing certificate remains physically legal");
            check(board.isSolved(),"existing certificate clears expedition board");
            check(!board.supportsManualGates(),"no new puzzle rule silently enabled");
        }
    }
    private static ExpeditionRun clear(ExpeditionRun run,String optionId){
        ExpeditionRun selected=run.choose(optionId);check(selected!=null&&run.selectedOption()==null,"selection is immutable");
        int board=selected.selectedOption().boardNumber;Level real=RelayCatalog.get(board);Board actual=new Board(real);
        boolean keys=!real.barriers.isEmpty(),relays=false;for(Tile piece:real.tiles)relays|=piece.switchMask!=0;
        check(selected.selectedOption().encounter==(keys?ExpeditionCatalog.Encounter.KEY:relays?ExpeditionCatalog.Encounter.RELAY:ExpeditionCatalog.Encounter.ARROWS),"announced mechanic matches actual certified board");
        for(int action:real.actionSolution)check(actual.performAction(action).success,"play actual selected U certificate");
        check(actual.isSolved(),"selected puzzle truly clears");String request="clear:"+selected.completedStages;
        check(selected.complete(request,board+1000,3)==null,"a different board cannot advance route");
        check(selected.complete(request,board,0)==null,"invalid result cannot award progress");
        ExpeditionRun won=selected.complete(request,board,3);check(won!=null&&won.completedStages==run.completedStages+1,"one victory advances precisely one stage");
        check(won.complete(request,board,3)==won,"same victory identity replays without advancing or new reward");
        check(won.complete(request,board,2)==null,"victory identity cannot change result on replay");
        check(won.encode().equals(ExpeditionRun.decode(won.encode()).encode()),"cold codec restores exact result and route");return won;
    }
    private static void skills(){
        ExpeditionRun nova=run("nova",10,ExpeditionRun.Goal.COPIES,88);ExpeditionCatalog.Option entry=nova.options().get(0);
        ExpeditionRun peek=nova.useSkill(entry.id);check(peek!=null&&peek.revealed(entry.id)&&peek.skillRemaining()==3&&!nova.revealed(entry.id),"Nova reveals a future candidate and consumes a saved charge");
        check(peek.useSkill(entry.id)==null,"same future candidate cannot consume repeated charges");
        int forecast=entry.futureBoardNumber;peek=clear(peek,entry.id);boolean actual=false;for(ExpeditionCatalog.Option next:peek.options())actual|=next.boardNumber==forecast;
        check(actual,"revealed candidate is a real option beyond the visible puzzle");
        for(ExpeditionCatalog.Option fork:peek.options()){
            ExpeditionRun revealed=peek.useSkill(fork.id);check(revealed!=null,"both fork candidates can be revealed independently");
            ExpeditionRun after=clear(revealed,fork.id);boolean found=false;for(ExpeditionCatalog.Option next:after.options())found|=next.boardNumber==fork.futureBoardNumber;
            check(found,"each branch forecast matches its actual downstream catalog");
        }
        ExpeditionRun miko=run("miko",1,ExpeditionRun.Goal.COPIES,88);ExpeditionCatalog.Option old=miko.options().get(0);
        ExpeditionRun rerouted=miko.useSkill(old.id);check(rerouted!=null&&rerouted.skillRemaining()==0,"Miko consumes one charge");
        ExpeditionCatalog.Option alternate=rerouted.options().get(0);check(alternate.alternate&&!alternate.id.equals(old.id)&&alternate.boardNumber!=old.boardNumber,"Miko substitutes an actually different certified board");
        check(miko.options().get(0).boardNumber==old.boardNumber&&rerouted.choose(old.id)==null,"prior state unchanged and retired option cannot be chosen");
        check(rerouted.useSkill(alternate.id)==null,"no repeated substitution on one offer");
        ExpeditionRun orbit=run("orbit",1,ExpeditionRun.Goal.HOME,88),project=orbit.useSkill("project");
        check(project!=null&&project.projectOpened()&&project.options().size()==2&&project.options().get(1).project,"Orbit adds a distinct project route before the entry");
        check(project.options().get(1).encounter==ExpeditionCatalog.Encounter.KEY,"home project takes a key route");
        for(long seed=-100;seed<=100;seed++){
            ExpeditionRun variants=run("orbit",10,ExpeditionRun.Goal.HOME,seed);
            for(int stage=0;stage<4;stage++){
                variants=variants.useSkill("project");Set<Integer> offered=new HashSet<>();for(ExpeditionCatalog.Option candidate:variants.options())check(offered.add(candidate.boardNumber),"project route never duplicates an already offered board");
                variants=clear(variants,variants.options().get(0).id);
            }
        }
        check(project.useSkill("project")==null&&orbit.options().size()==1,"project route opens once without mutating departure");
        check(project.choose(project.options().get(1).id).useSkill("project")==null,"skills cannot replace a selected board");
        for(int level:new int[]{1,2,3,6,10}){
            ExpeditionRun scout=run("nova",level,ExpeditionRun.Goal.COPIES,level);int used=0;
            for(int stage=0;stage<4;stage++){
                for(ExpeditionCatalog.Option candidate:scout.options()){ExpeditionRun reveal=scout.useSkill(candidate.id);if(reveal!=null){scout=reveal;used++;}}
                scout=clear(scout,scout.options().get(0).id);
            }
            check(used==CompanionProgression.skillCharges(level),"every Nova capacity milestone reveals a usable future candidate");
        }
        for(String who:new String[]{"miko","orbit"})for(int level:new int[]{1,2,3,6,10}){
            ExpeditionRun current=run(who,level,ExpeditionRun.Goal.COPIES,level);int used=0;
            for(int stage=0;stage<4;stage++){
                ExpeditionCatalog.Option before=current.options().get(0);
                ExpeditionRun effect=current.useSkill(who.equals("orbit")?"project":before.id);
                if(effect!=null){current=effect;used++;}
                current=clear(current,current.options().get(0).id);
            }
            check(used==CompanionProgression.skillCharges(level),"every declared charge has an actual usable stage for "+who+level);
        }
    }
    private static void branchingAndCodec(){
        for(long seed:new long[]{0,1,-1,Long.MIN_VALUE,Long.MAX_VALUE,96321})for(int route=0;route<4;route++){
            ExpeditionRun current=run("nova",1,ExpeditionRun.Goal.COPIES,seed);check(current!=null&&current.completedStages==0&&current.skillRemaining()==1,"valid departure snapshot");
            for(int stage=0;stage<4;stage++){
                List<ExpeditionCatalog.Option> options=current.options();check(options.size()==(stage==1||stage==2?2:1),"four stages and two actual forks");
                if(options.size()==2)check(options.get(0).boardNumber!=options.get(1).boardNumber&&options.get(0).encounter!=options.get(1).encounter,"fork changes actual challenge and mechanic");
                current=clear(current,options.get(options.size()==1?0:(route>>(stage-1))&1).id);
            }
            check(current.finished()&&current.results().size()==4&&current.totalStars()==12,"exactly four recorded real victories finish route");
            check(current.copiesReward()==1&&current.homeContributionReward()==0&&!current.claimed,"announced targeted main reward has no premature claim");
            ExpeditionRun claimed=current.claim();check(claimed.claimed&&claimed.claim()==claimed&&current.claimed==false,"claim marker immutable and idempotent");
            check(claimed.choose("entry")==null&&claimed.useSkill("entry")==null,"finished run cannot reopen route");
            String saved=claimed.encode();check(saved.equals(ExpeditionRun.decode(saved).encode()),"completed and claimed route survives cold codec");
            for(String invalid:new String[]{saved+";Q",saved.replace("v118_1|","v999_1|"),saved.replace("W,clear:0","W,clear:1"),saved.replace("|nova|1|","|inky|1|"),saved.replace("|COPIES|nova|","|COPIES|missing|")})
                check(ExpeditionRun.decode(invalid)==null,"corrupt or noncanonical record wholly refused");
        }
        ExpeditionRun fresh=run("nova",1,ExpeditionRun.Goal.HOME,5);check(fresh.claim()==null&&fresh.copiesReward()==0&&fresh.homeContributionReward()==0,"no reward before actual full completion");
        check(fresh.choose("missing")==null&&fresh.complete("skip",4,3)==null,"unknown route and skipped selection refused");
        check(ExpeditionRun.start("bad/id",0,"nova",1,ExpeditionRun.Goal.COPIES,"nova")==null,"delimiter injection rejected");
        check(ExpeditionRun.start("ok",0,"nova",1,ExpeditionRun.Goal.HOME,"sofa")==null,"unknown home project refused");
        check(ExpeditionRun.decode(null)==null&&ExpeditionRun.decode("")==null&&ExpeditionRun.decode("x".repeat(8193))==null,"empty and oversized data refused");
    }
    public static void main(String[] args)throws Exception{
        progression();boards();skills();branchingAndCodec();
        String report="{\n  \"passed\": true,\n  \"checks\": "+checks+",\n  \"certified_board_references\": "+ExpeditionCatalog.boardNumbers().size()+",\n  \"scope\": \"Pure model and codec with actual U board certificates, copies thresholds and migration, immutable branching and skill effects, idempotent completion/claim contracts. Store atomicity and native rendering are validated separately.\"\n}\n";
        if(args.length>0){Path output=Path.of(args[0]);Files.createDirectories(output.getParent());Files.writeString(output,report);}System.out.println(report);
    }
}
