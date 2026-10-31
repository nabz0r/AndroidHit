package com.androidhit.lumadrift.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Frozen, earned-play observatory prototype. Board references retain the certified U rules unchanged. */
public final class ExpeditionCatalog {
    public static final String VERSION="v118_1", DESTINATION="observatory", BOARD_FAMILY="U";
    public static final int STAGES=4, COPIES_REWARD=1, HOME_CONTRIBUTION=4;
    public static final String SOUVENIR_ID="observatory_star";
    public enum Encounter { ARROWS, RELAY, KEY }
    public static final class Option {
        public final String id,titleKey,descriptionKey;
        public final int stage,boardNumber,futureBoardNumber;
        public final Encounter encounter;
        public final boolean alternate,project;
        private Option(String id,int stage,int board,Encounter encounter,boolean alternate,boolean project,int future) {
            this.id=id;this.stage=stage;boardNumber=board;this.encounter=encounter;this.alternate=alternate;this.project=project;
            titleKey="exp_route_"+(project?"project":encounter==Encounter.KEY?"greenhouse":encounter==Encounter.RELAY?"observatory":"path");
            descriptionKey="exp_encounter_"+encounter.name().toLowerCase(java.util.Locale.ROOT);futureBoardNumber=future;
        }
    }
    private ExpeditionCatalog(){}
    private static int pick(long seed,int stage,int salt,int[] boards){long mixed=seed^(0x9E3779B97F4A7C15L*(stage+1))^(salt*0x632BE59BD9B4E019L);mixed^=mixed>>>33;return boards[Math.floorMod(mixed,boards.length)];}
    private static final int[] PATH={4,5,6,7,8};
    private static final int[] RELAY={13,14,15,16,17,18,19,20};
    private static final int[] KEY={9,10,11,12,21,22,23,24};
    private static final int[] FINALE={25,26,27,28,29,30};
    /** Every reference is small enough for an introductory four-board run and has an existing U certificate. */
    public static List<Integer> boardNumbers(){List<Integer> result=new ArrayList<>();for(int[] group:new int[][]{PATH,RELAY,KEY,FINALE})for(int value:group)result.add(value);return Collections.unmodifiableList(result);}
    private static int future(long seed,int stage,String previous){if(stage>=STAGES-1)return 0;if(stage==0)return pick(seed,1,1,RELAY);if(stage==1)return pick(seed,2,previous.contains("key")?6:1,previous.contains("key")?KEY:RELAY);return pick(seed,3,7,FINALE);}
    private static Option option(String id,int stage,long seed,int salt,int[] pool,Encounter encounter,boolean alternate,boolean project) {
        int board=pick(seed,stage,salt,pool);
        Encounter actual=(board==28||board==30)?Encounter.RELAY:encounter;
        return new Option(id,stage,board,actual,alternate,project,future(seed,stage,id));
    }
    /** priorOption is the chosen branch at the preceding stage; it changes the following real boards. */
    static List<Option> baseOptions(long seed,int stage,String priorOption){
        if(stage<0||stage>=STAGES)return Collections.emptyList();List<Option> result=new ArrayList<>();
        if(stage==0)result.add(option("entry",stage,seed,0,PATH,Encounter.ARROWS,false,false));
        else if(stage==3)result.add(option("finale",stage,seed,7,FINALE,Encounter.KEY,false,false));
        else {
            int offset=stage==2&&priorOption!=null&&priorOption.contains("key")?4:0;
            result.add(option("relay_"+stage,stage,seed,1+offset,RELAY,Encounter.RELAY,false,false));
            result.add(option("key_"+stage,stage,seed,2+offset,KEY,Encounter.KEY,false,false));
        }
        return result;
    }
    static Option replacement(Option old,long seed){
        int[] pool=old.encounter==Encounter.KEY?KEY:old.encounter==Encounter.RELAY?RELAY:PATH;
        int original=0;while(original<pool.length&&pool[original]!=old.boardNumber)original++;
        int board=pool[(original+1+Math.floorMod(seed,pool.length-1))%pool.length];
        return new Option(old.id+"_alt",old.stage,board,old.encounter,true,false,old.futureBoardNumber);
    }
    static Option project(long seed,int stage,ExpeditionRun.Goal goal,List<Option> existing){
        boolean copies=goal==ExpeditionRun.Goal.COPIES;int[] pool=copies?RELAY:KEY;
        Option offered=option("project_"+stage,stage,seed,9,pool,copies?Encounter.RELAY:Encounter.KEY,false,true);
        int index=0;while(index<pool.length&&pool[index]!=offered.boardNumber)index++;
        for(int attempt=0;attempt<pool.length;attempt++){
            int board=pool[(index+attempt)%pool.length];boolean conflict=false;for(Option other:existing)conflict|=other.boardNumber==board;
            if(!conflict)return new Option(offered.id,stage,board,offered.encounter,false,true,offered.futureBoardNumber);
        }
        throw new IllegalStateException("No distinct project board");
    }
}
