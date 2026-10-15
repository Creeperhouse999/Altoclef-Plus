package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveCellsTest {
    private static boolean contains(List<int[]> cells, int x, int y, int z) {
        return cells.stream().anyMatch(c -> c[0] == x && c[1] == y && c[2] == z);
    }

    @Test
    void diagonalTouchesBothCornersAndTheDestination() {
        List<int[]> cells = MoveCells.diagonal(0, 64, 0, 1, 1);
        assertTrue(contains(cells, 1, 64, 0), "corner");
        assertTrue(contains(cells, 0, 64, 1), "other corner");
        assertTrue(contains(cells, 1, 65, 0), "corner head height");
        assertTrue(contains(cells, 1, 64, 1), "destination");
        assertTrue(contains(cells, 1, 63, 1), "destination when stepping down");
        assertTrue(contains(cells, 1, 65, 1), "destination when stepping up");
        assertFalse(contains(cells, 0, 64, 0), "not the start");
    }

    @Test
    void stepUpAndDownTouchTheDestinationColumn() {
        List<int[]> up = MoveCells.vertical(0, 64, 0, 1, 0);
        assertTrue(contains(up, 1, 65, 0));
        assertTrue(contains(up, 1, 66, 0));
        List<int[]> down = MoveCells.vertical(0, 64, 0, 0, -1);
        assertTrue(contains(down, 0, 63, -1));
        assertTrue(contains(down, 0, 64, -1));
    }
}
