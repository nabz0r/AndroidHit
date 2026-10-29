package com.androidhit.lumadrift.ui;

/** Shared logical geometry for the 1.21 hero home. Physical touch sizes never shrink. */
public final class Menu121Layout {
    public static final float NAV_RESERVE=100f, SHOP_CONTENT_HEIGHT=952f;
    public final float contentHeight;
    public final Menu120Layout.Rect profile,settings,hero,avatar,style,evolution,team,play,gifts,shop,map,expedition,events,room,atelier;
    public final Menu120Layout.Rect[] environments;
    public Menu121Layout(float viewportHeight,float minimumTouch){
        float touch=Float.isFinite(minimumTouch)?Math.max(48f,minimumTouch):48f;
        float height=Float.isFinite(viewportHeight)?Math.max(180f,viewportHeight):700f;
        settings=new Menu120Layout.Rect(366-Math.max(48f,touch),12,366,12+Math.max(52f,touch));
        profile=new Menu120Layout.Rect(24,10,Math.min(308,settings.left-10),10+Math.max(62f,touch));
        // A complete dressed character owns the centre; gameplay and style controls have separate targets.
        hero=new Menu120Layout.Rect(24,116,366,464+Math.max(0,touch-48));
        avatar=new Menu120Layout.Rect(32,124,358,382);
        float halfAction=Math.max(48f,touch);
        evolution=new Menu120Layout.Rect(34,hero.bottom-12-halfAction,190,hero.bottom-12);
        style=new Menu120Layout.Rect(200,hero.bottom-12-halfAction,356,hero.bottom-12);
        play=new Menu120Layout.Rect(24,hero.bottom+14,366,hero.bottom+14+Math.max(64f,touch));
        int columns=touch<=79.5f?4:2;float quickWidth=(342f-(columns-1)*8f)/columns,quickHeight=Math.max(94f,touch),quickTop=play.bottom+14;
        environments=new Menu120Layout.Rect[4];for(int i=0;i<4;i++){float left=24+(i%columns)*(quickWidth+8),top=quickTop+(i/columns)*(quickHeight+8);environments[i]=new Menu120Layout.Rect(left,top,left+quickWidth,top+quickHeight);}
        float afterRooms=environments[3].bottom+14;
        team=new Menu120Layout.Rect(24,afterRooms,366,afterRooms+Math.max(116f,touch+64));
        float y=team.bottom+16;
        gifts=new Menu120Layout.Rect(24,y,188,y+132);shop=new Menu120Layout.Rect(202,y,366,y+132);y=gifts.bottom+16;
        float shortcut=Math.max(80f,touch);
        map=new Menu120Layout.Rect(24,y,130,y+shortcut);expedition=new Menu120Layout.Rect(142,y,248,y+shortcut);events=new Menu120Layout.Rect(260,y,366,y+shortcut);y=map.bottom+12;
        room=new Menu120Layout.Rect(24,y,188,y+Math.max(54f,touch));atelier=new Menu120Layout.Rect(202,y,366,y+Math.max(54f,touch));
        contentHeight=Math.max(height,room.bottom+NAV_RESERVE);
    }
}
