package com.androidhit.lumadrift.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Immutable ten-by-ten room state with exact legacy six-by-six decoding. No Android dependency or hidden relocation. */
public final class HomeLayout {
    public static final int SIZE=HomeCatalog.SIZE;
    public static final class Cell {
        public final int x,y;
        public Cell(int x,int y){this.x=x;this.y=y;}
        @Override public boolean equals(Object other){return other instanceof Cell&&((Cell)other).x==x&&((Cell)other).y==y;}
        @Override public int hashCode(){return x*31+y;}
    }
    public static final class Placement {
        public final String id,itemId;
        public final int x,y,rotation;
        public Placement(String id,int x,int y,int rotation){this.id=id;this.itemId=id;this.x=x;this.y=y;this.rotation=rotation;}
        public int width(){HomeCatalog.Item item=HomeCatalog.item(id);return item==null?0:rotation==1?item.depth:item.width;}
        public int depth(){HomeCatalog.Item item=HomeCatalog.item(id);return item==null?0:rotation==1?item.width:item.depth;}
        public boolean covers(int cellX,int cellY){return cellX>=x&&cellY>=y&&cellX<x+width()&&cellY<y+depth();}
    }
    public final int companionX,companionY;
    private final List<Placement> objects;
    public HomeLayout(List<Placement> placements,int companionX,int companionY){
        if(placements==null)throw new IllegalArgumentException("Missing room placements");
        this.objects=Collections.unmodifiableList(new ArrayList<>(placements));this.companionX=companionX;this.companionY=companionY;
        if(!valid())throw new IllegalArgumentException("Invalid room layout");
    }
    public static HomeLayout starter(){return starter(HomeCatalog.DEFAULT_ROOM);}
    public static HomeLayout starter(String room){
        if(!HomeCatalog.validRoom(room))throw new IllegalArgumentException("Unknown room");
        if("bedroom".equals(room)){List<Placement> p=new ArrayList<>();p.add(new Placement("bed",2,1,0));p.add(new Placement("lamp",7,1,0));p.add(new Placement("rug",4,4,0));p.add(new Placement("cushion",7,6,0));return new HomeLayout(p,5,5);}
        if("garden".equals(room)){List<Placement> p=new ArrayList<>();p.add(new Placement("plant",1,1,0));p.add(new Placement("flowerbed",5,1,0));p.add(new Placement("garden_path",4,4,0));return new HomeLayout(p,5,5);}
        List<Placement> p=new ArrayList<>();p.add(new Placement("bed",0,0,0));p.add(new Placement("lamp",5,0,0));
        p.add(new Placement("plant",0,5,0));p.add(new Placement("rug",2,2,0));p.add(new Placement("cushion",4,4,0));
        return new HomeLayout(p,3,3);
    }
    public List<Placement> placements(){return objects;}
    public Placement placement(String id){for(Placement p:objects)if(p.id.equals(id))return p;return null;}
    public static boolean inBounds(int x,int y){return x>=0&&y>=0&&x<SIZE&&y<SIZE;}
    public boolean isBlocked(int x,int y){
        if(!inBounds(x,y))return true;
        for(Placement p:objects){HomeCatalog.Item item=HomeCatalog.item(p.id);if(item!=null&&item.solid&&p.covers(x,y))return true;}return false;
    }
    private boolean valid(){
        if(!inBounds(companionX,companionY))return false;
        Set<String> seen=new HashSet<>();
        for(int i=0;i<objects.size();i++){
            Placement a=objects.get(i);if(a==null)return false;HomeCatalog.Item item=HomeCatalog.item(a.id);
            if(item==null||!seen.add(a.id)||(a.rotation!=0&&a.rotation!=1)||!inBounds(a.x,a.y)||a.x+a.width()>SIZE||a.y+a.depth()>SIZE)return false;
            if(!item.solid)continue;
            if(a.covers(companionX,companionY))return false;
            for(int j=0;j<i;j++){Placement b=objects.get(j);HomeCatalog.Item bItem=HomeCatalog.item(b.id);if(bItem.solid&&a.x<b.x+b.width()&&a.x+a.width()>b.x&&a.y<b.y+b.depth()&&a.y+a.depth()>b.y)return false;}
        }return true;
    }
    /** Refused placement returns null; this state never changes. Rugs may underlay furniture. */
    public HomeLayout tryPlace(String id,int x,int y,int rotation){
        if(HomeCatalog.item(id)==null)return null;List<Placement> next=new ArrayList<>();for(Placement p:objects)if(!p.id.equals(id))next.add(p);next.add(new Placement(id,x,y,rotation));
        try{return new HomeLayout(next,companionX,companionY);}catch(IllegalArgumentException invalid){return null;}
    }
    public HomeLayout tryRemove(String id){
        if(placement(id)==null)return null;List<Placement> next=new ArrayList<>();for(Placement p:objects)if(!p.id.equals(id))next.add(p);return new HomeLayout(next,companionX,companionY);
    }
    /** Four-neighbour shortest path, excluding origin and including destination. Empty means same cell or unreachable. */
    public List<Cell> pathTo(int x,int y){
        if(isBlocked(x,y)||x==companionX&&y==companionY)return Collections.emptyList();
        Cell[][] previous=new Cell[SIZE][SIZE];boolean[][] seen=new boolean[SIZE][SIZE];ArrayDeque<Cell> queue=new ArrayDeque<>();
        Cell start=new Cell(companionX,companionY);queue.add(start);seen[start.x][start.y]=true;int[] dx={1,0,-1,0},dy={0,1,0,-1};
        while(!queue.isEmpty()){
            Cell current=queue.removeFirst();if(current.x==x&&current.y==y){
                List<Cell> path=new ArrayList<>();Cell step=current;while(!step.equals(start)){path.add(step);step=previous[step.x][step.y];}Collections.reverse(path);return Collections.unmodifiableList(path);
            }
            for(int d=0;d<4;d++){int nx=current.x+dx[d],ny=current.y+dy[d];if(inBounds(nx,ny)&&!seen[nx][ny]&&!isBlocked(nx,ny)){seen[nx][ny]=true;previous[nx][ny]=current;queue.addLast(new Cell(nx,ny));}}
        }return Collections.emptyList();
    }
    public HomeLayout tryMove(int x,int y){if(!inBounds(x,y)||isBlocked(x,y))return null;if(x==companionX&&y==companionY)return this;if(pathTo(x,y).isEmpty())return null;return new HomeLayout(objects,x,y);}
    /** Stable compact record; all room edits save this single complete state atomically. */
    public String encode(){StringBuilder out=new StringBuilder("2|").append(companionX).append(',').append(companionY).append('|');for(Placement p:objects)out.append(p.id).append(',').append(p.x).append(',').append(p.y).append(',').append(p.rotation).append(';');return out.toString();}
    /** Invalid records never partially restore a room. The caller can use the safe starter state. */
    public static HomeLayout decode(String record){
        if(record==null||record.length()>8192)return null;try{
            String[] sections=record.split("\\|",-1);if(sections.length!=3||!("1".equals(sections[0])||"2".equals(sections[0])))return null;boolean legacy="1".equals(sections[0]);String[] companion=sections[1].split(",",-1);if(companion.length!=2)return null;
            List<Placement> placements=new ArrayList<>();if(!sections[2].isEmpty())for(String object:sections[2].split(";")){String[] p=object.split(",",-1);if(p.length!=4)return null;placements.add(new Placement(p[0],Integer.parseInt(p[1]),Integer.parseInt(p[2]),Integer.parseInt(p[3])));}
            int cx=Integer.parseInt(companion[0]),cy=Integer.parseInt(companion[1]);
            if(legacy){if(cx<0||cy<0||cx>=6||cy>=6)return null;for(Placement p:placements)if(p.x<0||p.y<0||p.x+p.width()>6||p.y+p.depth()>6)return null;}
            return new HomeLayout(placements,cx,cy);
        }catch(IllegalArgumentException invalid){return null;}
    }
}
