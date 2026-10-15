package altoclefplus.core;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;

/**
 * Runs another task as a chain's main task. AltoClef's chains ask the main task "finished?" before ever
 * ticking it, and some tasks (e.g. TimeoutWanderTask) crash on that because they set themselves up only when
 * started. This wrapper only asks once the inner task has really started.
 */
public final class StartedFirstTask extends Task {
    private final Task inner;
    private boolean ticked;

    public StartedFirstTask(Task inner) {
        this.inner = inner;
    }

    @Override
    protected void onStart(AltoClef mod) {
    }

    @Override
    protected Task onTick(AltoClef mod) {
        ticked = true;
        setDebugState(inner.toString());
        return inner;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return ticked && inner.isActive() && inner.isFinished(mod);
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof StartedFirstTask s && s.inner.equals(inner);
    }

    @Override
    protected String toDebugString() {
        return inner.toString();
    }
}
