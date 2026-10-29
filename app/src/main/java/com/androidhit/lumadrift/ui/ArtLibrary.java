package com.androidhit.lumadrift.ui;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.util.LruCache;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.BitSet;

/**
 * Packaged original art. A cell is decoded at authored resolution, without allocating its
 * complete six-cell atlas. The 48 MiB cache holds visible sprites instead of unrelated cells.
 * Registry keys stay separate from saved inventory identities. Drawing uses one UI thread.
 */
public final class ArtLibrary {
    public static final String ROOT="art/v117/";
    public static final int DEFAULT_CACHE_BYTES=48*1024*1024;
    private static final long MAX_SPRITE_BYTES=4L*1024*1024;
    private static final int ALPHA_THRESHOLD=8;
    private static final String TAG="LumaArt117";

    private final AssetManager assets;
    private final LruCache<String,Sprite> cache;
    private final Map<String,Definition> definitions=new LinkedHashMap<>();
    private final Map<String,Sheet> sheets=new LinkedHashMap<>();
    private final Map<String,String> failures=new LinkedHashMap<>();
    private final Map<String,DecodeInfo> decodeInfo=new LinkedHashMap<>();
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
    private final RectF destination=new RectF();
    private final float[] fitted=new float[4];
    private int decodeCount,evictionCount;

    public ArtLibrary(AssetManager assets){this(assets,DEFAULT_CACHE_BYTES);}

    public ArtLibrary(AssetManager assets,int cacheBytes){
        if(assets==null)throw new IllegalArgumentException("AssetManager required");
        if(cacheBytes<1024*1024)throw new IllegalArgumentException("Art cache must be at least 1 MiB");
        this.assets=assets;
        cache=new LruCache<String,Sprite>(cacheBytes){
            @Override protected int sizeOf(String key,Sprite value){return value.bitmap.getAllocationByteCount();}
            @Override protected void entryRemoved(boolean evicted,String key,Sprite oldValue,Sprite newValue){
                if(evicted)evictionCount++;
                // Canvas/display lists may retain the pixels after cache removal. Never recycle them.
            }
        };
    }

    /** Equal row-major cells; null/empty keys deliberately leave a cell unused. */
    public synchronized void registerSheet(String path,int columns,int rows,String... keys){
        if(path==null||!path.startsWith(ROOT)||path.contains("..")||path.contains("\\")
                ||columns<=0||rows<=0||keys==null||(long)columns*rows!=keys.length)
            throw new IllegalArgumentException("Invalid packaged art sheet");
        if(sheets.containsKey(path))throw new IllegalArgumentException("Sheet already registered: "+path);
        Map<String,Boolean> incoming=new LinkedHashMap<>();
        for(String key:keys){
            if(key==null||key.length()==0)continue;
            if(definitions.containsKey(key)||incoming.put(key,true)!=null)
                throw new IllegalArgumentException("Sprite already registered: "+key);
        }
        Sheet sheet=new Sheet(path,columns,rows,keys.clone());sheets.put(path,sheet);
        for(int i=0;i<keys.length;i++)if(keys[i]!=null&&keys[i].length()>0)
            definitions.put(keys[i],new Definition(sheet,i,keys[i],null,null));
    }

    /** Explicit original source rectangles preserve authored art that crosses an equal-grid seam. */
    public synchronized void registerRegions(String path,String[] keys,int[][] regions){
        registerRegions(path,keys,regions,null);
    }

