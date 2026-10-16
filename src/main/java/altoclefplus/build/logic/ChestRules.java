package altoclefplus.build.logic;

/** How much the bot may take from a chest: always leave the set minimum behind (e.g. one stack). */
public final class ChestRules {
    private ChestRules() {}

    /**
     * AltoClef's container pickup works with "have this many in total": cap that total so the chest keeps
     * its minimum. @return the new total target (never below what is already carried)
     */
    /** Take/leave rules are for storage (chests, barrels, shulkers), never the bot's furnaces or brewing stands. */
    public static boolean appliesTo(String containerType) {
        return "CHEST".equals(containerType) || "SHULKER".equals(containerType) || "ENDER_CHEST".equals(containerType);
    }

    public static int cappedTarget(int carried, int wantedTotal, int inChest, int leaveAtLeast) {
        return carried + takeable(inChest, leaveAtLeast, Math.max(0, wantedTotal - carried));
    }

    public static int takeable(int inChest, int leaveAtLeast, int needed) {
        return Math.max(0, Math.min(needed, inChest - Math.max(0, leaveAtLeast)));
    }
}
