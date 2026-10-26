package com.androidhit.lumadrift.commerce;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Shared non-Android validation for the payment boundary. Never accepts client prices. */
public final class BillingPolicy {
    private BillingPolicy() { }

    public static String accountHash(String uid) {
        if (uid == null || uid.isEmpty() || uid.length() > 128) throw new IllegalArgumentException("Authenticated UID required");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(uid.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder(64);
            for (byte value : digest) out.append(Character.forDigit((value & 255) >>> 4, 16)).append(Character.forDigit(value & 15, 16));
            return out.toString();
        } catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }

    /** Bearer tokens may only be sent to an explicit HTTPS origin, with no user info or query. */
    public static String serverBase(String candidate) {
        if (candidate == null || candidate.isBlank()) return "";
        try {
            URI url = new URI(candidate);
            if (!"https".equals(url.getScheme()) || url.getHost() == null || url.getUserInfo() != null
                    || url.getQuery() != null || url.getFragment() != null || url.getPort() != -1 && url.getPort() != 443
                    || url.getPath() != null && !url.getPath().isEmpty() && !"/".equals(url.getPath())) return "";
            return "https://" + url.getHost();
        } catch (Exception invalid) { return ""; }
    }

    public static boolean validRequestId(String requestId) {
        return requestId != null && requestId.matches("[a-zA-Z0-9_:-]{8,160}");
    }
}
