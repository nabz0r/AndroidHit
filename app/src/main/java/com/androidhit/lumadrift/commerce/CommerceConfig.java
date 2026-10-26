package com.androidhit.lumadrift.commerce;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Explicit lab configuration. Sample ads and simulated gold cannot enable production sales. */
public final class CommerceConfig {
    public static final boolean MODE_SANDBOX = true;
    public static final String ADS_APP_ID = "ca-app-pub-3940256099942544~3347511713";
    public static final String REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
    public static final int REWARDED_GOLD = 35;
    public static final List<String> GOLD_PRODUCT_IDS = Collections.unmodifiableList(Arrays.asList(
            "luma_gold_500", "luma_gold_1400", "luma_gold_3600"));

    // Deliberately absent. Changing MODE_SANDBOX alone cannot activate either service.
    public static final String PRODUCTION_ADMOB_APP_ID = "";
    public static final String PRODUCTION_REWARDED_AD_UNIT_ID = "";
    public static final String PURCHASE_VERIFIER_ENDPOINT = "";
    public static final boolean PURCHASE_VERIFIER_INTEGRATED = false;

    private CommerceConfig() { }

    public static int goldAmount(String productId) {
        if ("luma_gold_500".equals(productId)) return 500;
        if ("luma_gold_1400".equals(productId)) return 1400;
        if ("luma_gold_3600".equals(productId)) return 3600;
        return 0;
    }

    public static boolean productionAdsReady() {
        return !MODE_SANDBOX && realAppId(PRODUCTION_ADMOB_APP_ID)
                && realAdUnit(PRODUCTION_REWARDED_AD_UNIT_ID);
    }

    public static boolean productionBillingReady() {
        return !MODE_SANDBOX && PURCHASE_VERIFIER_INTEGRATED
                && PURCHASE_VERIFIER_ENDPOINT.startsWith("https://")
                && PURCHASE_VERIFIER_ENDPOINT.length() > 12;
    }

    static String appId() { return MODE_SANDBOX ? ADS_APP_ID : PRODUCTION_ADMOB_APP_ID; }
    static String rewardedId() { return MODE_SANDBOX ? REWARDED_AD_UNIT_ID : PRODUCTION_REWARDED_AD_UNIT_ID; }
    private static boolean realAppId(String value) {
        return value.matches("ca-app-pub-[0-9]{16}~[0-9]{10}") && !value.contains("3940256099942544");
    }
    private static boolean realAdUnit(String value) {
        return value.matches("ca-app-pub-[0-9]{16}/[0-9]{10}") && !value.contains("3940256099942544");
    }
}
