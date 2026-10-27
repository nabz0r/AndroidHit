import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;
/** Proposed mechanic model only. Each closed border may be opened for one action. */
public final class ManualGateAudit {
 static final int INF=1000000;
 static final class DP {
  final EarlyTrapAudit.Model m;final int[][] costs;
  DP(EarlyTrapAudit.Model m){this.m=m;costs=new int[16][1<<m.l.tiles.size()];for(int[]a:costs){Arrays.fill(a,-1);a[0]=0;}}
  int cost(long mask,int gates){int idx=(int)mask;if(costs[gates][idx]>=0)return costs[gates][idx];int best=INF;
   for(Tile t:m.l.tiles)if((mask&(1L<<t.id))!=0&&(gates&t.direction.bit)!=0&&(mask&m.ob[t.id])==0)best=Math.min(best,cost(mask&~(1L<<t.id),gates^t.switchMask));
   for(int bit=1;bit<=8;bit<<=1)if((gates&bit)==0)best=Math.min(best,1+cost(mask,gates|bit));
   return costs[gates][idx]=best;
  }
  String trace(long mask,int gates){StringJoiner steps=new StringJoiner(" -> ");while(mask!=0){int value=cost(mask,gates);boolean exited=false;for(Tile t:m.l.tiles)if((mask&(1L<<t.id))!=0&&(gates&t.direction.bit)!=0&&(mask&m.ob[t.id])==0&&cost(mask&~(1L<<t.id),gates^t.switchMask)==value){steps.add("exit:"+t.id);mask&=~(1L<<t.id);gates^=t.switchMask;exited=true;break;}if(exited)continue;boolean opened=false;for(Direction d:Direction.values())if((gates&d.bit)==0&&1+cost(mask,gates|d.bit)==value){steps.add("open:"+d);gates|=d.bit;opened=true;break;}if(!opened)throw new AssertionError("Trace failed");}return steps.toString();}
 }
 public static void main(String[]args)throws Exception{
  StringBuilder csv=new StringBuilder("level,original_losing_states,manual_recoverable_losing_states,max_extra_gate_opens_over_original_states,states_recoverable_in_original_budget,original_reachable_states\n");
  for(int n=1;n<=30;n++){EarlyTrapAudit.Model m=new EarlyTrapAudit.Model(BalancedCatalog.get(n));DP dp=new DP(m);int losing=0,recover=0,max=0,fits=0;MoveBudget b=MoveBudget.start(0,m.l);
   if(dp.cost(m.l.allTilesMask,m.l.initialGateMask)!=0)throw new AssertionError("Certificate should need no manual opening");
   for(long mask:m.paths.keySet()){int c=dp.cost(mask,m.gates(mask));if(c>=INF)throw new AssertionError("Spatial deadlock despite proposed gate control");max=Math.max(max,c);if(!m.eval(mask)){losing++;if(c<INF)recover++;}if(c<=b.mistakeAllowance())fits++;}
   csv.append(n).append(',').append(losing).append(',').append(recover).append(',').append(max).append(',').append(fits).append(',').append(m.paths.size()).append('\n');
   if(n==7||n==8){int[]trap=n==7?new int[]{2,3,5}:new int[]{0,1,2,5};long mask=m.l.allTilesMask;for(int id:trap)mask&=~(1L<<id);System.out.println("J"+n+" trap extra manual opens="+dp.cost(mask,m.gates(mask))+"; remaining exits="+Long.bitCount(mask)+"; total proposed recovery="+(Long.bitCount(mask)+dp.cost(mask,m.gates(mask)))+"; trace "+dp.trace(mask,m.gates(mask)));}
  }
  Files.writeString(Path.of(args[0],"manual-gate-proposal-metrics.csv"),csv);System.out.print(csv);
 }
}
