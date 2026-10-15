package altoclefplus.build.logic;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reads Litematica's {@code .litematic} files (compressed NBT) without needing the Litematica mod: every
 * region's palette and packed block indexes, merged into one {@link Schematic} with (0,0,0) at the minimum
 * corner. Regions may have negative sizes (they extend backwards from their position).
 */
public final class LitematicReader {
    /** Refuse absurdly large files rather than running out of memory. */
    public static final long MAX_VOLUME = 16_000_000L;

    private LitematicReader() {}

    public static Schematic read(InputStream in) throws IOException {
        NbtCompound root = NbtIo.readCompressed(in);
        NbtCompound meta = root.getCompound("Metadata");
        NbtCompound regions = root.getCompound("Regions");
        if (regions.isEmpty()) throw new IOException("No regions in this file");

        // Pass 1: bounds of every region.
        List<Region> list = new ArrayList<>();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (String name : regions.getKeys()) {
            NbtCompound r = regions.getCompound(name);
            Region reg = new Region(r);
            list.add(reg);
            minX = Math.min(minX, reg.x0);
            minY = Math.min(minY, reg.y0);
            minZ = Math.min(minZ, reg.z0);
            maxX = Math.max(maxX, reg.x0 + reg.sx - 1);
            maxY = Math.max(maxY, reg.y0 + reg.sy - 1);
            maxZ = Math.max(maxZ, reg.z0 + reg.sz - 1);
        }
        int sx = maxX - minX + 1, sy = maxY - minY + 1, sz = maxZ - minZ + 1;
        if ((long) sx * sy * sz > MAX_VOLUME) throw new IOException("Schematic too big: " + sx + "x" + sy + "x" + sz);

        // Pass 2: one shared palette, cells filled region by region.
        Map<String, Integer> paletteIndex = new LinkedHashMap<>();
        int[] cells = new int[sx * sy * sz];
        Arrays.fill(cells, Schematic.NONE);
        Map<Long, NbtCompound> blockEntities = new HashMap<>();
        List<Schematic.EntityEntry> entities = new ArrayList<>();
        for (Region reg : list) {
            int[] map = new int[reg.palette.size()];
            for (int i = 0; i < map.length; i++) map[i] = paletteIndex.computeIfAbsent(reg.palette.get(i), k -> paletteIndex.size());
            int bits = Math.max(2, 32 - Integer.numberOfLeadingZeros(Math.max(1, reg.palette.size() - 1)));
            long mask = (1L << bits) - 1;
            int layer = reg.sx * reg.sz;
            for (int y = 0; y < reg.sy; y++) {
                for (int z = 0; z < reg.sz; z++) {
                    for (int x = 0; x < reg.sx; x++) {
                        int i = y * layer + z * reg.sx + x;
                        int p = (int) get(reg.states, i, bits, mask);
                        if (p < 0 || p >= map.length) continue;
                        int wx = reg.x0 + x - minX, wy = reg.y0 + y - minY, wz = reg.z0 + z - minZ;
                        cells[(wy * sz + wz) * sx + wx] = map[p];
                    }
                }
            }
            for (NbtElement e : reg.tileEntities) {
                if (!(e instanceof NbtCompound c)) continue;
                int wx = reg.x0 + c.getInt("x") - minX, wy = reg.y0 + c.getInt("y") - minY, wz = reg.z0 + c.getInt("z") - minZ;
                blockEntities.put(Schematic.key(wx, wy, wz), c);
            }
            for (NbtElement e : reg.entities) {
                if (!(e instanceof NbtCompound c)) continue;
                NbtList pos = c.getList("Pos", NbtElement.DOUBLE_TYPE);
                if (pos.size() != 3) continue;
                entities.add(new Schematic.EntityEntry(c.getString("id"), reg.x0 + pos.getDouble(0) - minX,
                        reg.y0 + pos.getDouble(1) - minY, reg.z0 + pos.getDouble(2) - minZ, c));
            }
        }
        String[] palette = paletteIndex.keySet().toArray(new String[0]);
        return new Schematic(meta.getString("Name"), meta.getString("Author"), sx, sy, sz, palette, cells,
                blockEntities, entities);
    }

    /** Litematica's bit array: a value may be split across two longs. */
    static long get(long[] arr, int index, int bits, long mask) {
        long start = (long) index * bits;
        int si = (int) (start >> 6);
        int ei = (int) (((long) (index + 1) * bits - 1) >> 6);
        int off = (int) (start & 63);
        if (si >= arr.length) return -1;
        if (si == ei || ei >= arr.length) return (arr[si] >>> off) & mask;
        int endOff = 64 - off;
        return ((arr[si] >>> off) | (arr[ei] << endOff)) & mask;
    }

    /** "minecraft:oak_stairs" + {facing:north, half:bottom} -> "minecraft:oak_stairs[facing=north,half=bottom]". */
    static String stateString(NbtCompound c) {
        String name = c.getString("Name");
        NbtCompound props = c.getCompound("Properties");
        if (props.isEmpty()) return name;
        Map<String, String> sorted = new TreeMap<>();
        for (String k : props.getKeys()) sorted.put(k, props.getString(k));
        StringBuilder sb = new StringBuilder(name).append('[');
        boolean first = true;
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            if (!first) sb.append(',');
            sb.append(e.getKey()).append('=').append(e.getValue());
            first = false;
        }
        return sb.append(']').toString();
    }

    private static final class Region {
        final int x0, y0, z0, sx, sy, sz;
        final List<String> palette = new ArrayList<>();
        final long[] states;
        final NbtList tileEntities;
        final NbtList entities;

        Region(NbtCompound r) {
            NbtCompound pos = r.getCompound("Position");
            NbtCompound size = r.getCompound("Size");
            int px = pos.getInt("x"), py = pos.getInt("y"), pz = pos.getInt("z");
            int rx = size.getInt("x"), ry = size.getInt("y"), rz = size.getInt("z");
            // Negative sizes extend backwards from the position.
            x0 = rx < 0 ? px + rx + 1 : px;
            y0 = ry < 0 ? py + ry + 1 : py;
            z0 = rz < 0 ? pz + rz + 1 : pz;
            sx = Math.abs(rx);
            sy = Math.abs(ry);
            sz = Math.abs(rz);
            for (NbtElement e : r.getList("BlockStatePalette", NbtElement.COMPOUND_TYPE)) palette.add(stateString((NbtCompound) e));
            states = r.getLongArray("BlockStates");
            tileEntities = r.getList("TileEntities", NbtElement.COMPOUND_TYPE);
            entities = r.getList("Entities", NbtElement.COMPOUND_TYPE);
        }
    }
}
