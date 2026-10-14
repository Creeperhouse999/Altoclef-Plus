package altoclefplus.survival.logic;

import altoclefplus.core.logic.PlusConfig;

/**
 * Don't rush and die: rest and eat when hurt, flee when badly hurt with enemies around, and don't keep going
 * deeper underground while weak. Holding uses hysteresis (start at holdHealth, end at resumeHealth), only
 * starts when the bot can actually heal (food to eat, or hunger high enough to regenerate), and backs off
 * for a while after a rest that didn't heal.
 */
public final class CautionPolicy {
    public enum Action { NONE, HOLD_AND_EAT, FLEE }

    /** Hunger needed for natural regeneration. */
    public static final int REGEN_HUNGER = 18;
    public static final double BACKOFF_SECONDS = 120;

    private boolean holding;
    private double noRestUntil = Double.NEGATIVE_INFINITY;

    /**
     * @param hasFood   carries something edible
     * @param hunger    current food level (0-20)
     * @param nowSec    time in seconds
     */
    public Action decide(float health, int hostilesNear, double y, boolean hasFood, int hunger, double nowSec,
                         PlusConfig.Survival cfg) {
        if (!cfg.caution) {
            holding = false;
            return Action.NONE;
        }
        if (hostilesNear > 0) {
            return health <= cfg.fleeHealth ? Action.FLEE : Action.NONE;
        }
        boolean canHeal = hasFood || hunger >= REGEN_HUNGER;
        if (holding) {
            if (health >= cfg.resumeHealth || !canHeal) {
                holding = false;
                return Action.NONE;
            }
            return Action.HOLD_AND_EAT;
        }
        if (!canHeal || nowSec < noRestUntil) return Action.NONE;
        boolean hurt = health <= cfg.holdHealth;
        boolean weakUnderground = y < cfg.noDescendY && health <= cfg.noDescendHealth;
        if (hurt || weakUnderground) {
            holding = true;
            return Action.HOLD_AND_EAT;
        }
        return Action.NONE;
    }

    /** The rest timed out without reaching resume health: don't try again for a while. */
    public void restEndedWithoutHealing(double nowSec) {
        holding = false;
        noRestUntil = nowSec + BACKOFF_SECONDS;
    }

    public boolean isHolding() {
        return holding;
    }
}
