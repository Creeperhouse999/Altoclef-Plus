package altoclefplus.survival.logic;

import altoclefplus.core.logic.PlusConfig;

import java.util.ArrayList;
import java.util.List;

/** Notices repeated deaths in the same place so the bot stops walking back into the same trap. */
public final class DeathLoopGuard {
    private record Death(long timeMs, double x, double z) {}

    private final List<Death> deaths = new ArrayList<>();

    public void record(long timeMs, double x, double z) {
        deaths.add(new Death(timeMs, x, z));
    }

    public boolean isLoop(PlusConfig.Recovery cfg, long nowMs) {
        long window = cfg.deathLoopMinutes * 60_000L;
        deaths.removeIf(d -> nowMs - d.timeMs() > window);
        if (deaths.isEmpty()) return false;
        Death last = deaths.get(deaths.size() - 1);
        long near = deaths.stream()
                .filter(d -> Math.hypot(d.x() - last.x(), d.z() - last.z()) <= cfg.deathLoopRadius)
                .count();
        return near >= cfg.deathLoopDeaths;
    }

    /** Centre of the latest death, for marking the area as dangerous. */
    public double[] lastPosition() {
        if (deaths.isEmpty()) return null;
        Death d = deaths.get(deaths.size() - 1);
        return new double[]{d.x(), d.z()};
    }
}
