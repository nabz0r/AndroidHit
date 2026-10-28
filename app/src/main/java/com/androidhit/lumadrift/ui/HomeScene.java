package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import com.androidhit.lumadrift.data.AvatarLoadout;
import com.androidhit.lumadrift.data.AvatarStyles;
import com.androidhit.lumadrift.data.Catalog;
import com.androidhit.lumadrift.data.CosmeticCatalog;
import com.androidhit.lumadrift.data.HomeLayout;
import com.androidhit.lumadrift.data.HomeCatalog;
import java.util.List;

/** Original, bitmap-free little home. All scene coordinates live in a 390 × 380 space. */
public final class HomeScene {
    public static final float WIDTH = 390, HEIGHT = 380;
    public static final float ORIGIN_X = 195, ORIGIN_Y = 145, TILE_X = 28, TILE_Y = 15.5f;
    public static final int TARGET_FLOOR=-1,TARGET_COMPANION=-2,TARGET_WARDROBE=-3,TARGET_PUZZLE=-4,TARGET_OUTSIDE=-5;
    private static final int INK = 0xff59475f, WHITE = 0xfffffcf7;
    private static final int WOOD = 0xffc69a76, WOOD_DARK = 0xffa77962;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final Path floorPath = new Path();
    private final RectF itemBounds = new RectF();
    private final CompanionPortrait portrait = new CompanionPortrait();
    private final HomeLayout.Placement[] ordered = new HomeLayout.Placement[64];
    private final int[] orderIndices = new int[64];
    private String interactionItem = "";
    private boolean lampOn = true, gateLampOn = true;

    public HomeScene() {
        floorPath.moveTo(195,145);floorPath.lineTo(363,238);floorPath.lineTo(195,331);floorPath.lineTo(27,238);floorPath.close();
    }

    /** Bind the currently used object. For a lamp, lightOn is its actual switch state. */
    public void setInteraction(String itemId,boolean lightOn) {
        interactionItem=itemId==null?"":itemId;
        if("lamp".equals(itemId))lampOn=lightOn;
        else if("gate_lamp".equals(itemId))gateLampOn=lightOn;
    }

    /** Transient decorative lighting, independent of rewards, saved placement and motion. */
    public boolean isLightOn(String itemId) { return "gate_lamp".equals(itemId)?gateLampOn:lampOn; }

    /** Tile centre; top is applied by the caller when translating screen touches. */
    public static PointF floorPoint(float x, float y) {
        return new PointF(px(x,y),py(x,y));
    }

    /** Inverse projection, accepting only a real tile inside the floor. */
    public static int[] cellAt(float localX, float localY) {
        if(!Float.isFinite(localX)||!Float.isFinite(localY))return null;
        float a=(localX-ORIGIN_X)/TILE_X,b=(localY-ORIGIN_Y)/TILE_Y;
        int x=(int)Math.floor((a+b)*.5f),y=(int)Math.floor((b-a)*.5f);
        return x>=0&&x<6&&y>=0&&y<6?new int[]{x,y}:null;
    }

    /** Find the uppermost occupied footprint; non-solid rugs yield to furniture above them. */
    public static int itemAt(HomeLayout layout,int x,int y) {
        if(layout==null)return -1;
        List<HomeLayout.Placement> placements=layout.placements();
        int rug=-1;
        for(int i=placements.size()-1;i>=0;i--){
            HomeLayout.Placement p=placements.get(i);
            if(x>=p.x&&x<p.x+p.width()&&y>=p.y&&y<p.y+p.depth()){
                if("rug".equals(p.itemId)){rug=i;continue;}return i;
            }
        }
        return rug;
    }

    /** Actual sprite bounds, shared by room drawing and semantic touch targets. */
    public static void boundsFor(HomeLayout.Placement placement,RectF out) {
        if(placement==null||out==null)return;
        float x=placement.x+(placement.width()-1)*.5f,y=placement.y+(placement.depth()-1)*.5f;
        float xx=px(x,y),yy=py(x,y),scale=Math.min(itemWidth(placement.id)/naturalWidth(placement.id),itemHeight(placement.id)/naturalHeight(placement.id));
        float width=naturalWidth(placement.id)*scale,height=naturalHeight(placement.id)*scale;
        out.set(xx-width*.5f,yy-height+5,xx+width*.5f,yy+5);
    }

    /**
     * Hit-test in the exact painter order. A nonnegative result indexes layout.placements().
     * The caller can keep individual accessibility targets while routing pointer touches here.
     */
    public int targetAt(HomeLayout layout,float x,float y,float companionX,float companionY,String activity) {
        if(layout==null||!finite(x)||!finite(y))return TARGET_OUTSIDE;
        int target=cellAt(x,y)==null?TARGET_OUTSIDE:TARGET_FLOOR;
        float best=-Float.MAX_VALUE;
        if(x>=247&&x<=304&&y>=70&&y<=191){target=TARGET_WARDROBE;best=-100;}
        if(x>=309&&x<=355&&y>=126&&y<=234){target=TARGET_PUZZLE;best=-99;}
        List<HomeLayout.Placement> objects=layout.placements();
        for(int i=0;i<objects.size();i++){
            HomeLayout.Placement object=objects.get(i);boundsFor(object,itemBounds);float z=depth(object);
            if(itemBounds.contains(x,y)&&z>=best){best=z;target=i;}
        }
        float dx=(x-px(companionX,companionY))/43f,dy=(y-(py(companionX,companionY)-43))/49f;
        float z=companionX+companionY+.3f;
        HomeLayout.Placement used=layout.placement(interactionItem);
        if("rest".equals(activity)&&used!=null&&!"rug".equals(used.id))z=depth(used)+.01f;
        if(dx*dx+dy*dy<=1&&z>=best)target=TARGET_COMPANION;
        return target;
    }

