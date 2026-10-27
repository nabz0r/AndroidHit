package com.androidhit.lumadrift.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.androidhit.lumadrift.BuildConfig;
import com.androidhit.lumadrift.data.CosmeticCatalog;
import com.androidhit.lumadrift.data.GameStore;
import com.androidhit.lumadrift.data.V17Strings;
import com.androidhit.lumadrift.identity.AccountActivity;
import com.androidhit.lumadrift.ui.KawaiiArt;
import java.text.NumberFormat;
import java.util.*;

/** Six-language native account-cosmetic preview. Paid draws remain closed in this candidate. */
public final class CosmeticVaultActivity extends Activity {
    private static final int BG=0xff17182f,CARD=0xff2c2b4b,WHITE=0xfffff8f0,MINT=0xff97ead5,
            MUTED=0xffc4bfdc,GOLD=0xffffd78e,LILAC=0xffd3b9ff;
    private GameStore store;
    private CosmeticVaultClient client;
    private CosmeticVaultProtocol.Catalog catalog;
    private CosmeticVaultProtocol.Receipt receipt;
    private LinearLayout body;
    private boolean busy,destroyed;
    private long generation;
    private String viewedUid="",feedback="";
    private String t(String key,Object...args){return CosmeticVaultStrings.get(store.resolvedLanguage(),key,args);}
    private String itemName(CosmeticCatalog.Item item){return V17Strings.get(store.resolvedLanguage(),item.nameKey);}

