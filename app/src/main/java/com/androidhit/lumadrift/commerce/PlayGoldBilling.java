package com.androidhit.lumadrift.commerce;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Real Play Billing product/purchase observer. This lab does not launch payment
 * sheets, fabricate store prices, credit purchases, acknowledge or consume them.
 * Purchased tokens are handed only to the verification boundary, never a wallet.
 */
public final class PlayGoldBilling {
    public interface Listener {
        void onStatus(String statusKey);
        void onProducts(List<GoldProduct> products);
        void onPurchasePending(String productId);
        /** Unverified observation: do not grant gold from this callback. */
        void onPurchaseNeedsVerification(String productId, String purchaseToken);
    }

    public static final class GoldProduct {
        public final String id;
        public final int gold;
        /** Exactly the localized price received from Google Play. No fallback price. */
        public final String localizedPrice;
        public final String currencyCode;
        public final long priceMicros;
        private GoldProduct(String id, int gold, String price, String currency, long micros) {
            this.id = id; this.gold = gold; localizedPrice = price; currencyCode = currency; priceMicros = micros;
        }
    }

    private final Context context;
    private final Listener listener;
    private final Handler main = new Handler(Looper.getMainLooper());
    private BillingClient client;
    private boolean connecting, closed;
    private long generation;
    private String status = "billing_unavailable";
    private List<GoldProduct> currentProducts = Collections.emptyList();

    public PlayGoldBilling(Activity activity, Listener listener) {
        if (activity == null || listener == null) throw new IllegalArgumentException("Activity and listener required");
        context = activity.getApplicationContext(); this.listener = listener;
    }

    public String statusKey() { return status; }
    public List<GoldProduct> products() { return currentProducts; }
    /** Intentionally false until an actual verified fulfillment adapter is implemented. */
    public boolean isRealPurchaseEnabled() { return false; }

    /** Explicit store action. It does not contact an ad network. */
    public void connectAndQuery() {
        onMain(() -> {
            if (closed) return;
            if (client == null) {
                client = BillingClient.newBuilder(context)
                        .setListener((result, purchases) -> onMain(() -> handlePurchases(result, purchases)))
                        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                        .enableAutoServiceReconnection().build();
            }
            if (client.isReady()) { queryProducts(); queryOwned(); return; }
            if (connecting) return;
            connecting = true;
            final long request = generation;
            report("billing_connecting");
            client.startConnection(new BillingClientStateListener() {
                @Override public void onBillingSetupFinished(BillingResult result) {
                    onMain(() -> {
                        if (closed || request != generation) return;
                        connecting = false;
                        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            queryProducts(); queryOwned();
                        } else {
                            currentProducts = Collections.emptyList(); listener.onProducts(currentProducts);
                            report("billing_unavailable");
                        }
                    });
                }
                @Override public void onBillingServiceDisconnected() {
                    onMain(() -> { if (!closed) { connecting = false; report("billing_unavailable"); } });
                }
            });
        });
    }

    private void queryProducts() {
        final long request = generation;
        List<QueryProductDetailsParams.Product> wanted = new ArrayList<>();
        for (String id : CommerceConfig.GOLD_PRODUCT_IDS) wanted.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id).setProductType(BillingClient.ProductType.INAPP).build());
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder().setProductList(wanted).build();
        client.queryProductDetailsAsync(params, (result, details) -> onMain(() -> {
            if (closed || request != generation) return;
            Map<String, GoldProduct> fetched = new LinkedHashMap<>();
            if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                for (ProductDetails product : details.getProductDetailsList()) {
                    int gold = CommerceConfig.goldAmount(product.getProductId());
                    if (gold == 0) continue;
                    List<ProductDetails.OneTimePurchaseOfferDetails> offers = product.getOneTimePurchaseOfferDetailsList();
                    if (offers == null || offers.isEmpty()) continue;
                    ProductDetails.OneTimePurchaseOfferDetails offer = offers.get(0);
                    fetched.put(product.getProductId(), new GoldProduct(product.getProductId(), gold,
                            offer.getFormattedPrice(), offer.getPriceCurrencyCode(), offer.getPriceAmountMicros()));
                }
            }
            List<GoldProduct> ordered = new ArrayList<>();
            for (String id : CommerceConfig.GOLD_PRODUCT_IDS) if (fetched.containsKey(id)) ordered.add(fetched.get(id));
            currentProducts = Collections.unmodifiableList(ordered);
            listener.onProducts(currentProducts);
            report(currentProducts.isEmpty() ? "billing_unavailable" : "billing_ready");
        }));
    }

    /** Call on foreground return to observe pending transactions that have changed. */
    public void refreshPurchases() {
        onMain(() -> {
            if (closed) return;
            if (client == null || !client.isReady()) { connectAndQuery(); return; }
            queryOwned();
        });
    }

    private void queryOwned() {
        final long request = generation;
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                (result, purchases) -> onMain(() -> { if (!closed && request == generation) handlePurchases(result, purchases); }));
    }

    private void handlePurchases(BillingResult result, List<Purchase> purchases) {
        if (closed) return;
        if (result.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            report("billing_cancelled"); return;
        }
        if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            report("billing_unavailable"); return;
        }
        if (purchases == null) return;
        for (Purchase purchase : purchases) {
            for (String productId : purchase.getProducts()) {
                if (CommerceConfig.goldAmount(productId) == 0) continue;
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                    listener.onPurchasePending(productId); report("billing_pending");
                } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    // The client callback is not proof of payment. Backend verification,
                    // durable idempotent fulfillment and consumable delivery are absent.
                    listener.onPurchaseNeedsVerification(productId, purchase.getPurchaseToken());
                    report("billing_verification_required");
                }
            }
        }
    }

    /** No payment flow can be launched by this distribution, including license-test flows. */
    public void launchPurchase(String productId) {
        onMain(() -> {
            if (closed) return;
            if (CommerceConfig.goldAmount(productId) == 0) { report("billing_no_product"); return; }
            if (CommerceConfig.MODE_SANDBOX) { report("billing_test_only"); return; }
            // A configuration toggle cannot substitute for a secure verifier and
            // transactional fulfillment. No launchBillingFlow call exists here.
            report("production_blocked");
        });
    }

    private void report(String key) { if (!closed) { status = key; listener.onStatus(key); } }
    private void onMain(Runnable action) { if (Looper.myLooper() == main.getLooper()) action.run(); else main.post(action); }
    public void close() {
        onMain(() -> {
            closed = true; generation++; connecting = false; currentProducts = Collections.emptyList();
            if (client != null) { client.endConnection(); client = null; }
        });
    }
}
