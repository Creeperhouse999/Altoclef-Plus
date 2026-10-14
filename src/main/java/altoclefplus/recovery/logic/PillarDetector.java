package altoclefplus.recovery.logic;

import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;

import java.util.ArrayDeque;
import java.util.Optional;

/**
 * The "Nether elevator": climbing or bobbing up and down in one spot without the goal being straight up,
 * or reaching the Nether ceiling.
 */
public final class PillarDetector extends Detector {
    private record Sample(double sec, double x, double y, double z) {}

    private final ArrayDeque<Sample> window = new ArrayDeque<>();

    public PillarDetector(PlusConfig.Recovery cfg) {
        super(cfg);
    }

    @Override
    public StuckKind kind() {
        return StuckKind.PILLAR;
    }

    @Override
    protected Optional<String> check(Snapshot s) {
        if (s.goalVertical) {
            window.clear();
            return Optional.empty();
        }
        if ("the_nether".equals(s.dimension) && s.y >= cfg.netherCeilingY) {
            window.clear();
            return Optional.of("at the Nether ceiling (y=" + Math.round(s.y) + ")");
        }
        window.addLast(new Sample(s.seconds(), s.x, s.y, s.z));
        while (!window.isEmpty() && s.seconds() - window.peekFirst().sec() > cfg.pillarWindowSeconds) window.removeFirst();

        Sample first = window.peekFirst();
        double horizontal = Math.hypot(s.x - first.x(), s.z - first.z());
        if (horizontal >= cfg.pillarMaxHorizontal) return Optional.empty();

        double net = Math.abs(s.y - first.y());
        double travel = 0;
        Sample prev = null;
        for (Sample cur : window) {
            if (prev != null) travel += Math.abs(cur.y() - prev.y());
            prev = cur;
        }
        if (net >= cfg.pillarDeltaY || travel >= 2.0 * cfg.pillarDeltaY) {
            window.clear();
            return Optional.of(net >= cfg.pillarDeltaY
                    ? "moved " + Math.round(net) + " blocks vertically in place"
                    : "bobbing up and down in place");
        }
        return Optional.empty();
    }

    @Override
    protected void reset() {
        window.clear();
    }
}