    public void draw(Canvas c,float top,HomeLayout layout,String wallpaper,String floor,
                     Catalog.CharacterItem character,AvatarStyles.Style style,AvatarLoadout loadout,
                     List<CosmeticCatalog.Item> clothes,long time,boolean reduceMotion,
                     float companionX,float companionY,String activity,int selectedIndex,boolean decorating) {
        if(c==null)return;
        c.save();c.translate(0,top);
        shell(c,wallpaper,floor,time,reduceMotion);
        List<HomeLayout.Placement> placed=layout==null?null:layout.placements();
        int count=placed==null?0:Math.min(ordered.length,placed.size());
        // A fixed reusable scratch array keeps depth ordering free of per-frame lists/comparators.
        for(int i=0;i<count;i++){ordered[i]=placed.get(i);orderIndices[i]=i;}
        for(int i=1;i<count;i++){
            HomeLayout.Placement value=ordered[i];int idx=orderIndices[i],j=i-1;
            while(j>=0&&depth(ordered[j])>depth(value)){ordered[j+1]=ordered[j];orderIndices[j+1]=orderIndices[j];j--;}
            ordered[j+1]=value;orderIndices[j+1]=idx;
        }
        if(decorating)grid(c);
        // Textiles are always on the floor, independent of their insertion order in the save.
        for(int i=0;i<count;i++)if("rug".equals(ordered[i].itemId))furniture(c,ordered[i],orderIndices[i]==selectedIndex,time,reduceMotion,decorating);
        float safeX=finite(companionX)?clamp(companionX,0,5):3,safeY=finite(companionY)?clamp(companionY,0,5):3;
        boolean drawn=false;float companionDepth=safeX+safeY+.3f;
        boolean mounted="rest".equals(activity)&&!"rug".equals(interactionItem)&&layout!=null&&layout.placement(interactionItem)!=null;
        for(int i=0;i<count;i++){
            HomeLayout.Placement p=ordered[i];if("rug".equals(p.itemId))continue;
            if(!drawn&&!mounted&&depth(p)>companionDepth){companion(c,safeX,safeY,character,style,loadout,clothes,time,reduceMotion,activity);drawn=true;}
            furniture(c,p,orderIndices[i]==selectedIndex,time,reduceMotion,decorating);
            if(mounted&&!drawn&&interactionItem.equals(p.itemId)){companion(c,safeX,safeY,character,style,loadout,clothes,time,reduceMotion,activity);drawn=true;}
        }
        if(!drawn)companion(c,safeX,safeY,character,style,loadout,clothes,time,reduceMotion,activity);
        // The front edge anchors the cutaway without obscuring little feet or furniture.
        line(c,28,239,195,332,0xffcfaf98,2.2f);line(c,195,332,362,239,0xffc49f87,2.2f);
        c.restore();
        for(int i=0;i<count;i++)ordered[i]=null;
    }

