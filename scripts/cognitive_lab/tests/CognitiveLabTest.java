package cognitive_lab;

import android.content.Context;
import android.content.SharedPreferences;
import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;

/** Native-store parity and causal-boundary checks. No simulated result is a human claim. */
public final class CognitiveLabTest {
    static int checks;
    static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    static final class Prefs implements SharedPreferences {
        final Map<String,Object> data=new HashMap<>();
        public Map<String,?> getAll(){return new HashMap<>(data);}public boolean contains(String key){return data.containsKey(key);}
        public int getInt(String key,int fallback){return (Integer)data.getOrDefault(key,fallback);}public String getString(String key,String fallback){return (String)data.getOrDefault(key,fallback);}public boolean getBoolean(String key,boolean fallback){return (Boolean)data.getOrDefault(key,fallback);}
        public Editor edit(){return new Editor(){final Map<String,Object> writes=new HashMap<>();public Editor putInt(String k,int v){writes.put(k,v);return this;}public Editor putString(String k,String v){writes.put(k,v);return this;}public Editor putBoolean(String k,boolean v){writes.put(k,v);return this;}public boolean commit(){data.putAll(writes);return true;}};}
    }
    static final class Ctx extends Context {final Prefs prefs=new Prefs();public SharedPreferences getSharedPreferences(String name,int mode){return prefs;}}
    static Profile middle(){double[] u=new double[13];Arrays.fill(u,.55);return new Profile(0,u,100);}
    static void energyParity(){
        Environment.Energy ledger=new Environment.Energy();SessionEnergyStore nativeStore=new SessionEnergyStore(new Prefs());Random rng=new Random(114);long seconds=10000;int number=0;
        for(int event=0;event<5000;event++){seconds+=rng.nextInt(1300);int level=1+rng.nextInt(1000);ledger.capacity(EconomyCurve121.energyCapacity(level),seconds);nativeStore.setCapacity(EconomyCurve121.energyCapacity(level),seconds*1000);
            boolean expected=!SessionEnergyStore.costsEnergy(0,level);if(!expected){String id=String.format("00000000-0000-0000-0000-%012d",++number);expected=nativeStore.reserve(id,seconds*1000);if(expected)nativeStore.confirm(id,seconds*1000);}
            check(ledger.reserve(level,seconds)==expected,"actual SessionEnergyStore debit parity");check(ledger.count==nativeStore.snapshot(seconds*1000).count,"actual SessionEnergyStore refill/capacity parity");check(ledger.cap==nativeStore.maximum(),"actual capacity parity");}
        for(int level=1;level<=1000;level++)check((level>10)==SessionEnergyStore.costsEnergy(0,level),"all campaign energy eligibility");
        Environment.Energy full=new Environment.Energy();full.capacity(16,10000);check(full.count==10,"capacity growth gives no instant energy");full.refresh(10599);check(full.count==10,"no partial tick");full.refresh(10600);check(full.count==11,"first tick after capacity increase");
    }
    static void economyParity(){Ctx context=new Ctx();GameStore store=new GameStore(context);check(store.coins()==180,"actual GameStore fresh seed");int balance=180;for(int level=1;level<=1000;level++){int stars=1+level%3;int expected=EconomyCurve121.campaignReward(level,stars);check(store.completeLevel(level,stars,true)==expected,"actual GameStore first-clear entitlement parity");balance+=expected;check(store.coins()==balance,"actual GameStore award conservation");
            if(level%10==0){int paidIndex=1+(level%5);int cost=EconomyCurve121.undoPrice(level,paidIndex);String receipt="cognitive_test_"+level;check(store.reserveUndo121(receipt,level,paidIndex),"native paid undo reserve");balance-=cost;check(store.coins()==balance,"native paid undo debit parity");check(store.completeUndo121(receipt),"native paid undo completion");}}
    }
    static void mechanics(){
        for(int level=1;level<=1000;level++){Environment e=new Environment(level,1,0);check(e.board.level.progressionVersion==6,"current U semantics");check(e.budget.policyVersion()==6,"current policy6");check(e.shells.totalLayers()==(level<=10?0:5),"actual shell assignment");int illegal=-1;for(Tile piece:e.board.level.tiles)if(!e.board.canMove(piece.id)&&e.shells.layers(piece.id)==0){illegal=piece.id;break;}
            if(illegal>=0){long mask=e.board.remainingMask();int gates=e.board.gateMask();Board.MoveResult r=e.budget.attempt(e.board,illegal);check(!r.success&&e.budget.used()==0&&mask==e.board.remainingMask()&&gates==e.board.gateMask(),"blocked taps free and immutable");}
            int id=e.board.level.solution[0];while(e.shells.layers(id)>0){long mask=e.board.remainingMask();e.shells.tap(id);check(e.budget.used()==0&&e.board.remainingMask()==mask,"shell taps never consume exits");}
            check(e.budget.attempt(e.board,id).success,"test-only certified valid first exit");check(e.budget.used()==1,"productive exit charges1");check(e.budget.undo(e.board,0),"real undo succeeds");check(e.budget.used()==1&&e.board.moves()==0,"policy6 undo never refunds");check(e.shells.layers(id)==0,"shell stays broken through undo");}
        ExitChain121 chain=new ExitChain121();check(!chain.exit(0)&&!chain.exit(1)&&chain.exit(2),"3 distinct exits earn Focus");check(chain.use()&&!chain.use(),"Focus one use");chain.correction();chain.exit(0);check(!chain.ready(),"undo cannot farm Focus");
        Environment e=new Environment(1,1,0);CognitiveAgent.Observation first=e.observation(0,180);check(first.visibleHint>=0&&first.cueSource.equals("native_u1_coach"),"actual U1 coach rendered explicitly");
        Environment f=new Environment(46,1,0);for(int i=0;i<3;i++){int id=f.board.level.solution[i];while(f.shells.layers(id)>0)f.shells.tap(id);CognitiveAgent.Observation obs=f.observation(f.seconds,180);f.apply(new CognitiveAgent.Decision(id,0,"test_only_certificate",0,new ArrayList<>()),obs,f.seconds,1);}
        check(f.chain.ready(),"actual explicit Focus entitlement");CognitiveAgent.Observation obs=f.observation(f.seconds,180);Environment.Result result=f.apply(new CognitiveAgent.Decision(CognitiveAgent.FOCUS,0,"request_visible_help",0,new ArrayList<>()),obs,f.seconds,1);check(result.kind.equals("focus_visible"),"native earned Focus request");check(f.observation(f.seconds,180).cueSource.equals("native_earned_focus"),"Focus exposed as labeled cue");
    }
    static void policyBoundary(Path root)throws Exception {
        String policy=Files.readString(root.resolve("scripts/cognitive_lab/CognitiveAgent.java"));for(String forbidden:new String[]{"import com.androidhit",".solve(",".hint(",".guidance(",".minimumActions",".solution",".actionSolution",".isSafeMove("})check(!policy.contains(forbidden),"no solver/certificate import or call: "+forbidden);
        List<Profile> profiles=Profile.generate(100,20261030);Set<Double> labels=new HashSet<>();for(Profile p:profiles){labels.add(p.iq);check(p.planningDepth>=1&&p.planningDepth<=5&&p.workingMemory>=2&&p.searchCap<=160,"profile bounded dimensions");}check(labels.size()==100,"100 distinct fictional noncausal labels");check(Json.encode(profiles.get(0).json()).equals(Json.encode(Profile.generate(100,20261030).get(0).json())),"deterministic profile generation");
        double[] u=new double[13];Arrays.fill(u,.55);CognitiveAgent a=new CognitiveAgent(new Profile(0,u,70),"current_model"),b=new CognitiveAgent(new Profile(0,u,139),"current_model");Environment env=new Environment(46,1,0);CognitiveAgent.Observation obs=env.observation(0,180);
        for(int seed=0;seed<100;seed++){CognitiveAgent.Decision da=a.choose(obs,new Random(seed)),db=b.choose(obs,new Random(seed));check(Json.encode(da.json()).equals(Json.encode(db.json())),"hypothetical IQ label has zero causal influence");check(da.nodes<=a.profile.searchCap,"strict search node cap");}
        CognitiveAgent learning=new CognitiveAgent(middle(),"current_model"),frozen=new CognitiveAgent(middle(),"no_learning");double start=learning.meanKnowledge(),noStart=frozen.meanKnowledge();for(int n=0;n<20;n++){learning.learn(2,1);frozen.learn(2,1);}check(learning.meanKnowledge()>start,"rule learning responds to evidence");check(frozen.meanKnowledge()==noStart,"matched no-learning negative control");
        CognitiveAgent rested=new CognitiveAgent(middle(),"current_model"),tired=new CognitiveAgent(middle(),"current_model");tired.fatigue=1.5;check(CognitiveLab.continueUtility(middle(),rested,.5,.5,.5,"current_model")>CognitiveLab.continueUtility(middle(),tired,.5,.5,.5,"current_model"),"fatigue affects endogenous stopping");
        check(CognitiveLab.continueUtility(middle(),rested,.5,.5,1,"current_model")>CognitiveLab.continueUtility(middle(),rested,.5,.5,1,"reward_muted"),"reward salience affects explicit utility");
        CognitiveAgent knew=new CognitiveAgent(middle(),"current_model"),unknown=new CognitiveAgent(middle(),"current_model");Arrays.fill(knew.knowledge,1);Arrays.fill(unknown.knowledge,1);unknown.knowledge[4]=0;
        CognitiveAgent.Decision dk=knew.choose(obs,new Random(4)),du=unknown.choose(obs,new Random(4));check(!Json.encode(dk.candidates).equals(Json.encode(du.candidates)),"shell-rule knowledge affects perceived action burden");
        CognitiveLab.Config config=new CognitiveLab.Config();config.profiles=2;config.repetitions=2;Profile p=middle();CognitiveLab.Trajectory run1=CognitiveLab.run(config,p,0,"current_model",null,false),run2=CognitiveLab.run(config,p,0,"current_model",null,false);check(Json.encode(run1.json()).equals(Json.encode(run2.json())),"same profile/seed produces same trajectory");check(run1.coins==180+run1.coinsEarned-run1.coinsSpent,"full trajectory coin conservation");
    }
    public static void main(String[] args)throws Exception {Path root=Paths.get(args[0]);energyParity();economyParity();mechanics();policyBoundary(root);System.out.println(Json.encode(Json.obj("passed",true,"checks",checks,"scope","Actual native engine on1000 U boards;5000 native SessionEnergyStore parity events;1000 GameStore award parity checks; paid Undo, bounded policy/no-oracle boundary, independent noncausal fictional IQ labels, shell learning, fatigue/reward sensitivity and exact-seed trajectories. All behavioral outcomes synthetic.")));}
}
