package altoclefplus.inventory.logic;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The bot crafted and placed a new crafting table / furnace every time, because it left the old one behind.
 * Track the stations it placed itself (they appear while AltoClef is placing a container) and pick them up
 * again once it has stopped using them for 3 seconds, so the next craft places the carried one. Stations that
 * were already there (villages, the player's base) are never touched; ones left more than 8 blocks behind are
 * forgotten.
 */
public final class StationTracker {
    public static final long WAIT_TICKS = 60;
    public static final double MAX_DISTANCE = 8;

    private Set<Long> known = new HashSet<>();
    private final Set<Long> ours = new HashSet<>();
    private long lastUseTick = Long.MIN_VALUE / 2;
    private long lastPlacingTick = Long.MIN_VALUE / 2;
    public static final long PLACING_GRACE_TICKS = 40;

    /**
     * @param stationsNow positions (BlockPos.asLong) of crafting tables / furnaces near the bot right now
     * @param placing     AltoClef is placing a container at the moment
     */
    public void observe(Set<Long> stationsNow, boolean placing, long tick) {
        if (placing) lastPlacingTick = tick;
        // The block often appears a scan after the task text moved on from "placing": allow 2 seconds.
        boolean recentlyPlacing = tick - lastPlacingTick <= PLACING_GRACE_TICKS;
        for (long p : stationsNow) {
            if (!known.contains(p) && recentlyPlacing) {
                ours.add(p);
                // One placing makes one station: anything else appearing now isn't ours.
                lastPlacingTick = Long.MIN_VALUE / 2;
                recentlyPlacing = false;
            }
        }
        ours.retainAll(stationsNow);
        known = new HashSet<>(stationsNow);
    }

    /**
     * @param distances  distance from the bot to each station near it
     * @param usingNow   a crafting / smelting step is running
     * @return the station of ours to pick up now, if any
     */
    public Optional<Long> toCollect(Map<Long, Double> distances, boolean usingNow, long tick) {
        if (usingNow) {
            lastUseTick = tick;
            return Optional.empty();
        }
        if (tick - lastUseTick <= WAIT_TICKS) return Optional.empty();
        Long best = null;
        double bestD = Double.MAX_VALUE;
        for (Long p : new HashSet<>(ours)) {
            Double d = distances.get(p);
            if (d == null) continue;
            if (d > MAX_DISTANCE) {
                ours.remove(p);
                continue;
            }
            if (d < bestD) {
                bestD = d;
                best = p;
            }
        }
        return Optional.ofNullable(best);
    }

    public void collected(long pos) {
        ours.remove(pos);
    }

    public Set<Long> ours() {
        return Set.copyOf(ours);
    }
}
