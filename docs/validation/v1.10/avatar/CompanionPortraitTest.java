import android.graphics.Canvas;
import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.ui.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

/** Host draw-command contracts; never a claim of Android pixel or performance validation. */
public final class CompanionPortraitTest {
    private static int assertions;
    private static void check(boolean value,String message) { assertions++;if(!value)throw new AssertionError(message); }
    private static String hash(String value)throws Exception { byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));StringBuilder result=new StringBuilder();for(byte b:bytes)result.append(String.format(Locale.ROOT,"%02x",b&255));return result.toString(); }
    private static List<CosmeticCatalog.Item> cosmetics(int variant) {
        return Arrays.asList(CosmeticCatalog.ITEMS.get(variant),CosmeticCatalog.ITEMS.get(6+variant),CosmeticCatalog.ITEMS.get(12+variant),CosmeticCatalog.ITEMS.get(18+variant));
    }
    private static AvatarLoadout gear(int variant) { return new AvatarLoadout(AvatarGearCatalog.ITEMS.get(variant).id,AvatarGearCatalog.ITEMS.get(6+variant).id,AvatarGearCatalog.ITEMS.get(12+variant).id); }
    public static void main(String[] args)throws Exception {
        CompanionPortrait portrait=new CompanionPortrait();Canvas canvas=new Canvas();Set<String> signatures=new HashSet<>();
        int maxCalls=0;StringBuilder samples=new StringBuilder("companion,style,pose,sha256,draw_calls\n");
        List<CosmeticCatalog.Item> starter=cosmetics(0);AvatarLoadout starterGear=gear(0);
        for(Catalog.CharacterItem who:Catalog.CHARACTERS)for(AvatarStyles.Style style:AvatarStyles.all())for(AvatarStage.Pose pose:AvatarStage.Pose.values()) {
            canvas.reset();portrait.draw(canvas,who,100,100,40,style,pose,1000,true,starterGear,starter);
            String first=canvas.signature();int calls=canvas.drawCalls;
            check(canvas.depth==0,"balanced composed canvas state");check(calls>40,"all layers draw");
            check(signatures.add(hash(first)),"distinct combination of companion/style/pose");
            canvas.reset();portrait.draw(canvas,who,100,100,40,style,pose,96491,true,starterGear,starter);
            check(first.equals(canvas.signature()),"reduced motion freezes body, clothes, gear and aura together");
            samples.append(who.id).append(',').append(style.id).append(',').append(pose).append(',').append(hash(first)).append(',').append(calls).append('\n');maxCalls=Math.max(maxCalls,calls);
        }
        Set<String> glyphs=new HashSet<>(),glyphGeometry=new HashSet<>();
        for(AvatarGearCatalog.Item item:AvatarGearCatalog.ITEMS) {
            canvas.reset();portrait.drawGearGlyph(canvas,item,0,0,25,10,true);String first=canvas.signature();
            check(glyphs.add(hash(first)),"18 distinct legacy gear glyphs");check(glyphGeometry.add(hash(canvas.geometrySignature())),"gear differs by geometry, not only colour");
            check(canvas.depth==0&&canvas.drawCalls>1,"glyph balances canvas and has visible geometry");
            canvas.reset();portrait.drawGearGlyph(canvas,item,0,0,25,80000,true);check(first.equals(canvas.signature()),"static standalone gear glyph");
        }
        Set<String> clothesOnly=new HashSet<>();
        for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS) {
            canvas.reset();portrait.draw(canvas,Catalog.CHARACTERS[0],0,0,20,AvatarStyles.find(null),AvatarStage.Pose.IDLE,0,true,null,Collections.singletonList(item));
            check(clothesOnly.add(hash(canvas.signature())),"all24 cosmetic overrides actually change their visual");
        }
        for(int variant=0;variant<6;variant++) {
            canvas.reset();portrait.draw(canvas,Catalog.CHARACTERS[3],0,0,25,AvatarStyles.find("sakura"),AvatarStage.Pose.HAPPY,0,true,gear(variant),cosmetics(variant));
            check(canvas.depth==0,"all7 slot combinations balance canvas");
        }
        List<CosmeticCatalog.Item> override=new ArrayList<>(starter);override.add(CosmeticCatalog.ITEMS.get(1));override.add(null);
        List<CosmeticCatalog.Item> before=new ArrayList<>(override);
        canvas.reset();portrait.draw(canvas,null,0,0,25,null,null,0,true,starterGear,override);String appended=canvas.signature();
        List<CosmeticCatalog.Item> replacement=new ArrayList<>(starter);replacement.set(0,CosmeticCatalog.ITEMS.get(1));
        canvas.reset();portrait.draw(canvas,null,0,0,25,null,null,0,true,starterGear,replacement);
        check(appended.equals(canvas.signature()),"last override wins exactly once per cosmetic slot");
        check(before.equals(override),"renderer never edits caller preview list");check(starterGear.headId.equals("moonbud"),"loadout unchanged");
        canvas.reset();portrait.draw(canvas,null,0,0,20,null,null,0,true,null,null);check(canvas.drawCalls>10,"null overlays safely omit optional layers");
        canvas.reset();portrait.draw(canvas,null,0,0,0,null,null,0,false,null,null);check(canvas.drawCalls==0,"nonpositive radius ignored");
        portrait.draw(canvas,null,Float.NaN,0,20,null,null,0,false,null,null);check(canvas.drawCalls==0,"nonfinite drawing ignored");
        Set<String> expected=new HashSet<>(Arrays.asList("avatar","appearance","equipment","visual_styles","today","this_week","journey_rewards","ready_count","objectives","track_item","untrack_item","tracked_goal","view_source","preview","preview_only","wear_preview","reset_preview","look","benefits","cosmetic_slots","gear_slots","owned_count","collection_goal","goal_owned","goal_progress","next_action","claim_daily","play_journey","play_daily","play_weekly","all_claimed","ready","not_ready","progress","prism_story","chapter","story_progress","back_journey","claim_reward","open_inventory","empty_goal","choose_goal","show_avatar","unlock_source","source_not_purchase"));
        expected.addAll(Arrays.asList("rewards_all","journey_short","story_book","visit_note","period_changed","gold_account","gold_closed","view_wallet","source_account","source_capsule","acquired_with"));
        check(expected.equals(V110Strings.keys()),"all requested keys supplied, no accidental duplicate");
        String[] languages={"en","fr","de","es","pt","it"};
        for(String key:expected)for(String language:languages) {
            Object[] values=key.equals("acquired_with")?new Object[]{"Capsule"}:key.equals("ready_count")||key.equals("chapter")?new Object[]{3}:key.equals("owned_count")||key.equals("goal_progress")||key.equals("story_progress")?new Object[]{3,10}:new Object[]{};
            String text=V110Strings.get(language,key,values);
            check(text!=null&&!text.isEmpty()&&!text.contains("%d")&&!text.contains("%s"),"translated and formatted label "+key+" / "+language);
            check(text.equals(V110Strings.get(language+"-XX",key,values)),"regional fallback");
        }
        check(V110Strings.get("unsupported","avatar").equals(V110Strings.get("en","avatar")),"unsupported locale English fallback");
        boolean immutable=false;try{V110Strings.keys().clear();}catch(UnsupportedOperationException expectedFailure){immutable=true;}check(immutable,"string keys immutable");
        boolean unknown=false;try{V110Strings.get("en","missing");}catch(IllegalArgumentException expectedFailure){unknown=true;}check(unknown,"missing copy fails visibly in development");
        Path out=Path.of(args[0]);Files.createDirectories(out);Files.writeString(out.resolve("command-fingerprints.csv"),samples.toString());
        String report="{\n  \"passed\": true,\n  \"assertions\": "+assertions+",\n  \"companion_style_pose_combinations\": "+signatures.size()+",\n  \"gear_glyphs\": "+glyphs.size()+",\n  \"cosmetic_overrides\": "+clothesOnly.size()+",\n  \"localised_keys\": "+expected.size()+",\n  \"languages\": 6,\n  \"max_recorded_draw_calls\": "+maxCalls+",\n  \"scope\": \"Host recording Canvas contracts and real data classes. Not Android pixels, FPS, save writes, purchases or human preference.\"\n}\n";
        Files.writeString(out.resolve("host-validation.json"),report);System.out.print(report);
    }
}
