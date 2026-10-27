import android.graphics.Canvas;
import com.androidhit.lumadrift.data.AvatarStyles;
import com.androidhit.lumadrift.data.Catalog;
import com.androidhit.lumadrift.ui.AvatarStage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

/** Geometry and contract tests only. Actual Android rasterisation is a separate check. */
public final class AvatarStylesTest {
    private static int checks;
    private static void check(boolean condition,String description) { checks++; if (!condition) throw new AssertionError(description); }
    private static String hash(String text) throws Exception { byte[] bytes=MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)); StringBuilder out=new StringBuilder(); for(byte b:bytes) out.append(String.format("%02x", b&255)); return out.toString(); }
    public static void main(String[] args) throws Exception {
        check(AvatarStyles.all().size()==12,"12 styles");
        check(Catalog.CHARACTERS.length==24,"24 companions");
        check(AvatarStyles.variantCount()==288,"288 composite variants");
        boolean immutable=false;
        try { AvatarStyles.all().clear(); } catch (UnsupportedOperationException expected) { immutable=true; }
        check(immutable,"public collection immutable");
        check(AvatarStyles.find(null).id.equals(AvatarStyles.DEFAULT_ID),"null fallback");
        check(AvatarStyles.find("untrusted_style").id.equals(AvatarStyles.DEFAULT_ID),"unknown fallback");
        check(!AvatarStyles.isKnown("untrusted_style"),"unknown rejected by validation");
        String[] languages={"fr","en","es","de","pt","it"};
        Set<String> ids=new HashSet<>(), variants=new HashSet<>(), geometry=new HashSet<>();
        java.util.Map<String,Set<String>> characterGeometries=new java.util.HashMap<>();
        Set<AvatarStyles.Family> families=new HashSet<>();
        AvatarStage renderer=new AvatarStage();
        Canvas c=new Canvas();
        StringBuilder fingerprints=new StringBuilder("character,style,pose,commands_sha256,draw_calls\n");
        int maximumCalls=0;
        for(AvatarStyles.Style style:AvatarStyles.all()) {
            Set<String> silhouettes=new HashSet<>();
            check(ids.add(style.id),"unique style ID"); families.add(style.family);
            for(String language:languages) check(style.name(language)!=null && !style.name(language).isEmpty(),"localised style label");
            check(style.name("fr-FR").equals(style.name("fr")),"regional FR fallback");
            check(style.name(null).equals(style.name("en")),"null English fallback");
            check(style.name("unknown").equals(style.name("en")),"unknown English fallback");
            for(Catalog.CharacterItem who:Catalog.CHARACTERS) {
                check(variants.add(AvatarStyles.variantId(who,style.id)),"unique composite ID");
                Set<String> poses=new HashSet<>();
                for(AvatarStage.Pose pose:AvatarStage.Pose.values()) {
                    c.reset();renderer.draw(c,who,100,100,48,style.id,pose,1000,true);
                    String first=c.signature(),digest=hash(first); int calls=c.drawCalls;
                    check(c.depth==0,"balanced canvas state"); check(calls>10,"nonempty drawing");
                    c.reset();renderer.draw(c,who,100,100,48,style.id,pose,97381,true);
                    check(first.equals(c.signature()),"reduced motion independent of time");
                    check(poses.add(digest),"pose changes face/accessory geometry");
                    fingerprints.append(who.id).append(',').append(style.id).append(',').append(pose).append(',').append(digest).append(',').append(calls).append('\n');
                    maximumCalls=Math.max(maximumCalls,calls);
                    if(pose==AvatarStage.Pose.IDLE) {
                        check(geometry.add(digest),"all 288 idle command streams distinct");
                        check(silhouettes.add(hash(c.geometrySignature())),"24 distinct companion geometries per style, ignoring colour");
                        check(characterGeometries.computeIfAbsent(who.id,k->new HashSet<>()).add(hash(c.geometrySignature())),"style changes geometry, not only colour");
                    }
                }
            }
            for(float progress:new float[]{0,.1f,.5f,1,Float.NaN,-1,2}) {
                c.reset();renderer.drawBackdrop(c,390,844,style.id,progress,1000,true);String still=c.signature();
                check(c.depth==0,"balanced backdrop");c.reset();renderer.drawBackdrop(c,390,844,style.id,progress,91235,true);
                check(still.equals(c.signature()),"static backdrop independent of time");
            }
            c.reset();renderer.drawBackdrop(c,390,844,style.id,0,1000,false);String start=c.signature();
            c.reset();renderer.drawBackdrop(c,390,844,style.id,1,1000,false);
            check(!start.equals(c.signature()),"real completion drives parallax/landmark");
        }
        check(families.size()==3,"three render families"); check(geometry.size()==288,"all expected variants covered");
        for(Set<String> styles:characterGeometries.values()) check(styles.size()==12,"12 geometric treatments per companion");
        c.reset();renderer.draw(c,null,0,0,0,null,null,0,false);check(c.drawCalls==0,"zero radius ignored");
        renderer.draw(c,null,Float.NaN,0,20,null,null,0,false);check(c.drawCalls==0,"nonfinite coordinates ignored");
        renderer.drawBackdrop(c,0,844,null,0,0,false);check(c.drawCalls==0,"zero backdrop width ignored");
        c.reset();renderer.draw(c,null,0,0,20,null,null,0,true);check(c.drawCalls>10,"null safe defaults");
        Path destination=Path.of(args[0]);Files.createDirectories(destination);
        Files.writeString(destination.resolve("command-fingerprints.csv"),fingerprints.toString());
        String report="{\n  \"passed\": true,\n  \"checks\": "+checks+",\n  \"composite_variants\": 288,\n  \"poses_per_variant\": 5,\n  \"distinct_idle_command_streams\": "+geometry.size()+",\n  \"maximum_draw_calls_per_avatar\": "+maximumCalls+",\n  \"scope\": \"Host recording double: API, geometry and deterministic motion contracts; not Android pixels or frame-rate measurement.\"\n}\n";
        Files.writeString(destination.resolve("host-validation.json"),report);System.out.print(report);
    }
}
