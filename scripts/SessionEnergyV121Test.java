import com.androidhit.lumadrift.data.*;
import java.nio.file.*;
import java.util.*;

/** Actual capacity persistence and debit/refill recovery, independent of account currency. */
public final class SessionEnergyV121Test {
    private static long checks;
    private static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
    private static String id(int value){return String.format(Locale.ROOT,"00000000-0000-0000-0000-%012d",value);}
    private static void consume(SessionEnergyStore energy,int from,int count,long now){for(int index=from;index<from+count;index++){check(energy.reserve(id(index),now),"actual available attempt debit");energy.confirm(id(index),now);}}
    public static void main(String[] args)throws Exception{
        long now=10_000_000L,tick=SessionEnergyStore.REFILL_MS;
        RewardsCosmeticsV17Test.Prefs prefs=new RewardsCosmeticsV17Test.Prefs();SessionEnergyStore energy=new SessionEnergyStore(prefs);
        check(energy.maximum()==10&&energy.snapshot(now).count==10,"old default bank and capacity unchanged");
        long upgrade=now+100*tick;energy.setCapacity(16,upgrade);
        check(energy.maximum()==16&&energy.snapshot(upgrade).count==10,"capacity upgrade grants no units despite long prior full-cap time");
        check(energy.snapshot(upgrade+tick-1).count==10&&energy.snapshot(upgrade+tick).count==11,"one new slot refills only after its actual ten-minute interval");
        check(energy.snapshot(upgrade+6*tick).count==16,"all six added storage slots refill by real elapsed intervals");
        String stored=prefs.getString("session_energy_v120","");check(stored.startsWith("1|16|"),"existing version-one count codec retains enlarged capacity without new backend protocol");
        energy=new SessionEnergyStore(prefs.reboot());check(energy.maximum()==16&&energy.snapshot(upgrade+6*tick).count==16,"capacity and bank survive cold reconstruction");
        energy.setCapacity(10,upgrade+6*tick);check(energy.maximum()==16&&energy.snapshot(upgrade+6*tick).count==16,"older progression cannot lower unlocked storage or erase energy");
        consume(energy,1,16,upgrade+6*tick);check(energy.snapshot(upgrade+6*tick).count==0,"actual sixteen attempt debits drain enlarged bank exactly");
        check(!energy.reserve(id(17),upgrade+6*tick),"empty enlarged bank still refuses another attempt");
        String receipt="a".repeat(64);check(energy.creditReceipt(receipt,10,upgrade+6*tick)&&energy.snapshot(upgrade+6*tick).count==10,"existing verified ten-unit receipt remains exactly ten at cap16");
        check(!energy.creditReceipt(receipt,10,upgrade+6*tick)&&energy.snapshot(upgrade+6*tick).count==10,"larger cap cannot duplicate the same receipt");

        prefs=new RewardsCosmeticsV17Test.Prefs();energy=new SessionEnergyStore(prefs);energy.snapshot(now);consume(energy,30,5,now);
        energy.setCapacity(16,now+tick/2);
        check(energy.snapshot(now+tick/2).count==5&&energy.snapshot(now+tick).count==6,"partially spent bank preserves existing fractional regeneration time");
        check(energy.reserve(id(40),now+tick)&&energy.snapshot(now+tick).count==5,"new pending attempt reserves one unit");
        prefs=prefs.reboot();energy=new SessionEnergyStore(prefs);energy.recover("","",now+tick);
        check(energy.maximum()==16&&energy.snapshot(now+tick).count==6,"no durable board refunds only reserved unit under new cap");
        energy.recover("","",now+tick);check(energy.snapshot(now+tick).count==6,"duplicate recovery cannot refill extra slots");
        check(energy.reserve(id(41),now+tick),"separate actual pending run");
        prefs=prefs.reboot();energy=new SessionEnergyStore(prefs);energy.recover(id(41),"",now+tick);
        check(energy.snapshot(now+tick).count==5&&energy.reserve(id(41),now+tick)&&energy.snapshot(now+tick).count==5,"durable same-attempt resume stays free after cap migration");
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Prefs failed=new RewardsCosmeticsV17Test.Prefs();SessionEnergyStore value=new SessionEnergyStore(failed);value.snapshot(now);
            failed.fail(memory);boolean refused=false;try{value.setCapacity(16,now);}catch(GameStore.LocalSaveException expected){refused=true;}
            check(refused,"capacity failed write surfaces exact retry boundary");
            SessionEnergyStore cold=new SessionEnergyStore(failed.reboot());check(cold.maximum()==10&&cold.snapshot(now).count==10,"failed cap/count pair never partially reaches durable save");
            value.setCapacity(16,now);check(value.maximum()==16&&value.snapshot(now).count==10,"retry flushes complete cap migration without granting units");
        }
        for(int level=1;level<=1000;level++)for(int mode=0;mode<=5;mode++)check(SessionEnergyStore.costsEnergy(mode,level)==(mode!=4&&(mode!=0||level>10)),"all6000 existing mode/level eligibility decisions unchanged");
        String report="{\n  \"passed\":true,\n  \"checks\":"+checks+",\n  \"scope\":\"Actual SessionEnergyStore10-to16 storage migration, no immediate grant/retroactive full-cap ticks, unchanged ten-minute refill and ten-unit receipts, real pending debit/cold refund/resume, old codec and atomic refusal/retry doubles;6000 eligibility checks. No provider, native persistence or human pacing result.\"\n}\n";
        if(args.length>0){Path out=Path.of(args[0]);Files.createDirectories(out.getParent());Files.writeString(out,report);}System.out.print(report);
    }
}
