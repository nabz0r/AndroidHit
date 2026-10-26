package com.androidhit.lumadrift.wallet;

import android.app.Activity;
import com.androidhit.lumadrift.commerce.CommerceConfig;
import com.androidhit.lumadrift.commerce.PlayGoldBilling;

/** Tests the real controller against explicit lifecycle and provider test doubles. */
public final class WalletRecoveryTest {
    private static int checks;
    private static void check(boolean condition,String label){checks++;if(!condition)throw new AssertionError(label);}
    private static void reset(){CommerceConfig.MODE_SANDBOX=false;CommerceConfig.ready=true;ProductionWalletClient.currentUid="google-A";ProductionWalletClient.configured=true;ProductionWalletClient.created.clear();PlayGoldBilling.created.clear();}
    public static void main(String[] args){
        reset();int[] updates={0};Activity owner=new Activity();
        WalletRecoveryController c=new WalletRecoveryController(owner,()->updates[0]++);
        CommerceConfig.MODE_SANDBOX=true;c.onForeground();check(ProductionWalletClient.created.isEmpty()&&PlayGoldBilling.created.isEmpty(),"Lab has no recovery network");
        CommerceConfig.MODE_SANDBOX=false;CommerceConfig.ready=false;c.onForeground();check(ProductionWalletClient.created.isEmpty(),"No endpoint means no network");
        CommerceConfig.ready=true;ProductionWalletClient.currentUid="";c.onForeground();check(ProductionWalletClient.created.isEmpty(),"Guest has no network");
        ProductionWalletClient.currentUid="google-A";c.onForeground();
        ProductionWalletClient a=ProductionWalletClient.created.get(0);PlayGoldBilling ba=PlayGoldBilling.created.get(0);
        check(a.refreshes.size()==1&&ba.connects==1,"Signed account refreshes and observes owned purchases");
        ba.listener.onPurchaseNeedsVerification("luma_gold_500","token-A");ba.listener.onPurchaseNeedsVerification("luma_gold_500","token-A");
        check(a.tokens.size()==1,"Duplicate in-flight token verified once");
        a.verifications.get(0).onError("wallet_unavailable");ba.listener.onPurchaseNeedsVerification("luma_gold_500","token-A");check(a.tokens.size()==2,"Failed verification can retry");
        a.verifications.get(1).onSuccess(new ProductionWalletClient.Wallet());check(updates[0]==1,"Fresh verification updates collection");
        a.refreshes.get(0).onSuccess(new ProductionWalletClient.Wallet());check(updates[0]==2,"Fresh wallet updates collection");
        c.onBackground();check(a.closed&&ba.closed,"Background closes both provider owners");
        ba.listener.onPurchaseNeedsVerification("luma_gold_500","late-token");a.refreshes.get(0).onSuccess(new ProductionWalletClient.Wallet());
        check(a.tokens.size()==2&&updates[0]==2,"Background callbacks cannot perform or apply work");
        ProductionWalletClient.currentUid="google-B";c.onForeground();ProductionWalletClient b=ProductionWalletClient.created.get(1);PlayGoldBilling bb=PlayGoldBilling.created.get(1);
        check(b.refreshes.size()==1&&bb.connects==1,"New foreground account gets isolated session");
        ba.listener.onPurchaseNeedsVerification("luma_gold_500","cross-account");a.verifications.get(0).onSuccess(new ProductionWalletClient.Wallet());check(b.tokens.isEmpty()&&updates[0]==2,"Previous account callbacks do not cross sessions");
        ProductionWalletClient.currentUid="google-C";bb.listener.onPurchaseNeedsVerification("luma_gold_500","stale-B");b.refreshes.get(0).onSuccess(new ProductionWalletClient.Wallet());
        check(b.tokens.isEmpty()&&updates[0]==2,"UID change is checked independently of pause");
        c.onForeground();check(b.closed&&bb.closed&&ProductionWalletClient.created.size()==3,"UID change replaces previous session");
        ProductionWalletClient d=ProductionWalletClient.created.get(2);PlayGoldBilling bd=PlayGoldBilling.created.get(2);
        owner.finishing=true;bd.listener.onPurchaseNeedsVerification("luma_gold_500","finishing");check(d.tokens.isEmpty(),"Finishing owner cannot verify");owner.finishing=false;
        c.close();check(d.closed&&bd.closed,"Destroy closes active provider owners");c.onForeground();check(ProductionWalletClient.created.size()==3,"Closed controller cannot restart");
        d.refreshes.get(0).onSuccess(new ProductionWalletClient.Wallet());check(updates[0]==2,"Destroyed owner cannot apply response");
        reset();ProductionWalletClient.configured=false;WalletRecoveryController invalid=new WalletRecoveryController(new Activity(),()->{});invalid.onForeground();
        check(ProductionWalletClient.created.get(0).closed&&PlayGoldBilling.created.isEmpty(),"Invalid HTTPS adapter configuration never starts Play");invalid.close();
        System.out.println("Wallet recovery lifecycle: "+checks+" checks passed (provider doubles; no Google transaction)");
    }
}
