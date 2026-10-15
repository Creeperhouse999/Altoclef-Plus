package altoclefplus.core.logic;

import java.util.List;
import java.util.Locale;

/** Reads AltoClef's task chain to tell when the bot is in the middle of something that must not be interrupted. */
public final class TaskChainState {
    private TaskChainState() {}

    /**
     * Placing a block, using a crafting table or furnace, crafting or clicking slots right now. A side job
     * started at such a moment made AltoClef start over (in one run it re-placed the same crafting table again
     * and again), so side jobs wait. Judged by the deepest steps: a crafting-table task that is out mining for
     * materials is fine to interrupt.
     */
    public static boolean inDelicateStep(List<String> chain) {
        for (int i = chain.size() - 1; i >= 0; i--) {
            String e = chain.get(i).toLowerCase(Locale.ROOT);
            if (e.contains("destroy block") || e.contains("mine and collect") || e.contains("mining or collecting")
                    || e.contains("approach entity") || e.contains("pickup dropped") || e.contains("wander")
                    || e.contains("get to") || e.contains("getting to") || e.contains("going to")
                    || e.contains("walking")) {
                return false;
            }
            if (e.contains("place structure") || e.contains("placing container") || e.contains("nearby>")
                    || e.contains("container") || e.contains("crafting (w/") || e.contains("clicking")
                    || e.contains("receiving output") || e.contains("interact using")) {
                return true;
            }
        }
        return false;
    }
}
