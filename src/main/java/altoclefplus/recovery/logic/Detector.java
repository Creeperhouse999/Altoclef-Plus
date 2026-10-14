package altoclefplus.recovery.logic;

import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;

import java.util.Optional;

/** Watches the snapshot stream for one kind of "stuck". Returns details when it fires. */
public abstract class Detector {
    protected final PlusConfig.Recovery cfg;

    protected Detector(PlusConfig.Recovery cfg) {
        this.cfg = cfg;
    }

    public abstract StuckKind kind();

    /** Feed one snapshot. Quiet (and reset) while the bot is legitimately busy or idle. */
    public final Optional<String> update(Snapshot s) {
        if (suppressed(s)) {
            reset();
            return Optional.empty();
        }
        return check(s);
    }

    protected abstract Optional<String> check(Snapshot s);

    protected abstract void reset();

    /** No task, menus, eating, fighting, dead, or a known long wait (smelting, sleeping...). */
    public boolean suppressed(Snapshot s) {
        if (!s.userTaskRunning || s.containerOpen || s.eating || s.mobDefenseActive || s.dead) return true;
        for (String frag : cfg.longWaitTasks) {
            if (!frag.isEmpty() && s.leafTask.contains(frag)) return true;
        }
        return false;
    }

    /** "<Name> debug state" -> "<Name>" (task identity without its changing state text). */
    static String taskName(String chainEntry) {
        int end = chainEntry.indexOf('>');
        return end > 0 ? chainEntry.substring(0, end + 1) : chainEntry;
    }
}
