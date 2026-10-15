package altoclefplus.survival.logic;

/**
 * Keeps the bot on the block it is already heading to. A new block only wins when it is much closer
 * (under half the distance) and not more often than once per cooldown; a gone or unreachable target
 * is replaced right away.
 */
public final class TargetStickiness {
    private final long cooldownTicks;
    private long lastSwitchTick = Long.MIN_VALUE / 2;

    public TargetStickiness(long cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    /** Distances are squared. {@code currentValid} = the current target still exists and isn't blacklisted. */
    public boolean shouldSwitch(boolean currentValid, double currentDistSq, double candidateDistSq, long tick) {
        if (!currentValid) return true;
        if (tick - lastSwitchTick < cooldownTicks) return false;
        return candidateDistSq < currentDistSq / 4;
    }

    public void switched(long tick) {
        lastSwitchTick = tick;
    }
}
