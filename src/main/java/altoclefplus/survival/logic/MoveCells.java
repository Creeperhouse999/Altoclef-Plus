package altoclefplus.survival.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Cells a Baritone move passes through (feet and head height, plus one step up or down). Used to keep
 * diagonal / step-up / step-down moves away from doors and fence gates, which only the straight move opens.
 */
public final class MoveCells {
    private MoveCells() {}

    /** Diagonal from (x,y,z) to (destX, y±1, destZ): both corners and the destination column. */
    public static List<int[]> diagonal(int x, int y, int z, int destX, int destZ) {
        List<int[]> cells = new ArrayList<>();
        for (int dy = 0; dy <= 1; dy++) {
            cells.add(new int[]{destX, y + dy, z});
            cells.add(new int[]{x, y + dy, destZ});
        }
        for (int dy = -1; dy <= 1; dy++) cells.add(new int[]{destX, y + dy, destZ});
        return cells;
    }

    /** Step up or down to (destX, y±1, destZ): the destination column from one below to two above. */
    public static List<int[]> vertical(int x, int y, int z, int destX, int destZ) {
        List<int[]> cells = new ArrayList<>();
        for (int dy = -1; dy <= 2; dy++) cells.add(new int[]{destX, y + dy, destZ});
        return cells;
    }
}
