package altoclefplus.inventory.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TorchPlannerTest {
    @Test
    void placesInTheDarkAwayFromTheLastTorch() {
        TorchPlanner t = new TorchPlanner();
        assertTrue(t.shouldPlace(true, 2, 0, 0, 0, 0, true));
        t.placed(0, 0, 0);
        assertFalse(t.shouldPlace(true, 2, 0, 3, 0, 0, true), "only 3 blocks from the last one");
        assertTrue(t.shouldPlace(true, 2, 0, 8, 0, 0, true), "8 blocks on: next torch");
    }

    @Test
    void notWhenLitOffOrWithoutTorches() {
        TorchPlanner t = new TorchPlanner();
        assertFalse(t.shouldPlace(true, 10, 0, 0, 0, 0, true), "already lit by a torch");
        assertFalse(t.shouldPlace(true, 0, 12, 0, 0, 0, true), "daylight");
        assertFalse(t.shouldPlace(false, 0, 0, 0, 0, 0, true), "setting off");
        assertFalse(t.shouldPlace(true, 0, 0, 0, 0, 0, false), "no torches");
    }

    @Test
    void craftsFromCarriedCoalOnly() {
        assertTrue(TorchPlanner.shouldCraft(true, 4, 1, 1));
        assertFalse(TorchPlanner.shouldCraft(true, 20, 1, 1), "has enough");
        assertFalse(TorchPlanner.shouldCraft(true, 4, 0, 5), "no coal: no trip for it");
        assertFalse(TorchPlanner.shouldCraft(false, 0, 5, 5), "setting off");
    }
}
