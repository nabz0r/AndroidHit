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

/** Original, bitmap-free little home. A ten-by-ten world projected into a 390 × 440 viewport; camera geometry is shared with hit targets. */
public final class HomeScene {
    public static final float WIDTH = 390, HEIGHT = 440;
    public static final float ORIGIN_X = 320, ORIGIN_Y = 140, TILE_X = 28, TILE_Y = 15.5f;
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
    private float cameraZoom=1f,cameraPanX,cameraPanY;
    private String room="living";
    private static final float FIT=.62f, WORLD_WIDTH=640, WORLD_HEIGHT=480;
    public void setRoom(String value){room=HomeCatalog.validRoom(value)?value:HomeCatalog.DEFAULT_ROOM;}
    public String room(){return room;}
    /** Pan is expressed in viewport logical pixels. Invalid gesture values leave a safe fit view. */
    public void setCamera(float zoom,float panX,float panY){
        cameraZoom=finite(zoom)?clamp(zoom,.85f,2.5f):1f;
        float scale=cameraScale(),maxX=Math.max(0,(WORLD_WIDTH*scale-WIDTH)*.5f+12),maxY=Math.max(0,(WORLD_HEIGHT*scale-HEIGHT)*.5f+12);
        cameraPanX=finite(panX)?clamp(panX,-maxX,maxX):0;cameraPanY=finite(panY)?clamp(panY,-maxY,maxY):0;
    }
    public void resetCamera(){setCamera(1,0,0);}
    public float cameraZoom(){return cameraZoom;}
    public float cameraPanX(){return cameraPanX;}
    public float cameraPanY(){return cameraPanY;}
    private float cameraScale(){return FIT*cameraZoom;}
    private float projectX(float value){return WIDTH*.5f+(value-ORIGIN_X)*cameraScale()+cameraPanX;}
    private float projectY(float value){return HEIGHT*.5f+(value-WORLD_HEIGHT*.5f)*cameraScale()+cameraPanY;}
    private float worldX(float value){return (value-WIDTH*.5f-cameraPanX)/cameraScale()+ORIGIN_X;}
    private float worldY(float value){return (value-HEIGHT*.5f-cameraPanY)/cameraScale()+WORLD_HEIGHT*.5f;}
    public PointF projectedFloorPoint(float x,float y){return new PointF(projectX(px(x,y)),projectY(py(x,y)));}
    public int[] cellAtCamera(float x,float y){return cellAt(worldX(x),worldY(y));}
    public void projectedBoundsFor(HomeLayout.Placement placement,RectF out){if(placement==null||out==null)return;boundsFor(placement,out);out.set(projectX(out.left),projectY(out.top),projectX(out.right),projectY(out.bottom));}


    public HomeScene() {
        floorPath.moveTo(320,140);floorPath.lineTo(600,295);floorPath.lineTo(320,450);floorPath.lineTo(40,295);floorPath.close();
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
        return x>=0&&x<HomeCatalog.SIZE&&y>=0&&y<HomeCatalog.SIZE?new int[]{x,y}:null;
    }

