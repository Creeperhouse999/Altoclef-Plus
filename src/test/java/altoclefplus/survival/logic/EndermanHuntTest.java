package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static altoclefplus.survival.logic.EndermanHuntPlanner.Phase.*;
import static org.junit.jupiter.api.Assertions.*;

class EndermanHuntTest {
    private static EndermanHuntPlanner.State base() {
        return new EndermanHuntPlanner.State(0, 12, true, true, true, true, true, false, false, 0, false);
    }

    @Test
    void doneWhenEnoughPearls() {
        assertEquals(DONE, EndermanHuntPlanner.next(base().withPearls(12)));
    }

    @Test
    void picksUpDroppedPearlsFirst() {
        assertEquals(COLLECT, EndermanHuntPlanner.next(base().withPearlDropped(true)));
    }

    @Test
    void goesToTheNetherFirst() {
        assertEquals(GO_TO_NETHER, EndermanHuntPlanner.next(base().withInNether(false)));
    }

    @Test
    void findsAWarpedForestWhenNoEndermanAround() {
        assertEquals(FIND_FOREST, EndermanHuntPlanner.next(base().withInForest(false).withEndermanNearby(false)));
    }

    @Test
    void placesBoatThenAggrosThenFights() {
        assertEquals(PLACE_BOAT, EndermanHuntPlanner.next(base().withBoatPlaced(false)));
        assertEquals(AGGRO, EndermanHuntPlanner.next(base()));
        assertEquals(WAIT, EndermanHuntPlanner.next(base().withAngry(true, 5)));
        assertEquals(FIGHT, EndermanHuntPlanner.next(base().withSeated(true)));
    }

    @Test
    void fightsNormallyIfTheTrapDoesNotWork() {
        assertEquals(FIGHT, EndermanHuntPlanner.next(base().withAngry(true, 21)));
    }

    @Test
    void fightsWithoutTrapWhenNoBoat() {
        assertEquals(FIGHT, EndermanHuntPlanner.next(base().withBoatItem(false).withBoatPlaced(false)));
        assertEquals(FIND_FOREST, EndermanHuntPlanner.next(base().withBoatItem(false).withBoatPlaced(false)
                .withEndermanNearby(false).withInForest(false)));
    }

    @Test
    void critsOnlyWhileFallingWithFullCooldownInReach() {
        assertTrue(CritTiming.shouldAttack(false, -0.2, 1.0f, 2.5, 3.0));
        assertFalse(CritTiming.shouldAttack(true, 0, 1.0f, 2.5, 3.0), "on the ground");
        assertFalse(CritTiming.shouldAttack(false, 0.3, 1.0f, 2.5, 3.0), "still rising");
        assertFalse(CritTiming.shouldAttack(false, -0.2, 0.5f, 2.5, 3.0), "cooldown not ready");
        assertFalse(CritTiming.shouldAttack(false, -0.2, 1.0f, 3.5, 3.0), "out of reach");
        assertTrue(CritTiming.shouldJump(true, 0.95f, 2.5, 3.0));
        assertFalse(CritTiming.shouldJump(true, 0.5f, 2.5, 3.0));
    }

    @Test
    void prefersHigherDamageWeapon() {
        assertEquals("stone_axe", CritTiming.bestWeapon(Map.of("iron_sword", 1, "stone_axe", 1)));
        assertEquals("diamond_sword", CritTiming.bestWeapon(Map.of("diamond_sword", 1, "wooden_axe", 1)));
        assertEquals("", CritTiming.bestWeapon(Map.of("dirt", 3)));
    }
}
