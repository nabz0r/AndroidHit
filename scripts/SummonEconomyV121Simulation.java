import com.androidhit.lumadrift.data.*;
import java.nio.file.*;
import java.util.*;

/** Fixed deterministic cohort arithmetic against the actual versioned draw function. */
public final class SummonEconomyV121Simulation {
    private static final int COHORTS=2000,DRAWS=200;
    private static long checks;
    private static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
    private static String quote(String value){return '"'+value.replace("\"","\"\"")+'"';}
    public static void main(String[] args)throws Exception{
        Path out=Path.of(args.length>0?args[0]:"docs/validation/v1.21/host/summon-simulation");Files.createDirectories(out);
        StringBuilder items=new StringBuilder("banner,id,name_fr,name_en,rarity,exact_probability,observed_draws,total_draws,observed_probability,duplicate_coins,expected_draws_first_ownership,expected_draws_8_companion_copies,expected_draws_92_companion_copies,gross_first_ownership_old_55_coin_clear_equivalents,gross_first_ownership_new_110_coin_clear_equivalents\n");
        StringBuilder cohorts=new StringBuilder("banner,draws,cohorts,observed_mean_distinct,exact_expected_distinct,observed_complete_pool_fraction,observed_mean_duplicate_refund,exact_expected_duplicate_refund,observed_mean_net_coin_cost,exact_expected_net_coin_cost\n");
        StringBuilder summaries=new StringBuilder();
        for(SummonCatalog.Banner banner:SummonCatalog.Banner.values()){
            List<SummonCatalog.Item> pool=SummonCatalog.items(banner);int[] checkpoints={10,20,50,100,200};
            long[] distinct=new long[checkpoints.length],refunds=new long[checkpoints.length],complete=new long[checkpoints.length];
            Map<String,Long> counts=new LinkedHashMap<>();for(SummonCatalog.Item item:pool)counts.put(item.id,0L);
            for(int cohort=0;cohort<COHORTS;cohort++){
                Set<String> owned=new HashSet<>();long refunded=0;int checkpoint=0;
                String seed="v121-fixed-cohort-"+cohort;
                for(int draw=1;draw<=DRAWS;draw++){
                    SummonCatalog.Item item=SummonCatalog.draw(seed,banner,"v121-fixed-draw-"+draw);
                    check(counts.containsKey(item.id),"actual draw stays in exact advertised pool");counts.put(item.id,counts.get(item.id)+1);
                    if(!owned.add(item.id))refunded+=item.duplicateCoins;
                    if(draw==checkpoints[checkpoint]){
                        distinct[checkpoint]+=owned.size();refunds[checkpoint]+=refunded;if(owned.size()==pool.size())complete[checkpoint]++;
                        checkpoint++;if(checkpoint==checkpoints.length)break;
                    }
                }
            }
            int total=COHORTS*DRAWS,price=SummonCatalog.priceCoins(banner);double matureRefund=0;
            int sum=0;for(SummonCatalog.Item item:pool){
                sum+=item.chanceBasisPoints;double probability=item.chanceBasisPoints/10000d,expected=total*probability;
                check(Math.abs(counts.get(item.id)-expected)<7*Math.sqrt(total*probability*(1-probability)),"fixed simulation agrees with unconditional probability within explicit broad diagnostic bound");
                String fr=item.kind==SummonCatalog.Kind.AVATAR?Strings.get("fr",item.nameKey):V116Strings.get("fr",item.nameKey);
                String en=item.kind==SummonCatalog.Kind.AVATAR?Strings.get("en",item.nameKey):V116Strings.get("en",item.nameKey);
                check(!fr.equals(item.nameKey)&&!en.equals(item.nameKey),"actual pool entries have concrete French and English names");
                String eight=item.kind==SummonCatalog.Kind.AVATAR?String.format(Locale.ROOT,"%.6f",8/probability):"";
                String ninetyTwo=item.kind==SummonCatalog.Kind.AVATAR?String.format(Locale.ROOT,"%.6f",92/probability):"";
                items.append(String.format(Locale.ROOT,"%s,%s,%s,%s,%s,%.8f,%d,%d,%.8f,%d,%.6f,%s,%s,%.6f,%.6f\n",banner,item.id,quote(fr),quote(en),item.rarity,probability,counts.get(item.id),total,counts.get(item.id)/(double)total,item.duplicateCoins,1/probability,eight,ninetyTwo,price/probability/55,price/probability/110));
                matureRefund+=probability*item.duplicateCoins;
            }
            check(sum==10000,"exact source odds total100percent");
            check(SummonCatalog.find(banner,"nova")==null,"free Nova is excluded from paid-Coin pools");
            for(int checkpoint=0;checkpoint<checkpoints.length;checkpoint++){
                int draw=checkpoints[checkpoint];double expectedDistinct=0,expectedRefund=0;
                for(SummonCatalog.Item item:pool){double p=item.chanceBasisPoints/10000d,ever=1-Math.pow(1-p,draw);expectedDistinct+=ever;expectedRefund+=item.duplicateCoins*(draw*p-ever);}
                double observedRefund=refunds[checkpoint]/(double)COHORTS;
                cohorts.append(String.format(Locale.ROOT,"%s,%d,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f\n",banner,draw,COHORTS,distinct[checkpoint]/(double)COHORTS,expectedDistinct,complete[checkpoint]/(double)COHORTS,observedRefund,expectedRefund,draw*price-observedRefund,draw*price-expectedRefund));
            }
            if(summaries.length()>0)summaries.append(",\n");
            summaries.append("    {\"banner\":\"").append(banner).append("\",\"draws\":").append(total).append(",\"pool_size\":").append(pool.size()).append(",\"price_coins\":").append(price)
                    .append(",\"mature_expected_duplicate_refund\":").append(String.format(Locale.ROOT,"%.6f",matureRefund))
                    .append(",\"mature_expected_net_price\":").append(String.format(Locale.ROOT,"%.6f",price-matureRefund))
                    .append(",\"observed_complete_pool_at_200_draws\":").append(String.format(Locale.ROOT,"%.6f",complete[4]/(double)COHORTS)).append('}');
        }
        String report="{\n  \"passed\":true,\n  \"checks\":"+checks+",\n  \"fixed_seed_method\":\"SummonCatalog.draw(v121-fixed-cohort-N, banner, v121-fixed-draw-N),2000 independent modeled empty collections times200 draws per banner\",\n  \"actual_catalog_draws\":800000,\n  \"banners\":[\n"+summaries+"\n  ],\n  \"scope\":\"Pure fixed-seed source-pool simulation and exact independent-draw arithmetic, not real GameStore purchases, human cohorts, calendar/play time, RNG security, conversion, addiction, retention or revenue. First-clear equivalents are mechanical gross Coin ratios excluding other income, duplicate refunds and optional spending; current curve changes earnings, not these fixed draw odds/prices.\"\n}\n";
        Files.writeString(out.resolve("summary.json"),report);Files.writeString(out.resolve("items-fr-en.csv"),items);Files.writeString(out.resolve("cohorts.csv"),cohorts);System.out.print(report);
    }
}
