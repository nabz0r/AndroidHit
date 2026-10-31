package com.androidhit.lumadrift.ui;

/** Presentation-only sector geometry and travel. No unlocks, clocks, wallets or saved-game writes. */
public final class JourneyWorld122 {
    public static final int MAX_LEVEL=1000, SECTOR_SIZE=100, SECTORS=10;
    public static final float WIDTH=960f, HEIGHT=1680f, NODE_RADIUS=29f;
    public static final long MAX_TRAVEL_MS=1500L;
    private JourneyWorld122() { }

    public static final class Point {
        public final float x,y;
        public Point(float x,float y){this.x=x;this.y=y;}
    }
    public static final class Rect {
        public final float left,top,right,bottom;
        public Rect(float left,float top,float right,float bottom){
            if(!finite(left)||!finite(top)||!finite(right)||!finite(bottom)||right<left||bottom<top)
                throw new IllegalArgumentException("Finite ordered rectangle required");
            this.left=left;this.top=top;this.right=right;this.bottom=bottom;
        }
        public float width(){return right-left;}
        public float height(){return bottom-top;}
        public boolean contains(float x,float y){return x>=left&&x<=right&&y>=top&&y<=bottom;}
        public boolean intersects(float x,float y,float radius){return x+radius>=left&&x-radius<=right&&y+radius>=top&&y-radius<=bottom;}
    }
    public static int sector(int level){checkLevel(level);return (level-1)/SECTOR_SIZE;}
    public static int sectorStart(int level){return sector(level)*SECTOR_SIZE+1;}
    public static int sectorEnd(int level,int total){checkTotal(total);return Math.min(total,sectorStart(level)+SECTOR_SIZE-1);}
    public static int firstInSector(int sector,int total){checkSector(sector,total);return sector*SECTOR_SIZE+1;}
    public static int lastInSector(int sector,int total){return Math.min(total,firstInSector(sector,total)+SECTOR_SIZE-1);}

    /** Ten rows, with alternating traversal: every consecutive identity has one actual edge. */
    public static Point node(int level){
        checkLevel(level);int local=(level-1)%SECTOR_SIZE,row=local/10,position=local%10;
        int column=(row&1)==0?position:9-position;
        float x=111+82*column;
        float y=164+148*row+(float)Math.sin(column*.72+row*.61)*15;
        return new Point(x,y);
    }

    /** Exact point on the same cubic used by the world renderer; adjacent nodes within one sector. */
    public static Point edge(int from,int to,float progress){
        if(Math.abs(from-to)!=1||sector(from)!=sector(to)||!finite(progress))throw new IllegalArgumentException("Adjacent sector edge required");
        if(from>to)return edge(to,from,1-clamp(progress,0,1));
        float t=clamp(progress,0,1),u=1-t;Point a=node(from),b=node(to);
        float ax,bx,ay,by;
        if(Math.abs(a.y-b.y)>70){float bend=a.x<WIDTH/2?-49:49;ax=a.x+bend;bx=b.x+bend;ay=a.y+(b.y-a.y)*.32f;by=b.y-(b.y-a.y)*.32f;}
        else{ax=a.x+(b.x-a.x)*.33f;bx=b.x-(b.x-a.x)*.33f;ay=a.y-13;by=b.y+13;}
        return new Point(u*u*u*a.x+3*u*u*t*ax+3*u*t*t*bx+t*t*t*b.x,
                u*u*u*a.y+3*u*u*t*ay+3*u*t*t*by+t*t*t*b.y);
    }

    /** A fractional level follows real successive edges rather than cutting across islands. */
    public static Point routePoint(float position,int total){
        checkTotal(total);if(!finite(position)||position<1||position>total)throw new IllegalArgumentException("Route position outside catalogue");
        int low=(int)Math.floor(position);float fraction=position-low;
        if(fraction<.00001f||low==total)return node(low);
        if(sector(low)!=sector(low+1))throw new IllegalArgumentException("Sector crossing has no local edge");
        return edge(low,low+1,fraction);
    }

