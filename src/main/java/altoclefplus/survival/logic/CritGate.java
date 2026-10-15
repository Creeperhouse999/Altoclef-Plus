package altoclefplus.survival.logic;

/**
 * Decides, at the moment AltoClef wants to swing, whether to hit now, jump first, or wait: waits for a full
 * charge bar, then jumps and hits on the way down (critical hit, x1.5). If a crit can't happen safely, or it
 * doesn't come within {@code timeoutTicks} of the first wait (on the ground or in the air), it just hits so a
 * fight never stalls.
 */
public final class CritGate {
    public enum Decision { HIT, JUMP, WAIT }

    private static final float READY = 0.9f;
    private static final double FALLING = -0.08;

    private final long timeoutTicks;
    private long waitingSince = -1;
    private boolean jumped;

    public CritGate(long timeoutTicks) {
        this.timeoutTicks = timeoutTicks;
    }

    public Decision decide(float cooldown, boolean onGround, double velocityY, boolean safe, long tick) {
        if (cooldown < READY) return Decision.WAIT;
        if (!safe) return hit();
        if (!onGround && velocityY < FALLING) return hit();
        if (waitingSince < 0) waitingSince = tick;
        if (tick - waitingSince > timeoutTicks) return hit();
        if (onGround && !jumped) {
            jumped = true;
            return Decision.JUMP;
        }
        return Decision.WAIT;
    }

    private Decision hit() {
        waitingSince = -1;
        jumped = false;
        return Decision.HIT;
    }
}
