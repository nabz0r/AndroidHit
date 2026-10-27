package com.androidhit.lumadrift.social;

import com.androidhit.lumadrift.commerce.BillingPolicy;
import com.androidhit.lumadrift.data.CosmeticCatalog;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

/** Strict account-cosmetic wire contract; independent of Android and local earned currency. */
final class CosmeticVaultProtocol {
    static final int PRICE=100, DUPLICATE_REFUND=50, GUARANTEE=20;
    static final class Entry {
        final CosmeticCatalog.Item item;
        final double probability;
        Entry(CosmeticCatalog.Item item,double probability){this.item=item;this.probability=probability;}
    }
    static final class Catalog {
        final String uid;
        final boolean enabled;
        final long balance,version;
        final int pity;
        final Set<String> owned;
        final List<Entry> entries;
        Catalog(String uid,boolean enabled,long balance,long version,int pity,Set<String> owned,List<Entry> entries){
            this.uid=uid;this.enabled=enabled;this.balance=balance;this.version=version;this.pity=pity;
            this.owned=Collections.unmodifiableSet(owned);this.entries=Collections.unmodifiableList(entries);
        }
        boolean guaranteeNext(){return pity==GUARANTEE-1&&owned.size()<CosmeticCatalog.ITEMS.size();}
    }
    static final class Receipt {
        final String uid,id,itemId;
        final boolean duplicate;
        final long balance,version;
        final int pity,refund;
        final Set<String> owned;
        Receipt(String uid,String id,String itemId,boolean duplicate,long balance,long version,int pity,int refund,Set<String> owned){
            this.uid=uid;this.id=id;this.itemId=itemId;this.duplicate=duplicate;this.balance=balance;
            this.version=version;this.pity=pity;this.refund=refund;this.owned=Collections.unmodifiableSet(owned);
        }
    }
    private CosmeticVaultProtocol(){}
    static boolean canDraw(boolean buildEnabled,Catalog catalog,String currentUid){
        return canDraw(buildEnabled,catalog,currentUid,false);
    }
    static boolean canDraw(boolean buildEnabled,Catalog catalog,String currentUid,boolean resumeReceipt){
        return buildEnabled&&catalog!=null&&catalog.enabled&&catalog.uid.equals(currentUid)
                &&!currentUid.isEmpty()&&(resumeReceipt||catalog.balance>=PRICE);
    }
    static Catalog catalog(String uid,JSONObject value)throws Exception{
        requireUid(uid);
        if(!"LumaGold".equals(value.getString("currency"))||integer(value,"price")!=PRICE
                ||integer(value,"duplicateRefund")!=DUPLICATE_REFUND||integer(value,"guarantee")!=GUARANTEE
                ||bool(value,"gameplayBenefit"))throw new IllegalArgumentException("Unsupported cosmetic terms");
        boolean enabled=bool(value,"enabled");
        long balance=balance(value),version=version(value);int pity=pity(value);
        Set<String> owned=owned(value.getJSONArray("owned"));
        JSONArray items=value.getJSONArray("items");
        if(items.length()!=CosmeticCatalog.ITEMS.size())throw new IllegalArgumentException("Incomplete catalog");
        HashSet<String> seen=new HashSet<>();ArrayList<Entry> entries=new ArrayList<>();
        boolean guarantee=pity==GUARANTEE-1&&owned.size()<items.length();
        int total=0;for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS)if(!guarantee||!owned.contains(item.id))total+=item.chanceBasisPoints();
        double sum=0;
        for(int i=0;i<items.length();i++){
            JSONObject raw=items.getJSONObject(i);String id=raw.getString("id");CosmeticCatalog.Item item=CosmeticCatalog.find(id);
            if(item==null||!seen.add(id)||!item.slot.name().toLowerCase(Locale.ROOT).equals(raw.getString("slot"))
                    ||!item.rarity.name().equals(raw.getString("rarity"))||integer(raw,"weight")!=item.chanceBasisPoints())
                throw new IllegalArgumentException("Unknown cosmetic entry");
            Object rawProbability=raw.get("probability");
            if(!(rawProbability instanceof Number))throw new IllegalArgumentException("Non-numeric probability");
            double probability=((Number)rawProbability).doubleValue();
            double expected=guarantee&&owned.contains(id)?0:item.chanceBasisPoints()/(double)total;
            if(!Double.isFinite(probability)||probability<0||probability>1||Math.abs(probability-expected)>1e-9)
                throw new IllegalArgumentException("Incorrect conditional odds");
            sum+=probability;entries.add(new Entry(item,probability));
        }
        if(Math.abs(sum-1)>1e-8)throw new IllegalArgumentException("Incomplete probability mass");
        return new Catalog(uid,enabled,balance,version,pity,owned,entries);
    }
    static Receipt receipt(String uid,String requested,JSONObject value)throws Exception{
        requireUid(uid);
        if(!BillingPolicy.validRequestId(requested)||!requested.equals(value.getString("id"))
                ||!"LumaGold".equals(value.getString("currency"))||integer(value,"charged")!=PRICE)
            throw new IllegalArgumentException("Mismatched draw receipt");
        CosmeticCatalog.Item item=CosmeticCatalog.find(value.getString("itemId"));
        if(item==null||!item.rarity.name().equals(value.getString("rarity")))throw new IllegalArgumentException("Unknown draw item");
        boolean duplicate=bool(value,"duplicate");long rawRefund=integer(value,"refund");
        if(rawRefund!=(duplicate?DUPLICATE_REFUND:0))throw new IllegalArgumentException("Invalid duplicate compensation");
        int refund=(int)rawRefund;
        Set<String> owned=owned(value.getJSONArray("owned"));
        if(!owned.contains(item.id))throw new IllegalArgumentException("Missing acquired ownership");
        int pity=pity(value);if(!duplicate&&pity!=0||duplicate&&pity==0)throw new IllegalArgumentException("Invalid guarantee transition");
        return new Receipt(uid,requested,item.id,duplicate,balance(value),version(value),pity,refund,owned);
    }
    static Set<String> owned(JSONArray values)throws Exception{
        if(values.length()>CosmeticCatalog.ITEMS.size())throw new IllegalArgumentException("Oversized ownership");
        LinkedHashSet<String> ids=new LinkedHashSet<>();
        for(int i=0;i<values.length();i++){
            Object value=values.get(i);if(!(value instanceof String)||CosmeticCatalog.find((String)value)==null||!ids.add((String)value))
                throw new IllegalArgumentException("Invalid ownership");
        }
        return ids;
    }
    private static void requireUid(String uid){BillingPolicy.accountHash(uid);}
    private static long balance(JSONObject value)throws Exception{long balance=integer(value,"balance");if(balance< -1_000_000_000L||balance>1_000_000_000L)throw new IllegalArgumentException("Invalid balance");return balance;}
    private static long version(JSONObject value)throws Exception{long version=integer(value,"version");if(version<0)throw new IllegalArgumentException("Invalid version");return version;}
    private static int pity(JSONObject value)throws Exception{long pity=integer(value,"pity");if(pity<0||pity>=GUARANTEE)throw new IllegalArgumentException("Invalid guarantee counter");return(int)pity;}
    static long integer(JSONObject value,String key)throws Exception{Object raw=value.get(key);if(!(raw instanceof Byte)&&!(raw instanceof Short)&&!(raw instanceof Integer)&&!(raw instanceof Long))throw new IllegalArgumentException("Invalid integer");return((Number)raw).longValue();}
    private static boolean bool(JSONObject value,String key)throws Exception{Object raw=value.get(key);if(!(raw instanceof Boolean))throw new IllegalArgumentException("Invalid flag");return(Boolean)raw;}
}
