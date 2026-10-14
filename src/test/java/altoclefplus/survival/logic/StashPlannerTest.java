package altoclefplus.survival.logic;

import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class StashPlannerTest {
    private final PlusConfig.Survival cfg = new PlusConfig().survival;

    private static Snapshot headingToNether(Snapshot.Builder b) {
        return b.taskChain(List.of("<Beat Minecraft> Going to Nether")).build();
    }

    @Test
    void placesBedByThePortalFirst() {
        StashPlanner p = new StashPlanner();
        Snapshot s = headingToNether(Snapshot.builder().item("white_bed", 1));
        assertEquals(Optional.of(StashPlanner.Job.BED_AT_PORTAL), p.next(s, cfg, true));
        assertEquals(Optional.empty(), p.next(s, cfg, false), "only once a portal is close");
    }

    @Test
    void eachJobRunsOnce() {
        StashPlanner p = new StashPlanner();
        Snapshot s = headingToNether(Snapshot.builder().item("white_bed", 1));
        p.markDone(StashPlanner.Job.BED_AT_PORTAL);
        assertEquals(Optional.empty(), p.next(s, cfg, true));
        p.reset();
        assertEquals(Optional.of(StashPlanner.Job.BED_AT_PORTAL), p.next(s, cfg, true));
    }

    @Test
    void optionalStashesFollowSettings() {
        StashPlanner p = new StashPlanner();
        Snapshot s = headingToNether(Snapshot.builder().item("diamond", 5));
        assertEquals(Optional.empty(), p.next(s, cfg, true), "off by default");
        cfg.stashValuablesBeforeNether = true;
        assertEquals(Optional.of(StashPlanner.Job.VALUABLES), p.next(s, cfg, true));
        p.markDone(StashPlanner.Job.VALUABLES);
        cfg.backupGearBeforeNether = true;
        assertEquals(Optional.of(StashPlanner.Job.BACKUP_BEFORE_NETHER), p.next(s, cfg, true));
    }

    @Test
    void valuablesOnlyWhenThereIsSomethingToStore() {
        StashPlanner p = new StashPlanner();
        cfg.stashValuablesBeforeNether = true;
        assertEquals(Optional.empty(), p.next(headingToNether(Snapshot.builder().item("cobblestone", 5)), cfg, true));
    }

    @Test
    void backupBeforeTheEnd() {
        StashPlanner p = new StashPlanner();
        cfg.backupGearBeforeEnd = true;
        Snapshot s = Snapshot.builder().taskChain(List.of("<Beat Minecraft> Opening End Portal")).build();
        assertEquals(Optional.of(StashPlanner.Job.BACKUP_BEFORE_END), p.next(s, cfg, false));
    }

    @Test
    void quietOtherwise() {
        StashPlanner p = new StashPlanner();
        cfg.backupGearBeforeEnd = true;
        cfg.backupGearBeforeNether = true;
        assertEquals(Optional.empty(), p.next(Snapshot.builder().item("white_bed", 2).build(), cfg, true));
    }
}
