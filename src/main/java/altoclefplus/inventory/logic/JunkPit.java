package altoclefplus.inventory.logic;

import java.util.Optional;

/**
 * Where to throw junk so the bot doesn't walk over it and pick it up again: a 1x1 hole, 3 deep, right beside
 * the player (never under its feet). Items 3 blocks down are out of pickup reach.
 */
public final class JunkPit {
    public enum Cell { AIR, SOLID, FLUID, KEEP }

    /** Block lookup: x, y, z -> what is there (KEEP = protected / unbreakable / container). */
    public interface World {
        Cell at(int x, int y, int z);
    }

    public static final int DEPTH = 3;
    private static final int[][] SIDES = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

    private JunkPit() {}

    /** Top block of a pit column beside (px, py, pz), at foot level, or empty if no side is safe to dig. */
    public static Optional<int[]> choose(World w, int px, int py, int pz) {
        for (int[] d : SIDES) {
            int x = px + d[0], z = pz + d[1];
            if (isOpenPit(w, x, py, z)) return Optional.of(new int[]{x, py, z});
        }
        for (int[] d : SIDES) {
            int x = px + d[0], z = pz + d[1];
            if (safeToDig(w, x, py, z)) return Optional.of(new int[]{x, py, z});
        }
        return Optional.empty();
    }

    public static boolean isOpenPit(World w, int x, int y, int z) {
        for (int i = 0; i < DEPTH; i++) if (w.at(x, y - i, z) != Cell.AIR) return false;
        return w.at(x, y - DEPTH, z) == Cell.SOLID;
    }

    /** The column (foot level and 2 below) can be dug, has a floor, and no fluid flows in from any side. */
    static boolean safeToDig(World w, int x, int y, int z) {
        for (int i = 0; i < DEPTH; i++) {
            Cell c = w.at(x, y - i, z);
            if (c == Cell.FLUID || c == Cell.KEEP) return false;
            for (int[] d : SIDES) if (w.at(x + d[0], y - i, z + d[1]) == Cell.FLUID) return false;
        }
        return w.at(x, y - DEPTH, z) == Cell.SOLID;
    }
}
