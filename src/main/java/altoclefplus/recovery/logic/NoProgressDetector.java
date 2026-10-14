package altoclefplus.recovery.logic;

import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;

import java.util.Map;
import java.util.Optional;

/** Nothing moved, nothing collected and the step didn't change for a while. */
public final class NoProgressDetector extends Detector {
    private boolean anchored;
    private double ax, ay, az;
    private double anchorSec;
    private Map<String, Integer> anchorItems;
    private String anchorStep;

    public NoProgressDetector(PlusConfig.Recovery cfg) {
        super(cfg);
    }

    @Override
    public StuckKind kind() {
        return StuckKind.NO_PROGRESS;
    }

    @Override
    protected Optional<String> check(Snapshot s) {
        String step = s.taskChain.isEmpty() ? "" : s.taskChain.get(s.taskChain.size() - 1);
        String stepName = taskName(step);
        if (!anchored || progressed(s, stepName)) {
            anchor(s, stepName);
            return Optional.empty();
        }
        double waited = s.seconds() - anchorSec;
        if (waited >= cfg.noProgressSeconds) {
            anchor(s, stepName);
            return Optional.of("no progress for " + Math.round(waited) + "s on " + stepName);
        }
        return Optional.empty();
    }

    private boolean progressed(Snapshot s, String stepName) {
        double dx = s.x - ax, dy = s.y - ay, dz = s.z - az;
        return Math.sqrt(dx * dx + dy * dy + dz * dz) > cfg.noProgressDistance
                || !s.items.equals(anchorItems)
                || !stepName.equals(anchorStep);
    }

    private void anchor(Snapshot s, String stepName) {
        anchored = true;
        ax = s.x;
        ay = s.y;
        az = s.z;
        anchorSec = s.seconds();
        anchorItems = s.items;
        anchorStep = stepName;
    }

    @Override
    protected void reset() {
        anchored = false;
    }
}
