import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;

/** Offline audit of every legal branch, independent of the runtime planner's certificate. */
public final class AuditJourneyV114 {
    static final Direction[] DS=Direction.values();
    static long referenceStates;
    static final class Costs {
        final Level level;final long[] obstruction;final byte[] minimum,all,useful,greedy;
        long masks;
        Costs(Level l){level=l;obstruction=new long[l.tiles.size()];int capacity=1<<(l.tiles.size()+4);minimum=new byte[capacity];all=new byte[capacity];useful=new byte[capacity];greedy=new byte[capacity];
            for(int g=0;g<16;g++)minimum[g]=1;
            for(Tile t:l.tiles)for(int row=t.row+t.direction.dr,col=t.col+t.direction.dc;row>=0&&col>=0&&row<l.size&&col<l.size;row+=t.direction.dr,col+=t.direction.dc){for(Tile b:l.tiles)if(b.row==row&&b.col==col)obstruction[t.id]|=1L<<b.id;for(Barrier b:l.barriers)if(b.row==row&&b.col==col)obstruction[t.id]|=1L<<b.keyTileId;}
            fill(l.allTilesMask);
        }
        boolean spatial(long mask,Tile t){return(mask&(1L<<t.id))!=0&&(mask&obstruction[t.id])==0;}
        int key(long mask,int gates){return(int)((mask<<4)|gates);}
        void fill(long mask){int start=key(mask,0);if(minimum[start]!=0)return;masks++;long free=0;
            for(Tile t:level.tiles)if(spatial(mask,t)){free|=1L<<t.id;fill(mask&~(1L<<t.id));}
            if(free==0)throw new AssertionError("Spatial cycle in "+level.number);
            for(int gates=15;gates>=0;gates--){int best=255,ma=0,mu=0,mg=0;boolean exit=false;
                for(Tile t:level.tiles)if((free&(1L<<t.id))!=0&&(gates&t.direction.bit)!=0){int child=key(mask&~(1L<<t.id),gates^t.switchMask);exit=true;best=Math.min(best,minimum[child]&255);ma=Math.max(ma,1+(all[child]&255));mu=Math.max(mu,1+(useful[child]&255));mg=Math.max(mg,1+(greedy[child]&255));}
                for(Direction d:DS)if((gates&d.bit)==0){int child=start|(gates|d.bit);best=Math.min(best,minimum[child]&255);ma=Math.max(ma,1+(all[child]&255));boolean enables=false;for(Tile t:level.tiles)if((free&(1L<<t.id))!=0&&t.direction==d)enables=true;
                    if(enables){mu=Math.max(mu,1+(useful[child]&255));if(!exit)mg=Math.max(mg,1+(greedy[child]&255));}}
                if(best>=255||ma>=255||mu==0||mg==0)throw new AssertionError("Invalid gate cube");int index=start|gates;minimum[index]=(byte)(best+1);all[index]=(byte)ma;useful[index]=(byte)mu;greedy[index]=(byte)mg;
            }
        }
        int min(long mask,int gates){return(minimum[key(mask,gates)]&255)-1;}
        int maximum(long mask,int gates,int policy){return((policy==0?all:policy==1?useful:greedy)[key(mask,gates)]&255);}
        int[] actions(long mask,int gates,int policy){if(mask==0)return new int[0];ArrayList<Integer> result=new ArrayList<>();for(Tile t:level.tiles)if(spatial(mask,t)&&(gates&t.direction.bit)!=0)result.add(t.id);boolean exit=!result.isEmpty();for(Direction d:DS)if((gates&d.bit)==0){boolean enables=false;for(Tile t:level.tiles)if(spatial(mask,t)&&t.direction==d)enables=true;if(policy==0||enables&&(policy!=2||!exit))result.add(Board.gateAction(d));}return result.stream().mapToInt(Integer::intValue).toArray();}
        String witness(int base,int policy){long mask=level.allTilesMask;int gates=level.initialGateMask,used=0;StringBuilder path=new StringBuilder();while(mask!=0&&used+min(mask,gates)<=base){int chosen=-1,max=-1;for(int action:actions(mask,gates,policy)){long nextMask=mask;int nextGates=gates;if(Board.isGateAction(action))nextGates|=Board.gateDirection(action).bit;else{nextMask&=~(1L<<action);nextGates^=level.tile(action).switchMask;}int cost=1+maximum(nextMask,nextGates,policy);if(cost>max){max=cost;chosen=action;}}
                if(chosen<0)throw new AssertionError("No witness action");if(path.length()>0)path.append(',');path.append(chosen);if(Board.isGateAction(chosen))gates|=Board.gateDirection(chosen).bit;else{mask&=~(1L<<chosen);gates^=level.tile(chosen).switchMask;}used++;}
            return "{\"actions\":["+path+"],\"used\":"+used+",\"minimumRemaining\":"+min(mask,gates)+",\"remainingBudget\":"+(base-used)+",\"deficit\":"+Math.max(0,used+min(mask,gates)-base)+"}";
        }
    }
    public static void main(String[] args)throws Exception{Path out=Paths.get(args.length>0?args[0]:"docs/validation/v1.14/audit-journey");Files.createDirectories(out);int through=args.length>1?Integer.parseInt(args[1]):1000;StringBuilder csv=new StringBuilder("level,size,pieces,minimum,base,margin,initial_legal_exits,initial_closed_gates,switches,keys,decision_points,structural_score,worst_all_legal,worst_immediately_useful_gates,worst_exits_before_gates,spatial_masks,reference_gate_states\n"),witnesses=new StringBuilder("[\n");int allTrap=0,usefulTrap=0,greedyTrap=0;long started=System.nanoTime();
        for(int n=1;n<=through;n++){Level l=StrategyCatalog.get(n);Costs c=new Costs(l);MoveBudget b=MoveBudget.start(0,l);int min=c.min(l.allTilesMask,l.initialGateMask);if(min!=l.minimumActions)throw new AssertionError("Independent minimum mismatch L"+n);ProgressionInfo info=StrategyCatalog.info(n,false);int switches=0,initialExits=0;for(Tile t:l.tiles){if(t.switchMask!=0)switches++;if(c.spatial(l.allTilesMask,t)&&(l.initialGateMask&t.direction.bit)!=0)initialExits++;}int a=c.maximum(l.allTilesMask,l.initialGateMask,0),u=c.maximum(l.allTilesMask,l.initialGateMask,1),g=c.maximum(l.allTilesMask,l.initialGateMask,2);allTrap+=a>b.base()?1:0;usefulTrap+=u>b.base()?1:0;greedyTrap+=g>b.base()?1:0;referenceStates+=(c.masks+1)*16;
            csv.append(String.format(Locale.ROOT,"%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n",n,l.size,l.tiles.size(),min,b.base(),b.mistakeAllowance(),initialExits,4-Integer.bitCount(l.initialGateMask),switches,info.keyCount,info.decisionPoints,info.structuralScore,a,u,g,c.masks+1,(c.masks+1)*16));
            if(n>1)witnesses.append(",\n");witnesses.append("{\"level\":").append(n).append(",\"allLegal\":").append(c.witness(b.base(),0)).append(",\"immediatelyUsefulGates\":").append(c.witness(b.base(),1)).append(",\"exitsBeforeGates\":").append(c.witness(b.base(),2)).append('}');
            if(n<=30||n%100==0)System.out.println("L"+n+" min="+min+" budget="+b.base()+" worst-all="+a+" useful="+u+" exits-first="+g);
        }
        Files.writeString(out.resolve("audit-all-branches.csv"),csv);Files.writeString(out.resolve("audit-budget-witnesses.json"),witnesses.append("\n]\n"));String summary="{\n  \"scope\":\"Every finite legal branch of the frozen V4 Journey; no blocked attempts, undo, powers or extension. Structural policy bounds are not human difficulty or pass rates.\",\n  \"boards\":"+through+",\n  \"referenceGateStates\":"+referenceStates+",\n  \"boardsWithSomeAllLegalPathOverBudget\":"+allTrap+",\n  \"boardsWithSomeImmediatelyUsefulGatePathOverBudget\":"+usefulTrap+",\n  \"boardsWithSomeExitsBeforeGatesPathOverBudget\":"+greedyTrap+",\n  \"elapsedSeconds\":"+((System.nanoTime()-started)/1e9)+"\n}\n";Files.writeString(out.resolve("audit-summary.json"),summary);System.out.println(summary);
    }
}
