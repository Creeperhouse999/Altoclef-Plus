package altoclefplus.build.logic;

import net.minecraft.nbt.NbtCompound;

import java.util.List;
import java.util.Map;

/**
 * A loaded schematic in its own coordinates: (0,0,0) is the minimum corner. Each cell holds a block state
 * string like {@code minecraft:oak_stairs[facing=north,half=bottom]}, or null where the schematic has no
 * region (the world is left as it is there).
 *
 * @param blockEntities extra block data (chest contents, sign text) by packed position {@link #key}
 * @param entities      entities such as item frames, positions relative to the minimum corner
 */
public record Schematic(String name, String author, int sizeX, int sizeY, int sizeZ, String[] palette, int[] cells,
                        Map<Long, NbtCompound> blockEntities, List<EntityEntry> entities) {

    public record EntityEntry(String id, double x, double y, double z, NbtCompound nbt) {}

    /** Cell marker: not part of any region. */
    public static final int NONE = -1;

    public int index(int x, int y, int z) {
        return (y * sizeZ + z) * sizeX + x;
    }

    public boolean inBounds(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < sizeX && y < sizeY && z < sizeZ;
    }

    /** Block state at a position, or null if outside the regions. */
    public String stateAt(int x, int y, int z) {
        if (!inBounds(x, y, z)) return null;
        int p = cells[index(x, y, z)];
        return p == NONE ? null : palette[p];
    }

    public int nonAirCount() {
        int n = 0;
        for (int c : cells) if (c != NONE && !isAir(palette[c])) n++;
        return n;
    }

    public static boolean isAir(String state) {
        String id = blockId(state);
        return id.equals("minecraft:air") || id.equals("minecraft:cave_air") || id.equals("minecraft:void_air");
    }

    /** "minecraft:oak_stairs[facing=north]" -> "minecraft:oak_stairs". */
    public static String blockId(String state) {
        int b = state.indexOf('[');
        return b < 0 ? state : state.substring(0, b);
    }

    public static long key(int x, int y, int z) {
        return ((long) x & 0x3FFFFFL) << 42 | ((long) y & 0xFFFFFL) << 22 | ((long) z & 0x3FFFFFL);
    }
}
