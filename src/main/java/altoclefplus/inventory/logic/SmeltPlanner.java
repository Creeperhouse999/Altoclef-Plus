package altoclefplus.inventory.logic;

/** Background smelting: how long the bot may leave a filled furnace for a side job. */
public final class SmeltPlanner {
    /** A furnace smelts one item every 10 seconds. */
    private static final double SECONDS_PER_ITEM = 10;
    /** Shorter waits aren't worth walking away for. */
    private static final double MIN_SECONDS = 20;
    /** Stop fetching coal on the side once this much is carried. */
    public static final int ENOUGH_COAL = 32;

    private SmeltPlanner() {}

    /** Seconds the bot may spend away (0 = just wait at the furnace). */
    public static int sideJobSeconds(int itemsInFurnace, double cookProgress, int coalCarried) {
        if (coalCarried >= ENOUGH_COAL) return 0;
        double left = (itemsInFurnace - Math.max(0, Math.min(1, cookProgress))) * SECONDS_PER_ITEM;
        return left >= MIN_SECONDS ? (int) Math.round(left) : 0;
    }
}
