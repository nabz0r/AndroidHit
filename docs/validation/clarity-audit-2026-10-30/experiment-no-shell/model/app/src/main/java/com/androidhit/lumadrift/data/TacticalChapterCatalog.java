package com.androidhit.lumadrift.data;

import com.androidhit.lumadrift.engine.Barrier;
import com.androidhit.lumadrift.engine.Direction;
import com.androidhit.lumadrift.engine.Level;
import com.androidhit.lumadrift.engine.Tile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Separate, hand-authored chapter family. No campaign geometry or U rule is replaced. */
public final class TacticalChapterCatalog {
    public static final String VERSION="t122_1", BOARD_FAMILY="T122";
    public static final int STAGES=4, DEFAULT_CHARGE=6, COINS_REWARD=120, COPIES_REWARD=0, HOME_CONTRIBUTION=0;
    public static final String REWARD_COMPANION_ID="nova";
    public enum Episode { KEYS, RELAYS }
    public enum Encounter { KEY, RELAY, MIXED }
    private static final List<String> LOAN_TEAM=Collections.unmodifiableList(Arrays.asList("nova","hoot","orbit"));

    public static final class Layout {
        public final String id;
        public final Episode episode;
        public final Encounter encounter;
        public final int index,stage,boardNumber;
        private final int size,gates;
        private final int[][] pieces,walls;
        private final int[] certificate;
        private final String fr,en;
        private Layout(int index,Episode episode,int stage,String suffix,Encounter encounter,String fr,String en,
                       int size,int gates,int[][] pieces,int[][] walls,int... certificate) {
            this.index=index;this.episode=episode;this.stage=stage;this.encounter=encounter;this.fr=fr;this.en=en;
            id="t122_"+(episode==Episode.KEYS?"keys_":"relays_")+suffix;boardNumber=900+index;
            this.size=size;this.gates=gates;this.pieces=pieces;this.walls=walls;this.certificate=certificate.clone();
        }
        public String title(boolean english){return english?en:fr;}
        /** New Level each time protects the catalog certificate from public legacy arrays. */
        public Level level(){
            List<Tile> tiles=new ArrayList<>();for(int i=0;i<pieces.length;i++){int[] p=pieces[i];tiles.add(new Tile(i,p[0],p[1],Direction.values()[p[2]],p[3]));}
            List<Barrier> barriers=new ArrayList<>();for(int[] wall:walls)barriers.add(new Barrier(wall[0],wall[1],wall[2]));
            return new Level(boardNumber,0,size,gates,tiles,certificate,false,0x543132320000L+index,barriers,6);
        }
        /** Number alone cannot distinguish this family's board from campaign U901. */
        public boolean matches(Level actual){
            if(actual==null)return false;Level expected=level();
            if(actual.number!=expected.number||actual.chapter!=0||actual.size!=expected.size||actual.initialGateMask!=expected.initialGateMask
                    ||actual.isEvent||actual.seed!=expected.seed||actual.progressionVersion!=6||actual.minimumActions!=expected.minimumActions
                    ||actual.tiles.size()!=expected.tiles.size()||actual.barriers.size()!=expected.barriers.size()
                    ||!Arrays.equals(actual.solution,expected.solution)||!Arrays.equals(actual.actionSolution,expected.actionSolution))return false;
            for(Tile tile:expected.tiles){Tile other=actual.tile(tile.id);if(other==null||other.row!=tile.row||other.col!=tile.col||other.direction!=tile.direction||other.switchMask!=tile.switchMask)return false;}
            for(int i=0;i<expected.barriers.size();i++){Barrier a=expected.barriers.get(i),b=actual.barriers.get(i);if(a.row!=b.row||a.col!=b.col||a.keyTileId!=b.keyTileId)return false;}
            return true;
        }
    }
    private static final int[][] NONE=new int[0][];
    private static final List<Layout> LAYOUTS;
    static {
        List<Layout> all=new ArrayList<>();
        // Each row: row, column, direction ordinal (U/R/D/L), XOR gate mask.
        all.add(new Layout(1,Episode.KEYS,0,"entry",Encounter.KEY,"La première serrure","The first lock",5,15,
                new int[][]{{4,0,2,0},{1,2,2,0},{1,4,1,0},{2,0,0,0},{4,4,1,0}},new int[][]{{3,2,0}},0,1,2,3,4));
        all.add(new Layout(2,Episode.KEYS,1,"first_key",Encounter.KEY,"Une clé, deux cadenas","One key, two locks",6,15,
                new int[][]{{5,0,2,0},{0,3,2,0},{5,5,1,0},{1,1,2,0},{2,5,0,0},{4,0,3,0},{0,5,1,0}},
                new int[][]{{2,3,0},{4,3,0},{3,1,2}},0,1,2,3,5,6,4));
        all.add(new Layout(3,Episode.KEYS,1,"first_relay",Encounter.RELAY,"Avant de fermer","Before closing",5,13,
                new int[][]{{4,0,3,2},{1,0,1,0},{3,3,0,4},{0,4,2,0},{4,4,1,0},{2,2,3,0}},NONE,0,1,4,3,5,2));
        all.add(new Layout(4,Episode.KEYS,2,"second_key",Encounter.MIXED,"Le double passage","The double passage",6,13,
                new int[][]{{5,0,3,2},{0,2,2,0},{2,0,1,0},{5,5,1,0},{1,5,0,0},{4,4,2,1},{0,0,0,0}},
                new int[][]{{3,2,0},{2,4,3}},0,3,1,6,4,2,5));
        all.add(new Layout(5,Episode.KEYS,2,"second_relay",Encounter.RELAY,"Le dernier relais","The last relay",6,12,
                new int[][]{{5,0,3,1},{0,0,0,2},{2,2,1,0},{0,5,2,0},{4,5,1,0},{5,4,3,4},{4,1,0,1}},NONE,0,1,2,4,3,6,5));
        all.add(new Layout(6,Episode.KEYS,3,"finale",Encounter.MIXED,"Le jardin rouvert","The garden reopened",7,13,
                new int[][]{{6,0,3,2},{0,6,0,4},{1,2,2,0},{6,6,1,4},{2,0,1,0},{3,3,0,2},{5,5,2,0},{4,6,1,0}},
                new int[][]{{4,2,0},{2,4,1}},0,2,1,3,4,6,7,5));
        all.add(new Layout(7,Episode.RELAYS,0,"entry",Encounter.RELAY,"Réveiller les portes","Waking the gates",5,12,
                new int[][]{{4,0,3,1},{0,2,0,2},{2,1,1,0},{1,4,2,0},{4,4,1,0},{3,0,0,4}},NONE,0,1,2,4,3,5));
        all.add(new Layout(8,Episode.RELAYS,1,"first_key",Encounter.MIXED,"Deux bonnes clés","Two right keys",6,15,
                new int[][]{{5,5,1,0},{0,2,2,0},{0,0,0,0},{2,0,1,0},{4,4,0,2},{5,0,3,0},{0,5,1,0}},
                new int[][]{{3,2,0},{2,3,2}},0,2,1,3,5,6,4));
        all.add(new Layout(9,Episode.RELAYS,1,"first_relay",Encounter.RELAY,"Garder une issue","Keep an exit",6,9,
                new int[][]{{0,0,0,2},{3,1,1,4},{0,4,2,0},{5,4,3,0},{4,5,0,1},{1,2,3,2},{5,1,2,0}},NONE,0,1,6,3,2,4,5));
        all.add(new Layout(10,Episode.RELAYS,2,"second_key",Encounter.MIXED,"Les cadenas jumeaux","The twin locks",7,11,
                new int[][]{{6,0,3,4},{0,2,2,0},{0,6,1,0},{1,4,2,0},{6,6,2,2},{2,0,0,1},{4,6,1,0},{5,3,3,0}},
                new int[][]{{3,2,0},{2,4,2},{4,4,2}},0,1,2,3,6,4,7,5));
        all.add(new Layout(11,Episode.RELAYS,2,"second_relay",Encounter.RELAY,"Deux portes à préserver","Two gates to preserve",7,5,
                new int[][]{{0,0,0,8},{6,0,3,2},{2,1,1,1},{0,5,0,0},{5,5,2,2},{3,6,1,0},{6,6,1,0},{1,3,2,8}},NONE,0,1,3,2,5,6,4,7));
        all.add(new Layout(12,Episode.RELAYS,3,"finale",Encounter.MIXED,"L'atelier des lueurs","The glow workshop",7,9,
                new int[][]{{0,0,0,2},{6,0,3,4},{1,2,2,0},{2,1,1,0},{0,6,0,4},{5,5,2,0},{6,6,1,1},{3,3,3,2},{4,0,3,0}},
                new int[][]{{4,2,0},{2,4,1}},0,1,2,3,5,4,6,8,7));
        // Constructing each Level linearly checks every certificate against the actual engine.
        for(Layout layout:all)try{layout.level();}catch(IllegalArgumentException invalid){throw new IllegalArgumentException("Invalid chapter layout "+layout.id,invalid);}
        LAYOUTS=Collections.unmodifiableList(all);
    }
    private TacticalChapterCatalog(){}
    public static List<Layout> all(){return LAYOUTS;}
    public static Layout find(String id){for(Layout layout:LAYOUTS)if(layout.id.equals(id))return layout;return null;}
    public static List<Layout> options(Episode episode,int stage){List<Layout> result=new ArrayList<>();if(episode!=null&&stage>=0&&stage<STAGES)for(Layout layout:LAYOUTS)if(layout.episode==episode&&layout.stage==stage)result.add(layout);return Collections.unmodifiableList(result);}
    public static List<String> loanTeam(){return LOAN_TEAM;}
    public static CompanionBattle.Definition talent(int slot){return slot>=0&&slot<LOAN_TEAM.size()?CompanionBattle.find(LOAN_TEAM.get(slot)):null;}
    public static String souvenirId(Episode episode){return episode==Episode.KEYS?"tactical_keys_lantern":episode==Episode.RELAYS?"tactical_relays_lantern":null;}
}
