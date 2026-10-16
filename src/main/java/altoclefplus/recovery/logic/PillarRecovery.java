package altoclefplus.recovery.logic;

import java.util.OptionalInt;

/** Where to go after pillaring up uselessly: back down a tower in the open, never blind digging underground. */
public final class PillarRecovery {
    private PillarRecovery() {}

    public static OptionalInt descendTo(double y, String dimension, boolean skyAbove) {
        if (!skyAbove) return OptionalInt.empty();
        return OptionalInt.of((int) Math.max(dimension.equals("the_nether") ? 40 : -50, y - 20));
    }
}
