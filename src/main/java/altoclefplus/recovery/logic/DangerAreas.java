package altoclefplus.recovery.logic;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Places where the bot keeps dying: paths avoid walking through them for a while, so it doesn't walk back
 * into the same trap. Read by Baritone's pathing thread, hence the thread-safe list.
 */
public final class DangerAreas {
    private record Area(String dimension, int x, int y, int z, int radius, long untilMs) {}

    private final List<Area> areas = new CopyOnWriteArrayList<>();
    private volatile String standDim = "";
    private volatile int sx, sy, sz;

    public void add(String dimension, int x, int y, int z, int radius, long forMs, long nowMs) {
        areas.add(new Area(dimension, x, y, z, radius, nowMs + forMs));
    }

    /** The bot's current cell is never avoided, or it could not walk out of an area it is in. */
    public void setStandingAt(String dimension, int x, int y, int z) {
        standDim = dimension;
        sx = x;
        sy = y;
        sz = z;
    }

    public boolean avoid(String dimension, int x, int y, int z, long nowMs) {
        if (areas.isEmpty()) return false;
        if (dimension.equals(standDim) && x == sx && z == sz && Math.abs(y - sy) <= 1) return false;
        for (Area a : areas) {
            if (a.untilMs() < nowMs) {
                areas.remove(a);
                continue;
            }
            if (!a.dimension().equals(dimension)) continue;
            long dx = x - a.x(), dy = y - a.y(), dz = z - a.z();
            long cell = dx * dx + dy * dy + dz * dz, r2 = (long) a.radius() * a.radius();
            if (cell > r2) continue;
            // Already inside (e.g. respawned there): only block steps back towards the centre, so it can leave.
            if (a.dimension().equals(standDim)) {
                long bx = sx - a.x(), by = sy - a.y(), bz = sz - a.z();
                long bot = bx * bx + by * by + bz * bz;
                if (bot <= r2 && cell >= bot) continue;
            }
            return true;
        }
        return false;
    }

    /** {@link #avoid} in the dimension the bot is in now. */
    public boolean avoidHere(int x, int y, int z, long nowMs) {
        return avoid(standDim, x, y, z, nowMs);
    }

    public boolean isEmpty() {
        return areas.isEmpty();
    }
}