    private void shell(Canvas c,String wallpaper,String floor,long time,boolean reduceMotion) {
        boolean mint=has(wallpaper,"mint")||has(wallpaper,"sage"),rose=has(wallpaper,"rose")||has(wallpaper,"peach"),night=has(wallpaper,"night")||has(wallpaper,"star")||has(wallpaper,"dusk"),lavender=has(wallpaper,"lavender")||has(wallpaper,"lilac");
        int left=mint?0xffecf2e5:rose?0xffffece3:night?0xffe8def2:lavender?0xffeee5f7:0xfff8efe5;
        int right=mint?0xffdce9d6:rose?0xfff3d9d5:night?0xffd4c6e4:lavender?0xffddd3ed:0xffe9e0ee;
        oval(c,51,304,339,355,0x08705a79);oval(c,69,310,322,348,0x0c705a79);
        quad(c,27,238,195,331,195,344,27,251,0xffd4b49a);quad(c,195,331,363,238,363,251,195,344,0xffbb967e);
        quad(c,27,128,195,35,195,145,27,238,left);
        quad(c,195,35,363,128,363,238,195,145,right);
        // The wall edges have a light top cap, as on a tiny physical dollhouse.
        line(c,27,128,195,35,0xfffffbf4,5);line(c,195,35,363,128,0xfff8f2f9,5);
        line(c,27,128,27,238,0xffe0d2c7,4);line(c,363,128,363,238,0xffcbbcd7,4);
        if(mint||rose||lavender){
            c.save();wallClip(c,true);for(int i=0;i<16;i++)line(c,15+i*17,67,15+i*17,242,0x10ffffff,7);c.restore();
            c.save();wallClip(c,false);for(int i=0;i<16;i++)line(c,195+i*16,30,195+i*16,242,0x10ffffff,7);c.restore();
        }
        if(night){
            star(c,60,147,3,0x77ffffff);star(c,158,78,4,0xaaffffff);star(c,218,90,3,0x88ffffff);star(c,307,151,3,0x88ffffff);
        }
        int base=has(floor,"cloud")||has(floor,"white")?0xffefe9e6:has(floor,"honey")||has(floor,"oak")?0xffead0a9:has(floor,"night")||has(floor,"dark")?0xffc4b2c7:has(floor,"rose")?0xffead0c9:has(floor,"mint")?0xffd7e2cc:0xffead8bd;
        HomeCatalog.Surface floorStyle=HomeCatalog.floor(floor);
        if(floorStyle!=null)base=blend(floorStyle.color,WHITE,.17f);
        fill(base);c.drawPath(floorPath,paint);
        c.save();c.clipPath(floorPath);
        for(int i=0;i<=12;i++){
            float yy=145+i*15.5f;
            line(c,17,yy,373,yy+197,0x18a67f63,1);
            line(c,17,yy,373,yy-197,0x13ffffff,1);
        }
        for(int x=0;x<6;x++)for(int y=0;y<6;y++)if((x+y)%3==0){
            float xx=px(x,y),yy=py(x,y);line(c,xx-11,yy-3,xx+7,yy+7,0x12a7775b,.7f);
        }
        // The window casts a permanent warm patch, not a flashing temporal effect.
        quad(c,92,204,143,176,232,253,181,281,0x21fffce0);
        line(c,117,191,206,268,0x35fffce8,2.5f);
        c.restore();
        line(c,28,235,195,142,0xffd8bca4,5);line(c,195,142,362,235,0xffc8adc2,5);
        line(c,28,232,195,139,0xfffff8e9,1.4f);line(c,195,139,362,232,0xfff3eaf5,1.4f);
        window(c,time,reduceMotion);
        wardrobe(c);
        puzzleDoor(c);
        // Little wall hooks and a framed sprout keep the house welcoming before decoration.
        line(c,49,164,73,151,0xffc4a17f,2);circle(c,54,164,2.4f,0xffbd9272);circle(c,67,157,2.4f,0xffbd9272);
        quad(c,156,104,177,92,177,117,156,129,0xffd8bc96);quad(c,159,106,174,98,174,115,159,123,0xfffffaf0);
        line(c,167,116,167,106,0xff7da18a,1.3f);oval(c,161,105,168,110,0xffa6bf99);oval(c,167,102,173,107,0xff91b49b);
    }

    private void window(Canvas c,long time,boolean reduced) {
        c.save();c.translate(113,122);c.skew(0,-.554f);
        round(c,-38,-45,38,26,17,0x18846d69);round(c,-36,-48,36,23,17,0xffc9a783);
        round(c,-31,-43,31,18,13,0xfffff7e7);round(c,-27,-39,27,14,10,0xffcce4ec);
        // A very quiet sky inside the star-window-shaped frame.
        circle(c,10,-23,9,0xffffe8a7);oval(c,-27,-4,30,24,0xffb5d5bf);oval(c,-32,4,19,28,0xffa7c8b2);
        star(c,0,-13,19,0xfffff8e7);star(c,0,-13,13,0xffbddce8);
        line(c,0,-38,0,13,0xfffcf8ed,2);line(c,-27,-12,27,-12,0xfffcf8ed,2);
        round(c,-42,17,42,25,4,0xffb98b69);round(c,-42,14,42,21,4,0xffe6c6a1);
        // Short tied curtains leave the star and sky clearly visible.
        path.reset();path.moveTo(-39,-43);path.cubicTo(-28,-47,-21,-45,-21,-39);path.lineTo(-29,-4);path.lineTo(-39,11);path.close();shape(c,0xffe6b8c8);
        path.reset();path.moveTo(39,-43);path.cubicTo(28,-47,21,-45,21,-39);path.lineTo(29,-4);path.lineTo(39,11);path.close();shape(c,0xffe6b8c8);
        line(c,-38,-4,-29,-7,0xffcc8fa8,2);line(c,38,-4,29,-7,0xffcc8fa8,2);
        c.restore();
    }

    private void wardrobe(Canvas c) {
        c.save();c.translate(274,139);c.skew(0,.554f);
        round(c,-28,-51,28,35,5,0x1c614b66);round(c,-27,-54,27,31,5,0xffb3a0c5);
        round(c,-24,-53,24,27,4,0xffd6c6e4);round(c,-21,-48,-1,23,3,0xffeadeee);round(c,1,-48,21,23,3,0xffe4d4e8);
        line(c,0,-50,0,25,0xffc3acd3,1.5f);circle(c,-6,-8,2.2f,0xffb18e65);circle(c,6,-8,2.2f,0xffb18e65);
        // A hanger mark makes the wardrobe discoverable without introducing text in the art.
        arc(c,-4,-37,5,-28,190,250,0xffa68cb8,1.6f);
        path.reset();path.moveTo(0,-29);path.lineTo(-10,-21);path.quadTo(0,-17,10,-21);path.close();strokePath(c,0xffa68cb8,1.6f);
        line(c,-19,28,-19,35,0xff9c84aa,3);line(c,19,28,19,35,0xff9c84aa,3);
        c.restore();
    }

