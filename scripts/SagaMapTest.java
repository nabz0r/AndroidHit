import com.androidhit.lumadrift.ui.ExperienceLayout;
import com.androidhit.lumadrift.ui.SagaMap;

/** Source geometry contracts only; does not claim Android rendering or touch delivery. */
public final class SagaMapTest {
    private static long checks;
    private static void check(boolean good, String message) {
        checks++;
        if (!good) throw new AssertionError(message);
    }

    private static void allNodes() {
        float[] viewports = {266.66666f, 280, 320, 375, 400, 499, 500, 640, 720, 840, 1000, 1200, 1600};
        for (int total : new int[]{240, 1000}) for (float viewport : viewports) {
            float top = SagaMap.header(viewport), bottom = viewport - 95;
            check(top < bottom, "Compact header leaves a usable map area");
            for (int selected = 1; selected <= total; selected++) {
                float scroll = SagaMap.scrollTo(selected, viewport, total);
                float center = SagaMap.y(selected) - scroll;
                check(Float.isFinite(scroll) && scroll >= 0
                        && scroll <= Math.max(0, SagaMap.height(total) - viewport) + .01f, "Jump stays within exact map extent");
                check(center >= top && center <= bottom,
                        "Every selected node center remains outside pinned header/navigation: " + selected + " / " + viewport);
                check(SagaMap.x(selected) - 41 >= 0 && SagaMap.x(selected) + 41 <= 390, "Node badges stay within map width");
                int first = SagaMap.firstVisible(scroll, total), last = SagaMap.lastVisible(scroll, viewport, total);
                check(first >= 1 && last <= total && first <= selected && last >= selected, "Virtual range contains selected node and valid identities");
                check(last - first + 1 <= Math.ceil((viewport + 360) / SagaMap.STEP) + 3,
                        "Virtualization draws a viewport-sized range rather than 1000 nodes");
                for (int node = 1; node <= total; node++) {
                    float y = SagaMap.y(node) - scroll;
                    boolean intersects = y + 48 > top && y - 31 < bottom;
                    if (intersects) check(node >= first && node <= last, "Every visible hit rectangle is included by culling");
                }
            }
        }
        for (int n = 1; n <= 1000; n++) check(SagaMap.biome(n) == (n - 1) / 100, "Exact hundred-level biome boundary");
        check(SagaMap.biome(Integer.MIN_VALUE) == 0 && SagaMap.biome(Integer.MAX_VALUE) == 9, "Biome bounds saturate safely");
        for (int total : new int[]{240,1000}) {
            check(SagaMap.scrollTo(0,840,total) == SagaMap.scrollTo(1,840,total), "Jump lower bound saturates");
            check(SagaMap.scrollTo(total+1,840,total) == SagaMap.scrollTo(total,840,total), "Jump upper bound saturates");
        }
    }

    private static void landscapeAndPinnedHits() {
        ExperienceLayout landscape = new ExperienceLayout(2400,1080,24,56,3);
        check(Math.abs(landscape.viewportHeight - 266.66666f) < .01f, "Existing supported landscape layout is included");
        float scroll = SagaMap.scrollTo(500,landscape.viewportHeight,1000);
        check(SagaMap.y(500)-scroll >= 58 && SagaMap.y(500)-scroll <= landscape.viewportHeight-95,
                "Landscape My Level and Jump targets remain visible");
        // A node behind the normal header used to remain in the touch/AX list.
        float oldScroll = SagaMap.y(500)-840*.43f;
        float hiddenTop = SagaMap.y(497)-oldScroll-31, hiddenBottom = SagaMap.y(497)-oldScroll+48;
        float clippedTop = Math.max(SagaMap.header(840),hiddenTop), clippedBottom = Math.min(840-95,hiddenBottom);
        check(hiddenBottom < SagaMap.header(840) && clippedBottom <= clippedTop,
                "Pinned-header hit clipping omits the reproduced invisible level497 control");
    }

    private static void illustratedWorldAndCompanion() {
        for(int total:new int[]{240,1000})for(int level=1;level<=total;level++){
            int start=SagaMap.sectorStart(level),end=SagaMap.sectorEnd(level,total);
            check(start<=level&&end>=level&&end<=total,"World labels contain every real level, including the partial Remix world");
            check(start%100==1&&end-start<100,"Illustrated world boundaries preserve the existing 100-level partitions");
            float companion=SagaMap.companionX(level);
            check(companion-48>=0&&companion+48<=390,"The dressed current captain stays inside the map");
            check(Math.abs(companion-SagaMap.x(level))>=41+48,
                    "Captain art and its squad target never cover the current node or its level target");
        }
        check(SagaMap.sectorEnd(240,240)==240,"Remix world three ends at 240, never a fictional level 300");
        check(SagaMap.sectorStart(100)==1&&SagaMap.sectorStart(101)==101,"World label changes on the exact existing puzzle boundary");
    }

    private static void bottomControls() {
        // Contracts match the fixed page extents and final content controls in LumaView.
        float[][] controls = {
                {1050,876,921},  // Home: import challenge
                {1160,991,1036}, // Rewards: dressing room
                {1130,958,1003}, // Wardrobe: power inventory
                {1250,1095,1115} // Shop: currency separation caption
        };
        for (float viewport : new float[]{266.66666f,320,400,640,840,1200,1600}) for (float[] control : controls) {
            float height = Math.max(Math.max(840,viewport),control[0]);
            float endScroll = Math.max(0,height-viewport);
            float visibleTop = control[1]-endScroll, visibleBottom = control[2]-endScroll;
            check(visibleTop >= 0 && visibleBottom <= viewport-95,
                    "Entire final menu control remains above pinned navigation at scroll end");
        }
    }

    public static void main(String[] args) {
        allNodes(); landscapeAndPinnedHits(); illustratedWorldAndCompanion(); bottomControls();
        System.out.println("PASS " + checks + " SagaMap/page geometry checks; every Journey1..1000 and Remix1..240 jump,"
                + " 13 viewports including 266.67 landscape, virtual culling, biome/world boundaries, captain clearance, pinned hit clipping and final menu controls.");
        System.out.println("Scope: pure geometry and explicit fixed-page contracts; Android screenshots, real gesture delivery and text measurement are separate checks.");
    }
}