    @Override public void onCreate(Bundle state){
        super.onCreate(state);store=new GameStore(this);client=new CosmeticVaultClient(this);
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);viewedUid=client.uid();render();
    }
    @Override public void onResume(){super.onResume();if(client!=null&&!viewedUid.equals(client.uid())){generation++;client.close();client=new CosmeticVaultClient(this);viewedUid=client.uid();catalog=null;receipt=null;feedback="";busy=false;render();}}
    private void render(){
        if(destroyed)return;
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        scroll.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{BG,0xff3a2d55,BG}));
        scroll.setOnApplyWindowInsetsListener((v,insets)->{
            if(Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);}
            else v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        FrameLayout center=new FrameLayout(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(22),dp(14),dp(22),dp(38));
        center.addView(body,new FrameLayout.LayoutParams(Math.min(getResources().getDisplayMetrics().widthPixels,dp(560)),-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL));
        scroll.addView(center);setContentView(scroll);
        button(body,t("back"),this::finish,MINT,true);
        label(body,t("eyebrow"),12,MINT);label(body,t("title"),31,WHITE);
        HeroArt hero=new HeroArt();body.addView(hero,new LinearLayout.LayoutParams(-1,dp(118)));
        label(body,t("subtitle"),16,MUTED);

        if(!BuildConfig.PAID_COSMETIC_DRAWS_ENABLED||catalog==null||!catalog.enabled){
            LinearLayout closed=card(body,0xff37304f);label(closed,"✦  "+t("closed"),21,GOLD);
            label(closed,t("closed_note"),14,WHITE);
        }
        if(!feedback.isEmpty())label(body,feedback,15,GOLD);
        if(busy)label(body,t("loading"),15,MINT);

        LinearLayout account=card(body,CARD);
        boolean signed=!client.uid().isEmpty();
        label(account,catalog!=null?t("balance",catalog.balance):receipt!=null?t("balance",receipt.balance):t("balance_unknown"),20,WHITE);
        label(account,t("terms"),14,MUTED);
        if(!signed){label(account,t("sign_in_note"),14,MUTED);button(account,t("sign_in"),()->startActivity(new Intent(this,AccountActivity.class)),MINT,!busy);}
        button(account,t(catalog==null?"load":"refresh"),this::load,MINT,signed&&!busy&&CosmeticVaultClient.configured());
        if(!CosmeticVaultClient.configured())label(account,t("error_configuration"),14,GOLD);
        String pending=client.pendingRequest();
        if(!pending.isEmpty())label(account,t("pending"),14,GOLD);
        boolean allowed=CosmeticVaultProtocol.canDraw(BuildConfig.PAID_COSMETIC_DRAWS_ENABLED,catalog,client.uid(),!pending.isEmpty());
        button(account,t(!pending.isEmpty()?"retry":allowed?"draw":"closed"),this::confirmDraw,LILAC,!busy&&allowed);

        if(receipt!=null){
            LinearLayout result=card(body,0xff314d4d);CosmeticCatalog.Item item=CosmeticCatalog.find(receipt.itemId);
            result.addView(new ItemArt(item),new LinearLayout.LayoutParams(-1,dp(82)));
            label(result,t(receipt.duplicate?"duplicate":"new_item"),20,MINT);label(result,itemName(item),18,WHITE);label(result,t("result_note"),14,MUTED);
        }
        LinearLayout odds=card(body,CARD);label(odds,t("guarantee"),16,WHITE);
        if(catalog!=null){label(odds,catalog.guaranteeNext()?t("next_guaranteed"):t("pity",catalog.pity),17,MINT);label(odds,t("odds_note"),14,MUTED);}
        else label(odds,t("unknown"),14,MUTED);
        label(odds,t("account_note"),13,MUTED);
        label(body,catalog==null?t("preview_collection"):t("owned_count",catalog.owned.size()),22,WHITE);

        int columns=getResources().getConfiguration().screenWidthDp>=480?2:1;
        GridLayout grid=new GridLayout(this);grid.setColumnCount(columns);body.addView(grid,new LinearLayout.LayoutParams(-1,-2));
        Map<String,Double> probabilities=new HashMap<>();if(catalog!=null)for(CosmeticVaultProtocol.Entry entry:catalog.entries)probabilities.put(entry.item.id,entry.probability);
        int index=0;
        for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS){
            LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setPadding(dp(14),dp(11),dp(14),dp(16));
            boolean owned=catalog!=null&&catalog.owned.contains(item.id);tile.setBackground(shape(owned?0xff314a4c:CARD,owned?0xff6aa69b:0xff514666,20));
            GridLayout.LayoutParams layout=new GridLayout.LayoutParams(GridLayout.spec(index/columns),GridLayout.spec(index%columns,1f));
            layout.width=0;layout.height=-2;layout.setMargins(dp(index%columns==0?0:7),dp(9),dp(columns==2&&index%columns==0?7:0),dp(3));grid.addView(tile,layout);
            tile.addView(new ItemArt(item),new LinearLayout.LayoutParams(-1,dp(76)));
            label(tile,itemName(item),17,WHITE);label(tile,V17Strings.get(store.resolvedLanguage(),item.rarity.name().toLowerCase(Locale.ROOT)),12,LILAC);
            label(tile,catalog==null?t("unknown"):t(owned?"owned":"unowned"),13,owned?MINT:MUTED);
            if(catalog!=null)label(tile,probability(probabilities.get(item.id)),22,GOLD);
            index++;
        }
    }
    private void load(){
        if(busy||destroyed)return;busy=true;feedback="";render();
        final long request=++generation;
        client.catalog(new CosmeticVaultClient.Callback<CosmeticVaultProtocol.Catalog>(){
            public void onSuccess(CosmeticVaultProtocol.Catalog value){if(destroyed||request!=generation)return;catalog=value;viewedUid=value.uid;busy=false;feedback="";render();}
            public void onError(String code){if(destroyed||request!=generation)return;busy=false;if("VAULT_SIGN_IN_REQUIRED".equals(code)){catalog=null;receipt=null;}feedback=error(code);render();}
        });
    }
    private void confirmDraw(){
        String pending=client.pendingRequest();
        if(busy||!CosmeticVaultProtocol.canDraw(BuildConfig.PAID_COSMETIC_DRAWS_ENABLED,catalog,client.uid(),!pending.isEmpty()))return;
        StringBuilder message=new StringBuilder(pending.isEmpty()?t("confirm_note"):t("pending"));
        message.append("\n\n").append(t("terms")).append("\n\n").append(t("guarantee"));
        if(pending.isEmpty())for(CosmeticVaultProtocol.Entry entry:catalog.entries)message.append("\n").append(itemName(entry.item)).append(" · ").append(probability(entry.probability));
        TextView terms=new TextView(this);terms.setText(message);terms.setTextSize(15);terms.setPadding(dp(22),dp(12),dp(22),dp(16));
        ScrollView scrolling=new ScrollView(this);scrolling.addView(terms);
        new AlertDialog.Builder(this).setTitle(t("confirm_title")).setView(scrolling)
                .setNegativeButton(t("cancel"),null).setPositiveButton(t(pending.isEmpty()?"confirm":"retry"),(dialog,which)->draw()).show();
    }
    private void draw(){
        if(busy||destroyed)return;busy=true;feedback="";render();
        final long request=++generation;
        client.draw(catalog,new CosmeticVaultClient.Callback<CosmeticVaultProtocol.Receipt>(){
            public void onSuccess(CosmeticVaultProtocol.Receipt result){if(destroyed||request!=generation)return;receipt=result;catalog=null;busy=false;feedback="";render();}
            public void onError(String code){if(destroyed||request!=generation)return;busy=false;if("VAULT_SIGN_IN_REQUIRED".equals(code)){catalog=null;receipt=null;}feedback=error(code);render();}
        });
    }
    private String probability(double value){NumberFormat format=NumberFormat.getPercentInstance(Locale.forLanguageTag(store.resolvedLanguage()));format.setMinimumFractionDigits(0);format.setMaximumFractionDigits(3);return format.format(value);}
    private String error(String code){
        switch(code){case"VAULT_SIGN_IN_REQUIRED":case"GOOGLE_LOGIN_REQUIRED":case"UNAUTHENTICATED":return t("error_auth");
            case"VAULT_NOT_CONFIGURED":return t("error_configuration");case"NOT_FOUND":return t("error_not_deployed");
            case"PAID_COSMETIC_DRAWS_DISABLED":return t("closed");case"INSUFFICIENT_GOLD":return t("error_insufficient");
            case"VAULT_INVALID_RESPONSE":return t("error_invalid");case"VAULT_LOCAL_SAVE_FAILED":return t("error_save");default:return t("error_unavailable");}
    }
    private LinearLayout card(LinearLayout parent,int color){LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(18),dp(10),dp(18),dp(18));card.setBackground(shape(color,0xff514666,23));LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);params.topMargin=dp(15);parent.addView(card,params);return card;}
    private GradientDrawable shape(int fill,int border,int radius){GradientDrawable background=new GradientDrawable();background.setColor(fill);background.setCornerRadius(dp(radius));background.setStroke(dp(1),border);return background;}
    private TextView label(LinearLayout parent,String value,int size,int color){TextView text=new TextView(this);text.setText(value);text.setTextColor(color);text.setTextSize(size);text.setTypeface(Typeface.create("sans-serif",size>=20?Typeface.BOLD:Typeface.NORMAL));text.setLineSpacing(dp(3),1);text.setPadding(0,dp(8),0,dp(5));if(Build.VERSION.SDK_INT>=28&&size>=20)text.setAccessibilityHeading(true);parent.addView(text);return text;}
    private void button(LinearLayout parent,String value,Runnable action,int color,boolean enabled){Button button=new Button(this);button.setText(value);button.setAllCaps(false);button.setTextSize(15);button.setMinHeight(dp(48));button.setPadding(dp(13),dp(8),dp(13),dp(8));button.setTextColor(enabled?BG:MUTED);button.setBackground(shape(enabled?color:0xff403b59,enabled?color:0xff645a7b,15));button.setEnabled(enabled);button.setOnClickListener(v->action.run());LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);params.topMargin=dp(10);parent.addView(button,params);}
    private int dp(float value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private int color(CosmeticCatalog.Item item){if(item.id.contains("pumpkin")||item.id.contains("witch"))return 0xffffb88b;if(item.id.contains("cupid")||item.id.contains("valentine"))return 0xffffb3d3;if(item.id.contains("star"))return GOLD;if(item.id.contains("moon"))return LILAC;if(item.id.contains("aurora"))return 0xff9adaff;return MINT;}
    private final class ItemArt extends View {
        private final CosmeticCatalog.Item item;private final KawaiiArt art=new KawaiiArt();
        ItemArt(CosmeticCatalog.Item item){super(CosmeticVaultActivity.this);this.item=item;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);art.cosmetic(canvas,item.slot.name(),CosmeticCatalog.ITEMS.indexOf(item)%6,getWidth()/2f,getHeight()*.53f,dp(27),color(item));}
    }
    private final class HeroArt extends View {
        private final KawaiiArt art=new KawaiiArt();
        HeroArt(){super(CosmeticVaultActivity.this);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float x=getWidth()/2f,y=dp(62),spread=Math.min(dp(88),getWidth()*.30f),radius=Math.min(dp(27),getWidth()*.11f);art.power(canvas,"trail",x,y,dp(43),0,true);art.cosmetic(canvas,"HAT",1,x-spread,y+dp(9),radius,MINT);art.cosmetic(canvas,"DRESS",3,x+spread,y+dp(4),radius,LILAC);}
    }
    @Override protected void onDestroy(){destroyed=true;generation++;if(client!=null)client.close();super.onDestroy();}
}