    private void puzzleDoor(Canvas c) {
        c.save();c.translate(332,183);c.skew(0,.554f);
        round(c,-20,-47,20,34,11,0xffc9b1c4);round(c,-16,-44,16,34,9,0xff92b9b0);
        round(c,-12,-39,12,1,6,0xffbad8ca);round(c,-12,5,12,29,4,0xffabd0bf);
        // Luma's gate arrow is the house's way back to the puzzle.
        line(c,-6,-18,7,-18,WHITE,3);line(c,7,-18,2,-23,WHITE,3);line(c,7,-18,2,-13,WHITE,3);
        circle(c,10,4,2.4f,0xfff5d293);round(c,-23,31,23,37,3,0xffd7b697);
        c.restore();
    }

    private void grid(Canvas c) {
        c.save();c.clipPath(floorPath);
        for(int n=0;n<=6;n++){
            line(c,195+n*28,145+n*15.5f,27+n*28,238+n*15.5f,0x547d9b8d,1);
            line(c,195-n*28,145+n*15.5f,363-n*28,238+n*15.5f,0x547d9b8d,1);
        }
        c.restore();
    }

    private void furniture(Canvas c,HomeLayout.Placement p,boolean selected,long time,boolean reduced,boolean decorating) {
        float x=p.x+(p.width()-1)*.5f,y=p.y+(p.depth()-1)*.5f;
        float xx=px(x,y),yy=py(x,y);
        if(selected){
            footprint(c,p,0x397fc2b2);outlineFootprint(c,p,0xff59a99d,2);
            if(decorating){circle(c,xx,yy+13,7,0xfffffbf5);line(c,xx-3,yy+13,xx+3,yy+13,0xff5c9c91,1.5f);line(c,xx,yy+10,xx,yy+16,0xff5c9c91,1.5f);}
        }
        float width=itemWidth(p.itemId),height=itemHeight(p.itemId);
        itemBounds.set(xx-width*.5f,yy-height+5,xx+width*.5f,yy+5);
        c.save();
        if((p.rotation&1)!=0)c.scale(-1,1,xx,yy);
        drawItem(c,p.itemId,itemBounds,time,reduced);
        c.restore();
    }

    /** Draw the same distinctive object in room or catalogue; no dependency on ownership. */
    public void drawItem(Canvas c,String id,RectF bounds,long time,boolean reduceMotion) {
        if(c==null||bounds==null||bounds.width()<=0||bounds.height()<=0)return;
        c.save();c.translate(bounds.centerX(),bounds.bottom);float s=Math.min(bounds.width()/naturalWidth(id),bounds.height()/naturalHeight(id));c.scale(s,s);
        if("bed".equals(id))bed(c);
        else if("sofa".equals(id))sofa(c);
        else if("lamp".equals(id))lamp(c,false,lampOn,time,reduceMotion);
        else if("gate_lamp".equals(id))lamp(c,true,gateLampOn,time,reduceMotion);
        else if("plant".equals(id))plant(c,time,reduceMotion);
        else if("rug".equals(id))rug(c);
        else if("table".equals(id))table(c);
        else if("bookshelf".equals(id))bookshelf(c);
        else if("cushion".equals(id))cushion(c);
        else if("plush".equals(id))plush(c);
        else if("mobile".equals(id))mobile(c,time,reduceMotion);
        else if("stool".equals(id))stool(c);
        else {oval(c,-28,-16,28,-2,0x175a4560);round(c,-25,-49,25,-10,9,0xffd2bdcd);star(c,0,-30,11,WHITE);}
        c.restore();
    }

    private void bed(Canvas c) {
        oval(c,-43,-15,44,1,0x1a765c6e);
        leg(c,-34,-9,6);leg(c,34,-11,7);
        round(c,-43,-73,29,-26,10,0xffc3a7d3);round(c,-39,-68,25,-36,8,0xffddcce7);
        star(c,-7,-52,10,0xffffe8b5);
        quad(c,-43,-37,20,-46,45,-23,-18,-12,0xffdfd4e9);round(c,-43,-35,43,-12,8,0xffbca6ce);
        round(c,-38,-40,38,-17,8,0xfff9f2eb);round(c,-20,-37,37,-16,7,0xffaebed9);
        line(c,-13,-34,31,-34,0xffcedaeb,2);line(c,-13,-29,31,-29,0xffbecce2,1.5f);
        round(c,-35,-48,-5,-34,6,WHITE);line(c,-30,-43,-11,-43,0xffeee5ec,1.3f);
        round(c,-43,-20,43,-10,4,0xffc9aed7);star(c,23,-25,4,0xfff8f0d5);
    }

