package altoclefplus.survival.logic;

import altoclefplus.core.logic.PlusConfig;

/** Whether and how to go back for items after dying. */
public final class DeathRecoveryPlanner {
    public enum Plan { SKIP, GO, KIT_THEN_GO }

    /** Dropped items despawn after 5 minutes in loaded chunks; allow one extra minute. */
    public static final int GIVE_UP_TICKS = 6 * 60 * 20;

    private DeathRecoveryPlanner() {}

    /**
     * @param cause     "lava", "void", "mob", "fall", ... (items are destroyed by lava and void)
     * @param reachable death happened in the dimension the bot respawned in
     */
    public static Plan plan(String cause, boolean reachable, double distance, double deathY, PlusConfig.Survival cfg) {
        if (!cfg.deathRecovery || "lava".equals(cause) || "void".equals(cause) || !reachable) return Plan.SKIP;
        if (distance > cfg.deathKitDistance || deathY < cfg.deathKitBelowY) return Plan.KIT_THEN_GO;
        return Plan.GO;
    }

    public static boolean expired(long loadedTicksSinceDeath) {
        return loadedTicksSinceDeath >= GIVE_UP_TICKS;
    }
}
