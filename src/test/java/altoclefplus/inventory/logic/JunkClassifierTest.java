package altoclefplus.inventory.logic;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JunkClassifierTest {
    private static JunkClassifier classifier() {
        return new JunkClassifier(
                Set.of("diamond", "ender_pearl", "shield"),
                Set.of("gunpowder", "rotten_flesh", "bone"),
                Set.of("cobblestone", "dirt", "netherrack"),
                64, 128, 3);
    }

    @Test
    void nothingDroppedWhileThereIsRoom() {
        List<JunkClassifier.Drop> drops = classifier().toDrop(Map.of("gunpowder", 5), Set.of(), 10);
        assertTrue(drops.isEmpty());
    }

    @Test
    void dropsListedJunkWhenFull() {
        List<JunkClassifier.Drop> drops = classifier().toDrop(Map.of("gunpowder", 5, "diamond", 3), Set.of(), 2);
        assertEquals(List.of(new JunkClassifier.Drop("gunpowder", 5)), drops);
    }

    @Test
    void neverDropsProtectedUnknownToolsArmorOrNeededItems() {
        Map<String, Integer> inv = Map.of(
                "diamond", 3, "ender_pearl", 2, "string", 9, "iron_pickaxe", 1, "iron_helmet", 1, "bone", 4);
        List<JunkClassifier.Drop> drops = classifier().toDrop(inv, Set.of("bone"), 0);
        assertTrue(drops.isEmpty(), "got " + drops);
    }

    @Test
    void protectedWinsOverJunkList() {
        JunkClassifier c = new JunkClassifier(Set.of("gunpowder"), Set.of("gunpowder"), Set.of(), 64, 128, 3);
        assertTrue(c.toDrop(Map.of("gunpowder", 5), Set.of(), 0).isEmpty());
    }

    @Test
    void rottenFleshKeptWhenItIsTheOnlyFood() {
        assertTrue(classifier().toDrop(Map.of("rotten_flesh", 6), Set.of(), 0).isEmpty());
        assertEquals(List.of(new JunkClassifier.Drop("rotten_flesh", 6)),
                classifier().toDrop(Map.of("rotten_flesh", 6, "bread", 3), Set.of(), 0));
    }

    @Test
    void buildingBlocksOnlyAboveTheirCap() {
        List<JunkClassifier.Drop> drops = classifier().toDrop(Map.of("cobblestone", 100, "dirt", 30), Set.of(), 0);
        assertEquals(List.of(new JunkClassifier.Drop("cobblestone", 36)), drops);
    }

    @Test
    void buildingBlocksAboveTotalCapTrimLargestFirst() {
        List<JunkClassifier.Drop> drops = classifier().toDrop(
                Map.of("cobblestone", 64, "dirt", 64, "netherrack", 40), Set.of(), 0);
        int dropped = drops.stream().mapToInt(JunkClassifier.Drop::count).sum();
        assertEquals(40, dropped);
    }

    @Test
    void junkComesBeforeBuildingBlocks() {
        List<JunkClassifier.Drop> drops = classifier().toDrop(Map.of("cobblestone", 70, "bone", 2), Set.of(), 0);
        assertEquals("bone", drops.get(0).id());
    }

    @Test
    void stoneVariantsAndOresNobodyNeedsAreJunkWhenFull() {
        List<JunkClassifier.Drop> drops = classifier().toDrop(Map.of("calcite", 3, "raw_copper", 9, "amethyst_block", 3), Set.of(), 0);
        assertEquals(3, drops.size());
        assertTrue(classifier().toDrop(Map.of("raw_copper", 9), Set.of("raw_copper"), 0).isEmpty(), "needed by the task");
    }

    @Test
    void armorWorseThanWhatIsWornIsJunkExceptGoldenBoots() {
        Map<String, String> worn = Map.of("chest", "diamond_chestplate", "feet", "diamond_boots", "head", "diamond_helmet");
        List<JunkClassifier.Drop> drops = JunkClassifier.outdatedArmor(
                Map.of("iron_chestplate", 1, "iron_boots", 1, "golden_boots", 1, "iron_leggings", 1), worn, Set.of(), Set.of());
        assertEquals(List.of(new JunkClassifier.Drop("iron_boots", 1), new JunkClassifier.Drop("iron_chestplate", 1)), drops,
                "leggings: nothing better worn; golden boots: kept for the Nether");
    }

    @Test
    void keepsAtMostTwoOfTheBestTool() {
        List<JunkClassifier.Drop> drops = JunkClassifier.outdatedTools(Map.of("diamond_pickaxe", 3, "diamond_sword", 1), Set.of(), Set.of());
        assertEquals(List.of(new JunkClassifier.Drop("diamond_pickaxe", 1)), drops);
    }
}
