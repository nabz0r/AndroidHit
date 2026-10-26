package com.androidhit.lumadrift.wallet;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.androidhit.lumadrift.commerce.BillingPolicy;
import com.androidhit.lumadrift.commerce.CommerceConfig;
import com.androidhit.lumadrift.identity.IdentityConfig;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Authenticated server wallet. It never reads or modifies locally earned/test coins. */
public final class ProductionWalletClient {
    public interface Callback {
        void onSuccess(Wallet result);
        void onError(String statusKey);
    }
    public static final class Wallet {
        public final long balance, version;
        public final String uid;
        public final JSONObject inventory;
        public final org.json.JSONArray characters, skins;
        /** Server issued entitlement; absent for balance and receipt verification requests. */
        public final JSONObject entitlement;
        public final String purchaseStatus;
        public final boolean consumptionPending;
        private Wallet(String uid, JSONObject response) throws Exception {
            this.uid = uid;
            balance = exactLong(response, "balance"); version = exactLong(response, "version");
            if (balance < -1000000000L || balance > 1000000000L || version < 0) throw new IllegalArgumentException("Invalid wallet");
            characters = response.getJSONArray("characters"); skins = response.getJSONArray("skins");
            inventory = response.optJSONObject("inventory");
            if (inventory == null) throw new IllegalArgumentException("Missing inventory");
            entitlement = response.optJSONObject("entitlement");
            JSONObject purchase = response.optJSONObject("purchase");
            purchaseStatus = purchase == null ? "" : purchase.optString("status", "");
            consumptionPending = purchase != null && purchase.optBoolean("consumptionPending", false);
        }
        private static long exactLong(JSONObject json, String key) throws Exception {
            Object value = json.get(key);
            if (!(value instanceof Number) || value instanceof Float || value instanceof Double) throw new IllegalArgumentException("Invalid integer");
            return ((Number)value).longValue();
        }
    }
    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newFixedThreadPool(2);
    private volatile boolean closed;
    private volatile long generation;

    public ProductionWalletClient(Context context) { this.context = context.getApplicationContext(); }
    public boolean isConfigured() { return CommerceConfig.productionBillingReady() && !BillingPolicy.serverBase(CommerceConfig.PURCHASE_VERIFIER_ENDPOINT).isEmpty(); }
    public String uid() { return currentUid(context); }
    public boolean isSignedIn() { return !uid().isEmpty(); }

