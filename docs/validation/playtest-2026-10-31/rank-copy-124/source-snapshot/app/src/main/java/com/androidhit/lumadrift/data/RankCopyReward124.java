package com.androidhit.lumadrift.data;

/** The original historical rank reward is returned on retries without granting it twice. */
public final class RankCopyReward124 {
    public final int rank, copiesBefore, copiesAfter, coins;
    public final String runId, captainId, status;
    RankCopyReward124(int rank,String runId,String captainId,int before,int after,int coins,String status) {
        this.rank=rank;this.runId=runId;this.captainId=captainId;copiesBefore=before;copiesAfter=after;this.coins=coins;this.status=status;
    }
    public int copies() { return copiesAfter-copiesBefore; }
    String encode(){return "1|"+rank+"|"+runId+"|"+captainId+"|"+copiesBefore+"|"+copiesAfter+"|"+coins+"|"+status;}
    static RankCopyReward124 decode(String record) {
        if(record==null||record.length()>300)return null;
        try{
            String[] v=record.split("\\|",-1);if(v.length!=8||!v[0].equals("1"))return null;
            RankCopyReward124 value=new RankCopyReward124(Integer.parseInt(v[1]),v[2],v[3],Integer.parseInt(v[4]),Integer.parseInt(v[5]),Integer.parseInt(v[6]),v[7]);
            if(value.rank<2||value.rank>RankCopyRules124.rankForXp(Integer.MAX_VALUE)||!value.runId.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")||!value.captainId.matches("[a-z0-9_]{0,60}"))return null;
            if(value.copiesBefore<0||value.copiesAfter<value.copiesBefore||value.copiesAfter>CompanionProgression.MAX_COPIES||value.copies()>1||value.coins<0||value.coins>RankCopyRules124.MAX_CAPTAIN_DUPLICATE_COINS)return null;
            if(!value.status.equals("COPY")&&!value.status.equals("MAX_COMPENSATION")&&!value.status.equals("LEGACY_NO_CAPTAIN"))return null;
            if(value.status.equals("COPY")&&(value.copies()!=1||value.coins!=0||value.captainId.isEmpty()))return null;
            if(value.status.equals("MAX_COMPENSATION")&&(value.copies()!=0||value.copiesBefore!=CompanionProgression.MAX_COPIES||value.coins!=RankCopyRules124.MAX_CAPTAIN_DUPLICATE_COINS||value.captainId.isEmpty()))return null;
            if(value.status.equals("LEGACY_NO_CAPTAIN")&&(!value.captainId.isEmpty()||value.copiesAfter!=0||value.coins!=0))return null;
            return value.encode().equals(record)?value:null;
        }catch(IllegalArgumentException invalid){return null;}
    }
}
