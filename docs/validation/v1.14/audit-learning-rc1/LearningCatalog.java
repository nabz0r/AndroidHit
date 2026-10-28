package com.androidhit.lumadrift.engine;

import java.util.ArrayList;
import java.util.Arrays;

/** Separately identified N Journey. Historical L/M/T content and saves stay independent. */
public final class LearningCatalog {
    public static final int LEVEL_COUNT=1000;
    private static final Level[] LEVELS=new Level[LEVEL_COUNT];
    private LearningCatalog(){}
    private static String[] row(int number){if(number<1||number>LEVEL_COUNT)throw new IllegalArgumentException("N Journey level 1..1000 required");return LearningLayouts.JOURNEY[number-1].split("\\|",-1);}
    public static synchronized Level get(int number){String[] r=row(number);if(LEVELS[number-1]!=null)return LEVELS[number-1];ArrayList<Tile> tiles=new ArrayList<>();for(String item:r[4].split(";")){String[] t=item.split(",");tiles.add(new Tile(tiles.size(),Integer.parseInt(t[0]),Integer.parseInt(t[1]),Direction.values()[Integer.parseInt(t[2])],Integer.parseInt(t[3])));}ArrayList<Barrier> walls=new ArrayList<>();if(!r[5].isEmpty())for(String item:r[5].split(";")){String[] b=item.split(",");walls.add(new Barrier(Integer.parseInt(b[0]),Integer.parseInt(b[1]),Integer.parseInt(b[2])));}int[] actions=Arrays.stream(r[6].split(",")).mapToInt(Integer::parseInt).toArray();int[] exits=Arrays.stream(actions).filter(a->!Board.isGateAction(a)).toArray();return LEVELS[number-1]=new Level(number,(number-1)/30+1,Integer.parseInt(r[0]),Integer.parseInt(r[1]),tiles,exits,false,0x4e31313400000000L+number,walls,5,Integer.parseInt(r[2]),actions);}
    public static int allowance(int number){return Integer.parseInt(row(number)[3]);}
    public static int allowance(int number,boolean remix){if(remix)throw new IllegalArgumentException("N is Journey only");return allowance(number);}
    public static String lesson(int number){return row(number)[14];}
    public static int sourceNumber(int number){return Integer.parseInt(row(number)[15]);}
    public static ProgressionInfo info(int number){String[] r=row(number);return new ProgressionInfo(get(number),false,sourceNumber(number),allowance(number),ProgressionInfo.Difficulty.values()[Integer.parseInt(r[12])],ProgressionInfo.Beat.values()[Integer.parseInt(r[13])],Integer.parseInt(r[7]),Integer.parseInt(r[8]),Integer.parseInt(r[9]),Integer.parseInt(r[10]),Integer.parseInt(r[11]));}
    public static ProgressionInfo info(int number,boolean remix){if(remix)throw new IllegalArgumentException("N is Journey only");return info(number);}
}
