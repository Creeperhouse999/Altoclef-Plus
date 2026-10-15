package altoclefplus.build;

import altoclefplus.Plus;
import altoclefplus.build.logic.BuildJob;
import altoclefplus.build.logic.BuildQueue;
import altoclefplus.build.logic.Placement;
import altoclefplus.build.logic.Schematic;
import altoclefplus.ui.logic.BotState;
import altoclefplus.zones.Zones;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;

/**
 * Glue between the Build tab, the saved build queue and the running build: starts jobs through the
 * "plusbuild" command, keeps their state in the config (so paused builds survive a restart), starts the next
 * queued build when the bot is free, and holds the preview the overlay draws.
 */
public final class Builds {
    private static BuildQueue queue;
    private static List<BuildJob> queueBackedBy;
    private static long ticks;

    /** What the Build tab is previewing (null = nothing). */
    public static Schematic previewSchematic;
    public static Placement previewPlacement;
    public static boolean previewOn = true;

    private Builds() {}

    public static BuildQueue queue() {
        List<BuildJob> saved = Plus.config().builds;
        if (queue == null || queueBackedBy != saved) {
            queue = new BuildQueue(saved);
            queueBackedBy = saved;
        }
        return queue;
    }

    public static String world() {
        return Zones.worldKey();
    }

    public static BuildJob find(String id) {
        for (BuildJob j : queue().all()) if (String.valueOf(j.id).equals(id)) return j;
        return null;
    }

    /** Our rules for every block the builder wants to change. */
    static boolean mayTouch(BlockPos pos) {
        return Zones.mayBreak(pos) && Zones.mayPlace(pos);
    }

    /** Add a job; it starts right away if the bot is free, otherwise it waits in the queue. */
    public static void add(BuildJob job) {
        queue().add(job);
        save();
        Plus.log().add("build", "Queued " + job.name);
        tryStartNext();
    }

    public static void resume(BuildJob job) {
        queue().resume(job);
        save();
        tryStartNext();
    }

    public static void pause(BuildJob job) {
        if (job.state == BuildJob.State.RUNNING) Plus.control().togglePause(); // stops the task; onEnded marks it paused
        queue().pause(job);
        save();
    }

    /** Stop for good (the Build tab asks for confirmation first). */
    public static void stop(BuildJob job) {
        if (job.state == BuildJob.State.RUNNING) Plus.control().stop();
        queue().stop(job);
        save();
        Plus.log().add("build", "Stopped and forgot " + job.name);
    }

    /** Approve: build the rest without the blocks survival can't place / the item it can't get. */
    public static void approve(BuildJob job) {
        job.approvedSkipImpossible = true;
        for (String b : job.pendingSkip) if (!job.skipBlocks.contains(b)) job.skipBlocks.add(b);
        job.pendingSkip.clear();
        resume(job);
    }

    static void onStarted(BuildJob job) {
        queue().start(job);
        save();
    }

    static void onEnded(BuildTask task) {
        BuildJob job = task.job();
        switch (task.outcome()) {
            case DONE -> {
                queue().finish(job);
                Plus.log().add("build", "Finished " + job.name);
                if (Plus.ui() != null) Plus.ui().alert("Build finished: " + job.name);
            }
            case WAITING_APPROVAL, FAILED -> {
                queue().needsApproval(job, task.why());
                Plus.log().add("build", job.name + ": " + task.why());
                if (Plus.ui() != null) Plus.ui().alert("Build waits for you: " + job.name + " (see Build tab)");
            }
            default -> queue().pause(job); // paused or stopped from the keys/panel: keep it
        }
        save();
    }

    /** Once per client tick: start the next queued build when the bot is free. */
    public static void tick() {
        if (++ticks % 40 == 0) tryStartNext();
    }

    private static void tryStartNext() {
        if (Plus.mod() == null || Plus.control() == null) return;
        if (Plus.control().state() != BotState.IDLE || Plus.mod().getUserTaskChain().isActive()) return;
        Optional<BuildJob> next = queue().next(world());
        next.ifPresent(j -> Plus.control().start("plusbuild " + j.id));
    }

    public static Optional<BuildJob> running() {
        return queue().running(world());
    }

    /** HUD text while a build runs, e.g. "Build house (120/240)". */
    public static String hudLine() {
        return running().map(j -> "Build " + j.name + (j.total > 0 ? " (" + j.placed + "/" + j.total + ")" : "")).orElse("");
    }

    private static void save() {
        Plus.saveConfig();
    }
}
