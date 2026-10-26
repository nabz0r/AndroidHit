package com.androidhit.lumadrift.commerce;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.androidhit.lumadrift.BuildConfig;

/** Separate production/Lab builds. Release candidates use sample creatives until explicitly qualified. */
public final class CommerceConfig {
    public static final boolean MODE_SANDBOX = BuildConfig.MODE_SANDBOX;
    public static final boolean FORCE_TEST_ADS = BuildConfig.FORCE_TEST_ADS;
    public static final String ADS_APP_ID = "ca-app-pub-3940256099942544~3347511713";
    public static final String REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
    public static final int REWARDED_GOLD = 35;
    public static final List<String> GOLD_PRODUCT_IDS = Collections.unmodifiableList(Arrays.asList(
            "luma_gold_500", "luma_gold_1400", "luma_gold_3600"));

    public static final String PRODUCTION_ADMOB_APP_ID = "ca-app-pub-6914185503996225~9670436379";
    public static final String PRODUCTION_REWARDED_AD_UNIT_ID = "ca-app-pub-6914185503996225/7013317595";
    public static final String PRODUCTION_CONTINUE_AD_UNIT_ID = "ca-app-pub-6914185503996225/7504272185";
    public static final String PRODUCTION_POWER_AD_UNIT_ID = "ca-app-pub-6914185503996225/7093208640";
    public static final String PURCHASE_VERIFIER_ENDPOINT = BuildConfig.WALLET_API_BASE_URL;
    public static final boolean PURCHASE_VERIFIER_INTEGRATED = true;

    private CommerceConfig() { }

    public static int goldAmount(String productId) {
        if ("luma_gold_500".equals(productId)) return 500;
        if ("luma_gold_1400".equals(productId)) return 1400;
        if ("luma_gold_3600".equals(productId)) return 3600;
        return 0;
    }

    public static boolean productionAdsReady() {
        return !MODE_SANDBOX && !FORCE_TEST_ADS && realAppId(PRODUCTION_ADMOB_APP_ID)
                && realAdUnit(PRODUCTION_REWARDED_AD_UNIT_ID)
                && realAdUnit(PRODUCTION_CONTINUE_AD_UNIT_ID) && realAdUnit(PRODUCTION_POWER_AD_UNIT_ID);
    }

    public static boolean productionBillingReady() {
        return !MODE_SANDBOX && PURCHASE_VERIFIER_INTEGRATED
                && PURCHASE_VERIFIER_ENDPOINT.startsWith("https://")
                && PURCHASE_VERIFIER_ENDPOINT.length() > 12;
    }

    static String appId() { return MODE_SANDBOX ? ADS_APP_ID : PRODUCTION_ADMOB_APP_ID; }
    static String rewardedId() { return rewardedId(AdsController.RewardPlacement.GOLD); }
    static String rewardedId(AdsController.RewardPlacement placement) {
        if (MODE_SANDBOX || FORCE_TEST_ADS) return REWARDED_AD_UNIT_ID;
        if (placement == AdsController.RewardPlacement.CONTINUATION) return PRODUCTION_CONTINUE_AD_UNIT_ID;
        if (placement == AdsController.RewardPlacement.POWER) return PRODUCTION_POWER_AD_UNIT_ID;
        return PRODUCTION_REWARDED_AD_UNIT_ID;
    }
    private static boolean realAppId(String value) {
        return value.matches("ca-app-pub-[0-9]{16}~[0-9]{10}") && !value.contains("3940256099942544");
    }
    private static boolean realAdUnit(String value) {
        return value.matches("ca-app-pub-[0-9]{16}/[0-9]{10}") && !value.contains("3940256099942544");
    }
}