    private void sofa(Canvas c) {
        oval(c,-47,-12,47,1,0x18765c6e);leg(c,-35,-9,5);leg(c,35,-9,5);
        round(c,-43,-58,43,-19,16,0xff91b9ae);round(c,-37,-63,0,-23,15,0xffc2d6c4);round(c,0,-63,37,-23,15,0xffc2d6c4);
        round(c,-38,-31,38,-12,8,0xffa8c9b9);line(c,0,-29,0,-14,0xff91b7aa,1.6f);
        round(c,-47,-43,-31,-13,8,0xffb7d2c0);round(c,31,-43,47,-13,8,0xffb7d2c0);
        round(c,-29,-48,-7,-29,6,0xfff1d2c6);line(c,-24,-42,-13,-34,0xffdfb8ac,1.1f);
        round(c,9,-48,28,-29,6,0xffe8e0b8);star(c,18,-39,5,0xfffff7dd);
    }

    private void lamp(Canvas c,boolean gate,boolean on,long time,boolean reduced) {
        if(on){oval(c,-43,-21,43,4,0x17edc472);oval(c,-32,-17,32,0,0x1cf8d48b);}
        oval(c,-26,-12,26,-1,0x16765c6e);oval(c,-23,-15,23,-5,0xffc4a480);oval(c,-18,-18,18,-9,0xffe5c5a0);
        line(c,0,-17,0,-60,0xffa78065,5);line(c,-1,-17,-1,-60,0xffd5b68d,2);
        if(gate){
            round(c,-24,-79,24,-37,8,0xffa8b5ce);round(c,-18,-73,18,-43,6,on?0xffffe5a5:0xffbdc7ce);
            line(c,-9,-58,9,-58,0xffa28b6c,3);line(c,9,-58,3,-64,0xffa28b6c,3);line(c,9,-58,3,-52,0xffa28b6c,3);
            circle(c,20,-29,5,on?0xffe8c484:0xff8498a4);
            if(on)line(c,20,-27,20,-31,0xfffff8da,1.4f);else line(c,18,-29,22,-29,0xffeff4f3,1.4f);
        } else {
            if(on)oval(c,-31,-50,31,-32,0x25f4c873);path.reset();path.moveTo(-20,-80);path.quadTo(0,-87,20,-80);path.lineTo(31,-48);path.quadTo(0,-34,-31,-48);path.close();shape(c,on?0xffe9cfa7:0xffc3b8b3);
            oval(c,-31,-55,31,-41,on?0xffffe5b3:0xffa69aa0);line(c,-9,-77,-16,-51,0xfff3dfbf,2);line(c,9,-77,16,-51,0xffd9bc94,1.4f);
            line(c,20,-44,20,on?-31:-27,0xffaa8770,1.2f);circle(c,20,on?-30:-26,2.8f,0xffbca1c9);
        }
    }

    private void plant(Canvas c,long time,boolean reduced) {
        oval(c,-27,-11,27,1,0x16765c6e);line(c,0,-27,-1,-72,0xff638e72,3);
        float sway=reduced?0:(float)Math.sin((time%100000)*.0014f)*2;
        c.save();c.rotate(sway,0,-27);
        leaf(c,-1,-64,-23,-81,0xff9fbc8d);leaf(c,0,-56,27,-72,0xff83aa81);leaf(c,0,-44,-29,-59,0xff7ba486);leaf(c,0,-36,22,-49,0xffabc294);leaf(c,0,-70,4,-90,0xff86ad82);c.restore();
        path.reset();path.moveTo(-24,-32);path.lineTo(24,-32);path.lineTo(18,-6);path.quadTo(0,2,-18,-6);path.close();shape(c,0xffd4a78e);oval(c,-24,-38,24,-25,0xffe9c2a2);oval(c,-19,-35,19,-28,0xff89715e);
        line(c,-15,-23,-12,-9,0xffe9c2aa,1.8f);line(c,14,-23,12,-9,0xffb78a78,1.5f);
    }

    private void rug(Canvas c) {
        // A soft house-shaped weave, so the floor textile remains recognisable at thumbnail scale.
        oval(c,-48,-36,48,-2,0x106e536d);oval(c,-48,-38,48,-5,0xffd7abc0);oval(c,-43,-35,43,-8,0xffedd0d6);
        for(int i=-3;i<=3;i++){line(c,-48+i*1.4f,-24+i*3.5f,-53+i*1.4f,-23+i*3.5f,0xffcd9db7,1.5f);line(c,48-i*1.4f,-24+i*3.5f,53-i*1.4f,-23+i*3.5f,0xffcd9db7,1.5f);}
        path.reset();path.moveTo(-14,-21);path.lineTo(0,-31);path.lineTo(14,-21);path.lineTo(14,-15);path.lineTo(-14,-15);path.close();shape(c,0xfff9e9df);
        round(c,-3,-21,3,-15,1,0xffd5b3c6);star(c,26,-23,3,0xfff8e7db);star(c,-26,-23,3,0xfff8e7db);
    }

    private void table(Canvas c) {
        oval(c,-35,-12,35,1,0x16765c6e);leg(c,-25,-11,23);leg(c,24,-11,23);
        oval(c,-39,-46,39,-21,0xffb88d6d);oval(c,-39,-51,39,-26,0xffe6c69d);oval(c,-32,-48,32,-29,0xffecd1ad);
        // Tea and one tiny gate-shaped biscuit make the surface feel inhabited.
        oval(c,6,-43,25,-34,0xfff7eee1);round(c,9,-51,22,-38,3,0xfff7eee1);arc(c,20,-49,28,-40,270,240,0xfff7eee1,2.5f);oval(c,10,-53,21,-48,0xffb78f73);
        round(c,-23,-47,-7,-38,3,0xffc89577);line(c,-19,-43,-11,-43,0xfff0c999,1.6f);line(c,-11,-43,-14,-46,0xfff0c999,1.6f);
    }

