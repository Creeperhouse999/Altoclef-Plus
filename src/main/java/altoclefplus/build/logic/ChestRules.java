package altoclefplus.build.logic;

/** How much the bot may take from a chest: always leave the set minimum behind (e.g. one stack). */
public final class ChestRules {
    private ChestRules() {}

    public static int takeable(int inChest, int leaveAtLeast, int needed) {
        return Math.max(0, Math.min(needed, inChest - Math.max(0, leaveAtLeast)));
    }
}
