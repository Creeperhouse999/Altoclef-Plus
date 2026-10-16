package altoclefplus.inventory.logic;

import altoclefplus.core.logic.Snapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ToolsTest {
    private static ToolChoice.Candidate c(int slot, String id, float speed) {
        return new ToolChoice.Candidate(slot, id, speed);
    }

    @Test
    void picksFastestRealTool() {
        int slot = ToolChoice.pick(List.of(c(0, "iron_sword", 1.5f), c(1, "stone_axe", 4f), c(2, "wooden_axe", 2f)),
                Set.of(), Set.of());
        assertEquals(1, slot);
    }

    @Test
    void usesTheBestToolItOwns() {
        int slot = ToolChoice.pick(List.of(c(0, "crafting_table", 1f), c(1, "netherite_shovel", 9f), c(2, "stone_shovel", 4f)),
                Set.of(), Set.of());
        assertEquals(1, slot);
    }

    @Test
    void neverUsesArmorBlocksOrTablesToBreak() {
        int slot = ToolChoice.pick(List.of(c(0, "iron_helmet", 1f), c(1, "crafting_table", 1f), c(2, "", 1f)),
                Set.of("crafting_table"), Set.of());
        assertEquals(2, slot, "falls back to the empty hand slot");
    }

    @Test
    void fallsBackToJunkWhenNoEmptySlot() {
        int slot = ToolChoice.pick(List.of(c(0, "iron_helmet", 1f), c(3, "gunpowder", 1f)), Set.of(), Set.of("gunpowder"));
        assertEquals(3, slot);
    }

    @Test
    void returnsMinusOneWhenNothingSafe() {
        assertEquals(-1, ToolChoice.pick(List.of(c(0, "iron_helmet", 1f), c(1, "diamond", 1f)), Set.of("diamond"), Set.of()));
    }

    @Test
    void sparePlanCraftsAxeAndShovelAtStoneTier() {
        Snapshot s = Snapshot.builder().item("stone_pickaxe", 1).item("cobblestone", 10).item("oak_planks", 8).build();
        assertEquals(Optional.of("stone_axe"), SpareToolPlanner.next(s));

        Snapshot withAxe = s.toBuilder().item("stone_axe", 1).build();
        assertEquals(Optional.of("stone_shovel"), SpareToolPlanner.next(withAxe));
    }

    @Test
    void sparePlanUsesWoodBeforeStoneAndNeedsMaterialsOnHand() {
        Snapshot wood = Snapshot.builder().item("wooden_pickaxe", 1).item("oak_planks", 6).build();
        assertEquals(Optional.of("wooden_axe"), SpareToolPlanner.next(wood));

        Snapshot broke = Snapshot.builder().item("stone_pickaxe", 1).item("oak_planks", 1).build();
        assertEquals(Optional.empty(), SpareToolPlanner.next(broke));
    }

    @Test
    void sparePlanReplacesNearlyBrokenTool() {
        Snapshot s = Snapshot.builder().item("stone_pickaxe", 1).item("stone_axe", 1).item("stone_shovel", 1)
                .durability("stone_axe", 0.05f).item("cobblestone", 10).item("oak_planks", 8).build();
        assertEquals(Optional.of("stone_axe"), SpareToolPlanner.next(s));
    }

    @Test
    void cursorGuardStashesAfterTimeout() {
        CursorGuard g = new CursorGuard(40);
        for (int i = 0; i < 39; i++) assertEquals(CursorGuard.Action.NONE, g.tick(false, false));
        assertEquals(CursorGuard.Action.STASH, g.tick(false, false));
        assertEquals(CursorGuard.Action.NONE, g.tick(false, false), "resets after stashing");
    }

    @Test
    void cursorGuardResetsOnClickOrEmptyCursor() {
        CursorGuard g = new CursorGuard(3);
        g.tick(false, false);
        g.tick(false, false);
        g.tick(false, true);
        g.tick(false, false);
        g.tick(false, false);
        assertEquals(CursorGuard.Action.STASH, g.tick(false, false));
        g.tick(true, false);
        assertEquals(CursorGuard.Action.NONE, g.tick(false, false));
    }

    @Test
    void hotbarSelectsWhenAlreadyThereAndMovesToPinnedSlot() {
        HotbarLayout h = new HotbarLayout(Map.of("pickaxe", 1, "axe", 2), Set.of("cobblestone"), 200);
        List<String> bar = List.of("", "", "", "", "stone_axe", "", "", "", "");

        assertEquals(new HotbarLayout.Plan(HotbarLayout.Kind.SELECT, 4), h.plan(bar, "stone_axe", 0));
        assertEquals(new HotbarLayout.Plan(HotbarLayout.Kind.MOVE, 1), h.plan(bar, "iron_pickaxe", 0));
    }

    @Test
    void hotbarRespectsHysteresisForUnpinnedSlots() {
        HotbarLayout h = new HotbarLayout(Map.of("pickaxe", 1), Set.of(), 200);
        List<String> full = List.of("a", "b", "c", "d", "e", "f", "g", "h", "i");
        h.recordMove(7, 100);
        HotbarLayout.Plan p = h.plan(full, "torch", 150);
        assertNotEquals(7, p.slot(), "slot 7 was filled 50 ticks ago");
        assertNotEquals(1, p.slot(), "slot 1 is pinned for pickaxes");
    }

    @Test
    void cursorGuardIsPatientWhileAContainerIsOpen() {
        CursorGuard g = new CursorGuard(40);
        // A furnace/crafting task holds an item on the cursor between its own clicks: not stuck.
        for (int i = 0; i < 399; i++) assertEquals(CursorGuard.Action.NONE, g.tick(false, false, true));
        assertEquals(CursorGuard.Action.STASH, g.tick(false, false, true), "still rescues a truly stuck cursor");
    }

    @Test
    void sparePlanReplacesTheOnlyPickaxeBeforeItBreaks() {
        Snapshot base = Snapshot.builder().item("stone_pickaxe", 1).item("stone_axe", 1).item("stone_shovel", 1)
                .item("cobblestone", 10).item("oak_planks", 8).build();
        assertEquals(Optional.empty(), SpareToolPlanner.next(base), "healthy pickaxe: nothing to do");
        Snapshot worn = base.toBuilder().durability("stone_pickaxe", 0.12f).build();
        assertEquals(Optional.of("stone_pickaxe"), SpareToolPlanner.next(worn), "replace while we still have the stuff");
        Snapshot iron = Snapshot.builder().item("iron_pickaxe", 1).item("stone_axe", 1).item("stone_shovel", 1)
                .durability("iron_pickaxe", 0.1f).item("iron_ingot", 3).item("stick", 2).build();
        assertEquals(Optional.of("iron_pickaxe"), SpareToolPlanner.next(iron));
        Snapshot ironNoIngots = iron.toBuilder().item("iron_ingot", 0).item("cobblestone", 5).build();
        assertEquals(Optional.of("stone_pickaxe"), SpareToolPlanner.next(ironNoIngots), "at least a stone backup");
    }

    @Test
    void aReplacementAsksForOneMoreThanCarried() {
        Snapshot worn = Snapshot.builder().item("stone_pickaxe", 1).durability("stone_pickaxe", 0.12f).build();
        assertEquals(2, SpareToolPlanner.targetCount(worn, "stone_pickaxe"), "1 would be done at once: it already has one");
        assertEquals(1, SpareToolPlanner.targetCount(worn, "stone_axe"));
    }
}
