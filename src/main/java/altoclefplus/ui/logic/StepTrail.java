package altoclefplus.ui.logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** History of the leaf steps the bot worked on, newest last. */
public final class StepTrail {
    public record Step(String label, long startMs, long durationMs, boolean done) {}

    private final int capacity;
    private final ArrayDeque<Step> steps = new ArrayDeque<>();

    public StepTrail(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void update(String label, long nowMs) {
        if (label == null || label.isEmpty()) return;
        Step last = steps.peekLast();
        if (last != null && last.label().equals(label) && !last.done()) return;
        if (last != null && !last.done()) {
            steps.removeLast();
            steps.addLast(new Step(last.label(), last.startMs(), nowMs - last.startMs(), true));
        }
        steps.addLast(new Step(label, nowMs, 0, false));
        while (steps.size() > capacity) steps.removeFirst();
    }

    public synchronized List<Step> entries() {
        return new ArrayList<>(steps);
    }

    public synchronized void clear() {
        steps.clear();
    }
}