    /** Optional source-coordinate exclusions isolate interleaved bounding boxes without editing PNGs. */
    public synchronized void registerRegions(String path,String[] keys,int[][] regions,int[][][] exclusions){
        if(path==null||!path.startsWith(ROOT)||path.contains("..")||path.contains("\\")||keys==null||regions==null||keys.length!=regions.length||keys.length==0)
            throw new IllegalArgumentException("Invalid packaged art regions");
        if(sheets.containsKey(path))throw new IllegalArgumentException("Sheet already registered: "+path);
        Map<String,Boolean> incoming=new LinkedHashMap<>();
        for(int i=0;i<keys.length;i++){
            String key=keys[i];int[] r=regions[i];
            if(key==null||key.length()==0||definitions.containsKey(key)||incoming.put(key,true)!=null)
                throw new IllegalArgumentException("Sprite already registered or empty: "+key);
            if(r==null||r.length!=4||r[0]<0||r[1]<0||r[2]<=r[0]||r[3]<=r[1])
                throw new IllegalArgumentException("Invalid source region: "+key);
        }
        if(exclusions!=null&&exclusions.length!=keys.length)throw new IllegalArgumentException("Exclusion count must match regions");
        int[][][] copy=new int[keys.length][][];
        for(int i=0;i<keys.length;i++){
            int[][] cuts=exclusions==null?null:exclusions[i];copy[i]=new int[cuts==null?0:cuts.length][];
            for(int j=0;j<copy[i].length;j++){
                int[] cut=cuts[j];if(cut==null||cut.length!=4||cut[0]<0||cut[1]<0||cut[2]<=cut[0]||cut[3]<=cut[1])throw new IllegalArgumentException("Invalid source exclusion");
                copy[i][j]=cut.clone();
            }
        }
        Sheet sheet=new Sheet(path,0,0,keys.clone());sheets.put(path,sheet);
        for(int i=0;i<keys.length;i++)definitions.put(keys[i],new Definition(sheet,i,keys[i],new Rect(regions[i][0],regions[i][1],regions[i][2],regions[i][3]),copy[i]));
    }

    public synchronized boolean contains(String key){return definitions.containsKey(key);}

    /** Validates all registered art; a bounded cache cannot retain the entire game at once. */
    public synchronized boolean preloadAll(){
        boolean success=true;
        for(Definition definition:definitions.values())if(sprite(definition)==null)success=false;
        return success;
    }

    public synchronized boolean preload(String key){
        Definition definition=definitions.get(key);return definition!=null&&sprite(definition)!=null;
    }

    public synchronized Map<String,String> failures(){return Collections.unmodifiableMap(new LinkedHashMap<>(failures));}
    public synchronized List<String> keys(){return Collections.unmodifiableList(new ArrayList<>(definitions.keySet()));}
    public synchronized int residentBytes(){return cache.size();}
    public synchronized int cacheLimitBytes(){return cache.maxSize();}
    public synchronized int residentSpriteCount(){return cache.snapshot().size();}
    public synchronized int decodeCount(){return decodeCount;}
    public synchronized int evictionCount(){return evictionCount;}

    /** Read-only actual cell dimensions and repeated-decode counts, including evicted entries. */
    public synchronized Map<String,DecodeInfo> decodeDiagnostics(){
        return Collections.unmodifiableMap(new LinkedHashMap<>(decodeInfo));
    }

    public static final class DecodeInfo {
        public final String key,path,policy;
        public final int sourceWidth,sourceHeight,sourceLeft,sourceTop,sourceRight,sourceBottom;
        public final int decodedWidth,decodedHeight,sampleSize,allocationBytes,count,excludedRectangles;
        private DecodeInfo(Definition definition,int sourceWidth,int sourceHeight,Rect region,Bitmap bitmap,int sampleSize,int count){
            key=definition.key;path=definition.sheet.path;policy="full_resolution_cell_region";
            this.sourceWidth=sourceWidth;this.sourceHeight=sourceHeight;
            sourceLeft=region.left;sourceTop=region.top;sourceRight=region.right;sourceBottom=region.bottom;
            decodedWidth=bitmap.getWidth();decodedHeight=bitmap.getHeight();this.sampleSize=sampleSize;
            allocationBytes=bitmap.getAllocationByteCount();this.count=count;
            excludedRectangles=definition.exclusions==null?0:definition.exclusions.length;
        }
    }

