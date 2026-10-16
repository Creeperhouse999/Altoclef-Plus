package altoclefplus.build.logic;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BlueprintPlannerTest {
    /** 3x1x1 row: stone, oak_stairs[facing=north], air. */
    private static Schematic row() {
        String[] palette = {"minecraft:stone", "minecraft:oak_stairs[facing=north]", "minecraft:air"};
        return new Schematic("t", "", 3, 1, 1, palette, new int[]{0, 1, 2}, new HashMap<>(), List.of());
    }

    @Test
    void missingBlocksAreGhostsWrongOnesAreMarkedPlacedOnesVanish() {
        Map<String, String> world = new HashMap<>();
        world.put("11,64,0", "minecraft:oak_stairs"); // placed (state differences don't matter)
        world.put("12,64,0", "minecraft:dirt");       // schematic wants air there: not shown
        BlueprintPlanner.WorldView view = (x, y, z) -> world.getOrDefault(x + "," + y + "," + z, "minecraft:air");
        List<BlueprintPlanner.Ghost> ghosts = BlueprintPlanner.plan(row(), new Placement(10, 64, 0, 0), view, 10, 64, 0, 48, 100);
        assertEquals(List.of(new BlueprintPlanner.Ghost(10, 64, 0, BlueprintPlanner.Kind.MISSING)), ghosts);

        world.put("10,64,0", "minecraft:cobblestone");
        ghosts = BlueprintPlanner.plan(row(), new Placement(10, 64, 0, 0), view, 10, 64, 0, 48, 100);
        assertEquals(List.of(new BlueprintPlanner.Ghost(10, 64, 0, BlueprintPlanner.Kind.WRONG)), ghosts);
    }

    @Test
    void onlyNearTheCameraAndClosestFirstUpToTheCap() {
        BlueprintPlanner.WorldView empty = (x, y, z) -> "minecraft:air";
        String[] palette = {"minecraft:stone"};
        Schematic line = new Schematic("t", "", 100, 1, 1, palette, new int[100], new HashMap<>(), List.of());
        List<BlueprintPlanner.Ghost> ghosts = BlueprintPlanner.plan(line, new Placement(0, 64, 0, 0), empty, 0, 64, 0, 10, 5);
        assertEquals(5, ghosts.size(), "capped");
        assertEquals(0, ghosts.get(0).x(), "closest first");
        assertTrue(BlueprintPlanner.plan(line, new Placement(0, 64, 0, 0), empty, 0, 64, 0, 10, 1000).stream().allMatch(g -> g.x() <= 10),
                "nothing beyond the radius");
    }
}
