package altoclefplus.ui.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Runs "a; b; c" one command at a time. AltoClef would run the rest of a ';' list even after its task was
 * cancelled (cancel counts as "finished"), so Stop/Pause didn't stop presets. Every run/cancel bumps a
 * generation number; a finish callback from an older generation does nothing.
 * <p>
 * After the last part, every part is checked again: something got earlier can be lost later (a death took
 * the chestplate while the bot made boots), so a part whose result is missing is run again, for a few
 * rounds at most. Then the completion hook runs once.
 */
public final class CommandSequence {
    /** Starts one AltoClef command (no prefix) and calls onFinish when it ends or is cancelled. */
    public interface Executor {
        void execute(String command, Runnable onFinish);
    }

    private final Executor executor;
    /** True when a part's result is already there (e.g. "get iron_boots 1" and boots are owned). */
    private final Predicate<String> done;
    private final int maxRedoRounds;
    private final Runnable onComplete;
    private int generation;

    public CommandSequence(Executor executor) {
        this(executor, c -> true, 0, () -> { });
    }

    public CommandSequence(Executor executor, Predicate<String> done, int maxRedoRounds, Runnable onComplete) {
        this.executor = executor;
        this.done = done;
        this.maxRedoRounds = maxRedoRounds;
        this.onComplete = onComplete;
    }

    public void run(String commands) {
        int gen = ++generation;
        List<String> parts = new ArrayList<>();
        for (String part : commands.split(";")) {
            String p = part.trim();
            if (!p.isEmpty()) parts.add(p);
        }
        step(parts, 0, 0, gen);
    }

    public void cancel() {
        generation++;
    }

    private void step(List<String> parts, int index, int round, int gen) {
        if (gen != generation) return;
        if (index < parts.size()) {
            executor.execute(parts.get(index), () -> step(parts, index + 1, round, gen));
            return;
        }
        if (round < maxRedoRounds) {
            for (int i = 0; i < parts.size(); i++) {
                if (!done.test(parts.get(i))) {
                    // Redo just that part, then check everything again.
                    executor.execute(parts.get(i), () -> step(parts, parts.size(), round + 1, gen));
                    return;
                }
            }
        }
        onComplete.run();
    }
}
