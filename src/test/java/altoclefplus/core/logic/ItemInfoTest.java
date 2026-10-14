package altoclefplus.core.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemInfoTest {
    @Test
    void toolTypeAndTier() {
        assertEquals("pickaxe", ItemInfo.toolType("stone_pickaxe"));
        assertEquals("axe", ItemInfo.toolType("iron_axe"));
        assertEquals("shovel", ItemInfo.toolType("diamond_shovel"));
        assertEquals("sword", ItemInfo.toolType("wooden_sword"));
        assertEquals("", ItemInfo.toolType("pickaxe_banner_pattern"));
        assertEquals("", ItemInfo.toolType("iron_ingot"));
        assertEquals(ItemInfo.STONE, ItemInfo.tier("stone_pickaxe"));
        assertEquals(ItemInfo.IRON, ItemInfo.tier("iron_chestplate"));
        assertEquals(ItemInfo.DIAMOND, ItemInfo.tier("diamond_sword"));
        assertEquals(ItemInfo.NONE, ItemInfo.tier("dirt"));
    }

    @Test
    void goldIsWeakerThanStoneForTools() {
        assertTrue(ItemInfo.tier("golden_pickaxe") < ItemInfo.tier("stone_pickaxe"));
    }

    @Test
    void armorSlots() {
        assertEquals("head", ItemInfo.armorSlot("iron_helmet"));
        assertEquals("chest", ItemInfo.armorSlot("diamond_chestplate"));
        assertEquals("legs", ItemInfo.armorSlot("leather_leggings"));
        assertEquals("feet", ItemInfo.armorSlot("golden_boots"));
        assertEquals("head", ItemInfo.armorSlot("turtle_helmet"));
        assertEquals("", ItemInfo.armorSlot("iron_ingot"));
    }

    @Test
    void armorPoints() {
        assertTrue(ItemInfo.armorPoints("iron_chestplate") > ItemInfo.armorPoints("iron_helmet"));
        assertTrue(ItemInfo.armorPoints("diamond_boots") > ItemInfo.armorPoints("iron_boots"));
        assertTrue(ItemInfo.armorPoints("golden_chestplate") > ItemInfo.armorPoints("leather_chestplate"));
        assertEquals(0, ItemInfo.armorPoints("dirt"));
    }

    @Test
    void bestToolTierOfType() {
        Snapshot s = Snapshot.builder().item("wooden_pickaxe", 1).item("iron_pickaxe", 1).item("stone_axe", 1).build();
        assertEquals(ItemInfo.IRON, ItemInfo.bestTier(s, "pickaxe"));
        assertEquals(ItemInfo.STONE, ItemInfo.bestTier(s, "axe"));
        assertEquals(ItemInfo.NONE, ItemInfo.bestTier(s, "shovel"));
    }

    @Test
    void bestArmorTierCountsWornAndCarried() {
        Snapshot s = Snapshot.builder().armor("chest", "iron_chestplate").item("diamond_helmet", 1).build();
        assertEquals(ItemInfo.IRON, ItemInfo.bestArmorTier(s, "chest"));
        assertEquals(ItemInfo.DIAMOND, ItemInfo.bestArmorTier(s, "head"));
        assertEquals(ItemInfo.NONE, ItemInfo.bestArmorTier(s, "feet"));
    }

    @Test
    void foodUnitsAndCategories() {
        assertEquals(5, ItemInfo.foodUnits("bread"));
        assertEquals(8, ItemInfo.foodUnits("cooked_beef"));
        assertEquals(0, ItemInfo.foodUnits("dirt"));
        Snapshot s = Snapshot.builder().item("bread", 2).item("cooked_beef", 1).item("dirt", 64).build();
        assertEquals(18, ItemInfo.totalFoodUnits(s));
        assertTrue(ItemInfo.isLog("oak_log"));
        assertTrue(ItemInfo.isLog("crimson_stem"));
        assertTrue(ItemInfo.isPlanks("birch_planks"));
        assertTrue(ItemInfo.isBed("red_bed"));
        assertFalse(ItemInfo.isBed("bedrock"));
    }
}
