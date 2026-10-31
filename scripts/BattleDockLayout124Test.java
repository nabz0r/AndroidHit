import com.androidhit.lumadrift.ui.BattleDockLayout124;

/** Geometry and physical target floors only; no native or visual acceptance claim. */
public final class BattleDockLayout124Test {
    private static int checks;
    private static void check(boolean good,String message){checks++;if(!good)throw new AssertionError(message);}
    private static boolean close(float a,float b){return Math.abs(a-b)<.002f;}
    private static void validate(float touch,float text){
        BattleDockLayout124 layout=new BattleDockLayout124(touch,text);
        check(layout.columns==2||layout.columns==4,"two or four columns");
        check(layout.rows*layout.columns==4,"all four targets");
        check(layout.headerHeight>=24&&layout.headerHeight>=1.5f*text,"header respects text floor");
        check(layout.cellHeight>=104&&layout.cellHeight>=Math.max(48,touch),"cell height respects floor");
        check(layout.cellHeight>=3*text+24,"three text lines plus padding fit vertically");
        float previousTop=-1;
        for(int index=0;index<4;index++){
            BattleDockLayout124.Rect r=layout.slot(index);
            check(r.width()+.002f>=Math.max(48,touch)&&r.height()+.002f>=Math.max(48,touch),"target meets touch floor");
            check(r.left>=24&&r.right<=366.002f&&r.top>=layout.headerHeight+8,"bounded and below header");
            check(r.top>=previousTop,"stable row-major order");previousTop=r.top;
            check(layout.slotAt(r.centerX(),r.centerY())==index,"center routes exactly once");
            check(layout.slotAt(r.right,r.centerY())!=index&&layout.slotAt(r.centerX(),r.bottom)!=index,"half-open target edges");
            for(int other=index+1;other<4;other++)check(!r.intersects(layout.slot(other)),"no target overlaps");
        }
        check(close(layout.height,layout.slot(3).bottom+12),"height includes last target and bottom breathing room");
        check(layout.slotAt(0,0)==-1&&layout.slotAt(25,layout.headerHeight)==-1,"header and exterior are not targets");
        for(int index=0;index<4;index++){
            if(index%layout.columns!=layout.columns-1){
                BattleDockLayout124.Rect r=layout.slot(index),next=layout.slot(index+1);
                check(close(next.left-r.right,8)&&layout.slotAt((r.right+next.left)/2,r.centerY())==-1,"horizontal gap not shared");
            }
        }
    }
    private static void invalid(){
        for(float bad:new float[]{Float.NaN,Float.NEGATIVE_INFINITY,Float.POSITIVE_INFINITY,-1,0}){
            try{new BattleDockLayout124(bad,12);throw new AssertionError("invalid touch accepted");}catch(IllegalArgumentException expected){check(true,"invalid touch rejected");}
            try{new BattleDockLayout124(48,bad);throw new AssertionError("invalid text accepted");}catch(IllegalArgumentException expected){check(true,"invalid text rejected");}
        }
        try{new BattleDockLayout124(167.01f,12);throw new AssertionError("oversized touch accepted");}catch(IllegalArgumentException expected){check(true,"unfittable touch floor rejected");}
        try{new BattleDockLayout124(48,Float.MAX_VALUE);throw new AssertionError("overflow accepted");}catch(IllegalArgumentException expected){check(true,"overflow rejected");}
        BattleDockLayout124 layout=new BattleDockLayout124(48,12);
        for(int slot:new int[]{-1,4,999})try{layout.slot(slot);throw new AssertionError("invalid slot accepted");}catch(IllegalArgumentException expected){check(true,"invalid slot rejected");}
    }
    public static void main(String[] args){
        for(float touch:new float[]{1,48,58.5f,72,79.5f,79.5001f,80,100,132,167})
            for(float text:new float[]{1,12,18,24,32,48,96})validate(touch,text);
        check(new BattleDockLayout124(79.5f,12).columns==4,"exact four-column threshold inclusive");
        check(new BattleDockLayout124(79.5001f,12).columns==2,"above threshold reflows to two columns");
        check(new BattleDockLayout124(48,32).columns==2&&new BattleDockLayout124(48,40).columns==2,"large type reflows instead of ellipsizing every name into a narrow target");
        for(int viewport:new int[]{320,375,390,736,1280})for(float scale:new float[]{1,1.3f,1.8f,2.4f}){
            float physicalScale=viewport/390f;
            BattleDockLayout124 layout=new BattleDockLayout124(48/physicalScale,12*scale);
            for(int slot=0;slot<4;slot++)check(layout.slot(slot).width()*physicalScale>=47.99f
                    &&layout.slot(slot).height()*physicalScale>=47.99f,"physical 48dp target floor across viewport/font scenarios");
        }
        invalid();System.out.println("BattleDockLayout124: "+checks+" checks passed (pure geometry only)");
    }
}
