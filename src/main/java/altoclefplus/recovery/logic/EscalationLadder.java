package altoclefplus.recovery.logic;

import altoclefplus.core.logic.PlusConfig;

import java.util.Optional;

/**
 * What to try when stuck, one step further each time: repath, avoid the spot, restart the task, walk away.
 * Full cycles double the cooldown. After several cycles: safe mode (get safe, eat, alert, retry slowly).
 * The bot is only ever stopped if the user configured stopAfterSafeModeCycles.
 */
public final class EscalationLadder {
    public enum Level { REPATH, BLACKLIST, RESTART, ESCAPE, SAFE_MODE }

    private static final Level[] CYCLE = {Level.REPATH, Level.BLACKLIST, Level.RESTART, Level.ESCAPE};

    private final PlusConfig.Recovery cfg;
    private int step;
    private int cycles;
    private int safeModes;
    private boolean safeMode;
    private double cooldownUntil = Double.NEGATIVE_INFINITY;
    private double lastStuck = Double.NEGATIVE_INFINITY;

    public EscalationLadder(PlusConfig.Recovery cfg) {
        this.cfg = cfg;
    }

    public Optional<Level> onStuck(double nowSec) {
        lastStuck = nowSec;
        if (nowSec < cooldownUntil) return Optional.empty();
        if (cycles >= cfg.safeModeAfterCycles) {
            safeMode = true;
            safeModes++;
            cycles = 0;
            step = 0;
            cooldownUntil = nowSec + cfg.safeModeRetryMinutes * 60.0;
            return Optional.of(Level.SAFE_MODE);
        }
        Level level = CYCLE[step];
        step++;
        if (step >= CYCLE.length) {
            step = 0;
            cycles++;
        }
        cooldownUntil = nowSec + currentCooldownSeconds();
        return Optional.of(level);
    }

    /** Call while the bot makes normal progress; resets everything after a clean stretch. */
    public void onProgress(double nowSec) {
        if (nowSec - lastStuck >= cfg.resetAfterCleanSeconds) {
            step = 0;
            cycles = 0;
            safeMode = false;
            cooldownUntil = Double.NEGATIVE_INFINITY;
        }
    }

    public double currentCooldownSeconds() {
        double c = cfg.baseCooldownSeconds * Math.pow(2, cycles);
        return Math.min(c, cfg.maxCooldownSeconds);
    }

    public boolean inSafeMode() {
        return safeMode;
    }

    public boolean shouldStopBot() {
        return cfg.stopAfterSafeModeCycles > 0 && safeModes >= cfg.stopAfterSafeModeCycles;
    }
}
