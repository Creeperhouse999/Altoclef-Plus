package altoclefplus.build.logic;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static altoclefplus.build.logic.BuildJob.State.*;
import static org.junit.jupiter.api.Assertions.*;

class BuildQueueTest {
    static BuildJob job(String world, String name) {
        return BuildJob.create(world, "builtin:" + name + ".litematic", name, new Placement(0, 64, 0, 0), true);
    }

    @Test
    void queueRunsOneAfterAnother() {
        BuildQueue q = new BuildQueue(new ArrayList<>());
        BuildJob a = q.add(job("w", "hut"));
        BuildJob b = q.add(job("w", "wall"));
        assertEquals(a, q.next("w").orElseThrow(), "first in, first built");
        q.start(a);
        assertEquals(RUNNING, a.state);
        assertTrue(q.next("w").isEmpty(), "one build at a time");
        q.finish(a);
        assertEquals(DONE, a.state);
        assertEquals(b, q.next("w").orElseThrow());
    }

    @Test
    void pauseKeepsTheJobAndResumeContinues() {
        BuildQueue q = new BuildQueue(new ArrayList<>());
        BuildJob a = q.add(job("w", "hut"));
        q.start(a);
        a.placed = 40;
        q.pause(a);
        assertEquals(PAUSED, a.state);
        assertEquals(List.of(a), q.paused("w"));
        assertTrue(q.next("w").isEmpty(), "paused builds wait for Resume");
        q.resume(a);
        assertEquals(QUEUED, a.state);
        assertEquals(40, a.placed, "progress kept");
        assertEquals(a, q.next("w").orElseThrow());
    }

    @Test
    void stopForgetsAndMissingWaitsForApproval() {
        BuildQueue q = new BuildQueue(new ArrayList<>());
        BuildJob a = q.add(job("w", "hut"));
        q.start(a);
        q.needsApproval(a, "Can't place in survival: spawner x1");
        assertEquals(WAITING_APPROVAL, a.state);
        assertEquals("Can't place in survival: spawner x1", a.note);
        q.stop(a);
        assertTrue(q.all().isEmpty());
    }

    @Test
    void buildsBelongToTheirWorld() {
        BuildQueue q = new BuildQueue(new ArrayList<>());
        q.add(job("a", "hut"));
        assertTrue(q.next("b").isEmpty());
        assertEquals(1, q.forWorld("a").size());
    }

    @Test
    void aRunningJobAfterRestartComesBackPaused() {
        List<BuildJob> saved = new ArrayList<>();
        BuildJob a = job("w", "hut");
        a.state = RUNNING;
        saved.add(a);
        new BuildQueue(saved);
        assertEquals(PAUSED, a.state, "the game was closed mid-build: show it as paused, ready to resume");
    }
}
