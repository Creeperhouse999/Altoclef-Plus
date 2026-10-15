package altoclefplus.survival.logic;

import java.util.function.Predicate;

/**
 * Keeps a weapon choice for a short while. Swapping the held item resets the attack charge bar, so flipping
 * between axe and sword every tick would mean never hitting at all.
 */
public final class WeaponHold {
    private final long holdTicks;
    private String held = "";
    private long since = Long.MIN_VALUE / 2;

    public WeaponHold(long holdTicks) {
        this.holdTicks = holdTicks;
    }

    public String choose(String fresh, long tick, Predicate<String> stillCarried) {
        boolean keep = !held.isEmpty() && tick - since < holdTicks && stillCarried.test(held);
        if (keep) return held;
        if (!fresh.equals(held)) {
            held = fresh;
            since = tick;
        } else {
            since = tick;
        }
        return held;
    }
}
