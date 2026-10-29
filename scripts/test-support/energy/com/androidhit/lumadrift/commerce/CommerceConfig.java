package com.androidhit.lumadrift.commerce;
public final class CommerceConfig {
    public static boolean MODE_SANDBOX=false,ready=true;
    public static boolean productionBillingReady(){return ready;}
    public static final String PURCHASE_VERIFIER_ENDPOINT="https://wallet.fixture.test";
    public static int goldAmount(String id){return "luma_gold_500".equals(id)?500:0;}
}
