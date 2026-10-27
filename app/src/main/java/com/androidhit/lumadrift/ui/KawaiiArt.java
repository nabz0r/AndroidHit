package com.androidhit.lumadrift.ui;

import android.graphics.*;

/** Original vector inventory art, without downloaded sprites, fonts or bitmap allocations. */
public final class KawaiiArt {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private static final int INK=0xff20253e,WHITE=0xfffffcff;
    private void circle(Canvas c,float x,float y,float r,int color){p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,r,p);}
    private void line(Canvas c,float x,float y,float xx,float yy,int col,float w){p.setColor(col);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(w);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,p);p.setStyle(Paint.Style.FILL);}
    private void oval(Canvas c,float x,float y,float xx,float yy,int col){p.setColor(col);p.setShader(null);p.setStyle(Paint.Style.FILL);c.drawOval(x,y,xx,yy,p);}
    private void rounded(Canvas c,float x,float y,float w,float h,float r,int col){p.setColor(col);p.setShader(null);p.setStyle(Paint.Style.FILL);c.drawRoundRect(x,y,x+w,y+h,r,r,p);}
    private void star(Canvas c,float x,float y,float r,int color){path.reset();for(int i=0;i<10;i++){double a=i*Math.PI/5-Math.PI/2;float rr=i%2==0?r:r*.48f;float xx=x+(float)Math.cos(a)*rr,yy=y+(float)Math.sin(a)*rr;if(i==0)path.moveTo(xx,yy);else path.lineTo(xx,yy);}path.close();p.setColor(color);p.setShader(null);c.drawPath(path,p);}
    private void face(Canvas c,float x,float y,float r){oval(c,x-r*.35f,y-r*.10f,x-r*.22f,y+r*.18f,INK);oval(c,x+r*.22f,y-r*.10f,x+r*.35f,y+r*.18f,INK);circle(c,x-r*.29f,y-r*.04f,r*.035f,WHITE);circle(c,x+r*.28f,y-r*.04f,r*.035f,WHITE);line(c,x-r*.06f,y+r*.22f,x+r*.06f,y+r*.22f,INK,r*.055f);oval(c,x-r*.55f,y+r*.13f,x-r*.34f,y+r*.24f,0xffffa9bf);oval(c,x+r*.34f,y+r*.13f,x+r*.55f,y+r*.24f,0xffffa9bf);}
    public int powerColor(String id){switch(id){case "hint":return 0xffffda80;case "vision":return 0xffb5a2ff;case "shield":return 0xff8edaff;case "focus":return 0xff88ebcb;case "trail":return 0xffffb3cc;default:return 0xffffbd93;}}
    public void power(Canvas c,String id,float x,float y,float r,long time,boolean still){int col=powerColor(id);c.save();c.translate(x,y);float bob=still?0:(float)Math.sin(time*.003+id.hashCode())*r*.06f;c.translate(0,bob);circle(c,0,2,r*1.13f,0x2320273f);p.setShader(new RadialGradient(0,0,r,new int[]{0xfffef2ff,col},null,Shader.TileMode.CLAMP));p.setColor(WHITE);c.drawCircle(0,0,r,p);p.setShader(null);
        if(id.equals("hint")){star(c,0,-r*.22f,r*.62f,0xffffba5c);rounded(c,-r*.23f,r*.48f,r*.46f,r*.24f,r*.06f,0xffad805d);for(int i=-1;i<=1;i++)line(c,i*r*.76f,-r*.91f,i*r*.89f,-r*1.14f,col,r*.09f);}
        else if(id.equals("vision")){oval(c,-r*.72f,-r*.46f,r*.72f,r*.36f,WHITE);circle(c,0,-r*.05f,r*.34f,0xff8171ce);circle(c,0,-r*.05f,r*.16f,INK);circle(c,-r*.09f,-r*.16f,r*.07f,WHITE);}
        else if(id.equals("shield")){path.reset();path.moveTo(0,-r*.78f);path.lineTo(r*.63f,-r*.47f);path.lineTo(r*.48f,r*.33f);path.lineTo(0,r*.73f);path.lineTo(-r*.48f,r*.33f);path.lineTo(-r*.63f,-r*.47f);path.close();p.setColor(0xff568bc5);c.drawPath(path,p);star(c,0,-r*.05f,r*.36f,WHITE);}
        else if(id.equals("focus")){for(int s:new int[]{-1,1}){circle(c,s*r*.34f,-r*.15f,r*.31f,INK);circle(c,s*r*.34f,-r*.15f,r*.23f,WHITE);circle(c,s*r*.34f,-r*.15f,r*.12f,0xff419d8d);}line(c,-r*.12f,-r*.18f,r*.12f,-r*.18f,INK,r*.08f);}
        else if(id.equals("trail")){for(int i=0;i<3;i++){star(c,-r*.5f+i*r*.46f,-r*.5f+i*r*.31f,r*(.18f+i*.12f),i==2?WHITE:0xffce608c);}line(c,-r*.76f,r*.3f,-r*.32f,r*.3f,WHITE,r*.07f);}
        else{oval(c,-r*.75f,-r*.3f,r*.74f,r*.47f,WHITE);star(c,r*.15f,-r*.48f,r*.4f,0xffffac63);line(c,-r*.9f,r*.65f,-r*.32f,r*.65f,WHITE,r*.1f);}
        if(!id.equals("vision")&&!id.equals("focus")&&!id.equals("shield"))face(c,0,r*.15f,r*.55f);star(c,r*.84f,-r*.85f,r*.21f,WHITE);c.restore();}
    public void cosmetic(Canvas c,String slot,int variant,float x,float y,float r,int color){c.save();c.translate(x,y);variant=Math.floorMod(variant,6);
        if(slot.equals("HAT")){oval(c,-r*.9f,r*.17f,r*.9f,r*.46f,color);{rounded(c,-r*.58f,-r*.55f,r*1.16f,r*.82f,r*.25f,color);if(variant==1){circle(c,0,-r*.57f,r*.27f,0xffffdd92);circle(c,r*.13f,-r*.67f,r*.24f,color);}if(variant==2)star(c,0,-r*.54f,r*.35f,0xffffe09b);if(variant==3){line(c,0,-r*.52f,r*.12f,-r*.82f,0xff67b490,r*.15f);oval(c,r*.04f,-r*.87f,r*.45f,-r*.63f,0xff88d6a7);}if(variant==4){oval(c,-r*.85f,-r*.45f,-r*.45f,-r*.03f,WHITE);oval(c,r*.45f,-r*.45f,r*.85f,-r*.03f,WHITE);}if(variant==5){circle(c,0,-r*.65f,r*.29f,0xffffb4ce);star(c,0,-r*.65f,r*.17f,WHITE);}star(c,r*.26f,-r*.05f,r*.17f,WHITE);}}
        else if(slot.equals("DRESS")){path.reset();path.moveTo(-r*.42f,-r*.7f);path.lineTo(r*.42f,-r*.7f);path.lineTo(r*.84f,r*.65f);path.quadTo(0,r*.96f,-r*.84f,r*.65f);path.close();p.setColor(color);c.drawPath(path,p);line(c,-r*.49f,-r*.26f,r*.49f,-r*.26f,WHITE,r*.09f);star(c,0,-r*.19f,r*.19f,0xffffde98);for(int i=-1;i<=1;i++)star(c,i*r*.4f,r*.4f,r*.09f,WHITE);}
        else if(slot.equals("SHOES")){for(int s:new int[]{-1,1}){rounded(c,s*r*.5f-r*.35f,-r*.25f,r*.7f,r*.62f,r*.21f,color);rounded(c,s*r*.5f-r*.38f,r*.17f,r*.76f,r*.22f,r*.09f,WHITE);star(c,s*r*.5f,-r*.02f,r*.11f,WHITE);}}
        else{for(int s:new int[]{-1,1}){oval(c,s*r*.68f-r*.3f,-r*.43f,s*r*.68f+r*.3f,r*.34f,color);circle(c,s*r*.47f,0,r*.17f,color);rounded(c,s*r*.68f-r*.22f,r*.21f,r*.44f,r*.18f,r*.06f,WHITE);star(c,s*r*.68f,-r*.07f,r*.12f,WHITE);}}c.restore();}
}
