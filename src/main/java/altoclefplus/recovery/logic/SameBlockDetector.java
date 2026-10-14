package altoclefplus.recovery.logic;

import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;

import java.util.ArrayDeque;
import java.util.Optional;

/** Keeps starting to break the same block, or breaks one block for far too long. */
public final class SameBlockDetector extends Detector {
    private record Start(double sec, long pos) {}

    private final ArrayDeque<Start> starts = new ArrayDeque<>();
    private long current = Snapshot.NO_POS;
    private double currentSince;

    public SameBlockDetector(PlusConfig.Recovery cfg) {
        super(cfg);
    }

    @Override
    public StuckKind kind() {
        return StuckKind.SAME_BLOCK;
    }

    @Override
    protected Optional<String> check(Snapshot s) {
        double now = s.seconds();
        long pos = s.breakingPos;
        if (pos != current) {
            current = pos;
            currentSince = now;
            if (pos != Snapshot.NO_POS) starts.addLast(new Start(now, pos));
        }
        while (!starts.isEmpty() && now - starts.peekFirst().sec() > cfg.sameBlockWindowSeconds) starts.removeFirst();

        if (pos == Snapshot.NO_POS) return Optional.empty();
        long same = starts.stream().filter(st -> st.pos() == pos).count();
        if (same >= cfg.sameBlockStarts) {
            starts.clear();
            return Optional.of("started breaking the same block " + same + " times");
        }
        if (now - currentSince >= cfg.sameBlockMaxSeconds) {
            currentSince = now;
            return Optional.of("breaking one block for " + cfg.sameBlockMaxSeconds + "s");
        }
        return Optional.empty();
    }

    @Override
    protected void reset() {
        starts.clear();
        current = Snapshot.NO_POS;
    }
}
