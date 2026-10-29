package com.androidhit.lumadrift.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Fixed identity rarity and copies-only levels. No XP, awakening, spend, clock or random state. */
public final class CompanionProgression {
    public static final String VERSION="v118_1";
    public static final int MAX_LEVEL=10, MAX_COPIES=92;
    /** Cumulative totals. Levels 4–10 are provisional prototype balancing values. */
    private static final int[] THRESHOLDS={1,2,8,14,22,32,44,58,74,92};
    public enum Rarity { N, R, SR, SSR }
    public enum Skill { REVEAL, REROUTE, PROJECT, NONE }
    public static final class Definition {
        public final String id;
        public final Rarity rarity;
        public final Skill skill;
        public final boolean expeditionAvailable;
        private Definition(String id,Rarity rarity,Skill skill) {
            this.id=id;this.rarity=rarity;this.skill=skill;expeditionAvailable=skill!=Skill.NONE;
        }
    }
    public static final class Stats {
        public final int copies,level,nextThreshold,copiesToNext,surplus;
        private Stats(int copies) {
            this.copies=copies;level=levelForCopies(copies);nextThreshold=level>=MAX_LEVEL?MAX_COPIES:THRESHOLDS[level];
            copiesToNext=Math.max(0,nextThreshold-copies);surplus=Math.max(0,copies-MAX_COPIES);
        }
        public boolean maxed(){return level==MAX_LEVEL;}
    }
    private static final List<Definition> ITEMS;
    static {
        List<Definition> result=new ArrayList<>();
        result.add(new Definition("nova",Rarity.N,Skill.REVEAL));
        String[] ids={"miko","orbit","pip","astra","zig","lune","fenn","hoot","buzz","tula",
                "jelli","vela","nimbi","soli","mochi","nixi","prickly","quill","waddles","halo","emberling","bambu","inky"};
        for(int n=0;n<ids.length;n++)result.add(new Definition(ids[n],n<10?Rarity.N:n<18?Rarity.R:n<22?Rarity.SR:Rarity.SSR,
                n==0?Skill.REROUTE:n==1?Skill.PROJECT:Skill.NONE));
        // Local guest-series grade is a prototype visual classification; historical draw pools stay unchanged.
        result.add(new Definition("lol_ashe",Rarity.SR,Skill.NONE));
        result.add(new Definition("lol_ezreal",Rarity.SR,Skill.NONE));
        result.add(new Definition("lol_soraka",Rarity.SR,Skill.NONE));
        result.add(new Definition("lol_ahri",Rarity.SR,Skill.NONE));
        result.add(new Definition("lol_jinx",Rarity.SR,Skill.NONE));
        result.add(new Definition("lol_lux",Rarity.SR,Skill.NONE));
        result.add(new Definition("lol_teemo",Rarity.SR,Skill.NONE));
        result.add(new Definition("lol_yasuo",Rarity.SR,Skill.NONE));
        result.add(new Definition("lol_annie",Rarity.SR,Skill.NONE));
        result.add(new Definition("lol_lulu",Rarity.SR,Skill.NONE));
        result.add(new Definition("lol_sona",Rarity.SR,Skill.NONE));
        result.add(new Definition("lol_miss_fortune",Rarity.SR,Skill.NONE));
        result.add(new Definition("lol_vi",Rarity.SR,Skill.NONE));
        ITEMS=Collections.unmodifiableList(result);
    }
    private CompanionProgression(){}
    public static List<Definition> all(){return ITEMS;}
    public static Definition find(String id){for(Definition item:ITEMS)if(item.id.equals(id))return item;return null;}
    public static boolean expeditionAvailable(String id){Definition d=find(id);return d!=null&&d.expeditionAvailable;}
    public static int threshold(int level){if(level<1||level>MAX_LEVEL)throw new IllegalArgumentException("Level 1..10 required");return THRESHOLDS[level-1];}
    public static int levelForCopies(int copies){if(copies<0)throw new IllegalArgumentException("Negative copies");int level=0;for(int threshold:THRESHOLDS)if(copies>=threshold)level++;return level;}
    public static Stats stats(int copies){if(copies<0)throw new IllegalArgumentException("Negative copies");return new Stats(copies);}
    /** Ownership migrates to one copy; old duplicate coin receipts are never retroactively replayed. */
    public static int initialCopies(boolean owned,int savedCopies){return owned?Math.max(1,Math.max(0,savedCopies)):0;}
    /** Surplus is preserved for the caller's declared compensation policy; no integer wraparound. */
    public static int addCopies(int copies,int amount){if(copies<0||amount<0)throw new IllegalArgumentException("Nonnegative copies required");return (int)Math.min(Integer.MAX_VALUE,(long)copies+amount);}
    /** Prototype charges, fixed at departure: Lv1=1, Lv2=2, Lv3–5=3, Lv6–10=4. Higher thresholds remain provisional. */
    public static int skillCharges(int level){if(level<1||level>MAX_LEVEL)throw new IllegalArgumentException("Level 1..10 required");return level>=6?4:level>=3?3:level>=2?2:1;}
}