    private void bookshelf(Canvas c) {
        oval(c,-31,-10,31,1,0x16765c6e);leg(c,-23,-9,5);leg(c,23,-9,5);
        round(c,-31,-86,31,-9,4,0xffbb9277);round(c,-26,-80,26,-16,2,0xffdbc3a0);
        round(c,-23,-76,23,-51,1,0xffb99377);round(c,-23,-47,23,-21,1,0xffb99377);
        line(c,-30,-50,30,-50,0xffead1aa,4);line(c,-30,-19,30,-19,0xffead1aa,4);
        book(c,-21,-74,8,21,0xffadc3b0);book(c,-11,-77,9,24,0xffd8b0bd);book(c,0,-72,7,19,0xffb7bad5);book(c,10,-75,8,22,0xffe3c58e);
        round(c,-21,-36,-2,-23,3,0xffe5c7b5);star(c,-11,-30,4,0xfff9eddd);
        round(c,4,-34,22,-29,1,0xffafc1c6);round(c,6,-28,24,-23,1,0xffd4b5cf);circle(c,12,-41,5,0xfff2dfac);
    }

    private void cushion(Canvas c) {
        oval(c,-37,-10,37,0,0x17765c6e);round(c,-36,-36,36,-9,14,0xffd3aec9);round(c,-32,-39,32,-14,13,0xffebcbdc);
        line(c,-26,-25,-18,-29,0xffd4abc4,1.3f);line(c,26,-25,18,-29,0xffd4abc4,1.3f);circle(c,0,-26,2,0xffd6abc7);
        arc(c,-24,-34,24,-15,8,165,0xfff4e0e9,1.8f);
    }

    private void plush(Canvas c) {
        oval(c,-27,-9,27,1,0x16765c6e);oval(c,-26,-22,-4,-3,0xffbd9cb2);oval(c,4,-22,26,-3,0xffbd9cb2);
        oval(c,-21,-37,21,-6,0xffd3b5c8);circle(c,-17,-59,10,0xffd8bcd0);circle(c,17,-59,10,0xffd8bcd0);circle(c,-17,-59,5,0xffefd9df);circle(c,17,-59,5,0xffefd9df);
        oval(c,-26,-62,26,-24,0xffdfc4d6);oval(c,-15,-43,15,-28,0xfff0dce1);circle(c,-8,-45,2,INK);circle(c,8,-45,2,INK);oval(c,-3,-39,3,-35,0xff927080);
        line(c,0,-35,0,-32,0xff927080,1.3f);arc(c,-5,-35,5,-29,0,180,0xff927080,1.3f);heart(c,0,-19,8,0xfff7e5c5);
    }

    private void mobile(Canvas c,long time,boolean reduced) {
        oval(c,-32,-10,32,1,0x16765c6e);oval(c,-26,-15,26,-5,0xffb8a5c5);oval(c,-21,-19,21,-10,0xffdac7df);
        line(c,0,-15,0,-88,0xffb299bd,3);arc(c,-2,-91,32,-62,180,170,0xffb299bd,3);
        float drift=reduced?0:(float)Math.sin((time%100000)*.0018f)*3;
        c.save();c.rotate(drift,15,-78);line(c,-18,-73,43,-73,0xffceb692,2);line(c,-18,-73,-18,-46,0xffc7b6c9,1);line(c,15,-78,15,-39,0xffc7b6c9,1);line(c,43,-73,43,-53,0xffc7b6c9,1);
        star(c,-18,-41,9,0xffe5c78e);star(c,15,-34,10,0xffd1b1cd);
        line(c,36,-50,50,-50,0xff9dbfb4,4);line(c,50,-50,45,-55,0xff9dbfb4,4);line(c,50,-50,45,-45,0xff9dbfb4,4);c.restore();
    }

    private void stool(Canvas c) {
        oval(c,-28,-11,28,1,0x16765c6e);leg(c,-18,-10,26);leg(c,18,-10,26);line(c,-17,-16,17,-16,0xffb88e70,2.5f);
        oval(c,-31,-42,31,-23,0xffbb9177);oval(c,-31,-47,31,-28,0xffddba93);oval(c,-26,-48,26,-32,0xfff0d4ae);star(c,0,-40,7,0xfffff0d4);
    }

