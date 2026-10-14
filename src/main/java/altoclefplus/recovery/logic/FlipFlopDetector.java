package altoclefplus.recovery.logic;

import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Pathing, but pacing between the same few spots while Baritone keeps re-planning. */
public final class FlipFlopDetector extends Detector {
    private record Sample(double sec, long cell, int pathCounter) {}

    private final ArrayDeque<Sample> window = new ArrayDeque<>();
    private double lastSampleSec = Double.NEGATIVE_INFINITY;

    public FlipFlopDetector(PlusConfig.Recovery cfg) {
        super(cfg);
    }

    @Override
    public StuckKind kind() {
        return StuckKind.FLIP_FLOP;
    }

    @Override
    protected Optional<String> check(Snapshot s) {
        if (!s.pathing) {
            reset();
            return Optional.empty();
        }
        double now = s.seconds();
        if (now - lastSampleSec < 1.0) return Optional.empty();
        lastSampleSec = now;
        long cell = (((long) Math.floor(s.x / 2)) << 32) ^ (Math.floor(s.z / 2) == 0 ? 0 : (long) Math.floor(s.z / 2) & 0xffffffffL);
        window.addLast(new Sample(now, cell, s.pathCounter));
        while (!window.isEmpty() && now - window.peekFirst().sec() > cfg.flipFlopWindowSeconds) window.removeFirst();

        if (now - window.peekFirst().sec() < cfg.flipFlopWindowSeconds - 2) return Optional.empty();
        int repaths = s.pathCounter - window.peekFirst().pathCounter();
        if (repaths < cfg.flipFlopMinRepaths) return Optional.empty();

        Map<Long, Integer> counts = new HashMap<>();
        for (Sample sm : window) counts.merge(sm.cell(), 1, Integer::sum);
        List<Integer> sorted = new ArrayList<>(counts.values());
        sorted.sort((a, b) -> b - a);
        int top = 0;
        for (int i = 0; i < Math.min(cfg.flipFlopMaxCells, sorted.size()); i++) top += sorted.get(i);
        if (top >= cfg.flipFlopShare * window.size()) {
            window.clear();
            return Optional.of("pacing between " + Math.min(cfg.flipFlopMaxCells, sorted.size()) + " spots, " + repaths + " re-plans");
        }
        return Optional.empty();
    }

    @Override
    protected void reset() {
        window.clear();
        lastSampleSec = Double.NEGATIVE_INFINITY;
    }
}
