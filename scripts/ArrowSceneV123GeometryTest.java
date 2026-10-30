import com.androidhit.lumadrift.engine.Direction;
import com.androidhit.lumadrift.ui.ArrowScene123;

/** Host contract checks for the badge reservations; native captures qualify the paint. */
public final class ArrowSceneV123GeometryTest {
    private static int checks;
    private static void check(boolean ok, String message) {
        checks++; if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        float[] sizes = {24, 28, 32, 36, 40, 44, 48, 56, 64, 72, 88};
        for (float size : sizes) for (float x : new float[]{0, 67.5f, 211})
                for (float y : new float[]{0, 105.25f}) {
            ArrowScene123.Geometry g = ArrowScene123.geometry(x, y, size);
            check(!g.direction.overlaps(g.key), "Key cannot cover the movement arrow at " + size);
            check(!g.direction.overlaps(g.relay), "Relay cannot cover the movement arrow at " + size);
            check(!g.key.overlaps(g.relay), "Key plus relay (U29) must have separate footprints");
            for (ArrowScene123.Box box : new ArrowScene123.Box[]{g.direction, g.key, g.relay}) {
                check(box.left >= x - size / 2 && box.right <= x + size / 2,
                        "Reserved symbol must not intrude into the adjacent cell horizontally");
                check(box.top >= y - size / 2 && box.bottom <= y + size / 2,
                        "Reserved symbol must not intrude into the adjacent cell vertically");
                check(box.left < box.right && box.top < box.bottom, "Positive footprint");
            }
        }
        int[] originalColors = {0xff91e5cc, 0xffffb398, 0xffceb3f7, 0xffa4cefa};
        for (Direction d : Direction.values()) check(ArrowScene123.directionColor(d) == originalColors[d.ordinal()],
                "Preserve the existing direction colour mapping");
        for (float invalid : new float[]{0, -1, Float.NaN, Float.POSITIVE_INFINITY}) {
            boolean refused = false;
            try { ArrowScene123.geometry(0, 0, invalid); }
            catch (IllegalArgumentException expected) { refused = true; }
            check(refused, "Refuse an undefined tile footprint");
        }
        System.out.println("PASS " + checks + " arrow-scene geometry/identity assertions; paint needs native visual inspection");
    }
}
