import com.androidhit.lumadrift.engine.*;
/** Prints a reproducible legal solution for emulator interaction tests. */
public final class EngineFixture {
    public static void main(String[] args) {
        Level level=args[0].equals("event")?LevelCatalog.event(Long.parseLong(args[1])):LevelCatalog.get(Integer.parseInt(args[0]));
        System.out.print("{\"size\":"+level.size+",\"solution\":[");
        for(int i=0;i<level.solution.length;i++){Tile t=level.tile(level.solution[i]);if(i>0)System.out.print(",");System.out.print("["+t.row+","+t.col+"]");}
        System.out.println("]}");
    }
}
