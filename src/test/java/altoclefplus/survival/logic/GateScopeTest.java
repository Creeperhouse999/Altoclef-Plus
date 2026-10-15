package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static altoclefplus.survival.logic.GatePlanner.Gate.*;
import static org.junit.jupiter.api.Assertions.*;

class GateScopeTest {
    private static Set<GatePlanner.Gate> needed(String command, String... targets) {
        return GateScope.allowed(GateScope.Scope.NEEDED, command, Set.of(targets));
    }

    @Test
    void errandsSkipGates() {
        assertEquals(EnumSet.noneOf(GatePlanner.Gate.class), needed("get dirt 64", "dirt"));
        assertEquals(EnumSet.noneOf(GatePlanner.Gate.class), needed("get oak_log 10", "oak_log"));
    }

    @Test
    void deepOresNeedTheBasicKit() {
        assertEquals(EnumSet.of(STONE_KIT, IRON_KIT), needed("get diamond 3", "diamond"));
        assertEquals(EnumSet.of(STONE_KIT, IRON_KIT), needed("get redstone 10", "redstone"));
    }

    @Test
    void netherItemsNeedNetherGear() {
        assertEquals(EnumSet.of(STONE_KIT, IRON_KIT, IRON_ARMOR, NETHER_ENTRY), needed("get quartz 16", "quartz"));
        assertEquals(EnumSet.of(STONE_KIT, IRON_KIT, IRON_ARMOR, NETHER_ENTRY), needed("get blaze_rod 6", "blaze_rod"));
    }

    @Test
    void endItemsNeedEverything() {
        assertEquals(EnumSet.complementOf(EnumSet.of(GatePlanner.Gate.EARLY_BED)), needed("get end_stone 32", "end_stone"));
    }

    @Test
    void beatTheGameNeedsEverything() {
        assertEquals(EnumSet.allOf(GatePlanner.Gate.class), needed("gamer"));
    }

    @Test
    void targetsAreReadFromTheCommandToo() {
        assertEquals(EnumSet.of(STONE_KIT, IRON_KIT, IRON_ARMOR, NETHER_ENTRY), needed("get quartz 4"));
    }

    @Test
    void otherScopes() {
        assertEquals(EnumSet.complementOf(EnumSet.of(GatePlanner.Gate.EARLY_BED)), GateScope.allowed(GateScope.Scope.ALL, "get dirt 1", Set.of("dirt")));
        assertEquals(EnumSet.noneOf(GatePlanner.Gate.class), GateScope.allowed(GateScope.Scope.GAMER_ONLY, "get diamond 3", Set.of("diamond")));
        assertEquals(EnumSet.allOf(GatePlanner.Gate.class), GateScope.allowed(GateScope.Scope.GAMER_ONLY, "gamer", Set.of()));
    }
}
