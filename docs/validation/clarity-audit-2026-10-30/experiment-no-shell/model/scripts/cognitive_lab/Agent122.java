package cognitive_lab;

import java.util.*;

/** Policy sees visible projections and published cue content only. No native engine imports. */
final class Agent122 {
    static final int SKILL_BASE=-10,COMBO=-20;
    final CognitiveAgent base;final Set<String> refused=new HashSet<>();
    static final class Observation {
        final CognitiveAgent.Observation board;
        final int causeRule;final String causeSource;
        final long visionLegal;final int[] skillFamilies;final boolean[] skillsReady;final boolean comboReady;
        Observation(CognitiveAgent.Observation board,int rule,String source,long legal,int[] families,boolean[] ready,boolean combo){this.board=board;causeRule=rule;causeSource=source;visionLegal=legal;skillFamilies=families.clone();skillsReady=ready.clone();comboReady=combo;}
    }
    Agent122(Profile profile){base=new CognitiveAgent(profile,"current_model");}
    CognitiveAgent.Decision choose(Observation o,Random rng){return choose(o,rng,true);}
    CognitiveAgent.Decision choose(Observation o,Random rng,boolean permitSkills){
        // Skill timing is a declared policy, not an estimate of hidden solution safety.
        boolean pressure=base.consecutiveBlocked>0||o.board.deadEndWarning||Long.bitCount(o.board.present)>8;
        if(permitSkills&&o.comboReady&&!refused.contains(key(o,COMBO))&&pressure&&rng.nextDouble()>.25)return activeDecision(COMBO,"request_visible_resonance");
        if(permitSkills&&(pressure||base.decisions>0&&base.decisions%6==0)){
            List<Integer> slots=new ArrayList<>();for(int s=0;s<o.skillsReady.length;s++)if(o.skillsReady[s]&&!refused.contains(key(o,SKILL_BASE-s)))slots.add(s);
            Collections.shuffle(slots,rng);for(int slot:slots){int family=o.skillFamilies[slot];
                // REWIND is chosen only in response to the visible warning or remembered errors.
                if(family==5&&!o.board.deadEndWarning&&base.consecutiveBlocked==0)continue;
                if(rng.nextDouble()<.65+.25*base.profile.explorationInterest)return activeDecision(SKILL_BASE-slot,"request_visible_skill_family_"+family);
            }
        }
        if(o.visionLegal!=0&&rng.nextDouble()>base.profile.attentionLapse){
            List<Integer> visible=new ArrayList<>();for(int i=0;i<64;i++)if((o.visionLegal&(1L<<i))!=0)visible.add(i);
            if(!visible.isEmpty())return activeDecision(visible.get(rng.nextInt(visible.size())),"follow_visible_vision_legal_piece");
        }
        // Recognition changes only while the actual causal explanation is exposed.
        // It supplies a visible rule, not a next move or a better permanent starting state.
        double remembered=0;if(o.causeRule>=0){remembered=base.knowledge[o.causeRule];base.knowledge[o.causeRule]=1;}
        CognitiveAgent.Decision result=base.choose(o.board,rng);
        if(o.causeRule>=0)base.knowledge[o.causeRule]=remembered;
        return result;
    }
    CognitiveAgent.Decision activeDecision(int action,String reason){base.decisions++;return decision(action,reason);}
    static String key(Observation o,int action){return o.board.present+":"+o.board.gates+":"+action;}
    void feedback(Observation o,CognitiveAgent.Decision d,String kind,String reason,double seconds){if(kind.equals("skill_unavailable"))refused.add(key(o,d.action));base.feedback(o.board,d.action,kind,reason,seconds);}
    static CognitiveAgent.Decision decision(int action,String reason){return new CognitiveAgent.Decision(action,0,reason,0,new ArrayList<>());}
}