    public static int hitLevel(float x,float y,int sector,int total,float radius){
        checkSector(sector,total);if(!finite(x)||!finite(y)||!finite(radius)||radius<0)return -1;
        float best=radius*radius;int found=-1;
        for(int level=firstInSector(sector,total);level<=lastInSector(sector,total);level++){
            Point point=node(level);float dx=x-point.x,dy=y-point.y,d=dx*dx+dy*dy;
            if(d<=best){best=d;found=level;}
        }
        return found;
    }
    public static Point miniPoint(int level,Rect mini){Point p=node(level);return worldToMini(p.x,p.y,mini);}
    public static Point worldToMini(float x,float y,Rect mini){
        if(mini==null||!finite(x)||!finite(y))throw new IllegalArgumentException("Finite minimap point required");
        return new Point(mini.left+clamp(x,0,WIDTH)/WIDTH*mini.width(),mini.top+clamp(y,0,HEIGHT)/HEIGHT*mini.height());
    }
    public static Point miniToWorld(float x,float y,Rect mini){
        if(mini==null||mini.width()<=0||mini.height()<=0||!finite(x)||!finite(y))throw new IllegalArgumentException("Nonempty minimap required");
        return new Point(clamp((x-mini.left)/mini.width(),0,1)*WIDTH,clamp((y-mini.top)/mini.height(),0,1)*HEIGHT);
    }
    public static Rect miniViewport(Rect world,Rect mini){
        if(world==null)throw new IllegalArgumentException("World viewport required");
        Point a=worldToMini(world.left,world.top,mini),b=worldToMini(world.right,world.bottom,mini);
        return new Rect(a.x,a.y,b.x,b.y);
    }

    public static final class TravelFrame {
        public final float x,y,dx,dy,routePosition,progress;
        public final int destination,levelReached,sector;
        public final boolean moving,abbreviated;
        private TravelFrame(Point point,float dx,float dy,float position,float progress,int target,int reached,boolean moving,boolean abbreviated){
            x=point.x;y=point.y;this.dx=dx;this.dy=dy;routePosition=position;this.progress=progress;
            destination=target;levelReached=reached;sector=JourneyWorld122.sector(target);this.moving=moving;this.abbreviated=abbreviated;
        }
    }
    /** Short local travel is continuous. Long/cross-sector selections arrive immediately, never impose a wait. */
    public static final class Travel {
        private final float from;private final int to,total;private final long startedAt,duration;
        private final boolean abbreviated;private long furthestElapsed;
        private Travel(float from,int to,int total,long now,boolean reduced){
            checkTotal(total);if(to<1||to>total||!finite(from)||from<1||from>total)throw new IllegalArgumentException("Travel outside catalogue");
            this.from=from;this.to=to;this.total=total;startedAt=now;
            abbreviated=JourneyWorld122.sector((int)Math.floor(from))!=JourneyWorld122.sector(to)||Math.abs(to-from)>12;
            duration=reduced||abbreviated||Math.abs(to-from)<.00001f?0:Math.min(MAX_TRAVEL_MS,Math.max(320L,(long)(Math.abs(to-from)*220)));
        }
        public static Travel start(int from,int to,int total,long now,boolean reduced){return new Travel(from,to,total,now,reduced);}
        public long durationMillis(){return duration;}
        public int destination(){return to;}
        public Travel retarget(int target,long now,boolean reduced){return new Travel(sample(now).routePosition,target,total,now,reduced);}
        public TravelFrame sample(long now){
            long elapsed=now<=startedAt?0:now-startedAt;if(elapsed<0)elapsed=Long.MAX_VALUE;
            furthestElapsed=Math.max(furthestElapsed,elapsed);
            float raw=duration==0?1:Math.min(1,furthestElapsed/(float)duration);
            float smooth=raw*raw*(3-2*raw),position=raw>=1?to:from+(to-from)*smooth;
            Point point=routePoint(position,total);boolean moving=raw<1;
            float epsilon=Math.min(.005f,Math.abs(to-position));
            Point ahead=moving&&epsilon>0?routePoint(position+Math.signum(to-from)*epsilon,total):point;
            int reached=moving?(to>=from?(int)Math.floor(position):(int)Math.ceil(position)):to;
            return new TravelFrame(point,ahead.x-point.x,ahead.y-point.y,position,raw,to,reached,moving,abbreviated);
        }
    }
    static boolean finite(float n){return !Float.isNaN(n)&&!Float.isInfinite(n);}
    static float clamp(float n,float low,float high){return Math.max(low,Math.min(high,n));}
    private static void checkLevel(int level){if(level<1||level>MAX_LEVEL)throw new IllegalArgumentException("Level 1..1000 required");}
    private static void checkTotal(int total){if(total<1||total>MAX_LEVEL)throw new IllegalArgumentException("Catalogue 1..1000 required");}
    private static void checkSector(int value,int total){checkTotal(total);if(value<0||value>=(total+99)/100)throw new IllegalArgumentException("Sector outside catalogue");}
}
