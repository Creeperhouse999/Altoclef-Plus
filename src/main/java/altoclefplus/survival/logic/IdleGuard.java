package altoclefplus.survival.logic;

/**
 * "Defend while idle": with no task running and nobody at the controls, a nearby threat switches
 * AltoClef's mob defense on (ENGAGE); after a calm period, or as soon as the player plays again,
 * it is switched back off (RELEASE). A real task taking over is left alone.
 */
public final class IdleGuard {
    public enum Action { NONE, ENGAGE, RELEASE }

    private final int calmTicksNeeded;
    private boolean engaged;
    private int calmTicks;

    public IdleGuard(int calmTicksNeeded) {
        this.calmTicksNeeded = calmTicksNeeded;
    }

    public Action tick(boolean enabled, boolean userTaskRunning, boolean playerActive, boolean threat) {
        if (userTaskRunning) {
            engaged = false;
            return Action.NONE;
        }
        if (engaged && (!enabled || playerActive)) {
            engaged = false;
            return Action.RELEASE;
        }
        if (!enabled || playerActive) return Action.NONE;
        if (threat) {
            calmTicks = 0;
            if (engaged) return Action.NONE;
            engaged = true;
            return Action.ENGAGE;
        }
        if (engaged && ++calmTicks >= calmTicksNeeded) {
            engaged = false;
            return Action.RELEASE;
        }
        return Action.NONE;
    }

    public boolean engaged() {
        return engaged;
    }
}
