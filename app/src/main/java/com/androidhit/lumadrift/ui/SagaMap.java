package com.androidhit.lumadrift.ui;

/** Stable virtual coordinates: every node remains reachable without drawing 1,000 views. */
public final class SagaMap {
    public static final float FIRST_Y=250f, STEP=96f;
    private SagaMap() { }
    public static float y(int level){return FIRST_Y+(Math.max(1,level)-1)*STEP;}
    public static float x(int level){return 195f+(float)Math.sin((Math.max(1,level)-1)*.92)*101f;}
    public static float height(int total){return y(Math.max(1,total))+170f;}
    public static int at(float scroll,int total){return Math.max(1,Math.min(total,1+Math.round((scroll-FIRST_Y)/STEP)));}
    public static int firstVisible(float scroll,int total){return Math.max(1,at(scroll-180,total));}
    public static int lastVisible(float scroll,float viewport,int total){return Math.min(total,at(scroll+viewport+180,total));}
    public static int biome(int level){return Math.max(0,Math.min(9,(Math.max(1,level)-1)/100));}
    public static int sectorStart(int level){return biome(level)*100+1;}
    public static int sectorEnd(int level,int total){return Math.min(Math.max(1,total),sectorStart(level)+99);}
    /** The current captain is on the free side of the route, with a full dressed-art margin. */
    public static float companionX(int level){return x(level)<195?314f:76f;}
    public static float header(float viewport){return viewport<500?58f:194f;}
    public static float scrollTo(int level,float viewport,int total){float center=(header(viewport)+Math.max(header(viewport)+1,viewport-95))/2;return Math.max(0,Math.min(Math.max(0,height(total)-viewport),y(Math.min(total,Math.max(1,level)))-center));}
}
