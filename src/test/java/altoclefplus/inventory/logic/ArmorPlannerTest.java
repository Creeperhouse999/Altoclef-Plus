package altoclefplus.inventory.logic;

import altoclefplus.core.logic.Snapshot;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ArmorPlannerTest {
    private static Snapshot.Builder ironKitDone() {
        return Snapshot.builder().item("iron_pickaxe", 1).item("iron_sword", 1).item("shield", 1).item("water_bucket", 1);
    }

    @Test
    void craftsChestplateFirstWhenEnoughIron() {
        Snapshot s = ironKitDone().item("iron_ingot", 8).build();
        assertEquals(Optional.of("iron_chestplate"), ArmorPlanner.nextPiece(s));
    }

    @Test
    void craftsCheapestMissingPieceThatFitsLeftovers() {
        Snapshot s = ironKitDone().item("iron_ingot", 5).build();
        assertEquals(Optional.of("iron_helmet"), ArmorPlanner.nextPiece(s));
    }

    @Test
    void reservesIronForPickaxeSwordShieldAndBucket() {
        Snapshot s = Snapshot.builder().item("stone_pickaxe", 1).item("iron_ingot", 12).build();
        // 3 pickaxe + 2 sword + 1 shield + 3 bucket = 9 reserved, 3 left: not enough for any piece but boots (4)
        assertEquals(Optional.empty(), ArmorPlanner.nextPiece(s));
        Snapshot more = Snapshot.builder().item("stone_pickaxe", 1).item("iron_ingot", 13).build();
        assertEquals(Optional.of("iron_boots"), ArmorPlanner.nextPiece(more));
    }

    @Test
    void reservesDiamondsForPickaxeAndSword() {
        Snapshot s = ironKitDone().armor("chest", "iron_chestplate").armor("legs", "iron_leggings")
                .armor("head", "iron_helmet").armor("feet", "iron_boots").item("diamond", 9).build();
        // 3 pickaxe + 2 sword reserved, 4 left -> diamond boots
        assertEquals(Optional.of("diamond_boots"), ArmorPlanner.nextPiece(s));
    }

    @Test
    void doesNotDowngradeOrDuplicate() {
        Snapshot s = ironKitDone().armor("chest", "diamond_chestplate").armor("legs", "iron_leggings")
                .armor("head", "iron_helmet").armor("feet", "iron_boots").item("iron_ingot", 30).build();
        assertEquals(Optional.empty(), ArmorPlanner.nextPiece(s));
    }

    @Test
    void carriedPieceCountsAsOwned() {
        Snapshot s = ironKitDone().item("iron_chestplate", 1).item("iron_ingot", 8).build();
        assertNotEquals(Optional.of("iron_chestplate"), ArmorPlanner.nextPiece(s));
    }

    @Test
    void goldenBootsOnlyInTheNetherOrRightBeforeIt() {
        assertEquals("diamond_boots", ArmorPlanner.bestToWear("feet", "golden_boots", java.util.List.of("diamond_boots"), "overworld", false),
                "outside the Nether the better boots go back on");
        assertEquals("golden_boots", ArmorPlanner.bestToWear("feet", "diamond_boots", java.util.List.of("golden_boots"), "overworld", true),
                "next to a portal: put them on before going through");
        assertEquals("", ArmorPlanner.bestToWear("feet", "golden_boots", java.util.List.of("diamond_boots"), "the_nether", false), "keep golden boots on");
        assertEquals("golden_boots", ArmorPlanner.bestToWear("feet", "iron_boots", java.util.List.of("golden_boots"), "the_nether", false));
        assertEquals("diamond_chestplate", ArmorPlanner.bestToWear("chest", "iron_chestplate", java.util.List.of("diamond_chestplate"), "the_nether", false));
        assertEquals("", ArmorPlanner.bestToWear("head", "diamond_helmet", java.util.List.of("iron_helmet"), "overworld", false));
    }

    @Test
    void goldenBootsAreThrownAwayOnceTheNetherPartIsDone() {
        assertTrue(ArmorPlanner.dropGoldenBoots("overworld", 3, "diamond_boots", true), "back with ender eyes");
        assertFalse(ArmorPlanner.dropGoldenBoots("the_nether", 3, "diamond_boots", true), "still in the Nether");
        assertFalse(ArmorPlanner.dropGoldenBoots("overworld", 0, "diamond_boots", true), "may still need the Nether");
        assertFalse(ArmorPlanner.dropGoldenBoots("overworld", 3, "golden_boots", true), "not wearing other boots yet");
        assertFalse(ArmorPlanner.dropGoldenBoots("overworld", 3, "diamond_boots", false));
    }
}
