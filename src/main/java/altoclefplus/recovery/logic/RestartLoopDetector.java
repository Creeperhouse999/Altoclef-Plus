package altoclefplus.recovery.logic;

import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;

import java.util.ArrayDeque;
import java.util.Optional;

/** The same main subtask keeps starting over (watched one level under the top task, ignoring leaf churn). */
public final class RestartLoopDetector extends Detector {
    private record Start(double sec, String name) {}

    private final ArrayDeque<Start> starts = new ArrayDeque<>();
    private String current = "";

    public RestartLoopDetector(PlusConfig.Recovery cfg) {
        super(cfg);
    }

    @Override
    public StuckKind kind() {
        return StuckKind.RESTART_LOOP;
    }

    @Override
    protected Optional<String> check(Snapshot s) {
        if (s.taskChain.isEmpty()) return Optional.empty();
        String entry = s.taskChain.size() >= 2 ? s.taskChain.get(1) : s.taskChain.get(0);
        String name = taskName(entry);
        double now = s.seconds();
        if (!name.equals(current)) {
            current = name;
            starts.addLast(new Start(now, name));
        }
        while (!starts.isEmpty() && now - starts.peekFirst().sec() > cfg.restartWindowSeconds) starts.removeFirst();
        long same = starts.stream().filter(st -> st.name().equals(name)).count();
        if (same >= cfg.restartCount) {
            starts.clear();
            return Optional.of(name + " restarted " + same + " times");
        }
        return Optional.empty();
    }

    @Override
    protected void reset() {
        starts.clear();
        current = "";
    }
}
