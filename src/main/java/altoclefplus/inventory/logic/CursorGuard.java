package altoclefplus.inventory.logic;

/** Detects an item stuck on the cursor (the "endless cursor loop") so it can be put away. */
public final class CursorGuard {
    public enum Action { NONE, STASH }

    private final int timeoutTicks;
    private int stuckTicks;

    public CursorGuard(int timeoutTicks) {
        this.timeoutTicks = timeoutTicks;
    }

    public Action tick(boolean cursorEmpty, boolean clickedThisTick) {
        return tick(cursorEmpty, clickedThisTick, false);
    }

    /** With a container open (furnace, crafting table) AltoClef holds items between clicks: wait 10x longer. */
    public Action tick(boolean cursorEmpty, boolean clickedThisTick, boolean containerOpen) {
        if (cursorEmpty || clickedThisTick) {
            stuckTicks = 0;
            return Action.NONE;
        }
        stuckTicks++;
        if (stuckTicks >= (containerOpen ? timeoutTicks * 10 : timeoutTicks)) {
            stuckTicks = 0;
            return Action.STASH;
        }
        return Action.NONE;
    }
}
