package altoclefplus.inventory.logic;

import altoclefplus.core.logic.Snapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoorPlannerTest {
    @Test
    void craftsADoorOnlyFromWoodAlreadyCarried() {
        assertTrue(DoorPlanner.shouldCraft(Snapshot.builder().item("stone_pickaxe", 1).item("oak_log", 3).build()));
        assertFalse(DoorPlanner.shouldCraft(Snapshot.builder().item("stone_pickaxe", 1).item("oak_planks", 4).build()),
                "not enough wood: no wood trip just for a door");
        assertFalse(DoorPlanner.shouldCraft(Snapshot.builder().item("oak_log", 8).build()), "too early: no pickaxe yet");
        assertFalse(DoorPlanner.shouldCraft(Snapshot.builder().item("stone_pickaxe", 1).item("oak_log", 8)
                .item("spruce_door", 1).build()), "already has one");
    }
}