    private void companion(Canvas c,float x,float y,Catalog.CharacterItem character,AvatarStyles.Style style,AvatarLoadout loadout,List<CosmeticCatalog.Item> clothes,long time,boolean reduced,String activity) {
        float xx=px(x,y),yy=py(x,y);boolean hug="hug".equals(activity),rest="rest".equals(activity),read="read".equals(activity),play="play".equals(activity),lamp="lamp".equals(activity),tea="tea".equals(activity),walk="walk".equals(activity);
        oval(c,xx-23,yy-5,xx+23,yy+7,0x18775b7b);
        AvatarStage.Pose pose=hug||play||rest?AvatarStage.Pose.HAPPY:read?AvatarStage.Pose.THINK:lamp?AvatarStage.Pose.GATE:AvatarStage.Pose.IDLE;
        c.save();if(rest)c.rotate("bed".equals(interactionItem)?-17:-5,xx,yy-24);
        if(walk&&!reduced)c.rotate((float)Math.sin((time%10000)*.023f)*3,xx,yy-4);
        portrait.draw(c,character,xx,yy-38,24,style,pose,time,reduced||rest,loadout,clothes);c.restore();
        float phase=reduced?0:(time%1600L)/1600f;
        if(hug){
            // Open arms curl around the dressed companion; hearts also remain visible with motion reduced.
            arc(c,xx-30,yy-40,xx-2,yy-10,92,190,0xffe6bbd3,3);arc(c,xx+2,yy-40,xx+30,yy-10,258,190,0xffe6bbd3,3);
            heart(c,xx-27,yy-66-phase*8,6,0xffe7a8bf);heart(c,xx+27,yy-56-phase*8,5,0xffefc28e);heart(c,xx+4,yy-80-phase*4,5,0xffd4b3db);
            if("plush".equals(interactionItem)){c.save();c.translate(xx,yy-10);c.scale(.31f,.31f);plush(c);c.restore();}
        } else if(rest){
            // Sleep is represented visually, without language-specific text inside the illustration.
            crescent(c,xx+27,yy-68,7,0xffb29fc9);star(c,xx+35,yy-80,3,0xffd9bbd9);
        } else if(read){
            quad(c,xx-14,yy-20,xx,yy-17,xx,yy-4,xx-14,yy-7,0xffbdabc9);quad(c,xx,yy-17,xx+14,yy-20,xx+14,yy-7,xx,yy-4,0xffd7c6df);
            line(c,xx,yy-17,xx,yy-5,0xffa38bad,1);line(c,xx-11,yy-16,xx-4,yy-14,0xfff4e9ef,1);line(c,xx+4,yy-14,xx+11,yy-16,0xfff4e9ef,1);
        } else if(tea){
            oval(c,xx-11,yy-13,xx+12,yy-7,0xffe7d3c0);round(c,xx-8,yy-25,xx+9,yy-11,3,0xfffff8e9);arc(c,xx+6,yy-24,xx+16,yy-14,270,230,0xfffff8e9,2.5f);oval(c,xx-6,yy-27,xx+7,yy-22,0xffb99277);
            arc(c,xx-5,yy-42,xx+1,yy-29,260,110,0xffe8d6d3,1.4f);arc(c,xx+3,yy-44,xx+8,yy-31,260,110,0xffe8d6d3,1.4f);
        } else if(play){star(c,xx-28,yy-60,5,0xffdfb878);star(c,xx+29,yy-48,4,0xffa7c8bd);}
        else if(lamp){star(c,xx+31,yy-64,5,0xffe7c883);}
    }

