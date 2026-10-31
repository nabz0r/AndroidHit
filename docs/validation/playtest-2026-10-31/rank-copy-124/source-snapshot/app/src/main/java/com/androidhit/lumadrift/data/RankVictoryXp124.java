package com.androidhit.lumadrift.data;

/** XP before/after and the recipient belong to one durable solved-run award. */
public final class RankVictoryXp124 {
    public final String runId, boardKey, captainId;
    public final int beforeXp, afterXp, stars;
    public final boolean firstWin;
    RankVictoryXp124(String runId, String boardKey, String captainId, int stars, int beforeXp, int afterXp, boolean firstWin) {
        this.runId=runId;this.boardKey=boardKey;this.captainId=captainId;this.stars=stars;
        this.beforeXp=beforeXp;this.afterXp=afterXp;this.firstWin=firstWin;
        if (!valid()) throw new IllegalArgumentException("Valid rank XP receipt required");
    }
    public int ranksCrossed() { return firstWin ? RankCopyRules124.ranksCrossed(beforeXp, afterXp) : 0; }
    public String encode() { return "1|"+runId+"|"+boardKey+"|"+captainId+"|"+stars+"|"+beforeXp+"|"+afterXp+"|"+(firstWin?"1":"0"); }
    static RankVictoryXp124 decode(String encoded) {
        if (encoded==null||encoded.length()>500) return null;
        try {
            String[] p=encoded.split("\\|",-1);
            if(p.length!=8||!p[0].equals("1")||!(p[7].equals("0")||p[7].equals("1")))return null;
            RankVictoryXp124 value=new RankVictoryXp124(p[1],p[2],p[3],Integer.parseInt(p[4]),Integer.parseInt(p[5]),Integer.parseInt(p[6]),p[7].equals("1"));
            return value.encode().equals(encoded)?value:null;
        }catch(IllegalArgumentException invalid){return null;}
    }
    private boolean valid() {
        return runId!=null&&runId.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")
            &&boardKey!=null&&boardKey.matches("[a-z0-9_:-]{1,100}")&&captainId!=null&&captainId.matches("[a-z0-9_]{0,60}")
            &&stars>=1&&stars<=3&&beforeXp>=0&&afterXp>=beforeXp&&afterXp-beforeXp<=35
            &&(firstWin||beforeXp==afterXp);
    }
}
