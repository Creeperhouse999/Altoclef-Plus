package altoclefplus.build.logic;

import java.util.HashSet;
import java.util.Set;

/**
 * Scaffolding cleanup: throwaway blocks (dirt, cobble, netherrack...) around the build area that were not
 * there before the build and are not part of the schematic were placed to reach high spots: remove them.
 */
public final class ScaffoldTracker {
    private ScaffoldTracker() {}

    /** All sets hold packed positions ({@link Schematic#key}) of throwaway blocks. */
    public static Set<Long> toRemove(Set<Long> before, Set<Long> schematicWants, Set<Long> now) {
        Set<Long> out = new HashSet<>(now);
        out.removeAll(before);
        out.removeAll(schematicWants);
        return out;
    }
}
