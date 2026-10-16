package altoclefplus.inventory.logic;

/**
 * AltoClef crafts by clicking the recipe book and waiting for the server to fill the grid. In some modpacks
 * (JEI, Create) the click does nothing and the bot waited forever with "Waiting for recipe book click...".
 * After 3 seconds of waiting, craft by hand instead.
 */
public final class RecipeBookFallback {
    public static final long WAIT_MS = 3_000;

    private long waitingSince = -1;

    /** Called on every tick that is still waiting; true once it has waited long enough to give up on the book. */
    public boolean waiting(long nowMs) {
        if (waitingSince < 0) waitingSince = nowMs;
        return nowMs - waitingSince >= WAIT_MS;
    }

    /** The book filled the grid (or crafting moved on): start counting again next time. */
    public void progressed() {
        waitingSince = -1;
    }
}
