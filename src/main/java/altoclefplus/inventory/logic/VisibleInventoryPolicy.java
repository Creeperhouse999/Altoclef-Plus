package altoclefplus.inventory.logic;

import java.util.List;
import java.util.Locale;

/** When the bot's own inventory screen should be shown and hidden in "visible inventory" mode. */
public final class VisibleInventoryPolicy {
    /** Ticks without a click before the bot's inventory screen closes again. */
    public static final int CLOSE_AFTER_IDLE_TICKS = 20;

    private VisibleInventoryPolicy() {}

    public static boolean shouldOpen(boolean enabled, boolean taskRunning, boolean playerInventorySlot,
                                     boolean otherScreenOpen, boolean inventoryJob) {
        return enabled && taskRunning && playerInventorySlot && !otherScreenOpen && inventoryJob;
    }

    public static boolean shouldClose(long ticksSinceClick, boolean cursorEmpty, boolean taskRunning) {
        if (!cursorEmpty) return false;
        return !taskRunning || ticksSinceClick > CLOSE_AFTER_IDLE_TICKS;
    }

    /**
     * The bot is crafting, sorting or putting on armor right now (not just a maintenance click while mining or
     * fighting). Decided by the deepest real step; slot-level steps ("Clicking ...") are skipped.
     */
    public static boolean isInventoryJob(List<String> chain) {
        for (int i = chain.size() - 1; i >= 0; i--) {
            String e = chain.get(i).toLowerCase(Locale.ROOT);
            if (e.contains("clicking") || e.contains("receiving output") || e.contains("throw cursor")
                    || e.contains("move item") || e.contains("moving item")) continue;
            return e.contains("crafting (w/") || e.contains("craft in inventory") || e.contains("equipping armor")
                    || e.contains("sort");
        }
        return false;
    }
}
