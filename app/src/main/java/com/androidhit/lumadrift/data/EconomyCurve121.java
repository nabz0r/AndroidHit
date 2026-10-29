package com.androidhit.lumadrift.data;

/**
 * Versioned progression-only model. No balance, failure, spending or session
 * signal changes these terms. Stores opt in explicitly for new 1.21 attempts.
 */
public final class EconomyCurve121 {
    public static final String VERSION="v121_1";
    public static final int MAX_LEVEL=1000;
    public static final int FREE_UNDOS=2;
    private static final double REWARD_NORMALIZER=Math.log1p(990d/25d);
    private static final double BOOST_NORMALIZER=Math.log1p(990d/50d);
    private EconomyCurve121() { }

    /** U1–U10 stay at 1; U1000 reaches exactly 2. */
    public static double campaignMultiplier(int level){
        return 1d+Math.log1p(Math.max(0,level(level)-10)/25d)/REWARD_NORMALIZER;
    }

    /** Total first-clear entitlement; star upgrades pay only new minus old. */
    public static int campaignReward(int level,int stars){
        if(stars<0||stars>3)throw new IllegalArgumentException("Stars 0..3 required");
        if(stars==0){level(level);return 0;}
        return (int)Math.round((25+10*stars)*campaignMultiplier(level));
    }

    /** Gentler 1..1.4 pricing; the first ten opening boards keep base prices. */
    public static double boostMultiplier(int level){
        return 1d+.4d*Math.log1p(Math.max(0,level(level)-10)/50d)/BOOST_NORMALIZER;
    }

    public static int boostPrice(int baseCoins,int level){
        if(baseCoins<=0){level(level);return 0;}
        return (int)Math.min(Integer.MAX_VALUE,Math.ceil(baseCoins*boostMultiplier(level)));
    }

    /** paidIndex is one-based AFTER the attempt's two free Undo uses. */
    public static int undoPrice(int level,int paidIndex){
        if(paidIndex<1)throw new IllegalArgumentException("Paid Undo index starts at one");
        return (int)Math.min(40,Math.ceil(8+4*Math.log1p(level(level)/25d)+4*Math.log1p(paidIndex)));
    }

    public static int freeUndos(){return FREE_UNDOS;}
    /** Larger storage ceiling only; setting it never grants session units. */
    public static int energyCapacity(int level){
        return 10+(int)Math.floor(6*Math.log1p(Math.max(0,level(level)-10)/50d)/BOOST_NORMALIZER);
    }
    public static int premiumEnergyPriceGold(){return 50;}
    public static int premiumEnergyUnits(){return 10;}

    private static int level(int level){
        if(level<1||level>MAX_LEVEL)throw new IllegalArgumentException("U level 1..1000 required");
        return level;
    }
}