    /** Clear residency without invalidating an in-flight Canvas or display list. */
    public synchronized void trimMemory(){cache.evictAll();}

    public boolean draw(Canvas canvas,String key,RectF target){
        return target!=null&&draw(canvas,key,target.left,target.top,target.right,target.bottom,.5f,.5f,255);
    }

    /** Aspect-preserving content fit. Bottom alignment keeps feet on their authored ground. */
    public synchronized boolean draw(Canvas canvas,String key,float left,float top,float right,float bottom,
                                     float alignX,float alignY,int alpha){
        if(canvas==null||!finite(left)||!finite(top)||!finite(right)||!finite(bottom)
                ||right<=left||bottom<=top||!finite(alignX)||!finite(alignY))return false;
        // Reject in the caller's current matrix/clip before opening an offscreen catalogue sprite.
        if(canvas.quickReject(left,top,right,bottom,Canvas.EdgeType.AA))return false;
        Definition definition=definitions.get(key);if(definition==null)return false;
        Sprite sprite=sprite(definition);if(sprite==null)return false;
        Rect source=sprite.visible;
        if(!SpriteGeometry.fit(source.width(),source.height(),left,top,right,bottom,alignX,alignY,fitted))return false;
        destination.set(fitted[0],fitted[1],fitted[2],fitted[3]);
        paint.setAlpha(Math.max(0,Math.min(255,alpha)));paint.setColorFilter(null);
        render(canvas,sprite,source,destination);paint.setAlpha(255);return true;
    }

    public synchronized boolean fittedBounds(String key,RectF target,float alignX,float alignY,RectF out){
        if(target==null||out==null||!finite(target.left)||!finite(target.top)||!finite(target.right)||!finite(target.bottom)
                ||target.width()<=0||target.height()<=0)return false;
        Definition definition=definitions.get(key);if(definition==null)return false;
        Sprite sprite=sprite(definition);if(sprite==null)return false;
        Rect source=sprite.visible;
        if(!SpriteGeometry.fit(source.width(),source.height(),target.left,target.top,target.right,target.bottom,alignX,alignY,fitted))return false;
        out.set(fitted[0],fitted[1],fitted[2],fitted[3]);return true;
    }

    /** A normalized part of the visible sprite, trimmed locally once; pairs use [0,.5] and [.5,1]. */
    public synchronized boolean drawPart(Canvas canvas,String key,float sourceLeft,float sourceTop,float sourceRight,float sourceBottom,
                                        float left,float top,float right,float bottom,float alignX,float alignY,int alpha){
        if(canvas==null||!finite(sourceLeft)||!finite(sourceTop)||!finite(sourceRight)||!finite(sourceBottom)
                ||sourceLeft<0||sourceTop<0||sourceRight>1||sourceBottom>1||sourceRight<=sourceLeft||sourceBottom<=sourceTop
                ||!finite(left)||!finite(top)||!finite(right)||!finite(bottom)||right<=left||bottom<=top||!finite(alignX)||!finite(alignY))return false;
        if(canvas.quickReject(left,top,right,bottom,Canvas.EdgeType.AA))return false;
        Definition definition=definitions.get(key);if(definition==null)return false;
        Sprite sprite=sprite(definition);if(sprite==null)return false;
        Rect whole=sprite.visible;
        int l=whole.left+(int)Math.floor(whole.width()*sourceLeft),t=whole.top+(int)Math.floor(whole.height()*sourceTop);
        int r=whole.left+(int)Math.floor(whole.width()*sourceRight),b=whole.top+(int)Math.floor(whole.height()*sourceBottom);
        if(r<=l||b<=t)return false;
        String partKey=l+":"+t+":"+r+":"+b;
        Rect part=sprite.parts.get(partKey);
        if(part==null){
            part=alphaBounds(sprite,new Rect(l,t,r,b));if(part==null)return false;
            if(sprite.parts.size()<12)sprite.parts.put(partKey,part);
        }
        if(!SpriteGeometry.fit(part.width(),part.height(),left,top,right,bottom,alignX,alignY,fitted))return false;
        destination.set(fitted[0],fitted[1],fitted[2],fitted[3]);paint.setAlpha(Math.max(0,Math.min(255,alpha)));paint.setColorFilter(null);
        render(canvas,sprite,part,destination);paint.setAlpha(255);return true;
    }

