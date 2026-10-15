package altoclefplus.survival.logic;

/**
 * AltoClef only ever runs from creepers, so one that keeps following turns into an endless chase. Once a
 * creeper has stayed within 8 blocks for a while, fight it — if the bot has a weapon and enough health.
 */
public final class CreeperChase {
    private static final double CHASE_RANGE = 8;
    private static final float MIN_HEALTH = 10;

    private final long followTicks;
    private long since = -1;

    public CreeperChase(long followTicks) {
        this.followTicks = followTicks;
    }

    public boolean shouldFight(boolean creeperNear, double distance, boolean hasWeapon, float health, long tick) {
        if (!creeperNear || distance > CHASE_RANGE) {
            since = -1;
            return false;
        }
        if (since < 0) since = tick;
        return tick - since >= followTicks && hasWeapon && health >= MIN_HEALTH;
    }

    public void reset() {
        since = -1;
    }
}
