package altoclefplus.build.logic;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlacementAndMaterialsTest {
    static Schematic grid(int sx, int sy, int sz, String... statesInOrder) {
        String[] palette = Arrays.stream(statesInOrder).distinct().toArray(String[]::new);
        int[] cells = new int[sx * sy * sz];
        for (int i = 0; i < cells.length; i++) cells[i] = Arrays.asList(palette).indexOf(statesInOrder[i]);
        return new Schematic("t", "", sx, sy, sz, palette, cells, new HashMap<>(), List.of());
    }

    @Test
    void rotationsKeepTheSchematicInFrontOfYou() {
        Placement p = new Placement(100, 64, 200, 0);
        assertArrayEquals(new int[]{102, 65, 203}, p.toWorld(2, 1, 3));
        Placement cw = new Placement(100, 64, 200, 1); // clockwise 90: (x, z) -> (-z, x)
        assertArrayEquals(new int[]{97, 64, 202}, cw.toWorld(2, 0, 3));
        Placement half = new Placement(100, 64, 200, 2);
        assertArrayEquals(new int[]{98, 64, 197}, half.toWorld(2, 0, 3));
        Placement ccw = new Placement(100, 64, 200, 3);
        assertArrayEquals(new int[]{103, 64, 198}, ccw.toWorld(2, 0, 3));
    }

    @Test
    void facingPicksTheRotation() {
        assertEquals(0, Placement.rotationForFacing("south"));
        assertEquals(1, Placement.rotationForFacing("west"));
        assertEquals(2, Placement.rotationForFacing("north"));
        assertEquals(3, Placement.rotationForFacing("east"));
    }

    @Test
    void boundsCoverTheRotatedFootprint() {
        Placement cw = new Placement(0, 0, 0, 1);
        int[] b = cw.bounds(4, 2, 3); // minX, minY, minZ, maxX, maxY, maxZ
        assertArrayEquals(new int[]{-2, 0, 0, 0, 1, 3}, b);
    }

    @Test
    void materialsCountItemsNotBlocks() {
        Schematic s = grid(4, 1, 2,
                "minecraft:stone", "minecraft:stone", "minecraft:oak_door[facing=north,half=lower]", "minecraft:oak_door[facing=north,half=upper]",
                "minecraft:wall_torch[facing=north]", "minecraft:oak_slab[type=double]", "minecraft:air", "minecraft:white_bed[part=head]");
        Materials m = Materials.of(s);
        assertEquals(2, m.items().get("minecraft:stone"));
        assertEquals(1, m.items().get("minecraft:oak_door"), "a door is one item for two blocks");
        assertEquals(1, m.items().get("minecraft:torch"), "wall torch is placed from a torch");
        assertEquals(2, m.items().get("minecraft:oak_slab"), "double slab = 2 slabs");
        assertNull(m.items().get("minecraft:white_bed"), "head half only: the foot half carries the bed item");
        assertNull(m.items().get("minecraft:air"));
        assertTrue(m.unplaceable().isEmpty());
    }

    @Test
    void survivalImpossibleBlocksAreReported() {
        Schematic s = grid(3, 1, 1, "minecraft:command_block[conditional=false,facing=up]", "minecraft:spawner", "minecraft:redstone_wire[east=none,north=none,power=0,south=none,west=none]");
        Materials m = Materials.of(s);
        assertEquals(Map.of("minecraft:command_block", 1, "minecraft:spawner", 1), m.unplaceable());
        assertEquals(1, m.items().get("minecraft:redstone"), "redstone parts are normal blocks");
    }

    @Test
    void stillNeededSubtractsWhatIsCarried() {
        Materials m = Materials.of(grid(3, 1, 1, "minecraft:stone", "minecraft:stone", "minecraft:dirt"));
        Map<String, Integer> need = m.stillNeeded(Map.of("minecraft:stone", 1, "minecraft:dirt", 5));
        assertEquals(Map.of("minecraft:stone", 1), need);
    }
}
