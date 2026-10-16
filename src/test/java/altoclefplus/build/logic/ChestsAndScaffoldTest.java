package altoclefplus.build.logic;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChestsAndScaffoldTest {
    @Test
    void leavesAtLeastTheMinimumInEachChest() {
        assertEquals(36, ChestRules.takeable(100, 64, 200));
        assertEquals(0, ChestRules.takeable(50, 64, 200), "below a stack: take nothing");
        assertEquals(20, ChestRules.takeable(100, 64, 20), "never more than needed");
        assertEquals(100, ChestRules.takeable(100, 0, 500));
    }

    @Test
    void labelsFromFramesAndSignsPointToTheRightChest() {
        List<ChestLabels.Labeled> chests = List.of(
                new ChestLabels.Labeled(1, Set.of("minecraft:oak_log"), ""),
                new ChestLabels.Labeled(2, Set.of(), "Cobble / Stone"),
                new ChestLabels.Labeled(3, Set.of(), "Food"));
        assertEquals(Optional.of(1), ChestLabels.bestFor("minecraft:oak_log", chests), "frame shows the exact item");
        assertEquals(Optional.of(2), ChestLabels.bestFor("minecraft:cobblestone", chests), "sign word 'Cobble'");
        assertEquals(Optional.of(2), ChestLabels.bestFor("minecraft:stone", chests));
        assertEquals(Optional.empty(), ChestLabels.bestFor("minecraft:diamond", chests));
    }

    @Test
    void scaffoldingIsWhatAppearedThatTheSchematicDoesNotWant() {
        Set<Long> before = Set.of(Schematic.key(0, 64, 0));
        Set<Long> schematic = Set.of(Schematic.key(1, 64, 0));
        Set<Long> now = Set.of(Schematic.key(0, 64, 0), Schematic.key(1, 64, 0), Schematic.key(2, 65, 0));
        assertEquals(Set.of(Schematic.key(2, 65, 0)), ScaffoldTracker.toRemove(before, schematic, now));
    }

    @Test
    void pickupTargetIsCappedSoTheChestKeepsItsMinimum() {
        // Carrying 10, AltoClef wants 50 in total; the chest has 100 and must keep 64: may only reach 10 + 36.
        assertEquals(46, ChestRules.cappedTarget(10, 50, 100, 64));
        assertEquals(50, ChestRules.cappedTarget(10, 50, 200, 64), "plenty in the chest: AltoClef's own target");
        assertEquals(10, ChestRules.cappedTarget(10, 50, 64, 64), "nothing to spare");
    }

    @Test
    void chestRulesAreForStorageNotFurnaces() {
        assertTrue(ChestRules.appliesTo("CHEST"));
        assertTrue(ChestRules.appliesTo("SHULKER"));
        assertFalse(ChestRules.appliesTo("FURNACE"), "the bot's own smelting output must always be taken out");
        assertFalse(ChestRules.appliesTo("BREWING"));
    }
}
