package altoclefplus.recovery.logic;

import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;

import java.util.ArrayDeque;
import java.util.Optional;

/** Overworld -> Nether -> Overworld round trips piling up in a short time. Not reset by menus or eating. */
public final class DimensionPingPongDetector extends Detector {
    private final ArrayDeque<Double> returns = new ArrayDeque<>();
    private String last = "";
    private boolean visitedNether;

    public DimensionPingPongDetector(PlusConfig.Recovery cfg) {
        super(cfg);
    }

    @Override
    public StuckKind kind() {
        return StuckKind.DIMENSION_PING_PONG;
    }

    @Override
    public boolean suppressed(Snapshot s) {
        return !s.userTaskRunning;
    }

    @Override
    protected Optional<String> check(Snapshot s) {
        double now = s.seconds();
        if (!s.dimension.equals(last)) {
            if ("the_nether".equals(s.dimension)) visitedNether = true;
            if ("overworld".equals(s.dimension) && visitedNether) {
                returns.addLast(now);
                visitedNether = false;
            }
            last = s.dimension;
        }
        while (!returns.isEmpty() && now - returns.peekFirst() > cfg.pingPongMinutes * 60.0) returns.removeFirst();
        if (returns.size() >= cfg.pingPongRoundTrips) {
            int n = returns.size();
            returns.clear();
            return Optional.of(n + " Nether round trips in " + cfg.pingPongMinutes + " minutes");
        }
        return Optional.empty();
    }

    @Override
    protected void reset() {
        returns.clear();
        last = "";
        visitedNether = false;
    }
}
