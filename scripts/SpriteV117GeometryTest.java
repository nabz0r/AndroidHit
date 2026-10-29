import com.androidhit.lumadrift.ui.SpriteGeometry;

/** Host qualification for the same measured sprite placement used by native drawing. */
public final class SpriteV117GeometryTest {
    private static int checks;

    public static void main(String[] args) {
        float[] out = new float[4];
        check(SpriteGeometry.fit(400, 800, 0, 0, 240, 315, .5f, 1, out), "portrait fit");
        near(out[0], 41.25f); near(out[1], 0); near(out[2], 198.75f); near(out[3], 315);
        check(SpriteGeometry.fit(800, 400, 0, 0, 240, 315, .5f, 1, out), "wide fit");
        near(out[0], 0); near(out[1], 195); near(out[2], 240); near(out[3], 315);

        int[] sizes = {1, 7, 31, 128, 512, 683, 1024, 1537};
        float[][] targets = {
            {-72, -90, 72, 99}, {0, 0, 28, 28}, {24, 115, 296, 490},
            {-41, -45, 41, 35}, {0, 0, 320, 568}, {0, 0, 390, 844}
        };
        for (int width : sizes) for (int height : sizes) for (float[] target : targets) {
            for (float alignX : new float[]{0, .5f, 1}) for (float alignY : new float[]{0, .5f, 1}) {
                check(SpriteGeometry.fit(width, height, target[0], target[1], target[2], target[3], alignX, alignY, out), "valid fit");
                check(out[0] >= target[0] - .001f && out[1] >= target[1] - .001f
                        && out[2] <= target[2] + .001f && out[3] <= target[3] + .001f, "sprite remains inside target");
                float actualWidth = out[2] - out[0], actualHeight = out[3] - out[1];
                check(actualWidth > 0 && actualHeight > 0, "visible sprite");
                float ratio = actualWidth / actualHeight;
                check(Math.abs(ratio / ((float) width / height) - 1) < .002f, "unwarped aspect");
                check(Math.abs(actualWidth - (target[2] - target[0])) < .001f
                        || Math.abs(actualHeight - (target[3] - target[1])) < .001f, "largest contained art");
                if (alignY == 1) near(out[3], target[3]);
                if (alignX == 0) near(out[0], target[0]);
            }
        }

        int[] cell = new int[4];
        for (int width : new int[]{1536, 1537, 2048}) for (int height : new int[]{1024, 1280, 1281}) {
            long area = 0;
            for (int i = 0; i < 6; i++) {
                check(SpriteGeometry.cell(width, height, 3, 2, i, cell), "valid sheet cell");
                area += (long) (cell[2] - cell[0]) * (cell[3] - cell[1]);
                check(cell[0] >= 0 && cell[1] >= 0 && cell[2] <= width && cell[3] <= height, "cell inside sheet");
                if (i % 3 == 0) check(cell[0] == 0, "row begins at source edge");
                if (i % 3 == 2) check(cell[2] == width, "row ends at source edge");
                if (i < 3) check(cell[1] == 0, "first row begins at source edge");
                if (i >= 3) check(cell[3] == height, "last row ends at source edge");
            }
            check(area == (long) width * height, "no discarded seam pixels");
            int[] previous = new int[4], next = new int[4];
            for (int i = 0; i < 5; i++) {
                SpriteGeometry.cell(width, height, 3, 2, i, previous);
                SpriteGeometry.cell(width, height, 3, 2, i + 1, next);
                if (i % 3 != 2) check(previous[2] == next[0], "adjacent columns meet without overlap");
                else check(previous[3] == next[1], "adjacent rows meet without overlap");
            }
        }

        float[] unchanged = {2, 3, 4, 5};
        check(!SpriteGeometry.fit(0, 2, 0, 0, 2, 2, .5f, .5f, unchanged), "zero source refused");
        check(!SpriteGeometry.fit(Float.NaN, 2, 0, 0, 2, 2, .5f, .5f, unchanged), "NaN source refused");
        check(!SpriteGeometry.fit(2, 2, 2, 0, 0, 2, .5f, .5f, unchanged), "inverted target refused");
        check(!SpriteGeometry.fit(2, 2, 0, 0, 2, 2, Float.POSITIVE_INFINITY, .5f, unchanged), "invalid anchor refused");
        near(unchanged[0], 2); near(unchanged[1], 3); near(unchanged[2], 4); near(unchanged[3], 5);
        check(!SpriteGeometry.cell(10, 10, 3, 2, -1, cell), "negative cell refused");
        check(!SpriteGeometry.cell(10, 10, 3, 2, 6, cell), "overflow cell refused");
        check(!SpriteGeometry.cell(2, 2, 3, 2, 0, cell), "subpixel cells refused");
        System.out.println("Sprite 1.17 geometry PASS: " + checks + " checks (aspect, anchors, odd sheet seams, invalid inputs)");
    }

    private static void near(float actual, float expected) {
        check(Math.abs(actual - expected) < .001f, "expected " + expected + ", got " + actual);
    }

    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
}
