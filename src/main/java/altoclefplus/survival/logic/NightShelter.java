package altoclefplus.survival.logic;

/**
 * Early @gamer nights killed the bot again and again (skeletons, zombie groups). Without iron armor and
 * without a way to sleep, it waits the night out in a covered 2-deep hole instead.
 */
public final class NightShelter {
    private NightShelter() {}

    /** Mobs spawn freely from about 13000 to 23000 in each 24000-tick day. */
    public static boolean isNight(long timeOfDay) {
        long t = Math.floorMod(timeOfDay, 24000L);
        return t >= 13000 && t < 23000;
    }

    /**
     * @param armorTier best chest armor tier (ItemInfo tiers: 4 = iron)
     * @param canSleep  has a bed it can use tonight
     * @param blocks    throwaway blocks carried (one is needed to cover the hole)
     */
    public static boolean shouldShelter(boolean enabled, long timeOfDay, String dimension, int armorTier,
                                        boolean canSleep, boolean inWater, int blocks) {
        return enabled && isNight(timeOfDay) && "overworld".equals(dimension) && armorTier < 4 && !canSleep
                && !inWater && blocks > 0;
    }
}
