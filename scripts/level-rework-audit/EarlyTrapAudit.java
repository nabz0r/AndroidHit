import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;
import java.math.BigInteger;

/** Temporary read-only audit. Independent cell-ray reachability; no app edits. */
public final class EarlyTrapAudit {
  static Path out;
  static final StringBuilder csv=new StringBuilder("level,code,pieces,base,margin,switches,paired_switches,keys,barriers,initial_gates,reachable_states,losing_states,losing_with_legal,winning_states,mixed_winning_states,legal_edges,doom_edges_from_winning,earliest_doom_depth,earliest_hidden_doom_depth,initial_legal,initial_risky,uniform_legal_completion_probability,solution_orders\n");
  static final class Model {
    final Level l; final long[] ob; final byte[] win; final double[] probability; final BigInteger[] orders;
    final Map<Long,int[]> paths=new LinkedHashMap<>();
    Model(Level l){this.l=l;ob=new long[l.tiles.size()];win=new byte[1<<l.tiles.size()];probability=new double[win.length];Arrays.fill(probability,-1);orders=new BigInteger[win.length];win[0]=2;probability[0]=1;orders[0]=BigInteger.ONE;
      for(Tile t:l.tiles)for(int r=t.row+t.direction.dr,c=t.col+t.direction.dc;r>=0&&c>=0&&r<l.size&&c<l.size;r+=t.direction.dr,c+=t.direction.dc){for(Tile p:l.tiles)if(p.row==r&&p.col==c)ob[t.id]|=1L<<p.id;for(Barrier b:l.barriers)if(b.row==r&&b.col==c)ob[t.id]|=1L<<b.keyTileId;}
      eval(l.allTilesMask);ArrayDeque<Long> q=new ArrayDeque<>();q.add(l.allTilesMask);paths.put(l.allTilesMask,new int[0]);
      while(!q.isEmpty()){long mask=q.remove();int[] p=paths.get(mask);for(int id:legal(mask)){long next=mask&~(1L<<id);if(!paths.containsKey(next)){int[] np=Arrays.copyOf(p,p.length+1);np[p.length]=id;paths.put(next,np);q.add(next);}}}
    }
    int gates(long mask){int g=l.initialGateMask;for(Tile t:l.tiles)if((mask&(1L<<t.id))==0)g^=t.switchMask;return g;}
    List<Integer> legal(long mask){ArrayList<Integer> a=new ArrayList<>();int gates=gates(mask);for(Tile t:l.tiles)if((mask&(1L<<t.id))!=0&&(gates&t.direction.bit)!=0&&(mask&ob[t.id])==0)a.add(t.id);return a;}
    boolean eval(long mask){int idx=(int)mask;if(win[idx]!=0)return win[idx]==2;boolean good=false;for(int id:legal(mask)){boolean child=eval(mask&~(1L<<id));good|=child;}win[idx]=(byte)(good?2:1);return good;}
    double probability(long mask){int idx=(int)mask;if(probability[idx]>=0)return probability[idx];List<Integer>a=legal(mask);double p=0;for(int id:a)p+=probability(mask&~(1L<<id));return probability[idx]=a.isEmpty()?0:p/a.size();}
    BigInteger orders(long mask){int idx=(int)mask;if(orders[idx]!=null)return orders[idx];BigInteger n=BigInteger.ZERO;for(int id:legal(mask))n=n.add(orders(mask&~(1L<<id)));return orders[idx]=n;}
  }
  static String coords(Level l,int[] path){StringJoiner j=new StringJoiner(", ","[","]");for(int id:path){Tile t=l.tile(id);j.add("{id:"+id+",row:"+(t.row+1)+",col:"+(t.col+1)+",exit:"+t.direction+"}");}return j.toString();}
  static String safe(Model m,long mask){List<Integer>a=new ArrayList<>();for(int id:m.legal(mask))if(m.eval(mask&~(1L<<id)))a.add(id);return a.toString();}
  static String risky(Model m,long mask){List<Integer>a=new ArrayList<>();for(int id:m.legal(mask))if(!m.eval(mask&~(1L<<id)))a.add(id);return a.toString();}
  static void one(int n)throws Exception {
    Level l=BalancedCatalog.get(n);Model m=new Model(l);MoveBudget budget=MoveBudget.start(0,l);
    int dead=0,deadLegal=0,mixed=0,edges=0,doomEdges=0,depth=-1,hidden=-1,switches=0,paired=0;long firstDead=-1,firstHidden=-1;
    for(Tile t:l.tiles){if(t.switchMask!=0)switches++;if(Integer.bitCount(t.switchMask)>1)paired++;}
    StringBuilder states=new StringBuilder("mask,gates,depth,winning,legal,safe,risky,shortest_path\n");
    for(Map.Entry<Long,int[]>e:m.paths.entrySet()){
      long mask=e.getKey();int[] path=e.getValue();List<Integer>a=m.legal(mask);edges+=a.size();boolean good=m.eval(mask);int risk=0;
      Board board=new Board(l);if(!board.restoreHistory(path))throw new AssertionError("Bad BFS path");if(board.gateMask()!=m.gates(mask)||board.hasSolution()!=good||!new HashSet<>(a).equals(new HashSet<Integer>(){{for(Tile t:board.legalMoves())add(t.id);}}))throw new AssertionError("Runtime disagreement");
      for(int id:a){boolean safe=m.eval(mask&~(1L<<id));if(board.isSafeMove(id)!=safe)throw new AssertionError("Safe move disagreement");if(good&&!safe){risk++;doomEdges++;}}
      if(!good){dead++;if(depth<0){depth=path.length;firstDead=mask;}if(!a.isEmpty()){deadLegal++;if(hidden<0){hidden=path.length;firstHidden=mask;}}}
      if(good&&risk>0&&risk<a.size())mixed++;
      states.append(mask).append(',').append(m.gates(mask)).append(',').append(path.length).append(',').append(good).append(',').append('"').append(a).append("\",\"").append(safe(m,mask)).append("\",\"").append(risky(m,mask)).append("\",\"").append(Arrays.toString(path)).append("\"\n");
    }
    String code=ChallengeCode.encodeBalancedV3(n);int initRisk=0;for(int id:m.legal(l.allTilesMask))if(!m.eval(l.allTilesMask&~(1L<<id)))initRisk++;
    csv.append(n).append(',').append(code).append(',').append(l.tiles.size()).append(',').append(budget.base()).append(',').append(budget.mistakeAllowance()).append(',').append(switches).append(',').append(paired).append(',').append(BalancedCatalog.info(n,false).keyCount).append(',').append(l.barriers.size()).append(',').append(l.initialGateMask).append(',').append(m.paths.size()).append(',').append(dead).append(',').append(deadLegal).append(',').append(m.paths.size()-dead).append(',').append(mixed).append(',').append(edges).append(',').append(doomEdges).append(',').append(depth).append(',').append(hidden).append(',').append(m.legal(l.allTilesMask).size()).append(',').append(initRisk).append(',').append(String.format(Locale.ROOT,"%.12f",m.probability(l.allTilesMask))).append(',').append(m.orders(l.allTilesMask)).append('\n');
    if(n==7||n==8||n==13){
      StringBuilder r=new StringBuilder("# J"+n+" exact trap reproduction\n\nCommit 245f9f59f01ca1f5a4226b14ab8e8c707bcaf896. Coordinates one-based; tile IDs zero-based.\n\nIdentity: `"+code+"`; pieces "+l.tiles.size()+"; base "+budget.base()+"; margin "+budget.mistakeAllowance()+"; initial gates "+l.initialGateMask+".\n\nInitial legal IDs "+m.legal(l.allTilesMask)+"; safe "+safe(m,l.allTilesMask)+"; doomed "+risky(m,l.allTilesMask)+".\n\n|ID|Row|Column|Direction|SwitchMask|\n|---:|---:|---:|---|---:|\n");
      for(Tile t:l.tiles)r.append('|').append(t.id).append('|').append(t.row+1).append('|').append(t.col+1).append('|').append(t.direction).append('|').append(t.switchMask).append("|\n");
      r.append("\nFull completion: ").append(coords(l,l.solution)).append(".\n\n");
      for(long trap:new long[]{firstDead,firstHidden}){if(trap<0)continue;int[] path=m.paths.get(trap);Board b=new Board(l);b.restoreHistory(path);Board.Guidance g=b.guidance(budget.base()-path.length);r.append("Trap: ").append(coords(l,path)).append("\n\nRemainingMask ").append(trap).append("; gates ").append(m.gates(trap)).append("; legal remaining ").append(m.legal(trap)).append("; remaining actions ").append(g.remainingActions).append("; remaining pieces ").append(g.remainingPieces).append("; undos to recovery ").append(g.undoSteps).append("; recovery+finish actions ").append(g.actionsToRecoverAndFinish).append("; fits ").append(g.recoveryFitsBudget).append(".\n\n");if(path.length>0){long parent=trap|(1L<<path[path.length-1]);r.append("Before fatal choice: gates ").append(m.gates(parent)).append("; legal ").append(m.legal(parent)).append("; safe ").append(safe(m,parent)).append("; risky ").append(risky(m,parent)).append(".\n\n");}}
      Files.writeString(out.resolve("J"+n+"-reproduction.md"),r);Files.writeString(out.resolve("J"+n+"-states.csv"),states);
    }
    System.out.println("J"+n+": states="+m.paths.size()+" dead="+dead+" hidden="+deadLegal+" doomEdges="+doomEdges+" firstDoom="+depth+" initial="+m.legal(l.allTilesMask)+" risky="+risky(m,l.allTilesMask)+" uniformWin="+m.probability(l.allTilesMask));
  }
  static void summary()throws Exception {
    String[] rows=csv.toString().split("\n"),header=rows[0].split(",");Map<String,Integer> col=new HashMap<>();for(int i=0;i<header.length;i++)col.put(header[i],i);
    StringBuilder json=new StringBuilder("{\n  \"baseline\": \"245f9f59f01ca1f5a4226b14ab8e8c707bcaf896\",\n  \"scope\": \"diagnosis and proposed next-release mechanic only; not applied to 1.7\"");
    for(String key:new String[]{"reachable_states","losing_states","losing_with_legal","legal_edges","doom_edges_from_winning","mixed_winning_states"}){long sum=0;for(int r=1;r<rows.length;r++)sum+=Long.parseLong(rows[r].split(",")[col.get(key)]);json.append(",\n  \"").append(key).append("\": ").append(sum);}
    for(String key:new String[]{"levels_with_dead_end","levels_with_doomed_first_exit","trap_free_levels","levels_with_hidden_dead_end"}){ArrayList<Integer> levels=new ArrayList<>();for(int r=1;r<rows.length;r++){String[]f=rows[r].split(",");boolean include=key.equals("levels_with_doomed_first_exit")?Integer.parseInt(f[col.get("initial_risky")])>0:key.equals("levels_with_hidden_dead_end")?Integer.parseInt(f[col.get("losing_with_legal")])>0:key.equals("trap_free_levels")?Integer.parseInt(f[col.get("losing_states")])==0:Integer.parseInt(f[col.get("losing_states")])>0;if(include)levels.add(Integer.parseInt(f[0]));}json.append(",\n  \"").append(key).append("\": ").append(levels);}
    json.append("\n}\n");Files.writeString(out.resolve("summary.json"),json);
  }
  public static void main(String[]args)throws Exception{out=Path.of(args[0]);for(int n=1;n<=30;n++)one(n);Files.writeString(out.resolve("J1-30-metrics.csv"),csv);summary();}
}
