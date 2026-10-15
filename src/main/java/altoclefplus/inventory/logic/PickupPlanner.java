package altoclefplus.inventory.logic;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Our pickup rule: walk to the nearest dropped item the task needs, within range and already landed, one at
 * a time. A drop not picked up within the time limit (stuck in leaves, behind a wall) is given up on, so the
 * bot never stands around waiting for it.
 */
public final class PickupPlanner {
    public record Drop(int id, String item, double distance, boolean settled) {}

    private final double range;
    private final long giveUpTicks;
    private int chasing = -1;
    private long chaseStart;
    private final Set<Integer> givenUp = new HashSet<>();

    public PickupPlanner(double range, long giveUpTicks) {
        this.range = range;
        this.giveUpTicks = giveUpTicks;
    }

    public Optional<Integer> choose(List<Drop> drops, Set<String> wanted, long tick) {
        if (chasing >= 0 && tick - chaseStart > giveUpTicks) {
            givenUp.add(chasing);
            chasing = -1;
        }
        Drop best = null;
        for (Drop d : drops) {
            if (!d.settled() || d.distance() > range || !wanted.contains(d.item()) || givenUp.contains(d.id())) continue;
            if (d.id() == chasing) return Optional.of(d.id()); // keep going for the one we chose
            if (best == null || d.distance() < best.distance()) best = d;
        }
        if (best == null) {
            chasing = -1;
            return Optional.empty();
        }
        chasing = best.id();
        chaseStart = tick;
        return Optional.of(best.id());
    }
}
