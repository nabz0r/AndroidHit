package com.androidhit.lumadrift.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pure per-board companion contract. Ownership and certified board effects are checked by the caller. */
public final class CompanionBattle {
    public static final String VERSION="v119_1";
    public static final int MAX_TEAM=3, INITIAL_ENERGY=3, MAX_ENERGY=6;
    public enum Effect { GUIDE, VISION, TRAIL, PULSE, FLOW, REWIND }
    public static final class Definition {
        public final String id,signatureKey;
        public final Effect effect;
        public final int amount,durationMs,cost,color;
        private Definition(Catalog.CharacterItem character,Effect effect,int amount,int durationMs,int cost){
            id=character.id;signatureKey="battle_signature_"+id;this.effect=effect;this.amount=amount;
            this.durationMs=durationMs;this.cost=cost;color=character.color;
        }
    }
    private static final List<Definition> DEFINITIONS;
    static {
        List<Definition> definitions=new ArrayList<>();
        for(int n=0;n<Catalog.CHARACTERS.length;n++){
            Catalog.CharacterItem who=Catalog.CHARACTERS[n];Effect effect;int amount=1,duration=0,cost=2;
            if("nova".equals(who.id)){effect=Effect.GUIDE;duration=8000;}
            else if("miko".equals(who.id)){effect=Effect.TRAIL;amount=3;duration=8000;}
            else if("orbit".equals(who.id)){effect=Effect.FLOW;amount=2;cost=3;}
            else {
                effect=Effect.values()[(n-3)%Effect.values().length];
                switch(effect){
                    case GUIDE:duration=8000;break;
                    case VISION:duration=5000+(n%3)*1000;break;
                    case TRAIL:amount=2+n%3;duration=8000;cost=amount==4?3:2;break;
                    case FLOW:amount=2+n%2;cost=3;break;
                    case REWIND:amount=1+n%2;cost=amount==2?3:2;break;
                    default:break;
                }
            }
            definitions.add(new Definition(who,effect,amount,duration,cost));
        }
        DEFINITIONS=Collections.unmodifiableList(definitions);
    }
    private CompanionBattle(){}
    public static List<Definition> all(){return DEFINITIONS;}
    public static Definition find(String id){for(Definition definition:DEFINITIONS)if(definition.id.equals(id))return definition;return null;}

    /** Immutable run state; no clock, currency, ownership mutation or random value. */
    public static final class State {
        private final List<String> team,events;
        private final int energy,used;
        private final long credited;
        private final Effect lastFamily;
        private final boolean comboReady,comboSpent;
        private State(List<String> team,int energy,int used,long credited,Effect lastFamily,
                      boolean ready,boolean spent,List<String> events){
            this.team=Collections.unmodifiableList(new ArrayList<>(team));this.energy=energy;this.used=used;
            this.credited=credited;this.lastFamily=lastFamily;comboReady=ready;comboSpent=spent;
            this.events=Collections.unmodifiableList(new ArrayList<>(events));
        }
        /** Caller must filter these known IDs against actual ownership before starting or restoring a board. */
        public static State start(List<String> ids){
            if(ids==null||ids.isEmpty()||ids.size()>MAX_TEAM)return null;
            List<String> team=new ArrayList<>();
            for(String id:ids){if(find(id)==null||team.contains(id))return null;team.add(id);}
            return new State(team,INITIAL_ENERGY,0,0,null,false,false,Collections.emptyList());
        }
        public List<String> team(){return team;}
        public int energy(){return energy;}
        public boolean used(int slot){return slot>=0&&slot<team.size()&&(used&(1<<slot))!=0;}
        public boolean canUse(int slot){return slot>=0&&slot<team.size()&&!used(slot)&&energy>=find(team.get(slot)).cost;}
        public boolean creditedExit(int pieceId){return pieceId>=0&&pieceId<64&&(credited&(1L<<pieceId))!=0;}
        public Effect lastFamily(){return lastFamily;}
        public boolean comboReady(){return comboReady&&!comboSpent;}
        public boolean comboSpent(){return comboSpent;}
        private State next(int energy,int used,long credited,Effect family,boolean ready,boolean spent,String event){
            List<String> log=new ArrayList<>(events);log.add(event);
            return new State(team,energy,used,credited,family,ready,spent,log);
        }
        /** Apply only after the caller confirms a real, persistable board effect is available. */
        public State use(int slot){
            if(!canUse(slot))return null;Definition definition=find(team.get(slot));
            boolean ready=comboReady||!comboSpent&&lastFamily!=null&&lastFamily!=definition.effect;
            return next(energy-definition.cost,used|(1<<slot),credited,definition.effect,ready,comboSpent,"U"+slot);
        }
        /** Exact prior-state identity rejects stale UI requests without spending charges. */
        public State use(int slot,String expectedEncodedState){return encode().equals(expectedEncodedState)?use(slot):null;}
        /** Credit only a successfully exited tile. A manual gate (64..67) never earns energy. */
        public State creditExit(int pieceId){
            if(pieceId<0||pieceId>=64)return null;if(creditedExit(pieceId))return this;
            return next(Math.min(MAX_ENERGY,energy+1),used,credited|(1L<<pieceId),lastFamily,comboReady,comboSpent,"E"+pieceId);
        }
        /** One explicit certified exit, once per run. The caller checks effect feasibility before committing. */
        public State useCombo(){
            if(!comboReady())return null;
            return next(energy,used,credited,lastFamily,false,true,"C");
        }
        public State useCombo(String expectedEncodedState){return encode().equals(expectedEncodedState)?useCombo():null;}
        public String encode(){
            return VERSION+"|"+String.join(",",team)+"|"+energy+"|"+used+"|"+Long.toUnsignedString(credited,16)
                    +"|"+(lastFamily==null?"-":lastFamily.name())+"|"+(comboReady?"1":"0")+"|"+(comboSpent?"1":"0")+"|"+String.join(",",events);
        }
        /** Replay validates all claimed fields, energy caps, one-use limits and exact event spelling. */
        public static State decode(String encoded){
            if(encoded==null||encoded.length()>4096)return null;
            try{
                String[] parts=encoded.split("\\|",-1);
                if(parts.length!=9||!VERSION.equals(parts[0]))return null;
                List<String> ids=new ArrayList<>();Collections.addAll(ids,parts[1].split(",",-1));
                State run=start(ids);if(run==null)return null;
                if(!parts[8].isEmpty()){
                    String[] log=parts[8].split(",",-1);if(log.length>68)return null;
                    for(String event:log){
                        if("C".equals(event))run=run.useCombo();
                        else if(event.matches("U[0-2]"))run=run.use(event.charAt(1)-'0');
                        else if(event.matches("E(?:0|[1-9][0-9]?)")){
                            int piece=Integer.parseInt(event.substring(1));
                            if(run.creditedExit(piece))return null;run=run.creditExit(piece);
                        }else return null;
                        if(run==null)return null;
                    }
                }
                return encoded.equals(run.encode())?run:null;
            }catch(IllegalArgumentException invalid){return null;}
        }
    }
}
