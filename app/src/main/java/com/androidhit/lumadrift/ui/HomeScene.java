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

/** Original editable cutaway and raster furnishings, with one camera for drawing and hit targets. */
public final class HomeScene {
    public static final float WIDTH = 390, HEIGHT = 440;
    public static final float DETAIL_ZOOM = 2.0f;
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
    private float viewportWidth=WIDTH,viewportHeight=HEIGHT;
    private float cameraZoom=DETAIL_ZOOM,cameraPanX,cameraPanY;
    private String room="living";
    private static final float WORLD_WIDTH=640, WORLD_HEIGHT=480;
    public void setRoom(String value){room=HomeCatalog.validRoom(value)?value:HomeCatalog.DEFAULT_ROOM;}
    public String room(){return room;}
    /**
     * The host supplies its usable scene area directly, without applying a second fit scale.
     * Keep the user's zoom and re-clamp pan when controls or the display size change.
     */
    public void setViewport(float width,float height){
        viewportWidth=finite(width)&&width>=1&&width<=4096?width:WIDTH;
        viewportHeight=finite(height)&&height>=1&&height<=4096?height:HEIGHT;
        setCamera(cameraZoom,cameraPanX,cameraPanY);
    }
    public float viewportWidth(){return viewportWidth;}
    public float viewportHeight(){return viewportHeight;}
    /** Pan is expressed in viewport logical pixels. Invalid gesture values leave a safe fit view. */
    public void setCamera(float zoom,float panX,float panY){
        cameraZoom=finite(zoom)?clamp(zoom,1f,2.5f):1f;
        float scale=cameraScale(),maxX=Math.max(0,(WORLD_WIDTH*scale-viewportWidth)*.5f),maxY=Math.max(0,(WORLD_HEIGHT*scale-viewportHeight)*.5f);
        cameraPanX=finite(panX)?clamp(panX,-maxX,maxX):0;cameraPanY=finite(panY)?clamp(panY,-maxY,maxY):0;
    }
    /** Return to the close room view used on first arrival. */
    public void resetCamera(){setCamera(DETAIL_ZOOM,0,0);}
    /** Fit the entire cutaway into the current viewport. */
    public void resetOverview(){setCamera(1,0,0);}
    public float cameraZoom(){return cameraZoom;}
    public float cameraPanX(){return cameraPanX;}
    public float cameraPanY(){return cameraPanY;}
    private float cameraScale(){return Math.min(viewportWidth/WORLD_WIDTH,viewportHeight/WORLD_HEIGHT)*cameraZoom;}
    private float projectX(float value){return viewportWidth*.5f+(value-ORIGIN_X)*cameraScale()+cameraPanX;}
    private float projectY(float value){return viewportHeight*.5f+(value-WORLD_HEIGHT*.5f)*cameraScale()+cameraPanY;}
    private float worldX(float value){return (value-viewportWidth*.5f-cameraPanX)/cameraScale()+ORIGIN_X;}
    private float worldY(float value){return (value-viewportHeight*.5f-cameraPanY)/cameraScale()+WORLD_HEIGHT*.5f;}
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
        c.save();c.clipRect(0,top,viewportWidth,top+viewportHeight);c.translate(viewportWidth*.5f+cameraPanX,top+viewportHeight*.5f+cameraPanY);c.scale(cameraScale(),cameraScale());c.translate(-ORIGIN_X,-WORLD_HEIGHT*.5f);
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
        quad(c,40,295,320,450,320,464,40,309,garden?0xff9cb79b:0xffbc8e75);
        quad(c,320,450,600,295,600,309,320,464,garden?0xff839e88:0xff9a7289);
        line(c,42,303,320,457,garden?0xffcae0b5:0xffedc897,2.2f);
        line(c,320,457,598,303,garden?0xffb2ce9c:0xffd8a9a0,2.2f);
        if(garden){
            // A low garden fence and an open sky distinguish the outdoor room structurally.
            quad(c,40,257,320,102,320,140,40,295,blend(left,0xff96bc8b,.36f));quad(c,320,102,600,257,600,295,320,140,blend(right,0xff89ad87,.36f));
            for(int n=0;n<=10;n++){float x=40+n*28,y=295-n*15.5f;line(c,x,y-38,x,y,0xfff1e5cb,8);float rx=320+n*28,ry=140+n*15.5f;line(c,rx,ry-38,rx,ry,0xffeadcc0,8);}
            line(c,40,264,320,109,0xfffff1d4,5);line(c,320,109,600,264,0xffe5d1b6,5);
            circle(c,392,48,25,0x48ffe3a0);oval(c,91,50,188,75,0x55ffffff);oval(c,130,40,210,70,0x55ffffff);
        }else{
            quad(c,40,155,320,0,320,140,40,295,left);quad(c,320,0,600,155,600,295,320,140,right);
            RoomMaterials.wall(c,true,bedroom,left);RoomMaterials.wall(c,false,bedroom,right);
            wallMaterials(c,true,left,bedroom);wallMaterials(c,false,right,bedroom);
            line(c,40,155,320,0,0xffa87d95,7);line(c,320,0,600,155,0xff8e739e,7);
            line(c,40,153,320,-2,0xffffe7b9,3);line(c,320,-2,600,153,0xffffe4b4,3);
            line(c,40,155,40,295,0xffdfbc9a,6);line(c,600,155,600,295,0xffb49abe,6);
            line(c,43,157,43,294,0xfffff2d1,1.7f);line(c,597,157,597,294,0xfff4d5dc,1.7f);
        }
        int grass=has(floor,"cloud")?0xffc8d7b8:has(floor,"rose")?0xffd2c8aa:has(floor,"mint")?0xffa9cbb0:0xffbed3aa;
        fill(garden?grass:ground==null?0xffe6ceb0:blend(ground.color,WHITE,.18f));c.drawPath(floorPath,paint);
        boolean paintedFloor=!garden&&RoomMaterials.floor(c,ground==null?0xfff8eedc:blend(ground.color,WHITE,.65f));
        c.save();c.clipPath(floorPath);
        if(garden){
            for(int x=0;x<10;x++)for(int y=0;y<10;y++){float xx=px(x,y),yy=py(x,y);line(c,xx-4,yy+2,xx-2,yy-2,0x4f73996d,1.2f);line(c,xx-2,yy-2,xx+1,yy+1,0x4f73996d,1.2f);}
        }else{
            for(int n=0;!paintedFloor&&n<=20;n++){
                line(c,320+n*14,140+n*7.75f,40+n*14,295+n*7.75f,0x35a37a5b,1.1f);
                line(c,320+n*14+1.4f,140+n*7.75f,40+n*14+1.4f,295+n*7.75f,0x42fff8db,.65f);
            }
            for(int x=0;!paintedFloor&&x<10;x++)for(int y=0;y<10;y++){
                float xx=px(x,y),yy=py(x,y);
                float off=((x+y)%2==0?1:-1)*4;
                line(c,xx+off-6,yy-3,xx+off+5,yy+3,0x16846561,.6f);
                if((x*3+y)%5==0)oval(c,xx-3,yy-1.5f,xx+3,yy+1.5f,0x13805e69);
            }
            // Window-light quadrants give the editable floor a material surface and a stable light direction.
            quad(c,124,230,199,189,369,324,294,365,0x26fff4c5);
            quad(c,142,239,171,222,319,339,290,356,0x21fff8d4);
            quad(c,178,218,202,205,350,322,326,335,0x17fff9dc);
        }
        c.restore();
        if(!garden){
            line(c,40,290,320,135,0xffba977e,7);line(c,320,135,600,290,0xffa98ba8,7);
            line(c,40,287,320,132,0xffffedc6,2);line(c,320,132,600,287,0xfff0d9e3,2);
            c.save();c.translate(70,0);window(c,time,reduceMotion);c.restore();
            wardrobe(c);
        }
        c.save();c.translate(210,32);puzzleDoor(c);c.restore();
    }

    /** Subtle woven paper and gilded panel work remain tied to the player's chosen wall colour. */
    private void wallMaterials(Canvas c,boolean left,int color,boolean bedroom){
        c.save();path.reset();
        if(left){path.moveTo(40,155);path.lineTo(320,0);path.lineTo(320,140);path.lineTo(40,295);}
        else{path.moveTo(320,0);path.lineTo(600,155);path.lineTo(600,295);path.lineTo(320,140);}
        path.close();c.clipPath(path);
        for(int n=0;n<=10;n++){
            float x=(left?40:320)+n*28,y=left?155-n*15.5f:n*15.5f;
            line(c,x,y+6,x,y+133,bedroom?0x38fff6ed:0x187a607e,bedroom?5.5f:.8f);
            if(n%2==0){
                float bx=x+14,by=y+(left?-7.75f:7.75f);
                line(c,bx,by+41,bx,by+105,0x2ac99f79,.65f);
                diamond(c,bx,by+42,2.1f,3.2f,0x39c79e79);
                diamond(c,bx,by+106,2.1f,3.2f,0x39c79e79);
            }
        }
        for(int n=0;n<5;n++){
            float x=(left?55:335)+n*56,y=left?155-(x-40)*.554f:(x-320)*.554f;
            line(c,x,y+114,x+42,y+114+(left?-23.25f:23.25f),blend(color,WHITE,.60f),1.3f);
            line(c,x,y+116,x+42,y+116+(left?-23.25f:23.25f),0x24a27d87,.8f);
        }
        c.restore();
    }

    private void window(Canvas c,long time,boolean reduced) {
        ArtLibrary art=CharacterSprites.library();if(art==null)return;
        art.draw(c,"furniture/bay_window",55,62,175,189,.5f,1,255);
    }

    private void wardrobe(Canvas c) {
        ArtLibrary art=CharacterSprites.library();if(art==null)return;
        oval(c,420,222,479,236,0x205d405f);
        art.draw(c,"furniture/wardrobe",421,82,493,216,.5f,1,255);
    }

    private void puzzleDoor(Canvas c) {
        c.save();c.translate(332,183);c.skew(0,.554f);
        path.reset();path.moveTo(-23,35);path.lineTo(-23,-29);path.cubicTo(-23,-60,23,-60,23,-29);path.lineTo(23,35);path.close();shape(c,0xff795b8b);
        path.reset();path.moveTo(-20,34);path.lineTo(-20,-29);path.cubicTo(-20,-56,20,-56,20,-29);path.lineTo(20,34);path.close();shape(c,0xffffdc9a);
        path.reset();path.moveTo(-16,33);path.lineTo(-16,-28);path.cubicTo(-16,-50,16,-50,16,-28);path.lineTo(16,33);path.close();shape(c,0xff547da5);
        arc(c,-14,-43,14,-17,180,180,0xffbadfe7,2.3f);
        line(c,0,-40,0,-19,0xffe8d6a7,1.5f);line(c,-13,-28,13,-28,0xffe8d6a7,1.5f);
        round(c,-12,-13,12,27,4,0xff6b9ba7);round(c,-9,-10,9,11,3,0xff8ebcba);
        star(c,0,-1,7,0xffffe8ae);
        circle(c,11,15,3.0f,0xff866685);circle(c,10.5f,14.3f,2.1f,0xffffda84);
        round(c,-25,31,25,37,2.5f,0xffb3888f);line(c,-24,32,24,32,0xffffdbb0,1.5f);
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
        boundsFor(p,itemBounds);
        c.save();
        drawItem(c,p.itemId,itemBounds,time,reduced,p.rotation);
        c.restore();
    }

    /** Draw the same distinctive object in room or catalogue; no dependency on ownership. */
    public void drawItem(Canvas c,String id,RectF bounds,long time,boolean reduceMotion){drawItem(c,id,bounds,time,reduceMotion,0);}
    /** A saved quarter-turn safely mirrors the authored isometric view inside the same measured bounds. */
    public void drawItem(Canvas c,String id,RectF bounds,long time,boolean reduceMotion,int rotation) {
        if(c==null||bounds==null||bounds.width()<=0||bounds.height()<=0)return;
        ArtLibrary art=CharacterSprites.library();if(art==null||HomeCatalog.item(id)==null)return;
        c.save();
        if((rotation&1)!=0)c.scale(-1,1,bounds.centerX(),bounds.bottom);
        // The same outer bounds are used for the room's touch target and bitmap fit.
        art.draw(c,"furniture/"+id,bounds.left,bounds.top,bounds.right,bounds.bottom,.5f,1,255);
        boolean active="lamp".equals(id)?lampOn:"gate_lamp".equals(id)?gateLampOn:false;
        if(active&&("lamp".equals(id)||"gate_lamp".equals(id)||"lantern".equals(id))){
            float unit=Math.min(bounds.width(),bounds.height());
            oval(c,bounds.centerX()-unit*.22f,bounds.top+unit*.08f,bounds.centerX()+unit*.22f,bounds.top+unit*.49f,0x28ffe5a3);
            star(c,bounds.centerX()+unit*.23f,bounds.top+unit*.12f,unit*.035f,0xbffff1b3);
        }
        c.restore();
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
            if("plush".equals(interactionItem)){itemBounds.set(xx-11,yy-35,xx+11,yy-10);drawItem(c,"plush",itemBounds,0,true);}
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
    private void diamond(Canvas c,float x,float y,float w,float h,int color){path.reset();path.moveTo(x,y-h);path.lineTo(x+w,y);path.lineTo(x,y+h);path.lineTo(x-w,y);path.close();shape(c,color);}
    private void heart(Canvas c,float x,float y,float r,int color){path.reset();path.moveTo(x,y+r*.66f);path.cubicTo(x-r*1.35f,y,x-r*.7f,y-r*1.16f,x,y-r*.38f);path.cubicTo(x+r*.7f,y-r*1.16f,x+r*1.35f,y,x,y+r*.66f);path.close();shape(c,color);}
    private void crescent(Canvas c,float x,float y,float r,int color){path.reset();path.moveTo(x+r*.5f,y-r*.86f);path.cubicTo(x-r*.55f,y-r*1.1f,x-r*1.15f,y+r*.12f,x-r*.6f,y+r*.7f);path.cubicTo(x,y+r*1.2f,x+r*.7f,y+r*.7f,x+r*.84f,y+r*.3f);path.cubicTo(x,y+r*.6f,x-r*.1f,y-r*.3f,x+r*.5f,y-r*.86f);path.close();shape(c,color);}
}
