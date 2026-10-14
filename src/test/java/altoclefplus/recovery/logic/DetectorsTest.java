package altoclefplus.recovery.logic;

import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DetectorsTest {
    private final PlusConfig.Recovery cfg = new PlusConfig().recovery;

    private static Snapshot.Builder at(long tick, double x, double y, double z) {
        return Snapshot.builder().tick(tick).pos(x, y, z).userTaskRunning(true)
                .taskChain(List.of("<Beat Minecraft> x", "<Collect iron> y"));
    }

    /** Feeds one snapshot per second for {@code seconds}; returns true if the detector fired. */
    private static boolean feed(Detector d, int seconds, java.util.function.IntFunction<Snapshot> frame) {
        boolean fired = false;
        for (int i = 0; i <= seconds; i++) fired |= d.update(frame.apply(i)).isPresent();
        return fired;
    }

    @Test
    void noProgressFiresWhenStandingStill() {
        Detector d = new NoProgressDetector(cfg);
        assertTrue(feed(d, 31, i -> at(i * 20L, 0, 64, 0).build()));
    }

    @Test
    void noProgressQuietWhileMovingOrCollecting() {
        assertFalse(feed(new NoProgressDetector(cfg), 60, i -> at(i * 20L, i * 0.5, 64, 0).build()));
        assertFalse(feed(new NoProgressDetector(cfg), 60, i -> at(i * 20L, 0, 64, 0).item("cobblestone", i / 10 + 1).build()));
    }

    @Test
    void suppressedDuringLongWaitsAndOpenContainers() {
        assertFalse(feed(new NoProgressDetector(cfg), 60, i -> at(i * 20L, 0, 64, 0).containerOpen(true).build()));
        assertFalse(feed(new NoProgressDetector(cfg), 60,
                i -> at(i * 20L, 0, 64, 0).leafTask("SmeltInFurnaceTask").build()));
        assertFalse(feed(new NoProgressDetector(cfg), 60, i -> at(i * 20L, 0, 64, 0).userTaskRunning(false).build()));
    }

    @Test
    void pillarFiresOnLongClimbInPlace() {
        assertTrue(feed(new PillarDetector(cfg), 40, i -> at(i * 20L, 0.2, 64 + i, 0.1).build()));
    }

    @Test
    void pillarQuietWhenGoalIsStraightUp() {
        assertFalse(feed(new PillarDetector(cfg), 40, i -> at(i * 20L, 0.2, 64 + i, 0.1).goalVertical(true).build()));
    }

    @Test
    void pillarQuietWhenWalkingUpAMountain() {
        assertFalse(feed(new PillarDetector(cfg), 40, i -> at(i * 20L, i * 1.5, 64 + i, 0).build()));
    }

    @Test
    void pillarFiresAtNetherCeiling() {
        assertTrue(new PillarDetector(cfg).update(at(0, 0, 123, 0).dimension("the_nether").build()).isPresent());
    }

    @Test
    void pillarFiresOnUpDownElevator() {
        assertTrue(feed(new PillarDetector(cfg), 59, i -> at(i * 20L, 0, 64 + (i % 20 < 10 ? i % 10 : 10 - i % 10) * 1.5, 0).build()));
    }

    @Test
    void sameBlockFiresOnRepeatedStarts() {
        Detector d = new SameBlockDetector(cfg);
        boolean fired = false;
        for (int i = 0; i < 40; i++) {
            long pos = (i % 2 == 0) ? 12345L : Snapshot.NO_POS;
            fired |= d.update(at(i * 20L, 0, 64, 0).breakingPos(pos).build()).isPresent();
        }
        assertTrue(fired);
    }

    @Test
    void sameBlockFiresWhenBreakingForTooLong() {
        assertTrue(feed(new SameBlockDetector(cfg), 25, i -> at(i * 20L, 0, 64, 0).breakingPos(42L).build()));
    }

    @Test
    void sameBlockQuietWhenMiningDifferentBlocks() {
        assertFalse(feed(new SameBlockDetector(cfg), 60, i -> at(i * 20L, 0, 64, 0).breakingPos(i).build()));
    }

    @Test
    void flipFlopFiresWhenPacingWhileRepathing() {
        assertTrue(feed(new FlipFlopDetector(cfg), 50,
                i -> at(i * 20L, (i % 2) * 3, 64, 0).pathing(true).pathCounter(i / 5).build()));
    }

    @Test
    void flipFlopQuietWhenTravelling() {
        assertFalse(feed(new FlipFlopDetector(cfg), 50,
                i -> at(i * 20L, i * 4, 64, 0).pathing(true).pathCounter(i / 5).build()));
    }

    @Test
    void restartLoopFiresWhenSameSubtaskKeepsRestarting() {
        Detector d = new RestartLoopDetector(cfg);
        boolean fired = false;
        for (int i = 0; i < 10; i++) {
            String sub = i % 2 == 0 ? "<Get iron_pickaxe> mining" : "<Wander> looking";
            fired |= d.update(at(i * 200L, 0, 64, 0).taskChain(List.of("<Beat Minecraft> a", sub)).build()).isPresent();
        }
        assertTrue(fired);
    }

    @Test
    void restartLoopIgnoresDebugStateChanges() {
        Detector d = new RestartLoopDetector(cfg);
        boolean fired = false;
        for (int i = 0; i < 10; i++) {
            fired |= d.update(at(i * 200L, 0, 64, 0)
                    .taskChain(List.of("<Beat Minecraft> a", "<Get iron_pickaxe> step " + i)).build()).isPresent();
        }
        assertFalse(fired);
    }

    @Test
    void dimensionPingPongFiresAfterRoundTrips() {
        Detector d = new DimensionPingPongDetector(cfg);
        String[] dims = {"overworld", "the_nether", "overworld", "the_nether", "overworld"};
        Optional<String> last = Optional.empty();
        for (int i = 0; i < dims.length; i++) last = d.update(at(i * 1200L, 0, 64, 0).dimension(dims[i]).build());
        assertTrue(last.isPresent());
    }

    @Test
    void singleNetherTripIsFine() {
        Detector d = new DimensionPingPongDetector(cfg);
        String[] dims = {"overworld", "the_nether", "overworld"};
        boolean fired = false;
        for (int i = 0; i < dims.length; i++) fired |= d.update(at(i * 1200L, 0, 64, 0).dimension(dims[i]).build()).isPresent();
        assertFalse(fired);
    }
}
