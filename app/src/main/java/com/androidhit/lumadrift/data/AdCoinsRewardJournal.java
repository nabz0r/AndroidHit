package com.androidhit.lumadrift.data;

import android.content.SharedPreferences;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/** Device-local earned ad Coins only; never a premium-wallet or payment receipt. */
final class AdCoinsRewardJournal {
    static final int COINS = 35;
    private static final int MAX_COINS = 2_000_000_000;
    private static final String PENDING = "ad_coins_pending_v1_";
    // A first failed journal write stays replayable while this process lives.
    // Process death before any durable write cannot recover an SDK-only callback.
    private static final Map<SharedPreferences, Set<String>> UNRECORDED = new WeakHashMap<>();
    private final SharedPreferences prefs;
    AdCoinsRewardJournal(SharedPreferences prefs) { this.prefs = prefs; }

    void record(String showId) {
        if (!valid(showId)) throw new IllegalArgumentException("Earned ad show UUID required");
        synchronized (LocalSaveTransactions.LOCK) {
            UNRECORDED.computeIfAbsent(prefs, key -> new LinkedHashSet<>()).add(showId);
            recordRemembered();
        }
    }

    boolean hasPending() {
        synchronized (LocalSaveTransactions.LOCK) {
            Set<String> remembered = UNRECORDED.get(prefs);
            if (remembered != null && !remembered.isEmpty()) return true;
            return !pending().isEmpty();
        }
    }

    int replay() {
        synchronized (LocalSaveTransactions.LOCK) {
            recordRemembered();
            int credited = 0;
            for (String showId : pending()) {
                String digest = hash(showId), receipt = "gold_tx_v1_" + digest;
                Map<String, Object> write = new HashMap<>();
                if (!prefs.getBoolean(receipt, false)) {
                    int balance = Math.max(0, prefs.getInt("coins", 0));
                    // A full wallet keeps its earned reward pending; never clip it.
                    if (balance > MAX_COINS - COINS) continue;
                    write.put("coins", balance + COINS);
                    write.put(receipt, true);
                    credited += COINS;
                }
                // Credit, receipt and acknowledgement use one exact transaction.
                write.put(PENDING + digest, "");
                // A failed Android commit may already hide the pending row in
                // memory. Keep the intent visible until its exact flush succeeds.
                UNRECORDED.computeIfAbsent(prefs, key -> new LinkedHashSet<>()).add(showId);
                LocalSaveTransactions.write(prefs, write);
                UNRECORDED.get(prefs).remove(showId);
                if (UNRECORDED.get(prefs).isEmpty()) UNRECORDED.remove(prefs);
            }
            return credited;
        }
    }

    private void recordRemembered() {
        LocalSaveTransactions.flush(prefs);
        Set<String> remembered = UNRECORDED.get(prefs);
        if (remembered == null || remembered.isEmpty()) return;
        Map<String, Object> write = new HashMap<>();
        for (String showId : remembered) {
            String digest = hash(showId);
            if (!prefs.getBoolean("gold_tx_v1_" + digest, false)) write.put(PENDING + digest, showId);
        }
        LocalSaveTransactions.write(prefs, write);
        UNRECORDED.remove(prefs);
    }

    private ArrayList<String> pending() {
        ArrayList<String> result = new ArrayList<>();
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            if (!entry.getKey().startsWith(PENDING) || "".equals(entry.getValue())) continue;
            if (!(entry.getValue() instanceof String)) throw new GameStore.LocalSaveException("Invalid earned ad journal");
            String showId = (String) entry.getValue();
            if (!valid(showId) || !entry.getKey().equals(PENDING + hash(showId)))
                throw new GameStore.LocalSaveException("Invalid earned ad journal");
            result.add(showId);
        }
        Collections.sort(result);
        return result;
    }

    private static boolean valid(String showId) {
        return showId != null && showId.matches("ad:[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
    }
    private static String hash(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (byte part : bytes) out.append(Character.forDigit((part & 255) >>> 4, 16)).append(Character.forDigit(part & 15, 16));
            return out.toString();
        } catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
}
