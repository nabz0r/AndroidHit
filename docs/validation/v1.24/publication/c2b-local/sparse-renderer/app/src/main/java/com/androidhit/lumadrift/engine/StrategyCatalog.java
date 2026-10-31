package com.androidhit.lumadrift.engine;

import java.util.ArrayList;
import java.util.List;

/** Frozen L/M strategy route. Historical J/K lookup never points here. */
public final class StrategyCatalog {
    public static final int LEVEL_COUNT=1000,REMIX_COUNT=240;
    private static final Level[] JOURNEY=new Level[LEVEL_COUNT],REMIX=new Level[REMIX_COUNT];
    private StrategyCatalog() { }
    public static synchronized Level get(int number){return load(number,false);}
    public static synchronized Level remix(int number){return load(number,true);}
    private static String[] row(int number,boolean remix){if(number<1||number>(remix?REMIX_COUNT:LEVEL_COUNT))throw new IllegalArgumentException("V4 level range");return (remix?StrategyV4Layouts.REMIX:StrategyV4Layouts.JOURNEY)[number-1].split("\\|",-1);}
    public static int allowance(int number,boolean remix){return Integer.parseInt(row(number,remix)[2]);}
    private static Level load(int number,boolean remix){
        String[] row=row(number,remix);Level[] cache=remix?REMIX:JOURNEY;if(cache[number-1]!=null)return cache[number-1];
        int source=Integer.parseInt(row[0]);Level old=remix?BalancedCatalog.remix(source):BalancedCatalog.get(source);
        int switches=Integer.parseInt(row[4]),used=0;ArrayList<Tile> tiles=new ArrayList<>();
        String[] masks=row.length>7?row[7].split(","):null;
        for(Tile tile:old.tiles){int mask=tile.switchMask;if(mask!=0&&switches>=0&&used++>=switches)mask=0;if(masks!=null)mask=Integer.parseInt(masks[tile.id]);tiles.add(new Tile(tile.id,tile.row,tile.col,tile.direction,mask));}
        ArrayList<Barrier> walls=new ArrayList<>();if(!row[5].isEmpty())for(String encoded:row[5].split(";")){String[] v=encoded.split(",");walls.add(new Barrier(Integer.parseInt(v[0]),Integer.parseInt(v[1]),Integer.parseInt(v[2])));}
        String[] encoded=row[6].split(",");int[] plan=new int[encoded.length];for(int i=0;i<plan.length;i++)plan[i]=Integer.parseInt(encoded[i]);
        return cache[number-1]=new Level(number,(number-1)/30+1,old.size,Integer.parseInt(row[3]),tiles,old.solution,false,
                old.seed^0x56344C4D00000000L^number,walls,4,Integer.parseInt(row[1]),plan);
    }
    public static ProgressionInfo info(int number,boolean remix){
        Level level=remix?remix(number):get(number);String[] data=row(number,remix);
        ProgressionInfo.Difficulty difficulty=ProgressionInfo.Difficulty.values()[Integer.parseInt(data[8])];
        ProgressionInfo.Beat beat=ProgressionInfo.Beat.values()[Integer.parseInt(data[9])];
        return new ProgressionInfo(level,remix,Integer.parseInt(data[0]),allowance(number,remix),difficulty,beat,
                Integer.parseInt(data[10]),Integer.parseInt(data[11]),Integer.parseInt(data[12]),Integer.parseInt(data[13]),Integer.parseInt(data[14]));
    }
    /** Infinite seed space, finite audited geometry closure: deterministic selection among L21..L1000. */
    public static Level event(long seed){
        long mixed=seed^(seed>>>33);mixed*=0xff51afd7ed558ccdL;mixed^=mixed>>>33;
        int number=21+(int)Math.floorMod(mixed,980L);Level source=get(number);
        return new Level(0,0,source.size,source.initialGateMask,source.tiles,source.solution,true,seed,
                source.barriers,4,source.minimumActions,source.actionSolution);
    }
}