    private void render(Canvas canvas,Sprite sprite,Rect source,RectF target){
        if(sprite.excluded.isEmpty()){canvas.drawBitmap(sprite.bitmap,source,target,paint);return;}
        int save=canvas.save();canvas.translate(target.left,target.top);canvas.scale(target.width()/source.width(),target.height()/source.height());
        canvas.translate(-source.left,-source.top);canvas.clipRect(source.left,source.top,source.right,source.bottom);
        canvas.clipOutPath(sprite.exclusionPath);canvas.drawBitmap(sprite.bitmap,0,0,paint);canvas.restoreToCount(save);
    }

    private static Rect alphaBounds(Sprite sprite,Rect area){
        Bitmap bitmap=sprite.bitmap;
        int width=area.width(),height=area.height(),l=area.right,t=area.bottom,r=-1,b=-1;
        int batch=Math.min(16,height);int[] pixels=new int[width*batch];
        for(int y=area.top;y<area.bottom;y+=batch){
            int rows=Math.min(batch,area.bottom-y);bitmap.getPixels(pixels,0,width,area.left,y,width,rows);
            for(int row=0;row<rows;row++)for(int x=0;x<width;x++)if((pixels[row*width+x]>>>24)>ALPHA_THRESHOLD&&!sprite.excluded.get((y+row)*bitmap.getWidth()+area.left+x)){
                l=Math.min(l,area.left+x);r=Math.max(r,area.left+x);t=Math.min(t,y+row);b=y+row;
            }
        }
        return r<l||b<t?null:new Rect(Math.max(area.left,l-2),Math.max(area.top,t-2),Math.min(area.right,r+3),Math.min(area.bottom,b+3));
    }

