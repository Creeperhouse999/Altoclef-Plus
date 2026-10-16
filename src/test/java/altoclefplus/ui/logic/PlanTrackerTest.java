package altoclefplus.ui.logic;

import altoclefplus.core.logic.Snapshot;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlanTrackerTest {
    @Test
    void startsWithWoodAsCurrent() {
        PlanTracker t = new PlanTracker(12);
        t.update(Snapshot.builder().build());

        assertEquals(Milestone.WOOD, t.current());
        assertEquals(PlanTracker.Status.CURRENT, t.statuses().get(0).status());
        assertEquals(PlanTracker.Status.TODO, t.statuses().get(1).status());
    }

    @Test
    void milestonesTickInOrderFromInventory() {
        PlanTracker t = new PlanTracker(12);
        t.update(Snapshot.builder().item("oak_log", 3).item("crafting_table", 1).item("wooden_pickaxe", 1).build());

        assertTrue(t.isDone(Milestone.WOOD));
        assertTrue(t.isDone(Milestone.CRAFTING_TABLE));
        assertTrue(t.isDone(Milestone.WOODEN_PICKAXE));
        assertEquals(Milestone.STONE_TOOLS, t.current());
    }

    @Test
    void laterGearImpliesEarlierToolMilestones() {
        PlanTracker t = new PlanTracker(12);
        t.update(Snapshot.builder().item("iron_pickaxe", 1).item("stone_axe", 1).item("stone_shovel", 1).build());

        assertTrue(t.isDone(Milestone.WOODEN_PICKAXE));
        assertTrue(t.isDone(Milestone.STONE_TOOLS));
        assertTrue(t.isDone(Milestone.IRON_INGOTS));
    }

    @Test
    void ticksAreMonotonicAfterItemsAreLost() {
        PlanTracker t = new PlanTracker(12);
        t.update(Snapshot.builder().item("oak_log", 3).build());
        t.update(Snapshot.builder().build());

        assertTrue(t.isDone(Milestone.WOOD));
    }

    @Test
    void resetClearsTicks() {
        PlanTracker t = new PlanTracker(12);
        t.update(Snapshot.builder().item("oak_log", 3).build());
        t.reset();
        t.update(Snapshot.builder().build());

        assertFalse(t.isDone(Milestone.WOOD));
    }

    @Test
    void fullIronArmorNeedsAllFourSlots() {
        PlanTracker t = new PlanTracker(12);
        Snapshot.Builder b = Snapshot.builder().armor("head", "iron_helmet").armor("chest", "iron_chestplate")
                .item("iron_leggings", 1);
        t.update(b.build());
        assertFalse(t.isDone(Milestone.IRON_ARMOR));

        t.update(b.armor("feet", "diamond_boots").build());
        assertTrue(t.isDone(Milestone.IRON_ARMOR));
    }

    @Test
    void ironKitNeedsSwordShieldAndWaterBucket() {
        PlanTracker t = new PlanTracker(12);
        Snapshot.Builder b = Snapshot.builder().item("iron_pickaxe", 1).item("iron_sword", 1).item("water_bucket", 1);
        t.update(b.build());
        assertFalse(t.isDone(Milestone.IRON_KIT));

        t.update(b.offhand("shield").item("shield", 1).build());
        assertTrue(t.isDone(Milestone.IRON_KIT));
    }

    @Test
    void enteringNetherAndEndAreRemembered() {
        PlanTracker t = new PlanTracker(12);
        t.update(Snapshot.builder().dimension("the_nether").build());
        t.update(Snapshot.builder().dimension("the_end").build());

        assertTrue(t.isDone(Milestone.NETHER));
        assertTrue(t.isDone(Milestone.END));
        assertTrue(t.isDone(Milestone.STRONGHOLD));
    }

    @Test
    void eyeCountsUseTargetAndPartialProgress() {
        PlanTracker t = new PlanTracker(12);
        t.update(Snapshot.builder().item("ender_eye", 4).item("ender_pearl", 8).item("blaze_rod", 4).build());

        assertTrue(t.isDone(Milestone.ENDER_PEARLS));
        assertTrue(t.isDone(Milestone.BLAZE_RODS));
        assertFalse(t.isDone(Milestone.EYES_OF_ENDER));
    }

    @Test
    void strongholdFromBeatMinecraftDebugState() {
        PlanTracker t = new PlanTracker(12);
        t.update(Snapshot.builder().taskChain(List.of("<Beat Minecraft> Opening End Portal")).build());

        assertTrue(t.isDone(Milestone.STRONGHOLD));
    }

    @Test
    void listenerFiresOncePerMilestone() {
        List<Milestone> got = new ArrayList<>();
        PlanTracker t = new PlanTracker(12);
        t.setListener(got::add);

        t.update(Snapshot.builder().item("oak_log", 1).build());
        t.update(Snapshot.builder().item("oak_log", 2).build());

        assertEquals(List.of(Milestone.WOOD), got);
    }

    @Test
    void ironKitSkipsTheBucketWhenThatGateIsOff() {
        PlanTracker t = new PlanTracker(12);
        t.setRequireWaterBucket(false);
        t.update(Snapshot.builder().item("iron_pickaxe", 1).item("iron_sword", 1).offhand("shield").build());
        assertTrue(t.isDone(Milestone.IRON_KIT));
    }

    @Test
    void bedsAreDoneOnlyWithAsManyAsTheGamerTaskCollects() {
        PlanTracker t = new PlanTracker(12, 11);
        t.update(Snapshot.builder().item("white_bed", 4).build());
        assertFalse(t.isDone(Milestone.BEDS), "4 of 11: @gamer is still collecting beds");
        t.update(Snapshot.builder().item("white_bed", 11).build());
        assertTrue(t.isDone(Milestone.BEDS));
        assertEquals(11, t.targetBeds());
    }
}
