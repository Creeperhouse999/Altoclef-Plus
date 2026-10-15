package altoclefplus.inventory.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VisibleInventoryPolicyTest {
    @Test
    void opensOnlyForBotClicksInItsOwnInventoryWithNothingElseOpen() {
        assertTrue(VisibleInventoryPolicy.shouldOpen(true, true, true, false));
        assertFalse(VisibleInventoryPolicy.shouldOpen(false, true, true, false), "setting off");
        assertFalse(VisibleInventoryPolicy.shouldOpen(true, false, true, false), "no task running");
        assertFalse(VisibleInventoryPolicy.shouldOpen(true, true, false, false), "container slot: its screen is already visible");
        assertFalse(VisibleInventoryPolicy.shouldOpen(true, true, true, true), "another screen is open");
    }

    @Test
    void closesAfterAQuietMomentWithAnEmptyCursor() {
        assertFalse(VisibleInventoryPolicy.shouldClose(5, true, true), "still clicking");
        assertFalse(VisibleInventoryPolicy.shouldClose(40, false, true), "item on the cursor: closing would drop/return it");
        assertTrue(VisibleInventoryPolicy.shouldClose(40, true, true));
        assertTrue(VisibleInventoryPolicy.shouldClose(1, true, false), "task ended: close right away");
    }
}
