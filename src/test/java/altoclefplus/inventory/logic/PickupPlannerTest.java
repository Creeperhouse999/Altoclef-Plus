package altoclefplus.inventory.logic;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PickupPlannerTest {
    static PickupPlanner.Drop drop(int id, String item, double dist, boolean settled) {
        return new PickupPlanner.Drop(id, item, dist, settled);
    }

    @Test
    void picksTheNearestWantedSettledDrop() {
        PickupPlanner p = new PickupPlanner(10, 200);
        List<PickupPlanner.Drop> drops = List.of(drop(1, "oak_log", 6, true), drop(2, "oak_log", 3, true),
                drop(3, "dirt", 1, true), drop(4, "oak_log", 2, false));
        assertEquals(Optional.of(2), p.choose(drops, Set.of("oak_log"), 0), "nearest wanted one that has landed");
    }

    @Test
    void ignoresFarDropsAndGivesUpOnStuckOnes() {
        PickupPlanner p = new PickupPlanner(10, 200);
        assertEquals(Optional.empty(), p.choose(List.of(drop(1, "oak_log", 14, true)), Set.of("oak_log"), 0));
        assertEquals(Optional.of(1), p.choose(List.of(drop(1, "oak_log", 4, true)), Set.of("oak_log"), 0));
        // Still not picked up after 200 ticks: give up on that one (e.g. stuck in leaves).
        assertEquals(Optional.empty(), p.choose(List.of(drop(1, "oak_log", 4, true)), Set.of("oak_log"), 201));
        assertEquals(Optional.of(2), p.choose(List.of(drop(1, "oak_log", 4, true), drop(2, "oak_log", 5, true)), Set.of("oak_log"), 202));
    }

    @Test
    void nothingWantedNothingToDo() {
        assertEquals(Optional.empty(), new PickupPlanner(10, 200).choose(List.of(drop(1, "dirt", 1, true)), Set.of(), 0));
    }
}
