package altoclefplus.inventory.logic;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JunkPitTest {
    /** Fake world: 'S' solid diggable, '.' air, 'W' water/lava, 'X' unbreakable/protected. */
    static JunkPit.World world(java.util.Map<String, Character> cells, char def) {
        return (x, y, z) -> switch (cells.getOrDefault(x + "," + y + "," + z, def)) {
            case '.' -> JunkPit.Cell.AIR;
            case 'W' -> JunkPit.Cell.FLUID;
            case 'X' -> JunkPit.Cell.KEEP;
            default -> JunkPit.Cell.SOLID;
        };
    }

    @Test
    void picksASideColumnNotUnderTheFeet() {
        // Standing at (0,64,0) on solid ground; everything below y=64 solid.
        var w = world(java.util.Map.of(), 'S');
        Optional<int[]> pit = JunkPit.choose(w, 0, 64, 0);
        assertTrue(pit.isPresent());
        int[] p = pit.get();
        assertEquals(64, p[1], "top of the pit is at foot level");
        assertEquals(1, Math.abs(p[0]) + Math.abs(p[2]), "directly beside the player");
    }

    @Test
    void avoidsColumnsNextToFluidOrProtectedBlocks() {
        var cells = new java.util.HashMap<String, Character>();
        cells.put("1,62,0", 'W');   // east column hits water
        cells.put("0,63,1", 'X');   // south column is protected
        cells.put("-1,63,0", 'X');  // west column is protected
        var w = world(cells, 'S');
        int[] p = JunkPit.choose(w, 0, 64, 0).orElseThrow();
        assertArrayEquals(new int[]{0, 64, -1}, p, "only the north column is safe");
    }

    @Test
    void reusesAnOpenPit() {
        assertTrue(JunkPit.isOpenPit(world(java.util.Map.of("1,64,0", '.', "1,63,0", '.', "1,62,0", '.'), 'S'), 1, 64, 0));
        assertFalse(JunkPit.isOpenPit(world(java.util.Map.of("1,64,0", '.', "1,63,0", '.'), 'S'), 1, 64, 0), "only 2 deep");
    }

    @Test
    void aimsAtTheFarSideOfTheOpeningNotTheBottom() {
        // Bot at (10.5, 64, 10.5) standing on y=64; pit column at x=11 (opening at y=64).
        double[] aim = JunkPit.aimPoint(10.5, 10.5, 11, 64, 10);
        assertEquals(64.0, aim[1], 1e-9, "ground level: a shallow enough throw to carry the item into the hole");
        assertTrue(aim[0] > 11.5 && aim[0] < 12.0, "past the centre, on the far side: x=" + aim[0]);
        assertEquals(10.5, aim[2], 1e-9);
    }
}