    /** Find the uppermost occupied footprint; non-solid rugs yield to furniture above them. */
    public static int itemAt(HomeLayout layout,int x,int y) {
        if(layout==null)return -1;
        List<HomeLayout.Placement> placements=layout.placements();
        int rug=-1;
        for(int i=placements.size()-1;i>=0;i--){
            HomeLayout.Placement p=placements.get(i);
            if(x>=p.x&&x<p.x+p.width()&&y>=p.y&&y<p.y+p.depth()){
                if(!HomeCatalog.item(p.itemId).solid){rug=i;continue;}return i;
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
        x=worldX(x);y=worldY(y);
        int target=cellAt(x,y)==null?TARGET_OUTSIDE:TARGET_FLOOR;
        float best=-Float.MAX_VALUE;
        if(!"garden".equals(room)&&x>=421&&x<=478&&y>=110&&y<=231){target=TARGET_WARDROBE;best=-100;}
        if(x>=519&&x<=565&&y>=158&&y<=266){target=TARGET_PUZZLE;best=-99;}
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
        c.save();c.clipRect(0,top,WIDTH,top+HEIGHT);c.translate(WIDTH*.5f+cameraPanX,top+HEIGHT*.5f+cameraPanY);c.scale(cameraScale(),cameraScale());c.translate(-ORIGIN_X,-WORLD_HEIGHT*.5f);
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
        for(int i=0;i<count;i++)if(!HomeCatalog.item(ordered[i].itemId).solid)furniture(c,ordered[i],orderIndices[i]==selectedIndex,time,reduceMotion,decorating);
        float safeX=finite(companionX)?clamp(companionX,0,HomeCatalog.SIZE-1):3,safeY=finite(companionY)?clamp(companionY,0,HomeCatalog.SIZE-1):3;
        boolean drawn=false;float companionDepth=safeX+safeY+.3f;
        boolean mounted="rest".equals(activity)&&!"rug".equals(interactionItem)&&layout!=null&&layout.placement(interactionItem)!=null;
        for(int i=0;i<count;i++){
            HomeLayout.Placement p=ordered[i];if(!HomeCatalog.item(p.itemId).solid)continue;
            if(!drawn&&!mounted&&depth(p)>companionDepth){companion(c,safeX,safeY,character,style,loadout,clothes,time,reduceMotion,activity);drawn=true;}
            furniture(c,p,orderIndices[i]==selectedIndex,time,reduceMotion,decorating);
            if(mounted&&!drawn&&interactionItem.equals(p.itemId)){companion(c,safeX,safeY,character,style,loadout,clothes,time,reduceMotion,activity);drawn=true;}
        }
        if(!drawn)companion(c,safeX,safeY,character,style,loadout,clothes,time,reduceMotion,activity);
        // The front edge anchors the cutaway without obscuring little feet or furniture.
        line(c,40,295,320,450,0xffcfaf98,2.2f);line(c,320,450,600,295,0xffc49f87,2.2f);
        c.restore();
        for(int i=0;i<count;i++)ordered[i]=null;
    }

    private void shell(Canvas c,String wallpaper,String floor,long time,boolean reduceMotion) {
        boolean garden="garden".equals(room),bedroom="bedroom".equals(room);
        HomeCatalog.Surface walls=HomeCatalog.wallpaper(wallpaper),ground=HomeCatalog.floor(floor);
        int left=walls==null?0xfff8eadb:blend(walls.color,WHITE,.22f),right=blend(left,0xffaf9fc2,.16f);
        oval(c,50,394,590,477,0x126c607b);
        quad(c,40,295,320,450,320,464,40,309,garden?0xff9cb79b:0xffd0ae90);
        quad(c,320,450,600,295,600,309,320,464,garden?0xff839e88:0xffb9937c);
        if(garden){
            // A low garden fence and an open sky distinguish the outdoor room structurally.
            quad(c,40,257,320,102,320,140,40,295,blend(left,0xff96bc8b,.36f));quad(c,320,102,600,257,600,295,320,140,blend(right,0xff89ad87,.36f));
            for(int n=0;n<=10;n++){float x=40+n*28,y=295-n*15.5f;line(c,x,y-38,x,y,0xfff1e5cb,8);float rx=320+n*28,ry=140+n*15.5f;line(c,rx,ry-38,rx,ry,0xffeadcc0,8);}
            line(c,40,264,320,109,0xfffff1d4,5);line(c,320,109,600,264,0xffe5d1b6,5);
            circle(c,392,48,25,0x48ffe3a0);oval(c,91,50,188,75,0x55ffffff);oval(c,130,40,210,70,0x55ffffff);
        }else{
            quad(c,40,155,320,0,320,140,40,295,left);quad(c,320,0,600,155,600,295,320,140,right);
            line(c,40,155,320,0,0xfffffcf3,5);line(c,320,0,600,155,0xfffff4eb,5);
            line(c,40,155,40,295,0xffdfcebe,4);line(c,600,155,600,295,0xffc4b3cd,4);
            // Bedroom wallpaper includes vertical fabric bands; the living room uses quiet panels.
            for(int n=0;n<10;n++){float x=40+n*28,y=155-n*15.5f;line(c,x,y+8,x,y+132,bedroom?0x22ffffff:0x117d6589,bedroom?8:1);}
        }
        int grass=has(floor,"cloud")?0xffc8d7b8:has(floor,"rose")?0xffd2c8aa:has(floor,"mint")?0xffa9cbb0:0xffbed3aa;
        fill(garden?grass:ground==null?0xffe6ceb0:blend(ground.color,WHITE,.18f));c.drawPath(floorPath,paint);
        c.save();c.clipPath(floorPath);
        if(garden){
            for(int x=0;x<10;x++)for(int y=0;y<10;y++){float xx=px(x,y),yy=py(x,y);line(c,xx-4,yy+2,xx-2,yy-2,0x4f73996d,1.2f);line(c,xx-2,yy-2,xx+1,yy+1,0x4f73996d,1.2f);}
        }else{
            for(int n=0;n<=10;n++){line(c,320+n*28,140+n*15.5f,40+n*28,295+n*15.5f,0x30a37a5b,1.2f);line(c,320-n*28,140+n*15.5f,600-n*28,295+n*15.5f,0x28ffffff,1);}
            quad(c,124,230,199,189,369,324,294,365,0x18fff9cf);
        }
        c.restore();
        if(!garden){
            line(c,40,292,320,137,0xffd4b7a0,4);line(c,320,137,600,292,0xffc4a9bb,4);
            c.save();c.translate(70,0);window(c,time,reduceMotion);c.restore();
            c.save();c.translate(174,40);wardrobe(c);c.restore();
        }
        c.save();c.translate(210,32);puzzleDoor(c);c.restore();
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
        for(int n=0;n<=HomeCatalog.SIZE;n++){
            line(c,320+n*28,140+n*15.5f,40+n*28,295+n*15.5f,0x658198a0,1);
            line(c,320-n*28,140+n*15.5f,600-n*28,295+n*15.5f,0x658198a0,1);
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
        drawItem(c,p.itemId,itemBounds,time,reduced,p.rotation);
        c.restore();
    }

    /** Draw the same distinctive object in room or catalogue; no dependency on ownership. */
    public void drawItem(Canvas c,String id,RectF bounds,long time,boolean reduceMotion){drawItem(c,id,bounds,time,reduceMotion,0);}
    /** Rotation changes the visible front plane and the side face, rather than mirroring the art. */
    public void drawItem(Canvas c,String id,RectF bounds,long time,boolean reduceMotion,int rotation) {
        if(c==null||bounds==null||bounds.width()<=0||bounds.height()<=0)return;
        c.save();c.translate(bounds.centerX(),bounds.bottom);float s=Math.min(bounds.width()/naturalWidth(id),bounds.height()/naturalHeight(id));c.scale(s,s);
        boolean turned=(rotation&1)!=0;
        if(turned){c.scale(.82f,1);c.skew(0,.22f);}
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
        else drawNewItem(c,id,time,reduceMotion,turned);
        c.restore();
    }

    private void drawNewItem(Canvas c,String id,long time,boolean reduced,boolean turned){
        switch(id){
            case "aquarium":aquarium(c,time,reduced,turned);break;
            case "piano":piano(c,turned);break;
            case "greenhouse":greenhouse(c,turned);break;
            case "library":library(c,turned);break;
            case "bay_window":bayWindow(c,turned);break;
            case "telescope":telescope(c,turned);break;
            case "fireplace":fireplace(c,turned);break;
            case "desk":desk(c,turned);break;
            case "easel":easel(c,turned);break;
            case "record_player":recordPlayer(c,turned);break;
            case "arcade":arcade(c,turned);break;
            case "terrarium":terrarium(c);break;
            case "bonsai":bonsai(c);break;
            case "cactus":cactus(c);break;
            case "cherry_tree":cherryTree(c);break;
            case "fountain":fountain(c,time,reduced);break;
            case "pond":pond(c);break;
            case "hammock":hammock(c,turned);break;
            case "swing":swing(c,turned);break;
            case "bench":bench(c,turned);break;
            case "picnic":picnic(c,turned);break;
            case "flowerbed":flowerbed(c,turned);break;
            case "garden_path":gardenPath(c);break;
            case "lantern":lantern(c);break;
            case "canopy_bed":canopyBed(c,turned);break;
            case "wardrobe":furnitureWardrobe(c,turned);break;
            case "bath":bath(c,turned);break;
            case "mirror":mirror(c,turned);break;
            case "kitchen":kitchen(c,turned);break;
            case "chess_table":chessTable(c,turned);break;
        }
    }
    /** An explicit side face changes sides on the quarter-turn; the front details stay readable. */
    private void cabinet(Canvas c,float w,float h,int body,int trim,boolean turned){
        float d=turned?-14:14;
        quad(c,-w,-h,w,-h,w+d,-h-8,-w+d,-h-8,blend(body,WHITE,.22f));
        if(turned)quad(c,-w,-h,-w+d,-h-8,-w+d,-16,-w,-8,blend(body,INK,.19f));
        else quad(c,w,-h,w+d,-h-8,w+d,-16,w,-8,blend(body,INK,.19f));
        round(c,-w,-h,w,-8,4,body);line(c,-w+4,-h+5,w-4,-h+5,trim,2);
    }
    private void aquarium(Canvas c,long time,boolean reduced,boolean turned){
        oval(c,-62,-13,65,2,0x1c5a4560);cabinet(c,55,35,0xffb49a88,0xffebd1b0,turned);
        round(c,-60,-98,60,-31,5,0xff7fa8af);round(c,-55,-92,55,-37,3,0xff99d7df);
        quad(c,-55,-92,-37,-92,11,-37,-7,-37,0x48ffffff);round(c,-55,-45,55,-37,2,0xffd9cfab);
        for(int i=0;i<4;i++){float x=-44+i*27;line(c,x,-42,x-5,-66-i%2*9,0xff6caa94,3);leaf(c,x-2,-53,x+9,-63,0xff8abb9d);}
        float drift=reduced?0:(float)Math.sin(time*.0012)*5;
        fish(c,-25+drift,-72,0xfff1be85);fish(c,27-drift,-61,0xffdfa8c4);circle(c,9,-82,3,0x8affffff);circle(c,14,-73,2,0x8affffff);
        round(c,-61,-99,61,-93,2,0xffd5c7b4);line(c,-54,-88,51,-88,0xffd8f1ef,1.5f);
    }
    private void fish(Canvas c,float x,float y,int color){oval(c,x-8,y-4,x+8,y+4,color);quad(c,x-7,y,x-14,y-6,x-14,y+6,x-7,y,color);circle(c,x+4,y-1,1,INK);}
    private void piano(Canvas c,boolean turned){
        oval(c,-63,-12,65,2,0x1d5a4560);leg(c,-46,-6,34);leg(c,42,-9,42);leg(c,8,-19,27);
        // Curved grand-piano body and a lifted triangular lid are separate from the keyboard.
        path.reset();path.moveTo(-59,-57);path.lineTo(4,-85);path.cubicTo(73,-99,79,-43,30,-31);path.lineTo(-59,-34);path.close();shape(c,0xff514860);
        quad(c,-52,-61,turned?30:53,-112,58,-56,-52,-53,0xff655973);line(c,57,-58,56,-92,0xffd4b88e,2);
        round(c,-59,-49,35,-26,4,0xff343345);round(c,-55,-47,31,-33,2,WHITE);
        for(int i=0;i<12;i++){float x=-53+i*7;line(c,x,-46,x,-34,0xffc8becb,1);if(i%3!=0)round(c,x+3,-47,x+6,-39,1,0xff413c50);}
        round(c,-15,-21,22,-11,3,0xff746277);leg(c,-8,-3,10);leg(c,15,-3,10);
    }
    private void greenhouse(Canvas c,boolean turned){
        oval(c,-78,-17,80,3,0x185a4560);quad(c,-73,-15,-73,-88,0,-129,73,-88,0xffbadfd1);quad(c,-73,-15,0,12,73,-15,0,-44,0xffa0c5ad);
        quad(c,-69,-18,-69,-87,0,-125,0,-44,0x8fdef2e5);quad(c,0,-44,0,-125,69,-87,69,-18,0x86b8e1dc);
        line(c,-73,-88,0,-130,0xffe3d4b7,5);line(c,0,-130,73,-88,0xffd3c1a4,5);
        for(int x=-64;x<=64;x+=32){float roof=-125+Math.abs(x)*.56f;line(c,x,roof,x,-18+Math.abs(x)*.36f,0xffe3d4b7,3);}
        line(c,-72,-18,-72,-88,0xffe3d4b7,4);line(c,72,-18,72,-88,0xffc3ae92,4);line(c,0,-43,0,-128,0xfff1e5ca,4);
        for(int i=0;i<5;i++){float x=-48+i*24,y=-29+Math.abs(x)*.16f;round(c,x-8,y-12,x+8,y,3,0xffc79d80);line(c,x,y-10,x,y-49,0xff6e9a72,2);leaf(c,x,y-34,x-13,y-49,0xff8fb58a);leaf(c,x,y-26,x+12,y-42,0xff9ec591);}
        // The door is on the near left or near right face according to the orientation.
        float dx=turned?-37:37;quad(c,dx-15,-58,dx+15,-58,dx+15,-12,dx-15,-12,0x52fff4ce);line(c,dx-15,-58,dx+15,-58,0xffeadcbb,3);circle(c,dx+9,-31,2,0xffbc9d6d);
    }
    private void library(Canvas c,boolean turned){
        cabinet(c,65,116,0xffb68d6c,0xfff0d1a6,turned);
        for(int row=0;row<3;row++){float y=-105+row*31;round(c,-58,y-3,58,y+24,1,0xff8d6d5f);for(int i=0;i<11;i++){int[] colors={0xffacc6b3,0xffd8b0c6,0xffa4bdd5,0xffe5ca92};book(c,-55+i*10,y+(i%3)*3,7,21-(i%3)*3,colors[(i+row)%4]);}line(c,-62,y+25,62,y+25,0xffe7c79e,3);}
        line(c,0,-111,0,-14,0xffd5b18b,4);
    }
    private void bayWindow(Canvas c,boolean turned){
        round(c,-66,-114,66,-10,29,0xffd5b68f);round(c,-59,-108,59,-20,25,0xfffff1d3);round(c,-52,-101,52,-28,22,0xffb8dce7);
        circle(c,25,-76,17,0xffffe3a3);oval(c,-51,-55,60,-18,0xff9dc5ad);oval(c,-60,-44,20,-14,0xffb3d6b7);
        for(int i=-1;i<=1;i++)line(c,i*29,-101,i*29,-28,0xfffff5dc,3);line(c,-52,-62,52,-62,0xfffff5dc,3);
        float side=turned?-1:1;quad(c,side*58,-106,side*73,-98,side*73,-16,side*58,-24,0xffc4a388);
        round(c,-73,-23,73,-9,4,0xffe6c9a3);round(c,-67,-17,67,-9,2,0xffbd9677);
    }
    private void telescope(Canvas c,boolean turned){
        oval(c,-48,-9,50,3,0x1c5a4560);float aim=turned?-1:1;
        line(c,0,-51,-34,-3,0xffb18b69,5);line(c,0,-51,32,-3,0xffb18b69,5);line(c,0,-51,4,-11,0xffd5b488,5);circle(c,0,-54,8,0xffb7987b);
        c.save();c.rotate(aim*-25,0,-74);round(c,-42,-89,40,-63,7,0xffd9ba82);round(c,-48,-92,-30,-60,5,0xffb69d7a);oval(c,29,-89,48,-63,0xffa5c9db);oval(c,34,-85,44,-67,0xff668baf);round(c,-57,-82,-43,-70,3,0xff8d849b);line(c,-21,-85,20,-85,0xfff2d89f,3);c.restore();
    }
    private void fireplace(Canvas c,boolean turned){
        cabinet(c,48,91,0xffc59e8d,0xffefd1b4,turned);round(c,-34,-73,34,-11,20,0xff7c6270);round(c,-27,-64,27,-12,16,0xff574957);
        line(c,-19,-20,18,-13,0xff98715e,7);line(c,-17,-13,17,-20,0xffaf8166,6);
        path.reset();path.moveTo(-19,-19);path.quadTo(-25,-37,-8,-47);path.quadTo(-5,-33,4,-57);path.quadTo(28,-35,19,-18);path.close();shape(c,0xffefb16e);path.reset();path.moveTo(-6,-19);path.quadTo(-14,-32,3,-39);path.quadTo(14,-28,11,-18);path.close();shape(c,0xffffdc94);
        round(c,-56,-95,56,-83,3,0xffe4c6a6);round(c,-55,-12,55,-4,3,0xffd1b394);
    }
    private void desk(Canvas c,boolean turned){
        leg(c,-44,-4,45);leg(c,43,-4,45);cabinet(c,47,52,0xffcba580,0xfff2d3a7,turned);round(c,-47,-49,47,-39,2,0xffe6c7a0);
        quad(c,-24,-65,9,-65,17,-51,-16,-51,0xfffffae8);line(c,-7,-62,-2,-54,0xffbaa7bc,1);line(c,-19,-59,-11,-57,0xffb7b3bd,1);
        line(c,31,-50,31,-88,0xffaa8f70,3);line(c,31,-88,14,-91,0xffaa8f70,3);round(c,7,-95,25,-83,5,0xff9fbdaf);circle(c,32,-31,2,0xffb18c67);
    }
    private void easel(Canvas c,boolean turned){
        line(c,-4,-98,-28,-1,0xffba926f,5);line(c,4,-98,30,-1,0xffba926f,5);line(c,4,-69,turned?-16:18,-3,0xff907963,4);
        round(c,-35,-94,35,-32,2,0xfff7ebd5);round(c,-29,-88,29,-38,1,0xffb8d4e0);circle(c,14,-73,10,0xffffe6ad);quad(c,-29,-39,-29,-57,-8,-74,15,-39,0xff97b69a);quad(c,-11,-39,16,-65,29,-52,29,-39,0xff769e94);
        line(c,-41,-30,41,-30,0xffd7b18c,6);oval(c,11,-21,34,-9,0xffd2ae85);circle(c,16,-16,3,0xffe1a4b9);circle(c,24,-17,3,0xffa4c6b0);
    }
    private void recordPlayer(Canvas c,boolean turned){
        cabinet(c,33,45,0xffb28b70,0xffe4c39f,turned);quad(c,-33,-45,-25,-85,39,-85,33,-45,0xffc7a181);quad(c,-27,-50,-20,-78,31,-78,27,-50,0xff8d796c);
        oval(c,-29,-47,24,-29,0xff4d4858);oval(c,-11,-42,7,-35,0xffd8b0c6);circle(c,-2,-38,2,0xffefdec1);line(c,25,-47,19,-34,0xffe5d3ad,3);line(c,19,-34,11,-34,0xffe5d3ad,2);
        for(int i=0;i<4;i++)line(c,-23,-25,23,-25+i*4,0x50f4dbb6,1);circle(c,22,-14,3,0xffe1c299);
    }
    private void arcade(Canvas c,boolean turned){
        cabinet(c,31,109,0xff8b7eae,0xffe2c4df,turned);round(c,-26,-103,26,-88,3,0xffd3b3db);star(c,0,-96,5,0xffffedbf);
        round(c,-27,-84,27,-44,5,0xff514f76);round(c,-22,-79,22,-49,3,0xff8ed0da);star(c,0,-64,12,0xffeaf8db);line(c,-16,-55,15,-55,0xffb6f0e5,2);
        quad(c,-31,-41,31,-41,38,-25,-38,-25,0xffb8a4cb);line(c,-13,-28,-13,-40,0xff6d627c,3);circle(c,-13,-41,4,0xffd8b6cc);circle(c,15,-32,4,0xffeacb8d);circle(c,26,-32,3,0xffadd9b9);
        round(c,-17,-20,17,-12,2,0xff645b7d);
    }
    private void terrarium(Canvas c){
        oval(c,-31,-9,32,1,0x155a4560);quad(c,-30,-13,-33,-52,0,-86,33,-52,0x70bde7dd);quad(c,-30,-13,0,-3,33,-13,33,-52,0x85a4d5c4);
        line(c,-33,-52,0,-86,0xffc8b18d,2);line(c,0,-86,33,-52,0xffc8b18d,2);line(c,-33,-52,-30,-13,0xffc8b18d,2);line(c,33,-52,33,-13,0xffc8b18d,2);line(c,0,-86,0,-4,0xffe6d6b5,2);
        oval(c,-24,-27,26,-10,0xffa7bc86);leaf(c,-4,-19,-20,-49,0xff7daa87);leaf(c,2,-20,18,-51,0xff8fb99a);circle(c,17,-24,7,0xffd5c298);
    }
    private void bonsai(Canvas c){
        oval(c,-34,-12,34,-1,0xffa58d7a);round(c,-29,-23,29,-9,5,0xffcba786);line(c,0,-22,-8,-51,0xff93765f,8);line(c,-8,-51,6,-73,0xff93765f,6);line(c,-7,-46,-25,-60,0xff93765f,5);
        oval(c,-40,-74,0,-49,0xff8cab86);oval(c,-6,-89,38,-62,0xffa3c297);oval(c,-26,-63,23,-39,0xff91b692);line(c,-22,-82,-1,-85,0xffd4e3bc,2);
    }
    private void cactus(Canvas c){
        round(c,-22,-27,22,-4,7,0xffd5a08b);round(c,-25,-31,25,-22,4,0xffe7bfa0);round(c,-9,-91,10,-26,10,0xff8cb596);round(c,-29,-71,-15,-40,8,0xff9cc2a1);round(c,-25,-51,-4,-39,5,0xff8cb596);round(c,15,-81,29,-48,8,0xff85ad8f);round(c,7,-58,25,-46,6,0xff8cb596);
        for(int i=0;i<5;i++){float y=-78+i*10;line(c,-4,y,-1,y+3,0xffd6dfb7,1.3f);}flower(c,2,-92,7,0xffedbed1);
    }
    private void cherryTree(Canvas c){
        oval(c,-57,-16,57,3,0x165a4560);line(c,0,-5,0,-81,0xffaf8c74,13);line(c,0,-56,-27,-84,0xffaf8c74,7);line(c,1,-65,28,-100,0xffaf8c74,7);
        oval(c,-59,-119,7,-61,0xffe4b0c6);oval(c,-19,-143,49,-81,0xffefc4d4);oval(c,13,-118,66,-67,0xffdba8c4);oval(c,-40,-104,37,-56,0xffecc1d0);
        for(int i=0;i<8;i++){double a=i*2.2;flower(c,(float)Math.cos(a)*40,-91+(float)Math.sin(a)*23,4,0xffffe2e4);}
        oval(c,-29,-8,-17,-4,0xffedc0d1);oval(c,20,-17,28,-13,0xffedc0d1);
    }
    private void fountain(Canvas c,long time,boolean reduced){
        oval(c,-53,-32,53,1,0xffa9bbc3);oval(c,-49,-35,49,-5,0xffd3dee0);oval(c,-41,-30,41,-11,0xff9fced5);line(c,0,-20,0,-75,0xffb6c9cd,13);oval(c,-32,-79,32,-60,0xffd6e0de);oval(c,-26,-79,26,-66,0xffa3d4db);line(c,0,-74,0,-106,0xffb9e8e8,3);
        for(int side=-1;side<=1;side+=2){path.reset();path.moveTo(0,-103);path.quadTo(side*35,-115,side*37,-31);strokePath(c,0xffb2dfe3,3);}
        float ripple=reduced?0:(time%1800)/1800f*5;arc(c,-23-ripple,-27,23+ripple,-12,0,360,0x80e4f6ec,1.5f);
    }
    private void pond(Canvas c){
        oval(c,-78,-47,75,1,0xffa8bb9c);oval(c,-69,-46,69,-5,0xffd3cfb0);oval(c,-63,-42,62,-9,0xff8fc5c9);oval(c,-57,-39,56,-13,0xffa4d6d6);
        for(int i=0;i<7;i++){double a=i*.91;float x=(float)Math.cos(a)*66,y=-23+(float)Math.sin(a)*19;oval(c,x-10,y-5,x+10,y+4,0xffc4c8b2);}
        oval(c,-35,-31,-9,-20,0xff85b18a);oval(c,13,-22,35,-13,0xff91bb8f);flower(c,-20,-28,6,0xfff0c4d2);fish(c,20,-33,0xffeab897);
    }
    private void hammock(Canvas c,boolean turned){
        line(c,-61,-3,-54,-80,0xffbd9878,6);line(c,61,-3,54,-80,0xffbd9878,6);line(c,-71,-4,-47,-4,0xff9c846f,5);line(c,47,-4,71,-4,0xff9c846f,5);
        path.reset();path.moveTo(-54,-70);path.quadTo(0,-7,54,-70);path.lineTo(42,-31);path.quadTo(0,4,-42,-31);path.close();shape(c,0xffe4b4cc);
        for(int i=-2;i<=2;i++){path.reset();path.moveTo(-49,-62+i*2);path.quadTo(0,-8+i*3,49,-62+i*2);strokePath(c,i%2==0?0xfff5d9df:0xffc5adc9,2);}
        line(c,-55,-71,-45,-43,0xffe9d7b7,2);line(c,55,-71,45,-43,0xffe9d7b7,2);
    }
    private void swing(Canvas c,boolean turned){
        line(c,-54,-4,-36,-112,0xffb79573,6);line(c,-16,-4,-36,-112,0xffd4b68e,6);line(c,54,-4,36,-112,0xffb79573,6);line(c,16,-4,36,-112,0xffd4b68e,6);line(c,-44,-112,44,-112,0xffd9bc91,8);
        line(c,-24,-109,-24,-40,0xffc0a791,2);line(c,24,-109,24,-40,0xffc0a791,2);round(c,-33,-44,33,-31,4,0xffa9c7b3);round(c,-28,-72,28,-44,8,0xffc2d8bf);line(c,-24,-60,24,-60,0xff95b5a0,2);
    }
    private void bench(Canvas c,boolean turned){
        leg(c,-42,-2,28);leg(c,42,-2,28);for(int i=0;i<3;i++)round(c,-50,-67+i*11,50,-60+i*11,2,i%2==0?0xffb6ccba:0xffc4d7c1);round(c,-51,-34,51,-23,4,0xff9cbaa8);
        arc(c,-59,-48,-37,-17,180,175,0xffb99c7b,5);arc(c,37,-48,59,-17,185,175,0xffb99c7b,5);line(c,-45,-27,45,-27,0xffdbe1c8,2);
    }
    private void picnic(Canvas c,boolean turned){
        leg(c,-38,-3,43);leg(c,38,-3,43);quad(c,-49,-65,43,-65,64,-45,-28,-45,0xffddb891);round(c,-54,-29,54,-19,3,0xffc8a27f);leg(c,-45,-5,17);leg(c,45,-5,17);
        quad(c,-27,-65,20,-65,36,-46,-11,-46,0xfff0d9d2);for(int i=0;i<4;i++)line(c,-22+i*11,-64,-5+i*11,-47,0xffd1aab5,3);line(c,-18,-60,26,-60,0xffd1aab5,3);line(c,-12,-53,32,-53,0xffd1aab5,3);oval(c,32,-63,48,-56,0xfff8ead3);
    }
    private void flowerbed(Canvas c,boolean turned){
        cabinet(c,51,28,0xffb59479,0xffdebea0,turned);oval(c,-47,-32,47,-20,0xff879a74);
        for(int i=0;i<5;i++){float x=-37+i*18,y=-50-(i%2)*15;line(c,x,-26,x,y,0xff83aa7d,2);leaf(c,x,-33,x+9,-42,0xffa7c58d);flower(c,x,y,8,i%2==0?0xffebbbcc:0xfff2d28f);}
    }
    private void gardenPath(Canvas c){
        oval(c,-47,-35,-6,-18,0xffcad0bd);oval(c,6,-25,49,-9,0xffd9ddcc);oval(c,-29,-13,14,1,0xffbac5b3);oval(c,-2,-45,34,-31,0xffe3e5d6);line(c,-37,-29,-15,-29,0xffe6e8d9,2);line(c,18,-19,38,-19,0xffeef0e4,2);
    }
    private void lantern(Canvas c){
        oval(c,-32,-13,32,1,0x20e9c77c);round(c,-19,-64,19,-11,5,0xffa999b4);round(c,-14,-59,14,-17,3,0xffffdda0);line(c,0,-60,0,-18,0xffb59f87,2);line(c,-15,-38,15,-38,0xffb59f87,2);
        arc(c,-14,-89,14,-60,180,180,0xffa999b4,4);quad(c,-24,-64,0,-79,24,-64,0,-58,0xffc9b4cf);round(c,-25,-15,25,-6,3,0xffbaa6c2);star(c,0,-39,7,0xfffff2c6);
    }
    private void canopyBed(Canvas c,boolean turned){
        c.save();c.translate(0,3);c.scale(1.25f,1);bed(c);c.restore();
        for(int side=-1;side<=1;side+=2){line(c,side*55,-12,side*55,-127,0xffb49ac9,4);line(c,side*27,-32,side*27,-145,0xffc9afd8,3);}
        quad(c,-56,-127,-27,-146,29,-146,58,-127,0xffded0e9);quad(c,-56,-127,58,-127,45,-116,-43,-116,0xffc7b4d8);
        path.reset();path.moveTo(-53,-123);path.quadTo(-27,-115,-33,-94);path.lineTo(-45,-34);path.lineTo(-55,-13);path.close();shape(c,0xbff4e4e7);path.reset();path.moveTo(53,-123);path.quadTo(27,-115,33,-94);path.lineTo(45,-34);path.lineTo(55,-13);path.close();shape(c,0xbfece2ee);
        line(c,-52,-50,-41,-54,0xffd0b5ce,3);line(c,52,-50,41,-54,0xffd0b5ce,3);
    }
    private void furnitureWardrobe(Canvas c,boolean turned){
        cabinet(c,45,115,0xffb7a0c7,0xffe9d6e4,turned);round(c,-39,-108,-3,-14,4,0xffe1cce2);round(c,3,-108,39,-14,4,0xffd4bfdd);circle(c,-10,-57,2.5f,0xffbea06f);circle(c,10,-57,2.5f,0xffbea06f);
        line(c,-22,-92,22,-92,0xffc4a7c8,2);heart(c,0,-93,6,0xffedceda);leg(c,-31,-3,8);leg(c,31,-3,8);
    }
    private void bath(Canvas c,boolean turned){
        leg(c,-39,-1,18);leg(c,37,-1,18);oval(c,-61,-62,61,-5,0xffb4cdd0);oval(c,-60,-67,60,-21,0xfff1f0e5);oval(c,-48,-60,48,-29,0xffa8d6d9);
        circle(c,-24,-48,12,0xffeaf5ed);circle(c,-5,-55,16,0xfff4f8ec);circle(c,14,-49,11,0xffe3f0e8);circle(c,29,-54,9,0xfff1f6ed);line(c,-42,-58,-42,-91,0xffbcab95,4);arc(c,-42,-99,-22,-80,180,180,0xffbcab95,4);
    }
    private void mirror(Canvas c,boolean turned){
        line(c,-18,-5,-13,-40,0xffc4a985,5);line(c,18,-5,13,-40,0xffc4a985,5);oval(c,-30,-110,30,-23,0xffd5bb92);oval(c,-24,-103,24,-30,0xffacd2de);quad(c,-18,-78,-6,-97,19,-54,8,-34,0x7bffffff);line(c,-26,-4,26,-4,0xffbaa085,5);circle(c,turned?-25:25,-57,3,0xffe9d3a9);
    }
    private void kitchen(Canvas c,boolean turned){
        cabinet(c,66,62,0xffa9c6bb,0xffdbe6ce,turned);round(c,-70,-67,70,-54,4,0xffeed8bb);round(c,-61,-48,-19,-14,3,0xff678e91);round(c,-56,-43,-24,-19,2,0xffb9d0cf);line(c,-52,-40,-28,-40,0xffebebd9,2);round(c,4,-48,60,-13,3,0xffc4d8c5);line(c,32,-47,32,-15,0xff9eb9a9,2);circle(c,23,-29,2,0xffaf9675);circle(c,40,-29,2,0xffaf9675);
        for(int i=0;i<2;i++){oval(c,-57+i*24,-74,-39+i*24,-65,0xff786d75);oval(c,-53+i*24,-72,-43+i*24,-67,0xffbfa7a0);}
        oval(c,18,-75,59,-63,0xffa5bdbe);oval(c,22,-73,55,-65,0xffd9e4d7);line(c,45,-72,45,-96,0xffb9a88c,3);arc(c,31,-104,46,-88,180,180,0xffb9a88c,3);
    }
    private void chessTable(Canvas c,boolean turned){
        leg(c,-33,-3,35);leg(c,33,-3,35);quad(c,-49,-53,15,-68,51,-46,-13,-30,0xffb6977e);
        for(int x=0;x<6;x++)for(int y=0;y<6;y++){float xx=-42+x*9+y*5,yy=-51-x*2.1f+y*3.1f;quad(c,xx,yy,xx+9,yy-2.1f,xx+14,yy+1,xx+5,yy+3.1f,(x+y)%2==0?0xffeee2c8:0xff7e7480);}
        for(int i=0;i<3;i++){float x=-20+i*16,y=-45-i*3;line(c,x,y,x,y-12,0xfffcf1d5,3);circle(c,x,y-13,3,0xfffcf1d5);oval(c,x-4,y-2,x+4,y+1,0xffd5c4a7);}
    }
    private void flower(Canvas c,float x,float y,float radius,int color){for(int n=0;n<5;n++){double a=n*Math.PI*2/5;circle(c,x+(float)Math.cos(a)*radius*.65f,y+(float)Math.sin(a)*radius*.65f,radius*.46f,color);}circle(c,x,y,radius*.32f,0xfff3dc98);}

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
        float x=p.x,y=p.y,w=p.width(),d=p.depth();quad(c,ORIGIN_X+(x-y)*TILE_X,ORIGIN_Y+(x+y)*TILE_Y,ORIGIN_X+(x+w-y)*TILE_X,ORIGIN_Y+(x+w+y)*TILE_Y,ORIGIN_X+(x+w-y-d)*TILE_X,ORIGIN_Y+(x+w+y+d)*TILE_Y,ORIGIN_X+(x-y-d)*TILE_X,ORIGIN_Y+(x+y+d)*TILE_Y,color);
    }
    private void outlineFootprint(Canvas c,HomeLayout.Placement p,int color,float width){
        float x=p.x,y=p.y,w=p.width(),d=p.depth();path.reset();path.moveTo(ORIGIN_X+(x-y)*TILE_X,ORIGIN_Y+(x+y)*TILE_Y);path.lineTo(ORIGIN_X+(x+w-y)*TILE_X,ORIGIN_Y+(x+w+y)*TILE_Y);path.lineTo(ORIGIN_X+(x+w-y-d)*TILE_X,ORIGIN_Y+(x+w+y+d)*TILE_Y);path.lineTo(ORIGIN_X+(x-y-d)*TILE_X,ORIGIN_Y+(x+y+d)*TILE_Y);path.close();strokePath(c,color,width);
    }
    private void wallClip(Canvas c,boolean left){path.reset();path.moveTo(left?27:195,left?128:35);path.lineTo(left?195:363,left?35:128);path.lineTo(left?195:363,left?145:238);path.lineTo(left?27:195,left?238:145);path.close();c.clipPath(path);}
    private void leg(Canvas c,float x,float y,float h){line(c,x,y-h,x,y,WOOD_DARK,5);line(c,x-1,y-h,x-1,y-2,WOOD,2);}
    private void leaf(Canvas c,float x,float y,float xx,float yy,int color){path.reset();path.moveTo(x,y);path.quadTo(xx+12,yy-9,xx,yy);path.quadTo(xx-7,yy+11,x,y);path.close();shape(c,color);}
    private void book(Canvas c,float x,float y,float w,float h,int color){round(c,x,y,x+w,y+h,1,color);line(c,x+2,y+4,x+w-2,y+4,0x90fff9e9,1);}
    private static float depth(HomeLayout.Placement p){return !HomeCatalog.item(p.itemId).solid?-1:p.x+p.y+(p.width()+p.depth())*.5f;}
    private static float itemWidth(String id){if(isNew(id)){HomeCatalog.Item item=HomeCatalog.item(id);return Math.max(54,(item.width+item.depth)*26f);}return "bed".equals(id)||"sofa".equals(id)?93:"rug".equals(id)?108:"bookshelf".equals(id)?58:"table".equals(id)?68:"cushion".equals(id)?60:"mobile".equals(id)?59:"plant".equals(id)?49:"lamp".equals(id)||"gate_lamp".equals(id)?55:"plush".equals(id)?45:49;}
    private static float naturalWidth(String id){if(isNew(id))return newWidth(id);return "bed".equals(id)||"sofa".equals(id)?96:"rug".equals(id)?108:"bookshelf".equals(id)?64:"table".equals(id)?80:"cushion".equals(id)?74:"mobile".equals(id)?79:"plant".equals(id)?60:"lamp".equals(id)?64:"gate_lamp".equals(id)?50:"plush".equals(id)?56:64;}
    private static float naturalHeight(String id){if(isNew(id))return newHeight(id);return "rug".equals(id)?40:"cushion".equals(id)?40:"table".equals(id)?54:"stool".equals(id)?50:"plush".equals(id)?70:"plant".equals(id)?90:"bookshelf".equals(id)||"mobile".equals(id)?92:"lamp".equals(id)?88:"gate_lamp".equals(id)?82:"bed".equals(id)?75:66;}
    private static float itemHeight(String id){if(isNew(id))return newHeight(id)*.96f;return "rug".equals(id)?47:"cushion".equals(id)?34:"table".equals(id)?57:"stool".equals(id)?48:"plush".equals(id)?58:"plant".equals(id)?73:"bookshelf".equals(id)||"mobile".equals(id)?82:"lamp".equals(id)||"gate_lamp".equals(id)?82:76;}
    private static boolean isNew(String id){return HomeCatalog.item(id)!=null&&HomeCatalog.ITEMS.indexOf(HomeCatalog.item(id))>=12;}
    private static float newWidth(String id){switch(id){case "greenhouse":return 162;case "cherry_tree":return 138;case "pond":return 158;case "canopy_bed":return 132;case "piano":return 148;case "library":case "bay_window":case "kitchen":return 148;case "aquarium":case "hammock":return 144;case "picnic":case "bath":return 132;case "bench":case "swing":return 124;case "flowerbed":case "fireplace":return 126;case "desk":return 114;case "telescope":case "chess_table":return 108;case "fountain":return 112;case "wardrobe":return 108;case "garden_path":return 104;case "easel":return 90;case "record_player":case "bonsai":return 86;case "terrarium":case "arcade":return 80;case "mirror":case "cactus":return 66;default:return 60;}}
    private static float newHeight(String id){switch(id){case "canopy_bed":return 152;case "cherry_tree":return 150;case "greenhouse":return 142;case "piano":case "library":case "bay_window":case "wardrobe":case "swing":case "telescope":case "mirror":case "fountain":return 122;case "arcade":return 116;case "aquarium":case "kitchen":case "desk":case "easel":case "cactus":return 106;case "fireplace":return 102;case "terrarium":case "bonsai":case "lantern":case "record_player":return 94;case "bath":return 104;case "hammock":return 90;case "flowerbed":return 82;case "bench":return 76;case "picnic":case "chess_table":return 74;case "pond":return 52;case "garden_path":return 48;default:return 96;}}
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
