import com.androidhit.lumadrift.ui.ExperienceLayout;

/** Tests real layout geometry and physical text floors; no Android rendering or invented device claims. */
public final class ExperienceLayoutTest {
    private static int checks;
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    private static void near(float actual,float expected,float tolerance,String message){
        check(Float.isFinite(actual)&&Math.abs(actual-expected)<=tolerance,message+": "+actual+" vs "+expected);
    }
    private static void centeredAndBounded(ExperienceLayout layout,int width,String message){
        float left=layout.offsetX,right=layout.offsetX+390*layout.scale;
        check(left>=-.001f&&right<=width+.001f,message+" content stays inside width");
        near(left,width-right,.002f,message+" equal side margins");
    }
    private static void normalPhoneAndInsets(){
        ExperienceLayout phone=new ExperienceLayout(1080,2400,66,96,3);
        centeredAndBounded(phone,1080,"Portrait phone");
        near(phone.viewportHeight*phone.scale,2238,.002f,"System insets are excluded from drawable height");
        check(phone.contentHeight>=phone.viewportHeight,"Content never shorter than viewport");
        check(phone.contentHeight>=840,"Compact phone keeps design content height");
        near(phone.maximumScroll+phone.viewportHeight,phone.contentHeight,.001f,"Bottom scroll reaches last content pixel");

        ExperienceLayout noInsets=new ExperienceLayout(1080,2400,0,0,3);
        near(noInsets.scale,phone.scale,.0001f,"Insets do not shrink text or width");
        near((noInsets.viewportHeight-phone.viewportHeight)*phone.scale,162,.002f,"Top and bottom consume exactly their combined height");
        check(phone.maximumScroll>=noInsets.maximumScroll,"System bars cannot create more visible content");

        ExperienceLayout shortWindow=new ExperienceLayout(1080,1200,66,96,3);
        near(shortWindow.scale,phone.scale,.0001f,"Short windows scroll instead of scaling everything down");
        check(shortWindow.maximumScroll>phone.maximumScroll,"Shorter viewport has a larger scroll extent");
        near(shortWindow.maximumScroll+shortWindow.viewportHeight,shortWindow.contentHeight,.001f,"Short-window bottom remains reachable");

        ExperienceLayout tallWindow=new ExperienceLayout(1080,3600,66,96,3);
        check(tallWindow.maximumScroll==0,"Tall window does not manufacture empty scrolling");
        near(tallWindow.contentHeight,tallWindow.viewportHeight,.001f,"Tall content follows the viewport");
    }
    private static void compactLandscapeAndScroll(){
        ExperienceLayout landscape=new ExperienceLayout(2400,1080,24,56,3);
        centeredAndBounded(landscape,2400,"Landscape phone");
        check(landscape.offsetX>0,"Wide landscape is letterboxed instead of enlarging UI indefinitely");
        check(landscape.maximumScroll>0,"Landscape exposes scrollable content");
        check(landscape.scale/3<=1.25001f,"Physical enlargement cap applies on wide windows");
        near(landscape.clampScroll(-900),0,0,"Negative dragging cannot scroll before start");
        near(landscape.clampScroll(Float.NEGATIVE_INFINITY),0,0,"Negative fling saturates at start");
        near(landscape.clampScroll(Float.POSITIVE_INFINITY),landscape.maximumScroll,0,"Positive fling saturates at end");
        near(landscape.clampScroll(landscape.maximumScroll+900),landscape.maximumScroll,0,"Large drag cannot expose content beyond end");
        near(landscape.clampScroll(landscape.maximumScroll*.4f),landscape.maximumScroll*.4f,.001f,"Intermediate scroll remains continuous");

        // The saga positions its final row at contentHeight-151 and reserves 95 for navigation.
        float contentRowTop=landscape.contentHeight-151,contentRowBottom=contentRowTop+48;
        float visibleTop=contentRowTop-landscape.maximumScroll,visibleBottom=contentRowBottom-landscape.maximumScroll;
        check(visibleTop>=0&&visibleBottom<=landscape.viewportHeight-95,"Final menu row is reachable above pinned navigation at end of scroll");
        near(visibleBottom,landscape.viewportHeight-103,.001f,"Eight-unit gap remains above navigation content clip");

        // Reversing the draw transform must address the same logical point after scrolling and system insets.
        float logicalX=212,logicalY=contentRowTop+24,scroll=landscape.maximumScroll;
        float screenX=landscape.offsetX+logicalX*landscape.scale,screenY=24+(logicalY-scroll)*landscape.scale;
        near((screenX-landscape.offsetX)/landscape.scale,logicalX,.001f,"Horizontal input transform preserves target center");
        near((screenY-24)/landscape.scale+scroll,logicalY,.001f,"Scrolled input transform recovers content coordinate");

        ExperienceLayout allInset=new ExperienceLayout(1080,80,40,40,3);
        check(Float.isFinite(allInset.viewportHeight)&&allInset.viewportHeight>0,"Transient fully inset layout has finite nonzero viewport");
        check(Float.isFinite(allInset.maximumScroll)&&allInset.maximumScroll>=0,"Transient viewport does not produce invalid scroll bounds");
    }
    private static void physicalTextFloor(){
        for(float density:new float[]{.75f,1f,1.5f,2f,2.75f,3f,4f}){
            for(int widthDp:new int[]{240,280,320,360,390,411,600,840}){
                int width=Math.round(widthDp*density),height=Math.round(800*density);
                ExperienceLayout normal=new ExperienceLayout(width,height,0,0,density);
                centeredAndBounded(normal,width,"Width "+widthDp+"dp, density "+density);
                near(normal.minimumTextSize*normal.scale/density,12,.0001f,"Normal physical text floor is 12dp");
                ExperienceLayout explicitNormal=new ExperienceLayout(width,height,0,0,density,1);
                near(normal.minimumTextSize,explicitNormal.minimumTextSize,.0001f,"Five-argument constructor retains default font scale");
                for(float requestedFontScale:new float[]{.85f,1f,1.15f,1.3f,1.5f,2f}){
                    ExperienceLayout font=new ExperienceLayout(width,height,0,0,density,requestedFontScale);
                    near(font.scale,normal.scale,.0001f,"Font preference does not enlarge content width");
                    near(font.viewportHeight,normal.viewportHeight,.0001f,"Font preference does not change scroll viewport");
                    float physical=font.minimumTextSize*font.scale/density;
                    check(physical>=11.999f&&physical<=15.601f,"Physical floor stays within explicit 12–15.6dp policy");
                    if(requestedFontScale==1.15f)near(physical,13.8f,.0002f,"Intermediate font preference is honored");
                    if(requestedFontScale>=1.3f)near(physical,15.6f,.0002f,"Font growth is explicitly capped at 1.3");
                }
            }
        }
        ExperienceLayout narrow=new ExperienceLayout(960,1920,0,0,3);
        check(narrow.minimumTextSize>12,"320dp window raises logical minimum instead of retaining a physically small 12-unit label");
        near(narrow.minimumTextSize*narrow.scale,36,.0001f,"320dp normal floor renders at 36 pixels at density3");
        ExperienceLayout invalidPreference=new ExperienceLayout(1080,2400,0,0,3,Float.NaN);
        near(invalidPreference.minimumTextSize*invalidPreference.scale/3,12,.0001f,"Invalid font preference uses the default floor");
        // This verifies a documented limitation, not 48dp compliance for board cells or every control.
        near(300f/8,37.5f,0,"Eight-cell board has 37.5 logical-unit cells");
        check((300f/8)*narrow.scale/3<48,"Small board cells remain below the recommended touch target and need separate usability review");
        System.out.println("TEXT POLICY: physical 12dp floor, fontScale honored through 1.3; no 200% adaptive-text or universal 48dp-target claim");
    }
    public static void main(String[] args){
        normalPhoneAndInsets();compactLandscapeAndScroll();physicalTextFloor();
        System.out.println("PASS "+checks+" ExperienceLayout checks (pure geometry; installed clipping, legibility and gestures require Android QA)");
    }
}