    /** Restores the optional named Firebase instance only after an earlier explicit sign-in. */
    private static FirebaseUser currentUser(Context context) {
        try {
            FirebaseApp app;
            try { app = FirebaseApp.getInstance("LumaIdentity"); }
            catch (IllegalStateException missing) {
                if (!context.getSharedPreferences("luma_identity_state_v1", 0).getBoolean("previously_signed_in", false)) return null;
                IdentityConfig config = IdentityConfig.load(context);
                if (!config.isConfigured()) return null;
                synchronized (ProductionWalletClient.class) {
                    try { app = FirebaseApp.getInstance("LumaIdentity"); }
                    catch (IllegalStateException absent) {
                        app = FirebaseApp.initializeApp(context.getApplicationContext(), new FirebaseOptions.Builder()
                                .setApiKey(config.apiKey).setApplicationId(config.appId).setProjectId(config.projectId).build(), "LumaIdentity");
                    }
                }
                app.setDataCollectionDefaultEnabled(false);
            }
            FirebaseUser user = FirebaseAuth.getInstance(app).getCurrentUser();
            if (user == null || user.isAnonymous()) return null;
            for (com.google.firebase.auth.UserInfo provider : user.getProviderData()) if ("google.com".equals(provider.getProviderId())) return user;
            return null;
        } catch (RuntimeException unavailable) { return null; }
    }
    private static final Object CACHE_LOCK = new Object();
    private static final String CACHE_PREFS = "luma_server_wallet_v1";
    public static boolean ownsCharacter(Context context, String id) { return owns(context, "characters", id); }
    public static boolean ownsSkin(Context context, String id) { return owns(context, "skins", id); }
    private static boolean owns(Context context, String kind, String id) {
        if (CommerceConfig.MODE_SANDBOX || id == null || !id.matches("[a-z]{1,24}")) return false;
        String uid = currentUid(context); if (uid.isEmpty()) return false;
        synchronized (CACHE_LOCK) {
            try {
                String saved = context.getSharedPreferences(CACHE_PREFS, 0).getString(BillingPolicy.accountHash(uid), "");
                org.json.JSONArray ids = new JSONObject(saved).getJSONArray(kind);
                for (int i = 0; i < ids.length(); i++) if (id.equals(ids.getString(i))) return true;
            } catch (Exception absent) { return false; }
        }
        return false;
    }
    public static void clearCache(Context context, String uid) {
        if (uid == null || uid.isEmpty()) return;
        synchronized (CACHE_LOCK) { context.getSharedPreferences(CACHE_PREFS, 0).edit().remove(BillingPolicy.accountHash(uid)).commit(); }
    }
    private void cache(Wallet wallet) {
        synchronized (CACHE_LOCK) {
            try {
                android.content.SharedPreferences prefs = context.getSharedPreferences(CACHE_PREFS, 0);
                String key = BillingPolicy.accountHash(wallet.uid), previous = prefs.getString(key, "");
                if (!previous.isEmpty() && new JSONObject(previous).optLong("version", -1) > wallet.version) return;
                JSONObject value = new JSONObject().put("version", wallet.version).put("characters", wallet.characters).put("skins", wallet.skins);
                prefs.edit().putString(key, value.toString()).commit();
            } catch (Exception ignored) { /* A fresh server fetch restores this optional local cosmetic cache. */ }
        }
    }
    public static String currentUid(Context context) { FirebaseUser user = currentUser(context); return user == null ? "" : user.getUid(); }
    public void refresh(Callback callback) { request("GET", "/v1/wallet", null, callback); }
    public void verify(String productId, String purchaseToken, Callback callback) {
        if (CommerceConfig.goldAmount(productId) == 0 || purchaseToken == null || purchaseToken.isEmpty() || purchaseToken.length() > 4096) {
            fail(callback, "wallet_invalid_response"); return;
        }
        try { request("POST", "/v1/purchases/verify", new JSONObject().put("productId", productId).put("purchaseToken", purchaseToken), callback); }
        catch (Exception invalid) { fail(callback, "wallet_invalid_response"); }
    }
    public void spend(String sku, String requestId, Callback callback) {
        if (!BillingPolicy.validRequestId(requestId) || sku == null || !sku.matches("[a-z0-9_:]{1,80}")) { fail(callback, "wallet_invalid_response"); return; }
        try { request("POST", "/v1/wallet/spend", new JSONObject().put("requestId", requestId).put("sku", sku), callback); }
        catch (Exception invalid) { fail(callback, "wallet_invalid_response"); }
    }
    public void deleteAccount(Callback callback) {
        final String uid = uid();
        request("DELETE", "/v1/account", null, new Callback() {
            public void onSuccess(Wallet ignored) { clearCache(context, uid); callback.onSuccess(null); }
            public void onError(String statusKey) { callback.onError(statusKey); }
        });
    }

