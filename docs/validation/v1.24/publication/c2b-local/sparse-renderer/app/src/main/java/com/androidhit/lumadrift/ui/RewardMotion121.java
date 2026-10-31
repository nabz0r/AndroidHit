package com.androidhit.lumadrift.ui;

/** Display-only interpolation of confirmed balances. No reward, clock or transaction is created. */
public final class RewardMotion121 {
    public static final long DURATION_MS=620;
    public static final class Counter {
        private long from,target,started,lastNow;
        private boolean initialized;
        private long delta;
        public long sample(long confirmed,long now,boolean still){
            confirmed=Math.max(0,confirmed);
            now=Math.max(lastNow,now);lastNow=now;
            if(!initialized){initialized=true;from=target=confirmed;started=now;delta=0;return confirmed;}
            if(confirmed!=target){from=value(now);delta=confirmed-target;target=confirmed;started=now;}
            if(still){from=target;delta=0;return target;}
            return value(now);
        }
        private long value(long now){
            double t=Math.max(0,Math.min(1,(now-started)/(double)DURATION_MS));
            if(t<=0)return from;
            if(t>=1)return target;
            double eased=t*t*(3-2*t);
            return Math.round(from+((double)target-from)*eased);
        }
        public long gain(long now){return initialized&&now>=started&&now-started<1100?Math.max(0,delta):0;}
        public float age(long now){return Math.max(0,Math.min(1,(now-started)/1100f));}
        public void unavailable(){initialized=false;delta=0;}
    }
    private RewardMotion121(){}
}
