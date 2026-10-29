package com.androidhit.lumadrift.ui;

/** Reactions come from successful player exits, never a random near-miss or a purchase. */
public final class PuzzleReaction {
    public enum Kind { IDLE, JUMP, CHEER, DANCE, THINK }
    public static final long SUCCESS_MS=1300, BLOCK_MS=650;
    private int streak;
    private long at=-1;
    private boolean blocked;
    public void success(long now){streak++;at=now;blocked=false;}
    public void reset(){streak=0;at=-1;blocked=false;}
    public void blocked(long now){streak=0;at=now;blocked=true;}
    public int streak(){return streak;}
    public long elapsed(long now){return at<0?Long.MAX_VALUE:Math.max(0,now-at);}
    public Kind kind(long now){
        long age=elapsed(now);if(blocked)return age<BLOCK_MS?Kind.THINK:Kind.IDLE;
        if(age>=SUCCESS_MS)return Kind.IDLE;
        return streak>=6?Kind.DANCE:streak>=3?Kind.CHEER:Kind.JUMP;
    }
    public static float rise(Kind kind,long elapsed,boolean reduced){
        if(reduced||elapsed<0||elapsed>=SUCCESS_MS)return 0;
        float phase=Math.min(1,elapsed/(float)SUCCESS_MS);
        return kind==Kind.DANCE?-7*Math.abs((float)Math.sin(phase*Math.PI*4)):
                kind==Kind.CHEER?-8*Math.abs((float)Math.sin(phase*Math.PI*2)):
                kind==Kind.JUMP?-12*(float)Math.sin(phase*Math.PI):0;
    }
    public static float tilt(Kind kind,long elapsed,boolean reduced){return reduced||kind!=Kind.DANCE?0:9*(float)Math.sin(elapsed*Math.PI/200.0);}
}
