import android.graphics.Canvas;
import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.ui.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Pixel occlusion audit through Java2D; not an Android renderer or human-recognition score. */
public final class RasterAudit {
    private static final CompanionPortrait portrait=new CompanionPortrait();
    private static List<CosmeticCatalog.Item> clothes(){return Arrays.asList(CosmeticCatalog.ITEMS.get(0),CosmeticCatalog.ITEMS.get(6),CosmeticCatalog.ITEMS.get(12),CosmeticCatalog.ITEMS.get(18));}
    private static AvatarLoadout gear(int slot,int variant){String[] ids={AvatarGearCatalog.ITEMS.get(0).id,AvatarGearCatalog.ITEMS.get(6).id,AvatarGearCatalog.ITEMS.get(12).id};if(slot>=0)ids[slot]=AvatarGearCatalog.ITEMS.get(slot*6+variant).id;return new AvatarLoadout(ids[0],ids[1],ids[2]);}
    private static BufferedImage render(Catalog.CharacterItem who,AvatarStyles.Style style,int r,AvatarLoadout gear,List<CosmeticCatalog.Item> clothes){int size=(int)Math.ceil(r*4.5);Canvas c=new Canvas(size,size);portrait.draw(c,who,size/2f,size/2f,r,style,AvatarStage.Pose.IDLE,0,true,gear,clothes);if(!c.balanced())throw new AssertionError("Unbalanced draw");return c.image;}
    private static int difference(BufferedImage a,BufferedImage b,boolean outline){int count=0;for(int y=0;y<a.getHeight();y++)for(int x=0;x<a.getWidth();x++){int aa=a.getRGB(x,y),bb=b.getRGB(x,y);if(outline){if((aa>>>24>=128)!=(bb>>>24>=128))count++;}else{int delta=Math.max(Math.abs((aa>>>24)-(bb>>>24)),Math.max(Math.abs(((aa>>>16)&255)-((bb>>>16)&255)),Math.max(Math.abs(((aa>>>8)&255)-((bb>>>8)&255)),Math.abs((aa&255)-(bb&255)))));if(delta>=24)count++;}}return count;}
    private static void image(Path out,String name,BufferedImage image)throws Exception{ImageIO.write(image,"png",out.resolve(name).toFile());}
    public static void main(String[] args)throws Exception{
        Path out=Path.of(args[0]);Files.createDirectories(out);String label=args[1];
        StringBuilder pairs=new StringBuilder("radius,style,first,second,changed_pixels,changed_outline_pixels,bare_changed_pixels\n"),equipment=new StringBuilder("radius,style,character,slot,variant,changed_pixels\n");
        int minIdentity=Integer.MAX_VALUE,minGear=Integer.MAX_VALUE,zeroIdentity=0,zeroGear=0,zeroOutline=0,pairCases=0,gearCases=0;String worstIdentity="",worstGear="";
        for(int radius:new int[]{18,32})for(AvatarStyles.Style style:AvatarStyles.all()){
            BufferedImage[] dressed=new BufferedImage[24],bare=new BufferedImage[24];
            for(int c=0;c<24;c++){Catalog.CharacterItem who=Catalog.CHARACTERS[c];dressed[c]=render(who,style,radius,gear(-1,0),clothes());bare[c]=render(who,style,radius,null,null);}
            for(int a=0;a<24;a++)for(int b=a+1;b<24;b++){int delta=difference(dressed[a],dressed[b],false),outline=difference(dressed[a],dressed[b],true),uncovered=difference(bare[a],bare[b],false);pairs.append(radius).append(',').append(style.id).append(',').append(Catalog.CHARACTERS[a].id).append(',').append(Catalog.CHARACTERS[b].id).append(',').append(delta).append(',').append(outline).append(',').append(uncovered).append('\n');pairCases++;if(delta==0)zeroIdentity++;if(outline==0)zeroOutline++;if(delta<minIdentity){minIdentity=delta;worstIdentity=radius+":"+style.id+":"+Catalog.CHARACTERS[a].id+":"+Catalog.CHARACTERS[b].id;}}
            for(int c=0;c<24;c++)for(int slot=0;slot<3;slot++)for(int variant=1;variant<6;variant++){BufferedImage changed=render(Catalog.CHARACTERS[c],style,radius,gear(slot,variant),clothes());int delta=difference(dressed[c],changed,false);equipment.append(radius).append(',').append(style.id).append(',').append(Catalog.CHARACTERS[c].id).append(',').append(AvatarGearCatalog.Slot.values()[slot]).append(',').append(variant).append(',').append(delta).append('\n');gearCases++;if(delta==0)zeroGear++;if(delta<minGear){minGear=delta;worstGear=radius+":"+style.id+":"+Catalog.CHARACTERS[c].id+":"+slot+":"+variant;}}
        }
        BufferedImage atlas=new BufferedImage(8*160,9*175,BufferedImage.TYPE_INT_ARGB);Graphics2D g=atlas.createGraphics();g.setColor(new java.awt.Color(0xfffff4fc,true));g.fillRect(0,0,atlas.getWidth(),atlas.getHeight());g.setFont(new java.awt.Font("SansSerif",java.awt.Font.PLAIN,12));
        int group=0;for(String styleId:new String[]{"plush_classic","cyberpunk_neon","retro_arcade"}){AvatarStyles.Style style=AvatarStyles.find(styleId);for(int i=0;i<24;i++){int x=(i%8)*160,y=(group*3+i/8)*175;g.drawImage(render(Catalog.CHARACTERS[i],style,32,gear(-1,0),clothes()),x+8,y,null);g.setColor(new java.awt.Color(0xff40314f,true));g.drawString(Catalog.CHARACTERS[i].id+" / "+styleId,x+5,y+160);}group++;}g.dispose();image(out,label+"-fully-dressed-atlas.png",atlas);
        Files.writeString(out.resolve(label+"-identity-pixels.csv"),pairs.toString());Files.writeString(out.resolve(label+"-equipment-pixels.csv"),equipment.toString());
        String summary="{\n  \"scope\": \"Independent Java2D raster adaptation of original Canvas source. Not Android pixels, UI ownership, animation, performance or human recognition. Alpha and final occlusion are evaluated; no emulator or profile touched.\",\n  \"identity_pair_cases\": "+pairCases+",\n  \"gear_swap_cases\": "+gearCases+",\n  \"zero_identity_difference\": "+zeroIdentity+",\n  \"zero_gear_difference\": "+zeroGear+",\n  \"zero_outline_difference_pairs\": "+zeroOutline+",\n  \"minimum_changed_identity_pixels\": "+minIdentity+",\n  \"worst_identity\": \""+worstIdentity+"\",\n  \"minimum_changed_gear_pixels\": "+minGear+",\n  \"worst_gear\": \""+worstGear+"\"\n}\n";Files.writeString(out.resolve(label+"-summary.json"),summary);System.out.print(summary);
    }
}
