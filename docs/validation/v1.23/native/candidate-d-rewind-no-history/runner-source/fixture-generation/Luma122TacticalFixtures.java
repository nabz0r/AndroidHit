import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
public class Luma122TacticalFixtures {
 public static void main(String[] args){StringBuilder o=new StringBuilder("{\"family\":\"T122\",\"layouts\":[");boolean first=true;for(TacticalChapterCatalog.Layout d:TacticalChapterCatalog.all()){if(!first)o.append(',');first=false;Level l=d.level();Board b=new Board(l);o.append("{\"id\":\"").append(d.id).append("\",\"size\":").append(l.size).append(",\"number\":").append(l.number).append(",\"certificate\":[");for(int i=0;i<l.solution.length;i++){Tile t=l.tile(l.solution[i]);if(i>0)o.append(',');if(!b.move(t.id).success)throw new AssertionError();o.append("{\"id\":").append(t.id).append(",\"row\":").append(t.row+1).append(",\"col\":").append(t.col+1).append('}');}if(!b.isSolved())throw new AssertionError();o.append("]}");}System.out.println(o.append("]}"));}
}
