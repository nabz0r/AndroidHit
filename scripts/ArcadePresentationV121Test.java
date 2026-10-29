import com.androidhit.lumadrift.engine.*;
import com.androidhit.lumadrift.ui.RewardMotion121;
import java.nio.file.*;
import java.util.*;

/** Confirmed display interpolation and actual direct-exit Focus accounting. */
public final class ArcadePresentationV121Test {
    private static long checks;
    private static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
    private static void counters(){
        RewardMotion121.Counter value=new RewardMotion121.Counter();
        check(value.sample(100,0,false)==100 && value.gain(0)==0,"initial confirmed balance has no invented gain");
        check(value.sample(200,100,false)==100 && value.gain(100)==100,"actual confirmed gain starts at visible prior balance");
        check(value.sample(200,410,false)==150,"half-duration smoothstep midpoint");
        check(value.sample(250,410,false)==150 && value.gain(410)==50,"interrupted gain preserves visible continuity and only new confirmed delta");
        check(value.sample(250,720,false)==200 && value.sample(250,1030,false)==250,"interrupted gain reaches exact confirmed endpoint");
        check(value.gain(1510)==0,"gain particles expire at declared bound");
        value.unavailable();
        check(value.gain(1511)==0 && value.sample(900,1600,false)==900 && value.gain(1600)==0,"unknown wallet resets baseline without fictional credit");
        check(value.sample(500,1700,false)==900 && value.gain(1700)==0,"confirmed spend animates down without a positive gain");
        long previous=900;
        for(long now=1700;now<=2320;now+=10){long current=value.sample(500,now,false);check(current<=previous && current>=500,"spend interpolation stays monotone and bounded");previous=current;}
        check(previous==500,"spend ends exactly at confirmed balance");
        check(value.sample(1000,2500,true)==1000 && value.gain(2500)==0,"reduced motion settles gains immediately without reward animation");
        check(value.sample(125,2600,true)==125 && value.gain(2600)==0,"reduced motion settles spends immediately");
        check(value.sample(-1,2700,true)==0,"display domain clamps impossible negative confirmed amount");

        RewardMotion121.Counter clock=new RewardMotion121.Counter();
        clock.sample(10,1000,false);clock.sample(110,1100,false);
        long half=clock.sample(110,1410,false);
        check(half==60 && clock.sample(110,1200,false)==half,"sample clock regression cannot rewind animation");
        check(clock.sample(110,1720,false)==110 && clock.sample(110,1000,false)==110,"settled value remains settled after clock regression");

        long[] balances={0,1,1L<<53,(1L<<53)+1,Long.MAX_VALUE-1024,Long.MAX_VALUE-10,Long.MAX_VALUE};
        for(long amount:balances){
            RewardMotion121.Counter large=new RewardMotion121.Counter();
            check(large.sample(amount,0,false)==amount,"large initialization exact");
            check(large.sample(amount,620,false)==amount,"large settled endpoint exact");
            check(large.sample(0,1000,false)==amount && large.gain(1000)==0,"large decrease cannot invent positive gain");
            check(large.sample(0,1620,false)==0,"large decrease exact endpoint");
            check(large.sample(amount,2000,false)==0 && large.gain(2000)==amount,"large confirmed gain remains exact and nonnegative");
            long mid=large.sample(amount,2310,false);check(mid>=0 && mid<=amount,"large interpolation stays inside actual balances");
            check(large.sample(amount,2620,false)==amount,"large increase exact endpoint");
        }
    }
    private static ExitChain121 cold(ExitChain121 chain,Level level){
        ExitChain121 result=ExitChain121.decode(chain.encode(),level);
        check(result!=null && result.encode().equals(chain.encode()),"canonical chain roundtrip");
        check(result.progress()==chain.progress() && result.ready()==chain.ready() && result.spent()==chain.spent(),"restored actual chain state exact");
        return result;
    }
    private static void actualChains(){
        for(int number=1;number<=1000;number++){
            Level level=RelayCatalog.get(number);Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);
            ExitChain121 chain=new ExitChain121();int exits=0;
            for(int id:level.solution){
                Board.MoveResult result=budget.attempt(board,id);
                check(result.success && result.tile!=null,"chain fixture uses actual certified legal direct exit");
                exits++;chain.exit(result.tile.id);chain=cold(chain,level);
                check(chain.progress()==Math.min(3,exits),"only distinct actual direct exits advance one step");
                check(chain.ready()==(exits>=3),"Focus ready exactly after third distinct real exit");
                int before=chain.progress();check(!chain.exit(result.tile.id) && chain.progress()==before,"duplicate exit cannot farm progress");
            }
            if(level.tiles.size()>=3){
                chain.correction();check(chain.ready(),"earned Focus survives later correction");
                check(chain.use() && chain.spent() && !chain.ready() && !chain.use(),"Focus explicit use succeeds exactly once");
                chain=cold(chain,level);for(Tile tile:level.tiles)chain.exit(tile.id);
                check(!chain.ready() && chain.spent(),"repeated or later exits cannot refill spent Focus");
            }
        }
        Level level=null;for(int number=1000;number>=1;number--)if(RelayCatalog.get(number).tiles.size()>=6){level=RelayCatalog.get(number);break;}
        check(level!=null,"real board with six exits available");
        Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);ExitChain121 chain=new ExitChain121();
        for(int step=0;step<2;step++){check(budget.attempt(board,level.solution[step]).success,"actual early exit");chain.exit(level.solution[step]);}
        check(chain.progress()==2 && !chain.ready(),"two exits not yet earned");
        check(budget.undo(board,0),"real Undo restores last piece");chain.correction();
        check(chain.progress()==0 && !chain.ready(),"correction resets only unearned streak");
        check(budget.attempt(board,level.solution[1]).success,"actual re-exit after Undo");chain.exit(level.solution[1]);
        check(chain.progress()==0,"Undo and re-exit cannot farm the previously credited ID");
        for(int step=2;step<5;step++){check(budget.attempt(board,level.solution[step]).success,"new distinct actual direct exit after correction");chain.exit(level.solution[step]);}
        check(chain.ready() && chain.progress()==3,"three genuinely new direct exits can earn Focus after correction");
    }
    private static void refusedAndCodec(){
        Level level=RelayCatalog.get(1000);ExitChain121 chain=new ExitChain121();String empty=chain.encode();
        for(int id:new int[]{-1,64,65,66,67,Integer.MAX_VALUE})check(!chain.exit(id) && chain.encode().equals(empty),"invalid or gate-action IDs never credit chain");
        Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);
        for(Direction direction:Direction.values()){
            Board.MoveResult refusal=budget.attempt(board,Board.gateAction(direction));
            check(!refusal.success && refusal.tile==null,"actual relay gate action is refused");
            if(refusal.success && refusal.tile!=null)chain.exit(refusal.tile.id);
            check(chain.progress()==0,"refused action contributes no direct exit");
        }
        check(ExitChain121.decode(null,level).encode().equals(empty) && ExitChain121.decode("",level).encode().equals(empty),"historical missing field starts empty");
        for(String malformed:new String[]{"x","2:0:0:0:","1:01:0:0:","1:+1:0:0:0","1:-1:0:0:","1:4:1:0:0,1,2,3","1:3:0:0:0,1,2","1:0:0:1:","1:3:1:0:0,1","1:1:0:0:0,0","1:1:0:0:01","1:1:0:0:+1","1:1:0:0:64","1:2:0:0:1,0","1:1:0:0:0,","1:0:0:0:|"})
            check(ExitChain121.decode(malformed,level)==null,"malformed/noncanonical chain rejected");
        Level small=RelayCatalog.get(1);int foreign=small.tiles.size();
        check(level.tile(foreign)!=null && small.tile(foreign)==null,"cross-puzzle witness ID only exists in larger puzzle");
        ExitChain121 foreignState=new ExitChain121();foreignState.exit(foreign);
        check(ExitChain121.decode(foreignState.encode(),level)!=null && ExitChain121.decode(foreignState.encode(),small)==null,"foreign absent tile ID cannot restore into small puzzle");
    }
    public static void main(String[] args)throws Exception{
        counters();actualChains();refusedAndCodec();
        String report="{\n  \"passed\":true,\n  \"checks\":"+checks+",\n  \"boards_with_actual_direct_exits\":1000,\n  \"scope\":\"Pure confirmed-balance display and actual U1-U1000 direct-exit chain model: unknown/reset/spend/interrupted/large/reduced-motion/clock cases, real Undo anti-farm, once-only Focus, strict codecs and absent foreign IDs. No Android animation, input, persistence, player acceptance or extra economy credit.\"\n}\n";
        if(args.length>0){Path out=Path.of(args[0]);Files.createDirectories(out.getParent());Files.writeString(out,report);}System.out.print(report);
    }
}
