package altoclefplus.survival.logic;

/**
 * Baritone holds jump whenever the bot's feet are in water, so it bobbed along the surface. In deep water with
 * air to spare, let go of jump and sprint instead: the bot dips under and swims like a player. It still jumps
 * when low on air, in shallow water, or to climb out onto a block ahead.
 *
 * Also: what counts as a threat for "Defending". Any damage used to (falls, starving), but the client can't
 * tell what hurt it, so only hostile mobs count.
 */
public final class SwimPolicy {
    private SwimPolicy() {}

    /**
     * @param waterDepth   water blocks from the feet down
     * @param blockAhead   a solid block in front at feet or head height (time to climb out)
     * @param forward      moving forward
     */
    public static boolean swim(boolean feetInWater, int waterDepth, int air, int maxAir, boolean blockAhead, boolean forward) {
        return feetInWater && forward && waterDepth >= 2 && air > maxAir / 2 && !blockAhead;
    }

    /** @param seenNearby hostile it can see within 10 blocks; @param close hostile within 4 blocks */
    public static boolean threat(boolean seenNearby, boolean close) {
        return seenNearby || close;
    }
}
