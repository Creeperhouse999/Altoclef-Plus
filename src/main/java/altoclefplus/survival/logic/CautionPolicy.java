package altoclefplus.survival.logic;

import altoclefplus.core.logic.PlusConfig;

/**
 * Don't rush and die: rest and eat when hurt, flee when badly hurt with enemies around, and don't keep going
 * deeper underground while weak. Holding uses hysteresis (start at holdHealth, end at resumeHealth).
 */
public final class CautionPolicy {
    public enum Action { NONE, HOLD_AND_EAT, FLEE }

    private boolean holding;

    public Action decide(float health, int hostilesNear, double y, PlusConfig.Survival cfg) {
        if (!cfg.caution) {
            holding = false;
            return Action.NONE;
        }
        if (hostilesNear > 0) {
            return health <= cfg.fleeHealth ? Action.FLEE : Action.NONE;
        }
        if (holding) {
            if (health >= cfg.resumeHealth) {
                holding = false;
                return Action.NONE;
            }
            return Action.HOLD_AND_EAT;
        }
        boolean hurt = health <= cfg.holdHealth;
        boolean weakUnderground = y < cfg.noDescendY && health <= cfg.noDescendHealth;
        if (hurt || weakUnderground) {
            holding = true;
            return Action.HOLD_AND_EAT;
        }
        return Action.NONE;
    }

    public boolean isHolding() {
        return holding;
    }
}