    private void request(String method, String path, JSONObject body, Callback callback) {
        if (callback == null) throw new IllegalArgumentException("Callback required");
        if (closed) return;
        if (!isConfigured()) { fail(callback, "wallet_not_configured"); return; }
        FirebaseUser user = currentUser(context);
        if (user == null) { fail(callback, "wallet_sign_in_required"); return; }
        final String uid = user.getUid(); final long request = generation;
        user.getIdToken(false).addOnCompleteListener(task -> {
            if (!current(request, uid)) { if (!closed) fail(callback, "wallet_sign_in_required"); return; }
            if (!task.isSuccessful() || task.getResult() == null || task.getResult().getToken() == null) { fail(callback, "wallet_sign_in_required"); return; }
            final String token = task.getResult().getToken();
            try { io.execute(() -> execute(method, path, body, token, uid, request, callback)); }
            catch (java.util.concurrent.RejectedExecutionException stopped) { if (!closed) fail(callback, "wallet_unavailable"); }
        });
    }
    private void execute(String method, String path, JSONObject body, String token, String uid, long request, Callback callback) {
        HttpURLConnection connection = null;
        try {
            if (!current(request, uid)) return;
            URL endpoint = new URL(BillingPolicy.serverBase(CommerceConfig.PURCHASE_VERIFIER_ENDPOINT) + path);
            connection = (HttpURLConnection) endpoint.openConnection();
            // Never forward a Firebase bearer token across a redirect.
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(12000); connection.setReadTimeout(20000);
            connection.setRequestMethod(method); connection.setUseCaches(false);
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Accept", "application/json");
            if (body != null) {
                byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setDoOutput(true); connection.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream out = connection.getOutputStream()) { out.write(bytes); }
            }
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                String error = status == 401 ? "wallet_sign_in_required" : status == 402 ? "wallet_insufficient" : status == 409 ? "wallet_conflict" : status == 403 ? "wallet_forbidden" : "wallet_unavailable";
                if (status == 409 || status == 401) {
                    try (InputStream input = connection.getErrorStream()) {
                        if (input != null) {
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream(); byte[] part = new byte[1024]; int count;
                            while ((count = input.read(part)) != -1 && bytes.size() < 8192) bytes.write(part, 0, Math.min(count, 8192 - bytes.size()));
                            JSONObject detail = new JSONObject(bytes.toString(StandardCharsets.UTF_8.name())).optJSONObject("error");
                            String code = detail == null ? "" : detail.optString("code");
                            if ("INSUFFICIENT_GOLD".equals(code)) error = "wallet_insufficient";
                            if ("RECENT_LOGIN_REQUIRED".equals(code)) error = "account_recent_login";
                        }
                    } catch (Exception ignored) { /* Keep a safe localized status. */ }
                }
                deliverError(request, uid, callback, error); return;
            }
            if ("DELETE".equals(method)) {
                main.post(() -> {
                    if (closed || request != generation) return;
                    clearCache(context, uid);
                    String activeUid = currentUid(context);
                    if (activeUid.isEmpty() || uid.equals(activeUid)) callback.onSuccess(null);
                    else callback.onError("wallet_sign_in_required");
                }); return;
            }
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            try (InputStream input = connection.getInputStream()) {
                byte[] buffer = new byte[4096]; int n;
                while ((n = input.read(buffer)) != -1) { if (data.size() + n > 131072) throw new IllegalArgumentException("Oversized response"); data.write(buffer, 0, n); }
            }
            Wallet wallet = new Wallet(uid, new JSONObject(data.toString(StandardCharsets.UTF_8.name())));
            main.post(() -> { if (current(request, uid)) { cache(wallet); callback.onSuccess(wallet); } });
        } catch (IllegalArgumentException malformed) { deliverError(request, uid, callback, "wallet_invalid_response"); }
        catch (Exception unavailable) { deliverError(request, uid, callback, "wallet_unavailable"); }
        finally { if (connection != null) connection.disconnect(); }
    }
    private boolean current(long request, String uid) { return !closed && request == generation && uid.equals(currentUid(context)); }
    private void deliverError(long request, String uid, Callback callback, String key) { main.post(() -> { if (current(request, uid)) callback.onError(key); }); }
    private void fail(Callback callback, String key) { main.post(() -> { if (!closed) callback.onError(key); }); }
    public void close() { closed = true; generation++; io.shutdownNow(); }
}
