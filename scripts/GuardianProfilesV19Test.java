import android.content.SharedPreferences;
import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;

/** Optional-mode complement, separate from the paired first-100 Journey audit. */
public final class GuardianProfilesV19Test {
    static long checks,scheduled,attempts,cleared,restarts,blocked,blockedCellwise,peels,steps,exits,undos,zeroBudgetUndos,hints,restores,mandatoryRestores,repeatedIdRestores,journeyBoards,rewardCoins,forcedRecoveryEdges;
    static final StringBuilder moves=new StringBuilder("profile,sensitivity,seed,guardian,attempt,event,piece,success,reason,row_before,col_before,layers_before,row_after,col_after,layers_after,present_after,work_before,work_after,used_before,used_after,remaining_actions,history\n");
    static final StringBuilder encounters=new StringBuilder("profile,sensitivity,seed,guardian,attempt,solved,minimum,budget,used,blocked,blocked_cellwise,peels,steps,exits,undo,zero_budget_undo,free_hints,cold_restores,earned_coins,remaining_work\n");
    static final StringBuilder totals=new StringBuilder("profile,sensitivity,seed,scheduled_guardians,solved_guardians,attempts,restarts,blocked,blocked_cellwise,peels,steps,exits,undo,zero_budget_undo,free_hints,cold_restores,journey_boards_solved,coins,guardian_reward_coins,anchors\n");
    static final StringBuilder interruptions=new StringBuilder("profile,sensitivity,seed,guardian,attempt,checkpoint,used,work,history_length,distinct_piece_ids,canonical_save\n");
    static final StringBuilder recoveryEdges=new StringBuilder("profile,guardian,productive_prefix,blocked_until_zero,used_before_undo,work_before_undo,remaining_actions_after_undo,work_after_undo,canonical_restore_exact,free_restart_solved\n");
    static void check(boolean ok,String reason){checks++;if(!ok)throw new AssertionError(reason);}
    static final class Profile {
        String id,anchors;double offPlan,probe,sessionMin,sessionMax,latencyMin,latencyMax;int helpAfter,retries;
        Profile(String[] v){id=v[0];offPlan=Double.parseDouble(v[1]);probe=Double.parseDouble(v[2]);helpAfter=Integer.parseInt(v[3]);sessionMin=Double.parseDouble(v[4]);sessionMax=Double.parseDouble(v[5]);latencyMin=Double.parseDouble(v[6]);latencyMax=Double.parseDouble(v[7]);retries=Integer.parseInt(v[8]);anchors=v[10];}
    }
    static final class Stats {int attempts,restarts,blocked,blockedCellwise,peels,steps,exits,undo,zeroUndo,hints,restores,used;boolean solved;}
    static final class Session {
        RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();GameStore store=new GameStore(context,null,()->1793059200000L);
        GuardianEncounter encounter;
        int globalActions;
    }
    static List<Integer> legal(GuardianEncounter encounter){List<Integer> ids=new ArrayList<>();for(GuardianEncounter.Piece p:encounter.snapshot().pieces)if(p.present&&encounter.canMove(p.id))ids.add(p.id);return ids;}
    static List<Integer> blocked(GuardianEncounter encounter){List<Integer> ids=new ArrayList<>();for(GuardianEncounter.Piece p:encounter.snapshot().pieces)if(p.present&&!encounter.canMove(p.id))ids.add(p.id);return ids;}
    static String history(GuardianEncounter encounter){StringJoiner ids=new StringJoiner(";");for(int id:encounter.history())ids.add(Integer.toString(id));return ids.toString();}
    static void same(GuardianEncounter.Snapshot before,GuardianEncounter.Snapshot after){
        check(before.number==after.number&&before.size==after.size&&before.totalWork==after.totalWork&&before.remainingWork==after.remainingWork&&before.used==after.used&&before.remainingActions==after.remainingActions&&before.solved==after.solved,"cold restore counters exact");
        for(int i=0;i<before.pieces.size();i++){GuardianEncounter.Piece a=before.pieces.get(i),b=after.pieces.get(i);check(a.id==b.id&&a.row==b.row&&a.col==b.col&&a.layers==b.layers&&a.present==b.present&&a.cellwise==b.cellwise&&a.direction==b.direction,"cold restore repeated-hit/step coordinates exact");}
    }
    static void checkpoint(Session session,Profile profile,double sensitivity,long seed,int guardian,int attempt,String kind){
        GuardianEncounter.Snapshot before=session.encounter.snapshot();String save=session.encounter.encode();int coins=session.store.coins(),clears=session.store.completedLevels(),hintsBefore=session.store.boosterStock(Catalog.HINT);
        SharedPreferences prefs=session.context.getSharedPreferences("guardian_profile_test_session",0);check(prefs.edit().putString("state",save).commit(),"test adapter saves canonical encounter record");
        session.context=session.context.reboot();session.store=new GameStore(session.context,null,()->1793059200000L);
        String fromDisk=session.context.getSharedPreferences("guardian_profile_test_session",0).getString("state","");session.encounter=GuardianEncounter.restore(fromDisk);
        check(session.encounter!=null&&session.encounter.encode().equals(save),"cold replay accepts exact bounded save");same(before,session.encounter.snapshot());
        check(session.store.coins()==coins&&session.store.completedLevels()==clears&&session.store.boosterStock(Catalog.HINT)==hintsBefore,"cold replay does not duplicate progress or rewards");
        Set<Integer> distinct=new HashSet<>();for(int id:session.encounter.history())distinct.add(id);if(distinct.size()<session.encounter.history().length)repeatedIdRestores++;
        restores++;if(kind.equals("mandatory_after_two_productive_hits"))mandatoryRestores++;
        interruptions.append(profile.id).append(',').append(sensitivity).append(',').append(seed).append(',').append(guardian).append(',').append(attempt).append(',').append(kind).append(',').append(before.used).append(',').append(before.remainingWork).append(',').append(session.encounter.history().length).append(',').append(distinct.size()).append(',').append(save.replace(',',';')).append('\n');
    }
    static void record(Profile profile,double sensitivity,long seed,int guardian,int attempt,String event,int id,GuardianEncounter.Snapshot before,GuardianEncounter after,GuardianEncounter.MoveResult result){
        GuardianEncounter.Piece a=id<0?null:before.pieces.get(id),b=id<0?null:after.piece(id);
        moves.append(profile.id).append(',').append(sensitivity).append(',').append(seed).append(',').append(guardian).append(',').append(attempt).append(',').append(event).append(',').append(id).append(',').append(result==null?true:result.success).append(',').append(result==null?"NONE":result.reason).append(',').append(a==null?-1:a.row).append(',').append(a==null?-1:a.col).append(',').append(a==null?0:a.layers).append(',').append(b==null?-1:b.row).append(',').append(b==null?-1:b.col).append(',').append(b==null?0:b.layers).append(',').append(b!=null&&b.present).append(',').append(before.remainingWork).append(',').append(after.remainingWork()).append(',').append(before.used).append(',').append(after.snapshot().used).append(',').append(after.remainingActions()).append(',').append(history(after)).append('\n');
    }
    static Stats encounter(Session session,Profile profile,double sensitivity,long seed,int guardian,Random rng){
        Stats total=new Stats();boolean learnedHelp=false;int actionWindow=Math.max(1,(int)(((profile.sessionMin+profile.sessionMax)/2)*60/((profile.latencyMin+profile.latencyMax)/2)));
        check(session.store.guardianUnlocked(guardian)&&!session.store.guardianCleared(guardian),"real Journey records unlock only this available encounter");
        for(int attempt=0;attempt<=profile.retries;attempt++){
            session.encounter=GuardianEncounter.start(guardian);Stats one=new Stats();one.attempts=1;total.attempts++;attempts++;if(attempt>0){total.restarts++;restarts++;}
            // Explicit recovery experiment: after a failed attempt, follow free visible guidance.
            boolean assisted=learnedHelp;int bad=0,productive=0,guard=session.encounter.definition.baseBudget*5+20;boolean checkpointed=false;
            if(assisted){one.hints++;hints++;}
            while(session.encounter.canAct()&&guard-->0){
                GuardianEncounter.Snapshot before=session.encounter.snapshot();int[] plan=session.encounter.optimalPlan();check(plan.length==session.encounter.remainingWork()&&plan.length>0,"free hint is a real minimum completion from current state");
                int id=plan[0];
                if(!assisted&&rng.nextDouble()<Math.min(.9,profile.offPlan*sensitivity)){List<Integer> choices=legal(session.encounter);check(!choices.isEmpty(),"reachable encounter has a legal productive action");id=choices.get(rng.nextInt(choices.size()));}
                if(!assisted&&rng.nextDouble()<Math.min(.9,profile.probe*sensitivity)){
                    List<Integer> probes=blocked(session.encounter);
                    if(probes.isEmpty()){probes=new ArrayList<>();for(GuardianEncounter.Piece p:before.pieces)if(p.present)probes.add(p.id);}
                    id=probes.get(rng.nextInt(probes.size()));
                }
                GuardianEncounter.Piece piece=before.pieces.get(id);boolean expectedLegal=session.encounter.canMove(id);GuardianEncounter.MoveResult move=session.encounter.move(id);session.globalActions++;
                check(move.success==expectedLegal&&move.remainingActions==before.remainingActions-1,"real selected present piece costs exactly one action");
                String event;
                if(move.blocked){bad++;one.blocked++;blocked++;if(piece.cellwise){one.blockedCellwise++;blockedCellwise++;}event="BLOCK";check(move.remainingWork==before.remainingWork,"blocked step/exit preserves real work");}
                else {productive++;check(move.success&&move.remainingWork==before.remainingWork-1,"successful hit or step clears one real work unit");
                    GuardianEncounter.Piece after=session.encounter.piece(id);
                    if(piece.layers>1){one.peels++;peels++;event="PEEL";check(move.peeled&&!move.stepped&&!move.removed&&after.layers==piece.layers-1&&after.row==piece.row&&after.col==piece.col,"first hit peels, never silently moves a thick piece");}
                    else if(move.stepped){one.steps++;steps++;event="STEP";check(piece.cellwise&&Math.abs(after.row-piece.row)+Math.abs(after.col-piece.col)==1&&after.present,"footprint piece advances exactly one cell");}
                    else {one.exits++;exits++;event="EXIT";check(move.removed&&!after.present,"exit actually removes piece");}
                }
                record(profile,sensitivity,seed,guardian,attempt,event,id,before,session.encounter,move);
                if(!assisted&&bad>=profile.helpAfter){assisted=true;learnedHelp=true;one.hints++;hints++;}
                if(!checkpointed&&productive>=2){checkpoint(session,profile,sensitivity,seed,guardian,attempt,"mandatory_after_two_productive_hits");checkpointed=true;one.restores++;}
                else if(session.globalActions%actionWindow==0){checkpoint(session,profile,sensitivity,seed,guardian,attempt,"modelled_session_window");one.restores++;}
                if(assisted&&session.encounter.remainingActions()<session.encounter.remainingWork())break;
            }
            check(guard>0,"optional encounter action loop is bounded");
            if(!session.encounter.isSolved()&&session.encounter.canUndo()&&session.encounter.remainingActions()<session.encounter.remainingWork()){
                GuardianEncounter.Snapshot before=session.encounter.snapshot();boolean zero=before.remainingActions==0;
                check(session.encounter.undo()&&session.encounter.remainingActions()==before.remainingActions+1&&session.encounter.remainingWork()==before.remainingWork+1,"free Undo restores actual productive action, including exhausted state");one.undo++;undos++;if(zero){one.zeroUndo++;zeroBudgetUndos++;}
                record(profile,sensitivity,seed,guardian,attempt,zero?"UNDO_AT_ZERO":"UNDO_SHORT_BUDGET",-1,before,session.encounter,null);
                checkpoint(session,profile,sensitivity,seed,guardian,attempt,"after_free_undo");one.restores++;
                // Failed probes remain charged; Undo cannot manufacture their cost back.
                learnedHelp=true;
            }
            one.solved=session.encounter.isSolved();one.used=session.encounter.snapshot().used;int earned=0;
            if(one.solved){
                int coins=session.store.coins(),campaign=session.store.completedLevels();earned=session.store.completeGuardian(guardian);check(earned==100&&session.store.coins()==coins+100,"real solved encounter receives exactly 100 earned coins");
                check(session.store.completeGuardian(guardian)==0&&new GameStore(session.context).completeGuardian(guardian)==0,"same/cross-handle replay cannot grant another reward");
                session.context=session.context.reboot();session.store=new GameStore(session.context,null,()->1793059200000L);
                check(session.store.guardianCleared(guardian)&&session.store.completeGuardian(guardian)==0&&session.store.coins()==coins+100&&session.store.completedLevels()==campaign,"receipt cold replay leaves wallet and Journey exact");
                total.solved=true;cleared++;rewardCoins+=earned;
            }else check(!session.store.guardianCleared(guardian),"failed attempt neither clears Guardian nor creates a reward");
            encounters.append(profile.id).append(',').append(sensitivity).append(',').append(seed).append(',').append(guardian).append(',').append(attempt).append(',').append(one.solved).append(',').append(session.encounter.definition.minimumActions).append(',').append(session.encounter.definition.baseBudget).append(',').append(one.used).append(',').append(one.blocked).append(',').append(one.blockedCellwise).append(',').append(one.peels).append(',').append(one.steps).append(',').append(one.exits).append(',').append(one.undo).append(',').append(one.zeroUndo).append(',').append(one.hints).append(',').append(one.restores).append(',').append(earned).append(',').append(session.encounter.remainingWork()).append('\n');
            total.blocked+=one.blocked;total.blockedCellwise+=one.blockedCellwise;total.peels+=one.peels;total.steps+=one.steps;total.exits+=one.exits;total.undo+=one.undo;total.zeroUndo+=one.zeroUndo;total.hints+=one.hints;total.restores+=one.restores;
            if(one.solved)break;
        }
        return total;
    }
    static void forcedZeroFixture(){GuardianEncounter e=GuardianEncounter.start(3);check(e.move(0).peeled&&!e.canMove(0),"fixed thick-rear-piece zero fixture");while(e.canAct())check(e.move(0).blocked,"fixed blocked probes reach zero");int used=e.snapshot().used,work=e.remainingWork();check(e.remainingActions()==0&&e.undo()&&e.remainingActions()==1&&e.snapshot().used==used-1&&e.remainingWork()==work+1,"forced zero Undo never softlocks or erases failed charges");GuardianEncounter restored=GuardianEncounter.restore(e.encode());check(restored!=null&&restored.remainingActions()==1,"forced zero Undo cold history reconstructs");e.restart();for(int action:e.optimalPlan())check(e.move(action).success,"forced zero recovery restart legal");check(e.isSolved(),"forced zero fixture completes after free restart");}
    static void forcedRecoveryEdges(Profile profile){for(int number:new int[]{3,5,10}){
        GuardianEncounter e=GuardianEncounter.start(number);int guard=e.definition.totalWork;List<Integer> blockedIds=blocked(e);
        while((blockedIds.isEmpty()||!e.canUndo())&&guard-->0){check(e.move(e.optimalPlan()[0]).success,"edge setup reaches a real front/back dependency");blockedIds=blocked(e);}
        check(!blockedIds.isEmpty()&&e.canUndo(),"forced boundary has a reversible real move and real blocked piece");int prefix=e.history().length,id=blockedIds.get(0),probes=0;
        while(e.canAct()){check(e.move(id).blocked,"forced boundary repeatedly charges real blocked piece");probes++;}
        int used=e.snapshot().used,work=e.remainingWork();check(e.remainingActions()==0&&e.undo()&&e.remainingActions()==1&&e.snapshot().used==used-1&&e.remainingWork()==work+1,"forced boundary Undo at zero refunds exactly one productive action");
        String save=e.encode();GuardianEncounter restored=GuardianEncounter.restore(save);check(restored!=null&&save.equals(restored.encode()),"forced boundary canonical cold replay exact");int remaining=restored.remainingActions(),afterWork=restored.remainingWork();
        restored.restart();check(restored.snapshot().used==0&&restored.remainingActions()==restored.definition.baseBudget,"free restart restores full fixed budget");for(int action:restored.optimalPlan())check(restored.move(action).success,"forced boundary free guidance completes restart");check(restored.isSolved(),"forced boundary has no softlock after recovery");
        recoveryEdges.append(profile.id).append(',').append(number).append(',').append(prefix).append(',').append(probes).append(',').append(used).append(',').append(work).append(',').append(remaining).append(',').append(afterWork).append(",true,true\n");forcedRecoveryEdges++;
    }}
    public static void main(String[]args)throws Exception{
        Path output=Path.of(args[0]);Files.createDirectories(output);long start=System.nanoTime();List<String> inputs=Files.readAllLines(output.resolve("profile-inputs.tsv"));forcedZeroFixture();
        for(int p=1;p<inputs.size();p++){Profile profile=new Profile(inputs.get(p).split("\t",-1));forcedRecoveryEdges(profile);for(double sensitivity:new double[]{.5,1,1.5})for(int s=0;s<4;s++){
            long seed=0x19B05500L+s*1009L;Random rng=new Random(seed);Session session=new Session();Stats combined=new Stats();int solved=0;
            for(int guardian=1;guardian<=10;guardian++){
                for(int n=(guardian-1)*10+1;n<=guardian*10;n++){
                    Board board=new Board(StrategyCatalog.get(n));MoveBudget budget=MoveBudget.start(0,board.level);for(int action:board.solve())check(budget.consumeAction()&&board.performAction(action).success,"real Journey board complete before store unlock");check(board.isSolved(),"real Journey certificate finished");
                    check(session.store.completeLevel(n,JourneyExperience.stars(budget))==55,"one real Journey first-clear reward");journeyBoards++;
                }
                scheduled++;Stats one=encounter(session,profile,sensitivity,seed,guardian,rng);if(one.solved)solved++;
                combined.attempts+=one.attempts;combined.restarts+=one.restarts;combined.blocked+=one.blocked;combined.blockedCellwise+=one.blockedCellwise;combined.peels+=one.peels;combined.steps+=one.steps;combined.exits+=one.exits;combined.undo+=one.undo;combined.zeroUndo+=one.zeroUndo;combined.hints+=one.hints;combined.restores+=one.restores;
            }
            check(solved==10&&session.store.completedLevels()==100&&session.store.totalStars()==300,"all ten optional encounters and all 100 real Journey records complete");
            check(session.store.coins()==6680,"free hints, retries and cold reconstruction never spend or duplicate coins");
            totals.append(profile.id).append(',').append(sensitivity).append(',').append(seed).append(",10,").append(solved).append(',').append(combined.attempts).append(',').append(combined.restarts).append(',').append(combined.blocked).append(',').append(combined.blockedCellwise).append(',').append(combined.peels).append(',').append(combined.steps).append(',').append(combined.exits).append(',').append(combined.undo).append(',').append(combined.zeroUndo).append(',').append(combined.hints).append(',').append(combined.restores).append(",100,").append(session.store.coins()).append(",1000,").append(profile.anchors).append('\n');
        }}
        check(scheduled==720&&cleared==720&&journeyBoards==7200&&rewardCoins==72000,"planned complement coverage exact");check(peels>0&&steps>0&&blockedCellwise>0&&repeatedIdRestores>0,"new layer, step, blocked cell and repeated-ID save branches covered");
        Files.writeString(output.resolve("actions.csv"),moves);Files.writeString(output.resolve("encounter-attempts.csv"),encounters);Files.writeString(output.resolve("profile-totals.csv"),totals);Files.writeString(output.resolve("cold-restores.csv"),interruptions);Files.writeString(output.resolve("forced-recovery-edges.csv"),recoveryEdges);
        String summary="{\n  \"passed\":true,\n  \"assertions\":"+checks+",\n  \"behaviour_profiles\":6,\n  \"sensitivity_multipliers\":[0.5,1.0,1.5],\n  \"seeds_each\":4,\n  \"scheduled_optional_guardian_encounters\":"+scheduled+",\n  \"solved_optional_guardian_encounters\":"+cleared+",\n  \"guardian_attempts_including_retries\":"+attempts+",\n  \"free_restarts\":"+restarts+",\n  \"blocked_probes\":"+blocked+",\n  \"blocked_cellwise_probes\":"+blockedCellwise+",\n  \"layer_peels\":"+peels+",\n  \"single_cell_steps\":"+steps+",\n  \"actual_piece_exits\":"+exits+",\n  \"free_undos\":"+undos+",\n  \"natural_zero_budget_undos\":"+zeroBudgetUndos+",\n  \"separate_forced_zero_budget_contracts\":1,\n  \"separate_forced_zero_undo_restart_edges\":"+forcedRecoveryEdges+",\n  \"free_help_uses\":"+hints+",\n  \"cold_restores\":"+restores+",\n  \"mandatory_after_two_hits_restores\":"+mandatoryRestores+",\n  \"repeated_piece_id_restores\":"+repeatedIdRestores+",\n  \"real_journey_boards_solved_before_unlock_records\":"+journeyBoards+",\n  \"actual_guardian_reward_coins_total_across_synthetic_runs\":"+rewardCoins+",\n  \"elapsed_host_seconds\":"+((System.nanoTime()-start)/1e9)+",\n  \"scope\":\"Separate optional-mode engineering complement: real GuardianEncounter, Board, MoveBudget and GameStore, established JVM preference/reboot adapters, copied 2026 profile coverage anchors. Error/help/retry/session inputs are assumptions; no actual people, Google/social users, enjoyment or retention prediction. Native touch and render evidence is separate.\"\n}\n";
        Files.writeString(output.resolve("summary.json"),summary);System.out.println(summary);
    }
}
