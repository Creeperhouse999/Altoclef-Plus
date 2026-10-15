package altoclefplus.survival.logic;

import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GatePlannerTest {
    private final PlusConfig.Survival cfg = new PlusConfig().survival;

    private Optional<GatePlanner.Need> next(GatePlanner g, Snapshot s) {
        return g.next(s, cfg, 0);
    }

    private static String item(Optional<GatePlanner.Need> n) {
        return n.map(GatePlanner.Need::item).orElse("none");
    }

    @Test
    void nothingBeforeStonePickaxe() {
        assertEquals("none", item(next(new GatePlanner(), Snapshot.builder().item("wooden_pickaxe", 1).build())));
    }

    @Test
    void stoneKitAfterStonePickaxe() {
        GatePlanner g = new GatePlanner();
        Snapshot.Builder b = Snapshot.builder().item("stone_pickaxe", 1);
        assertEquals("stone_sword", item(next(g, b.build())));
        assertEquals("stone_axe", item(next(g, b.item("stone_sword", 1).build())));
        assertEquals("stone_shovel", item(next(g, b.item("stone_axe", 1).build())));
        assertEquals("none", item(next(g, b.item("stone_shovel", 1).build())));
    }

    @Test
    void ironKitThenArmorInOrder() {
        GatePlanner g = new GatePlanner();
        Snapshot.Builder b = Snapshot.builder().item("iron_pickaxe", 1).item("stone_sword", 1)
                .item("stone_axe", 1).item("stone_shovel", 1);
        assertEquals("iron_sword", item(next(g, b.build())));
        b.item("iron_sword", 1);
        assertEquals("shield", item(next(g, b.build())));
        b.offhand("shield");
        assertEquals("water_bucket", item(next(g, b.build())));
        b.item("water_bucket", 1);
        assertEquals("iron_chestplate", item(next(g, b.build())));
        b.armor("chest", "iron_chestplate").armor("legs", "iron_leggings").armor("head", "iron_helmet");
        assertEquals("iron_boots", item(next(g, b.build())));
        b.armor("feet", "golden_boots");
        assertEquals("iron_boots", item(next(g, b.build())), "golden boots don't count as iron armor");
        b.item("iron_boots", 1);
        assertEquals("none", item(next(g, b.build())));
    }

    @Test
    void emptyBucketSatisfiesWaterGate() {
        // AltoClef pours its water bucket out while casting obsidian; don't interrupt it to "get water".
        GatePlanner g = new GatePlanner();
        Snapshot s = Snapshot.builder().item("iron_pickaxe", 1).item("iron_sword", 1).item("stone_axe", 1)
                .item("stone_shovel", 1).offhand("shield").item("bucket", 1).build();
        assertEquals("iron_chestplate", item(next(g, s)));
    }

    @Test
    void gatesOutsideTheTaskScopeAreSkipped() {
        GatePlanner g = new GatePlanner();
        Snapshot s = Snapshot.builder().item("stone_pickaxe", 1).build();
        assertEquals("none", item(g.next(s, cfg, 0, java.util.EnumSet.noneOf(GatePlanner.Gate.class))));
        assertEquals("stone_sword", item(g.next(s, cfg, 0, java.util.EnumSet.of(GatePlanner.Gate.STONE_KIT))));
    }

    @Test
    void betterGearAlreadyOwnedSatisfiesTheGate() {
        GatePlanner g = new GatePlanner();
        Snapshot s = Snapshot.builder().item("stone_pickaxe", 1).item("netherite_shovel", 1)
                .item("diamond_axe", 1).item("iron_sword", 1).build();
        assertEquals("none", item(next(g, s)));
    }

    @Test
    void disabledGateIsSkipped() {
        GatePlanner g = new GatePlanner();
        cfg.gateStoneKit = false;
        assertEquals("none", item(next(g, Snapshot.builder().item("stone_pickaxe", 1).build())));
    }

    @Test
    void repeatedFailuresSkipGateForAWhile() {
        GatePlanner g = new GatePlanner();
        Snapshot s = Snapshot.builder().item("iron_pickaxe", 1).item("stone_sword", 1).item("stone_axe", 1)
                .item("stone_shovel", 1).item("iron_sword", 1).build();
        for (int i = 0; i < cfg.gateFailuresBeforeSkip; i++) g.markFailed(GatePlanner.Gate.IRON_KIT, 1000);

        assertNotEquals("shield", item(g.next(s, cfg, 2000)));
        long later = 1000 + cfg.gateSkipMinutes * 60_000L + 1;
        assertEquals("shield", item(g.next(s, cfg, later)));
    }

    @Test
    void netherEntryNeedsFoodWhenHeadingThere() {
        GatePlanner g = new GatePlanner();
        Snapshot s = Snapshot.builder().item("iron_pickaxe", 1).item("iron_sword", 1).item("stone_axe", 1)
                .item("stone_shovel", 1).offhand("shield").item("water_bucket", 1)
                .armor("chest", "iron_chestplate").armor("legs", "iron_leggings").armor("head", "iron_helmet")
                .armor("feet", "iron_boots").item("bread", 1)
                .taskChain(List.of("<Beat Minecraft> Going to Nether")).build();

        Optional<GatePlanner.Need> n = next(g, s);
        assertEquals("food", item(n));
        assertEquals(GatePlanner.Gate.NETHER_ENTRY, n.orElseThrow().gate());
        assertEquals(cfg.netherMinFoodUnits, n.orElseThrow().count());
    }

    @Test
    void netherEntryBringsABoatForEndermanHunting() {
        GatePlanner g = new GatePlanner();
        Snapshot s = Snapshot.builder().item("iron_pickaxe", 1).item("iron_sword", 1).item("stone_axe", 1)
                .item("stone_shovel", 1).offhand("shield").item("water_bucket", 1)
                .armor("chest", "iron_chestplate").armor("legs", "iron_leggings").armor("head", "iron_helmet")
                .armor("feet", "iron_boots").item("cooked_beef", 5)
                .taskChain(List.of("<Beat Minecraft> Going to Nether")).build();
        assertEquals("oak_boat", item(next(g, s)));
        assertEquals("none", item(next(g, s.toBuilder().item("birch_boat", 1).build())));
        cfg.endermanBoatHunt = false;
        assertEquals("none", item(next(g, s)));
    }

    @Test
    void netherGateQuietWhenNotHeadingThere() {
        GatePlanner g = new GatePlanner();
        Snapshot s = Snapshot.builder().item("bread", 1).taskChain(List.of("<Beat Minecraft> Collecting beds.")).build();
        assertEquals("none", item(next(g, s)));
    }
}
