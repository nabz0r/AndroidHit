package com.androidhit.lumadrift.commerce;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.androidhit.lumadrift.wallet.ProductionWalletClient;
import java.lang.ref.WeakReference;
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
 * Play Billing checkout and recovery. Purchased tokens cross only the server
 * verification boundary. Gold is never credited, acknowledged or consumed here.
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
    private final WeakReference<Activity> activity;
    private final Map<String, ProductDetails> detailsById = new LinkedHashMap<>();
    private final Map<String, String> offersById = new LinkedHashMap<>();
    private boolean checkoutInFlight;
    private final Listener listener;
    private final Handler main = new Handler(Looper.getMainLooper());
    private BillingClient client;
    private boolean connecting, closed, queryCatalogRequested;
    private long generation;
    private String status = "billing_unavailable";
    private List<GoldProduct> currentProducts = Collections.emptyList();

    public PlayGoldBilling(Activity activity, Listener listener) {
        if (activity == null || listener == null) throw new IllegalArgumentException("Activity and listener required");
        context = activity.getApplicationContext(); this.activity = new WeakReference<>(activity); this.listener = listener;
    }

    public String statusKey() { return status; }
    public List<GoldProduct> products() { return currentProducts; }
    /** Configuration alone cannot bypass account, product and connection checks. */
    public boolean isRealPurchaseEnabled() { return CommerceConfig.productionBillingReady()
            && !BillingPolicy.serverBase(CommerceConfig.PURCHASE_VERIFIER_ENDPOINT).isEmpty()
            && !ProductionWalletClient.currentUid(context).isEmpty(); }

    /** Explicit store action. It does not contact an ad network. */
    public void connectAndQuery() { connect(true); }
    /** Existing-account foreground recovery: no product catalogue query or checkout. */
    public void connectForRecovery() { connect(false); }
    private void connect(boolean includeCatalogue) {
        onMain(() -> {
            if (closed) return;
            queryCatalogRequested |= includeCatalogue;
            if (client == null) {
                client = BillingClient.newBuilder(context)
                        .setListener((result, purchases) -> onMain(() -> handlePurchases(result, purchases)))
                        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                        .enableAutoServiceReconnection().build();
            }
            if (client.isReady()) { if (queryCatalogRequested) queryProducts(); queryOwned(); return; }
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
                            if (queryCatalogRequested) queryProducts(); queryOwned();
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
            detailsById.clear(); offersById.clear();
            if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                for (ProductDetails product : details.getProductDetailsList()) {
                    int gold = CommerceConfig.goldAmount(product.getProductId());
                    if (gold == 0) continue;
                    List<ProductDetails.OneTimePurchaseOfferDetails> offers = product.getOneTimePurchaseOfferDetailsList();
                    if (offers == null || offers.isEmpty()) continue;
                    // Gold is a consumable, never a rental or preorder. Prefer the
                    // ordinary buy option; do not silently substitute a different offer.
                    ProductDetails.OneTimePurchaseOfferDetails offer = null;
                    for (ProductDetails.OneTimePurchaseOfferDetails candidate : offers) {
                        if (candidate.getRentalDetails() == null && candidate.getPreorderDetails() == null
                                && (candidate.getOfferId() == null || candidate.getOfferId().isEmpty())) { offer = candidate; break; }
                    }
                    if (offer == null || offer.getOfferToken() == null || offer.getOfferToken().isEmpty()) continue;
                    detailsById.put(product.getProductId(), product); offersById.put(product.getProductId(), offer.getOfferToken());
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
            if (client == null || !client.isReady()) { connect(queryCatalogRequested); return; }
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
        checkoutInFlight = false;
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
                if (purchase.getQuantity() != 1) { report("wallet_forbidden"); continue; }
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                    listener.onPurchasePending(productId); report("billing_pending");
                    // Register pending tokens with the server too: RTDN and its
                    // maintenance worker can later fulfill without this screen open.
                    listener.onPurchaseNeedsVerification(productId, purchase.getPurchaseToken());
                } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    // A Play callback is not proof of payment. Only the authenticated
                    // server may verify, durably grant once, then consume the purchase.
                    listener.onPurchaseNeedsVerification(productId, purchase.getPurchaseToken());
                    report("billing_verification_required");
                }
            }
        }
    }

    /** Explicit user checkout, with the exact displayed offer and a non-PII account binding. */
    public void launchPurchase(String productId) {
        onMain(() -> {
            if (closed || checkoutInFlight) return;
            if (CommerceConfig.goldAmount(productId) == 0) { report("billing_no_product"); return; }
            if (CommerceConfig.MODE_SANDBOX) { report("billing_test_only"); return; }
            if (!CommerceConfig.productionBillingReady() || BillingPolicy.serverBase(CommerceConfig.PURCHASE_VERIFIER_ENDPOINT).isEmpty()) { report("wallet_not_configured"); return; }
            String uid = ProductionWalletClient.currentUid(context);
            if (uid.isEmpty()) { report("wallet_sign_in_required"); return; }
            Activity owner = activity.get();
            if (owner == null || owner.isFinishing() || owner.isDestroyed()) return;
            ProductDetails details = detailsById.get(productId); String offer = offersById.get(productId);
            if (client == null || !client.isReady() || details == null || offer == null) { report("billing_no_product"); connectAndQuery(); return; }
            BillingFlowParams.ProductDetailsParams product = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details).setOfferToken(offer).build();
            BillingFlowParams params = BillingFlowParams.newBuilder().setProductDetailsParamsList(Collections.singletonList(product))
                    .setObfuscatedAccountId(BillingPolicy.accountHash(uid)).build();
            checkoutInFlight = true;
            BillingResult result = client.launchBillingFlow(owner, params);
            if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                checkoutInFlight = false;
                if (result.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) queryOwned();
                report(result.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED ? "billing_cancelled" : "billing_unavailable");
            }
        });
    }

    private void report(String key) { if (!closed) { status = key; listener.onStatus(key); } }
    private void onMain(Runnable action) { if (Looper.myLooper() == main.getLooper()) action.run(); else main.post(action); }
    public void close() {
        onMain(() -> {
            closed = true; generation++; connecting = false; checkoutInFlight = false; currentProducts = Collections.emptyList();
            detailsById.clear(); offersById.clear(); activity.clear();
            if (client != null) { client.endConnection(); client = null; }
        });
    }
}