    private Sprite sprite(Definition definition){
        Sprite cached=cache.get(definition.key);if(cached!=null)return cached;
        if(failures.containsKey(definition.key))return null;
        BitmapRegionDecoder decoder=null;
        try(InputStream input=assets.open(definition.sheet.path)){
            decoder=BitmapRegionDecoder.newInstance(input,false);
            if(decoder==null)throw new IOException("Region decoder unavailable");
            int sourceWidth=decoder.getWidth(),sourceHeight=decoder.getHeight();
            if(definition.explicit==null&&(sourceWidth<definition.sheet.columns*8||sourceHeight<definition.sheet.rows*8))
                throw new IOException("Missing or undersized sheet");
            int[] cell=new int[4];
            if(definition.explicit!=null){cell[0]=definition.explicit.left;cell[1]=definition.explicit.top;cell[2]=definition.explicit.right;cell[3]=definition.explicit.bottom;
                if(cell[2]>sourceWidth||cell[3]>sourceHeight)throw new IOException("Source region outside original PNG");
            }else if(!SpriteGeometry.cell(sourceWidth,sourceHeight,definition.sheet.columns,definition.sheet.rows,definition.index,cell))throw new IOException("Invalid source cell geometry");
            Rect region=new Rect(cell[0],cell[1],cell[2],cell[3]);
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inPreferredConfig=Bitmap.Config.ARGB_8888;options.inScaled=false;options.inSampleSize=1;
            long maximum=Math.min(MAX_SPRITE_BYTES,cache.maxSize());
            while(((long)region.width()/options.inSampleSize)*((long)region.height()/options.inSampleSize)*4>maximum)
                options.inSampleSize*=2;
            Bitmap bitmap=decoder.decodeRegion(region,options);
            if(bitmap==null)throw new IOException("Region decoder returned no pixels");
            if(!bitmap.hasAlpha())throw new IOException("Sprite cell has no alpha channel");
            int width=bitmap.getWidth(),height=bitmap.getHeight(),l=width,t=height,r=-1,b=-1;
            BitSet excluded=new BitSet();Path exclusionPath=new Path();
            if(definition.exclusions!=null)for(int[] cut:definition.exclusions){
                int cl=Math.max(0,(cut[0]-region.left)/options.inSampleSize),ct=Math.max(0,(cut[1]-region.top)/options.inSampleSize);
                int cr=Math.min(width,(cut[2]-region.left+options.inSampleSize-1)/options.inSampleSize),cb=Math.min(height,(cut[3]-region.top+options.inSampleSize-1)/options.inSampleSize);
                if(cr<=cl||cb<=ct)continue;
                exclusionPath.addRect(cl,ct,cr,cb,Path.Direction.CW);
                for(int yy=ct;yy<cb;yy++)excluded.set(yy*width+cl,yy*width+cr);
            }
            int batch=Math.min(16,height);int[] pixels=new int[width*batch];boolean transparent=false;
            for(int y=0;y<height;y+=batch){
                int rows=Math.min(batch,height-y);
                bitmap.getPixels(pixels,0,width,0,y,width,rows);
                for(int row=0;row<rows;row++)for(int x=0;x<width;x++){
                    if((pixels[row*width+x]>>>24)<=ALPHA_THRESHOLD||excluded.get((y+row)*width+x)){transparent=true;continue;}
                    l=Math.min(l,x);r=Math.max(r,x);t=Math.min(t,y+row);b=y+row;
                }
            }
            if(r<l||b<t)throw new IOException("Empty sprite cell "+definition.index);
            if(!transparent)throw new IOException("Sprite cell is fully opaque");
            Rect visible=new Rect(Math.max(0,l-2),Math.max(0,t-2),Math.min(width,r+3),Math.min(height,b+3));
            Sprite decoded=new Sprite(bitmap,visible,excluded,exclusionPath);
            DecodeInfo previous=decodeInfo.get(definition.key);
            decodeInfo.put(definition.key,new DecodeInfo(definition,sourceWidth,sourceHeight,region,bitmap,options.inSampleSize,previous==null?1:previous.count+1));
            decodeCount++;cache.put(definition.key,decoded);
            Log.i(TAG,"Sprite ready: key="+definition.key+", pixels="+width+"x"+height
                    +", decode="+decodeCount+", evictions="+evictionCount+", residentBytes="+cache.size());
            return decoded;
        }catch(IOException|RuntimeException failure){
            String message=failure.getClass().getSimpleName()+": "+failure.getMessage();
            failures.put(definition.key,message);Log.w(TAG,"Cannot load "+definition.key+" from "+definition.sheet.path+": "+message);return null;
        }finally{if(decoder!=null)decoder.recycle();}
    }

    private static boolean finite(float value){return !Float.isNaN(value)&&!Float.isInfinite(value);}
    private static final class Definition {
        final Sheet sheet;final int index;final String key;final Rect explicit;final int[][] exclusions;
        Definition(Sheet sheet,int index,String key,Rect explicit,int[][] exclusions){this.sheet=sheet;this.index=index;this.key=key;this.explicit=explicit;this.exclusions=exclusions;}
    }
    private static final class Sheet {
        final String path;final int columns,rows;final String[] keys;
        Sheet(String path,int columns,int rows,String[] keys){this.path=path;this.columns=columns;this.rows=rows;this.keys=keys;}
    }
    private static final class Sprite {
        final Bitmap bitmap;final Rect visible;final Map<String,Rect> parts=new LinkedHashMap<>();final BitSet excluded;final Path exclusionPath;
        Sprite(Bitmap bitmap,Rect visible,BitSet excluded,Path exclusionPath){this.bitmap=bitmap;this.visible=visible;this.excluded=excluded;this.exclusionPath=exclusionPath;}
    }
}
