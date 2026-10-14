package altoclefplus.core;

import adris.altoclef.AltoClef;
import adris.altoclef.chains.SingleTaskChain;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import altoclefplus.Plus;
import altoclefplus.core.logic.events.Notice;

/**
 * An AltoClef task chain for short side jobs (equip, craft one item, get gear for a gate...). AltoClef runs the
 * highest-priority active chain, so while a job is set this chain briefly takes over from the user task
 * (priority 50) but still yields to eating (55) and mob defense (65+).
 */
public class PlusChain extends SingleTaskChain {
    private final String name;
    private final float priority;
    private String jobLabel = "";
    private Runnable onFinish = () -> {};

    public PlusChain(TaskRunner runner, String name, float priority) {
        super(runner);
        this.name = name;
        this.priority = priority;
    }

    /** Starts a job if none is running. @return true if started. */
    public boolean startJob(Task task, String label, Runnable whenDone) {
        if (isActive()) return false;
        jobLabel = label;
        onFinish = whenDone == null ? () -> {} : whenDone;
        setTask(task);
        Plus.bus().publish(new Notice(name, label));
        return true;
    }

    public void cancelJob() {
        if (isActive()) stop(Plus.mod());
        _mainTask = null;
        jobLabel = "";
    }

    public String jobLabel() {
        return isActive() ? jobLabel : "";
    }

    @Override
    public float getPriority(AltoClef mod) {
        if (!AltoClef.inGame() || !isActive()) return Float.NEGATIVE_INFINITY;
        return priority;
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {
        _mainTask = null;
        jobLabel = "";
        Runnable r = onFinish;
        onFinish = () -> {};
        r.run();
    }

    @Override
    public String getName() {
        return name;
    }
}
