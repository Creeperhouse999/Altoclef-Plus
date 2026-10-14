package altoclefplus.core;

import adris.altoclef.AltoClef;
import adris.altoclef.chains.SingleTaskChain;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import altoclefplus.Plus;
import altoclefplus.core.logic.events.Notice;

import java.util.HashMap;
import java.util.Map;

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
    private long deadlineMs = Long.MAX_VALUE;
    private final long maxJobMs;
    /** Job labels that timed out recently -> earliest time they may start again. */
    private final Map<String, Long> backoffUntil = new HashMap<>();

    /** Default upper bound for a side job. */
    public static final long MAX_JOB_MS = 5 * 60_000L;
    /** After a job times out, the same job isn't started again for this long. */
    public static final long BACKOFF_MS = 5 * 60_000L;

    public PlusChain(TaskRunner runner, String name, float priority) {
        this(runner, name, priority, MAX_JOB_MS);
    }

    public PlusChain(TaskRunner runner, String name, float priority, long maxJobMs) {
        super(runner);
        this.name = name;
        this.priority = priority;
        this.maxJobMs = maxJobMs;
    }

    /** Starts a job if none is running. @return true if started. */
    public boolean startJob(Task task, String label, Runnable whenDone) {
        if (isActive() || task == null) return false;
        // Only while the bot is running a task: a job queued while AltoClef is idle would hijack the next task.
        if (Plus.mod() == null || !Plus.mod().getUserTaskChain().isActive()) return false;
        long now = System.currentTimeMillis();
        if (backoffUntil.getOrDefault(label, 0L) > now) return false;
        jobLabel = label;
        onFinish = whenDone == null ? () -> {} : whenDone;
        deadlineMs = now + maxJobMs;
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
        if (System.currentTimeMillis() > deadlineMs) {
            Plus.log().add(name, "Gave up on '" + jobLabel + "' (took too long)");
            backoffUntil.put(jobLabel, System.currentTimeMillis() + BACKOFF_MS);
            cancelJob();
            return Float.NEGATIVE_INFINITY;
        }
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
