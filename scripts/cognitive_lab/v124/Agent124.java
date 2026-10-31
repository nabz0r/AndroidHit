package cognitive_lab;

import java.util.Random;

/** Same bounded policy, with negative episodic memories scoped to visible board identity. */
final class Agent124 {
    final Profile profile;
    Agent122 delegate;
    CognitiveAgent base;
    private String scope;
    Agent124(Profile profile){this.profile=profile;delegate=new Agent122(profile);base=delegate.base;}

    /** Uses only the public identity and structured visible geometry, never a hidden continuation. */
    void enterBoard(String identity,CognitiveAgent.Piece[] pieces,CognitiveAgent.Wall[] walls){
        if(identity==null||identity.isEmpty()||pieces==null||walls==null)throw new IllegalArgumentException("Visible board identity required");
        StringBuilder key=new StringBuilder(identity).append('|');
        for(CognitiveAgent.Piece p:pieces)key.append(p.id).append(',').append(p.row).append(',').append(p.col).append(',').append(p.direction).append(',').append(p.switchMask).append(';');
        key.append('|');for(CognitiveAgent.Wall wall:walls)key.append(wall.row).append(',').append(wall.col).append(',').append(wall.key).append(';');
        String nextScope=key.toString();if(nextScope.equals(scope))return;
        CognitiveAgent previous=base;delegate=new Agent122(profile);base=delegate.base;
        System.arraycopy(previous.knowledge,0,base.knowledge,0,previous.knowledge.length);
        base.fatigue=previous.fatigue;base.frustration=previous.frustration;
        base.decisions=previous.decisions;base.totalNodes=previous.totalNodes;
        // Block sequences, rejected skills and private tile memories belong to
        // the old geometry. Learned rules and accumulated state remain transferable.
        scope=nextScope;
    }
    CognitiveAgent.Decision choose(Agent122.Observation observation,Random random){return delegate.choose(observation,random);}
    CognitiveAgent.Decision choose(Agent122.Observation observation,Random random,boolean permitSkills){return delegate.choose(observation,random,permitSkills);}
    void feedback(Agent122.Observation observation,CognitiveAgent.Decision decision,String kind,String reason,double seconds){delegate.feedback(observation,decision,kind,reason,seconds);}
}
