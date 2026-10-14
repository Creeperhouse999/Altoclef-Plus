package altoclefplus.core;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which mixin hooks have actually fired. Fragile hooks into AltoClef internals use require = 0, so
 * a hook that never fires is shown in the UI as "not seen" instead of crashing the game.
 */
public final class Hooks {
    private static final Set<String> EXPECTED = ConcurrentHashMap.newKeySet();
    private static final Set<String> SEEN = ConcurrentHashMap.newKeySet();

    private Hooks() {}

    public static void expect(String name) {
        EXPECTED.add(name);
    }

    public static void hit(String name) {
        SEEN.add(name);
    }

    public static boolean seen(String name) {
        return SEEN.contains(name);
    }

    /** name -> seen, sorted by name. */
    public static Map<String, Boolean> report() {
        Map<String, Boolean> out = new TreeMap<>();
        for (String e : EXPECTED) out.put(e, SEEN.contains(e));
        return out;
    }
}
