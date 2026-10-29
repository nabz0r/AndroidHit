import android.content.SharedPreferences;
import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.ui.PuzzleReaction;
import java.util.*;
import java.util.concurrent.*;

/** Device energy transaction tests, including memory-before-disk failure and cold recovery. */
public final class SessionEnergyV120Test {
    static int checks;
    static void check(boolean condition,String why){checks++;if(!condition)throw new AssertionError(why);}
    static final class Prefs implements SharedPreferences {
        final Map<String,Object> memory=new HashMap<>(),disk=new HashMap<>();int fail;boolean memoryFirst;
        public Map<String,?> getAll(){return new HashMap<>(memory);}
        public boolean contains(String k){return memory.containsKey(k);}
        public int getInt(String k,int d){return (Integer)memory.getOrDefault(k,d);}
        public String getString(String k,String d){return (String)memory.getOrDefault(k,d);}
        public boolean getBoolean(String k,boolean d){return (Boolean)memory.getOrDefault(k,d);}
        public Editor edit(){return new Editor(){final Map<String,Object> changes=new HashMap<>();
            public Editor putInt(String k,int v){changes.put(k,v);return this;}
            public Editor putString(String k,String v){changes.put(k,v);return this;}
            public Editor putBoolean(String k,boolean v){changes.put(k,v);return this;}
            public boolean commit(){if(fail-->0){if(memoryFirst)memory.putAll(changes);return false;}memory.putAll(changes);disk.putAll(changes);return true;}
        };}
        Prefs cold(){Prefs p=new Prefs();p.disk.putAll(disk);p.memory.putAll(disk);return p;}
    }
    static String id(int n){return String.format(Locale.ROOT,"00000000-0000-0000-0000-%012d",n);}
    static void refused(Runnable r){boolean bad=false;try{r.run();}catch(IllegalArgumentException e){bad=true;}check(bad,"invalid input rejected");}
    static void consume(SessionEnergyStore s,int start,int count,long t){for(int n=start;n<start+count;n++){check(s.reserve(id(n),t),"debit available");s.confirm(id(n),t);}}
    public static void main(String[] args)throws Exception{
        long now=1_000_000;Prefs p=new Prefs();SessionEnergyStore s=new SessionEnergyStore(p);
        check(s.snapshot(now).count==10,"full initial bank");Map<String,Object> before=new HashMap<>(p.disk);s.snapshot(now+1000);check(p.disk.equals(before),"full HUD read never rewrites clock every frame");
        consume(s,1,10,now);check(s.snapshot(now).count==0,"exact 10 attempts");check(!s.reserve(id(11),now),"empty refuses");check(s.snapshot(now+599_999).count==0,"not early refill");check(s.snapshot(now+600_000).count==1,"one refill");check(s.snapshot(now+1_500_000).count==2,"partial tick preserved");check(s.snapshot(now+6_000_000).count==10,"long offline refill bounded");
        for(int mode=0;mode<=5;mode++)for(int n=1;n<=1000;n++)check(SessionEnergyStore.costsEnergy(mode,n)==(mode!=4&&(mode!=0||n>10)),"mode/level cost policy");
        p=new Prefs();s=new SessionEnergyStore(p);check(s.reserve(id(1),now),"reserve");check(s.reserve(id(1),now),"retry pending same run");check(s.snapshot(now).count==9,"no duplicate pending cost");check(!s.reserve(id(2),now),"second pending board refused");
        s=new SessionEnergyStore(p.cold());s.recover("", "",now);check(s.snapshot(now).count==10,"crash before durable board refunds reservation");
        check(s.reserve(id(2),now),"new reservation");// Independent cold disk recovery below.
        Prefs q=new Prefs();SessionEnergyStore c=new SessionEnergyStore(q);c.reserve(id(3),now);c=new SessionEnergyStore(q.cold());c.recover(id(3),"",now);check(c.snapshot(now).count==9,"saved board confirms pending exactly once");check(c.reserve(id(3),now),"same durable run remains free");check(c.snapshot(now).count==9,"confirmed duplicate no debit");
        for(boolean memoryFirst:new boolean[]{false,true}){
            Prefs f=new Prefs();SessionEnergyStore e=new SessionEnergyStore(f);e.snapshot(now);f.fail=1;f.memoryFirst=memoryFirst;boolean failed=false;
            try{e.reserve(id(1),now);}catch(GameStore.LocalSaveException expected){failed=true;}
            check(failed,"failed reserve not reported successful");check(e.reserve(id(1),now),"retry flushes exact pending transaction");e.confirm(id(1),now);check(e.snapshot(now).count==9,"failed retry cannot double debit");
            consume(e,2,9,now);String receipt="a".repeat(64);f.fail=1;failed=false;try{e.creditReceipt(receipt,10,now);}catch(GameStore.LocalSaveException expected){failed=true;}
            check(failed,"failed paid credit not acknowledged");check(!e.creditReceipt(receipt,10,now),"retry recognizes durable flushed receipt");check(e.snapshot(now).count==10,"paid grant exact once");e=new SessionEnergyStore(f.cold());check(!e.creditReceipt(receipt,10,now),"cold paid receipt no replay");
        }
        Prefs concurrent=new Prefs();SessionEnergyStore cs=new SessionEnergyStore(concurrent);ExecutorService pool=Executors.newFixedThreadPool(8);List<Future<Boolean>> results=new ArrayList<>();
        for(int n=0;n<100;n++)results.add(pool.submit(()->cs.reserve(id(91),now)));for(Future<Boolean> r:results)check(r.get(),"concurrent duplicate reserve");pool.shutdown();cs.confirm(id(91),now);check(cs.snapshot(now).count==9,"concurrent debit single");
        cs.snapshot(now-999_999);check(cs.snapshot(now-999_998).count==9,"clock rollback no immediate grant");check(cs.snapshot(now).count==9,"restore old time cannot replay a refill");
        SessionEnergyStore finalStore=cs;refused(()->finalStore.reserve("bad",now));refused(()->finalStore.creditReceipt("token",10,now));refused(()->finalStore.creditReceipt("b".repeat(64),11,now));
        PuzzleReaction reaction=new PuzzleReaction();check(reaction.kind(0)==PuzzleReaction.Kind.IDLE,"no fake idle celebration");
        for(int n=1;n<=9;n++){reaction.success(n*2000);check(reaction.streak()==n,"actual exit streak");check(reaction.kind(n*2000)==(n>=6?PuzzleReaction.Kind.DANCE:n>=3?PuzzleReaction.Kind.CHEER:PuzzleReaction.Kind.JUMP),"reaction tier from real exits");check(reaction.kind(n*2000+1300)==PuzzleReaction.Kind.IDLE,"reaction ends");}
        reaction.blocked(22000);check(reaction.streak()==0&&reaction.kind(22000)==PuzzleReaction.Kind.THINK,"blocked resets streak");reaction.reset();check(reaction.kind(22000)==PuzzleReaction.Kind.IDLE,"undo resets");
        for(PuzzleReaction.Kind kind:PuzzleReaction.Kind.values())for(int t=0;t<1400;t++){check(PuzzleReaction.rise(kind,t,true)==0&&PuzzleReaction.tilt(kind,t,true)==0,"reduced motion stable");check(Float.isFinite(PuzzleReaction.rise(kind,t,false))&&Float.isFinite(PuzzleReaction.tilt(kind,t,false)),"finite transforms");}
        for(String lang:new String[]{"fr","en","de","es","pt","it","ja"})check(!SessionEnergyStrings.get(lang,"rule").contains("rule")&&SessionEnergyStrings.get(lang,"label",5).contains("5/10"),"localized rule and balance");
        System.out.println("PASS session energy / reactions: "+checks+" assertions");
    }
}
