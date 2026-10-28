package com.androidhit.lumadrift.engine;

/** Exact exit-only planner for the frozen U route. One bounded cache per active board. */
public final class RelaySolver {
    public static final int MAX_PIECES = 16;
    private RelaySolver() { }

    public static final class Session {
        private final Level level;
        private final int[] obstruction;
        /* Each piece exits once; XOR gate state is uniquely determined by the remaining subset. */
        private final byte[] verdict;
        private int entries;
        public Session(Level level) {
            if(level==null||level.tiles.size()>MAX_PIECES)throw new IllegalArgumentException("Relay board supports at most 16 pieces");
            this.level=level;obstruction=new int[level.tiles.size()];verdict=new byte[1<<level.tiles.size()];verdict[0]=2;
            for(Tile tile:level.tiles){
                for(Tile other:level.tiles)if(other.id!=tile.id&&onRay(tile,other.row,other.col))obstruction[tile.id]|=1<<other.id;
                for(Barrier barrier:level.barriers)if(onRay(tile,barrier.row,barrier.col))obstruction[tile.id]|=1<<barrier.keyTileId;
            }
        }
        private static boolean onRay(Tile tile,int row,int col){return tile.direction.dr==0?row==tile.row&&(col-tile.col)*tile.direction.dc>0:col==tile.col&&(row-tile.row)*tile.direction.dr>0;}
        private void requireState(long mask,int gates){
            if((mask&~level.allTilesMask)!=0||(gates&~15)!=0)throw new IllegalArgumentException("State outside relay board");
            int expected=level.initialGateMask;for(Tile tile:level.tiles)if((mask&(1L<<tile.id))==0)expected^=tile.switchMask;
            if(expected!=gates)throw new IllegalArgumentException("Gate state does not match exited pieces");
        }
        private boolean search(int mask,int gates){
            if(verdict[mask]!=0)return verdict[mask]==2;
            for(int id:level.solution){Tile tile=level.tile(id);if((mask&(1<<id))==0||(mask&obstruction[id])!=0||(gates&tile.direction.bit)==0)continue;
                if(search(mask&~(1<<id),gates^tile.switchMask)){verdict[mask]=2;entries++;return true;}
            }
            verdict[mask]=1;entries++;return false;
        }
        public boolean hasSolution(long remaining,int gates){requireState(remaining,gates);return search((int)remaining,gates);}
        public int[] solve(long remaining,int gates){
            requireState(remaining,gates);int mask=(int)remaining;if(!search(mask,gates))return null;
            int[] result=new int[Integer.bitCount(mask)];int step=0;
            while(mask!=0){boolean found=false;for(int id:level.solution){Tile tile=level.tile(id);if((mask&(1<<id))==0||(mask&obstruction[id])!=0||(gates&tile.direction.bit)==0)continue;
                if(search(mask&~(1<<id),gates^tile.switchMask)){result[step++]=id;mask&=~(1<<id);gates^=tile.switchMask;found=true;break;}}
                if(!found)throw new IllegalStateException("Relay plan reconstruction failed");
            }return result;
        }
        public int cachedStates(){return entries;}
        public int cacheBytes(){return verdict.length+obstruction.length*Integer.BYTES;}
    }
}
