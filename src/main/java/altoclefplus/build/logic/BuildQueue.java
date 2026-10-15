package altoclefplus.build.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static altoclefplus.build.logic.BuildJob.State.*;

/**
 * The build queue, backed by the saved list in the config. One build runs at a time per world; Pause keeps
 * a build (and its progress) until Resume; Stop removes it for good; a build that needs your approval
 * (missing or impossible blocks) waits until you decide.
 */
public final class BuildQueue {
    private final List<BuildJob> jobs;

    public BuildQueue(List<BuildJob> saved) {
        this.jobs = saved;
        // The game was closed while a build ran: show it as paused, ready to resume.
        for (BuildJob j : jobs) if (j.state == RUNNING) j.state = PAUSED;
    }

    public BuildJob add(BuildJob j) {
        j.state = QUEUED;
        jobs.add(j);
        return j;
    }

    /** The next build to start in this world, if none is running. */
    public Optional<BuildJob> next(String world) {
        for (BuildJob j : jobs) if (j.world.equals(world) && (j.state == RUNNING || j.state == WAITING_APPROVAL)) return Optional.empty();
        for (BuildJob j : jobs) if (j.world.equals(world) && j.state == QUEUED) return Optional.of(j);
        return Optional.empty();
    }

    public Optional<BuildJob> running(String world) {
        for (BuildJob j : jobs) if (j.world.equals(world) && j.state == RUNNING) return Optional.of(j);
        return Optional.empty();
    }

    public void start(BuildJob j) {
        j.state = RUNNING;
        j.note = "";
    }

    public void pause(BuildJob j) {
        if (j.state != DONE) j.state = PAUSED;
    }

    public void resume(BuildJob j) {
        if (j.state == PAUSED || j.state == WAITING_APPROVAL) j.state = QUEUED;
    }

    public void needsApproval(BuildJob j, String why) {
        j.state = WAITING_APPROVAL;
        j.note = why;
    }

    public void finish(BuildJob j) {
        j.state = DONE;
        j.note = "";
    }

    /** Stop: forget the build completely (the panel asks for confirmation first). */
    public void stop(BuildJob j) {
        jobs.remove(j);
    }

    public List<BuildJob> paused(String world) {
        List<BuildJob> out = new ArrayList<>();
        for (BuildJob j : jobs) if (j.world.equals(world) && j.state == PAUSED) out.add(j);
        return out;
    }

    public List<BuildJob> forWorld(String world) {
        List<BuildJob> out = new ArrayList<>();
        for (BuildJob j : jobs) if (j.world.equals(world)) out.add(j);
        return out;
    }

    public List<BuildJob> all() {
        return jobs;
    }
}
