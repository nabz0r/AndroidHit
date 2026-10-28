import android.graphics.Canvas;
import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.ui.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;

/** Independent final-pixel contracts: no clipping, deterministic reduced motion, visible swaps. */
public final class RasterContract {
    private static final CompanionPortrait renderer=new CompanionPortrait();
    private static int assertions;
    private static float largestExtent;
    private static void check(boolean value,String note){assertions++;if(!value)throw new AssertionError(note);}
    private static List<CosmeticCatalog.Item> clothes(int variant){return Arrays.asList(CosmeticCatalog.ITEMS.get(variant),CosmeticCatalog.ITEMS.get(6+variant),CosmeticCatalog.ITEMS.get(12+variant),CosmeticCatalog.ITEMS.get(18+variant));}
    private static AvatarLoadout gear(int variant){return new AvatarLoadout(AvatarGearCatalog.ITEMS.get(variant).id,AvatarGearCatalog.ITEMS.get(6+variant).id,AvatarGearCatalog.ITEMS.get(12+variant).id);}
    private static BufferedImage draw(Catalog.CharacterItem who,AvatarStyles.Style style,AvatarStage.Pose pose,int variant,long time,boolean still){Canvas c=new Canvas(180,180);renderer.draw(c,who,90,90,40,style,pose,time,still,gear(variant),clothes(variant));check(c.balanced(),"balanced Canvas after actual raster");return c.image;}
    private static int diff(BufferedImage a,BufferedImage b){int count=0;for(int y=0;y<a.getHeight();y++)for(int x=0;x<a.getWidth();x++)if(a.getRGB(x,y)!=b.getRGB(x,y))count++;return count;}
    private static void extent(BufferedImage image){float maximum=0;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if((image.getRGB(x,y)>>>24)>=32){maximum=Math.max(maximum,Math.max(Math.abs(x+.5f-90),Math.abs(y+.5f-90))/40);}largestExtent=Math.max(largestExtent,maximum);check(maximum<=CompanionPortrait.ART_EXTENT+.025f,"actual foreground remains inside public ART_EXTENT: "+maximum);}
    public static void main(String[] args)throws Exception{
        int combinations=0;
        for(Catalog.CharacterItem who:Catalog.CHARACTERS)for(AvatarStyles.Style style:AvatarStyles.all())for(int variant=0;variant<6;variant++)for(AvatarStage.Pose pose:AvatarStage.Pose.values()){
            BufferedImage first=draw(who,style,pose,variant,0,true),later=draw(who,style,pose,variant,98457,true);check(diff(first,later)==0,"reduced motion freezes final pixels across timestamps");extent(first);extent(draw(who,style,pose,variant,1100,false));combinations++;
        }
        int cosmeticCases=0,minCosmetic=Integer.MAX_VALUE;
        for(AvatarStyles.Style style:AvatarStyles.all())for(int slot=0;slot<4;slot++)for(int variant=1;variant<6;variant++){
            List<CosmeticCatalog.Item> base=clothes(0),changed=new ArrayList<>(base);changed.set(slot,CosmeticCatalog.ITEMS.get(slot*6+variant));Canvas a=new Canvas(90,90),b=new Canvas(90,90);renderer.draw(a,Catalog.CHARACTERS[0],45,45,18,style,AvatarStage.Pose.IDLE,0,true,gear(0),base);renderer.draw(b,Catalog.CHARACTERS[0],45,45,18,style,AvatarStage.Pose.IDLE,0,true,gear(0),changed);int pixels=diff(a.image,b.image);check(pixels>0,"every cosmetic variant produces surviving composited pixels");minCosmetic=Math.min(minCosmetic,pixels);cosmeticCases++;
        }
        String report="{\n  \"passed\": true,\n  \"assertions\": "+assertions+",\n  \"full_loadout_style_pose_combinations\": "+combinations+",\n  \"independent_cosmetic_swap_cases\": "+cosmeticCases+",\n  \"minimum_changed_cosmetic_pixels\": "+minCosmetic+",\n  \"largest_foreground_extent_r\": "+largestExtent+",\n  \"declared_art_extent_r\": "+CompanionPortrait.ART_EXTENT+",\n  \"scope\": \"Independent Java2D final-pixel adapter. Not native Android layout, ownership, performance or recognition. No emulator or stored profile touched.\"\n}\n";Files.writeString(Path.of(args[0]).resolve("raster-contract.json"),report);System.out.print(report);
    }
}
