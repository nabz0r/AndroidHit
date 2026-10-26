package com.androidhit.lumadrift.commerce;

import java.lang.reflect.Field;
import java.util.Map;

/** Credential destination, stable account binding and translated production UI checks. */
public final class BillingPolicyTest {
    private static int checks;
    private static void check(boolean valid, String label) { checks++; if (!valid) throw new AssertionError(label); }
    public static void main(String[] args) throws Exception {
        check("4ab08c5d68eeb18c08df44084fd659b3945ca897720de9a8bce5301bd7d2360d".equals(BillingPolicy.accountHash("firebase-user-123")), "Published UID binding vector");
        check(!BillingPolicy.accountHash("a").equals(BillingPolicy.accountHash("A")), "UID case preserved");
        for (String uid : new String[] {null,"","a".repeat(129)}) {
            boolean rejected=false;try { BillingPolicy.accountHash(uid); } catch (IllegalArgumentException expected) { rejected=true; }
            check(rejected,"Invalid UID refused");
        }
        check("https://wallet.example.com".equals(BillingPolicy.serverBase("https://wallet.example.com/")), "Canonical HTTPS origin");
        check("https://wallet.example.com".equals(BillingPolicy.serverBase("https://wallet.example.com:443")), "Standard HTTPS port");
        String[] blocked = {null,"","http://wallet.example.com","https://user:secret@wallet.example.com","https://wallet.example.com?token=x", "https://wallet.example.com#x", "https://wallet.example.com:8080", "https://wallet.example.com/v1", "https://wallet.example.com/%2e%2e", "//wallet.example.com", "file:///tmp/wallet", "javascript:alert(1)", "https:///missing-host", "https://wallet.example.com\\@evil.example", "https://wallet.example.com/\n"};
        for (String origin : blocked) check(BillingPolicy.serverBase(origin).isEmpty(),"Unsafe bearer destination refused: "+origin);
        check(BillingPolicy.validRequestId("89c57d7c-5ed9-4d35-83fe-365761f37df9"),"Durable UUID request supported");
        check(BillingPolicy.validRequestId("a".repeat(160)),"Maximum request supported");
        for (String id : new String[] {null,"","a","a".repeat(161),"request/escape","request\nheader","requête_01"}) check(!BillingPolicy.validRequestId(id),"Malformed request rejected");
        Field field=WalletStrings.class.getDeclaredField("COPY");field.setAccessible(true);
        @SuppressWarnings("unchecked") Map<String,String[]> translations=(Map<String,String[]>)field.get(null);
        String[] languages={"en","fr","de","es","pt","it"};
        for (Map.Entry<String,String[]> entry:translations.entrySet()) {
            check(entry.getValue().length==6,"All languages for "+entry.getKey());
            for (String language:languages) {
                String key=entry.getKey();Object[] values=key.equals("wallet_balance")||key.equals("wallet_local")?new Object[]{123L}:key.equals("wallet_buy")||key.equals("wallet_confirm")?new Object[]{"Nova",123}:key.equals("wallet_owned")?new Object[]{"Nova"}:new Object[0];
                String text=WalletStrings.get(language,key,values);
                check(!text.isBlank()&&!text.equals(key),"Translated usable copy "+language+":"+key);
            }
        }
        System.out.println("Billing policy and production copy: "+checks+" checks passed");
    }
}