    private void footprint(Canvas c,HomeLayout.Placement p,int color){
        float x=p.x,y=p.y,w=p.width(),d=p.depth();quad(c,195+(x-y)*28,145+(x+y)*15.5f,195+(x+w-y)*28,145+(x+w+y)*15.5f,195+(x+w-y-d)*28,145+(x+w+y+d)*15.5f,195+(x-y-d)*28,145+(x+y+d)*15.5f,color);
    }
    private void outlineFootprint(Canvas c,HomeLayout.Placement p,int color,float width){
        float x=p.x,y=p.y,w=p.width(),d=p.depth();path.reset();path.moveTo(195+(x-y)*28,145+(x+y)*15.5f);path.lineTo(195+(x+w-y)*28,145+(x+w+y)*15.5f);path.lineTo(195+(x+w-y-d)*28,145+(x+w+y+d)*15.5f);path.lineTo(195+(x-y-d)*28,145+(x+y+d)*15.5f);path.close();strokePath(c,color,width);
    }
    private void wallClip(Canvas c,boolean left){path.reset();path.moveTo(left?27:195,left?128:35);path.lineTo(left?195:363,left?35:128);path.lineTo(left?195:363,left?145:238);path.lineTo(left?27:195,left?238:145);path.close();c.clipPath(path);}
    private void leg(Canvas c,float x,float y,float h){line(c,x,y-h,x,y,WOOD_DARK,5);line(c,x-1,y-h,x-1,y-2,WOOD,2);}
    private void leaf(Canvas c,float x,float y,float xx,float yy,int color){path.reset();path.moveTo(x,y);path.quadTo(xx+12,yy-9,xx,yy);path.quadTo(xx-7,yy+11,x,y);path.close();shape(c,color);}
    private void book(Canvas c,float x,float y,float w,float h,int color){round(c,x,y,x+w,y+h,1,color);line(c,x+2,y+4,x+w-2,y+4,0x90fff9e9,1);}
    private static float depth(HomeLayout.Placement p){return "rug".equals(p.itemId)?-1:p.x+p.y+(p.width()+p.depth())*.5f;}
    private static float itemWidth(String id){return "bed".equals(id)||"sofa".equals(id)?93:"rug".equals(id)?108:"bookshelf".equals(id)?58:"table".equals(id)?68:"cushion".equals(id)?60:"mobile".equals(id)?59:"plant".equals(id)?49:"lamp".equals(id)||"gate_lamp".equals(id)?55:"plush".equals(id)?45:49;}
    private static float naturalWidth(String id){return "bed".equals(id)||"sofa".equals(id)?96:"rug".equals(id)?108:"bookshelf".equals(id)?64:"table".equals(id)?80:"cushion".equals(id)?74:"mobile".equals(id)?79:"plant".equals(id)?60:"lamp".equals(id)?64:"gate_lamp".equals(id)?50:"plush".equals(id)?56:64;}
    private static float naturalHeight(String id){return "rug".equals(id)?40:"cushion".equals(id)?40:"table".equals(id)?54:"stool".equals(id)?50:"plush".equals(id)?70:"plant".equals(id)?90:"bookshelf".equals(id)||"mobile".equals(id)?92:"lamp".equals(id)?88:"gate_lamp".equals(id)?82:"bed".equals(id)?75:66;}
    private static float itemHeight(String id){return "rug".equals(id)?47:"cushion".equals(id)?34:"table".equals(id)?57:"stool".equals(id)?48:"plush".equals(id)?58:"plant".equals(id)?73:"bookshelf".equals(id)||"mobile".equals(id)?82:"lamp".equals(id)||"gate_lamp".equals(id)?82:76;}
    private static float px(float x,float y){return ORIGIN_X+(x-y)*TILE_X;}
    private static float py(float x,float y){return ORIGIN_Y+(x+y+1)*TILE_Y;}
    private static boolean has(String text,String value){return text!=null&&text.contains(value);}
    private static int blend(int a,int b,float amount){int r=Math.round(((a>>16)&255)*(1-amount)+((b>>16)&255)*amount),g=Math.round(((a>>8)&255)*(1-amount)+((b>>8)&255)*amount),blue=Math.round((a&255)*(1-amount)+(b&255)*amount);return 0xff000000|(r<<16)|(g<<8)|blue;}
    private static boolean finite(float n){return !Float.isNaN(n)&&!Float.isInfinite(n);}
    private static float clamp(float n,float min,float max){return Math.max(min,Math.min(max,n));}
    private void fill(int color){paint.setShader(null);paint.setColor(color);paint.setStyle(Paint.Style.FILL);paint.setStrokeWidth(0);paint.setStrokeCap(Paint.Cap.ROUND);}
    private void shape(Canvas c,int color){fill(color);c.drawPath(path,paint);}
    private void strokePath(Canvas c,int color,float width){fill(color);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(width);paint.setStrokeJoin(Paint.Join.ROUND);c.drawPath(path,paint);}
    private void oval(Canvas c,float l,float t,float r,float b,int color){fill(color);c.drawOval(l,t,r,b,paint);}
    private void circle(Canvas c,float x,float y,float r,int color){oval(c,x-r,y-r,x+r,y+r,color);}
    private void round(Canvas c,float l,float t,float r,float b,float radius,int color){fill(color);c.drawRoundRect(l,t,r,b,radius,radius,paint);}
    private void line(Canvas c,float x,float y,float xx,float yy,int color,float width){fill(color);paint.setStrokeWidth(width);c.drawLine(x,y,xx,yy,paint);}
    private void arc(Canvas c,float l,float t,float r,float b,float start,float sweep,int color,float width){fill(color);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(width);c.drawArc(l,t,r,b,start,sweep,false,paint);}
    private void quad(Canvas c,float x,float y,float xx,float yy,float xxx,float yyy,float xxxx,float yyyy,int color){path.reset();path.moveTo(x,y);path.lineTo(xx,yy);path.lineTo(xxx,yyy);path.lineTo(xxxx,yyyy);path.close();shape(c,color);}
    private void star(Canvas c,float x,float y,float r,int color){path.reset();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float rr=i%2==0?r:r*.47f,xx=x+(float)Math.cos(a)*rr,yy=y+(float)Math.sin(a)*rr;if(i==0)path.moveTo(xx,yy);else path.lineTo(xx,yy);}path.close();shape(c,color);}
    private void heart(Canvas c,float x,float y,float r,int color){path.reset();path.moveTo(x,y+r*.66f);path.cubicTo(x-r*1.35f,y,x-r*.7f,y-r*1.16f,x,y-r*.38f);path.cubicTo(x+r*.7f,y-r*1.16f,x+r*1.35f,y,x,y+r*.66f);path.close();shape(c,color);}
    private void crescent(Canvas c,float x,float y,float r,int color){path.reset();path.moveTo(x+r*.5f,y-r*.86f);path.cubicTo(x-r*.55f,y-r*1.1f,x-r*1.15f,y+r*.12f,x-r*.6f,y+r*.7f);path.cubicTo(x,y+r*1.2f,x+r*.7f,y+r*.7f,x+r*.84f,y+r*.3f);path.cubicTo(x,y+r*.6f,x-r*.1f,y-r*.3f,x+r*.5f,y-r*.86f);path.close();shape(c,color);}
}
