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
        if (cursorEmpty || clickedThisTick) {
            stuckTicks = 0;
            return Action.NONE;
        }
        stuckTicks++;
        if (stuckTicks >= timeoutTicks) {
            stuckTicks = 0;
            return Action.STASH;
        }
        return Action.NONE;
    }
}
