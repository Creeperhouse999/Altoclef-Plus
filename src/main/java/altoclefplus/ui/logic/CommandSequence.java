package altoclefplus.ui.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs "a; b; c" one command at a time. AltoClef would run the rest of a ';' list even after its task was
 * cancelled (cancel counts as "finished"), so Stop/Pause didn't stop presets. Every run/cancel bumps a
 * generation number; a finish callback from an older generation does nothing.
 */
public final class CommandSequence {
    /** Starts one AltoClef command (no prefix) and calls onFinish when it ends or is cancelled. */
    public interface Executor {
        void execute(String command, Runnable onFinish);
    }

    private final Executor executor;
    private int generation;

    public CommandSequence(Executor executor) {
        this.executor = executor;
    }

    public void run(String commands) {
        int gen = ++generation;
        List<String> parts = new ArrayList<>();
        for (String part : commands.split(";")) {
            String p = part.trim();
            if (!p.isEmpty()) parts.add(p);
        }
        step(parts, 0, gen);
    }

    public void cancel() {
        generation++;
    }

    private void step(List<String> parts, int index, int gen) {
        if (gen != generation || index >= parts.size()) return;
        executor.execute(parts.get(index), () -> step(parts, index + 1, gen));
    }
}
