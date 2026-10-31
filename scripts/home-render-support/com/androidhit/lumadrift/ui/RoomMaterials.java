package com.androidhit.lumadrift.ui;
import android.graphics.Canvas;
/** Geometry-only host substitute; native material pixels are inspected separately. */
public final class RoomMaterials {
    public static boolean wall(Canvas c,boolean left,boolean bedroom,int tint){return false;}
    public static boolean wall(Canvas c,boolean left,String room,int tint){return false;}
    public static boolean floor(Canvas c,int tint){return false;}
    public static boolean floor(Canvas c,String room,int tint){return false;}
}
