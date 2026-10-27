import com.androidhit.lumadrift.engine.*;
import java.util.*;

/** Independent exact model for offline authoring, not a model of human players. */
final class ProgressionAnalysis {
    final Level level;
    final long[] blockers;
    final double[] probabilities;
    final double probability;
    int decisions, unsafe, choices, forced, initialLegal, firstTrap=-1, states, deadStates;

    ProgressionAnalysis(Level level) {
        this.level=level;
        int count=level.tiles.size();
        if(count>22)throw new IllegalArgumentException("Offline array analysis limited to 22 pieces");
        blockers=new long[count];
        probabilities=new double[1<<count];Arrays.fill(probabilities,-1);probabilities[0]=1;
        for(Tile tile:level.tiles) {
            for(Tile other:level.tiles)if(other.id!=tile.id&&onRay(tile,other.row,other.col))blockers[tile.id]|=1L<<other.id;
            for(Barrier barrier:level.barriers)if(onRay(tile,barrier.row,barrier.col))blockers[tile.id]|=1L<<barrier.keyTileId;
        }
        probability=p(level.allTilesMask,level.initialGateMask);
        long mask=level.allTilesMask;int gates=level.initialGateMask,step=0;
        for(int next:level.solution) {
            int legal=0,bad=0;
            for(Tile tile:level.tiles)if(can(mask,gates,tile)) {
                legal++;if(p(mask&~(1L<<tile.id),gates^tile.switchMask)==0)bad++;
            }
            if(step==0)initialLegal=legal;
            if(legal==1)forced++;
            if(bad>0&&bad<legal){decisions++;if(firstTrap<0)firstTrap=step;}
            unsafe+=bad;choices+=legal;
            if(!can(mask,gates,level.tile(next)))throw new AssertionError("Invalid certified solution "+level.number+" / "+next);
            mask&=~(1L<<next);gates^=level.tile(next).switchMask;step++;
        }
        if(mask!=0||probability==0)throw new AssertionError("Unsolvable content");
    }

    static boolean onRay(Tile tile,int row,int col) {
        return tile.direction.dr==0?row==tile.row&&(col-tile.col)*tile.direction.dc>0
                :col==tile.col&&(row-tile.row)*tile.direction.dr>0;
    }
    boolean can(long mask,int gates,Tile tile) {
        return(mask&(1L<<tile.id))!=0&&(gates&tile.direction.bit)!=0&&(mask&blockers[tile.id])==0;
    }
    double p(long mask,int gates) {
        int index=(int)mask;if(probabilities[index]>=0)return probabilities[index];
        double sum=0;int legal=0;
        for(Tile tile:level.tiles)if(can(mask,gates,tile)) {
            legal++;sum+=p(mask&~(1L<<tile.id),gates^tile.switchMask);
        }
        states++;if(legal==0)deadStates++;
        return probabilities[index]=legal==0?0:sum/legal;
    }
    int blindWins(int allowance,int trials,long seed) {
        int wins=0;
        for(int i=0;i<trials;i++) {
            Random random=new Random(seed^i);long mask=level.allTilesMask;int gates=level.initialGateMask;
            for(int action=0;action<level.tiles.size()+allowance&&mask!=0;action++) {
                int nth=random.nextInt(Long.bitCount(mask));Tile selected=null;
                for(Tile tile:level.tiles)if((mask&(1L<<tile.id))!=0&&nth--==0){selected=tile;break;}
                if(can(mask,gates,selected)){mask&=~(1L<<selected.id);gates^=selected.switchMask;}
            }
            if(mask==0)wins++;
        }
        return wins;
    }
    static int direction(int value,int transform) {
        int reflected=transform>=4&&(value==1||value==3)?4-value:value;
        return(reflected+transform%4)%4;
    }
    static int gateMask(int mask,int transform) {
        int out=0;for(int d=0;d<4;d++)if((mask&(1<<d))!=0)out|=1<<direction(d,transform);return out;
    }
    static int[] point(int row,int col,int size,int transform) {
        if(transform>=4)col=size-1-col;
        for(int i=0;i<transform%4;i++){int next=col;col=size-1-row;row=next;}
        return new int[]{row,col};
    }
    /** D4, translations, reordered IDs; key-to-seal associations are preserved. */
    static String canonical(Level level) {
        String best=null;
        for(int transform=0;transform<8;transform++) {
            int[][] xy=new int[level.tiles.size()][2];
            int minRow=Integer.MAX_VALUE,minCol=Integer.MAX_VALUE;
            for(Tile tile:level.tiles){xy[tile.id]=point(tile.row,tile.col,level.size,transform);minRow=Math.min(minRow,xy[tile.id][0]);minCol=Math.min(minCol,xy[tile.id][1]);}
            for(Barrier barrier:level.barriers){int[] at=point(barrier.row,barrier.col,level.size,transform);minRow=Math.min(minRow,at[0]);minCol=Math.min(minCol,at[1]);}
            ArrayList<String> tiles=new ArrayList<>(),walls=new ArrayList<>();
            String[] ids=new String[level.tiles.size()];
            for(Tile tile:level.tiles){ids[tile.id]=(xy[tile.id][0]-minRow)+","+(xy[tile.id][1]-minCol)+","+direction(tile.direction.ordinal(),transform)+","+gateMask(tile.switchMask,transform);tiles.add(ids[tile.id]);}
            for(Barrier barrier:level.barriers){int[] at=point(barrier.row,barrier.col,level.size,transform);walls.add((at[0]-minRow)+","+(at[1]-minCol)+">"+ids[barrier.keyTileId]);}
            Collections.sort(tiles);Collections.sort(walls);
            String key=gateMask(level.initialGateMask,transform)+":"+String.join(";",tiles)+"#"+String.join(";",walls);
            if(best==null||key.compareTo(best)<0)best=key;
        }
        return best;
    }
}
