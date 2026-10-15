package altoclefplus.inventory.logic;

/** When the bot's own inventory screen should be shown and hidden in "visible inventory" mode. */
public final class VisibleInventoryPolicy {
    /** Ticks without a click before the bot's inventory screen closes again. */
    public static final int CLOSE_AFTER_IDLE_TICKS = 20;

    private VisibleInventoryPolicy() {}

    public static boolean shouldOpen(boolean enabled, boolean taskRunning, boolean playerInventorySlot, boolean otherScreenOpen) {
        return enabled && taskRunning && playerInventorySlot && !otherScreenOpen;
    }

    public static boolean shouldClose(long ticksSinceClick, boolean cursorEmpty, boolean taskRunning) {
        if (!cursorEmpty) return false;
        return !taskRunning || ticksSinceClick > CLOSE_AFTER_IDLE_TICKS;
    }
}
