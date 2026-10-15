package altoclefplus.inventory.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VisibleInventoryPolicyTest {
    @Test
    void opensOnlyForBotClicksInItsOwnInventoryWithNothingElseOpen() {
        assertTrue(VisibleInventoryPolicy.shouldOpen(true, true, true, false, true));
        assertFalse(VisibleInventoryPolicy.shouldOpen(false, true, true, false, true), "setting off");
        assertFalse(VisibleInventoryPolicy.shouldOpen(true, false, true, false, true), "no task running");
        assertFalse(VisibleInventoryPolicy.shouldOpen(true, true, false, false, true), "container slot: its screen is already visible");
        assertFalse(VisibleInventoryPolicy.shouldOpen(true, true, true, true, true), "another screen is open");
        assertFalse(VisibleInventoryPolicy.shouldOpen(true, true, true, false, false),
                "maintenance clicks (refresh, weapon swap) while mining or fighting: don't pop the screen open");
    }

    @Test
    void closesAfterAQuietMomentWithAnEmptyCursor() {
        assertFalse(VisibleInventoryPolicy.shouldClose(5, true, true), "still clicking");
        assertFalse(VisibleInventoryPolicy.shouldClose(40, false, true), "item on the cursor: closing would drop/return it");
        assertTrue(VisibleInventoryPolicy.shouldClose(40, true, true));
        assertTrue(VisibleInventoryPolicy.shouldClose(1, true, false), "task ended: close right away");
    }

    @Test
    void inventoryJobsAreCraftingSortingAndEquipping() {
        assertTrue(VisibleInventoryPolicy.isInventoryJob(java.util.List.of("<Get iron>", "<Crafting (w/ RECIPE): x>")));
        assertTrue(VisibleInventoryPolicy.isInventoryJob(java.util.List.of("<Equipping armor [iron_helmet]>")));
        assertFalse(VisibleInventoryPolicy.isInventoryJob(java.util.List.of("<Mine And Collect>", "<Destroy block at 1, 2, 3>")));
        assertFalse(VisibleInventoryPolicy.isInventoryJob(java.util.List.of("<Doing stuff in crafting_table x 1 container>", "<Destroy block at 1, 2, 3>")),
                "a crafting-table task that is really mining right now");
    }
}
