package com.androidhit.lumadrift.onboarding;

import com.androidhit.lumadrift.engine.Board;
import com.androidhit.lumadrift.engine.Direction;
import com.androidhit.lumadrift.engine.Level;
import com.androidhit.lumadrift.engine.Tile;
import java.util.Arrays;

/** Three real exits and one visible switch. No campaign, inventory or reward mutation. */
public final class FirstLight {
    private FirstLight() { }
    public static Board restore(String history) {
        Level level = new Level(1,0,4,Direction.UP.bit|Direction.DOWN.bit|Direction.LEFT.bit,
                Arrays.asList(new Tile(0,2,0,Direction.UP,Direction.RIGHT.bit),
                        new Tile(1,1,2,Direction.RIGHT,0),new Tile(2,3,1,Direction.DOWN,0)),
                new int[]{0,1,2},false,113L);
        Board board = new Board(level);
        if (history != null && history.matches("[0-2](?:,[0-2]){0,2}")) {
            String[] parts=history.split(",");int[] moves=new int[parts.length];
            for(int i=0;i<parts.length;i++)moves[i]=Integer.parseInt(parts[i]);
            board.restoreHistory(moves);
        }
        return board;
    }
    public static String history(Board board) {
        StringBuilder value=new StringBuilder();
        for(int id:board.history()){if(value.length()>0)value.append(',');value.append(id);}
        return value.toString();
    }
}
